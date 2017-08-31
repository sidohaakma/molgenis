package org.molgenis.data.excel.service;

import com.google.common.collect.Streams;
import org.apache.commons.io.FilenameUtils;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.util.LocaleUtil;
import org.molgenis.data.MolgenisDataException;
import org.molgenis.data.excel.utils.ExcelFileExtensions;
import org.molgenis.data.processor.AbstractCellProcessor;
import org.molgenis.data.processor.CellProcessor;
import org.molgenis.util.Pair;
import org.springframework.stereotype.Component;

import java.io.*;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.google.common.collect.Lists.newArrayList;
import static java.lang.String.format;
import static java.time.ZoneOffset.UTC;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.*;
import static org.molgenis.data.excel.utils.ExcelValidation.validateExcelSheets;

@Component
public class ExcelServiceImpl implements ExcelService
{
	@Override
	public List<Sheet> buildExcelSheetsFromFile(final File file)
	{
		try (Workbook workbook = WorkbookFactory.create(file))
		{
			List<Sheet> sheets = newArrayList();
			workbook.sheetIterator().forEachRemaining(sheet ->
			{
				if (validateExcelSheets(sheet, file.getName()))
				{
					sheets.add(sheet);
				}
			});
			return sheets;
		}
		catch (IOException err)
		{
			throw new MolgenisDataException(format("File not found [%s]", file.getName()));
		}
		catch (InvalidFormatException err)
		{
			throw new MolgenisDataException(format("No valid filetype uploaded [%s]", file.getName()));
		}

	}

	@Override
	public Sheet buildExcelSheetFromFile(final File file, String sheetName)
	{
		Sheet sheet;
		try (Workbook workbook = WorkbookFactory.create(file))
		{
			sheet = workbook.getSheet(sheetName);
			validateExcelSheets(sheet, file.getName());
		}
		catch (IOException err)
		{
			throw new MolgenisDataException(format("File not found [%s]", file.getName()));
		}
		catch (InvalidFormatException err)
		{
			throw new MolgenisDataException(format("No valid file type uploaded [%s]", file.getName()));
		}
		return sheet;
	}

	@Override
	public boolean isExcelFile(String filename)
	{
		String extension = FilenameUtils.getExtension(filename);
		if (ExcelFileExtensions.getExcel().contains(extension))
		{
			return true;
		}
		return false;
	}

	@Override
	public void renameSheet(String newSheetname, File file, int index)
	{
		try (FileInputStream fis = new FileInputStream(file); Workbook workbook = WorkbookFactory.create(fis))
		{
			workbook.setSheetName(index, newSheetname);
			workbook.write(new FileOutputStream(file));
		}
		catch (Exception e)
		{
			throw new MolgenisDataException(e);
		}
	}

	@Override
	public int getNumberOfRowsOnSheet(final File file, int index)
	{
		int rowNumbers;
		try (Workbook workbook = WorkbookFactory.create(file))
		{
			rowNumbers = workbook.getSheetAt(index).getPhysicalNumberOfRows();
		}
		catch (IOException err)
		{
			throw new MolgenisDataException(format("File not found [%s]", file.getName()));
		}
		catch (InvalidFormatException err)
		{
			throw new MolgenisDataException(format("Invalid file type uploaded [%s]", file.getName()));
		}
		return rowNumbers;
	}

	public String getSheetName(final File file, int index)
	{
		String sheetName;
		try (Workbook workbook = WorkbookFactory.create(file))
		{
			sheetName = workbook.getSheetName(index);
		}
		catch (IOException err)
		{
			throw new MolgenisDataException(format("No file found [%s]", file.getName()));
		}
		catch (InvalidFormatException err)
		{
			throw new MolgenisDataException(format("Invalid file uploaded [%s]", file.getName()));
		}
		return sheetName;
	}

	@Override
	public int getNumberOfSheets(final File file)
	{
		try (Workbook workbook = WorkbookFactory.create(file))
		{
			return workbook.getNumberOfSheets();
		}
		catch (IOException err)
		{
			throw new MolgenisDataException(format("No file found [%s]", file.getName()));
		}
		catch (InvalidFormatException err)
		{
			throw new MolgenisDataException(format("Invalid file uploaded [%s]", file.getName()));
		}
	}

