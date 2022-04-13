package plugins.materials.events;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import plugins.materials.support.BatchId;
import plugins.materials.support.StageId;
import tools.annotations.UnitTest;
import tools.annotations.UnitTestConstructor;
import tools.annotations.UnitTestMethod;

@UnitTest(target = StageMembershipRemovalObservationEvent.class)
public class AT_StageMembershipRemovalObservationEvent {
	
	@Test
	@UnitTestConstructor(args = {BatchId.class, StageId.class})
	public void testConstructor() {
		//nothing to test
	}
	
	@Test
	@UnitTestMethod(name="getPrimaryKeyValue",args = {})
	public void testGetPrimaryKeyValue() {
		BatchId batchId = new BatchId(23);
		StageId stageId = new StageId(765);
		StageMembershipRemovalObservationEvent stageMembershipRemovalObservationEvent = new StageMembershipRemovalObservationEvent(batchId, stageId);
		assertEquals(StageMembershipRemovalObservationEvent.class,stageMembershipRemovalObservationEvent.getPrimaryKeyValue());
	}
	
	@Test
	@UnitTestMethod(name="getBatchId",args = {})
	public void testGetBatchId() {
		BatchId batchId = new BatchId(23);
		StageId stageId = new StageId(765);
		StageMembershipRemovalObservationEvent stageMembershipRemovalObservationEvent = new StageMembershipRemovalObservationEvent(batchId, stageId);
		assertEquals(batchId,stageMembershipRemovalObservationEvent.getBatchId());
	}

	@Test
	@UnitTestMethod(name="getStageId",args = {})
	public void testGetStageId() {
		BatchId batchId = new BatchId(23);
		StageId stageId = new StageId(765);
		StageMembershipRemovalObservationEvent stageMembershipRemovalObservationEvent = new StageMembershipRemovalObservationEvent(batchId, stageId);
		assertEquals(stageId,stageMembershipRemovalObservationEvent.getStageId());
	}

	@Test
	@UnitTestMethod(name="toString",args = {})
	public void testToString() {
		BatchId batchId = new BatchId(23);
		StageId stageId = new StageId(765);
		StageMembershipRemovalObservationEvent stageMembershipRemovalObservationEvent = new StageMembershipRemovalObservationEvent(batchId, stageId);
		assertEquals("StageMembershipRemovalObservationEvent [batchId=23, stageId=765]",stageMembershipRemovalObservationEvent.toString());
	}
}
