package org.molgenis.data.excel.service;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.molgenis.data.processor.CellProcessor;
import org.molgenis.util.Pair;

import java.io.File;
import java.io.OutputStream;
import java.util.List;

/**
 * Creates a list of {@link Sheet}s from an excel file
 */
public interface ExcelService
{
	/**
	 * Builds one or more excel sheets based on a xls or xlsx file
	 *
	 * @param file uploaded file
	 * @return list of sheets
	 */
	List<Sheet> buildExcelSheetsFromFile(final File file);

	/**
	 * @param file
	 * @param sheetName
	 * @return
	 */
	Sheet buildExcelSheetFromFile(final File file, String sheetName);

	/**
	 * <p>Determine if filename is of Exceltype</p>
	 *
	 * @param filename filename of uploaded file
	 * @return is of Exceltype
	 */
	boolean isExcelFile(String filename);

	/**
	 * <p>Rename the ExcelSheet to make it possible to upload the same file twice</p>
	 *
	 * @param newSheetname new sheetname
	 * @param file         uploaded file
	 * @param index        number of uploads
	 */
	void renameSheet(String newSheetname, File file, int index);

	/**
	 * @param file
	 * @param index
	 * @return
	 * @deprecated <p>Was used in CsvIterator. Now not used anymore.</p>
	 */
	@Deprecated
	String getSheetName(final File file, int index);

	/**
	 * @param file uploaded file
	 * @return number of sheets
	 */
	int getNumberOfSheets(final File file);

	/**
	 * @param file
	 * @param index
	 * @return
	 */
	int getNumberOfRowsOnSheet(final File file, int index);

	/**
	 * <p>Gets a cell value as String and process the value with the given cellProcessors</p>
	 *
	 * @param cell Excel cell element
	 * @return value from cell in String-format
	 */
	String toValue(Cell cell);

	/**
	 * <p>Parse the sheet header into columns.</p>
	 *
	 * @param sheet      sheet to parse
	 * @param processors {@link CellProcessor}
	 * @return list of columns
	 */
	List<String> parseHeader(Sheet sheet, List<CellProcessor> processors);

	/**
	 * <p>Parse an Excel-Row to a pair of integer and string values.</p>
	 *
	 * @param row        Excel Row element
	 * @param processors {@link CellProcessor}
	 * @return list of {@link Pair}
	 */
	List<Pair<Integer, String>> parse(Row row, List<CellProcessor> processors);

	/**
	 * @param file
	 * @param out
	 * @deprecated <p>Was used in CsvIterator. Now not used anymore.</p>
	 */
	@Deprecated
	void save(final File file, OutputStream out);

}