package com.github.ferstl.spring.jdbc.oracle;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;


public class RowCountPerBatchMatcher extends TypeSafeMatcher<int[][]> {

  private final int[][] expectedRowCounts;

  public static Matcher<int[][]> matchesBatchedRowCounts(int batchSize, int expectedNrOfUpdates) {
    return new RowCountPerBatchMatcher(batchSize, expectedNrOfUpdates);
  }

  private RowCountPerBatchMatcher(int batchSize, int expectedNrOfUpdates) {
    int numberOfBatches = expectedNrOfUpdates / batchSize;
    int sizeOfLastBatch = expectedNrOfUpdates % batchSize;
    if (expectedNrOfUpdates != 0) {
      if (sizeOfLastBatch == 0) {
        sizeOfLastBatch = batchSize;
      } else {
        numberOfBatches += 1;
      }
    }

    this.expectedRowCounts = new int[numberOfBatches][];

    // Complete batches
    for (int i = 0; i < numberOfBatches - 1; i++) {
      int[] rowCountsInBatch = new int[batchSize];
      rowCountsInBatch[batchSize - 1] = batchSize;
      this.expectedRowCounts[i] = rowCountsInBatch;
    }

    // Last possibly incomplete batch
    if (sizeOfLastBatch != 0) {
      int[] rowCountsLastBatch = new int[sizeOfLastBatch];
      rowCountsLastBatch[sizeOfLastBatch - 1] = sizeOfLastBatch;
      this.expectedRowCounts[this.expectedRowCounts.length - 1] = rowCountsLastBatch;
    }
  }

  @Override
  public void describeTo(Description description) {
    description.appendText("Row counts ").appendValue(this.expectedRowCounts);
  }

  @Override
  protected boolean matchesSafely(int[][] result) {
    if (result.length != this.expectedRowCounts.length) {
      return false;
    }

    for (int i = 0; i < result.length; i++) {
      if (result[i].length != this.expectedRowCounts[i].length) {
        return false;
      }

      for (int j = 0; j < result[i].length; j++) {
        if (result[i][j] != this.expectedRowCounts[i][j]) {
          return false;
        }
      }
    }

    return true;
  }

}
