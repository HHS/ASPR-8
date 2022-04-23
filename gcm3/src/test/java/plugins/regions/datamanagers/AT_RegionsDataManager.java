package plugins.regions.datamanagers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.math3.random.RandomGenerator;
import org.junit.jupiter.api.Test;

import nucleus.DataManagerContext;
import nucleus.EventLabel;
import nucleus.EventLabeler;
import nucleus.NucleusError;
import nucleus.Plugin;
import nucleus.Simulation;
import nucleus.Simulation.Builder;
import nucleus.testsupport.testplugin.ScenarioPlanCompletionObserver;
import nucleus.testsupport.testplugin.TestActorPlan;
import nucleus.testsupport.testplugin.TestError;
import nucleus.testsupport.testplugin.TestPlugin;
import nucleus.testsupport.testplugin.TestPluginData;
import nucleus.util.ContractException;
import plugins.partitions.PartitionsPlugin;
import plugins.people.PeoplePlugin;
import plugins.people.PeoplePluginData;
import plugins.people.datamanagers.PeopleDataManager;
import plugins.people.events.BulkPersonAdditionEvent;
import plugins.people.events.PersonAdditionEvent;
import plugins.people.events.PersonImminentRemovalEvent;
import plugins.people.support.BulkPersonConstructionData;
import plugins.people.support.PersonConstructionData;
import plugins.people.support.PersonError;
import plugins.people.support.PersonId;
import plugins.regions.RegionPlugin;
import plugins.regions.RegionPluginData;
import plugins.regions.events.PersonRegionUpdateEvent;
import plugins.regions.events.RegionPropertyUpdateEvent;
import plugins.regions.support.RegionError;
import plugins.regions.support.RegionId;
import plugins.regions.support.RegionPropertyId;
import plugins.regions.testsupport.RegionsActionSupport;
import plugins.regions.testsupport.TestRegionId;
import plugins.regions.testsupport.TestRegionPropertyId;
import plugins.reports.ReportsPlugin;
import plugins.reports.ReportsPluginData;
import plugins.stochastics.StochasticsDataManager;
import plugins.stochastics.StochasticsPlugin;
import plugins.stochastics.StochasticsPluginData;
import plugins.util.properties.PropertyDefinition;
import plugins.util.properties.PropertyError;
import plugins.util.properties.TimeTrackingPolicy;
import tools.annotations.UnitTest;
import tools.annotations.UnitTestConstructor;
import tools.annotations.UnitTestMethod;
import util.random.RandomGeneratorProvider;
import util.wrappers.MultiKey;
import util.wrappers.MutableDouble;
import util.wrappers.MutableInteger;

@UnitTest(target = RegionsDataManager.class)
public class AT_RegionsDataManager {

	@Test
	@UnitTestMethod(name = "expandCapacity", args = { int.class })
	public void testExpandCapacity() {
		RegionsActionSupport.testConsumer(20, 3161087621160007875L, TimeTrackingPolicy.TRACK_TIME, (c) -> {
			// show that a negative growth causes an exception
			RegionsDataManager regionsDataManager = c.getDataManager(RegionsDataManager.class);
			ContractException contractException = assertThrows(ContractException.class, () -> regionsDataManager.expandCapacity(-1));
			assertEquals(PersonError.NEGATIVE_GROWTH_PROJECTION, contractException.getErrorType());
		});

		// use manual tests for non-negative growth
	}

	@Test
	@UnitTestConstructor(args = { RegionPluginData.class })
	public void testConstructor() {
		// this test is covered by the remaining tests
		ContractException contractException = assertThrows(ContractException.class, () -> new RegionsDataManager(null));
		assertEquals(RegionError.NULL_REGION_PLUGIN_DATA, contractException.getErrorType());
	}

	@Test
	@UnitTestMethod(name = "getPeopleInRegion", args = { RegionId.class })
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
		Plugin testPlugin = TestPlugin.getTestPlugin(testPluginData);
		RegionsActionSupport.testConsumers(0, 3347423560010833899L, TimeTrackingPolicy.TRACK_TIME, testPlugin);

		// precondition test: if the region id is null
		RegionsActionSupport.testConsumer(0, 9052181434511982170L, TimeTrackingPolicy.TRACK_TIME, (c) -> {
			RegionsDataManager regionsDataManager = c.getDataManager(RegionsDataManager.class);

			ContractException contractException = assertThrows(ContractException.class, () -> regionsDataManager.getPeopleInRegion(null));
			assertEquals(RegionError.NULL_REGION_ID, contractException.getErrorType());
		});

