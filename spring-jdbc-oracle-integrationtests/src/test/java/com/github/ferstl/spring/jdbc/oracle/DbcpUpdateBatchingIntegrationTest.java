package com.github.ferstl.spring.jdbc.oracle;

import org.springframework.test.context.ActiveProfiles;

import com.github.ferstl.spring.jdbc.oracle.dsconfig.DataSourceProfile;

@ActiveProfiles(DataSourceProfile.COMMONS_DBCP)
public class DbcpUpdateBatchingIntegrationTest extends AbstractUpdateBatchingIntegrationTest {

}
