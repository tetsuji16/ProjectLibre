package test.com.projectlibre1.exchange;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;

import junit.framework.TestCase;
import net.sf.mpxj.ProjectFile;
import net.sf.mpxj.Task;
import net.sf.mpxj.writer.ProjectWriter;
import net.sf.mpxj.writer.ProjectWriterUtility;

import com.projectlibre.core.pm.exchange.MspImporter;
import com.projectlibre1.collaboration.CollaborationMetadataStore;
import com.projectlibre1.session.FileHelper;

public class XlsxSupportTest extends TestCase {
	public void testFileHelperAcceptsXlsx() {
		assertTrue(FileHelper.isFileNameAllowed("plan.xlsx", true));
		assertTrue(FileHelper.isFileNameAllowed("plan.xlsx", false));
		assertEquals(FileHelper.MSP_FILE_TYPE, FileHelper.getFileType("plan.xlsx"));
	}

	public void testCollaborationRecognizesXlsx() {
		assertTrue(CollaborationMetadataStore.isCollaborationCandidate("plan.xlsx"));
	}

	public void testProjectWriterUtilitySupportsXlsx() throws Exception {
		ProjectWriter writer = ProjectWriterUtility.getProjectWriter("plan.xlsx");
		assertNotNull(writer);
	}

	public void testMspImporterCanReadGeneratedXlsx() throws Exception {
		File tempFile = File.createTempFile("projectlibre-xlsx-import", ".xlsx");
		tempFile.deleteOnExit();

		ProjectFile file = new ProjectFile();
		file.addDefaultBaseCalendar();
		Task task = file.addTask();
		task.setName("Imported Task");

		ProjectWriter writer = ProjectWriterUtility.getProjectWriter(tempFile.getAbsolutePath());
		writer.write(file, tempFile);

		MspImporter importer = new MspImporter();
		com.projectlibre.pm.tasks.Project imported = importer.importProject(tempFile.getAbsolutePath(), new MspImporter.ProgressClosure() {
			@Override
			public void updateProgress(float progress, String label) {
			}
		});

		assertNotNull(imported);
	}

	public void testMspImporterTreatsXmlContentWithXlsxExtensionAsXml() throws Exception {
		File tempFile = File.createTempFile("projectlibre-xlsx-xml-fallback", ".xlsx");
		tempFile.deleteOnExit();

		String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n"
			+ "<Project xmlns=\"http://schemas.microsoft.com/project\">"
			+ "<Name>Fallback</Name>"
			+ "<Tasks>"
			+ "<Task><UID>0</UID><ID>0</ID><Name>Project Summary</Name><Summary>1</Summary></Task>"
			+ "<Task><UID>1</UID><ID>1</ID><Name>XML Task</Name></Task>"
			+ "</Tasks>"
			+ "</Project>";
		FileOutputStream out = new FileOutputStream(tempFile);
		try {
			out.write(xml.getBytes(StandardCharsets.UTF_8));
		} finally {
			out.close();
		}

		MspImporter importer = new MspImporter();
		Method prepare = MspImporter.class.getDeclaredMethod("prepareProjectStream", InputStream.class);
		prepare.setAccessible(true);
		Method normalize = MspImporter.class.getDeclaredMethod("normalizeExtension", String.class, InputStream.class);
		normalize.setAccessible(true);

		InputStream in = null;
		try {
			in = (InputStream) prepare.invoke(importer, new java.io.FileInputStream(tempFile));
			String normalized = (String) normalize.invoke(importer, "xlsx", in);
			assertEquals("xml", normalized);
		} finally {
			if (in != null) {
				in.close();
			}
		}
	}
}
