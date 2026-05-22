package com.projectlibre1.pm.graphic.spreadsheet.swingx;

import org.jdesktop.swingx.treetable.AbstractTreeTableModel;

import com.projectlibre1.pm.graphic.model.cache.GraphicNode;
import com.projectlibre1.pm.graphic.model.cache.NodeModelCache;
import com.projectlibre1.pm.graphic.spreadsheet.SpreadSheetModel;

public class NodeModelCacheTreeTableModel extends AbstractTreeTableModel {
	private final NodeModelCache cache;
	private final SpreadSheetModel spreadSheetModel;

	public NodeModelCacheTreeTableModel(NodeModelCache cache, SpreadSheetModel spreadSheetModel) {
		super((cache == null) ? null : cache.getRoot());
		this.cache = cache;
		this.spreadSheetModel = spreadSheetModel;
	}

	public int getColumnCount() {
		return (spreadSheetModel == null) ? 0 : spreadSheetModel.getColumnCount();
	}

	public String getColumnName(int column) {
		return (spreadSheetModel == null) ? "" : spreadSheetModel.getColumnName(column);
	}

	public Object getValueAt(Object node, int column) {
		if (!(node instanceof GraphicNode) || spreadSheetModel == null || cache == null)
			return null;
		int row = cache.getRowAt(((GraphicNode)node).getNode());
		if (row < 0)
			return (column == 0) ? ((GraphicNode)node).getNode() : null;
		return spreadSheetModel.getValueAt(row, column);
	}

	public boolean isCellEditable(Object node, int column) {
		if (!(node instanceof GraphicNode) || spreadSheetModel == null || cache == null)
			return false;
		int row = cache.getRowAt(((GraphicNode)node).getNode());
		return row >= 0 && spreadSheetModel.isCellEditable(row, column);
	}

	public void setValueAt(Object value, Object node, int column) {
		if (!(node instanceof GraphicNode) || spreadSheetModel == null || cache == null)
			return;
		int row = cache.getRowAt(((GraphicNode)node).getNode());
		if (row >= 0)
			spreadSheetModel.setValueAt(value, row, column);
	}

	public Object getChild(Object parent, int index) {
		return (cache == null) ? null : cache.getChild(parent, index);
	}

	public int getChildCount(Object parent) {
		return (cache == null) ? 0 : cache.getChildCount(parent);
	}

	public int getIndexOfChild(Object parent, Object child) {
		return (cache == null) ? -1 : cache.getIndexOfChild(parent, child);
	}

	public boolean isLeaf(Object node) {
		return cache == null || cache.isLeaf(node);
	}
}
