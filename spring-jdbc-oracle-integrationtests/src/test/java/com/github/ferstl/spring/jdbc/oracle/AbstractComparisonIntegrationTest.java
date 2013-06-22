/*
 * Copyright (c) 2013 by Stefan Ferstl <st.ferstl@gmail.com>
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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ParameterizedPreparedStatementSetter;
import org.springframework.jdbc.core.SqlTypeValue;
import org.springframework.jdbc.core.StatementCreatorUtils;
import org.springframework.jdbc.core.support.AbstractInterruptibleBatchPreparedStatementSetter;

import static org.junit.Assert.assertEquals;

/**
 * This test compares the size of the result arrays of the {@link OracleJdbcTemplate} the the result arrays of the
 * Spring's classic {@link JdbcTemplate}.
 */
public abstract class AbstractComparisonIntegrationTest extends AbstractOracleJdbcTemplateIntegrationTest {

  private static final String INSERT_SQL = "INSERT INTO test_table(id, val, numval) VALUES (seq_test_table.nextval, ?, ?)";

  @Autowired
  private JdbcTemplate classicJdbcTemplate;

  private int nrOfInserts;

  @Before
  public void before() {
    this.nrOfInserts = this.batchSize * 2 + 2;
  }

  @Test
  public void updateWithArgList() {
    List<Object[]> argList = createArgList(this.nrOfInserts);
    int[] resultOracle = this.jdbcTemplate.batchUpdate(INSERT_SQL, argList);
    int[] resultClassic = this.classicJdbcTemplate.batchUpdate(INSERT_SQL, argList);

    assertEquals(resultOracle.length, resultClassic.length);
  }

  @Test
  public void updateWithEmptyArgList() {
    List<Object[]> argList = Collections.emptyList();
    int[] resultOracle = this.jdbcTemplate.batchUpdate(INSERT_SQL, argList);
    int[] resultClassic = this.classicJdbcTemplate.batchUpdate(INSERT_SQL, argList);

    assertEquals(resultOracle.length, resultClassic.length);
  }

  @Test
  public void updateWithPss() {
    int[] resultOracle =
        this.jdbcTemplate.batchUpdate(INSERT_SQL, new InsertBatchPreparedStatementSetter(this.batchSize));
    int[] resultClassic =
        this.classicJdbcTemplate.batchUpdate(INSERT_SQL, new InsertBatchPreparedStatementSetter(this.batchSize));

    assertEquals(resultOracle.length, resultClassic.length);
  }

  @Test
  public void updateWithEmptyPss() {
    int[] resultOracle =
        this.jdbcTemplate.batchUpdate(INSERT_SQL, new InsertBatchPreparedStatementSetter(0));
    int[] resultClassic =
        this.classicJdbcTemplate.batchUpdate(INSERT_SQL, new InsertBatchPreparedStatementSetter(0));

    assertEquals(resultOracle.length, resultClassic.length);
  }

  @Test
  public void updateWithInterruptiblePss() {
    int[] resultOracle =
        this.jdbcTemplate.batchUpdate(INSERT_SQL, new InsertInterruptiblePreparedStatementSetter(this.batchSize));
    int[] resultClassic =
        this.classicJdbcTemplate.batchUpdate(INSERT_SQL, new InsertInterruptiblePreparedStatementSetter(this.batchSize));

    assertEquals(resultOracle.length, resultClassic.length);
  }

  @Test
  public void updateWithEmptyInterruptiblePss() {
    int[] resultOracle =
        this.jdbcTemplate.batchUpdate(INSERT_SQL, new InsertInterruptiblePreparedStatementSetter(0));
    int[] resultClassic =
        this.classicJdbcTemplate.batchUpdate(INSERT_SQL, new InsertInterruptiblePreparedStatementSetter(0));

    assertEquals(resultOracle.length, resultClassic.length);
  }

  @Test
  public void updateWithParameterizedPss() {
    List<Object[]> argList = createArgList(this.nrOfInserts);

    int[][] resultOracle =
        this.jdbcTemplate.batchUpdate(INSERT_SQL, argList, this.batchSize, new InsertParameterizedPreparedStatementSetter());
    int[][] resultClassic =
        this.classicJdbcTemplate.batchUpdate(INSERT_SQL, argList, this.batchSize, new InsertParameterizedPreparedStatementSetter());

    assertEquals(resultOracle.length, resultClassic.length);
    for (int i = 0; i < resultOracle.length; i++) {
      assertEquals(resultOracle[i].length, resultClassic[i].length);
    }
  }

  @Test
  public void updateWithEmptyParameterizedPss() {
    List<Object[]> argList = createArgList(0);

    int[][] resultOracle =
        this.jdbcTemplate.batchUpdate(INSERT_SQL, argList, this.batchSize, new InsertParameterizedPreparedStatementSetter());
    int[][] resultClassic =
        this.classicJdbcTemplate.batchUpdate(INSERT_SQL, argList, this.batchSize, new InsertParameterizedPreparedStatementSetter());

    assertEquals(resultOracle.length, resultClassic.length);
  }

  private static List<Object[]> createArgList(int nrOfRows) {
    List<Object[]> batchArgs = new ArrayList<>();
    for (int i = 0; i < nrOfRows; i++) {
      batchArgs.add(new Object[] { "INSERT", Integer.MAX_VALUE - i });
    }
    return batchArgs;
  }

  static class InsertBatchPreparedStatementSetter implements BatchPreparedStatementSetter {

    private final int[] values;

    public InsertBatchPreparedStatementSetter(int nrOfRows) {
      this.values = new int[nrOfRows];
      for (int i = 0; i < nrOfRows; i++) {
        this.values[i] = Integer.MAX_VALUE - i;
      }
    }

    @Override
    public void setValues(PreparedStatement ps, int i) throws SQLException {
      StatementCreatorUtils.setParameterValue(ps, 1, SqlTypeValue.TYPE_UNKNOWN, "INSERT");
      StatementCreatorUtils.setParameterValue(ps, 2, SqlTypeValue.TYPE_UNKNOWN, this.values[i]);
    }

    @Override
    public int getBatchSize() {
      return this.values.length;
    }
  }

  static class InsertInterruptiblePreparedStatementSetter extends AbstractInterruptibleBatchPreparedStatementSetter {

    private final int[] values;

    public InsertInterruptiblePreparedStatementSetter(int nrOfRows) {
      this.values = new int[nrOfRows];
      for (int i = 0; i < nrOfRows; i++) {
        this.values[i] = Integer.MAX_VALUE - i;
      }
    }

    @Override
    protected boolean setValuesIfAvailable(PreparedStatement ps, int i) throws SQLException {
      if (i >= this.values.length) {
        return false;
      }

      StatementCreatorUtils.setParameterValue(ps, 1, SqlTypeValue.TYPE_UNKNOWN, "INSERT");
      StatementCreatorUtils.setParameterValue(ps, 2, SqlTypeValue.TYPE_UNKNOWN, this.values[i]);

      return true;
    }

  }

  static class InsertParameterizedPreparedStatementSetter implements ParameterizedPreparedStatementSetter<Object[]> {

    @Override
    public void setValues(PreparedStatement ps, Object[] argument) throws SQLException {
      for (int i = 0; i < argument.length; i++) {
        StatementCreatorUtils.setParameterValue(ps, i + 1, SqlTypeValue.TYPE_UNKNOWN, argument[i]);
      }
    }
  }
}
