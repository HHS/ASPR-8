package plugins.partitions.testsupport.attributes.support;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Set;

import org.apache.commons.math3.random.RandomGenerator;
import org.junit.jupiter.api.Test;

import nucleus.Plugin;
import nucleus.Simulation;
import nucleus.Simulation.Builder;
import nucleus.SimulationContext;
import nucleus.testsupport.testplugin.ScenarioPlanCompletionObserver;
import nucleus.testsupport.testplugin.TestActorPlan;
import nucleus.testsupport.testplugin.TestError;
import nucleus.testsupport.testplugin.TestPlugin;
import nucleus.testsupport.testplugin.TestPluginData;
import nucleus.util.ContractException;
import plugins.partitions.PartitionsPlugin;
import plugins.partitions.support.Equality;
import plugins.partitions.support.Filter;
import plugins.partitions.support.FilterSensitivity;
import plugins.partitions.support.PartitionError;
import plugins.partitions.testsupport.PartitionsActionSupport;
import plugins.partitions.testsupport.attributes.AttributesDataManager;
import plugins.partitions.testsupport.attributes.AttributesPlugin;
import plugins.partitions.testsupport.attributes.AttributesPluginData;
import plugins.partitions.testsupport.attributes.events.AttributeUpdateEvent;
import plugins.people.PeoplePlugin;
import plugins.people.PeoplePluginData;
import plugins.people.PersonDataManager;
import plugins.people.support.PersonId;
import plugins.reports.ReportsPlugin;
import plugins.reports.ReportsPluginData;
import plugins.stochastics.StochasticsDataManager;
import plugins.stochastics.StochasticsPlugin;
import plugins.stochastics.StochasticsPluginData;
import tools.annotations.UnitTest;
import tools.annotations.UnitTestConstructor;
import tools.annotations.UnitTestMethod;

@UnitTest(target = AttributeFilter.class)
public final class AT_AttributeFilter {

	@Test
	@UnitTestConstructor(args = { AttributeId.class, Equality.class, Object.class })
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
	@UnitTestMethod(name = "validate", args = { SimulationContext.class })
	public void testValidate() {
		int initialPopulation = 100;

		final Builder builder = Simulation.builder();
		// define some person attributes
		final AttributesPluginData.Builder attributesBuilder = AttributesPluginData.builder();
		for (final TestAttributeId testAttributeId : TestAttributeId.values()) {
			attributesBuilder.defineAttribute(testAttributeId, testAttributeId.getAttributeDefinition());
		}
		AttributeDefinition attributeDefinition = AttributeDefinition.builder().setDefaultValue(new Data(7)).setType(Data.class).build();
		attributesBuilder.defineAttribute(LocalAttributeId.DATA_ID, attributeDefinition);

		Plugin attributesPlugin = AttributesPlugin.getAttributesPlugin(attributesBuilder.build());

		builder.addPlugin(attributesPlugin);

		final PeoplePluginData.Builder peopleBuilder = PeoplePluginData.builder();
		for (int i = 0; i < initialPopulation; i++) {
			peopleBuilder.addPersonId(new PersonId(i));			
		}
		
		PeoplePluginData peoplePluginData = peopleBuilder.build();
		Plugin peoplePlugin = PeoplePlugin.getPeoplePlugin(peoplePluginData);
		builder.addPlugin(peoplePlugin);

		builder.addPlugin(ReportsPlugin.getReportPlugin(ReportsPluginData.builder().build()));

		builder.addPlugin(StochasticsPlugin.getStochasticsPlugin(StochasticsPluginData.builder().setSeed(7698506335486677498L).build()));

		builder.addPlugin(PartitionsPlugin.getPartitionsPlugin());

		// and add the action plugin to the engine
		TestPluginData.Builder pluginBuilder = TestPluginData.builder();

		pluginBuilder.addTestActorPlan("actor", new TestActorPlan(0, (c) -> {
			// if the filter's attribute id is null
			ContractException contractException = assertThrows(ContractException.class, () -> new AttributeFilter(null, Equality.EQUAL, false).validate(c));
			assertEquals(AttributeError.NULL_ATTRIBUTE_ID, contractException.getErrorType());

			// if the filter's equality operator is null
			contractException = assertThrows(ContractException.class, () -> new AttributeFilter(TestAttributeId.BOOLEAN_0, null, false).validate(c));
			assertEquals(PartitionError.NULL_EQUALITY_OPERATOR, contractException.getErrorType());

			// if the filter's value is null
			contractException = assertThrows(ContractException.class, () -> new AttributeFilter(TestAttributeId.BOOLEAN_0, Equality.EQUAL, null).validate(c));
			assertEquals(AttributeError.NULL_ATTRIBUTE_VALUE, contractException.getErrorType());

			// if the filter's value is incompatible with the attribute
			// definition associated with the filter's attribute id.
			contractException = assertThrows(ContractException.class, () -> new AttributeFilter(TestAttributeId.BOOLEAN_0, Equality.EQUAL, 5).validate(c));
			assertEquals(AttributeError.INCOMPATIBLE_VALUE, contractException.getErrorType());

			// if the filter's value is not a COMPARABLE when the filter's
			// equality operator is not EQUALS or NOT_EQUALS.

			contractException = assertThrows(ContractException.class, () -> new AttributeFilter(LocalAttributeId.DATA_ID, Equality.GREATER_THAN, new Data(12)).validate(c));
			assertEquals(PartitionError.NON_COMPARABLE_ATTRIBUTE, contractException.getErrorType());

		}));

		TestPluginData testPluginData = pluginBuilder.build();
		builder.addPlugin(TestPlugin.getTestPlugin(testPluginData));

		// build and execute the engine
		ScenarioPlanCompletionObserver scenarioPlanCompletionObserver = new ScenarioPlanCompletionObserver();
		builder//
				.setOutputConsumer(scenarioPlanCompletionObserver::handleOutput)//
				.build()//
				.execute();

		// show that all actions were executed
		if (!scenarioPlanCompletionObserver.allPlansExecuted()) {
			throw new ContractException(TestError.TEST_EXECUTION_FAILURE);
		}

	}

