package plugins.compartments.events.observation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import javax.naming.Context;

import org.junit.jupiter.api.Test;

import nucleus.Engine;
import nucleus.Engine.EngineBuilder;
import nucleus.EventLabel;
import nucleus.EventLabeler;
import nucleus.testsupport.actionplugin.ActionPlugin;
import nucleus.testsupport.actionplugin.AgentActionPlan;
import plugins.compartments.CompartmentPlugin;
import plugins.compartments.initialdata.CompartmentInitialData;
import plugins.compartments.support.CompartmentId;
import plugins.compartments.support.CompartmentPropertyId;
import plugins.compartments.testsupport.TestCompartmentId;
import plugins.components.ComponentPlugin;
import plugins.partitions.PartitionsPlugin;
import plugins.people.PeoplePlugin;
import plugins.people.initialdata.PeopleInitialData;
import plugins.properties.PropertiesPlugin;
import plugins.properties.support.PropertyDefinition;
import plugins.reports.ReportPlugin;
import plugins.reports.initialdata.ReportsInitialData;
import plugins.stochastics.StochasticsPlugin;
import plugins.stochastics.initialdata.StochasticsInitialData;
import util.annotations.UnitTest;
import util.annotations.UnitTestConstructor;
import util.annotations.UnitTestMethod;

@UnitTest(target = CompartmentPropertyChangeObservationEvent.class)
public class AT_CompartmentPropertyChangeObservationEvent {

	@Test
	@UnitTestConstructor(args = { CompartmentId.class, CompartmentPropertyId.class, Object.class, Object.class })
	public void testConstructor() {
		CompartmentId compartmentId = TestCompartmentId.COMPARTMENT_2;
		CompartmentPropertyId compartmentPropertyId = TestCompartmentId.COMPARTMENT_2.getCompartmentPropertyId(0);
		Object previousValue = 5;
		Object currentValue = 6;
		CompartmentPropertyChangeObservationEvent event = new CompartmentPropertyChangeObservationEvent(compartmentId, compartmentPropertyId, previousValue, currentValue);
		assertNotNull(event);
	}

	@Test
	@UnitTestMethod(name = "getCompartmentId", args = {})
	public void tetGetCompartmentId() {
		CompartmentId expectedCompartmentId = TestCompartmentId.COMPARTMENT_2;
		CompartmentPropertyId compartmentPropertyId = TestCompartmentId.COMPARTMENT_2.getCompartmentPropertyId(0);
		Object previousValue = 5;
		Object currentValue = 6;
		CompartmentPropertyChangeObservationEvent event = new CompartmentPropertyChangeObservationEvent(expectedCompartmentId, compartmentPropertyId, previousValue, currentValue);
		CompartmentId actualCompartmentId = event.getCompartmentId();
		assertEquals(expectedCompartmentId, actualCompartmentId);
	}

	@Test
	@UnitTestMethod(name = "getCompartmentPropertyId", args = {})
	public void testGetCompartmentPropertyId() {
		CompartmentId compartmentId = TestCompartmentId.COMPARTMENT_2;
		CompartmentPropertyId expectedCompartmentPropertyId = TestCompartmentId.COMPARTMENT_2.getCompartmentPropertyId(0);
		Object previousValue = 5;
		Object currentValue = 6;
		CompartmentPropertyChangeObservationEvent event = new CompartmentPropertyChangeObservationEvent(compartmentId, expectedCompartmentPropertyId, previousValue, currentValue);
		CompartmentPropertyId actualCompartmentPropertyId = event.getCompartmentPropertyId();
		assertEquals(expectedCompartmentPropertyId, actualCompartmentPropertyId);
	}

	@Test
	@UnitTestMethod(name = "getCurrentPropertyValue", args = {})
	public void testGetCurrentPropertyValue() {
		CompartmentId compartmentId = TestCompartmentId.COMPARTMENT_2;
		CompartmentPropertyId compartmentPropertyId = TestCompartmentId.COMPARTMENT_2.getCompartmentPropertyId(0);
		Object previousValue = 5;
		Object expectedCurrentValue = 6;
		CompartmentPropertyChangeObservationEvent event = new CompartmentPropertyChangeObservationEvent(compartmentId, compartmentPropertyId, previousValue, expectedCurrentValue);
		Object actualCurrentPropertyValue = event.getCurrentPropertyValue();
		assertEquals(expectedCurrentValue, actualCurrentPropertyValue);
	}

