package nucleus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

import nucleus.testsupport.actionplugin.ActionPluginInitializer;
import nucleus.testsupport.actionplugin.AgentActionPlan;
import nucleus.testsupport.actionplugin.ReportActionPlan;
import nucleus.testsupport.actionplugin.junk.ActionDataView;
import nucleus.testsupport.actionplugin.DataManagerActionPlan;
import plugins.reports.ReportId;
import util.ContractException;
import util.MultiKey;
import util.MutableBoolean;
import util.MutableInteger;
import util.annotations.UnitTest;
import util.annotations.UnitTestMethod;

@UnitTest(target = DataManagerContext.class)

public class AT_ResolverContext {

	@Test
	@UnitTestMethod(name = "addPlan", args = { Consumer.class, double.class })
	public void testAddPlan() {
		ActionPluginInitializer.Builder pluginBuilder = ActionPluginInitializer.builder();

		// add a resolver
		ResolverId resolverId = new SimpleResolverId("resolver");
		pluginBuilder.addResolver(resolverId);

		// have the resolver test preconditions
		pluginBuilder.addResolverActionPlan(resolverId, new DataManagerActionPlan(5, (c) -> {
			// if the plan is null
			ContractException contractException = assertThrows(ContractException.class, () -> {
				c.addPlan(null, 12.0);
			});
			assertEquals(NucleusError.NULL_PLAN, contractException.getErrorType());

			// if the plan is scheduled for a time in the past
			contractException = assertThrows(ContractException.class, () -> {
				c.addPlan((c2) -> {
				}, 4.0);
			});
			assertEquals(NucleusError.PAST_PLANNING_TIME, contractException.getErrorType());
		}));

		// create a container for expected planning values
		Set<Integer> expectedPlanningValues = new LinkedHashSet<>();
		for (int i = 0; i < 10; i++) {
			expectedPlanningValues.add(i);
		}

		// create a container to collect executed plan information
		Set<Integer> observedPlanningValues = new LinkedHashSet<>();

		// have the resolver add some plans and have each plan execution record
		// data
		pluginBuilder.addResolverActionPlan(resolverId, new DataManagerActionPlan(5, (c) -> {
			double planTime = c.getTime();
			for (Integer value : expectedPlanningValues) {
				planTime += 1;
				c.addPlan((c2) -> {
					observedPlanningValues.add(value);
				}, planTime);
			}
		}));

		// build the plugin
		ActionPluginInitializer actionPluginInitializer = pluginBuilder.build();

		// build and execute the engine
		Simulation.builder().addPlugin(ActionPluginInitializer.PLUGIN_ID, actionPluginInitializer::init).build().execute();

		// show that all the actions executed
		assertTrue(actionPluginInitializer.allActionsExecuted());

		// show that all the plans added by the resolver were executed
		assertEquals(expectedPlanningValues, observedPlanningValues);

	}

	@Test
	@UnitTestMethod(name = "addPlan", args = { Consumer.class, double.class, Object.class })
	public void testAddPlan_WithKey() {

		ActionPluginInitializer.Builder pluginBuilder = ActionPluginInitializer.builder();

		// add a resolver
		ResolverId resolverId = new SimpleResolverId("resolver");
		pluginBuilder.addResolver(resolverId);

		// have the resolver test preconditions
		pluginBuilder.addResolverActionPlan(resolverId, new DataManagerActionPlan(5, (c) -> {
			// if the plan is null
			ContractException contractException = assertThrows(ContractException.class, () -> {
				c.addPlan(null, 12.0, new Object());
			});
			assertEquals(NucleusError.NULL_PLAN, contractException.getErrorType());

			// if the plan is scheduled for a time in the past
			contractException = assertThrows(ContractException.class, () -> {
				c.addPlan((c2) -> {
				}, 4.0, new Object());
			});
			assertEquals(NucleusError.PAST_PLANNING_TIME, contractException.getErrorType());

			// if the key is already in use by an existing plan
			Object key = new Object();
			c.addPlan((c2) -> {
			}, 17, key);

			contractException = assertThrows(ContractException.class, () -> {
				c.addPlan((c2) -> {
				}, 4.0, key);
			});
			assertEquals(NucleusError.DUPLICATE_PLAN_KEY, contractException.getErrorType());
		}));

		// create a container for expected planning values
		Set<Integer> expectedPlanningValues = new LinkedHashSet<>();
		for (int i = 0; i < 10; i++) {
			expectedPlanningValues.add(i);
		}

		// create a container to collected executed plan information
		Set<Integer> observedPlanningValues = new LinkedHashSet<>();

		// Have the resolver add some plans and have each plan record data. Show
		// that each added plan is retrievable by its key.
		pluginBuilder.addResolverActionPlan(resolverId, new DataManagerActionPlan(5, (c) -> {
			double planTime = c.getTime();
			for (Integer value : expectedPlanningValues) {
				planTime += 1;
				// create a plan
				Consumer<DataManagerContext> plan = (c2) -> {
					observedPlanningValues.add(value);
				};
				// schedule the plan with the context
				c.addPlan(plan, planTime, value);

				// retrieve the plan by its key
				Consumer<DataManagerContext> plan2 = c.getPlan(value);

				// show that the retrieved plan is the plan that was added
				assertEquals(plan, plan2);
			}

		}));

		// build the plugin
		ActionPluginInitializer actionPluginInitializer = pluginBuilder.build();

		// build and execute the engine
		Simulation.builder().addPlugin(ActionPluginInitializer.PLUGIN_ID, actionPluginInitializer::init).build().execute();

		// show that all the actions executed
		assertTrue(actionPluginInitializer.allActionsExecuted());

		// show that all the plans added by the resolver were executed
		assertEquals(expectedPlanningValues, observedPlanningValues);

	}

	@Test
	@UnitTestMethod(name = "getPlan", args = { Object.class })
	public void testGetPlan() {

		ActionPluginInitializer.Builder pluginBuilder = ActionPluginInitializer.builder();

		// add a resolver
		ResolverId resolverId = new SimpleResolverId("resolver");
		pluginBuilder.addResolver(resolverId);

		// have the resolver test preconditions
		pluginBuilder.addResolverActionPlan(resolverId, new DataManagerActionPlan(0, (c) -> {
			ContractException contractException = assertThrows(ContractException.class, () -> c.getPlan(null));
			assertEquals(NucleusError.NULL_PLAN_KEY, contractException.getErrorType());
		}));

		// have the resolver add and retrieve some plans
		pluginBuilder.addResolverActionPlan(resolverId, new DataManagerActionPlan(0, (c) -> {
			for (int i = 0; i < 10; i++) {
				Object key = i;
				double planTime = i + 10;
				Consumer<DataManagerContext> plan = (c2) -> {
				};
				c.addPlan(plan, planTime, key);
				assertEquals(plan, c.getPlan(key));
			}
		}));

		// build the plugin
		ActionPluginInitializer actionPluginInitializer = pluginBuilder.build();

		// build and execute the engine
		Simulation.builder().addPlugin(ActionPluginInitializer.PLUGIN_ID, actionPluginInitializer::init).build().execute();

		// show that all actions were executed
		assertTrue(actionPluginInitializer.allActionsExecuted());
	}

