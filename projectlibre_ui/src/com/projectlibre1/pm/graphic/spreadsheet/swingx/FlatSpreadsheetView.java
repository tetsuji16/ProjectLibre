package com.projectlibre1.pm.graphic.spreadsheet.swingx;

import com.projectlibre1.pm.graphic.spreadsheet.SpreadSheet;

public class FlatSpreadsheetView extends SpreadSheet {
	private static final long serialVersionUID = 1L;

	public FlatSpreadsheetView() {
		super();
		setSortable(false);
		setColumnControlVisible(false);
	}
}
