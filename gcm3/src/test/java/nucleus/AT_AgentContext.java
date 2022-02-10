package nucleus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

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
import nucleus.testsupport.actionplugin.DataManagerActionPlan;
import util.ContractError;
import util.ContractException;
import util.MultiKey;
import util.MutableBoolean;
import util.annotations.UnitTest;
import util.annotations.UnitTestMethod;

/**
 * 
 * Test for the implementation of AgentContext by the nucleus Engine
 * 
 * @author Shawn Hatch
 *
 */
@UnitTest(target = AgentContext.class)
public class AT_AgentContext {

	private static class AddPlanDataView implements DataView {
		public boolean planExecuted;
	}

	private static class TestEvent implements Event {

	}

	private static class TestObservationEvent implements Event {

	}

	

	/**
	 * Tests {@link AgentContext#addPlan(Consumer, double)
	 */
	@Test
	@UnitTestMethod(name = "addPlan", args = { Consumer.class, double.class })
	public void testAddPlan() {
		ActionPluginInitializer.Builder pluginBuilder = ActionPluginInitializer.builder();

		// add a data view that exists to show that the plan we add is actually
		// executed and thus the addition of the plan must have succeeded
		AddPlanDataView addPlanDataView = new AddPlanDataView();
		pluginBuilder.addDataView(addPlanDataView);

		// ensure that the test agent will be created
		pluginBuilder.addAgent("Alpha");

		// test preconditions
		pluginBuilder.addAgentActionPlan("Alpha", new AgentActionPlan(1, (context) -> {
			ContractException contractException = assertThrows(ContractException.class, () -> context.addPlan(null, 1));
			assertEquals(NucleusError.NULL_PLAN, contractException.getErrorType());

			contractException = assertThrows(ContractException.class, () -> context.addPlan((c) -> {
			}, 0));
			assertEquals(NucleusError.PAST_PLANNING_TIME, contractException.getErrorType());

		}));

		// have the added test agent add a plan that we can later show was
		// executed
		pluginBuilder.addAgentActionPlan("Alpha", new AgentActionPlan(2, (context) -> {
			context.addPlan((c) -> {
				addPlanDataView.planExecuted = true;
			}, 100);
		}));

		// build the plugin
		ActionPluginInitializer actionPluginInitializer = pluginBuilder.build();

		// run the simulation
		Simulation.builder().addPlugin(ActionPluginInitializer.PLUGIN_ID,actionPluginInitializer::init).build().execute();

		// show that the plan that was added by the action plan was executed and
		// thus the addPlan invocation functioned correctly
		assertTrue(addPlanDataView.planExecuted);

		// show that the action plans got executed
		assertTrue(actionPluginInitializer.allActionsExecuted());
	}

	/**
	 * Tests {@link AgentContext#addPlan(Consumer, double, Object)
	 */
	@Test
	@UnitTestMethod(name = "addPlan", args = { Consumer.class, double.class, Object.class })
	public void testAddPlanWithKey() {
		ActionPluginInitializer.Builder pluginBuilder = ActionPluginInitializer.builder();

		// ensure that the test agent will be created
		pluginBuilder.addAgent("Alpha");

		// test preconditions
		pluginBuilder.addAgentActionPlan("Alpha", new AgentActionPlan(1, (context) -> {
			Object key = new Object();

			double scheduledTime = context.getTime() + 1;

			ContractException contractException = assertThrows(ContractException.class, () -> context.addPlan(null, scheduledTime, key));
			assertEquals(NucleusError.NULL_PLAN, contractException.getErrorType());

			contractException = assertThrows(ContractException.class, () -> context.addPlan((c) -> {
			}, 0, key));
			assertEquals(NucleusError.PAST_PLANNING_TIME, contractException.getErrorType());

			contractException = assertThrows(ContractException.class, () -> context.addPlan((c) -> {
			}, scheduledTime, null));
			assertEquals(NucleusError.NULL_PLAN_KEY, contractException.getErrorType());

			context.addPlan((c) -> {
			}, scheduledTime, key);

			contractException = assertThrows(ContractException.class, () -> context.addPlan((c) -> {
			}, scheduledTime, key));
			assertEquals(NucleusError.DUPLICATE_PLAN_KEY, contractException.getErrorType());

		}));

		// have the added test agent add a plan that can be retrieved and thus
		// was added successfully
		pluginBuilder.addAgentActionPlan("Alpha", new AgentActionPlan(2, (context) -> {
			Object key = new Object();
			assertFalse(context.getPlan(key).isPresent());
			context.addPlan((c) -> {
			}, 100, key);
			assertTrue(context.getPlan(key).isPresent());
		}));

		// build the plugin
		ActionPluginInitializer actionPluginInitializer = pluginBuilder.build();

		// run the simulation
		Simulation.builder().addPlugin(ActionPluginInitializer.PLUGIN_ID,actionPluginInitializer::init).build().execute();

		// show that the action plans got executed
		assertTrue(actionPluginInitializer.allActionsExecuted());
	}

