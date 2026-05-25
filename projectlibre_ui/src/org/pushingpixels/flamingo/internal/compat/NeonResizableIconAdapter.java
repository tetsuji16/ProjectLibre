package org.pushingpixels.flamingo.internal.compat;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;

import org.pushingpixels.flamingo.api.common.icon.ResizableIcon;

public final class NeonResizableIconAdapter implements ResizableIcon {
	private final org.pushingpixels.neon.icon.ResizableIcon delegate;

	public NeonResizableIconAdapter(
			org.pushingpixels.neon.icon.ResizableIcon delegate) {
		this.delegate = delegate;
	}

	@Override
	public void setDimension(Dimension newDimension) {
		this.delegate.setDimension(newDimension);
	}

	@Override
	public int getIconWidth() {
		return this.delegate.getIconWidth();
	}

	@Override
	public int getIconHeight() {
		return this.delegate.getIconHeight();
	}

	@Override
	public void paintIcon(Component c, Graphics g, int x, int y) {
		this.delegate.paintIcon(c, g, x, y);
	}

	public org.pushingpixels.neon.icon.ResizableIcon getDelegate() {
		return this.delegate;
	}
}
