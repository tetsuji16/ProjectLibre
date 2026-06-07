/*******************************************************************************
 * The contents of this file are subject to the Common Public Attribution License
 * Version 1.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.projectlibre.com/license . The License is based on the Mozilla Public
 * License Version 1.1 but Sections 14 and 15 have been added to cover use of
 * software over a computer network and provide for limited attribution for the
 * Original Developer. In addition, Exhibit A has been modified to be consistent
 * with Exhibit B.
 *******************************************************************************/
package com.projectlibre1.dialog.assignment;

import java.awt.Dimension;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JScrollPane;
import javax.swing.JViewport;
import javax.swing.event.ChangeEvent;

import com.projectlibre1.field.Field;
import com.projectlibre1.graphic.configuration.GraphicConfiguration;
import com.projectlibre1.graphic.configuration.SpreadSheetFieldArray;
import com.projectlibre1.grouping.core.Node;
import com.projectlibre1.grouping.core.model.NodeModelDataFactory;
import com.projectlibre1.options.TimesheetOption;
import com.projectlibre1.pm.assignment.Assignment;
import com.projectlibre1.pm.assignment.timesheet.TimesheetAssignment;
import com.projectlibre1.pm.assignment.timesheet.TimesheetStatus;
import com.projectlibre1.pm.graphic.spreadsheet.SpreadSheet;
import com.projectlibre1.pm.graphic.spreadsheet.SpreadSheetModel;
import com.projectlibre1.pm.graphic.spreadsheet.SpreadSheetUtils;
import com.projectlibre1.pm.resource.Resource;
import com.projectlibre1.pm.snapshot.Snapshottable;
import com.projectlibre1.pm.task.Project;

public class TimesheetEntryPane extends JScrollPane {
	private static final long serialVersionUID = 1L;

	public static final String spreadsheetCategory = "timesheetSpreadsheet";

	private final CommonAssignmentDialog dialog;
	private final Project project;
	private final List selectedResources = new ArrayList();
	private final List timesheetAssignments = new ArrayList();
	private SpreadSheet spreadSheet;
	private final Collection editableTimesheetFields;

	private class TimesheetSpreadSheet extends SpreadSheet {
		private static final long serialVersionUID = 1L;

		private TimesheetAssignment getEntryInRow(int row) {
			Node node = ((SpreadSheetModel) getModel()).getNode(row).getNode();
			if (node != null && !node.isVirtual()) {
				return (TimesheetAssignment) node.getImpl();
			}
			return null;
		}

		public void setValueAt(Object aValue, int row, int column) {
			super.setValueAt(aValue, row, column);
			TimesheetAssignment entry = getEntryInRow(row);
			if (entry != null) {
				entry.getAssignment().setTimesheetStatus(TimesheetStatus.ENTERED);
				entry.setDirty(true);
			}
		}

		public java.awt.Component prepareEditor(javax.swing.table.TableCellEditor editor, int row, int column) {
			dialog.setEditorButtonsVisible(true);
			return super.prepareEditor(editor, row, column);
		}

		public void editingCanceled(ChangeEvent e) {
			dialog.setEditorButtonsVisible(false);
			super.editingCanceled(e);
		}

		public void editingStopped(ChangeEvent e) {
			dialog.setEditorButtonsVisible(false);
			super.editingStopped(e);
		}
	}

	public TimesheetEntryPane(CommonAssignmentDialog dialog, Project project) {
		super();
		this.dialog = dialog;
		this.project = project;
		this.editableTimesheetFields = createEditableTimesheetFields();
	}

	public void setSelectedResources(List resources) {
		selectedResources.clear();
		if (resources != null) {
			selectedResources.addAll(resources);
		}
		init();
	}

	public SpreadSheet getSpreadSheet() {
		return spreadSheet;
	}

	private void init() {
		if (project == null) {
			return;
		}
		if (spreadSheet == null) {
			spreadSheet = new TimesheetSpreadSheet();
			spreadSheet.setSpreadSheetCategory(spreadsheetCategory);
			spreadSheet.setCanModifyColumns(false);
			spreadSheet.setCanSelectFieldArray(false);
			spreadSheet.setActions(new String[] {});
			setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		}

		rebuildTimesheetAssignments();
		if (spreadSheet.getModel() == null) {
			SpreadSheetUtils.createCollectionSpreadSheet(
				spreadSheet,
				timesheetAssignments,
				"Text.Timesheet",
				spreadsheetCategory,
				TimesheetOption.getInstance().getTimesheetFieldArrayName(),
				false,
				(NodeModelDataFactory) project,
				0);
		} else {
			SpreadSheetUtils.updateCollectionSpreadSheet(
				spreadSheet,
				timesheetAssignments,
				(NodeModelDataFactory) project,
				0);
		}

		JViewport viewport = createViewport();
		viewport.setView(spreadSheet);
		setViewport(viewport);

		Dimension preferred = spreadSheet.getPreferredSize();
		Dimension enclosing = new Dimension();
		GraphicConfiguration config = GraphicConfiguration.getInstance();
		int rowHeaderWidth = config.getRowHeaderWidth() + spreadSheet.getRowMargin() * 2;
		enclosing.setSize(preferred.getWidth() + rowHeaderWidth, preferred.getHeight());
		viewport.setPreferredSize(enclosing);
		spreadSheet.setEnabled(!timesheetAssignments.isEmpty());
		if (spreadSheet.getRowHeader() != null) {
			spreadSheet.getRowHeader().setEnabled(!timesheetAssignments.isEmpty());
		}
	}

