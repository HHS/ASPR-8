package gov.hhs.aspr.ms.gcm.plugins.materials.testsupport;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.math3.random.RandomGenerator;
import org.junit.jupiter.api.Test;

import gov.hhs.aspr.ms.gcm.plugins.materials.support.BatchPropertyId;
import gov.hhs.aspr.ms.gcm.plugins.util.properties.PropertyDefinition;
import util.annotations.UnitTestMethod;
import util.random.RandomGeneratorProvider;

public class AT_TestBatchPropertyId {

	@Test
	@UnitTestMethod(target = TestBatchPropertyId.class, name = "getPropertyDefinition", args = {})
	public void testGetPropertyDefinition() {
		for (TestBatchPropertyId testBatchPropertyId : TestBatchPropertyId.values()) {
			PropertyDefinition propertyDefinition = testBatchPropertyId.getPropertyDefinition();
			assertNotNull(propertyDefinition);
		}
	}

	@Test
	@UnitTestMethod(target = TestBatchPropertyId.class, name = "getTestMaterialId", args = {})
	public void testGetTestMaterialId() {
		for (TestBatchPropertyId testBatchPropertyId : TestBatchPropertyId.values()) {
			assertNotNull(testBatchPropertyId.getTestMaterialId());
		}
	}

	@Test
	@UnitTestMethod(target = TestBatchPropertyId.class, name = "getTestBatchPropertyIds", args = { TestMaterialId.class })
	public void testGetTestBatchPropertyIds() {
		for (TestMaterialId testMaterialId : TestMaterialId.values()) {
			Set<TestBatchPropertyId> expectedBatchPropertyIds = new LinkedHashSet<>();
			for (TestBatchPropertyId testBatchPropertyId : TestBatchPropertyId.values()) {
				if (testMaterialId.equals(testBatchPropertyId.getTestMaterialId())) {
					expectedBatchPropertyIds.add(testBatchPropertyId);
				}
			}
			Set<TestBatchPropertyId> actualBatchPropertyIds = TestBatchPropertyId.getTestBatchPropertyIds(testMaterialId);
			assertEquals(expectedBatchPropertyIds, actualBatchPropertyIds);
		}
	}

	@Test
	@UnitTestMethod(target = TestBatchPropertyId.class, name = "getUnknownBatchPropertyId", args = {})
	public void testGetUnknownBatchPropertyId() {
		BatchPropertyId unknownBatchPropertyId = TestBatchPropertyId.getUnknownBatchPropertyId();
		assertNotNull(unknownBatchPropertyId);
		for (TestBatchPropertyId testBatchPropertyId : TestBatchPropertyId.values()) {
			assertNotEquals(testBatchPropertyId, unknownBatchPropertyId);
		}
		BatchPropertyId unknownBatchPropertyId2 = TestBatchPropertyId.getUnknownBatchPropertyId();
		assertNotEquals(unknownBatchPropertyId, unknownBatchPropertyId2);
	}

	@Test
	@UnitTestMethod(target = TestBatchPropertyId.class, name = "getRandomMutableBatchPropertyId", args = { TestMaterialId.class, RandomGenerator.class })
	public void testGetRandomMutableBatchPropertyId() {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(7432312917768892660L);
		for (TestMaterialId testMaterialId : TestMaterialId.values()) {
			TestBatchPropertyId batchPropertyId = TestBatchPropertyId.getRandomMutableBatchPropertyId(testMaterialId, randomGenerator);
			assertNotNull(batchPropertyId);
			assertEquals(testMaterialId, batchPropertyId.getTestMaterialId());
		}
	}

	@Test
	@UnitTestMethod(target = TestBatchPropertyId.class, name = "getRandomPropertyValue", args = { RandomGenerator.class })
	public void testGetRandomPropertyValue() {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(2839187347342327244L);
		for (TestBatchPropertyId testBatchPropertyId : TestBatchPropertyId.values()) {
			PropertyDefinition propertyDefinition = testBatchPropertyId.getPropertyDefinition();
			for (int i = 0; i < 10; i++) {
				Object propertyValue = testBatchPropertyId.getRandomPropertyValue(randomGenerator);
				assertNotNull(propertyValue);
				assertTrue(propertyDefinition.getType().isAssignableFrom(propertyValue.getClass()));
			}
		}
	}
	

	@Test
	@UnitTestMethod(target = TestBatchPropertyId.class, name = "getBatchPropertyIds", args = {})
	public void testGetBatchPropertyIds() {
		assertEquals(Arrays.asList(TestBatchPropertyId.values()),TestBatchPropertyId.getBatchPropertyIds());		
	}

	@Test
	@UnitTestMethod(target = TestBatchPropertyId.class, name = "getBatchPropertyIds", args = { RandomGenerator.class })
	public void testGetBatchPropertyIds_RandomGenerator() {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(2272120777807498806L);
		LinkedHashSet<TestBatchPropertyId> expectedSet = new LinkedHashSet<>(Arrays.asList(TestBatchPropertyId.values()));
		Set<List<TestBatchPropertyId>> actualLists =  new LinkedHashSet<>();
		for(int i = 0;i<1000;i++) {
			List<TestBatchPropertyId> batchPropertyIds = TestBatchPropertyId.getBatchPropertyIds(randomGenerator);
			actualLists.add(batchPropertyIds);
			//show that the generated list has all the property ids
			assertEquals(expectedSet,new LinkedHashSet<>(batchPropertyIds));		
		}
		//show that the 1000 lists of the 362880 = 9! possible arrangements result in nearly no duplicates		
		assertTrue(actualLists.size()>990);
	}

}
