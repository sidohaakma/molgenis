package org.molgenis.data.csv;

import com.google.common.collect.Iterables;
import org.molgenis.data.Entity;
import org.molgenis.data.RepositoryCapability;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.processor.CellProcessor;
import org.molgenis.data.support.AbstractRepository;
import org.molgenis.data.support.DynamicEntity;

import java.util.*;

import static org.molgenis.data.processor.AbstractCellProcessor.processCell;

/**
 * Repository implementation for csv files.
 * <p>
 * The filename without the extension is considered to be the entityname
 */
public class CsvRepository extends AbstractRepository
{
	private final Map<String, List<String[]>> lines;
	private final List<String> columns;
	private final String repositoryName;
	private final EntityType entityType;
	private final List<CellProcessor> processors;

	public CsvRepository(Map<String, List<String[]>> lines, List<String> columns, String repositoryName,
			EntityType entityType, List<CellProcessor> processors)
	{
		this.lines = Objects.requireNonNull(lines);
		this.columns = Objects.requireNonNull(columns);
		this.repositoryName = Objects.requireNonNull(repositoryName);
		this.entityType = Objects.requireNonNull(entityType);
		this.processors = Objects.requireNonNull(processors);
	}

	@Override
	public Iterator<Entity> iterator()
	{
		return lines.get(repositoryName).stream().skip(1).map(this::toEntity).iterator();
	}

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

	private Entity toEntity(String[] row)
	{
		Entity entity = new DynamicEntity(entityType);
		for (int index = 0; index < columns.size(); index++)
		{
			entity.set(columns.get(index), processCell(row[index], false, processors));
		}
		return entity;
	}

}
