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

import nucleus.DataView;
import nucleus.NucleusError;
import nucleus.ResolverContext;
import nucleus.testsupport.actionplugin.ActionPlugin;
import nucleus.testsupport.actionplugin.AgentActionPlan;
import plugins.people.datacontainers.PersonDataView;
import plugins.people.support.PersonError;
import plugins.people.support.PersonId;
import plugins.properties.support.PropertyDefinition;
import plugins.properties.support.TimeTrackingPolicy;
import plugins.regions.datacontainers.RegionDataView;
import plugins.regions.datacontainers.RegionLocationDataView;
import plugins.regions.support.RegionError;
import plugins.regions.support.RegionId;
import plugins.regions.testsupport.TestRegionId;
import plugins.resources.events.mutation.RegionResourceAdditionEvent;
import plugins.resources.events.mutation.ResourcePropertyValueAssignmentEvent;
import plugins.resources.events.mutation.ResourceTransferToPersonEvent;
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

@UnitTest(target = ResourceDataView.class)
public final class AT_ResourceDataView implements DataView {

	@Test
	@UnitTestConstructor(args = { ResolverContext.class, ResourceDataManager.class })
	public void testConstructor() {

		ResourcesActionSupport.testConsumer(15, 1963956124979067643L, (c) -> {

			ResourceDataView resourceDataView = c.getDataView(ResourceDataView.class).get();
			RegionDataView regionDataView = c.getDataView(RegionDataView.class).get();
			PersonDataView personDataView = c.getDataView(PersonDataView.class).get();

			ResourceDataManager resourceDataManager = new ResourceDataManager(c);
			Set<ResourceId> resourceIds = resourceDataView.getResourceIds();
			for (ResourceId resourceId : resourceIds) {
				resourceDataManager.addResource(resourceId, resourceDataView.getPersonResourceTimeTrackingPolicy(resourceId));
				Set<ResourcePropertyId> resourcePropertyIds = resourceDataView.getResourcePropertyIds(resourceId);
				for (ResourcePropertyId resourcePropertyId : resourcePropertyIds) {
					PropertyDefinition resourcePropertyDefinition = resourceDataView.getResourcePropertyDefinition(resourceId, resourcePropertyId);
					Object propertyValue = resourceDataView.getResourcePropertyValue(resourceId, resourcePropertyId);
					resourceDataManager.defineResourceProperty(resourceId, resourcePropertyId, resourcePropertyDefinition, propertyValue);
				}
			}

			Set<RegionId> regionIds = regionDataView.getRegionIds();
			for (RegionId regionId : regionIds) {
				resourceDataManager.addRegion(regionId);
				for (ResourceId resourceId : resourceIds) {
					long regionResourceLevel = resourceDataView.getRegionResourceLevel(regionId, resourceId);
					resourceDataManager.incrementRegionResourceLevel(regionId, resourceId, regionResourceLevel);
				}
			}

			List<PersonId> people = personDataView.getPeople();
			for (PersonId personId : people) {
				for (ResourceId resourceId : resourceIds) {
					long personResourceLevel = resourceDataView.getPersonResourceLevel(resourceId, personId);
					resourceDataManager.incrementPersonResourceLevel(resourceId, personId, personResourceLevel);
				}
			}
			ContractException contractException = assertThrows(ContractException.class, () -> new ResourceDataView(null, resourceDataManager));
			assertEquals(NucleusError.NULL_CONTEXT, contractException.getErrorType());

			contractException = assertThrows(ContractException.class, () -> new ResourceDataView(c, null));
			assertEquals(ResourceError.NULL_RESOURCE_DATA_MANAGER, contractException.getErrorType());

		});

	}

	@Test
	@UnitTestMethod(name = "getPeopleWithoutResource", args = { ResourceId.class })
	public void testGetPeopleWithoutResource() {

		ResourcesActionSupport.testConsumer(100, 3641510187112920884L, (c) -> {
			ResourceDataView resourceDataView = c.getDataView(ResourceDataView.class).get();
			RegionLocationDataView regionLocationDataView = c.getDataView(RegionLocationDataView.class).get();
			StochasticsDataView stochasticsDataView = c.getDataView(StochasticsDataView.class).get();
			RandomGenerator randomGenerator = stochasticsDataView.getRandomGenerator();
			PersonDataView personDataView = c.getDataView(PersonDataView.class).get();
			Set<PersonId> expectedPeople = new LinkedHashSet<>();
			// give about half of the people the resource
			for (PersonId personId : personDataView.getPeople()) {
				if (randomGenerator.nextBoolean()) {
					RegionId regionId = regionLocationDataView.getPersonRegion(personId);
					c.resolveEvent(new RegionResourceAdditionEvent(TestResourceId.RESOURCE_5, regionId, 5));
					c.resolveEvent(new ResourceTransferToPersonEvent(TestResourceId.RESOURCE_5, personId, 5));
				} else {
					expectedPeople.add(personId);
				}
			}
			// show that those who did not get the resource are returned
			List<PersonId> actualPeople = resourceDataView.getPeopleWithoutResource(TestResourceId.RESOURCE_5);
			assertEquals(expectedPeople.size(), actualPeople.size());
			assertEquals(expectedPeople, new LinkedHashSet<>(actualPeople));

			// precondition tests

			// if the resource id is null
			ContractException contractException = assertThrows(ContractException.class, () -> resourceDataView.getPeopleWithoutResource(null));
			assertEquals(ResourceError.NULL_RESOURCE_ID, contractException.getErrorType());

			// if the resource id is unknown
			contractException = assertThrows(ContractException.class, () -> resourceDataView.getPeopleWithoutResource(TestResourceId.getUnknownResourceId()));
			assertEquals(ResourceError.UNKNOWN_RESOURCE_ID, contractException.getErrorType());

		});

	}

