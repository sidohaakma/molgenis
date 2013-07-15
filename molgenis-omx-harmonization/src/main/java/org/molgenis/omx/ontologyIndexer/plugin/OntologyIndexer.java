package org.molgenis.omx.ontologyIndexer.plugin;

import java.io.File;

import org.molgenis.framework.tupletable.TableException;

public interface OntologyIndexer
{
	void index(String ontologyName, File file) throws TableException;

	boolean isIndexingRunning();

	boolean isCorrectOntology();

	String getOntologyUri();
}
