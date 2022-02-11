package plugins.resources.datacontainers;

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
import org.junit.jupiter.api.Test;

import nucleus.Context;
import nucleus.DataView;
import nucleus.NucleusError;
import nucleus.ResolverContext;
import nucleus.testsupport.actionplugin.ActionPlugin;
import nucleus.testsupport.actionplugin.AgentActionPlan;
import plugins.people.datacontainers.PersonDataView;
import plugins.people.support.PersonId;
import plugins.properties.support.PropertyDefinition;
import plugins.properties.support.TimeTrackingPolicy;
import plugins.regions.datacontainers.RegionDataView;
import plugins.regions.support.RegionId;
import plugins.regions.testsupport.TestRegionId;
import plugins.resources.initialdata.ResourceInitialData;
import plugins.resources.support.ResourceError;
import plugins.resources.support.ResourceId;
import plugins.resources.support.ResourcePropertyId;
import plugins.resources.testsupport.ResourcesActionSupport;
import plugins.resources.testsupport.TestResourceId;
import plugins.resources.testsupport.TestResourcePropertyId;
import plugins.stochastics.datacontainers.StochasticsDataView;
import util.ContractException;
import util.MultiKey;
import util.MutableDouble;
import util.annotations.UnitTest;
import util.annotations.UnitTestConstructor;
import util.annotations.UnitTestMethod;

@UnitTest(target = ResourceDataManager.class)
public final class AT_ResourceDataManager {
	// Returns a copy of the ResourceDataManger that is owned by the plugin
	private ResourceDataManager getResourceDataManager(Context context) {

		ResourceDataView resourceDataView = context.getDataView(ResourceDataView.class).get();
		RegionDataView regionDataView = context.getDataView(RegionDataView.class).get();
		PersonDataView personDataView = context.getDataView(PersonDataView.class).get();

		ResourceDataManager result = new ResourceDataManager(context);
		Set<ResourceId> resourceIds = resourceDataView.getResourceIds();
		for (ResourceId resourceId : resourceIds) {
			result.addResource(resourceId, resourceDataView.getPersonResourceTimeTrackingPolicy(resourceId));
			Set<ResourcePropertyId> resourcePropertyIds = resourceDataView.getResourcePropertyIds(resourceId);
			for (ResourcePropertyId resourcePropertyId : resourcePropertyIds) {
				PropertyDefinition resourcePropertyDefinition = resourceDataView.getResourcePropertyDefinition(resourceId, resourcePropertyId);
				Object propertyValue = resourceDataView.getResourcePropertyValue(resourceId, resourcePropertyId);
				result.defineResourceProperty(resourceId, resourcePropertyId, resourcePropertyDefinition, propertyValue);
			}
		}

		Set<RegionId> regionIds = regionDataView.getRegionIds();
		for (RegionId regionId : regionIds) {
			result.addRegion(regionId);
			for (ResourceId resourceId : resourceIds) {
				long regionResourceLevel = resourceDataView.getRegionResourceLevel(regionId, resourceId);
				result.incrementRegionResourceLevel(regionId, resourceId, regionResourceLevel);
			}
		}

		List<PersonId> people = personDataView.getPeople();
		for (PersonId personId : people) {
			for (ResourceId resourceId : resourceIds) {
				long personResourceLevel = resourceDataView.getPersonResourceLevel(resourceId, personId);
				result.incrementPersonResourceLevel(resourceId, personId, personResourceLevel);
			}
		}

		return result;
	}

	@Test
	@UnitTestMethod(name = "decrementPersonResourceLevel", args = { ResourceId.class, PersonId.class, long.class })
	public void testDecrementPersonResourceLevel() {
		ResourcesActionSupport.testConsumer(15, 6030004420451387389L, (c) -> {
			ResourceDataManager resourceDataManager = getResourceDataManager(c);
			PersonDataView personDataView = c.getDataView(PersonDataView.class).get();

			for (PersonId personId : personDataView.getPeople()) {
				for (TestResourceId testResourceId : TestResourceId.values()) {
					// add a bit of resource so that we can test taking it away
					long actualResourceLevel = resourceDataManager.getPersonResourceLevel(testResourceId, personId);
					long expectedResourceLevel = actualResourceLevel + 100;
					resourceDataManager.incrementPersonResourceLevel(testResourceId, personId, 100);
					actualResourceLevel = resourceDataManager.getPersonResourceLevel(testResourceId, personId);
					assertEquals(actualResourceLevel, expectedResourceLevel);

					for (int i = 0; i < 3; i++) {
						expectedResourceLevel -= 10;
						resourceDataManager.decrementPersonResourceLevel(testResourceId, personId, 10);
						actualResourceLevel = resourceDataManager.getPersonResourceLevel(testResourceId, personId);
						assertEquals(actualResourceLevel, expectedResourceLevel);
					}
				}
			}

			// precondition tests

			// if the resource id is null
			assertThrows(RuntimeException.class, () -> resourceDataManager.decrementPersonResourceLevel(null, new PersonId(0), 10));

			// if the resource id is unknown
			assertThrows(RuntimeException.class, () -> resourceDataManager.decrementPersonResourceLevel(TestResourceId.getUnknownResourceId(), new PersonId(0), 10));

			// if the person id is null
			assertThrows(RuntimeException.class, () -> resourceDataManager.decrementPersonResourceLevel(TestResourceId.RESOURCE_1, null, 10));

			// if the person id has a negative value
			assertThrows(RuntimeException.class, () -> resourceDataManager.decrementPersonResourceLevel(TestResourceId.RESOURCE_1, new PersonId(-1), 10));

			// if the amount causes an overflow
			resourceDataManager.decrementPersonResourceLevel(TestResourceId.RESOURCE_1, new PersonId(0), Long.MAX_VALUE);
			assertThrows(RuntimeException.class, () -> resourceDataManager.decrementPersonResourceLevel(TestResourceId.RESOURCE_1, new PersonId(0), Long.MAX_VALUE));

		});

	}