	/**
	 * Tests {@link AgentContext#getPlan(Object)
	 */
	@Test
	@UnitTestMethod(name = "getPlan", args = { Object.class })
	public void testGetPlan() {
		ActionPluginInitializer.Builder pluginBuilder = ActionPluginInitializer.builder();

		// ensure that the test agent will be created
		pluginBuilder.addAgent("Alpha");

		// test preconditions
		pluginBuilder.addAgentActionPlan("Alpha", new AgentActionPlan(1, (context) -> {
			ContractException contractException = assertThrows(ContractException.class, () -> context.getPlan(null));
			assertEquals(NucleusError.NULL_PLAN_KEY, contractException.getErrorType());
		}));

		// have the added test agent add a plan that can be retrieved and thus
		// was added successfully
		pluginBuilder.addAgentActionPlan("Alpha", new AgentActionPlan(2, (context) -> {
			Object key = new Object();
			assertFalse(context.getPlan(key).isPresent());
			context.addPlan((c) -> {
			}, 100, key);
			assertTrue(context.getPlan(key).isPresent());
		}));

		// build the plugin
		ActionPluginInitializer actionPluginInitializer = pluginBuilder.build();

		// run the simulation
		Simulation.builder().addPlugin(ActionPluginInitializer.PLUGIN_ID,actionPluginInitializer::init).build().execute();

		// show that the action plans got executed
		assertTrue(actionPluginInitializer.allActionsExecuted());
	}

	/**
	 * Tests {@link AgentContext#getPlanTime(Object)
	 */
	@Test
	@UnitTestMethod(name = "getPlanTime", args = { Object.class })
	public void testGetPlanTime() {
		ActionPluginInitializer.Builder pluginBuilder = ActionPluginInitializer.builder();

		// ensure that the test agent will be created
		pluginBuilder.addAgent("Alpha");

		// test preconditions
		pluginBuilder.addAgentActionPlan("Alpha", new AgentActionPlan(1, (context) -> {
			ContractException contractException = assertThrows(ContractException.class, () -> context.getPlanTime(null));
			assertEquals(NucleusError.NULL_PLAN_KEY, contractException.getErrorType());
		}));

		// have the added test agent add a plan and show that the plan time is
		// as
		// expected
		pluginBuilder.addAgentActionPlan("Alpha", new AgentActionPlan(2, (context) -> {
			Object key = new Object();
			assertFalse(context.getPlanTime(key).isPresent());
			double expectedPlanTime = 100;
			context.addPlan((c) -> {
			}, expectedPlanTime, key);
			assertTrue(context.getPlanTime(key).isPresent());
			Double actualPlanTime = context.getPlanTime(key).get();
			assertEquals(expectedPlanTime, actualPlanTime, 0);
		}));
		// build the plugin
		ActionPluginInitializer actionPluginInitializer = pluginBuilder.build();
		// run the simulation
		Simulation.builder().addPlugin(ActionPluginInitializer.PLUGIN_ID,actionPluginInitializer::init).build().execute();

		// show that the action plans got executed
		assertTrue(actionPluginInitializer.allActionsExecuted());
	}

	/**
	 * Tests {@link AgentContext#removePlan(Object)
	 */
	@Test
	@UnitTestMethod(name = "removePlan", args = { Object.class })
	public void testRemovePlan() {

		ActionPluginInitializer.Builder pluginBuilder = ActionPluginInitializer.builder();

		// ensure that the test agent will be created
		pluginBuilder.addAgent("Alpha");

		// test preconditions
		pluginBuilder.addAgentActionPlan("Alpha", new AgentActionPlan(1, (context) -> {
			ContractException contractException = assertThrows(ContractException.class, () -> context.removePlan(null));
			assertEquals(NucleusError.NULL_PLAN_KEY, contractException.getErrorType());
		}));

		Object key = new Object();

		// have the added test agent add a plan
		pluginBuilder.addAgentActionPlan("Alpha", new AgentActionPlan(2, (context) -> {
			context.addPlan((c) -> {
				fail();
			}, 100, key);
		}));

		// have the test agent remove the plan and show the plan no longer
		// exists
		pluginBuilder.addAgentActionPlan("Alpha", new AgentActionPlan(3, (context) -> {
			assertTrue(context.getPlan(key).isPresent());

			context.removePlan(key);

			assertFalse(context.getPlan(key).isPresent());

		}));

		// build the plugin
		ActionPluginInitializer actionPluginInitializer = pluginBuilder.build();

		// run the simulation
		Simulation.builder().addPlugin(ActionPluginInitializer.PLUGIN_ID,actionPluginInitializer::init).build().execute();

		// show that the action plans got executed
		assertTrue(actionPluginInitializer.allActionsExecuted());
	}

	/**
	 * Tests {@link AgentContext#getPlanKeys()
	 */
	@Test
	@UnitTestMethod(name = "getPlanKeys", args = {})
	public void testGetPlanKeys() {

		ActionPluginInitializer.Builder pluginBuilder = ActionPluginInitializer.builder();

		// ensure that the test agent will be created
		pluginBuilder.addAgent("Alpha");

		// There are no precondition tests
		Set<Object> expectedKeys = new LinkedHashSet<>();
		int keyCount = 20;
		for (int i = 0; i < keyCount; i++) {
			expectedKeys.add(new Object());
		}

		// have the test agent add some plans
		pluginBuilder.addAgentActionPlan("Alpha", new AgentActionPlan(1, (context) -> {
			for (Object key : expectedKeys) {
				context.addPlan((c) -> {
				}, 100, key);
			}

			Set<Object> actualKeys = context.getPlanKeys().stream().collect(Collectors.toCollection(LinkedHashSet::new));
			assertEquals(expectedKeys, actualKeys);

		}));

		// build the plugin
		ActionPluginInitializer actionPluginInitializer = pluginBuilder.build();

		// run the simulation
		Simulation.builder().addPlugin(ActionPluginInitializer.PLUGIN_ID,actionPluginInitializer::init).build().execute();

		// show that the action plans got executed
		assertTrue(actionPluginInitializer.allActionsExecuted());
	}

