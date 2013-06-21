package com.github.ferstl.spring.jdbc.oracle.dsconfig;

import java.sql.SQLException;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.datasource.SingleConnectionDataSource;

@Profile(DataSourceProfile.SINGLE_CONNECTION)
@Configuration
public class SingleConnectionDataSourceConfiguration {

  @Autowired
  private Environment env;

  @Bean
  public DataSource dataSource() throws SQLException {
    SingleConnectionDataSource ds = new SingleConnectionDataSource(
        this.env.getProperty("db.url"),
        this.env.getProperty("db.username"),
        this.env.getProperty("db.password"),
        false);

    ds.setAutoCommit(false);
    return ds;
  }
}
