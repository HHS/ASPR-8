package plugins.personproperties.events.observation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

import org.junit.jupiter.api.Test;

import nucleus.AgentContext;
import nucleus.Context;
import nucleus.Simulation;
import nucleus.Simulation.Builder;
import nucleus.Event;
import nucleus.EventLabel;
import nucleus.EventLabeler;
import nucleus.testsupport.actionplugin.ActionPlugin;
import nucleus.testsupport.actionplugin.AgentActionPlan;
import plugins.compartments.CompartmentPlugin;
import plugins.compartments.datacontainers.CompartmentLocationDataView;
import plugins.compartments.initialdata.CompartmentInitialData;
import plugins.compartments.support.CompartmentId;
import plugins.compartments.testsupport.TestCompartmentId;
import plugins.components.ComponentPlugin;
import plugins.partitions.PartitionsPlugin;
import plugins.people.PeoplePlugin;
import plugins.people.datacontainers.PersonDataView;
import plugins.people.initialdata.PeopleInitialData;
import plugins.people.support.PersonId;
import plugins.personproperties.PersonPropertiesPlugin;
import plugins.personproperties.initialdata.PersonPropertyInitialData;
import plugins.personproperties.support.PersonPropertyId;
import plugins.personproperties.testsupport.TestPersonPropertyId;
import plugins.properties.PropertiesPlugin;
import plugins.regions.RegionPlugin;
import plugins.regions.datacontainers.RegionLocationDataView;
import plugins.regions.initialdata.RegionInitialData;
import plugins.regions.support.RegionId;
import plugins.regions.testsupport.TestRegionId;
import plugins.reports.ReportPlugin;
import plugins.reports.initialdata.ReportsInitialData;
import plugins.stochastics.StochasticsPlugin;
import plugins.stochastics.initialdata.StochasticsInitialData;
import util.annotations.UnitTest;
import util.annotations.UnitTestConstructor;
import util.annotations.UnitTestMethod;

@UnitTest(target = PersonPropertyChangeObservationEvent.class)
public class AT_PersonPropertyChangeObservationEvent implements Event {

	private void testConsumer(int initialPopulation, long seed, Consumer<AgentContext> consumer) {
		ActionPlugin.Builder pluginBuilder = ActionPlugin.builder();
		pluginBuilder.addAgent("agent");
		pluginBuilder.addAgentActionPlan("agent", new AgentActionPlan(0, consumer));
		testConsumers(initialPopulation, seed, pluginBuilder.build());
	}

	private void testConsumers(int initialPopulation, long seed, ActionPlugin actionPlugin) {

		Builder builder = Simulation.builder();

		// add the person property plugin
		PersonPropertyInitialData.Builder personPropertyBuilder = PersonPropertyInitialData.builder();
		for (TestPersonPropertyId testPersonPropertyId : TestPersonPropertyId.values()) {
			personPropertyBuilder.definePersonProperty(testPersonPropertyId, testPersonPropertyId.getPropertyDefinition());
		}

		builder.addPlugin(PersonPropertiesPlugin.PLUGIN_ID, new PersonPropertiesPlugin(personPropertyBuilder.build())::init);

		// add the people plugin
		builder.addPlugin(PartitionsPlugin.PLUGIN_ID, new PartitionsPlugin()::init);
		PeopleInitialData.Builder peopleBuilder = PeopleInitialData.builder();
		List<PersonId> people = new ArrayList<>();
		for (int i = 0; i < initialPopulation; i++) {
			people.add(new PersonId(i));
		}

		for (PersonId personId : people) {
			peopleBuilder.addPersonId(personId);
		}

		builder.addPlugin(PeoplePlugin.PLUGIN_ID, new PeoplePlugin(peopleBuilder.build())::init);

		// add the properties plugin
		builder.addPlugin(PropertiesPlugin.PLUGIN_ID, new PropertiesPlugin()::init);

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

		builder.addPlugin(CompartmentPlugin.PLUGIN_ID, new CompartmentPlugin(compartmentBuilder.build())::init);

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

		builder.addPlugin(RegionPlugin.PLUGIN_ID, new RegionPlugin(regionBuilder.build())::init);

		// add the component plugin
		builder.addPlugin(ComponentPlugin.PLUGIN_ID, new ComponentPlugin()::init);

		// add the report plugin
		builder.addPlugin(ReportPlugin.PLUGIN_ID, new ReportPlugin(ReportsInitialData.builder().build())::init);

		// add the stochastics plugin
		builder.addPlugin(StochasticsPlugin.PLUGIN_ID, new StochasticsPlugin(StochasticsInitialData.builder().setSeed(seed).build())::init);

		// add the action plugin
		builder.addPlugin(ActionPlugin.PLUGIN_ID, actionPlugin::init);

		// build and execute the engine
		builder.build().execute();

		// show that all actions were executed
		assertTrue(actionPlugin.allActionsExecuted());

	}

