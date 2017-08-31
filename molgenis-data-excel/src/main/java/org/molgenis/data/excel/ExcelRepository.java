package org.molgenis.data.excel;

import com.google.common.collect.Iterables;
import org.apache.poi.ss.usermodel.Sheet;
import org.molgenis.data.Entity;
import org.molgenis.data.RepositoryCapability;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.AttributeFactory;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.meta.model.EntityTypeFactory;
import org.molgenis.data.processor.CellProcessor;
import org.molgenis.data.support.AbstractRepository;

import javax.annotation.Nullable;
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
	private final Sheet sheet;
	private final String sheetName;
	private final EntityTypeFactory entityTypeFactory;
	private final AttributeFactory attrMetaFactory;

	/**
	 * process cells after reading
	 */
	private List<CellProcessor> cellProcessors;

	private EntityType entityType;

	public ExcelRepository(Sheet sheet, EntityTypeFactory entityTypeFactory, AttributeFactory attrMetaFactory)
	{
		this(sheet, entityTypeFactory, attrMetaFactory, null);
	}

	public ExcelRepository(Sheet sheet, EntityTypeFactory entityTypeFactory, AttributeFactory attrMetaFactory,
			@Nullable List<CellProcessor> cellProcessors)
	{
		this.sheet = requireNonNull(sheet);
		this.sheetName = sheet.getSheetName();
		this.entityTypeFactory = requireNonNull(entityTypeFactory);
		this.attrMetaFactory = requireNonNull(attrMetaFactory);
		this.cellProcessors = cellProcessors;
	}

	@Override
	public Iterator<Entity> iterator()
	{
		return new ExcelIterator(sheet, cellProcessors, getEntityType());
	}

	public void addCellProcessor(CellProcessor cellProcessor)
	{
		if (cellProcessors == null) cellProcessors = new ArrayList<>();
		cellProcessors.add(cellProcessor);
	}

	public EntityType getEntityType()
	{
		String finalSheetName = this.sheetName.isEmpty() ? sheet.getSheetName() : this.sheetName;
		if (entityType == null)
		{
			entityType = entityTypeFactory.create(finalSheetName).setLabel(finalSheetName);

			for (String attrName : new ExcelIterator(sheet, null).getColNamesMap().keySet())
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
