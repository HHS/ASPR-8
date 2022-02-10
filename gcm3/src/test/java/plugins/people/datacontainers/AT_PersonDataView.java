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

import nucleus.testsupport.MockSimulationContext;
import plugins.people.support.PersonError;
import plugins.people.support.PersonId;
import util.ContractException;
import util.MutableDouble;
import util.SeedProvider;
import util.annotations.UnitTest;
import util.annotations.UnitTestConstructor;
import util.annotations.UnitTestMethod;

@UnitTest(target = PersonDataView.class)
public class AT_PersonDataView {

	@Test
	@UnitTestConstructor(args = { PersonDataManager.class })
	public void testConstructor() {
		// precondition tests
		ContractException contractException = assertThrows(ContractException.class, () -> new PersonDataView(null));
		assertEquals(PersonError.NULL_PERSON_DATA_MANAGER, contractException.getErrorType());
	}

	@Test
	@UnitTestMethod(name = "personExists", args = { PersonId.class })
	public void testPersonExists() {
		MockSimulationContext mockSimulationContext = MockSimulationContext.builder().build();
		PersonDataManager personDataManager = new PersonDataManager(mockSimulationContext, 10);
		PersonDataView personDataView = new PersonDataView(personDataManager);

		for (int i = 0; i < 10; i++) {
			personDataManager.addPersonId();
		}

		assertFalse(personDataView.personExists(new PersonId(-1)));

		for (int i = 0; i < 10; i++) {
			assertTrue(personDataView.personExists(new PersonId(i)));
		}

		for (int i = 10; i < 20; i++) {
			assertFalse(personDataView.personExists(new PersonId(i)));
		}
	}

	@Test
	@UnitTestMethod(name = "getPeople", args = {})
	public void testGetPeople() {
		MockSimulationContext mockSimulationContext = MockSimulationContext.builder().build();
		PersonDataManager personDataManager = new PersonDataManager(mockSimulationContext, 10);
		PersonDataView personDataView = new PersonDataView(personDataManager);

		// add some people
		List<PersonId> expectedPeople = new ArrayList<>();
		for (int i = 0; i < 10; i++) {
			PersonId personId = personDataManager.addPersonId();
			expectedPeople.add(personId);
		}

		List<PersonId> actualPeople = personDataView.getPeople();
		assertEquals(expectedPeople.size(), actualPeople.size());
		assertEquals(new LinkedHashSet<>(expectedPeople), new LinkedHashSet<>(actualPeople));

		// remove a few people
		for (int i = 0; i < 5; i++) {
			PersonId personId = new PersonId(i);
			personDataManager.removePerson(personId);
			expectedPeople.remove(personId);
		}

		// show that the removals resulted in the correct people
		actualPeople = personDataView.getPeople();
		assertEquals(expectedPeople.size(), actualPeople.size());
		assertEquals(new LinkedHashSet<>(expectedPeople), new LinkedHashSet<>(actualPeople));
	}

	@Test
	@UnitTestMethod(name = "getLastIssuedPersonId", args = {})
	public void getLastIssuedPersonId() {
		MockSimulationContext mockSimulationContext = MockSimulationContext.builder().build();
		PersonDataManager personDataManager = new PersonDataManager(mockSimulationContext, 10);
		PersonDataView personDataView = new PersonDataView(personDataManager);

		// show that no people have been added
		assertFalse(personDataView.getLastIssuedPersonId().isPresent());

		// show that last issued person matches expectations
		for (int i = 0; i < 10; i++) {
			PersonId personId = personDataManager.addPersonId();

			Optional<PersonId> lastIssuedPersonId = personDataView.getLastIssuedPersonId();
			assertTrue(lastIssuedPersonId.isPresent());
			assertEquals(personId, lastIssuedPersonId.get());

		}
	}

	@Test
	@UnitTestMethod(name = "personIndexExists", args = { int.class })
	public void testPersonIndexExists() {
		MockSimulationContext mockSimulationContext = MockSimulationContext.builder().build();
		PersonDataManager personDataManager = new PersonDataManager(mockSimulationContext, 10);
		PersonDataView personDataView = new PersonDataView(personDataManager);

		// initially there are no people despite the initial size
		assertFalse(personDataView.personIndexExists(-1));
		assertFalse(personDataView.personIndexExists(0));
		assertFalse(personDataView.personIndexExists(1));

		// show that we can add a few people and for each the manager will
		// indicate that they exist
		for (int i = 0; i < 10; i++) {
			PersonId personId = personDataManager.addPersonId();
			assertTrue(personDataView.personIndexExists(personId.getValue()));
		}

		// show that people who should not exist, actually don't exist
		assertFalse(personDataManager.personIndexExists(-1));
		for (int i = 10; i < 20; i++) {
			assertFalse(personDataView.personIndexExists(i));
		}
	}

	@Test
	@UnitTestMethod(name = "getPersonIdLimit", args = {})
	public void testGetPersonIdLimit() {
		MockSimulationContext mockSimulationContext = MockSimulationContext.builder().build();
		PersonDataManager personDataManager = new PersonDataManager(mockSimulationContext, 10);
		PersonDataView personDataView = new PersonDataView(personDataManager);

		// initially there are no people despite the initial size, so we expect
		// the limit to be zero.
		assertEquals(0, personDataView.getPersonIdLimit());

		// show that the limit increments as PersonId values are added
		for (int i = 0; i < 10; i++) {
			PersonId personId = personDataManager.addPersonId();
			assertEquals(personId.getValue() + 1, personDataView.getPersonIdLimit());
		}
	}

