package plugins.personproperties.support;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;

import org.apache.commons.math3.random.RandomGenerator;
import org.junit.jupiter.api.Test;

import nucleus.AgentContext;
import nucleus.Context;
import nucleus.Engine;
import nucleus.Engine.EngineBuilder;
import nucleus.testsupport.actionplugin.ActionPlugin;
import nucleus.testsupport.actionplugin.AgentActionPlan;
import plugins.compartments.CompartmentPlugin;
import plugins.compartments.initialdata.CompartmentInitialData;
import plugins.compartments.testsupport.TestCompartmentId;
import plugins.components.ComponentPlugin;
import plugins.partitions.PartitionsPlugin;
import plugins.partitions.support.LabelerSensitivity;
import plugins.people.PeoplePlugin;
import plugins.people.datacontainers.PersonDataView;
import plugins.people.initialdata.PeopleInitialData;
import plugins.people.support.PersonError;
import plugins.people.support.PersonId;
import plugins.personproperties.PersonPropertiesPlugin;
import plugins.personproperties.datacontainers.PersonPropertyDataView;
import plugins.personproperties.events.mutation.PersonPropertyValueAssignmentEvent;
import plugins.personproperties.events.observation.PersonPropertyChangeObservationEvent;
import plugins.personproperties.initialdata.PersonPropertyInitialData;
import plugins.personproperties.testsupport.TestPersonPropertyId;
import plugins.properties.PropertiesPlugin;
import plugins.regions.RegionPlugin;
import plugins.regions.initialdata.RegionInitialData;
import plugins.regions.testsupport.TestRegionId;
import plugins.reports.ReportPlugin;
import plugins.reports.initialdata.ReportsInitialData;
import plugins.stochastics.StochasticsPlugin;
import plugins.stochastics.datacontainers.StochasticsDataView;
import plugins.stochastics.initialdata.StochasticsInitialData;
import util.ContractException;
import util.annotations.UnitTest;
import util.annotations.UnitTestConstructor;
import util.annotations.UnitTestMethod;

@UnitTest(target = PersonPropertyLabeler.class)
public class AT_PersonPropertyLabeler {

	@Test
	@UnitTestConstructor(args = { PersonPropertyId.class, Function.class, Object.class })
	public void testConstructor() {
		// nothing to test
	}

	@Test
	@UnitTestMethod(name = "getLabelerSensitivities", args = { PersonPropertyId.class, Function.class, Object.class })
	public void testGetLabelerSensitivities() {

		/*
		 * Get the labeler sensitivities and show that they are consistent with
		 * their documented behaviors.
		 */

		PersonPropertyId personPropertyId = TestPersonPropertyId.PERSON_PROPERTY_4_BOOLEAN_MUTABLE_TRACK;
		PersonPropertyLabeler personPropertyLabeler = new PersonPropertyLabeler(personPropertyId, (c) -> null);

		Set<LabelerSensitivity<?>> labelerSensitivities = personPropertyLabeler.getLabelerSensitivities();

		// show that there is exactly one sensitivity
		assertEquals(1, labelerSensitivities.size());

		// show that the sensitivity is associated with
		// PersonPropertyChangeObservationEvent
		LabelerSensitivity<?> labelerSensitivity = labelerSensitivities.iterator().next();
		assertEquals(PersonPropertyChangeObservationEvent.class, labelerSensitivity.getEventClass());

		/*
		 * Show that the sensitivity will return the person id from a
		 * PersonCompartmentChangeObservationEvent if the event matches the
		 * person property id.
		 */
		PersonId personId = new PersonId(56);
		PersonPropertyChangeObservationEvent personPropertyChangeObservationEvent = new PersonPropertyChangeObservationEvent(personId, personPropertyId, false, true);
		Optional<PersonId> optional = labelerSensitivity.getPersonId(personPropertyChangeObservationEvent);
		assertTrue(optional.isPresent());
		assertEquals(personId, optional.get());

		/*
		 * Show that the sensitivity will return an empty optional of person id
		 * from a PersonCompartmentChangeObservationEvent if the event does not
		 * match the person property id.
		 */

		personPropertyId = TestPersonPropertyId.PERSON_PROPERTY_1_BOOLEAN_MUTABLE_NO_TRACK;
		personPropertyChangeObservationEvent = new PersonPropertyChangeObservationEvent(personId, personPropertyId, false, true);
		optional = labelerSensitivity.getPersonId(personPropertyChangeObservationEvent);
		assertFalse(optional.isPresent());

	}

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
	@UnitTestMethod(name = "getLabel", args = { Context.class, PersonId.class })
	public void testGetLabel() {
		/*
		 * Have the agent show that the person property labeler produces a label
		 * for each person that is consistent with the function passed to the
		 * compartment labeler.
		 */
		testConsumer(10, 6445109933336671672L, (c) -> {
			// establish data views
			PersonDataView personDataView = c.getDataView(PersonDataView.class).get();
			PersonPropertyDataView personPropertyDataView = c.getDataView(PersonPropertyDataView.class).get();
			StochasticsDataView stochasticsDataView = c.getDataView(StochasticsDataView.class).get();
			RandomGenerator randomGenerator = stochasticsDataView.getRandomGenerator();

			// select a property to work with
			PersonPropertyId personPropertyId = TestPersonPropertyId.PERSON_PROPERTY_1_BOOLEAN_MUTABLE_NO_TRACK;

			/*
			 * Assign random values to the people so that we can get some
			 * variety in the labels
			 */
			List<PersonId> people = personDataView.getPeople();
			for (PersonId personId : people) {
				c.resolveEvent(new PersonPropertyValueAssignmentEvent(personId, personPropertyId, randomGenerator.nextBoolean()));
			}

			/*
			 * build a person property labeler with a function that can be
			 * tested
			 */
			Function<Object, Object> function = (input) -> {
				Boolean value = (Boolean) input;
				if (value) {
					return "A";
				}
				return "B";
			};

			PersonPropertyLabeler personPropertyLabeler = new PersonPropertyLabeler(personPropertyId, function);

			/*
			 * Apply the labeler to each person and compare it to the more
			 * direct use of the labeler's function
			 */
			for (PersonId personId : people) {

				// get the person's compartment and apply the function directly
				Boolean value = personPropertyDataView.getPersonPropertyValue(personId, personPropertyId);
				Object expectedLabel = function.apply(value);

				// get the label from the person id
				Object actualLabel = personPropertyLabeler.getLabel(c, personId);

				// show that the two labels are equal
				assertEquals(expectedLabel, actualLabel);				

			}

			// precondition tests

			// if the person does not exist
			ContractException contractException = assertThrows(ContractException.class, () -> personPropertyLabeler.getLabel(c, new PersonId(-1)));
			assertEquals(PersonError.UNKNOWN_PERSON_ID, contractException.getErrorType());

			// if the person id is null
			contractException = assertThrows(ContractException.class, () -> personPropertyLabeler.getLabel(c, null));
			assertEquals(PersonError.NULL_PERSON_ID, contractException.getErrorType());
		});
	}

	@Test
	@UnitTestMethod(name = "getDimension", args = {})
	public void testGetDimension() {
		for (TestPersonPropertyId testPersonPropertyId : TestPersonPropertyId.values()) {
			assertEquals(testPersonPropertyId, new PersonPropertyLabeler(testPersonPropertyId, (c) -> null).getDimension());
		}
	}

}
