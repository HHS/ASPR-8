package plugins.regions.datacontainers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.junit.jupiter.api.Test;

import nucleus.Context;
import nucleus.testsupport.MockContext;
import plugins.people.support.PersonId;
import plugins.properties.support.TimeTrackingPolicy;
import plugins.regions.initialdata.RegionInitialData;
import plugins.regions.support.RegionId;
import plugins.regions.testsupport.TestRegionId;
import util.MutableDouble;
import util.annotations.UnitTest;
import util.annotations.UnitTestConstructor;
import util.annotations.UnitTestMethod;

@UnitTest(target = RegionLocationDataManager.class)
public class AT_RegionLocationDataManger {

	
	
	@Test
	@UnitTestConstructor(args = { Context.class, RegionInitialData.class })
	public void testConstructor() {

		// precondition: the context cannot be null
		assertThrows(RuntimeException.class, () -> new RegionLocationDataManager(null, RegionInitialData.builder().build()));

		// precondition: the region initial data cannot be null
		assertThrows(RuntimeException.class, () -> new RegionLocationDataManager(MockContext.builder().build(), null));

	}
	

	/*
	 * Returns a RegionLocationDataManger with the full set of test
	 * regions, no people, obeying the given time tracking policy.
	 */
	private RegionLocationDataManager getRegionLocationDataManger(Context context, TimeTrackingPolicy timeTrackingPolicy) {

		RegionInitialData.Builder builder = RegionInitialData.builder();
		for (TestRegionId testRegionId : TestRegionId.values()) {
			builder.setRegionComponentInitialBehaviorSupplier(testRegionId, () -> (c) -> {
			});
		}
		builder.setPersonRegionArrivalTracking(timeTrackingPolicy);
		RegionInitialData regionInitialData = builder.build();

		RegionLocationDataManager regionLocationDataManager = new RegionLocationDataManager(context, regionInitialData);
		return regionLocationDataManager;
	}


	
	@Test
	@UnitTestMethod(name = "getRegionPopulationCount", args = { RegionId.class })
	public void testGetRegionPopulationCount() {
		
		MockContext mockContext = MockContext.builder().build();
		
		RegionLocationDataManager regionLocationDataManager = getRegionLocationDataManger(mockContext, TimeTrackingPolicy.DO_NOT_TRACK_TIME);
		
		// show that each region has no people
		for (TestRegionId testRegionId : TestRegionId.values()) {
			assertEquals(0, regionLocationDataManager.getRegionPopulationCount(testRegionId));
		}

		// show that adding people results in the correct population counts
		int n = TestRegionId.values().length;
		for (int i = 0; i < 3 * n; i++) {
			TestRegionId regionId = TestRegionId.values()[i % n];
			regionLocationDataManager.setPersonRegion(new PersonId(i), regionId);
		}

		for (TestRegionId testRegionId : TestRegionId.values()) {
			assertEquals(3, regionLocationDataManager.getRegionPopulationCount(testRegionId));
		}

		// precondition tests

		// if the region id is null
		assertThrows(RuntimeException.class, () -> regionLocationDataManager.getRegionPopulationCount(null));

		// if the region id is unknown
		assertThrows(RuntimeException.class, () -> regionLocationDataManager.getRegionPopulationCount(TestRegionId.getUnknownRegionId()));

	}
	

	

