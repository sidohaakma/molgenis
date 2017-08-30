package org.molgenis.data.csv;

import org.molgenis.data.Entity;
import org.molgenis.data.csv.service.CsvService;
import org.molgenis.data.csv.service.CsvServiceImpl;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.processor.AbstractCellProcessor;
import org.molgenis.data.processor.CellProcessor;
import org.molgenis.data.support.DynamicEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.*;

/**
 * <p>The CSV Iterator handles single CSV-files. No ZIP-files. It is a low level iterator that handles single files only.</p>
 */
public class CsvIterator implements Iterator<Entity>
{

	private Logger LOG = LoggerFactory.getLogger(CsvIterator.class);

	private final EntityType entityType;
	private final List<CellProcessor> cellProcessors;
	private Map<String, Integer> colNamesMap; // column names index
	private Entity next;

	private Iterator<String[]> csvIterator = null;
	private CsvService csvService = new CsvServiceImpl();

	CsvIterator(File file, String repositoryName, List<CellProcessor> cellProcessors, Character separator)
	{
		this(file, repositoryName, cellProcessors, separator, null);
	}

	CsvIterator(File file, String repositoryName, List<CellProcessor> cellProcessors, Character separator,
			EntityType entityType)
	{
		this.cellProcessors = cellProcessors;
		this.entityType = entityType;
		Map<String, List<String[]>> content = csvService.buildLinesFromFile(file, repositoryName, separator);
		colNamesMap = toColNamesMap(content.get(repositoryName).get(0));
		content.get(repositoryName).remove(0);  // remove header row
		csvIterator = content.get(repositoryName).iterator();
	}

	Map<String, Integer> getColNamesMap()
	{
		return colNamesMap;
	}

	@Override
	public boolean hasNext()
	{
		return csvIterator.hasNext();
	}

	@Override
	public Entity next()
	{
		return get();
	}

	private Entity get()
	{
		if (csvIterator.hasNext())
		{
			String[] values = csvIterator.next();

			if ((values != null) && (values.length >= colNamesMap.size()))
			{
				List<String> valueList = Arrays.asList(values);
				for (int i = 0; i < values.length; ++i)
				{
					// subsequent separators indicate
					// null
					// values instead of empty strings
					String value = values[i].isEmpty() ? null : values[i];
					values[i] = processCell(value, false);
				}

				next = new DynamicEntity(entityType);

				for (String name : colNamesMap.keySet())
				{
					next.set(name, valueList.get(colNamesMap.get(name)));
				}
			}
			else
			{
				next = null;
			}
		}

		return next;
	}

	@Override
	public void remove()
	{
		throw new UnsupportedOperationException();
	}

	private Map<String, Integer> toColNamesMap(String[] headers)
	{
		if ((headers == null) || (headers.length == 0))
		{
			return Collections.emptyMap();
		}

		int capacity = (int) (headers.length / 0.75) + 1;
		Map<String, Integer> columnIdx = new LinkedHashMap<>(capacity);
		for (int i = 0; i < headers.length; ++i)
		{
			String header = processCell(headers[i], true);
			columnIdx.put(header, i);
		}

		return columnIdx;
	}

	private String processCell(String value, boolean isHeader)
	{
		return AbstractCellProcessor.processCell(value, isHeader, cellProcessors);
	}

}
