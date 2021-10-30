/*
 * Copyright (c) 2021 by Philippe Marschall <philippe.marschall@gmail.com>
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

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import oracle.ucp.jdbc.PoolDataSource;
import oracle.ucp.jdbc.PoolDataSourceFactory;

@Profile(DataSourceProfile.UCP)
@Configuration
public class UcpDataSourceConfiguration {

  @Bean
  public DataSource dataSource(
          @Value("${db.url}") String url,
          @Value("${db.username}") String userName,
          @Value("${db.password}") String password,
          @Value("${initialSize}") int initialSize,
          @Value("${minIdle}") int minIdle,
          @Value("${maxActive}") int maxActive) throws SQLException {

    PoolDataSource  dataSource = PoolDataSourceFactory.getPoolDataSource();
    
    dataSource.setConnectionFactoryClassName("oracle.jdbc.pool.OracleDataSource");
    dataSource.setURL(url);
    dataSource.setUser(userName);
    dataSource.setPassword(password);
    dataSource.setInitialPoolSize(initialSize);
    dataSource.setMinPoolSize(minIdle);
    dataSource.setMaxPoolSize(maxActive);
    
    return dataSource;
  }
}
