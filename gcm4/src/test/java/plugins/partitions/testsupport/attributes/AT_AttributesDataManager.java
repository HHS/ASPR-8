package plugins.partitions.testsupport.attributes;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.EnumSet;
import java.util.LinkedHashSet;
import java.util.Set;

import org.apache.commons.math3.random.RandomGenerator;
import org.junit.jupiter.api.Test;

import nucleus.DataManagerContext;
import nucleus.EventFilter;
import nucleus.testsupport.testplugin.TestActorPlan;
import nucleus.testsupport.testplugin.TestPluginData;
import nucleus.testsupport.testplugin.TestSimulation;
import plugins.partitions.testsupport.PartitionsTestPluginFactory;
import plugins.partitions.testsupport.attributes.events.AttributeUpdateEvent;
import plugins.partitions.testsupport.attributes.support.AttributeDefinition;
import plugins.partitions.testsupport.attributes.support.AttributeError;
import plugins.partitions.testsupport.attributes.support.AttributeId;
import plugins.partitions.testsupport.attributes.support.TestAttributeId;
import plugins.people.datamanagers.PeopleDataManager;
import plugins.people.support.PersonError;
import plugins.people.support.PersonId;
import plugins.stochastics.StochasticsDataManager;
import util.annotations.UnitTestConstructor;
import util.annotations.UnitTestMethod;
import util.errors.ContractException;
import util.wrappers.MultiKey;

public class AT_AttributesDataManager {

	@Test
	@UnitTestMethod(target = AttributesDataManager.class,name = "init", args = { DataManagerContext.class })
	public void testAttributesDataViewInitialization() {
		TestSimulation.executeSimulation(PartitionsTestPluginFactory.factory(0, 5241628071704306523L, (c) -> {
			AttributesDataManager attributesDataManager = c.getDataManager(AttributesDataManager.class);

			assertEquals(EnumSet.allOf(TestAttributeId.class), attributesDataManager.getAttributeIds());

			for (TestAttributeId testAttributeId : TestAttributeId.values()) {
				assertEquals(testAttributeId.getAttributeDefinition(), attributesDataManager.getAttributeDefinition(testAttributeId));
			}
		}).getPlugins());
	}

	
	@Test
	@UnitTestMethod(target = AttributesDataManager.class,name = "setAttributeValue", args = { PersonId.class, AttributeId.class, Object.class })

	public void testSetAttributeValue() {

		TestPluginData.Builder pluginDataBuilder = TestPluginData.builder();

		// add an agent that will observe attribute changes
		Set<PersonId> peopleObserved = new LinkedHashSet<>();
		Set<PersonId> expectedPersonIds = new LinkedHashSet<>();
		for (int i = 0; i < 10; i++) {
			expectedPersonIds.add(new PersonId(i));
		}

		pluginDataBuilder.addTestActorPlan("observer", new TestActorPlan(0, (c) -> {
			AttributesDataManager attributesDataManager = c.getDataManager(AttributesDataManager.class);
			EventFilter<AttributeUpdateEvent> eventFilter = attributesDataManager.getEventFilterForAttributeUpdateEvent(TestAttributeId.BOOLEAN_0);
			c.subscribe(eventFilter, (c2, e) -> {
				peopleObserved.add(e.personId());
			});
		}));

		// add an agent that will show that the AttributesDataView is properly
		// initialized
		pluginDataBuilder.addTestActorPlan("actor", new TestActorPlan(1, (c) -> {

			PeopleDataManager peopleDataManager = c.getDataManager(PeopleDataManager.class);
			AttributesDataManager attributesDataManager = c.getDataManager(AttributesDataManager.class);

			for (PersonId personId : peopleDataManager.getPeople()) {
				Boolean value = attributesDataManager.getAttributeValue(personId, TestAttributeId.BOOLEAN_0);

				assertFalse(value);
				attributesDataManager.setAttributeValue(personId, TestAttributeId.BOOLEAN_0, true);

				value = attributesDataManager.getAttributeValue(personId, TestAttributeId.BOOLEAN_0);
				assertTrue(value);
			}

		}));

		TestPluginData testPluginData = pluginDataBuilder.build();

		TestSimulation.executeSimulation(PartitionsTestPluginFactory.factory(expectedPersonIds.size(), 4599936503626031739L, testPluginData).getPlugins());

		// show that the correct observations were made;
		assertEquals(expectedPersonIds, peopleObserved);

	}