	@Test
	@UnitTestMethod(name = "", args = { RegionId.class, ResourceId.class })
	public void testDecrementRegionResourceLevel() {

		ResourcesActionSupport.testConsumer(15, 2750917698199588250L, (c) -> {
			ResourceDataManager resourceDataManager = getResourceDataManager(c);
			RegionDataView regionDataView = c.getDataView(RegionDataView.class).get();

			for (RegionId regionId : regionDataView.getRegionIds()) {
				for (TestResourceId testResourceId : TestResourceId.values()) {
					// add a bit of resource so that we can test taking it away
					long actualResourceLevel = resourceDataManager.getRegionResourceLevel(regionId, testResourceId);
					long expectedResourceLevel = actualResourceLevel + 100;
					resourceDataManager.incrementRegionResourceLevel(regionId, testResourceId, 100);
					actualResourceLevel = resourceDataManager.getRegionResourceLevel(regionId, testResourceId);
					assertEquals(actualResourceLevel, expectedResourceLevel);

					// show that decrementing the value results in the correct
					// balance
					for (int i = 0; i < 3; i++) {
						expectedResourceLevel -= 10;
						resourceDataManager.decrementRegionResourceLevel(regionId, testResourceId, 10);
						actualResourceLevel = resourceDataManager.getRegionResourceLevel(regionId, testResourceId);
						assertEquals(actualResourceLevel, expectedResourceLevel);
					}
				}
			}

			// precondition tests

			// if the resource id is null
			assertThrows(RuntimeException.class, () -> resourceDataManager.decrementRegionResourceLevel(TestRegionId.REGION_2, null, 10));

			// if the resource id is unknown
			assertThrows(RuntimeException.class, () -> resourceDataManager.decrementRegionResourceLevel(TestRegionId.REGION_2, TestResourceId.getUnknownResourceId(), 10));

			// if the region id is null
			assertThrows(RuntimeException.class, () -> resourceDataManager.decrementRegionResourceLevel(null, TestResourceId.RESOURCE_1, 10));

			// if the amount is negative
			assertThrows(RuntimeException.class, () -> resourceDataManager.decrementRegionResourceLevel(TestRegionId.REGION_2, TestResourceId.RESOURCE_3, -1L));

			// if the amount is negative
			assertThrows(RuntimeException.class, () -> resourceDataManager.decrementRegionResourceLevel(TestRegionId.REGION_2, TestResourceId.RESOURCE_3, 1000L));

		});
	}

	@Test
	@UnitTestMethod(name = "dropPerson", args = { PersonId.class })
	public void testDropPerson() {

		ResourcesActionSupport.testConsumer(5, 2305198136601648340L, (c) -> {
			ResourceDataManager resourceDataManager = getResourceDataManager(c);
			PersonDataView personDataView = c.getDataView(PersonDataView.class).get();
			ResourceDataView resourceDataView = c.getDataView(ResourceDataView.class).get();
			Set<ResourceId> resourceIds = resourceDataView.getResourceIds();

			for (PersonId personId : personDataView.getPeople()) {
				// add a bit of each kind of resource to the person
				for (ResourceId resourceId : resourceIds) {
					resourceDataManager.incrementPersonResourceLevel(resourceId, personId, 10);
					assertEquals(10L, resourceDataManager.getPersonResourceLevel(resourceId, personId));
				}
				// drop the person
				resourceDataManager.dropPerson(personId);

				// show that the balance for each resource is now zero
				for (ResourceId resourceId : resourceIds) {
					assertEquals(0L, resourceDataManager.getPersonResourceLevel(resourceId, personId));
				}
			}

			// precondition tests

			// if the person id is null
			assertThrows(RuntimeException.class, () -> resourceDataManager.dropPerson(null));

			// if the person id contains a negative value
			assertThrows(RuntimeException.class, () -> resourceDataManager.dropPerson(new PersonId(-1)));

		});

	}

	@Test
	@UnitTestMethod(name = "getPeopleWithoutResource", args = { ResourceId.class })
	public void testGetPeopleWithoutResource() {

		ResourcesActionSupport.testConsumer(100, 5761923077365827764L, (c) -> {
			ResourceDataManager resourceDataManager = getResourceDataManager(c);
			StochasticsDataView stochasticsDataView = c.getDataView(StochasticsDataView.class).get();
			RandomGenerator randomGenerator = stochasticsDataView.getRandomGenerator();
			PersonDataView personDataView = c.getDataView(PersonDataView.class).get();
			Set<PersonId> expectedPeople = new LinkedHashSet<>();
			// give about half of the people the resource
			for (PersonId personId : personDataView.getPeople()) {
				if (randomGenerator.nextBoolean()) {
					resourceDataManager.incrementPersonResourceLevel(TestResourceId.RESOURCE_5, personId, 5);
				} else {
					expectedPeople.add(personId);
				}
			}
			// show that those who did not get the resource are returned
			List<PersonId> actualPeople = resourceDataManager.getPeopleWithoutResource(TestResourceId.RESOURCE_5);
			assertEquals(expectedPeople.size(), actualPeople.size());
			assertEquals(expectedPeople, new LinkedHashSet<>(actualPeople));

			// precondition tests

			// if the resource id is null
			assertThrows(RuntimeException.class, () -> resourceDataManager.getPeopleWithoutResource(null));

			// if the resource id is unknown
			assertThrows(RuntimeException.class, () -> resourceDataManager.getPeopleWithoutResource(TestResourceId.getUnknownResourceId()));
		});
	}

	@Test
	@UnitTestMethod(name = "getPeopleWithResource", args = { ResourceId.class })
	public void testGetPeopleWithResource() {

		ResourcesActionSupport.testConsumer(100, 8180022601243927809L, (c) -> {
			ResourceDataManager resourceDataManager = getResourceDataManager(c);
			StochasticsDataView stochasticsDataView = c.getDataView(StochasticsDataView.class).get();
			RandomGenerator randomGenerator = stochasticsDataView.getRandomGenerator();
			PersonDataView personDataView = c.getDataView(PersonDataView.class).get();
			Set<PersonId> expectedPeople = new LinkedHashSet<>();
			// give about half of the people the resource
			for (PersonId personId : personDataView.getPeople()) {
				if (randomGenerator.nextBoolean()) {
					resourceDataManager.incrementPersonResourceLevel(TestResourceId.RESOURCE_5, personId, 5);
					expectedPeople.add(personId);
				}
			}
			// show that those who did not get the resource are returned
			List<PersonId> actualPeople = resourceDataManager.getPeopleWithResource(TestResourceId.RESOURCE_5);
			assertEquals(expectedPeople.size(), actualPeople.size());
			assertEquals(expectedPeople, new LinkedHashSet<>(actualPeople));

			// precondition tests

			// if the resource id is null
			assertThrows(RuntimeException.class, () -> resourceDataManager.getPeopleWithoutResource(null));

			// if the resource id is unknown
			assertThrows(RuntimeException.class, () -> resourceDataManager.getPeopleWithoutResource(TestResourceId.getUnknownResourceId()));
		});
	}

