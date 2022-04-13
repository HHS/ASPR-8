package plugins.people.events;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import nucleus.Event;
import nucleus.EventLabel;
import nucleus.EventLabeler;
import nucleus.util.ContractException;
import plugins.people.support.PersonError;
import plugins.people.support.PersonId;
import plugins.people.testsupport.PeopleActionSupport;
import tools.annotations.UnitTest;
import tools.annotations.UnitTestConstructor;
import tools.annotations.UnitTestMethod;

@UnitTest(target = PersonImminentRemovalEvent.class)
public class AT_PersonImminentRemovalEvent implements Event {

	@Test
	@UnitTestConstructor(args = { PersonId.class })
	public void testConstructor() {

		// precondition tests
		ContractException contractException = assertThrows(ContractException.class, () -> new PersonImminentRemovalEvent(null));
		assertEquals(PersonError.NULL_PERSON_ID, contractException.getErrorType());

	}

	@Test
	@UnitTestMethod(name = "getPersonId", args = { PersonId.class })
	public void testGetPersonId() {
		for (int i = 0; i < 10; i++) {
			PersonId personId = new PersonId(i);
			PersonImminentRemovalEvent personImminentRemovalEvent = new PersonImminentRemovalEvent(personId);
			assertEquals(personId, personImminentRemovalEvent.getPersonId());
		}
	}

	@Test
	@UnitTestMethod(name = "toString", args = {})
	public void testToString() {
		for (int i = 0; i < 10; i++) {
			PersonId personId = new PersonId(i);
			PersonImminentRemovalEvent personImminentRemovalEvent = new PersonImminentRemovalEvent(personId);
			String expectedValue = "PersonImminentRemovalEvent [personId="+i+"]";
			String actualValue =personImminentRemovalEvent.toString();
			assertEquals(expectedValue, actualValue);
		}
		
	}

	@Test
	@UnitTestMethod(name = "getEventLabel", args = {})
	public void testGetEventLabel() {
		EventLabel<PersonImminentRemovalEvent> eventLabel = PersonImminentRemovalEvent.getEventLabel();
		assertEquals(PersonImminentRemovalEvent.class, eventLabel.getEventClass());
		assertEquals(PersonImminentRemovalEvent.class, eventLabel.getPrimaryKeyValue());
		assertEquals(PersonImminentRemovalEvent.getEventLabeler().getId(), eventLabel.getLabelerId());
	}
	
	@Test
	@UnitTestMethod(name = "getEventLabeler", args = {})
	public void testGetEventLabeler() {
		PeopleActionSupport.testConsumer(0,(c) -> {
			// show that the event labeler can be constructed has the correct
			// values
			EventLabeler<PersonImminentRemovalEvent> eventLabeler = PersonImminentRemovalEvent.getEventLabeler();
			assertEquals(PersonImminentRemovalEvent.class, eventLabeler.getEventClass());

			assertEquals(PersonImminentRemovalEvent.getEventLabel().getLabelerId(), eventLabeler.getId());

			// show that the event labeler produces the expected event
			// label

			// create an event			
			PersonImminentRemovalEvent event = new PersonImminentRemovalEvent(new PersonId(0));

			// derive the expected event label for this event
			EventLabel<PersonImminentRemovalEvent> expectedEventLabel = PersonImminentRemovalEvent.getEventLabel();

			// have the event labeler produce an event label and show it
			// is equal to the expected event label
			EventLabel<PersonImminentRemovalEvent> actualEventLabel = eventLabeler.getEventLabel(c, event);
			assertEquals(expectedEventLabel, actualEventLabel);

		});
	}
}
