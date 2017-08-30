package org.molgenis.oneclickimporter.service.utils;

import com.google.common.io.Resources;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Sheet;
import org.molgenis.data.excel.service.ExcelService;
import org.molgenis.data.excel.service.ExcelServiceImpl;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;

public class OneClickImporterTestUtils
{
	public static List<Sheet> loadSheetFromFile(Class<?> clazz, String fileName)
			throws IOException, InvalidFormatException, URISyntaxException
	{
		URL resourceUrl = Resources.getResource(clazz, fileName);
		File file = new File(new URI(resourceUrl.toString()).getPath());

		ExcelService excelService = new ExcelServiceImpl();
		return excelService.buildExcelSheetsFromFile(file);
	}

	public static File loadFile(Class<?> clazz, String fileName)
			throws IOException, InvalidFormatException, URISyntaxException
	{
		URL resourceUrl = Resources.getResource(clazz, fileName);
		return new File(new URI(resourceUrl.toString()).getPath());
	}
}
