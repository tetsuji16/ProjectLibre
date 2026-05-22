package com.projectlibre1.pm.graphic.spreadsheet.swingx;

import java.awt.Rectangle;
import java.awt.event.KeyEvent;

import com.projectlibre1.grouping.core.Node;
import com.projectlibre1.pm.graphic.spreadsheet.SpreadSheetModel;
import com.projectlibre1.pm.graphic.spreadsheet.common.CommonSpreadSheet;

public class SpreadsheetSelectionState {
	private PendingUndoSelection pendingUndoSelection;
	private CellPosition activeCell;
	private CellPosition anchorCell;
	private CellPosition selectionEnd;

	public void clearTrackedRangeSelection() {
		if (activeCell == null)
			return;
		anchorCell = activeCell;
		selectionEnd = activeCell;
	}

	public void clearSelectionState() {
		activeCell = null;
		anchorCell = null;
		selectionEnd = null;
	}

	public void selectionChanged(CommonSpreadSheet table, int row, int column, boolean toggle, boolean extend) {
		CellPosition normalized = normalize(table, row, column);
		if (normalized == null)
			return;
		if (toggle)
			return;
		if (extend) {
			ensureInitializedFromSelection(table);
			if (activeCell == null)
				synchronizeToSingleCell(normalized);
			else
				selectionEnd = normalized;
			return;
		}
		synchronizeToSingleCell(normalized);
	}

	public void rememberPendingUndoSelection(CommonSpreadSheet table, int row, int column, int followRow, int followColumn) {
		Node node = null;
		Object impl = null;
		if (table.getModel() instanceof SpreadSheetModel && row >= 0 && row < table.getRowCount()) {
			node = table.getNodeInRow(row);
			impl = (node == null) ? null : node.getImpl();
		}
		pendingUndoSelection = new PendingUndoSelection(node, impl, row, column, followRow, followColumn);
	}

	public PendingUndoSelection consumePendingUndoSelection(int currentRow, int currentColumn) {
		PendingUndoSelection selection = pendingUndoSelection;
		pendingUndoSelection = null;
		if (selection == null)
			return null;
		if (selection.followRow != currentRow || selection.followColumn != currentColumn)
			return null;
		return selection;
	}

	public boolean handleArrowKeyNavigation(CommonSpreadSheet table, KeyEvent e) {
		if (!isArrowKey(e))
			return false;
		ensureInitializedFromSelection(table);
		if (activeCell == null)
			return false;
		if (e.isShiftDown() && !e.isControlDown() && !e.isAltDown() && !e.isMetaDown()) {
			moveSelectionEnd(table, rowDeltaFor(e.getKeyCode()), columnDeltaFor(e.getKeyCode()));
			return true;
		}
		if (!e.isShiftDown() && !e.isControlDown() && !e.isAltDown() && !e.isMetaDown()) {
			moveActiveCell(table, rowDeltaFor(e.getKeyCode()), columnDeltaFor(e.getKeyCode()));
			return true;
		}
		return false;
	}

	private void moveSelectionEnd(CommonSpreadSheet table, int rowDelta, int columnDelta) {
		CellPosition currentEnd = (selectionEnd == null) ? activeCell : selectionEnd;
		CellPosition moved = clamp(table, currentEnd.row + rowDelta, currentEnd.column + columnDelta);
		selectionEnd = moved;
		applyRangeSelection(table);
	}

	private void moveActiveCell(CommonSpreadSheet table, int rowDelta, int columnDelta) {
		CellPosition currentActive = activeCell;
		if (currentActive == null)
			return;
		CellPosition moved = clamp(table, currentActive.row + rowDelta, currentActive.column + columnDelta);
		synchronizeToSingleCell(moved);
		applySingleSelection(table, moved);
	}

