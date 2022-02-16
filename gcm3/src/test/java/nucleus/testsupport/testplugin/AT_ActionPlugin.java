package nucleus.testsupport.testplugin;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.math3.random.RandomGenerator;
import org.junit.jupiter.api.Test;

import nucleus.DataView;
import nucleus.Simulation;
import nucleus.Event;
import nucleus.EventLabel;
import nucleus.EventLabeler;
import nucleus.EventLabelerId;
import nucleus.MultiKeyEventLabel;
import nucleus.PluginContext;
import nucleus.ReportId;
import nucleus.ResolverId;
import nucleus.SimpleEventLabeler;
import nucleus.SimpleReportId;
import nucleus.SimpleResolverId;
import util.MutableInteger;
import util.SeedProvider;
import util.annotations.UnitTest;
import util.annotations.UnitTestMethod;

@UnitTest(target = ActionPlugin.class)
public class AT_ActionPlugin {

	/**
	 * Show that the plugin's static plugin id is not null
	 */
	@Test
	public void testPluginId() {
		assertNotNull(ActionPlugin.PLUGIN_ID);
	}

	/**
	 * Show that a ActionPlugin Builder is returned
	 */
	@Test
	@UnitTestMethod(name = "builder", args = {})
	public void testBuilder() {
		assertNotNull(ActionPlugin.builder());
	}

	/**
	 * Shows that allActionsExecuted() return true if and only there is at least
	 * one action added to the plugin and all actions executed.
	 */
	@Test
	@UnitTestMethod(name = "allActionsExecuted", args = {})
	public void testAllActionsExecuted() {

		/*
		 * First we test that allActionsExecuted() is true when all actions are
		 * executed.
		 */
		ActionPlugin.Builder pluginBuilder = ActionPlugin.builder();

		pluginBuilder.addAgent("agent");
		MutableInteger actionCounter = new MutableInteger();

		pluginBuilder.addAgentActionPlan("agent", new AgentActionPlan(0, (c) -> actionCounter.increment()));
		pluginBuilder.addAgentActionPlan("agent", new AgentActionPlan(1, (c) -> actionCounter.increment()));
		pluginBuilder.addAgentActionPlan("agent", new AgentActionPlan(2, (c) -> actionCounter.increment()));

		ActionPlugin actionPlugin = pluginBuilder.build();

		// build and execute the engine
		Simulation.builder().addPlugin(ActionPlugin.PLUGIN_ID, actionPlugin::init).build().execute();

		// show that all actions executed
		assertEquals(3, actionCounter.getValue());
		assertTrue(actionPlugin.allActionsExecuted());

		/*
		 * Next show that allActionsExecuted() is false when some actions were
		 * not executed.
		 */

		pluginBuilder.addAgent("agent");
		actionCounter.setValue(0);

		pluginBuilder.addAgentActionPlan("agent", new AgentActionPlan(0, (c) -> actionCounter.increment()));
		pluginBuilder.addAgentActionPlan("agent", new AgentActionPlan(1, (c) -> {
			actionCounter.increment();
			c.halt();
		}));
		pluginBuilder.addAgentActionPlan("agent", new AgentActionPlan(2, (c) -> actionCounter.increment()));

		actionPlugin = pluginBuilder.build();

		// build and execute the engine
		Simulation.builder().addPlugin(ActionPlugin.PLUGIN_ID, actionPlugin::init).build().execute();

		// show that all actions executed
		assertEquals(2, actionCounter.getValue());
		assertFalse(actionPlugin.allActionsExecuted());

		/*
		 * Finally, show that allActionsExecuted() is false when there are no
		 * actions
		 */
		pluginBuilder.addAgent("agent");

		actionPlugin = pluginBuilder.build();

		// build and execute the engine
		Simulation.builder().addPlugin(ActionPlugin.PLUGIN_ID, actionPlugin::init).build().execute();

		// show that all actions executed
		assertFalse(actionPlugin.allActionsExecuted());

	}

	/*
	 * Empty implementor of DataView
	 */
	private static class TestDataView implements DataView {
	}