	@Test
	@UnitTestMethod(name = "getRegionPopulationTime", args = { RegionId.class })
	public void testGetRegionPopulationTime() {
		MutableDouble time = new MutableDouble(0);
		MockContext mockContext = MockContext.builder().setTimeSupplier(()->time.getValue()).build();
		RegionLocationDataManager regionLocationDataManager = getRegionLocationDataManger(mockContext, TimeTrackingPolicy.DO_NOT_TRACK_TIME);
		
		// show that each region starts with zero population time
		for (TestRegionId testRegionId : TestRegionId.values()) {
			assertEquals(0, regionLocationDataManager.getRegionPopulationTime(testRegionId));
		}

		// show that adding people results in the correct population times
		Map<RegionId, MutableDouble> expectedAssignmentTimes = new LinkedHashMap<>();
		for (TestRegionId testRegionId : TestRegionId.values()) {
			expectedAssignmentTimes.put(testRegionId, new MutableDouble());
		}

		int n = TestRegionId.values().length;
		for (int i = 0; i < 3 * n; i++) {
			time.setValue(i * 100);
			TestRegionId regionId = TestRegionId.values()[i % n];
			regionLocationDataManager.setPersonRegion(new PersonId(i), regionId);
			expectedAssignmentTimes.get(regionId).setValue(mockContext.getTime());
		}

		for (TestRegionId testRegionId : TestRegionId.values()) {
			double expectedRegionPopulationTime = expectedAssignmentTimes.get(testRegionId).getValue();
			double actualRegionPopulationTime = regionLocationDataManager.getRegionPopulationTime(testRegionId);
			assertEquals(expectedRegionPopulationTime, actualRegionPopulationTime);
		}

		// precondition tests
		assertThrows(RuntimeException.class, () -> regionLocationDataManager.getRegionPopulationTime(null));
		assertThrows(RuntimeException.class, () -> regionLocationDataManager.getRegionPopulationTime(TestRegionId.getUnknownRegionId()));

	}


	
	@Test
	@UnitTestMethod(name = "getPeopleInRegion", args = { RegionId.class })
	public void testGetPeopleInRegion() {
		
		MockContext mockContext = MockContext.builder().build();
		RegionLocationDataManager regionLocationDataManager = getRegionLocationDataManger(mockContext, TimeTrackingPolicy.DO_NOT_TRACK_TIME);


		// show that each region has no people
		for (TestRegionId testRegionId : TestRegionId.values()) {
			assertEquals(0, regionLocationDataManager.getRegionPopulationCount(testRegionId));
		}

		// show that adding people results in the correct population times
		int n = TestRegionId.values().length;

		// create a container for the people we expect to be in each region
		Map<RegionId, Set<PersonId>> expectedRegionPopulations = new LinkedHashMap<>();
		for (TestRegionId testRegionId : TestRegionId.values()) {
			expectedRegionPopulations.put(testRegionId, new LinkedHashSet<>());
		}

		// move people into the regions
		for (int i = 0; i < 3 * n; i++) {
			TestRegionId regionId = TestRegionId.values()[i % n];
			Set<PersonId> expectedPeople = expectedRegionPopulations.get(regionId);
			PersonId personId = new PersonId(i);
			expectedPeople.add(personId);
			regionLocationDataManager.setPersonRegion(personId, regionId);
		}

		for (TestRegionId testRegionId : TestRegionId.values()) {
			// get the set of people in the region
			int[] peopleInRegion = regionLocationDataManager.getPeopleInRegion(testRegionId);

			Set<PersonId> acutalPeople = new LinkedHashSet<>();
			for (int i = 0; i < peopleInRegion.length; i++) {
				acutalPeople.add(new PersonId(peopleInRegion[i]));
			}
			// get the set of people we expect
			Set<PersonId> expectedPeople = expectedRegionPopulations.get(testRegionId);

			// assert the two sets are equal
			assertEquals(expectedPeople, acutalPeople);
		}

		// precondition tests
		assertThrows(RuntimeException.class, () -> regionLocationDataManager.getPeopleInRegion(null));
		assertThrows(RuntimeException.class, () -> regionLocationDataManager.getPeopleInRegion(TestRegionId.getUnknownRegionId()));

	}
	

	

	@Test
	@UnitTestMethod(name = "getPersonRegion", args = { PersonId.class })
	public void testGetPersonRegion() {
		
		MockContext mockContext = MockContext.builder().build();
		RegionLocationDataManager regionLocationDataManager = getRegionLocationDataManger(mockContext, TimeTrackingPolicy.DO_NOT_TRACK_TIME);

		// show that adding people results in the correct population times
		int n = TestRegionId.values().length;

		// create a container for the people we expect to be in each region
		Map<PersonId, RegionId> expectedRegions = new LinkedHashMap<>();

		// move people into the regions
		for (int i = 0; i < 3 * n; i++) {
			TestRegionId regionId = TestRegionId.values()[i % n];
			PersonId personId = new PersonId(i);
			expectedRegions.put(personId, regionId);
			regionLocationDataManager.setPersonRegion(personId, regionId);
		}

		for (int i = 0; i < 3 * n; i++) {
			PersonId personId = new PersonId(i);
			RegionId expectedRegionId = expectedRegions.get(personId);
			RegionId acutalRegionId = regionLocationDataManager.getPersonRegion(personId);
			assertEquals(expectedRegionId, acutalRegionId);
		}

		// precondition tests

		// if the person id is null
		assertThrows(RuntimeException.class, () -> regionLocationDataManager.getPersonRegion(null));

		// if the person id is unknown
		assertThrows(RuntimeException.class, () -> regionLocationDataManager.getPersonRegion(new PersonId(-1)));

	}
	
	


