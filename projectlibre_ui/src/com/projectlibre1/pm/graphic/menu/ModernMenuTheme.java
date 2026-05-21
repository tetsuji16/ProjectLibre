package com.projectlibre1.pm.graphic.menu;

import java.awt.Color;
import java.awt.Insets;

import javax.swing.BorderFactory;
import javax.swing.UIManager;
import javax.swing.border.Border;

public final class ModernMenuTheme {
	private static final Color FALLBACK_SURFACE = new Color(247, 244, 238);
	private static final Color FALLBACK_PANEL = new Color(255, 252, 247);
	private static final Color FALLBACK_CARD = new Color(255, 255, 255);
	private static final Color FALLBACK_BORDER = new Color(225, 217, 206);
	private static final Color FALLBACK_ACCENT = new Color(191, 117, 59);
	private static final Color FALLBACK_ACCENT_SOFT = new Color(245, 229, 214);
	private static final Color FALLBACK_TEXT = new Color(52, 45, 39);
	private static final Color FALLBACK_MUTED = new Color(118, 109, 99);

	private ModernMenuTheme() {
	}

	public static Color surface() {
		Color color = UIManager.getColor("Panel.background");
		return color != null ? mix(color, FALLBACK_SURFACE, 0.50f) : FALLBACK_SURFACE;
	}

	public static Color workspaceSurface() {
		return surface();
	}

	public static Color elevatedSurface() {
		Color color = UIManager.getColor("ToolBar.background");
		return color != null ? mix(color, FALLBACK_PANEL, 0.55f) : FALLBACK_PANEL;
	}

	public static Color cardSurface() {
		Color color = UIManager.getColor("Menu.background");
		return color != null ? mix(color, FALLBACK_CARD, 0.60f) : FALLBACK_CARD;
	}

	public static Color border() {
		Color color = UIManager.getColor("Component.borderColor");
		return color != null ? mix(color, FALLBACK_BORDER, 0.65f) : FALLBACK_BORDER;
	}

	public static Color accent() {
		Color color = UIManager.getColor("Component.focusColor");
		return color != null ? mix(color, FALLBACK_ACCENT, 0.35f) : FALLBACK_ACCENT;
	}

	public static Color accentSoft() {
		return mix(accent(), FALLBACK_ACCENT_SOFT, 0.18f);
	}

	public static Color strongText() {
		Color color = UIManager.getColor("Label.foreground");
		return color != null ? mix(color, FALLBACK_TEXT, 0.70f) : FALLBACK_TEXT;
	}

	public static Color mutedText() {
		Color color = UIManager.getColor("Label.disabledForeground");
		return color != null ? mix(color, FALLBACK_MUTED, 0.70f) : FALLBACK_MUTED;
	}

	public static Color selectionBackground() {
		return accentSoft();
	}

	public static Color selectionForeground() {
		return strongText();
	}

	public static int railWidth() {
		return 176;
	}

	public static int cardRadius() {
		return 18;
	}

	public static int sectionGap() {
		return 12;
	}

	public static Insets compactInsets() {
		return new Insets(6, 10, 6, 10);
	}

	public static Insets roomyInsets() {
		return new Insets(10, 14, 10, 14);
	}

	public static Border shellBorder() {
		return BorderFactory.createEmptyBorder(14, 14, 14, 14);
	}

	public static Border railBorder() {
		return BorderFactory.createCompoundBorder(
			BorderFactory.createLineBorder(border(), 1),
			BorderFactory.createEmptyBorder(18, 12, 18, 12));
	}

	public static Border headerBorder() {
		return BorderFactory.createCompoundBorder(
			BorderFactory.createLineBorder(border(), 1),
			BorderFactory.createEmptyBorder(12, 14, 12, 14));
	}

	public static Border cardBorder() {
		return BorderFactory.createCompoundBorder(
			BorderFactory.createLineBorder(border(), 1),
			BorderFactory.createEmptyBorder(12, 14, 12, 14));
	}

	public static Border softCardBorder() {
		return BorderFactory.createCompoundBorder(
			BorderFactory.createLineBorder(border(), 1),
			BorderFactory.createEmptyBorder(18, 18, 18, 18));
	}

	private static Color mix(Color base, Color fallback, float fallbackWeight) {
		float clamped = Math.max(0f, Math.min(1f, fallbackWeight));
		int red = (int) (base.getRed() * (1f - clamped) + fallback.getRed() * clamped);
		int green = (int) (base.getGreen() * (1f - clamped) + fallback.getGreen() * clamped);
		int blue = (int) (base.getBlue() * (1f - clamped) + fallback.getBlue() * clamped);
		return new Color(red, green, blue);
	}
}