	@Test
	@UnitTestMethod(name = "getPersonResourceLevel", args = { ResourceId.class, PersonId.class })
	public void testGetPersonResourceLevel() {

		ResourcesActionSupport.testConsumer(20, 7415837862164724883L, (c) -> {
			ResourceDataManager resourceDataManager = getResourceDataManager(c);
			StochasticsDataView stochasticsDataView = c.getDataView(StochasticsDataView.class).get();
			RandomGenerator randomGenerator = stochasticsDataView.getRandomGenerator();
			PersonDataView personDataView = c.getDataView(PersonDataView.class).get();

			List<PersonId> people = personDataView.getPeople();

			// give random amounts of resource to random people
			for (int i = 0; i < 1000; i++) {
				PersonId personId = people.get(randomGenerator.nextInt(people.size()));
				ResourceId resourceId = TestResourceId.getRandomResourceId(randomGenerator);
				int amount = randomGenerator.nextInt(5);
				long expectedLevel = resourceDataManager.getPersonResourceLevel(resourceId, personId);
				expectedLevel += amount;
				resourceDataManager.incrementPersonResourceLevel(resourceId, personId, amount);
				long actualLevel = resourceDataManager.getPersonResourceLevel(resourceId, personId);
				assertEquals(expectedLevel, actualLevel);
			}

			// precondition tests

			// if the resource id is null
			assertThrows(RuntimeException.class, () -> resourceDataManager.getPersonResourceLevel(null, new PersonId(0)));

			// if the resource id is unknown
			assertThrows(RuntimeException.class, () -> resourceDataManager.getPersonResourceLevel(TestResourceId.getUnknownResourceId(), new PersonId(0)));

			// if the person id null
			assertThrows(RuntimeException.class, () -> resourceDataManager.getPersonResourceLevel(TestResourceId.RESOURCE_1, null));

			// if the person id has a negative value
			assertThrows(RuntimeException.class, () -> resourceDataManager.getPersonResourceLevel(TestResourceId.RESOURCE_1, new PersonId(-1)));

		});

	}

	private static class LocalDataView implements DataView {
		ResourceDataManager resourceDataManager;
	}

	@Test
	@UnitTestMethod(name = "getPersonResourceTime", args = { ResourceId.class, PersonId.class })
	public void testGetPersonResourceTime() {

		ActionPlugin.Builder pluginBuilder = ActionPlugin.builder();

		Map<MultiKey, MutableDouble> expectedTimes = new LinkedHashMap<>();

		pluginBuilder.addDataView(new LocalDataView());

		pluginBuilder.addAgent("agent");
		pluginBuilder.addAgentActionPlan("agent", new AgentActionPlan(0, (c) -> {
			// establish data views
			LocalDataView localDataView = c.getDataView(LocalDataView.class).get();
			localDataView.resourceDataManager = getResourceDataManager(c);
			StochasticsDataView stochasticsDataView = c.getDataView(StochasticsDataView.class).get();
			RandomGenerator randomGenerator = stochasticsDataView.getRandomGenerator();
			PersonDataView personDataView = c.getDataView(PersonDataView.class).get();

			// establish the people and resources
			Set<ResourceId> resourceIds = localDataView.resourceDataManager.getResourceIds();
			List<PersonId> people = personDataView.getPeople();

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
				TimeTrackingPolicy personResourceTimeTrackingPolicy = localDataView.resourceDataManager.getPersonResourceTimeTrackingPolicy(resourceId);
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
				localDataView.resourceDataManager.incrementPersonResourceLevel(resourceId, personId, amount);
				expectedTimes.get(new MultiKey(personId, resourceId)).setValue(c.getTime());
			}

		}));

		// make more resource updates at time 1
		pluginBuilder.addAgentActionPlan("agent", new AgentActionPlan(1, (c) -> {
			// establish data views
			LocalDataView localDataView = c.getDataView(LocalDataView.class).get();

			StochasticsDataView stochasticsDataView = c.getDataView(StochasticsDataView.class).get();
			RandomGenerator randomGenerator = stochasticsDataView.getRandomGenerator();
			PersonDataView personDataView = c.getDataView(PersonDataView.class).get();

			// establish the people and resources
			List<PersonId> people = personDataView.getPeople();

			// give random amounts of resource to random people
			for (int i = 0; i < people.size(); i++) {
				PersonId personId = people.get(randomGenerator.nextInt(people.size()));
				ResourceId resourceId = TestResourceId.getRandomResourceId(randomGenerator);
				int amount = randomGenerator.nextInt(5) + 1;
				localDataView.resourceDataManager.incrementPersonResourceLevel(resourceId, personId, amount);
				expectedTimes.get(new MultiKey(personId, resourceId)).setValue(c.getTime());
			}

		}));

		// make more resource updates at time 2
		pluginBuilder.addAgentActionPlan("agent", new AgentActionPlan(2, (c) -> {
			// establish data views
			LocalDataView localDataView = c.getDataView(LocalDataView.class).get();
			StochasticsDataView stochasticsDataView = c.getDataView(StochasticsDataView.class).get();
			RandomGenerator randomGenerator = stochasticsDataView.getRandomGenerator();
			PersonDataView personDataView = c.getDataView(PersonDataView.class).get();

			// establish the people and resources
			List<PersonId> people = personDataView.getPeople();

			// give random amounts of resource to random people

			for (int i = 0; i < people.size(); i++) {
				PersonId personId = people.get(randomGenerator.nextInt(people.size()));
				ResourceId resourceId = TestResourceId.getRandomResourceId(randomGenerator);
				int amount = randomGenerator.nextInt(5) + 1;
				localDataView.resourceDataManager.incrementPersonResourceLevel(resourceId, personId, amount);
				expectedTimes.get(new MultiKey(personId, resourceId)).setValue(c.getTime());
			}

		}));

