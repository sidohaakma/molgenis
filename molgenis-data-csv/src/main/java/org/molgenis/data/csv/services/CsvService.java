package org.molgenis.data.csv.services;

import java.io.File;
import java.util.List;

public interface CsvService
{

	Character DEFAULT_CSV_SEPARATOR = ',';

	/**
	 * Creates a List with String[] containing the lines of a CSV file
	 * Including the header
	 * <p>
	 * Uses the {@link com.opencsv.CSVReader} which is also used in the {@link org.molgenis.data.csv.CsvIterator}
	 *
	 * @param file can be a zip or a regular file
	 */
	List<String[]> buildLinesFromFile(File file);

	/**
	 * Just like the buildLinesFromFile method but added a separator.
	 *
	 * @param file      CSV file to parse
	 * @param separator CSV separator
	 * @return List of lines
	 */
	List<String[]> buildLinesFromFile(File file, Character separator);

}
