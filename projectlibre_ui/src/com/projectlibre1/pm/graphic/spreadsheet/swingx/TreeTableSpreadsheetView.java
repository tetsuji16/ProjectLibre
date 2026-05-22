package com.projectlibre1.pm.graphic.spreadsheet.swingx;

import java.util.ArrayList;

import com.projectlibre1.graphic.configuration.ActionList;
import com.projectlibre1.graphic.configuration.CellStyle;
import com.projectlibre1.pm.graphic.model.cache.NodeModelCache;
import com.projectlibre1.pm.graphic.spreadsheet.SpreadSheetModel;

public class TreeTableSpreadsheetView extends FlatSpreadsheetView {
	private static final long serialVersionUID = 1L;
	private NodeModelCacheTreeTableModel swingXTreeTableModel;

	public void setCache(NodeModelCache cache, ArrayList fieldArray, CellStyle cellStyle, ActionList actionList) {
		super.setCache(cache, fieldArray, cellStyle, actionList);
		if (getModel() instanceof SpreadSheetModel)
			swingXTreeTableModel = new NodeModelCacheTreeTableModel(cache, (SpreadSheetModel)getModel());
		else
			swingXTreeTableModel = null;
	}

	public NodeModelCacheTreeTableModel getSwingXTreeTableModel() {
		return swingXTreeTableModel;
	}
}
