package nucleus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;

import org.junit.jupiter.api.Test;

import nucleus.testsupport.actionplugin.ActionPlugin;
import nucleus.testsupport.actionplugin.AgentActionPlan;
import nucleus.testsupport.actionplugin.ReportActionPlan;
import nucleus.testsupport.actionplugin.ResolverActionPlan;
import util.ContractError;
import util.ContractException;
import util.MultiKey;
import util.MutableBoolean;
import util.annotations.UnitTest;
import util.annotations.UnitTestMethod;

/**
 * 
 * Test for the implementation of ReportContext by the nucleus Engine
 * 
 * @author Shawn Hatch
 *
 */
@UnitTest(target = ReportContext.class)
public class AT_ReportContext {

	@Test
	@UnitTestMethod(name = "addPlan", args = { Consumer.class, double.class })
	public void testAddPlan() {

		ActionPlugin.Builder pluginBuilder = ActionPlugin.builder();

		// add an agent with a plan that will ensure all report plans are
		// executed
		pluginBuilder.addAgent("Agent");
		pluginBuilder.addAgentActionPlan("Agent", new AgentActionPlan(Double.POSITIVE_INFINITY, (c) -> {
		}));

		// add a mutable boolean that will be set to true when the plan executes
		MutableBoolean planExecution = new MutableBoolean();

		assertFalse(planExecution.getValue());

		// create the test report
		ReportId reportId = new SimpleReportId("test report");
		pluginBuilder.addReport(reportId);

		// have the report create a plan that will change the value of the
		// mutable boolean
		pluginBuilder.addReportActionPlan(reportId, new ReportActionPlan(1, (c) -> {
			c.addPlan((c2) -> {
				planExecution.setValue(true);
			}, 12);
		}));

		// test preconditions
		pluginBuilder.addReportActionPlan(reportId, new ReportActionPlan(2, (context) -> {
			ContractException contractException = assertThrows(ContractException.class, () -> context.addPlan(null, 1));
			assertEquals(NucleusError.NULL_PLAN, contractException.getErrorType());

			contractException = assertThrows(ContractException.class, () -> context.addPlan((c) -> {
			}, 0));
			assertEquals(NucleusError.PAST_PLANNING_TIME, contractException.getErrorType());

		}));

		// build the plugin
		ActionPlugin actionPlugin = pluginBuilder.build();

		// run the simulation
		Simulation.builder().addPlugin(ActionPlugin.PLUGIN_ID,actionPlugin::init).build().execute();

		// show that the plan that was added by the action plan was executed and
		// thus the addPlan invocation functioned correctly
		assertTrue(planExecution.getValue());

		// show that the action plans got executed
		assertTrue(actionPlugin.allActionsExecuted());

	}

	@Test
	@UnitTestMethod(name = "getCurrentReportId", args = {})
	public void testGetCurrentReportId() {
		ActionPlugin.Builder pluginBuilder = ActionPlugin.builder();

		// add an agent with a plan that will ensure all report plans are
		// executed
		pluginBuilder.addAgent("Agent");
		pluginBuilder.addAgentActionPlan("Agent", new AgentActionPlan(Double.POSITIVE_INFINITY, (c) -> {
		}));

		ReportId reportId1 = new SimpleReportId("report 1");
		ReportId reportId2 = new SimpleReportId("report 2");
		ReportId reportId3 = new SimpleReportId("report 3");

		pluginBuilder.addReport(reportId1);
		pluginBuilder.addReport(reportId2);
		pluginBuilder.addReport(reportId3);

		double testTime = 1;
		// there are no precondition tests

		// have the test agents test their own agent ids
		pluginBuilder.addReportActionPlan(reportId1, new ReportActionPlan(testTime++, (context) -> {
			ReportId reportId = context.getCurrentReportId();
			assertEquals(reportId1, reportId);
		}));

		pluginBuilder.addReportActionPlan(reportId2, new ReportActionPlan(testTime++, (context) -> {
			ReportId reportId = context.getCurrentReportId();
			assertEquals(reportId2, reportId);
		}));

		pluginBuilder.addReportActionPlan(reportId3, new ReportActionPlan(testTime++, (context) -> {
			ReportId reportId = context.getCurrentReportId();
			assertEquals(reportId3, reportId);
		}));

		// build the plugin
		ActionPlugin actionPlugin = pluginBuilder.build();

		// run the simulation
		Simulation.builder().addPlugin(ActionPlugin.PLUGIN_ID,actionPlugin::init).build().execute();

		// show that the action plans got executed
		assertTrue(actionPlugin.allActionsExecuted());
	}