	@Override
	public String toValue(Cell cell)
	{
		String value;
		switch (cell.getCellTypeEnum())
		{
			case BLANK:
				value = null;
				break;
			case STRING:
				value = cell.getStringCellValue();
				break;
			case NUMERIC:
				if (DateUtil.isCellDateFormatted(cell))
				{
					try
					{
						// Excel dates are LocalDateTime, stored without timezone.
						// Interpret them as UTC to prevent ambiguous DST overlaps which happen in other timezones.
						LocaleUtil.setUserTimeZone(LocaleUtil.TIMEZONE_UTC);
						Date dateCellValue = cell.getDateCellValue();
						value = formatUTCDateAsLocalDateTime(dateCellValue);
					}
					finally
					{
						LocaleUtil.resetUserTimeZone();
					}
				}
				else
				{
					// excel stores integer values as double values
					// read an integer if the double value equals the
					// integer value
					double x = cell.getNumericCellValue();
					if (x == Math.rint(x) && !Double.isNaN(x) && !Double.isInfinite(x))
						value = String.valueOf((long) x);
					else value = String.valueOf(x);
				}
				break;
			case BOOLEAN:
				value = String.valueOf(cell.getBooleanCellValue());
				break;
			case FORMULA:
				// evaluate formula
				FormulaEvaluator evaluator = cell.getSheet().getWorkbook().getCreationHelper().createFormulaEvaluator();
				CellValue cellValue = evaluator.evaluate(cell);
				switch (cellValue.getCellTypeEnum())
				{
					case BOOLEAN:
						value = String.valueOf(cellValue.getBooleanValue());
						break;
					case NUMERIC:
						if (DateUtil.isCellDateFormatted(cell))
						{
							try
							{
								// Excel dates are LocalDateTime, stored without timezone.
								// Interpret them as UTC to prevent ambiguous DST overlaps which happen in other timezones.
								LocaleUtil.setUserTimeZone(LocaleUtil.TIMEZONE_UTC);
								Date javaDate = DateUtil.getJavaDate(cellValue.getNumberValue(), false);
								value = formatUTCDateAsLocalDateTime(javaDate);

							}
							finally
							{
								LocaleUtil.resetUserTimeZone();
							}
						}
						else
						{
							// excel stores integer values as double values
							// read an integer if the double value equals the
							// integer value
							double x = cellValue.getNumberValue();
							if (x == Math.rint(x) && !Double.isNaN(x) && !Double.isInfinite(x))
								value = String.valueOf((long) x);
							else value = String.valueOf(x);
						}
						break;
					case STRING:
						value = cellValue.getStringValue();
						break;
					case BLANK:
						value = null;
						break;
					default:
						throw new MolgenisDataException("unsupported cell type: " + cellValue.getCellTypeEnum());
				}
				break;
			default:
				throw new MolgenisDataException("unsupported cell type: " + cell.getCellTypeEnum());
		}

		return value;
	}

	@Override
	public void save(File file, OutputStream out)
	{
		try (Workbook workbook = WorkbookFactory.create(file))
		{
			workbook.write(out);
		}
		catch (IOException err)
		{
			throw new MolgenisDataException(format("No file found [%s]", file.getName()));
		}
		catch (InvalidFormatException err)
		{
			throw new MolgenisDataException(format("Invalid file uploaded [%s]", file.getName()));
		}
	}

	@Override
	public List<Pair<Integer, String>> parse(Row row, List<CellProcessor> processors)
	{
		return Streams.stream(row.cellIterator())
					  .map(cell -> new Pair<>(cell.getColumnIndex(), this.parseDataCell(cell, processors)))
					  .collect(Collectors.toList());
	}

	private String parseDataCell(Cell cell, List<CellProcessor> processors)
	{
		return parseCell(cell, processors, false);
	}

	private String parseCell(Cell cell, List<CellProcessor> processors, boolean isHeader)
	{
		try
		{
			String value = toValue(cell);
			AbstractCellProcessor.processCell(value, isHeader, processors);
			return value;
		}
		catch (Exception err)
		{
			final int rowIndex = cell.getRowIndex() + 1;
			final String column = CellReference.convertNumToColString(cell.getColumnIndex());
			throw new MolgenisDataException(
					format("Invalid value at [%s!%s%d]", cell.getSheet().getSheetName(), column, rowIndex));
		}
	}

	@Override
	public List<String> parseHeader(Sheet sheet, List<CellProcessor> processors)
	{
		if (sheet.getPhysicalNumberOfRows() == 0)
		{
			throw new MolgenisDataException(format("Sheet [%s] is empty", sheet.getSheetName()));
		}
		List<String> parsedHeaders = Streams.stream(sheet.getRow(0).cellIterator())
											.map(cell -> parseCell(cell, processors, true))
											.collect(Collectors.toList());

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
					format("Sheet [%s] has duplicate columns %s", sheet.getSheetName(), duplicateHeaders.toString()));
		}
		return parsedHeaders;
	}

	/**
	 * Formats parsed Date as LocalDateTime string at zone UTC to express that we don't know the timezone.
	 *
	 * @param javaDate Parsed Date representing start of day in UTC
	 * @return Formatted {@link LocalDateTime} string of the java.util.Date
	 */

	private static String formatUTCDateAsLocalDateTime(Date javaDate)
	{
		String value;// Now back from start of day in UTC to LocalDateTime to express that we don't know the timezone.
		LocalDateTime localDateTime = javaDate.toInstant().atZone(UTC).toLocalDateTime();
		// And format to string
		value = localDateTime.toString();
		return value;
	}

}
