# spring-jdbc-oracle
*- Oracle specific extensions for spring-jdbc*

[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.github.ferstl/spring-jdbc-oracle/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.github.ferstl/spring-jdbc-oracle)

This project offers extensions to spring-jdbc that allow using the following Oracle specific features:

* Native named parameters
* Arrays
* Explicit statement caching


## How to use spring-jdbc-oracle

### Maven Dependencies
Binaries and source code of this project are available on [Maven Central](http://central.maven.org/maven2/com/github/ferstl/spring-jdbc-oracle/), so no further repository configuration is required. The maven setup looks like this:

```xml
    <!-- Dependency containing the OracleJdbcTemplate -->
    <dependency>
      <groupId>com.github.ferstl</groupId>
      <artifactId>spring-jdbc-oracle</artifactId>
      <version>2.0.0</version>
    </dependency>
```

### Named Parameter Support

Oracle natively supports named parameters yet Spring's `NamedParameterJdbcTemplate` still ends up parsing and rewriting the query. This is unnecessary when proprietary Oracle vendor extensions are used.

`OracleNamedParameterJdbcTemplate` is a subclass of Spring's classic `NamedParameterJdbcTemplate` which overwrites the methods
* `int update(String, SqlParameterSource, KeyHolder, String[])`
* `int[] batchUpdate(String, SqlParameterSource[])`
* `PreparedStatementCreator.getPreparedStatementCreator(String, SqlParameterSource)`

The classic `NamedParameterJdbcTemplate` contains a few more methods but all of them end up calling one of the three methods mentioned above.


#### Usage of the OracleNamedParameterJdbcTemplate

The `OracleNamedParameterJdbcTemplate` is a replacement for Spring's `NamedParameterJdbcTemplate` which works only on Oracle databases. You can use the `OracleNamedParameterJdbcTemplate` in almost the same way. The only difference is that collections are not supported, instead arrays with `SqlOracleArrayValue` have to be used:

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

Besides the implicit statement caching, which caches a certain number of the most recently used statements, OJDBC also offers an [explicit statement caching API](https://docs.oracle.com/en/database/oracle/oracle-database/18/jjdbc/statement-and-resultset-caching.html#GUID-DFBC7F09-5F27-42E1-8044-24733A6AE5F8) that only caches statements upon request giving the user more control.

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

### How to run the Integration Tests
1. Follow the instructions of [Oracle Database on Docker](https://github.com/oracle/docker-images/tree/master/OracleDatabase/SingleInstance) to build a 19.3.0-se2 Docker image using `./buildDockerImage.sh -v 19.3.0 -s`.
1. Run `spring-jdbc-oracle-integrationtests/src/test/resources/run_oracle.sh`. If the image already rests run `docker start spring-jdbc-oracle`. Wait for a long time for the image to start.
1. Once Docker image is started, the tests need to be run with the tag `integration`, e.g `mvn test -Dgroups="integration"`.

