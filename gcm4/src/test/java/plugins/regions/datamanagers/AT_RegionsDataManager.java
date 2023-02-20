package plugins.regions.datamanagers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

import org.apache.commons.math3.random.RandomGenerator;
import org.apache.commons.math3.util.Pair;
import org.junit.jupiter.api.Test;

import nucleus.ActorContext;
import nucleus.DataManagerContext;
import nucleus.Event;
import nucleus.EventFilter;
import nucleus.testsupport.testplugin.TestActorPlan;
import nucleus.testsupport.testplugin.TestDataManager;
import nucleus.testsupport.testplugin.TestDataManagerPlan;
import nucleus.testsupport.testplugin.TestPluginData;
import nucleus.testsupport.testplugin.TestSimulation;
import plugins.people.PeoplePluginId;
import plugins.people.datamanagers.PeopleDataManager;
import plugins.people.events.PersonImminentAdditionEvent;
import plugins.people.events.PersonRemovalEvent;
import plugins.people.support.PersonConstructionData;
import plugins.people.support.PersonError;
import plugins.people.support.PersonId;
import plugins.regions.RegionsPluginData;
import plugins.regions.events.PersonRegionUpdateEvent;
import plugins.regions.events.RegionAdditionEvent;
import plugins.regions.events.RegionPropertyDefinitionEvent;
import plugins.regions.events.RegionPropertyUpdateEvent;
import plugins.regions.support.RegionConstructionData;
import plugins.regions.support.RegionError;
import plugins.regions.support.RegionId;
import plugins.regions.support.RegionPropertyDefinitionInitialization;
import plugins.regions.support.RegionPropertyId;
import plugins.regions.testsupport.RegionsTestPluginFactory;
import plugins.regions.testsupport.TestRegionId;
import plugins.regions.testsupport.TestRegionPropertyId;
import plugins.stochastics.StochasticsDataManager;
import plugins.util.properties.PropertyDefinition;
import plugins.util.properties.PropertyError;
import plugins.util.properties.TimeTrackingPolicy;
import util.annotations.UnitTestConstructor;
import util.annotations.UnitTestMethod;
import util.errors.ContractException;
import util.random.RandomGeneratorProvider;
import util.wrappers.MultiKey;
import util.wrappers.MutableDouble;
import util.wrappers.MutableInteger;

public class AT_RegionsDataManager {

	@Test
	@UnitTestMethod(target = RegionsDataManager.class, name = "expandCapacity", args = { int.class })
	public void testExpandCapacity() {
		TestSimulation.executeSimulation(RegionsTestPluginFactory.factory(20, 3161087621160007875L, TimeTrackingPolicy.TRACK_TIME, (c) -> {
			// show that a negative growth causes an exception
			RegionsDataManager regionsDataManager = c.getDataManager(RegionsDataManager.class);
			ContractException contractException = assertThrows(ContractException.class, () -> regionsDataManager.expandCapacity(-1));
			assertEquals(PersonError.NEGATIVE_GROWTH_PROJECTION, contractException.getErrorType());
		}).getPlugins());

		// use manual tests for non-negative growth
	}

	@Test
	@UnitTestConstructor(target = RegionsDataManager.class, args = { RegionsPluginData.class })
	public void testConstructor() {
		// this test is covered by the remaining tests
		ContractException contractException = assertThrows(ContractException.class, () -> new RegionsDataManager(null));
		assertEquals(RegionError.NULL_REGION_PLUGIN_DATA, contractException.getErrorType());
	}

	@Test
	@UnitTestMethod(target = RegionsDataManager.class, name = "getPeopleInRegion", args = { RegionId.class })
	public void testGetPeopleInRegion() {

		// create a container to hold expectations
		Map<RegionId, Set<PersonId>> expectedPeopelInRegions = new LinkedHashMap<>();
		for (TestRegionId testRegionId : TestRegionId.values()) {
			expectedPeopelInRegions.put(testRegionId, new LinkedHashSet<>());
		}

		TestPluginData.Builder pluginBuilder = TestPluginData.builder();

		// show that each region is empty at time zero
		pluginBuilder.addTestActorPlan("actor", new TestActorPlan(0, (c) -> {
			RegionsDataManager regionsDataManager = c.getDataManager(RegionsDataManager.class);
			for (TestRegionId testRegionId : TestRegionId.values()) {
				assertEquals(0, regionsDataManager.getPeopleInRegion(testRegionId).size());
			}
		}));

		// add some people
		pluginBuilder.addTestActorPlan("actor", new TestActorPlan(1, (c) -> {
			StochasticsDataManager stochasticsDataManager = c.getDataManager(StochasticsDataManager.class);
			PeopleDataManager peopleDataManager = c.getDataManager(PeopleDataManager.class);
			for (int i = 0; i < 100; i++) {
				TestRegionId regionId = TestRegionId.getRandomRegionId(stochasticsDataManager.getRandomGenerator());
				PersonConstructionData personConstructionData = PersonConstructionData.builder().add(regionId).build();
				PersonId personId = peopleDataManager.addPerson(personConstructionData);
				expectedPeopelInRegions.get(regionId).add(personId);
			}
		}));

		// show that the people in the regions match expectations
		pluginBuilder.addTestActorPlan("actor", new TestActorPlan(2, (c) -> {
			RegionsDataManager regionsDataManager = c.getDataManager(RegionsDataManager.class);
			for (TestRegionId testRegionId : TestRegionId.values()) {
				Set<PersonId> expectedPeople = expectedPeopelInRegions.get(testRegionId);
				LinkedHashSet<PersonId> actualPeople = new LinkedHashSet<>(regionsDataManager.getPeopleInRegion(testRegionId));
				assertEquals(expectedPeople, actualPeople);
			}
		}));

		// build and add the action plugin
		TestPluginData testPluginData = pluginBuilder.build();
		TestSimulation.executeSimulation(RegionsTestPluginFactory.factory(0, 3347423560010833899L, TimeTrackingPolicy.TRACK_TIME, testPluginData).getPlugins());

		// precondition test: if the region id is null
		TestSimulation.executeSimulation(RegionsTestPluginFactory.factory(0, 9052181434511982170L, TimeTrackingPolicy.TRACK_TIME, (c) -> {
			RegionsDataManager regionsDataManager = c.getDataManager(RegionsDataManager.class);

			ContractException contractException = assertThrows(ContractException.class, () -> regionsDataManager.getPeopleInRegion(null));
			assertEquals(RegionError.NULL_REGION_ID, contractException.getErrorType());
		}).getPlugins());

		// precondition test: if the region id is unknown
		TestSimulation.executeSimulation(RegionsTestPluginFactory.factory(0, 1410190102298165957L, TimeTrackingPolicy.TRACK_TIME, (c) -> {
			RegionsDataManager regionsDataManager = c.getDataManager(RegionsDataManager.class);

			// if the region id is unknown
			ContractException contractException = assertThrows(ContractException.class, () -> regionsDataManager.getPeopleInRegion(TestRegionId.getUnknownRegionId()));
			assertEquals(RegionError.UNKNOWN_REGION_ID, contractException.getErrorType());
		}).getPlugins());

	}

	@Test
	@UnitTestMethod(target = RegionsDataManager.class, name = "getPersonRegion", args = { PersonId.class })
	public void testGetPersonRegion() {

		TestPluginData.Builder pluginBuilder = TestPluginData.builder();

		// create a container to hold expectations
		Map<PersonId, RegionId> expectedPersonRegions = new LinkedHashMap<>();

		int numberOfPeople = 100;

		/*
		 * Add some people and show that their regions are correctly assigned.
		 */
		pluginBuilder.addTestActorPlan("actor", new TestActorPlan(0, (c) -> {
			StochasticsDataManager stochasticsDataManager = c.getDataManager(StochasticsDataManager.class);
			PeopleDataManager peopleDataManager = c.getDataManager(PeopleDataManager.class);
			RegionsDataManager regionsDataManager = c.getDataManager(RegionsDataManager.class);
			for (int i = 0; i < numberOfPeople; i++) {
				// select a region at random
				TestRegionId regionId = TestRegionId.getRandomRegionId(stochasticsDataManager.getRandomGenerator());
				// create the person
				PersonConstructionData personConstructionData = PersonConstructionData.builder().add(regionId).build();
				PersonId personId = peopleDataManager.addPerson(personConstructionData);
				// show that the person has the correct region
				assertEquals(regionId, regionsDataManager.getPersonRegion(personId));
				// add the person to the expectations
				expectedPersonRegions.put(personId, regionId);
			}
		}));

		// move people over time and show that each time they are moved the
		// correct region is reported
		pluginBuilder.addTestActorPlan("actor", new TestActorPlan(0, (c) -> {
			PeopleDataManager peopleDataManager = c.getDataManager(PeopleDataManager.class);
			double planTime = 0;
			for (PersonId personId : peopleDataManager.getPeople()) {
				c.addPlan((c2) -> {
					// determine the person's current region
					RegionsDataManager regionsDataManager = c2.getDataManager(RegionsDataManager.class);
					TestRegionId regionId = regionsDataManager.getPersonRegion(personId);
					// select the next region for the person
					regionId = regionId.next();
					// move the person
					regionsDataManager.setPersonRegion(personId, regionId);
					/*
					 * show that the region arrival time for the person is the
					 * current time in the simulation
					 */
					assertEquals(regionId, regionsDataManager.getPersonRegion(personId));
					// update the expectations
					expectedPersonRegions.put(personId, regionId);
				}, planTime);
				planTime++;
			}
		}));

		double postPersonMovementTime = numberOfPeople;

		/*
		 * Show that the people region arrival times are maintained over time
		 */
		pluginBuilder.addTestActorPlan("actor", new TestActorPlan(postPersonMovementTime, (c) -> {
			PeopleDataManager peopleDataManager = c.getDataManager(PeopleDataManager.class);
			RegionsDataManager regionsDataManager = c.getDataManager(RegionsDataManager.class);
			for (PersonId personId : peopleDataManager.getPeople()) {
				RegionId expectedRegionId = expectedPersonRegions.get(personId);
				RegionId actualRegionId = regionsDataManager.getPersonRegion(personId);
				assertEquals(expectedRegionId, actualRegionId);
			}
		}));
		// build and add the action plugin
		TestPluginData testPluginData = pluginBuilder.build();
		TestSimulation.executeSimulation(RegionsTestPluginFactory.factory(0, 5151111920517015649L, TimeTrackingPolicy.TRACK_TIME, testPluginData).getPlugins());

		// precondition test: if the person id is null
		TestSimulation.executeSimulation(RegionsTestPluginFactory.factory(0, 1490027040692903854L, TimeTrackingPolicy.TRACK_TIME, (c) -> {
			RegionsDataManager regionsDataManager = c.getDataManager(RegionsDataManager.class);
			ContractException contractException = assertThrows(ContractException.class, () -> regionsDataManager.getPersonRegion(null));
			assertEquals(PersonError.NULL_PERSON_ID, contractException.getErrorType());
		}).getPlugins());

		// precondition test: if the person id is unknown
		TestSimulation.executeSimulation(RegionsTestPluginFactory.factory(0, 2144445839100475443L, TimeTrackingPolicy.TRACK_TIME, (c) -> {
			RegionsDataManager regionsDataManager = c.getDataManager(RegionsDataManager.class);
			ContractException contractException = assertThrows(ContractException.class, () -> regionsDataManager.getPersonRegion(new PersonId(100000)));
			assertEquals(PersonError.UNKNOWN_PERSON_ID, contractException.getErrorType());
		}).getPlugins());
	}

	@Test
	@UnitTestMethod(target = RegionsDataManager.class, name = "getPersonRegionArrivalTime", args = { PersonId.class })
	public void testGetPersonRegionArrivalTime() {

		TestPluginData.Builder pluginBuilder = TestPluginData.builder();

		// create a container to hold expectations
		Map<PersonId, MutableDouble> expectedPersonRegionArrivalTimes = new LinkedHashMap<>();

		int numberOfPeople = 100;

		/*
		 * Add some people and show that their region arrival times are zero.
		 */
		pluginBuilder.addTestActorPlan("actor", new TestActorPlan(0, (c) -> {
			StochasticsDataManager stochasticsDataManager = c.getDataManager(StochasticsDataManager.class);
			PeopleDataManager peopleDataManager = c.getDataManager(PeopleDataManager.class);
			RegionsDataManager regionsDataManager = c.getDataManager(RegionsDataManager.class);
			for (int i = 0; i < numberOfPeople; i++) {
				// select a region at random
				TestRegionId regionId = TestRegionId.getRandomRegionId(stochasticsDataManager.getRandomGenerator());
				// create the person
				PersonConstructionData personConstructionData = PersonConstructionData.builder().add(regionId).build();
				PersonId personId = peopleDataManager.addPerson(personConstructionData);

				// show that the person has a region arrival time of zero
				assertEquals(0.0, regionsDataManager.getPersonRegionArrivalTime(personId));

				// add the person to the expectations
				expectedPersonRegionArrivalTimes.put(personId, new MutableDouble());
			}
		}));

		// move people over time and show that each time they are moved the
		// their arrival time is correct
		pluginBuilder.addTestActorPlan("actor", new TestActorPlan(0, (c) -> {
			PeopleDataManager peopleDataManager = c.getDataManager(PeopleDataManager.class);
			double planTime = 0;
			for (PersonId personId : peopleDataManager.getPeople()) {
				c.addPlan((c2) -> {
					// determine the person's current region
					RegionsDataManager regionsDataManager = c2.getDataManager(RegionsDataManager.class);
					TestRegionId regionId = regionsDataManager.getPersonRegion(personId);
					// select the next region for the person
					regionId = regionId.next();
					// move the person
					regionsDataManager.setPersonRegion(personId, regionId);
					/*
					 * show that the region arrival time for the person is the
					 * current time in the simulation
					 */
					assertEquals(c2.getTime(), regionsDataManager.getPersonRegionArrivalTime(personId));
					// update the expectations
					expectedPersonRegionArrivalTimes.get(personId).setValue(c2.getTime());
				}, planTime);
				planTime++;
			}
		}));

		double postPersonMovementTime = numberOfPeople;

		/*
		 * Show that the people region arrival times are maintained over time
		 */
		pluginBuilder.addTestActorPlan("actor", new TestActorPlan(postPersonMovementTime, (c) -> {
			PeopleDataManager peopleDataManager = c.getDataManager(PeopleDataManager.class);
			RegionsDataManager regionsDataManager = c.getDataManager(RegionsDataManager.class);
			for (PersonId personId : peopleDataManager.getPeople()) {
				double expectedArrivalTime = expectedPersonRegionArrivalTimes.get(personId).getValue();
				double actualArrivalTime = regionsDataManager.getPersonRegionArrivalTime(personId);
				assertEquals(expectedArrivalTime, actualArrivalTime);
			}
		}));

		// build and add the action plugin
		TestPluginData testPluginData = pluginBuilder.build();
		TestSimulation.executeSimulation(RegionsTestPluginFactory.factory(0, 2278422620232176214L, TimeTrackingPolicy.TRACK_TIME, testPluginData).getPlugins());

		// precondition test: if region arrival times are not being tracked
		pluginBuilder.addTestActorPlan("actor", new TestActorPlan(0, (c) -> {
			PeopleDataManager peopleDataManager = c.getDataManager(PeopleDataManager.class);
			StochasticsDataManager stochasticsDataManager = c.getDataManager(StochasticsDataManager.class);
			for (int i = 0; i < numberOfPeople; i++) {
				// select a region at random
				TestRegionId regionId = TestRegionId.getRandomRegionId(stochasticsDataManager.getRandomGenerator());
				// create the person
				PersonConstructionData personConstructionData = PersonConstructionData.builder().add(regionId).build();
				peopleDataManager.addPerson(personConstructionData);
			}
		}));

		// build and add the action plugin
		testPluginData = pluginBuilder.build();
		TestSimulation.executeSimulation(RegionsTestPluginFactory.factory(0, 9214210856215652451L, TimeTrackingPolicy.TRACK_TIME, testPluginData).getPlugins());

		// precondition test: if region arrival times are not being tracked
		TestSimulation.executeSimulation(RegionsTestPluginFactory.factory(10, 1906010286127446114L, TimeTrackingPolicy.DO_NOT_TRACK_TIME, (c) -> {
			RegionsDataManager regionsDataManager = c.getDataManager(RegionsDataManager.class);

			// if region arrival times are not being tracked
			ContractException contractException = assertThrows(ContractException.class, () -> regionsDataManager.getPersonRegionArrivalTime(new PersonId(0)));
			assertEquals(RegionError.REGION_ARRIVAL_TIMES_NOT_TRACKED, contractException.getErrorType());
		}).getPlugins());

		// precondition test: if the person id is null
		TestSimulation.executeSimulation(RegionsTestPluginFactory.factory(0, 2922597221561284586L, TimeTrackingPolicy.TRACK_TIME, (c) -> {
			RegionsDataManager regionsDataManager = c.getDataManager(RegionsDataManager.class);
			ContractException contractException = assertThrows(ContractException.class, () -> regionsDataManager.getPersonRegionArrivalTime(null));
			assertEquals(PersonError.NULL_PERSON_ID, contractException.getErrorType());
		}).getPlugins());

		// precondition test: if the person id is unknown
		TestSimulation.executeSimulation(RegionsTestPluginFactory.factory(0, 9132391945335483479L, TimeTrackingPolicy.TRACK_TIME, (c) -> {
			RegionsDataManager regionsDataManager = c.getDataManager(RegionsDataManager.class);
			ContractException contractException = assertThrows(ContractException.class, () -> regionsDataManager.getPersonRegionArrivalTime(new PersonId(100000)));
			assertEquals(PersonError.UNKNOWN_PERSON_ID, contractException.getErrorType());
		}).getPlugins());

	}

