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

import static org.junit.Assert.assertEquals;

import java.util.Collections;
import java.util.List;

import org.junit.Test;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.RowMapper;

/**
 * Integration test that uses {@link CachedPreparedStatementCreator}.
 */
public abstract class AbstractCachedStatementIntegrationTest extends AbstractOracleJdbcTemplateIntegrationTest {

  @Test
  public void executeTwice(){
    String key = "key";
    String sql = "SELECT 1 FROM dual";

    PreparedStatementCreator statementCreator = new CachedPreparedStatementCreator(key, sql);
    RowMapper<Integer> rowMapper = (rs, i) -> rs.getInt(1);

    List<Integer> result = this.jdbcTemplate.query(statementCreator, rowMapper);
    assertEquals(Collections.singletonList(1), result);

    result = this.jdbcTemplate.query(statementCreator, rowMapper);
    assertEquals(Collections.singletonList(1), result);
  }

}
