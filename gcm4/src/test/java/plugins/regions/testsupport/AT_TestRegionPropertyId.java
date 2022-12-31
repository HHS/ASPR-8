package plugins.regions.testsupport;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.math3.random.RandomGenerator;
import org.junit.jupiter.api.Test;

import plugins.regions.support.RegionPropertyId;
import plugins.util.properties.PropertyDefinition;
import tools.annotations.UnitTestMethod;
import util.random.RandomGeneratorProvider;
import util.wrappers.MutableInteger;

public class AT_TestRegionPropertyId {

	/**
	 * Shows that a generated unknown region property id is not null and not a
	 * member of the enum and is unique.
	 */
	@Test
	@UnitTestMethod(target = TestRegionPropertyId.class, name = "getUnknownRegionPropertyId", args = {})
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
	 * Shows that size() returns the number of members in the TestRegionId enum
	 */
	@Test
	@UnitTestMethod(target = TestRegionPropertyId.class, name = "size", args = {})
	public void testSize() {
		assertEquals(TestRegionPropertyId.values().length, TestRegionPropertyId.size());
	}

	@Test
	@UnitTestMethod(target = TestRegionPropertyId.class, name = "getPropertyDefinition", args = {})
	public void testGetPropertyDefinition() {
		for (TestRegionPropertyId propertyId : TestRegionPropertyId.values()) {
			PropertyDefinition propertyDefinition = propertyId.getPropertyDefinition();
			assertNotNull(propertyDefinition);
		}
	}

	@Test
	@UnitTestMethod(target = TestRegionPropertyId.class, name = "getRandomPropertyValue", args = { RandomGenerator.class })
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
			// show that the values are reasonable unique
			if (propertyDefinition.getType() != Boolean.class) {
				assertTrue(values.size() > 10);
			} else {
				assertEquals(2, values.size());
			}
		}
	}

	@Test
	@UnitTestMethod(target = TestRegionPropertyId.class, name = "getPropertiesWithDefaultValues", args = {})
	public void testGetPropertesWithDefaultValues() {
		List<TestRegionPropertyId> expectedValues = new ArrayList<>();

		for (TestRegionPropertyId id : TestRegionPropertyId.values()) {
			if (id.getPropertyDefinition().getDefaultValue().isPresent()) {
				expectedValues.add(id);
			}
		}

		List<TestRegionPropertyId> actualValues = TestRegionPropertyId.getPropertiesWithDefaultValues();

		assertNotNull(actualValues);
		assertEquals(expectedValues.size(), actualValues.size());
		Set<TestRegionPropertyId> setOfExpectedValues = new LinkedHashSet<>(expectedValues);
		Set<TestRegionPropertyId> setOfActualValues = new LinkedHashSet<>(actualValues);
		assertEquals(setOfExpectedValues, setOfActualValues);
		assertEquals(expectedValues.size(), setOfExpectedValues.size());
		assertEquals(actualValues.size(), setOfActualValues.size());
	}

	@Test
	@UnitTestMethod(target = TestRegionPropertyId.class, name = "getPropertiesWithoutDefaultValues", args = {})
	public void testGetPropertesWithoutDefaultValues() {
		List<TestRegionPropertyId> expectedValues = new ArrayList<>();

		for (TestRegionPropertyId id : TestRegionPropertyId.values()) {
			if (id.getPropertyDefinition().getDefaultValue().isEmpty()) {
				expectedValues.add(id);
			}
		}

		List<TestRegionPropertyId> actualValues = TestRegionPropertyId.getPropertiesWithoutDefaultValues();

		assertNotNull(actualValues);
		assertEquals(expectedValues.size(), actualValues.size());
		Set<TestRegionPropertyId> setOfExpectedValues = new LinkedHashSet<>(expectedValues);
		Set<TestRegionPropertyId> setOfActualValues = new LinkedHashSet<>(actualValues);
		assertEquals(setOfExpectedValues, setOfActualValues);
		assertEquals(expectedValues.size(), setOfExpectedValues.size());
		assertEquals(actualValues.size(), setOfActualValues.size());
	}

	@Test
	@UnitTestMethod(target = TestRegionPropertyId.class, name = "getRandomMutableRegionPropertyId", args = { RandomGenerator.class })
	public void testGetRandomMutableRegionPropertyId() {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(4772298816496492540L);

		Set<TestRegionPropertyId> applicableValues = Set.of(TestRegionPropertyId.REGION_PROPERTY_2_INTEGER_MUTABLE, TestRegionPropertyId.REGION_PROPERTY_3_DOUBLE_MUTABLE,
				TestRegionPropertyId.REGION_PROPERTY_1_BOOLEAN_MUTABLE);

		for (int i = 0; i < 15; i++) {
			TestRegionPropertyId actualValue = TestRegionPropertyId.getRandomMutableRegionPropertyId(randomGenerator);

			assertNotNull(actualValue);
			assertTrue(applicableValues.contains(actualValue));
		}
	}

	@Test
	@UnitTestMethod(target = TestRegionPropertyId.class, name = "getRandomRegionPropertyId", args = { RandomGenerator.class })
	public void testGetRandomRegionPropertyId() {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(8246696863539332004L);

		Set<TestRegionPropertyId> applicableValues = EnumSet.allOf(TestRegionPropertyId.class);
		Map<TestRegionPropertyId, MutableInteger> valueCounter = new LinkedHashMap<>();

		for (TestRegionPropertyId actualValue : TestRegionPropertyId.values()) {
			valueCounter.put(actualValue, new MutableInteger());
		}

		for (int i = 0; i < 100; i++) {
			for (int j = 0; j < applicableValues.size(); j++) {
				TestRegionPropertyId actualValue = TestRegionPropertyId.getRandomRegionPropertyId(randomGenerator);

				assertNotNull(actualValue);
				assertTrue(applicableValues.contains(actualValue));
				valueCounter.get(actualValue).increment();
			}
		}
		for (TestRegionPropertyId propertyId : valueCounter.keySet()) {
			int numTimes = valueCounter.get(propertyId).getValue();
			assertTrue(numTimes >= 90);
			assertTrue(numTimes < 150);
		}
	}
}
