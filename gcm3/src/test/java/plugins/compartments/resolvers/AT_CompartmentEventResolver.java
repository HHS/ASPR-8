package plugins.compartments.resolvers;

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
import nucleus.Event;
import nucleus.EventLabel;
import nucleus.EventLabeler;
import nucleus.NucleusError;
import nucleus.ResolverId;
import nucleus.SimpleResolverId;
import nucleus.Simulation;
import nucleus.Simulation.Builder;
import nucleus.testsupport.actionplugin.ActionAgent;
import nucleus.testsupport.actionplugin.ActionPlugin;
import nucleus.testsupport.actionplugin.AgentActionPlan;
import nucleus.testsupport.actionplugin.ResolverActionPlan;
import plugins.compartments.CompartmentPlugin;
import plugins.compartments.datacontainers.CompartmentDataView;
import plugins.compartments.datacontainers.CompartmentLocationDataView;
import plugins.compartments.events.mutation.CompartmentPropertyValueAssignmentEvent;
import plugins.compartments.events.mutation.PersonCompartmentAssignmentEvent;
import plugins.compartments.events.observation.CompartmentPropertyChangeObservationEvent;
import plugins.compartments.events.observation.PersonCompartmentChangeObservationEvent;
import plugins.compartments.initialdata.CompartmentInitialData;
import plugins.compartments.support.CompartmentError;
import plugins.compartments.support.CompartmentId;
import plugins.compartments.support.CompartmentPropertyId;
import plugins.compartments.support.SimpleCompartmentPropertyId;
import plugins.compartments.testsupport.CompartmentsActionSupport;
import plugins.compartments.testsupport.TestCompartmentId;
import plugins.compartments.testsupport.TestCompartmentPropertyId;
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
import plugins.stochastics.StochasticsDataView;
import plugins.stochastics.StochasticsPlugin;
import util.ContractException;
import util.MultiKey;
import util.SeedProvider;
import util.annotations.UnitTest;
import util.annotations.UnitTestMethod;

@UnitTest(target = CompartmentEventResolver.class)
public class AT_CompartmentEventResolver {

