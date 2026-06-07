package com.projectlibre1.menu;

import static com.projectlibre1.menu.testsupport.ButtonVisibilityValidator.assertAttachedButtonsAreVisible;
import static com.projectlibre1.menu.testsupport.ButtonVisibilityValidator.assertValidCommandButton;
import static com.projectlibre1.menu.testsupport.ButtonVisibilityValidator.assertValidSwingButton;
import static com.projectlibre1.menu.testsupport.MenuDefinitionSupport.menuBundle;
import static com.projectlibre1.menu.testsupport.MenuDefinitionSupport.ribbonBandIds;
import static com.projectlibre1.menu.testsupport.MenuDefinitionSupport.ribbonBandsByTask;
import static com.projectlibre1.menu.testsupport.MenuDefinitionSupport.ribbonButtonIdsForTask;
import static com.projectlibre1.menu.testsupport.MenuDefinitionSupport.ribbonBundles;
import static com.projectlibre1.menu.testsupport.MenuDefinitionSupport.ribbonButtonIds;
import static com.projectlibre1.menu.testsupport.MenuDefinitionSupport.stubActionMap;
import static com.projectlibre1.menu.testsupport.MenuDefinitionSupport.toolBarButtonIds;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;

import javax.swing.JToolBar;
import javax.swing.SwingUtilities;

import org.junit.jupiter.api.Test;
import org.pushingpixels.flamingo.api.common.AbstractCommandButton;
import org.pushingpixels.flamingo.api.ribbon.AbstractRibbonBand;
import org.pushingpixels.flamingo.api.ribbon.RibbonTask;

import com.projectlibre1.menu.MenuManager;

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
			assertTrue(
				com.projectlibre1.menu.testsupport.MenuDefinitionSupport.hasLocalizedLabel(japaneseBundle, id),
				() -> id + " is missing Japanese text and tooltip");
		}
	}

	@Test
	void ribbonButtonsCanHaveSelectionStateUpdatedWithoutClassCast() throws Exception {
		SwingUtilities.invokeAndWait(() -> {
			MenuManager manager = MenuManager.getInstance(stubActionMap());
			manager.getRibbon(MenuManager.STANDARD_RIBBON, null);
			org.junit.jupiter.api.Assertions.assertDoesNotThrow(() -> {
				manager.setActionSelected("Projects", true);
				manager.setActionSelected("Report", true);
				manager.setActionEnabled("Projects", true);
				manager.setActionVisible("Report", true);
			});
		});
	}

	@Test
	void viewRibbonTaskOwnsItsViewAndZoomButtonsExclusively() {
		Set<String> viewButtons = ribbonButtonIdsForTask("ViewRibbonTask");
		Set<String> taskButtons = ribbonButtonIdsForTask("TaskRibbonTask");
		Set<String> resourceButtons = ribbonButtonIdsForTask("ResourceRibbonTask");

		for (String buttonId : viewButtons) {
			assertFalse(taskButtons.contains(buttonId), () -> "View button leaked into TaskRibbonTask: " + buttonId);
			assertFalse(resourceButtons.contains(buttonId), () -> "View button leaked into ResourceRibbonTask: " + buttonId);
		}
	}

	@Test
	void preferencesRibbonBandAppearsOnlyInProjectRibbonTask() {
		var owners = new ArrayList<String>();
		for (Map.Entry<String, java.util.List<String>> entry : ribbonBandsByTask().entrySet()) {
			if (entry.getValue().contains("PreferencesRibbonBand")) {
				owners.add(entry.getKey());
			}
		}
		assertEquals(java.util.List.of("ProjectRibbonTask"), owners);
	}

	@Test
	void taskAndResourceRibbonTasksDoNotContainViewBands() {
		Set<String> viewBands = new HashSet<>();
		for (String bandId : ribbonBandIds("ViewRibbonTask")) {
			if (bandId.contains("Views") || bandId.contains("SubViews")) {
				viewBands.add(bandId);
			}
		}

		for (String taskBand : ribbonBandIds("TaskRibbonTask")) {
			assertFalse(viewBands.contains(taskBand), () -> "TaskRibbonTask still contains view band " + taskBand);
		}
		for (String resourceBand : ribbonBandIds("ResourceRibbonTask")) {
			assertFalse(viewBands.contains(resourceBand), () -> "ResourceRibbonTask still contains view band " + resourceBand);
		}
	}

	@Test
	void viewRibbonIncludesDisplaySettingsBand() {
		assertTrue(ribbonBandIds("ViewRibbonTask").contains("ViewSettingsRibbonBand"));
		assertEquals(
			java.util.List.of("RibbonToggleProgressLine", "RibbonLabelResourceNames", "RibbonLabelTaskName"),
			com.projectlibre1.menu.testsupport.MenuDefinitionSupport.ribbonButtonIds("ViewSettingsRibbonBand"));
	}

	@Test
	void viewSettingsButtonsHaveBackingMenuItems() {
		ResourceBundle internal = com.projectlibre1.menu.testsupport.MenuDefinitionSupport.menuInternalBundle();
		ResourceBundle labels = menuBundle(Locale.ROOT);
		for (String id : java.util.List.of("ToggleProgressLine", "LabelResourceNames", "LabelTaskName")) {
			assertTrue(internal.containsKey(id + ".action"), () -> id + " is missing an internal action mapping");
			assertTrue(labels.containsKey(id + ".text"), () -> id + " is missing menu text");
		}
	}
}
