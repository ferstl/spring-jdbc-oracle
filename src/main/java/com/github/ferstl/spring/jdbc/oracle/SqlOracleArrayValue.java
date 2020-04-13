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

import java.sql.Array;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Arrays;

import org.springframework.dao.CleanupFailureDataAccessException;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.object.StoredProcedure;
import oracle.jdbc.OracleConnection;
import oracle.jdbc.OraclePreparedStatement;

/**
 * Implementation of the SqlTypeValue interface, for convenient
 * creation of provided scalar values as an Oracle {@link Array}.
 * <p>
 * A usage example from a {@link JdbcTemplate}:
 * <pre>
 * <code>jdbcTemplate.queryForInt(SQL, new SqlOracleArrayValue("MYARRAYTYPE", values));</code>
 * </pre>
 * <p>A usage example from a {@link StoredProcedure}:
 * <pre><code>storedProcedure.declareParameter(new SqlParameter("myarrayparameter", Types.ARRAY, "MYARRAYTYPE"));
 * ...
 * Map&lt;String, Object&gt; inParams = new HashMap&lt;&gt;();
 * inParams.put("myarrayparameter", new SqlOracleArrayValue("MYARRAYTYPE", objectArray);
 * Map&lt;String, Object&gt; out = storedProcedure.execute(inParams);
 * </code></pre>
 * <p>Similar to org.springframework.data.jdbc.support.oracle.SqlArrayValue
 * but updated for Spring 5 and later and OJDBC 11.2g and later.
 *
 * @see <a href="https://docs.oracle.com/en/database/oracle/oracle-database/12.2/jajdb/oracle/jdbc/OracleConnection.html#createOracleArray-java.lang.String-java.lang.Object-">OracleConnection#createOracleArray</a>
 */
public final class SqlOracleArrayValue implements NamedSqlValue {

  private final Object[] values;

  private final String typeName;

  private Array array;

  /**
   * Constructor that takes two parameters, one parameter with the array of values passed in to
   * the statement and one that takes the type name.
   *
   * @param typeName the type name
   * @param values the array containing the values
   */
  public SqlOracleArrayValue(String typeName, Object... values) {
    this.values = values;
    this.typeName = typeName;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void setValue(PreparedStatement ps, int paramIndex) throws SQLException {
    Array array = this.createArray(ps.getConnection());
    ps.setArray(paramIndex, array);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void setValue(PreparedStatement ps, String paramName) throws SQLException {
    Array array = this.createArray(ps.getConnection());
    ps.unwrap(OraclePreparedStatement.class).setArrayAtName(paramName, array);
  }

  private Array createArray(Connection conn) throws SQLException {
    if (this.array != null) {
      throw new InvalidDataAccessApiUsageException("Value bound more than once");
    }
    this.array = conn.unwrap(OracleConnection.class).createOracleArray(this.typeName, this.values);
    return this.array;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void cleanup() {
    if (this.array == null) {
      // #cleanup may be called twice in case of exceptions
      // avoid calling #free twice
      return;
    }
    // https://docs.oracle.com/javase/tutorial/jdbc/basics/array.html#releasing_array
    try {
      this.array.free();
      this.array = null;
    } catch (SQLException e) {
      throw new CleanupFailureDataAccessException("could not free array", e);
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String toString() {
    return Arrays.toString(this.values);
  }

}
