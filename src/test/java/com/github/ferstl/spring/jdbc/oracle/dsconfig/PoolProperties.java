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
    props.setProperty("defaultAutoCommit", "false");

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
