package plugins.materials.events.mutation;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import plugins.materials.support.BatchId;
import util.annotations.UnitTest;
import util.annotations.UnitTestConstructor;
import util.annotations.UnitTestMethod;


@UnitTest(target = MoveBatchToInventoryEvent.class)
public final class AT_MoveBatchToInventoryEvent {

	
	@Test
	@UnitTestConstructor(args = { BatchId.class})	
	public void testConstructor() {
		//nothing to test
		
	}
	
	@Test
	@UnitTestMethod(name = "getPrimaryKeyValue",args = {  })	
	public void testGetPrimaryKeyValue() {
		BatchId batchId = new BatchId(3523);
		MoveBatchToInventoryEvent moveBatchToInventoryEvent = new MoveBatchToInventoryEvent(batchId);
		assertEquals(MoveBatchToInventoryEvent.class,moveBatchToInventoryEvent.getPrimaryKeyValue());
	}

	@Test
	@UnitTestMethod(name = "getBatchId",args = { })	
	public void testGetBatchId() {
		BatchId batchId = new BatchId(3523);
		MoveBatchToInventoryEvent moveBatchToInventoryEvent = new MoveBatchToInventoryEvent(batchId);
		assertEquals(batchId,moveBatchToInventoryEvent.getBatchId());

	}

}
