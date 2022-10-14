package plugins.materials.events;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import plugins.materials.support.MaterialsProducerId;
import plugins.materials.support.StageId;
import plugins.materials.testsupport.TestMaterialsProducerId;
import tools.annotations.UnitTest;
import tools.annotations.UnitTestConstructor;
import tools.annotations.UnitTestMethod;

@UnitTest(target = StageMaterialsProducerUpdateEvent.class)
public class AT_StageMaterialsProducerUpdateEvent {

	@Test
	@UnitTestConstructor(args = { StageId.class, MaterialsProducerId.class, MaterialsProducerId.class })
	public void testConstructor() {
		// nothing to test
	}

	@Test
	@UnitTestMethod(name = "getPrimaryKeyValue", args = {})
	public void testGetPrimaryKeyValue() {
		StageId stageId = new StageId(344);
		MaterialsProducerId previousMaterialsProducerId = TestMaterialsProducerId.MATERIALS_PRODUCER_1;
		MaterialsProducerId currentMaterialsProducerId = TestMaterialsProducerId.MATERIALS_PRODUCER_2;
		StageMaterialsProducerUpdateEvent stageMaterialsProducerUpdateEvent = new StageMaterialsProducerUpdateEvent(stageId, previousMaterialsProducerId,
				currentMaterialsProducerId);
		assertEquals(StageMaterialsProducerUpdateEvent.class, stageMaterialsProducerUpdateEvent.getPrimaryKeyValue());
	}

	@Test
	@UnitTestMethod(name = "getStageId", args = {})
	public void testGetStageId() {
		StageId stageId = new StageId(344);
		MaterialsProducerId previousMaterialsProducerId = TestMaterialsProducerId.MATERIALS_PRODUCER_1;
		MaterialsProducerId currentMaterialsProducerId = TestMaterialsProducerId.MATERIALS_PRODUCER_2;
		StageMaterialsProducerUpdateEvent stageMaterialsProducerUpdateEvent = new StageMaterialsProducerUpdateEvent(stageId, previousMaterialsProducerId,
				currentMaterialsProducerId);
		assertEquals(stageId, stageMaterialsProducerUpdateEvent.getStageId());
	}

	@Test
	@UnitTestMethod(name = "getPreviousMaterialsProducerId", args = {})
	public void testGetPreviousMaterialsProducerId() {
		StageId stageId = new StageId(344);
		MaterialsProducerId previousMaterialsProducerId = TestMaterialsProducerId.MATERIALS_PRODUCER_1;
		MaterialsProducerId currentMaterialsProducerId = TestMaterialsProducerId.MATERIALS_PRODUCER_2;
		StageMaterialsProducerUpdateEvent stageMaterialsProducerUpdateEvent = new StageMaterialsProducerUpdateEvent(stageId, previousMaterialsProducerId,
				currentMaterialsProducerId);
		assertEquals(previousMaterialsProducerId, stageMaterialsProducerUpdateEvent.getPreviousMaterialsProducerId());
	}

	@Test
	@UnitTestMethod(name = "getCurrentMaterialsProducerId", args = {})
	public void testGetCurrentMaterialsProducerId() {
		StageId stageId = new StageId(344);
		MaterialsProducerId previousMaterialsProducerId = TestMaterialsProducerId.MATERIALS_PRODUCER_1;
		MaterialsProducerId currentMaterialsProducerId = TestMaterialsProducerId.MATERIALS_PRODUCER_2;
		StageMaterialsProducerUpdateEvent stageMaterialsProducerUpdateEvent = new StageMaterialsProducerUpdateEvent(stageId, previousMaterialsProducerId,
				currentMaterialsProducerId);
		assertEquals(currentMaterialsProducerId, stageMaterialsProducerUpdateEvent.getCurrentMaterialsProducerId());
	}

	@Test
	@UnitTestMethod(name = "toString", args = {})
	public void testToString() {
		StageId stageId = new StageId(344);
		MaterialsProducerId previousMaterialsProducerId = TestMaterialsProducerId.MATERIALS_PRODUCER_1;
		MaterialsProducerId currentMaterialsProducerId = TestMaterialsProducerId.MATERIALS_PRODUCER_2;
		StageMaterialsProducerUpdateEvent stageMaterialsProducerUpdateEvent = new StageMaterialsProducerUpdateEvent(stageId, previousMaterialsProducerId,
				currentMaterialsProducerId);
		String expectedValue = "StageMaterialsProducerUpdateEvent [stageId=344, previousMaterialsProducerId=MATERIALS_PRODUCER_1, currentMaterialsProducerId=MATERIALS_PRODUCER_2]";
		String actualValue = stageMaterialsProducerUpdateEvent.toString();
		assertEquals(expectedValue, actualValue);
	}
}