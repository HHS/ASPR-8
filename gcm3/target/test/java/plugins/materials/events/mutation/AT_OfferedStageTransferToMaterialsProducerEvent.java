package plugins.materials.events.mutation;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import plugins.materials.support.MaterialsProducerId;
import plugins.materials.support.StageId;
import plugins.materials.testsupport.TestMaterialsProducerId;
import util.annotations.UnitTest;
import util.annotations.UnitTestConstructor;
import util.annotations.UnitTestMethod;


@UnitTest(target = OfferedStageTransferToMaterialsProducerEvent.class)
public final class AT_OfferedStageTransferToMaterialsProducerEvent {

	
	@Test
	@UnitTestConstructor(args = { StageId.class, MaterialsProducerId.class})	
	public void testConstructor() {
		//nothing to test
	}
	
	@Test
	@UnitTestMethod(name = "getPrimaryKeyValue",args = { })	
	public void testGetPrimaryKeyValue() {
		StageId stageId = new StageId(5456);
		MaterialsProducerId materialsProducerId = TestMaterialsProducerId.MATERIALS_PRODUCER_3;
		OfferedStageTransferToMaterialsProducerEvent offeredStageTransferToMaterialsProducerEvent = new OfferedStageTransferToMaterialsProducerEvent(stageId,materialsProducerId);
		assertEquals(OfferedStageTransferToMaterialsProducerEvent.class, offeredStageTransferToMaterialsProducerEvent.getPrimaryKeyValue());		
	}

	@Test
	@UnitTestMethod(name = "getStageId",args = { })	
	public void testGetStageId() {
		StageId stageId = new StageId(5456);
		MaterialsProducerId materialsProducerId = TestMaterialsProducerId.MATERIALS_PRODUCER_3;
		OfferedStageTransferToMaterialsProducerEvent offeredStageTransferToMaterialsProducerEvent = new OfferedStageTransferToMaterialsProducerEvent(stageId,materialsProducerId);
		assertEquals(stageId, offeredStageTransferToMaterialsProducerEvent.getStageId());
	}

	@Test
	@UnitTestMethod(name = "getMaterialsProducerId",args = {  })	
	public void testGetMaterialsProducerId() {
		StageId stageId = new StageId(5456);
		MaterialsProducerId materialsProducerId = TestMaterialsProducerId.MATERIALS_PRODUCER_3;
		OfferedStageTransferToMaterialsProducerEvent offeredStageTransferToMaterialsProducerEvent = new OfferedStageTransferToMaterialsProducerEvent(stageId,materialsProducerId);
		assertEquals(materialsProducerId, offeredStageTransferToMaterialsProducerEvent.getMaterialsProducerId());
	}

}
