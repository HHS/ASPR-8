package plugins.personproperties.resolvers;

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
import org.junit.jupiter.api.Test;

import nucleus.AgentContext;
import nucleus.Engine;
import nucleus.Engine.EngineBuilder;
import nucleus.EventLabel;
import nucleus.EventLabeler;
import nucleus.NucleusError;
import nucleus.ResolverContext;
import nucleus.testsupport.actionplugin.ActionPlugin;
import nucleus.testsupport.actionplugin.AgentActionPlan;
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
import plugins.people.initialdata.PeopleInitialData;
import plugins.people.support.BulkPersonContructionData;
import plugins.people.support.PersonContructionData;
import plugins.people.support.PersonError;
import plugins.people.support.PersonId;
import plugins.personproperties.PersonPropertiesPlugin;
import plugins.personproperties.datacontainers.PersonPropertyDataView;
import plugins.personproperties.events.mutation.PersonPropertyValueAssignmentEvent;
import plugins.personproperties.events.observation.PersonPropertyChangeObservationEvent;
import plugins.personproperties.initialdata.PersonPropertyInitialData;
import plugins.personproperties.support.PersonPropertyError;
import plugins.personproperties.support.PersonPropertyId;
import plugins.personproperties.support.PersonPropertyInitialization;
import plugins.personproperties.testsupport.TestPersonPropertyId;
import plugins.properties.PropertiesPlugin;
import plugins.properties.support.PropertyDefinition;
import plugins.properties.support.PropertyError;
import plugins.properties.support.TimeTrackingPolicy;
import plugins.regions.RegionPlugin;
import plugins.regions.datacontainers.RegionLocationDataView;
import plugins.regions.initialdata.RegionInitialData;
import plugins.regions.testsupport.TestRegionId;
import plugins.reports.ReportPlugin;
import plugins.reports.initialdata.ReportsInitialData;
import plugins.stochastics.StochasticsPlugin;
import plugins.stochastics.datacontainers.StochasticsDataView;
import plugins.stochastics.initialdata.StochasticsInitialData;
import util.ContractException;
import util.MultiKey;
import util.SeedProvider;
import util.annotations.UnitTest;
import util.annotations.UnitTestConstructor;
import util.annotations.UnitTestMethod;

@UnitTest(target = PersonPropertyEventResolver.class)
public class AT_PersonPropertyEventResolver {

	private void testConsumer(int initialPopulation, long seed, Consumer<AgentContext> consumer) {
		ActionPlugin.Builder pluginBuilder = ActionPlugin.builder();
		pluginBuilder.addAgent("agent");
		pluginBuilder.addAgentActionPlan("agent", new AgentActionPlan(0, consumer));
		testConsumers(initialPopulation, seed, pluginBuilder.build());
	}

	private void testConsumers(int initialPopulation, long seed, ActionPlugin actionPlugin) {

		EngineBuilder engineBuilder = Engine.builder();

		// add the person property plugin
		PersonPropertyInitialData.Builder personPropertyBuilder = PersonPropertyInitialData.builder();
		for (TestPersonPropertyId testPersonPropertyId : TestPersonPropertyId.values()) {
			personPropertyBuilder.definePersonProperty(testPersonPropertyId, testPersonPropertyId.getPropertyDefinition());
		}

		engineBuilder.addPlugin(PersonPropertiesPlugin.PLUGIN_ID, new PersonPropertiesPlugin(personPropertyBuilder.build())::init);

		// add the people plugin
		engineBuilder.addPlugin(PartitionsPlugin.PLUGIN_ID, new PartitionsPlugin()::init);
		PeopleInitialData.Builder peopleBuilder = PeopleInitialData.builder();
		List<PersonId> people = new ArrayList<>();
		for (int i = 0; i < initialPopulation; i++) {
			people.add(new PersonId(i));
		}

		for (PersonId personId : people) {
			peopleBuilder.addPersonId(personId);
		}

		engineBuilder.addPlugin(PeoplePlugin.PLUGIN_ID, new PeoplePlugin(peopleBuilder.build())::init);

		// add the properties plugin
		engineBuilder.addPlugin(PropertiesPlugin.PLUGIN_ID, new PropertiesPlugin()::init);

		// add the compartments plugin
		CompartmentInitialData.Builder compartmentBuilder = CompartmentInitialData.builder();

		// add the compartments
		for (TestCompartmentId testCompartmentId : TestCompartmentId.values()) {
			compartmentBuilder.setCompartmentInitialBehaviorSupplier(testCompartmentId, () -> (c2) -> {
			});
		}

		// assign people to compartments
		TestCompartmentId testCompartmentId = TestCompartmentId.COMPARTMENT_1;
		for (PersonId personId : people) {
			compartmentBuilder.setPersonCompartment(personId, testCompartmentId.next());
		}

		engineBuilder.addPlugin(CompartmentPlugin.PLUGIN_ID, new CompartmentPlugin(compartmentBuilder.build())::init);

		// add the regions plugin
		RegionInitialData.Builder regionBuilder = RegionInitialData.builder();

		// add the regions
		for (TestRegionId testRegionId : TestRegionId.values()) {
			regionBuilder.setRegionComponentInitialBehaviorSupplier(testRegionId, () -> (c2) -> {
			});
		}

		// assign people to regions
		TestRegionId testRegionId = TestRegionId.REGION_1;
		for (PersonId personId : people) {
			regionBuilder.setPersonRegion(personId, testRegionId.next());
		}

		engineBuilder.addPlugin(RegionPlugin.PLUGIN_ID, new RegionPlugin(regionBuilder.build())::init);

		// add the component plugin
		engineBuilder.addPlugin(ComponentPlugin.PLUGIN_ID, new ComponentPlugin()::init);

		// add the report plugin
		engineBuilder.addPlugin(ReportPlugin.PLUGIN_ID, new ReportPlugin(ReportsInitialData.builder().build())::init);

		// add the stochastics plugin
		engineBuilder.addPlugin(StochasticsPlugin.PLUGIN_ID, new StochasticsPlugin(StochasticsInitialData.builder().setSeed(seed).build())::init);

		// add the action plugin
		engineBuilder.addPlugin(ActionPlugin.PLUGIN_ID, actionPlugin::init);

		// build and execute the engine
		engineBuilder.build().execute();

		// show that all actions were executed
		assertTrue(actionPlugin.allActionsExecuted());

	}

