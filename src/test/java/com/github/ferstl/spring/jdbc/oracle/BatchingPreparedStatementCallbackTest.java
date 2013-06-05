package com.github.ferstl.spring.jdbc.oracle;

import java.sql.SQLException;

import oracle.jdbc.OraclePreparedStatement;

import org.junit.Before;
import org.junit.Test;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;

import com.github.ferstl.spring.jdbc.oracle.OracleJdbcTemplate.BatchingPreparedStatementCallback;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * JUnit tests for {@link BatchingPreparedStatementCallback}.
 */
public class BatchingPreparedStatementCallbackTest {

  private OraclePreparedStatement ops;

  @Before
  public void before() throws Exception {
    this.ops = OracleJdbcGuru.createOraclePS();
  }

  @Test
  public void completeBatches() throws Exception {
    testDoInPreparedStatement(3, 6);
  }

  @Test
  public void incompleteBatch() throws Exception {
    testDoInPreparedStatement(5, 2);
  }

  @Test
  public void completeAndIncompleteBatches() throws Exception {
    testDoInPreparedStatement(3, 8);
  }

  @Test
  public void emptyPreparedStatementSetter() throws Exception {
    testDoInPreparedStatement(3, 0);
  }

  private void testDoInPreparedStatement(int sendBatchSize, int pssBatchSize) throws SQLException {
    BatchPreparedStatementSetter pss = mock(BatchPreparedStatementSetter.class);
    when(pss.getBatchSize()).thenReturn(pssBatchSize);

    BatchingPreparedStatementCallback psc = new BatchingPreparedStatementCallback(sendBatchSize, pss);

    int[] result = psc.doInPreparedStatement(this.ops);

    verifyRowCounts(result, sendBatchSize, pssBatchSize);
    verifyPreparedStatementCalls(pssBatchSize, pss);
  }

  private void verifyRowCounts(int[] result, int sendBatchSize, int pssBatchSize) {
    // Check the number of row counts
    assertEquals("Wrong number of executed statements", pssBatchSize, result.length);

    // Check for correct execution of complete batches
    int sizeOfCompleteBatches = (pssBatchSize / sendBatchSize) * sendBatchSize;
    for (int i = 1; i <= sizeOfCompleteBatches; i++) {
      if (i % sendBatchSize != 0) {
        assertEquals("An unexpected update occurred in a complete batch. Position: " + (i - 1), 0, result[i - 1]);
      } else {
        assertEquals("Wrong row count at the end of a complete batch", sendBatchSize, result[i - 1]);
      }
    }

    // Check for correct execution of a possibly last incomplete batch.
    int sizeOfLastBatch = pssBatchSize % sendBatchSize;
    if (sizeOfLastBatch != 0) {
      for (int i = sizeOfCompleteBatches + 1; i < sizeOfCompleteBatches + sizeOfLastBatch; i++) {
        if (i % sendBatchSize != 0) {
          assertEquals("An unexpected update occurred in the last batch. Position: " + (i - 1), 0, result[i - 1]);
        }
      }

      assertEquals("Wrong row count at the end of the last batch.", sizeOfLastBatch, result[result.length -1]);
    }
  }

  private void verifyPreparedStatementCalls(int pssBatchSize, BatchPreparedStatementSetter pss) throws SQLException {
    for (int i = 0; i < pssBatchSize; i++) {
      verify(pss).setValues(this.ops, i);
      verify(this.ops, times(pssBatchSize)).executeUpdate();
    }
  }

}
