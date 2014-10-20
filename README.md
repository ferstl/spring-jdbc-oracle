# spring-jdbc-oracle
*- A variant of Spring's JdbcTemplate that uses Oracle Update Batching*

[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.github.ferstl/spring-jdbc-oracle/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.github.ferstl/spring-jdbc-oracle)

If Spring's classic JdbcTemplate is used in combination with an Oracle DB, the `batchUpdate()` methods won't return the number of affected rows. Instead, these methods do always return an array containing -2 (`Statement#SUCCESS_NO_INFO`) in each element. In order to get the number of affected rows during a batch INSERT/UPDATE/DELETE, it is required to use [Oracle Update Batching](http://docs.oracle.com/cd/B28359_01/java.111/b31224/oraperf.htm#autoId2).

This project contains a subclass of Spring's classic `JdbcTemplate` called `OracleJdbcTemplate` which overwrites the methods
* `int[] batchUpdate(String sql, BatchPreparedStatementSetter pss)` and
* `<T> int[][] batchUpdate(String sql, Collection<T> batchArgs, int batchSize, ParameterizedPreparedStatementSetter<T> pss)`

The classic JdbcTemplate contains a few more overloaded `batchUpdate()` methods but all of them result in a call of the two methods mentioned above.


### How to use the OracleJdbcTemplate

#### Maven Dependencies
Binaries and source code of this project are available on [Maven Central](http://central.maven.org/maven2/com/github/ferstl/spring-jdbc-oracle/), so no further repository configuration is required. However, an additional dependency to [Oracle's proprietary JDBC driver](http://www.oracle.com/technetwork/database/enterprise-edition/jdbc-112010-090769.html) (OJDBC) is required and needs to be installed manually in your maven repositories. Once the OJDBC driver is available in your repository, the maven setup looks like this:

    <!-- Dependency containing the OracleJdbcTemplate -->
    <dependency>
      <groupId>com.github.ferstl</groupId>
      <artifactId>spring-jdbc-oracle</artifactId>
      <version>0.9.0</version>
    </dependency>
    
    <!-- Dependency to Oracle's JDBC driver (use the coordinates under which you deployed the driver). -->
    <dependency>
      <groupId>com.oracle</groupId>
      <artifactId>ojdbc6</artifactId>
      <version>11.2.0.3.0</version>
    </dependency>

#### Usage of the OracleJdbcTemplate
The `OracleJdbcTemplate` is a drop-in replacement for Spring's `JdbcTemplate` which works only on Oracle databases. You can use the `OracleJdbcTemplate` in exactly the same way. The only difference is the batch size as additional constructor argument:

    @Bean
    DataSource dataSource() {
      // Create a DataSource for your Oracle DB.
      // ...
    }
    
    @Bean
    public JdbcTemplate jdbcTemplate() {
      return new OracleJdbcTemplate(30, dataSource());
    }

#### Connection Pools
The `OracleJdbcTemplate` has been tested with these connection pools:
* [Commons DBCP](http://commons.apache.org/proper/commons-dbcp/)
* [Tomcat JDBC Connection Pool](https://tomcat.apache.org/tomcat-8.0-doc/jdbc-pool.html).


Since version `1.0.0`, no special configuration has to be made when a connection pool is used. Versions prior to `1.0.0` have to set a `NativeJdbcExtractor` on the `OracleJdbcTemplate` in order to avoid `ClassCastException`s.

There are also integratin tests working with these pools. See below for further details about running these tests.


### Project Layout
This project consists of three modules:
* `spring-jdbc-oracle`: This module contains the `OracleJdbcTemplate` and is the only module you need at a user's viewpoint.
* `spring-jdbc-oracle-ojdbc`: This module contains a fake `OraclePreparedStatement` interface that declares the Oracle-specific methods used by the `OracleJdbcTemplate`. The module is used at compile time in order to avoid having a dependency to the OJDBC driver which is not on maven central.
* `spring-jdbc-oracle-integrationtests`: This module contains integration tests that run agains an Oracle database. Take a look at the next section for further details.

#### How to run the Integration Tests
1. Get yourself an Oracle database. In case you don't happen to have a significant amount of spare money under your pillow, [Oracle XE](http://www.oracle.com/technetwork/products/express-edition/overview/index.html) works perfectly fine.
1. Get the Oracle's [OJDBC driver](http://www.oracle.com/technetwork/database/enterprise-edition/jdbc-112010-090769.html) and install it in your maven repository. The POM file of the integration tests defines these coordinates: `com.oracle:ojdbc6:11.2.0.3.0`. Adjust the POM file in case you install the library under different coordinates.
1. The default configuration of the integration tests assumes that you are running an Oracle database with the SID `XE` and a schema called `spring_jdbc_oracle` on localhost. Should this not be the case in your environment, you need to create a property file called `database_<your-system-username>.properties` in `spring-jdbc-oracle-integrationtest/src/test/resources` defining the coordinates of your database and schema. Take a look at the `database.properties` configuration file for the values to configure.
1. Once the database is set up and configured, the tests need to be run with the system property `-Dtestgroup=integration`, e.g `mvn -Dtestgroup=integration clean test`.


### FAQ
**Q**: Does the `OracleJdbcTemplate` return the number of affected rows for each statement in a batch?
**A**: No. It returns the total number of affected rows at the end of a batch. For example, with a batch size of 5 and 7 statements (each of which updates exactly one row), the result of a `OracleJdbcTemplate#batchUpdate()` call would be `[0, 0, 0, 0, 5, 0, 2]`.

**Q**: Does Oracle Update Batching perform better?
**A**: I haven't made any performance tests myself. If this is a critical issue for you, you should make some tests.


**Q**: What is a "good" batch size?
**A**: According to the documentation, a "good" batch size is around 30. But you should make some tests to find out the batch size that works best in your environment.
