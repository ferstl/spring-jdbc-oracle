# spring-jdbc-oracle
*- Oracle specific extensions for spring-jdbc*

[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.github.ferstl/spring-jdbc-oracle/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.github.ferstl/spring-jdbc-oracle)

Oracle natively supports named parameter yet Spring's NamedParameterJdbcTemplate still ends up parsing and rewriting the query. This is unnecessary when proprietary Oracle vendor extensions are used.

This project contains a subclass of Spring's classic `NamedParameterJdbcTemplate` called `OracleNamedParameterJdbcTemplate` which overwrites the methods
* `int update(String, SqlParameterSource, KeyHolder, String[])`
* `int[] batchUpdate(String, SqlParameterSource[])`
* `PreparedStatementCreator .getPreparedStatementCreator(String, SqlParameterSource)`

The classic NamedParameterJdbcTemplate contains a few more methods but all of them result in a call of the three methods mentioned above.


### How to use the OracleJdbcTemplate

#### Maven Dependencies
Binaries and source code of this project are available on [Maven Central](http://central.maven.org/maven2/com/github/ferstl/spring-jdbc-oracle/), so no further repository configuration is required. However, an additional dependency to [Oracle's proprietary JDBC driver](http://www.oracle.com/technetwork/database/enterprise-edition/jdbc-112010-090769.html) (OJDBC) is required and needs to be installed manually in your maven repositories. Once the OJDBC driver is available in your repository, the maven setup looks like this:

    <!-- Dependency containing the OracleJdbcTemplate -->
    <dependency>
      <groupId>com.github.ferstl</groupId>
      <artifactId>spring-jdbc-oracle</artifactId>
      <version>2.0.0</version>
    </dependency>
    
    <!-- Dependency to Oracle's JDBC driver (use the coordinates under which you deployed the driver). -->
    <dependency>
      <groupId>com.oracle</groupId>
      <artifactId>ojdbc8</artifactId>
      <version>12.2.0.1</version>
    </dependency>

#### Usage of the OracleNamedParameterJdbcTemplate
The `OracleNamedParameterJdbcTemplate` is a replacement for Spring's `NamedParameterJdbcTemplate` which works only on Oracle databases. You can use the `OracleNamedParameterJdbcTemplate` in almost the same way. The only difference is the collections are not supported, instead arrays with `SqlOracleArrayValue` have to be used:

    @Bean
    DataSource dataSource() {
      // Create a DataSource for your Oracle DB.
      // ...
    }
    
    @Bean
    public NamedParameterJdbcOperations namedParameterJdbcOperations() {
      return new OracleNamedParameterJdbcTemplate(dataSource());
    }

#### Connection Pools
The `OracleJdbcTemplate` has been tested with these connection pools:
* [Commons DBCP](http://commons.apache.org/proper/commons-dbcp/)
* [Tomcat JDBC Connection Pool](https://tomcat.apache.org/tomcat-8.0-doc/jdbc-pool.html).


There are also integration tests working with these pools. See below for further details about running these tests.


### Project Layout
This project consists of three modules:
* `spring-jdbc-oracle`: This module contains the `OracleNamedParameterJdbcTemplate` and the `SqlOracleArrayValue` and is the only module you need at a user's viewpoint.
* `spring-jdbc-oracle-ojdbc`: This module contains a fake `OraclePreparedStatement` interface that declares the Oracle-specific methods used by the `OracleJdbcTemplate`. The module is used at compile time in order to avoid having a dependency to the OJDBC driver which is not on maven central.
* `spring-jdbc-oracle-integrationtests`: This module contains integration tests that run agains an Oracle database. Take a look at the next section for further details.

#### How to run the Integration Tests
1. Get yourself an Oracle database. In case you don't happen to have a significant amount of spare money under your pillow, [Oracle XE](http://www.oracle.com/technetwork/products/express-edition/overview/index.html) works perfectly fine.
1. Get the Oracle's [OJDBC driver](http://www.oracle.com/technetwork/database/enterprise-edition/jdbc-112010-090769.html) and install it in your maven repository. The POM file of the integration tests defines these coordinates: `com.oracle:ojdbc6:11.2.0.3.0`. Adjust the POM file in case you install the library under different coordinates.
1. The default configuration of the integration tests assumes that you are running an Oracle database with the SID `XE` and a schema called `spring_jdbc_oracle` on localhost. Should this not be the case in your environment, you need to create a property file called `database_<your-system-username>.properties` in `spring-jdbc-oracle-integrationtest/src/test/resources` defining the coordinates of your database and schema. Take a look at the `database.properties` configuration file for the values to configure.
1. Once the database is set up and configured, the tests need to be run with the system property `-Dtestgroup=integration`, e.g `mvn -Dtestgroup=integration clean test`.

