package plugins.partitions.testsupport.attributes;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.math3.random.RandomGenerator;
import org.junit.jupiter.api.Test;

import nucleus.DataManagerContext;
import nucleus.EventFilter;
import nucleus.Plugin;
import nucleus.testsupport.testplugin.TestActorPlan;
import nucleus.testsupport.testplugin.TestPlugin;
import nucleus.testsupport.testplugin.TestPluginData;
import nucleus.testsupport.testplugin.TestSimulation;
import plugins.partitions.PartitionsPlugin;
import plugins.partitions.datamanagers.PartitionsPluginData;
import plugins.partitions.testsupport.PartitionsTestPluginFactory;
import plugins.partitions.testsupport.PartitionsTestPluginFactory.Factory;
import plugins.partitions.testsupport.attributes.events.AttributeUpdateEvent;
import plugins.partitions.testsupport.attributes.support.AttributeDefinition;
import plugins.partitions.testsupport.attributes.support.AttributeError;
import plugins.partitions.testsupport.attributes.support.AttributeId;
import plugins.partitions.testsupport.attributes.support.TestAttributeId;
import plugins.people.PeoplePlugin;
import plugins.people.datamanagers.PeopleDataManager;
import plugins.people.datamanagers.PeoplePluginData;
import plugins.people.support.PersonError;
import plugins.people.support.PersonId;
import plugins.people.support.PersonRange;
import plugins.stochastics.StochasticsPlugin;
import plugins.stochastics.datamanagers.StochasticsDataManager;
import plugins.stochastics.datamanagers.StochasticsPluginData;
import plugins.stochastics.support.WellState;
import util.annotations.UnitTestConstructor;
import util.annotations.UnitTestMethod;
import util.errors.ContractException;
import util.random.RandomGeneratorProvider;
import util.wrappers.MultiKey;

public class AT_AttributesDataManager {

	@Test
	@UnitTestMethod(target = AttributesDataManager.class, name = "init", args = { DataManagerContext.class })
	public void testAttributesDataViewInitialization() {
		Factory factory = PartitionsTestPluginFactory.factory(0, 5241628071704306523L, (c) -> {
			AttributesDataManager attributesDataManager = c.getDataManager(AttributesDataManager.class);

			assertEquals(EnumSet.allOf(TestAttributeId.class), attributesDataManager.getAttributeIds());

			for (TestAttributeId testAttributeId : TestAttributeId.values()) {
				assertEquals(testAttributeId.getAttributeDefinition(),
						attributesDataManager.getAttributeDefinition(testAttributeId));
			}
		});

		TestSimulation.builder().addPlugins(factory.getPlugins()).build().execute();
	}

	@Test
	@UnitTestMethod(target = AttributesDataManager.class, name = "setAttributeValue", args = { PersonId.class,
			AttributeId.class, Object.class })

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
			EventFilter<AttributeUpdateEvent> eventFilter = attributesDataManager
					.getEventFilterForAttributeUpdateEvent(TestAttributeId.BOOLEAN_0);
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

		Factory factory = PartitionsTestPluginFactory.factory(expectedPersonIds.size(), 4599936503626031739L,
				testPluginData);
		TestSimulation.builder().addPlugins(factory.getPlugins()).build().execute();

