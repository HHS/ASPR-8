package plugins.people;

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

import annotations.UnitTest;
import annotations.UnitTestConstructor;
import annotations.UnitTestMethod;
import nucleus.DataManagerContext;
import nucleus.EventLabeler;
import nucleus.NucleusError;
import nucleus.Plugin;
import nucleus.testsupport.testplugin.TestActorPlan;
import nucleus.testsupport.testplugin.TestPlugin;
import nucleus.testsupport.testplugin.TestPluginData;
import nucleus.util.ContractException;
import plugins.people.events.BulkPersonCreationObservationEvent;
import plugins.people.events.PersonCreationObservationEvent;
import plugins.people.events.PersonImminentRemovalObservationEvent;
import plugins.people.support.BulkPersonConstructionData;
import plugins.people.support.PersonConstructionData;
import plugins.people.support.PersonError;
import plugins.people.support.PersonId;
import plugins.people.testsupport.PeopleActionSupport;

@UnitTest(target = PersonDataManager.class)
public final class AT_PersonDataManager {

	@Test
	@UnitTestMethod(name = "personIndexExists", args = { int.class })
	public void testPersonIndexExists() {

		PeopleActionSupport.testConsumer((c) -> {
			PersonDataManager personDataManager = c.getDataManager(PersonDataManager.class).get();

			// initially there are no people despite the initial size
			assertFalse(personDataManager.personIndexExists(-1));
			assertFalse(personDataManager.personIndexExists(0));
			assertFalse(personDataManager.personIndexExists(1));

			// show that we can add a few people and for each the manager will
			// indicate that they exist
			for (int i = 0; i < 10; i++) {
				PersonId personId = personDataManager.addPerson(PersonConstructionData.builder().build());
				assertTrue(personDataManager.personIndexExists(personId.getValue()));
			}

			// show that people who should not exist, actually don't exist
			assertFalse(personDataManager.personIndexExists(-1));
			for (int i = 10; i < 20; i++) {
				assertFalse(personDataManager.personIndexExists(i));
			}

		});

	}

	@Test
	@UnitTestMethod(name = "getPersonIdLimit", args = {})
	public void testGetPersonIdLimit() {
		PeopleActionSupport.testConsumer((c) -> {
			PersonDataManager personDataManager = c.getDataManager(PersonDataManager.class).get();
			/*
			 * Initially there are no people despite the initial size, so we
			 * expect the limit to be zero.
			 */
			assertEquals(0, personDataManager.getPersonIdLimit());

			// show that the limit increments as PersonId values are added
			for (int i = 0; i < 10; i++) {
				PersonId personId = personDataManager.addPerson(PersonConstructionData.builder().build());
				assertEquals(personId.getValue() + 1, personDataManager.getPersonIdLimit());
			}

		});

	}

	@Test
	@UnitTestMethod(name = "getBoxedPersonId", args = { int.class })
	public void testGetBoxedPersonId() {
		PeopleActionSupport.testConsumer((c) -> {
			PersonDataManager personDataManager = c.getDataManager(PersonDataManager.class).get();
			// show that the boxed person id is correct
			for (int i = 0; i < 10; i++) {
				Optional<PersonId> optional = personDataManager.getBoxedPersonId(i);
				assertFalse(optional.isPresent());

				PersonId personId = personDataManager.addPerson(PersonConstructionData.builder().build());
				optional = personDataManager.getBoxedPersonId(i);
				assertTrue(optional.isPresent());

				PersonId boxedPersonId = optional.get();
				assertEquals(personId, boxedPersonId);
			}
		});

	}

