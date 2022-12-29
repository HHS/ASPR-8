package nucleus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.apache.commons.math3.util.Pair;
import org.junit.jupiter.api.Test;

import nucleus.testsupport.testplugin.ScenarioPlanCompletionObserver;
import nucleus.testsupport.testplugin.TestActorPlan;
import nucleus.testsupport.testplugin.TestDataManager;
import nucleus.testsupport.testplugin.TestDataManagerPlan;
import nucleus.testsupport.testplugin.TestPlugin;
import nucleus.testsupport.testplugin.TestPluginData;
import nucleus.testsupport.testplugin.TestScenarioReport;
import tools.annotations.UnitTest;
import tools.annotations.UnitTestMethod;
import util.errors.ContractException;
import util.wrappers.MutableBoolean;
import util.wrappers.MutableInteger;

@UnitTest(target = DataManagerContext.class)

public class AT_DataManagerContext {

	@Test
	@UnitTestMethod(name = "getTime", args = {})
	public void testGetTime() {

		TestPluginData.Builder pluginDataBuilder = TestPluginData.builder();

		Set<Double> planTimes = new LinkedHashSet<>();

		planTimes.add(4.6);
		planTimes.add(13.8764);
		planTimes.add(554.345);
		planTimes.add(7.95346);
		planTimes.add(400.234234);
		planTimes.add(3000.12422346);

		/*
		 * Have the data manager build plans to check the time in the simulation
		 * against the planning time
		 */
		pluginDataBuilder.addTestDataManager("dm", () -> new TestDataManager1());
		pluginDataBuilder.addTestDataManagerPlan("dm", new TestDataManagerPlan(0, (context1) -> {
			for (Double planTime : planTimes) {
				context1.addPlan((context2) -> {
					assertEquals(planTime.doubleValue(), context2.getTime(), 0);
				}, planTime);
			}
		}));

		// build the action plugin
		TestPluginData testPluginData = pluginDataBuilder.build();
		Plugin testPlugin = TestPlugin.getTestPlugin(testPluginData);

		ScenarioPlanCompletionObserver scenarioPlanCompletionObserver = new ScenarioPlanCompletionObserver();

		// execute the engine
		Simulation	.builder()//
					.setOutputConsumer(scenarioPlanCompletionObserver::handleOutput)//
					.addPlugin(testPlugin)//
					.build()//
					.execute();//

		// show that the action was executed
		assertTrue(scenarioPlanCompletionObserver.allPlansExecuted());

	}

	@Test
	@UnitTestMethod(name = "releaseOutput", args = { Object.class })
	public void testReleaseOutput() {

		// begin building the action plugin
		TestPluginData.Builder pluginDataBuilder = TestPluginData.builder();

		// set up the expected output
		Set<Object> expectedOutput = new LinkedHashSet<>();
		expectedOutput.add("the sly fox");
		expectedOutput.add(15);
		expectedOutput.add("the lazy, brown dog");
		expectedOutput.add(45.34513453);

		// have the data manager release the output
		pluginDataBuilder.addTestDataManager("dm", () -> new TestDataManager1());
		pluginDataBuilder.addTestDataManagerPlan("dm", new TestDataManagerPlan(1, (c) -> {
			for (Object outputValue : expectedOutput) {
				c.releaseOutput(outputValue);
			}
		}));

		// build the plugin
		TestPluginData testPluginData = pluginDataBuilder.build();
		Plugin testPlugin = TestPlugin.getTestPlugin(testPluginData);
		Set<Object> actualOutput = new LinkedHashSet<>();

		/*
		 * Add an output consumer that will place the output into the
		 * actualOutput set above and then execute the simulation
		 */
		Simulation	.builder()//
					.addPlugin(testPlugin)//
					.setOutputConsumer((o) -> {
						if (!(o instanceof TestScenarioReport)) {
							actualOutput.add(o);
						}
					})//
					.build()//
					.execute();//

		// show that the output matches expectations
		assertEquals(expectedOutput, actualOutput);

	}