		// precondition test: if the region id is unknown
		RegionsActionSupport.testConsumer(0, 1410190102298165957L, TimeTrackingPolicy.TRACK_TIME, (c) -> {
			RegionsDataManager regionsDataManager = c.getDataManager(RegionsDataManager.class);

			// if the region id is unknown
			ContractException contractException = assertThrows(ContractException.class, () -> regionsDataManager.getPeopleInRegion(TestRegionId.getUnknownRegionId()));
			assertEquals(RegionError.UNKNOWN_REGION_ID, contractException.getErrorType());
		});

	}

	@Test
	@UnitTestMethod(name = "getPersonRegion", args = { PersonId.class })
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
		Plugin testPlugin = TestPlugin.getTestPlugin(testPluginData);

		RegionsActionSupport.testConsumers(0, 5151111920517015649L, TimeTrackingPolicy.TRACK_TIME, testPlugin);

		// precondition test: if the person id is null
		RegionsActionSupport.testConsumer(0, 1490027040692903854L, TimeTrackingPolicy.TRACK_TIME, (c) -> {
			RegionsDataManager regionsDataManager = c.getDataManager(RegionsDataManager.class);
			ContractException contractException = assertThrows(ContractException.class, () -> regionsDataManager.getPersonRegion(null));
			assertEquals(PersonError.NULL_PERSON_ID, contractException.getErrorType());
		});

		// precondition test: if the person id is unknown
		RegionsActionSupport.testConsumer(0, 2144445839100475443L, TimeTrackingPolicy.TRACK_TIME, (c) -> {
			RegionsDataManager regionsDataManager = c.getDataManager(RegionsDataManager.class);
			ContractException contractException = assertThrows(ContractException.class, () -> regionsDataManager.getPersonRegion(new PersonId(-1)));
			assertEquals(PersonError.UNKNOWN_PERSON_ID, contractException.getErrorType());
		});
	}

	@Test
	@UnitTestMethod(name = "getPersonRegionArrivalTime", args = { PersonId.class })
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
		Plugin testPlugin = TestPlugin.getTestPlugin(testPluginData);
		RegionsActionSupport.testConsumers(0, 2278422620232176214L, TimeTrackingPolicy.TRACK_TIME, testPlugin);

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
		testPlugin = TestPlugin.getTestPlugin(testPluginData);
		RegionsActionSupport.testConsumers(0, 9214210856215652451L, TimeTrackingPolicy.TRACK_TIME, testPlugin);

		// precondition test: if region arrival times are not being tracked
		RegionsActionSupport.testConsumer(10, 1906010286127446114L, TimeTrackingPolicy.DO_NOT_TRACK_TIME, (c) -> {		
			RegionsDataManager regionsDataManager = c.getDataManager(RegionsDataManager.class);

			// if region arrival times are not being tracked
			ContractException contractException = assertThrows(ContractException.class, () -> regionsDataManager.getPersonRegionArrivalTime(new PersonId(0)));
			assertEquals(RegionError.REGION_ARRIVAL_TIMES_NOT_TRACKED, contractException.getErrorType());
		});

		// precondition test: if the person id is null
		RegionsActionSupport.testConsumer(0, 9214210856215652451L, TimeTrackingPolicy.TRACK_TIME, (c) -> {
			RegionsDataManager regionsDataManager = c.getDataManager(RegionsDataManager.class);
			ContractException contractException = assertThrows(ContractException.class, () -> regionsDataManager.getPersonRegionArrivalTime(null));
			assertEquals(PersonError.NULL_PERSON_ID, contractException.getErrorType());
		});

		// precondition test: if the person id is unknown
		RegionsActionSupport.testConsumer(0, 9132391945335483479L, TimeTrackingPolicy.TRACK_TIME, (c) -> {
			RegionsDataManager regionsDataManager = c.getDataManager(RegionsDataManager.class);
			ContractException contractException = assertThrows(ContractException.class, () -> regionsDataManager.getPersonRegionArrivalTime(new PersonId(-1)));
			assertEquals(PersonError.UNKNOWN_PERSON_ID, contractException.getErrorType());
		});

	}

	@Test
	@UnitTestMethod(name = "getPersonRegionArrivalTrackingPolicy", args = {})
	public void testGetPersonRegionArrivalTrackingPolicy() {
		for (TimeTrackingPolicy timeTrackingPolicy : TimeTrackingPolicy.values()) {
			RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(2934280155665825436L);
			RegionsActionSupport.testConsumer(0, randomGenerator.nextLong(), timeTrackingPolicy, (c) -> {
				RegionsDataManager regionsDataManager = c.getDataManager(RegionsDataManager.class);
				assertEquals(timeTrackingPolicy, regionsDataManager.getPersonRegionArrivalTrackingPolicy());
			});
		}
	}

	@Test
	@UnitTestMethod(name = "getRegionIds", args = {})
	public void testGetRegionIds() {
		RegionsActionSupport.testConsumer(0, 9132391945335483479L, TimeTrackingPolicy.TRACK_TIME, (c) -> {
			RegionsDataManager regionsDataManager = c.getDataManager(RegionsDataManager.class);

			Set<RegionId> expectedRegionIds = new LinkedHashSet<>();
			for (TestRegionId testRegionId : TestRegionId.values()) {
				expectedRegionIds.add(testRegionId);
			}
			assertEquals(expectedRegionIds, regionsDataManager.getRegionIds());
		});
	}

	@Test
	@UnitTestMethod(name = "getRegionPopulationCount", args = { RegionId.class })
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
		Plugin testPlugin = TestPlugin.getTestPlugin(testPluginData);
		RegionsActionSupport.testConsumers(0, 1525815460460902517L, TimeTrackingPolicy.TRACK_TIME, testPlugin);

	}

	@Test
	@UnitTestMethod(name = "getRegionPopulationTime", args = { RegionId.class })
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
		Plugin testPlugin = TestPlugin.getTestPlugin(testPluginData);
		RegionsActionSupport.testConsumers(numberOfPeople, 2430955549982485988L, TimeTrackingPolicy.TRACK_TIME, testPlugin);

		// precondition test: if the region id is null
		RegionsActionSupport.testConsumer(numberOfPeople, 3091951498637393024L, TimeTrackingPolicy.TRACK_TIME, (c) -> {
			RegionsDataManager regionsDataManager = c.getDataManager(RegionsDataManager.class);
			ContractException contractException = assertThrows(ContractException.class, () -> regionsDataManager.getRegionPopulationTime(null));
			assertEquals(RegionError.NULL_REGION_ID, contractException.getErrorType());

		});

		// precondition tests: if the region id is unknown
		RegionsActionSupport.testConsumer(numberOfPeople, 2415744693759237392L, TimeTrackingPolicy.TRACK_TIME, (c) -> {
			RegionsDataManager regionsDataManager = c.getDataManager(RegionsDataManager.class);
			ContractException contractException = assertThrows(ContractException.class, () -> regionsDataManager.getRegionPopulationTime(TestRegionId.getUnknownRegionId()));
			assertEquals(RegionError.UNKNOWN_REGION_ID, contractException.getErrorType());

		});

	}

	@Test
	@UnitTestMethod(name = "getRegionPropertyDefinition", args = { RegionPropertyId.class })
	public void testGetRegionPropertyDefinition() {
		RegionsActionSupport.testConsumer(0, 8915683065425449883L, TimeTrackingPolicy.TRACK_TIME, (c) -> {
			RegionsDataManager regionsDataManager = c.getDataManager(RegionsDataManager.class);

			Set<TestRegionPropertyId> regionPropertyIds = regionsDataManager.getRegionPropertyIds();
			assertEquals(TestRegionPropertyId.size(), regionPropertyIds.size());
			for (TestRegionPropertyId testRegionPropertyId : regionPropertyIds) {
				PropertyDefinition expectedPropertyDefinition = testRegionPropertyId.getPropertyDefinition();
				PropertyDefinition actualPropertyDefinition = regionsDataManager.getRegionPropertyDefinition(testRegionPropertyId);
				assertEquals(expectedPropertyDefinition, actualPropertyDefinition);
			}

		});

		// precondition check: if the region property id is null
		RegionsActionSupport.testConsumer(0, 4217775232224320101L, TimeTrackingPolicy.TRACK_TIME, (c) -> {
			RegionsDataManager regionsDataManager = c.getDataManager(RegionsDataManager.class);
			ContractException contractException = assertThrows(ContractException.class, () -> regionsDataManager.getRegionPropertyDefinition(null));
			assertEquals(RegionError.NULL_REGION_PROPERTY_ID, contractException.getErrorType());
		});

		// precondition check: if the region property id is unknown
		RegionsActionSupport.testConsumer(0, 1425794836864585647L, TimeTrackingPolicy.TRACK_TIME, (c) -> {
			RegionsDataManager regionsDataManager = c.getDataManager(RegionsDataManager.class);
			ContractException contractException = assertThrows(ContractException.class, () -> regionsDataManager.getRegionPropertyDefinition(TestRegionPropertyId.getUnknownRegionPropertyId()));
			assertEquals(RegionError.UNKNOWN_REGION_PROPERTY_ID, contractException.getErrorType());
		});
	}

	@Test
	@UnitTestMethod(name = "getRegionPropertyIds", args = {})
	public void testGetRegionPropertyIds() {
		RegionsActionSupport.testConsumer(0, 2658585233315606268L, TimeTrackingPolicy.TRACK_TIME, (c) -> {
			RegionsDataManager regionsDataManager = c.getDataManager(RegionsDataManager.class);
			Set<RegionPropertyId> expectedRegionPropertyIds = new LinkedHashSet<>();
			for (TestRegionPropertyId testRegionPropertyId : TestRegionPropertyId.values()) {
				expectedRegionPropertyIds.add(testRegionPropertyId);
			}
			assertEquals(expectedRegionPropertyIds, regionsDataManager.getRegionPropertyIds());
		});
		// no precondition tests

	}

	@Test
	@UnitTestMethod(name = "getRegionPropertyValue", args = { RegionId.class, RegionPropertyId.class })
	public void testGetRegionPropertyValue() {

		Map<MultiKey, Object> expectedPropertyValues = new LinkedHashMap<>();
		for (TestRegionId testRegionId : TestRegionId.values()) {
			for (TestRegionPropertyId testRegionPropertyId : TestRegionPropertyId.values()) {
				Object value = testRegionPropertyId.getPropertyDefinition().getDefaultValue().get();
				expectedPropertyValues.put(new MultiKey(testRegionId, testRegionPropertyId), value);
			}
		}
		TestPluginData.Builder pluginBuilder = TestPluginData.builder();

		// show that changes to the property values properly reflect the
		// previous values

		for (int i = 0; i < 300; i++) {
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
		Plugin testPlugin = TestPlugin.getTestPlugin(testPluginData);
		RegionsActionSupport.testConsumers(0, 8784099691519492811L, TimeTrackingPolicy.TRACK_TIME, testPlugin);

		// precondition check: if the region id is null
		RegionsActionSupport.testConsumer(0, 8784099691519492811L, TimeTrackingPolicy.TRACK_TIME, (c) -> {
			RegionsDataManager regionsDataManager = c.getDataManager(RegionsDataManager.class);
			RegionPropertyId knownRegionPropertyId = TestRegionPropertyId.REGION_PROPERTY_1_BOOLEAN_MUTABLE;
			ContractException contractException = assertThrows(ContractException.class, () -> regionsDataManager.getRegionPropertyValue(null, knownRegionPropertyId));
			assertEquals(RegionError.NULL_REGION_ID, contractException.getErrorType());
		});

		// precondition check: if the region id is not known
		RegionsActionSupport.testConsumer(0, 1546629608367614750L, TimeTrackingPolicy.TRACK_TIME, (c) -> {
			RegionsDataManager regionsDataManager = c.getDataManager(RegionsDataManager.class);
			RegionId unknownRegionId = TestRegionId.getUnknownRegionId();
			RegionPropertyId knownRegionPropertyId = TestRegionPropertyId.REGION_PROPERTY_1_BOOLEAN_MUTABLE;
			ContractException contractException = assertThrows(ContractException.class, () -> regionsDataManager.getRegionPropertyValue(unknownRegionId, knownRegionPropertyId));
			assertEquals(RegionError.UNKNOWN_REGION_ID, contractException.getErrorType());
		});

		// precondition check: if the region property id is null
		RegionsActionSupport.testConsumer(0, 2997323471294141386L, TimeTrackingPolicy.TRACK_TIME, (c) -> {
			RegionsDataManager regionsDataManager = c.getDataManager(RegionsDataManager.class);
			RegionId knownRegionId = TestRegionId.REGION_1;
			ContractException contractException = assertThrows(ContractException.class, () -> regionsDataManager.getRegionPropertyValue(knownRegionId, null));
			assertEquals(RegionError.NULL_REGION_PROPERTY_ID, contractException.getErrorType());
		});

		// precondition check: if the region property id is unknown
		RegionsActionSupport.testConsumer(0, 3797498566412748237L, TimeTrackingPolicy.TRACK_TIME, (c) -> {
			RegionsDataManager regionsDataManager = c.getDataManager(RegionsDataManager.class);
			RegionId knownRegionId = TestRegionId.REGION_1;
			RegionPropertyId unknownRegionPropertyId = TestRegionPropertyId.getUnknownRegionPropertyId();
			ContractException contractException = assertThrows(ContractException.class, () -> regionsDataManager.getRegionPropertyValue(knownRegionId, unknownRegionPropertyId));
			assertEquals(RegionError.UNKNOWN_REGION_PROPERTY_ID, contractException.getErrorType());
		});

	}

	@Test
	@UnitTestMethod(name = "getRegionPropertyTime", args = { RegionId.class, RegionPropertyId.class })
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
		Plugin testPlugin = TestPlugin.getTestPlugin(testPluginData);
		RegionsActionSupport.testConsumers(0, 1085097084913380645L, TimeTrackingPolicy.TRACK_TIME, testPlugin);

		// precondition check: if the region id is null
		RegionsActionSupport.testConsumer(0, 9165213921588406384L, TimeTrackingPolicy.TRACK_TIME, (c) -> {
			RegionsDataManager regionsDataManager = c.getDataManager(RegionsDataManager.class);
			RegionPropertyId knownRegionPropertyId = TestRegionPropertyId.REGION_PROPERTY_1_BOOLEAN_MUTABLE;
			ContractException contractException = assertThrows(ContractException.class, () -> regionsDataManager.getRegionPropertyTime(null, knownRegionPropertyId));
			assertEquals(RegionError.NULL_REGION_ID, contractException.getErrorType());
		});

		// precondition check: if the region id is not known
		RegionsActionSupport.testConsumer(0, 1546629608367614750L, TimeTrackingPolicy.TRACK_TIME, (c) -> {
			RegionsDataManager regionsDataManager = c.getDataManager(RegionsDataManager.class);
			RegionId unknownRegionId = TestRegionId.getUnknownRegionId();
			RegionPropertyId knownRegionPropertyId = TestRegionPropertyId.REGION_PROPERTY_1_BOOLEAN_MUTABLE;
			ContractException contractException = assertThrows(ContractException.class, () -> regionsDataManager.getRegionPropertyTime(unknownRegionId, knownRegionPropertyId));
			assertEquals(RegionError.UNKNOWN_REGION_ID, contractException.getErrorType());
		});

		// precondition check: if the region property id is null
		RegionsActionSupport.testConsumer(0, 7141175136643291537L, TimeTrackingPolicy.TRACK_TIME, (c) -> {
			RegionsDataManager regionsDataManager = c.getDataManager(RegionsDataManager.class);
			RegionId knownRegionId = TestRegionId.REGION_1;
			ContractException contractException = assertThrows(ContractException.class, () -> regionsDataManager.getRegionPropertyTime(knownRegionId, null));
			assertEquals(RegionError.NULL_REGION_PROPERTY_ID, contractException.getErrorType());
		});

		// precondition check: if the region property id is unknown
		RegionsActionSupport.testConsumer(0, 2200230008116664966L, TimeTrackingPolicy.TRACK_TIME, (c) -> {
			RegionsDataManager regionsDataManager = c.getDataManager(RegionsDataManager.class);
			RegionId knownRegionId = TestRegionId.REGION_1;
			RegionPropertyId unknownRegionPropertyId = TestRegionPropertyId.getUnknownRegionPropertyId();
			ContractException contractException = assertThrows(ContractException.class, () -> regionsDataManager.getRegionPropertyTime(knownRegionId, unknownRegionPropertyId));
			assertEquals(RegionError.UNKNOWN_REGION_PROPERTY_ID, contractException.getErrorType());
		});

	}

	@Test
	@UnitTestMethod(name = "regionIdExists", args = { RegionId.class })
	public void testRegionIdExists() {
		RegionsActionSupport.testConsumer(0, 3797498566412748237L, TimeTrackingPolicy.TRACK_TIME, (c) -> {
			RegionsDataManager regionsDataManager = c.getDataManager(RegionsDataManager.class);

			// show that null region ids do not exist
			assertFalse(regionsDataManager.regionIdExists(null));

			// show that the region ids added do exist
			for (TestRegionId testRegionId : TestRegionId.values()) {
				assertTrue(regionsDataManager.regionIdExists(testRegionId));
			}

			// show that an unknown region id does not exist
			assertFalse(regionsDataManager.regionIdExists(TestRegionId.getUnknownRegionId()));

		});

	}

	@Test
	@UnitTestMethod(name = "regionPropertyIdExists", args = { RegionPropertyId.class })
	public void testRegionPropertyIdExists() {
		RegionsActionSupport.testConsumer(0, 3797498566412748237L, TimeTrackingPolicy.TRACK_TIME, (c) -> {
			RegionsDataManager regionsDataManager = c.getDataManager(RegionsDataManager.class);

			// show that the property ids exist
			for (TestRegionPropertyId testRegionPropertyId : TestRegionPropertyId.values()) {
				assertTrue(regionsDataManager.regionPropertyIdExists(testRegionPropertyId));
			}

			// show that null references return false
			assertFalse(regionsDataManager.regionPropertyIdExists(null));

			// show that unknown region property ids return false
			assertFalse(regionsDataManager.regionPropertyIdExists(TestRegionPropertyId.getUnknownRegionPropertyId()));

		});

	}

	@Test
	@UnitTestMethod(name = "setPersonRegion", args = { PersonId.class, RegionId.class })
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

			for (TestRegionId testRegionId : TestRegionId.values()) {
				EventLabel<PersonRegionUpdateEvent> eventLabel = PersonRegionUpdateEvent.getEventLabelByArrivalRegion(c, testRegionId);
				c.subscribe(eventLabel, (c2, e) -> {
					recievedObservations.add(new MultiKey(e.getPreviousRegionId(), e.getCurrentRegionId(), e.getPersonId(), c2.getTime()));
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
		Plugin testPlugin = TestPlugin.getTestPlugin(testPluginData);
		RegionsActionSupport.testConsumers(numberOfPeople, 5655227215512656797L, TimeTrackingPolicy.TRACK_TIME, testPlugin);

		// show that the observations were correct
		assertEquals(expectedObservations.size(), recievedObservations.size());
		assertEquals(new LinkedHashSet<>(expectedObservations), new LinkedHashSet<>(recievedObservations));

		/*
		 * precondition test: if the person id is null
		 */
		RegionsActionSupport.testConsumer(numberOfPeople, 9048586333860290178L, TimeTrackingPolicy.TRACK_TIME, (c) -> {
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

		});

		/*
		 * precondition test: if the person id is unknown
		 */
		RegionsActionSupport.testConsumer(numberOfPeople, 6693022571477538917L, TimeTrackingPolicy.TRACK_TIME, (c) -> {
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
		});

		/*
		 * precondition test: if the region id is null
		 */
		RegionsActionSupport.testConsumer(numberOfPeople, 1385204599279421266L, TimeTrackingPolicy.TRACK_TIME, (c) -> {
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

		});

		/*
		 * precondition test: if the region id is unknown
		 */
		RegionsActionSupport.testConsumer(numberOfPeople, 6025662871362676118L, TimeTrackingPolicy.TRACK_TIME, (c) -> {
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

		});

	}

	@Test
	@UnitTestMethod(name = "setRegionPropertyValue", args = { RegionId.class, RegionPropertyId.class, Object.class })
	public void testRegionPropertyValueAssignmentEvent() {

		TestPluginData.Builder pluginBuilder = TestPluginData.builder();

		// create containers to hold actual and expected observations
		List<MultiKey> actualObservations = new ArrayList<>();
		List<MultiKey> expectedObservations = new ArrayList<>();

		// Have the observer agent start observations record them
		pluginBuilder.addTestActorPlan("observer", new TestActorPlan(0, (c) -> {

			EventLabel<RegionPropertyUpdateEvent> eventLabel = RegionPropertyUpdateEvent.getEventLabelByRegionAndProperty(c, TestRegionId.REGION_1,
					TestRegionPropertyId.REGION_PROPERTY_2_INTEGER_MUTABLE);
			c.subscribe(eventLabel, (c2, e) -> {
				actualObservations.add(new MultiKey(c2.getTime(), e.getRegionId(), e.getRegionPropertyId(), e.getCurrentPropertyValue()));
			});

			eventLabel = RegionPropertyUpdateEvent.getEventLabelByRegionAndProperty(c, TestRegionId.REGION_2, TestRegionPropertyId.REGION_PROPERTY_3_DOUBLE_MUTABLE);
			c.subscribe(eventLabel, (c2, e) -> {
				actualObservations.add(new MultiKey(c2.getTime(), e.getRegionId(), e.getRegionPropertyId(), e.getCurrentPropertyValue()));
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
		Plugin testPlugin = TestPlugin.getTestPlugin(testPluginData);
		RegionsActionSupport.testConsumers(0, 4630021532130673951L, TimeTrackingPolicy.TRACK_TIME, testPlugin);

		// show that the observed changes match expectations
		assertEquals(expectedObservations.size(), actualObservations.size());

		assertEquals(new LinkedHashSet<>(expectedObservations), new LinkedHashSet<>(actualObservations));

		// precondition check: if the region id is null
		RegionsActionSupport.testConsumer(0, 7347707922069273812L, TimeTrackingPolicy.TRACK_TIME, (c) -> {
			RegionPropertyId regionPropertyId = TestRegionPropertyId.REGION_PROPERTY_2_INTEGER_MUTABLE;
			Object propertyValue = 67;
			RegionsDataManager regionsDataManager = c.getDataManager(RegionsDataManager.class);

			ContractException contractException = assertThrows(ContractException.class, () -> regionsDataManager.setRegionPropertyValue(null, regionPropertyId, propertyValue));
			assertEquals(RegionError.NULL_REGION_ID, contractException.getErrorType());

		});

		// precondition check: if the region id is unknown
		RegionsActionSupport.testConsumer(0, 3075330757105736185L, TimeTrackingPolicy.TRACK_TIME, (c) -> {

			RegionId unknownRegionId = TestRegionId.getUnknownRegionId();
			RegionPropertyId regionPropertyId = TestRegionPropertyId.REGION_PROPERTY_2_INTEGER_MUTABLE;
			Object propertyValue = 67;
			RegionsDataManager regionsDataManager = c.getDataManager(RegionsDataManager.class);

			ContractException contractException = assertThrows(ContractException.class, () -> regionsDataManager.setRegionPropertyValue(unknownRegionId, regionPropertyId, propertyValue));
			assertEquals(RegionError.UNKNOWN_REGION_ID, contractException.getErrorType());

		});

		// precondition check: if the region property id is null
		RegionsActionSupport.testConsumer(0, 4169934733913962790L, TimeTrackingPolicy.TRACK_TIME, (c) -> {
			RegionId regionId = TestRegionId.REGION_1;
			Object propertyValue = 67;
			RegionsDataManager regionsDataManager = c.getDataManager(RegionsDataManager.class);

			ContractException contractException = assertThrows(ContractException.class, () -> regionsDataManager.setRegionPropertyValue(regionId, null, propertyValue));
			assertEquals(RegionError.NULL_REGION_PROPERTY_ID, contractException.getErrorType());

		});

		// precondition check: if the region property id is unknown
		RegionsActionSupport.testConsumer(0, 5578070775436119166L, TimeTrackingPolicy.TRACK_TIME, (c) -> {
			RegionId regionId = TestRegionId.REGION_1;
			RegionPropertyId unknownRegionPropertyId = TestRegionPropertyId.getUnknownRegionPropertyId();
			Object propertyValue = 67;
			RegionsDataManager regionsDataManager = c.getDataManager(RegionsDataManager.class);

			ContractException contractException = assertThrows(ContractException.class, () -> regionsDataManager.setRegionPropertyValue(regionId, unknownRegionPropertyId, propertyValue));
			assertEquals(RegionError.UNKNOWN_REGION_PROPERTY_ID, contractException.getErrorType());

		});

		// precondition check: if the region property value is null
		RegionsActionSupport.testConsumer(0, 217279748753596418L, TimeTrackingPolicy.TRACK_TIME, (c) -> {
			RegionId regionId = TestRegionId.REGION_1;
			RegionPropertyId regionPropertyId = TestRegionPropertyId.REGION_PROPERTY_2_INTEGER_MUTABLE;
			RegionsDataManager regionsDataManager = c.getDataManager(RegionsDataManager.class);

			ContractException contractException = assertThrows(ContractException.class, () -> regionsDataManager.setRegionPropertyValue(regionId, regionPropertyId, null));
			assertEquals(RegionError.NULL_REGION_PROPERTY_VALUE, contractException.getErrorType());

		});

		// precondition check: if the region property value is incompatible with
		// the defined type for the property
		RegionsActionSupport.testConsumer(0, 7043526072670323223L, TimeTrackingPolicy.TRACK_TIME, (c) -> {
			RegionId regionId = TestRegionId.REGION_1;
			RegionPropertyId regionPropertyId = TestRegionPropertyId.REGION_PROPERTY_2_INTEGER_MUTABLE;
			Object incompatiblePropertyValue = "incompatible value";
			RegionsDataManager regionsDataManager = c.getDataManager(RegionsDataManager.class);

			ContractException contractException = assertThrows(ContractException.class, () -> regionsDataManager.setRegionPropertyValue(regionId, regionPropertyId, incompatiblePropertyValue));
			assertEquals(PropertyError.INCOMPATIBLE_VALUE, contractException.getErrorType());

		});

		// precondition check: if the region id is unknown
		RegionsActionSupport.testConsumer(0, 8501593854721316109L, TimeTrackingPolicy.TRACK_TIME, (c) -> {
			RegionId unknownRegionId = TestRegionId.getUnknownRegionId();
			RegionPropertyId regionPropertyId = TestRegionPropertyId.REGION_PROPERTY_2_INTEGER_MUTABLE;
			Object propertyValue = 67;
			RegionsDataManager regionsDataManager = c.getDataManager(RegionsDataManager.class);

			ContractException contractException = assertThrows(ContractException.class, () -> regionsDataManager.setRegionPropertyValue(unknownRegionId, regionPropertyId, propertyValue));
			assertEquals(RegionError.UNKNOWN_REGION_ID, contractException.getErrorType());

		});

		// precondition check: if the region property value is null
		RegionsActionSupport.testConsumer(0, 8501593854721316109L, TimeTrackingPolicy.TRACK_TIME, (c) -> {
			RegionId regionId = TestRegionId.REGION_1;
			RegionPropertyId immutableRegionPropertyId = TestRegionPropertyId.REGION_PROPERTY_5_INTEGER_IMMUTABLE;			
			RegionsDataManager regionsDataManager = c.getDataManager(RegionsDataManager.class);

			ContractException contractException = assertThrows(ContractException.class, () -> regionsDataManager.setRegionPropertyValue(regionId, immutableRegionPropertyId, null));
			assertEquals(RegionError.NULL_REGION_PROPERTY_VALUE, contractException.getErrorType());

		});
	}

	
	
	@Test
	@UnitTestMethod(name = "init", args = {DataManagerContext.class})
	public void testRegionPluginData() {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(4454658950052475227L);
		int initialPopulation = 30;
		List<PersonId> people = new ArrayList<>();
		for(int i = 0;i<initialPopulation;i++) {
			people.add(new PersonId(i));
		}

		Builder builder = Simulation.builder();

		// add the region plugin
		RegionPluginData.Builder regionPluginBuilder = RegionPluginData.builder();
		for (TestRegionId regionId : TestRegionId.values()) {
			regionPluginBuilder.addRegion(regionId);
		}

		for (TestRegionPropertyId testRegionPropertyId : TestRegionPropertyId.values()) {
			regionPluginBuilder.defineRegionProperty(testRegionPropertyId, testRegionPropertyId.getPropertyDefinition());
		}

		for (TestRegionId regionId : TestRegionId.values()) {
			for (TestRegionPropertyId testRegionPropertyId : TestRegionPropertyId.values()) {
				if (randomGenerator.nextBoolean()) {
					regionPluginBuilder.setRegionPropertyValue(regionId, testRegionPropertyId, testRegionPropertyId.getRandomPropertyValue(randomGenerator));
				}
			}
		}
		TestRegionId testRegionId = TestRegionId.REGION_1;
		for(PersonId personId : people) {
			regionPluginBuilder.setPersonRegion(personId, testRegionId);
			testRegionId = testRegionId.next();
		}

		regionPluginBuilder.setPersonRegionArrivalTracking(TimeTrackingPolicy.TRACK_TIME);
		RegionPluginData regionPluginData = regionPluginBuilder.build();
		builder.addPlugin(RegionPlugin.getRegionPlugin(regionPluginData));

		// add the people plugin
		PeoplePluginData.Builder peopleBuilder = PeoplePluginData.builder();		
		for(PersonId personId : people) {
			peopleBuilder.addPersonId(personId);			
		}
		PeoplePluginData peoplePluginData = peopleBuilder.build();
		builder.addPlugin(PeoplePlugin.getPeoplePlugin(peoplePluginData));

		// add the report plugin
		builder.addPlugin(ReportsPlugin.getReportPlugin(ReportsPluginData.builder().build()));

		// add the stochastics plugin
		builder.addPlugin(StochasticsPlugin.getStochasticsPlugin(StochasticsPluginData.builder().setSeed(randomGenerator.nextLong()).build()));

		// add the partitions plugin
		builder.addPlugin(PartitionsPlugin.getPartitionsPlugin());

		// add the test plugin
		TestPluginData.Builder pluginBuilder = TestPluginData.builder();

		pluginBuilder.addTestActorPlan("actor", new TestActorPlan(0, (c) -> {
			RegionsDataManager regionsDataManager = c.getDataManager(RegionsDataManager.class);
			// show that the initial state of the region data manager matches
			// the state of the region plugin data

			assertEquals(regionPluginData.getPersonRegionArrivalTrackingPolicy(), regionsDataManager.getPersonRegionArrivalTrackingPolicy());
			assertEquals(regionPluginData.getRegionIds(), regionsDataManager.getRegionIds());
			assertEquals(regionPluginData.getRegionPropertyIds(), regionsDataManager.getRegionPropertyIds());
			for (RegionPropertyId regionPropertyId : regionPluginData.getRegionPropertyIds()) {
				PropertyDefinition expectedPropertyDefinition = regionPluginData.getRegionPropertyDefinition(regionPropertyId);
				PropertyDefinition actualPropertyDefinition = regionsDataManager.getRegionPropertyDefinition(regionPropertyId);
				assertEquals(expectedPropertyDefinition, actualPropertyDefinition);
			}
			for (RegionId regionId : regionPluginData.getRegionIds()) {
				for (RegionPropertyId regionPropertyId : regionPluginData.getRegionPropertyIds()) {
					Object expectedValue = regionPluginData.getRegionPropertyValue(regionId, regionPropertyId);
					Object actualValue = regionsDataManager.getRegionPropertyValue(regionId, regionPropertyId);
					assertEquals(expectedValue, actualValue);
				}
			}

		}));
		TestPluginData testPluginData = pluginBuilder.build();
		Plugin testPlugin = TestPlugin.getTestPlugin(testPluginData);

		builder.addPlugin(testPlugin);

		ScenarioPlanCompletionObserver scenarioPlanCompletionObserver = new ScenarioPlanCompletionObserver();

		// build and execute the engine
		builder.setOutputConsumer(scenarioPlanCompletionObserver::handleOutput);
		builder.build().execute();

		// show that all actions were executed
		if (!scenarioPlanCompletionObserver.allPlansExecuted()) {
			throw new ContractException(TestError.TEST_EXECUTION_FAILURE);
		}

	}

	/**
	 * Shows that all event {@linkplain PersonRegionUpdateEvent}
	 * labelers are created
	 */
	@Test
	@UnitTestMethod(name = "init", args = {DataManagerContext.class})
	public void testPersonRegionUpdateEventLabelers() {
		RegionsActionSupport.testConsumer(0, 2734071676096451334L, TimeTrackingPolicy.DO_NOT_TRACK_TIME, (c) -> {
			EventLabeler<PersonRegionUpdateEvent> eventLabelerForArrivalRegion = PersonRegionUpdateEvent.getEventLabelerForArrivalRegion();
			assertNotNull(eventLabelerForArrivalRegion);
			ContractException contractException = assertThrows(ContractException.class, () -> c.addEventLabeler(eventLabelerForArrivalRegion));
			assertEquals(NucleusError.DUPLICATE_LABELER_ID_IN_EVENT_LABELER, contractException.getErrorType());

			EventLabeler<PersonRegionUpdateEvent> eventLabelerForDepartureRegion = PersonRegionUpdateEvent.getEventLabelerForDepartureRegion();
			assertNotNull(eventLabelerForDepartureRegion);
			contractException = assertThrows(ContractException.class, () -> c.addEventLabeler(eventLabelerForDepartureRegion));
			assertEquals(NucleusError.DUPLICATE_LABELER_ID_IN_EVENT_LABELER, contractException.getErrorType());

			EventLabeler<PersonRegionUpdateEvent> eventLabelerForPerson = PersonRegionUpdateEvent.getEventLabelerForPerson();
			assertNotNull(eventLabelerForPerson);
			contractException = assertThrows(ContractException.class, () -> c.addEventLabeler(eventLabelerForPerson));
			assertEquals(NucleusError.DUPLICATE_LABELER_ID_IN_EVENT_LABELER, contractException.getErrorType());

		});
	}

	/**
	 * Shows that all event {@linkplain RegionPropertyUpdateEvent}
	 * labelers are created
	 */
	@Test
	@UnitTestMethod(name = "init", args = {DataManagerContext.class})	
	public void testRegionPropertyUpdateEventLabelers() {

		RegionsActionSupport.testConsumer(0, 4228466028646070532L, TimeTrackingPolicy.DO_NOT_TRACK_TIME, (c) -> {
			EventLabeler<RegionPropertyUpdateEvent> eventLabeler1 = RegionPropertyUpdateEvent.getEventLabelerForProperty();
			assertNotNull(eventLabeler1);
			ContractException contractException = assertThrows(ContractException.class, () -> c.addEventLabeler(eventLabeler1));
			assertEquals(NucleusError.DUPLICATE_LABELER_ID_IN_EVENT_LABELER, contractException.getErrorType());

			EventLabeler<RegionPropertyUpdateEvent> eventLabeler2 = RegionPropertyUpdateEvent.getEventLabelerForRegionAndProperty();
			assertNotNull(eventLabeler2);
			contractException = assertThrows(ContractException.class, () -> c.addEventLabeler(eventLabeler2));
			assertEquals(NucleusError.DUPLICATE_LABELER_ID_IN_EVENT_LABELER, contractException.getErrorType());

		});

	}
	
	@Test
	@UnitTestMethod(name = "init", args = {DataManagerContext.class})
	public void testPluginDataLoaded() {

		long seed = 4228466028646070532L;
		
		int initialPopulation = 100;
		
		List<PersonId> people = new ArrayList<>();
		for (int i = 0; i < initialPopulation; i++) {
			people.add(new PersonId(i));
		}
		Builder builder = Simulation.builder();

		// add the region plugin
		RegionPluginData.Builder regionPluginBuilder = RegionPluginData.builder();
		for (TestRegionId regionId : TestRegionId.values()) {
			regionPluginBuilder.addRegion(regionId);
		}
		
		for(TestRegionPropertyId testRegionPropertyId : TestRegionPropertyId.values()) {
			regionPluginBuilder.defineRegionProperty(testRegionPropertyId, testRegionPropertyId.getPropertyDefinition());
		}
		TestRegionId testRegionId = TestRegionId.REGION_1;
		regionPluginBuilder.setPersonRegionArrivalTracking(TimeTrackingPolicy.TRACK_TIME);
		for(PersonId personId : people) {
			regionPluginBuilder.setPersonRegion(personId, testRegionId);
			testRegionId = testRegionId.next();
		}
		RegionPluginData regionPluginData = regionPluginBuilder.build();
		builder.addPlugin(RegionPlugin.getRegionPlugin(regionPluginData));

		// add the people plugin
		PeoplePluginData.Builder peopleBuilder = PeoplePluginData.builder();
		for(PersonId personId : people) {
			peopleBuilder.addPersonId(personId);
		}		
		PeoplePluginData peoplePluginData = peopleBuilder.build();		
		builder.addPlugin(PeoplePlugin.getPeoplePlugin(peoplePluginData));

		// add the report plugin
		builder.addPlugin(ReportsPlugin.getReportPlugin(ReportsPluginData.builder().build()));

		// add the stochastics plugin
		builder.addPlugin(StochasticsPlugin.getStochasticsPlugin(StochasticsPluginData.builder().setSeed(seed).build()));

		// add the partitions plugin
		builder.addPlugin(PartitionsPlugin.getPartitionsPlugin());

		// add the test plugin
		TestPluginData.Builder pluginBuilder = TestPluginData.builder();
		pluginBuilder.addTestActorPlan("actor", new TestActorPlan(0,(c)->{
			RegionsDataManager regionsDataManager = c.getDataManager(RegionsDataManager.class);
			for(PersonId personId : people) {
				RegionId actualRegionId = regionsDataManager.getPersonRegion(personId);
				RegionId expectedRegionId = regionPluginData.getPersonRegion(personId);
				assertEquals(actualRegionId, expectedRegionId);
			}
			
		}));
		TestPluginData testPluginData = pluginBuilder.build();						
		Plugin testPlugin = TestPlugin.getTestPlugin(testPluginData);
		builder.addPlugin(testPlugin);

		ScenarioPlanCompletionObserver scenarioPlanCompletionObserver = new ScenarioPlanCompletionObserver();

		// build and execute the engine
		builder.setOutputConsumer(scenarioPlanCompletionObserver::handleOutput);
		builder.build().execute();

		// show that all actions were executed
		if (!scenarioPlanCompletionObserver.allPlansExecuted()) {
			throw new ContractException(TestError.TEST_EXECUTION_FAILURE);
		}


	}

	@Test
	@UnitTestMethod(name = "init", args = {DataManagerContext.class})
	public void testPersonAdditionEvent() {
		/*
		 * Have the agent create some people over time and show that each person
		 * is in the correct region at the correct time
		 */
		RegionsActionSupport.testConsumer(0, 8294774271110836859L, TimeTrackingPolicy.TRACK_TIME, (c) -> {
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

		});

		// precondition check: if no region data was included in the event
		RegionsActionSupport.testConsumer(0, 8294774271110836859L, TimeTrackingPolicy.TRACK_TIME, (c) -> {
			PeopleDataManager peopleDataManager = c.getDataManager(PeopleDataManager.class);
			PersonConstructionData personConstructionData = PersonConstructionData.builder().build();
			ContractException contractException = assertThrows(ContractException.class, () -> peopleDataManager.addPerson(personConstructionData));
			assertEquals(RegionError.NULL_REGION_ID, contractException.getErrorType());
		});

		// precondition check: if the region in the event is unknown
		RegionsActionSupport.testConsumer(0, 2879410509293373914L, TimeTrackingPolicy.TRACK_TIME, (c) -> {
			PeopleDataManager peopleDataManager = c.getDataManager(PeopleDataManager.class);
			PersonConstructionData personConstructionData = PersonConstructionData.builder().add(TestRegionId.getUnknownRegionId()).build();
			ContractException contractException = assertThrows(ContractException.class, () -> peopleDataManager.addPerson(personConstructionData));
			assertEquals(RegionError.UNKNOWN_REGION_ID, contractException.getErrorType());
		});

		// precondition check: if the person id does not exist
		RegionsActionSupport.testConsumer(0, 5311711819224332248L, TimeTrackingPolicy.TRACK_TIME, (c) -> {
			/*
			 * Note : it is not possible to force the PersonDataManager to
			 * release such an event, so we release it from an actor
			 */
			PersonConstructionData personConstructionData = PersonConstructionData.builder().add(TestRegionId.REGION_1).build();
			PersonAdditionEvent personAdditionEvent = new PersonAdditionEvent(new PersonId(10000), personConstructionData);
			ContractException contractException = assertThrows(ContractException.class, () -> c.releaseEvent(personAdditionEvent));
			assertEquals(PersonError.UNKNOWN_PERSON_ID, contractException.getErrorType());
		});

		// precondition check: if the person was previously added
		RegionsActionSupport.testConsumer(0, 5824136557013438265L, TimeTrackingPolicy.TRACK_TIME, (c) -> {
			/*
			 * Note : it is not possible to force the PersonDataManager to
			 * release such an event, so we release it from an actor
			 */
			PeopleDataManager peopleDataManager = c.getDataManager(PeopleDataManager.class);
			PersonConstructionData personConstructionData = PersonConstructionData.builder().add(TestRegionId.REGION_1).build();
			PersonId personId = peopleDataManager.addPerson(personConstructionData);

			PersonAdditionEvent personAdditionEvent = new PersonAdditionEvent(personId, personConstructionData);

			ContractException contractException = assertThrows(ContractException.class, () -> c.releaseEvent(personAdditionEvent));
			assertEquals(RegionError.DUPLICATE_PERSON_ADDITION, contractException.getErrorType());
		});

	}

	@Test
	@UnitTestMethod(name = "init", args = {DataManagerContext.class})
	public void testBulkPersonAdditionEvent() {

		/*
		 * Have the agent create some people over time and show that each person
		 * is in the correct region at the correct time
		 */
		RegionsActionSupport.testConsumer(0, 2654453328570666100L, TimeTrackingPolicy.TRACK_TIME, (c) -> {
			StochasticsDataManager stochasticsDataManager = c.getDataManager(StochasticsDataManager.class);
			RandomGenerator randomGenerator = stochasticsDataManager.getRandomGenerator();

			for (int i = 0; i < 100; i++) {
				c.addPlan((c2) -> {
					StochasticsDataManager stochasticsDataManager2 = c2.getDataManager(StochasticsDataManager.class);
					RegionsDataManager regionsDataManager = c2.getDataManager(RegionsDataManager.class);
					PeopleDataManager peopleDataManager = c2.getDataManager(PeopleDataManager.class);

					RandomGenerator rng = stochasticsDataManager2.getRandomGenerator();
					/*
					 * Generate a random region to for each new person and add
					 * the person
					 */
					Map<Integer, RegionId> expectedRegions = new LinkedHashMap<>();
					BulkPersonConstructionData.Builder bulkBuilder = BulkPersonConstructionData.builder();
					PersonConstructionData.Builder personBuilder = PersonConstructionData.builder();
					int personCount = rng.nextInt(5) + 1;

					for (int j = 0; j < personCount; j++) {
						RegionId randomRegionId = TestRegionId.getRandomRegionId(rng);
						personBuilder.add(randomRegionId);
						expectedRegions.put(j, randomRegionId);
						bulkBuilder.add(personBuilder.build());
					}
					BulkPersonConstructionData bulkPersonConstructionData = bulkBuilder.build();
					PersonId personId = peopleDataManager.addBulkPeople(bulkPersonConstructionData).get();

					int basePersonIndex = personId.getValue();

					/*
					 * Show that each person is in the correct region with the
					 * correct region arrival time
					 */
					for (int j = 0; j < personCount; j++) {
						personId = new PersonId(j + basePersonIndex);
						RegionId personRegionId = regionsDataManager.getPersonRegion(personId);
						RegionId randomRegionId = expectedRegions.get(j);
						assertEquals(randomRegionId, personRegionId);
						assertEquals(c2.getTime(), regionsDataManager.getPersonRegionArrivalTime(personId));
					}

				}, randomGenerator.nextDouble() * 1000);
			}

		});

		// precondition check:
		// precondition check: if no region data was included in the event
		RegionsActionSupport.testConsumer(0, 7059505726403414171L, TimeTrackingPolicy.TRACK_TIME, (c) -> {
			PeopleDataManager peopleDataManager = c.getDataManager(PeopleDataManager.class);
			PersonConstructionData personConstructionData = PersonConstructionData.builder().build();
			BulkPersonConstructionData bulkPersonConstructionData = BulkPersonConstructionData.builder().add(personConstructionData).build();
			ContractException contractException = assertThrows(ContractException.class, () -> peopleDataManager.addBulkPeople(bulkPersonConstructionData));
			assertEquals(RegionError.NULL_REGION_ID, contractException.getErrorType());
		});

		// precondition check: if the region in the event is unknown
		RegionsActionSupport.testConsumer(0, 5120242925932651968L, TimeTrackingPolicy.TRACK_TIME, (c) -> {
			PeopleDataManager peopleDataManager = c.getDataManager(PeopleDataManager.class);
			PersonConstructionData personConstructionData = PersonConstructionData.builder().add(TestRegionId.getUnknownRegionId()).build();
			BulkPersonConstructionData bulkPersonConstructionData = BulkPersonConstructionData.builder().add(personConstructionData).build();
			ContractException contractException = assertThrows(ContractException.class, () -> peopleDataManager.addBulkPeople(bulkPersonConstructionData));
			assertEquals(RegionError.UNKNOWN_REGION_ID, contractException.getErrorType());
		});

		// precondition check: if the person id does not exist
		RegionsActionSupport.testConsumer(0, 5968783102821781999L, TimeTrackingPolicy.TRACK_TIME, (c) -> {
			/*
			 * Note : it is not possible to force the PersonDataManager to
			 * release such an event, so we release it from an actor
			 */
			PersonConstructionData personConstructionData = PersonConstructionData.builder().add(TestRegionId.REGION_1).build();
			BulkPersonConstructionData bulkPersonConstructionData = BulkPersonConstructionData.builder().add(personConstructionData).build();
			BulkPersonAdditionEvent bulkPersonAdditionEvent = new BulkPersonAdditionEvent(new PersonId(45), bulkPersonConstructionData);
			ContractException contractException = assertThrows(ContractException.class, () -> c.releaseEvent(bulkPersonAdditionEvent));
			assertEquals(PersonError.UNKNOWN_PERSON_ID, contractException.getErrorType());
		});

		// precondition check: if the person was previously added
		RegionsActionSupport.testConsumer(0, 8092390328840929050L, TimeTrackingPolicy.TRACK_TIME, (c) -> {
			/*
			 * Note : it is not possible to force the PersonDataManager to
			 * release such an event, so we release it from an actor
			 */
			PeopleDataManager peopleDataManager = c.getDataManager(PeopleDataManager.class);
			PersonConstructionData personConstructionData = PersonConstructionData.builder().add(TestRegionId.REGION_1).build();
			PersonId personId = peopleDataManager.addPerson(personConstructionData);

			BulkPersonConstructionData bulkPersonConstructionData = BulkPersonConstructionData.builder().add(personConstructionData).build();
			BulkPersonAdditionEvent bulkPersonAdditionEvent = new BulkPersonAdditionEvent(personId, bulkPersonConstructionData);

			ContractException contractException = assertThrows(ContractException.class, () -> c.releaseEvent(bulkPersonAdditionEvent));
			assertEquals(RegionError.DUPLICATE_PERSON_ADDITION, contractException.getErrorType());
		});
	}

	@Test
	@UnitTestMethod(name = "init", args = {DataManagerContext.class})
	public void testPersonImminentRemovalEvent() {

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
		 * Have the actor show that the person is no longer in the location data
		 * view
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
		Plugin testPlugin = TestPlugin.getTestPlugin(testPluginData);
		RegionsActionSupport.testConsumers(0, 163202760371564041L, TimeTrackingPolicy.TRACK_TIME, testPlugin);

		// precondition test: if the person id is unknown
		RegionsActionSupport.testConsumer(0, 2314376445339268382L, TimeTrackingPolicy.TRACK_TIME, (c) -> {
			PersonImminentRemovalEvent personImminentRemovalEvent = new PersonImminentRemovalEvent(new PersonId(1000));
			ContractException contractException = assertThrows(ContractException.class, () -> c.releaseEvent(personImminentRemovalEvent));
			assertEquals(PersonError.UNKNOWN_PERSON_ID, contractException.getErrorType());
		});

		/*
		 * Precondition test: if the person was previously removed. The
		 * exception will be thrown after the consumer fully executes and will
		 * bubble up and out of the simulation instance being executed and thus
		 * must be captured outside of the static test methods.
		 */
		ContractException contractException = assertThrows(ContractException.class, () -> {
			RegionsActionSupport.testConsumer(0, 2314376445339268382L, TimeTrackingPolicy.TRACK_TIME, (c) -> {
				PeopleDataManager peopleDataManager = c.getDataManager(PeopleDataManager.class);
				PersonId personId = peopleDataManager.addPerson(PersonConstructionData.builder().add(TestRegionId.REGION_1).build());
				peopleDataManager.removePerson(personId);
				PersonImminentRemovalEvent personImminentRemovalEvent = new PersonImminentRemovalEvent(personId);
				c.releaseEvent(personImminentRemovalEvent);
			});
		});
		assertEquals(RegionError.DUPLICATE_PERSON_REMOVAL, contractException.getErrorType());

	}

}
