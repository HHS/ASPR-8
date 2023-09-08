package gov.hhs.aspr.ms.gcm.plugins.resources.datamanagers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.function.Consumer;

import org.apache.commons.math3.random.RandomGenerator;
import org.apache.commons.math3.util.FastMath;
import org.apache.commons.math3.util.Pair;
import org.junit.jupiter.api.Test;

import gov.hhs.aspr.ms.gcm.nucleus.ActorContext;
import gov.hhs.aspr.ms.gcm.nucleus.DataManagerContext;
import gov.hhs.aspr.ms.gcm.nucleus.EventFilter;
import gov.hhs.aspr.ms.gcm.nucleus.Plugin;
import gov.hhs.aspr.ms.gcm.nucleus.Simulation;
import gov.hhs.aspr.ms.gcm.nucleus.SimulationState;
import gov.hhs.aspr.ms.gcm.nucleus.testsupport.runcontinuityplugin.RunContinuityPlugin;
import gov.hhs.aspr.ms.gcm.nucleus.testsupport.runcontinuityplugin.RunContinuityPluginData;
import gov.hhs.aspr.ms.gcm.nucleus.testsupport.testplugin.TestActorPlan;
import gov.hhs.aspr.ms.gcm.nucleus.testsupport.testplugin.TestOutputConsumer;
import gov.hhs.aspr.ms.gcm.nucleus.testsupport.testplugin.TestPlugin;
import gov.hhs.aspr.ms.gcm.nucleus.testsupport.testplugin.TestPluginData;
import gov.hhs.aspr.ms.gcm.nucleus.testsupport.testplugin.TestSimulation;
import gov.hhs.aspr.ms.gcm.plugins.people.PeoplePlugin;
import gov.hhs.aspr.ms.gcm.plugins.people.datamanagers.PeopleDataManager;
import gov.hhs.aspr.ms.gcm.plugins.people.datamanagers.PeoplePluginData;
import gov.hhs.aspr.ms.gcm.plugins.people.support.PersonConstructionData;
import gov.hhs.aspr.ms.gcm.plugins.people.support.PersonError;
import gov.hhs.aspr.ms.gcm.plugins.people.support.PersonId;
import gov.hhs.aspr.ms.gcm.plugins.people.support.PersonRange;
import gov.hhs.aspr.ms.gcm.plugins.regions.RegionsPlugin;
import gov.hhs.aspr.ms.gcm.plugins.regions.datamanagers.RegionsDataManager;
import gov.hhs.aspr.ms.gcm.plugins.regions.datamanagers.RegionsPluginData;
import gov.hhs.aspr.ms.gcm.plugins.regions.support.RegionConstructionData;
import gov.hhs.aspr.ms.gcm.plugins.regions.support.RegionError;
import gov.hhs.aspr.ms.gcm.plugins.regions.support.RegionId;
import gov.hhs.aspr.ms.gcm.plugins.regions.testsupport.TestRegionId;
import gov.hhs.aspr.ms.gcm.plugins.resources.ResourcesPlugin;
import gov.hhs.aspr.ms.gcm.plugins.resources.events.PersonResourceUpdateEvent;
import gov.hhs.aspr.ms.gcm.plugins.resources.events.RegionResourceUpdateEvent;
import gov.hhs.aspr.ms.gcm.plugins.resources.events.ResourceIdAdditionEvent;
import gov.hhs.aspr.ms.gcm.plugins.resources.events.ResourcePropertyDefinitionEvent;
import gov.hhs.aspr.ms.gcm.plugins.resources.events.ResourcePropertyUpdateEvent;
import gov.hhs.aspr.ms.gcm.plugins.resources.support.ResourceError;
import gov.hhs.aspr.ms.gcm.plugins.resources.support.ResourceId;
import gov.hhs.aspr.ms.gcm.plugins.resources.support.ResourceInitialization;
import gov.hhs.aspr.ms.gcm.plugins.resources.support.ResourcePropertyId;
import gov.hhs.aspr.ms.gcm.plugins.resources.support.ResourcePropertyInitialization;
import gov.hhs.aspr.ms.gcm.plugins.resources.testsupport.ResourcesTestPluginFactory;
import gov.hhs.aspr.ms.gcm.plugins.resources.testsupport.ResourcesTestPluginFactory.Factory;
import gov.hhs.aspr.ms.gcm.plugins.resources.testsupport.TestResourceId;
import gov.hhs.aspr.ms.gcm.plugins.resources.testsupport.TestResourcePropertyId;
import gov.hhs.aspr.ms.gcm.plugins.stochastics.StochasticsPlugin;
import gov.hhs.aspr.ms.gcm.plugins.stochastics.datamanagers.StochasticsDataManager;
import gov.hhs.aspr.ms.gcm.plugins.stochastics.datamanagers.StochasticsPluginData;
import gov.hhs.aspr.ms.gcm.plugins.stochastics.support.WellState;
import gov.hhs.aspr.ms.gcm.plugins.util.properties.PropertyDefinition;
import gov.hhs.aspr.ms.gcm.plugins.util.properties.PropertyError;
import util.annotations.UnitTestConstructor;
import util.annotations.UnitTestMethod;
import util.errors.ContractException;
import util.random.RandomGeneratorProvider;
import util.wrappers.MultiKey;
import util.wrappers.MutableDouble;
import util.wrappers.MutableInteger;
import util.wrappers.MutableObject;

public final class AT_ResourcesDataManager {

	/**
	 * Demonstrates that the data manager produces plugin data that reflects its
	 * final state
	 */
	@Test
	@UnitTestMethod(target = ResourcesDataManager.class, name = "init", args = { DataManagerContext.class })
	public void testStateFinalization() {

		// show that the plugin data persists after multiple actions
		List<RegionId> expectedRegionIds = new ArrayList<>();

		ResourcesPluginData resourcesPluginData2 = ResourcesPluginData.builder()
				.defineResourceProperty(TestResourceId.RESOURCE_1,
						TestResourcePropertyId.ResourceProperty_1_2_INTEGER_MUTABLE,
						TestResourcePropertyId.ResourceProperty_1_2_INTEGER_MUTABLE.getPropertyDefinition())
				.defineResourceProperty(TestResourceId.RESOURCE_2,
						TestResourcePropertyId.ResourceProperty_1_1_BOOLEAN_MUTABLE,
						TestResourcePropertyId.ResourceProperty_1_1_BOOLEAN_MUTABLE.getPropertyDefinition())
				.addResource(TestResourceId.RESOURCE_1, 0.0, false)//
				.addResource(TestResourceId.RESOURCE_2, 0.0, true)//
				.setResourcePropertyValue(TestResourceId.RESOURCE_1,
						TestResourcePropertyId.ResourceProperty_1_2_INTEGER_MUTABLE, 45)//
				.build();
		TestPluginData.Builder pluginBuilder = TestPluginData.builder();

		pluginBuilder.addTestActorPlan("actor", new TestActorPlan(0, (c) -> {
			ResourcesDataManager resourcesDataManager = c.getDataManager(ResourcesDataManager.class);
			RegionsDataManager regionsDataManager = c.getDataManager(RegionsDataManager.class);

			resourcesDataManager.addResourceToRegion(TestResourceId.RESOURCE_1, TestRegionId.REGION_1, 55);

			RegionId personRegion = regionsDataManager.getPersonRegion(new PersonId(0));
			expectedRegionIds.add(personRegion);
			resourcesDataManager.addResourceToRegion(TestResourceId.RESOURCE_2, personRegion, 33);
			resourcesDataManager.transferResourceToPersonFromRegion(TestResourceId.RESOURCE_2, new PersonId(0), 30);
		}));

		pluginBuilder.addTestActorPlan("actor", new TestActorPlan(1, (c) -> {
			ResourcesDataManager resourcesDataManager = c.getDataManager(ResourcesDataManager.class);

			resourcesDataManager.addResourceId(TestResourceId.RESOURCE_3, false);
			resourcesDataManager.addResourceToRegion(TestResourceId.RESOURCE_3, TestRegionId.REGION_2, 73);
			resourcesDataManager.transferResourceFromPersonToRegion(TestResourceId.RESOURCE_2, new PersonId(0), 10);
			resourcesDataManager.transferResourceBetweenRegions(TestResourceId.RESOURCE_2, TestRegionId.REGION_1,
					TestRegionId.REGION_2, 5);
			resourcesDataManager.expandCapacity(5);

		}));

		TestPluginData testPluginData2 = pluginBuilder.build();
		Factory factory2 = ResourcesTestPluginFactory.factory(2, 7939130943360648501L, testPluginData2)//
				.setResourcesPluginData(resourcesPluginData2);
		TestOutputConsumer testOutputConsumer2 = TestSimulation.builder()//
				.addPlugins(factory2.getPlugins())//
				.setProduceSimulationStateOnHalt(true)//
				.setSimulationHaltTime(2)//
				.build()//
				.execute();
		Map<ResourcesPluginData, Integer> outputItems2 = testOutputConsumer2
				.getOutputItemMap(ResourcesPluginData.class);
		assertEquals(1, outputItems2.size());
		ResourcesPluginData actualPluginData = outputItems2.keySet().iterator().next();
		ResourcesPluginData expectedPluginData = ResourcesPluginData.builder()
				.defineResourceProperty(TestResourceId.RESOURCE_1,
						TestResourcePropertyId.ResourceProperty_1_2_INTEGER_MUTABLE,
						TestResourcePropertyId.ResourceProperty_1_2_INTEGER_MUTABLE.getPropertyDefinition())
				.defineResourceProperty(TestResourceId.RESOURCE_2,
						TestResourcePropertyId.ResourceProperty_1_1_BOOLEAN_MUTABLE,
						TestResourcePropertyId.ResourceProperty_1_1_BOOLEAN_MUTABLE.getPropertyDefinition())
				.addResource(TestResourceId.RESOURCE_1, 0.0, false)//
				.addResource(TestResourceId.RESOURCE_2, 0.0, true)//
				.addResource(TestResourceId.RESOURCE_3, 1.0, false)//
				.setResourcePropertyValue(TestResourceId.RESOURCE_1,
						TestResourcePropertyId.ResourceProperty_1_2_INTEGER_MUTABLE, 45)
				.setRegionResourceLevel(TestRegionId.REGION_1, TestResourceId.RESOURCE_1, 55)
				.setRegionResourceLevel(expectedRegionIds.get(0), TestResourceId.RESOURCE_2, 8)
				.setRegionResourceLevel(TestRegionId.REGION_2, TestResourceId.RESOURCE_2, 5)
				.setRegionResourceLevel(TestRegionId.REGION_2, TestResourceId.RESOURCE_3, 73)
				.setPersonResourceLevel(new PersonId(0), TestResourceId.RESOURCE_2, 20L)//
				.setPersonResourceTime(new PersonId(0), TestResourceId.RESOURCE_2, 1.0).build();

		assertEquals(expectedPluginData, actualPluginData);
	}

	@Test
	@UnitTestMethod(target = ResourcesDataManager.class, name = "init", args = { DataManagerContext.class })
	public void testPersonRemovalEvent() {
		TestPluginData.Builder pluginBuilder = TestPluginData.builder();

		// Have an actor add a person with resources and then remove that person

		MutableObject<PersonId> mutablePersonId = new MutableObject<>();

		pluginBuilder.addTestActorPlan("actor", new TestActorPlan(0, (c) -> {

			PeopleDataManager peopleDataManager = c.getDataManager(PeopleDataManager.class);
			ResourcesDataManager resourcesDataManager = c.getDataManager(ResourcesDataManager.class);

			// create a person and set their resources
			PersonConstructionData personConstructionData = PersonConstructionData.builder().add(TestRegionId.REGION_1)
					.build();
			PersonId personId = peopleDataManager.addPerson(personConstructionData);
			mutablePersonId.setValue(personId);

			for (TestResourceId testResourceId : TestResourceId.values()) {
				resourcesDataManager.addResourceToRegion(testResourceId, TestRegionId.REGION_1, 100);
				resourcesDataManager.transferResourceToPersonFromRegion(testResourceId, personId, 1);
			}

			peopleDataManager.removePerson(personId);

			// show that the person still exists
			assertTrue(peopleDataManager.personExists(personId));
		}));

		// Have the actor show that the person does not exist and there are no
		// resources for that person. This is done at the same time as the
		// person removal, but due to ordering will executeSimulation after the
		// person is
		// fully eliminated
		pluginBuilder.addTestActorPlan("actor", new TestActorPlan(0, (c) -> {
			PersonId personId = mutablePersonId.getValue();
			PeopleDataManager peopleDataManager = c.getDataManager(PeopleDataManager.class);
			ResourcesDataManager resourcesDataManager = c.getDataManager(ResourcesDataManager.class);

			assertFalse(peopleDataManager.personExists(personId));

			for (TestResourceId testResourceId : TestResourceId.values()) {
				assertThrows(ContractException.class,
						() -> resourcesDataManager.getPersonResourceLevel(testResourceId, personId));
			}

		}));

		TestPluginData testPluginData = pluginBuilder.build();
		Factory factory = ResourcesTestPluginFactory.factory(0, 5231820238498733928L, testPluginData);
		TestSimulation.builder().addPlugins(factory.getPlugins()).build().execute();
	}

	@Test
	@UnitTestMethod(target = ResourcesDataManager.class, name = "getPeopleWithoutResource", args = { ResourceId.class })
	public void testGetPeopleWithoutResource() {

		Factory factory = ResourcesTestPluginFactory.factory(100, 3641510187112920884L, (c) -> {
			ResourcesDataManager resourcesDataManager = c.getDataManager(ResourcesDataManager.class);
			RegionsDataManager regionsDataManager = c.getDataManager(RegionsDataManager.class);
			StochasticsDataManager stochasticsDataManager = c.getDataManager(StochasticsDataManager.class);
			RandomGenerator randomGenerator = stochasticsDataManager.getRandomGenerator();
			PeopleDataManager peopleDataManager = c.getDataManager(PeopleDataManager.class);
			Set<PersonId> expectedPeople = new LinkedHashSet<>();
			// give about half of the people the resource
			for (PersonId personId : peopleDataManager.getPeople()) {
				long resourceLevel = resourcesDataManager.getPersonResourceLevel(TestResourceId.RESOURCE_5, personId);
				if (randomGenerator.nextBoolean() || resourceLevel > 0) {
					RegionId regionId = regionsDataManager.getPersonRegion(personId);
					resourcesDataManager.addResourceToRegion(TestResourceId.RESOURCE_5, regionId, 5);
					resourcesDataManager.transferResourceToPersonFromRegion(TestResourceId.RESOURCE_5, personId, 5);
				} else {
					expectedPeople.add(personId);
				}
			}
			// show that those who did not get the resource are returned
			List<PersonId> actualPeople = resourcesDataManager.getPeopleWithoutResource(TestResourceId.RESOURCE_5);
			assertEquals(expectedPeople.size(), actualPeople.size());
			assertEquals(expectedPeople, new LinkedHashSet<>(actualPeople));

		});
		TestSimulation.builder().addPlugins(factory.getPlugins()).build().execute();

		/* precondition test: if the resource id is null */
		ContractException contractException = assertThrows(ContractException.class, () -> {
			Factory factory2 = ResourcesTestPluginFactory.factory(100, 3473450607674582992L, (c) -> {
				ResourcesDataManager resourcesDataManager = c.getDataManager(ResourcesDataManager.class);
				resourcesDataManager.getPeopleWithoutResource(null);
			});
			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();
		});
		assertEquals(ResourceError.NULL_RESOURCE_ID, contractException.getErrorType());

		/* precondition test: if the resource id is unknown */
		contractException = assertThrows(ContractException.class, () -> {
			Factory factory2 = ResourcesTestPluginFactory.factory(100, 1143781261828756924L, (c) -> {
				ResourcesDataManager resourcesDataManager = c.getDataManager(ResourcesDataManager.class);
				resourcesDataManager.getPeopleWithoutResource(TestResourceId.getUnknownResourceId());
			});
			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();
		});
		assertEquals(ResourceError.UNKNOWN_RESOURCE_ID, contractException.getErrorType());

	}

	@Test
	@UnitTestConstructor(target = ResourcesDataManager.class, args = { ResourcesPluginData.class })
	public void testConstructor() {
		ContractException contractException = assertThrows(ContractException.class,
				() -> new ResourcesDataManager(null));
		assertEquals(ResourceError.NULL_RESOURCE_PLUGIN_DATA, contractException.getErrorType());
	}

	@Test
	@UnitTestMethod(target = ResourcesDataManager.class, name = "expandCapacity", args = { int.class })
	public void testExpandCapacity() {
		Factory factory = ResourcesTestPluginFactory.factory(100, 9107703044214388523L, (c) -> {
			ResourcesDataManager resourcesDataManager = c.getDataManager(ResourcesDataManager.class);
			ContractException contractException = assertThrows(ContractException.class,
					() -> resourcesDataManager.expandCapacity(-1));
			assertEquals(PersonError.NEGATIVE_GROWTH_PROJECTION, contractException.getErrorType());
		});
		TestSimulation.builder().addPlugins(factory.getPlugins()).build().execute();
	}

	@Test
	@UnitTestMethod(target = ResourcesDataManager.class, name = "getPeopleWithResource", args = { ResourceId.class })
	public void testGetPeopleWithResource() {

		Factory factory = ResourcesTestPluginFactory.factory(100, 1030108367649001208L, (c) -> {
			ResourcesDataManager resourcesDataManager = c.getDataManager(ResourcesDataManager.class);
			RegionsDataManager regionsDataManager = c.getDataManager(RegionsDataManager.class);
			StochasticsDataManager stochasticsDataManager = c.getDataManager(StochasticsDataManager.class);
			RandomGenerator randomGenerator = stochasticsDataManager.getRandomGenerator();
			PeopleDataManager peopleDataManager = c.getDataManager(PeopleDataManager.class);
			Set<PersonId> expectedPeople = new LinkedHashSet<>();
			// give about half of the people the resource
			for (PersonId personId : peopleDataManager.getPeople()) {
				long resourceLevel = resourcesDataManager.getPersonResourceLevel(TestResourceId.RESOURCE_5, personId);
				if (randomGenerator.nextBoolean() || resourceLevel > 0) {
					RegionId regionId = regionsDataManager.getPersonRegion(personId);
					resourcesDataManager.addResourceToRegion(TestResourceId.RESOURCE_5, regionId, 5);
					resourcesDataManager.transferResourceToPersonFromRegion(TestResourceId.RESOURCE_5, personId, 5);
					expectedPeople.add(personId);
				}
			}
			// show that those who did not get the resource are returned
			List<PersonId> actualPeople = resourcesDataManager.getPeopleWithResource(TestResourceId.RESOURCE_5);
			assertEquals(expectedPeople.size(), actualPeople.size());
			assertEquals(expectedPeople, new LinkedHashSet<>(actualPeople));

		});
		TestSimulation.builder().addPlugins(factory.getPlugins()).build().execute();

		/* precondition test: if the resource id is null */
		ContractException contractException = assertThrows(ContractException.class, () -> {
			Factory factory2 = ResourcesTestPluginFactory.factory(100, 319392144027980087L, (c) -> {
				ResourcesDataManager resourcesDataManager = c.getDataManager(ResourcesDataManager.class);
				resourcesDataManager.getPeopleWithoutResource(null);
			});
			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();
		});
		assertEquals(ResourceError.NULL_RESOURCE_ID, contractException.getErrorType());

		/* precondition test: if the resource id is unknown */
		contractException = assertThrows(ContractException.class, () -> {
			Factory factory2 = ResourcesTestPluginFactory.factory(100, 8576038889544967878L, (c) -> {
				ResourcesDataManager resourcesDataManager = c.getDataManager(ResourcesDataManager.class);
				resourcesDataManager.getPeopleWithoutResource(TestResourceId.getUnknownResourceId());
			});
			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();
		});
		assertEquals(ResourceError.UNKNOWN_RESOURCE_ID, contractException.getErrorType());

	}

	@Test
	@UnitTestMethod(target = ResourcesDataManager.class, name = "getPersonResourceLevel", args = { ResourceId.class,
			PersonId.class })
	public void testGetPersonResourceLevel() {

		Factory factory = ResourcesTestPluginFactory.factory(20, 110987310555566746L, (c) -> {
			ResourcesDataManager resourcesDataManager = c.getDataManager(ResourcesDataManager.class);
			RegionsDataManager regionsDataManager = c.getDataManager(RegionsDataManager.class);
			StochasticsDataManager stochasticsDataManager = c.getDataManager(StochasticsDataManager.class);
			RandomGenerator randomGenerator = stochasticsDataManager.getRandomGenerator();
			PeopleDataManager peopleDataManager = c.getDataManager(PeopleDataManager.class);

			List<PersonId> people = peopleDataManager.getPeople();

			// give random amounts of resource to random people
			for (int i = 0; i < 1000; i++) {
				PersonId personId = people.get(randomGenerator.nextInt(people.size()));
				ResourceId resourceId = TestResourceId.getRandomResourceId(randomGenerator);
				int amount = randomGenerator.nextInt(5);
				long expectedLevel = resourcesDataManager.getPersonResourceLevel(resourceId, personId);
				expectedLevel += amount;

				RegionId regionId = regionsDataManager.getPersonRegion(personId);
				resourcesDataManager.addResourceToRegion(resourceId, regionId, amount);
				resourcesDataManager.transferResourceToPersonFromRegion(resourceId, personId, amount);
				long actualLevel = resourcesDataManager.getPersonResourceLevel(resourceId, personId);
				assertEquals(expectedLevel, actualLevel);
			}

		});

		TestSimulation.builder().addPlugins(factory.getPlugins()).build().execute();

		/* precondition test: if the resource id is null */
		ContractException contractException = assertThrows(ContractException.class, () -> {
			Factory factory2 = ResourcesTestPluginFactory.factory(20, 5173387308794126450L, (c) -> {
				ResourcesDataManager resourcesDataManager = c.getDataManager(ResourcesDataManager.class);
				resourcesDataManager.getPersonResourceLevel(null, new PersonId(0));
			});
			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();
		});
		assertEquals(ResourceError.NULL_RESOURCE_ID, contractException.getErrorType());

		/* precondition test: if the resource id is unknown */
		contractException = assertThrows(ContractException.class, () -> {
			Factory factory2 = ResourcesTestPluginFactory.factory(20, 5756572221517144312L, (c) -> {
				ResourcesDataManager resourcesDataManager = c.getDataManager(ResourcesDataManager.class);
				resourcesDataManager.getPersonResourceLevel(TestResourceId.getUnknownResourceId(), new PersonId(0));
			});
			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();
		});
		assertEquals(ResourceError.UNKNOWN_RESOURCE_ID, contractException.getErrorType());

		/* precondition test: if the person id null */
		contractException = assertThrows(ContractException.class, () -> {
			Factory factory2 = ResourcesTestPluginFactory.factory(20, 1392115005391991861L, (c) -> {
				ResourcesDataManager resourcesDataManager = c.getDataManager(ResourcesDataManager.class);
				resourcesDataManager.getPersonResourceLevel(TestResourceId.RESOURCE_1, null);
			});
			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();
		});
		assertEquals(PersonError.NULL_PERSON_ID, contractException.getErrorType());
	}

	@Test
	@UnitTestMethod(target = ResourcesDataManager.class, name = "getPersonResourceTime", args = { ResourceId.class,
			PersonId.class })
	public void testGetPersonResourceTime() {
		TestPluginData.Builder pluginBuilder = TestPluginData.builder();

		Map<MultiKey, MutableDouble> expectedTimes = new LinkedHashMap<>();

		pluginBuilder.addTestActorPlan("actor", new TestActorPlan(0, (c) -> {
			// establish data views
			RegionsDataManager regionsDataManager = c.getDataManager(RegionsDataManager.class);
			ResourcesDataManager resourcesDataManager = c.getDataManager(ResourcesDataManager.class);
			StochasticsDataManager stochasticsDataManager = c.getDataManager(StochasticsDataManager.class);
			RandomGenerator randomGenerator = stochasticsDataManager.getRandomGenerator();
			PeopleDataManager peopleDataManager = c.getDataManager(PeopleDataManager.class);

			// establish the people and resources
			Set<ResourceId> resourceIds = resourcesDataManager.getResourceIds();
			List<PersonId> people = peopleDataManager.getPeople();

			// initialize the expected times
			for (PersonId personId : people) {
				for (ResourceId resourceId : resourceIds) {
					expectedTimes.put(new MultiKey(personId, resourceId), new MutableDouble());
				}
			}

			// show that there are at least two resources that are being time
			// tracked
			int trackedResourceCount = 0;
			for (ResourceId resourceId : resourceIds) {
				boolean personResourceTimeTrackingPolicy = resourcesDataManager
						.getPersonResourceTimeTrackingPolicy(resourceId);
				if (personResourceTimeTrackingPolicy) {
					trackedResourceCount++;
				}
			}
			assertTrue(trackedResourceCount > 1);

			// give random amounts of resource to random people
			for (int i = 0; i < people.size(); i++) {
				PersonId personId = people.get(randomGenerator.nextInt(people.size()));
				ResourceId resourceId = TestResourceId.getRandomResourceId(randomGenerator);
				int amount = randomGenerator.nextInt(5) + 1;

				RegionId regionId = regionsDataManager.getPersonRegion(personId);
				resourcesDataManager.addResourceToRegion(resourceId, regionId, amount);
				resourcesDataManager.transferResourceToPersonFromRegion(resourceId, personId, amount);

				expectedTimes.get(new MultiKey(personId, resourceId)).setValue(c.getTime());
			}

		}));

		// make more resource updates at time 1
		pluginBuilder.addTestActorPlan("actor", new TestActorPlan(1, (c) -> {
			// establish data views
			RegionsDataManager regionsDataManager = c.getDataManager(RegionsDataManager.class);
			ResourcesDataManager resourcesDataManager = c.getDataManager(ResourcesDataManager.class);
			StochasticsDataManager stochasticsDataManager = c.getDataManager(StochasticsDataManager.class);
			RandomGenerator randomGenerator = stochasticsDataManager.getRandomGenerator();
			PeopleDataManager peopleDataManager = c.getDataManager(PeopleDataManager.class);

			// establish the people and resources
			List<PersonId> people = peopleDataManager.getPeople();

			// give random amounts of resource to random people
			for (int i = 0; i < people.size(); i++) {
				PersonId personId = people.get(randomGenerator.nextInt(people.size()));
				ResourceId resourceId = TestResourceId.getRandomResourceId(randomGenerator);
				int amount = randomGenerator.nextInt(5) + 1;

				RegionId regionId = regionsDataManager.getPersonRegion(personId);
				resourcesDataManager.addResourceToRegion(resourceId, regionId, amount);
				resourcesDataManager.transferResourceToPersonFromRegion(resourceId, personId, amount);

				expectedTimes.get(new MultiKey(personId, resourceId)).setValue(c.getTime());
			}

		}));

		// make more resource updates at time 2
		pluginBuilder.addTestActorPlan("actor", new TestActorPlan(2, (c) -> {
			// establish data views
			RegionsDataManager regionsDataManager = c.getDataManager(RegionsDataManager.class);
			ResourcesDataManager resourcesDataManager = c.getDataManager(ResourcesDataManager.class);
			StochasticsDataManager stochasticsDataManager = c.getDataManager(StochasticsDataManager.class);
			RandomGenerator randomGenerator = stochasticsDataManager.getRandomGenerator();
			PeopleDataManager peopleDataManager = c.getDataManager(PeopleDataManager.class);

			// establish the people and resources
			List<PersonId> people = peopleDataManager.getPeople();

			// give random amounts of resource to random people

			for (int i = 0; i < people.size(); i++) {
				PersonId personId = people.get(randomGenerator.nextInt(people.size()));
				ResourceId resourceId = TestResourceId.getRandomResourceId(randomGenerator);
				int amount = randomGenerator.nextInt(5) + 1;
				RegionId regionId = regionsDataManager.getPersonRegion(personId);
				resourcesDataManager.addResourceToRegion(resourceId, regionId, amount);
				resourcesDataManager.transferResourceToPersonFromRegion(resourceId, personId, amount);
				expectedTimes.get(new MultiKey(personId, resourceId)).setValue(c.getTime());
			}

		}));

		// test the person resource times
		pluginBuilder.addTestActorPlan("actor", new TestActorPlan(3, (c) -> {
			// establish data views

			ResourcesDataManager resourcesDataManager = c.getDataManager(ResourcesDataManager.class);
			PeopleDataManager peopleDataManager = c.getDataManager(PeopleDataManager.class);

			// show that the person resource times match expectations
			int actualAssertionsCount = 0;
			for (MultiKey multiKey : expectedTimes.keySet()) {
				PersonId personId = multiKey.getKey(0);
				ResourceId resourceId = multiKey.getKey(1);
				boolean trackTimes = resourcesDataManager.getPersonResourceTimeTrackingPolicy(resourceId);
				if (trackTimes) {
					double expectedTime = expectedTimes.get(multiKey).getValue();
					double actualTime = resourcesDataManager.getPersonResourceTime(resourceId, personId);
					assertEquals(expectedTime, actualTime);
					actualAssertionsCount++;
				}
			}
			/*
			 * Show that the number of time values that were tested is equal to the size of
			 * the population times the number of time-tracked resources
			 */

			int trackedResourceCount = 0;
			for (ResourceId resourceId : resourcesDataManager.getResourceIds()) {
				boolean trackTimes = resourcesDataManager.getPersonResourceTimeTrackingPolicy(resourceId);
				if (trackTimes) {
					trackedResourceCount++;
				}
			}

			int expectedAssertionsCount = peopleDataManager.getPopulationCount() * trackedResourceCount;
			assertEquals(expectedAssertionsCount, actualAssertionsCount);
		}));

		TestPluginData testPluginData = pluginBuilder.build();
		Factory factory = ResourcesTestPluginFactory.factory(30, 3274189520478045515L, testPluginData);
		TestSimulation.builder().addPlugins(factory.getPlugins()).build().execute();
		/*
		 * precondition test: if the assignment times for the resource are not tracked
		 */
		ContractException contractException = assertThrows(ContractException.class, () -> {
			Factory factory2 = ResourcesTestPluginFactory.factory(30, 4631279382559646912L, (c) -> {
				ResourcesDataManager resourcesDataManager = c.getDataManager(ResourcesDataManager.class);
				resourcesDataManager.getPersonResourceTime(TestResourceId.RESOURCE_2, new PersonId(0));
			});
			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();
		});
		assertEquals(ResourceError.RESOURCE_ASSIGNMENT_TIME_NOT_TRACKED, contractException.getErrorType());

		/* precondition test: if the resource id is null */
		contractException = assertThrows(ContractException.class, () -> {
			Factory factory2 = ResourcesTestPluginFactory.factory(30, 2409228447197751995L, (c) -> {
				ResourcesDataManager resourcesDataManager = c.getDataManager(ResourcesDataManager.class);
				resourcesDataManager.getPersonResourceTime(null, new PersonId(0));
			});
			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();
		});
		assertEquals(ResourceError.NULL_RESOURCE_ID, contractException.getErrorType());

		/* precondition test: if the resource id is unknown */
		contractException = assertThrows(ContractException.class, () -> {
			Factory factory2 = ResourcesTestPluginFactory.factory(30, 6640524810334992305L, (c) -> {
				ResourcesDataManager resourcesDataManager = c.getDataManager(ResourcesDataManager.class);
				resourcesDataManager.getPersonResourceTime(TestResourceId.getUnknownResourceId(), new PersonId(0));
			});
			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();
		});
		assertEquals(ResourceError.UNKNOWN_RESOURCE_ID, contractException.getErrorType());

		/* precondition test: if the person id null */
		contractException = assertThrows(ContractException.class, () -> {
			Factory factory2 = ResourcesTestPluginFactory.factory(30, 6775179388362303664L, (c) -> {
				ResourcesDataManager resourcesDataManager = c.getDataManager(ResourcesDataManager.class);
				resourcesDataManager.getPersonResourceTime(TestResourceId.RESOURCE_1, null);
			});
			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();
		});
		assertEquals(PersonError.NULL_PERSON_ID, contractException.getErrorType());

	}

