package com.projectlibre1.pm.graphic.spreadsheet.swingx;

import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.AbstractAction;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableColumnModel;

import com.projectlibre1.pm.graphic.spreadsheet.SpreadSheet;
import com.projectlibre1.pm.graphic.spreadsheet.SpreadSheetPopupMenu;
import com.projectlibre1.pm.graphic.spreadsheet.common.CommonSpreadSheet;
import com.projectlibre1.pm.graphic.spreadsheet.common.CommonSpreadSheetModel;
import com.projectlibre1.pm.graphic.spreadsheet.common.CommonTable;
import com.projectlibre1.graphic.configuration.shape.Colors;

public class SpreadsheetRowHeaderView extends CommonTable {
	protected final CommonSpreadSheet table;

	public SpreadsheetRowHeaderView(CommonSpreadSheet table) {
		this.table = table;
		setGridColor(Colors.GRAY);
		configureClipboardBindings();
	}

	public void setModel(CommonSpreadSheetModel spreadSheetModel, DefaultTableColumnModel spreadSheetColumnModel) {
		setModel(spreadSheetModel);
		setColumnModel(spreadSheetColumnModel);
		setAutoResizeMode(AUTO_RESIZE_OFF);
		installMouseBehavior();
	}

	public void changeSelection(int rowIndex, int columnIndex, boolean toggle, boolean extend) {
		changeSelection(rowIndex, columnIndex, toggle, extend, true);
	}

	public void changeSelection(int rowIndex, int columnIndex, boolean toggle, boolean extend, boolean forwards) {
		boolean clearTable = (getSelectedRowCount() == 0);
		super.changeSelection(rowIndex, columnIndex, toggle, extend);
		if (!forwards)
			return;
		table.finishCurrentOperations();
		if (clearTable)
			table.changeSelection(rowIndex, columnIndex, false, false, false);
		else
			table.changeSelection(rowIndex, columnIndex, toggle, extend, false);
		if (table.getColumnCount() > 0)
			table.getColumnModel().getSelectionModel().addSelectionInterval(0, table.getColumnCount() - 1);
	}

	public CommonSpreadSheet getSpreadSheet() {
		return table;
	}

	private void configureClipboardBindings() {
		if (!(table instanceof SpreadSheet))
			return;
		final SpreadsheetCommandDispatcher dispatcher = ((SpreadSheet)table).getCommandDispatcher();
		getActionMap().put("cut", new AbstractAction() {
			public void actionPerformed(java.awt.event.ActionEvent e) {
				dispatcher.cut();
			}
		});
		getActionMap().put("copy", new AbstractAction() {
			public void actionPerformed(java.awt.event.ActionEvent e) {
				dispatcher.copy();
			}
		});
		getActionMap().put("paste", new AbstractAction() {
			public void actionPerformed(java.awt.event.ActionEvent e) {
				dispatcher.pasteValues();
			}
		});
		getActionMap().put("insertClipboard", new AbstractAction() {
			public void actionPerformed(java.awt.event.ActionEvent e) {
				dispatcher.insertClipboard();
			}
		});
		InputMap inputMap = getInputMap(JComponent.WHEN_FOCUSED);
		inputMap.put(KeyStroke.getKeyStroke("ctrl X"), "cut");
		inputMap.put(KeyStroke.getKeyStroke("ctrl C"), "copy");
		inputMap.put(KeyStroke.getKeyStroke("ctrl V"), "paste");
		inputMap.put(KeyStroke.getKeyStroke("shift ctrl V"), "insertClipboard");
	}

	private void installMouseBehavior() {
		if (!(table instanceof SpreadSheet))
			return;
		final SpreadSheet spreadSheet = (SpreadSheet)table;
		addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent e) {
				SpreadSheetPopupMenu popup = spreadSheet.getPopup();
				if (SwingUtilities.isLeftMouseButton(e)) {
					handleLeftClick(e, spreadSheet);
					return;
				}
				if (popup != null && SwingUtilities.isRightMouseButton(e))
					handleRightClick(e, popup);
			}
		});
	}

	private void handleLeftClick(MouseEvent e, SpreadSheet spreadSheet) {
		int row = rowAtPoint(e.getPoint());
		if (e.getClickCount() == 1 && isRowFullySelected(row)) {
			clearSelection();
			table.clearSelection();
			e.consume();
			return;
		}
		if (e.getClickCount() == 2)
			spreadSheet.doDoubleClick(0, 0);
	}

	private void handleRightClick(MouseEvent e, SpreadSheetPopupMenu popup) {
		Point p = e.getPoint();
		int row = rowAtPoint(p);
		if (row >= 0)
			table.getSelectionModel().addSelectionInterval(row, row);
		popup.setRow(row);
		popup.setCol(0);
		popup.show(this, e.getX(), e.getY());
	}

	private boolean isRowFullySelected(int row) {
		if (row < 0)
			return false;
		if (!getSelectionModel().isSelectedIndex(row))
			return false;
		return getSelectedRowCount() == 1 && table.getSelectedRowCount() == 1
			&& table.getSelectedRow() == row
			&& table.getSelectedColumnCount() == table.getColumnCount();
	}
}
