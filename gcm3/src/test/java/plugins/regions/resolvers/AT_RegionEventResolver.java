package plugins.regions.resolvers;

import static org.junit.jupiter.api.Assertions.assertEquals;
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

import org.apache.commons.math3.random.RandomGenerator;
import org.junit.jupiter.api.Test;

import nucleus.AgentId;
import nucleus.Simulation;
import nucleus.Simulation.Builder;
import nucleus.Event;
import nucleus.EventLabel;
import nucleus.EventLabeler;
import nucleus.NucleusError;
import nucleus.ResolverId;
import nucleus.SimpleResolverId;
import nucleus.testsupport.actionplugin.ActionAgent;
import nucleus.testsupport.actionplugin.ActionPlugin;
import nucleus.testsupport.actionplugin.AgentActionPlan;
import nucleus.testsupport.actionplugin.ResolverActionPlan;
import plugins.regions.RegionPlugin;
import plugins.regions.datacontainers.RegionDataView;
import plugins.regions.datacontainers.RegionLocationDataView;
import plugins.regions.events.mutation.RegionPropertyValueAssignmentEvent;
import plugins.regions.events.mutation.PersonRegionAssignmentEvent;
import plugins.regions.events.observation.RegionPropertyChangeObservationEvent;
import plugins.regions.events.observation.PersonRegionChangeObservationEvent;
import plugins.regions.initialdata.RegionInitialData;
import plugins.regions.support.RegionError;
import plugins.regions.support.RegionId;
import plugins.regions.support.RegionPropertyId;
import plugins.regions.support.SimpleRegionPropertyId;
import plugins.regions.testsupport.TestRegionId;
import plugins.regions.testsupport.TestRegionPropertyId;
import plugins.components.ComponentPlugin;
import plugins.components.datacontainers.ComponentDataView;
import plugins.partitions.PartitionsPlugin;
import plugins.people.PeoplePlugin;
import plugins.people.datacontainers.PersonDataView;
import plugins.people.events.mutation.BulkPersonCreationEvent;
import plugins.people.events.mutation.PersonCreationEvent;
import plugins.people.events.mutation.PersonRemovalRequestEvent;
import plugins.people.events.observation.BulkPersonCreationObservationEvent;
import plugins.people.events.observation.PersonCreationObservationEvent;
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
import plugins.reports.ReportPlugin;
import plugins.reports.initialdata.ReportsInitialData;
import plugins.stochastics.StochasticsPlugin;
import plugins.stochastics.datacontainers.StochasticsDataView;
import plugins.stochastics.initialdata.StochasticsInitialData;
import util.ContractException;
import util.MultiKey;
import util.SeedProvider;
import util.annotations.UnitTest;
import util.annotations.UnitTestMethod;

@UnitTest(target = RegionEventResolver.class)
public class AT_RegionEventResolver {

	/**
	 * Shows that the region data view is published with the correct initial
	 * state. Other tests will demonstrate that the data view is maintained.
	 */
	@Test
	@UnitTestMethod(name = "init", args = {})
	public void testRegionDataViewInitialization() {

		RandomGenerator randomGenerator = SeedProvider.getRandomGenerator(6570432288835614372L);
		Builder builder = Simulation.builder();

		/*
		 * Add the regions
		 */
		RegionInitialData.Builder regionInitialDataBuilder = RegionInitialData.builder();
		for (TestRegionId testRegionId : TestRegionId.values()) {
			regionInitialDataBuilder.setRegionComponentInitialBehaviorSupplier(testRegionId, () -> new ActionAgent(testRegionId)::init);

		}

		/*
		 * Define the region properties and initial values, using a variety of
		 * default vs. explicit values, types, default values and mutability.
		 * Time tracking policies are not enforced by the region plugin, but are
		 * reflected in the stored property definitions.
		 *
		 */
		RegionPropertyId propertyId = new SimpleRegionPropertyId("prop_1_1");
		PropertyDefinition propertyDefinition = PropertyDefinition.builder().setType(Integer.class).setDefaultValue(15).setPropertyValueMutability(false).build();
		regionInitialDataBuilder.defineRegionProperty(propertyId, propertyDefinition);

		propertyId = new SimpleRegionPropertyId("prop_1_2");
		propertyDefinition = PropertyDefinition.builder().setType(Integer.class).setDefaultValue(11).setPropertyValueMutability(false).build();
		regionInitialDataBuilder.defineRegionProperty(propertyId, propertyDefinition);
		regionInitialDataBuilder.setRegionPropertyValue(TestRegionId.REGION_1, propertyId, 45);

		propertyId = new SimpleRegionPropertyId("prop_2_1");
		propertyDefinition = PropertyDefinition.builder().setType(String.class).setDefaultValue("default").build();
		regionInitialDataBuilder.defineRegionProperty(propertyId, propertyDefinition);

		propertyId = new SimpleRegionPropertyId("prop_2_2");
		propertyDefinition = PropertyDefinition.builder().setType(Double.class).setDefaultValue(88.8).build();
		regionInitialDataBuilder.defineRegionProperty(propertyId, propertyDefinition);
		regionInitialDataBuilder.setRegionPropertyValue(TestRegionId.REGION_2, propertyId, 367.4);

		propertyId = new SimpleRegionPropertyId("prop_3_1");
		propertyDefinition = PropertyDefinition.builder().setType(Boolean.class).setDefaultValue(false).build();
		regionInitialDataBuilder.defineRegionProperty(propertyId, propertyDefinition);

		// add the region plugin
		RegionInitialData regionInitialData = regionInitialDataBuilder.build();
		RegionPlugin regionPlugin = new RegionPlugin(regionInitialData);
		builder.addPlugin(RegionPlugin.PLUGIN_ID, regionPlugin::init);

		// add the remaining plugins that are needed for dependencies
		builder.addPlugin(PeoplePlugin.PLUGIN_ID, new PeoplePlugin(PeopleInitialData.builder().build())::init);
		builder.addPlugin(StochasticsPlugin.PLUGIN_ID, new StochasticsPlugin(StochasticsInitialData.builder().setSeed(randomGenerator.nextLong()).build())::init);
		builder.addPlugin(ReportPlugin.PLUGIN_ID, new ReportPlugin(ReportsInitialData.builder().build())::init);
		builder.addPlugin(PropertiesPlugin.PLUGIN_ID, new PropertiesPlugin()::init);
		builder.addPlugin(ComponentPlugin.PLUGIN_ID, new ComponentPlugin()::init);
		builder.addPlugin(PartitionsPlugin.PLUGIN_ID, new PartitionsPlugin()::init);

		ActionPlugin.Builder pluginBuilder = ActionPlugin.builder();

		/*
		 * Create an agents to create people
		 */
		pluginBuilder.addAgent("agent");

		/*
		 * Have the agent show that the information in the region initialization
		 * data is present in the region data view. This is not a repetition for
		 * all the tests of the region data view but is instead a test of the
		 * agreement between the data view and the initialization data.
		 */
		pluginBuilder.addAgentActionPlan("agent", new AgentActionPlan(0, (c) -> {

			// show that the region data view exists
			Optional<RegionDataView> optional = c.getDataView(RegionDataView.class);
			assertTrue(optional.isPresent());

			RegionDataView regionDataView = optional.get();

			// show that the region ids match
			Set<RegionId> expectedRegionIds = regionInitialData.getRegionIds();
			Set<RegionId> actualRegionIds = regionDataView.getRegionIds();
			assertEquals(expectedRegionIds, actualRegionIds);

			Set<RegionPropertyId> expectedRegionPropertyIds = regionInitialData.getRegionPropertyIds();
			Set<RegionPropertyId> actualRegionPropertyIds = regionDataView.getRegionPropertyIds();
			assertEquals(expectedRegionPropertyIds, actualRegionPropertyIds);

			for (RegionPropertyId regionPropertyId : expectedRegionPropertyIds) {
				// show that each property definition is correct
				PropertyDefinition expectedRegionPropertyDefinition = regionInitialData.getRegionPropertyDefinition(regionPropertyId);
				PropertyDefinition actualRegionPropertyDefinition = regionDataView.getRegionPropertyDefinition(regionPropertyId);
				assertEquals(expectedRegionPropertyDefinition, actualRegionPropertyDefinition);
			}

			for (RegionId regionId : expectedRegionIds) {
				for (RegionPropertyId regionPropertyId : expectedRegionPropertyIds) {
					// show that the value of each property is correct
					Object expectedPropertyValue = regionInitialData.getRegionPropertyValue(regionId, regionPropertyId);
					Object actualPropertyValue = regionDataView.getRegionPropertyValue(regionId, regionPropertyId);
					assertEquals(expectedPropertyValue, actualPropertyValue);
					assertEquals(0.0, regionDataView.getRegionPropertyTime(regionId, regionPropertyId));
				}
			}

		}));

		// build action plugin
		ActionPlugin actionPlugin = pluginBuilder.build();
		builder.addPlugin(ActionPlugin.PLUGIN_ID, actionPlugin::init);

		// build and execute the engine
		builder.build().execute();

		// show that all actions were executed
		assertTrue(actionPlugin.allActionsExecuted());
	}

