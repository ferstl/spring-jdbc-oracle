package com.github.ferstl.spring.jdbc.oracle.dsconfig;

import javax.sql.DataSource;

import org.apache.commons.dbcp.BasicDataSourceFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;

@Profile(DataSourceProfile.COMMONS_DBCP)
@Configuration
public class CommonsDbcpDataSourceConfiguration {

  @Autowired
  private Environment env;

  @Bean
  public DataSource dataSource() throws Exception {
    return BasicDataSourceFactory.createDataSource(PoolProperties.createFromEnvironment(this.env));
  }
}