	/**
	 * Shows that the plugin.init() provides the following:
	 * 
	 * 1)AliasAssignmentEvent event handling
	 * 
	 * 2)ActionDataView initialization and publication
	 * 
	 * 3)creation of client agents
	 * 
	 * 4)creation of client reports
	 * 
	 * 5)creation of client resolvers
	 * 
	 * 6)publication of client data views.
	 * 
	 * 7)client event labelers are added to the simulation
	 * 
	 * It is not practical to test the init() directly. Instead we infer from
	 * the other tests in this class that the init() functions properly
	 */

	@Test
	@UnitTestMethod(name = "init", args = { PluginContext.class })
	public void testInit() {
		// covered by other tests
	}

	/**
	 * Shows that client agents can be added to the plugin
	 */
	@Test
	@UnitTestMethod(target = ActionPlugin.Builder.class, name = "addAgent", args = { Object.class })
	public void testAddAgent() {
		ActionPlugin.Builder pluginBuilder = ActionPlugin.builder();

		// Add a few agents
		List<Object> aliases = new ArrayList<>();

		aliases.add("agent1");
		aliases.add("agent2");
		aliases.add("agent3");

		for (Object alias : aliases) {
			pluginBuilder.addAgent(alias);
		}

		// Give actions to the agents
		for (Object alias : aliases) {
			pluginBuilder.addAgentActionPlan(alias, new AgentActionPlan(0, (c) -> {
				// show that this is the agent matching the alias
				ActionDataView actionDataView = c.getDataView(ActionDataView.class).get();
				Object actualAlias = actionDataView.getAgentAliasId(c.getCurrentAgentId()).get();
				assertEquals(alias, actualAlias);
			}));
		}

		// build the plugin
		ActionPlugin actionPlugin = pluginBuilder.build();

		// build and execute the engine
		Simulation.builder().addPlugin(ActionPlugin.PLUGIN_ID, actionPlugin::init).build().execute();

		// show that the agents were created
		assertTrue(actionPlugin.allActionsExecuted());

		// precondition tests
		assertThrows(RuntimeException.class, () -> ActionPlugin.builder().addAgent(null));

	}

	/**
	 * Show that agent action plans can be added
	 */
	@Test
	@UnitTestMethod(target = ActionPlugin.Builder.class, name = "addAgentActionPlan", args = { Object.class, AgentActionPlan.class })
	public void testAddAgentActionPlan() {
		ActionPlugin.Builder pluginBuilder = ActionPlugin.builder();

		// Add a few agents
		List<Object> aliases = new ArrayList<>();

		aliases.add("agent1");
		aliases.add("agent2");
		aliases.add("agent3");

		for (Object alias : aliases) {
			pluginBuilder.addAgent(alias);
		}

		// Give actions to the agents
		for (Object alias : aliases) {
			pluginBuilder.addAgentActionPlan(alias, new AgentActionPlan(0, (c) -> {
				// show that this is the agent matching the alias
				ActionDataView actionDataView = c.getDataView(ActionDataView.class).get();
				Object actualAlias = actionDataView.getAgentAliasId(c.getCurrentAgentId()).get();
				assertEquals(alias, actualAlias);
			}));
		}

		// build the plugin
		ActionPlugin actionPlugin = pluginBuilder.build();

		// build and execute the engine
		Simulation.builder().addPlugin(ActionPlugin.PLUGIN_ID, actionPlugin::init).build().execute();

		// show that all actions executed
		assertTrue(actionPlugin.allActionsExecuted());

		// precondition tests

		// if the alias is null
		assertThrows(RuntimeException.class, () -> ActionPlugin.builder().addAgentActionPlan(null, new AgentActionPlan(0, (c) -> {
		})));

		// if the agent action plan is null
		assertThrows(RuntimeException.class, () -> ActionPlugin.builder().addAgentActionPlan("alias", null));

	}

