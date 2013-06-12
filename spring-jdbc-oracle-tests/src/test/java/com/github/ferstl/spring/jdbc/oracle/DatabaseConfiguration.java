package com.github.ferstl.spring.jdbc.oracle;

import java.util.Properties;

import javax.sql.DataSource;

import org.apache.tomcat.jdbc.pool.DataSourceFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.nativejdbc.NativeJdbcExtractor;
import org.springframework.jdbc.support.nativejdbc.OracleJdbc4NativeJdbcExtractor;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EnableTransactionManagement
public class DatabaseConfiguration {

  @Bean
  public JdbcTemplate jdbcTemplate() throws Exception {
    JdbcTemplate jdbcTemplate = new OracleJdbcTemplate(0, dataSource());
    jdbcTemplate.setNativeJdbcExtractor(nativeJdbcExtractor());
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
  DataSource dataSource() throws Exception {
    DataSourceFactory df = new DataSourceFactory();
    Properties props = new Properties();
    props.setProperty("url", "jdbc:oracle:thin:@localhost:1521:xe");
    props.setProperty("defaultAutoCommit", "false");
    props.setProperty("driverClassName", "oracle.jdbc.OracleDriver");
    props.setProperty("username", "batch");
    props.setProperty("password", "batch");
    props.setProperty("maxActive", "10");
    props.setProperty("maxIdle", "10");
    props.setProperty("minIdle", "10");
    props.setProperty("initialSize", "1");

    return df.createDataSource(props);
  }
}