		// test the person resource times
		pluginBuilder.addAgentActionPlan("agent", new AgentActionPlan(3, (c) -> {
			// establish data views

			LocalDataView localDataView = c.getDataView(LocalDataView.class).get();
			PersonDataView personDataView = c.getDataView(PersonDataView.class).get();

			// show that the person resource times match expectations
			int actualAssertionsCount = 0;
			for (MultiKey multiKey : expectedTimes.keySet()) {
				PersonId personId = multiKey.getKey(0);
				ResourceId resourceId = multiKey.getKey(1);
				TimeTrackingPolicy personResourceTimeTrackingPolicy = localDataView.resourceDataManager.getPersonResourceTimeTrackingPolicy(resourceId);
				if (personResourceTimeTrackingPolicy == TimeTrackingPolicy.TRACK_TIME) {
					double expectedTime = expectedTimes.get(multiKey).getValue();
					double actualTime = localDataView.resourceDataManager.getPersonResourceTime(resourceId, personId);
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
			for (ResourceId resourceId : localDataView.resourceDataManager.getResourceIds()) {
				TimeTrackingPolicy personResourceTimeTrackingPolicy = localDataView.resourceDataManager.getPersonResourceTimeTrackingPolicy(resourceId);
				if (personResourceTimeTrackingPolicy == TimeTrackingPolicy.TRACK_TIME) {
					trackedResourceCount++;
				}
			}

			int expectedAssertionsCount = personDataView.getPopulationCount() * trackedResourceCount;
			assertEquals(expectedAssertionsCount, actualAssertionsCount);
		}));

		// precondition tests
		pluginBuilder.addAgentActionPlan("agent", new AgentActionPlan(4, (c) -> {
			// establish data views

			LocalDataView localDataView = c.getDataView(LocalDataView.class).get();

			// if the assignment times for the resource are not tracked
			assertThrows(RuntimeException.class, () -> localDataView.resourceDataManager.getPersonResourceTime(TestResourceId.RESOURCE_2, new PersonId(0)));

			// if the resource id is null
			assertThrows(RuntimeException.class, () -> localDataView.resourceDataManager.getPersonResourceTime(null, new PersonId(0)));

			// if the resource id is unknown
			assertThrows(RuntimeException.class, () -> localDataView.resourceDataManager.getPersonResourceTime(TestResourceId.getUnknownResourceId(), new PersonId(0)));

			// if the person id null
			assertThrows(RuntimeException.class, () -> localDataView.resourceDataManager.getPersonResourceTime(TestResourceId.RESOURCE_1, null));

			// if the person id has a negative value
			assertThrows(RuntimeException.class, () -> localDataView.resourceDataManager.getPersonResourceTime(TestResourceId.RESOURCE_1, new PersonId(-1)));

		}));

		ResourcesActionSupport.testConsumers(30, 2136024697302760769L, pluginBuilder.build());

	}

	@Test
	@UnitTestMethod(name = "getPersonResourceTimeTrackingPolicy", args = { ResourceId.class })
	public void testGetPersonResourceTimeTrackingPolicy() {

		ResourcesActionSupport.testConsumer(5, 2467596053690516306L, (c) -> {
			ResourceDataManager resourceDataManager = getResourceDataManager(c);
			for (TestResourceId testResourceId : TestResourceId.values()) {
				TimeTrackingPolicy actualPolicy = resourceDataManager.getPersonResourceTimeTrackingPolicy(testResourceId);
				TimeTrackingPolicy expectedPolicy = testResourceId.getTimeTrackingPolicy();
				assertEquals(expectedPolicy, actualPolicy);
			}
		});

	}

	@Test
	@UnitTestMethod(name = "getRegionResourceLevel", args = { RegionId.class, ResourceId.class })
	public void testGetRegionResourceLevel() {

		ResourcesActionSupport.testConsumer(20, 6794039747965016172L, (c) -> {
			ResourceDataManager resourceDataManager = getResourceDataManager(c);
			StochasticsDataView stochasticsDataView = c.getDataView(StochasticsDataView.class).get();
			RandomGenerator randomGenerator = stochasticsDataView.getRandomGenerator();
			RegionDataView regionDataView = c.getDataView(RegionDataView.class).get();

			List<RegionId> regionIds = new ArrayList<>(regionDataView.getRegionIds());

			// give random amounts of resource to random regions
			for (int i = 0; i < 1000; i++) {
				RegionId regionId = regionIds.get(randomGenerator.nextInt(regionIds.size()));
				ResourceId resourceId = TestResourceId.getRandomResourceId(randomGenerator);
				int amount = randomGenerator.nextInt(5);
				long expectedLevel = resourceDataManager.getRegionResourceLevel(regionId, resourceId);
				expectedLevel += amount;
				resourceDataManager.incrementRegionResourceLevel(regionId, resourceId, amount);
				long actualLevel = resourceDataManager.getRegionResourceLevel(regionId, resourceId);
				assertEquals(expectedLevel, actualLevel);
			}

			// precondition tests

			// if the resource id is null
			assertThrows(RuntimeException.class, () -> resourceDataManager.getRegionResourceLevel(TestRegionId.REGION_1, null));

			// if the resource id is unknown
			assertThrows(RuntimeException.class, () -> resourceDataManager.getRegionResourceLevel(TestRegionId.REGION_1, TestResourceId.getUnknownResourceId()));

			// if the region id null
			assertThrows(RuntimeException.class, () -> resourceDataManager.getRegionResourceLevel(null, TestResourceId.RESOURCE_1));

			// if the region id is unknown
			assertThrows(RuntimeException.class, () -> resourceDataManager.getRegionResourceLevel(TestRegionId.getUnknownRegionId(), TestResourceId.RESOURCE_1));

		});
	}

	@Test
	@UnitTestMethod(name = "getRegionResourceTime", args = { RegionId.class, ResourceId.class })
	public void testGetRegionResourceTime() {

		ActionPlugin.Builder pluginBuilder = ActionPlugin.builder();

		Map<MultiKey, MutableDouble> expectedTimes = new LinkedHashMap<>();

		pluginBuilder.addDataView(new LocalDataView());

		pluginBuilder.addAgent("agent");
		pluginBuilder.addAgentActionPlan("agent", new AgentActionPlan(0, (c) -> {
			// establish data views
			LocalDataView localDataView = c.getDataView(LocalDataView.class).get();
			localDataView.resourceDataManager = getResourceDataManager(c);
			StochasticsDataView stochasticsDataView = c.getDataView(StochasticsDataView.class).get();
			RandomGenerator randomGenerator = stochasticsDataView.getRandomGenerator();
			RegionDataView regionDataView = c.getDataView(RegionDataView.class).get();

			// establish the people and resources
			Set<ResourceId> resourceIds = localDataView.resourceDataManager.getResourceIds();
			List<RegionId> regionIds = new ArrayList<>(regionDataView.getRegionIds());

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
				localDataView.resourceDataManager.incrementRegionResourceLevel(regionId, resourceId, amount);
				expectedTimes.get(new MultiKey(regionId, resourceId)).setValue(c.getTime());
			}

		}));

		// make more resource updates at time 1
		pluginBuilder.addAgentActionPlan("agent", new AgentActionPlan(1, (c) -> {
			// establish data views
			LocalDataView localDataView = c.getDataView(LocalDataView.class).get();

			StochasticsDataView stochasticsDataView = c.getDataView(StochasticsDataView.class).get();
			RandomGenerator randomGenerator = stochasticsDataView.getRandomGenerator();
			RegionDataView regionDataView = c.getDataView(RegionDataView.class).get();

			// establish the regions
			List<RegionId> regionIds = new ArrayList<>(regionDataView.getRegionIds());

			// give random amounts of resource to random regions
			for (int i = 0; i < regionIds.size(); i++) {
				RegionId regionId = regionIds.get(randomGenerator.nextInt(regionIds.size()));
				ResourceId resourceId = TestResourceId.getRandomResourceId(randomGenerator);
				int amount = randomGenerator.nextInt(5) + 1;
				localDataView.resourceDataManager.incrementRegionResourceLevel(regionId, resourceId, amount);
				expectedTimes.get(new MultiKey(regionId, resourceId)).setValue(c.getTime());
			}

		}));

		// make more resource updates at time 2
		pluginBuilder.addAgentActionPlan("agent", new AgentActionPlan(2, (c) -> {
			// establish data views
			LocalDataView localDataView = c.getDataView(LocalDataView.class).get();

			StochasticsDataView stochasticsDataView = c.getDataView(StochasticsDataView.class).get();
			RandomGenerator randomGenerator = stochasticsDataView.getRandomGenerator();
			RegionDataView regionDataView = c.getDataView(RegionDataView.class).get();

			// establish the regions
			List<RegionId> regionIds = new ArrayList<>(regionDataView.getRegionIds());

			// give random amounts of resource to random regions
			for (int i = 0; i < regionIds.size(); i++) {
				RegionId regionId = regionIds.get(randomGenerator.nextInt(regionIds.size()));
				ResourceId resourceId = TestResourceId.getRandomResourceId(randomGenerator);
				int amount = randomGenerator.nextInt(5) + 1;
				localDataView.resourceDataManager.incrementRegionResourceLevel(regionId, resourceId, amount);
				expectedTimes.get(new MultiKey(regionId, resourceId)).setValue(c.getTime());
			}

		}));

		// test the person resource times
		pluginBuilder.addAgentActionPlan("agent", new AgentActionPlan(3, (c) -> {
			// establish data views

			LocalDataView localDataView = c.getDataView(LocalDataView.class).get();
			RegionDataView regionDataView = c.getDataView(RegionDataView.class).get();

			// show that the region resource times match expectations
			int actualAssertionsCount = 0;
			for (MultiKey multiKey : expectedTimes.keySet()) {
				RegionId regionId = multiKey.getKey(0);
				ResourceId resourceId = multiKey.getKey(1);
				double expectedTime = expectedTimes.get(multiKey).getValue();
				double actualTime = localDataView.resourceDataManager.getRegionResourceTime(regionId, resourceId);
				assertEquals(expectedTime, actualTime);
				actualAssertionsCount++;
			}
			/*
			 * Show that the number of time values that were tested is equal to
			 * the size of the population times the number of resources
			 */
			int expectedAssertionsCount = regionDataView.getRegionIds().size() * localDataView.resourceDataManager.getResourceIds().size();
			assertEquals(expectedAssertionsCount, actualAssertionsCount);
		}));

		// precondition tests
		pluginBuilder.addAgentActionPlan("agent", new AgentActionPlan(4, (c) -> {
			// establish data views

			LocalDataView localDataView = c.getDataView(LocalDataView.class).get();

			// if the assignment times for the resource are not tracked
			assertThrows(RuntimeException.class, () -> localDataView.resourceDataManager.getPersonResourceTime(TestResourceId.RESOURCE_2, new PersonId(0)));

			// if the resource id is null
			assertThrows(RuntimeException.class, () -> localDataView.resourceDataManager.getPersonResourceTime(null, new PersonId(0)));

			// if the resource id is unknown
			assertThrows(RuntimeException.class, () -> localDataView.resourceDataManager.getPersonResourceTime(TestResourceId.getUnknownResourceId(), new PersonId(0)));

			// if the person id null
			assertThrows(RuntimeException.class, () -> localDataView.resourceDataManager.getPersonResourceTime(TestResourceId.RESOURCE_1, null));

			// if the person id has a negative value
			assertThrows(RuntimeException.class, () -> localDataView.resourceDataManager.getPersonResourceTime(TestResourceId.RESOURCE_1, new PersonId(-1)));

		}));

		ResourcesActionSupport.testConsumers(30, 5351535408725138492L, pluginBuilder.build());

	}

