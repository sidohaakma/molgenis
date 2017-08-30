package org.molgenis.data.csv.service;

import java.io.File;
import java.util.List;
import java.util.Map;

public interface CsvService
{

	Character DEFAULT_CSV_SEPARATOR = ',';
	String ILLEGAL_CHARACTER_REGEX = "[^a-zA-Z0-9_#]+";

	/**
	 * Creates a List with String[] containing the lines of a CSV file
	 * Including the header
	 * <p>
	 * Uses the {@link com.opencsv.CSVReader} which is also used in the {@link org.molgenis.data.csv.CsvIterator}
	 *
	 * @param file can be a zip or a regular file
	 */
	Map<String, List<String[]>> buildLinesFromFile(final File file);

	/**
	 * @param file
	 * @return
	 */
	Map<String, List<String[]>> buildLinesFromFile(final File file, final String repositoryName);

	/**
	 * Just like the buildLinesFromFile method but added a separator.
	 *
	 * @param file      CSV file to parse
	 * @param separator CSV separator
	 * @return List of lines
	 */
	Map<String, List<String[]>> buildLinesFromFile(final File file, final String repositoryName, Character separator);

	String createValidIdFromFileName(String fileName);

}
