package plugins.materials.events;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import plugins.materials.support.BatchId;
import tools.annotations.UnitTest;
import tools.annotations.UnitTestConstructor;
import tools.annotations.UnitTestMethod;

@UnitTest(target = BatchAdditionEvent.class)
public class AT_BatchAdditionEvent {

	@Test
	@UnitTestConstructor(args = { BatchId.class })
	public void testConstructor() {
		// nothing to test
	}

	@Test
	@UnitTestMethod(name = "getBatchId", args = {})
	public void testGetBatchId() {
		BatchId batchId = new BatchId(56456);
		BatchAdditionEvent batchAdditionEvent = new BatchAdditionEvent(batchId);
		assertEquals(batchId, batchAdditionEvent.getBatchId());
	}

	@Test
	@UnitTestMethod(name = "toString", args = {})
	public void testToString() {
		BatchId batchId = new BatchId(56456);
		BatchAdditionEvent batchAdditionEvent = new BatchAdditionEvent(batchId);
		assertEquals("BatchAdditionEvent [batchId=56456]", batchAdditionEvent.toString());
	}

}
