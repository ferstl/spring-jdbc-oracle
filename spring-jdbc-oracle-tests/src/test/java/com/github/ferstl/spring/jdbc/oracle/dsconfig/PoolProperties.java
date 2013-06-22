package com.github.ferstl.spring.jdbc.oracle.dsconfig;

import java.util.Properties;

import org.springframework.core.env.Environment;

/**
 * Create connection pool properties from the Spring environment.
 */
final class PoolProperties {

  public static Properties createFromEnvironment(Environment env) {
    Properties props = new Properties();
    props.setProperty("url", env.getProperty("db.url"));
    props.setProperty("username", env.getProperty("db.username"));
    props.setProperty("password", env.getProperty("db.password"));
    props.setProperty("defaultAutoCommit ", env.getProperty("db.defaultAutoCommit"));

    props.setProperty("driverClassName", env.getProperty("driverClassName"));
    props.setProperty("maxActive", env.getProperty("maxActive"));
    props.setProperty("maxIdle", env.getProperty("maxIdle"));
    props.setProperty("minIdle", env.getProperty("minIdle"));
    props.setProperty("initialSize", env.getProperty("initialSize"));

    return props;
  }

  private PoolProperties() {
    throw new AssertionError("Not instantiable");
  }
}