	/**
	 * Shows that data views can be added
	 */
	@Test
	@UnitTestMethod(target = ActionPlugin.Builder.class, name = "addDataView", args = { DataView.class })
	public void testAddDataView() {
		ActionPlugin.Builder pluginBuilder = ActionPlugin.builder();

		pluginBuilder.addAgent("agent");

		TestDataView testDataView = new TestDataView();
		pluginBuilder.addDataView(testDataView);

		pluginBuilder.addAgentActionPlan("agent", new AgentActionPlan(0, (c) -> {
			// show that the data view is available
			assertTrue(c.getDataView(TestDataView.class).isPresent());

		}));

		// build the plugin
		ActionPlugin actionPlugin = pluginBuilder.build();

		// build and execute the engine
		Simulation.builder().addPlugin(ActionPlugin.PLUGIN_ID, actionPlugin::init).build().execute();

		// show that the agents were created
		assertTrue(actionPlugin.allActionsExecuted());

		// precondition tests
		assertThrows(RuntimeException.class, () -> ActionPlugin.builder().addDataView(null));

	}

	/*
	 * Master id field to identify individual test events
	 */
	private static int masterTestEventId;

	/*
	 * Event used to transport test events to the resolver. The resolver then
	 * queues the internal test event for handling by agents.
	 */
	private static class TestWrapperEvent implements Event {
		private final TestEvent testEvent;

		public TestWrapperEvent(TestEvent testEvent) {
			this.testEvent = testEvent;
		}

		public TestEvent getTestEvent() {
			return testEvent;
		}
	}

	private static class TestEvent implements Event {
		private final int value;
		private int id = masterTestEventId++;

		public TestEvent(int value) {
			this.value = value;
		}

		public int getValue() {
			return value;
		}

		public int getId() {
			return id;
		}
	}

	private static EventLabelerId TEST_EVENT_LABELER_ID = new EventLabelerId() {
	};

	private static EventLabel<TestEvent> getTestEventLabel(int value) {
		return new MultiKeyEventLabel<>(TestEvent.class, TEST_EVENT_LABELER_ID, TestEvent.class, value);
	}

	private static EventLabeler<TestEvent> TEST_EVENT_LABELER = new SimpleEventLabeler<>(TEST_EVENT_LABELER_ID, TestEvent.class, (c, e) -> {
		return getTestEventLabel(e.getValue());
	});

	/**
	 * Shows that event labelers added to the builder are present in the
	 * simulation.
	 */
	@Test
	@UnitTestMethod(target = ActionPlugin.Builder.class, name = "addEventLabeler", args = { EventLabeler.class })
	public void testAddEventLabeler() {
		ActionPlugin.Builder pluginBuilder = ActionPlugin.builder();

		// Create two agents
		pluginBuilder.addAgent("observer agent");
		pluginBuilder.addAgent("generating agent");

		// create a counter for the number of test events that were sent to the
		// listening agent
		Set<Integer> expectedIds = new LinkedHashSet<>();
		Set<Integer> actualIds = new LinkedHashSet<>();
		/*
		 * Have the agent add an event labeler
		 */
		pluginBuilder.addAgentActionPlan("observer agent", new AgentActionPlan(0, (c) -> {
			c.addEventLabeler(TEST_EVENT_LABELER);
			c.subscribe(getTestEventLabel(5), (c2, e) -> {
				assertEquals(5, e.getValue());
				actualIds.add(e.getId());
			});
		}));

		RandomGenerator randomGenerator = SeedProvider.getRandomGenerator(4587629533550426553L);

		// Have the generating agent resolver test events over time
		pluginBuilder.addAgentActionPlan("generating agent", new AgentActionPlan(0, (c) -> {
			for (int i = 0; i < 100; i++) {
				double planTime = i + 1;
				c.addPlan((c2) -> {
					int value = randomGenerator.nextInt(5) + 1;
					TestEvent testEvent = new TestEvent(value);
					if (value == 5) {
						expectedIds.add(testEvent.id);
					}
					c.resolveEvent(new TestWrapperEvent(testEvent));
				}, planTime);
			}
		}));

		/*
		 * Events sent by agents cannot be observed by other agents. Instead, we
		 * need an event resolver to resend the event. Note that the resolver
		 * cannot directly resolve and send the TestEvent since that will cause
		 * an infinite loop.
		 */
		ResolverId resolverId = new SimpleResolverId("resolver");
		pluginBuilder.addResolver(resolverId);
		pluginBuilder.addResolverActionPlan(resolverId, new ResolverActionPlan(0, (c) -> {
			c.subscribeToEventExecutionPhase(TestWrapperEvent.class, (c2, e) -> {
				c2.queueEventForResolution(e.getTestEvent());
			});
		}));

		// build the plugin
		ActionPlugin actionPlugin = pluginBuilder.build();

		// build and execute the engine
		Simulation.builder().addPlugin(ActionPlugin.PLUGIN_ID, actionPlugin::init).build().execute();

		// show that all actions executed
		assertTrue(actionPlugin.allActionsExecuted());

		// show that there were a few test events with value 5 created
		assertFalse(expectedIds.isEmpty());

		// show that the actual and expected ids are the same
		assertEquals(expectedIds, actualIds);

		// precondition tests
		assertThrows(RuntimeException.class, () -> ActionPlugin.builder().addEventLabeler(null));
	}