	@Test
	@UnitTestMethod(name = "getBoxedPersonId", args = { int.class })
	public void testGetBoxedPersonId() {
		MockSimulationContext mockSimulationContext = MockSimulationContext.builder().build();
		PersonDataManager personDataManager = new PersonDataManager(mockSimulationContext, 10);
		PersonDataView personDataView = new PersonDataView(personDataManager);

		// show that the boxed person id is correct
		for (int i = 0; i < 10; i++) {
			PersonId personId = personDataManager.addPersonId();
			PersonId boxedPersonId = personDataView.getBoxedPersonId(i);
			assertEquals(personId, boxedPersonId);
		}

		// precondition checks

		// if the person does not exist
		ContractException contractException = assertThrows(ContractException.class, () -> personDataView.getBoxedPersonId(-1));
		assertEquals(PersonError.UNKNOWN_PERSON_ID, contractException.getErrorType());

	}

	@Test
	@UnitTestMethod(name = "getScenarioToSimPeopleMap", args = {})
	public void testGetScenarioToSimPeopleMap() {
		MockSimulationContext mockSimulationContext = MockSimulationContext.builder().build();
		PersonDataManager personDataManager = new PersonDataManager(mockSimulationContext, 0);
		PersonDataView personDataView = new PersonDataView(personDataManager);

		/*
		 * The map returned by the manager should be empty until we have added
		 * the map to the manger
		 */
		Map<PersonId, PersonId> scenarioToSimPeopleMap = personDataView.getScenarioToSimPeopleMap();
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

		Map<PersonId, PersonId> actualMap = personDataView.getScenarioToSimPeopleMap();
		assertEquals(expectedMap, actualMap);
		assertFalse(expectedMap == actualMap);
	}

	@Test
	@UnitTestMethod(name = "getSimToScenarioPeopleMap", args = {})
	public void testGetSimToScenarioPeopleMap() {
		MockSimulationContext mockSimulationContext = MockSimulationContext.builder().build();
		PersonDataManager personDataManager = new PersonDataManager(mockSimulationContext, 0);
		PersonDataView personDataView = new PersonDataView(personDataManager);

		/*
		 * The map returned by the manager should be empty until we have added
		 * the map to the manger
		 */
		Map<PersonId, PersonId> simToScenarioPeopleMap = personDataView.getSimToScenarioPeopleMap();
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

		Map<PersonId, PersonId> actualMap = personDataView.getSimToScenarioPeopleMap();
		assertEquals(expectedMap, actualMap);
	}

	@Test
	@UnitTestMethod(name = "getPopulationCount", args = {})
	public void testGetPopulationCount() {
		MockSimulationContext mockSimulationContext = MockSimulationContext.builder().build();
		PersonDataManager personDataManager = new PersonDataManager(mockSimulationContext, 0);
		PersonDataView personDataView = new PersonDataView(personDataManager);

		// show the population count grows as we add people
		for (int i = 0; i < 10; i++) {
			assertEquals(i, personDataView.getPopulationCount());
			personDataManager.addPersonId();
			assertEquals(i + 1, personDataView.getPopulationCount());
		}
	}

	@Test
	@UnitTestMethod(name = "getProjectedPopulationCount", args = {})
	public void testGetProjectedPopulationCount() {
		MockSimulationContext mockSimulationContext = MockSimulationContext.builder().build();
		PersonDataManager personDataManager = new PersonDataManager(mockSimulationContext, 0);
		PersonDataView personDataView = new PersonDataView(personDataManager);

		assertEquals(0, personDataView.getProjectedPopulationCount());

		/*
		 * Add a few people so we are not working from a zero-base and show the
		 * projected population count is correct
		 */
		for (int i = 0; i < 10; i++) {
			personDataManager.addPersonId();
		}
		assertEquals(10, personDataView.getProjectedPopulationCount());

		// show that expanding the capcity results in the correct projected
		// count
		personDataManager.expandCapacity(30);
		assertEquals(40, personDataView.getProjectedPopulationCount());

		/*
		 * show that adding people will not change the projected population
		 * count until the actual population catches up
		 */
		for (int i = 0; i < 100; i++) {
			personDataManager.addPersonId();
			int expectedValue = FastMath.max(40, personDataView.getPopulationCount());
			assertEquals(expectedValue, personDataView.getProjectedPopulationCount());
		}

		// show that expanding multiple times works as well
		personDataManager.expandCapacity(100);
		assertEquals(210, personDataView.getProjectedPopulationCount());

		personDataManager.expandCapacity(100);
		assertEquals(310, personDataView.getProjectedPopulationCount());
	}

	@Test
	@UnitTestMethod(name = "getPopulationTime", args = {})
	public void testGetPopulationTime() {
		RandomGenerator randomGenerator = SeedProvider.getRandomGenerator(3301789058397712945L);

		MutableDouble time = new MutableDouble();
		MockSimulationContext mockSimulationContext = MockSimulationContext.builder().setTimeSupplier(() -> time.getValue()).build();
		PersonDataManager personDataManager = new PersonDataManager(mockSimulationContext, 0);
		PersonDataView personDataView = new PersonDataView(personDataManager);
		
		for (int i = 0; i < 10; i++) {
			personDataManager.addPersonId();
			assertEquals(mockSimulationContext.getTime(), personDataView.getPopulationTime());
			time.increment(randomGenerator.nextDouble());
		}
		for (int i = 0; i < 10; i++) {
			personDataManager.removePerson(new PersonId(i));
			assertEquals(mockSimulationContext.getTime(), personDataView.getPopulationTime());
			time.increment(randomGenerator.nextDouble());
		}
	}

}