	@Test
	@UnitTestConstructor(args = { PersonId.class, PersonPropertyId.class, Object.class, Object.class })
	public void testConstructor() {
		// nothing to test
	}

	@Test
	@UnitTestMethod(name = "getCurrentPropertyValue", args = {})
	public void testGetCurrentPropertyValue() {
		PersonId personId = new PersonId(10);
		PersonPropertyId personPropertyId = TestPersonPropertyId.PERSON_PROPERTY_1_BOOLEAN_MUTABLE_NO_TRACK;
		Object previousValue = 0;
		for (int i = 0; i < 10; i++) {
			Object currentValue = i;
			PersonPropertyChangeObservationEvent personPropertyChangeObservationEvent = new PersonPropertyChangeObservationEvent(personId, personPropertyId, previousValue, currentValue);
			assertEquals(currentValue, personPropertyChangeObservationEvent.getCurrentPropertyValue());
		}
	}

	@Test
	@UnitTestMethod(name = "getPersonPropertyId", args = {})
	public void testGetPersonPropertyId() {
		PersonId personId = new PersonId(10);
		Object previousValue = 0;
		Object currentValue = 1;
		for (TestPersonPropertyId testPersonPropertyId : TestPersonPropertyId.values()) {
			PersonPropertyChangeObservationEvent personPropertyChangeObservationEvent = new PersonPropertyChangeObservationEvent(personId, testPersonPropertyId, previousValue, currentValue);
			assertEquals(testPersonPropertyId, personPropertyChangeObservationEvent.getPersonPropertyId());
		}
	}

	@Test
	@UnitTestMethod(name = "getPersonId", args = {})
	public void testGetPersonId() {
		PersonPropertyId personPropertyId = TestPersonPropertyId.PERSON_PROPERTY_1_BOOLEAN_MUTABLE_NO_TRACK;
		Object previousValue = 0;
		Object currentValue = 1;
		for (int i = 0; i < 10; i++) {
			PersonId personId = new PersonId(i);
			PersonPropertyChangeObservationEvent personPropertyChangeObservationEvent = new PersonPropertyChangeObservationEvent(personId, personPropertyId, previousValue, currentValue);
			assertEquals(personId, personPropertyChangeObservationEvent.getPersonId());
		}
	}

	@Test
	@UnitTestMethod(name = "getPreviousPropertyValue", args = {})
	public void testGetPreviousPropertyValue() {
		PersonId personId = new PersonId(10);
		PersonPropertyId personPropertyId = TestPersonPropertyId.PERSON_PROPERTY_1_BOOLEAN_MUTABLE_NO_TRACK;

		Object currentValue = 1;
		for (int i = 0; i < 10; i++) {
			Object previousValue = i;
			PersonPropertyChangeObservationEvent personPropertyChangeObservationEvent = new PersonPropertyChangeObservationEvent(personId, personPropertyId, previousValue, currentValue);
			assertEquals(previousValue, personPropertyChangeObservationEvent.getPreviousPropertyValue());
		}

	}