	@Test
	@UnitTestConstructor(target = AttributesDataManager.class,args = { AttributesPluginData.class })
	public void testConstructor() {
		ContractException contractException = assertThrows(ContractException.class, () -> new AttributesDataManager(null));
		assertEquals(AttributeError.NULL_ATTRIBUTE_INITIAL_DATA, contractException.getErrorType());
	}

	@Test
	@UnitTestMethod(target = AttributesDataManager.class,name = "attributeExists", args = { AttributeId.class })
	public void testAttributeExists() {
		TestSimulation.executeSimulation(PartitionsTestPluginFactory.factory(100, 6136319242032948471L, (c) -> {
			AttributesDataManager attributesDataManager = c.getDataManager(AttributesDataManager.class);

			for (TestAttributeId testAttributeId : TestAttributeId.values()) {
				assertTrue(attributesDataManager.attributeExists(testAttributeId));
			}

			assertFalse(attributesDataManager.attributeExists(TestAttributeId.getUnknownAttributeId()));

			assertFalse(attributesDataManager.attributeExists(null));
		}).getPlugins());
	}

	@Test
	@UnitTestMethod(target = AttributesDataManager.class,name = "getAttributeDefinition", args = { AttributeId.class })
	public void testGetAttributeDefinition() {

		TestSimulation.executeSimulation(PartitionsTestPluginFactory.factory(100, 7056826773207732206L, (c) -> {

			AttributesDataManager attributesDataManager = c.getDataManager(AttributesDataManager.class);

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
		}).getPlugins());
	}

	@Test
	@UnitTestMethod(target = AttributesDataManager.class,name = "getAttributeIds", args = {})
	public void testGetAttributeIds() {
		TestSimulation.executeSimulation(PartitionsTestPluginFactory.factory(100, 3922883805893258744L, (c) -> {
			AttributesDataManager attributesDataManager = c.getDataManager(AttributesDataManager.class);
			assertEquals(EnumSet.allOf(TestAttributeId.class), attributesDataManager.getAttributeIds());
		}).getPlugins());
	}

