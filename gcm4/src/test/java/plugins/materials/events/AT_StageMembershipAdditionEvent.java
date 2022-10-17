package plugins.materials.events;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import plugins.materials.support.BatchId;
import plugins.materials.support.StageId;
import tools.annotations.UnitTest;
import tools.annotations.UnitTestConstructor;
import tools.annotations.UnitTestMethod;

@UnitTest(target = StageMembershipAdditionEvent.class)
public class AT_StageMembershipAdditionEvent {
	

	@Test
	@UnitTestConstructor(args = {BatchId.class, StageId.class})
	public void testConstructor() {
		//nothing to test
	}

	@Test
	@UnitTestMethod(name="getBatchId",args = {})
	public void testGetBatchId() {
		BatchId batchId = new BatchId(6);
		StageId stageId = new StageId(252);
		StageMembershipAdditionEvent stageMembershipAdditionEvent = new StageMembershipAdditionEvent(batchId, stageId);
		assertEquals(batchId, stageMembershipAdditionEvent.getBatchId());
	}

	@Test
	@UnitTestMethod(name="getStageId",args = {})
	public void testGetStageId() {
		BatchId batchId = new BatchId(6);
		StageId stageId = new StageId(252);
		StageMembershipAdditionEvent stageMembershipAdditionEvent = new StageMembershipAdditionEvent(batchId, stageId);
		assertEquals(stageId, stageMembershipAdditionEvent.getStageId());
	}

	@Test
	@UnitTestMethod(name="toString",args = {})
	public void testToString() {
		BatchId batchId = new BatchId(6);
		StageId stageId = new StageId(252);
		StageMembershipAdditionEvent stageMembershipAdditionEvent = new StageMembershipAdditionEvent(batchId, stageId);
		assertEquals("StageMembershipAdditionEvent [batchId=6, stageId=252]", stageMembershipAdditionEvent.toString());
	}

}