	@Test
	@UnitTestMethod(name = "getPlanTime", args = { Object.class })
	public void testGetPlanTime() {

		ActionPluginInitializer.Builder pluginBuilder = ActionPluginInitializer.builder();

		// add a resolver
		ResolverId resolverId = new SimpleResolverId("resolver");
		pluginBuilder.addResolver(resolverId);

		// have the resolver check preconditions
		pluginBuilder.addResolverActionPlan(resolverId, new DataManagerActionPlan(0, (c) -> {
			ContractException contractException = assertThrows(ContractException.class, () -> c.getPlanTime(null));
			assertEquals(NucleusError.NULL_PLAN_KEY, contractException.getErrorType());
		}));

		Map<Object, Double> planTimes = new LinkedHashMap<>();
		planTimes.put("A", 12.3453);
		planTimes.put("B", 4.35);
		planTimes.put("C", 123422.5233);
		planTimes.put("D", 1.6);
		planTimes.put("E", 120000.1);

		/*
		 * Have the resolver schedule plans and retrieve the plan times
		 * associated with the plan's key
		 */
		pluginBuilder.addResolverActionPlan(resolverId, new DataManagerActionPlan(1, (c) -> {
			for (Object key : planTimes.keySet()) {
				Double planTime = planTimes.get(key);
				c.addPlan((c2) -> {
				}, planTime, key);
				assertEquals(c.getPlanTime(key), planTime);
			}

		}));

		// build the plugin
		ActionPluginInitializer actionPluginInitializer = pluginBuilder.build();

		// build and execute the engine
		Simulation.builder().addPlugin(ActionPluginInitializer.PLUGIN_ID, actionPluginInitializer::init).build().execute();

		// show that all actions were executed
		assertTrue(actionPluginInitializer.allActionsExecuted());
	}

	@Test
	@UnitTestMethod(name = "removePlan", args = { Object.class })
	public void testRemovePlan() {

		ActionPluginInitializer.Builder pluginBuilder = ActionPluginInitializer.builder();

		// create a resolver
		ResolverId resolverId = new SimpleResolverId("resolver");
		pluginBuilder.addResolver(resolverId);

		// have the resolver test preconditions
		pluginBuilder.addResolverActionPlan(resolverId, new DataManagerActionPlan(0, (c) -> {
			ContractException contractException = assertThrows(ContractException.class, () -> c.removePlan(null));
			assertEquals(NucleusError.NULL_PLAN_KEY, contractException.getErrorType());
		}));

		/*
		 * Create a counter that will be incremented each time that a plan is
		 * executed despite having been removed.
		 */
		MutableInteger planExecutionCounter = new MutableInteger();

		/*
		 * Have the resolver add and remove some plans, showing that removed
		 * plans are removed and have each plan increment a counter. We expect
		 * the counter to be zero at the end of the simulation.
		 */
		pluginBuilder.addResolverActionPlan(resolverId, new DataManagerActionPlan(1, (c) -> {
			for (int i = 0; i < 10; i++) {
				double planTime = i + 5;
				Object key = i;
				c.addPlan((c2) -> {
					planExecutionCounter.increment();
				}, planTime, key);
				assertNotNull(c.getPlan(key));
				c.removePlan(key);
				assertNull(c.getPlan(key));
			}
		}));
		// build the plugin
		ActionPluginInitializer actionPluginInitializer = pluginBuilder.build();

		// build and execute the engine
		Simulation.builder().addPlugin(ActionPluginInitializer.PLUGIN_ID, actionPluginInitializer::init).build().execute();

		// show that all actions were executed
		assertTrue(actionPluginInitializer.allActionsExecuted());

		// show that non of the removed plans executed
		assertEquals(0, planExecutionCounter.getValue());
	}

	@Test
	@UnitTestMethod(name = "getPlanKeys", args = {})
	public void testGetPlanKeys() {

		ActionPluginInitializer.Builder pluginBuilder = ActionPluginInitializer.builder();

		// add two resolvers just to show that plan keys are specific to the
		// resolver
		ResolverId resolverId_1 = new SimpleResolverId("resolver 1");
		pluginBuilder.addResolver(resolverId_1);

		ResolverId resolverId_2 = new SimpleResolverId("resolver 2");
		pluginBuilder.addResolver(resolverId_2);

		// create some plan keys for the two resolvers, using a bit of overlap
		Set<Object> planKeys_1 = new LinkedHashSet<>();
		planKeys_1.add("A");
		planKeys_1.add("B");
		planKeys_1.add("C");
		planKeys_1.add("D");
		planKeys_1.add("E");

		Set<Object> planKeys_2 = new LinkedHashSet<>();
		planKeys_1.add("C");
		planKeys_1.add("D");
		planKeys_1.add("E");
		planKeys_1.add("F");
		planKeys_1.add("G");

		// have the two resolvers add the plans for some far future action
		pluginBuilder.addResolverActionPlan(resolverId_1, new DataManagerActionPlan(1, (c) -> {
			for (Object key : planKeys_1) {
				c.addPlan((c2) -> {
				}, 1000, key);
			}
		}));

		pluginBuilder.addResolverActionPlan(resolverId_2, new DataManagerActionPlan(1, (c) -> {
			for (Object key : planKeys_2) {
				c.addPlan((c2) -> {
				}, 1000, key);
			}
		}));

		/*
		 * Have the two resolvers get their plan keys and show that they match
		 * our expectations
		 */

		pluginBuilder.addResolverActionPlan(resolverId_1, new DataManagerActionPlan(2, (c) -> {
			assertEquals(planKeys_1, c.getPlanKeys().stream().collect(Collectors.toCollection(LinkedHashSet::new)));
		}));

		pluginBuilder.addResolverActionPlan(resolverId_2, new DataManagerActionPlan(2, (c) -> {
			assertEquals(planKeys_2, c.getPlanKeys().stream().collect(Collectors.toCollection(LinkedHashSet::new)));
		}));

		// build the plugin
		ActionPluginInitializer actionPluginInitializer = pluginBuilder.build();

		// build and execute the engine
		Simulation.builder().addPlugin(ActionPluginInitializer.PLUGIN_ID, actionPluginInitializer::init).build().execute();

		// show that all the actions executed
		assertTrue(actionPluginInitializer.allActionsExecuted());
	}

