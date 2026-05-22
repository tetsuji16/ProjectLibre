package com.projectlibre1.pm.graphic.spreadsheet.swingx;

import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.TableColumn;

public class TimeSpreadsheetRowHeaderColumnModel extends DefaultTableColumnModel {
	private int columnIndex;

	public void addColumn(TableColumn tc) {
		if (columnIndex == 0) {
			tc.setMinWidth(40);
			tc.setMaxWidth(40);
			tc.setResizable(false);
			tc.setCellRenderer(new TimeSpreadsheetRowHeaderRenderer());
			tc.setCellEditor(null);
			super.addColumn(tc);
		}
		columnIndex++;
	}
}