	@Test
	@UnitTestMethod(name = "toString", args = {})
	public void testToString() {
		PersonId personId = new PersonId(10);
		PersonPropertyId personPropertyId = TestPersonPropertyId.PERSON_PROPERTY_1_BOOLEAN_MUTABLE_NO_TRACK;
		Object previousValue = 0;
		Object currentValue = 1;
		PersonPropertyChangeObservationEvent personPropertyChangeObservationEvent = new PersonPropertyChangeObservationEvent(personId, personPropertyId, previousValue, currentValue);
		String actualValue = personPropertyChangeObservationEvent.toString();
		String expectedValue = "PersonPropertyChangeObservationEvent [personId=10, personPropertyId=PERSON_PROPERTY_1_BOOLEAN_MUTABLE_NO_TRACK, previousPropertyValue=0, currentPropertyValue=1]";
		assertEquals(actualValue, expectedValue);
	}

	@Test
	@UnitTestMethod(name = "getEventLabelByCompartmentAndProperty", args = { Context.class, CompartmentId.class, PersonPropertyId.class })
	public void testGetEventLabelByCompartmentAndProperty() {

		testConsumer(0, 7660943930243490312L, (c) -> {
			CompartmentLocationDataView compartmentLocationDataView = c.getDataView(CompartmentLocationDataView.class).get();
			
			Set<EventLabel<PersonPropertyChangeObservationEvent>> eventLabels = new LinkedHashSet<>();

			for (TestCompartmentId testCompartmentId : TestCompartmentId.values()) {
				for (TestPersonPropertyId testPersonPropertyId : TestPersonPropertyId.values()) {

					EventLabel<PersonPropertyChangeObservationEvent> eventLabel = PersonPropertyChangeObservationEvent.getEventLabelByCompartmentAndProperty(c, testCompartmentId,
							testPersonPropertyId);

					// show that the event label has the correct event class
					assertEquals(PersonPropertyChangeObservationEvent.class, eventLabel.getEventClass());

					// show that the event label has the correct primary key
					assertEquals(testPersonPropertyId, eventLabel.getPrimaryKeyValue());

					// show that the event label has the same id as its
					// associated labeler
					EventLabeler<PersonPropertyChangeObservationEvent> eventLabeler = PersonPropertyChangeObservationEvent.getEventLabelerForCompartmentAndProperty(compartmentLocationDataView);
					assertEquals(eventLabeler.getId(), eventLabel.getLabelerId());

					// show that two event labels with the same inputs are equal
					EventLabel<PersonPropertyChangeObservationEvent> eventLabel2 = PersonPropertyChangeObservationEvent.getEventLabelByCompartmentAndProperty(c, testCompartmentId,
							testPersonPropertyId);
					assertEquals(eventLabel, eventLabel2);

					// show that equal event labels have equal hash codes
					assertEquals(eventLabel.hashCode(), eventLabel2.hashCode());

					// show that two event labels with different inputs are not
					// equal
					assertTrue(eventLabels.add(eventLabel));
				}
			}
		});
	}

	@Test
	@UnitTestMethod(name = "getEventLabelerForCompartmentAndProperty", args = {})
	public void testGetEventLabelerForCompartmentAndProperty() {
		 
		testConsumer(50, 1683420326422351068L, (c) -> {
			CompartmentLocationDataView compartmentLocationDataView = c.getDataView(CompartmentLocationDataView.class).get();

			// create an event labeler
			EventLabeler<PersonPropertyChangeObservationEvent> eventLabeler = PersonPropertyChangeObservationEvent.getEventLabelerForCompartmentAndProperty(compartmentLocationDataView);

			// show that the event labeler has the correct event class
			assertEquals(PersonPropertyChangeObservationEvent.class, eventLabeler.getEventClass());

			// show that the event labeler produces the expected event label
			for (TestCompartmentId testCompartmentId : TestCompartmentId.values()) {
				
				//we will need to select a person from the compartment to run this test correctly
				List<PersonId> peopleInCompartment = compartmentLocationDataView.getPeopleInCompartment(testCompartmentId);
				if(peopleInCompartment.isEmpty()) {
					continue;
				}
				PersonId personId = peopleInCompartment.get(0);
				for (TestPersonPropertyId testPersonPropertyId : TestPersonPropertyId.values()) {

					// derive the expected event label for this event
					EventLabel<PersonPropertyChangeObservationEvent> expectedEventLabel = PersonPropertyChangeObservationEvent.getEventLabelByCompartmentAndProperty(c, testCompartmentId, testPersonPropertyId);

					// show that the event label and event labeler have equal id
					// values
					assertEquals(expectedEventLabel.getLabelerId(), eventLabeler.getId());

					// create an event
					PersonPropertyChangeObservationEvent event = new PersonPropertyChangeObservationEvent(personId, testPersonPropertyId, 1, 2);

					// show that the event labeler produces the correct an event
					// label
					EventLabel<PersonPropertyChangeObservationEvent> actualEventLabel = eventLabeler.getEventLabel(c, event);

					assertEquals(expectedEventLabel, actualEventLabel);

				}
			}
		});		
	}