	@Test
	@UnitTestMethod(name = "evaluate", args = { SimulationContext.class, PersonId.class })
	public void testEvaluate() {
		PartitionsActionSupport.testConsumer(100, 2853953940626718331L, (c) -> {
			Filter filter = new AttributeFilter(TestAttributeId.BOOLEAN_0, Equality.EQUAL, true);
			PersonDataManager personDataManager = c.getDataManager(PersonDataManager.class);
			AttributesDataManager attributesDataManager = c.getDataManager(AttributesDataManager.class);
			StochasticsDataManager stochasticsDataManager = c.getDataManager(StochasticsDataManager.class);
			RandomGenerator randomGenerator = stochasticsDataManager.getRandomGenerator();

			for (PersonId personId : personDataManager.getPeople()) {
				boolean value = randomGenerator.nextBoolean();
				attributesDataManager.setAttributeValue(personId, TestAttributeId.BOOLEAN_0, value);
				boolean expected = attributesDataManager.getAttributeValue(personId, TestAttributeId.BOOLEAN_0);
				boolean actual = filter.evaluate(c, personId);
				assertEquals(expected, actual);
			}

			
		});
		
		/* precondition: if the context is null */
		PartitionsActionSupport.testConsumer(100, 1011872226453537614L, (c) -> {
			Filter filter = new AttributeFilter(TestAttributeId.BOOLEAN_0, Equality.EQUAL, true);
			assertThrows(RuntimeException.class, () -> filter.evaluate(null, new PersonId(0)));
		});
		
		/* precondition: if the person id is null */
		PartitionsActionSupport.testConsumer(100, 6858667758520667469L, (c) -> {
			Filter filter = new AttributeFilter(TestAttributeId.BOOLEAN_0, Equality.EQUAL, true);
			assertThrows(RuntimeException.class, () -> filter.evaluate(c, null));
		});

		/* precondition: if the person id is unknown */
		PartitionsActionSupport.testConsumer(100, 9106972672436024633L, (c) -> {
			Filter filter = new AttributeFilter(TestAttributeId.BOOLEAN_0, Equality.EQUAL, true);
			assertThrows(RuntimeException.class, () -> filter.evaluate(c, new PersonId(123412342)));
		});

	}

	@Test
	@UnitTestMethod(name = "getFilterSensitivities", args = {})
	public void testGetFilterSensitivities() {

		PartitionsActionSupport.testConsumer(100, 3455263917994200075L, (c) -> {

			// create an attribute filter
			Filter filter = new AttributeFilter(TestAttributeId.BOOLEAN_0, Equality.EQUAL, false);

			/*
			 * show the filter has a single sensitivity
			 */
			Set<FilterSensitivity<?>> filterSensitivities = filter.getFilterSensitivities();
			assertNotNull(filterSensitivities);
			assertEquals(filterSensitivities.size(), 1);

			/*
			 * show that this sensitivity is associated with
			 * AttributeUpdateEvent events.
			 */
			FilterSensitivity<?> filterSensitivity = filterSensitivities.iterator().next();
			assertEquals(AttributeUpdateEvent.class, filterSensitivity.getEventClass());

			/*
			 * Show that the sensitivity requires refresh for
			 * AttributeUpdateEvent events if and only if the
			 * attribute ids are equal and the event has different previous and
			 * current values.
			 */
			PersonId personId = new PersonId(0);

			AttributeUpdateEvent attributeUpdateEvent = new AttributeUpdateEvent(personId, TestAttributeId.BOOLEAN_0, false, true);

			assertTrue(filterSensitivity.requiresRefresh(c, attributeUpdateEvent).isPresent());

			attributeUpdateEvent = new AttributeUpdateEvent(personId, TestAttributeId.BOOLEAN_0, false, false);

			assertFalse(filterSensitivity.requiresRefresh(c, attributeUpdateEvent).isPresent());

			attributeUpdateEvent = new AttributeUpdateEvent(personId, TestAttributeId.BOOLEAN_1, false, true);

			assertFalse(filterSensitivity.requiresRefresh(c, attributeUpdateEvent).isPresent());

		});

	}

}
