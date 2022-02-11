package plugins.materials.events.mutation;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import plugins.materials.support.BatchConstructionInfo;
import plugins.materials.support.MaterialId;
import plugins.materials.testsupport.TestMaterialId;
import util.annotations.UnitTest;
import util.annotations.UnitTestConstructor;
import util.annotations.UnitTestMethod;

@UnitTest(target = BatchCreationEvent.class)
public final class AT_BatchCreationEvent {

	@Test
	@UnitTestConstructor(args = { MaterialId.class, double.class })
	public void testConstructor() {
		// nothing to test
	}

	@Test
	@UnitTestMethod(name = "getMaterialId", args = {})
	public void testGetMaterialId() {
		MaterialId materialId = TestMaterialId.MATERIAL_2;
		double amount = 456.6;
		BatchCreationEvent batchCreationEvent = new BatchCreationEvent(materialId, amount);
		assertEquals(materialId, batchCreationEvent.getMaterialId());
	}

	@Test
	@UnitTestMethod(name = "getAmount", args = {})
	public void testGetAmount() {
		MaterialId materialId = TestMaterialId.MATERIAL_2;
		double amount = 456.6;
		BatchCreationEvent batchCreationEvent = new BatchCreationEvent(materialId, amount);
		assertEquals(amount, batchCreationEvent.getAmount());
	}

	@Test
	@UnitTestMethod(name = "getPrimaryKeyValue", args = { BatchConstructionInfo.class })
	public void testGetPrimaryKeyValue() {
		MaterialId materialId = TestMaterialId.MATERIAL_2;
		double amount = 456.6;
		BatchCreationEvent batchCreationEvent = new BatchCreationEvent(materialId, amount);
		assertEquals(BatchCreationEvent.class, batchCreationEvent.getPrimaryKeyValue());
	}

}
