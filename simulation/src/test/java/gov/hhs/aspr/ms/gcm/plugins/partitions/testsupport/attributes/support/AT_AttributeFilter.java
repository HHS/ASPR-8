package gov.hhs.aspr.ms.gcm.plugins.partitions.testsupport.attributes.support;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.math3.random.RandomGenerator;
import org.junit.jupiter.api.Test;

import gov.hhs.aspr.ms.gcm.nucleus.Plugin;
import gov.hhs.aspr.ms.gcm.nucleus.testsupport.testplugin.TestActorPlan;
import gov.hhs.aspr.ms.gcm.nucleus.testsupport.testplugin.TestPlugin;
import gov.hhs.aspr.ms.gcm.nucleus.testsupport.testplugin.TestPluginData;
import gov.hhs.aspr.ms.gcm.nucleus.testsupport.testplugin.TestSimulation;
import gov.hhs.aspr.ms.gcm.plugins.partitions.PartitionsPlugin;
import gov.hhs.aspr.ms.gcm.plugins.partitions.datamanagers.PartitionsPluginData;
import gov.hhs.aspr.ms.gcm.plugins.partitions.support.Equality;
import gov.hhs.aspr.ms.gcm.plugins.partitions.support.FilterSensitivity;
import gov.hhs.aspr.ms.gcm.plugins.partitions.support.PartitionError;
import gov.hhs.aspr.ms.gcm.plugins.partitions.support.PartitionsContext;
import gov.hhs.aspr.ms.gcm.plugins.partitions.support.filters.Filter;
import gov.hhs.aspr.ms.gcm.plugins.partitions.testsupport.PartitionsTestPluginFactory;
import gov.hhs.aspr.ms.gcm.plugins.partitions.testsupport.PartitionsTestPluginFactory.Factory;
import gov.hhs.aspr.ms.gcm.plugins.partitions.testsupport.TestPartitionsContext;
import gov.hhs.aspr.ms.gcm.plugins.partitions.testsupport.attributes.AttributesDataManager;
import gov.hhs.aspr.ms.gcm.plugins.partitions.testsupport.attributes.AttributesPlugin;
import gov.hhs.aspr.ms.gcm.plugins.partitions.testsupport.attributes.AttributesPluginData;
import gov.hhs.aspr.ms.gcm.plugins.partitions.testsupport.attributes.events.AttributeUpdateEvent;
import gov.hhs.aspr.ms.gcm.plugins.people.PeoplePlugin;
import gov.hhs.aspr.ms.gcm.plugins.people.datamanagers.PeopleDataManager;
import gov.hhs.aspr.ms.gcm.plugins.people.datamanagers.PeoplePluginData;
import gov.hhs.aspr.ms.gcm.plugins.people.support.PersonId;
import gov.hhs.aspr.ms.gcm.plugins.people.support.PersonRange;
import gov.hhs.aspr.ms.gcm.plugins.stochastics.StochasticsPlugin;
import gov.hhs.aspr.ms.gcm.plugins.stochastics.datamanagers.StochasticsDataManager;
import gov.hhs.aspr.ms.gcm.plugins.stochastics.datamanagers.StochasticsPluginData;
import gov.hhs.aspr.ms.gcm.plugins.stochastics.support.WellState;
import gov.hhs.aspr.ms.util.annotations.UnitTestConstructor;
import gov.hhs.aspr.ms.util.annotations.UnitTestMethod;
import gov.hhs.aspr.ms.util.errors.ContractException;
import gov.hhs.aspr.ms.util.random.RandomGeneratorProvider;
import gov.hhs.aspr.ms.util.wrappers.MultiKey;

public final class AT_AttributeFilter {

	@Test
	@UnitTestConstructor(target = AttributeFilter.class, args = { AttributeId.class, Equality.class, Object.class })
	public void testConstructor() {
		// nothing to test
	}

	private static enum LocalAttributeId implements AttributeId {
		DATA_ID
	}

	private static class Data {
		private final int value;

