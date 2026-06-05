package com.projectlibre1.reports.adapter;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.projectlibre1.configuration.ReportDefinition;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperReport;

class ReportUtilTest {
	@Test
	void loadsBundledJrxmlReportDefinitions() throws Exception {
		ReportDefinition definition = new ReportDefinition();
		definition.setName("projectDetails");
		definition.setFile("projectDetails.jrxml");

		JasperReport report = ReportUtil.getReport(definition, null, null);

		assertNotNull(report);
		assertNotNull(report.getName());
	}

	@Test
	void failsClearlyWhenReportDefinitionIsMissing() {
		ReportDefinition definition = new ReportDefinition();
		definition.setName("missing");
		definition.setFile("missing-report.jrxml");

		JRException exception = assertThrows(JRException.class, () -> ReportUtil.getReport(definition, null, null));

		assertTrue(exception.getMessage().contains("missing-report.jrxml"));
	}
}
