package com.github.ferstl.spring.jdbc.oracle;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.IfProfileValue;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Transactional;

import com.github.ferstl.spring.jdbc.oracle.dsconfig.DataSourceProfile;

import static com.github.ferstl.spring.jdbc.oracle.RowCountMatcher.matchesRowCounts;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

@ActiveProfiles(DataSourceProfile.SINGLE_CONNECTION)
//@ActiveProfiles(DataSourceProfile.COMMONS_DBCP)
//@ActiveProfiles(DataSourceProfile.TOMCAT_POOL)
@ContextConfiguration(classes = DatabaseConfiguration.class)
@TransactionConfiguration
@Transactional
@IfProfileValue(name = "testgroup", value="integration")
@RunWith(SpringJUnit4ClassRunner.class)
public class OracleJdbcTemplateIntegrationTest {

  private static final String SINGLE_ROW_SQL = "UPDATE test_table t SET t.numval = ? WHERE t.numval = ?";
  private static final String MULTI_ROW_SQL = "UPDATE test_table t SET t.numval = ? WHERE t.numval BETWEEN ? AND ?";

  @Autowired
  private JdbcTemplate jdbcTemplate;

  @Autowired
  private Environment env;

  private int batchSize;

  @Before
  public void before() {
    this.batchSize = this.env.getProperty("db.batchsize", Integer.class);
  }

  @Test
  public void test() {
    int nrOf = this.jdbcTemplate.queryForObject("SELECT count(val) FROM test_table", Integer.class);
    assertEquals(10000, nrOf);
  }

  @Test
  public void updateCompleteBatchWithArgList() {
    int nrOfUpdates = 2 * this.batchSize;
    List<Object[]> batchArgs = new ArrayList<>(nrOfUpdates);
    for (int i = 0; i < nrOfUpdates; i++) {
      batchArgs.add(new Object[] { i + 1, i + 11 });
    }

    int[] result = this.jdbcTemplate.batchUpdate(SINGLE_ROW_SQL, batchArgs);

    assertThat(result, matchesRowCounts(this.batchSize, nrOfUpdates));
  }

  @Test
  public void updateIncompleteBatchWithArgList() {
    int nrOfUpdates = this.batchSize + 2;
    List<Object[]> batchArgs = new ArrayList<>(nrOfUpdates);
    for (int i = 0; i < nrOfUpdates; i++) {
      batchArgs.add(new Object[] { i + 1, i + 11 });
    }

    int[] result = this.jdbcTemplate.batchUpdate(SINGLE_ROW_SQL, batchArgs);

    assertThat(result, matchesRowCounts(this.batchSize, nrOfUpdates));
  }

  @Test
  public void updateMultipleRowsWithArgList() {
    Object[] args = new Object[] {9999, 100, 199};
    int[] result = this.jdbcTemplate.batchUpdate(MULTI_ROW_SQL, Collections.singletonList(args));

    assertEquals(1, result.length);
    assertEquals(100, result[0]);
  }
}
