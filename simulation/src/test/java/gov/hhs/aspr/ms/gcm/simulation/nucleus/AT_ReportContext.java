package gov.hhs.aspr.ms.gcm.simulation.nucleus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import org.junit.jupiter.api.Test;

import gov.hhs.aspr.ms.gcm.simulation.nucleus.testsupport.testplugin.TestActorPlan;
import gov.hhs.aspr.ms.gcm.simulation.nucleus.testsupport.testplugin.TestDataManager;
import gov.hhs.aspr.ms.gcm.simulation.nucleus.testsupport.testplugin.TestDataManagerPlan;
import gov.hhs.aspr.ms.gcm.simulation.nucleus.testsupport.testplugin.TestOutputConsumer;
import gov.hhs.aspr.ms.gcm.simulation.nucleus.testsupport.testplugin.TestPlugin;
import gov.hhs.aspr.ms.gcm.simulation.nucleus.testsupport.testplugin.TestPluginData;
import gov.hhs.aspr.ms.gcm.simulation.nucleus.testsupport.testplugin.TestReportPlan;
import gov.hhs.aspr.ms.gcm.simulation.nucleus.testsupport.testplugin.TestSimulation;
import gov.hhs.aspr.ms.util.annotations.UnitTestMethod;
import gov.hhs.aspr.ms.util.errors.ContractException;
import gov.hhs.aspr.ms.util.wrappers.MutableBoolean;
import gov.hhs.aspr.ms.util.wrappers.MutableInteger;

public class AT_ReportContext {

	private static class TestDataManager1 extends TestDataManager {
	}

	private static class TestDataManager3 extends TestDataManager {

	}

	private static class TestDataManager3A extends TestDataManager3 {

	}

	private static class TestDataManager3B extends TestDataManager3 {

	}

	private static class TestDataManager4 extends TestDataManager {

	}

	private static class TestDataManager4A extends TestDataManager4 {

	}

	/*
	 * Executes the simulation by adding TestReport that executes the give consumer
	 * in a task planned at time zero. Also adds a TestActor with a task scheduled
	 * at positive infinity to guarantee the execution of the report's task.
	 */
	private void testConsumer(Consumer<ReportContext> consumer) {
		TestPluginData.Builder pluginDataBuilder = TestPluginData.builder();

		pluginDataBuilder.addTestReportPlan("report", new TestReportPlan(0, consumer));
		pluginDataBuilder.addTestActorPlan("actor", new TestActorPlan(Double.POSITIVE_INFINITY, (c) -> {
		}));

		TestPluginData testPluginData = pluginDataBuilder.build();
		Plugin testPlugin = TestPlugin.getTestPlugin(testPluginData);

		TestSimulation.builder().addPlugin(testPlugin).build().execute();

	}

	@Test
	@UnitTestMethod(target = ReportContext.class, name = "addPlan", args = { Consumer.class, double.class })
	public void testAddPlan_Consumer() {
		TestPluginData.Builder pluginDataBuilder = TestPluginData.builder();

		/*
		 * Show that passive plans do not execute if there are no remaining active
		 * plans. To do this, we will schedule a few passive plans, one active plan and
		 * then a few more passive plans. We will then show that the passive plans that
		 * come after the last active plan never execute
		 */

		// create some containers for passive keys
		Set<Object> expectedOutput = new LinkedHashSet<>();
		expectedOutput.add("A");
		expectedOutput.add("B");
		Set<Object> actualOuput = new LinkedHashSet<>();

		pluginDataBuilder.addTestReportPlan("actor", new TestReportPlan(4, (context) -> {

			// schedule two passive plans
			context.addPlan((c) -> {
				actualOuput.add("A");
			}, 5);
			context.addPlan((c) -> {
				actualOuput.add("B");
			}, 6);

			// schedule two more passive plans
			context.addPlan((c) -> {
				actualOuput.add("C");
			}, 8);
			context.addPlan((c) -> {
				actualOuput.add("D");
			}, 9);

		}));

		pluginDataBuilder.addTestActorPlan("actor", new TestActorPlan(7, (context) -> {
			// place holder active plan that drives time to 7.0
		}));

		// build the plugin
		TestPluginData testPluginData = pluginDataBuilder.build();
		Plugin testPlugin = TestPlugin.getTestPlugin(testPluginData);

		// run the simulation -- we do not need to show that all plans executed
		Simulation.builder()//
				.addPlugin(testPlugin)//
				.build()//
				.execute();//

		// show that the last two passive plans did not execute
		assertEquals(expectedOutput, actualOuput);

		// precondition test : if the plan is null
		ContractException contractException = assertThrows(ContractException.class, () -> testConsumer((c) -> {
			c.addPlan(null, 0);
		}));
		assertEquals(NucleusError.NULL_PLAN_CONSUMER, contractException.getErrorType());

		// precondition test : if the plan is scheduled for the past
		contractException = assertThrows(ContractException.class, () -> testConsumer((c) -> {
			c.addPlan((c2) -> {
			}, -1);
		}));
		assertEquals(NucleusError.PAST_PLANNING_TIME, contractException.getErrorType());

		// precondition test : if the plan is added to the simulation after event processing is finished
		contractException = assertThrows(ContractException.class, () -> testConsumer((c) -> {
			c.addPlan(new ReportPlan(0, (c1) -> {
				c1.subscribeToSimulationClose((c2->{					
					c2.addPlan(c3->{},0);
				}));
			}));
		}));
		assertEquals(NucleusError.PLANNING_QUEUE_CLOSED, contractException.getErrorType());

		

	}

