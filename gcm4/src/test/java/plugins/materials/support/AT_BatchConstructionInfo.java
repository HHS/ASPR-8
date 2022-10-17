package plugins.materials.support;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.commons.math3.random.RandomGenerator;
import org.junit.jupiter.api.Test;

import plugins.materials.testsupport.TestBatchPropertyId;
import plugins.materials.testsupport.TestMaterialId;
import plugins.materials.testsupport.TestMaterialsProducerId;
import plugins.util.properties.PropertyError;
import tools.annotations.UnitTest;
import tools.annotations.UnitTestMethod;
import util.errors.ContractException;
import util.random.RandomGeneratorProvider;

@UnitTest(target = BatchConstructionInfo.class)
public class AT_BatchConstructionInfo {

	@Test
	@UnitTestMethod(name = "builder", args = {})
	public void testBuilder() {

		assertNotNull(BatchConstructionInfo.builder());
	}

	@Test
	@UnitTestMethod(target = BatchConstructionInfo.Builder.class, name = "build", args = {})
	public void testBuild() {

		BatchConstructionInfo batchConstructionInfo = //
				BatchConstructionInfo	.builder()//
										.setMaterialId(TestMaterialId.MATERIAL_1)//
										.setMaterialsProducerId(TestMaterialsProducerId.MATERIALS_PRODUCER_1)//
										.build();
		assertNotNull(batchConstructionInfo);
		assertEquals(0, batchConstructionInfo.getAmount());
		assertEquals(TestMaterialId.MATERIAL_1, batchConstructionInfo.getMaterialId());
		assertEquals(TestMaterialsProducerId.MATERIALS_PRODUCER_1, batchConstructionInfo.getMaterialsProducerId());
		assertTrue(batchConstructionInfo.getPropertyValues().isEmpty());

		// precondition test : if the material id was not set
		ContractException contractException = assertThrows(ContractException.class, () -> //
		BatchConstructionInfo	.builder()//
								.setMaterialsProducerId(TestMaterialsProducerId.MATERIALS_PRODUCER_1)//
								.build());//

		assertEquals(MaterialsError.NULL_MATERIAL_ID, contractException.getErrorType());

		// precondition test: if the materials producer id was not set
		contractException = assertThrows(ContractException.class, () -> //
		BatchConstructionInfo	.builder()//
								.setMaterialId(TestMaterialId.MATERIAL_1)//
								.build());//

		assertEquals(MaterialsError.NULL_MATERIALS_PRODUCER_ID, contractException.getErrorType());

	}

	// 2143058467392057240L
	//
	// 4485728390035031270L
	@Test
	@UnitTestMethod(target = BatchConstructionInfo.Builder.class, name = "setAmount", args = { double.class })
	public void testSetAmount() {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(3990739257871475353L);

		for (int i = 0; i < 10; i++) {
			double amount = 1000 * i;
			BatchConstructionInfo batchConstructionInfo = BatchConstructionInfo	.builder()//
																				.setMaterialsProducerId(TestMaterialsProducerId.getRandomMaterialsProducerId(randomGenerator))//
																				.setMaterialId(TestMaterialId.getRandomMaterialId(randomGenerator)) //
																				.setAmount(amount)//
																				.build();//
			assertEquals(amount, batchConstructionInfo.getAmount());
		}

		// precondition test: if the amount is negative

		ContractException contractException = assertThrows(ContractException.class, //
				() -> BatchConstructionInfo	.builder()//
											.setMaterialsProducerId(TestMaterialsProducerId.getRandomMaterialsProducerId(randomGenerator))//
											.setMaterialId(TestMaterialId.getRandomMaterialId(randomGenerator)) //
											.setAmount(-1.0)//
											.build()//
		);
		assertEquals(MaterialsError.NEGATIVE_MATERIAL_AMOUNT, contractException.getErrorType());

		// if the amount is not finite

		contractException = assertThrows(ContractException.class, //
				() -> BatchConstructionInfo	.builder()//
											.setMaterialsProducerId(TestMaterialsProducerId.getRandomMaterialsProducerId(randomGenerator))//
											.setMaterialId(TestMaterialId.getRandomMaterialId(randomGenerator)) //
											.setAmount(Double.POSITIVE_INFINITY)//
											.build()//
		);
		assertEquals(MaterialsError.NON_FINITE_MATERIAL_AMOUNT, contractException.getErrorType());

		contractException = assertThrows(ContractException.class, //
				() -> BatchConstructionInfo	.builder()//
											.setMaterialsProducerId(TestMaterialsProducerId.getRandomMaterialsProducerId(randomGenerator))//
											.setMaterialId(TestMaterialId.getRandomMaterialId(randomGenerator)) //
											.setAmount(Double.NaN)//
											.build()//
		);
		assertEquals(MaterialsError.NON_FINITE_MATERIAL_AMOUNT, contractException.getErrorType());

	}

