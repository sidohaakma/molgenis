package org.molgenis.data.excel;

import com.google.common.collect.Iterables;
import org.molgenis.data.Entity;
import org.molgenis.data.RepositoryCapability;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.AttributeFactory;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.meta.model.EntityTypeFactory;
import org.molgenis.data.processor.CellProcessor;
import org.molgenis.data.support.AbstractRepository;

import javax.annotation.Nullable;
import java.io.File;
import java.util.*;

import static java.util.Objects.requireNonNull;
import static org.molgenis.data.meta.AttributeType.STRING;

/**
 * ExcelSheet {@link org.molgenis.data.Repository} implementation
 * <p>
 * It is assumed that the first row of the sheet is the header row.
 * <p>
 * All attributes will be of the string type. The cell values are converted to string.
 * <p>
 * The url of this Repository is defined as excel://${filename}/${sheetName}
 */
public class ExcelRepository extends AbstractRepository
{
	private final File file;
	private final String sheetName;
	private final EntityTypeFactory entityTypeFactory;
	private final AttributeFactory attrMetaFactory;

	/**
	 * process cells after reading
	 */
	private List<CellProcessor> cellProcessors;

	private EntityType entityType;

	public ExcelRepository(File file, EntityTypeFactory entityTypeFactory, AttributeFactory attrMetaFactory,
			String sheetName)
	{
		this(file, entityTypeFactory, attrMetaFactory, sheetName, null);
	}

	public ExcelRepository(File file, EntityTypeFactory entityTypeFactory, AttributeFactory attrMetaFactory,
			String sheetName, @Nullable List<CellProcessor> cellProcessors)
	{
		this.file = requireNonNull(file);
		this.sheetName = sheetName;
		this.entityTypeFactory = requireNonNull(entityTypeFactory);
		this.attrMetaFactory = requireNonNull(attrMetaFactory);
		this.cellProcessors = cellProcessors;
	}

	@Override
	public Iterator<Entity> iterator()
	{
		return new ExcelIterator(file, sheetName, cellProcessors, getEntityType());
	}

	public void addCellProcessor(CellProcessor cellProcessor)
	{
		if (cellProcessors == null) cellProcessors = new ArrayList<>();
		cellProcessors.add(cellProcessor);
	}

	public EntityType getEntityType()
	{
		String sheetOrFileName;
		if (entityType == null)
		{
			if (sheetName == null)
			{
				sheetOrFileName = file.getName();
			}
			else
			{
				sheetOrFileName = sheetName;
			}
			entityType = entityTypeFactory.create(sheetOrFileName).setLabel(sheetOrFileName);

			for (String attrName : new ExcelIterator(file, sheetOrFileName, null).getColNamesMap().keySet())
			{
				Attribute attr = attrMetaFactory.create().setName(attrName).setDataType(STRING);
				entityType.addAttribute(attr);
			}
		}

		return entityType;
	}

	@Override
	public Set<RepositoryCapability> getCapabilities()
	{
		return Collections.emptySet();
	}

	@Override
	public long count()
	{
		return Iterables.size(this);
	}
}
