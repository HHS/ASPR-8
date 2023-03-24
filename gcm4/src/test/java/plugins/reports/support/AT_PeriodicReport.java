package plugins.reports.support;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.math3.util.FastMath;
import org.junit.jupiter.api.Test;

import nucleus.Plugin;
import nucleus.ReportContext;
import nucleus.SimplePluginId;
import nucleus.Simulation;
import nucleus.testsupport.testplugin.TestActorPlan;
import nucleus.testsupport.testplugin.TestOutputConsumer;
import nucleus.testsupport.testplugin.TestPlugin;
import nucleus.testsupport.testplugin.TestPluginData;
import nucleus.testsupport.testplugin.TestSimulation;
import util.annotations.UnitTestConstructor;
import util.annotations.UnitTestMethod;
import util.errors.ContractException;
import util.wrappers.MutableDouble;
import util.wrappers.MutableInteger;

public class AT_PeriodicReport {

	private static class TestReport extends PeriodicReport {

		public TestReport(ReportLabel reportLabel, ReportPeriod reportPeriod) {
			super(reportLabel, reportPeriod);
		}

		@Override
		protected void flush(ReportContext reportContext) {
		}

	}

	/*
	 * Used to test the fillTimeFields() method when the period is
	 * ReportPeriod.DAILY
	 */
	private static class DailyTestReport extends PeriodicReport {

		private MutableInteger testCounter = new MutableInteger();

		public DailyTestReport(ReportLabel reportLabel, ReportPeriod reportPeriod) {
			super(reportLabel, reportPeriod);
			if (reportPeriod != ReportPeriod.DAILY) {
				throw new RuntimeException("must be a daily report");
			}
		}

		@Override
		protected void flush(ReportContext reportContext) {
			testCounter.increment();
			ReportHeader.Builder reportHeaderBuilder = ReportHeader.builder();
			this.addTimeFieldHeaders(reportHeaderBuilder);
			ReportHeader reportHeader = reportHeaderBuilder.build();
			ReportItem.Builder reportItemBuilder = ReportItem.builder();

			fillTimeFields(reportItemBuilder);

			ReportItem reportItem = reportItemBuilder.setReportLabel(getReportLabel()).setReportHeader(reportHeader).build();

			int dayValue = (int) FastMath.ceil(reportContext.getTime());

			String expectedTimeString = Integer.toString(dayValue);

			String actualTimeString = reportItem.getValue(0);

			assertEquals(expectedTimeString, actualTimeString);
			reportContext.releaseOutput(reportItem);

		}

	}

	/*
	 * Used to test the fillTimeFields() method when the period is
	 * ReportPeriod.HOURLY
	 */
	private static class HourlyTestReport extends PeriodicReport {

		private MutableInteger testCounter = new MutableInteger();

		public HourlyTestReport(ReportLabel reportLabel, ReportPeriod reportPeriod) {
			super(reportLabel, reportPeriod);
			if (reportPeriod != ReportPeriod.HOURLY) {
				throw new RuntimeException("must be an hourly report");
			}

		}

		@Override
		protected void flush(ReportContext reportContext) {
			testCounter.increment();
			ReportHeader.Builder reportHeaderBuilder = ReportHeader.builder();
			this.addTimeFieldHeaders(reportHeaderBuilder);
			ReportHeader reportHeader = reportHeaderBuilder.build();
			ReportItem.Builder reportItemBuilder = ReportItem.builder();

			fillTimeFields(reportItemBuilder);

			ReportItem reportItem = reportItemBuilder.setReportLabel(getReportLabel()).setReportHeader(reportHeader).build();
			double time = reportContext.getTime();

			int hour = (int) FastMath.ceil(time * 24);
			int expectedDay = hour / 24;
			int expectedHour = hour % 24;
			String expectedHourTimeString = Integer.toString(expectedHour);
			String expectedDayTimeString = Integer.toString(expectedDay);
			String actualDayTimeString = reportItem.getValue(0);
			String actualHourTimeString = reportItem.getValue(1);
			assertEquals(expectedDayTimeString, actualDayTimeString);
			assertEquals(expectedHourTimeString, actualHourTimeString);

			reportContext.releaseOutput(reportItem);
		}

	}

