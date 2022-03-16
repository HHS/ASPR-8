package plugins.people.events;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import annotations.UnitTest;
import annotations.UnitTestConstructor;
import annotations.UnitTestMethod;
import nucleus.Event;
import nucleus.EventLabel;
import nucleus.EventLabeler;
import nucleus.util.ContractException;
import plugins.people.support.PersonError;
import plugins.people.support.PersonId;
import plugins.people.testsupport.PeopleActionSupport;

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
	
	@Test
	@UnitTestMethod(name = "getEventLabeler", args = {})
	public void testGetEventLabeler() {
		PeopleActionSupport.testConsumer((c) -> {
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
