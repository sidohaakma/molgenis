package org.molgenis.data.model;

import org.molgenis.data.AbstractSystemEntityFactory;
import org.molgenis.data.populate.EntityPopulator;
import org.springframework.stereotype.Component;

@Component
public class AclRuleFactory extends AbstractSystemEntityFactory<AclRule, AclRuleMetaData, String>
{
	AclRuleFactory(AclRuleMetaData aclRuleMetadata, EntityPopulator entityPopulator)
	{
		super(AclRule.class, aclRuleMetadata, entityPopulator);
	}
}
