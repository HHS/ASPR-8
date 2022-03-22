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

import annotations.UnitTest;
import annotations.UnitTestConstructor;
import annotations.UnitTestMethod;
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
import plugins.people.PersonDataManager;
import plugins.people.events.BulkPersonCreationObservationEvent;
import plugins.people.events.PersonCreationObservationEvent;
import plugins.people.events.PersonImminentRemovalObservationEvent;
import plugins.people.support.BulkPersonConstructionData;
import plugins.people.support.PersonConstructionData;
import plugins.people.support.PersonError;
import plugins.people.support.PersonId;
import plugins.regions.RegionPlugin;
import plugins.regions.RegionPluginData;
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
import util.MultiKey;
import util.MutableDouble;
import util.MutableInteger;
import util.RandomGeneratorProvider;

@UnitTest(target = RegionDataManager.class)
public class AT_RegionDataManager {

	@Test
	@UnitTestMethod(name = "expandCapacity", args = { int.class })
	public void testExpandCapacity() {
		RegionsActionSupport.testConsumer(20, 3161087621160007875L, TimeTrackingPolicy.TRACK_TIME, (c) -> {
			// show that a negative growth causes an exception
			RegionDataManager regionDataManager = c.getDataManager(RegionDataManager.class).get();
			ContractException contractException = assertThrows(ContractException.class, () -> regionDataManager.expandCapacity(-1));
			assertEquals(PersonError.NEGATIVE_GROWTH_PROJECTION, contractException.getErrorType());
		});

		// use manual tests for non-negative growth
	}

