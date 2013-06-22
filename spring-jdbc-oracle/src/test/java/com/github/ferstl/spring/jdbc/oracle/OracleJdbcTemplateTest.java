package com.github.ferstl.spring.jdbc.oracle;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ParameterDisposer;
import org.springframework.jdbc.core.ParameterizedPreparedStatementSetter;
import org.springframework.jdbc.core.SqlTypeValue;
import org.springframework.jdbc.core.StatementCreatorUtils;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * JUnit tests for {@link OracleJdbcTemplate}.
 * <p>
 *   These tests mostly cover the correct invocation of the constructor and the {@code batchUpdate()} methods. The whole
 *   Oracle update batching logic is tested in the unit tests of the used {@code PreparedStatementCallback}s.
 * </p>
 */
public class OracleJdbcTemplateTest {

  private JdbcTemplate jdbcTemplate;

  @Rule
  public ExpectedException expectedException = ExpectedException.none();

  @Before
  public void before() throws Exception {
    this.jdbcTemplate = new OracleJdbcTemplate(5, OracleJdbcGuru.createDataSource());
  }

  @Test
  public void constructorInvalidBatchSize1() {
    this.expectedException.expect(IllegalArgumentException.class);
    new OracleJdbcTemplate(0);
  }

  @Test
  public void constructorInvalidBatchSize2() {
    this.expectedException.expect(IllegalArgumentException.class);
    new OracleJdbcTemplate(-1, OracleJdbcGuru.createDataSource());
  }

  @Test
  public void constructorInvalidBatchSize3() {
    this.expectedException.expect(IllegalArgumentException.class);
    new OracleJdbcTemplate(0, OracleJdbcGuru.createDataSource(), true);
  }

  @Test
  public void withBatchPreparedStatementSetter() {
    SingleIntBatchPreparedStatementSetter pss = new SingleIntBatchPreparedStatementSetter(6);

    int[] rowCounts = this.jdbcTemplate.batchUpdate("doesn't matter", pss);

    assertArrayEquals(rowCounts, new int[] {0, 0, 0, 0, 5, 1});
    assertTrue("Parameters not disposed", pss.cleanupCalled);
  }

  @Test
  public void withArgList() {
    List<Object[]> batchArgs = Arrays.asList(
        new Object[]{"1"},
        new Object[]{"2"},
        new Object[]{"3"},
        new Object[]{"4"},
        new Object[]{"5"},
        new Object[]{"6"});

    int[] rowCounts = this.jdbcTemplate.batchUpdate("doesn't matter", batchArgs);

    assertArrayEquals(rowCounts, new int[] {0, 0, 0, 0, 5, 1});
  }

  @Test
  public void withArgListAndArgTypes() {
    List<Object[]> batchArgs = Arrays.asList(
        new Object[]{"1"},
        new Object[]{"2"},
        new Object[]{"3"},
        new Object[]{"4"},
        new Object[]{"5"},
        new Object[]{"6"});

    int[] argTypes = new int[] {Types.VARCHAR, Types.VARCHAR, Types.VARCHAR, Types.VARCHAR, Types.VARCHAR, Types.VARCHAR};

    int[] rowCounts = this.jdbcTemplate.batchUpdate("doesn't matter", batchArgs, argTypes);

    assertArrayEquals(rowCounts, new int[] {0, 0, 0, 0, 5, 1});
  }


  @Test
  public void withParameterizedPss() {
    NopParameterizedPreparedStatementSetter pss = new NopParameterizedPreparedStatementSetter();
    int[][] rowCounts = this.jdbcTemplate.batchUpdate(
        "doesn't matter", Arrays.asList("1", "2", "3", "4", "5", "6"), 4, pss);

    assertEquals("Expected 2 batches", 2, rowCounts.length);
    assertArrayEquals(rowCounts[0], new int[]{0, 0, 0, 4});
    assertArrayEquals(rowCounts[1], new int[] {0, 2});
    assertTrue("Parameters were not disposed", pss.cleanupCalled);
  }


  static class SingleIntBatchPreparedStatementSetter implements BatchPreparedStatementSetter, ParameterDisposer {

    private boolean cleanupCalled;
    private final int[] array;

    public SingleIntBatchPreparedStatementSetter(int size) {
      this.cleanupCalled = false;

      this.array = new int[size];
      for (int i = 0; i < size; i++) {
        this.array[i] = i;
      }
    }

    @Override
    public void setValues(PreparedStatement ps, int i) throws SQLException {
      int val = this.array[i];
      StatementCreatorUtils.setParameterValue(ps, 1, SqlTypeValue.TYPE_UNKNOWN, val);
    }

    @Override
    public int getBatchSize() {
      return this.array.length;
    }

    @Override
    public void cleanupParameters() {
      this.cleanupCalled = true;
    }
  }

  static class NopParameterizedPreparedStatementSetter implements ParameterizedPreparedStatementSetter<String>, ParameterDisposer {

    private boolean cleanupCalled = false;

    @Override
    public void setValues(PreparedStatement ps, String argument) throws SQLException {
      // NOP
    }

    @Override
    public void cleanupParameters() {
      this.cleanupCalled = true;
    }
  }

}
