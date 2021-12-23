package plugins.people.resolvers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.Test;

import nucleus.Engine;
import nucleus.Engine.EngineBuilder;
import nucleus.EventLabeler;
import nucleus.NucleusError;
import nucleus.ResolverContext;
import nucleus.testsupport.actionplugin.ActionPlugin;
import nucleus.testsupport.actionplugin.AgentActionPlan;
import plugins.people.PeoplePlugin;
import plugins.people.datacontainers.PersonDataView;
import plugins.people.events.mutation.BulkPersonCreationEvent;
import plugins.people.events.mutation.PersonCreationEvent;
import plugins.people.events.mutation.PersonRemovalRequestEvent;
import plugins.people.events.mutation.PopulationGrowthProjectionEvent;
import plugins.people.events.observation.BulkPersonCreationObservationEvent;
import plugins.people.events.observation.PersonCreationObservationEvent;
import plugins.people.events.observation.PersonImminentRemovalObservationEvent;
import plugins.people.initialdata.PeopleInitialData;
import plugins.people.support.BulkPersonContructionData;
import plugins.people.support.PersonContructionData;
import plugins.people.support.PersonError;
import plugins.people.support.PersonId;
import util.ContractException;
import util.annotations.UnitTest;
import util.annotations.UnitTestConstructor;
import util.annotations.UnitTestMethod;

@UnitTest(target = PersonEventResolver.class)

public final class AT_PersonEventResolver {

	@Test
	@UnitTestConstructor(args = { PeopleInitialData.class })
	public void testConstructor() {
		// nothing to test
	}

	@Test
	@UnitTestMethod(name = "init", args = { ResolverContext.class })
	public void testPersonDataViewInitialization() {
		EngineBuilder engineBuilder = Engine.builder();
		PeopleInitialData.Builder peopleBuilder = PeopleInitialData.builder();
		/*
		 * Create the people plugin initialized with a few people. Note that we
		 * are putting the id values in reverse order and that they are not
		 * contiguous
		 */

		// create a container for our expectations
		Set<PersonId> expectedPersonIds = new LinkedHashSet<>();

		int personCount = 30;
		for (int i = 0; i < personCount; i++) {
			expectedPersonIds.add(new PersonId(i));
			peopleBuilder.addPersonId(new PersonId(1000 - i * i));
		}

		engineBuilder.addPlugin(PeoplePlugin.PLUGIN_ID, new PeoplePlugin(peopleBuilder.build())::init);

		ActionPlugin.Builder pluginBuilder = ActionPlugin.builder();

		// add a test agent
		pluginBuilder.addAgent("agent");

		// have the agent show that the people are present
		pluginBuilder.addAgentActionPlan("agent", new AgentActionPlan(0, (c) -> {
			Optional<PersonDataView> optional = c.getDataView(PersonDataView.class);
			assertTrue(optional.isPresent());

			PersonDataView personDataView = optional.get();

			assertEquals(expectedPersonIds, new LinkedHashSet<>(personDataView.getPeople()));

		}));

		// build the action plugin
		ActionPlugin actionPlugin = pluginBuilder.build();
		engineBuilder.addPlugin(ActionPlugin.PLUGIN_ID, actionPlugin::init);

		// build and execute the engine
		engineBuilder.build().execute();

		// show that all actions were executed
		assertTrue(actionPlugin.allActionsExecuted());
	}

	@Test
	@UnitTestMethod(name = "init", args = { ResolverContext.class })
	public void testPopulationGrowthProjectionEvent() {
		EngineBuilder engineBuilder = Engine.builder();

		// add the people plugin
		engineBuilder.addPlugin(PeoplePlugin.PLUGIN_ID, new PeoplePlugin(PeopleInitialData.builder().build())::init);

		ActionPlugin.Builder pluginBuilder = ActionPlugin.builder();

		// add a test agent
		pluginBuilder.addAgent("agent");

		// have the agent show that precondition tests work
		pluginBuilder.addAgentActionPlan("agent", new AgentActionPlan(0, (c) -> {
			ContractException contractException = assertThrows(ContractException.class, () -> c.resolveEvent(new PopulationGrowthProjectionEvent(-1)));
			assertEquals(PersonError.NEGATIVE_GROWTH_PROJECTION, contractException.getErrorType());
		}));

		// build the action plugin
		ActionPlugin actionPlugin = pluginBuilder.build();
		engineBuilder.addPlugin(ActionPlugin.PLUGIN_ID, actionPlugin::init);

		// build and execute the engine
		engineBuilder.build().execute();

		// show that all actions were executed
		assertTrue(actionPlugin.allActionsExecuted());
	}