	/**
	 * Shows that the compartment data view is published with the correct
	 * initial state. Other tests will demonstrate that the data view is
	 * maintained.
	 */
	@Test
	@UnitTestMethod(name = "init", args = {})
	public void testCompartmentDataViewInitialization() {

		RandomGenerator randomGenerator = SeedProvider.getRandomGenerator(6570432288835614372L);
		Builder builder = Simulation.builder();

		/*
		 * Add the compartments
		 */
		CompartmentInitialData.Builder compartmentInitialDataBuilder = CompartmentInitialData.builder();
		for (TestCompartmentId testCompartmentId : TestCompartmentId.values()) {
			compartmentInitialDataBuilder.setCompartmentInitialBehaviorSupplier(testCompartmentId, () -> new ActionAgent(testCompartmentId)::init);

		}

		/*
		 * Define the compartment properties and initial values, using a variety
		 * of default vs. explicit values, types, default values and mutability.
		 * Time tracking policies are not enforced by the compartment plugin,
		 * but are reflected in the stored property definitions.
		 *
		 */
		CompartmentPropertyId propertyId = new SimpleCompartmentPropertyId("prop_1_1");
		PropertyDefinition propertyDefinition = PropertyDefinition.builder().setType(Integer.class).setDefaultValue(15).setPropertyValueMutability(false).build();
		compartmentInitialDataBuilder.defineCompartmentProperty(TestCompartmentId.COMPARTMENT_1, propertyId, propertyDefinition);

		propertyId = new SimpleCompartmentPropertyId("prop_1_2");
		propertyDefinition = PropertyDefinition.builder().setType(Integer.class).setDefaultValue(11).setPropertyValueMutability(false).build();
		compartmentInitialDataBuilder.defineCompartmentProperty(TestCompartmentId.COMPARTMENT_1, propertyId, propertyDefinition);
		compartmentInitialDataBuilder.setCompartmentPropertyValue(TestCompartmentId.COMPARTMENT_1, propertyId, 45);

		propertyId = new SimpleCompartmentPropertyId("prop_2_1");
		propertyDefinition = PropertyDefinition.builder().setType(String.class).setDefaultValue("default").build();
		compartmentInitialDataBuilder.defineCompartmentProperty(TestCompartmentId.COMPARTMENT_2, propertyId, propertyDefinition);

		propertyId = new SimpleCompartmentPropertyId("prop_2_2");
		propertyDefinition = PropertyDefinition.builder().setType(Double.class).setDefaultValue(88.8).build();
		compartmentInitialDataBuilder.defineCompartmentProperty(TestCompartmentId.COMPARTMENT_2, propertyId, propertyDefinition);
		compartmentInitialDataBuilder.setCompartmentPropertyValue(TestCompartmentId.COMPARTMENT_2, propertyId, 367.4);

		propertyId = new SimpleCompartmentPropertyId("prop_3_1");
		propertyDefinition = PropertyDefinition.builder().setType(Boolean.class).setDefaultValue(false).build();
		compartmentInitialDataBuilder.defineCompartmentProperty(TestCompartmentId.COMPARTMENT_3, propertyId, propertyDefinition);

		// add the compartment plugin
		CompartmentInitialData compartmentInitialData = compartmentInitialDataBuilder.build();
		CompartmentPlugin compartmentPlugin = new CompartmentPlugin(compartmentInitialData);
		builder.addPlugin(CompartmentPlugin.PLUGIN_ID, compartmentPlugin::init);

		// add the remaining plugins that are needed for dependencies
		builder.addPlugin(PeoplePlugin.PLUGIN_ID, new PeoplePlugin(PeopleInitialData.builder().build())::init);
		builder.addPlugin(StochasticsPlugin.PLUGIN_ID, StochasticsPlugin.builder().setSeed(randomGenerator.nextLong()).build()::init);
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
		 * Have the agent show that the information in the compartment
		 * initialization data is present in the compartment data view. This is
		 * not a repetition for all the tests of the compartment data view but
		 * is instead a test of the agreement between the data view and the
		 * initialization data.
		 */
		pluginBuilder.addAgentActionPlan("agent", new AgentActionPlan(0, (c) -> {

			// show that the compartment data view exists
			Optional<CompartmentDataView> optional = c.getDataView(CompartmentDataView.class);
			assertTrue(optional.isPresent());

			CompartmentDataView compartmentDataView = optional.get();

			// show that the compartment ids match
			Set<CompartmentId> expectedCompartmentIds = compartmentInitialData.getCompartmentIds();
			Set<CompartmentId> actualCompartmentIds = compartmentDataView.getCompartmentIds();
			assertEquals(expectedCompartmentIds, actualCompartmentIds);

			for (CompartmentId compartmentId : expectedCompartmentIds) {
				// show that each compartment has the correct compartment
				// property ids
				Set<CompartmentPropertyId> expectedCompartmentPropertyIds = compartmentInitialData.getCompartmentPropertyIds(compartmentId);
				Set<CompartmentPropertyId> actualCompartmentPropertyIds = compartmentDataView.getCompartmentPropertyIds(compartmentId);
				assertEquals(expectedCompartmentPropertyIds, actualCompartmentPropertyIds);

				for (CompartmentPropertyId compartmentPropertyId : expectedCompartmentPropertyIds) {
					// show that each property definition is correct
					PropertyDefinition expectedCompartmentPropertyDefinition = compartmentInitialData.getCompartmentPropertyDefinition(compartmentId, compartmentPropertyId);
					PropertyDefinition actualCompartmentPropertyDefinition = compartmentDataView.getCompartmentPropertyDefinition(compartmentId, compartmentPropertyId);
					assertEquals(expectedCompartmentPropertyDefinition, actualCompartmentPropertyDefinition);

					// show that the value of each property is correct
					Object expectedPropertyValue = compartmentInitialData.getCompartmentPropertyValue(compartmentId, compartmentPropertyId);
					Object actualPropertyValue = compartmentDataView.getCompartmentPropertyValue(compartmentId, compartmentPropertyId);
					assertEquals(expectedPropertyValue, actualPropertyValue);
					assertEquals(0.0, compartmentDataView.getCompartmentPropertyTime(compartmentId, compartmentPropertyId));
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
	 * Shows that the compartment location data view is published with the
	 * correct initial state. Other tests will demonstrate that the data view is
	 * maintained.
	 */
	@Test
	@UnitTestMethod(name = "init", args = {})
	public void testCompartmentLocationDataViewInitialization() {
		for (TimeTrackingPolicy timeTrackingPolicy : TimeTrackingPolicy.values()) {
			CompartmentsActionSupport.testConsumer(0, 4228466028646070532L, timeTrackingPolicy, (c) -> {
				// show that the compartment location data view exists
				Optional<CompartmentLocationDataView> optional = c.getDataView(CompartmentLocationDataView.class);
				assertTrue(optional.isPresent());

				CompartmentLocationDataView compartmentLocationDataView = optional.get();

				// show that the compartment arrival tracking policy is correct
				TimeTrackingPolicy expectedPolicy = timeTrackingPolicy;
				TimeTrackingPolicy actualPolicy = compartmentLocationDataView.getPersonCompartmentArrivalTrackingPolicy();
				assertEquals(expectedPolicy, actualPolicy);
			});
		}

	}

	/**
	 * Shows that the compartments are created by the resolver.
	 */
	@Test
	@UnitTestMethod(name = "init", args = {})
	public void testCompartmentInitialization() {

		CompartmentsActionSupport.testConsumer(0, 4228466028646070532L, TimeTrackingPolicy.DO_NOT_TRACK_TIME, (c) -> {
			for (TestCompartmentId testCompartmentId : TestCompartmentId.values()) {

				// convert the compartment id into its corresponding agent id
				ComponentDataView componentDataView = c.getDataView(ComponentDataView.class).get();
				AgentId agentId = componentDataView.getAgentId(testCompartmentId);
				assertNotNull(agentId);

				/*
				 * Ask the context if the agent exists. Note that it is possible
				 * that we can convert the component id to an agent id without
				 * the agent existing.
				 */
				assertTrue(c.agentExists(agentId));
			}
		});

	}

	/**
	 * Shows that all event
	 * {@linkplain CompartmentPropertyChangeObservationEvent} labelers are
	 * created
	 */
	@Test
	@UnitTestMethod(name = "init", args = {})
	public void testCompartmentPropertyChangeObservationEventLabelers() {
		CompartmentsActionSupport.testConsumer(0, 4228466028646070532L, TimeTrackingPolicy.DO_NOT_TRACK_TIME, (c) -> {
			EventLabeler<CompartmentPropertyChangeObservationEvent> eventLabeler = CompartmentPropertyChangeObservationEvent.getEventLabeler();
			assertNotNull(eventLabeler);
			ContractException contractException = assertThrows(ContractException.class, () -> c.addEventLabeler(eventLabeler));
			assertEquals(NucleusError.DUPLICATE_LABELER_ID_IN_EVENT_LABELER, contractException.getErrorType());
		});
	}

	/**
	 * Shows that all event {@linkplain PersonCompartmentChangeObservationEvent}
	 * labelers are created
	 */
	@Test
	@UnitTestMethod(name = "init", args = {})
	public void testPersonCompartmentChangeObservationEventLabelers() {

		CompartmentsActionSupport.testConsumer(0, 2734071676096451334L, TimeTrackingPolicy.DO_NOT_TRACK_TIME, (c)->{
			EventLabeler<PersonCompartmentChangeObservationEvent> eventLabelerForArrivalCompartment = PersonCompartmentChangeObservationEvent.getEventLabelerForArrivalCompartment();
			assertNotNull(eventLabelerForArrivalCompartment);
			ContractException contractException = assertThrows(ContractException.class, () -> c.addEventLabeler(eventLabelerForArrivalCompartment));
			assertEquals(NucleusError.DUPLICATE_LABELER_ID_IN_EVENT_LABELER, contractException.getErrorType());

			EventLabeler<PersonCompartmentChangeObservationEvent> eventLabelerForDepartureCompartment = PersonCompartmentChangeObservationEvent.getEventLabelerForDepartureCompartment();
			assertNotNull(eventLabelerForDepartureCompartment);
			contractException = assertThrows(ContractException.class, () -> c.addEventLabeler(eventLabelerForDepartureCompartment));
			assertEquals(NucleusError.DUPLICATE_LABELER_ID_IN_EVENT_LABELER, contractException.getErrorType());

			EventLabeler<PersonCompartmentChangeObservationEvent> eventLabelerForPerson = PersonCompartmentChangeObservationEvent.getEventLabelerForPerson();
			assertNotNull(eventLabelerForPerson);
			contractException = assertThrows(ContractException.class, () -> c.addEventLabeler(eventLabelerForPerson));
			assertEquals(NucleusError.DUPLICATE_LABELER_ID_IN_EVENT_LABELER, contractException.getErrorType());

		});
		
		
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
	 * Shows PersonCompartmentAssignmentEvent events are handled properly. The
	 * person's current compartment and compartment arrival time are updated in
	 * the CompartmentLocationDataView and a corresponding
	 * PersonCompartmentChangeObservationEvent is generated.
	 */
	@Test
	@UnitTestMethod(name = "init", args = {})
	public void testPersonCompartmentAssignmentEvent() {

		// Create the standard pre-populated engine builder
		RandomGenerator randomGenerator = SeedProvider.getRandomGenerator(5655227215512656797L);
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

			for (TestCompartmentId testCompartmentId : TestCompartmentId.values()) {
				EventLabel<PersonCompartmentChangeObservationEvent> eventLabel = PersonCompartmentChangeObservationEvent.getEventLabelByArrivalCompartment(c, testCompartmentId);
				c.subscribe(eventLabel, (c2, e) -> {
					recievedObservations.add(new MultiKey(e.getPreviousCompartmentId(), e.getCurrentCompartmentId(), e.getPersonId(), c2.getTime()));
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
			CompartmentLocationDataView compartmentLocationDataView = c.getDataView(CompartmentLocationDataView.class).get();

			List<PersonId> people = personDataView.getPeople();
			assertTrue(people.size() > 0);

			// schedule a time for each person to be moved to a new compartment
			double planTime = 10;
			for (final PersonId personId : people) {
				c.addPlan((c2) -> {
					TestCompartmentId compartmentId = compartmentLocationDataView.getPersonCompartment(personId);
					TestCompartmentId nextCompartmentId = compartmentId.next();
					c2.resolveEvent(new PersonCompartmentAssignmentEvent(personId, nextCompartmentId));

					// show that the person's compartment is updated
					assertEquals(nextCompartmentId, compartmentLocationDataView.getPersonCompartment(personId));
					expectedObservations.add(new MultiKey(compartmentId, nextCompartmentId, personId, c2.getTime()));

					// show that the person's compartment arrival time is
					// updated
					assertEquals(c2.getTime(), compartmentLocationDataView.getPersonCompartmentArrivalTime(personId));

				}, planTime);
				planTime += 5;
			}
		}));

		/*
		 * Test the preconditions
		 */
		pluginBuilder.addAgentActionPlan(TestCompartmentId.COMPARTMENT_3, new AgentActionPlan(0, (c) -> {
			// Select a person at random from the simulation and create a person
			// id outside of the simulation

			PersonDataView personDataView = c.getDataView(PersonDataView.class).get();
			List<PersonId> people = personDataView.getPeople();
			PersonId personId = people.get(randomGenerator.nextInt(people.size()));
			PersonId badPersonId = new PersonId(people.size());

			// establish the person's current compartment and next compartment
			CompartmentLocationDataView compartmentLocationDataView = c.getDataView(CompartmentLocationDataView.class).get();
			TestCompartmentId currentCompartmentId = compartmentLocationDataView.getPersonCompartment(personId);
			TestCompartmentId nextCompartmentId = currentCompartmentId.next();

			// create a non-existent compartment id
			CompartmentId unknownCompartmentId = TestCompartmentId.getUnknownCompartmentId();

			// if the person id is null
			ContractException contractException = assertThrows(ContractException.class, () -> c.resolveEvent(new PersonCompartmentAssignmentEvent(null, nextCompartmentId)));
			assertEquals(PersonError.NULL_PERSON_ID, contractException.getErrorType());

			// if the person id is unknown
			contractException = assertThrows(ContractException.class, () -> c.resolveEvent(new PersonCompartmentAssignmentEvent(badPersonId, nextCompartmentId)));
			assertEquals(PersonError.UNKNOWN_PERSON_ID, contractException.getErrorType());

			// if the compartment id is null
			contractException = assertThrows(ContractException.class, () -> c.resolveEvent(new PersonCompartmentAssignmentEvent(personId, null)));
			assertEquals(CompartmentError.NULL_COMPARTMENT_ID, contractException.getErrorType());

			// if the compartment id is unknown
			contractException = assertThrows(ContractException.class, () -> c.resolveEvent(new PersonCompartmentAssignmentEvent(personId, unknownCompartmentId)));
			assertEquals(CompartmentError.UNKNOWN_COMPARTMENT_ID, contractException.getErrorType());

			// if the compartment is the current compartment for the person
			contractException = assertThrows(ContractException.class, () -> c.resolveEvent(new PersonCompartmentAssignmentEvent(personId, currentCompartmentId)));
			assertEquals(CompartmentError.SAME_COMPARTMENT, contractException.getErrorType());
		}));

		// build the plugin
		ActionPlugin actionPlugin = pluginBuilder.build();
		CompartmentsActionSupport.testConsumers(30, 5655227215512656797L, TimeTrackingPolicy.TRACK_TIME, actionPlugin);

		// show that the observations were correct
		assertEquals(expectedObservations.size(), recievedObservations.size());
		assertEquals(new LinkedHashSet<>(expectedObservations), new LinkedHashSet<>(recievedObservations));
	}

	/**
	 * Shows CompartmentPropertyValueAssignmentEvent events are handled
	 * properly. The resolver updates the compartment's property value and time
	 * in the CompartmentDataView and generates a corresponding
	 * CompartmentPropertyChangeObservationEvent
	 */
	@Test
	@UnitTestMethod(name = "init", args = {})
	public void testCompartmentPropertyValueAssignmentEvent() {

		RandomGenerator randomGenerator = SeedProvider.getRandomGenerator(2585617683851220731L);
		Builder builder = Simulation.builder();

		// add the compartments
		CompartmentInitialData.Builder compartmentInitialDataBuilder = CompartmentInitialData.builder();
		for (TestCompartmentId testCompartmentId : TestCompartmentId.values()) {
			compartmentInitialDataBuilder.setCompartmentInitialBehaviorSupplier(testCompartmentId, () -> new ActionAgent(testCompartmentId)::init);
		}

		// add a few compartment properties to support tests
		CompartmentPropertyId propertyId_1_1 = new SimpleCompartmentPropertyId("propertyId_1_1");
		PropertyDefinition propertyDefinition = PropertyDefinition.builder().setType(Integer.class).setDefaultValue(5).build();
		compartmentInitialDataBuilder.defineCompartmentProperty(TestCompartmentId.COMPARTMENT_1, propertyId_1_1, propertyDefinition);

		CompartmentPropertyId propertyId_1_immutable = new SimpleCompartmentPropertyId("propertyId_1_immutable");
		propertyDefinition = PropertyDefinition.builder().setPropertyValueMutability(false).setType(Integer.class).setDefaultValue(12).build();
		compartmentInitialDataBuilder.defineCompartmentProperty(TestCompartmentId.COMPARTMENT_1, propertyId_1_immutable, propertyDefinition);

		CompartmentPropertyId propertyId_1_2 = new SimpleCompartmentPropertyId("propertyId_1_2");
		propertyDefinition = PropertyDefinition.builder().setType(String.class).setDefaultValue("abc").build();
		compartmentInitialDataBuilder.defineCompartmentProperty(TestCompartmentId.COMPARTMENT_1, propertyId_1_2, propertyDefinition);

		CompartmentPropertyId propertyId_2_1 = new SimpleCompartmentPropertyId("propertyId_2_1");
		propertyDefinition = PropertyDefinition.builder().setType(Double.class).setDefaultValue(45.6).build();
		compartmentInitialDataBuilder.defineCompartmentProperty(TestCompartmentId.COMPARTMENT_2, propertyId_2_1, propertyDefinition);

		// add the compartment plugin
		CompartmentPlugin compartmentPlugin = new CompartmentPlugin(compartmentInitialDataBuilder.build());
		builder.addPlugin(CompartmentPlugin.PLUGIN_ID, compartmentPlugin::init);

		// add the remaining plugins that are needed for dependencies
		builder.addPlugin(PeoplePlugin.PLUGIN_ID, new PeoplePlugin(PeopleInitialData.builder().build())::init);
		builder.addPlugin(StochasticsPlugin.PLUGIN_ID, StochasticsPlugin.builder().setSeed(randomGenerator.nextLong()).build()::init);
		builder.addPlugin(ReportPlugin.PLUGIN_ID, new ReportPlugin(ReportsInitialData.builder().build())::init);
		builder.addPlugin(PropertiesPlugin.PLUGIN_ID, new PropertiesPlugin()::init);
		builder.addPlugin(ComponentPlugin.PLUGIN_ID, new ComponentPlugin()::init);
		builder.addPlugin(PartitionsPlugin.PLUGIN_ID, new PartitionsPlugin()::init);

		ActionPlugin.Builder pluginBuilder = ActionPlugin.builder();

		// create two agents to make and observe compartment property value
		// changes
		pluginBuilder.addAgent("update agent");
		pluginBuilder.addAgent("observer agent");

		// create containers to hold actual and expected observations
		List<MultiKey> actualObservations = new ArrayList<>();
		List<MultiKey> expectedObservations = new ArrayList<>();

		// Have the observer agent start observations record them
		pluginBuilder.addAgentActionPlan("observer agent", new AgentActionPlan(0, (c) -> {
			EventLabel<CompartmentPropertyChangeObservationEvent> eventLabel = CompartmentPropertyChangeObservationEvent.getEventLabel(c, TestCompartmentId.COMPARTMENT_1, propertyId_1_1);
			c.subscribe(eventLabel, (c2, e) -> {
				actualObservations.add(new MultiKey(c2.getTime(), e.getCompartmentId(), e.getCompartmentPropertyId(), e.getCurrentPropertyValue()));
			});

			eventLabel = CompartmentPropertyChangeObservationEvent.getEventLabel(c, TestCompartmentId.COMPARTMENT_1, propertyId_1_2);
			c.subscribe(eventLabel, (c2, e) -> {
				actualObservations.add(new MultiKey(c2.getTime(), e.getCompartmentId(), e.getCompartmentPropertyId(), e.getCurrentPropertyValue()));
			});

			eventLabel = CompartmentPropertyChangeObservationEvent.getEventLabel(c, TestCompartmentId.COMPARTMENT_2, propertyId_2_1);
			c.subscribe(eventLabel, (c2, e) -> {
				actualObservations.add(new MultiKey(c2.getTime(), e.getCompartmentId(), e.getCompartmentPropertyId(), e.getCurrentPropertyValue()));
			});

		}));

		// Have the update agent make various compartment property updates over
		// time
		pluginBuilder.addAgentActionPlan("update agent", new AgentActionPlan(0, (c) -> {

			// property_1_1 updates
			for (int i = 0; i < 10; i++) {
				c.addPlan((c2) -> {
					CompartmentDataView compartmentDataView = c2.getDataView(CompartmentDataView.class).get();
					Integer newValue = randomGenerator.nextInt();
					c2.resolveEvent(new CompartmentPropertyValueAssignmentEvent(TestCompartmentId.COMPARTMENT_1, propertyId_1_1, newValue));
					Integer actualValue = compartmentDataView.getCompartmentPropertyValue(TestCompartmentId.COMPARTMENT_1, propertyId_1_1);
					assertEquals(newValue, actualValue);
					double valueTime = compartmentDataView.getCompartmentPropertyTime(TestCompartmentId.COMPARTMENT_1, propertyId_1_1);
					assertEquals(c2.getTime(), valueTime);
					expectedObservations.add(new MultiKey(c2.getTime(), TestCompartmentId.COMPARTMENT_1, propertyId_1_1, newValue));
				}, randomGenerator.nextDouble() * 1000);
			}

			// property_1_2 updates
			for (int i = 0; i < 10; i++) {
				c.addPlan((c2) -> {
					CompartmentDataView compartmentDataView = c2.getDataView(CompartmentDataView.class).get();
					String newValue = Integer.toString(randomGenerator.nextInt());
					c2.resolveEvent(new CompartmentPropertyValueAssignmentEvent(TestCompartmentId.COMPARTMENT_1, propertyId_1_2, newValue));
					String actualValue = compartmentDataView.getCompartmentPropertyValue(TestCompartmentId.COMPARTMENT_1, propertyId_1_2);
					assertEquals(newValue, actualValue);
					double valueTime = compartmentDataView.getCompartmentPropertyTime(TestCompartmentId.COMPARTMENT_1, propertyId_1_2);
					assertEquals(c2.getTime(), valueTime);
					expectedObservations.add(new MultiKey(c2.getTime(), TestCompartmentId.COMPARTMENT_1, propertyId_1_2, newValue));
				}, randomGenerator.nextDouble() * 1000);
			}

			// property_2_1 updates
			for (int i = 0; i < 10; i++) {
				c.addPlan((c2) -> {
					CompartmentDataView compartmentDataView = c2.getDataView(CompartmentDataView.class).get();
					Double newValue = randomGenerator.nextDouble();
					c2.resolveEvent(new CompartmentPropertyValueAssignmentEvent(TestCompartmentId.COMPARTMENT_2, propertyId_2_1, newValue));
					Double actualValue = compartmentDataView.getCompartmentPropertyValue(TestCompartmentId.COMPARTMENT_2, propertyId_2_1);
					assertEquals(newValue, actualValue);
					double valueTime = compartmentDataView.getCompartmentPropertyTime(TestCompartmentId.COMPARTMENT_2, propertyId_2_1);
					assertEquals(c2.getTime(), valueTime);
					expectedObservations.add(new MultiKey(c2.getTime(), TestCompartmentId.COMPARTMENT_2, propertyId_2_1, newValue));
				}, randomGenerator.nextDouble() * 1000);
			}

		}));

		// precondition checks
		pluginBuilder.addAgentActionPlan("update agent", new AgentActionPlan(0, (c) -> {
			CompartmentId compartmentId = TestCompartmentId.COMPARTMENT_1;
			CompartmentId unknownCompartmentId = TestCompartmentId.getUnknownCompartmentId();
			CompartmentPropertyId compartmentPropertyId = propertyId_1_1;
			CompartmentPropertyId immutableCompartmentPropertyId = propertyId_1_immutable;
			CompartmentPropertyId unknownCompartmentPropertyId = TestCompartmentPropertyId.getUnknownCompartmentPropertyId();
			Object propertyValue = 67;
			Object incompatiblePropertyValue = "incompatible value";

			// if the compartment id is null
			ContractException contractException = assertThrows(ContractException.class, () -> c.resolveEvent(new CompartmentPropertyValueAssignmentEvent(null, compartmentPropertyId, propertyValue)));
			assertEquals(CompartmentError.NULL_COMPARTMENT_ID, contractException.getErrorType());

			// if the compartment id is unknown
			contractException = assertThrows(ContractException.class, () -> c.resolveEvent(new CompartmentPropertyValueAssignmentEvent(unknownCompartmentId, compartmentPropertyId, propertyValue)));
			assertEquals(CompartmentError.UNKNOWN_COMPARTMENT_ID, contractException.getErrorType());

			// if the compartment property id is null
			contractException = assertThrows(ContractException.class, () -> c.resolveEvent(new CompartmentPropertyValueAssignmentEvent(compartmentId, null, propertyValue)));
			assertEquals(CompartmentError.NULL_COMPARTMENT_PROPERTY_ID, contractException.getErrorType());

			// if the compartment property id is unknown
			contractException = assertThrows(ContractException.class, () -> c.resolveEvent(new CompartmentPropertyValueAssignmentEvent(compartmentId, unknownCompartmentPropertyId, propertyValue)));
			assertEquals(CompartmentError.UNKNOWN_COMPARTMENT_PROPERTY_ID, contractException.getErrorType());

			// if the compartment property value is null
			contractException = assertThrows(ContractException.class, () -> c.resolveEvent(new CompartmentPropertyValueAssignmentEvent(compartmentId, compartmentPropertyId, null)));
			assertEquals(CompartmentError.NULL_COMPARTMENT_PROPERTY_VALUE, contractException.getErrorType());

			// if the compartment property value is incompatible with the
			// defined type for the property
			contractException = assertThrows(ContractException.class,
					() -> c.resolveEvent(new CompartmentPropertyValueAssignmentEvent(compartmentId, compartmentPropertyId, incompatiblePropertyValue)));
			assertEquals(PropertyError.INCOMPATIBLE_VALUE, contractException.getErrorType());

			// if the property has been defined as immutable
			contractException = assertThrows(ContractException.class, () -> c.resolveEvent(new CompartmentPropertyValueAssignmentEvent(compartmentId, immutableCompartmentPropertyId, propertyValue)));
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

	/**
	 * Shows PersonCreationObservationEvent events are handled properly The
	 * person's initial compartment in the is set in the
	 * CompartmentLocationDataView from the compartment reference in the
	 * auxiliary data of the event.
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

		ActionPlugin.Builder pluginBuilder = ActionPlugin.builder();

		/*
		 * Create an agents to create people
		 */
		pluginBuilder.addAgent("agent");

		/*
		 * Have the agent create some people over time and show that each person
		 * is in the correct compartment at the correct time
		 */
		pluginBuilder.addAgentActionPlan("agent", new AgentActionPlan(0, (c) -> {

			for (int i = 0; i < 100; i++) {
				c.addPlan((c2) -> {
					StochasticsDataView stochasticsDataView = c2.getDataView(StochasticsDataView.class).get();
					CompartmentLocationDataView compartmentLocationDataView = c2.getDataView(CompartmentLocationDataView.class).get();
					PersonDataView personDataView = c2.getDataView(PersonDataView.class).get();

					/*
					 * Generate a random compartment to for the new person and
					 * add the person
					 */
					TestCompartmentId randomCompartmentId = TestCompartmentId.getRandomCompartmentId(stochasticsDataView.getRandomGenerator());
					PersonContructionData personContructionData = PersonContructionData.builder().add(randomCompartmentId).build();
					c2.resolveEvent(new PersonCreationEvent(personContructionData));
					PersonId personId = personDataView.getLastIssuedPersonId().get();

					/*
					 * Show that the person is in the correct compartment with
					 * the correct compartment arrival time
					 */
					CompartmentId personCompartmentId = compartmentLocationDataView.getPersonCompartment(personId);
					assertEquals(randomCompartmentId, personCompartmentId);
					assertEquals(c2.getTime(), compartmentLocationDataView.getPersonCompartmentArrivalTime(personId));

				}, randomGenerator.nextDouble() * 1000);
			}

		}));

		// precondition checks
		pluginBuilder.addAgentActionPlan("agent", new AgentActionPlan(0, (c) -> {

			PersonId personId = new PersonId(100000);

			// if no compartment data was included in the event
			PersonContructionData personContructionData1 = PersonContructionData.builder().build();
			ContractException contractException = assertThrows(ContractException.class, () -> c.resolveEvent(new PersonCreationObservationEvent(personId, personContructionData1)));
			assertEquals(CompartmentError.NULL_COMPARTMENT_ID, contractException.getErrorType());

			// if the compartment in the event is unknown
			PersonContructionData personContructionData2 = PersonContructionData.builder().add(TestCompartmentId.getUnknownCompartmentId()).build();
			contractException = assertThrows(ContractException.class, () -> c.resolveEvent(new PersonCreationObservationEvent(personId, personContructionData2)));
			assertEquals(CompartmentError.UNKNOWN_COMPARTMENT_ID, contractException.getErrorType());

		}));

		// build action plugin
		ActionPlugin actionPlugin = pluginBuilder.build();
		CompartmentsActionSupport.testConsumers(0, 8294774271110836859L, TimeTrackingPolicy.TRACK_TIME, actionPlugin);

	}

	/**
	 * 
	 * 
	 * Shows BulkPersonCreationObservationEvent events are handled properly. The
	 * person's initial compartment in the is set in the
	 * CompartmentLocationDataView from the compartment references in the
	 * associated auxiliary data of the event.
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
		


		ActionPlugin.Builder pluginBuilder = ActionPlugin.builder();

		/*
		 * Create an agents to create people
		 */
		pluginBuilder.addAgent("agent");

		/*
		 * Have the agent create some people over time and show that each person
		 * is in the correct compartment at the correct time
		 */
		pluginBuilder.addAgentActionPlan("agent", new AgentActionPlan(0, (c) -> {

			for (int i = 0; i < 100; i++) {
				c.addPlan((c2) -> {
					StochasticsDataView stochasticsDataView = c2.getDataView(StochasticsDataView.class).get();
					CompartmentLocationDataView compartmentLocationDataView = c2.getDataView(CompartmentLocationDataView.class).get();
					PersonDataView personDataView = c2.getDataView(PersonDataView.class).get();

					RandomGenerator rng = stochasticsDataView.getRandomGenerator();
					/*
					 * Generate a random compartment to for each new person and
					 * add the person
					 */
					Map<Integer, CompartmentId> expectedCompartments = new LinkedHashMap<>();
					BulkPersonContructionData.Builder bulkBuilder = BulkPersonContructionData.builder();
					PersonContructionData.Builder personBuilder = PersonContructionData.builder();
					int personCount = rng.nextInt(5) + 1;

					for (int j = 0; j < personCount; j++) {
						CompartmentId randomCompartmentId = TestCompartmentId.getRandomCompartmentId(rng);
						personBuilder.add(randomCompartmentId);
						expectedCompartments.put(j, randomCompartmentId);
						bulkBuilder.add(personBuilder.build());
					}
					BulkPersonContructionData bulkPersonContructionData = bulkBuilder.build();
					c2.resolveEvent(new BulkPersonCreationEvent(bulkPersonContructionData));
					PersonId personId = personDataView.getLastIssuedPersonId().get();

					int basePersonIndex = personId.getValue() - personCount + 1;

					/*
					 * Show that each person is in the correct compartment with
					 * the correct compartment arrival time
					 */
					for (int j = 0; j < personCount; j++) {
						personId = new PersonId(j + basePersonIndex);
						CompartmentId personCompartmentId = compartmentLocationDataView.getPersonCompartment(personId);
						CompartmentId randomCompartmentId = expectedCompartments.get(j);
						assertEquals(randomCompartmentId, personCompartmentId);
						assertEquals(c2.getTime(), compartmentLocationDataView.getPersonCompartmentArrivalTime(personId));
					}

				}, randomGenerator.nextDouble() * 1000);
			}

		}));

		// precondition checks
		pluginBuilder.addAgentActionPlan("agent", new AgentActionPlan(0, (c) -> {

			PersonId personId = new PersonId(100000);

			// if no compartment data was included in the event
			PersonContructionData personContructionData = PersonContructionData.builder().build();
			BulkPersonContructionData bulkPersonContructionData = BulkPersonContructionData.builder().add(personContructionData).build();
			BulkPersonCreationObservationEvent bulkPersonCreationObservationEvent1 = new BulkPersonCreationObservationEvent(personId, bulkPersonContructionData);
			ContractException contractException = assertThrows(ContractException.class, () -> c.resolveEvent(bulkPersonCreationObservationEvent1));
			assertEquals(CompartmentError.NULL_COMPARTMENT_ID, contractException.getErrorType());

			// if the compartment in the event is unknown

			personContructionData = PersonContructionData.builder().add(TestCompartmentId.getUnknownCompartmentId()).build();
			bulkPersonContructionData = BulkPersonContructionData.builder().add(personContructionData).build();
			BulkPersonCreationObservationEvent bulkPersonCreationObservationEvent2 = new BulkPersonCreationObservationEvent(personId, bulkPersonContructionData);
			contractException = assertThrows(ContractException.class, () -> c.resolveEvent(bulkPersonCreationObservationEvent2));
			assertEquals(CompartmentError.UNKNOWN_COMPARTMENT_ID, contractException.getErrorType());

		}));

		// build action plugin
		ActionPlugin actionPlugin = pluginBuilder.build();
		CompartmentsActionSupport.testConsumers(0, 2654453328570666100L, TimeTrackingPolicy.TRACK_TIME, actionPlugin);

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

		
		ActionPlugin.Builder pluginBuilder = ActionPlugin.builder();

		/*
		 * Precondition checks
		 * 
		 * The compartment resolver should throw contract exceptions when a
		 * PersonImminentRemovalObservationEvent is generated without a valid
		 * person id. Normally these events are generated by the people
		 * resolver, but that resolver will not produce such an invalid event.
		 * 
		 * We will force the production of an invalid event with a custom
		 * resolver and custom event. Due to ordering in the addition of
		 * plugins, we can guarantee that the first resolver to validate will be
		 * the compartment resolver. Multiple resolvers may throw these
		 * exceptions and we will have to verify that the correct resolver is
		 * the source of the exception by examining the exception.
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
		 * Have an agent send custom event to the custom resolver that will
		 * cause the required contract exceptions
		 */
		pluginBuilder.addAgent("agent");
		pluginBuilder.addAgentActionPlan("agent", new AgentActionPlan(1, (c) -> {

			ContractException contractException = assertThrows(ContractException.class, () -> c.resolveEvent(new CustomEvent(null)));
			assertEquals(PersonError.NULL_PERSON_ID, contractException.getErrorType());

			contractException = assertThrows(ContractException.class, () -> c.resolveEvent(new CustomEvent(new PersonId(-1))));
			assertEquals(PersonError.UNKNOWN_PERSON_ID, contractException.getErrorType());
			assertTrue(contractException.getMessage().contains(CompartmentEventResolver.class.getSimpleName()));

		}));

		/*
		 * Have the agent add a person and then remove it. There will be a delay
		 * of 0 time for the person to be removed.
		 */
		pluginBuilder.addAgentActionPlan("agent", new AgentActionPlan(2, (c) -> {

			PersonContructionData personContructionData = PersonContructionData.builder().add(TestCompartmentId.COMPARTMENT_1).build();
			c.resolveEvent(new PersonCreationEvent(personContructionData));
			PersonId personId = c.getDataView(PersonDataView.class).get().getLastIssuedPersonId().get();

			CompartmentLocationDataView compartmentLocationDataView = c.getDataView(CompartmentLocationDataView.class).get();
			int compartmentPopulationCount = compartmentLocationDataView.getCompartmentPopulationCount(TestCompartmentId.COMPARTMENT_1);
			assertEquals(1, compartmentPopulationCount);
			assertEquals(TestCompartmentId.COMPARTMENT_1, compartmentLocationDataView.getPersonCompartment(personId));

			c.resolveEvent(new PersonRemovalRequestEvent(personId));

		}));

		/*
		 * Have the agent show that the person is no longer in the location data
		 * view
		 * 
		 */
		pluginBuilder.addAgentActionPlan("agent", new AgentActionPlan(3, (c) -> {

			CompartmentLocationDataView compartmentLocationDataView = c.getDataView(CompartmentLocationDataView.class).get();
			PersonId personId = c.getDataView(PersonDataView.class).get().getLastIssuedPersonId().get();

			int compartmentPopulationCount = compartmentLocationDataView.getCompartmentPopulationCount(TestCompartmentId.COMPARTMENT_1);
			assertEquals(0, compartmentPopulationCount);
			ContractException contractException = assertThrows(ContractException.class, () -> compartmentLocationDataView.getPersonCompartment(personId));
			assertEquals(PersonError.UNKNOWN_PERSON_ID, contractException.getErrorType());

		}));

		// build action plugin
		ActionPlugin actionPlugin = pluginBuilder.build();
		CompartmentsActionSupport.testConsumers(0, 163202760371564041L, TimeTrackingPolicy.DO_NOT_TRACK_TIME, actionPlugin);
	}

}
