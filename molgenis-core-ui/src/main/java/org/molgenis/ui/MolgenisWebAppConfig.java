package org.molgenis.ui;

import com.google.common.collect.Maps;
import freemarker.template.Configuration;
import freemarker.template.TemplateException;
import org.molgenis.data.DataService;
import org.molgenis.data.convert.StringToDateConverter;
import org.molgenis.data.convert.StringToDateTimeConverter;
import org.molgenis.data.i18n.LanguageService;
import org.molgenis.data.i18n.PropertiesMessageSource;
import org.molgenis.data.platform.config.PlatformConfig;
import org.molgenis.data.settings.AppSettings;
import org.molgenis.file.FileStore;
import org.molgenis.messageconverter.CsvHttpMessageConverter;
import org.molgenis.security.CorsInterceptor;
import org.molgenis.security.core.PermissionService;
import org.molgenis.security.freemarker.HasPermissionDirective;
import org.molgenis.security.freemarker.NotHasPermissionDirective;
import org.molgenis.security.settings.AuthenticationSettings;
import org.molgenis.ui.converter.RdfConverter;
import org.molgenis.ui.freemarker.LimitMethod;
import org.molgenis.ui.freemarker.MolgenisFreemarkerObjectWrapper;
import org.molgenis.ui.menu.MenuMolgenisUi;
import org.molgenis.ui.menu.MenuReaderService;
import org.molgenis.ui.menu.MenuReaderServiceImpl;
import org.molgenis.ui.menumanager.MenuManagerService;
import org.molgenis.ui.menumanager.MenuManagerServiceImpl;
import org.molgenis.ui.security.MolgenisUiPermissionDecorator;
import org.molgenis.ui.style.StyleService;
import org.molgenis.ui.style.ThemeFingerprintRegistry;
import org.molgenis.util.ApplicationContextProvider;
import org.molgenis.util.GsonHttpMessageConverter;
import org.molgenis.util.ResourceFingerprintRegistry;
import org.molgenis.util.TemplateResourceUtils;
import org.molgenis.web.PluginController;
import org.molgenis.web.PluginInterceptor;
import org.molgenis.web.Ui;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.PropertySources;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.format.FormatterRegistry;
import org.springframework.http.converter.BufferedImageHttpMessageConverter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.ResourceHttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.web.multipart.MultipartResolver;
import org.springframework.web.multipart.support.StandardServletMultipartResolver;
import org.springframework.web.servlet.ViewResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.PathMatchConfigurer;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import org.springframework.web.servlet.handler.MappedInterceptor;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfigurer;
import org.springframework.web.servlet.view.freemarker.FreeMarkerViewResolver;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import static freemarker.template.Configuration.VERSION_2_3_23;
import static java.lang.String.format;
import static org.apache.commons.compress.utils.CharsetNames.UTF_8;
import static org.molgenis.framework.ui.ResourcePathPatterns.*;
import static org.molgenis.security.UriConstants.PATH_SEGMENT_APPS;
import static org.molgenis.ui.FileStoreConstants.FILE_STORE_PLUGIN_APPS_PATH;

@Import({ PlatformConfig.class, RdfConverter.class })
@PropertySources({
		@PropertySource(ignoreResourceNotFound = true, encoding = UTF_8, value = "file:${molgenis.home:\\/srv\\/molgenis\\/.molgenis}/molgenis-server.properties"),
		@PropertySource(ignoreResourceNotFound = true, encoding = UTF_8, value = "classpath:molgenis-server.properties") })
public abstract class MolgenisWebAppConfig extends WebMvcConfigurerAdapter
{
	private static final String MOLGENIS_HOME = "molgenis.home";
	private static final String DEFAULT_MOLGENIS_HOME = "/srv/molgenis/.molgenis";
	private static final String MOLGENIS_PROPERTIES = "molgenis-server.properties";

	@Autowired
	private DataService dataService;

	@Autowired
	private AppSettings appSettings;

	@Autowired
	private AuthenticationSettings authenticationSettings;

	@Autowired
	private PermissionService permissionService;

	@Autowired
	private GsonHttpMessageConverter gsonHttpMessageConverter;

	@Autowired
	private RdfConverter rdfConverter;

	@Autowired
	private LanguageService languageService;

	@Autowired
	private StyleService styleService;

