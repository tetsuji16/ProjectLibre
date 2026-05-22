package com.projectlibre1.pm.graphic.spreadsheet.swingx;

import java.awt.event.ActionEvent;

import com.projectlibre1.menu.MenuActionConstants;
import com.projectlibre1.pm.graphic.spreadsheet.SpreadSheet;

public class SpreadsheetCommandDispatcher {
	private final SpreadSheet spreadSheet;

	public SpreadsheetCommandDispatcher(SpreadSheet spreadSheet) {
		this.spreadSheet = spreadSheet;
	}

	public void cut() {
		executeAction(MenuActionConstants.ACTION_CUT);
	}

	public void copy() {
		executeAction(MenuActionConstants.ACTION_COPY);
	}

	public void pasteValues() {
		spreadSheet.pasteClipboardAsValues();
	}

	public void insertClipboard() {
		spreadSheet.insertClipboardContents();
	}

	public void executeAction(String actionId) {
		spreadSheet.prepareAction(actionId).actionPerformed(new ActionEvent(
			spreadSheet, ActionEvent.ACTION_PERFORMED, actionId));
	}
}
