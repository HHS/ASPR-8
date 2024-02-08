package gov.hhs.aspr.ms.gcm.plugins.materials.testsupport;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.apache.commons.math3.random.RandomGenerator;
import org.junit.jupiter.api.Test;

import gov.hhs.aspr.ms.gcm.plugins.materials.support.BatchConstructionInfo;
import gov.hhs.aspr.ms.gcm.plugins.materials.support.MaterialId;
import gov.hhs.aspr.ms.gcm.plugins.materials.support.MaterialsError;
import gov.hhs.aspr.ms.gcm.plugins.materials.support.MaterialsProducerId;
import gov.hhs.aspr.ms.util.annotations.UnitTestMethod;
import gov.hhs.aspr.ms.util.errors.ContractException;
import gov.hhs.aspr.ms.util.random.RandomGeneratorProvider;

public class AT_TestBatchConstructionInfo {

	@Test
	@UnitTestMethod(target = TestBatchConstructionInfo.class, name = "getBatchConstructionInfo", args = { MaterialsProducerId.class, MaterialId.class, double.class, RandomGenerator.class })
	public void testGetBatchConstructionInfo() {
		MaterialsProducerId materialsProducerId = TestMaterialsProducerId.MATERIALS_PRODUCER_1;
		MaterialId materialId = TestMaterialId.MATERIAL_1;
		double amount = 25.0;
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(2531244935109186076L);

		BatchConstructionInfo batchConstructionInfo = TestBatchConstructionInfo.getBatchConstructionInfo(materialsProducerId, materialId, amount, randomGenerator);

		assertNotNull(batchConstructionInfo);

		// precondition: null material id
		ContractException contractException = assertThrows(ContractException.class, () -> TestBatchConstructionInfo.getBatchConstructionInfo(materialsProducerId, null, amount, randomGenerator));
		assertEquals(MaterialsError.NULL_MATERIAL_ID, contractException.getErrorType());

		// precondition: null material producer id
		contractException = assertThrows(ContractException.class, () -> TestBatchConstructionInfo.getBatchConstructionInfo(null, materialId, amount, randomGenerator));
		assertEquals(MaterialsError.NULL_MATERIALS_PRODUCER_ID, contractException.getErrorType());

		// precondition: negative amount
		contractException = assertThrows(ContractException.class, () -> TestBatchConstructionInfo.getBatchConstructionInfo(materialsProducerId, materialId, -100.0, randomGenerator));
		assertEquals(MaterialsError.NEGATIVE_MATERIAL_AMOUNT, contractException.getErrorType());

		// precondition: amount is not finite
		contractException = assertThrows(ContractException.class, () -> TestBatchConstructionInfo.getBatchConstructionInfo(materialsProducerId, materialId, Double.POSITIVE_INFINITY, randomGenerator));
		assertEquals(MaterialsError.NON_FINITE_MATERIAL_AMOUNT, contractException.getErrorType());
	}
}
