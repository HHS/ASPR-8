package plugins.materials.events;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import plugins.materials.support.BatchId;
import plugins.materials.support.StageId;
import tools.annotations.UnitTest;
import tools.annotations.UnitTestConstructor;
import tools.annotations.UnitTestMethod;

@UnitTest(target = StageMembershipRemovalEvent.class)
public class AT_StageMembershipRemovalEvent {
	
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
		StageMembershipRemovalEvent stageMembershipRemovalEvent = new StageMembershipRemovalEvent(batchId, stageId);
		assertEquals(StageMembershipRemovalEvent.class,stageMembershipRemovalEvent.getPrimaryKeyValue());
	}
	
	@Test
	@UnitTestMethod(name="getBatchId",args = {})
	public void testGetBatchId() {
		BatchId batchId = new BatchId(23);
		StageId stageId = new StageId(765);
		StageMembershipRemovalEvent stageMembershipRemovalEvent = new StageMembershipRemovalEvent(batchId, stageId);
		assertEquals(batchId,stageMembershipRemovalEvent.getBatchId());
	}

	@Test
	@UnitTestMethod(name="getStageId",args = {})
	public void testGetStageId() {
		BatchId batchId = new BatchId(23);
		StageId stageId = new StageId(765);
		StageMembershipRemovalEvent stageMembershipRemovalEvent = new StageMembershipRemovalEvent(batchId, stageId);
		assertEquals(stageId,stageMembershipRemovalEvent.getStageId());
	}

	@Test
	@UnitTestMethod(name="toString",args = {})
	public void testToString() {
		BatchId batchId = new BatchId(23);
		StageId stageId = new StageId(765);
		StageMembershipRemovalEvent stageMembershipRemovalEvent = new StageMembershipRemovalEvent(batchId, stageId);
		assertEquals("StageMembershipRemovalEvent [batchId=23, stageId=765]",stageMembershipRemovalEvent.toString());
	}
}