	@Test
	@UnitTestMethod(target = BatchConstructionInfo.Builder.class, name = "setMaterialId", args = { MaterialId.class })
	public void testSetMaterialId() {
		for (TestMaterialId testMaterialId : TestMaterialId.values()) {
			BatchConstructionInfo batchConstructionInfo = BatchConstructionInfo	.builder()//
																				.setMaterialId(testMaterialId)//
																				.setMaterialsProducerId(TestMaterialsProducerId.MATERIALS_PRODUCER_1).build();//
			assertEquals(testMaterialId, batchConstructionInfo.getMaterialId());

		}

		// precondition test: if the material id is null
		ContractException contractException = assertThrows(ContractException.class, () -> //
		BatchConstructionInfo	.builder()//
								.setMaterialId(null)//
								.setMaterialsProducerId(TestMaterialsProducerId.MATERIALS_PRODUCER_1).build()//
		);
		assertEquals(MaterialsError.NULL_MATERIAL_ID, contractException.getErrorType());

	}

	@Test
	@UnitTestMethod(target = BatchConstructionInfo.Builder.class, name = "setPropertyValue", args = { BatchPropertyId.class, Object.class })
	public void testSetPropertyValue() {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(1174771995707697849L);
		BatchConstructionInfo.Builder builder = BatchConstructionInfo.builder();//
		for (TestMaterialsProducerId testMaterialsProducerId : TestMaterialsProducerId.values()) {
			for (TestMaterialId testMaterialId : TestMaterialId.values()) {
				builder.setMaterialId(testMaterialId);//
				builder.setMaterialsProducerId(testMaterialsProducerId);//
				Map<BatchPropertyId, Object> expectedPropertyValues = new LinkedHashMap<>();
				for (TestBatchPropertyId testBatchPropertyId : TestBatchPropertyId.getTestBatchPropertyIds(testMaterialId)) {
					Object value = testBatchPropertyId.getRandomPropertyValue(randomGenerator);
					expectedPropertyValues.put(testBatchPropertyId, value);
					builder.setPropertyValue(testBatchPropertyId, value);//
				}
				BatchConstructionInfo batchConstructionInfo = builder.build();//
				assertEquals(testMaterialId, batchConstructionInfo.getMaterialId());

				Map<BatchPropertyId, Object> propertyValues = batchConstructionInfo.getPropertyValues();
				assertEquals(expectedPropertyValues, propertyValues);
			}
		}

		// precondition test: if the property id is null
		ContractException contractException = assertThrows(ContractException.class, () -> BatchConstructionInfo.builder().setPropertyValue(null, 15));
		assertEquals(PropertyError.NULL_PROPERTY_ID, contractException.getErrorType());

		// precondition test: if the property value is null
		contractException = assertThrows(ContractException.class, () -> BatchConstructionInfo.builder().setPropertyValue(TestBatchPropertyId.BATCH_PROPERTY_1_1_BOOLEAN_IMMUTABLE_NO_TRACK, null));
		assertEquals(PropertyError.NULL_PROPERTY_VALUE, contractException.getErrorType());

	}

	@Test
	@UnitTestMethod(name = "getMaterialId", args = {})
	public void testGetMaterialId() {

		for (TestMaterialId testMaterialId : TestMaterialId.values()) {
			BatchConstructionInfo batchConstructionInfo = BatchConstructionInfo	.builder()//
																				.setMaterialId(TestMaterialId.MATERIAL_3)//
																				.setMaterialsProducerId(TestMaterialsProducerId.MATERIALS_PRODUCER_1)//
																				.setMaterialId(testMaterialId)//
																				.build();//
			assertEquals(testMaterialId, batchConstructionInfo.getMaterialId());
		}
	}

	@Test
	@UnitTestMethod(name = "getAmount", args = {})
	public void testGetAmount() {
		for (int i = 0; i < 10; i++) {
			double amount = 1000 * i;
			BatchConstructionInfo batchConstructionInfo = BatchConstructionInfo	.builder()//
																				.setMaterialId(TestMaterialId.MATERIAL_1)//
																				.setMaterialsProducerId(TestMaterialsProducerId.MATERIALS_PRODUCER_2).setAmount(amount)//
																				.build();//
			assertEquals(amount, batchConstructionInfo.getAmount());
		}
	}

	@Test
	@UnitTestMethod(name = "getPropertyValues", args = {})
	public void testGetPropertyValues() {

		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(1805920219436314340L);
		BatchConstructionInfo.Builder builder = BatchConstructionInfo.builder();//
		for (TestMaterialsProducerId testMaterialsProducerId : TestMaterialsProducerId.values()) {
			for (TestMaterialId testMaterialId : TestMaterialId.values()) {
				builder.setMaterialsProducerId(testMaterialsProducerId);
				builder.setMaterialId(testMaterialId);//

				Map<BatchPropertyId, Object> expectedPropertyValues = new LinkedHashMap<>();
				for (TestBatchPropertyId testBatchPropertyId : TestBatchPropertyId.getTestBatchPropertyIds(testMaterialId)) {
					Object value = testBatchPropertyId.getRandomPropertyValue(randomGenerator);
					expectedPropertyValues.put(testBatchPropertyId, value);
					builder.setPropertyValue(testBatchPropertyId, value);//
				}
				BatchConstructionInfo batchConstructionInfo = builder.build();//
				assertEquals(testMaterialId, batchConstructionInfo.getMaterialId());

				Map<BatchPropertyId, Object> propertyValues = batchConstructionInfo.getPropertyValues();
				assertEquals(expectedPropertyValues, propertyValues);
			}
		}
	}

}
