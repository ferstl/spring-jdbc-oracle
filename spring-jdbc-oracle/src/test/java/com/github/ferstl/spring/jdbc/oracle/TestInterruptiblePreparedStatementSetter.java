package com.github.ferstl.spring.jdbc.oracle;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.springframework.jdbc.core.ParameterDisposer;
import org.springframework.jdbc.core.SqlTypeValue;
import org.springframework.jdbc.core.StatementCreatorUtils;
import org.springframework.jdbc.core.support.AbstractInterruptibleBatchPreparedStatementSetter;

public class TestInterruptiblePreparedStatementSetter extends AbstractInterruptibleBatchPreparedStatementSetter implements ParameterDisposer {

  private int[] parameters;

  public TestInterruptiblePreparedStatementSetter(int numberOfRows) {
    this.parameters = new int[numberOfRows];

    for (int i = 0; i < numberOfRows; i++) {
      this.parameters[i] = i + 1;
    }
  }

  @Override
  protected boolean setValuesIfAvailable(PreparedStatement ps, int i) throws SQLException {
    if (i >= this.parameters.length) {
      return false;
    }

    StatementCreatorUtils.setParameterValue(ps, 1, SqlTypeValue.TYPE_UNKNOWN, Integer.MAX_VALUE);
    StatementCreatorUtils.setParameterValue(ps, 2, SqlTypeValue.TYPE_UNKNOWN, this.parameters[i]);

    return true;
  }

  @Override
  public void cleanupParameters() {
    this.parameters = null;
  }

  public boolean isCleanedUp() {
    return this.parameters == null;
  }
}