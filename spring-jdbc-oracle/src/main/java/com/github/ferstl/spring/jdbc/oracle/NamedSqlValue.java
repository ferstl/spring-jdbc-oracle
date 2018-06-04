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
package com.github.ferstl.spring.jdbc.oracle;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.springframework.jdbc.support.SqlValue;

/**
 * Adds named support to {@link SqlValue}.
 *
 * @see org.springframework.jdbc.support.SqlValue
 */
public interface NamedSqlValue extends SqlValue {

  /**
   * Set the value on the given PreparedStatement.
   *
   * @param ps the PreparedStatement to work on
   * @param paramName the name of the parameter for which we need to set the value
   * @throws SQLException if a SQLException is encountered while setting parameter values
   * @see org.springframework.jdbc.support.SqlValue#setValue(PreparedStatement, int)
   */
  void setValue(PreparedStatement ps, String paramName) throws SQLException;

}