	@Test
	@UnitTestMethod(target = ReportContext.class, name = "addPlan", args = { ReportPlan.class })
	public void testAddPlan_Plan() {
		TestPluginData.Builder pluginDataBuilder = TestPluginData.builder();

		/*
		 * Show that passive plans do not execute if there are no remaining active
		 * plans. To do this, we will schedule a few passive plans, one active plan and
		 * then a few more passive plans. We will then show that the passive plans that
		 * come after the last active plan never execute
		 */

		// create some containers for passive keys
		Set<Object> expectedOutput = new LinkedHashSet<>();
		expectedOutput.add("A");
		expectedOutput.add("B");
		Set<Object> actualOuput = new LinkedHashSet<>();

		pluginDataBuilder.addTestReportPlan("actor", new TestReportPlan(4, (context) -> {

			// schedule two passive plans
			context.addPlan(new ReportPlan(5, (c) -> {
				actualOuput.add("A");
			}));

			context.addPlan(new ReportPlan(6, (c) -> {
				actualOuput.add("B");
			}));

			// schedule two more passive plans
			context.addPlan(new ReportPlan(8, (c) -> {
				actualOuput.add("C");
			}));//

			context.addPlan(new ReportPlan(9, (c) -> {
				actualOuput.add("D");
			}));
		}));

		pluginDataBuilder.addTestActorPlan("actor", new TestActorPlan(7, (context) ->

		{
			// place holder active plan that drives time to 7.0
		}));

		// build the plugin
		TestPluginData testPluginData = pluginDataBuilder.build();
		Plugin testPlugin = TestPlugin.getTestPlugin(testPluginData);

		// run the simulation -- we do not need to show that all plans executed
		Simulation.builder()//
				.addPlugin(testPlugin)//
				.build()//
				.execute();//

		// show that the last two passive plans did not execute
		assertEquals(expectedOutput, actualOuput);


		ContractException contractException = assertThrows(ContractException.class, () -> testConsumer((c) -> {
			c.addPlan(null);
		}));
		assertEquals(NucleusError.NULL_PLAN, contractException.getErrorType());

		// precondition test : if the plan is scheduled for the past
		contractException = assertThrows(ContractException.class, () -> testConsumer((c) -> {
			c.addPlan(new ReportPlan(-1, (c1) -> {
			}));
		}));
		assertEquals(NucleusError.PAST_PLANNING_TIME, contractException.getErrorType());

		// precondition test : if the arrival id is less than -1
		contractException = assertThrows(ContractException.class, () -> testConsumer((c) -> {
			c.addPlan(new ReportPlan(0, -2, (c1) -> {
			}));
		}));
		assertEquals(NucleusError.INVALID_PLAN_ARRIVAL_ID, contractException.getErrorType());

		// precondition test : if the plan is added to the simulation after event processing is finished
		contractException = assertThrows(ContractException.class, () -> testConsumer((c) -> {
			c.addPlan(new ReportPlan(0, (c1) -> {
				c1.subscribeToSimulationClose((c2->{					
					c2.addPlan(new ReportPlan(0,(c3->{})));
				}));
			}));
		}));
		assertEquals(NucleusError.PLANNING_QUEUE_CLOSED, contractException.getErrorType());
	}