	@Test
	@UnitTestMethod(target = RegionsDataManager.class, name = "getPersonRegionArrivalTrackingPolicy", args = {})
	public void testGetPersonRegionArrivalTrackingPolicy() {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(7220786446142555493L);
		for (TimeTrackingPolicy timeTrackingPolicy : TimeTrackingPolicy.values()) {
			TestSimulation.executeSimulation(RegionsTestPluginFactory.factory(0, randomGenerator.nextLong(), timeTrackingPolicy, (c) -> {
				RegionsDataManager regionsDataManager = c.getDataManager(RegionsDataManager.class);
				assertEquals(timeTrackingPolicy, regionsDataManager.getPersonRegionArrivalTrackingPolicy());
			}).getPlugins());
		}
	}

	@Test
	@UnitTestMethod(target = RegionsDataManager.class, name = "getRegionIds", args = {})
	public void testGetRegionIds() {
		TestSimulation.executeSimulation(RegionsTestPluginFactory.factory(0, 87615823520161580L, TimeTrackingPolicy.TRACK_TIME, (c) -> {
			RegionsDataManager regionsDataManager = c.getDataManager(RegionsDataManager.class);

			Set<RegionId> expectedRegionIds = new LinkedHashSet<>();
			for (TestRegionId testRegionId : TestRegionId.values()) {
				expectedRegionIds.add(testRegionId);
			}
			assertEquals(expectedRegionIds, regionsDataManager.getRegionIds());
		}).getPlugins());
	}

	@Test
	@UnitTestMethod(target = RegionsDataManager.class, name = "getRegionPopulationCount", args = { RegionId.class })
	public void testGetRegionPopulationCount() {

		TestPluginData.Builder pluginBuilder = TestPluginData.builder();

		// show that each region has no people
		pluginBuilder.addTestActorPlan("actor", new TestActorPlan(0, (c) -> {
			RegionsDataManager regionsDataManager = c.getDataManager(RegionsDataManager.class);
			for (TestRegionId testRegionId : TestRegionId.values()) {
				assertEquals(0, regionsDataManager.getRegionPopulationCount(testRegionId));
			}
		}));

		// show that adding people results in the correct population counts

		pluginBuilder.addTestActorPlan("actor", new TestActorPlan(1, (c) -> {
			RegionsDataManager regionsDataManager = c.getDataManager(RegionsDataManager.class);
			PeopleDataManager peopleDataManager = c.getDataManager(PeopleDataManager.class);
			int n = TestRegionId.values().length;
			for (int i = 0; i < 3 * n; i++) {
				TestRegionId regionId = TestRegionId.values()[i % n];
				PersonConstructionData personConstructionData = PersonConstructionData.builder().add(regionId).build();
				peopleDataManager.addPerson(personConstructionData);
			}

			for (TestRegionId testRegionId : TestRegionId.values()) {
				assertEquals(3, regionsDataManager.getRegionPopulationCount(testRegionId));
			}

		}));

		// precondition tests

		pluginBuilder.addTestActorPlan("actor", new TestActorPlan(2, (c) -> {
			RegionsDataManager regionsDataManager = c.getDataManager(RegionsDataManager.class);

			// if the region id is null
			ContractException contractException = assertThrows(ContractException.class, () -> regionsDataManager.getRegionPopulationCount(null));
			assertEquals(RegionError.NULL_REGION_ID, contractException.getErrorType());

			// if the region id is unknown
			contractException = assertThrows(ContractException.class, () -> regionsDataManager.getRegionPopulationCount(TestRegionId.getUnknownRegionId()));
			assertEquals(RegionError.UNKNOWN_REGION_ID, contractException.getErrorType());

		}));

		// build and add the action plugin
		TestPluginData testPluginData = pluginBuilder.build();
		TestSimulation.executeSimulation(RegionsTestPluginFactory.factory(0, 1525815460460902517L, TimeTrackingPolicy.TRACK_TIME, testPluginData).getPlugins());

	}

	@Test
	@UnitTestMethod(target = RegionsDataManager.class, name = "getRegionPopulationTime", args = { RegionId.class })
	public void testGetRegionPopulationTime() {

		TestPluginData.Builder pluginBuilder = TestPluginData.builder();

		// show that each region has a zero population time
		pluginBuilder.addTestActorPlan("actor", new TestActorPlan(0, (c) -> {
			RegionsDataManager regionsDataManager = c.getDataManager(RegionsDataManager.class);
			for (TestRegionId testRegionId : TestRegionId.values()) {
				assertEquals(0, regionsDataManager.getRegionPopulationTime(testRegionId));
			}

		}));

		Map<RegionId, MutableDouble> expectedAssignmentTimes = new LinkedHashMap<>();
		for (TestRegionId testRegionId : TestRegionId.values()) {
			expectedAssignmentTimes.put(testRegionId, new MutableDouble());
		}

		int numberOfPeople = 100;

		// show that adding people over time results in the correct population
		// times
		pluginBuilder.addTestActorPlan("actor", new TestActorPlan(0, (c) -> {
			for (int i = 0; i < numberOfPeople; i++) {
				double planTime = i;
				c.addPlan((c2) -> {
					RegionsDataManager regionsDataManager = c2.getDataManager(RegionsDataManager.class);
					StochasticsDataManager stochasticsDataManager = c2.getDataManager(StochasticsDataManager.class);
					TestRegionId regionId = TestRegionId.getRandomRegionId(stochasticsDataManager.getRandomGenerator());
					PersonConstructionData personConstructionData = PersonConstructionData.builder().add(regionId).build();
					PeopleDataManager peopleDataManager = c2.getDataManager(PeopleDataManager.class);
					peopleDataManager.addPerson(personConstructionData);
					assertEquals(c2.getTime(), regionsDataManager.getRegionPopulationTime(regionId), 0);
					expectedAssignmentTimes.get(regionId).setValue(c2.getTime());
				}, planTime);
			}
		}));

		// show that the proper region population times are maintained
		// after all the person additions are complete.
		double postPersonAdditionTime = numberOfPeople;

		pluginBuilder.addTestActorPlan("actor", new TestActorPlan(postPersonAdditionTime, (c) -> {
			RegionsDataManager regionsDataManager = c.getDataManager(RegionsDataManager.class);
			for (TestRegionId testRegionId : TestRegionId.values()) {
				double expectedRegionPopulationTime = expectedAssignmentTimes.get(testRegionId).getValue();
				double actualRegionPopulationTime = regionsDataManager.getRegionPopulationTime(testRegionId);
				assertEquals(expectedRegionPopulationTime, actualRegionPopulationTime);
			}
		}));

		// build and add the action plugin
		TestPluginData testPluginData = pluginBuilder.build();
		TestSimulation.executeSimulation(RegionsTestPluginFactory.factory(numberOfPeople, 2430955549982485988L, TimeTrackingPolicy.TRACK_TIME, testPluginData).getPlugins());;

		// precondition test: if the region id is null
		TestSimulation.executeSimulation(RegionsTestPluginFactory.factory(numberOfPeople, 3091951498637393024L, TimeTrackingPolicy.TRACK_TIME, (c) -> {
			RegionsDataManager regionsDataManager = c.getDataManager(RegionsDataManager.class);
			ContractException contractException = assertThrows(ContractException.class, () -> regionsDataManager.getRegionPopulationTime(null));
			assertEquals(RegionError.NULL_REGION_ID, contractException.getErrorType());

		}).getPlugins());;

		// precondition tests: if the region id is unknown
		TestSimulation.executeSimulation(RegionsTestPluginFactory.factory(numberOfPeople, 2415744693759237392L, TimeTrackingPolicy.TRACK_TIME, (c) -> {
			RegionsDataManager regionsDataManager = c.getDataManager(RegionsDataManager.class);
			ContractException contractException = assertThrows(ContractException.class, () -> regionsDataManager.getRegionPopulationTime(TestRegionId.getUnknownRegionId()));
			assertEquals(RegionError.UNKNOWN_REGION_ID, contractException.getErrorType());

		}).getPlugins());;

	}

	@Test
	@UnitTestMethod(target = RegionsDataManager.class, name = "getRegionPropertyDefinition", args = { RegionPropertyId.class })
	public void testGetRegionPropertyDefinition() {
		TestSimulation.executeSimulation(RegionsTestPluginFactory.factory(0, 8915683065425449883L, TimeTrackingPolicy.TRACK_TIME, (c) -> {
			RegionsDataManager regionsDataManager = c.getDataManager(RegionsDataManager.class);

			Set<TestRegionPropertyId> regionPropertyIds = regionsDataManager.getRegionPropertyIds();
			assertEquals(TestRegionPropertyId.size(), regionPropertyIds.size());
			for (TestRegionPropertyId testRegionPropertyId : regionPropertyIds) {
				PropertyDefinition expectedPropertyDefinition = testRegionPropertyId.getPropertyDefinition();
				PropertyDefinition actualPropertyDefinition = regionsDataManager.getRegionPropertyDefinition(testRegionPropertyId);
				assertEquals(expectedPropertyDefinition, actualPropertyDefinition);
			}

		}).getPlugins());;

		// precondition check: if the region property id is null
		TestSimulation.executeSimulation(RegionsTestPluginFactory.factory(0, 4217775232224320101L, TimeTrackingPolicy.TRACK_TIME, (c) -> {
			RegionsDataManager regionsDataManager = c.getDataManager(RegionsDataManager.class);
			ContractException contractException = assertThrows(ContractException.class, () -> regionsDataManager.getRegionPropertyDefinition(null));
			assertEquals(PropertyError.NULL_PROPERTY_ID, contractException.getErrorType());
		}).getPlugins());;

		// precondition check: if the region property id is unknown
		TestSimulation.executeSimulation(RegionsTestPluginFactory.factory(0, 1425794836864585647L, TimeTrackingPolicy.TRACK_TIME, (c) -> {
			RegionsDataManager regionsDataManager = c.getDataManager(RegionsDataManager.class);
			ContractException contractException = assertThrows(ContractException.class, () -> regionsDataManager.getRegionPropertyDefinition(TestRegionPropertyId.getUnknownRegionPropertyId()));
			assertEquals(PropertyError.UNKNOWN_PROPERTY_ID, contractException.getErrorType());
		}).getPlugins());;
	}

	@Test
	@UnitTestMethod(target = RegionsDataManager.class, name = "getRegionPropertyIds", args = {})
	public void testGetRegionPropertyIds() {
		TestSimulation.executeSimulation(RegionsTestPluginFactory.factory(0, 2658585233315606268L, TimeTrackingPolicy.TRACK_TIME, (c) -> {
			RegionsDataManager regionsDataManager = c.getDataManager(RegionsDataManager.class);
			Set<RegionPropertyId> expectedRegionPropertyIds = new LinkedHashSet<>();
			for (TestRegionPropertyId testRegionPropertyId : TestRegionPropertyId.values()) {
				expectedRegionPropertyIds.add(testRegionPropertyId);
			}
			assertEquals(expectedRegionPropertyIds, regionsDataManager.getRegionPropertyIds());
		}).getPlugins());;
		// no precondition tests

	}

	@Test
	@UnitTestMethod(target = RegionsDataManager.class, name = "getRegionPropertyValue", args = { RegionId.class, RegionPropertyId.class })
	public void testGetRegionPropertyValue() {

		TestPluginData.Builder pluginBuilder = TestPluginData.builder();
		Map<MultiKey, Object> expectedPropertyValues = new LinkedHashMap<>();

		pluginBuilder.addTestActorPlan("actor", new TestActorPlan(0, (c) -> {

			RegionsDataManager regionsDataManager = c.getDataManager(RegionsDataManager.class);

			for (TestRegionId testRegionId : TestRegionId.values()) {
				for (TestRegionPropertyId testRegionPropertyId : TestRegionPropertyId.values()) {

					Object regionPropertyValue = regionsDataManager.getRegionPropertyValue(testRegionId, testRegionPropertyId);
					MultiKey multiKey = new MultiKey(testRegionId, testRegionPropertyId);
					expectedPropertyValues.put(multiKey, regionPropertyValue);
				}
			}
		}));

		// show that changes to the property values properly reflect the
		// previous values

		for (int i = 1; i < 300; i++) {
			pluginBuilder.addTestActorPlan("actor", new TestActorPlan(i, (c) -> {
				RegionsDataManager regionsDataManager = c.getDataManager(RegionsDataManager.class);
				StochasticsDataManager stochasticsDataManager = c.getDataManager(StochasticsDataManager.class);
				RandomGenerator randomGenerator = stochasticsDataManager.getRandomGenerator();
				TestRegionId testRegionId = TestRegionId.getRandomRegionId(randomGenerator);
				TestRegionPropertyId testRegionPropertyId = TestRegionPropertyId.getRandomMutableRegionPropertyId(randomGenerator);

				// show that the property has the correct value
				MultiKey multiKey = new MultiKey(testRegionId, testRegionPropertyId);
				Object expectedPropertyValue = expectedPropertyValues.get(multiKey);

				Object actualPropertyValue = regionsDataManager.getRegionPropertyValue(testRegionId, testRegionPropertyId);
				assertEquals(expectedPropertyValue, actualPropertyValue);

				Object newPropertyValue = testRegionPropertyId.getRandomPropertyValue(randomGenerator);
				regionsDataManager.setRegionPropertyValue(testRegionId, testRegionPropertyId, newPropertyValue);
				expectedPropertyValues.put(multiKey, newPropertyValue);

			}));
		}

		TestPluginData testPluginData = pluginBuilder.build();
		TestSimulation.executeSimulation(RegionsTestPluginFactory.factory(0, 8784099691519492811L, TimeTrackingPolicy.TRACK_TIME, testPluginData).getPlugins());;

		// precondition check: if the region id is null
		TestSimulation.executeSimulation(RegionsTestPluginFactory.factory(0, 468427930601885944L, TimeTrackingPolicy.TRACK_TIME, (c) -> {
			RegionsDataManager regionsDataManager = c.getDataManager(RegionsDataManager.class);
			RegionPropertyId knownRegionPropertyId = TestRegionPropertyId.REGION_PROPERTY_1_BOOLEAN_MUTABLE;
			ContractException contractException = assertThrows(ContractException.class, () -> regionsDataManager.getRegionPropertyValue(null, knownRegionPropertyId));
			assertEquals(RegionError.NULL_REGION_ID, contractException.getErrorType());
		}).getPlugins());;

		// precondition check: if the region id is not known
		TestSimulation.executeSimulation(RegionsTestPluginFactory.factory(0, 6075787443228538245L, TimeTrackingPolicy.TRACK_TIME, (c) -> {
			RegionsDataManager regionsDataManager = c.getDataManager(RegionsDataManager.class);
			RegionId unknownRegionId = TestRegionId.getUnknownRegionId();
			RegionPropertyId knownRegionPropertyId = TestRegionPropertyId.REGION_PROPERTY_1_BOOLEAN_MUTABLE;
			ContractException contractException = assertThrows(ContractException.class, () -> regionsDataManager.getRegionPropertyValue(unknownRegionId, knownRegionPropertyId));
			assertEquals(RegionError.UNKNOWN_REGION_ID, contractException.getErrorType());
		}).getPlugins());;

		// precondition check: if the region property id is null
		TestSimulation.executeSimulation(RegionsTestPluginFactory.factory(0, 2997323471294141386L, TimeTrackingPolicy.TRACK_TIME, (c) -> {
			RegionsDataManager regionsDataManager = c.getDataManager(RegionsDataManager.class);
			RegionId knownRegionId = TestRegionId.REGION_1;
			ContractException contractException = assertThrows(ContractException.class, () -> regionsDataManager.getRegionPropertyValue(knownRegionId, null));
			assertEquals(PropertyError.NULL_PROPERTY_ID, contractException.getErrorType());
		}).getPlugins());;

		// precondition check: if the region property id is unknown
		TestSimulation.executeSimulation(RegionsTestPluginFactory.factory(0, 7980671049474262492L, TimeTrackingPolicy.TRACK_TIME, (c) -> {
			RegionsDataManager regionsDataManager = c.getDataManager(RegionsDataManager.class);
			RegionId knownRegionId = TestRegionId.REGION_1;
			RegionPropertyId unknownRegionPropertyId = TestRegionPropertyId.getUnknownRegionPropertyId();
			ContractException contractException = assertThrows(ContractException.class, () -> regionsDataManager.getRegionPropertyValue(knownRegionId, unknownRegionPropertyId));
			assertEquals(PropertyError.UNKNOWN_PROPERTY_ID, contractException.getErrorType());
		}).getPlugins());;

	}