	@Test
	@UnitTestMethod(name = "addPerson", args = { PersonConstructionData.class })
	public void testaddPerson() {

		TestPluginData.Builder pluginDataBuilder = TestPluginData.builder();

		// create containers to hold observations
		Set<PersonId> observedPersonIds = new LinkedHashSet<>();
		Set<PersonId> expectedPersonIds = new LinkedHashSet<>();

		for (int i = 0; i < 10; i++) {
			PersonId personId = new PersonId(i);
			expectedPersonIds.add(personId);
		}

		pluginDataBuilder.addTestActorPlan("observer", new TestActorPlan(0, (c) -> {
			c.subscribe(PersonCreationObservationEvent.getEventLabel(), (c2, e) -> observedPersonIds.add(e.getPersonId()));
		}));

		// have the agent add a few people and show they were added
		pluginDataBuilder.addTestActorPlan("agent", new TestActorPlan(1, (c) -> {
			PersonDataManager personDataManager = c.getDataManager(PersonDataManager.class).get();
			for (PersonId expectedPersonId : expectedPersonIds) {
				PersonId actualPersonId = personDataManager.addPerson(PersonConstructionData.builder().build());
				assertEquals(expectedPersonId, actualPersonId);
				assertTrue(personDataManager.personExists(actualPersonId));
			}
		}));

		// precondition tests
		pluginDataBuilder.addTestActorPlan("agent", new TestActorPlan(2, (c) -> {
			PersonDataManager personDataManager = c.getDataManager(PersonDataManager.class).get();
			ContractException contractException = assertThrows(ContractException.class, () -> personDataManager.addPerson(null));
			assertEquals(PersonError.NULL_PERSON_CONSTRUCTION_DATA, contractException.getErrorType());
		}));

		TestPluginData testPluginData = pluginDataBuilder.build();
		Plugin plugin = TestPlugin.getPlugin(testPluginData);

		PeopleActionSupport.testConsumers(plugin);

		// show that the expected and acutual observations match
		assertEquals(expectedPersonIds, observedPersonIds);
	}

	@Test
	@UnitTestMethod(name = "addBulkPeople", args = { BulkPersonConstructionData.class })
	public void testBulkPersonCreationEvent() {
		TestPluginData.Builder pluginDataBuilder = TestPluginData.builder();

		// create containers to hold observations
		Set<BulkPersonConstructionData> observedBulkPersonConstructionData = new LinkedHashSet<>();
		Set<BulkPersonConstructionData> expectedBulkPersonConstructionData = new LinkedHashSet<>();

		// generate three BulkPersonConstructionData instances that contain
		// unique data
		int uniqueId = 0;
		for (int i = 0; i < 3; i++) {
			BulkPersonConstructionData.Builder bulkBuilder = BulkPersonConstructionData.builder();
			for (int j = 0; j < 10; j++) {
				PersonConstructionData personConstructionData = PersonConstructionData.builder().add(uniqueId++).build();
				bulkBuilder.add(personConstructionData);
			}
			expectedBulkPersonConstructionData.add(bulkBuilder.build());
		}

		pluginDataBuilder.addTestActorPlan("observer", new TestActorPlan(0, (c) -> {
			c.subscribe(BulkPersonCreationObservationEvent.getEventLabel(), (c2, e) -> observedBulkPersonConstructionData.add(e.getBulkPersonConstructionData()));
		}));

		// have the agent add a bulk people and show the people were added
		pluginDataBuilder.addTestActorPlan("agent", new TestActorPlan(1, (c) -> {

			PersonDataManager personDataManager = c.getDataManager(PersonDataManager.class).get();
			for (BulkPersonConstructionData bulkPersonConstructionData : expectedBulkPersonConstructionData) {
				List<PersonId> priorPeople = personDataManager.getPeople();
				personDataManager.addBulkPeople(bulkPersonConstructionData);

				List<PersonId> postPeople = personDataManager.getPeople();
				postPeople.removeAll(priorPeople);
				int expectedNewPeople = bulkPersonConstructionData.getPersonConstructionDatas().size();
				assertEquals(expectedNewPeople, postPeople.size());
			}
		}));

		// precondition tests
		pluginDataBuilder.addTestActorPlan("agent", new TestActorPlan(2, (c) -> {
			PersonDataManager personDataManager = c.getDataManager(PersonDataManager.class).get();
			ContractException contractException = assertThrows(ContractException.class, () -> personDataManager.addBulkPeople(null));
			assertEquals(PersonError.NULL_BULK_PERSON_CONSTRUCTION_DATA, contractException.getErrorType());
		}));

		TestPluginData testPluginData = pluginDataBuilder.build();
		Plugin plugin = TestPlugin.getPlugin(testPluginData);

		PeopleActionSupport.testConsumers(plugin);

		// show that the expected and acutual observations match
		assertEquals(expectedBulkPersonConstructionData, observedBulkPersonConstructionData);
	}

