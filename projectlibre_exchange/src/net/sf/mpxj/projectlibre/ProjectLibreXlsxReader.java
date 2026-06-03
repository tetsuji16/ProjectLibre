package net.sf.mpxj.projectlibre;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import net.sf.mpxj.MPXJException;
import net.sf.mpxj.ProjectFile;
import net.sf.mpxj.Resource;
import net.sf.mpxj.Task;
import net.sf.mpxj.reader.AbstractProjectReader;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class ProjectLibreXlsxReader extends AbstractProjectReader {
	private static final String TASKS_SHEET = "Tasks";

	public ProjectFile read(String fileName) throws MPXJException {
		return read(new File(fileName));
	}

	public List<ProjectFile> readAll(String fileName) throws MPXJException {
		return Collections.singletonList(read(fileName));
	}

	public ProjectFile read(File file) throws MPXJException {
		try {
			FileInputStream in = new FileInputStream(file);
			try {
				return read(in);
			} finally {
				in.close();
			}
		} catch (Exception e) {
			throw new MPXJException("Failed to read ProjectLibre XLSX file", e);
		}
	}

	public List<ProjectFile> readAll(File file) throws MPXJException {
		return Collections.singletonList(read(file));
	}

	public ProjectFile read(InputStream in) throws MPXJException {
		try {
			XSSFWorkbook workbook = new XSSFWorkbook(in);
			try {
				ProjectFile project = new ProjectFile();
				project.addDefaultBaseCalendar();
				Map<String, Resource> resources = new LinkedHashMap<String, Resource>();
				Sheet sheet = workbook.getSheet(TASKS_SHEET);
				if (sheet == null) {
					throw new MPXJException("Missing Tasks sheet");
				}
				for (int rowIndex = 1; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
					Row row = sheet.getRow(rowIndex);
					if (row == null || isBlank(row)) {
						continue;
					}
					readTask(project, resources, row);
				}
				addListenersToProject(project);
				return project;
			} finally {
				workbook.close();
			}
		} catch (MPXJException e) {
			throw e;
		} catch (Exception e) {
			throw new MPXJException("Failed to read ProjectLibre XLSX stream", e);
		}
	}

	public List<ProjectFile> readAll(InputStream in) throws MPXJException {
		return Collections.singletonList(read(in));
	}

	private void readTask(ProjectFile project, Map<String, Resource> resources, Row row) {
		Task task = project.addTask();
		Integer uniqueId = integerCell(row, 0);
		Integer id = integerCell(row, 1);
		if (uniqueId != null) {
			task.setUniqueID(uniqueId);
		}
		if (id != null) {
			task.setID(id);
		}
		task.setName(stringCell(row, 2));
		task.setNotes(stringCell(row, 3));
		Date start = dateCell(row, 5);
		Date finish = dateCell(row, 6);
		if (start != null) {
			task.setStart(start);
		}
		if (finish != null) {
			task.setFinish(finish);
		}
		Double percentComplete = doubleCell(row, 7);
		if (percentComplete != null) {
			task.setPercentageComplete(percentComplete);
		}
		for (String resourceName : stringCell(row, 4).split(",")) {
			String trimmed = resourceName.trim();
			if (trimmed.length() == 0) {
				continue;
			}
			Resource resource = resources.get(trimmed);
			if (resource == null) {
				resource = project.addResource();
				resource.setName(trimmed);
				resources.put(trimmed, resource);
			}
			task.addResourceAssignment(resource);
		}
	}

	private boolean isBlank(Row row) {
		for (int i = 0; i <= 7; i++) {
			if (stringCell(row, i).length() > 0) {
				return false;
			}
		}
		return true;
	}

	private String stringCell(Row row, int column) {
		Cell cell = row.getCell(column);
		if (cell == null) {
			return "";
		}
		switch (cell.getCellType()) {
		case Cell.CELL_TYPE_NUMERIC:
			double value = cell.getNumericCellValue();
			long asLong = (long) value;
			return Double.compare(value, asLong) == 0 ? String.valueOf(asLong) : String.valueOf(value);
		case Cell.CELL_TYPE_STRING:
			return cell.getStringCellValue();
		case Cell.CELL_TYPE_BOOLEAN:
			return String.valueOf(cell.getBooleanCellValue());
		default:
			return "";
		}
	}

	private Integer integerCell(Row row, int column) {
		Double value = doubleCell(row, column);
		return value == null ? null : Integer.valueOf(value.intValue());
	}

	private Double doubleCell(Row row, int column) {
		Cell cell = row.getCell(column);
		if (cell == null) {
			return null;
		}
		if (cell.getCellType() == Cell.CELL_TYPE_NUMERIC) {
			return Double.valueOf(cell.getNumericCellValue());
		}
		String text = stringCell(row, column);
		if (text.length() == 0) {
			return null;
		}
		return Double.valueOf(text);
	}

	private Date dateCell(Row row, int column) {
		Double value = doubleCell(row, column);
		return value == null || value.longValue() <= 0L ? null : new Date(value.longValue());
	}
}