	@Test
	@UnitTestMethod(name = "init", args = { ResolverContext.class })
	public void testPersonRemovalRequestEvent() {
		EngineBuilder engineBuilder = Engine.builder();

		// add the people plugin
		engineBuilder.addPlugin(PeoplePlugin.PLUGIN_ID, new PeoplePlugin(PeopleInitialData.builder().build())::init);

		ActionPlugin.Builder pluginBuilder = ActionPlugin.builder();

		// add test agents
		pluginBuilder.addAgent("agent");
		pluginBuilder.addAgent("observer");

		// add a container to collect the observed removals
		Set<PersonId> observedRemovals = new LinkedHashSet<>();
		Set<PersonId> expectedRemovals = new LinkedHashSet<>();
		for (int i = 0; i < 5; i++) {
			expectedRemovals.add(new PersonId(i));
		}

		// have the observer subscribe to the removals and record them onto the
		// observed removals
		pluginBuilder.addAgentActionPlan("observer", new AgentActionPlan(1, (c) -> {
			c.subscribe(PersonImminentRemovalObservationEvent.getEventLabel(), (c2, e) -> observedRemovals.add(e.getPersonId()));
		}));

		// have the agent add a few people
		pluginBuilder.addAgentActionPlan("agent", new AgentActionPlan(1, (c) -> {
			for (int i = 0; i < 10; i++) {
				c.resolveEvent(new PersonCreationEvent(PersonContructionData.builder().build()));
			}
		}));

		// have the agent remove the people
		pluginBuilder.addAgentActionPlan("agent", new AgentActionPlan(2, (c) -> {
			for (int i = 0; i < 5; i++) {
				c.resolveEvent(new PersonRemovalRequestEvent(new PersonId(i)));
			}
		}));

		// precondition tests
		pluginBuilder.addAgentActionPlan("agent", new AgentActionPlan(1, (c) -> {

			ContractException contractException = assertThrows(ContractException.class, () -> c.resolveEvent(new PersonRemovalRequestEvent(null)));
			assertEquals(PersonError.NULL_PERSON_ID, contractException.getErrorType());

			contractException = assertThrows(ContractException.class, () -> c.resolveEvent(new PersonRemovalRequestEvent(new PersonId(1000))));
			assertEquals(PersonError.UNKNOWN_PERSON_ID, contractException.getErrorType());

		}));

		// build the action plugin
		ActionPlugin actionPlugin = pluginBuilder.build();
		engineBuilder.addPlugin(ActionPlugin.PLUGIN_ID, actionPlugin::init);

		// build and execute the engine
		engineBuilder.build().execute();

		// show that all actions were executed
		assertTrue(actionPlugin.allActionsExecuted());

		// show that the observed removals match the expected removals
		assertEquals(expectedRemovals, observedRemovals);
	}

	@Test
	@UnitTestMethod(name = "init", args = { ResolverContext.class })
	public void testPersonCreationEvent() {
		EngineBuilder engineBuilder = Engine.builder();

		// add the people plugin
		engineBuilder.addPlugin(PeoplePlugin.PLUGIN_ID, new PeoplePlugin(PeopleInitialData.builder().build())::init);

		ActionPlugin.Builder pluginBuilder = ActionPlugin.builder();

		// create containers to hold observations
		Set<PersonId> observedPersonIds = new LinkedHashSet<>();
		Set<PersonId> expectedPersonIds = new LinkedHashSet<>();

		for (int i = 0; i < 10; i++) {
			PersonId personId = new PersonId(i);
			expectedPersonIds.add(personId);
		}
		// add test agents
		pluginBuilder.addAgent("agent");
		pluginBuilder.addAgent("observer");

		pluginBuilder.addAgentActionPlan("observer", new AgentActionPlan(0, (c) -> {
			c.subscribe(PersonCreationObservationEvent.getEventLabel(), (c2, e) -> observedPersonIds.add(e.getPersonId()));
		}));

		// have the agent add a few people and show they were added
		pluginBuilder.addAgentActionPlan("agent", new AgentActionPlan(1, (c) -> {

			PersonDataView personDataView = c.getDataView(PersonDataView.class).get();
			for (PersonId expectedPersonId : expectedPersonIds) {
				c.resolveEvent(new PersonCreationEvent(PersonContructionData.builder().build()));
				PersonId actualPersonId = personDataView.getLastIssuedPersonId().get();
				assertEquals(expectedPersonId, actualPersonId);
				assertTrue(personDataView.personExists(actualPersonId));
			}
		}));

		// precondition tests
		pluginBuilder.addAgentActionPlan("agent", new AgentActionPlan(2, (c) -> {

			ContractException contractException = assertThrows(ContractException.class, () -> c.resolveEvent(new PersonCreationEvent(null)));
			assertEquals(PersonError.NULL_PERSON_CONTRUCTION_DATA, contractException.getErrorType());

		}));

		// build the action plugin
		ActionPlugin actionPlugin = pluginBuilder.build();
		engineBuilder.addPlugin(ActionPlugin.PLUGIN_ID, actionPlugin::init);

		// build and execute the engine
		engineBuilder.build().execute();

		// show that all actions were executed
		assertTrue(actionPlugin.allActionsExecuted());

		// show that the expected and acutual observations match
		assertEquals(expectedPersonIds, observedPersonIds);
	}

