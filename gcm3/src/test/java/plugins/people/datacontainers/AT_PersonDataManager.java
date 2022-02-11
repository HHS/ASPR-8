package plugins.people.datacontainers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.math3.random.RandomGenerator;
import org.apache.commons.math3.util.FastMath;
import org.junit.jupiter.api.Test;

import nucleus.Context;
import nucleus.NucleusError;
import nucleus.testsupport.MockContext;
import plugins.people.support.PersonError;
import plugins.people.support.PersonId;
import util.ContractException;
import util.MutableDouble;
import util.SeedProvider;
import util.annotations.UnitTest;
import util.annotations.UnitTestConstructor;
import util.annotations.UnitTestMethod;

@UnitTest(target = PersonDataManager.class)
public final class AT_PersonDataManager {

	@Test
	@UnitTestMethod(name = "personIndexExists", args = { int.class })
	public void testPersonIndexExists() {
		MockContext mockContext = MockContext.builder().build();
		PersonDataManager personDataManager = new PersonDataManager(mockContext, 10);

		// initially there are no people despite the initial size
		assertFalse(personDataManager.personIndexExists(-1));
		assertFalse(personDataManager.personIndexExists(0));
		assertFalse(personDataManager.personIndexExists(1));

		// show that we can add a few people and for each the manager will
		// indicate that they exist
		for (int i = 0; i < 10; i++) {
			PersonId personId = personDataManager.addPersonId();
			assertTrue(personDataManager.personIndexExists(personId.getValue()));
		}

		// show that people who should not exist, actually don't exist
		assertFalse(personDataManager.personIndexExists(-1));
		for (int i = 10; i < 20; i++) {
			assertFalse(personDataManager.personIndexExists(i));
		}

	}

	@Test
	@UnitTestMethod(name = "getPersonIdLimit", args = {})
	public void testGetPersonIdLimit() {
		MockContext mockContext = MockContext.builder().build();
		PersonDataManager personDataManager = new PersonDataManager(mockContext, 10);

		// initially there are no people despite the initial size, so we expect
		// the limit to be zero.
		assertEquals(0, personDataManager.getPersonIdLimit());

		// show that the limit increments as PersonId values are added
		for (int i = 0; i < 10; i++) {
			PersonId personId = personDataManager.addPersonId();
			assertEquals(personId.getValue() + 1, personDataManager.getPersonIdLimit());
		}
	}

	@Test
	@UnitTestMethod(name = "getBoxedPersonId", args = { int.class })
	public void testGetBoxedPersonId() {
		MockContext mockContext = MockContext.builder().build();
		PersonDataManager personDataManager = new PersonDataManager(mockContext, 10);

		// show that the boxed person id is correct
		for (int i = 0; i < 10; i++) {
			PersonId personId = personDataManager.addPersonId();
			PersonId boxedPersonId = personDataManager.getBoxedPersonId(i);
			assertEquals(personId, boxedPersonId);
		}

		// precondition checks

		// if the person does not exist
		ContractException contractException = assertThrows(ContractException.class, () -> personDataManager.getBoxedPersonId(-1));
		assertEquals(PersonError.UNKNOWN_PERSON_ID, contractException.getErrorType());

	}

	@Test
	@UnitTestMethod(name = "addPersonId", args = {})
	public void testAddPersonId() {
		MockContext mockContext = MockContext.builder().build();
		PersonDataManager personDataManager = new PersonDataManager(mockContext, 10);

		// show the person id returned has the expected value
		for (int i = 0; i < 10; i++) {
			int expectedValue = personDataManager.getPersonIdLimit();
			PersonId personId = personDataManager.addPersonId();
			assertEquals(expectedValue, personId.getValue());
		}
	}

	@Test
	@UnitTestMethod(name = "personExists", args = { PersonId.class })
	public void testPersonExists() {
		MockContext mockContext = MockContext.builder().build();
		PersonDataManager personDataManager = new PersonDataManager(mockContext, 10);

		for (int i = 0; i < 10; i++) {
			personDataManager.addPersonId();
		}

		assertFalse(personDataManager.personExists(new PersonId(-1)));

		for (int i = 0; i < 10; i++) {
			assertTrue(personDataManager.personExists(new PersonId(i)));
		}

		for (int i = 10; i < 20; i++) {
			assertFalse(personDataManager.personExists(new PersonId(i)));
		}
	}

	@Test
	@UnitTestMethod(name = "getPeople", args = {})
	public void testGetPeople() {
		MockContext mockContext = MockContext.builder().build();
		PersonDataManager personDataManager = new PersonDataManager(mockContext, 10);

		// add some people
		List<PersonId> expectedPeople = new ArrayList<>();
		for (int i = 0; i < 10; i++) {
			PersonId personId = personDataManager.addPersonId();
			expectedPeople.add(personId);
		}

		List<PersonId> actualPeople = personDataManager.getPeople();
		assertEquals(expectedPeople.size(), actualPeople.size());
		assertEquals(new LinkedHashSet<>(expectedPeople), new LinkedHashSet<>(actualPeople));

		// remove a few people
		for (int i = 0; i < 5; i++) {
			PersonId personId = new PersonId(i);
			personDataManager.removePerson(personId);
			expectedPeople.remove(personId);
		}

		// show that the removals resulted in the correct people
		actualPeople = personDataManager.getPeople();
		assertEquals(expectedPeople.size(), actualPeople.size());
		assertEquals(new LinkedHashSet<>(expectedPeople), new LinkedHashSet<>(actualPeople));

	}

