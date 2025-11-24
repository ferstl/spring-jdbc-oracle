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

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import com.github.ferstl.spring.jdbc.oracle.RollbackSingleConnectionDataSource;

@Profile(DataSourceProfile.ROLLBACK_SINGLE_CONNECTION)
@Configuration
public class RollbackSingleConnectionDataSourceConfiguration {

  @Bean
  @SuppressWarnings("deprecation")
  public DataSource dataSource(
      @Value("${db.url}") String url,
      @Value("${db.username}") String userName,
      @Value("${db.password}") String password) {
    RollbackSingleConnectionDataSource ds = new RollbackSingleConnectionDataSource(url, userName, password, true);

    ds.setAutoCommit(false);
    return ds;
  }
}