	@Test
	@UnitTestConstructor(args = { PersonPropertyInitialData.class })
	public void testConstructor() {
		ContractException contractException = assertThrows(ContractException.class, () -> new PersonPropertyEventResolver(null));
		assertEquals(PersonPropertyError.NULL_PERSON_PROPERTY_INITIAL_DATA, contractException.getErrorType());
	}

	@Test
	@UnitTestMethod(name = "init", args = { ResolverContext.class })
	public void testPersonPropertyChangeObservationEventLabelers() {

		/*
		 * For each labeler, show that the labeler was previously added,
		 * presumably by the resolver.
		 */

		testConsumer(100, 4585617051924828596L, (c) -> {
			CompartmentLocationDataView compartmentLocationDataView = c.getDataView(CompartmentLocationDataView.class).get();
			RegionLocationDataView regionLocationDataView = c.getDataView(RegionLocationDataView.class).get();
			
			EventLabeler<PersonPropertyChangeObservationEvent> eventLabelerForCompartmentAndProperty = PersonPropertyChangeObservationEvent.getEventLabelerForCompartmentAndProperty(compartmentLocationDataView);
			assertNotNull(eventLabelerForCompartmentAndProperty);
			ContractException contractException = assertThrows(ContractException.class, () -> c.addEventLabeler(eventLabelerForCompartmentAndProperty));
			assertEquals(NucleusError.DUPLICATE_LABELER_ID_IN_EVENT_LABELER, contractException.getErrorType());

			EventLabeler<PersonPropertyChangeObservationEvent> eventLabelerForRegionAndProperty = PersonPropertyChangeObservationEvent.getEventLabelerForRegionAndProperty(regionLocationDataView);
			assertNotNull(eventLabelerForCompartmentAndProperty);
			contractException = assertThrows(ContractException.class, () -> c.addEventLabeler(eventLabelerForRegionAndProperty));
			assertEquals(NucleusError.DUPLICATE_LABELER_ID_IN_EVENT_LABELER, contractException.getErrorType());

			EventLabeler<PersonPropertyChangeObservationEvent> eventLabelerForPersonAndProperty = PersonPropertyChangeObservationEvent.getEventLabelerForPersonAndProperty();
			assertNotNull(eventLabelerForCompartmentAndProperty);
			contractException = assertThrows(ContractException.class, () -> c.addEventLabeler(eventLabelerForPersonAndProperty));
			assertEquals(NucleusError.DUPLICATE_LABELER_ID_IN_EVENT_LABELER, contractException.getErrorType());

			EventLabeler<PersonPropertyChangeObservationEvent> eventLabelerForProperty = PersonPropertyChangeObservationEvent.getEventLabelerForProperty();
			assertNotNull(eventLabelerForCompartmentAndProperty);
			contractException = assertThrows(ContractException.class, () -> c.addEventLabeler(eventLabelerForProperty));
			assertEquals(NucleusError.DUPLICATE_LABELER_ID_IN_EVENT_LABELER, contractException.getErrorType());

		});

	}

