package nucleus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.naming.Context;

import org.junit.jupiter.api.Test;

import nucleus.testsupport.testplugin.ScenarioPlanCompletionObserver;
import nucleus.testsupport.testplugin.TestActorPlan;
import nucleus.testsupport.testplugin.TestDataManager;
import nucleus.testsupport.testplugin.TestDataManagerPlan;
import nucleus.testsupport.testplugin.TestPluginData;
import nucleus.testsupport.testplugin.TestPluginDataManager;
import nucleus.testsupport.testplugin.TestPluginInitializer;
import nucleus.testsupport.testplugin.TestScenarioReport;
import util.ContractException;
import util.MutableBoolean;
import util.annotations.UnitTest;
import util.annotations.UnitTestMethod;

@UnitTest(target = DataManagerContext.class)

public class AT_DataManagerContext {

	@Test
	@UnitTestMethod(name = "getTime", args = {})
	public void testGetTime() {

		TestPluginData.Builder pluginBuilder = TestPluginData.builder();

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
		pluginBuilder.addTestDataManager("dm", TestDataManager1.class);
		pluginBuilder.addTestDataManagerPlan("dm", new TestDataManagerPlan(0, (context1) -> {
			for (Double planTime : planTimes) {
				context1.addPlan((context2) -> {
					assertEquals(planTime.doubleValue(), context2.getTime(), 0);
				}, planTime);
			}
		}));

		// build the action plugin
		TestPluginData testPluginData = pluginBuilder.build();

		ScenarioPlanCompletionObserver scenarioPlanCompletionObserver = new ScenarioPlanCompletionObserver();

		// execute the engine
		Simulation	.builder()//
					.setOutputConsumer(scenarioPlanCompletionObserver::handleOutput)//
					.addPluginData(testPluginData)//
					.addPluginInitializer(new TestPluginInitializer())//
					.build()//
					.execute();//

		// show that the action was executed
		assertTrue(scenarioPlanCompletionObserver.allPlansExecuted());

	}

	@Test
	@UnitTestMethod(name = "releaseOutput", args = { Object.class })
	public void testReleaseOutput() {

		// begin building the action plugin
		TestPluginData.Builder pluginBuilder = TestPluginData.builder();

		// set up the expected output
		Set<Object> expectedOutput = new LinkedHashSet<>();
		expectedOutput.add("the sly fox");
		expectedOutput.add(15);
		expectedOutput.add("the lazy, brown dog");
		expectedOutput.add(45.34513453);

		// have the data manager release the output
		pluginBuilder.addTestDataManager("dm", TestDataManager1.class);
		pluginBuilder.addTestDataManagerPlan("dm", new TestDataManagerPlan(1, (c) -> {
			for (Object outputValue : expectedOutput) {
				c.releaseOutput(outputValue);
			}
		}));

		// build the plugin
		TestPluginData testPluginData = pluginBuilder.build();

		Set<Object> actualOutput = new LinkedHashSet<>();

		/*
		 * Add an output consumer that will place the output into the
		 * actualOutput set above and then execute the simulation
		 */
		Simulation	.builder()//
					.addPluginData(testPluginData)//
					.addPluginInitializer(new TestPluginInitializer())//
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
		fail();

	}

	@Test
	@UnitTestMethod(name = "getDataManager", args = { Class.class })
	public void testGetDataManager() {
		// create the test plugin data builder
		TestPluginData.Builder pluginBuilder = TestPluginData.builder();

		// create a data manager for the actor to find

		pluginBuilder.addTestDataManager("dm1", TestDataManager1.class);

		/*
		 * Have the agent search for the data manager that was added to the
		 * simulation. Show that there is no instance of the second type of data
		 * manager present.
		 */
		pluginBuilder.addTestDataManagerPlan("dm1", new TestDataManagerPlan(4, (c) -> {
			Optional<TestDataManager1> optional1 = c.getDataManager(TestDataManager1.class);
			assertTrue(optional1.isPresent());

			Optional<TestDataManager2> optional2 = c.getDataManager(TestDataManager2.class);
			assertFalse(optional2.isPresent());
		}));

		// build the action plugin
		TestPluginData testPluginData = pluginBuilder.build();

		ScenarioPlanCompletionObserver scenarioPlanCompletionObserver = new ScenarioPlanCompletionObserver();

		// execute the engine
		Simulation	.builder()//
					.setOutputConsumer(scenarioPlanCompletionObserver::handleOutput).addPluginData(testPluginData)//
					.addPluginInitializer(new TestPluginInitializer())//
					.build()//
					.execute();//

		// show that the action was executed
		assertTrue(scenarioPlanCompletionObserver.allPlansExecuted());

	}

	@Test
	@UnitTestMethod(name = "getDataManagerId", args = {})
	public void testGetDataManagerId() {
		TestPluginData.Builder pluginBuilder = TestPluginData.builder();
		
		pluginBuilder.addTestDataManager("dm1", TestDataManager1.class);
		pluginBuilder.addTestDataManagerPlan("dm1", new TestDataManagerPlan(0, (context) -> {
			TestPluginDataManager testPluginDataManager = context.getDataManager(TestPluginDataManager.class).get();
			Object alias = testPluginDataManager.getDataManagerAlias(context.getDataManagerId()).get();
			assertEquals("dm1", alias);
		}));
		
		pluginBuilder.addTestDataManager("dm2", TestDataManager2.class);
		pluginBuilder.addTestDataManagerPlan("dm2", new TestDataManagerPlan(1, (context) -> {
			TestPluginDataManager testPluginDataManager = context.getDataManager(TestPluginDataManager.class).get();
			Object alias = testPluginDataManager.getDataManagerAlias(context.getDataManagerId()).get();
			assertEquals("dm2", alias);
		}));


		// build the plugin
		TestPluginData testPluginData = pluginBuilder.build();
		ScenarioPlanCompletionObserver scenarioPlanCompletionObserver = new ScenarioPlanCompletionObserver();
		// run the simulation
		Simulation	.builder()//
					.setOutputConsumer(scenarioPlanCompletionObserver::handleOutput)//
					.addPluginData(testPluginData)//
					.addPluginInitializer(new TestPluginInitializer())//
					.build()//
					.execute();//

		// show that the action plans got executed
		assertTrue(scenarioPlanCompletionObserver.allPlansExecuted());
	}

