package plugins.people.datamanagers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;

import org.apache.commons.math3.util.FastMath;
import org.apache.commons.math3.util.Pair;
import org.junit.jupiter.api.Test;

import nucleus.ActorContext;
import nucleus.DataManagerContext;
import nucleus.EventFilter;
import nucleus.Plugin;
import nucleus.Simulation;
import nucleus.SimulationState;
import nucleus.testsupport.runcontinuityplugin.RunContinuityPlugin;
import nucleus.testsupport.runcontinuityplugin.RunContinuityPluginData;
import nucleus.testsupport.testplugin.TestActorPlan;
import nucleus.testsupport.testplugin.TestOutputConsumer;
import nucleus.testsupport.testplugin.TestPluginData;
import nucleus.testsupport.testplugin.TestSimulation;
import plugins.people.PeoplePlugin;
import plugins.people.events.PersonAdditionEvent;
import plugins.people.events.PersonImminentAdditionEvent;
import plugins.people.events.PersonImminentRemovalEvent;
import plugins.people.events.PersonRemovalEvent;
import plugins.people.support.PersonConstructionData;
import plugins.people.support.PersonError;
import plugins.people.support.PersonId;
import plugins.people.support.PersonRange;
import plugins.people.testsupport.PeopleTestPluginFactory;
import plugins.people.testsupport.PeopleTestPluginFactory.Factory;
import util.annotations.UnitTag;
import util.annotations.UnitTestConstructor;
import util.annotations.UnitTestMethod;
import util.errors.ContractException;

public final class AT_PeopleDataManager {
	/**
	 * Demonstrates that the data manager's initial state reflects its plugin
	 * data
	 */
	@Test
	@UnitTestMethod(target = PeopleDataManager.class, name = "init", args = { DataManagerContext.class })
	public void testStateInitialization() {

		// add a few people with gaps between id values
		int numberOfPeople = 5;
		PeoplePluginData.Builder peoplePluginDataBuilder = PeoplePluginData.builder();
		Set<PersonId> expectedPersonIds = new LinkedHashSet<>();
		for (int i = 0; i < numberOfPeople; i++) {
			PersonId personId = new PersonId(i * 3 + 10);
			peoplePluginDataBuilder.addPersonRange(new PersonRange(personId.getValue(), personId.getValue()));
			expectedPersonIds.add(personId);
		}

		PeoplePluginData peoplePluginData = peoplePluginDataBuilder.build();

		// add an actor to test the people were properly loaded into the person
		// data manager
		TestPluginData.Builder pluginBuilder = TestPluginData.builder();
		pluginBuilder.addTestActorPlan("actor", new TestActorPlan(0, (c) -> {
			PeopleDataManager peopleDataManager = c.getDataManager(PeopleDataManager.class);
			assertEquals(numberOfPeople, peopleDataManager.getPopulationCount());
			Set<PersonId> actualPersonIds = new LinkedHashSet<>(peopleDataManager.getPeople());
			assertEquals(expectedPersonIds, actualPersonIds);

			// show that the person id limit has the correct value
			int maxPersonIndex = Integer.MIN_VALUE;
			for (PersonId personId : expectedPersonIds) {
				maxPersonIndex = FastMath.max(maxPersonIndex, personId.getValue());
			}
			assertEquals(maxPersonIndex + 1, peopleDataManager.getPersonIdLimit());

			// show that the people who exist meet expectations
			Set<PersonId> peopleByExistence = new LinkedHashSet<>();
			for (int i = 0; i < maxPersonIndex + 10; i++) {
				PersonId personId = new PersonId(i);
				if (peopleDataManager.personExists(personId)) {
					peopleByExistence.add(personId);
				}
			}
			assertEquals(expectedPersonIds, peopleByExistence);

		}));
		TestPluginData testPluginData = pluginBuilder.build();

		Factory factory = PeopleTestPluginFactory//
													.factory(6970812715559334185L, testPluginData)//
													.setPeoplePluginData(peoplePluginData);

		TestSimulation	.builder()//
						.addPlugins(factory.getPlugins())//
						.build()//
						.execute();

		/**
		 * precondition test: if the plugin data person assignment time exceeds
		 * the start time of the simulation
		 */

		ContractException contractException = assertThrows(ContractException.class, () -> {
			PeoplePluginData peoplePluginData2 = PeoplePluginData	.builder()//
																	.setAssignmentTime(2.0)//
																	.build();

			Factory factory2 = PeopleTestPluginFactory	.factory(1054042752863257441L, testPluginData)//
														.setPeoplePluginData(peoplePluginData2);

			SimulationState simulationState = SimulationState	.builder()//
																.setStartTime(1.0)//
																.build();

			TestSimulation	.builder()//
							.addPlugins(factory2.getPlugins())//
							.setSimulationState(simulationState)//
							.build()//
							.execute();
		});
		assertEquals(PersonError.PERSON_ASSIGNMENT_TIME_IN_FUTURE, contractException.getErrorType());

	}

