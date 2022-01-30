package plugins.resources.resolvers;

import static org.junit.jupiter.api.Assertions.assertEquals;
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

import nucleus.AgentContext;
import nucleus.Simulation;
import nucleus.Simulation.Builder;
import nucleus.EventLabeler;
import nucleus.NucleusError;
import nucleus.ResolverId;
import nucleus.SimpleResolverId;
import nucleus.testsupport.actionplugin.ActionAgent;
import nucleus.testsupport.actionplugin.ActionError;
import nucleus.testsupport.actionplugin.ActionPlugin;
import nucleus.testsupport.actionplugin.AgentActionPlan;
import nucleus.testsupport.actionplugin.ResolverActionPlan;
import plugins.compartments.CompartmentPlugin;
import plugins.compartments.datacontainers.CompartmentLocationDataView;
import plugins.compartments.initialdata.CompartmentInitialData;
import plugins.compartments.testsupport.TestCompartmentId;
import plugins.components.ComponentPlugin;
import plugins.partitions.PartitionsPlugin;
import plugins.people.PeoplePlugin;
import plugins.people.datacontainers.PersonDataView;
import plugins.people.events.mutation.BulkPersonCreationEvent;
import plugins.people.events.mutation.PersonCreationEvent;
import plugins.people.events.mutation.PersonRemovalRequestEvent;
import plugins.people.events.observation.PersonImminentRemovalObservationEvent;
import plugins.people.initialdata.PeopleInitialData;
import plugins.people.support.BulkPersonContructionData;
import plugins.people.support.PersonContructionData;
import plugins.people.support.PersonError;
import plugins.people.support.PersonId;
import plugins.properties.PropertiesPlugin;
import plugins.properties.support.PropertyDefinition;
import plugins.properties.support.PropertyError;
import plugins.properties.support.TimeTrackingPolicy;
import plugins.regions.RegionPlugin;
import plugins.regions.datacontainers.RegionDataView;
import plugins.regions.datacontainers.RegionLocationDataView;
import plugins.regions.initialdata.RegionInitialData;
import plugins.regions.support.RegionError;
import plugins.regions.support.RegionId;
import plugins.regions.testsupport.TestRegionId;
import plugins.reports.ReportPlugin;
import plugins.reports.initialdata.ReportsInitialData;
import plugins.resources.ResourcesPlugin;
import plugins.resources.datacontainers.ResourceDataView;
import plugins.resources.events.mutation.InterRegionalResourceTransferEvent;
import plugins.resources.events.mutation.PersonResourceRemovalEvent;
import plugins.resources.events.mutation.RegionResourceAdditionEvent;
import plugins.resources.events.mutation.RegionResourceRemovalEvent;
import plugins.resources.events.mutation.ResourcePropertyValueAssignmentEvent;
import plugins.resources.events.mutation.ResourceTransferFromPersonEvent;
import plugins.resources.events.mutation.ResourceTransferToPersonEvent;
import plugins.resources.events.observation.PersonResourceChangeObservationEvent;
import plugins.resources.events.observation.RegionResourceChangeObservationEvent;
import plugins.resources.events.observation.ResourcePropertyChangeObservationEvent;
import plugins.resources.initialdata.ResourceInitialData;
import plugins.resources.support.ResourceError;
import plugins.resources.support.ResourceId;
import plugins.resources.support.ResourceInitialization;
import plugins.resources.support.ResourcePropertyId;
import plugins.resources.testsupport.ResourcesActionSupport;
import plugins.resources.testsupport.TestResourceId;
import plugins.resources.testsupport.TestResourcePropertyId;
import plugins.stochastics.StochasticsPlugin;
import plugins.stochastics.datacontainers.StochasticsDataView;
import plugins.stochastics.initialdata.StochasticsInitialData;
import util.ContractException;
import util.MultiKey;
import util.MutableInteger;
import util.SeedProvider;
import util.annotations.UnitTest;
import util.annotations.UnitTestConstructor;
import util.annotations.UnitTestMethod;

@UnitTest(target = ResourceEventResolver.class)
public final class AT_ResourceEventResolver {

	@Test
	@UnitTestConstructor(args = { ResourceInitialData.class })
	public void testConstructor() {
		ContractException contractException = assertThrows(ContractException.class, () -> new ResourceEventResolver(null));
		assertEquals(ResourceError.NULL_RESOURCE_INITIAL_DATA, contractException.getErrorType());
	}

	@Test
	@UnitTestMethod(name = "init", args = {})
	public void testPopulationGrowthProjectionEvent() {
		/*
		 * nothing to test -- verification can only be done via performance
		 * testing
		 */
	}

