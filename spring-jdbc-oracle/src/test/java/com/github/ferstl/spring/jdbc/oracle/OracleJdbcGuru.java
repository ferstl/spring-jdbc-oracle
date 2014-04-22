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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.mockito.Matchers;

import oracle.jdbc.OraclePreparedStatement;

import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Helper class to create a stubbed {@link OraclePreparedStatement} to be used in tests. The created
 * prepared statement is a Mockito mock of an abstract {@link OraclePreparedStatement} that
 * simulates the relevant batch update logic. This is somewhat debug-unfriendly but requires much
 * less code than implementing all the methods of the prepared statement interface.
 */
public class OracleJdbcGuru {

  public static OraclePreparedStatement createOraclePS() {
    OraclePreparedStatement ops = mock(OraclePreparedStatementStub.class);

    try {
      doCallRealMethod().when(ops).setExecuteBatch(anyInt());
      when(ops.getExecuteBatch()).thenCallRealMethod();
      when(ops.executeUpdate()).thenCallRealMethod();
      when(ops.sendBatch()).thenCallRealMethod();
      when(ops.unwrap(OraclePreparedStatement.class)).thenReturn(ops);
    } catch (SQLException e) {
      throw new RuntimeException("Won't happen here.");
    }

    return ops;
  }

  public static DataSource createDataSource() {
    DataSource ds = mock(DataSource.class);
    Connection connection = mock(Connection.class);
    PreparedStatement ops = createOraclePS();

    try {
      when(connection.prepareStatement(anyString())).thenReturn(ops);
      when(connection.prepareStatement(anyString(), anyInt())).thenReturn(ops);
      when(connection.prepareStatement(anyString(), Matchers.<int[]>any())).thenReturn(ops);
      when(connection.prepareStatement(anyString(), Matchers.<String[]>any())).thenReturn(ops);
      when(connection.prepareStatement(anyString(), anyInt(), anyInt())).thenReturn(ops);
      when(connection.prepareStatement(anyString(), anyInt(), anyInt(), anyInt())).thenReturn(ops);

      when(ds.getConnection()).thenReturn(connection);
    } catch (SQLException e) {
      throw new RuntimeException("Won't happen here.");
    }

    return ds;
  }

  private OracleJdbcGuru() {
    throw new AssertionError("Not instantiable");
  }

  /**
   * Abstract implementation of an {@link OraclePreparedStatement} that simulates the batch update logic.
   */
  static abstract class OraclePreparedStatementStub implements OraclePreparedStatement {
    int sendBatchSize;
    int updateCount;

    @Override
    public void setExecuteBatch(int sendBatchSize) throws SQLException {
      if (this.updateCount != 0) {
        throw new IllegalStateException("Batch is not empty: " + this.updateCount);
      }

      // The real OraclePreparedStatement implementation does the same thing.
      if (sendBatchSize < 1) {
        throw new IllegalArgumentException("Invalid batch value: " + sendBatchSize);
      }
      this.sendBatchSize = sendBatchSize;
    }

    @Override
    public int getExecuteBatch() {
      return this.sendBatchSize;
    }

    @Override
    public int executeUpdate() throws SQLException {
      this.updateCount++;

      if (this.updateCount % this.sendBatchSize == 0) {
        return returnAndResetUpdateCount();
      }

      return 0;
    }

    @Override
    public int sendBatch() throws SQLException {
      // This is more strict than the real implementation but we don't want to call this method if it is not necessary.
      if (this.updateCount == 0) {
        throw new IllegalStateException("Tried to send an empty batch.");
      }

      int ret = returnAndResetUpdateCount();
      return ret;
    }

    private int returnAndResetUpdateCount() {
      int ret = this.updateCount;
      this.updateCount = 0;
      return ret;
    }
  }

}