	@Test
	@UnitTestMethod(name = "getPersonRegionArrivalTime", args = { PersonId.class })
	public void testGetPersonRegionArrivalTime() {
		MutableDouble time = new MutableDouble(0);
		MockContext mockContext = MockContext.builder().setTimeSupplier(()->time.getValue()).build();
		RegionLocationDataManager regionLocationDataManager = getRegionLocationDataManger(mockContext, TimeTrackingPolicy.TRACK_TIME);

		Random random = new Random(3930357586634914723L);

		int populationSize = 50;

		// move people into the regions
		Map<PersonId, MutableDouble> expectedArrivalTimes = new LinkedHashMap<>();
		for (int i = 0; i < populationSize; i++) {
			PersonId personId = new PersonId(i);
			TestRegionId regionId = TestRegionId.values()[random.nextInt(TestRegionId.values().length)];
			regionLocationDataManager.setPersonRegion(personId, regionId);
			assertEquals(0.0, regionLocationDataManager.getPersonRegionArrivalTime(personId));
			expectedArrivalTimes.put(personId, new MutableDouble());
		}

		// move people randomly over time
		for (int i = 0; i < 200; i++) {
			time.setValue(i);
			PersonId personId = new PersonId(random.nextInt(populationSize));
			TestRegionId regionId = TestRegionId.values()[random.nextInt(TestRegionId.values().length)];
			regionLocationDataManager.setPersonRegion(personId, regionId);
			assertEquals(mockContext.getTime(), regionLocationDataManager.getPersonRegionArrivalTime(personId));
			// show that the arrival time in the region is correct
			expectedArrivalTimes.get(personId).setValue(mockContext.getTime());
		}

		// show that the region arrival times remain stable over time
		time.increment(100);
		for (int i = 0; i < populationSize; i++) {
			PersonId personId = new PersonId(i);
			assertEquals(expectedArrivalTimes.get(personId).getValue(), regionLocationDataManager.getPersonRegionArrivalTime(personId));
		}

		// precondition tests

		// if the person id is null
		assertThrows(RuntimeException.class, () -> regionLocationDataManager.getPersonRegionArrivalTime(null));

		// if the person id is unknown
		assertThrows(RuntimeException.class, () -> regionLocationDataManager.getPersonRegionArrivalTime(new PersonId(-1)));

	}
	
	
	

	@Test
	@UnitTestMethod(name = "getPersonRegionArrivalTrackingPolicy", args = {})
	public void testGetPersonRegionArrivalTrackingPolicy() {
		for (TimeTrackingPolicy timeTrackingPolicy : TimeTrackingPolicy.values()) {
			RegionLocationDataManager regionLocationDataManager = getRegionLocationDataManger(MockContext.builder().build(), timeTrackingPolicy);
			assertEquals(timeTrackingPolicy, regionLocationDataManager.getPersonRegionArrivalTrackingPolicy());
		}
	}

	@Test
	@UnitTestMethod(name = "removePerson", args = { PersonId.class })
	public void testRemovePerson() {
		MockContext mockContext = MockContext.builder().build();
		RegionLocationDataManager regionLocationDataManager = getRegionLocationDataManger(mockContext, TimeTrackingPolicy.TRACK_TIME);

		int populationSize = 50;

		// move people into the regions

		for (int i = 0; i < populationSize; i++) {
			PersonId personId = new PersonId(i);
			TestRegionId regionId = TestRegionId.values()[i % TestRegionId.size()];
			regionLocationDataManager.setPersonRegion(personId, regionId);
		}

		for (int i = 0; i < populationSize; i++) {
			PersonId personId = new PersonId(i);
			regionLocationDataManager.removePerson(personId);
			assertThrows(RuntimeException.class, () -> regionLocationDataManager.removePerson(personId));
		}
		
		//precondition tests
		
		//if the person id is null
		assertThrows(RuntimeException.class, ()->regionLocationDataManager.removePerson(null));

		//if the person id is unknown
		assertThrows(RuntimeException.class, ()->regionLocationDataManager.removePerson(new PersonId(-1)));

	}


	
	@Test
	@UnitTestMethod(name = "setPersonRegion", args = { PersonId.class, RegionId.class })
	public void testSetPersonRegion() {		
		MockContext mockContext = MockContext.builder().build();
		RegionLocationDataManager regionLocationDataManager = getRegionLocationDataManger(mockContext, TimeTrackingPolicy.TRACK_TIME);

		int populationSize = 50;

		for (int i = 0; i < populationSize; i++) {
			PersonId personId = new PersonId(i);
			TestRegionId regionId = TestRegionId.values()[i % TestRegionId.size()];
			regionLocationDataManager.setPersonRegion(personId, regionId);
			assertEquals(regionId, regionLocationDataManager.getPersonRegion(personId));
		}
		
		//precondition tests
		PersonId personId = new PersonId(0);
		RegionId regionId = regionLocationDataManager.getPersonRegion(personId);
		
		assertThrows(RuntimeException.class,()-> regionLocationDataManager.setPersonRegion(null, regionId));
		assertThrows(RuntimeException.class,()-> regionLocationDataManager.setPersonRegion(new PersonId(-1), regionId));
		assertThrows(RuntimeException.class,()-> regionLocationDataManager.setPersonRegion(personId, null));
		assertThrows(RuntimeException.class,()-> regionLocationDataManager.setPersonRegion(personId, TestRegionId.getUnknownRegionId()));
	}
	
}