	@Test
	@UnitTestMethod(name = "init", args = {})
	public void testInterRegionalResourceTransferEvent() {
		ActionPlugin.Builder pluginBuilder = ActionPlugin.builder();

		Set<MultiKey> actualObservations = new LinkedHashSet<>();
		Set<MultiKey> expectedObservations = new LinkedHashSet<>();

		// create an agent to observe resource transfers
		pluginBuilder.addAgent("observer");
		pluginBuilder.addAgentActionPlan("observer", new AgentActionPlan(0, (c) -> {
			for (TestRegionId testRegionId : TestRegionId.values()) {
				for (TestResourceId testResourceId : TestResourceId.values()) {
					c.subscribe(RegionResourceChangeObservationEvent.getEventLabelByRegionAndResource(c, testRegionId, testResourceId), (c2, e) -> {
						actualObservations.add(new MultiKey(e.getRegionId(), e.getResourceId(), e.getPreviousResourceLevel(), e.getCurrentResourceLevel()));
					});
				}
			}
		}));

		// create an agent that will transfer resources between regions
		pluginBuilder.addAgent("agent");
		pluginBuilder.addAgentActionPlan("agent", new AgentActionPlan(1, (c) -> {

			StochasticsDataView stochasticsDataView = c.getDataView(StochasticsDataView.class).get();
			RandomGenerator randomGenerator = stochasticsDataView.getRandomGenerator();
			ResourceDataView resourceDataView = c.getDataView(ResourceDataView.class).get();

			// add resources to all the regions
			for (TestRegionId testRegionId : TestRegionId.values()) {
				for (TestResourceId testResourceId : TestResourceId.values()) {
					c.resolveEvent(new RegionResourceAdditionEvent(testResourceId, testRegionId, 100L));
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
					long region1Level = resourceDataView.getRegionResourceLevel(regionId1, resourceId);
					// ensure that the first region has a positive amount of the
					// resource
					if (region1Level > 0) {
						// establish the current level of the second region
						long region2Level = resourceDataView.getRegionResourceLevel(regionId2, resourceId);
						// select an amount to transfer
						long amount = randomGenerator.nextInt((int) region1Level) + 1;
						c.resolveEvent(new InterRegionalResourceTransferEvent(resourceId, regionId1, regionId2, amount));
						transfercount++;

						// show that the amount was transferred
						long expectedRegion1Level = region1Level - amount;
						long expectedRegion2Level = region2Level + amount;

						long actualRegion1Level = resourceDataView.getRegionResourceLevel(regionId1, resourceId);
						long actualRegion2Level = resourceDataView.getRegionResourceLevel(regionId2, resourceId);
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
		pluginBuilder.addAgentActionPlan("observer", new AgentActionPlan(2, (c) -> {
			assertEquals(expectedObservations, actualObservations);
		}));

		pluginBuilder.addAgent("agent");
		pluginBuilder.addAgentActionPlan("agent", new AgentActionPlan(3, (c) -> {

			ResourceDataView resourceDataView = c.getDataView(ResourceDataView.class).get();

			// precondition tests
			ResourceId resourceId = TestResourceId.RESOURCE_1;
			RegionId regionId1 = TestRegionId.REGION_1;
			RegionId regionId2 = TestRegionId.REGION_2;
			long amount = 10;

			// add resources to all the regions to ensure the precondition tests
			// will work
			for (TestRegionId testRegionId : TestRegionId.values()) {
				for (TestResourceId testResourceId : TestResourceId.values()) {
					c.resolveEvent(new RegionResourceAdditionEvent(testResourceId, testRegionId, 100L));
				}
			}

			// if the source region is null
			ContractException contractException = assertThrows(ContractException.class, () -> c.resolveEvent(new InterRegionalResourceTransferEvent(resourceId, null, regionId2, amount)));
			assertEquals(RegionError.NULL_REGION_ID, contractException.getErrorType());

			// if the source region is unknown
			contractException = assertThrows(ContractException.class, () -> c.resolveEvent(new InterRegionalResourceTransferEvent(resourceId, TestRegionId.getUnknownRegionId(), regionId2, amount)));
			assertEquals(RegionError.UNKNOWN_REGION_ID, contractException.getErrorType());

			// if the destination region is null
			contractException = assertThrows(ContractException.class, () -> c.resolveEvent(new InterRegionalResourceTransferEvent(resourceId, regionId1, null, amount)));
			assertEquals(RegionError.NULL_REGION_ID, contractException.getErrorType());

			// if the destination region is unknown
			contractException = assertThrows(ContractException.class, () -> c.resolveEvent(new InterRegionalResourceTransferEvent(resourceId, regionId1, TestRegionId.getUnknownRegionId(), amount)));
			assertEquals(RegionError.UNKNOWN_REGION_ID, contractException.getErrorType());

			// if the resource id is null
			contractException = assertThrows(ContractException.class, () -> c.resolveEvent(new InterRegionalResourceTransferEvent(null, regionId1, regionId2, amount)));
			assertEquals(ResourceError.NULL_RESOURCE_ID, contractException.getErrorType());

			// if the resource id is unknown
			contractException = assertThrows(ContractException.class,
					() -> c.resolveEvent(new InterRegionalResourceTransferEvent(TestResourceId.getUnknownResourceId(), regionId1, regionId2, amount)));
			assertEquals(ResourceError.UNKNOWN_RESOURCE_ID, contractException.getErrorType());

			// if the resource amount is negative
			contractException = assertThrows(ContractException.class, () -> c.resolveEvent(new InterRegionalResourceTransferEvent(resourceId, regionId1, regionId2, -1)));
			assertEquals(ResourceError.NEGATIVE_RESOURCE_AMOUNT, contractException.getErrorType());

			// if the source and destination region are equal
			contractException = assertThrows(ContractException.class, () -> c.resolveEvent(new InterRegionalResourceTransferEvent(resourceId, regionId1, regionId1, amount)));
			assertEquals(ResourceError.REFLEXIVE_RESOURCE_TRANSFER, contractException.getErrorType());

			// if the source region does not have sufficient resources to
			// support the transfer
			contractException = assertThrows(ContractException.class, () -> c.resolveEvent(new InterRegionalResourceTransferEvent(resourceId, regionId1, regionId2, 100000L)));
			assertEquals(ResourceError.INSUFFICIENT_RESOURCES_AVAILABLE, contractException.getErrorType());

			// if the transfer will cause a numeric overflow in the destination
			// region

			// fill region 2 to the max long value
			long fillAmount = Long.MAX_VALUE - resourceDataView.getRegionResourceLevel(regionId2, resourceId);
			c.resolveEvent(new RegionResourceAdditionEvent(resourceId, regionId2, fillAmount));
			contractException = assertThrows(ContractException.class, () -> c.resolveEvent(new InterRegionalResourceTransferEvent(resourceId, regionId1, regionId2, amount)));
			assertEquals(ResourceError.RESOURCE_ARITHMETIC_EXCEPTION, contractException.getErrorType());

		}));

		ActionPlugin actionPlugin = pluginBuilder.build();
		ResourcesActionSupport.testConsumers(0, 7976375269741360076L, actionPlugin);
	}

	@Test
	@UnitTestMethod(name = "init", args = {})
	public void testPersonResourceRemovalEvent() {

		ActionPlugin.Builder pluginBuilder = ActionPlugin.builder();

		Set<MultiKey> expectedObservations = new LinkedHashSet<>();
		Set<MultiKey> actualObservations = new LinkedHashSet<>();

		// Have and agent give resources to people
		pluginBuilder.addAgent("agent");
		pluginBuilder.addAgentActionPlan("agent", new AgentActionPlan(0, (c) -> {

			PersonDataView personDataView = c.getDataView(PersonDataView.class).get();
			RegionLocationDataView regionLocationDataView = c.getDataView(RegionLocationDataView.class).get();

			List<PersonId> people = personDataView.getPeople();
			// add resources to all the people
			for (PersonId personId : people) {
				for (TestResourceId testResourceId : TestResourceId.values()) {
					RegionId regionId = regionLocationDataView.getPersonRegion(personId);
					c.resolveEvent(new RegionResourceAdditionEvent(testResourceId, regionId, 100L));
					c.resolveEvent(new ResourceTransferToPersonEvent(testResourceId, personId, 100L));
				}
			}
		}));

		// have an agent observe the changes to person resources
		pluginBuilder.addAgent("observer");
		pluginBuilder.addAgentActionPlan("observer", new AgentActionPlan(1, (c) -> {
			for (TestResourceId testResourceId : TestResourceId.values()) {
				c.subscribe(PersonResourceChangeObservationEvent.getEventLabelByResource(c, testResourceId), (c2, e) -> {
					actualObservations.add(new MultiKey(e.getPersonId(), e.getResourceId(), e.getPreviousResourceLevel(), e.getCurrentResourceLevel()));
				});
			}
		}));

		// Have the agent remove resources from people
		pluginBuilder.addAgentActionPlan("agent", new AgentActionPlan(2, (c) -> {

			StochasticsDataView stochasticsDataView = c.getDataView(StochasticsDataView.class).get();
			RandomGenerator randomGenerator = stochasticsDataView.getRandomGenerator();
			ResourceDataView resourceDataView = c.getDataView(ResourceDataView.class).get();
			PersonDataView personDataView = c.getDataView(PersonDataView.class).get();

			List<PersonId> people = personDataView.getPeople();

			// remove random amounts of resources from people
			int transfercount = 0;
			for (int i = 0; i < 40; i++) {
				// select a random person and resource
				PersonId personId = people.get(randomGenerator.nextInt(people.size()));
				TestResourceId resourceId = TestResourceId.getRandomResourceId(randomGenerator);

				long personResourceLevel = resourceDataView.getPersonResourceLevel(resourceId, personId);
				// ensure that the person has a positive amount of the resource
				if (personResourceLevel > 0) {

					// select an amount to remove
					long amount = randomGenerator.nextInt((int) personResourceLevel) + 1;
					c.resolveEvent(new PersonResourceRemovalEvent(resourceId, personId, amount));
					transfercount++;

					// show that the amount was transferred
					long expectedPersonResourceLevel = personResourceLevel - amount;
					long actualPersonResorceLevel = resourceDataView.getPersonResourceLevel(resourceId, personId);
					assertEquals(expectedPersonResourceLevel, actualPersonResorceLevel);

					expectedObservations.add(new MultiKey(personId, resourceId, personResourceLevel, expectedPersonResourceLevel));

				}

			}

			// show that enough transfers occurred to make a valid test
			assertTrue(transfercount > 10);

		}));

		// Have the observer show that the generated observations were correct
		pluginBuilder.addAgentActionPlan("observer", new AgentActionPlan(3, (c) -> {
			assertEquals(expectedObservations, actualObservations);
		}));

		// Have the agent test preconditions
		pluginBuilder.addAgentActionPlan("agent", new AgentActionPlan(4, (c) -> {
			RegionLocationDataView regionLocationDataView = c.getDataView(RegionLocationDataView.class).get();

			// precondition tests
			ResourceId resourceId = TestResourceId.RESOURCE_1;
			PersonId personId = new PersonId(0);
			long amount = 10;

			// add resource to the person to ensure the precondition tests will
			// work
			RegionId regionId = regionLocationDataView.getPersonRegion(personId);
			c.resolveEvent(new RegionResourceAdditionEvent(resourceId, regionId, 100L));
			c.resolveEvent(new ResourceTransferToPersonEvent(resourceId, personId, 100L));

			// if the person id is null
			ContractException contractException = assertThrows(ContractException.class, () -> c.resolveEvent(new PersonResourceRemovalEvent(resourceId, null, amount)));
			assertEquals(PersonError.NULL_PERSON_ID, contractException.getErrorType());

			// if the person does not exist
			contractException = assertThrows(ContractException.class, () -> c.resolveEvent(new PersonResourceRemovalEvent(resourceId, new PersonId(1000), amount)));
			assertEquals(PersonError.UNKNOWN_PERSON_ID, contractException.getErrorType());

			// if the resource id is null
			contractException = assertThrows(ContractException.class, () -> c.resolveEvent(new PersonResourceRemovalEvent(null, personId, amount)));
			assertEquals(ResourceError.NULL_RESOURCE_ID, contractException.getErrorType());

			// if the resource id is unknown
			contractException = assertThrows(ContractException.class, () -> c.resolveEvent(new PersonResourceRemovalEvent(TestResourceId.getUnknownResourceId(), personId, amount)));
			assertEquals(ResourceError.UNKNOWN_RESOURCE_ID, contractException.getErrorType());

			// if the amount is negative
			contractException = assertThrows(ContractException.class, () -> c.resolveEvent(new PersonResourceRemovalEvent(resourceId, personId, -1)));
			assertEquals(ResourceError.NEGATIVE_RESOURCE_AMOUNT, contractException.getErrorType());

			// if the person does not have the required amount of the resource
			contractException = assertThrows(ContractException.class, () -> c.resolveEvent(new PersonResourceRemovalEvent(resourceId, personId, 10000)));
			assertEquals(ResourceError.INSUFFICIENT_RESOURCES_AVAILABLE, contractException.getErrorType());

		}));

		ActionPlugin actionPlugin = pluginBuilder.build();

		ResourcesActionSupport.testConsumers(50, 6476360369877622233L, actionPlugin);

	}

	@Test
	@UnitTestMethod(name = "init", args = {})
	public void testRegionResourceAdditionEvent() {

		ActionPlugin.Builder pluginBuilder = ActionPlugin.builder();

		Set<MultiKey> expectedObservations = new LinkedHashSet<>();
		Set<MultiKey> actualObservations = new LinkedHashSet<>();

		// Have an agent to observe the resource changes
		pluginBuilder.addAgent("observer");
		pluginBuilder.addAgentActionPlan("observer", new AgentActionPlan(0, (c) -> {
			for (TestRegionId testRegionId : TestRegionId.values()) {
				for (TestResourceId testResourceId : TestResourceId.values()) {
					c.subscribe(RegionResourceChangeObservationEvent.getEventLabelByRegionAndResource(c, testRegionId, testResourceId), (c2, e) -> {
						actualObservations.add(new MultiKey(e.getRegionId(), e.getResourceId(), e.getPreviousResourceLevel(), e.getCurrentResourceLevel()));
					});
				}
			}
		}));

		// Have an agent add resources to regions
		pluginBuilder.addAgent("agent");
		pluginBuilder.addAgentActionPlan("agent", new AgentActionPlan(1, (c) -> {

			StochasticsDataView stochasticsDataView = c.getDataView(StochasticsDataView.class).get();
			RandomGenerator randomGenerator = stochasticsDataView.getRandomGenerator();
			ResourceDataView resourceDataView = c.getDataView(ResourceDataView.class).get();

			// add random amounts of resources to regions
			int transfercount = 0;
			for (int i = 0; i < 40; i++) {
				// select random regions and resources
				TestRegionId regionId = TestRegionId.getRandomRegionId(randomGenerator);
				TestResourceId resourceId = TestResourceId.getRandomResourceId(randomGenerator);
				long regionLevel = resourceDataView.getRegionResourceLevel(regionId, resourceId);

				// select an amount to add
				long amount = randomGenerator.nextInt(100) + 1;
				c.resolveEvent(new RegionResourceAdditionEvent(resourceId, regionId, amount));
				transfercount++;

				// show that the amount was added
				long expectedRegionLevel = regionLevel + amount;
				long actualRegionLevel = resourceDataView.getRegionResourceLevel(regionId, resourceId);
				assertEquals(expectedRegionLevel, actualRegionLevel);

				expectedObservations.add(new MultiKey(regionId, resourceId, regionLevel, expectedRegionLevel));

			}

			// show that enough additions occurred to make a valid test
			assertTrue(transfercount > 10);

		}));

		// have the observer show that the correct observations were generated
		pluginBuilder.addAgentActionPlan("observer", new AgentActionPlan(2, (c) -> {
			assertEquals(expectedObservations, actualObservations);

		}));

		// have the agent test preconditions
		pluginBuilder.addAgentActionPlan("agent", new AgentActionPlan(3, (c) -> {
			// precondition tests
			ResourceId resourceId = TestResourceId.RESOURCE_1;
			RegionId regionId = TestRegionId.REGION_1;
			long amount = 10;

			// if the region id is null
			ContractException contractException = assertThrows(ContractException.class, () -> c.resolveEvent(new RegionResourceAdditionEvent(resourceId, null, amount)));
			assertEquals(RegionError.NULL_REGION_ID, contractException.getErrorType());

			// if the region id is unknown
			contractException = assertThrows(ContractException.class, () -> c.resolveEvent(new RegionResourceAdditionEvent(resourceId, TestRegionId.getUnknownRegionId(), amount)));
			assertEquals(RegionError.UNKNOWN_REGION_ID, contractException.getErrorType());

			// if the resource id is null
			contractException = assertThrows(ContractException.class, () -> c.resolveEvent(new RegionResourceAdditionEvent(null, regionId, amount)));
			assertEquals(ResourceError.NULL_RESOURCE_ID, contractException.getErrorType());

			// if the resource id is unknown
			contractException = assertThrows(ContractException.class, () -> c.resolveEvent(new RegionResourceAdditionEvent(TestResourceId.getUnknownResourceId(), regionId, amount)));
			assertEquals(ResourceError.UNKNOWN_RESOURCE_ID, contractException.getErrorType());

			// if the amount is negative
			contractException = assertThrows(ContractException.class, () -> c.resolveEvent(new RegionResourceAdditionEvent(resourceId, regionId, -1)));
			assertEquals(ResourceError.NEGATIVE_RESOURCE_AMOUNT, contractException.getErrorType());

			// if the addition results in an overflow
			c.resolveEvent(new RegionResourceAdditionEvent(resourceId, regionId, amount));
			contractException = assertThrows(ContractException.class, () -> c.resolveEvent(new RegionResourceAdditionEvent(resourceId, regionId, Long.MAX_VALUE)));
			assertEquals(ResourceError.RESOURCE_ARITHMETIC_EXCEPTION, contractException.getErrorType());

		}));

		ActionPlugin actionPlugin = pluginBuilder.build();

		ResourcesActionSupport.testConsumers(0, 2273638431976256278L, actionPlugin);

	}

	@Test
	@UnitTestMethod(name = "init", args = {})
	public void testRegionResourceRemovalEvent() {

		ActionPlugin.Builder pluginBuilder = ActionPlugin.builder();

		Set<MultiKey> expectedObservations = new LinkedHashSet<>();
		Set<MultiKey> actualObservations = new LinkedHashSet<>();

		pluginBuilder.addAgent("agent");

		// Have the agent add resources to the regions
		pluginBuilder.addAgentActionPlan("agent", new AgentActionPlan(0, (c) -> {

			// add resources to the regions
			for (TestRegionId testRegionId : TestRegionId.values()) {
				for (TestResourceId testResourceId : TestResourceId.values()) {
					c.resolveEvent(new RegionResourceAdditionEvent(testResourceId, testRegionId, 100L));
				}
			}

		}));

		// Have an agent observe the resource being removed from regions
		pluginBuilder.addAgent("observer");
		pluginBuilder.addAgentActionPlan("observer", new AgentActionPlan(1, (c) -> {

			for (TestRegionId testRegionId : TestRegionId.values()) {
				for (TestResourceId testResourceId : TestResourceId.values()) {
					c.subscribe(RegionResourceChangeObservationEvent.getEventLabelByRegionAndResource(c, testRegionId, testResourceId), (c2, e) -> {

						actualObservations.add(new MultiKey(e.getRegionId(), e.getResourceId(), e.getPreviousResourceLevel(), e.getCurrentResourceLevel()));
					});
				}
			}

		}));

		// Have the agent remove resources from regions
		pluginBuilder.addAgentActionPlan("agent", new AgentActionPlan(2, (c) -> {

			StochasticsDataView stochasticsDataView = c.getDataView(StochasticsDataView.class).get();
			RandomGenerator randomGenerator = stochasticsDataView.getRandomGenerator();
			ResourceDataView resourceDataView = c.getDataView(ResourceDataView.class).get();

			// remove random amounts of resources from regions
			int transfercount = 0;
			for (int i = 0; i < 40; i++) {
				// select random regions and resources
				TestRegionId regionId = TestRegionId.getRandomRegionId(randomGenerator);
				TestResourceId resourceId = TestResourceId.getRandomResourceId(randomGenerator);
				long regionLevel = resourceDataView.getRegionResourceLevel(regionId, resourceId);

				if (regionLevel > 0) {
					// select an amount to add
					long amount = randomGenerator.nextInt((int) regionLevel) + 1;
					c.resolveEvent(new RegionResourceRemovalEvent(resourceId, regionId, amount));
					transfercount++;

					// show that the amount was added
					long expectedRegionLevel = regionLevel - amount;
					long actualRegionLevel = resourceDataView.getRegionResourceLevel(regionId, resourceId);
					assertEquals(expectedRegionLevel, actualRegionLevel);

					expectedObservations.add(new MultiKey(regionId, resourceId, regionLevel, expectedRegionLevel));
				}
			}

			// show that enough removals occurred to make a valid test
			assertTrue(transfercount > 10);

		}));

		// Have the observer show that the observations were correctly generated
		pluginBuilder.addAgentActionPlan("observer", new AgentActionPlan(3, (c) -> {
			assertEquals(expectedObservations, actualObservations);
		}));

		// Have the agent test preconditions
		pluginBuilder.addAgentActionPlan("agent", new AgentActionPlan(4, (c) -> {
			// precondition tests
			ResourceId resourceId = TestResourceId.RESOURCE_1;
			RegionId regionId = TestRegionId.REGION_1;
			long amount = 10;

			// if the region id is null
			ContractException contractException = assertThrows(ContractException.class, () -> c.resolveEvent(new RegionResourceRemovalEvent(resourceId, null, amount)));
			assertEquals(RegionError.NULL_REGION_ID, contractException.getErrorType());

			// if the region id is unknown
			contractException = assertThrows(ContractException.class, () -> c.resolveEvent(new RegionResourceRemovalEvent(resourceId, TestRegionId.getUnknownRegionId(), amount)));
			assertEquals(RegionError.UNKNOWN_REGION_ID, contractException.getErrorType());

			// if the resource id is null
			contractException = assertThrows(ContractException.class, () -> c.resolveEvent(new RegionResourceRemovalEvent(null, regionId, amount)));
			assertEquals(ResourceError.NULL_RESOURCE_ID, contractException.getErrorType());

			// if the resource id is unknown
			contractException = assertThrows(ContractException.class, () -> c.resolveEvent(new RegionResourceRemovalEvent(TestResourceId.getUnknownResourceId(), regionId, amount)));
			assertEquals(ResourceError.UNKNOWN_RESOURCE_ID, contractException.getErrorType());

			// if the amount is negative
			contractException = assertThrows(ContractException.class, () -> c.resolveEvent(new RegionResourceRemovalEvent(resourceId, regionId, -1L)));
			assertEquals(ResourceError.NEGATIVE_RESOURCE_AMOUNT, contractException.getErrorType());

			// if the region does not have the required amount of the resource
			contractException = assertThrows(ContractException.class, () -> c.resolveEvent(new RegionResourceRemovalEvent(resourceId, regionId, 10000000L)));
			assertEquals(ResourceError.INSUFFICIENT_RESOURCES_AVAILABLE, contractException.getErrorType());

		}));

		ActionPlugin actionPlugin = pluginBuilder.build();
		ResourcesActionSupport.testConsumers(0, 3784957617927969790L, actionPlugin);

	}

	@Test
	@UnitTestMethod(name = "init", args = {})
	public void testResourcePropertyValueAssignmentEvent() {
		ActionPlugin.Builder pluginBuilder = ActionPlugin.builder();

		Set<MultiKey> expectedObservations = new LinkedHashSet<>();
		Set<MultiKey> actualObservations = new LinkedHashSet<>();

		// Have an agent observe the resource property changes
		pluginBuilder.addAgent("observer");
		pluginBuilder.addAgentActionPlan("observer", new AgentActionPlan(0, (c) -> {

			for (TestResourceId testResourceId : TestResourceId.values()) {
				Set<TestResourcePropertyId> testResourcePropertyIds = TestResourcePropertyId.getTestResourcePropertyIds(testResourceId);
				for (TestResourcePropertyId testResourcePropertyId : testResourcePropertyIds) {
					c.subscribe(ResourcePropertyChangeObservationEvent.getEventLabel(c, testResourceId, testResourcePropertyId), (c2, e) -> {
						actualObservations.add(new MultiKey(e.getResourceId(), e.getResourcePropertyId(), e.getPreviousPropertyValue(), e.getCurrentPropertyValue()));
					});
				}
			}
		}));

		// Have an agent assign resource properties
		pluginBuilder.addAgent("agent");
		pluginBuilder.addAgentActionPlan("agent", new AgentActionPlan(0, (c) -> {

			StochasticsDataView stochasticsDataView = c.getDataView(StochasticsDataView.class).get();
			RandomGenerator randomGenerator = stochasticsDataView.getRandomGenerator();
			ResourceDataView resourceDataView = c.getDataView(ResourceDataView.class).get();

			for (TestResourceId testResourceId : TestResourceId.values()) {
				Set<TestResourcePropertyId> testResourcePropertyIds = TestResourcePropertyId.getTestResourcePropertyIds(testResourceId);
				for (TestResourcePropertyId testResourcePropertyId : testResourcePropertyIds) {
					PropertyDefinition propertyDefinition = resourceDataView.getResourcePropertyDefinition(testResourceId, testResourcePropertyId);
					if (propertyDefinition.propertyValuesAreMutable()) {
						// update the property value
						Object resourcePropertyValue = resourceDataView.getResourcePropertyValue(testResourceId, testResourcePropertyId);
						Object expectedValue = testResourcePropertyId.getRandomPropertyValue(randomGenerator);
						c.resolveEvent(new ResourcePropertyValueAssignmentEvent(testResourceId, testResourcePropertyId, expectedValue));

						// show that the property value was changed
						Object actualValue = resourceDataView.getResourcePropertyValue(testResourceId, testResourcePropertyId);
						assertEquals(expectedValue, actualValue);

						expectedObservations.add(new MultiKey(testResourceId, testResourcePropertyId, resourcePropertyValue, expectedValue));
					}
				}
			}

		}));

		// Have the observer show the the observations were properly generated
		pluginBuilder.addAgentActionPlan("agent", new AgentActionPlan(0, (c) -> {
			assertEquals(expectedObservations, actualObservations);
		}));

		// Have the the agent test preconditions
		pluginBuilder.addAgentActionPlan("agent", new AgentActionPlan(0, (c) -> {

			// precondition tests
			ResourceId resourceId = TestResourceId.RESOURCE_1;
			ResourcePropertyId resourcePropertyId = TestResourcePropertyId.ResourceProperty_1_2_INTEGER_MUTABLE;

			Object value = 10;

			// if the resource id is null
			ContractException contractException = assertThrows(ContractException.class, () -> c.resolveEvent(new ResourcePropertyValueAssignmentEvent(null, resourcePropertyId, value)));
			assertEquals(ResourceError.NULL_RESOURCE_ID, contractException.getErrorType());

			// if the resource id is unknown
			contractException = assertThrows(ContractException.class, () -> c.resolveEvent(new ResourcePropertyValueAssignmentEvent(TestResourceId.getUnknownResourceId(), resourcePropertyId, value)));
			assertEquals(ResourceError.UNKNOWN_RESOURCE_ID, contractException.getErrorType());

			// if the resource property id is null
			contractException = assertThrows(ContractException.class, () -> c.resolveEvent(new ResourcePropertyValueAssignmentEvent(resourceId, null, value)));
			assertEquals(ResourceError.NULL_RESOURCE_PROPERTY_ID, contractException.getErrorType());

			// if the resource property id is unknown
			contractException = assertThrows(ContractException.class,
					() -> c.resolveEvent(new ResourcePropertyValueAssignmentEvent(resourceId, TestResourcePropertyId.getUnknownResourcePropertyId(), value)));
			assertEquals(ResourceError.UNKNOWN_RESOURCE_PROPERTY_ID, contractException.getErrorType());

			// if the resource property value is null
			contractException = assertThrows(ContractException.class, () -> c.resolveEvent(new ResourcePropertyValueAssignmentEvent(resourceId, resourcePropertyId, null)));
			assertEquals(ResourceError.NULL_RESOURCE_PROPERTY_VALUE, contractException.getErrorType());

			// if the resource property value is incompatible with the
			// corresponding property definition
			contractException = assertThrows(ContractException.class, () -> c.resolveEvent(new ResourcePropertyValueAssignmentEvent(resourceId, resourcePropertyId, 23.4)));
			assertEquals(PropertyError.INCOMPATIBLE_VALUE, contractException.getErrorType());

			// if the property has been defined as immutable
			contractException = assertThrows(ContractException.class,
					() -> c.resolveEvent(new ResourcePropertyValueAssignmentEvent(TestResourceId.RESOURCE_5, TestResourcePropertyId.ResourceProperty_5_1_INTEGER_IMMUTABLE, value)));
			assertEquals(PropertyError.IMMUTABLE_VALUE, contractException.getErrorType());

		}));

		ActionPlugin actionPlugin = pluginBuilder.build();
		ResourcesActionSupport.testConsumers(0, 8240654442453940072L, actionPlugin);

	}

	@Test
	@UnitTestMethod(name = "init", args = {})
	public void testResourceTransferFromPersonEvent() {
		ActionPlugin.Builder pluginBuilder = ActionPlugin.builder();

		pluginBuilder.addAgent("agent");
		pluginBuilder.addAgent("observer");

		Set<MultiKey> expectedObservations = new LinkedHashSet<>();
		Set<MultiKey> actualObservations = new LinkedHashSet<>();

		// Have an agent give people resources
		pluginBuilder.addAgentActionPlan("agent", new AgentActionPlan(0, (c) -> {
			PersonDataView personDataView = c.getDataView(PersonDataView.class).get();
			RegionLocationDataView regionLocationDataView = c.getDataView(RegionLocationDataView.class).get();

			List<PersonId> people = personDataView.getPeople();

			// add resources to all people
			for (PersonId personId : people) {
				RegionId regionId = regionLocationDataView.getPersonRegion(personId);
				for (TestResourceId testResourceId : TestResourceId.values()) {
					c.resolveEvent(new RegionResourceAdditionEvent(testResourceId, regionId, 100L));
					c.resolveEvent(new ResourceTransferToPersonEvent(testResourceId, personId, 100L));
				}
			}

		}));

		// Have an agent observe the resource changes
		pluginBuilder.addAgentActionPlan("observer", new AgentActionPlan(1, (c) -> {

			for (TestRegionId testRegionId : TestRegionId.values()) {
				for (TestResourceId testResourceId : TestResourceId.values()) {
					c.subscribe(RegionResourceChangeObservationEvent.getEventLabelByRegionAndResource(c, testRegionId, testResourceId), (c2, e) -> {
						actualObservations.add(new MultiKey(e.getRegionId(), e.getResourceId(), e.getPreviousResourceLevel(), e.getCurrentResourceLevel()));
					});
				}
			}
			for (TestResourceId testResourceId : TestResourceId.values()) {
				c.subscribe(PersonResourceChangeObservationEvent.getEventLabelByResource(c, testResourceId), (c2, e) -> {
					actualObservations.add(new MultiKey(e.getPersonId(), e.getResourceId(), e.getPreviousResourceLevel(), e.getCurrentResourceLevel()));

				});
			}

		}));

		// Have an agent return resources from people back to their regions
		pluginBuilder.addAgentActionPlan("agent", new AgentActionPlan(2, (c) -> {

			StochasticsDataView stochasticsDataView = c.getDataView(StochasticsDataView.class).get();
			RandomGenerator randomGenerator = stochasticsDataView.getRandomGenerator();
			ResourceDataView resourceDataView = c.getDataView(ResourceDataView.class).get();
			PersonDataView personDataView = c.getDataView(PersonDataView.class).get();
			RegionLocationDataView regionLocationDataView = c.getDataView(RegionLocationDataView.class).get();

			List<PersonId> people = personDataView.getPeople();

			int transferCount = 0;
			// transfer resources back to the regions from the people
			for (int i = 0; i < 100; i++) {
				PersonId personId = people.get(randomGenerator.nextInt(people.size()));
				ResourceId resourceId = TestResourceId.getRandomResourceId(randomGenerator);
				RegionId regionId = regionLocationDataView.getPersonRegion(personId);
				long personResourceLevel = resourceDataView.getPersonResourceLevel(resourceId, personId);
				long regionResourceLevel = resourceDataView.getRegionResourceLevel(regionId, resourceId);

				if (personResourceLevel > 0) {
					long amount = randomGenerator.nextInt((int) personResourceLevel);
					long expectedPersonResourceLevel = personResourceLevel - amount;
					long expectedRegionResourceLevel = regionResourceLevel + amount;
					c.resolveEvent(new ResourceTransferFromPersonEvent(resourceId, personId, amount));
					transferCount++;
					long actualPersonResourceLevel = resourceDataView.getPersonResourceLevel(resourceId, personId);
					long actualRegionResourceLevel = resourceDataView.getRegionResourceLevel(regionId, resourceId);
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
		pluginBuilder.addAgentActionPlan("observer", new AgentActionPlan(3, (c) -> {
			assertEquals(expectedObservations, actualObservations);
		}));

		// Have an agent test preconditions
		pluginBuilder.addAgentActionPlan("agent", new AgentActionPlan(4, (c) -> {

			RegionLocationDataView regionLocationDataView = c.getDataView(RegionLocationDataView.class).get();
			ResourceDataView resourceDataView = c.getDataView(ResourceDataView.class).get();
			// precondition tests
			PersonId personId = new PersonId(0);
			ResourceId resourceId = TestResourceId.RESOURCE_4;
			long amount = 10;

			// add resources to the person to support the precondition tests

			RegionId regionId = regionLocationDataView.getPersonRegion(personId);
			for (TestResourceId testResourceId : TestResourceId.values()) {
				c.resolveEvent(new RegionResourceAdditionEvent(testResourceId, regionId, 100L));
				c.resolveEvent(new ResourceTransferToPersonEvent(testResourceId, personId, 100L));
			}

			// if the person id is null
			ContractException contractException = assertThrows(ContractException.class, () -> c.resolveEvent(new ResourceTransferFromPersonEvent(resourceId, null, amount)));
			assertEquals(PersonError.NULL_PERSON_ID, contractException.getErrorType());

			// if the person does not exist
			contractException = assertThrows(ContractException.class, () -> c.resolveEvent(new ResourceTransferFromPersonEvent(resourceId, new PersonId(3434), amount)));
			assertEquals(PersonError.UNKNOWN_PERSON_ID, contractException.getErrorType());

			// if the resource id is null
			contractException = assertThrows(ContractException.class, () -> c.resolveEvent(new ResourceTransferFromPersonEvent(null, personId, amount)));
			assertEquals(ResourceError.NULL_RESOURCE_ID, contractException.getErrorType());

			// if the resource id is unknown
			contractException = assertThrows(ContractException.class, () -> c.resolveEvent(new ResourceTransferFromPersonEvent(TestResourceId.getUnknownResourceId(), personId, amount)));
			assertEquals(ResourceError.UNKNOWN_RESOURCE_ID, contractException.getErrorType());

			// if the amount is negative
			contractException = assertThrows(ContractException.class, () -> c.resolveEvent(new ResourceTransferFromPersonEvent(resourceId, personId, -1L)));
			assertEquals(ResourceError.NEGATIVE_RESOURCE_AMOUNT, contractException.getErrorType());

			// if the person does not have the required amount of the resource
			contractException = assertThrows(ContractException.class, () -> c.resolveEvent(new ResourceTransferFromPersonEvent(resourceId, personId, 1000000)));
			assertEquals(ResourceError.INSUFFICIENT_RESOURCES_AVAILABLE, contractException.getErrorType());

			// if the transfer results in an overflow of the region's resource
			// level

			// fill the region
			long regionResourceLevel = resourceDataView.getRegionResourceLevel(regionId, resourceId);
			c.resolveEvent(new RegionResourceRemovalEvent(resourceId, regionId, regionResourceLevel));
			c.resolveEvent(new RegionResourceAdditionEvent(resourceId, regionId, Long.MAX_VALUE));

			// empty the person
			long personResourceLevel = resourceDataView.getPersonResourceLevel(resourceId, personId);
			c.resolveEvent(new PersonResourceRemovalEvent(resourceId, personId, personResourceLevel));

			// transfer the region's inventory to the person
			c.resolveEvent(new ResourceTransferToPersonEvent(resourceId, personId, Long.MAX_VALUE));

			// add one more unit to the region
			c.resolveEvent(new RegionResourceAdditionEvent(resourceId, regionId, 1L));

			// attempt to transfer the person's inventory back to the region

			contractException = assertThrows(ContractException.class, () -> c.resolveEvent(new ResourceTransferFromPersonEvent(resourceId, personId, Long.MAX_VALUE)));
			assertEquals(ResourceError.RESOURCE_ARITHMETIC_EXCEPTION, contractException.getErrorType());

		}));

		ActionPlugin actionPlugin = pluginBuilder.build();

		ResourcesActionSupport.testConsumers(30, 3166011813977431605L, actionPlugin);

	}

	@Test
	@UnitTestMethod(name = "init", args = {})
	public void testResourceTransferToPersonEvent() {
		ActionPlugin.Builder pluginBuilder = ActionPlugin.builder();

		pluginBuilder.addAgent("agent");
		pluginBuilder.addAgent("observer");

		Set<MultiKey> expectedObservations = new LinkedHashSet<>();
		Set<MultiKey> actualObservations = new LinkedHashSet<>();

		// Have an agent add resources to the regions
		pluginBuilder.addAgentActionPlan("agent", new AgentActionPlan(0, (c) -> {
			PersonDataView personDataView = c.getDataView(PersonDataView.class).get();
			RegionLocationDataView regionLocationDataView = c.getDataView(RegionLocationDataView.class).get();

			List<PersonId> people = personDataView.getPeople();

			// add resources to all regions
			for (PersonId personId : people) {
				RegionId regionId = regionLocationDataView.getPersonRegion(personId);
				for (TestResourceId testResourceId : TestResourceId.values()) {
					c.resolveEvent(new RegionResourceAdditionEvent(testResourceId, regionId, 1000L));
				}
			}

		}));

		// Have an agent observe the resource transfers
		pluginBuilder.addAgentActionPlan("observer", new AgentActionPlan(1, (c) -> {

			for (TestRegionId testRegionId : TestRegionId.values()) {
				for (TestResourceId testResourceId : TestResourceId.values()) {
					c.subscribe(RegionResourceChangeObservationEvent.getEventLabelByRegionAndResource(c, testRegionId, testResourceId), (c2, e) -> {
						actualObservations.add(new MultiKey(e.getRegionId(), e.getResourceId(), e.getPreviousResourceLevel(), e.getCurrentResourceLevel()));
					});
				}
			}
			for (TestResourceId testResourceId : TestResourceId.values()) {
				c.subscribe(PersonResourceChangeObservationEvent.getEventLabelByResource(c, testResourceId), (c2, e) -> {
					actualObservations.add(new MultiKey(e.getPersonId(), e.getResourceId(), e.getPreviousResourceLevel(), e.getCurrentResourceLevel()));

				});
			}

		}));

		// Have an agent transfer the resources to people
		pluginBuilder.addAgentActionPlan("agent", new AgentActionPlan(2, (c) -> {

			StochasticsDataView stochasticsDataView = c.getDataView(StochasticsDataView.class).get();
			RandomGenerator randomGenerator = stochasticsDataView.getRandomGenerator();
			ResourceDataView resourceDataView = c.getDataView(ResourceDataView.class).get();
			PersonDataView personDataView = c.getDataView(PersonDataView.class).get();
			RegionLocationDataView regionLocationDataView = c.getDataView(RegionLocationDataView.class).get();

			List<PersonId> people = personDataView.getPeople();

			int transferCount = 0;
			// transfer resources back to the people
			for (int i = 0; i < 100; i++) {
				PersonId personId = people.get(randomGenerator.nextInt(people.size()));
				ResourceId resourceId = TestResourceId.getRandomResourceId(randomGenerator);
				RegionId regionId = regionLocationDataView.getPersonRegion(personId);
				long personResourceLevel = resourceDataView.getPersonResourceLevel(resourceId, personId);
				long regionResourceLevel = resourceDataView.getRegionResourceLevel(regionId, resourceId);

				if (regionResourceLevel > 0) {
					long amount = randomGenerator.nextInt((int) regionResourceLevel);
					long expectedPersonResourceLevel = personResourceLevel + amount;
					long expectedRegionResourceLevel = regionResourceLevel - amount;
					c.resolveEvent(new ResourceTransferToPersonEvent(resourceId, personId, amount));
					transferCount++;
					long actualPersonResourceLevel = resourceDataView.getPersonResourceLevel(resourceId, personId);
					long actualRegionResourceLevel = resourceDataView.getRegionResourceLevel(regionId, resourceId);
					assertEquals(expectedPersonResourceLevel, actualPersonResourceLevel);
					assertEquals(expectedRegionResourceLevel, actualRegionResourceLevel);

					expectedObservations.add(new MultiKey(regionId, resourceId, regionResourceLevel, expectedRegionResourceLevel));
					expectedObservations.add(new MultiKey(personId, resourceId, personResourceLevel, expectedPersonResourceLevel));

				}
			}

			// show that a reasonable number of transfers occurred
			assertTrue(transferCount > 20);

		}));

		// Have an agent show that the proper observations were generated
		pluginBuilder.addAgentActionPlan("observer", new AgentActionPlan(3, (c) -> {
			assertEquals(expectedObservations, actualObservations);
		}));

		// Have an agent test preconditions
		pluginBuilder.addAgentActionPlan("agent", new AgentActionPlan(4, (c) -> {

			ResourceDataView resourceDataView = c.getDataView(ResourceDataView.class).get();

			RegionLocationDataView regionLocationDataView = c.getDataView(RegionLocationDataView.class).get();

			// precondition tests
			PersonId personId = new PersonId(0);
			ResourceId resourceId = TestResourceId.RESOURCE_4;
			long amount = 10;

			// add resources to the region to support the precondition tests

			RegionId regionId = regionLocationDataView.getPersonRegion(personId);
			for (TestResourceId testResourceId : TestResourceId.values()) {
				c.resolveEvent(new RegionResourceAdditionEvent(testResourceId, regionId, 100L));
			}

			// if the person id is null
			ContractException contractException = assertThrows(ContractException.class, () -> c.resolveEvent(new ResourceTransferToPersonEvent(resourceId, null, amount)));
			assertEquals(PersonError.NULL_PERSON_ID, contractException.getErrorType());

			// if the person does not exist
			contractException = assertThrows(ContractException.class, () -> c.resolveEvent(new ResourceTransferToPersonEvent(resourceId, new PersonId(3434), amount)));
			assertEquals(PersonError.UNKNOWN_PERSON_ID, contractException.getErrorType());

			// if the resource id is null
			contractException = assertThrows(ContractException.class, () -> c.resolveEvent(new ResourceTransferToPersonEvent(null, personId, amount)));
			assertEquals(ResourceError.NULL_RESOURCE_ID, contractException.getErrorType());

			// if the resource id is unknown
			contractException = assertThrows(ContractException.class, () -> c.resolveEvent(new ResourceTransferToPersonEvent(TestResourceId.getUnknownResourceId(), personId, amount)));
			assertEquals(ResourceError.UNKNOWN_RESOURCE_ID, contractException.getErrorType());

			// if the amount is negative
			contractException = assertThrows(ContractException.class, () -> c.resolveEvent(new ResourceTransferToPersonEvent(resourceId, personId, -1L)));
			assertEquals(ResourceError.NEGATIVE_RESOURCE_AMOUNT, contractException.getErrorType());

			// if the region does not have the required amount of the resource
			contractException = assertThrows(ContractException.class, () -> c.resolveEvent(new ResourceTransferToPersonEvent(resourceId, personId, 1000000)));
			assertEquals(ResourceError.INSUFFICIENT_RESOURCES_AVAILABLE, contractException.getErrorType());

			// if the transfer results in an overflow of the person's resource
			// level

			// fill the region
			long regionResourceLevel = resourceDataView.getRegionResourceLevel(regionId, resourceId);
			c.resolveEvent(new RegionResourceRemovalEvent(resourceId, regionId, regionResourceLevel));
			c.resolveEvent(new RegionResourceAdditionEvent(resourceId, regionId, Long.MAX_VALUE));

			// empty the person
			long personResourceLevel = resourceDataView.getPersonResourceLevel(resourceId, personId);
			c.resolveEvent(new PersonResourceRemovalEvent(resourceId, personId, personResourceLevel));

			// transfer the region's inventory to the person
			c.resolveEvent(new ResourceTransferToPersonEvent(resourceId, personId, Long.MAX_VALUE));

			// add one more unit to the region
			c.resolveEvent(new RegionResourceAdditionEvent(resourceId, regionId, 1L));

			// attempt to transfer the on unit to the person

			contractException = assertThrows(ContractException.class, () -> c.resolveEvent(new ResourceTransferToPersonEvent(resourceId, personId, 1L)));
			assertEquals(ResourceError.RESOURCE_ARITHMETIC_EXCEPTION, contractException.getErrorType());

		}));

		ActionPlugin actionPlugin = pluginBuilder.build();

		ResourcesActionSupport.testConsumers(30, 3808042869854225459L, actionPlugin);

	}

	@Test
	@UnitTestMethod(name = "init", args = {})
	public void testPersonCreationObservationEvent() {

		// Have an agent create a few people with random resource levels
		ResourcesActionSupport.testConsumer(0, 5441878385875188805L, (c) -> {
			ResourceDataView resourceDataView = c.getDataView(ResourceDataView.class).get();
			PersonDataView personDataView = c.getDataView(PersonDataView.class).get();
			StochasticsDataView stochasticsDataView = c.getDataView(StochasticsDataView.class).get();
			RandomGenerator randomGenerator = stochasticsDataView.getRandomGenerator();

			// create 30 people, testing each in turn for their resource levels
			for (int i = 0; i < 30; i++) {
				PersonContructionData.Builder builder = PersonContructionData.builder();
				// give the person a region and compartment
				builder.add(TestCompartmentId.getRandomCompartmentId(randomGenerator));
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
				// PersonCreationObservationEvent
				c.resolveEvent(new PersonCreationEvent(builder.build()));
				PersonId personId = personDataView.getLastIssuedPersonId().get();

				// show that the person has the correct resource levels
				for (TestResourceId testResourceId : TestResourceId.values()) {
					int actualPersonResourceLevel = (int) resourceDataView.getPersonResourceLevel(testResourceId, personId);
					int expectedPersonResourceLevel = expectedResources.get(testResourceId).getValue();
					assertEquals(expectedPersonResourceLevel, actualPersonResourceLevel);
				}
			}

			// precondition tests

			/*
			 * Precondition tests for the validity of the person id are shadowed
			 * by other plugins and cannot be easily tested
			 */

			/*
			 * if the auxiliary data contains a ResourceInitialization that has
			 * a null resource id
			 */

			ContractException contractException = assertThrows(ContractException.class, () -> {
				c.resolveEvent(new PersonCreationEvent(PersonContructionData.builder().add(TestCompartmentId.getRandomCompartmentId(randomGenerator))
																			.add(TestRegionId.getRandomRegionId(randomGenerator)).add(new ResourceInitialization(null, 15L)).build()));
			});
			assertEquals(ResourceError.NULL_RESOURCE_ID, contractException.getErrorType());

			/*
			 * if the auxiliary data contains a ResourceInitialization that has
			 * an unknown resource id
			 */
			contractException = assertThrows(ContractException.class, () -> {
				c.resolveEvent(
						new PersonCreationEvent(PersonContructionData	.builder().add(TestCompartmentId.getRandomCompartmentId(randomGenerator)).add(TestRegionId.getRandomRegionId(randomGenerator))
																		.add(new ResourceInitialization(TestResourceId.getUnknownResourceId(), 15L)).build()));
			});
			assertEquals(ResourceError.UNKNOWN_RESOURCE_ID, contractException.getErrorType());

			/*
			 * if the auxiliary data contains a ResourceInitialization that has
			 * a negative resource level
			 */
			contractException = assertThrows(ContractException.class, () -> {
				c.resolveEvent(
						new PersonCreationEvent(PersonContructionData	.builder().add(TestCompartmentId.getRandomCompartmentId(randomGenerator)).add(TestRegionId.getRandomRegionId(randomGenerator))
																		.add(new ResourceInitialization(TestResourceId.RESOURCE_1, -15L)).build()));
			});
			assertEquals(ResourceError.NEGATIVE_RESOURCE_AMOUNT, contractException.getErrorType());

		});

	}

	@Test
	@UnitTestMethod(name = "init", args = {})
	public void testBulkPersonCreationObservationEvent() {

		// Have an agent create a few people with random resource levels in a
		// bulk construction request
		ResourcesActionSupport.testConsumer(0, 1373835434254978465L, (c) -> {
			ResourceDataView resourceDataView = c.getDataView(ResourceDataView.class).get();
			PersonDataView personDataView = c.getDataView(PersonDataView.class).get();
			StochasticsDataView stochasticsDataView = c.getDataView(StochasticsDataView.class).get();
			RandomGenerator randomGenerator = stochasticsDataView.getRandomGenerator();

			// prepare builders
			BulkPersonContructionData.Builder bulkBuilder = BulkPersonContructionData.builder();
			PersonContructionData.Builder personBuilder = PersonContructionData.builder();
			// create a map to hold expected resource levels
			Map<Integer, Map<ResourceId, MutableInteger>> expectedResources = new LinkedHashMap<>();

			int numberOfPeople = 30;

			// add the people to the construction data
			for (int i = 0; i < numberOfPeople; i++) {
				// build the map of expected resources for the person
				Map<ResourceId, MutableInteger> expectationForPerson = new LinkedHashMap<>();
				expectedResources.put(i, expectationForPerson);
				// assign a region and compartment to the person
				personBuilder.add(TestCompartmentId.getRandomCompartmentId(randomGenerator));
				personBuilder.add(TestRegionId.getRandomRegionId(randomGenerator));
				// give the person a positive resource level for about half of
				// the resources
				for (TestResourceId testResourceId : TestResourceId.values()) {
					MutableInteger mutableInteger = new MutableInteger();
					expectationForPerson.put(testResourceId, mutableInteger);
					if (randomGenerator.nextBoolean()) {
						int amount = randomGenerator.nextInt(30) + 1;
						mutableInteger.setValue(amount);
						ResourceInitialization resourceInitialization = new ResourceInitialization(testResourceId, (long) amount);
						personBuilder.add(resourceInitialization);
					}
				}
				bulkBuilder.add(personBuilder.build());
			}

			/*
			 * derive the offset for person ids since the
			 * BulkPersonCreationEvent will not carry person ids. We have to
			 * derive it in context here since we will not get the benefit of
			 * actually seeing the observation event directly
			 */
			int personIdOffset = 0;
			if (personDataView.getLastIssuedPersonId().isPresent()) {
				PersonId personId = personDataView.getLastIssuedPersonId().get();
				personIdOffset = personId.getValue();
			}

			// create the people which will in turn generate the
			// BulkPersonCreationObservationEvent
			c.resolveEvent(new BulkPersonCreationEvent(bulkBuilder.build()));

			// show that each person has the correct resource levels
			for (int i = 0; i < numberOfPeople; i++) {
				Map<ResourceId, MutableInteger> expectationForPerson = expectedResources.get(i);
				PersonId personId = new PersonId(personIdOffset + i);
				for (TestResourceId testResourceId : TestResourceId.values()) {
					int actualPersonResourceLevel = (int) resourceDataView.getPersonResourceLevel(testResourceId, personId);
					int expectedPersonResourceLevel = expectationForPerson.get(testResourceId).getValue();
					assertEquals(expectedPersonResourceLevel, actualPersonResourceLevel);
				}
			}

			// precondition tests

			/*
			 * Precondition tests for the validity of the person id are shadowed
			 * by other plugins and cannot be easily tested
			 */

			/*
			 * if the auxiliary data contains a ResourceInitialization that has
			 * a null resource id
			 */

			ContractException contractException = assertThrows(ContractException.class, () -> {

				PersonContructionData personContructionData = PersonContructionData	.builder()//
																					.add(TestCompartmentId.getRandomCompartmentId(randomGenerator))//
																					.add(TestRegionId.getRandomRegionId(randomGenerator))//
																					.add(new ResourceInitialization(null, 15L))//
																					.build();//
				BulkPersonContructionData bulkPersonContructionData = BulkPersonContructionData.builder().add(personContructionData).build();
				BulkPersonCreationEvent bulkPersonCreationEvent = new BulkPersonCreationEvent(bulkPersonContructionData);
				c.resolveEvent(bulkPersonCreationEvent);
			});
			assertEquals(ResourceError.NULL_RESOURCE_ID, contractException.getErrorType());

			/*
			 * if the auxiliary data contains a ResourceInitialization that has
			 * an unknown resource id
			 */
			contractException = assertThrows(ContractException.class, () -> {

				PersonContructionData personContructionData = PersonContructionData	.builder()//
																					.add(TestCompartmentId.getRandomCompartmentId(randomGenerator))//
																					.add(TestRegionId.getRandomRegionId(randomGenerator))//
																					.add(new ResourceInitialization(TestResourceId.getUnknownResourceId(), 15L))//
																					.build();//
				BulkPersonContructionData bulkPersonContructionData = BulkPersonContructionData.builder().add(personContructionData).build();
				BulkPersonCreationEvent bulkPersonCreationEvent = new BulkPersonCreationEvent(bulkPersonContructionData);
				c.resolveEvent(bulkPersonCreationEvent);
			});
			assertEquals(ResourceError.UNKNOWN_RESOURCE_ID, contractException.getErrorType());

			/*
			 * if the auxiliary data contains a ResourceInitialization that has
			 * a negative resource level
			 */
			contractException = assertThrows(ContractException.class, () -> {

				PersonContructionData personContructionData = PersonContructionData	.builder()//
																					.add(TestCompartmentId.getRandomCompartmentId(randomGenerator))//
																					.add(TestRegionId.getRandomRegionId(randomGenerator))//
																					.add(new ResourceInitialization(TestResourceId.RESOURCE_1, -15L))//
																					.build();//
				BulkPersonContructionData bulkPersonContructionData = BulkPersonContructionData.builder().add(personContructionData).build();
				BulkPersonCreationEvent bulkPersonCreationEvent = new BulkPersonCreationEvent(bulkPersonContructionData);
				c.resolveEvent(bulkPersonCreationEvent);
			});
			assertEquals(ResourceError.NEGATIVE_RESOURCE_AMOUNT, contractException.getErrorType());

		});

	}

	@Test
	@UnitTestMethod(name = "init", args = {})
	public void testPersonImminentRemovalObservationEvent() {
		ActionPlugin.Builder pluginBuilder = ActionPlugin.builder();

		/*
		 * Create a resolver that will demonstrate that the
		 * PersonImminentRemovalObservationEvent does not cause the immediate
		 * removal of the person's resource data. We know that this resolver
		 * will execute its handler AFTER the resolver in the Resource plugin
		 * due to the behavior of the ResourcesActionSupport class.
		 */
		ResolverId resolverId = new SimpleResolverId("test resolver");
		pluginBuilder.addResolver(resolverId);
		pluginBuilder.addResolverActionPlan(resolverId, new ResolverActionPlan(0, (c) -> {
			c.subscribeToEventExecutionPhase(PersonImminentRemovalObservationEvent.class, (c2, e) -> {
				ResourceDataView resourceDataView = c2.getDataView(ResourceDataView.class).get();
				long personResourceLevel = resourceDataView.getPersonResourceLevel(TestResourceId.RESOURCE_1, e.getPersonId());
				assertTrue(personResourceLevel > 0);
			});
		}));

		// Have an agent add a person with resources and then remove that person
		pluginBuilder.addAgent("agent");
		pluginBuilder.addAgentActionPlan("agent", new AgentActionPlan(0, (c) -> {

			PersonDataView personDataView = c.getDataView(PersonDataView.class).get();

			// create a person and set their resources
			PersonContructionData personContructionData = PersonContructionData	.builder()//
																				.add(TestRegionId.REGION_1)//
																				.add(TestCompartmentId.COMPARTMENT_1)//
																				.add(new ResourceInitialization(TestResourceId.RESOURCE_1, 15L))//
																				.build();//

			c.resolveEvent(new PersonCreationEvent(personContructionData));
			PersonId personId = personDataView.getLastIssuedPersonId().get();

			c.resolveEvent(new PersonRemovalRequestEvent(personId));

			// show that the person still exists
			assertTrue(personDataView.personExists(personId));
		}));

		// Have the agent show that the person does not exist and there are no
		// resources for that person. This is done at the same time as the
		// person removal, but due to ordering will execute after the person is
		// fully eliminated
		pluginBuilder.addAgentActionPlan("agent", new AgentActionPlan(0, (c) -> {

			PersonDataView personDataView = c.getDataView(PersonDataView.class).get();
			ResourceDataView resourceDataView = c.getDataView(ResourceDataView.class).get();

			PersonId personId = personDataView.getLastIssuedPersonId().get();

			ContractException contractException = assertThrows(ContractException.class, () -> resourceDataView.getPersonResourceLevel(TestResourceId.RESOURCE_1, personId));
			assertEquals(PersonError.UNKNOWN_PERSON_ID, contractException.getErrorType());

		}));

		ActionPlugin actionPlugin = pluginBuilder.build();
		ResourcesActionSupport.testConsumers(0, 5231820238498733928L, actionPlugin);

	}

	@Test
	@UnitTestMethod(name = "init", args = {})
	public void testInitializeResourceDataView() {

		int initialPopulation = 10;

		RandomGenerator randomGenerator = SeedProvider.getRandomGenerator(1828556358289827784L);

		// create a list of people
		List<PersonId> people = new ArrayList<>();
		for (int i = 0; i < initialPopulation; i++) {
			people.add(new PersonId(i));
		}

		Builder builder = Simulation.builder();

		// add the resources plugin
		ResourceInitialData.Builder resourcesBuilder = ResourceInitialData.builder();

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

		ResourceInitialData resourceInitialData = resourcesBuilder.build();

		builder.addPlugin(ResourcesPlugin.PLUGIN_ID, new ResourcesPlugin(resourceInitialData)::init);

		// add the partitions plugin
		builder.addPlugin(PartitionsPlugin.PLUGIN_ID, new PartitionsPlugin()::init);

		// add the people plugin

		PeopleInitialData.Builder peopleBuilder = PeopleInitialData.builder();

		for (PersonId personId : people) {
			peopleBuilder.addPersonId(personId);
		}

		builder.addPlugin(PeoplePlugin.PLUGIN_ID, new PeoplePlugin(peopleBuilder.build())::init);

		// add the properties plugin
		builder.addPlugin(PropertiesPlugin.PLUGIN_ID, new PropertiesPlugin()::init);

		// add the compartments plugin
		CompartmentInitialData.Builder compartmentsBuilder = CompartmentInitialData.builder();
		for (TestCompartmentId testCompartmentId : TestCompartmentId.values()) {
			compartmentsBuilder.setCompartmentInitialBehaviorSupplier(testCompartmentId, () -> new ActionAgent(testCompartmentId)::init);
		}

		for (PersonId personId : people) {
			compartmentsBuilder.setPersonCompartment(personId, TestCompartmentId.getRandomCompartmentId(randomGenerator));
		}

		builder.addPlugin(CompartmentPlugin.PLUGIN_ID, new CompartmentPlugin(compartmentsBuilder.build())::init);

		// add the regions plugin
		RegionInitialData.Builder regionsBuilder = RegionInitialData.builder();
		for (TestRegionId testRegionId : TestRegionId.values()) {
			regionsBuilder.setRegionComponentInitialBehaviorSupplier(testRegionId, () -> new ActionAgent(testRegionId)::init);
		}
		for (PersonId personId : people) {
			regionsBuilder.setPersonRegion(personId, TestRegionId.getRandomRegionId(randomGenerator));
		}

		builder.addPlugin(RegionPlugin.PLUGIN_ID, new RegionPlugin(regionsBuilder.build())::init);

		// add the report plugin

		builder.addPlugin(ReportPlugin.PLUGIN_ID, new ReportPlugin(ReportsInitialData.builder().build())::init);

		// add the component plugin
		builder.addPlugin(ComponentPlugin.PLUGIN_ID, new ComponentPlugin()::init);

		// add the stochastics plugin
		builder.addPlugin(StochasticsPlugin.PLUGIN_ID, new StochasticsPlugin(StochasticsInitialData.builder().setSeed(randomGenerator.nextLong()).build())::init);

		ActionPlugin.Builder pluginBuilder = ActionPlugin.builder();

		pluginBuilder.addAgent("agent");
		pluginBuilder.addAgentActionPlan("agent", new AgentActionPlan(0, (c) -> {

			ResourceDataView resourceDataView = c.getDataView(ResourceDataView.class).get();
			PersonDataView personDataView = c.getDataView(PersonDataView.class).get();
			RegionDataView regionDataView = c.getDataView(RegionDataView.class).get();

			List<PersonId> personIds = personDataView.getPeople();
			assertEquals(new LinkedHashSet<>(personIds), resourceInitialData.getPersonIds());

			Set<RegionId> expectedRegionIds = regionDataView.getRegionIds();
			Set<RegionId> actualRegionIds = resourceInitialData.getRegionIds();
			assertEquals(expectedRegionIds, actualRegionIds);

			for (RegionId regionId : resourceInitialData.getRegionIds()) {
				for (ResourceId resourceId : resourceInitialData.getResourceIds()) {
					long expectedRegionResourceLevel = resourceInitialData.getRegionResourceLevel(regionId, resourceId);
					long actualRegionResourceLevel = resourceDataView.getRegionResourceLevel(regionId, resourceId);
					assertEquals(expectedRegionResourceLevel, actualRegionResourceLevel);
				}
			}

			for (ResourceId resourceId : resourceInitialData.getResourceIds()) {
				TimeTrackingPolicy expectedPolicy = resourceInitialData.getPersonResourceTimeTrackingPolicy(resourceId);
				TimeTrackingPolicy actualPolicy = resourceDataView.getPersonResourceTimeTrackingPolicy(resourceId);
				assertEquals(expectedPolicy, actualPolicy);
			}

			assertEquals(resourceInitialData.getResourceIds(), resourceDataView.getResourceIds());

			for (PersonId personId : personIds) {
				for (ResourceId resourceId : resourceInitialData.getResourceIds()) {
					long expectedPersonResourceLevel = resourceInitialData.getPersonResourceLevel(personId, resourceId);
					long actualPersonResourceLevel = resourceDataView.getPersonResourceLevel(resourceId, personId);
					assertEquals(expectedPersonResourceLevel, actualPersonResourceLevel);
				}
			}
			for (ResourceId resourceId : resourceInitialData.getResourceIds()) {
				Set<ResourcePropertyId> expectedResourcePropertyIds = resourceInitialData.getResourcePropertyIds(resourceId);
				Set<ResourcePropertyId> actualResourcePropertyIds = resourceDataView.getResourcePropertyIds(resourceId);
				assertEquals(expectedResourcePropertyIds, actualResourcePropertyIds);
				
				for(ResourcePropertyId resourcePropertyId : expectedResourcePropertyIds) {
					PropertyDefinition expectedDefinition = resourceInitialData.getResourcePropertyDefinition(resourceId, resourcePropertyId);
					PropertyDefinition actualDefinition = resourceDataView.getResourcePropertyDefinition(resourceId, resourcePropertyId);
					assertEquals(expectedDefinition, actualDefinition);
					
					Object expectedValue = resourceInitialData.getResourcePropertyValue(resourceId, resourcePropertyId);
					Object actualValue = resourceDataView.getResourcePropertyValue(resourceId, resourcePropertyId);
					assertEquals(expectedValue, actualValue);
					
				}
			}
			
		}));

		ActionPlugin actionPlugin = pluginBuilder.build();

		// add the action plugin
		builder.addPlugin(ActionPlugin.PLUGIN_ID, actionPlugin::init);

		// build and execute the engine
		builder.build().execute();

		// show that all actions were executed
		if (!actionPlugin.allActionsExecuted()) {
			throw new ContractException(ActionError.ACTION_EXECUTION_FAILURE);
		}

	}

	private void testEventLabeler(AgentContext c, EventLabeler<?> eventLabeler) {
		assertNotNull(eventLabeler);
		ContractException contractException = assertThrows(ContractException.class, () -> c.addEventLabeler(eventLabeler));
		assertEquals(NucleusError.DUPLICATE_LABELER_ID_IN_EVENT_LABELER, contractException.getErrorType());
	}

	@Test
	@UnitTestMethod(name = "init", args = {})
	public void testPersonResourceChangeObservationEventLabelers() {

		// Have the agent attempt to add the event labelers and show that a
		// contract exception is thrown, indicating that the labelers were
		// previously added by the resolver.

		ResourcesActionSupport.testConsumer(100, 4062799122381184575L, (c) -> {

			CompartmentLocationDataView compartmentLocationDataView = c.getDataView(CompartmentLocationDataView.class).get();
			RegionLocationDataView regionLocationDataView = c.getDataView(RegionLocationDataView.class).get();

			testEventLabeler(c, PersonResourceChangeObservationEvent.getEventLabelerForCompartmentAndResource(compartmentLocationDataView));
			testEventLabeler(c, PersonResourceChangeObservationEvent.getEventLabelerForPersonAndResource());
			testEventLabeler(c, PersonResourceChangeObservationEvent.getEventLabelerForRegionAndResource(regionLocationDataView));
			testEventLabeler(c, PersonResourceChangeObservationEvent.getEventLabelerForResource());

		});

	}

	@Test
	@UnitTestMethod(name = "init", args = {})
	public void testResourcePropertyChangeObservationEventLabelers() {

		// Have the agent attempt to add the event labelers and show that a
		// contract exception is thrown, indicating that the labelers were
		// previously added by the resolver.

		ResourcesActionSupport.testConsumer(100, 3611119165176896462L, (c) -> {
			testEventLabeler(c, ResourcePropertyChangeObservationEvent.getEventLabeler());
		});

	}

	@Test
	@UnitTestMethod(name = "init", args = {})
	public void testRegionResourceChangeObservationEventLabelers() {

		//
		// Have the agent attempt to add the event labelers and show that a
		// contract exception is thrown, indicating that the labelers were
		// previously added by the resolver.

		ResourcesActionSupport.testConsumer(100, 8290874716343051269L, (c) -> {
			testEventLabeler(c, RegionResourceChangeObservationEvent.getEventLabelerForRegionAndResource());
		});

	}

}
