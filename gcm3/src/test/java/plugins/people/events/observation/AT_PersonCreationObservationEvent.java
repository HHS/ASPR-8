package plugins.people.events.observation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.function.Consumer;

import org.junit.jupiter.api.Test;

import nucleus.AgentContext;
import nucleus.Simulation;
import nucleus.Simulation.Builder;
import nucleus.Event;
import nucleus.EventLabel;
import nucleus.EventLabeler;
import nucleus.testsupport.actionplugin.ActionPlugin;
import nucleus.testsupport.actionplugin.AgentActionPlan;
import plugins.people.PeoplePlugin;
import plugins.people.initialdata.PeopleInitialData;
import plugins.people.support.PersonContructionData;
import plugins.people.support.PersonError;
import plugins.people.support.PersonId;
import util.ContractException;
import util.annotations.UnitTest;
import util.annotations.UnitTestConstructor;
import util.annotations.UnitTestMethod;

@UnitTest(target = PersonCreationObservationEvent.class)
public class AT_PersonCreationObservationEvent implements Event {

	@Test
	@UnitTestConstructor(args = { PersonId.class, PersonContructionData.class })
	public void testConstructor() {
		PersonId personId = new PersonId(0);
		PersonContructionData personContructionData = PersonContructionData.builder().build();

		ContractException contractException = assertThrows(ContractException.class, () -> new PersonCreationObservationEvent(null, personContructionData));
		assertEquals(PersonError.NULL_PERSON_ID, contractException.getErrorType());

		contractException = assertThrows(ContractException.class, () -> new PersonCreationObservationEvent(personId, null));
		assertEquals(PersonError.NULL_PERSON_CONTRUCTION_DATA, contractException.getErrorType());
	}

	@Test
	@UnitTestMethod(name = "getPersonId", args = {})
	public void testGetPersonId() {
		for (int i = 0; i < 10; i++) {
			PersonId personId = new PersonId(i);
			PersonContructionData personContructionData = PersonContructionData.builder().build();
			PersonCreationObservationEvent personCreationObservationEvent = new PersonCreationObservationEvent(personId, personContructionData);

			assertEquals(personId, personCreationObservationEvent.getPersonId());
		}
	}

	@Test
	@UnitTestMethod(name = "getPersonContructionData", args = {})
	public void testGetPersonContructionData() {
		PersonId personId = new PersonId(0);
		PersonContructionData personContructionData = PersonContructionData.builder().build();
		PersonCreationObservationEvent personCreationObservationEvent = new PersonCreationObservationEvent(personId, personContructionData);

		assertEquals(personContructionData, personCreationObservationEvent.getPersonContructionData());
	}

	@Test
	@UnitTestMethod(name = "getEventLabel", args = {})
	public void testGetEventLabel() {
		EventLabel<PersonCreationObservationEvent> eventLabel = PersonCreationObservationEvent.getEventLabel();
		assertEquals(PersonCreationObservationEvent.class, eventLabel.getEventClass());
		assertEquals(PersonCreationObservationEvent.class, eventLabel.getPrimaryKeyValue());
		assertEquals(PersonCreationObservationEvent.getEventLabeler().getId(), eventLabel.getLabelerId());
	}
	/*
	 * Runs the engine by loading all plugins necessary to support people and
	 * executes the given consumer as an AgentActionPlan.
	 */
	private void testConsumer(Consumer<AgentContext> consumer) {

		Builder builder = Simulation.builder();

		builder.addPlugin(PeoplePlugin.PLUGIN_ID, new PeoplePlugin(PeopleInitialData.builder().build())::init);

		ActionPlugin.Builder pluginBuilder = ActionPlugin.builder();

		pluginBuilder.addAgent("agent");
		pluginBuilder.addAgentActionPlan("agent", new AgentActionPlan(0, consumer));

		ActionPlugin actionPlugin = pluginBuilder.build();
		builder.addPlugin(ActionPlugin.PLUGIN_ID, actionPlugin::init);

		// build and execute the engine
		builder.build().execute();

		// show that all actions were executed
		assertTrue(actionPlugin.allActionsExecuted());

	}

	@Test
	@UnitTestMethod(name = "getEventLabeler", args = {})
	public void testGetEventLabeler() {
		testConsumer((c) -> {
			// show that the event labeler can be constructed has the correct
			// values
			EventLabeler<PersonCreationObservationEvent> eventLabeler = PersonCreationObservationEvent.getEventLabeler();
			assertEquals(PersonCreationObservationEvent.class, eventLabeler.getEventClass());

			assertEquals(PersonCreationObservationEvent.getEventLabel().getLabelerId(), eventLabeler.getId());

			// show that the event labeler produces the expected event
			// label

			// create an event			
			PersonCreationObservationEvent event = new PersonCreationObservationEvent(new PersonId(0), PersonContructionData.builder().build());

			// derive the expected event label for this event
			EventLabel<PersonCreationObservationEvent> expectedEventLabel = PersonCreationObservationEvent.getEventLabel();

			// have the event labeler produce an event label and show it
			// is equal to the expected event label
			EventLabel<PersonCreationObservationEvent> actualEventLabel = eventLabeler.getEventLabel(c, event);
			assertEquals(expectedEventLabel, actualEventLabel);

		});
	}
}
