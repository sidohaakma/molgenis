package org.molgenis.data.csv;

import org.molgenis.data.MolgenisDataException;
import org.molgenis.data.csv.service.CsvService;
import org.molgenis.data.csv.service.CsvServiceImpl;
import org.molgenis.util.ResourceUtils;
import org.testng.annotations.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.testng.Assert.assertEquals;

public class CsvServiceTest
{
	private CsvService csvService = new CsvServiceImpl();

	@Test
	public void buildLinesFromFileTest() throws IOException, URISyntaxException, MolgenisDataException
	{
		Map<String, List<String[]>> actual = csvService.buildLinesFromFile(
				ResourceUtils.getFile(getClass(), "/simple-valid.csv"));
		List<String[]> expected = new ArrayList<>();
		expected.add(new String[] { "name", "superpower" });
		expected.add(new String[] { "Mark", "arrow functions" });
		expected.add(new String[] { "Connor", "Oldschool syntax" });
		expected.add(new String[] { "Fleur", "Lambda Magician" });
		expected.add(new String[] { "Dennis", "Root access" });

		Map<String, List<String[]>> keyValueMap = new HashMap<>();
		keyValueMap.put("simple_valid", expected);

		assertEquals(actual.get("simple_valid"), keyValueMap.get("simple_valid"));

	}

	@Test(expectedExceptions = MolgenisDataException.class, expectedExceptionsMessageRegExp = "CSV-file: \\[empty-file.csv\\] is empty")
	public void buildLinesWithEmptyFile() throws IOException, URISyntaxException, MolgenisDataException
	{
		csvService.buildLinesFromFile(ResourceUtils.getFile(getClass(), "/empty-file.csv"));
	}

	@Test(expectedExceptions = MolgenisDataException.class, expectedExceptionsMessageRegExp = "Header was found, but no data is present in file \\[header-without-data.csv\\]")
	public void buildLinesWithHeaderOnly() throws IOException, URISyntaxException, MolgenisDataException
	{
		csvService.buildLinesFromFile(ResourceUtils.getFile(getClass(), "/header-without-data.csv"));
	}

}