	@Test
	@UnitTestMethod(name = "init", args = { ResolverContext.class })
	public void testPersonPropertyDataViewInitialization() {

		// create a random generator
		RandomGenerator randomGenerator = SeedProvider.getRandomGenerator(2693836950854697940L);

		EngineBuilder engineBuilder = Engine.builder();

		// add the people plugin with 10 people
		engineBuilder.addPlugin(PartitionsPlugin.PLUGIN_ID, new PartitionsPlugin()::init);
		PeopleInitialData.Builder peopleBuilder = PeopleInitialData.builder();
		List<PersonId> people = new ArrayList<>();
		for (int i = 0; i < 10; i++) {
			people.add(new PersonId(i));
		}

		for (PersonId personId : people) {
			peopleBuilder.addPersonId(personId);
		}

		engineBuilder.addPlugin(PeoplePlugin.PLUGIN_ID, new PeoplePlugin(peopleBuilder.build())::init);

		// add the person property plugin
		PersonPropertyInitialData.Builder personPropertyBuilder = PersonPropertyInitialData.builder();
		for (TestPersonPropertyId testPersonPropertyId : TestPersonPropertyId.values()) {
			personPropertyBuilder.definePersonProperty(testPersonPropertyId, testPersonPropertyId.getPropertyDefinition());
		}

		// create a container for expected person property values and add the
		// values to the builder
		Map<PersonId, Map<TestPersonPropertyId, Object>> expectedPropertyValues = new LinkedHashMap<>();

		for (PersonId personId : people) {
			Map<TestPersonPropertyId, Object> propertyValueMap = new LinkedHashMap<>();
			expectedPropertyValues.put(personId, propertyValueMap);

			boolean b1 = randomGenerator.nextBoolean();
			personPropertyBuilder.setPersonPropertyValue(personId, TestPersonPropertyId.PERSON_PROPERTY_1_BOOLEAN_MUTABLE_NO_TRACK, b1);
			propertyValueMap.put(TestPersonPropertyId.PERSON_PROPERTY_1_BOOLEAN_MUTABLE_NO_TRACK, b1);

			int i1 = randomGenerator.nextInt();
			personPropertyBuilder.setPersonPropertyValue(personId, TestPersonPropertyId.PERSON_PROPERTY_2_INTEGER_MUTABLE_NO_TRACK, i1);
			propertyValueMap.put(TestPersonPropertyId.PERSON_PROPERTY_2_INTEGER_MUTABLE_NO_TRACK, i1);

			double d1 = randomGenerator.nextDouble();
			personPropertyBuilder.setPersonPropertyValue(personId, TestPersonPropertyId.PERSON_PROPERTY_3_DOUBLE_MUTABLE_NO_TRACK, d1);
			propertyValueMap.put(TestPersonPropertyId.PERSON_PROPERTY_3_DOUBLE_MUTABLE_NO_TRACK, d1);

			boolean b2 = randomGenerator.nextBoolean();
			personPropertyBuilder.setPersonPropertyValue(personId, TestPersonPropertyId.PERSON_PROPERTY_4_BOOLEAN_MUTABLE_TRACK, b2);
			propertyValueMap.put(TestPersonPropertyId.PERSON_PROPERTY_4_BOOLEAN_MUTABLE_TRACK, b2);

			int i2 = randomGenerator.nextInt();
			personPropertyBuilder.setPersonPropertyValue(personId, TestPersonPropertyId.PERSON_PROPERTY_5_INTEGER_MUTABLE_TRACK, i2);
			propertyValueMap.put(TestPersonPropertyId.PERSON_PROPERTY_5_INTEGER_MUTABLE_TRACK, i2);

			double d2 = randomGenerator.nextDouble();
			personPropertyBuilder.setPersonPropertyValue(personId, TestPersonPropertyId.PERSON_PROPERTY_6_DOUBLE_MUTABLE_TRACK, d2);
			propertyValueMap.put(TestPersonPropertyId.PERSON_PROPERTY_6_DOUBLE_MUTABLE_TRACK, d2);

			boolean b3 = randomGenerator.nextBoolean();
			personPropertyBuilder.setPersonPropertyValue(personId, TestPersonPropertyId.PERSON_PROPERTY_7_BOOLEAN_IMMUTABLE_NO_TRACK, b3);
			propertyValueMap.put(TestPersonPropertyId.PERSON_PROPERTY_7_BOOLEAN_IMMUTABLE_NO_TRACK, b3);

			int i3 = randomGenerator.nextInt();
			personPropertyBuilder.setPersonPropertyValue(personId, TestPersonPropertyId.PERSON_PROPERTY_8_INTEGER_IMMUTABLE_NO_TRACK, i3);
			propertyValueMap.put(TestPersonPropertyId.PERSON_PROPERTY_8_INTEGER_IMMUTABLE_NO_TRACK, i3);

			double d3 = randomGenerator.nextDouble();
			personPropertyBuilder.setPersonPropertyValue(personId, TestPersonPropertyId.PERSON_PROPERTY_9_DOUBLE_IMMUTABLE_NO_TRACK, d3);
			propertyValueMap.put(TestPersonPropertyId.PERSON_PROPERTY_9_DOUBLE_IMMUTABLE_NO_TRACK, d3);
		}

		PersonPropertyInitialData personPropertyInitialData = personPropertyBuilder.build();
		engineBuilder.addPlugin(PersonPropertiesPlugin.PLUGIN_ID, new PersonPropertiesPlugin(personPropertyInitialData)::init);

		// add the properties plugin
		engineBuilder.addPlugin(PropertiesPlugin.PLUGIN_ID, new PropertiesPlugin()::init);

		// add the compartments plugin
		CompartmentInitialData.Builder compartmentBuilder = CompartmentInitialData.builder();

		// add the compartments
		for (TestCompartmentId testCompartmentId : TestCompartmentId.values()) {
			compartmentBuilder.setCompartmentInitialBehaviorSupplier(testCompartmentId, () -> (c2) -> {
			});
		}

		// assign people to compartments
		TestCompartmentId testCompartmentId = TestCompartmentId.COMPARTMENT_1;
		for (PersonId personId : people) {
			compartmentBuilder.setPersonCompartment(personId, testCompartmentId.next());
		}

		engineBuilder.addPlugin(CompartmentPlugin.PLUGIN_ID, new CompartmentPlugin(compartmentBuilder.build())::init);

		// add the regions plugin
		RegionInitialData.Builder regionBuilder = RegionInitialData.builder();

		// add the regions
		for (TestRegionId testRegionId : TestRegionId.values()) {
			regionBuilder.setRegionComponentInitialBehaviorSupplier(testRegionId, () -> (c2) -> {
			});
		}

		// assign people to regions
		TestRegionId testRegionId = TestRegionId.REGION_1;
		for (PersonId personId : people) {
			regionBuilder.setPersonRegion(personId, testRegionId.next());
		}

		engineBuilder.addPlugin(RegionPlugin.PLUGIN_ID, new RegionPlugin(regionBuilder.build())::init);

		// add the component plugin
		engineBuilder.addPlugin(ComponentPlugin.PLUGIN_ID, new ComponentPlugin()::init);

		// add the report plugin
		engineBuilder.addPlugin(ReportPlugin.PLUGIN_ID, new ReportPlugin(ReportsInitialData.builder().build())::init);

		// add the stochastics plugin
		engineBuilder.addPlugin(StochasticsPlugin.PLUGIN_ID, new StochasticsPlugin(StochasticsInitialData.builder().setSeed(randomGenerator.nextLong()).build())::init);

		// add the action plugin
		ActionPlugin.Builder pluginBuilder = ActionPlugin.builder();

		/*
		 * Add an agent that will show that the person property data view is
		 * properly initialized from the person property initial data
		 */
		pluginBuilder.addAgent("agent");
		pluginBuilder.addAgentActionPlan("agent", new AgentActionPlan(0, (c2) -> {
			// get the person property data view
			PersonPropertyDataView personPropertyDataView = c2.getDataView(PersonPropertyDataView.class).get();

			// show that the property ids are correct
			assertEquals(personPropertyInitialData.getPersonPropertyIds(), personPropertyDataView.getPersonPropertyIds());

			// show that the property definitions are correct
			for (PersonPropertyId personPropertyId : personPropertyInitialData.getPersonPropertyIds()) {
				PropertyDefinition expectedPropertyDefinition = personPropertyInitialData.getPersonPropertyDefinition(personPropertyId);
				PropertyDefinition actualPropertyDefinition = personPropertyDataView.getPersonPropertyDefinition(personPropertyId);
				assertEquals(expectedPropertyDefinition, actualPropertyDefinition);
			}

			// show that the person property values are correct and that they
			// have the appropriate time values

			for (PersonId personId : expectedPropertyValues.keySet()) {
				Map<TestPersonPropertyId, Object> propertyValueMap = expectedPropertyValues.get(personId);
				for (TestPersonPropertyId testPersonPropertyId : propertyValueMap.keySet()) {
					Object expectedValue = propertyValueMap.get(testPersonPropertyId);
					Object actualValue = personPropertyDataView.getPersonPropertyValue(personId, testPersonPropertyId);
					assertEquals(expectedValue, actualValue);

					boolean timeTrackingOn = testPersonPropertyId.getPropertyDefinition().getTimeTrackingPolicy().equals(TimeTrackingPolicy.TRACK_TIME);
					if (timeTrackingOn) {
						assertEquals(0.0, personPropertyDataView.getPersonPropertyTime(personId, testPersonPropertyId));
					}
				}
			}

		}));

		ActionPlugin actionPlugin = pluginBuilder.build();

		engineBuilder.addPlugin(ActionPlugin.PLUGIN_ID, actionPlugin::init);

		// build and execute the engine
		engineBuilder.build().execute();

		// show that all actions were executed
		assertTrue(actionPlugin.allActionsExecuted());

	}

