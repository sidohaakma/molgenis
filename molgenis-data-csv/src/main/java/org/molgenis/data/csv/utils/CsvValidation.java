package org.molgenis.data.csv.utils;

import org.molgenis.data.MolgenisDataException;

import java.util.List;

import static java.lang.String.format;

public class CsvValidation
{
	private CsvValidation()
	{
	}

	/**
	 * <p>CSV-file validation.</p>
	 *
	 * @param lines content of CSV-file
	 */
	public static void validateCsvFile(List<String[]> lines, String fileName)
	{
		if (lines.isEmpty())
		{
			throw new MolgenisDataException(format("CSV-file: [%s] is empty", fileName));
		}

		if (lines.size() == 1)
		{
			throw new MolgenisDataException(format("Header was found, but no data is present in file [%s]", fileName));
		}

		int headerLength = lines.get(0).length;
		lines.forEach(row ->
		{
			if (row.length < headerLength)
			{
				throw new MolgenisDataException(
						format("Column count [%s] is not greater or equal then header count [%s] (in CSV-file [%s])",
								row.length, headerLength, fileName));
			}
		});
	}

	public static boolean validateCsvFileExtensions(String fileExtension)
	{
		boolean isValidExtension = false;
		if (CsvFileExtensions.getCSV().contains(fileExtension))
		{
			isValidExtension = true;
		}
		else
		{
			throw new MolgenisDataException(
					format("File type not recognized [%s], the following file types are supported [%s]", fileExtension,
							CsvFileExtensions.getCSV().toString()));
		}
		return isValidExtension;
	}

}
