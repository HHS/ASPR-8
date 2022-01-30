package plugins.partitions.testsupport.attributes.support;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

import org.junit.jupiter.api.Test;

import nucleus.Context;
import nucleus.Simulation;
import nucleus.Simulation.Builder;
import nucleus.testsupport.actionplugin.ActionPlugin;
import nucleus.testsupport.actionplugin.AgentActionPlan;
import plugins.components.ComponentPlugin;
import plugins.partitions.PartitionsPlugin;
import plugins.partitions.support.LabelerSensitivity;
import plugins.partitions.testsupport.attributes.AttributesPlugin;
import plugins.partitions.testsupport.attributes.datacontainers.AttributesDataView;
import plugins.partitions.testsupport.attributes.events.observation.AttributeChangeObservationEvent;
import plugins.partitions.testsupport.attributes.initialdata.AttributeInitialData;
import plugins.people.PeoplePlugin;
import plugins.people.datacontainers.PersonDataView;
import plugins.people.initialdata.PeopleInitialData;
import plugins.people.support.PersonError;
import plugins.people.support.PersonId;
import plugins.reports.ReportPlugin;
import plugins.reports.initialdata.ReportsInitialData;
import plugins.stochastics.StochasticsPlugin;
import plugins.stochastics.initialdata.StochasticsInitialData;
import util.ContractException;
import util.annotations.UnitTest;
import util.annotations.UnitTestConstructor;
import util.annotations.UnitTestMethod;

@UnitTest(target = AttributeLabeler.class)
public final class AT_AttributeLabeler {

	@Test
	@UnitTestConstructor(args = { AttributeId.class, Function.class })
	public void testConstructor() {
		// nothing to test
	}

	@Test
	@UnitTestMethod(name = "getLabelerSensitivities", args = { AttributeId.class, Function.class })
	public void testGetLabelerSensitivities() {
		/*
		 * Get the labeler sensitivities and show that they are consistent with
		 * their documented behaviors.
		 */

		AttributeLabeler attributeLabeler = new AttributeLabeler(TestAttributeId.BOOLEAN_0, (c) -> null);

		Set<LabelerSensitivity<?>> labelerSensitivities = attributeLabeler.getLabelerSensitivities();

		// show that there is exactly one sensitivity
		assertEquals(1, labelerSensitivities.size());

		// show that the sensitivity is associated with
		// AttributeChangeObservationEvent
		LabelerSensitivity<?> labelerSensitivity = labelerSensitivities.iterator().next();
		assertEquals(AttributeChangeObservationEvent.class, labelerSensitivity.getEventClass());

		// show that the sensitivity will return the person id from a
		// AttributeChangeObservationEvent
		PersonId personId = new PersonId(56);
		AttributeChangeObservationEvent attributeChangeObservationEvent = new AttributeChangeObservationEvent(personId, TestAttributeId.BOOLEAN_0, false, true);
		Optional<PersonId> optional = labelerSensitivity.getPersonId(attributeChangeObservationEvent);
		assertTrue(optional.isPresent());
		assertEquals(personId, optional.get());

	}

	@Test
	@UnitTestMethod(name = "getLabel", args = { Context.class, PersonId.class })
	public void testGetLabel() {

		Builder builder = Simulation.builder();

		AttributeInitialData.Builder attributeBuilder = AttributeInitialData.builder();
		for (TestAttributeId testAttributeId : TestAttributeId.values()) {
			attributeBuilder.defineAttribute(testAttributeId, testAttributeId.getAttributeDefinition());
		}

		builder.addPlugin(AttributesPlugin.PLUGIN_ID, new AttributesPlugin(attributeBuilder.build())::init);

		// add the remaining plugins
		PeopleInitialData.Builder peopleBuilder = PeopleInitialData.builder();
		for (int i = 0; i < 10; i++) {
			peopleBuilder.addPersonId(new PersonId(i));
		}
		builder.addPlugin(PeoplePlugin.PLUGIN_ID, new PeoplePlugin(peopleBuilder.build())::init);
		builder.addPlugin(ComponentPlugin.PLUGIN_ID, new ComponentPlugin()::init);
		builder.addPlugin(StochasticsPlugin.PLUGIN_ID, new StochasticsPlugin(StochasticsInitialData.builder().setSeed(4676319446289433016L).build())::init);
		builder.addPlugin(ReportPlugin.PLUGIN_ID, new ReportPlugin(ReportsInitialData.builder().build())::init);
		builder.addPlugin(PartitionsPlugin.PLUGIN_ID, new PartitionsPlugin()::init);

		// build an attribute function
		Function<Object, Object> function = (c) -> {
			Boolean value = (Boolean) c;
			if (value) {
				return "A";
			}
			return "B";
		};

		AttributeLabeler attributeLabeler = new AttributeLabeler(TestAttributeId.BOOLEAN_0, function);

		ActionPlugin.Builder pluginBuilder = ActionPlugin.builder();

		// add the test agent
		pluginBuilder.addAgent("agent");

		/*
		 * 
		 */
		pluginBuilder.addAgentActionPlan("agent", new AgentActionPlan(0, (c) -> {
			PersonDataView personDataView = c.getDataView(PersonDataView.class).get();
			AttributesDataView attributesDataView = c.getDataView(AttributesDataView.class).get();
			List<PersonId> people = personDataView.getPeople();
			for (PersonId personId : people) {

				// get the person's attribute value and apply the function
				// directly
				Boolean b0 = attributesDataView.getAttributeValue(personId, TestAttributeId.BOOLEAN_0);
				Object expectedLabel = function.apply(b0);

				// get the label from the person id
				Object actualLabel = attributeLabeler.getLabel(c, personId);

				// show that the two labels are equal
				assertEquals(expectedLabel, actualLabel);

			}
		}));

		// test preconditions
		pluginBuilder.addAgentActionPlan("agent", new AgentActionPlan(1, (c) -> {

			// if the person does not exist
			ContractException contractException = assertThrows(ContractException.class, () -> attributeLabeler.getLabel(c, new PersonId(-1)));
			assertEquals(PersonError.UNKNOWN_PERSON_ID, contractException.getErrorType());

			// if the person id is null
			contractException = assertThrows(ContractException.class, () -> attributeLabeler.getLabel(c, null));
			assertEquals(PersonError.NULL_PERSON_ID, contractException.getErrorType());

		}));

		ActionPlugin actionPlugin = pluginBuilder.build();
		builder.addPlugin(ActionPlugin.PLUGIN_ID, actionPlugin::init);

		// build and execute the engine
		builder.build().execute();

		// show that all actions were executed
		assertTrue(actionPlugin.allActionsExecuted());
	}

	@Test
	@UnitTestMethod(name = "getDimension", args = {})
	public void testGetDimension() {
		for (TestAttributeId testAttributeId : TestAttributeId.values()) {
			assertEquals(testAttributeId, new AttributeLabeler(testAttributeId, (c) -> null).getDimension());
		}
	}

}
