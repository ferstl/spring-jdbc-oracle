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
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
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
        if (paramSource instanceof MapSqlParameterSource) {
            MapSqlParameterSource mapSource = (MapSqlParameterSource) paramSource;
            List<String> nameIndices = buildNameIndices(sql, mapSource.getValues().keySet());
        } else if (paramSource instanceof BeanPropertySqlParameterSource) {
            BeanPropertySqlParameterSource beanSource = (BeanPropertySqlParameterSource) paramSource;
            List<String> nameIndices = buildNameIndices(sql, Arrays.asList(beanSource.getReadablePropertyNames()));
        }
    }

    private List<String> buildNameIndices(String sql, Collection<String> names) {
        Map<String, List<Integer>> startIndices = new HashMap<>(names.size());
        for (String name : names) {
            startIndices.put(name, startIndices(sql, name));
        }
    }

    private List<Integer> startIndices(String sql, String name) {
        String key = ':' + name; // REVIEW could be avoided
        List<Integer> indices = null;
        int startIndex = 0;
        while (startIndex >= 0) {
            int index = sql.indexOf(sql, startIndex);
            if (index >= 0) {
                boolean found;
                if (name.length() == index + key.length()) {
                    // SQL ends with :name
                    found = true;
                } else {
                    // check if we found :name or :name2
                    char next = sql.charAt(index + key.length() + 1);
                    found = Character.isWhitespace(next);
                }
                if (found) {
                    if (indices == null) {
                        // optimize for the case where a single named parameter occurs only once
                        indices = Collections.singletonList(index);
                    } else if (indices.size() == 1) {
                        List<Integer> newIndices = new ArrayList<>(2);
                        newIndices.add(indices.get(0));
                        newIndices.add(index);
                        indices = newIndices;
                    } else {
                        indices.add(index);
                    }
                }
            } else {
                break;
            }
        }
        if (indices != null) {
            return indices;
        } else {
            return Collections.emptyList();
        }
    }

}
