package com.github.ferstl.spring.jdbc.oracle;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import javax.sql.DataSource;

import oracle.jdbc.OraclePreparedStatement;

import org.mockito.Matchers;

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
public class OraclePreparedStatementGuru {

  public static OraclePreparedStatement createOraclePS() {
    OraclePreparedStatement ops = mock(OraclePreparedStatementStub.class);

    try {
      doCallRealMethod().when(ops).setExecuteBatch(anyInt());
      when(ops.getExecuteBatch()).thenCallRealMethod();
      when(ops.executeUpdate()).thenCallRealMethod();
      when(ops.sendBatch()).thenCallRealMethod();
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

  private OraclePreparedStatementGuru() {
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
