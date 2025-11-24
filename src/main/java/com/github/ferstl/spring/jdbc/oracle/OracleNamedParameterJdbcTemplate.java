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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Collection;
import java.util.Objects;

import javax.sql.DataSource;

import org.jspecify.annotations.Nullable;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.jdbc.core.ParameterDisposer;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.PreparedStatementSetter;
import org.springframework.jdbc.core.SqlProvider;
import org.springframework.jdbc.core.SqlTypeValue;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.ParsedSql;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.jdbc.support.SqlValue;

import oracle.jdbc.OraclePreparedStatement;

/**
 * A subclass of Spring's {@link NamedParameterJdbcTemplate} the uses
 * Oracle named parameter to avoid parsing building a new query.
 * <h3>Limitations</h3>
 * <ul>
 * <li>does not support {@link SqlValue}, instead {@link NamedSqlValue} has to be used</li>
 * <li>does not support collections, instead arrays with a {@link SqlOracleArrayValue}
 * or similar have to be used</li>
 * <li>does not support {@link SqlTypeValue}</li>
 * <li>does not support binding {@link java.util.Calendar}</li>
 * </ul>
 */
public final class OracleNamedParameterJdbcTemplate extends NamedParameterJdbcTemplate {

  /**
   * Create a new NamedParameterJdbcTemplate for the given {@link DataSource}.
   * <p>Creates a classic Spring {@link org.springframework.jdbc.core.JdbcTemplate} and wraps it.
   *
   * @param dataSource the JDBC DataSource to access
   */
  public OracleNamedParameterJdbcTemplate(DataSource dataSource) {
    super(dataSource);
  }

  /**
   * Create a new NamedParameterJdbcTemplate for the given classic
   * Spring {@link org.springframework.jdbc.core.JdbcTemplate}.
   *
   * @param classicJdbcTemplate the classic Spring JdbcTemplate to wrap
   */
  public OracleNamedParameterJdbcTemplate(JdbcOperations classicJdbcTemplate) {
    super(classicJdbcTemplate);
  }

  @Override
  public int update(String sql, SqlParameterSource parameterSource, KeyHolder generatedKeyHolder, @Nullable String[] keyColumnNames) {
    boolean returnGeneratedKeys = keyColumnNames != null;
    return getJdbcOperations().update(new NamedPreparedStatementCreator(sql, parameterSource, returnGeneratedKeys, keyColumnNames), generatedKeyHolder);
  }