	@Test
	@UnitTestMethod(name = "getResourceIds", args = {})
	public void testGetResourceIds() {

		ResourcesActionSupport.testConsumer(5, 8064412632119101398L, (c) -> {
			ResourceDataManager resourceDataManager = getResourceDataManager(c);

			// show that the resource ids are the test resource ids
			Set<ResourceId> expectedResourceIds = new LinkedHashSet<>();
			for (TestResourceId testResourceId : TestResourceId.values()) {
				expectedResourceIds.add(testResourceId);
			}
			assertEquals(expectedResourceIds, resourceDataManager.getResourceIds());

			// add a resource and show that the new resource is included
			ResourceId resourceId = new ResourceId() {
			};
			expectedResourceIds.add(resourceId);
			resourceDataManager.addResource(resourceId, TimeTrackingPolicy.TRACK_TIME);
			assertEquals(expectedResourceIds, resourceDataManager.getResourceIds());

		});
	}

	@Test
	@UnitTestMethod(name = "getResourcePropertyDefinition", args = { ResourceId.class, ResourcePropertyId.class })
	public void testGetResourcePropertyDefinition() {

		ResourcesActionSupport.testConsumer(5, 8387235219834806147L, (c) -> {
			ResourceDataManager resourceDataManager = getResourceDataManager(c);
			// show that each of the resource property definitions from the test
			// resource property enum are present
			for (TestResourcePropertyId testResourcePropertyId : TestResourcePropertyId.values()) {
				PropertyDefinition expectedPropertyDefinition = testResourcePropertyId.getPropertyDefinition();
				PropertyDefinition actualPropertyDefinition = resourceDataManager.getResourcePropertyDefinition(testResourcePropertyId.getTestResourceId(), testResourcePropertyId);
				assertEquals(expectedPropertyDefinition, actualPropertyDefinition);
			}
		});
	}

	@Test
	@UnitTestMethod(name = "getResourcePropertyIds", args = { ResourceId.class })
	public void testGetResourcePropertyIds() {

		ResourcesActionSupport.testConsumer(5, 8015209971315333865L, (c) -> {
			ResourceDataManager resourceDataManager = getResourceDataManager(c);
			// show that the resource property ids are the test resource
			// property ids
			for (TestResourceId testResourceId : TestResourceId.values()) {
				Set<TestResourcePropertyId> expectedPropertyIds = TestResourcePropertyId.getTestResourcePropertyIds(testResourceId);
				Set<ResourcePropertyId> actualPropertyIds = resourceDataManager.getResourcePropertyIds(testResourceId);
				assertEquals(expectedPropertyIds, actualPropertyIds);
			}

			// precondition tests

			// if the resource id is null
			assertThrows(RuntimeException.class, () -> resourceDataManager.getResourcePropertyIds(null));

			// if the resource id unknown</li>
			assertThrows(RuntimeException.class, () -> resourceDataManager.getResourcePropertyIds(TestResourceId.getUnknownResourceId()));
		});
	}

	@Test
	@UnitTestMethod(name = "getResourcePropertyTime", args = { ResourceId.class, ResourcePropertyId.class })
	public void testGetResourcePropertyTime() {

		ActionPlugin.Builder pluginBuilder = ActionPlugin.builder();

		Map<MultiKey, MutableDouble> expectedTimes = new LinkedHashMap<>();

		pluginBuilder.addDataView(new LocalDataView());

		pluginBuilder.addAgent("agent");
		pluginBuilder.addAgentActionPlan("agent", new AgentActionPlan(0, (c) -> {
			// establish data views
			LocalDataView localDataView = c.getDataView(LocalDataView.class).get();
			localDataView.resourceDataManager = getResourceDataManager(c);
			StochasticsDataView stochasticsDataView = c.getDataView(StochasticsDataView.class).get();
			RandomGenerator randomGenerator = stochasticsDataView.getRandomGenerator();

			// establish the resources
			Set<ResourceId> resourceIds = localDataView.resourceDataManager.getResourceIds();

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
				PropertyDefinition propertyDefinition = localDataView.resourceDataManager.getResourcePropertyDefinition(testResourceId, testResourcePropertyId);
				if (propertyDefinition.propertyValuesAreMutable()) {
					Object value = testResourcePropertyId.getRandomPropertyValue(randomGenerator);
					localDataView.resourceDataManager.setResourcePropertyValue(testResourceId, testResourcePropertyId, value);
					expectedTimes.get(new MultiKey(testResourceId, testResourcePropertyId)).setValue(c.getTime());
				}
			}

		}));