	/**
	 * Tests {@link AgentContext#resolveEvent(Event)
	 */
	@Test
	@UnitTestMethod(name = "resolveEvent", args = { Event.class })
	public void testResolveEvent() {

		ActionPluginInitializer.Builder pluginBuilder = ActionPluginInitializer.builder();

		MutableBoolean eventResolved = new MutableBoolean();

		// Add a resolver for a test event type
		ResolverId resolverId = new SimpleResolverId("Test Resolver");
		pluginBuilder.addResolver(resolverId);

		// Have the resolver subscribe to test event and then set the
		// eventResolved to true
		pluginBuilder.addResolverActionPlan(resolverId, new DataManagerActionPlan(0, (c) -> {
			c.subscribeToEventExecutionPhase(TestEvent.class, (c2, e) -> {
				eventResolved.setValue(true);
			});
		}));

		// create the test agent
		pluginBuilder.addAgent("Alpha");

		// have the test agent resolve an event and show it was resolved
		pluginBuilder.addAgentActionPlan("Alpha", new AgentActionPlan(2, (context) -> {
			context.resolveEvent(new TestEvent());
			assertTrue(eventResolved.getValue());
		}));

		// precondition tests
		pluginBuilder.addAgentActionPlan("Alpha", new AgentActionPlan(1, (context) -> {
			ContractException contractException = assertThrows(ContractException.class, () -> context.resolveEvent(null));
			assertEquals(NucleusError.NULL_EVENT, contractException.getErrorType());
		}));

		// build the plugin
		ActionPluginInitializer actionPluginInitializer = pluginBuilder.build();

		// run the simulation
		Simulation.builder().addPlugin(ActionPluginInitializer.PLUGIN_ID,actionPluginInitializer::init).build().execute();

		// show that the action plans got executed
		assertTrue(actionPluginInitializer.allActionsExecuted());
	}

	/**
	 * Tests {@link AgentContext#getCurrentAgentId()
	 */
	@Test
	@UnitTestMethod(name = "getCurrentAgentId", args = {})
	public void testGetCurrentAgentId() {

		ActionPluginInitializer.Builder pluginBuilder = ActionPluginInitializer.builder();

		// add three test agents
		pluginBuilder.addAgent("Alpha");
		pluginBuilder.addAgent("Beta");
		pluginBuilder.addAgent("Gamma");

		double testTime = 1;
		// there are no precondition tests

		Map<Object, AgentId> map = new LinkedHashMap<>();

		// have the test agents test their own agent ids
		pluginBuilder.addAgentActionPlan("Alpha", new AgentActionPlan(testTime++, (context) -> {
			AgentId agentId = context.getCurrentAgentId();
			assertNotNull(agentId);
			map.put("Alpha", context.getCurrentAgentId());
		}));

		pluginBuilder.addAgentActionPlan("Beta", new AgentActionPlan(testTime++, (context) -> {
			AgentId agentId = context.getCurrentAgentId();
			assertNotNull(agentId);
			map.put("Beta", context.getCurrentAgentId());
		}));

		pluginBuilder.addAgentActionPlan("Gamma", new AgentActionPlan(testTime++, (context) -> {
			AgentId agentId = context.getCurrentAgentId();
			assertNotNull(agentId);
			map.put("Gamma", context.getCurrentAgentId());
		}));

		pluginBuilder.addAgentActionPlan("Alpha", new AgentActionPlan(testTime++, (context) -> {
			AgentId agentId = context.getCurrentAgentId();
			assertNotNull(agentId);
			assertEquals(map.get("Alpha"), agentId);
		}));

		pluginBuilder.addAgentActionPlan("Beta", new AgentActionPlan(testTime++, (context) -> {
			AgentId agentId = context.getCurrentAgentId();
			assertNotNull(agentId);
			assertEquals(map.get("Beta"), agentId);
		}));

		pluginBuilder.addAgentActionPlan("Gamma", new AgentActionPlan(testTime++, (context) -> {
			AgentId agentId = context.getCurrentAgentId();
			assertNotNull(agentId);
			assertEquals(map.get("Gamma"), agentId);
		}));

		// build the plugin
		ActionPluginInitializer actionPluginInitializer = pluginBuilder.build();

		// run the simulation
		Simulation.builder().addPlugin(ActionPluginInitializer.PLUGIN_ID,actionPluginInitializer::init).build().execute();

		// show that the action plans got executed
		assertTrue(actionPluginInitializer.allActionsExecuted());
	}

	/**
	 * Tests {@link AgentContext#agentExists(AgentId)
	 */
	@Test
	@UnitTestMethod(name = "agentExists", args = { AgentId.class })
	public void testAgentExists() {

		ActionPluginInitializer.Builder pluginBuilder = ActionPluginInitializer.builder();

		// ensure that the test agent will be created
		pluginBuilder.addAgent("Alpha");

		double testTime = 1;
		// there are no precondition tests

		// have the test agent show it exists and that other agents do not
		pluginBuilder.addAgentActionPlan("Alpha", new AgentActionPlan(testTime++, (context) -> {
			assertTrue(context.agentExists(new AgentId(0)));
			assertFalse(context.agentExists(new AgentId(1)));
			assertFalse(context.agentExists(new AgentId(2)));
		}));

		// build the plugin
		ActionPluginInitializer actionPluginInitializer = pluginBuilder.build();

		// run the simulation
		Simulation.builder().addPlugin(ActionPluginInitializer.PLUGIN_ID,actionPluginInitializer::init).build().execute();

		// show that the action plans got executed
		assertTrue(actionPluginInitializer.allActionsExecuted());
	}