	@Test
	@UnitTestMethod(target = ResourcesDataManager.class, name = "getPersonResourceTimeTrackingPolicy", args = {
			ResourceId.class })
	public void testGetPersonResourceTimeTrackingPolicy() {

		Factory factory = ResourcesTestPluginFactory.factory(5, 757175164544632409L, (c) -> {
			ResourcesDataManager resourcesDataManager = c.getDataManager(ResourcesDataManager.class);
			for (TestResourceId testResourceId : TestResourceId.values()) {
				boolean actualPolicy = resourcesDataManager.getPersonResourceTimeTrackingPolicy(testResourceId);
				boolean expectedPolicy = testResourceId.getTimeTrackingPolicy();
				assertEquals(expectedPolicy, actualPolicy);
			}
		});

		TestSimulation.builder().addPlugins(factory.getPlugins()).build().execute();

		/* precondition test: if the resource id is null */
		ContractException contractException = assertThrows(ContractException.class, () -> {
			Factory factory2 = ResourcesTestPluginFactory.factory(5, 1761534115327431429L, (c) -> {
				ResourcesDataManager resourcesDataManager = c.getDataManager(ResourcesDataManager.class);
				resourcesDataManager.getPersonResourceTimeTrackingPolicy(null);
			});
			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();
		});
		assertEquals(ResourceError.NULL_RESOURCE_ID, contractException.getErrorType());

		/* precondition test: if the resource id is unknown */
		contractException = assertThrows(ContractException.class, () -> {
			Factory factory2 = ResourcesTestPluginFactory.factory(5, 7202590650313787556L, (c) -> {
				ResourcesDataManager resourcesDataManager = c.getDataManager(ResourcesDataManager.class);
				resourcesDataManager.getPersonResourceTimeTrackingPolicy(TestResourceId.getUnknownResourceId());
			});
			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();
		});
		assertEquals(ResourceError.UNKNOWN_RESOURCE_ID, contractException.getErrorType());

	}

	@Test
	@UnitTestMethod(target = ResourcesDataManager.class, name = "getRegionResourceLevel", args = { RegionId.class,
			ResourceId.class })
	public void testGetRegionResourceLevel() {

		Factory factory = ResourcesTestPluginFactory.factory(20, 6606932435911201728L, (c) -> {
			ResourcesDataManager resourcesDataManager = c.getDataManager(ResourcesDataManager.class);
			StochasticsDataManager stochasticsDataManager = c.getDataManager(StochasticsDataManager.class);
			RandomGenerator randomGenerator = stochasticsDataManager.getRandomGenerator();
			RegionsDataManager regionsDataManager = c.getDataManager(RegionsDataManager.class);

			List<RegionId> regionIds = new ArrayList<>(regionsDataManager.getRegionIds());

			// give random amounts of resource to random regions
			for (int i = 0; i < 1000; i++) {
				RegionId regionId = regionIds.get(randomGenerator.nextInt(regionIds.size()));
				ResourceId resourceId = TestResourceId.getRandomResourceId(randomGenerator);
				int amount = randomGenerator.nextInt(5);
				long expectedLevel = resourcesDataManager.getRegionResourceLevel(regionId, resourceId);
				expectedLevel += amount;
				resourcesDataManager.addResourceToRegion(resourceId, regionId, amount);
				long actualLevel = resourcesDataManager.getRegionResourceLevel(regionId, resourceId);
				assertEquals(expectedLevel, actualLevel);
			}

		});
		TestSimulation.builder().addPlugins(factory.getPlugins()).build().execute();

		/* precondition test: if the resource id is null */
		ContractException contractException = assertThrows(ContractException.class, () -> {
			Factory factory2 = ResourcesTestPluginFactory.factory(20, 1436454351032688103L, (c) -> {
				ResourcesDataManager resourcesDataManager = c.getDataManager(ResourcesDataManager.class);
				resourcesDataManager.getRegionResourceLevel(TestRegionId.REGION_1, null);
			});
			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();
		});
		assertEquals(ResourceError.NULL_RESOURCE_ID, contractException.getErrorType());

		/* precondition test: if the resource id is unknown */
		contractException = assertThrows(ContractException.class, () -> {
			Factory factory2 = ResourcesTestPluginFactory.factory(20, 7954290176104108412L, (c) -> {
				ResourcesDataManager resourcesDataManager = c.getDataManager(ResourcesDataManager.class);
				resourcesDataManager.getRegionResourceLevel(TestRegionId.REGION_1,
						TestResourceId.getUnknownResourceId());
			});
			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();
		});
		assertEquals(ResourceError.UNKNOWN_RESOURCE_ID, contractException.getErrorType());

		/* precondition test: if the region id null */
		contractException = assertThrows(ContractException.class, () -> {
			Factory factory2 = ResourcesTestPluginFactory.factory(20, 936653403265146113L, (c) -> {
				ResourcesDataManager resourcesDataManager = c.getDataManager(ResourcesDataManager.class);
				resourcesDataManager.getRegionResourceLevel(null, TestResourceId.RESOURCE_1);
			});
			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();
		});
		assertEquals(RegionError.NULL_REGION_ID, contractException.getErrorType());

		/* precondition test: if the region id is unknown */
		contractException = assertThrows(ContractException.class, () -> {
			Factory factory2 = ResourcesTestPluginFactory.factory(20, 8256630838791330328L, (c) -> {
				ResourcesDataManager resourcesDataManager = c.getDataManager(ResourcesDataManager.class);
				resourcesDataManager.getRegionResourceLevel(TestRegionId.getUnknownRegionId(),
						TestResourceId.RESOURCE_1);
			});
			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();
		});
		assertEquals(RegionError.UNKNOWN_REGION_ID, contractException.getErrorType());
	}

	@Test
	@UnitTestMethod(target = ResourcesDataManager.class, name = "getResourceIds", args = {})
	public void testGetResourceIds() {

		Factory factory = ResourcesTestPluginFactory.factory(5, 2601236547109660988L, (c) -> {
			ResourcesDataManager resourcesDataManager = c.getDataManager(ResourcesDataManager.class);

			// show that the resource ids are the test resource ids
			Set<ResourceId> expectedResourceIds = new LinkedHashSet<>();
			for (TestResourceId testResourceId : TestResourceId.values()) {
				expectedResourceIds.add(testResourceId);
			}
			assertEquals(expectedResourceIds, resourcesDataManager.getResourceIds());

		});
		TestSimulation.builder().addPlugins(factory.getPlugins()).build().execute();
	}

	@Test
	@UnitTestMethod(target = ResourcesDataManager.class, name = "getResourcePropertyDefinition", args = {
			ResourceId.class, ResourcePropertyId.class })
	public void testGetResourcePropertyDefinition() {

		Factory factory = ResourcesTestPluginFactory.factory(5, 7619546908709928867L, (c) -> {
			ResourcesDataManager resourcesDataManager = c.getDataManager(ResourcesDataManager.class);
			// show that each of the resource property definitions from the test
			// resource property enum are present
			for (TestResourcePropertyId testResourcePropertyId : TestResourcePropertyId.values()) {
				PropertyDefinition expectedPropertyDefinition = testResourcePropertyId.getPropertyDefinition();
				PropertyDefinition actualPropertyDefinition = resourcesDataManager.getResourcePropertyDefinition(
						testResourcePropertyId.getTestResourceId(), testResourcePropertyId);
				assertEquals(expectedPropertyDefinition, actualPropertyDefinition);
			}
		});
		TestSimulation.builder().addPlugins(factory.getPlugins()).build().execute();
	}

	@Test
	@UnitTestMethod(target = ResourcesDataManager.class, name = "getResourcePropertyIds", args = { ResourceId.class })
	public void testGetResourcePropertyIds() {

		Factory factory = ResourcesTestPluginFactory.factory(5, 1203402714876510055L, (c) -> {
			ResourcesDataManager resourcesDataManager = c.getDataManager(ResourcesDataManager.class);
			// show that the resource property ids are the test resource
			// property ids
			for (TestResourceId testResourceId : TestResourceId.values()) {
				Set<TestResourcePropertyId> expectedPropertyIds = TestResourcePropertyId
						.getTestResourcePropertyIds(testResourceId);
				Set<ResourcePropertyId> actualPropertyIds = resourcesDataManager.getResourcePropertyIds(testResourceId);
				assertEquals(expectedPropertyIds, actualPropertyIds);
			}

		});
		TestSimulation.builder().addPlugins(factory.getPlugins()).build().execute();

		/* precondition tests if the resource id is null */
		ContractException contractException = assertThrows(ContractException.class, () -> {
			Factory factory2 = ResourcesTestPluginFactory.factory(5, 3551512082879672269L, (c) -> {
				ResourcesDataManager resourcesDataManager = c.getDataManager(ResourcesDataManager.class);
				resourcesDataManager.getResourcePropertyIds(null);
			});
			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();
		});
		assertEquals(ResourceError.NULL_RESOURCE_ID, contractException.getErrorType());

		/* precondition tests if the resource id unknown */
		contractException = assertThrows(ContractException.class, () -> {
			Factory factory2 = ResourcesTestPluginFactory.factory(5, 7372199991315732905L, (c) -> {
				ResourcesDataManager resourcesDataManager = c.getDataManager(ResourcesDataManager.class);
				resourcesDataManager.getResourcePropertyIds(TestResourceId.getUnknownResourceId());
			});
			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();
		});
		assertEquals(ResourceError.UNKNOWN_RESOURCE_ID, contractException.getErrorType());
	}

	@Test
	@UnitTestMethod(target = ResourcesDataManager.class, name = "getResourcePropertyValue", args = { ResourceId.class,
			ResourcePropertyId.class })
	public void testGetResourcePropertyValue() {

		Factory factory = ResourcesTestPluginFactory.factory(10, 8757871520559824784L, (c) -> {
			ResourcesDataManager resourcesDataManager = c.getDataManager(ResourcesDataManager.class);
			StochasticsDataManager stochasticsDataManager = c.getDataManager(StochasticsDataManager.class);
			RandomGenerator randomGenerator = stochasticsDataManager.getRandomGenerator();

			// establish the expected values of all resource properties
			Map<MultiKey, Object> expectedValues = new LinkedHashMap<>();
			for (TestResourceId testResourceId : TestResourceId.values()) {
				for (TestResourcePropertyId testResourcePropertyId : TestResourcePropertyId
						.getTestResourcePropertyIds(testResourceId)) {
					Object propertyValue = resourcesDataManager.getResourcePropertyValue(testResourceId,
							testResourcePropertyId);
					expectedValues.put(new MultiKey(testResourceId, testResourcePropertyId), propertyValue);
				}
			}

			// make a few random resource property updates
			int updateCount = 0;
			for (int i = 0; i < 1000; i++) {
				TestResourceId testResourceId = TestResourceId.getRandomResourceId(randomGenerator);
				TestResourcePropertyId testResourcePropertyId = TestResourcePropertyId
						.getRandomResourcePropertyId(testResourceId, randomGenerator);
				PropertyDefinition resourcePropertyDefinition = resourcesDataManager
						.getResourcePropertyDefinition(testResourceId, testResourcePropertyId);
				if (resourcePropertyDefinition.propertyValuesAreMutable()) {
					Object propertyValue = testResourcePropertyId.getRandomPropertyValue(randomGenerator);
					resourcesDataManager.setResourcePropertyValue(testResourceId, testResourcePropertyId,
							propertyValue);
					expectedValues.put(new MultiKey(testResourceId, testResourcePropertyId), propertyValue);
					updateCount++;
				}
			}

			/*
			 * Show that the number of updates was reasonable - some of the properties are
			 * not mutable so it will be <1000
			 */
			assertTrue(updateCount > 500);

			// show that the values of the resource properties are correct
			for (MultiKey multiKey : expectedValues.keySet()) {
				TestResourceId testResourceId = multiKey.getKey(0);
				TestResourcePropertyId testResourcePropertyId = multiKey.getKey(1);
				Object expectedValue = expectedValues.get(multiKey);
				Object actualValue = resourcesDataManager.getResourcePropertyValue(testResourceId,
						testResourcePropertyId);
				assertEquals(expectedValue, actualValue);
			}

		});

		TestSimulation.builder().addPlugins(factory.getPlugins()).build().execute();

		/* precondition test: if the resource id is null */
		ContractException contractException = assertThrows(ContractException.class, () -> {
			Factory factory2 = ResourcesTestPluginFactory.factory(10, 5856579804289926491L, (c) -> {
				ResourcesDataManager resourcesDataManager = c.getDataManager(ResourcesDataManager.class);
				resourcesDataManager.getResourcePropertyValue(null,
						TestResourcePropertyId.ResourceProperty_1_1_BOOLEAN_MUTABLE);
			});
			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();
		});
		assertEquals(ResourceError.NULL_RESOURCE_ID, contractException.getErrorType());

		/* precondition test: if the resource id unknown */
		contractException = assertThrows(ContractException.class, () -> {
			Factory factory2 = ResourcesTestPluginFactory.factory(10, 1735955680485266104L, (c) -> {
				ResourcesDataManager resourcesDataManager = c.getDataManager(ResourcesDataManager.class);
				resourcesDataManager.getResourcePropertyValue(TestResourceId.getUnknownResourceId(),
						TestResourcePropertyId.ResourceProperty_1_1_BOOLEAN_MUTABLE);
			});
			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();
		});
		assertEquals(ResourceError.UNKNOWN_RESOURCE_ID, contractException.getErrorType());

		/* precondition test: if the resource property id is null */
		contractException = assertThrows(ContractException.class, () -> {
			Factory factory2 = ResourcesTestPluginFactory.factory(10, 5544999164968796966L, (c) -> {
				ResourcesDataManager resourcesDataManager = c.getDataManager(ResourcesDataManager.class);
				resourcesDataManager.getResourcePropertyValue(TestResourceId.RESOURCE_1, null);
			});
			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();
		});
		assertEquals(PropertyError.NULL_PROPERTY_ID, contractException.getErrorType());

		/* precondition test: if the resource property id unknown */
		contractException = assertThrows(ContractException.class, () -> {
			Factory factory2 = ResourcesTestPluginFactory.factory(10, 3394498124288646142L, (c) -> {
				ResourcesDataManager resourcesDataManager = c.getDataManager(ResourcesDataManager.class);
				resourcesDataManager.getResourcePropertyValue(TestResourceId.RESOURCE_1,
						TestResourcePropertyId.ResourceProperty_2_1_BOOLEAN_MUTABLE);
			});
			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();
		});
		assertEquals(PropertyError.UNKNOWN_PROPERTY_ID, contractException.getErrorType());

		/* precondition test: if the resource property id unknown */
		contractException = assertThrows(ContractException.class, () -> {
			Factory factory2 = ResourcesTestPluginFactory.factory(10, 2505584646755789288L, (c) -> {
				ResourcesDataManager resourcesDataManager = c.getDataManager(ResourcesDataManager.class);
				resourcesDataManager.getResourcePropertyValue(TestResourceId.RESOURCE_1,
						TestResourcePropertyId.getUnknownResourcePropertyId());
			});
			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();
		});
		assertEquals(PropertyError.UNKNOWN_PROPERTY_ID, contractException.getErrorType());

	}

	@Test
	@UnitTestMethod(target = ResourcesDataManager.class, name = "resourceIdExists", args = { ResourceId.class })
	public void testResourceIdExists() {

		Factory factory = ResourcesTestPluginFactory.factory(5, 4964974931601945506L, (c) -> {
			ResourcesDataManager resourcesDataManager = c.getDataManager(ResourcesDataManager.class);

			// show that the resource ids that exist are the test resource ids

			for (TestResourceId testResourceId : TestResourceId.values()) {
				assertTrue(resourcesDataManager.resourceIdExists(testResourceId));
			}
			assertFalse(resourcesDataManager.resourceIdExists(TestResourceId.getUnknownResourceId()));
			assertFalse(resourcesDataManager.resourceIdExists(null));

		});
		TestSimulation.builder().addPlugins(factory.getPlugins()).build().execute();
	}

	@Test
	@UnitTestMethod(target = ResourcesDataManager.class, name = "resourcePropertyIdExists", args = { ResourceId.class,
			ResourcePropertyId.class })
	public void testResourcePropertyIdExists() {

		Factory factory = ResourcesTestPluginFactory.factory(5, 8074706630609416041L, (c) -> {
			ResourcesDataManager resourcesDataManager = c.getDataManager(ResourcesDataManager.class);

			// show that the resource property ids that exist are the test
			// resource property ids

			for (TestResourcePropertyId testResourcePropertyId : TestResourcePropertyId.values()) {
				assertTrue(resourcesDataManager.resourcePropertyIdExists(testResourcePropertyId.getTestResourceId(),
						testResourcePropertyId));
			}

			assertFalse(resourcesDataManager.resourcePropertyIdExists(TestResourceId.RESOURCE_1,
					TestResourcePropertyId.ResourceProperty_2_1_BOOLEAN_MUTABLE));
			assertFalse(resourcesDataManager.resourcePropertyIdExists(null,
					TestResourcePropertyId.ResourceProperty_2_1_BOOLEAN_MUTABLE));
			assertFalse(resourcesDataManager.resourcePropertyIdExists(TestResourceId.RESOURCE_1, null));
			assertFalse(resourcesDataManager.resourcePropertyIdExists(TestResourceId.RESOURCE_1,
					TestResourcePropertyId.getUnknownResourcePropertyId()));
			assertFalse(resourcesDataManager.resourcePropertyIdExists(TestResourceId.getUnknownResourceId(),
					TestResourcePropertyId.ResourceProperty_1_2_INTEGER_MUTABLE));
			assertFalse(resourcesDataManager.resourcePropertyIdExists(TestResourceId.getUnknownResourceId(),
					TestResourcePropertyId.getUnknownResourcePropertyId()));

		});
		TestSimulation.builder().addPlugins(factory.getPlugins()).build().execute();
	}

	@Test
	@UnitTestMethod(target = ResourcesDataManager.class, name = "defineResourceProperty", args = {
			ResourcePropertyInitialization.class })

	public void testDefineResourceProperty() {

		TestPluginData.Builder pluginBuilder = TestPluginData.builder();

		Set<MultiKey> expectedObservations = new LinkedHashSet<>();
		Set<MultiKey> actualObservations = new LinkedHashSet<>();

		// have an actor observe the ResourcePropertyAdditionEvent events
		pluginBuilder.addTestActorPlan("observer", new TestActorPlan(0, (c) -> {
			c.subscribe(EventFilter.builder(ResourcePropertyDefinitionEvent.class).build(), (c2, e) -> {
				MultiKey multiKey = new MultiKey(c2.getTime(), e.resourceId(), e.resourcePropertyId(),
						e.resourcePropertyValue());
				actualObservations.add(multiKey);
			});
		}));

		// have an actor define a new resource property
		pluginBuilder.addTestActorPlan("actor", new TestActorPlan(1, (c) -> {
			ResourcesDataManager resourcesDataManager = c.getDataManager(ResourcesDataManager.class);
			ResourcePropertyId newResourcePropertyId = TestResourcePropertyId.getUnknownResourcePropertyId();
			PropertyDefinition propertyDefinition = PropertyDefinition.builder().setType(Double.class)
					.setDefaultValue(34.6).build();
			ResourcePropertyInitialization resourcePropertyInitialization = //
					ResourcePropertyInitialization.builder()//
							.setPropertyDefinition(propertyDefinition)//
							.setResourceId(TestResourceId.RESOURCE_1)//
							.setResourcePropertyId(newResourcePropertyId)//
							.build();
			resourcesDataManager.defineResourceProperty(resourcePropertyInitialization);
			assertTrue(resourcesDataManager.resourcePropertyIdExists(TestResourceId.RESOURCE_1, newResourcePropertyId));
			PropertyDefinition actualDefinition = resourcesDataManager
					.getResourcePropertyDefinition(TestResourceId.RESOURCE_1, newResourcePropertyId);
			assertEquals(propertyDefinition, actualDefinition);
			MultiKey multiKey = new MultiKey(c.getTime(), TestResourceId.RESOURCE_1, newResourcePropertyId,
					propertyDefinition.getDefaultValue().get());
			expectedObservations.add(multiKey);
		}));

		// have an actor define a new resource property
		pluginBuilder.addTestActorPlan("actor", new TestActorPlan(2, (c) -> {
			ResourcesDataManager resourcesDataManager = c.getDataManager(ResourcesDataManager.class);
			ResourcePropertyId newResourcePropertyId = TestResourcePropertyId.getUnknownResourcePropertyId();
			PropertyDefinition propertyDefinition = PropertyDefinition.builder().setType(String.class)
					.setDefaultValue("default").build();

			ResourcePropertyInitialization resourcePropertyInitialization = //
					ResourcePropertyInitialization.builder()//
							.setPropertyDefinition(propertyDefinition)//
							.setResourceId(TestResourceId.RESOURCE_2)//
							.setResourcePropertyId(newResourcePropertyId)//
							.build();
			resourcesDataManager.defineResourceProperty(resourcePropertyInitialization);

			assertTrue(resourcesDataManager.resourcePropertyIdExists(TestResourceId.RESOURCE_2, newResourcePropertyId));
			PropertyDefinition actualDefinition = resourcesDataManager
					.getResourcePropertyDefinition(TestResourceId.RESOURCE_2, newResourcePropertyId);
			assertEquals(propertyDefinition, actualDefinition);
			MultiKey multiKey = new MultiKey(c.getTime(), TestResourceId.RESOURCE_2, newResourcePropertyId,
					propertyDefinition.getDefaultValue().get());
			expectedObservations.add(multiKey);
		}));

		// have the observer verify the observations were correct
		pluginBuilder.addTestActorPlan("observer", new TestActorPlan(3, (c) -> {
			assertEquals(2, expectedObservations.size());
			assertEquals(expectedObservations, actualObservations);
		}));

		TestPluginData testPluginData = pluginBuilder.build();
		Factory factory = ResourcesTestPluginFactory.factory(5, 4535415202634885293L, testPluginData);
		TestSimulation.builder().addPlugins(factory.getPlugins()).build().execute();

		/* precondition test: if the resource id is unknown */
		ContractException contractException = assertThrows(ContractException.class, () -> {
			Factory factory2 = ResourcesTestPluginFactory.factory(5, 6361316703720629700L, (c) -> {
				ResourcesDataManager resourcesDataManager = c.getDataManager(ResourcesDataManager.class);
				PropertyDefinition propertyDefinition = PropertyDefinition.builder().setType(Integer.class)
						.setDefaultValue(1).build();
				ResourcePropertyInitialization resourcePropertyInitialization = //
						ResourcePropertyInitialization.builder()//
								.setPropertyDefinition(propertyDefinition)//
								.setResourceId(TestResourceId.getUnknownResourceId())//
								.setResourcePropertyId(TestResourcePropertyId.getUnknownResourcePropertyId())//
								.build();
				resourcesDataManager.defineResourceProperty(resourcePropertyInitialization);
			});

			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();
		});
		assertEquals(ResourceError.UNKNOWN_RESOURCE_ID, contractException.getErrorType());

		/* precondition test: if the resource property is already defined */
		contractException = assertThrows(ContractException.class, () -> {
			Factory factory2 = ResourcesTestPluginFactory.factory(5, 3114198987897928160L, (c) -> {
				ResourcesDataManager resourcesDataManager = c.getDataManager(ResourcesDataManager.class);
				PropertyDefinition propertyDefinition = PropertyDefinition.builder().setType(Integer.class)
						.setDefaultValue(1).build();
				ResourcePropertyInitialization resourcePropertyInitialization = //
						ResourcePropertyInitialization.builder()//
								.setPropertyDefinition(propertyDefinition)//
								.setResourceId(TestResourceId.RESOURCE_1)//
								.setResourcePropertyId(TestResourcePropertyId.ResourceProperty_1_2_INTEGER_MUTABLE)//
								.build();
				resourcesDataManager.defineResourceProperty(resourcePropertyInitialization);
			});
			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();
		});
		assertEquals(PropertyError.DUPLICATE_PROPERTY_DEFINITION, contractException.getErrorType());

	}

