package com.projectlibre1.pm.graphic.spreadsheet.swingx;

import java.awt.Component;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.table.DefaultTableCellRenderer;

import com.projectlibre1.field.Field;
import com.projectlibre1.pm.graphic.frames.GraphicManager;
import com.projectlibre1.pm.graphic.model.cache.GraphicNode;
import com.projectlibre1.pm.graphic.spreadsheet.SpreadSheetParams;
import com.projectlibre1.pm.graphic.spreadsheet.renderer.OfflineRenderer;

public class SpreadsheetRowHeaderRenderer extends DefaultTableCellRenderer implements OfflineRenderer {
	public SpreadsheetRowHeaderRenderer() {
		super();
	}

	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
		JLabel component;
		if (table == null) {
			setValue(null);
			component = this;
		} else {
			component = (JLabel)super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
			component.setForeground(table.getTableHeader().getForeground());
			component.setBackground(isSelected
				? GraphicManager.getInstance().getLafManager().getSelectedBackgroundColor()
				: GraphicManager.getInstance().getLafManager().getUnselectedBackgroundColor());
			component.setFont(table.getTableHeader().getFont());
		}
		component.setHorizontalAlignment(CENTER);
		component.setText(value == null ? "" : value.toString());
		Border border = UIManager.getBorder("TableHeader.cellBorder");
		if (border != null)
			component.setBorder(border);
		return component;
	}

	public Component getComponent(Object value, GraphicNode node, Field field, SpreadSheetParams params) {
		JComponent component = (JComponent)getTableCellRendererComponent(null, value, false, false, -1, -1);
		return component;
	}
}
