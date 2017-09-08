package org.molgenis.data.excel;

import org.apache.poi.ss.usermodel.Sheet;
import org.molgenis.data.Entity;
import org.molgenis.data.MolgenisInvalidFormatException;
import org.molgenis.data.Repository;
import org.molgenis.data.UnknownEntityException;
import org.molgenis.data.excel.service.ExcelService;
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
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static org.molgenis.data.meta.AttributeType.STRING;

/**
 * Read an excel file and iterate through the sheets.
 * <p>
 * A sheet is exposed as a {@link org.molgenis.data.Repository} with the sheetname as the Repository name
 */
public class ExcelRepositoryCollection extends FileRepositoryCollection
{
	public static final String NAME = "EXCEL";

	private ExcelService excelService;
	private EntityTypeFactory entityTypeFactory;
	private AttributeFactory attributeFactory;

	private final File file;
	private List<String> entityTypeIds;
	private List<Sheet> sheets;

	public ExcelRepositoryCollection(File file) throws IOException, MolgenisInvalidFormatException
	{
		this(file, Collections.singletonList(new TrimProcessor()));
	}

	private ExcelRepositoryCollection(File file, List<CellProcessor> processors)
	{
		super(ExcelFileExtensions.getExcel(), processors.toArray(new CellProcessor[0]));
		this.file = Objects.requireNonNull(file);
	}

	@Override
	public void init() throws IOException
	{
		this.sheets = excelService.buildExcelSheetsFromFile(this.file);
		this.entityTypeIds = sheets.stream().map(Sheet::getSheetName).collect(Collectors.toList());
	}

	@Override
	public Iterable<String> getEntityTypeIds()
	{
		return entityTypeIds;
	}

	@Override
	public Repository<Entity> getRepository(final String sheetName)
	{
		Sheet sheet = sheets.stream()
							.filter(candidate -> candidate.getSheetName().equals(sheetName))
							.findFirst()
							.orElseThrow(UnknownEntityException::new);

		List<String> columns = excelService.parseHeader(sheet, cellProcessors);
		return new ExcelRepository(excelService, sheet, columns, getEntityType(sheet.getSheetName(), columns),
				cellProcessors);
	}

	private EntityType getEntityType(String repositoryName, List<String> columns)
	{
		EntityType entityType = entityTypeFactory.create(repositoryName).setLabel(repositoryName);

		columns.stream()
			   .map(column -> attributeFactory.create().setName(column).setDataType(STRING))
			   .forEach(entityType::addAttribute);

		return entityType;
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

	@Autowired
	public void setExcelService(ExcelService excelService)
	{
		this.excelService = excelService;
	}

}