		// make more resource property updates at time 1
		pluginBuilder.addAgentActionPlan("agent", new AgentActionPlan(1, (c) -> {
			// establish data views
			LocalDataView localDataView = c.getDataView(LocalDataView.class).get();
			StochasticsDataView stochasticsDataView = c.getDataView(StochasticsDataView.class).get();
			RandomGenerator randomGenerator = stochasticsDataView.getRandomGenerator();

			// establish the resources
			Set<ResourceId> resourceIds = localDataView.resourceDataManager.getResourceIds();

			// set random values to the resource properties
			for (int i = 0; i < resourceIds.size(); i++) {
				TestResourceId testResourceId = TestResourceId.getRandomResourceId(randomGenerator);
				TestResourcePropertyId testResourcePropertyId = TestResourcePropertyId.getRandomResourcePropertyId(testResourceId, randomGenerator);
				PropertyDefinition propertyDefinition = localDataView.resourceDataManager.getResourcePropertyDefinition(testResourceId, testResourcePropertyId);
				if (propertyDefinition.propertyValuesAreMutable()) {
					Object value = testResourcePropertyId.getRandomPropertyValue(randomGenerator);
					localDataView.resourceDataManager.setResourcePropertyValue(testResourceId, testResourcePropertyId, value);
					expectedTimes.get(new MultiKey(testResourceId, testResourcePropertyId)).setValue(c.getTime());
				}
			}

		}));

		// make more resource property updates at time 2
		pluginBuilder.addAgentActionPlan("agent", new AgentActionPlan(2, (c) -> {
			// establish data views
			LocalDataView localDataView = c.getDataView(LocalDataView.class).get();
			StochasticsDataView stochasticsDataView = c.getDataView(StochasticsDataView.class).get();
			RandomGenerator randomGenerator = stochasticsDataView.getRandomGenerator();

			// establish the resources
			Set<ResourceId> resourceIds = localDataView.resourceDataManager.getResourceIds();

			// set random values to the resource properties
			for (int i = 0; i < resourceIds.size(); i++) {
				TestResourceId testResourceId = TestResourceId.getRandomResourceId(randomGenerator);
				TestResourcePropertyId testResourcePropertyId = TestResourcePropertyId.getRandomResourcePropertyId(testResourceId, randomGenerator);
				PropertyDefinition propertyDefinition = localDataView.resourceDataManager.getResourcePropertyDefinition(testResourceId, testResourcePropertyId);
				if (propertyDefinition.propertyValuesAreMutable()) {
					Object value = testResourcePropertyId.getRandomPropertyValue(randomGenerator);
					localDataView.resourceDataManager.setResourcePropertyValue(testResourceId, testResourcePropertyId, value);
					expectedTimes.get(new MultiKey(testResourceId, testResourcePropertyId)).setValue(c.getTime());
				}
			}

		}));

