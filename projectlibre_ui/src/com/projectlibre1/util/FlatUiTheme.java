package com.projectlibre1.util;

import java.awt.Color;

import javax.swing.UIManager;

/**
 * Shared color roles for the FlatLaf-based UI.
 *
 * Keep the palette here so the app-facing helpers and the UI defaults stay in sync.
 */
final class FlatUiTheme {
	static final Color APP_BACKGROUND = new Color(0xF3F2F1);
	static final Color RIBBON_CHROME_BACKGROUND = new Color(0xF3F2F1);
	static final Color RIBBON_SURFACE = Color.WHITE;
	static final Color TABLE_BACKGROUND = APP_BACKGROUND;
	static final Color TABLE_FOREGROUND = new Color(0x202124);
	static final Color TABLE_SELECTION_BACKGROUND = new Color(0x4A90E2);
	static final Color TABLE_SELECTION_FOREGROUND = Color.WHITE;
	static final Color HEADER_BACKGROUND = APP_BACKGROUND;
	static final Color HEADER_FOREGROUND = TABLE_FOREGROUND;
	static final Color LABEL_FOREGROUND = new Color(0x202124);
	static final Color DISABLED_FOREGROUND = new Color(0x70757D);
	static final Color BORDER = new Color(0xB8C0CA);
	static final Color SEPARATOR = new Color(0xD0D6DD);
	static final Color ACCENT = new Color(0x4A90E2);
	static final Color ERROR = new Color(0xC62828);
	static final Color TABLE_GRID = BORDER;
	static final Color INFO_FOREGROUND = LABEL_FOREGROUND;

	private FlatUiTheme() {
	}

	static void installIntoUIManager() {
		UIManager.put("Panel.background", APP_BACKGROUND);
		UIManager.put("PopupPanel.background", APP_BACKGROUND);
		UIManager.put("ProjectLibre.ribbonChromeBackground", RIBBON_CHROME_BACKGROUND);
		UIManager.put("ProjectLibre.ribbonSurfaceBackground", RIBBON_SURFACE);
		UIManager.put("Table.background", TABLE_BACKGROUND);
		UIManager.put("Table.foreground", TABLE_FOREGROUND);
		UIManager.put("Table.selectionBackground", TABLE_SELECTION_BACKGROUND);
		UIManager.put("Table.selectionForeground", TABLE_SELECTION_FOREGROUND);
		UIManager.put("Table.gridColor", TABLE_GRID);
		UIManager.put("TableHeader.background", HEADER_BACKGROUND);
		UIManager.put("TableHeader.cellBackground", HEADER_BACKGROUND);
		UIManager.put("TableHeader.foreground", HEADER_FOREGROUND);
		UIManager.put("Label.foreground", LABEL_FOREGROUND);
		UIManager.put("Label.disabledForeground", DISABLED_FOREGROUND);
		UIManager.put("TextField.foreground", INFO_FOREGROUND);
		UIManager.put("TextField.selectionBackground", TABLE_SELECTION_BACKGROUND);
		UIManager.put("TextField.selectionForeground", TABLE_SELECTION_FOREGROUND);
		UIManager.put("Separator.foreground", SEPARATOR);
		UIManager.put("Component.borderColor", BORDER);
		UIManager.put("Component.focusColor", ACCENT);
		UIManager.put("ProgressBar.foreground", ACCENT);
		UIManager.put("Actions.Blue", ACCENT);
		UIManager.put("Actions.Red", ERROR);
		UIManager.put("Component.errorFocusColor", ERROR);
	}
}
