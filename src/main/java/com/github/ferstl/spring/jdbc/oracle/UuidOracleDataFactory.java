package com.github.ferstl.spring.jdbc.oracle;

import java.sql.SQLException;
import java.util.UUID;

import oracle.jdbc.OracleData;
import oracle.jdbc.OracleDataFactory;

/**
 * An {@link OracleDataFactory} that creates {@link UuidOracleData} instances.
 *
 * <h2>Usage</h2>
 *
 * <pre><code> static final UuidOracleDataFactory UUID_ORACLE_DATA_FACTORY = new UuidOracleDataFactory();
 * jdbcTemplate.queryForObject("SELECT uuid_row FROM table_name",
 *          (rs, i) -&gt; {
 *            UuidOracleData oracleData = (UuidOracleData) rs.unwrap(OracleResultSet.class).getObject("uuid_row", UUID_ORACLE_DATA_FACTORY);
 *            return oracleData.getUuid();
 * });
 * </code></pre>
 *
 * <p>
 * Also works with named parameters using {@link OracleNamedParameterJdbcTemplate}.
 */
public final class UuidOracleDataFactory implements OracleDataFactory {

  @Override
  public OracleData create(Object jdbcValue, int sqlType) throws SQLException {
    if (jdbcValue == null) {
      return new UuidOracleData(null);
    }
    if (!(jdbcValue instanceof byte[])) {
      throw new SQLException("unexpected data type: " + jdbcValue.getClass());
    }
    byte[] raw = (byte[]) jdbcValue;
    UUID uuid;
    try {
      uuid = UuidUtils.fromByteArray(raw);
    } catch (IllegalArgumentException e) {
      throw new SQLException(e);
    }
    return new UuidOracleData(uuid);
  }

}
