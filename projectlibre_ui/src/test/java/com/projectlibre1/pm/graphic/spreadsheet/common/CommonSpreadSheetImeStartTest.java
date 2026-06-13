package com.projectlibre1.pm.graphic.spreadsheet.common;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.awt.event.InputMethodEvent;
import java.awt.event.KeyEvent;
import java.lang.reflect.Field;
import java.text.AttributedString;
import java.util.concurrent.atomic.AtomicReference;

import javax.swing.SwingUtilities;
import javax.swing.JTextField;

import org.junit.jupiter.api.Test;

class CommonSpreadSheetImeStartTest {
	@Test
	void inputMethodStartDoesNotClearExistingCellText() throws Exception {
		AtomicReference<Throwable> failure = new AtomicReference<>();
		SwingUtilities.invokeAndWait(() -> {
			try {
				var sheet = new RecordingSpreadSheet();

				var event = new InputMethodEvent(sheet, InputMethodEvent.INPUT_METHOD_TEXT_CHANGED, new AttributedString("テ").getIterator(), 0, null, null);
				sheet.processInputMethodEvent(event);

				assertNotNull(sheet.recordedEditEvent);
				assertFalse(readBooleanField(sheet.recordedEditEvent, "clearTextOnStart"));
			} catch (Throwable t) {
				failure.set(t);
			}
		});
		if (failure.get() != null) {
			throw new RuntimeException(failure.get());
		}
	}

	@Test
	void receivedTextIsAppendedToTheActiveEditor() throws Exception {
		AtomicReference<RecordingSpreadSheet> sheetRef = new AtomicReference<>();
		SwingUtilities.invokeAndWait(() -> sheetRef.set(new RecordingSpreadSheet()));
		var sheet = sheetRef.get();
		SwingUtilities.invokeAndWait(() -> {
			sheet.primeEditor("既存");
			sheet.processKeyEvent(new KeyEvent(sheet, KeyEvent.KEY_TYPED, System.currentTimeMillis(), 0, KeyEvent.VK_UNDEFINED, 'テ'));
		});
		SwingUtilities.invokeAndWait(() -> {});
		SwingUtilities.invokeAndWait(() -> {
			sheet.processKeyEvent(new KeyEvent(sheet, KeyEvent.KEY_TYPED, System.currentTimeMillis(), 0, KeyEvent.VK_UNDEFINED, 'ス'));
		});
		SwingUtilities.invokeAndWait(() -> {});

		assertEquals("既存テス", sheet.editorText());
	}

	private static boolean readBooleanField(Object target, String fieldName) throws Exception {
		Field field = target.getClass().getDeclaredField(fieldName);
		field.setAccessible(true);
		return field.getBoolean(target);
	}

	private static final class RecordingSpreadSheet extends CommonSpreadSheet {
		private static final long serialVersionUID = 1L;
		private java.util.EventObject recordedEditEvent;
		private boolean editing;

		void primeEditor(String text) {
			editorComp = new JTextField(text);
			editing = true;
		}

		String editorText() {
			return ((JTextField) editorComp).getText();
		}

		@Override
		public boolean isEditing() {
			return editing;
		}

		@Override
		public int getCurrentRow() {
			return 0;
		}

		@Override
		public int getSelectedRow() {
			return 0;
		}

		@Override
		public int getSelectedColumn() {
			return 0;
		}

		@Override
		public int getRowCount() {
			return 1;
		}

		@Override
		public int getColumnCount() {
			return 1;
		}

		@Override
		public boolean editCellAt(int row, int column, java.util.EventObject e) {
			recordedEditEvent = e;
			editing = editorComp != null;
			return editorComp != null;
		}
	}
}
