package org.molgenis.data.excel;

import com.google.common.collect.Lists;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Sheet;
import org.molgenis.data.AbstractMolgenisSpringTest;
import org.molgenis.data.Entity;
import org.molgenis.data.MolgenisDataException;
import org.molgenis.data.excel.service.ExcelService;
import org.molgenis.data.excel.service.ExcelServiceImpl;
import org.molgenis.data.meta.AttributeType;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.AttributeFactory;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.meta.model.EntityTypeFactory;
import org.molgenis.data.processor.CellProcessor;
import org.molgenis.data.processor.TrimProcessor;
import org.molgenis.util.ResourceUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import static org.molgenis.data.meta.AttributeType.STRING;
import static org.testng.Assert.*;

public class ExcelRepositoryTest extends AbstractMolgenisSpringTest
{
	@Autowired
	private EntityTypeFactory entityTypeFactory;

	@Autowired
	private AttributeFactory attributeFactory;

	private ExcelRepository excelRepository;

	private List<CellProcessor> processors = Lists.newArrayList(new TrimProcessor());

	private ExcelService excelService = new ExcelServiceImpl();

	@BeforeMethod
	public void beforeMethod() throws InvalidFormatException, IOException
	{
		File file = ResourceUtils.getFile(getClass(), "/test.xls");
		Sheet sheet = excelService.buildExcelSheetFromFile(file, "test");
		List<String> columns = excelService.parseHeader(sheet, processors);
		EntityType entityType = getEntityType("test", columns);
		excelRepository = new ExcelRepository(excelService, sheet, columns, entityType, processors);
	}

	private EntityType getEntityType(String repositoryName, List<String> columns)
	{
		EntityType entityType = entityTypeFactory.create(repositoryName).setLabel(repositoryName);

		columns.stream()
			   .map(column -> attributeFactory.create().setName(column).setDataType(STRING))
			   .forEach(entityType::addAttribute);

		return entityType;
	}

	@SuppressWarnings("resource")
	@Test(expectedExceptions = MolgenisDataException.class)

	public void testExcelRepositoryConstructor()
	{
		File file = ResourceUtils.getFile(getClass(), "/test.xls");
		Sheet sheet = excelService.buildExcelSheetFromFile(file, "test_mergedcells");
		List<String> columns = excelService.parseHeader(sheet, processors);
		ExcelRepository repository = new ExcelRepository(excelService, sheet, columns, null, processors);
		repository.iterator();
	}

	@Test
	public void testGetAttribute()
	{
		Attribute attr = excelRepository.getEntityType().getAttribute("col1");
		assertNotNull(attr);
		assertEquals(attr.getDataType(), AttributeType.STRING);
		assertEquals(attr.getName(), "col1");
	}

	@Test
	public void testGetAttributes()
	{
		Iterator<Attribute> it = excelRepository.getEntityType().getAttributes().iterator();
		assertTrue(it.hasNext());
		assertEquals(it.next().getName(), "col1");
		assertTrue(it.hasNext());
		assertEquals(it.next().getName(), "col2");
		assertFalse(it.hasNext());
	}

	@Test
	public void testGetDescription()
	{
		assertNull(excelRepository.getEntityType().getDescription());
	}

	@Test
	public void testGetIdAttribute()
	{
		assertNull(excelRepository.getEntityType().getIdAttribute());
	}

	@Test
	public void testGetLabel()
	{
		assertEquals(excelRepository.getEntityType().getLabel(), "test");
	}

	@Test
	public void testGetLabelAttribute()
	{
		assertNull(excelRepository.getEntityType().getLabelAttribute());
	}

	@Test
	public void testGetName()
	{
		assertEquals(excelRepository.getName(), "test");
	}

	@Test
	public void testIterator()
	{
		Iterator<Entity> it = excelRepository.iterator();
		assertTrue(it.hasNext());

		Entity row1 = it.next();
		assertEquals(row1.get("col1"), "val1");
		assertEquals(row1.get("col2"), "val2");
		assertTrue(it.hasNext());

		Entity row2 = it.next();
		assertEquals(row2.get("col1"), "val3");
		assertEquals(row2.get("col2"), "val4");
		assertTrue(it.hasNext());

		Entity row3 = it.next();
		assertEquals(row3.get("col1"), "XXX");
		assertEquals(row3.get("col2"), "val6");
		assertTrue(it.hasNext());

		// test number cell (col1) and formula cell (col2)
		Entity row4 = it.next();
		assertEquals(row4.get("col1"), "1.2");
		assertEquals(row4.get("col2"), "2.4");
		assertFalse(it.hasNext());
	}

	@Test
	public void testAttributesAndIterator() throws IOException
	{
		Iterator<Attribute> headerIt = excelRepository.getEntityType().getAttributes().iterator();
		assertTrue(headerIt.hasNext());
		assertEquals(headerIt.next().getName(), "col1");
		assertTrue(headerIt.hasNext());
		assertEquals(headerIt.next().getName(), "col2");

		Iterator<Entity> it = excelRepository.iterator();
		assertTrue(it.hasNext());

		Entity row1 = it.next();
		assertEquals(row1.get("col1"), "val1");
		assertEquals(row1.get("col2"), "val2");
		assertTrue(it.hasNext());

		Entity row2 = it.next();
		assertEquals(row2.get("col1"), "val3");
		assertEquals(row2.get("col2"), "val4");
		assertTrue(it.hasNext());

		Entity row3 = it.next();
		assertEquals(row3.get("col1"), "XXX");
		assertEquals(row3.get("col2"), "val6");
		assertTrue(it.hasNext());

		// test number cell (col1) and formula cell (col2)
		Entity row4 = it.next();
		assertEquals(row4.get("col1"), "1.2");
		assertEquals(row4.get("col2"), "2.4");
		assertFalse(it.hasNext());
	}
}