	private static class StringEvent implements Event {
		private final String value;

		public StringEvent(String value) {
			this.value = value;
		}

		public String getValue() {
			return value;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((value == null) ? 0 : value.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			}
			if (!(obj instanceof StringEvent)) {
				return false;
			}
			StringEvent other = (StringEvent) obj;
			if (value == null) {
				if (other.value != null) {
					return false;
				}
			} else if (!value.equals(other.value)) {
				return false;
			}
			return true;
		}

		@Override
		public String toString() {
			StringBuilder builder = new StringBuilder();
			builder.append("StringEvent [value=");
			builder.append(value);
			builder.append("]");
			return builder.toString();
		}

	}

	@Test
	@UnitTestMethod(name = "subscribe", args = { Class.class, ReportEventConsumer.class })
	public void testSubscribe_ByEventClass() {

		// create some content for a resolver to broadcast as StringEvents
		Set<MultiKey> plannedEvents = new LinkedHashSet<>();
		plannedEvents.add(new MultiKey(3.532, "apple"));
		plannedEvents.add(new MultiKey(213.52334, "banana"));
		plannedEvents.add(new MultiKey(64.23, "cherry"));
		plannedEvents.add(new MultiKey(63.74, "durian"));
		plannedEvents.add(new MultiKey(9.23423, "elderberries"));
		plannedEvents.add(new MultiKey(10003.234, "fig"));
		plannedEvents.add(new MultiKey(4234.999, "grapefruit"));
		plannedEvents.add(new MultiKey(12346.532, "honeydew"));

		// create a container to hold the data received by the report from the
		// StringEvents
		Set<MultiKey> receivedEvents = new LinkedHashSet<>();

		// create the action plugin builder
		ActionPlugin.Builder pluginBuilder = ActionPlugin.builder();

		// add an agent that will guarantee the execution of report planning
		pluginBuilder.addAgent("Agent");
		pluginBuilder.addAgentActionPlan("Agent", new AgentActionPlan(Double.POSITIVE_INFINITY, (c) -> {
		}));

		// add a report
		ReportId reportId = new SimpleReportId("report");
		pluginBuilder.addReport(reportId);

		/*
		 * Have the report subscribe to all events of type Event1 and record
		 * them
		 */
		pluginBuilder.addReportActionPlan(reportId, new ReportActionPlan(0, (c) -> {
			c.subscribe(StringEvent.class, (c2, e) -> {
				receivedEvents.add(new MultiKey(c2.getTime(), e.getValue()));
			});
		}));

		// preconditions tests:
		pluginBuilder.addReportActionPlan(reportId, new ReportActionPlan(3, (c) -> {

			// show that a null class type reference is not allowed
			Class<StringEvent> nullClassReference = null;
			ContractException contractException = assertThrows(ContractException.class, () -> c.subscribe(nullClassReference, (c2, e) -> {
			}));
			assertEquals(NucleusError.NULL_EVENT_CLASS, contractException.getErrorType());

			// show that a null event consumer is not allowed
			contractException = assertThrows(ContractException.class, () -> c.subscribe(StringEvent.class, null));
			assertEquals(NucleusError.NULL_EVENT_CONSUMER, contractException.getErrorType());

		}));

		// Create a resolver and have it plan the release of the events for the
		// report to receive
		ResolverId resolverId = new SimpleResolverId("resolver");
		pluginBuilder.addResolver(resolverId);
		pluginBuilder.addResolverActionPlan(resolverId, new ResolverActionPlan(0, (c) -> {
			for (MultiKey multiKey : plannedEvents) {
				Double time = multiKey.getKey(0);
				String value = multiKey.getKey(1);
				StringEvent stringEvent = new StringEvent(value);
				c.addPlan((c2) -> {
					c2.queueEventForResolution(stringEvent);
				}, time);
			}
		}));

		// build the plugin
		ActionPlugin actionPlugin = pluginBuilder.build();

		// build and execute the engine
		Simulation.builder().addPlugin(ActionPlugin.PLUGIN_ID,actionPlugin::init).build().execute();

		assertEquals(plannedEvents, receivedEvents);

	}

