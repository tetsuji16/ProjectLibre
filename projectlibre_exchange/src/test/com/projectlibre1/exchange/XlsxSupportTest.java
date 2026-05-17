package test.com.projectlibre1.exchange;

import java.io.File;

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
}
