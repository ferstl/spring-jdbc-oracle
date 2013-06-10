package oracle.jdbc;

import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * Dummy {@code OraclePreparedStatement} interface which avoids a dependency to OJDBC.
 */
public interface OraclePreparedStatement extends PreparedStatement {

  void setExecuteBatch(int sendBatchSize) throws SQLException;
  int getExecuteBatch();
  int sendBatch() throws SQLException;
}