	/*
	 * Used to test the fillTimeFields() method when the period is
	 * ReportPeriod.END_OF_SIMULATION
	 */
	private static class EndOfSimulationTestReport extends PeriodicReport {

		private MutableInteger flushCount = new MutableInteger();
		private MutableDouble flushTime = new MutableDouble();

		public EndOfSimulationTestReport(ReportLabel reportLabel, ReportPeriod reportPeriod) {
			super(reportLabel, reportPeriod);
			if (reportPeriod != ReportPeriod.END_OF_SIMULATION) {
				throw new RuntimeException("must be an end of simulation report");
			}
		}

		@Override
		protected void flush(ReportContext reportContext) {
			flushTime.setValue(reportContext.getTime());
			flushCount.increment();

			ReportHeader.Builder reportHeaderBuilder = ReportHeader.builder();
			addTimeFieldHeaders(reportHeaderBuilder);
			ReportHeader reportHeader = reportHeaderBuilder.build();
			ReportItem.Builder reportItemBuilder = ReportItem.builder();
			fillTimeFields(reportItemBuilder);

			ReportItem reportItem = reportItemBuilder.setReportLabel(getReportLabel()).setReportHeader(reportHeader).build();

			assertEquals(0, reportItem.size());
		}

	}

	private static class InitTestReport extends PeriodicReport {
		private List<Double> flushTimes = new ArrayList<>();

		public InitTestReport(ReportLabel reportLabel, ReportPeriod reportPeriod) {
			super(reportLabel, reportPeriod);
		}

		@Override
		protected void flush(ReportContext reportContext) {
			flushTimes.add(reportContext.getTime());
		}

	}

	@Test
	@UnitTestConstructor(target = PeriodicReport.class, args = { ReportLabel.class, ReportPeriod.class })
	public void testConstructor() {
		ContractException contractException = assertThrows(ContractException.class, () -> new TestReport(null, ReportPeriod.DAILY));
		assertEquals(ReportError.NULL_REPORT_LABEL, contractException.getErrorType());

		contractException = assertThrows(ContractException.class, () -> new TestReport(new SimpleReportLabel("report"), null));
		assertEquals(ReportError.NULL_REPORT_PERIOD, contractException.getErrorType());

	}

	@Test
	@UnitTestMethod(target = PeriodicReport.class, name = "init", args = { ReportContext.class })
	public void testAddTimeFieldHeaders() {

		ReportHeader.Builder reportHeaderBuilder = ReportHeader.builder();

		ReportLabel reportLabel = new SimpleReportLabel("report");

		TestReport testReport = new TestReport(reportLabel, ReportPeriod.HOURLY);
		testReport.addTimeFieldHeaders(reportHeaderBuilder);
		ReportHeader reportHeader = reportHeaderBuilder.build();
		List<String> headerStrings = reportHeader.getHeaderStrings();
		assertEquals(2, headerStrings.size());
		assertEquals("day", headerStrings.get(0));
		assertEquals("hour", headerStrings.get(1));

		testReport = new TestReport(reportLabel, ReportPeriod.DAILY);
		testReport.addTimeFieldHeaders(reportHeaderBuilder);
		reportHeader = reportHeaderBuilder.build();
		headerStrings = reportHeader.getHeaderStrings();
		assertEquals(1, headerStrings.size());
		assertEquals("day", headerStrings.get(0));

		testReport = new TestReport(reportLabel, ReportPeriod.END_OF_SIMULATION);
		testReport.addTimeFieldHeaders(reportHeaderBuilder);
		reportHeader = reportHeaderBuilder.build();
		headerStrings = reportHeader.getHeaderStrings();
		assertEquals(0, headerStrings.size());

	}

