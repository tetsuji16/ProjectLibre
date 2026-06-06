package com.projectlibre1.pm.graphic.spreadsheet;

import static org.junit.jupiter.api.Assertions.assertSame;

import org.junit.jupiter.api.Test;

import com.projectlibre1.configuration.FieldDictionary;
import com.projectlibre1.graphic.configuration.SpreadSheetCategories;

class SpreadSheetUtilsTest {
	@Test
	void usageSpreadsheetCategoriesExposeCombinedFieldLists() {
		FieldDictionary dictionary = FieldDictionary.getInstance();

		assertSame(dictionary.getTaskAndAssignmentFields(),
			SpreadSheetUtils.getFieldsForCategory(SpreadSheetCategories.taskAssignmentSpreadsheetCategory));
		assertSame(dictionary.getResourceAndAssignmentFields(),
			SpreadSheetUtils.getFieldsForCategory(SpreadSheetCategories.resourceAssignmentSpreadsheetCategory));
	}
}