	@Test
	@UnitTestMethod(target = RegionsDataManager.class, name = "getRegionPropertyTime", args = { RegionId.class, RegionPropertyId.class })
	public void testGetRegionPropertyTime() {
		Map<MultiKey, MutableDouble> expectedPropertyTimes = new LinkedHashMap<>();
		for (TestRegionId testRegionId : TestRegionId.values()) {
			for (TestRegionPropertyId testRegionPropertyId : TestRegionPropertyId.values()) {
				expectedPropertyTimes.put(new MultiKey(testRegionId, testRegionPropertyId), new MutableDouble());
			}
		}
		TestPluginData.Builder pluginBuilder = TestPluginData.builder();

		// show that the property times are currently zero
		pluginBuilder.addTestActorPlan("actor", new TestActorPlan(0, (c) -> {
			RegionsDataManager regionsDataManager = c.getDataManager(RegionsDataManager.class);
			assertTrue(regionsDataManager.getRegionIds().size() > 0);
			assertTrue(regionsDataManager.getRegionPropertyIds().size() > 0);
			for (RegionId regionId : regionsDataManager.getRegionIds()) {
				for (RegionPropertyId regionPropertyId : regionsDataManager.getRegionPropertyIds()) {
					double regionPropertyTime = regionsDataManager.getRegionPropertyTime(regionId, regionPropertyId);
					assertEquals(0, regionPropertyTime, 0);
				}
			}
		}));

		// show that changes to the property values properly reflect the time
		// the occured

		for (int i = 0; i < 300; i++) {
			pluginBuilder.addTestActorPlan("actor", new TestActorPlan(i, (c) -> {
				RegionsDataManager regionsDataManager = c.getDataManager(RegionsDataManager.class);
				StochasticsDataManager stochasticsDataManager = c.getDataManager(StochasticsDataManager.class);
				RandomGenerator randomGenerator = stochasticsDataManager.getRandomGenerator();
				TestRegionId testRegionId = TestRegionId.getRandomRegionId(randomGenerator);
				TestRegionPropertyId testRegionPropertyId = TestRegionPropertyId.getRandomMutableRegionPropertyId(randomGenerator);

				// show that the property has the correct time
				MutableDouble mutableDouble = expectedPropertyTimes.get(new MultiKey(testRegionId, testRegionPropertyId));
				double expectedPropertyTime = mutableDouble.getValue();
				double actualPropertyTime = regionsDataManager.getRegionPropertyTime(testRegionId, testRegionPropertyId);
				assertEquals(expectedPropertyTime, actualPropertyTime);

				Object newPropertyValue = testRegionPropertyId.getRandomPropertyValue(randomGenerator);

				regionsDataManager.setRegionPropertyValue(testRegionId, testRegionPropertyId, newPropertyValue);
				assertEquals(c.getTime(), regionsDataManager.getRegionPropertyTime(testRegionId, testRegionPropertyId));
				mutableDouble.setValue(c.getTime());
			}));
		}

		TestPluginData testPluginData = pluginBuilder.build();
		TestSimulation.executeSimulation(RegionsTestPluginFactory.factory(0, 1085097084913380645L, TimeTrackingPolicy.TRACK_TIME, testPluginData).getPlugins());;

		// precondition check: if the region id is null
		TestSimulation.executeSimulation(RegionsTestPluginFactory.factory(0, 9165213921588406384L, TimeTrackingPolicy.TRACK_TIME, (c) -> {
			RegionsDataManager regionsDataManager = c.getDataManager(RegionsDataManager.class);
			RegionPropertyId knownRegionPropertyId = TestRegionPropertyId.REGION_PROPERTY_1_BOOLEAN_MUTABLE;
			ContractException contractException = assertThrows(ContractException.class, () -> regionsDataManager.getRegionPropertyTime(null, knownRegionPropertyId));
			assertEquals(RegionError.NULL_REGION_ID, contractException.getErrorType());
		}).getPlugins());;

		// precondition check: if the region id is not known
		TestSimulation.executeSimulation(RegionsTestPluginFactory.factory(0, 1546629608367614750L, TimeTrackingPolicy.TRACK_TIME, (c) -> {
			RegionsDataManager regionsDataManager = c.getDataManager(RegionsDataManager.class);
			RegionId unknownRegionId = TestRegionId.getUnknownRegionId();
			RegionPropertyId knownRegionPropertyId = TestRegionPropertyId.REGION_PROPERTY_1_BOOLEAN_MUTABLE;
			ContractException contractException = assertThrows(ContractException.class, () -> regionsDataManager.getRegionPropertyTime(unknownRegionId, knownRegionPropertyId));
			assertEquals(RegionError.UNKNOWN_REGION_ID, contractException.getErrorType());
		}).getPlugins());;

		// precondition check: if the region property id is null
		TestSimulation.executeSimulation(RegionsTestPluginFactory.factory(0, 7141175136643291537L, TimeTrackingPolicy.TRACK_TIME, (c) -> {
			RegionsDataManager regionsDataManager = c.getDataManager(RegionsDataManager.class);
			RegionId knownRegionId = TestRegionId.REGION_1;
			ContractException contractException = assertThrows(ContractException.class, () -> regionsDataManager.getRegionPropertyTime(knownRegionId, null));
			assertEquals(PropertyError.NULL_PROPERTY_ID, contractException.getErrorType());
		}).getPlugins());;

		// precondition check: if the region property id is unknown
		TestSimulation.executeSimulation(RegionsTestPluginFactory.factory(0, 2200230008116664966L, TimeTrackingPolicy.TRACK_TIME, (c) -> {
			RegionsDataManager regionsDataManager = c.getDataManager(RegionsDataManager.class);
			RegionId knownRegionId = TestRegionId.REGION_1;
			RegionPropertyId unknownRegionPropertyId = TestRegionPropertyId.getUnknownRegionPropertyId();
			ContractException contractException = assertThrows(ContractException.class, () -> regionsDataManager.getRegionPropertyTime(knownRegionId, unknownRegionPropertyId));
			assertEquals(PropertyError.UNKNOWN_PROPERTY_ID, contractException.getErrorType());
		}).getPlugins());;

	}

	@Test
	@UnitTestMethod(target = RegionsDataManager.class, name = "regionIdExists", args = { RegionId.class })
	public void testRegionIdExists() {
		TestSimulation.executeSimulation(RegionsTestPluginFactory.factory(0, 8636579794186794067L, TimeTrackingPolicy.TRACK_TIME, (c) -> {
			RegionsDataManager regionsDataManager = c.getDataManager(RegionsDataManager.class);

			// show that null region ids do not exist
			assertFalse(regionsDataManager.regionIdExists(null));

			// show that the region ids added do exist
			for (TestRegionId testRegionId : TestRegionId.values()) {
				assertTrue(regionsDataManager.regionIdExists(testRegionId));
			}

			// show that an unknown region id does not exist
			assertFalse(regionsDataManager.regionIdExists(TestRegionId.getUnknownRegionId()));

		}).getPlugins());;

	}

	@Test
	@UnitTestMethod(target = RegionsDataManager.class, name = "regionPropertyIdExists", args = { RegionPropertyId.class })
	public void testRegionPropertyIdExists() {
		TestSimulation.executeSimulation(RegionsTestPluginFactory.factory(0, 3797498566412748237L, TimeTrackingPolicy.TRACK_TIME, (c) -> {
			RegionsDataManager regionsDataManager = c.getDataManager(RegionsDataManager.class);

			// show that the property ids exist
			for (TestRegionPropertyId testRegionPropertyId : TestRegionPropertyId.values()) {
				assertTrue(regionsDataManager.regionPropertyIdExists(testRegionPropertyId));
			}

			// show that null references return false
			assertFalse(regionsDataManager.regionPropertyIdExists(null));

			// show that unknown region property ids return false
			assertFalse(regionsDataManager.regionPropertyIdExists(TestRegionPropertyId.getUnknownRegionPropertyId()));

		}).getPlugins());;

	}

	@Test
	@UnitTestMethod(target = RegionsDataManager.class, name = "setPersonRegion", args = { PersonId.class, RegionId.class })
	public void testSetPersonRegion() {
		int numberOfPeople = 30;

		TestPluginData.Builder pluginBuilder = TestPluginData.builder();

		// create some containers for movement observations
		List<MultiKey> recievedObservations = new ArrayList<>();
		List<MultiKey> expectedObservations = new ArrayList<>();

		/*
		 * Have the observer agent observe all movements and record those
		 * observations
		 */
		pluginBuilder.addTestActorPlan("observer", new TestActorPlan(0, (c) -> {
			RegionsDataManager regionsDataManager = c.getDataManager(RegionsDataManager.class);
			for (TestRegionId testRegionId : TestRegionId.values()) {
				EventFilter<PersonRegionUpdateEvent> eventFilter = regionsDataManager.getEventFilterForPersonRegionUpdateEvent_ByArrivalRegion(testRegionId);
				c.subscribe(eventFilter, (c2, e) -> {
					recievedObservations.add(new MultiKey(e.previousRegionId(), e.currentRegionId(), e.personId(), c2.getTime()));
				});
			}
		}));

		/*
		 * Have the mover agent move every person over time and show that each
		 * person is where we expect them to be
		 */
		pluginBuilder.addTestActorPlan("mover", new TestActorPlan(1, (c) -> {

			/*
			 * Make sure that there are actually people in the simulation so
			 * that test is actually testing something
			 */
			PeopleDataManager peopleDataManager = c.getDataManager(PeopleDataManager.class);
			RegionsDataManager regionsDataManager = c.getDataManager(RegionsDataManager.class);

			List<PersonId> people = peopleDataManager.getPeople();
			assertTrue(people.size() > 0);

			// schedule a time for each person to be moved to a new region
			double planTime = 10;
			for (final PersonId personId : people) {
				c.addPlan((c2) -> {
					TestRegionId regionId = regionsDataManager.getPersonRegion(personId);
					TestRegionId nextRegionId = regionId.next();
					regionsDataManager.setPersonRegion(personId, nextRegionId);

					// show that the person's region is updated
					assertEquals(nextRegionId, regionsDataManager.getPersonRegion(personId));
					expectedObservations.add(new MultiKey(regionId, nextRegionId, personId, c2.getTime()));

					// show that the person's region arrival time is
					// updated
					assertEquals(c2.getTime(), regionsDataManager.getPersonRegionArrivalTime(personId));

				}, planTime);
				planTime += 5;
			}
		}));

		// build the plugin
		TestPluginData testPluginData = pluginBuilder.build();
		TestSimulation.executeSimulation(RegionsTestPluginFactory.factory(numberOfPeople, 5655227215512656797L, TimeTrackingPolicy.TRACK_TIME, testPluginData).getPlugins());;

		// show that the observations were correct
		assertEquals(expectedObservations.size(), recievedObservations.size());
		assertEquals(new LinkedHashSet<>(expectedObservations), new LinkedHashSet<>(recievedObservations));

		/*
		 * precondition test: if the person id is null
		 */
		TestSimulation.executeSimulation(RegionsTestPluginFactory.factory(numberOfPeople, 9048586333860290178L, TimeTrackingPolicy.TRACK_TIME, (c) -> {
			// Select a person at random from the simulation and create a person
			// id outside of the simulation

			StochasticsDataManager stochasticsDataManager = c.getDataManager(StochasticsDataManager.class);
			RandomGenerator randomGenerator = stochasticsDataManager.getRandomGenerator();

			PeopleDataManager peopleDataManager = c.getDataManager(PeopleDataManager.class);
			List<PersonId> people = peopleDataManager.getPeople();
			PersonId personId = people.get(randomGenerator.nextInt(people.size()));

			// establish the person's current region and next region
			RegionsDataManager regionsDataManager = c.getDataManager(RegionsDataManager.class);
			TestRegionId currentRegionId = regionsDataManager.getPersonRegion(personId);
			TestRegionId nextRegionId = currentRegionId.next();

			ContractException contractException = assertThrows(ContractException.class, () -> regionsDataManager.setPersonRegion(null, nextRegionId));
			assertEquals(PersonError.NULL_PERSON_ID, contractException.getErrorType());

		}).getPlugins());;

		/*
		 * precondition test: if the person id is unknown
		 */
		TestSimulation.executeSimulation(RegionsTestPluginFactory.factory(numberOfPeople, 6693022571477538917L, TimeTrackingPolicy.TRACK_TIME, (c) -> {
			// Select a person at random from the simulation and create a person
			// id outside of the simulation

			StochasticsDataManager stochasticsDataManager = c.getDataManager(StochasticsDataManager.class);
			RandomGenerator randomGenerator = stochasticsDataManager.getRandomGenerator();

			PeopleDataManager peopleDataManager = c.getDataManager(PeopleDataManager.class);
			List<PersonId> people = peopleDataManager.getPeople();
			PersonId personId = people.get(randomGenerator.nextInt(people.size()));
			PersonId badPersonId = new PersonId(people.size());

			// establish the person's current region and next region
			RegionsDataManager regionsDataManager = c.getDataManager(RegionsDataManager.class);
			TestRegionId currentRegionId = regionsDataManager.getPersonRegion(personId);
			TestRegionId nextRegionId = currentRegionId.next();

			ContractException contractException = assertThrows(ContractException.class, () -> regionsDataManager.setPersonRegion(badPersonId, nextRegionId));
			assertEquals(PersonError.UNKNOWN_PERSON_ID, contractException.getErrorType());
		}).getPlugins());;

		/*
		 * precondition test: if the region id is null
		 */
		TestSimulation.executeSimulation(RegionsTestPluginFactory.factory(numberOfPeople, 1385204599279421266L, TimeTrackingPolicy.TRACK_TIME, (c) -> {
			// Select a person at random from the simulation and create a person
			// id outside of the simulation

			StochasticsDataManager stochasticsDataManager = c.getDataManager(StochasticsDataManager.class);
			RandomGenerator randomGenerator = stochasticsDataManager.getRandomGenerator();

			PeopleDataManager peopleDataManager = c.getDataManager(PeopleDataManager.class);
			List<PersonId> people = peopleDataManager.getPeople();
			PersonId personId = people.get(randomGenerator.nextInt(people.size()));

			// establish the person's current region and next region
			RegionsDataManager regionsDataManager = c.getDataManager(RegionsDataManager.class);

			//
			ContractException contractException = assertThrows(ContractException.class, () -> regionsDataManager.setPersonRegion(personId, null));
			assertEquals(RegionError.NULL_REGION_ID, contractException.getErrorType());

		}).getPlugins());;

		/*
		 * precondition test: if the region id is unknown
		 */
		TestSimulation.executeSimulation(RegionsTestPluginFactory.factory(numberOfPeople, 6025662871362676118L, TimeTrackingPolicy.TRACK_TIME, (c) -> {
			// Select a person at random from the simulation and create a person
			// id outside of the simulation

			StochasticsDataManager stochasticsDataManager = c.getDataManager(StochasticsDataManager.class);
			RandomGenerator randomGenerator = stochasticsDataManager.getRandomGenerator();

			PeopleDataManager peopleDataManager = c.getDataManager(PeopleDataManager.class);
			List<PersonId> people = peopleDataManager.getPeople();
			PersonId personId = people.get(randomGenerator.nextInt(people.size()));

			// establish the person's current region and next region
			RegionsDataManager regionsDataManager = c.getDataManager(RegionsDataManager.class);

			// create a non-existent region id
			RegionId unknownRegionId = TestRegionId.getUnknownRegionId();

			ContractException contractException = assertThrows(ContractException.class, () -> regionsDataManager.setPersonRegion(personId, unknownRegionId));
			assertEquals(RegionError.UNKNOWN_REGION_ID, contractException.getErrorType());

		}).getPlugins());;

	}

