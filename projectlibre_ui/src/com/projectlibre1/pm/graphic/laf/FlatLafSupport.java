package com.projectlibre1.pm.graphic.laf;

import com.formdev.flatlaf.FlatLightLaf;
import com.projectlibre1.util.Environment;

import javax.swing.UIManager;

/**
 * Centralized FlatLaf bootstrap used by all Swing entry points.
 */
public final class FlatLafSupport {
	private static boolean installed;

	private FlatLafSupport() {
	}

	public static synchronized void install() {
		if (installed) {
			return;
		}
		FlatLightLaf.setup();
		Environment.setNewLook(true);
		Environment.setNewLaf(true);
		installed = true;
	}

	public static boolean isInstalled() {
		return installed || isFlatLafActive();
	}

	public static boolean isFlatLafActive() {
		return isFlatLafClass(UIManager.getLookAndFeel());
	}

	public static boolean isFlatLafClass(Object lookAndFeel) {
		return lookAndFeel != null
			&& lookAndFeel.getClass().getName().startsWith("com.formdev.flatlaf");
	}
}
