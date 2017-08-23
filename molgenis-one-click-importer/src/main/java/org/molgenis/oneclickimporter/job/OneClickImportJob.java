package org.molgenis.oneclickimporter.job;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Sheet;
import org.molgenis.data.csv.services.CsvService;
import org.molgenis.data.csv.utils.CsvFileExtensions;
import org.molgenis.data.excel.ExcelFileExtensions;
import org.molgenis.data.jobs.Progress;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.file.FileStore;
import org.molgenis.oneclickimporter.exceptions.EmptySheetException;
import org.molgenis.oneclickimporter.exceptions.UnknownFileTypeException;
import org.molgenis.oneclickimporter.model.DataCollection;
import org.molgenis.oneclickimporter.service.EntityService;
import org.molgenis.oneclickimporter.service.ExcelService;
import org.molgenis.oneclickimporter.service.OneClickImporterNamingService;
import org.molgenis.oneclickimporter.service.OneClickImporterService;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.IOException;
import java.util.List;

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
	public List<EntityType> getEntityType(Progress progress, String filename)
			throws UnknownFileTypeException, IOException, InvalidFormatException, EmptySheetException
	{
		File file = fileStore.getFile(filename);
		String fileExtension = findExtensionFromPossibilities(filename, newHashSet("csv", "xlsx", "zip", "xls"));

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
		else if (fileExtension.equals(CsvFileExtensions.CSV) || fileExtension.equals(CsvFileExtensions.ZIP))
		{
			List<String[]> lines = csvService.buildLinesFromFile(file);
			dataCollections.add(oneClickImporterService.buildDataCollectionFromCsv(
					oneClickImporterNamingService.createValidIdFromFileName(filename), lines));
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
