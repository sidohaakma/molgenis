package org.molgenis.data.mapper.job;

import org.molgenis.data.DataService;
import org.molgenis.data.jobs.Job;
import org.molgenis.data.jobs.Progress;
import org.molgenis.data.mapper.mapping.model.EntityMapping;
import org.molgenis.data.mapper.mapping.model.MappingProject;
import org.molgenis.data.mapper.mapping.model.MappingTarget;
import org.molgenis.data.mapper.service.MappingService;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.support.TransactionTemplate;

import static java.util.Objects.requireNonNull;
import static org.molgenis.data.mapper.service.impl.MappingServiceImpl.MAPPING_BATCH_SIZE;

public class MappingJob extends Job<Void>
{
	private final String mappingProjectId;
	private final String targetEntityTypeId;
	private final boolean addSourceAttribute;
	private final MappingService mappingService;
	private final DataService dataService;

	MappingJob(String mappingProjectId, String targetEntityTypeId, boolean addSourceAttribute, Progress progress,
			Authentication userAuthentication, TransactionTemplate transactionTemplate, MappingService mappingService,
			DataService dataService)
	{
		super(progress, transactionTemplate, userAuthentication);
		this.mappingProjectId = requireNonNull(mappingProjectId);
		this.targetEntityTypeId = requireNonNull(targetEntityTypeId);
		this.addSourceAttribute = requireNonNull(addSourceAttribute);
		this.mappingService = requireNonNull(mappingService);
		this.dataService = requireNonNull(dataService);
	}

	@Override
	public Void call(Progress progress) throws Exception
	{
		MappingProject mappingProject = mappingService.getMappingProject(mappingProjectId);
		MappingTarget mappingTarget = mappingProject.getMappingTargets().get(0);
		progress.setProgressMax(calculateMaxProgress(mappingTarget));
		mappingService.applyMappings(mappingTarget, targetEntityTypeId, addSourceAttribute, progress);
		return null;
	}

	private int calculateMaxProgress(MappingTarget mappingTarget)
	{
		return mappingTarget.getEntityMappings().stream().mapToInt(this::countBatches).sum();
	}

	private int countBatches(EntityMapping entityMapping)
	{
		long sourceRows = dataService.count(entityMapping.getSourceEntityType().getId());

		long batches = sourceRows / MAPPING_BATCH_SIZE;
		long remainder = sourceRows % MAPPING_BATCH_SIZE;

		if (remainder > 0)
		{
			batches++;
		}

		return (int) batches;
	}
}