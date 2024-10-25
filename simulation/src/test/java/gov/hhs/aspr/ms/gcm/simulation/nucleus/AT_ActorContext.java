package gov.hhs.aspr.ms.gcm.simulation.nucleus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import org.junit.jupiter.api.Test;

import gov.hhs.aspr.ms.gcm.simulation.nucleus.testsupport.testplugin.TestActorPlan;
import gov.hhs.aspr.ms.gcm.simulation.nucleus.testsupport.testplugin.TestDataManager;
import gov.hhs.aspr.ms.gcm.simulation.nucleus.testsupport.testplugin.TestDataManagerPlan;
import gov.hhs.aspr.ms.gcm.simulation.nucleus.testsupport.testplugin.TestPlugin;
import gov.hhs.aspr.ms.gcm.simulation.nucleus.testsupport.testplugin.TestPluginData;
import gov.hhs.aspr.ms.gcm.simulation.nucleus.testsupport.testplugin.TestScenarioReport;
import gov.hhs.aspr.ms.gcm.simulation.nucleus.testsupport.testplugin.TestSimulation;
import gov.hhs.aspr.ms.util.annotations.UnitTestMethod;
import gov.hhs.aspr.ms.util.errors.ContractException;
import gov.hhs.aspr.ms.util.wrappers.MultiKey;
import gov.hhs.aspr.ms.util.wrappers.MutableBoolean;

public class AT_ActorContext {

	/*
	 * DataView implementor to support tests
	 */
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

	private static class TestDataManager4B extends TestDataManager4 {

	}

	private static class DataChangeEvent implements Event {
		private final DatumType datumType;
		private final int value;

		public DataChangeEvent(final DatumType datumType, final int value) {
			super();
			this.datumType = datumType;
			this.value = value;
		}

		@Override
		public boolean equals(final Object obj) {
			if (this == obj) {
				return true;
			}
			if (!(obj instanceof DataChangeEvent)) {
				return false;
			}
			final DataChangeEvent other = (DataChangeEvent) obj;
			if ((datumType != other.datumType) || (value != other.value)) {
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
			builder.append("DataChangeEvent [datumType=");
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

	private static enum Local_Function_ID {
		DATUM, VALUE;
	}

	private static class BaseEvent implements Event {

	}

	private static class TestEvent implements Event {

	}

	private static enum ValueType {
		HIGH, LOW
	}

	/**
	 * Tests {@link AgentContext#agentExists(AgentId)
	 */
	@Test
	@UnitTestMethod(target = ActorContext.class, name = "actorExists", args = { ActorId.class })
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
		TestPluginData testPluginData = pluginDataBuilder.build();
		Plugin testPlugin = TestPlugin.getTestPlugin(testPluginData);

		TestSimulation.builder().addPlugin(testPlugin).build().execute();

	}

	@Test
	@UnitTestMethod(target = ActorContext.class, name = "addActor", args = { Consumer.class })
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

		TestSimulation.builder().addPlugin(testPlugin).build().execute();

		// show that the action plans got executed
		assertTrue(actorWasAdded.getValue());
	}

	//

