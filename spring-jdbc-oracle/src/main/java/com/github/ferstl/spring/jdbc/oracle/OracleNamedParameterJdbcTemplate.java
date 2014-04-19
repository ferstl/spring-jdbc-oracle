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
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import oracle.jdbc.OraclePreparedStatement;

import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSourceUtils;

/**
 * A subclass of Spring's {@link NamedParameterJdbcTemplate} the uses
 * Oracle named parameter support to avoid building a new query.
 * <h3>Limitations</h3>
 * <ul>
 * <li>
 * currently only works with
 * {@link org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource}
 * and
 * {@link org.springframework.jdbc.core.namedparam.MapSqlParameterSource}
 * but thats a limitation in {@link SqlParameterSourceUtils}
 * </li>
 * <li>does not support collections</li>
 * <ul>
 */
public final class OracleNamedParameterJdbcTemplate extends NamedParameterJdbcTemplate {

  /**
   * Create a new OracleNamedParameterJdbcTemplate for the given classic
   * Spring {@link org.springframework.jdbc.core.JdbcTemplate}.
   * @param classicJdbcTemplate the classic Spring JdbcTemplate to wrap
   */
  public OracleNamedParameterJdbcTemplate(JdbcOperations classicJdbcTemplate) {
    super(classicJdbcTemplate);
  }

  @Override
  protected PreparedStatementCreator getPreparedStatementCreator(String sql, SqlParameterSource parameterSource) {
    return new NamedPreparedStatementCreator(sql, parameterSource);
  }
  
  static final class NamedPreparedStatementCreator implements PreparedStatementCreator {
    
    private final String sql;
    private final SqlParameterSource parameterSource;
    
    NamedPreparedStatementCreator(String sql, SqlParameterSource parameterSource) {
      this.sql = sql;
      this.parameterSource = parameterSource;
    }

    @Override
    public PreparedStatement createPreparedStatement(Connection connection) throws SQLException {
      PreparedStatement wrapped = connection.prepareStatement(sql);
      OraclePreparedStatement statement = wrapped.unwrap(OraclePreparedStatement.class);
      
      for (String parameterName : this.getParameterNames()) {
        int sqlType = this.parameterSource.getSqlType(parameterName);
        Object value = this.parameterSource.getValue(parameterName);
        if (sqlType != SqlParameterSource.TYPE_UNKNOWN) {
          if (value != null) {
            statement.setObjectAtName(parameterName, value, sqlType);
          } else {
            statement.setNullAtName(parameterName, sqlType);
          }
        } else {
          if (value != null) {
            statement.setObjectAtName(parameterName, value);
          } else {
            // REVIEW: not actually sure but there doesn't seem to be a
            // setNullAtName without a type
            // we can't do
            // statement.getParameterMetaData().getParameterType(i)
            // because that doesn't take names and
            // OracleParameterMetaData doesn't add any additional methods
            // it might not even be doable in an unambiguous way because
            // a name can be used several times in a query for comparisons
            // (or inserts) of columns of varying types
            statement.setNullAtName(parameterName, Types.NULL);
          }
        }
      }
      return wrapped;
    }
    
    private Collection<String> getParameterNames() {
      Map<?, ?> insensitiveParameterNames = SqlParameterSourceUtils.extractCaseInsensitiveParameterNames(parameterSource);
      Set<String> parameterNames = new HashSet<>(insensitiveParameterNames.size());
      for (Object each : insensitiveParameterNames.values()) {
        parameterNames.add((String) each);
      }
      return parameterNames;
    }
    
  }



}