	@Test
	@UnitTestMethod(name = "subscribeToSimulationClose", args = { Consumer.class })
	public void testSubscribeToSimulationClose() {
		TestPluginData.Builder pluginDataBuilder = TestPluginData.builder();

		MutableBoolean simCloseEventHandled = new MutableBoolean();

		// have a data manager schedule a few events and subscribe to simulation
		// close
		pluginDataBuilder.addTestDataManager("dm", () -> new TestDataManager1());
		pluginDataBuilder.addTestDataManagerPlan("dm", new TestDataManagerPlan(0, (c) -> {
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

		TestPluginData testPluginData = pluginDataBuilder.build();
		Plugin testPlugin = TestPlugin.getTestPlugin(testPluginData);
		Simulation	.builder()//
					.addPlugin(testPlugin)//
					.build()//
					.execute();//

		// show that the subscription to simulation close was successful
		assertTrue(simCloseEventHandled.getValue());

	}

	@Test
	@UnitTestMethod(name = "getDataManager", args = { Class.class })
	public void testGetDataManager() {

		// create the test plugin data builder
		TestPluginData.Builder pluginDataBuilder = TestPluginData.builder();

		// create a data manager for the actor to find

		pluginDataBuilder.addTestDataManager("dm1", () -> new TestDataManager1());
		pluginDataBuilder.addTestDataManager("dm3A", () -> new TestDataManager3A());
		pluginDataBuilder.addTestDataManager("dm3B", () -> new TestDataManager3B());
		pluginDataBuilder.addTestDataManager("dm4A", () -> new TestDataManager4A());

		pluginDataBuilder.addTestActorPlan("actor", new TestActorPlan(0, (c) -> {
			TestDataManager1 testDataManager1 = c.getDataManager(TestDataManager1.class);
			assertNotNull(testDataManager1);

			TestDataManager3A testDataManager3A = c.getDataManager(TestDataManager3A.class);
			assertNotNull(testDataManager3A);

			TestDataManager3B testDataManager3B = c.getDataManager(TestDataManager3B.class);
			assertNotNull(testDataManager3B);

			TestDataManager4A testDataManager4A = c.getDataManager(TestDataManager4A.class);
			assertNotNull(testDataManager4A);

		}));

		// build the action plugin
		TestPluginData testPluginData = pluginDataBuilder.build();

		Plugin testPlugin = TestPlugin.getTestPlugin(testPluginData);

		ScenarioPlanCompletionObserver scenarioPlanCompletionObserver = new ScenarioPlanCompletionObserver();

		// execute the engine
		Simulation	.builder()//
					.setOutputConsumer(scenarioPlanCompletionObserver::handleOutput)//
					.addPlugin(testPlugin)//
					.build()//
					.execute();//

		// show that the action was executed
		assertTrue(scenarioPlanCompletionObserver.allPlansExecuted());

		// Precondition test 1

		pluginDataBuilder.addTestDataManager("dm3A", () -> new TestDataManager3A());
		pluginDataBuilder.addTestDataManager("dm3B", () -> new TestDataManager3B());

		pluginDataBuilder.addTestDataManagerPlan("dm3A", new TestDataManagerPlan(4, (c) -> {
			ContractException contractException = assertThrows(ContractException.class, () -> c.getDataManager(TestDataManager3.class));
			assertEquals(NucleusError.AMBIGUOUS_DATA_MANAGER_CLASS, contractException.getErrorType());
		}));

		// build the action plugin
		testPluginData = pluginDataBuilder.build();
		testPlugin = TestPlugin.getTestPlugin(testPluginData);

		scenarioPlanCompletionObserver = new ScenarioPlanCompletionObserver();

		// execute the engine
		Simulation	.builder()//
					.setOutputConsumer(scenarioPlanCompletionObserver::handleOutput)//
					.addPlugin(testPlugin)//
					.build()//
					.execute();//

		// show that the action was executed
		assertTrue(scenarioPlanCompletionObserver.allPlansExecuted());

		// Precondition test 2

		pluginDataBuilder.addTestDataManager("dm3B", () -> new TestDataManager3B());

		pluginDataBuilder.addTestDataManagerPlan("dm3B", new TestDataManagerPlan(4, (c) -> {
			ContractException contractException = assertThrows(ContractException.class, () -> c.getDataManager(null));
			assertEquals(NucleusError.NULL_DATA_MANAGER_CLASS, contractException.getErrorType());
		}));

		// build the action plugin
		testPluginData = pluginDataBuilder.build();
		testPlugin = TestPlugin.getTestPlugin(testPluginData);

		scenarioPlanCompletionObserver = new ScenarioPlanCompletionObserver();

		// execute the engine
		Simulation	.builder()//
					.setOutputConsumer(scenarioPlanCompletionObserver::handleOutput)//
					.addPlugin(testPlugin)//
					.build()//
					.execute();//

		// show that the action was executed
		assertTrue(scenarioPlanCompletionObserver.allPlansExecuted());

	}

	@Test
	@UnitTestMethod(name = "getDataManagerId", args = {})
	public void testGetDataManagerId() {
		TestPluginData.Builder pluginDataBuilder = TestPluginData.builder();

		Set<DataManagerId> observedDataManagerIds = new LinkedHashSet<>();

		pluginDataBuilder.addTestDataManager("dm1", () -> new TestDataManager1());
		pluginDataBuilder.addTestDataManagerPlan("dm1", new TestDataManagerPlan(0, (context) -> {
			observedDataManagerIds.add(context.getDataManagerId());
		}));

		pluginDataBuilder.addTestDataManager("dm2", () -> new TestDataManager2());
		pluginDataBuilder.addTestDataManagerPlan("dm2", new TestDataManagerPlan(1, (context) -> {
			observedDataManagerIds.add(context.getDataManagerId());
		}));

		// build the plugin
		TestPluginData testPluginData = pluginDataBuilder.build();
		Plugin testPlugin = TestPlugin.getTestPlugin(testPluginData);

		ScenarioPlanCompletionObserver scenarioPlanCompletionObserver = new ScenarioPlanCompletionObserver();
		// run the simulation
		Simulation	.builder()//
					.setOutputConsumer(scenarioPlanCompletionObserver::handleOutput)//
					.addPlugin(testPlugin)//
					.build()//
					.execute();//

		// show that the action plans got executed
		assertTrue(scenarioPlanCompletionObserver.allPlansExecuted());

		// show that each data manger has a distinct id
		assertEquals(2, observedDataManagerIds.size());
	}

	@Test
	@UnitTestMethod(name = "addPlan", args = { Consumer.class, double.class })
	public void testAddPlan() {

		TestPluginData.Builder pluginDataBuilder = TestPluginData.builder();

		// test preconditions
		pluginDataBuilder.addTestDataManager("dm", () -> new TestDataManager1());
		pluginDataBuilder.addTestDataManagerPlan("dm", new TestDataManagerPlan(1, (context) -> {
			double scheduledTime = context.getTime() + 1;

			ContractException contractException = assertThrows(ContractException.class, () -> context.addPlan(null, scheduledTime));
			assertEquals(NucleusError.NULL_PLAN, contractException.getErrorType());

			contractException = assertThrows(ContractException.class, () -> context.addPlan((c) -> {
			}, 0));
			assertEquals(NucleusError.PAST_PLANNING_TIME, contractException.getErrorType());

		}));

		/*
		 * Have the actor add a plan and show that that plan executes
		 */

		MutableBoolean planExecuted = new MutableBoolean();

		pluginDataBuilder.addTestDataManagerPlan("dm", new TestDataManagerPlan(4, (context) -> {
			// schedule two passive plans
			context.addPlan((c) -> {
				planExecuted.setValue(true);
			}, 5);
		}));

		// build the plugin
		TestPluginData testPluginData = pluginDataBuilder.build();
		Plugin testPlugin = TestPlugin.getTestPlugin(testPluginData);

		// run the simulation
		Simulation	.builder()//
					.addPlugin(testPlugin)//
					.build()//
					.execute();//

		// we do not need to show that all plans executed

		// show that the last two passive plans did not execute
		assertTrue(planExecuted.getValue());

	}

	@Test
	@UnitTestMethod(name = "addPassivePlan", args = { Consumer.class, double.class })
	public void testAddPassivePlan() {
		TestPluginData.Builder pluginDataBuilder = TestPluginData.builder();
		pluginDataBuilder.addTestDataManager("dm", () -> new TestDataManager1());

		// test preconditions
		pluginDataBuilder.addTestDataManagerPlan("dm", new TestDataManagerPlan(1, (context) -> {
			double scheduledTime = context.getTime() + 1;

			ContractException contractException = assertThrows(ContractException.class, () -> context.addPassivePlan(null, scheduledTime));
			assertEquals(NucleusError.NULL_PLAN, contractException.getErrorType());

			contractException = assertThrows(ContractException.class, () -> context.addPassivePlan((c) -> {
			}, 0));
			assertEquals(NucleusError.PAST_PLANNING_TIME, contractException.getErrorType());

		}));

		/*
		 * Show that passive plans do not execute if there are no remaining
		 * active plans. To do this, we will schedule a few passive plans, one
		 * active plan and then a few more passive plans. We will then show that
		 * the passive plans that come after the last active plan never execute
		 */

		// create some containers for passive keys
		Set<Integer> expectedPassiveValues = new LinkedHashSet<>();
		expectedPassiveValues.add(1);
		expectedPassiveValues.add(2);
		Set<Integer> actualPassiveValues = new LinkedHashSet<>();

		pluginDataBuilder.addTestDataManagerPlan("dm", new TestDataManagerPlan(4, (context) -> {

			// schedule two passive plans
			context.addPassivePlan((c) -> {
				actualPassiveValues.add(1);
			}, 5);
			context.addPassivePlan((c) -> {
				actualPassiveValues.add(2);
			}, 6);

			// schedule the last active plan
			context.addPlan((c) -> {
			}, 7);

			// schedule two more passive plans
			context.addPassivePlan((c) -> {
				actualPassiveValues.add(3);
			}, 8);
			context.addPassivePlan((c) -> {
				actualPassiveValues.add(4);
			}, 9);

		}));

		// build the plugin
		TestPluginData testPluginData = pluginDataBuilder.build();
		Plugin testPlugin = TestPlugin.getTestPlugin(testPluginData);

		// run the simulation
		Simulation	.builder()//
					.addPlugin(testPlugin)//
					.build()//
					.execute();//

		// we do not need to show that all plans executed

		// show that the last two passive plans did not execute
		assertEquals(expectedPassiveValues, actualPassiveValues);
	}

	@Test
	@UnitTestMethod(name = "addPassiveKeyedPlan", args = { Consumer.class, double.class, Object.class })
	public void testAddPassiveKeyedPlan() {

		TestPluginData.Builder pluginDataBuilder = TestPluginData.builder();

		// test preconditions
		pluginDataBuilder.addTestDataManager("dm", () -> new TestDataManager1());
		pluginDataBuilder.addTestDataManagerPlan("dm", new TestDataManagerPlan(1, (context) -> {
			Object key = new Object();

			double scheduledTime = context.getTime() + 1;

			ContractException contractException = assertThrows(ContractException.class, () -> context.addPassiveKeyedPlan(null, scheduledTime, key));
			assertEquals(NucleusError.NULL_PLAN, contractException.getErrorType());

			contractException = assertThrows(ContractException.class, () -> context.addPassiveKeyedPlan((c) -> {
			}, 0, key));
			assertEquals(NucleusError.PAST_PLANNING_TIME, contractException.getErrorType());

			contractException = assertThrows(ContractException.class, () -> context.addPassiveKeyedPlan((c) -> {
			}, scheduledTime, null));
			assertEquals(NucleusError.NULL_PLAN_KEY, contractException.getErrorType());

			context.addPassiveKeyedPlan((c) -> {
			}, scheduledTime, key);

			contractException = assertThrows(ContractException.class, () -> context.addPassiveKeyedPlan((c) -> {
			}, scheduledTime, key));
			assertEquals(NucleusError.DUPLICATE_PLAN_KEY, contractException.getErrorType());

		}));

		/*
		 * have the added test agent add a plan that can be retrieved and thus
		 * was added successfully
		 */
		pluginDataBuilder.addTestDataManagerPlan("dm", new TestDataManagerPlan(2, (context) -> {
			Object key = new Object();

			assertFalse(context.getPlan(key).isPresent());

			context.addPassiveKeyedPlan((c) -> {
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
		Set<Integer> expectedPassiveKeys = new LinkedHashSet<>();
		expectedPassiveKeys.add(1);
		expectedPassiveKeys.add(2);
		Set<Integer> actualPassiveKeys = new LinkedHashSet<>();

		pluginDataBuilder.addTestDataManagerPlan("dm", new TestDataManagerPlan(4, (context) -> {

			// schedule two passive plans
			context.addPassiveKeyedPlan((c) -> {
				actualPassiveKeys.add(1);
			}, 5, 1);
			context.addPassiveKeyedPlan((c) -> {
				actualPassiveKeys.add(2);
			}, 6, 2);

			// schedule the last active plan
			context.addPlan((c) -> {
			}, 7);

			// schedule two more passive plans
			context.addPassiveKeyedPlan((c) -> {
				actualPassiveKeys.add(3);
			}, 8, 3);
			context.addPassiveKeyedPlan((c) -> {
				actualPassiveKeys.add(4);
			}, 9, 4);

		}));

		// build the plugin
		TestPluginData testPluginData = pluginDataBuilder.build();
		Plugin testPlugin = TestPlugin.getTestPlugin(testPluginData);

		// run the simulation
		Simulation	.builder()//
					.addPlugin(testPlugin)//
					.build()//
					.execute();//

		// we do not need to show that all plans executed

		// show that the last two passive plans did not execute
		assertEquals(expectedPassiveKeys, actualPassiveKeys);

	}

	@Test
	@UnitTestMethod(name = "addKeyedPlan", args = { Consumer.class, double.class, Object.class })
	public void testAddKeyedPlan() {
		TestPluginData.Builder pluginDataBuilder = TestPluginData.builder();

		// test preconditions
		pluginDataBuilder.addTestDataManager("dm", () -> new TestDataManager1());
		pluginDataBuilder.addTestDataManagerPlan("dm", new TestDataManagerPlan(1, (context) -> {
			Object key = new Object();

			double scheduledTime = context.getTime() + 1;

			ContractException contractException = assertThrows(ContractException.class, () -> context.addKeyedPlan(null, scheduledTime, key));
			assertEquals(NucleusError.NULL_PLAN, contractException.getErrorType());

			contractException = assertThrows(ContractException.class, () -> context.addKeyedPlan((c) -> {
			}, 0, key));
			assertEquals(NucleusError.PAST_PLANNING_TIME, contractException.getErrorType());

			contractException = assertThrows(ContractException.class, () -> context.addKeyedPlan((c) -> {
			}, scheduledTime, null));
			assertEquals(NucleusError.NULL_PLAN_KEY, contractException.getErrorType());

			context.addKeyedPlan((c) -> {
			}, scheduledTime, key);

			contractException = assertThrows(ContractException.class, () -> context.addKeyedPlan((c) -> {
			}, scheduledTime, key));
			assertEquals(NucleusError.DUPLICATE_PLAN_KEY, contractException.getErrorType());

		}));

		/*
		 * have the added test agent add a plan that can be retrieved and thus
		 * was added successfully
		 */
		pluginDataBuilder.addTestDataManagerPlan("dm", new TestDataManagerPlan(2, (context) -> {
			Object key = new Object();
			assertFalse(context.getPlan(key).isPresent());
			context.addKeyedPlan((c) -> {
			}, 100, key);
			assertTrue(context.getPlan(key).isPresent());
		}));

		// build the plugin
		TestPluginData testPluginData = pluginDataBuilder.build();
		Plugin testPlugin = TestPlugin.getTestPlugin(testPluginData);

		ScenarioPlanCompletionObserver scenarioPlanCompletionObserver = new ScenarioPlanCompletionObserver();

		// run the simulation
		Simulation	.builder()//
					.setOutputConsumer(scenarioPlanCompletionObserver::handleOutput)//
					.addPlugin(testPlugin)//
					.build()//
					.execute();//

		// show that the action plans got executed
		assertTrue(scenarioPlanCompletionObserver.allPlansExecuted());

	}

	@Test
	@UnitTestMethod(name = "getPlan", args = { Object.class })
	public void testGetPlan() {
		TestPluginData.Builder pluginDataBuilder = TestPluginData.builder();

		// test preconditions
		pluginDataBuilder.addTestDataManager("dm", () -> new TestDataManager1());
		pluginDataBuilder.addTestDataManagerPlan("dm", new TestDataManagerPlan(1, (context) -> {
			ContractException contractException = assertThrows(ContractException.class, () -> context.getPlan(null));
			assertEquals(NucleusError.NULL_PLAN_KEY, contractException.getErrorType());
		}));

		/*
		 * have the added test agent add a plan that can be retrieved and thus
		 * was added successfully
		 */
		pluginDataBuilder.addTestDataManagerPlan("dm", new TestDataManagerPlan(2, (context) -> {
			Object key = new Object();
			assertFalse(context.getPlan(key).isPresent());
			context.addKeyedPlan((c) -> {
			}, 100, key);
			assertTrue(context.getPlan(key).isPresent());
		}));

		// build the plugin
		TestPluginData testPluginData = pluginDataBuilder.build();
		Plugin testPlugin = TestPlugin.getTestPlugin(testPluginData);

		ScenarioPlanCompletionObserver scenarioPlanCompletionObserver = new ScenarioPlanCompletionObserver();

		// run the simulation
		Simulation	.builder()//
					.setOutputConsumer(scenarioPlanCompletionObserver::handleOutput)//
					.addPlugin(testPlugin)//
					.build()//
					.execute();//

		// show that the action plans got executed
		assertTrue(scenarioPlanCompletionObserver.allPlansExecuted());

	}

	@Test
	@UnitTestMethod(name = "getPlanTime", args = { Object.class })
	public void testGetPlanTime() {

		TestPluginData.Builder pluginDataBuilder = TestPluginData.builder();

		// test preconditions
		pluginDataBuilder.addTestDataManager("dm", () -> new TestDataManager1());
		pluginDataBuilder.addTestDataManagerPlan("dm", new TestDataManagerPlan(1, (context) -> {
			ContractException contractException = assertThrows(ContractException.class, () -> context.getPlanTime(null));
			assertEquals(NucleusError.NULL_PLAN_KEY, contractException.getErrorType());
		}));

		/*
		 * have the added test agent add a plan and show that the plan time is
		 * as expected
		 */
		pluginDataBuilder.addTestDataManagerPlan("dm", new TestDataManagerPlan(2, (context) -> {
			Object key = new Object();
			assertFalse(context.getPlanTime(key).isPresent());
			double expectedPlanTime = 100;
			context.addKeyedPlan((c) -> {
			}, expectedPlanTime, key);
			assertTrue(context.getPlanTime(key).isPresent());
			Double actualPlanTime = context.getPlanTime(key).get();
			assertEquals(expectedPlanTime, actualPlanTime, 0);
		}));

		// build the plugin
		TestPluginData testPluginData = pluginDataBuilder.build();
		Plugin testPlugin = TestPlugin.getTestPlugin(testPluginData);

		ScenarioPlanCompletionObserver scenarioPlanCompletionObserver = new ScenarioPlanCompletionObserver();
		// run the simulation
		Simulation	.builder()//
					.setOutputConsumer(scenarioPlanCompletionObserver::handleOutput)//
					.addPlugin(testPlugin)//
					.build()//
					.execute();//

		// show that the action plans got executed
		assertTrue(scenarioPlanCompletionObserver.allPlansExecuted());

	}

	@Test
	@UnitTestMethod(name = "removePlan", args = { Object.class })
	public void testRemovePlan() {
		TestPluginData.Builder pluginDataBuilder = TestPluginData.builder();

		// test preconditions
		pluginDataBuilder.addTestDataManager("dm", () -> new TestDataManager1());
		pluginDataBuilder.addTestDataManagerPlan("dm", new TestDataManagerPlan(1, (context) -> {
			ContractException contractException = assertThrows(ContractException.class, () -> context.removePlan(null));
			assertEquals(NucleusError.NULL_PLAN_KEY, contractException.getErrorType());
		}));

		Object key = new Object();
		MutableBoolean removedPlanHasExecuted = new MutableBoolean();

		// have the added test agent add a plan
		pluginDataBuilder.addTestDataManagerPlan("dm", new TestDataManagerPlan(2, (context) -> {
			context.addKeyedPlan((c2) -> {
				removedPlanHasExecuted.setValue(true);
			}, 4, key);
		}));

		// have the test agent remove the plan and show the plan no longer
		// exists
		pluginDataBuilder.addTestDataManagerPlan("dm", new TestDataManagerPlan(3, (context) -> {
			assertTrue(context.getPlan(key).isPresent());

			context.removePlan(key);

			assertFalse(context.getPlan(key).isPresent());

		}));

		// build the plugin
		TestPluginData testPluginData = pluginDataBuilder.build();
		Plugin testPlugin = TestPlugin.getTestPlugin(testPluginData);

		ScenarioPlanCompletionObserver scenarioPlanCompletionObserver = new ScenarioPlanCompletionObserver();

		// run the simulation
		Simulation	.builder()//
					.setOutputConsumer(scenarioPlanCompletionObserver::handleOutput)//
					.addPlugin(testPlugin)//
					.build()//
					.execute();//

		// show that the action plans got executed
		assertTrue(scenarioPlanCompletionObserver.allPlansExecuted());

		// show that the remove plan was not executed
		assertFalse(removedPlanHasExecuted.getValue());
	}

	@Test
	@UnitTestMethod(name = "getPlanKeys", args = {})
	public void testGetPlanKeys() {
		TestPluginData.Builder pluginDataBuilder = TestPluginData.builder();

		// There are no precondition tests
		Set<Object> expectedKeys = new LinkedHashSet<>();
		int keyCount = 20;
		for (int i = 0; i < keyCount; i++) {
			expectedKeys.add(new Object());
		}

		// have the test agent add some plans
		pluginDataBuilder.addTestDataManager("dm", () -> new TestDataManager1());
		pluginDataBuilder.addTestDataManagerPlan("dm", new TestDataManagerPlan(1, (context) -> {
			for (Object key : expectedKeys) {
				context.addKeyedPlan((c) -> {
				}, 100, key);
			}

			Set<Object> actualKeys = context.getPlanKeys().stream().collect(Collectors.toCollection(LinkedHashSet::new));
			assertEquals(expectedKeys, actualKeys);

		}));

		// build the plugin
		TestPluginData testPluginData = pluginDataBuilder.build();
		Plugin testPlugin = TestPlugin.getTestPlugin(testPluginData);

		ScenarioPlanCompletionObserver scenarioPlanCompletionObserver = new ScenarioPlanCompletionObserver();

		// run the simulation
		Simulation	.builder()//
					.setOutputConsumer(scenarioPlanCompletionObserver::handleOutput)//
					.addPlugin(testPlugin)//
					.build().execute();//

		// show that the action plans got executed
		assertTrue(scenarioPlanCompletionObserver.allPlansExecuted());
	}

	@Test
	@UnitTestMethod(name = "releaseEvent", args = { Event.class })
	public void testReleaseEvent() {
		TestPluginData.Builder pluginDataBuilder = TestPluginData.builder();

		MutableBoolean eventResolved = new MutableBoolean();

		// Have the data manager subscribe to test event and then set the
		// eventResolved to true
		pluginDataBuilder.addTestDataManager("dm1", () -> new TestDataManager1());
		pluginDataBuilder.addTestDataManagerPlan("dm1", new TestDataManagerPlan(0, (c) -> {
			c.subscribe(TestEvent.class, (c2, e) -> {
				eventResolved.setValue(true);
			});
		}));

		// have another data manager resolve a test event
		pluginDataBuilder.addTestDataManager("dm2", () -> new TestDataManager2());
		pluginDataBuilder.addTestDataManagerPlan("dm2", new TestDataManagerPlan(1, (context) -> {
			context.releaseEvent(new TestEvent());
		}));

		// precondition tests
		pluginDataBuilder.addTestDataManagerPlan("dm1", new TestDataManagerPlan(1, (context) -> {
			ContractException contractException = assertThrows(ContractException.class, () -> context.releaseEvent(null));
			assertEquals(NucleusError.NULL_EVENT, contractException.getErrorType());
		}));

		// build the plugin
		TestPluginData testPluginData = pluginDataBuilder.build();
		Plugin testPlugin = TestPlugin.getTestPlugin(testPluginData);

		// run the simulation
		Simulation	.builder()//
					.addPlugin(testPlugin)//
					.build()//
					.execute();//

		// show that event actually resolved
		assertTrue(eventResolved.getValue());

	}

	private static class TestEvent implements Event {

	}

	private static class TestDataManager1 extends TestDataManager {
	}

	private static class TestDataManager2 extends TestDataManager {
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
	@UnitTestMethod(name = "actorExists", args = { ActorId.class })
	public void testActorExists() {

		TestPluginData.Builder pluginDataBuilder = TestPluginData.builder();

		double testTime = 1;
		// there are no precondition tests

		pluginDataBuilder.addTestDataManager("dm", () -> new TestDataManager1());

		// have the test agent show it exists and that other agents do not
		pluginDataBuilder.addTestDataManagerPlan("dm", new TestDataManagerPlan(testTime++, (context) -> {
			for (int i = 0; i < 3; i++) {
				ActorId actorId = new ActorId(i);
				assertFalse(context.actorExists(actorId));
			}
			for (int i = 0; i < 3; i++) {
				ActorId actorId = context.addActor((c) -> {
				});
				assertTrue(context.actorExists(actorId));
			}

		}));

		// build the plugin
		TestPluginData testPluginData = pluginDataBuilder.build();
		Plugin testPlugin = TestPlugin.getTestPlugin(testPluginData);

		ScenarioPlanCompletionObserver scenarioPlanCompletionObserver = new ScenarioPlanCompletionObserver();

		// run the simulation
		Simulation	.builder()//
					.setOutputConsumer(scenarioPlanCompletionObserver::handleOutput)//
					.addPlugin(testPlugin)//
					.build()//
					.execute();//

		// show that the action plans got executed
		assertTrue(scenarioPlanCompletionObserver.allPlansExecuted());

	}

	@Test

	@UnitTestMethod(name = "addActor", args = { Consumer.class })
	public void testAddActor() {

		TestPluginData.Builder pluginDataBuilder = TestPluginData.builder();

		MutableBoolean actorWasAdded = new MutableBoolean();

		// there are no precondition tests

		// have the test agent show it exists and that other agents do not
		pluginDataBuilder.addTestDataManager("dm", () -> new TestDataManager1());
		pluginDataBuilder.addTestDataManagerPlan("dm", new TestDataManagerPlan(1, (c) -> {
			c.addActor((c2) -> actorWasAdded.setValue(true));
		}));

		// build the plugin
		TestPluginData testPluginData = pluginDataBuilder.build();
		Plugin testPlugin = TestPlugin.getTestPlugin(testPluginData);

		// run the simulation
		Simulation	.builder()//
					.addPlugin(testPlugin)//
					.build()//
					.execute();//

		// show that the action plans got executed
		assertTrue(actorWasAdded.getValue());

	}

	@Test
	@UnitTestMethod(name = "removeActor", args = { ActorId.class })
	public void testRemoveActor() {

		TestPluginData.Builder pluginDataBuilder = TestPluginData.builder();

		// have the resolver execute the precondition tests
		pluginDataBuilder.addTestDataManager("dm", () -> new TestDataManager1());
		pluginDataBuilder.addTestDataManagerPlan("dm", new TestDataManagerPlan(0, (c) -> {

			ContractException contractException = assertThrows(ContractException.class, () -> c.removeActor(null));
			assertEquals(NucleusError.NULL_ACTOR_ID, contractException.getErrorType());

			contractException = assertThrows(ContractException.class, () -> c.removeActor(new ActorId(1000)));
			assertEquals(NucleusError.UNKNOWN_ACTOR_ID, contractException.getErrorType());

		}));

		List<ActorId> addedActorIds = new ArrayList<>();

		// have the add a few agents
		pluginDataBuilder.addTestDataManagerPlan("dm", new TestDataManagerPlan(1, (c) -> {
			for (int i = 0; i < 10; i++) {
				ActorId actorId = c.addActor((c2) -> {
				});
				assertTrue(c.actorExists(actorId));
				addedActorIds.add(actorId);
			}
		}));

		// have the actor remove the added actors
		pluginDataBuilder.addTestDataManagerPlan("dm", new TestDataManagerPlan(2, (c) -> {
			for (ActorId actorId : addedActorIds) {
				c.removeActor(actorId);
				assertFalse(c.actorExists(actorId));
			}
		}));

		// build the plugin
		TestPluginData testPluginData = pluginDataBuilder.build();
		Plugin testPlugin = TestPlugin.getTestPlugin(testPluginData);

		ScenarioPlanCompletionObserver scenarioPlanCompletionObserver = new ScenarioPlanCompletionObserver();

		// build and execute the engine
		Simulation	.builder()//
					.setOutputConsumer(scenarioPlanCompletionObserver::handleOutput)//
					.addPlugin(testPlugin)//
					.build()//
					.execute();//

		// show that the actions were executed
		assertTrue(scenarioPlanCompletionObserver.allPlansExecuted());
	}

	@Test
	@UnitTestMethod(name = "halt", args = {})
	public void testHalt() {

		TestPluginData.Builder pluginDataBuilder = TestPluginData.builder();

		// there are no precondition tests

		pluginDataBuilder.addTestDataManager("dm", () -> new TestDataManager1());

		// have the test agent execute several tasks, with one of the tasks
		// halting the simulation
		TestDataManagerPlan plan1 = new TestDataManagerPlan(1, (context) -> {
		});
		pluginDataBuilder.addTestDataManagerPlan("dm", plan1);

		TestDataManagerPlan plan2 = new TestDataManagerPlan(2, (context) -> {
		});
		pluginDataBuilder.addTestDataManagerPlan("dm", plan2);

		TestDataManagerPlan plan3 = new TestDataManagerPlan(3, (context) -> {
			context.halt();
		});
		pluginDataBuilder.addTestDataManagerPlan("dm", plan3);

		TestDataManagerPlan plan4 = new TestDataManagerPlan(4, (context) -> {
		});
		pluginDataBuilder.addTestDataManagerPlan("dm", plan4);

		TestDataManagerPlan plan5 = new TestDataManagerPlan(5, (context) -> {
		});
		pluginDataBuilder.addTestDataManagerPlan("dm", plan5);

		// build the plugin
		TestPluginData testPluginData = pluginDataBuilder.build();
		Plugin testPlugin = TestPlugin.getTestPlugin(testPluginData);

		// run the simulation
		Simulation	.builder()//
					.addPlugin(testPlugin)//
					.build()//
					.execute();//

		// show that the plans that were scheduled after the halt did not
		// execute
		assertTrue(plan1.executed());
		assertTrue(plan2.executed());
		assertTrue(plan3.executed());
		assertFalse(plan4.executed());
		assertFalse(plan5.executed());

	}

	// private void combinedSubscriptionTest() {
	//
	// TestPluginData.Builder pluginDataBuilder = TestPluginData.builder();
	//
	// /*
	// * create a container that will record the phases of event resolution
	// * that were executed by the resolver that reflects the order in which
	// * they occured
	// */
	//
	// List<String> observedPhases = new ArrayList<>();
	//
	// /*
	// * Create a container with the phases we expect in the order we expect
	// * them.
	// */
	// List<String> expectedPhases = new ArrayList<>();
	// expectedPhases.add("execution");
	// expectedPhases.add("post-action");
	//
	// // have the resolver test preconditions for all the phases
	// pluginDataBuilder.addTestDataManager("dm", () -> new TestDataManager1());
	// pluginDataBuilder.addTestDataManagerPlan("dm", new TestDataManagerPlan(0,
	// (c) -> {
	//
	// ContractException contractException =
	// assertThrows(ContractException.class, () -> c.subscribe(null, (c2, e) ->
	// {
	// }));
	// assertEquals(NucleusError.NULL_EVENT_CLASS,
	// contractException.getErrorType());
	//
	// contractException = assertThrows(ContractException.class, () ->
	// c.subscribe(TestEvent.class, null));
	// assertEquals(NucleusError.NULL_EVENT_CONSUMER,
	// contractException.getErrorType());
	//
	// contractException = assertThrows(ContractException.class, () ->
	// c.subscribePostOrder(null, (c2, e) -> {
	// }));
	// assertEquals(NucleusError.NULL_EVENT_CLASS,
	// contractException.getErrorType());
	//
	// contractException = assertThrows(ContractException.class, () ->
	// c.subscribe(TestEvent.class, null));
	// assertEquals(NucleusError.NULL_EVENT_CONSUMER,
	// contractException.getErrorType());
	//
	// }));
	//
	// // have the resolver subscribe to the two phases for test events.
	// pluginDataBuilder.addTestDataManagerPlan("dm", new TestDataManagerPlan(0,
	// (c) -> {
	//
	// c.subscribe(TestEvent.class, (c2, e) -> {
	// observedPhases.add("execution");
	// });
	//
	// c.subscribePostOrder(TestEvent.class, (c2, e) -> {
	// observedPhases.add("post-action");
	// });
	// }));
	//
	// // create an agent that will generate a test event
	//
	// pluginDataBuilder.addTestActorPlan("actor", new TestActorPlan(1, (c) -> {
	// c.releaseEvent(new TestEvent());
	// }));
	//
	// // build the plugin
	// TestPluginData testPluginData = pluginDataBuilder.build();
	// Plugin testPlugin = TestPlugin.getTestPlugin(testPluginData);
	//
	// // build and execute the engine
	// Simulation .builder()//
	// .addPlugin(testPlugin)//
	// .build()//
	// .execute();//
	//
	// /*
	// * show that the resolver engaged in the three event resolution phases
	// * in the proper order
	// */
	// assertEquals(expectedPhases, observedPhases);
	//
	// }

	private void combinedSubscriptionTest() {

		TestPluginData.Builder pluginDataBuilder = TestPluginData.builder();

		MutableBoolean observed = new MutableBoolean();

		// have the resolver test preconditions for all the phases
		pluginDataBuilder.addTestDataManager("dm", () -> new TestDataManager1());
		pluginDataBuilder.addTestDataManagerPlan("dm", new TestDataManagerPlan(0, (c) -> {

			ContractException contractException = assertThrows(ContractException.class, () -> c.subscribe(null, (c2, e) -> {
			}));
			assertEquals(NucleusError.NULL_EVENT_CLASS, contractException.getErrorType());

			contractException = assertThrows(ContractException.class, () -> c.subscribe(TestEvent.class, null));
			assertEquals(NucleusError.NULL_EVENT_CONSUMER, contractException.getErrorType());

		}));

		// have the resolver subscribe for test events.
		pluginDataBuilder.addTestDataManagerPlan("dm", new TestDataManagerPlan(0, (c) -> {

			c.subscribe(TestEvent.class, (c2, e) -> {
				observed.setValue(true);
			});

		}));

		// create a data manager that will generate a test event

		pluginDataBuilder.addTestDataManager("generator", () -> new TestDataManager());
		pluginDataBuilder.addTestDataManagerPlan("generator", new TestDataManagerPlan(1, (c) -> {
			c.releaseEvent(new TestEvent());
		}));

		// build the plugin
		TestPluginData testPluginData = pluginDataBuilder.build();
		Plugin testPlugin = TestPlugin.getTestPlugin(testPluginData);

		// build and execute the engine
		Simulation	.builder()//
					.addPlugin(testPlugin)//
					.build()//
					.execute();//

		/*
		 * show that the resolver engaged in the three event resolution phases
		 * in the proper order
		 */
		assertTrue(observed.getValue());

	}

	@Test
	@UnitTestMethod(name = "subscribe", args = { Class.class, BiConsumer.class })
	public void testSubscribe() {
		combinedSubscriptionTest();
	}

	// @Test
	// @UnitTestMethod(name = "subscribeToEventPostPhase", args = { Class.class,
	// BiConsumer.class })
	// public void testSubscribeToEventPostPhase() {
	// combinedSubscriptionTest();
	// }

	@Test
	@UnitTestMethod(name = "unsubscribe", args = { Class.class })
	public void testUnSubscribeToEvent() {

		TestPluginData.Builder pluginDataBuilder = TestPluginData.builder();

		// have the resolver test preconditions
		pluginDataBuilder.addTestDataManager("dm", () -> new TestDataManager1());
		pluginDataBuilder.addTestDataManagerPlan("dm", new TestDataManagerPlan(0, (c) -> {
			ContractException contractException = assertThrows(ContractException.class, () -> c.unsubscribe(null));
			assertEquals(NucleusError.NULL_EVENT_CLASS, contractException.getErrorType());
		}));

		/*
		 * Create a container to count then number of times a subscription
		 * execution occured
		 */
		MutableInteger phaseExecutionCount = new MutableInteger();

		/*
		 * have the resolver subscribe to the test event and have it handle each
		 * type of event handling by incrementing a counter
		 */

		pluginDataBuilder.addTestDataManagerPlan("dm", new TestDataManagerPlan(0, (c) -> {

			c.subscribe(TestEvent.class, (c2, e) -> {
				phaseExecutionCount.increment();
			});

		}));

		// create a data manager that will produce a test event
		pluginDataBuilder.addTestDataManager("generator", () -> new TestDataManager());
		pluginDataBuilder.addTestDataManagerPlan("generator", new TestDataManagerPlan(1, (c) -> {
			c.releaseEvent(new TestEvent());
		}));

		/*
		 * Show that the phaseExecutionCount is three after the the agent is
		 * done
		 */
		pluginDataBuilder.addTestDataManagerPlan("dm", new TestDataManagerPlan(2, (c) -> {
			assertEquals(1, phaseExecutionCount.getValue());
		}));

		// have the resolver unsubscribe
		pluginDataBuilder.addTestDataManagerPlan("dm", new TestDataManagerPlan(3, (c) -> {
			c.unsubscribe(TestEvent.class);
		}));

		// have the data manager generate another test event
		pluginDataBuilder.addTestDataManagerPlan("generator", new TestDataManagerPlan(4, (c) -> {
			c.releaseEvent(new TestEvent());
		}));

		/*
		 * Show that the phaseExecutionCount is still three after the the agent
		 * is done and thus the resolver is no longer subscribed
		 */
		pluginDataBuilder.addTestDataManagerPlan("dm", new TestDataManagerPlan(5, (c) -> {
			assertEquals(1, phaseExecutionCount.getValue());
		}));

		// build the plugin
		TestPluginData testPluginData = pluginDataBuilder.build();
		Plugin testPlugin = TestPlugin.getTestPlugin(testPluginData);

		ScenarioPlanCompletionObserver scenarioPlanCompletionObserver = new ScenarioPlanCompletionObserver();

		// build and execute the engine
		Simulation	.builder()//
					.setOutputConsumer(scenarioPlanCompletionObserver::handleOutput)//
					.addPlugin(testPlugin)//
					.build()//
					.execute();//

		// show that all actions executed
		assertTrue(scenarioPlanCompletionObserver.allPlansExecuted());
	}

	@Test
	@UnitTestMethod(name = "subscribersExist", args = { Class.class })
	public void testSubscribersExist() {

		/*
		 * create a simple event label as a place holder -- all test events will
		 * be matched
		 */
		EventFilter<TestEvent> eventFilter = EventFilter.builder(TestEvent.class)//
														.build();//

		TestPluginData.Builder pluginDataBuilder = TestPluginData.builder();

		// add the first data manager

		/*
		 * Have the test resolver show that there are initially no subscribers
		 * to test events.
		 */
		pluginDataBuilder.addTestDataManager("dm1", () -> new TestDataManager1());
		pluginDataBuilder.addTestDataManagerPlan("dm1", new TestDataManagerPlan(0, (c) -> {
			assertFalse(c.subscribersExist(TestEvent.class));
		}));

		// create an agent and have it subscribe to test events at time 1

		pluginDataBuilder.addTestActorPlan("actor", new TestActorPlan(1, (c) -> {
			// subscribe to the event label
			c.subscribe(eventFilter, (c2, e) -> {
			});
		}));

		// show that the resolver now sees that there are subscribers
		pluginDataBuilder.addTestDataManagerPlan("dm1", new TestDataManagerPlan(2, (c) -> {
			assertTrue(c.subscribersExist(TestEvent.class));
		}));

		// have the agent unsubscribe
		pluginDataBuilder.addTestActorPlan("actor", new TestActorPlan(3, (c) -> {
			c.unsubscribe(eventFilter);
		}));

		// show that the resolver see no subscribers
		pluginDataBuilder.addTestDataManagerPlan("dm1", new TestDataManagerPlan(4, (c) -> {
			assertFalse(c.subscribersExist(TestEvent.class));
		}));

		// add a second data manager

		pluginDataBuilder.addTestDataManager("dm2", () -> new TestDataManager2());

		pluginDataBuilder.addTestDataManagerPlan("dm2", new TestDataManagerPlan(5, (c) -> {
			c.subscribe(TestEvent.class, (c2, e) -> {
			});
		}));

		// show that the test resolver now sees that there are subscribers
		pluginDataBuilder.addTestDataManagerPlan("dm1", new TestDataManagerPlan(6, (c) -> {
			assertTrue(c.subscribersExist(TestEvent.class));
		}));

		// have the second data manager unsubscribe
		pluginDataBuilder.addTestDataManagerPlan("dm2", new TestDataManagerPlan(7, (c) -> {
			c.unsubscribe(TestEvent.class);
		}));

		// show that dm1 now sees that there are no subscribers
		pluginDataBuilder.addTestDataManagerPlan("dm1", new TestDataManagerPlan(8, (c) -> {
			assertFalse(c.subscribersExist(TestEvent.class));
		}));

		// build the plugin
		TestPluginData testPluginData = pluginDataBuilder.build();
		Plugin testPlugin = TestPlugin.getTestPlugin(testPluginData);

		ScenarioPlanCompletionObserver scenarioPlanCompletionObserver = new ScenarioPlanCompletionObserver();

		// build and execute the engine
		Simulation	.builder()//
					.setOutputConsumer(scenarioPlanCompletionObserver::handleOutput)//
					.addPlugin(testPlugin)//
					.build()//
					.execute();//

		// show that all actions were executed
		assertTrue(scenarioPlanCompletionObserver.allPlansExecuted());
	}
	
	private static class ActorObservingDataManager extends TestDataManager{
		private List<Pair<Double,ActorId>> observedPairs;
		private DataManagerContext dataManagerContext;
		public ActorObservingDataManager(List<Pair<Double,ActorId>> observedPairs) {
			this.observedPairs = observedPairs;
		}
		public void init(DataManagerContext dataManagerContext) {
			super.init(dataManagerContext);
			this.dataManagerContext = dataManagerContext;
		}
		public void observe() {
			observedPairs.add(new Pair<>(dataManagerContext.getTime(),dataManagerContext.getActorId()));
		}
	}

	@Test
	@UnitTestMethod(name = "getActorId", args = {})
	public void testGetActorId() {
		List<Pair<Double,ActorId>> expectedPairs = new ArrayList<>();
		List<Pair<Double,ActorId>> observedPairs = new ArrayList<>(); 
		
		
		TestPluginData.Builder pluginDataBuilder = TestPluginData.builder();

		double testTime = 1;
		// there are no precondition tests
		
		pluginDataBuilder.addTestDataManager("dm", () -> new ActorObservingDataManager(observedPairs));

		/*
		 * Have actors get their own actor ids and show that these ids match the
		 * expected values established duing the initialization of the
		 * TestActors.
		 */
		pluginDataBuilder.addTestActorPlan("Alpha", new TestActorPlan(testTime++, (c) -> {
			c.getDataManager(ActorObservingDataManager.class).observe();
			expectedPairs.add(new Pair<>(c.getTime(),c.getActorId()));
		}));

		pluginDataBuilder.addTestActorPlan("Beta", new TestActorPlan(testTime++, (c) -> {
			c.getDataManager(ActorObservingDataManager.class).observe();
			expectedPairs.add(new Pair<>(c.getTime(),c.getActorId()));
		}));

		pluginDataBuilder.addTestActorPlan("Gamma", new TestActorPlan(testTime++, (c) -> {
			c.getDataManager(ActorObservingDataManager.class).observe();
			expectedPairs.add(new Pair<>(c.getTime(),c.getActorId()));
		}));

		// build the plugin
		TestPluginData testPluginData = pluginDataBuilder.build();
		Plugin testPlugin = TestPlugin.getTestPlugin(testPluginData);
		ScenarioPlanCompletionObserver scenarioPlanCompletionObserver = new ScenarioPlanCompletionObserver();

		// run the simulation
		Simulation	.builder()//
					.setOutputConsumer(scenarioPlanCompletionObserver::handleOutput)//
					.addPlugin(testPlugin)//
					.build()//
					.execute();//

		// show that all action plans were executed
		assertTrue(scenarioPlanCompletionObserver.allPlansExecuted());

		// show that the number of actor ids matches the number of actor aliases
		assertEquals(expectedPairs, observedPairs);
	}

}
