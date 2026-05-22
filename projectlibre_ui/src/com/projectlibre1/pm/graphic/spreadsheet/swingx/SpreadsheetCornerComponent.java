package com.projectlibre1.pm.graphic.spreadsheet.swingx;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import com.projectlibre1.pm.graphic.spreadsheet.SpreadSheet;
import com.projectlibre1.pm.graphic.spreadsheet.common.CommonSpreadSheet;
import com.projectlibre1.pm.graphic.spreadsheet.common.GradientCorner;
import com.projectlibre1.pm.graphic.spreadsheet.selection.SpreadSheetColumnsPopupMenu;
import com.projectlibre1.pm.graphic.spreadsheet.selection.TimeSpreadSheetColumnsPopupMenu;
import com.projectlibre1.pm.graphic.spreadsheet.time.TimeSpreadSheet;
import com.projectlibre1.configuration.Dictionary;
import com.projectlibre1.strings.Messages;
import com.projectlibre1.util.Environment;

public class SpreadsheetCornerComponent extends GradientCorner implements ListSelectionListener {
	private final CommonSpreadSheet spreadSheet;

	public SpreadsheetCornerComponent(CommonSpreadSheet spreadSheet) {
		this.spreadSheet = spreadSheet;
		if (spreadSheet.isCanSelectFieldArray())
			setToolTipText("dummy");
		if (spreadSheet instanceof SpreadSheet)
			spreadSheet.getRowHeader().getSelectionModel().addListSelectionListener(this);

		Border border = UIManager.getBorder("TableHeader.cellBorder");
		if (border != null)
			setBorder(border);

		addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent e) {
				if (SwingUtilities.isRightMouseButton(e)) {
					showColumnsPopup(e);
					return;
				}
				selectAll();
			}
		});
	}

	public String getToolTipText(MouseEvent e) {
		return "<html>" + Dictionary.getCategoryText(spreadSheet.getSpreadSheetCategory()) + ": " + spreadSheet.getFieldArray()
			+ "<br>" + Messages.getString("Text.rightClickSelectToSpreadsheet") + "<html>";
	}

	public void valueChanged(ListSelectionEvent e) {
		ListSelectionModel model = (ListSelectionModel)e.getSource();
		if (Environment.isMac()) {
			if (selected)
				setSelected(false);
			return;
		}
		setSelected(model.getMinSelectionIndex() >= 0);
	}

	private void selectAll() {
		if (spreadSheet.getColumnCount() > 0)
			spreadSheet.getColumnModel().getSelectionModel().setSelectionInterval(0, spreadSheet.getColumnCount() - 1);
		if (spreadSheet.getRowCount() > 0) {
			spreadSheet.getSelectionModel().setSelectionInterval(0, spreadSheet.getRowCount() - 1);
			spreadSheet.getRowHeader().getSelectionModel().setSelectionInterval(0, spreadSheet.getRowCount() - 1);
		}
		if (Environment.isMac())
			setSelected(true);
	}

	private void showColumnsPopup(MouseEvent e) {
		if (spreadSheet.getSpreadSheetCategory() == null || !spreadSheet.isCanSelectFieldArray())
			return;
		if (spreadSheet instanceof TimeSpreadSheet) {
			TimeSpreadSheetColumnsPopupMenu columnsPopup =
				new TimeSpreadSheetColumnsPopupMenu((TimeSpreadSheet)spreadSheet, spreadSheet.getSpreadSheetCategory());
			columnsPopup.show(spreadSheet, e.getX(), e.getY());
			return;
		}
		SpreadSheetColumnsPopupMenu columnsPopup =
			new SpreadSheetColumnsPopupMenu(spreadSheet, spreadSheet.getSpreadSheetCategory());
		columnsPopup.show(spreadSheet, e.getX(), e.getY());
	}
}
