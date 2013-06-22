package com.github.ferstl.spring.jdbc.oracle;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;


public class RowCountMatcher extends TypeSafeMatcher<int[]> {

  private final int[] expectedRowCounts;

  public static Matcher<int[]> matchesRowCounts(int batchSize, int expectedNrOfRows) {
    return new RowCountMatcher(batchSize, expectedNrOfRows);
  }

  private RowCountMatcher(int batchSize, int expectedNrOfUpdates) {
    this.expectedRowCounts = new int[expectedNrOfUpdates];

    for (int i = 0; i < expectedNrOfUpdates; i++) {
      if ((i + 1) % batchSize == 0) {
        this.expectedRowCounts[i] = batchSize;
      }
    }

    int remainingRowCounts = expectedNrOfUpdates % batchSize;
    if (remainingRowCounts != 0) {
      this.expectedRowCounts[this.expectedRowCounts.length - 1] = remainingRowCounts;
    }
  }

  @Override
  public void describeTo(Description description) {
    description.appendText("Row counts ").appendValue(this.expectedRowCounts);
  }

  @Override
  protected boolean matchesSafely(int[] result) {
    if (result.length != this.expectedRowCounts.length) {
      return false;
    }

    for (int i = 0; i < result.length; i++) {
      if (result[i] != this.expectedRowCounts[i]) {
        return false;
      }
    }

    return true;
  }

}
