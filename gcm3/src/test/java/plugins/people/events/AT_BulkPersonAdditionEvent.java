package plugins.people.events;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import nucleus.Event;
import plugins.people.support.BulkPersonConstructionData;
import plugins.people.support.PersonError;
import plugins.people.support.PersonId;
import tools.annotations.UnitTest;
import tools.annotations.UnitTestConstructor;
import tools.annotations.UnitTestMethod;
import util.errors.ContractException;

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

	


}
