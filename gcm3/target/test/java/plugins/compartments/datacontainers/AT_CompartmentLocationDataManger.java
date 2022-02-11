package plugins.compartments.datacontainers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.junit.jupiter.api.Test;

import nucleus.SimulationContext;
import nucleus.testsupport.MockSimulationContext;
import plugins.compartments.initialdata.CompartmentInitialData;
import plugins.compartments.support.CompartmentId;
import plugins.compartments.testsupport.TestCompartmentId;
import plugins.people.support.PersonId;
import plugins.properties.support.TimeTrackingPolicy;
import util.MutableDouble;
import util.annotations.UnitTest;
import util.annotations.UnitTestConstructor;
import util.annotations.UnitTestMethod;

@UnitTest(target = CompartmentLocationDataManager.class)
public class AT_CompartmentLocationDataManger {

	
	
	@Test
	@UnitTestConstructor(args = { SimulationContext.class, CompartmentInitialData.class })
	public void testConstructor() {

		// precondition: the context cannot be null
		assertThrows(RuntimeException.class, () -> new CompartmentLocationDataManager(null, CompartmentInitialData.builder().build()));

		// precondition: the compartment initial data cannot be null
		assertThrows(RuntimeException.class, () -> new CompartmentLocationDataManager(MockSimulationContext.builder().build(), null));

	}

	/*
	 * Returns a CompartmentLocationDataManger with the full set of test
	 * compartments, no people, obeying the given time tracking policy.
	 */
	private CompartmentLocationDataManager getCompartmentLocationDataManger(SimulationContext simulationContext, TimeTrackingPolicy timeTrackingPolicy) {
		// create a mock context

		CompartmentInitialData.Builder builder = CompartmentInitialData.builder();
		for (TestCompartmentId testCompartmentId : TestCompartmentId.values()) {
			builder.setCompartmentInitialBehaviorSupplier(testCompartmentId, () -> (c) -> {
			});
		}
		builder.setPersonCompartmentArrivalTracking(timeTrackingPolicy);
		CompartmentInitialData compartmentInitialData = builder.build();

		CompartmentLocationDataManager compartmentLocationDataManager = new CompartmentLocationDataManager(simulationContext, compartmentInitialData);
		return compartmentLocationDataManager;
	}

	@Test
	@UnitTestMethod(name = "getCompartmentPopulationCount", args = { CompartmentId.class })
	public void testGetCompartmentPopulationCount() {
		MockSimulationContext mockSimulationContext = MockSimulationContext.builder().build();
		CompartmentLocationDataManager compartmentLocationDataManager = getCompartmentLocationDataManger(mockSimulationContext, TimeTrackingPolicy.DO_NOT_TRACK_TIME);
		
		// show that each compartment has no people
		for (TestCompartmentId testCompartmentId : TestCompartmentId.values()) {
			assertEquals(0, compartmentLocationDataManager.getCompartmentPopulationCount(testCompartmentId));
		}

		// show that adding people results in the correct population counts
		int n = TestCompartmentId.values().length;
		for (int i = 0; i < 3 * n; i++) {
			TestCompartmentId compartmentId = TestCompartmentId.values()[i % n];
			compartmentLocationDataManager.setPersonCompartment(new PersonId(i), compartmentId);
		}

		for (TestCompartmentId testCompartmentId : TestCompartmentId.values()) {
			assertEquals(3, compartmentLocationDataManager.getCompartmentPopulationCount(testCompartmentId));
		}

		// precondition tests

		// if the compartment id is null
		assertThrows(RuntimeException.class, () -> compartmentLocationDataManager.getCompartmentPopulationCount(null));

		// if the compartment id is unknown
		assertThrows(RuntimeException.class, () -> compartmentLocationDataManager.getCompartmentPopulationCount(TestCompartmentId.getUnknownCompartmentId()));

	}

