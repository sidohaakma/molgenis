package org.molgenis.data.csv;

import com.google.common.collect.Iterables;
import org.apache.commons.io.FileUtils;
import org.molgenis.data.AbstractMolgenisSpringTest;
import org.molgenis.data.MolgenisInvalidFormatException;
import org.molgenis.data.meta.model.AttributeFactory;
import org.molgenis.data.meta.model.EntityTypeFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.FileCopyUtils;
import org.testng.annotations.Test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

public class CsvRepositorySourceTest extends AbstractMolgenisSpringTest
{
	@Autowired
	private EntityTypeFactory entityTypeFactory;

	@Autowired
	private AttributeFactory attrMetaFactory;

	@Test
	public void testGetRepositoriesCsv() throws IOException, MolgenisInvalidFormatException
	{
		InputStream in = getClass().getResourceAsStream("/testdata.csv");
		File csvFile = new File(FileUtils.getTempDirectory(), "testdata.csv");
		FileCopyUtils.copy(in, new FileOutputStream(csvFile));

		CsvRepositoryCollection repo = new CsvRepositoryCollection(csvFile);
		repo.setEntityTypeFactory(entityTypeFactory);
		repo.setAttributeFactory(attrMetaFactory);
		assertNotNull(repo.getEntityTypeIds());
		assertEquals(Iterables.size(repo.getEntityTypeIds()), 1);
		assertEquals(Iterables.get(repo.getEntityTypeIds(), 0), "testdata");

	}

	@Test
	public void testGetRepositoriesZip() throws IOException, MolgenisInvalidFormatException
	{
		File zip = File.createTempFile("file", ".zip");
		try (FileOutputStream fos = new FileOutputStream(zip))
		{
			fos.write(new byte[] { 0x50, 0x4B, 0x03, 0x04, 0x14, 0x00, 0x00, 0x00, 0x08, 0x00, 0x32, 0x40, 0x28, 0x42,
					(byte) 0xAC, 0x50, (byte) 0xD0, 0x75, 0x12, 0x00, 0x00, 0x00, 0x14, 0x00, 0x00, 0x00, 0x05, 0x00,
					0x00, 0x00, 0x30, 0x2E, 0x63, 0x73, 0x76, 0x4B, (byte) 0xCE, (byte) 0xCF, 0x31, (byte) 0xD4, 0x01,
					0x12, 0x46, (byte) 0xBC, 0x5C, 0x65, (byte) 0x89, 0x40, 0x26, (byte) 0x90, 0x30, 0x02, 0x00, 0x50,
					0x4B, 0x03, 0x04, 0x14, 0x00, 0x00, 0x00, 0x08, 0x00, 0x39, 0x40, 0x28, 0x42, (byte) 0xB5, 0x61,
					0x7A, (byte) 0xEA, 0x12, 0x00, 0x00, 0x00, 0x14, 0x00, 0x00, 0x00, 0x05, 0x00, 0x00, 0x00, 0x31,
					0x2E, 0x63, 0x73, 0x76, 0x4B, (byte) 0xCE, (byte) 0xCF, 0x31, (byte) 0xD6, 0x01, 0x12, 0x26,
					(byte) 0xBC, 0x5C, 0x65, (byte) 0x89, 0x40, 0x26, (byte) 0x90, 0x30, 0x01, 0x00, 0x50, 0x4B, 0x03,
					0x04, 0x14, 0x00, 0x00, 0x00, 0x08, 0x00, 0x47, 0x40, 0x28, 0x42, 0x60, (byte) 0xBB, (byte) 0xC8,
					(byte) 0xB0, 0x11, 0x00, 0x00, 0x00, 0x13, 0x00, 0x00, 0x00, 0x05, 0x00, 0x00, 0x00, 0x32, 0x2E,
					0x74, 0x73, 0x76, 0x4B, (byte) 0xCE, (byte) 0xCF, 0x31, (byte) 0xE5, 0x04, 0x12, 0x66, 0x5C, 0x65,
					(byte) 0x89, 0x40, 0x16, (byte) 0x90, 0x30, 0x03, 0x00, 0x50, 0x4B, 0x01, 0x02, 0x3F, 0x00, 0x14,
					0x00, 0x00, 0x00, 0x08, 0x00, 0x32, 0x40, 0x28, 0x42, (byte) 0xAC, 0x50, (byte) 0xD0, 0x75, 0x12,
					0x00, 0x00, 0x00, 0x14, 0x00, 0x00, 0x00, 0x05, 0x00, 0x24, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
					0x00, 0x20, 0x20, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x30, 0x2E, 0x63, 0x73, 0x76, 0x0A, 0x00,
					0x20, 0x00, 0x00, 0x00, 0x00, 0x00, 0x01, 0x00, 0x18, 0x00, 0x3C, (byte) 0xC6, (byte) 0x84,
					(byte) 0xFE, 0x6D, (byte) 0xED, (byte) 0xCD, 0x01, 0x7C, 0x51, 0x2B, (byte) 0xD8, 0x6D, (byte) 0xED,
					(byte) 0xCD, 0x01, 0x7C, 0x51, 0x2B, (byte) 0xD8, 0x6D, (byte) 0xED, (byte) 0xCD, 0x01, 0x50, 0x4B,
					0x01, 0x02, 0x3F, 0x00, 0x14, 0x00, 0x00, 0x00, 0x08, 0x00, 0x39, 0x40, 0x28, 0x42, (byte) 0xB5,
					0x61, 0x7A, (byte) 0xEA, 0x12, 0x00, 0x00, 0x00, 0x14, 0x00, 0x00, 0x00, 0x05, 0x00, 0x24, 0x00,
					0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x20, 0x20, 0x00, 0x00, 0x35, 0x00, 0x00, 0x00, 0x31, 0x2E,
					0x63, 0x73, 0x76, 0x0A, 0x00, 0x20, 0x00, 0x00, 0x00, 0x00, 0x00, 0x01, 0x00, 0x18, 0x00,
					(byte) 0xD0, (byte) 0xB1, 0x53, 0x07, 0x6E, (byte) 0xED, (byte) 0xCD, 0x01, (byte) 0xF1,
					(byte) 0xF6, (byte) 0xE0, (byte) 0xE1, 0x6D, (byte) 0xED, (byte) 0xCD, 0x01, (byte) 0xF1,
					(byte) 0xF6, (byte) 0xE0, (byte) 0xE1, 0x6D, (byte) 0xED, (byte) 0xCD, 0x01, 0x50, 0x4B, 0x01, 0x02,
					0x3F, 0x00, 0x14, 0x00, 0x00, 0x00, 0x08, 0x00, 0x47, 0x40, 0x28, 0x42, 0x60, (byte) 0xBB,
					(byte) 0xC8, (byte) 0xB0, 0x11, 0x00, 0x00, 0x00, 0x13, 0x00, 0x00, 0x00, 0x05, 0x00, 0x24, 0x00,
					0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x20, 0x20, 0x00, 0x00, 0x6A, 0x00, 0x00, 0x00, 0x32, 0x2E,
					0x74, 0x73, 0x76, 0x0A, 0x00, 0x20, 0x00, 0x00, 0x00, 0x00, 0x00, 0x01, 0x00, 0x18, 0x00, 0x58,
					0x5C, (byte) 0xB9, 0x15, 0x6E, (byte) 0xED, (byte) 0xCD, 0x01, (byte) 0xA0, 0x04, 0x36, (byte) 0xE3,
					0x6D, (byte) 0xED, (byte) 0xCD, 0x01, (byte) 0xA0, 0x04, 0x36, (byte) 0xE3, 0x6D, (byte) 0xED,
					(byte) 0xCD, 0x01, 0x50, 0x4B, 0x05, 0x06, 0x00, 0x00, 0x00, 0x00, 0x03, 0x00, 0x03, 0x00, 0x05,
					0x01, 0x00, 0x00, (byte) 0x9E, 0x00, 0x00, 0x00, 0x00, 0x00 });
		}

		CsvRepositoryCollection repo = new CsvRepositoryCollection(zip);
		repo.setEntityTypeFactory(entityTypeFactory);
		repo.setAttributeFactory(attrMetaFactory);
		assertNotNull(repo.getEntityTypeIds());
		assertEquals(Iterables.size(repo.getEntityTypeIds()), 3);
		assertNotNull(repo.getRepository("0"));
		assertNotNull(repo.getRepository("1"));

	}