	/**
	 * Tests {@link AgentContext#addPlan(Consumer, double)
	 */
	@Test
	@UnitTestMethod(target = ActorContext.class, name = "addPlan", args = { Consumer.class, double.class })
	public void testAddPlan_Consumer() {

		TestPluginData.Builder pluginDataBuilder = TestPluginData.builder();

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
		Simulation.builder()//
				.addPlugin(testPlugin)//
				.build()//
				.execute();//

		// we do not need to show that all plans executed

		// show that the last two passive plans did not execute
		assertTrue(planExecuted.getValue());

		// precondition test: if the consumer is null
		ContractException contractException = assertThrows(ContractException.class, () -> {
			Simulation.builder()//
					.addPlugin(TestPlugin.getTestPlugin(
							TestPluginData.builder().addTestActorPlan("actor", new TestActorPlan(0, (c) -> {
								c.addPlan(null, 0);
							})).build()))//
					.build()//
					.execute();//
		});
		assertEquals(NucleusError.NULL_PLAN_CONSUMER, contractException.getErrorType());

		// precondition test: if the plan is scheduled for a time in the past
		contractException = assertThrows(ContractException.class, () -> {
			Simulation.builder()//
					.addPlugin(TestPlugin.getTestPlugin(
							TestPluginData.builder().addTestActorPlan("actor", new TestActorPlan(0, (c) -> {
								c.addPlan((c2) -> {
								}, -10);
							})).build()))//
					.build()//
					.execute();//
		});
		assertEquals(NucleusError.PAST_PLANNING_TIME, contractException.getErrorType());

		// precondition test: if the plan is added to the simulation after event
		// processing is finished
		contractException = assertThrows(ContractException.class, () -> {
			Simulation.builder()//
					.addPlugin(TestPlugin.getTestPlugin(
							TestPluginData.builder().addTestActorPlan("actor", new TestActorPlan(0, (c) -> {
								c.subscribeToSimulationClose((c2) -> {
									c2.addPlan((c3) -> {
									}, 0);
								});

							})).build()))//
					.build()//
					.execute();//
		});
		assertEquals(NucleusError.PLANNING_QUEUE_CLOSED, contractException.getErrorType());

	}

	@Test
	@UnitTestMethod(target = ActorContext.class, name = "addPlan", args = { ActorPlan.class })
	public void testAddPlan_Plan() {

		TestPluginData.Builder pluginDataBuilder = TestPluginData.builder();

		/*
		 * Have the actor add a plan and show that that plan executes
		 */

		MutableBoolean planExecuted = new MutableBoolean();
		ActorPlan actorPlan = new ConsumerActorPlan(5, (c) -> {
			planExecuted.setValue(true);
		});

		pluginDataBuilder.addTestActorPlan("actor", new TestActorPlan(4, (context) -> {
			// schedule two passive plans
			context.addPlan(actorPlan);
		}));

		// build the plugin
		TestPluginData testPluginData = pluginDataBuilder.build();
		Plugin testPlugin = TestPlugin.getTestPlugin(testPluginData);

		// run the simulation
		Simulation.builder()//
				.addPlugin(testPlugin)//
				.build()//
				.execute();//

		// we do not need to show that all plans executed

		// show that the last two passive plans did not execute
		assertTrue(planExecuted.getValue());

		// precondition test: if the plan is null
		ContractException contractException = assertThrows(ContractException.class, () -> {
			Simulation.builder()//
					.addPlugin(TestPlugin.getTestPlugin(
							TestPluginData.builder().addTestActorPlan("actor", new TestActorPlan(0, (c) -> {
								c.addPlan(null);
							})).build()))//
					.build()//
					.execute();//
		});
		assertEquals(NucleusError.NULL_PLAN, contractException.getErrorType());

		// precondition test: if the plan is scheduled for a time in the past
		contractException = assertThrows(ContractException.class, () -> {
			Simulation.builder()//
					.addPlugin(TestPlugin.getTestPlugin(
							TestPluginData.builder().addTestActorPlan("actor", new TestActorPlan(0, (c) -> {
								c.addPlan(new ConsumerActorPlan(-10, (c3) -> {
								}));
							})).build()))//
					.build()//
					.execute();//
		});
		assertEquals(NucleusError.PAST_PLANNING_TIME, contractException.getErrorType());

		// precondition test: if the plan is added to the simulation after event
		// processing is finished
		contractException = assertThrows(ContractException.class, () -> {
			Simulation.builder()//
					.addPlugin(TestPlugin.getTestPlugin(
							TestPluginData.builder().addTestActorPlan("actor", new TestActorPlan(0, (c) -> {
								c.subscribeToSimulationClose((c2) -> {
									c2.addPlan(new ConsumerActorPlan(0, (c3) -> {
									}));
								});

							})).build()))//
					.build()//
					.execute();//
		});
		assertEquals(NucleusError.PLANNING_QUEUE_CLOSED, contractException.getErrorType());

	}