	private void applyRangeSelection(CommonSpreadSheet table) {
		if (anchorCell == null || selectionEnd == null)
			return;
		int startRow = Math.min(anchorCell.row, selectionEnd.row);
		int endRow = Math.max(anchorCell.row, selectionEnd.row);
		int startColumn = Math.min(anchorCell.column, selectionEnd.column);
		int endColumn = Math.max(anchorCell.column, selectionEnd.column);
		table.requestFocusInWindow();
		table.setRowSelectionInterval(startRow, endRow);
		table.getColumnModel().getSelectionModel().setSelectionInterval(startColumn, endColumn);
		table.getRowHeader().clearSelection();
		scrollToCell(table, selectionEnd);
	}

	private void applySingleSelection(CommonSpreadSheet table, CellPosition position) {
		table.requestFocusInWindow();
		table.setRowSelectionInterval(position.row, position.row);
		table.getColumnModel().getSelectionModel().setSelectionInterval(position.column, position.column);
		table.getRowHeader().clearSelection();
		scrollToCell(table, position);
	}

	private void scrollToCell(CommonSpreadSheet table, CellPosition position) {
		Rectangle cell = table.getCellRect(position.row, position.column, true);
		table.scrollRectToVisible(cell);
	}

	private void ensureInitializedFromSelection(CommonSpreadSheet table) {
		if (activeCell != null && anchorCell != null && selectionEnd != null)
			return;
		CellPosition current = currentSelectedCell(table);
		if (current != null)
			synchronizeToSingleCell(current);
	}

	private CellPosition currentSelectedCell(CommonSpreadSheet table) {
		int row = table.getSelectedRow();
		if (row < 0)
			row = table.getCurrentRow();
		int column = table.getSelectedColumn();
		if (row < 0 || column < 0)
			return null;
		return normalize(table, row, column);
	}

	private void synchronizeToSingleCell(CellPosition cell) {
		activeCell = cell;
		anchorCell = cell;
		selectionEnd = cell;
	}

	private static CellPosition normalize(CommonSpreadSheet table, int row, int column) {
		if (table.getRowCount() <= 0 || table.getColumnCount() <= 0)
			return null;
		return new CellPosition(
			Math.min(Math.max(row, 0), table.getRowCount() - 1),
			Math.min(Math.max(column, 0), table.getColumnCount() - 1));
	}

	private static CellPosition clamp(CommonSpreadSheet table, int row, int column) {
		return normalize(table, row, column);
	}

	private static boolean isArrowKey(KeyEvent e) {
		if (e == null || e.getID() != KeyEvent.KEY_PRESSED)
			return false;
		int keyCode = e.getKeyCode();
		return keyCode == KeyEvent.VK_LEFT || keyCode == KeyEvent.VK_RIGHT || keyCode == KeyEvent.VK_UP || keyCode == KeyEvent.VK_DOWN;
	}

	private static int rowDeltaFor(int keyCode) {
		if (keyCode == KeyEvent.VK_UP)
			return -1;
		if (keyCode == KeyEvent.VK_DOWN)
			return 1;
		return 0;
	}

	private static int columnDeltaFor(int keyCode) {
		if (keyCode == KeyEvent.VK_LEFT)
			return -1;
		if (keyCode == KeyEvent.VK_RIGHT)
			return 1;
		return 0;
	}

	public static final class PendingUndoSelection {
		private final Node node;
		private final Object impl;
		private final int row;
		private final int column;
		private final int followRow;
		private final int followColumn;

		private PendingUndoSelection(Node node, Object impl, int row, int column, int followRow, int followColumn) {
			this.node = node;
			this.impl = impl;
			this.row = row;
			this.column = column;
			this.followRow = followRow;
			this.followColumn = followColumn;
		}

		public Node getNode() {
			return node;
		}

		public Object getImpl() {
			return impl;
		}

		public int getRow() {
			return row;
		}

		public int getColumn() {
			return column;
		}
	}

	private static final class CellPosition {
		private final int row;
		private final int column;

		private CellPosition(int row, int column) {
			this.row = row;
			this.column = column;
		}
	}
}
