
ALTER SESSION SET CONTAINER = ORCLPDB1;
ALTER SESSION SET CURRENT_SCHEMA = spring_jdbc_oracle;

CREATE OR REPLACE PACKAGE spring_jdbc_oracle AS
   PROCEDURE reset_seq_test_table;
END spring_jdbc_oracle; 
/

CREATE OR REPLACE PACKAGE BODY spring_jdbc_oracle AS

   PROCEDURE reset_seq_test_table AS
       l_currval NUMBER(5);
   BEGIN
       SELECT seq_test_table.nextval
         INTO l_currval
         FROM dual;
 
       EXECUTE IMMEDIATE 'ALTER SEQUENCE seq_test_table INCREMENT BY -' || (l_currval + 1);

       SELECT seq_test_table.nextval
         INTO l_currval
         FROM dual;
 
       EXECUTE IMMEDIATE 'ALTER SEQUENCE seq_test_table INCREMENT BY 1';
   END;

END spring_jdbc_oracle; 
/

