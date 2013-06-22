-- Drop existing test infrastructure
DROP SEQUENCE seq_test_table;
DROP TABLE test_table;

-- Setup test infrastructure
CREATE SEQUENCE seq_test_table MINVALUE 0;
CREATE TABLE test_table (
  id     NUMBER(5) NOT NULL PRIMARY KEY,
  val    VARCHAR2(50) NOT NULL,
  numval NUMBER(10) NOT NULL
);