	@Test
	@UnitTestMethod(target = PeriodicReport.class, name = "init", args = { ReportContext.class })
	public void testFillTimeFields_Daily() {
		double simulationEndTime = 10.6;

		List<Plugin> plugins = new ArrayList<>();

		ReportLabel reportLabel = new SimpleReportLabel("report");
		DailyTestReport dailyTestReport = new DailyTestReport(reportLabel, ReportPeriod.DAILY);

		plugins.add(Plugin	.builder()//
							.setPluginId(new SimplePluginId("anonymous plugin"))//
							.setInitializer((pc) -> {
								pc.addReport(dailyTestReport::init);
							})//
							.build());

		// add the reports plugin

		TestPluginData.Builder pluginBuilder = TestPluginData.builder();

		// create an agent that will make the engine run for a few days

		pluginBuilder.addTestActorPlan("actor", new TestActorPlan(simulationEndTime, (c) -> {
		}));

		TestPluginData testPluginData = pluginBuilder.build();
		Plugin testPlugin = TestPlugin.getTestPlugin(testPluginData);
		plugins.add(testPlugin);

		// build and execute the simulation

		TestOutputConsumer testOutputConsumer = new TestOutputConsumer();
		TestSimulation.executeSimulation(plugins, testOutputConsumer);

		int maxDay = (int) FastMath.ceil(simulationEndTime);
		Set<Integer> expectedDays = new LinkedHashSet<>();
		for (int i = 0; i <= maxDay; i++) {
			expectedDays.add(i);
		}

		Set<Integer> actualDays = new LinkedHashSet<>();
		Map<ReportItem, Integer> outputItems = testOutputConsumer.getOutputItems(ReportItem.class);
		for (ReportItem reportItem : outputItems.keySet()) {
			assertEquals(1, outputItems.get(reportItem));
			actualDays.add(Integer.parseInt(reportItem.getValue(0)));
		}
		assertEquals(expectedDays, actualDays);

	}

	@Test
	@UnitTestMethod(target = PeriodicReport.class, name = "init", args = { ReportContext.class })
	public void testFillTimeFields_Hourly() {
		double simulationEndTime = 3.6;

		List<Plugin> plugins = new ArrayList<>();

		ReportLabel reportLabel = new SimpleReportLabel("report");
		HourlyTestReport hourlyTestReport = new HourlyTestReport(reportLabel, ReportPeriod.HOURLY);

		plugins.add(Plugin	.builder()//
				.setPluginId(new SimplePluginId("anonymous plugin"))//
				.setInitializer((pc) -> {
					pc.addReport(hourlyTestReport::init);
				})//
				.build());

		TestPluginData.Builder pluginBuilder = TestPluginData.builder();

		// create an agent that will make the engine run for a few days
		pluginBuilder.addTestActorPlan("actor", new TestActorPlan(simulationEndTime, (c) -> {
		}));

		TestPluginData testPluginData = pluginBuilder.build();
		Plugin testPlugin = TestPlugin.getTestPlugin(testPluginData);
		plugins.add(testPlugin);

		// build and execute the engine
		TestOutputConsumer testOutputConsumer = new TestOutputConsumer();
		TestSimulation.executeSimulation(plugins, testOutputConsumer);

		// hours 0 through 3d 15h inclusive
		Set<Integer> expectedHours = new LinkedHashSet<>();
		for (int i = 0; i < 88; i++) {
			expectedHours.add(i);
		}

		Set<Integer> actualHours = new LinkedHashSet<>();
		Map<ReportItem, Integer> outputItems = testOutputConsumer.getOutputItems(ReportItem.class);
		for (ReportItem reportItem : outputItems.keySet()) {
			Integer count = outputItems.get(reportItem);
			assertEquals(1, count);
			int hour = Integer.parseInt(reportItem.getValue(0));
			hour *= 24;
			hour += Integer.parseInt(reportItem.getValue(1));
			actualHours.add(hour);
		}

		assertEquals(expectedHours, actualHours);

	}