	@Test
	@UnitTestMethod(name = "getPeopleWithResource", args = { ResourceId.class })
	public void testGetPeopleWithResource() {

		ResourcesActionSupport.testConsumer(100, 1030108367649001208L, (c) -> {
			ResourceDataView resourceDataView = c.getDataView(ResourceDataView.class).get();
			RegionLocationDataView regionLocationDataView = c.getDataView(RegionLocationDataView.class).get();
			StochasticsDataView stochasticsDataView = c.getDataView(StochasticsDataView.class).get();
			RandomGenerator randomGenerator = stochasticsDataView.getRandomGenerator();
			PersonDataView personDataView = c.getDataView(PersonDataView.class).get();
			Set<PersonId> expectedPeople = new LinkedHashSet<>();
			// give about half of the people the resource
			for (PersonId personId : personDataView.getPeople()) {
				if (randomGenerator.nextBoolean()) {
					RegionId regionId = regionLocationDataView.getPersonRegion(personId);
					c.resolveEvent(new RegionResourceAdditionEvent(TestResourceId.RESOURCE_5, regionId, 5));
					c.resolveEvent(new ResourceTransferToPersonEvent(TestResourceId.RESOURCE_5, personId, 5));
					expectedPeople.add(personId);
				}
			}
			// show that those who did not get the resource are returned
			List<PersonId> actualPeople = resourceDataView.getPeopleWithResource(TestResourceId.RESOURCE_5);
			assertEquals(expectedPeople.size(), actualPeople.size());
			assertEquals(expectedPeople, new LinkedHashSet<>(actualPeople));

			// precondition tests

			// if the resource id is null
			ContractException contractException = assertThrows(ContractException.class, () -> resourceDataView.getPeopleWithoutResource(null));
			assertEquals(ResourceError.NULL_RESOURCE_ID, contractException.getErrorType());

			// if the resource id is unknown
			contractException = assertThrows(ContractException.class, () -> resourceDataView.getPeopleWithoutResource(TestResourceId.getUnknownResourceId()));
			assertEquals(ResourceError.UNKNOWN_RESOURCE_ID, contractException.getErrorType());
		});

	}

	@Test
	@UnitTestMethod(name = "getPersonResourceLevel", args = { ResourceId.class, PersonId.class })
	public void testGetPersonResourceLevel() {

		ResourcesActionSupport.testConsumer(20, 110987310555566746L, (c) -> {
			ResourceDataView resourceDataView = c.getDataView(ResourceDataView.class).get();
			RegionLocationDataView regionLocationDataView = c.getDataView(RegionLocationDataView.class).get();
			StochasticsDataView stochasticsDataView = c.getDataView(StochasticsDataView.class).get();
			RandomGenerator randomGenerator = stochasticsDataView.getRandomGenerator();
			PersonDataView personDataView = c.getDataView(PersonDataView.class).get();

			List<PersonId> people = personDataView.getPeople();

			// give random amounts of resource to random people
			for (int i = 0; i < 1000; i++) {
				PersonId personId = people.get(randomGenerator.nextInt(people.size()));
				ResourceId resourceId = TestResourceId.getRandomResourceId(randomGenerator);
				int amount = randomGenerator.nextInt(5);
				long expectedLevel = resourceDataView.getPersonResourceLevel(resourceId, personId);
				expectedLevel += amount;
				
				
				RegionId regionId = regionLocationDataView.getPersonRegion(personId);
				c.resolveEvent(new RegionResourceAdditionEvent(resourceId, regionId, amount));
				c.resolveEvent(new ResourceTransferToPersonEvent(resourceId, personId, amount));
				
				long actualLevel = resourceDataView.getPersonResourceLevel(resourceId, personId);
				assertEquals(expectedLevel, actualLevel);
			}

			// precondition tests

			// if the resource id is null
			ContractException contractException = assertThrows(ContractException.class, () -> resourceDataView.getPersonResourceLevel(null, new PersonId(0)));
			assertEquals(ResourceError.NULL_RESOURCE_ID, contractException.getErrorType());
			
			// if the resource id is unknown
			contractException = assertThrows(ContractException.class, () -> resourceDataView.getPersonResourceLevel(TestResourceId.getUnknownResourceId(), new PersonId(0)));
			assertEquals(ResourceError.UNKNOWN_RESOURCE_ID, contractException.getErrorType());
			
			// if the person id null
			contractException = assertThrows(ContractException.class, () -> resourceDataView.getPersonResourceLevel(TestResourceId.RESOURCE_1, null));
			assertEquals(PersonError.NULL_PERSON_ID, contractException.getErrorType());
			
			// if the person id has a negative value
			contractException = assertThrows(ContractException.class, () -> resourceDataView.getPersonResourceLevel(TestResourceId.RESOURCE_1, new PersonId(-1)));
			assertEquals(PersonError.UNKNOWN_PERSON_ID, contractException.getErrorType());
			

		});
	}