	@Test
	@UnitTestMethod(target = ResourcesDataManager.class, name = "addResourceId", args = { ResourceId.class,
			boolean.class })
	public void testAddResourceId() {

		ResourceId newResourceId1 = TestResourceId.getUnknownResourceId();
		ResourceId newResourceId2 = TestResourceId.getUnknownResourceId();
		Set<MultiKey> expectedObservations = new LinkedHashSet<>();
		Set<MultiKey> actualObservations = new LinkedHashSet<>();

		TestPluginData.Builder pluginBuilder = TestPluginData.builder();

		pluginBuilder.addTestActorPlan("observer", new TestActorPlan(0, (c) -> {
			c.subscribe(EventFilter.builder(ResourceIdAdditionEvent.class).build(), (c2, e) -> {
				MultiKey multiKey = new MultiKey(c2.getTime(), e.resourceId(), e.timeTrackingPolicy());
				actualObservations.add(multiKey);
			});

		}));

		pluginBuilder.addTestActorPlan("actor", new TestActorPlan(1, (c) -> {
			ResourcesDataManager resourcesDataManager = c.getDataManager(ResourcesDataManager.class);
			boolean timeTrackingPolicy = false;
			assertFalse(resourcesDataManager.resourceIdExists(newResourceId1));
			resourcesDataManager.addResourceId(newResourceId1, timeTrackingPolicy);
			assertTrue(resourcesDataManager.resourceIdExists(newResourceId1));
			MultiKey multiKey = new MultiKey(c.getTime(), newResourceId1, false);
			expectedObservations.add(multiKey);
		}));

		pluginBuilder.addTestActorPlan("actor", new TestActorPlan(2, (c) -> {
			ResourcesDataManager resourcesDataManager = c.getDataManager(ResourcesDataManager.class);
			boolean timeTrackingPolicy = true;
			assertFalse(resourcesDataManager.resourceIdExists(newResourceId2));
			resourcesDataManager.addResourceId(newResourceId2, timeTrackingPolicy);
			assertTrue(resourcesDataManager.resourceIdExists(newResourceId2));
			MultiKey multiKey = new MultiKey(c.getTime(), newResourceId2, true);
			expectedObservations.add(multiKey);
		}));

		pluginBuilder.addTestActorPlan("observer", new TestActorPlan(3, (c) -> {
			assertEquals(expectedObservations, actualObservations);
		}));

		TestPluginData testPluginData = pluginBuilder.build();
		Factory factory = ResourcesTestPluginFactory.factory(5, 3128266603988900429L, testPluginData);
		TestSimulation.builder().addPlugins(factory.getPlugins()).build().execute();

		// precondition test: if the resource id is null
		ContractException contractException = assertThrows(ContractException.class, () -> {
			Factory factory2 = ResourcesTestPluginFactory.factory(5, 3016555021220987436L, (c) -> {
				ResourcesDataManager resourcesDataManager = c.getDataManager(ResourcesDataManager.class);
				resourcesDataManager.addResourceId(null, false);
			});
			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();
		});
		assertEquals(ResourceError.NULL_RESOURCE_ID, contractException.getErrorType());

		// precondition test: if the resource type is already present
		contractException = assertThrows(ContractException.class, () -> {
			Factory factory2 = ResourcesTestPluginFactory.factory(5, 9097839209339012193L, (c) -> {
				ResourcesDataManager resourcesDataManager = c.getDataManager(ResourcesDataManager.class);
				resourcesDataManager.addResourceId(TestResourceId.RESOURCE_1, false);
			});
			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();
		});
		assertEquals(ResourceError.DUPLICATE_RESOURCE_ID, contractException.getErrorType());

	}

	@Test
	@UnitTestMethod(target = ResourcesDataManager.class, name = "setResourcePropertyValue", args = { ResourceId.class,
			ResourcePropertyId.class, Object.class })
	public void testSetResourcePropertyValue() {
		TestPluginData.Builder pluginBuilder = TestPluginData.builder();

		Set<MultiKey> expectedObservations = new LinkedHashSet<>();
		Set<MultiKey> actualObservations = new LinkedHashSet<>();

		// Have an actor observe the resource property changes

		pluginBuilder.addTestActorPlan("observer", new TestActorPlan(0, (c) -> {
			ResourcesDataManager resourcesDataManager = c.getDataManager(ResourcesDataManager.class);
			for (TestResourceId testResourceId : TestResourceId.values()) {
				Set<TestResourcePropertyId> testResourcePropertyIds = TestResourcePropertyId
						.getTestResourcePropertyIds(testResourceId);
				for (TestResourcePropertyId testResourcePropertyId : testResourcePropertyIds) {
					EventFilter<ResourcePropertyUpdateEvent> eventFilter = resourcesDataManager
							.getEventFilterForResourcePropertyUpdateEvent(testResourceId, testResourcePropertyId);
					c.subscribe(eventFilter, (c2, e) -> {
						actualObservations.add(new MultiKey(e.resourceId(), e.resourcePropertyId(),
								e.previousPropertyValue(), e.currentPropertyValue()));
					});
				}
			}
		}));

		// Have an actor assign resource properties

		pluginBuilder.addTestActorPlan("actor", new TestActorPlan(0, (c) -> {

			StochasticsDataManager stochasticsDataManager = c.getDataManager(StochasticsDataManager.class);
			RandomGenerator randomGenerator = stochasticsDataManager.getRandomGenerator();
			ResourcesDataManager resourcesDataManager = c.getDataManager(ResourcesDataManager.class);

			for (TestResourceId testResourceId : TestResourceId.values()) {
				Set<TestResourcePropertyId> testResourcePropertyIds = TestResourcePropertyId
						.getTestResourcePropertyIds(testResourceId);
				for (TestResourcePropertyId testResourcePropertyId : testResourcePropertyIds) {
					PropertyDefinition propertyDefinition = resourcesDataManager
							.getResourcePropertyDefinition(testResourceId, testResourcePropertyId);
					if (propertyDefinition.propertyValuesAreMutable()) {
						// update the property value
						Object resourcePropertyValue = resourcesDataManager.getResourcePropertyValue(testResourceId,
								testResourcePropertyId);
						Object expectedValue = testResourcePropertyId.getRandomPropertyValue(randomGenerator);
						resourcesDataManager.setResourcePropertyValue(testResourceId, testResourcePropertyId,
								expectedValue);
						// show that the property value was changed
						Object actualValue = resourcesDataManager.getResourcePropertyValue(testResourceId,
								testResourcePropertyId);
						assertEquals(expectedValue, actualValue);

						expectedObservations.add(new MultiKey(testResourceId, testResourcePropertyId,
								resourcePropertyValue, expectedValue));
					}
				}
			}

		}));

		// Have the observer show the the observations were properly generated
		pluginBuilder.addTestActorPlan("actor", new TestActorPlan(0, (c) -> {
			assertEquals(expectedObservations, actualObservations);
		}));

		TestPluginData testPluginData = pluginBuilder.build();
		Factory factory = ResourcesTestPluginFactory.factory(0, 8240654442453940072L, testPluginData);
		TestSimulation.builder().addPlugins(factory.getPlugins()).build().execute();

		/* precondition test: if the resource id is null */
		ContractException contractException = assertThrows(ContractException.class, () -> {
			Factory factory2 = ResourcesTestPluginFactory.factory(0, 8603231391482244436L, (c) -> {
				ResourcePropertyId resourcePropertyId = TestResourcePropertyId.ResourceProperty_1_2_INTEGER_MUTABLE;
				ResourcesDataManager resourcesDataManager = c.getDataManager(ResourcesDataManager.class);
				Object value = 10;
				resourcesDataManager.setResourcePropertyValue(null, resourcePropertyId, value);
			});
			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();
		});
		assertEquals(ResourceError.NULL_RESOURCE_ID, contractException.getErrorType());

		/* precondition test: if the resource id is unknown */
		contractException = assertThrows(ContractException.class, () -> {
			Factory factory2 = ResourcesTestPluginFactory.factory(0, 4345368701918830681L, (c) -> {
				ResourcePropertyId resourcePropertyId = TestResourcePropertyId.ResourceProperty_1_2_INTEGER_MUTABLE;
				ResourcesDataManager resourcesDataManager = c.getDataManager(ResourcesDataManager.class);
				Object value = 10;
				resourcesDataManager.setResourcePropertyValue(TestResourceId.getUnknownResourceId(), resourcePropertyId,
						value);
			});
			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();
		});
		assertEquals(ResourceError.UNKNOWN_RESOURCE_ID, contractException.getErrorType());

		/* precondition test: if the resource property id is null */
		contractException = assertThrows(ContractException.class, () -> {
			Factory factory2 = ResourcesTestPluginFactory.factory(0, 697099694521127247L, (c) -> {
				ResourceId resourceId = TestResourceId.RESOURCE_1;
				ResourcesDataManager resourcesDataManager = c.getDataManager(ResourcesDataManager.class);
				Object value = 10;
				resourcesDataManager.setResourcePropertyValue(resourceId, null, value);
			});
			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();
		});
		assertEquals(PropertyError.NULL_PROPERTY_ID, contractException.getErrorType());

		/* precondition test: if the resource property id is unknown */
		contractException = assertThrows(ContractException.class, () -> {
			Factory factory2 = ResourcesTestPluginFactory.factory(0, 5208483875882077960L, (c) -> {
				ResourceId resourceId = TestResourceId.RESOURCE_1;
				ResourcesDataManager resourcesDataManager = c.getDataManager(ResourcesDataManager.class);
				Object value = 10;
				resourcesDataManager.setResourcePropertyValue(resourceId,
						TestResourcePropertyId.getUnknownResourcePropertyId(), value);
			});
			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();
		});
		assertEquals(PropertyError.UNKNOWN_PROPERTY_ID, contractException.getErrorType());

		/* precondition test: if the resource property value is null */
		contractException = assertThrows(ContractException.class, () -> {
			Factory factory2 = ResourcesTestPluginFactory.factory(0, 1862818482356534123L, (c) -> {
				ResourceId resourceId = TestResourceId.RESOURCE_1;
				ResourcePropertyId resourcePropertyId = TestResourcePropertyId.ResourceProperty_1_2_INTEGER_MUTABLE;
				ResourcesDataManager resourcesDataManager = c.getDataManager(ResourcesDataManager.class);
				resourcesDataManager.setResourcePropertyValue(resourceId, resourcePropertyId, null);
			});
			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();
		});
		assertEquals(PropertyError.NULL_PROPERTY_VALUE, contractException.getErrorType());

		/*
		 * precondition test: if the resource property value is incompatible with the
		 * corresponding property definition
		 */
		contractException = assertThrows(ContractException.class, () -> {
			Factory factory2 = ResourcesTestPluginFactory.factory(0, 8731358919842250070L, (c) -> {
				ResourceId resourceId = TestResourceId.RESOURCE_1;
				ResourcePropertyId resourcePropertyId = TestResourcePropertyId.ResourceProperty_1_2_INTEGER_MUTABLE;
				ResourcesDataManager resourcesDataManager = c.getDataManager(ResourcesDataManager.class);
				resourcesDataManager.setResourcePropertyValue(resourceId, resourcePropertyId, 23.4);
			});
			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();
		});
		assertEquals(PropertyError.INCOMPATIBLE_VALUE, contractException.getErrorType());

		/* precondition test: if the property has been defined as immutable */
		contractException = assertThrows(ContractException.class, () -> {
			Factory factory2 = ResourcesTestPluginFactory.factory(0, 2773568485593496806L, (c) -> {
				ResourcesDataManager resourcesDataManager = c.getDataManager(ResourcesDataManager.class);
				Object value = 10;
				resourcesDataManager.setResourcePropertyValue(TestResourceId.RESOURCE_5,
						TestResourcePropertyId.ResourceProperty_5_1_INTEGER_IMMUTABLE, value);
			});
			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();
		});
		assertEquals(PropertyError.IMMUTABLE_VALUE, contractException.getErrorType());

	}

	@Test
	@UnitTestMethod(target = ResourcesDataManager.class, name = "removeResourceFromPerson", args = { ResourceId.class,
			PersonId.class, long.class })
	public void testPersonResourceRemovalEvent() {

		TestPluginData.Builder pluginBuilder = TestPluginData.builder();

		Set<MultiKey> expectedObservations = new LinkedHashSet<>();
		Set<MultiKey> actualObservations = new LinkedHashSet<>();

		// Have and actor give resources to people

		pluginBuilder.addTestActorPlan("actor", new TestActorPlan(0, (c) -> {
			ResourcesDataManager resourcesDataManager = c.getDataManager(ResourcesDataManager.class);
			PeopleDataManager peopleDataManager = c.getDataManager(PeopleDataManager.class);
			RegionsDataManager regionsDataManager = c.getDataManager(RegionsDataManager.class);

			List<PersonId> people = peopleDataManager.getPeople();
			// add resources to all the people
			for (PersonId personId : people) {
				for (TestResourceId testResourceId : TestResourceId.values()) {
					RegionId regionId = regionsDataManager.getPersonRegion(personId);
					resourcesDataManager.addResourceToRegion(testResourceId, regionId, 100L);
					resourcesDataManager.transferResourceToPersonFromRegion(testResourceId, personId, 100L);
				}
			}
		}));

		// have an actor observe the changes to person resources
		pluginBuilder.addTestActorPlan("observer", new TestActorPlan(1, (c) -> {
			ResourcesDataManager resourcesDataManager = c.getDataManager(ResourcesDataManager.class);
			for (TestResourceId testResourceId : TestResourceId.values()) {
				EventFilter<PersonResourceUpdateEvent> eventFilter = resourcesDataManager
						.getEventFilterForPersonResourceUpdateEvent(testResourceId);
				c.subscribe(eventFilter, (c2, e) -> {
					actualObservations.add(new MultiKey(e.personId(), e.resourceId(), e.previousResourceLevel(),
							e.currentResourceLevel()));
				});
			}
		}));

		// Have the actor remove resources from people
		pluginBuilder.addTestActorPlan("actor", new TestActorPlan(2, (c) -> {

			StochasticsDataManager stochasticsDataManager = c.getDataManager(StochasticsDataManager.class);
			RandomGenerator randomGenerator = stochasticsDataManager.getRandomGenerator();
			ResourcesDataManager resourcesDataManager = c.getDataManager(ResourcesDataManager.class);
			PeopleDataManager peopleDataManager = c.getDataManager(PeopleDataManager.class);

			List<PersonId> people = peopleDataManager.getPeople();

			// remove random amounts of resources from people
			int transfercount = 0;
			for (int i = 0; i < 40; i++) {
				// select a random person and resource
				PersonId personId = people.get(randomGenerator.nextInt(people.size()));
				TestResourceId resourceId = TestResourceId.getRandomResourceId(randomGenerator);

				long personResourceLevel = resourcesDataManager.getPersonResourceLevel(resourceId, personId);
				// ensure that the person has a positive amount of the resource
				if (personResourceLevel > 0) {

					// select an amount to remove
					long amount = randomGenerator.nextInt((int) personResourceLevel) + 1;
					resourcesDataManager.removeResourceFromPerson(resourceId, personId, amount);
					transfercount++;

					// show that the amount was transferred
					long expectedPersonResourceLevel = personResourceLevel - amount;
					long actualPersonResourceLevel = resourcesDataManager.getPersonResourceLevel(resourceId, personId);
					assertEquals(expectedPersonResourceLevel, actualPersonResourceLevel);

					expectedObservations
							.add(new MultiKey(personId, resourceId, personResourceLevel, expectedPersonResourceLevel));

				}

			}

			// show that enough transfers occurred to make a valid test
			assertTrue(transfercount > 10);

		}));

		// Have the observer show that the generated observations were correct
		pluginBuilder.addTestActorPlan("observer", new TestActorPlan(3, (c) -> {
			assertEquals(expectedObservations, actualObservations);
		}));

		TestPluginData testPluginData = pluginBuilder.build();
		Factory factory = ResourcesTestPluginFactory.factory(50, 6476360369877622233L, testPluginData);
		TestSimulation.builder().addPlugins(factory.getPlugins()).build().execute();

		/* precondition test: if the person id is null */
		ContractException contractException = assertThrows(ContractException.class, () -> {
			Factory factory2 = ResourcesTestPluginFactory.factory(50, 368123167921446410L, (c) -> {
				RegionsDataManager regionsDataManager = c.getDataManager(RegionsDataManager.class);
				ResourcesDataManager resourcesDataManager = c.getDataManager(ResourcesDataManager.class);
				ResourceId resourceId = TestResourceId.RESOURCE_1;
				PersonId personId = new PersonId(0);
				long amount = 10;
				// add resource to the person to ensure the precondition tests
				// will
				// work
				RegionId regionId = regionsDataManager.getPersonRegion(personId);
				resourcesDataManager.addResourceToRegion(resourceId, regionId, 100L);
				resourcesDataManager.transferResourceToPersonFromRegion(resourceId, personId, 100L);
				resourcesDataManager.removeResourceFromPerson(resourceId, null, amount);
			});
			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();
		});
		assertEquals(PersonError.NULL_PERSON_ID, contractException.getErrorType());

		/* precondition test: if the person does not exist */
		contractException = assertThrows(ContractException.class, () -> {
			Factory factory2 = ResourcesTestPluginFactory.factory(50, 463919801005664846L, (c) -> {
				RegionsDataManager regionsDataManager = c.getDataManager(RegionsDataManager.class);
				ResourcesDataManager resourcesDataManager = c.getDataManager(ResourcesDataManager.class);
				ResourceId resourceId = TestResourceId.RESOURCE_1;
				PersonId personId = new PersonId(0);
				long amount = 10;
				// add resource to the person to ensure the precondition tests
				// will
				// work
				RegionId regionId = regionsDataManager.getPersonRegion(personId);
				resourcesDataManager.addResourceToRegion(resourceId, regionId, 100L);
				resourcesDataManager.transferResourceToPersonFromRegion(resourceId, personId, 100L);
				resourcesDataManager.removeResourceFromPerson(resourceId, new PersonId(1000), amount);
			});
			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();
		});
		assertEquals(PersonError.UNKNOWN_PERSON_ID, contractException.getErrorType());

		/* precondition test: if the resource id is null */
		contractException = assertThrows(ContractException.class, () -> {
			Factory factory2 = ResourcesTestPluginFactory.factory(50, 5201087860428100698L, (c) -> {
				RegionsDataManager regionsDataManager = c.getDataManager(RegionsDataManager.class);
				ResourcesDataManager resourcesDataManager = c.getDataManager(ResourcesDataManager.class);
				ResourceId resourceId = TestResourceId.RESOURCE_1;
				PersonId personId = new PersonId(0);
				long amount = 10;
				// add resource to the person to ensure the precondition tests
				// will
				// work
				RegionId regionId = regionsDataManager.getPersonRegion(personId);
				resourcesDataManager.addResourceToRegion(resourceId, regionId, 100L);
				resourcesDataManager.transferResourceToPersonFromRegion(resourceId, personId, 100L);
				resourcesDataManager.removeResourceFromPerson(null, personId, amount);
			});
			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();
		});
		assertEquals(ResourceError.NULL_RESOURCE_ID, contractException.getErrorType());

		/* precondition test: if the resource id is unknown */
		contractException = assertThrows(ContractException.class, () -> {
			Factory factory2 = ResourcesTestPluginFactory.factory(50, 805801782412801541L, (c) -> {
				RegionsDataManager regionsDataManager = c.getDataManager(RegionsDataManager.class);
				ResourcesDataManager resourcesDataManager = c.getDataManager(ResourcesDataManager.class);
				ResourceId resourceId = TestResourceId.RESOURCE_1;
				PersonId personId = new PersonId(0);
				long amount = 10;
				// add resource to the person to ensure the precondition tests
				// will
				// work
				RegionId regionId = regionsDataManager.getPersonRegion(personId);
				resourcesDataManager.addResourceToRegion(resourceId, regionId, 100L);
				resourcesDataManager.transferResourceToPersonFromRegion(resourceId, personId, 100L);
				resourcesDataManager.removeResourceFromPerson(TestResourceId.getUnknownResourceId(), personId, amount);
			});
			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();
		});
		assertEquals(ResourceError.UNKNOWN_RESOURCE_ID, contractException.getErrorType());

		/* precondition test: if the amount is negative */
		contractException = assertThrows(ContractException.class, () -> {
			Factory factory2 = ResourcesTestPluginFactory.factory(50, 6748548509217290999L, (c) -> {
				RegionsDataManager regionsDataManager = c.getDataManager(RegionsDataManager.class);
				ResourcesDataManager resourcesDataManager = c.getDataManager(ResourcesDataManager.class);
				ResourceId resourceId = TestResourceId.RESOURCE_1;
				PersonId personId = new PersonId(0);
				// add resource to the person to ensure the precondition tests
				// will
				// work
				RegionId regionId = regionsDataManager.getPersonRegion(personId);
				resourcesDataManager.addResourceToRegion(resourceId, regionId, 100L);
				resourcesDataManager.transferResourceToPersonFromRegion(resourceId, personId, 100L);
				resourcesDataManager.removeResourceFromPerson(resourceId, personId, -1);
			});
			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();
		});
		assertEquals(ResourceError.NEGATIVE_RESOURCE_AMOUNT, contractException.getErrorType());

		/*
		 * precondition test: if the person does not have the required amount of the
		 * resource
		 */
		contractException = assertThrows(ContractException.class, () -> {
			Factory factory2 = ResourcesTestPluginFactory.factory(50, 6668079690803354725L, (c) -> {
				RegionsDataManager regionsDataManager = c.getDataManager(RegionsDataManager.class);
				ResourcesDataManager resourcesDataManager = c.getDataManager(ResourcesDataManager.class);
				ResourceId resourceId = TestResourceId.RESOURCE_1;
				PersonId personId = new PersonId(0);
				// add resource to the person to ensure the precondition tests
				// will
				// work
				RegionId regionId = regionsDataManager.getPersonRegion(personId);
				resourcesDataManager.addResourceToRegion(resourceId, regionId, 100L);
				resourcesDataManager.transferResourceToPersonFromRegion(resourceId, personId, 100L);
				resourcesDataManager.removeResourceFromPerson(resourceId, personId, 10000);
			});
			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();
		});
		assertEquals(ResourceError.INSUFFICIENT_RESOURCES_AVAILABLE, contractException.getErrorType());

	}

	@Test
	@UnitTestMethod(target = ResourcesDataManager.class, name = "removeResourceFromRegion", args = { ResourceId.class,
			RegionId.class, long.class })
	public void testRegionResourceRemovalEvent() {

		TestPluginData.Builder pluginBuilder = TestPluginData.builder();

		Set<MultiKey> expectedObservations = new LinkedHashSet<>();
		Set<MultiKey> actualObservations = new LinkedHashSet<>();

		// Have the actor add resources to the regions
		pluginBuilder.addTestActorPlan("actor", new TestActorPlan(0, (c) -> {
			ResourcesDataManager resourcesDataManager = c.getDataManager(ResourcesDataManager.class);
			// add resources to the regions
			for (TestRegionId testRegionId : TestRegionId.values()) {
				for (TestResourceId testResourceId : TestResourceId.values()) {
					resourcesDataManager.addResourceToRegion(testResourceId, testRegionId, 100L);
				}
			}

		}));

		// Have an actor observe the resource being removed from regions

		pluginBuilder.addTestActorPlan("observer", new TestActorPlan(1, (c) -> {
			ResourcesDataManager resourcesDataManager = c.getDataManager(ResourcesDataManager.class);
			for (TestRegionId testRegionId : TestRegionId.values()) {
				for (TestResourceId testResourceId : TestResourceId.values()) {
					EventFilter<RegionResourceUpdateEvent> eventFilter = resourcesDataManager
							.getEventFilterForRegionResourceUpdateEvent(testResourceId, testRegionId);
					c.subscribe(eventFilter, (c2, e) -> {
						actualObservations.add(new MultiKey(e.regionId(), e.resourceId(), e.previousResourceLevel(),
								e.currentResourceLevel()));
					});
				}
			}

		}));

		// Have the actor remove resources from regions
		pluginBuilder.addTestActorPlan("actor", new TestActorPlan(2, (c) -> {

			StochasticsDataManager stochasticsDataManager = c.getDataManager(StochasticsDataManager.class);
			RandomGenerator randomGenerator = stochasticsDataManager.getRandomGenerator();
			ResourcesDataManager resourcesDataManager = c.getDataManager(ResourcesDataManager.class);

			// remove random amounts of resources from regions
			int transfercount = 0;
			for (int i = 0; i < 40; i++) {
				// select random regions and resources
				TestRegionId regionId = TestRegionId.getRandomRegionId(randomGenerator);
				TestResourceId resourceId = TestResourceId.getRandomResourceId(randomGenerator);
				long regionLevel = resourcesDataManager.getRegionResourceLevel(regionId, resourceId);

				if (regionLevel > 0) {
					// select an amount to add
					long amount = randomGenerator.nextInt((int) regionLevel) + 1;
					resourcesDataManager.removeResourceFromRegion(resourceId, regionId, amount);
					transfercount++;

					// show that the amount was added
					long expectedRegionLevel = regionLevel - amount;
					long actualRegionLevel = resourcesDataManager.getRegionResourceLevel(regionId, resourceId);
					assertEquals(expectedRegionLevel, actualRegionLevel);

					expectedObservations.add(new MultiKey(regionId, resourceId, regionLevel, expectedRegionLevel));
				}
			}

			// show that enough removals occurred to make a valid test
			assertTrue(transfercount > 10);

		}));

		// Have the observer show that the observations were correctly generated
		pluginBuilder.addTestActorPlan("observer", new TestActorPlan(3, (c) -> {
			assertEquals(expectedObservations, actualObservations);
		}));

		TestPluginData testPluginData = pluginBuilder.build();
		Factory factory = ResourcesTestPluginFactory.factory(0, 3784957617927969790L, testPluginData);
		TestSimulation.builder().addPlugins(factory.getPlugins()).build().execute();

		/* precondition test: if the region id is null */
		ContractException contractException = assertThrows(ContractException.class, () -> {
			Factory factory2 = ResourcesTestPluginFactory.factory(0, 5886805948424471010L, (c) -> {
				ResourcesDataManager resourcesDataManager = c.getDataManager(ResourcesDataManager.class);
				ResourceId resourceId = TestResourceId.RESOURCE_1;
				long amount = 10;
				resourcesDataManager.removeResourceFromRegion(resourceId, null, amount);
			});
			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();
		});
		assertEquals(RegionError.NULL_REGION_ID, contractException.getErrorType());

		/* precondition test: if the region id is unknown */
		contractException = assertThrows(ContractException.class, () -> {
			Factory factory2 = ResourcesTestPluginFactory.factory(0, 1916159097321882678L, (c) -> {
				ResourcesDataManager resourcesDataManager = c.getDataManager(ResourcesDataManager.class);
				ResourceId resourceId = TestResourceId.RESOURCE_1;
				long amount = 10;
				resourcesDataManager.removeResourceFromRegion(resourceId, TestRegionId.getUnknownRegionId(), amount);
			});
			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();
		});
		assertEquals(RegionError.UNKNOWN_REGION_ID, contractException.getErrorType());

		/* precondition test: if the resource id is null */
		contractException = assertThrows(ContractException.class, () -> {
			Factory factory2 = ResourcesTestPluginFactory.factory(0, 6766634049148364532L, (c) -> {
				ResourcesDataManager resourcesDataManager = c.getDataManager(ResourcesDataManager.class);
				RegionId regionId = TestRegionId.REGION_1;
				long amount = 10;
				resourcesDataManager.removeResourceFromRegion(null, regionId, amount);
			});
			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();
		});
		assertEquals(ResourceError.NULL_RESOURCE_ID, contractException.getErrorType());

		/* precondition test: if the resource id is unknown */
		contractException = assertThrows(ContractException.class, () -> {
			Factory factory2 = ResourcesTestPluginFactory.factory(0, 3589045787461097821L, (c) -> {
				ResourcesDataManager resourcesDataManager = c.getDataManager(ResourcesDataManager.class);
				RegionId regionId = TestRegionId.REGION_1;
				long amount = 10;
				resourcesDataManager.removeResourceFromRegion(TestResourceId.getUnknownResourceId(), regionId, amount);
			});
			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();
		});
		assertEquals(ResourceError.UNKNOWN_RESOURCE_ID, contractException.getErrorType());

		/* precondition test: if the amount is negative */
		contractException = assertThrows(ContractException.class, () -> {
			Factory factory2 = ResourcesTestPluginFactory.factory(0, 4784578124305542584L, (c) -> {
				ResourcesDataManager resourcesDataManager = c.getDataManager(ResourcesDataManager.class);
				ResourceId resourceId = TestResourceId.RESOURCE_1;
				RegionId regionId = TestRegionId.REGION_1;
				resourcesDataManager.removeResourceFromRegion(resourceId, regionId, -1L);
			});
			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();
		});
		assertEquals(ResourceError.NEGATIVE_RESOURCE_AMOUNT, contractException.getErrorType());

		/*
		 * precondition test: if the region does not have the required amount of the
		 * resource
		 */
		contractException = assertThrows(ContractException.class, () -> {
			Factory factory2 = ResourcesTestPluginFactory.factory(0, 4875324598998641428L, (c) -> {
				ResourcesDataManager resourcesDataManager = c.getDataManager(ResourcesDataManager.class);
				ResourceId resourceId = TestResourceId.RESOURCE_1;
				RegionId regionId = TestRegionId.REGION_1;
				resourcesDataManager.removeResourceFromRegion(resourceId, regionId, 10000000L);
			});

			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();
		});
		assertEquals(ResourceError.INSUFFICIENT_RESOURCES_AVAILABLE, contractException.getErrorType());
	}