	@Test
	@UnitTestMethod(name = "init", args = { ResolverContext.class })
	public void testPersonPropertyValueAssignmentEvent() {

		ActionPlugin.Builder pluginBuilder = ActionPlugin.builder();

		// create some containers to hold the expected and actual observations
		// for later comparison
		Set<MultiKey> expectedObservations = new LinkedHashSet<>();
		Set<MultiKey> actualObservations = new LinkedHashSet<>();

		// add an agent that will observe changes to all person properties
		pluginBuilder.addAgent("observer");
		pluginBuilder.addAgentActionPlan("observer", new AgentActionPlan(0, (c) -> {
			for (TestPersonPropertyId testPersonPropertyId : TestPersonPropertyId.values()) {
				EventLabel<PersonPropertyChangeObservationEvent> eventLabel = PersonPropertyChangeObservationEvent.getEventLabelByProperty(c, testPersonPropertyId);
				c.subscribe(eventLabel, (c2, e) -> {
					actualObservations.add(new MultiKey(e.getPersonId(), e.getPersonPropertyId(), e.getPreviousPropertyValue(), e.getCurrentPropertyValue()));
				});
			}
		}));

		/*
		 * Add an agent that will alter person property values and record the
		 * corresponding expected observations.
		 */
		pluginBuilder.addAgent("agent");
		pluginBuilder.addAgentActionPlan("agent", new AgentActionPlan(1, (c) -> {

			// establish data views
			PersonDataView personDataView = c.getDataView(PersonDataView.class).get();
			PersonPropertyDataView personPropertyDataView = c.getDataView(PersonPropertyDataView.class).get();
			StochasticsDataView stochasticsDataView = c.getDataView(StochasticsDataView.class).get();
			RandomGenerator randomGenerator = stochasticsDataView.getRandomGenerator();

			// select all the property ids that are mutable
			Set<TestPersonPropertyId> mutableProperties = new LinkedHashSet<>();
			for (TestPersonPropertyId testPersonPropertyId : TestPersonPropertyId.values()) {
				boolean mutable = testPersonPropertyId.getPropertyDefinition().propertyValuesAreMutable();
				if (mutable) {
					mutableProperties.add(testPersonPropertyId);
				}
			}

			// get the people
			List<PersonId> people = personDataView.getPeople();

			// set all their mutable property values, recording the expected
			// observations
			for (PersonId personId : people) {
				for (TestPersonPropertyId testPersonPropertyId : mutableProperties) {

					// determine the new and current values
					Object newValue = testPersonPropertyId.getRandomPropertyValue(randomGenerator);
					Object currentValue = personPropertyDataView.getPersonPropertyValue(personId, testPersonPropertyId);

					// record the expected observation
					expectedObservations.add(new MultiKey(personId, testPersonPropertyId, currentValue, newValue));

					// update the person property
					c.resolveEvent(new PersonPropertyValueAssignmentEvent(personId, testPersonPropertyId, newValue));

					// show that the value changed
					Object actualValue = personPropertyDataView.getPersonPropertyValue(personId, testPersonPropertyId);
					assertEquals(newValue, actualValue);
				}
			}
		}));

		// have the agent perform precondition checks
		pluginBuilder.addAgentActionPlan("agent", new AgentActionPlan(2, (c) -> {
			PersonId personId = new PersonId(0);
			PersonPropertyId personPropertyId = TestPersonPropertyId.PERSON_PROPERTY_1_BOOLEAN_MUTABLE_NO_TRACK;
			PersonPropertyId immutablePersonPropertyId = TestPersonPropertyId.PERSON_PROPERTY_7_BOOLEAN_IMMUTABLE_NO_TRACK;
			Object value = true;

			PersonId unknownPersonId = new PersonId(100000);
			PersonPropertyId unknownPersonPropertyId = TestPersonPropertyId.getUnknownPersonPropertyId();
			Object incompatibleValue = 12;

			// if the person id is null
			ContractException contractException = assertThrows(ContractException.class, () -> c.resolveEvent(new PersonPropertyValueAssignmentEvent(null, personPropertyId, value)));
			assertEquals(PersonError.NULL_PERSON_ID, contractException.getErrorType());

			// if the person id is unknown
			contractException = assertThrows(ContractException.class, () -> c.resolveEvent(new PersonPropertyValueAssignmentEvent(unknownPersonId, personPropertyId, value)));
			assertEquals(PersonError.UNKNOWN_PERSON_ID, contractException.getErrorType());

			// if the person property id is null
			contractException = assertThrows(ContractException.class, () -> c.resolveEvent(new PersonPropertyValueAssignmentEvent(personId, null, value)));
			assertEquals(PersonPropertyError.NULL_PERSON_PROPERTY_ID, contractException.getErrorType());

			// if the person property id is unknown
			contractException = assertThrows(ContractException.class, () -> c.resolveEvent(new PersonPropertyValueAssignmentEvent(personId, unknownPersonPropertyId, value)));
			assertEquals(PersonPropertyError.UNKNOWN_PERSON_PROPERTY_ID, contractException.getErrorType());

			// if the property value is null
			contractException = assertThrows(ContractException.class, () -> c.resolveEvent(new PersonPropertyValueAssignmentEvent(personId, personPropertyId, null)));
			assertEquals(PersonPropertyError.NULL_PERSON_PROPERTY_VALUE, contractException.getErrorType());

			// if the property value is not compatible with the corresponding
			// property definition
			contractException = assertThrows(ContractException.class, () -> c.resolveEvent(new PersonPropertyValueAssignmentEvent(personId, personPropertyId, incompatibleValue)));
			assertEquals(PropertyError.INCOMPATIBLE_VALUE, contractException.getErrorType());

			// if the corresponding property definition marks the property as
			// immutable
			contractException = assertThrows(ContractException.class, () -> c.resolveEvent(new PersonPropertyValueAssignmentEvent(personId, immutablePersonPropertyId, value)));
			assertEquals(PropertyError.IMMUTABLE_VALUE, contractException.getErrorType());

		}));

		// have the observer show that the expected observations were actually
		// observed
		pluginBuilder.addAgent("observer");
		pluginBuilder.addAgentActionPlan("observer", new AgentActionPlan(3, (c) -> {
			assertEquals(expectedObservations, actualObservations);
		}));

		testConsumers(10, 2321272063791878719L, pluginBuilder.build());

	}

