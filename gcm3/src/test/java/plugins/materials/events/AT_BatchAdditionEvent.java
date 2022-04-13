package plugins.materials.events;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import plugins.materials.support.BatchId;
import tools.annotations.UnitTest;
import tools.annotations.UnitTestConstructor;
import tools.annotations.UnitTestMethod;

@UnitTest(target = BatchCreationObservationEvent.class)
public class AT_BatchCreationObservationEvent {

	@Test
	@UnitTestConstructor(args = { BatchId.class })
	public void testConstructor() {
		// nothing to test
	}

	@Test
	@UnitTestMethod(name = "getPrimaryKeyValue", args = {})
	public void testGetPrimaryKeyValue() {
		BatchId batchId = new BatchId(56456);
		BatchCreationObservationEvent batchCreationObservationEvent = new BatchCreationObservationEvent(batchId);
		assertEquals(BatchCreationObservationEvent.class, batchCreationObservationEvent.getPrimaryKeyValue());
	}

	@Test
	@UnitTestMethod(name = "getBatchId", args = {})
	public void testGetBatchId() {
		BatchId batchId = new BatchId(56456);
		BatchCreationObservationEvent batchCreationObservationEvent = new BatchCreationObservationEvent(batchId);
		assertEquals(batchId, batchCreationObservationEvent.getBatchId());
	}

	@Test
	@UnitTestMethod(name = "toString", args = {})
	public void testToString() {
		BatchId batchId = new BatchId(56456);
		BatchCreationObservationEvent batchCreationObservationEvent = new BatchCreationObservationEvent(batchId);
		assertEquals("BatchCreationObservationEvent [batchId=56456]", batchCreationObservationEvent.toString());
	}

}
