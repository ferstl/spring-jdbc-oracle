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
package oracle.jdbc;

import java.sql.Array;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Wrapper;

import oracle.sql.TIMESTAMPTZ;

/**
 * Dummy {@code OraclePreparedStatement} interface which avoids a dependency to OJDBC.
 */
public interface OraclePreparedStatement extends AutoCloseable, OracleStatement, PreparedStatement, Statement, Wrapper {

  // batch support
  void setExecuteBatch(int sendBatchSize) throws SQLException;
  int getExecuteBatch();
  int sendBatch() throws SQLException;

  // named support
  void setObjectAtName(String parameterName, Object value) throws SQLException;
  void setObjectAtName(String parameterName, Object value, int targetSqlType) throws SQLException;
  void setObjectAtName(String parameterName, Object value, int targetSqlType, int scale) throws SQLException;
  void setNullAtName(String parameterName, int sqlType) throws SQLException;
  void setNullAtName(String parameterName, int sqlType, String sqlName) throws SQLException;
  void setArrayAtName(String parameterName, Array value) throws SQLException;

  // timestamptz support
  void setTIMESTAMPTZ(int parameterIndex, TIMESTAMPTZ x) throws SQLException;
}
