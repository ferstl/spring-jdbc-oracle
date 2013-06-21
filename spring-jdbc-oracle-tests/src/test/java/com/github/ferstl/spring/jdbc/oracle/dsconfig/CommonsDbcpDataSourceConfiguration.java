package com.github.ferstl.spring.jdbc.oracle;

import java.util.Properties;

import javax.sql.DataSource;

import org.apache.commons.dbcp.BasicDataSourceFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;

@Profile("dbcp-pool")
@Configuration
public class CommonsDbcpDataSourceConfiguration {

  @Autowired
  private Environment env;

  @Bean
  public DataSource dataSource() throws Exception {
    Properties props = new Properties();
    props.setProperty("url", this.env.getProperty("db.url"));
    props.setProperty("username", this.env.getProperty("db.username"));
    props.setProperty("password", this.env.getProperty("db.password"));
    props.setProperty("defaultAutoCommit", this.env.getProperty("defaultAutoCommit"));
    props.setProperty("driverClassName", this.env.getProperty("driverClassName"));
    props.setProperty("maxActive", this.env.getProperty("maxActive"));
    props.setProperty("maxIdle", this.env.getProperty("maxIdle"));
    props.setProperty("minIdle", this.env.getProperty("minIdle"));
    props.setProperty("initialSize", this.env.getProperty("initialSize"));

    return BasicDataSourceFactory.createDataSource(props);
  }
}
