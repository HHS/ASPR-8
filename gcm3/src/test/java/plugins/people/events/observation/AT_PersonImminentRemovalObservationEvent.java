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
import plugins.people.support.PersonError;
import plugins.people.support.PersonId;
import util.ContractException;
import util.annotations.UnitTest;
import util.annotations.UnitTestConstructor;
import util.annotations.UnitTestMethod;

@UnitTest(target = PersonImminentRemovalObservationEvent.class)
public class AT_PersonImminentRemovalObservationEvent implements Event {

	@Test
	@UnitTestConstructor(args = { PersonId.class })
	public void testConstructor() {

		// precondition tests
		ContractException contractException = assertThrows(ContractException.class, () -> new PersonImminentRemovalObservationEvent(null));
		assertEquals(PersonError.NULL_PERSON_ID, contractException.getErrorType());

	}

	@Test
	@UnitTestMethod(name = "getPersonId", args = { PersonId.class })
	public void testGetPersonId() {
		for (int i = 0; i < 10; i++) {
			PersonId personId = new PersonId(i);
			PersonImminentRemovalObservationEvent personImminentRemovalObservationEvent = new PersonImminentRemovalObservationEvent(personId);
			assertEquals(personId, personImminentRemovalObservationEvent.getPersonId());
		}
	}

	@Test
	@UnitTestMethod(name = "toString", args = {})
	public void testToString() {
		for (int i = 0; i < 10; i++) {
			PersonId personId = new PersonId(i);
			PersonImminentRemovalObservationEvent personImminentRemovalObservationEvent = new PersonImminentRemovalObservationEvent(personId);
			String expectedValue = "PersonRemovalObservationEvent [personId="+i+"]";
			String actualValue =personImminentRemovalObservationEvent.toString();
			assertEquals(expectedValue, actualValue);
		}
		
	}

	@Test
	@UnitTestMethod(name = "getEventLabel", args = {})
	public void testGetEventLabel() {
		EventLabel<PersonImminentRemovalObservationEvent> eventLabel = PersonImminentRemovalObservationEvent.getEventLabel();
		assertEquals(PersonImminentRemovalObservationEvent.class, eventLabel.getEventClass());
		assertEquals(PersonImminentRemovalObservationEvent.class, eventLabel.getPrimaryKeyValue());
		assertEquals(PersonImminentRemovalObservationEvent.getEventLabeler().getId(), eventLabel.getLabelerId());
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
	@UnitTestMethod(name = "getEventLabeler", args = {})
	public void testGetEventLabeler() {
		testConsumer((c) -> {
			// show that the event labeler can be constructed has the correct
			// values
			EventLabeler<PersonImminentRemovalObservationEvent> eventLabeler = PersonImminentRemovalObservationEvent.getEventLabeler();
			assertEquals(PersonImminentRemovalObservationEvent.class, eventLabeler.getEventClass());

			assertEquals(PersonImminentRemovalObservationEvent.getEventLabel().getLabelerId(), eventLabeler.getId());

			// show that the event labeler produces the expected event
			// label

			// create an event			
			PersonImminentRemovalObservationEvent event = new PersonImminentRemovalObservationEvent(new PersonId(0));

			// derive the expected event label for this event
			EventLabel<PersonImminentRemovalObservationEvent> expectedEventLabel = PersonImminentRemovalObservationEvent.getEventLabel();

			// have the event labeler produce an event label and show it
			// is equal to the expected event label
			EventLabel<PersonImminentRemovalObservationEvent> actualEventLabel = eventLabeler.getEventLabel(c, event);
			assertEquals(expectedEventLabel, actualEventLabel);

		});
	}
}
