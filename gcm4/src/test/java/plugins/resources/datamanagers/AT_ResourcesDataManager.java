package plugins.resources.datamanagers;

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

import org.apache.commons.math3.random.RandomGenerator;
import org.apache.commons.math3.util.Pair;
import org.junit.jupiter.api.Test;

import nucleus.DataManagerContext;
import nucleus.EventFilter;
import nucleus.Plugin;
import nucleus.Simulation;
import nucleus.Simulation.Builder;
import nucleus.testsupport.testplugin.ScenarioPlanCompletionObserver;
import nucleus.testsupport.testplugin.TestActorPlan;
import nucleus.testsupport.testplugin.TestError;
import nucleus.testsupport.testplugin.TestPlugin;
import nucleus.testsupport.testplugin.TestPluginData;
import plugins.people.PeoplePlugin;
import plugins.people.PeoplePluginData;
import plugins.people.datamanagers.PeopleDataManager;
import plugins.people.support.PersonConstructionData;
import plugins.people.support.PersonError;
import plugins.people.support.PersonId;
import plugins.regions.RegionsPlugin;
import plugins.regions.RegionsPluginData;
import plugins.regions.datamanagers.RegionsDataManager;
import plugins.regions.support.RegionConstructionData;
import plugins.regions.support.RegionError;
import plugins.regions.support.RegionId;
import plugins.regions.testsupport.TestRegionId;
import plugins.resources.ResourcesPlugin;
import plugins.resources.ResourcesPluginData;
import plugins.resources.events.PersonResourceUpdateEvent;
import plugins.resources.events.RegionResourceUpdateEvent;
import plugins.resources.events.ResourceIdAdditionEvent;
import plugins.resources.events.ResourcePropertyDefinitionEvent;
import plugins.resources.events.ResourcePropertyUpdateEvent;
import plugins.resources.support.ResourceError;
import plugins.resources.support.ResourceId;
import plugins.resources.support.ResourceInitialization;
import plugins.resources.support.ResourcePropertyId;
import plugins.resources.support.ResourcePropertyInitialization;
import plugins.resources.testsupport.ResourcesActionSupport;
import plugins.resources.testsupport.TestResourceId;
import plugins.resources.testsupport.TestResourcePropertyId;
import plugins.stochastics.StochasticsDataManager;
import plugins.stochastics.StochasticsPlugin;
import plugins.stochastics.StochasticsPluginData;
import plugins.util.properties.PropertyDefinition;
import plugins.util.properties.PropertyError;
import plugins.util.properties.TimeTrackingPolicy;
import tools.annotations.UnitTestConstructor;
import tools.annotations.UnitTestMethod;
import util.errors.ContractException;
import util.random.RandomGeneratorProvider;
import util.wrappers.MultiKey;
import util.wrappers.MutableDouble;
import util.wrappers.MutableInteger;
import util.wrappers.MutableObject;

public final class AT_ResourcesDataManager {
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
			PersonConstructionData personConstructionData = PersonConstructionData.builder().add(TestRegionId.REGION_1).build();
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
		// person removal, but due to ordering will execute after the person is
		// fully eliminated
		pluginBuilder.addTestActorPlan("actor", new TestActorPlan(0, (c) -> {
			PersonId personId = mutablePersonId.getValue();
			PeopleDataManager peopleDataManager = c.getDataManager(PeopleDataManager.class);
			ResourcesDataManager resourcesDataManager = c.getDataManager(ResourcesDataManager.class);

			assertFalse(peopleDataManager.personExists(personId));

			for (TestResourceId testResourceId : TestResourceId.values()) {
				assertThrows(ContractException.class, () -> resourcesDataManager.getPersonResourceLevel(testResourceId, personId));
			}

		}));

