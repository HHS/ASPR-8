package nucleus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

import annotations.UnitTest;
import annotations.UnitTestMethod;
import nucleus.testsupport.testplugin.ScenarioPlanCompletionObserver;
import nucleus.testsupport.testplugin.TestActorPlan;
import nucleus.testsupport.testplugin.TestDataManager;
import nucleus.testsupport.testplugin.TestPlugin;
import nucleus.testsupport.testplugin.TestPluginData;
import nucleus.testsupport.testplugin.TestScenarioReport;
import nucleus.util.ContractException;
import util.MultiKey;
import util.MutableBoolean;

/**
 *
 * Test for the implementation of AgentContext by the nucleus Engine
 *
 * @author Shawn Hatch
 *
 */
@UnitTest(target = ActorContext.class)
public class AT_ActorContext {

	/*
	 * DataView implementor to support tests
	 */
	private static class TestDataManager1 extends TestDataManager {
	}

	/*
	 * DataView implementor to support tests
	 */
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

	private static class DataChangeObservationEvent implements Event {
		private final DatumType datumType;
		private final int value;

		public DataChangeObservationEvent(final DatumType datumType, final int value) {
			super();
			this.datumType = datumType;
			this.value = value;
		}

		@Override
		public boolean equals(final Object obj) {
			if (this == obj) {
				return true;
			}
			if (!(obj instanceof DataChangeObservationEvent)) {
				return false;
			}
			final DataChangeObservationEvent other = (DataChangeObservationEvent) obj;
			if ((datumType != other.datumType) || (value != other.value)) {
				return false;
			}
			return true;
		}

		public DatumType getDatumType() {
			return datumType;
		}

		@Override
		public Object getPrimaryKeyValue() {
			return datumType;
		}

		public int getValue() {
			return value;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = (prime * result) + ((datumType == null) ? 0 : datumType.hashCode());
			result = (prime * result) + value;
			return result;
		}

		@Override
		public String toString() {
			final StringBuilder builder = new StringBuilder();
			builder.append("DataChangeObservationEvent [datumType=");
			builder.append(datumType);
			builder.append(", value=");
			builder.append(value);
			builder.append("]");
			return builder.toString();
		}
	}

	private static enum DatumType {
		TYPE_1, TYPE_2
	}

	private static enum Local_Labeler_ID implements EventLabelerId {
		TEST_LABELER_ID, OBSERVATION_TEST_LABELER_ID, DATA_CHANGE
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

	private static class NullPrimaryKey_TestEventLabel extends TestEventLabel {
		@Override
		public Object getPrimaryKeyValue() {
			return null;
		}
	}

	private static class TestEvent implements Event {

	}

	private static class TestEventLabel implements EventLabel<TestEvent> {

		@Override
		public boolean equals(final Object obj) {
			if (this == obj) {
				return true;
			}
			if (!(obj instanceof TestObservationEventLabel)) {
				return false;
			}
			return true;
		}

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
	}

	/*
	 * Event labeler class designed to possibly not comply with preconditions
	 * required for the adding of event labelers
	 */
	private static class TestEventLabeler implements EventLabeler<TestEvent> {
		private final Class<TestEvent> eventClass;
		private final EventLabelerId eventLabelerId;

		public TestEventLabeler(final Class<TestEvent> eventClass, final EventLabelerId eventLabelerId) {
			this.eventClass = eventClass;
			this.eventLabelerId = eventLabelerId;
		}

		@Override
		public Class<TestEvent> getEventClass() {
			return eventClass;
		}

		@Override
		public EventLabel<TestEvent> getEventLabel(final SimulationContext context, final TestEvent event) {
			return new MultiKeyEventLabel<>(TestEvent.class, eventLabelerId, TestEvent.class);
		}

		@Override
		public EventLabelerId getId() {
			return eventLabelerId;
		}

	}

	private static class TestObservationEvent implements Event {

	}

	private static class TestObservationEventLabel implements EventLabel<TestObservationEvent> {

		@Override
		public boolean equals(final Object obj) {
			if (this == obj) {
				return true;
			}
			if (!(obj instanceof TestObservationEventLabel)) {
				return false;
			}
			return true;
		}

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

	}

	private static class UnknownLabeler_TestEventLabel extends TestEventLabel {
		@Override
		public EventLabelerId getLabelerId() {
			return new EventLabelerId() {
			};
		}
	}

	private static enum ValueType {
		HIGH, LOW
	}

