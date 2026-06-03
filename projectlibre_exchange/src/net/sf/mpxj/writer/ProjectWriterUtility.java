package net.sf.mpxj.writer;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import net.sf.mpxj.json.JsonWriter;
import net.sf.mpxj.mpx.MPXWriter;
import net.sf.mpxj.mspdi.MSPDIWriter;
import net.sf.mpxj.planner.PlannerWriter;
import net.sf.mpxj.primavera.PrimaveraPMFileWriter;
import net.sf.mpxj.projectlibre.ProjectLibreXlsxWriter;
import net.sf.mpxj.sdef.SDEFWriter;

public final class ProjectWriterUtility {
	private static final Map<String, Class<? extends ProjectWriter>> WRITER_MAP = new HashMap<String, Class<? extends ProjectWriter>>();

	static {
		WRITER_MAP.put("MPX", MPXWriter.class);
		WRITER_MAP.put("XML", MSPDIWriter.class);
		WRITER_MAP.put("PMXML", PrimaveraPMFileWriter.class);
		WRITER_MAP.put("PLANNER", PlannerWriter.class);
		WRITER_MAP.put("JSON", JsonWriter.class);
		WRITER_MAP.put("SDEF", SDEFWriter.class);
		WRITER_MAP.put("XLSX", ProjectLibreXlsxWriter.class);
	}

	private ProjectWriterUtility() {
	}

	public static ProjectWriter getProjectWriter(String fileName) throws InstantiationException, IllegalAccessException {
		int extensionPosition = fileName.lastIndexOf('.');
		if (extensionPosition == -1) {
			throw new IllegalArgumentException("Filename has no extension: " + fileName);
		}
		String extension = fileName.substring(extensionPosition + 1).toUpperCase();
		Class<? extends ProjectWriter> writerClass = WRITER_MAP.get(extension);
		if (writerClass == null) {
			throw new IllegalArgumentException("Cannot write files of type: " + fileName);
		}
		return writerClass.newInstance();
	}

	public static Set<String> getSupportedFileExtensions() {
		return WRITER_MAP.keySet();
	}
}
