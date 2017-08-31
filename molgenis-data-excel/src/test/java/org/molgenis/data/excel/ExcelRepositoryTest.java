package org.molgenis.data.excel;

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
import org.molgenis.data.meta.model.EntityTypeFactory;
import org.molgenis.data.processor.CellProcessor;
import org.molgenis.util.ResourceUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;

import static org.mockito.Mockito.*;
import static org.testng.Assert.*;

public class ExcelRepositoryTest extends AbstractMolgenisSpringTest
{
	@Autowired
	private EntityTypeFactory entityTypeFactory;

	@Autowired
	private AttributeFactory attrMetaFactory;

	private ExcelRepository excelSheetReader;

	private ExcelService excelService = new ExcelServiceImpl();

	@BeforeMethod
	public void beforeMethod() throws InvalidFormatException, IOException
	{
		File file = ResourceUtils.getFile(getClass(), "/test.xls");

		Sheet sheet = excelService.buildExcelSheetFromFile(file, "test");
		excelSheetReader = new ExcelRepository(sheet, entityTypeFactory, attrMetaFactory, null);
	}

	@SuppressWarnings("resource")
	@Test(expectedExceptions = MolgenisDataException.class)
	public void testExcelRepositoryConstructor()
	{
		File file = ResourceUtils.getFile(getClass(), "/test.xls");
		Sheet sheet = excelService.buildExcelSheetFromFile(file, "test_mergedcells");
		ExcelRepository repository = new ExcelRepository(sheet, entityTypeFactory, attrMetaFactory, null);
		repository.iterator();
	}

	@Test
	public void testAddCellProcessorHeader()
	{
		CellProcessor processor = mock(CellProcessor.class);
		when(processor.processHeader()).thenReturn(true);
		when(processor.process("col1")).thenReturn("col1");
		when(processor.process("col2")).thenReturn("col2");

		excelSheetReader.addCellProcessor(processor);
		for (@SuppressWarnings("unused") Entity entity : excelSheetReader)
		{
		}
		verify(processor).process("col1");
		verify(processor).process("col2");
	}

	@Test
	public void testAddCellProcessorData()
	{
		CellProcessor processor = when(mock(CellProcessor.class).processData()).thenReturn(true).getMock();
		excelSheetReader.addCellProcessor(processor);
		for (Entity entity : excelSheetReader)
			entity.get("col2");

		verify(processor).process("val2");
		verify(processor).process("val4");
		verify(processor).process("val6");
	}

	@Test
	public void testGetAttribute()
	{
		Attribute attr = excelSheetReader.getEntityType().getAttribute("col1");
		assertNotNull(attr);
		assertEquals(attr.getDataType(), AttributeType.STRING);
		assertEquals(attr.getName(), "col1");
	}

	@Test
	public void testGetAttributes()
	{
		Iterator<Attribute> it = excelSheetReader.getEntityType().getAttributes().iterator();
		assertTrue(it.hasNext());
		assertEquals(it.next().getName(), "col1");
		assertTrue(it.hasNext());
		assertEquals(it.next().getName(), "col2");
		assertFalse(it.hasNext());
	}

	@Test
	public void testGetDescription()
	{
		assertNull(excelSheetReader.getEntityType().getDescription());
	}

	@Test
	public void testGetIdAttribute()
	{
		assertNull(excelSheetReader.getEntityType().getIdAttribute());
	}

	@Test
	public void testGetLabel()
	{
		assertEquals(excelSheetReader.getEntityType().getLabel(), "test");
	}

	@Test
	public void testGetLabelAttribute()
	{
		assertNull(excelSheetReader.getEntityType().getLabelAttribute());
	}

	@Test
	public void testGetName()
	{
		assertEquals(excelSheetReader.getName(), "test");
	}

	@Test
	public void testIterator()
	{
		Iterator<Entity> it = excelSheetReader.iterator();
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
		Iterator<Attribute> headerIt = excelSheetReader.getEntityType().getAttributes().iterator();
		assertTrue(headerIt.hasNext());
		assertEquals(headerIt.next().getName(), "col1");
		assertTrue(headerIt.hasNext());
		assertEquals(headerIt.next().getName(), "col2");

		Iterator<Entity> it = excelSheetReader.iterator();
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