	@Test
	@UnitTestMethod(name = "currentAgentIsEventSource", args = {})
	public void testCurrentAgentIsEventSource() {

		ActionPluginInitializer.Builder pluginBuilder = ActionPluginInitializer.builder();

		// create a container for the test resolver to record observed values
		Set<MultiKey> observedData = new LinkedHashSet<>();

		// create a container for the expected data
		Set<MultiKey> expectedData = new LinkedHashSet<>();
		expectedData.add(new MultiKey(false, 10.0));
		expectedData.add(new MultiKey(true, 20.0));

		/*
		 * add a resolver that will receive test events from both another
		 * resolver and from an agent
		 */
		ResolverId resolverId_1 = new SimpleResolverId("event handling resolver");
		pluginBuilder.addResolver(resolverId_1);

		pluginBuilder.addResolverActionPlan(resolverId_1, new DataManagerActionPlan(0, (c) -> {
			c.subscribeToEventExecutionPhase(TestEvent.class, (c2, e) -> {
				observedData.add(new MultiKey(c2.currentAgentIsEventSource(), c2.getTime()));
			});
		}));

		// add another resolver that will send a test event at time 10
		ResolverId resolverId_2 = new SimpleResolverId("sending resolver");
		pluginBuilder.addResolver(resolverId_2);

		pluginBuilder.addResolverActionPlan(resolverId_2, new DataManagerActionPlan(10, (c) -> {
			c.resolveEvent(new TestEvent());
		}));

		// add an agent that will send a test event at time 20
		pluginBuilder.addAgent("sending agent");
		pluginBuilder.addAgentActionPlan("sending agent", new AgentActionPlan(20, (c) -> {
			c.resolveEvent(new TestEvent());
		}));

		// build the plugin
		ActionPluginInitializer actionPluginInitializer = pluginBuilder.build();

		// build and execute the engine
		Simulation.builder().addPlugin(ActionPluginInitializer.PLUGIN_ID, actionPluginInitializer::init).build().execute();

		// show that all the actions executed
		assertTrue(actionPluginInitializer.allActionsExecuted());

		// show that the observed data matches the expected data
		assertEquals(expectedData, observedData);
	}

	@Test
	@UnitTestMethod(name = "queueEventForResolution", args = { Event.class })
	public void testQueueEventForResolution() {

		ActionPluginInitializer.Builder pluginBuilder = ActionPluginInitializer.builder();

		// add two resolvers
		ResolverId resolverId_1 = new SimpleResolverId("resolver 1");
		pluginBuilder.addResolver(resolverId_1);

		ResolverId resolverId_2 = new SimpleResolverId("resolver 2");
		pluginBuilder.addResolver(resolverId_2);

		// have the first resolver test preconditions
		pluginBuilder.addResolverActionPlan(resolverId_1, new DataManagerActionPlan(0, (c) -> {
			ContractException contractException = assertThrows(ContractException.class, () -> c.resolveEvent(null));
			assertEquals(NucleusError.NULL_EVENT, contractException.getErrorType());
		}));

		/*
		 * Create a container that shows the test event was received and thus
		 * the queue works
		 */
		MutableBoolean testEventReceived = new MutableBoolean();

		/*
		 * Have the second resolver subscribe to test events and record when a
		 * test event is received
		 */
		pluginBuilder.addResolverActionPlan(resolverId_2, new DataManagerActionPlan(0, (c) -> {
			c.subscribeToEventExecutionPhase(TestEvent.class, (c2, e) -> {
				testEventReceived.setValue(true);
			});
		}));

		// have the first resolver queue a test event for resolution
		pluginBuilder.addResolverActionPlan(resolverId_2, new DataManagerActionPlan(5, (c) -> {
			c.resolveEvent(new TestEvent());
		}));

		// build the plugin
		ActionPluginInitializer actionPluginInitializer = pluginBuilder.build();

		// build and execute the engine
		Simulation.builder().addPlugin(ActionPluginInitializer.PLUGIN_ID, actionPluginInitializer::init).build().execute();

		// show that all actions were executed
		assertTrue(actionPluginInitializer.allActionsExecuted());

		// show that the test event was received and thus the queue works
		assertTrue(testEventReceived.getValue());

	}

	@Test
	@UnitTestMethod(name = "getAvailableAgentId", args = {})
	public void testGetAvailableAgentId() {

		ActionPluginInitializer.Builder pluginBuilder = ActionPluginInitializer.builder();

		// add two resolvers to show that agent ids are apportioned globally
		ResolverId resolverId_1 = new SimpleResolverId("resolver 1");
		pluginBuilder.addResolver(resolverId_1);

		ResolverId resolverId_2 = new SimpleResolverId("resolver 2");
		pluginBuilder.addResolver(resolverId_2);

		// create a container to hold the available agent ids as they are
		// discovered
		Set<AgentId> observedAgentIds = new LinkedHashSet<>();

		// create the set of expected AgentIds
		Set<AgentId> expectedAgentIds = new LinkedHashSet<>();
		for (int i = 0; i < 6; i++) {
			expectedAgentIds.add(new AgentId(i));
		}
		/*
		 * Have the resolvers plan the addition of agents using the available
		 * agent ids over six times. Record the agent ids into a container.
		 */
		pluginBuilder.addResolverActionPlan(resolverId_1, new DataManagerActionPlan(0, (c) -> {
			for (int i = 0; i < 3; i++) {
				Consumer<DataManagerContext> plan = (c2) -> {
					AgentId agentId = c.getAvailableAgentId();
					observedAgentIds.add(agentId);
					c.addAgent((c3) -> {
					}, agentId);
				};

				c.addPlan(plan, i + 7);
			}
		}));

		pluginBuilder.addResolverActionPlan(resolverId_2, new DataManagerActionPlan(0, (c) -> {
			for (int i = 0; i < 3; i++) {
				Consumer<DataManagerContext> plan = (c2) -> {
					AgentId agentId = c.getAvailableAgentId();
					observedAgentIds.add(agentId);
					c.addAgent((c3) -> {
					}, agentId);
				};

				c.addPlan(plan, i + 8);
			}
		}));

		// build the plugin
		ActionPluginInitializer actionPluginInitializer = pluginBuilder.build();

		// build and execute the engine
		Simulation.builder().addPlugin(ActionPluginInitializer.PLUGIN_ID, actionPluginInitializer::init).build().execute();

		// show that all actions were executed
		assertTrue(actionPluginInitializer.allActionsExecuted());

		// show that the available agent ids matched our expectations
		assertEquals(expectedAgentIds, observedAgentIds);

	}

	private static class TestEvent implements Event {

	}

