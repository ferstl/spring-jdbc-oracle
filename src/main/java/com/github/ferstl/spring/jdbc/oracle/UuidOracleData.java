package com.github.ferstl.spring.jdbc.oracle;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Objects;
import java.util.UUID;

import oracle.jdbc.OracleData;

/**
 * A wrapper around a {@link UUID} that implements {@link OracleData} for
 * reading to and writing from a {@code RAW(16)}.
 *
 * <h2>Usage</h2>
 *
 * <pre><code> jdbcTemplate.update("INSERT INTO table_name(uuid_row) VALUES(?)", new UuidOracleData(uuid));
 * </code></pre>
 *
 * <p>
 * Also works with named parameters using {@link OracleNamedParameterJdbcTemplate}.
 */
public final class UuidOracleData implements OracleData {

  private final UUID uuid;

  /**
   * Constructs a {@link UuidOracleData}.
   *
   * @param uuid the underlying {@link UUID}, can be {@code null}
   */
  public UuidOracleData(UUID uuid) {
    this.uuid = uuid;
  }

  @Override
  public Object toJDBCObject(Connection connection) throws SQLException {
    return UuidUtils.toByteArray(this.uuid);
  }

  /**
   * Converts a {@code RAW(16)} 128 bit UUID value to a {@link UuidOracleData}.
   *
   * <p>
   * Has to be implemented according to the
   * <a href="https://docs.oracle.com/en/database/oracle/oracle-database/19/jajdb/oracle/jdbc/OracleData.html">OracleData</a>
   * class Javadoc.
   *
   * @param raw the {@code RAW(16)} 128 bit UUID value
   * @return the {@link UuidOracleData}, never {@code null}
   * @throws SQLException if conversion to a {@link UuidOracleData} fails
   */
  public static UuidOracleData valueOf(byte[] raw) throws SQLException {
    UUID uuid;
    try {
      uuid = UuidUtils.fromByteArray(raw);
    } catch (IllegalArgumentException e) {
      throw new SQLException(e);
    }
    return new UuidOracleData(uuid);
  }

  /**
   * Returns the underlying {@link UUID}.
   *
   * @return the underlying {@link UUID}, can be {@code null}
   */
  public UUID getUuid() {
    return this.uuid;
  }

  @Override
  public String toString() {
    return Objects.toString(this.uuid);
  }

  // #equals(Object) and #hashCode() intentionally not implemented because of unclear contract

}