	@Test
	public void testIteratorFromZipFile() throws IOException
	{
		File zipFile = createTmpFileForResource("zipFile.zip");

		CsvRepositoryCollection repo = new CsvRepositoryCollection(zipFile);
		repo.setEntityTypeFactory(entityTypeFactory);
		repo.setAttributeFactory(attrMetaFactory);
		assertNotNull(repo.getRepository("testData"));

	}

	@Test
	public void testIteratorFromZipFileWithFolder() throws IOException
	{
		File zipFile = createTmpFileForResource("zipFileWithFolder.zip");

		CsvRepositoryCollection repo = new CsvRepositoryCollection(zipFile);
		repo.setEntityTypeFactory(entityTypeFactory);
		repo.setAttributeFactory(attrMetaFactory);
		assertNotNull(repo.getRepository("testData"));

	}

	@Test
	public void testIteratorFromZipFileWithBom() throws IOException
	{
		File zipFile = createTmpFileForResource("zipFileWithBom.zip");

		CsvRepositoryCollection repo = new CsvRepositoryCollection(zipFile);
		repo.setEntityTypeFactory(entityTypeFactory);
		repo.setAttributeFactory(attrMetaFactory);
		assertNotNull(repo.getRepository("testDataWithBom"));
	}

	@Test
	public void testIteratorFromZipFileWithFolderWithBom() throws IOException
	{
		File zipFile = createTmpFileForResource("zipFileWithFolderWithBom.zip");

		CsvRepositoryCollection repo = new CsvRepositoryCollection(zipFile);
		repo.setEntityTypeFactory(entityTypeFactory);
		repo.setAttributeFactory(attrMetaFactory);
		assertNotNull(repo.getRepository("testDataWithBom"));
	}

	private File createTmpFileForResource(String fileName) throws IOException
	{
		InputStream in = getClass().getResourceAsStream("/" + fileName);
		File csvFile = new File(FileUtils.getTempDirectory(), fileName);
		FileCopyUtils.copy(in, new FileOutputStream(csvFile));
		return csvFile;
	}

}