	@Test
	@UnitTestMethod(target = ResourcesDataManager.class, name = "transferResourceBetweenRegions", args = {
			ResourceId.class, RegionId.class, RegionId.class, long.class })
	public void testTransferResourceBetweenRegions() {
		TestPluginData.Builder pluginBuilder = TestPluginData.builder();

		Set<MultiKey> actualObservations = new LinkedHashSet<>();
		Set<MultiKey> expectedObservations = new LinkedHashSet<>();

		// create an actor to observe resource transfers

		pluginBuilder.addTestActorPlan("observer", new TestActorPlan(0, (c) -> {
			ResourcesDataManager resourcesDataManager = c.getDataManager(ResourcesDataManager.class);
			for (TestRegionId testRegionId : TestRegionId.values()) {
				for (TestResourceId testResourceId : TestResourceId.values()) {
					EventFilter<RegionResourceUpdateEvent> eventFilter = resourcesDataManager
							.getEventFilterForRegionResourceUpdateEvent(testResourceId, testRegionId);
					c.subscribe(eventFilter, (c2, e) -> {
						actualObservations.add(new MultiKey(e.regionId(), e.resourceId(), e.previousResourceLevel(),
								e.currentResourceLevel()));
					});
				}
			}
		}));

		// create an actor that will transfer resources between regions
		pluginBuilder.addTestActorPlan("actor", new TestActorPlan(1, (c) -> {

			StochasticsDataManager stochasticsDataManager = c.getDataManager(StochasticsDataManager.class);
			RandomGenerator randomGenerator = stochasticsDataManager.getRandomGenerator();
			ResourcesDataManager resourcesDataManager = c.getDataManager(ResourcesDataManager.class);

			// add resources to all the regions
			for (TestRegionId testRegionId : TestRegionId.values()) {
				for (TestResourceId testResourceId : TestResourceId.values()) {
					long resourceLevel = resourcesDataManager.getRegionResourceLevel(testRegionId, testResourceId);
					resourcesDataManager.addResourceToRegion(testResourceId, testRegionId, 100L);
					expectedObservations
							.add(new MultiKey(testRegionId, testResourceId, resourceLevel, 100L + resourceLevel));
				}
			}

			// transfer random amounts of resources between regions
			int transfercount = 0;
			for (int i = 0; i < 40; i++) {
				// select random regions and resource
				TestRegionId regionId1 = TestRegionId.getRandomRegionId(randomGenerator);
				TestRegionId regionId2 = TestRegionId.getRandomRegionId(randomGenerator);
				TestResourceId resourceId = TestResourceId.getRandomResourceId(randomGenerator);
				// ensure the regions are different
				if (!regionId1.equals(regionId2)) {
					long region1Level = resourcesDataManager.getRegionResourceLevel(regionId1, resourceId);
					// ensure that the first region has a positive amount of the
					// resource
					if (region1Level > 0) {
						// establish the current level of the second region
						long region2Level = resourcesDataManager.getRegionResourceLevel(regionId2, resourceId);
						// select an amount to transfer
						long amount = randomGenerator.nextInt((int) region1Level) + 1;
						resourcesDataManager.transferResourceBetweenRegions(resourceId, regionId1, regionId2, amount);
						transfercount++;

						// show that the amount was transferred
						long expectedRegion1Level = region1Level - amount;
						long expectedRegion2Level = region2Level + amount;

						long actualRegion1Level = resourcesDataManager.getRegionResourceLevel(regionId1, resourceId);
						long actualRegion2Level = resourcesDataManager.getRegionResourceLevel(regionId2, resourceId);
						assertEquals(expectedRegion1Level, actualRegion1Level);
						assertEquals(expectedRegion2Level, actualRegion2Level);

						expectedObservations
								.add(new MultiKey(regionId1, resourceId, region1Level, expectedRegion1Level));
						expectedObservations
								.add(new MultiKey(regionId2, resourceId, region2Level, expectedRegion2Level));

					}
				}
			}

			// show that enough transfers occurred to make a valid test
			assertTrue(transfercount > 10);

		}));

		// have the observer show that the correct observations were made
		pluginBuilder.addTestActorPlan("observer", new TestActorPlan(2, (c) -> {
			assertEquals(expectedObservations, actualObservations);
		}));

		TestPluginData testPluginData = pluginBuilder.build();
		Factory factory = ResourcesTestPluginFactory.factory(0, 7976375269741360076L, testPluginData);
		TestSimulation.builder().addPlugins(factory.getPlugins()).build().execute();

		/* precondition test: if the source region is null */
		ContractException contractException = assertThrows(ContractException.class, () -> {
			Factory factory2 = ResourcesTestPluginFactory.factory(0, 2545276913032843668L, (c) -> {
				ResourcesDataManager resourcesDataManager = c.getDataManager(ResourcesDataManager.class);
				ResourceId resourceId = TestResourceId.RESOURCE_1;
				RegionId regionId2 = TestRegionId.REGION_2;
				long amount = 10;

				// add resources to all the regions to ensure the precondition
				// tests
				// will work
				for (TestRegionId testRegionId : TestRegionId.values()) {
					for (TestResourceId testResourceId : TestResourceId.values()) {
						resourcesDataManager.addResourceToRegion(testResourceId, testRegionId, 100L);
					}
				}
				resourcesDataManager.transferResourceBetweenRegions(resourceId, null, regionId2, amount);
			});
			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();
		});
		assertEquals(RegionError.NULL_REGION_ID, contractException.getErrorType());

		/* precondition test: if the source region is unknown */
		contractException = assertThrows(ContractException.class, () -> {
			Factory factory2 = ResourcesTestPluginFactory.factory(0, 1182536948902380826L, (c) -> {
				ResourcesDataManager resourcesDataManager = c.getDataManager(ResourcesDataManager.class);
				ResourceId resourceId = TestResourceId.RESOURCE_1;
				RegionId regionId2 = TestRegionId.REGION_2;
				long amount = 10;
				// add resources to all the regions to ensure the precondition
				// tests
				// will work
				for (TestRegionId testRegionId : TestRegionId.values()) {
					for (TestResourceId testResourceId : TestResourceId.values()) {
						resourcesDataManager.addResourceToRegion(testResourceId, testRegionId, 100L);
					}
				}
				resourcesDataManager.transferResourceBetweenRegions(resourceId, TestRegionId.getUnknownRegionId(),
						regionId2, amount);
			});
			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();
		});
		assertEquals(RegionError.UNKNOWN_REGION_ID, contractException.getErrorType());

		/* precondition test: if the destination region is null */
		contractException = assertThrows(ContractException.class, () -> {
			Factory factory2 = ResourcesTestPluginFactory.factory(0, 3358578155263941L, (c) -> {
				ResourcesDataManager resourcesDataManager = c.getDataManager(ResourcesDataManager.class);
				ResourceId resourceId = TestResourceId.RESOURCE_1;
				RegionId regionId1 = TestRegionId.REGION_1;
				long amount = 10;
				// add resources to all the regions to ensure the precondition
				// tests
				// will work
				for (TestRegionId testRegionId : TestRegionId.values()) {
					for (TestResourceId testResourceId : TestResourceId.values()) {
						resourcesDataManager.addResourceToRegion(testResourceId, testRegionId, 100L);
					}
				}
				resourcesDataManager.transferResourceBetweenRegions(resourceId, regionId1, null, amount);
			});
			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();
		});
		assertEquals(RegionError.NULL_REGION_ID, contractException.getErrorType());

		/* precondition test: if the destination region is unknown */
		contractException = assertThrows(ContractException.class, () -> {
			Factory factory2 = ResourcesTestPluginFactory.factory(0, 289436879730670757L, (c) -> {
				ResourcesDataManager resourcesDataManager = c.getDataManager(ResourcesDataManager.class);
				ResourceId resourceId = TestResourceId.RESOURCE_1;
				RegionId regionId1 = TestRegionId.REGION_1;
				long amount = 10;
				// add resources to all the regions to ensure the precondition
				// tests
				// will work
				for (TestRegionId testRegionId : TestRegionId.values()) {
					for (TestResourceId testResourceId : TestResourceId.values()) {
						resourcesDataManager.addResourceToRegion(testResourceId, testRegionId, 100L);
					}
				}
				resourcesDataManager.transferResourceBetweenRegions(resourceId, regionId1,
						TestRegionId.getUnknownRegionId(), amount);
			});
			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();
		});
		assertEquals(RegionError.UNKNOWN_REGION_ID, contractException.getErrorType());

		/* precondition test: if the resource id is null */
		contractException = assertThrows(ContractException.class, () -> {
			Factory factory2 = ResourcesTestPluginFactory.factory(0, 3690172166437098600L, (c) -> {
				ResourcesDataManager resourcesDataManager = c.getDataManager(ResourcesDataManager.class);
				RegionId regionId1 = TestRegionId.REGION_1;
				RegionId regionId2 = TestRegionId.REGION_2;
				long amount = 10;
				// add resources to all the regions to ensure the precondition
				// tests
				// will work
				for (TestRegionId testRegionId : TestRegionId.values()) {
					for (TestResourceId testResourceId : TestResourceId.values()) {
						resourcesDataManager.addResourceToRegion(testResourceId, testRegionId, 100L);
					}
				}
				resourcesDataManager.transferResourceBetweenRegions(null, regionId1, regionId2, amount);
			});
			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();
		});
		assertEquals(ResourceError.NULL_RESOURCE_ID, contractException.getErrorType());

		/* precondition test: if the resource id is unknown */
		contractException = assertThrows(ContractException.class, () -> {
			Factory factory2 = ResourcesTestPluginFactory.factory(0, 7636787584894783093L, (c) -> {
				ResourcesDataManager resourcesDataManager = c.getDataManager(ResourcesDataManager.class);
				RegionId regionId1 = TestRegionId.REGION_1;
				RegionId regionId2 = TestRegionId.REGION_2;
				long amount = 10;
				// add resources to all the regions to ensure the precondition
				// tests
				// will work
				for (TestRegionId testRegionId : TestRegionId.values()) {
					for (TestResourceId testResourceId : TestResourceId.values()) {
						resourcesDataManager.addResourceToRegion(testResourceId, testRegionId, 100L);
					}
				}
				resourcesDataManager.transferResourceBetweenRegions(TestResourceId.getUnknownResourceId(), regionId1,
						regionId2, amount);
			});
			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();
		});
		assertEquals(ResourceError.UNKNOWN_RESOURCE_ID, contractException.getErrorType());

		/* precondition test: if the resource amount is negative */
		contractException = assertThrows(ContractException.class, () -> {
			Factory factory2 = ResourcesTestPluginFactory.factory(0, 1320571074133841280L, (c) -> {
				ResourcesDataManager resourcesDataManager = c.getDataManager(ResourcesDataManager.class);
				ResourceId resourceId = TestResourceId.RESOURCE_1;
				RegionId regionId1 = TestRegionId.REGION_1;
				RegionId regionId2 = TestRegionId.REGION_2;
				// add resources to all the regions to ensure the precondition
				// tests
				// will work
				for (TestRegionId testRegionId : TestRegionId.values()) {
					for (TestResourceId testResourceId : TestResourceId.values()) {
						resourcesDataManager.addResourceToRegion(testResourceId, testRegionId, 100L);
					}
				}
				resourcesDataManager.transferResourceBetweenRegions(resourceId, regionId1, regionId2, -1);
			});
			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();
		});
		assertEquals(ResourceError.NEGATIVE_RESOURCE_AMOUNT, contractException.getErrorType());

		/* precondition test: if the source and destination region are equal */
		contractException = assertThrows(ContractException.class, () -> {
			Factory factory2 = ResourcesTestPluginFactory.factory(0, 2402299633191289724L, (c) -> {
				ResourcesDataManager resourcesDataManager = c.getDataManager(ResourcesDataManager.class);
				ResourceId resourceId = TestResourceId.RESOURCE_1;
				RegionId regionId1 = TestRegionId.REGION_1;
				long amount = 10;
				// add resources to all the regions to ensure the precondition
				// tests
				// will work
				for (TestRegionId testRegionId : TestRegionId.values()) {
					for (TestResourceId testResourceId : TestResourceId.values()) {
						resourcesDataManager.addResourceToRegion(testResourceId, testRegionId, 100L);
					}
				}
				resourcesDataManager.transferResourceBetweenRegions(resourceId, regionId1, regionId1, amount);
			});
			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();
		});
		assertEquals(ResourceError.REFLEXIVE_RESOURCE_TRANSFER, contractException.getErrorType());

		/*
		 * precondition test: if the source region does not have sufficient resources to
		 * support the transfer
		 */
		contractException = assertThrows(ContractException.class, () -> {
			Factory factory2 = ResourcesTestPluginFactory.factory(0, 9136536902267748610L, (c) -> {
				ResourcesDataManager resourcesDataManager = c.getDataManager(ResourcesDataManager.class);
				ResourceId resourceId = TestResourceId.RESOURCE_1;
				RegionId regionId1 = TestRegionId.REGION_1;
				RegionId regionId2 = TestRegionId.REGION_2;
				// add resources to all the regions to ensure the precondition
				// tests
				// will work
				for (TestRegionId testRegionId : TestRegionId.values()) {
					for (TestResourceId testResourceId : TestResourceId.values()) {
						resourcesDataManager.addResourceToRegion(testResourceId, testRegionId, 100L);
					}
				}
				resourcesDataManager.transferResourceBetweenRegions(resourceId, regionId1, regionId2, 100000L);
			});
			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();
		});
		assertEquals(ResourceError.INSUFFICIENT_RESOURCES_AVAILABLE, contractException.getErrorType());

		/*
		 * precondition test: if the transfer will cause a numeric overflow in the
		 * destination region
		 */
		contractException = assertThrows(ContractException.class, () -> {
			Factory factory2 = ResourcesTestPluginFactory.factory(0, 342832088592207841L, (c) -> {
				ResourcesDataManager resourcesDataManager = c.getDataManager(ResourcesDataManager.class);
				ResourceId resourceId = TestResourceId.RESOURCE_1;
				RegionId regionId1 = TestRegionId.REGION_1;
				RegionId regionId2 = TestRegionId.REGION_2;
				long amount = 10;
				// add resources to all the regions to ensure the precondition
				// tests
				// will work
				for (TestRegionId testRegionId : TestRegionId.values()) {
					for (TestResourceId testResourceId : TestResourceId.values()) {
						resourcesDataManager.addResourceToRegion(testResourceId, testRegionId, 100L);
					}
				}
				// fill region 2 to the max long value
				long fillAmount = Long.MAX_VALUE - resourcesDataManager.getRegionResourceLevel(regionId2, resourceId);
				resourcesDataManager.addResourceToRegion(resourceId, regionId2, fillAmount);
				resourcesDataManager.transferResourceBetweenRegions(resourceId, regionId1, regionId2, amount);
			});
			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();
		});
		assertEquals(ResourceError.RESOURCE_ARITHMETIC_EXCEPTION, contractException.getErrorType());

	}

	@Test
	@UnitTestMethod(target = ResourcesDataManager.class, name = "transferResourceFromPersonToRegion", args = {
			ResourceId.class, PersonId.class, long.class })
	public void testResourceTransferFromPersonEvent() {
		TestPluginData.Builder pluginBuilder = TestPluginData.builder();

		Set<MultiKey> expectedObservations = new LinkedHashSet<>();
		Set<MultiKey> actualObservations = new LinkedHashSet<>();

		// Have an actor give people resources
		pluginBuilder.addTestActorPlan("actor", new TestActorPlan(0, (c) -> {
			PeopleDataManager peopleDataManager = c.getDataManager(PeopleDataManager.class);
			RegionsDataManager regionsDataManager = c.getDataManager(RegionsDataManager.class);
			ResourcesDataManager resourcesDataManager = c.getDataManager(ResourcesDataManager.class);

			List<PersonId> people = peopleDataManager.getPeople();

			// add resources to all people
			for (PersonId personId : people) {
				RegionId regionId = regionsDataManager.getPersonRegion(personId);
				for (TestResourceId testResourceId : TestResourceId.values()) {
					resourcesDataManager.addResourceToRegion(testResourceId, regionId, 100L);
					resourcesDataManager.transferResourceToPersonFromRegion(testResourceId, personId, 100L);
				}
			}

		}));

		// Have an actor observe the resource changes
		pluginBuilder.addTestActorPlan("observer", new TestActorPlan(1, (c) -> {
			ResourcesDataManager resourcesDataManager = c.getDataManager(ResourcesDataManager.class);
			for (TestRegionId testRegionId : TestRegionId.values()) {
				for (TestResourceId testResourceId : TestResourceId.values()) {
					EventFilter<RegionResourceUpdateEvent> eventFilter = resourcesDataManager
							.getEventFilterForRegionResourceUpdateEvent(testResourceId, testRegionId);
					c.subscribe(eventFilter, (c2, e) -> {
						actualObservations.add(new MultiKey(e.regionId(), e.resourceId(), e.previousResourceLevel(),
								e.currentResourceLevel()));
					});
				}
			}
			for (TestResourceId testResourceId : TestResourceId.values()) {
				EventFilter<PersonResourceUpdateEvent> eventFilter = resourcesDataManager
						.getEventFilterForPersonResourceUpdateEvent(testResourceId);
				c.subscribe(eventFilter, (c2, e) -> {
					actualObservations.add(new MultiKey(e.personId(), e.resourceId(), e.previousResourceLevel(),
							e.currentResourceLevel()));
				});
			}

		}));

		// Have an actor return resources from people back to their regions
		pluginBuilder.addTestActorPlan("actor", new TestActorPlan(2, (c) -> {

			StochasticsDataManager stochasticsDataManager = c.getDataManager(StochasticsDataManager.class);
			RandomGenerator randomGenerator = stochasticsDataManager.getRandomGenerator();
			ResourcesDataManager resourcesDataManager = c.getDataManager(ResourcesDataManager.class);
			PeopleDataManager peopleDataManager = c.getDataManager(PeopleDataManager.class);
			RegionsDataManager regionsDataManager = c.getDataManager(RegionsDataManager.class);

			List<PersonId> people = peopleDataManager.getPeople();

			int transferCount = 0;
			// transfer resources back to the regions from the people
			for (int i = 0; i < 100; i++) {
				PersonId personId = people.get(randomGenerator.nextInt(people.size()));
				ResourceId resourceId = TestResourceId.getRandomResourceId(randomGenerator);
				RegionId regionId = regionsDataManager.getPersonRegion(personId);
				long personResourceLevel = resourcesDataManager.getPersonResourceLevel(resourceId, personId);
				long regionResourceLevel = resourcesDataManager.getRegionResourceLevel(regionId, resourceId);

				if (personResourceLevel > 0) {
					long amount = randomGenerator.nextInt((int) personResourceLevel);
					long expectedPersonResourceLevel = personResourceLevel - amount;
					long expectedRegionResourceLevel = regionResourceLevel + amount;
					resourcesDataManager.transferResourceFromPersonToRegion(resourceId, personId, amount);
					transferCount++;
					long actualPersonResourceLevel = resourcesDataManager.getPersonResourceLevel(resourceId, personId);
					long actualRegionResourceLevel = resourcesDataManager.getRegionResourceLevel(regionId, resourceId);
					assertEquals(expectedPersonResourceLevel, actualPersonResourceLevel);
					assertEquals(expectedRegionResourceLevel, actualRegionResourceLevel);

					expectedObservations
							.add(new MultiKey(regionId, resourceId, regionResourceLevel, expectedRegionResourceLevel));
					expectedObservations
							.add(new MultiKey(personId, resourceId, personResourceLevel, expectedPersonResourceLevel));

				}
			}

			// show that a reasonable number of transfers occurred
			assertTrue(transferCount > 20);

		}));

		// Have the observer show that the observations were properly generated
		pluginBuilder.addTestActorPlan("observer", new TestActorPlan(3, (c) -> {
			assertEquals(expectedObservations, actualObservations);
		}));

		// Have an actor test preconditions
		pluginBuilder.addTestActorPlan("actor", new TestActorPlan(4, (c) -> {

			RegionsDataManager regionsDataManager = c.getDataManager(RegionsDataManager.class);
			ResourcesDataManager resourcesDataManager = c.getDataManager(ResourcesDataManager.class);
			// precondition tests
			PersonId personId = new PersonId(0);
			ResourceId resourceId = TestResourceId.RESOURCE_4;
			long amount = 10;

			// add resources to the person to support the precondition tests

			RegionId regionId = regionsDataManager.getPersonRegion(personId);
			for (TestResourceId testResourceId : TestResourceId.values()) {
				resourcesDataManager.addResourceToRegion(testResourceId, regionId, 100L);
				resourcesDataManager.transferResourceToPersonFromRegion(testResourceId, personId, 100L);
			}

			// if the person id is null

			ContractException contractException = assertThrows(ContractException.class,
					() -> resourcesDataManager.transferResourceFromPersonToRegion(resourceId, null, amount));
			assertEquals(PersonError.NULL_PERSON_ID, contractException.getErrorType());

			// if the person does not exist
			contractException = assertThrows(ContractException.class, () -> resourcesDataManager
					.transferResourceFromPersonToRegion(resourceId, new PersonId(3434), amount));
			assertEquals(PersonError.UNKNOWN_PERSON_ID, contractException.getErrorType());

			// if the resource id is null
			contractException = assertThrows(ContractException.class,
					() -> resourcesDataManager.transferResourceFromPersonToRegion(null, personId, amount));
			assertEquals(ResourceError.NULL_RESOURCE_ID, contractException.getErrorType());

			// if the resource id is unknown
			contractException = assertThrows(ContractException.class, () -> resourcesDataManager
					.transferResourceFromPersonToRegion(TestResourceId.getUnknownResourceId(), personId, amount));
			assertEquals(ResourceError.UNKNOWN_RESOURCE_ID, contractException.getErrorType());

			// if the amount is negative
			contractException = assertThrows(ContractException.class,
					() -> resourcesDataManager.transferResourceFromPersonToRegion(resourceId, personId, -1L));
			assertEquals(ResourceError.NEGATIVE_RESOURCE_AMOUNT, contractException.getErrorType());

			// if the person does not have the required amount of the resource
			contractException = assertThrows(ContractException.class,
					() -> resourcesDataManager.transferResourceFromPersonToRegion(resourceId, personId, 1000000));
			assertEquals(ResourceError.INSUFFICIENT_RESOURCES_AVAILABLE, contractException.getErrorType());

			// if the transfer results in an overflow of the region's resource
			// level

			// fill the region
			long regionResourceLevel = resourcesDataManager.getRegionResourceLevel(regionId, resourceId);
			resourcesDataManager.removeResourceFromRegion(resourceId, regionId, regionResourceLevel);
			resourcesDataManager.addResourceToRegion(resourceId, regionId, Long.MAX_VALUE);

			// empty the person
			long personResourceLevel = resourcesDataManager.getPersonResourceLevel(resourceId, personId);
			resourcesDataManager.removeResourceFromPerson(resourceId, personId, personResourceLevel);

			// transfer the region's inventory to the person
			resourcesDataManager.transferResourceToPersonFromRegion(resourceId, personId, Long.MAX_VALUE);

			// add one more unit to the region
			resourcesDataManager.addResourceToRegion(resourceId, regionId, 1L);

			// attempt to transfer the person's inventory back to the region

			contractException = assertThrows(ContractException.class, () -> resourcesDataManager
					.transferResourceFromPersonToRegion(resourceId, personId, Long.MAX_VALUE));
			assertEquals(ResourceError.RESOURCE_ARITHMETIC_EXCEPTION, contractException.getErrorType());

		}));

		TestPluginData testPluginData = pluginBuilder.build();
		Factory factory = ResourcesTestPluginFactory.factory(30, 3166011813977431605L, testPluginData);

		TestSimulation.builder().addPlugins(factory.getPlugins()).build().execute();

	}