	@Test
	@UnitTestMethod(target = RegionsDataManager.class, name = "setRegionPropertyValue", args = { RegionId.class, RegionPropertyId.class, Object.class })
	public void testSetRegionPropertyValue() {

		TestPluginData.Builder pluginBuilder = TestPluginData.builder();

		// create containers to hold actual and expected observations
		List<MultiKey> actualObservations = new ArrayList<>();
		List<MultiKey> expectedObservations = new ArrayList<>();

		// Have the observer agent start observations record them
		pluginBuilder.addTestActorPlan("observer", new TestActorPlan(0, (c) -> {
			RegionsDataManager regionsDataManager = c.getDataManager(RegionsDataManager.class);
			EventFilter<RegionPropertyUpdateEvent> eventFilter = regionsDataManager.getEventFilterForRegionPropertyUpdateEvent(TestRegionId.REGION_1,
					TestRegionPropertyId.REGION_PROPERTY_2_INTEGER_MUTABLE);
			c.subscribe(eventFilter, (c2, e) -> {
				actualObservations.add(new MultiKey(c2.getTime(), e.regionId(), e.regionPropertyId(), e.currentPropertyValue()));
			});

			eventFilter = regionsDataManager.getEventFilterForRegionPropertyUpdateEvent(TestRegionId.REGION_2, TestRegionPropertyId.REGION_PROPERTY_3_DOUBLE_MUTABLE);
			c.subscribe(eventFilter, (c2, e) -> {
				actualObservations.add(new MultiKey(c2.getTime(), e.regionId(), e.regionPropertyId(), e.currentPropertyValue()));
			});

		}));

		// Have the update agent make various region property updates over
		// time
		pluginBuilder.addTestActorPlan("update", new TestActorPlan(0, (c) -> {
			StochasticsDataManager stochasticsDataManager = c.getDataManager(StochasticsDataManager.class);
			RandomGenerator randomGenerator = stochasticsDataManager.getRandomGenerator();

			for (int i = 0; i < 10; i++) {
				c.addPlan((c2) -> {
					StochasticsDataManager stochasticsDataManager2 = c.getDataManager(StochasticsDataManager.class);
					RandomGenerator randomGenerator2 = stochasticsDataManager2.getRandomGenerator();
					RegionsDataManager regionsDataManager = c2.getDataManager(RegionsDataManager.class);
					Integer newValue = randomGenerator2.nextInt();
					regionsDataManager.setRegionPropertyValue(TestRegionId.REGION_1, TestRegionPropertyId.REGION_PROPERTY_2_INTEGER_MUTABLE, newValue);
					Integer actualValue = regionsDataManager.getRegionPropertyValue(TestRegionId.REGION_1, TestRegionPropertyId.REGION_PROPERTY_2_INTEGER_MUTABLE);
					assertEquals(newValue, actualValue);
					double valueTime = regionsDataManager.getRegionPropertyTime(TestRegionId.REGION_1, TestRegionPropertyId.REGION_PROPERTY_2_INTEGER_MUTABLE);
					assertEquals(c2.getTime(), valueTime);
					expectedObservations.add(new MultiKey(c2.getTime(), TestRegionId.REGION_1, TestRegionPropertyId.REGION_PROPERTY_2_INTEGER_MUTABLE, newValue));
				}, randomGenerator.nextDouble() * 1000);
			}

			for (int i = 0; i < 10; i++) {
				c.addPlan((c2) -> {
					StochasticsDataManager stochasticsDataManager2 = c.getDataManager(StochasticsDataManager.class);
					RandomGenerator randomGenerator2 = stochasticsDataManager2.getRandomGenerator();
					RegionsDataManager regionsDataManager = c2.getDataManager(RegionsDataManager.class);
					Double newValue = randomGenerator2.nextDouble();
					regionsDataManager.setRegionPropertyValue(TestRegionId.REGION_2, TestRegionPropertyId.REGION_PROPERTY_3_DOUBLE_MUTABLE, newValue);
					Double actualValue = regionsDataManager.getRegionPropertyValue(TestRegionId.REGION_2, TestRegionPropertyId.REGION_PROPERTY_3_DOUBLE_MUTABLE);
					assertEquals(newValue, actualValue);
					double valueTime = regionsDataManager.getRegionPropertyTime(TestRegionId.REGION_2, TestRegionPropertyId.REGION_PROPERTY_3_DOUBLE_MUTABLE);
					assertEquals(c2.getTime(), valueTime);
					expectedObservations.add(new MultiKey(c2.getTime(), TestRegionId.REGION_2, TestRegionPropertyId.REGION_PROPERTY_3_DOUBLE_MUTABLE, newValue));
				}, randomGenerator.nextDouble() * 1000);
			}

		}));

		TestPluginData testPluginData = pluginBuilder.build();
		TestSimulation.executeSimulation(RegionsTestPluginFactory.factory(0, 4630021532130673951L, TimeTrackingPolicy.TRACK_TIME, testPluginData).getPlugins());

		// show that the observed changes match expectations
		assertEquals(expectedObservations.size(), actualObservations.size());

		assertEquals(new LinkedHashSet<>(expectedObservations), new LinkedHashSet<>(actualObservations));

		// precondition check: if the region id is null
		TestSimulation.executeSimulation(RegionsTestPluginFactory.factory(0, 7347707922069273812L, TimeTrackingPolicy.TRACK_TIME, (c) -> {
			RegionPropertyId regionPropertyId = TestRegionPropertyId.REGION_PROPERTY_2_INTEGER_MUTABLE;
			Object propertyValue = 67;
			RegionsDataManager regionsDataManager = c.getDataManager(RegionsDataManager.class);

			ContractException contractException = assertThrows(ContractException.class, () -> regionsDataManager.setRegionPropertyValue(null, regionPropertyId, propertyValue));
			assertEquals(RegionError.NULL_REGION_ID, contractException.getErrorType());

		}).getPlugins());;

		// precondition check: if the region id is unknown
		TestSimulation.executeSimulation(RegionsTestPluginFactory.factory(0, 3075330757105736185L, TimeTrackingPolicy.TRACK_TIME, (c) -> {

			RegionId unknownRegionId = TestRegionId.getUnknownRegionId();
			RegionPropertyId regionPropertyId = TestRegionPropertyId.REGION_PROPERTY_2_INTEGER_MUTABLE;
			Object propertyValue = 67;
			RegionsDataManager regionsDataManager = c.getDataManager(RegionsDataManager.class);

			ContractException contractException = assertThrows(ContractException.class, () -> regionsDataManager.setRegionPropertyValue(unknownRegionId, regionPropertyId, propertyValue));
			assertEquals(RegionError.UNKNOWN_REGION_ID, contractException.getErrorType());

		}).getPlugins());;

		// precondition check: if the region property id is null
		TestSimulation.executeSimulation(RegionsTestPluginFactory.factory(0, 4169934733913962790L, TimeTrackingPolicy.TRACK_TIME, (c) -> {
			RegionId regionId = TestRegionId.REGION_1;
			Object propertyValue = 67;
			RegionsDataManager regionsDataManager = c.getDataManager(RegionsDataManager.class);

			ContractException contractException = assertThrows(ContractException.class, () -> regionsDataManager.setRegionPropertyValue(regionId, null, propertyValue));
			assertEquals(PropertyError.NULL_PROPERTY_ID, contractException.getErrorType());

		}).getPlugins());;

		// precondition check: if the region property id is unknown
		TestSimulation.executeSimulation(RegionsTestPluginFactory.factory(0, 5578070775436119166L, TimeTrackingPolicy.TRACK_TIME, (c) -> {
			RegionId regionId = TestRegionId.REGION_1;
			RegionPropertyId unknownRegionPropertyId = TestRegionPropertyId.getUnknownRegionPropertyId();
			Object propertyValue = 67;
			RegionsDataManager regionsDataManager = c.getDataManager(RegionsDataManager.class);

			ContractException contractException = assertThrows(ContractException.class, () -> regionsDataManager.setRegionPropertyValue(regionId, unknownRegionPropertyId, propertyValue));
			assertEquals(PropertyError.UNKNOWN_PROPERTY_ID, contractException.getErrorType());

		}).getPlugins());;

		// precondition check: if the region property value is null
		TestSimulation.executeSimulation(RegionsTestPluginFactory.factory(0, 217279748753596418L, TimeTrackingPolicy.TRACK_TIME, (c) -> {
			RegionId regionId = TestRegionId.REGION_1;
			RegionPropertyId regionPropertyId = TestRegionPropertyId.REGION_PROPERTY_2_INTEGER_MUTABLE;
			RegionsDataManager regionsDataManager = c.getDataManager(RegionsDataManager.class);

			ContractException contractException = assertThrows(ContractException.class, () -> regionsDataManager.setRegionPropertyValue(regionId, regionPropertyId, null));
			assertEquals(PropertyError.NULL_PROPERTY_VALUE, contractException.getErrorType());

		}).getPlugins());;

		// precondition check: if the region property value is incompatible with
		// the defined type for the property
		TestSimulation.executeSimulation(RegionsTestPluginFactory.factory(0, 7043526072670323223L, TimeTrackingPolicy.TRACK_TIME, (c) -> {
			RegionId regionId = TestRegionId.REGION_1;
			RegionPropertyId regionPropertyId = TestRegionPropertyId.REGION_PROPERTY_2_INTEGER_MUTABLE;
			Object incompatiblePropertyValue = "incompatible value";
			RegionsDataManager regionsDataManager = c.getDataManager(RegionsDataManager.class);

			ContractException contractException = assertThrows(ContractException.class, () -> regionsDataManager.setRegionPropertyValue(regionId, regionPropertyId, incompatiblePropertyValue));
			assertEquals(PropertyError.INCOMPATIBLE_VALUE, contractException.getErrorType());

		}).getPlugins());;

		// precondition check: if the region id is unknown
		TestSimulation.executeSimulation(RegionsTestPluginFactory.factory(0, 8501593854721316109L, TimeTrackingPolicy.TRACK_TIME, (c) -> {
			RegionId unknownRegionId = TestRegionId.getUnknownRegionId();
			RegionPropertyId regionPropertyId = TestRegionPropertyId.REGION_PROPERTY_2_INTEGER_MUTABLE;
			Object propertyValue = 67;
			RegionsDataManager regionsDataManager = c.getDataManager(RegionsDataManager.class);

			ContractException contractException = assertThrows(ContractException.class, () -> regionsDataManager.setRegionPropertyValue(unknownRegionId, regionPropertyId, propertyValue));
			assertEquals(RegionError.UNKNOWN_REGION_ID, contractException.getErrorType());

		}).getPlugins());;

		// precondition check: if the region property value is null
		TestSimulation.executeSimulation(RegionsTestPluginFactory.factory(0, 6977487076968608944L, TimeTrackingPolicy.TRACK_TIME, (c) -> {
			RegionId regionId = TestRegionId.REGION_1;
			RegionPropertyId immutableRegionPropertyId = TestRegionPropertyId.REGION_PROPERTY_5_INTEGER_IMMUTABLE;
			RegionsDataManager regionsDataManager = c.getDataManager(RegionsDataManager.class);

			ContractException contractException = assertThrows(ContractException.class, () -> regionsDataManager.setRegionPropertyValue(regionId, immutableRegionPropertyId, null));
			assertEquals(PropertyError.NULL_PROPERTY_VALUE, contractException.getErrorType());

		}).getPlugins());;
	}

	@Test
	@UnitTestMethod(target = RegionsDataManager.class, name = "init", args = { DataManagerContext.class })
	public void testRegionPluginData() {
		long seed = 4454658950052475227L;
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(seed);
		int initialPopulation = 30;
		List<PersonId> people = new ArrayList<>();
		for (int i = 0; i < initialPopulation; i++) {
			people.add(new PersonId(i));
		}

		// add the region plugin
		RegionsPluginData.Builder regionPluginBuilder = RegionsPluginData.builder();
		for (TestRegionId regionId : TestRegionId.values()) {
			regionPluginBuilder.addRegion(regionId);
		}

		for (TestRegionPropertyId testRegionPropertyId : TestRegionPropertyId.values()) {
			regionPluginBuilder.defineRegionProperty(testRegionPropertyId, testRegionPropertyId.getPropertyDefinition());
		}

		Map<RegionId, Map<RegionPropertyId, Object>> expectedRegionProperties = new LinkedHashMap<>();

		for (TestRegionId regionId : TestRegionId.values()) {
			Map<RegionPropertyId, Object> propertyMap = new LinkedHashMap<>();
			expectedRegionProperties.put(regionId, propertyMap);
			for (TestRegionPropertyId testRegionPropertyId : TestRegionPropertyId.values()) {
				if (testRegionPropertyId.getPropertyDefinition().getDefaultValue().isEmpty() || randomGenerator.nextBoolean()) {
					Object randomPropertyValue = testRegionPropertyId.getRandomPropertyValue(randomGenerator);
					regionPluginBuilder.setRegionPropertyValue(regionId, testRegionPropertyId, randomPropertyValue);
					propertyMap.put(testRegionPropertyId, randomPropertyValue);
				} else {
					propertyMap.put(testRegionPropertyId, testRegionPropertyId.getPropertyDefinition().getDefaultValue().get());
				}
			}
		}
		TestRegionId testRegionId = TestRegionId.REGION_1;
		for (PersonId personId : people) {
			regionPluginBuilder.setPersonRegion(personId, testRegionId);
			testRegionId = testRegionId.next();
		}

		regionPluginBuilder.setPersonRegionArrivalTracking(TimeTrackingPolicy.TRACK_TIME);
		RegionsPluginData regionsPluginData = regionPluginBuilder.build();

		// add the test plugin
		TestPluginData.Builder pluginBuilder = TestPluginData.builder();

		pluginBuilder.addTestActorPlan("actor", new TestActorPlan(0, (c) -> {
			RegionsDataManager regionsDataManager = c.getDataManager(RegionsDataManager.class);
			// show that the initial state of the region data manager matches
			// the state of the region plugin data

			assertEquals(regionsPluginData.getPersonRegionArrivalTrackingPolicy(), regionsDataManager.getPersonRegionArrivalTrackingPolicy());
			assertEquals(regionsPluginData.getRegionIds(), regionsDataManager.getRegionIds());
			assertEquals(regionsPluginData.getRegionPropertyIds(), regionsDataManager.getRegionPropertyIds());
			for (RegionPropertyId regionPropertyId : regionsPluginData.getRegionPropertyIds()) {
				PropertyDefinition expectedPropertyDefinition = regionsPluginData.getRegionPropertyDefinition(regionPropertyId);
				PropertyDefinition actualPropertyDefinition = regionsDataManager.getRegionPropertyDefinition(regionPropertyId);
				assertEquals(expectedPropertyDefinition, actualPropertyDefinition);
			}
			for (RegionId regionId : regionsPluginData.getRegionIds()) {
				for (TestRegionPropertyId testRegionPropertyId : TestRegionPropertyId.values()) {
					Object expectedValue = expectedRegionProperties.get(regionId).get(testRegionPropertyId);
					Object actualValue = regionsDataManager.getRegionPropertyValue(regionId, testRegionPropertyId);
					assertEquals(expectedValue, actualValue);
				}
			}

		}));
		TestPluginData testPluginData = pluginBuilder.build();
		TestSimulation.executeSimulation(RegionsTestPluginFactory.factory(initialPopulation, seed, TimeTrackingPolicy.TRACK_TIME, testPluginData).setRegionsPluginData(regionsPluginData).getPlugins());
	}