	@Test
	@UnitTestMethod(name = "getEventLabelByPersonAndProperty", args = { Context.class, PersonId.class, PersonPropertyId.class })
	public void testGetEventLabelByPersonAndProperty() {

		testConsumer(5, 4447674464104241765L, (c) -> {
			PersonDataView personDataView = c.getDataView(PersonDataView.class).get();
			List<PersonId> people = personDataView.getPeople();

			Set<EventLabel<PersonPropertyChangeObservationEvent>> eventLabels = new LinkedHashSet<>();

			for (PersonId personId : people) {
				for (TestPersonPropertyId testPersonPropertyId : TestPersonPropertyId.values()) {

					EventLabel<PersonPropertyChangeObservationEvent> eventLabel = PersonPropertyChangeObservationEvent.getEventLabelByPersonAndProperty(c, personId, testPersonPropertyId);

					// show that the event label has the correct event class
					assertEquals(PersonPropertyChangeObservationEvent.class, eventLabel.getEventClass());

					// show that the event label has the correct primary key
					assertEquals(testPersonPropertyId, eventLabel.getPrimaryKeyValue());

					// show that the event label has the same id as its
					// associated labeler
					EventLabeler<PersonPropertyChangeObservationEvent> eventLabeler = PersonPropertyChangeObservationEvent.getEventLabelerForPersonAndProperty();
					assertEquals(eventLabeler.getId(), eventLabel.getLabelerId());

					// show that two event labels with the same inputs are equal
					EventLabel<PersonPropertyChangeObservationEvent> eventLabel2 = PersonPropertyChangeObservationEvent.getEventLabelByPersonAndProperty(c, personId, testPersonPropertyId);
					assertEquals(eventLabel, eventLabel2);

					// show that equal event labels have equal hash codes
					assertEquals(eventLabel.hashCode(), eventLabel2.hashCode());

					// show that two event labels with different inputs are not
					// equal
					assertTrue(eventLabels.add(eventLabel));
				}
			}
		});

	}

	@Test
	@UnitTestMethod(name = "getEventLabelerForPersonAndProperty", args = {})
	public void testGetEventLabelerForPersonAndProperty() {

		testConsumer(5, 1295505199200349679L, (c) -> {
			PersonDataView personDataView = c.getDataView(PersonDataView.class).get();
			List<PersonId> people = personDataView.getPeople();

			// create an event labeler
			EventLabeler<PersonPropertyChangeObservationEvent> eventLabeler = PersonPropertyChangeObservationEvent.getEventLabelerForPersonAndProperty();

			// show that the event labeler has the correct event class
			assertEquals(PersonPropertyChangeObservationEvent.class, eventLabeler.getEventClass());

			// show that the event labeler produces the expected event label
			for (PersonId personId : people) {
				for (TestPersonPropertyId testPersonPropertyId : TestPersonPropertyId.values()) {

					// derive the expected event label for this event
					EventLabel<PersonPropertyChangeObservationEvent> expectedEventLabel = PersonPropertyChangeObservationEvent.getEventLabelByPersonAndProperty(c, personId, testPersonPropertyId);

					// show that the event label and event labeler have equal id
					// values
					assertEquals(expectedEventLabel.getLabelerId(), eventLabeler.getId());

					// create an event
					PersonPropertyChangeObservationEvent event = new PersonPropertyChangeObservationEvent(personId, testPersonPropertyId, 1, 2);

					// show that the event labeler produces the correct an event
					// label
					EventLabel<PersonPropertyChangeObservationEvent> actualEventLabel = eventLabeler.getEventLabel(c, event);

					assertEquals(expectedEventLabel, actualEventLabel);

				}
			}
		});
	}

