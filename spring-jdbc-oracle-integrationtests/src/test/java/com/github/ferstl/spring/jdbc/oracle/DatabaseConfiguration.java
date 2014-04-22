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

import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.init.DatabasePopulatorUtils;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

import com.github.ferstl.spring.jdbc.oracle.dsconfig.CommonsDbcpDataSourceConfiguration;
import com.github.ferstl.spring.jdbc.oracle.dsconfig.SingleConnectionDataSourceConfiguration;
import com.github.ferstl.spring.jdbc.oracle.dsconfig.TomcatPoolDataSourceConfiguration;

@Configuration
@PropertySource({
  "classpath:database.properties",
  "classpath:database_${user.name}.properties",
  "classpath:connectionpool.properties"})
@Import({
  SingleConnectionDataSourceConfiguration.class,
  TomcatPoolDataSourceConfiguration.class,
  CommonsDbcpDataSourceConfiguration.class})
@EnableTransactionManagement
public class DatabaseConfiguration {

  private static final int NUMBER_OF_ROWS = 10000;
  private static final String INSERT_SQL = "INSERT INTO test_table(id, val, numval) VALUES(seq_test_table.nextval, ?, ?)";

  @Autowired
  private DataSource dataSource;

  @Autowired
  private Environment env;

  @Bean
  public JdbcTemplate jdbcTemplate() throws Exception {
    prepareDatabase();

    JdbcTemplate jdbcTemplate = new OracleJdbcTemplate(this.env.getProperty("db.batchsize", Integer.class), this.dataSource);
    initDatabase(jdbcTemplate);

    return jdbcTemplate;
  }

  @Bean
  public JdbcTemplate classicJdbcTemplate() {
    JdbcTemplate jdbcTemplate = new JdbcTemplate(this.dataSource);

    return jdbcTemplate;
  }

  @Bean
  public NamedParameterJdbcTemplate npJdbcTemplate() throws Exception {
    return new NamedParameterJdbcTemplate(jdbcTemplate());
  }

  @Bean
  public OracleNamedParameterJdbcTemplate onpJdbcTemplate() throws Exception {
    return new OracleNamedParameterJdbcTemplate(jdbcTemplate());
  }

  @Bean
  PlatformTransactionManager transactionManager() {
    return new DataSourceTransactionManager(this.dataSource);
  }

  private void prepareDatabase() {
    final ResourceDatabasePopulator populator = new ResourceDatabasePopulator();
    populator.addScript(new ClassPathResource("prepare-database.sql"));
    populator.setIgnoreFailedDrops(true);

    TransactionTemplate trxTemplate = new TransactionTemplate(transactionManager());
    trxTemplate.execute(new TransactionCallback<Void>() {

      @Override
      public Void doInTransaction(TransactionStatus status) {
        DatabasePopulatorUtils.execute(populator, DatabaseConfiguration.this.dataSource);
        return null;
      }
    });
  }

  private void initDatabase(final JdbcTemplate jdbcTemplate) {
    TransactionTemplate trxTemplate = new TransactionTemplate(transactionManager());
    trxTemplate.execute(new TransactionCallback<int[]>() {

      @Override
      public int[] doInTransaction(TransactionStatus status) {
        List<Object[]> batchArgs = new ArrayList<>(NUMBER_OF_ROWS);
        for (int i = 0; i < NUMBER_OF_ROWS; i++) {
          int value = i + 1;
          batchArgs.add(new Object[] {String.format("Value_%05d", value), value});
        }
        return jdbcTemplate.batchUpdate(INSERT_SQL, batchArgs);
      }
    });
  }
}
