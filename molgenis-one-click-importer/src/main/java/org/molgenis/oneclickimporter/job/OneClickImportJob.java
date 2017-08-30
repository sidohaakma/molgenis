package org.molgenis.oneclickimporter.job;

import org.apache.poi.ss.usermodel.Sheet;
import org.molgenis.data.csv.service.CsvService;
import org.molgenis.data.csv.utils.CsvFileExtensions;
import org.molgenis.data.excel.service.ExcelService;
import org.molgenis.data.excel.utils.ExcelFileExtensions;
import org.molgenis.data.jobs.Progress;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.file.FileStore;
import org.molgenis.oneclickimporter.exceptions.UnknownFileTypeException;
import org.molgenis.oneclickimporter.model.DataCollection;
import org.molgenis.oneclickimporter.service.EntityService;
import org.molgenis.oneclickimporter.service.OneClickImporterNamingService;
import org.molgenis.oneclickimporter.service.OneClickImporterService;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.util.List;
import java.util.Map;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Sets.newHashSet;
import static java.util.Objects.requireNonNull;
import static org.molgenis.util.FileExtensionUtils.findExtensionFromPossibilities;

@Component
public class OneClickImportJob
{
	private final ExcelService excelService;
	private final CsvService csvService;
	private final OneClickImporterService oneClickImporterService;
	private final OneClickImporterNamingService oneClickImporterNamingService;
	private final EntityService entityService;
	private final FileStore fileStore;

	public OneClickImportJob(ExcelService excelService, CsvService csvService,
			OneClickImporterService oneClickImporterService,
			OneClickImporterNamingService oneClickImporterNamingService, EntityService entityService,
			FileStore fileStore)
	{
		this.excelService = requireNonNull(excelService);
		this.csvService = requireNonNull(csvService);
		this.oneClickImporterService = requireNonNull(oneClickImporterService);
		this.oneClickImporterNamingService = requireNonNull(oneClickImporterNamingService);
		this.entityService = requireNonNull(entityService);
		this.fileStore = requireNonNull(fileStore);
	}

	@Transactional
	public List<EntityType> getEntityType(Progress progress, String filename) throws UnknownFileTypeException
	{
		File file = fileStore.getFile(filename);
		String fileExtension = findExtensionFromPossibilities(filename,
				newHashSet(CsvFileExtensions.CSV.toString(), ExcelFileExtensions.XLS.toString(),
						ExcelFileExtensions.XLSX.toString(), CsvFileExtensions.ZIP.toString()));

		progress.status("Preparing import");
		List<DataCollection> dataCollections = newArrayList();
		if (fileExtension == null)
		{
			throw new UnknownFileTypeException(
					String.format("File [%s] does not have a valid extension, supported: [csv, xlsx, zip, xls]",
							filename));
		}
		else if (fileExtension.equals(ExcelFileExtensions.XLS.toString()) || fileExtension.equals(
				ExcelFileExtensions.XLSX.toString()))
		{
			List<Sheet> sheets = excelService.buildExcelSheetsFromFile(file);
			dataCollections.addAll(oneClickImporterService.buildDataCollectionsFromExcel(sheets));
		}
		else if (fileExtension.equals(CsvFileExtensions.CSV.toString()) || fileExtension.equals(
				CsvFileExtensions.ZIP.toString()))
		{
			Map<String, List<String[]>> linesOfMultipleFiles = csvService.buildLinesFromFile(file);
			linesOfMultipleFiles.keySet()
								.forEach(key -> dataCollections.add(
										oneClickImporterService.buildDataCollectionFromCsv(key,
												linesOfMultipleFiles.get(key))));
		}

		List<EntityType> entityTypes = newArrayList();
		String packageName = oneClickImporterNamingService.createValidIdFromFileName(filename);
		dataCollections.forEach(dataCollection ->
		{
			progress.status("Importing [" + dataCollection.getName() + "] into package [" + packageName + "]");
			entityTypes.add(entityService.createEntityType(dataCollection, packageName));
		});

		return entityTypes;
	}
}