	@Test
	@UnitTestMethod(name = "getCompartmentPopulationTime", args = { CompartmentId.class })
	public void testGetCompartmentPopulationTime() {
		MutableDouble time = new MutableDouble(0);
		MockSimulationContext mockSimulationContext = MockSimulationContext.builder().setTimeSupplier(()->time.getValue()).build();
		CompartmentLocationDataManager compartmentLocationDataManager = getCompartmentLocationDataManger(mockSimulationContext, TimeTrackingPolicy.DO_NOT_TRACK_TIME);
		
		// show that each compartment starts with zero population time
		for (TestCompartmentId testCompartmentId : TestCompartmentId.values()) {
			assertEquals(0, compartmentLocationDataManager.getCompartmentPopulationTime(testCompartmentId));
		}

		// show that adding people results in the correct population times
		Map<CompartmentId, MutableDouble> expectedAssignmentTimes = new LinkedHashMap<>();
		for (TestCompartmentId testCompartmentId : TestCompartmentId.values()) {
			expectedAssignmentTimes.put(testCompartmentId, new MutableDouble());
		}

		int n = TestCompartmentId.values().length;
		for (int i = 0; i < 3 * n; i++) {
			time.setValue(i * 100);
			TestCompartmentId compartmentId = TestCompartmentId.values()[i % n];
			compartmentLocationDataManager.setPersonCompartment(new PersonId(i), compartmentId);
			expectedAssignmentTimes.get(compartmentId).setValue(mockSimulationContext.getTime());
		}

		for (TestCompartmentId testCompartmentId : TestCompartmentId.values()) {
			double expectedCompartmentPopulationTime = expectedAssignmentTimes.get(testCompartmentId).getValue();
			double actualCompartmentPopulationTime = compartmentLocationDataManager.getCompartmentPopulationTime(testCompartmentId);
			assertEquals(expectedCompartmentPopulationTime, actualCompartmentPopulationTime);
		}

		// precondition tests
		assertThrows(RuntimeException.class, () -> compartmentLocationDataManager.getCompartmentPopulationTime(null));
		assertThrows(RuntimeException.class, () -> compartmentLocationDataManager.getCompartmentPopulationTime(TestCompartmentId.getUnknownCompartmentId()));

	}

	@Test
	@UnitTestMethod(name = "getPeopleInCompartment", args = { CompartmentId.class })
	public void testGetPeopleInCompartment() {
		MockSimulationContext mockSimulationContext = MockSimulationContext.builder().build();
		CompartmentLocationDataManager compartmentLocationDataManager = getCompartmentLocationDataManger(mockSimulationContext, TimeTrackingPolicy.DO_NOT_TRACK_TIME);


		// show that each compartment has no people
		for (TestCompartmentId testCompartmentId : TestCompartmentId.values()) {
			assertEquals(0, compartmentLocationDataManager.getCompartmentPopulationCount(testCompartmentId));
		}

		// show that adding people results in the correct population times
		int n = TestCompartmentId.values().length;

		// create a container for the people we expect to be in each compartment
		Map<CompartmentId, Set<PersonId>> expectedCompartmentPopulations = new LinkedHashMap<>();
		for (TestCompartmentId testCompartmentId : TestCompartmentId.values()) {
			expectedCompartmentPopulations.put(testCompartmentId, new LinkedHashSet<>());
		}

		// move people into the compartments
		for (int i = 0; i < 3 * n; i++) {
			TestCompartmentId compartmentId = TestCompartmentId.values()[i % n];
			Set<PersonId> expectedPeople = expectedCompartmentPopulations.get(compartmentId);
			PersonId personId = new PersonId(i);
			expectedPeople.add(personId);
			compartmentLocationDataManager.setPersonCompartment(personId, compartmentId);
		}

		for (TestCompartmentId testCompartmentId : TestCompartmentId.values()) {
			// get the set of people in the compartment
			int[] peopleInCompartment = compartmentLocationDataManager.getPeopleInCompartment(testCompartmentId);

			Set<PersonId> acutalPeople = new LinkedHashSet<>();
			for (int i = 0; i < peopleInCompartment.length; i++) {
				acutalPeople.add(new PersonId(peopleInCompartment[i]));
			}
			// get the set of people we expect
			Set<PersonId> expectedPeople = expectedCompartmentPopulations.get(testCompartmentId);

			// assert the two sets are equal
			assertEquals(expectedPeople, acutalPeople);
		}

		// precondition tests
		assertThrows(RuntimeException.class, () -> compartmentLocationDataManager.getPeopleInCompartment(null));
		assertThrows(RuntimeException.class, () -> compartmentLocationDataManager.getPeopleInCompartment(TestCompartmentId.getUnknownCompartmentId()));

	}

