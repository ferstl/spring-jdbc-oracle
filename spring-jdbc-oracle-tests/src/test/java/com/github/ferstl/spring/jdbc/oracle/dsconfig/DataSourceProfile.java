package com.github.ferstl.spring.jdbc.oracle.dsconfig;


public final class DataSourceProfile {

  public static final String SINGLE_CONNECTION = "single-connection";
  public static final String TOMCAT_POOL = "tomcat-pool";
  public static final String COMMONS_DBCP = "dbcp-pool";

  private DataSourceProfile() {
    throw new AssertionError("Not instantiable");
  }

}
