package com.github.ferstl.spring.jdbc.oracle;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.IfProfileValue;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;

import com.github.ferstl.spring.jdbc.oracle.dsconfig.DataSourceProfile;

import static org.junit.Assert.assertEquals;

@ActiveProfiles(DataSourceProfile.SINGLE_CONNECTION)
//@ActiveProfiles(DataSourceProfile.COMMONS_DBCP)
//@ActiveProfiles(DataSourceProfile.TOMCAT_POOL)
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = DatabaseConfiguration.class)
@TransactionConfiguration
@IfProfileValue(name = "testgroup", value="integration")
public class OracleJdbcTemplateIntegrationTest {

  @Autowired
  private JdbcTemplate jdbcTemplate;

  @Test
  public void test() {
    int nrOf = this.jdbcTemplate.queryForObject("SELECT count(val) FROM test_table", Integer.class);
    assertEquals(10000, nrOf);
  }
}
