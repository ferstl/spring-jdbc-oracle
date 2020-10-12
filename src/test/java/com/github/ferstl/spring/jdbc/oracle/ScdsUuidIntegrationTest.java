package com.github.ferstl.spring.jdbc.oracle;

import org.springframework.test.context.ActiveProfiles;

import com.github.ferstl.spring.jdbc.oracle.dsconfig.DataSourceProfile;

@ActiveProfiles(DataSourceProfile.SINGLE_CONNECTION)
public class ScdsUuidIntegrationTest extends AbstractUuidIntegrationTest {

}