	@Test
	@UnitTestMethod(target = ResourcesDataManager.class, name = "transferResourceToPersonFromRegion", args = {
			ResourceId.class, PersonId.class, long.class })
	public void testResourceTransferToPersonEvent() {
		TestPluginData.Builder pluginBuilder = TestPluginData.builder();

		Set<MultiKey> expectedObservations = new LinkedHashSet<>();
		Set<MultiKey> actualObservations = new LinkedHashSet<>();

		// Have an actor add resources to the regions
		pluginBuilder.addTestActorPlan("actor", new TestActorPlan(0, (c) -> {
			PeopleDataManager peopleDataManager = c.getDataManager(PeopleDataManager.class);
			RegionsDataManager regionLocationDataManager = c.getDataManager(RegionsDataManager.class);
			ResourcesDataManager resourcesDataManager = c.getDataManager(ResourcesDataManager.class);

			List<PersonId> people = peopleDataManager.getPeople();

			// add resources to all regions
			for (PersonId personId : people) {
				RegionId regionId = regionLocationDataManager.getPersonRegion(personId);
				for (TestResourceId testResourceId : TestResourceId.values()) {
					resourcesDataManager.addResourceToRegion(testResourceId, regionId, 1000L);
				}
			}

		}));

		// Have an actor observe the resource transfers
		pluginBuilder.addTestActorPlan("observer", new TestActorPlan(1, (c) -> {
			ResourcesDataManager resourcesDataManager = c.getDataManager(ResourcesDataManager.class);

			for (TestRegionId testRegionId : TestRegionId.values()) {
				for (TestResourceId testResourceId : TestResourceId.values()) {
					EventFilter<RegionResourceUpdateEvent> eventFilter = resourcesDataManager
							.getEventFilterForRegionResourceUpdateEvent(testResourceId, testRegionId);
					c.subscribe(eventFilter, (c2, e) -> {
						actualObservations.add(new MultiKey(e.regionId(), e.resourceId(), e.previousResourceLevel(),
								e.currentResourceLevel()));
					});
				}
			}

			for (TestResourceId testResourceId : TestResourceId.values()) {
				EventFilter<PersonResourceUpdateEvent> eventFilter = resourcesDataManager
						.getEventFilterForPersonResourceUpdateEvent(testResourceId);
				c.subscribe(eventFilter, (c2, e) -> {
					actualObservations.add(new MultiKey(e.personId(), e.resourceId(), e.previousResourceLevel(),
							e.currentResourceLevel()));

				});
			}

		}));

		// Have an actor transfer the resources to people
		pluginBuilder.addTestActorPlan("actor", new TestActorPlan(2, (c) -> {

			StochasticsDataManager stochasticsDataManager = c.getDataManager(StochasticsDataManager.class);
			RandomGenerator randomGenerator = stochasticsDataManager.getRandomGenerator();
			ResourcesDataManager resourcesDataManager = c.getDataManager(ResourcesDataManager.class);
			PeopleDataManager peopleDataManager = c.getDataManager(PeopleDataManager.class);
			RegionsDataManager regionLocationDataManager = c.getDataManager(RegionsDataManager.class);

			List<PersonId> people = peopleDataManager.getPeople();

			int transferCount = 0;
			// transfer resources back to the people
			for (int i = 0; i < 100; i++) {
				PersonId personId = people.get(randomGenerator.nextInt(people.size()));
				ResourceId resourceId = TestResourceId.getRandomResourceId(randomGenerator);
				RegionId regionId = regionLocationDataManager.getPersonRegion(personId);
				long personResourceLevel = resourcesDataManager.getPersonResourceLevel(resourceId, personId);
				long regionResourceLevel = resourcesDataManager.getRegionResourceLevel(regionId, resourceId);

				if (regionResourceLevel > 0) {
					long amount = randomGenerator.nextInt((int) regionResourceLevel);
					long expectedPersonResourceLevel = personResourceLevel + amount;
					long expectedRegionResourceLevel = regionResourceLevel - amount;
					resourcesDataManager.transferResourceToPersonFromRegion(resourceId, personId, amount);
					transferCount++;
					long actualPersonResourceLevel = resourcesDataManager.getPersonResourceLevel(resourceId, personId);
					long actualRegionResourceLevel = resourcesDataManager.getRegionResourceLevel(regionId, resourceId);
					assertEquals(expectedPersonResourceLevel, actualPersonResourceLevel);
					assertEquals(expectedRegionResourceLevel, actualRegionResourceLevel);

					expectedObservations
							.add(new MultiKey(regionId, resourceId, regionResourceLevel, expectedRegionResourceLevel));
					expectedObservations
							.add(new MultiKey(personId, resourceId, personResourceLevel, expectedPersonResourceLevel));

				}
			}

			// show that a reasonable number of transfers occurred
			assertTrue(transferCount > 20);

		}));

		// Have an actor show that the proper observations were generated
		pluginBuilder.addTestActorPlan("observer", new TestActorPlan(3, (c) -> {
			assertEquals(expectedObservations, actualObservations);
		}));

		TestPluginData testPluginData = pluginBuilder.build();
		Factory factory = ResourcesTestPluginFactory.factory(30, 3808042869854225459L, testPluginData);
		TestSimulation.builder().addPlugins(factory.getPlugins()).build().execute();

		/* precondition test: if the person id is null */
		ContractException contractException = assertThrows(ContractException.class, () -> {
			Factory factory2 = ResourcesTestPluginFactory.factory(30, 2628501738627419743L, (c) -> {
				ResourcesDataManager resourcesDataManager = c.getDataManager(ResourcesDataManager.class);
				RegionsDataManager regionsDataManager = c.getDataManager(RegionsDataManager.class);
				PersonId personId = new PersonId(0);
				ResourceId resourceId = TestResourceId.RESOURCE_4;
				long amount = 10;
				// add resources to the region to support the precondition tests
				RegionId regionId = regionsDataManager.getPersonRegion(personId);
				for (TestResourceId testResourceId : TestResourceId.values()) {
					resourcesDataManager.addResourceToRegion(testResourceId, regionId, 100L);
				}
				resourcesDataManager.transferResourceToPersonFromRegion(resourceId, null, amount);
			});
			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();
		});
		assertEquals(PersonError.NULL_PERSON_ID, contractException.getErrorType());

		/* precondition test: if the person does not exist */
		contractException = assertThrows(ContractException.class, () -> {
			Factory factory2 = ResourcesTestPluginFactory.factory(30, 4172586983768511485L, (c) -> {
				ResourcesDataManager resourcesDataManager = c.getDataManager(ResourcesDataManager.class);
				RegionsDataManager regionsDataManager = c.getDataManager(RegionsDataManager.class);
				PersonId personId = new PersonId(0);
				ResourceId resourceId = TestResourceId.RESOURCE_4;
				long amount = 10;
				// add resources to the region to support the precondition tests
				RegionId regionId = regionsDataManager.getPersonRegion(personId);
				for (TestResourceId testResourceId : TestResourceId.values()) {
					resourcesDataManager.addResourceToRegion(testResourceId, regionId, 100L);
				}
				resourcesDataManager.transferResourceToPersonFromRegion(resourceId, new PersonId(3434), amount);
			});
			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();
		});
		assertEquals(PersonError.UNKNOWN_PERSON_ID, contractException.getErrorType());

		/* precondition test: if the resource id is null */
		contractException = assertThrows(ContractException.class, () -> {
			Factory factory2 = ResourcesTestPluginFactory.factory(30, 6256935891787853979L, (c) -> {
				ResourcesDataManager resourcesDataManager = c.getDataManager(ResourcesDataManager.class);
				RegionsDataManager regionsDataManager = c.getDataManager(RegionsDataManager.class);
				PersonId personId = new PersonId(0);
				long amount = 10;
				// add resources to the region to support the precondition tests
				RegionId regionId = regionsDataManager.getPersonRegion(personId);
				for (TestResourceId testResourceId : TestResourceId.values()) {
					resourcesDataManager.addResourceToRegion(testResourceId, regionId, 100L);
				}
				resourcesDataManager.transferResourceToPersonFromRegion(null, personId, amount);
			});
			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();
		});
		assertEquals(ResourceError.NULL_RESOURCE_ID, contractException.getErrorType());

		/* precondition test: if the resource id is unknown */
		contractException = assertThrows(ContractException.class, () -> {
			Factory factory2 = ResourcesTestPluginFactory.factory(30, 6949348067383487020L, (c) -> {
				ResourcesDataManager resourcesDataManager = c.getDataManager(ResourcesDataManager.class);
				RegionsDataManager regionsDataManager = c.getDataManager(RegionsDataManager.class);
				PersonId personId = new PersonId(0);
				long amount = 10;
				// add resources to the region to support the precondition tests
				RegionId regionId = regionsDataManager.getPersonRegion(personId);
				for (TestResourceId testResourceId : TestResourceId.values()) {
					resourcesDataManager.addResourceToRegion(testResourceId, regionId, 100L);
				}
				resourcesDataManager.transferResourceToPersonFromRegion(TestResourceId.getUnknownResourceId(), personId,
						amount);
			});
			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();
		});
		assertEquals(ResourceError.UNKNOWN_RESOURCE_ID, contractException.getErrorType());

		/* precondition test: if the amount is negative */
		contractException = assertThrows(ContractException.class, () -> {
			Factory factory2 = ResourcesTestPluginFactory.factory(30, 6911979438110217773L, (c) -> {
				ResourcesDataManager resourcesDataManager = c.getDataManager(ResourcesDataManager.class);
				RegionsDataManager regionsDataManager = c.getDataManager(RegionsDataManager.class);
				PersonId personId = new PersonId(0);
				ResourceId resourceId = TestResourceId.RESOURCE_4;
				// add resources to the region to support the precondition tests
				RegionId regionId = regionsDataManager.getPersonRegion(personId);
				for (TestResourceId testResourceId : TestResourceId.values()) {
					resourcesDataManager.addResourceToRegion(testResourceId, regionId, 100L);
				}
				resourcesDataManager.transferResourceToPersonFromRegion(resourceId, personId, -1L);
			});
			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();
		});
		assertEquals(ResourceError.NEGATIVE_RESOURCE_AMOUNT, contractException.getErrorType());

		/*
		 * precondition test: if the region does not have the required amount of the
		 * resource
		 */
		contractException = assertThrows(ContractException.class, () -> {
			Factory factory2 = ResourcesTestPluginFactory.factory(30, 1022333582572896703L, (c) -> {
				ResourcesDataManager resourcesDataManager = c.getDataManager(ResourcesDataManager.class);
				RegionsDataManager regionsDataManager = c.getDataManager(RegionsDataManager.class);
				PersonId personId = new PersonId(0);
				ResourceId resourceId = TestResourceId.RESOURCE_4;
				// add resources to the region to support the precondition tests
				RegionId regionId = regionsDataManager.getPersonRegion(personId);
				for (TestResourceId testResourceId : TestResourceId.values()) {
					resourcesDataManager.addResourceToRegion(testResourceId, regionId, 100L);
				}
				resourcesDataManager.transferResourceToPersonFromRegion(resourceId, personId, 1000000);
			});
			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();
		});
		assertEquals(ResourceError.INSUFFICIENT_RESOURCES_AVAILABLE, contractException.getErrorType());

		/*
		 * precondition test: if the transfer results in an overflow of the person's
		 * resource level
		 */
		contractException = assertThrows(ContractException.class, () -> {
			Factory factory2 = ResourcesTestPluginFactory.factory(30, 1989550065510462161L, (c) -> {
				ResourcesDataManager resourcesDataManager = c.getDataManager(ResourcesDataManager.class);
				RegionsDataManager regionsDataManager = c.getDataManager(RegionsDataManager.class);
				PersonId personId = new PersonId(0);
				ResourceId resourceId = TestResourceId.RESOURCE_4;
				// add resources to the region to support the precondition tests
				RegionId regionId = regionsDataManager.getPersonRegion(personId);
				for (TestResourceId testResourceId : TestResourceId.values()) {
					resourcesDataManager.addResourceToRegion(testResourceId, regionId, 100L);
				}
				// fill the region
				long regionResourceLevel = resourcesDataManager.getRegionResourceLevel(regionId, resourceId);
				resourcesDataManager.removeResourceFromRegion(resourceId, regionId, regionResourceLevel);
				resourcesDataManager.addResourceToRegion(resourceId, regionId, Long.MAX_VALUE);

				// empty the person
				long personResourceLevel = resourcesDataManager.getPersonResourceLevel(resourceId, personId);
				resourcesDataManager.removeResourceFromPerson(resourceId, personId, personResourceLevel);

				// transfer the region's inventory to the person
				resourcesDataManager.transferResourceToPersonFromRegion(resourceId, personId, Long.MAX_VALUE);

				// add one more unit to the region
				resourcesDataManager.addResourceToRegion(resourceId, regionId, 1L);

				// attempt to transfer the on unit to the person

				resourcesDataManager.transferResourceToPersonFromRegion(resourceId, personId, 1L);
			});

			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();

		});
		assertEquals(ResourceError.RESOURCE_ARITHMETIC_EXCEPTION, contractException.getErrorType());

	}

	@Test
	@UnitTestMethod(target = ResourcesDataManager.class, name = "addResourceToRegion", args = { ResourceId.class,
			RegionId.class, long.class })
	public void testAddResourceToRegion() {

		TestPluginData.Builder pluginBuilder = TestPluginData.builder();

		Set<MultiKey> expectedObservations = new LinkedHashSet<>();
		Set<MultiKey> actualObservations = new LinkedHashSet<>();

		// Have an actor to observe the resource changes

		pluginBuilder.addTestActorPlan("observer", new TestActorPlan(0, (c) -> {
			ResourcesDataManager resourcesDataManager = c.getDataManager(ResourcesDataManager.class);
			for (TestRegionId testRegionId : TestRegionId.values()) {
				for (TestResourceId testResourceId : TestResourceId.values()) {
					EventFilter<RegionResourceUpdateEvent> eventFilter = resourcesDataManager
							.getEventFilterForRegionResourceUpdateEvent(testResourceId, testRegionId);
					c.subscribe(eventFilter, (c2, e) -> {
						actualObservations.add(new MultiKey(e.regionId(), e.resourceId(), e.previousResourceLevel(),
								e.currentResourceLevel()));
					});
				}
			}
		}));

		// Have an actor add resources to regions
		pluginBuilder.addTestActorPlan("actor", new TestActorPlan(1, (c) -> {

			StochasticsDataManager stochasticsDataManager = c.getDataManager(StochasticsDataManager.class);
			RandomGenerator randomGenerator = stochasticsDataManager.getRandomGenerator();
			ResourcesDataManager resourcesDataManager = c.getDataManager(ResourcesDataManager.class);

			// add random amounts of resources to regions
			int transfercount = 0;
			for (int i = 0; i < 40; i++) {
				// select random regions and resources
				TestRegionId regionId = TestRegionId.getRandomRegionId(randomGenerator);
				TestResourceId resourceId = TestResourceId.getRandomResourceId(randomGenerator);
				long regionLevel = resourcesDataManager.getRegionResourceLevel(regionId, resourceId);

				// select an amount to add
				long amount = randomGenerator.nextInt(100) + 1;
				resourcesDataManager.addResourceToRegion(resourceId, regionId, amount);
				transfercount++;

				// show that the amount was added
				long expectedRegionLevel = regionLevel + amount;
				long actualRegionLevel = resourcesDataManager.getRegionResourceLevel(regionId, resourceId);
				assertEquals(expectedRegionLevel, actualRegionLevel);

				expectedObservations.add(new MultiKey(regionId, resourceId, regionLevel, expectedRegionLevel));

			}

			// show that enough additions occurred to make a valid test
			assertTrue(transfercount > 10);

		}));

		// have the observer show that the correct observations were generated
		pluginBuilder.addTestActorPlan("observer", new TestActorPlan(2, (c) -> {
			assertEquals(expectedObservations, actualObservations);

		}));

		TestPluginData testPluginData = pluginBuilder.build();
		Factory factory = ResourcesTestPluginFactory.factory(0, 2273638431976256278L, testPluginData);
		TestSimulation.builder().addPlugins(factory.getPlugins()).build().execute();

		/* precondition test: if the region id is null */
		ContractException contractException = assertThrows(ContractException.class, () -> {
			Factory factory2 = ResourcesTestPluginFactory.factory(0, 6097938300290796293L, (c) -> {
				ResourceId resourceId = TestResourceId.RESOURCE_1;
				long amount = 10;
				ResourcesDataManager resourcesDataManager = c.getDataManager(ResourcesDataManager.class);
				resourcesDataManager.addResourceToRegion(resourceId, null, amount);
			});
			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();
		});
		assertEquals(RegionError.NULL_REGION_ID, contractException.getErrorType());

		/* precondition test: if the region id is unknown */
		contractException = assertThrows(ContractException.class, () -> {
			Factory factory2 = ResourcesTestPluginFactory.factory(0, 1284607529543124944L, (c) -> {
				ResourceId resourceId = TestResourceId.RESOURCE_1;
				long amount = 10;
				ResourcesDataManager resourcesDataManager = c.getDataManager(ResourcesDataManager.class);
				resourcesDataManager.addResourceToRegion(resourceId, TestRegionId.getUnknownRegionId(), amount);
			});
			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();
		});
		assertEquals(RegionError.UNKNOWN_REGION_ID, contractException.getErrorType());

		/* precondition test: if the resource id is null */
		contractException = assertThrows(ContractException.class, () -> {
			Factory factory2 = ResourcesTestPluginFactory.factory(0, 5929063621703486118L, (c) -> {
				RegionId regionId = TestRegionId.REGION_1;
				long amount = 10;
				ResourcesDataManager resourcesDataManager = c.getDataManager(ResourcesDataManager.class);
				resourcesDataManager.addResourceToRegion(null, regionId, amount);
			});
			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();
		});
		assertEquals(ResourceError.NULL_RESOURCE_ID, contractException.getErrorType());

		/* precondition test: if the resource id is unknown */
		contractException = assertThrows(ContractException.class, () -> {
			Factory factory2 = ResourcesTestPluginFactory.factory(0, 1240045272882068003L, (c) -> {
				RegionId regionId = TestRegionId.REGION_1;
				long amount = 10;
				ResourcesDataManager resourcesDataManager = c.getDataManager(ResourcesDataManager.class);
				resourcesDataManager.addResourceToRegion(TestResourceId.getUnknownResourceId(), regionId, amount);
			});
			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();
		});
		assertEquals(ResourceError.UNKNOWN_RESOURCE_ID, contractException.getErrorType());

		/* precondition test: if the amount is negative */
		contractException = assertThrows(ContractException.class, () -> {
			Factory factory2 = ResourcesTestPluginFactory.factory(0, 2192023733930104434L, (c) -> {
				ResourceId resourceId = TestResourceId.RESOURCE_1;
				RegionId regionId = TestRegionId.REGION_1;
				ResourcesDataManager resourcesDataManager = c.getDataManager(ResourcesDataManager.class);
				resourcesDataManager.addResourceToRegion(resourceId, regionId, -1);
			});
			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();
		});
		assertEquals(ResourceError.NEGATIVE_RESOURCE_AMOUNT, contractException.getErrorType());

		/* precondition test: if the addition results in an overflow */
		contractException = assertThrows(ContractException.class, () -> {
			Factory factory2 = ResourcesTestPluginFactory.factory(0, 4518775448744653729L, (c) -> {
				ResourceId resourceId = TestResourceId.RESOURCE_1;
				RegionId regionId = TestRegionId.REGION_1;
				long amount = 10;
				ResourcesDataManager resourcesDataManager = c.getDataManager(ResourcesDataManager.class);
				resourcesDataManager.addResourceToRegion(resourceId, regionId, amount);
				resourcesDataManager.addResourceToRegion(resourceId, regionId, Long.MAX_VALUE);
			});
			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();
		});
		assertEquals(ResourceError.RESOURCE_ARITHMETIC_EXCEPTION, contractException.getErrorType());

	}

	@Test
	@UnitTestMethod(target = ResourcesDataManager.class, name = "init", args = { DataManagerContext.class })
	public void testPersonAdditionEvent() {

		// Have an actor create a few people with random resource levels
		Factory factory = ResourcesTestPluginFactory.factory(0, 5441878385875188805L, (c) -> {
			ResourcesDataManager resourcesDataManager = c.getDataManager(ResourcesDataManager.class);
			PeopleDataManager peopleDataManager = c.getDataManager(PeopleDataManager.class);
			StochasticsDataManager stochasticsDataManager = c.getDataManager(StochasticsDataManager.class);
			RandomGenerator randomGenerator = stochasticsDataManager.getRandomGenerator();

			// create 30 people, testing each in turn for their resource levels
			for (int i = 0; i < 30; i++) {
				PersonConstructionData.Builder builder = PersonConstructionData.builder();
				// give the person and region
				builder.add(TestRegionId.getRandomRegionId(randomGenerator));

				// give the person a positive resource level for about half of
				// the resources
				Map<ResourceId, MutableInteger> expectedResources = new LinkedHashMap<>();
				for (TestResourceId testResourceId : TestResourceId.values()) {
					MutableInteger mutableInteger = new MutableInteger();
					expectedResources.put(testResourceId, mutableInteger);
					if (randomGenerator.nextBoolean()) {
						int amount = randomGenerator.nextInt(30) + 1;
						mutableInteger.setValue(amount);
						ResourceInitialization resourceInitialization = new ResourceInitialization(testResourceId,
								(long) amount);
						builder.add(resourceInitialization);
					}
				}

				// create the person which will in turn generate the
				// PersonAdditionEvent
				PersonId personId = peopleDataManager.addPerson(builder.build());

				// show that the person has the correct resource levels
				for (TestResourceId testResourceId : TestResourceId.values()) {
					int actualPersonResourceLevel = (int) resourcesDataManager.getPersonResourceLevel(testResourceId,
							personId);
					int expectedPersonResourceLevel = expectedResources.get(testResourceId).getValue();
					assertEquals(expectedPersonResourceLevel, actualPersonResourceLevel);
				}
			}

		});
		TestSimulation.builder().addPlugins(factory.getPlugins()).build().execute();

		/* precondition test: */
		ContractException contractException = assertThrows(ContractException.class, () -> {
			Factory factory2 = ResourcesTestPluginFactory.factory(0, 3508334533286675130L, (c) -> {
				PeopleDataManager peopleDataManager = c.getDataManager(PeopleDataManager.class);
				/*
				 * Precondition tests for the validity of the person id are shadowed by other
				 * plugins and cannot be easily tested
				 */

				/*
				 * if the auxiliary data contains a ResourceInitialization that has a null
				 * resource id
				 */
				peopleDataManager.addPerson(PersonConstructionData.builder().add(TestRegionId.REGION_1)
						.add(new ResourceInitialization(null, 15L)).build());
			});
			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();
		});

		assertEquals(ResourceError.NULL_RESOURCE_ID, contractException.getErrorType());

		/* precondition test: */
		contractException = assertThrows(ContractException.class, () -> {
			Factory factory2 = ResourcesTestPluginFactory.factory(0, 7458875943724352968L, (c) -> {
				PeopleDataManager peopleDataManager = c.getDataManager(PeopleDataManager.class);

				/*
				 * Precondition tests for the validity of the person id are shadowed by other
				 * plugins and cannot be easily tested
				 */

				/*
				 * if the auxiliary data contains a ResourceInitialization that has an unknown
				 * resource id
				 */

				peopleDataManager.addPerson(PersonConstructionData.builder()//
						.add(TestRegionId.REGION_2)//
						.add(new ResourceInitialization(TestResourceId.getUnknownResourceId(), 15L))//
						.build());

			});
			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();
		});
		assertEquals(ResourceError.UNKNOWN_RESOURCE_ID, contractException.getErrorType());

		/* precondition test: */
		contractException = assertThrows(ContractException.class, () -> {
			Factory factory2 = ResourcesTestPluginFactory.factory(0, 3702960689314847457L, (c) -> {
				PeopleDataManager peopleDataManager = c.getDataManager(PeopleDataManager.class);

				/*
				 * Precondition tests for the validity of the person id are shadowed by other
				 * plugins and cannot be easily tested
				 */

				/*
				 * if the auxiliary data contains a ResourceInitialization that has a negative
				 * resource level
				 */

				peopleDataManager.addPerson(PersonConstructionData.builder()//
						.add(TestRegionId.REGION_3)//
						.add(new ResourceInitialization(TestResourceId.RESOURCE_1, -15L))//
						.build());//

			});
			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();
		});
		assertEquals(ResourceError.NEGATIVE_RESOURCE_AMOUNT, contractException.getErrorType());

	}

	@Test
	@UnitTestMethod(target = ResourcesDataManager.class, name = "init", args = { DataManagerContext.class })
	public void testRegionAdditionEvent() {

		/*
		 * show that a newly added region will cause the resource data manager to return
		 * the expected levels from the event.
		 */

		Factory factory = ResourcesTestPluginFactory.factory(0, 7471968091128250788L, (c) -> {
			RegionsDataManager regionsDataManager = c.getDataManager(RegionsDataManager.class);
			RegionId newRegionId = TestRegionId.getUnknownRegionId();

			Map<TestResourceId, Long> expectedValues = new LinkedHashMap<>();
			for (TestResourceId testResourceId : TestResourceId.values()) {
				expectedValues.put(testResourceId, 0L);
			}
			expectedValues.put(TestResourceId.RESOURCE_1, 75L);
			expectedValues.put(TestResourceId.RESOURCE_2, 432L);

			RegionConstructionData.Builder regionConstructionDataBuilder = //
					RegionConstructionData.builder()//
							.setRegionId(newRegionId);

			for (TestResourceId testResourceId : TestResourceId.values()) {
				Long amount = expectedValues.get(testResourceId);
				if (amount != 0L) {
					regionConstructionDataBuilder.addValue(new ResourceInitialization(testResourceId, amount));//
				}
			}

			RegionConstructionData regionConstructionData = regionConstructionDataBuilder.build();
			regionsDataManager.addRegion(regionConstructionData);
			ResourcesDataManager resourcesDataManager = c.getDataManager(ResourcesDataManager.class);

			for (TestResourceId testResourceId : TestResourceId.values()) {
				long expectedResourceLevel = expectedValues.get(testResourceId);
				long actualResourceLevel = resourcesDataManager.getRegionResourceLevel(newRegionId, testResourceId);
				assertEquals(expectedResourceLevel, actualResourceLevel);
			}

		});
		TestSimulation.builder().addPlugins(factory.getPlugins()).build().execute();

		/*
		 * show that an unknown region will cause the resource data manager to throw an
		 * exception when retrieving a resource level for that region
		 */
		assertThrows(ContractException.class, () -> {
			Factory factory2 = ResourcesTestPluginFactory.factory(0, 4192802703078518338L, (c) -> {
				RegionId newRegionId = TestRegionId.getUnknownRegionId();
				ResourcesDataManager resourcesDataManager = c.getDataManager(ResourcesDataManager.class);
				resourcesDataManager.getRegionResourceLevel(newRegionId, TestResourceId.RESOURCE_1);
			});
			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();
		});
	}

