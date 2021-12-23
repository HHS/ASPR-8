package plugins.partitions.testsupport.attributes.resolvers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.EnumSet;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.Test;

import nucleus.Engine;
import nucleus.Engine.EngineBuilder;
import nucleus.EventLabeler;
import nucleus.NucleusError;
import nucleus.ResolverContext;
import nucleus.testsupport.actionplugin.ActionPlugin;
import nucleus.testsupport.actionplugin.AgentActionPlan;
import plugins.components.ComponentPlugin;
import plugins.partitions.PartitionsPlugin;
import plugins.partitions.testsupport.attributes.AttributesPlugin;
import plugins.partitions.testsupport.attributes.datacontainers.AttributesDataView;
import plugins.partitions.testsupport.attributes.events.mutation.AttributeValueAssignmentEvent;
import plugins.partitions.testsupport.attributes.events.observation.AttributeChangeObservationEvent;
import plugins.partitions.testsupport.attributes.initialdata.AttributeInitialData;
import plugins.partitions.testsupport.attributes.support.TestAttributeId;
import plugins.people.PeoplePlugin;
import plugins.people.datacontainers.PersonDataView;
import plugins.people.initialdata.PeopleInitialData;
import plugins.people.support.PersonId;
import plugins.reports.ReportPlugin;
import plugins.reports.initialdata.ReportsInitialData;
import plugins.stochastics.StochasticsPlugin;
import plugins.stochastics.initialdata.StochasticsInitialData;
import util.ContractException;
import util.annotations.UnitTest;
import util.annotations.UnitTestConstructor;
import util.annotations.UnitTestMethod;

@UnitTest(target = AttributesEventResolver.class)
public class AT_AttributesEventResolver {

	@Test
	@UnitTestConstructor(args = { AttributeInitialData.class })
	public void testConstructor() {
		assertThrows(ContractException.class, () -> new AttributesEventResolver(null));
	}

	@Test
	@UnitTestMethod(name = "init", args = { ResolverContext.class })
	public void testAttributesDataViewInitialization() {
		EngineBuilder engineBuilder = Engine.builder();

		// add the attributes plugin
		AttributeInitialData.Builder attributeBuilder = AttributeInitialData.builder();
		for (TestAttributeId testAttributeId : TestAttributeId.values()) {
			attributeBuilder.defineAttribute(testAttributeId, testAttributeId.getAttributeDefinition());
		}
		AttributeInitialData attributeInitialData = attributeBuilder.build();
		engineBuilder.addPlugin(AttributesPlugin.PLUGIN_ID, new AttributesPlugin(attributeInitialData)::init);

		// add the people plugin
		engineBuilder.addPlugin(PeoplePlugin.PLUGIN_ID, new PeoplePlugin(PeopleInitialData.builder().build())::init);
		engineBuilder.addPlugin(ComponentPlugin.PLUGIN_ID, new ComponentPlugin()::init);
		engineBuilder.addPlugin(StochasticsPlugin.PLUGIN_ID, new StochasticsPlugin(StochasticsInitialData.builder().setSeed(5241628071704306523L).build())::init);
		engineBuilder.addPlugin(ReportPlugin.PLUGIN_ID, new ReportPlugin(ReportsInitialData.builder().build())::init);
		engineBuilder.addPlugin(PartitionsPlugin.PLUGIN_ID, new PartitionsPlugin()::init);
		ActionPlugin.Builder pluginBuilder = ActionPlugin.builder();

		// add an agent that will show that the AttributesDataView is properly
		// initialized
		pluginBuilder.addAgent("agent");
		pluginBuilder.addAgentActionPlan("agent", new AgentActionPlan(0, (c) -> {
			Optional<AttributesDataView> optional = c.getDataView(AttributesDataView.class);
			assertTrue(optional.isPresent());

			AttributesDataView attributesDataView = optional.get();
			assertEquals(EnumSet.allOf(TestAttributeId.class), attributesDataView.getAttributeIds());

			for (TestAttributeId testAttributeId : TestAttributeId.values()) {
				assertEquals(testAttributeId.getAttributeDefinition(), attributesDataView.getAttributeDefinition(testAttributeId));
			}

		}));

		ActionPlugin actionPlugin = pluginBuilder.build();
		engineBuilder.addPlugin(ActionPlugin.PLUGIN_ID, actionPlugin::init);

		// build and execute the engine
		engineBuilder.build().execute();

		// show that all actions were executed
		assertTrue(actionPlugin.allActionsExecuted());
	}

