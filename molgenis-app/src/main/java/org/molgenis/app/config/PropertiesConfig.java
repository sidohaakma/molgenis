package org.molgenis.app.config;

import org.molgenis.core.ui.MolgenisWebAppConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.PropertySources;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.env.Environment;

@Configuration
@PropertySources({ @PropertySource(value = "file:${" + MolgenisWebAppConfig.MOLGENIS_HOME
		+ "}/molgenis-server.properties", ignoreResourceNotFound = true, encoding = "UTF-8"),
		@PropertySource(value = "classpath:/molgenis.properties", ignoreResourceNotFound = true, encoding = "UTF-8") })
public class PropertiesConfig
{

	@Autowired
	private Environment env;

	@Bean
	public PropertySourcesPlaceholderConfigurer properties()
	{
		return new PropertySourcesPlaceholderConfigurer();
	}

}
