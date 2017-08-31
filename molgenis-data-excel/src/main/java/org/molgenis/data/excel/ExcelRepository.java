package org.molgenis.data.excel;

import com.google.common.collect.Iterables;
import com.google.common.collect.Streams;
import org.apache.poi.ss.usermodel.Sheet;
import org.molgenis.data.Entity;
import org.molgenis.data.RepositoryCapability;
import org.molgenis.data.excel.service.ExcelService;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.processor.CellProcessor;
import org.molgenis.data.support.AbstractRepository;
import org.molgenis.data.support.DynamicEntity;
import org.molgenis.util.Pair;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import static java.util.Objects.requireNonNull;

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
	private final List<String> columns;
	private final ExcelService excelService;
	private final List<CellProcessor> processors;
	private final EntityType entityType;

	public ExcelRepository(ExcelService excelService, Sheet sheet, List<String> columns, EntityType entityType,
			List<CellProcessor> processors)
	{
		this.excelService = requireNonNull(excelService);
		this.sheet = requireNonNull(sheet);
		this.columns = requireNonNull(columns);
		this.entityType = requireNonNull(entityType);
		this.processors = requireNonNull(processors);
	}

	@Override
	public Iterator<Entity> iterator()
	{
		return Streams.stream(sheet.rowIterator())
					  .skip(1)
					  .map(row -> excelService.parse(row, processors))
					  .map(this::toEntity)
					  .iterator();
	}

	private Entity toEntity(List<Pair<Integer, String>> values)
	{
		Entity entity = new DynamicEntity(entityType);
		values.stream()
			  .filter(pair -> pair.getA() < columns.size())
			  .forEach(pair -> entity.set(columns.get(pair.getA()), pair.getB()));
		return entity;
	}

	@Override
	public EntityType getEntityType()
	{
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
