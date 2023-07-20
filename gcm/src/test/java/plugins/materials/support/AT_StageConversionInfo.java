package plugins.materials.support;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.commons.math3.random.RandomGenerator;
import org.junit.jupiter.api.Test;

import plugins.materials.testsupport.TestBatchPropertyId;
import plugins.materials.testsupport.TestMaterialId;
import plugins.util.properties.PropertyError;
import util.annotations.UnitTestMethod;
import util.errors.ContractException;
import util.random.RandomGeneratorProvider;

public class AT_StageConversionInfo {
	@Test
	@UnitTestMethod(target = StageConversionInfo.class, name = "builder", args = {})
	public void testBuilder() {
		assertNotNull(StageConversionInfo.builder());
	}

	@Test
	@UnitTestMethod(target = StageConversionInfo.class, name = "getAmount", args = {})
	public void testGetAmount() {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(1709194760368532797L);
		for (int i = 0; i < 30; i++) {
			double amount = randomGenerator.nextDouble();
			StageConversionInfo stageConversionInfo = //
					StageConversionInfo.builder()//
							.setAmount(amount)//
							.setMaterialId(TestMaterialId.MATERIAL_1)//
							.setStageId(new StageId(6))//
							.build();

			assertEquals(amount, stageConversionInfo.getAmount());
		}

	}

	@Test
	@UnitTestMethod(target = StageConversionInfo.class, name = "getMaterialId", args = {})
	public void testGetMaterialId() {

		for (TestMaterialId testMaterialId : TestMaterialId.values()) {

			StageConversionInfo stageConversionInfo = //
					StageConversionInfo.builder()//
							.setAmount(14.0)//
							.setMaterialId(testMaterialId)//
							.setStageId(new StageId(6))//
							.build();

			assertEquals(testMaterialId, stageConversionInfo.getMaterialId());
		}

	}

	@Test
	@UnitTestMethod(target = StageConversionInfo.class, name = "getPropertyValues", args = {})
	public void testGetPropertyValues() {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(685359137261114208L);

		for (int i = 0; i < 30; i++) {
			Map<BatchPropertyId, Object> expectedPropertyValues = new LinkedHashMap<>();

			StageConversionInfo.Builder builder = //
					StageConversionInfo.builder()//
							.setAmount(14.0)//
							.setMaterialId(TestMaterialId.MATERIAL_2)//
							.setStageId(new StageId(6));//

			for (TestBatchPropertyId testBatchPropertyId : TestBatchPropertyId.values()) {
				if (randomGenerator.nextBoolean()) {
					Object propertyValue = testBatchPropertyId.getRandomPropertyValue(randomGenerator);
					builder.setPropertyValue(testBatchPropertyId, propertyValue);
					expectedPropertyValues.put(testBatchPropertyId, propertyValue);
				}
			}

			StageConversionInfo stageConversionInfo = builder.build();
			Map<BatchPropertyId, Object> actualPropertyValues = stageConversionInfo.getPropertyValues();
			assertEquals(expectedPropertyValues, actualPropertyValues);

		}

	}

	@Test
	@UnitTestMethod(target = StageConversionInfo.class, name = "getStageId", args = {})
	public void testGetStageId() {

		for (int i = 0; i < 30; i++) {
			StageId stageId = new StageId(i);
			StageConversionInfo stageConversionInfo = //
					StageConversionInfo.builder()//
							.setAmount(14.0)//
							.setMaterialId(TestMaterialId.MATERIAL_2)//
							.setStageId(stageId)//
							.build();

			assertEquals(stageId, stageConversionInfo.getStageId());
		}

	}

	@Test
	@UnitTestMethod(target = StageConversionInfo.Builder.class, name = "setStageId", args = { StageId.class })
	public void testSetStageId() {

		for (int i = 0; i < 30; i++) {
			StageId stageId = new StageId(i);
			StageConversionInfo stageConversionInfo = //
					StageConversionInfo.builder()//
							.setAmount(14.0)//
							.setMaterialId(TestMaterialId.MATERIAL_2)//
							.setStageId(stageId)//
							.build();

			assertEquals(stageId, stageConversionInfo.getStageId());
		}

		// precondition tests : if the stage id is null
		ContractException contractException = assertThrows(ContractException.class,
				() -> StageConversionInfo.builder().setStageId(null));
		assertEquals(MaterialsError.NULL_STAGE_ID, contractException.getErrorType());
	}

