package org.molgenis.data.excel;

import com.google.common.collect.Lists;
import org.molgenis.data.AbstractMolgenisSpringTest;
import org.molgenis.data.Entity;
import org.molgenis.data.MolgenisInvalidFormatException;
import org.molgenis.data.Repository;
import org.molgenis.data.excel.service.ExcelServiceImpl;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.AttributeFactory;
import org.molgenis.data.meta.model.EntityTypeFactory;
import org.molgenis.util.ResourceUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

public class ExcelRepositorySourceTest extends AbstractMolgenisSpringTest
{
	@Autowired
	private EntityTypeFactory entityTypeFactory;

	@Autowired
	private AttributeFactory attrMetaFactory;

	private ExcelRepositoryCollection excelRepositoryCollection;

	@BeforeMethod
	public void beforeMethod() throws MolgenisInvalidFormatException, IOException
	{
		File file = ResourceUtils.getFile(getClass(), "/test-multiple-valid-sheets.xls");
		excelRepositoryCollection = new ExcelRepositoryCollection(file);
		excelRepositoryCollection.setExcelService(new ExcelServiceImpl());
		excelRepositoryCollection.setAttributeFactory(attrMetaFactory);
		excelRepositoryCollection.setEntityTypeFactory(entityTypeFactory);
		excelRepositoryCollection.init();
	}

	@Test
	public void getRepositories()
	{
		List<String> repositories = Lists.newArrayList(excelRepositoryCollection.getEntityTypeIds());
		assertEquals(repositories, Arrays.asList("test", "Blad2", "test_mergedcells"));
	}

	@Test
	public void getRepository()
	{
		Repository<Entity> test = excelRepositoryCollection.getRepository("test");
		assertNotNull(test);
		assertEquals(test.getName(), "test");
		Attribute attrTest = test.getEntityType().getAttribute("col1");
		assertEquals(attrTest.getName(), "col1");

		Repository<Entity> blad2 = excelRepositoryCollection.getRepository("Blad2");
		assertNotNull(blad2);
		assertEquals(blad2.getName(), "Blad2");
		Attribute attrBlad2 = blad2.getEntityType().getAttribute("col11");
		assertEquals(attrBlad2.getName(), "col11");
	}
}
