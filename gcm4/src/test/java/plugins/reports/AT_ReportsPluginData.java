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
import plugins.reports.support.SimpleReportId;
import tools.annotations.UnitTestMethod;

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
		ReportLabel reportId_1 = new SimpleReportId("report 1");
		ReportLabel reportId_2 = new SimpleReportId("report 2");

		Set<ReportLabel> expectedReportIds = new LinkedHashSet<>();
		expectedReportIds.add(reportId_1);
		expectedReportIds.add(reportId_2);

		Set<ReportLabel> observedReportIds = new LinkedHashSet<>();

		ReportsPluginData.Builder builder = ReportsPluginData.builder();
		builder.addReport(() -> (c) -> {
			observedReportIds.add(reportId_1);
		});
		builder.addReport(() -> (c) -> {
			observedReportIds.add(reportId_2);
		});

		ReportsPluginData reportsPluginData = builder.build();

		Set<Consumer<ReportContext>> reports = reportsPluginData.getReports();

		assertNotNull(reports);

		for (Consumer<ReportContext> report : reports) {
			assertNotNull(report);
			report.accept(null);
		}

		assertEquals(observedReportIds, expectedReportIds);

	}

	@Test
	@UnitTestMethod(target = ReportsPluginData.class, name = "getReports", args = {})
	public void testGetReportIds() {

		ReportLabel reportId_1 = new SimpleReportId("report 1");
		ReportLabel reportId_2 = new SimpleReportId("report 2");

		Set<ReportLabel> expectedReportIds = new LinkedHashSet<>();
		expectedReportIds.add(reportId_1);
		expectedReportIds.add(reportId_2);

		Set<ReportLabel> observedReportIds = new LinkedHashSet<>();

		ReportsPluginData.Builder builder = ReportsPluginData.builder();
		builder.addReport(() -> (c) -> {
			observedReportIds.add(reportId_1);
		});
		builder.addReport(() -> (c) -> {
			observedReportIds.add(reportId_2);
		});

		ReportsPluginData reportsPluginData = builder.build();

		Set<Consumer<ReportContext>> reports = reportsPluginData.getReports();

		assertNotNull(reports);

		for (Consumer<ReportContext> report : reports) {
			assertNotNull(report);
			report.accept(null);
		}

		assertEquals(observedReportIds, expectedReportIds);

	}

	@Test
	@UnitTestMethod(target = ReportsPluginData.class, name = "getCloneBuilder", args = {})
	public void testGetCloneBuilder() {
		ReportLabel reportId_1 = new SimpleReportId("report 1");
		ReportLabel reportId_2 = new SimpleReportId("report 2");

		Set<ReportLabel> expectedReportIds = new LinkedHashSet<>();
		expectedReportIds.add(reportId_1);
		expectedReportIds.add(reportId_2);

		Set<ReportLabel> observedReportIds = new LinkedHashSet<>();

		ReportsPluginData.Builder builder = ReportsPluginData.builder();
		builder.addReport(() -> (c) -> {
			observedReportIds.add(reportId_1);
		});
		builder.addReport(() -> (c) -> {
			observedReportIds.add(reportId_2);
		});

		ReportsPluginData reportsPluginData = builder.build();
		ReportsPluginData cloneReportsPluginData = (ReportsPluginData) reportsPluginData.getCloneBuilder().build();

		Set<Consumer<ReportContext>> reports = cloneReportsPluginData.getReports();

		assertNotNull(reports);

		for (Consumer<ReportContext> report : reports) {
			assertNotNull(report);
			report.accept(null);
		}

		assertEquals(observedReportIds, expectedReportIds);
	}

}
