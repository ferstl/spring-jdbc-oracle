/*
 * Copyright (c) 2013 by Stefan Ferstl <st.ferstl@gmail.com>
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

import java.util.Collections;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import static com.github.ferstl.spring.jdbc.oracle.RowCountMatcher.matchesRowCounts;
import static org.junit.Assert.assertThat;

/**
 * Integration test that uses {@link NamedParameterJdbcTemplate}.
 */
public abstract class AbstractNamedParameterIntegrationTest extends AbstractOracleJdbcTemplateIntegrationTest {

  private static final String DELETE_SQL = "DELETE FROM test_table t WHERE t.numval = :value";

  @Autowired
  private NamedParameterJdbcTemplate npJdbcTemplate;

  private int nrOfDeletes;

  @Before
  public void before() {
    this.nrOfDeletes = this.batchSize * 2 + 2;
  }

  @Test
  public void deleteWithArgMap() {
    int[] result = this.npJdbcTemplate.batchUpdate(DELETE_SQL, createArgMaps(this.nrOfDeletes));

    assertThat(result, matchesRowCounts(this.nrOfDeletes));
  }

  @Test
  public void deleteWithParamSource() {
    int[] result = this.npJdbcTemplate.batchUpdate(DELETE_SQL, createParamSources(this.nrOfDeletes));

    assertThat(result, matchesRowCounts(this.nrOfDeletes));
  }

  private static Map<String, Object>[] createArgMaps(int nrOfRows) {
    @SuppressWarnings("unchecked")
    Map<String, Object>[] args = new Map[nrOfRows];

    for (int i = 0; i < nrOfRows; i++) {
      args[i] = Collections.<String, Object>singletonMap("value", i + 1);
    }

    return args;
  }

  private static SqlParameterSource[] createParamSources(int nrOfRows) {
    Map<String, Object>[] batchArgs = createArgMaps(nrOfRows);

    SqlParameterSource[] sources = new SqlParameterSource[nrOfRows];
    for (int i = 0; i < sources.length; i++) {
      sources[i] = new MapSqlParameterSource(batchArgs[i]);
    }

    return sources;
  }
}
