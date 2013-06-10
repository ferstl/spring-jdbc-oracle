package com.github.ferstl.spring.jdbc.oracle;

import java.sql.SQLException;
import java.util.ArrayList;

import org.junit.Before;
import org.junit.Test;
import org.springframework.jdbc.core.ParameterizedPreparedStatementSetter;

import oracle.jdbc.OraclePreparedStatement;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * JUnit tests for {@link ParameterizedBatchingPreparedStatementCallback}.
 */
public class ParameterizedBatchingPreparedStatementCallbackTest {

  private OraclePreparedStatement ops;

  @Before
  public void before() {
    this.ops = OracleJdbcGuru.createOraclePS();
  }

  @Test
  public void completeBatches() throws SQLException {
    doInPreparedStatement(3, 6);
  }

  @Test
  public void incompleteBatches() throws SQLException {
    doInPreparedStatement(5, 2);
  }

  @Test
  public void completeAndIncompleteBatches() throws SQLException {
    doInPreparedStatement(3, 8);
  }

  @Test
  public void emptyPss() throws SQLException {
    doInPreparedStatement(3, 0);
  }

  private void doInPreparedStatement(int batchSize, int batchArgSize) throws SQLException {
    ParameterizedPreparedStatementSetter<String> ppss = mock(ParameterizedPreparedStatementSetter.class);

    // Create the arguments for the batch update
    ArrayList<String> batchArgs = new ArrayList<>(batchArgSize);
    for (int i = 0; i < batchArgSize; i++) {
      batchArgs.add(Integer.toString(i));
    }

    ParameterizedBatchingPreparedStatementCallback<String> psc =
        new ParameterizedBatchingPreparedStatementCallback<>(ppss, batchSize, batchArgs);
    int[][] result = psc.doInPreparedStatement(this.ops);

    verifyRowCounts(result, batchSize, batchArgSize);
    verifyPreparedStatementCalls(batchArgSize, ppss);
  }

  private void verifyRowCounts(int[][] result, int batchSize, int batchArgSize) {
    // Calculate the expected size of the last batch.
    int sizeOfLastBatch = batchArgSize % batchSize;
    if (sizeOfLastBatch == 0) {
      sizeOfLastBatch = batchSize;
    }

    // Calculate the expected number of batches.
    int numberOfBatches = batchArgSize / batchSize;
    if (sizeOfLastBatch != batchSize) {
      numberOfBatches += 1;
    }

    assertEquals("Wrong number of batches", numberOfBatches, result.length);

    // Verify the complete batches
    for (int i = 0; i < result.length - 1; i++) {
      assertEquals("Wrong size of batch " + i, batchSize, result[i].length);

      verifyRowCountsInBatch(result[i], batchSize);
    }

    // Verify the last batch
    if (result.length > 1) {
      int[] lastBatch = result[result.length - 1];
      assertEquals("Wrong size of the last batch", sizeOfLastBatch, lastBatch.length);

      verifyRowCountsInBatch(lastBatch, sizeOfLastBatch);
    }
  }

  private void verifyRowCountsInBatch(int[] rowCounts, int batchSize) {
    for (int j = 0; j < rowCounts.length; j++) {
      // Only the last number is expected to be non-zero
      if (j != rowCounts.length - 1) {
        assertEquals("An unexpected update occurred in the batch. Position: " + j, 0, rowCounts[j]);
      } else {
        assertEquals("Wrong row count at the end of the batch", batchSize, rowCounts[j]);
      }
    }
  }

  private void verifyPreparedStatementCalls(int batchArgSize, ParameterizedPreparedStatementSetter<String> ppss)
  throws SQLException {

    for (int i = 0; i < batchArgSize; i++) {
      verify(ppss).setValues(this.ops, Integer.toString(i));
      verify(this.ops, times(batchArgSize)).executeUpdate();
    }
  }

}
