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