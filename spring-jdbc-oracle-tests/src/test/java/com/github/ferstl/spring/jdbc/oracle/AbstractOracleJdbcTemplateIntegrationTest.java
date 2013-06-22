package com.github.ferstl.spring.jdbc.oracle;

import org.junit.runner.RunWith;
import org.springframework.test.annotation.IfProfileValue;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Transactional;

/**
 * Abstract base class for integration tests. This class does only set the required annotations for the integration
 * tests.
 */
@ContextConfiguration(classes = DatabaseConfiguration.class)
@TransactionConfiguration
@Transactional
@IfProfileValue(name = "testgroup", value="integration")
@RunWith(SpringJUnit4ClassRunner.class)
public abstract class AbstractOracleJdbcTemplateIntegrationTest {

}