	@Test
	@UnitTestMethod(name = "getPersonResourceTime", args = { ResourceId.class, PersonId.class })
	public void testGetPersonResourceTime() {
		ActionPlugin.Builder pluginBuilder = ActionPlugin.builder();

		Map<MultiKey, MutableDouble> expectedTimes = new LinkedHashMap<>();

	

		pluginBuilder.addAgent("agent");
		pluginBuilder.addAgentActionPlan("agent", new AgentActionPlan(0, (c) -> {
			// establish data views
			RegionLocationDataView regionLocationDataView = c.getDataView(RegionLocationDataView.class).get();
			ResourceDataView resourceDataView = c.getDataView(ResourceDataView.class).get();
			StochasticsDataView stochasticsDataView = c.getDataView(StochasticsDataView.class).get();
			RandomGenerator randomGenerator = stochasticsDataView.getRandomGenerator();
			PersonDataView personDataView = c.getDataView(PersonDataView.class).get();

			// establish the people and resources
			Set<ResourceId> resourceIds = resourceDataView.getResourceIds();
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
				TimeTrackingPolicy personResourceTimeTrackingPolicy = resourceDataView.getPersonResourceTimeTrackingPolicy(resourceId);
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
				
				RegionId regionId = regionLocationDataView.getPersonRegion(personId);
				c.resolveEvent(new RegionResourceAdditionEvent(resourceId, regionId, amount));
				c.resolveEvent(new ResourceTransferToPersonEvent(resourceId, personId, amount));
				
				expectedTimes.get(new MultiKey(personId, resourceId)).setValue(c.getTime());
			}

		}));

