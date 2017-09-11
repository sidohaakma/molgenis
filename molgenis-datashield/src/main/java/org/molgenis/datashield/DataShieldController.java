package org.molgenis.datashield;

import com.google.common.collect.ImmutableMap;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static java.lang.String.format;
import static org.molgenis.datashield.DataShieldController.URI;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.web.bind.annotation.RequestMethod.*;

/**
 * <p>Returns the molgenis DataSHIELD interface</p>
 * <p>
 * <ul>It only supports the following methods
 * <li>ds.mean</li>
 * </ul>
 */
@Controller
@RequestMapping(URI)
public class DataShieldController
{

	private static final Logger LOG = LoggerFactory.getLogger(DataShieldController.class);

	public static final String URI = "/datashield/ws";
	private static final String DATASOURCE_URI = "/datasource";
	private static final String TABLE_URI = "/table";
	private static final String SESSIONS_URI = "/datashield/sessions";
	private static final String SESSION_URI = "/datashield/session";
	private static final String SYMBOL_URI = "/symbol";

	private static final String VIEW_DATASHIELD = "view-datashield";

	@RequestMapping(value = DATASOURCE_URI + "/{projectId}" + TABLE_URI
			+ "/{tableId}", method = GET, produces = APPLICATION_JSON_VALUE)
	@ResponseBody
	public Map<String, Object> getTableDataByProjectId(HttpServletRequest request,
			@PathVariable("projectId") String projectId, @PathVariable("tableId") String tableId)
	{

		if (projectId != null && tableId != null)
		{
			LOG.info(format("DataSHIELD --> getTableDataByProjectId [%s] / [%s]", projectId, tableId));
		}

		// Has to return an R Opal object. Need to look at endpoint for in opal-datashield module.
		// curl -k  -L
		//
		// -H 'Authorization: X-Opal-Auth RkROX3VzZXI6RkROX3VzZXI='
		// -H 'Accept: application/json'
		//
		// 'http://opal1.haakma.org/ws/datasource/TestProject/table/PATIENTS'

		//		{
		//			"name":"PATIENTS", "entityType":"Patient", "link":"/datasource/TestProject/table/PATIENTS", "datasourceName":
		//			"TestProject", "timestamps":{
		//			"created":"2017-09-07T13:57:34.000+02", "lastUpdate":"2017-09-07T14:53:37.000+02"
		//		}
		//		}

		return ImmutableMap.of("name", "PATIENTS", "entityType", "Patient", "link",
				"/datasource/TestProject/table/PATIENTS", "datasourceName", "TestProject", "timestamps",
				ImmutableMap.of("created", "2017-09-07T13:57:34.000+02", "lastUpdate", "2017-09-07T14:53:37.000+02"));
	}

	@RequestMapping(value = SESSIONS_URI, method = POST, produces = APPLICATION_JSON_VALUE)
	@ResponseBody()
	public Map<String, Object> createSession(HttpServletRequest request, HttpServletResponse response)
	{
		LOG.info("DataSHIELD --> createSession");

		response.addHeader("X-Opal-Version", "2.7.0");

		// Has to return an R Opal object. Need to look at endpoint for in opal-datashield module.
		// Authorization header : Authroization --> X-Opal-Auth RkROX3VzZXI6RkROX3VzZXI=
		//
		//		{
		//			"id":"acbbfb05-30a1-429f-8e6a-3e7c33c6cbd7", "user":"FDN_user", "creationDate":
		//			"2017-09-08T15:36:08.911+02", "lastAccessDate":"2017-09-08T15:36:08.990+02", "status":"WAITING", "link":
		//			"/r/session/acbbfb05-30a1-429f-8e6a-3e7c33c6cbd7", "context":"DataSHIELD"
		//		}

		Map<String, Object> responseBody = new HashMap<>();
		responseBody.put("id", "acbbfb05-30a1-429f-8e6a-3e7c33c6cbd7");
		responseBody.put("user", "FDN_user");
		responseBody.put("creationDate", "2017-09-08T15:36:08.911+02");
		responseBody.put("lastAccessDate", "2017-09-08T15:36:08.990+02");
		responseBody.put("status", "WAITING");
		responseBody.put("link", "/r/session/acbbfb05-30a1-429f-8e6a-3e7c33c6cbd7");
		responseBody.put("context", "DataSHIELD");
		return responseBody;
	}

	@RequestMapping(value = SESSION_URI + "/{sessionId}" + SYMBOL_URI
			+ "/{name}", method = PUT, produces = APPLICATION_JSON_VALUE)
	@ResponseBody
	public Map<String, Object> getSymbol(HttpServletRequest request, @PathVariable("sessionId") String sessionId,
			@PathVariable("name") String name) throws IOException

	{
		LOG.info("DataSHIELD --> getSymbol");

		IOUtils.copy(request.getInputStream(), System.out);

		// Has to return an R Opal object. Need to look at endpoint for in opal-datashield module.
		// Authorization header : Authroization --> X-Opal-Auth RkROX3VzZXI6RkROX3VzZXI=
		//
		//		{
		//			"id":"acbbfb05-30a1-429f-8e6a-3e7c33c6cbd7", "user":"FDN_user", "creationDate":
		//			"2017-09-08T15:36:08.911+02", "lastAccessDate":"2017-09-08T15:36:08.990+02", "status":"WAITING", "link":
		//			"/r/session/acbbfb05-30a1-429f-8e6a-3e7c33c6cbd7", "context":"DataSHIELD"
		//		}

		Map<String, Object> response = new HashMap<>();
		response.put("id", sessionId);
		response.put("user", "FDN_user");
		response.put("creationDate", "2017-09-08T15:36:08.911+02");
		response.put("lastAccessDate", "2017-09-08T15:36:08.990+02");
		response.put("status", "WAITING");
		response.put("link", "/r/session/acbbfb05-30a1-429f-8e6a-3e7c33c6cbd7");
		response.put("context", "DataSHIELD");

		return response;
	}

}

