package plugins.personproperties.testsupport;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.math3.random.RandomGenerator;
import org.junit.jupiter.api.Test;

import plugins.personproperties.support.PersonPropertyId;
import plugins.util.properties.PropertyDefinition;
import tools.annotations.UnitTest;
import tools.annotations.UnitTestMethod;
import util.random.RandomGeneratorProvider;
import util.wrappers.MutableInteger;

@UnitTest(target = TestPersonPropertyId.class)
public class AT_TestPersonPropertyId implements PersonPropertyId {

	@Test
	@UnitTestMethod(name = "getRandomPersonPropertyId", args = { RandomGenerator.class })
	public void testGetRandomPersonPropertyId() {
		Map<TestPersonPropertyId, MutableInteger> countMap = new LinkedHashMap<>();
		for (TestPersonPropertyId testPersonPropertyId : TestPersonPropertyId.values()) {
			countMap.put(testPersonPropertyId, new MutableInteger());
		}
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(5471359242434395756L);
		int sampleCount = 1000;
		for (int i = 0; i < sampleCount; i++) {
			TestPersonPropertyId randomPersonPropertyId = TestPersonPropertyId.getRandomPersonPropertyId(randomGenerator);
			assertNotNull(randomPersonPropertyId);
			countMap.get(randomPersonPropertyId).increment();
		}

		int minCount = sampleCount / TestPersonPropertyId.values().length;
		minCount *= 4;
		minCount /= 5;
		for (TestPersonPropertyId testPersonPropertyId : TestPersonPropertyId.values()) {
			assertTrue(countMap.get(testPersonPropertyId).getValue() > minCount);
		}
	}

	@Test
	@UnitTestMethod(name = "getRandomPropertyValue", args = { RandomGenerator.class })
	public void testGetRandomPropertyValue() {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(8507625070108242089L);

		/*
		 * Show that randomly generated values are compatible with the
		 * associated property definition. Show that the values are reasonably
		 * unique
		 */
		for (TestPersonPropertyId testPersonPropertyId : TestPersonPropertyId.values()) {
			PropertyDefinition propertyDefinition = testPersonPropertyId.getPropertyDefinition();
			Set<Object> values = new LinkedHashSet<>();
			for (int i = 0; i < 100; i++) {
				Object propertyValue = testPersonPropertyId.getRandomPropertyValue(randomGenerator);
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
	@UnitTestMethod(name = "getPropertyDefinition", args = {})
	public void testGetPropertyDefinition() {
		for (TestPersonPropertyId testPersonPropertyId : TestPersonPropertyId.values()) {
			PropertyDefinition propertyDefinition = testPersonPropertyId.getPropertyDefinition();
			assertNotNull(propertyDefinition);
		}
	}

	@Test
	@UnitTestMethod(name = "getUnknownPersonPropertyId", args = {})
	public void testGetUnknownPersonPropertyId() {
		/*
		 * Shows that a generated unknown person property id is unique, not null
		 * and not a member of the enum
		 */
		Set<TestPersonPropertyId> testProperties = EnumSet.allOf(TestPersonPropertyId.class);
		Set<PersonPropertyId> unknownPersonPropertyIds = new LinkedHashSet<>();
		for (int i = 0; i < 30; i++) {
			PersonPropertyId unknownPersonPropertyId = TestPersonPropertyId.getUnknownPersonPropertyId();
			assertNotNull(unknownPersonPropertyId);
			boolean unique = unknownPersonPropertyIds.add(unknownPersonPropertyId);
			assertTrue(unique);
			assertFalse(testProperties.contains(unknownPersonPropertyId));
		}
	}

	@Test
	@UnitTestMethod(name = "getPropertiesWithDefaultValues", args = {})
	public void testGetPropertiesWithDefaultValues() {
		List<TestPersonPropertyId> expectedValues = Arrays.asList(
				TestPersonPropertyId.PERSON_PROPERTY_1_BOOLEAN_MUTABLE_NO_TRACK,
				TestPersonPropertyId.PERSON_PROPERTY_2_INTEGER_MUTABLE_NO_TRACK,
				TestPersonPropertyId.PERSON_PROPERTY_3_DOUBLE_MUTABLE_NO_TRACK,
				TestPersonPropertyId.PERSON_PROPERTY_4_BOOLEAN_MUTABLE_TRACK,
				TestPersonPropertyId.PERSON_PROPERTY_5_INTEGER_MUTABLE_TRACK,
				TestPersonPropertyId.PERSON_PROPERTY_6_DOUBLE_MUTABLE_TRACK,
				TestPersonPropertyId.PERSON_PROPERTY_7_BOOLEAN_IMMUTABLE_NO_TRACK,
				TestPersonPropertyId.PERSON_PROPERTY_8_INTEGER_IMMUTABLE_NO_TRACK);

		List<TestPersonPropertyId> actualValues = TestPersonPropertyId.getPropertiesWithDefaultValues();

		assertNotNull(actualValues);
		assertEquals(expectedValues.size(), actualValues.size());
		Set<TestPersonPropertyId> setOfExpectedValues = new LinkedHashSet<>(expectedValues);
		Set<TestPersonPropertyId> setOfActualValues = new LinkedHashSet<>(actualValues);
		assertEquals(setOfExpectedValues, setOfActualValues);
		assertEquals(expectedValues.size(), setOfExpectedValues.size());
		assertEquals(actualValues.size(), setOfActualValues.size());
	}

	@Test
	@UnitTestMethod(name = "getPropertiesWithoutDefaultValues", args = {})
	public void testGetPropertiesWithoutDefaultValues() {
		List<TestPersonPropertyId> expectedValues = Arrays
				.asList(TestPersonPropertyId.PERSON_PROPERTY_9_DOUBLE_IMMUTABLE_NO_TRACK);
		List<TestPersonPropertyId> actualValues = TestPersonPropertyId.getPropertiesWithoutDefaultValues();

		assertNotNull(actualValues);
		assertEquals(expectedValues.size(), actualValues.size());
		Set<TestPersonPropertyId> setOfExpectedValues = new LinkedHashSet<>(expectedValues);
		Set<TestPersonPropertyId> setOfActualValues = new LinkedHashSet<>(actualValues);
		assertEquals(setOfExpectedValues, setOfActualValues);
		assertEquals(expectedValues.size(), setOfExpectedValues.size());
		assertEquals(actualValues.size(), setOfActualValues.size());
	}
}