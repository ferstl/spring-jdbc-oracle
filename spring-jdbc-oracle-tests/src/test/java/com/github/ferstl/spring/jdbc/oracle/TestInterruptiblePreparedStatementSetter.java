package com.github.ferstl.spring.jdbc.oracle;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.springframework.jdbc.core.SqlTypeValue;
import org.springframework.jdbc.core.StatementCreatorUtils;
import org.springframework.jdbc.core.support.AbstractInterruptibleBatchPreparedStatementSetter;

class SingleRowInterruptiblePreparedStatementSetter extends AbstractInterruptibleBatchPreparedStatementSetter {

  private final int[] parameters;

  public SingleRowInterruptiblePreparedStatementSetter(int numberOfRows) {
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

    StatementCreatorUtils.setParameterValue(ps, 1, SqlTypeValue.TYPE_UNKNOWN, 42);
    StatementCreatorUtils.setParameterValue(ps, 2, SqlTypeValue.TYPE_UNKNOWN, this.parameters[i]);

    return true;
  }
}