package com.projectlibre1.pm.graphic.spreadsheet.swingx;

import com.projectlibre1.pm.graphic.spreadsheet.time.TimeSpreadSheet;
import com.projectlibre1.pm.task.Project;

public class TimeGridSpreadsheetView extends TimeSpreadSheet {
	private static final long serialVersionUID = 1L;

	public TimeGridSpreadsheetView(Project project) {
		super(project);
		setSortable(false);
		setColumnControlVisible(false);
	}
}