	@Test
	@UnitTestMethod(target = ReportContext.class, name = "getDataManager", args = { Class.class })
	public void testGetDataManager() {
		// create the test plugin data builder
		TestPluginData.Builder pluginDataBuilder = TestPluginData.builder();

		// create a data manager for the report to find

		pluginDataBuilder.addTestDataManager("dm1", () -> new TestDataManager1());
		pluginDataBuilder.addTestDataManager("dm3A", () -> new TestDataManager3A());
		pluginDataBuilder.addTestDataManager("dm3B", () -> new TestDataManager3B());
		pluginDataBuilder.addTestDataManager("dm4A", () -> new TestDataManager4A());

		pluginDataBuilder.addTestReportPlan("report", new TestReportPlan(0, (c) -> {
			c.getDataManager(TestDataManager1.class);
			c.getDataManager(TestDataManager3A.class);
			c.getDataManager(TestDataManager3B.class);
			c.getDataManager(TestDataManager4A.class);
		}));

		pluginDataBuilder.addTestActorPlan("actor", new TestActorPlan(Double.POSITIVE_INFINITY, (c) -> {
		}));

		// build the action plugin
		TestPluginData testPluginData = pluginDataBuilder.build();
		Plugin testPlugin = TestPlugin.getTestPlugin(testPluginData);

		TestSimulation.builder().addPlugin(testPlugin).build().execute();

		// precondition test : if the class reference is ambiguous
		pluginDataBuilder.addTestDataManager("dm3A", () -> new TestDataManager3A());
		pluginDataBuilder.addTestDataManager("dm3B", () -> new TestDataManager3B());

		// show that ambiguous class matching throws an exception
		pluginDataBuilder.addTestReportPlan("report", new TestReportPlan(0, (c) -> {
			ContractException contractException = assertThrows(ContractException.class,
					() -> c.getDataManager(TestDataManager3.class));
			assertEquals(NucleusError.AMBIGUOUS_DATA_MANAGER_CLASS, contractException.getErrorType());
		}));

		pluginDataBuilder.addTestActorPlan("actor", new TestActorPlan(Double.POSITIVE_INFINITY, (c) -> {
		}));

		// build the action plugin
		testPluginData = pluginDataBuilder.build();
		testPlugin = TestPlugin.getTestPlugin(testPluginData);

		TestSimulation.builder().addPlugin(testPlugin).build().execute();

		// Precondition test 2
		pluginDataBuilder.addTestReportPlan("report", new TestReportPlan(0, (c) -> {
			ContractException contractException = assertThrows(ContractException.class, () -> c.getDataManager(null));
			assertEquals(NucleusError.NULL_DATA_MANAGER_CLASS, contractException.getErrorType());
		}));
		pluginDataBuilder.addTestActorPlan("actor", new TestActorPlan(Double.POSITIVE_INFINITY, (c) -> {
		}));
		// build the action plugin
		testPluginData = pluginDataBuilder.build();
		testPlugin = TestPlugin.getTestPlugin(testPluginData);

		TestSimulation.builder().addPlugin(testPlugin).build().execute();
	}

