package plugins.people.events.observation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.function.Consumer;

import org.junit.jupiter.api.Test;

import nucleus.AgentContext;
import nucleus.Engine;
import nucleus.Engine.EngineBuilder;
import nucleus.Event;
import nucleus.EventLabel;
import nucleus.EventLabeler;
import nucleus.testsupport.actionplugin.ActionPlugin;
import nucleus.testsupport.actionplugin.AgentActionPlan;
import plugins.people.PeoplePlugin;
import plugins.people.initialdata.PeopleInitialData;
import plugins.people.support.BulkPersonContructionData;
import plugins.people.support.PersonError;
import plugins.people.support.PersonId;
import util.ContractException;
import util.annotations.UnitTest;
import util.annotations.UnitTestConstructor;
import util.annotations.UnitTestMethod;

@UnitTest(target = BulkPersonCreationObservationEvent.class)
public class AT_BulkPersonCreationObservationEvent implements Event {

	@Test
	@UnitTestConstructor(args = { PersonId.class, BulkPersonContructionData.class })
	public void testConstruction() {

		// precondition tests
		PersonId personId = new PersonId(0);
		BulkPersonContructionData bulkPersonContructionData = BulkPersonContructionData.builder().build();

		ContractException contractException = assertThrows(ContractException.class, () -> new BulkPersonCreationObservationEvent(null, bulkPersonContructionData));
		assertEquals(PersonError.NULL_PERSON_ID, contractException.getErrorType());

		contractException = assertThrows(ContractException.class, () -> new BulkPersonCreationObservationEvent(personId, null));
		assertEquals(PersonError.NULL_BULK_PERSON_CONTRUCTION_DATA, contractException.getErrorType());

	}

	@Test
	@UnitTestMethod(name = "getPersonId", args = {})
	public void testGetPersonId() {

		BulkPersonContructionData bulkPersonContructionData = BulkPersonContructionData.builder().build();

		for (int i = 0; i < 10; i++) {
			PersonId personId = new PersonId(i);
			BulkPersonCreationObservationEvent bulkPersonCreationObservationEvent = new BulkPersonCreationObservationEvent(personId, bulkPersonContructionData);
			assertEquals(personId, bulkPersonCreationObservationEvent.getPersonId());
		}

	}

	@Test
	@UnitTestMethod(name = "getBulkPersonContructionData", args = {})
	public void testGetBulkPersonContructionData() {
		PersonId personId = new PersonId(45);
		BulkPersonContructionData bulkPersonContructionData = BulkPersonContructionData.builder().build();
		BulkPersonCreationObservationEvent bulkPersonCreationObservationEvent = new BulkPersonCreationObservationEvent(personId, bulkPersonContructionData);
		assertEquals(bulkPersonContructionData, bulkPersonCreationObservationEvent.getBulkPersonContructionData());
	}

	/*
	 * Runs the engine by loading all plugins necessary to support people and
	 * executes the given consumer as an AgentActionPlan.
	 */
	private void testConsumer(Consumer<AgentContext> consumer) {

		EngineBuilder engineBuilder = Engine.builder();

		engineBuilder.addPlugin(PeoplePlugin.PLUGIN_ID, new PeoplePlugin(PeopleInitialData.builder().build())::init);

		ActionPlugin.Builder pluginBuilder = ActionPlugin.builder();

		pluginBuilder.addAgent("agent");
		pluginBuilder.addAgentActionPlan("agent", new AgentActionPlan(0, consumer));

		ActionPlugin actionPlugin = pluginBuilder.build();
		engineBuilder.addPlugin(ActionPlugin.PLUGIN_ID, actionPlugin::init);

		// build and execute the engine
		engineBuilder.build().execute();

		// show that all actions were executed
		assertTrue(actionPlugin.allActionsExecuted());

	}

	@Test
	@UnitTestMethod(name = "getEventLabel", args = {})
	public void testGetEventLabel() {
		EventLabel<BulkPersonCreationObservationEvent> eventLabel = BulkPersonCreationObservationEvent.getEventLabel();
		assertEquals(BulkPersonCreationObservationEvent.class, eventLabel.getEventClass());
		assertEquals(BulkPersonCreationObservationEvent.class, eventLabel.getPrimaryKeyValue());
		assertEquals(BulkPersonCreationObservationEvent.getEventLabeler().getId(), eventLabel.getLabelerId());
	}

	@Test
	@UnitTestMethod(name = "getEventLabeler", args = {})
	public void testGetEventLabeler() {
		testConsumer((c) -> {
			// show that the event labeler can be constructed has the correct
			// values
			EventLabeler<BulkPersonCreationObservationEvent> eventLabeler = BulkPersonCreationObservationEvent.getEventLabeler();
			assertEquals(BulkPersonCreationObservationEvent.class, eventLabeler.getEventClass());

			assertEquals(BulkPersonCreationObservationEvent.getEventLabel().getLabelerId(), eventLabeler.getId());

			// show that the event labeler produces the expected event
			// label

			// create an event			
			BulkPersonCreationObservationEvent event = new BulkPersonCreationObservationEvent(new PersonId(0), BulkPersonContructionData.builder().build());

			// derive the expected event label for this event
			EventLabel<BulkPersonCreationObservationEvent> expectedEventLabel = BulkPersonCreationObservationEvent.getEventLabel();

			// have the event labeler produce an event label and show it
			// is equal to the expected event label
			EventLabel<BulkPersonCreationObservationEvent> actualEventLabel = eventLabeler.getEventLabel(c, event);
			assertEquals(expectedEventLabel, actualEventLabel);

		});
	}
}
