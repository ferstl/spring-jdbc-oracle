/*
 * Copyright (c) 2019 by Philippe Marschall <philippe.marschall@gmail.com>
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

import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.Array;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.Date;
import java.sql.NClob;
import java.sql.ParameterMetaData;
import java.sql.PreparedStatement;
import java.sql.Ref;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.RowId;
import java.sql.SQLException;
import java.sql.SQLType;
import java.sql.SQLWarning;
import java.sql.SQLXML;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Objects;

import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.SqlProvider;

import oracle.jdbc.OracleConnection;
import oracle.jdbc.OraclePreparedStatement;

/**
 * A {@link PreparedStatementCreator} that causes OJDBC explicit
 * statement caching to be used. This can be used to limit the number of
 * soft parses in you application.
 * 
 * <p>Make sure you
 * <a href="https://docs.oracle.com/en/database/oracle/oracle-database/21/jjdbc/statement-and-resultset-caching.html#GUID-3E425401-A7F0-49FA-A057-01DB6ECCFFC9">enable explicit statement caching</a>
 * .</p>
 * 
 * @see <a href="https://docs.oracle.com/en/database/oracle/oracle-database/21/jjdbc/statement-and-resultset-caching.html#GUID-DFBC7F09-5F27-42E1-8044-24733A6AE5F8">Explicit Statement Caching</a>
 * @see JdbcOperations#query(PreparedStatementCreator, org.springframework.jdbc.core.ResultSetExtractor)
 * @see JdbcOperations#query(PreparedStatementCreator, org.springframework.jdbc.core.RowCallbackHandler)
 * @see JdbcOperations#query(PreparedStatementCreator, org.springframework.jdbc.core.RowMapper)
 * @see JdbcTemplate#query(PreparedStatementCreator, org.springframework.jdbc.core.PreparedStatementSetter, org.springframework.jdbc.core.ResultSetExtractor)
 */
public final class CachedPreparedStatementCreator implements PreparedStatementCreator, SqlProvider {

  private final String key;
  private final String sql;

  /**
   * Creates a CachedPreparedStatementCreator.
   * 
   * @param key the cache key for the created prepared statement,
   *        has to be unique, not {@code null}
   * @param sql SQL query string for the cached prepared statement,
   *        not {@code null}
   */
  public CachedPreparedStatementCreator(String key, String sql) {
    Objects.requireNonNull(key, "key");
    Objects.requireNonNull(sql, "sql");
    this.key = key;
    this.sql = sql;
  }

  @Override
  public String getSql() {
    return this.sql;
  }

  @Override
  public PreparedStatement createPreparedStatement(Connection connection) throws SQLException {
    OracleConnection oracleConnection = connection.unwrap(OracleConnection.class);
    PreparedStatement statement = oracleConnection.getStatementWithKey(this.key);
    if (statement == null) {
      statement = connection.prepareStatement(this.sql);
    }
    return new CachedPreparedStatement(this.key, statement);
  }

  /**
   * Ensures that instead of being closed the statement is instead returned to the pool.
   */
  static final class CachedPreparedStatement implements PreparedStatement {

    private final String key;
    private final PreparedStatement delegate;

    CachedPreparedStatement(String key, PreparedStatement delegate) {
      Objects.requireNonNull(key, "key");
      Objects.requireNonNull(delegate, "delegate");
      this.key = key;
      this.delegate = delegate;
    }

    @Override
    public void close() throws SQLException {
      this.delegate.unwrap(OraclePreparedStatement.class).closeWithKey(this.key);
    }

    @Override
    public <T> T unwrap(Class<T> iface) throws SQLException {
      return this.delegate.unwrap(iface);
    }

    @Override
    public ResultSet executeQuery(String sql) throws SQLException {
      return this.delegate.executeQuery(sql);
    }

    @Override
    public ResultSet executeQuery() throws SQLException {
      return this.delegate.executeQuery();
    }

    @Override
    public boolean isWrapperFor(Class<?> iface) throws SQLException {
      return this.delegate.isWrapperFor(iface);
    }

    @Override
    public int executeUpdate(String sql) throws SQLException {
      return this.delegate.executeUpdate(sql);
    }

    @Override
    public int executeUpdate() throws SQLException {
      return this.delegate.executeUpdate();
    }

    @Override
    public void setNull(int parameterIndex, int sqlType) throws SQLException {
      this.delegate.setNull(parameterIndex, sqlType);
    }

    @Override
    public int getMaxFieldSize() throws SQLException {
      return this.delegate.getMaxFieldSize();
    }

    @Override
    public void setBoolean(int parameterIndex, boolean x) throws SQLException {
      this.delegate.setBoolean(parameterIndex, x);
    }

