package com.github.ferstl.spring.jdbc.oracle;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.ParameterizedPreparedStatementSetter;
import org.springframework.jdbc.core.SqlTypeValue;
import org.springframework.jdbc.core.StatementCreatorUtils;
import org.springframework.jdbc.core.support.AbstractInterruptibleBatchPreparedStatementSetter;
import org.springframework.test.context.ActiveProfiles;

import com.github.ferstl.spring.jdbc.oracle.dsconfig.DataSourceProfile;

import static com.github.ferstl.spring.jdbc.oracle.RowCountMatcher.matchesRowCounts;
import static com.github.ferstl.spring.jdbc.oracle.RowCountPerBatchMatcher.matchesBatchedRowCounts;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

@ActiveProfiles(DataSourceProfile.SINGLE_CONNECTION)
//@ActiveProfiles(DataSourceProfile.COMMONS_DBCP)
//@ActiveProfiles(DataSourceProfile.TOMCAT_POOL)
public class AbstractInsertIntegrationTest extends AbstractOracleJdbcTemplateIntegrationTest {

  private static final String INSERT_SQL = "INSERT INTO test_table(id, val, numval) VALUES (seq_test_table.nextval, ?, ?)";

  private static final String VERIFY_INSERT_SQL = "SELECT count(val) FROM test_table t WHERE t.val = ?";
  private int nrOfUpdates;

  @Before
  public void before() {
    this.nrOfUpdates = this.batchSize + 2;
  }

  @Test
  public void insertWithArgList() {
    int[] result = this.jdbcTemplate.batchUpdate(INSERT_SQL, createBatchArgs());

    assertThat(result, matchesRowCounts(this.batchSize, this.nrOfUpdates));
    verifyInserts();
  }

  @Test
  public void insertWithPss() {
    int[] result = this.jdbcTemplate.batchUpdate(INSERT_SQL, new InsertBatchPreparedStatementSetter(this.nrOfUpdates));

    assertThat(result, matchesRowCounts(this.batchSize, this.nrOfUpdates));
    verifyInserts();
  }

  @Test
  public void insertWithInterruptiblePss() {
    int[] result = this.jdbcTemplate.batchUpdate(INSERT_SQL, new InsertInterruptiblePreparedStatementSetter(this.nrOfUpdates));

    assertThat(result, matchesRowCounts(this.batchSize, this.nrOfUpdates));
    verifyInserts();
  }

  @Test
  public void insertWithParameterizedPss() {
    int[][] result =
        this.jdbcTemplate.batchUpdate(INSERT_SQL, createBatchArgs(), this.batchSize, new InsertParameterizedPreparedStatementSetter());

    assertThat(result, matchesBatchedRowCounts(this.batchSize, this.nrOfUpdates));
    verifyInserts();
  }

  private List<Object[]> createBatchArgs() {
    List<Object[]> batchArgs = new ArrayList<>();
    for (int i = 0; i < this.nrOfUpdates; i++) {
      batchArgs.add(new Object[] { "INSERT", Integer.MAX_VALUE - i });
    }
    return batchArgs;
  }

  private void verifyInserts() {
    assertEquals((Integer) this.nrOfUpdates, this.jdbcTemplate.queryForObject(VERIFY_INSERT_SQL, Integer.class, "INSERT"));
  }

  static class InsertBatchPreparedStatementSetter implements BatchPreparedStatementSetter {

    private final int[] values;

    public InsertBatchPreparedStatementSetter(int nrOfRows) {
      this.values = new int[nrOfRows];
      for (int i = 0; i < nrOfRows; i++) {
        this.values[i] = Integer.MAX_VALUE - i;
      }
    }

    @Override
    public void setValues(PreparedStatement ps, int i) throws SQLException {
      StatementCreatorUtils.setParameterValue(ps, 1, SqlTypeValue.TYPE_UNKNOWN, "INSERT");
      StatementCreatorUtils.setParameterValue(ps, 2, SqlTypeValue.TYPE_UNKNOWN, this.values[i]);
    }

    @Override
    public int getBatchSize() {
      return this.values.length;
    }

  }

  static class InsertInterruptiblePreparedStatementSetter extends AbstractInterruptibleBatchPreparedStatementSetter {

    private final int[] values;

    public InsertInterruptiblePreparedStatementSetter(int nrOfRows) {
      this.values = new int[nrOfRows];
      for (int i = 0; i < nrOfRows; i++) {
        this.values[i] = Integer.MAX_VALUE - i;
      }
    }

    @Override
    protected boolean setValuesIfAvailable(PreparedStatement ps, int i) throws SQLException {
      if (i >= this.values.length) {
        return false;
      }

      StatementCreatorUtils.setParameterValue(ps, 1, SqlTypeValue.TYPE_UNKNOWN, "INSERT");
      StatementCreatorUtils.setParameterValue(ps, 2, SqlTypeValue.TYPE_UNKNOWN, this.values[i]);

      return true;
    }

  }

  static class InsertParameterizedPreparedStatementSetter implements ParameterizedPreparedStatementSetter<Object[]> {

    @Override
    public void setValues(PreparedStatement ps, Object[] argument) throws SQLException {
      for (int i = 0; i < argument.length; i++) {
        StatementCreatorUtils.setParameterValue(ps, i + 1, SqlTypeValue.TYPE_UNKNOWN, argument[i]);
      }
    }
  }

  }