  @Override
  public int[] batchUpdate(String sql, SqlParameterSource[] batchArgs) {

    return getJdbcOperations().batchUpdate(sql, new BatchPreparedStatementSetter() {

      @Override
      public void setValues(PreparedStatement ps, int i) throws SQLException {
        SqlParameterSource parameterSource = batchArgs[i];
        NamedPreparedStatementCreator satementSetter = new NamedPreparedStatementCreator(sql, parameterSource);
        satementSetter.setValues(ps);
      }

      @Override
      public int getBatchSize() {
        return batchArgs.length;
      }
    });
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected PreparedStatementCreator getPreparedStatementCreator(String sql, SqlParameterSource parameterSource) {
    return new NamedPreparedStatementCreator(sql, parameterSource);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected ParsedSql getParsedSql(String sql) {
    // the point of this class is to avoid parsing so if somebody tries to parse then we forgot to
    // override a method
    throw new UnsupportedOperationException("parsing SQL is not supported");
  }

  /**
   * Binds named parameters using proprietary Oracle methods.
   */
  static final class NamedPreparedStatementCreator implements PreparedStatementCreator, PreparedStatementSetter, SqlProvider, ParameterDisposer {

    private final String sql;
    private final SqlParameterSource parameterSource;

    private final boolean returnGeneratedKeys;

    @Nullable
    private final String[] generatedKeysColumnNames;

    NamedPreparedStatementCreator(String sql, SqlParameterSource parameterSource) {
      Objects.requireNonNull(sql);
      Objects.requireNonNull(parameterSource);
      this.sql = sql;
      this.parameterSource = parameterSource;
      this.returnGeneratedKeys = false;
      this.generatedKeysColumnNames = null;
    }

    NamedPreparedStatementCreator(String sql, SqlParameterSource parameterSource, boolean returnGeneratedKeys, String[] generatedKeysColumnNames) {
      Objects.requireNonNull(sql);
      Objects.requireNonNull(parameterSource);
      this.sql = sql;
      this.parameterSource = parameterSource;
      this.returnGeneratedKeys = false;
      this.generatedKeysColumnNames = null;
    }

    @Override
    public PreparedStatement createPreparedStatement(Connection connection) throws SQLException {
      PreparedStatement statement;
      if (this.generatedKeysColumnNames != null) {
        statement = connection.prepareStatement(this.sql, this.generatedKeysColumnNames);
      } else if (this.returnGeneratedKeys) {
        statement = connection.prepareStatement(this.sql, PreparedStatement.RETURN_GENERATED_KEYS);
      } else {
        statement = connection.prepareStatement(this.sql);
      }

      this.setValues(statement);
      return statement;
    }

    @Override
    public void setValues(PreparedStatement ps) throws SQLException {
      OraclePreparedStatement statement = ps.unwrap(OraclePreparedStatement.class);

      for (String parameterName : this.parameterSource.getParameterNames()) {
        int sqlType = this.parameterSource.getSqlType(parameterName);
        Object value = this.parameterSource.getValue(parameterName);
        validateValue(value);
        if (value != null) {
          setValue(statement, parameterName, value, sqlType);
        } else {
          String typeName = this.parameterSource.getTypeName(parameterName);
          setNull(statement, parameterName, sqlType, typeName);
        }
      }

    }

    private static void validateValue(Object value) {
      if (value instanceof SqlValue && !(value instanceof NamedSqlValue)) {
        // SqlValue does not support binding by name
        throw new IllegalArgumentException("SqlValue not supported, use NamedSqlValue");
      }
      if (value instanceof SqlTypeValue) {
        // SqlTypeValue does not support binding by name
        throw new IllegalArgumentException("SqlTypeValue not supported, use NamedSqlValue");
      }
      if (value instanceof Collection) {
        // ojdbc does not support binding Collection
        throw new IllegalArgumentException("Collection not supported");
      }
    }

    private static void setValue(OraclePreparedStatement oracleStatement, String parameterName, Object value, int sqlType) throws SQLException {
      if (value instanceof NamedSqlValue) {
        NamedSqlValue sqlValue = (NamedSqlValue) value;
        sqlValue.setValue(oracleStatement, parameterName);
      } else {
        Object bindParameter = convertToBindable(value);
        if (sqlType != SqlParameterSource.TYPE_UNKNOWN) {
          oracleStatement.setObjectAtName(parameterName, bindParameter, sqlType);
        } else {
          oracleStatement.setObjectAtName(parameterName, bindParameter);
        }
      }
    }

    /**
     * OJDBC does not support binding common Java types most notably
     * {@link java.util.Date} this method converts some of the to
     * bindable types.
     *
     * @param object the object to bind with may need conversion
     * @return an equivalent value that hopefully ojdbc support
     * @see org.springframework.jdbc.core.StatementCreatorUtils#setValue(PreparedStatement, int, int, String, Integer, Object)
     */
    private static Object convertToBindable(Object object) {
      if (object instanceof java.util.Date) {
        return convertToSqlTemporal((java.util.Date) object);
      } else {
        return object;
      }
    }

    /**
     * Converts a {@link java.util.Date} that is not a java.sql type
     * to a {@link java.sql.Timestamp}.
     *
     * @param date the date to convert, not null
     * @return The SQL Timestamp.
     * @see org.springframework.jdbc.core.StatementCreatorUtils#isDateValue(Class)
     */
    private static Object convertToSqlTemporal(java.util.Date date) {
      if (date instanceof java.sql.Date) {
        return date;
      } else if (date instanceof java.sql.Timestamp) {
        return date;
      } else if (date instanceof java.sql.Time) {
        return date;
      } else {
        return copyDate(date);
      }
    }

    private static Object copyDate(java.util.Date date) {
      return new java.sql.Timestamp(date.getTime());
    }

    private static void setNull(OraclePreparedStatement oracleStatement, String parameterName, int sqlType, String typeName) throws SQLException {
      if (sqlType != SqlParameterSource.TYPE_UNKNOWN) {
        if (typeName != null) {
          oracleStatement.setNullAtName(parameterName, sqlType, typeName);
        } else {
          oracleStatement.setNullAtName(parameterName, sqlType);
        }
      } else {
        // REVIEW: I'm not actually sure if Types.NULL is the correct type for
        // null but there doesn't seem to be a setNullAtName without a type.
        //
        // We can't call
        // statement.getParameterMetaData().getParameterType(i)
        // because that doesn't take names and
        // OracleParameterMetaData doesn't add any additional methods
        // for named parameters.
        // It might not even be possible to determine the type in an unambiguous
        // way because a name can be used several times in a query for comparisons
        // (or inserts) of columns of varying types
        oracleStatement.setNullAtName(parameterName, Types.NULL);
      }
    }

    @Override
    public String getSql() {
      return this.sql;
    }

    @Override
    public void cleanupParameters() {
      for (String parameterName : this.parameterSource.getParameterNames()) {
        Object value = this.parameterSource.getValue(parameterName);
        if (value instanceof SqlValue) {
          ((SqlValue) value).cleanup();
        }
      }
    }

  }

}