	/**
	 * Tests {@link AgentContext#halt()
	 */
	@Test
	@UnitTestMethod(name = "halt", args = {})
	public void testHalt() {

		ActionPluginInitializer.Builder pluginBuilder = ActionPluginInitializer.builder();

		// add a test agent
		pluginBuilder.addAgent("Alpha");

		// there are no precondition tests

		// have the test agent execute several tasks, with one of the tasks
		// halting the simulation
		AgentActionPlan actionPlan1 = new AgentActionPlan(1, (context) -> {
		});
		pluginBuilder.addAgentActionPlan("Alpha", actionPlan1);

		AgentActionPlan actionPlan2 = new AgentActionPlan(2, (context) -> {
		});
		pluginBuilder.addAgentActionPlan("Alpha", actionPlan2);

		AgentActionPlan actionPlan3 = new AgentActionPlan(3, (context) -> {
			context.halt();
		});
		pluginBuilder.addAgentActionPlan("Alpha", actionPlan3);

		AgentActionPlan actionPlan4 = new AgentActionPlan(4, (context) -> {
		});
		pluginBuilder.addAgentActionPlan("Alpha", actionPlan4);

		AgentActionPlan actionPlan5 = new AgentActionPlan(5, (context) -> {
		});
		pluginBuilder.addAgentActionPlan("Alpha", actionPlan5);

		// build the plugin
		ActionPluginInitializer actionPluginInitializer = pluginBuilder.build();

		// run the simulation
		Simulation.builder().addPlugin(ActionPluginInitializer.PLUGIN_ID,actionPluginInitializer::init).build().execute();

		// show that the plans that were scheduled after the halt did not
		// execute
		assertTrue(actionPlan1.executed());
		assertTrue(actionPlan2.executed());
		assertTrue(actionPlan3.executed());
		assertFalse(actionPlan4.executed());
		assertFalse(actionPlan5.executed());

	}

	private static enum Local_Labeler_ID implements EventLabelerId {
		TEST_LABELER_ID, OBSERVATION_TEST_LABELER_ID, DATA_CHANGE
	}

	private static class TestObservationEventLabel implements EventLabel<TestObservationEvent> {

		@Override
		public Class<TestObservationEvent> getEventClass() {
			return TestObservationEvent.class;
		}

		@Override
		public EventLabelerId getLabelerId() {
			return Local_Labeler_ID.OBSERVATION_TEST_LABELER_ID;
		}

		@Override
		public Object getPrimaryKeyValue() {
			return TestObservationEvent.class;
		}

		@Override
		public int hashCode() {
			return 0;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			}
			if (!(obj instanceof TestObservationEventLabel)) {
				return false;
			}
			return true;
		}

	}

	private static class TestEventLabel implements EventLabel<TestEvent> {

		@Override
		public Class<TestEvent> getEventClass() {
			return TestEvent.class;
		}

		@Override
		public EventLabelerId getLabelerId() {
			return Local_Labeler_ID.TEST_LABELER_ID;
		}

		@Override
		public Object getPrimaryKeyValue() {
			return TestEvent.class;
		}

		@Override
		public int hashCode() {
			return 0;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			}
			if (!(obj instanceof TestObservationEventLabel)) {
				return false;
			}
			return true;
		}
	}

	private static class NullEventClass_TestEventLabel extends TestEventLabel {
		@Override
		public Class<TestEvent> getEventClass() {
			return null;
		}
	}

	private static class NullLabelerId_TestEventLabel extends TestEventLabel {
		@Override
		public EventLabelerId getLabelerId() {
			return null;
		}
	}

	private static class UnknownLabeler_TestEventLabel extends TestEventLabel {
		@Override
		public EventLabelerId getLabelerId() {
			return new EventLabelerId() {
			};
		}
	}

	private static class NullPrimaryKey_TestEventLabel extends TestEventLabel {
		@Override
		public Object getPrimaryKeyValue() {
			return null;
		}
	}

	private static enum DatumType {
		TYPE_1, TYPE_2
	}

	private static class DataChangeObservationEvent implements Event {
		private final DatumType datumType;
		private final int value;

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((datumType == null) ? 0 : datumType.hashCode());
			result = prime * result + value;
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			}
			if (!(obj instanceof DataChangeObservationEvent)) {
				return false;
			}
			DataChangeObservationEvent other = (DataChangeObservationEvent) obj;
			if (datumType != other.datumType) {
				return false;
			}
			if (value != other.value) {
				return false;
			}
			return true;
		}

		public DatumType getDatumType() {
			return datumType;
		}

		public int getValue() {
			return value;
		}

		public DataChangeObservationEvent(DatumType datumType, int value) {
			super();
			this.datumType = datumType;
			this.value = value;
		}

		@Override
		public Object getPrimaryKeyValue() {
			return datumType;
		}

		@Override
		public String toString() {
			StringBuilder builder = new StringBuilder();
			builder.append("DataChangeObservationEvent [datumType=");
			builder.append(datumType);
			builder.append(", value=");
			builder.append(value);
			builder.append("]");
			return builder.toString();
		}
	}

