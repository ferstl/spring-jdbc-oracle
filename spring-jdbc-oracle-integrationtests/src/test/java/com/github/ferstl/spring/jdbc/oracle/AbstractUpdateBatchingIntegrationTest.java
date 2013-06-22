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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.Test;
import org.springframework.test.context.ActiveProfiles;

import com.github.ferstl.spring.jdbc.oracle.dsconfig.DataSourceProfile;

import static com.github.ferstl.spring.jdbc.oracle.RowCountMatcher.matchesRowCounts;
import static com.github.ferstl.spring.jdbc.oracle.RowCountPerBatchMatcher.matchesBatchedRowCounts;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

@ActiveProfiles(DataSourceProfile.SINGLE_CONNECTION)
//@ActiveProfiles(DataSourceProfile.COMMONS_DBCP)
//@ActiveProfiles(DataSourceProfile.TOMCAT_POOL)
public class AbstractUpdateBatchingIntegrationTest extends AbstractOracleJdbcTemplateIntegrationTest {

  /** SQL that updates one single row. */
  private static final String SINGLE_ROW_SQL = "UPDATE test_table t SET t.numval = ? WHERE t.numval = ?";

  /** SQL that updates multiple rows. */
  private static final String MULTI_ROW_SQL = "UPDATE test_table t SET t.numval = ? WHERE t.numval BETWEEN ? AND ?";

  /** SQL that verifies the result of {@link #SINGLE_ROW_SQL}. */
  private static final String SINGLE_ROW_VERIFY_SQL = "SELECT count(numval) FROM test_table t WHERE t.numval = ?";

  @Test
  public void updateCompleteBatchWithArgList() {
    int nrOfUpdates = 2 * this.batchSize;
    int[] result = this.jdbcTemplate.batchUpdate(SINGLE_ROW_SQL, createBatchArgs(nrOfUpdates));

    assertThat(result, matchesRowCounts(this.batchSize, nrOfUpdates));
    verifyUpdates(nrOfUpdates);
  }

  @Test
  public void updateIncompleteBatchWithArgList() {
    int nrOfUpdates = this.batchSize + 2;
    int[] result = this.jdbcTemplate.batchUpdate(SINGLE_ROW_SQL, createBatchArgs(nrOfUpdates));

    assertThat(result, matchesRowCounts(this.batchSize, nrOfUpdates));
    verifyUpdates(nrOfUpdates);
  }

  @Test
  public void noUpdateWithArgList() {
    int nrOfUpdates = this.batchSize * 2 + 2;
    List<Object[]> batchArgs = new ArrayList<>(nrOfUpdates);
    for (int i = 0; i < nrOfUpdates; i++) {
      batchArgs.add(new Object[] {Integer.MAX_VALUE, Integer.MAX_VALUE});
    }
    int[] result = this.jdbcTemplate.batchUpdate(SINGLE_ROW_SQL, batchArgs);

    assertEquals(nrOfUpdates, result.length);
    for (int updateCount : result) {
      assertEquals(0, updateCount);
    }
    verifyUpdates(0);
  }

  @Test
  public void updateWithEmptyArgList() {
    int[] result = this.jdbcTemplate.batchUpdate(SINGLE_ROW_SQL, Collections.<Object[]>emptyList());

    assertEquals(0, result.length);
    verifyUpdates(0);
  }

  @Test
  public void updateMultipleRowsWithSingleArgList() {
    Object[] args = new Object[] {Integer.MAX_VALUE, 100, 199};
    int[] result = this.jdbcTemplate.batchUpdate(MULTI_ROW_SQL, Collections.singletonList(args));

    assertEquals(1, result.length);
    assertEquals(100, result[0]);
    verifyUpdates(100);
  }

  @Test
  public void updateCompleteBatchWithPss() {
    int nrOfUpdates = this.batchSize * 2;

    int[] result = this.jdbcTemplate.batchUpdate(SINGLE_ROW_SQL, new TestBatchPreparedStatementSetter(nrOfUpdates));

    assertThat(result, matchesRowCounts(this.batchSize, nrOfUpdates));
    verifyUpdates(nrOfUpdates);
  }

