package org.molgenis.data.model;

import org.molgenis.auth.SecurityPackage;
import org.molgenis.data.meta.SystemEntityType;
import org.springframework.stereotype.Component;

import static java.util.Objects.requireNonNull;
import static org.molgenis.auth.SecurityPackage.PACKAGE_SECURITY;
import static org.molgenis.data.meta.model.EntityType.AttributeRole.ROLE_ID;
import static org.molgenis.data.meta.model.EntityType.AttributeRole.ROLE_LABEL;
import static org.molgenis.data.meta.model.Package.PACKAGE_SEPARATOR;

@Component
public class AclRuleMetaData extends SystemEntityType
{
	private static final String SIMPLE_NAME = "AclRule";
	public static final String ACL_RULE = PACKAGE_SECURITY + PACKAGE_SEPARATOR + SIMPLE_NAME;

	public static final String ID = "id";
	public static final String LABEL = "label";
	public static final String ENTITY_TYPE = "entityType";
	public static final String ATTRIBUTE = "attribute";
	public static final String VALUE = "value";
	public static final String PERMISSION = "permission";
	public static final String SID = "sid";

	private final SecurityPackage securityPackage;

	AclRuleMetaData(SecurityPackage scriptPackage)
	{
		super(SIMPLE_NAME, PACKAGE_SECURITY);
		this.securityPackage = requireNonNull(scriptPackage);
	}

	@Override
	public void init()
	{
		setLabel("ACL rule");
		setPackage(securityPackage);

		addAttribute(ID, ROLE_ID).setAuto(true).setVisible(false);
		addAttribute(LABEL, ROLE_LABEL).setNillable(false);
		addAttribute(ENTITY_TYPE).setNillable(false);
		addAttribute(ATTRIBUTE).setNillable(false);
		addAttribute(VALUE).setNillable(false);
		addAttribute(PERMISSION).setNillable(false);
		addAttribute(SID).setNillable(false);

	}
}