	/**
	 * Demonstrates that the data manager produces plugin data that reflects its
	 * final state
	 */
	@Test
	@UnitTestMethod(target = PeopleDataManager.class, name = "init", args = { DataManagerContext.class })
	public void testStateFinalization() {

		PeoplePluginData.Builder peoplePluginDataBuilder = PeoplePluginData.builder();
		PeoplePluginData peoplePluginData = peoplePluginDataBuilder.build();
		Set<PersonId> expectedPersonIds = new LinkedHashSet<>();

		TestPluginData.Builder pluginBuilder = TestPluginData.builder();

		// add 10 people to the data manager
		pluginBuilder.addTestActorPlan("actor", new TestActorPlan(0, (c) -> {
			PeopleDataManager peopleDataManager = c.getDataManager(PeopleDataManager.class);
			PersonConstructionData personConstructionData = PersonConstructionData.builder().build();
			for (int i = 0; i < 10; i++) {
				PersonId personId = peopleDataManager.addPerson(personConstructionData);
				expectedPersonIds.add(personId);
			}
		}));

		// show that the plugin data contains what we defined
		TestPluginData testPluginData = pluginBuilder.build();
		Factory factory = PeopleTestPluginFactory//
													.factory(6970812715559334185L, testPluginData)//
													.setPeoplePluginData(peoplePluginData);

		TestOutputConsumer testOutputConsumer = TestSimulation	.builder()//
																.addPlugins(factory.getPlugins())//
																.setProduceSimulationStateOnHalt(true)//
																.setSimulationHaltTime(2)//
																.build()//
																.execute();

		Map<PeoplePluginData, Integer> outputItems = testOutputConsumer.getOutputItemMap(PeoplePluginData.class);
		assertEquals(1, outputItems.size());
		PeoplePluginData actualPluginData = outputItems.keySet().iterator().next();

		PeoplePluginData.Builder expectedBuilder = PeoplePluginData.builder();
		for (PersonId personId : expectedPersonIds) {
			expectedBuilder.addPersonRange(new PersonRange(personId.getValue(), personId.getValue()));
		}
		PeoplePluginData expectedPluginData = expectedBuilder.build();

		assertEquals(expectedPluginData, actualPluginData);

		// show that the plugin data persists after multiple actions
		Set<PersonId> expectedPersonIds2 = new LinkedHashSet<>();

		pluginBuilder = TestPluginData.builder();

		pluginBuilder.addTestActorPlan("actor", new TestActorPlan(0, (c) -> {
			PeopleDataManager peopleDataManager = c.getDataManager(PeopleDataManager.class);
			PersonConstructionData personConstructionData = PersonConstructionData.builder().build();
			for (int i = 0; i < 10; i++) {
				PersonId personId = peopleDataManager.addPerson(personConstructionData);
				expectedPersonIds2.add(personId);
			}
		}));

		pluginBuilder.addTestActorPlan("actor", new TestActorPlan(1, (c) -> {
			PeopleDataManager peopleDataManager = c.getDataManager(PeopleDataManager.class);
			List<PersonId> people = peopleDataManager.getPeople();
			for (int i = 0; i < 9; i += 2) {
				PersonId personId = people.get(i);
				peopleDataManager.removePerson(personId);
				expectedPersonIds2.remove(personId);
			}
		}));

		testPluginData = pluginBuilder.build();

		factory = PeopleTestPluginFactory//
											.factory(6970812715559334185L, testPluginData)//
											.setPeoplePluginData(peoplePluginData);

		testOutputConsumer = TestSimulation	.builder()//
											.addPlugins(factory.getPlugins())//
											.setProduceSimulationStateOnHalt(true)//
											.setSimulationHaltTime(2).build()//
											.execute();

		outputItems = testOutputConsumer.getOutputItemMap(PeoplePluginData.class);
		assertEquals(1, outputItems.size());
		actualPluginData = outputItems.keySet().iterator().next();

		expectedBuilder = PeoplePluginData.builder();
		for (PersonId personId : expectedPersonIds2) {
			expectedBuilder.addPersonRange(new PersonRange(personId.getValue(), personId.getValue()));
		}
		expectedBuilder.setAssignmentTime(1.0);
		expectedPluginData = expectedBuilder.build();

		assertEquals(expectedPluginData, actualPluginData);
	}

