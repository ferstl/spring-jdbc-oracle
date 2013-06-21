package com.github.ferstl.spring.jdbc.oracle;

import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.init.DatabasePopulatorUtils;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.jdbc.support.nativejdbc.NativeJdbcExtractor;
import org.springframework.jdbc.support.nativejdbc.OracleJdbc4NativeJdbcExtractor;
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
  "connectionpool.properties"})
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

  @Bean
  public JdbcTemplate jdbcTemplate() throws Exception {
    prepareDatabase();

    JdbcTemplate jdbcTemplate = new OracleJdbcTemplate(10, this.dataSource);
    jdbcTemplate.setNativeJdbcExtractor(nativeJdbcExtractor());
    initDatabase(jdbcTemplate);

    return jdbcTemplate;
  }

  @Bean
  public NamedParameterJdbcTemplate namedParameterJdbcTemplate() throws Exception {
    return new NamedParameterJdbcTemplate(jdbcTemplate());
  }

  @Bean
  NativeJdbcExtractor nativeJdbcExtractor() {
    return new OracleJdbc4NativeJdbcExtractor();
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
