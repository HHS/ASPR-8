package plugins.people.datamanagers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.apache.commons.math3.util.FastMath;
import org.junit.jupiter.api.Test;

import nucleus.DataManagerContext;
import nucleus.EventFilter;
import nucleus.testsupport.testplugin.TestActorPlan;
import nucleus.testsupport.testplugin.TestPluginData;
import nucleus.testsupport.testplugin.TestSimulation;
import plugins.people.PeoplePluginData;
import plugins.people.events.PersonAdditionEvent;
import plugins.people.events.PersonImminentAdditionEvent;
import plugins.people.events.PersonImminentRemovalEvent;
import plugins.people.events.PersonRemovalEvent;
import plugins.people.support.PersonConstructionData;
import plugins.people.support.PersonError;
import plugins.people.support.PersonId;
import plugins.people.testsupport.PeopleTestPluginFactory;
import plugins.people.testsupport.PeopleTestPluginFactory.Factory;
import util.annotations.UnitTestConstructor;
import util.annotations.UnitTestMethod;
import util.errors.ContractException;

public final class AT_PeopleDataManager {

	// the initial data is not being used correctly and will lead to errors,
	// this is due to there not being a test for init(). That test will need to
	// demonstrate that a non-contiguous set of person id values will work.

	// init(DataManagerContext)

	@Test
	@UnitTestMethod(target = PeopleDataManager.class, name = "init", args = { DataManagerContext.class })
	public void testInit() {

		// add a few people with gaps between id values
		int numberOfPeople = 5;
		PeoplePluginData.Builder peoplePluginDataBuilder = PeoplePluginData.builder();
		Set<PersonId> expectedPersonIds = new LinkedHashSet<>();
		for (int i = 0; i < numberOfPeople; i++) {
			PersonId personId = new PersonId(i * 3 + 10);
			peoplePluginDataBuilder.addPersonId(personId);
			expectedPersonIds.add(personId);
		}

		PeoplePluginData peoplePluginData = peoplePluginDataBuilder.build();

		// add an actor to test the people were properly loaded into the person
		// data manger
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

		Factory factory = PeopleTestPluginFactory.factory(6970812715559334185L, testPluginData).setPeoplePluginData(peoplePluginData);
		TestSimulation.builder().addPlugins(factory.getPlugins()).build().execute();

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
			c.subscribe(EventFilter.builder(PersonImminentAdditionEvent.class).build(),
					(c2, e) -> observedImminentPersonIds.add(e.personId()));
			c.subscribe(EventFilter.builder(PersonAdditionEvent.class).build(),
					(c2, e) -> observedPersonIds.add(e.personId()));
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
			ContractException contractException = assertThrows(ContractException.class,
					() -> peopleDataManager.addPerson(null));
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
			c.subscribe(EventFilter.builder(PersonRemovalEvent.class).build(),
					(c2, e) -> observedRemovals.add(e.personId()));
			c.subscribe(EventFilter.builder(PersonImminentRemovalEvent.class).build(),
					(c2, e) -> observedImminentRemovals.add(e.personId()));
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

			ContractException contractException = assertThrows(ContractException.class,
					() -> peopleDataManager.removePerson(null));
			assertEquals(PersonError.NULL_PERSON_ID, contractException.getErrorType());

			contractException = assertThrows(ContractException.class,
					() -> peopleDataManager.removePerson(new PersonId(1000)));
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
	@UnitTestMethod(target = PeopleDataManager.class, name = "expandCapacity", args = { int.class })
	public void testExpandCapacity() {
		Factory factory = PeopleTestPluginFactory.factory(2579559197218306247L, (c) -> {
			// show that a negative growth causes an exception
			PeopleDataManager peopleDataManager = c.getDataManager(PeopleDataManager.class);
			ContractException contractException = assertThrows(ContractException.class,
					() -> peopleDataManager.expandCapacity(-1));
			assertEquals(PersonError.NEGATIVE_GROWTH_PROJECTION, contractException.getErrorType());
		});
		TestSimulation.builder().addPlugins(factory.getPlugins()).build().execute();

		// use manual tests for non-negative growth
	}

	@Test
	@UnitTestMethod(target = PeopleDataManager.class, name = "getPopulationCount", args = {})
	public void testGetPopulationCount() {

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
	@UnitTestMethod(target = PeopleDataManager.class, name = "getProjectedPopulationCount", args = {})
	public void testGetProjectedPopulationCount() {

		Factory factory = PeopleTestPluginFactory.factory(1779635024257337287L, (c) -> {

			PeopleDataManager peopleDataManager = c.getDataManager(PeopleDataManager.class);

			assertEquals(0, peopleDataManager.getProjectedPopulationCount());

			/*
			 * Add a few people, so we are not working from a zero-base and show
			 * the projected population count is correct
			 */
			for (int i = 0; i < 10; i++) {
				peopleDataManager.addPerson(PersonConstructionData.builder().build());
			}
			assertEquals(10, peopleDataManager.getProjectedPopulationCount());

			// show that expanding the capacity results in the correct projected
			// count
			peopleDataManager.expandCapacity(30);
			assertEquals(40, peopleDataManager.getProjectedPopulationCount());

			/*
			 * show that adding people will not change the projected population
			 * count until the actual population catches up
			 */
			for (int i = 0; i < 100; i++) {
				peopleDataManager.addPerson(PersonConstructionData.builder().build());
				int expectedValue = FastMath.max(40, peopleDataManager.getPopulationCount());
				assertEquals(expectedValue, peopleDataManager.getProjectedPopulationCount());
			}

			// show that expanding multiple times works as well
			peopleDataManager.expandCapacity(100);
			assertEquals(210, peopleDataManager.getProjectedPopulationCount());

			peopleDataManager.expandCapacity(100);
			assertEquals(310, peopleDataManager.getProjectedPopulationCount());
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
			EventFilter<PersonImminentRemovalEvent> eventFilter = peopleDataManager
					.getEventFilterForPersonImminentRemovalEvent();
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

}
