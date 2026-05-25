package com.projectlibre1.util;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Insets;
import java.awt.Font;

import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.JTabbedPane;
import javax.swing.JToolBar;
import javax.swing.UIManager;
import javax.swing.plaf.FontUIResource;
import javax.swing.border.Border;

/**
 * Small FlatLaf-friendly UI helpers for shared Swing styling.
 */
public final class FlatUiSupport {
	private static final String RIBBON_CHROME_BACKGROUND_KEY = "ProjectLibre.ribbonChromeBackground";
	private static final String RIBBON_SURFACE_BACKGROUND_KEY = "ProjectLibre.ribbonSurfaceBackground";

	private FlatUiSupport() {
	}

	public static Font uiFont() {
		Font font = UIManager.getFont("defaultFont");
		if (font == null)
			font = UIManager.getFont("Table.font");
		if (font == null)
			font = UIManager.getFont("Label.font");
		if (font == null)
			font = new Font("SansSerif", Font.PLAIN, 12);
		return font;
	}

	public static Font headerFont() {
		return uiFont().deriveFont(Font.BOLD);
	}

	public static Color color(String key, Color fallback) {
		Color value = UIManager.getColor(key);
		return value != null ? value : fallback;
	}

	public static Color appBackground() {
		return color("Panel.background", FlatUiTheme.APP_BACKGROUND);
	}

	public static Color panelBackground() {
		return appBackground();
	}

	public static Color ribbonChromeBackground() {
		return color(RIBBON_CHROME_BACKGROUND_KEY, FlatUiTheme.RIBBON_CHROME_BACKGROUND);
	}

	public static Color tableBackground() {
		return color("Table.background", FlatUiTheme.TABLE_BACKGROUND);
	}

	public static Color tableForeground() {
		return color("Table.foreground", FlatUiTheme.TABLE_FOREGROUND);
	}

	public static Color tableSelectionBackground() {
		return color("Table.selectionBackground", FlatUiTheme.TABLE_SELECTION_BACKGROUND);
	}

	public static Color tableSelectionForeground() {
		return color("Table.selectionForeground", FlatUiTheme.TABLE_SELECTION_FOREGROUND);
	}

	public static Color headerBackground() {
		return color("TableHeader.background", FlatUiTheme.HEADER_BACKGROUND);
	}

	public static Color headerForeground() {
		return color("TableHeader.foreground", FlatUiTheme.HEADER_FOREGROUND);
	}

	public static Color disabledForeground() {
		return color("Label.disabledForeground", FlatUiTheme.DISABLED_FOREGROUND);
	}

	public static Color infoForeground() {
		return color("TextField.foreground", FlatUiTheme.INFO_FOREGROUND);
	}

	public static Color errorForeground() {
		Color color = UIManager.getColor("Actions.Red");
		if (color == null)
			color = UIManager.getColor("Component.errorFocusColor");
		if (color == null)
			color = FlatUiTheme.ERROR;
		return color;
	}

	public static Color labelForeground() {
		return color("Label.foreground", FlatUiTheme.LABEL_FOREGROUND);
	}

	public static Color borderColor() {
		return color("Component.borderColor", FlatUiTheme.BORDER);
	}

	public static Color separatorColor() {
		return color("Separator.foreground", FlatUiTheme.SEPARATOR);
	}

	public static Color ribbonTopLineColor() {
		return blend(separatorColor(), ribbonChromeBackground(), 0.35f);
	}

	public static Color ribbonSurfaceColor() {
		return color(RIBBON_SURFACE_BACKGROUND_KEY, FlatUiTheme.RIBBON_SURFACE);
	}

	public static Color ribbonSurfaceBorderColor() {
		return blend(borderColor(), panelBackground(), 0.82f);
	}

	public static Color ribbonSelectedTabColor() {
		return Color.WHITE;
	}

	public static Color ribbonTabHoverColor() {
		return blend(accentColor(), Color.WHITE, 0.90f);
	}

	public static Color tabSelectedForeground() {
		return accentColor();
	}

	public static Color tabUnselectedForeground() {
		return labelForeground();
	}

	public static Color tableGridColor() {
		return color("Table.gridColor", FlatUiTheme.TABLE_GRID);
	}

	public static Color accentColor() {
		Color color = UIManager.getColor("Component.focusColor");
		if (color == null)
			color = UIManager.getColor("ProgressBar.foreground");
		if (color == null)
			color = UIManager.getColor("Actions.Blue");
		if (color == null)
			color = FlatUiTheme.ACCENT;
		return color;
	}

	public static Color blend(Color first, Color second, float firstWeight) {
		float weight = Math.max(0f, Math.min(1f, firstWeight));
		float secondWeight = 1f - weight;
		int red = Math.round(first.getRed() * weight + second.getRed() * secondWeight);
		int green = Math.round(first.getGreen() * weight + second.getGreen() * secondWeight);
		int blue = Math.round(first.getBlue() * weight + second.getBlue() * secondWeight);
		return new Color(red, green, blue);
	}

	public static void enableAntialiasing(Graphics2D g2) {
		if (g2 == null)
			return;
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
	}

	public static void styleToolBar(JToolBar toolBar) {
		if (toolBar == null)
			return;
		toolBar.setFloatable(false);
		toolBar.setRollover(true);
		toolBar.setOpaque(false);
		toolBar.setBorder(BorderFactory.createEmptyBorder(2, 4, 2, 4));
		toolBar.setMargin(new Insets(2, 6, 2, 6));
		toolBar.setBackground(appBackground());
		toolBar.putClientProperty("JToolBar.isRollover", Boolean.TRUE);
	}

	public static void styleToolBarButton(AbstractButton button) {
		if (button == null)
			return;
		button.setFocusable(false);
		button.setBorderPainted(false);
		button.setContentAreaFilled(false);
		button.setOpaque(false);
		button.setFocusPainted(false);
		button.setRolloverEnabled(true);
		button.setMargin(new Insets(3, 5, 3, 5));
	}

	public static void styleTabbedPane(JTabbedPane tabbedPane) {
		if (tabbedPane == null)
			return;
		tabbedPane.putClientProperty("JTabbedPane.tabType", "underlined");
		tabbedPane.putClientProperty("JTabbedPane.showTabSeparators", Boolean.TRUE);
		tabbedPane.putClientProperty("JTabbedPane.tabSeparatorsFullHeight", Boolean.TRUE);
		tabbedPane.putClientProperty("JTabbedPane.showContentSeparator", Boolean.FALSE);
		tabbedPane.putClientProperty("JTabbedPane.tabHeight", Integer.valueOf(36));
		tabbedPane.setOpaque(true);
		tabbedPane.setBackground(appBackground());
		tabbedPane.setForeground(tabUnselectedForeground());
	}

	public static Border focusBorder() {
		Border border = UIManager.getBorder("TextField.border");
		if (border == null)
			border = BorderFactory.createLineBorder(borderColor());
		return border;
	}

	public static Border tableCellBorder() {
		return BorderFactory.createEmptyBorder(2, 4, 2, 4);
	}

	public static Border tableEditorBorder() {
		Border border = UIManager.getBorder("TextField.border");
		if (border == null)
			border = BorderFactory.createLineBorder(borderColor());
		return border;
	}

	public static Border tableHeaderBorder() {
		Border border = UIManager.getBorder("TableHeader.cellBorder");
		if (border == null)
			border = BorderFactory.createMatteBorder(0, 0, 1, 0, borderColor());
		return border;
	}

	public static FontUIResource asFontUIResource(Font font) {
		return font == null ? null : new FontUIResource(font);
	}
}