	@Test
	@UnitTestMethod(name = "getPersonCompartment", args = { PersonId.class })
	public void testGetPersonCompartment() {
		MockSimulationContext mockSimulationContext = MockSimulationContext.builder().build();
		CompartmentLocationDataManager compartmentLocationDataManager = getCompartmentLocationDataManger(mockSimulationContext, TimeTrackingPolicy.DO_NOT_TRACK_TIME);

		// show that adding people results in the correct population times
		int n = TestCompartmentId.values().length;

		// create a container for the people we expect to be in each compartment
		Map<PersonId, CompartmentId> expectedCompartments = new LinkedHashMap<>();

		// move people into the compartments
		for (int i = 0; i < 3 * n; i++) {
			TestCompartmentId compartmentId = TestCompartmentId.values()[i % n];
			PersonId personId = new PersonId(i);
			expectedCompartments.put(personId, compartmentId);
			compartmentLocationDataManager.setPersonCompartment(personId, compartmentId);
		}

		for (int i = 0; i < 3 * n; i++) {
			PersonId personId = new PersonId(i);
			CompartmentId expectedCompartmentId = expectedCompartments.get(personId);
			CompartmentId acutalCompartmentId = compartmentLocationDataManager.getPersonCompartment(personId);
			assertEquals(expectedCompartmentId, acutalCompartmentId);
		}

		// precondition tests

		// if the person id is null
		assertThrows(RuntimeException.class, () -> compartmentLocationDataManager.getPersonCompartment(null));

		// if the person id is unknown
		assertThrows(RuntimeException.class, () -> compartmentLocationDataManager.getPersonCompartment(new PersonId(-1)));

	}

	@Test
	@UnitTestMethod(name = "getPersonCompartmentArrivalTime", args = { PersonId.class })
	public void testGetPersonCompartmentArrivalTime() {
		MutableDouble time = new MutableDouble(0);
		MockSimulationContext mockSimulationContext = MockSimulationContext.builder().setTimeSupplier(()->time.getValue()).build();
		CompartmentLocationDataManager compartmentLocationDataManager = getCompartmentLocationDataManger(mockSimulationContext, TimeTrackingPolicy.TRACK_TIME);

		Random random = new Random(4707435414693465083L);

		int populationSize = 50;

		// move people into the compartments
		Map<PersonId, MutableDouble> expectedArrivalTimes = new LinkedHashMap<>();
		for (int i = 0; i < populationSize; i++) {
			PersonId personId = new PersonId(i);
			TestCompartmentId compartmentId = TestCompartmentId.values()[random.nextInt(TestCompartmentId.values().length)];
			compartmentLocationDataManager.setPersonCompartment(personId, compartmentId);
			assertEquals(0.0, compartmentLocationDataManager.getPersonCompartmentArrivalTime(personId));
			expectedArrivalTimes.put(personId, new MutableDouble());
		}

		// move people randomly over time
		for (int i = 0; i < 200; i++) {
			time.setValue(i);
			PersonId personId = new PersonId(random.nextInt(populationSize));
			TestCompartmentId compartmentId = TestCompartmentId.values()[random.nextInt(TestCompartmentId.values().length)];
			compartmentLocationDataManager.setPersonCompartment(personId, compartmentId);
			assertEquals(mockSimulationContext.getTime(), compartmentLocationDataManager.getPersonCompartmentArrivalTime(personId));
			// show that the arrival time in the compartment is correct
			expectedArrivalTimes.get(personId).setValue(mockSimulationContext.getTime());
		}

		// show that the compartment arrival times remain stable over time
		time.increment(100);
		for (int i = 0; i < populationSize; i++) {
			PersonId personId = new PersonId(i);
			assertEquals(expectedArrivalTimes.get(personId).getValue(), compartmentLocationDataManager.getPersonCompartmentArrivalTime(personId));
		}

		// precondition tests

		// if the person id is null
		assertThrows(RuntimeException.class, () -> compartmentLocationDataManager.getPersonCompartmentArrivalTime(null));

		// if the person id is unknown
		assertThrows(RuntimeException.class, () -> compartmentLocationDataManager.getPersonCompartmentArrivalTime(new PersonId(-1)));

	}