	private static EventLabel<DataChangeObservationEvent> getEventLabelByDatumAndValue(final DatumType datumType, final ValueType valueType) {
		return new MultiKeyEventLabel<>(datumType, Local_Labeler_ID.DATA_CHANGE, DataChangeObservationEvent.class, datumType, valueType);
	}

	private static EventLabeler<DataChangeObservationEvent> getEventLabelerForDataChangeObservation() {
		return new SimpleEventLabeler<>(Local_Labeler_ID.DATA_CHANGE, DataChangeObservationEvent.class, (context, event) -> {
			ValueType valueType = ValueType.LOW;
			if (event.getValue() > 10) {
				valueType = ValueType.HIGH;
			}
			return new MultiKeyEventLabel<>(event.getDatumType(), Local_Labeler_ID.DATA_CHANGE, DataChangeObservationEvent.class, event.getDatumType(), valueType);
		});
	}

	/**
	 * Tests {@link AgentContext#agentExists(AgentId)
	 */
	@Test
	@UnitTestMethod(name = "actorExists", args = { ActorId.class })
	public void testActorExists() {

		TestPluginData.Builder pluginDataBuilder = TestPluginData.builder();

		double testTime = 1;
		// there are no precondition tests

		// have the test agent show it exists and that other agents do not
		pluginDataBuilder.addTestActorPlan("actor", new TestActorPlan(testTime++, (context) -> {
			assertTrue(context.actorExists(new ActorId(0)));
			assertFalse(context.actorExists(new ActorId(1)));
			assertFalse(context.actorExists(new ActorId(2)));
		}));

		// build the plugin
		TestPluginData testPluginData = pluginDataBuilder.build();
		ScenarioPlanCompletionObserver scenarioPlanCompletionObserver = new ScenarioPlanCompletionObserver();

		Plugin testPlugin = TestPlugin.getTestPlugin(testPluginData);

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
	@UnitTestMethod(name = "addActor", args = { ActorContext.class })
	public void testAddActor() {
		TestPluginData.Builder pluginDataBuilder = TestPluginData.builder();

		MutableBoolean actorWasAdded = new MutableBoolean();

		// there are no precondition tests

		// have the test agent show it exists and that other agents do not
		pluginDataBuilder.addTestActorPlan("actor", new TestActorPlan(1, (c) -> {
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
	@UnitTestMethod(name = "addEventLabeler", args = { EventLabeler.class })
	public void testAddEventLabeler() {

		TestPluginData.Builder pluginDataBuilder = TestPluginData.builder();

		// have the actor test the preconditions
		pluginDataBuilder.addTestActorPlan("observer", new TestActorPlan(0, (c) -> {
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
		pluginDataBuilder.addTestActorPlan("observer", new TestActorPlan(1, (c) -> {
			c.addEventLabeler(eventLabeler);
		}));

		/*
		 * Create a container for the agent to record that it received the Test
		 * Event and we can conclude that the event labeler had been properly
		 * added to the simulation.
		 */
		MutableBoolean eventObserved = new MutableBoolean();

		// have the agent observe the test event

		pluginDataBuilder.addTestActorPlan("observer", new TestActorPlan(2, (c) -> {
			c.subscribe(new MultiKeyEventLabel<>(TestEvent.class, id, TestEvent.class), (c2, e) -> {
				eventObserved.setValue(true);
			});
		}));

		// have the actor create a test event for the agent to observe
		pluginDataBuilder.addTestActorPlan("observer", new TestActorPlan(3, (c) -> {
			c.releaseEvent(new TestEvent());
		}));

		// build the plugin
		TestPluginData testPluginData = pluginDataBuilder.build();
		ScenarioPlanCompletionObserver scenarioPlanCompletionObserver = new ScenarioPlanCompletionObserver();
		Plugin testPlugin = TestPlugin.getTestPlugin(testPluginData);

		// build and execute the engine
		Simulation	.builder()//
					.setOutputConsumer(scenarioPlanCompletionObserver::handleOutput)//
					.addPlugin(testPlugin)//
					.build()//
					.execute();//

		// show that all plans were executed
		assertTrue(scenarioPlanCompletionObserver.allPlansExecuted());

		/*
		 * Show that the event labeler must have been added to the simulation
		 * since the agent observed the test event
		 */
		assertTrue(eventObserved.getValue());

	}

	/**
	 * Tests {@link AgentContext#addPlan(Consumer, double, Object)
	 */
	@Test
	@UnitTestMethod(name = "addKeyedPlan", args = { Consumer.class, double.class, Object.class })
	public void testAddKeyedPlan() {

		TestPluginData.Builder pluginDataBuilder = TestPluginData.builder();

		// test preconditions
		pluginDataBuilder.addTestActorPlan("Alpha", new TestActorPlan(1, (context) -> {
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
		pluginDataBuilder.addTestActorPlan("Alpha", new TestActorPlan(2, (context) -> {
			Object key = new Object();
			assertFalse(context.getPlan(key).isPresent());
			context.addKeyedPlan((c) -> {
			}, 100, key);
			assertTrue(context.getPlan(key).isPresent());
		}));

		// build the plugin
		TestPluginData testPluginData = pluginDataBuilder.build();
		ScenarioPlanCompletionObserver scenarioPlanCompletionObserver = new ScenarioPlanCompletionObserver();
		Plugin testPlugin = TestPlugin.getTestPlugin(testPluginData);

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
	@UnitTestMethod(name = "addPassiveKeyedPlan", args = { Consumer.class, Object.class })
	public void testAddPassiveKeyedPlan() {
		TestPluginData.Builder pluginDataBuilder = TestPluginData.builder();

		// test preconditions
		pluginDataBuilder.addTestActorPlan("actor", new TestActorPlan(1, (context) -> {
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
		pluginDataBuilder.addTestActorPlan("actor", new TestActorPlan(2, (context) -> {
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

		pluginDataBuilder.addTestActorPlan("actor", new TestActorPlan(4, (context) -> {

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
	@UnitTestMethod(name = "addPassivePlan", args = { Consumer.class, double.class })
	public void testAddPassivePlan() {
		TestPluginData.Builder pluginDataBuilder = TestPluginData.builder();

		// test preconditions
		pluginDataBuilder.addTestActorPlan("actor", new TestActorPlan(1, (context) -> {
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

		pluginDataBuilder.addTestActorPlan("actor", new TestActorPlan(4, (context) -> {

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

	//

	/**
	 * Tests {@link AgentContext#addPlan(Consumer, double)
	 */
	@Test
	@UnitTestMethod(name = "addPlan", args = { Consumer.class, double.class })
	public void testAddPlan() {

		TestPluginData.Builder pluginDataBuilder = TestPluginData.builder();

		// test preconditions
		pluginDataBuilder.addTestActorPlan("actor", new TestActorPlan(1, (context) -> {
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

		pluginDataBuilder.addTestActorPlan("actor", new TestActorPlan(4, (context) -> {
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
	@UnitTestMethod(name = "getActorId", args = {})
	public void testGetActorId() {

		TestPluginData.Builder pluginDataBuilder = TestPluginData.builder();

		double testTime = 1;
		// there are no precondition tests

		Set<ActorId> observedActorIds = new LinkedHashSet<>();
		
		/*
		 * Have actors get their own actor ids and show that these ids match the
		 * expected values established duing the initialization of the
		 * TestActors.
		 */
		pluginDataBuilder.addTestActorPlan("Alpha", new TestActorPlan(testTime++, (c) -> {
			ActorId actorId = c.getActorId();
			observedActorIds.add(actorId);
			assertNotNull(actorId);
			
		}));

		pluginDataBuilder.addTestActorPlan("Beta", new TestActorPlan(testTime++, (c) -> {
			ActorId actorId = c.getActorId();
			observedActorIds.add(actorId);
			assertNotNull(actorId);
			
		}));

		pluginDataBuilder.addTestActorPlan("Gamma", new TestActorPlan(testTime++, (c) -> {
			ActorId actorId = c.getActorId();
			observedActorIds.add(actorId);
			assertNotNull(actorId);
			
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
		
		//show that the number of actor ids matches the number of actor aliases
		assertEquals(3, observedActorIds.size());
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

		/*
		 * Have the agent search for the data manager that was added to the
		 * simulation. Show that there is no instance of the second type of data
		 * manager present.
		 */
		pluginDataBuilder.addTestActorPlan("actor", new TestActorPlan(4, (c) -> {
			Optional<TestDataManager1> optional1 = c.getDataManager(TestDataManager1.class);
			assertTrue(optional1.isPresent());

			Optional<TestDataManager2> optional2 = c.getDataManager(TestDataManager2.class);
			assertFalse(optional2.isPresent());

			// show that we can ask for the child classes of a type individually
			Optional<TestDataManager3A> optional3A = c.getDataManager(TestDataManager3A.class);
			assertTrue(optional3A.isPresent());

			Optional<TestDataManager3B> optional3B = c.getDataManager(TestDataManager3B.class);
			assertTrue(optional3B.isPresent());

			// show that we can retrieve by the super type when there is no
			// collision
			Optional<TestDataManager4> optional4 = c.getDataManager(TestDataManager4.class);
			assertTrue(optional4.isPresent());

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
		pluginDataBuilder.addTestDataManager("dm3A",()->new TestDataManager3A());
		pluginDataBuilder.addTestDataManager("dm3B", ()->new TestDataManager3B());

		// show that ambiguous class matching throws an exception
		pluginDataBuilder.addTestActorPlan("actor", new TestActorPlan(0, (c) -> {
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
		pluginDataBuilder.addTestActorPlan("actor", new TestActorPlan(0, (c) -> {
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

	/**
	 * Tests {@link AgentContext#getPlan(Object)
	 */
	@Test
	@UnitTestMethod(name = "getPlan", args = { Object.class })
	public void testGetPlan() {

		TestPluginData.Builder pluginDataBuilder = TestPluginData.builder();

		// test preconditions
		pluginDataBuilder.addTestActorPlan("actor", new TestActorPlan(1, (context) -> {
			ContractException contractException = assertThrows(ContractException.class, () -> context.getPlan(null));
			assertEquals(NucleusError.NULL_PLAN_KEY, contractException.getErrorType());
		}));

		/*
		 * have the added test agent add a plan that can be retrieved and thus
		 * was added successfully
		 */
		pluginDataBuilder.addTestActorPlan("actor", new TestActorPlan(2, (context) -> {
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

	/**
	 * Tests {@link AgentContext#getPlanKeys()
	 */
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
		pluginDataBuilder.addTestActorPlan("actor", new TestActorPlan(1, (context) -> {
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
					.build()//
					.execute();//

		// show that the action plans got executed
		assertTrue(scenarioPlanCompletionObserver.allPlansExecuted());
	}

	/**
	 * Tests {@link AgentContext#getPlanTime(Object)
	 */
	@Test
	@UnitTestMethod(name = "getPlanTime", args = { Object.class })
	public void testGetPlanTime() {

		TestPluginData.Builder pluginDataBuilder = TestPluginData.builder();

		// test preconditions
		pluginDataBuilder.addTestActorPlan("actor", new TestActorPlan(1, (context) -> {
			ContractException contractException = assertThrows(ContractException.class, () -> context.getPlanTime(null));
			assertEquals(NucleusError.NULL_PLAN_KEY, contractException.getErrorType());
		}));

		/*
		 * have the added test agent add a plan and show that the plan time is
		 * as expected
		 */
		pluginDataBuilder.addTestActorPlan("actor", new TestActorPlan(2, (context) -> {
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
		 * Have the agent build plans to check the time in the simulation
		 * against the planning time
		 */
		pluginDataBuilder.addTestActorPlan("Agent1", new TestActorPlan(0, (context1) -> {
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

	/**
	 * Tests {@link AgentContext#halt()
	 */
	@Test
	@UnitTestMethod(name = "halt", args = {})
	public void testHalt() {
		TestPluginData.Builder pluginDataBuilder = TestPluginData.builder();

		// there are no precondition tests

		// have the test agent execute several tasks, with one of the tasks
		// halting the simulation
		TestActorPlan actionPlan1 = new TestActorPlan(1, (context) -> {
		});
		pluginDataBuilder.addTestActorPlan("actor", actionPlan1);

		TestActorPlan actionPlan2 = new TestActorPlan(2, (context) -> {
		});
		pluginDataBuilder.addTestActorPlan("actor", actionPlan2);

		TestActorPlan actionPlan3 = new TestActorPlan(3, (context) -> {
			context.halt();
		});
		pluginDataBuilder.addTestActorPlan("actor", actionPlan3);

		TestActorPlan actionPlan4 = new TestActorPlan(4, (context) -> {
		});
		pluginDataBuilder.addTestActorPlan("actor", actionPlan4);

		TestActorPlan actionPlan5 = new TestActorPlan(5, (context) -> {
		});
		pluginDataBuilder.addTestActorPlan("actor", actionPlan5);

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
		assertTrue(actionPlan1.executed());
		assertTrue(actionPlan2.executed());
		assertTrue(actionPlan3.executed());
		assertFalse(actionPlan4.executed());
		assertFalse(actionPlan5.executed());

	}

	@Test
	@UnitTestMethod(name = "releaseOutput", args = {})
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
		pluginDataBuilder.addTestActorPlan("actor", new TestActorPlan(1, (c) -> {
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
	@UnitTestMethod(name = "removeActor", args = { ActorId.class })
	public void testRemoveActor() {
		TestPluginData.Builder pluginDataBuilder = TestPluginData.builder();

		// have the resolver execute the precondition tests
		pluginDataBuilder.addTestActorPlan("actor", new TestActorPlan(0, (c) -> {

			ContractException contractException = assertThrows(ContractException.class, () -> c.removeActor(null));
			assertEquals(NucleusError.NULL_ACTOR_ID, contractException.getErrorType());

			contractException = assertThrows(ContractException.class, () -> c.removeActor(new ActorId(1000)));
			assertEquals(NucleusError.UNKNOWN_ACTOR_ID, contractException.getErrorType());

		}));

		List<ActorId> addedActorIds = new ArrayList<>();

		// have the add a few agents
		pluginDataBuilder.addTestActorPlan("actor", new TestActorPlan(1, (c) -> {
			for (int i = 0; i < 10; i++) {
				ActorId actorId = c.addActor((c2) -> {
				});
				assertTrue(c.actorExists(actorId));
				addedActorIds.add(actorId);
			}
		}));

		// have the actor remove the added actors
		pluginDataBuilder.addTestActorPlan("actor", new TestActorPlan(2, (c) -> {
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

	/**
	 * Tests {@link AgentContext#removePlan(Object)
	 */
	@Test
	@UnitTestMethod(name = "removePlan", args = { Object.class })
	public void testRemovePlan() {

		TestPluginData.Builder pluginDataBuilder = TestPluginData.builder();

		// test preconditions
		pluginDataBuilder.addTestActorPlan("actor", new TestActorPlan(1, (context) -> {
			ContractException contractException = assertThrows(ContractException.class, () -> context.removePlan(null));
			assertEquals(NucleusError.NULL_PLAN_KEY, contractException.getErrorType());
		}));

		Object key = new Object();
		MutableBoolean removedPlanHasExecuted = new MutableBoolean();

		// have the added test agent add a plan
		pluginDataBuilder.addTestActorPlan("actor", new TestActorPlan(2, (context) -> {
			context.addKeyedPlan((c2) -> {
				removedPlanHasExecuted.setValue(true);
			}, 4, key);
		}));

		// have the test agent remove the plan and show the plan no longer
		// exists
		pluginDataBuilder.addTestActorPlan("actor", new TestActorPlan(3, (context) -> {
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

	/**
	 * Tests {@link AgentContext#releaseEvent(Event)
	 */
	@Test
	@UnitTestMethod(name = "releaseEvent", args = { Event.class })
	public void testReleaseEvent() {
		TestPluginData.Builder pluginDataBuilder = TestPluginData.builder();

		MutableBoolean eventResolved = new MutableBoolean();

		// Have the actor subscribe to test event and then set the
		// eventResolved to true
		pluginDataBuilder.addTestActorPlan("alpha", new TestActorPlan(0, (c) -> {
			c.subscribe(TestEvent.class, (c2, e) -> {
				eventResolved.setValue(true);
			});
		}));

		// have another actor resolve a test event
		pluginDataBuilder.addTestActorPlan("beta", new TestActorPlan(1, (context) -> {
			context.releaseEvent(new TestEvent());
		}));

		// precondition tests
		pluginDataBuilder.addTestActorPlan("actor", new TestActorPlan(1, (context) -> {
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

	@Test
	@UnitTestMethod(name = "subscribe", args = { Class.class, BiConsumer.class })
	public void testSubscribe_EventClass() {
		TestPluginData.Builder pluginDataBuilder = TestPluginData.builder();

		// have an actor perform precondition tests
		pluginDataBuilder.addTestActorPlan("precondition checker", new TestActorPlan(0, (context) -> {
			Class<? extends Event> eventClass = null;
			ContractException contractException = assertThrows(ContractException.class, () -> context.subscribe(eventClass, (c, e) -> {
			}));
			assertEquals(NucleusError.NULL_EVENT_CLASS, contractException.getErrorType());

			contractException = assertThrows(ContractException.class, () -> context.subscribe(TestEvent.class, null));
			assertEquals(NucleusError.NULL_EVENT_CONSUMER, contractException.getErrorType());
		}));

		Set<MultiKey> receivedEvents = new LinkedHashSet<>();

		/*
		 * 
		 * Have an actor subscribe to data change events.
		 */

		pluginDataBuilder.addTestActorPlan("subscriber", new TestActorPlan(1, (context) -> {
			context.subscribe(DataChangeObservationEvent.class, (c, e) -> {
				receivedEvents.add(new MultiKey(c.getTime(), e));
			});
		}));

		/*
		 * Have another actor generate several data change observation events
		 * with differing types and values.
		 */
		pluginDataBuilder.addTestActorPlan("generator", new TestActorPlan(2, (c) -> {
			c.releaseEvent(new DataChangeObservationEvent(DatumType.TYPE_1, 0));
			c.releaseEvent(new DataChangeObservationEvent(DatumType.TYPE_2, 5));
			c.releaseEvent(new DataChangeObservationEvent(DatumType.TYPE_1, 20));

		}));

		pluginDataBuilder.addTestActorPlan("generator", new TestActorPlan(3, (c) -> {

			c.releaseEvent(new DataChangeObservationEvent(DatumType.TYPE_2, 0));
			c.releaseEvent(new DataChangeObservationEvent(DatumType.TYPE_1, 5));
			c.releaseEvent(new DataChangeObservationEvent(DatumType.TYPE_2, 25));
			c.releaseEvent(new DataChangeObservationEvent(DatumType.TYPE_1, 38));
			c.releaseEvent(new DataChangeObservationEvent(DatumType.TYPE_2, 234));
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

		// show that all plans got executed
		assertTrue(scenarioPlanCompletionObserver.allPlansExecuted());

		// show that all and only the observations corresponding to the
		// subscribed event label were delivered to the subscriber actor
		Set<MultiKey> expectedEvents = new LinkedHashSet<>();

		expectedEvents.add(new MultiKey(2.0, new DataChangeObservationEvent(DatumType.TYPE_1, 0)));
		expectedEvents.add(new MultiKey(2.0, new DataChangeObservationEvent(DatumType.TYPE_2, 5)));
		expectedEvents.add(new MultiKey(2.0, new DataChangeObservationEvent(DatumType.TYPE_1, 20)));
		expectedEvents.add(new MultiKey(3.0, new DataChangeObservationEvent(DatumType.TYPE_2, 0)));
		expectedEvents.add(new MultiKey(3.0, new DataChangeObservationEvent(DatumType.TYPE_1, 5)));
		expectedEvents.add(new MultiKey(3.0, new DataChangeObservationEvent(DatumType.TYPE_2, 25)));
		expectedEvents.add(new MultiKey(3.0, new DataChangeObservationEvent(DatumType.TYPE_1, 38)));
		expectedEvents.add(new MultiKey(3.0, new DataChangeObservationEvent(DatumType.TYPE_2, 234)));

		assertEquals(expectedEvents, receivedEvents);

	}

	/**
	 * Tests {@link AgentContext#subscribe(EventLabel, AgentEventConsumer)
	 */
	@Test
	@UnitTestMethod(name = "subscribe", args = { EventLabel.class, BiConsumer.class })
	public void testSubscribe_EventLabel() {

		TestPluginData.Builder pluginDataBuilder = TestPluginData.builder();

		// have an actor perform precondition tests
		pluginDataBuilder.addTestActorPlan("precondition checker", new TestActorPlan(0, (context) -> {

			context.addEventLabeler(new SimpleEventLabeler<TestEvent>(Local_Labeler_ID.TEST_LABELER_ID, TestEvent.class, (c, e) -> {
				return new TestEventLabel();
			}));

			TestEventLabel testEventLabel = null;
			ContractException contractException = assertThrows(ContractException.class, () -> context.subscribe(testEventLabel, (c, e) -> {
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
		 * Have an actor add an event labeler for DataChangeObservation events.
		 * Then have it subscribe to data change events that are or type 1 and
		 * high value. When it receives a data change observation, it records it
		 * as a multi-key in the received events set.
		 */

		pluginDataBuilder.addTestActorPlan("subscriber", new TestActorPlan(1, (context) -> {

			context.addEventLabeler(getEventLabelerForDataChangeObservation());
			context.subscribe(getEventLabelByDatumAndValue(DatumType.TYPE_1, ValueType.HIGH), (c, e) -> {
				receivedEvents.add(new MultiKey(c.getTime(), e));
			});
		}));

		/*
		 * Have another actor generate several data change observation events
		 * with differing types and values.
		 */
		pluginDataBuilder.addTestActorPlan("generator", new TestActorPlan(2, (c) -> {
			c.releaseEvent(new DataChangeObservationEvent(DatumType.TYPE_1, 0));
			c.releaseEvent(new DataChangeObservationEvent(DatumType.TYPE_2, 5));
			c.releaseEvent(new DataChangeObservationEvent(DatumType.TYPE_1, 20));
			c.releaseEvent(new DataChangeObservationEvent(DatumType.TYPE_2, 0));
			c.releaseEvent(new DataChangeObservationEvent(DatumType.TYPE_1, 5));
			c.releaseEvent(new DataChangeObservationEvent(DatumType.TYPE_2, 25));
			c.releaseEvent(new DataChangeObservationEvent(DatumType.TYPE_1, 38));
			c.releaseEvent(new DataChangeObservationEvent(DatumType.TYPE_2, 234));
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

		// show that all plans got executed
		assertTrue(scenarioPlanCompletionObserver.allPlansExecuted());

		// show that all and only the observations corresponding to the
		// subscribed event label were delivered to the subscriber actor
		Set<MultiKey> expectedEvents = new LinkedHashSet<>();
		expectedEvents.add(new MultiKey(2.0, new DataChangeObservationEvent(DatumType.TYPE_1, 20)));
		expectedEvents.add(new MultiKey(2.0, new DataChangeObservationEvent(DatumType.TYPE_1, 38)));

		assertEquals(expectedEvents, receivedEvents);

	}

	@Test
	@UnitTestMethod(name = "subscribeToSimulationClose", args = { Consumer.class })
	public void testSubscribeToSimulationClose() {
		TestPluginData.Builder pluginDataBuilder = TestPluginData.builder();

		MutableBoolean simCloseEventHandled = new MutableBoolean();

		// have an actor schedule a few events and subscribe to simulation close
		pluginDataBuilder.addTestActorPlan("actor", new TestActorPlan(0, (c) -> {
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
	@UnitTestMethod(name = "unsubscribe", args = { Class.class })
	public void testUnsubscribe_EventClass() {

		TestPluginData.Builder pluginDataBuilder = TestPluginData.builder();

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

		pluginDataBuilder.addTestActorPlan("generator", new TestActorPlan(0, (c) -> {

			for (Double time : eventGenerationTimes) {
				c.addPlan((c2) -> {
					c2.releaseEvent(new TestEvent());
				}, time);
			}
		}));

		/*
		 * precondition tests -- have the first actor test all the precondition
		 * exceptions
		 */
		pluginDataBuilder.addTestActorPlan("precondition tester", new TestActorPlan(0, (context) -> {

			// if the Event class is null
			Class<? extends Event> eventClass = null;
			ContractException contractException = assertThrows(ContractException.class, () -> context.unsubscribe(eventClass));
			assertEquals(NucleusError.NULL_EVENT_CLASS, contractException.getErrorType());

		}));

		// create a container for the events that are received by the three
		// actors
		Set<MultiKey> recievedEvents = new LinkedHashSet<>();

		// have the Alpha actor subscribe to the Test Event at time 0
		pluginDataBuilder.addTestActorPlan("Alpha", new TestActorPlan(0.1, (context) -> {
			context.subscribe(TestEvent.class, (c, e) -> {
				recievedEvents.add(new MultiKey("Alpha", c.getTime()));
			});
		}));

		// have the Alpha actor unsubscribe to the Test Event at time 5
		pluginDataBuilder.addTestActorPlan("Alpha", new TestActorPlan(5.1, (context) -> {
			context.unsubscribe(TestEvent.class);
		}));

		// have the Beta actor subscribe to the Test Event at time 4
		pluginDataBuilder.addTestActorPlan("Beta", new TestActorPlan(4.1, (context) -> {
			context.subscribe(TestEvent.class, (c, e) -> {
				recievedEvents.add(new MultiKey("Beta", c.getTime()));
			});
		}));

		// have the Beta actor unsubscribe to the Test Event at time 8
		pluginDataBuilder.addTestActorPlan("Beta", new TestActorPlan(8.1, (context) -> {

			context.unsubscribe(TestEvent.class);
		}));

		// have the Gamma actor subscribe to the Test Event at time 6
		pluginDataBuilder.addTestActorPlan("Gamma", new TestActorPlan(6.1, (context) -> {
			context.subscribe(TestEvent.class, (c, e) -> {
				recievedEvents.add(new MultiKey("Gamma", c.getTime()));
			});
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

		// show that all and only the observations corresponding to the
		// subscribed event label were delivered to the actors
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

	/**
	 * Tests {@link AgentContext#unsubscribe(EventLabel)
	 */
	@Test
	@UnitTestMethod(name = "unsubscribe", args = { EventLabel.class })
	public void testUnsubscribe_EventLabel() {

		TestPluginData.Builder pluginDataBuilder = TestPluginData.builder();

		/*
		 * Generate an event label that will match all TestEvents. This will be
		 * used throughout.
		 */
		MultiKeyEventLabel<TestEvent> eventLabel = new MultiKeyEventLabel<>(TestEvent.class, Local_Labeler_ID.TEST_LABELER_ID, TestEvent.class);

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

		pluginDataBuilder.addTestActorPlan("generator", new TestActorPlan(0, (c) -> {
			/*
			 * Add the required event labeler -- we want all TestEvents to be
			 * passed to all actor subscribers so that we can demonstrate that
			 * unsubscribing works without complicating the test with filtering
			 */
			EventLabeler<TestEvent> eventLabeler = new SimpleEventLabeler<TestEvent>(Local_Labeler_ID.TEST_LABELER_ID, TestEvent.class, (c2, e) -> {
				return eventLabel;
			});
			c.addEventLabeler(eventLabeler);

			for (Double time : eventGenerationTimes) {
				c.addPlan((c2) -> {
					c2.releaseEvent(new TestEvent());
				}, time);
			}
		}));

		/*
		 * precondition tests -- have the first actor test all the precondition
		 * exceptions
		 */
		pluginDataBuilder.addTestActorPlan("precondition tester", new TestActorPlan(0, (context) -> {

			// if the EventLabel is null
			EventLabel<TestEvent> nullEventLabel = null;
			ContractException contractException = assertThrows(ContractException.class, () -> context.unsubscribe(nullEventLabel));
			assertEquals(NucleusError.NULL_EVENT_LABEL, contractException.getErrorType());

			// if the event labeler id in the event label is null
			contractException = assertThrows(ContractException.class, () -> context.unsubscribe(new NullLabelerId_TestEventLabel()));
			assertEquals(NucleusError.NULL_LABELER_ID_IN_EVENT_LABEL, contractException.getErrorType());

			// if the event labeler id in the event label cannot be resolved to
			// a registered event labeler
			contractException = assertThrows(ContractException.class, () -> context.unsubscribe(new UnknownLabeler_TestEventLabel()));
			assertEquals(NucleusError.UNKNOWN_EVENT_LABELER, contractException.getErrorType());

			// if the event label has a null primary key
			contractException = assertThrows(ContractException.class, () -> context.unsubscribe(new NullPrimaryKey_TestEventLabel()));
			assertEquals(NucleusError.NULL_PRIMARY_KEY_VALUE, contractException.getErrorType());

		}));

		// create a container for the events that are received by the three
		// actors
		Set<MultiKey> recievedEvents = new LinkedHashSet<>();

		// have the Alpha actor subscribe to the Test Event at time 0
		pluginDataBuilder.addTestActorPlan("Alpha", new TestActorPlan(0.1, (context) -> {
			context.subscribe(eventLabel, (c, e) -> {
				recievedEvents.add(new MultiKey("Alpha", c.getTime()));
			});
		}));

		// have the Alpha actor unsubscribe to the Test Event at time 5
		pluginDataBuilder.addTestActorPlan("Alpha", new TestActorPlan(5.1, (context) -> {
			context.unsubscribe(eventLabel);
		}));

		// have the Beta actor subscribe to the Test Event at time 4
		pluginDataBuilder.addTestActorPlan("Beta", new TestActorPlan(4.1, (context) -> {
			context.subscribe(eventLabel, (c, e) -> {
				recievedEvents.add(new MultiKey("Beta", c.getTime()));
			});
		}));

		// have the Beta actor unsubscribe to the Test Event at time 8
		pluginDataBuilder.addTestActorPlan("Beta", new TestActorPlan(8.1, (context) -> {
			context.unsubscribe(eventLabel);
		}));

		// have the Gamma actor subscribe to the Test Event at time 6
		pluginDataBuilder.addTestActorPlan("Gamma", new TestActorPlan(6.1, (context) -> {
			context.subscribe(eventLabel, (c, e) -> {
				recievedEvents.add(new MultiKey("Gamma", c.getTime()));
			});
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

		// show that all and only the observations corresponding to the
		// subscribed event label were delivered to the actors
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

}