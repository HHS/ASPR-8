package gov.hhs.aspr.ms.gcm.simulation.plugins.partitions.testsupport.attributes.support;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

import org.apache.commons.math3.random.RandomGenerator;
import org.junit.jupiter.api.Test;

import gov.hhs.aspr.ms.gcm.simulation.nucleus.Event;
import gov.hhs.aspr.ms.gcm.simulation.nucleus.testsupport.testplugin.TestActorPlan;
import gov.hhs.aspr.ms.gcm.simulation.nucleus.testsupport.testplugin.TestPluginData;
import gov.hhs.aspr.ms.gcm.simulation.nucleus.testsupport.testplugin.TestSimulation;
import gov.hhs.aspr.ms.gcm.simulation.plugins.partitions.support.LabelerSensitivity;
import gov.hhs.aspr.ms.gcm.simulation.plugins.partitions.support.PartitionsContext;
import gov.hhs.aspr.ms.gcm.simulation.plugins.partitions.testsupport.FunctionalAttributeLabeler;
import gov.hhs.aspr.ms.gcm.simulation.plugins.partitions.testsupport.PartitionsTestPluginFactory;
import gov.hhs.aspr.ms.gcm.simulation.plugins.partitions.testsupport.PartitionsTestPluginFactory.Factory;
import gov.hhs.aspr.ms.gcm.simulation.plugins.partitions.testsupport.TestPartitionsContext;
import gov.hhs.aspr.ms.gcm.simulation.plugins.partitions.testsupport.attributes.AttributesDataManager;
import gov.hhs.aspr.ms.gcm.simulation.plugins.partitions.testsupport.attributes.events.AttributeUpdateEvent;
import gov.hhs.aspr.ms.gcm.simulation.plugins.people.datamanagers.PeopleDataManager;
import gov.hhs.aspr.ms.gcm.simulation.plugins.people.support.PersonError;
import gov.hhs.aspr.ms.gcm.simulation.plugins.people.support.PersonId;
import gov.hhs.aspr.ms.util.annotations.UnitTestConstructor;
import gov.hhs.aspr.ms.util.annotations.UnitTestMethod;
import gov.hhs.aspr.ms.util.errors.ContractException;
import gov.hhs.aspr.ms.util.random.RandomGeneratorProvider;

public final class AT_AttributeLabeler {

	@Test
	@UnitTestConstructor(target = AttributeLabeler.class, args = { AttributeId.class })
	public void testConstructor() {
		// nothing to test
	}

	@Test
	@UnitTestMethod(target = AttributeLabeler.class, name = "getLabelerSensitivities", args = {})
	public void testGetLabelerSensitivities() {
		/*
		 * Get the labeler sensitivities and show that they are consistent with their
		 * documented behaviors.
		 */

		AttributeLabeler attributeLabeler = new FunctionalAttributeLabeler(TestAttributeId.BOOLEAN_0, (c) -> null);

		Set<LabelerSensitivity<?>> labelerSensitivities = attributeLabeler.getLabelerSensitivities();

		// show that there is exactly one sensitivity
		assertEquals(1, labelerSensitivities.size());

		// show that the sensitivity is associated with
		// AttributeUpdateEvent
		LabelerSensitivity<?> labelerSensitivity = labelerSensitivities.iterator().next();
		assertEquals(AttributeUpdateEvent.class, labelerSensitivity.getEventClass());

		// show that the sensitivity will return the person id from a
		// AttributeUpdateEvent
		PersonId personId = new PersonId(56);
		AttributeUpdateEvent attributeUpdateEvent = new AttributeUpdateEvent(personId, TestAttributeId.BOOLEAN_0, false,
				true);
		Optional<PersonId> optional = labelerSensitivity.getPersonId(attributeUpdateEvent);
		assertTrue(optional.isPresent());
		assertEquals(personId, optional.get());

	}

