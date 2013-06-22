package com.github.ferstl.spring.jdbc.oracle;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.IfProfileValue;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Transactional;

import com.github.ferstl.spring.jdbc.oracle.dsconfig.DataSourceProfile;

import static com.github.ferstl.spring.jdbc.oracle.RowCountMatcher.matchesRowCounts;
import static com.github.ferstl.spring.jdbc.oracle.RowCountPerBatchMatcher.matchesBatchedRowCounts;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

@ActiveProfiles(DataSourceProfile.SINGLE_CONNECTION)
//@ActiveProfiles(DataSourceProfile.COMMONS_DBCP)
//@ActiveProfiles(DataSourceProfile.TOMCAT_POOL)
@ContextConfiguration(classes = DatabaseConfiguration.class)
@TransactionConfiguration
@Transactional
@IfProfileValue(name = "testgroup", value="integration")
@RunWith(SpringJUnit4ClassRunner.class)
public class OracleJdbcTemplateIntegrationTest {

  /** SQL that updates one single row. */
  private static final String SINGLE_ROW_SQL = "UPDATE test_table t SET t.numval = ? WHERE t.numval = ?";

  /** SQL that updates multiple rows. */
  private static final String MULTI_ROW_SQL = "UPDATE test_table t SET t.numval = ? WHERE t.numval BETWEEN ? AND ?";

  @Autowired
  private JdbcTemplate jdbcTemplate;

  @Autowired
  private Environment env;

  private int batchSize;

  @Before
  public void before() {
    this.batchSize = this.env.getProperty("db.batchsize", Integer.class);
  }

  @Test
  public void updateCompleteBatchWithArgList() {
    int nrOfUpdates = 2 * this.batchSize;
    List<Object[]> batchArgs = new ArrayList<>(nrOfUpdates);
    for (int i = 0; i < nrOfUpdates; i++) {
      batchArgs.add(new Object[] { i + 1, i + 11 });
    }

    int[] result = this.jdbcTemplate.batchUpdate(SINGLE_ROW_SQL, batchArgs);

    assertThat(result, matchesRowCounts(this.batchSize, nrOfUpdates));
  }

  @Test
  public void updateIncompleteBatchWithArgList() {
    int nrOfUpdates = this.batchSize + 2;
    List<Object[]> batchArgs = new ArrayList<>(nrOfUpdates);
    for (int i = 0; i < nrOfUpdates; i++) {
      batchArgs.add(new Object[] { i + 1, i + 11 });
    }

    int[] result = this.jdbcTemplate.batchUpdate(SINGLE_ROW_SQL, batchArgs);

    assertThat(result, matchesRowCounts(this.batchSize, nrOfUpdates));
  }

  @Test
  public void noUpdateWithArgList() {
    int nrOfUpdates = this.batchSize * 2 + 2;

    List<Object[]> batchArgs = new ArrayList<>(nrOfUpdates);
    for (int i = 0; i < nrOfUpdates; i++) {
      batchArgs.add(new Object[] { i + 1, Integer.MAX_VALUE });
    }

    int[] result = this.jdbcTemplate.batchUpdate(SINGLE_ROW_SQL, batchArgs);

    assertEquals(nrOfUpdates, result.length);
    for (int updateCount : result) {
      assertEquals(0, updateCount);
    }
  }

  @Test
  public void updateWithEmptyArgList() {
    int[] result = this.jdbcTemplate.batchUpdate(SINGLE_ROW_SQL, Collections.<Object[]>emptyList());

    assertEquals(0, result.length);
  }

  @Test
  public void updateMultipleRowsWithSingleArgList() {
    Object[] args = new Object[] {9999, 100, 199};
    int[] result = this.jdbcTemplate.batchUpdate(MULTI_ROW_SQL, Collections.singletonList(args));

    assertEquals(1, result.length);
    assertEquals(100, result[0]);
  }

  @Test
  public void updateCompleteBatchWithPss() {
    int nrOfUpdates = this.batchSize * 2;

    int[] result = this.jdbcTemplate.batchUpdate(SINGLE_ROW_SQL, new TestBatchPreparedStatementSetter(nrOfUpdates));

    assertThat(result, matchesRowCounts(this.batchSize, nrOfUpdates));
  }

  @Test
  public void updateIncompleteBatchWithPss() {
    int nrOfUpdates = this.batchSize + 2;

    int[] result = this.jdbcTemplate.batchUpdate(SINGLE_ROW_SQL, new TestBatchPreparedStatementSetter(nrOfUpdates));

    assertThat(result, matchesRowCounts(this.batchSize, nrOfUpdates));
  }

  @Test
  public void updateWithEmptyPss() {
    int[] result = this.jdbcTemplate.batchUpdate(SINGLE_ROW_SQL, new TestBatchPreparedStatementSetter(0));

    assertEquals(0, result.length);
  }

  @Test
  public void updateCompleteBatchWithInterruptiblePss() {
    int nrOfUpdates = this.batchSize * 2;

    int[] result = this.jdbcTemplate.batchUpdate(SINGLE_ROW_SQL, new TestInterruptiblePreparedStatementSetter(nrOfUpdates));

    assertThat(result, matchesRowCounts(this.batchSize, nrOfUpdates));
  }

  @Test
  public void updateIncompleteBatchWithInterruptiblePss() {
    int nrOfUpdates = this.batchSize + 2;

    int[] result = this.jdbcTemplate.batchUpdate(SINGLE_ROW_SQL, new TestInterruptiblePreparedStatementSetter(nrOfUpdates));

    assertThat(result, matchesRowCounts(this.batchSize, nrOfUpdates));
  }

  @Test
  public void updateWithEmptyInterruptiblePss() {
    int[] result = this.jdbcTemplate.batchUpdate(SINGLE_ROW_SQL, new TestInterruptiblePreparedStatementSetter(0));

    assertEquals(0, result.length);
  }

  @Test
  public void updateCompleteBatchWithParameterizedPss() {
    int customBatchSize = 5;
    int nrOfUpdates = 2 * customBatchSize;
    List<int[]> batchArgs = new ArrayList<>(nrOfUpdates);
    for (int i = 0; i < nrOfUpdates; i++) {
      batchArgs.add(new int[] { i + 1, i + 11 });
    }

    int[][] result = this.jdbcTemplate.batchUpdate(SINGLE_ROW_SQL, batchArgs, customBatchSize, new TestParameterizedPreparedStatementSetter());

    assertThat(result, matchesBatchedRowCounts(customBatchSize, nrOfUpdates));
  }

  @Test
  public void updateInompleteBatchWithParameterizedPss() {
    int customBatchSize = 5;
    int nrOfUpdates = customBatchSize + 2;
    List<int[]> batchArgs = new ArrayList<>(nrOfUpdates);
    for (int i = 0; i < nrOfUpdates; i++) {
      batchArgs.add(new int[] { i + 1, i + 11 });
    }

    int[][] result = this.jdbcTemplate.batchUpdate(SINGLE_ROW_SQL, batchArgs, customBatchSize, new TestParameterizedPreparedStatementSetter());

    assertThat(result, matchesBatchedRowCounts(customBatchSize, nrOfUpdates));
  }

  @Test
  public void updateInompleteBatchWithParameterizedPssEmptyArgList() {
    int customBatchSize = 5;

    int[][] result = this.jdbcTemplate.batchUpdate(
        SINGLE_ROW_SQL, Collections.<int[]>emptyList(), customBatchSize, new TestParameterizedPreparedStatementSetter());

    assertThat(result, matchesBatchedRowCounts(customBatchSize, 0));
  }
}
