package plugins.reports;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.LinkedHashSet;
import java.util.Set;

import org.junit.jupiter.api.Test;

import nucleus.Plugin;
import nucleus.Simulation;
import plugins.reports.support.ReportLabel;
import plugins.reports.support.SimpleReportLabel;
import util.annotations.UnitTestMethod;

public class AT_ReportPlugin {

	@Test
	@UnitTestMethod(target = ReportsPlugin.class, name = "getReportsPlugin", args = { ReportsPluginData.class })
	public void testGetReportPlugin() {
		// Build the report plugin from two reports
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

		Plugin reportPlugin = ReportsPlugin.getReportsPlugin(reportsPluginData);

		// show that the report has the reports plugin data
		reportPlugin.getPluginDatas().contains(reportsPluginData);

		// show that the plugin has the correct idea
		assertEquals(ReportsPluginId.PLUGIN_ID, reportPlugin.getPluginId());

		// show that plugin correctly adds the reports to the simulation
		Simulation.builder().addPlugin(reportPlugin).build().execute();

		assertEquals(observedReportLabels, expectedReportLabels);

	}

}
