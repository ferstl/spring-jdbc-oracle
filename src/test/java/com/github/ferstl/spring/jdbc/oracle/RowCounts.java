package com.github.ferstl.spring.jdbc.oracle;

import java.util.Arrays;

final class RowCounts {

  private RowCounts() {
    throw new AssertionError("not instantiable");
  }

  static int[] rowCounts(int expectedNrOfUpdates) {
    int[] rowCounts = new int[expectedNrOfUpdates];
    Arrays.fill(rowCounts, 1);
    return rowCounts;
  }

  static int[][] batchedRowCounts(int batchSize, int expectedNrOfUpdates) {
    int numberOfBatches = expectedNrOfUpdates / batchSize;
    int sizeOfLastBatch = expectedNrOfUpdates % batchSize;
    if (expectedNrOfUpdates != 0) {
      if (sizeOfLastBatch == 0) {
        sizeOfLastBatch = batchSize;
      } else {
        numberOfBatches += 1;
      }
    }

    int[][] expectedRowCounts = new int[numberOfBatches][];

    // Complete batches
    for (int i = 0; i < numberOfBatches - 1; i++) {
      int[] rowCountsInBatch = new int[batchSize];
      Arrays.fill(rowCountsInBatch, 1);
      expectedRowCounts[i] = rowCountsInBatch;
    }

    // Last possibly incomplete batch
    if (sizeOfLastBatch != 0) {
      int[] rowCountsLastBatch = new int[sizeOfLastBatch];
      Arrays.fill(rowCountsLastBatch, 1);
      expectedRowCounts[expectedRowCounts.length - 1] = rowCountsLastBatch;
    }
    return expectedRowCounts;
  }

}
