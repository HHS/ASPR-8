package plugins.partitions.testsupport.attributes.datacontainers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Consumer;

import org.apache.commons.math3.random.RandomGenerator;
import org.junit.jupiter.api.Test;

import nucleus.AgentContext;
import nucleus.Context;
import nucleus.DataView;
import nucleus.Simulation;
import nucleus.Simulation.Builder;
import nucleus.NucleusError;
import nucleus.testsupport.actionplugin.ActionPlugin;
import nucleus.testsupport.actionplugin.AgentActionPlan;
import plugins.components.ComponentPlugin;
import plugins.partitions.PartitionsPlugin;
import plugins.partitions.testsupport.attributes.AttributesPlugin;
import plugins.partitions.testsupport.attributes.events.mutation.AttributeValueAssignmentEvent;
import plugins.partitions.testsupport.attributes.initialdata.AttributeInitialData;
import plugins.partitions.testsupport.attributes.support.AttributeDefinition;
import plugins.partitions.testsupport.attributes.support.AttributeError;
import plugins.partitions.testsupport.attributes.support.AttributeId;
import plugins.partitions.testsupport.attributes.support.TestAttributeId;
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
import util.SeedProvider;
import util.annotations.UnitTest;
import util.annotations.UnitTestConstructor;
import util.annotations.UnitTestMethod;

@UnitTest(target = AttributesDataView.class)
public final class AT_AttributesDataView implements DataView {

	private void testConsumer(final int initialPopultionSize, final Consumer<AgentContext> consumer) {
		final Builder builder = Simulation.builder();
		// define some person attributes
		final AttributeInitialData.Builder attributesBuilder = AttributeInitialData.builder();
		for (final TestAttributeId testAttributeId : TestAttributeId.values()) {
			attributesBuilder.defineAttribute(testAttributeId, testAttributeId.getAttributeDefinition());
		}
		builder.addPlugin(AttributesPlugin.PLUGIN_ID, new AttributesPlugin(attributesBuilder.build())::init);

		final PeopleInitialData.Builder peopleBuilder = PeopleInitialData.builder();
		for (int i = 0; i < initialPopultionSize; i++) {
			peopleBuilder.addPersonId(new PersonId(i));
		}
		builder.addPlugin(PeoplePlugin.PLUGIN_ID, new PeoplePlugin(peopleBuilder.build())::init);
		builder.addPlugin(ComponentPlugin.PLUGIN_ID, new ComponentPlugin()::init);
		builder.addPlugin(StochasticsPlugin.PLUGIN_ID, new StochasticsPlugin(StochasticsInitialData.builder().setSeed(5241628071704306523L).build())::init);
		builder.addPlugin(ReportPlugin.PLUGIN_ID, new ReportPlugin(ReportsInitialData.builder().build())::init);
		builder.addPlugin(PartitionsPlugin.PLUGIN_ID, new PartitionsPlugin()::init);

		/*
		 * Add an agent that executes the consumer.
		 *
		 * Add a second agent to show that the initial population exists and the
		 * attribute ids exist.
		 *
		 */
		final ActionPlugin.Builder pluginBuilder = ActionPlugin.builder();

		/*
		 * Add an agent to show that the partition data view exists
		 */
		pluginBuilder.addAgent("agent");
		pluginBuilder.addAgentActionPlan("agent", new AgentActionPlan(0, (c) -> {
			consumer.accept(c);
		}));

		// build and add the action plugin to the engine
		final ActionPlugin actionPlugin = pluginBuilder.build();
		builder.addPlugin(ActionPlugin.PLUGIN_ID, actionPlugin::init);

		// build and execute the engine
		builder.build().execute();

		// show that all actions were executed
		assertTrue(actionPlugin.allActionsExecuted());

	}

	@Test
	@UnitTestMethod(name = "getAttributeDefinition", args = { AttributeId.class })
	public void testGetAttributeDefinition() {
		// 7056826773207732206L
		testConsumer(100, (c) -> {

			AttributesDataView attributesDataView = c.getDataView(AttributesDataView.class).get();

			for (TestAttributeId testAttributeId : TestAttributeId.values()) {
				AttributeDefinition expectedAttributeDefinition = testAttributeId.getAttributeDefinition();
				AttributeDefinition actualAttributeDefinition = attributesDataView.getAttributeDefinition(testAttributeId);
				assertEquals(expectedAttributeDefinition, actualAttributeDefinition);
			}

			// precondition tests:

			// if the attribute id is null
			ContractException contractException = assertThrows(ContractException.class, () -> attributesDataView.getAttributeDefinition(null));
			assertEquals(AttributeError.NULL_ATTRIBUTE_ID, contractException.getErrorType());

			// if the attribute id unknown
			contractException = assertThrows(ContractException.class, () -> attributesDataView.getAttributeDefinition(TestAttributeId.getUnknownAttributeId()));
			assertEquals(AttributeError.UNKNOWN_ATTRIBUTE_ID, contractException.getErrorType());
		});

	}

	@Test
	@UnitTestMethod(name = "getAttributeIds", args = {})
	public void testGetAttributeIds() {
		testConsumer(100, (c) -> {
			AttributesDataView attributesDataView = c.getDataView(AttributesDataView.class).get();
			assertEquals(EnumSet.allOf(TestAttributeId.class), attributesDataView.getAttributeIds());
		});

	}