	@Test
	@UnitTestConstructor(args = { RegionPluginData.class })
	public void testConstructor() {
		// this test is covered by the remaining tests
		ContractException contractException = assertThrows(ContractException.class, () -> new RegionDataManager(null));
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
		pluginBuilder.addTestActorPlan("agent", new TestActorPlan(0, (c) -> {
			RegionDataManager regionDataManager = c.getDataManager(RegionDataManager.class).get();
			for (TestRegionId testRegionId : TestRegionId.values()) {
				assertEquals(0, regionDataManager.getPeopleInRegion(testRegionId).size());
			}
		}));

		// add some people
		pluginBuilder.addTestActorPlan("agent", new TestActorPlan(1, (c) -> {
			StochasticsDataManager stochasticsDataManager = c.getDataManager(StochasticsDataManager.class).get();
			PersonDataManager personDataManager = c.getDataManager(PersonDataManager.class).get();
			for (int i = 0; i < 100; i++) {
				TestRegionId regionId = TestRegionId.getRandomRegionId(stochasticsDataManager.getRandomGenerator());
				PersonConstructionData personConstructionData = PersonConstructionData.builder().add(regionId).build();
				PersonId personId = personDataManager.addPerson(personConstructionData);
				expectedPeopelInRegions.get(regionId).add(personId);
			}
		}));

		// show that the people in the regions match expectations
		pluginBuilder.addTestActorPlan("agent", new TestActorPlan(2, (c) -> {
			RegionDataManager regionDataManager = c.getDataManager(RegionDataManager.class).get();
			for (TestRegionId testRegionId : TestRegionId.values()) {
				Set<PersonId> expectedPeople = expectedPeopelInRegions.get(testRegionId);
				LinkedHashSet<PersonId> actualPeople = new LinkedHashSet<>(regionDataManager.getPeopleInRegion(testRegionId));
				assertEquals(expectedPeople, actualPeople);
			}
		}));

		// build and add the action plugin
		TestPluginData testPluginData = pluginBuilder.build();
		Plugin testPlugin = TestPlugin.getTestPlugin(testPluginData);
		RegionsActionSupport.testConsumers(0, 3347423560010833899L, TimeTrackingPolicy.TRACK_TIME, testPlugin);

		// precondition test: if the region id is null
		RegionsActionSupport.testConsumer(0, 9052181434511982170L, TimeTrackingPolicy.TRACK_TIME, (c) -> {
			RegionDataManager regionDataManager = c.getDataManager(RegionDataManager.class).get();

			ContractException contractException = assertThrows(ContractException.class, () -> regionDataManager.getPeopleInRegion(null));
			assertEquals(RegionError.NULL_REGION_ID, contractException.getErrorType());
		});

		// precondition test: if the region id is unknown
		RegionsActionSupport.testConsumer(0, 1410190102298165957L, TimeTrackingPolicy.TRACK_TIME, (c) -> {
			RegionDataManager regionDataManager = c.getDataManager(RegionDataManager.class).get();

			// if the region id is unknown
			ContractException contractException = assertThrows(ContractException.class, () -> regionDataManager.getPeopleInRegion(TestRegionId.getUnknownRegionId()));
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
		pluginBuilder.addTestActorPlan("agent", new TestActorPlan(0, (c) -> {
			StochasticsDataManager stochasticsDataManager = c.getDataManager(StochasticsDataManager.class).get();
			PersonDataManager personDataManager = c.getDataManager(PersonDataManager.class).get();
			RegionDataManager regionDataManager = c.getDataManager(RegionDataManager.class).get();
			for (int i = 0; i < numberOfPeople; i++) {
				// select a region at random
				TestRegionId regionId = TestRegionId.getRandomRegionId(stochasticsDataManager.getRandomGenerator());
				// create the person
				PersonConstructionData personConstructionData = PersonConstructionData.builder().add(regionId).build();
				PersonId personId = personDataManager.addPerson(personConstructionData);
				// show that the person has the correct region
				assertEquals(regionId, regionDataManager.getPersonRegion(personId));
				// add the person to the expectations
				expectedPersonRegions.put(personId, regionId);
			}
		}));

		// move people over time and show that each time they are moved the
		// correct region is reported
		pluginBuilder.addTestActorPlan("agent", new TestActorPlan(0, (c) -> {
			PersonDataManager personDataManager = c.getDataManager(PersonDataManager.class).get();
			double planTime = 0;
			for (PersonId personId : personDataManager.getPeople()) {
				c.addPlan((c2) -> {
					// determine the person's current region
					RegionDataManager regionDataManager = c2.getDataManager(RegionDataManager.class).get();
					TestRegionId regionId = regionDataManager.getPersonRegion(personId);
					// select the next region for the person
					regionId = regionId.next();
					// move the person
					regionDataManager.setPersonRegion(personId, regionId);
					/*
					 * show that the region arrival time for the person is the
					 * current time in the simulation
					 */
					assertEquals(regionId, regionDataManager.getPersonRegion(personId));
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
		pluginBuilder.addTestActorPlan("agent", new TestActorPlan(postPersonMovementTime, (c) -> {
			PersonDataManager personDataManager = c.getDataManager(PersonDataManager.class).get();
			RegionDataManager regionDataManager = c.getDataManager(RegionDataManager.class).get();
			for (PersonId personId : personDataManager.getPeople()) {
				RegionId expectedRegionId = expectedPersonRegions.get(personId);
				RegionId actualRegionId = regionDataManager.getPersonRegion(personId);
				assertEquals(expectedRegionId, actualRegionId);
			}
		}));
		// build and add the action plugin
		TestPluginData testPluginData = pluginBuilder.build();
		Plugin testPlugin = TestPlugin.getTestPlugin(testPluginData);

		RegionsActionSupport.testConsumers(0, 5151111920517015649L, TimeTrackingPolicy.TRACK_TIME, testPlugin);

		// precondition test: if the person id is null
		RegionsActionSupport.testConsumer(0, 1490027040692903854L, TimeTrackingPolicy.TRACK_TIME, (c) -> {
			RegionDataManager regionDataManager = c.getDataManager(RegionDataManager.class).get();
			ContractException contractException = assertThrows(ContractException.class, () -> regionDataManager.getPersonRegion(null));
			assertEquals(PersonError.NULL_PERSON_ID, contractException.getErrorType());
		});

		// precondition test: if the person id is unknown
		RegionsActionSupport.testConsumer(0, 2144445839100475443L, TimeTrackingPolicy.TRACK_TIME, (c) -> {
			RegionDataManager regionDataManager = c.getDataManager(RegionDataManager.class).get();
			ContractException contractException = assertThrows(ContractException.class, () -> regionDataManager.getPersonRegion(new PersonId(-1)));
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
		pluginBuilder.addTestActorPlan("agent", new TestActorPlan(0, (c) -> {
			StochasticsDataManager stochasticsDataManager = c.getDataManager(StochasticsDataManager.class).get();
			PersonDataManager personDataManager = c.getDataManager(PersonDataManager.class).get();
			RegionDataManager regionDataManager = c.getDataManager(RegionDataManager.class).get();
			for (int i = 0; i < numberOfPeople; i++) {
				// select a region at random
				TestRegionId regionId = TestRegionId.getRandomRegionId(stochasticsDataManager.getRandomGenerator());
				// create the person
				PersonConstructionData personConstructionData = PersonConstructionData.builder().add(regionId).build();
				PersonId personId = personDataManager.addPerson(personConstructionData);

				// show that the person has a region arrival time of zero
				assertEquals(0.0, regionDataManager.getPersonRegionArrivalTime(personId));

				// add the person to the expectations
				expectedPersonRegionArrivalTimes.put(personId, new MutableDouble());
			}
		}));

		// move people over time and show that each time they are moved the
		// their arrival time is correct
		pluginBuilder.addTestActorPlan("agent", new TestActorPlan(0, (c) -> {
			PersonDataManager personDataManager = c.getDataManager(PersonDataManager.class).get();
			double planTime = 0;
			for (PersonId personId : personDataManager.getPeople()) {
				c.addPlan((c2) -> {
					// determine the person's current region
					RegionDataManager regionDataManager = c2.getDataManager(RegionDataManager.class).get();
					TestRegionId regionId = regionDataManager.getPersonRegion(personId);
					// select the next region for the person
					regionId = regionId.next();
					// move the person
					regionDataManager.setPersonRegion(personId, regionId);
					/*
					 * show that the region arrival time for the person is the
					 * current time in the simulation
					 */
					assertEquals(c2.getTime(), regionDataManager.getPersonRegionArrivalTime(personId));
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
		pluginBuilder.addTestActorPlan("agent", new TestActorPlan(postPersonMovementTime, (c) -> {
			PersonDataManager personDataManager = c.getDataManager(PersonDataManager.class).get();
			RegionDataManager regionDataManager = c.getDataManager(RegionDataManager.class).get();
			for (PersonId personId : personDataManager.getPeople()) {
				double expectedArrivalTime = expectedPersonRegionArrivalTimes.get(personId).getValue();
				double actualArrivalTime = regionDataManager.getPersonRegionArrivalTime(personId);
				assertEquals(expectedArrivalTime, actualArrivalTime);
			}
		}));

		// build and add the action plugin
		TestPluginData testPluginData = pluginBuilder.build();
		Plugin testPlugin = TestPlugin.getTestPlugin(testPluginData);
		RegionsActionSupport.testConsumers(0, 2278422620232176214L, TimeTrackingPolicy.TRACK_TIME, testPlugin);

		// precondition test: if region arrival times are not being tracked		
		pluginBuilder.addTestActorPlan("agent", new TestActorPlan(0, (c) -> {
			PersonDataManager personDataManager = c.getDataManager(PersonDataManager.class).get();
			StochasticsDataManager stochasticsDataManager = c.getDataManager(StochasticsDataManager.class).get();
			for (int i = 0; i < numberOfPeople; i++) {
				// select a region at random
				TestRegionId regionId = TestRegionId.getRandomRegionId(stochasticsDataManager.getRandomGenerator());
				// create the person
				PersonConstructionData personConstructionData = PersonConstructionData.builder().add(regionId).build();
				personDataManager.addPerson(personConstructionData);
			}
		}));

		

		// build and add the action plugin
		testPluginData = pluginBuilder.build();
		testPlugin = TestPlugin.getTestPlugin(testPluginData);
		RegionsActionSupport.testConsumers(0, 9214210856215652451L, TimeTrackingPolicy.TRACK_TIME, testPlugin);

		// precondition test: if region arrival times are not being tracked
		RegionsActionSupport.testConsumer(10, 1906010286127446114L, TimeTrackingPolicy.DO_NOT_TRACK_TIME, (c) -> {		
			RegionDataManager regionDataManager = c.getDataManager(RegionDataManager.class).get();

			// if region arrival times are not being tracked
			ContractException contractException = assertThrows(ContractException.class, () -> regionDataManager.getPersonRegionArrivalTime(new PersonId(0)));
			assertEquals(RegionError.REGION_ARRIVAL_TIMES_NOT_TRACKED, contractException.getErrorType());
		});

		// precondition test: if the person id is null
		RegionsActionSupport.testConsumer(0, 9214210856215652451L, TimeTrackingPolicy.TRACK_TIME, (c) -> {
			RegionDataManager regionDataManager = c.getDataManager(RegionDataManager.class).get();
			ContractException contractException = assertThrows(ContractException.class, () -> regionDataManager.getPersonRegionArrivalTime(null));
			assertEquals(PersonError.NULL_PERSON_ID, contractException.getErrorType());
		});

		// precondition test: if the person id is unknown
		RegionsActionSupport.testConsumer(0, 9132391945335483479L, TimeTrackingPolicy.TRACK_TIME, (c) -> {
			RegionDataManager regionDataManager = c.getDataManager(RegionDataManager.class).get();
			ContractException contractException = assertThrows(ContractException.class, () -> regionDataManager.getPersonRegionArrivalTime(new PersonId(-1)));
			assertEquals(PersonError.UNKNOWN_PERSON_ID, contractException.getErrorType());
		});

	}

	@Test
	@UnitTestMethod(name = "getPersonRegionArrivalTrackingPolicy", args = {})
	public void testGetPersonRegionArrivalTrackingPolicy() {
		for (TimeTrackingPolicy timeTrackingPolicy : TimeTrackingPolicy.values()) {
			RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(2934280155665825436L);
			RegionsActionSupport.testConsumer(0, randomGenerator.nextLong(), timeTrackingPolicy, (c) -> {
				RegionDataManager regionDataManager = c.getDataManager(RegionDataManager.class).get();
				assertEquals(timeTrackingPolicy, regionDataManager.getPersonRegionArrivalTrackingPolicy());
			});
		}
	}

	@Test
	@UnitTestMethod(name = "getRegionIds", args = {})
	public void testGetRegionIds() {
		RegionsActionSupport.testConsumer(0, 9132391945335483479L, TimeTrackingPolicy.TRACK_TIME, (c) -> {
			RegionDataManager regionDataManager = c.getDataManager(RegionDataManager.class).get();

			Set<RegionId> expectedRegionIds = new LinkedHashSet<>();
			for (TestRegionId testRegionId : TestRegionId.values()) {
				expectedRegionIds.add(testRegionId);
			}
			assertEquals(expectedRegionIds, regionDataManager.getRegionIds());
		});
	}

	@Test
	@UnitTestMethod(name = "getRegionPopulationCount", args = { RegionId.class })
	public void testGetRegionPopulationCount() {

		TestPluginData.Builder pluginBuilder = TestPluginData.builder();

		// show that each region has no people
		pluginBuilder.addTestActorPlan("agent", new TestActorPlan(0, (c) -> {
			RegionDataManager regionDataManager = c.getDataManager(RegionDataManager.class).get();
			for (TestRegionId testRegionId : TestRegionId.values()) {
				assertEquals(0, regionDataManager.getRegionPopulationCount(testRegionId));
			}
		}));

		// show that adding people results in the correct population counts

		pluginBuilder.addTestActorPlan("agent", new TestActorPlan(1, (c) -> {
			RegionDataManager regionDataManager = c.getDataManager(RegionDataManager.class).get();
			PersonDataManager personDataManager = c.getDataManager(PersonDataManager.class).get();
			int n = TestRegionId.values().length;
			for (int i = 0; i < 3 * n; i++) {
				TestRegionId regionId = TestRegionId.values()[i % n];
				PersonConstructionData personConstructionData = PersonConstructionData.builder().add(regionId).build();
				personDataManager.addPerson(personConstructionData);
			}

			for (TestRegionId testRegionId : TestRegionId.values()) {
				assertEquals(3, regionDataManager.getRegionPopulationCount(testRegionId));
			}

		}));

		// precondition tests

		pluginBuilder.addTestActorPlan("agent", new TestActorPlan(2, (c) -> {
			RegionDataManager regionDataManager = c.getDataManager(RegionDataManager.class).get();

			// if the region id is null
			ContractException contractException = assertThrows(ContractException.class, () -> regionDataManager.getRegionPopulationCount(null));
			assertEquals(RegionError.NULL_REGION_ID, contractException.getErrorType());

			// if the region id is unknown
			contractException = assertThrows(ContractException.class, () -> regionDataManager.getRegionPopulationCount(TestRegionId.getUnknownRegionId()));
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
		pluginBuilder.addTestActorPlan("agent", new TestActorPlan(0, (c) -> {
			RegionDataManager regionDataManager = c.getDataManager(RegionDataManager.class).get();
			for (TestRegionId testRegionId : TestRegionId.values()) {
				assertEquals(0, regionDataManager.getRegionPopulationTime(testRegionId));
			}

		}));

		Map<RegionId, MutableDouble> expectedAssignmentTimes = new LinkedHashMap<>();
		for (TestRegionId testRegionId : TestRegionId.values()) {
			expectedAssignmentTimes.put(testRegionId, new MutableDouble());
		}

		int numberOfPeople = 100;

		// show that adding people over time results in the correct population
		// times
		pluginBuilder.addTestActorPlan("agent", new TestActorPlan(0, (c) -> {
			for (int i = 0; i < numberOfPeople; i++) {
				double planTime = i;
				c.addPlan((c2) -> {
					RegionDataManager regionDataManager = c2.getDataManager(RegionDataManager.class).get();
					StochasticsDataManager stochasticsDataManager = c2.getDataManager(StochasticsDataManager.class).get();
					TestRegionId regionId = TestRegionId.getRandomRegionId(stochasticsDataManager.getRandomGenerator());
					PersonConstructionData personConstructionData = PersonConstructionData.builder().add(regionId).build();
					PersonDataManager personDataManager = c2.getDataManager(PersonDataManager.class).get();
					personDataManager.addPerson(personConstructionData);
					assertEquals(c2.getTime(), regionDataManager.getRegionPopulationTime(regionId), 0);
					expectedAssignmentTimes.get(regionId).setValue(c2.getTime());
				}, planTime);
			}
		}));

		// show that the proper region population times are maintained
		// after all the person additions are complete.
		double postPersonAdditionTime = numberOfPeople;

		pluginBuilder.addTestActorPlan("agent", new TestActorPlan(postPersonAdditionTime, (c) -> {
			RegionDataManager regionDataManager = c.getDataManager(RegionDataManager.class).get();
			for (TestRegionId testRegionId : TestRegionId.values()) {
				double expectedRegionPopulationTime = expectedAssignmentTimes.get(testRegionId).getValue();
				double actualRegionPopulationTime = regionDataManager.getRegionPopulationTime(testRegionId);
				assertEquals(expectedRegionPopulationTime, actualRegionPopulationTime);
			}
		}));

		// build and add the action plugin
		TestPluginData testPluginData = pluginBuilder.build();
		Plugin testPlugin = TestPlugin.getTestPlugin(testPluginData);
		RegionsActionSupport.testConsumers(numberOfPeople, 2430955549982485988L, TimeTrackingPolicy.TRACK_TIME, testPlugin);

		// precondition test: if the region id is null
		RegionsActionSupport.testConsumer(numberOfPeople, 3091951498637393024L, TimeTrackingPolicy.TRACK_TIME, (c) -> {
			RegionDataManager regionDataManager = c.getDataManager(RegionDataManager.class).get();
			ContractException contractException = assertThrows(ContractException.class, () -> regionDataManager.getRegionPopulationTime(null));
			assertEquals(RegionError.NULL_REGION_ID, contractException.getErrorType());

		});

		// precondition tests: if the region id is unknown
		RegionsActionSupport.testConsumer(numberOfPeople, 2415744693759237392L, TimeTrackingPolicy.TRACK_TIME, (c) -> {
			RegionDataManager regionDataManager = c.getDataManager(RegionDataManager.class).get();
			ContractException contractException = assertThrows(ContractException.class, () -> regionDataManager.getRegionPopulationTime(TestRegionId.getUnknownRegionId()));
			assertEquals(RegionError.UNKNOWN_REGION_ID, contractException.getErrorType());

		});

	}

	@Test
	@UnitTestMethod(name = "getRegionPropertyDefinition", args = { RegionPropertyId.class })
	public void testGetRegionPropertyDefinition() {
		RegionsActionSupport.testConsumer(0, 8915683065425449883L, TimeTrackingPolicy.TRACK_TIME, (c) -> {
			RegionDataManager regionDataManager = c.getDataManager(RegionDataManager.class).get();

			Set<TestRegionPropertyId> regionPropertyIds = regionDataManager.getRegionPropertyIds();
			assertEquals(TestRegionPropertyId.size(), regionPropertyIds.size());
			for (TestRegionPropertyId testRegionPropertyId : regionPropertyIds) {
				PropertyDefinition expectedPropertyDefinition = testRegionPropertyId.getPropertyDefinition();
				PropertyDefinition actualPropertyDefinition = regionDataManager.getRegionPropertyDefinition(testRegionPropertyId);
				assertEquals(expectedPropertyDefinition, actualPropertyDefinition);
			}

		});

		// precondition check: if the region property id is null
		RegionsActionSupport.testConsumer(0, 4217775232224320101L, TimeTrackingPolicy.TRACK_TIME, (c) -> {
			RegionDataManager regionDataManager = c.getDataManager(RegionDataManager.class).get();
			ContractException contractException = assertThrows(ContractException.class, () -> regionDataManager.getRegionPropertyDefinition(null));
			assertEquals(RegionError.NULL_REGION_PROPERTY_ID, contractException.getErrorType());
		});

		// precondition check: if the region property id is unknown
		RegionsActionSupport.testConsumer(0, 1425794836864585647L, TimeTrackingPolicy.TRACK_TIME, (c) -> {
			RegionDataManager regionDataManager = c.getDataManager(RegionDataManager.class).get();
			ContractException contractException = assertThrows(ContractException.class, () -> regionDataManager.getRegionPropertyDefinition(TestRegionPropertyId.getUnknownRegionPropertyId()));
			assertEquals(RegionError.UNKNOWN_REGION_PROPERTY_ID, contractException.getErrorType());
		});
	}

	@Test
	@UnitTestMethod(name = "getRegionPropertyIds", args = {})
	public void testGetRegionPropertyIds() {
		RegionsActionSupport.testConsumer(0, 2658585233315606268L, TimeTrackingPolicy.TRACK_TIME, (c) -> {
			RegionDataManager regionDataManager = c.getDataManager(RegionDataManager.class).get();
			Set<RegionPropertyId> expectedRegionPropertyIds = new LinkedHashSet<>();
			for (TestRegionPropertyId testRegionPropertyId : TestRegionPropertyId.values()) {
				expectedRegionPropertyIds.add(testRegionPropertyId);
			}
			assertEquals(expectedRegionPropertyIds, regionDataManager.getRegionPropertyIds());
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
				RegionDataManager regionDataManager = c.getDataManager(RegionDataManager.class).get();
				StochasticsDataManager stochasticsDataManager = c.getDataManager(StochasticsDataManager.class).get();
				RandomGenerator randomGenerator = stochasticsDataManager.getRandomGenerator();
				TestRegionId testRegionId = TestRegionId.getRandomRegionId(randomGenerator);
				TestRegionPropertyId testRegionPropertyId = TestRegionPropertyId.getRandomMutableRegionPropertyId(randomGenerator);

				// show that the property has the correct value
				MultiKey multiKey = new MultiKey(testRegionId, testRegionPropertyId);
				Object expectedPropertyValue = expectedPropertyValues.get(multiKey);

				Object actualPropertyValue = regionDataManager.getRegionPropertyValue(testRegionId, testRegionPropertyId);
				assertEquals(expectedPropertyValue, actualPropertyValue);

				Object newPropertyValue = testRegionPropertyId.getRandomPropertyValue(randomGenerator);
				regionDataManager.setRegionPropertyValue(testRegionId, testRegionPropertyId, newPropertyValue);
				expectedPropertyValues.put(multiKey, newPropertyValue);

			}));
		}

		TestPluginData testPluginData = pluginBuilder.build();
		Plugin testPlugin = TestPlugin.getTestPlugin(testPluginData);
		RegionsActionSupport.testConsumers(0, 8784099691519492811L, TimeTrackingPolicy.TRACK_TIME, testPlugin);

		// precondition check: if the region id is null
		RegionsActionSupport.testConsumer(0, 8784099691519492811L, TimeTrackingPolicy.TRACK_TIME, (c) -> {
			RegionDataManager regionDataManager = c.getDataManager(RegionDataManager.class).get();
			RegionPropertyId knownRegionPropertyId = TestRegionPropertyId.REGION_PROPERTY_1_BOOLEAN_MUTABLE;
			ContractException contractException = assertThrows(ContractException.class, () -> regionDataManager.getRegionPropertyValue(null, knownRegionPropertyId));
			assertEquals(RegionError.NULL_REGION_ID, contractException.getErrorType());
		});

		// precondition check: if the region id is not known
		RegionsActionSupport.testConsumer(0, 1546629608367614750L, TimeTrackingPolicy.TRACK_TIME, (c) -> {
			RegionDataManager regionDataManager = c.getDataManager(RegionDataManager.class).get();
			RegionId unknownRegionId = TestRegionId.getUnknownRegionId();
			RegionPropertyId knownRegionPropertyId = TestRegionPropertyId.REGION_PROPERTY_1_BOOLEAN_MUTABLE;
			ContractException contractException = assertThrows(ContractException.class, () -> regionDataManager.getRegionPropertyValue(unknownRegionId, knownRegionPropertyId));
			assertEquals(RegionError.UNKNOWN_REGION_ID, contractException.getErrorType());
		});

		// precondition check: if the region property id is null
		RegionsActionSupport.testConsumer(0, 2997323471294141386L, TimeTrackingPolicy.TRACK_TIME, (c) -> {
			RegionDataManager regionDataManager = c.getDataManager(RegionDataManager.class).get();
			RegionId knownRegionId = TestRegionId.REGION_1;
			ContractException contractException = assertThrows(ContractException.class, () -> regionDataManager.getRegionPropertyValue(knownRegionId, null));
			assertEquals(RegionError.NULL_REGION_PROPERTY_ID, contractException.getErrorType());
		});

		// precondition check: if the region property id is unknown
		RegionsActionSupport.testConsumer(0, 3797498566412748237L, TimeTrackingPolicy.TRACK_TIME, (c) -> {
			RegionDataManager regionDataManager = c.getDataManager(RegionDataManager.class).get();
			RegionId knownRegionId = TestRegionId.REGION_1;
			RegionPropertyId unknownRegionPropertyId = TestRegionPropertyId.getUnknownRegionPropertyId();
			ContractException contractException = assertThrows(ContractException.class, () -> regionDataManager.getRegionPropertyValue(knownRegionId, unknownRegionPropertyId));
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
			RegionDataManager regionDataManager = c.getDataManager(RegionDataManager.class).get();
			assertTrue(regionDataManager.getRegionIds().size() > 0);
			assertTrue(regionDataManager.getRegionPropertyIds().size() > 0);
			for (RegionId regionId : regionDataManager.getRegionIds()) {
				for (RegionPropertyId regionPropertyId : regionDataManager.getRegionPropertyIds()) {
					double regionPropertyTime = regionDataManager.getRegionPropertyTime(regionId, regionPropertyId);
					assertEquals(0, regionPropertyTime, 0);
				}
			}
		}));

		// show that changes to the property values properly reflect the time
		// the occured

		for (int i = 0; i < 300; i++) {
			pluginBuilder.addTestActorPlan("actor", new TestActorPlan(i, (c) -> {
				RegionDataManager regionDataManager = c.getDataManager(RegionDataManager.class).get();
				StochasticsDataManager stochasticsDataManager = c.getDataManager(StochasticsDataManager.class).get();
				RandomGenerator randomGenerator = stochasticsDataManager.getRandomGenerator();
				TestRegionId testRegionId = TestRegionId.getRandomRegionId(randomGenerator);
				TestRegionPropertyId testRegionPropertyId = TestRegionPropertyId.getRandomMutableRegionPropertyId(randomGenerator);

				// show that the property has the correct time
				MutableDouble mutableDouble = expectedPropertyTimes.get(new MultiKey(testRegionId, testRegionPropertyId));
				double expectedPropertyTime = mutableDouble.getValue();
				double actualPropertyTime = regionDataManager.getRegionPropertyTime(testRegionId, testRegionPropertyId);
				assertEquals(expectedPropertyTime, actualPropertyTime);

				Object newPropertyValue = testRegionPropertyId.getRandomPropertyValue(randomGenerator);

				regionDataManager.setRegionPropertyValue(testRegionId, testRegionPropertyId, newPropertyValue);
				assertEquals(c.getTime(), regionDataManager.getRegionPropertyTime(testRegionId, testRegionPropertyId));
				mutableDouble.setValue(c.getTime());
			}));
		}

		TestPluginData testPluginData = pluginBuilder.build();
		Plugin testPlugin = TestPlugin.getTestPlugin(testPluginData);
		RegionsActionSupport.testConsumers(0, 1085097084913380645L, TimeTrackingPolicy.TRACK_TIME, testPlugin);

		// precondition check: if the region id is null
		RegionsActionSupport.testConsumer(0, 9165213921588406384L, TimeTrackingPolicy.TRACK_TIME, (c) -> {
			RegionDataManager regionDataManager = c.getDataManager(RegionDataManager.class).get();
			RegionPropertyId knownRegionPropertyId = TestRegionPropertyId.REGION_PROPERTY_1_BOOLEAN_MUTABLE;
			ContractException contractException = assertThrows(ContractException.class, () -> regionDataManager.getRegionPropertyTime(null, knownRegionPropertyId));
			assertEquals(RegionError.NULL_REGION_ID, contractException.getErrorType());
		});

		// precondition check: if the region id is not known
		RegionsActionSupport.testConsumer(0, 1546629608367614750L, TimeTrackingPolicy.TRACK_TIME, (c) -> {
			RegionDataManager regionDataManager = c.getDataManager(RegionDataManager.class).get();
			RegionId unknownRegionId = TestRegionId.getUnknownRegionId();
			RegionPropertyId knownRegionPropertyId = TestRegionPropertyId.REGION_PROPERTY_1_BOOLEAN_MUTABLE;
			ContractException contractException = assertThrows(ContractException.class, () -> regionDataManager.getRegionPropertyTime(unknownRegionId, knownRegionPropertyId));
			assertEquals(RegionError.UNKNOWN_REGION_ID, contractException.getErrorType());
		});

		// precondition check: if the region property id is null
		RegionsActionSupport.testConsumer(0, 7141175136643291537L, TimeTrackingPolicy.TRACK_TIME, (c) -> {
			RegionDataManager regionDataManager = c.getDataManager(RegionDataManager.class).get();
			RegionId knownRegionId = TestRegionId.REGION_1;
			ContractException contractException = assertThrows(ContractException.class, () -> regionDataManager.getRegionPropertyTime(knownRegionId, null));
			assertEquals(RegionError.NULL_REGION_PROPERTY_ID, contractException.getErrorType());
		});

		// precondition check: if the region property id is unknown
		RegionsActionSupport.testConsumer(0, 2200230008116664966L, TimeTrackingPolicy.TRACK_TIME, (c) -> {
			RegionDataManager regionDataManager = c.getDataManager(RegionDataManager.class).get();
			RegionId knownRegionId = TestRegionId.REGION_1;
			RegionPropertyId unknownRegionPropertyId = TestRegionPropertyId.getUnknownRegionPropertyId();
			ContractException contractException = assertThrows(ContractException.class, () -> regionDataManager.getRegionPropertyTime(knownRegionId, unknownRegionPropertyId));
			assertEquals(RegionError.UNKNOWN_REGION_PROPERTY_ID, contractException.getErrorType());
		});

	}

	@Test
	@UnitTestMethod(name = "regionIdExists", args = { RegionId.class })
	public void testRegionIdExists() {
		RegionsActionSupport.testConsumer(0, 3797498566412748237L, TimeTrackingPolicy.TRACK_TIME, (c) -> {
			RegionDataManager regionDataManager = c.getDataManager(RegionDataManager.class).get();

			// show that null region ids do not exist
			assertFalse(regionDataManager.regionIdExists(null));

			// show that the region ids added do exist
			for (TestRegionId testRegionId : TestRegionId.values()) {
				assertTrue(regionDataManager.regionIdExists(testRegionId));
			}

			// show that an unknown region id does not exist
			assertFalse(regionDataManager.regionIdExists(TestRegionId.getUnknownRegionId()));

		});

	}

	@Test
	@UnitTestMethod(name = "regionPropertyIdExists", args = { RegionPropertyId.class })
	public void testRegionPropertyIdExists() {
		RegionsActionSupport.testConsumer(0, 3797498566412748237L, TimeTrackingPolicy.TRACK_TIME, (c) -> {
			RegionDataManager regionDataManager = c.getDataManager(RegionDataManager.class).get();

			// show that the property ids exist
			for (TestRegionPropertyId testRegionPropertyId : TestRegionPropertyId.values()) {
				assertTrue(regionDataManager.regionPropertyIdExists(testRegionPropertyId));
			}

			// show that null references return false
			assertFalse(regionDataManager.regionPropertyIdExists(null));

			// show that unknown region property ids return false
			assertFalse(regionDataManager.regionPropertyIdExists(TestRegionPropertyId.getUnknownRegionPropertyId()));

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
				EventLabel<PersonRegionChangeObservationEvent> eventLabel = PersonRegionChangeObservationEvent.getEventLabelByArrivalRegion(c, testRegionId);
				c.subscribe(eventLabel, (c2, e) -> {
					recievedObservations.add(new MultiKey(e.getPreviousRegionId(), e.getCurrentRegionId(), e.getPersonId(), c2.getTime()));
				});
			}
		}));

		/*
		 * Have the mover agent move every person over time and show that each
		 * person is where we expect them to be
		 */
		pluginBuilder.addTestActorPlan("mover agent", new TestActorPlan(1, (c) -> {

			/*
			 * Make sure that there are actually people in the simulation so
			 * that test is actually testing something
			 */
			PersonDataManager personDataManager = c.getDataManager(PersonDataManager.class).get();
			RegionDataManager regionDataManager = c.getDataManager(RegionDataManager.class).get();

			List<PersonId> people = personDataManager.getPeople();
			assertTrue(people.size() > 0);

			// schedule a time for each person to be moved to a new region
			double planTime = 10;
			for (final PersonId personId : people) {
				c.addPlan((c2) -> {
					TestRegionId regionId = regionDataManager.getPersonRegion(personId);
					TestRegionId nextRegionId = regionId.next();
					regionDataManager.setPersonRegion(personId, nextRegionId);

					// show that the person's region is updated
					assertEquals(nextRegionId, regionDataManager.getPersonRegion(personId));
					expectedObservations.add(new MultiKey(regionId, nextRegionId, personId, c2.getTime()));

					// show that the person's region arrival time is
					// updated
					assertEquals(c2.getTime(), regionDataManager.getPersonRegionArrivalTime(personId));

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

			StochasticsDataManager stochasticsDataManager = c.getDataManager(StochasticsDataManager.class).get();
			RandomGenerator randomGenerator = stochasticsDataManager.getRandomGenerator();

			PersonDataManager personDataManager = c.getDataManager(PersonDataManager.class).get();
			List<PersonId> people = personDataManager.getPeople();
			PersonId personId = people.get(randomGenerator.nextInt(people.size()));

			// establish the person's current region and next region
			RegionDataManager regionDataManager = c.getDataManager(RegionDataManager.class).get();
			TestRegionId currentRegionId = regionDataManager.getPersonRegion(personId);
			TestRegionId nextRegionId = currentRegionId.next();

			ContractException contractException = assertThrows(ContractException.class, () -> regionDataManager.setPersonRegion(null, nextRegionId));
			assertEquals(PersonError.NULL_PERSON_ID, contractException.getErrorType());

		});

		/*
		 * precondition test: if the person id is unknown
		 */
		RegionsActionSupport.testConsumer(numberOfPeople, 6693022571477538917L, TimeTrackingPolicy.TRACK_TIME, (c) -> {
			// Select a person at random from the simulation and create a person
			// id outside of the simulation

			StochasticsDataManager stochasticsDataManager = c.getDataManager(StochasticsDataManager.class).get();
			RandomGenerator randomGenerator = stochasticsDataManager.getRandomGenerator();

			PersonDataManager personDataManager = c.getDataManager(PersonDataManager.class).get();
			List<PersonId> people = personDataManager.getPeople();
			PersonId personId = people.get(randomGenerator.nextInt(people.size()));
			PersonId badPersonId = new PersonId(people.size());

			// establish the person's current region and next region
			RegionDataManager regionDataManager = c.getDataManager(RegionDataManager.class).get();
			TestRegionId currentRegionId = regionDataManager.getPersonRegion(personId);
			TestRegionId nextRegionId = currentRegionId.next();

			ContractException contractException = assertThrows(ContractException.class, () -> regionDataManager.setPersonRegion(badPersonId, nextRegionId));
			assertEquals(PersonError.UNKNOWN_PERSON_ID, contractException.getErrorType());
		});

		/*
		 * precondition test: if the region id is null
		 */
		RegionsActionSupport.testConsumer(numberOfPeople, 1385204599279421266L, TimeTrackingPolicy.TRACK_TIME, (c) -> {
			// Select a person at random from the simulation and create a person
			// id outside of the simulation

			StochasticsDataManager stochasticsDataManager = c.getDataManager(StochasticsDataManager.class).get();
			RandomGenerator randomGenerator = stochasticsDataManager.getRandomGenerator();

			PersonDataManager personDataManager = c.getDataManager(PersonDataManager.class).get();
			List<PersonId> people = personDataManager.getPeople();
			PersonId personId = people.get(randomGenerator.nextInt(people.size()));

			// establish the person's current region and next region
			RegionDataManager regionDataManager = c.getDataManager(RegionDataManager.class).get();

			//
			ContractException contractException = assertThrows(ContractException.class, () -> regionDataManager.setPersonRegion(personId, null));
			assertEquals(RegionError.NULL_REGION_ID, contractException.getErrorType());

		});

		/*
		 * precondition test: if the region id is unknown
		 */
		RegionsActionSupport.testConsumer(numberOfPeople, 6025662871362676118L, TimeTrackingPolicy.TRACK_TIME, (c) -> {
			// Select a person at random from the simulation and create a person
			// id outside of the simulation

			StochasticsDataManager stochasticsDataManager = c.getDataManager(StochasticsDataManager.class).get();
			RandomGenerator randomGenerator = stochasticsDataManager.getRandomGenerator();

			PersonDataManager personDataManager = c.getDataManager(PersonDataManager.class).get();
			List<PersonId> people = personDataManager.getPeople();
			PersonId personId = people.get(randomGenerator.nextInt(people.size()));

			// establish the person's current region and next region
			RegionDataManager regionDataManager = c.getDataManager(RegionDataManager.class).get();

			// create a non-existent region id
			RegionId unknownRegionId = TestRegionId.getUnknownRegionId();

			ContractException contractException = assertThrows(ContractException.class, () -> regionDataManager.setPersonRegion(personId, unknownRegionId));
			assertEquals(RegionError.UNKNOWN_REGION_ID, contractException.getErrorType());

		});

		/*
		 * precondition test: if the region is the current region for the person
		 */
		RegionsActionSupport.testConsumer(numberOfPeople, 5385423081958576523L, TimeTrackingPolicy.TRACK_TIME, (c) -> {
			// Select a person at random from the simulation and create a person
			// id outside of the simulation

			StochasticsDataManager stochasticsDataManager = c.getDataManager(StochasticsDataManager.class).get();
			RandomGenerator randomGenerator = stochasticsDataManager.getRandomGenerator();

			PersonDataManager personDataManager = c.getDataManager(PersonDataManager.class).get();
			List<PersonId> people = personDataManager.getPeople();
			PersonId personId = people.get(randomGenerator.nextInt(people.size()));

			// establish the person's current region and next region
			RegionDataManager regionDataManager = c.getDataManager(RegionDataManager.class).get();
			TestRegionId currentRegionId = regionDataManager.getPersonRegion(personId);

			ContractException contractException = assertThrows(ContractException.class, () -> regionDataManager.setPersonRegion(personId, currentRegionId));
			assertEquals(RegionError.SAME_REGION, contractException.getErrorType());

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
		pluginBuilder.addTestActorPlan("observer agent", new TestActorPlan(0, (c) -> {

			EventLabel<RegionPropertyChangeObservationEvent> eventLabel = RegionPropertyChangeObservationEvent.getEventLabelByRegionAndProperty(c, TestRegionId.REGION_1,
					TestRegionPropertyId.REGION_PROPERTY_2_INTEGER_MUTABLE);
			c.subscribe(eventLabel, (c2, e) -> {
				actualObservations.add(new MultiKey(c2.getTime(), e.getRegionId(), e.getRegionPropertyId(), e.getCurrentPropertyValue()));
			});

			eventLabel = RegionPropertyChangeObservationEvent.getEventLabelByRegionAndProperty(c, TestRegionId.REGION_2, TestRegionPropertyId.REGION_PROPERTY_3_DOUBLE_MUTABLE);
			c.subscribe(eventLabel, (c2, e) -> {
				actualObservations.add(new MultiKey(c2.getTime(), e.getRegionId(), e.getRegionPropertyId(), e.getCurrentPropertyValue()));
			});

		}));

		// Have the update agent make various region property updates over
		// time
		pluginBuilder.addTestActorPlan("update agent", new TestActorPlan(0, (c) -> {
			StochasticsDataManager stochasticsDataManager = c.getDataManager(StochasticsDataManager.class).get();
			RandomGenerator randomGenerator = stochasticsDataManager.getRandomGenerator();

			for (int i = 0; i < 10; i++) {
				c.addPlan((c2) -> {
					StochasticsDataManager stochasticsDataManager2 = c.getDataManager(StochasticsDataManager.class).get();
					RandomGenerator randomGenerator2 = stochasticsDataManager2.getRandomGenerator();
					RegionDataManager regionDataManager = c2.getDataManager(RegionDataManager.class).get();
					Integer newValue = randomGenerator2.nextInt();
					regionDataManager.setRegionPropertyValue(TestRegionId.REGION_1, TestRegionPropertyId.REGION_PROPERTY_2_INTEGER_MUTABLE, newValue);
					Integer actualValue = regionDataManager.getRegionPropertyValue(TestRegionId.REGION_1, TestRegionPropertyId.REGION_PROPERTY_2_INTEGER_MUTABLE);
					assertEquals(newValue, actualValue);
					double valueTime = regionDataManager.getRegionPropertyTime(TestRegionId.REGION_1, TestRegionPropertyId.REGION_PROPERTY_2_INTEGER_MUTABLE);
					assertEquals(c2.getTime(), valueTime);
					expectedObservations.add(new MultiKey(c2.getTime(), TestRegionId.REGION_1, TestRegionPropertyId.REGION_PROPERTY_2_INTEGER_MUTABLE, newValue));
				}, randomGenerator.nextDouble() * 1000);
			}

			for (int i = 0; i < 10; i++) {
				c.addPlan((c2) -> {
					StochasticsDataManager stochasticsDataManager2 = c.getDataManager(StochasticsDataManager.class).get();
					RandomGenerator randomGenerator2 = stochasticsDataManager2.getRandomGenerator();
					RegionDataManager regionDataManager = c2.getDataManager(RegionDataManager.class).get();
					Double newValue = randomGenerator2.nextDouble();
					regionDataManager.setRegionPropertyValue(TestRegionId.REGION_2, TestRegionPropertyId.REGION_PROPERTY_3_DOUBLE_MUTABLE, newValue);
					Double actualValue = regionDataManager.getRegionPropertyValue(TestRegionId.REGION_2, TestRegionPropertyId.REGION_PROPERTY_3_DOUBLE_MUTABLE);
					assertEquals(newValue, actualValue);
					double valueTime = regionDataManager.getRegionPropertyTime(TestRegionId.REGION_2, TestRegionPropertyId.REGION_PROPERTY_3_DOUBLE_MUTABLE);
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
			RegionDataManager regionDataManager = c.getDataManager(RegionDataManager.class).get();

			ContractException contractException = assertThrows(ContractException.class, () -> regionDataManager.setRegionPropertyValue(null, regionPropertyId, propertyValue));
			assertEquals(RegionError.NULL_REGION_ID, contractException.getErrorType());

		});

		// precondition check: if the region id is unknown
		RegionsActionSupport.testConsumer(0, 3075330757105736185L, TimeTrackingPolicy.TRACK_TIME, (c) -> {

			RegionId unknownRegionId = TestRegionId.getUnknownRegionId();
			RegionPropertyId regionPropertyId = TestRegionPropertyId.REGION_PROPERTY_2_INTEGER_MUTABLE;
			Object propertyValue = 67;
			RegionDataManager regionDataManager = c.getDataManager(RegionDataManager.class).get();

			ContractException contractException = assertThrows(ContractException.class, () -> regionDataManager.setRegionPropertyValue(unknownRegionId, regionPropertyId, propertyValue));
			assertEquals(RegionError.UNKNOWN_REGION_ID, contractException.getErrorType());

		});

		// precondition check: if the region property id is null
		RegionsActionSupport.testConsumer(0, 4169934733913962790L, TimeTrackingPolicy.TRACK_TIME, (c) -> {
			RegionId regionId = TestRegionId.REGION_1;
			Object propertyValue = 67;
			RegionDataManager regionDataManager = c.getDataManager(RegionDataManager.class).get();

			ContractException contractException = assertThrows(ContractException.class, () -> regionDataManager.setRegionPropertyValue(regionId, null, propertyValue));
			assertEquals(RegionError.NULL_REGION_PROPERTY_ID, contractException.getErrorType());

		});

		// precondition check: if the region property id is unknown
		RegionsActionSupport.testConsumer(0, 5578070775436119166L, TimeTrackingPolicy.TRACK_TIME, (c) -> {
			RegionId regionId = TestRegionId.REGION_1;
			RegionPropertyId unknownRegionPropertyId = TestRegionPropertyId.getUnknownRegionPropertyId();
			Object propertyValue = 67;
			RegionDataManager regionDataManager = c.getDataManager(RegionDataManager.class).get();

			ContractException contractException = assertThrows(ContractException.class, () -> regionDataManager.setRegionPropertyValue(regionId, unknownRegionPropertyId, propertyValue));
			assertEquals(RegionError.UNKNOWN_REGION_PROPERTY_ID, contractException.getErrorType());

		});

		// precondition check: if the region property value is null
		RegionsActionSupport.testConsumer(0, 217279748753596418L, TimeTrackingPolicy.TRACK_TIME, (c) -> {
			RegionId regionId = TestRegionId.REGION_1;
			RegionPropertyId regionPropertyId = TestRegionPropertyId.REGION_PROPERTY_2_INTEGER_MUTABLE;
			RegionDataManager regionDataManager = c.getDataManager(RegionDataManager.class).get();

			ContractException contractException = assertThrows(ContractException.class, () -> regionDataManager.setRegionPropertyValue(regionId, regionPropertyId, null));
			assertEquals(RegionError.NULL_REGION_PROPERTY_VALUE, contractException.getErrorType());

		});

		// precondition check: if the region property value is incompatible with
		// the defined type for the property
		RegionsActionSupport.testConsumer(0, 7043526072670323223L, TimeTrackingPolicy.TRACK_TIME, (c) -> {
			RegionId regionId = TestRegionId.REGION_1;
			RegionPropertyId regionPropertyId = TestRegionPropertyId.REGION_PROPERTY_2_INTEGER_MUTABLE;
			Object incompatiblePropertyValue = "incompatible value";
			RegionDataManager regionDataManager = c.getDataManager(RegionDataManager.class).get();

			ContractException contractException = assertThrows(ContractException.class, () -> regionDataManager.setRegionPropertyValue(regionId, regionPropertyId, incompatiblePropertyValue));
			assertEquals(PropertyError.INCOMPATIBLE_VALUE, contractException.getErrorType());

		});

		// precondition check: if the region id is unknown
		RegionsActionSupport.testConsumer(0, 8501593854721316109L, TimeTrackingPolicy.TRACK_TIME, (c) -> {
			RegionId unknownRegionId = TestRegionId.getUnknownRegionId();
			RegionPropertyId regionPropertyId = TestRegionPropertyId.REGION_PROPERTY_2_INTEGER_MUTABLE;
			Object propertyValue = 67;
			RegionDataManager regionDataManager = c.getDataManager(RegionDataManager.class).get();

			ContractException contractException = assertThrows(ContractException.class, () -> regionDataManager.setRegionPropertyValue(unknownRegionId, regionPropertyId, propertyValue));
			assertEquals(RegionError.UNKNOWN_REGION_ID, contractException.getErrorType());

		});

		// precondition check: if the region property value is null
		RegionsActionSupport.testConsumer(0, 8501593854721316109L, TimeTrackingPolicy.TRACK_TIME, (c) -> {
			RegionId regionId = TestRegionId.REGION_1;
			RegionPropertyId immutableRegionPropertyId = TestRegionPropertyId.REGION_PROPERTY_5_INTEGER_IMMUTABLE;			
			RegionDataManager regionDataManager = c.getDataManager(RegionDataManager.class).get();

			ContractException contractException = assertThrows(ContractException.class, () -> regionDataManager.setRegionPropertyValue(regionId, immutableRegionPropertyId, null));
			assertEquals(RegionError.NULL_REGION_PROPERTY_VALUE, contractException.getErrorType());

		});
	}

	
	
	@Test
	@UnitTestMethod(name = "init", args = {})
	public void testRegionPluginData() {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(4454658950052475227L);
		int initialPopulation = 30;

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

		regionPluginBuilder.setPersonRegionArrivalTracking(TimeTrackingPolicy.TRACK_TIME);
		RegionPluginData regionPluginData = regionPluginBuilder.build();
		builder.addPlugin(RegionPlugin.getRegionPlugin(regionPluginData));

		// add the people plugin
		TestRegionId testRegionId = TestRegionId.REGION_1;
		BulkPersonConstructionData.Builder bulkBuilder = BulkPersonConstructionData.builder();
		for (int i = 0; i < initialPopulation; i++) {
			PersonConstructionData personConstructionData = PersonConstructionData.builder().add(testRegionId).build();
			testRegionId = testRegionId.next();
			bulkBuilder.add(personConstructionData);
		}
		PeoplePluginData peoplePluginData = PeoplePluginData.builder().addBulkPersonConstructionData(bulkBuilder.build()).build();
		builder.addPlugin(PeoplePlugin.getPeoplePlugin(peoplePluginData));

		// add the report plugin
		builder.addPlugin(ReportsPlugin.getReportPlugin(ReportsPluginData.builder().build()));

		// add the stochastics plugin
		builder.addPlugin(StochasticsPlugin.getPlugin(StochasticsPluginData.builder().setSeed(randomGenerator.nextLong()).build()));

		// add the partitions plugin
		builder.addPlugin(PartitionsPlugin.getPartitionsPlugin());

		// add the test plugin
		TestPluginData.Builder pluginBuilder = TestPluginData.builder();

		pluginBuilder.addTestActorPlan("actor", new TestActorPlan(0, (c) -> {
			RegionDataManager regionDataManager = c.getDataManager(RegionDataManager.class).get();
			// show that the initial state of the region data manager matches
			// the state of the region plugin data

			assertEquals(regionPluginData.getPersonRegionArrivalTrackingPolicy(), regionDataManager.getPersonRegionArrivalTrackingPolicy());
			assertEquals(regionPluginData.getRegionIds(), regionDataManager.getRegionIds());
			assertEquals(regionPluginData.getRegionPropertyIds(), regionDataManager.getRegionPropertyIds());
			for (RegionPropertyId regionPropertyId : regionPluginData.getRegionPropertyIds()) {
				PropertyDefinition expectedPropertyDefinition = regionPluginData.getRegionPropertyDefinition(regionPropertyId);
				PropertyDefinition actualPropertyDefinition = regionDataManager.getRegionPropertyDefinition(regionPropertyId);
				assertEquals(expectedPropertyDefinition, actualPropertyDefinition);
			}
			for (RegionId regionId : regionPluginData.getRegionIds()) {
				for (RegionPropertyId regionPropertyId : regionPluginData.getRegionPropertyIds()) {
					Object expectedValue = regionPluginData.getRegionPropertyValue(regionId, regionPropertyId);
					Object actualValue = regionDataManager.getRegionPropertyValue(regionId, regionPropertyId);
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
	 * Shows that all event {@linkplain PersonRegionChangeObservationEvent}
	 * labelers are created
	 */
	@Test
	@UnitTestMethod(name = "init", args = {})
	public void testPersonRegionChangeObservationEventLabelers() {
		RegionsActionSupport.testConsumer(0, 2734071676096451334L, TimeTrackingPolicy.DO_NOT_TRACK_TIME, (c) -> {
			EventLabeler<PersonRegionChangeObservationEvent> eventLabelerForArrivalRegion = PersonRegionChangeObservationEvent.getEventLabelerForArrivalRegion();
			assertNotNull(eventLabelerForArrivalRegion);
			ContractException contractException = assertThrows(ContractException.class, () -> c.addEventLabeler(eventLabelerForArrivalRegion));
			assertEquals(NucleusError.DUPLICATE_LABELER_ID_IN_EVENT_LABELER, contractException.getErrorType());

			EventLabeler<PersonRegionChangeObservationEvent> eventLabelerForDepartureRegion = PersonRegionChangeObservationEvent.getEventLabelerForDepartureRegion();
			assertNotNull(eventLabelerForDepartureRegion);
			contractException = assertThrows(ContractException.class, () -> c.addEventLabeler(eventLabelerForDepartureRegion));
			assertEquals(NucleusError.DUPLICATE_LABELER_ID_IN_EVENT_LABELER, contractException.getErrorType());

			EventLabeler<PersonRegionChangeObservationEvent> eventLabelerForPerson = PersonRegionChangeObservationEvent.getEventLabelerForPerson();
			assertNotNull(eventLabelerForPerson);
			contractException = assertThrows(ContractException.class, () -> c.addEventLabeler(eventLabelerForPerson));
			assertEquals(NucleusError.DUPLICATE_LABELER_ID_IN_EVENT_LABELER, contractException.getErrorType());

		});
	}

	/**
	 * Shows that all event {@linkplain RegionPropertyChangeObservationEvent}
	 * labelers are created
	 */
	@Test
	@UnitTestMethod(name = "init", args = {})
	public void testRegionPropertyChangeObservationEventLabelers() {

		RegionsActionSupport.testConsumer(0, 4228466028646070532L, TimeTrackingPolicy.DO_NOT_TRACK_TIME, (c) -> {
			EventLabeler<RegionPropertyChangeObservationEvent> eventLabeler1 = RegionPropertyChangeObservationEvent.getEventLabelerForProperty();
			assertNotNull(eventLabeler1);
			ContractException contractException = assertThrows(ContractException.class, () -> c.addEventLabeler(eventLabeler1));
			assertEquals(NucleusError.DUPLICATE_LABELER_ID_IN_EVENT_LABELER, contractException.getErrorType());

			EventLabeler<RegionPropertyChangeObservationEvent> eventLabeler2 = RegionPropertyChangeObservationEvent.getEventLabelerForRegionAndProperty();
			assertNotNull(eventLabeler2);
			contractException = assertThrows(ContractException.class, () -> c.addEventLabeler(eventLabeler2));
			assertEquals(NucleusError.DUPLICATE_LABELER_ID_IN_EVENT_LABELER, contractException.getErrorType());

		});

	}

	@Test
	@UnitTestMethod(name = "init", args = {})
	public void testPersonCreationObservationEvent() {
		/*
		 * Have the agent create some people over time and show that each person
		 * is in the correct region at the correct time
		 */
		RegionsActionSupport.testConsumer(0, 8294774271110836859L, TimeTrackingPolicy.TRACK_TIME, (c) -> {
			StochasticsDataManager stochasticsDataManager = c.getDataManager(StochasticsDataManager.class).get();
			RandomGenerator randomGenerator = stochasticsDataManager.getRandomGenerator();

			for (int i = 0; i < 100; i++) {
				c.addPlan((c2) -> {
					StochasticsDataManager stochasticsDataManager2 = c2.getDataManager(StochasticsDataManager.class).get();
					RegionDataManager regionDataManager = c2.getDataManager(RegionDataManager.class).get();
					PersonDataManager personDataManager = c2.getDataManager(PersonDataManager.class).get();

					/*
					 * Generate a random region to for the new person and add
					 * the person
					 */
					TestRegionId randomRegionId = TestRegionId.getRandomRegionId(stochasticsDataManager2.getRandomGenerator());
					PersonConstructionData personConstructionData = PersonConstructionData.builder().add(randomRegionId).build();
					PersonId personId = personDataManager.addPerson(personConstructionData);

					/*
					 * Show that the person is in the correct region with the
					 * correct region arrival time
					 */
					RegionId personRegionId = regionDataManager.getPersonRegion(personId);
					assertEquals(randomRegionId, personRegionId);
					assertEquals(c2.getTime(), regionDataManager.getPersonRegionArrivalTime(personId));

				}, randomGenerator.nextDouble() * 1000);
			}

		});

		// precondition check: if no region data was included in the event
		RegionsActionSupport.testConsumer(0, 8294774271110836859L, TimeTrackingPolicy.TRACK_TIME, (c) -> {
			PersonDataManager personDataManager = c.getDataManager(PersonDataManager.class).get();
			PersonConstructionData personConstructionData = PersonConstructionData.builder().build();
			ContractException contractException = assertThrows(ContractException.class, () -> personDataManager.addPerson(personConstructionData));
			assertEquals(RegionError.NULL_REGION_ID, contractException.getErrorType());
		});

		// precondition check: if the region in the event is unknown
		RegionsActionSupport.testConsumer(0, 2879410509293373914L, TimeTrackingPolicy.TRACK_TIME, (c) -> {
			PersonDataManager personDataManager = c.getDataManager(PersonDataManager.class).get();
			PersonConstructionData personConstructionData = PersonConstructionData.builder().add(TestRegionId.getUnknownRegionId()).build();
			ContractException contractException = assertThrows(ContractException.class, () -> personDataManager.addPerson(personConstructionData));
			assertEquals(RegionError.UNKNOWN_REGION_ID, contractException.getErrorType());
		});

		// precondition check: if the person id does not exist
		RegionsActionSupport.testConsumer(0, 5311711819224332248L, TimeTrackingPolicy.TRACK_TIME, (c) -> {
			/*
			 * Note : it is not possible to force the PersonDataManager to
			 * release such an event, so we release it from an actor
			 */
			PersonConstructionData personConstructionData = PersonConstructionData.builder().add(TestRegionId.REGION_1).build();
			PersonCreationObservationEvent personCreationObservationEvent = new PersonCreationObservationEvent(new PersonId(10000), personConstructionData);
			ContractException contractException = assertThrows(ContractException.class, () -> c.releaseEvent(personCreationObservationEvent));
			assertEquals(PersonError.UNKNOWN_PERSON_ID, contractException.getErrorType());
		});

		// precondition check: if the person was previously added
		RegionsActionSupport.testConsumer(0, 5824136557013438265L, TimeTrackingPolicy.TRACK_TIME, (c) -> {
			/*
			 * Note : it is not possible to force the PersonDataManager to
			 * release such an event, so we release it from an actor
			 */
			PersonDataManager personDataManager = c.getDataManager(PersonDataManager.class).get();
			PersonConstructionData personConstructionData = PersonConstructionData.builder().add(TestRegionId.REGION_1).build();
			PersonId personId = personDataManager.addPerson(personConstructionData);

			PersonCreationObservationEvent personCreationObservationEvent = new PersonCreationObservationEvent(personId, personConstructionData);

			ContractException contractException = assertThrows(ContractException.class, () -> c.releaseEvent(personCreationObservationEvent));
			assertEquals(RegionError.DUPLICATE_PERSON_ADDITION, contractException.getErrorType());
		});

	}

	@Test
	@UnitTestMethod(name = "init", args = {})
	public void testBulkPersonCreationObservationEvent() {

		/*
		 * Have the agent create some people over time and show that each person
		 * is in the correct region at the correct time
		 */
		RegionsActionSupport.testConsumer(0, 2654453328570666100L, TimeTrackingPolicy.TRACK_TIME, (c) -> {
			StochasticsDataManager stochasticsDataManager = c.getDataManager(StochasticsDataManager.class).get();
			RandomGenerator randomGenerator = stochasticsDataManager.getRandomGenerator();

			for (int i = 0; i < 100; i++) {
				c.addPlan((c2) -> {
					StochasticsDataManager stochasticsDataManager2 = c2.getDataManager(StochasticsDataManager.class).get();
					RegionDataManager regionDataManager = c2.getDataManager(RegionDataManager.class).get();
					PersonDataManager personDataManager = c2.getDataManager(PersonDataManager.class).get();

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
					PersonId personId = personDataManager.addBulkPeople(bulkPersonConstructionData).get();

					int basePersonIndex = personId.getValue();

					/*
					 * Show that each person is in the correct region with the
					 * correct region arrival time
					 */
					for (int j = 0; j < personCount; j++) {
						personId = new PersonId(j + basePersonIndex);
						RegionId personRegionId = regionDataManager.getPersonRegion(personId);
						RegionId randomRegionId = expectedRegions.get(j);
						assertEquals(randomRegionId, personRegionId);
						assertEquals(c2.getTime(), regionDataManager.getPersonRegionArrivalTime(personId));
					}

				}, randomGenerator.nextDouble() * 1000);
			}

		});

		// precondition check:
		// precondition check: if no region data was included in the event
		RegionsActionSupport.testConsumer(0, 7059505726403414171L, TimeTrackingPolicy.TRACK_TIME, (c) -> {
			PersonDataManager personDataManager = c.getDataManager(PersonDataManager.class).get();
			PersonConstructionData personConstructionData = PersonConstructionData.builder().build();
			BulkPersonConstructionData bulkPersonConstructionData = BulkPersonConstructionData.builder().add(personConstructionData).build();
			ContractException contractException = assertThrows(ContractException.class, () -> personDataManager.addBulkPeople(bulkPersonConstructionData));
			assertEquals(RegionError.NULL_REGION_ID, contractException.getErrorType());
		});

		// precondition check: if the region in the event is unknown
		RegionsActionSupport.testConsumer(0, 5120242925932651968L, TimeTrackingPolicy.TRACK_TIME, (c) -> {
			PersonDataManager personDataManager = c.getDataManager(PersonDataManager.class).get();
			PersonConstructionData personConstructionData = PersonConstructionData.builder().add(TestRegionId.getUnknownRegionId()).build();
			BulkPersonConstructionData bulkPersonConstructionData = BulkPersonConstructionData.builder().add(personConstructionData).build();
			ContractException contractException = assertThrows(ContractException.class, () -> personDataManager.addBulkPeople(bulkPersonConstructionData));
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
			BulkPersonCreationObservationEvent bulkPersonCreationObservationEvent = new BulkPersonCreationObservationEvent(new PersonId(45), bulkPersonConstructionData);
			ContractException contractException = assertThrows(ContractException.class, () -> c.releaseEvent(bulkPersonCreationObservationEvent));
			assertEquals(PersonError.UNKNOWN_PERSON_ID, contractException.getErrorType());
		});

		// precondition check: if the person was previously added
		RegionsActionSupport.testConsumer(0, 8092390328840929050L, TimeTrackingPolicy.TRACK_TIME, (c) -> {
			/*
			 * Note : it is not possible to force the PersonDataManager to
			 * release such an event, so we release it from an actor
			 */
			PersonDataManager personDataManager = c.getDataManager(PersonDataManager.class).get();
			PersonConstructionData personConstructionData = PersonConstructionData.builder().add(TestRegionId.REGION_1).build();
			PersonId personId = personDataManager.addPerson(personConstructionData);

			BulkPersonConstructionData bulkPersonConstructionData = BulkPersonConstructionData.builder().add(personConstructionData).build();
			BulkPersonCreationObservationEvent bulkPersonCreationObservationEvent = new BulkPersonCreationObservationEvent(personId, bulkPersonConstructionData);

			ContractException contractException = assertThrows(ContractException.class, () -> c.releaseEvent(bulkPersonCreationObservationEvent));
			assertEquals(RegionError.DUPLICATE_PERSON_ADDITION, contractException.getErrorType());
		});
	}

	@Test
	@UnitTestMethod(name = "init", args = {})
	public void testPersonImminentRemovalObservationEvent() {

		MutableInteger pId = new MutableInteger();

		TestPluginData.Builder pluginBuilder = TestPluginData.builder();
		/*
		 * Have the actor add a person and then remove it. There will be a delay
		 * of 0 time for the person to be removed.
		 */
		pluginBuilder.addTestActorPlan("actor", new TestActorPlan(2, (c) -> {
			PersonDataManager personDataManager = c.getDataManager(PersonDataManager.class).get();
			RegionDataManager regionDataManager = c.getDataManager(RegionDataManager.class).get();

			PersonConstructionData personConstructionData = PersonConstructionData.builder().add(TestRegionId.REGION_1).build();
			PersonId personId = personDataManager.addPerson(personConstructionData);
			pId.setValue(personId.getValue());

			int regionPopulationCount = regionDataManager.getRegionPopulationCount(TestRegionId.REGION_1);
			assertEquals(1, regionPopulationCount);
			assertEquals(TestRegionId.REGION_1, regionDataManager.getPersonRegion(personId));
			personDataManager.removePerson(personId);

		}));

		/*
		 * Have the actor show that the person is no longer in the location data
		 * view
		 * 
		 */
		pluginBuilder.addTestActorPlan("actor", new TestActorPlan(3, (c) -> {
			RegionDataManager regionDataManager = c.getDataManager(RegionDataManager.class).get();
			PersonId personId = new PersonId(pId.getValue());

			int regionPopulationCount = regionDataManager.getRegionPopulationCount(TestRegionId.REGION_1);
			assertEquals(0, regionPopulationCount);
			ContractException contractException = assertThrows(ContractException.class, () -> regionDataManager.getPersonRegion(personId));
			assertEquals(PersonError.UNKNOWN_PERSON_ID, contractException.getErrorType());

		}));

		// build action plugin
		TestPluginData testPluginData = pluginBuilder.build();
		Plugin testPlugin = TestPlugin.getTestPlugin(testPluginData);
		RegionsActionSupport.testConsumers(0, 163202760371564041L, TimeTrackingPolicy.TRACK_TIME, testPlugin);

		// precondition test: if the person id is unknown
		RegionsActionSupport.testConsumer(0, 2314376445339268382L, TimeTrackingPolicy.TRACK_TIME, (c) -> {
			PersonImminentRemovalObservationEvent personImminentRemovalObservationEvent = new PersonImminentRemovalObservationEvent(new PersonId(1000));
			ContractException contractException = assertThrows(ContractException.class, () -> c.releaseEvent(personImminentRemovalObservationEvent));
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
				PersonDataManager personDataManager = c.getDataManager(PersonDataManager.class).get();
				PersonId personId = personDataManager.addPerson(PersonConstructionData.builder().add(TestRegionId.REGION_1).build());
				personDataManager.removePerson(personId);
				PersonImminentRemovalObservationEvent personImminentRemovalObservationEvent = new PersonImminentRemovalObservationEvent(personId);
				c.releaseEvent(personImminentRemovalObservationEvent);
			});
		});
		assertEquals(RegionError.DUPLICATE_PERSON_REMOVAL, contractException.getErrorType());

	}

}