	@Test
	@UnitTestMethod(target = PeopleDataManager.class, name = "personIndexExists", args = { int.class })
	public void testPersonIndexExists() {

		Factory factory = PeopleTestPluginFactory.factory(3328026739613106739L, (c) -> {
			PeopleDataManager peopleDataManager = c.getDataManager(PeopleDataManager.class);

			// initially there are no people despite the initial size
			assertFalse(peopleDataManager.personIndexExists(-1));
			assertFalse(peopleDataManager.personIndexExists(0));
			assertFalse(peopleDataManager.personIndexExists(1));

			// show that we can add a few people and for each the manager will
			// indicate that they exist
			for (int i = 0; i < 10; i++) {
				PersonId personId = peopleDataManager.addPerson(PersonConstructionData.builder().build());
				assertTrue(peopleDataManager.personIndexExists(personId.getValue()));
			}

			// show that people who should not exist, actually don't exist
			assertFalse(peopleDataManager.personIndexExists(-1));
			for (int i = 10; i < 20; i++) {
				assertFalse(peopleDataManager.personIndexExists(i));
			}

		});
		TestSimulation.builder().addPlugins(factory.getPlugins()).build().execute();

	}

	@Test
	@UnitTestMethod(target = PeopleDataManager.class, name = "getPersonIdLimit", args = {})
	public void testGetPersonIdLimit() {
		Factory factory = PeopleTestPluginFactory.factory(2489565009155477444L, (c) -> {
			PeopleDataManager peopleDataManager = c.getDataManager(PeopleDataManager.class);
			/*
			 * Initially there are no people despite the initial size, so we
			 * expect the limit to be zero.
			 */
			assertEquals(0, peopleDataManager.getPersonIdLimit());

			// show that the limit increments as PersonId values are added
			for (int i = 0; i < 10; i++) {
				PersonId personId = peopleDataManager.addPerson(PersonConstructionData.builder().build());
				assertEquals(personId.getValue() + 1, peopleDataManager.getPersonIdLimit());
			}

		});
		TestSimulation.builder().addPlugins(factory.getPlugins()).build().execute();

	}

	@Test
	@UnitTestMethod(target = PeopleDataManager.class, name = "getBoxedPersonId", args = { int.class })
	public void testGetBoxedPersonId() {
		Factory factory = PeopleTestPluginFactory.factory(7973222351020835580L, (c) -> {
			PeopleDataManager peopleDataManager = c.getDataManager(PeopleDataManager.class);
			// show that the boxed person id is correct
			for (int i = 0; i < 10; i++) {
				Optional<PersonId> optional = peopleDataManager.getBoxedPersonId(i);
				assertFalse(optional.isPresent());

				PersonId personId = peopleDataManager.addPerson(PersonConstructionData.builder().build());
				optional = peopleDataManager.getBoxedPersonId(i);
				assertTrue(optional.isPresent());

				PersonId boxedPersonId = optional.get();
				assertEquals(personId, boxedPersonId);
			}
		});
		TestSimulation.builder().addPlugins(factory.getPlugins()).build().execute();
	}

