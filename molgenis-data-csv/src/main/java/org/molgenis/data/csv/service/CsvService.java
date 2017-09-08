package org.molgenis.data.csv.service;

import org.molgenis.data.processor.CellProcessor;

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
	 * Uses the {@link com.opencsv.CSVReader}
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

	/**
	 * <p>Is needed to make a valid filename in the Map<String, List<String[]>></p>
	 *
	 * @param fileName raw filename
	 * @return a filename with no exeptional characters
	 */
	String createValidIdFromFileName(String fileName);

	/**
	 * <p></p>
	 *
	 * @param parsedCsvFiles
	 * @param id
	 * @param processors
	 * @return list of columns
	 */
	List<String> parseHeader(Map<String, List<String[]>> parsedCsvFiles, String id, List<CellProcessor> processors);

}
