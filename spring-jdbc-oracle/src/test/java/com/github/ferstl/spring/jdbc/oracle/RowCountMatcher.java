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