	@Test
	@UnitTestMethod(name = "getCurrentAgentId", args = {})
	public void testGetCurrentAgentId() {

		ActionPluginInitializer.Builder pluginBuilder = ActionPluginInitializer.builder();

		// build a resolver
		ResolverId resolverId = new SimpleResolverId("resolver");
		pluginBuilder.addResolver(resolverId);

		// create a container for holding the test events received by the
		// resolver
		Set<MultiKey> observedEvents = new LinkedHashSet<>();

		// have the resolver subscribe to Test Event
		pluginBuilder.addResolverActionPlan(resolverId, new DataManagerActionPlan(0, (c) -> {
			c.subscribeToEventExecutionPhase(TestEvent.class, (c2, e) -> {
				ActionDataView actionDataView = c2.getDataView(ActionDataView.class).get();
				Object alias = actionDataView.getAgentAliasId(c2.getCurrentAgentId()).get();
				observedEvents.add(new MultiKey(alias, c2.getTime()));
			});
		}));

		// add a few new agents
		pluginBuilder.addAgent("Alpha");
		pluginBuilder.addAgent("Beta");
		pluginBuilder.addAgent("Gamma");

		// have the new agents generate some TestEvents at various times
		pluginBuilder.addAgentActionPlan("Alpha", new AgentActionPlan(1, (c) -> c.resolveEvent(new TestEvent())));
		pluginBuilder.addAgentActionPlan("Gamma", new AgentActionPlan(2, (c) -> c.resolveEvent(new TestEvent())));
		pluginBuilder.addAgentActionPlan("Gamma", new AgentActionPlan(3, (c) -> c.resolveEvent(new TestEvent())));
		pluginBuilder.addAgentActionPlan("Alpha", new AgentActionPlan(4, (c) -> c.resolveEvent(new TestEvent())));
		pluginBuilder.addAgentActionPlan("Gamma", new AgentActionPlan(5, (c) -> c.resolveEvent(new TestEvent())));
		pluginBuilder.addAgentActionPlan("Beta", new AgentActionPlan(6, (c) -> c.resolveEvent(new TestEvent())));
		pluginBuilder.addAgentActionPlan("Alpha", new AgentActionPlan(7, (c) -> c.resolveEvent(new TestEvent())));
		pluginBuilder.addAgentActionPlan("Gamma", new AgentActionPlan(8, (c) -> c.resolveEvent(new TestEvent())));
		pluginBuilder.addAgentActionPlan("Alpha", new AgentActionPlan(9, (c) -> c.resolveEvent(new TestEvent())));
		pluginBuilder.addAgentActionPlan("Beta", new AgentActionPlan(10, (c) -> c.resolveEvent(new TestEvent())));

		// generate the expected events that the resolver should observer
		Set<MultiKey> expectedEvents = new LinkedHashSet<>();
		expectedEvents.add(new MultiKey("Alpha", 1.0));
		expectedEvents.add(new MultiKey("Gamma", 2.0));
		expectedEvents.add(new MultiKey("Gamma", 3.0));
		expectedEvents.add(new MultiKey("Alpha", 4.0));
		expectedEvents.add(new MultiKey("Gamma", 5.0));
		expectedEvents.add(new MultiKey("Beta", 6.0));
		expectedEvents.add(new MultiKey("Alpha", 7.0));
		expectedEvents.add(new MultiKey("Gamma", 8.0));
		expectedEvents.add(new MultiKey("Alpha", 9.0));
		expectedEvents.add(new MultiKey("Beta", 10.0));

		// build the plugin
		ActionPluginInitializer actionPluginInitializer = pluginBuilder.build();

		// build and execute the engine
		Simulation.builder().addPlugin(ActionPluginInitializer.PLUGIN_ID, actionPluginInitializer::init).build().execute();

		// show that all the actions were executed
		assertTrue(actionPluginInitializer.allActionsExecuted());

		// show the the data collected by the resolver reflects the proper agent
		// identification
		assertEquals(expectedEvents, observedEvents);
	}

	@Test
	@UnitTestMethod(name = "agentExists", args = { AgentId.class })
	public void testAgentExists() {

		ActionPluginInitializer.Builder pluginBuilder = ActionPluginInitializer.builder();

		// add a resolver
		ResolverId resolverId = new SimpleResolverId("resolver");
		pluginBuilder.addResolver(resolverId);

		// have the resolver create a few agents and test the existence of
		// various agent id values

		pluginBuilder.addResolverActionPlan(resolverId, new DataManagerActionPlan(0, (c) -> {
			for (int i = 0; i < 10; i++) {
				assertFalse(c.agentExists(new AgentId(i)));
				c.addAgent((c2) -> {
				}, new AgentId(i));
				assertTrue(c.agentExists(new AgentId(i)));
			}

			for (int i = 0; i < 10; i++) {
				assertTrue(c.agentExists(new AgentId(i)));
			}
			for (int i = 11; i < 20; i++) {
				assertFalse(c.agentExists(new AgentId(i)));
			}
		}));

		// build the plugin
		ActionPluginInitializer actionPluginInitializer = pluginBuilder.build();

		// build and execute the engine
		Simulation.builder().addPlugin(ActionPluginInitializer.PLUGIN_ID, actionPluginInitializer::init).build().execute();

		// show that all the actions executed
		assertTrue(actionPluginInitializer.allActionsExecuted());

	}

	@Test

	@UnitTestMethod(name = "addAgent", args = { Consumer.class, AgentId.class })
	public void testAddAgent() {

		ActionPluginInitializer.Builder pluginBuilder = ActionPluginInitializer.builder();

		// add a resolver
		ResolverId resolverId = new SimpleResolverId("resolver");
		pluginBuilder.addResolver(resolverId);

		// have the resolver create a few agents
		pluginBuilder.addResolverActionPlan(resolverId, new DataManagerActionPlan(0, (c) -> {
			for (int i = 0; i < 10; i++) {
				assertFalse(c.agentExists(new AgentId(i)));

				c.addAgent((c2) -> {
				}, new AgentId(i));

				assertTrue(c.agentExists(new AgentId(i)));
			}

		}));

		// precondition tests
		pluginBuilder.addResolverActionPlan(resolverId, new DataManagerActionPlan(0, (c) -> {

			AgentId availableAgentId = c.getAvailableAgentId();

			ContractException contractException = assertThrows(ContractException.class, () -> c.addAgent(null, availableAgentId));

			assertEquals(NucleusError.NULL_AGENT_CONTEXT_CONSUMER, contractException.getErrorType());

			contractException = assertThrows(ContractException.class, () -> c.addAgent((c2) -> {
			}, new AgentId(-1)));
			assertEquals(NucleusError.NEGATIVE_AGENT_ID, contractException.getErrorType());

			contractException = assertThrows(ContractException.class, () -> c.addAgent((c2) -> {
			}, new AgentId(0)));
			assertEquals(NucleusError.AGENT_ID_IN_USE, contractException.getErrorType());

		}));

		// build the plugin
		ActionPluginInitializer actionPluginInitializer = pluginBuilder.build();

		// build and execute the engine
		Simulation.builder().addPlugin(ActionPluginInitializer.PLUGIN_ID, actionPluginInitializer::init).build().execute();

		// show that all the actions executed
		assertTrue(actionPluginInitializer.allActionsExecuted());

	}