	@Test
	@UnitTestMethod(name = "getEventLabelByProperty", args = { Context.class, PersonPropertyId.class })
	public void testGetEventLabelByProperty() {

		testConsumer(0, 3639063830450063191L, (c) -> {

			Set<EventLabel<PersonPropertyChangeObservationEvent>> eventLabels = new LinkedHashSet<>();

			for (TestPersonPropertyId testPersonPropertyId : TestPersonPropertyId.values()) {

				EventLabel<PersonPropertyChangeObservationEvent> eventLabel = PersonPropertyChangeObservationEvent.getEventLabelByProperty(c, testPersonPropertyId);

				// show that the event label has the correct event class
				assertEquals(PersonPropertyChangeObservationEvent.class, eventLabel.getEventClass());

				// show that the event label has the correct primary key
				assertEquals(testPersonPropertyId, eventLabel.getPrimaryKeyValue());

				// show that the event label has the same id as its
				// associated labeler
				EventLabeler<PersonPropertyChangeObservationEvent> eventLabeler = PersonPropertyChangeObservationEvent.getEventLabelerForProperty();
				assertEquals(eventLabeler.getId(), eventLabel.getLabelerId());

				// show that two event labels with the same inputs are equal
				EventLabel<PersonPropertyChangeObservationEvent> eventLabel2 = PersonPropertyChangeObservationEvent.getEventLabelByProperty(c, testPersonPropertyId);
				assertEquals(eventLabel, eventLabel2);

				// show that equal event labels have equal hash codes
				assertEquals(eventLabel.hashCode(), eventLabel2.hashCode());

				// show that two event labels with different inputs are not
				// equal
				assertTrue(eventLabels.add(eventLabel));
			}

		});

	}

	@Test
	@UnitTestMethod(name = "getEventLabelerForProperty", args = {})
	public void testGetEventLabelerForProperty() {

 
		testConsumer(0, 1006134798657400111L, (c) -> {
			

			// create an event labeler
			EventLabeler<PersonPropertyChangeObservationEvent> eventLabeler = PersonPropertyChangeObservationEvent.getEventLabelerForProperty();

			// show that the event labeler has the correct event class
			assertEquals(PersonPropertyChangeObservationEvent.class, eventLabeler.getEventClass());

			// show that the event labeler produces the expected event label
			
				
				
				for (TestPersonPropertyId testPersonPropertyId : TestPersonPropertyId.values()) {

					// derive the expected event label for this event
					EventLabel<PersonPropertyChangeObservationEvent> expectedEventLabel = PersonPropertyChangeObservationEvent.getEventLabelByProperty(c, testPersonPropertyId);

					// show that the event label and event labeler have equal id
					// values
					assertEquals(expectedEventLabel.getLabelerId(), eventLabeler.getId());

					// create an event
					PersonPropertyChangeObservationEvent event = new PersonPropertyChangeObservationEvent(new PersonId(0), testPersonPropertyId, 1, 2);

					// show that the event labeler produces the correct an event
					// label
					EventLabel<PersonPropertyChangeObservationEvent> actualEventLabel = eventLabeler.getEventLabel(c, event);

					assertEquals(expectedEventLabel, actualEventLabel);

				}
			
		});	

	}