		// show that the correct observations were made;
		assertEquals(expectedPersonIds, peopleObserved);

	}

	@Test
	@UnitTestConstructor(target = AttributesDataManager.class, args = { AttributesPluginData.class })
	public void testConstructor() {
		ContractException contractException = assertThrows(ContractException.class,
				() -> new AttributesDataManager(null));
		assertEquals(AttributeError.NULL_ATTRIBUTE_INITIAL_DATA, contractException.getErrorType());
	}

	@Test
	@UnitTestMethod(target = AttributesDataManager.class, name = "attributeExists", args = { AttributeId.class })
	public void testAttributeExists() {
		Factory factory = PartitionsTestPluginFactory.factory(100, 6136319242032948471L, (c) -> {
			AttributesDataManager attributesDataManager = c.getDataManager(AttributesDataManager.class);

			for (TestAttributeId testAttributeId : TestAttributeId.values()) {
				assertTrue(attributesDataManager.attributeExists(testAttributeId));
			}

			assertFalse(attributesDataManager.attributeExists(TestAttributeId.getUnknownAttributeId()));

			assertFalse(attributesDataManager.attributeExists(null));
		});

		TestSimulation.builder().addPlugins(factory.getPlugins()).build().execute();
	}

	@Test
	@UnitTestMethod(target = AttributesDataManager.class, name = "getAttributeDefinition", args = { AttributeId.class })
	public void testGetAttributeDefinition() {

		Factory factory = PartitionsTestPluginFactory.factory(100, 7056826773207732206L, (c) -> {

			AttributesDataManager attributesDataManager = c.getDataManager(AttributesDataManager.class);

			for (TestAttributeId testAttributeId : TestAttributeId.values()) {
				AttributeDefinition expectedAttributeDefinition = testAttributeId.getAttributeDefinition();
				AttributeDefinition actualAttributeDefinition = attributesDataManager
						.getAttributeDefinition(testAttributeId);
				assertEquals(expectedAttributeDefinition, actualAttributeDefinition);
			}

			// precondition tests:

			// if the attribute id is null
			ContractException contractException = assertThrows(ContractException.class,
					() -> attributesDataManager.getAttributeDefinition(null));
			assertEquals(AttributeError.NULL_ATTRIBUTE_ID, contractException.getErrorType());

			// if the attribute id unknown
			contractException = assertThrows(ContractException.class,
					() -> attributesDataManager.getAttributeDefinition(TestAttributeId.getUnknownAttributeId()));
			assertEquals(AttributeError.UNKNOWN_ATTRIBUTE_ID, contractException.getErrorType());
		});
		TestSimulation.builder().addPlugins(factory.getPlugins()).build().execute();
	}

	@Test
	@UnitTestMethod(target = AttributesDataManager.class, name = "getAttributeIds", args = {})
	public void testGetAttributeIds() {
		Factory factory = PartitionsTestPluginFactory.factory(100, 3922883805893258744L, (c) -> {
			AttributesDataManager attributesDataManager = c.getDataManager(AttributesDataManager.class);
			assertEquals(EnumSet.allOf(TestAttributeId.class), attributesDataManager.getAttributeIds());
		});
		TestSimulation.builder().addPlugins(factory.getPlugins()).build().execute();
	}

	@Test
	@UnitTestMethod(target = AttributesDataManager.class, name = "getAttributeValue", args = { PersonId.class,
			AttributeId.class })
	public void testGetAttributeValue() {

		Factory factory = PartitionsTestPluginFactory.factory(100, 6311231823303553715L, (c) -> {

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
		});
		TestSimulation.builder().addPlugins(factory.getPlugins()).build().execute();

		// if the attribute id is null
		ContractException contractException = assertThrows(ContractException.class, () -> {
			Factory factory2 = PartitionsTestPluginFactory.factory(100, 1745090937470588460L, (c) -> {
				AttributesDataManager attributesDataManager = c.getDataManager(AttributesDataManager.class);
				PersonId goodPersonId = new PersonId(0);
				attributesDataManager.getAttributeValue(goodPersonId, null);
			});
			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();
		});
		assertEquals(AttributeError.NULL_ATTRIBUTE_ID, contractException.getErrorType());

		// if the attribute id unknown
		contractException = assertThrows(ContractException.class, () -> {
			Factory factory2 = PartitionsTestPluginFactory.factory(100, 6116294452862148843L, (c) -> {
				AttributesDataManager attributesDataManager = c.getDataManager(AttributesDataManager.class);
				PersonId goodPersonId = new PersonId(0);
				AttributeId badAttributeId = TestAttributeId.getUnknownAttributeId();
				attributesDataManager.getAttributeValue(goodPersonId, badAttributeId);
			});
			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();
		});
		assertEquals(AttributeError.UNKNOWN_ATTRIBUTE_ID, contractException.getErrorType());

		// if the person id is null
		contractException = assertThrows(ContractException.class, () -> {
			Factory factory2 = PartitionsTestPluginFactory.factory(100, 5272912205692835718L, (c) -> {
				AttributesDataManager attributesDataManager = c.getDataManager(AttributesDataManager.class);
				AttributeId goodAttributeId = TestAttributeId.BOOLEAN_0;
				attributesDataManager.getAttributeValue(null, goodAttributeId);
			});
			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();
		});
		assertEquals(PersonError.NULL_PERSON_ID, contractException.getErrorType());

		// if the person id is unknown
		contractException = assertThrows(ContractException.class, () -> {
			Factory factory2 = PartitionsTestPluginFactory.factory(100, 4650301398849685719L, (c) -> {
				AttributesDataManager attributesDataManager = c.getDataManager(AttributesDataManager.class);
				PersonId badPersonId = new PersonId(10000000);
				AttributeId goodAttributeId = TestAttributeId.BOOLEAN_0;
				attributesDataManager.getAttributeValue(badPersonId, goodAttributeId);
			});
			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();
		});
		assertEquals(PersonError.UNKNOWN_PERSON_ID, contractException.getErrorType());

	}

	@Test
	@UnitTestMethod(target = AttributesDataManager.class, name = "getEventFilterForAttributeUpdateEvent", args = {
			AttributeId.class })
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
				EventFilter<AttributeUpdateEvent> eventFilter = attributesDataManager
						.getEventFilterForAttributeUpdateEvent(testAttributeId);
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

		Factory factory = PartitionsTestPluginFactory.factory(10, 6745924247452865860L, testPluginData);
		TestSimulation.builder().addPlugins(factory.getPlugins()).build().execute();

		// show that the correct observations were made;
		assertEquals(expectedObservations, actualObservations);

		// precondition test: if the attribute id is null
		ContractException contractException = assertThrows(ContractException.class, () -> {
			Factory factory2 = PartitionsTestPluginFactory.factory(10, 947664382516123630L, (c) -> {
				AttributesDataManager attributesDataManager = c.getDataManager(AttributesDataManager.class);
				attributesDataManager.getEventFilterForAttributeUpdateEvent(null);
			});
			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();
		});
		assertEquals(AttributeError.NULL_ATTRIBUTE_ID, contractException.getErrorType());

		// precondition test: if the attribute id is not known
		contractException = assertThrows(ContractException.class, () -> {
			Factory factory2 = PartitionsTestPluginFactory.factory(10, 1819497399235641135L, (c) -> {
				AttributesDataManager attributesDataManager = c.getDataManager(AttributesDataManager.class);
				attributesDataManager.getEventFilterForAttributeUpdateEvent(TestAttributeId.getUnknownAttributeId());
			});
			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();
		});
		assertEquals(AttributeError.UNKNOWN_ATTRIBUTE_ID, contractException.getErrorType());

	}

	@Test
	@UnitTestMethod(target = AttributesDataManager.class, name = "getEventFilterForAttributeUpdateEvent", args = {})
	public void testGetEventFilterForAttributeUpdateEvent() {

		TestPluginData.Builder pluginDataBuilder = TestPluginData.builder();

		// add an agent that will observe all attribute changes
		Set<MultiKey> actualObservations = new LinkedHashSet<>();
		Set<MultiKey> expectedObservations = new LinkedHashSet<>();

		pluginDataBuilder.addTestActorPlan("observer", new TestActorPlan(0, (c) -> {
			AttributesDataManager attributesDataManager = c.getDataManager(AttributesDataManager.class);

			EventFilter<AttributeUpdateEvent> eventFilter = attributesDataManager
					.getEventFilterForAttributeUpdateEvent();
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

		Factory factory = PartitionsTestPluginFactory.factory(10, 6920537014398191296L, testPluginData);
		TestSimulation.builder().addPlugins(factory.getPlugins()).build().execute();

		// show that the correct observations were made;
		assertEquals(expectedObservations, actualObservations);

	}

	private AttributesPluginData getRandomAttributesPluginData(long seed, List<PersonId> people) {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(seed);
		AttributesPluginData.Builder builder = AttributesPluginData.builder();
		
		for (TestAttributeId testAttributeId : TestAttributeId.values()) {
			AttributeDefinition attributeDefinition = testAttributeId.getAttributeDefinition();
			builder.defineAttribute(testAttributeId, attributeDefinition);
		}

		for (PersonId personId : people) {
			for (TestAttributeId testAttributeId : TestAttributeId.values()) {
				Object propertyValue = testAttributeId.getRandomPropertyValue(randomGenerator);
				builder.setPersonAttributeValue(personId, testAttributeId, propertyValue);
			}
		}

		return builder.build();
	}

	@Test
	@UnitTestMethod(target = AttributesDataManager.class, name = "toString", args = {})
	public void testToString() {
		List<PersonId> people = new ArrayList<>();
		for (int i = 0; i < 5; i++) {
			people.add(new PersonId(i));
		}

		AttributesPluginData randomAttributesPluginData = getRandomAttributesPluginData(146602962355699453L, people);
		Plugin attributesPlugin = AttributesPlugin.getAttributesPlugin(randomAttributesPluginData);

		PeoplePluginData peoplePluginData = PeoplePluginData.builder().addPersonRange(new PersonRange(0, 4)).build();
		Plugin peoplePlugin = PeoplePlugin.getPeoplePlugin(peoplePluginData);

		PartitionsPluginData partitionsPluginData = PartitionsPluginData.builder().build();
		Plugin partitionsPlugin = PartitionsPlugin.builder().setPartitionsPluginData(partitionsPluginData)
				.getPartitionsPlugin();

		StochasticsPluginData stochasticsPluginData = StochasticsPluginData.builder()
				.setMainRNGState(WellState.builder().setSeed(6440829571736239633L).build()).build();
		Plugin stochasticsPlugin = StochasticsPlugin.getStochasticsPlugin(stochasticsPluginData);

		TestPluginData testPluginData = TestPluginData.builder().addTestActorPlan("actor", new TestActorPlan(0, (c) -> {
			AttributesDataManager attributesDataManager = c.getDataManager(AttributesDataManager.class);
			String actualValue = attributesDataManager.toString();
			
			//expected value validated via inspection
			String expectedValue = "AttributesDataManager ["
					+ "attributeDefinitions={"
					+ "INT_0=AttributeDefinition [type=class java.lang.Integer, defaultValue=0], "
					+ "INT_1=AttributeDefinition [type=class java.lang.Integer, defaultValue=1], "
					+ "DOUBLE_0=AttributeDefinition [type=class java.lang.Double, defaultValue=0.0], "
					+ "DOUBLE_1=AttributeDefinition [type=class java.lang.Double, defaultValue=1.0], "
					+ "BOOLEAN_0=AttributeDefinition [type=class java.lang.Boolean, defaultValue=false], "
					+ "BOOLEAN_1=AttributeDefinition [type=class java.lang.Boolean, defaultValue=true]}, "
					+ "attributeValues={"
					+ "INT_0={0=-1853985413, 1=267548881, 2=2046503620, 3=781984606, 4=-1339150308}, "
					+ "INT_1={0=907447393, 1=-821246733, 2=1097040035, 3=242609926, 4=1108077451}, "
					+ "DOUBLE_0={0=0.17544748424416778, 1=0.3430229866948431, 2=0.7111106859824385, 3=0.4231972579610015, 4=0.8313225649559359}, "
					+ "DOUBLE_1={0=0.40215185526664254, 1=0.9508065326364401, 2=0.4860658436547638, 3=0.732728221359974, 4=0.268176327893356}, "
					+ "BOOLEAN_0={0=false, 1=true, 2=true, 3=true, 4=false}, "
					+ "BOOLEAN_1={0=false, 1=false, 2=true, 3=false, 4=true}}]";
			
			assertEquals(expectedValue, actualValue);

		})).build();
		Plugin testPlugin = TestPlugin.getTestPlugin(testPluginData);

		TestSimulation.builder()//
				.addPlugin(attributesPlugin)//
				.addPlugin(partitionsPlugin)//
				.addPlugin(peoplePlugin)//
				.addPlugin(stochasticsPlugin)//
				.addPlugin(testPlugin)//
				.build()//
				.execute();

	}

}