	@Test
	@UnitTestMethod(name = "init", args = { ResolverContext.class })
	public void testAttributeValueAssignmentEvent() {
		EngineBuilder engineBuilder = Engine.builder();

		// add the attributes plugin
		AttributeInitialData.Builder attributeBuilder = AttributeInitialData.builder();
		for (TestAttributeId testAttributeId : TestAttributeId.values()) {
			attributeBuilder.defineAttribute(testAttributeId, testAttributeId.getAttributeDefinition());
		}
		AttributeInitialData attributeInitialData = attributeBuilder.build();
		engineBuilder.addPlugin(AttributesPlugin.PLUGIN_ID, new AttributesPlugin(attributeInitialData)::init);

		// add the people plugin
		PeopleInitialData.Builder peopleBuilder = PeopleInitialData.builder();
		for (int i = 0; i < 10; i++) {
			peopleBuilder.addPersonId(new PersonId(i));
		}
		PeopleInitialData peopleInitialData = peopleBuilder.build();
		engineBuilder.addPlugin(PeoplePlugin.PLUGIN_ID, new PeoplePlugin(peopleInitialData)::init);
		
		engineBuilder.addPlugin(ComponentPlugin.PLUGIN_ID, new ComponentPlugin()::init);
		engineBuilder.addPlugin(StochasticsPlugin.PLUGIN_ID, new StochasticsPlugin(StochasticsInitialData.builder().setSeed(5241628071704306523L).build())::init);
		engineBuilder.addPlugin(ReportPlugin.PLUGIN_ID, new ReportPlugin(ReportsInitialData.builder().build())::init);
		engineBuilder.addPlugin(PartitionsPlugin.PLUGIN_ID, new PartitionsPlugin()::init);

		ActionPlugin.Builder pluginBuilder = ActionPlugin.builder();

		// add an agent that will observe attribute changes
		Set<PersonId> peopleObserved = new LinkedHashSet<>();

		pluginBuilder.addAgent("observer");
		pluginBuilder.addAgentActionPlan("observer", new AgentActionPlan(0, (c) -> {
			c.subscribe(AttributeChangeObservationEvent.getEventLabel(c, TestAttributeId.BOOLEAN_0), (c2, e) -> {
				
				peopleObserved.add(e.getPersonId());
			});

		}));

		// add an agent that will show that the AttributesDataView is properly
		// initialized
		pluginBuilder.addAgent("agent");
		pluginBuilder.addAgentActionPlan("agent", new AgentActionPlan(1, (c) -> {

			PersonDataView personDataView = c.getDataView(PersonDataView.class).get();
			AttributesDataView attributesDataView = c.getDataView(AttributesDataView.class).get();

			for (PersonId personId : personDataView.getPeople()) {
				Boolean value = attributesDataView.getAttributeValue(personId, TestAttributeId.BOOLEAN_0);

				assertFalse(value);

				c.resolveEvent(new AttributeValueAssignmentEvent(personId, TestAttributeId.BOOLEAN_0, true));
				value = attributesDataView.getAttributeValue(personId, TestAttributeId.BOOLEAN_0);

				assertTrue(value);

			}

		}));

		ActionPlugin actionPlugin = pluginBuilder.build();
		engineBuilder.addPlugin(ActionPlugin.PLUGIN_ID, actionPlugin::init);

		// build and execute the engine
		engineBuilder.build().execute();

		// show that all actions were executed
		assertTrue(actionPlugin.allActionsExecuted());

		// show that the correct observations were made;
		assertEquals(peopleInitialData.getPersonIds(), peopleObserved);

	}

	@Test
	@UnitTestMethod(name = "init", args = { ResolverContext.class })
	public void testAttributeChangeObservationEventLabelers() {
		EngineBuilder engineBuilder = Engine.builder();

		// add the attributes plugin
		AttributeInitialData.Builder attributeBuilder = AttributeInitialData.builder();
		for (TestAttributeId testAttributeId : TestAttributeId.values()) {
			attributeBuilder.defineAttribute(testAttributeId, testAttributeId.getAttributeDefinition());
		}
		AttributeInitialData attributeInitialData = attributeBuilder.build();
		engineBuilder.addPlugin(AttributesPlugin.PLUGIN_ID, new AttributesPlugin(attributeInitialData)::init);

		// add the people plugin
		engineBuilder.addPlugin(PeoplePlugin.PLUGIN_ID, new PeoplePlugin(PeopleInitialData.builder().build())::init);
		
		
		
		engineBuilder.addPlugin(ComponentPlugin.PLUGIN_ID, new ComponentPlugin()::init);
		engineBuilder.addPlugin(StochasticsPlugin.PLUGIN_ID, new StochasticsPlugin(StochasticsInitialData.builder().setSeed(5241628071704306523L).build())::init);
		engineBuilder.addPlugin(ReportPlugin.PLUGIN_ID, new ReportPlugin(ReportsInitialData.builder().build())::init);
		engineBuilder.addPlugin(PartitionsPlugin.PLUGIN_ID, new PartitionsPlugin()::init);

		ActionPlugin.Builder pluginBuilder = ActionPlugin.builder();


		// add an agent that will show that the AttributeChangeObservation event labelers were added by the resolver
		pluginBuilder.addAgent("agent");
		pluginBuilder.addAgentActionPlan("agent", new AgentActionPlan(1, (c) -> {
			EventLabeler<AttributeChangeObservationEvent> eventLabeler = AttributeChangeObservationEvent.getEventLabeler();
			assertNotNull(eventLabeler);
			ContractException contractException = assertThrows(ContractException.class, () -> c.addEventLabeler(eventLabeler));
			assertEquals(NucleusError.DUPLICATE_LABELER_ID_IN_EVENT_LABELER, contractException.getErrorType());
		}));

		ActionPlugin actionPlugin = pluginBuilder.build();
		engineBuilder.addPlugin(ActionPlugin.PLUGIN_ID, actionPlugin::init);

		// build and execute the engine
		engineBuilder.build().execute();

		// show that all actions were executed
		assertTrue(actionPlugin.allActionsExecuted());

	}

}
