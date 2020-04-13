package com.github.ferstl.spring.jdbc.oracle;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;

import javax.sql.DataSource;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.SqlProvider;

import oracle.jdbc.OracleConnection;
import oracle.jdbc.OraclePreparedStatement;

public class CachedPreparedStatementCreatorTest {

  private JdbcOperations jdbcOperations;
  private OracleConnection connection;

  @BeforeEach
  public void setUp() throws SQLException {
    DataSource dataSource = mock(DataSource.class);
    this.connection = mock(OracleConnection.class);
    when(dataSource.getConnection()).thenReturn(this.connection);
    when(connection.unwrap(OracleConnection.class)).thenReturn(this.connection);

    this.jdbcOperations = new JdbcTemplate(dataSource);
  }

  @Test
  public void notCached() throws SQLException {
    String key = "key";
    String sql = "SELECT 1 FROM dual";


    ResultSet resultSet = mock(ResultSet.class);
    when(resultSet.next()).thenReturn(true, false);
    when(resultSet.getInt(1)).thenReturn(1);

    OraclePreparedStatement preparedStatement = mock(OraclePreparedStatement.class);
    when(preparedStatement.unwrap(OraclePreparedStatement.class)).thenReturn(preparedStatement);
    when(preparedStatement.executeQuery()).thenReturn(resultSet);

    when(this.connection.getStatementWithKey(key)).thenReturn(null);
    when(this.connection.prepareStatement(sql)).thenReturn(preparedStatement);

    PreparedStatementCreator creator = new CachedPreparedStatementCreator(key, sql);
    List<Integer> result = this.jdbcOperations.query(creator, (rs, i) -> rs.getInt(1));
    assertEquals(Collections.singletonList(1), result);
    assertEquals(sql, ((SqlProvider) creator).getSql());

    verify(this.connection).getStatementWithKey(key);
    verify(preparedStatement).closeWithKey(key);
    verify(preparedStatement, never()).close();
  }

  @Test
  public void cached() throws SQLException {
    String key = "key";
    String sql = "SELECT 1 FROM dual";


    ResultSet resultSet = mock(ResultSet.class);
    when(resultSet.next()).thenReturn(true, false);
    when(resultSet.getInt(1)).thenReturn(1);

    OraclePreparedStatement preparedStatement = mock(OraclePreparedStatement.class);
    when(preparedStatement.unwrap(OraclePreparedStatement.class)).thenReturn(preparedStatement);
    when(preparedStatement.executeQuery()).thenReturn(resultSet);

    when(this.connection.getStatementWithKey(key)).thenReturn(preparedStatement);
    when(this.connection.prepareStatement(sql)).thenReturn(preparedStatement);

    PreparedStatementCreator creator = new CachedPreparedStatementCreator(key, sql);
    List<Integer> result = this.jdbcOperations.query(creator, (rs, i) -> rs.getInt(1));
    assertEquals(Collections.singletonList(1), result);
    assertEquals(sql, ((SqlProvider) creator).getSql());

    verify(this.connection).getStatementWithKey(key);
    verify(preparedStatement).closeWithKey(key);
    verify(this.connection, never()).prepareStatement(sql);
    verify(preparedStatement, never()).close();
  }

}