	@Test
	@UnitTestMethod(name = "init", args = { ResolverContext.class })
	public void testPersonCreationObservationEvent() {

		testConsumer(100, 4771130331997762252L, (c) -> {
			// establish data views
			StochasticsDataView stochasticsDataView = c.getDataView(StochasticsDataView.class).get();
			PersonDataView personDataView = c.getDataView(PersonDataView.class).get();
			PersonPropertyDataView personPropertyDataView = c.getDataView(PersonPropertyDataView.class).get();

			// get the random generator for use later
			RandomGenerator randomGenerator = stochasticsDataView.getRandomGenerator();

			// add a person with some person property auxiliary data
			PersonContructionData.Builder personBuilder = PersonContructionData.builder();

			// create a container to hold expectations
			Map<PersonPropertyId, Object> expectedPropertyValues = new LinkedHashMap<>();

			// set the expectation to the default values of all the properties
			Set<PersonPropertyId> personPropertyIds = personPropertyDataView.getPersonPropertyIds();
			for (PersonPropertyId personPropertyId : personPropertyIds) {
				PropertyDefinition personPropertyDefinition = personPropertyDataView.getPersonPropertyDefinition(personPropertyId);
				Object value = personPropertyDefinition.getDefaultValue().get();
				expectedPropertyValues.put(personPropertyId, value);
			}

			// set two properties to random values and record them in the
			// expected data
			int iValue = randomGenerator.nextInt();
			personBuilder.add(new PersonPropertyInitialization(TestPersonPropertyId.PERSON_PROPERTY_2_INTEGER_MUTABLE_NO_TRACK, iValue));
			expectedPropertyValues.put(TestPersonPropertyId.PERSON_PROPERTY_2_INTEGER_MUTABLE_NO_TRACK, iValue);

			double dValue = randomGenerator.nextDouble();
			personBuilder.add(new PersonPropertyInitialization(TestPersonPropertyId.PERSON_PROPERTY_3_DOUBLE_MUTABLE_NO_TRACK, dValue));
			expectedPropertyValues.put(TestPersonPropertyId.PERSON_PROPERTY_3_DOUBLE_MUTABLE_NO_TRACK, dValue);

			// the plugin uses compartments and regions, so we need to select
			// them for the new person
			personBuilder.add(TestCompartmentId.COMPARTMENT_1);
			personBuilder.add(TestRegionId.REGION_1);

			// add the person and get its person id
			c.resolveEvent(new PersonCreationEvent(personBuilder.build()));
			PersonId personId = personDataView.getLastIssuedPersonId().get();

			// show that the person exists
			assertTrue(personDataView.personExists(personId));

			// show that the person has the correct property values
			for (PersonPropertyId personPropertyId : personPropertyIds) {
				Object expectedValue = expectedPropertyValues.get(personPropertyId);
				Object actualValue = personPropertyDataView.getPersonPropertyValue(personId, personPropertyId);
				assertEquals(expectedValue, actualValue);
			}

			// precondition tests

			// if the event contains a PersonPropertyInitialization that has a
			// null person property id
			ContractException contractException = assertThrows(ContractException.class, () -> {
				personBuilder.add(TestCompartmentId.getRandomCompartmentId(randomGenerator));
				personBuilder.add(TestRegionId.getRandomRegionId(randomGenerator));
				personBuilder.add(new PersonPropertyInitialization(null, false));
				c.resolveEvent(new PersonCreationEvent(personBuilder.build()));
			});
			assertEquals(PersonPropertyError.NULL_PERSON_PROPERTY_ID, contractException.getErrorType());

			// if the event contains a PersonPropertyInitialization that has an
			// unknown person property id
			contractException = assertThrows(ContractException.class, () -> {
				personBuilder.add(TestCompartmentId.getRandomCompartmentId(randomGenerator));
				personBuilder.add(TestRegionId.getRandomRegionId(randomGenerator));
				personBuilder.add(new PersonPropertyInitialization(TestPersonPropertyId.getUnknownPersonPropertyId(), false));
				c.resolveEvent(new PersonCreationEvent(personBuilder.build()));
			});
			assertEquals(PersonPropertyError.UNKNOWN_PERSON_PROPERTY_ID, contractException.getErrorType());

			// if the event contains a PersonPropertyInitialization that has a
			// null person property value
			contractException = assertThrows(ContractException.class, () -> {
				personBuilder.add(TestCompartmentId.getRandomCompartmentId(randomGenerator));
				personBuilder.add(TestRegionId.getRandomRegionId(randomGenerator));
				personBuilder.add(new PersonPropertyInitialization(TestPersonPropertyId.PERSON_PROPERTY_1_BOOLEAN_MUTABLE_NO_TRACK, null));
				c.resolveEvent(new PersonCreationEvent(personBuilder.build()));
			});
			assertEquals(PersonPropertyError.NULL_PERSON_PROPERTY_VALUE, contractException.getErrorType());

			// if the event contains a PersonPropertyInitialization that has a
			// person property value that is not compatible with the
			// corresponding property definition
			contractException = assertThrows(ContractException.class, () -> {
				personBuilder.add(TestCompartmentId.getRandomCompartmentId(randomGenerator));
				personBuilder.add(TestRegionId.getRandomRegionId(randomGenerator));
				personBuilder.add(new PersonPropertyInitialization(TestPersonPropertyId.PERSON_PROPERTY_1_BOOLEAN_MUTABLE_NO_TRACK, 45));
				c.resolveEvent(new PersonCreationEvent(personBuilder.build()));
			});
			assertEquals(PropertyError.INCOMPATIBLE_VALUE, contractException.getErrorType());

		});

	}

