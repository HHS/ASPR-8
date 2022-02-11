package plugins.materials.support;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.commons.math3.random.RandomGenerator;
import org.junit.jupiter.api.Test;

import plugins.materials.testsupport.TestBatchPropertyId;
import plugins.materials.testsupport.TestMaterialId;
import util.SeedProvider;
import util.annotations.UnitTest;
import util.annotations.UnitTestMethod;

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
		BatchConstructionInfo batchConstructionInfo = BatchConstructionInfo.builder().build();
		assertNotNull(batchConstructionInfo);
		assertEquals(0, batchConstructionInfo.getAmount());
		assertNull(batchConstructionInfo.getMaterialId());
		assertTrue(batchConstructionInfo.getPropertyValues().isEmpty());
	}

	@Test
	@UnitTestMethod(target = BatchConstructionInfo.Builder.class, name = "setAmount", args = { double.class })
	public void testSetAmount() {
		for (int i = 0; i < 10; i++) {
			double amount = 1000 * i;
			BatchConstructionInfo batchConstructionInfo = BatchConstructionInfo	.builder()//
																				.setAmount(amount)//
																				.build();//
			assertEquals(amount, batchConstructionInfo.getAmount());
		}
	}

	@Test
	@UnitTestMethod(target = BatchConstructionInfo.Builder.class, name = "setMaterialId", args = { MaterialId.class })
	public void testSetMaterialId() {
		for (TestMaterialId testMaterialId : TestMaterialId.values()) {
			BatchConstructionInfo batchConstructionInfo = BatchConstructionInfo	.builder()//
																				.setMaterialId(testMaterialId)//
																				.build();//
			assertEquals(testMaterialId, batchConstructionInfo.getMaterialId());
		}

	}

	@Test
	@UnitTestMethod(target = BatchConstructionInfo.Builder.class, name = "setPropertyValue", args = { BatchPropertyId.class, Object.class })
	public void testSetPropertyValue() {

		RandomGenerator randomGenerator = SeedProvider.getRandomGenerator(1174771995707697849L);
		BatchConstructionInfo.Builder builder = BatchConstructionInfo.builder();//
		for (TestMaterialId testMaterialId : TestMaterialId.values()) {
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

	@Test
	@UnitTestMethod(name = "getMaterialId", args = {})
	public void testGetMaterialId() {
 
		for (TestMaterialId testMaterialId : TestMaterialId.values()) {
			BatchConstructionInfo batchConstructionInfo = BatchConstructionInfo	.builder()//
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
																				.setAmount(amount)//
																				.build();//
			assertEquals(amount, batchConstructionInfo.getAmount());
		}
	}

	@Test
	@UnitTestMethod(name = "getPropertyValues", args = {})
	public void testGetPropertyValues() {
		RandomGenerator randomGenerator = SeedProvider.getRandomGenerator(1805920219436314340L);
		BatchConstructionInfo.Builder builder = BatchConstructionInfo.builder();//
		for (TestMaterialId testMaterialId : TestMaterialId.values()) {
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