	private void rebuildTimesheetAssignments() {
		timesheetAssignments.clear();
		for (Iterator i = resolveAssignments().iterator(); i.hasNext();) {
			Assignment liveAssignment = (Assignment) i.next();
			Assignment timesheetSnapshot = prepareTimesheetSnapshot(liveAssignment);
			Resource resource = liveAssignment.getResource();
			TimesheetAssignment entry = new TimesheetAssignment(
				liveAssignment.getProjectName(),
				liveAssignment.getTask().getName(),
				liveAssignment.getOwningProject().getUniqueId(),
				liveAssignment.getTask().getUniqueId(),
				resource == null ? 0 : resource.getUniqueId(),
				timesheetSnapshot,
				null);
			timesheetAssignments.add(entry);
		}
		Collections.sort(timesheetAssignments, new Comparator() {
			public int compare(Object o1, Object o2) {
				TimesheetAssignment left = (TimesheetAssignment) o1;
				TimesheetAssignment right = (TimesheetAssignment) o2;
				int byResource = safe(left.getResourceName()).compareToIgnoreCase(safe(right.getResourceName()));
				if (byResource != 0) {
					return byResource;
				}
				int byTask = safe(left.getTaskName()).compareToIgnoreCase(safe(right.getTaskName()));
				if (byTask != 0) {
					return byTask;
				}
				return Long.compare(left.getCachedStart(), right.getCachedStart());
			}
		});
	}

	private List resolveAssignments() {
		List assignments = new ArrayList();
		for (Iterator i = resolveResources().iterator(); i.hasNext();) {
			Resource resource = (Resource) i.next();
			assignments.addAll(resource.getAssignments());
		}
		return assignments;
	}

	private static String safe(String value) {
		return value == null ? "" : value;
	}

	private Assignment prepareTimesheetSnapshot(Assignment liveAssignment) {
		Assignment timesheetSnapshot = liveAssignment.getBaselineAssignment(Snapshottable.TIMESHEET, true);
		timesheetSnapshot.setTimesheetAssignment(true);
		int status = timesheetSnapshot.getTimesheetStatus();
		if (status == TimesheetStatus.NO_DATA || status == TimesheetStatus.INTEGRATED) {
			Field.copyData(editableTimesheetFields, timesheetSnapshot, liveAssignment);
			timesheetSnapshot.setTimesheetStatus(status);
		}
		return timesheetSnapshot;
	}

	private Collection createEditableTimesheetFields() {
		List fields = new LinkedList();
		SpreadSheetFieldArray fieldArray = TimesheetOption.getInstance().getTimesheetFieldArray();
		for (Iterator i = fieldArray.iterator(); i.hasNext();) {
			Field field = (Field) i.next();
			if (!field.isReadOnly()) {
				fields.add(field);
			}
		}
		return fields;
	}

	private List resolveResources() {
		LinkedHashSet uniqueResources = new LinkedHashSet();
		if (!selectedResources.isEmpty()) {
			uniqueResources.addAll(selectedResources);
		} else {
			if (project.getResourcePool() != null) {
				uniqueResources.addAll(project.getResourcePool().getResourceList());
			}
		}
		List result = new ArrayList();
		for (Iterator i = uniqueResources.iterator(); i.hasNext();) {
			Object next = i.next();
			if (next instanceof Resource && !((Resource) next).getAssignments().isEmpty()) {
				result.add(next);
			}
		}
		return result;
	}

	public boolean hasRows() {
		return !timesheetAssignments.isEmpty();
	}

	public boolean applyTimesheet() {
		if (spreadSheet != null && spreadSheet.isEditing() && spreadSheet.getCellEditor() != null) {
			spreadSheet.getCellEditor().stopCellEditing();
		}
		normalizeEnteredTimesheets();
		boolean changed = project.applyTimesheet(editableTimesheetFields, System.currentTimeMillis());
		if (changed) {
			rebuildTimesheetAssignments();
			SpreadSheetUtils.updateCollectionSpreadSheet(
				spreadSheet,
				timesheetAssignments,
				(NodeModelDataFactory) project,
				0);
		}
		return changed;
	}

	private void normalizeEnteredTimesheets() {
		for (Iterator i = timesheetAssignments.iterator(); i.hasNext();) {
			TimesheetAssignment entry = (TimesheetAssignment) i.next();
			Assignment snapshot = entry.getAssignment();
			if (snapshot.getTimesheetStatus() == TimesheetStatus.ENTERED) {
				snapshot.setTimesheetStatus(TimesheetStatus.VALIDATED);
			}
		}
	}
}
