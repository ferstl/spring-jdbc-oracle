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

import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.jdbc.core.ParameterDisposer;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.SqlTypeValue;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.support.SqlValue;

import oracle.jdbc.OraclePreparedStatement;

/**
 * JUnit tests for {@link OracleNamedParameterJdbcTemplate}.
 */
public class OracleNamedParameterJdbcTemplateTest {

  private OracleNamedParameterJdbcTemplate namedJdbcTemplate;

  @Before
  public void setUp() {
    JdbcOperations jdbcOperations = mock(JdbcOperations.class);
    this.namedJdbcTemplate = new OracleNamedParameterJdbcTemplate(jdbcOperations);
  }

  @Test
  public void endingNoSpace() throws SQLException {
    Map<String, Object> map = new HashMap<>(4);
    map.put("ten", 10);
    map.put("twenty", 20);
    String sql = "SELECT 1 FROM dual WHERE 1 = :ten or 20 = :twenty";
    PreparedStatementCreator preparedStatementCreator = this.namedJdbcTemplate.getPreparedStatementCreator(
            sql, new MapSqlParameterSource(map));

    Connection connection = mock(Connection.class);
    PreparedStatement preparedStatement = mock(PreparedStatement.class);
    OraclePreparedStatement oracleStatement = mock(OraclePreparedStatement.class);

    when(connection.prepareStatement(sql)).thenReturn(preparedStatement);
    when(preparedStatement.unwrap(OraclePreparedStatement.class)).thenReturn(oracleStatement);

    preparedStatementCreator.createPreparedStatement(connection);

    verify(oracleStatement).setObjectAtName("ten", 10);
    verify(oracleStatement).setObjectAtName("twenty", 20);
  }

  @Test
  public void setNullNoType() throws SQLException {
    Map<String, Object> map = new HashMap<>(2);
    map.put("ten", 10);
    map.put("twenty", null);
    String sql = "SELECT 1 FROM dual WHERE 1 = :ten or 20 = :twenty";
    PreparedStatementCreator preparedStatementCreator = this.namedJdbcTemplate.getPreparedStatementCreator(
            sql, new MapSqlParameterSource(map));

    Connection connection = mock(Connection.class);
    PreparedStatement preparedStatement = mock(PreparedStatement.class);
    OraclePreparedStatement oracleStatement = mock(OraclePreparedStatement.class);

    when(connection.prepareStatement(sql)).thenReturn(preparedStatement);
    when(preparedStatement.unwrap(OraclePreparedStatement.class)).thenReturn(oracleStatement);

    preparedStatementCreator.createPreparedStatement(connection);

    verify(oracleStatement).setObjectAtName("ten", 10);
    verify(oracleStatement).setNullAtName("twenty", Types.NULL);
  }

  @Test
  public void setWithType() throws SQLException {
    MapSqlParameterSource source = new MapSqlParameterSource(new HashMap<String, Object>(2));
    source.addValue("ten", 10, Types.NUMERIC);
    source.addValue("twenty", null, Types.VARCHAR);
    String sql = "SELECT 1 FROM dual WHERE 1 = :ten or 20 = :twenty";
    PreparedStatementCreator preparedStatementCreator = this.namedJdbcTemplate.getPreparedStatementCreator(
            sql, source);

    Connection connection = mock(Connection.class);
    PreparedStatement preparedStatement = mock(PreparedStatement.class);
    OraclePreparedStatement oracleStatement = mock(OraclePreparedStatement.class);

    when(connection.prepareStatement(sql)).thenReturn(preparedStatement);
    when(preparedStatement.unwrap(OraclePreparedStatement.class)).thenReturn(oracleStatement);

    preparedStatementCreator.createPreparedStatement(connection);

    verify(oracleStatement).setObjectAtName("ten", 10, Types.NUMERIC);
    verify(oracleStatement).setNullAtName("twenty", Types.VARCHAR);
  }

  @Test
  public void endingWithSpace() throws SQLException {
    Map<String, Object> map = new HashMap<>(4);
    map.put("ten", 10);
    map.put("twenty", 20);
    String sql = "SELECT 1 FROM dual WHERE 10 = :ten or 20 = :twenty ";
    PreparedStatementCreator preparedStatementCreator = this.namedJdbcTemplate.getPreparedStatementCreator(
            sql, new MapSqlParameterSource(map));

    Connection connection = mock(Connection.class);
    PreparedStatement preparedStatement = mock(PreparedStatement.class);
    OraclePreparedStatement oracleStatement = mock(OraclePreparedStatement.class);

    when(connection.prepareStatement(sql)).thenReturn(preparedStatement);
    when(preparedStatement.unwrap(OraclePreparedStatement.class)).thenReturn(oracleStatement);

    preparedStatementCreator.createPreparedStatement(connection);

    verify(oracleStatement).setObjectAtName("ten", 10);
    verify(oracleStatement).setObjectAtName("twenty", 20);
  }