	@Test
	@UnitTestMethod(target = ActorContext.class, name = "getActorId", args = {})
	public void testGetActorId() {

		TestPluginData.Builder pluginDataBuilder = TestPluginData.builder();

		double testTime = 1;
		// there are no precondition tests

		Set<ActorId> observedActorIds = new LinkedHashSet<>();

		/*
		 * Have actors get their own actor ids and show that these ids match the
		 * expected values established duing the initialization of the TestActors.
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

		TestSimulation.builder().addPlugin(testPlugin).build().execute();

		// show that the number of actor ids matches the number of actor aliases
		assertEquals(3, observedActorIds.size());
	}
	
	
	
	
	private void executeGetDataManagerTest(Consumer<ActorContext> consumer) {
		
		// create the test plugin data builder
		TestPluginData.Builder pluginDataBuilder = TestPluginData.builder();

		// create a data manager for the actor to find

		pluginDataBuilder.addTestDataManager("dm1", () -> new TestDataManager1());
		pluginDataBuilder.addTestDataManager("dm3A", () -> new TestDataManager3A());
		pluginDataBuilder.addTestDataManager("dm3B", () -> new TestDataManager3B());
		pluginDataBuilder.addTestDataManager("dm4A", () -> new TestDataManager4A());

		pluginDataBuilder.addTestActorPlan("actor", new TestActorPlan(0,  consumer));

		// build the action plugin
		TestPluginData testPluginData = pluginDataBuilder.build();
		Plugin testPlugin = TestPlugin.getTestPlugin(testPluginData);

		TestSimulation.builder().addPlugin(testPlugin).build().execute();
	}

	@Test
	@UnitTestMethod(target = ActorContext.class, name = "getDataManager", args = { Class.class })
	public void testGetDataManager() {

		//postcondition test: 
		executeGetDataManagerTest((c)->{
			assertNotNull(c.getDataManager(TestDataManager1.class));			 
			assertNotNull(c.getDataManager(TestDataManager3A.class));
			assertNotNull(c.getDataManager(TestDataManager3B.class));
			assertNotNull(c.getDataManager(TestDataManager4A.class));
			assertNotNull(c.getDataManager(TestDataManager4.class));
		});

		// precondition test: if the class reference is null
		ContractException contractException = assertThrows(ContractException.class, ()->{
			executeGetDataManagerTest((c)->{
				c.getDataManager(null);
			});
		});
		assertEquals(NucleusError.NULL_DATA_MANAGER_CLASS, contractException.getErrorType());
		
		// precondition test: if the class reference is null
		contractException = assertThrows(ContractException.class, ()->{
			executeGetDataManagerTest((c)->{
				c.getDataManager(TestDataManager3.class);
			});
		});
		assertEquals(NucleusError.AMBIGUOUS_DATA_MANAGER_CLASS, contractException.getErrorType());
		
		// precondition test: if the class reference is unknown
		contractException = assertThrows(ContractException.class, ()->{
			executeGetDataManagerTest((c)->{
				c.getDataManager(TestDataManager4B.class);
			});
		});
		assertEquals(NucleusError.UNKNOWN_DATA_MANAGER, contractException.getErrorType());
	}

	@Test
	@UnitTestMethod(target = ActorContext.class, name = "dataManagerExists", args = { Class.class })
	public void testDataManagerExists() {

		// create the test plugin data builder
		TestPluginData.Builder pluginDataBuilder = TestPluginData.builder();

		// create a data manager for the actor to find

		pluginDataBuilder.addTestDataManager("dm1", () -> new TestDataManager1());
		pluginDataBuilder.addTestDataManager("dm3A", () -> new TestDataManager3A());
		pluginDataBuilder.addTestDataManager("dm3B", () -> new TestDataManager3B());
		pluginDataBuilder.addTestDataManager("dm4A", () -> new TestDataManager4A());

		pluginDataBuilder.addTestActorPlan("actor", new TestActorPlan(0, (c) -> {
			// show that the explicit class references return true
			assertTrue(c.dataManagerExists(TestDataManager1.class));
			assertTrue(c.dataManagerExists(TestDataManager3A.class));
			assertTrue(c.dataManagerExists(TestDataManager3B.class));
			assertTrue(c.dataManagerExists(TestDataManager4A.class));

			// show that zero class matching returns false
			assertFalse(c.dataManagerExists(TestDataManager4B.class));

			// show that ambiguous class matching returns false
			assertFalse(c.dataManagerExists(TestDataManager3.class));

			// show that a null yields a false
			assertFalse(c.dataManagerExists(null));

		}));

		// build the action plugin
		TestPluginData testPluginData = pluginDataBuilder.build();
		Plugin testPlugin = TestPlugin.getTestPlugin(testPluginData);

		TestSimulation.builder().addPlugin(testPlugin).build().execute();
	}

	@Test
	@UnitTestMethod(target = ActorContext.class, name = "getScheduledSimulationHaltTime", args = {})
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
					.addTestActorPlan("actor 1", new TestActorPlan(0, (context) -> {
						assertEquals(stopTime, context.getScheduledSimulationHaltTime());
					})).build();

			Plugin testPlugin = TestPlugin.getTestPlugin(testPluginData);

			TestSimulation.builder().setSimulationHaltTime(stopTime).addPlugin(testPlugin).build().execute();
		}
	}

	@Test
	@UnitTestMethod(target = ActorContext.class, name = "getTime", args = {})
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
		 * Have the agent build plans to check the time in the simulation against the
		 * planning time
		 */
		pluginDataBuilder.addTestActorPlan("Actor 1", new TestActorPlan(0, (context1) -> {
			for (Double planTime : planTimes) {
				context1.addPlan((context2) -> {
					assertEquals(planTime.doubleValue(), context2.getTime(), 0);
				}, planTime);
			}
		}));

		// build the action plugin
		TestPluginData testPluginData = pluginDataBuilder.build();
		Plugin testPlugin = TestPlugin.getTestPlugin(testPluginData);

		// execute the engine
		TestSimulation.builder().addPlugin(testPlugin).build().execute();
	}