	@Test
	@UnitTestMethod(name = "getAttributeValue", args = { PersonId.class, AttributeId.class })
	public void testGetAttributeValue() {

		testConsumer(100, (c) -> {

			RandomGenerator randomGenerator = SeedProvider.getRandomGenerator(3296963241519285254L);

			AttributesDataView attributesDataView = c.getDataView(AttributesDataView.class).get();
			PersonDataView personDataView = c.getDataView(PersonDataView.class).get();

			// show that there are people in the test
			assertEquals(100, personDataView.getPopulationCount());

			// build a structure to hold expected attribute values
			Map<PersonId, Map<AttributeId, Object>> expectedValues = new LinkedHashMap<>();

			// set random attribute values on people
			for (PersonId personId : personDataView.getPeople()) {
				Map<AttributeId, Object> attributeMap = new LinkedHashMap<>();
				expectedValues.put(personId, attributeMap);

				boolean b0 = randomGenerator.nextBoolean();
				c.resolveEvent(new AttributeValueAssignmentEvent(personId, TestAttributeId.BOOLEAN_0, b0));
				attributeMap.put(TestAttributeId.BOOLEAN_0, b0);

				boolean b1 = randomGenerator.nextBoolean();
				c.resolveEvent(new AttributeValueAssignmentEvent(personId, TestAttributeId.BOOLEAN_1, b1));
				attributeMap.put(TestAttributeId.BOOLEAN_1, b1);

				int i0 = randomGenerator.nextInt();
				c.resolveEvent(new AttributeValueAssignmentEvent(personId, TestAttributeId.INT_0, i0));
				attributeMap.put(TestAttributeId.INT_0, i0);

				int i1 = randomGenerator.nextInt();
				c.resolveEvent(new AttributeValueAssignmentEvent(personId, TestAttributeId.INT_1, i1));
				attributeMap.put(TestAttributeId.INT_1, i1);

				double d0 = randomGenerator.nextDouble();
				c.resolveEvent(new AttributeValueAssignmentEvent(personId, TestAttributeId.DOUBLE_0, d0));
				attributeMap.put(TestAttributeId.DOUBLE_0, d0);

				double d1 = randomGenerator.nextDouble();
				c.resolveEvent(new AttributeValueAssignmentEvent(personId, TestAttributeId.DOUBLE_1, d1));
				attributeMap.put(TestAttributeId.DOUBLE_1, d1);

			}

			// show that the expected attribute values are returned by the data
			// view
			for (PersonId personId : expectedValues.keySet()) {
				Map<AttributeId, Object> attributeMap = expectedValues.get(personId);
				for (AttributeId attributeId : attributeMap.keySet()) {
					Object expectedValue = attributeMap.get(attributeId);
					Object actualValue = attributesDataView.getAttributeValue(personId, attributeId);
					assertEquals(expectedValue, actualValue);
				}
			}

			// precondition tests:

			PersonId goodPersonId = new PersonId(0);
			PersonId badPersonId = new PersonId(10000000);
			AttributeId goodAttributeId = TestAttributeId.BOOLEAN_0;
			AttributeId badAttributeId = TestAttributeId.getUnknownAttributeId();

			// if the attribute id is null
			ContractException contractException = assertThrows(ContractException.class, () -> attributesDataView.getAttributeValue(goodPersonId, null));
			assertEquals(AttributeError.NULL_ATTRIBUTE_ID, contractException.getErrorType());

			// if the attribute id unknown
			contractException = assertThrows(ContractException.class, () -> attributesDataView.getAttributeValue(goodPersonId, badAttributeId));
			assertEquals(AttributeError.UNKNOWN_ATTRIBUTE_ID, contractException.getErrorType());

			// if the person id is null
			contractException = assertThrows(ContractException.class, () -> attributesDataView.getAttributeValue(null, goodAttributeId));
			assertEquals(PersonError.NULL_PERSON_ID, contractException.getErrorType());

			// if the person id is unknown
			contractException = assertThrows(ContractException.class, () -> attributesDataView.getAttributeValue(badPersonId, goodAttributeId));
			assertEquals(PersonError.UNKNOWN_PERSON_ID, contractException.getErrorType());

		});

	}

	@Test
	@UnitTestConstructor(args = { Context.class, AttributesDataManager.class })
	public void testConstructor() {
		testConsumer(100, (c) -> {
			AttributesDataManager attributesDataManager = new AttributesDataManager(c);

			ContractException contractException = assertThrows(ContractException.class, () -> new AttributesDataView(null, attributesDataManager));
			assertEquals(NucleusError.NULL_CONTEXT, contractException.getErrorType());

			contractException = assertThrows(ContractException.class, () -> new AttributesDataView(c, null));
			assertEquals(AttributeError.NULL_ATTRIBUTE_DATA_MANAGER, contractException.getErrorType());

		});

	}

	@Test
	@UnitTestMethod(name = "attributeExists", args = { AttributeId.class })
	public void testAttributeExists() {
		testConsumer(100, (c) -> {
			AttributesDataView attributesDataView = c.getDataView(AttributesDataView.class).get();

			for (TestAttributeId testAttributeId : TestAttributeId.values()) {
				assertTrue(attributesDataView.attributeExists(testAttributeId));
			}
			
			assertFalse(attributesDataView.attributeExists(TestAttributeId.getUnknownAttributeId()));
			
			assertFalse(attributesDataView.attributeExists(null));
		});
	}

}
