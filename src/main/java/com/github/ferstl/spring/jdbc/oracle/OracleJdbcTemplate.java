package com.github.ferstl.spring.jdbc.oracle;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.sql.DataSource;

import oracle.jdbc.OraclePreparedStatement;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.InterruptibleBatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ParameterDisposer;
import org.springframework.jdbc.core.ParameterizedPreparedStatementSetter;
import org.springframework.jdbc.core.PreparedStatementCallback;


public class OracleJdbcTemplate extends JdbcTemplate {

  private final int sendBatchSize;

  public OracleJdbcTemplate(int sendBatchSize) {
    validateSendBatchSize(sendBatchSize);
    this.sendBatchSize = sendBatchSize;
  }

  public OracleJdbcTemplate(int sendBatchSize, DataSource dataSource, boolean lazyInit) {
    super(dataSource, lazyInit);
    validateSendBatchSize(sendBatchSize);
    this.sendBatchSize = sendBatchSize;
  }

  public OracleJdbcTemplate(int sendBatchSize, DataSource dataSource) {
    super(dataSource);
    validateSendBatchSize(sendBatchSize);
    this.sendBatchSize = sendBatchSize;
  }

  @Override
  public int[] batchUpdate(String sql, final BatchPreparedStatementSetter pss) throws DataAccessException {
    if (this.logger.isDebugEnabled()) {
      this.logger.debug("Executing SQL batch update [" + sql + "]");
    }

    try {
      return execute(sql, new BatchingPreparedStatementCallback(this.sendBatchSize, pss));
    } finally {
      if (pss instanceof ParameterDisposer) {
        ((ParameterDisposer) pss).cleanupParameters();
      }
    }
  }

  @Override
  public <T> int[][] batchUpdate(
      String sql, Collection<T> batchArgs, int batchSize, ParameterizedPreparedStatementSetter<T> ppss) {

    if (this.logger.isDebugEnabled()) {
      this.logger.debug("Executing SQL batch update [" + sql + "] with a batch size of " + batchSize);
    }

    try {
    return execute(sql, new ParameterizedBatchingPreparedStatementCallback<>(ppss, batchSize, batchArgs));
    } finally {
      if (ppss instanceof ParameterDisposer) {
        ((ParameterDisposer) ppss).cleanupParameters();
      }
    }
  }

  private static void validateSendBatchSize(int sendBatchSize) {
    if (sendBatchSize < 1) {
      throw new IllegalArgumentException("Invalid batch size: " + sendBatchSize + ". Must be greater than 0.");
    }
  }

  static class BatchingPreparedStatementCallback implements PreparedStatementCallback<int[]> {

    private final int sendBatchSize;
    private final BatchPreparedStatementSetter pss;

    BatchingPreparedStatementCallback(int sendBatchSize, BatchPreparedStatementSetter pss) {
      this.sendBatchSize = sendBatchSize;
      this.pss = pss;
    }

    @Override
    public int[] doInPreparedStatement(PreparedStatement ps) throws SQLException, DataAccessException {
      OraclePreparedStatement ops = (OraclePreparedStatement) ps;
      int batchSize = this.pss.getBatchSize();

      // Don't use an int[] array here because instances of InterruptibleBatchPreparedStatementSetter
      // might return Integer.MAX_VALUE as batch size.
      List<Integer> rowCounts = new ArrayList<>();

      if (this.pss instanceof InterruptibleBatchPreparedStatementSetter) {
        InterruptibleBatchPreparedStatementSetter ipss = (InterruptibleBatchPreparedStatementSetter) this.pss;
        executeUpdate(ops, ipss, rowCounts);
      } else {
        int completeBatchSize = (batchSize / this.sendBatchSize) * this.sendBatchSize;
        int remainingBatchSize = batchSize % this.sendBatchSize;
        executeUpdate(ops, rowCounts, 0, completeBatchSize);
        executeUpdate(ops, rowCounts, completeBatchSize, completeBatchSize + remainingBatchSize);
      }

      return toIntArray(rowCounts);
    }

    private void executeUpdate(OraclePreparedStatement ops, List<Integer> rowCounts, int start, int end)
    throws SQLException {

      int batchSize = end - start;

      if (batchSize > 0) {
        int sendBatchSize = this.sendBatchSize < batchSize ? this.sendBatchSize : batchSize;
        ops.setExecuteBatch(sendBatchSize);

        for (int i = start; i < end; i++) {
          this.pss.setValues(ops, i);
          rowCounts.add(ops.executeUpdate());
        }
      }
    }

    private void executeUpdate(OraclePreparedStatement ops, InterruptibleBatchPreparedStatementSetter ipss, List<Integer> rowCounts)
    throws SQLException {

      ops.setExecuteBatch(this.sendBatchSize);
      int i = 0;
      while (i < ipss.getBatchSize()) {
        if (ipss.isBatchExhausted(i)) {
          break;
        }
        ipss.setValues(ops, i);
        rowCounts.add(ops.executeUpdate());
        i++;
      }

      if (i > 0 && i % this.sendBatchSize != 0) {
        rowCounts.set(rowCounts.size() - 1, ops.sendBatch());
      }

    }

    private static int[] toIntArray(List<Integer> intList) {
      int[] array = new int[intList.size()];
      int i = 0;
      for (Integer integer : intList) {
        array[i++] = integer;
      }
      return array;
    }
  }

  static class ParameterizedBatchingPreparedStatementCallback<T> implements PreparedStatementCallback<int[][]> {

    private final ParameterizedPreparedStatementSetter<T> ppss;
    private final int sendBatchSize;
    private final List<T> batchArgs;


    public ParameterizedBatchingPreparedStatementCallback(
        ParameterizedPreparedStatementSetter<T> ppss,
        int sendBatchSize,
        Collection<T> batchArgs) {

      this.ppss = ppss;
      this.sendBatchSize = sendBatchSize < batchArgs.size() ? sendBatchSize : batchArgs.size();
      this.batchArgs = new ArrayList<>(batchArgs);
    }

    @Override
    public int[][] doInPreparedStatement(PreparedStatement ps) throws SQLException, DataAccessException {
      OraclePreparedStatement ops = (OraclePreparedStatement) ps;

      List<int[]> rowCounts = new ArrayList<>();
      for (int i = 0; i < this.batchArgs.size(); i += this.sendBatchSize) {
        int remainder = this.batchArgs.size() - i;
        int batchSize = remainder < this.sendBatchSize ? remainder : this.sendBatchSize;

        int[] rowCountsCurrentBatch = new int[batchSize];
        rowCounts.add(rowCountsCurrentBatch);
        ops.setExecuteBatch(batchSize);

        List<T> batch = this.batchArgs.subList(i, i + batchSize);
        for (int j = 0; j < batchSize; j++) {
          this.ppss.setValues(ops, batch.get(j));
          rowCountsCurrentBatch[j] = ops.executeUpdate();
        }
      }

      return rowCounts.toArray(new int[rowCounts.size()][]);
    }

  }

}
