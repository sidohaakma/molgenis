package org.molgenis.data.excel;

import com.google.common.collect.Iterators;
import com.google.common.collect.Sets;
import org.apache.commons.io.FileUtils;
import org.apache.poi.ss.usermodel.Sheet;
import org.molgenis.data.AbstractMolgenisSpringTest;
import org.molgenis.data.Entity;
import org.molgenis.data.excel.service.ExcelService;
import org.molgenis.data.excel.service.ExcelServiceImpl;
import org.molgenis.data.meta.model.AttributeFactory;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.meta.model.EntityTypeFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.FileCopyUtils;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

import static org.testng.Assert.assertEquals;

public class ExcelIteratorTest extends AbstractMolgenisSpringTest
{

	@Autowired
	private EntityTypeFactory entityTypeFactory;

	@Autowired
	private AttributeFactory attrMetaFactory;

	private EntityType entityType;

	private ExcelService excelService = new ExcelServiceImpl();

	@BeforeMethod
	public void setUpBeforeMethod()
	{
		entityType = entityTypeFactory.create();
		entityType.addAttribute(attrMetaFactory.create().setName("name"));
		entityType.addAttribute(attrMetaFactory.create().setName("description"));
		entityType.addAttribute(attrMetaFactory.create().setName("parent"));
	}

	@Test
	public void testIteratorFromComplexExcelFile() throws IOException
	{
		File excelFile = createTmpFileForResource("test_complex_packages.xlsx");

		Sheet sheet = excelService.buildExcelSheetFromFile(excelFile, "packages");

		ExcelIterator it = new ExcelIterator(sheet, null, entityType);
		assertEquals(it.getColNamesMap().keySet(),
				Sets.newLinkedHashSet(Arrays.asList("name", "description", "parent")));
		assertEquals(Iterators.size(it), 3);

		it = new ExcelIterator(sheet, null, entityType);
		Entity entity = it.next();
		assertEquals(entity.get("name"), "it_emx_datatypes");
		assertEquals(entity.get("description"), "MOLGENIS datatypes test package");
		assertEquals(entity.get("parent"), "it_emx");
	}

	private File createTmpFileForResource(String fileName) throws IOException
	{
		InputStream in = getClass().getResourceAsStream("/" + fileName);
		File excelFile = new File(FileUtils.getTempDirectory(), fileName);
		FileCopyUtils.copy(in, new FileOutputStream(excelFile));
		return excelFile;
	}

}