	@Test
	@UnitTestMethod(name = "getPreviousPropertyValue", args = {})
	public void testGetPreviousPropertyValue() {
		CompartmentId compartmentId = TestCompartmentId.COMPARTMENT_2;
		CompartmentPropertyId compartmentPropertyId = TestCompartmentId.COMPARTMENT_2.getCompartmentPropertyId(0);
		Object expectedPreviousValue = 5;
		Object currentValue = 6;
		CompartmentPropertyChangeObservationEvent event = new CompartmentPropertyChangeObservationEvent(compartmentId, compartmentPropertyId, expectedPreviousValue, currentValue);
		Object actualPreviousPropertyValue = event.getPreviousPropertyValue();
		assertEquals(expectedPreviousValue, actualPreviousPropertyValue);
	}

	@Test
	@UnitTestMethod(name = "getPrimaryKeyValue", args = {})
	public void testGetPrimaryKeyValue() {
		for (TestCompartmentId testCompartmentId : TestCompartmentId.values()) {
			for (CompartmentPropertyId compartmentPropertyId : testCompartmentId.getCompartmentPropertyIds()) {
				Object previousValue = 5;
				Object currentValue = 6;
				CompartmentPropertyChangeObservationEvent event = new CompartmentPropertyChangeObservationEvent(testCompartmentId, compartmentPropertyId, previousValue, currentValue);
				assertEquals(compartmentPropertyId, event.getPrimaryKeyValue());
			}
		}
	}

	@Test
	@UnitTestMethod(name = "toString", args = {})
	public void testToString() {
		CompartmentId compartmentId = TestCompartmentId.COMPARTMENT_2;
		CompartmentPropertyId compartmentPropertyId = TestCompartmentId.COMPARTMENT_2.getCompartmentPropertyId(0);
		Object expectedPreviousValue = 5;
		Object currentValue = 6;
		CompartmentPropertyChangeObservationEvent event = new CompartmentPropertyChangeObservationEvent(compartmentId, compartmentPropertyId, expectedPreviousValue, currentValue);
		String actualValue = event.toString();
		String expectedValue = "CompartmentPropertyChangeObservationEvent [compartmentId=COMPARTMENT_2, compartmentPropertyId=TestCompartmentPropertyId [id=Compartment_Property_2_1], previousPropertyValue=5, currentPropertyValue=6]";
		assertEquals(expectedValue, actualValue);
	}

