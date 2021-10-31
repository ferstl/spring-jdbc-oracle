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


public final class DataSourceProfile {

  public static final String SINGLE_CONNECTION = "single-connection";
  public static final String ROLLBACK_SINGLE_CONNECTION = "rollback-single-connection";
  public static final String TOMCAT_POOL = "tomcat-pool";
  public static final String COMMONS_DBCP = "dbcp-pool";
  public static final String HIKARICP = "hikaricp";
  public static final String UCP = "ucp";

  private DataSourceProfile() {
    throw new AssertionError("Not instantiable");
  }

}
