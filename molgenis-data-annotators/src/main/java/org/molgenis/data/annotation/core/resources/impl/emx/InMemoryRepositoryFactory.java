package org.molgenis.data.annotation.core.resources.impl.emx;

import com.google.common.collect.ImmutableMap;
import org.molgenis.data.Entity;
import org.molgenis.data.MolgenisDataException;
import org.molgenis.data.Repository;
import org.molgenis.data.annotation.core.resources.impl.RepositoryFactory;
import org.molgenis.data.excel.ExcelRepositoryCollection;
import org.molgenis.data.excel.service.ExcelService;
import org.molgenis.data.importer.MetaDataParser;
import org.molgenis.data.mem.InMemoryRepository;
import org.molgenis.data.meta.DefaultPackage;
import org.molgenis.data.meta.model.AttributeFactory;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.meta.model.EntityTypeFactory;

import java.io.File;
import java.io.IOException;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.StreamSupport;

/**
 * Factory that can instantiate a InMemoryRepository.
 */
public class InMemoryRepositoryFactory implements RepositoryFactory
{
	private final AttributeFactory attributeFactory;
	private final EntityTypeFactory entityTypeFactory;
	private final ExcelService excelService;

	private final String id;
	private final String name;

	private ExcelRepositoryCollection repositoryCollection = null;
	private final MetaDataParser parser;

	public InMemoryRepositoryFactory(String entityId, String name, MetaDataParser parser, ExcelService excelService,
			EntityTypeFactory entityTypeFactory, AttributeFactory attributeFactory)
	{
		this.id = entityId;
		this.name = name;
		this.parser = parser;
		this.excelService = excelService;
		this.attributeFactory = attributeFactory;
		this.entityTypeFactory = entityTypeFactory;
	}

	@Override
	public Repository<Entity> createRepository(File file) throws IOException
	{
		try
		{
			repositoryCollection = new ExcelRepositoryCollection(file);
			repositoryCollection.setEntityTypeFactory(entityTypeFactory);
			repositoryCollection.setAttributeFactory(attributeFactory);
			repositoryCollection.setExcelService(excelService);
		}
		catch (Exception e)
		{
			throw new MolgenisDataException(
					"Unable to create ExcelRepositoryCollection for file:" + file.getName() + " exception: " + e);
		}

		ImmutableMap<String, EntityType> entityMap = parser.parse(repositoryCollection, DefaultPackage.PACKAGE_DEFAULT)
														   .getEntityMap();
		if (!entityMap.containsKey(id))
		{
			throw new MolgenisDataException("Entity [" + id + "] is not found. Entities found: " + entityMap.keySet());
		}

		EntityType metaData = entityMap.get(id);
		InMemoryRepository inMemoryRepository = new InMemoryRepository(metaData);
		inMemoryRepository.add(StreamSupport.stream(
				Spliterators.spliteratorUnknownSize(repositoryCollection.getRepository(name).iterator(),
						Spliterator.ORDERED), false));
		return inMemoryRepository;
	}
}