	/**
	 * Shows that the region location data view is published with the correct
	 * initial state. Other tests will demonstrate that the data view is
	 * maintained.
	 */
	@Test
	@UnitTestMethod(name = "init", args = {})
	public void testRegionLocationDataViewInitialization() {
		RandomGenerator randomGenerator = SeedProvider.getRandomGenerator(4228466028646070532L);
		Builder builder = Simulation.builder();

		/*
		 * Add the regions
		 */
		RegionInitialData.Builder regionInitialDataBuilder = RegionInitialData.builder();
		for (TestRegionId testRegionId : TestRegionId.values()) {
			regionInitialDataBuilder.setRegionComponentInitialBehaviorSupplier(testRegionId, () -> new ActionAgent(testRegionId)::init);

		}
		regionInitialDataBuilder.setPersonRegionArrivalTracking(TimeTrackingPolicy.TRACK_TIME);

		// add the region plugin
		RegionInitialData regionInitialData = regionInitialDataBuilder.build();
		RegionPlugin regionPlugin = new RegionPlugin(regionInitialData);
		builder.addPlugin(RegionPlugin.PLUGIN_ID, regionPlugin::init);

		// add the remaining plugins that are needed for dependencies
		builder.addPlugin(PeoplePlugin.PLUGIN_ID, new PeoplePlugin(PeopleInitialData.builder().build())::init);
		builder.addPlugin(StochasticsPlugin.PLUGIN_ID, new StochasticsPlugin(StochasticsInitialData.builder().setSeed(randomGenerator.nextLong()).build())::init);
		builder.addPlugin(ReportPlugin.PLUGIN_ID, new ReportPlugin(ReportsInitialData.builder().build())::init);
		builder.addPlugin(PropertiesPlugin.PLUGIN_ID, new PropertiesPlugin()::init);
		builder.addPlugin(ComponentPlugin.PLUGIN_ID, new ComponentPlugin()::init);
		builder.addPlugin(PartitionsPlugin.PLUGIN_ID, new PartitionsPlugin()::init);

		ActionPlugin.Builder pluginBuilder = ActionPlugin.builder();

		/*
		 * Create an agents to create people
		 */
		pluginBuilder.addAgent("agent");

		/*
		 * Have the agent show that region tracking policy is correct.
		 */
		pluginBuilder.addAgentActionPlan("agent", new AgentActionPlan(0, (c) -> {

			// show that the region location data view exists
			Optional<RegionLocationDataView> optional = c.getDataView(RegionLocationDataView.class);
			assertTrue(optional.isPresent());

			RegionLocationDataView regionLocationDataView = optional.get();

			// show that the region arrival tracking policy is correct
			TimeTrackingPolicy expectedPolicy = regionInitialData.getPersonRegionArrivalTrackingPolicy();
			TimeTrackingPolicy actualPolicy = regionLocationDataView.getPersonRegionArrivalTrackingPolicy();
			assertEquals(expectedPolicy, actualPolicy);

		}));

		// build action plugin
		ActionPlugin actionPlugin = pluginBuilder.build();
		builder.addPlugin(ActionPlugin.PLUGIN_ID, actionPlugin::init);

		// build and execute the engine
		builder.build().execute();

		// show that all actions were executed
		assertTrue(actionPlugin.allActionsExecuted());

	}

	/**
	 * Shows that the regions are created by the resolver.
	 */
	@Test
	@UnitTestMethod(name = "init", args = {})
	public void testRegionInitialization() {

		RandomGenerator randomGenerator = SeedProvider.getRandomGenerator(4228466028646070532L);
		Builder builder = Simulation.builder();

		// add the regions
		RegionInitialData.Builder regionInitialDataBuilder = RegionInitialData.builder();
		for (TestRegionId testRegionId : TestRegionId.values()) {
			regionInitialDataBuilder.setRegionComponentInitialBehaviorSupplier(testRegionId, () -> new ActionAgent(testRegionId)::init);
		}

		// add the region plugin
		RegionPlugin regionPlugin = new RegionPlugin(regionInitialDataBuilder.build());
		builder.addPlugin(RegionPlugin.PLUGIN_ID, regionPlugin::init);

		// add the remaining plugins that are needed for dependencies
		builder.addPlugin(PeoplePlugin.PLUGIN_ID, new PeoplePlugin(PeopleInitialData.builder().build())::init);
		builder.addPlugin(StochasticsPlugin.PLUGIN_ID, new StochasticsPlugin(StochasticsInitialData.builder().setSeed(randomGenerator.nextLong()).build())::init);
		builder.addPlugin(ReportPlugin.PLUGIN_ID, new ReportPlugin(ReportsInitialData.builder().build())::init);
		builder.addPlugin(PropertiesPlugin.PLUGIN_ID, new PropertiesPlugin()::init);
		builder.addPlugin(ComponentPlugin.PLUGIN_ID, new ComponentPlugin()::init);
		builder.addPlugin(PartitionsPlugin.PLUGIN_ID, new PartitionsPlugin()::init);

		ActionPlugin.Builder pluginBuilder = ActionPlugin.builder();

		// create an agent to search for the regions
		pluginBuilder.addAgent("agent");

		// Have the update agent make various region property updates over
		// time
		pluginBuilder.addAgentActionPlan("agent", new AgentActionPlan(0, (c) -> {

			for (TestRegionId testRegionId : TestRegionId.values()) {

				// convert the region id into its corresponding agent id
				ComponentDataView componentDataView = c.getDataView(ComponentDataView.class).get();
				AgentId agentId = componentDataView.getAgentId(testRegionId);
				assertNotNull(agentId);

				/*
				 * Ask the context if the agent exists. Note that it is possible
				 * that we can convert the component id to an agent id without
				 * the agent existing.
				 */
				assertTrue(c.agentExists(agentId));
			}

		}));

		// build action plugin
		ActionPlugin actionPlugin = pluginBuilder.build();
		builder.addPlugin(ActionPlugin.PLUGIN_ID, actionPlugin::init);

		// build and execute the engine
		builder.build().execute();

		// show that all actions were executed
		assertTrue(actionPlugin.allActionsExecuted());

	}