  @Test
  public void updateIncompleteBatchWithPss() {
    int nrOfUpdates = this.batchSize + 2;

    int[] result = this.jdbcTemplate.batchUpdate(SINGLE_ROW_SQL, new TestBatchPreparedStatementSetter(nrOfUpdates));

    assertThat(result, matchesRowCounts(this.batchSize, nrOfUpdates));
    verifyUpdates(nrOfUpdates);
  }

  @Test
  public void updateWithEmptyPss() {
    int[] result = this.jdbcTemplate.batchUpdate(SINGLE_ROW_SQL, new TestBatchPreparedStatementSetter(0));

    assertEquals(0, result.length);
    verifyUpdates(0);
  }

  @Test
  public void updateCompleteBatchWithInterruptiblePss() {
    int nrOfUpdates = this.batchSize * 2;

    int[] result = this.jdbcTemplate.batchUpdate(SINGLE_ROW_SQL, new TestInterruptiblePreparedStatementSetter(nrOfUpdates));

    assertThat(result, matchesRowCounts(this.batchSize, nrOfUpdates));
    verifyUpdates(nrOfUpdates);
  }

  @Test
  public void updateIncompleteBatchWithInterruptiblePss() {
    int nrOfUpdates = this.batchSize + 2;

    int[] result = this.jdbcTemplate.batchUpdate(SINGLE_ROW_SQL, new TestInterruptiblePreparedStatementSetter(nrOfUpdates));

    assertThat(result, matchesRowCounts(this.batchSize, nrOfUpdates));
    verifyUpdates(nrOfUpdates);
  }

  @Test
  public void updateWithEmptyInterruptiblePss() {
    int[] result = this.jdbcTemplate.batchUpdate(SINGLE_ROW_SQL, new TestInterruptiblePreparedStatementSetter(0));

    assertEquals(0, result.length);
    verifyUpdates(0);
  }

  @Test
  public void updateCompleteBatchWithParameterizedPss() {
    int customBatchSize = 5;
    int nrOfUpdates = 2 * customBatchSize;
    int[][] result = this.jdbcTemplate.batchUpdate(
        SINGLE_ROW_SQL, createIntBatchArgs(nrOfUpdates), customBatchSize, new TestParameterizedPreparedStatementSetter());

    assertThat(result, matchesBatchedRowCounts(customBatchSize, nrOfUpdates));
    verifyUpdates(nrOfUpdates);
  }

  @Test
  public void updateInompleteBatchWithParameterizedPss() {
    int customBatchSize = 5;
    int nrOfUpdates = customBatchSize + 2;
    int[][] result = this.jdbcTemplate.batchUpdate(
        SINGLE_ROW_SQL, createIntBatchArgs(nrOfUpdates), customBatchSize, new TestParameterizedPreparedStatementSetter());

    assertThat(result, matchesBatchedRowCounts(customBatchSize, nrOfUpdates));
    verifyUpdates(nrOfUpdates);
  }

  @Test
  public void updateInompleteBatchWithParameterizedPssEmptyArgList() {
    int customBatchSize = 5;

    int[][] result = this.jdbcTemplate.batchUpdate(
        SINGLE_ROW_SQL, Collections.<int[]>emptyList(), customBatchSize, new TestParameterizedPreparedStatementSetter());

    assertThat(result, matchesBatchedRowCounts(customBatchSize, 0));
    verifyUpdates(0);
  }

  private void verifyUpdates(int nrOfUpdates) {
    assertEquals((Integer) nrOfUpdates, this.jdbcTemplate.queryForObject(SINGLE_ROW_VERIFY_SQL, Integer.class, Integer.MAX_VALUE));
  }

  private static List<Object[]> createBatchArgs(int nrOfUpdates) {
    List<Object[]> batchArgs = new ArrayList<>(nrOfUpdates);
    for (int i = 0; i < nrOfUpdates; i++) {
      batchArgs.add(new Object[] { Integer.MAX_VALUE, i + 11 });
    }
    return batchArgs;
  }

  private static List<int[]> createIntBatchArgs(int nrOfUpdates) {
    List<int[]> batchArgs = new ArrayList<>(nrOfUpdates);
    for (int i = 0; i < nrOfUpdates; i++) {
      batchArgs.add(new int[] { Integer.MAX_VALUE, i + 11 });
    }
    return batchArgs;
  }
}
