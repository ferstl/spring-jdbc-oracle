DROP SEQUENCE seq_test_table;
DROP TABLE test_table;

CREATE SEQUENCE seq_test_table MINVALUE 0;
CREATE TABLE test_table (
  id NUMBER(5) NOT NULL PRIMARY KEY,
  val VARCHAR2(50) NOT NULL
);

