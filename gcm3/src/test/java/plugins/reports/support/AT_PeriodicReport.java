package plugins.reports.support;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.math3.util.FastMath;
import org.junit.jupiter.api.Test;

import nucleus.ActorContext;
import nucleus.Plugin;
import nucleus.Simulation;
import nucleus.testsupport.testplugin.TestActorPlan;
import nucleus.testsupport.testplugin.TestPlugin;
import nucleus.testsupport.testplugin.TestPluginData;
import plugins.reports.ReportsPlugin;
import plugins.reports.ReportsPluginData;
import tools.annotations.UnitTest;
import tools.annotations.UnitTestConstructor;
import tools.annotations.UnitTestMethod;
import util.errors.ContractException;
import util.wrappers.MutableDouble;
import util.wrappers.MutableInteger;

@UnitTest(target = PeriodicReport.class)
public class AT_PeriodicReport {

	private static class TestReport extends PeriodicReport {

		public TestReport(ReportId reportId, ReportPeriod reportPeriod) {
			super(reportId, reportPeriod);
		}

		@Override
		protected void flush(ActorContext reportContext) {
		}

	}

	/*
	 * Used to test the fillTimeFields() method when the period is
	 * ReportPeriod.DAILY
	 */
	private static class DailyTestReport extends PeriodicReport {

		private MutableInteger testCounter = new MutableInteger();

		public DailyTestReport(ReportId reportId, ReportPeriod reportPeriod) {
			super(reportId, reportPeriod);
			if (reportPeriod != ReportPeriod.DAILY) {
				throw new RuntimeException("must be a daily report");
			}
		}

		@Override
		protected void flush(ActorContext reportContext) {
			testCounter.increment();
			ReportHeader.Builder reportHeaderBuilder = ReportHeader.builder();
			this.addTimeFieldHeaders(reportHeaderBuilder);
			ReportHeader reportHeader = reportHeaderBuilder.build();
			ReportItem.Builder reportItemBuilder = ReportItem.builder();

			fillTimeFields(reportItemBuilder);

			ReportItem reportItem = reportItemBuilder.setReportId(getReportId()).setReportHeader(reportHeader).build();

			int dayValue = (int) FastMath.ceil(reportContext.getTime());
			dayValue--;
			String expectedTimeString = Integer.toString(dayValue);
			String actualTimeString = reportItem.getValue(0);
			assertEquals(expectedTimeString, actualTimeString);

		}

	}

	/*
	 * Used to test the fillTimeFields() method when the period is
	 * ReportPeriod.HOURLY
	 */
	private static class HourlyTestReport extends PeriodicReport {

		private MutableInteger testCounter = new MutableInteger();

		public HourlyTestReport(ReportId reportId, ReportPeriod reportPeriod) {
			super(reportId, reportPeriod);
			if (reportPeriod != ReportPeriod.HOURLY) {
				throw new RuntimeException("must be an hourly report");
			}

		}

		@Override
		protected void flush(ActorContext reportContext) {
			testCounter.increment();
			ReportHeader.Builder reportHeaderBuilder = ReportHeader.builder();
			this.addTimeFieldHeaders(reportHeaderBuilder);
			ReportHeader reportHeader = reportHeaderBuilder.build();
			ReportItem.Builder reportItemBuilder = ReportItem.builder();

			fillTimeFields(reportItemBuilder);

			ReportItem reportItem = reportItemBuilder.setReportId(getReportId()).setReportHeader(reportHeader).build();
			double time = reportContext.getTime();
			double hourScaledTime = time * 24;
			double closestHourTime = FastMath.floor(time * 24);
			int hour = (int) closestHourTime;
			if (hourScaledTime - closestHourTime < 0.01) {
				hour--;
			}
			int expectedDay = hour / 24;
			int expectedHour = hour % 24;
			String expectedHourTimeString = Integer.toString(expectedHour);
			String expectedDayTimeString = Integer.toString(expectedDay);
			String actualDayTimeString = reportItem.getValue(0);
			String actualHourTimeString = reportItem.getValue(1);
			assertEquals(expectedDayTimeString, actualDayTimeString);
			assertEquals(expectedHourTimeString, actualHourTimeString);
		}

	}

