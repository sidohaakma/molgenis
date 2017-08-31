package org.molgenis.data.excel;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.CellReference;
import org.molgenis.data.Entity;
import org.molgenis.data.excel.service.ExcelService;
import org.molgenis.data.excel.service.ExcelServiceImpl;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.processor.AbstractCellProcessor;
import org.molgenis.data.processor.CellProcessor;
import org.springframework.util.LinkedCaseInsensitiveMap;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class ExcelIterator implements Iterator<Entity>
{

	private final EntityType entityType;
	private final List<CellProcessor> cellProcessors;
	private final Map<String, Integer> colNamesMap; // column names index
	private Entity next;

	private Iterator<Row> rowIterator = null;

	private ExcelService excelService = new ExcelServiceImpl();

	ExcelIterator(Sheet sheet, List<CellProcessor> cellProcessors)
	{
		this(sheet, cellProcessors, null);
	}

	ExcelIterator(Sheet sheet, List<CellProcessor> cellProcessors, EntityType entityType)
	{
		this.cellProcessors = cellProcessors;
		this.entityType = entityType;
		this.colNamesMap = toColNamesMap(sheet);
		this.rowIterator = sheet.iterator();
	}

	@Override
	public boolean hasNext()
	{
		return rowIterator.hasNext();
	}

	public Entity get()
	{
		if (rowIterator.hasNext())
		{
			Row row = rowIterator.next();
			if (row.getRowNum() == 0)
			{
				if (rowIterator.hasNext())
				{
					row = rowIterator.next();
				}
			}
			if (row != null)
			{
				next = new ExcelEntity(row, colNamesMap, cellProcessors, entityType);
			}
			else
			{
				next = null;
			}
		}
		return next;
	}

	@Override
	public Entity next()
	{
		return get();
	}

	@Override
	public void remove()
	{
		throw new UnsupportedOperationException();
	}

	public Map<String, Integer> getColNamesMap()
	{
		return colNamesMap;
	}

	private Map<String, Integer> toColNamesMap(Sheet sheet)
	{
		Map<String, Integer> columnIdx = new LinkedCaseInsensitiveMap<>();

		Row row = sheet.getRow(0);

		for (int index = 0; index < row.getPhysicalNumberOfCells(); index++)
		{
			try
			{
				String header = processCell(excelService.toValue(row.getCell(index)), true);
				if (null != header)
				{
					columnIdx.put(header, index);
				}
			}
			catch (final IllegalStateException ex)
			{
				final int rowIndex = row.getRowNum();
				final String column = CellReference.convertNumToColString(index);
				throw new IllegalStateException("Invalid value at [" + "" + "] " + column + rowIndex + 1, ex);
			}
		}
		return columnIdx;
	}

	private String processCell(String value, boolean isHeader)
	{
		return AbstractCellProcessor.processCell(value, isHeader, cellProcessors);
	}

}