	@Test
	@UnitTestMethod(name = "getEventLabel", args = { Context.class, CompartmentId.class, CompartmentPropertyId.class })
	public void testGetEventLabel() {

		EngineBuilder engineBuilder = Engine.builder();

		// add the test compartments and their associated property definitions
		int defaultValue = 0;
		CompartmentInitialData.Builder builder = CompartmentInitialData.builder();
		for (TestCompartmentId testCompartmentId : TestCompartmentId.values()) {
			builder.setCompartmentInitialBehaviorSupplier(testCompartmentId, () -> (c) -> {
			});

			for (CompartmentPropertyId compartmentPropertyId : testCompartmentId.getCompartmentPropertyIds()) {
				PropertyDefinition propertyDefinition = PropertyDefinition.builder().setDefaultValue(defaultValue++).setType(Integer.class).build();
				builder.defineCompartmentProperty(testCompartmentId, compartmentPropertyId, propertyDefinition);
			}

		}
		engineBuilder.addPlugin(CompartmentPlugin.PLUGIN_ID, new CompartmentPlugin(builder.build())::init);

		engineBuilder.addPlugin(PeoplePlugin.PLUGIN_ID, new PeoplePlugin(PeopleInitialData.builder().build())::init);
		engineBuilder.addPlugin(StochasticsPlugin.PLUGIN_ID, new StochasticsPlugin(StochasticsInitialData.builder().setSeed(1925120766573695456L).build())::init);
		engineBuilder.addPlugin(ReportPlugin.PLUGIN_ID, new ReportPlugin(ReportsInitialData.builder().build())::init);
		engineBuilder.addPlugin(PropertiesPlugin.PLUGIN_ID, new PropertiesPlugin()::init);
		engineBuilder.addPlugin(ComponentPlugin.PLUGIN_ID, new ComponentPlugin()::init);
		engineBuilder.addPlugin(PartitionsPlugin.PLUGIN_ID, new PartitionsPlugin()::init);

		ActionPlugin.Builder pluginBuilder = ActionPlugin.builder();

		pluginBuilder.addAgent("agent");
		pluginBuilder.addAgentActionPlan("agent", new AgentActionPlan(0, (c) -> {

			for (TestCompartmentId testCompartmentId : TestCompartmentId.values()) {
				for (CompartmentPropertyId compartmentPropertyId : testCompartmentId.getCompartmentPropertyIds()) {
					EventLabel<CompartmentPropertyChangeObservationEvent> eventLabel = CompartmentPropertyChangeObservationEvent.getEventLabel(c, testCompartmentId, compartmentPropertyId);
					assertEquals(CompartmentPropertyChangeObservationEvent.class, eventLabel.getEventClass());
					assertEquals(compartmentPropertyId, eventLabel.getPrimaryKeyValue());
					assertEquals(CompartmentPropertyChangeObservationEvent.getEventLabeler().getId(), eventLabel.getLabelerId());
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
	@UnitTestMethod(name = "getEventLabeler", args = {})
	public void testGetEventLabeler() {
		EngineBuilder engineBuilder = Engine.builder();

		// add the test compartments and their associated property definitions
		int defaultValue = 0;
		CompartmentInitialData.Builder builder = CompartmentInitialData.builder();
		for (TestCompartmentId testCompartmentId : TestCompartmentId.values()) {
			builder.setCompartmentInitialBehaviorSupplier(testCompartmentId, () -> (c) -> {
			});

			for (CompartmentPropertyId compartmentPropertyId : testCompartmentId.getCompartmentPropertyIds()) {
				PropertyDefinition propertyDefinition = PropertyDefinition.builder().setDefaultValue(defaultValue++).setType(Integer.class).build();
				builder.defineCompartmentProperty(testCompartmentId, compartmentPropertyId, propertyDefinition);
			}

		}
		engineBuilder.addPlugin(CompartmentPlugin.PLUGIN_ID, new CompartmentPlugin(builder.build())::init);
		engineBuilder.addPlugin(PeoplePlugin.PLUGIN_ID, new PeoplePlugin(PeopleInitialData.builder().build())::init);
		engineBuilder.addPlugin(StochasticsPlugin.PLUGIN_ID, new StochasticsPlugin(StochasticsInitialData.builder().setSeed(628042077827535235L).build())::init);
		engineBuilder.addPlugin(ReportPlugin.PLUGIN_ID, new ReportPlugin(ReportsInitialData.builder().build())::init);
		engineBuilder.addPlugin(PropertiesPlugin.PLUGIN_ID, new PropertiesPlugin()::init);
		engineBuilder.addPlugin(ComponentPlugin.PLUGIN_ID, new ComponentPlugin()::init);
		engineBuilder.addPlugin(PartitionsPlugin.PLUGIN_ID, new PartitionsPlugin()::init);

		ActionPlugin.Builder pluginBuilder = ActionPlugin.builder();

		pluginBuilder.addAgent("agent");
		pluginBuilder.addAgentActionPlan("agent", new AgentActionPlan(0, (c) -> {
			// show that the event labeler can be constructed has the correct
			// values
			EventLabeler<CompartmentPropertyChangeObservationEvent> eventLabeler = CompartmentPropertyChangeObservationEvent.getEventLabeler();
			assertEquals(CompartmentPropertyChangeObservationEvent.class, eventLabeler.getEventClass());

			for (TestCompartmentId testCompartmentId : TestCompartmentId.values()) {
				for (CompartmentPropertyId compartmentPropertyId : testCompartmentId.getCompartmentPropertyIds()) {
					assertEquals(CompartmentPropertyChangeObservationEvent.getEventLabel(c, testCompartmentId, compartmentPropertyId).getLabelerId(), eventLabeler.getId());

					// show that the event labeler produces the expected event
					// label

					// create an event
					Object previousPropertyValue = 5;
					Object currentPropertyValue = 6;
					CompartmentPropertyChangeObservationEvent event = new CompartmentPropertyChangeObservationEvent(testCompartmentId, compartmentPropertyId, previousPropertyValue,
							currentPropertyValue);

					// derive the expected event label for this event
					EventLabel<CompartmentPropertyChangeObservationEvent> expectedEventLabel = CompartmentPropertyChangeObservationEvent.getEventLabel(c, testCompartmentId, compartmentPropertyId);

					// have the event labeler produce an event label and show it
					// is equal to the expected event label
					EventLabel<CompartmentPropertyChangeObservationEvent> actualEventLabel = eventLabeler.getEventLabel(c, event);
					assertEquals(expectedEventLabel, actualEventLabel);
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

}