    @Override
    public void setByte(int parameterIndex, byte x) throws SQLException {
      this.delegate.setByte(parameterIndex, x);
    }

    @Override
    public void setMaxFieldSize(int max) throws SQLException {
      this.delegate.setMaxFieldSize(max);
    }

    @Override
    public void setShort(int parameterIndex, short x) throws SQLException {
      this.delegate.setShort(parameterIndex, x);
    }

    @Override
    public int getMaxRows() throws SQLException {
      return this.delegate.getMaxRows();
    }

    @Override
    public void setInt(int parameterIndex, int x) throws SQLException {
      this.delegate.setInt(parameterIndex, x);
    }

    @Override
    public void setMaxRows(int max) throws SQLException {
      this.delegate.setMaxRows(max);
    }

    @Override
    public void setLong(int parameterIndex, long x) throws SQLException {
      this.delegate.setLong(parameterIndex, x);
    }

    @Override
    public void setFloat(int parameterIndex, float x) throws SQLException {
      this.delegate.setFloat(parameterIndex, x);
    }

    @Override
    public void setEscapeProcessing(boolean enable) throws SQLException {
      this.delegate.setEscapeProcessing(enable);
    }

    @Override
    public void setDouble(int parameterIndex, double x) throws SQLException {
      this.delegate.setDouble(parameterIndex, x);
    }

    @Override
    public int getQueryTimeout() throws SQLException {
      return this.delegate.getQueryTimeout();
    }

    @Override
    public void setBigDecimal(int parameterIndex, BigDecimal x)
            throws SQLException {
      this.delegate.setBigDecimal(parameterIndex, x);
    }

    @Override
    public void setQueryTimeout(int seconds) throws SQLException {
      this.delegate.setQueryTimeout(seconds);
    }

    @Override
    public void setString(int parameterIndex, String x) throws SQLException {
      this.delegate.setString(parameterIndex, x);
    }

    @Override
    public void setBytes(int parameterIndex, byte[] x) throws SQLException {
      this.delegate.setBytes(parameterIndex, x);
    }

    @Override
    public void cancel() throws SQLException {
      this.delegate.cancel();
    }

    @Override
    public void setDate(int parameterIndex, Date x) throws SQLException {
      this.delegate.setDate(parameterIndex, x);
    }

    @Override
    public SQLWarning getWarnings() throws SQLException {
      return this.delegate.getWarnings();
    }

    @Override
    public void setTime(int parameterIndex, Time x) throws SQLException {
      this.delegate.setTime(parameterIndex, x);
    }

    @Override
    public void setTimestamp(int parameterIndex, Timestamp x) throws SQLException {
      this.delegate.setTimestamp(parameterIndex, x);
    }

    @Override
    public void clearWarnings() throws SQLException {
      this.delegate.clearWarnings();
    }

    @Override
    public void setCursorName(String name) throws SQLException {
      this.delegate.setCursorName(name);
    }

    @Override
    public void setAsciiStream(int parameterIndex, InputStream x, int length) throws SQLException {
      this.delegate.setAsciiStream(parameterIndex, x, length);
    }

    @Override
    @Deprecated
    public void setUnicodeStream(int parameterIndex, InputStream x, int length) throws SQLException {
      this.delegate.setUnicodeStream(parameterIndex, x, length);
    }

    @Override
    public boolean execute(String sql) throws SQLException {
      return this.delegate.execute(sql);
    }

    @Override
    public void setBinaryStream(int parameterIndex, InputStream x, int length) throws SQLException {
      this.delegate.setBinaryStream(parameterIndex, x, length);
    }

    @Override
    public ResultSet getResultSet() throws SQLException {
      return this.delegate.getResultSet();
    }

    @Override
    public int getUpdateCount() throws SQLException {
      return this.delegate.getUpdateCount();
    }

    @Override
    public void clearParameters() throws SQLException {
      this.delegate.clearParameters();
    }

    @Override
    public boolean getMoreResults() throws SQLException {
      return this.delegate.getMoreResults();
    }

    @Override
    public void setObject(int parameterIndex, Object x, int targetSqlType) throws SQLException {
      this.delegate.setObject(parameterIndex, x, targetSqlType);
    }

    @Override
    public void setFetchDirection(int direction) throws SQLException {
      this.delegate.setFetchDirection(direction);
    }

    @Override
    public void setObject(int parameterIndex, Object x) throws SQLException {
      this.delegate.setObject(parameterIndex, x);
    }

    @Override
    public int getFetchDirection() throws SQLException {
      return this.delegate.getFetchDirection();
    }

    @Override
    public void setFetchSize(int rows) throws SQLException {
      this.delegate.setFetchSize(rows);
    }

    @Override
    public int getFetchSize() throws SQLException {
      return this.delegate.getFetchSize();
    }

