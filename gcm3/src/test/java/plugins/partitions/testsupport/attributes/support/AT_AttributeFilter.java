package plugins.partitions.testsupport.attributes.support;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Set;
import java.util.function.Consumer;

import org.apache.commons.math3.random.RandomGenerator;
import org.junit.jupiter.api.Test;

import nucleus.AgentContext;
import nucleus.Context;
import nucleus.Engine;
import nucleus.Engine.EngineBuilder;
import nucleus.testsupport.actionplugin.ActionPlugin;
import nucleus.testsupport.actionplugin.AgentActionPlan;
import plugins.components.ComponentPlugin;
import plugins.partitions.PartitionsPlugin;
import plugins.partitions.support.Equality;
import plugins.partitions.support.Filter;
import plugins.partitions.support.FilterSensitivity;
import plugins.partitions.support.PartitionError;
import plugins.partitions.testsupport.attributes.AttributesPlugin;
import plugins.partitions.testsupport.attributes.datacontainers.AttributesDataView;
import plugins.partitions.testsupport.attributes.events.mutation.AttributeValueAssignmentEvent;
import plugins.partitions.testsupport.attributes.events.observation.AttributeChangeObservationEvent;
import plugins.partitions.testsupport.attributes.initialdata.AttributeInitialData;
import plugins.people.PeoplePlugin;
import plugins.people.datacontainers.PersonDataView;
import plugins.people.initialdata.PeopleInitialData;
import plugins.people.support.PersonId;
import plugins.reports.ReportPlugin;
import plugins.reports.initialdata.ReportsInitialData;
import plugins.stochastics.StochasticsPlugin;
import plugins.stochastics.datacontainers.StochasticsDataView;
import plugins.stochastics.initialdata.StochasticsInitialData;
import util.ContractException;
import util.annotations.UnitTest;
import util.annotations.UnitTestConstructor;
import util.annotations.UnitTestMethod;

@UnitTest(target = AttributeFilter.class)
public final class AT_AttributeFilter {

	@Test
	@UnitTestConstructor(args = { AttributeId.class, Equality.class, Object.class })
	public void testConstructor() {
		// nothing to test
	}

	private void testConsumer(int initialPopulationCount, long seed, Consumer<AgentContext> consumer) {
		
		EngineBuilder engineBuilder = Engine.builder();

		AttributeInitialData.Builder attributeBuilder = AttributeInitialData.builder();
		for (TestAttributeId testAttributeId : TestAttributeId.values()) {
			attributeBuilder.defineAttribute(testAttributeId, testAttributeId.getAttributeDefinition());
		}
		attributeBuilder.defineAttribute(LocalAttributeId.DATA_ID, AttributeDefinition.builder().setType(Data.class).setDefaultValue(new Data(0)).build());

		engineBuilder.addPlugin(AttributesPlugin.PLUGIN_ID, new AttributesPlugin(attributeBuilder.build())::init);

		// add the remaining plugins
		PeopleInitialData.Builder peopleBuilder = PeopleInitialData.builder();
		for (int i = 0; i < initialPopulationCount; i++) {
			peopleBuilder.addPersonId(new PersonId(i));
		}
		engineBuilder.addPlugin(PeoplePlugin.PLUGIN_ID, new PeoplePlugin(peopleBuilder.build())::init);
		engineBuilder.addPlugin(ComponentPlugin.PLUGIN_ID, new ComponentPlugin()::init);
		engineBuilder.addPlugin(StochasticsPlugin.PLUGIN_ID, new StochasticsPlugin(StochasticsInitialData.builder().setSeed(seed).build())::init);
		engineBuilder.addPlugin(ReportPlugin.PLUGIN_ID, new ReportPlugin(ReportsInitialData.builder().build())::init);
		engineBuilder.addPlugin(PartitionsPlugin.PLUGIN_ID, new PartitionsPlugin()::init);

		ActionPlugin.Builder pluginBuilder = ActionPlugin.builder();

		// add the test agent
		pluginBuilder.addAgent("agent");

		/*
		 * Execute the consumer
		 */
		pluginBuilder.addAgentActionPlan("agent", new AgentActionPlan(0, consumer));

		ActionPlugin actionPlugin = pluginBuilder.build();
		engineBuilder.addPlugin(ActionPlugin.PLUGIN_ID, actionPlugin::init);

		// build and execute the engine
		engineBuilder.build().execute();

		// show that all actions were executed
		assertTrue(actionPlugin.allActionsExecuted());
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
	@UnitTestMethod(name = "validate", args = { Context.class })
	public void testValidate() {

		testConsumer(100, 3690002958793431202L, (c) -> {

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

		});

	}

	@Test
	@UnitTestMethod(name = "evaluate", args = { Context.class, PersonId.class })
	public void testEvaluate() {
		testConsumer(100, 2853953940626718331L, (c) -> {
			Filter filter = new AttributeFilter(TestAttributeId.BOOLEAN_0, Equality.EQUAL, true);
			PersonDataView personDataView = c.getDataView(PersonDataView.class).get();
			AttributesDataView attributesDataView = c.getDataView(AttributesDataView.class).get();
			StochasticsDataView stochasticsDataView = c.getDataView(StochasticsDataView.class).get();
			RandomGenerator randomGenerator = stochasticsDataView.getRandomGenerator();

			for (PersonId personId : personDataView.getPeople()) {
				boolean value = randomGenerator.nextBoolean();
				c.resolveEvent(new AttributeValueAssignmentEvent(personId, TestAttributeId.BOOLEAN_0, value));

				boolean expected = attributesDataView.getAttributeValue(personId, TestAttributeId.BOOLEAN_0);
				boolean actual = filter.evaluate(c, personId);
				assertEquals(expected, actual);
			}

			/* precondition: if the context is null */
			assertThrows(RuntimeException.class, () -> filter.evaluate(null, new PersonId(0)));

			/* precondition: if the person id is null */
			assertThrows(RuntimeException.class, () -> filter.evaluate(c, null));

			/* precondition: if the person id is unknown */
			assertThrows(RuntimeException.class, () -> filter.evaluate(c, new PersonId(123412342)));
		});
	}

	@Test
	@UnitTestMethod(name = "getFilterSensitivities", args = {})
	public void testGetFilterSensitivities() {

		testConsumer(100, 3455263917994200075L, (c) -> {

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
			 * AttributeChangeObservationEvent events.
			 */
			FilterSensitivity<?> filterSensitivity = filterSensitivities.iterator().next();
			assertEquals(AttributeChangeObservationEvent.class, filterSensitivity.getEventClass());

			/*
			 * Show that the sensitivity requires refresh for
			 * AttributeChangeObservationEvent events if and only if the
			 * attribute ids are equal and the event has different previous and
			 * current values.
			 */
			PersonId personId = new PersonId(0);
			
			AttributeChangeObservationEvent attributeChangeObservationEvent = new AttributeChangeObservationEvent(
					personId,
					TestAttributeId.BOOLEAN_0,
					false,true);
			
			assertTrue(filterSensitivity.requiresRefresh(c, attributeChangeObservationEvent).isPresent());

			attributeChangeObservationEvent = new AttributeChangeObservationEvent(
					personId,
					TestAttributeId.BOOLEAN_0,
					false,false);
			
			assertFalse(filterSensitivity.requiresRefresh(c, attributeChangeObservationEvent).isPresent());


			attributeChangeObservationEvent = new AttributeChangeObservationEvent(
					personId,
					TestAttributeId.BOOLEAN_1,
					false,true);
			
			assertFalse(filterSensitivity.requiresRefresh(c, attributeChangeObservationEvent).isPresent());

		});

	}

}