	@Test
	@UnitTestMethod(name = "personExists", args = { PersonId.class })
	public void testPersonExists() {

		PeopleActionSupport.testConsumer((c) -> {
			PersonDataManager personDataManager = c.getDataManager(PersonDataManager.class).get();

			for (int i = 0; i < 10; i++) {
				personDataManager.addPerson(PersonConstructionData.builder().build());
			}

			assertFalse(personDataManager.personExists(new PersonId(-1)));

			for (int i = 0; i < 10; i++) {
				assertTrue(personDataManager.personExists(new PersonId(i)));
			}

			for (int i = 10; i < 20; i++) {
				assertFalse(personDataManager.personExists(new PersonId(i)));
			}

		});

	}

	@Test
	@UnitTestMethod(name = "getPeople", args = {})
	public void testGetPeople() {

		List<PersonId> expectedPeople = new ArrayList<>();

		TestPluginData.Builder pluginBuilder = TestPluginData.builder();

		pluginBuilder.addTestActorPlan("actor", new TestActorPlan(0, (c) -> {
			PersonDataManager personDataManager = c.getDataManager(PersonDataManager.class).get();

			// add some people

			for (int i = 0; i < 10; i++) {
				PersonId personId = personDataManager.addPerson(PersonConstructionData.builder().build());
				expectedPeople.add(personId);
			}

			List<PersonId> actualPeople = personDataManager.getPeople();
			assertEquals(expectedPeople.size(), actualPeople.size());
			assertEquals(new LinkedHashSet<>(expectedPeople), new LinkedHashSet<>(actualPeople));

		}));

		pluginBuilder.addTestActorPlan("actor", new TestActorPlan(1, (c) -> {
			PersonDataManager personDataManager = c.getDataManager(PersonDataManager.class).get();

			// remove a few people
			for (int i = 0; i < 5; i++) {
				PersonId personId = new PersonId(i);
				personDataManager.removePerson(personId);
				expectedPeople.remove(personId);
			}

		}));

		pluginBuilder.addTestActorPlan("actor", new TestActorPlan(2, (c) -> {
			PersonDataManager personDataManager = c.getDataManager(PersonDataManager.class).get();
			// show that the removals resulted in the correct people
			List<PersonId> actualPeople = personDataManager.getPeople();
			assertEquals(expectedPeople.size(), actualPeople.size());
			assertEquals(new LinkedHashSet<>(expectedPeople), new LinkedHashSet<>(actualPeople));
		}));

		TestPluginData testPluginData = pluginBuilder.build();
		Plugin testPlugin = TestPlugin.getPlugin(testPluginData);

		PeopleActionSupport.testConsumers(testPlugin);
	}

