/*
 * Copyright (c) 2013 by Stefan Ferstl <st.ferstl@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.ferstl.spring.jdbc.oracle;

import org.junit.Before;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.test.annotation.IfProfileValue;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

/**
 * Abstract base class for integration tests. This class does only set the required annotations for the integration
 * tests.
 */
@ContextConfiguration(classes = DatabaseConfiguration.class)
@Transactional
@IfProfileValue(name = "testgroup", value = "integration")
@RunWith(SpringJUnit4ClassRunner.class)
public abstract class AbstractOracleJdbcTemplateIntegrationTest {

  @Autowired
  Environment env;

  int batchSize;

  @Before
  public final void setupBatchSize() {
    this.batchSize = this.env.getProperty("db.batchsize", Integer.class);
  }
}
