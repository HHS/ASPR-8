package plugins.reports;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.LinkedHashSet;
import java.util.Set;

import org.junit.jupiter.api.Test;

import nucleus.Plugin;
import nucleus.Simulation;
import plugins.reports.support.ReportId;
import plugins.reports.support.SimpleReportId;
import tools.annotations.UnitTest;
import tools.annotations.UnitTestMethod;

@UnitTest(target = ReportsPlugin.class)
public class AT_ReportPlugin {

	@Test
	@UnitTestMethod(name = "getReportPlugin", args = {})
	public void testGetReportPlugin() {
		//Build the report plugin from two reports
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

		Plugin reportPlugin = ReportsPlugin.getReportsPlugin(reportsPluginData);
		
		//show that the report has the reports plugin data
		reportPlugin.getPluginDatas().contains(reportsPluginData);
		
		//show that the plugin has the correct idea
		assertEquals(ReportsPluginId.PLUGIN_ID, reportPlugin.getPluginId());

		//show that plugin correctly adds the reports to the simulation 
		Simulation.builder().addPlugin(reportPlugin).build().execute();

		assertEquals(observedReportIds, expectedReportIds);

	}

}
