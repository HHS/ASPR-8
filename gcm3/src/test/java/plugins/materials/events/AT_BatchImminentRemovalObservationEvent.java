package plugins.materials.events;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import plugins.materials.support.BatchId;
import tools.annotations.UnitTest;
import tools.annotations.UnitTestConstructor;
import tools.annotations.UnitTestMethod;

@UnitTest(target = BatchImminentRemovalObservationEvent.class)
public class AT_BatchImminentRemovalObservationEvent {

	@Test
	@UnitTestConstructor(args = { BatchId.class })
	public void testConstructor() {
		// nothing to test
	}

	@Test
	@UnitTestMethod(name = "getPrimaryKeyValue", args = {})
	public void testGetPrimaryKeyValue() {
		BatchId batchId = new BatchId(7867);
		BatchImminentRemovalObservationEvent batchImminentRemovalObservationEvent = new BatchImminentRemovalObservationEvent(batchId);
		assertEquals(BatchImminentRemovalObservationEvent.class, batchImminentRemovalObservationEvent.getPrimaryKeyValue());
	}

	@Test
	@UnitTestMethod(name = "getBatchId", args = {})
	public void testGetBatchId() {
		BatchId batchId = new BatchId(7867);
		BatchImminentRemovalObservationEvent batchImminentRemovalObservationEvent = new BatchImminentRemovalObservationEvent(batchId);
		assertEquals(batchId, batchImminentRemovalObservationEvent.getBatchId());
	}

	@Test
	@UnitTestMethod(name = "toString", args = {})
	public void testToString() {
		BatchId batchId = new BatchId(7867);
		BatchImminentRemovalObservationEvent batchImminentRemovalObservationEvent = new BatchImminentRemovalObservationEvent(batchId);
		assertEquals("BatchImminentRemovalObservationEvent [batchId=7867]", batchImminentRemovalObservationEvent.toString());
	}

}