	@Test
	@UnitTestMethod(target = PeopleDataManager.class, name = "addPerson", args = { PersonConstructionData.class })
	public void testAddPerson() {

		TestPluginData.Builder pluginDataBuilder = TestPluginData.builder();

		// create containers to hold observations
		Set<PersonId> observedPersonIds = new LinkedHashSet<>();
		Set<PersonId> observedImminentPersonIds = new LinkedHashSet<>();
		Set<PersonId> expectedPersonIds = new LinkedHashSet<>();

		for (int i = 0; i < 10; i++) {
			PersonId personId = new PersonId(i);
			expectedPersonIds.add(personId);
		}

		pluginDataBuilder.addTestActorPlan("observer", new TestActorPlan(0, (c) -> {
			c.subscribe(EventFilter.builder(PersonImminentAdditionEvent.class).build(), (c2, e) -> observedImminentPersonIds.add(e.personId()));
			c.subscribe(EventFilter.builder(PersonAdditionEvent.class).build(), (c2, e) -> observedPersonIds.add(e.personId()));
		}));

		// have the agent add a few people and show they were added
		pluginDataBuilder.addTestActorPlan("actor", new TestActorPlan(1, (c) -> {
			PeopleDataManager peopleDataManager = c.getDataManager(PeopleDataManager.class);
			for (PersonId expectedPersonId : expectedPersonIds) {
				PersonId actualPersonId = peopleDataManager.addPerson(PersonConstructionData.builder().build());
				assertEquals(expectedPersonId, actualPersonId);
				assertTrue(peopleDataManager.personExists(actualPersonId));
			}
		}));

		// precondition tests
		pluginDataBuilder.addTestActorPlan("actor", new TestActorPlan(2, (c) -> {
			PeopleDataManager peopleDataManager = c.getDataManager(PeopleDataManager.class);
			ContractException contractException = assertThrows(ContractException.class, () -> peopleDataManager.addPerson(null));
			assertEquals(PersonError.NULL_PERSON_CONSTRUCTION_DATA, contractException.getErrorType());
		}));

		TestPluginData testPluginData = pluginDataBuilder.build();

		Factory factory = PeopleTestPluginFactory.factory(3010391631885520624L, testPluginData);

		TestSimulation.builder().addPlugins(factory.getPlugins()).build().execute();
		// show that the expected and actual observations match
		assertEquals(expectedPersonIds, observedPersonIds);
		assertEquals(expectedPersonIds, observedImminentPersonIds);
	}

	@Test
	@UnitTestMethod(target = PeopleDataManager.class, name = "personExists", args = { PersonId.class })
	public void testPersonExists() {

		Factory factory = PeopleTestPluginFactory.factory(8692409871861590014L, (c) -> {
			PeopleDataManager peopleDataManager = c.getDataManager(PeopleDataManager.class);

			for (int i = 0; i < 10; i++) {
				peopleDataManager.addPerson(PersonConstructionData.builder().build());
			}

			assertFalse(peopleDataManager.personExists(new PersonId(100000)));

			for (int i = 0; i < 10; i++) {
				assertTrue(peopleDataManager.personExists(new PersonId(i)));
			}

			for (int i = 10; i < 20; i++) {
				assertFalse(peopleDataManager.personExists(new PersonId(i)));
			}

		});
		TestSimulation.builder().addPlugins(factory.getPlugins()).build().execute();

	}

