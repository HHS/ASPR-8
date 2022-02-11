package plugins.materials.events.mutation;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import plugins.materials.support.BatchConstructionInfo;
import plugins.materials.support.BatchId;
import util.annotations.UnitTest;
import util.annotations.UnitTestConstructor;
import util.annotations.UnitTestMethod;

@UnitTest(target = BatchContentShiftEvent.class)
public final class AT_BatchContentShiftEvent {

	@Test
	@UnitTestConstructor(args = { BatchId.class, BatchId.class, double.class })
	public void testConstructor() {
		// nothing to test
	}

	@Test
	@UnitTestMethod(name = "getSourceBatchId", args = {})
	public void testGetSourceBatchId() {
		BatchId sourceBatchId = new BatchId(0);
		BatchId destinationBatchId = new BatchId(1);
		double amount = 234.345;
		BatchContentShiftEvent batchContentShiftEvent = new BatchContentShiftEvent(sourceBatchId, destinationBatchId, amount);
		assertEquals(sourceBatchId, batchContentShiftEvent.getSourceBatchId());
	}

	@Test
	@UnitTestMethod(name = "getDestinationBatchId", args = {})
	public void testGetDestinationBatchId() {
		BatchId sourceBatchId = new BatchId(0);
		BatchId destinationBatchId = new BatchId(1);
		double amount = 234.345;
		BatchContentShiftEvent batchContentShiftEvent = new BatchContentShiftEvent(sourceBatchId, destinationBatchId, amount);
		assertEquals(destinationBatchId, batchContentShiftEvent.getDestinationBatchId());
	}

	@Test
	@UnitTestMethod(name = "getAmount", args = {})
	public void testGetAmount() {
		BatchId sourceBatchId = new BatchId(0);
		BatchId destinationBatchId = new BatchId(1);
		double amount = 234.345;
		BatchContentShiftEvent batchContentShiftEvent = new BatchContentShiftEvent(sourceBatchId, destinationBatchId, amount);
		assertEquals(amount, batchContentShiftEvent.getAmount());
	}

	@Test
	@UnitTestMethod(name = "getPrimaryKeyValue", args = { BatchConstructionInfo.class })
	public void testGetPrimaryKeyValue() {
		BatchId sourceBatchId = new BatchId(0);
		BatchId destinationBatchId = new BatchId(1);
		double amount = 234.345;
		BatchContentShiftEvent batchContentShiftEvent = new BatchContentShiftEvent(sourceBatchId, destinationBatchId, amount);
		assertEquals(BatchContentShiftEvent.class, batchContentShiftEvent.getPrimaryKeyValue());
	}

}