	@Test
	@UnitTestMethod(name = "init", args = { ResolverContext.class })
	public void testBulkPersonCreationObservationEvent() {
		testConsumer(100, 2547218192811543040L, (c) -> {
			// establish data views
			StochasticsDataView stochasticsDataView = c.getDataView(StochasticsDataView.class).get();
			PersonDataView personDataView = c.getDataView(PersonDataView.class).get();
			PersonPropertyDataView personPropertyDataView = c.getDataView(PersonPropertyDataView.class).get();

			// get the random generator for use later
			RandomGenerator randomGenerator = stochasticsDataView.getRandomGenerator();

			// set the number of people we will add
			int bulkPersonCount = 20;
			Optional<PersonId> optionalLastPersonAdded = personDataView.getLastIssuedPersonId();
			int lastAddedId = -1;
			if (optionalLastPersonAdded.isPresent()) {
				lastAddedId = optionalLastPersonAdded.get().getValue();
			}

			/*
			 * Create a container to hold expectations. Note that we choose to
			 * not assign about half of the property values so that we can
			 * demonstrate that default values are being used correctly.
			 */
			Map<PersonId, Map<PersonPropertyId, Object>> expectedPropertyValues = new LinkedHashMap<>();
			Map<PersonId, Map<PersonPropertyId, Boolean>> shouldBeAssigned = new LinkedHashMap<>();

			/*
			 * for each person, set the expectations randomly to either the
			 * default value of the property or a new randomized value
			 */

			for (int i = 0; i < bulkPersonCount; i++) {
				PersonId personId = new PersonId(i + lastAddedId + 1);
				Map<PersonPropertyId, Object> propertyValueMap = new LinkedHashMap<>();
				expectedPropertyValues.put(personId, propertyValueMap);

				Map<PersonPropertyId, Boolean> propertyAssignmentMap = new LinkedHashMap<>();
				shouldBeAssigned.put(personId, propertyAssignmentMap);

				for (TestPersonPropertyId testPersonPropertyId : TestPersonPropertyId.values()) {

					if (randomGenerator.nextBoolean()) {
						Object value = testPersonPropertyId.getRandomPropertyValue(randomGenerator);
						propertyValueMap.put(testPersonPropertyId, value);
						propertyAssignmentMap.put(testPersonPropertyId, true);
					} else {
						PropertyDefinition personPropertyDefinition = testPersonPropertyId.getPropertyDefinition();
						Object value = personPropertyDefinition.getDefaultValue().get();
						propertyValueMap.put(testPersonPropertyId, value);
						propertyAssignmentMap.put(testPersonPropertyId, false);
					}

				}
			}

			// Construct the bulk add event from the expected values
			BulkPersonContructionData.Builder bulkBuilder = BulkPersonContructionData.builder();
			PersonContructionData.Builder personBuilder = PersonContructionData.builder();

			for (PersonId personId : expectedPropertyValues.keySet()) {
				Map<PersonPropertyId, Object> propertyValueMap = expectedPropertyValues.get(personId);
				Map<PersonPropertyId, Boolean> propertyAssignmentMap = shouldBeAssigned.get(personId);
				for (TestPersonPropertyId testPersonPropertyId : TestPersonPropertyId.values()) {
					Boolean shouldAssignProperty = propertyAssignmentMap.get(testPersonPropertyId);
					if (shouldAssignProperty) {
						Object value = propertyValueMap.get(testPersonPropertyId);
						personBuilder.add(new PersonPropertyInitialization(testPersonPropertyId, value));
					}
				}
				/*
				 * The plugin uses compartments and regions, so we need to
				 * select them for the new person
				 */
				personBuilder.add(TestCompartmentId.getRandomCompartmentId(randomGenerator));
				personBuilder.add(TestRegionId.getRandomRegionId(randomGenerator));

				bulkBuilder.add(personBuilder.build());
			}

			// add the people via the bulk creation event
			c.resolveEvent(new BulkPersonCreationEvent(bulkBuilder.build()));

			// show that the people exist
			for (PersonId personId : expectedPropertyValues.keySet()) {
				assertTrue(personDataView.personExists(personId));
			}

			// show that the people have the correct property values
			for (PersonId personId : expectedPropertyValues.keySet()) {
				Map<PersonPropertyId, Object> propertyValueMap = expectedPropertyValues.get(personId);
				for (TestPersonPropertyId testPersonPropertyId : TestPersonPropertyId.values()) {
					Object expectedValue = propertyValueMap.get(testPersonPropertyId);
					Object actualValue = personPropertyDataView.getPersonPropertyValue(personId, testPersonPropertyId);
					assertEquals(expectedValue, actualValue);
				}
			}

			// precondition tests
			// if the event contains a PersonPropertyInitialization that has a
			// null person property id
			ContractException contractException = assertThrows(ContractException.class, () -> {
				personBuilder.add(TestCompartmentId.getRandomCompartmentId(randomGenerator));
				personBuilder.add(TestRegionId.getRandomRegionId(randomGenerator));
				personBuilder.add(new PersonPropertyInitialization(null, false));
				bulkBuilder.add(personBuilder.build());
				c.resolveEvent(new BulkPersonCreationEvent(bulkBuilder.build()));
			});
			assertEquals(PersonPropertyError.NULL_PERSON_PROPERTY_ID, contractException.getErrorType());

			// if the event contains a PersonPropertyInitialization that has an
			// unknown person property id
			contractException = assertThrows(ContractException.class, () -> {
				personBuilder.add(TestCompartmentId.getRandomCompartmentId(randomGenerator));
				personBuilder.add(TestRegionId.getRandomRegionId(randomGenerator));
				personBuilder.add(new PersonPropertyInitialization(TestPersonPropertyId.getUnknownPersonPropertyId(), false));
				bulkBuilder.add(personBuilder.build());
				c.resolveEvent(new BulkPersonCreationEvent(bulkBuilder.build()));
			});
			assertEquals(PersonPropertyError.UNKNOWN_PERSON_PROPERTY_ID, contractException.getErrorType());

			// if the event contains a PersonPropertyInitialization that has a
			// null person property value
			contractException = assertThrows(ContractException.class, () -> {
				personBuilder.add(TestCompartmentId.getRandomCompartmentId(randomGenerator));
				personBuilder.add(TestRegionId.getRandomRegionId(randomGenerator));
				personBuilder.add(new PersonPropertyInitialization(TestPersonPropertyId.PERSON_PROPERTY_1_BOOLEAN_MUTABLE_NO_TRACK, null));
				bulkBuilder.add(personBuilder.build());
				c.resolveEvent(new BulkPersonCreationEvent(bulkBuilder.build()));
			});
			assertEquals(PersonPropertyError.NULL_PERSON_PROPERTY_VALUE, contractException.getErrorType());

			// if the event contains a PersonPropertyInitialization that has a
			// person property value that is not compatible with the
			// corresponding property definition
			contractException = assertThrows(ContractException.class, () -> {
				personBuilder.add(TestCompartmentId.getRandomCompartmentId(randomGenerator));
				personBuilder.add(TestRegionId.getRandomRegionId(randomGenerator));
				personBuilder.add(new PersonPropertyInitialization(TestPersonPropertyId.PERSON_PROPERTY_1_BOOLEAN_MUTABLE_NO_TRACK, 45));
				bulkBuilder.add(personBuilder.build());
				c.resolveEvent(new BulkPersonCreationEvent(bulkBuilder.build()));
			});
			assertEquals(PropertyError.INCOMPATIBLE_VALUE, contractException.getErrorType());
		});

	}

