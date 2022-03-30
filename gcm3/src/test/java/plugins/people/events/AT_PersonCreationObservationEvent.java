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
import plugins.people.support.PersonConstructionData;
import plugins.people.support.PersonError;
import plugins.people.support.PersonId;
import plugins.people.testsupport.PeopleActionSupport;

@UnitTest(target = PersonCreationObservationEvent.class)
public class AT_PersonCreationObservationEvent implements Event {

	@Test
	@UnitTestConstructor(args = { PersonId.class, PersonConstructionData.class })
	public void testConstructor() {
		PersonId personId = new PersonId(0);
		PersonConstructionData personConstructionData = PersonConstructionData.builder().build();

		ContractException contractException = assertThrows(ContractException.class, () -> new PersonCreationObservationEvent(null, personConstructionData));
		assertEquals(PersonError.NULL_PERSON_ID, contractException.getErrorType());

		contractException = assertThrows(ContractException.class, () -> new PersonCreationObservationEvent(personId, null));
		assertEquals(PersonError.NULL_PERSON_CONSTRUCTION_DATA, contractException.getErrorType());
	}

	@Test
	@UnitTestMethod(name = "getPersonId", args = {})
	public void testGetPersonId() {
		for (int i = 0; i < 10; i++) {
			PersonId personId = new PersonId(i);
			PersonConstructionData personConstructionData = PersonConstructionData.builder().build();
			PersonCreationObservationEvent personCreationObservationEvent = new PersonCreationObservationEvent(personId, personConstructionData);

			assertEquals(personId, personCreationObservationEvent.getPersonId());
		}
	}

	@Test
	@UnitTestMethod(name = "getPersonConstructionData", args = {})
	public void testGetPersonConstructionData() {
		PersonId personId = new PersonId(0);
		PersonConstructionData personConstructionData = PersonConstructionData.builder().build();
		PersonCreationObservationEvent personCreationObservationEvent = new PersonCreationObservationEvent(personId, personConstructionData);

		assertEquals(personConstructionData, personCreationObservationEvent.getPersonConstructionData());
	}

	@Test
	@UnitTestMethod(name = "getEventLabel", args = {})
	public void testGetEventLabel() {
		EventLabel<PersonCreationObservationEvent> eventLabel = PersonCreationObservationEvent.getEventLabel();
		assertEquals(PersonCreationObservationEvent.class, eventLabel.getEventClass());
		assertEquals(PersonCreationObservationEvent.class, eventLabel.getPrimaryKeyValue());
		assertEquals(PersonCreationObservationEvent.getEventLabeler().getId(), eventLabel.getLabelerId());
	}
	

	@Test
	@UnitTestMethod(name = "getEventLabeler", args = {})
	public void testGetEventLabeler() {
		PeopleActionSupport.testConsumer(0,(c) -> {
			// show that the event labeler can be constructed has the correct
			// values
			EventLabeler<PersonCreationObservationEvent> eventLabeler = PersonCreationObservationEvent.getEventLabeler();
			assertEquals(PersonCreationObservationEvent.class, eventLabeler.getEventClass());

			assertEquals(PersonCreationObservationEvent.getEventLabel().getLabelerId(), eventLabeler.getId());

			// show that the event labeler produces the expected event
			// label

			// create an event			
			PersonCreationObservationEvent event = new PersonCreationObservationEvent(new PersonId(0), PersonConstructionData.builder().build());

			// derive the expected event label for this event
			EventLabel<PersonCreationObservationEvent> expectedEventLabel = PersonCreationObservationEvent.getEventLabel();

			// have the event labeler produce an event label and show it
			// is equal to the expected event label
			EventLabel<PersonCreationObservationEvent> actualEventLabel = eventLabeler.getEventLabel(c, event);
			assertEquals(expectedEventLabel, actualEventLabel);

		});
	}
}
