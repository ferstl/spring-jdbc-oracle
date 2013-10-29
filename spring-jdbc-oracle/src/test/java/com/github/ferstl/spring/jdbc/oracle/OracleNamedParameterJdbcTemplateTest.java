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

}