	@Test
	@UnitTestMethod(name = "addPlan", args = { Consumer.class, double.class })
	public void testAddPlan() {

		TestPluginData.Builder pluginBuilder = TestPluginData.builder();

		// test preconditions
		pluginBuilder.addTestDataManager("dm", TestDataManager1.class);
		pluginBuilder.addTestDataManagerPlan("dm", new TestDataManagerPlan(1, (context) -> {
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

		pluginBuilder.addTestDataManagerPlan("dm", new TestDataManagerPlan(4, (context) -> {
			// schedule two passive plans
			context.addPlan((c) -> {
				planExecuted.setValue(true);
			}, 5);
		}));

		// build the plugin
		TestPluginData testPluginData = pluginBuilder.build();

		// run the simulation
		Simulation	.builder()//
					.addPluginData(testPluginData)//
					.addPluginInitializer(new TestPluginInitializer())//
					.build()//
					.execute();//

		// we do not need to show that all plans executed

		// show that the last two passive plans did not execute
		assertTrue(planExecuted.getValue());

		// ActionPlugin.Builder pluginBuilder = ActionPlugin.builder();
		//
		// // add a resolver
		// ResolverId resolverId = new SimpleResolverId("resolver");
		// pluginBuilder.addResolver(resolverId);
		//
		// // have the resolver test preconditions
		// pluginBuilder.addResolverActionPlan(resolverId, new
		// ResolverActionPlan(5, (c) -> {
		// // if the plan is null
		// ContractException contractException =
		// assertThrows(ContractException.class, () -> {
		// c.addPlan(null, 12.0);
		// });
		// assertEquals(NucleusError.NULL_PLAN,
		// contractException.getErrorType());
		//
		// // if the plan is scheduled for a time in the past
		// contractException = assertThrows(ContractException.class, () -> {
		// c.addPlan((c2) -> {
		// }, 4.0);
		// });
		// assertEquals(NucleusError.PAST_PLANNING_TIME,
		// contractException.getErrorType());
		// }));
		//
		// // create a container for expected planning values
		// Set<Integer> expectedPlanningValues = new LinkedHashSet<>();
		// for (int i = 0; i < 10; i++) {
		// expectedPlanningValues.add(i);
		// }
		//
		// // create a container to collect executed plan information
		// Set<Integer> observedPlanningValues = new LinkedHashSet<>();
		//
		// // have the resolver add some plans and have each plan execution
		// record
		// // data
		// pluginBuilder.addResolverActionPlan(resolverId, new
		// ResolverActionPlan(5, (c) -> {
		// double planTime = c.getTime();
		// for (Integer value : expectedPlanningValues) {
		// planTime += 1;
		// c.addPlan((c2) -> {
		// observedPlanningValues.add(value);
		// }, planTime);
		// }
		// }));
		//
		// // build the plugin
		// ActionPlugin actionPlugin = pluginBuilder.build();
		//
		// // build and execute the engine
		// Simulation.builder().addPlugin(ActionPlugin.PLUGIN_ID,
		// actionPlugin::init).build().execute();
		//
		// // show that all the actions executed
		// assertTrue(actionPlugin.allActionsExecuted());
		//
		// // show that all the plans added by the resolver were executed
		// assertEquals(expectedPlanningValues, observedPlanningValues);

	}

	@Test
	@UnitTestMethod(name = "addPassivePlan", args = { Consumer.class, double.class })
	public void testAddPassivePlan() {
		fail();
	}

	@Test
	@UnitTestMethod(name = "addKeyedPassivePlan", args = { Consumer.class, double.class, Object.class })
	public void testAddKeyedPassivePlan() {
		fail();
	}

	@Test
	@UnitTestMethod(name = "addKeyedPlan", args = { Consumer.class, double.class, Object.class })
	public void testAddKeyedPlan() {
		fail();
		// ActionPlugin.Builder pluginBuilder = ActionPlugin.builder();
		//
		// // add a resolver
		// ResolverId resolverId = new SimpleResolverId("resolver");
		// pluginBuilder.addResolver(resolverId);
		//
		// // have the resolver test preconditions
		// pluginBuilder.addResolverActionPlan(resolverId, new
		// ResolverActionPlan(5, (c) -> {
		// // if the plan is null
		// ContractException contractException =
		// assertThrows(ContractException.class, () -> {
		// c.addPlan(null, 12.0, new Object());
		// });
		// assertEquals(NucleusError.NULL_PLAN,
		// contractException.getErrorType());
		//
		// // if the plan is scheduled for a time in the past
		// contractException = assertThrows(ContractException.class, () -> {
		// c.addPlan((c2) -> {
		// }, 4.0, new Object());
		// });
		// assertEquals(NucleusError.PAST_PLANNING_TIME,
		// contractException.getErrorType());
		//
		// // if the key is already in use by an existing plan
		// Object key = new Object();
		// c.addPlan((c2) -> {
		// }, 17, key);
		//
		// contractException = assertThrows(ContractException.class, () -> {
		// c.addPlan((c2) -> {
		// }, 4.0, key);
		// });
		// assertEquals(NucleusError.DUPLICATE_PLAN_KEY,
		// contractException.getErrorType());
		// }));
		//
		// // create a container for expected planning values
		// Set<Integer> expectedPlanningValues = new LinkedHashSet<>();
		// for (int i = 0; i < 10; i++) {
		// expectedPlanningValues.add(i);
		// }
		//
		// // create a container to collected executed plan information
		// Set<Integer> observedPlanningValues = new LinkedHashSet<>();
		//
		// // Have the resolver add some plans and have each plan record data.
		// Show
		// // that each added plan is retrievable by its key.
		// pluginBuilder.addResolverActionPlan(resolverId, new
		// ResolverActionPlan(5, (c) -> {
		// double planTime = c.getTime();
		// for (Integer value : expectedPlanningValues) {
		// planTime += 1;
		// // create a plan
		// Consumer<ResolverContext> plan = (c2) -> {
		// observedPlanningValues.add(value);
		// };
		// // schedule the plan with the context
		// c.addPlan(plan, planTime, value);
		//
		// // retrieve the plan by its key
		// Consumer<ResolverContext> plan2 = c.getPlan(value);
		//
		// // show that the retrieved plan is the plan that was added
		// assertEquals(plan, plan2);
		// }
		//
		// }));
		//
		// // build the plugin
		// ActionPlugin actionPlugin = pluginBuilder.build();
		//
		// // build and execute the engine
		// Simulation.builder().addPlugin(ActionPlugin.PLUGIN_ID,
		// actionPlugin::init).build().execute();
		//
		// // show that all the actions executed
		// assertTrue(actionPlugin.allActionsExecuted());
		//
		// // show that all the plans added by the resolver were executed
		// assertEquals(expectedPlanningValues, observedPlanningValues);

	}

	@Test
	@UnitTestMethod(name = "getPlan", args = { Object.class })
	public void testGetPlan() {
		TestPluginData.Builder pluginBuilder = TestPluginData.builder();

		// test preconditions
		pluginBuilder.addTestDataManager("dm", TestDataManager1.class);
		pluginBuilder.addTestDataManagerPlan("dm", new TestDataManagerPlan(1, (context) -> {
			ContractException contractException = assertThrows(ContractException.class, () -> context.getPlan(null));
			assertEquals(NucleusError.NULL_PLAN_KEY, contractException.getErrorType());
		}));

		/*
		 * have the added test agent add a plan that can be retrieved and thus
		 * was added successfully
		 */
		pluginBuilder.addTestDataManagerPlan("dm", new TestDataManagerPlan(2, (context) -> {
			Object key = new Object();
			assertFalse(context.getPlan(key).isPresent());
			context.addKeyedPlan((c) -> {
			}, 100, key);
			assertTrue(context.getPlan(key).isPresent());
		}));

		// build the plugin
		TestPluginData testPluginData = pluginBuilder.build();
		ScenarioPlanCompletionObserver scenarioPlanCompletionObserver = new ScenarioPlanCompletionObserver();

		// run the simulation
		Simulation	.builder()//
					.setOutputConsumer(scenarioPlanCompletionObserver::handleOutput)//
					.addPluginData(testPluginData)//
					.addPluginInitializer(new TestPluginInitializer())//
					.build()//
					.execute();//

		// show that the action plans got executed
		assertTrue(scenarioPlanCompletionObserver.allPlansExecuted());

	}

	@Test
	@UnitTestMethod(name = "getPlanTime", args = { Object.class })
	public void testGetPlanTime() {

		TestPluginData.Builder pluginBuilder = TestPluginData.builder();

		// test preconditions
		pluginBuilder.addTestDataManager("dm", TestDataManager1.class);
		pluginBuilder.addTestDataManagerPlan("dm", new TestDataManagerPlan(1, (context) -> {
			ContractException contractException = assertThrows(ContractException.class, () -> context.getPlanTime(null));
			assertEquals(NucleusError.NULL_PLAN_KEY, contractException.getErrorType());
		}));

		/*
		 * have the added test agent add a plan and show that the plan time is
		 * as expected
		 */
		pluginBuilder.addTestDataManagerPlan("dm", new TestDataManagerPlan(2, (context) -> {
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
		TestPluginData testPluginData = pluginBuilder.build();
		ScenarioPlanCompletionObserver scenarioPlanCompletionObserver = new ScenarioPlanCompletionObserver();
		// run the simulation
		Simulation	.builder()//
					.setOutputConsumer(scenarioPlanCompletionObserver::handleOutput)//
					.addPluginData(testPluginData)//
					.addPluginInitializer(new TestPluginInitializer())//
					.build()//
					.execute();//

		// show that the action plans got executed
		assertTrue(scenarioPlanCompletionObserver.allPlansExecuted());

	}

	@Test
	@UnitTestMethod(name = "removePlan", args = { Object.class })
	public void testRemovePlan() {
		fail();
		// ActionPlugin.Builder pluginBuilder = ActionPlugin.builder();
		//
		// // create a resolver
		// ResolverId resolverId = new SimpleResolverId("resolver");
		// pluginBuilder.addResolver(resolverId);
		//
		// // have the resolver test preconditions
		// pluginBuilder.addResolverActionPlan(resolverId, new
		// ResolverActionPlan(0, (c) -> {
		// ContractException contractException =
		// assertThrows(ContractException.class, () -> c.removePlan(null));
		// assertEquals(NucleusError.NULL_PLAN_KEY,
		// contractException.getErrorType());
		// }));
		//
		// /*
		// * Create a counter that will be incremented each time that a plan is
		// * executed despite having been removed.
		// */
		// MutableInteger planExecutionCounter = new MutableInteger();
		//
		// /*
		// * Have the resolver add and remove some plans, showing that removed
		// * plans are removed and have each plan increment a counter. We expect
		// * the counter to be zero at the end of the simulation.
		// */
		// pluginBuilder.addResolverActionPlan(resolverId, new
		// ResolverActionPlan(1, (c) -> {
		// for (int i = 0; i < 10; i++) {
		// double planTime = i + 5;
		// Object key = i;
		// c.addPlan((c2) -> {
		// planExecutionCounter.increment();
		// }, planTime, key);
		// assertNotNull(c.getPlan(key));
		// c.removePlan(key);
		// assertNull(c.getPlan(key));
		// }
		// }));
		// // build the plugin
		// ActionPlugin actionPlugin = pluginBuilder.build();
		//
		// // build and execute the engine
		// Simulation.builder().addPlugin(ActionPlugin.PLUGIN_ID,
		// actionPlugin::init).build().execute();
		//
		// // show that all actions were executed
		// assertTrue(actionPlugin.allActionsExecuted());
		//
		// // show that non of the removed plans executed
		// assertEquals(0, planExecutionCounter.getValue());
	}

	@Test
	@UnitTestMethod(name = "getPlanKeys", args = {})
	public void testGetPlanKeys() {
		TestPluginData.Builder pluginBuilder = TestPluginData.builder();

		// There are no precondition tests
		Set<Object> expectedKeys = new LinkedHashSet<>();
		int keyCount = 20;
		for (int i = 0; i < keyCount; i++) {
			expectedKeys.add(new Object());
		}

		// have the test agent add some plans
		pluginBuilder.addTestDataManager("dm", TestDataManager1.class);
		pluginBuilder.addTestDataManagerPlan("dm", new TestDataManagerPlan(1, (context) -> {
			for (Object key : expectedKeys) {
				context.addKeyedPlan((c) -> {
				}, 100, key);
			}

			Set<Object> actualKeys = context.getPlanKeys().stream().collect(Collectors.toCollection(LinkedHashSet::new));
			assertEquals(expectedKeys, actualKeys);

		}));

		// build the plugin
		TestPluginData testPluginData = pluginBuilder.build();

		ScenarioPlanCompletionObserver scenarioPlanCompletionObserver = new ScenarioPlanCompletionObserver();

		// run the simulation
		Simulation	.builder()//
					.setOutputConsumer(scenarioPlanCompletionObserver::handleOutput).addPluginData(testPluginData)//
					.addPluginInitializer(new TestPluginInitializer()).build()//
					.execute();//

		// show that the action plans got executed
		assertTrue(scenarioPlanCompletionObserver.allPlansExecuted());
	}

	@Test
	@UnitTestMethod(name = "resolveEvent", args = { Event.class })
	public void testResolveEvent() {
		fail();
		// ActionPlugin.Builder pluginBuilder = ActionPlugin.builder();
		//
		// // add two resolvers
		// ResolverId resolverId_1 = new SimpleResolverId("resolver 1");
		// pluginBuilder.addResolver(resolverId_1);
		//
		// ResolverId resolverId_2 = new SimpleResolverId("resolver 2");
		// pluginBuilder.addResolver(resolverId_2);
		//
		// // have the first resolver test preconditions
		// pluginBuilder.addResolverActionPlan(resolverId_1, new
		// ResolverActionPlan(0, (c) -> {
		// ContractException contractException =
		// assertThrows(ContractException.class, () ->
		// c.queueEventForResolution(null));
		// assertEquals(NucleusError.NULL_EVENT,
		// contractException.getErrorType());
		// }));
		//
		// /*
		// * Create a container that shows the test event was received and thus
		// * the queue works
		// */
		// MutableBoolean testEventReceived = new MutableBoolean();
		//
		// /*
		// * Have the second resolver subscribe to test events and record when a
		// * test event is received
		// */
		// pluginBuilder.addResolverActionPlan(resolverId_2, new
		// ResolverActionPlan(0, (c) -> {
		// c.subscribeToEventExecutionPhase(TestEvent.class, (c2, e) -> {
		// testEventReceived.setValue(true);
		// });
		// }));
		//
		// // have the first resolver queue a test event for resolution
		// pluginBuilder.addResolverActionPlan(resolverId_2, new
		// ResolverActionPlan(5, (c) -> {
		// c.queueEventForResolution(new TestEvent());
		// }));
		//
		// // build the plugin
		// ActionPlugin actionPlugin = pluginBuilder.build();
		//
		// // build and execute the engine
		// Simulation.builder().addPlugin(ActionPlugin.PLUGIN_ID,
		// actionPlugin::init).build().execute();
		//
		// // show that all actions were executed
		// assertTrue(actionPlugin.allActionsExecuted());
		//
		// // show that the test event was received and thus the queue works
		// assertTrue(testEventReceived.getValue());

	}

	private static class TestEvent implements Event {

	}

	public static class TestDataManager1 extends TestDataManager {
	}

	public static class TestDataManager2 extends TestDataManager {
	}

	@Test
	@UnitTestMethod(name = "actorExists", args = { ActorId.class })
	public void testActorExists() {

		TestPluginData.Builder pluginBuilder = TestPluginData.builder();

		double testTime = 1;
		// there are no precondition tests

		pluginBuilder.addTestDataManager("dm", TestDataManager1.class);

		// have the test agent show it exists and that other agents do not
		pluginBuilder.addTestDataManagerPlan("dm", new TestDataManagerPlan(testTime++, (context) -> {
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
		TestPluginData testPluginData = pluginBuilder.build();
		ScenarioPlanCompletionObserver scenarioPlanCompletionObserver = new ScenarioPlanCompletionObserver();

		// run the simulation
		Simulation	.builder()//
					.setOutputConsumer(scenarioPlanCompletionObserver::handleOutput).addPluginData(testPluginData)//
					.addPluginInitializer(new TestPluginInitializer())//
					.build()//
					.execute();//

		// show that the action plans got executed
		assertTrue(scenarioPlanCompletionObserver.allPlansExecuted());

	}

	@Test

	@UnitTestMethod(name = "addActor", args = { Consumer.class })
	public void testAddActor() {

		TestPluginData.Builder pluginBuilder = TestPluginData.builder();

		MutableBoolean actorWasAdded = new MutableBoolean();

		// there are no precondition tests

		// have the test agent show it exists and that other agents do not
		pluginBuilder.addTestDataManager("dm", TestDataManager1.class);
		pluginBuilder.addTestDataManagerPlan("dm", new TestDataManagerPlan(1, (c) -> {
			c.addActor((c2) -> actorWasAdded.setValue(true));
		}));

		// build the plugin
		TestPluginData testPluginData = pluginBuilder.build();

		// run the simulation
		Simulation	.builder()//
					.addPluginData(testPluginData)//
					.addPluginInitializer(new TestPluginInitializer())//
					.build()//
					.execute();//

		// show that the action plans got executed
		assertTrue(actorWasAdded.getValue());

		// ActionPlugin.Builder pluginBuilder = ActionPlugin.builder();
		//
		// // add a resolver
		// ResolverId resolverId = new SimpleResolverId("resolver");
		// pluginBuilder.addResolver(resolverId);
		//
		// // have the resolver create a few agents
		// pluginBuilder.addResolverActionPlan(resolverId, new
		// ResolverActionPlan(0, (c) -> {
		// for (int i = 0; i < 10; i++) {
		// assertFalse(c.agentExists(new AgentId(i)));
		//
		// c.addAgent((c2) -> {
		// }, new AgentId(i));
		//
		// assertTrue(c.agentExists(new AgentId(i)));
		// }
		//
		// }));
		//
		// // precondition tests
		// pluginBuilder.addResolverActionPlan(resolverId, new
		// ResolverActionPlan(0, (c) -> {
		//
		// AgentId availableAgentId = c.getAvailableAgentId();
		//
		// ContractException contractException =
		// assertThrows(ContractException.class, () -> c.addAgent(null,
		// availableAgentId));
		//
		// assertEquals(NucleusError.NULL_AGENT_CONTEXT_CONSUMER,
		// contractException.getErrorType());
		//
		// contractException = assertThrows(ContractException.class, () ->
		// c.addAgent((c2) -> {
		// }, new AgentId(-1)));
		// assertEquals(NucleusError.NEGATIVE_AGENT_ID,
		// contractException.getErrorType());
		//
		// contractException = assertThrows(ContractException.class, () ->
		// c.addAgent((c2) -> {
		// }, new AgentId(0)));
		// assertEquals(NucleusError.AGENT_ID_IN_USE,
		// contractException.getErrorType());
		//
		// }));
		//
		// // build the plugin
		// ActionPlugin actionPlugin = pluginBuilder.build();
		//
		// // build and execute the engine
		// Simulation.builder().addPlugin(ActionPlugin.PLUGIN_ID,
		// actionPlugin::init).build().execute();
		//
		// // show that all the actions executed
		// assertTrue(actionPlugin.allActionsExecuted());

	}

	@Test
	@UnitTestMethod(name = "removeActor", args = { ActorId.class })
	public void testRemoveActor() {

		TestPluginData.Builder pluginBuilder = TestPluginData.builder();

		// have the resolver execute the precondition tests
		pluginBuilder.addTestDataManager("dm", TestDataManager1.class);
		pluginBuilder.addTestDataManagerPlan("dm", new TestDataManagerPlan(0, (c) -> {

			ContractException contractException = assertThrows(ContractException.class, () -> c.removeActor(null));
			assertEquals(NucleusError.NULL_ACTOR_ID, contractException.getErrorType());

			contractException = assertThrows(ContractException.class, () -> c.removeActor(new ActorId(1000)));
			assertEquals(NucleusError.UNKNOWN_ACTOR_ID, contractException.getErrorType());

		}));

		List<ActorId> addedActorIds = new ArrayList<>();

		// have the add a few agents
		pluginBuilder.addTestDataManagerPlan("dm", new TestDataManagerPlan(1, (c) -> {
			for (int i = 0; i < 10; i++) {
				ActorId actorId = c.addActor((c2) -> {
				});
				assertTrue(c.actorExists(actorId));
				addedActorIds.add(actorId);
			}
		}));

		// have the actor remove the added actors
		pluginBuilder.addTestDataManagerPlan("dm", new TestDataManagerPlan(2, (c) -> {
			for (ActorId actorId : addedActorIds) {
				c.removeActor(actorId);
				assertFalse(c.actorExists(actorId));
			}
		}));

		// build the plugin
		TestPluginData testPluginData = pluginBuilder.build();

		ScenarioPlanCompletionObserver scenarioPlanCompletionObserver = new ScenarioPlanCompletionObserver();

		// build and execute the engine
		Simulation	.builder()//
					.setOutputConsumer(scenarioPlanCompletionObserver::handleOutput)//
					.addPluginData(testPluginData)//
					.addPluginInitializer(new TestPluginInitializer())//
					.build()//
					.execute();//

		// show that the actions were executed
		assertTrue(scenarioPlanCompletionObserver.allPlansExecuted());
	}

	@Test
	@UnitTestMethod(name = "halt", args = {})
	public void testHalt() {

		TestPluginData.Builder pluginBuilder = TestPluginData.builder();

		// there are no precondition tests

		pluginBuilder.addTestDataManager("dm", TestDataManager1.class);

		// have the test agent execute several tasks, with one of the tasks
		// halting the simulation
		TestDataManagerPlan plan1 = new TestDataManagerPlan(1, (context) -> {
		});
		pluginBuilder.addTestDataManagerPlan("dm", plan1);

		TestDataManagerPlan plan2 = new TestDataManagerPlan(2, (context) -> {
		});
		pluginBuilder.addTestDataManagerPlan("dm", plan2);

		TestDataManagerPlan plan3 = new TestDataManagerPlan(3, (context) -> {
			context.halt();
		});
		pluginBuilder.addTestDataManagerPlan("dm", plan3);

		TestDataManagerPlan plan4 = new TestDataManagerPlan(4, (context) -> {
		});
		pluginBuilder.addTestDataManagerPlan("dm", plan4);

		TestDataManagerPlan plan5 = new TestDataManagerPlan(5, (context) -> {
		});
		pluginBuilder.addTestDataManagerPlan("dm", plan5);

		// build the plugin
		TestPluginData testPluginData = pluginBuilder.build();

		// run the simulation
		Simulation	.builder()//
					.addPluginData(testPluginData)//
					.addPluginInitializer(new TestPluginInitializer())//
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

	private void combinedSubscriptionTest() {
		fail();
		// ActionPlugin.Builder pluginBuilder = ActionPlugin.builder();
		//
		// // add a resolver
		// ResolverId resolverId = new SimpleResolverId("resolver");
		// pluginBuilder.addResolver(resolverId);
		//
		// /*
		// * create a container that will record the phases of event resolution
		// * that were executed by the resolver that reflects the order in which
		// * they occured
		// */
		//
		// List<String> observedPhases = new ArrayList<>();
		//
		// // create a container with the phases we expect in the order we
		// expect
		// // them
		// List<String> expectedPhases = new ArrayList<>();
		// expectedPhases.add("validation");
		// expectedPhases.add("execution");
		// expectedPhases.add("post-action");
		//
		// // have the resolver test preconditions for all the phases
		// pluginBuilder.addResolverActionPlan(resolverId, new
		// ResolverActionPlan(0, (c) -> {
		// ContractException contractException =
		// assertThrows(ContractException.class, () ->
		// c.subscribeToEventValidationPhase(null, (c2, e) -> {
		// }));
		// assertEquals(NucleusError.NULL_EVENT_CLASS,
		// contractException.getErrorType());
		//
		// contractException = assertThrows(ContractException.class, () ->
		// c.subscribeToEventValidationPhase(TestEvent.class, null));
		// assertEquals(NucleusError.NULL_EVENT_CONSUMER,
		// contractException.getErrorType());
		//
		// contractException = assertThrows(ContractException.class, () ->
		// c.subscribeToEventExecutionPhase(null, (c2, e) -> {
		// }));
		// assertEquals(NucleusError.NULL_EVENT_CLASS,
		// contractException.getErrorType());
		//
		// contractException = assertThrows(ContractException.class, () ->
		// c.subscribeToEventExecutionPhase(TestEvent.class, null));
		// assertEquals(NucleusError.NULL_EVENT_CONSUMER,
		// contractException.getErrorType());
		//
		// contractException = assertThrows(ContractException.class, () ->
		// c.subscribeToEventPostPhase(null, (c2, e) -> {
		// }));
		// assertEquals(NucleusError.NULL_EVENT_CLASS,
		// contractException.getErrorType());
		//
		// contractException = assertThrows(ContractException.class, () ->
		// c.subscribeToEventPostPhase(TestEvent.class, null));
		// assertEquals(NucleusError.NULL_EVENT_CONSUMER,
		// contractException.getErrorType());
		//
		// }));
		//
		// // have the resolver subscribe to the three phases for test events.
		// pluginBuilder.addResolverActionPlan(resolverId, new
		// ResolverActionPlan(0, (c) -> {
		// c.subscribeToEventValidationPhase(TestEvent.class, (c2, e) -> {
		// observedPhases.add("validation");
		// });
		//
		// c.subscribeToEventExecutionPhase(TestEvent.class, (c2, e) -> {
		// observedPhases.add("execution");
		// });
		//
		// c.subscribeToEventPostPhase(TestEvent.class, (c2, e) -> {
		// observedPhases.add("post-action");
		// });
		// }));
		//
		// // create an agent that will generate a test event
		// pluginBuilder.addAgent("agent");
		// pluginBuilder.addAgentActionPlan("agent", new AgentActionPlan(1, (c)
		// -> {
		// c.resolveEvent(new TestEvent());
		// }));
		//
		// // build the plugin
		// ActionPlugin actionPlugin = pluginBuilder.build();
		//
		// // build and execute the engine
		// Simulation.builder().addPlugin(ActionPlugin.PLUGIN_ID,
		// actionPlugin::init).build().execute();
		//
		// // show that the resolver engaged in the three event resolution
		// phases
		// // in the proper order
		// assertEquals(expectedPhases, observedPhases);

	}

	@Test
	@UnitTestMethod(name = "subscribe", args = { Class.class, BiConsumer.class })
	public void testSubscribe() {
		fail();
		// combinedSubscriptionTest();
	}

	@Test
	@UnitTestMethod(name = "subscribeToEventPostPhase", args = { Class.class, BiConsumer.class })
	public void testSubscribeToEventPostPhase() {
		fail();
		// combinedSubscriptionTest();
	}

	@Test
	@UnitTestMethod(name = "unSubscribeToEvent", args = { Class.class })
	public void testUnSubscribeToEvent() {
		fail();
		// ActionPlugin.Builder pluginBuilder = ActionPlugin.builder();
		//
		// // add a resolver
		// ResolverId resolverId = new SimpleResolverId("resolver");
		// pluginBuilder.addResolver(resolverId);
		//
		// // have the resolver test preconditions
		// pluginBuilder.addResolverActionPlan(resolverId, new
		// ResolverActionPlan(0, (c) -> {
		// ContractException contractException =
		// assertThrows(ContractException.class, () ->
		// c.unSubscribeToEvent(null));
		// assertEquals(NucleusError.NULL_EVENT_CLASS,
		// contractException.getErrorType());
		// }));
		//
		// /*
		// * Create a container to count then number of times a subscription
		// * execution occured
		// */
		// MutableInteger phaseExecutionCount = new MutableInteger();
		//
		// // have the resolver subscribe to the test event and have it handle
		// each
		// // type of event handling by incrementing a counter
		// pluginBuilder.addResolverActionPlan(resolverId, new
		// ResolverActionPlan(0, (c) -> {
		//
		// c.subscribeToEventValidationPhase(TestEvent.class, (c2, e) -> {
		// phaseExecutionCount.increment();
		// });
		//
		// c.subscribeToEventExecutionPhase(TestEvent.class, (c2, e) -> {
		// phaseExecutionCount.increment();
		// });
		//
		// c.subscribeToEventPostPhase(TestEvent.class, (c2, e) -> {
		// phaseExecutionCount.increment();
		// });
		//
		// }));
		//
		// // create an agent that will produce a test event
		// pluginBuilder.addAgent("agent");
		// pluginBuilder.addAgentActionPlan("agent", new AgentActionPlan(1, (c)
		// -> {
		// c.resolveEvent(new TestEvent());
		// }));
		//
		// /*
		// * Show that the phaseExecutionCount is three after the the agent is
		// * done
		// */
		// pluginBuilder.addResolverActionPlan(resolverId, new
		// ResolverActionPlan(2, (c) -> {
		// assertEquals(3, phaseExecutionCount.getValue());
		// }));
		//
		// // have the resolver unsubscribe
		// pluginBuilder.addResolverActionPlan(resolverId, new
		// ResolverActionPlan(3, (c) -> {
		// c.unSubscribeToEvent(TestEvent.class);
		// }));
		//
		// // have the agent generate another test event
		// pluginBuilder.addAgentActionPlan("agent", new AgentActionPlan(4, (c)
		// -> {
		// c.resolveEvent(new TestEvent());
		// }));
		//
		// /*
		// * Show that the phaseExecutionCount is still three after the the
		// agent
		// * is done and thus the resolver is no longer subscribed
		// */
		// pluginBuilder.addResolverActionPlan(resolverId, new
		// ResolverActionPlan(5, (c) -> {
		// assertEquals(3, phaseExecutionCount.getValue());
		// }));
		//
		// // build the plugin
		// ActionPlugin actionPlugin = pluginBuilder.build();
		//
		// // build and execute the engine
		// Simulation.builder().addPlugin(ActionPlugin.PLUGIN_ID,
		// actionPlugin::init).build().execute();
		//
		// // show that all actions executed
		// assertTrue(actionPlugin.allActionsExecuted());
	}

	/*
	 * Event labeler class designed to possibly not comply with preconditions
	 * required for the adding of event labelers
	 */
	private static class TestEventLabeler implements EventLabeler<TestEvent> {
		private final Class<TestEvent> eventClass;
		private final EventLabelerId eventLabelerId;

		public TestEventLabeler(Class<TestEvent> eventClass, EventLabelerId eventLabelerId) {
			this.eventClass = eventClass;
			this.eventLabelerId = eventLabelerId;
		}

		@Override
		public EventLabel<TestEvent> getEventLabel(SimulationContext context, TestEvent event) {
			return new MultiKeyEventLabel<>(TestEvent.class, eventLabelerId, TestEvent.class);
		}

		@Override
		public Class<TestEvent> getEventClass() {
			return eventClass;
		}

		@Override
		public EventLabelerId getId() {
			return eventLabelerId;
		}

	}

	@Test
	@UnitTestMethod(name = "addEventLabeler", args = { EventLabeler.class })
	public void testAddEventLabeler() {

		TestPluginData.Builder pluginBuilder = TestPluginData.builder();

		// have the actor test the preconditions
		pluginBuilder.addTestDataManager("dm", TestDataManager1.class);
		pluginBuilder.addTestDataManagerPlan("dm", new TestDataManagerPlan(0, (c) -> {
			EventLabelerId eventLabelerId = new EventLabelerId() {
			};

			// if the event labeler is null
			ContractException contractException = assertThrows(ContractException.class, () -> c.addEventLabeler(null));
			assertEquals(NucleusError.NULL_EVENT_LABELER, contractException.getErrorType());

			// if the event class is null
			contractException = assertThrows(ContractException.class, () -> c.addEventLabeler(new TestEventLabeler(null, eventLabelerId)));
			assertEquals(NucleusError.NULL_EVENT_CLASS_IN_EVENT_LABELER, contractException.getErrorType());

			// if the event labeler contains a null labeler id
			contractException = assertThrows(ContractException.class, () -> c.addEventLabeler(new TestEventLabeler(TestEvent.class, null)));
			assertEquals(NucleusError.NULL_LABELER_ID_IN_EVENT_LABELER, contractException.getErrorType());

			/*
			 * if the event labeler contains a labeler id that is the id of a
			 * previously added event labeler
			 */
			c.addEventLabeler(new TestEventLabeler(TestEvent.class, eventLabelerId));
			contractException = assertThrows(ContractException.class, () -> c.addEventLabeler(new TestEventLabeler(TestEvent.class, eventLabelerId)));
			assertEquals(NucleusError.DUPLICATE_LABELER_ID_IN_EVENT_LABELER, contractException.getErrorType());

		}));

		/*
		 * create a new event labeler that will be added by the resolver and the
		 * utilized by an agent.
		 */
		EventLabelerId id = new EventLabelerId() {
		};

		EventLabeler<TestEvent> eventLabeler = new TestEventLabeler(TestEvent.class, id);

		// have the actor add the event labeler
		pluginBuilder.addTestDataManagerPlan("dm", new TestDataManagerPlan(1, (c) -> {
			c.addEventLabeler(eventLabeler);
		}));

		/*
		 * Create a container for the agent to record that it received the Test
		 * Event and we can conclude that the event labeler had been properly
		 * added to the simulation.
		 */
		MutableBoolean eventObserved = new MutableBoolean();

		// have the agent observe the test event

		pluginBuilder.addTestActorPlan("observer", new TestActorPlan(2, (c) -> {
			c.subscribe(new MultiKeyEventLabel<>(TestEvent.class, id, TestEvent.class), (c2, e) -> {
				eventObserved.setValue(true);
			});
		}));

		// have the actor create a test event for the agent to observe
		pluginBuilder.addTestActorPlan("observer", new TestActorPlan(3, (c) -> {
			c.resolveEvent(new TestEvent());
		}));

		// build the plugin
		TestPluginData testPluginData = pluginBuilder.build();
		ScenarioPlanCompletionObserver scenarioPlanCompletionObserver = new ScenarioPlanCompletionObserver();

		// build and execute the engine
		Simulation	.builder()//
					.setOutputConsumer(scenarioPlanCompletionObserver::handleOutput).addPluginData(testPluginData)//
					.addPluginInitializer(new TestPluginInitializer()).build()//
					.execute();//

		// show that all plans were executed
		assertTrue(scenarioPlanCompletionObserver.allPlansExecuted());

		/*
		 * Show that the event labeler must have been added to the simulation
		 * since the agent observed the test event
		 */
		assertTrue(eventObserved.getValue());
	}

	@Test
	@UnitTestMethod(name = "subscribersExistForEvent", args = { Class.class })
	public void testSubscribersExistForEvent() {
		fail();
		// // create an event labeler id
		// EventLabelerId eventLabelerId = new EventLabelerId() {
		// };
		//
		// // create a simple event label as a place holder -- all test events
		// will
		// // be matched
		// MultiKeyEventLabel<TestEvent> eventLabel = new
		// MultiKeyEventLabel<>(TestEvent.class, eventLabelerId,
		// TestEvent.class);
		//
		// // create an event labeler that always returns the label above
		// EventLabeler<TestEvent> eventLabeler = new
		// SimpleEventLabeler<>(eventLabelerId, TestEvent.class, (c2, e) -> {
		// return eventLabel;
		// });
		//
		// ActionPlugin.Builder pluginBuilder = ActionPlugin.builder();
		//
		// /////////////////////////////////////////////////////////
		// // Case 1 : an agent subscriber
		// /////////////////////////////////////////////////////////
		//
		// // add the test resolver
		// ResolverId testResolverId = new SimpleResolverId("test resolver");
		// pluginBuilder.addResolver(testResolverId);
		//
		// /*
		// * Have the test resolver show that there are initially no subscribers
		// * to test events.
		// */
		// pluginBuilder.addResolverActionPlan(testResolverId, new
		// ResolverActionPlan(0, (c) -> {
		// assertFalse(c.subscribersExistForEvent(TestEvent.class));
		// }));
		//
		// // create an agent and have it subscribe to test events at time 1
		// pluginBuilder.addAgent("agent");
		// pluginBuilder.addAgentActionPlan("agent", new AgentActionPlan(1, (c)
		// -> {
		// // add the event labeler to the context
		// c.addEventLabeler(eventLabeler);
		//
		// // subscribe to the event label
		// c.subscribe(eventLabel, (c2, e) -> {
		// });
		// }));
		//
		// // show that the resolver now sees that there are subscribers
		// pluginBuilder.addResolverActionPlan(testResolverId, new
		// ResolverActionPlan(2, (c) -> {
		// assertTrue(c.subscribersExistForEvent(TestEvent.class));
		// }));
		// // have the agent unsubscribe
		// pluginBuilder.addAgentActionPlan("agent", new AgentActionPlan(3, (c)
		// -> {
		// c.unsubscribe(eventLabel);
		// }));
		// // show that the resolver see no subscribers
		// pluginBuilder.addResolverActionPlan(testResolverId, new
		// ResolverActionPlan(4, (c) -> {
		// assertFalse(c.subscribersExistForEvent(TestEvent.class));
		// }));
		//
		// // build the plugin
		// ActionPlugin actionPlugin = pluginBuilder.build();
		//
		// // build and execute the engine
		// Simulation.builder().addPlugin(ActionPlugin.PLUGIN_ID,
		// actionPlugin::init).build().execute();
		//
		// // show that all actions were executed
		// assertTrue(actionPlugin.allActionsExecuted());
		//
		// /////////////////////////////////////////////////////////
		// // Case 2 : a report subscriber
		// /////////////////////////////////////////////////////////
		//
		// // add the test resolver
		// testResolverId = new SimpleResolverId("test resolver");
		// pluginBuilder.addResolver(testResolverId);
		//
		// /*
		// * Have the test resolver show that there are initially no subscribers
		// * to test events.
		// */
		// pluginBuilder.addResolverActionPlan(testResolverId, new
		// ResolverActionPlan(0, (c) -> {
		// assertFalse(c.subscribersExistForEvent(TestEvent.class));
		// }));
		//
		// // add a report
		// ReportId reportId = new SimpleReportId("report");
		// pluginBuilder.addReport(reportId);
		//
		// // have the report subscribe to the test event
		// pluginBuilder.addReportActionPlan(reportId, new ReportActionPlan(1,
		// (c) -> {
		// // add the event labeler to the context
		// c.addEventLabeler(eventLabeler);
		//
		// // subscribe to the event label
		// c.subscribe(eventLabel, (c2, e) -> {
		// });
		// }));
		//
		// // show that the resolver now sees that there are subscribers
		// pluginBuilder.addResolverActionPlan(testResolverId, new
		// ResolverActionPlan(2, (c) -> {
		// assertTrue(c.subscribersExistForEvent(TestEvent.class));
		// }));
		//
		// // build the plugin
		// actionPlugin = pluginBuilder.build();
		//
		// // build and execute the engine
		// Simulation.builder().addPlugin(ActionPlugin.PLUGIN_ID,
		// actionPlugin::init).build().execute();
		//
		// // show that all actions were executed
		// assertTrue(actionPlugin.allActionsExecuted());
		// /////////////////////////////////////////////////////////
		// // Case 3 : a resolver subscriber
		// /////////////////////////////////////////////////////////
		//
		// // add the test resolver
		// testResolverId = new SimpleResolverId("test resolver");
		// pluginBuilder.addResolver(testResolverId);
		//
		// /*
		// * Have the test resolver show that there are initially no subscribers
		// * to test events.
		// */
		// pluginBuilder.addResolverActionPlan(testResolverId, new
		// ResolverActionPlan(0, (c) -> {
		// assertFalse(c.subscribersExistForEvent(TestEvent.class));
		// }));
		//
		// // add a second resolver and have it subscribe to the test event
		// ResolverId subscriberResolverId = new SimpleResolverId("subscriber
		// resolver");
		// pluginBuilder.addResolver(subscriberResolverId);
		//
		// pluginBuilder.addResolverActionPlan(subscriberResolverId, new
		// ResolverActionPlan(1, (c) -> {
		// c.subscribeToEventExecutionPhase(TestEvent.class, (c2, e) -> {
		// });
		// }));
		//
		// // show that the test resolver now sees that there are subscribers
		// pluginBuilder.addResolverActionPlan(testResolverId, new
		// ResolverActionPlan(2, (c) -> {
		// assertTrue(c.subscribersExistForEvent(TestEvent.class));
		// }));
		//
		// // have the second resolver unsubscribe
		// pluginBuilder.addResolverActionPlan(subscriberResolverId, new
		// ResolverActionPlan(3, (c) -> {
		// c.unSubscribeToEvent(TestEvent.class);
		// }));
		//
		// // show that the test resolver now sees that there are no subscribers
		// pluginBuilder.addResolverActionPlan(testResolverId, new
		// ResolverActionPlan(4, (c) -> {
		// assertFalse(c.subscribersExistForEvent(TestEvent.class));
		// }));
		//
		// // build the plugin
		// actionPlugin = pluginBuilder.build();
		//
		// // build and execute the engine
		// Simulation.builder().addPlugin(ActionPlugin.PLUGIN_ID,
		// actionPlugin::init).build().execute();
		//
		// // show that all actions were executed
		// assertTrue(actionPlugin.allActionsExecuted());
	}

}