	@Test
	@UnitTestMethod(target = RegionsDataManager.class, name = "init", args = { DataManagerContext.class })
	public void testPluginDataLoaded() {

		long seed = 4228466028646070532L;

		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(seed);

		int initialPopulation = 100;

		List<PersonId> people = new ArrayList<>();
		for (int i = 0; i < initialPopulation; i++) {
			people.add(new PersonId(i));
		}

		// add the region plugin
		RegionsPluginData.Builder regionPluginBuilder = RegionsPluginData.builder();
		for (TestRegionId regionId : TestRegionId.values()) {
			regionPluginBuilder.addRegion(regionId);
		}

		for (TestRegionPropertyId testRegionPropertyId : TestRegionPropertyId.values()) {
			regionPluginBuilder.defineRegionProperty(testRegionPropertyId, testRegionPropertyId.getPropertyDefinition());
		}

		for (TestRegionPropertyId testRegionPropertyId : TestRegionPropertyId.getPropertiesWithoutDefaultValues()) {
			for (TestRegionId testRegionId : TestRegionId.values()) {
				Object randomPropertyValue = testRegionPropertyId.getRandomPropertyValue(randomGenerator);
				regionPluginBuilder.setRegionPropertyValue(testRegionId, testRegionPropertyId, randomPropertyValue);
			}
		}
		TestRegionId testRegionId = TestRegionId.REGION_1;
		regionPluginBuilder.setPersonRegionArrivalTracking(TimeTrackingPolicy.TRACK_TIME);
		for (PersonId personId : people) {
			regionPluginBuilder.setPersonRegion(personId, testRegionId);
			testRegionId = testRegionId.next();
		}
		RegionsPluginData regionsPluginData = regionPluginBuilder.build();

		// add the test plugin
		TestPluginData.Builder pluginBuilder = TestPluginData.builder();
		pluginBuilder.addTestActorPlan("actor", new TestActorPlan(0, (c) -> {
			RegionsDataManager regionsDataManager = c.getDataManager(RegionsDataManager.class);
			for (PersonId personId : people) {
				RegionId actualRegionId = regionsDataManager.getPersonRegion(personId);
				RegionId expectedRegionId = regionsPluginData.getPersonRegion(personId).get();
				assertEquals(actualRegionId, expectedRegionId);
			}

		}));
		TestPluginData testPluginData = pluginBuilder.build();

		TestSimulation.executeSimulation(RegionsTestPluginFactory.factory(initialPopulation, seed, TimeTrackingPolicy.TRACK_TIME, testPluginData).setRegionsPluginData(regionsPluginData).getPlugins());

	}

	@Test
	@UnitTestMethod(target = RegionsDataManager.class, name = "init", args = { DataManagerContext.class })
	public void testPersonImmimentAdditionEvent() {
		/*
		 * Have the agent create some people over time and show that each person
		 * is in the correct region at the correct time
		 */
		TestSimulation.executeSimulation(RegionsTestPluginFactory.factory(0, 8294774271110836859L, TimeTrackingPolicy.TRACK_TIME, (c) -> {
			StochasticsDataManager stochasticsDataManager = c.getDataManager(StochasticsDataManager.class);
			RandomGenerator randomGenerator = stochasticsDataManager.getRandomGenerator();

			for (int i = 0; i < 100; i++) {
				c.addPlan((c2) -> {
					StochasticsDataManager stochasticsDataManager2 = c2.getDataManager(StochasticsDataManager.class);
					RegionsDataManager regionsDataManager = c2.getDataManager(RegionsDataManager.class);
					PeopleDataManager peopleDataManager = c2.getDataManager(PeopleDataManager.class);

					/*
					 * Generate a random region to for the new person and add
					 * the person
					 */
					TestRegionId randomRegionId = TestRegionId.getRandomRegionId(stochasticsDataManager2.getRandomGenerator());
					PersonConstructionData personConstructionData = PersonConstructionData.builder().add(randomRegionId).build();
					PersonId personId = peopleDataManager.addPerson(personConstructionData);

					/*
					 * Show that the person is in the correct region with the
					 * correct region arrival time
					 */
					RegionId personRegionId = regionsDataManager.getPersonRegion(personId);
					assertEquals(randomRegionId, personRegionId);
					assertEquals(c2.getTime(), regionsDataManager.getPersonRegionArrivalTime(personId));

				}, randomGenerator.nextDouble() * 1000);
			}

		}).getPlugins());;

		// precondition check: if no region data was included in the event
		TestSimulation.executeSimulation(RegionsTestPluginFactory.factory(0, 7737810808059858455L, TimeTrackingPolicy.TRACK_TIME, (c) -> {
			PeopleDataManager peopleDataManager = c.getDataManager(PeopleDataManager.class);
			PersonConstructionData personConstructionData = PersonConstructionData.builder().build();
			ContractException contractException = assertThrows(ContractException.class, () -> peopleDataManager.addPerson(personConstructionData));
			assertEquals(RegionError.NULL_REGION_ID, contractException.getErrorType());
		}).getPlugins());;

		// precondition check: if the region in the event is unknown
		TestSimulation.executeSimulation(RegionsTestPluginFactory.factory(0, 2879410509293373914L, TimeTrackingPolicy.TRACK_TIME, (c) -> {
			PeopleDataManager peopleDataManager = c.getDataManager(PeopleDataManager.class);
			PersonConstructionData personConstructionData = PersonConstructionData.builder().add(TestRegionId.getUnknownRegionId()).build();
			ContractException contractException = assertThrows(ContractException.class, () -> peopleDataManager.addPerson(personConstructionData));
			assertEquals(RegionError.UNKNOWN_REGION_ID, contractException.getErrorType());
		}).getPlugins());;

		// precondition check: if the person id does not exist
		/*
		 * Note : it is not possible to force the PersonDataManager to release
		 * such an event, so we release it from a test data manager
		 */
		TestPluginData.Builder pluginBuilder = TestPluginData.builder();
		
		pluginBuilder.addTestDataManager("dm", ()->new PassThroughDataManager());
		pluginBuilder.addTestActorPlan("actor", new TestActorPlan(0, (c) -> {
			PersonConstructionData personConstructionData = PersonConstructionData.builder().add(TestRegionId.REGION_1).build();
			PersonImminentAdditionEvent personImminentAdditionEvent = new PersonImminentAdditionEvent(new PersonId(10000), personConstructionData);

			PassThroughDataManager passThroughDataManager = c.getDataManager(PassThroughDataManager.class);
			ContractException contractException = assertThrows(ContractException.class,()->passThroughDataManager.passThrough(personImminentAdditionEvent));			
			assertEquals(PersonError.UNKNOWN_PERSON_ID, contractException.getErrorType());
		}));

		TestPluginData testPluginData = pluginBuilder.build();
		TestSimulation.executeSimulation(RegionsTestPluginFactory.factory(0, 5311711819224332248L, TimeTrackingPolicy.TRACK_TIME, testPluginData).getPlugins());

		// precondition check: if the person was previously added
		/*
		 * Note : it is not possible to force the PersonDataManager to release
		 * such an event, so we release it from a test data manager
		 */
		pluginBuilder = TestPluginData.builder();
		pluginBuilder.addTestDataManager("dm", ()->new PassThroughDataManager());
		pluginBuilder.addTestActorPlan("actor", new TestActorPlan(0, (c) -> {
			PeopleDataManager peopleDataManager = c.getDataManager(PeopleDataManager.class);
			PersonConstructionData personConstructionData = PersonConstructionData.builder().add(TestRegionId.REGION_1).build();
			PersonId personId = peopleDataManager.addPerson(personConstructionData);

			PersonImminentAdditionEvent personImminentAdditionEvent = new PersonImminentAdditionEvent(personId, personConstructionData);
			PassThroughDataManager passThroughDataManager = c.getDataManager(PassThroughDataManager.class);
			ContractException contractException = assertThrows(ContractException.class,()->passThroughDataManager.passThrough(personImminentAdditionEvent));
			assertEquals(RegionError.DUPLICATE_PERSON_ADDITION, contractException.getErrorType());
		}));

		
		
		pluginBuilder.addPluginDependency(PeoplePluginId.PLUGIN_ID);
		testPluginData = pluginBuilder.build();
		TestSimulation.executeSimulation(RegionsTestPluginFactory.factory(0, 5824136557013438265L, TimeTrackingPolicy.TRACK_TIME, testPluginData).getPlugins());

	}

	@Test
	@UnitTestMethod(target = RegionsDataManager.class, name = "defineRegionProperty", args = { RegionPropertyDefinitionInitialization.class })
	public void testDefineRegionProperty() {

		Set<MultiKey> expectedObservations = new LinkedHashSet<>();
		Set<MultiKey> actualObservations = new LinkedHashSet<>();

		RegionPropertyId regionPropertyId_1 = TestRegionPropertyId.getUnknownRegionPropertyId();
		RegionPropertyId regionPropertyId_2 = TestRegionPropertyId.getUnknownRegionPropertyId();

		TestPluginData.Builder pluginBuilder = TestPluginData.builder();

		// add an observer
		pluginBuilder.addTestActorPlan("observer", new TestActorPlan(0, (c) -> {
			c.subscribe(EventFilter.builder(RegionPropertyDefinitionEvent.class).build(), (c2, e) -> {
				MultiKey multiKey = new MultiKey(c2.getTime(), e.regionPropertyId());
				actualObservations.add(multiKey);
			});
		}));

		// have an actor define property 1
		pluginBuilder.addTestActorPlan("actor", new TestActorPlan(1, (c) -> {
			RegionsDataManager regionsDataManager = c.getDataManager(RegionsDataManager.class);
			assertFalse(regionsDataManager.regionPropertyIdExists(regionPropertyId_1));
			PropertyDefinition propertyDefinition = PropertyDefinition.builder().setType(Integer.class).setDefaultValue(55).build();
			RegionPropertyDefinitionInitialization.Builder propertyBuilder = RegionPropertyDefinitionInitialization.builder();
			propertyBuilder.setRegionPropertyId(regionPropertyId_1).setPropertyDefinition(propertyDefinition);
			Set<RegionId> regionIds = regionsDataManager.getRegionIds();
			assertFalse(regionIds.isEmpty());
			int value = 0;
			Map<RegionId, Integer> expectedValues = new LinkedHashMap<>();
			for (RegionId regionId : regionIds) {
				propertyBuilder.addPropertyValue(regionId, value);
				expectedValues.put(regionId, value);
				value++;
			}

			RegionPropertyDefinitionInitialization regionPropertyDefinitionInitialization = propertyBuilder.build();

			regionsDataManager.defineRegionProperty(regionPropertyDefinitionInitialization);
			assertTrue(regionsDataManager.regionPropertyIdExists(regionPropertyId_1));
			assertTrue(regionsDataManager.getRegionPropertyIds().contains(regionPropertyId_1));
			PropertyDefinition actualPropertyDefinition = regionsDataManager.getRegionPropertyDefinition(regionPropertyId_1);
			assertEquals(propertyDefinition, actualPropertyDefinition);
			MultiKey multiKey = new MultiKey(c.getTime(), regionPropertyId_1);
			expectedObservations.add(multiKey);

			for (RegionId regionId : regionIds) {
				Integer expectedValue = expectedValues.get(regionId);
				Integer actualValue = regionsDataManager.getRegionPropertyValue(regionId, regionPropertyId_1);
				assertEquals(expectedValue, actualValue);
			}

		}));

		// have an actor define property 2 having no default property
		pluginBuilder.addTestActorPlan("actor", new TestActorPlan(2, (c) -> {
			RegionsDataManager regionsDataManager = c.getDataManager(RegionsDataManager.class);
			Set<RegionId> regionIds = regionsDataManager.getRegionIds();
			assertFalse(regionIds.isEmpty());

			assertFalse(regionsDataManager.regionPropertyIdExists(regionPropertyId_2));
			String defaultValue = "default value";
			PropertyDefinition propertyDefinition = PropertyDefinition.builder().setType(String.class).setDefaultValue(defaultValue).build();
			RegionPropertyDefinitionInitialization regionPropertyDefinitionInitialization = RegionPropertyDefinitionInitialization	.builder().setRegionPropertyId(regionPropertyId_2)
																																	.setPropertyDefinition(propertyDefinition).build();
			regionsDataManager.defineRegionProperty(regionPropertyDefinitionInitialization);
			assertTrue(regionsDataManager.regionPropertyIdExists(regionPropertyId_2));
			assertTrue(regionsDataManager.getRegionPropertyIds().contains(regionPropertyId_2));
			PropertyDefinition actualPropertyDefinition = regionsDataManager.getRegionPropertyDefinition(regionPropertyId_2);
			assertEquals(propertyDefinition, actualPropertyDefinition);
			MultiKey multiKey = new MultiKey(c.getTime(), regionPropertyId_2);
			expectedObservations.add(multiKey);
			for (RegionId regionId : regionIds) {
				String actualValue = regionsDataManager.getRegionPropertyValue(regionId, regionPropertyId_2);
				assertEquals(defaultValue, actualValue);
			}

		}));

		pluginBuilder.addTestActorPlan("observer", new TestActorPlan(3, (c) -> {
			assertFalse(expectedObservations.isEmpty());
			assertEquals(expectedObservations, actualObservations);
		}));

		TestPluginData testPluginData = pluginBuilder.build();
		TestSimulation.executeSimulation(RegionsTestPluginFactory.factory(0, 6410427420030580842L, TimeTrackingPolicy.TRACK_TIME, testPluginData).getPlugins());

		/*
		 * precondition test: if the region property definition initialization
		 * is null
		 */
		TestSimulation.executeSimulation(RegionsTestPluginFactory.factory(0, 755408328420621219L, TimeTrackingPolicy.TRACK_TIME, (c) -> {
			RegionsDataManager regionsDataManager = c.getDataManager(RegionsDataManager.class);
			ContractException contractException = assertThrows(ContractException.class, () -> regionsDataManager.defineRegionProperty(null));
			assertEquals(RegionError.NULL_REGION_PROPERTY_DEFINITION_INITIALIZATION, contractException.getErrorType());
		}).getPlugins());;

		/*
		 * precondition test: if the region property is already defined
		 */
		TestSimulation.executeSimulation(RegionsTestPluginFactory.factory(0, 1524991526094322535L, TimeTrackingPolicy.TRACK_TIME, (c) -> {
			RegionsDataManager regionsDataManager = c.getDataManager(RegionsDataManager.class);
			ContractException contractException = assertThrows(ContractException.class, () -> {

				PropertyDefinition propertyDefinition = PropertyDefinition	.builder()//
																			.setType(Integer.class)//
																			.setDefaultValue(7)//
																			.build();

				RegionPropertyDefinitionInitialization regionPropertyDefinitionInitialization = //
						RegionPropertyDefinitionInitialization	.builder()//
																.setPropertyDefinition(propertyDefinition)//
																.setRegionPropertyId(TestRegionPropertyId.REGION_PROPERTY_2_INTEGER_MUTABLE)//
																.build();
				regionsDataManager.defineRegionProperty(regionPropertyDefinitionInitialization);
			});
			assertEquals(PropertyError.DUPLICATE_PROPERTY_DEFINITION, contractException.getErrorType());
		}).getPlugins());;

		/*
		 * precondition test: if the region property definition has no default
		 * and a property value for some region is missing from the
		 * RegionPropertyDefinitionInitialization
		 * 
		 */
		TestSimulation.executeSimulation(RegionsTestPluginFactory.factory(0, 737227361871382193L, TimeTrackingPolicy.TRACK_TIME, (c) -> {
			RegionsDataManager regionsDataManager = c.getDataManager(RegionsDataManager.class);
			ContractException contractException = assertThrows(ContractException.class, () -> {

				PropertyDefinition propertyDefinition = PropertyDefinition	.builder()//
																			.setType(Integer.class)//
																			.build();

				RegionPropertyDefinitionInitialization regionPropertyDefinitionInitialization = //
						RegionPropertyDefinitionInitialization	.builder()//
																.setPropertyDefinition(propertyDefinition)//
																.setRegionPropertyId(TestRegionPropertyId.getUnknownRegionPropertyId())//
																.build();

				regionsDataManager.defineRegionProperty(regionPropertyDefinitionInitialization);

			});
			assertEquals(PropertyError.INSUFFICIENT_PROPERTY_VALUE_ASSIGNMENT, contractException.getErrorType());
		}).getPlugins());;

		// * <li>{@linkplain
		// PropertyError#INSUFFICIENT_PROPERTY_VALUE_ASSIGNMENT}
		// * </li>

	}

