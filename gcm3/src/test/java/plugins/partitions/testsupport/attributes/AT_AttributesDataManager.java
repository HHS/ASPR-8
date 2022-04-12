package plugins.partitions.testsupport.attributes;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.EnumSet;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;

import org.apache.commons.math3.random.RandomGenerator;
import org.junit.jupiter.api.Test;

import nucleus.DataManagerContext;
import nucleus.EventLabeler;
import nucleus.NucleusError;
import nucleus.Plugin;
import nucleus.testsupport.testplugin.TestActorPlan;
import nucleus.testsupport.testplugin.TestPlugin;
import nucleus.testsupport.testplugin.TestPluginData;
import nucleus.util.ContractException;
import plugins.partitions.testsupport.PartitionsActionSupport;
import plugins.partitions.testsupport.attributes.events.AttributeChangeObservationEvent;
import plugins.partitions.testsupport.attributes.support.AttributeDefinition;
import plugins.partitions.testsupport.attributes.support.AttributeError;
import plugins.partitions.testsupport.attributes.support.AttributeId;
import plugins.partitions.testsupport.attributes.support.TestAttributeId;
import plugins.people.PersonDataManager;
import plugins.people.support.PersonError;
import plugins.people.support.PersonId;
import plugins.stochastics.StochasticsDataManager;
import tools.annotations.UnitTest;
import tools.annotations.UnitTestConstructor;
import tools.annotations.UnitTestMethod;

@UnitTest(target = AttributesDataManager.class)
public class AT_AttributesDataManager {

	@Test
	@UnitTestMethod(name = "init", args = { DataManagerContext.class })
	public void testAttributesDataViewInitialization() {
		PartitionsActionSupport.testConsumer(0, 5241628071704306523L, (c) -> {
			Optional<AttributesDataManager> optional = c.getDataManager(AttributesDataManager.class);
			assertTrue(optional.isPresent());

			AttributesDataManager attributesDataManager = optional.get();
			assertEquals(EnumSet.allOf(TestAttributeId.class), attributesDataManager.getAttributeIds());

			for (TestAttributeId testAttributeId : TestAttributeId.values()) {
				assertEquals(testAttributeId.getAttributeDefinition(), attributesDataManager.getAttributeDefinition(testAttributeId));
			}
		});
	}

	@Test
	@UnitTestMethod(name = "init", args = { DataManagerContext.class })
	public void testAttributeChangeObservationEventLabelers() {
		PartitionsActionSupport.testConsumer(0, 5241628071704306523L, (c) -> {
			EventLabeler<AttributeChangeObservationEvent> eventLabeler = AttributeChangeObservationEvent.getEventLabeler();
			assertNotNull(eventLabeler);
			ContractException contractException = assertThrows(ContractException.class, () -> c.addEventLabeler(eventLabeler));
			assertEquals(NucleusError.DUPLICATE_LABELER_ID_IN_EVENT_LABELER, contractException.getErrorType());
		});
	}

	@Test
	@UnitTestMethod(name = "setAttributeValue", args = { PersonId.class, AttributeId.class, Object.class })

	public void testSetAttributeValue() {

		TestPluginData.Builder pluginDataBuilder = TestPluginData.builder();

		// add an agent that will observe attribute changes
		Set<PersonId> peopleObserved = new LinkedHashSet<>();
		Set<PersonId> expectedPersonIds = new LinkedHashSet<>();
		for (int i = 0; i < 10; i++) {
			expectedPersonIds.add(new PersonId(i));
		}

		pluginDataBuilder.addTestActorPlan("observer", new TestActorPlan(0, (c) -> {
			c.subscribe(AttributeChangeObservationEvent.getEventLabel(c, TestAttributeId.BOOLEAN_0), (c2, e) -> {
				peopleObserved.add(e.getPersonId());
			});
		}));

		// add an agent that will show that the AttributesDataView is properly
		// initialized
		pluginDataBuilder.addTestActorPlan("agent", new TestActorPlan(1, (c) -> {

			PersonDataManager personDataManager = c.getDataManager(PersonDataManager.class).get();
			AttributesDataManager attributesDataManager = c.getDataManager(AttributesDataManager.class).get();

			for (PersonId personId : personDataManager.getPeople()) {
				Boolean value = attributesDataManager.getAttributeValue(personId, TestAttributeId.BOOLEAN_0);

				assertFalse(value);
				attributesDataManager.setAttributeValue(personId, TestAttributeId.BOOLEAN_0, true);

				value = attributesDataManager.getAttributeValue(personId, TestAttributeId.BOOLEAN_0);
				assertTrue(value);
			}

		}));

		TestPluginData testPluginData = pluginDataBuilder.build();
		Plugin testPlugin = TestPlugin.getTestPlugin(testPluginData);

		PartitionsActionSupport.testConsumers(expectedPersonIds.size(), 5241628071704306523L, testPlugin);

		// show that the correct observations were made;
		assertEquals(expectedPersonIds, peopleObserved);

	}