	@Test
	@UnitTestMethod(name = "removePerson", args = { PersonId.class })
	public void testRemovePerson() {

		TestPluginData.Builder pluginDataBuilder = TestPluginData.builder();

		// add a container to collect the observed removals
		Set<PersonId> observedRemovals = new LinkedHashSet<>();
		Set<PersonId> expectedRemovals = new LinkedHashSet<>();
		for (int i = 0; i < 5; i++) {
			expectedRemovals.add(new PersonId(i));
		}

		// have the observer subscribe to the removals and record them onto the
		// observed removals
		pluginDataBuilder.addTestActorPlan("observer", new TestActorPlan(1, (c) -> {
			c.subscribe(PersonImminentRemovalObservationEvent.getEventLabel(), (c2, e) -> observedRemovals.add(e.getPersonId()));
		}));

		// have the agent add a few people
		pluginDataBuilder.addTestActorPlan("agent", new TestActorPlan(1, (c) -> {
			for (int i = 0; i < 10; i++) {
				PersonDataManager personDataManager = c.getDataManager(PersonDataManager.class).get();
				personDataManager.addPerson(PersonConstructionData.builder().build());
			}
		}));

		// have the agent remove some people
		pluginDataBuilder.addTestActorPlan("agent", new TestActorPlan(2, (c) -> {
			for (int i = 0; i < 5; i++) {
				PersonDataManager personDataManager = c.getDataManager(PersonDataManager.class).get();
				personDataManager.removePerson(new PersonId(i));
			}
		}));

		// precondition tests
		pluginDataBuilder.addTestActorPlan("agent", new TestActorPlan(3, (c) -> {

			PersonDataManager personDataManager = c.getDataManager(PersonDataManager.class).get();

			ContractException contractException = assertThrows(ContractException.class, () -> personDataManager.removePerson(null));
			assertEquals(PersonError.NULL_PERSON_ID, contractException.getErrorType());

			contractException = assertThrows(ContractException.class, () -> personDataManager.removePerson(new PersonId(1000)));
			assertEquals(PersonError.UNKNOWN_PERSON_ID, contractException.getErrorType());

		}));
		TestPluginData testPluginData = pluginDataBuilder.build();
		Plugin testPlugin = TestPlugin.getPlugin(testPluginData);

		PeopleActionSupport.testConsumers(testPlugin);

		// show that the observed removals match the expected removals
		assertEquals(expectedRemovals, observedRemovals);
	}

	@Test
	@UnitTestConstructor(args = { PeoplePluginData.class })
	public void testConstructor() {
		// nothing to test
	}

	@Test
	@UnitTestMethod(name = "expandCapacity", args = { int.class })
	public void testExpandCapacity() {
		PeopleActionSupport.testConsumer((c) -> {
			// show that a negative growth causes an exception
			PersonDataManager personDataManager = c.getDataManager(PersonDataManager.class).get();
			ContractException contractException = assertThrows(ContractException.class, () -> personDataManager.expandCapacity(-1));
			assertEquals(PersonError.NEGATIVE_GROWTH_PROJECTION, contractException.getErrorType());
		});

		// use manual tests for non-negative growth
	}

	@Test
	@UnitTestMethod(name = "getPopulationCount", args = {})
	public void testGetPopulationCount() {

		PeopleActionSupport.testConsumer((c) -> {
			PersonDataManager personDataManager = c.getDataManager(PersonDataManager.class).get();
			// show the population count grows as we add people
			for (int i = 0; i < 10; i++) {
				assertEquals(i, personDataManager.getPopulationCount());
				personDataManager.addPerson(PersonConstructionData.builder().build());
				assertEquals(i + 1, personDataManager.getPopulationCount());
			}
		});

	}

	@Test
	@UnitTestMethod(name = "getProjectedPopulationCount", args = {})
	public void testGetProjectedPopulationCount() {

		PeopleActionSupport.testConsumer((c) -> {

			PersonDataManager personDataManager = c.getDataManager(PersonDataManager.class).get();

			assertEquals(0, personDataManager.getProjectedPopulationCount());

			/*
			 * Add a few people so we are not working from a zero-base and show
			 * the projected population count is correct
			 */
			for (int i = 0; i < 10; i++) {
				personDataManager.addPerson(PersonConstructionData.builder().build());
			}
			assertEquals(10, personDataManager.getProjectedPopulationCount());

			// show that expanding the capacity results in the correct projected
			// count
			personDataManager.expandCapacity(30);
			assertEquals(40, personDataManager.getProjectedPopulationCount());

			/*
			 * show that adding people will not change the projected population
			 * count until the actual population catches up
			 */
			for (int i = 0; i < 100; i++) {
				personDataManager.addPerson(PersonConstructionData.builder().build());
				int expectedValue = FastMath.max(40, personDataManager.getPopulationCount());
				assertEquals(expectedValue, personDataManager.getProjectedPopulationCount());
			}

			// show that expanding multiple times works as well
			personDataManager.expandCapacity(100);
			assertEquals(210, personDataManager.getProjectedPopulationCount());

			personDataManager.expandCapacity(100);
			assertEquals(310, personDataManager.getProjectedPopulationCount());
		});
	}

