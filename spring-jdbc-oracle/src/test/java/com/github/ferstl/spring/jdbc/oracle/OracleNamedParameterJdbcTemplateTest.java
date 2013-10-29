/*
 * Copyright (c) 2013 by Philippe Marschall <philippe.marschall@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.ferstl.spring.jdbc.oracle;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;

/**
 * JUnit tests for {@link OracleNamedParameterJdbcTemplate}.
 */
public class OracleNamedParameterJdbcTemplateTest {

    private OracleNamedParameterJdbcTemplate namedJdbcTemplate;

    @Before
    public void setUp() {
        JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);
        this.namedJdbcTemplate = new OracleNamedParameterJdbcTemplate(jdbcTemplate);
    }

    @Test
    public void endingNoSpace() throws SQLException {
        Map<String, Object> map = new HashMap<>(3);
        map.put("one", 1);
        map.put("ten", 10);
        map.put("twenty", 20);
        String sql = "SELECT 1 FROM dual WHERE 1 = :ten or 20 = :twenty";
        PreparedStatementCreator preparedStatementCreator = this.namedJdbcTemplate.getPreparedStatementCreator(
                sql, new MapSqlParameterSource(map));

        Connection connection = mock(Connection.class);
        PreparedStatement preparedStatement = mock(PreparedStatement.class);

        when(connection.prepareStatement(sql)).thenReturn(preparedStatement);

        preparedStatementCreator.createPreparedStatement(connection);

        verify(preparedStatement).setObject(1, 10);
        verify(preparedStatement).setObject(2, 20);
    }

    @Test
    public void endingWithSpace() throws SQLException {
        Map<String, Object> map = new HashMap<>(3);
        map.put("one", 1);
        map.put("ten", 10);
        map.put("twenty", 20);
        String sql = "SELECT 1 FROM dual WHERE 10 = :ten or 20 = :twenty ";
        PreparedStatementCreator preparedStatementCreator = this.namedJdbcTemplate.getPreparedStatementCreator(
                sql, new MapSqlParameterSource(map));

        Connection connection = mock(Connection.class);
        PreparedStatement preparedStatement = mock(PreparedStatement.class);

        when(connection.prepareStatement(sql)).thenReturn(preparedStatement);

        preparedStatementCreator.createPreparedStatement(connection);

        verify(preparedStatement).setObject(1, 10);
        verify(preparedStatement).setObject(2, 20);
    }
    
    @Test
    public void repetition() throws SQLException {
      Map<String, Object> map = new HashMap<>(3);
      map.put("one", 1);
      map.put("ten", 10);
      map.put("twenty", 20);
      String sql = "SELECT 1 FROM dual WHERE 10 = :ten or 0 < :ten ";
      PreparedStatementCreator preparedStatementCreator = this.namedJdbcTemplate.getPreparedStatementCreator(
          sql, new MapSqlParameterSource(map));
      
      Connection connection = mock(Connection.class);
      PreparedStatement preparedStatement = mock(PreparedStatement.class);
      
      when(connection.prepareStatement(sql)).thenReturn(preparedStatement);
      
      preparedStatementCreator.createPreparedStatement(connection);
      
      verify(preparedStatement).setObject(1, 10);
      verify(preparedStatement).setObject(2, 10);
    }

    @Test
    public void commonPrefix() throws SQLException {
        Map<String, Object> map = new HashMap<>(3);
        map.put("arg", 10);
        map.put("arg2", 20);
        String sql = "SELECT 1 FROM dual WHERE 10 = :arg or 20 = :arg2";
        PreparedStatementCreator preparedStatementCreator = this.namedJdbcTemplate.getPreparedStatementCreator(
                sql, new MapSqlParameterSource(map));

        Connection connection = mock(Connection.class);
        PreparedStatement preparedStatement = mock(PreparedStatement.class);

        when(connection.prepareStatement(sql)).thenReturn(preparedStatement);

        preparedStatementCreator.createPreparedStatement(connection);

        verify(preparedStatement).setObject(1, 10);
        verify(preparedStatement).setObject(2, 20);
    }

    @Test
    public void collection() throws SQLException {
        Map<String, Object> map = new HashMap<>(3);
        map.put("ten", 10);
        map.put("twenty", 20);
        map.put("collection", Arrays.asList(1, 23, 42));
        String sql = "SELECT 1 FROM dual WHERE 10 = :ten or 42 in (:collection) or 20 = :twenty";
        String expectedSql = "SELECT 1 FROM dual WHERE 10 = :ten or 42 in (?,?,?) or 20 = :twenty";
        PreparedStatementCreator preparedStatementCreator = this.namedJdbcTemplate.getPreparedStatementCreator(
                sql,
                new MapSqlParameterSource(map));

        Connection connection = mock(Connection.class);
        PreparedStatement preparedStatement = mock(PreparedStatement.class);

        when(connection.prepareStatement(expectedSql)).thenReturn(preparedStatement);

        preparedStatementCreator.createPreparedStatement(connection);

        verify(preparedStatement).setObject(1, 10);
        verify(preparedStatement).setObject(2, 1);
        verify(preparedStatement).setObject(3, 23);
        verify(preparedStatement).setObject(4, 42);
        verify(preparedStatement).setObject(5, 20);
    }

    @Test
    public void endsWithCollection() throws SQLException {
        Map<String, Object> map = new HashMap<>(3);
        map.put("ten", 10);
        map.put("twenty", 20);
        map.put("collection", Arrays.asList(1, 23, 42));
        String sql = "SELECT 1 FROM dual WHERE 10 = :ten or 42 in (:collection)";
        String expectedSql = "SELECT 1 FROM dual WHERE 10 = :ten or 42 in (?,?,?)";
        PreparedStatementCreator preparedStatementCreator = this.namedJdbcTemplate.getPreparedStatementCreator(
                sql,
                new MapSqlParameterSource(map));

        Connection connection = mock(Connection.class);
        PreparedStatement preparedStatement = mock(PreparedStatement.class);

        when(connection.prepareStatement(expectedSql)).thenReturn(preparedStatement);

        preparedStatementCreator.createPreparedStatement(connection);

        verify(preparedStatement).setObject(1, 10);
        verify(preparedStatement).setObject(2, 1);
        verify(preparedStatement).setObject(3, 23);
        verify(preparedStatement).setObject(4, 42);
    }

    @Test
    public void endsWithCollectionSpace() throws SQLException {
        Map<String, Object> map = new HashMap<>(3);
        map.put("ten", 10);
        map.put("twenty", 20);
        map.put("collection", Arrays.asList(1, 23, 42));
        String sql = "SELECT 1 FROM dual WHERE 10 = :ten or 42 in (:collection) ";
        String expectedSql = "SELECT 1 FROM dual WHERE 10 = :ten or 42 in (?,?,?) ";
        PreparedStatementCreator preparedStatementCreator = this.namedJdbcTemplate.getPreparedStatementCreator(
                sql,
                new MapSqlParameterSource(map));

        Connection connection = mock(Connection.class);
        PreparedStatement preparedStatement = mock(PreparedStatement.class);

        when(connection.prepareStatement(expectedSql)).thenReturn(preparedStatement);

        preparedStatementCreator.createPreparedStatement(connection);

        verify(preparedStatement).setObject(1, 10);
        verify(preparedStatement).setObject(2, 1);
        verify(preparedStatement).setObject(3, 23);
        verify(preparedStatement).setObject(4, 42);
    }

}