    @Override
    public boolean execute() throws SQLException {
      return this.delegate.execute();
    }

    @Override
    public int getResultSetConcurrency() throws SQLException {
      return this.delegate.getResultSetConcurrency();
    }

    @Override
    public int getResultSetType() throws SQLException {
      return this.delegate.getResultSetType();
    }

    @Override
    public void addBatch(String sql) throws SQLException {
      this.delegate.addBatch(sql);
    }

    @Override
    public void addBatch() throws SQLException {
      this.delegate.addBatch();
    }

    @Override
    public void setCharacterStream(int parameterIndex, Reader reader, int length) throws SQLException {
      this.delegate.setCharacterStream(parameterIndex, reader, length);
    }

    @Override
    public void clearBatch() throws SQLException {
      this.delegate.clearBatch();
    }

    @Override
    public int[] executeBatch() throws SQLException {
      return this.delegate.executeBatch();
    }

    @Override
    public void setRef(int parameterIndex, Ref x) throws SQLException {
      this.delegate.setRef(parameterIndex, x);
    }

    @Override
    public void setBlob(int parameterIndex, Blob x) throws SQLException {
      this.delegate.setBlob(parameterIndex, x);
    }

    @Override
    public void setClob(int parameterIndex, Clob x) throws SQLException {
      this.delegate.setClob(parameterIndex, x);
    }

    @Override
    public void setArray(int parameterIndex, Array x) throws SQLException {
      this.delegate.setArray(parameterIndex, x);
    }

    @Override
    public Connection getConnection() throws SQLException {
      return this.delegate.getConnection();
    }

    @Override
    public ResultSetMetaData getMetaData() throws SQLException {
      return this.delegate.getMetaData();
    }

    @Override
    public void setDate(int parameterIndex, Date x, Calendar cal)
            throws SQLException {
      this.delegate.setDate(parameterIndex, x, cal);
    }

    @Override
    public boolean getMoreResults(int current) throws SQLException {
      return this.delegate.getMoreResults(current);
    }

    @Override
    public void setTime(int parameterIndex, Time x, Calendar cal) throws SQLException {
      this.delegate.setTime(parameterIndex, x, cal);
    }

    @Override
    public ResultSet getGeneratedKeys() throws SQLException {
      return this.delegate.getGeneratedKeys();
    }

    @Override
    public void setTimestamp(int parameterIndex, Timestamp x, Calendar cal) throws SQLException {
      this.delegate.setTimestamp(parameterIndex, x, cal);
    }

    @Override
    public int executeUpdate(String sql, int autoGeneratedKeys) throws SQLException {
      return this.delegate.executeUpdate(sql, autoGeneratedKeys);
    }

    @Override
    public void setNull(int parameterIndex, int sqlType, String typeName) throws SQLException {
      this.delegate.setNull(parameterIndex, sqlType, typeName);
    }

    @Override
    public int executeUpdate(String sql, int[] columnIndexes) throws SQLException {
      return this.delegate.executeUpdate(sql, columnIndexes);
    }

    @Override
    public void setURL(int parameterIndex, URL x) throws SQLException {
      this.delegate.setURL(parameterIndex, x);
    }

    @Override
    public ParameterMetaData getParameterMetaData() throws SQLException {
      return this.delegate.getParameterMetaData();
    }

    @Override
    public void setRowId(int parameterIndex, RowId x) throws SQLException {
      this.delegate.setRowId(parameterIndex, x);
    }

    @Override
    public int executeUpdate(String sql, String[] columnNames) throws SQLException {
      return this.delegate.executeUpdate(sql, columnNames);
    }

    @Override
    public void setNString(int parameterIndex, String value) throws SQLException {
      this.delegate.setNString(parameterIndex, value);
    }

    @Override
    public void setNCharacterStream(int parameterIndex, Reader value, long length) throws SQLException {
      this.delegate.setNCharacterStream(parameterIndex, value, length);
    }

    @Override
    public boolean execute(String sql, int autoGeneratedKeys)
            throws SQLException {
      return this.delegate.execute(sql, autoGeneratedKeys);
    }

    @Override
    public void setNClob(int parameterIndex, NClob value) throws SQLException { this.delegate.setNClob(parameterIndex, value);
    }

    @Override
    public void setClob(int parameterIndex, Reader reader, long length) throws SQLException {
      this.delegate.setClob(parameterIndex, reader, length);
    }

    @Override
    public boolean execute(String sql, int[] columnIndexes) throws SQLException {
      return this.delegate.execute(sql, columnIndexes);
    }

    @Override
    public void setBlob(int parameterIndex, InputStream inputStream, long length) throws SQLException {
      this.delegate.setBlob(parameterIndex, inputStream, length);
    }

