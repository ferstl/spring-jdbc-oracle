/*
 * Copyright (c) 2021 by Philippe Marschall <philippe.marschall@gmail.com>
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

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.Connection;
import java.sql.SQLException;

import org.junit.jupiter.api.Test;

public class RollbackSingleConnectionDataSourceTest {

  @Test
  @SuppressWarnings("deprecation")
  public void rollbackBeforeClose() throws SQLException {
    Connection targetConnection = mock(Connection.class);
    when(targetConnection.getAutoCommit()).thenReturn(false);
    when(targetConnection.isClosed()).thenReturn(false);

    RollbackSingleConnectionDataSource dataSource = new RollbackSingleConnectionDataSource(targetConnection, true);
    dataSource.setAutoCommit(false);

    try (Connection connection = dataSource.getConnection()) {
      assertNotNull(connection);
    }

    verify(targetConnection, never()).close();

    dataSource.destroy();

    verify(targetConnection, times(1)).rollback();
    verify(targetConnection, times(1)).close();
  }

}