	@Test
	@UnitTestMethod(target = ReportContext.class, name = "getReportId", args = {})
	public void testGetReportId() {
		TestPluginData.Builder pluginDataBuilder = TestPluginData.builder();

		double testTime = 1;
		// there are no precondition tests

		Set<ReportId> observedReportIds = new LinkedHashSet<>();
		Set<ReportId> expectedReportIds = new LinkedHashSet<>();
		for (int i = 0; i < 3; i++) {
			expectedReportIds.add(new ReportId(i));
		}

		/*
		 * Have actors get their own actor ids and show that these ids match the
		 * expected values established during the initialization of the TestReports.
		 */
		pluginDataBuilder.addTestReportPlan("Alpha", new TestReportPlan(testTime++, (c) -> {
			ReportId reportId = c.getReportId();
			observedReportIds.add(reportId);
			assertNotNull(reportId);

		}));

		pluginDataBuilder.addTestReportPlan("Beta", new TestReportPlan(testTime++, (c) -> {
			ReportId reportId = c.getReportId();
			observedReportIds.add(reportId);
			assertNotNull(reportId);

		}));

		pluginDataBuilder.addTestReportPlan("Gamma", new TestReportPlan(testTime++, (c) -> {
			ReportId reportId = c.getReportId();
			observedReportIds.add(reportId);
			assertNotNull(reportId);

		}));

		pluginDataBuilder.addTestActorPlan("actor", new TestActorPlan(Double.POSITIVE_INFINITY, (c) -> {
			assertEquals(expectedReportIds, observedReportIds);
		}));

		// build the plugin
		TestPluginData testPluginData = pluginDataBuilder.build();
		Plugin testPlugin = TestPlugin.getTestPlugin(testPluginData);

		TestSimulation.builder().addPlugin(testPlugin).build().execute();
	}

	@Test
	@UnitTestMethod(target = ReportContext.class, name = "getScheduledSimulationHaltTime", args = {})
	public void testGetScheduledSimulationHaltTime() {
		Set<Double> stopTimes = new LinkedHashSet<>();

		stopTimes.add(4.6);
		stopTimes.add(13.0);
		stopTimes.add(554.3);
		stopTimes.add(7.9);
		stopTimes.add(400.2);
		stopTimes.add(3000.1);

		for (Double stopTime : stopTimes) {
			TestPluginData testPluginData = TestPluginData.builder()
					.addTestReportPlan("report plan", new TestReportPlan(0, (context) -> {
						assertEquals(stopTime, context.getScheduledSimulationHaltTime());
					})).addTestActorPlan("actor", new TestActorPlan(1, (c) -> {
					})).build();

			Plugin testPlugin = TestPlugin.getTestPlugin(testPluginData);

			TestSimulation.builder().setSimulationHaltTime(stopTime).addPlugin(testPlugin).build().execute();
		}
	}

	@Test
	@UnitTestMethod(target = ReportContext.class, name = "getTime", args = {})
	public void testGetTime() {
		Set<Double> planTimes = new LinkedHashSet<>();

		planTimes.add(4.6);
		planTimes.add(13.8764);
		planTimes.add(554.345);
		planTimes.add(7.95346);
		planTimes.add(400.234234);
		planTimes.add(3000.12422346);

		/*
		 * Have a report build plans to check the time in the simulation against the
		 * planning time
		 */
		testConsumer((context1) -> {
			for (Double planTime : planTimes) {
				context1.addPlan((context2) -> {
					assertEquals(planTime.doubleValue(), context2.getTime(), 0);
				}, planTime);
			}
		});
	}

	@Test
	@UnitTestMethod(target = ReportContext.class, name = "releaseOutput", args = { Object.class })
	public void testReleaseOutput() {
		// begin building the action plugin
		TestPluginData.Builder pluginDataBuilder = TestPluginData.builder();

		// set up the expected output
		Set<Object> expectedOutput = new LinkedHashSet<>();
		expectedOutput.add("the sly fox");
		expectedOutput.add(15);
		expectedOutput.add("the lazy, brown dog");
		expectedOutput.add(45.34513453);

		// have the agent release the output
		pluginDataBuilder.addTestReportPlan("report", new TestReportPlan(1, (c) -> {
			for (Object outputValue : expectedOutput) {
				c.releaseOutput(outputValue);
			}
		}));

		pluginDataBuilder.addTestActorPlan("actor", new TestActorPlan(Double.POSITIVE_INFINITY, (c) -> {
		}));

		// build the plugin
		TestPluginData testPluginData = pluginDataBuilder.build();
		Plugin testPlugin = TestPlugin.getTestPlugin(testPluginData);

		/*
		 * Add an output consumer that will place the output into the actualOutput set
		 * above and then execute the simulation
		 */
		TestOutputConsumer testOutputConsumer = TestSimulation.builder().addPlugin(testPlugin).build().execute();

		Map<Object, Integer> outputItems = testOutputConsumer.getOutputItemMap(Object.class);
		for (Object key : outputItems.keySet()) {
			Integer count = outputItems.get(key);
			assertEquals(1, count.intValue());
		}

		// show that the output matches expectations
		assertTrue(outputItems.keySet().containsAll(expectedOutput));
	}

