package com.projectlibre1.pm.graphic.spreadsheet.swingx;

import java.awt.Component;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

import com.projectlibre1.graphic.configuration.shape.Colors;

public class TimeSpreadsheetRowHeaderRenderer extends DefaultTableCellRenderer {
	public TimeSpreadsheetRowHeaderRenderer() {
		super();
	}

	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
		JLabel component = (JLabel)super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
		component.setForeground(table.getTableHeader().getForeground());
		component.setBackground(Colors.findColor("NORMAL_YELLOW"));
		component.setFont(table.getTableHeader().getFont());
		component.setHorizontalAlignment(CENTER);
		component.setText(value == null ? "" : value.toString());
		return component;
	}
}
