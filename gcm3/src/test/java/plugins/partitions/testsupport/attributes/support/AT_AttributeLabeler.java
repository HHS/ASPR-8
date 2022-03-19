package plugins.partitions.testsupport.attributes.support;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

import org.junit.jupiter.api.Test;

import annotations.UnitTest;
import annotations.UnitTestConstructor;
import annotations.UnitTestMethod;
import nucleus.Plugin;
import nucleus.SimulationContext;
import nucleus.testsupport.testplugin.TestActorPlan;
import nucleus.testsupport.testplugin.TestPlugin;
import nucleus.testsupport.testplugin.TestPluginData;
import nucleus.util.ContractException;
import plugins.partitions.support.LabelerSensitivity;
import plugins.partitions.testsupport.PartitionsActionSupport;
import plugins.partitions.testsupport.attributes.AttributesDataManager;
import plugins.partitions.testsupport.attributes.events.AttributeChangeObservationEvent;
import plugins.people.PersonDataManager;
import plugins.people.support.PersonError;
import plugins.people.support.PersonId;

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
	@UnitTestMethod(name = "getLabel", args = { SimulationContext.class, PersonId.class })
	public void testGetLabel() {
		// build an attribute function
		Function<Object, Object> function = (c) -> {
			Boolean value = (Boolean) c;
			if (value) {
				return "A";
			}
			return "B";
		};

		AttributeLabeler attributeLabeler = new AttributeLabeler(TestAttributeId.BOOLEAN_0, function);

		TestPluginData.Builder pluginBuilder = TestPluginData.builder();

		

		/*
		 * 
		 */
		pluginBuilder.addTestActorPlan("agent", new TestActorPlan(0, (c) -> {
			PersonDataManager personDataManager = c.getDataManager(PersonDataManager.class).get();
			AttributesDataManager attributesDataManager = c.getDataManager(AttributesDataManager.class).get();
			List<PersonId> people = personDataManager.getPeople();
			for (PersonId personId : people) {

				// get the person's attribute value and apply the function
				// directly
				Boolean b0 = attributesDataManager.getAttributeValue(personId, TestAttributeId.BOOLEAN_0);
				Object expectedLabel = function.apply(b0);

				// get the label from the person id
				Object actualLabel = attributeLabeler.getLabel(c, personId);

				// show that the two labels are equal
				assertEquals(expectedLabel, actualLabel);

			}
		}));

		// test preconditions
		pluginBuilder.addTestActorPlan("agent", new TestActorPlan(1, (c) -> {

			// if the person does not exist
			ContractException contractException = assertThrows(ContractException.class, () -> attributeLabeler.getLabel(c, new PersonId(-1)));
			assertEquals(PersonError.UNKNOWN_PERSON_ID, contractException.getErrorType());

			// if the person id is null
			contractException = assertThrows(ContractException.class, () -> attributeLabeler.getLabel(c, null));
			assertEquals(PersonError.NULL_PERSON_ID, contractException.getErrorType());

		}));

		TestPluginData testPluginData = pluginBuilder.build();
		Plugin testPlugin = TestPlugin.getPlugin(testPluginData);
		PartitionsActionSupport.testConsumers(10, 4676319446289433016L, testPlugin);

	}

	@Test
	@UnitTestMethod(name = "getDimension", args = {})
	public void testGetDimension() {
		for (TestAttributeId testAttributeId : TestAttributeId.values()) {
			assertEquals(testAttributeId, new AttributeLabeler(testAttributeId, (c) -> null).getDimension());
		}
	}

}