	@Override
	public void addResourceHandlers(ResourceHandlerRegistry registry)
	{
		int cachePeriod;
		if (environment.equals("development"))
		{
			cachePeriod = 0;
		}
		else
		{
			cachePeriod = 31536000; // a year
		}
		registry.addResourceHandler(PATTERN_CSS)
				.addResourceLocations("/css/", "classpath:/css/")
				.setCachePeriod(cachePeriod);
		registry.addResourceHandler(PATTERN_IMG)
				.addResourceLocations("/img/", "classpath:/img/")
				.setCachePeriod(cachePeriod);
		registry.addResourceHandler(PATTERN_JS)
				.addResourceLocations("/js/", "classpath:/js/")
				.setCachePeriod(cachePeriod);
		registry.addResourceHandler(PATTERN_FONTS)
				.addResourceLocations("/fonts/", "classpath:/fonts/")
				.setCachePeriod(cachePeriod);
		registry.addResourceHandler("/generated-doc/**").addResourceLocations("/generated-doc/").setCachePeriod(3600);
		registry.addResourceHandler("/html/**").addResourceLocations("/html/", "classpath:/html/").setCachePeriod(3600);

		// Add resource handler for apps
		FileStore fileStore = fileStore();
		registry.addResourceHandler("/" + PATH_SEGMENT_APPS + "/**")
				.addResourceLocations("file:///" + fileStore.getStorageDir() + '/' + FILE_STORE_PLUGIN_APPS_PATH + '/');
		registry.addResourceHandler("/webjars/**")
				.addResourceLocations("classpath:/META-INF/resources/webjars/")
				.setCachePeriod(3600)
				.resourceChain(true);
		// see https://github.com/spring-projects/spring-boot/issues/4403 for why the resourceChain needs to be explicitly added.
	}

	@Value("${environment:production}")
	private String environment;

	@Override
	public void configureMessageConverters(List<HttpMessageConverter<?>> converters)
	{
		converters.add(gsonHttpMessageConverter);
		converters.add(new BufferedImageHttpMessageConverter());
		converters.add(new CsvHttpMessageConverter());
		converters.add(new ResourceHttpMessageConverter());
		converters.add(new StringHttpMessageConverter());
		converters.add(rdfConverter);
	}

	@Override
	public void configurePathMatch(PathMatchConfigurer configurer)
	{
		// Fix for https://github.com/molgenis/molgenis/issues/5431
		configurer.setUseRegisteredSuffixPatternMatch(true);
	}

	@Bean
	public MappedInterceptor mappedCorsInterceptor()
	{
		/*
		 * This way, the cors interceptor is added to the resource handlers as well, if the patterns overlap.
		 *
		 * See https://jira.spring.io/browse/SPR-10655
		 */
		return new MappedInterceptor(new String[] { "/api/**", "/fdp/**" }, corsInterceptor());
	}

	@Override
	public void addInterceptors(InterceptorRegistry registry)
	{
		String pluginInterceptPattern = PluginController.PLUGIN_URI_PREFIX + "**";
		registry.addInterceptor(molgenisInterceptor());
		registry.addInterceptor(molgenisPluginInterceptor()).addPathPatterns(pluginInterceptPattern);
	}

	@Override
	public void addFormatters(FormatterRegistry registry)
	{
		registry.addConverter(new StringToDateTimeConverter());
		registry.addConverter(new StringToDateConverter());
	}

	@Bean
	public ResourceFingerprintRegistry resourceFingerprintRegistry()
	{
		return new ResourceFingerprintRegistry();
	}

	@Bean
	public ThemeFingerprintRegistry themeFingerprintRegistry()
	{
		return new ThemeFingerprintRegistry(styleService);
	}

	@Bean
	public TemplateResourceUtils templateResourceUtils()
	{
		return new TemplateResourceUtils();
	}

	@Bean
	public MolgenisInterceptor molgenisInterceptor()
	{
		return new MolgenisInterceptor(resourceFingerprintRegistry(), themeFingerprintRegistry(),
				templateResourceUtils(), appSettings, authenticationSettings, languageService, environment);
	}

	@Bean
	public PropertiesMessageSource formMessageSource()
	{
		return new PropertiesMessageSource("form");
	}

	@Bean
	public PropertiesMessageSource dataexplorerMessageSource()
	{
		return new PropertiesMessageSource("dataexplorer");
	}

	@Bean
	public PluginInterceptor molgenisPluginInterceptor()
	{
		return new PluginInterceptor(molgenisUi(), permissionService);
	}

	@Bean
	public static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer()
	{
		return new PropertySourcesPlaceholderConfigurer();
	}

	//	@Bean
	//	public static PropertySourcesPlaceholderConfigurer properties()
	//	{
	//		PropertySourcesPlaceholderConfigurer pspc = new PropertySourcesPlaceholderConfigurer();
	//		Resource[] resources = new Resource[] {
	//				new FileSystemResource(System.getProperty(MOLGENIS_HOME) + File.separator + MOLGENIS_PROPERTIES),
	//				new ClassPathResource(File.separator + MOLGENIS_PROPERTIES) };
	//		pspc.setLocations(resources);
	//		pspc.setFileEncoding(UTF_8);
	//		pspc.setIgnoreUnresolvablePlaceholders(true);
	//		pspc.setIgnoreResourceNotFound(true);
	//		pspc.setNullValue("@null");
	//		return pspc;
	//	}

