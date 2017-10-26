package org.molgenis.integrationtest.data.postgresql;

import org.molgenis.data.postgresql.PostgreSqlConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import(PostgreSqlConfiguration.class)
@ComponentScan("org.molgenis.data.postgresql")
public class PostgreSqlTestConfig
{
}