	/**
	 * Shows that all event {@linkplain PersonRegionChangeObservationEvent}
	 * labelers are created
	 */
	@Test
	@UnitTestMethod(name = "init", args = {})
	public void testPersonRegionChangeObservationEventLabelers() {

		RandomGenerator randomGenerator = SeedProvider.getRandomGenerator(2734071676096451334L);
		Builder builder = Simulation.builder();

		// add the regions
		RegionInitialData.Builder regionInitialDataBuilder = RegionInitialData.builder();
		for (TestRegionId testRegionId : TestRegionId.values()) {
			regionInitialDataBuilder.setRegionComponentInitialBehaviorSupplier(testRegionId, () -> new ActionAgent(testRegionId)::init);
		}

		// add the region plugin
		RegionPlugin regionPlugin = new RegionPlugin(regionInitialDataBuilder.build());
		builder.addPlugin(RegionPlugin.PLUGIN_ID, regionPlugin::init);

		// add the remaining plugins that are needed for dependencies
		builder.addPlugin(PeoplePlugin.PLUGIN_ID, new PeoplePlugin(PeopleInitialData.builder().build())::init);
		builder.addPlugin(StochasticsPlugin.PLUGIN_ID, new StochasticsPlugin(StochasticsInitialData.builder().setSeed(randomGenerator.nextLong()).build())::init);
		builder.addPlugin(ReportPlugin.PLUGIN_ID, new ReportPlugin(ReportsInitialData.builder().build())::init);
		builder.addPlugin(PropertiesPlugin.PLUGIN_ID, new PropertiesPlugin()::init);
		builder.addPlugin(ComponentPlugin.PLUGIN_ID, new ComponentPlugin()::init);
		builder.addPlugin(PartitionsPlugin.PLUGIN_ID, new PartitionsPlugin()::init);

		ActionPlugin.Builder pluginBuilder = ActionPlugin.builder();

		// create an agent to search for the regions
		pluginBuilder.addAgent("agent");

		// Have the agent attempt to add the event labeler and show that a
		// contract exception is thrown, indicating that the labeler was
		// previously added by the resolver.
		pluginBuilder.addAgentActionPlan("agent", new AgentActionPlan(0, (c) -> {
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

		}));

		// build action plugin
		ActionPlugin actionPlugin = pluginBuilder.build();
		builder.addPlugin(ActionPlugin.PLUGIN_ID, actionPlugin::init);

		// build and execute the engine
		builder.build().execute();

		// show that all actions were executed
		assertTrue(actionPlugin.allActionsExecuted());
	}

	/**
	 * Shows PopulationGrowthProjectionEvent events are handled properly
	 */
	@Test
	@UnitTestMethod(name = "init", args = {})
	public void testPopulationGrowthProjectionEvent() {
		/*
		 * nothing to test -- verification can only be done via performance
		 * testing
		 */
	}

