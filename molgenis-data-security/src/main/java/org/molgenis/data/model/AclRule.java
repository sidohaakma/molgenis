package org.molgenis.data.model;

import org.molgenis.data.Entity;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.support.StaticEntity;

/**
 * Script type entity
 */
public class AclRule extends StaticEntity
{
	public AclRule(Entity entity)
	{
		super(entity);
	}

	/**
	 * Constructs a script type with the given meta data
	 *
	 * @param entityType script type meta data
	 */
	public AclRule(EntityType entityType)
	{
		super(entityType);
	}

	/**
	 * Constructs a script type with the given type name and meta data
	 *
	 * @param id         script type name
	 * @param entityType script type meta data
	 */
	public AclRule(String id, EntityType entityType)
	{
		super(entityType);
		set(AclRuleMetaData.ID, id);
	}

}