		// make more resource updates at time 1
		pluginBuilder.addAgentActionPlan("agent", new AgentActionPlan(1, (c) -> {
			// establish data views
			RegionLocationDataView regionLocationDataView = c.getDataView(RegionLocationDataView.class).get();

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
				
				RegionId regionId = regionLocationDataView.getPersonRegion(personId);
				c.resolveEvent(new RegionResourceAdditionEvent(resourceId, regionId, amount));
				c.resolveEvent(new ResourceTransferToPersonEvent(resourceId, personId, amount));
				
				
				expectedTimes.get(new MultiKey(personId, resourceId)).setValue(c.getTime());
			}

		}));

		// make more resource updates at time 2
		pluginBuilder.addAgentActionPlan("agent", new AgentActionPlan(2, (c) -> {
			// establish data views
			RegionLocationDataView regionLocationDataView = c.getDataView(RegionLocationDataView.class).get();

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
				RegionId regionId = regionLocationDataView.getPersonRegion(personId);
				c.resolveEvent(new RegionResourceAdditionEvent(resourceId, regionId, amount));
				c.resolveEvent(new ResourceTransferToPersonEvent(resourceId, personId, amount));
				
				expectedTimes.get(new MultiKey(personId, resourceId)).setValue(c.getTime());
			}

		}));

		// test the person resource times
		pluginBuilder.addAgentActionPlan("agent", new AgentActionPlan(3, (c) -> {
			// establish data views

			
			ResourceDataView resourceDataView = c.getDataView(ResourceDataView.class).get();
			PersonDataView personDataView = c.getDataView(PersonDataView.class).get();

			// show that the person resource times match expectations
			int actualAssertionsCount = 0;
			for (MultiKey multiKey : expectedTimes.keySet()) {
				PersonId personId = multiKey.getKey(0);
				ResourceId resourceId = multiKey.getKey(1);
				TimeTrackingPolicy personResourceTimeTrackingPolicy = resourceDataView.getPersonResourceTimeTrackingPolicy(resourceId);
				if (personResourceTimeTrackingPolicy == TimeTrackingPolicy.TRACK_TIME) {
					double expectedTime = expectedTimes.get(multiKey).getValue();
					double actualTime = resourceDataView.getPersonResourceTime(resourceId, personId);
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
			for (ResourceId resourceId : resourceDataView.getResourceIds()) {
				TimeTrackingPolicy personResourceTimeTrackingPolicy = resourceDataView.getPersonResourceTimeTrackingPolicy(resourceId);
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

			ResourceDataView resourceDataView = c.getDataView(ResourceDataView.class).get();

			// if the assignment times for the resource are not tracked
			ContractException contractException = assertThrows(ContractException.class, () -> resourceDataView.getPersonResourceTime(TestResourceId.RESOURCE_2, new PersonId(0)));
			assertEquals(ResourceError.RESOURCE_ASSIGNMENT_TIME_NOT_TRACKED, contractException.getErrorType());
			
			// if the resource id is null
			contractException = assertThrows(ContractException.class, () -> resourceDataView.getPersonResourceTime(null, new PersonId(0)));
			assertEquals(ResourceError.NULL_RESOURCE_ID, contractException.getErrorType());
			
			// if the resource id is unknown
			contractException = assertThrows(ContractException.class, () -> resourceDataView.getPersonResourceTime(TestResourceId.getUnknownResourceId(), new PersonId(0)));
			assertEquals(ResourceError.UNKNOWN_RESOURCE_ID, contractException.getErrorType());
			
			// if the person id null
			contractException = assertThrows(ContractException.class, () -> resourceDataView.getPersonResourceTime(TestResourceId.RESOURCE_1, null));
			assertEquals(PersonError.NULL_PERSON_ID, contractException.getErrorType());
			
			// if the person id has a negative value
			contractException = assertThrows(ContractException.class, () -> resourceDataView.getPersonResourceTime(TestResourceId.RESOURCE_1, new PersonId(-1)));
			assertEquals(PersonError.UNKNOWN_PERSON_ID, contractException.getErrorType());

		}));

		ResourcesActionSupport.testConsumers(30, 3274189520478045515L, pluginBuilder.build());

	}

	@Test
	@UnitTestMethod(name = "getRegionResourceLevel", args = { RegionId.class, ResourceId.class })
	public void testGetRegionResourceLevel() {
		
		
		ResourcesActionSupport.testConsumer(20, 6606932435911201728L, (c) -> {
			ResourceDataView resourceDataView = c.getDataView(ResourceDataView.class).get();
			StochasticsDataView stochasticsDataView = c.getDataView(StochasticsDataView.class).get();
			RandomGenerator randomGenerator = stochasticsDataView.getRandomGenerator();
			RegionDataView regionDataView = c.getDataView(RegionDataView.class).get();

			List<RegionId> regionIds = new ArrayList<>(regionDataView.getRegionIds());

			// give random amounts of resource to random regions
			for (int i = 0; i < 1000; i++) {
				RegionId regionId = regionIds.get(randomGenerator.nextInt(regionIds.size()));
				ResourceId resourceId = TestResourceId.getRandomResourceId(randomGenerator);
				int amount = randomGenerator.nextInt(5);
				long expectedLevel = resourceDataView.getRegionResourceLevel(regionId, resourceId);
				expectedLevel += amount;
				c.resolveEvent(new RegionResourceAdditionEvent(resourceId, regionId, amount));
				long actualLevel = resourceDataView.getRegionResourceLevel(regionId, resourceId);
				assertEquals(expectedLevel, actualLevel);
			}

			// precondition tests

			// if the resource id is null
			ContractException contractException = assertThrows(ContractException.class, () -> resourceDataView.getRegionResourceLevel(TestRegionId.REGION_1, null));
			assertEquals(ResourceError.NULL_RESOURCE_ID, contractException.getErrorType());
			
			// if the resource id is unknown
			contractException = assertThrows(ContractException.class, () -> resourceDataView.getRegionResourceLevel(TestRegionId.REGION_1, TestResourceId.getUnknownResourceId()));
			assertEquals(ResourceError.UNKNOWN_RESOURCE_ID, contractException.getErrorType());
			
			// if the region id null
			contractException = assertThrows(ContractException.class, () -> resourceDataView.getRegionResourceLevel(null, TestResourceId.RESOURCE_1));
			assertEquals(RegionError.NULL_REGION_ID, contractException.getErrorType());
			
			// if the region id is unknown
			contractException = assertThrows(ContractException.class, () -> resourceDataView.getRegionResourceLevel(TestRegionId.getUnknownRegionId(), TestResourceId.RESOURCE_1));
			assertEquals(RegionError.UNKNOWN_REGION_ID, contractException.getErrorType());
			
			
		});
	}

	@Test
	@UnitTestMethod(name = "getRegionResourceTime", args = { RegionId.class, ResourceId.class })
	public void testGetRegionResourceTime() {
		 
		ActionPlugin.Builder pluginBuilder = ActionPlugin.builder();

		Map<MultiKey, MutableDouble> expectedTimes = new LinkedHashMap<>();

		

		pluginBuilder.addAgent("agent");
		pluginBuilder.addAgentActionPlan("agent", new AgentActionPlan(0, (c) -> {
			// establish data views
			
			ResourceDataView resourceDataView = c.getDataView(ResourceDataView.class).get(); 
			StochasticsDataView stochasticsDataView = c.getDataView(StochasticsDataView.class).get();
			RandomGenerator randomGenerator = stochasticsDataView.getRandomGenerator();
			RegionDataView regionDataView = c.getDataView(RegionDataView.class).get();

			// establish the people and resources
			Set<ResourceId> resourceIds = resourceDataView.getResourceIds();
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
				c.resolveEvent(new RegionResourceAdditionEvent(resourceId, regionId, amount));
				expectedTimes.get(new MultiKey(regionId, resourceId)).setValue(c.getTime());
			}

		}));

		// make more resource updates at time 1
		pluginBuilder.addAgentActionPlan("agent", new AgentActionPlan(1, (c) -> {
			// establish data views
			

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
				c.resolveEvent(new RegionResourceAdditionEvent(resourceId, regionId, amount));				
				expectedTimes.get(new MultiKey(regionId, resourceId)).setValue(c.getTime());
			}

		}));

		// make more resource updates at time 2
		pluginBuilder.addAgentActionPlan("agent", new AgentActionPlan(2, (c) -> {
			// establish data views

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
				c.resolveEvent(new RegionResourceAdditionEvent(resourceId, regionId, amount));				
				expectedTimes.get(new MultiKey(regionId, resourceId)).setValue(c.getTime());
			}

		}));

		// test the person resource times
		pluginBuilder.addAgentActionPlan("agent", new AgentActionPlan(3, (c) -> {
			// establish data views

			ResourceDataView resourceDataView = c.getDataView(ResourceDataView.class).get();
			RegionDataView regionDataView = c.getDataView(RegionDataView.class).get();

			// show that the region resource times match expectations
			int actualAssertionsCount = 0;
			for (MultiKey multiKey : expectedTimes.keySet()) {
				RegionId regionId = multiKey.getKey(0);
				ResourceId resourceId = multiKey.getKey(1);
				double expectedTime = expectedTimes.get(multiKey).getValue();
				double actualTime = resourceDataView.getRegionResourceTime(regionId, resourceId);
				assertEquals(expectedTime, actualTime);
				actualAssertionsCount++;
			}
			/*
			 * Show that the number of time values that were tested is equal to
			 * the size of the population times the number of resources
			 */
			int expectedAssertionsCount = regionDataView.getRegionIds().size() * resourceDataView.getResourceIds().size();
			assertEquals(expectedAssertionsCount, actualAssertionsCount);
		}));

		// precondition tests
		pluginBuilder.addAgentActionPlan("agent", new AgentActionPlan(4, (c) -> {
			// establish data views

			ResourceDataView resourceDataView = c.getDataView(ResourceDataView.class).get();

			// if the assignment times for the resource are not tracked
			ContractException contractException = assertThrows(ContractException.class, () -> resourceDataView.getPersonResourceTime(TestResourceId.RESOURCE_2, new PersonId(0)));
			assertEquals(ResourceError.RESOURCE_ASSIGNMENT_TIME_NOT_TRACKED, contractException.getErrorType());

			// if the resource id is null
			contractException = assertThrows(ContractException.class, () -> resourceDataView.getPersonResourceTime(null, new PersonId(0)));
			assertEquals(ResourceError.NULL_RESOURCE_ID, contractException.getErrorType());

			// if the resource id is unknown
			contractException = assertThrows(ContractException.class, () -> resourceDataView.getPersonResourceTime(TestResourceId.getUnknownResourceId(), new PersonId(0)));
			assertEquals(ResourceError.UNKNOWN_RESOURCE_ID, contractException.getErrorType());

			// if the person id null
			contractException = assertThrows(ContractException.class, () -> resourceDataView.getPersonResourceTime(TestResourceId.RESOURCE_1, null));
			assertEquals(PersonError.NULL_PERSON_ID, contractException.getErrorType());

			// if the person id has a negative value
			contractException = assertThrows(ContractException.class, () -> resourceDataView.getPersonResourceTime(TestResourceId.RESOURCE_1, new PersonId(-1)));
			assertEquals(PersonError.UNKNOWN_PERSON_ID, contractException.getErrorType());
			
		}));

		ResourcesActionSupport.testConsumers(30, 6128764970683025350L, pluginBuilder.build());


	}

	@Test
	@UnitTestMethod(name = "getPersonResourceTimeTrackingPolicy", args = { ResourceId.class })
	public void testGetPersonResourceTimeTrackingPolicy() {
		
		ResourcesActionSupport.testConsumer(5, 757175164544632409L, (c) -> {
			ResourceDataView resourceDataView = c.getDataView(ResourceDataView.class).get();
			for (TestResourceId testResourceId : TestResourceId.values()) {
				TimeTrackingPolicy actualPolicy = resourceDataView.getPersonResourceTimeTrackingPolicy(testResourceId);
				TimeTrackingPolicy expectedPolicy = testResourceId.getTimeTrackingPolicy();
				assertEquals(expectedPolicy, actualPolicy);
			}
		});

	}

	@Test
	@UnitTestMethod(name = "getResourceIds", args = {})
	public void testGetResourceIds() {
		 
		ResourcesActionSupport.testConsumer(5, 2601236547109660988L, (c) -> {
			ResourceDataView resourceDataView = c.getDataView(ResourceDataView.class).get();

			// show that the resource ids are the test resource ids
			Set<ResourceId> expectedResourceIds = new LinkedHashSet<>();
			for (TestResourceId testResourceId : TestResourceId.values()) {
				expectedResourceIds.add(testResourceId);
			}
			assertEquals(expectedResourceIds, resourceDataView.getResourceIds());
		});
	}

	@Test
	@UnitTestMethod(name = "resourcePropertyIdExists", args = { ResourceId.class, ResourcePropertyId.class })
	public void testResourcePropertyIdExists() {
		
		ResourcesActionSupport.testConsumer(5, 8074706630609416041L, (c) -> {
			ResourceDataView resourceDataView = c.getDataView(ResourceDataView.class).get();

			// show that the resource property ids that exist are the test
			// resource property ids

			for (TestResourcePropertyId testResourcePropertyId : TestResourcePropertyId.values()) {
				assertTrue(resourceDataView.resourcePropertyIdExists(testResourcePropertyId.getTestResourceId(), testResourcePropertyId));
			}

			assertFalse(resourceDataView.resourcePropertyIdExists(TestResourceId.RESOURCE_1, TestResourcePropertyId.ResourceProperty_2_1_BOOLEAN_MUTABLE));
			assertFalse(resourceDataView.resourcePropertyIdExists(null, TestResourcePropertyId.ResourceProperty_2_1_BOOLEAN_MUTABLE));
			assertFalse(resourceDataView.resourcePropertyIdExists(TestResourceId.RESOURCE_1, null));
			assertFalse(resourceDataView.resourcePropertyIdExists(TestResourceId.RESOURCE_1, TestResourcePropertyId.getUnknownResourcePropertyId()));
			assertFalse(resourceDataView.resourcePropertyIdExists(TestResourceId.getUnknownResourceId(), TestResourcePropertyId.ResourceProperty_1_2_INTEGER_MUTABLE));
			assertFalse(resourceDataView.resourcePropertyIdExists(TestResourceId.getUnknownResourceId(), TestResourcePropertyId.getUnknownResourcePropertyId()));

		});
	}

	@Test
	@UnitTestMethod(name = "resourceIdExists", args = { ResourceId.class })
	public void testResourceIdExists() {
		 
		ResourcesActionSupport.testConsumer(5, 4964974931601945506L, (c) -> {
			ResourceDataView resourceDataView = c.getDataView(ResourceDataView.class).get();

			// show that the resource ids that exist are the test resource ids

			for (TestResourceId testResourceId : TestResourceId.values()) {
				assertTrue(resourceDataView.resourceIdExists(testResourceId));
			}
			assertFalse(resourceDataView.resourceIdExists(TestResourceId.getUnknownResourceId()));
			assertFalse(resourceDataView.resourceIdExists(null));

		});
	}

	@Test
	@UnitTestMethod(name = "getResourcePropertyDefinition", args = { ResourceId.class, ResourcePropertyId.class })
	public void testGetResourcePropertyDefinition() {
		
		ResourcesActionSupport.testConsumer(5, 7619546908709928867L, (c) -> {
			ResourceDataView resourceDataView = c.getDataView(ResourceDataView.class).get();
			// show that each of the resource property definitions from the test
			// resource property enum are present
			for (TestResourcePropertyId testResourcePropertyId : TestResourcePropertyId.values()) {
				PropertyDefinition expectedPropertyDefinition = testResourcePropertyId.getPropertyDefinition();
				PropertyDefinition actualPropertyDefinition = resourceDataView.getResourcePropertyDefinition(testResourcePropertyId.getTestResourceId(), testResourcePropertyId);
				assertEquals(expectedPropertyDefinition, actualPropertyDefinition);
			}
		});
	}

	@Test
	@UnitTestMethod(name = "getResourcePropertyIds", args = { ResourceId.class })
	public void testGetResourcePropertyIds() {
		
		ResourcesActionSupport.testConsumer(5, 1203402714876510055L, (c) -> {
			ResourceDataView resourceDataView = c.getDataView(ResourceDataView.class).get();
			// show that the resource property ids are the test resource
			// property ids
			for (TestResourceId testResourceId : TestResourceId.values()) {
				Set<TestResourcePropertyId> expectedPropertyIds = TestResourcePropertyId.getTestResourcePropertyIds(testResourceId);
				Set<ResourcePropertyId> actualPropertyIds = resourceDataView.getResourcePropertyIds(testResourceId);
				assertEquals(expectedPropertyIds, actualPropertyIds);
			}

			// precondition tests
		 
			// if the resource id is null			
			ContractException contractException = assertThrows(ContractException.class, () -> resourceDataView.getResourcePropertyIds(null));
			assertEquals(ResourceError.NULL_RESOURCE_ID,contractException.getErrorType());

			// if the resource id unknown</li>
			contractException = assertThrows(ContractException.class, () -> resourceDataView.getResourcePropertyIds(TestResourceId.getUnknownResourceId()));
			assertEquals(ResourceError.UNKNOWN_RESOURCE_ID,contractException.getErrorType());
		});
	}

	@Test
	@UnitTestMethod(name = "getResourcePropertyValue", args = { ResourceId.class, ResourcePropertyId.class })
	public void testGetResourcePropertyValue() {
		 
		ResourcesActionSupport.testConsumer(10, 8757871520559824784L, (c) -> {
			ResourceDataView resourceDataView = c.getDataView(ResourceDataView.class).get();
			StochasticsDataView stochasticsDataView = c.getDataView(StochasticsDataView.class).get();
			RandomGenerator randomGenerator = stochasticsDataView.getRandomGenerator();

			// establish the expected values of all resource properties
			Map<MultiKey, Object> expectedValues = new LinkedHashMap<>();
			for (TestResourceId testResourceId : TestResourceId.values()) {
				for (TestResourcePropertyId testResourcePropertyId : TestResourcePropertyId.getTestResourcePropertyIds(testResourceId)) {
					Object propertyValue = resourceDataView.getResourcePropertyValue(testResourceId, testResourcePropertyId);
					expectedValues.put(new MultiKey(testResourceId, testResourcePropertyId), propertyValue);
				}
			}

			// make a few random resource property updates
			int updateCount = 0;
			for (int i = 0; i < 1000; i++) {
				TestResourceId testResourceId = TestResourceId.getRandomResourceId(randomGenerator);
				TestResourcePropertyId testResourcePropertyId = TestResourcePropertyId.getRandomResourcePropertyId(testResourceId, randomGenerator);
				PropertyDefinition resourcePropertyDefinition = resourceDataView.getResourcePropertyDefinition(testResourceId, testResourcePropertyId);
				if (resourcePropertyDefinition.propertyValuesAreMutable()) {
					Object propertyValue = testResourcePropertyId.getRandomPropertyValue(randomGenerator);
					c.resolveEvent(new ResourcePropertyValueAssignmentEvent(testResourceId, testResourcePropertyId, propertyValue));					
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
				Object actualValue = resourceDataView.getResourcePropertyValue(testResourceId, testResourcePropertyId);
				assertEquals(expectedValue, actualValue);
			}

			// precondition tests

			// if the resource id is null
			ContractException contractException = assertThrows(ContractException.class, () -> resourceDataView.getResourcePropertyValue(null, TestResourcePropertyId.ResourceProperty_1_1_BOOLEAN_MUTABLE));
			assertEquals(ResourceError.NULL_RESOURCE_ID,contractException.getErrorType());

			// if the resource id unknown
			contractException = assertThrows(ContractException.class,
					() -> resourceDataView.getResourcePropertyValue(TestResourceId.getUnknownResourceId(), TestResourcePropertyId.ResourceProperty_1_1_BOOLEAN_MUTABLE));
			assertEquals(ResourceError.UNKNOWN_RESOURCE_ID,contractException.getErrorType());
			
			// if the resource property id is null
			contractException = assertThrows(ContractException.class, () -> resourceDataView.getResourcePropertyValue(TestResourceId.RESOURCE_1, null));
			assertEquals(ResourceError.NULL_RESOURCE_PROPERTY_ID,contractException.getErrorType());
			
			// if the resource property id unknown
			contractException = assertThrows(ContractException.class, () -> resourceDataView.getResourcePropertyValue(TestResourceId.RESOURCE_1, TestResourcePropertyId.ResourceProperty_2_1_BOOLEAN_MUTABLE));
			assertEquals(ResourceError.UNKNOWN_RESOURCE_PROPERTY_ID,contractException.getErrorType());
			
			contractException = assertThrows(ContractException.class, () -> resourceDataView.getResourcePropertyValue(TestResourceId.RESOURCE_1, TestResourcePropertyId.getUnknownResourcePropertyId()));
			assertEquals(ResourceError.UNKNOWN_RESOURCE_PROPERTY_ID,contractException.getErrorType());
		});
	}

	@Test
	@UnitTestMethod(name = "getResourcePropertyTime", args = { ResourceId.class, ResourcePropertyId.class })
	public void testGetResourcePropertyTime() {
	 
		ActionPlugin.Builder pluginBuilder = ActionPlugin.builder();

		Map<MultiKey, MutableDouble> expectedTimes = new LinkedHashMap<>();

		pluginBuilder.addAgent("agent");
		pluginBuilder.addAgentActionPlan("agent", new AgentActionPlan(0, (c) -> {
			// establish data views
			ResourceDataView resourceDataView = c.getDataView(ResourceDataView.class).get();
			
			StochasticsDataView stochasticsDataView = c.getDataView(StochasticsDataView.class).get();
			RandomGenerator randomGenerator = stochasticsDataView.getRandomGenerator();

			// establish the resources
			Set<ResourceId> resourceIds = resourceDataView.getResourceIds();

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
				PropertyDefinition propertyDefinition = resourceDataView.getResourcePropertyDefinition(testResourceId, testResourcePropertyId);
				if (propertyDefinition.propertyValuesAreMutable()) {
					Object value = testResourcePropertyId.getRandomPropertyValue(randomGenerator);
					c.resolveEvent(new ResourcePropertyValueAssignmentEvent(testResourceId, testResourcePropertyId, value));					
					expectedTimes.get(new MultiKey(testResourceId, testResourcePropertyId)).setValue(c.getTime());
				}
			}

		}));

		// make more resource property updates at time 1
		pluginBuilder.addAgentActionPlan("agent", new AgentActionPlan(1, (c) -> {
			// establish data views
			ResourceDataView resourceDataView = c.getDataView(ResourceDataView.class).get();
			StochasticsDataView stochasticsDataView = c.getDataView(StochasticsDataView.class).get();
			RandomGenerator randomGenerator = stochasticsDataView.getRandomGenerator();

			// establish the resources
			Set<ResourceId> resourceIds = resourceDataView.getResourceIds();

			// set random values to the resource properties
			for (int i = 0; i < resourceIds.size(); i++) {
				TestResourceId testResourceId = TestResourceId.getRandomResourceId(randomGenerator);
				TestResourcePropertyId testResourcePropertyId = TestResourcePropertyId.getRandomResourcePropertyId(testResourceId, randomGenerator);
				PropertyDefinition propertyDefinition = resourceDataView.getResourcePropertyDefinition(testResourceId, testResourcePropertyId);
				if (propertyDefinition.propertyValuesAreMutable()) {
					Object value = testResourcePropertyId.getRandomPropertyValue(randomGenerator);
					c.resolveEvent(new ResourcePropertyValueAssignmentEvent(testResourceId, testResourcePropertyId, value));					
					expectedTimes.get(new MultiKey(testResourceId, testResourcePropertyId)).setValue(c.getTime());
				}
			}

		}));

		// make more resource property updates at time 2
		pluginBuilder.addAgentActionPlan("agent", new AgentActionPlan(2, (c) -> {
			// establish data views
			ResourceDataView resourceDataView = c.getDataView(ResourceDataView.class).get();
			StochasticsDataView stochasticsDataView = c.getDataView(StochasticsDataView.class).get();
			RandomGenerator randomGenerator = stochasticsDataView.getRandomGenerator();

			// establish the resources
			Set<ResourceId> resourceIds = resourceDataView.getResourceIds();

			// set random values to the resource properties
			for (int i = 0; i < resourceIds.size(); i++) {
				TestResourceId testResourceId = TestResourceId.getRandomResourceId(randomGenerator);
				TestResourcePropertyId testResourcePropertyId = TestResourcePropertyId.getRandomResourcePropertyId(testResourceId, randomGenerator);
				PropertyDefinition propertyDefinition = resourceDataView.getResourcePropertyDefinition(testResourceId, testResourcePropertyId);
				if (propertyDefinition.propertyValuesAreMutable()) {
					Object value = testResourcePropertyId.getRandomPropertyValue(randomGenerator);
					c.resolveEvent(new ResourcePropertyValueAssignmentEvent(testResourceId, testResourcePropertyId, value));					
					expectedTimes.get(new MultiKey(testResourceId, testResourcePropertyId)).setValue(c.getTime());
				}
			}

		}));

		// test the person resource times
		pluginBuilder.addAgentActionPlan("agent", new AgentActionPlan(3, (c) -> {
			// establish data views

			ResourceDataView resourceDataView = c.getDataView(ResourceDataView.class).get();

			// show that the person resource times match expectations
			int actualAssertionsCount = 0;
			for (MultiKey multiKey : expectedTimes.keySet()) {
				ResourceId resourceId = multiKey.getKey(0);
				ResourcePropertyId resourcePropertyId = multiKey.getKey(1);
				double expectedTime = expectedTimes.get(multiKey).getValue();
				double actualTime = resourceDataView.getResourcePropertyTime(resourceId, resourcePropertyId);
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

			ResourceDataView resourceDataView = c.getDataView(ResourceDataView.class).get();

			// if the resource id is null
			ContractException contractException = assertThrows(ContractException.class, () -> resourceDataView.getResourcePropertyTime(null, TestResourcePropertyId.ResourceProperty_1_1_BOOLEAN_MUTABLE));
			assertEquals(ResourceError.NULL_RESOURCE_ID,contractException.getErrorType());
			
			// if the resource id is unknown
			contractException = assertThrows(ContractException.class,
					() -> resourceDataView.getResourcePropertyTime(TestResourceId.getUnknownResourceId(), TestResourcePropertyId.ResourceProperty_1_1_BOOLEAN_MUTABLE));
			assertEquals(ResourceError.UNKNOWN_RESOURCE_ID,contractException.getErrorType());
			
			// if the resource property id is null
			contractException = assertThrows(ContractException.class, () -> resourceDataView.getResourcePropertyTime(TestResourceId.RESOURCE_1, null));
			assertEquals(ResourceError.NULL_RESOURCE_PROPERTY_ID,contractException.getErrorType());
			
			// if the resource property id is unknown
			contractException = assertThrows(ContractException.class,
					() -> resourceDataView.getResourcePropertyTime(TestResourceId.RESOURCE_1, TestResourcePropertyId.ResourceProperty_2_1_BOOLEAN_MUTABLE));
			assertEquals(ResourceError.UNKNOWN_RESOURCE_PROPERTY_ID,contractException.getErrorType());
			
			// if the resource property id is unknown
			contractException = assertThrows(ContractException.class, () -> resourceDataView.getResourcePropertyTime(TestResourceId.RESOURCE_1, TestResourcePropertyId.getUnknownResourcePropertyId()));
			assertEquals(ResourceError.UNKNOWN_RESOURCE_PROPERTY_ID,contractException.getErrorType());

		}));

		ResourcesActionSupport.testConsumers(10, 9211924242349528396L, pluginBuilder.build());

	}
}
