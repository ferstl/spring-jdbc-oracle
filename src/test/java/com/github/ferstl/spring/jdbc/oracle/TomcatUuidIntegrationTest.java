package com.github.ferstl.spring.jdbc.oracle;

import org.springframework.test.context.ActiveProfiles;

import com.github.ferstl.spring.jdbc.oracle.dsconfig.DataSourceProfile;

@ActiveProfiles(DataSourceProfile.TOMCAT_POOL)
public class TomcatUuidIntegrationTest extends AbstractUuidIntegrationTest {

}