	@Test
	@UnitTestMethod(name = "removeAgent", args = { AgentId.class })
	public void testRemoveAgent() {
		ActionPluginInitializer.Builder pluginBuilder = ActionPluginInitializer.builder();

		// add a test resolver
		ResolverId resolverId = new SimpleResolverId("resolver");
		pluginBuilder.addResolver(resolverId);

		// have the resolver execute the precondition tests
		pluginBuilder.addResolverActionPlan(resolverId, new DataManagerActionPlan(0, (c) -> {

			ContractException contractException = assertThrows(ContractException.class, () -> c.removeAgent(new AgentId(-1)));
			assertEquals(NucleusError.NEGATIVE_AGENT_ID, contractException.getErrorType());

			contractException = assertThrows(ContractException.class, () -> c.removeAgent(null));
			assertEquals(NucleusError.NULL_AGENT_ID, contractException.getErrorType());

			contractException = assertThrows(ContractException.class, () -> c.removeAgent(new AgentId(1000)));
			assertEquals(NucleusError.UNKNOWN_AGENT_ID, contractException.getErrorType());

		}));

		// have the add a few agents
		pluginBuilder.addResolverActionPlan(resolverId, new DataManagerActionPlan(1, (c) -> {
			for (int i = 0; i < 10; i++) {
				AgentId agentId = new AgentId(i);
				c.addAgent((c2) -> {
				}, agentId);
				assertTrue(c.agentExists(agentId));
			}
		}));

		// have the resolver remove a the recently added agents
		pluginBuilder.addResolverActionPlan(resolverId, new DataManagerActionPlan(2, (c) -> {
			for (int i = 0; i < 10; i++) {
				AgentId agentId = new AgentId(i);
				assertTrue(c.agentExists(agentId));
				c.removeAgent(agentId);
				assertFalse(c.agentExists(agentId));
			}
		}));

		// build the plugin
		ActionPluginInitializer actionPluginInitializer = pluginBuilder.build();

		// build and execute the engine
		Simulation.builder().addPlugin(ActionPluginInitializer.PLUGIN_ID, actionPluginInitializer::init).build().execute();

		// show that the actions were executed
		assertTrue(actionPluginInitializer.allActionsExecuted());
	}

	@Test
	@UnitTestMethod(name = "addReport", args = { ReportId.class, Consumer.class })
	public void testAddReport() {
		ActionPluginInitializer.Builder pluginBuilder = ActionPluginInitializer.builder();

		// add a test resolver
		ResolverId resolverId = new SimpleResolverId("resolver");
		pluginBuilder.addResolver(resolverId);

		// have the resolver test preconditions
		pluginBuilder.addResolverActionPlan(resolverId, new DataManagerActionPlan(0, (c) -> {
			// if the report id is null
			ContractException contractException = assertThrows(ContractException.class, () -> c.addReport(null, (c2) -> {
			}));
			assertEquals(NucleusError.NULL_REPORT_ID, contractException.getErrorType());

			// if the report context consumer is null
			contractException = assertThrows(ContractException.class, () -> c.addReport(new SimpleReportId("test report"), null));
			assertEquals(NucleusError.NULL_REPORT_CONTEXT_CONSUMER, contractException.getErrorType());

			// if the report id is in use by another report
			ReportId reportId = new SimpleReportId("test report");
			c.addReport(reportId, (c2) -> {
			});
			contractException = assertThrows(ContractException.class, () -> c.addReport(reportId, (c2) -> {
			}));
			assertEquals(NucleusError.REPORT_ID_IN_USE, contractException.getErrorType());

		}));

		// create a set of report ids for the resolver to add
		Set<ReportId> exepectedReportIds = new LinkedHashSet<>();
		for (int i = 0; i < 10; i++) {
			ReportId reportId = new SimpleReportId("report " + i);
			exepectedReportIds.add(reportId);
		}

		// create a set of report ids for the reports to record
		Set<ReportId> observedReportIds = new LinkedHashSet<>();

		// have the resolver add the reports, with each report recording its id
		pluginBuilder.addResolverActionPlan(resolverId, new DataManagerActionPlan(0, (c) -> {
			for (ReportId reportId : exepectedReportIds) {
				c.addReport(reportId, (c2) -> {
					observedReportIds.add(c2.getCurrentReportId());
				});
			}
		}));

		// build the plugin
		ActionPluginInitializer actionPluginInitializer = pluginBuilder.build();

		// build and execute the engine
		Simulation.builder().addPlugin(ActionPluginInitializer.PLUGIN_ID, actionPluginInitializer::init).build().execute();

		// show that all actions were executed
		assertTrue(actionPluginInitializer.allActionsExecuted());

		// show the added reports matched our expectations
		assertEquals(exepectedReportIds, observedReportIds);

	}

	@Test
	@UnitTestMethod(name = "halt", args = {})
	public void testHalt() {

		ActionPluginInitializer.Builder pluginBuilder = ActionPluginInitializer.builder();

		// add a test resolver
		ResolverId resolverId = new SimpleResolverId("resolver");
		pluginBuilder.addResolver(resolverId);

		// there are no precondition tests

		// have the test resolver execute several tasks, with one of the tasks
		// halting the simulation
		DataManagerActionPlan actionPlan1 = new DataManagerActionPlan(1, (context) -> {
		});
		pluginBuilder.addResolverActionPlan(resolverId, actionPlan1);

		DataManagerActionPlan actionPlan2 = new DataManagerActionPlan(2, (context) -> {
		});
		pluginBuilder.addResolverActionPlan(resolverId, actionPlan2);

		DataManagerActionPlan actionPlan3 = new DataManagerActionPlan(3, (context) -> {
			context.halt();
		});
		pluginBuilder.addResolverActionPlan(resolverId, actionPlan3);

		DataManagerActionPlan actionPlan4 = new DataManagerActionPlan(4, (context) -> {
		});
		pluginBuilder.addResolverActionPlan(resolverId, actionPlan4);

		DataManagerActionPlan actionPlan5 = new DataManagerActionPlan(5, (context) -> {
		});
		pluginBuilder.addResolverActionPlan(resolverId, actionPlan5);

		// build the plugin
		ActionPluginInitializer actionPluginInitializer = pluginBuilder.build();

		// run the simulation
		Simulation.builder().addPlugin(ActionPluginInitializer.PLUGIN_ID, actionPluginInitializer::init).build().execute();

		// show that the plans that were scheduled after the halt did not
		// execute
		assertTrue(actionPlan1.executed());
		assertTrue(actionPlan2.executed());
		assertTrue(actionPlan3.executed());
		assertFalse(actionPlan4.executed());
		assertFalse(actionPlan5.executed());

	}