	/**
	 * Shows that client reports can be added to the plugin
	 */
	@Test
	@UnitTestMethod(target = ActionPlugin.Builder.class, name = "addReport", args = { ReportId.class })
	public void testAddReport() {
		ActionPlugin.Builder pluginBuilder = ActionPlugin.builder();

		// Add a few agents
		List<ReportId> reportIds = new ArrayList<>();

		reportIds.add(new SimpleReportId("report 1"));
		reportIds.add(new SimpleReportId("report 2"));
		reportIds.add(new SimpleReportId("report 3"));

		for (ReportId reportId : reportIds) {
			pluginBuilder.addReport(reportId);
		}

		// Give actions to the reports
		for (ReportId reportId : reportIds) {
			pluginBuilder.addReportActionPlan(reportId, new ReportActionPlan(0, (c) -> {
				// show that this is the report matching the alias
				ReportId currentReportId = c.getCurrentReportId();
				assertEquals(reportId, currentReportId);
			}));
		}

		// add an agent to force time flow since reports can't force time
		pluginBuilder.addAgent("agent");
		pluginBuilder.addAgentActionPlan("agent", new AgentActionPlan(10, (c) -> {
		}));

		// build the plugin
		ActionPlugin actionPlugin = pluginBuilder.build();

		// build and execute the engine
		Simulation.builder().addPlugin(ActionPlugin.PLUGIN_ID, actionPlugin::init).build().execute();

		// show that the reports were created
		assertTrue(actionPlugin.allActionsExecuted());

		// precondition tests
		assertThrows(RuntimeException.class, () -> ActionPlugin.builder().addReport(null));

	}

	/**
	 * Show that report action plans can be added
	 */
	@Test
	@UnitTestMethod(target = ActionPlugin.Builder.class, name = "addReportActionPlan", args = { ReportId.class, ReportActionPlan.class })
	public void testAddReportActionPlan() {
		ActionPlugin.Builder pluginBuilder = ActionPlugin.builder();

		// Add a few agents
		List<ReportId> reportIds = new ArrayList<>();

		reportIds.add(new SimpleReportId("report 1"));
		reportIds.add(new SimpleReportId("report 2"));
		reportIds.add(new SimpleReportId("report 3"));

		for (ReportId reportId : reportIds) {
			pluginBuilder.addReport(reportId);
		}

		// Give actions to the reports
		for (ReportId reportId : reportIds) {
			pluginBuilder.addReportActionPlan(reportId, new ReportActionPlan(0, (c) -> {
				// show that this is the report matching the alias
				ReportId currentReportId = c.getCurrentReportId();
				assertEquals(reportId, currentReportId);
			}));
		}

		// add an agent to force time flow since reports can't force time
		pluginBuilder.addAgent("agent");
		pluginBuilder.addAgentActionPlan("agent", new AgentActionPlan(10, (c) -> {
		}));

		// build the plugin
		ActionPlugin actionPlugin = pluginBuilder.build();

		// build and execute the engine
		Simulation.builder().addPlugin(ActionPlugin.PLUGIN_ID, actionPlugin::init).build().execute();

		// show that the report actions were execute
		assertTrue(actionPlugin.allActionsExecuted());

		// precondition tests
		assertThrows(RuntimeException.class, () -> ActionPlugin.builder().addReportActionPlan(new SimpleReportId("failing report"), null));

	}