	private static class TestEvent implements Event {

	}

	private static enum EventLabelFailureMode {
		NONE, NULL_EVENT_CLASS, NULL_LABELER_ID, UNKNOWN_LABELER, NULL_PRIMARY_KEY
	}

	private static class TestEventLabel implements EventLabel<TestEvent> {
		private final EventLabelFailureMode eventLabelFailureMode;

		public TestEventLabel(EventLabelFailureMode eventLabelFailureMode) {
			this.eventLabelFailureMode = eventLabelFailureMode;
		}

		@Override
		public Class<TestEvent> getEventClass() {
			switch (eventLabelFailureMode) {
			case NULL_EVENT_CLASS:
				return null;
			default:
				return TestEvent.class;
			}
		}

		@Override
		public EventLabelerId getLabelerId() {
			switch (eventLabelFailureMode) {
			case NULL_LABELER_ID:
				return null;
			case UNKNOWN_LABELER:
				return new EventLabelerId() {
				};
			default:
				return Local_Labeler_ID.TEST_LABELER_ID;
			}

		}

		@Override
		public Object getPrimaryKeyValue() {
			switch (eventLabelFailureMode) {
			case NULL_PRIMARY_KEY:
				return null;
			default:
				return TestEvent.class;
			}
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

	private static class TestObservationEvent implements Event {

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

	

	private static enum Local_Labeler_ID implements EventLabelerId {
		TEST_LABELER_ID, OBSERVATION_TEST_LABELER_ID, DATA_CHANGE
	}

	private static enum DatumType {
		TYPE_1, TYPE_2
	}

	@Test
	@UnitTestMethod(name = "subscribe", args = { EventLabel.class, ReportEventConsumer.class })

	public void testSubscribe_ByEventLabel() {
		ActionPlugin.Builder pluginBuilder = ActionPlugin.builder();

		// add an agent with a plan that will ensure all report plans are
		// executed
		pluginBuilder.addAgent("Agent");
		pluginBuilder.addAgentActionPlan("Agent", new AgentActionPlan(Double.POSITIVE_INFINITY, (c) -> {
		}));

		// add a test report
		ReportId reportId = new SimpleReportId("report");
		pluginBuilder.addReport(reportId);

		// create a container for the received events
		Set<DataChangeObservationEvent> actualEvents = new LinkedHashSet<>();

		/*
		 * Have the report subscribe to the Test Event and upon receiving a Test
		 * Event increment a counter
		 */
		pluginBuilder.addReportActionPlan(reportId, new ReportActionPlan(0, (context) -> {
			context.subscribe(getEventLabelByDatumAndValue(DatumType.TYPE_1, ValueType.HIGH), (c, e) -> {
				actualEvents.add(e);
			});
		}));

		// add an event labeler for the data change observation events
		pluginBuilder.addEventLabeler(getEventLabelerForDataChangeObservation());

		// add an event labeler that will support the precondition tests
		pluginBuilder.addEventLabeler(new SimpleEventLabeler<>(Local_Labeler_ID.TEST_LABELER_ID, TestEvent.class, (context, event) -> {
			return new MultiKeyEventLabel<>(TestEvent.class, Local_Labeler_ID.TEST_LABELER_ID, TestEvent.class);
		}));

		// Add a resolver for a test event type
		ResolverId resolverId = new SimpleResolverId("Test Resolver");
		pluginBuilder.addResolver(resolverId);

		// Have the resolver produce a few DataChangeObservations
		pluginBuilder.addResolverActionPlan(resolverId, new ResolverActionPlan(1, (c) -> {
			c.queueEventForResolution(new DataChangeObservationEvent(DatumType.TYPE_1, 0));
			c.queueEventForResolution(new DataChangeObservationEvent(DatumType.TYPE_2, 5));
			c.queueEventForResolution(new DataChangeObservationEvent(DatumType.TYPE_1, 20));
			c.queueEventForResolution(new DataChangeObservationEvent(DatumType.TYPE_2, 0));
			c.queueEventForResolution(new DataChangeObservationEvent(DatumType.TYPE_1, 5));
			c.queueEventForResolution(new DataChangeObservationEvent(DatumType.TYPE_2, 25));
			c.queueEventForResolution(new DataChangeObservationEvent(DatumType.TYPE_1, 38));
			c.queueEventForResolution(new DataChangeObservationEvent(DatumType.TYPE_2, 234));
		}));

		double testTime = 1;
		// precondition tests
		pluginBuilder.addReportActionPlan(reportId, new ReportActionPlan(testTime++, (context) -> {

			EventLabel<TestEvent> nullEventLabel = null;
			ContractException contractException = assertThrows(ContractException.class, () -> context.subscribe(nullEventLabel, (c, e) -> {
			}));
			assertEquals(NucleusError.NULL_EVENT_LABEL, contractException.getErrorType());

			contractException = assertThrows(ContractException.class, () -> context.subscribe(new TestEventLabel(EventLabelFailureMode.NONE), null));
			assertEquals(NucleusError.NULL_EVENT_CONSUMER, contractException.getErrorType());

			contractException = assertThrows(ContractException.class, () -> context.subscribe(new TestEventLabel(EventLabelFailureMode.NULL_EVENT_CLASS), (c, e) -> {
			}));
			assertEquals(NucleusError.NULL_EVENT_CLASS_IN_EVENT_LABEL, contractException.getErrorType());

			contractException = assertThrows(ContractException.class, () -> context.subscribe(new TestEventLabel(EventLabelFailureMode.NULL_LABELER_ID), (c, e) -> {
			}));
			assertEquals(NucleusError.NULL_LABELER_ID_IN_EVENT_LABEL, contractException.getErrorType());

			contractException = assertThrows(ContractException.class, () -> context.subscribe(new TestEventLabel(EventLabelFailureMode.UNKNOWN_LABELER), (c, e) -> {
			}));
			assertEquals(NucleusError.UNKNOWN_EVENT_LABELER, contractException.getErrorType());

			contractException = assertThrows(ContractException.class, () -> context.subscribe(new TestEventLabel(EventLabelFailureMode.NULL_PRIMARY_KEY), (c, e) -> {
			}));
			assertEquals(NucleusError.NULL_PRIMARY_KEY_VALUE, contractException.getErrorType());

		}));

		// build the plugin
		ActionPlugin actionPlugin = pluginBuilder.build();

		// run the simulation
		Simulation.builder().addPlugin(ActionPlugin.PLUGIN_ID,actionPlugin::init).build().execute();

		// show that the action plans got executed
		assertTrue(actionPlugin.allActionsExecuted());

		// show that all and only the observations corresponding to the
		// subscribed event label were delivered to the Alpha agent
		Set<DataChangeObservationEvent> expectedEvents = new LinkedHashSet<>();
		expectedEvents.add(new DataChangeObservationEvent(DatumType.TYPE_1, 20));
		expectedEvents.add(new DataChangeObservationEvent(DatumType.TYPE_1, 38));

		assertEquals(expectedEvents, actualEvents);
	}

	/**
	 * Subscribes the current report to have the given ReportContext consumer
	 * invoked at the end of the simulation.
	 */
	@Test
	@UnitTestMethod(name = "subscribeToSimulationClose", args = { Consumer.class })
	public void testSubscribeToSimulationClose() {
		MutableBoolean mutableBoolean = new MutableBoolean();

		ActionPlugin.Builder pluginBuilder = ActionPlugin.builder();

		// add an agent with a plan that will ensure all report plans are
		// executed
		pluginBuilder.addAgent("Agent");
		pluginBuilder.addAgentActionPlan("Agent", new AgentActionPlan(Double.POSITIVE_INFINITY, (c) -> {
		}));

		ReportId reportId = new SimpleReportId("report");
		pluginBuilder.addReport(reportId);

		pluginBuilder.addReportActionPlan(reportId, new ReportActionPlan(3, (r) -> {
			r.subscribeToSimulationClose((r2) -> {
				mutableBoolean.setValue(true);
			});
		}));

		ActionPlugin actionPlugin = pluginBuilder.build();

		Simulation.builder().addPlugin(ActionPlugin.PLUGIN_ID,actionPlugin::init).build().execute();

		assertTrue(mutableBoolean.getValue());
	}

	private static class Event1 implements Event {
	}

	@Test
	@UnitTestMethod(name = "releaseOutput", args = {})
	public void testReleaseOutput() {

		Set<Double> expectedPlanTimes = new LinkedHashSet<>();
		expectedPlanTimes.add(2.6);
		expectedPlanTimes.add(5.7);
		expectedPlanTimes.add(12.8);
		expectedPlanTimes.add(20.9);

		ActionPlugin.Builder pluginBuilder = ActionPlugin.builder();

		// add an agent with a plan that will ensure all report plans are
		// executed
		pluginBuilder.addAgent("Agent");
		pluginBuilder.addAgentActionPlan("Agent", new AgentActionPlan(Double.POSITIVE_INFINITY, (c) -> {
		}));

		// add a report
		ReportId reportId = new SimpleReportId("report");
		pluginBuilder.addReport(reportId);

		/*
		 * Have the report subscribe for Event1 events at time 0. Upon receiving
		 * the event, the report will release the simulation times
		 */
		pluginBuilder.addReportActionPlan(reportId, new ReportActionPlan(0, (context1) -> {
			context1.subscribe(Event1.class, (context2, e) -> {
				context2.releaseOutput(context2.getTime());
			});
		}));

		/*
		 * Add a resolver whose initialization will add plans to generate events
		 * of type Event1 at the expected times
		 */
		ResolverId resolverId = new SimpleResolverId("resolver");
		pluginBuilder.addResolver(resolverId);
		pluginBuilder.addResolverActionPlan(resolverId, new ResolverActionPlan(0, (c1) -> {
			// have the resolver produce events at the expected times
			for (Double planTime : expectedPlanTimes) {
				c1.addPlan((c2) -> {
					c2.queueEventForResolution(new Event1());
				}, planTime);
			}
		}));

		// Create a set to contain the released output
		Set<Object> output = new LinkedHashSet<>();

		/*
		 * Build and execute the engine. Add an output consumer that puts
		 * collects the output onto a set
		 */
		ActionPlugin actionPlugin = pluginBuilder.build();
		Simulation	.builder()//
				.addPlugin(ActionPlugin.PLUGIN_ID,actionPlugin::init)//
				.setOutputConsumer((o) -> output.add(o))//
				.build().execute();//

		// compare the set of planning times with the recorded output
		assertEquals(expectedPlanTimes, output);

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
		ActionPlugin.Builder pluginBuilder = ActionPlugin.builder();

		// add an agent with a plan that will ensure all report plans are
		// executed
		pluginBuilder.addAgent("Agent");
		pluginBuilder.addAgentActionPlan("Agent", new AgentActionPlan(Double.POSITIVE_INFINITY, (c) -> {
		}));

		// add a report
		ReportId reportId = new SimpleReportId("report");
		pluginBuilder.addReport(reportId);

		// create a data view for the agent to search for
		LocalDataView1 localDataView1 = new LocalDataView1();
		pluginBuilder.addDataView(localDataView1);

		/*
		 * Have the report search for the data view that was added to the
		 * simulation. Show that there is no instance of the second type of data
		 * view present.
		 */
		pluginBuilder.addReportActionPlan(reportId, new ReportActionPlan(4, (c) -> {
			Optional<LocalDataView1> optional1 = c.getDataView(LocalDataView1.class);
			assertTrue(optional1.isPresent());
			assertEquals(localDataView1, optional1.get());

			Optional<LocalDataView2> optional2 = c.getDataView(LocalDataView2.class);
			assertFalse(optional2.isPresent());

		}));

		// build the action plugin
		ActionPlugin actionPlugin = pluginBuilder.build();

		// execute the engine
		Simulation.builder().addPlugin(ActionPlugin.PLUGIN_ID,actionPlugin::init).build().execute();

		// show that the action was executed
		assertTrue(actionPlugin.allActionsExecuted());

	}

	@Test
	@UnitTestMethod(name = "getTime", args = {})
	public void testGetTime() {
		// create the action plugin builder
		ActionPlugin.Builder pluginBuilder = ActionPlugin.builder();

		// add an agent with a plan that will ensure all report plans are
		// executed
		pluginBuilder.addAgent("Agent");
		pluginBuilder.addAgentActionPlan("Agent", new AgentActionPlan(Double.POSITIVE_INFINITY, (c) -> {
		}));

		// add a report
		ReportId reportId = new SimpleReportId("report");
		pluginBuilder.addReport(reportId);

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
		pluginBuilder.addReportActionPlan(reportId, new ReportActionPlan(0, (context1) -> {
			for (Double planTime : planTimes) {
				context1.addPlan((context2) -> {
					assertEquals(planTime.doubleValue(), context2.getTime(), 0);
				}, planTime);
			}
		}));

		// build the action plugin
		ActionPlugin actionPlugin = pluginBuilder.build();

		// execute the engine
		Simulation.builder().addPlugin(ActionPlugin.PLUGIN_ID,actionPlugin::init).build().execute();

		// show that the action was executed
		assertTrue(actionPlugin.allActionsExecuted());
	}

	@Test
	@UnitTestMethod(name = "throwContractException", args = { ContractError.class })
	public void testThrowContractException() {
		// create the action plugin builder
		ActionPlugin.Builder pluginBuilder = ActionPlugin.builder();

		// add an agent that will guarantee the execution of report planning
		pluginBuilder.addAgent("Agent");
		pluginBuilder.addAgentActionPlan("Agent", new AgentActionPlan(Double.POSITIVE_INFINITY, (c) -> {
		}));

		// create a report
		ReportId reportId = new SimpleReportId("report");
		pluginBuilder.addReport(reportId);

		/*
		 * Have the report throw a contract exception
		 */
		ReportActionPlan reportActionPlan = new ReportActionPlan(4, (context) -> {
			context.throwContractException(NucleusError.ACCESS_VIOLATION);
		});

		pluginBuilder.addReportActionPlan(reportId, reportActionPlan);

		// build the action plugin
		ActionPlugin actionPlugin = pluginBuilder.build();

		// execute the engine and show that the expected contract exception was
		// thrown
		ContractException contractException = assertThrows(ContractException.class, () -> Simulation.builder().addPlugin(ActionPlugin.PLUGIN_ID,actionPlugin::init).build().execute());
		assertEquals(NucleusError.ACCESS_VIOLATION, contractException.getErrorType());

		// Show that the report action plan was executed
		assertTrue(reportActionPlan.executed());
	}

	@Test
	@UnitTestMethod(name = "throwContractException", args = { ContractError.class, Object.class })
	public void testThrowContractExceptionWithDetails() {
		// create the action plugin builder
		ActionPlugin.Builder pluginBuilder = ActionPlugin.builder();

		// add an agent that will guarantee the execution of report planning
		pluginBuilder.addAgent("Agent");
		pluginBuilder.addAgentActionPlan("Agent", new AgentActionPlan(Double.POSITIVE_INFINITY, (c) -> {
		}));

		// create a report
		ReportId reportId = new SimpleReportId("report");
		pluginBuilder.addReport(reportId);

		//
		String details = "these are the details for the contract exception";

		/*
		 * Have the report throw a contract exception
		 */
		ReportActionPlan reportActionPlan = new ReportActionPlan(4, (context) -> {
			context.throwContractException(NucleusError.ACCESS_VIOLATION, details);
		});

		pluginBuilder.addReportActionPlan(reportId, reportActionPlan);

		// build the action plugin
		ActionPlugin actionPlugin = pluginBuilder.build();

		// execute the engine and show that the expected contract exception was
		// thrown with the details contained in the exception's message
		ContractException contractException = assertThrows(ContractException.class, () -> Simulation.builder().addPlugin(ActionPlugin.PLUGIN_ID,actionPlugin::init).build().execute());
		assertEquals(NucleusError.ACCESS_VIOLATION, contractException.getErrorType());
		assertTrue(contractException.getMessage().contains(details));

		// Show that the report action plan was executed
		assertTrue(reportActionPlan.executed());
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

	private static enum ValueType {
		HIGH, LOW
	}
	
	@Test
	@UnitTestMethod(name = "addEventLabeler", args = { EventLabeler.class })
	public void testAddEventLabeler() {

		ActionPlugin.Builder pluginBuilder = ActionPlugin.builder();

		//add a report
		ReportId reportId = new SimpleReportId("report");
		pluginBuilder.addReport(reportId);
		
		

		// have the report test the preconditions
		pluginBuilder.addReportActionPlan(reportId, new ReportActionPlan(0, (c) -> {
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

		
		// have the report add the event labeler
		pluginBuilder.addReportActionPlan(reportId, new ReportActionPlan(1, (c) -> {
			c.addEventLabeler(eventLabeler);
		}));

		/*
		 * Create a container for the agent to record that it received the Test
		 * Event and we can conclude that the event labeler had been properly
		 * added to the simulation.
		 */
		MutableBoolean eventObserved = new MutableBoolean();

		// have the report observe the test event
		
		pluginBuilder.addReportActionPlan(reportId, new ReportActionPlan(2, (c) -> {
			c.subscribe(new MultiKeyEventLabel<>(TestEvent.class, id, TestEvent.class), (c2, e) -> {
				eventObserved.setValue(true);
			});
		}));
		
		// add a resolver
		ResolverId resolverId = new SimpleResolverId("resolver");
		pluginBuilder.addResolver(resolverId);

		// have the resolver create a test event for the agent to observe
		pluginBuilder.addResolverActionPlan(resolverId, new ResolverActionPlan(3, (c) -> {
			c.queueEventForResolution(new TestEvent());
		}));

		// build the plugin
		ActionPlugin actionPlugin = pluginBuilder.build();

		// build and execute the engine
		Simulation.builder().addPlugin(ActionPlugin.PLUGIN_ID,actionPlugin::init).build().execute();

		// show that all actions were executed
		assertTrue(actionPlugin.allActionsExecuted());

		/*
		 * Show that the event labeler must have been added to the simulation
		 * since the agent observed the test event
		 */
		assertTrue(eventObserved.getValue());

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
		public EventLabel<TestEvent> getEventLabel(Context context, TestEvent event) {
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

}
