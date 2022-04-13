package plugins.people.events;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import nucleus.Event;
import nucleus.EventLabel;
import nucleus.EventLabeler;
import nucleus.util.ContractException;
import plugins.people.support.PersonConstructionData;
import plugins.people.support.PersonError;
import plugins.people.support.PersonId;
import plugins.people.testsupport.PeopleActionSupport;
import tools.annotations.UnitTest;
import tools.annotations.UnitTestConstructor;
import tools.annotations.UnitTestMethod;

@UnitTest(target = PersonAdditionEvent.class)
public class AT_PersonAdditionEvent implements Event {

	@Test
	@UnitTestConstructor(args = { PersonId.class, PersonConstructionData.class })
	public void testConstructor() {
		PersonId personId = new PersonId(0);
		PersonConstructionData personConstructionData = PersonConstructionData.builder().build();

		ContractException contractException = assertThrows(ContractException.class, () -> new PersonAdditionEvent(null, personConstructionData));
		assertEquals(PersonError.NULL_PERSON_ID, contractException.getErrorType());

		contractException = assertThrows(ContractException.class, () -> new PersonAdditionEvent(personId, null));
		assertEquals(PersonError.NULL_PERSON_CONSTRUCTION_DATA, contractException.getErrorType());
	}

	@Test
	@UnitTestMethod(name = "getPersonId", args = {})
	public void testGetPersonId() {
		for (int i = 0; i < 10; i++) {
			PersonId personId = new PersonId(i);
			PersonConstructionData personConstructionData = PersonConstructionData.builder().build();
			PersonAdditionEvent personAdditionEvent = new PersonAdditionEvent(personId, personConstructionData);

			assertEquals(personId, personAdditionEvent.getPersonId());
		}
	}

	@Test
	@UnitTestMethod(name = "getPersonConstructionData", args = {})
	public void testGetPersonConstructionData() {
		PersonId personId = new PersonId(0);
		PersonConstructionData personConstructionData = PersonConstructionData.builder().build();
		PersonAdditionEvent personAdditionEvent = new PersonAdditionEvent(personId, personConstructionData);

		assertEquals(personConstructionData, personAdditionEvent.getPersonConstructionData());
	}

	@Test
	@UnitTestMethod(name = "getEventLabel", args = {})
	public void testGetEventLabel() {
		EventLabel<PersonAdditionEvent> eventLabel = PersonAdditionEvent.getEventLabel();
		assertEquals(PersonAdditionEvent.class, eventLabel.getEventClass());
		assertEquals(PersonAdditionEvent.class, eventLabel.getPrimaryKeyValue());
		assertEquals(PersonAdditionEvent.getEventLabeler().getId(), eventLabel.getLabelerId());
	}
	

	@Test
	@UnitTestMethod(name = "getEventLabeler", args = {})
	public void testGetEventLabeler() {
		PeopleActionSupport.testConsumer(0,(c) -> {
			// show that the event labeler can be constructed has the correct
			// values
			EventLabeler<PersonAdditionEvent> eventLabeler = PersonAdditionEvent.getEventLabeler();
			assertEquals(PersonAdditionEvent.class, eventLabeler.getEventClass());

			assertEquals(PersonAdditionEvent.getEventLabel().getLabelerId(), eventLabeler.getId());

			// show that the event labeler produces the expected event
			// label

			// create an event			
			PersonAdditionEvent event = new PersonAdditionEvent(new PersonId(0), PersonConstructionData.builder().build());

			// derive the expected event label for this event
			EventLabel<PersonAdditionEvent> expectedEventLabel = PersonAdditionEvent.getEventLabel();

			// have the event labeler produce an event label and show it
			// is equal to the expected event label
			EventLabel<PersonAdditionEvent> actualEventLabel = eventLabeler.getEventLabel(c, event);
			assertEquals(expectedEventLabel, actualEventLabel);

		});
	}
}
