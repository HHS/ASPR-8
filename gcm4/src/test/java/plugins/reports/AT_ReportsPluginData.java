package plugins.reports;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Supplier;

import org.junit.jupiter.api.Test;

import nucleus.ReportContext;
import plugins.reports.support.ReportLabel;
import plugins.reports.support.SimpleReportLabel;
import util.annotations.UnitTestMethod;

public class AT_ReportsPluginData {

	@Test
	@UnitTestMethod(target = ReportsPluginData.class, name = "builder", args = {})
	public void testBuilder() {
		assertNotNull(ReportsPluginData.builder());
	}

	@Test
	@UnitTestMethod(target = ReportsPluginData.Builder.class, name = "build", args = {})
	public void testBuild() {
		assertNotNull(ReportsPluginData.builder().build());
	}

	@Test
	@UnitTestMethod(target = ReportsPluginData.Builder.class, name = "addReport", args = { Supplier.class })
	public void testAddReport() {
		ReportLabel reportLabel_1 = new SimpleReportLabel("report 1");
		ReportLabel reportLabel_2 = new SimpleReportLabel("report 2");

		Set<ReportLabel> expectedReportLabels = new LinkedHashSet<>();
		expectedReportLabels.add(reportLabel_1);
		expectedReportLabels.add(reportLabel_2);

		Set<ReportLabel> observedReportLabels = new LinkedHashSet<>();

		ReportsPluginData.Builder builder = ReportsPluginData.builder();
		builder.addReport(() -> (c) -> {
			observedReportLabels.add(reportLabel_1);
		});
		builder.addReport(() -> (c) -> {
			observedReportLabels.add(reportLabel_2);
		});

		ReportsPluginData reportsPluginData = builder.build();

		Set<Consumer<ReportContext>> reports = reportsPluginData.getReports();

		assertNotNull(reports);

		for (Consumer<ReportContext> report : reports) {
			assertNotNull(report);
			report.accept(null);
		}

		assertEquals(observedReportLabels, expectedReportLabels);

	}

	@Test
	@UnitTestMethod(target = ReportsPluginData.class, name = "getReports", args = {})
	public void testGetReportLabels() {

		ReportLabel reportLabel_1 = new SimpleReportLabel("report 1");
		ReportLabel reportLabel_2 = new SimpleReportLabel("report 2");

		Set<ReportLabel> expectedReportLabels = new LinkedHashSet<>();
		expectedReportLabels.add(reportLabel_1);
		expectedReportLabels.add(reportLabel_2);

		Set<ReportLabel> observedReportLabels = new LinkedHashSet<>();

		ReportsPluginData.Builder builder = ReportsPluginData.builder();
		builder.addReport(() -> (c) -> {
			observedReportLabels.add(reportLabel_1);
		});
		builder.addReport(() -> (c) -> {
			observedReportLabels.add(reportLabel_2);
		});

		ReportsPluginData reportsPluginData = builder.build();

		Set<Consumer<ReportContext>> reports = reportsPluginData.getReports();

		assertNotNull(reports);

		for (Consumer<ReportContext> report : reports) {
			assertNotNull(report);
			report.accept(null);
		}

		assertEquals(observedReportLabels, expectedReportLabels);

	}

	@Test
	@UnitTestMethod(target = ReportsPluginData.class, name = "getCloneBuilder", args = {})
	public void testGetCloneBuilder() {
		ReportLabel reportLabel_1 = new SimpleReportLabel("report 1");
		ReportLabel reportLabel_2 = new SimpleReportLabel("report 2");

		Set<ReportLabel> expectedReportLabels = new LinkedHashSet<>();
		expectedReportLabels.add(reportLabel_1);
		expectedReportLabels.add(reportLabel_2);

		Set<ReportLabel> observedReportLabels = new LinkedHashSet<>();

		ReportsPluginData.Builder builder = ReportsPluginData.builder();
		builder.addReport(() -> (c) -> {
			observedReportLabels.add(reportLabel_1);
		});
		builder.addReport(() -> (c) -> {
			observedReportLabels.add(reportLabel_2);
		});

		ReportsPluginData reportsPluginData = builder.build();
		ReportsPluginData cloneReportsPluginData = (ReportsPluginData) reportsPluginData.getCloneBuilder().build();

		Set<Consumer<ReportContext>> reports = cloneReportsPluginData.getReports();

		assertNotNull(reports);

		for (Consumer<ReportContext> report : reports) {
			assertNotNull(report);
			report.accept(null);
		}

		assertEquals(observedReportLabels, expectedReportLabels);
	}

}
