package plugins.materials.events.mutation;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import plugins.materials.support.BatchId;
import plugins.materials.support.StageId;
import util.annotations.UnitTest;
import util.annotations.UnitTestConstructor;
import util.annotations.UnitTestMethod;

@UnitTest(target = MoveBatchToStageEvent.class)
public final class AT_MoveBatchToStageEvent {

	@Test
	@UnitTestConstructor(args = { BatchId.class, StageId.class })
	public void testConstructor() {
		// nothing to test
	}

	@Test
	@UnitTestMethod(name = "getPrimaryKeyValue", args = {})
	public void testGetPrimaryKeyValue() {
		BatchId batchId = new BatchId(75);
		StageId stageId = new StageId(764576);
		MoveBatchToStageEvent moveBatchToStageEvent = new MoveBatchToStageEvent(batchId, stageId);
		assertEquals(MoveBatchToStageEvent.class, moveBatchToStageEvent.getPrimaryKeyValue());
	}

	@Test
	@UnitTestMethod(name = "getBatchId", args = {})
	public void testGetBatchId() {
		BatchId batchId = new BatchId(75);
		StageId stageId = new StageId(764576);
		MoveBatchToStageEvent moveBatchToStageEvent = new MoveBatchToStageEvent(batchId, stageId);
		assertEquals(batchId, moveBatchToStageEvent.getBatchId());
	}

	@Test
	@UnitTestMethod(name = "getStageId", args = {})
	public void testGetStageId() {
		BatchId batchId = new BatchId(75);
		StageId stageId = new StageId(764576);
		MoveBatchToStageEvent moveBatchToStageEvent = new MoveBatchToStageEvent(batchId, stageId);
		assertEquals(stageId, moveBatchToStageEvent.getStageId());
	}

}
