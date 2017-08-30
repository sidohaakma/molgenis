package org.molgenis.data.excel;

import com.google.common.collect.Lists;
import org.apache.poi.ss.usermodel.Sheet;
import org.molgenis.data.Entity;
import org.molgenis.data.Repository;
import org.molgenis.data.excel.service.ExcelService;
import org.molgenis.data.excel.service.ExcelServiceImpl;
import org.molgenis.data.excel.utils.ExcelFileExtensions;
import org.molgenis.data.meta.model.AttributeFactory;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.meta.model.EntityTypeFactory;
import org.molgenis.data.processor.CellProcessor;
import org.molgenis.data.processor.TrimProcessor;
import org.molgenis.data.support.FileRepositoryCollection;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;

/**
 * Read an excel file and iterate through the sheets.
 * <p>
 * A sheet is exposed as a {@link org.molgenis.data.Repository} with the sheetname as the Repository name
 */
public class ExcelRepositoryCollection extends FileRepositoryCollection
{
	public static final String NAME = "EXCEL";

	private final File file;
	private EntityTypeFactory entityTypeFactory;
	private AttributeFactory attributeFactory;

	private List<String> entityTypeIds;
	private List<String> entityTypeIdsLowerCase;

	private ExcelService excelService = new ExcelServiceImpl();

	public ExcelRepositoryCollection(File file)
	{
		this(file, new TrimProcessor());
	}

	public ExcelRepositoryCollection(File file, CellProcessor... cellProcessors)
	{
		super(ExcelFileExtensions.getExcel(), cellProcessors);
		this.file = file;
		List<Sheet> sheets = excelService.buildExcelSheetsFromFile(file);
		loadEntityNames(sheets);
	}

	private void loadEntityNames(List<Sheet> sheets)
	{
		entityTypeIds = Lists.newArrayList();
		entityTypeIdsLowerCase = Lists.newArrayList();
		sheets.forEach(sheet ->
		{
			entityTypeIds.add(sheet.getSheetName());
			entityTypeIdsLowerCase.add(sheet.getSheetName().toLowerCase());
		});
	}

	@Override
	public void init() throws IOException
	{
		// no operation
	}

	@Override
	public Iterable<String> getEntityTypeIds()
	{
		return entityTypeIds;
	}

	@Override
	public Repository<Entity> getRepository(String sheetName)
	{
		ExcelRepository repository = null;
		if (entityTypeIds.contains(sheetName))
		{
			repository = new ExcelRepository(file, entityTypeFactory, attributeFactory, sheetName, cellProcessors);
		}
		return repository;
	}

	@Override
	public String getName()
	{
		return NAME;
	}

	@Override
	public Iterator<Repository<Entity>> iterator()
	{
		return new Iterator<Repository<Entity>>()
		{
			Iterator<String> it = getEntityTypeIds().iterator();

			@Override
			public boolean hasNext()
			{
				return it.hasNext();
			}

			@Override
			public Repository<Entity> next()
			{
				return getRepository(it.next());
			}

		};
	}

	@Override
	public boolean hasRepository(String name)
	{
		if (null == name) return false;
		for (String s : getEntityTypeIds())
		{
			if (s.equals(name)) return true;
		}
		return false;
	}

	@Override
	public boolean hasRepository(EntityType entityType)
	{
		return hasRepository(entityType.getId());
	}

	@Autowired
	public void setEntityTypeFactory(EntityTypeFactory entityTypeFactory)
	{
		this.entityTypeFactory = entityTypeFactory;
	}

	@Autowired
	public void setAttributeFactory(AttributeFactory attributeFactory)
	{
		this.attributeFactory = attributeFactory;
	}
}
