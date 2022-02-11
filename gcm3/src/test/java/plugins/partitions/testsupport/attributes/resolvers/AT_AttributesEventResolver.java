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

import nucleus.EventLabeler;
import nucleus.NucleusError;
import nucleus.ResolverContext;
import nucleus.testsupport.actionplugin.ActionPlugin;
import nucleus.testsupport.actionplugin.AgentActionPlan;
import plugins.partitions.testsupport.PartitionsActionSupport;
import plugins.partitions.testsupport.attributes.datacontainers.AttributesDataView;
import plugins.partitions.testsupport.attributes.events.mutation.AttributeValueAssignmentEvent;
import plugins.partitions.testsupport.attributes.events.observation.AttributeChangeObservationEvent;
import plugins.partitions.testsupport.attributes.initialdata.AttributeInitialData;
import plugins.partitions.testsupport.attributes.support.TestAttributeId;
import plugins.people.datacontainers.PersonDataView;
import plugins.people.support.PersonId;
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
		PartitionsActionSupport.testConsumer(0, 5241628071704306523L, (c) -> {
			Optional<AttributesDataView> optional = c.getDataView(AttributesDataView.class);
			assertTrue(optional.isPresent());

			AttributesDataView attributesDataView = optional.get();
			assertEquals(EnumSet.allOf(TestAttributeId.class), attributesDataView.getAttributeIds());

			for (TestAttributeId testAttributeId : TestAttributeId.values()) {
				assertEquals(testAttributeId.getAttributeDefinition(), attributesDataView.getAttributeDefinition(testAttributeId));
			}
		});
	}

	@Test
	@UnitTestMethod(name = "init", args = { ResolverContext.class })
	public void testAttributeValueAssignmentEvent() {

		ActionPlugin.Builder pluginBuilder = ActionPlugin.builder();

		// add an agent that will observe attribute changes
		Set<PersonId> peopleObserved = new LinkedHashSet<>();
		Set<PersonId> expectedPersonIds = new LinkedHashSet<>();
		for (int i = 0; i < 10; i++) {
			expectedPersonIds.add(new PersonId(i));
		}

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
		PartitionsActionSupport.testConsumers(expectedPersonIds.size(), 5241628071704306523L, actionPlugin);

		// show that the correct observations were made;
		assertEquals(expectedPersonIds, peopleObserved);

	}

	@Test
	@UnitTestMethod(name = "init", args = { ResolverContext.class })
	public void testAttributeChangeObservationEventLabelers() {
		PartitionsActionSupport.testConsumer(0, 5241628071704306523L, (c) -> {
			EventLabeler<AttributeChangeObservationEvent> eventLabeler = AttributeChangeObservationEvent.getEventLabeler();
			assertNotNull(eventLabeler);
			ContractException contractException = assertThrows(ContractException.class, () -> c.addEventLabeler(eventLabeler));
			assertEquals(NucleusError.DUPLICATE_LABELER_ID_IN_EVENT_LABELER, contractException.getErrorType());
		});
	}

}