	/**
	 * Demonstrates that the data manager's initial state reflects its plugin data
	 */
	@Test
	@UnitTestMethod(target = ResourcesDataManager.class, name = "init", args = { DataManagerContext.class })
	public void testStateInitialization() {

		int initialPopulation = 10;

		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(1828556358289827784L);

		// create a list of people
		List<PersonId> people = new ArrayList<>();
		for (int i = 0; i < initialPopulation; i++) {
			people.add(new PersonId(i));
		}

		// add the resources plugin
		ResourcesPluginData.Builder resourcesBuilder = ResourcesPluginData.builder();

		for (int i = 0; i < initialPopulation; i++) {
			PersonId personId = new PersonId(i);
			for (TestResourceId testResourceId : TestResourceId.values()) {
				resourcesBuilder.setPersonResourceLevel(personId, testResourceId, randomGenerator.nextInt(5));
			}
		}

		for (TestRegionId testRegionId : TestRegionId.values()) {
			for (TestResourceId testResourceId : TestResourceId.values()) {
				resourcesBuilder.setRegionResourceLevel(testRegionId, testResourceId, randomGenerator.nextInt(5));
			}
		}

		for (TestResourceId testResourceId : TestResourceId.values()) {
			resourcesBuilder.addResource(testResourceId, 0.0, testResourceId.getTimeTrackingPolicy());
		}

		for (TestResourcePropertyId testResourcePropertyId : TestResourcePropertyId.values()) {
			TestResourceId testResourceId = testResourcePropertyId.getTestResourceId();
			PropertyDefinition propertyDefinition = testResourcePropertyId.getPropertyDefinition();
			Object propertyValue = testResourcePropertyId.getRandomPropertyValue(randomGenerator);
			resourcesBuilder.defineResourceProperty(testResourceId, testResourcePropertyId, propertyDefinition);
			resourcesBuilder.setResourcePropertyValue(testResourceId, testResourcePropertyId, propertyValue);
		}

		ResourcesPluginData resourcesPluginData = resourcesBuilder.build();

		TestPluginData.Builder pluginBuilder = TestPluginData.builder();

		pluginBuilder.addTestActorPlan("actor", new TestActorPlan(0, (c) -> {

			ResourcesDataManager resourcesDataManager = c.getDataManager(ResourcesDataManager.class);
			PeopleDataManager peopleDataManager = c.getDataManager(PeopleDataManager.class);
			RegionsDataManager regionsDataManager = c.getDataManager(RegionsDataManager.class);

			List<PersonId> personIds = peopleDataManager.getPeople();

			Set<RegionId> expectedRegionIds = regionsDataManager.getRegionIds();
			Set<RegionId> actualRegionIds = resourcesPluginData.getRegionIds();
			assertEquals(expectedRegionIds, actualRegionIds);

			for (RegionId regionId : resourcesPluginData.getRegionIds()) {
				Map<ResourceId, Long> expectedAmounts = new LinkedHashMap<>();
				for (ResourceId resourceId : resourcesPluginData.getResourceIds()) {
					expectedAmounts.put(resourceId, 0L);
				}
				for (ResourceId resourceId : resourcesPluginData.getResourceIds()) {
					Optional<Long> optional = resourcesPluginData.getRegionResourceLevel(regionId, resourceId);
					if (optional.isPresent()) {
						expectedAmounts.put(resourceId, optional.get());
					}
				}
				for (ResourceId resourceId : resourcesPluginData.getResourceIds()) {
					long expectedRegionResourceLevel = expectedAmounts.get(resourceId);
					long actualRegionResourceLevel = resourcesDataManager.getRegionResourceLevel(regionId, resourceId);
					assertEquals(expectedRegionResourceLevel, actualRegionResourceLevel);
				}
			}

			for (ResourceId resourceId : resourcesPluginData.getResourceIds()) {
				boolean expectedPolicy = resourcesPluginData.getResourceTimeTrackingPolicy(resourceId);
				boolean actualPolicy = resourcesDataManager.getPersonResourceTimeTrackingPolicy(resourceId);
				assertEquals(expectedPolicy, actualPolicy);
			}

			assertEquals(resourcesPluginData.getResourceIds(), resourcesDataManager.getResourceIds());

			for (ResourceId resourceId : resourcesDataManager.getResourceIds()) {
				List<Long> expectedResourceLevels = resourcesPluginData.getPersonResourceLevels(resourceId);
				for (PersonId personId : personIds) {
					long expectedLevel = 0L;
					int personIndex = personId.getValue();
					if (personIndex < expectedResourceLevels.size()) {
						Long value = expectedResourceLevels.get(personIndex);
						if (value != null) {
							expectedLevel = value;
						}
					}
					long actualLevel = resourcesDataManager.getPersonResourceLevel(resourceId, personId);
					assertEquals(expectedLevel, actualLevel);
				}
			}

			for (ResourceId resourceId : resourcesDataManager.getResourceIds()) {
				boolean trackTimes = resourcesPluginData.getResourceTimeTrackingPolicy(resourceId);
				if (trackTimes) {
					List<Double> expectedResourceTimes = resourcesPluginData.getPersonResourceTimes(resourceId);
					Double resourceDefaultTime = resourcesPluginData.getResourceDefaultTime(resourceId);
					for (PersonId personId : personIds) {
						double expectedTime = resourceDefaultTime;
						int personIndex = personId.getValue();
						if (personIndex < expectedResourceTimes.size()) {
							Double time = expectedResourceTimes.get(personIndex);
							if (time != null) {
								expectedTime = time;
							}
						}
						double actualTime = resourcesDataManager.getPersonResourceTime(resourceId, personId);
						assertEquals(expectedTime, actualTime);
					}
				}
			}

			for (ResourceId resourceId : resourcesPluginData.getResourceIds()) {
				Set<ResourcePropertyId> expectedResourcePropertyIds = resourcesPluginData
						.getResourcePropertyIds(resourceId);
				Set<ResourcePropertyId> actualResourcePropertyIds = resourcesDataManager
						.getResourcePropertyIds(resourceId);
				assertEquals(expectedResourcePropertyIds, actualResourcePropertyIds);

				for (ResourcePropertyId resourcePropertyId : expectedResourcePropertyIds) {
					PropertyDefinition expectedDefinition = resourcesPluginData
							.getResourcePropertyDefinition(resourceId, resourcePropertyId);
					PropertyDefinition actualDefinition = resourcesDataManager.getResourcePropertyDefinition(resourceId,
							resourcePropertyId);
					assertEquals(expectedDefinition, actualDefinition);

					Optional<Object> optional = resourcesPluginData.getResourcePropertyValue(resourceId,
							resourcePropertyId);
					assertTrue(optional.isPresent());
					Object expectedValue = optional.get();
					Object actualValue = resourcesDataManager.getResourcePropertyValue(resourceId, resourcePropertyId);
					assertEquals(expectedValue, actualValue);

				}
			}

		}));

		TestPluginData testPluginData = pluginBuilder.build();
		Factory factory = ResourcesTestPluginFactory.factory(initialPopulation, initialPopulation, testPluginData)
				.setResourcesPluginData(resourcesPluginData);
		TestSimulation.builder().addPlugins(factory.getPlugins()).build().execute();

	}

	@Test
	@UnitTestMethod(target = ResourcesDataManager.class, name = "getEventFilterForPersonResourceUpdateEvent", args = {
			ResourceId.class })
	public void testGetEventFilterForPersonResourceUpdateEvent_Resource() {

		Set<TestResourceId> selectedResources = new LinkedHashSet<>();
		selectedResources.add(TestResourceId.RESOURCE_1);
		selectedResources.add(TestResourceId.RESOURCE_3);
		selectedResources.add(TestResourceId.RESOURCE_5);

		TestPluginData.Builder pluginBuilder = TestPluginData.builder();

		Set<MultiKey> expectedObservations = new LinkedHashSet<>();
		Set<MultiKey> actualObservations = new LinkedHashSet<>();

		// Have an actor add resources to the regions
		pluginBuilder.addTestActorPlan("actor", new TestActorPlan(0, (c) -> {
			PeopleDataManager peopleDataManager = c.getDataManager(PeopleDataManager.class);
			RegionsDataManager regionLocationDataManager = c.getDataManager(RegionsDataManager.class);
			ResourcesDataManager resourcesDataManager = c.getDataManager(ResourcesDataManager.class);

			List<PersonId> people = peopleDataManager.getPeople();

			// add resources to all regions
			for (PersonId personId : people) {
				RegionId regionId = regionLocationDataManager.getPersonRegion(personId);
				for (TestResourceId testResourceId : TestResourceId.values()) {
					resourcesDataManager.addResourceToRegion(testResourceId, regionId, 1L);
				}
			}

		}));

		// Have an actor observe the resource transfers
		pluginBuilder.addTestActorPlan("observer", new TestActorPlan(1, (c) -> {

			ResourcesDataManager resourcesDataManager = c.getDataManager(ResourcesDataManager.class);

			for (TestResourceId testResourceId : selectedResources) {
				EventFilter<PersonResourceUpdateEvent> eventFilter = resourcesDataManager
						.getEventFilterForPersonResourceUpdateEvent(testResourceId);
				c.subscribe(eventFilter, (c2, e) -> {
					actualObservations.add(new MultiKey(c.getTime(), e.personId(), e.resourceId()));
				});
			}

		}));

		int comparisonDay = 100;

		// Have an actor transfer the resources to people
		pluginBuilder.addTestActorPlan("actor", new TestActorPlan(1, (c) -> {

			StochasticsDataManager stochasticsDataManager = c.getDataManager(StochasticsDataManager.class);
			RandomGenerator randomGenerator = stochasticsDataManager.getRandomGenerator();
			ResourcesDataManager resourcesDataManager = c.getDataManager(ResourcesDataManager.class);
			PeopleDataManager peopleDataManager = c.getDataManager(PeopleDataManager.class);
			RegionsDataManager regionsDataManager = c.getDataManager(RegionsDataManager.class);
			List<PersonId> people = peopleDataManager.getPeople();

			for (int i = 2; i < comparisonDay; i++) {
				c.addPlan((c2) -> {
					TestResourceId testResourceId = TestResourceId.getRandomResourceId(randomGenerator);
					PersonId personId = people.get(randomGenerator.nextInt(people.size()));
					RegionId regionId = regionsDataManager.getPersonRegion(personId);
					long resourceLevel = resourcesDataManager.getRegionResourceLevel(regionId, testResourceId);
					if (resourceLevel > 0) {
						resourcesDataManager.transferResourceToPersonFromRegion(testResourceId, personId, 1L);
						if (selectedResources.contains(testResourceId)) {
							expectedObservations.add(new MultiKey(c2.getTime(), personId, testResourceId));
						}
					}
				}, i);
			}

		}));

		// Have an actor show that the proper observations were generated
		pluginBuilder.addTestActorPlan("observer", new TestActorPlan(comparisonDay, (c) -> {
			assertEquals(expectedObservations, actualObservations);
		}));

		TestPluginData testPluginData = pluginBuilder.build();
		Factory factory = ResourcesTestPluginFactory.factory(30, 4043641365602447479L, testPluginData);
		TestSimulation.builder().addPlugins(factory.getPlugins()).build().execute();

		/* precondition test: if the resource id is null */
		ContractException contractException = assertThrows(ContractException.class, () -> {
			Factory factory2 = ResourcesTestPluginFactory.factory(30, 5107085853667531414L, (c) -> {
				ResourcesDataManager resourcesDataManager = c.getDataManager(ResourcesDataManager.class);
				resourcesDataManager.getEventFilterForPersonResourceUpdateEvent(null);
			});
			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();
		});
		assertEquals(ResourceError.NULL_RESOURCE_ID, contractException.getErrorType());

		/* precondition test: if the resource id is unknown */
		contractException = assertThrows(ContractException.class, () -> {
			Factory factory2 = ResourcesTestPluginFactory.factory(30, 5551635264070855342L, (c) -> {
				ResourcesDataManager resourcesDataManager = c.getDataManager(ResourcesDataManager.class);
				resourcesDataManager.getEventFilterForPersonResourceUpdateEvent(TestResourceId.getUnknownResourceId());
			});
			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();
		});
		assertEquals(ResourceError.UNKNOWN_RESOURCE_ID, contractException.getErrorType());

	}

	@Test
	@UnitTestMethod(target = ResourcesDataManager.class, name = "getEventFilterForPersonResourceUpdateEvent", args = {
			ResourceId.class, PersonId.class })
	public void testGetEventFilterForPersonResourceUpdateEvent_Resource_Person() {

		Set<TestResourceId> selectedResources = new LinkedHashSet<>();
		selectedResources.add(TestResourceId.RESOURCE_1);
		selectedResources.add(TestResourceId.RESOURCE_3);
		selectedResources.add(TestResourceId.RESOURCE_5);

		Set<PersonId> selectedPeople = new LinkedHashSet<>();
		selectedPeople.add(new PersonId(22));
		selectedPeople.add(new PersonId(8));
		selectedPeople.add(new PersonId(5));

		TestPluginData.Builder pluginBuilder = TestPluginData.builder();

		Set<MultiKey> expectedObservations = new LinkedHashSet<>();
		Set<MultiKey> actualObservations = new LinkedHashSet<>();

		// Have an actor add resources to the regions
		pluginBuilder.addTestActorPlan("actor", new TestActorPlan(0, (c) -> {

			PeopleDataManager peopleDataManager = c.getDataManager(PeopleDataManager.class);
			RegionsDataManager regionLocationDataManager = c.getDataManager(RegionsDataManager.class);
			ResourcesDataManager resourcesDataManager = c.getDataManager(ResourcesDataManager.class);

			List<PersonId> people = peopleDataManager.getPeople();

			// add resources to all regions
			for (PersonId personId : people) {
				RegionId regionId = regionLocationDataManager.getPersonRegion(personId);
				for (TestResourceId testResourceId : TestResourceId.values()) {
					resourcesDataManager.addResourceToRegion(testResourceId, regionId, 1L);
				}
			}

		}));

		// Have an actor observe the resource transfers
		pluginBuilder.addTestActorPlan("observer", new TestActorPlan(1, (c) -> {

			ResourcesDataManager resourcesDataManager = c.getDataManager(ResourcesDataManager.class);

			for (TestResourceId testResourceId : selectedResources) {
				for (PersonId personId : selectedPeople) {
					EventFilter<PersonResourceUpdateEvent> eventFilter = resourcesDataManager
							.getEventFilterForPersonResourceUpdateEvent(testResourceId, personId);
					c.subscribe(eventFilter, (c2, e) -> {
						actualObservations.add(new MultiKey(c.getTime(), e.personId(), e.resourceId()));
					});
				}
			}

		}));

		int comparisonDay = 100;

		// Have an actor transfer the resources to people
		pluginBuilder.addTestActorPlan("actor", new TestActorPlan(1, (c) -> {

			StochasticsDataManager stochasticsDataManager = c.getDataManager(StochasticsDataManager.class);
			RandomGenerator randomGenerator = stochasticsDataManager.getRandomGenerator();
			ResourcesDataManager resourcesDataManager = c.getDataManager(ResourcesDataManager.class);
			PeopleDataManager peopleDataManager = c.getDataManager(PeopleDataManager.class);
			RegionsDataManager regionsDataManager = c.getDataManager(RegionsDataManager.class);
			List<PersonId> people = peopleDataManager.getPeople();

			for (int i = 2; i < comparisonDay; i++) {
				c.addPlan((c2) -> {
					TestResourceId testResourceId = TestResourceId.getRandomResourceId(randomGenerator);
					PersonId personId = people.get(randomGenerator.nextInt(people.size()));
					RegionId regionId = regionsDataManager.getPersonRegion(personId);
					long resourceLevel = resourcesDataManager.getRegionResourceLevel(regionId, testResourceId);
					if (resourceLevel > 0) {
						resourcesDataManager.transferResourceToPersonFromRegion(testResourceId, personId, 1L);
						if (selectedResources.contains(testResourceId) && selectedPeople.contains(personId)) {
							expectedObservations.add(new MultiKey(c2.getTime(), personId, testResourceId));
						}
					}
				}, i);
			}

		}));

		// Have an actor show that the proper observations were generated
		pluginBuilder.addTestActorPlan("observer", new TestActorPlan(comparisonDay, (c) -> {
			assertEquals(expectedObservations, actualObservations);
		}));

		TestPluginData testPluginData = pluginBuilder.build();
		Factory factory = ResourcesTestPluginFactory.factory(30, 3776094770483573425L, testPluginData);
		TestSimulation.builder().addPlugins(factory.getPlugins()).build().execute();

		/* precondition test: if the resource id is null */
		ContractException contractException = assertThrows(ContractException.class, () -> {
			Factory factory2 = ResourcesTestPluginFactory.factory(30, 8909938597230752836L, (c) -> {
				ResourcesDataManager resourcesDataManager = c.getDataManager(ResourcesDataManager.class);
				resourcesDataManager.getEventFilterForPersonResourceUpdateEvent(null, new PersonId(0));
			});
			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();
		});
		assertEquals(ResourceError.NULL_RESOURCE_ID, contractException.getErrorType());

		/* precondition test: if the resource id is unknown */
		contractException = assertThrows(ContractException.class, () -> {
			Factory factory2 = ResourcesTestPluginFactory.factory(30, 4146350189128134907L, (c) -> {
				ResourcesDataManager resourcesDataManager = c.getDataManager(ResourcesDataManager.class);
				resourcesDataManager.getEventFilterForPersonResourceUpdateEvent(TestResourceId.getUnknownResourceId(),
						new PersonId(0));
			});
			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();
		});
		assertEquals(ResourceError.UNKNOWN_RESOURCE_ID, contractException.getErrorType());

		/* precondition test: if the person id is null */
		contractException = assertThrows(ContractException.class, () -> {
			Factory factory2 = ResourcesTestPluginFactory.factory(30, 8356399638914398643L, (c) -> {
				ResourcesDataManager resourcesDataManager = c.getDataManager(ResourcesDataManager.class);
				PersonId nullPersonId = null;
				resourcesDataManager.getEventFilterForPersonResourceUpdateEvent(TestResourceId.RESOURCE_1,
						nullPersonId);
			});
			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();
		});
		assertEquals(PersonError.NULL_PERSON_ID, contractException.getErrorType());

		/* precondition test: if the person id is unknown */
		contractException = assertThrows(ContractException.class, () -> {
			Factory factory2 = ResourcesTestPluginFactory.factory(30, 3890936504108305392L, (c) -> {
				ResourcesDataManager resourcesDataManager = c.getDataManager(ResourcesDataManager.class);
				PersonId unknownPersonId = new PersonId(100000);
				resourcesDataManager.getEventFilterForPersonResourceUpdateEvent(TestResourceId.RESOURCE_1,
						unknownPersonId);
			});
			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();
		});
		assertEquals(PersonError.UNKNOWN_PERSON_ID, contractException.getErrorType());

	}

	@Test
	@UnitTestMethod(target = ResourcesDataManager.class, name = "getEventFilterForPersonResourceUpdateEvent", args = {
			ResourceId.class, RegionId.class })
	public void testGetEventFilterForPersonResourceUpdateEvent_Resource_Region() {
		Set<TestResourceId> selectedResources = new LinkedHashSet<>();
		selectedResources.add(TestResourceId.RESOURCE_1);
		selectedResources.add(TestResourceId.RESOURCE_3);
		selectedResources.add(TestResourceId.RESOURCE_5);

		Set<RegionId> selectedRegions = new LinkedHashSet<>();
		selectedRegions.add(TestRegionId.REGION_1);
		selectedRegions.add(TestRegionId.REGION_5);
		selectedRegions.add(TestRegionId.REGION_6);

		TestPluginData.Builder pluginBuilder = TestPluginData.builder();

		Set<MultiKey> expectedObservations = new LinkedHashSet<>();
		Set<MultiKey> actualObservations = new LinkedHashSet<>();

		// Have an actor add resources to the regions
		pluginBuilder.addTestActorPlan("actor", new TestActorPlan(0, (c) -> {

			PeopleDataManager peopleDataManager = c.getDataManager(PeopleDataManager.class);
			RegionsDataManager regionLocationDataManager = c.getDataManager(RegionsDataManager.class);
			ResourcesDataManager resourcesDataManager = c.getDataManager(ResourcesDataManager.class);

			List<PersonId> people = peopleDataManager.getPeople();

			// add resources to all regions
			for (PersonId personId : people) {
				RegionId regionId = regionLocationDataManager.getPersonRegion(personId);
				for (TestResourceId testResourceId : TestResourceId.values()) {
					resourcesDataManager.addResourceToRegion(testResourceId, regionId, 1L);
				}
			}

		}));

		// Have an actor observe the resource transfers
		pluginBuilder.addTestActorPlan("observer", new TestActorPlan(1, (c) -> {

			ResourcesDataManager resourcesDataManager = c.getDataManager(ResourcesDataManager.class);

			for (TestResourceId testResourceId : selectedResources) {
				for (RegionId regionId : selectedRegions) {
					EventFilter<PersonResourceUpdateEvent> eventFilter = resourcesDataManager
							.getEventFilterForPersonResourceUpdateEvent(testResourceId, regionId);
					c.subscribe(eventFilter, (c2, e) -> {
						actualObservations.add(new MultiKey(c.getTime(), e.personId(), e.resourceId()));
					});
				}
			}

		}));

		int comparisonDay = 100;

		// Have an actor transfer the resources to people
		pluginBuilder.addTestActorPlan("actor", new TestActorPlan(1, (c) -> {

			StochasticsDataManager stochasticsDataManager = c.getDataManager(StochasticsDataManager.class);
			RandomGenerator randomGenerator = stochasticsDataManager.getRandomGenerator();
			ResourcesDataManager resourcesDataManager = c.getDataManager(ResourcesDataManager.class);
			PeopleDataManager peopleDataManager = c.getDataManager(PeopleDataManager.class);
			RegionsDataManager regionsDataManager = c.getDataManager(RegionsDataManager.class);
			List<PersonId> people = peopleDataManager.getPeople();

			for (int i = 2; i < comparisonDay; i++) {
				c.addPlan((c2) -> {
					TestResourceId testResourceId = TestResourceId.getRandomResourceId(randomGenerator);
					PersonId personId = people.get(randomGenerator.nextInt(people.size()));
					RegionId regionId = regionsDataManager.getPersonRegion(personId);
					long resourceLevel = resourcesDataManager.getRegionResourceLevel(regionId, testResourceId);
					if (resourceLevel > 0) {
						resourcesDataManager.transferResourceToPersonFromRegion(testResourceId, personId, 1L);
						if (selectedResources.contains(testResourceId) && selectedRegions.contains(regionId)) {
							expectedObservations.add(new MultiKey(c2.getTime(), personId, testResourceId));
						}
					}
				}, i);
			}

		}));

		// Have an actor show that the proper observations were generated
		pluginBuilder.addTestActorPlan("observer", new TestActorPlan(comparisonDay, (c) -> {
			assertEquals(expectedObservations, actualObservations);
		}));

		TestPluginData testPluginData = pluginBuilder.build();
		Factory factory = ResourcesTestPluginFactory.factory(30, 1727074366899837142L, testPluginData);
		TestSimulation.builder().addPlugins(factory.getPlugins()).build().execute();

		/* precondition test: if the resource id is null */
		ContractException contractException = assertThrows(ContractException.class, () -> {
			Factory factory2 = ResourcesTestPluginFactory.factory(30, 7693743966390586978L, (c) -> {
				ResourcesDataManager resourcesDataManager = c.getDataManager(ResourcesDataManager.class);
				resourcesDataManager.getEventFilterForPersonResourceUpdateEvent(null, new PersonId(0));
			});
			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();
		});
		assertEquals(ResourceError.NULL_RESOURCE_ID, contractException.getErrorType());

		/* precondition test: if the resource id is unknown */
		contractException = assertThrows(ContractException.class, () -> {
			Factory factory2 = ResourcesTestPluginFactory.factory(30, 693173450564289263L, (c) -> {
				ResourcesDataManager resourcesDataManager = c.getDataManager(ResourcesDataManager.class);
				resourcesDataManager.getEventFilterForPersonResourceUpdateEvent(TestResourceId.getUnknownResourceId(),
						new PersonId(0));
			});
			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();
		});
		assertEquals(ResourceError.UNKNOWN_RESOURCE_ID, contractException.getErrorType());

		/* precondition test: if the region id is null */
		contractException = assertThrows(ContractException.class, () -> {
			Factory factory2 = ResourcesTestPluginFactory.factory(30, 9201364062172125070L, (c) -> {
				ResourcesDataManager resourcesDataManager = c.getDataManager(ResourcesDataManager.class);
				RegionId nullRegionId = null;
				resourcesDataManager.getEventFilterForPersonResourceUpdateEvent(TestResourceId.RESOURCE_1,
						nullRegionId);
			});
			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();
		});
		assertEquals(RegionError.NULL_REGION_ID, contractException.getErrorType());

		/* precondition test: if the region id is unknown */
		contractException = assertThrows(ContractException.class, () -> {
			Factory factory2 = ResourcesTestPluginFactory.factory(30, 5569918148190340272L, (c) -> {
				ResourcesDataManager resourcesDataManager = c.getDataManager(ResourcesDataManager.class);
				RegionId unknownRegionId = TestRegionId.getUnknownRegionId();
				resourcesDataManager.getEventFilterForPersonResourceUpdateEvent(TestResourceId.RESOURCE_1,
						unknownRegionId);
			});
			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();
		});
		assertEquals(RegionError.UNKNOWN_REGION_ID, contractException.getErrorType());

	}

	@Test
	@UnitTestMethod(target = ResourcesDataManager.class, name = "getEventFilterForPersonResourceUpdateEvent", args = {})
	public void testGetEventFilterForPersonResourceUpdateEvent() {

		TestPluginData.Builder pluginBuilder = TestPluginData.builder();

		Set<MultiKey> expectedObservations = new LinkedHashSet<>();
		Set<MultiKey> actualObservations = new LinkedHashSet<>();

		// Have an actor add resources to the regions
		pluginBuilder.addTestActorPlan("actor", new TestActorPlan(0, (c) -> {

			PeopleDataManager peopleDataManager = c.getDataManager(PeopleDataManager.class);
			RegionsDataManager regionLocationDataManager = c.getDataManager(RegionsDataManager.class);
			ResourcesDataManager resourcesDataManager = c.getDataManager(ResourcesDataManager.class);

			List<PersonId> people = peopleDataManager.getPeople();

			// add resources to all regions
			for (PersonId personId : people) {
				RegionId regionId = regionLocationDataManager.getPersonRegion(personId);
				for (TestResourceId testResourceId : TestResourceId.values()) {
					resourcesDataManager.addResourceToRegion(testResourceId, regionId, 1L);
				}
			}

		}));

		// Have an actor observe the resource transfers
		pluginBuilder.addTestActorPlan("observer", new TestActorPlan(1, (c) -> {

			ResourcesDataManager resourcesDataManager = c.getDataManager(ResourcesDataManager.class);

			EventFilter<PersonResourceUpdateEvent> eventFilter = resourcesDataManager
					.getEventFilterForPersonResourceUpdateEvent();
			c.subscribe(eventFilter, (c2, e) -> {
				actualObservations.add(new MultiKey(c.getTime(), e.personId(), e.resourceId()));
			});

		}));

		int comparisonDay = 100;

		// Have an actor transfer the resources to people
		pluginBuilder.addTestActorPlan("actor", new TestActorPlan(1, (c) -> {

			StochasticsDataManager stochasticsDataManager = c.getDataManager(StochasticsDataManager.class);
			RandomGenerator randomGenerator = stochasticsDataManager.getRandomGenerator();
			ResourcesDataManager resourcesDataManager = c.getDataManager(ResourcesDataManager.class);
			PeopleDataManager peopleDataManager = c.getDataManager(PeopleDataManager.class);
			RegionsDataManager regionsDataManager = c.getDataManager(RegionsDataManager.class);
			List<PersonId> people = peopleDataManager.getPeople();

			for (int i = 2; i < comparisonDay; i++) {
				c.addPlan((c2) -> {
					TestResourceId testResourceId = TestResourceId.getRandomResourceId(randomGenerator);
					PersonId personId = people.get(randomGenerator.nextInt(people.size()));
					RegionId regionId = regionsDataManager.getPersonRegion(personId);
					long resourceLevel = resourcesDataManager.getRegionResourceLevel(regionId, testResourceId);
					if (resourceLevel > 0) {
						resourcesDataManager.transferResourceToPersonFromRegion(testResourceId, personId, 1L);
						expectedObservations.add(new MultiKey(c2.getTime(), personId, testResourceId));
					}
				}, i);
			}

		}));

		// Have an actor show that the proper observations were generated
		pluginBuilder.addTestActorPlan("observer", new TestActorPlan(comparisonDay, (c) -> {
			assertEquals(expectedObservations, actualObservations);
		}));

		TestPluginData testPluginData = pluginBuilder.build();
		Factory factory = ResourcesTestPluginFactory.factory(30, 1345117947886682832L, testPluginData);
		TestSimulation.builder().addPlugins(factory.getPlugins()).build().execute();

	}