	/**
	 * Shows PersonRegionAssignmentEvent events are handled properly. The
	 * person's current region and region arrival time are updated in the
	 * RegionLocationDataView and a corresponding
	 * PersonRegionChangeObservationEvent is generated.
	 */
	@Test
	@UnitTestMethod(name = "init", args = {})
	public void testPersonRegionAssignmentEvent() {

		// Create the standard pre-populated engine builder
		RandomGenerator randomGenerator = SeedProvider.getRandomGenerator(5655227215512656797L);
		Builder builder = Simulation.builder();

		// create some people for the plugins
		int numberOfPeople = 30;
		List<PersonId> initialPeople = new ArrayList<>();
		for (int i = 0; i < numberOfPeople; i++) {
			initialPeople.add(new PersonId(i));
		}

		// add the People plugin with the 30 people
		PeopleInitialData.Builder peopleInitialDataBuilder = PeopleInitialData.builder();
		for (PersonId personId : initialPeople) {
			peopleInitialDataBuilder.addPersonId(personId);
		}
		PeoplePlugin peoplePlugin = new PeoplePlugin(peopleInitialDataBuilder.build());
		builder.addPlugin(PeoplePlugin.PLUGIN_ID, peoplePlugin::init);

		// add the Region plugin with a few regions and 30 people
		// randomly assigned to regions
		RegionInitialData.Builder regionInitialDataBuilder = RegionInitialData.builder();
		regionInitialDataBuilder.setPersonRegionArrivalTracking(TimeTrackingPolicy.TRACK_TIME);
		for (TestRegionId testRegionId : TestRegionId.values()) {
			regionInitialDataBuilder.setRegionComponentInitialBehaviorSupplier(testRegionId, () -> new ActionAgent(testRegionId)::init);
		}
		for (PersonId personId : initialPeople) {
			TestRegionId regionId = TestRegionId.getRandomRegionId(randomGenerator);
			regionInitialDataBuilder.setPersonRegion(personId, regionId);
		}
		RegionPlugin regionPlugin = new RegionPlugin(regionInitialDataBuilder.build());
		builder.addPlugin(RegionPlugin.PLUGIN_ID, regionPlugin::init);

		// add the remaining plugins that are needed for dependencies
		builder.addPlugin(StochasticsPlugin.PLUGIN_ID, new StochasticsPlugin(StochasticsInitialData.builder().setSeed(randomGenerator.nextLong()).build())::init);
		builder.addPlugin(ReportPlugin.PLUGIN_ID, new ReportPlugin(ReportsInitialData.builder().build())::init);
		builder.addPlugin(PropertiesPlugin.PLUGIN_ID, new PropertiesPlugin()::init);
		builder.addPlugin(ComponentPlugin.PLUGIN_ID, new ComponentPlugin()::init);
		builder.addPlugin(PartitionsPlugin.PLUGIN_ID, new PartitionsPlugin()::init);

		ActionPlugin.Builder pluginBuilder = ActionPlugin.builder();

		// create two agents to move and observe people being moved
		pluginBuilder.addAgent("mover agent");
		pluginBuilder.addAgent("observer");

		// create some containers for movement observations
		List<MultiKey> recievedObservations = new ArrayList<>();
		List<MultiKey> expectedObservations = new ArrayList<>();

		/*
		 * Have the observer agent observe all movements and record those
		 * observations
		 */
		pluginBuilder.addAgentActionPlan("observer", new AgentActionPlan(0, (c) -> {

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
		pluginBuilder.addAgentActionPlan("mover agent", new AgentActionPlan(1, (c) -> {

			/*
			 * Make sure that there are actually people in the simulation so
			 * that test is actually testing something
			 */
			PersonDataView personDataView = c.getDataView(PersonDataView.class).get();
			RegionLocationDataView regionLocationDataView = c.getDataView(RegionLocationDataView.class).get();

			List<PersonId> people = personDataView.getPeople();
			assertTrue(people.size() > 0);

			// schedule a time for each person to be moved to a new region
			double planTime = 10;
			for (final PersonId personId : people) {
				c.addPlan((c2) -> {
					TestRegionId regionId = regionLocationDataView.getPersonRegion(personId);
					TestRegionId nextRegionId = regionId.next();
					c2.resolveEvent(new PersonRegionAssignmentEvent(personId, nextRegionId));

					// show that the person's region is updated
					assertEquals(nextRegionId, regionLocationDataView.getPersonRegion(personId));
					expectedObservations.add(new MultiKey(regionId, nextRegionId, personId, c2.getTime()));

					// show that the person's region arrival time is
					// updated
					assertEquals(c2.getTime(), regionLocationDataView.getPersonRegionArrivalTime(personId));

				}, planTime);
				planTime += 5;
			}
		}));

		/*
		 * Test the preconditions
		 */
		pluginBuilder.addAgentActionPlan(TestRegionId.REGION_3, new AgentActionPlan(0, (c) -> {
			// Select a person at random from the simulation and create a person
			// id outside of the simulation

			PersonDataView personDataView = c.getDataView(PersonDataView.class).get();
			List<PersonId> people = personDataView.getPeople();
			PersonId personId = people.get(randomGenerator.nextInt(people.size()));
			PersonId badPersonId = new PersonId(people.size());

			// establish the person's current region and next region
			RegionLocationDataView regionLocationDataView = c.getDataView(RegionLocationDataView.class).get();
			TestRegionId currentRegionId = regionLocationDataView.getPersonRegion(personId);
			TestRegionId nextRegionId = currentRegionId.next();

			// create a non-existent region id
			RegionId unknownRegionId = TestRegionId.getUnknownRegionId();

			// if the person id is null
			ContractException contractException = assertThrows(ContractException.class, () -> c.resolveEvent(new PersonRegionAssignmentEvent(null, nextRegionId)));
			assertEquals(PersonError.NULL_PERSON_ID, contractException.getErrorType());

			// if the person id is unknown
			contractException = assertThrows(ContractException.class, () -> c.resolveEvent(new PersonRegionAssignmentEvent(badPersonId, nextRegionId)));
			assertEquals(PersonError.UNKNOWN_PERSON_ID, contractException.getErrorType());

			// if the region id is null
			contractException = assertThrows(ContractException.class, () -> c.resolveEvent(new PersonRegionAssignmentEvent(personId, null)));
			assertEquals(RegionError.NULL_REGION_ID, contractException.getErrorType());

			// if the region id is unknown
			contractException = assertThrows(ContractException.class, () -> c.resolveEvent(new PersonRegionAssignmentEvent(personId, unknownRegionId)));
			assertEquals(RegionError.UNKNOWN_REGION_ID, contractException.getErrorType());

			// if the region is the current region for the person
			contractException = assertThrows(ContractException.class, () -> c.resolveEvent(new PersonRegionAssignmentEvent(personId, currentRegionId)));
			assertEquals(RegionError.SAME_REGION, contractException.getErrorType());
		}));

		// build the plugin
		ActionPlugin actionPlugin = pluginBuilder.build();

		// build and execute the engine
		builder.addPlugin(ActionPlugin.PLUGIN_ID, actionPlugin::init).build().execute();//

		// show that all the test actions were performed
		assertTrue(actionPlugin.allActionsExecuted());

		// show that the observations were correct
		assertEquals(expectedObservations.size(), recievedObservations.size());
		assertEquals(new LinkedHashSet<>(expectedObservations), new LinkedHashSet<>(recievedObservations));
	}


	/**
	 * Shows PersonCreationObservationEvent events are handled properly The
	 * person's initial region in the is set in the RegionLocationDataView from
	 * the region reference in the auxiliary data of the event.
	 * 
	 * It is not practical to use PersonCreationObservationEvent events
	 * directly. Instead, we use PersonCreationEvent events to force the people
	 * plugin to create the corresponding PersonCreationObservationEvent events
	 * as a consequence of person creation.
	 * 
	 */
	@Test
	@UnitTestMethod(name = "init", args = {})
	public void testPersonCreationObservationEvent() {

		RandomGenerator randomGenerator = SeedProvider.getRandomGenerator(8294774271110836859L);
		Builder builder = Simulation.builder();

		// add the regions
		RegionInitialData.Builder regionInitialDataBuilder = RegionInitialData.builder();
		for (TestRegionId testRegionId : TestRegionId.values()) {
			regionInitialDataBuilder.setRegionComponentInitialBehaviorSupplier(testRegionId, () -> new ActionAgent(testRegionId)::init);
		}
		regionInitialDataBuilder.setPersonRegionArrivalTracking(TimeTrackingPolicy.TRACK_TIME);

		// add the region plugin
		RegionPlugin regionPlugin = new RegionPlugin(regionInitialDataBuilder.build());
		builder.addPlugin(RegionPlugin.PLUGIN_ID, regionPlugin::init);

		// add the remaining plugins that are needed for dependencies
		builder.addPlugin(PeoplePlugin.PLUGIN_ID, new PeoplePlugin(PeopleInitialData.builder().build())::init);
		builder.addPlugin(StochasticsPlugin.PLUGIN_ID, new StochasticsPlugin(StochasticsInitialData.builder().setSeed(randomGenerator.nextLong()).build())::init);
		builder.addPlugin(ReportPlugin.PLUGIN_ID, new ReportPlugin(ReportsInitialData.builder().build())::init);
		builder.addPlugin(PropertiesPlugin.PLUGIN_ID, new PropertiesPlugin()::init);
		builder.addPlugin(ComponentPlugin.PLUGIN_ID, new ComponentPlugin()::init);
		builder.addPlugin(PartitionsPlugin.PLUGIN_ID, new PartitionsPlugin()::init);

		ActionPlugin.Builder pluginBuilder = ActionPlugin.builder();

		/*
		 * Create an agents to create people
		 */
		pluginBuilder.addAgent("agent");

		/*
		 * Have the agent create some people over time and show that each person
		 * is in the correct region at the correct time
		 */
		pluginBuilder.addAgentActionPlan("agent", new AgentActionPlan(0, (c) -> {

			for (int i = 0; i < 100; i++) {
				c.addPlan((c2) -> {
					StochasticsDataView stochasticsDataView = c2.getDataView(StochasticsDataView.class).get();
					RegionLocationDataView regionLocationDataView = c2.getDataView(RegionLocationDataView.class).get();
					PersonDataView personDataView = c2.getDataView(PersonDataView.class).get();

					/*
					 * Generate a random region to for the new person and add
					 * the person
					 */
					TestRegionId randomRegionId = TestRegionId.getRandomRegionId(stochasticsDataView.getRandomGenerator());
					PersonContructionData personContructionData = PersonContructionData.builder().add(randomRegionId).build();
					c2.resolveEvent(new PersonCreationEvent(personContructionData));
					PersonId personId = personDataView.getLastIssuedPersonId().get();

					/*
					 * Show that the person is in the correct region with the
					 * correct region arrival time
					 */
					RegionId personRegionId = regionLocationDataView.getPersonRegion(personId);
					assertEquals(randomRegionId, personRegionId);
					assertEquals(c2.getTime(), regionLocationDataView.getPersonRegionArrivalTime(personId));

				}, randomGenerator.nextDouble() * 1000);
			}

		}));

		// precondition checks
		pluginBuilder.addAgentActionPlan("agent", new AgentActionPlan(0, (c) -> {

			PersonId personId = new PersonId(100000);

			// if no region data was included in the event
			PersonContructionData personContructionData1 = PersonContructionData.builder().build();
			ContractException contractException = assertThrows(ContractException.class, () -> c.resolveEvent(new PersonCreationObservationEvent(personId, personContructionData1)));
			assertEquals(RegionError.NULL_REGION_ID, contractException.getErrorType());

			// if the region in the event is unknown
			PersonContructionData personContructionData2 = PersonContructionData.builder().add(TestRegionId.getUnknownRegionId()).build();
			contractException = assertThrows(ContractException.class, () -> c.resolveEvent(new PersonCreationObservationEvent(personId, personContructionData2)));
			assertEquals(RegionError.UNKNOWN_REGION_ID, contractException.getErrorType());

		}));

		// build action plugin
		ActionPlugin actionPlugin = pluginBuilder.build();
		builder.addPlugin(ActionPlugin.PLUGIN_ID, actionPlugin::init);

		// build and execute the engine
		builder.build().execute();

		// show that all actions were executed
		assertTrue(actionPlugin.allActionsExecuted());

	}

	/**
	 * 
	 * 
	 * Shows BulkPersonCreationObservationEvent events are handled properly. The
	 * person's initial region in the is set in the RegionLocationDataView from
	 * the region references in the associated auxiliary data of the event.
	 * 
	 * It is not practical to use BulkPersonCreationObservationEvent events
	 * directly. Instead, we use BulkPersonCreationEvent events to force the
	 * people plugin to create the corresponding
	 * BulkPersonCreationObservationEvent event as a consequence of bulk person
	 * creation.
	 * 
	 */
	@Test
	@UnitTestMethod(name = "init", args = {})
	public void testBulkPersonCreationObservationEvent() {

		RandomGenerator randomGenerator = SeedProvider.getRandomGenerator(2654453328570666100L);
		Builder builder = Simulation.builder();

		// add the regions
		RegionInitialData.Builder regionInitialDataBuilder = RegionInitialData.builder();
		for (TestRegionId testRegionId : TestRegionId.values()) {
			regionInitialDataBuilder.setRegionComponentInitialBehaviorSupplier(testRegionId, () -> new ActionAgent(testRegionId)::init);
		}
		regionInitialDataBuilder.setPersonRegionArrivalTracking(TimeTrackingPolicy.TRACK_TIME);

		// add the region plugin
		RegionPlugin regionPlugin = new RegionPlugin(regionInitialDataBuilder.build());
		builder.addPlugin(RegionPlugin.PLUGIN_ID, regionPlugin::init);

		// add the remaining plugins that are needed for dependencies
		builder.addPlugin(PeoplePlugin.PLUGIN_ID, new PeoplePlugin(PeopleInitialData.builder().build())::init);
		builder.addPlugin(StochasticsPlugin.PLUGIN_ID, new StochasticsPlugin(StochasticsInitialData.builder().setSeed(randomGenerator.nextLong()).build())::init);
		builder.addPlugin(ReportPlugin.PLUGIN_ID, new ReportPlugin(ReportsInitialData.builder().build())::init);
		builder.addPlugin(PropertiesPlugin.PLUGIN_ID, new PropertiesPlugin()::init);
		builder.addPlugin(ComponentPlugin.PLUGIN_ID, new ComponentPlugin()::init);
		builder.addPlugin(PartitionsPlugin.PLUGIN_ID, new PartitionsPlugin()::init);

		ActionPlugin.Builder pluginBuilder = ActionPlugin.builder();

		/*
		 * Create an agents to create people
		 */
		pluginBuilder.addAgent("agent");

		/*
		 * Have the agent create some people over time and show that each person
		 * is in the correct region at the correct time
		 */
		pluginBuilder.addAgentActionPlan("agent", new AgentActionPlan(0, (c) -> {

			for (int i = 0; i < 100; i++) {
				c.addPlan((c2) -> {
					StochasticsDataView stochasticsDataView = c2.getDataView(StochasticsDataView.class).get();
					RegionLocationDataView regionLocationDataView = c2.getDataView(RegionLocationDataView.class).get();
					PersonDataView personDataView = c2.getDataView(PersonDataView.class).get();

					RandomGenerator rng = stochasticsDataView.getRandomGenerator();
					/*
					 * Generate a random region to for each new person and add
					 * the person
					 */
					Map<Integer, RegionId> expectedRegions = new LinkedHashMap<>();
					BulkPersonContructionData.Builder bulkBuilder = BulkPersonContructionData.builder();
					PersonContructionData.Builder personBuilder = PersonContructionData.builder();
					int personCount = rng.nextInt(5) + 1;

					for (int j = 0; j < personCount; j++) {
						RegionId randomRegionId = TestRegionId.getRandomRegionId(rng);
						personBuilder.add(randomRegionId);
						expectedRegions.put(j, randomRegionId);
						bulkBuilder.add(personBuilder.build());
					}
					BulkPersonContructionData bulkPersonContructionData = bulkBuilder.build();
					c2.resolveEvent(new BulkPersonCreationEvent(bulkPersonContructionData));
					PersonId personId = personDataView.getLastIssuedPersonId().get();

					int basePersonIndex = personId.getValue() - personCount + 1;

					/*
					 * Show that each person is in the correct region with the
					 * correct region arrival time
					 */
					for (int j = 0; j < personCount; j++) {
						personId = new PersonId(j + basePersonIndex);
						RegionId personRegionId = regionLocationDataView.getPersonRegion(personId);
						RegionId randomRegionId = expectedRegions.get(j);
						assertEquals(randomRegionId, personRegionId);
						assertEquals(c2.getTime(), regionLocationDataView.getPersonRegionArrivalTime(personId));
					}

				}, randomGenerator.nextDouble() * 1000);
			}

		}));

		// precondition checks
		pluginBuilder.addAgentActionPlan("agent", new AgentActionPlan(0, (c) -> {

			PersonId personId = new PersonId(100000);

			// if no region data was included in the event
			PersonContructionData personContructionData = PersonContructionData.builder().build();
			BulkPersonContructionData bulkPersonContructionData = BulkPersonContructionData.builder().add(personContructionData).build();
			BulkPersonCreationObservationEvent bulkPersonCreationObservationEvent1 = new BulkPersonCreationObservationEvent(personId, bulkPersonContructionData);
			ContractException contractException = assertThrows(ContractException.class, () -> c.resolveEvent(bulkPersonCreationObservationEvent1));
			assertEquals(RegionError.NULL_REGION_ID, contractException.getErrorType());

			// if the region in the event is unknown

			personContructionData = PersonContructionData.builder().add(TestRegionId.getUnknownRegionId()).build();
			bulkPersonContructionData = BulkPersonContructionData.builder().add(personContructionData).build();
			BulkPersonCreationObservationEvent bulkPersonCreationObservationEvent2 = new BulkPersonCreationObservationEvent(personId, bulkPersonContructionData);
			contractException = assertThrows(ContractException.class, () -> c.resolveEvent(bulkPersonCreationObservationEvent2));
			assertEquals(RegionError.UNKNOWN_REGION_ID, contractException.getErrorType());

		}));

		// build action plugin
		ActionPlugin actionPlugin = pluginBuilder.build();
		builder.addPlugin(ActionPlugin.PLUGIN_ID, actionPlugin::init);

		// build and execute the engine
		builder.build().execute();

		// show that all actions were executed
		assertTrue(actionPlugin.allActionsExecuted());

	}

	private static class CustomEvent implements Event {
		private final PersonId personId;

		private CustomEvent(PersonId personId) {
			this.personId = personId;
		}

		public PersonId getPersonId() {
			return personId;
		}

	}

	/**
	 * Shows PersonImminentRemovalObservationEvent events are handled properly.
	 */
	@Test
	@UnitTestMethod(name = "init", args = {})
	public void testPersonImminentRemovalObservationEvent() {

		RandomGenerator randomGenerator = SeedProvider.getRandomGenerator(163202760371564041L);
		Builder builder = Simulation.builder();

		/*
		 * Add the regions
		 */
		RegionInitialData.Builder regionInitialDataBuilder = RegionInitialData.builder();
		for (TestRegionId testRegionId : TestRegionId.values()) {
			regionInitialDataBuilder.setRegionComponentInitialBehaviorSupplier(testRegionId, () -> new ActionAgent(testRegionId)::init);

		}

		// add the region plugin
		RegionInitialData regionInitialData = regionInitialDataBuilder.build();
		RegionPlugin regionPlugin = new RegionPlugin(regionInitialData);
		builder.addPlugin(RegionPlugin.PLUGIN_ID, regionPlugin::init);

		// add the remaining plugins that are needed for dependencies
		builder.addPlugin(PeoplePlugin.PLUGIN_ID, new PeoplePlugin(PeopleInitialData.builder().build())::init);
		builder.addPlugin(StochasticsPlugin.PLUGIN_ID, new StochasticsPlugin(StochasticsInitialData.builder().setSeed(randomGenerator.nextLong()).build())::init);
		builder.addPlugin(ReportPlugin.PLUGIN_ID, new ReportPlugin(ReportsInitialData.builder().build())::init);
		builder.addPlugin(PropertiesPlugin.PLUGIN_ID, new PropertiesPlugin()::init);
		builder.addPlugin(ComponentPlugin.PLUGIN_ID, new ComponentPlugin()::init);
		builder.addPlugin(PartitionsPlugin.PLUGIN_ID, new PartitionsPlugin()::init);

		ActionPlugin.Builder pluginBuilder = ActionPlugin.builder();

		/*
		 * Precondition checks
		 * 
		 * The region resolver should throw contract exceptions when a
		 * PersonImminentRemovalObservationEvent is generated without a valid
		 * person id. Normally these events are generated by the people
		 * resolver, but that resolver will not produce such an invalid event.
		 * 
		 * We will force the production of an invalid event with a custom
		 * resolver and custom event. Due to ordering in the addition of
		 * plugins, we can guarantee that the first resolver to validate will be
		 * the region resolver. Multiple resolvers may throw these exceptions
		 * and we will have to verify that the correct resolver is the source of
		 * the exception by examining the exception.
		 * 
		 * Since resolvers do not have immediate event resolution, it is best to
		 * have an agent that creates a custom event for the custom resolver to
		 * resolve. This will in turn produce the desired invalid
		 * PersonImminentRemovalObservationEvent
		 */
		ResolverId resolverId = new SimpleResolverId("custom resolver");
		pluginBuilder.addResolver(resolverId);

		pluginBuilder.addResolverActionPlan(resolverId, new ResolverActionPlan(0, (c) -> {
			c.subscribeToEventExecutionPhase(CustomEvent.class, (c2, e) -> {
				PersonImminentRemovalObservationEvent event = new PersonImminentRemovalObservationEvent(e.getPersonId());
				c.queueEventForResolution(event);
			});
		}));

		/*
		 * Have an agent send a custom event to the custom resolver that will
		 * cause the required contract exceptions
		 */
		pluginBuilder.addAgent("agent");
		pluginBuilder.addAgentActionPlan("agent", new AgentActionPlan(1, (c) -> {

			ContractException contractException = assertThrows(ContractException.class, () -> c.resolveEvent(new CustomEvent(null)));
			assertEquals(PersonError.NULL_PERSON_ID, contractException.getErrorType());
			

			contractException = assertThrows(ContractException.class, () -> c.resolveEvent(new CustomEvent(new PersonId(-1))));
			assertEquals(PersonError.UNKNOWN_PERSON_ID, contractException.getErrorType());
			assertTrue(contractException.getMessage().contains(RegionEventResolver.class.getSimpleName()));

		}));

		/*
		 * Have the agent add a person and then remove it. There will be a delay
		 * of 0 time for the person to be removed.
		 */
		pluginBuilder.addAgentActionPlan("agent", new AgentActionPlan(2, (c) -> {

			PersonContructionData personContructionData = PersonContructionData.builder().add(TestRegionId.REGION_1).build();
			c.resolveEvent(new PersonCreationEvent(personContructionData));
			PersonId personId = c.getDataView(PersonDataView.class).get().getLastIssuedPersonId().get();

			RegionLocationDataView regionLocationDataView = c.getDataView(RegionLocationDataView.class).get();
			int regionPopulationCount = regionLocationDataView.getRegionPopulationCount(TestRegionId.REGION_1);
			assertEquals(1, regionPopulationCount);
			assertEquals(TestRegionId.REGION_1, regionLocationDataView.getPersonRegion(personId));

			c.resolveEvent(new PersonRemovalRequestEvent(personId));

		}));

		/*
		 * Have the agent show that the person is no longer in the location data
		 * view
		 * 
		 */
		pluginBuilder.addAgentActionPlan("agent", new AgentActionPlan(3, (c) -> {

			RegionLocationDataView regionLocationDataView = c.getDataView(RegionLocationDataView.class).get();
			PersonId personId = c.getDataView(PersonDataView.class).get().getLastIssuedPersonId().get();

			int regionPopulationCount = regionLocationDataView.getRegionPopulationCount(TestRegionId.REGION_1);
			assertEquals(0, regionPopulationCount);
			ContractException contractException = assertThrows(ContractException.class, () -> regionLocationDataView.getPersonRegion(personId));
			assertEquals(PersonError.UNKNOWN_PERSON_ID, contractException.getErrorType());

		}));

		// build action plugin
		ActionPlugin actionPlugin = pluginBuilder.build();
		builder.addPlugin(ActionPlugin.PLUGIN_ID, actionPlugin::init);

		// build and execute the engine
		builder.build().execute();

		// show that all actions were executed
		assertTrue(actionPlugin.allActionsExecuted());

	}

	/**
	 * Shows that all event {@linkplain RegionPropertyChangeObservationEvent}
	 * labelers are created
	 */
	@Test
	@UnitTestMethod(name = "init", args = {})
	public void testRegionPropertyChangeObservationEventLabelers() {

		RandomGenerator randomGenerator = SeedProvider.getRandomGenerator(4228466028646070532L);
		Builder builder = Simulation.builder();

		// add the regions
		RegionInitialData.Builder regionInitialDataBuilder = RegionInitialData.builder();
		for (TestRegionId testRegionId : TestRegionId.values()) {
			regionInitialDataBuilder.setRegionComponentInitialBehaviorSupplier(testRegionId, () -> new ActionAgent(testRegionId)::init);
		}

		// add the region plugin
		RegionPlugin regionPlugin = new RegionPlugin(regionInitialDataBuilder.build());
		builder.addPlugin(RegionPlugin.PLUGIN_ID, regionPlugin::init);

		// add the remaining plugins that are needed for dependencies
		builder.addPlugin(PeoplePlugin.PLUGIN_ID, new PeoplePlugin(PeopleInitialData.builder().build())::init);
		builder.addPlugin(StochasticsPlugin.PLUGIN_ID, new StochasticsPlugin(StochasticsInitialData.builder().setSeed(randomGenerator.nextLong()).build())::init);
		builder.addPlugin(ReportPlugin.PLUGIN_ID, new ReportPlugin(ReportsInitialData.builder().build())::init);
		builder.addPlugin(PropertiesPlugin.PLUGIN_ID, new PropertiesPlugin()::init);
		builder.addPlugin(ComponentPlugin.PLUGIN_ID, new ComponentPlugin()::init);
		builder.addPlugin(PartitionsPlugin.PLUGIN_ID, new PartitionsPlugin()::init);

		ActionPlugin.Builder pluginBuilder = ActionPlugin.builder();

		// create an agent to search for the regions
		pluginBuilder.addAgent("agent");

		// Have the agent attempt to add the event labeler and show that a
		// contract exception is thrown, indicating that the labeler was
		// previously added by the resolver.
		pluginBuilder.addAgentActionPlan("agent", new AgentActionPlan(0, (c) -> {
			EventLabeler<RegionPropertyChangeObservationEvent> eventLabeler1 = RegionPropertyChangeObservationEvent.getEventLabelerForProperty();
			assertNotNull(eventLabeler1);
			ContractException contractException = assertThrows(ContractException.class, () -> c.addEventLabeler(eventLabeler1));
			assertEquals(NucleusError.DUPLICATE_LABELER_ID_IN_EVENT_LABELER, contractException.getErrorType());
			
			EventLabeler<RegionPropertyChangeObservationEvent> eventLabeler2 = RegionPropertyChangeObservationEvent.getEventLabelerForRegionAndProperty();
			assertNotNull(eventLabeler2);
			contractException = assertThrows(ContractException.class, () -> c.addEventLabeler(eventLabeler2));
			assertEquals(NucleusError.DUPLICATE_LABELER_ID_IN_EVENT_LABELER, contractException.getErrorType());

		}));

		// build action plugin
		ActionPlugin actionPlugin = pluginBuilder.build();
		builder.addPlugin(ActionPlugin.PLUGIN_ID, actionPlugin::init);

		// build and execute the engine
		builder.build().execute();

		// show that all actions were executed
		assertTrue(actionPlugin.allActionsExecuted());

	}

	
	/**
	 * Shows RegionPropertyValueAssignmentEvent events are handled properly. The
	 * resolver updates the region's property value and time in the
	 * RegionDataView and generates a corresponding
	 * RegionPropertyChangeObservationEvent
	 */
	@Test
	@UnitTestMethod(name = "init", args = {})
	public void testRegionPropertyValueAssignmentEvent() {

		RandomGenerator randomGenerator = SeedProvider.getRandomGenerator(2585617683851220731L);
		Builder builder = Simulation.builder();

		// add the regions
		RegionInitialData.Builder regionInitialDataBuilder = RegionInitialData.builder();
		for (TestRegionId testRegionId : TestRegionId.values()) {
			regionInitialDataBuilder.setRegionComponentInitialBehaviorSupplier(testRegionId, () -> new ActionAgent(testRegionId)::init);
		}

		// add a few region properties to support tests
		RegionPropertyId propertyId_1_1 = new SimpleRegionPropertyId("propertyId_1_1");
		PropertyDefinition propertyDefinition = PropertyDefinition.builder().setType(Integer.class).setDefaultValue(5).build();
		regionInitialDataBuilder.defineRegionProperty(propertyId_1_1, propertyDefinition);

		RegionPropertyId propertyId_1_immutable = new SimpleRegionPropertyId("propertyId_1_immutable");
		propertyDefinition = PropertyDefinition.builder().setPropertyValueMutability(false).setType(Integer.class).setDefaultValue(12).build();
		regionInitialDataBuilder.defineRegionProperty(propertyId_1_immutable, propertyDefinition);

		RegionPropertyId propertyId_1_2 = new SimpleRegionPropertyId("propertyId_1_2");
		propertyDefinition = PropertyDefinition.builder().setType(String.class).setDefaultValue("abc").build();
		regionInitialDataBuilder.defineRegionProperty(propertyId_1_2, propertyDefinition);

		RegionPropertyId propertyId_2_1 = new SimpleRegionPropertyId("propertyId_2_1");
		propertyDefinition = PropertyDefinition.builder().setType(Double.class).setDefaultValue(45.6).build();
		regionInitialDataBuilder.defineRegionProperty(propertyId_2_1, propertyDefinition);

		// add the region plugin
		RegionPlugin regionPlugin = new RegionPlugin(regionInitialDataBuilder.build());
		builder.addPlugin(RegionPlugin.PLUGIN_ID, regionPlugin::init);

		// add the remaining plugins that are needed for dependencies
		builder.addPlugin(PeoplePlugin.PLUGIN_ID, new PeoplePlugin(PeopleInitialData.builder().build())::init);
		builder.addPlugin(StochasticsPlugin.PLUGIN_ID, new StochasticsPlugin(StochasticsInitialData.builder().setSeed(randomGenerator.nextLong()).build())::init);
		builder.addPlugin(ReportPlugin.PLUGIN_ID, new ReportPlugin(ReportsInitialData.builder().build())::init);
		builder.addPlugin(PropertiesPlugin.PLUGIN_ID, new PropertiesPlugin()::init);
		builder.addPlugin(ComponentPlugin.PLUGIN_ID, new ComponentPlugin()::init);
		builder.addPlugin(PartitionsPlugin.PLUGIN_ID, new PartitionsPlugin()::init);

		ActionPlugin.Builder pluginBuilder = ActionPlugin.builder();

		// create two agents to make and observe region property value
		// changes
		pluginBuilder.addAgent("update agent");
		pluginBuilder.addAgent("observer agent");

		// create containers to hold actual and expected observations
		List<MultiKey> actualObservations = new ArrayList<>();
		List<MultiKey> expectedObservations = new ArrayList<>();

		// Have the observer agent start observations record them
		pluginBuilder.addAgentActionPlan("observer agent", new AgentActionPlan(0, (c) -> {
			EventLabel<RegionPropertyChangeObservationEvent> eventLabel = RegionPropertyChangeObservationEvent.getEventLabelByRegionAndProperty(c, TestRegionId.REGION_1, propertyId_1_1);
			c.subscribe(eventLabel, (c2, e) -> {
				actualObservations.add(new MultiKey(c2.getTime(), e.getRegionId(), e.getRegionPropertyId(), e.getCurrentPropertyValue()));
			});

			eventLabel = RegionPropertyChangeObservationEvent.getEventLabelByRegionAndProperty(c, TestRegionId.REGION_1, propertyId_1_2);
			c.subscribe(eventLabel, (c2, e) -> {
				actualObservations.add(new MultiKey(c2.getTime(), e.getRegionId(), e.getRegionPropertyId(), e.getCurrentPropertyValue()));
			});

			eventLabel = RegionPropertyChangeObservationEvent.getEventLabelByRegionAndProperty(c, TestRegionId.REGION_2, propertyId_2_1);
			c.subscribe(eventLabel, (c2, e) -> {
				actualObservations.add(new MultiKey(c2.getTime(), e.getRegionId(), e.getRegionPropertyId(), e.getCurrentPropertyValue()));
			});

		}));

		// Have the update agent make various region property updates over
		// time
		pluginBuilder.addAgentActionPlan("update agent", new AgentActionPlan(0, (c) -> {

			// property_1_1 updates
			for (int i = 0; i < 10; i++) {
				c.addPlan((c2) -> {
					RegionDataView regionDataView = c2.getDataView(RegionDataView.class).get();
					Integer newValue = randomGenerator.nextInt();
					c2.resolveEvent(new RegionPropertyValueAssignmentEvent(TestRegionId.REGION_1, propertyId_1_1, newValue));
					Integer actualValue = regionDataView.getRegionPropertyValue(TestRegionId.REGION_1, propertyId_1_1);
					assertEquals(newValue, actualValue);
					double valueTime = regionDataView.getRegionPropertyTime(TestRegionId.REGION_1, propertyId_1_1);
					assertEquals(c2.getTime(), valueTime);
					expectedObservations.add(new MultiKey(c2.getTime(), TestRegionId.REGION_1, propertyId_1_1, newValue));
				}, randomGenerator.nextDouble() * 1000);
			}

			// property_1_2 updates
			for (int i = 0; i < 10; i++) {
				c.addPlan((c2) -> {
					RegionDataView regionDataView = c2.getDataView(RegionDataView.class).get();
					String newValue = Integer.toString(randomGenerator.nextInt());
					c2.resolveEvent(new RegionPropertyValueAssignmentEvent(TestRegionId.REGION_1, propertyId_1_2, newValue));
					String actualValue = regionDataView.getRegionPropertyValue(TestRegionId.REGION_1, propertyId_1_2);
					assertEquals(newValue, actualValue);
					double valueTime = regionDataView.getRegionPropertyTime(TestRegionId.REGION_1, propertyId_1_2);
					assertEquals(c2.getTime(), valueTime);
					expectedObservations.add(new MultiKey(c2.getTime(), TestRegionId.REGION_1, propertyId_1_2, newValue));
				}, randomGenerator.nextDouble() * 1000);
			}

			// property_2_1 updates
			for (int i = 0; i < 10; i++) {
				c.addPlan((c2) -> {
					RegionDataView regionDataView = c2.getDataView(RegionDataView.class).get();
					Double newValue = randomGenerator.nextDouble();
					c2.resolveEvent(new RegionPropertyValueAssignmentEvent(TestRegionId.REGION_2, propertyId_2_1, newValue));
					Double actualValue = regionDataView.getRegionPropertyValue(TestRegionId.REGION_2, propertyId_2_1);
					assertEquals(newValue, actualValue);
					double valueTime = regionDataView.getRegionPropertyTime(TestRegionId.REGION_2, propertyId_2_1);
					assertEquals(c2.getTime(), valueTime);
					expectedObservations.add(new MultiKey(c2.getTime(), TestRegionId.REGION_2, propertyId_2_1, newValue));
				}, randomGenerator.nextDouble() * 1000);
			}

		}));

		// precondition checks
		pluginBuilder.addAgentActionPlan("update agent", new AgentActionPlan(0, (c) -> {
			RegionId regionId = TestRegionId.REGION_1;
			RegionId unknownRegionId = TestRegionId.getUnknownRegionId();
			RegionPropertyId regionPropertyId = propertyId_1_1;
			RegionPropertyId immutableRegionPropertyId = propertyId_1_immutable;
			RegionPropertyId unknownRegionPropertyId = TestRegionPropertyId.getUnknownRegionPropertyId();
			Object propertyValue = 67;
			Object incompatiblePropertyValue = "incompatible value";

			// if the region id is null
			ContractException contractException = assertThrows(ContractException.class, () -> c.resolveEvent(new RegionPropertyValueAssignmentEvent(null, regionPropertyId, propertyValue)));
			assertEquals(RegionError.NULL_REGION_ID, contractException.getErrorType());

			// if the region id is unknown
			contractException = assertThrows(ContractException.class, () -> c.resolveEvent(new RegionPropertyValueAssignmentEvent(unknownRegionId, regionPropertyId, propertyValue)));
			assertEquals(RegionError.UNKNOWN_REGION_ID, contractException.getErrorType());

			// if the region property id is null
			contractException = assertThrows(ContractException.class, () -> c.resolveEvent(new RegionPropertyValueAssignmentEvent(regionId, null, propertyValue)));
			assertEquals(RegionError.NULL_REGION_PROPERTY_ID, contractException.getErrorType());

			// if the region property id is unknown
			contractException = assertThrows(ContractException.class, () -> c.resolveEvent(new RegionPropertyValueAssignmentEvent(regionId, unknownRegionPropertyId, propertyValue)));
			assertEquals(RegionError.UNKNOWN_REGION_PROPERTY_ID, contractException.getErrorType());

			// if the region property value is null
			contractException = assertThrows(ContractException.class, () -> c.resolveEvent(new RegionPropertyValueAssignmentEvent(regionId, regionPropertyId, null)));
			assertEquals(RegionError.NULL_REGION_PROPERTY_VALUE, contractException.getErrorType());

			// if the region property value is incompatible with the
			// defined type for the property
			contractException = assertThrows(ContractException.class, () -> c.resolveEvent(new RegionPropertyValueAssignmentEvent(regionId, regionPropertyId, incompatiblePropertyValue)));
			assertEquals(PropertyError.INCOMPATIBLE_VALUE, contractException.getErrorType());

			// if the property has been defined as immutable
			contractException = assertThrows(ContractException.class, () -> c.resolveEvent(new RegionPropertyValueAssignmentEvent(regionId, immutableRegionPropertyId, propertyValue)));
			assertEquals(PropertyError.IMMUTABLE_VALUE, contractException.getErrorType());

		}));

		// build action plugin
		ActionPlugin actionPlugin = pluginBuilder.build();
		builder.addPlugin(ActionPlugin.PLUGIN_ID, actionPlugin::init);

		// build and execute the engine
		builder.build().execute();

		// show that all actions were executed
		assertTrue(actionPlugin.allActionsExecuted());

		// show that the observed changes match expectations
		assertEquals(expectedObservations.size(), actualObservations.size());

		assertEquals(new LinkedHashSet<>(expectedObservations), new LinkedHashSet<>(actualObservations));
	}

}
