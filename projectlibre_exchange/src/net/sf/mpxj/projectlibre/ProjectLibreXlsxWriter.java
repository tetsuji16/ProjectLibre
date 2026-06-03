package net.sf.mpxj.projectlibre;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Date;

import net.sf.mpxj.ProjectFile;
import net.sf.mpxj.ResourceAssignment;
import net.sf.mpxj.Task;
import net.sf.mpxj.writer.ProjectWriter;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class ProjectLibreXlsxWriter implements ProjectWriter {
	private static final String TASKS_SHEET = "Tasks";

	public void write(ProjectFile projectFile, String fileName) throws IOException {
		write(projectFile, new File(fileName));
	}

	public void write(ProjectFile projectFile, File file) throws IOException {
		FileOutputStream out = new FileOutputStream(file);
		try {
			write(projectFile, out);
		} finally {
			out.close();
		}
	}

	public void write(ProjectFile projectFile, OutputStream out) throws IOException {
		XSSFWorkbook workbook = new XSSFWorkbook();
		try {
			Sheet sheet = workbook.createSheet(TASKS_SHEET);
			writeHeader(sheet.createRow(0));
			int rowIndex = 1;
			for (Task task : projectFile.getTasks()) {
				if (task == null || task.getNull()) {
					continue;
				}
				writeTask(sheet.createRow(rowIndex++), task);
			}
			for (int i = 0; i < 8; i++) {
				sheet.autoSizeColumn(i);
			}
			workbook.write(out);
		} finally {
			workbook.close();
		}
	}

	public void writeProjectLibreProject(com.projectlibre1.pm.task.Project project, OutputStream out) throws IOException {
		XSSFWorkbook workbook = new XSSFWorkbook();
		try {
			Sheet sheet = workbook.createSheet(TASKS_SHEET);
			writeHeader(sheet.createRow(0));
			int rowIndex = 1;
			for (Object value : project.getTasks()) {
				if (!(value instanceof com.projectlibre1.pm.task.Task)) {
					continue;
				}
				writeProjectLibreTask(sheet.createRow(rowIndex++), (com.projectlibre1.pm.task.Task) value);
			}
			for (int i = 0; i < 8; i++) {
				sheet.autoSizeColumn(i);
			}
			workbook.write(out);
		} finally {
			workbook.close();
		}
	}

	private void writeHeader(Row row) {
		row.createCell(0).setCellValue("UniqueID");
		row.createCell(1).setCellValue("ID");
		row.createCell(2).setCellValue("Name");
		row.createCell(3).setCellValue("Notes");
		row.createCell(4).setCellValue("ResourceNames");
		row.createCell(5).setCellValue("Start");
		row.createCell(6).setCellValue("Finish");
		row.createCell(7).setCellValue("PercentComplete");
	}

	private void writeTask(Row row, Task task) {
		if (task.getUniqueID() != null) {
			row.createCell(0).setCellValue(task.getUniqueID().intValue());
		}
		if (task.getID() != null) {
			row.createCell(1).setCellValue(task.getID().intValue());
		}
		row.createCell(2).setCellValue(safe(task.getName()));
		row.createCell(3).setCellValue(safe(task.getNotes()));
		row.createCell(4).setCellValue(resourceNames(task));
		writeDate(row, 5, task.getStart());
		writeDate(row, 6, task.getFinish());
		if (task.getPercentageComplete() != null) {
			row.createCell(7).setCellValue(task.getPercentageComplete().doubleValue());
		}
	}

	private void writeProjectLibreTask(Row row, com.projectlibre1.pm.task.Task task) {
		row.createCell(0).setCellValue(task.getUniqueId());
		row.createCell(1).setCellValue(task.getId());
		row.createCell(2).setCellValue(safe(task.getName()));
		row.createCell(3).setCellValue(safe(task.getNotes()));
		row.createCell(4).setCellValue(projectLibreResourceNames(task));
		writeMillis(row, 5, task.getStart());
		writeMillis(row, 6, task.getEnd());
		row.createCell(7).setCellValue(task.getPercentComplete());
	}

	private void writeDate(Row row, int column, Date value) {
		if (value != null) {
			row.createCell(column).setCellValue(value.getTime());
		}
	}

	private void writeMillis(Row row, int column, long value) {
		if (value > 0L) {
			row.createCell(column).setCellValue(value);
		}
	}

	private String resourceNames(Task task) {
		StringBuilder names = new StringBuilder();
		for (ResourceAssignment assignment : task.getResourceAssignments()) {
			if (assignment.getResource() == null || assignment.getResource().getName() == null) {
				continue;
			}
			if (names.length() > 0) {
				names.append(",");
			}
			names.append(assignment.getResource().getName());
		}
		return names.toString();
	}

	private String projectLibreResourceNames(com.projectlibre1.pm.task.Task task) {
		if (task instanceof com.projectlibre1.pm.task.NormalTask) {
			String names = ((com.projectlibre1.pm.task.NormalTask) task).getResourceNames();
			return names == null ? "" : names;
		}
		return "";
	}

	private String safe(String value) {
		return value == null ? "" : value;
	}
}
