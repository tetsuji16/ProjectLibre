package com.projectlibre1.pm.graphic.spreadsheet.editor;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.text.SimpleDateFormat;

import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;

import org.junit.jupiter.api.Test;

class EditorSelectionBehaviorTest {
	@Test
	void keyboardFocusSpinnerSelectAllKeepsFullSelection() throws Exception {
		SwingUtilities.invokeAndWait(() -> {
			KeyboardFocusSpinner spinner = new KeyboardFocusSpinner(new SpinnerNumberModel(12.0, 0.0, 100.0, 1.0));
			spinner.setEditor(new javax.swing.JSpinner.NumberEditor(spinner, "0"));
			spinner.getTextField().setText("12345");

			spinner.selectAll(true);

			assertEquals("12345", spinner.getTextField().getSelectedText());
		});
	}

	@Test
	void dateFieldMouseSelectionRestoresFullSelection() throws Exception {
		SwingUtilities.invokeAndWait(() -> {
			DateEditor.ExtDateField field = new DateEditor.ExtDateField(new SimpleDateFormat("yyyy/MM/dd"));
			field.getTextField().setText("2026/06/06");
			field.selectAll(false);

			assertEquals("2026/06/06", field.getTextField().getSelectedText());
		});
	}
}