	@Test
	@UnitTestMethod(target = StageConversionInfo.Builder.class, name = "setMaterialId", args = { MaterialId.class })
	public void testSetMaterialId() {

		for (TestMaterialId testMaterialId : TestMaterialId.values()) {

			StageConversionInfo stageConversionInfo = //
					StageConversionInfo.builder()//
							.setAmount(14.0)//
							.setMaterialId(testMaterialId)//
							.setStageId(new StageId(6))//
							.build();

			assertEquals(testMaterialId, stageConversionInfo.getMaterialId());
		}

		// precondition tests : if the materialF id is null
		ContractException contractException = assertThrows(ContractException.class,
				() -> StageConversionInfo.builder().setMaterialId(null));
		assertEquals(MaterialsError.NULL_MATERIAL_ID, contractException.getErrorType());
	}

	@Test
	@UnitTestMethod(target = StageConversionInfo.Builder.class, name = "setAmount", args = { double.class })
	public void testSetAmount() {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(1709194760368532797L);
		for (int i = 0; i < 30; i++) {
			double amount = randomGenerator.nextDouble();
			StageConversionInfo stageConversionInfo = //
					StageConversionInfo.builder()//
							.setAmount(amount)//
							.setMaterialId(TestMaterialId.MATERIAL_1)//
							.setStageId(new StageId(6))//
							.build();

			assertEquals(amount, stageConversionInfo.getAmount());
		}

		// precondition tests : if the amount is negative
		ContractException contractException = assertThrows(ContractException.class,
				() -> StageConversionInfo.builder().setAmount(-234L));
		assertEquals(MaterialsError.NEGATIVE_MATERIAL_AMOUNT, contractException.getErrorType());

		// precondition tests : if the amount is not finite
		contractException = assertThrows(ContractException.class,
				() -> StageConversionInfo.builder().setAmount(Double.NaN));
		assertEquals(MaterialsError.NON_FINITE_MATERIAL_AMOUNT, contractException.getErrorType());

		contractException = assertThrows(ContractException.class,
				() -> StageConversionInfo.builder().setAmount(Double.POSITIVE_INFINITY));
		assertEquals(MaterialsError.NON_FINITE_MATERIAL_AMOUNT, contractException.getErrorType());

		contractException = assertThrows(ContractException.class,
				() -> StageConversionInfo.builder().setAmount(Double.NEGATIVE_INFINITY));
		assertEquals(MaterialsError.NON_FINITE_MATERIAL_AMOUNT, contractException.getErrorType());

	}

	@Test
	@UnitTestMethod(target = StageConversionInfo.Builder.class, name = "setPropertyValue", args = {
			BatchPropertyId.class, Object.class })
	public void testSetPropertyValue() {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(4143317260505344550L);

		for (int i = 0; i < 30; i++) {
			Map<BatchPropertyId, Object> expectedPropertyValues = new LinkedHashMap<>();

			StageConversionInfo.Builder builder = //
					StageConversionInfo.builder()//
							.setAmount(14.0)//
							.setMaterialId(TestMaterialId.MATERIAL_2)//
							.setStageId(new StageId(6));//

			for (TestBatchPropertyId testBatchPropertyId : TestBatchPropertyId.values()) {
				if (randomGenerator.nextBoolean()) {
					Object propertyValue = testBatchPropertyId.getRandomPropertyValue(randomGenerator);
					builder.setPropertyValue(testBatchPropertyId, propertyValue);
					expectedPropertyValues.put(testBatchPropertyId, propertyValue);
				}
			}

			StageConversionInfo stageConversionInfo = builder.build();
			Map<BatchPropertyId, Object> actualPropertyValues = stageConversionInfo.getPropertyValues();
			assertEquals(expectedPropertyValues, actualPropertyValues);

		}

		// precondition tests : if the property id is null
		ContractException contractException = assertThrows(ContractException.class,
				() -> StageConversionInfo.builder().setPropertyValue(null, 3.6));
		assertEquals(PropertyError.NULL_PROPERTY_ID, contractException.getErrorType());

		// precondition tests : if the property id is null
		contractException = assertThrows(ContractException.class, () -> StageConversionInfo.builder()
				.setPropertyValue(TestBatchPropertyId.BATCH_PROPERTY_1_1_BOOLEAN_IMMUTABLE_NO_TRACK, null));
		assertEquals(PropertyError.NULL_PROPERTY_VALUE, contractException.getErrorType());

	}

	@Test
	@UnitTestMethod(target = StageConversionInfo.Builder.class, name = "build", args = {})
	public void testBuild() {

		// precondition tests : if the stage id is null
		ContractException contractException = assertThrows(ContractException.class,
				() -> StageConversionInfo.builder().setMaterialId(TestMaterialId.MATERIAL_1).build());
		assertEquals(MaterialsError.NULL_STAGE_ID, contractException.getErrorType());

		// precondition tests : if the material id is null
		contractException = assertThrows(ContractException.class,
				() -> StageConversionInfo.builder().setStageId(new StageId(0)).build());
		assertEquals(MaterialsError.NULL_MATERIAL_ID, contractException.getErrorType());

	}

}
