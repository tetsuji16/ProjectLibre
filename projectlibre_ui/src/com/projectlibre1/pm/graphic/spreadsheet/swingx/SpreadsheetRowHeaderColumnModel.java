package com.projectlibre1.pm.graphic.spreadsheet.swingx;

import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.TableColumn;

import com.projectlibre1.graphic.configuration.GraphicConfiguration;

public class SpreadsheetRowHeaderColumnModel extends DefaultTableColumnModel {
	private int columnIndex;

	public void addColumn(TableColumn tc) {
		if (columnIndex == 0) {
			int width = GraphicConfiguration.getInstance().getRowHeaderWidth();
			tc.setMinWidth(width);
			tc.setMaxWidth(width);
			tc.setResizable(false);
			tc.setCellRenderer(new SpreadsheetRowHeaderRenderer());
			tc.setCellEditor(null);
			super.addColumn(tc);
		}
		columnIndex++;
	}
}