		// test the person resource times
		pluginBuilder.addAgentActionPlan("agent", new AgentActionPlan(3, (c) -> {
			// establish data views

			LocalDataView localDataView = c.getDataView(LocalDataView.class).get();

			// show that the person resource times match expectations
			int actualAssertionsCount = 0;
			for (MultiKey multiKey : expectedTimes.keySet()) {
				ResourceId resourceId = multiKey.getKey(0);
				ResourcePropertyId resourcePropertyId = multiKey.getKey(1);
				double expectedTime = expectedTimes.get(multiKey).getValue();
				double actualTime = localDataView.resourceDataManager.getResourcePropertyTime(resourceId, resourcePropertyId);
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

		// precondition tests
		pluginBuilder.addAgentActionPlan("agent", new AgentActionPlan(4, (c) -> {
			// establish data views

			LocalDataView localDataView = c.getDataView(LocalDataView.class).get();

			// if the resource id is null
			assertThrows(RuntimeException.class, () -> localDataView.resourceDataManager.getResourcePropertyTime(null, TestResourcePropertyId.ResourceProperty_1_1_BOOLEAN_MUTABLE));

			// if the resource id is unknown
			assertThrows(RuntimeException.class,
					() -> localDataView.resourceDataManager.getResourcePropertyTime(TestResourceId.getUnknownResourceId(), TestResourcePropertyId.ResourceProperty_1_1_BOOLEAN_MUTABLE));

			// if the resource property id is null
			assertThrows(RuntimeException.class, () -> localDataView.resourceDataManager.getResourcePropertyTime(TestResourceId.RESOURCE_1, null));

			// if the resource property id is unknown
			assertThrows(RuntimeException.class,
					() -> localDataView.resourceDataManager.getResourcePropertyTime(TestResourceId.RESOURCE_1, TestResourcePropertyId.ResourceProperty_2_1_BOOLEAN_MUTABLE));

			// if the resource property id is unknown
			assertThrows(RuntimeException.class, () -> localDataView.resourceDataManager.getResourcePropertyTime(TestResourceId.RESOURCE_1, TestResourcePropertyId.getUnknownResourcePropertyId()));

		}));

		ResourcesActionSupport.testConsumers(10, 7395914216206749257L, pluginBuilder.build());

	}

	@Test
	@UnitTestMethod(name = "getResourcePropertyValue", args = { ResourceId.class, ResourcePropertyId.class })
	public void testGetResourcePropertyValue() {

		ResourcesActionSupport.testConsumer(10, 3652970289155460782L, (c) -> {
			ResourceDataManager resourceDataManager = getResourceDataManager(c);
			StochasticsDataView stochasticsDataView = c.getDataView(StochasticsDataView.class).get();
			RandomGenerator randomGenerator = stochasticsDataView.getRandomGenerator();

			// establish the expected values of all resource properties
			Map<MultiKey, Object> expectedValues = new LinkedHashMap<>();
			for (TestResourceId testResourceId : TestResourceId.values()) {
				for (TestResourcePropertyId testResourcePropertyId : TestResourcePropertyId.getTestResourcePropertyIds(testResourceId)) {
					Object propertyValue = resourceDataManager.getResourcePropertyValue(testResourceId, testResourcePropertyId);
					expectedValues.put(new MultiKey(testResourceId, testResourcePropertyId), propertyValue);
				}
			}

			// make a few random resource property updates
			int updateCount = 0;
			for (int i = 0; i < 1000; i++) {
				TestResourceId testResourceId = TestResourceId.getRandomResourceId(randomGenerator);
				TestResourcePropertyId testResourcePropertyId = TestResourcePropertyId.getRandomResourcePropertyId(testResourceId, randomGenerator);
				PropertyDefinition resourcePropertyDefinition = resourceDataManager.getResourcePropertyDefinition(testResourceId, testResourcePropertyId);
				if (resourcePropertyDefinition.propertyValuesAreMutable()) {
					Object propertyValue = testResourcePropertyId.getRandomPropertyValue(randomGenerator);
					resourceDataManager.setResourcePropertyValue(testResourceId, testResourcePropertyId, propertyValue);
					expectedValues.put(new MultiKey(testResourceId, testResourcePropertyId), propertyValue);
					updateCount++;
				}
			}

			/*
			 * Show that the number of updates was reasonable - some of the
			 * properties are not mutable so it will be <1000
			 */
			assertTrue(updateCount > 500);

			// show that the values of the resource propeties are correct
			for (MultiKey multiKey : expectedValues.keySet()) {
				TestResourceId testResourceId = multiKey.getKey(0);
				TestResourcePropertyId testResourcePropertyId = multiKey.getKey(1);
				Object expectedValue = expectedValues.get(multiKey);
				Object actualValue = resourceDataManager.getResourcePropertyValue(testResourceId, testResourcePropertyId);
				assertEquals(expectedValue, actualValue);
			}

			// precondition tests

			// if the resource id is null
			assertThrows(RuntimeException.class, () -> resourceDataManager.getResourcePropertyValue(null, TestResourcePropertyId.ResourceProperty_1_1_BOOLEAN_MUTABLE));

			// if the resource id unknown
			assertThrows(RuntimeException.class,
					() -> resourceDataManager.getResourcePropertyValue(TestResourceId.getUnknownResourceId(), TestResourcePropertyId.ResourceProperty_1_1_BOOLEAN_MUTABLE));

			// if the resource property id is null
			assertThrows(RuntimeException.class, () -> resourceDataManager.getResourcePropertyValue(TestResourceId.RESOURCE_1, null));

			// if the resource property id unknown
			assertThrows(RuntimeException.class, () -> resourceDataManager.getResourcePropertyValue(TestResourceId.RESOURCE_1, TestResourcePropertyId.ResourceProperty_2_1_BOOLEAN_MUTABLE));

			assertThrows(RuntimeException.class, () -> resourceDataManager.getResourcePropertyValue(TestResourceId.RESOURCE_1, TestResourcePropertyId.getUnknownResourcePropertyId()));

		});
	}

	@Test
	@UnitTestMethod(name = "incrementPersonResourceLevel", args = { ResourceId.class, PersonId.class, long.class })
	public void testIncrementPersonResourceLevel() {

		ResourcesActionSupport.testConsumer(15, 1710364086676989315L, (c) -> {
			ResourceDataManager resourceDataManager = getResourceDataManager(c);
			PersonDataView personDataView = c.getDataView(PersonDataView.class).get();

			for (PersonId personId : personDataView.getPeople()) {
				for (TestResourceId testResourceId : TestResourceId.values()) {
					// add a bit of resource so that we can test taking it away
					long actualResourceLevel = resourceDataManager.getPersonResourceLevel(testResourceId, personId);
					long expectedResourceLevel = actualResourceLevel;

					// show that incrementing the value results in the correct
					// balance
					for (int i = 0; i < 3; i++) {
						expectedResourceLevel += 10;
						resourceDataManager.incrementPersonResourceLevel(testResourceId, personId, 10);
						actualResourceLevel = resourceDataManager.getPersonResourceLevel(testResourceId, personId);
						assertEquals(actualResourceLevel, expectedResourceLevel);
					}
				}
			}

			// precondition tests

			// if the resource id is null
			assertThrows(RuntimeException.class, () -> resourceDataManager.incrementPersonResourceLevel(null, new PersonId(0), 10));

			// if the resource id is unknown
			assertThrows(RuntimeException.class, () -> resourceDataManager.incrementPersonResourceLevel(TestResourceId.getUnknownResourceId(), new PersonId(0), 10));

			// if the person id is null
			assertThrows(RuntimeException.class, () -> resourceDataManager.incrementPersonResourceLevel(TestResourceId.RESOURCE_1, null, 10));

			// if the person id has a negative value
			assertThrows(RuntimeException.class, () -> resourceDataManager.incrementPersonResourceLevel(TestResourceId.RESOURCE_1, new PersonId(-1), 10));

			// if the amount causes an overflow
			assertThrows(RuntimeException.class, () -> resourceDataManager.incrementPersonResourceLevel(TestResourceId.RESOURCE_1, new PersonId(0), Long.MAX_VALUE));

		});
	}

	@Test
	@UnitTestMethod(name = "incrementRegionResourceLevel", args = { RegionId.class, ResourceId.class, long.class })
	public void testIncrementRegionResourceLevel() {

		ResourcesActionSupport.testConsumer(15, 1583216651087452100L, (c) -> {
			ResourceDataManager resourceDataManager = getResourceDataManager(c);
			RegionDataView regionDataView = c.getDataView(RegionDataView.class).get();

			for (RegionId regionId : regionDataView.getRegionIds()) {
				for (TestResourceId testResourceId : TestResourceId.values()) {
					// add a bit of resource so that we can test taking it away
					long actualResourceLevel = resourceDataManager.getRegionResourceLevel(regionId, testResourceId);
					long expectedResourceLevel = actualResourceLevel;

					// show that incrementing the value results in the correct
					// balance
					for (int i = 0; i < 3; i++) {
						expectedResourceLevel += 10;
						resourceDataManager.incrementRegionResourceLevel(regionId, testResourceId, 10);
						actualResourceLevel = resourceDataManager.getRegionResourceLevel(regionId, testResourceId);
						assertEquals(actualResourceLevel, expectedResourceLevel);
					}
				}
			}

			// precondition tests

			// if the resource id is null
			assertThrows(RuntimeException.class, () -> resourceDataManager.incrementRegionResourceLevel(TestRegionId.REGION_2, null, 10));

			// if the resource id is unknown
			assertThrows(RuntimeException.class, () -> resourceDataManager.incrementRegionResourceLevel(TestRegionId.REGION_2, TestResourceId.getUnknownResourceId(), 10));

			// if the region id is null
			assertThrows(RuntimeException.class, () -> resourceDataManager.incrementRegionResourceLevel(null, TestResourceId.RESOURCE_1, 10));

			// if the region id is unknown
			assertThrows(RuntimeException.class, () -> resourceDataManager.incrementRegionResourceLevel(TestRegionId.getUnknownRegionId(), TestResourceId.RESOURCE_1, 10));

			// if the amount is negative
			ContractException contractException = assertThrows(ContractException.class, () -> resourceDataManager.incrementRegionResourceLevel(TestRegionId.REGION_2, TestResourceId.RESOURCE_3, -1L));
			assertEquals(ResourceError.NEGATIVE_RESOURCE_AMOUNT, contractException.getErrorType());

			// if the amount causes an overflow
			assertThrows(RuntimeException.class, () -> resourceDataManager.incrementRegionResourceLevel(TestRegionId.REGION_2, TestResourceId.RESOURCE_3, Long.MAX_VALUE));

		});
	}

	@Test
	@UnitTestConstructor(args = { ResolverContext.class, ResourceInitialData.class })
	public void testConstructor() {
		ContractException contractException = assertThrows(ContractException.class, () -> new ResourceDataManager(null));
		assertEquals(NucleusError.NULL_CONTEXT, contractException.getErrorType());
	}

	@Test
	@UnitTestMethod(name = "resourceIdExists", args = { ResourceId.class })
	public void testResourceIdExists() {

		ResourcesActionSupport.testConsumer(5, 6439381853290054705L, (c) -> {
			ResourceDataManager resourceDataManager = getResourceDataManager(c);

			// show that the resource ids that exist are the test resource ids

			for (TestResourceId testResourceId : TestResourceId.values()) {
				assertTrue(resourceDataManager.resourceIdExists(testResourceId));
			}
			assertFalse(resourceDataManager.resourceIdExists(TestResourceId.getUnknownResourceId()));
			assertFalse(resourceDataManager.resourceIdExists(null));

		});
	}

	@Test
	@UnitTestMethod(name = "resourcePropertyIdExists", args = { ResourceId.class, ResourcePropertyId.class })
	public void testResourcePropertyIdExists() {
		
		ResourcesActionSupport.testConsumer(5, 1388634541689616758L, (c) -> {
			ResourceDataManager resourceDataManager = getResourceDataManager(c);

			// show that the resource property ids that exist are the test
			// resource property ids

			for (TestResourcePropertyId testResourcePropertyId : TestResourcePropertyId.values()) {
				assertTrue(resourceDataManager.resourcePropertyIdExists(testResourcePropertyId.getTestResourceId(), testResourcePropertyId));
			}

			assertFalse(resourceDataManager.resourcePropertyIdExists(TestResourceId.RESOURCE_1, TestResourcePropertyId.ResourceProperty_2_1_BOOLEAN_MUTABLE));
			assertFalse(resourceDataManager.resourcePropertyIdExists(null, TestResourcePropertyId.ResourceProperty_2_1_BOOLEAN_MUTABLE));
			assertFalse(resourceDataManager.resourcePropertyIdExists(TestResourceId.RESOURCE_1, null));
			assertFalse(resourceDataManager.resourcePropertyIdExists(TestResourceId.RESOURCE_1, TestResourcePropertyId.getUnknownResourcePropertyId()));
			assertFalse(resourceDataManager.resourcePropertyIdExists(TestResourceId.getUnknownResourceId(), TestResourcePropertyId.ResourceProperty_1_2_INTEGER_MUTABLE));
			assertFalse(resourceDataManager.resourcePropertyIdExists(TestResourceId.getUnknownResourceId(), TestResourcePropertyId.getUnknownResourcePropertyId()));

		});
	}

	@Test
	@UnitTestMethod(name = "setResourcePropertyValue", args = { ResourceId.class, ResourcePropertyId.class, Object.class })
	public void testSetResourcePropertyValue() {

		ResourcesActionSupport.testConsumer(10, 7263244660188172288L, (c) -> {
			ResourceDataManager resourceDataManager = getResourceDataManager(c);
			StochasticsDataView stochasticsDataView = c.getDataView(StochasticsDataView.class).get();
			RandomGenerator randomGenerator = stochasticsDataView.getRandomGenerator();

			// establish the expected values of all resource properties
			Map<MultiKey, Object> expectedValues = new LinkedHashMap<>();
			for (TestResourceId testResourceId : TestResourceId.values()) {
				for (TestResourcePropertyId testResourcePropertyId : TestResourcePropertyId.getTestResourcePropertyIds(testResourceId)) {
					Object propertyValue = resourceDataManager.getResourcePropertyValue(testResourceId, testResourcePropertyId);
					expectedValues.put(new MultiKey(testResourceId, testResourcePropertyId), propertyValue);
				}
			}

			// make a few random resource property updates
			int updateCount = 0;
			for (int i = 0; i < 1000; i++) {
				TestResourceId testResourceId = TestResourceId.getRandomResourceId(randomGenerator);
				TestResourcePropertyId testResourcePropertyId = TestResourcePropertyId.getRandomResourcePropertyId(testResourceId, randomGenerator);
				PropertyDefinition resourcePropertyDefinition = resourceDataManager.getResourcePropertyDefinition(testResourceId, testResourcePropertyId);
				if (resourcePropertyDefinition.propertyValuesAreMutable()) {
					Object propertyValue = testResourcePropertyId.getRandomPropertyValue(randomGenerator);
					resourceDataManager.setResourcePropertyValue(testResourceId, testResourcePropertyId, propertyValue);
					expectedValues.put(new MultiKey(testResourceId, testResourcePropertyId), propertyValue);
					updateCount++;
				}
			}

			/*
			 * Show that the number of updates was reasonable - some of the
			 * properties are not mutable so it will be <1000
			 */
			assertTrue(updateCount > 500);

			// show that the values of the resource propeties are correct
			for (MultiKey multiKey : expectedValues.keySet()) {
				TestResourceId testResourceId = multiKey.getKey(0);
				TestResourcePropertyId testResourcePropertyId = multiKey.getKey(1);
				Object expectedValue = expectedValues.get(multiKey);
				Object actualValue = resourceDataManager.getResourcePropertyValue(testResourceId, testResourcePropertyId);
				assertEquals(expectedValue, actualValue);
			}

			// precondition tests

			// if the resource id is null
			assertThrows(RuntimeException.class, () -> resourceDataManager.setResourcePropertyValue(null, TestResourcePropertyId.ResourceProperty_1_1_BOOLEAN_MUTABLE, false));

			// if the resource id unknown
			assertThrows(RuntimeException.class,
					() -> resourceDataManager.setResourcePropertyValue(TestResourceId.getUnknownResourceId(), TestResourcePropertyId.ResourceProperty_1_1_BOOLEAN_MUTABLE, false));

			// if the resource property id is null
			assertThrows(RuntimeException.class, () -> resourceDataManager.setResourcePropertyValue(TestResourceId.RESOURCE_1, null, 12));

			// if the resource property id unknown
			assertThrows(RuntimeException.class, () -> resourceDataManager.setResourcePropertyValue(TestResourceId.RESOURCE_1, TestResourcePropertyId.ResourceProperty_2_1_BOOLEAN_MUTABLE, true));

			assertThrows(RuntimeException.class, () -> resourceDataManager.setResourcePropertyValue(TestResourceId.RESOURCE_1, TestResourcePropertyId.getUnknownResourcePropertyId(), 45));

		});
	}

	@Test
	@UnitTestMethod(name = "expandCapacity", args = { int.class })
	public void testExpandCapacity() {
		// nothing to test
		// needs to be tested manually under performance testing
	}

}
