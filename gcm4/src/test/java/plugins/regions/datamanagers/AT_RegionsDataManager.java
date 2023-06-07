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
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;

import org.apache.commons.math3.random.RandomGenerator;
import org.apache.commons.math3.util.FastMath;
import org.apache.commons.math3.util.Pair;
import org.junit.jupiter.api.Test;

import nucleus.ActorContext;
import nucleus.DataManagerContext;
import nucleus.Event;
import nucleus.EventFilter;
import nucleus.Plugin;
import nucleus.Simulation;
import nucleus.SimulationState;
import nucleus.testsupport.runcontinuityplugin.RunContinuityPlugin;
import nucleus.testsupport.runcontinuityplugin.RunContinuityPluginData;
import nucleus.testsupport.testplugin.TestActorPlan;
import nucleus.testsupport.testplugin.TestDataManager;
import nucleus.testsupport.testplugin.TestDataManagerPlan;
import nucleus.testsupport.testplugin.TestOutputConsumer;
import nucleus.testsupport.testplugin.TestPlugin;
import nucleus.testsupport.testplugin.TestPluginData;
import nucleus.testsupport.testplugin.TestSimulation;
import plugins.people.PeoplePlugin;
import plugins.people.PeoplePluginData;
import plugins.people.PeoplePluginId;
import plugins.people.datamanagers.PeopleDataManager;
import plugins.people.events.PersonImminentAdditionEvent;
import plugins.people.events.PersonRemovalEvent;
import plugins.people.support.PersonConstructionData;
import plugins.people.support.PersonError;
import plugins.people.support.PersonId;
import plugins.people.support.PersonRange;
import plugins.regions.RegionsPlugin;
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
import plugins.regions.testsupport.RegionsTestPluginFactory.Factory;
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
		Factory factory = RegionsTestPluginFactory.factory(20, 3161087621160007875L, TimeTrackingPolicy.TRACK_TIME, (c) -> {
			// show that a negative growth causes an exception
			RegionsDataManager regionsDataManager = c.getDataManager(RegionsDataManager.class);
			ContractException contractException = assertThrows(ContractException.class, () -> regionsDataManager.expandCapacity(-1));
			assertEquals(PersonError.NEGATIVE_GROWTH_PROJECTION, contractException.getErrorType());
		});
		TestSimulation.builder().addPlugins(factory.getPlugins()).build().execute();

		// use manual tests for non-negative growth
	}

	@Test
	@UnitTestConstructor(target = RegionsDataManager.class, args = { RegionsPluginData.class })
	public void testConstructor() {
		// this test is covered by the remaining tests
		ContractException contractException = assertThrows(ContractException.class, () -> new RegionsDataManager(null));
		assertEquals(RegionError.NULL_REGION_PLUGIN_DATA, contractException.getErrorType());
	}

	/**
	 * Demonstrates that the data manager produces plugin data that reflects its
	 * final state
	 */
	@Test
	@UnitTestMethod(target = RegionsDataManager.class, name = "init", args = { DataManagerContext.class })
	public void testStateFinalization() {

		RegionsPluginData regionsPluginData = RegionsPluginData.builder().build();
		TestPluginData.Builder pluginBuilder = TestPluginData.builder();
		RegionPropertyDefinitionInitialization regionPropertyDefinitionInitialization = RegionPropertyDefinitionInitialization.builder().setRegionPropertyId(
				TestRegionPropertyId.REGION_PROPERTY_1_BOOLEAN_MUTABLE).setPropertyDefinition(TestRegionPropertyId.REGION_PROPERTY_1_BOOLEAN_MUTABLE.getPropertyDefinition()).build();

		pluginBuilder.addTestActorPlan("actor", new TestActorPlan(0, (c) -> {
			RegionsDataManager regionsDataManager = c.getDataManager(RegionsDataManager.class);
			regionsDataManager.defineRegionProperty(regionPropertyDefinitionInitialization);
			RegionId regionId = TestRegionId.REGION_1;
			RegionConstructionData regionConstructionData = RegionConstructionData	.builder().setRegionPropertyValue(regionPropertyDefinitionInitialization.getRegionPropertyId(), true)
																					.setRegionId(regionId).build();
			regionsDataManager.addRegion(regionConstructionData);
			regionsDataManager.setRegionPropertyValue(regionId, regionPropertyDefinitionInitialization.getRegionPropertyId(), true);
		}));

		TestPluginData testPluginData = pluginBuilder.build();
		Factory factory = RegionsTestPluginFactory	.factory(0, 3347423560010833899L, TimeTrackingPolicy.TRACK_TIME, testPluginData)//
													.setRegionsPluginData(regionsPluginData);

		TestOutputConsumer testOutputConsumer = TestSimulation	.builder()//
																.addPlugins(factory.getPlugins())//
																.setProduceSimulationStateOnHalt(true)//
																.setSimulationHaltTime(2).build()//
																.execute();
		Map<RegionsPluginData, Integer> outputItems = testOutputConsumer.getOutputItemMap(RegionsPluginData.class);
		assertEquals(1, outputItems.size());

		RegionsPluginData expectedPluginData = RegionsPluginData.builder().addRegion(TestRegionId.REGION_1)
																.defineRegionProperty(regionPropertyDefinitionInitialization.getRegionPropertyId(),
																		regionPropertyDefinitionInitialization.getPropertyDefinition())
																.setRegionPropertyValue(TestRegionId.REGION_1, regionPropertyDefinitionInitialization.getRegionPropertyId(), true).build();
		RegionsPluginData actualPluginData = outputItems.keySet().iterator().next();
		assertEquals(expectedPluginData, actualPluginData);

		//
		regionsPluginData = RegionsPluginData.builder().build();
		pluginBuilder = TestPluginData.builder();
		RegionPropertyDefinitionInitialization regionPropertyDefinitionInitialization2 = RegionPropertyDefinitionInitialization.builder().setRegionPropertyId(
				TestRegionPropertyId.REGION_PROPERTY_2_INTEGER_MUTABLE).setPropertyDefinition(TestRegionPropertyId.REGION_PROPERTY_2_INTEGER_MUTABLE.getPropertyDefinition()).build();
		RegionPropertyDefinitionInitialization regionPropertyDefinitionInitialization3 = RegionPropertyDefinitionInitialization.builder().setRegionPropertyId(
				TestRegionPropertyId.REGION_PROPERTY_3_DOUBLE_MUTABLE).setPropertyDefinition(TestRegionPropertyId.REGION_PROPERTY_3_DOUBLE_MUTABLE.getPropertyDefinition()).build();

		pluginBuilder.addTestActorPlan("actor", new TestActorPlan(0, (c) -> {
			RegionsDataManager regionsDataManager = c.getDataManager(RegionsDataManager.class);
			regionsDataManager.defineRegionProperty(regionPropertyDefinitionInitialization2);
			regionsDataManager.defineRegionProperty(regionPropertyDefinitionInitialization3);
			RegionId regionId2 = TestRegionId.REGION_2;
			RegionId regionId3 = TestRegionId.REGION_3;
			RegionConstructionData regionConstructionData2 = RegionConstructionData	.builder().setRegionPropertyValue(regionPropertyDefinitionInitialization2.getRegionPropertyId(), 15)
																					.setRegionId(regionId2).build();
			RegionConstructionData regionConstructionData3 = RegionConstructionData	.builder().setRegionPropertyValue(regionPropertyDefinitionInitialization2.getRegionPropertyId(), 67)
																					.setRegionId(regionId3).build();
			regionsDataManager.addRegion(regionConstructionData2);
			regionsDataManager.addRegion(regionConstructionData3);
			regionsDataManager.setRegionPropertyValue(regionId2, regionPropertyDefinitionInitialization2.getRegionPropertyId(), 15);
			regionsDataManager.setRegionPropertyValue(regionId3, regionPropertyDefinitionInitialization3.getRegionPropertyId(), 67.9);
		}));

		pluginBuilder.addTestActorPlan("actor", new TestActorPlan(1, (c) -> {
			RegionsDataManager regionsDataManager = c.getDataManager(RegionsDataManager.class);
			RegionId regionId2 = TestRegionId.REGION_2;
			RegionId regionId3 = TestRegionId.REGION_3;
			regionsDataManager.setRegionPropertyValue(regionId2, regionPropertyDefinitionInitialization2.getRegionPropertyId(), 92);
			regionsDataManager.setRegionPropertyValue(regionId2, regionPropertyDefinitionInitialization2.getRegionPropertyId(), 5);
			regionsDataManager.setRegionPropertyValue(regionId3, regionPropertyDefinitionInitialization3.getRegionPropertyId(), 82.5);
			regionsDataManager.setRegionPropertyValue(regionId3, regionPropertyDefinitionInitialization3.getRegionPropertyId(), 123.5);
		}));

		testPluginData = pluginBuilder.build();
		factory = RegionsTestPluginFactory.factory(0, 3347423560010833899L, TimeTrackingPolicy.TRACK_TIME, testPluginData).setRegionsPluginData(regionsPluginData);
		testOutputConsumer = TestSimulation.builder().addPlugins(factory.getPlugins()).setProduceSimulationStateOnHalt(true).setSimulationHaltTime(2).build().execute();
		outputItems = testOutputConsumer.getOutputItemMap(RegionsPluginData.class);
		assertEquals(1, outputItems.size());
		expectedPluginData = RegionsPluginData	.builder().addRegion(TestRegionId.REGION_2).addRegion(TestRegionId.REGION_3)
												.defineRegionProperty(regionPropertyDefinitionInitialization2.getRegionPropertyId(), regionPropertyDefinitionInitialization2.getPropertyDefinition())
												.defineRegionProperty(regionPropertyDefinitionInitialization3.getRegionPropertyId(), regionPropertyDefinitionInitialization3.getPropertyDefinition())
												.setRegionPropertyValue(TestRegionId.REGION_2, regionPropertyDefinitionInitialization2.getRegionPropertyId(), 5)
												.setRegionPropertyValue(TestRegionId.REGION_3, regionPropertyDefinitionInitialization2.getRegionPropertyId(), 67)
												.setRegionPropertyValue(TestRegionId.REGION_2, regionPropertyDefinitionInitialization3.getRegionPropertyId(), 0.0)
												.setRegionPropertyValue(TestRegionId.REGION_3, regionPropertyDefinitionInitialization3.getRegionPropertyId(), 123.5)

												.build();
		actualPluginData = outputItems.keySet().iterator().next();
		assertEquals(expectedPluginData, actualPluginData);
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
		Factory factory = RegionsTestPluginFactory.factory(0, 3347423560010833899L, TimeTrackingPolicy.TRACK_TIME, testPluginData);
		TestSimulation.builder().addPlugins(factory.getPlugins()).build().execute();

		// precondition test: if the region id is null
		ContractException contractException = assertThrows(ContractException.class, () -> {
			Factory factory2 = RegionsTestPluginFactory.factory(0, 9052181434511982170L, TimeTrackingPolicy.TRACK_TIME, (c) -> {
				RegionsDataManager regionsDataManager = c.getDataManager(RegionsDataManager.class);
				regionsDataManager.getPeopleInRegion(null);
			});
			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();
		});
		assertEquals(RegionError.NULL_REGION_ID, contractException.getErrorType());

		// precondition test: if the region id is unknown
		contractException = assertThrows(ContractException.class, () -> {
			Factory factory2 = RegionsTestPluginFactory.factory(0, 1410190102298165957L, TimeTrackingPolicy.TRACK_TIME, (c) -> {
				RegionsDataManager regionsDataManager = c.getDataManager(RegionsDataManager.class);
				// if the region id is unknown
				regionsDataManager.getPeopleInRegion(TestRegionId.getUnknownRegionId());
			});
			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();
		});
		assertEquals(RegionError.UNKNOWN_REGION_ID, contractException.getErrorType());

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
		Factory factory = RegionsTestPluginFactory.factory(0, 5151111920517015649L, TimeTrackingPolicy.TRACK_TIME, testPluginData);
		TestSimulation.builder().addPlugins(factory.getPlugins()).build().execute();

		// precondition test: if the person id is null
		ContractException contractException = assertThrows(ContractException.class, () -> {
			Factory factory2 = RegionsTestPluginFactory.factory(0, 1490027040692903854L, TimeTrackingPolicy.TRACK_TIME, (c) -> {
				RegionsDataManager regionsDataManager = c.getDataManager(RegionsDataManager.class);
				regionsDataManager.getPersonRegion(null);
			});
			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();
		});
		assertEquals(PersonError.NULL_PERSON_ID, contractException.getErrorType());

		// precondition test: if the person id is unknown
		contractException = assertThrows(ContractException.class, () -> {
			Factory factory2 = RegionsTestPluginFactory.factory(0, 2144445839100475443L, TimeTrackingPolicy.TRACK_TIME, (c) -> {
				RegionsDataManager regionsDataManager = c.getDataManager(RegionsDataManager.class);
				regionsDataManager.getPersonRegion(new PersonId(100000));
			});
			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();
		});
		assertEquals(PersonError.UNKNOWN_PERSON_ID, contractException.getErrorType());
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
		Factory factory = RegionsTestPluginFactory.factory(0, 2278422620232176214L, TimeTrackingPolicy.TRACK_TIME, testPluginData);
		TestSimulation.builder().addPlugins(factory.getPlugins()).build().execute();

		// precondition test: if region arrival times are not being tracked
		pluginBuilder = TestPluginData.builder();
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
		factory = RegionsTestPluginFactory.factory(0, 9214210856215652451L, TimeTrackingPolicy.TRACK_TIME, testPluginData);
		TestSimulation.builder().addPlugins(factory.getPlugins()).build().execute();

		// precondition test: if region arrival times are not being tracked
		ContractException contractException = assertThrows(ContractException.class, () -> {
			Factory factory2 = RegionsTestPluginFactory.factory(10, 1906010286127446114L, TimeTrackingPolicy.DO_NOT_TRACK_TIME, (c) -> {
				RegionsDataManager regionsDataManager = c.getDataManager(RegionsDataManager.class);
				// if region arrival times are not being tracked
				regionsDataManager.getPersonRegionArrivalTime(new PersonId(0));
			});
			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();
		});
		assertEquals(RegionError.REGION_ARRIVAL_TIMES_NOT_TRACKED, contractException.getErrorType());

		// precondition test: if the person id is null
		contractException = assertThrows(ContractException.class, () -> {
			Factory factory2 = RegionsTestPluginFactory.factory(0, 2922597221561284586L, TimeTrackingPolicy.TRACK_TIME, (c) -> {
				RegionsDataManager regionsDataManager = c.getDataManager(RegionsDataManager.class);
				regionsDataManager.getPersonRegionArrivalTime(null);
			});
			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();
		});
		assertEquals(PersonError.NULL_PERSON_ID, contractException.getErrorType());

		// precondition test: if the person id is unknown
		contractException = assertThrows(ContractException.class, () -> {
			Factory factory2 = RegionsTestPluginFactory.factory(0, 9132391945335483479L, TimeTrackingPolicy.TRACK_TIME, (c) -> {
				RegionsDataManager regionsDataManager = c.getDataManager(RegionsDataManager.class);
				regionsDataManager.getPersonRegionArrivalTime(new PersonId(100000));
			});
			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();
		});
		assertEquals(PersonError.UNKNOWN_PERSON_ID, contractException.getErrorType());

	}

	@Test
	@UnitTestMethod(target = RegionsDataManager.class, name = "getPersonRegionArrivalTrackingPolicy", args = {})
	public void testGetPersonRegionArrivalTrackingPolicy() {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(7220786446142555493L);
		for (TimeTrackingPolicy timeTrackingPolicy : TimeTrackingPolicy.values()) {
			Factory factory = RegionsTestPluginFactory.factory(0, randomGenerator.nextLong(), timeTrackingPolicy, (c) -> {
				RegionsDataManager regionsDataManager = c.getDataManager(RegionsDataManager.class);
				assertEquals(timeTrackingPolicy == TimeTrackingPolicy.TRACK_TIME, regionsDataManager.regionArrivalsAreTracked());
			});
			TestSimulation.builder().addPlugins(factory.getPlugins()).build().execute();
		}
	}

	@Test
	@UnitTestMethod(target = RegionsDataManager.class, name = "getRegionIds", args = {})
	public void testGetRegionIds() {
		Factory factory = RegionsTestPluginFactory.factory(0, 87615823520161580L, TimeTrackingPolicy.TRACK_TIME, (c) -> {
			RegionsDataManager regionsDataManager = c.getDataManager(RegionsDataManager.class);

			Set<RegionId> expectedRegionIds = new LinkedHashSet<>();
			for (TestRegionId testRegionId : TestRegionId.values()) {
				expectedRegionIds.add(testRegionId);
			}
			assertEquals(expectedRegionIds, regionsDataManager.getRegionIds());
		});
		TestSimulation.builder().addPlugins(factory.getPlugins()).build().execute();
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
		Factory factory = RegionsTestPluginFactory.factory(0, 1525815460460902517L, TimeTrackingPolicy.TRACK_TIME, testPluginData);
		TestSimulation.builder().addPlugins(factory.getPlugins()).build().execute();

	}

	@Test
	@UnitTestMethod(target = RegionsDataManager.class, name = "getRegionPropertyDefinition", args = { RegionPropertyId.class })
	public void testGetRegionPropertyDefinition() {
		Factory factory = RegionsTestPluginFactory.factory(0, 8915683065425449883L, TimeTrackingPolicy.TRACK_TIME, (c) -> {
			RegionsDataManager regionsDataManager = c.getDataManager(RegionsDataManager.class);
			Set<TestRegionPropertyId> regionPropertyIds = regionsDataManager.getRegionPropertyIds();
			assertEquals(TestRegionPropertyId.size(), regionPropertyIds.size());
			for (TestRegionPropertyId testRegionPropertyId : regionPropertyIds) {
				PropertyDefinition expectedPropertyDefinition = testRegionPropertyId.getPropertyDefinition();
				PropertyDefinition actualPropertyDefinition = regionsDataManager.getRegionPropertyDefinition(testRegionPropertyId);
				assertEquals(expectedPropertyDefinition, actualPropertyDefinition);
			}
		});
		TestSimulation.builder().addPlugins(factory.getPlugins()).build().execute();

		// precondition check: if the region property id is null
		ContractException contractException = assertThrows(ContractException.class, () -> {
			Factory factory2 = RegionsTestPluginFactory.factory(0, 4217775232224320101L, TimeTrackingPolicy.TRACK_TIME, (c) -> {
				RegionsDataManager regionsDataManager = c.getDataManager(RegionsDataManager.class);
				regionsDataManager.getRegionPropertyDefinition(null);
			});
			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();
		});
		assertEquals(PropertyError.NULL_PROPERTY_ID, contractException.getErrorType());

		// precondition check: if the region property id is unknown
		contractException = assertThrows(ContractException.class, () -> {
			Factory factory2 = RegionsTestPluginFactory.factory(0, 1425794836864585647L, TimeTrackingPolicy.TRACK_TIME, (c) -> {
				RegionsDataManager regionsDataManager = c.getDataManager(RegionsDataManager.class);
				regionsDataManager.getRegionPropertyDefinition(TestRegionPropertyId.getUnknownRegionPropertyId());
			});
			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();
		});
		assertEquals(PropertyError.UNKNOWN_PROPERTY_ID, contractException.getErrorType());
	}

	@Test
	@UnitTestMethod(target = RegionsDataManager.class, name = "getRegionPropertyIds", args = {})
	public void testGetRegionPropertyIds() {
		Factory factory = RegionsTestPluginFactory.factory(0, 2658585233315606268L, TimeTrackingPolicy.TRACK_TIME, (c) -> {
			RegionsDataManager regionsDataManager = c.getDataManager(RegionsDataManager.class);
			Set<RegionPropertyId> expectedRegionPropertyIds = new LinkedHashSet<>();
			for (TestRegionPropertyId testRegionPropertyId : TestRegionPropertyId.values()) {
				expectedRegionPropertyIds.add(testRegionPropertyId);
			}
			assertEquals(expectedRegionPropertyIds, regionsDataManager.getRegionPropertyIds());
		});
		TestSimulation.builder().addPlugins(factory.getPlugins()).build().execute();
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
		Factory factory = RegionsTestPluginFactory.factory(0, 8784099691519492811L, TimeTrackingPolicy.TRACK_TIME, testPluginData);
		TestSimulation.builder().addPlugins(factory.getPlugins()).build().execute();

		// precondition check: if the region id is null
		ContractException contractException = assertThrows(ContractException.class, () -> {
			Factory factory2 = RegionsTestPluginFactory.factory(0, 468427930601885944L, TimeTrackingPolicy.TRACK_TIME, (c) -> {
				RegionsDataManager regionsDataManager = c.getDataManager(RegionsDataManager.class);
				RegionPropertyId knownRegionPropertyId = TestRegionPropertyId.REGION_PROPERTY_1_BOOLEAN_MUTABLE;
				regionsDataManager.getRegionPropertyValue(null, knownRegionPropertyId);
			});
			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();
		});
		assertEquals(RegionError.NULL_REGION_ID, contractException.getErrorType());

		// precondition check: if the region id is not known
		contractException = assertThrows(ContractException.class, () -> {
			Factory factory2 = RegionsTestPluginFactory.factory(0, 6075787443228538245L, TimeTrackingPolicy.TRACK_TIME, (c) -> {
				RegionsDataManager regionsDataManager = c.getDataManager(RegionsDataManager.class);
				RegionId unknownRegionId = TestRegionId.getUnknownRegionId();
				RegionPropertyId knownRegionPropertyId = TestRegionPropertyId.REGION_PROPERTY_1_BOOLEAN_MUTABLE;
				regionsDataManager.getRegionPropertyValue(unknownRegionId, knownRegionPropertyId);
			});
			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();
		});
		assertEquals(RegionError.UNKNOWN_REGION_ID, contractException.getErrorType());

		// precondition check: if the region property id is null
		contractException = assertThrows(ContractException.class, () -> {
			Factory factory2 = RegionsTestPluginFactory.factory(0, 2997323471294141386L, TimeTrackingPolicy.TRACK_TIME, (c) -> {
				RegionsDataManager regionsDataManager = c.getDataManager(RegionsDataManager.class);
				RegionId knownRegionId = TestRegionId.REGION_1;
				regionsDataManager.getRegionPropertyValue(knownRegionId, null);
			});
			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();
		});
		assertEquals(PropertyError.NULL_PROPERTY_ID, contractException.getErrorType());

		// precondition check: if the region property id is unknown
		contractException = assertThrows(ContractException.class, () -> {
			Factory factory2 = RegionsTestPluginFactory.factory(0, 7980671049474262492L, TimeTrackingPolicy.TRACK_TIME, (c) -> {
				RegionsDataManager regionsDataManager = c.getDataManager(RegionsDataManager.class);
				RegionId knownRegionId = TestRegionId.REGION_1;
				RegionPropertyId unknownRegionPropertyId = TestRegionPropertyId.getUnknownRegionPropertyId();
				regionsDataManager.getRegionPropertyValue(knownRegionId, unknownRegionPropertyId);
			});
			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();
		});
		assertEquals(PropertyError.UNKNOWN_PROPERTY_ID, contractException.getErrorType());
	}

	@Test
	@UnitTestMethod(target = RegionsDataManager.class, name = "regionIdExists", args = { RegionId.class })
	public void testRegionIdExists() {
		Factory factory = RegionsTestPluginFactory.factory(0, 8636579794186794067L, TimeTrackingPolicy.TRACK_TIME, (c) -> {
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
		TestSimulation.builder().addPlugins(factory.getPlugins()).build().execute();

	}

	@Test
	@UnitTestMethod(target = RegionsDataManager.class, name = "regionPropertyIdExists", args = { RegionPropertyId.class })
	public void testRegionPropertyIdExists() {
		Factory factory = RegionsTestPluginFactory.factory(0, 3797498566412748237L, TimeTrackingPolicy.TRACK_TIME, (c) -> {
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
		TestSimulation.builder().addPlugins(factory.getPlugins()).build().execute();

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
		Factory factory = RegionsTestPluginFactory.factory(numberOfPeople, 5655227215512656797L, TimeTrackingPolicy.TRACK_TIME, testPluginData);
		TestSimulation.builder().addPlugins(factory.getPlugins()).build().execute();

		// show that the observations were correct
		assertEquals(expectedObservations.size(), recievedObservations.size());
		assertEquals(new LinkedHashSet<>(expectedObservations), new LinkedHashSet<>(recievedObservations));

		/*
		 * precondition test: if the person id is null
		 */
		factory = RegionsTestPluginFactory.factory(numberOfPeople, 9048586333860290178L, TimeTrackingPolicy.TRACK_TIME, (c) -> {
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
		TestSimulation.builder().addPlugins(factory.getPlugins()).build().execute();

		/*
		 * precondition test: if the person id is unknown
		 */
		factory = RegionsTestPluginFactory.factory(numberOfPeople, 6693022571477538917L, TimeTrackingPolicy.TRACK_TIME, (c) -> {
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
		TestSimulation.builder().addPlugins(factory.getPlugins()).build().execute();

		/*
		 * precondition test: if the region id is null
		 */
		ContractException contractException = assertThrows(ContractException.class, () -> {
			Factory factory2 = RegionsTestPluginFactory.factory(numberOfPeople, 1385204599279421266L, TimeTrackingPolicy.TRACK_TIME, (c) -> {
				// Select a person at random from the simulation and create a
				// person
				// id outside of the simulation

				StochasticsDataManager stochasticsDataManager = c.getDataManager(StochasticsDataManager.class);
				RandomGenerator randomGenerator = stochasticsDataManager.getRandomGenerator();

				PeopleDataManager peopleDataManager = c.getDataManager(PeopleDataManager.class);
				List<PersonId> people = peopleDataManager.getPeople();
				PersonId personId = people.get(randomGenerator.nextInt(people.size()));

				// establish the person's current region and next region
				RegionsDataManager regionsDataManager = c.getDataManager(RegionsDataManager.class);

				//
				regionsDataManager.setPersonRegion(personId, null);
			});
			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();
		});
		assertEquals(RegionError.NULL_REGION_ID, contractException.getErrorType());

		/*
		 * precondition test: if the region id is unknown
		 */
		contractException = assertThrows(ContractException.class, () -> {
			Factory factory2 = RegionsTestPluginFactory.factory(numberOfPeople, 6025662871362676118L, TimeTrackingPolicy.TRACK_TIME, (c) -> {
				// Select a person at random from the simulation and create a
				// person
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

				regionsDataManager.setPersonRegion(personId, unknownRegionId);
			});
			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();
		});
		assertEquals(RegionError.UNKNOWN_REGION_ID, contractException.getErrorType());

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
					expectedObservations.add(new MultiKey(c2.getTime(), TestRegionId.REGION_2, TestRegionPropertyId.REGION_PROPERTY_3_DOUBLE_MUTABLE, newValue));
				}, randomGenerator.nextDouble() * 1000);
			}

		}));

		TestPluginData testPluginData = pluginBuilder.build();
		Factory factory = RegionsTestPluginFactory.factory(0, 4630021532130673951L, TimeTrackingPolicy.TRACK_TIME, testPluginData);
		TestSimulation.builder().addPlugins(factory.getPlugins()).build().execute();

		// show that the observed changes match expectations
		assertEquals(expectedObservations.size(), actualObservations.size());

		assertEquals(new LinkedHashSet<>(expectedObservations), new LinkedHashSet<>(actualObservations));

		// precondition check: if the region id is null
		ContractException contractException = assertThrows(ContractException.class, () -> {
			Factory factory2 = RegionsTestPluginFactory.factory(0, 7347707922069273812L, TimeTrackingPolicy.TRACK_TIME, (c) -> {
				RegionPropertyId regionPropertyId = TestRegionPropertyId.REGION_PROPERTY_2_INTEGER_MUTABLE;
				Object propertyValue = 67;
				RegionsDataManager regionsDataManager = c.getDataManager(RegionsDataManager.class);
				regionsDataManager.setRegionPropertyValue(null, regionPropertyId, propertyValue);
			});
			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();
		});
		assertEquals(RegionError.NULL_REGION_ID, contractException.getErrorType());

		// precondition check: if the region id is unknown
		contractException = assertThrows(ContractException.class, () -> {
			Factory factory2 = RegionsTestPluginFactory.factory(0, 3075330757105736185L, TimeTrackingPolicy.TRACK_TIME, (c) -> {
				RegionId unknownRegionId = TestRegionId.getUnknownRegionId();
				RegionPropertyId regionPropertyId = TestRegionPropertyId.REGION_PROPERTY_2_INTEGER_MUTABLE;
				Object propertyValue = 67;
				RegionsDataManager regionsDataManager = c.getDataManager(RegionsDataManager.class);
				regionsDataManager.setRegionPropertyValue(unknownRegionId, regionPropertyId, propertyValue);
			});
			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();
		});
		assertEquals(RegionError.UNKNOWN_REGION_ID, contractException.getErrorType());

		// precondition check: if the region property id is null
		contractException = assertThrows(ContractException.class, () -> {
			Factory factory2 = RegionsTestPluginFactory.factory(0, 4169934733913962790L, TimeTrackingPolicy.TRACK_TIME, (c) -> {
				RegionId regionId = TestRegionId.REGION_1;
				Object propertyValue = 67;
				RegionsDataManager regionsDataManager = c.getDataManager(RegionsDataManager.class);
				regionsDataManager.setRegionPropertyValue(regionId, null, propertyValue);
			});
			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();
		});
		assertEquals(PropertyError.NULL_PROPERTY_ID, contractException.getErrorType());

		// precondition check: if the region property id is unknown
		contractException = assertThrows(ContractException.class, () -> {
			Factory factory2 = RegionsTestPluginFactory.factory(0, 5578070775436119166L, TimeTrackingPolicy.TRACK_TIME, (c) -> {
				RegionId regionId = TestRegionId.REGION_1;
				RegionPropertyId unknownRegionPropertyId = TestRegionPropertyId.getUnknownRegionPropertyId();
				Object propertyValue = 67;
				RegionsDataManager regionsDataManager = c.getDataManager(RegionsDataManager.class);
				regionsDataManager.setRegionPropertyValue(regionId, unknownRegionPropertyId, propertyValue);
			});
			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();
		});
		assertEquals(PropertyError.UNKNOWN_PROPERTY_ID, contractException.getErrorType());

		// precondition check: if the region property value is null
		contractException = assertThrows(ContractException.class, () -> {
			Factory factory2 = RegionsTestPluginFactory.factory(0, 217279748753596418L, TimeTrackingPolicy.TRACK_TIME, (c) -> {
				RegionId regionId = TestRegionId.REGION_1;
				RegionPropertyId regionPropertyId = TestRegionPropertyId.REGION_PROPERTY_2_INTEGER_MUTABLE;
				RegionsDataManager regionsDataManager = c.getDataManager(RegionsDataManager.class);
				regionsDataManager.setRegionPropertyValue(regionId, regionPropertyId, null);
			});
			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();
		});
		assertEquals(PropertyError.NULL_PROPERTY_VALUE, contractException.getErrorType());

		// precondition check: if the region property value is incompatible with
		// the defined type for the property
		contractException = assertThrows(ContractException.class, () -> {
			Factory factory2 = RegionsTestPluginFactory.factory(0, 7043526072670323223L, TimeTrackingPolicy.TRACK_TIME, (c) -> {
				RegionId regionId = TestRegionId.REGION_1;
				RegionPropertyId regionPropertyId = TestRegionPropertyId.REGION_PROPERTY_2_INTEGER_MUTABLE;
				Object incompatiblePropertyValue = "incompatible value";
				RegionsDataManager regionsDataManager = c.getDataManager(RegionsDataManager.class);
				regionsDataManager.setRegionPropertyValue(regionId, regionPropertyId, incompatiblePropertyValue);
			});
			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();
		});
		assertEquals(PropertyError.INCOMPATIBLE_VALUE, contractException.getErrorType());

		// precondition check: if the region id is unknown
		contractException = assertThrows(ContractException.class, () -> {
			Factory factory2 = RegionsTestPluginFactory.factory(0, 8501593854721316109L, TimeTrackingPolicy.TRACK_TIME, (c) -> {
				RegionId unknownRegionId = TestRegionId.getUnknownRegionId();
				RegionPropertyId regionPropertyId = TestRegionPropertyId.REGION_PROPERTY_2_INTEGER_MUTABLE;
				Object propertyValue = 67;
				RegionsDataManager regionsDataManager = c.getDataManager(RegionsDataManager.class);
				regionsDataManager.setRegionPropertyValue(unknownRegionId, regionPropertyId, propertyValue);
			});
			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();
		});
		assertEquals(RegionError.UNKNOWN_REGION_ID, contractException.getErrorType());

		// precondition check: if the region property value is null
		contractException = assertThrows(ContractException.class, () -> {
			Factory factory2 = RegionsTestPluginFactory.factory(0, 6977487076968608944L, TimeTrackingPolicy.TRACK_TIME, (c) -> {
				RegionId regionId = TestRegionId.REGION_1;
				RegionPropertyId immutableRegionPropertyId = TestRegionPropertyId.REGION_PROPERTY_5_INTEGER_IMMUTABLE;
				RegionsDataManager regionsDataManager = c.getDataManager(RegionsDataManager.class);
				regionsDataManager.setRegionPropertyValue(regionId, immutableRegionPropertyId, null);
			});
			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();
		});
		assertEquals(PropertyError.NULL_PROPERTY_VALUE, contractException.getErrorType());

	}

	/*
	 * Returns a region plugin data with 1) a randomized tracking policy, 2) a
	 * randomized set of region properties, 3) randomized set of regions and 4)
	 * a randomized set of person region assignments
	 */
	private static RegionsPluginData getRandomizedRegionsPluginData(int populationSize, RandomGenerator randomGenerator) {

		RegionsPluginData.Builder regionPluginBuilder = RegionsPluginData.builder();

		// pick about half of the test regions, with at least one selected
		boolean firstRegionAdded = false;
		List<TestRegionId> selectedRegions = new ArrayList<>();
		for (TestRegionId regionId : TestRegionId.values()) {
			if (firstRegionAdded) {
				if (randomGenerator.nextBoolean()) {
					selectedRegions.add(regionId);

				}
			} else {
				firstRegionAdded = true;
				selectedRegions.add(regionId);

			}
		}
		for (TestRegionId regionId : selectedRegions) {
			regionPluginBuilder.addRegion(regionId);
		}

		// pick about half of the properties and assign non-default values to
		// about half of the regions
		for (TestRegionPropertyId testRegionPropertyId : TestRegionPropertyId.values()) {
			if (randomGenerator.nextBoolean()) {
				PropertyDefinition propertyDefinition = testRegionPropertyId.getPropertyDefinition();
				regionPluginBuilder.defineRegionProperty(testRegionPropertyId, propertyDefinition);
				boolean noDefaultValuePresent = propertyDefinition.getDefaultValue().isEmpty();
				for (TestRegionId regionId : selectedRegions) {
					if (noDefaultValuePresent || randomGenerator.nextBoolean()) {
						regionPluginBuilder.setRegionPropertyValue(regionId, testRegionPropertyId, testRegionPropertyId.getRandomPropertyValue(randomGenerator));
					}
				}
			}
		}
		boolean trackTimes = randomGenerator.nextBoolean();
		regionPluginBuilder.setPersonRegionArrivalTracking(trackTimes);

		for (int i = 0; i < populationSize; i++) {
			PersonId personId = new PersonId(i);
			RegionId regionId = selectedRegions.get(randomGenerator.nextInt(selectedRegions.size()));
			if (trackTimes) {
				regionPluginBuilder.addPerson(personId, regionId, 0.0);
			} else {
				regionPluginBuilder.addPerson(personId, regionId);
			}
		}

		return regionPluginBuilder.build();
	}

	/**
	 * Demonstrates that the data manager's initial state reflects its plugin
	 * data
	 */
	@Test
	@UnitTestMethod(target = RegionsDataManager.class, name = "init", args = { DataManagerContext.class })
	public void testStateInitialization() {

		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(4454658950052475227L);
		int populationSize = 30;

		/*
		 * Run 10 iterations showing that randomly generated region plugin data
		 * instances are properly reflected in the initial state of the regions
		 * data manager
		 */
		for (int i = 0; i < 10; i++) {

			// Build the people plugin with the starting population
			PeoplePluginData peoplePluginData = PeoplePluginData.builder()//
																.addPersonRange(new PersonRange(0, populationSize - 1))//
																.build();
			Plugin peoplePlugin = PeoplePlugin.getPeoplePlugin(peoplePluginData);

			// Build a region plugin with randomized regions plugin data
			RegionsPluginData regionsPluginData = getRandomizedRegionsPluginData(populationSize, randomGenerator);
			Plugin regionsPlugin = RegionsPlugin.builder().setRegionsPluginData(regionsPluginData).getRegionsPlugin();

			// Build the test plugin
			TestPluginData.Builder pluginBuilder = TestPluginData.builder();

			/*
			 * Add a single actor that will demonstrate that every aspect of the
			 * regions plugin data is reflected in the state of the regions data
			 * manager
			 */
			pluginBuilder.addTestActorPlan("actor", new TestActorPlan(0, (c) -> {
				PeopleDataManager peopleDataManager = c.getDataManager(PeopleDataManager.class);
				RegionsDataManager regionsDataManager = c.getDataManager(RegionsDataManager.class);
				// show that the initial state of the region data manager
				// matches
				// the state of the region plugin data

				assertEquals(regionsPluginData.getRegionIds(), regionsDataManager.getRegionIds());
				assertEquals(regionsPluginData.getPersonRegionArrivalTrackingPolicy(), regionsDataManager.regionArrivalsAreTracked());
				assertEquals(regionsPluginData.getRegionPropertyIds(), regionsDataManager.getRegionPropertyIds());
				for (RegionPropertyId regionPropertyId : regionsPluginData.getRegionPropertyIds()) {
					PropertyDefinition expectedPropertyDefinition = regionsPluginData.getRegionPropertyDefinition(regionPropertyId);
					PropertyDefinition actualPropertyDefinition = regionsDataManager.getRegionPropertyDefinition(regionPropertyId);
					assertEquals(expectedPropertyDefinition, actualPropertyDefinition);
				}

				for (RegionId regionId : regionsPluginData.getRegionIds()) {
					Map<RegionPropertyId, Object> expectedPropertyValues = regionsPluginData.getRegionPropertyValues(regionId);
					for (RegionPropertyId regionPropertyId : regionsPluginData.getRegionPropertyIds()) {
						Object expectedValue = expectedPropertyValues.get(regionPropertyId);
						if (expectedValue == null) {
							expectedValue = regionsPluginData.getRegionPropertyDefinition(regionPropertyId).getDefaultValue().get();
						}
						Object actualValue = regionsDataManager.getRegionPropertyValue(regionId, regionPropertyId);
						assertEquals(expectedValue, actualValue);
					}
				}

				List<PersonId> people = peopleDataManager.getPeople();
				for (PersonId personId : people) {
					RegionId expectedRegionId = regionsPluginData.getPersonRegion(personId).get();
					RegionId actualRegionId = regionsDataManager.getPersonRegion(personId);
					assertEquals(expectedRegionId, actualRegionId);

					Optional<Double> optional = regionsPluginData.getPersonRegionArrivalTime(personId);
					if (optional.isPresent()) {
						Double expectedTime = optional.get();
						double actualTime = regionsDataManager.getPersonRegionArrivalTime(personId);
						assertEquals(expectedTime, actualTime);
					}
				}

			}));

			TestPluginData testPluginData = pluginBuilder.build();
			Plugin testPlugin = TestPlugin.getTestPlugin(testPluginData);

			// Run the simulation
			TestSimulation	.builder()//
							.addPlugin(testPlugin)//
							.addPlugin(peoplePlugin)//
							.addPlugin(regionsPlugin)//
							.build()//
							.execute();
		}

		/*
		 * precondition test: if a person in the people plugin does not have an
		 * assigned region id in the region plugin data
		 */
		ContractException contractException = assertThrows(ContractException.class, () -> {
			PeoplePluginData peoplePluginData = PeoplePluginData.builder()//
																.addPersonRange(new PersonRange(0, 1))//
																.build();
			Plugin peoplePlugin = PeoplePlugin.getPeoplePlugin(peoplePluginData);

			RegionsPluginData regionsPluginData = RegionsPluginData	.builder()//
																	.addRegion(TestRegionId.REGION_1)//
																	.addPerson(new PersonId(0), TestRegionId.REGION_1)//
																	.build();
			Plugin regionsPlugin = RegionsPlugin.builder().setRegionsPluginData(regionsPluginData).getRegionsPlugin();

			Simulation.builder().addPlugin(peoplePlugin).addPlugin(regionsPlugin).build().execute();

		});
		assertEquals(RegionError.UNKNOWN_REGION_ID, contractException.getErrorType());

		/*
		 * precondition test: if a person's region arrival time exceeds the
		 * current simulation time
		 */

		contractException = assertThrows(ContractException.class, () -> {
			PeoplePluginData peoplePluginData = PeoplePluginData.builder()//
																.addPersonRange(new PersonRange(0, 1))//
																.build();
			Plugin peoplePlugin = PeoplePlugin.getPeoplePlugin(peoplePluginData);

			RegionsPluginData regionsPluginData = RegionsPluginData	.builder()//
																	.addRegion(TestRegionId.REGION_1)//
																	.setPersonRegionArrivalTracking(true)//
																	.addPerson(new PersonId(0), TestRegionId.REGION_1, 7.7)//
																	.build();
			Plugin regionsPlugin = RegionsPlugin.builder().setRegionsPluginData(regionsPluginData).getRegionsPlugin();

			SimulationState simulationState = SimulationState	.builder()//
																.setStartTime(2.4)//
																.build();

			Simulation	.builder()//
						.setSimulationState(simulationState)//
						.addPlugin(peoplePlugin)//
						.addPlugin(regionsPlugin)//
						.build()//
						.execute();

		});
		assertEquals(RegionError.REGION_ARRIVAL_TIME_EXCEEDS_SIM_TIME, contractException.getErrorType());

		/*
		 * precondition test: if the regions plugin data contains information
		 * for an unknown person id
		 */
		contractException = assertThrows(ContractException.class, () -> {
			PeoplePluginData peoplePluginData = PeoplePluginData.builder().build();
			Plugin peoplePlugin = PeoplePlugin.getPeoplePlugin(peoplePluginData);

			RegionsPluginData regionsPluginData = RegionsPluginData	.builder()//
																	.addRegion(TestRegionId.REGION_1)//
																	.addPerson(new PersonId(0), TestRegionId.REGION_1)//
																	.build();
			Plugin regionsPlugin = RegionsPlugin.builder()//
												.setRegionsPluginData(regionsPluginData)//
												.getRegionsPlugin();

			Simulation	.builder()//
						.addPlugin(peoplePlugin)//
						.addPlugin(regionsPlugin)//
						.build()//
						.execute();

		});
		assertEquals(PersonError.UNKNOWN_PERSON_ID, contractException.getErrorType());

	}

	@Test
	@UnitTestMethod(target = RegionsDataManager.class, name = "init", args = { DataManagerContext.class })
	public void testPersonImmimentAdditionEvent() {
		/*
		 * Have the agent create some people over time and show that each person
		 * is in the correct region at the correct time
		 */
		Factory factory = RegionsTestPluginFactory.factory(0, 8294774271110836859L, TimeTrackingPolicy.TRACK_TIME, (c) -> {
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
		TestSimulation.builder().addPlugins(factory.getPlugins()).build().execute();

		ContractException contractException = assertThrows(ContractException.class, () -> {
			Factory factory2 = RegionsTestPluginFactory.factory(0, 7737810808059858455L, TimeTrackingPolicy.TRACK_TIME, (c) -> {
				PeopleDataManager peopleDataManager = c.getDataManager(PeopleDataManager.class);
				PersonConstructionData personConstructionData = PersonConstructionData.builder().build();
				peopleDataManager.addPerson(personConstructionData);
			});

			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();

		});
		assertEquals(RegionError.NULL_REGION_ID, contractException.getErrorType());

		// precondition check: if the region in the event is unknown
		contractException = assertThrows(ContractException.class, () -> {
			Factory factory2 = RegionsTestPluginFactory.factory(0, 2879410509293373914L, TimeTrackingPolicy.TRACK_TIME, (c) -> {
				PeopleDataManager peopleDataManager = c.getDataManager(PeopleDataManager.class);
				PersonConstructionData personConstructionData = PersonConstructionData.builder().add(TestRegionId.getUnknownRegionId()).build();
				peopleDataManager.addPerson(personConstructionData);
			});
			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();
		});
		assertEquals(RegionError.UNKNOWN_REGION_ID, contractException.getErrorType());
		// precondition check: if the person id does not exist
		/*
		 * Note : it is not possible to force the PersonDataManager to release
		 * such an event, so we release it from a test data manager
		 */
		TestPluginData.Builder pluginBuilder = TestPluginData.builder();

		pluginBuilder.addTestDataManager("dm", () -> new PassThroughDataManager());
		pluginBuilder.addTestActorPlan("actor", new TestActorPlan(0, (c) -> {
			PersonConstructionData personConstructionData = PersonConstructionData.builder().add(TestRegionId.REGION_1).build();
			PersonImminentAdditionEvent personImminentAdditionEvent = new PersonImminentAdditionEvent(new PersonId(10000), personConstructionData);
			PassThroughDataManager passThroughDataManager = c.getDataManager(PassThroughDataManager.class);
			passThroughDataManager.passThrough(personImminentAdditionEvent);
		}));

		TestPluginData testPluginData1 = pluginBuilder.build();
		contractException = assertThrows(ContractException.class, () -> {
			Factory factory2 = RegionsTestPluginFactory.factory(0, 5311711819224332248L, TimeTrackingPolicy.TRACK_TIME, testPluginData1);
			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();
		});
		assertEquals(PersonError.UNKNOWN_PERSON_ID, contractException.getErrorType());

		// precondition check: if the person was previously added
		/*
		 * Note : it is not possible to force the PersonDataManager to release
		 * such an event, so we release it from a test data manager
		 */
		pluginBuilder = TestPluginData.builder();
		pluginBuilder.addTestDataManager("dm", () -> new PassThroughDataManager());
		pluginBuilder.addTestActorPlan("actor", new TestActorPlan(0, (c) -> {
			PeopleDataManager peopleDataManager = c.getDataManager(PeopleDataManager.class);
			PersonConstructionData personConstructionData = PersonConstructionData.builder().add(TestRegionId.REGION_1).build();
			PersonId personId = peopleDataManager.addPerson(personConstructionData);

			PersonImminentAdditionEvent personImminentAdditionEvent = new PersonImminentAdditionEvent(personId, personConstructionData);
			PassThroughDataManager passThroughDataManager = c.getDataManager(PassThroughDataManager.class);
			passThroughDataManager.passThrough(personImminentAdditionEvent);

		}));

		pluginBuilder.addPluginDependency(PeoplePluginId.PLUGIN_ID);
		TestPluginData testPluginData2 = pluginBuilder.build();
		contractException = assertThrows(ContractException.class, () -> {
			Factory factory2 = RegionsTestPluginFactory.factory(0, 5824136557013438265L, TimeTrackingPolicy.TRACK_TIME, testPluginData2);
			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();
		});
		assertEquals(RegionError.DUPLICATE_PERSON_ADDITION, contractException.getErrorType());

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
		Factory factory = RegionsTestPluginFactory.factory(0, 6410427420030580842L, TimeTrackingPolicy.TRACK_TIME, testPluginData);
		TestSimulation.builder().addPlugins(factory.getPlugins()).build().execute();

		/*
		 * precondition test: if the region property definition initialization
		 * is null
		 */
		ContractException contractException = assertThrows(ContractException.class, () -> {
			Factory factory2 = RegionsTestPluginFactory.factory(0, 755408328420621219L, TimeTrackingPolicy.TRACK_TIME, (c) -> {
				RegionsDataManager regionsDataManager = c.getDataManager(RegionsDataManager.class);
				regionsDataManager.defineRegionProperty(null);
			});
			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();
		});
		assertEquals(RegionError.NULL_REGION_PROPERTY_DEFINITION_INITIALIZATION, contractException.getErrorType());

		/*
		 * precondition test: if the region property is already defined
		 */
		contractException = assertThrows(ContractException.class, () -> {
			Factory factory2 = RegionsTestPluginFactory.factory(0, 1524991526094322535L, TimeTrackingPolicy.TRACK_TIME, (c) -> {
				RegionsDataManager regionsDataManager = c.getDataManager(RegionsDataManager.class);
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
			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();
		});
		assertEquals(PropertyError.DUPLICATE_PROPERTY_DEFINITION, contractException.getErrorType());

		/*
		 * precondition test: if the region property definition has no default
		 * and a property value for some region is missing from the
		 * RegionPropertyDefinitionInitialization
		 * 
		 */
		contractException = assertThrows(ContractException.class, () -> {
			Factory factory2 = RegionsTestPluginFactory.factory(0, 737227361871382193L, TimeTrackingPolicy.TRACK_TIME, (c) -> {
				RegionsDataManager regionsDataManager = c.getDataManager(RegionsDataManager.class);
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
			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();
		});
		assertEquals(PropertyError.INSUFFICIENT_PROPERTY_VALUE_ASSIGNMENT, contractException.getErrorType());
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
		Factory factory = RegionsTestPluginFactory.factory(0, 4801681059718243112L, TimeTrackingPolicy.TRACK_TIME, testPluginData);
		TestSimulation.builder().addPlugins(factory.getPlugins()).build().execute();

		/*
		 * precondition test: if the region construction data is null
		 */
		ContractException contractException = assertThrows(ContractException.class, () -> {
			Factory factory2 = RegionsTestPluginFactory.factory(0, 1930072318129921567L, TimeTrackingPolicy.TRACK_TIME, (c) -> {
				RegionsDataManager regionsDataManager = c.getDataManager(RegionsDataManager.class);
				regionsDataManager.addRegion(null);
			});
			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();
		});
		assertEquals(RegionError.NULL_REGION_CONSTRUCTION_DATA, contractException.getErrorType());

		/*
		 * precondition test: if the region is already present
		 */
		contractException = assertThrows(ContractException.class, () -> {
			Factory factory2 = RegionsTestPluginFactory.factory(0, 4107332213003089045L, TimeTrackingPolicy.TRACK_TIME, (c) -> {
				RegionsDataManager regionsDataManager = c.getDataManager(RegionsDataManager.class);
				RegionConstructionData regionConstructionData = RegionConstructionData.builder().setRegionId(TestRegionId.REGION_1).build();
				regionsDataManager.addRegion(regionConstructionData);
			});
			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();
		});
		assertEquals(RegionError.DUPLICATE_REGION_ID, contractException.getErrorType());

		/*
		 * precondition test: if not all region properties have default values
		 */
		contractException = assertThrows(ContractException.class, () -> {
			testConsumerWithNoDefaultRegionProperties(6895625301110154531L, (c) -> {
				RegionsDataManager regionsDataManager = c.getDataManager(RegionsDataManager.class);
				RegionConstructionData regionConstructionData = RegionConstructionData.builder().setRegionId(TestRegionId.getUnknownRegionId()).build();
				regionsDataManager.addRegion(regionConstructionData);
			});
		});
		assertEquals(PropertyError.INSUFFICIENT_PROPERTY_VALUE_ASSIGNMENT, contractException.getErrorType());

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

		Factory factory = RegionsTestPluginFactory	.factory(0, seed, TimeTrackingPolicy.TRACK_TIME, testPluginData)//
													.setRegionsPluginData(regionsPluginData);
		TestSimulation.builder().addPlugins(factory.getPlugins()).build().execute();
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
		Factory factory = RegionsTestPluginFactory.factory(numberOfPeople, 6280260397394362229L, TimeTrackingPolicy.TRACK_TIME, testPluginData);
		TestSimulation.builder().addPlugins(factory.getPlugins()).build().execute();

		// show that the observations were correct
		assertEquals(expectedObservations.size(), recievedObservations.size());
		assertEquals(new LinkedHashSet<>(expectedObservations), new LinkedHashSet<>(recievedObservations));

		/*
		 * precondition test: if the region id is null
		 */
		ContractException contractException = assertThrows(ContractException.class, () -> {
			Factory factory2 = RegionsTestPluginFactory.factory(numberOfPeople, 8703868236194395945L, TimeTrackingPolicy.TRACK_TIME, (c) -> {
				RegionsDataManager regionsDataManager = c.getDataManager(RegionsDataManager.class);
				regionsDataManager.getEventFilterForPersonRegionUpdateEvent_ByArrivalRegion(null);
			});
			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();
		});
		assertEquals(RegionError.NULL_REGION_ID, contractException.getErrorType());
		/*
		 * precondition test: if the region id is not known
		 */
		contractException = assertThrows(ContractException.class, () -> {
			Factory factory2 = RegionsTestPluginFactory.factory(numberOfPeople, 1521124301443522213L, TimeTrackingPolicy.TRACK_TIME, (c) -> {
				RegionsDataManager regionsDataManager = c.getDataManager(RegionsDataManager.class);
				regionsDataManager.getEventFilterForPersonRegionUpdateEvent_ByArrivalRegion(TestRegionId.getUnknownRegionId());
			});
			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();
		});
		assertEquals(RegionError.UNKNOWN_REGION_ID, contractException.getErrorType());
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
		Factory factory = RegionsTestPluginFactory.factory(numberOfPeople, 5906547765098032882L, TimeTrackingPolicy.TRACK_TIME, testPluginData);
		TestSimulation.builder().addPlugins(factory.getPlugins()).build().execute();

		// show that the observations were correct
		assertEquals(expectedObservations.size(), recievedObservations.size());
		assertEquals(new LinkedHashSet<>(expectedObservations), new LinkedHashSet<>(recievedObservations));

		/*
		 * precondition test: if the region id is null
		 */
		ContractException contractException = assertThrows(ContractException.class, () -> {
			Factory factory2 = RegionsTestPluginFactory.factory(numberOfPeople, 5941332064278474841L, TimeTrackingPolicy.TRACK_TIME, (c) -> {
				RegionsDataManager regionsDataManager = c.getDataManager(RegionsDataManager.class);
				regionsDataManager.getEventFilterForPersonRegionUpdateEvent_ByDepartureRegion(null);
			});
			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();
		});
		assertEquals(RegionError.NULL_REGION_ID, contractException.getErrorType());
		/*
		 * precondition test: if the region id is not known
		 */
		contractException = assertThrows(ContractException.class, () -> {
			Factory factory2 = RegionsTestPluginFactory.factory(numberOfPeople, 5981948058533294963L, TimeTrackingPolicy.TRACK_TIME, (c) -> {
				RegionsDataManager regionsDataManager = c.getDataManager(RegionsDataManager.class);
				regionsDataManager.getEventFilterForPersonRegionUpdateEvent_ByDepartureRegion(TestRegionId.getUnknownRegionId());
			});
			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();
		});
		assertEquals(RegionError.UNKNOWN_REGION_ID, contractException.getErrorType());

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
		Factory factory = RegionsTestPluginFactory.factory(numberOfPeople, 3786801901191355144L, TimeTrackingPolicy.TRACK_TIME, testPluginData);
		TestSimulation.builder().addPlugins(factory.getPlugins()).build().execute();

		// show that the observations were correct
		assertEquals(expectedObservations.size(), recievedObservations.size());
		assertEquals(new LinkedHashSet<>(expectedObservations), new LinkedHashSet<>(recievedObservations));

		/*
		 * precondition test: if the person id is null
		 */
		ContractException contractException = assertThrows(ContractException.class, () -> {
			Factory factory2 = RegionsTestPluginFactory.factory(numberOfPeople, 4504604454474342921L, TimeTrackingPolicy.TRACK_TIME, (c) -> {
				RegionsDataManager regionsDataManager = c.getDataManager(RegionsDataManager.class);
				PersonId nullPersonId = null;
				regionsDataManager.getEventFilterForPersonRegionUpdateEvent(nullPersonId);
			});
			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();
		});
		assertEquals(PersonError.NULL_PERSON_ID, contractException.getErrorType());

		/*
		 * precondition test: if the person id is not known
		 */
		contractException = assertThrows(ContractException.class, () -> {
			Factory factory2 = RegionsTestPluginFactory.factory(numberOfPeople, 1166492228021587827L, TimeTrackingPolicy.TRACK_TIME, (c) -> {
				RegionsDataManager regionsDataManager = c.getDataManager(RegionsDataManager.class);
				regionsDataManager.getEventFilterForPersonRegionUpdateEvent(new PersonId(1000000));
			});
			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();
		});
		assertEquals(PersonError.UNKNOWN_PERSON_ID, contractException.getErrorType());

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
		Factory factory = RegionsTestPluginFactory.factory(numberOfPeople, 8773677547139261431L, TimeTrackingPolicy.TRACK_TIME, testPluginData);
		TestSimulation.builder().addPlugins(factory.getPlugins()).build().execute();

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
		Factory factory = RegionsTestPluginFactory.factory(0, 1827237237983764002L, TimeTrackingPolicy.TRACK_TIME, testPluginData);
		TestSimulation.builder().addPlugins(factory.getPlugins()).build().execute();

		// precondition check: if the region property id is null
		ContractException contractException = assertThrows(ContractException.class, () -> {
			Factory factory2 = RegionsTestPluginFactory.factory(0, 2294490256521547918L, TimeTrackingPolicy.TRACK_TIME, (c) -> {
				RegionsDataManager regionsDataManager = c.getDataManager(RegionsDataManager.class);
				regionsDataManager.getEventFilterForRegionPropertyUpdateEvent(null);
			});
			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();
		});
		assertEquals(PropertyError.NULL_PROPERTY_ID, contractException.getErrorType());

		// precondition check: if the region property id is unknown
		contractException = assertThrows(ContractException.class, () -> {
			Factory factory2 = RegionsTestPluginFactory.factory(0, 4878569785353296577L, TimeTrackingPolicy.TRACK_TIME, (c) -> {
				RegionsDataManager regionsDataManager = c.getDataManager(RegionsDataManager.class);
				regionsDataManager.getEventFilterForRegionPropertyUpdateEvent(TestRegionPropertyId.getUnknownRegionPropertyId());
			});
			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();
		});
		assertEquals(PropertyError.UNKNOWN_PROPERTY_ID, contractException.getErrorType());

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
		Factory factory = RegionsTestPluginFactory.factory(0, 7132294759338470890L, TimeTrackingPolicy.TRACK_TIME, testPluginData);
		TestSimulation.builder().addPlugins(factory.getPlugins()).build().execute();

		// precondition check: if the region property id is null
		ContractException contractException = assertThrows(ContractException.class, () -> {
			Factory factory2 = RegionsTestPluginFactory.factory(0, 5168071523034596869L, TimeTrackingPolicy.TRACK_TIME, (c) -> {
				RegionsDataManager regionsDataManager = c.getDataManager(RegionsDataManager.class);
				regionsDataManager.getEventFilterForRegionPropertyUpdateEvent(TestRegionId.REGION_1, null);
			});
			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();
		});
		assertEquals(PropertyError.NULL_PROPERTY_ID, contractException.getErrorType());

		// precondition check: if the region property id is unknown
		contractException = assertThrows(ContractException.class, () -> {
			Factory factory2 = RegionsTestPluginFactory.factory(0, 5851898172389262566L, TimeTrackingPolicy.TRACK_TIME, (c) -> {
				RegionsDataManager regionsDataManager = c.getDataManager(RegionsDataManager.class);
				regionsDataManager.getEventFilterForRegionPropertyUpdateEvent(TestRegionId.REGION_1, TestRegionPropertyId.getUnknownRegionPropertyId());
			});
			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();
		});
		assertEquals(PropertyError.UNKNOWN_PROPERTY_ID, contractException.getErrorType());

		// precondition check: if the region id is null
		contractException = assertThrows(ContractException.class, () -> {
			Factory factory2 = RegionsTestPluginFactory.factory(0, 3683702073309702135L, TimeTrackingPolicy.TRACK_TIME, (c) -> {
				RegionsDataManager regionsDataManager = c.getDataManager(RegionsDataManager.class);
				regionsDataManager.getEventFilterForRegionPropertyUpdateEvent(null, TestRegionPropertyId.REGION_PROPERTY_2_INTEGER_MUTABLE);
			});
			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();
		});
		assertEquals(RegionError.NULL_REGION_ID, contractException.getErrorType());

		// precondition check: if the region id is unknown
		contractException = assertThrows(ContractException.class, () -> {
			Factory factory2 = RegionsTestPluginFactory.factory(0, 6706349084351695058L, TimeTrackingPolicy.TRACK_TIME, (c) -> {
				RegionsDataManager regionsDataManager = c.getDataManager(RegionsDataManager.class);
				regionsDataManager.getEventFilterForRegionPropertyUpdateEvent(TestRegionId.getUnknownRegionId(), TestRegionPropertyId.REGION_PROPERTY_2_INTEGER_MUTABLE);
			});
			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();
		});
		assertEquals(RegionError.UNKNOWN_REGION_ID, contractException.getErrorType());

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
		Factory factory = RegionsTestPluginFactory.factory(0, 6300334142182919392L, TimeTrackingPolicy.TRACK_TIME, testPluginData);
		TestSimulation.builder().addPlugins(factory.getPlugins()).build().execute();
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
		Factory factory = RegionsTestPluginFactory.factory(0, 6272247954838684078L, TimeTrackingPolicy.TRACK_TIME, testPluginData);
		TestSimulation.builder().addPlugins(factory.getPlugins()).build().execute();
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
		Factory factory = RegionsTestPluginFactory.factory(0, 1033803161227361793L, TimeTrackingPolicy.TRACK_TIME, testPluginData);
		TestSimulation.builder().addPlugins(factory.getPlugins()).build().execute();

	}

	private static class PassThroughDataManager extends TestDataManager {
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
		Factory factory = RegionsTestPluginFactory.factory(0, 163202760371564041L, TimeTrackingPolicy.TRACK_TIME, testPluginData);
		TestSimulation.builder().addPlugins(factory.getPlugins()).build().execute();

		// precondition test: if the person id is unknown
		/*
		 * Note : it is not possible to force the PersonDataManager to release
		 * such an event, so we release it from a data manager
		 */
		pluginBuilder.addTestDataManager("dm", () -> new PassThroughDataManager());
		pluginBuilder.addTestActorPlan("actor", new TestActorPlan(0, (c) -> {
			PersonRemovalEvent personRemovalEvent = new PersonRemovalEvent(new PersonId(1000));
			PassThroughDataManager passThroughDataManager = c.getDataManager(PassThroughDataManager.class);
			ContractException contractException = assertThrows(ContractException.class, () -> passThroughDataManager.passThrough(personRemovalEvent));
			assertEquals(PersonError.UNKNOWN_PERSON_ID, contractException.getErrorType());
		}));
		testPluginData = pluginBuilder.build();
		factory = RegionsTestPluginFactory.factory(0, 2314376445339268382L, TimeTrackingPolicy.TRACK_TIME, testPluginData);
		TestSimulation.builder().addPlugins(factory.getPlugins()).build().execute();

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
		ContractException contractException = assertThrows(ContractException.class, () -> {
			Factory factory2 = RegionsTestPluginFactory.factory(0, 3490172254703369545L, TimeTrackingPolicy.TRACK_TIME, pluginBuilder.build());
			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();
		});
		assertEquals(PersonError.UNKNOWN_PERSON_ID, contractException.getErrorType());

	}

	/**
	 * Demonstrates that the data manager exhibits run continuity. The state of
	 * the data manager is not effected by repeatedly starting and stopping the
	 * simulation.
	 */
	@Test
	@UnitTestMethod(target = RegionsDataManager.class, name = "init", args = { DataManagerContext.class })
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
	 * Returns the regions plugin data resulting from several region related
	 * events over several days. Attempts to stop and start the simulation by
	 * the given number of increments.
	 */
	private String testStateContinuity(int incrementCount) {
		String result = null;
		
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(4401967199145357368L);

		/*
		 * Build the RunContinuityPluginData with several context consumers that
		 * will add regions and people, define region properties, etc;
		 */
		RunContinuityPluginData.Builder continuityBuilder = RunContinuityPluginData.builder();

		//add two regions
		continuityBuilder.addContextConsumer(0.5, (c) -> {
			RegionsDataManager regionsDataManager = c.getDataManager(RegionsDataManager.class);

			RegionConstructionData regionConstructionData = RegionConstructionData	.builder()//
																					.setRegionId(TestRegionId.REGION_1)//
																					.build();

			regionsDataManager.addRegion(regionConstructionData);

			regionConstructionData = RegionConstructionData	.builder()//
															.setRegionId(TestRegionId.REGION_2)//
															.build();
			regionsDataManager.addRegion(regionConstructionData);
		});

		//add a few people
		continuityBuilder.addContextConsumer(1.2, (c) -> {
			PeopleDataManager peopleDataManager = c.getDataManager(PeopleDataManager.class);

			PersonConstructionData personConstructionData = PersonConstructionData	.builder()//
																					.add(TestRegionId.REGION_1)//
																					.build();
			peopleDataManager.addPerson(personConstructionData);
			peopleDataManager.addPerson(personConstructionData);
			peopleDataManager.addPerson(personConstructionData);

			personConstructionData = PersonConstructionData	.builder()//
															.add(TestRegionId.REGION_2)//
															.build();
			peopleDataManager.addPerson(personConstructionData);
			peopleDataManager.addPerson(personConstructionData);

		});

		//define a few region properties
		continuityBuilder.addContextConsumer(4.7, (c) -> {

			RegionsDataManager regionsDataManager = c.getDataManager(RegionsDataManager.class);

			TestRegionPropertyId testRegionPropertyId = TestRegionPropertyId.REGION_PROPERTY_2_INTEGER_MUTABLE;
			RegionPropertyDefinitionInitialization regionPropertyDefinitionInitialization = //
					RegionPropertyDefinitionInitialization	.builder()//
															.setPropertyDefinition(testRegionPropertyId.getPropertyDefinition())//
															.setRegionPropertyId(testRegionPropertyId)//
															.addPropertyValue(TestRegionId.REGION_1, testRegionPropertyId.getRandomPropertyValue(randomGenerator))
															.addPropertyValue(TestRegionId.REGION_2, testRegionPropertyId.getRandomPropertyValue(randomGenerator))//
															.build();

			regionsDataManager.defineRegionProperty(regionPropertyDefinitionInitialization);

			testRegionPropertyId = TestRegionPropertyId.REGION_PROPERTY_3_DOUBLE_MUTABLE;
			regionPropertyDefinitionInitialization = //
					RegionPropertyDefinitionInitialization	.builder()//
															.setPropertyDefinition(testRegionPropertyId.getPropertyDefinition())//
															.setRegionPropertyId(testRegionPropertyId)//
															.build();

			regionsDataManager.defineRegionProperty(regionPropertyDefinitionInitialization);

			

		});

		//move some people
		continuityBuilder.addContextConsumer(5.5, (c) -> {
			RegionsDataManager regionsDataManager = c.getDataManager(RegionsDataManager.class);
			regionsDataManager.setPersonRegion(new PersonId(0), TestRegionId.REGION_2);
			regionsDataManager.setPersonRegion(new PersonId(2), TestRegionId.REGION_2);
			regionsDataManager.setPersonRegion(new PersonId(3), TestRegionId.REGION_1);
		});
		
		//update region properties
		continuityBuilder.addContextConsumer(5.8, (c) -> {
			RegionsDataManager regionsDataManager = c.getDataManager(RegionsDataManager.class);
			
			TestRegionPropertyId testRegionPropertyId = TestRegionPropertyId.REGION_PROPERTY_2_INTEGER_MUTABLE;
			regionsDataManager.setRegionPropertyValue(TestRegionId.REGION_1, testRegionPropertyId, testRegionPropertyId.getRandomPropertyValue(randomGenerator));
			regionsDataManager.setRegionPropertyValue(TestRegionId.REGION_2, testRegionPropertyId, testRegionPropertyId.getRandomPropertyValue(randomGenerator));

			testRegionPropertyId = TestRegionPropertyId.REGION_PROPERTY_3_DOUBLE_MUTABLE;
			regionsDataManager.setRegionPropertyValue(TestRegionId.REGION_1, testRegionPropertyId, testRegionPropertyId.getRandomPropertyValue(randomGenerator));
			regionsDataManager.setRegionPropertyValue(TestRegionId.REGION_2, testRegionPropertyId, testRegionPropertyId.getRandomPropertyValue(randomGenerator));

			c.releaseOutput(regionsDataManager.toString());
			
		});

		
		RunContinuityPluginData runContinuityPluginData = continuityBuilder.build();

		// Build an empty people plugin data for time zero
		PeoplePluginData peoplePluginData = PeoplePluginData.builder().build();

		/*
		 * Build an empty regions plugin data with region arrival tracking
		 * turned on
		 */
		RegionsPluginData regionsPluginData = RegionsPluginData	.builder()//
																.setPersonRegionArrivalTracking(true)//
																.build();

		// build the initial simulation state data -- time starts at zero
		SimulationState simulationState = SimulationState.builder().build();

		/*
		 * Run the simulation in increments until all the plans in the run
		 * continuity plugin data have been executed
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

			// build the regions plugin
			Plugin regionsPlugin = RegionsPlugin.builder()//
												.setRegionsPluginData(regionsPluginData)//
												.getRegionsPlugin();

			TestOutputConsumer outputConsumer = new TestOutputConsumer();

			// execute the simulation so that it produces a people plugin data
			Simulation simulation = Simulation	.builder()//
												.addPlugin(peoplePlugin)//
												.addPlugin(regionsPlugin)//
												.addPlugin(runContinuityPlugin)//
												.setSimulationHaltTime(haltTime)//
												.setRecordState(true)//
												.setOutputConsumer(outputConsumer)//
												.setSimulationState(simulationState)//
												.build();//
			simulation.execute();

			// retrieve the people plugin data
			peoplePluginData = outputConsumer.getOutputItem(PeoplePluginData.class).get();

			// retrieve the regions plugin data
			regionsPluginData = outputConsumer.getOutputItem(RegionsPluginData.class).get();

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
