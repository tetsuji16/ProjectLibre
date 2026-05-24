package com.projectlibre1.util;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Insets;
import java.awt.Font;

import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.JToolBar;
import javax.swing.UIManager;
import javax.swing.plaf.FontUIResource;
import javax.swing.border.Border;

/**
 * Small FlatLaf-friendly UI helpers for shared Swing styling.
 */
public final class FlatUiSupport {
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

	public static Color panelBackground() {
		return color("Panel.background", Color.WHITE);
	}

	public static Color tableBackground() {
		return color("Table.background", panelBackground());
	}

	public static Color tableForeground() {
		return color("Table.foreground", labelForeground());
	}

	public static Color tableSelectionBackground() {
		return color("Table.selectionBackground", accentColor());
	}

	public static Color tableSelectionForeground() {
		return color("Table.selectionForeground", Color.WHITE);
	}

	public static Color headerBackground() {
		return color("TableHeader.background", panelBackground());
	}

	public static Color headerForeground() {
		return color("TableHeader.foreground", tableForeground());
	}

	public static Color disabledForeground() {
		return color("Label.disabledForeground", labelForeground().darker());
	}

	public static Color infoForeground() {
		return color("TextField.foreground", labelForeground());
	}

	public static Color errorForeground() {
		Color color = UIManager.getColor("Actions.Red");
		if (color == null)
			color = UIManager.getColor("Component.errorFocusColor");
		if (color == null)
			color = new Color(0xC62828);
		return color;
	}

	public static Color labelForeground() {
		return color("Label.foreground", Color.BLACK);
	}

	public static Color borderColor() {
		return color("Component.borderColor", labelForeground().darker());
	}

	public static Color tableGridColor() {
		return color("Table.gridColor", borderColor());
	}

	public static Color accentColor() {
		Color color = UIManager.getColor("Component.focusColor");
		if (color == null)
			color = UIManager.getColor("ProgressBar.foreground");
		if (color == null)
			color = UIManager.getColor("Actions.Blue");
		if (color == null)
			color = new Color(0x4A90E2);
		return color;
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
		toolBar.setBackground(panelBackground());
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
