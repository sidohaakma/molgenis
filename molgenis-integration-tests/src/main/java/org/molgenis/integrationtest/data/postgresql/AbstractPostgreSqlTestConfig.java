package org.molgenis.integrationtest.data.postgresql;

import com.mchange.v2.c3p0.ComboPooledDataSource;
import org.molgenis.data.DataService;
import org.molgenis.data.ManageableRepositoryCollection;
import org.molgenis.data.postgresql.PostgreSqlConfiguration;
import org.molgenis.data.postgresql.PostgreSqlEntityFactory;
import org.molgenis.data.postgresql.PostgreSqlRepository;
import org.molgenis.data.postgresql.PostgreSqlRepositoryCollection;
import org.molgenis.integrationtest.data.AbstractDataApiTestConfig;
import org.molgenis.util.ResourceUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

@Import(
{ PostgreSqlEntityFactory.class, PostgreSqlConfiguration.class })
public abstract class AbstractPostgreSqlTestConfig extends AbstractDataApiTestConfig
{
	private static final Logger LOG = LoggerFactory.getLogger(AbstractPostgreSqlTestConfig.class);

	@Autowired
	DataService dataService;

	@Autowired
	PostgreSqlEntityFactory postgreSqlEntityFactory;

	@Autowired
	JdbcTemplate jdbcTemplate;

	@Override
	protected ManageableRepositoryCollection getBackend()
	{
		return new PostgreSqlRepositoryCollection(dataSource)
		{
			@Override
			protected PostgreSqlRepository createPostgreSqlRepository()
			{
				return new PostgreSqlRepository(postgreSqlEntityFactory, jdbcTemplate);
			}

			@Override
			public boolean hasRepository(String name)
			{
				throw new UnsupportedOperationException();
			}
		};
	}

	@Override
	public void setUp()
	{
		try
		{
			Connection conn = getConnection();

			Statement statement = conn.createStatement();
			statement.executeUpdate("DROP DATABASE IF EXISTS \"molgenisIntegrationTest\"");
			statement.executeUpdate("CREATE DATABASE \"molgenisIntegrationTest\"");

			conn.close();
		}
		catch (Exception e)
		{
			throw new RuntimeException(e.getMessage());
		}
	}

	private Connection getConnection() throws IOException, SQLException
	{
		Properties properties = new Properties();
		File file = ResourceUtils.getFile(getClass(), "/postgresql/molgenis.properties");
		properties.load(new FileInputStream(file));

		String db_uri = properties.getProperty("db_uri");
		int slashIndex = db_uri.lastIndexOf('/');

		// remove the, not yet created, database from the connection url
		return DriverManager.getConnection(db_uri.substring(0, slashIndex + 1), properties.getProperty("db_user"),
				properties.getProperty("db_password"));
	}

	@PostConstruct
	public void init()
	{
		super.init();
	}

	@PreDestroy
	public void cleanup()
	{
		try
		{
			((ComboPooledDataSource) dataSource).close();

			Connection conn = getConnection();
			Statement statement = conn.createStatement();
			statement.executeUpdate("DROP database if exists \"molgenisIntegrationTest\"");
			conn.close();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	@Bean
	public static PropertySourcesPlaceholderConfigurer properties()
	{
		PropertySourcesPlaceholderConfigurer pspc = new PropertySourcesPlaceholderConfigurer();
		Resource[] resources = new Resource[]
		{ new FileSystemResource(System.getProperty("molgenis.home") + "/molgenis-server.properties"),
				new ClassPathResource("/postgresql/molgenis.properties") };
		pspc.setLocations(resources);
		pspc.setFileEncoding("UTF-8");
		pspc.setIgnoreUnresolvablePlaceholders(true);
		pspc.setIgnoreResourceNotFound(true);
		pspc.setNullValue("@null");
		return pspc;
	}
}