  @Test
  public void repetition() throws SQLException {
    Map<String, Object> map = new HashMap<>(3);
    map.put("ten", 10);
    String sql = "SELECT 1 FROM dual WHERE 10 = :ten or 0 < :ten ";
    PreparedStatementCreator preparedStatementCreator = this.namedJdbcTemplate.getPreparedStatementCreator(
            sql, new MapSqlParameterSource(map));

    Connection connection = mock(Connection.class);
    PreparedStatement preparedStatement = mock(PreparedStatement.class);
    OraclePreparedStatement oracleStatement = mock(OraclePreparedStatement.class);

    when(connection.prepareStatement(sql)).thenReturn(preparedStatement);
    when(preparedStatement.unwrap(OraclePreparedStatement.class)).thenReturn(oracleStatement);

    preparedStatementCreator.createPreparedStatement(connection);

    verify(oracleStatement).setObjectAtName("ten", 10);
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
    OraclePreparedStatement oracleStatement = mock(OraclePreparedStatement.class);

    when(connection.prepareStatement(sql)).thenReturn(preparedStatement);
    when(preparedStatement.unwrap(OraclePreparedStatement.class)).thenReturn(oracleStatement);

    preparedStatementCreator.createPreparedStatement(connection);

    verify(oracleStatement).setObjectAtName("arg", 10);
    verify(oracleStatement).setObjectAtName("arg2", 20);
  }

  @Test
  public void collectionUnsupported() throws SQLException {
    Map<String, Object> map = new HashMap<>(3);
    map.put("ten", 10);
    map.put("twenty", 20);
    map.put("collection", Arrays.asList(1, 23, 42));
    String sql = "SELECT 1 FROM dual WHERE 10 = :ten or 42 in (:collection) or 20 = :twenty";
    PreparedStatementCreator preparedStatementCreator = this.namedJdbcTemplate.getPreparedStatementCreator(
            sql,
            new MapSqlParameterSource(map));

    Connection connection = mock(Connection.class);
    PreparedStatement preparedStatement = mock(PreparedStatement.class);
    OraclePreparedStatement oraclePreparedStatement = mock(OraclePreparedStatement.class);

    when(preparedStatement.unwrap(OraclePreparedStatement.class)).thenReturn(oraclePreparedStatement);
    when(connection.prepareStatement(sql)).thenReturn(preparedStatement);

    try {
      preparedStatementCreator.createPreparedStatement(connection);
      fail("connection is currently unsupported");
    } catch (IllegalArgumentException e) {
      // should reach here
    }
  }
  
  @Test
  public void sqlValueUnsupported() throws SQLException {
    Map<String, Object> map = Collections.singletonMap("collection", mock(SqlValue.class));
    String sql = "SELECT 1 FROM dual WHERE 10 = ANY(:collection)";
    PreparedStatementCreator preparedStatementCreator = this.namedJdbcTemplate.getPreparedStatementCreator(
            sql,
            new MapSqlParameterSource(map));
    
    Connection connection = mock(Connection.class);
    PreparedStatement preparedStatement = mock(PreparedStatement.class);
    OraclePreparedStatement oraclePreparedStatement = mock(OraclePreparedStatement.class);
    
    when(preparedStatement.unwrap(OraclePreparedStatement.class)).thenReturn(oraclePreparedStatement);
    when(connection.prepareStatement(sql)).thenReturn(preparedStatement);
    
    try {
      preparedStatementCreator.createPreparedStatement(connection);
      fail("SqlValue is currently unsupported");
    } catch (IllegalArgumentException e) {
      // should reach here
    }
  }
  
  @Test
  public void sqlTypeValueUnsupported() throws SQLException {
    Map<String, Object> map = Collections.singletonMap("collection", mock(SqlTypeValue.class));
    String sql = "SELECT 1 FROM dual WHERE 10 = ANY(:collection)";
    PreparedStatementCreator preparedStatementCreator = this.namedJdbcTemplate.getPreparedStatementCreator(
            sql,
            new MapSqlParameterSource(map));
    
    Connection connection = mock(Connection.class);
    PreparedStatement preparedStatement = mock(PreparedStatement.class);
    OraclePreparedStatement oraclePreparedStatement = mock(OraclePreparedStatement.class);
    
    when(preparedStatement.unwrap(OraclePreparedStatement.class)).thenReturn(oraclePreparedStatement);
    when(connection.prepareStatement(sql)).thenReturn(preparedStatement);
    
    try {
      preparedStatementCreator.createPreparedStatement(connection);
      fail("SqlValue is currently unsupported");
    } catch (IllegalArgumentException e) {
      // should reach here
    }
  }

  @Test
  public void array() throws SQLException {
    NamedSqlValue namedSqlValue = mock(NamedSqlValue.class);

    Map<String, Object> map = new HashMap<>(4);
    map.put("ten", 10);
    map.put("twenty", 20);
    map.put("collection", namedSqlValue);
    String sql = "SELECT 1 FROM dual WHERE 10 = :ten or 42 = ANY(:collection) or 20 = :twenty";
    PreparedStatementCreator preparedStatementCreator = this.namedJdbcTemplate.getPreparedStatementCreator(
            sql,
            new MapSqlParameterSource(map));

    Connection connection = mock(Connection.class);
    PreparedStatement preparedStatement = mock(PreparedStatement.class);
    OraclePreparedStatement oraclePreparedStatement = mock(OraclePreparedStatement.class);

    when(preparedStatement.unwrap(OraclePreparedStatement.class)).thenReturn(oraclePreparedStatement);
    when(connection.prepareStatement(sql)).thenReturn(preparedStatement);

    preparedStatementCreator.createPreparedStatement(connection);
    ((ParameterDisposer) preparedStatementCreator).cleanupParameters();

    verify(oraclePreparedStatement).setObjectAtName("ten", 10);
    verify(oraclePreparedStatement).setObjectAtName("twenty", 20);
    verify(namedSqlValue).setValue(oraclePreparedStatement, "collection");
    verify(namedSqlValue).cleanup();
  }

}
