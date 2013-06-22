package com.github.ferstl.spring.jdbc.oracle;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.springframework.jdbc.core.ParameterDisposer;
import org.springframework.jdbc.core.ParameterizedPreparedStatementSetter;
import org.springframework.jdbc.core.SqlTypeValue;
import org.springframework.jdbc.core.StatementCreatorUtils;

public class TestParameterizedPreparedStatementSetter implements ParameterizedPreparedStatementSetter<int[]>, ParameterDisposer {

  private boolean cleanedUp = false;

  @Override
  public void setValues(PreparedStatement ps, int[] argument) throws SQLException {
    for (int i = 0; i < argument.length; i++) {
      StatementCreatorUtils.setParameterValue(ps, i + 1, SqlTypeValue.TYPE_UNKNOWN, argument[i]);
    }
  }

  @Override
  public void cleanupParameters() {
    this.cleanedUp = true;
  }

  public boolean isCleanedUp() {
    return this.cleanedUp;
  }

}