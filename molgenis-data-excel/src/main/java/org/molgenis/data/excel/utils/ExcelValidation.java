package org.molgenis.data.excel.utils;

import org.apache.poi.ss.usermodel.Sheet;
import org.molgenis.data.MolgenisDataException;

import static java.lang.String.format;

public class ExcelValidation
{

	private ExcelValidation()
	{
		// no constructor to avoid class initialization
	}

	public static boolean validateExcelSheets(Sheet sheet, String fileName)
	{
		if (sheet == null)
		{
			throw new MolgenisDataException(format("Sheet could not be parsed from file [%s]", fileName));
		}
		if (sheet.getPhysicalNumberOfRows() == 0)
		{
			throw new MolgenisDataException(format("Sheet [%s] is empty", sheet.getSheetName()));
		}
		if (sheet.getNumMergedRegions() > 0)
		{
			throw new MolgenisDataException(
					format("Sheet [%s] contains merged regions which is not supported", sheet.getSheetName()));
		}
		return true;
	}

}
