package org.molgenis.data.excel;

import org.apache.commons.io.FileUtils;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.*;
import org.molgenis.data.MolgenisDataException;
import org.molgenis.data.excel.service.ExcelService;
import org.molgenis.data.excel.service.ExcelServiceImpl;
import org.molgenis.util.ResourceUtils;
import org.testng.annotations.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URISyntaxException;

import static org.apache.poi.ss.usermodel.CellType.FORMULA;
import static org.apache.poi.ss.usermodel.CellType.NUMERIC;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

public class ExcelServiceTest
{
	private ExcelService excelService = new ExcelServiceImpl();

	@Test(expectedExceptions = MolgenisDataException.class)
	public void testBuildExcelSheetsWithEmptyFile() throws InvalidFormatException, IOException, URISyntaxException
	{
		excelService.buildExcelSheetsFromFile(ResourceUtils.getFile(getClass(), "/empty-sheet.xlsx"));
	}

	@Test
	public void testBuildExcelSheetsWithHeaderOnly() throws InvalidFormatException, IOException, URISyntaxException
	{
		excelService.buildExcelSheetsFromFile(ResourceUtils.getFile(getClass(), "/header-without-data.xlsx"));
	}

	@Test
	public void testRenameSheetTest() throws IOException, InvalidFormatException
	{
		File file = ResourceUtils.getFile(getClass(), "/test.xls");
		File temp = File.createTempFile("unittest_", ".xls");
		FileUtils.copyFile(file, temp);
		excelService.renameSheet("unittest", temp, 0);
		Workbook workbook = WorkbookFactory.create(new FileInputStream(temp));
		assertEquals(workbook.getSheetAt(0).getSheetName(), "unittest");
	}

	@Test
	public void testGetNumberOfSheetsTest()
	{
		File file = ResourceUtils.getFile(getClass(), "/test.xls");
		assertEquals(excelService.getNumberOfSheets(file), 3);
	}

	@Test
	public void testIsExcelFileTrueXLSX()
	{
		assertEquals(excelService.isExcelFile("test.xlsx"), true);
	}

	@Test
	public void testIsExcelFileTrueXLS()
	{
		assertEquals(excelService.isExcelFile("test.xls"), true);
	}

	@Test
	public void testIsExcelFileFalse()
	{
		assertEquals(excelService.isExcelFile("test.csv"), false);
	}

	// regression test: https://github.com/molgenis/molgenis/issues/6048
	@Test
	public void testToValueNumericLong() throws Exception
	{
		Cell cell = mock(Cell.class);
		when(cell.getCellTypeEnum()).thenReturn(NUMERIC);
		when(cell.getNumericCellValue()).thenReturn(1.2342151234E10);
		assertEquals(excelService.toValue(cell), "12342151234");
	}

	@Test
	public void testToValueFormulaNumericLong() throws Exception
	{
		CellValue cellValue = new CellValue(1.2342151234E10);

		Cell cell = mock(Cell.class);

		FormulaEvaluator formulaEvaluator = mock(FormulaEvaluator.class);
		when(formulaEvaluator.evaluate(cell)).thenReturn(cellValue);

		CreationHelper creationHelper = mock(CreationHelper.class);
		when(creationHelper.createFormulaEvaluator()).thenReturn(formulaEvaluator);

		Workbook workbook = mock(Workbook.class);
		when(workbook.getCreationHelper()).thenReturn(creationHelper);

		Sheet sheet = mock(Sheet.class);
		when(sheet.getWorkbook()).thenReturn(workbook);

		when(cell.getCellTypeEnum()).thenReturn(FORMULA);
		when(cell.getSheet()).thenReturn(sheet);
		when(cell.getNumericCellValue()).thenReturn(1.2342151234E10);
		assertEquals(excelService.toValue(cell), "12342151234");
	}

	@Test
	public void testGetNumberOfRowsOnSheet()
	{
		File file = ResourceUtils.getFile(getClass(), "/test.xls");
		assertEquals(excelService.getNumberOfRowsOnSheet(file, 0), 5);
	}

	@Test
	public void testGetNumberOfSheets()
	{
		File file = ResourceUtils.getFile(getClass(), "/test.xls");
		assertEquals(excelService.getNumberOfSheets(file), 3);
	}

}