	private static class TestEvent1 implements Event {

	}

	@Test
	@UnitTestMethod(target = ReportContext.class, name = "subscribe", args = { Class.class, BiConsumer.class })
	public void testSubscribe() {
		TestPluginData.Builder pluginDataBuilder = TestPluginData.builder();

		MutableBoolean observed = new MutableBoolean();

		// have the report subscribe for test events.
		pluginDataBuilder.addTestReportPlan("report", new TestReportPlan(0, (c) -> {
			c.subscribe(TestEvent1.class, (c2, e) -> {
				observed.setValue(true);
			});
		}));

		// create a data manager that will generate a test event

		pluginDataBuilder.addTestDataManager("generator", () -> new TestDataManager());
		pluginDataBuilder.addTestDataManagerPlan("generator", new TestDataManagerPlan(1, (c) -> {
			c.releaseObservationEvent(new TestEvent1());
		}));

		/*
		 * show that report received the event
		 */
		pluginDataBuilder.addTestActorPlan("actor", new TestActorPlan(Double.POSITIVE_INFINITY, (c) -> {
			assertTrue(observed.getValue());
		}));

		// build the plugin
		TestPluginData testPluginData = pluginDataBuilder.build();
		Plugin testPlugin = TestPlugin.getTestPlugin(testPluginData);

		// build and execute the engine
		TestSimulation.builder().addPlugin(testPlugin).build().execute();

		// precondition test: if the event class is null
		ContractException contractException = assertThrows(ContractException.class, () -> testConsumer((c) -> {
			c.subscribe(null, (c2, e) -> {
			});
		}));
		assertEquals(NucleusError.NULL_EVENT_CLASS, contractException.getErrorType());

		// precondition test: if the event consumer is null
		contractException = assertThrows(ContractException.class, () -> testConsumer((c) -> {
			c.subscribe(TestEvent1.class, null);
		}));
		assertEquals(NucleusError.NULL_EVENT_CONSUMER, contractException.getErrorType());

		// precondition test: if the subsciption duplicates another subscription
		contractException = assertThrows(ContractException.class, () -> testConsumer((c) -> {
			c.subscribe(TestEvent1.class, (c2, e) -> {
			});
			c.subscribe(TestEvent1.class, (c2, e) -> {
			});
		}));
		assertEquals(NucleusError.DUPLICATE_EVENT_SUBSCRIPTION, contractException.getErrorType());

	}

	@Test
	@UnitTestMethod(target = ReportContext.class, name = "subscribeToSimulationClose", args = { Consumer.class })
	public void testSubscribeToSimulationClose() {
		TestPluginData.Builder pluginDataBuilder = TestPluginData.builder();

		MutableBoolean simCloseEventHandled = new MutableBoolean();

		// have a report schedule a few plans and subscribe to simulation close
		pluginDataBuilder.addTestReportPlan("report", new TestReportPlan(0, (c) -> {
			c.addPlan((c2) -> {
			}, 1);
			c.addPlan((c2) -> {
			}, 2);
			c.addPlan((c2) -> {
			}, 3);
			c.subscribeToSimulationClose((c2) -> {
				simCloseEventHandled.setValue(true);
			});
		}));

		pluginDataBuilder.addTestActorPlan("actor", new TestActorPlan(Double.POSITIVE_INFINITY, (c) -> {
		}));

		TestPluginData testPluginData = pluginDataBuilder.build();
		Plugin testPlugin = TestPlugin.getTestPlugin(testPluginData);

		TestSimulation.builder().addPlugin(testPlugin).build().execute();

		// show that the subscription to simulation close was successful
		assertTrue(simCloseEventHandled.getValue());

	}

	@Test
	@UnitTestMethod(target = ReportContext.class, name = "stateRecordingIsScheduled", args = {})
	public void testStateRecordingIsScheduled() {
		Set<Boolean> stateRecordingList = new LinkedHashSet<>();

		stateRecordingList.add(false);
		stateRecordingList.add(false);
		stateRecordingList.add(true);
		stateRecordingList.add(false);
		stateRecordingList.add(true);
		stateRecordingList.add(true);

		for (Boolean stateRecording : stateRecordingList) {
			TestPluginData testPluginData = TestPluginData.builder()
					.addTestReportPlan("actor 1", new TestReportPlan(0, (context) -> {
						assertEquals(stateRecording, context.stateRecordingIsScheduled());
					})).addTestActorPlan("actor", new TestActorPlan(1, (c) -> {
					})).build();

			Plugin testPlugin = TestPlugin.getTestPlugin(testPluginData);

			TestSimulation.builder().setSimulationHaltTime(2).setProduceSimulationStateOnHalt(stateRecording)
					.addPlugin(testPlugin).build().execute();
		}
	}