	@Test
	@UnitTestMethod(name = "init", args = { ResolverContext.class })
	public void testBulkPersonCreationEvent() {
		EngineBuilder engineBuilder = Engine.builder();

		// add the people plugin
		engineBuilder.addPlugin(PeoplePlugin.PLUGIN_ID, new PeoplePlugin(PeopleInitialData.builder().build())::init);

		ActionPlugin.Builder pluginBuilder = ActionPlugin.builder();

		// create containers to hold observations
		Set<BulkPersonContructionData> observedBulkPersonContructionData = new LinkedHashSet<>();
		Set<BulkPersonContructionData> expectedBulkPersonContructionData = new LinkedHashSet<>();

		// generate three BulkPersonContructionData instances that contain
		// unique data
		int uniqueId = 0;
		for (int i = 0; i < 3; i++) {
			BulkPersonContructionData.Builder builder = BulkPersonContructionData.builder();
			for (int j = 0; j < 10; j++) {
				PersonContructionData personContructionData = PersonContructionData.builder().add(uniqueId++).build();
				builder.add(personContructionData);
			}
			expectedBulkPersonContructionData.add(builder.build());
		}
		// add test agents
		pluginBuilder.addAgent("agent");
		pluginBuilder.addAgent("observer");

		pluginBuilder.addAgentActionPlan("observer", new AgentActionPlan(0, (c) -> {
			c.subscribe(BulkPersonCreationObservationEvent.getEventLabel(), (c2, e) -> observedBulkPersonContructionData.add(e.getBulkPersonContructionData()));
		}));

		// have the agent add a bulk people and show the people were added
		pluginBuilder.addAgentActionPlan("agent", new AgentActionPlan(1, (c) -> {

			PersonDataView personDataView = c.getDataView(PersonDataView.class).get();
			for (BulkPersonContructionData bulkPersonContructionData : expectedBulkPersonContructionData) {
				List<PersonId> priorPeople = personDataView.getPeople();
				c.resolveEvent(new BulkPersonCreationEvent(bulkPersonContructionData));
				List<PersonId> postPeople = personDataView.getPeople();
				postPeople.removeAll(priorPeople);
				int expectedNewPeople = bulkPersonContructionData.getPersonContructionDatas().size();
				assertEquals(expectedNewPeople, postPeople.size());
			}
		}));

		// precondition tests
		pluginBuilder.addAgentActionPlan("agent", new AgentActionPlan(2, (c) -> {

			ContractException contractException = assertThrows(ContractException.class, () -> c.resolveEvent(new BulkPersonCreationEvent(null)));
			assertEquals(PersonError.NULL_BULK_PERSON_CONTRUCTION_DATA, contractException.getErrorType());

		}));

		// build the action plugin
		ActionPlugin actionPlugin = pluginBuilder.build();
		engineBuilder.addPlugin(ActionPlugin.PLUGIN_ID, actionPlugin::init);

		// build and execute the engine
		engineBuilder.build().execute();

		// show that all actions were executed
		assertTrue(actionPlugin.allActionsExecuted());

		// show that the expected and acutual observations match
		assertEquals(expectedBulkPersonContructionData, observedBulkPersonContructionData);
	}

	/*
	 * Runs the engine with an action agent that attempts to add the given event
	 * labeler. Failure is expected and indicates that the resolver already
	 * added the event labeler.
	 * 
	 */
	private void testLabeler(EventLabeler<?> eventLabeler) {

		EngineBuilder engineBuilder = Engine.builder();

		engineBuilder.addPlugin(PeoplePlugin.PLUGIN_ID, new PeoplePlugin(PeopleInitialData.builder().build())::init);

		ActionPlugin.Builder pluginBuilder = ActionPlugin.builder();

		pluginBuilder.addAgent("agent");
		pluginBuilder.addAgentActionPlan("agent", new AgentActionPlan(0, (c) -> {
			assertNotNull(eventLabeler);
			ContractException contractException = assertThrows(ContractException.class, () -> c.addEventLabeler(eventLabeler));
			assertEquals(NucleusError.DUPLICATE_LABELER_ID_IN_EVENT_LABELER, contractException.getErrorType());
		}));

		ActionPlugin actionPlugin = pluginBuilder.build();
		engineBuilder.addPlugin(ActionPlugin.PLUGIN_ID, actionPlugin::init);

		// build and execute the engine
		engineBuilder.build().execute();

		// show that all actions were executed
		assertTrue(actionPlugin.allActionsExecuted());

	}

	@Test
	@UnitTestMethod(name = "init", args = { ResolverContext.class })
	public void testBulkPersonCreationObservationEventLabelers() {
		EventLabeler<BulkPersonCreationObservationEvent> eventLabeler = BulkPersonCreationObservationEvent.getEventLabeler();
		testLabeler(eventLabeler);
	}

	@Test
	@UnitTestMethod(name = "init", args = { ResolverContext.class })
	public void testPersonCreationObservationEventLabelers() {
		EventLabeler<PersonCreationObservationEvent> eventLabeler = PersonCreationObservationEvent.getEventLabeler();
		testLabeler(eventLabeler);
	}

	@Test
	@UnitTestMethod(name = "init", args = { ResolverContext.class })
	public void testPersonImminentRemovalObservationEventLabelers() {
		EventLabeler<PersonImminentRemovalObservationEvent> eventLabeler = PersonImminentRemovalObservationEvent.getEventLabeler();
		testLabeler(eventLabeler);
	}

}