		public Data(int value) {
			this.value = value;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + value;
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			}
			if (!(obj instanceof Data)) {
				return false;
			}
			Data other = (Data) obj;
			if (value != other.value) {
				return false;
			}
			return true;
		}
	}

	@Test
	@UnitTestMethod(target = AttributeFilter.class, name = "validate", args = { PartitionsContext.class })
	public void testValidate() {
		int initialPopulation = 100;

		List<Plugin> plugins = new ArrayList<>();
		// define some person attributes
		final AttributesPluginData.Builder attributesBuilder = AttributesPluginData.builder();
		for (final TestAttributeId testAttributeId : TestAttributeId.values()) {
			attributesBuilder.defineAttribute(testAttributeId, testAttributeId.getAttributeDefinition());
		}
		AttributeDefinition attributeDefinition = AttributeDefinition.builder().setDefaultValue(new Data(7))
				.setType(Data.class).build();
		attributesBuilder.defineAttribute(LocalAttributeId.DATA_ID, attributeDefinition);

		Plugin attributesPlugin = AttributesPlugin.getAttributesPlugin(attributesBuilder.build());

		Plugin partitionsPlugin = PartitionsPlugin.builder()//
				.setPartitionsPluginData(PartitionsPluginData.builder().build())//
				// .addPluginDependency(AttributesPluginId.PLUGIN_ID)//
				.getPartitionsPlugin();

		plugins.add(attributesPlugin);

		final PeoplePluginData.Builder peopleBuilder = PeoplePluginData.builder();
		peopleBuilder.addPersonRange(new PersonRange(0, initialPopulation - 1));

		PeoplePluginData peoplePluginData = peopleBuilder.build();
		Plugin peoplePlugin = PeoplePlugin.getPeoplePlugin(peoplePluginData);
		plugins.add(peoplePlugin);

		WellState wellState = WellState.builder().setSeed(7698506335486677498L).build();
		plugins.add(StochasticsPlugin
				.getStochasticsPlugin(StochasticsPluginData.builder().setMainRNGState(wellState).build()));

		plugins.add(partitionsPlugin);

		// and add the action plugin to the engine
		TestPluginData.Builder pluginBuilder = TestPluginData.builder();

		pluginBuilder.addTestActorPlan("actor", new TestActorPlan(0, (c) -> {

			TestPartitionsContext testPartitionsContext = new TestPartitionsContext(c);
			// if the filter's attribute id is null
			ContractException contractException = assertThrows(ContractException.class,
					() -> new AttributeFilter(null, Equality.EQUAL, false).validate(testPartitionsContext));
			assertEquals(AttributeError.NULL_ATTRIBUTE_ID, contractException.getErrorType());

			// if the filter's equality operator is null
			contractException = assertThrows(ContractException.class,
					() -> new AttributeFilter(TestAttributeId.BOOLEAN_0, null, false).validate(testPartitionsContext));
			assertEquals(PartitionError.NULL_EQUALITY_OPERATOR, contractException.getErrorType());

			// if the filter's value is null
			contractException = assertThrows(ContractException.class,
					() -> new AttributeFilter(TestAttributeId.BOOLEAN_0, Equality.EQUAL, null)
							.validate(testPartitionsContext));
			assertEquals(AttributeError.NULL_ATTRIBUTE_VALUE, contractException.getErrorType());

			// if the filter's value is incompatible with the attribute
			// definition associated with the filter's attribute id.
			contractException = assertThrows(ContractException.class,
					() -> new AttributeFilter(TestAttributeId.BOOLEAN_0, Equality.EQUAL, 5)
							.validate(testPartitionsContext));
			assertEquals(AttributeError.INCOMPATIBLE_VALUE, contractException.getErrorType());

			// if the filter's value is not a COMPARABLE when the filter's
			// equality operator is not EQUALS or NOT_EQUALS.

			contractException = assertThrows(ContractException.class,
					() -> new AttributeFilter(LocalAttributeId.DATA_ID, Equality.GREATER_THAN, new Data(12))
							.validate(testPartitionsContext));
			assertEquals(PartitionError.NON_COMPARABLE_ATTRIBUTE, contractException.getErrorType());

		}));

		TestPluginData testPluginData = pluginBuilder.build();
		plugins.add(TestPlugin.getTestPlugin(testPluginData));

		TestSimulation.builder().addPlugins(plugins).build().execute();

	}

	@Test
	@UnitTestMethod(target = AttributeFilter.class, name = "evaluate", args = { PartitionsContext.class,
			PersonId.class })
	public void testEvaluate() {
		Factory factory = PartitionsTestPluginFactory.factory(100, 2853953940626718331L, (c) -> {
			TestPartitionsContext testPartitionsContext = new TestPartitionsContext(c);

			Filter filter = new AttributeFilter(TestAttributeId.BOOLEAN_0, Equality.EQUAL, true);
			PeopleDataManager peopleDataManager = c.getDataManager(PeopleDataManager.class);
			AttributesDataManager attributesDataManager = c.getDataManager(AttributesDataManager.class);
			StochasticsDataManager stochasticsDataManager = c.getDataManager(StochasticsDataManager.class);
			RandomGenerator randomGenerator = stochasticsDataManager.getRandomGenerator();

			for (PersonId personId : peopleDataManager.getPeople()) {
				boolean value = randomGenerator.nextBoolean();
				attributesDataManager.setAttributeValue(personId, TestAttributeId.BOOLEAN_0, value);
				boolean expected = attributesDataManager.getAttributeValue(personId, TestAttributeId.BOOLEAN_0);
				boolean actual = filter.evaluate(testPartitionsContext, personId);
				assertEquals(expected, actual);
			}

		});
		TestSimulation.builder().addPlugins(factory.getPlugins()).build().execute();

		/* precondition: if the context is null */
		assertThrows(RuntimeException.class, () -> {
			Factory factory2 = PartitionsTestPluginFactory.factory(100, 1011872226453537614L, (c) -> {
				Filter filter = new AttributeFilter(TestAttributeId.BOOLEAN_0, Equality.EQUAL, true);
				filter.evaluate(null, new PersonId(0));
			});
			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();
		});

		/* precondition: if the person id is null */
		assertThrows(RuntimeException.class, () -> {

			Factory factory2 = PartitionsTestPluginFactory.factory(100, 6858667758520667469L, (c) -> {
				TestPartitionsContext testPartitionsContext = new TestPartitionsContext(c);
				Filter filter = new AttributeFilter(TestAttributeId.BOOLEAN_0, Equality.EQUAL, true);
				filter.evaluate(testPartitionsContext, null);
			});
			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();
		});

		/* precondition: if the person id is unknown */
		assertThrows(RuntimeException.class, () -> {
			Factory factory2 = PartitionsTestPluginFactory.factory(100, 9106972672436024633L, (c) -> {
				TestPartitionsContext testPartitionsContext = new TestPartitionsContext(c);
				Filter filter = new AttributeFilter(TestAttributeId.BOOLEAN_0, Equality.EQUAL, true);
				filter.evaluate(testPartitionsContext, new PersonId(123412342));
			});
			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();
		});

	}

	@Test
	@UnitTestMethod(target = AttributeFilter.class, name = "getFilterSensitivities", args = {})
	public void testGetFilterSensitivities() {

		Factory factory = PartitionsTestPluginFactory.factory(100, 3455263917994200075L, (c) -> {
			TestPartitionsContext testPartitionsContext = new TestPartitionsContext(c);
			// create an attribute filter
			Filter filter = new AttributeFilter(TestAttributeId.BOOLEAN_0, Equality.EQUAL, false);

			/*
			 * show the filter has a single sensitivity
			 */
			Set<FilterSensitivity<?>> filterSensitivities = filter.getFilterSensitivities();
			assertNotNull(filterSensitivities);
			assertEquals(filterSensitivities.size(), 1);

			/*
			 * show that this sensitivity is associated with AttributeUpdateEvent events.
			 */
			FilterSensitivity<?> filterSensitivity = filterSensitivities.iterator().next();
			assertEquals(AttributeUpdateEvent.class, filterSensitivity.getEventClass());

			/*
			 * Show that the sensitivity requires refresh for AttributeUpdateEvent events if
			 * and only if the attribute ids are equal and the event has different previous
			 * and current values.
			 */
			PersonId personId = new PersonId(0);

			AttributeUpdateEvent attributeUpdateEvent = new AttributeUpdateEvent(personId, TestAttributeId.BOOLEAN_0,
					false, true);

			assertTrue(filterSensitivity.requiresRefresh(testPartitionsContext, attributeUpdateEvent).isPresent());

			attributeUpdateEvent = new AttributeUpdateEvent(personId, TestAttributeId.BOOLEAN_0, false, false);

			assertFalse(filterSensitivity.requiresRefresh(testPartitionsContext, attributeUpdateEvent).isPresent());

			attributeUpdateEvent = new AttributeUpdateEvent(personId, TestAttributeId.BOOLEAN_1, false, true);

			assertFalse(filterSensitivity.requiresRefresh(testPartitionsContext, attributeUpdateEvent).isPresent());

		});
		TestSimulation.builder().addPlugins(factory.getPlugins()).build().execute();
	}

	@Test
	@UnitTestMethod(target = AttributeFilter.class, name = "getValue", args = {})
	public void testGetValue() {
		for (int i = 0; i < 10; i++) {
			AttributeFilter attributeFilter = new AttributeFilter(TestAttributeId.INT_0, Equality.EQUAL, i);
			assertEquals(i, attributeFilter.getValue());
		}
	}

	@Test
	@UnitTestMethod(target = AttributeFilter.class, name = "getEquality", args = {})
	public void testGetEquality() {
		for (Equality equality : Equality.values()) {
			AttributeFilter attributeFilter = new AttributeFilter(TestAttributeId.INT_0, equality, 25);
			assertEquals(equality, attributeFilter.getEquality());
		}
	}

	@Test
	@UnitTestMethod(target = AttributeFilter.class, name = "getAttributeId", args = {})
	public void testGetAttributeId() {
		for (TestAttributeId testAttributeId : TestAttributeId.values()) {
			AttributeFilter attributeFilter = new AttributeFilter(testAttributeId, Equality.EQUAL, 25);
			assertEquals(testAttributeId, attributeFilter.getAttributeId());
		}
	}

	private AttributeFilter getRandomAttributeFilter(long seed) {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(seed);
		TestAttributeId testAttributeId = TestAttributeId.getRandomAttributeId(randomGenerator);
		Object propertyValue = testAttributeId.getRandomPropertyValue(randomGenerator);
		Equality randomEquality = Equality.getRandomEquality(randomGenerator);
		return new AttributeFilter(testAttributeId, randomEquality, propertyValue);
	}

	@Test
	@UnitTestMethod(target = AttributeFilter.class, name = "equals", args = { Object.class })
	public void testEquals() {

		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(6412971079328158580L);

		// never equal to null
		for (int i = 0; i < 30; i++) {
			AttributeFilter attributeFilter = getRandomAttributeFilter(randomGenerator.nextLong());
			assertFalse(attributeFilter.equals(null));
		}

		// reflexive
		for (int i = 0; i < 30; i++) {
			AttributeFilter attributeFilter = getRandomAttributeFilter(randomGenerator.nextLong());
			assertTrue(attributeFilter.equals(attributeFilter));
		}

		// symmetric, transitive, consistent
		for (int i = 0; i < 30; i++) {
			long seed = randomGenerator.nextLong();
			AttributeFilter attributeFilter1 = getRandomAttributeFilter(seed);
			AttributeFilter attributeFilter2 = getRandomAttributeFilter(seed);
			for (int j = 0; j < 5; j++) {
				assertTrue(attributeFilter1.equals(attributeFilter2));
				assertTrue(attributeFilter2.equals(attributeFilter1));
			}
		}

		// different inputs yield non-equal objects
		Set<AttributeFilter> attributeFilters = new LinkedHashSet<>();
		Set<MultiKey> multiKeys = new LinkedHashSet<>();
		for (int i = 0; i < 100; i++) {
			AttributeFilter attributeFilter = getRandomAttributeFilter(randomGenerator.nextLong());
			attributeFilters.add(attributeFilter);
			MultiKey multiKey = new MultiKey(attributeFilter.getAttributeId(), attributeFilter.getEquality(),
					attributeFilter.getValue());
			multiKeys.add(multiKey);
		}

		assertEquals(multiKeys.size(), attributeFilters.size());

	}

	@Test
	@UnitTestMethod(target = AttributeFilter.class, name = "hashCode", args = {})
	public void testHashCode() {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(5562725555946491304L);

		// equal objects have equal hash codes
		for (int i = 0; i < 30; i++) {
			long seed = randomGenerator.nextLong();
			AttributeFilter attributeFilter1 = getRandomAttributeFilter(seed);
			AttributeFilter attributeFilter2 = getRandomAttributeFilter(seed);
			assertEquals(attributeFilter1, attributeFilter2);
			assertEquals(attributeFilter1.hashCode(), attributeFilter2.hashCode());
		}

		// hash codes are reasonably distributed
		Set<AttributeFilter> attributeFilters = new LinkedHashSet<>();
		for (int i = 0; i < 1000; i++) {
			AttributeFilter attributeFilter = getRandomAttributeFilter(randomGenerator.nextLong());
			attributeFilters.add(attributeFilter);
		}

		// There will be a fairly high collision rate since 1/3 of the attribute
		// properties are Booleans
		assertTrue(attributeFilters.size() > 675);

	}

	@Test
	@UnitTestMethod(target = AttributeFilter.class, name = "toString", args = {})
	public void testToString() {
		AttributeFilter attributeFilter = new AttributeFilter(TestAttributeId.INT_0, Equality.EQUAL, 25);
		String actualValue = attributeFilter.toString();		
		String expectedValue = "AttributeFilter [attributeId=INT_0, value=25, equality=EQUAL, attributesDataManager=null]";
		assertEquals(expectedValue, actualValue);
	}

}