//	

	/**
	 * Tests {@link AgentContext#subscribe(EventLabel, AgentEventConsumer)
	 */
	@Test
	@UnitTestMethod(name = "subscribe", args = { EventLabel.class, AgentEventConsumer.class })
	public void testSubscribe() {
		ActionPluginInitializer.Builder pluginBuilder = ActionPluginInitializer.builder();

		pluginBuilder.addEventLabeler(new SimpleEventLabeler<TestEvent>(Local_Labeler_ID.TEST_LABELER_ID, TestEvent.class, (c, e) -> {
			return new TestEventLabel();
		}));

		pluginBuilder.addEventLabeler(getEventLabelerForDataChangeObservation());

		// Add a resolver that will generate DataChangeObservationEvents

		ResolverId resolverId = new SimpleResolverId("resolver");
		pluginBuilder.addResolver(resolverId);

		/*
		 * Have the resolver generate multiple DataChangeObservation events.
		 * Some will match the agent's subscription, some won't.
		 */
		pluginBuilder.addResolverActionPlan(resolverId, new DataManagerActionPlan(10, (c) -> {
			c.resolveEvent(new DataChangeObservationEvent(DatumType.TYPE_1, 0));
			c.resolveEvent(new DataChangeObservationEvent(DatumType.TYPE_2, 5));
			c.resolveEvent(new DataChangeObservationEvent(DatumType.TYPE_1, 20));
			c.resolveEvent(new DataChangeObservationEvent(DatumType.TYPE_2, 0));
			c.resolveEvent(new DataChangeObservationEvent(DatumType.TYPE_1, 5));
			c.resolveEvent(new DataChangeObservationEvent(DatumType.TYPE_2, 25));
			c.resolveEvent(new DataChangeObservationEvent(DatumType.TYPE_1, 38));
			c.resolveEvent(new DataChangeObservationEvent(DatumType.TYPE_2, 234));
		}));

		// create the test agent
		pluginBuilder.addAgent("Alpha");

		// precondition tests
		pluginBuilder.addAgentActionPlan("Alpha", new AgentActionPlan(0, (context) -> {

			ContractException contractException = assertThrows(ContractException.class, () -> context.subscribe(null, (c, e) -> {
			}));
			assertEquals(NucleusError.NULL_EVENT_LABEL, contractException.getErrorType());

			contractException = assertThrows(ContractException.class, () -> context.subscribe(new TestEventLabel(), null));
			assertEquals(NucleusError.NULL_EVENT_CONSUMER, contractException.getErrorType());

			contractException = assertThrows(ContractException.class, () -> context.subscribe(new NullEventClass_TestEventLabel(), (c, e) -> {
			}));
			assertEquals(NucleusError.NULL_EVENT_CLASS_IN_EVENT_LABEL, contractException.getErrorType());

			contractException = assertThrows(ContractException.class, () -> context.subscribe(new NullLabelerId_TestEventLabel(), (c, e) -> {
			}));
			assertEquals(NucleusError.NULL_LABELER_ID_IN_EVENT_LABEL, contractException.getErrorType());

			contractException = assertThrows(ContractException.class, () -> context.subscribe(new UnknownLabeler_TestEventLabel(), (c, e) -> {
			}));
			assertEquals(NucleusError.UNKNOWN_EVENT_LABELER, contractException.getErrorType());

			contractException = assertThrows(ContractException.class, () -> context.subscribe(new NullPrimaryKey_TestEventLabel(), (c, e) -> {
			}));
			assertEquals(NucleusError.NULL_PRIMARY_KEY_VALUE, contractException.getErrorType());

		}));

		Set<MultiKey> receivedEvents = new LinkedHashSet<>();

		/*
		 * Have the test agent subscribe to DataChangeObservationEvent filtered
		 * to DatumType.Type_1 and having a high value. For each event received,
		 * the agent will record the event as a multiKey.
		 */
		pluginBuilder.addAgentActionPlan("Alpha", new AgentActionPlan(1, (context) -> {
			context.subscribe(getEventLabelByDatumAndValue(DatumType.TYPE_1, ValueType.HIGH), (c, e) -> {
				receivedEvents.add(new MultiKey(c.getTime(), e));
			});
		}));

		// build the plugin
		ActionPluginInitializer actionPluginInitializer = pluginBuilder.build();

		// run the simulation
		Simulation.builder().addPlugin(ActionPluginInitializer.PLUGIN_ID,actionPluginInitializer::init).build().execute();

		// show that the action plans got executed
		assertTrue(actionPluginInitializer.allActionsExecuted());

		// show that all and only the observations corresponding to the
		// subscribed event label were delivered to the Alpha agent
		Set<MultiKey> expectedEvents = new LinkedHashSet<>();
		expectedEvents.add(new MultiKey(10.0, new DataChangeObservationEvent(DatumType.TYPE_1, 20)));
		expectedEvents.add(new MultiKey(10.0, new DataChangeObservationEvent(DatumType.TYPE_1, 38)));

		assertEquals(expectedEvents, receivedEvents);

	}

	private static enum ValueType {
		HIGH, LOW
	}

	public static EventLabel<DataChangeObservationEvent> getEventLabelByDatumAndValue(DatumType datumType, ValueType valueType) {
		return new MultiKeyEventLabel<>(datumType, Local_Labeler_ID.DATA_CHANGE, DataChangeObservationEvent.class, datumType, valueType);
	}

	public static EventLabeler<DataChangeObservationEvent> getEventLabelerForDataChangeObservation() {
		return new SimpleEventLabeler<>(Local_Labeler_ID.DATA_CHANGE, DataChangeObservationEvent.class, (context, event) -> {
			ValueType valueType = ValueType.LOW;
			if (event.getValue() > 10) {
				valueType = ValueType.HIGH;
			}
			return new MultiKeyEventLabel<>(event.getDatumType(), Local_Labeler_ID.DATA_CHANGE, DataChangeObservationEvent.class, event.getDatumType(), valueType);
		});
	}

	/**
	 * Tests {@link AgentContext#unsubscribe(EventLabel)
	 */
	@Test
	@UnitTestMethod(name = "unsubscribeToEvent", args = { EventLabel.class })
	public void testUnsubscribeToEvent() {

		ActionPluginInitializer.Builder pluginBuilder = ActionPluginInitializer.builder();

		// Generate an event label that will match all TestEvents. This will be
		// used throughout.
		MultiKeyEventLabel<TestEvent> eventLabel = new MultiKeyEventLabel<>(TestEvent.class, Local_Labeler_ID.TEST_LABELER_ID, TestEvent.class);

		/*
		 * Add a corresponding event labeler -- we want all TestEvents to be
		 * passed to all agent subscribers so that we can demonstrate that
		 * unsubscribing works without complicating the test with filtering
		 */
		pluginBuilder.addEventLabeler(new SimpleEventLabeler<TestEvent>(Local_Labeler_ID.TEST_LABELER_ID, TestEvent.class, (c, e) -> {
			return eventLabel;
		}));

		// create a test resolver
		ResolverId resolverId = new SimpleResolverId("Test Resolver");
		pluginBuilder.addResolver(resolverId);

		// create some times for the resolver to generate events
		List<Double> eventGenerationTimes = new ArrayList<>();
		eventGenerationTimes.add(1.0);
		eventGenerationTimes.add(2.0);
		eventGenerationTimes.add(3.0);
		eventGenerationTimes.add(4.0);
		eventGenerationTimes.add(5.0);
		eventGenerationTimes.add(6.0);
		eventGenerationTimes.add(7.0);
		eventGenerationTimes.add(8.0);
		eventGenerationTimes.add(9.0);

		/*
		 * At time 0, have the test resolver generate plans to generate events
		 * at various times
		 */

		pluginBuilder.addResolverActionPlan(resolverId, new DataManagerActionPlan(0, (c) -> {
			for (Double time : eventGenerationTimes) {
				c.addPlan((c2) -> {
					c2.resolveEvent(new TestEvent());
				}, time);
			}
		}));

		/*
		 * Create three test agents that will subscribe and unsubscribe to
		 * TestEvents at various times
		 */
		pluginBuilder.addAgent("Alpha");
		pluginBuilder.addAgent("Beta");
		pluginBuilder.addAgent("Gamma");

		// precondition tests -- have the first agent test all the precondition exceptions
		pluginBuilder.addAgentActionPlan("Alpha", new AgentActionPlan(0, (context) -> {

			// if the EventLabel is null
			ContractException contractException = assertThrows(ContractException.class, () -> context.unsubscribe(null));
			assertEquals(NucleusError.NULL_EVENT_LABEL, contractException.getErrorType());

			// if the event class in the event label is null
			contractException = assertThrows(ContractException.class, () -> context.subscribe(new NullEventClass_TestEventLabel(), (c, e) -> {
			}));
			assertEquals(NucleusError.NULL_EVENT_CLASS_IN_EVENT_LABEL, contractException.getErrorType());

			// if the event labeler id in the event label is null
			contractException = assertThrows(ContractException.class, () -> context.subscribe(new NullLabelerId_TestEventLabel(), (c, e) -> {
			}));
			assertEquals(NucleusError.NULL_LABELER_ID_IN_EVENT_LABEL, contractException.getErrorType());

			// if the event labeler id in the event label cannot be resolved to
			// a registered event labeler
			contractException = assertThrows(ContractException.class, () -> context.subscribe(new UnknownLabeler_TestEventLabel(), (c, e) -> {
			}));
			assertEquals(NucleusError.UNKNOWN_EVENT_LABELER, contractException.getErrorType());

			// if the event label has a null primary key
			contractException = assertThrows(ContractException.class, () -> context.subscribe(new NullPrimaryKey_TestEventLabel(), (c, e) -> {
			}));
			assertEquals(NucleusError.NULL_PRIMARY_KEY_VALUE, contractException.getErrorType());

		}));

		//create a container for the events that are received by the three agents
		Set<MultiKey> recievedEvents = new LinkedHashSet<>();

		// have the Alpha agent subscribe to the Test Event at time 0
		pluginBuilder.addAgentActionPlan("Alpha", new AgentActionPlan(0, (context) -> {
			context.subscribe(eventLabel, (c, e) -> {
				recievedEvents.add(new MultiKey("Alpha", c.getTime()));
			});
		}));

		// have the Alpha agent unsubscribe to the Test Event at time 5
		pluginBuilder.addAgentActionPlan("Alpha", new AgentActionPlan(5, (context) -> {
			context.unsubscribe(eventLabel);
		}));

		// have the Beta agent subscribe to the Test Event at time 4
		pluginBuilder.addAgentActionPlan("Beta", new AgentActionPlan(4, (context) -> {
			context.subscribe(eventLabel, (c, e) -> {
				recievedEvents.add(new MultiKey("Beta", c.getTime()));
			});
		}));

		// have the Beta agent unsubscribe to the Test Event at time 8
		pluginBuilder.addAgentActionPlan("Beta", new AgentActionPlan(8, (context) -> {
			context.unsubscribe(eventLabel);
		}));

		// have the Gamma agent subscribe to the Test Event at time 6
		pluginBuilder.addAgentActionPlan("Gamma", new AgentActionPlan(6, (context) -> {
			context.subscribe(eventLabel, (c, e) -> {
				recievedEvents.add(new MultiKey("Gamma", c.getTime()));
			});
		}));

		// build the plugin
		ActionPluginInitializer actionPluginInitializer = pluginBuilder.build();

		// run the simulation
		Simulation.builder().addPlugin(ActionPluginInitializer.PLUGIN_ID,actionPluginInitializer::init).build().execute();

		// show that all action plans were executed
		assertTrue(actionPluginInitializer.allActionsExecuted());

		// show that all and only the observations corresponding to the
		// subscribed event label were delivered to the agents
		Set<MultiKey> expectedEvents = new LinkedHashSet<>();
		expectedEvents.add(new MultiKey("Alpha", 1.0));
		expectedEvents.add(new MultiKey("Alpha", 2.0));
		expectedEvents.add(new MultiKey("Alpha", 3.0));
		expectedEvents.add(new MultiKey("Alpha", 4.0));
		expectedEvents.add(new MultiKey("Alpha", 5.0));
		expectedEvents.add(new MultiKey("Beta", 5.0));
		expectedEvents.add(new MultiKey("Beta", 6.0));
		expectedEvents.add(new MultiKey("Beta", 7.0));
		expectedEvents.add(new MultiKey("Beta", 8.0));
		expectedEvents.add(new MultiKey("Gamma", 7.0));
		expectedEvents.add(new MultiKey("Gamma", 8.0));
		expectedEvents.add(new MultiKey("Gamma", 9.0));

		// show that the expected and actual event records are the same
		assertEquals(expectedEvents, recievedEvents);

	}

	@Test
	@UnitTestMethod(name = "releaseOutput", args = {})
	public void testReleaseOutput() {

		// begin building the action plugin
		ActionPluginInitializer.Builder pluginBuilder = ActionPluginInitializer.builder();

		// ensure that the test agent will be created
		pluginBuilder.addAgent("Alpha");

		// set up the expected output
		Set<Object> expectedOutput = new LinkedHashSet<>();
		expectedOutput.add("the sly fox");
		expectedOutput.add(15);
		expectedOutput.add("the lazy, brown dog");
		expectedOutput.add(45.34513453);

		// have the agent release the output
		pluginBuilder.addAgentActionPlan("Alpha", new AgentActionPlan(1, (c) -> {
			for (Object outputValue : expectedOutput) {
				c.releaseOutput(outputValue);
			}
		}));

		// build the plugin
		ActionPluginInitializer actionPluginInitializer = pluginBuilder.build();

		Set<Object> actualOutput = new LinkedHashSet<>();

		/*
		 * Add an output consumer that will place the output into the
		 * actualOutput set above and then execute the simulation
		 */
		Simulation	.builder()//
				.addPlugin(ActionPluginInitializer.PLUGIN_ID,actionPluginInitializer::init)//
				.setOutputConsumer((o) -> actualOutput.add(o))//
				.build()//
				.execute();//

		// show that the agent action was executed
		assertTrue(actionPluginInitializer.allActionsExecuted());

		// show that the output matches expectations
		assertEquals(expectedOutput, actualOutput);

	}

	/*
	 * DataView implementor to support tests
	 */
	private static class LocalDataView1 implements DataView {
	}

	/*
	 * DataView implementor to support tests
	 */
	private static class LocalDataView2 implements DataView {
	}

	@Test
	@UnitTestMethod(name = "getDataView", args = { Class.class })

	public void testGetDataView() {
		// create the action plugin builder
		ActionPluginInitializer.Builder pluginBuilder = ActionPluginInitializer.builder();

		// mark Agent1 for creation
		pluginBuilder.addAgent("Agent1");

		// create a data view for the agent to search for
		LocalDataView1 localDataView1 = new LocalDataView1();
		pluginBuilder.addDataView(localDataView1);

		/*
		 * Have the agent search for the data view that was added to the
		 * simulation. Show that there is no instance of the second type of data
		 * view present.
		 */
		pluginBuilder.addAgentActionPlan("Agent1", new AgentActionPlan(4, (c) -> {
			Optional<LocalDataView1> optional1 = c.getDataView(LocalDataView1.class);
			assertTrue(optional1.isPresent());
			assertEquals(localDataView1, optional1.get());

			Optional<LocalDataView2> optional2 = c.getDataView(LocalDataView2.class);
			assertFalse(optional2.isPresent());

		}));

		// build the action plugin
		ActionPluginInitializer actionPluginInitializer = pluginBuilder.build();

		// execute the engine
		Simulation.builder().addPlugin(ActionPluginInitializer.PLUGIN_ID,actionPluginInitializer::init).build().execute();

		// show that the action was executed
		assertTrue(actionPluginInitializer.allActionsExecuted());

	}

	@Test
	@UnitTestMethod(name = "getTime", args = {})

	public void testGetTime() {
		// create the action plugin builder
		ActionPluginInitializer.Builder pluginBuilder = ActionPluginInitializer.builder();

		// mark Agent1 for creation
		pluginBuilder.addAgent("Agent1");

		Set<Double> planTimes = new LinkedHashSet<>();

		planTimes.add(4.6);
		planTimes.add(13.8764);
		planTimes.add(554.345);
		planTimes.add(7.95346);
		planTimes.add(400.234234);
		planTimes.add(3000.12422346);

		/*
		 * Have the agent build plans to check the time in the simulation
		 * against the planning time
		 */
		pluginBuilder.addAgentActionPlan("Agent1", new AgentActionPlan(0, (context1) -> {
			for (Double planTime : planTimes) {
				context1.addPlan((context2) -> {
					assertEquals(planTime.doubleValue(), context2.getTime(), 0);
				}, planTime);
			}
		}));

		// build the action plugin
		ActionPluginInitializer actionPluginInitializer = pluginBuilder.build();

		// execute the engine
		Simulation.builder().addPlugin(ActionPluginInitializer.PLUGIN_ID,actionPluginInitializer::init).build().execute();

		// show that the action was executed
		assertTrue(actionPluginInitializer.allActionsExecuted());
	}

	@Test
	@UnitTestMethod(name = "throwContractException", args = { ContractError.class })
	public void testThrowContractException() {
		// create the action plugin builder
		ActionPluginInitializer.Builder pluginBuilder = ActionPluginInitializer.builder();

		// mark Agent1 for creation
		pluginBuilder.addAgent("Agent1");

		/*
		 * Have the agent throw a contract exception
		 */
		pluginBuilder.addAgentActionPlan("Agent1", new AgentActionPlan(4, (context) -> {
			context.throwContractException(NucleusError.ACCESS_VIOLATION);
		}));

		// build the action plugin
		ActionPluginInitializer actionPluginInitializer = pluginBuilder.build();

		// execute the engine and show that the expected contract exception was
		// thrown
		ContractException contractException = assertThrows(ContractException.class, () -> Simulation.builder().addPlugin(ActionPluginInitializer.PLUGIN_ID,actionPluginInitializer::init).build().execute());
		assertEquals(NucleusError.ACCESS_VIOLATION, contractException.getErrorType());

		// show that the action was executed
		assertTrue(actionPluginInitializer.allActionsExecuted());
	}

	@Test
	@UnitTestMethod(name = "throwContractException", args = { ContractError.class, Object.class })

	public void testThrowContractExceptionWithDetails() {
		// create the action plugin builder
		ActionPluginInitializer.Builder pluginBuilder = ActionPluginInitializer.builder();

		// mark Agent1 for creation
		pluginBuilder.addAgent("Agent1");

		String details = "these are the details for the contract exception";
		/*
		 * Have the agent throw a contract exception
		 */
		pluginBuilder.addAgentActionPlan("Agent1", new AgentActionPlan(4, (context) -> {
			context.throwContractException(NucleusError.ACCESS_VIOLATION, details);
		}));

		// build the action plugin
		ActionPluginInitializer actionPluginInitializer = pluginBuilder.build();

		/*
		 * Execute the engine. Show that the expected contract exception was
		 * thrown and the exceptions message text contains the expected details
		 */
		ContractException contractException = assertThrows(ContractException.class, () -> Simulation.builder().addPlugin(ActionPluginInitializer.PLUGIN_ID,actionPluginInitializer::init).build().execute());
		assertEquals(NucleusError.ACCESS_VIOLATION, contractException.getErrorType());
		contractException.getMessage().contains(details);

		// show that the action was executed
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

		//add an agent
		pluginBuilder.addAgent("observer agent");
		
		

		// have the agent test the preconditions
		pluginBuilder.addAgentActionPlan("observer agent", new AgentActionPlan(0, (c) -> {
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

		
		// have the agent add the event labeler
		pluginBuilder.addAgentActionPlan("observer agent", new AgentActionPlan(1, (c) -> {
			c.addEventLabeler(eventLabeler);
		}));

		/*
		 * Create a container for the agent to record that it received the Test
		 * Event and we can conclude that the event labeler had been properly
		 * added to the simulation.
		 */
		MutableBoolean eventObserved = new MutableBoolean();

		// have the agent observe the test event
		
		pluginBuilder.addAgentActionPlan("observer agent", new AgentActionPlan(2, (c) -> {
			c.subscribe(new MultiKeyEventLabel<>(TestEvent.class, id, TestEvent.class), (c2, e) -> {
				eventObserved.setValue(true);
			});
		}));
		
		// add a resolver
		ResolverId resolverId = new SimpleResolverId("resolver");
		pluginBuilder.addResolver(resolverId);

		// have the resolver create a test event for the agent to observe
		pluginBuilder.addResolverActionPlan(resolverId, new DataManagerActionPlan(3, (c) -> {
			c.resolveEvent(new TestEvent());
		}));

		// build the plugin
		ActionPluginInitializer actionPluginInitializer = pluginBuilder.build();

		// build and execute the engine
		Simulation.builder().addPlugin(ActionPluginInitializer.PLUGIN_ID,actionPluginInitializer::init).build().execute();

		// show that all actions were executed
		assertTrue(actionPluginInitializer.allActionsExecuted());

		/*
		 * Show that the event labeler must have been added to the simulation
		 * since the agent observed the test event
		 */
		assertTrue(eventObserved.getValue());

	}
}