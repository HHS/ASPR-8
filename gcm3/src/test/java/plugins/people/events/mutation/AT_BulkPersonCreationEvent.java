package plugins.people.events.mutation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import nucleus.Event;
import plugins.people.support.BulkPersonContructionData;
import plugins.people.support.PersonError;
import util.ContractException;
import util.annotations.UnitTest;
import util.annotations.UnitTestConstructor;
import util.annotations.UnitTestMethod;

@UnitTest(target = BulkPersonCreationEvent.class)
public final class AT_BulkPersonCreationEvent implements Event {

	@Test
	@UnitTestConstructor(args = { BulkPersonContructionData.class })
	public void testConstructor() {
		// precondition tests
		ContractException contractException = assertThrows(ContractException.class, () -> new BulkPersonCreationEvent(null));
		assertEquals(PersonError.NULL_BULK_PERSON_CONTRUCTION_DATA, contractException.getErrorType());
	}

	@Test
	@UnitTestMethod(name = "getBulkPersonContructionData", args = {})
	public void testGetBulkPersonContructionData() {
		BulkPersonContructionData bulkPersonContructionData = BulkPersonContructionData.builder().build();
		BulkPersonCreationEvent bulkPersonCreationEvent = new BulkPersonCreationEvent(bulkPersonContructionData);
		assertEquals(bulkPersonContructionData, bulkPersonCreationEvent.getBulkPersonContructionData());
	}

}
