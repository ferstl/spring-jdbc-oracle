# spring-jdbc-oracle
*- Oracle specific extensions for spring-jdbc*

[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.github.ferstl/spring-jdbc-oracle/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.github.ferstl/spring-jdbc-oracle)

This project offers extensions to spring-jdbc that allow using the following Oracle specific features:

* native named parameters
* arrays
* explicit statement caching


## How to use the spring-jdbc-oracle

### Maven Dependencies
Binaries and source code of this project are available on [Maven Central](http://central.maven.org/maven2/com/github/ferstl/spring-jdbc-oracle/), so no further repository configuration is required. However, an additional dependency to [Oracle's proprietary JDBC driver](https://www.oracle.com/technetwork/database/application-development/jdbc/downloads/index.html) (OJDBC) is required and can be retrieved from the [Oracle Maven Repository](https://blogs.oracle.com/dev2dev/get-oracle-jdbc-drivers-and-ucp-from-oracle-maven-repository-without-ides). Once the OJDBC driver is available in your repository, the maven setup looks like this:

```xml
    <!-- Dependency containing the OracleJdbcTemplate -->
    <dependency>
      <groupId>com.github.ferstl</groupId>
      <artifactId>spring-jdbc-oracle</artifactId>
      <version>2.0.0</version>
    </dependency>
    
    <!-- Dependency to Oracle's JDBC driver (if you installed in manually the coordinates may differ). -->
    <dependency>
      <groupId>com.oracle.jdbc</groupId>
      <artifactId>ojdbc8</artifactId>
      <version>18.3.0.0</version>
    </dependency>
```

### Named Parameter Support

Oracle natively supports named parameters yet Spring's `NamedParameterJdbcTemplate` still ends up parsing and rewriting the query. This is unnecessary when proprietary Oracle vendor extensions are used.

`OracleNamedParameterJdbcTemplate` is subclass of Spring's classic `NamedParameterJdbcTemplate` called which overwrites the methods
* `int update(String, SqlParameterSource, KeyHolder, String[])`
* `int[] batchUpdate(String, SqlParameterSource[])`
* `PreparedStatementCreator.getPreparedStatementCreator(String, SqlParameterSource)`

The classic `NamedParameterJdbcTemplate` contains a few more methods but all of them end up calling one of the three methods mentioned above.


#### Usage of the OracleNamedParameterJdbcTemplate

The `OracleNamedParameterJdbcTemplate` is a replacement for Spring's `NamedParameterJdbcTemplate` which works only on Oracle databases. You can use the `OracleNamedParameterJdbcTemplate` in almost the same way. The only difference is the collections are not supported, instead arrays with `SqlOracleArrayValue` have to be used:

```java
    @Bean
    DataSource dataSource() {
      // Create a DataSource for your Oracle DB.
      // ...
    }
    
    @Bean
    public NamedParameterJdbcOperations namedParameterJdbcOperations() {
      return new OracleNamedParameterJdbcTemplate(dataSource());
    }
```


## Array Parameter Support

Array support in Oracle is different from other databases in that Oracle does not support creating arrays from an element type, instead a new array type has to be created. This means that vendor extensions to JDBC have to be used to create `java.sql.Array` instances.

Arrays are a good replacement for [dynamic IN lists](https://blog.jooq.org/2018/04/13/when-using-bind-variables-is-not-enough-dynamic-in-lists/).

```java
this.jdbcOperations.query("SELECT * FROM some_table WHERE id IN (?)",
    rowMapper,
    new SqlOracleArrayValue("CUSTOM_ARRAY_TYPE", ids);
```

`SqlOracleArrayValue` can be used with either the standard `JdbcTemplate` or the `OracleNamedParameterJdbcTemplate`.

## Explicit Statement Caching

Besides the implicitly statement caching which caches a certain number of the most recently used statements OJDBC also offers an [explicit statement caching API](https://docs.oracle.com/en/database/oracle/oracle-database/18/jjdbc/statement-and-resultset-caching.html#GUID-DFBC7F09-5F27-42E1-8044-24733A6AE5F8) that only caches statements upon request giving the user more control.

The `CachedPreparedStatementCreator` creates a `PreparedStatement` that will be cached under a predetermined key.

```java
this.jdbcOperations.query(new CachedPreparedStatementCreator(cacheKey, SQL), rowMapper);
```

At the moment explicit statement caching can not be used with `OracleNamedParameterJdbcTemplate` because `NamedParameterJdbcOperations` does not offer any methods that take a `PreparedStatementCreator`.

## Connection Pools

The project has been tested with these connection pools:
* [Commons DBCP](https://commons.apache.org/proper/commons-dbcp/)
* [Tomcat JDBC Connection Pool](https://tomcat.apache.org/tomcat-9.0-doc/jdbc-pool.html).

There are also integration tests working with these pools. See below for further details about running these tests.


## Project Layout
This project consists of three modules:
* `spring-jdbc-oracle`: This module contains the `OracleNamedParameterJdbcTemplate` and the `SqlOracleArrayValue` and is the only module you need at a user's viewpoint.
* `spring-jdbc-oracle-ojdbc`: This module contains a fake `OraclePreparedStatement` interface that declares the Oracle-specific methods used by the `OracleJdbcTemplate`. The module is used at compile time in order to avoid having a dependency to the OJDBC driver which is not on maven central.
* `spring-jdbc-oracle-integrationtests`: This module contains integration tests that run agains an Oracle database. Take a look at the next section for further details.

### How to run the Integration Tests
1. Get yourself an Oracle database. In case you don't happen to have a significant amount of spare money under your pillow, [Oracle XE](http://www.oracle.com/technetwork/products/express-edition/overview/index.html) works perfectly fine.
1. Get the Oracle's [OJDBC driver](https://www.oracle.com/technetwork/database/application-development/jdbc/downloads/index.html) and install it in your maven repository. The POM file of the integration tests defines these coordinates: `com.oracle:ojdbc8:12.2.0.1`. Adjust the POM file in case you install the library under different coordinates.
1. The default configuration of the integration tests assumes that you are running an Oracle database with the SID `XE` and a schema called `spring_jdbc_oracle` on localhost. Should this not be the case in your environment, you need to create a property file called `database_<your-system-username>.properties` in `spring-jdbc-oracle-integrationtest/src/test/resources` defining the coordinates of your database and schema. Take a look at the `database.properties` configuration file for the values to configure.
1. Once the database is set up and configured, the tests need to be run with the system property `-Dtestgroup=integration`, e.g `mvn -Dtestgroup=integration clean test`.