	@Test
	@UnitTestMethod(name = "removePerson", args = { PersonId.class })
	public void testRemovePerson() {
		MockContext mockContext = MockContext.builder().build();
		PersonDataManager personDataManager = new PersonDataManager(mockContext, 10);

		// add some people
		List<PersonId> expectedPeople = new ArrayList<>();
		for (int i = 0; i < 10; i++) {
			PersonId personId = personDataManager.addPersonId();
			expectedPeople.add(personId);
		}

		List<PersonId> actualPeople = personDataManager.getPeople();
		assertEquals(expectedPeople.size(), actualPeople.size());
		assertEquals(new LinkedHashSet<>(expectedPeople), new LinkedHashSet<>(actualPeople));

		// remove a few people
		for (int i = 0; i < 5; i++) {
			PersonId personId = new PersonId(i);
			personDataManager.removePerson(personId);
			expectedPeople.remove(personId);
		}

		// show that the removals resulted in the correct people
		actualPeople = personDataManager.getPeople();
		assertEquals(expectedPeople.size(), actualPeople.size());
		assertEquals(new LinkedHashSet<>(expectedPeople), new LinkedHashSet<>(actualPeople));

		// precondition checks

		// if the person id is null
		ContractException contractException = assertThrows(ContractException.class, () -> personDataManager.removePerson(null));
		assertEquals(PersonError.NULL_PERSON_ID, contractException.getErrorType());

		// if the person id is unknown
		contractException = assertThrows(ContractException.class, () -> personDataManager.removePerson(new PersonId(100)));
		assertEquals(PersonError.UNKNOWN_PERSON_ID, contractException.getErrorType());

	}

	@Test
	@UnitTestConstructor(args = { Context.class, int.class })
	public void testTestConstructor() {
		MockContext mockContext = MockContext.builder().build();

		// precondition tests

		// if the context is null
		ContractException contractException = assertThrows(ContractException.class, () -> new PersonDataManager(null, 10));
		assertEquals(NucleusError.NULL_CONTEXT, contractException.getErrorType());

		// if the initial capacity is negative
		assertThrows(IllegalArgumentException.class, () -> new PersonDataManager(mockContext, -1));

	}

	@Test
	@UnitTestMethod(name = "expandCapacity", args = { int.class })
	public void testExpandCapacity() {
		// nothing to test
		// needs to be tested manually under performance testing
	}

	@Test
	@UnitTestMethod(name = "getLastIssuedPersonId", args = {})
	public void testGetLastIssuedPersonId() {
		MockContext mockContext = MockContext.builder().build();
		PersonDataManager personDataManager = new PersonDataManager(mockContext, 10);

		// show that no people have been added
		assertFalse(personDataManager.getLastIssuedPersonId().isPresent());

		// show that last issued person matches expectations
		for (int i = 0; i < 10; i++) {
			PersonId personId = personDataManager.addPersonId();

			Optional<PersonId> lastIssuedPersonId = personDataManager.getLastIssuedPersonId();
			assertTrue(lastIssuedPersonId.isPresent());
			assertEquals(personId, lastIssuedPersonId.get());

		}

	}

	@Test
	@UnitTestMethod(name = "getScenarioToSimPeopleMap", args = {})
	public void testGetScenarioToSimPeopleMap() {
		MockContext mockContext = MockContext.builder().build();
		PersonDataManager personDataManager = new PersonDataManager(mockContext, 0);

		/*
		 * The map returned by the manager should be empty until we have added
		 * the map to the manger
		 */
		Map<PersonId, PersonId> scenarioToSimPeopleMap = personDataManager.getScenarioToSimPeopleMap();
		assertEquals(0, scenarioToSimPeopleMap.size());

		/*
		 * Add the map to the manager and show that it returns a new map
		 * instance containing the same data
		 */
		Map<PersonId, PersonId> expectedMap = new LinkedHashMap<>();
		for (int i = 0; i < 10; i++) {
			expectedMap.put(new PersonId(i * i), new PersonId(i));
		}
		personDataManager.setScenarioToSimPeopleMap(expectedMap);

		Map<PersonId, PersonId> actualMap = personDataManager.getScenarioToSimPeopleMap();
		assertEquals(expectedMap, actualMap);
		assertFalse(expectedMap == actualMap);

	}

