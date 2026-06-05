package org.projectlibre.export;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.io.File;

import org.junit.jupiter.api.Test;

public class ImageExportTest {
	@Test
	public void keepsExistingPdfExtension() {
		File input = new File("C:\\temp\\report.pdf");

		File output = ImageExport.appendPdfExtensionIfMissing(input);

		assertEquals(input, output);
	}

	@Test
	public void appendsPdfExtensionWithoutDroppingParentDirectory() {
		File input = new File("C:\\temp\\report");

		File output = ImageExport.appendPdfExtensionIfMissing(input);

		assertEquals(new File("C:\\temp\\report.pdf"), output);
	}

	@Test
	public void returnsNullWhenFileIsNull() {
		assertNull(ImageExport.appendPdfExtensionIfMissing(null));
	}
}
