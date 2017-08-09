package org.molgenis.data.security.meta;

import org.molgenis.data.AbstractRepositoryDecorator;
import org.molgenis.data.Fetch;
import org.molgenis.data.Query;
import org.molgenis.data.Repository;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.security.core.Permission;
import org.molgenis.security.core.PermissionService;

import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;

/**
 * Entity type repository decorator that marks entity types as read-only for the current user based on permissions.
 */
public class EntityTypeRepositorySecurityDecorator extends AbstractRepositoryDecorator<EntityType>
{
	private final PermissionService permissionService;

	public EntityTypeRepositorySecurityDecorator(Repository<EntityType> delegateRepository,
			PermissionService permissionService)
	{
		super(delegateRepository);
		this.permissionService = requireNonNull(permissionService);
	}

	@Override
	public Iterator<EntityType> iterator()
	{
		Iterable<EntityType> entityTypeIterable = delegate()::iterator;
		return StreamSupport.stream(entityTypeIterable.spliterator(), false)
							.map(this::toPermittedEntityType)
							.iterator();
	}

	@Override
	public void forEachBatched(Fetch fetch, Consumer<List<EntityType>> consumer, int batchSize)
	{
		MappedConsumer mappedConsumer = new MappedConsumer(consumer, this);
		delegate().forEachBatched(fetch, mappedConsumer::map, batchSize);
	}

	@Override
	public Stream<EntityType> findAll(Query<EntityType> q)
	{
		return delegate().findAll(q).map(this::toPermittedEntityType);
	}

	@Override
	public EntityType findOne(Query<EntityType> q)
	{
		return toPermittedEntityType(delegate().findOne(q));
	}

	@Override
	public EntityType findOneById(Object id)
	{
		return toPermittedEntityType(delegate().findOneById(id));
	}

	@Override
	public EntityType findOneById(Object id, Fetch fetch)
	{
		return toPermittedEntityType(delegate().findOneById(id, fetch));
	}

	@Override
	public Stream<EntityType> findAll(Stream<Object> ids)
	{
		return delegate().findAll(ids).map(this::toPermittedEntityType);
	}

	@Override
	public Stream<EntityType> findAll(Stream<Object> ids, Fetch fetch)
	{
		return delegate().findAll(ids, fetch).map(this::toPermittedEntityType);
	}

	private EntityType toPermittedEntityType(EntityType entityType)
	{
		if (entityType != null)
		{
			if (!permissionService.hasPermissionOnEntityType(entityType.getId(), Permission.WRITE))
			{
				entityType.setReadOnly(true);
			}
		}
		return entityType;
	}

	private static class MappedConsumer
	{
		private final Consumer<List<EntityType>> consumer;
		private final EntityTypeRepositorySecurityDecorator entityTypeRepositorySecurityDecorator;

		MappedConsumer(Consumer<List<EntityType>> consumer,
				EntityTypeRepositorySecurityDecorator entityTypeRepositorySecurityDecorator)
		{
			this.consumer = requireNonNull(consumer);
			this.entityTypeRepositorySecurityDecorator = requireNonNull(entityTypeRepositorySecurityDecorator);
		}

		public void map(List<EntityType> entityTypes)
		{
			Stream<EntityType> filteredEntities = entityTypes.stream()
															 .map(entityTypeRepositorySecurityDecorator::toPermittedEntityType);
			consumer.accept(filteredEntities.collect(toList()));
		}
	}
}
