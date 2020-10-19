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

import static com.github.ferstl.spring.jdbc.oracle.RowCountMatcher.matchesRowCounts;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;

import oracle.jdbc.OracleResultSet;

/**
 * Integration test that uses {@link OracleNamedParameterJdbcTemplate}.
 */
public abstract class AbstractOracleNamedParameterIntegrationTest extends AbstractOracleJdbcTemplateIntegrationTest {

  private static final String DELETE_SQL = "DELETE FROM test_table t WHERE t.numval = :value";

  private static final UuidOracleDataFactory UUID_ORACLE_DATA_FACTORY = new UuidOracleDataFactory();


  @Autowired
  private OracleNamedParameterJdbcTemplate onpJdbcTemplate;

  private int nrOfDeletes;

  @BeforeEach
  public void before() {
    this.nrOfDeletes = (this.batchSize * 2) + 2;
  }

  @Test
  public void deleteWithArgMap() {
    int[] result = this.onpJdbcTemplate.batchUpdate(DELETE_SQL, createArgMaps(this.nrOfDeletes));

    assertThat(result, matchesRowCounts(this.nrOfDeletes));
  }

  @Test
  public void deleteWithParamSource() {
    int[] result = this.onpJdbcTemplate.batchUpdate(DELETE_SQL, createParamSources(this.nrOfDeletes));

    assertThat(result, matchesRowCounts(this.nrOfDeletes));
  }

  @Test
  public void inlists() {
    Map<String, Object> map = Collections.singletonMap("ids", new SqlOracleArrayValue("TEST_ARRAY_TYPE", 1, 2, 3));
    List<String> values = this.onpJdbcTemplate.query("SELECT val "
            + "FROM test_table "
            // 18c syntax
            //            + "WHERE id = ANY(:ids)",
            + "WHERE id = ANY(select column_value from table(:ids))",
        new MapSqlParameterSource(map),
        (rs, i) -> rs.getString(1));

    assertEquals(Arrays.asList("Value_00002", "Value_00003", "Value_00004"), values);
  }

  @Test
  public void uuid() {
    UUID expected = UUID.randomUUID();
    Map<String, Object> map = Collections.singletonMap("uuid", new UuidOracleData(expected));
    UUID actual = this.onpJdbcTemplate.queryForObject("SELECT :uuid AS uuid_row"
            + " FROM dual",
            new MapSqlParameterSource(map),
            (rs, i) -> {
              UuidOracleData oracleData = (UuidOracleData) rs.unwrap(OracleResultSet.class).getObject("uuid_row", UUID_ORACLE_DATA_FACTORY);
              return oracleData.getUuid();
            });

    assertEquals(expected, actual);
  }

  private static Map<String, Object>[] createArgMaps(int nrOfRows) {
    @SuppressWarnings("unchecked")
    Map<String, Object>[] args = new Map[nrOfRows];

    for (int i = 0; i < nrOfRows; i++) {
      args[i] = Collections.singletonMap("value", i + 1);
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
