package com.github.ferstl.spring.jdbc.oracle;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;

@Ignore
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = DatabaseConfiguration.class)
@TransactionConfiguration(defaultRollback = true)
public class OJdbcTest {

  @Autowired
  private JdbcTemplate jdbcTemplate;

  @Autowired
  private NamedParameterJdbcTemplate npJdbcTemplate;

  @Before
  public void before() {
    this.jdbcTemplate.execute("TRUNCATE TABLE test");
    insertData();
  }

  @Test
  public void test() {
    print();
  }

  @Test
  public void testBatchUpdate() {
    String sql = "UPDATE test SET c1 = 'foo' WHERE c1 IN (:val)";
    Map<String, ?> map = Collections.singletonMap("val", Arrays.asList("String_0000", "bar"));
    SqlParameterSource paramSource = new MapSqlParameterSource(map);
//    this.npJdbcTemplate.update(sql, paramSource);
//    UpdateHelperOracle.executeBatchUpdateOracle(sql, Collections.singletonList(paramSource), this.npJdbcTemplate, 30);
    this.npJdbcTemplate.batchUpdate(sql, new SqlParameterSource[] { paramSource });
    print();
  }

  @Test
  public void testBatchUpdate2() {
    String sql = "UPDATE test SET c1 = 'foo' WHERE c1 = (:val)";
    int batchSize = 100;
    SqlParameterSource[] params = new SqlParameterSource[batchSize];
    for (int i = 0; i < batchSize; i++) {
      params[i] = new MapSqlParameterSource("val", formatString(i));
    }
    int[] counters = this.npJdbcTemplate.batchUpdate(sql, params);
    for (int counter : counters) {
      System.out.println(counter);
    }
//    print();
  }

  @Test
  public void batchUpdateWithSqlArray() {
    String[] sqls = new String[] {
        "UPDATE test SET c1 = 'foo' WHERE c1 = 'String_0000'",
        "UPDATE test SET c1 = 'foo' WHERE c1 LIKE 'String_%1'"
    };

    int[] result = this.jdbcTemplate.batchUpdate(sqls);
    for (int i : result) {
      System.out.println(i);
    }
  }


  // Initialize DB (use with @TransactionConfiguration(defaultRollback = false))
  private void insertData() {
    for (int i = 0; i < 5000; i++) {
      this.jdbcTemplate.execute("INSERT INTO test VALUES('" + formatString(i) + "')");
    }

  }


  private void print() {
    List<String> result = this.jdbcTemplate.queryForList("SELECT c1 FROM test ORDER BY c1", String.class);

    for (String string : result) {
      System.out.println(string);
    }

  }

  private static String formatString(int i) {
    return String.format("String_%04d", i);
  }

}