	@Test
	@UnitTestMethod(target = ReportContext.class, name = "unsubscribe", args = { Class.class })
	public void testUnsubscribe() {
		TestPluginData.Builder pluginDataBuilder = TestPluginData.builder();

		/*
		 * Create a container to count the number of times a subscription execution
		 * occurred
		 */
		MutableInteger observationCount = new MutableInteger();

		/*
		 * have the resolver subscribe to the test event and have it handle each type of
		 * event handling by incrementing a counter
		 */

		int taskTime = 0;

		pluginDataBuilder.addTestReportPlan("report", new TestReportPlan(taskTime++, (c) -> {
			c.subscribe(TestEvent1.class, (c2, e) -> {
				observationCount.increment();
			});
		}));

		// create a data manager that will produce a test event
		pluginDataBuilder.addTestDataManager("generator", () -> new TestDataManager());
		pluginDataBuilder.addTestDataManagerPlan("generator", new TestDataManagerPlan(taskTime++, (c) -> {
			c.releaseObservationEvent(new TestEvent1());
		}));

		pluginDataBuilder.addTestDataManagerPlan("generator", new TestDataManagerPlan(taskTime++, (c) -> {
			c.releaseObservationEvent(new TestEvent1());
		}));

		pluginDataBuilder.addTestDataManagerPlan("generator", new TestDataManagerPlan(taskTime++, (c) -> {
			c.releaseObservationEvent(new TestEvent1());
		}));

		/*
		 * Show that the phaseExecutionCount is three after the the agent is done
		 */
		pluginDataBuilder.addTestActorPlan("actor", new TestActorPlan(taskTime++, (c) -> {
			assertEquals(3, observationCount.getValue());
		}));

		// have the report unsubscribe
		pluginDataBuilder.addTestReportPlan("report", new TestReportPlan(taskTime++, (c) -> {
			c.unsubscribe(TestEvent1.class);
		}));

		// have the data manager generate another test event
		pluginDataBuilder.addTestDataManagerPlan("generator", new TestDataManagerPlan(taskTime++, (c) -> {
			c.releaseObservationEvent(new TestEvent1());
		}));

		/*
		 * Show that the observation count is still three
		 */
		pluginDataBuilder.addTestActorPlan("actor", new TestActorPlan(taskTime++, (c) -> {
			assertEquals(3, observationCount.getValue());
		}));

		// build the plugin
		TestPluginData testPluginData = pluginDataBuilder.build();
		Plugin testPlugin = TestPlugin.getTestPlugin(testPluginData);

		// build and execute the engine
		TestSimulation.builder().addPlugin(testPlugin).build().execute();

		// precondition test: if the event class reference is null
		ContractException contractException = assertThrows(ContractException.class, () -> testConsumer((c) -> {
			c.unsubscribe(null);
		}));
		assertEquals(NucleusError.NULL_EVENT_CLASS, contractException.getErrorType());

	}