	@Test
	@UnitTestMethod(target = PeopleDataManager.class, name = "getPeople", args = {})
	public void testGetPeople() {

		List<PersonId> expectedPeople = new ArrayList<>();

		TestPluginData.Builder pluginBuilder = TestPluginData.builder();

		pluginBuilder.addTestActorPlan("actor", new TestActorPlan(0, (c) -> {
			PeopleDataManager peopleDataManager = c.getDataManager(PeopleDataManager.class);

			// add some people

			for (int i = 0; i < 10; i++) {
				PersonId personId = peopleDataManager.addPerson(PersonConstructionData.builder().build());
				expectedPeople.add(personId);
			}

			List<PersonId> actualPeople = peopleDataManager.getPeople();
			assertEquals(expectedPeople.size(), actualPeople.size());
			assertEquals(new LinkedHashSet<>(expectedPeople), new LinkedHashSet<>(actualPeople));

		}));

		pluginBuilder.addTestActorPlan("actor", new TestActorPlan(1, (c) -> {
			PeopleDataManager peopleDataManager = c.getDataManager(PeopleDataManager.class);

			// remove a few people
			for (int i = 0; i < 5; i++) {
				PersonId personId = new PersonId(i);
				peopleDataManager.removePerson(personId);
				expectedPeople.remove(personId);
			}

		}));

		pluginBuilder.addTestActorPlan("actor", new TestActorPlan(2, (c) -> {
			PeopleDataManager peopleDataManager = c.getDataManager(PeopleDataManager.class);
			// show that the removals resulted in the correct people
			List<PersonId> actualPeople = peopleDataManager.getPeople();
			assertEquals(expectedPeople.size(), actualPeople.size());
			assertEquals(new LinkedHashSet<>(expectedPeople), new LinkedHashSet<>(actualPeople));
		}));

		TestPluginData testPluginData = pluginBuilder.build();

		Factory factory = PeopleTestPluginFactory.factory(6955438283727605404L, testPluginData);
		TestSimulation.builder().addPlugins(factory.getPlugins()).build().execute();
	}

	@Test
	@UnitTestMethod(target = PeopleDataManager.class, name = "removePerson", args = { PersonId.class })
	public void testRemovePerson() {

		TestPluginData.Builder pluginDataBuilder = TestPluginData.builder();

		// add a container to collect the observed removals
		Set<PersonId> observedRemovals = new LinkedHashSet<>();
		Set<PersonId> observedImminentRemovals = new LinkedHashSet<>();
		Set<PersonId> expectedRemovals = new LinkedHashSet<>();
		for (int i = 0; i < 5; i++) {
			expectedRemovals.add(new PersonId(i));
		}

		// have the observer subscribe to the removals and record them onto the
		// observed removals
		pluginDataBuilder.addTestActorPlan("observer", new TestActorPlan(1, (c) -> {
			c.subscribe(EventFilter.builder(PersonRemovalEvent.class).build(), (c2, e) -> observedRemovals.add(e.personId()));
			c.subscribe(EventFilter.builder(PersonImminentRemovalEvent.class).build(), (c2, e) -> observedImminentRemovals.add(e.personId()));
		}));

		// have the agent add a few people
		pluginDataBuilder.addTestActorPlan("actor", new TestActorPlan(1, (c) -> {
			for (int i = 0; i < 10; i++) {
				PeopleDataManager peopleDataManager = c.getDataManager(PeopleDataManager.class);
				peopleDataManager.addPerson(PersonConstructionData.builder().build());
			}
		}));

		// have the agent remove some people
		pluginDataBuilder.addTestActorPlan("actor", new TestActorPlan(2, (c) -> {
			for (int i = 0; i < 5; i++) {
				PeopleDataManager peopleDataManager = c.getDataManager(PeopleDataManager.class);
				peopleDataManager.removePerson(new PersonId(i));
			}
		}));

		// precondition tests
		pluginDataBuilder.addTestActorPlan("actor", new TestActorPlan(3, (c) -> {

			PeopleDataManager peopleDataManager = c.getDataManager(PeopleDataManager.class);

			ContractException contractException = assertThrows(ContractException.class, () -> peopleDataManager.removePerson(null));
			assertEquals(PersonError.NULL_PERSON_ID, contractException.getErrorType());

			contractException = assertThrows(ContractException.class, () -> peopleDataManager.removePerson(new PersonId(1000)));
			assertEquals(PersonError.UNKNOWN_PERSON_ID, contractException.getErrorType());

		}));
		TestPluginData testPluginData = pluginDataBuilder.build();

		Factory factory = PeopleTestPluginFactory.factory(8330481544200634026L, testPluginData);
		TestSimulation.builder().addPlugins(factory.getPlugins()).build().execute();
		// show that the observed removals match the expected removals
		assertEquals(expectedRemovals, observedRemovals);
		assertEquals(expectedRemovals, observedImminentRemovals);
	}

	@Test
	@UnitTestConstructor(target = PeopleDataManager.class, args = { PeoplePluginData.class })
	public void testConstructor() {
		// nothing to test
	}