	@Test
	@UnitTestMethod(name = "subscribeToEventValidationPhase", args = { Class.class, DataManagerEventConsumer.class })
	public void testSubscribeToEventValidationPhase() {
		combinedSubscriptionTest();
	}

	private void combinedSubscriptionTest() {

		ActionPluginInitializer.Builder pluginBuilder = ActionPluginInitializer.builder();

		// add a resolver
		ResolverId resolverId = new SimpleResolverId("resolver");
		pluginBuilder.addResolver(resolverId);

		/*
		 * create a container that will record the phases of event resolution
		 * that were executed by the resolver that reflects the order in which
		 * they occured
		 */

		List<String> observedPhases = new ArrayList<>();

		// create a container with the phases we expect in the order we expect
		// them
		List<String> expectedPhases = new ArrayList<>();
		expectedPhases.add("validation");
		expectedPhases.add("execution");
		expectedPhases.add("post-action");

		// have the resolver test preconditions for all the phases
		pluginBuilder.addResolverActionPlan(resolverId, new DataManagerActionPlan(0, (c) -> {
			ContractException contractException = assertThrows(ContractException.class, () -> c.subscribeToEventValidationPhase(null, (c2, e) -> {
			}));
			assertEquals(NucleusError.NULL_EVENT_CLASS, contractException.getErrorType());

			contractException = assertThrows(ContractException.class, () -> c.subscribeToEventValidationPhase(TestEvent.class, null));
			assertEquals(NucleusError.NULL_EVENT_CONSUMER, contractException.getErrorType());

			contractException = assertThrows(ContractException.class, () -> c.subscribeToEventExecutionPhase(null, (c2, e) -> {
			}));
			assertEquals(NucleusError.NULL_EVENT_CLASS, contractException.getErrorType());

			contractException = assertThrows(ContractException.class, () -> c.subscribeToEventExecutionPhase(TestEvent.class, null));
			assertEquals(NucleusError.NULL_EVENT_CONSUMER, contractException.getErrorType());

			contractException = assertThrows(ContractException.class, () -> c.subscribeToEventPostPhase(null, (c2, e) -> {
			}));
			assertEquals(NucleusError.NULL_EVENT_CLASS, contractException.getErrorType());

			contractException = assertThrows(ContractException.class, () -> c.subscribeToEventPostPhase(TestEvent.class, null));
			assertEquals(NucleusError.NULL_EVENT_CONSUMER, contractException.getErrorType());

		}));

		// have the resolver subscribe to the three phases for test events.
		pluginBuilder.addResolverActionPlan(resolverId, new DataManagerActionPlan(0, (c) -> {
			c.subscribeToEventValidationPhase(TestEvent.class, (c2, e) -> {
				observedPhases.add("validation");
			});

			c.subscribeToEventExecutionPhase(TestEvent.class, (c2, e) -> {
				observedPhases.add("execution");
			});

			c.subscribeToEventPostPhase(TestEvent.class, (c2, e) -> {
				observedPhases.add("post-action");
			});
		}));

		// create an agent that will generate a test event
		pluginBuilder.addAgent("agent");
		pluginBuilder.addAgentActionPlan("agent", new AgentActionPlan(1, (c) -> {
			c.resolveEvent(new TestEvent());
		}));

		// build the plugin
		ActionPluginInitializer actionPluginInitializer = pluginBuilder.build();

		// build and execute the engine
		Simulation.builder().addPlugin(ActionPluginInitializer.PLUGIN_ID, actionPluginInitializer::init).build().execute();

		// show that the resolver engaged in the three event resolution phases
		// in the proper order
		assertEquals(expectedPhases, observedPhases);

	}

	@Test
	@UnitTestMethod(name = "subscribeToEventExecutionPhase", args = { Class.class, DataManagerEventConsumer.class })
	public void testSubscribeToEventExecutionPhase() {
		combinedSubscriptionTest();
	}

	@Test
	@UnitTestMethod(name = "subscribeToEventPostPhase", args = { Class.class, DataManagerEventConsumer.class })
	public void testSubscribeToEventPostPhase() {
		combinedSubscriptionTest();
	}

