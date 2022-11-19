package plugins.regions.testsupport;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.math3.random.RandomGenerator;
import org.junit.jupiter.api.Test;

import plugins.regions.support.RegionPropertyId;
import plugins.util.properties.PropertyDefinition;
import tools.annotations.UnitTest;
import tools.annotations.UnitTestMethod;
import util.random.RandomGeneratorProvider;

@UnitTest(target = TestRegionPropertyId.class)
public class AT_TestRegionPropertyId {

	/**
	 * Shows that a generated unknown region property id is not null and
	 * not a member of the enum and is unique.
	 */
	@Test
	@UnitTestMethod(name = "getUnknownRegionPropertyId", args = {})
	public void testGetUnknownRegionPropertyId() {
		RegionPropertyId unknownRegionPropertyId = TestRegionPropertyId.getUnknownRegionPropertyId();
		assertNotNull(unknownRegionPropertyId);
		for (TestRegionPropertyId testRegionPropertyId : TestRegionPropertyId.values()) {
			assertNotEquals(testRegionPropertyId, unknownRegionPropertyId);
		}

		Set<RegionPropertyId> unknownRegionPropertyIds = new LinkedHashSet<>();
		for (int i = 0; i < 10; i++) {
			unknownRegionPropertyId = TestRegionPropertyId.getUnknownRegionPropertyId();
			assertNotNull(unknownRegionPropertyId);
			boolean unique = unknownRegionPropertyIds.add(unknownRegionPropertyId);
			assertTrue(unique);
		}
	}

	/**
	 * Shows that size() returns the number of members in the TestRegionId
	 * enum
	 */
	@Test
	@UnitTestMethod(name = "size", args = {})
	public void testSize() {
		assertEquals(TestRegionPropertyId.values().length, TestRegionPropertyId.size());
	}

	@Test
	@UnitTestMethod(name = "getPropertyDefinition", args = {})
	public void testGetPropertyDefinition() {
		for (TestRegionPropertyId propertyId : TestRegionPropertyId.values()) {
			PropertyDefinition propertyDefinition = propertyId.getPropertyDefinition();
			assertNotNull(propertyDefinition);
		}
	}

	@Test
	@UnitTestMethod(name = "getRandomPropertyValue", args = { RandomGenerator.class })
	public void testGetRandomPropertyValue() {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(9052754083757003238L);

		/*
		 * Show that randomly generated values are compatible with the
		 * associated property definition. Show that the values are reasonably
		 * unique
		 */
		for (TestRegionPropertyId testRegionPropertyId : TestRegionPropertyId.values()) {
			PropertyDefinition propertyDefinition = testRegionPropertyId.getPropertyDefinition();
			Set<Object> values = new LinkedHashSet<>();
			for (int i = 0; i < 100; i++) {
				Object propertyValue = testRegionPropertyId.getRandomPropertyValue(randomGenerator);
				values.add(propertyValue);
				assertTrue(propertyDefinition.getType().isAssignableFrom(propertyValue.getClass()));				
			}
			//show that the values are reasonable unique
			if (propertyDefinition.getType() != Boolean.class) {
				assertTrue(values.size() > 10);
			} else {
				assertEquals(2, values.size());
			}
		}
	}

	@Test
	@UnitTestMethod(name = "getPropertesWithDefaultValues", args = {})
	public void testGetPropertesWithDefaultValues() {
		List<TestRegionPropertyId> expectedValues = Arrays.asList(
				TestRegionPropertyId.REGION_PROPERTY_1_BOOLEAN_MUTABLE,
				TestRegionPropertyId.REGION_PROPERTY_3_DOUBLE_MUTABLE,
				TestRegionPropertyId.REGION_PROPERTY_4_BOOLEAN_IMMUTABLE,
				TestRegionPropertyId.REGION_PROPERTY_5_INTEGER_IMMUTABLE,
				TestRegionPropertyId.REGION_PROPERTY_6_DOUBLE_IMMUTABLE);

		List<TestRegionPropertyId> actualValues = TestRegionPropertyId.getPropertesWithDefaultValues();

		assertNotNull(actualValues);
		assertEquals(expectedValues.size(), actualValues.size());
		Set<TestRegionPropertyId> setOfExpectedValues = new LinkedHashSet<>(expectedValues);
		Set<TestRegionPropertyId> setOfActualValues = new LinkedHashSet<>(actualValues);
		assertEquals(setOfExpectedValues, setOfActualValues);
		assertEquals(expectedValues.size(), setOfExpectedValues.size());
		assertEquals(actualValues.size(), setOfActualValues.size());
	}

	@Test
	@UnitTestMethod(name = "getPropertesWithoutDefaultValues", args = {})
	public void testGetPropertesWithoutDefaultValues() {
		List<TestRegionPropertyId> expectedValues = Arrays
				.asList(TestRegionPropertyId.REGION_PROPERTY_2_INTEGER_MUTABLE);
		List<TestRegionPropertyId> actualValues = TestRegionPropertyId.getPropertesWithoutDefaultValues();

		assertNotNull(actualValues);
		assertEquals(expectedValues.size(), actualValues.size());
		Set<TestRegionPropertyId> setOfExpectedValues = new LinkedHashSet<>(expectedValues);
		Set<TestRegionPropertyId> setOfActualValues = new LinkedHashSet<>(actualValues);
		assertEquals(setOfExpectedValues, setOfActualValues);
		assertEquals(expectedValues.size(), setOfExpectedValues.size());
		assertEquals(actualValues.size(), setOfActualValues.size());
	}

	@Test
	@UnitTestMethod(name = "getRandomMutableRegionPropertyId", args = { RandomGenerator.class })
	public void testGetRandomMutableRegionPropertyId() {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(4772298816496492540L);

		Set<TestRegionPropertyId> applicableValues = Set.of(TestRegionPropertyId.REGION_PROPERTY_2_INTEGER_MUTABLE,
				TestRegionPropertyId.REGION_PROPERTY_3_DOUBLE_MUTABLE,
				TestRegionPropertyId.REGION_PROPERTY_1_BOOLEAN_MUTABLE);

		for (int i = 0; i < 15; i++) {
			TestRegionPropertyId actualValue = TestRegionPropertyId.getRandomMutableRegionPropertyId(randomGenerator);

			assertNotNull(actualValue);
			assertTrue(applicableValues.contains(actualValue));
		}
	}

	@Test
	@UnitTestMethod(name = "getRandomRegionPropertyId", args = { RandomGenerator.class })
	public void testGetRandomRegionPropertyId() {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(8246696863539332004L);

		List<TestRegionPropertyId> applicableValues = Arrays.asList(TestRegionPropertyId.values());
		Map<TestRegionPropertyId, Integer> valueCounter = new LinkedHashMap<>();

		for (int i = 0; i < 100; i++) {
			for (int j = 0; j < applicableValues.size(); j++) {
				TestRegionPropertyId actualValue = TestRegionPropertyId.getRandomRegionPropertyId(randomGenerator);

				assertNotNull(actualValue);
				assertTrue(applicableValues.contains(actualValue));
				int numTimes = 1;
				if (valueCounter.containsKey(actualValue))
					numTimes = valueCounter.get(actualValue) + 1;
				valueCounter.put(actualValue, numTimes);
			}
		}
		for (TestRegionPropertyId propertyId : valueCounter.keySet()) {
			int numTimes = valueCounter.get(propertyId);
			assertTrue(numTimes >= 90);
		}
	}
}
