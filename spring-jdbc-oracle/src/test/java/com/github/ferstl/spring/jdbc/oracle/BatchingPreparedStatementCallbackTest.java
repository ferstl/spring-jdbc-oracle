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

import java.sql.SQLException;
import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.InterruptibleBatchPreparedStatementSetter;
import oracle.jdbc.OraclePreparedStatement;

import static com.github.ferstl.spring.jdbc.oracle.RowCountMatcher.matchesRowCounts;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.anyInt;
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
  public void completeBatchesWithBpss() throws Exception {
    doInPreparedStatementWithBpss(3, 6);
  }

  @Test
  public void incompleteBatchWithBpss() throws Exception {
    doInPreparedStatementWithBpss(5, 2);
  }

  @Test
  public void completeAndIncompleteBatchesWithBpss() throws Exception {
    doInPreparedStatementWithBpss(3, 8);
  }

  @Test
  public void emptyBpss() throws Exception {
    doInPreparedStatementWithBpss(3, 0);
  }

  @Test
  public void completeBatchesWithIbpss() throws SQLException {
    doInPreparedStatementWithIpss(4, 8, Integer.MAX_VALUE);
  }

  @Test
  public void incompleteBatchWithIbpss() throws SQLException {
    doInPreparedStatementWithIpss(4, 1, Integer.MAX_VALUE);
  }

  @Test
  public void completeAndIncompleteBatchesWithIbpss() throws SQLException {
    doInPreparedStatementWithIpss(4, 6, Integer.MAX_VALUE);
  }

  @Test
  public void emptyIbpss() throws SQLException {
    doInPreparedStatementWithIpss(4, 0, Integer.MAX_VALUE);
  }

  @Test
  public void ipssSmallBatchSize() throws SQLException {
    doInPreparedStatementWithIpss(4, 6, 5);
  }

  @Test
  public void ipssZeroBatchSize() throws SQLException {
    doInPreparedStatementWithIpss(4, 6, 0);
  }


  private void doInPreparedStatementWithBpss(int sendBatchSize, int pssBatchSize) throws SQLException {
    BatchPreparedStatementSetter pss = mock(BatchPreparedStatementSetter.class);
    when(pss.getBatchSize()).thenReturn(pssBatchSize);

    BatchingPreparedStatementCallback psc = new BatchingPreparedStatementCallback(sendBatchSize, pss);

    int[] result = psc.doInPreparedStatement(this.ops);

    assertThat(result, matchesRowCounts(sendBatchSize, pssBatchSize));
    verifyPreparedStatementCalls(pssBatchSize, pss);
  }

  private void doInPreparedStatementWithIpss(int sendBatchSize, final int effectiveBatchSize, int pssBatchSize) throws SQLException {

    InterruptibleBatchPreparedStatementSetter ipss = mock(InterruptibleBatchPreparedStatementSetter.class);
    when(ipss.getBatchSize()).thenReturn(pssBatchSize);
    when(ipss.isBatchExhausted(anyInt())).thenAnswer(new Answer<Boolean>() {

      @Override
      public Boolean answer(InvocationOnMock invocation) throws Throwable {
        return effectiveBatchSize <= (int) invocation.getArguments()[0];
      }
    });

    BatchingPreparedStatementCallback psc = new BatchingPreparedStatementCallback(sendBatchSize, ipss);

    int[] result = psc.doInPreparedStatement(this.ops);

    int usedBatchSize = effectiveBatchSize < pssBatchSize ? effectiveBatchSize : pssBatchSize;
    assertThat(result, matchesRowCounts(sendBatchSize, usedBatchSize));
    verifyPreparedStatementCalls(usedBatchSize, ipss);
  }

  private void verifyPreparedStatementCalls(int pssBatchSize, BatchPreparedStatementSetter pss) throws SQLException {
    for (int i = 0; i < pssBatchSize; i++) {
      verify(pss).setValues(this.ops, i);
      verify(this.ops, times(pssBatchSize)).executeUpdate();
    }
  }

}
