
ALTER SESSION SET CONTAINER = ORCLPDB1;
ALTER SESSION SET CURRENT_SCHEMA = spring_jdbc_oracle;

CREATE SEQUENCE seq_test_table
     START WITH 0
--     -1 required for resetting the sequence
       MINVALUE -1
       MAXVALUE 99999;
