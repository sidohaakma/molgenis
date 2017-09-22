package org.molgenis.data;

import org.apache.commons.lang3.StringUtils;
import org.molgenis.data.model.AclRule;
import org.molgenis.data.model.AclRuleMetaData;
import org.molgenis.data.security.acl.*;
import org.molgenis.security.core.Permission;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;
import static org.molgenis.security.core.runas.RunAsSystemAspect.runAsSystem;

/**
 * Repository decorated
 */
public class RepositorySecurityQueryDecorator extends AbstractRepositoryDecorator<Entity>
{
	private final EntityAclManager entityAclManager;
	private final DataService dataService;

	public RepositorySecurityQueryDecorator(Repository<Entity> delegateRepository, EntityAclManager entityAclManager,
			DataService dataService)
	{
		super(delegateRepository);
		this.entityAclManager = requireNonNull(entityAclManager);
		this.dataService = dataService;
	}

	@Override
	public void add(Entity entity)
	{
		delegate().add(entity);
		processEntity(entity);
	}

	@Override
	public Integer add(Stream<Entity> entities)
	{
		List<Entity> entities1 = entities.collect(Collectors.toList());

		Integer result = delegate().add(entities1.stream());
		entities1.forEach(this::processEntity);
		return result;
	}

	private boolean processEntity(Entity entity)
	{
		List<AclRule> aclRules = runAsSystem(() ->
		{
			return dataService.query(AclRuleMetaData.ACL_RULE, AclRule.class)
							  .eq(AclRuleMetaData.ENTITY_TYPE, getEntityType().getId())
							  .findAll()
							  .collect(Collectors.toList());
		});
		aclRules.forEach(aclRule ->
		{
			if (matches(aclRule, entity))
			{
				addAces(aclRule, entity);
			}
		});
		return true;
	}

	private void addAces(AclRule aclRule, Entity entity)
	{
		EntityIdentity entityIdentity = EntityIdentity.create(entity.getEntityType().getId(), entity.getIdValue());
		EntityAcl entityAcl = entityAclManager.readAcl(entityIdentity);
		List<EntityAce> entityAceList = new ArrayList<>(entityAcl.getEntries());
		EntityAce ace = EntityAce.create(
				Collections.singleton(Permission.valueOf(aclRule.getString(AclRuleMetaData.PERMISSION))),
				SecurityId.createForAuthority(aclRule.getString(AclRuleMetaData.SID)), true);
		entityAceList.add(ace);
		EntityAcl entityAcl1 = entityAcl.toBuilder().setEntries(entityAceList).build();
		entityAclManager.updateAcl(entityAcl1);
	}

	private boolean matches(AclRule aclRule, Entity entity)
	{
		return StringUtils.equals(entity.getString(aclRule.getAttribute()), aclRule.getValue());
	}
}