	@Test
	@UnitTestMethod(name = "getPopulationTime", args = {})
	public void testGetPopulationTime() {
		TestPluginData.Builder pluginBuilder = TestPluginData.builder();

		pluginBuilder.addTestActorPlan("actor", new TestActorPlan(0, (c) -> {
			PersonDataManager personDataManager = c.getDataManager(PersonDataManager.class).get();

			assertEquals(0.0, personDataManager.getPopulationTime());

			// add some people
			for (int i = 0; i < 10; i++) {
				personDataManager.addPerson(PersonConstructionData.builder().build());
				assertEquals(c.getTime(), personDataManager.getPopulationTime());
			}

		}));

		pluginBuilder.addTestActorPlan("actor", new TestActorPlan(1, (c) -> {
			PersonDataManager personDataManager = c.getDataManager(PersonDataManager.class).get();
			personDataManager.removePerson(new PersonId(0));
		}));

		pluginBuilder.addTestActorPlan("actor", new TestActorPlan(1, (c) -> {
			PersonDataManager personDataManager = c.getDataManager(PersonDataManager.class).get();
			assertEquals(c.getTime(), personDataManager.getPopulationTime());
		}));

		pluginBuilder.addTestActorPlan("actor", new TestActorPlan(2, (c) -> {
			PersonDataManager personDataManager = c.getDataManager(PersonDataManager.class).get();
			personDataManager.removePerson(new PersonId(1));
		}));

		pluginBuilder.addTestActorPlan("actor", new TestActorPlan(2, (c) -> {
			PersonDataManager personDataManager = c.getDataManager(PersonDataManager.class).get();
			assertEquals(c.getTime(), personDataManager.getPopulationTime());
		}));

		TestPluginData testPluginData = pluginBuilder.build();
		Plugin testPlugin = TestPlugin.getPlugin(testPluginData);

		PeopleActionSupport.testConsumers(testPlugin);
	}

	@Test
	@UnitTestMethod(name = "init", args = { DataManagerContext.class })
	public void testPersonCreationObservationEventLabelers() {
		EventLabeler<PersonCreationObservationEvent> eventLabeler = PersonCreationObservationEvent.getEventLabeler();
		PeopleActionSupport.testConsumer((c) -> {
			ContractException contractException = assertThrows(ContractException.class, () -> c.addEventLabeler(eventLabeler));
			assertEquals(NucleusError.DUPLICATE_LABELER_ID_IN_EVENT_LABELER, contractException.getErrorType());
		});
	}

	@Test
	@UnitTestMethod(name = "init", args = { DataManagerContext.class })
	public void testPersonImminentRemovalObservationEventLabelers() {
		EventLabeler<PersonImminentRemovalObservationEvent> eventLabeler = PersonImminentRemovalObservationEvent.getEventLabeler();
		PeopleActionSupport.testConsumer((c) -> {
			ContractException contractException = assertThrows(ContractException.class, () -> c.addEventLabeler(eventLabeler));
			assertEquals(NucleusError.DUPLICATE_LABELER_ID_IN_EVENT_LABELER, contractException.getErrorType());
		});
	}

	@Test
	@UnitTestMethod(name = "init", args = { DataManagerContext.class })
	public void testBulkPersonCreationObservationEventLabelers() {
		EventLabeler<BulkPersonCreationObservationEvent> eventLabeler = BulkPersonCreationObservationEvent.getEventLabeler();
		PeopleActionSupport.testConsumer((c) -> {
			ContractException contractException = assertThrows(ContractException.class, () -> c.addEventLabeler(eventLabeler));
			assertEquals(NucleusError.DUPLICATE_LABELER_ID_IN_EVENT_LABELER, contractException.getErrorType());
		});
	}

}