	@Test
	@UnitTestMethod(target = RegionsDataManager.class, name = "addRegion", args = { RegionConstructionData.class })
	public void testAddRegion() {

		TestPluginData.Builder pluginBuilder = TestPluginData.builder();

		Set<MultiKey> expectedObservations = new LinkedHashSet<>();
		Set<MultiKey> actualObservations = new LinkedHashSet<>();

		pluginBuilder.addTestActorPlan("observer", new TestActorPlan(0, (c) -> {
			c.subscribe(EventFilter.builder(RegionAdditionEvent.class).build(), (c2, e) -> {
				MultiKey multiKey = new MultiKey(c2.getTime(), e.getRegionId());
				actualObservations.add(multiKey);
			});
		}));

		pluginBuilder.addTestActorPlan("actor", new TestActorPlan(1, (c) -> {
			StochasticsDataManager stochasticsDataManager = c.getDataManager(StochasticsDataManager.class);
			RandomGenerator randomGenerator = stochasticsDataManager.getRandomGenerator();

			RegionsDataManager regionsDataManager = c.getDataManager(RegionsDataManager.class);
			RegionId newRegionId = TestRegionId.getUnknownRegionId();
			RegionConstructionData.Builder builder = RegionConstructionData.builder().setRegionId(newRegionId);//
			for (TestRegionPropertyId testRegionPropertyId : TestRegionPropertyId.getPropertiesWithoutDefaultValues()) {
				builder.setRegionPropertyValue(testRegionPropertyId, testRegionPropertyId.getRandomPropertyValue(randomGenerator));
			}
			RegionConstructionData regionConstructionData = builder.build();
			regionsDataManager.addRegion(regionConstructionData);
			MultiKey multiKey = new MultiKey(c.getTime(), newRegionId);
			expectedObservations.add(multiKey);
		}));

		pluginBuilder.addTestActorPlan("actor", new TestActorPlan(2, (c) -> {
			StochasticsDataManager stochasticsDataManager = c.getDataManager(StochasticsDataManager.class);
			RandomGenerator randomGenerator = stochasticsDataManager.getRandomGenerator();
			RegionsDataManager regionsDataManager = c.getDataManager(RegionsDataManager.class);

			RegionId newRegionId = TestRegionId.getUnknownRegionId();
			RegionConstructionData.Builder builder = RegionConstructionData.builder().setRegionId(newRegionId);//
			for (TestRegionPropertyId testRegionPropertyId : TestRegionPropertyId.getPropertiesWithoutDefaultValues()) {
				builder.setRegionPropertyValue(testRegionPropertyId, testRegionPropertyId.getRandomPropertyValue(randomGenerator));
			}
			RegionConstructionData regionConstructionData = builder.build();
			regionsDataManager.addRegion(regionConstructionData);
			MultiKey multiKey = new MultiKey(c.getTime(), newRegionId);
			expectedObservations.add(multiKey);
		}));

		pluginBuilder.addTestActorPlan("observer", new TestActorPlan(3, (c) -> {
			assertEquals(expectedObservations, actualObservations);
		}));

		TestPluginData testPluginData = pluginBuilder.build();
		TestSimulation.executeSimulation(RegionsTestPluginFactory.factory(0, 4801681059718243112L, TimeTrackingPolicy.TRACK_TIME, testPluginData).getPlugins());

		/*
		 * precondition test: if the region construction data is null
		 */
		TestSimulation.executeSimulation(RegionsTestPluginFactory.factory(0, 1930072318129921567L, TimeTrackingPolicy.TRACK_TIME, (c) -> {
			RegionsDataManager regionsDataManager = c.getDataManager(RegionsDataManager.class);
			ContractException contractException = assertThrows(ContractException.class, () -> regionsDataManager.addRegion(null));
			assertEquals(RegionError.NULL_REGION_CONSTRUCTION_DATA, contractException.getErrorType());
		}).getPlugins());;

		/*
		 * precondition test: if the region is already present
		 */
		TestSimulation.executeSimulation(RegionsTestPluginFactory.factory(0, 4107332213003089045L, TimeTrackingPolicy.TRACK_TIME, (c) -> {
			RegionsDataManager regionsDataManager = c.getDataManager(RegionsDataManager.class);
			RegionConstructionData regionConstructionData = RegionConstructionData.builder().setRegionId(TestRegionId.REGION_1).build();
			ContractException contractException = assertThrows(ContractException.class, () -> regionsDataManager.addRegion(regionConstructionData));
			assertEquals(RegionError.DUPLICATE_REGION_ID, contractException.getErrorType());
		}).getPlugins());;

		/*
		 * precondition test: if not all region properties have default values
		 */
		testConsumerWithNoDefaultRegionProperties(6895625301110154531L, (c) -> {
			RegionsDataManager regionsDataManager = c.getDataManager(RegionsDataManager.class);
			RegionConstructionData regionConstructionData = RegionConstructionData.builder().setRegionId(TestRegionId.getUnknownRegionId()).build();
			ContractException contractException = assertThrows(ContractException.class, () -> regionsDataManager.addRegion(regionConstructionData));
			assertEquals(PropertyError.INSUFFICIENT_PROPERTY_VALUE_ASSIGNMENT, contractException.getErrorType());
		});

	}

	/*
	 * Executes the simulation with each of the region properties defined
	 * without default values
	 * 
	 */
	private static void testConsumerWithNoDefaultRegionProperties(long seed, Consumer<ActorContext> consumer) {

		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(seed);

		TestPluginData.Builder pluginBuilder = TestPluginData.builder();

		pluginBuilder.addTestActorPlan("actor", new TestActorPlan(0, consumer));
		TestPluginData testPluginData = pluginBuilder.build();

		// add the region plugin
		RegionsPluginData.Builder regionPluginBuilder = RegionsPluginData.builder();
		for (TestRegionId regionId : TestRegionId.values()) {
			regionPluginBuilder.addRegion(regionId);
		}

		for (TestRegionPropertyId testRegionPropertyId : TestRegionPropertyId.values()) {
			// create a property definition with no default
			PropertyDefinition propertyDefinition = testRegionPropertyId.getPropertyDefinition();
			propertyDefinition = PropertyDefinition.builder().setType(propertyDefinition.getType()).build();
			regionPluginBuilder.defineRegionProperty(testRegionPropertyId, propertyDefinition);
			for (TestRegionId regionId : TestRegionId.values()) {
				regionPluginBuilder.setRegionPropertyValue(regionId, testRegionPropertyId, testRegionPropertyId.getRandomPropertyValue(randomGenerator));
			}
		}

		RegionsPluginData regionsPluginData = regionPluginBuilder.build();

		TestSimulation.executeSimulation(RegionsTestPluginFactory.factory(0, seed, TimeTrackingPolicy.TRACK_TIME, testPluginData).setRegionsPluginData(regionsPluginData).getPlugins());



	}

	@Test
	@UnitTestMethod(target = RegionsDataManager.class, name = "getEventFilterForPersonRegionUpdateEvent_ByArrivalRegion", args = { RegionId.class })
	public void testGetEventFilterForPersonRegionUpdateEvent_ByArrivalRegion() {

		int numberOfPeople = 30;

		TestPluginData.Builder pluginBuilder = TestPluginData.builder();

		// create some containers for movement observations
		List<MultiKey> recievedObservations = new ArrayList<>();
		List<MultiKey> expectedObservations = new ArrayList<>();

		Set<TestRegionId> selectedRegions = new LinkedHashSet<>();
		selectedRegions.add(TestRegionId.REGION_1);
		selectedRegions.add(TestRegionId.REGION_4);
		selectedRegions.add(TestRegionId.REGION_6);

		/*
		 * Have the observer agent observe all movements into the selected
		 * regions observations
		 */
		pluginBuilder.addTestActorPlan("observer", new TestActorPlan(0, (c) -> {

			for (TestRegionId testRegionId : selectedRegions) {
				RegionsDataManager regionsDataManager = c.getDataManager(RegionsDataManager.class);
				EventFilter<PersonRegionUpdateEvent> eventFilter = regionsDataManager.getEventFilterForPersonRegionUpdateEvent_ByArrivalRegion(testRegionId);
				c.subscribe(eventFilter, (c2, e) -> {
					recievedObservations.add(new MultiKey(e.previousRegionId(), e.currentRegionId(), e.personId(), c2.getTime()));
				});
			}
		}));

		/*
		 * Have the mover agent move every person over time
		 */
		pluginBuilder.addTestActorPlan("mover", new TestActorPlan(1, (c) -> {

			/*
			 * Make sure that there are actually people in the simulation so
			 * that test is actually testing something
			 */
			PeopleDataManager peopleDataManager = c.getDataManager(PeopleDataManager.class);
			RegionsDataManager regionsDataManager = c.getDataManager(RegionsDataManager.class);

			List<PersonId> people = peopleDataManager.getPeople();
			assertTrue(people.size() > 0);

			// schedule a time for each person to be moved to a new region
			double planTime = 10;
			for (final PersonId personId : people) {
				c.addPlan((c2) -> {
					TestRegionId regionId = regionsDataManager.getPersonRegion(personId);
					TestRegionId nextRegionId = regionId.next();
					regionsDataManager.setPersonRegion(personId, nextRegionId);

					if (selectedRegions.contains(nextRegionId)) {
						expectedObservations.add(new MultiKey(regionId, nextRegionId, personId, c2.getTime()));
					}
				}, planTime);
				planTime += 5;
			}
		}));

		// build the plugin
		TestPluginData testPluginData = pluginBuilder.build();
		TestSimulation.executeSimulation(RegionsTestPluginFactory.factory(numberOfPeople, 6280260397394362229L, TimeTrackingPolicy.TRACK_TIME, testPluginData).getPlugins());

		// show that the observations were correct
		assertEquals(expectedObservations.size(), recievedObservations.size());
		assertEquals(new LinkedHashSet<>(expectedObservations), new LinkedHashSet<>(recievedObservations));

		/*
		 * precondition test: if the region id is null
		 */
		TestSimulation.executeSimulation(RegionsTestPluginFactory.factory(numberOfPeople, 8703868236194395945L, TimeTrackingPolicy.TRACK_TIME, (c) -> {
			ContractException contractException = assertThrows(ContractException.class, () -> {
				RegionsDataManager regionsDataManager = c.getDataManager(RegionsDataManager.class);
				regionsDataManager.getEventFilterForPersonRegionUpdateEvent_ByArrivalRegion(null);
			});
			assertEquals(RegionError.NULL_REGION_ID, contractException.getErrorType());
		}).getPlugins());;
		/*
		 * precondition test: if the region id is not known
		 */
		TestSimulation.executeSimulation(RegionsTestPluginFactory.factory(numberOfPeople, 1521124301443522213L, TimeTrackingPolicy.TRACK_TIME, (c) -> {
			ContractException contractException = assertThrows(ContractException.class, () -> {
				RegionsDataManager regionsDataManager = c.getDataManager(RegionsDataManager.class);
				regionsDataManager.getEventFilterForPersonRegionUpdateEvent_ByArrivalRegion(TestRegionId.getUnknownRegionId());
			});
			assertEquals(RegionError.UNKNOWN_REGION_ID, contractException.getErrorType());
		}).getPlugins());;

	}

	@Test
	@UnitTestMethod(target = RegionsDataManager.class, name = "getEventFilterForPersonRegionUpdateEvent_ByDepartureRegion", args = { RegionId.class })
	public void testGetEventFilterForPersonRegionUpdateEvent_ByDepartureRegion() {

		int numberOfPeople = 30;

		TestPluginData.Builder pluginBuilder = TestPluginData.builder();

		// create some containers for movement observations
		List<MultiKey> recievedObservations = new ArrayList<>();
		List<MultiKey> expectedObservations = new ArrayList<>();

		Set<TestRegionId> selectedRegions = new LinkedHashSet<>();
		selectedRegions.add(TestRegionId.REGION_1);
		selectedRegions.add(TestRegionId.REGION_2);
		selectedRegions.add(TestRegionId.REGION_3);
		selectedRegions.add(TestRegionId.REGION_6);

		/*
		 * Have the observer agent observe all movements out of the selected
		 * regions observations
		 */
		pluginBuilder.addTestActorPlan("observer", new TestActorPlan(0, (c) -> {

			for (TestRegionId testRegionId : selectedRegions) {
				RegionsDataManager regionsDataManager = c.getDataManager(RegionsDataManager.class);
				EventFilter<PersonRegionUpdateEvent> eventFilter = regionsDataManager.getEventFilterForPersonRegionUpdateEvent_ByDepartureRegion(testRegionId);
				c.subscribe(eventFilter, (c2, e) -> {
					recievedObservations.add(new MultiKey(e.previousRegionId(), e.currentRegionId(), e.personId(), c2.getTime()));
				});
			}
		}));

		/*
		 * Have the mover agent move every person over time
		 */
		pluginBuilder.addTestActorPlan("mover", new TestActorPlan(1, (c) -> {

			/*
			 * Make sure that there are actually people in the simulation so
			 * that test is actually testing something
			 */
			PeopleDataManager peopleDataManager = c.getDataManager(PeopleDataManager.class);
			RegionsDataManager regionsDataManager = c.getDataManager(RegionsDataManager.class);

			List<PersonId> people = peopleDataManager.getPeople();
			assertTrue(people.size() > 0);

			// schedule a time for each person to be moved to a new region
			double planTime = 10;
			for (final PersonId personId : people) {
				c.addPlan((c2) -> {
					TestRegionId regionId = regionsDataManager.getPersonRegion(personId);
					TestRegionId nextRegionId = regionId.next();
					regionsDataManager.setPersonRegion(personId, nextRegionId);

					if (selectedRegions.contains(regionId)) {
						expectedObservations.add(new MultiKey(regionId, nextRegionId, personId, c2.getTime()));
					}
				}, planTime);
				planTime += 5;
			}
		}));

		// build the plugin
		TestPluginData testPluginData = pluginBuilder.build();
		TestSimulation.executeSimulation(RegionsTestPluginFactory.factory(numberOfPeople, 5906547765098032882L, TimeTrackingPolicy.TRACK_TIME, testPluginData).getPlugins());

		// show that the observations were correct
		assertEquals(expectedObservations.size(), recievedObservations.size());
		assertEquals(new LinkedHashSet<>(expectedObservations), new LinkedHashSet<>(recievedObservations));

		/*
		 * precondition test: if the region id is null
		 */
		TestSimulation.executeSimulation(RegionsTestPluginFactory.factory(numberOfPeople, 5941332064278474841L, TimeTrackingPolicy.TRACK_TIME, (c) -> {
			ContractException contractException = assertThrows(ContractException.class, () -> {
				RegionsDataManager regionsDataManager = c.getDataManager(RegionsDataManager.class);
				regionsDataManager.getEventFilterForPersonRegionUpdateEvent_ByDepartureRegion(null);
			});
			assertEquals(RegionError.NULL_REGION_ID, contractException.getErrorType());
		}).getPlugins());;
		/*
		 * precondition test: if the region id is not known
		 */
		TestSimulation.executeSimulation(RegionsTestPluginFactory.factory(numberOfPeople, 5981948058533294963L, TimeTrackingPolicy.TRACK_TIME, (c) -> {
			ContractException contractException = assertThrows(ContractException.class, () -> {
				RegionsDataManager regionsDataManager = c.getDataManager(RegionsDataManager.class);
				regionsDataManager.getEventFilterForPersonRegionUpdateEvent_ByDepartureRegion(TestRegionId.getUnknownRegionId());
			});
			assertEquals(RegionError.UNKNOWN_REGION_ID, contractException.getErrorType());
		}).getPlugins());;

	}

