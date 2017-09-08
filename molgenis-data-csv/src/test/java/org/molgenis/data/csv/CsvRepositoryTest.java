package org.molgenis.data.csv;

import com.google.common.collect.Lists;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.molgenis.data.AbstractMolgenisSpringTest;
import org.molgenis.data.Entity;
import org.molgenis.data.MolgenisDataException;
import org.molgenis.data.csv.service.CsvService;
import org.molgenis.data.csv.service.CsvServiceImpl;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.AttributeFactory;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.meta.model.EntityTypeFactory;
import org.molgenis.data.processor.TrimProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.FileCopyUtils;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static org.molgenis.data.meta.AttributeType.STRING;
import static org.testng.Assert.*;

public class CsvRepositoryTest extends AbstractMolgenisSpringTest
{

	private CsvService csvService = new CsvServiceImpl();

	@Autowired
	private EntityTypeFactory entityTypeFactory;

	@Autowired
	private AttributeFactory attributeFactory;

	private static File test;
	private static File testdata;
	private static File novalues;
	private static File emptyvalues;
	private static File testtsv;
	private static File emptylines;
	private static File emptylinessinglecol;

	@BeforeClass
	public static void beforeClass() throws IOException
	{
		InputStream in = CsvRepositoryTest.class.getResourceAsStream("/test.csv");
		test = new File(FileUtils.getTempDirectory(), "test.csv");
		FileCopyUtils.copy(in, new FileOutputStream(test));

		in = CsvRepositoryTest.class.getResourceAsStream("/testdata.csv");
		testdata = new File(FileUtils.getTempDirectory(), "testdata.csv");
		FileCopyUtils.copy(in, new FileOutputStream(testdata));

		in = CsvRepositoryTest.class.getResourceAsStream("/novalues.csv");
		novalues = new File(FileUtils.getTempDirectory(), "novalues.csv");
		FileCopyUtils.copy(in, new FileOutputStream(novalues));

		in = CsvRepositoryTest.class.getResourceAsStream("/emptyvalues.csv");
		emptyvalues = new File(FileUtils.getTempDirectory(), "emptyvalues.csv");
		FileCopyUtils.copy(in, new FileOutputStream(emptyvalues));

		in = CsvRepositoryTest.class.getResourceAsStream("/test.tsv");
		testtsv = new File(FileUtils.getTempDirectory(), "test.tsv");
		FileCopyUtils.copy(in, new FileOutputStream(testtsv));

		in = CsvRepositoryTest.class.getResourceAsStream("/emptylines.csv");
		emptylines = new File(FileUtils.getTempDirectory(), "emptylines.csv");
		FileCopyUtils.copy(in, new FileOutputStream(emptylines));

		in = CsvRepositoryTest.class.getResourceAsStream("/emptylinessinglecol.csv");
		emptylinessinglecol = new File(FileUtils.getTempDirectory(), "emptylinessinglecol.csv");
		FileCopyUtils.copy(in, new FileOutputStream(emptylinessinglecol));
	}

	@Test
	public void metaData() throws IOException
	{
		CsvRepository csvRepository = null;
		try
		{
			Map<String, List<String[]>> lines = csvService.buildLinesFromFile(testdata);
			List<String> columns = csvService.parseHeader(lines, "testdata", Lists.newArrayList(new TrimProcessor()));
			csvRepository = new CsvRepository(lines, columns, "testdata", getEntityType("testdata", columns),
					Lists.newArrayList(new TrimProcessor()));
			assertEquals(csvRepository.getName(), "testdata");
			Iterator<Attribute> it = csvRepository.getEntityType().getAttributes().iterator();
			assertTrue(it.hasNext());
			assertEquals(it.next().getName(), "col1");
			assertTrue(it.hasNext());
			assertEquals(it.next().getName(), "col2");
			assertFalse(it.hasNext());
		}
		finally
		{
			IOUtils.closeQuietly(csvRepository);
		}
	}

	/**
	 * Test based on au.com.bytecode.opencsv.CSVReaderTest
	 */
	@Test
	public void iterator() throws IOException
	{
		CsvRepository csvRepository = null;
		try
		{
			Map<String, List<String[]>> lines = csvService.buildLinesFromFile(testdata);
			List<String> columns = csvService.parseHeader(lines, "testdata", Lists.newArrayList(new TrimProcessor()));

			csvRepository = new CsvRepository(lines, columns, "testdata", getEntityType("testdata", columns),
					Lists.newArrayList(new TrimProcessor()));
			Iterator<Entity> it = csvRepository.iterator();

			assertTrue(it.hasNext());
			Entity entity = it.next();
			assertEquals(entity.get("col1"), "val1");
			assertEquals(entity.get("col2"), "val2");

			assertTrue(it.hasNext());
			entity = it.next();
			assertEquals(entity.get("col1"), "a,a");
			assertEquals(entity.get("col2"), "b");
			assertTrue(it.hasNext());

			assertTrue(it.hasNext());
			entity = it.next();
			assertNull(entity.get("col1"));
			assertEquals(entity.get("col2"), "a");

			assertTrue(it.hasNext());
			entity = it.next();
			assertEquals(entity.get("col1"), "\"");
			assertEquals(entity.get("col2"), "\"\"");

			assertTrue(it.hasNext());
			entity = it.next();
			assertEquals(entity.get("col1"), ",");
			assertEquals(entity.get("col2"), ",,");

			assertFalse(it.hasNext());
		}
		finally
		{
			IOUtils.closeQuietly(csvRepository);
		}
	}

