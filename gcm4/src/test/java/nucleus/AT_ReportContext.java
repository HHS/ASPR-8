package nucleus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

import nucleus.testsupport.testplugin.TestActorPlan;
import nucleus.testsupport.testplugin.TestDataManager;
import nucleus.testsupport.testplugin.TestDataManagerPlan;
import nucleus.testsupport.testplugin.TestOutputConsumer;
import nucleus.testsupport.testplugin.TestPlugin;
import nucleus.testsupport.testplugin.TestPluginData;
import nucleus.testsupport.testplugin.TestReportPlan;
import nucleus.testsupport.testplugin.TestSimulation;
import util.annotations.UnitTag;
import util.annotations.UnitTestMethod;
import util.errors.ContractException;
import util.wrappers.MutableBoolean;
import util.wrappers.MutableInteger;

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

	@Test
	@UnitTestMethod(target = ReportContext.class, name = "addKeyedPlan", args = { Consumer.class, double.class, Object.class })
	public void testAddKeyedPlan() {
		TestPluginData.Builder pluginDataBuilder = TestPluginData.builder();

		/*
		 * have the added test report add a plan that can be retrieved and thus
		 * was added successfully
		 */
		pluginDataBuilder.addTestReportPlan("report", new TestReportPlan(2, (context) -> {
			Object key = new Object();

			assertFalse(context.getPlan(key).isPresent());

			context.addKeyedPlan((c) -> {
			}, 3, key);

			assertTrue(context.getPlan(key).isPresent());
		}));

		/*
		 * Show that passive plans do not execute if there are no remaining
		 * active plans. To do this, we will schedule a few passive plans, one
		 * active plan and then a few more passive plans. We will then show that
		 * the passive plans that come after the last active plan never execute
		 */

		// create some containers for passive keys
		Set<Object> expectedPassiveKeys = new LinkedHashSet<>();
		expectedPassiveKeys.add("A");
		expectedPassiveKeys.add("B");
		Set<Object> actualPassiveKeys = new LinkedHashSet<>();

		pluginDataBuilder.addTestReportPlan("actor", new TestReportPlan(4, (context) -> {

			// schedule two passive plans
			context.addKeyedPlan((c) -> {
				actualPassiveKeys.add("A");
			}, 5, "A");
			context.addKeyedPlan((c) -> {
				actualPassiveKeys.add("B");
			}, 6, "B");

			// schedule two more passive plans
			context.addKeyedPlan((c) -> {
				actualPassiveKeys.add("C");
			}, 8, "C");
			context.addKeyedPlan((c) -> {
				actualPassiveKeys.add("D");
			}, 9, "D");

		}));

		pluginDataBuilder.addTestActorPlan("actor", new TestActorPlan(7, (context) -> {
			// place holder active plan that drives time to 7.0
		}));

		// build the plugin
		TestPluginData testPluginData = pluginDataBuilder.build();
		Plugin testPlugin = TestPlugin.getTestPlugin(testPluginData);

		// run the simulation -- we do not need to show that all plans executed
		Simulation	.builder()//
					.addPlugin(testPlugin)//
					.build()//
					.execute();//

		// show that the last two passive plans did not execute
		assertEquals(expectedPassiveKeys, actualPassiveKeys);

		// precondition test : if the plan is null
		ContractException contractException = assertThrows(ContractException.class, () -> testConsumer((c) -> {
			c.addKeyedPlan(null, 0, "key");
		}));
		assertEquals(NucleusError.NULL_PLAN, contractException.getErrorType());

		// precondition test : if the plan is scheduled for the past
		contractException = assertThrows(ContractException.class, () -> testConsumer((c) -> {
			c.addKeyedPlan((c2) -> {
			}, -1, "key");
		}));
		assertEquals(NucleusError.PAST_PLANNING_TIME, contractException.getErrorType());

		// precondition test : if the plan key is null
		contractException = assertThrows(ContractException.class, () -> testConsumer((c) -> {
			c.addKeyedPlan((c2) -> {
			}, 0, null);
		}));
		assertEquals(NucleusError.NULL_PLAN_KEY, contractException.getErrorType());

		// precondition test : if the plan key duplicates an existing plan
		contractException = assertThrows(ContractException.class, () -> testConsumer((c) -> {
			c.addKeyedPlan((c2) -> {
			}, 0, "key");
			c.addKeyedPlan((c2) -> {
			}, 0, "key");
		}));
		assertEquals(NucleusError.DUPLICATE_PLAN_KEY, contractException.getErrorType());
	}

	/*
	 * Executes the simulation by adding TestReport that executes the give
	 * consumer in a task planned at time zero. Also adds a TestActor with a
	 * task scheduled at positive infinity to guarantee the execution of the
	 * report's task.
	 */
	private void testConsumer(Consumer<ReportContext> consumer) {
		TestPluginData.Builder pluginDataBuilder = TestPluginData.builder();

		pluginDataBuilder.addTestReportPlan("report", new TestReportPlan(0, consumer));
		pluginDataBuilder.addTestActorPlan("actor", new TestActorPlan(Double.POSITIVE_INFINITY, (c) -> {
		}));

		TestPluginData testPluginData = pluginDataBuilder.build();
		Plugin testPlugin = TestPlugin.getTestPlugin(testPluginData);

		TestSimulation.executeSimulation(testPlugin);

	}

	@Test
	@UnitTestMethod(target = ReportContext.class, name = "addPlan", args = { Consumer.class, double.class })
	public void testAddPlan() {
		TestPluginData.Builder pluginDataBuilder = TestPluginData.builder();

		/*
		 * Show that passive plans do not execute if there are no remaining
		 * active plans. To do this, we will schedule a few passive plans, one
		 * active plan and then a few more passive plans. We will then show that
		 * the passive plans that come after the last active plan never execute
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
		Simulation	.builder()//
					.addPlugin(testPlugin)//
					.build()//
					.execute();//

		// show that the last two passive plans did not execute
		assertEquals(expectedOutput, actualOuput);

		// precondition test : if the plan is null
		ContractException contractException = assertThrows(ContractException.class, () -> testConsumer((c) -> {
			c.addPlan(null, 0);
		}));
		assertEquals(NucleusError.NULL_PLAN, contractException.getErrorType());

		// precondition test : if the plan is scheduled for the past
		contractException = assertThrows(ContractException.class, () -> testConsumer((c) -> {
			c.addPlan((c2) -> {
			}, -1);
		}));
		assertEquals(NucleusError.PAST_PLANNING_TIME, contractException.getErrorType());

	}

	@Test
	@UnitTestMethod(target = ReportContext.class, name = "getDataManager", args = { Class.class }, tags = { UnitTag.INCOMPLETE })
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

		TestSimulation.executeSimulation(testPlugin);

		// precondition test : if the class reference is ambiguous
		pluginDataBuilder.addTestDataManager("dm3A", () -> new TestDataManager3A());
		pluginDataBuilder.addTestDataManager("dm3B", () -> new TestDataManager3B());

		// show that ambiguous class matching throws an exception
		pluginDataBuilder.addTestReportPlan("report", new TestReportPlan(0, (c) -> {
			ContractException contractException = assertThrows(ContractException.class, () -> c.getDataManager(TestDataManager3.class));
			assertEquals(NucleusError.AMBIGUOUS_DATA_MANAGER_CLASS, contractException.getErrorType());
		}));

		pluginDataBuilder.addTestActorPlan("actor", new TestActorPlan(Double.POSITIVE_INFINITY, (c) -> {
		}));

		// build the action plugin
		testPluginData = pluginDataBuilder.build();
		testPlugin = TestPlugin.getTestPlugin(testPluginData);

		TestSimulation.executeSimulation(testPlugin);

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

		TestSimulation.executeSimulation(testPlugin);
	}

	@Test
	@UnitTestMethod(target = ReportContext.class, name = "getPlan", args = { Object.class }, tags = { UnitTag.INCOMPLETE })
	public void testGetPlan() {
		/*
		 * Have a test report show that a plan added with a key can be retrieved
		 */
		testConsumer((context) -> {
			Object key = new Object();
			assertFalse(context.getPlan(key).isPresent());
			context.addKeyedPlan((c) -> {
			}, 100, key);
			assertTrue(context.getPlan(key).isPresent());
		});

		// precondition test : if the plan key is null
		ContractException contractException = assertThrows(ContractException.class, () -> testConsumer((c) -> c.getPlan(null)));
		assertEquals(NucleusError.NULL_PLAN_KEY, contractException.getErrorType());
	}

	@Test
	@UnitTestMethod(target = ReportContext.class, name = "getPlanKeys", args = {}, tags = { UnitTag.INCOMPLETE })
	public void testGetPlanKeys() {

		// There are no precondition tests
		Set<Object> expectedKeys = new LinkedHashSet<>();
		int keyCount = 20;
		for (int i = 0; i < keyCount; i++) {
			expectedKeys.add(new Object());
		}

		testConsumer((context) -> {
			for (Object key : expectedKeys) {
				context.addKeyedPlan((c) -> {
				}, 100, key);
			}

			Set<Object> actualKeys = context.getPlanKeys().stream().collect(Collectors.toCollection(LinkedHashSet::new));
			assertEquals(expectedKeys, actualKeys);
		});
	}

	@Test
	@UnitTestMethod(target = ReportContext.class, name = "getPlanTime", args = { Object.class }, tags = { UnitTag.INCOMPLETE })
	public void testGetPlanTime() {
		testConsumer((context) -> {
			Object key = new Object();
			assertFalse(context.getPlanTime(key).isPresent());
			double expectedPlanTime = 100;
			context.addKeyedPlan((c) -> {
			}, expectedPlanTime, key);
			assertTrue(context.getPlanTime(key).isPresent());
			Double actualPlanTime = context.getPlanTime(key).get();
			assertEquals(expectedPlanTime, actualPlanTime, 0);
		});

		// precondition test : if the plan key is null
		ContractException contractException = assertThrows(ContractException.class, () -> testConsumer((c) -> c.getPlanTime(null)));
		assertEquals(NucleusError.NULL_PLAN_KEY, contractException.getErrorType());
	}

	@Test
	@UnitTestMethod(target = ReportContext.class, name = "getReportId", args = {}, tags = { UnitTag.INCOMPLETE })
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
		 * expected values established during the initialization of the
		 * TestReports.
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

		TestSimulation.executeSimulation(testPlugin);
	}

	@Test
	@UnitTestMethod(target = ReportContext.class, name = "getTime", args = {}, tags = { UnitTag.INCOMPLETE })
	public void testGetTime() {
		Set<Double> planTimes = new LinkedHashSet<>();

		planTimes.add(4.6);
		planTimes.add(13.8764);
		planTimes.add(554.345);
		planTimes.add(7.95346);
		planTimes.add(400.234234);
		planTimes.add(3000.12422346);

		/*
		 * Have a report build plans to check the time in the simulation against
		 * the planning time
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
	@UnitTestMethod(target = ReportContext.class, name = "releaseOutput", args = { Object.class }, tags = { UnitTag.INCOMPLETE })
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
		 * Add an output consumer that will place the output into the
		 * actualOutput set above and then execute the simulation
		 */
		TestOutputConsumer testOutputConsumer = new TestOutputConsumer();
		TestSimulation.executeSimulation(testPlugin, testOutputConsumer);

		Map<Object, Integer> outputItems = testOutputConsumer.getOutputItems(Object.class);
		for (Object key : outputItems.keySet()) {
			Integer count = outputItems.get(key);
			assertEquals(1, count.intValue());
		}

		// show that the output matches expectations
		assertTrue(outputItems.keySet().containsAll(expectedOutput));
	}

	@Test
	@UnitTestMethod(target = ReportContext.class, name = "removePlan", args = { Object.class }, tags = { UnitTag.INCOMPLETE })
	public void testRemovePlan() {
		TestPluginData.Builder pluginDataBuilder = TestPluginData.builder();

		// test preconditions
		pluginDataBuilder.addTestReportPlan("report", new TestReportPlan(1, (context) -> {
			ContractException contractException = assertThrows(ContractException.class, () -> context.removePlan(null));
			assertEquals(NucleusError.NULL_PLAN_KEY, contractException.getErrorType());
		}));

		Object key = new Object();
		MutableBoolean removedPlanHasExecuted = new MutableBoolean();

		// have the added test report add a plan
		pluginDataBuilder.addTestReportPlan("report", new TestReportPlan(2, (context) -> {
			context.addKeyedPlan((c2) -> {
				removedPlanHasExecuted.setValue(true);
			}, 4, key);
		}));

		// have the test report remove the plan and show the plan no longer
		// exists
		pluginDataBuilder.addTestReportPlan("report", new TestReportPlan(3, (context) -> {
			assertTrue(context.getPlan(key).isPresent());

			context.removePlan(key);

			assertFalse(context.getPlan(key).isPresent());

		}));

		pluginDataBuilder.addTestActorPlan("actor", new TestActorPlan(Double.POSITIVE_INFINITY, (c) -> {
		}));

		// build the plugin
		TestPluginData testPluginData = pluginDataBuilder.build();
		Plugin testPlugin = TestPlugin.getTestPlugin(testPluginData);

		// run the simulation
		TestSimulation.executeSimulation(testPlugin);

		// show that the remove plan was not executed
		assertFalse(removedPlanHasExecuted.getValue());
	}

	private static class TestEvent1 implements Event {

	}

	@Test
	@UnitTestMethod(target = ReportContext.class, name = "subscribe", args = { Class.class, BiConsumer.class }, tags = { UnitTag.INCOMPLETE })
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
		TestSimulation.executeSimulation(testPlugin);

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
	@UnitTestMethod(target = ReportContext.class, name = "subscribeToSimulationClose", args = { Consumer.class }, tags = { UnitTag.INCOMPLETE })
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

		TestSimulation.executeSimulation(testPlugin);

		// show that the subscription to simulation close was successful
		assertTrue(simCloseEventHandled.getValue());

	}

	@Test
	@UnitTestMethod(target = ReportContext.class, name = "unsubscribe", args = { Class.class }, tags = { UnitTag.INCOMPLETE })
	public void testUnsubscribe() {
		TestPluginData.Builder pluginDataBuilder = TestPluginData.builder();

		/*
		 * Create a container to count the number of times a subscription
		 * execution occurred
		 */
		MutableInteger observationCount = new MutableInteger();

		/*
		 * have the resolver subscribe to the test event and have it handle each
		 * type of event handling by incrementing a counter
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
		 * Show that the phaseExecutionCount is three after the the agent is
		 * done
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
		TestSimulation.executeSimulation(testPlugin);

		// precondition test: if the event class reference is null
		ContractException contractException = assertThrows(ContractException.class, () -> testConsumer((c) -> {
			c.unsubscribe(null);
		}));
		assertEquals(NucleusError.NULL_EVENT_CLASS, contractException.getErrorType());

	}

}