	@Bean
	public FileStore fileStore()
	{
		// get molgenis home directory
		String molgenisHomeDir = System.getProperty(MOLGENIS_HOME, DEFAULT_MOLGENIS_HOME);
		if (molgenisHomeDir == null)
		{
			throw new IllegalArgumentException(format("Missing required JAVA system property [%s]", MOLGENIS_HOME));
		}
		if (!molgenisHomeDir.endsWith(File.separator)) molgenisHomeDir = molgenisHomeDir + File.separator;

		// create molgenis store directory in molgenis data directory if not exists
		String molgenisFileStoreDirStr = molgenisHomeDir + "data" + File.separator + "filestore";
		File molgenisDataDir = new File(molgenisFileStoreDirStr);
		if (!molgenisDataDir.exists())
		{
			if (!molgenisDataDir.mkdirs())
			{
				throw new RuntimeException(format("Failed to create directory: [%s]", molgenisFileStoreDirStr));
			}
		}

		return new FileStore(molgenisFileStoreDirStr);
	}

	/**
	 * Bean that allows referencing Spring managed beans from Java code which is not managed by Spring
	 */
	@Bean
	public ApplicationContextProvider applicationContextProvider()
	{
		return new ApplicationContextProvider();
	}

	/**
	 * Enable spring freemarker viewresolver. All freemarker template names should end with '.ftl'
	 */
	@Bean
	public ViewResolver viewResolver()
	{
		FreeMarkerViewResolver resolver = new FreeMarkerViewResolver();
		resolver.setCache(true);
		resolver.setSuffix(".ftl");
		resolver.setContentType("text/html;charset=UTF-8");
		return resolver;
	}

	/**
	 * Configure freemarker. All freemarker templates should be on the classpath in a package called 'freemarker'
	 */
	@Bean
	public FreeMarkerConfigurer freeMarkerConfigurer() throws IOException, TemplateException
	{
		FreeMarkerConfigurer result = new FreeMarkerConfigurer()
		{
			@Override
			protected void postProcessConfiguration(Configuration config) throws IOException, TemplateException
			{
				config.setObjectWrapper(new MolgenisFreemarkerObjectWrapper(VERSION_2_3_23));
			}
		};
		result.setPreferFileSystemAccess(false);
		result.setTemplateLoaderPath("classpath:/templates/");
		result.setDefaultEncoding("UTF-8");
		Properties freemarkerSettings = new Properties();
		freemarkerSettings.setProperty(Configuration.LOCALIZED_LOOKUP_KEY, Boolean.FALSE.toString());
		result.setFreemarkerSettings(freemarkerSettings);
		Map<String, Object> freemarkerVariables = Maps.newHashMap();
		freemarkerVariables.put("limit", new LimitMethod());
		freemarkerVariables.put("hasPermission", new HasPermissionDirective(permissionService));
		freemarkerVariables.put("notHasPermission", new NotHasPermissionDirective(permissionService));
		addFreemarkerVariables(freemarkerVariables);

		result.setFreemarkerVariables(freemarkerVariables);

		return result;
	}

	// Override in subclass if you need more freemarker variables
	protected void addFreemarkerVariables(Map<String, Object> freemarkerVariables)
	{

	}

	@Bean
	public MultipartResolver multipartResolver()
	{
		return new StandardServletMultipartResolver();
	}

	@Bean
	public MenuReaderService menuReaderService()
	{
		return new MenuReaderServiceImpl(appSettings);
	}

	@Bean
	public MenuManagerService menuManagerService()
	{
		return new MenuManagerServiceImpl(menuReaderService(), appSettings, dataService);
	}

	@Bean
	public Ui molgenisUi()
	{
		Ui molgenisUi = new MenuMolgenisUi(menuReaderService());
		return new MolgenisUiPermissionDecorator(molgenisUi, permissionService);
	}

	@Bean
	public CorsInterceptor corsInterceptor()
	{
		return new CorsInterceptor();
	}

	@PostConstruct
	public void validateMolgenisServerProperties()
	{
		// validate properties defined in molgenis-server.properties
		String path = System.getProperty(MOLGENIS_HOME, DEFAULT_MOLGENIS_HOME) + File.separator + MOLGENIS_PROPERTIES;
		if (environment == null)
		{
			throw new RuntimeException(
					format("Missing required property 'environment' in [%s] allowed values are [development, production].",
							path));
		}
		else if (!environment.equals("development") && !environment.equals("production"))
		{
			throw new RuntimeException(
					format("Invalid value [{0}] for property 'environment' in [{1}], allowed values are [development, production].",
							environment, path));
		}
	}
}