	@Test
	@UnitTestMethod(name = "getPersonCompartmentArrivalTrackingPolicy", args = {})
	public void testGetPersonCompartmentArrivalTrackingPolicy() {
		for (TimeTrackingPolicy timeTrackingPolicy : TimeTrackingPolicy.values()) {
			CompartmentLocationDataManager compartmentLocationDataManager = getCompartmentLocationDataManger(MockSimulationContext.builder().build(), timeTrackingPolicy);
			assertEquals(timeTrackingPolicy, compartmentLocationDataManager.getPersonCompartmentArrivalTrackingPolicy());
		}
	}

	@Test
	@UnitTestMethod(name = "removePerson", args = { PersonId.class })
	public void testRemovePerson() {
		MockSimulationContext mockSimulationContext = MockSimulationContext.builder().build();
		CompartmentLocationDataManager compartmentLocationDataManager = getCompartmentLocationDataManger(mockSimulationContext, TimeTrackingPolicy.TRACK_TIME);

		int populationSize = 50;

		// move people into the compartments

		for (int i = 0; i < populationSize; i++) {
			PersonId personId = new PersonId(i);
			TestCompartmentId compartmentId = TestCompartmentId.values()[i % TestCompartmentId.size()];
			compartmentLocationDataManager.setPersonCompartment(personId, compartmentId);
		}

		for (int i = 0; i < populationSize; i++) {
			PersonId personId = new PersonId(i);
			compartmentLocationDataManager.removePerson(personId);
			assertThrows(RuntimeException.class, () -> compartmentLocationDataManager.removePerson(personId));
		}
		
		//precondition tests
		
		//if the person id is null
		assertThrows(RuntimeException.class, ()->compartmentLocationDataManager.removePerson(null));

		//if the person id is unknown
		assertThrows(RuntimeException.class, ()->compartmentLocationDataManager.removePerson(new PersonId(-1)));

	}

	@Test
	@UnitTestMethod(name = "setPersonCompartment", args = { PersonId.class, CompartmentId.class })
	public void testSetPersonCompartment() {
		MockSimulationContext mockSimulationContext = MockSimulationContext.builder().build();
		CompartmentLocationDataManager compartmentLocationDataManager = getCompartmentLocationDataManger(mockSimulationContext, TimeTrackingPolicy.TRACK_TIME);

		int populationSize = 50;

		for (int i = 0; i < populationSize; i++) {
			PersonId personId = new PersonId(i);
			TestCompartmentId compartmentId = TestCompartmentId.values()[i % TestCompartmentId.size()];
			compartmentLocationDataManager.setPersonCompartment(personId, compartmentId);
			assertEquals(compartmentId, compartmentLocationDataManager.getPersonCompartment(personId));
		}
		
		//precondition tests
		PersonId personId = new PersonId(0);
		CompartmentId compartmentId = compartmentLocationDataManager.getPersonCompartment(personId);
		
		assertThrows(RuntimeException.class,()-> compartmentLocationDataManager.setPersonCompartment(null, compartmentId));
		assertThrows(RuntimeException.class,()-> compartmentLocationDataManager.setPersonCompartment(new PersonId(-1), compartmentId));
		assertThrows(RuntimeException.class,()-> compartmentLocationDataManager.setPersonCompartment(personId, null));
		assertThrows(RuntimeException.class,()-> compartmentLocationDataManager.setPersonCompartment(personId, TestCompartmentId.getUnknownCompartmentId()));

	}
}