	@Test
	@UnitTestMethod(name = "unSubscribeToEvent", args = { Class.class })
	public void testUnSubscribeToEvent() {

		ActionPluginInitializer.Builder pluginBuilder = ActionPluginInitializer.builder();

		// add a resolver
		ResolverId resolverId = new SimpleResolverId("resolver");
		pluginBuilder.addResolver(resolverId);

		// have the resolver test preconditions
		pluginBuilder.addResolverActionPlan(resolverId, new DataManagerActionPlan(0, (c) -> {
			ContractException contractException = assertThrows(ContractException.class, () -> c.unSubscribeToEvent(null));
			assertEquals(NucleusError.NULL_EVENT_CLASS, contractException.getErrorType());
		}));

		/*
		 * Create a container to count then number of times a subscription
		 * execution occured
		 */
		MutableInteger phaseExecutionCount = new MutableInteger();

		// have the resolver subscribe to the test event and have it handle each
		// type of event handling by incrementing a counter
		pluginBuilder.addResolverActionPlan(resolverId, new DataManagerActionPlan(0, (c) -> {

			c.subscribeToEventValidationPhase(TestEvent.class, (c2, e) -> {
				phaseExecutionCount.increment();
			});

			c.subscribeToEventExecutionPhase(TestEvent.class, (c2, e) -> {
				phaseExecutionCount.increment();
			});

			c.subscribeToEventPostPhase(TestEvent.class, (c2, e) -> {
				phaseExecutionCount.increment();
			});

		}));

		// create an agent that will produce a test event
		pluginBuilder.addAgent("agent");
		pluginBuilder.addAgentActionPlan("agent", new AgentActionPlan(1, (c) -> {
			c.resolveEvent(new TestEvent());
		}));

		/*
		 * Show that the phaseExecutionCount is three after the the agent is
		 * done
		 */
		pluginBuilder.addResolverActionPlan(resolverId, new DataManagerActionPlan(2, (c) -> {
			assertEquals(3, phaseExecutionCount.getValue());
		}));

		// have the resolver unsubscribe
		pluginBuilder.addResolverActionPlan(resolverId, new DataManagerActionPlan(3, (c) -> {
			c.unSubscribeToEvent(TestEvent.class);
		}));

		// have the agent generate another test event
		pluginBuilder.addAgentActionPlan("agent", new AgentActionPlan(4, (c) -> {
			c.resolveEvent(new TestEvent());
		}));

		/*
		 * Show that the phaseExecutionCount is still three after the the agent
		 * is done and thus the resolver is no longer subscribed
		 */
		pluginBuilder.addResolverActionPlan(resolverId, new DataManagerActionPlan(5, (c) -> {
			assertEquals(3, phaseExecutionCount.getValue());
		}));

		// build the plugin
		ActionPluginInitializer actionPluginInitializer = pluginBuilder.build();

		// build and execute the engine
		Simulation.builder().addPlugin(ActionPluginInitializer.PLUGIN_ID, actionPluginInitializer::init).build().execute();

		// show that all actions executed
		assertTrue(actionPluginInitializer.allActionsExecuted());
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
		public EventLabel<TestEvent> getEventLabel(SimulationContext simulationContext, TestEvent event) {
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

		ActionPluginInitializer.Builder pluginBuilder = ActionPluginInitializer.builder();

		// add a resolver
		ResolverId resolverId = new SimpleResolverId("resolver");
		pluginBuilder.addResolver(resolverId);

		// have the resolver test the preconditions
		pluginBuilder.addResolverActionPlan(resolverId, new DataManagerActionPlan(0, (c) -> {
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

		// create a new event labeler that will be added by the resolver and the
		// utilized by an agent.
		EventLabelerId id = new EventLabelerId() {
		};

		EventLabeler<TestEvent> eventLabeler = new TestEventLabeler(TestEvent.class, id);

		// have the resolver add the event labeler
		pluginBuilder.addResolverActionPlan(resolverId, new DataManagerActionPlan(1, (c) -> {
			c.addEventLabeler(eventLabeler);
		}));

		/*
		 * Create a container for the agent to record that it received the Test
		 * Event and we can conclude that the event labeler had been properly
		 * added to the simulation.
		 */
		MutableBoolean eventObserved = new MutableBoolean();

		// add an agent that will observe the test event
		pluginBuilder.addAgent("observer agent");
		pluginBuilder.addAgentActionPlan("observer agent", new AgentActionPlan(2, (c) -> {
			c.subscribe(new MultiKeyEventLabel<>(TestEvent.class, id, TestEvent.class), (c2, e) -> {
				eventObserved.setValue(true);
			});
		}));

		// have the resolver create a test event for the agent to observe
		pluginBuilder.addResolverActionPlan(resolverId, new DataManagerActionPlan(3, (c) -> {
			c.resolveEvent(new TestEvent());
		}));

		// build the plugin
		ActionPluginInitializer actionPluginInitializer = pluginBuilder.build();

		// build and execute the engine
		Simulation.builder().addPlugin(ActionPluginInitializer.PLUGIN_ID, actionPluginInitializer::init).build().execute();

		// show that all actions were executed
		assertTrue(actionPluginInitializer.allActionsExecuted());

		/*
		 * Show that the event labeler must have been added to the simulation
		 * since the agent observed the test event
		 */
		assertTrue(eventObserved.getValue());

	}

	@Test
	@UnitTestMethod(name = "getSafeContext", args = {})
	public void testGetSafeContext() {

		ActionPluginInitializer.Builder pluginBuilder = ActionPluginInitializer.builder();

		// add a resolver
		ResolverId resolverId = new SimpleResolverId("resolver");
		pluginBuilder.addResolver(resolverId);

		// have the resolver get a safe context
		pluginBuilder.addResolverActionPlan(resolverId, new DataManagerActionPlan(0, (c) -> {
			SimulationContext safeContext = c.getSafeContext();
			assertNotNull(safeContext);
		}));

		// build the plugin
		ActionPluginInitializer actionPluginInitializer = pluginBuilder.build();

		// build and execute the engine
		Simulation.builder().addPlugin(ActionPluginInitializer.PLUGIN_ID, actionPluginInitializer::init).build().execute();

		// show that all the actions were executed
		assertTrue(actionPluginInitializer.allActionsExecuted());
	}

	@Test
	@UnitTestMethod(name = "subscribersExistForEvent", args = { Class.class })
	public void testSubscribersExistForEvent() {

		// create an event labeler id
		EventLabelerId eventLabelerId = new EventLabelerId() {
		};

		// create a simple event label as a place holder -- all test events will
		// be matched
		MultiKeyEventLabel<TestEvent> eventLabel = new MultiKeyEventLabel<>(TestEvent.class, eventLabelerId, TestEvent.class);

		// create an event labeler that always returns the label above
		EventLabeler<TestEvent> eventLabeler = new SimpleEventLabeler<>(eventLabelerId, TestEvent.class, (c2, e) -> {
			return eventLabel;
		});

		ActionPluginInitializer.Builder pluginBuilder = ActionPluginInitializer.builder();

		/////////////////////////////////////////////////////////
		// Case 1 : an agent subscriber
		/////////////////////////////////////////////////////////

		// add the test resolver
		ResolverId testResolverId = new SimpleResolverId("test resolver");
		pluginBuilder.addResolver(testResolverId);

		/*
		 * Have the test resolver show that there are initially no subscribers
		 * to test events.
		 */
		pluginBuilder.addResolverActionPlan(testResolverId, new DataManagerActionPlan(0, (c) -> {
			assertFalse(c.subscribersExistForEvent(TestEvent.class));
		}));

		// create an agent and have it subscribe to test events at time 1
		pluginBuilder.addAgent("agent");
		pluginBuilder.addAgentActionPlan("agent", new AgentActionPlan(1, (c) -> {
			// add the event labeler to the context
			c.addEventLabeler(eventLabeler);

			// subscribe to the event label
			c.subscribe(eventLabel, (c2, e) -> {
			});
		}));

		// show that the resolver now sees that there are subscribers
		pluginBuilder.addResolverActionPlan(testResolverId, new DataManagerActionPlan(2, (c) -> {
			assertTrue(c.subscribersExistForEvent(TestEvent.class));
		}));
		// have the agent unsubscribe
		pluginBuilder.addAgentActionPlan("agent", new AgentActionPlan(3, (c) -> {
			c.unsubscribe(eventLabel);
		}));
		// show that the resolver see no subscribers
		pluginBuilder.addResolverActionPlan(testResolverId, new DataManagerActionPlan(4, (c) -> {
			assertFalse(c.subscribersExistForEvent(TestEvent.class));
		}));

		// build the plugin
		ActionPluginInitializer actionPluginInitializer = pluginBuilder.build();

		// build and execute the engine
		Simulation.builder().addPlugin(ActionPluginInitializer.PLUGIN_ID, actionPluginInitializer::init).build().execute();

		// show that all actions were executed
		assertTrue(actionPluginInitializer.allActionsExecuted());

		/////////////////////////////////////////////////////////
		// Case 2 : a report subscriber
		/////////////////////////////////////////////////////////

		// add the test resolver
		testResolverId = new SimpleResolverId("test resolver");
		pluginBuilder.addResolver(testResolverId);

		/*
		 * Have the test resolver show that there are initially no subscribers
		 * to test events.
		 */
		pluginBuilder.addResolverActionPlan(testResolverId, new DataManagerActionPlan(0, (c) -> {
			assertFalse(c.subscribersExistForEvent(TestEvent.class));
		}));

		// add a report
		ReportId reportId = new SimpleReportId("report");
		pluginBuilder.addReport(reportId);

		// have the report subscribe to the test event
		pluginBuilder.addReportActionPlan(reportId, new ReportActionPlan(1, (c) -> {
			// add the event labeler to the context
			c.addEventLabeler(eventLabeler);

			// subscribe to the event label
			c.subscribe(eventLabel, (c2, e) -> {
			});
		}));

		// show that the resolver now sees that there are subscribers
		pluginBuilder.addResolverActionPlan(testResolverId, new DataManagerActionPlan(2, (c) -> {
			assertTrue(c.subscribersExistForEvent(TestEvent.class));
		}));

		// build the plugin
		actionPluginInitializer = pluginBuilder.build();

		// build and execute the engine
		Simulation.builder().addPlugin(ActionPluginInitializer.PLUGIN_ID, actionPluginInitializer::init).build().execute();

		// show that all actions were executed
		assertTrue(actionPluginInitializer.allActionsExecuted());
		/////////////////////////////////////////////////////////
		// Case 3 : a resolver subscriber
		/////////////////////////////////////////////////////////

		// add the test resolver
		testResolverId = new SimpleResolverId("test resolver");
		pluginBuilder.addResolver(testResolverId);

		/*
		 * Have the test resolver show that there are initially no subscribers
		 * to test events.
		 */
		pluginBuilder.addResolverActionPlan(testResolverId, new DataManagerActionPlan(0, (c) -> {
			assertFalse(c.subscribersExistForEvent(TestEvent.class));
		}));

		// add a second resolver and have it subscribe to the test event
		ResolverId subscriberResolverId = new SimpleResolverId("subscriber resolver");
		pluginBuilder.addResolver(subscriberResolverId);

		pluginBuilder.addResolverActionPlan(subscriberResolverId, new DataManagerActionPlan(1, (c) -> {
			c.subscribeToEventExecutionPhase(TestEvent.class, (c2, e) -> {
			});
		}));

		// show that the test resolver now sees that there are subscribers
		pluginBuilder.addResolverActionPlan(testResolverId, new DataManagerActionPlan(2, (c) -> {
			assertTrue(c.subscribersExistForEvent(TestEvent.class));
		}));

		// have the second resolver unsubscribe
		pluginBuilder.addResolverActionPlan(subscriberResolverId, new DataManagerActionPlan(3, (c) -> {
			c.unSubscribeToEvent(TestEvent.class);
		}));

		// show that the test resolver now sees that there are no subscribers
		pluginBuilder.addResolverActionPlan(testResolverId, new DataManagerActionPlan(4, (c) -> {
			assertFalse(c.subscribersExistForEvent(TestEvent.class));
		}));

		// build the plugin
		actionPluginInitializer = pluginBuilder.build();

		// build and execute the engine
		Simulation.builder().addPlugin(ActionPluginInitializer.PLUGIN_ID, actionPluginInitializer::init).build().execute();

		// show that all actions were executed
		assertTrue(actionPluginInitializer.allActionsExecuted());
	}

	private static class TestDataView implements DataView {
	}

	@Test
	@UnitTestMethod(name = "publishDataView", args = { DataView.class })
	public void testPublishDataView() {

		ActionPluginInitializer.Builder pluginBuilder = ActionPluginInitializer.builder();

		// add a resolver
		ResolverId resolverId = new SimpleResolverId("resolver");
		pluginBuilder.addResolver(resolverId);

		// have the resolver add a data view
		pluginBuilder.addResolverActionPlan(resolverId, new DataManagerActionPlan(0, (c) -> {

			// show that there is currently no TestDataView available
			Optional<TestDataView> optional = c.getDataView(TestDataView.class);
			assertFalse(optional.isPresent());

			// create a TestDataView and publish it
			TestDataView testDataView1 = new TestDataView();
			c.publishDataView(testDataView1);

			/*
			 * Show that a TestDataView can be retrieved and is equal to the one
			 * that was added.
			 */
			optional = c.getDataView(TestDataView.class);
			assertTrue(optional.isPresent());

			TestDataView testDataView2 = optional.get();

			assertEquals(testDataView1, testDataView2);

		}));

		//precondition tests
		pluginBuilder.addResolverActionPlan(resolverId, new DataManagerActionPlan(0, (c) -> {
			ContractException contractException = assertThrows(ContractException.class, () -> c.publishDataView(null));
			assertEquals(NucleusError.NULL_DATA_VIEW, contractException.getErrorType());
		}));

		// build the plugin
		ActionPluginInitializer actionPluginInitializer = pluginBuilder.build();

		// build and execute the engine
		Simulation.builder().addPlugin(ActionPluginInitializer.PLUGIN_ID, actionPluginInitializer::init).build().execute();

		// show that all the actions executed
		assertTrue(actionPluginInitializer.allActionsExecuted());

	}

	@Test
	@UnitTestMethod(name = "getCurrentResolverId", args = {})
	public void testGetCurrentResolverId() {

		ActionPluginInitializer.Builder pluginBuilder = ActionPluginInitializer.builder();

		// add a few resolvers
		List<ResolverId> resolverIds = new ArrayList<>();
		int resolverCount = 10;
		for (int i = 0; i < resolverCount; i++) {
			ResolverId resolverId = new SimpleResolverId("resolver " + i);
			resolverIds.add(resolverId);
			pluginBuilder.addResolver(resolverId);
		}

		// have the resolvers get their current ids and show they are equal to
		// the expected values
		for (ResolverId resolverId : resolverIds) {
			pluginBuilder.addResolverActionPlan(resolverId, new DataManagerActionPlan(1, (c) -> {
				assertEquals(resolverId, c.getCurrentResolverId());
			}));
		}

		// build the plugin
		ActionPluginInitializer actionPluginInitializer = pluginBuilder.build();

		// build and execute the engine
		Simulation.builder().addPlugin(ActionPluginInitializer.PLUGIN_ID, actionPluginInitializer::init).build().execute();

		// show that all actions were executed
		assertTrue(actionPluginInitializer.allActionsExecuted());

	}
}
