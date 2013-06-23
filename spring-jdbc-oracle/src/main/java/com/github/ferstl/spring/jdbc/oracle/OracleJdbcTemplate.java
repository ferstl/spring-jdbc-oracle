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

import java.util.Collection;

import javax.sql.DataSource;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ParameterDisposer;
import org.springframework.jdbc.core.ParameterizedPreparedStatementSetter;

/**
 * A subclass of Spring's {@link JdbcTemplate} the uses
 * <a href="http://docs.oracle.com/cd/B28359_01/java.111/b31224/oraperf.htm#autoId2">Oracle Update Batching</a>. Each of
 * the {@code batchUpdate()} methods in this class will return the number of updated rows on the DB. However, batches
 * are processed as a whole, so it is not possible to find out the number of updated rows for each individual statement
 * in a batch. For example, if the batch size is set to 5 and a batch update containing 7 statements
 * (each of which updates exactly one row) is executed, the result will be {@code [0, 0, 0, 0, 5, 0, 2]}.
 */
public class OracleJdbcTemplate extends JdbcTemplate {

  private final int sendBatchSize;

  public OracleJdbcTemplate(int sendBatchSize) {
    validateSendBatchSize(sendBatchSize);
    this.sendBatchSize = sendBatchSize;
  }

  public OracleJdbcTemplate(int sendBatchSize, DataSource dataSource, boolean lazyInit) {
    super(dataSource, lazyInit);
    validateSendBatchSize(sendBatchSize);
    this.sendBatchSize = sendBatchSize;
  }

  public OracleJdbcTemplate(int sendBatchSize, DataSource dataSource) {
    super(dataSource);
    validateSendBatchSize(sendBatchSize);
    this.sendBatchSize = sendBatchSize;
  }

  @Override
  public int[] batchUpdate(String sql, final BatchPreparedStatementSetter pss) throws DataAccessException {
    if (this.logger.isDebugEnabled()) {
      this.logger.debug("Executing SQL batch update [" + sql + "]");
    }

    try {
      return execute(sql, new BatchingPreparedStatementCallback(this.sendBatchSize, pss));
    } finally {
      if (pss instanceof ParameterDisposer) {
        ((ParameterDisposer) pss).cleanupParameters();
      }
    }
  }

  @Override
  public <T> int[][] batchUpdate(
      String sql, Collection<T> batchArgs, int batchSize, ParameterizedPreparedStatementSetter<T> ppss) {

    if (this.logger.isDebugEnabled()) {
      this.logger.debug("Executing SQL batch update [" + sql + "] with a batch size of " + batchSize);
    }

    try {
      return execute(sql, new ParameterizedBatchingPreparedStatementCallback<>(ppss, batchSize, batchArgs));
    } finally {
      if (ppss instanceof ParameterDisposer) {
        ((ParameterDisposer) ppss).cleanupParameters();
      }
    }
  }

  private static void validateSendBatchSize(int sendBatchSize) {
    if (sendBatchSize < 1) {
      throw new IllegalArgumentException("Invalid batch size: " + sendBatchSize + ". Must be greater than 0.");
    }
  }

}