	@Test
	@UnitTestMethod(target = PeopleDataManager.class, name = "getPopulationCount", args = {}, tags = { UnitTag.INCOMPLETE })
	public void testGetPopulationCount() {

		/*
		 * INCOMPLETE TEST The test is incomplete since it does not start with a
		 * population present in the plugin data -- it failed to catch a bug in
		 * the people data manager's init
		 */

		Factory factory = PeopleTestPluginFactory.factory(8471595108422434117L, (c) -> {
			PeopleDataManager peopleDataManager = c.getDataManager(PeopleDataManager.class);
			// show the population count grows as we add people
			for (int i = 0; i < 10; i++) {
				assertEquals(i, peopleDataManager.getPopulationCount());
				peopleDataManager.addPerson(PersonConstructionData.builder().build());
				assertEquals(i + 1, peopleDataManager.getPopulationCount());
			}
		});
		TestSimulation.builder().addPlugins(factory.getPlugins()).build().execute();

	}

	@Test
	@UnitTestMethod(target = PeopleDataManager.class, name = "getPopulationTime", args = {})
	public void testGetPopulationTime() {
		TestPluginData.Builder pluginBuilder = TestPluginData.builder();

		pluginBuilder.addTestActorPlan("actor", new TestActorPlan(0, (c) -> {
			PeopleDataManager peopleDataManager = c.getDataManager(PeopleDataManager.class);

			assertEquals(0.0, peopleDataManager.getPopulationTime());

			// add some people
			for (int i = 0; i < 10; i++) {
				peopleDataManager.addPerson(PersonConstructionData.builder().build());
				assertEquals(c.getTime(), peopleDataManager.getPopulationTime());
			}

		}));

		pluginBuilder.addTestActorPlan("actor", new TestActorPlan(1, (c) -> {
			PeopleDataManager peopleDataManager = c.getDataManager(PeopleDataManager.class);
			peopleDataManager.removePerson(new PersonId(0));
		}));

		pluginBuilder.addTestActorPlan("actor", new TestActorPlan(1, (c) -> {
			PeopleDataManager peopleDataManager = c.getDataManager(PeopleDataManager.class);
			assertEquals(c.getTime(), peopleDataManager.getPopulationTime());
		}));

		pluginBuilder.addTestActorPlan("actor", new TestActorPlan(2, (c) -> {
			PeopleDataManager peopleDataManager = c.getDataManager(PeopleDataManager.class);
			peopleDataManager.removePerson(new PersonId(1));
		}));

		pluginBuilder.addTestActorPlan("actor", new TestActorPlan(2, (c) -> {
			PeopleDataManager peopleDataManager = c.getDataManager(PeopleDataManager.class);
			assertEquals(c.getTime(), peopleDataManager.getPopulationTime());
		}));

		TestPluginData testPluginData = pluginBuilder.build();

		Factory factory = PeopleTestPluginFactory.factory(544849633773456332L, testPluginData);
		TestSimulation.builder().addPlugins(factory.getPlugins()).build().execute();
	}

	@Test
	@UnitTestMethod(target = PeopleDataManager.class, name = "getEventFilterForPersonAdditionEvent", args = {})
	public void testGetEventFilterForPersonAdditionEvent() {
		TestPluginData.Builder pluginDataBuilder = TestPluginData.builder();

		// create containers to hold observations
		Set<PersonId> observedPersonIds = new LinkedHashSet<>();
		Set<PersonId> expectedPersonIds = new LinkedHashSet<>();

		pluginDataBuilder.addTestActorPlan("observer", new TestActorPlan(0, (c) -> {
			PeopleDataManager peopleDataManager = c.getDataManager(PeopleDataManager.class);
			EventFilter<PersonAdditionEvent> eventFilter = peopleDataManager.getEventFilterForPersonAdditionEvent();
			c.subscribe(eventFilter, (c2, e) -> observedPersonIds.add(e.personId()));
		}));

		// have the agent add a few people and show they were added
		pluginDataBuilder.addTestActorPlan("actor", new TestActorPlan(1, (c) -> {
			PeopleDataManager peopleDataManager = c.getDataManager(PeopleDataManager.class);
			for (int i = 0; i < 10; i++) {
				PersonId personId = peopleDataManager.addPerson(PersonConstructionData.builder().build());
				expectedPersonIds.add(personId);
			}
		}));

		// have the observer show that the expected and actual observations
		// match
		pluginDataBuilder.addTestActorPlan("observer", new TestActorPlan(2, (c) -> {
			assertEquals(expectedPersonIds, observedPersonIds);
		}));

		TestPluginData testPluginData = pluginDataBuilder.build();

		Factory factory = PeopleTestPluginFactory.factory(1359354206586648087L, testPluginData);
		TestSimulation.builder().addPlugins(factory.getPlugins()).build().execute();
	}

