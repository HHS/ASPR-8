package plugins.reports;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Supplier;

import org.junit.jupiter.api.Test;

import nucleus.ActorContext;
import plugins.reports.support.ReportId;
import plugins.reports.support.SimpleReportId;
import tools.annotations.UnitTest;
import tools.annotations.UnitTestMethod;

@UnitTest(target = ReportsPluginData.class)
public class AT_ReportsPluginData {

	@Test
	@UnitTestMethod(name = "builder", args = {})
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
		ReportId reportId_1 = new SimpleReportId("report 1");
		ReportId reportId_2 = new SimpleReportId("report 2");

		Set<ReportId> expectedReportIds = new LinkedHashSet<>();
		expectedReportIds.add(reportId_1);
		expectedReportIds.add(reportId_2);

		Set<ReportId> observedReportIds = new LinkedHashSet<>();

		ReportsPluginData.Builder builder = ReportsPluginData.builder();
		builder.addReport(() -> (c) -> {
			observedReportIds.add(reportId_1);
		});
		builder.addReport(() -> (c) -> {
			observedReportIds.add(reportId_2);
		});

		ReportsPluginData reportsPluginData = builder.build();

		Set<Consumer<ActorContext>> reports = reportsPluginData.getReports();

		assertNotNull(reports);

		for (Consumer<ActorContext> report : reports) {
			assertNotNull(report);
			report.accept(null);
		}

		assertEquals(observedReportIds, expectedReportIds);

	}

	@Test
	@UnitTestMethod(name = "getReports", args = {})
	public void testGetReportIds() {

		ReportId reportId_1 = new SimpleReportId("report 1");
		ReportId reportId_2 = new SimpleReportId("report 2");

		Set<ReportId> expectedReportIds = new LinkedHashSet<>();
		expectedReportIds.add(reportId_1);
		expectedReportIds.add(reportId_2);

		Set<ReportId> observedReportIds = new LinkedHashSet<>();

		ReportsPluginData.Builder builder = ReportsPluginData.builder();
		builder.addReport(() -> (c) -> {
			observedReportIds.add(reportId_1);
		});
		builder.addReport(() -> (c) -> {
			observedReportIds.add(reportId_2);
		});

		ReportsPluginData reportsPluginData = builder.build();

		Set<Consumer<ActorContext>> reports = reportsPluginData.getReports();

		assertNotNull(reports);

		for (Consumer<ActorContext> report : reports) {
			assertNotNull(report);
			report.accept(null);
		}

		assertEquals(observedReportIds, expectedReportIds);

	}

	@Test
	@UnitTestMethod(name = "getCloneBuilder", args = {})
	public void testGetCloneBuilder() {
		ReportId reportId_1 = new SimpleReportId("report 1");
		ReportId reportId_2 = new SimpleReportId("report 2");

		Set<ReportId> expectedReportIds = new LinkedHashSet<>();
		expectedReportIds.add(reportId_1);
		expectedReportIds.add(reportId_2);

		Set<ReportId> observedReportIds = new LinkedHashSet<>();

		ReportsPluginData.Builder builder = ReportsPluginData.builder();
		builder.addReport(() -> (c) -> {
			observedReportIds.add(reportId_1);
		});
		builder.addReport(() -> (c) -> {
			observedReportIds.add(reportId_2);
		});

		ReportsPluginData reportsPluginData = builder.build();
		ReportsPluginData cloneReportsPluginData = (ReportsPluginData)reportsPluginData.getCloneBuilder().build();

		Set<Consumer<ActorContext>> reports = cloneReportsPluginData.getReports();

		assertNotNull(reports);

		for (Consumer<ActorContext> report : reports) {
			assertNotNull(report);
			report.accept(null);
		}

		assertEquals(observedReportIds, expectedReportIds);
	}

}