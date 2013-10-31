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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.PreparedStatementCreatorFactory;
import org.springframework.jdbc.core.SqlParameter;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;

/**
 * A subclass of Spring's {@link NamedParameterJdbcTemplate} the uses
 * Oracle named parameter support to avoid building a new query.
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
    List<String> parameterNames = findParameterNames(sql);
    List<SqlParameter> parameters = new ArrayList<>(parameterNames.size());
    Object[] values = new Object[parameterNames.size()];

    List<Integer> collectionIndices = getCollectionIndices(parameterNames, parameterSource);
    if (!collectionIndices.isEmpty()) {
      values = flatten(values);
      sql = expandCollectionPlaceholders(sql, collectionIndices, parameterNames, parameterSource);
    }

    int i = 0;
    for (String parameterName : parameterNames) {
      int sqlType = parameterSource.getSqlType(parameterName);
      String typeName = parameterSource.getTypeName(parameterName);
      SqlParameter parameter = new SqlParameter(sqlType, typeName);
      parameters.add(parameter);
      values[i++] = parameterSource.getValue(parameterName);
    }
    PreparedStatementCreatorFactory pscf = new PreparedStatementCreatorFactory(sql, parameters);
    return pscf.newPreparedStatementCreator(values);
  }

  private String expandCollectionPlaceholders(String sql, List<Integer> collectionIndices, List<String> parameterNames, SqlParameterSource parameterSource) {
    StringBuilder buffer = new StringBuilder((int) (sql.length() * 1.1));
    Iterator<Integer> collectionIterator = collectionIndices.iterator();
    int collectionIndex = collectionIterator.next();

    int startIndex = 0;
    int parameterIndex = 0;
    while (startIndex >= 0) {
      int index = sql.indexOf(':', startIndex);
      if (index >= 0) {
        int endIndex = sql.length();
        for (int i = index + 1; i < sql.length(); ++i) {
          char c = sql.charAt(i);
          if (isTerminator(c)) {
            endIndex = i;
            break;
          }
        }

        if (parameterIndex == collectionIndex) {
          buffer.append(sql, startIndex, index);
          String parameterName = parameterNames.get(parameterIndex);
          Collection<?> value = (Collection<?>) parameterSource.getValue(parameterName);
          addPlaceholders(buffer, value.size());
          if (collectionIterator.hasNext()) {
            collectionIndex = collectionIterator.next();
          }

        } else {
          buffer.append(sql, startIndex, endIndex);
        }

        if (endIndex == sql.length()) {
          break;
        } else if (parameterIndex == collectionIndex && !collectionIterator.hasNext()) {
          buffer.append(sql, endIndex, sql.length());
          break;
        } else {
          startIndex = endIndex;
          parameterIndex += 1;
        }
      } else {
        break;
      }
    }

    return buffer.toString();
  }

  private void addPlaceholders(StringBuilder buffer, int count) {
    for (int i = 0; i < count; ++i) {
      if (i > 0) {
        buffer.append(',');
      }
      buffer.append('?');
    }
  }

  private Object[] flatten(Object[] values) {
    int size = 0;
    for (Object value : values) {
      if (value instanceof Collection) {
        size += ((Collection<?>) value).size();
      } else {
        size += 1;
      }
    }
    Object[] flat = new Object[size];
    int i = 0;
    for (Object value : values) {
      if (value instanceof Collection) {
        for (Object inner : (Collection<?>) value) {
          flat[i++] = inner;
        }
      } else {
        flat[i++] = value;
      }
    }

    return flat;
  }

  private List<Integer> getCollectionIndices(List<String> parameterNames, SqlParameterSource parameterSource) {
    List<Integer> indices = null;
    for (int i = 0; i < parameterNames.size(); ++i) {
      String parameterName = parameterNames.get(i);
      Object value = parameterSource.getValue(parameterName);
      if (value instanceof Collection) {
        if (indices == null) {
          indices = new ArrayList<>(2);
        }
        indices.add(i);
      }
    }

    if (indices != null) {
      return indices;
    } else {
      return Collections.emptyList();
    }
  }

  private List<String> findParameterNames(String sql) {
    // FIXME will fail for comments eg
    // SELECT 1 FROM dual WHERE 1 = 1 -- and 2 = :one
    // FIXME will fail for stirng litersl eg
    // SELECT 1 FROM dual WHERE 2 != "2 = :one"
    List<String> names = new ArrayList<>();
    int startIndex = 0;
    while (startIndex >= 0) {
      int index = sql.indexOf(':', startIndex);
      if (index >= 0) {
        int endIndex = sql.length();
        for (int i = index + 1; i < sql.length(); ++i) {
          char c = sql.charAt(i);
          if (isTerminator(c)) {
            endIndex = i;
            break;
          }
        }
        if (endIndex < sql.length()) {
          names.add(sql.substring(index + 1, endIndex));
          startIndex = endIndex + 1;
        } else {
          names.add(sql.substring(index + 1));
          break;
        }
      } else {
        break;
      }
    }
    return names;
  }

  private boolean isTerminator(char c) {
    return c == ')' || Character.isWhitespace(c);
  }

}
