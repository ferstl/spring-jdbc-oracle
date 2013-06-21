package com.github.ferstl.spring.jdbc.oracle.dsconfig;

import javax.sql.DataSource;

import org.apache.tomcat.jdbc.pool.DataSourceFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;

@Profile("tomcat-pool")
@Configuration
public class TomcatPoolDataSourceConfiguration {

  @Autowired
  private Environment env;

  @Bean
  public DataSource dataSource() throws Exception {
    DataSourceFactory df = new DataSourceFactory();
    return df.createDataSource(PoolProperties.createFromEnvironment(this.env));
  }
}
