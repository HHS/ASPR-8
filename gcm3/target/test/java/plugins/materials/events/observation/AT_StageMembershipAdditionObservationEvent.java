package plugins.materials.events.observation;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import plugins.materials.support.BatchId;
import plugins.materials.support.StageId;
import util.annotations.UnitTest;
import util.annotations.UnitTestConstructor;
import util.annotations.UnitTestMethod;

@UnitTest(target = StageMembershipAdditionObservationEvent.class)
public class AT_StageMembershipAdditionObservationEvent {
	

	@Test
	@UnitTestConstructor(args = {BatchId.class, StageId.class})
	public void testConstructor() {
		//nothing to test
	}
	
	@Test
	@UnitTestMethod(name="getPrimaryKeyValue",args = {})
	public void testGetPrimaryKeyValue() {
		BatchId batchId = new BatchId(6);
		StageId stageId = new StageId(252);
		StageMembershipAdditionObservationEvent stageMembershipAdditionObservationEvent = new StageMembershipAdditionObservationEvent(batchId, stageId);
		assertEquals(StageMembershipAdditionObservationEvent.class, stageMembershipAdditionObservationEvent.getPrimaryKeyValue());
	}

	@Test
	@UnitTestMethod(name="getBatchId",args = {})
	public void testGetBatchId() {
		BatchId batchId = new BatchId(6);
		StageId stageId = new StageId(252);
		StageMembershipAdditionObservationEvent stageMembershipAdditionObservationEvent = new StageMembershipAdditionObservationEvent(batchId, stageId);
		assertEquals(batchId, stageMembershipAdditionObservationEvent.getBatchId());
	}

	@Test
	@UnitTestMethod(name="getStageId",args = {})
	public void testGetStageId() {
		BatchId batchId = new BatchId(6);
		StageId stageId = new StageId(252);
		StageMembershipAdditionObservationEvent stageMembershipAdditionObservationEvent = new StageMembershipAdditionObservationEvent(batchId, stageId);
		assertEquals(stageId, stageMembershipAdditionObservationEvent.getStageId());
	}

	@Test
	@UnitTestMethod(name="toString",args = {})
	public void testToString() {
		BatchId batchId = new BatchId(6);
		StageId stageId = new StageId(252);
		StageMembershipAdditionObservationEvent stageMembershipAdditionObservationEvent = new StageMembershipAdditionObservationEvent(batchId, stageId);
		assertEquals("StageMembershipAdditionObservationEvent [batchId=6, stageId=252]", stageMembershipAdditionObservationEvent.toString());
	}

}