	@Test
	@UnitTestMethod(target = ResourcesDataManager.class, name = "getEventFilterForRegionResourceUpdateEvent", args = {
			ResourceId.class })
	public void testGetEventFilterForRegionResourceUpdateEvent_Resource() {
		TestPluginData.Builder pluginBuilder = TestPluginData.builder();

		Set<MultiKey> expectedObservations = new LinkedHashSet<>();
		Set<MultiKey> actualObservations = new LinkedHashSet<>();

		Set<TestResourceId> selectedResources = new LinkedHashSet<>();
		selectedResources.add(TestResourceId.RESOURCE_1);
		selectedResources.add(TestResourceId.RESOURCE_3);
		selectedResources.add(TestResourceId.RESOURCE_4);

		// Have an actor to observe the selected resource changes

		pluginBuilder.addTestActorPlan("observer", new TestActorPlan(0, (c) -> {
			ResourcesDataManager resourcesDataManager = c.getDataManager(ResourcesDataManager.class);
			for (TestResourceId testResourceId : selectedResources) {
				EventFilter<RegionResourceUpdateEvent> eventFilter = resourcesDataManager
						.getEventFilterForRegionResourceUpdateEvent(testResourceId);
				c.subscribe(eventFilter, (c2, e) -> {
					actualObservations.add(new MultiKey(e.regionId(), e.resourceId(), e.previousResourceLevel(),
							e.currentResourceLevel()));
				});
			}
		}));

		int comparisonDay = 100;

		// Have an actor add resources to regions
		pluginBuilder.addTestActorPlan("actor", new TestActorPlan(1, (c) -> {

			StochasticsDataManager stochasticsDataManager = c.getDataManager(StochasticsDataManager.class);
			RandomGenerator randomGenerator = stochasticsDataManager.getRandomGenerator();
			ResourcesDataManager resourcesDataManager = c.getDataManager(ResourcesDataManager.class);

			// add random amounts of resources to regions

			for (int i = 2; i < comparisonDay; i++) {
				c.addPlan((c2) -> {
					// select random regions and resources
					TestRegionId regionId = TestRegionId.getRandomRegionId(randomGenerator);
					TestResourceId resourceId = TestResourceId.getRandomResourceId(randomGenerator);
					long regionLevel = resourcesDataManager.getRegionResourceLevel(regionId, resourceId);

					// select an amount to add
					long amount = randomGenerator.nextInt(100) + 1;
					resourcesDataManager.addResourceToRegion(resourceId, regionId, amount);

					if (selectedResources.contains(resourceId)) {
						expectedObservations.add(new MultiKey(regionId, resourceId, regionLevel, regionLevel + amount));
					}

				}, i);
			}

		}));

		// have the observer show that the correct observations were generated
		pluginBuilder.addTestActorPlan("observer", new TestActorPlan(comparisonDay, (c) -> {
			assertFalse(expectedObservations.isEmpty());
			assertEquals(expectedObservations, actualObservations);
		}));

		TestPluginData testPluginData = pluginBuilder.build();
		Factory factory = ResourcesTestPluginFactory.factory(0, 2870952108296201475L, testPluginData);
		TestSimulation.builder().addPlugins(factory.getPlugins()).build().execute();

		/* precondition test: if the resource id is null */
		ContractException contractException = assertThrows(ContractException.class, () -> {
			Factory factory2 = ResourcesTestPluginFactory.factory(0, 9101711257710159283L, (c) -> {
				ResourcesDataManager resourcesDataManager = c.getDataManager(ResourcesDataManager.class);
				ResourceId nullResourceId = null;
				resourcesDataManager.getEventFilterForRegionResourceUpdateEvent(nullResourceId);
			});
			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();
		});
		assertEquals(ResourceError.NULL_RESOURCE_ID, contractException.getErrorType());

		/* precondition test: if the resource id is unknown */
		contractException = assertThrows(ContractException.class, () -> {
			Factory factory2 = ResourcesTestPluginFactory.factory(0, 4216397684435821705L, (c) -> {
				ResourcesDataManager resourcesDataManager = c.getDataManager(ResourcesDataManager.class);
				ResourceId unknownResourceId = TestResourceId.getUnknownResourceId();
				resourcesDataManager.getEventFilterForRegionResourceUpdateEvent(unknownResourceId);
			});
			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();
		});
		assertEquals(ResourceError.UNKNOWN_RESOURCE_ID, contractException.getErrorType());
	}

	@Test
	@UnitTestMethod(target = ResourcesDataManager.class, name = "getEventFilterForRegionResourceUpdateEvent", args = {
			ResourceId.class, RegionId.class })
	public void testGetEventFilterForRegionResourceUpdateEvent_Resource_Region() {
		TestPluginData.Builder pluginBuilder = TestPluginData.builder();

		Set<MultiKey> expectedObservations = new LinkedHashSet<>();
		Set<MultiKey> actualObservations = new LinkedHashSet<>();

		Set<Pair<TestRegionId, TestResourceId>> selectedRegionResourcePairs = new LinkedHashSet<>();
		selectedRegionResourcePairs.add(new Pair<>(TestRegionId.REGION_2, TestResourceId.RESOURCE_1));
		selectedRegionResourcePairs.add(new Pair<>(TestRegionId.REGION_5, TestResourceId.RESOURCE_2));
		selectedRegionResourcePairs.add(new Pair<>(TestRegionId.REGION_2, TestResourceId.RESOURCE_3));
		selectedRegionResourcePairs.add(new Pair<>(TestRegionId.REGION_2, TestResourceId.RESOURCE_4));
		selectedRegionResourcePairs.add(new Pair<>(TestRegionId.REGION_4, TestResourceId.RESOURCE_5));
		selectedRegionResourcePairs.add(new Pair<>(TestRegionId.REGION_4, TestResourceId.RESOURCE_3));

		// Have an actor to observe the selected resource changes
		pluginBuilder.addTestActorPlan("observer", new TestActorPlan(0, (c) -> {
			ResourcesDataManager resourcesDataManager = c.getDataManager(ResourcesDataManager.class);

			for (Pair<TestRegionId, TestResourceId> pair : selectedRegionResourcePairs) {
				TestRegionId regionId = pair.getFirst();
				TestResourceId resourceId = pair.getSecond();
				EventFilter<RegionResourceUpdateEvent> eventFilter = resourcesDataManager
						.getEventFilterForRegionResourceUpdateEvent(resourceId, regionId);
				c.subscribe(eventFilter, (c2, e) -> {
					actualObservations.add(new MultiKey(e.regionId(), e.resourceId(), e.previousResourceLevel(),
							e.currentResourceLevel()));
				});
			}
		}));

		int comparisonDay = 100;

		// Have an actor add resources to regions
		pluginBuilder.addTestActorPlan("actor", new TestActorPlan(1, (c) -> {

			StochasticsDataManager stochasticsDataManager = c.getDataManager(StochasticsDataManager.class);
			RandomGenerator randomGenerator = stochasticsDataManager.getRandomGenerator();
			ResourcesDataManager resourcesDataManager = c.getDataManager(ResourcesDataManager.class);

			// add random amounts of resources to regions

			for (int i = 2; i < comparisonDay; i++) {
				c.addPlan((c2) -> {
					// select random regions and resources
					TestRegionId regionId = TestRegionId.getRandomRegionId(randomGenerator);
					TestResourceId resourceId = TestResourceId.getRandomResourceId(randomGenerator);
					long regionLevel = resourcesDataManager.getRegionResourceLevel(regionId, resourceId);

					// select an amount to add
					long amount = randomGenerator.nextInt(100) + 1;
					resourcesDataManager.addResourceToRegion(resourceId, regionId, amount);
					Pair<TestRegionId, TestResourceId> pair = new Pair<>(regionId, resourceId);
					if (selectedRegionResourcePairs.contains(pair)) {
						expectedObservations.add(new MultiKey(regionId, resourceId, regionLevel, regionLevel + amount));
					}

				}, i);
			}

		}));

		// have the observer show that the correct observations were generated
		pluginBuilder.addTestActorPlan("observer", new TestActorPlan(comparisonDay, (c) -> {
			assertFalse(expectedObservations.isEmpty());
			assertEquals(expectedObservations, actualObservations);
		}));

		TestPluginData testPluginData = pluginBuilder.build();
		Factory factory = ResourcesTestPluginFactory.factory(0, 9022862258230350395L, testPluginData);
		TestSimulation.builder().addPlugins(factory.getPlugins()).build().execute();

		/* precondition test: if the resource id is null */
		ContractException contractException = assertThrows(ContractException.class, () -> {
			Factory factory2 = ResourcesTestPluginFactory.factory(0, 217976606974469406L, (c) -> {
				ResourcesDataManager resourcesDataManager = c.getDataManager(ResourcesDataManager.class);
				ResourceId nullResourceId = null;
				resourcesDataManager.getEventFilterForRegionResourceUpdateEvent(nullResourceId);
			});
			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();
		});
		assertEquals(ResourceError.NULL_RESOURCE_ID, contractException.getErrorType());

		/* precondition test: if the resource id is unknown */
		contractException = assertThrows(ContractException.class, () -> {
			Factory factory2 = ResourcesTestPluginFactory.factory(0, 8125399461811894989L, (c) -> {
				ResourcesDataManager resourcesDataManager = c.getDataManager(ResourcesDataManager.class);
				ResourceId unknownResourceId = TestResourceId.getUnknownResourceId();
				resourcesDataManager.getEventFilterForRegionResourceUpdateEvent(unknownResourceId);
			});
			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();
		});
		assertEquals(ResourceError.UNKNOWN_RESOURCE_ID, contractException.getErrorType());

	}

	@Test
	@UnitTestMethod(target = ResourcesDataManager.class, name = "getEventFilterForRegionResourceUpdateEvent", args = {})
	public void testGetEventFilterForRegionResourceUpdateEvent() {
		TestPluginData.Builder pluginBuilder = TestPluginData.builder();

		Set<MultiKey> expectedObservations = new LinkedHashSet<>();
		Set<MultiKey> actualObservations = new LinkedHashSet<>();

		// Have an actor to observe the selected resource changes
		pluginBuilder.addTestActorPlan("observer", new TestActorPlan(0, (c) -> {
			ResourcesDataManager resourcesDataManager = c.getDataManager(ResourcesDataManager.class);

			EventFilter<RegionResourceUpdateEvent> eventFilter = resourcesDataManager
					.getEventFilterForRegionResourceUpdateEvent();
			c.subscribe(eventFilter, (c2, e) -> {
				actualObservations.add(new MultiKey(e.regionId(), e.resourceId(), e.previousResourceLevel(),
						e.currentResourceLevel()));
			});

		}));

		int comparisonDay = 100;

		// Have an actor add resources to regions
		pluginBuilder.addTestActorPlan("actor", new TestActorPlan(1, (c) -> {

			StochasticsDataManager stochasticsDataManager = c.getDataManager(StochasticsDataManager.class);
			RandomGenerator randomGenerator = stochasticsDataManager.getRandomGenerator();
			ResourcesDataManager resourcesDataManager = c.getDataManager(ResourcesDataManager.class);

			// add random amounts of resources to regions

			for (int i = 2; i < comparisonDay; i++) {
				c.addPlan((c2) -> {
					// select random regions and resources
					TestRegionId regionId = TestRegionId.getRandomRegionId(randomGenerator);
					TestResourceId resourceId = TestResourceId.getRandomResourceId(randomGenerator);
					long regionLevel = resourcesDataManager.getRegionResourceLevel(regionId, resourceId);

					// select an amount to add
					long amount = randomGenerator.nextInt(100) + 1;
					resourcesDataManager.addResourceToRegion(resourceId, regionId, amount);
					expectedObservations.add(new MultiKey(regionId, resourceId, regionLevel, regionLevel + amount));
				}, i);
			}

		}));

		// have the observer show that the correct observations were generated
		pluginBuilder.addTestActorPlan("observer", new TestActorPlan(comparisonDay, (c) -> {
			assertFalse(expectedObservations.isEmpty());
			assertEquals(expectedObservations, actualObservations);
		}));

		TestPluginData testPluginData = pluginBuilder.build();
		Factory factory = ResourcesTestPluginFactory.factory(0, 4130610902285408287L, testPluginData);
		TestSimulation.builder().addPlugins(factory.getPlugins()).build().execute();
	}

	@Test
	@UnitTestMethod(target = ResourcesDataManager.class, name = "getEventFilterForResourcePropertyUpdateEvent", args = {
			ResourceId.class, ResourcePropertyId.class })
	public void testGetEventFilterForResourcePropertyUpdateEvent_Resource_Property() {
		TestPluginData.Builder pluginBuilder = TestPluginData.builder();

		Set<MultiKey> expectedObservations = new LinkedHashSet<>();
		Set<MultiKey> actualObservations = new LinkedHashSet<>();

		Set<Pair<TestResourceId, TestResourcePropertyId>> selectedResourcePropertyPairs = new LinkedHashSet<>();
		selectedResourcePropertyPairs
				.add(new Pair<>(TestResourceId.RESOURCE_1, TestResourcePropertyId.ResourceProperty_1_3_DOUBLE_MUTABLE));
		selectedResourcePropertyPairs.add(
				new Pair<>(TestResourceId.RESOURCE_1, TestResourcePropertyId.ResourceProperty_1_2_INTEGER_MUTABLE));
		selectedResourcePropertyPairs.add(
				new Pair<>(TestResourceId.RESOURCE_2, TestResourcePropertyId.ResourceProperty_2_2_INTEGER_MUTABLE));
		selectedResourcePropertyPairs
				.add(new Pair<>(TestResourceId.RESOURCE_3, TestResourcePropertyId.ResourceProperty_3_2_STRING_MUTABLE));
		selectedResourcePropertyPairs.add(
				new Pair<>(TestResourceId.RESOURCE_3, TestResourcePropertyId.ResourceProperty_3_1_BOOLEAN_MUTABLE));
		selectedResourcePropertyPairs.add(
				new Pair<>(TestResourceId.RESOURCE_5, TestResourcePropertyId.ResourceProperty_5_2_DOUBLE_IMMUTABLE));

		// Have an actor observe the selected resource property changes

		pluginBuilder.addTestActorPlan("observer", new TestActorPlan(0, (c) -> {
			ResourcesDataManager resourcesDataManager = c.getDataManager(ResourcesDataManager.class);
			for (Pair<TestResourceId, TestResourcePropertyId> pair : selectedResourcePropertyPairs) {
				TestResourceId testResourceId = pair.getFirst();
				TestResourcePropertyId testResourcePropertyId = pair.getSecond();
				EventFilter<ResourcePropertyUpdateEvent> eventFilter = resourcesDataManager
						.getEventFilterForResourcePropertyUpdateEvent(testResourceId, testResourcePropertyId);

				c.subscribe(eventFilter, (c2, e) -> {
					actualObservations.add(new MultiKey(c.getTime(), e.resourceId(), e.resourcePropertyId(),
							e.previousPropertyValue(), e.currentPropertyValue()));
				});

			}
		}));

		int comparisonDay = 100;

		// Have an actor assign resource properties

		pluginBuilder.addTestActorPlan("actor", new TestActorPlan(1, (c) -> {

			StochasticsDataManager stochasticsDataManager = c.getDataManager(StochasticsDataManager.class);
			RandomGenerator randomGenerator = stochasticsDataManager.getRandomGenerator();
			ResourcesDataManager resourcesDataManager = c.getDataManager(ResourcesDataManager.class);

			TestResourceId testResourceId = TestResourceId.getRandomResourceId(randomGenerator);
			TestResourcePropertyId testResourcePropertyId = TestResourcePropertyId
					.getRandomResourcePropertyId(testResourceId, randomGenerator);

			for (int i = 1; i < comparisonDay; i++) {
				c.addPlan((c2) -> {
					PropertyDefinition propertyDefinition = resourcesDataManager
							.getResourcePropertyDefinition(testResourceId, testResourcePropertyId);
					if (propertyDefinition.propertyValuesAreMutable()) {
						// update the property value
						Object resourcePropertyValue = resourcesDataManager.getResourcePropertyValue(testResourceId,
								testResourcePropertyId);
						Object expectedValue = testResourcePropertyId.getRandomPropertyValue(randomGenerator);
						resourcesDataManager.setResourcePropertyValue(testResourceId, testResourcePropertyId,
								expectedValue);

						Pair<TestResourceId, TestResourcePropertyId> pair = new Pair<>(testResourceId,
								testResourcePropertyId);
						if (selectedResourcePropertyPairs.contains(pair)) {
							expectedObservations.add(new MultiKey(c2.getTime(), testResourceId, testResourcePropertyId,
									resourcePropertyValue, expectedValue));
						}
					}
				}, i);
			}

		}));

		// Have the observer show that the observations were properly generated
		pluginBuilder.addTestActorPlan("observer", new TestActorPlan(comparisonDay, (c) -> {
			assertEquals(expectedObservations, actualObservations);
		}));

		TestPluginData testPluginData = pluginBuilder.build();
		Factory factory = ResourcesTestPluginFactory.factory(0, 4039871222190675923L, testPluginData);
		TestSimulation.builder().addPlugins(factory.getPlugins()).build().execute();

		/* precondition test: if the resource id is null */
		ContractException contractException = assertThrows(ContractException.class, () -> {
			Factory factory2 = ResourcesTestPluginFactory.factory(0, 7664472869248061620L, (c) -> {
				ResourceId resourceId = null;
				ResourcePropertyId resourcePropertyId = TestResourcePropertyId.ResourceProperty_1_2_INTEGER_MUTABLE;
				ResourcesDataManager resourcesDataManager = c.getDataManager(ResourcesDataManager.class);
				resourcesDataManager.getEventFilterForResourcePropertyUpdateEvent(resourceId, resourcePropertyId);
			});
			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();
		});
		assertEquals(ResourceError.NULL_RESOURCE_ID, contractException.getErrorType());

		/* precondition test: if the resource id is unknown */
		contractException = assertThrows(ContractException.class, () -> {
			Factory factory2 = ResourcesTestPluginFactory.factory(0, 2475328515664171695L, (c) -> {
				ResourceId resourceId = TestResourceId.getUnknownResourceId();
				ResourcePropertyId resourcePropertyId = TestResourcePropertyId.ResourceProperty_1_2_INTEGER_MUTABLE;
				ResourcesDataManager resourcesDataManager = c.getDataManager(ResourcesDataManager.class);
				resourcesDataManager.getEventFilterForResourcePropertyUpdateEvent(resourceId, resourcePropertyId);
			});
			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();
		});
		assertEquals(ResourceError.UNKNOWN_RESOURCE_ID, contractException.getErrorType());

		/* precondition test: if the resource property id is null */
		contractException = assertThrows(ContractException.class, () -> {
			Factory factory2 = ResourcesTestPluginFactory.factory(0, 7416000716392694948L, (c) -> {
				ResourceId resourceId = TestResourceId.RESOURCE_1;
				ResourcePropertyId resourcePropertyId = null;
				ResourcesDataManager resourcesDataManager = c.getDataManager(ResourcesDataManager.class);
				resourcesDataManager.getEventFilterForResourcePropertyUpdateEvent(resourceId, resourcePropertyId);
			});
			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();
		});
		assertEquals(PropertyError.NULL_PROPERTY_ID, contractException.getErrorType());

		/* precondition test: if the resource property id is unknown */
		contractException = assertThrows(ContractException.class, () -> {
			Factory factory2 = ResourcesTestPluginFactory.factory(0, 697790634696788239L, (c) -> {
				ResourceId resourceId = TestResourceId.RESOURCE_1;
				ResourcePropertyId resourcePropertyId = TestResourcePropertyId.getUnknownResourcePropertyId();
				ResourcesDataManager resourcesDataManager = c.getDataManager(ResourcesDataManager.class);
				resourcesDataManager.getEventFilterForResourcePropertyUpdateEvent(resourceId, resourcePropertyId);
			});
			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();
		});
		assertEquals(PropertyError.UNKNOWN_PROPERTY_ID, contractException.getErrorType());

		/*
		 * precondition test: if the resource property id is unknown -- in this case it
		 * is linked to a different resource
		 */
		contractException = assertThrows(ContractException.class, () -> {
			Factory factory2 = ResourcesTestPluginFactory.factory(0, 107265130769422979L, (c) -> {
				ResourceId resourceId = TestResourceId.RESOURCE_1;
				ResourcePropertyId resourcePropertyId = TestResourcePropertyId.ResourceProperty_2_1_BOOLEAN_MUTABLE;
				ResourcesDataManager resourcesDataManager = c.getDataManager(ResourcesDataManager.class);
				resourcesDataManager.getEventFilterForResourcePropertyUpdateEvent(resourceId, resourcePropertyId);
			});
			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();
		});
		assertEquals(PropertyError.UNKNOWN_PROPERTY_ID, contractException.getErrorType());

	}

	@Test
	@UnitTestMethod(target = ResourcesDataManager.class, name = "getEventFilterForResourcePropertyUpdateEvent", args = {})
	public void testGetEventFilterForResourcePropertyUpdateEvent() {
		TestPluginData.Builder pluginBuilder = TestPluginData.builder();

		Set<MultiKey> expectedObservations = new LinkedHashSet<>();
		Set<MultiKey> actualObservations = new LinkedHashSet<>();

		// Have an actor observe the selected resource property changes

		pluginBuilder.addTestActorPlan("observer", new TestActorPlan(0, (c) -> {
			ResourcesDataManager resourcesDataManager = c.getDataManager(ResourcesDataManager.class);

			EventFilter<ResourcePropertyUpdateEvent> eventFilter = resourcesDataManager
					.getEventFilterForResourcePropertyUpdateEvent();

			c.subscribe(eventFilter, (c2, e) -> {
				actualObservations.add(new MultiKey(c.getTime(), e.resourceId(), e.resourcePropertyId(),
						e.previousPropertyValue(), e.currentPropertyValue()));
			});

		}));

		int comparisonDay = 100;

		// Have an actor assign resource properties

		pluginBuilder.addTestActorPlan("actor", new TestActorPlan(1, (c) -> {

			StochasticsDataManager stochasticsDataManager = c.getDataManager(StochasticsDataManager.class);
			RandomGenerator randomGenerator = stochasticsDataManager.getRandomGenerator();
			ResourcesDataManager resourcesDataManager = c.getDataManager(ResourcesDataManager.class);

			TestResourceId testResourceId = TestResourceId.getRandomResourceId(randomGenerator);
			TestResourcePropertyId testResourcePropertyId = TestResourcePropertyId
					.getRandomResourcePropertyId(testResourceId, randomGenerator);

			for (int i = 1; i < comparisonDay; i++) {
				c.addPlan((c2) -> {
					PropertyDefinition propertyDefinition = resourcesDataManager
							.getResourcePropertyDefinition(testResourceId, testResourcePropertyId);
					if (propertyDefinition.propertyValuesAreMutable()) {
						// update the property value
						Object resourcePropertyValue = resourcesDataManager.getResourcePropertyValue(testResourceId,
								testResourcePropertyId);
						Object expectedValue = testResourcePropertyId.getRandomPropertyValue(randomGenerator);
						resourcesDataManager.setResourcePropertyValue(testResourceId, testResourcePropertyId,
								expectedValue);
						expectedObservations.add(new MultiKey(c2.getTime(), testResourceId, testResourcePropertyId,
								resourcePropertyValue, expectedValue));
					}
				}, i);
			}

		}));

		// Have the observer show that the observations were properly generated
		pluginBuilder.addTestActorPlan("observer", new TestActorPlan(comparisonDay, (c) -> {
			assertEquals(expectedObservations, actualObservations);
		}));

		TestPluginData testPluginData = pluginBuilder.build();
		Factory factory = ResourcesTestPluginFactory.factory(0, 4428711217570070234L, testPluginData);
		TestSimulation.builder().addPlugins(factory.getPlugins()).build().execute();

	}

	@Test
	@UnitTestMethod(target = ResourcesDataManager.class, name = "getEventFilterForResourceIdAdditionEvent", args = {})
	public void testGetEventFilterForResourceIdAdditionEvent() {
		ResourceId newResourceId1 = TestResourceId.getUnknownResourceId();
		ResourceId newResourceId2 = TestResourceId.getUnknownResourceId();
		Set<MultiKey> expectedObservations = new LinkedHashSet<>();
		Set<MultiKey> actualObservations = new LinkedHashSet<>();

		TestPluginData.Builder pluginBuilder = TestPluginData.builder();

		pluginBuilder.addTestActorPlan("observer", new TestActorPlan(0, (c) -> {
			ResourcesDataManager resourcesDataManager = c.getDataManager(ResourcesDataManager.class);
			EventFilter<ResourceIdAdditionEvent> eventFilter = resourcesDataManager
					.getEventFilterForResourceIdAdditionEvent();
			c.subscribe(eventFilter, (c2, e) -> {
				MultiKey multiKey = new MultiKey(c2.getTime(), e.resourceId(), e.timeTrackingPolicy());
				actualObservations.add(multiKey);
			});

		}));

		pluginBuilder.addTestActorPlan("actor", new TestActorPlan(1, (c) -> {
			ResourcesDataManager resourcesDataManager = c.getDataManager(ResourcesDataManager.class);
			boolean timeTrackingPolicy = false;
			assertFalse(resourcesDataManager.resourceIdExists(newResourceId1));
			resourcesDataManager.addResourceId(newResourceId1, timeTrackingPolicy);
			MultiKey multiKey = new MultiKey(c.getTime(), newResourceId1, false);
			expectedObservations.add(multiKey);
		}));

		pluginBuilder.addTestActorPlan("actor", new TestActorPlan(2, (c) -> {
			ResourcesDataManager resourcesDataManager = c.getDataManager(ResourcesDataManager.class);
			boolean timeTrackingPolicy = true;
			assertFalse(resourcesDataManager.resourceIdExists(newResourceId2));
			resourcesDataManager.addResourceId(newResourceId2, timeTrackingPolicy);
			MultiKey multiKey = new MultiKey(c.getTime(), newResourceId2, true);
			expectedObservations.add(multiKey);
		}));

		pluginBuilder.addTestActorPlan("observer", new TestActorPlan(3, (c) -> {
			assertEquals(expectedObservations, actualObservations);
		}));

		TestPluginData testPluginData = pluginBuilder.build();
		Factory factory = ResourcesTestPluginFactory.factory(5, 6169797168816977272L, testPluginData);
		TestSimulation.builder().addPlugins(factory.getPlugins()).build().execute();
	}

