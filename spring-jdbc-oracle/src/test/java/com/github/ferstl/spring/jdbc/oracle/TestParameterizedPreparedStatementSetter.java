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

import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.springframework.jdbc.core.ParameterDisposer;
import org.springframework.jdbc.core.ParameterizedPreparedStatementSetter;
import org.springframework.jdbc.core.SqlTypeValue;
import org.springframework.jdbc.core.StatementCreatorUtils;

public class TestParameterizedPreparedStatementSetter implements ParameterizedPreparedStatementSetter<int[]>, ParameterDisposer {

  private boolean cleanedUp = false;

  @Override
  public void setValues(PreparedStatement ps, int[] argument) throws SQLException {
    for (int i = 0; i < argument.length; i++) {
      StatementCreatorUtils.setParameterValue(ps, i + 1, SqlTypeValue.TYPE_UNKNOWN, argument[i]);
    }
  }

  @Override
  public void cleanupParameters() {
    this.cleanedUp = true;
  }

  public boolean isDisposed() {
    return this.cleanedUp;
  }

}