	@Test
	@UnitTestMethod(target = ReportContext.class, name = "getSimulationTime", args = { LocalDateTime.class })
	public void testGetSimulationTime() {

		LocalDate localDate = LocalDate.of(2020, 4, 1);

		List<LocalDateTime> localDateTimes = new ArrayList<>();

		localDateTimes.add(LocalDateTime.of(2023, 1, 10, 2, 17, 45));
		localDateTimes.add(LocalDateTime.of(2024, 6, 13, 8, 45, 37));
		localDateTimes.add(LocalDateTime.of(2020, 3, 15, 22, 13, 18));
		localDateTimes.add(LocalDateTime.of(2023, 12, 25, 15, 38, 19));

		TestPluginData.Builder pluginDataBuilder = TestPluginData.builder();
		pluginDataBuilder.addTestReportPlan("report", new TestReportPlan(0, (c) -> {

			SimulationTimeConverter simulationTimeConverter = new SimulationTimeConverter(
					LocalDateTime.of(localDate, LocalTime.of(0, 0)));
			for (LocalDateTime localDateTime : localDateTimes) {
				assertEquals(simulationTimeConverter.getSimulationTime(localDateTime),
						c.getSimulationTime(localDateTime));
			}

		}));

		pluginDataBuilder.addTestActorPlan("actor", new TestActorPlan(1, (c) -> {
		}));

		TestPluginData testPluginData = pluginDataBuilder.build();
		Plugin testPlugin = TestPlugin.getTestPlugin(testPluginData);

		// execute the engine
		SimulationState simulationState = SimulationState.builder().setBaseDate(localDate).build();
		TestSimulation.builder().setSimulationState(simulationState).addPlugin(testPlugin).build().execute();

	}

	@Test
	@UnitTestMethod(target = ReportContext.class, name = "getLocalDateTime", args = { double.class })
	public void testGetLocalDateTime() {

		LocalDate localDate = LocalDate.of(2020, 4, 1);
		List<Double> times = new ArrayList<>();
		times.add(-5.7);
		times.add(-2.234);
		times.add(0.0);
		times.add(3.9);
		times.add(137.765);
		times.add(4000.5437);

		TestPluginData.Builder pluginDataBuilder = TestPluginData.builder();
		pluginDataBuilder.addTestReportPlan("report", new TestReportPlan(0, (c) -> {
			SimulationTimeConverter simulationTimeConverter = new SimulationTimeConverter(
					LocalDateTime.of(localDate, LocalTime.of(0, 0)));
			for (Double time : times) {
				assertEquals(simulationTimeConverter.getLocalDateTime(time), c.getLocalDateTime(time));
			}
		}));

		pluginDataBuilder.addTestActorPlan("actor", new TestActorPlan(1, (c) -> {
		}));

		TestPluginData testPluginData = pluginDataBuilder.build();
		Plugin testPlugin = TestPlugin.getTestPlugin(testPluginData);

		SimulationState simulationState = SimulationState.builder().setBaseDate(localDate).build();
		TestSimulation.builder().setSimulationState(simulationState).addPlugin(testPlugin).build().execute();

	}

	@Test
	@UnitTestMethod(target = ReportContext.class, name = "retrievePlans", args = {})
	public void testRetrievePlans() {
		TestPluginData.Builder pluginDataBuilder = TestPluginData.builder();

		// test preconditions
		pluginDataBuilder.addTestReportPlan("report", new TestReportPlan(1, (context) -> {

			ContractException contractException = assertThrows(ContractException.class, () -> context.retrievePlans());
			assertEquals(NucleusError.PLANNING_QUEUE_ACTIVE, contractException.getErrorType());
		}));

		List<ReportPlan> dmPlans = new ArrayList<>();
		List<ReportPlan> expectedPlans = new ArrayList<>();
		double haltTime = 50;

		for (int i = 1; i <= 100; i++) {
			ReportPlan actorPlan = new ReportPlan(i, (c) -> {
			});
			if (i > 50) {
				expectedPlans.add(actorPlan);
			}
			dmPlans.add(actorPlan);
		}
		/*
		 * Have the actor add a plan and show that that plan executes
		 */

		pluginDataBuilder.addTestReportPlan("report", new TestReportPlan(0, (context) -> {
			for (ReportPlan plan : dmPlans) {
				context.addPlan(plan);
			}

			context.subscribeToSimulationClose(c -> planRetrievalSimCloseSubscribe(c, expectedPlans));
		}));

		// build the plugin
		TestPluginData testPluginData = pluginDataBuilder.build();
		Plugin testPlugin = TestPlugin.getTestPlugin(testPluginData);

		// run the simulation
		Simulation.builder()//
				.addPlugin(testPlugin)//
				.setSimulationHaltTime(haltTime).build()//
				.execute();//
	}

	private void planRetrievalSimCloseSubscribe(ReportContext context, List<ReportPlan> expectedPlans) {
		List<ReportPlan> plans = context.retrievePlans();

		assertEquals(expectedPlans.size(), plans.size());
		assertEquals(expectedPlans, plans);
	}
}