	@Test
	@UnitTestConstructor(args = { AttributesPluginData.class })
	public void testConstructor() {
		ContractException contractException = assertThrows(ContractException.class, () -> new AttributesDataManager(null));
		assertEquals(AttributeError.NULL_ATTRIBUTE_INITIAL_DATA, contractException.getErrorType());
	}

	@Test
	@UnitTestMethod(name = "attributeExists", args = { AttributeId.class })
	public void testAttributeExists() {
		PartitionsActionSupport.testConsumer(100, 6136319242032948471L, (c) -> {
			AttributesDataManager attributesDataManager = c.getDataManager(AttributesDataManager.class).get();

			for (TestAttributeId testAttributeId : TestAttributeId.values()) {
				assertTrue(attributesDataManager.attributeExists(testAttributeId));
			}

			assertFalse(attributesDataManager.attributeExists(TestAttributeId.getUnknownAttributeId()));

			assertFalse(attributesDataManager.attributeExists(null));
		});
	}

	@Test
	@UnitTestMethod(name = "getAttributeDefinition", args = { AttributeId.class })
	public void testGetAttributeDefinition() {

		PartitionsActionSupport.testConsumer(100, 7056826773207732206L, (c) -> {

			AttributesDataManager attributesDataManager = c.getDataManager(AttributesDataManager.class).get();

			for (TestAttributeId testAttributeId : TestAttributeId.values()) {
				AttributeDefinition expectedAttributeDefinition = testAttributeId.getAttributeDefinition();
				AttributeDefinition actualAttributeDefinition = attributesDataManager.getAttributeDefinition(testAttributeId);
				assertEquals(expectedAttributeDefinition, actualAttributeDefinition);
			}

			// precondition tests:

			// if the attribute id is null
			ContractException contractException = assertThrows(ContractException.class, () -> attributesDataManager.getAttributeDefinition(null));
			assertEquals(AttributeError.NULL_ATTRIBUTE_ID, contractException.getErrorType());

			// if the attribute id unknown
			contractException = assertThrows(ContractException.class, () -> attributesDataManager.getAttributeDefinition(TestAttributeId.getUnknownAttributeId()));
			assertEquals(AttributeError.UNKNOWN_ATTRIBUTE_ID, contractException.getErrorType());
		});
	}

	@Test
	@UnitTestMethod(name = "getAttributeIds", args = {})
	public void testGetAttributeIds() {
		PartitionsActionSupport.testConsumer(100, 3922883805893258744L, (c) -> {
			AttributesDataManager attributesDataManager = c.getDataManager(AttributesDataManager.class).get();
			assertEquals(EnumSet.allOf(TestAttributeId.class), attributesDataManager.getAttributeIds());
		});
	}

