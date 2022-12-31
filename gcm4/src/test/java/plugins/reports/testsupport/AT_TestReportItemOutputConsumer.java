package plugins.reports.testsupport;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

import nucleus.Dimension;
import nucleus.Experiment;
import nucleus.ExperimentContext;
import nucleus.Plugin;
import nucleus.testsupport.testplugin.TestActorPlan;
import nucleus.testsupport.testplugin.TestPlugin;
import nucleus.testsupport.testplugin.TestPluginData;
import plugins.reports.support.ReportHeader;
import plugins.reports.support.ReportId;
import plugins.reports.support.ReportItem;
import plugins.reports.support.SimpleReportId;
import tools.annotations.UnitTestConstructor;
import tools.annotations.UnitTestMethod;
import util.wrappers.MutableInteger;

public class AT_TestReportItemOutputConsumer {

	private enum ReportIds implements ReportId {
		REPORT_ID_1(new SimpleReportId("id 1"), ReportHeader.builder().add("A").add("B").build()), //
		REPORT_ID_2(new SimpleReportId("id 2"), ReportHeader.builder().add("C").add("D").add("E").build()), //
		REPORT_ID_3(new SimpleReportId("id 3"), ReportHeader.builder().add("F").add("G").build());//

		private final ReportHeader reportHeader;
		private final ReportId reportId;

		private ReportIds(ReportId reportId, ReportHeader reportHeader) {
			this.reportId = reportId;
			this.reportHeader = reportHeader;
		}
	}

	private static MutableInteger scenarioId = new MutableInteger(-1);

	private final static Map<Integer, Map<ReportItem, Integer>> expectedReportItems = new LinkedHashMap<>();

	@Test
	@UnitTestConstructor(target = TestReportItemOutputConsumer.class, args = {})
	public void testConstructor() {
		// nothing to test
	}

	@Test
	@UnitTestMethod(target = TestReportItemOutputConsumer.class, name = "getReportItems", args = {})
	public void testGetReportItems() {
		/*
		 * Have an actor generate a few report items with occasional duplicate
		 * over two scenarios. Record expected results and compare to the report
		 * items returned by the
		 */

		TestReportItemOutputConsumer testReportItemOutputConsumer = new TestReportItemOutputConsumer();

		Experiment.Builder experimentBuilder = Experiment.builder();
		experimentBuilder.addExperimentContextConsumer(testReportItemOutputConsumer::init);
		TestPluginData.Builder pluginBuilder = TestPluginData.builder();

		pluginBuilder.addTestActorPlan("scenario tracker", new TestActorPlan(0, (c) -> {
			scenarioId.increment();
		}));

		pluginBuilder.addTestActorPlan("actor", new TestActorPlan(1, (c) -> {

			ReportItem reportItem;
			if (scenarioId.getValue() == 0) {
				reportItem = generateReportItem(ReportIds.REPORT_ID_1, 3.4, false);
			} else {
				reportItem = generateReportItem(ReportIds.REPORT_ID_1, 6.7, true);
			}

			record(reportItem, 2);
			c.releaseOutput(reportItem);
			c.releaseOutput(reportItem);
		}));

		pluginBuilder.addTestActorPlan("actor", new TestActorPlan(2, (c) -> {

			ReportItem reportItem;
			if (scenarioId.getValue() == 0) {
				reportItem = generateReportItem(ReportIds.REPORT_ID_2, 12, "tango", false);
			} else {
				reportItem = generateReportItem(ReportIds.REPORT_ID_3, "bravo", 3.4);
			}

			record(reportItem, 1);
			c.releaseOutput(reportItem);
		}));

		pluginBuilder.addTestActorPlan("actor", new TestActorPlan(3, (c) -> {

			ReportItem reportItem;
			if (scenarioId.getValue() == 0) {
				reportItem = generateReportItem(ReportIds.REPORT_ID_1, 6.29, true);
			} else {
				reportItem = generateReportItem(ReportIds.REPORT_ID_2, 45, "foxtrot", true);
			}

			record(reportItem, 3);
			c.releaseOutput(reportItem);
			c.releaseOutput(reportItem);
			c.releaseOutput(reportItem);
		}));

		TestPluginData testPluginData = pluginBuilder.build();
		Plugin testPlugin = TestPlugin.getTestPlugin(testPluginData);

		Dimension dimension = Dimension	.builder()//
										.addLevel((t) -> {
											ArrayList<String> result = new ArrayList<>();
											result.add("alpha1");
											return result;
										})//
										.addLevel((t) -> {
											ArrayList<String> result = new ArrayList<>();
											result.add("alpha2");
											return result;
										})//
										.addMetaDatum("Alpha")//
										.build();//

		experimentBuilder.addDimension(dimension);
		experimentBuilder.addPlugin(testPlugin);
		experimentBuilder.setThreadCount(0);
		experimentBuilder.build().execute();

		Map<Integer, Map<ReportItem, Integer>> actualReportItems = testReportItemOutputConsumer.getReportItems();
		assertEquals(expectedReportItems, actualReportItems);

	}

	private static void record(ReportItem reportItem, int count) {
		int scenId = scenarioId.getValue();
		Map<ReportItem, Integer> map = expectedReportItems.get(scenId);
		if (map == null) {
			map = new LinkedHashMap<>();
			expectedReportItems.put(scenId, map);
		}
		map.put(reportItem, count);
	}

	private static ReportItem generateReportItem(ReportIds reportIds, Object... values) {

		ReportItem.Builder builder = ReportItem.builder();//
		builder.setReportId(reportIds.reportId);//
		builder.setReportHeader(reportIds.reportHeader);//
		for (Object value : values) {
			builder.addValue(value);
		}
		return builder.build();//
	}

	@Test
	@UnitTestMethod(target = TestReportItemOutputConsumer.class, name = "init", args = { ExperimentContext.class })
	public void testInit() {
		// covered by testGetReportItems
	}

}