	@Test
	@UnitTestMethod(target = PeopleDataManager.class, name = "getEventFilterForPersonImminentRemovalEvent", args = {})
	public void testGetEventFilterForPersonImminentRemovalEvent() {
		TestPluginData.Builder pluginDataBuilder = TestPluginData.builder();

		// add a container to collect the observed removals
		Set<PersonId> observedRemovals = new LinkedHashSet<>();
		Set<PersonId> expectedRemovals = new LinkedHashSet<>();

		// have the observer subscribe to the removals and record them onto the
		// observed removals
		pluginDataBuilder.addTestActorPlan("observer", new TestActorPlan(1, (c) -> {
			PeopleDataManager peopleDataManager = c.getDataManager(PeopleDataManager.class);
			EventFilter<PersonImminentRemovalEvent> eventFilter = peopleDataManager.getEventFilterForPersonImminentRemovalEvent();
			c.subscribe(eventFilter, (c2, e) -> observedRemovals.add(e.personId()));
		}));

		// have the actor add a few people
		pluginDataBuilder.addTestActorPlan("actor", new TestActorPlan(2, (c) -> {
			PeopleDataManager peopleDataManager = c.getDataManager(PeopleDataManager.class);
			for (int i = 0; i < 10; i++) {
				peopleDataManager.addPerson(PersonConstructionData.builder().build());
			}
		}));

		// have the actor remove some people
		pluginDataBuilder.addTestActorPlan("actor", new TestActorPlan(3, (c) -> {
			PeopleDataManager peopleDataManager = c.getDataManager(PeopleDataManager.class);
			for (int i = 0; i < 5; i++) {
				PersonId personId = new PersonId(i);
				peopleDataManager.removePerson(personId);
				expectedRemovals.add(personId);
			}
		}));

		// have the observer show the expected and observed events are equal
		pluginDataBuilder.addTestActorPlan("observer", new TestActorPlan(4, (c) -> {
			assertFalse(expectedRemovals.isEmpty());
			assertEquals(expectedRemovals, observedRemovals);
		}));

		TestPluginData testPluginData = pluginDataBuilder.build();

		Factory factory = PeopleTestPluginFactory.factory(3387041999627132151L, testPluginData);
		TestSimulation.builder().addPlugins(factory.getPlugins()).build().execute();

	}

	/**
	 * Demonstrates that the data manager exhibits run continuity. The state of
	 * the data manager is not effected by repeatedly starting and stopping the
	 * simulation.
	 */
	@Test
	@UnitTestMethod(target = PeopleDataManager.class, name = "init", args = { DataManagerContext.class })
	public void testStateContinuity() {

		/*
		 * Note that we are not testing the content of the plugin datas -- that
		 * is covered by the other state tests. We show here only that the
		 * resulting plugin data state is the same without regard to how we
		 * break up the run.
		 */

		Set<String> pluginDatas = new LinkedHashSet<>();
		pluginDatas.add(testStateContinuity(1));
		pluginDatas.add(testStateContinuity(5));
		pluginDatas.add(testStateContinuity(10));

		assertEquals(1, pluginDatas.size());

	}

