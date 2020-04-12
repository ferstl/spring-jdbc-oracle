package com.github.ferstl.spring.jdbc.oracle;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.Array;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.junit.jupiter.api.Test;

import oracle.jdbc.OracleConnection;
import oracle.jdbc.OraclePreparedStatement;

public class SqlOracleArrayValueTest {

  @Test
  public void execution() throws SQLException {
    Object[] values = new Object[] {1L, 2L, 3L};
    String typeName = "CUSTOM_ARRAY_TYPE";
    String paramName = "parameter1";
    NamedSqlValue value = new SqlOracleArrayValue(typeName , values);

    Connection connection = mock(Connection.class);
    OracleConnection oracleConnection = mock(OracleConnection.class);
    PreparedStatement preparedStatement = mock(PreparedStatement.class);
    OraclePreparedStatement oraclePreparedStatement = mock(OraclePreparedStatement.class);
    Array array = mock(Array.class);

    when(connection.unwrap(OracleConnection.class)).thenReturn(oracleConnection);
    when(preparedStatement.unwrap(OraclePreparedStatement.class)).thenReturn(oraclePreparedStatement);
    when(preparedStatement.getConnection()).thenReturn(connection);

    when(oracleConnection.createOracleArray(typeName, values)).thenReturn(array);

    value.setValue(preparedStatement, paramName);

    verify(oraclePreparedStatement).setArrayAtName(paramName, array);
    verify(array, never()).free();

    value.cleanup();

    verify(array).free();

  }

}