	@Test
	@UnitTestMethod(target = PeriodicReport.class, name = "init", args = { ReportContext.class })
	public void testFillTimeFields_EndOfSimulation() {
		double simulationEndTime = 3.6;

		Simulation.Builder builder = Simulation.builder();

		ReportLabel reportLabel = new SimpleReportLabel("report");
		EndOfSimulationTestReport endOfSimulationTestReport = new EndOfSimulationTestReport(reportLabel, ReportPeriod.END_OF_SIMULATION);
		builder.addPlugin(Plugin	.builder()//
				.setPluginId(new SimplePluginId("anonymous plugin"))//
				.setInitializer((pc) -> {
					pc.addReport(endOfSimulationTestReport::init);
				})//
				.build());


		TestPluginData.Builder pluginBuilder = TestPluginData.builder();

		// create an agent that will make the engine run for a few days

		pluginBuilder.addTestActorPlan("actor", new TestActorPlan(simulationEndTime, (c) -> {
		}));

		TestPluginData testPluginData = pluginBuilder.build();
		Plugin testPlugin = TestPlugin.getTestPlugin(testPluginData);
		builder.addPlugin(testPlugin);

		// build and execute the engine
		builder.build().execute();

		/*
		 * Show that the end of simulation test report executed its test exactly
		 * once at the end of the simulation
		 */

		assertEquals(1, endOfSimulationTestReport.flushCount.getValue());
		assertEquals(simulationEndTime, endOfSimulationTestReport.flushTime.getValue());

	}

	@Test
	@UnitTestMethod(target = PeriodicReport.class, name = "init", args = { ReportContext.class })
	public void testInit() {

		for (ReportPeriod reportPeriod : ReportPeriod.values()) {

			double simulationEndTime = 10.6;

			Simulation.Builder builder = Simulation.builder();

			ReportLabel reportLabel = new SimpleReportLabel("report");
			InitTestReport initTestReport = new InitTestReport(reportLabel, reportPeriod);
			
			builder.addPlugin(Plugin	.builder()//
					.setPluginId(new SimplePluginId("anonymous plugin"))//
					.setInitializer((pc) -> {
						pc.addReport(initTestReport::init);
					})//
					.build());
			

			TestPluginData.Builder pluginBuilder = TestPluginData.builder();

			// create an agent that will make the engine run for a few days

			pluginBuilder.addTestActorPlan("actor", new TestActorPlan(simulationEndTime, (c) -> {
			}));

			TestPluginData testPluginData = pluginBuilder.build();
			Plugin testPlugin = TestPlugin.getTestPlugin(testPluginData);
			builder.addPlugin(testPlugin);

			// build and execute the engine
			builder.build().execute();

			// show that the report is flushed at the end of the simulation
			int lastIndex = initTestReport.flushTimes.size() - 1;
			assertTrue(lastIndex >= 0);
			double lastFlushTime = initTestReport.flushTimes.get(lastIndex);
			assertEquals(simulationEndTime, lastFlushTime);

			// show that the report is flushed on the given reporting period

			// calculate the expected flushing times as a function of the report
			// period
			List<Double> expectedTimes = new ArrayList<>();
			switch (reportPeriod) {
			case DAILY:
				int lastDay = (int) simulationEndTime;
				for (int i = 0; i <= lastDay; i++) {
					double time = i;
					expectedTimes.add(time);
				}
				expectedTimes.add(simulationEndTime);
				break;
			case END_OF_SIMULATION:
				expectedTimes.add(simulationEndTime);
				// expectedTimes.addAll(initTestReport.flushTimes);
				break;
			case HOURLY:

				int lastHour = (int) (simulationEndTime * 24);
				for (int i = 0; i <= lastHour; i++) {
					double time = i;
					time /= 24;
					expectedTimes.add(time);
				}
				expectedTimes.add(simulationEndTime);

				break;
			default:
				throw new RuntimeException("unhandled report period " + reportPeriod);
			}

			// show that the expected and actual times are reasonably equal
			assertEquals(expectedTimes.size(), initTestReport.flushTimes.size());

			for (int i = 0; i < expectedTimes.size(); i++) {
				assertEquals(expectedTimes.get(i), initTestReport.flushTimes.get(i), 0.00001);
			}

			// precondition tests

			TestReport testReport = new TestReport(reportLabel, ReportPeriod.DAILY);
			ContractException contractException = assertThrows(ContractException.class, () -> testReport.init(null));
			assertEquals(ReportError.NULL_CONTEXT, contractException.getErrorType());
		}

		// precondition tests
		TestReport testReport = new TestReport(new SimpleReportLabel("report"), ReportPeriod.DAILY);
		ContractException contractException = assertThrows(ContractException.class, () -> testReport.init(null));
		assertEquals(ReportError.NULL_CONTEXT, contractException.getErrorType());
	}

}
