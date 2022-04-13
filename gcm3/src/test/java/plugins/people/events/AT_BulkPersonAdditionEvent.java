package plugins.people.events;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import nucleus.Event;
import nucleus.EventLabel;
import nucleus.EventLabeler;
import nucleus.util.ContractException;
import plugins.people.support.BulkPersonConstructionData;
import plugins.people.support.PersonError;
import plugins.people.support.PersonId;
import plugins.people.testsupport.PeopleActionSupport;
import tools.annotations.UnitTest;
import tools.annotations.UnitTestConstructor;
import tools.annotations.UnitTestMethod;

@UnitTest(target = BulkPersonAdditionEvent.class)
public class AT_BulkPersonAdditionEvent implements Event {

	@Test
	@UnitTestConstructor(args = { PersonId.class, BulkPersonConstructionData.class })
	public void testConstruction() {

		// precondition tests
		PersonId personId = new PersonId(0);
		BulkPersonConstructionData bulkPersonConstructionData = BulkPersonConstructionData.builder().build();

		ContractException contractException = assertThrows(ContractException.class, () -> new BulkPersonAdditionEvent(null, bulkPersonConstructionData));
		assertEquals(PersonError.NULL_PERSON_ID, contractException.getErrorType());

		contractException = assertThrows(ContractException.class, () -> new BulkPersonAdditionEvent(personId, null));
		assertEquals(PersonError.NULL_BULK_PERSON_CONSTRUCTION_DATA, contractException.getErrorType());

	}

	@Test
	@UnitTestMethod(name = "getPersonId", args = {})
	public void testGetPersonId() {

		BulkPersonConstructionData bulkPersonConstructionData = BulkPersonConstructionData.builder().build();

		for (int i = 0; i < 10; i++) {
			PersonId personId = new PersonId(i);
			BulkPersonAdditionEvent bulkPersonAdditionEvent = new BulkPersonAdditionEvent(personId, bulkPersonConstructionData);
			assertEquals(personId, bulkPersonAdditionEvent.getPersonId());
		}

	}

	@Test
	@UnitTestMethod(name = "getBulkPersonConstructionData", args = {})
	public void testGetBulkPersonConstructionData() {
		PersonId personId = new PersonId(45);
		BulkPersonConstructionData bulkPersonConstructionData = BulkPersonConstructionData.builder().build();
		BulkPersonAdditionEvent bulkPersonAdditionEvent = new BulkPersonAdditionEvent(personId, bulkPersonConstructionData);
		assertEquals(bulkPersonConstructionData, bulkPersonAdditionEvent.getBulkPersonConstructionData());
	}

	

	@Test
	@UnitTestMethod(name = "getEventLabel", args = {})
	public void testGetEventLabel() {
		EventLabel<BulkPersonAdditionEvent> eventLabel = BulkPersonAdditionEvent.getEventLabel();
		assertEquals(BulkPersonAdditionEvent.class, eventLabel.getEventClass());
		assertEquals(BulkPersonAdditionEvent.class, eventLabel.getPrimaryKeyValue());
		assertEquals(BulkPersonAdditionEvent.getEventLabeler().getId(), eventLabel.getLabelerId());
	}

	@Test
	@UnitTestMethod(name = "getEventLabeler", args = {})
	public void testGetEventLabeler() {
		PeopleActionSupport.testConsumer(0,(c) -> {
			// show that the event labeler can be constructed has the correct
			// values
			EventLabeler<BulkPersonAdditionEvent> eventLabeler = BulkPersonAdditionEvent.getEventLabeler();
			assertEquals(BulkPersonAdditionEvent.class, eventLabeler.getEventClass());

			assertEquals(BulkPersonAdditionEvent.getEventLabel().getLabelerId(), eventLabeler.getId());

			// show that the event labeler produces the expected event
			// label

			// create an event			
			BulkPersonAdditionEvent event = new BulkPersonAdditionEvent(new PersonId(0), BulkPersonConstructionData.builder().build());

			// derive the expected event label for this event
			EventLabel<BulkPersonAdditionEvent> expectedEventLabel = BulkPersonAdditionEvent.getEventLabel();

			// have the event labeler produce an event label and show it
			// is equal to the expected event label
			EventLabel<BulkPersonAdditionEvent> actualEventLabel = eventLabeler.getEventLabel(c, event);
			assertEquals(expectedEventLabel, actualEventLabel);

		});
	}
}
