package plugins.materials.events.mutation;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import plugins.materials.support.BatchConstructionInfo;
import plugins.materials.support.BatchId;
import util.annotations.UnitTest;
import util.annotations.UnitTestConstructor;
import util.annotations.UnitTestMethod;


@UnitTest(target = BatchRemovalRequestEvent.class)
public final class AT_BatchRemovalRequestEvent {
	

	@Test
	@UnitTestConstructor(args = { BatchId.class })	
	public void testConstructor() {
		//nothing to test
	}

	@Test
	@UnitTestMethod(name = "getBatchId",args = {  })	
	public void testGetBatchId() {
		BatchId batchId = new BatchId(2456);
		BatchRemovalRequestEvent batchRemovalRequestEvent = new BatchRemovalRequestEvent(batchId);
		assertEquals(batchId, batchRemovalRequestEvent.getBatchId());
	}
	
	@Test
	@UnitTestMethod(name = "getPrimaryKeyValue",args = { BatchConstructionInfo.class })	
	public void testGetPrimaryKeyValue() {
		BatchId batchId = new BatchId(2456);
		BatchRemovalRequestEvent batchRemovalRequestEvent = new BatchRemovalRequestEvent(batchId);
		assertEquals(BatchRemovalRequestEvent.class, batchRemovalRequestEvent.getPrimaryKeyValue());
	}

}