	@Test
	@UnitTestMethod(target = AttributesDataManager.class,name = "getAttributeValue", args = { PersonId.class, AttributeId.class })
	public void testGetAttributeValue() {

		TestSimulation.executeSimulation(PartitionsTestPluginFactory.factory(100, 6311231823303553715L, (c) -> {

			StochasticsDataManager stochasticsDataManager = c.getDataManager(StochasticsDataManager.class);
			RandomGenerator randomGenerator = stochasticsDataManager.getRandomGenerator();

			AttributesDataManager attributesDataManager = c.getDataManager(AttributesDataManager.class);
			PeopleDataManager peopleDataManager = c.getDataManager(PeopleDataManager.class);

			// show that there are people in the test
			assertEquals(100, peopleDataManager.getPopulationCount());

			// set random attribute values on people
			for (PersonId personId : peopleDataManager.getPeople()) {

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

		}).getPlugins());

		TestSimulation.executeSimulation(PartitionsTestPluginFactory.factory(100, 1745090937470588460L, (c) -> {
			AttributesDataManager attributesDataManager = c.getDataManager(AttributesDataManager.class);

			PersonId goodPersonId = new PersonId(0);

			// if the attribute id is null
			ContractException contractException = assertThrows(ContractException.class, () -> attributesDataManager.getAttributeValue(goodPersonId, null));
			assertEquals(AttributeError.NULL_ATTRIBUTE_ID, contractException.getErrorType());

		}).getPlugins());

		TestSimulation.executeSimulation(PartitionsTestPluginFactory.factory(100, 6116294452862148843L, (c) -> {
			AttributesDataManager attributesDataManager = c.getDataManager(AttributesDataManager.class);

			PersonId goodPersonId = new PersonId(0);
			AttributeId badAttributeId = TestAttributeId.getUnknownAttributeId();

			// if the attribute id unknown
			ContractException contractException = assertThrows(ContractException.class, () -> attributesDataManager.getAttributeValue(goodPersonId, badAttributeId));
			assertEquals(AttributeError.UNKNOWN_ATTRIBUTE_ID, contractException.getErrorType());

		}).getPlugins());

		TestSimulation.executeSimulation(PartitionsTestPluginFactory.factory(100, 5272912205692835718L, (c) -> {
			AttributesDataManager attributesDataManager = c.getDataManager(AttributesDataManager.class);

			AttributeId goodAttributeId = TestAttributeId.BOOLEAN_0;

			// if the person id is null
			ContractException contractException = assertThrows(ContractException.class, () -> attributesDataManager.getAttributeValue(null, goodAttributeId));
			assertEquals(PersonError.NULL_PERSON_ID, contractException.getErrorType());

		}).getPlugins());

		TestSimulation.executeSimulation(PartitionsTestPluginFactory.factory(100, 4650301398849685719L, (c) -> {
			AttributesDataManager attributesDataManager = c.getDataManager(AttributesDataManager.class);
			PersonId badPersonId = new PersonId(10000000);
			AttributeId goodAttributeId = TestAttributeId.BOOLEAN_0;
			// if the person id is unknown
			ContractException contractException = assertThrows(ContractException.class, () -> attributesDataManager.getAttributeValue(badPersonId, goodAttributeId));
			assertEquals(PersonError.UNKNOWN_PERSON_ID, contractException.getErrorType());

		}).getPlugins());

	}

	@Test
	@UnitTestMethod(target = AttributesDataManager.class,name = "getEventFilterForAttributeUpdateEvent", args = { AttributeId.class })
	public void testGetEventFilterForAttributeUpdateEvent_attribute() {

		// select a proper subset of the attribute ids for filtering
		Set<TestAttributeId> selectedTestAttributeIds = new LinkedHashSet<>();
		selectedTestAttributeIds.add(TestAttributeId.BOOLEAN_0);
		selectedTestAttributeIds.add(TestAttributeId.DOUBLE_1);
		selectedTestAttributeIds.add(TestAttributeId.INT_1);

		TestPluginData.Builder pluginDataBuilder = TestPluginData.builder();

		// add an agent that will observe attribute changes
		Set<MultiKey> actualObservations = new LinkedHashSet<>();
		Set<MultiKey> expectedObservations = new LinkedHashSet<>();

		pluginDataBuilder.addTestActorPlan("observer", new TestActorPlan(0, (c) -> {
			AttributesDataManager attributesDataManager = c.getDataManager(AttributesDataManager.class);

			for (TestAttributeId testAttributeId : selectedTestAttributeIds) {
				EventFilter<AttributeUpdateEvent> eventFilter = attributesDataManager.getEventFilterForAttributeUpdateEvent(testAttributeId);
				c.subscribe(eventFilter, (c2, e) -> {
					actualObservations.add(new MultiKey(c.getTime(), e.attributeId(), e.personId()));
				});
			}
		}));

		// add an actor that will alter the people attributes over time
		for (int i = 1; i < 5; i++) {
			pluginDataBuilder.addTestActorPlan("actor", new TestActorPlan(i, (c) -> {

				PeopleDataManager peopleDataManager = c.getDataManager(PeopleDataManager.class);
				AttributesDataManager attributesDataManager = c.getDataManager(AttributesDataManager.class);
				StochasticsDataManager stochasticsDataManager = c.getDataManager(StochasticsDataManager.class);
				RandomGenerator randomGenerator = stochasticsDataManager.getRandomGenerator();

				for (PersonId personId : peopleDataManager.getPeople()) {
					for (TestAttributeId testAttributeId : TestAttributeId.values()) {
						if (randomGenerator.nextBoolean()) {
							Object propertyValue = testAttributeId.getRandomPropertyValue(randomGenerator);
							attributesDataManager.setAttributeValue(personId, testAttributeId, propertyValue);
							if (selectedTestAttributeIds.contains(testAttributeId)) {
								expectedObservations.add(new MultiKey(c.getTime(), testAttributeId, personId));
							}
						}
					}
				}
			}));
		}

		TestPluginData testPluginData = pluginDataBuilder.build();

		TestSimulation.executeSimulation(PartitionsTestPluginFactory.factory(10, 6745924247452865860L, testPluginData).getPlugins());

		// show that the correct observations were made;
		assertEquals(expectedObservations, actualObservations);

		// precondition test: if the attribute id is null
		TestSimulation.executeSimulation(PartitionsTestPluginFactory.factory(10, 947664382516123630L, (c) -> {
			AttributesDataManager attributesDataManager = c.getDataManager(AttributesDataManager.class);
			ContractException contractException = assertThrows(ContractException.class, () -> attributesDataManager.getEventFilterForAttributeUpdateEvent(null));
			assertEquals(AttributeError.NULL_ATTRIBUTE_ID, contractException.getErrorType());
		}).getPlugins());

		// precondition test: if the attribute id is not known
		TestSimulation.executeSimulation(PartitionsTestPluginFactory.factory(10, 1819497399235641135L, (c) -> {
			AttributesDataManager attributesDataManager = c.getDataManager(AttributesDataManager.class);
			ContractException contractException = assertThrows(ContractException.class, () -> attributesDataManager.getEventFilterForAttributeUpdateEvent(TestAttributeId.getUnknownAttributeId()));
			assertEquals(AttributeError.UNKNOWN_ATTRIBUTE_ID, contractException.getErrorType());
		}).getPlugins());

	}

	@Test
	@UnitTestMethod(target = AttributesDataManager.class,name = "getEventFilterForAttributeUpdateEvent", args = {})
	public void testGetEventFilterForAttributeUpdateEvent() {

		TestPluginData.Builder pluginDataBuilder = TestPluginData.builder();

		// add an agent that will observe all attribute changes
		Set<MultiKey> actualObservations = new LinkedHashSet<>();
		Set<MultiKey> expectedObservations = new LinkedHashSet<>();

		pluginDataBuilder.addTestActorPlan("observer", new TestActorPlan(0, (c) -> {
			AttributesDataManager attributesDataManager = c.getDataManager(AttributesDataManager.class);

			EventFilter<AttributeUpdateEvent> eventFilter = attributesDataManager.getEventFilterForAttributeUpdateEvent();
			c.subscribe(eventFilter, (c2, e) -> {
				actualObservations.add(new MultiKey(c.getTime(), e.attributeId(), e.personId()));
			});

		}));

		// add an actor that will alter the people attributes over time
		for (int i = 1; i < 5; i++) {
			pluginDataBuilder.addTestActorPlan("actor", new TestActorPlan(i, (c) -> {

				PeopleDataManager peopleDataManager = c.getDataManager(PeopleDataManager.class);
				AttributesDataManager attributesDataManager = c.getDataManager(AttributesDataManager.class);
				StochasticsDataManager stochasticsDataManager = c.getDataManager(StochasticsDataManager.class);
				RandomGenerator randomGenerator = stochasticsDataManager.getRandomGenerator();

				for (PersonId personId : peopleDataManager.getPeople()) {
					for (TestAttributeId testAttributeId : TestAttributeId.values()) {
						if (randomGenerator.nextBoolean()) {
							Object propertyValue = testAttributeId.getRandomPropertyValue(randomGenerator);
							attributesDataManager.setAttributeValue(personId, testAttributeId, propertyValue);
							expectedObservations.add(new MultiKey(c.getTime(), testAttributeId, personId));
						}
					}
				}
			}));
		}

		TestPluginData testPluginData = pluginDataBuilder.build();

		TestSimulation.executeSimulation(PartitionsTestPluginFactory.factory(10, 6920537014398191296L, testPluginData).getPlugins());

		// show that the correct observations were made;
		assertEquals(expectedObservations, actualObservations);

	}

}