	@Test
	@UnitTestMethod(name = "getAttributeValue", args = { PersonId.class, AttributeId.class })
	public void testGetAttributeValue() {

		PartitionsActionSupport.testConsumer(100, 3296963241519285254L, (c) -> {

			StochasticsDataManager stochasticsDataManager = c.getDataManager(StochasticsDataManager.class).get();
			RandomGenerator randomGenerator = stochasticsDataManager.getRandomGenerator();

			AttributesDataManager attributesDataManager = c.getDataManager(AttributesDataManager.class).get();
			PersonDataManager personDataManager = c.getDataManager(PersonDataManager.class).get();

			// show that there are people in the test
			assertEquals(100, personDataManager.getPopulationCount());

			// set random attribute values on people
			for (PersonId personId : personDataManager.getPeople()) {

				Boolean b0_expected = randomGenerator.nextBoolean();
				attributesDataManager.setAttributeValue(personId, TestAttributeId.BOOLEAN_0, b0_expected);
				Boolean b0_actual = attributesDataManager.getAttributeValue(personId, TestAttributeId.BOOLEAN_0);
				assertEquals(b0_expected, b0_actual);

				Boolean b1_expected = randomGenerator.nextBoolean();
				attributesDataManager.setAttributeValue(personId, TestAttributeId.BOOLEAN_1, b1_expected);
				Boolean b1_actual = attributesDataManager.getAttributeValue(personId, TestAttributeId.BOOLEAN_1);
				assertEquals(b1_expected, b1_actual);

				Integer i0_expected = randomGenerator.nextInt();
				attributesDataManager.setAttributeValue(personId, TestAttributeId.INT_0, i0_expected);
				Integer i0_actual = attributesDataManager.getAttributeValue(personId, TestAttributeId.INT_0);
				assertEquals(i0_expected, i0_actual);

				Integer i1_expected = randomGenerator.nextInt();
				attributesDataManager.setAttributeValue(personId, TestAttributeId.INT_1, i1_expected);
				Integer i1_actual = attributesDataManager.getAttributeValue(personId, TestAttributeId.INT_1);
				assertEquals(i1_expected, i1_actual);

				Double d0_expected = randomGenerator.nextDouble();
				attributesDataManager.setAttributeValue(personId, TestAttributeId.DOUBLE_0, d0_expected);
				Double d0_actual = attributesDataManager.getAttributeValue(personId, TestAttributeId.DOUBLE_0);
				assertEquals(d0_expected, d0_actual);

				Double d1_expected = randomGenerator.nextDouble();
				attributesDataManager.setAttributeValue(personId, TestAttributeId.DOUBLE_1, d1_expected);
				Double d1_actual = attributesDataManager.getAttributeValue(personId, TestAttributeId.DOUBLE_1);
				assertEquals(d1_expected, d1_actual);

			}

		});

		PartitionsActionSupport.testConsumer(100, 3296963241519285254L, (c) -> {
			AttributesDataManager attributesDataManager = c.getDataManager(AttributesDataManager.class).get();

			PersonId goodPersonId = new PersonId(0);

			// if the attribute id is null
			ContractException contractException = assertThrows(ContractException.class, () -> attributesDataManager.getAttributeValue(goodPersonId, null));
			assertEquals(AttributeError.NULL_ATTRIBUTE_ID, contractException.getErrorType());

		});

		PartitionsActionSupport.testConsumer(100, 3296963241519285254L, (c) -> {
			AttributesDataManager attributesDataManager = c.getDataManager(AttributesDataManager.class).get();

			PersonId goodPersonId = new PersonId(0);
			AttributeId badAttributeId = TestAttributeId.getUnknownAttributeId();

			// if the attribute id unknown
			ContractException contractException = assertThrows(ContractException.class, () -> attributesDataManager.getAttributeValue(goodPersonId, badAttributeId));
			assertEquals(AttributeError.UNKNOWN_ATTRIBUTE_ID, contractException.getErrorType());

		});

		PartitionsActionSupport.testConsumer(100, 3296963241519285254L, (c) -> {
			AttributesDataManager attributesDataManager = c.getDataManager(AttributesDataManager.class).get();

			AttributeId goodAttributeId = TestAttributeId.BOOLEAN_0;

			// if the person id is null
			ContractException contractException = assertThrows(ContractException.class, () -> attributesDataManager.getAttributeValue(null, goodAttributeId));
			assertEquals(PersonError.NULL_PERSON_ID, contractException.getErrorType());

		});

		PartitionsActionSupport.testConsumer(100, 3296963241519285254L, (c) -> {
			AttributesDataManager attributesDataManager = c.getDataManager(AttributesDataManager.class).get();
			PersonId badPersonId = new PersonId(10000000);
			AttributeId goodAttributeId = TestAttributeId.BOOLEAN_0;
			// if the person id is unknown
			ContractException contractException = assertThrows(ContractException.class, () -> attributesDataManager.getAttributeValue(badPersonId, goodAttributeId));
			assertEquals(PersonError.UNKNOWN_PERSON_ID, contractException.getErrorType());

		});

	}

}
