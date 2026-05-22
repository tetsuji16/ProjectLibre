package com.projectlibre1.pm.graphic.spreadsheet.swingx;

import java.awt.event.KeyEvent;

import com.projectlibre1.pm.graphic.spreadsheet.common.CommonSpreadSheet;

public class SpreadsheetKeyController {
	private final SpreadsheetSelectionState selectionState;

	public SpreadsheetKeyController(SpreadsheetSelectionState selectionState) {
		this.selectionState = selectionState;
	}

	public boolean handleBeforeKeyEvent(CommonSpreadSheet table, KeyEvent e) {
		if (e == null || table.isEditing())
			return false;
		if (selectionState.handleArrowKeyNavigation(table, e))
			return true;
		return false;
	}

	public void handleAfterKeyEvent(CommonSpreadSheet table, KeyEvent e) {
	}
}
