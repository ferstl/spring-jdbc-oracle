package com.github.ferstl.spring.jdbc.oracle;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.Locale;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import oracle.jdbc.OracleResultSet;

public abstract class AbstractUuidIntegrationTest extends AbstractOracleJdbcTemplateIntegrationTest {

  private static final UuidOracleDataFactory UUID_ORACLE_DATA_FACTORY = new UuidOracleDataFactory();

  @Test
  public void sysGuid() {
    UUID sysGuid = this.jdbcTemplate.queryForObject("SELECT SYS_GUID() FROM dual", (rs, i) -> {
      UuidOracleData oracleData = (UuidOracleData) rs.unwrap(OracleResultSet.class).getObject(1, UUID_ORACLE_DATA_FACTORY);
      String hexString = rs.getString(1); // hex string in all caps without dashes
      UUID uuid = oracleData.getUuid();
      assertEquals(hexString, uuid.toString().replace("-", "").toUpperCase(Locale.US));
      return uuid;
    });
    assertNotNull(sysGuid);
  }

  @Test
  public void uuidBindParameter() {
    UUID uuid = UUID.randomUUID();
    UUID readBack = this.jdbcTemplate.queryForObject("SELECT CAST(? AS RAW(16)) FROM dual", (rs, i) -> {
      UuidOracleData oracleData = (UuidOracleData) rs.unwrap(OracleResultSet.class).getObject(1, UUID_ORACLE_DATA_FACTORY);
      return oracleData.getUuid();
    }, new UuidOracleData(uuid));
    assertNotNull(readBack);
    assertEquals(uuid, readBack);
  }

}
