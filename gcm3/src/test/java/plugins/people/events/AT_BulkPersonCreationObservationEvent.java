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
import plugins.people.support.BulkPersonConstructionData;
import plugins.people.support.PersonError;
import plugins.people.support.PersonId;
import plugins.people.testsupport.PeopleActionSupport;

@UnitTest(target = BulkPersonCreationObservationEvent.class)
public class AT_BulkPersonCreationObservationEvent implements Event {

	@Test
	@UnitTestConstructor(args = { PersonId.class, BulkPersonConstructionData.class })
	public void testConstruction() {

		// precondition tests
		PersonId personId = new PersonId(0);
		BulkPersonConstructionData bulkPersonConstructionData = BulkPersonConstructionData.builder().build();

		ContractException contractException = assertThrows(ContractException.class, () -> new BulkPersonCreationObservationEvent(null, bulkPersonConstructionData));
		assertEquals(PersonError.NULL_PERSON_ID, contractException.getErrorType());

		contractException = assertThrows(ContractException.class, () -> new BulkPersonCreationObservationEvent(personId, null));
		assertEquals(PersonError.NULL_BULK_PERSON_CONTRUCTION_DATA, contractException.getErrorType());

	}

	@Test
	@UnitTestMethod(name = "getPersonId", args = {})
	public void testGetPersonId() {

		BulkPersonConstructionData bulkPersonConstructionData = BulkPersonConstructionData.builder().build();

		for (int i = 0; i < 10; i++) {
			PersonId personId = new PersonId(i);
			BulkPersonCreationObservationEvent bulkPersonCreationObservationEvent = new BulkPersonCreationObservationEvent(personId, bulkPersonConstructionData);
			assertEquals(personId, bulkPersonCreationObservationEvent.getPersonId());
		}

	}

	@Test
	@UnitTestMethod(name = "getBulkPersonContructionData", args = {})
	public void testGetBulkPersonContructionData() {
		PersonId personId = new PersonId(45);
		BulkPersonConstructionData bulkPersonConstructionData = BulkPersonConstructionData.builder().build();
		BulkPersonCreationObservationEvent bulkPersonCreationObservationEvent = new BulkPersonCreationObservationEvent(personId, bulkPersonConstructionData);
		assertEquals(bulkPersonConstructionData, bulkPersonCreationObservationEvent.getBulkPersonContructionData());
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
		PeopleActionSupport.testConsumer((c) -> {
			// show that the event labeler can be constructed has the correct
			// values
			EventLabeler<BulkPersonCreationObservationEvent> eventLabeler = BulkPersonCreationObservationEvent.getEventLabeler();
			assertEquals(BulkPersonCreationObservationEvent.class, eventLabeler.getEventClass());

			assertEquals(BulkPersonCreationObservationEvent.getEventLabel().getLabelerId(), eventLabeler.getId());

			// show that the event labeler produces the expected event
			// label

			// create an event			
			BulkPersonCreationObservationEvent event = new BulkPersonCreationObservationEvent(new PersonId(0), BulkPersonConstructionData.builder().build());

			// derive the expected event label for this event
			EventLabel<BulkPersonCreationObservationEvent> expectedEventLabel = BulkPersonCreationObservationEvent.getEventLabel();

			// have the event labeler produce an event label and show it
			// is equal to the expected event label
			EventLabel<BulkPersonCreationObservationEvent> actualEventLabel = eventLabeler.getEventLabel(c, event);
			assertEquals(expectedEventLabel, actualEventLabel);

		});
	}
}