	@Test
	@UnitTestMethod(target = AttributeLabeler.class, name = "getCurrentLabel", args = { PartitionsContext.class,
			PersonId.class })
	public void testGetCurrentLabel() {
		// build an attribute function
		Function<Object, Object> function = (c) -> {
			Boolean value = (Boolean) c;
			if (value) {
				return "A";
			}
			return "B";
		};

		AttributeLabeler attributeLabeler = new FunctionalAttributeLabeler(TestAttributeId.BOOLEAN_0, function);

		TestPluginData.Builder pluginBuilder = TestPluginData.builder();

		/*
		 * 
		 */
		pluginBuilder.addTestActorPlan("actor", new TestActorPlan(0, (c) -> {

			TestPartitionsContext testPartitionsContext = new TestPartitionsContext(c);

			PeopleDataManager peopleDataManager = c.getDataManager(PeopleDataManager.class);
			AttributesDataManager attributesDataManager = c.getDataManager(AttributesDataManager.class);
			List<PersonId> people = peopleDataManager.getPeople();
			for (PersonId personId : people) {

				// get the person's attribute value and apply the function
				// directly
				Boolean b0 = attributesDataManager.getAttributeValue(personId, TestAttributeId.BOOLEAN_0);
				Object expectedLabel = function.apply(b0);

				// get the label from the person id
				Object actualLabel = attributeLabeler.getCurrentLabel(testPartitionsContext, personId);

				// show that the two labels are equal
				assertEquals(expectedLabel, actualLabel);

			}
		}));

		// test preconditions
		pluginBuilder.addTestActorPlan("actor", new TestActorPlan(1, (c) -> {

			TestPartitionsContext testPartitionsContext = new TestPartitionsContext(c);

			// if the person does not exist
			ContractException contractException = assertThrows(ContractException.class,
					() -> attributeLabeler.getCurrentLabel(testPartitionsContext, new PersonId(100000)));
			assertEquals(PersonError.UNKNOWN_PERSON_ID, contractException.getErrorType());

			// if the person id is null
			contractException = assertThrows(ContractException.class,
					() -> attributeLabeler.getCurrentLabel(testPartitionsContext, null));
			assertEquals(PersonError.NULL_PERSON_ID, contractException.getErrorType());

		}));

		TestPluginData testPluginData = pluginBuilder.build();

		Factory factory = PartitionsTestPluginFactory.factory(10, 4676319446289433016L, testPluginData);
		TestSimulation.builder().addPlugins(factory.getPlugins()).build().execute();

	}

	@Test
	@UnitTestMethod(target = AttributeLabeler.class, name = "getPastLabel", args = { PartitionsContext.class,
			Event.class })
	public void testGetPastLabel() {
		Function<Object, Object> function = (c) -> {
			return c;
		};

		AttributeLabeler attributeLabeler = new FunctionalAttributeLabeler(TestAttributeId.INT_0, function);

		TestPluginData.Builder pluginBuilder = TestPluginData.builder();
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(8705700576614764378L);

		/*
		 * 
		 */
		pluginBuilder.addTestActorPlan("actor", new TestActorPlan(0, (c) -> {

			TestPartitionsContext testPartitionsContext = new TestPartitionsContext(c);

			PeopleDataManager peopleDataManager = c.getDataManager(PeopleDataManager.class);
			AttributesDataManager attributesDataManager = c.getDataManager(AttributesDataManager.class);
			List<PersonId> people = peopleDataManager.getPeople();

			AttributeUpdateEvent event;
			for (PersonId personId : people) {

				// get the person's attribute value and apply the function
				// directly
				int prev = attributesDataManager.getAttributeValue(personId, TestAttributeId.INT_0);
				int next = randomGenerator.nextInt(100);
				attributesDataManager.setAttributeValue(personId, TestAttributeId.INT_0, next);
				event = new AttributeUpdateEvent(personId, TestAttributeId.INT_0, prev, next);
				Object expectedLabel = function.apply(prev);

				// get the label from the person id
				Object actualLabel = attributeLabeler.getPastLabel(testPartitionsContext, event);

				// show that the two labels are equal
				assertEquals(expectedLabel, actualLabel);

			}
		}));

		TestPluginData testPluginData = pluginBuilder.build();
		Factory factory = PartitionsTestPluginFactory.factory(10, 4161680035971681080L, testPluginData);
		TestSimulation.builder().addPlugins(factory.getPlugins()).build().execute();
	}

	@Test
	@UnitTestMethod(target = AttributeLabeler.class, name = "getId", args = {})
	public void testGetId() {
		for (TestAttributeId testAttributeId : TestAttributeId.values()) {
			assertEquals(testAttributeId, new FunctionalAttributeLabeler(testAttributeId, (c) -> null).getId());
		}
	}

	@Test
	@UnitTestMethod(target = AttributeLabeler.class, name = "toString", args = {})
	public void testToString() {
		FunctionalAttributeLabeler functionalAttributeLabeler = new FunctionalAttributeLabeler(
				TestAttributeId.BOOLEAN_0, (c) -> null);
		String actualValue = functionalAttributeLabeler.toString();

		String expectedValue = "AttributeLabeler [attributeId=BOOLEAN_0]";

		assertEquals(expectedValue, actualValue);
	}

}
