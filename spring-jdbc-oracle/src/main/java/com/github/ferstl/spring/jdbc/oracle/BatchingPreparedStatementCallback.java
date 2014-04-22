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

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.InterruptibleBatchPreparedStatementSetter;
import org.springframework.jdbc.core.PreparedStatementCallback;

import oracle.jdbc.OraclePreparedStatement;

/**
 * {@link PreparedStatementCallback} that uses Oracle Update Batching.
 */
class BatchingPreparedStatementCallback implements PreparedStatementCallback<int[]> {

  private final int sendBatchSize;
  private final BatchPreparedStatementSetter pss;

  /**
   * Constructor.
   * @param sendBatchSize Size of the batch that will be sent to the DB.
   * @param pss Prepared statement setter.
   */
  BatchingPreparedStatementCallback(int sendBatchSize, BatchPreparedStatementSetter pss) {
    this.sendBatchSize = sendBatchSize;
    this.pss = pss;
  }

  @Override
  public int[] doInPreparedStatement(PreparedStatement ps) throws SQLException, DataAccessException {
    OraclePreparedStatement ops = ps.unwrap(OraclePreparedStatement.class);
    int batchSize = this.pss.getBatchSize();

    // Don't use an int[] array here because instances of InterruptibleBatchPreparedStatementSetter
    // might return Integer.MAX_VALUE as batch size.
    List<Integer> rowCounts = new ArrayList<>();

    if (this.pss instanceof InterruptibleBatchPreparedStatementSetter) {
      InterruptibleBatchPreparedStatementSetter ipss = (InterruptibleBatchPreparedStatementSetter) this.pss;
      executeUpdate(ops, ipss, rowCounts);
    } else {
      int sizeOfCompleteBatches = (batchSize / this.sendBatchSize) * this.sendBatchSize;
      int sizeOfLastBatch = batchSize % this.sendBatchSize;
      executeUpdate(ops, rowCounts, 0, sizeOfCompleteBatches);
      executeUpdate(ops, rowCounts, sizeOfCompleteBatches, sizeOfCompleteBatches + sizeOfLastBatch);
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
      ipss.setValues(ops, i);
      if (ipss.isBatchExhausted(i)) {
        break;
      }
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
