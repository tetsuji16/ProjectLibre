package com.projectlibre1.pm.graphic.spreadsheet.swingx;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.SwingUtilities;

import com.projectlibre1.pm.graphic.spreadsheet.SpreadSheet;
import com.projectlibre1.pm.graphic.spreadsheet.SpreadSheetColumnMenu;
import com.projectlibre1.pm.graphic.spreadsheet.common.CommonSpreadSheet;

public class SpreadsheetHeaderMouseListener extends MouseAdapter {
	private final SpreadSheet table;

	public SpreadsheetHeaderMouseListener(SpreadSheet table) {
		this.table = table;
	}

	public void mouseClicked(MouseEvent e) {
		int col = table.columnAtPoint(e.getPoint());
		if (SwingUtilities.isLeftMouseButton(e)) {
			if (isColumnFullySelected(col)) {
				table.clearSelection();
				e.consume();
				return;
			}
			table.getRowHeader().clearSelection();
			if (col >= 0)
				table.getColumnModel().getSelectionModel().setSelectionInterval(col, col);
			if (table.getRowCount() > 0)
				table.getSelectionModel().setSelectionInterval(0, table.getRowCount() - 1);
			return;
		}
		if (SwingUtilities.isRightMouseButton(e)
			&& table.getSpreadSheetCategory() != null
			&& table.isHasColumnHeaderPopup()) {
			SpreadSheetColumnMenu columnsPopup = new SpreadSheetColumnMenu((CommonSpreadSheet)table, col + 1);
			columnsPopup.show(table, e.getX(), e.getY());
		}
	}

	private boolean isColumnFullySelected(int col) {
		if (col < 0)
			return false;
		if (!table.getColumnModel().getSelectionModel().isSelectedIndex(col))
			return false;
		return table.getSelectedColumnCount() == 1 && table.getSelectedRowCount() == table.getRowCount();
	}
}