	/**
	 * Shows that client resolvers can be added to the plugin
	 */
	@Test
	@UnitTestMethod(target = ActionPlugin.Builder.class, name = "addResolver", args = { ResolverId.class })
	public void testAddResolver() {
		ActionPlugin.Builder pluginBuilder = ActionPlugin.builder();

		// Add a few resolvers
		List<ResolverId> resolverIds = new ArrayList<>();

		resolverIds.add(new SimpleResolverId("resolver 1"));
		resolverIds.add(new SimpleResolverId("resolver 2"));
		resolverIds.add(new SimpleResolverId("resolver 3"));

		for (ResolverId resolverId : resolverIds) {
			pluginBuilder.addResolver(resolverId);
		}

		// Give actions to the agents
		for (ResolverId resolverId : resolverIds) {
			pluginBuilder.addResolverActionPlan(resolverId, new ResolverActionPlan(0, (c) -> {
				// show that this is the resolver
				ResolverId currentResolverId = c.getCurrentResolverId();
				assertEquals(resolverId, currentResolverId);
			}));
		}

		// build the plugin
		ActionPlugin actionPlugin = pluginBuilder.build();

		// build and execute the engine
		Simulation.builder().addPlugin(ActionPlugin.PLUGIN_ID, actionPlugin::init).build().execute();

		// show that the resolvers were created
		assertTrue(actionPlugin.allActionsExecuted());

		// precondition tests
		assertThrows(RuntimeException.class, () -> ActionPlugin.builder().addResolver(null));

	}

	/**
	 * Show that resolver action plans can be added
	 */
	@Test
	@UnitTestMethod(target = ActionPlugin.Builder.class, name = "addResolverActionPlan", args = { ResolverId.class, ResolverActionPlan.class })
	public void testAddResolverActionPlan() {
		ActionPlugin.Builder pluginBuilder = ActionPlugin.builder();

		// Add a few resolvers
		List<ResolverId> resolverIds = new ArrayList<>();

		resolverIds.add(new SimpleResolverId("resolver 1"));
		resolverIds.add(new SimpleResolverId("resolver 2"));
		resolverIds.add(new SimpleResolverId("resolver 3"));

		for (ResolverId resolverId : resolverIds) {
			pluginBuilder.addResolver(resolverId);
		}

		// Give actions to the agents
		for (ResolverId resolverId : resolverIds) {
			pluginBuilder.addResolverActionPlan(resolverId, new ResolverActionPlan(0, (c) -> {
				// show that this is the resolver
				ResolverId currentResolverId = c.getCurrentResolverId();
				assertEquals(resolverId, currentResolverId);
			}));
		}

		// build the plugin
		ActionPlugin actionPlugin = pluginBuilder.build();

		// build and execute the engine
		Simulation.builder().addPlugin(ActionPlugin.PLUGIN_ID, actionPlugin::init).build().execute();

		// show that the resolvers were created
		assertTrue(actionPlugin.allActionsExecuted());

		// precondition tests

		// if the consumer is null
		assertThrows(RuntimeException.class, () -> ActionPlugin.builder().addResolverActionPlan(null, new ResolverActionPlan(0, (c) -> {
		})));

		// if the consumer is null
		assertThrows(RuntimeException.class, () -> ActionPlugin.builder().addResolverActionPlan(new SimpleResolverId("failing resolver"), null));

	}

	/**
	 * Show that the build() method generates a non-null ActionPlugin
	 */
	@Test
	@UnitTestMethod(target = ActionPlugin.Builder.class, name = "build", args = {})
	public void testBuild() {

		assertNotNull(ActionPlugin.builder().build());

	}

}
