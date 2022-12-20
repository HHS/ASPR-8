package plugins.materials.events;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import plugins.materials.support.BatchId;
import tools.annotations.UnitTest;
import tools.annotations.UnitTestConstructor;
import tools.annotations.UnitTestMethod;

@UnitTest(target = BatchImminentRemovalEvent.class)
public class AT_BatchImminentRemovalEvent {

	@Test
	@UnitTestConstructor(args = { BatchId.class })
	public void testConstructor() {
		// nothing to test
	}

	@Test
	@UnitTestMethod(name = "getBatchId", args = {})
	public void testGetBatchId() {
		BatchId batchId = new BatchId(7867);
		BatchImminentRemovalEvent batchImminentRemovalEvent = new BatchImminentRemovalEvent(batchId);
		assertEquals(batchId, batchImminentRemovalEvent.batchId());
	}

	@Test
	@UnitTestMethod(name = "toString", args = {})
	public void testToString() {
		BatchId batchId = new BatchId(7867);
		BatchImminentRemovalEvent batchImminentRemovalEvent = new BatchImminentRemovalEvent(batchId);
		assertEquals("BatchImminentRemovalEvent [batchId=7867]", batchImminentRemovalEvent.toString());
	}

}