	@Test(expectedExceptions = MolgenisDataException.class, expectedExceptionsMessageRegExp = "Header was found, but no data is present in file \\[novalues\\.csv\\]")
	public void testIteratorNoValues() throws IOException
	{
		Map<String, List<String[]>> lines = csvService.buildLinesFromFile(novalues);
		List<String> columns = csvService.parseHeader(lines, "novalues", Lists.newArrayList(new TrimProcessor()));
		CsvRepository csvRepository = new CsvRepository(lines, columns, "novalues", getEntityType("novalues", columns),
				Lists.newArrayList(new TrimProcessor()));
		Iterator<Entity> it = csvRepository.iterator();
		assertFalse(it.hasNext());

	}

	@Test
	public void testIteratorEmptyValues() throws IOException
	{
		Map<String, List<String[]>> lines = csvService.buildLinesFromFile(emptylines);
		List<String> columns = csvService.parseHeader(lines, "emptyvalues", Lists.newArrayList(new TrimProcessor()));
		CsvRepository csvRepository = new CsvRepository(lines, columns, "emptyvalues",
				getEntityType("emptyvalues", columns), Lists.newArrayList(new TrimProcessor()));
		Iterator<Entity> it = csvRepository.iterator();
		assertTrue(it.hasNext());
		assertNull(it.next().get("col1"));
	}

	@Test
	public void testIteratorTsv() throws IOException
	{
		Map<String, List<String[]>> lines = csvService.buildLinesFromFile(testtsv);
		List<String> columns = csvService.parseHeader(lines, "testtsv", Lists.newArrayList(new TrimProcessor()));
		CsvRepository tsvRepository = new CsvRepository(lines, columns, "testtsv", getEntityType("testtsv", columns),
				Lists.newArrayList(new TrimProcessor()));

		Iterator<Entity> it = tsvRepository.iterator();
		Entity entity = it.next();
		assertEquals(entity.get("col1"), "val1");
		assertEquals(entity.get("col2"), "val2");
		assertFalse(it.hasNext());

	}

	@Test(expectedExceptions = MolgenisDataException.class, expectedExceptionsMessageRegExp = "Column count \\[1\\] is not greater or equal then header count \\[2\\] \\(in CSV-file \\[emptylines\\.csv\\]\\)")
	public void testIteratorEmptyLines() throws IOException
	{
		Map<String, List<String[]>> lines = csvService.buildLinesFromFile(emptylines);
		List<String> columns = csvService.parseHeader(lines, "emptyLines", Lists.newArrayList(new TrimProcessor()));
		CsvRepository csvRepository = new CsvRepository(lines, columns, "emptyLines",
				getEntityType("emptyLines", columns), Lists.newArrayList(new TrimProcessor()));
		Iterator<Entity> it = csvRepository.iterator();
		Entity entity = it.next();
		assertEquals(entity.get("col1"), "val1");
		assertFalse(it.hasNext());
	}

	@Test
	public void testIteratorEmptyLinesMissingColumns() throws IOException
	{
		Map<String, List<String[]>> lines = csvService.buildLinesFromFile(emptylinessinglecol);
		List<String> columns = csvService.parseHeader(lines, "emptylinessinglecol",
				Lists.newArrayList(new TrimProcessor()));
		CsvRepository csvRepository = new CsvRepository(lines, columns, "emptylinessinglecol",
				getEntityType("emptylinessinglecol", columns), Lists.newArrayList(new TrimProcessor()));
		Iterator<Entity> it = csvRepository.iterator();
		Entity entity = it.next();
		assertEquals(entity.get("col1"), "val1");

		assertTrue(it.hasNext());
		entity = it.next();
		assertNull(entity.get("col1"));

		assertFalse(it.hasNext());
	}

	private EntityType getEntityType(String repositoryName, List<String> columns)
	{
		EntityType entityType = entityTypeFactory.create(repositoryName).setLabel(repositoryName);

		columns.stream()
			   .map(column -> attributeFactory.create().setName(column).setDataType(STRING))
			   .forEach(entityType::addAttribute);

		return entityType;
	}
}

