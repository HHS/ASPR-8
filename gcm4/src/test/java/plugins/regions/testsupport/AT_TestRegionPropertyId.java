package plugins.regions.testsupport;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
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

		Set<Integer> integers = new LinkedHashSet<>();
		Set<Boolean> booleans = new LinkedHashSet<>();
		Set<Double> doubles = new LinkedHashSet<>();

		TestRegionPropertyId integerM = TestRegionPropertyId.REGION_PROPERTY_2_INTEGER_MUTABLE;
		TestRegionPropertyId integerI = TestRegionPropertyId.REGION_PROPERTY_5_INTEGER_IMMUTABLE;
		TestRegionPropertyId doubleM = TestRegionPropertyId.REGION_PROPERTY_3_DOUBLE_MUTABLE;
		TestRegionPropertyId doubleI = TestRegionPropertyId.REGION_PROPERTY_6_DOUBLE_IMMUTABLE;
		TestRegionPropertyId booleanM = TestRegionPropertyId.REGION_PROPERTY_1_BOOLEAN_MUTABLE;
		TestRegionPropertyId booleanI = TestRegionPropertyId.REGION_PROPERTY_4_BOOLEAN_IMMUTABLE;

		boolean assertFalse = false;
		for (int i = 0; i < 15; i++) {
			int intValM = integerM.getRandomPropertyValue(randomGenerator);
			int intValI = integerI.getRandomPropertyValue(randomGenerator);
			double doubleValM = doubleM.getRandomPropertyValue(randomGenerator);
			double doubleValI = doubleI.getRandomPropertyValue(randomGenerator);
			boolean booleanValM = booleanM.getRandomPropertyValue(randomGenerator);
			boolean booleanValI = booleanI.getRandomPropertyValue(randomGenerator);

			assertNotNull(intValM);
			assertNotNull(intValI);
			assertNotNull(doubleValM);
			assertNotNull(doubleValI);
			assertNotNull(booleanValM);
			assertNotNull(booleanValI);

			assertTrue(integers.add(intValM));
			assertTrue(integers.add(intValI));
			assertTrue(doubles.add(doubleValM));
			assertTrue(doubles.add(doubleValI));

			// this is needed because realistically even though it is random, boolean is
			// either true or false
			assertFalse = booleans.contains(booleanValM);
			if (assertFalse) {
				assertFalse(booleans.add(booleanValM));
			} else {
				assertTrue(booleans.add(booleanValM));
			}
			assertFalse = booleans.contains(booleanValI);
			if (assertFalse) {
				assertFalse(booleans.add(booleanValI));
			} else {
				assertTrue(booleans.add(booleanValI));
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
		assertTrue(!actualValues.isEmpty());
		assertEquals(expectedValues, actualValues);
	}

	@Test
	@UnitTestMethod(name = "getPropertesWithoutDefaultValues", args = {})
	public void testGetPropertesWithoutDefaultValues() {
		List<TestRegionPropertyId> expectedValues = Arrays.asList(TestRegionPropertyId.REGION_PROPERTY_2_INTEGER_MUTABLE);
		List<TestRegionPropertyId> actualValues = TestRegionPropertyId.getPropertesWithoutDefaultValues();

		assertNotNull(actualValues);
		assertTrue(!actualValues.isEmpty());
		assertEquals(expectedValues, actualValues);
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
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(7346795875603186755L);

		List<TestRegionPropertyId> applicableValues = Arrays.asList(TestRegionPropertyId.values());

		for (int i = 0; i < applicableValues.size(); i++) {
			TestRegionPropertyId actualValue = TestRegionPropertyId.getRandomRegionPropertyId(randomGenerator);

			assertNotNull(actualValue);
			assertTrue(applicableValues.contains(actualValue));
		}
	}
}
