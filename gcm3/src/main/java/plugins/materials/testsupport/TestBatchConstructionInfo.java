package plugins.materials.testsupport;

import org.apache.commons.math3.random.RandomGenerator;

import plugins.materials.support.BatchConstructionInfo;
import plugins.materials.support.MaterialId;
import plugins.materials.support.MaterialsProducerId;

public class TestBatchConstructionInfo {

	public static BatchConstructionInfo getBatchConstructionInfo(MaterialsProducerId materialsProducerId, MaterialId materialId, double amount, RandomGenerator randomGenerator) {
		BatchConstructionInfo.Builder builder = //
				BatchConstructionInfo	.builder()//
										.setMaterialId(materialId)//
										.setMaterialsProducerId(materialsProducerId)//
										.setAmount(amount);//
		for (TestBatchPropertyId testBatchPropertyId : TestBatchPropertyId.values()) {
			if (testBatchPropertyId.getTestMaterialId().equals(materialId)) {
				if (testBatchPropertyId.getPropertyDefinition().getDefaultValue().isEmpty()) {
					Object value = testBatchPropertyId.getRandomPropertyValue(randomGenerator);
					builder.setPropertyValue(testBatchPropertyId, value);
				}
			}
		}
		return builder.build();
	}
}
