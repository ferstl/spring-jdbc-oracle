/*
 * Copyright (c) 2018 by Philippe Marschall <philippe.marschall@gmail.com>
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
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * Dummy {@code OracleConnection} interface which avoids a dependency to OJDBC.
 */
public interface OracleConnection extends Connection {

  // array support
  Array createOracleArray(String arrayTypeName, Object elements) throws SQLException;

  // statement caching support
  CallableStatement getCallWithKey(String key) throws SQLException;
  PreparedStatement getStatementWithKey(String key) throws SQLException;

  void setExplicitCachingEnabled(boolean cache) throws SQLException;
  boolean getExplicitCachingEnabled() throws SQLException;

  int getStatementCacheSize() throws SQLException;
  void setStatementCacheSize(int size) throws SQLException;

}