	@Test
	@UnitTestMethod(name = "getEventLabelByRegionAndProperty", args = { Context.class, RegionId.class, PersonPropertyId.class })
	public void testGetEventLabelByRegionAndProperty() {
		testConsumer(0, 7020781813930698612L, (c) -> {

			RegionLocationDataView regionLocationDataView = c.getDataView(RegionLocationDataView.class).get();
			
			Set<EventLabel<PersonPropertyChangeObservationEvent>> eventLabels = new LinkedHashSet<>();

			for (TestRegionId testRegionId : TestRegionId.values()) {
				for (TestPersonPropertyId testPersonPropertyId : TestPersonPropertyId.values()) {

					EventLabel<PersonPropertyChangeObservationEvent> eventLabel = PersonPropertyChangeObservationEvent.getEventLabelByRegionAndProperty(c, testRegionId, testPersonPropertyId);

					// show that the event label has the correct event class
					assertEquals(PersonPropertyChangeObservationEvent.class, eventLabel.getEventClass());

					// show that the event label has the correct primary key
					assertEquals(testPersonPropertyId, eventLabel.getPrimaryKeyValue());

					// show that the event label has the same id as its
					// associated labeler
					EventLabeler<PersonPropertyChangeObservationEvent> eventLabeler = PersonPropertyChangeObservationEvent.getEventLabelerForRegionAndProperty(regionLocationDataView);
					assertEquals(eventLabeler.getId(), eventLabel.getLabelerId());

					// show that two event labels with the same inputs are equal
					EventLabel<PersonPropertyChangeObservationEvent> eventLabel2 = PersonPropertyChangeObservationEvent.getEventLabelByRegionAndProperty(c, testRegionId, testPersonPropertyId);
					assertEquals(eventLabel, eventLabel2);

					// show that equal event labels have equal hash codes
					assertEquals(eventLabel.hashCode(), eventLabel2.hashCode());

					// show that two event labels with different inputs are not
					// equal
					assertTrue(eventLabels.add(eventLabel));
				}
			}
		});

	}

	@Test
	@UnitTestMethod(name = "getEventLabelerForRegionAndProperty", args = {})
	public void testGetEventLabelerForRegionAndProperty() {
	 
		testConsumer(50, 7370040718450691849L, (c) -> {
			RegionLocationDataView regionLocationDataView = c.getDataView(RegionLocationDataView.class).get();

			// create an event labeler
			EventLabeler<PersonPropertyChangeObservationEvent> eventLabeler = PersonPropertyChangeObservationEvent.getEventLabelerForRegionAndProperty(regionLocationDataView);

			// show that the event labeler has the correct event class
			assertEquals(PersonPropertyChangeObservationEvent.class, eventLabeler.getEventClass());

			// show that the event labeler produces the expected event label
			for (TestRegionId testRegionId : TestRegionId.values()) {
				
				//we will need to select a person from the region to run this test correctly
				List<PersonId> peopleInRegion = regionLocationDataView.getPeopleInRegion(testRegionId);
				if(peopleInRegion.isEmpty()) {
					continue;
				}
				PersonId personId = peopleInRegion.get(0);
				for (TestPersonPropertyId testPersonPropertyId : TestPersonPropertyId.values()) {

					// derive the expected event label for this event
					EventLabel<PersonPropertyChangeObservationEvent> expectedEventLabel = PersonPropertyChangeObservationEvent.getEventLabelByRegionAndProperty(c, testRegionId, testPersonPropertyId);

					// show that the event label and event labeler have equal id
					// values
					assertEquals(expectedEventLabel.getLabelerId(), eventLabeler.getId());

					// create an event
					PersonPropertyChangeObservationEvent event = new PersonPropertyChangeObservationEvent(personId, testPersonPropertyId, 1, 2);

					// show that the event labeler produces the correct an event
					// label
					EventLabel<PersonPropertyChangeObservationEvent> actualEventLabel = eventLabeler.getEventLabel(c, event);

					assertEquals(expectedEventLabel, actualEventLabel);

				}
			}
		});	

	}

	@Test
	@UnitTestMethod(name = "getPrimaryKeyValue", args = {})
	public void testGetPrimaryKeyValue() {

		PersonId personId = new PersonId(10);
		Object previousValue = 0;
		Object currentValue = 1;
		for (TestPersonPropertyId testPersonPropertyId : TestPersonPropertyId.values()) {
			PersonPropertyChangeObservationEvent personPropertyChangeObservationEvent = new PersonPropertyChangeObservationEvent(personId, testPersonPropertyId, previousValue, currentValue);
			assertEquals(testPersonPropertyId, personPropertyChangeObservationEvent.getPrimaryKeyValue());
		}
	}

}