	@Test
	@UnitTestMethod(target = RegionsDataManager.class, name = "getEventFilterForPersonRegionUpdateEvent", args = { PersonId.class })
	public void testGetEventFilterForPersonRegionUpdateEvent_Person() {
		int numberOfPeople = 30;

		TestPluginData.Builder pluginBuilder = TestPluginData.builder();

		// create some containers for movement observations
		List<MultiKey> recievedObservations = new ArrayList<>();
		List<MultiKey> expectedObservations = new ArrayList<>();

		Set<PersonId> selectedPeople = new LinkedHashSet<>();

		/*
		 * Have the observer agent observe all movements out of the selected
		 * regions observations
		 */
		pluginBuilder.addTestActorPlan("observer", new TestActorPlan(0, (c) -> {
			PeopleDataManager peopleDataManager = c.getDataManager(PeopleDataManager.class);
			RegionsDataManager regionsDataManager = c.getDataManager(RegionsDataManager.class);
			StochasticsDataManager stochasticsDataManager = c.getDataManager(StochasticsDataManager.class);
			RandomGenerator randomGenerator = stochasticsDataManager.getRandomGenerator();

			List<PersonId> people = peopleDataManager.getPeople();
			for (PersonId personId : people) {
				if (randomGenerator.nextBoolean()) {
					selectedPeople.add(personId);
					EventFilter<PersonRegionUpdateEvent> eventFilter = regionsDataManager.getEventFilterForPersonRegionUpdateEvent(personId);
					c.subscribe(eventFilter, (c2, e) -> {
						recievedObservations.add(new MultiKey(e.previousRegionId(), e.currentRegionId(), e.personId(), c2.getTime()));
					});
				}
			}
		}));

		/*
		 * Have the mover agent move every person over time
		 */
		pluginBuilder.addTestActorPlan("mover", new TestActorPlan(1, (c) -> {

			/*
			 * Make sure that there are actually people in the simulation so
			 * that test is actually testing something
			 */
			PeopleDataManager peopleDataManager = c.getDataManager(PeopleDataManager.class);
			RegionsDataManager regionsDataManager = c.getDataManager(RegionsDataManager.class);

			List<PersonId> people = peopleDataManager.getPeople();
			assertTrue(people.size() > 0);

			// schedule a time for each person to be moved to a new region
			double planTime = 10;
			for (final PersonId personId : people) {
				c.addPlan((c2) -> {
					TestRegionId regionId = regionsDataManager.getPersonRegion(personId);
					TestRegionId nextRegionId = regionId.next();
					regionsDataManager.setPersonRegion(personId, nextRegionId);

					if (selectedPeople.contains(personId)) {
						expectedObservations.add(new MultiKey(regionId, nextRegionId, personId, c2.getTime()));
					}
				}, planTime);
				planTime += 5;
			}
		}));

		// build the plugin
		TestPluginData testPluginData = pluginBuilder.build();
		TestSimulation.executeSimulation(RegionsTestPluginFactory.factory(numberOfPeople, 3786801901191355144L, TimeTrackingPolicy.TRACK_TIME, testPluginData).getPlugins());

		// show that the observations were correct
		assertEquals(expectedObservations.size(), recievedObservations.size());
		assertEquals(new LinkedHashSet<>(expectedObservations), new LinkedHashSet<>(recievedObservations));

		/*
		 * precondition test: if the person id is null
		 */
		TestSimulation.executeSimulation(RegionsTestPluginFactory.factory(numberOfPeople, 4504604454474342921L, TimeTrackingPolicy.TRACK_TIME, (c) -> {
			ContractException contractException = assertThrows(ContractException.class, () -> {
				RegionsDataManager regionsDataManager = c.getDataManager(RegionsDataManager.class);
				PersonId nullPersonId = null;
				regionsDataManager.getEventFilterForPersonRegionUpdateEvent(nullPersonId);
			});
			assertEquals(PersonError.NULL_PERSON_ID, contractException.getErrorType());
		}).getPlugins());;
		/*
		 * precondition test: if the person id is not known
		 */
		TestSimulation.executeSimulation(RegionsTestPluginFactory.factory(numberOfPeople, 1166492228021587827L, TimeTrackingPolicy.TRACK_TIME, (c) -> {
			ContractException contractException = assertThrows(ContractException.class, () -> {
				RegionsDataManager regionsDataManager = c.getDataManager(RegionsDataManager.class);
				regionsDataManager.getEventFilterForPersonRegionUpdateEvent(new PersonId(1000000));
			});
			assertEquals(PersonError.UNKNOWN_PERSON_ID, contractException.getErrorType());
		}).getPlugins());;
	}

	@Test
	@UnitTestMethod(target = RegionsDataManager.class, name = "getEventFilterForPersonRegionUpdateEvent", args = {})
	public void testGetEventFilterForPersonRegionUpdateEvent() {

		int numberOfPeople = 30;

		TestPluginData.Builder pluginBuilder = TestPluginData.builder();

		// create some containers for movement observations
		List<MultiKey> recievedObservations = new ArrayList<>();
		List<MultiKey> expectedObservations = new ArrayList<>();

		/*
		 * Have the observer agent observe all movements out of the selected
		 * regions observations
		 */
		pluginBuilder.addTestActorPlan("observer", new TestActorPlan(0, (c) -> {
			PeopleDataManager peopleDataManager = c.getDataManager(PeopleDataManager.class);
			RegionsDataManager regionsDataManager = c.getDataManager(RegionsDataManager.class);

			List<PersonId> people = peopleDataManager.getPeople();
			for (PersonId personId : people) {
				EventFilter<PersonRegionUpdateEvent> eventFilter = regionsDataManager.getEventFilterForPersonRegionUpdateEvent(personId);
				c.subscribe(eventFilter, (c2, e) -> {
					recievedObservations.add(new MultiKey(e.previousRegionId(), e.currentRegionId(), e.personId(), c2.getTime()));
				});
			}
		}));

		/*
		 * Have the mover agent move every person over time
		 */
		pluginBuilder.addTestActorPlan("mover", new TestActorPlan(1, (c) -> {

			/*
			 * Make sure that there are actually people in the simulation so
			 * that test is actually testing something
			 */
			PeopleDataManager peopleDataManager = c.getDataManager(PeopleDataManager.class);
			RegionsDataManager regionsDataManager = c.getDataManager(RegionsDataManager.class);

			List<PersonId> people = peopleDataManager.getPeople();
			assertTrue(people.size() > 0);

			// schedule a time for each person to be moved to a new region
			double planTime = 10;
			for (final PersonId personId : people) {
				c.addPlan((c2) -> {
					TestRegionId regionId = regionsDataManager.getPersonRegion(personId);
					TestRegionId nextRegionId = regionId.next();
					regionsDataManager.setPersonRegion(personId, nextRegionId);
					expectedObservations.add(new MultiKey(regionId, nextRegionId, personId, c2.getTime()));
				}, planTime);
				planTime += 5;
			}
		}));

		// build the plugin
		TestPluginData testPluginData = pluginBuilder.build();
		TestSimulation.executeSimulation(RegionsTestPluginFactory.factory(numberOfPeople, 8773677547139261431L, TimeTrackingPolicy.TRACK_TIME, testPluginData).getPlugins());

		// show that the observations were correct
		assertEquals(expectedObservations.size(), recievedObservations.size());
		assertEquals(new LinkedHashSet<>(expectedObservations), new LinkedHashSet<>(recievedObservations));

	}

	@Test
	@UnitTestMethod(target = RegionsDataManager.class, name = "getEventFilterForRegionPropertyUpdateEvent", args = { RegionPropertyId.class })
	public void testGetEventFilterForRegionPropertyUpdateEvent_Region() {

		TestPluginData.Builder pluginBuilder = TestPluginData.builder();

		// create containers to hold actual and expected observations
		Set<MultiKey> actualObservations = new LinkedHashSet<>();
		Set<MultiKey> expectedObservations = new LinkedHashSet<>();

		Set<TestRegionPropertyId> selectedPropertyIds = new LinkedHashSet<>();
		selectedPropertyIds.add(TestRegionPropertyId.REGION_PROPERTY_1_BOOLEAN_MUTABLE);
		selectedPropertyIds.add(TestRegionPropertyId.REGION_PROPERTY_2_INTEGER_MUTABLE);

		// Have the observer agent observe updates to the selected properties
		pluginBuilder.addTestActorPlan("observer", new TestActorPlan(0, (c) -> {
			RegionsDataManager regionsDataManager = c.getDataManager(RegionsDataManager.class);
			for (TestRegionPropertyId testRegionPropertyId : selectedPropertyIds) {
				EventFilter<RegionPropertyUpdateEvent> eventFilter = regionsDataManager.getEventFilterForRegionPropertyUpdateEvent(testRegionPropertyId);
				c.subscribe(eventFilter, (c2, e) -> {
					actualObservations.add(new MultiKey(c2.getTime(), e.regionId(), e.regionPropertyId(), e.currentPropertyValue()));
				});
			}
		}));

		int comparisonDay = 100;

		// Have the update agent make various region property updates over
		// time
		pluginBuilder.addTestActorPlan("update", new TestActorPlan(0, (c) -> {

			for (int i = 1; i < comparisonDay; i++) {
				c.addPlan((c2) -> {
					StochasticsDataManager stochasticsDataManager2 = c2.getDataManager(StochasticsDataManager.class);
					RandomGenerator randomGenerator = stochasticsDataManager2.getRandomGenerator();
					RegionsDataManager regionsDataManager = c2.getDataManager(RegionsDataManager.class);

					TestRegionId regionId = TestRegionId.getRandomRegionId(randomGenerator);
					TestRegionPropertyId regionPropertyId = TestRegionPropertyId.getRandomMutableRegionPropertyId(randomGenerator);
					Object propertyValue = regionPropertyId.getRandomPropertyValue(randomGenerator);

					regionsDataManager.setRegionPropertyValue(regionId, regionPropertyId, propertyValue);

					if (selectedPropertyIds.contains(regionPropertyId)) {
						expectedObservations.add(new MultiKey(c2.getTime(), regionId, regionPropertyId, propertyValue));
					}

				}, i);
			}
		}));

		// Have the observer agent observe show observed changes match
		// expectations
		pluginBuilder.addTestActorPlan("observer", new TestActorPlan(comparisonDay, (c) -> {
			assertTrue(expectedObservations.size() > 0);
			assertEquals(expectedObservations, actualObservations);
		}));

		TestPluginData testPluginData = pluginBuilder.build();
		TestSimulation.executeSimulation(RegionsTestPluginFactory.factory(0, 1827237237983764002L, TimeTrackingPolicy.TRACK_TIME, testPluginData).getPlugins());

		// precondition check: if the region property id is null
		TestSimulation.executeSimulation(RegionsTestPluginFactory.factory(0, 2294490256521547918L, TimeTrackingPolicy.TRACK_TIME, (c) -> {
			RegionsDataManager regionsDataManager = c.getDataManager(RegionsDataManager.class);
			ContractException contractException = assertThrows(ContractException.class, () -> regionsDataManager.getEventFilterForRegionPropertyUpdateEvent(null));
			assertEquals(PropertyError.NULL_PROPERTY_ID, contractException.getErrorType());
		}).getPlugins());;

		// precondition check: if the region property id is unknown
		TestSimulation.executeSimulation(RegionsTestPluginFactory.factory(0, 4878569785353296577L, TimeTrackingPolicy.TRACK_TIME, (c) -> {
			RegionsDataManager regionsDataManager = c.getDataManager(RegionsDataManager.class);
			ContractException contractException = assertThrows(ContractException.class,
					() -> regionsDataManager.getEventFilterForRegionPropertyUpdateEvent(TestRegionPropertyId.getUnknownRegionPropertyId()));
			assertEquals(PropertyError.UNKNOWN_PROPERTY_ID, contractException.getErrorType());
		}).getPlugins());;

	}

	@Test
	@UnitTestMethod(target = RegionsDataManager.class, name = "getEventFilterForRegionPropertyUpdateEvent", args = { RegionId.class, RegionPropertyId.class })
	public void getEventFilterForRegionPropertyUpdateEvent_Region_Property() {
		TestPluginData.Builder pluginBuilder = TestPluginData.builder();

		// create containers to hold actual and expected observations
		Set<MultiKey> actualObservations = new LinkedHashSet<>();
		Set<MultiKey> expectedObservations = new LinkedHashSet<>();

		Set<Pair<RegionId, RegionPropertyId>> selectedRegionPropertyPairs = new LinkedHashSet<>();
		selectedRegionPropertyPairs.add(new Pair<>(TestRegionId.REGION_1, TestRegionPropertyId.REGION_PROPERTY_1_BOOLEAN_MUTABLE));
		selectedRegionPropertyPairs.add(new Pair<>(TestRegionId.REGION_2, TestRegionPropertyId.REGION_PROPERTY_2_INTEGER_MUTABLE));
		selectedRegionPropertyPairs.add(new Pair<>(TestRegionId.REGION_3, TestRegionPropertyId.REGION_PROPERTY_3_DOUBLE_MUTABLE));
		selectedRegionPropertyPairs.add(new Pair<>(TestRegionId.REGION_4, TestRegionPropertyId.REGION_PROPERTY_1_BOOLEAN_MUTABLE));
		selectedRegionPropertyPairs.add(new Pair<>(TestRegionId.REGION_5, TestRegionPropertyId.REGION_PROPERTY_2_INTEGER_MUTABLE));
		selectedRegionPropertyPairs.add(new Pair<>(TestRegionId.REGION_6, TestRegionPropertyId.REGION_PROPERTY_3_DOUBLE_MUTABLE));

		// Have the observer agent observe updates to the selected
		// region/property pairs
		pluginBuilder.addTestActorPlan("observer", new TestActorPlan(0, (c) -> {
			RegionsDataManager regionsDataManager = c.getDataManager(RegionsDataManager.class);
			for (Pair<RegionId, RegionPropertyId> pair : selectedRegionPropertyPairs) {
				RegionId regionId = pair.getFirst();
				RegionPropertyId regionPropertyId = pair.getSecond();
				EventFilter<RegionPropertyUpdateEvent> eventFilter = regionsDataManager.getEventFilterForRegionPropertyUpdateEvent(regionId, regionPropertyId);
				c.subscribe(eventFilter, (c2, e) -> {
					actualObservations.add(new MultiKey(c2.getTime(), e.regionId(), e.regionPropertyId(), e.currentPropertyValue()));
				});
			}
		}));

		int comparisonDay = 100;

		// Have the update agent make various region property updates over
		// time
		pluginBuilder.addTestActorPlan("update", new TestActorPlan(0, (c) -> {

			for (int i = 1; i < comparisonDay; i++) {
				c.addPlan((c2) -> {
					StochasticsDataManager stochasticsDataManager2 = c2.getDataManager(StochasticsDataManager.class);
					RandomGenerator randomGenerator = stochasticsDataManager2.getRandomGenerator();
					RegionsDataManager regionsDataManager = c2.getDataManager(RegionsDataManager.class);

					TestRegionId regionId = TestRegionId.getRandomRegionId(randomGenerator);
					TestRegionPropertyId regionPropertyId = TestRegionPropertyId.getRandomMutableRegionPropertyId(randomGenerator);
					Object propertyValue = regionPropertyId.getRandomPropertyValue(randomGenerator);

					regionsDataManager.setRegionPropertyValue(regionId, regionPropertyId, propertyValue);

					Pair<RegionId, RegionPropertyId> pair = new Pair<>(regionId, regionPropertyId);
					if (selectedRegionPropertyPairs.contains(pair)) {
						expectedObservations.add(new MultiKey(c2.getTime(), regionId, regionPropertyId, propertyValue));
					}

				}, i);
			}
		}));

		// Have the observer agent observe show observed changes match
		// expectations
		pluginBuilder.addTestActorPlan("observer", new TestActorPlan(comparisonDay, (c) -> {
			assertTrue(expectedObservations.size() > 0);
			assertEquals(expectedObservations, actualObservations);
		}));

		TestPluginData testPluginData = pluginBuilder.build();
		TestSimulation.executeSimulation(RegionsTestPluginFactory.factory(0, 7132294759338470890L, TimeTrackingPolicy.TRACK_TIME, testPluginData).getPlugins());

		// precondition check: if the region property id is null
		TestSimulation.executeSimulation(RegionsTestPluginFactory.factory(0, 5168071523034596869L, TimeTrackingPolicy.TRACK_TIME, (c) -> {
			RegionsDataManager regionsDataManager = c.getDataManager(RegionsDataManager.class);
			ContractException contractException = assertThrows(ContractException.class, () -> regionsDataManager.getEventFilterForRegionPropertyUpdateEvent(TestRegionId.REGION_1, null));
			assertEquals(PropertyError.NULL_PROPERTY_ID, contractException.getErrorType());
		}).getPlugins());;

		// precondition check: if the region property id is unknown
		TestSimulation.executeSimulation(RegionsTestPluginFactory.factory(0, 5851898172389262566L, TimeTrackingPolicy.TRACK_TIME, (c) -> {
			RegionsDataManager regionsDataManager = c.getDataManager(RegionsDataManager.class);
			ContractException contractException = assertThrows(ContractException.class,
					() -> regionsDataManager.getEventFilterForRegionPropertyUpdateEvent(TestRegionId.REGION_1, TestRegionPropertyId.getUnknownRegionPropertyId()));
			assertEquals(PropertyError.UNKNOWN_PROPERTY_ID, contractException.getErrorType());
		}).getPlugins());;

		// precondition check: if the region id is null
		TestSimulation.executeSimulation(RegionsTestPluginFactory.factory(0, 3683702073309702135L, TimeTrackingPolicy.TRACK_TIME, (c) -> {
			RegionsDataManager regionsDataManager = c.getDataManager(RegionsDataManager.class);
			ContractException contractException = assertThrows(ContractException.class,
					() -> regionsDataManager.getEventFilterForRegionPropertyUpdateEvent(null, TestRegionPropertyId.REGION_PROPERTY_2_INTEGER_MUTABLE));
			assertEquals(RegionError.NULL_REGION_ID, contractException.getErrorType());
		}).getPlugins());;

		// precondition check: if the region id is unknown
		TestSimulation.executeSimulation(RegionsTestPluginFactory.factory(0, 6706349084351695058L, TimeTrackingPolicy.TRACK_TIME, (c) -> {
			RegionsDataManager regionsDataManager = c.getDataManager(RegionsDataManager.class);
			ContractException contractException = assertThrows(ContractException.class,
					() -> regionsDataManager.getEventFilterForRegionPropertyUpdateEvent(TestRegionId.getUnknownRegionId(), TestRegionPropertyId.REGION_PROPERTY_2_INTEGER_MUTABLE));
			assertEquals(RegionError.UNKNOWN_REGION_ID, contractException.getErrorType());
		}).getPlugins());;

	}