	/**
	 * Tests {@link AgentContext#halt()
	 */
	@Test
	@UnitTestMethod(target = ActorContext.class, name = "halt", args = {})
	public void testHalt() {
		TestPluginData.Builder pluginDataBuilder = TestPluginData.builder();

		Set<Integer> expectedValues = new LinkedHashSet<>();
		expectedValues.add(1);
		expectedValues.add(2);
		expectedValues.add(3);

		Set<Integer> actualValues = new LinkedHashSet<>();

		// have the test agent execute several tasks, with one of the tasks
		// halting the simulation
		pluginDataBuilder.addTestActorPlan("actor", new TestActorPlan(1, (context) -> {
			actualValues.add(1);
		}));

		pluginDataBuilder.addTestActorPlan("actor", new TestActorPlan(2, (context) -> {
			actualValues.add(2);
		}));

		pluginDataBuilder.addTestActorPlan("actor", new TestActorPlan(3, (context) -> {
			actualValues.add(3);
			context.halt();
		}));

		pluginDataBuilder.addTestActorPlan("actor", new TestActorPlan(4, (context) -> {
			actualValues.add(4);
		}));

		pluginDataBuilder.addTestActorPlan("actor", new TestActorPlan(5, (context) -> {
			actualValues.add(5);
		}));

		// build the plugin
		TestPluginData testPluginData = pluginDataBuilder.build();
		Plugin testPlugin = TestPlugin.getTestPlugin(testPluginData);

		// run the simulation
		Simulation.builder()//
				.addPlugin(testPlugin)//
				.build()//
				.execute();//

		// show that the plans that were scheduled after the halt did not
		// execute
		assertEquals(expectedValues, actualValues);

	}

	@Test
	@UnitTestMethod(target = ActorContext.class, name = "releaseOutput", args = { Object.class })
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
		 * Add an output consumer that will place the output into the actualOutput set
		 * above and then execute the simulation
		 */
		Simulation.builder()//
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
	@UnitTestMethod(target = ActorContext.class, name = "removeActor", args = { ActorId.class })
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

		// build and execute the engine
		TestSimulation.builder().addPlugin(testPlugin).build().execute();

	}

