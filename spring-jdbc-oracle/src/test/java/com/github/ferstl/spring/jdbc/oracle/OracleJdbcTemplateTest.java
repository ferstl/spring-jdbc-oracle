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

import java.sql.Types;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * JUnit tests for {@link OracleJdbcTemplate}.
 * <p>
 *   These tests mostly cover the correct invocation of the constructor and the {@code batchUpdate()} methods. The whole
 *   Oracle update batching logic is tested in the unit tests of the used {@code PreparedStatementCallback}s.
 * </p>
 */
public class OracleJdbcTemplateTest {

  private JdbcTemplate jdbcTemplate;

  @Rule
  public ExpectedException expectedException = ExpectedException.none();

  @Before
  public void before() throws Exception {
    this.jdbcTemplate = new OracleJdbcTemplate(5, OracleJdbcGuru.createDataSource());
  }

  @Test
  public void constructorInvalidBatchSize1() {
    this.expectedException.expect(IllegalArgumentException.class);
    new OracleJdbcTemplate(0);
  }

  @Test
  public void constructorInvalidBatchSize2() {
    this.expectedException.expect(IllegalArgumentException.class);
    new OracleJdbcTemplate(-1, OracleJdbcGuru.createDataSource());
  }

  @Test
  public void constructorInvalidBatchSize3() {
    this.expectedException.expect(IllegalArgumentException.class);
    new OracleJdbcTemplate(0, OracleJdbcGuru.createDataSource(), true);
  }

  @Test
  public void withBatchPreparedStatementSetter() {
    TestBatchPreparedStatementSetter pss = new TestBatchPreparedStatementSetter(6);

    int[] rowCounts = this.jdbcTemplate.batchUpdate("doesn't matter", pss);

    assertArrayEquals(rowCounts, new int[] {0, 0, 0, 0, 5, 1});
    assertTrue("Parameters not disposed", pss.isDisposed());
  }

  @Test
  public void withArgList() {
    List<Object[]> batchArgs = Arrays.asList(
        new Object[]{"1"},
        new Object[]{"2"},
        new Object[]{"3"},
        new Object[]{"4"},
        new Object[]{"5"},
        new Object[]{"6"});

    int[] rowCounts = this.jdbcTemplate.batchUpdate("doesn't matter", batchArgs);

    assertArrayEquals(rowCounts, new int[] {0, 0, 0, 0, 5, 1});
  }

  @Test
  public void withArgListAndArgTypes() {
    List<Object[]> batchArgs = Arrays.asList(
        new Object[]{"1"},
        new Object[]{"2"},
        new Object[]{"3"},
        new Object[]{"4"},
        new Object[]{"5"},
        new Object[]{"6"});

    int[] argTypes = new int[] {Types.VARCHAR, Types.VARCHAR, Types.VARCHAR, Types.VARCHAR, Types.VARCHAR, Types.VARCHAR};

    int[] rowCounts = this.jdbcTemplate.batchUpdate("doesn't matter", batchArgs, argTypes);

    assertArrayEquals(rowCounts, new int[] {0, 0, 0, 0, 5, 1});
  }


  @Test
  public void withParameterizedPss() {
    TestParameterizedPreparedStatementSetter pss = new TestParameterizedPreparedStatementSetter();
    List<int[]> argList = new ArrayList<>(6);
    for (int i = 0; i < 6; i++) {
      argList.add(new int[] {i, i});
    }

    int[][] rowCounts = this.jdbcTemplate.batchUpdate("doesn't matter", argList, 4, pss);

    assertEquals("Expected 2 batches", 2, rowCounts.length);
    assertArrayEquals(rowCounts[0], new int[]{0, 0, 0, 4});
    assertArrayEquals(rowCounts[1], new int[] {0, 2});
    assertTrue("Parameters were not disposed", pss.isDisposed());
  }
}