	@Test
	@UnitTestMethod(target = RegionsDataManager.class, name = "getEventFilterForRegionPropertyUpdateEvent", args = {})
	public void testGetEventFilterForRegionPropertyUpdateEvent() {
		TestPluginData.Builder pluginBuilder = TestPluginData.builder();

		// create containers to hold actual and expected observations
		Set<MultiKey> actualObservations = new LinkedHashSet<>();
		Set<MultiKey> expectedObservations = new LinkedHashSet<>();

		// Have the observer agent observe updates to the selected
		// region/property pairs
		pluginBuilder.addTestActorPlan("observer", new TestActorPlan(0, (c) -> {
			RegionsDataManager regionsDataManager = c.getDataManager(RegionsDataManager.class);
			EventFilter<RegionPropertyUpdateEvent> eventFilter = regionsDataManager.getEventFilterForRegionPropertyUpdateEvent();
			c.subscribe(eventFilter, (c2, e) -> {
				actualObservations.add(new MultiKey(c2.getTime(), e.regionId(), e.regionPropertyId(), e.currentPropertyValue()));
			});

		}));

		int comparisonDay = 100;

		// Have the update agent make various region property updates over
		// time
		pluginBuilder.addTestActorPlan("update", new TestActorPlan(0, (c) -> {

			for (int i = 1; i < comparisonDay; i++) {
				c.addPlan((c2) -> {
					StochasticsDataManager stochasticsDataManager2 = c2.getDataManager(StochasticsDataManager.class);
					RandomGenerator randomGenerator = stochasticsDataManager2.getRandomGenerator();
					RegionsDataManager regionsDataManager = c2.getDataManager(RegionsDataManager.class);

					TestRegionId regionId = TestRegionId.getRandomRegionId(randomGenerator);
					TestRegionPropertyId regionPropertyId = TestRegionPropertyId.getRandomMutableRegionPropertyId(randomGenerator);
					Object propertyValue = regionPropertyId.getRandomPropertyValue(randomGenerator);

					regionsDataManager.setRegionPropertyValue(regionId, regionPropertyId, propertyValue);

					expectedObservations.add(new MultiKey(c2.getTime(), regionId, regionPropertyId, propertyValue));

				}, i);
			}
		}));

		// Have the observer agent observe show observed changes match
		// expectations
		pluginBuilder.addTestActorPlan("observer", new TestActorPlan(comparisonDay, (c) -> {
			assertTrue(expectedObservations.size() > 0);
			assertEquals(expectedObservations, actualObservations);
		}));

		TestPluginData testPluginData = pluginBuilder.build();
		TestSimulation.executeSimulation(RegionsTestPluginFactory.factory(0, 6300334142182919392L, TimeTrackingPolicy.TRACK_TIME, testPluginData).getPlugins());
	}

	@Test
	@UnitTestMethod(target = RegionsDataManager.class, name = "getEventFilterForRegionAdditionEvent", args = {})
	public void testGetEventFilterForRegionAdditionEvent() {

		TestPluginData.Builder pluginBuilder = TestPluginData.builder();

		Set<MultiKey> expectedObservations = new LinkedHashSet<>();
		Set<MultiKey> actualObservations = new LinkedHashSet<>();

		pluginBuilder.addTestActorPlan("observer", new TestActorPlan(0, (c) -> {
			RegionsDataManager regionsDataManager = c.getDataManager(RegionsDataManager.class);
			EventFilter<RegionAdditionEvent> eventFilter = regionsDataManager.getEventFilterForRegionAdditionEvent();
			c.subscribe(eventFilter, (c2, e) -> {
				MultiKey multiKey = new MultiKey(c2.getTime(), e.getRegionId());
				actualObservations.add(multiKey);
			});
		}));

		int comparisonDay = 10;

		pluginBuilder.addTestActorPlan("actor", new TestActorPlan(1, (c) -> {

			for (int i = 1; i < comparisonDay; i++) {
				c.addPlan((c2) -> {
					StochasticsDataManager stochasticsDataManager = c.getDataManager(StochasticsDataManager.class);
					RandomGenerator randomGenerator = stochasticsDataManager.getRandomGenerator();

					RegionsDataManager regionsDataManager = c.getDataManager(RegionsDataManager.class);
					RegionId newRegionId = TestRegionId.getUnknownRegionId();
					RegionConstructionData.Builder builder = RegionConstructionData.builder().setRegionId(newRegionId);//
					for (TestRegionPropertyId testRegionPropertyId : TestRegionPropertyId.getPropertiesWithoutDefaultValues()) {
						builder.setRegionPropertyValue(testRegionPropertyId, testRegionPropertyId.getRandomPropertyValue(randomGenerator));
					}
					RegionConstructionData regionConstructionData = builder.build();
					regionsDataManager.addRegion(regionConstructionData);
					MultiKey multiKey = new MultiKey(c.getTime(), newRegionId);
					expectedObservations.add(multiKey);
				}, i);
			}

		}));

		pluginBuilder.addTestActorPlan("observer", new TestActorPlan(comparisonDay, (c) -> {
			assertEquals(expectedObservations, actualObservations);
		}));

		TestPluginData testPluginData = pluginBuilder.build();
		TestSimulation.executeSimulation(RegionsTestPluginFactory.factory(0, 6272247954838684078L, TimeTrackingPolicy.TRACK_TIME, testPluginData).getPlugins());
	}

	@Test
	@UnitTestMethod(target = RegionsDataManager.class, name = "getEventFilterForRegionPropertyDefinitionEvent", args = {})
	public void testGetEventFilterForRegionPropertyDefinitionEvent() {

		Set<MultiKey> expectedObservations = new LinkedHashSet<>();
		Set<MultiKey> actualObservations = new LinkedHashSet<>();

		TestPluginData.Builder pluginBuilder = TestPluginData.builder();

		// add an observer
		pluginBuilder.addTestActorPlan("observer", new TestActorPlan(0, (c) -> {
			RegionsDataManager regionsDataManager = c.getDataManager(RegionsDataManager.class);
			EventFilter<RegionPropertyDefinitionEvent> eventFilter = regionsDataManager.getEventFilterForRegionPropertyDefinitionEvent();
			c.subscribe(eventFilter, (c2, e) -> {
				MultiKey multiKey = new MultiKey(c2.getTime(), e.regionPropertyId());
				actualObservations.add(multiKey);
			});
		}));

		int comparisonDay = 10;

		// have an actor define property 1
		pluginBuilder.addTestActorPlan("actor", new TestActorPlan(1, (c) -> {

			for (int i = 1; i < comparisonDay; i++) {
				c.addPlan((c2) -> {
					RegionsDataManager regionsDataManager = c.getDataManager(RegionsDataManager.class);
					PropertyDefinition propertyDefinition = PropertyDefinition.builder().setType(Integer.class).setDefaultValue(55).build();
					RegionPropertyDefinitionInitialization.Builder propertyBuilder = RegionPropertyDefinitionInitialization.builder();
					RegionPropertyId regionPropertyId = TestRegionPropertyId.getUnknownRegionPropertyId();
					propertyBuilder.setRegionPropertyId(regionPropertyId).setPropertyDefinition(propertyDefinition);
					Set<RegionId> regionIds = regionsDataManager.getRegionIds();
					assertFalse(regionIds.isEmpty());
					int value = 0;
					Map<RegionId, Integer> expectedValues = new LinkedHashMap<>();
					for (RegionId regionId : regionIds) {
						propertyBuilder.addPropertyValue(regionId, value);
						expectedValues.put(regionId, value);
						value++;
					}
					RegionPropertyDefinitionInitialization regionPropertyDefinitionInitialization = propertyBuilder.build();

					regionsDataManager.defineRegionProperty(regionPropertyDefinitionInitialization);

					MultiKey multiKey = new MultiKey(c.getTime(), regionPropertyId);
					expectedObservations.add(multiKey);

				}, i);
			}

		}));

		pluginBuilder.addTestActorPlan("observer", new TestActorPlan(3, (c) -> {
			assertFalse(expectedObservations.isEmpty());
			assertEquals(expectedObservations, actualObservations);
		}));

		TestPluginData testPluginData = pluginBuilder.build();
		TestSimulation.executeSimulation(RegionsTestPluginFactory.factory(0, 1033803161227361793L, TimeTrackingPolicy.TRACK_TIME, testPluginData).getPlugins());

	}
	
	private static class PassThroughDataManager extends TestDataManager{
		private DataManagerContext dataManagerContext;
		public void init(DataManagerContext dataManagerContext) {
			super.init(dataManagerContext);
			this.dataManagerContext = dataManagerContext;
		}
		
		public void passThrough(Event event) {
			dataManagerContext.releaseMutationEvent(event);			
		}
	}

	@Test
	@UnitTestMethod(target = RegionsDataManager.class, name = "init", args = { DataManagerContext.class })
	public void testPersonRemovalEvent() {

		MutableInteger pId = new MutableInteger();

		TestPluginData.Builder pluginBuilder = TestPluginData.builder();
		/*
		 * Have the actor add a person and then remove it. There will be a delay
		 * of 0 time for the person to be removed.
		 */
		pluginBuilder.addTestActorPlan("actor", new TestActorPlan(2, (c) -> {
			PeopleDataManager peopleDataManager = c.getDataManager(PeopleDataManager.class);
			RegionsDataManager regionsDataManager = c.getDataManager(RegionsDataManager.class);

			PersonConstructionData personConstructionData = PersonConstructionData.builder().add(TestRegionId.REGION_1).build();
			PersonId personId = peopleDataManager.addPerson(personConstructionData);
			pId.setValue(personId.getValue());

			int regionPopulationCount = regionsDataManager.getRegionPopulationCount(TestRegionId.REGION_1);
			assertEquals(1, regionPopulationCount);
			assertEquals(TestRegionId.REGION_1, regionsDataManager.getPersonRegion(personId));
			peopleDataManager.removePerson(personId);

		}));

		/*
		 * Have the actor show that the person is no longer present
		 * 
		 */
		pluginBuilder.addTestActorPlan("actor", new TestActorPlan(3, (c) -> {
			RegionsDataManager regionsDataManager = c.getDataManager(RegionsDataManager.class);
			PersonId personId = new PersonId(pId.getValue());

			int regionPopulationCount = regionsDataManager.getRegionPopulationCount(TestRegionId.REGION_1);
			assertEquals(0, regionPopulationCount);
			ContractException contractException = assertThrows(ContractException.class, () -> regionsDataManager.getPersonRegion(personId));
			assertEquals(PersonError.UNKNOWN_PERSON_ID, contractException.getErrorType());

		}));

		// build action plugin
		TestPluginData testPluginData = pluginBuilder.build();
		TestSimulation.executeSimulation(RegionsTestPluginFactory.factory(0, 163202760371564041L, TimeTrackingPolicy.TRACK_TIME, testPluginData).getPlugins());

		// precondition test: if the person id is unknown
		/*
		 * Note : it is not possible to force the PersonDataManager to release
		 * such an event, so we release it from a data manager
		 */
		pluginBuilder.addTestDataManager("dm", ()->new PassThroughDataManager());
		pluginBuilder.addTestActorPlan("actor", new TestActorPlan(0, (c) -> {
			PersonRemovalEvent personRemovalEvent = new PersonRemovalEvent(new PersonId(1000));
			PassThroughDataManager passThroughDataManager = c.getDataManager(PassThroughDataManager.class);
			ContractException contractException = assertThrows(ContractException.class,()->passThroughDataManager.passThrough(personRemovalEvent));			
			assertEquals(PersonError.UNKNOWN_PERSON_ID, contractException.getErrorType());
		}));
		testPluginData = pluginBuilder.build();
		TestSimulation.executeSimulation(RegionsTestPluginFactory.factory(0, 2314376445339268382L, TimeTrackingPolicy.TRACK_TIME, testPluginData).getPlugins());

		/*
		 * Precondition test: if the person was previously removed. The
		 * exception will be thrown after the consumer fully executes and will
		 * bubble up and out of the simulation instance being executed and thus
		 * must be captured outside of the static test methods. Note : it is not
		 * possible to force the PersonDataManager to release such an event, so
		 * we release it from an actor
		 */
		pluginBuilder.addTestDataManager("dm", () -> new TestDataManager());
		pluginBuilder.addTestDataManagerPlan("dm", new TestDataManagerPlan(0, (c) -> {
			PeopleDataManager peopleDataManager = c.getDataManager(PeopleDataManager.class);
			PersonId personId = peopleDataManager.addPerson(PersonConstructionData.builder().add(TestRegionId.REGION_1).build());
			peopleDataManager.removePerson(personId);
			PersonRemovalEvent personRemovalEvent = new PersonRemovalEvent(personId);

			c.releaseObservationEvent(personRemovalEvent);

		}));
		pluginBuilder.addPluginDependency(PeoplePluginId.PLUGIN_ID);
		ContractException contractException = assertThrows(ContractException.class, () -> TestSimulation.executeSimulation(RegionsTestPluginFactory.factory(0, 3490172254703369545L, TimeTrackingPolicy.TRACK_TIME, pluginBuilder.build()).getPlugins()));
		assertEquals(PersonError.UNKNOWN_PERSON_ID, contractException.getErrorType());

	}

}
