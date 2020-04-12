-- Delete existing data

DELETE FROM test_table;

-- Reset the sequence

CALL spring_jdbc_oracle.reset_seq_test_table();