		TestPluginData testPluginData = pluginBuilder.build();
		Plugin testPlugin = TestPlugin.getTestPlugin(testPluginData);
		ResourcesActionSupport.testConsumers(0, 5231820238498733928L, testPlugin);

	}

	@Test
	@UnitTestMethod(target = ResourcesDataManager.class, name = "getPeopleWithoutResource", args = { ResourceId.class })
	public void testGetPeopleWithoutResource() {

		ResourcesActionSupport.testConsumer(100, 3641510187112920884L, (c) -> {
			ResourcesDataManager resourcesDataManager = c.getDataManager(ResourcesDataManager.class);
			RegionsDataManager regionsDataManager = c.getDataManager(RegionsDataManager.class);
			StochasticsDataManager stochasticsDataManager = c.getDataManager(StochasticsDataManager.class);
			RandomGenerator randomGenerator = stochasticsDataManager.getRandomGenerator();
			PeopleDataManager peopleDataManager = c.getDataManager(PeopleDataManager.class);
			Set<PersonId> expectedPeople = new LinkedHashSet<>();
			// give about half of the people the resource
			for (PersonId personId : peopleDataManager.getPeople()) {
				if (randomGenerator.nextBoolean()) {
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

		/* precondition test: if the resource id is null */
		ResourcesActionSupport.testConsumer(100, 3473450607674582992L, (c) -> {
			ResourcesDataManager resourcesDataManager = c.getDataManager(ResourcesDataManager.class);
			ContractException contractException = assertThrows(ContractException.class, () -> resourcesDataManager.getPeopleWithoutResource(null));
			assertEquals(ResourceError.NULL_RESOURCE_ID, contractException.getErrorType());
		});

		/* precondition test: if the resource id is unknown */
		ResourcesActionSupport.testConsumer(100, 1143781261828756924L, (c) -> {
			ResourcesDataManager resourcesDataManager = c.getDataManager(ResourcesDataManager.class);
			ContractException contractException = assertThrows(ContractException.class, () -> resourcesDataManager.getPeopleWithoutResource(TestResourceId.getUnknownResourceId()));
			assertEquals(ResourceError.UNKNOWN_RESOURCE_ID, contractException.getErrorType());
		});

	}

	@Test
	@UnitTestConstructor(target = ResourcesDataManager.class, args = { ResourcesPluginData.class })
	public void testConstructor() {
		ContractException contractException = assertThrows(ContractException.class, () -> new ResourcesDataManager(null));
		assertEquals(ResourceError.NULL_RESOURCE_PLUGIN_DATA, contractException.getErrorType());
	}

	@Test
	@UnitTestMethod(target = ResourcesDataManager.class, name = "expandCapacity", args = { int.class })
	public void testExpandCapacity() {
		ResourcesActionSupport.testConsumer(100, 9107703044214388523L, (c) -> {
			ResourcesDataManager resourcesDataManager = c.getDataManager(ResourcesDataManager.class);
			ContractException contractException = assertThrows(ContractException.class, () -> resourcesDataManager.expandCapacity(-1));
			assertEquals(PersonError.NEGATIVE_GROWTH_PROJECTION, contractException.getErrorType());
		});
	}

	@Test
	@UnitTestMethod(target = ResourcesDataManager.class, name = "getPeopleWithResource", args = { ResourceId.class })
	public void testGetPeopleWithResource() {

		ResourcesActionSupport.testConsumer(100, 1030108367649001208L, (c) -> {
			ResourcesDataManager resourcesDataManager = c.getDataManager(ResourcesDataManager.class);
			RegionsDataManager regionsDataManager = c.getDataManager(RegionsDataManager.class);
			StochasticsDataManager stochasticsDataManager = c.getDataManager(StochasticsDataManager.class);
			RandomGenerator randomGenerator = stochasticsDataManager.getRandomGenerator();
			PeopleDataManager peopleDataManager = c.getDataManager(PeopleDataManager.class);
			Set<PersonId> expectedPeople = new LinkedHashSet<>();
			// give about half of the people the resource
			for (PersonId personId : peopleDataManager.getPeople()) {
				if (randomGenerator.nextBoolean()) {
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

		/* precondition test: if the resource id is null */
		ResourcesActionSupport.testConsumer(100, 319392144027980087L, (c) -> {
			ResourcesDataManager resourcesDataManager = c.getDataManager(ResourcesDataManager.class);
			ContractException contractException = assertThrows(ContractException.class, () -> resourcesDataManager.getPeopleWithoutResource(null));
			assertEquals(ResourceError.NULL_RESOURCE_ID, contractException.getErrorType());
		});

		/* precondition test: if the resource id is unknown */
		ResourcesActionSupport.testConsumer(100, 8576038889544967878L, (c) -> {
			ResourcesDataManager resourcesDataManager = c.getDataManager(ResourcesDataManager.class);
			ContractException contractException = assertThrows(ContractException.class, () -> resourcesDataManager.getPeopleWithoutResource(TestResourceId.getUnknownResourceId()));
			assertEquals(ResourceError.UNKNOWN_RESOURCE_ID, contractException.getErrorType());
		});
	}

	@Test
	@UnitTestMethod(target = ResourcesDataManager.class, name = "getPersonResourceLevel", args = { ResourceId.class, PersonId.class })
	public void testGetPersonResourceLevel() {

		ResourcesActionSupport.testConsumer(20, 110987310555566746L, (c) -> {
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

		/* precondition test: if the resource id is null */
		ResourcesActionSupport.testConsumer(20, 5173387308794126450L, (c) -> {
			ResourcesDataManager resourcesDataManager = c.getDataManager(ResourcesDataManager.class);
			ContractException contractException = assertThrows(ContractException.class, () -> resourcesDataManager.getPersonResourceLevel(null, new PersonId(0)));
			assertEquals(ResourceError.NULL_RESOURCE_ID, contractException.getErrorType());
		});
		/* precondition test: if the resource id is unknown */
		ResourcesActionSupport.testConsumer(20, 5756572221517144312L, (c) -> {
			ResourcesDataManager resourcesDataManager = c.getDataManager(ResourcesDataManager.class);
			ContractException contractException = assertThrows(ContractException.class, () -> resourcesDataManager.getPersonResourceLevel(TestResourceId.getUnknownResourceId(), new PersonId(0)));
			assertEquals(ResourceError.UNKNOWN_RESOURCE_ID, contractException.getErrorType());

		});

		/* precondition test: if the person id null */
		ResourcesActionSupport.testConsumer(20, 1392115005391991861L, (c) -> {
			ResourcesDataManager resourcesDataManager = c.getDataManager(ResourcesDataManager.class);
			ContractException contractException = assertThrows(ContractException.class, () -> resourcesDataManager.getPersonResourceLevel(TestResourceId.RESOURCE_1, null));
			assertEquals(PersonError.NULL_PERSON_ID, contractException.getErrorType());
		});
	}

	@Test
	@UnitTestMethod(target = ResourcesDataManager.class, name = "getPersonResourceTime", args = { ResourceId.class, PersonId.class })
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
				TimeTrackingPolicy personResourceTimeTrackingPolicy = resourcesDataManager.getPersonResourceTimeTrackingPolicy(resourceId);
				if (personResourceTimeTrackingPolicy == TimeTrackingPolicy.TRACK_TIME) {
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
				TimeTrackingPolicy personResourceTimeTrackingPolicy = resourcesDataManager.getPersonResourceTimeTrackingPolicy(resourceId);
				if (personResourceTimeTrackingPolicy == TimeTrackingPolicy.TRACK_TIME) {
					double expectedTime = expectedTimes.get(multiKey).getValue();
					double actualTime = resourcesDataManager.getPersonResourceTime(resourceId, personId);
					assertEquals(expectedTime, actualTime);
					actualAssertionsCount++;
				}
			}
			/*
			 * Show that the number of time values that were tested is equal to
			 * the size of the population times the number of time-tracked
			 * resources
			 */

			int trackedResourceCount = 0;
			for (ResourceId resourceId : resourcesDataManager.getResourceIds()) {
				TimeTrackingPolicy personResourceTimeTrackingPolicy = resourcesDataManager.getPersonResourceTimeTrackingPolicy(resourceId);
				if (personResourceTimeTrackingPolicy == TimeTrackingPolicy.TRACK_TIME) {
					trackedResourceCount++;
				}
			}

			int expectedAssertionsCount = peopleDataManager.getPopulationCount() * trackedResourceCount;
			assertEquals(expectedAssertionsCount, actualAssertionsCount);
		}));

		TestPluginData testPluginData = pluginBuilder.build();
		Plugin testPlugin = TestPlugin.getTestPlugin(testPluginData);
		ResourcesActionSupport.testConsumers(30, 3274189520478045515L, testPlugin);

		/*
		 * precondition test: if the assignment times for the resource are not
		 * tracked
		 */
		ResourcesActionSupport.testConsumer(30, 4631279382559646912L, (c) -> {
			ResourcesDataManager resourcesDataManager = c.getDataManager(ResourcesDataManager.class);
			ContractException contractException = assertThrows(ContractException.class, () -> resourcesDataManager.getPersonResourceTime(TestResourceId.RESOURCE_2, new PersonId(0)));
			assertEquals(ResourceError.RESOURCE_ASSIGNMENT_TIME_NOT_TRACKED, contractException.getErrorType());
		});

		/* precondition test: if the resource id is null */
		ResourcesActionSupport.testConsumer(30, 2409228447197751995L, (c) -> {
			ResourcesDataManager resourcesDataManager = c.getDataManager(ResourcesDataManager.class);
			ContractException contractException = assertThrows(ContractException.class, () -> resourcesDataManager.getPersonResourceTime(null, new PersonId(0)));
			assertEquals(ResourceError.NULL_RESOURCE_ID, contractException.getErrorType());
		});

		/* precondition test: if the resource id is unknown */
		ResourcesActionSupport.testConsumer(30, 6640524810334992305L, (c) -> {
			ResourcesDataManager resourcesDataManager = c.getDataManager(ResourcesDataManager.class);
			ContractException contractException = assertThrows(ContractException.class, () -> resourcesDataManager.getPersonResourceTime(TestResourceId.getUnknownResourceId(), new PersonId(0)));
			assertEquals(ResourceError.UNKNOWN_RESOURCE_ID, contractException.getErrorType());
		});

		/* precondition test: if the person id null */
		ResourcesActionSupport.testConsumer(30, 6775179388362303664L, (c) -> {
			ResourcesDataManager resourcesDataManager = c.getDataManager(ResourcesDataManager.class);
			ContractException contractException = assertThrows(ContractException.class, () -> resourcesDataManager.getPersonResourceTime(TestResourceId.RESOURCE_1, null));
			assertEquals(PersonError.NULL_PERSON_ID, contractException.getErrorType());
		});

	}

	@Test
	@UnitTestMethod(target = ResourcesDataManager.class, name = "getPersonResourceTimeTrackingPolicy", args = { ResourceId.class })
	public void testGetPersonResourceTimeTrackingPolicy() {

		ResourcesActionSupport.testConsumer(5, 757175164544632409L, (c) -> {
			ResourcesDataManager resourcesDataManager = c.getDataManager(ResourcesDataManager.class);
			for (TestResourceId testResourceId : TestResourceId.values()) {
				TimeTrackingPolicy actualPolicy = resourcesDataManager.getPersonResourceTimeTrackingPolicy(testResourceId);
				TimeTrackingPolicy expectedPolicy = testResourceId.getTimeTrackingPolicy();
				assertEquals(expectedPolicy, actualPolicy);
			}
		});

		/* precondition test: if the resource id is null */
		ResourcesActionSupport.testConsumer(5, 1761534115327431429L, (c) -> {
			ResourcesDataManager resourcesDataManager = c.getDataManager(ResourcesDataManager.class);
			ContractException contractException = assertThrows(ContractException.class, () -> resourcesDataManager.getPersonResourceTimeTrackingPolicy(null));
			assertEquals(ResourceError.NULL_RESOURCE_ID, contractException.getErrorType());
		});

		/* precondition test: if the resource id is unknown */
		ResourcesActionSupport.testConsumer(5, 7202590650313787556L, (c) -> {
			ResourcesDataManager resourcesDataManager = c.getDataManager(ResourcesDataManager.class);
			ContractException contractException = assertThrows(ContractException.class, () -> resourcesDataManager.getPersonResourceTimeTrackingPolicy(TestResourceId.getUnknownResourceId()));
			assertEquals(ResourceError.UNKNOWN_RESOURCE_ID, contractException.getErrorType());
		});

	}

	@Test
	@UnitTestMethod(target = ResourcesDataManager.class, name = "getRegionResourceLevel", args = { RegionId.class, ResourceId.class })
	public void testGetRegionResourceLevel() {

		ResourcesActionSupport.testConsumer(20, 6606932435911201728L, (c) -> {
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

		/* precondition test: if the resource id is null */
		ResourcesActionSupport.testConsumer(20, 1436454351032688103L, (c) -> {
			ResourcesDataManager resourcesDataManager = c.getDataManager(ResourcesDataManager.class);
			ContractException contractException = assertThrows(ContractException.class, () -> resourcesDataManager.getRegionResourceLevel(TestRegionId.REGION_1, null));
			assertEquals(ResourceError.NULL_RESOURCE_ID, contractException.getErrorType());
		});

		/* precondition test: if the resource id is unknown */
		ResourcesActionSupport.testConsumer(20, 7954290176104108412L, (c) -> {
			ResourcesDataManager resourcesDataManager = c.getDataManager(ResourcesDataManager.class);
			ContractException contractException = assertThrows(ContractException.class,
					() -> resourcesDataManager.getRegionResourceLevel(TestRegionId.REGION_1, TestResourceId.getUnknownResourceId()));
			assertEquals(ResourceError.UNKNOWN_RESOURCE_ID, contractException.getErrorType());
		});

		/* precondition test: if the region id null */
		ResourcesActionSupport.testConsumer(20, 936653403265146113L, (c) -> {
			ResourcesDataManager resourcesDataManager = c.getDataManager(ResourcesDataManager.class);
			ContractException contractException = assertThrows(ContractException.class, () -> resourcesDataManager.getRegionResourceLevel(null, TestResourceId.RESOURCE_1));
			assertEquals(RegionError.NULL_REGION_ID, contractException.getErrorType());
		});

		/* precondition test: if the region id is unknown */
		ResourcesActionSupport.testConsumer(20, 8256630838791330328L, (c) -> {
			ResourcesDataManager resourcesDataManager = c.getDataManager(ResourcesDataManager.class);
			ContractException contractException = assertThrows(ContractException.class,
					() -> resourcesDataManager.getRegionResourceLevel(TestRegionId.getUnknownRegionId(), TestResourceId.RESOURCE_1));
			assertEquals(RegionError.UNKNOWN_REGION_ID, contractException.getErrorType());
		});
	}

	@Test
	@UnitTestMethod(target = ResourcesDataManager.class, name = "getRegionResourceTime", args = { RegionId.class, ResourceId.class })
	public void testGetRegionResourceTime() {

		TestPluginData.Builder pluginBuilder = TestPluginData.builder();

		Map<MultiKey, MutableDouble> expectedTimes = new LinkedHashMap<>();

		pluginBuilder.addTestActorPlan("actor", new TestActorPlan(0, (c) -> {
			// establish data views

			ResourcesDataManager resourcesDataManager = c.getDataManager(ResourcesDataManager.class);
			StochasticsDataManager stochasticsDataManager = c.getDataManager(StochasticsDataManager.class);
			RandomGenerator randomGenerator = stochasticsDataManager.getRandomGenerator();
			RegionsDataManager regionsDataManager = c.getDataManager(RegionsDataManager.class);

			// establish the people and resources
			Set<ResourceId> resourceIds = resourcesDataManager.getResourceIds();
			List<RegionId> regionIds = new ArrayList<>(regionsDataManager.getRegionIds());

			// initialize the expected times
			for (RegionId regionId : regionIds) {
				for (ResourceId resourceId : resourceIds) {
					expectedTimes.put(new MultiKey(regionId, resourceId), new MutableDouble());
				}
			}

			// give random amounts of resource to random regions
			for (int i = 0; i < regionIds.size(); i++) {
				RegionId regionId = regionIds.get(randomGenerator.nextInt(regionIds.size()));
				ResourceId resourceId = TestResourceId.getRandomResourceId(randomGenerator);
				int amount = randomGenerator.nextInt(5) + 1;
				resourcesDataManager.addResourceToRegion(resourceId, regionId, amount);
				expectedTimes.get(new MultiKey(regionId, resourceId)).setValue(c.getTime());
			}

		}));

		// make more resource updates at time 1
		pluginBuilder.addTestActorPlan("actor", new TestActorPlan(1, (c) -> {
			// establish data views

			StochasticsDataManager stochasticsDataManager = c.getDataManager(StochasticsDataManager.class);
			RandomGenerator randomGenerator = stochasticsDataManager.getRandomGenerator();
			RegionsDataManager regionsDataManager = c.getDataManager(RegionsDataManager.class);
			ResourcesDataManager resourcesDataManager = c.getDataManager(ResourcesDataManager.class);

			// establish the regions
			List<RegionId> regionIds = new ArrayList<>(regionsDataManager.getRegionIds());

			// give random amounts of resource to random regions
			for (int i = 0; i < regionIds.size(); i++) {
				RegionId regionId = regionIds.get(randomGenerator.nextInt(regionIds.size()));
				ResourceId resourceId = TestResourceId.getRandomResourceId(randomGenerator);
				int amount = randomGenerator.nextInt(5) + 1;
				resourcesDataManager.addResourceToRegion(resourceId, regionId, amount);
				expectedTimes.get(new MultiKey(regionId, resourceId)).setValue(c.getTime());
			}

		}));

		// make more resource updates at time 2
		pluginBuilder.addTestActorPlan("actor", new TestActorPlan(2, (c) -> {
			// establish data views

			StochasticsDataManager stochasticsDataManager = c.getDataManager(StochasticsDataManager.class);
			RandomGenerator randomGenerator = stochasticsDataManager.getRandomGenerator();
			RegionsDataManager regionsDataManager = c.getDataManager(RegionsDataManager.class);
			ResourcesDataManager resourcesDataManager = c.getDataManager(ResourcesDataManager.class);
			// establish the regions
			List<RegionId> regionIds = new ArrayList<>(regionsDataManager.getRegionIds());

			// give random amounts of resource to random regions
			for (int i = 0; i < regionIds.size(); i++) {
				RegionId regionId = regionIds.get(randomGenerator.nextInt(regionIds.size()));
				ResourceId resourceId = TestResourceId.getRandomResourceId(randomGenerator);
				int amount = randomGenerator.nextInt(5) + 1;
				resourcesDataManager.addResourceToRegion(resourceId, regionId, amount);
				expectedTimes.get(new MultiKey(regionId, resourceId)).setValue(c.getTime());
			}

		}));

		// test the person resource times
		pluginBuilder.addTestActorPlan("actor", new TestActorPlan(3, (c) -> {
			// establish data views

			ResourcesDataManager resourcesDataManager = c.getDataManager(ResourcesDataManager.class);
			RegionsDataManager regionsDataManager = c.getDataManager(RegionsDataManager.class);

			// show that the region resource times match expectations
			int actualAssertionsCount = 0;
			for (MultiKey multiKey : expectedTimes.keySet()) {
				RegionId regionId = multiKey.getKey(0);
				ResourceId resourceId = multiKey.getKey(1);
				double expectedTime = expectedTimes.get(multiKey).getValue();
				double actualTime = resourcesDataManager.getRegionResourceTime(regionId, resourceId);
				assertEquals(expectedTime, actualTime);
				actualAssertionsCount++;
			}
			/*
			 * Show that the number of time values that were tested is equal to
			 * the size of the population times the number of resources
			 */
			int expectedAssertionsCount = regionsDataManager.getRegionIds().size() * resourcesDataManager.getResourceIds().size();
			assertEquals(expectedAssertionsCount, actualAssertionsCount);
		}));

		TestPluginData testPluginData = pluginBuilder.build();
		Plugin testPlugin = TestPlugin.getTestPlugin(testPluginData);
		ResourcesActionSupport.testConsumers(30, 6128764970683025350L, testPlugin);

		/*
		 * precondition test: if the assignment times for the resource are not
		 * tracked
		 */
		ResourcesActionSupport.testConsumer(30, 3888561557931148149L, (c) -> {
			ResourcesDataManager resourcesDataManager = c.getDataManager(ResourcesDataManager.class);
			ContractException contractException = assertThrows(ContractException.class, () -> resourcesDataManager.getPersonResourceTime(TestResourceId.RESOURCE_2, new PersonId(0)));
			assertEquals(ResourceError.RESOURCE_ASSIGNMENT_TIME_NOT_TRACKED, contractException.getErrorType());
		});

		/* precondition test: if the resource id is null */
		ResourcesActionSupport.testConsumer(30, 9045818580061726595L, (c) -> {
			ResourcesDataManager resourcesDataManager = c.getDataManager(ResourcesDataManager.class);
			ContractException contractException = assertThrows(ContractException.class, () -> resourcesDataManager.getPersonResourceTime(null, new PersonId(0)));
			assertEquals(ResourceError.NULL_RESOURCE_ID, contractException.getErrorType());
		});

		/* precondition test: if the resource id is unknown */
		ResourcesActionSupport.testConsumer(30, 5592254382530100326L, (c) -> {
			ResourcesDataManager resourcesDataManager = c.getDataManager(ResourcesDataManager.class);
			ContractException contractException = assertThrows(ContractException.class, () -> resourcesDataManager.getPersonResourceTime(TestResourceId.getUnknownResourceId(), new PersonId(0)));
			assertEquals(ResourceError.UNKNOWN_RESOURCE_ID, contractException.getErrorType());
		});

		/* precondition test: if the person id null */
		ResourcesActionSupport.testConsumer(30, 1245016103076447355L, (c) -> {
			ResourcesDataManager resourcesDataManager = c.getDataManager(ResourcesDataManager.class);
			ContractException contractException = assertThrows(ContractException.class, () -> resourcesDataManager.getPersonResourceTime(TestResourceId.RESOURCE_1, null));
			assertEquals(PersonError.NULL_PERSON_ID, contractException.getErrorType());
		});

	}

	@Test
	@UnitTestMethod(target = ResourcesDataManager.class, name = "getResourceIds", args = {})
	public void testGetResourceIds() {

		ResourcesActionSupport.testConsumer(5, 2601236547109660988L, (c) -> {
			ResourcesDataManager resourcesDataManager = c.getDataManager(ResourcesDataManager.class);

			// show that the resource ids are the test resource ids
			Set<ResourceId> expectedResourceIds = new LinkedHashSet<>();
			for (TestResourceId testResourceId : TestResourceId.values()) {
				expectedResourceIds.add(testResourceId);
			}
			assertEquals(expectedResourceIds, resourcesDataManager.getResourceIds());

		});
	}

	@Test
	@UnitTestMethod(target = ResourcesDataManager.class, name = "getResourcePropertyDefinition", args = { ResourceId.class, ResourcePropertyId.class })
	public void testGetResourcePropertyDefinition() {

		ResourcesActionSupport.testConsumer(5, 7619546908709928867L, (c) -> {
			ResourcesDataManager resourcesDataManager = c.getDataManager(ResourcesDataManager.class);
			// show that each of the resource property definitions from the test
			// resource property enum are present
			for (TestResourcePropertyId testResourcePropertyId : TestResourcePropertyId.values()) {
				PropertyDefinition expectedPropertyDefinition = testResourcePropertyId.getPropertyDefinition();
				PropertyDefinition actualPropertyDefinition = resourcesDataManager.getResourcePropertyDefinition(testResourcePropertyId.getTestResourceId(), testResourcePropertyId);
				assertEquals(expectedPropertyDefinition, actualPropertyDefinition);
			}
		});
	}

	@Test
	@UnitTestMethod(target = ResourcesDataManager.class, name = "getResourcePropertyIds", args = { ResourceId.class })
	public void testGetResourcePropertyIds() {

		ResourcesActionSupport.testConsumer(5, 1203402714876510055L, (c) -> {
			ResourcesDataManager resourcesDataManager = c.getDataManager(ResourcesDataManager.class);
			// show that the resource property ids are the test resource
			// property ids
			for (TestResourceId testResourceId : TestResourceId.values()) {
				Set<TestResourcePropertyId> expectedPropertyIds = TestResourcePropertyId.getTestResourcePropertyIds(testResourceId);
				Set<ResourcePropertyId> actualPropertyIds = resourcesDataManager.getResourcePropertyIds(testResourceId);
				assertEquals(expectedPropertyIds, actualPropertyIds);
			}

		});

		/* precondition tests if the resource id is null */
		ResourcesActionSupport.testConsumer(5, 3551512082879672269L, (c) -> {
			ResourcesDataManager resourcesDataManager = c.getDataManager(ResourcesDataManager.class);
			ContractException contractException = assertThrows(ContractException.class, () -> resourcesDataManager.getResourcePropertyIds(null));
			assertEquals(ResourceError.NULL_RESOURCE_ID, contractException.getErrorType());
		});

		/* precondition tests if the resource id unknown */
		ResourcesActionSupport.testConsumer(5, 7372199991315732905L, (c) -> {
			ResourcesDataManager resourcesDataManager = c.getDataManager(ResourcesDataManager.class);
			ContractException contractException = assertThrows(ContractException.class, () -> resourcesDataManager.getResourcePropertyIds(TestResourceId.getUnknownResourceId()));
			assertEquals(ResourceError.UNKNOWN_RESOURCE_ID, contractException.getErrorType());
		});
	}

	@Test
	@UnitTestMethod(target = ResourcesDataManager.class, name = "getResourcePropertyTime", args = { ResourceId.class, ResourcePropertyId.class })
	public void testGetResourcePropertyTime() {

		TestPluginData.Builder pluginBuilder = TestPluginData.builder();

		Map<MultiKey, MutableDouble> expectedTimes = new LinkedHashMap<>();

		pluginBuilder.addTestActorPlan("actor", new TestActorPlan(0, (c) -> {
			// establish data views
			ResourcesDataManager resourcesDataManager = c.getDataManager(ResourcesDataManager.class);

			StochasticsDataManager stochasticsDataManager = c.getDataManager(StochasticsDataManager.class);
			RandomGenerator randomGenerator = stochasticsDataManager.getRandomGenerator();

			// establish the resources
			Set<ResourceId> resourceIds = resourcesDataManager.getResourceIds();

			// initialize the expected times
			for (TestResourceId testResourceId : TestResourceId.values()) {
				for (TestResourcePropertyId testResourcePropertyId : TestResourcePropertyId.getTestResourcePropertyIds(testResourceId)) {
					expectedTimes.put(new MultiKey(testResourceId, testResourcePropertyId), new MutableDouble());
				}
			}

			// set random values to the resource properties
			for (int i = 0; i < resourceIds.size(); i++) {
				TestResourceId testResourceId = TestResourceId.getRandomResourceId(randomGenerator);
				TestResourcePropertyId testResourcePropertyId = TestResourcePropertyId.getRandomResourcePropertyId(testResourceId, randomGenerator);
				PropertyDefinition propertyDefinition = resourcesDataManager.getResourcePropertyDefinition(testResourceId, testResourcePropertyId);
				if (propertyDefinition.propertyValuesAreMutable()) {
					Object value = testResourcePropertyId.getRandomPropertyValue(randomGenerator);
					resourcesDataManager.setResourcePropertyValue(testResourceId, testResourcePropertyId, value);
					expectedTimes.get(new MultiKey(testResourceId, testResourcePropertyId)).setValue(c.getTime());
				}
			}

		}));

		// make more resource property updates at time 1
		pluginBuilder.addTestActorPlan("actor", new TestActorPlan(1, (c) -> {
			// establish data views
			ResourcesDataManager resourcesDataManager = c.getDataManager(ResourcesDataManager.class);
			StochasticsDataManager stochasticsDataManager = c.getDataManager(StochasticsDataManager.class);
			RandomGenerator randomGenerator = stochasticsDataManager.getRandomGenerator();

			// establish the resources
			Set<ResourceId> resourceIds = resourcesDataManager.getResourceIds();

			// set random values to the resource properties
			for (int i = 0; i < resourceIds.size(); i++) {
				TestResourceId testResourceId = TestResourceId.getRandomResourceId(randomGenerator);
				TestResourcePropertyId testResourcePropertyId = TestResourcePropertyId.getRandomResourcePropertyId(testResourceId, randomGenerator);
				PropertyDefinition propertyDefinition = resourcesDataManager.getResourcePropertyDefinition(testResourceId, testResourcePropertyId);
				if (propertyDefinition.propertyValuesAreMutable()) {
					Object value = testResourcePropertyId.getRandomPropertyValue(randomGenerator);
					resourcesDataManager.setResourcePropertyValue(testResourceId, testResourcePropertyId, value);
					expectedTimes.get(new MultiKey(testResourceId, testResourcePropertyId)).setValue(c.getTime());
				}
			}

		}));

		// make more resource property updates at time 2
		pluginBuilder.addTestActorPlan("actor", new TestActorPlan(2, (c) -> {
			// establish data views
			ResourcesDataManager resourcesDataManager = c.getDataManager(ResourcesDataManager.class);
			StochasticsDataManager stochasticsDataManager = c.getDataManager(StochasticsDataManager.class);
			RandomGenerator randomGenerator = stochasticsDataManager.getRandomGenerator();

			// establish the resources
			Set<ResourceId> resourceIds = resourcesDataManager.getResourceIds();

			// set random values to the resource properties
			for (int i = 0; i < resourceIds.size(); i++) {
				TestResourceId testResourceId = TestResourceId.getRandomResourceId(randomGenerator);
				TestResourcePropertyId testResourcePropertyId = TestResourcePropertyId.getRandomResourcePropertyId(testResourceId, randomGenerator);
				PropertyDefinition propertyDefinition = resourcesDataManager.getResourcePropertyDefinition(testResourceId, testResourcePropertyId);
				if (propertyDefinition.propertyValuesAreMutable()) {
					Object value = testResourcePropertyId.getRandomPropertyValue(randomGenerator);
					resourcesDataManager.setResourcePropertyValue(testResourceId, testResourcePropertyId, value);
					expectedTimes.get(new MultiKey(testResourceId, testResourcePropertyId)).setValue(c.getTime());
				}
			}

		}));

		// test the person resource times
		pluginBuilder.addTestActorPlan("actor", new TestActorPlan(3, (c) -> {
			// establish data views

			ResourcesDataManager resourcesDataManager = c.getDataManager(ResourcesDataManager.class);

			// show that the person resource times match expectations
			int actualAssertionsCount = 0;
			for (MultiKey multiKey : expectedTimes.keySet()) {
				ResourceId resourceId = multiKey.getKey(0);
				ResourcePropertyId resourcePropertyId = multiKey.getKey(1);
				double expectedTime = expectedTimes.get(multiKey).getValue();
				double actualTime = resourcesDataManager.getResourcePropertyTime(resourceId, resourcePropertyId);
				assertEquals(expectedTime, actualTime);
				actualAssertionsCount++;
			}
			/*
			 * Show that the number of time values that were tested is equal to
			 * the number of properties
			 */

			int expectedAssertionsCount = TestResourcePropertyId.values().length;
			assertEquals(expectedAssertionsCount, actualAssertionsCount);
		}));

		TestPluginData testPluginData = pluginBuilder.build();
		Plugin testPlugin = TestPlugin.getTestPlugin(testPluginData);
		ResourcesActionSupport.testConsumers(10, 9211924242349528396L, testPlugin);

		/* precondition test: if the resource id is null */
		ResourcesActionSupport.testConsumer(10, 1319950978123668303L, (c) -> {
			ResourcesDataManager resourcesDataManager = c.getDataManager(ResourcesDataManager.class);
			ContractException contractException = assertThrows(ContractException.class,
					() -> resourcesDataManager.getResourcePropertyTime(null, TestResourcePropertyId.ResourceProperty_1_1_BOOLEAN_MUTABLE));
			assertEquals(ResourceError.NULL_RESOURCE_ID, contractException.getErrorType());
		});
		/* precondition test: if the resource id is unknown */
		ResourcesActionSupport.testConsumer(10, 250698207522319222L, (c) -> {
			ResourcesDataManager resourcesDataManager = c.getDataManager(ResourcesDataManager.class);
			ContractException contractException = assertThrows(ContractException.class,
					() -> resourcesDataManager.getResourcePropertyTime(TestResourceId.getUnknownResourceId(), TestResourcePropertyId.ResourceProperty_1_1_BOOLEAN_MUTABLE));
			assertEquals(ResourceError.UNKNOWN_RESOURCE_ID, contractException.getErrorType());
		});
		/* precondition test: if the resource property id is null */
		ResourcesActionSupport.testConsumer(10, 5885550428859775099L, (c) -> {
			ResourcesDataManager resourcesDataManager = c.getDataManager(ResourcesDataManager.class);
			ContractException contractException = assertThrows(ContractException.class, () -> resourcesDataManager.getResourcePropertyTime(TestResourceId.RESOURCE_1, null));
			assertEquals(PropertyError.NULL_PROPERTY_ID, contractException.getErrorType());
		});
		/* precondition test: if the resource property id is unknown */
		ResourcesActionSupport.testConsumer(10, 6053540186403572061L, (c) -> {
			ResourcesDataManager resourcesDataManager = c.getDataManager(ResourcesDataManager.class);
			ContractException contractException = assertThrows(ContractException.class,
					() -> resourcesDataManager.getResourcePropertyTime(TestResourceId.RESOURCE_1, TestResourcePropertyId.ResourceProperty_2_1_BOOLEAN_MUTABLE));
			assertEquals(PropertyError.UNKNOWN_PROPERTY_ID, contractException.getErrorType());
		});

		/* precondition test: if the resource property id is unknown */
		ResourcesActionSupport.testConsumer(10, 6439495795797811534L, (c) -> {
			ResourcesDataManager resourcesDataManager = c.getDataManager(ResourcesDataManager.class);
			ContractException contractException = assertThrows(ContractException.class,
					() -> resourcesDataManager.getResourcePropertyTime(TestResourceId.RESOURCE_1, TestResourcePropertyId.getUnknownResourcePropertyId()));
			assertEquals(PropertyError.UNKNOWN_PROPERTY_ID, contractException.getErrorType());
		});

	}

	@Test
	@UnitTestMethod(target = ResourcesDataManager.class, name = "getResourcePropertyValue", args = { ResourceId.class, ResourcePropertyId.class })
	public void testGetResourcePropertyValue() {

		ResourcesActionSupport.testConsumer(10, 8757871520559824784L, (c) -> {
			ResourcesDataManager resourcesDataManager = c.getDataManager(ResourcesDataManager.class);
			StochasticsDataManager stochasticsDataManager = c.getDataManager(StochasticsDataManager.class);
			RandomGenerator randomGenerator = stochasticsDataManager.getRandomGenerator();

			// establish the expected values of all resource properties
			Map<MultiKey, Object> expectedValues = new LinkedHashMap<>();
			for (TestResourceId testResourceId : TestResourceId.values()) {
				for (TestResourcePropertyId testResourcePropertyId : TestResourcePropertyId.getTestResourcePropertyIds(testResourceId)) {
					Object propertyValue = resourcesDataManager.getResourcePropertyValue(testResourceId, testResourcePropertyId);
					expectedValues.put(new MultiKey(testResourceId, testResourcePropertyId), propertyValue);
				}
			}

			// make a few random resource property updates
			int updateCount = 0;
			for (int i = 0; i < 1000; i++) {
				TestResourceId testResourceId = TestResourceId.getRandomResourceId(randomGenerator);
				TestResourcePropertyId testResourcePropertyId = TestResourcePropertyId.getRandomResourcePropertyId(testResourceId, randomGenerator);
				PropertyDefinition resourcePropertyDefinition = resourcesDataManager.getResourcePropertyDefinition(testResourceId, testResourcePropertyId);
				if (resourcePropertyDefinition.propertyValuesAreMutable()) {
					Object propertyValue = testResourcePropertyId.getRandomPropertyValue(randomGenerator);
					resourcesDataManager.setResourcePropertyValue(testResourceId, testResourcePropertyId, propertyValue);
					expectedValues.put(new MultiKey(testResourceId, testResourcePropertyId), propertyValue);
					updateCount++;
				}
			}

			/*
			 * Show that the number of updates was reasonable - some of the
			 * properties are not mutable so it will be <1000
			 */
			assertTrue(updateCount > 500);

			// show that the values of the resource properties are correct
			for (MultiKey multiKey : expectedValues.keySet()) {
				TestResourceId testResourceId = multiKey.getKey(0);
				TestResourcePropertyId testResourcePropertyId = multiKey.getKey(1);
				Object expectedValue = expectedValues.get(multiKey);
				Object actualValue = resourcesDataManager.getResourcePropertyValue(testResourceId, testResourcePropertyId);
				assertEquals(expectedValue, actualValue);
			}

		});

		/* precondition test: if the resource id is null */
		ResourcesActionSupport.testConsumer(10, 5856579804289926491L, (c) -> {
			ResourcesDataManager resourcesDataManager = c.getDataManager(ResourcesDataManager.class);
			ContractException contractException = assertThrows(ContractException.class,
					() -> resourcesDataManager.getResourcePropertyValue(null, TestResourcePropertyId.ResourceProperty_1_1_BOOLEAN_MUTABLE));
			assertEquals(ResourceError.NULL_RESOURCE_ID, contractException.getErrorType());
		});

		/* precondition test: if the resource id unknown */
		ResourcesActionSupport.testConsumer(10, 1735955680485266104L, (c) -> {
			ResourcesDataManager resourcesDataManager = c.getDataManager(ResourcesDataManager.class);
			ContractException contractException = assertThrows(ContractException.class,
					() -> resourcesDataManager.getResourcePropertyValue(TestResourceId.getUnknownResourceId(), TestResourcePropertyId.ResourceProperty_1_1_BOOLEAN_MUTABLE));
			assertEquals(ResourceError.UNKNOWN_RESOURCE_ID, contractException.getErrorType());
		});

		/* precondition test: if the resource property id is null */
		ResourcesActionSupport.testConsumer(10, 5544999164968796966L, (c) -> {
			ResourcesDataManager resourcesDataManager = c.getDataManager(ResourcesDataManager.class);
			ContractException contractException = assertThrows(ContractException.class, () -> resourcesDataManager.getResourcePropertyValue(TestResourceId.RESOURCE_1, null));
			assertEquals(PropertyError.NULL_PROPERTY_ID, contractException.getErrorType());
		});

		/* precondition test: if the resource property id unknown */
		ResourcesActionSupport.testConsumer(10, 3394498124288646142L, (c) -> {
			ResourcesDataManager resourcesDataManager = c.getDataManager(ResourcesDataManager.class);
			ContractException contractException = assertThrows(ContractException.class,
					() -> resourcesDataManager.getResourcePropertyValue(TestResourceId.RESOURCE_1, TestResourcePropertyId.ResourceProperty_2_1_BOOLEAN_MUTABLE));
			assertEquals(PropertyError.UNKNOWN_PROPERTY_ID, contractException.getErrorType());
		});

		/* precondition test: if the resource property id unknown */
		ResourcesActionSupport.testConsumer(10, 2505584646755789288L, (c) -> {
			ResourcesDataManager resourcesDataManager = c.getDataManager(ResourcesDataManager.class);
			ContractException contractException = assertThrows(ContractException.class,
					() -> resourcesDataManager.getResourcePropertyValue(TestResourceId.RESOURCE_1, TestResourcePropertyId.getUnknownResourcePropertyId()));
			assertEquals(PropertyError.UNKNOWN_PROPERTY_ID, contractException.getErrorType());
		});
	}

	@Test
	@UnitTestMethod(target = ResourcesDataManager.class, name = "resourceIdExists", args = { ResourceId.class })
	public void testResourceIdExists() {

		ResourcesActionSupport.testConsumer(5, 4964974931601945506L, (c) -> {
			ResourcesDataManager resourcesDataManager = c.getDataManager(ResourcesDataManager.class);

			// show that the resource ids that exist are the test resource ids

			for (TestResourceId testResourceId : TestResourceId.values()) {
				assertTrue(resourcesDataManager.resourceIdExists(testResourceId));
			}
			assertFalse(resourcesDataManager.resourceIdExists(TestResourceId.getUnknownResourceId()));
			assertFalse(resourcesDataManager.resourceIdExists(null));

		});
	}

	@Test
	@UnitTestMethod(target = ResourcesDataManager.class, name = "resourcePropertyIdExists", args = { ResourceId.class, ResourcePropertyId.class })
	public void testResourcePropertyIdExists() {

		ResourcesActionSupport.testConsumer(5, 8074706630609416041L, (c) -> {
			ResourcesDataManager resourcesDataManager = c.getDataManager(ResourcesDataManager.class);

			// show that the resource property ids that exist are the test
			// resource property ids

			for (TestResourcePropertyId testResourcePropertyId : TestResourcePropertyId.values()) {
				assertTrue(resourcesDataManager.resourcePropertyIdExists(testResourcePropertyId.getTestResourceId(), testResourcePropertyId));
			}

			assertFalse(resourcesDataManager.resourcePropertyIdExists(TestResourceId.RESOURCE_1, TestResourcePropertyId.ResourceProperty_2_1_BOOLEAN_MUTABLE));
			assertFalse(resourcesDataManager.resourcePropertyIdExists(null, TestResourcePropertyId.ResourceProperty_2_1_BOOLEAN_MUTABLE));
			assertFalse(resourcesDataManager.resourcePropertyIdExists(TestResourceId.RESOURCE_1, null));
			assertFalse(resourcesDataManager.resourcePropertyIdExists(TestResourceId.RESOURCE_1, TestResourcePropertyId.getUnknownResourcePropertyId()));
			assertFalse(resourcesDataManager.resourcePropertyIdExists(TestResourceId.getUnknownResourceId(), TestResourcePropertyId.ResourceProperty_1_2_INTEGER_MUTABLE));
			assertFalse(resourcesDataManager.resourcePropertyIdExists(TestResourceId.getUnknownResourceId(), TestResourcePropertyId.getUnknownResourcePropertyId()));

		});
	}

	@Test
	@UnitTestMethod(target = ResourcesDataManager.class, name = "defineResourceProperty", args = { ResourcePropertyInitialization.class })

	public void testDefineResourceProperty() {

		TestPluginData.Builder pluginBuilder = TestPluginData.builder();

		Set<MultiKey> expectedObservations = new LinkedHashSet<>();
		Set<MultiKey> actualObservations = new LinkedHashSet<>();

		// have an actor observe the ResourcePropertyAdditionEvent events
		pluginBuilder.addTestActorPlan("observer", new TestActorPlan(0, (c) -> {
			c.subscribe(EventFilter.builder(ResourcePropertyDefinitionEvent.class).build(), (c2, e) -> {
				MultiKey multiKey = new MultiKey(c2.getTime(), e.resourceId(), e.resourcePropertyId(), e.resourcePropertyValue());
				actualObservations.add(multiKey);
			});
		}));

		// have an actor define a new resource property
		pluginBuilder.addTestActorPlan("actor", new TestActorPlan(1, (c) -> {
			ResourcesDataManager resourcesDataManager = c.getDataManager(ResourcesDataManager.class);
			ResourcePropertyId newResourcePropertyId = TestResourcePropertyId.getUnknownResourcePropertyId();
			PropertyDefinition propertyDefinition = PropertyDefinition.builder().setType(Double.class).setDefaultValue(34.6).build();
			ResourcePropertyInitialization resourcePropertyInitialization = //
					ResourcePropertyInitialization	.builder()//
													.setPropertyDefinition(propertyDefinition)//
													.setResourceId(TestResourceId.RESOURCE_1)//
													.setResourcePropertyId(newResourcePropertyId)//
													.build();
			resourcesDataManager.defineResourceProperty(resourcePropertyInitialization);
			assertTrue(resourcesDataManager.resourcePropertyIdExists(TestResourceId.RESOURCE_1, newResourcePropertyId));
			PropertyDefinition actualDefinition = resourcesDataManager.getResourcePropertyDefinition(TestResourceId.RESOURCE_1, newResourcePropertyId);
			assertEquals(propertyDefinition, actualDefinition);
			MultiKey multiKey = new MultiKey(c.getTime(), TestResourceId.RESOURCE_1, newResourcePropertyId, propertyDefinition.getDefaultValue().get());
			expectedObservations.add(multiKey);
		}));

		// have an actor define a new resource property
		pluginBuilder.addTestActorPlan("actor", new TestActorPlan(2, (c) -> {
			ResourcesDataManager resourcesDataManager = c.getDataManager(ResourcesDataManager.class);
			ResourcePropertyId newResourcePropertyId = TestResourcePropertyId.getUnknownResourcePropertyId();
			PropertyDefinition propertyDefinition = PropertyDefinition.builder().setType(String.class).setDefaultValue("default").build();

			ResourcePropertyInitialization resourcePropertyInitialization = //
					ResourcePropertyInitialization	.builder()//
													.setPropertyDefinition(propertyDefinition)//
													.setResourceId(TestResourceId.RESOURCE_2)//
													.setResourcePropertyId(newResourcePropertyId)//
													.build();
			resourcesDataManager.defineResourceProperty(resourcePropertyInitialization);

			assertTrue(resourcesDataManager.resourcePropertyIdExists(TestResourceId.RESOURCE_2, newResourcePropertyId));
			PropertyDefinition actualDefinition = resourcesDataManager.getResourcePropertyDefinition(TestResourceId.RESOURCE_2, newResourcePropertyId);
			assertEquals(propertyDefinition, actualDefinition);
			MultiKey multiKey = new MultiKey(c.getTime(), TestResourceId.RESOURCE_2, newResourcePropertyId, propertyDefinition.getDefaultValue().get());
			expectedObservations.add(multiKey);
		}));

		// have the observer verify the observations were correct
		pluginBuilder.addTestActorPlan("observer", new TestActorPlan(3, (c) -> {
			assertTrue(expectedObservations.size() > 0);
			assertEquals(expectedObservations, actualObservations);
		}));

		TestPluginData testPluginData = pluginBuilder.build();
		Plugin testPlugin = TestPlugin.getTestPlugin(testPluginData);
		ResourcesActionSupport.testConsumers(5, 4535415202634885293L, testPlugin);

		/* precondition test: if the resource id is unknown */
		ResourcesActionSupport.testConsumer(5, 6361316703720629700L, (c) -> {
			ResourcesDataManager resourcesDataManager = c.getDataManager(ResourcesDataManager.class);

			ContractException contractException = assertThrows(ContractException.class, () -> {
				PropertyDefinition propertyDefinition = PropertyDefinition.builder().setType(Integer.class).setDefaultValue(1).build();
				ResourcePropertyInitialization resourcePropertyInitialization = //
						ResourcePropertyInitialization	.builder()//
														.setPropertyDefinition(propertyDefinition)//
														.setResourceId(TestResourceId.getUnknownResourceId())//
														.setResourcePropertyId(TestResourcePropertyId.getUnknownResourcePropertyId())//
														.build();
				resourcesDataManager.defineResourceProperty(resourcePropertyInitialization);
			});

			assertEquals(ResourceError.UNKNOWN_RESOURCE_ID, contractException.getErrorType());
		});

		/* precondition test: if the resource property is already defined */
		ResourcesActionSupport.testConsumer(5, 3114198987897928160L, (c) -> {
			ResourcesDataManager resourcesDataManager = c.getDataManager(ResourcesDataManager.class);
			ContractException contractException = assertThrows(ContractException.class, () -> {
				PropertyDefinition propertyDefinition = PropertyDefinition.builder().setType(Integer.class).setDefaultValue(1).build();
				ResourcePropertyInitialization resourcePropertyInitialization = //
						ResourcePropertyInitialization	.builder()//
														.setPropertyDefinition(propertyDefinition)//
														.setResourceId(TestResourceId.RESOURCE_1)//
														.setResourcePropertyId(TestResourcePropertyId.ResourceProperty_1_2_INTEGER_MUTABLE)//
														.build();
				resourcesDataManager.defineResourceProperty(resourcePropertyInitialization);

			});
			assertEquals(PropertyError.DUPLICATE_PROPERTY_DEFINITION, contractException.getErrorType());
		});

	}

	@Test
	@UnitTestMethod(target = ResourcesDataManager.class, name = "addResourceId", args = { ResourceId.class, TimeTrackingPolicy.class })
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
			TimeTrackingPolicy timeTrackingPolicy = TimeTrackingPolicy.DO_NOT_TRACK_TIME;
			assertFalse(resourcesDataManager.resourceIdExists(newResourceId1));
			resourcesDataManager.addResourceId(newResourceId1, timeTrackingPolicy);
			assertTrue(resourcesDataManager.resourceIdExists(newResourceId1));
			MultiKey multiKey = new MultiKey(c.getTime(), newResourceId1, TimeTrackingPolicy.DO_NOT_TRACK_TIME);
			expectedObservations.add(multiKey);
		}));

		pluginBuilder.addTestActorPlan("actor", new TestActorPlan(2, (c) -> {
			ResourcesDataManager resourcesDataManager = c.getDataManager(ResourcesDataManager.class);
			TimeTrackingPolicy timeTrackingPolicy = TimeTrackingPolicy.TRACK_TIME;
			assertFalse(resourcesDataManager.resourceIdExists(newResourceId2));
			resourcesDataManager.addResourceId(newResourceId2, timeTrackingPolicy);
			assertTrue(resourcesDataManager.resourceIdExists(newResourceId2));
			MultiKey multiKey = new MultiKey(c.getTime(), newResourceId2, TimeTrackingPolicy.TRACK_TIME);
			expectedObservations.add(multiKey);
		}));

		pluginBuilder.addTestActorPlan("observer", new TestActorPlan(3, (c) -> {
			assertEquals(expectedObservations, actualObservations);
		}));

		TestPluginData testPluginData = pluginBuilder.build();
		Plugin testPlugin = TestPlugin.getTestPlugin(testPluginData);
		ResourcesActionSupport.testConsumers(5, 3128266603988900429L, testPlugin);

		// precondition test: if the resource id is null
		ResourcesActionSupport.testConsumer(5, 3016555021220987436L, (c) -> {
			ResourcesDataManager resourcesDataManager = c.getDataManager(ResourcesDataManager.class);
			ContractException contractException = assertThrows(ContractException.class, () -> resourcesDataManager.addResourceId(null, TimeTrackingPolicy.DO_NOT_TRACK_TIME));
			assertEquals(ResourceError.NULL_RESOURCE_ID, contractException.getErrorType());
		});

		// precondition test: if the resource type is already present
		ResourcesActionSupport.testConsumer(5, 9097839209339012193L, (c) -> {
			ResourcesDataManager resourcesDataManager = c.getDataManager(ResourcesDataManager.class);
			ContractException contractException = assertThrows(ContractException.class, () -> resourcesDataManager.addResourceId(TestResourceId.RESOURCE_1, TimeTrackingPolicy.DO_NOT_TRACK_TIME));
			assertEquals(ResourceError.DUPLICATE_RESOURCE_ID, contractException.getErrorType());
		});

		// precondition test: if the time tracking policy is null
		ResourcesActionSupport.testConsumer(5, 5786650172226277505L, (c) -> {
			ResourcesDataManager resourcesDataManager = c.getDataManager(ResourcesDataManager.class);
			ContractException contractException = assertThrows(ContractException.class, () -> resourcesDataManager.addResourceId(TestResourceId.getUnknownResourceId(), null));
			assertEquals(PropertyError.NULL_TIME_TRACKING_POLICY, contractException.getErrorType());
		});

	}

	@Test
	@UnitTestMethod(target = ResourcesDataManager.class, name = "setResourcePropertyValue", args = { ResourceId.class, ResourcePropertyId.class, Object.class })
	public void testSetResourcePropertyValue() {
		TestPluginData.Builder pluginBuilder = TestPluginData.builder();

		Set<MultiKey> expectedObservations = new LinkedHashSet<>();
		Set<MultiKey> actualObservations = new LinkedHashSet<>();

		// Have an actor observe the resource property changes

		pluginBuilder.addTestActorPlan("observer", new TestActorPlan(0, (c) -> {
			ResourcesDataManager resourcesDataManager = c.getDataManager(ResourcesDataManager.class);
			for (TestResourceId testResourceId : TestResourceId.values()) {
				Set<TestResourcePropertyId> testResourcePropertyIds = TestResourcePropertyId.getTestResourcePropertyIds(testResourceId);
				for (TestResourcePropertyId testResourcePropertyId : testResourcePropertyIds) {
					EventFilter<ResourcePropertyUpdateEvent> eventFilter = resourcesDataManager.getEventFilterForResourcePropertyUpdateEvent(testResourceId, testResourcePropertyId);
					c.subscribe(eventFilter, (c2, e) -> {
						actualObservations.add(new MultiKey(e.resourceId(), e.resourcePropertyId(), e.previousPropertyValue(), e.currentPropertyValue()));
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
				Set<TestResourcePropertyId> testResourcePropertyIds = TestResourcePropertyId.getTestResourcePropertyIds(testResourceId);
				for (TestResourcePropertyId testResourcePropertyId : testResourcePropertyIds) {
					PropertyDefinition propertyDefinition = resourcesDataManager.getResourcePropertyDefinition(testResourceId, testResourcePropertyId);
					if (propertyDefinition.propertyValuesAreMutable()) {
						// update the property value
						Object resourcePropertyValue = resourcesDataManager.getResourcePropertyValue(testResourceId, testResourcePropertyId);
						Object expectedValue = testResourcePropertyId.getRandomPropertyValue(randomGenerator);
						resourcesDataManager.setResourcePropertyValue(testResourceId, testResourcePropertyId, expectedValue);
						// show that the property value was changed
						Object actualValue = resourcesDataManager.getResourcePropertyValue(testResourceId, testResourcePropertyId);
						assertEquals(expectedValue, actualValue);

						expectedObservations.add(new MultiKey(testResourceId, testResourcePropertyId, resourcePropertyValue, expectedValue));
					}
				}
			}

		}));

		// Have the observer show the the observations were properly generated
		pluginBuilder.addTestActorPlan("actor", new TestActorPlan(0, (c) -> {
			assertEquals(expectedObservations, actualObservations);
		}));

		TestPluginData testPluginData = pluginBuilder.build();
		Plugin testPlugin = TestPlugin.getTestPlugin(testPluginData);
		ResourcesActionSupport.testConsumers(0, 8240654442453940072L, testPlugin);

		/* precondition test: if the resource id is null */
		ResourcesActionSupport.testConsumer(0, 8603231391482244436L, (c) -> {
			ResourcePropertyId resourcePropertyId = TestResourcePropertyId.ResourceProperty_1_2_INTEGER_MUTABLE;
			ResourcesDataManager resourcesDataManager = c.getDataManager(ResourcesDataManager.class);
			Object value = 10;
			ContractException contractException = assertThrows(ContractException.class, () -> resourcesDataManager.setResourcePropertyValue(null, resourcePropertyId, value));
			assertEquals(ResourceError.NULL_RESOURCE_ID, contractException.getErrorType());
		});

		/* precondition test: if the resource id is unknown */
		ResourcesActionSupport.testConsumer(0, 4345368701918830681L, (c) -> {
			ResourcePropertyId resourcePropertyId = TestResourcePropertyId.ResourceProperty_1_2_INTEGER_MUTABLE;
			ResourcesDataManager resourcesDataManager = c.getDataManager(ResourcesDataManager.class);
			Object value = 10;
			ContractException contractException = assertThrows(ContractException.class,
					() -> resourcesDataManager.setResourcePropertyValue(TestResourceId.getUnknownResourceId(), resourcePropertyId, value));
			assertEquals(ResourceError.UNKNOWN_RESOURCE_ID, contractException.getErrorType());
		});

		/* precondition test: if the resource property id is null */
		ResourcesActionSupport.testConsumer(0, 697099694521127247L, (c) -> {
			ResourceId resourceId = TestResourceId.RESOURCE_1;
			ResourcesDataManager resourcesDataManager = c.getDataManager(ResourcesDataManager.class);
			Object value = 10;
			ContractException contractException = assertThrows(ContractException.class, () -> resourcesDataManager.setResourcePropertyValue(resourceId, null, value));
			assertEquals(PropertyError.NULL_PROPERTY_ID, contractException.getErrorType());
		});

		/* precondition test: if the resource property id is unknown */
		ResourcesActionSupport.testConsumer(0, 5208483875882077960L, (c) -> {
			ResourceId resourceId = TestResourceId.RESOURCE_1;
			ResourcesDataManager resourcesDataManager = c.getDataManager(ResourcesDataManager.class);
			Object value = 10;
			ContractException contractException = assertThrows(ContractException.class,
					() -> resourcesDataManager.setResourcePropertyValue(resourceId, TestResourcePropertyId.getUnknownResourcePropertyId(), value));
			assertEquals(PropertyError.UNKNOWN_PROPERTY_ID, contractException.getErrorType());
		});

		/* precondition test: if the resource property value is null */
		ResourcesActionSupport.testConsumer(0, 1862818482356534123L, (c) -> {
			ResourceId resourceId = TestResourceId.RESOURCE_1;
			ResourcePropertyId resourcePropertyId = TestResourcePropertyId.ResourceProperty_1_2_INTEGER_MUTABLE;
			ResourcesDataManager resourcesDataManager = c.getDataManager(ResourcesDataManager.class);
			ContractException contractException = assertThrows(ContractException.class, () -> resourcesDataManager.setResourcePropertyValue(resourceId, resourcePropertyId, null));
			assertEquals(PropertyError.NULL_PROPERTY_VALUE, contractException.getErrorType());
		});

		/*
		 * precondition test: if the resource property value is incompatible
		 * with the corresponding property definition
		 */
		ResourcesActionSupport.testConsumer(0, 8731358919842250070L, (c) -> {
			ResourceId resourceId = TestResourceId.RESOURCE_1;
			ResourcePropertyId resourcePropertyId = TestResourcePropertyId.ResourceProperty_1_2_INTEGER_MUTABLE;
			ResourcesDataManager resourcesDataManager = c.getDataManager(ResourcesDataManager.class);
			ContractException contractException = assertThrows(ContractException.class, () -> resourcesDataManager.setResourcePropertyValue(resourceId, resourcePropertyId, 23.4));
			assertEquals(PropertyError.INCOMPATIBLE_VALUE, contractException.getErrorType());
		});

		/* precondition test: if the property has been defined as immutable */
		ResourcesActionSupport.testConsumer(0, 2773568485593496806L, (c) -> {
			ResourcesDataManager resourcesDataManager = c.getDataManager(ResourcesDataManager.class);
			Object value = 10;
			ContractException contractException = assertThrows(ContractException.class,
					() -> resourcesDataManager.setResourcePropertyValue(TestResourceId.RESOURCE_5, TestResourcePropertyId.ResourceProperty_5_1_INTEGER_IMMUTABLE, value));
			assertEquals(PropertyError.IMMUTABLE_VALUE, contractException.getErrorType());
		});

	}

	@Test
	@UnitTestMethod(target = ResourcesDataManager.class, name = "removeResourceFromPerson", args = { ResourceId.class, PersonId.class, long.class })
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
				EventFilter<PersonResourceUpdateEvent> eventFilter = resourcesDataManager.getEventFilterForPersonResourceUpdateEvent(testResourceId);
				c.subscribe(eventFilter, (c2, e) -> {
					actualObservations.add(new MultiKey(e.personId(), e.resourceId(), e.previousResourceLevel(), e.currentResourceLevel()));
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

					expectedObservations.add(new MultiKey(personId, resourceId, personResourceLevel, expectedPersonResourceLevel));

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
		Plugin testPlugin = TestPlugin.getTestPlugin(testPluginData);
		ResourcesActionSupport.testConsumers(50, 6476360369877622233L, testPlugin);

		///////////////////
		/* precondition test: if the person id is null */
		ResourcesActionSupport.testConsumer(50, 368123167921446410L, (c) -> {
			RegionsDataManager regionsDataManager = c.getDataManager(RegionsDataManager.class);
			ResourcesDataManager resourcesDataManager = c.getDataManager(ResourcesDataManager.class);
			ResourceId resourceId = TestResourceId.RESOURCE_1;
			PersonId personId = new PersonId(0);
			long amount = 10;
			// add resource to the person to ensure the precondition tests will
			// work
			RegionId regionId = regionsDataManager.getPersonRegion(personId);
			resourcesDataManager.addResourceToRegion(resourceId, regionId, 100L);
			resourcesDataManager.transferResourceToPersonFromRegion(resourceId, personId, 100L);
			ContractException contractException = assertThrows(ContractException.class, () -> resourcesDataManager.removeResourceFromPerson(resourceId, null, amount));
			assertEquals(PersonError.NULL_PERSON_ID, contractException.getErrorType());
		});

		/* precondition test: if the person does not exist */
		ResourcesActionSupport.testConsumer(50, 463919801005664846L, (c) -> {
			RegionsDataManager regionsDataManager = c.getDataManager(RegionsDataManager.class);
			ResourcesDataManager resourcesDataManager = c.getDataManager(ResourcesDataManager.class);
			ResourceId resourceId = TestResourceId.RESOURCE_1;
			PersonId personId = new PersonId(0);
			long amount = 10;
			// add resource to the person to ensure the precondition tests will
			// work
			RegionId regionId = regionsDataManager.getPersonRegion(personId);
			resourcesDataManager.addResourceToRegion(resourceId, regionId, 100L);
			resourcesDataManager.transferResourceToPersonFromRegion(resourceId, personId, 100L);
			ContractException contractException = assertThrows(ContractException.class, () -> resourcesDataManager.removeResourceFromPerson(resourceId, new PersonId(1000), amount));
			assertEquals(PersonError.UNKNOWN_PERSON_ID, contractException.getErrorType());
		});

		/* precondition test: if the resource id is null */
		ResourcesActionSupport.testConsumer(50, 5201087860428100698L, (c) -> {
			RegionsDataManager regionsDataManager = c.getDataManager(RegionsDataManager.class);
			ResourcesDataManager resourcesDataManager = c.getDataManager(ResourcesDataManager.class);
			ResourceId resourceId = TestResourceId.RESOURCE_1;
			PersonId personId = new PersonId(0);
			long amount = 10;
			// add resource to the person to ensure the precondition tests will
			// work
			RegionId regionId = regionsDataManager.getPersonRegion(personId);
			resourcesDataManager.addResourceToRegion(resourceId, regionId, 100L);
			resourcesDataManager.transferResourceToPersonFromRegion(resourceId, personId, 100L);
			ContractException contractException = assertThrows(ContractException.class, () -> resourcesDataManager.removeResourceFromPerson(null, personId, amount));
			assertEquals(ResourceError.NULL_RESOURCE_ID, contractException.getErrorType());
		});

		/* precondition test: if the resource id is unknown */
		ResourcesActionSupport.testConsumer(50, 805801782412801541L, (c) -> {
			RegionsDataManager regionsDataManager = c.getDataManager(RegionsDataManager.class);
			ResourcesDataManager resourcesDataManager = c.getDataManager(ResourcesDataManager.class);
			ResourceId resourceId = TestResourceId.RESOURCE_1;
			PersonId personId = new PersonId(0);
			long amount = 10;
			// add resource to the person to ensure the precondition tests will
			// work
			RegionId regionId = regionsDataManager.getPersonRegion(personId);
			resourcesDataManager.addResourceToRegion(resourceId, regionId, 100L);
			resourcesDataManager.transferResourceToPersonFromRegion(resourceId, personId, 100L);
			ContractException contractException = assertThrows(ContractException.class, () -> resourcesDataManager.removeResourceFromPerson(TestResourceId.getUnknownResourceId(), personId, amount));
			assertEquals(ResourceError.UNKNOWN_RESOURCE_ID, contractException.getErrorType());
		});

		/* precondition test: if the amount is negative */
		ResourcesActionSupport.testConsumer(50, 6748548509217290999L, (c) -> {
			RegionsDataManager regionsDataManager = c.getDataManager(RegionsDataManager.class);
			ResourcesDataManager resourcesDataManager = c.getDataManager(ResourcesDataManager.class);
			ResourceId resourceId = TestResourceId.RESOURCE_1;
			PersonId personId = new PersonId(0);
			// add resource to the person to ensure the precondition tests will
			// work
			RegionId regionId = regionsDataManager.getPersonRegion(personId);
			resourcesDataManager.addResourceToRegion(resourceId, regionId, 100L);
			resourcesDataManager.transferResourceToPersonFromRegion(resourceId, personId, 100L);
			ContractException contractException = assertThrows(ContractException.class, () -> resourcesDataManager.removeResourceFromPerson(resourceId, personId, -1));
			assertEquals(ResourceError.NEGATIVE_RESOURCE_AMOUNT, contractException.getErrorType());

		});

		/*
		 * precondition test: if the person does not have the required amount of
		 * the resource
		 */
		ResourcesActionSupport.testConsumer(50, 6668079690803354725L, (c) -> {
			RegionsDataManager regionsDataManager = c.getDataManager(RegionsDataManager.class);
			ResourcesDataManager resourcesDataManager = c.getDataManager(ResourcesDataManager.class);
			ResourceId resourceId = TestResourceId.RESOURCE_1;
			PersonId personId = new PersonId(0);
			// add resource to the person to ensure the precondition tests will
			// work
			RegionId regionId = regionsDataManager.getPersonRegion(personId);
			resourcesDataManager.addResourceToRegion(resourceId, regionId, 100L);
			resourcesDataManager.transferResourceToPersonFromRegion(resourceId, personId, 100L);
			ContractException contractException = assertThrows(ContractException.class, () -> resourcesDataManager.removeResourceFromPerson(resourceId, personId, 10000));
			assertEquals(ResourceError.INSUFFICIENT_RESOURCES_AVAILABLE, contractException.getErrorType());
		});

	}

	@Test
	@UnitTestMethod(target = ResourcesDataManager.class, name = "removeResourceFromRegion", args = { ResourceId.class, RegionId.class, long.class })
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
					EventFilter<RegionResourceUpdateEvent> eventFilter = resourcesDataManager.getEventFilterForRegionResourceUpdateEvent(testResourceId, testRegionId);
					c.subscribe(eventFilter, (c2, e) -> {
						actualObservations.add(new MultiKey(e.regionId(), e.resourceId(), e.previousResourceLevel(), e.currentResourceLevel()));
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
		Plugin testPlugin = TestPlugin.getTestPlugin(testPluginData);
		ResourcesActionSupport.testConsumers(0, 3784957617927969790L, testPlugin);

		/* precondition test: if the region id is null */
		ResourcesActionSupport.testConsumer(0, 5886805948424471010L, (c) -> {
			ResourcesDataManager resourcesDataManager = c.getDataManager(ResourcesDataManager.class);
			ResourceId resourceId = TestResourceId.RESOURCE_1;
			long amount = 10;
			ContractException contractException = assertThrows(ContractException.class, () -> resourcesDataManager.removeResourceFromRegion(resourceId, null, amount));
			assertEquals(RegionError.NULL_REGION_ID, contractException.getErrorType());
		});

		/* precondition test: if the region id is unknown */
		ResourcesActionSupport.testConsumer(0, 1916159097321882678L, (c) -> {
			ResourcesDataManager resourcesDataManager = c.getDataManager(ResourcesDataManager.class);
			ResourceId resourceId = TestResourceId.RESOURCE_1;
			long amount = 10;
			ContractException contractException = assertThrows(ContractException.class, () -> resourcesDataManager.removeResourceFromRegion(resourceId, TestRegionId.getUnknownRegionId(), amount));
			assertEquals(RegionError.UNKNOWN_REGION_ID, contractException.getErrorType());
		});

		/* precondition test: if the resource id is null */
		ResourcesActionSupport.testConsumer(0, 6766634049148364532L, (c) -> {
			ResourcesDataManager resourcesDataManager = c.getDataManager(ResourcesDataManager.class);
			RegionId regionId = TestRegionId.REGION_1;
			long amount = 10;
			ContractException contractException = assertThrows(ContractException.class, () -> resourcesDataManager.removeResourceFromRegion(null, regionId, amount));
			assertEquals(ResourceError.NULL_RESOURCE_ID, contractException.getErrorType());
		});

		/* precondition test: if the resource id is unknown */
		ResourcesActionSupport.testConsumer(0, 3589045787461097821L, (c) -> {
			ResourcesDataManager resourcesDataManager = c.getDataManager(ResourcesDataManager.class);
			RegionId regionId = TestRegionId.REGION_1;
			long amount = 10;
			ContractException contractException = assertThrows(ContractException.class, () -> resourcesDataManager.removeResourceFromRegion(TestResourceId.getUnknownResourceId(), regionId, amount));
			assertEquals(ResourceError.UNKNOWN_RESOURCE_ID, contractException.getErrorType());
		});

		/* precondition test: if the amount is negative */
		ResourcesActionSupport.testConsumer(0, 4784578124305542584L, (c) -> {
			ResourcesDataManager resourcesDataManager = c.getDataManager(ResourcesDataManager.class);
			ResourceId resourceId = TestResourceId.RESOURCE_1;
			RegionId regionId = TestRegionId.REGION_1;
			ContractException contractException = assertThrows(ContractException.class, () -> resourcesDataManager.removeResourceFromRegion(resourceId, regionId, -1L));
			assertEquals(ResourceError.NEGATIVE_RESOURCE_AMOUNT, contractException.getErrorType());
		});

		/*
		 * precondition test: if the region does not have the required amount of
		 * the resource
		 */
		ResourcesActionSupport.testConsumer(0, 4875324598998641428L, (c) -> {
			ResourcesDataManager resourcesDataManager = c.getDataManager(ResourcesDataManager.class);
			ResourceId resourceId = TestResourceId.RESOURCE_1;
			RegionId regionId = TestRegionId.REGION_1;
			ContractException contractException = assertThrows(ContractException.class, () -> resourcesDataManager.removeResourceFromRegion(resourceId, regionId, 10000000L));
			assertEquals(ResourceError.INSUFFICIENT_RESOURCES_AVAILABLE, contractException.getErrorType());
		});

	}

	@Test
	@UnitTestMethod(target = ResourcesDataManager.class, name = "transferResourceBetweenRegions", args = { ResourceId.class, RegionId.class, RegionId.class, long.class })
	public void testTransferResourceBetweenRegions() {
		TestPluginData.Builder pluginBuilder = TestPluginData.builder();

		Set<MultiKey> actualObservations = new LinkedHashSet<>();
		Set<MultiKey> expectedObservations = new LinkedHashSet<>();

		// create an actor to observe resource transfers

		pluginBuilder.addTestActorPlan("observer", new TestActorPlan(0, (c) -> {
			ResourcesDataManager resourcesDataManager = c.getDataManager(ResourcesDataManager.class);
			for (TestRegionId testRegionId : TestRegionId.values()) {
				for (TestResourceId testResourceId : TestResourceId.values()) {
					EventFilter<RegionResourceUpdateEvent> eventFilter = resourcesDataManager.getEventFilterForRegionResourceUpdateEvent(testResourceId, testRegionId);
					c.subscribe(eventFilter, (c2, e) -> {
						actualObservations.add(new MultiKey(e.regionId(), e.resourceId(), e.previousResourceLevel(), e.currentResourceLevel()));
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
					resourcesDataManager.addResourceToRegion(testResourceId, testRegionId, 100L);
					expectedObservations.add(new MultiKey(testRegionId, testResourceId, 0L, 100L));
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

						expectedObservations.add(new MultiKey(regionId1, resourceId, region1Level, expectedRegion1Level));
						expectedObservations.add(new MultiKey(regionId2, resourceId, region2Level, expectedRegion2Level));

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
		Plugin testPlugin = TestPlugin.getTestPlugin(testPluginData);
		ResourcesActionSupport.testConsumers(0, 7976375269741360076L, testPlugin);

		/* precondition test: if the source region is null */
		ResourcesActionSupport.testConsumer(0, 2545276913032843668L, (c) -> {
			ResourcesDataManager resourcesDataManager = c.getDataManager(ResourcesDataManager.class);
			ResourceId resourceId = TestResourceId.RESOURCE_1;
			RegionId regionId2 = TestRegionId.REGION_2;
			long amount = 10;

			// add resources to all the regions to ensure the precondition tests
			// will work
			for (TestRegionId testRegionId : TestRegionId.values()) {
				for (TestResourceId testResourceId : TestResourceId.values()) {
					resourcesDataManager.addResourceToRegion(testResourceId, testRegionId, 100L);
				}
			}
			ContractException contractException = assertThrows(ContractException.class, () -> resourcesDataManager.transferResourceBetweenRegions(resourceId, null, regionId2, amount));
			assertEquals(RegionError.NULL_REGION_ID, contractException.getErrorType());
		});

		/* precondition test: if the source region is unknown */
		ResourcesActionSupport.testConsumer(0, 1182536948902380826L, (c) -> {
			ResourcesDataManager resourcesDataManager = c.getDataManager(ResourcesDataManager.class);
			ResourceId resourceId = TestResourceId.RESOURCE_1;
			RegionId regionId2 = TestRegionId.REGION_2;
			long amount = 10;
			// add resources to all the regions to ensure the precondition tests
			// will work
			for (TestRegionId testRegionId : TestRegionId.values()) {
				for (TestResourceId testResourceId : TestResourceId.values()) {
					resourcesDataManager.addResourceToRegion(testResourceId, testRegionId, 100L);
				}
			}
			ContractException contractException = assertThrows(ContractException.class,
					() -> resourcesDataManager.transferResourceBetweenRegions(resourceId, TestRegionId.getUnknownRegionId(), regionId2, amount));
			assertEquals(RegionError.UNKNOWN_REGION_ID, contractException.getErrorType());
		});

		/* precondition test: if the destination region is null */
		ResourcesActionSupport.testConsumer(0, 3358578155263941L, (c) -> {
			ResourcesDataManager resourcesDataManager = c.getDataManager(ResourcesDataManager.class);
			ResourceId resourceId = TestResourceId.RESOURCE_1;
			RegionId regionId1 = TestRegionId.REGION_1;
			long amount = 10;
			// add resources to all the regions to ensure the precondition tests
			// will work
			for (TestRegionId testRegionId : TestRegionId.values()) {
				for (TestResourceId testResourceId : TestResourceId.values()) {
					resourcesDataManager.addResourceToRegion(testResourceId, testRegionId, 100L);
				}
			}
			ContractException contractException = assertThrows(ContractException.class, () -> resourcesDataManager.transferResourceBetweenRegions(resourceId, regionId1, null, amount));
			assertEquals(RegionError.NULL_REGION_ID, contractException.getErrorType());
		});

		/* precondition test: if the destination region is unknown */
		ResourcesActionSupport.testConsumer(0, 289436879730670757L, (c) -> {
			ResourcesDataManager resourcesDataManager = c.getDataManager(ResourcesDataManager.class);
			ResourceId resourceId = TestResourceId.RESOURCE_1;
			RegionId regionId1 = TestRegionId.REGION_1;
			long amount = 10;
			// add resources to all the regions to ensure the precondition tests
			// will work
			for (TestRegionId testRegionId : TestRegionId.values()) {
				for (TestResourceId testResourceId : TestResourceId.values()) {
					resourcesDataManager.addResourceToRegion(testResourceId, testRegionId, 100L);
				}
			}
			ContractException contractException = assertThrows(ContractException.class,
					() -> resourcesDataManager.transferResourceBetweenRegions(resourceId, regionId1, TestRegionId.getUnknownRegionId(), amount));
			assertEquals(RegionError.UNKNOWN_REGION_ID, contractException.getErrorType());
		});

		/* precondition test: if the resource id is null */
		ResourcesActionSupport.testConsumer(0, 3690172166437098600L, (c) -> {
			ResourcesDataManager resourcesDataManager = c.getDataManager(ResourcesDataManager.class);
			RegionId regionId1 = TestRegionId.REGION_1;
			RegionId regionId2 = TestRegionId.REGION_2;
			long amount = 10;
			// add resources to all the regions to ensure the precondition tests
			// will work
			for (TestRegionId testRegionId : TestRegionId.values()) {
				for (TestResourceId testResourceId : TestResourceId.values()) {
					resourcesDataManager.addResourceToRegion(testResourceId, testRegionId, 100L);
				}
			}
			ContractException contractException = assertThrows(ContractException.class, () -> resourcesDataManager.transferResourceBetweenRegions(null, regionId1, regionId2, amount));
			assertEquals(ResourceError.NULL_RESOURCE_ID, contractException.getErrorType());
		});

		/* precondition test: if the resource id is unknown */
		ResourcesActionSupport.testConsumer(0, 7636787584894783093L, (c) -> {
			ResourcesDataManager resourcesDataManager = c.getDataManager(ResourcesDataManager.class);
			RegionId regionId1 = TestRegionId.REGION_1;
			RegionId regionId2 = TestRegionId.REGION_2;
			long amount = 10;
			// add resources to all the regions to ensure the precondition tests
			// will work
			for (TestRegionId testRegionId : TestRegionId.values()) {
				for (TestResourceId testResourceId : TestResourceId.values()) {
					resourcesDataManager.addResourceToRegion(testResourceId, testRegionId, 100L);
				}
			}
			ContractException contractException = assertThrows(ContractException.class,
					() -> resourcesDataManager.transferResourceBetweenRegions(TestResourceId.getUnknownResourceId(), regionId1, regionId2, amount));
			assertEquals(ResourceError.UNKNOWN_RESOURCE_ID, contractException.getErrorType());
		});

		/* precondition test: if the resource amount is negative */
		ResourcesActionSupport.testConsumer(0, 1320571074133841280L, (c) -> {
			ResourcesDataManager resourcesDataManager = c.getDataManager(ResourcesDataManager.class);
			ResourceId resourceId = TestResourceId.RESOURCE_1;
			RegionId regionId1 = TestRegionId.REGION_1;
			RegionId regionId2 = TestRegionId.REGION_2;
			// add resources to all the regions to ensure the precondition tests
			// will work
			for (TestRegionId testRegionId : TestRegionId.values()) {
				for (TestResourceId testResourceId : TestResourceId.values()) {
					resourcesDataManager.addResourceToRegion(testResourceId, testRegionId, 100L);
				}
			}
			ContractException contractException = assertThrows(ContractException.class, () -> resourcesDataManager.transferResourceBetweenRegions(resourceId, regionId1, regionId2, -1));
			assertEquals(ResourceError.NEGATIVE_RESOURCE_AMOUNT, contractException.getErrorType());
		});

		/* precondition test: if the source and destination region are equal */
		ResourcesActionSupport.testConsumer(0, 2402299633191289724L, (c) -> {
			ResourcesDataManager resourcesDataManager = c.getDataManager(ResourcesDataManager.class);
			ResourceId resourceId = TestResourceId.RESOURCE_1;
			RegionId regionId1 = TestRegionId.REGION_1;
			long amount = 10;
			// add resources to all the regions to ensure the precondition tests
			// will work
			for (TestRegionId testRegionId : TestRegionId.values()) {
				for (TestResourceId testResourceId : TestResourceId.values()) {
					resourcesDataManager.addResourceToRegion(testResourceId, testRegionId, 100L);
				}
			}
			ContractException contractException = assertThrows(ContractException.class, () -> resourcesDataManager.transferResourceBetweenRegions(resourceId, regionId1, regionId1, amount));
			assertEquals(ResourceError.REFLEXIVE_RESOURCE_TRANSFER, contractException.getErrorType());
		});

		/*
		 * precondition test: if the source region does not have sufficient
		 * resources to support the transfer
		 */
		ResourcesActionSupport.testConsumer(0, 9136536902267748610L, (c) -> {
			ResourcesDataManager resourcesDataManager = c.getDataManager(ResourcesDataManager.class);
			ResourceId resourceId = TestResourceId.RESOURCE_1;
			RegionId regionId1 = TestRegionId.REGION_1;
			RegionId regionId2 = TestRegionId.REGION_2;
			// add resources to all the regions to ensure the precondition tests
			// will work
			for (TestRegionId testRegionId : TestRegionId.values()) {
				for (TestResourceId testResourceId : TestResourceId.values()) {
					resourcesDataManager.addResourceToRegion(testResourceId, testRegionId, 100L);
				}
			}
			ContractException contractException = assertThrows(ContractException.class, () -> resourcesDataManager.transferResourceBetweenRegions(resourceId, regionId1, regionId2, 100000L));
			assertEquals(ResourceError.INSUFFICIENT_RESOURCES_AVAILABLE, contractException.getErrorType());
		});

		/*
		 * precondition test: if the transfer will cause a numeric overflow in
		 * the destination region
		 */
		ResourcesActionSupport.testConsumer(0, 342832088592207841L, (c) -> {
			ResourcesDataManager resourcesDataManager = c.getDataManager(ResourcesDataManager.class);
			ResourceId resourceId = TestResourceId.RESOURCE_1;
			RegionId regionId1 = TestRegionId.REGION_1;
			RegionId regionId2 = TestRegionId.REGION_2;
			long amount = 10;
			// add resources to all the regions to ensure the precondition tests
			// will work
			for (TestRegionId testRegionId : TestRegionId.values()) {
				for (TestResourceId testResourceId : TestResourceId.values()) {
					resourcesDataManager.addResourceToRegion(testResourceId, testRegionId, 100L);
				}
			}
			// fill region 2 to the max long value
			long fillAmount = Long.MAX_VALUE - resourcesDataManager.getRegionResourceLevel(regionId2, resourceId);
			resourcesDataManager.addResourceToRegion(resourceId, regionId2, fillAmount);
			ContractException contractException = assertThrows(ContractException.class, () -> resourcesDataManager.transferResourceBetweenRegions(resourceId, regionId1, regionId2, amount));
			assertEquals(ResourceError.RESOURCE_ARITHMETIC_EXCEPTION, contractException.getErrorType());
		});

	}

	@Test
	@UnitTestMethod(target = ResourcesDataManager.class, name = "transferResourceFromPersonToRegion", args = { ResourceId.class, PersonId.class, long.class })
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
					EventFilter<RegionResourceUpdateEvent> eventFilter = resourcesDataManager.getEventFilterForRegionResourceUpdateEvent(testResourceId, testRegionId);
					c.subscribe(eventFilter, (c2, e) -> {
						actualObservations.add(new MultiKey(e.regionId(), e.resourceId(), e.previousResourceLevel(), e.currentResourceLevel()));
					});
				}
			}
			for (TestResourceId testResourceId : TestResourceId.values()) {
				EventFilter<PersonResourceUpdateEvent> eventFilter = resourcesDataManager.getEventFilterForPersonResourceUpdateEvent(testResourceId);
				c.subscribe(eventFilter, (c2, e) -> {
					actualObservations.add(new MultiKey(e.personId(), e.resourceId(), e.previousResourceLevel(), e.currentResourceLevel()));
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

					expectedObservations.add(new MultiKey(regionId, resourceId, regionResourceLevel, expectedRegionResourceLevel));
					expectedObservations.add(new MultiKey(personId, resourceId, personResourceLevel, expectedPersonResourceLevel));

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

			ContractException contractException = assertThrows(ContractException.class, () -> resourcesDataManager.transferResourceFromPersonToRegion(resourceId, null, amount));
			assertEquals(PersonError.NULL_PERSON_ID, contractException.getErrorType());

			// if the person does not exist
			contractException = assertThrows(ContractException.class, () -> resourcesDataManager.transferResourceFromPersonToRegion(resourceId, new PersonId(3434), amount));
			assertEquals(PersonError.UNKNOWN_PERSON_ID, contractException.getErrorType());

			// if the resource id is null
			contractException = assertThrows(ContractException.class, () -> resourcesDataManager.transferResourceFromPersonToRegion(null, personId, amount));
			assertEquals(ResourceError.NULL_RESOURCE_ID, contractException.getErrorType());

			// if the resource id is unknown
			contractException = assertThrows(ContractException.class, () -> resourcesDataManager.transferResourceFromPersonToRegion(TestResourceId.getUnknownResourceId(), personId, amount));
			assertEquals(ResourceError.UNKNOWN_RESOURCE_ID, contractException.getErrorType());

			// if the amount is negative
			contractException = assertThrows(ContractException.class, () -> resourcesDataManager.transferResourceFromPersonToRegion(resourceId, personId, -1L));
			assertEquals(ResourceError.NEGATIVE_RESOURCE_AMOUNT, contractException.getErrorType());

			// if the person does not have the required amount of the resource
			contractException = assertThrows(ContractException.class, () -> resourcesDataManager.transferResourceFromPersonToRegion(resourceId, personId, 1000000));
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

			contractException = assertThrows(ContractException.class, () -> resourcesDataManager.transferResourceFromPersonToRegion(resourceId, personId, Long.MAX_VALUE));
			assertEquals(ResourceError.RESOURCE_ARITHMETIC_EXCEPTION, contractException.getErrorType());

		}));

		TestPluginData testPluginData = pluginBuilder.build();
		Plugin testPlugin = TestPlugin.getTestPlugin(testPluginData);

		ResourcesActionSupport.testConsumers(30, 3166011813977431605L, testPlugin);

		ResourcesActionSupport.testConsumer(30, 1001250760859234604L, (c) -> {

		});

	}

	@Test
	@UnitTestMethod(target = ResourcesDataManager.class, name = "transferResourceToPersonFromRegion", args = { ResourceId.class, PersonId.class, long.class })
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
					EventFilter<RegionResourceUpdateEvent> eventFilter = resourcesDataManager.getEventFilterForRegionResourceUpdateEvent(testResourceId, testRegionId);
					c.subscribe(eventFilter, (c2, e) -> {
						actualObservations.add(new MultiKey(e.regionId(), e.resourceId(), e.previousResourceLevel(), e.currentResourceLevel()));
					});
				}
			}

			for (TestResourceId testResourceId : TestResourceId.values()) {
				EventFilter<PersonResourceUpdateEvent> eventFilter = resourcesDataManager.getEventFilterForPersonResourceUpdateEvent(testResourceId);
				c.subscribe(eventFilter, (c2, e) -> {
					actualObservations.add(new MultiKey(e.personId(), e.resourceId(), e.previousResourceLevel(), e.currentResourceLevel()));

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

					expectedObservations.add(new MultiKey(regionId, resourceId, regionResourceLevel, expectedRegionResourceLevel));
					expectedObservations.add(new MultiKey(personId, resourceId, personResourceLevel, expectedPersonResourceLevel));

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
		Plugin testPlugin = TestPlugin.getTestPlugin(testPluginData);
		ResourcesActionSupport.testConsumers(30, 3808042869854225459L, testPlugin);

		/* precondition test: if the person id is null */
		ResourcesActionSupport.testConsumer(30, 2628501738627419743L, (c) -> {
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
			ContractException contractException = assertThrows(ContractException.class, () -> resourcesDataManager.transferResourceToPersonFromRegion(resourceId, null, amount));
			assertEquals(PersonError.NULL_PERSON_ID, contractException.getErrorType());
		});

		/* precondition test: if the person does not exist */
		ResourcesActionSupport.testConsumer(30, 4172586983768511485L, (c) -> {
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
			ContractException contractException = assertThrows(ContractException.class, () -> resourcesDataManager.transferResourceToPersonFromRegion(resourceId, new PersonId(3434), amount));
			assertEquals(PersonError.UNKNOWN_PERSON_ID, contractException.getErrorType());
		});

		/* precondition test: if the resource id is null */
		ResourcesActionSupport.testConsumer(30, 6256935891787853979L, (c) -> {
			ResourcesDataManager resourcesDataManager = c.getDataManager(ResourcesDataManager.class);
			RegionsDataManager regionsDataManager = c.getDataManager(RegionsDataManager.class);
			PersonId personId = new PersonId(0);
			long amount = 10;
			// add resources to the region to support the precondition tests
			RegionId regionId = regionsDataManager.getPersonRegion(personId);
			for (TestResourceId testResourceId : TestResourceId.values()) {
				resourcesDataManager.addResourceToRegion(testResourceId, regionId, 100L);
			}
			ContractException contractException = assertThrows(ContractException.class, () -> resourcesDataManager.transferResourceToPersonFromRegion(null, personId, amount));
			assertEquals(ResourceError.NULL_RESOURCE_ID, contractException.getErrorType());
		});

		/* precondition test: if the resource id is unknown */
		ResourcesActionSupport.testConsumer(30, 6949348067383487020L, (c) -> {
			ResourcesDataManager resourcesDataManager = c.getDataManager(ResourcesDataManager.class);
			RegionsDataManager regionsDataManager = c.getDataManager(RegionsDataManager.class);
			PersonId personId = new PersonId(0);
			long amount = 10;
			// add resources to the region to support the precondition tests
			RegionId regionId = regionsDataManager.getPersonRegion(personId);
			for (TestResourceId testResourceId : TestResourceId.values()) {
				resourcesDataManager.addResourceToRegion(testResourceId, regionId, 100L);
			}
			ContractException contractException = assertThrows(ContractException.class,
					() -> resourcesDataManager.transferResourceToPersonFromRegion(TestResourceId.getUnknownResourceId(), personId, amount));
			assertEquals(ResourceError.UNKNOWN_RESOURCE_ID, contractException.getErrorType());
		});

		/* precondition test: if the amount is negative */
		ResourcesActionSupport.testConsumer(30, 6911979438110217773L, (c) -> {
			ResourcesDataManager resourcesDataManager = c.getDataManager(ResourcesDataManager.class);
			RegionsDataManager regionsDataManager = c.getDataManager(RegionsDataManager.class);
			PersonId personId = new PersonId(0);
			ResourceId resourceId = TestResourceId.RESOURCE_4;
			// add resources to the region to support the precondition tests
			RegionId regionId = regionsDataManager.getPersonRegion(personId);
			for (TestResourceId testResourceId : TestResourceId.values()) {
				resourcesDataManager.addResourceToRegion(testResourceId, regionId, 100L);
			}
			ContractException contractException = assertThrows(ContractException.class, () -> resourcesDataManager.transferResourceToPersonFromRegion(resourceId, personId, -1L));
			assertEquals(ResourceError.NEGATIVE_RESOURCE_AMOUNT, contractException.getErrorType());
		});

		/*
		 * precondition test: if the region does not have the required amount of
		 * the resource
		 */
		ResourcesActionSupport.testConsumer(30, 1022333582572896703L, (c) -> {
			ResourcesDataManager resourcesDataManager = c.getDataManager(ResourcesDataManager.class);
			RegionsDataManager regionsDataManager = c.getDataManager(RegionsDataManager.class);
			PersonId personId = new PersonId(0);
			ResourceId resourceId = TestResourceId.RESOURCE_4;
			// add resources to the region to support the precondition tests
			RegionId regionId = regionsDataManager.getPersonRegion(personId);
			for (TestResourceId testResourceId : TestResourceId.values()) {
				resourcesDataManager.addResourceToRegion(testResourceId, regionId, 100L);
			}
			ContractException contractException = assertThrows(ContractException.class, () -> resourcesDataManager.transferResourceToPersonFromRegion(resourceId, personId, 1000000));
			assertEquals(ResourceError.INSUFFICIENT_RESOURCES_AVAILABLE, contractException.getErrorType());
		});

		/*
		 * precondition test: if the transfer results in an overflow of the
		 * person's resource level
		 */
		ResourcesActionSupport.testConsumer(30, 1989550065510462161L, (c) -> {
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

			ContractException contractException = assertThrows(ContractException.class, () -> resourcesDataManager.transferResourceToPersonFromRegion(resourceId, personId, 1L));
			assertEquals(ResourceError.RESOURCE_ARITHMETIC_EXCEPTION, contractException.getErrorType());

		});

	}

	@Test
	@UnitTestMethod(target = ResourcesDataManager.class, name = "addResourceToRegion", args = { ResourceId.class, RegionId.class, long.class })
	public void testAddResourceToRegion() {

		TestPluginData.Builder pluginBuilder = TestPluginData.builder();

		Set<MultiKey> expectedObservations = new LinkedHashSet<>();
		Set<MultiKey> actualObservations = new LinkedHashSet<>();

		// Have an actor to observe the resource changes

		pluginBuilder.addTestActorPlan("observer", new TestActorPlan(0, (c) -> {
			ResourcesDataManager resourcesDataManager = c.getDataManager(ResourcesDataManager.class);
			for (TestRegionId testRegionId : TestRegionId.values()) {
				for (TestResourceId testResourceId : TestResourceId.values()) {
					EventFilter<RegionResourceUpdateEvent> eventFilter = resourcesDataManager.getEventFilterForRegionResourceUpdateEvent(testResourceId, testRegionId);
					c.subscribe(eventFilter, (c2, e) -> {
						actualObservations.add(new MultiKey(e.regionId(), e.resourceId(), e.previousResourceLevel(), e.currentResourceLevel()));
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
		Plugin testPlugin = TestPlugin.getTestPlugin(testPluginData);
		ResourcesActionSupport.testConsumers(0, 2273638431976256278L, testPlugin);

		/* precondition test: if the region id is null */
		ResourcesActionSupport.testConsumer(0, 6097938300290796293L, (c) -> {
			ResourceId resourceId = TestResourceId.RESOURCE_1;
			long amount = 10;
			ResourcesDataManager resourcesDataManager = c.getDataManager(ResourcesDataManager.class);
			ContractException contractException = assertThrows(ContractException.class, () -> resourcesDataManager.addResourceToRegion(resourceId, null, amount));
			assertEquals(RegionError.NULL_REGION_ID, contractException.getErrorType());
		});

		/* precondition test: if the region id is unknown */
		ResourcesActionSupport.testConsumer(0, 1284607529543124944L, (c) -> {
			ResourceId resourceId = TestResourceId.RESOURCE_1;
			long amount = 10;
			ResourcesDataManager resourcesDataManager = c.getDataManager(ResourcesDataManager.class);
			ContractException contractException = assertThrows(ContractException.class, () -> resourcesDataManager.addResourceToRegion(resourceId, TestRegionId.getUnknownRegionId(), amount));
			assertEquals(RegionError.UNKNOWN_REGION_ID, contractException.getErrorType());
		});

		/* precondition test: if the resource id is null */
		ResourcesActionSupport.testConsumer(0, 5929063621703486118L, (c) -> {
			RegionId regionId = TestRegionId.REGION_1;
			long amount = 10;
			ResourcesDataManager resourcesDataManager = c.getDataManager(ResourcesDataManager.class);
			ContractException contractException = assertThrows(ContractException.class, () -> resourcesDataManager.addResourceToRegion(null, regionId, amount));
			assertEquals(ResourceError.NULL_RESOURCE_ID, contractException.getErrorType());
		});

		/* precondition test: if the resource id is unknown */
		ResourcesActionSupport.testConsumer(0, 1240045272882068003L, (c) -> {
			RegionId regionId = TestRegionId.REGION_1;
			long amount = 10;
			ResourcesDataManager resourcesDataManager = c.getDataManager(ResourcesDataManager.class);
			ContractException contractException = assertThrows(ContractException.class, () -> resourcesDataManager.addResourceToRegion(TestResourceId.getUnknownResourceId(), regionId, amount));
			assertEquals(ResourceError.UNKNOWN_RESOURCE_ID, contractException.getErrorType());
		});

		/* precondition test: if the amount is negative */
		ResourcesActionSupport.testConsumer(0, 2192023733930104434L, (c) -> {
			ResourceId resourceId = TestResourceId.RESOURCE_1;
			RegionId regionId = TestRegionId.REGION_1;
			ResourcesDataManager resourcesDataManager = c.getDataManager(ResourcesDataManager.class);
			ContractException contractException = assertThrows(ContractException.class, () -> resourcesDataManager.addResourceToRegion(resourceId, regionId, -1));
			assertEquals(ResourceError.NEGATIVE_RESOURCE_AMOUNT, contractException.getErrorType());
		});

		/* precondition test: if the addition results in an overflow */
		ResourcesActionSupport.testConsumer(0, 4518775448744653729L, (c) -> {
			ResourceId resourceId = TestResourceId.RESOURCE_1;
			RegionId regionId = TestRegionId.REGION_1;
			long amount = 10;
			ResourcesDataManager resourcesDataManager = c.getDataManager(ResourcesDataManager.class);
			resourcesDataManager.addResourceToRegion(resourceId, regionId, amount);
			ContractException contractException = assertThrows(ContractException.class, () -> resourcesDataManager.addResourceToRegion(resourceId, regionId, Long.MAX_VALUE));
			assertEquals(ResourceError.RESOURCE_ARITHMETIC_EXCEPTION, contractException.getErrorType());
		});

	}

	@Test
	@UnitTestMethod(target = ResourcesDataManager.class, name = "init", args = { DataManagerContext.class })
	public void testPersonAdditionEvent() {

		// Have an actor create a few people with random resource levels
		ResourcesActionSupport.testConsumer(0, 5441878385875188805L, (c) -> {
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
						ResourceInitialization resourceInitialization = new ResourceInitialization(testResourceId, (long) amount);
						builder.add(resourceInitialization);
					}
				}

				// create the person which will in turn generate the
				// PersonAdditionEvent
				PersonId personId = peopleDataManager.addPerson(builder.build());

				// show that the person has the correct resource levels
				for (TestResourceId testResourceId : TestResourceId.values()) {
					int actualPersonResourceLevel = (int) resourcesDataManager.getPersonResourceLevel(testResourceId, personId);
					int expectedPersonResourceLevel = expectedResources.get(testResourceId).getValue();
					assertEquals(expectedPersonResourceLevel, actualPersonResourceLevel);
				}
			}

		});

		/* precondition test: */
		ResourcesActionSupport.testConsumer(0, 3508334533286675130L, (c) -> {
			PeopleDataManager peopleDataManager = c.getDataManager(PeopleDataManager.class);

			/*
			 * Precondition tests for the validity of the person id are shadowed
			 * by other plugins and cannot be easily tested
			 */

			/*
			 * if the auxiliary data contains a ResourceInitialization that has
			 * a null resource id
			 */

			ContractException contractException = assertThrows(ContractException.class, () -> {

				peopleDataManager.addPerson(PersonConstructionData.builder().add(TestRegionId.REGION_1).add(new ResourceInitialization(null, 15L)).build());
			});
			assertEquals(ResourceError.NULL_RESOURCE_ID, contractException.getErrorType());

		});

		/* precondition test: */
		ResourcesActionSupport.testConsumer(0, 7458875943724352968L, (c) -> {
			PeopleDataManager peopleDataManager = c.getDataManager(PeopleDataManager.class);

			/*
			 * Precondition tests for the validity of the person id are shadowed
			 * by other plugins and cannot be easily tested
			 */

			/*
			 * if the auxiliary data contains a ResourceInitialization that has
			 * an unknown resource id
			 */
			ContractException contractException = assertThrows(ContractException.class, () -> {
				peopleDataManager.addPerson(PersonConstructionData	.builder()

																	.add(TestRegionId.REGION_2).add(new ResourceInitialization(TestResourceId.getUnknownResourceId(), 15L)).build());
			});
			assertEquals(ResourceError.UNKNOWN_RESOURCE_ID, contractException.getErrorType());

		});

		/* precondition test: */
		ResourcesActionSupport.testConsumer(0, 3702960689314847457L, (c) -> {
			PeopleDataManager peopleDataManager = c.getDataManager(PeopleDataManager.class);

			/*
			 * Precondition tests for the validity of the person id are shadowed
			 * by other plugins and cannot be easily tested
			 */

			/*
			 * if the auxiliary data contains a ResourceInitialization that has
			 * a negative resource level
			 */
			ContractException contractException = assertThrows(ContractException.class, () -> {
				peopleDataManager.addPerson(PersonConstructionData	.builder()

																	.add(TestRegionId.REGION_3).add(new ResourceInitialization(TestResourceId.RESOURCE_1, -15L)).build());
			});
			assertEquals(ResourceError.NEGATIVE_RESOURCE_AMOUNT, contractException.getErrorType());

		});
	}

	@Test
	@UnitTestMethod(target = ResourcesDataManager.class, name = "init", args = { DataManagerContext.class })
	public void testRegionAdditionEvent() {

		/*
		 * show that an unknown region will cause the resource data manager to
		 * throw an exception when retrieving a resource level for that region
		 */
		ResourcesActionSupport.testConsumer(0, 4192802703078518338L, (c) -> {
			RegionId newRegionId = TestRegionId.getUnknownRegionId();
			ResourcesDataManager resourcesDataManager = c.getDataManager(ResourcesDataManager.class);
			assertThrows(ContractException.class, () -> resourcesDataManager.getRegionResourceLevel(newRegionId, TestResourceId.RESOURCE_1));
		});

		/*
		 * show that a newly added region will cause the resource data manager
		 * to return the expected levels from the event.
		 */
		ResourcesActionSupport.testConsumer(0, 7471968091128250788L, (c) -> {
			RegionsDataManager regionsDataManager = c.getDataManager(RegionsDataManager.class);
			RegionId newRegionId = TestRegionId.getUnknownRegionId();

			Map<TestResourceId, Long> expectedValues = new LinkedHashMap<>();
			for (TestResourceId testResourceId : TestResourceId.values()) {
				expectedValues.put(testResourceId, 0L);
			}
			expectedValues.put(TestResourceId.RESOURCE_1, 75L);
			expectedValues.put(TestResourceId.RESOURCE_2, 432L);

			RegionConstructionData.Builder regionConstructionDataBuilder = //
					RegionConstructionData	.builder()//
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
	}

	@Test
	@UnitTestMethod(target = ResourcesDataManager.class, name = "init", args = { DataManagerContext.class })
	public void testInitializeResourceDataManager() {

		int initialPopulation = 10;

		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(1828556358289827784L);

		// create a list of people
		List<PersonId> people = new ArrayList<>();
		for (int i = 0; i < initialPopulation; i++) {
			people.add(new PersonId(i));
		}

		Builder builder = Simulation.builder();

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
			resourcesBuilder.addResource(testResourceId);
			resourcesBuilder.setResourceTimeTracking(testResourceId, testResourceId.getTimeTrackingPolicy());
		}

		for (TestResourcePropertyId testResourcePropertyId : TestResourcePropertyId.values()) {
			TestResourceId testResourceId = testResourcePropertyId.getTestResourceId();
			PropertyDefinition propertyDefinition = testResourcePropertyId.getPropertyDefinition();
			Object propertyValue = testResourcePropertyId.getRandomPropertyValue(randomGenerator);
			resourcesBuilder.defineResourceProperty(testResourceId, testResourcePropertyId, propertyDefinition);
			resourcesBuilder.setResourcePropertyValue(testResourceId, testResourcePropertyId, propertyValue);
		}

		ResourcesPluginData resourcesPluginData = resourcesBuilder.build();
		Plugin resourcesPlugin = ResourcesPlugin.getResourcesPlugin(resourcesPluginData);
		builder.addPlugin(resourcesPlugin);

		// add the people plugin

		PeoplePluginData.Builder peopleBuilder = PeoplePluginData.builder();

		for (PersonId personId : people) {
			peopleBuilder.addPersonId(personId);
		}
		PeoplePluginData peoplePluginData = peopleBuilder.build();
		Plugin peoplePlugin = PeoplePlugin.getPeoplePlugin(peoplePluginData);
		builder.addPlugin(peoplePlugin);

		// add the regions plugin
		RegionsPluginData.Builder regionsBuilder = RegionsPluginData.builder();
		for (TestRegionId testRegionId : TestRegionId.values()) {
			regionsBuilder.addRegion(testRegionId);
		}
		for (PersonId personId : people) {
			regionsBuilder.setPersonRegion(personId, TestRegionId.getRandomRegionId(randomGenerator));
		}
		RegionsPluginData regionsPluginData = regionsBuilder.build();
		Plugin regionPlugin = RegionsPlugin.getRegionsPlugin(regionsPluginData);
		builder.addPlugin(regionPlugin);

		// add the stochastics plugin
		StochasticsPluginData stochasticsPluginData = StochasticsPluginData.builder().setSeed(randomGenerator.nextLong()).build();
		Plugin stochasticsPlugin = StochasticsPlugin.getStochasticsPlugin(stochasticsPluginData);
		builder.addPlugin(stochasticsPlugin);

		TestPluginData.Builder pluginBuilder = TestPluginData.builder();

		pluginBuilder.addTestActorPlan("actor", new TestActorPlan(0, (c) -> {

			ResourcesDataManager resourcesDataManager = c.getDataManager(ResourcesDataManager.class);
			PeopleDataManager peopleDataManager = c.getDataManager(PeopleDataManager.class);
			RegionsDataManager regionsDataManager = c.getDataManager(RegionsDataManager.class);

			List<PersonId> personIds = peopleDataManager.getPeople();
			assertEquals(personIds.size(), resourcesPluginData.getPersonCount());

			Set<RegionId> expectedRegionIds = regionsDataManager.getRegionIds();
			Set<RegionId> actualRegionIds = resourcesPluginData.getRegionIds();
			assertEquals(expectedRegionIds, actualRegionIds);

			for (RegionId regionId : resourcesPluginData.getRegionIds()) {
				Map<ResourceId, Long> expectedAmounts = new LinkedHashMap<>();
				for (ResourceId resourceId : resourcesPluginData.getResourceIds()) {
					expectedAmounts.put(resourceId, 0L);
				}
				for (ResourceInitialization resourceInitialization : resourcesPluginData.getRegionResourceLevels(regionId)) {
					expectedAmounts.put(resourceInitialization.getResourceId(), resourceInitialization.getAmount());
				}
				for (ResourceId resourceId : resourcesPluginData.getResourceIds()) {
					long expectedRegionResourceLevel = expectedAmounts.get(resourceId);
					long actualRegionResourceLevel = resourcesDataManager.getRegionResourceLevel(regionId, resourceId);
					assertEquals(expectedRegionResourceLevel, actualRegionResourceLevel);
				}
			}

			for (ResourceId resourceId : resourcesPluginData.getResourceIds()) {
				TimeTrackingPolicy expectedPolicy = resourcesPluginData.getPersonResourceTimeTrackingPolicy(resourceId);
				TimeTrackingPolicy actualPolicy = resourcesDataManager.getPersonResourceTimeTrackingPolicy(resourceId);
				assertEquals(expectedPolicy, actualPolicy);
			}

			assertEquals(resourcesPluginData.getResourceIds(), resourcesDataManager.getResourceIds());

			for (PersonId personId : personIds) {
				List<ResourceInitialization> personResourceLevels = resourcesPluginData.getPersonResourceLevels(personId);
				Map<ResourceId, Long> expectedAmounts = new LinkedHashMap<>();
				for (ResourceId resourceId : resourcesPluginData.getResourceIds()) {
					expectedAmounts.put(resourceId, 0L);
				}
				for (ResourceInitialization resourceInitialization : personResourceLevels) {
					expectedAmounts.put(resourceInitialization.getResourceId(), resourceInitialization.getAmount());
				}
				for (ResourceId resourceId : resourcesPluginData.getResourceIds()) {
					Long expectedAmount = expectedAmounts.get(resourceId);
					long actualAmount = resourcesDataManager.getPersonResourceLevel(resourceId, personId);
					assertEquals(expectedAmount, actualAmount);
				}
			}
			for (ResourceId resourceId : resourcesPluginData.getResourceIds()) {
				Set<ResourcePropertyId> expectedResourcePropertyIds = resourcesPluginData.getResourcePropertyIds(resourceId);
				Set<ResourcePropertyId> actualResourcePropertyIds = resourcesDataManager.getResourcePropertyIds(resourceId);
				assertEquals(expectedResourcePropertyIds, actualResourcePropertyIds);

				for (ResourcePropertyId resourcePropertyId : expectedResourcePropertyIds) {
					PropertyDefinition expectedDefinition = resourcesPluginData.getResourcePropertyDefinition(resourceId, resourcePropertyId);
					PropertyDefinition actualDefinition = resourcesDataManager.getResourcePropertyDefinition(resourceId, resourcePropertyId);
					assertEquals(expectedDefinition, actualDefinition);

					Object expectedValue = resourcesPluginData.getResourcePropertyValue(resourceId, resourcePropertyId);
					Object actualValue = resourcesDataManager.getResourcePropertyValue(resourceId, resourcePropertyId);
					assertEquals(expectedValue, actualValue);

				}
			}

		}));

		TestPluginData testPluginData = pluginBuilder.build();
		Plugin testPlugin = TestPlugin.getTestPlugin(testPluginData);

		// add the action plugin
		builder.addPlugin(testPlugin);

		// build and execute the engine
		ScenarioPlanCompletionObserver scenarioPlanCompletionObserver = new ScenarioPlanCompletionObserver();
		builder.setOutputConsumer(scenarioPlanCompletionObserver::handleOutput).build().execute();

		// show that all actions were executed
		if (!scenarioPlanCompletionObserver.allPlansExecuted()) {
			throw new ContractException(TestError.TEST_EXECUTION_FAILURE);
		}

	}

	@Test
	@UnitTestMethod(target = ResourcesDataManager.class, name = "getEventFilterForPersonResourceUpdateEvent", args = { ResourceId.class })
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
				EventFilter<PersonResourceUpdateEvent> eventFilter = resourcesDataManager.getEventFilterForPersonResourceUpdateEvent(testResourceId);
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
		Plugin testPlugin = TestPlugin.getTestPlugin(testPluginData);
		ResourcesActionSupport.testConsumers(30, 4043641365602447479L, testPlugin);

		/* precondition test: if the resource id is null */
		ResourcesActionSupport.testConsumer(30, 5107085853667531414L, (c) -> {
			ResourcesDataManager resourcesDataManager = c.getDataManager(ResourcesDataManager.class);
			ContractException contractException = assertThrows(ContractException.class, () -> resourcesDataManager.getEventFilterForPersonResourceUpdateEvent(null));
			assertEquals(ResourceError.NULL_RESOURCE_ID, contractException.getErrorType());
		});

		/* precondition test: if the resource id is unknown */
		ResourcesActionSupport.testConsumer(30, 5551635264070855342L, (c) -> {
			ResourcesDataManager resourcesDataManager = c.getDataManager(ResourcesDataManager.class);
			ContractException contractException = assertThrows(ContractException.class, () -> resourcesDataManager.getEventFilterForPersonResourceUpdateEvent(TestResourceId.getUnknownResourceId()));
			assertEquals(ResourceError.UNKNOWN_RESOURCE_ID, contractException.getErrorType());
		});

	}

	@Test
	@UnitTestMethod(target = ResourcesDataManager.class, name = "getEventFilterForPersonResourceUpdateEvent", args = { ResourceId.class, PersonId.class })
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
					EventFilter<PersonResourceUpdateEvent> eventFilter = resourcesDataManager.getEventFilterForPersonResourceUpdateEvent(testResourceId, personId);
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
		Plugin testPlugin = TestPlugin.getTestPlugin(testPluginData);
		ResourcesActionSupport.testConsumers(30, 3776094770483573425L, testPlugin);

		/* precondition test: if the resource id is null */
		ResourcesActionSupport.testConsumer(30, 8909938597230752836L, (c) -> {
			ResourcesDataManager resourcesDataManager = c.getDataManager(ResourcesDataManager.class);
			ContractException contractException = assertThrows(ContractException.class, () -> resourcesDataManager.getEventFilterForPersonResourceUpdateEvent(null, new PersonId(0)));
			assertEquals(ResourceError.NULL_RESOURCE_ID, contractException.getErrorType());
		});

		/* precondition test: if the resource id is unknown */
		ResourcesActionSupport.testConsumer(30, 4146350189128134907L, (c) -> {
			ResourcesDataManager resourcesDataManager = c.getDataManager(ResourcesDataManager.class);
			ContractException contractException = assertThrows(ContractException.class,
					() -> resourcesDataManager.getEventFilterForPersonResourceUpdateEvent(TestResourceId.getUnknownResourceId(), new PersonId(0)));
			assertEquals(ResourceError.UNKNOWN_RESOURCE_ID, contractException.getErrorType());
		});

		/* precondition test: if the person id is null */
		ResourcesActionSupport.testConsumer(30, 8356399638914398643L, (c) -> {
			ResourcesDataManager resourcesDataManager = c.getDataManager(ResourcesDataManager.class);
			PersonId nullPersonId = null;
			ContractException contractException = assertThrows(ContractException.class, () -> resourcesDataManager.getEventFilterForPersonResourceUpdateEvent(TestResourceId.RESOURCE_1, nullPersonId));
			assertEquals(PersonError.NULL_PERSON_ID, contractException.getErrorType());
		});

		/* precondition test: if the person id is unknown */
		ResourcesActionSupport.testConsumer(30, 3890936504108305392L, (c) -> {
			ResourcesDataManager resourcesDataManager = c.getDataManager(ResourcesDataManager.class);
			PersonId unknownPersonId = new PersonId(100000);
			ContractException contractException = assertThrows(ContractException.class,
					() -> resourcesDataManager.getEventFilterForPersonResourceUpdateEvent(TestResourceId.RESOURCE_1, unknownPersonId));
			assertEquals(PersonError.UNKNOWN_PERSON_ID, contractException.getErrorType());
		});

	}

	@Test
	@UnitTestMethod(target = ResourcesDataManager.class, name = "getEventFilterForPersonResourceUpdateEvent", args = { ResourceId.class, RegionId.class })
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
					EventFilter<PersonResourceUpdateEvent> eventFilter = resourcesDataManager.getEventFilterForPersonResourceUpdateEvent(testResourceId, regionId);
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
		Plugin testPlugin = TestPlugin.getTestPlugin(testPluginData);
		ResourcesActionSupport.testConsumers(30, 1727074366899837142L, testPlugin);

		/* precondition test: if the resource id is null */
		ResourcesActionSupport.testConsumer(30, 7693743966390586978L, (c) -> {
			ResourcesDataManager resourcesDataManager = c.getDataManager(ResourcesDataManager.class);
			ContractException contractException = assertThrows(ContractException.class, () -> resourcesDataManager.getEventFilterForPersonResourceUpdateEvent(null, new PersonId(0)));
			assertEquals(ResourceError.NULL_RESOURCE_ID, contractException.getErrorType());
		});

		/* precondition test: if the resource id is unknown */
		ResourcesActionSupport.testConsumer(30, 693173450564289263L, (c) -> {
			ResourcesDataManager resourcesDataManager = c.getDataManager(ResourcesDataManager.class);
			ContractException contractException = assertThrows(ContractException.class,
					() -> resourcesDataManager.getEventFilterForPersonResourceUpdateEvent(TestResourceId.getUnknownResourceId(), new PersonId(0)));
			assertEquals(ResourceError.UNKNOWN_RESOURCE_ID, contractException.getErrorType());
		});

		/* precondition test: if the region id is null */
		ResourcesActionSupport.testConsumer(30, 9201364062172125070L, (c) -> {
			ResourcesDataManager resourcesDataManager = c.getDataManager(ResourcesDataManager.class);
			RegionId nullRegionId = null;
			ContractException contractException = assertThrows(ContractException.class, () -> resourcesDataManager.getEventFilterForPersonResourceUpdateEvent(TestResourceId.RESOURCE_1, nullRegionId));
			assertEquals(RegionError.NULL_REGION_ID, contractException.getErrorType());
		});

		/* precondition test: if the region id is unknown */
		ResourcesActionSupport.testConsumer(30, 5569918148190340272L, (c) -> {
			ResourcesDataManager resourcesDataManager = c.getDataManager(ResourcesDataManager.class);
			RegionId unknownRegionId = TestRegionId.getUnknownRegionId();
			ContractException contractException = assertThrows(ContractException.class,
					() -> resourcesDataManager.getEventFilterForPersonResourceUpdateEvent(TestResourceId.RESOURCE_1, unknownRegionId));
			assertEquals(RegionError.UNKNOWN_REGION_ID, contractException.getErrorType());
		});

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

			EventFilter<PersonResourceUpdateEvent> eventFilter = resourcesDataManager.getEventFilterForPersonResourceUpdateEvent();
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
		Plugin testPlugin = TestPlugin.getTestPlugin(testPluginData);
		ResourcesActionSupport.testConsumers(30, 1345117947886682832L, testPlugin);

	}

	@Test
	@UnitTestMethod(target = ResourcesDataManager.class, name = "getEventFilterForRegionResourceUpdateEvent", args = { ResourceId.class })
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
				EventFilter<RegionResourceUpdateEvent> eventFilter = resourcesDataManager.getEventFilterForRegionResourceUpdateEvent(testResourceId);
				c.subscribe(eventFilter, (c2, e) -> {
					actualObservations.add(new MultiKey(e.regionId(), e.resourceId(), e.previousResourceLevel(), e.currentResourceLevel()));
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
		Plugin testPlugin = TestPlugin.getTestPlugin(testPluginData);
		ResourcesActionSupport.testConsumers(0, 2870952108296201475L, testPlugin);

		/* precondition test: if the resource id is null */
		ResourcesActionSupport.testConsumer(0, 9101711257710159283L, (c) -> {
			ResourcesDataManager resourcesDataManager = c.getDataManager(ResourcesDataManager.class);
			ResourceId nullResourceId = null;
			ContractException contractException = assertThrows(ContractException.class, () -> resourcesDataManager.getEventFilterForRegionResourceUpdateEvent(nullResourceId));
			assertEquals(ResourceError.NULL_RESOURCE_ID, contractException.getErrorType());
		});

		/* precondition test: if the resource id is unknown */
		ResourcesActionSupport.testConsumer(0, 4216397684435821705L, (c) -> {
			ResourcesDataManager resourcesDataManager = c.getDataManager(ResourcesDataManager.class);
			ResourceId unknownResourceId = TestResourceId.getUnknownResourceId();
			ContractException contractException = assertThrows(ContractException.class, () -> resourcesDataManager.getEventFilterForRegionResourceUpdateEvent(unknownResourceId));
			assertEquals(ResourceError.UNKNOWN_RESOURCE_ID, contractException.getErrorType());
		});
	}

	@Test
	@UnitTestMethod(target = ResourcesDataManager.class, name = "getEventFilterForRegionResourceUpdateEvent", args = { ResourceId.class, RegionId.class })
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
				EventFilter<RegionResourceUpdateEvent> eventFilter = resourcesDataManager.getEventFilterForRegionResourceUpdateEvent(resourceId, regionId);
				c.subscribe(eventFilter, (c2, e) -> {
					actualObservations.add(new MultiKey(e.regionId(), e.resourceId(), e.previousResourceLevel(), e.currentResourceLevel()));
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
		Plugin testPlugin = TestPlugin.getTestPlugin(testPluginData);
		ResourcesActionSupport.testConsumers(0, 9022862258230350395L, testPlugin);

		/* precondition test: if the resource id is null */
		ResourcesActionSupport.testConsumer(0, 217976606974469406L, (c) -> {
			ResourcesDataManager resourcesDataManager = c.getDataManager(ResourcesDataManager.class);
			ResourceId nullResourceId = null;
			ContractException contractException = assertThrows(ContractException.class, () -> resourcesDataManager.getEventFilterForRegionResourceUpdateEvent(nullResourceId));
			assertEquals(ResourceError.NULL_RESOURCE_ID, contractException.getErrorType());
		});

		/* precondition test: if the resource id is unknown */
		ResourcesActionSupport.testConsumer(0, 8125399461811894989L, (c) -> {
			ResourcesDataManager resourcesDataManager = c.getDataManager(ResourcesDataManager.class);
			ResourceId unknownResourceId = TestResourceId.getUnknownResourceId();
			ContractException contractException = assertThrows(ContractException.class, () -> resourcesDataManager.getEventFilterForRegionResourceUpdateEvent(unknownResourceId));
			assertEquals(ResourceError.UNKNOWN_RESOURCE_ID, contractException.getErrorType());
		});

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

			EventFilter<RegionResourceUpdateEvent> eventFilter = resourcesDataManager.getEventFilterForRegionResourceUpdateEvent();
			c.subscribe(eventFilter, (c2, e) -> {
				actualObservations.add(new MultiKey(e.regionId(), e.resourceId(), e.previousResourceLevel(), e.currentResourceLevel()));
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
		Plugin testPlugin = TestPlugin.getTestPlugin(testPluginData);
		ResourcesActionSupport.testConsumers(0, 4130610902285408287L, testPlugin);

	}

	@Test
	@UnitTestMethod(target = ResourcesDataManager.class, name = "getEventFilterForResourcePropertyUpdateEvent", args = { ResourceId.class, ResourcePropertyId.class })
	public void testGetEventFilterForResourcePropertyUpdateEvent_Resource_Property() {
		TestPluginData.Builder pluginBuilder = TestPluginData.builder();

		Set<MultiKey> expectedObservations = new LinkedHashSet<>();
		Set<MultiKey> actualObservations = new LinkedHashSet<>();

		Set<Pair<TestResourceId, TestResourcePropertyId>> selectedResourcePropertyPairs = new LinkedHashSet<>();
		selectedResourcePropertyPairs.add(new Pair<>(TestResourceId.RESOURCE_1, TestResourcePropertyId.ResourceProperty_1_3_DOUBLE_MUTABLE));
		selectedResourcePropertyPairs.add(new Pair<>(TestResourceId.RESOURCE_1, TestResourcePropertyId.ResourceProperty_1_2_INTEGER_MUTABLE));
		selectedResourcePropertyPairs.add(new Pair<>(TestResourceId.RESOURCE_2, TestResourcePropertyId.ResourceProperty_2_2_INTEGER_MUTABLE));
		selectedResourcePropertyPairs.add(new Pair<>(TestResourceId.RESOURCE_3, TestResourcePropertyId.ResourceProperty_3_2_STRING_MUTABLE));
		selectedResourcePropertyPairs.add(new Pair<>(TestResourceId.RESOURCE_3, TestResourcePropertyId.ResourceProperty_3_1_BOOLEAN_MUTABLE));
		selectedResourcePropertyPairs.add(new Pair<>(TestResourceId.RESOURCE_5, TestResourcePropertyId.ResourceProperty_5_1_DOUBLE_IMMUTABLE));

		// Have an actor observe the selected resource property changes

		pluginBuilder.addTestActorPlan("observer", new TestActorPlan(0, (c) -> {
			ResourcesDataManager resourcesDataManager = c.getDataManager(ResourcesDataManager.class);
			for (Pair<TestResourceId, TestResourcePropertyId> pair : selectedResourcePropertyPairs) {
				TestResourceId testResourceId = pair.getFirst();
				TestResourcePropertyId testResourcePropertyId = pair.getSecond();
				EventFilter<ResourcePropertyUpdateEvent> eventFilter = resourcesDataManager.getEventFilterForResourcePropertyUpdateEvent(testResourceId, testResourcePropertyId);

				c.subscribe(eventFilter, (c2, e) -> {
					actualObservations.add(new MultiKey(c.getTime(), e.resourceId(), e.resourcePropertyId(), e.previousPropertyValue(), e.currentPropertyValue()));
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
			TestResourcePropertyId testResourcePropertyId = TestResourcePropertyId.getRandomResourcePropertyId(testResourceId, randomGenerator);

			for (int i = 1; i < comparisonDay; i++) {
				c.addPlan((c2) -> {
					PropertyDefinition propertyDefinition = resourcesDataManager.getResourcePropertyDefinition(testResourceId, testResourcePropertyId);
					if (propertyDefinition.propertyValuesAreMutable()) {
						// update the property value
						Object resourcePropertyValue = resourcesDataManager.getResourcePropertyValue(testResourceId, testResourcePropertyId);
						Object expectedValue = testResourcePropertyId.getRandomPropertyValue(randomGenerator);
						resourcesDataManager.setResourcePropertyValue(testResourceId, testResourcePropertyId, expectedValue);

						Pair<TestResourceId, TestResourcePropertyId> pair = new Pair<>(testResourceId, testResourcePropertyId);
						if (selectedResourcePropertyPairs.contains(pair)) {
							expectedObservations.add(new MultiKey(c2.getTime(), testResourceId, testResourcePropertyId, resourcePropertyValue, expectedValue));
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
		Plugin testPlugin = TestPlugin.getTestPlugin(testPluginData);
		ResourcesActionSupport.testConsumers(0, 4039871222190675923L, testPlugin);

		/* precondition test: if the resource id is null */
		ResourcesActionSupport.testConsumer(0, 7664472869248061620L, (c) -> {
			ResourceId resourceId = null;
			ResourcePropertyId resourcePropertyId = TestResourcePropertyId.ResourceProperty_1_2_INTEGER_MUTABLE;
			ResourcesDataManager resourcesDataManager = c.getDataManager(ResourcesDataManager.class);
			ContractException contractException = assertThrows(ContractException.class, () -> resourcesDataManager.getEventFilterForResourcePropertyUpdateEvent(resourceId, resourcePropertyId));
			assertEquals(ResourceError.NULL_RESOURCE_ID, contractException.getErrorType());
		});

		/* precondition test: if the resource id is unknown */
		ResourcesActionSupport.testConsumer(0, 2475328515664171695L, (c) -> {
			ResourceId resourceId = TestResourceId.getUnknownResourceId();
			ResourcePropertyId resourcePropertyId = TestResourcePropertyId.ResourceProperty_1_2_INTEGER_MUTABLE;
			ResourcesDataManager resourcesDataManager = c.getDataManager(ResourcesDataManager.class);
			ContractException contractException = assertThrows(ContractException.class, () -> resourcesDataManager.getEventFilterForResourcePropertyUpdateEvent(resourceId, resourcePropertyId));
			assertEquals(ResourceError.UNKNOWN_RESOURCE_ID, contractException.getErrorType());
		});

		/* precondition test: if the resource property id is null */
		ResourcesActionSupport.testConsumer(0, 7416000716392694948L, (c) -> {
			ResourceId resourceId = TestResourceId.RESOURCE_1;
			ResourcePropertyId resourcePropertyId = null;
			ResourcesDataManager resourcesDataManager = c.getDataManager(ResourcesDataManager.class);
			ContractException contractException = assertThrows(ContractException.class, () -> resourcesDataManager.getEventFilterForResourcePropertyUpdateEvent(resourceId, resourcePropertyId));
			assertEquals(PropertyError.NULL_PROPERTY_ID, contractException.getErrorType());
		});

		/* precondition test: if the resource property id is unknown */
		ResourcesActionSupport.testConsumer(0, 697790634696788239L, (c) -> {
			ResourceId resourceId = TestResourceId.RESOURCE_1;
			ResourcePropertyId resourcePropertyId = TestResourcePropertyId.getUnknownResourcePropertyId();
			ResourcesDataManager resourcesDataManager = c.getDataManager(ResourcesDataManager.class);
			ContractException contractException = assertThrows(ContractException.class, () -> resourcesDataManager.getEventFilterForResourcePropertyUpdateEvent(resourceId, resourcePropertyId));
			assertEquals(PropertyError.UNKNOWN_PROPERTY_ID, contractException.getErrorType());
		});

		/*
		 * precondition test: if the resource property id is unknown -- in this
		 * case it is linked to a different resource
		 */
		ResourcesActionSupport.testConsumer(0, 107265130769422979L, (c) -> {
			ResourceId resourceId = TestResourceId.RESOURCE_1;
			ResourcePropertyId resourcePropertyId = TestResourcePropertyId.ResourceProperty_2_1_BOOLEAN_MUTABLE;
			ResourcesDataManager resourcesDataManager = c.getDataManager(ResourcesDataManager.class);
			ContractException contractException = assertThrows(ContractException.class, () -> resourcesDataManager.getEventFilterForResourcePropertyUpdateEvent(resourceId, resourcePropertyId));
			assertEquals(PropertyError.UNKNOWN_PROPERTY_ID, contractException.getErrorType());
		});

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

			EventFilter<ResourcePropertyUpdateEvent> eventFilter = resourcesDataManager.getEventFilterForResourcePropertyUpdateEvent();

			c.subscribe(eventFilter, (c2, e) -> {
				actualObservations.add(new MultiKey(c.getTime(), e.resourceId(), e.resourcePropertyId(), e.previousPropertyValue(), e.currentPropertyValue()));
			});

		}));

		int comparisonDay = 100;

		// Have an actor assign resource properties

		pluginBuilder.addTestActorPlan("actor", new TestActorPlan(1, (c) -> {

			StochasticsDataManager stochasticsDataManager = c.getDataManager(StochasticsDataManager.class);
			RandomGenerator randomGenerator = stochasticsDataManager.getRandomGenerator();
			ResourcesDataManager resourcesDataManager = c.getDataManager(ResourcesDataManager.class);

			TestResourceId testResourceId = TestResourceId.getRandomResourceId(randomGenerator);
			TestResourcePropertyId testResourcePropertyId = TestResourcePropertyId.getRandomResourcePropertyId(testResourceId, randomGenerator);

			for (int i = 1; i < comparisonDay; i++) {
				c.addPlan((c2) -> {
					PropertyDefinition propertyDefinition = resourcesDataManager.getResourcePropertyDefinition(testResourceId, testResourcePropertyId);
					if (propertyDefinition.propertyValuesAreMutable()) {
						// update the property value
						Object resourcePropertyValue = resourcesDataManager.getResourcePropertyValue(testResourceId, testResourcePropertyId);
						Object expectedValue = testResourcePropertyId.getRandomPropertyValue(randomGenerator);
						resourcesDataManager.setResourcePropertyValue(testResourceId, testResourcePropertyId, expectedValue);
						expectedObservations.add(new MultiKey(c2.getTime(), testResourceId, testResourcePropertyId, resourcePropertyValue, expectedValue));
					}
				}, i);
			}

		}));

		// Have the observer show that the observations were properly generated
		pluginBuilder.addTestActorPlan("observer", new TestActorPlan(comparisonDay, (c) -> {
			assertEquals(expectedObservations, actualObservations);
		}));

		TestPluginData testPluginData = pluginBuilder.build();
		Plugin testPlugin = TestPlugin.getTestPlugin(testPluginData);
		ResourcesActionSupport.testConsumers(0, 4428711217570070234L, testPlugin);

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
			EventFilter<ResourceIdAdditionEvent> eventFilter = resourcesDataManager.getEventFilterForResourceIdAdditionEvent();
			c.subscribe(eventFilter, (c2, e) -> {
				MultiKey multiKey = new MultiKey(c2.getTime(), e.resourceId(), e.timeTrackingPolicy());
				actualObservations.add(multiKey);
			});

		}));

		pluginBuilder.addTestActorPlan("actor", new TestActorPlan(1, (c) -> {
			ResourcesDataManager resourcesDataManager = c.getDataManager(ResourcesDataManager.class);
			TimeTrackingPolicy timeTrackingPolicy = TimeTrackingPolicy.DO_NOT_TRACK_TIME;
			assertFalse(resourcesDataManager.resourceIdExists(newResourceId1));
			resourcesDataManager.addResourceId(newResourceId1, timeTrackingPolicy);
			MultiKey multiKey = new MultiKey(c.getTime(), newResourceId1, TimeTrackingPolicy.DO_NOT_TRACK_TIME);
			expectedObservations.add(multiKey);
		}));

		pluginBuilder.addTestActorPlan("actor", new TestActorPlan(2, (c) -> {
			ResourcesDataManager resourcesDataManager = c.getDataManager(ResourcesDataManager.class);
			TimeTrackingPolicy timeTrackingPolicy = TimeTrackingPolicy.TRACK_TIME;
			assertFalse(resourcesDataManager.resourceIdExists(newResourceId2));
			resourcesDataManager.addResourceId(newResourceId2, timeTrackingPolicy);
			MultiKey multiKey = new MultiKey(c.getTime(), newResourceId2, TimeTrackingPolicy.TRACK_TIME);
			expectedObservations.add(multiKey);
		}));

		pluginBuilder.addTestActorPlan("observer", new TestActorPlan(3, (c) -> {
			assertEquals(expectedObservations, actualObservations);
		}));

		TestPluginData testPluginData = pluginBuilder.build();
		Plugin testPlugin = TestPlugin.getTestPlugin(testPluginData);
		ResourcesActionSupport.testConsumers(5, 6169797168816977272L, testPlugin);
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
			EventFilter<ResourcePropertyDefinitionEvent> eventFilter = resourcesDataManager.getEventFilterForResourcePropertyDefinitionEvent();
			c.subscribe(eventFilter, (c2, e) -> {
				MultiKey multiKey = new MultiKey(c2.getTime(), e.resourceId(), e.resourcePropertyId());
				actualObservations.add(multiKey);
			});
		}));

		// have an actor define a new resource property
		pluginBuilder.addTestActorPlan("actor", new TestActorPlan(1, (c) -> {
			ResourcesDataManager resourcesDataManager = c.getDataManager(ResourcesDataManager.class);
			ResourcePropertyId newResourcePropertyId = TestResourcePropertyId.getUnknownResourcePropertyId();
			PropertyDefinition propertyDefinition = PropertyDefinition.builder().setType(Double.class).setDefaultValue(34.6).build();
			ResourcePropertyInitialization resourcePropertyInitialization = //
					ResourcePropertyInitialization	.builder()//
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
			PropertyDefinition propertyDefinition = PropertyDefinition.builder().setType(String.class).setDefaultValue("default").build();

			ResourcePropertyInitialization resourcePropertyInitialization = //
					ResourcePropertyInitialization	.builder()//
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
			assertTrue(expectedObservations.size() > 0);
			assertEquals(expectedObservations, actualObservations);
		}));

		TestPluginData testPluginData = pluginBuilder.build();
		Plugin testPlugin = TestPlugin.getTestPlugin(testPluginData);
		ResourcesActionSupport.testConsumers(5, 1942435631952524244L, testPlugin);

	}
}