	@Test
	@UnitTestMethod(name = "init", args = { ResolverContext.class })
	public void testPersonImminentRemovalObservationEvent() {

		ActionPlugin.Builder pluginBuilder = ActionPlugin.builder();

		/*
		 * Have the agent remove a person and show that their properties remain
		 * during the current span of this agent's activation
		 */
		pluginBuilder.addAgent("agent");
		pluginBuilder.addAgentActionPlan("agent", new AgentActionPlan(0, (c) -> {
			PersonId personId = new PersonId(0);

			PersonDataView personDataView = c.getDataView(PersonDataView.class).get();
			assertTrue(personDataView.personExists(personId));

			PersonPropertyId personPropertyId = TestPersonPropertyId.PERSON_PROPERTY_2_INTEGER_MUTABLE_NO_TRACK;

			// Set the property value to a non-default value.
			Integer expectedPropertyValue = 999;
			PersonPropertyDataView personPropertyDataView = c.getDataView(PersonPropertyDataView.class).get();
			c.resolveEvent(new PersonPropertyValueAssignmentEvent(personId, personPropertyId, expectedPropertyValue));

			// remove the person
			c.resolveEvent(new PersonRemovalRequestEvent(personId));

			// show that the property value is still present
			Object actualPropertyValue = personPropertyDataView.getPersonPropertyValue(personId, personPropertyId);
			assertEquals(expectedPropertyValue, actualPropertyValue);

		}));

		// Have the agent now show that these person properties are no longer
		// available
		pluginBuilder.addAgent("agent");
		pluginBuilder.addAgentActionPlan("agent", new AgentActionPlan(0, (c) -> {

			PersonId personId = new PersonId(0);

			// show that the person does not exist
			PersonDataView personDataView = c.getDataView(PersonDataView.class).get();
			assertFalse(personDataView.personExists(personId));

			PersonPropertyDataView personPropertyDataView = c.getDataView(PersonPropertyDataView.class).get();

			PersonPropertyId personPropertyId = TestPersonPropertyId.PERSON_PROPERTY_2_INTEGER_MUTABLE_NO_TRACK;

			ContractException contractException = assertThrows(ContractException.class, () -> personPropertyDataView.getPersonPropertyValue(personId, personPropertyId));
			assertEquals(PersonError.UNKNOWN_PERSON_ID, contractException.getErrorType());

		}));
		testConsumers(10, 2020442537537236753L, pluginBuilder.build());

	}
}