	@Test
	@UnitTestMethod(target = ResourcesDataManager.class, name = "getEventFilterForResourcePropertyDefinitionEvent", args = {})
	public void testGetEventFilterForResourcePropertyDefinitionEvent() {
		TestPluginData.Builder pluginBuilder = TestPluginData.builder();

		Set<MultiKey> expectedObservations = new LinkedHashSet<>();
		Set<MultiKey> actualObservations = new LinkedHashSet<>();

		// have an actor observe the resource property definition events
		pluginBuilder.addTestActorPlan("observer", new TestActorPlan(0, (c) -> {
			ResourcesDataManager resourcesDataManager = c.getDataManager(ResourcesDataManager.class);
			EventFilter<ResourcePropertyDefinitionEvent> eventFilter = resourcesDataManager
					.getEventFilterForResourcePropertyDefinitionEvent();
			c.subscribe(eventFilter, (c2, e) -> {
				MultiKey multiKey = new MultiKey(c2.getTime(), e.resourceId(), e.resourcePropertyId());
				actualObservations.add(multiKey);
			});
		}));

		// have an actor define a new resource property
		pluginBuilder.addTestActorPlan("actor", new TestActorPlan(1, (c) -> {
			ResourcesDataManager resourcesDataManager = c.getDataManager(ResourcesDataManager.class);
			ResourcePropertyId newResourcePropertyId = TestResourcePropertyId.getUnknownResourcePropertyId();
			PropertyDefinition propertyDefinition = PropertyDefinition.builder().setType(Double.class)
					.setDefaultValue(34.6).build();
			ResourcePropertyInitialization resourcePropertyInitialization = //
					ResourcePropertyInitialization.builder()//
							.setPropertyDefinition(propertyDefinition)//
							.setResourceId(TestResourceId.RESOURCE_1)//
							.setResourcePropertyId(newResourcePropertyId)//
							.build();
			resourcesDataManager.defineResourceProperty(resourcePropertyInitialization);
			MultiKey multiKey = new MultiKey(c.getTime(), TestResourceId.RESOURCE_1, newResourcePropertyId);
			expectedObservations.add(multiKey);
		}));

		// have an actor define a new resource property
		pluginBuilder.addTestActorPlan("actor", new TestActorPlan(2, (c) -> {
			ResourcesDataManager resourcesDataManager = c.getDataManager(ResourcesDataManager.class);
			ResourcePropertyId newResourcePropertyId = TestResourcePropertyId.getUnknownResourcePropertyId();
			PropertyDefinition propertyDefinition = PropertyDefinition.builder().setType(String.class)
					.setDefaultValue("default").build();

			ResourcePropertyInitialization resourcePropertyInitialization = //
					ResourcePropertyInitialization.builder()//
							.setPropertyDefinition(propertyDefinition)//
							.setResourceId(TestResourceId.RESOURCE_2)//
							.setResourcePropertyId(newResourcePropertyId)//
							.build();
			resourcesDataManager.defineResourceProperty(resourcePropertyInitialization);

			MultiKey multiKey = new MultiKey(c.getTime(), TestResourceId.RESOURCE_2, newResourcePropertyId);
			expectedObservations.add(multiKey);
		}));

		// have the observer verify the observations were correct
		pluginBuilder.addTestActorPlan("observer", new TestActorPlan(3, (c) -> {
			assertEquals(2, expectedObservations.size());
			assertEquals(expectedObservations, actualObservations);
		}));

		TestPluginData testPluginData = pluginBuilder.build();
		Factory factory = ResourcesTestPluginFactory.factory(5, 1942435631952524244L, testPluginData);
		TestSimulation.builder().addPlugins(factory.getPlugins()).build().execute();

	}

	/**
	 * Demonstrates that the data manager exhibits run continuity. The state of the
	 * data manager is not effected by repeatedly starting and stopping the
	 * simulation.
	 */
	@Test
	@UnitTestMethod(target = ResourcesDataManager.class, name = "init", args = { DataManagerContext.class })
	public void testStateContinuity() {

		/*
		 * Note that we are not testing the content of the plugin datas -- that is
		 * covered by the other state tests. We show here only that the resulting plugin
		 * data state is the same without regard to how we break up the run.
		 */

		Set<String> pluginDatas = new LinkedHashSet<>();
		pluginDatas.add(testStateContinuity(1));
		pluginDatas.add(testStateContinuity(5));
		pluginDatas.add(testStateContinuity(10));

		assertEquals(1, pluginDatas.size());
	}

	/*
	 * Returns the resources plugin data resulting from various resource related
	 * events over several days. Attempts to stop and start the simulation by the
	 * given number of increments.
	 */
	private String testStateContinuity(int incrementCount) {
		String result = null;

		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(177404262666515111L);

		/*
		 * Build the RunContinuityPluginData with five context consumers that will add
		 * and remove people over several days
		 */
		RunContinuityPluginData.Builder continuityBuilder = RunContinuityPluginData.builder();

		/*
		 * Add some resource ids. Add some people with one of the resources added to the
		 * people.
		 */
		continuityBuilder.addContextConsumer(0.5, (c) -> {
			ResourcesDataManager resourcesDataManager = c.getDataManager(ResourcesDataManager.class);
			PeopleDataManager peopleDataManager = c.getDataManager(PeopleDataManager.class);

			boolean trackResources = false;
			for (TestResourceId testResourceId : TestResourceId.values()) {
				resourcesDataManager.addResourceId(testResourceId, trackResources);
				trackResources = !trackResources;
			}

			for (int i = 0; i < 10; i++) {
				TestRegionId regionId = TestRegionId.getRandomRegionId(randomGenerator);
				ResourceId resourceId = TestResourceId.RESOURCE_3;
				long resourceLevel = randomGenerator.nextInt(5);
				ResourceInitialization resourceInitialization = new ResourceInitialization(resourceId, resourceLevel);
				PersonConstructionData personConstructionData = PersonConstructionData.builder()//
						.add(regionId)//
						.add(resourceInitialization)//
						.build();//
				peopleDataManager.addPerson(personConstructionData);
			}

		});

		/*
		 * Add 300 to 1300 units of each resource to each region
		 */
		continuityBuilder.addContextConsumer(1.2, (c) -> {
			ResourcesDataManager resourcesDataManager = c.getDataManager(ResourcesDataManager.class);

			List<MultiKey> list = new ArrayList<>();
			Random random = new Random(randomGenerator.nextLong());

			for (TestRegionId testRegionId : TestRegionId.values()) {
				for (TestResourceId testResourceId : TestResourceId.values()) {
					list.add(new MultiKey(testRegionId, testResourceId));
				}
			}
			Collections.shuffle(list, random);

			for (MultiKey multiKey : list) {
				TestRegionId testRegionId = multiKey.getKey(0);
				TestResourceId testResourceId = multiKey.getKey(1);
				long amount = randomGenerator.nextInt(10) * 100 + 300;
				resourcesDataManager.addResourceToRegion(testResourceId, testRegionId, amount);
			}
		});

		// add some more resources to some regions
		continuityBuilder.addContextConsumer(1.5, (c) -> {
			ResourcesDataManager resourcesDataManager = c.getDataManager(ResourcesDataManager.class);
			for (int i = 0; i < 30; i++) {
				TestRegionId testRegionId = TestRegionId.getRandomRegionId(randomGenerator);
				TestResourceId testResourceId = TestResourceId.getRandomResourceId(randomGenerator);
				long amount = randomGenerator.nextInt(1000) + 1;
				resourcesDataManager.addResourceToRegion(testResourceId, testRegionId, amount);
			}
		});

		/*
		 * define some resource properties
		 */
		continuityBuilder.addContextConsumer(1.6, (c) -> {
			ResourcesDataManager resourcesDataManager = c.getDataManager(ResourcesDataManager.class);
			for (TestResourcePropertyId testResourcePropertyId : TestResourcePropertyId.values()) {
				ResourcePropertyInitialization resourcePropertyInitialization = //
						ResourcePropertyInitialization.builder()//
								.setPropertyDefinition(testResourcePropertyId.getPropertyDefinition())//
								.setResourceId(testResourcePropertyId.getTestResourceId())
								.setResourcePropertyId(testResourcePropertyId).build();
				resourcesDataManager.defineResourceProperty(resourcePropertyInitialization);
			}

		});

		// set some resource properties
		continuityBuilder.addContextConsumer(2.2, (c) -> {
			ResourcesDataManager resourcesDataManager = c.getDataManager(ResourcesDataManager.class);

			List<TestResourcePropertyId> testResourcePropertyIds = Arrays.asList(TestResourcePropertyId.values());
			Collections.reverse(testResourcePropertyIds);

			for (TestResourcePropertyId testResourcePropertyId : testResourcePropertyIds) {

				PropertyDefinition propertyDefinition = resourcesDataManager.getResourcePropertyDefinition(
						testResourcePropertyId.getTestResourceId(), testResourcePropertyId);
				if (propertyDefinition.propertyValuesAreMutable()) {
					resourcesDataManager.setResourcePropertyValue(testResourcePropertyId.getTestResourceId(),
							testResourcePropertyId, testResourcePropertyId.getRandomPropertyValue(randomGenerator));
				}
			}

		});

		// transfer resources between regions
		continuityBuilder.addContextConsumer(2.5, (c) -> {
			ResourcesDataManager resourcesDataManager = c.getDataManager(ResourcesDataManager.class);
			for (int i = 0; i < 50; i++) {
				TestResourceId resourceId = TestResourceId.getRandomResourceId(randomGenerator);
				TestRegionId sourceRegionId = TestRegionId.getRandomRegionId(randomGenerator);
				TestRegionId destinationRegionId = TestRegionId.getRandomRegionId(randomGenerator);

				if (sourceRegionId != destinationRegionId) {

					long regionResourceLevel = resourcesDataManager.getRegionResourceLevel(sourceRegionId, resourceId);

					long amountToTransfer = regionResourceLevel / 10;

					if (amountToTransfer > 0) {
						resourcesDataManager.transferResourceBetweenRegions(resourceId, sourceRegionId,
								destinationRegionId, amountToTransfer);
					}
				}
			}

		});

		// transfer resource from regions to people
		continuityBuilder.addContextConsumer(4.6, (c) -> {
			ResourcesDataManager resourcesDataManager = c.getDataManager(ResourcesDataManager.class);
			PeopleDataManager peopleDataManager = c.getDataManager(PeopleDataManager.class);
			RegionsDataManager regionsDataManager = c.getDataManager(RegionsDataManager.class);

			List<PersonId> people = peopleDataManager.getPeople();

			for (int i = 0; i < people.size(); i++) {
				PersonId personId = people.get(randomGenerator.nextInt(people.size()));
				TestResourceId testResourceId = TestResourceId.getRandomResourceId(randomGenerator);
				RegionId regionId = regionsDataManager.getPersonRegion(personId);
				long avaialableAmount = resourcesDataManager.getRegionResourceLevel(regionId, testResourceId);
				long amount = randomGenerator.nextInt(15);
				amount = FastMath.min(amount, avaialableAmount);
				if (amount > 0) {
					resourcesDataManager.transferResourceToPersonFromRegion(testResourceId, personId, amount);
				}
			}

		});

		// transfer resources from people to regions
		continuityBuilder.addContextConsumer(4.7, (c) -> {
			ResourcesDataManager resourcesDataManager = c.getDataManager(ResourcesDataManager.class);
			PeopleDataManager peopleDataManager = c.getDataManager(PeopleDataManager.class);
			List<PersonId> people = peopleDataManager.getPeople();
			for (int i = 0; i < people.size(); i++) {
				PersonId personId = people.get(randomGenerator.nextInt(people.size()));
				TestResourceId testResourceId = TestResourceId.getRandomResourceId(randomGenerator);
				long avaialableAmount = resourcesDataManager.getPersonResourceLevel(testResourceId, personId);
				long amount = randomGenerator.nextInt(15);
				amount = FastMath.min(amount, avaialableAmount);
				if (amount > 0) {
					resourcesDataManager.transferResourceFromPersonToRegion(testResourceId, personId, amount);
				}
			}

		});

		// remove resource from people
		continuityBuilder.addContextConsumer(5.3, (c) -> {
			ResourcesDataManager resourcesDataManager = c.getDataManager(ResourcesDataManager.class);
			PeopleDataManager peopleDataManager = c.getDataManager(PeopleDataManager.class);
			List<PersonId> people = peopleDataManager.getPeople();
			for (int i = 0; i < people.size(); i++) {
				PersonId personId = people.get(randomGenerator.nextInt(people.size()));
				TestResourceId testResourceId = TestResourceId.getRandomResourceId(randomGenerator);
				long avaialableAmount = resourcesDataManager.getPersonResourceLevel(testResourceId, personId);
				long amount = randomGenerator.nextInt(15);
				amount = FastMath.min(amount, avaialableAmount);
				if (amount > 0) {
					resourcesDataManager.removeResourceFromPerson(testResourceId, personId, amount);
				}
			}

		});

		// remove resources from regions
		continuityBuilder.addContextConsumer(5.5, (c) -> {
			ResourcesDataManager resourcesDataManager = c.getDataManager(ResourcesDataManager.class);
			RegionsDataManager regionsDataManager = c.getDataManager(RegionsDataManager.class);

			List<RegionId> regionIds = new ArrayList<>(regionsDataManager.getRegionIds());

			for (int i = 0; i < regionIds.size(); i++) {
				RegionId regionId = regionIds.get(randomGenerator.nextInt(regionIds.size()));
				TestResourceId testResourceId = TestResourceId.getRandomResourceId(randomGenerator);
				long avaialableAmount = resourcesDataManager.getRegionResourceLevel(regionId, testResourceId);
				long amount = randomGenerator.nextInt(15);
				amount = FastMath.min(amount, avaialableAmount);
				if (amount > 0) {
					resourcesDataManager.removeResourceFromRegion(testResourceId, regionId, amount);
				}
			}

		});

		continuityBuilder.addContextConsumer(6.0, (c) -> {
			ResourcesDataManager resourcesDataManager = c.getDataManager(ResourcesDataManager.class);
			c.releaseOutput(resourcesDataManager.toString());
		});

		RunContinuityPluginData runContinuityPluginData = continuityBuilder.build();

		// Build an empty people plugin data for time zero
		PeoplePluginData peoplePluginData = PeoplePluginData.builder().build();

		// Build a regions plugin data with the test regions
		RegionsPluginData.Builder regionsBuilder = RegionsPluginData.builder();
		for (TestRegionId testRegionId : TestRegionId.values()) {
			regionsBuilder.addRegion(testRegionId);
		}
		RegionsPluginData regionsPluginData = regionsBuilder.build();

		// Build an empty resources plugin data
		ResourcesPluginData resourcesPluginData = ResourcesPluginData.builder().build();

		// Build the initial simulation state data -- time starts at zero
		SimulationState simulationState = SimulationState.builder().build();

		/*
		 * Run the simulation in one day increments until all the plans in the run
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
			Plugin regionsPlugin = RegionsPlugin.builder().setRegionsPluginData(regionsPluginData).getRegionsPlugin();

			// build the resources plugin
			Plugin resourcesPlugin = ResourcesPlugin.builder().setResourcesPluginData(resourcesPluginData)
					.getResourcesPlugin();

			TestOutputConsumer outputConsumer = new TestOutputConsumer();

			// execute the simulation so that it produces a people plugin data
			Simulation simulation = Simulation.builder()//
					.addPlugin(peoplePlugin)//
					.addPlugin(regionsPlugin)//
					.addPlugin(runContinuityPlugin)//
					.addPlugin(resourcesPlugin)//
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

			// retrieve the resources plugin data
			resourcesPluginData = outputConsumer.getOutputItem(ResourcesPluginData.class).get();

			// retrieve the simulation state
			simulationState = outputConsumer.getOutputItem(SimulationState.class).get();

			// retrieve the run continuity plugin data
			runContinuityPluginData = outputConsumer.getOutputItem(RunContinuityPluginData.class).get();

			Optional<String> optional = outputConsumer.getOutputItem(String.class);
			if (optional.isPresent()) {
				result = optional.get();
			}

		}

		// show that the result is a reasonably long string
		assertNotNull(result);
		assertTrue(result.length() > 100);

		return result;

	}

	@Test
	@UnitTestMethod(target = ResourcesDataManager.class, name = "toString", args = {})
	public void testToString() {	
		

		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(7634125044092781695L);
		Random random = new Random(randomGenerator.nextLong());
		
		//build the people plugin
		
		PeoplePluginData.Builder peoplePluginDataBuilder = PeoplePluginData.builder();
		for(int i = 0;i<10;i++) {
			int id = 2*i+1;
			peoplePluginDataBuilder.addPersonRange(new PersonRange(id, id));
		}
		PeoplePluginData peoplePluginData = peoplePluginDataBuilder.build();
		Plugin peoplePlugin = PeoplePlugin.getPeoplePlugin(peoplePluginData);
		
		List<PersonId> people = peoplePluginData.getPersonIds();
		
		//build the resources plugin
		ResourcesPluginData.Builder resourcesPluginDataBuilder = ResourcesPluginData.builder();

		List<TestResourceId> selectedTestResourceIds = new ArrayList<>();

		for (TestResourceId testResourceId : TestResourceId.values()) {
			selectedTestResourceIds.add(testResourceId);
		}
		Collections.shuffle(selectedTestResourceIds, random);

		Set<TestResourceId> timeTrackedResourceIds = new LinkedHashSet<>();

		for (TestResourceId testResourceId : selectedTestResourceIds) {
			double time = randomGenerator.nextDouble();
			boolean timeTrackingPolicy = randomGenerator.nextBoolean();
			if (timeTrackingPolicy) {
				timeTrackedResourceIds.add(testResourceId);
			}
			resourcesPluginDataBuilder.addResource(testResourceId, time, timeTrackingPolicy);
		}

		List<TestResourcePropertyId> selectedTestResourcePropertyIds = new ArrayList<>();

		for (TestResourcePropertyId testResourcePropertyId : TestResourcePropertyId.values()) {
			selectedTestResourcePropertyIds.add(testResourcePropertyId);
		}

		Collections.shuffle(selectedTestResourcePropertyIds, random);

		for (TestResourcePropertyId testResourcePropertyId : selectedTestResourcePropertyIds) {
			resourcesPluginDataBuilder.defineResourceProperty(testResourcePropertyId.getTestResourceId(),
					testResourcePropertyId, testResourcePropertyId.getPropertyDefinition());
		}

		for (TestResourcePropertyId testResourcePropertyId : selectedTestResourcePropertyIds) {
			boolean required = testResourcePropertyId.getPropertyDefinition().getDefaultValue().isEmpty();
			if (required || randomGenerator.nextBoolean()) {
				resourcesPluginDataBuilder.setResourcePropertyValue(testResourcePropertyId.getTestResourceId(),
						testResourcePropertyId, testResourcePropertyId.getRandomPropertyValue(randomGenerator));
			}
		}

		List<TestRegionId> selectedRegionIds = new ArrayList<>();

		for (TestRegionId testRegionId : TestRegionId.values()) {
			selectedRegionIds.add(testRegionId);
		}
		Collections.shuffle(selectedRegionIds,random);

		for (TestRegionId testRegionId : selectedRegionIds) {
			Collections.shuffle(selectedTestResourceIds,random);
			for (TestResourceId testResourceId : selectedTestResourceIds) {
				if (randomGenerator.nextBoolean()) {
					long value = randomGenerator.nextInt(1000);
					resourcesPluginDataBuilder.setRegionResourceLevel(testRegionId, testResourceId, value);
				}
			}
		}
		
		
		

		for (PersonId personId: people) {		
			Collections.shuffle(selectedTestResourceIds,random);
			for (TestResourceId testResourceId : selectedTestResourceIds) {
				if (randomGenerator.nextBoolean()) {
					long value = randomGenerator.nextInt(5);
					resourcesPluginDataBuilder.setPersonResourceLevel(personId, testResourceId, value);
				}
				if (timeTrackedResourceIds.contains(testResourceId) && randomGenerator.nextBoolean()) {
					double time = randomGenerator.nextDouble() + 1.0;
					resourcesPluginDataBuilder.setPersonResourceTime(personId, testResourceId, time);
				}
			}
		}
		ResourcesPluginData resourcesPluginData = resourcesPluginDataBuilder.build();
		Plugin resourcesPlugin = ResourcesPlugin.builder().setResourcesPluginData(resourcesPluginData)
				.getResourcesPlugin();
		
		//build the regions plugin
		
		RegionsPluginData.Builder regionsPluginDataBuilder  = RegionsPluginData.builder();
		for(TestRegionId testRegionId : TestRegionId.values()) {
			regionsPluginDataBuilder.addRegion(testRegionId);
		}
		
		for(PersonId personId : people) {
			regionsPluginDataBuilder.addPerson(personId, TestRegionId.getRandomRegionId(randomGenerator));
		}
		
		RegionsPluginData regionsPluginData = regionsPluginDataBuilder.build();
		Plugin regionsPlugin = RegionsPlugin.builder().setRegionsPluginData(regionsPluginData).getRegionsPlugin();
		
		//generate the stochastics plugin
		StochasticsPluginData stochasticsPluginData = StochasticsPluginData.builder().setMainRNGState(WellState.builder().setSeed(randomGenerator.nextLong()).build()).build();
		Plugin stochasticsPlugin = StochasticsPlugin.getStochasticsPlugin(stochasticsPluginData);
	
		//generate the test plugin
		TestPluginData.Builder testPluginDataBuilder = TestPluginData.builder();
		testPluginDataBuilder.addTestActorPlan("actor", new TestActorPlan(3,(c)->{
			ResourcesDataManager resourcesDataManager = c.getDataManager(ResourcesDataManager.class);
			String actualValue = resourcesDataManager.toString();
			//Expected value vaidated by inspection
			String expectedValue = "ResourcesDataManager ["
					+ "resourceDefaultTimes={"
					+ "RESOURCE_4=0.217564152372538, "
					+ "RESOURCE_3=0.8830727840178094, "
					+ "RESOURCE_1=0.33699001981033283, "
					+ "RESOURCE_2=0.6084446874416862, "
					+ "RESOURCE_5=0.2584192617810743}, "
					
					+ "resourcePropertyValues={"
					+ "RESOURCE_2={ResourceProperty_2_2_INTEGER_MUTABLE=-1872626629, ResourceProperty_2_1_BOOLEAN_MUTABLE=true}, "
					+ "RESOURCE_3={ResourceProperty_3_1_BOOLEAN_MUTABLE=true}, "
					+ "RESOURCE_5={ResourceProperty_5_1_INTEGER_IMMUTABLE=1952440069}}, "
					
					+ "personResourceLevels={"
					+ "RESOURCE_1=IntValueContainer [subTypeArray=ByteArray [values=[1=0, 3=0, 5=2, 7=0, 9=0, 11=2, 13=4, 15=0, 17=1, 19=0], defaultValue=0]], "
					+ "RESOURCE_3=IntValueContainer [subTypeArray=ByteArray [values=[1=1, 3=0, 5=2, 7=4, 9=0, 11=0, 13=4, 15=0, 17=0, 19=0], defaultValue=0]], "
					+ "RESOURCE_2=IntValueContainer [subTypeArray=ByteArray [values=[1=0, 3=0, 5=3, 7=3, 9=0, 11=0, 13=0, 15=0, 17=0, 19=0], defaultValue=0]], "
					+ "RESOURCE_5=IntValueContainer [subTypeArray=ByteArray [values=[1=0, 3=0, 5=0, 7=2, 9=0, 11=0, 13=3, 15=0, 17=0, 19=0], defaultValue=0]], "
					+ "RESOURCE_4=IntValueContainer [subTypeArray=ByteArray [values=[1=0, 3=0, 5=0, 7=0, 9=0, 11=0, 13=2, 15=0, 17=0, 19=1], defaultValue=0]]}, "
					
					+ "resourcePropertyDefinitions={"
					+ "RESOURCE_2={ResourceProperty_2_2_INTEGER_MUTABLE=PropertyDefinition [type=class java.lang.Integer, propertyValuesAreMutable=true, defaultValue=5], ResourceProperty_2_1_BOOLEAN_MUTABLE=PropertyDefinition [type=class java.lang.Boolean, propertyValuesAreMutable=true, defaultValue=true]}, "
					+ "RESOURCE_1={ResourceProperty_1_3_DOUBLE_MUTABLE=PropertyDefinition [type=class java.lang.Double, propertyValuesAreMutable=true, defaultValue=0.0], ResourceProperty_1_1_BOOLEAN_MUTABLE=PropertyDefinition [type=class java.lang.Boolean, propertyValuesAreMutable=true, defaultValue=false], ResourceProperty_1_2_INTEGER_MUTABLE=PropertyDefinition [type=class java.lang.Integer, propertyValuesAreMutable=true, defaultValue=0]}, "
					+ "RESOURCE_3={ResourceProperty_3_1_BOOLEAN_MUTABLE=PropertyDefinition [type=class java.lang.Boolean, propertyValuesAreMutable=true, defaultValue=false], ResourceProperty_3_2_STRING_MUTABLE=PropertyDefinition [type=class java.lang.String, propertyValuesAreMutable=true, defaultValue=]}, "
					+ "RESOURCE_5={ResourceProperty_5_1_INTEGER_IMMUTABLE=PropertyDefinition [type=class java.lang.Integer, propertyValuesAreMutable=false, defaultValue=7], ResourceProperty_5_2_DOUBLE_IMMUTABLE=PropertyDefinition [type=class java.lang.Double, propertyValuesAreMutable=false, defaultValue=2.7]}, "
					+ "RESOURCE_4={ResourceProperty_4_1_BOOLEAN_MUTABLE=PropertyDefinition [type=class java.lang.Boolean, propertyValuesAreMutable=true, defaultValue=true]}}, "
					
					+ "resourceTimeTrackingPolicies={"
					+ "RESOURCE_4=false, "
					+ "RESOURCE_3=true, "
					+ "RESOURCE_1=true, "
					+ "RESOURCE_2=true, "
					+ "RESOURCE_5=true}, "
					
					+ "personResourceTimes={"
					+ "RESOURCE_1=DoubleValueContainer [values=[1=1.4101040879057936, 3=0.33699001981033283, 5=0.33699001981033283, 7=1.671063001388121, 9=1.8031941854939413, 11=1.17923883714124, 13=0.33699001981033283, 15=0.33699001981033283, 17=0.33699001981033283, 19=0.33699001981033283], defaultValue=0.33699001981033283], "
					+ "RESOURCE_3=DoubleValueContainer [values=[1=1.1385628625362318, 3=0.8830727840178094, 5=1.991978622923995, 7=0.8830727840178094, 9=1.3511645237538967, 11=0.8830727840178094, 13=1.4140228511404762, 15=1.1216048964167127, 17=0.8830727840178094, 19=0.8830727840178094], defaultValue=0.8830727840178094], "
					+ "RESOURCE_5=DoubleValueContainer [values=[1=0.2584192617810743, 3=1.200541444517943, 5=0.2584192617810743, 7=0.2584192617810743, 9=1.543391403912596, 11=0.2584192617810743, 13=0.2584192617810743, 15=1.9944181030137489, 17=0.2584192617810743, 19=1.542836102920688], defaultValue=0.2584192617810743], "
					+ "RESOURCE_2=DoubleValueContainer [values=[1=0.6084446874416862, 3=0.6084446874416862, 5=1.413709876061411, 7=1.6431364543995068, 9=1.4748673882603254, 11=0.6084446874416862, 13=1.9264298147324626, 15=1.3054658380301465, 17=0.6084446874416862, 19=0.6084446874416862], defaultValue=0.6084446874416862]}, "
					
					+ "regionResources={"
					+ "REGION_3={RESOURCE_4=MutableLong [value=973], RESOURCE_1=MutableLong [value=10], RESOURCE_3=MutableLong [value=216], RESOURCE_2=MutableLong [value=267]}, "
					+ "REGION_4={RESOURCE_3=MutableLong [value=22], RESOURCE_5=MutableLong [value=720], RESOURCE_1=MutableLong [value=705], RESOURCE_2=MutableLong [value=877]}, "
					+ "REGION_2={RESOURCE_1=MutableLong [value=121], RESOURCE_4=MutableLong [value=216], RESOURCE_2=MutableLong [value=244], RESOURCE_3=MutableLong [value=502]}, "
					+ "REGION_1={RESOURCE_4=MutableLong [value=0], RESOURCE_5=MutableLong [value=719]}, "
					+ "REGION_5={RESOURCE_4=MutableLong [value=954], RESOURCE_2=MutableLong [value=598], RESOURCE_5=MutableLong [value=347], RESOURCE_1=MutableLong [value=865]}, "
					+ "REGION_6={RESOURCE_2=MutableLong [value=868], RESOURCE_3=MutableLong [value=831], RESOURCE_5=MutableLong [value=643]}}]";
			
			assertEquals(expectedValue, actualValue);
			
		}));
		TestPluginData testPluginData = testPluginDataBuilder.build();
		Plugin testPlugin = TestPlugin.getTestPlugin(testPluginData);
		
		
		SimulationState simulationState = SimulationState.builder().setStartTime(2).build();

		TestSimulation.builder()//
				.addPlugin(resourcesPlugin)//
				.addPlugin(peoplePlugin)//
				.addPlugin(regionsPlugin)//
				.addPlugin(stochasticsPlugin)//
				.addPlugin(testPlugin)//
				.setSimulationState(simulationState)//
				.build()//
				.execute();

	}
}
