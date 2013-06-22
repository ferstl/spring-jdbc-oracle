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
        this.env.getProperty("db.defaultAutoCommit", Boolean.class));

    ds.setAutoCommit(false);
    return ds;
  }
}
