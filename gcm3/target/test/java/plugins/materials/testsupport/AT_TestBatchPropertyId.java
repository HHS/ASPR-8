package plugins.materials.testsupport;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.LinkedHashSet;
import java.util.Set;

import org.apache.commons.math3.random.RandomGenerator;
import org.junit.jupiter.api.Test;

import plugins.materials.support.BatchPropertyId;
import plugins.properties.support.PropertyDefinition;
import util.SeedProvider;
import util.annotations.UnitTest;
import util.annotations.UnitTestMethod;

@UnitTest(target = TestBatchPropertyId.class)
public class AT_TestBatchPropertyId {

	@Test
	@UnitTestMethod(name = "getPropertyDefinition", args = {})
	public void testGetPropertyDefinition() {
		for (TestBatchPropertyId testBatchPropertyId : TestBatchPropertyId.values()) {
			PropertyDefinition propertyDefinition = testBatchPropertyId.getPropertyDefinition();
			assertNotNull(propertyDefinition);
			assertTrue(propertyDefinition.getDefaultValue().isPresent());
		}
	}

	@Test
	@UnitTestMethod(name = "getTestMaterialId", args = {})
	public void testGetTestMaterialId() {
		for (TestBatchPropertyId testBatchPropertyId : TestBatchPropertyId.values()) {
			assertNotNull(testBatchPropertyId.getTestMaterialId());
		}
	}

	@Test
	@UnitTestMethod(name = "getTestBatchPropertyIds", args = { TestMaterialId.class })
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
	@UnitTestMethod(name = "getUnknownBatchPropertyId", args = { TestMaterialId.class })
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
	@UnitTestMethod(name = "getRandomMutableBatchPropertyId", args = { TestMaterialId.class, RandomGenerator.class })
	public void testGetRandomMutableBatchPropertyId() {
		RandomGenerator randomGenerator = SeedProvider.getRandomGenerator(7432312917768892660L);
		for (TestMaterialId testMaterialId : TestMaterialId.values()) {
			TestBatchPropertyId batchPropertyId = TestBatchPropertyId.getRandomMutableBatchPropertyId(testMaterialId, randomGenerator);
			assertNotNull(batchPropertyId);
			assertEquals(testMaterialId, batchPropertyId.getTestMaterialId());
		}
	}

	@Test
	@UnitTestMethod(name = "getRandomPropertyValue", args = { RandomGenerator.class })
	public void testGetRandomPropertyValue() {
		RandomGenerator randomGenerator = SeedProvider.getRandomGenerator(2839187347342327244L);
		for (TestBatchPropertyId testBatchPropertyId : TestBatchPropertyId.values()) {
			PropertyDefinition propertyDefinition = testBatchPropertyId.getPropertyDefinition();
			for (int i = 0; i < 10; i++) {
				Object propertyValue = testBatchPropertyId.getRandomPropertyValue(randomGenerator);
				assertNotNull(propertyValue);
				assertTrue(propertyDefinition.getType().isAssignableFrom(propertyValue.getClass()));
			}
		}
	}
}
