# spring-jdbc-oracle
*- A variant of Spring's JdbcTemplate that uses [Oracle Update Batching](http://docs.oracle.com/cd/B28359_01/java.111/b31224/oraperf.htm#autoId2)*

### Description
Besides that Oracle Update Batching should perform better than plain JDBC batch updates (not tested but the documentation says so), the main advantage is that we can get the number of affected rows for each execution of a `PreparedStatement`. Plain JDBC batch updates just return an array containing -2 (`Statement#SUCCESS_NO_INFO`).

In Spring 3.2.2 there are 5 `batchUpdate()` methods in `JdbcTemplate`:

1. `int[] batchUpdate(String[] sql)`
2. `int[] batchUpdate(String sql, BatchPreparedStatementSetter pss)`
3. `int[] batchUpdate(String sql, List<Object[]> batchArgs)`
4. `int[] batchUpdate(String sql, List<Object[]> batchArgs, int[] argTypes)`
5. `<T> int[][] batchUpdate(String sql, Collection<T> batchArgs, int batchSize, ParameterizedPreparedStatementSetter<T> pss)`

Method (1) works out of the box since it uses `java.sql.Statement` and not `java.sql.PreparedStatement`. Method (4) calls method (2). Method (3) calls method (4) which in turn calls method (2).

So only methods (2) and (5) need to be re-implemented for Oracle Update Batching.

### TODOs / Ideas
* A lot of tests against an Oracle DB (Oracle XE is fine)
  * Correct batch size
  * Tests with pooled `DataSource`s (tomcat-jdbc, commons-dbcp)
  * Tests with `NamedParameterJdbcTemplate`
* Eliminate OJDBC dependency from the POM
  * Separate Maven module for Oracle tests
  * Call Oracle-specific methods using reflection (Java 6) or `MethodHandle`s (Java 7)
* Supplementary methods that return `int` (sum of all affected rows in a batch update) instead of `int[]`