	/*
	 * Used to test the fillTimeFields() method when the period is
	 * ReportPeriod.END_OF_SIMULATION
	 */
	private static class EndOfSimulationTestReport extends PeriodicReport {

		private MutableInteger flushCount = new MutableInteger();
		private MutableDouble flushTime = new MutableDouble();

		public EndOfSimulationTestReport(ReportId reportId, ReportPeriod reportPeriod) {
			super(reportId, reportPeriod);
			if (reportPeriod != ReportPeriod.END_OF_SIMULATION) {
				throw new RuntimeException("must be an hourly report");
			}

		}

		@Override
		protected void flush(ActorContext reportContext) {
			flushTime.setValue(reportContext.getTime());
			flushCount.increment();

			ReportHeader.Builder reportHeaderBuilder = ReportHeader.builder();
			addTimeFieldHeaders(reportHeaderBuilder);
			ReportHeader reportHeader = reportHeaderBuilder.build();
			ReportItem.Builder reportItemBuilder = ReportItem.builder();
			fillTimeFields(reportItemBuilder);

			ReportItem reportItem = reportItemBuilder.setReportId(getReportId()).setReportHeader(reportHeader).build();

			assertEquals(0, reportItem.size());
		}

	}

	private static class InitTestReport extends PeriodicReport {
		private List<Double> flushTimes = new ArrayList<>();

		public InitTestReport(ReportId reportId, ReportPeriod reportPeriod) {
			super(reportId, reportPeriod);
		}

		@Override
		protected void flush(ActorContext reportContext) {
			flushTimes.add(reportContext.getTime());
		}

	}

	@Test
	@UnitTestConstructor(args = { ReportPeriod.class })
	public void testConstructor() {
		ContractException contractException = assertThrows(ContractException.class, () -> new TestReport(null, ReportPeriod.DAILY));
		assertEquals(ReportError.NULL_REPORT_ID, contractException.getErrorType());

		contractException = assertThrows(ContractException.class, () -> new TestReport(new SimpleReportId("report"), null));
		assertEquals(ReportError.NULL_REPORT_PERIOD, contractException.getErrorType());

	}

	@Test
	@UnitTestMethod(name = "addTimeFieldHeaders", args = { ReportHeader.Builder.class })
	public void testAddTimeFieldHeaders() {

		ReportHeader.Builder reportHeaderBuilder = ReportHeader.builder();

		ReportId reportId = new SimpleReportId("report");

		TestReport testReport = new TestReport(reportId, ReportPeriod.HOURLY);
		testReport.addTimeFieldHeaders(reportHeaderBuilder);
		ReportHeader reportHeader = reportHeaderBuilder.build();
		List<String> headerStrings = reportHeader.getHeaderStrings();
		assertEquals(2, headerStrings.size());
		assertEquals("day", headerStrings.get(0));
		assertEquals("hour", headerStrings.get(1));

		testReport = new TestReport(reportId, ReportPeriod.DAILY);
		testReport.addTimeFieldHeaders(reportHeaderBuilder);
		reportHeader = reportHeaderBuilder.build();
		headerStrings = reportHeader.getHeaderStrings();
		assertEquals(1, headerStrings.size());
		assertEquals("day", headerStrings.get(0));

		testReport = new TestReport(reportId, ReportPeriod.END_OF_SIMULATION);
		testReport.addTimeFieldHeaders(reportHeaderBuilder);
		reportHeader = reportHeaderBuilder.build();
		headerStrings = reportHeader.getHeaderStrings();
		assertEquals(0, headerStrings.size());

	}

	@Test
	@UnitTestMethod(name = "fillTimeFields", args = { ReportItem.Builder.class })
	public void testFillTimeFields_Daily() {
		double simulationEndTime = 10.6;

		Simulation.Builder builder = Simulation.builder();

		ReportId reportId = new SimpleReportId("report");
		DailyTestReport dailyTestReport = new DailyTestReport(reportId, ReportPeriod.DAILY);
		ReportsPluginData reportsInitialData = ReportsPluginData.builder().addReport(() -> {
			return dailyTestReport::init;
		}).build();

		// add the reports plugin
		builder.addPlugin(ReportsPlugin.getReportPlugin(reportsInitialData));

		TestPluginData.Builder pluginBuilder = TestPluginData.builder();

		// create an agent that will make the engine run for a few days

		pluginBuilder.addTestActorPlan("actor", new TestActorPlan(simulationEndTime, (c) -> {
		}));

		TestPluginData testPluginData = pluginBuilder.build();
		Plugin testPlugin = TestPlugin.getTestPlugin(testPluginData);
		builder.addPlugin(testPlugin);

		// build and execute the simulation
		builder.build().execute();

		// show that the daily test report actually executed its tests
		int expectedFlushExecutionCount = (int) FastMath.ceil(simulationEndTime);
		assertEquals(expectedFlushExecutionCount, dailyTestReport.testCounter.getValue());

	}

