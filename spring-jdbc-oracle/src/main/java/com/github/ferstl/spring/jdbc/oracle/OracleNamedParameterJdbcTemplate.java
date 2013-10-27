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
    protected PreparedStatementCreator getPreparedStatementCreator(String sql, SqlParameterSource paramSource) {
        List<String> names = findNames(sql);
        List<SqlParameter> parameters = new ArrayList<>(names.size());
        Object[] values = new Object[names.size()];

        List<Integer> collectionIndices = getCollectionIndices(values);
        if (!collectionIndices.isEmpty()) {
            values = flatten(values);
            // TODO rewrite SQL
        }

        int i = 0;
        for (String name : names) {
            SqlParameter parameter = new SqlParameter(paramSource.getSqlType(name), paramSource.getTypeName(name));
            parameters.add(parameter);
            values[i++] = paramSource.getValue(name);
        }
        PreparedStatementCreatorFactory pscf = new PreparedStatementCreatorFactory(sql, parameters);
        return pscf.newPreparedStatementCreator(values);
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

    private List<Integer> getCollectionIndices(Object[] values) {
        List<Integer> indices = null;
        for (int i = 0; i < values.length; ++i) {
            Object value = values[i];
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

    private List<String> findNames(String sql) {
        List<String> names = new ArrayList<>();
        int startIndex = 0;
        while (startIndex >= 0) {
            int index = sql.indexOf(':', startIndex);
            if (index >= 0) {
                int endIndex = sql.length();
                for (int i = index + 1; i < sql.length(); ++i) {
                    char c = sql.charAt(i);
                    if (Character.isWhitespace(c)) {
                        endIndex = i;
                        break;
                    }
                }
                if (endIndex < sql.length()) {
                    names.add(sql.substring(index + 1, endIndex));
                    startIndex = endIndex;
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

}
