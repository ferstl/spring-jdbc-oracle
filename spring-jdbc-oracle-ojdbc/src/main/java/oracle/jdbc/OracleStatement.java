/*
 * Copyright (c) 2019 by Philippe Marschall <philippe.marschall@gmail.com>
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

import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Wrapper;

/**
 * Dummy {@code OracleStatement} interface which avoids a dependency to OJDBC.
 */
public interface OracleStatement extends AutoCloseable, Statement, Wrapper {

  // statement caching support
  void closeWithKey(String key) throws SQLException;

}