	@Test
	@UnitTestMethod(name = "fillTimeFields", args = { ReportItem.Builder.class })
	public void testFillTimeFields_Hourly() {
		double simulationEndTime = 3.6;

		Simulation.Builder builder = Simulation.builder();

		ReportId reportId = new SimpleReportId("report");
		HourlyTestReport hourlyTestReport = new HourlyTestReport(reportId,ReportPeriod.HOURLY);
		ReportsPluginData reportsInitialData = ReportsPluginData.builder().addReport(() -> {
			return hourlyTestReport::init;
		}).build();
		Plugin reportPlugin = ReportsPlugin.getReportPlugin(reportsInitialData);

		// add the reports plugin
		builder.addPlugin(reportPlugin);


		TestPluginData.Builder pluginBuilder = TestPluginData.builder();

		// create an agent that will make the engine run for a few days		
		pluginBuilder.addTestActorPlan("actor", new TestActorPlan(simulationEndTime, (c) -> {
		}));

		TestPluginData testPluginData = pluginBuilder.build();
		Plugin testPlugin = TestPlugin.getTestPlugin(testPluginData);
		builder.addPlugin(testPlugin);

		// build and execute the engine
		builder.build().execute();

		// show that the hourly test report actually executed its tests
		int expectedFlushExecutionCount = (int) FastMath.ceil(24 * simulationEndTime);
		assertEquals(expectedFlushExecutionCount, hourlyTestReport.testCounter.getValue());

	}

	@Test
	@UnitTestMethod(name = "fillTimeFields", args = { ReportItem.Builder.class })
	public void testFillTimeFields_EndOfSimulation() {
		double simulationEndTime = 3.6;

		Simulation.Builder builder = Simulation.builder();

		ReportId reportId = new SimpleReportId("report");
		EndOfSimulationTestReport endOfSimulationTestReport = new EndOfSimulationTestReport(reportId,ReportPeriod.END_OF_SIMULATION);
		ReportsPluginData reportsInitialData = ReportsPluginData.builder().addReport( () -> {
			return endOfSimulationTestReport::init;
		}).build();

		Plugin reportPlugin = ReportsPlugin.getReportPlugin(reportsInitialData);
		
		// add the reports plugin
		builder.addPlugin(reportPlugin);

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
	@UnitTestMethod(name = "init", args = { ActorContext.class })
	public void testInit() {

		for (ReportPeriod reportPeriod : ReportPeriod.values()) {

			double simulationEndTime = 10.6;

			Simulation.Builder builder = Simulation.builder();

			ReportId reportId = new SimpleReportId("report");
			InitTestReport initTestReport = new InitTestReport(reportId,reportPeriod);
			ReportsPluginData reportsInitialData = ReportsPluginData.builder().addReport( () -> {
				return initTestReport::init;
			}).build();

			// add the reports plugin
			Plugin reportPlugin = ReportsPlugin.getReportPlugin(reportsInitialData);
			builder.addPlugin(reportPlugin);


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
				for (int i = 1; i <= lastDay; i++) {
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
				for (int i = 1; i <= lastHour; i++) {
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
			
			TestReport testReport = new TestReport(reportId,ReportPeriod.DAILY);
			ContractException contractException = assertThrows(ContractException.class, () -> testReport.init(null));
			assertEquals(ReportError.NULL_CONTEXT, contractException.getErrorType());
		}

		// precondition tests
		TestReport testReport = new TestReport(new SimpleReportId("report"),ReportPeriod.DAILY);
		ContractException contractException = assertThrows(ContractException.class, () -> testReport.init(null));
		assertEquals(ReportError.NULL_CONTEXT, contractException.getErrorType());
	}

}