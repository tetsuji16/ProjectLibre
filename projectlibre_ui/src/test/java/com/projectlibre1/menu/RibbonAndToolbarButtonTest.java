package com.projectlibre1.menu;

import static com.projectlibre1.menu.testsupport.ButtonVisibilityValidator.assertAttachedButtonsAreVisible;
import static com.projectlibre1.menu.testsupport.ButtonVisibilityValidator.assertValidCommandButton;
import static com.projectlibre1.menu.testsupport.ButtonVisibilityValidator.assertValidSwingButton;
import static com.projectlibre1.menu.testsupport.MenuDefinitionSupport.menuBundle;
import static com.projectlibre1.menu.testsupport.MenuDefinitionSupport.ribbonBundles;
import static com.projectlibre1.menu.testsupport.MenuDefinitionSupport.ribbonButtonIds;
import static com.projectlibre1.menu.testsupport.MenuDefinitionSupport.stubActionMap;
import static com.projectlibre1.menu.testsupport.MenuDefinitionSupport.toolBarButtonIds;

import java.util.Locale;

import javax.swing.JToolBar;
import javax.swing.SwingUtilities;

import org.junit.jupiter.api.Test;
import org.pushingpixels.flamingo.api.common.AbstractCommandButton;
import org.pushingpixels.flamingo.api.ribbon.AbstractRibbonBand;
import org.pushingpixels.flamingo.api.ribbon.RibbonTask;

class RibbonAndToolbarButtonTest {
	@Test
	void standardRibbonButtonsCanBeConstructedInDefaultLocale() throws Exception {
		ExtRibbonFactory factory = new ExtRibbonFactory(stubActionMap(), ribbonBundles(Locale.ROOT));
		SwingUtilities.invokeAndWait(() -> {
			for (String id : ribbonButtonIds()) {
				AbstractCommandButton button = factory.createJButton(id);
				assertValidCommandButton(id, button, true);
			}
		});
	}

	@Test
	void standardRibbonButtonsCanBeConstructedInJapaneseLocale() throws Exception {
		ExtRibbonFactory factory = new ExtRibbonFactory(stubActionMap(), ribbonBundles(Locale.JAPANESE));
		SwingUtilities.invokeAndWait(() -> {
			for (String id : ribbonButtonIds()) {
				AbstractCommandButton button = factory.createJButton(id);
				assertValidCommandButton(id, button, true);
			}
		});
	}

	@Test
	void standardRibbonCreatesAttachedVisibleButtons() throws Exception {
		ExtRibbonFactory factory = new ExtRibbonFactory(stubActionMap(), ribbonBundles(Locale.JAPANESE));
		SwingUtilities.invokeAndWait(() -> {
			for (RibbonTask task : factory.createRibbon("StandardRibbon", null)) {
				for (AbstractRibbonBand<?> band : task.getBands()) {
					assertAttachedButtonsAreVisible(band, task.getTitle() + "/" + band.getTitle());
				}
			}
		});
	}

	@Test
	void ribbonViewToolbarButtonsCanBeConstructed() throws Exception {
		ExtToolBarFactory factory = new ExtToolBarFactory(stubActionMap(), ribbonBundles(Locale.JAPANESE));
		SwingUtilities.invokeAndWait(() -> {
			for (String id : toolBarButtonIds(MenuManager.RIBBON_VIEW_BAR)) {
				assertValidSwingButton(id, factory.createJButton(id), true);
			}
			JToolBar toolBar = factory.createJToolBar(MenuManager.RIBBON_VIEW_BAR);
			assertAttachedButtonsAreVisible(toolBar, MenuManager.RIBBON_VIEW_BAR);
		});
	}

	@Test
	void printPreviewToolbarButtonsCanBeConstructed() throws Exception {
		ExtToolBarFactory factory = new ExtToolBarFactory(stubActionMap(), ribbonBundles(Locale.JAPANESE));
		SwingUtilities.invokeAndWait(() -> {
			for (String id : toolBarButtonIds(MenuManager.PRINT_PREVIEW_TOOL_BAR)) {
				assertValidSwingButton(id, factory.createJButton(id), true);
			}
			JToolBar toolBar = factory.createJToolBar(MenuManager.PRINT_PREVIEW_TOOL_BAR);
			assertAttachedButtonsAreVisible(toolBar, MenuManager.PRINT_PREVIEW_TOOL_BAR);
		});
	}

	@Test
	void japaneseBundleStillProvidesLabelsForStandardRibbonButtons() {
		var japaneseBundle = menuBundle(Locale.JAPANESE);
		for (String id : ribbonButtonIds()) {
			org.junit.jupiter.api.Assertions.assertTrue(
				com.projectlibre1.menu.testsupport.MenuDefinitionSupport.hasLocalizedLabel(japaneseBundle, id),
				() -> id + " is missing Japanese text and tooltip");
		}
	}
}
