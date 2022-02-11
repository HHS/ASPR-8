package plugins.materials.events.mutation;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import plugins.materials.support.BatchConstructionInfo;
import plugins.materials.support.MaterialId;
import plugins.materials.support.StageId;
import plugins.materials.testsupport.TestMaterialId;
import util.annotations.UnitTest;
import util.annotations.UnitTestConstructor;
import util.annotations.UnitTestMethod;

/**
 * Converts a stage to a batch that will be held in the inventory of the
 * invoking materials producer. The stage and its associated batches are
 * destroyed. The stage must be owned by the invoking materials producer and
 * must not be in the offered state.
 * 
 * @author Shawn Hatch
 *
 */
@UnitTest(target = StageToBatchConversionEvent.class)
public final class AT_StageToBatchConversionEvent {
	
	@Test
	@UnitTestConstructor(args = { StageId.class, MaterialId.class, double.class })
	public void testConstructor() {
		//nothing to test
	}

	@Test
	@UnitTestMethod(name = "getPrimaryKeyValue", args = { BatchConstructionInfo.class })
	public void testGetPrimaryKeyValue() {
		StageId stageId = new StageId(35645);
		MaterialId materialId = TestMaterialId.MATERIAL_2;
		double amount = 23453.45;
		StageToBatchConversionEvent stageToBatchConversionEvent = new StageToBatchConversionEvent(stageId,materialId,amount);
		assertEquals(StageToBatchConversionEvent.class,stageToBatchConversionEvent.getPrimaryKeyValue());
	}

	@Test
	@UnitTestMethod(name = "getStageId", args = { BatchConstructionInfo.class })
	public void testGetStageId() {
		StageId stageId = new StageId(35645);
		MaterialId materialId = TestMaterialId.MATERIAL_2;
		double amount = 23453.45;
		StageToBatchConversionEvent stageToBatchConversionEvent = new StageToBatchConversionEvent(stageId,materialId,amount);
		assertEquals(stageId,stageToBatchConversionEvent.getStageId());
	}

	@Test
	@UnitTestMethod(name = "getMaterialId", args = { BatchConstructionInfo.class })
	public void testGetMaterialId() {
		StageId stageId = new StageId(35645);
		MaterialId materialId = TestMaterialId.MATERIAL_2;
		double amount = 23453.45;
		StageToBatchConversionEvent stageToBatchConversionEvent = new StageToBatchConversionEvent(stageId,materialId,amount);
		assertEquals(materialId,stageToBatchConversionEvent.getMaterialId());
	}

	@Test
	@UnitTestMethod(name = "getAmount", args = { BatchConstructionInfo.class })
	public void testGetAmount() {
		StageId stageId = new StageId(35645);
		MaterialId materialId = TestMaterialId.MATERIAL_2;
		double amount = 23453.45;
		StageToBatchConversionEvent stageToBatchConversionEvent = new StageToBatchConversionEvent(stageId,materialId,amount);
		assertEquals(amount,stageToBatchConversionEvent.getAmount());
	}

}