    @Override
    public void setNClob(int parameterIndex, Reader reader, long length) throws SQLException {
      this.delegate.setNClob(parameterIndex, reader, length);
    }

    @Override
    public boolean execute(String sql, String[] columnNames) throws SQLException {
      return this.delegate.execute(sql, columnNames);
    }

    @Override
    public void setSQLXML(int parameterIndex, SQLXML xmlObject) throws SQLException {
      this.delegate.setSQLXML(parameterIndex, xmlObject);
    }

    @Override
    public void setObject(int parameterIndex, Object x, int targetSqlType, int scaleOrLength) throws SQLException {
      this.delegate.setObject(parameterIndex, x, targetSqlType, scaleOrLength);
    }

    @Override
    public int getResultSetHoldability() throws SQLException {
      return this.delegate.getResultSetHoldability();
    }

    @Override
    public boolean isClosed() throws SQLException {
      return this.delegate.isClosed();
    }

    @Override
    public void setPoolable(boolean poolable) throws SQLException {
      this.delegate.setPoolable(poolable);
    }

    @Override
    public void setAsciiStream(int parameterIndex, InputStream x, long length) throws SQLException {
      this.delegate.setAsciiStream(parameterIndex, x, length);
    }

    @Override
    public boolean isPoolable() throws SQLException { return this.delegate.isPoolable();
    }

    @Override
    public void closeOnCompletion() throws SQLException {
      this.delegate.closeOnCompletion();
    }

    @Override
    public void setBinaryStream(int parameterIndex, InputStream x, long length) throws SQLException {
      this.delegate.setBinaryStream(parameterIndex, x, length);
    }

    @Override
    public boolean isCloseOnCompletion() throws SQLException {
      return this.delegate.isCloseOnCompletion();
    }

    @Override
    public void setCharacterStream(int parameterIndex, Reader reader, long length) throws SQLException {
      this.delegate.setCharacterStream(parameterIndex, reader, length);
    }

    @Override
    public long getLargeUpdateCount() throws SQLException {
      return this.delegate.getLargeUpdateCount();
    }

    @Override
    public void setLargeMaxRows(long max) throws SQLException {
      this.delegate.setLargeMaxRows(max);
    }

    @Override
    public void setAsciiStream(int parameterIndex, InputStream x) throws SQLException {
      this.delegate.setAsciiStream(parameterIndex, x);
    }

    @Override
    public long getLargeMaxRows() throws SQLException {
      return this.delegate.getLargeMaxRows();
    }

    @Override
    public void setBinaryStream(int parameterIndex, InputStream x) throws SQLException {
      this.delegate.setBinaryStream(parameterIndex, x);
    }

    @Override
    public long[] executeLargeBatch() throws SQLException {
      return this.delegate.executeLargeBatch();
    }

    @Override
    public void setCharacterStream(int parameterIndex, Reader reader)
            throws SQLException {
      this.delegate.setCharacterStream(parameterIndex, reader);
    }

    @Override
    public void setNCharacterStream(int parameterIndex, Reader value) throws SQLException {
      this.delegate.setNCharacterStream(parameterIndex, value);
    }

    @Override
    public long executeLargeUpdate(String sql) throws SQLException {
      return this.delegate.executeLargeUpdate(sql);
    }

    @Override
    public void setClob(int parameterIndex, Reader reader) throws SQLException {
      this.delegate.setClob(parameterIndex, reader);
    }

    @Override
    public long executeLargeUpdate(String sql, int autoGeneratedKeys) throws SQLException {
      return this.delegate.executeLargeUpdate(sql, autoGeneratedKeys);
    }

    @Override
    public void setBlob(int parameterIndex, InputStream inputStream) throws SQLException {
      this.delegate.setBlob(parameterIndex, inputStream);
    }

    @Override
    public void setNClob(int parameterIndex, Reader reader) throws SQLException {
      this.delegate.setNClob(parameterIndex, reader);
    }

    @Override
    public long executeLargeUpdate(String sql, int[] columnIndexes) throws SQLException {
      return this.delegate.executeLargeUpdate(sql, columnIndexes);
    }

    @Override
    public void setObject(int parameterIndex, Object x, SQLType targetSqlType, int scaleOrLength) throws SQLException {
      this.delegate.setObject(parameterIndex, x, targetSqlType, scaleOrLength);
    }

    @Override
    public long executeLargeUpdate(String sql, String[] columnNames) throws SQLException {
      return this.delegate.executeLargeUpdate(sql, columnNames);
    }

    @Override
    public void setObject(int parameterIndex, Object x, SQLType targetSqlType) throws SQLException {
      this.delegate.setObject(parameterIndex, x, targetSqlType);
    }

    @Override
    public long executeLargeUpdate() throws SQLException {
      return this.delegate.executeLargeUpdate();
    }

  }

}