	@Test
	@UnitTestMethod(name = "getSimToScenarioPeopleMap", args = {})
	public void testGetSimToScenarioPeopleMap() {
		MockContext mockContext = MockContext.builder().build();
		PersonDataManager personDataManager = new PersonDataManager(mockContext, 0);

		/*
		 * The map returned by the manager should be empty until we have added
		 * the map to the manger
		 */
		Map<PersonId, PersonId> simToScenarioPeopleMap = personDataManager.getSimToScenarioPeopleMap();
		assertEquals(0, simToScenarioPeopleMap.size());

		/*
		 * Add the map to the manager and show that it returns the correct
		 * reverse map
		 */
		Map<PersonId, PersonId> scenarioToSimPeopleMap = new LinkedHashMap<>();
		Map<PersonId, PersonId> expectedMap = new LinkedHashMap<>();
		for (int i = 0; i < 10; i++) {
			scenarioToSimPeopleMap.put(new PersonId(i * i), new PersonId(i));
			expectedMap.put(new PersonId(i), new PersonId(i * i));
		}
		personDataManager.setScenarioToSimPeopleMap(scenarioToSimPeopleMap);

		Map<PersonId, PersonId> actualMap = personDataManager.getSimToScenarioPeopleMap();
		assertEquals(expectedMap, actualMap);

	}

	@Test
	@UnitTestMethod(name = "setScenarioToSimPeopleMap", args = { Map.class })
	public void testSetScenarioToSimPeopleMap() {
		MockContext mockContext = MockContext.builder().build();
		PersonDataManager personDataManager = new PersonDataManager(mockContext, 0);

		/*
		 * Add the map to the manager and show that it returns a new map
		 * instance containing the same data
		 */
		Map<PersonId, PersonId> expectedMap = new LinkedHashMap<>();
		for (int i = 0; i < 10; i++) {
			expectedMap.put(new PersonId(i * i), new PersonId(i));
		}
		personDataManager.setScenarioToSimPeopleMap(expectedMap);

		Map<PersonId, PersonId> actualMap = personDataManager.getScenarioToSimPeopleMap();
		assertEquals(expectedMap, actualMap);
		assertFalse(expectedMap == actualMap);

		// precondition tests

		// show that a non one-to-one mapping will fail
		Map<PersonId, PersonId> badMapping = new LinkedHashMap<>();
		badMapping.put(new PersonId(5), new PersonId(0));
		badMapping.put(new PersonId(50), new PersonId(1));
		badMapping.put(new PersonId(500), new PersonId(0));
		assertThrows(ContractException.class, () -> personDataManager.setScenarioToSimPeopleMap(badMapping));

	}

	@Test
	@UnitTestMethod(name = "getPopulationCount", args = {})
	public void testGetPopulationCount() {
		MockContext mockContext = MockContext.builder().build();
		PersonDataManager personDataManager = new PersonDataManager(mockContext, 0);

		// show the population count grows as we add people
		for (int i = 0; i < 10; i++) {
			assertEquals(i, personDataManager.getPopulationCount());
			personDataManager.addPersonId();
			assertEquals(i + 1, personDataManager.getPopulationCount());

		}
	}

	@Test
	@UnitTestMethod(name = "getProjectedPopulationCount", args = {})
	public void testGetProjectedPopulationCount() {
		MockContext mockContext = MockContext.builder().build();
		PersonDataManager personDataManager = new PersonDataManager(mockContext, 0);

		assertEquals(0, personDataManager.getProjectedPopulationCount());

		/*
		 * Add a few people so we are not working from a zero-base and show the
		 * projected population count is correct
		 */
		for (int i = 0; i < 10; i++) {
			personDataManager.addPersonId();
		}
		assertEquals(10, personDataManager.getProjectedPopulationCount());

		// show that expanding the capcity results in the correct projected
		// count
		personDataManager.expandCapacity(30);
		assertEquals(40, personDataManager.getProjectedPopulationCount());

		/*
		 * show that adding people will not change the projected population
		 * count until the actual population catches up
		 */
		for (int i = 0; i < 100; i++) {
			personDataManager.addPersonId();
			int expectedValue = FastMath.max(40, personDataManager.getPopulationCount());
			assertEquals(expectedValue, personDataManager.getProjectedPopulationCount());
		}

		// show that expanding multiple times works as well
		personDataManager.expandCapacity(100);
		assertEquals(210, personDataManager.getProjectedPopulationCount());

		personDataManager.expandCapacity(100);
		assertEquals(310, personDataManager.getProjectedPopulationCount());

	}

	@Test
	@UnitTestMethod(name = "getPopulationTime", args = {})
	public void testGetPopulationTime() {
		RandomGenerator randomGenerator = SeedProvider.getRandomGenerator(3248439683828015913L);
		
		MutableDouble time = new MutableDouble();
		MockContext mockContext = MockContext.builder().setTimeSupplier(()->time.getValue()).build();
		PersonDataManager personDataManager = new PersonDataManager(mockContext, 0);
		
		for (int i = 0; i < 10; i++) {
			personDataManager.addPersonId();
			assertEquals(mockContext.getTime(),personDataManager.getPopulationTime());
			time.increment(randomGenerator.nextDouble());
		}
		for (int i = 0; i < 10; i++) {
			personDataManager.removePerson(new PersonId(i));
			assertEquals(mockContext.getTime(),personDataManager.getPopulationTime());
			time.increment(randomGenerator.nextDouble());
		}
		
	}
}