	@Test
	@UnitTestMethod(target = ActorContext.class, name = "stateRecordingIsScheduled", args = {})
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
					.addTestActorPlan("actor 1", new TestActorPlan(0, (context) -> {
						assertEquals(stateRecording, context.stateRecordingIsScheduled());
					})).build();

			Plugin testPlugin = TestPlugin.getTestPlugin(testPluginData);

			TestSimulation.builder().setSimulationHaltTime(1).setProduceSimulationStateOnHalt(stateRecording)
					.addPlugin(testPlugin).build().execute();
		}
	}

	@Test
	@UnitTestMethod(target = ActorContext.class, name = "subscribeToSimulationClose", args = { Consumer.class })
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
		Simulation.builder()//
				.addPlugin(testPlugin)//
				.build()//
				.execute();//

		// show that the subscription to simulation close was successful
		assertTrue(simCloseEventHandled.getValue());

	}

	@Test
	@UnitTestMethod(target = ActorContext.class, name = "subscribe", args = { EventFilter.class, BiConsumer.class })
	public void testSubscribe() {

		TestPluginData.Builder pluginDataBuilder = TestPluginData.builder();

		// have an actor perform precondition tests
		pluginDataBuilder.addTestActorPlan("precondition checker", new TestActorPlan(0, (context) -> {
			EventFilter<TestEvent> eventFilter = EventFilter.builder(TestEvent.class).build();

			// if the event filter is null
			EventFilter<TestEvent> nullEventFilter = null;
			ContractException contractException = assertThrows(ContractException.class,
					() -> context.subscribe(nullEventFilter, (c, e) -> {
					}));
			assertEquals(NucleusError.NULL_EVENT_FILTER, contractException.getErrorType());

			// if the event consumer is null
			contractException = assertThrows(ContractException.class, () -> context.subscribe(eventFilter, null));
			assertEquals(NucleusError.NULL_EVENT_CONSUMER, contractException.getErrorType());

		}));

		Set<MultiKey> receivedEvents = new LinkedHashSet<>();

		/*
		 * Have an actor add an event filter for DataChangeObservation events. Then have
		 * it subscribe to data change events that are of type 1 and high value. When it
		 * receives a data change observation, it records it as a multi-key in the
		 * received events set.
		 */

		pluginDataBuilder.addTestActorPlan("subscriber", new TestActorPlan(1, (context) -> {

			EventFilter<DataChangeEvent> eventFilter = //
					EventFilter.builder(DataChangeEvent.class)//
							.addFunctionValuePair(new IdentifiableFunction<DataChangeEvent>(Local_Function_ID.DATUM,
									(e) -> e.getDatumType()), DatumType.TYPE_1)//
							.addFunctionValuePair(
									new IdentifiableFunction<DataChangeEvent>(Local_Function_ID.VALUE, (e) -> {
										if (e.getValue() > 10) {
											return ValueType.HIGH;
										} else {
											return ValueType.LOW;
										}
									}), ValueType.HIGH)//
							.build();//
			context.subscribe(eventFilter, (c, e) -> {
				receivedEvents.add(new MultiKey(c.getTime(), e));
			});
		}));

		/*
		 * Have a data manager generate several data change observation events with
		 * differing types and values.
		 */
		pluginDataBuilder.addTestDataManager("generator", () -> new TestDataManager());
		pluginDataBuilder.addTestDataManagerPlan("generator", new TestDataManagerPlan(2, (c) -> {
			c.releaseObservationEvent(new DataChangeEvent(DatumType.TYPE_1, 0));
			c.releaseObservationEvent(new DataChangeEvent(DatumType.TYPE_2, 5));
			c.releaseObservationEvent(new DataChangeEvent(DatumType.TYPE_1, 20));
			c.releaseObservationEvent(new DataChangeEvent(DatumType.TYPE_2, 0));
			c.releaseObservationEvent(new DataChangeEvent(DatumType.TYPE_1, 5));
			c.releaseObservationEvent(new DataChangeEvent(DatumType.TYPE_2, 25));
			c.releaseObservationEvent(new DataChangeEvent(DatumType.TYPE_1, 38));
			c.releaseObservationEvent(new DataChangeEvent(DatumType.TYPE_2, 234));
		}));

		// build the plugin
		TestPluginData testPluginData = pluginDataBuilder.build();
		Plugin testPlugin = TestPlugin.getTestPlugin(testPluginData);

		// run the simulation
		TestSimulation.builder().addPlugin(testPlugin).build().execute();

		// show that all and only the observations corresponding to the
		// subscribed event label were delivered to the subscriber actor
		Set<MultiKey> expectedEvents = new LinkedHashSet<>();
		expectedEvents.add(new MultiKey(2.0, new DataChangeEvent(DatumType.TYPE_1, 20)));
		expectedEvents.add(new MultiKey(2.0, new DataChangeEvent(DatumType.TYPE_1, 38)));

		assertEquals(expectedEvents, receivedEvents);
	}

	@Test
	@UnitTestMethod(target = ActorContext.class, name = "unsubscribe", args = { EventFilter.class })
	public void testUnsubscribe() {

		TestPluginData.Builder pluginDataBuilder = TestPluginData.builder();

		/*
		 * Generate an event label that will match all TestEvents. This will be used
		 * throughout.
		 */
		EventFilter<BaseEvent> eventFilter = EventFilter.builder(BaseEvent.class).build();//

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
		 * At time 0, have the test data manager generate plans to generate events at
		 * various times
		 */
		pluginDataBuilder.addTestDataManager("generator", () -> new TestDataManager1());
		pluginDataBuilder.addTestDataManagerPlan("generator", new TestDataManagerPlan(0, (c) -> {

			for (Double time : eventGenerationTimes) {
				c.addPlan((c2) -> {
					c2.releaseObservationEvent(new BaseEvent());
				}, time);
			}
		}));

		/*
		 * precondition tests -- have the first actor test all the precondition
		 * exceptions
		 */
		pluginDataBuilder.addTestActorPlan("precondition tester", new TestActorPlan(0, (context) -> {

			// if the event filter is null
			EventFilter<BaseEvent> nullEventFilter = null;
			ContractException contractException = assertThrows(ContractException.class,
					() -> context.unsubscribe(nullEventFilter));
			assertEquals(NucleusError.NULL_EVENT_FILTER, contractException.getErrorType());

		}));

		// create a container for the events that are received by the three
		// actors
		Set<MultiKey> recievedEvents = new LinkedHashSet<>();

		// have the Alpha actor subscribe to the Test Event at time 0
		pluginDataBuilder.addTestActorPlan("Alpha", new TestActorPlan(0.1, (context) -> {
			context.subscribe(eventFilter, (c, e) -> {
				recievedEvents.add(new MultiKey("Alpha", c.getTime()));
			});
		}));

		// have the Alpha actor unsubscribe to the Test Event at time 5
		pluginDataBuilder.addTestActorPlan("Alpha", new TestActorPlan(5.1, (context) -> {
			context.unsubscribe(eventFilter);
		}));

		// have the Beta actor subscribe to the Test Event at time 4
		pluginDataBuilder.addTestActorPlan("Beta", new TestActorPlan(4.1, (context) -> {
			context.subscribe(eventFilter, (c, e) -> {
				recievedEvents.add(new MultiKey("Beta", c.getTime()));
			});
		}));

		// have the Beta actor unsubscribe to the Test Event at time 8
		pluginDataBuilder.addTestActorPlan("Beta", new TestActorPlan(8.1, (context) -> {
			context.unsubscribe(eventFilter);
		}));

		// have the Gamma actor subscribe to the Test Event at time 6
		pluginDataBuilder.addTestActorPlan("Gamma", new TestActorPlan(6.1, (context) -> {
			context.subscribe(eventFilter, (c, e) -> {
				recievedEvents.add(new MultiKey("Gamma", c.getTime()));
			});
		}));

		// build the plugin
		TestPluginData testPluginData = pluginDataBuilder.build();
		Plugin testPlugin = TestPlugin.getTestPlugin(testPluginData);

		// run the simulation
		TestSimulation.builder().addPlugin(testPlugin).build().execute();

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

	@Test
	@UnitTestMethod(target = ActorContext.class, name = "getSimulationTime", args = { LocalDateTime.class })
	public void testGetSimulationTime() {

		// create some base dates to test

		LocalDate localDate = LocalDate.of(2020, 4, 1);

		List<LocalDateTime> localDateTimes = new ArrayList<>();

		localDateTimes.add(LocalDateTime.of(2023, 1, 10, 2, 17, 45));
		localDateTimes.add(LocalDateTime.of(2024, 6, 13, 8, 45, 37));
		localDateTimes.add(LocalDateTime.of(2020, 3, 15, 22, 13, 18));
		localDateTimes.add(LocalDateTime.of(2023, 12, 25, 15, 38, 19));

		TestPluginData.Builder pluginDataBuilder = TestPluginData.builder();
		pluginDataBuilder.addTestActorPlan("actor", new TestActorPlan(0, (c) -> {

			SimulationTimeConverter simulationTimeConverter = new SimulationTimeConverter(
					LocalDateTime.of(localDate, LocalTime.of(0, 0)));
			for (LocalDateTime localDateTime : localDateTimes) {
				assertEquals(simulationTimeConverter.getSimulationTime(localDateTime),
						c.getSimulationTime(localDateTime));
			}

		}));
		TestPluginData testPluginData = pluginDataBuilder.build();
		Plugin testPlugin = TestPlugin.getTestPlugin(testPluginData);

		SimulationState simulationState = SimulationState.builder().setBaseDate(localDate).build();
		TestSimulation.builder().setSimulationState(simulationState).addPlugin(testPlugin).build().execute();

	}

	@Test
	@UnitTestMethod(target = ActorContext.class, name = "getLocalDateTime", args = { double.class })
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
		pluginDataBuilder.addTestActorPlan("actor", new TestActorPlan(0, (c) -> {
			SimulationTimeConverter simulationTimeConverter = new SimulationTimeConverter(
					LocalDateTime.of(localDate, LocalTime.of(0, 0)));
			for (Double time : times) {
				assertEquals(simulationTimeConverter.getLocalDateTime(time), c.getLocalDateTime(time));
			}
		}));
		TestPluginData testPluginData = pluginDataBuilder.build();
		Plugin testPlugin = TestPlugin.getTestPlugin(testPluginData);

		SimulationState simulationState = SimulationState.builder().setBaseDate(localDate).build();
		TestSimulation.builder().setSimulationState(simulationState).addPlugin(testPlugin).build().execute();

	}

	@Test
	@UnitTestMethod(target = ActorContext.class, name = "retrievePlans", args = {})
	public void testRetrievePlans() {
		TestPluginData.Builder pluginDataBuilder = TestPluginData.builder();

		// test preconditions
		pluginDataBuilder.addTestActorPlan("actor", new TestActorPlan(1, (context) -> {

			ContractException contractException = assertThrows(ContractException.class, () -> context.retrievePlans());
			assertEquals(NucleusError.PLANNING_QUEUE_ACTIVE, contractException.getErrorType());
		}));

		List<ActorPlan> actorPlans = new ArrayList<>();
		List<ActorPlan> expectedPlans = new ArrayList<>();
		double haltTime = 50;

		for (int i = 1; i <= 100; i++) {
			ActorPlan actorPlan = new ConsumerActorPlan(i, (c) -> {
			});
			if (i > 50) {
				expectedPlans.add(actorPlan);
			}
			actorPlans.add(actorPlan);
		}
		/*
		 * Have the actor add a plan and show that that plan executes
		 */

		pluginDataBuilder.addTestActorPlan("actor", new TestActorPlan(0, (context) -> {
			for (ActorPlan plan : actorPlans) {
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

	private void planRetrievalSimCloseSubscribe(ActorContext context, List<ActorPlan> expectedPlans) {
		List<ActorPlan> plans = context.retrievePlans();

		assertEquals(expectedPlans.size(), plans.size());
		assertEquals(expectedPlans, plans);
	}
}