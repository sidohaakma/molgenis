package org.molgenis.data.csv.service;

import com.google.common.collect.Lists;
import com.opencsv.CSVParser;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import org.apache.commons.io.input.BOMInputStream;
import org.molgenis.data.MolgenisDataException;
import org.molgenis.data.csv.utils.CsvFileExtensions;
import org.molgenis.data.csv.utils.CsvValidation;
import org.molgenis.data.processor.AbstractCellProcessor;
import org.molgenis.data.processor.CellProcessor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.lang.String.format;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.*;
import static org.molgenis.data.csv.CsvRepositoryCollection.MAC_ZIP;
import static org.molgenis.util.FileExtensionUtils.getFileNameWithoutExtension;
import static org.molgenis.util.file.ZipFileUtil.unzip;

@Component
public class CsvServiceImpl implements CsvService
{

	private Character separator = DEFAULT_CSV_SEPARATOR;

	@Override
	public Map<String, List<String[]>> buildLinesFromFile(File file)
	{
		return parseCsvOrZipFile(file, null);
	}

	@Override
	public Map<String, List<String[]>> buildLinesFromFile(File file, String repositoryName)
	{
		return parseCsvOrZipFile(file, repositoryName);
	}

	@Override
	public Map<String, List<String[]>> buildLinesFromFile(File file, String repositoryName, Character separator)
	{
		if (separator == null)
		{
			this.separator = DEFAULT_CSV_SEPARATOR;
		}
		else
		{
			this.separator = separator;
		}
		return buildLinesFromFile(file, repositoryName);
	}

	private Map<String, List<String[]>> parseCsvOrZipFile(File file, String repositoryName)
	{
		Map<String, List<String[]>> keyValueCsvLines = new HashMap<>();
		String fileExtension = StringUtils.getFilenameExtension(file.getName());
		if (fileExtension.contains(CsvFileExtensions.ZIP.toString()))
		{
			try
			{
				for (File zipEntry : unzip(file))
				{
					String zipFileExtension = StringUtils.getFilenameExtension(zipEntry.getName());
					{
						if (!zipEntry.getName().contains(MAC_ZIP) && !zipEntry.isDirectory())
						{
							if (repositoryName != null && repositoryName.equals(zipEntry.getName()))
							{
								List<String[]> lines = parseCsvContent(zipEntry, zipFileExtension);
								keyValueCsvLines.put(createValidIdFromFileName(zipEntry.getName()), lines);
								break;
							}
							else
							{
								List<String[]> lines = parseCsvContent(zipEntry, zipFileExtension);
								keyValueCsvLines.put(createValidIdFromFileName(zipEntry.getName()), lines);
							}
						}
					}
				}
			}
			catch (IOException e)
			{
				throw new MolgenisDataException(format("No file found [%s]", file.getName()));
			}
		}
		else
		{
			keyValueCsvLines.put(createValidIdFromFileName(file.getName()), parseCsvContent(file, fileExtension));
		}
		return keyValueCsvLines;
	}

	/**
	 * <p>Create a CsvReader to obtain data from CSV file</p>
	 *
	 * @param file given file
	 * @return inesList of rows
	 */
	private List<String[]> parseCsvContent(File file, String fileExtension)
	{
		List<String[]> lines;
		CSVReader reader;
		try (InputStream inputStream = new FileInputStream(file))
		{
			reader = parseCsvContent(fileExtension, removeByteOrderMark(inputStream));
			if (reader != null)
			{
				lines = reader.readAll();
				CsvValidation.validateCsvFile(lines, file.getName());
			}
			else
			{
				throw new MolgenisDataException(format("CSV-file [%s] is corrupt", file.getName()));
			}
		}
		catch (IOException err)
		{
			throw new MolgenisDataException(format("CSV-file [%s] is corrupt", file.getName()));
		}
		return lines;
	}

	private CSVReader parseCsvContent(String fileExtension, InputStream in)
	{
		CSVReader csvReader = null;
		Reader reader = new InputStreamReader(in, UTF_8);
		if (CsvValidation.validateCsvFileExtensions(fileExtension))
		{
			if (fileExtension.equals(CsvFileExtensions.TSV.toString()) && separator == DEFAULT_CSV_SEPARATOR)
			{
				separator = '\t';
			}

			CSVParser parser = new CSVParserBuilder().withSeparator(separator).build();
			csvReader = new CSVReaderBuilder(reader).withCSVParser(parser).build();
		}
		return csvReader;
	}

	@Override
	public List<String> parseHeader(Map<String, List<String[]>> parsedCsvParsed, String id,
			List<CellProcessor> processors)
	{
		List<String[]> parsedCsvFile = parsedCsvParsed.get(id);
		String[] headerRow = parsedCsvFile.get(0);
		if (headerRow == null || headerRow.length == 0)
		{
			throw new MolgenisDataException(format("CSV-file [%s] columns are empty", id));
		}
		List<String> parsedHeaders = Lists.newArrayList(headerRow).stream().
				map(cell -> AbstractCellProcessor.processCell(cell, true, processors)).collect(Collectors.toList());

		int maxIndex = parsedHeaders.indexOf(null);
		if (maxIndex >= 0)
		{
			parsedHeaders = parsedHeaders.subList(0, maxIndex);
		}

		Map<String, Long> counted = parsedHeaders.stream().collect(groupingBy(identity(), counting()));

		List<String> duplicateHeaders = counted.entrySet()
											   .stream()
											   .filter(entry -> entry.getValue() > 1)
											   .map(Map.Entry::getKey)
											   .collect(toList());
		if (!duplicateHeaders.isEmpty())
		{
			throw new MolgenisDataException(
					format("CSV-file [%s] has duplicate columns %s", id, duplicateHeaders.toString()));
		}
		return parsedHeaders;
	}

	/**
	 * <p>Convert the inputstreams that can be generated by the CsvIterator and check on BOM-attachements.</p>
	 *
	 * @param inputStream from zipfile or normal files
	 * @return inputStream without ByteOrderMark (always)
	 */
	private InputStream removeByteOrderMark(InputStream inputStream)
	{
		return new BOMInputStream(inputStream, false);
	}

	@Override
	public String createValidIdFromFileName(String filename)
	{
		String packageName = getFileNameWithoutExtension(filename);
		return packageName.replaceAll(ILLEGAL_CHARACTER_REGEX, "_");
	}

}