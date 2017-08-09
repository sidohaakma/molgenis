package org.molgenis.ui;

import org.molgenis.data.meta.SystemEntityType;
import org.springframework.stereotype.Component;

import static org.molgenis.data.meta.model.EntityType.AttributeRole.*;
import static org.molgenis.data.meta.model.Package.PACKAGE_SEPARATOR;
import static org.molgenis.data.system.model.RootSystemPackage.PACKAGE_SYSTEM;

@Component
public class PluginMetadata extends SystemEntityType
{
	private static final String SIMPLE_NAME = "Plugin";
	public static final String PLUGIN = PACKAGE_SYSTEM + PACKAGE_SEPARATOR + SIMPLE_NAME;

	public static final String ID = "id";
	public static final String LABEL = "label";
	public static final String URI = "uri";
	public static final String FULL_URI = "fullUri";

	PluginMetadata()
	{
		super(SIMPLE_NAME, PACKAGE_SYSTEM);
	}

	@Override
	public void init()
	{
		setLabel("Plugin");

		addAttribute(ID, ROLE_ID).setLabel("Identifier");
		addAttribute(LABEL, ROLE_LABEL, ROLE_LOOKUP).setNillable(false).setUnique(true).setLabel("Label");
		addAttribute(URI).setLabel("URI");
		addAttribute(FULL_URI).setLabel("Full URI");
	}
}