	/*
	 * Returns the people plugin data resulting from several people events over
	 * several days. Attempts to stop and start the simulation by the given
	 * number of increments.
	 */
	private String testStateContinuity(int incrementCount) {
		String result = null;

		/*
		 * Build the RunContinuityPluginData with five context consumers that
		 * will add and remove people over several days
		 */
		RunContinuityPluginData.Builder continuityBuilder = RunContinuityPluginData.builder();

		continuityBuilder.addContextConsumer(0.5, (c) -> {
			PeopleDataManager peopleDataManager = c.getDataManager(PeopleDataManager.class);
			for (int i = 0; i < 3; i++) {
				peopleDataManager.addPerson(PersonConstructionData.builder().build());
			}
		});

		continuityBuilder.addContextConsumer(1.2, (c) -> {
			PeopleDataManager peopleDataManager = c.getDataManager(PeopleDataManager.class);
			peopleDataManager.removePerson(new PersonId(0));
			for (int i = 0; i < 3; i++) {
				peopleDataManager.addPerson(PersonConstructionData.builder().build());
			}
		});

		continuityBuilder.addContextConsumer(1.8, (c) -> {
			PeopleDataManager peopleDataManager = c.getDataManager(PeopleDataManager.class);
			peopleDataManager.removePerson(new PersonId(3));
			peopleDataManager.removePerson(new PersonId(4));
			for (int i = 0; i < 5; i++) {
				peopleDataManager.addPerson(PersonConstructionData.builder().build());
			}
		});

		continuityBuilder.addContextConsumer(2.05, (c) -> {
			PeopleDataManager peopleDataManager = c.getDataManager(PeopleDataManager.class);
			peopleDataManager.removePerson(new PersonId(1));
			peopleDataManager.removePerson(new PersonId(6));
			peopleDataManager.removePerson(new PersonId(10));

			for (int i = 0; i < 3; i++) {
				peopleDataManager.addPerson(PersonConstructionData.builder().build());
			}
		});

		continuityBuilder.addContextConsumer(4.2, (c) -> {
			PeopleDataManager peopleDataManager = c.getDataManager(PeopleDataManager.class);

			for (int i = 0; i < 3; i++) {
				peopleDataManager.addPerson(PersonConstructionData.builder().build());
			}

			c.releaseOutput(peopleDataManager.toString());
		});

		RunContinuityPluginData runContinuityPluginData = continuityBuilder.build();

		// Build an empty people plugin data for time zero
		PeoplePluginData peoplePluginData = PeoplePluginData.builder().build();

		// build the initial simulation state data -- time starts at zero
		SimulationState simulationState = SimulationState.builder().build();

		/*
		 * Run the simulation in one day increments until all the plans in the
		 * run continuity plugin data have been executed
		 */
		double haltTime = 0;
		double maxTime = Double.NEGATIVE_INFINITY;
		for (Pair<Double, Consumer<ActorContext>> pair : runContinuityPluginData.getConsumers()) {
			Double time = pair.getFirst();
			maxTime = FastMath.max(maxTime, time);
		}
		double timeIncrement = maxTime / incrementCount;
		while (!runContinuityPluginData.allPlansComplete()) {
			haltTime += timeIncrement;

			// build the run continuity plugin
			Plugin runContinuityPlugin = RunContinuityPlugin.builder()//
															.setRunContinuityPluginData(runContinuityPluginData)//
															.build();

			// build the people plugin
			Plugin peoplePlugin = PeoplePlugin.getPeoplePlugin(peoplePluginData);

			TestOutputConsumer outputConsumer = new TestOutputConsumer();

			// execute the simulation so that it produces a people plugin data
			Simulation simulation = Simulation	.builder()//
												.addPlugin(peoplePlugin)//
												.addPlugin(runContinuityPlugin)//
												.setSimulationHaltTime(haltTime)//
												.setRecordState(true)//
												.setOutputConsumer(outputConsumer)//
												.setSimulationState(simulationState)//
												.build();//
			simulation.execute();

			// retrieve the people plugin data
			peoplePluginData = outputConsumer.getOutputItem(PeoplePluginData.class).get();

			// retrieve the simulation state
			simulationState = outputConsumer.getOutputItem(SimulationState.class).get();

			// retrieve the run continuity plugin data
			runContinuityPluginData = outputConsumer.getOutputItem(RunContinuityPluginData.class).get();
			
			Optional<String> optional = outputConsumer.getOutputItem(String.class);
			if(optional.isPresent()) {
				result = optional.get();
			}
			
		}

		assertNotNull(result);
		return result;

	}

}
