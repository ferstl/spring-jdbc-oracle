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

import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.jdbc.core.support.AbstractSqlTypeValue;

import java.sql.Array;
import java.sql.Connection;
import java.sql.SQLException;

import oracle.jdbc.OracleConnection;
import org.springframework.jdbc.object.StoredProcedure;

/**
 * Implementation of the SqlTypeValue interface, for convenient
 * creation of provided scalar values as an Oracle {@link Array}.
 *
 * <p>A usage example from a {@link StoredProcedure}:
 *
 * <pre><code>storedProcedure.declareParameter(new SqlParameter("myarrayparameter", Types.ARRAY, "MYARRAYTYPE"));
 * ...
 *
 * Map&lt;String, Object&gt; inParams = new HashMap&lt;&gt;();
 * inParams.put("myarrayparameter", new OracleArraySqlTypeValue&lt;Number&gt;(objectArray);
 * Map&lt;String, Object&gt; out = storedProcedure.execute(inParams);
 * </code></pre>
 * 
 * <p>Similar to org.springframework.data.jdbc.support.oracle.SqlArrayValue
 * but updated for Spring 5 and later and OJDBC 11.2g and later.
 * 
 * @see <a href="https://docs.oracle.com/en/database/oracle/oracle-database/12.2/jajdb/oracle/jdbc/OracleConnection.html#createOracleArray-java.lang.String-java.lang.Object-">OracleConnection#createOracleArray</a>
 */
public final class OracleArraySqlTypeValue<T> extends AbstractSqlTypeValue {

  private final T[] values;

  private final String defaultTypeName;


  /**
   * Constructor that takes one parameter with the array of values passed in to the
   * statement.
   * @param values the array containing the values
   */
  public OracleArraySqlTypeValue(T[] values) {
    this.values = values;
    this.defaultTypeName = null;
  }

  /**
   * Constructor that takes two parameters, one parameter with the array of values passed in to the
   * statement and one that takes the default type name to be used when the context where this class
   * is used is not aware of the type name to use.
   * @param values the array containing the values
   * @param defaultTypeName the default type name
   */
  public OracleArraySqlTypeValue(T[] values, String defaultTypeName) {
    this.values = values;
    this.defaultTypeName = defaultTypeName;
  }


  protected Object createTypeValue(Connection conn, int sqlType, String typeName)
          throws SQLException {
    String arrayTypeName = typeName != null ? typeName : this.defaultTypeName;
    if (arrayTypeName == null) {
      throw new InvalidDataAccessApiUsageException(
              "The typeName is null in this context. Consider setting the defaultTypeName.");
    }
    return conn.unwrap(OracleConnection.class).createOracleArray(arrayTypeName, this.values);
  }
}