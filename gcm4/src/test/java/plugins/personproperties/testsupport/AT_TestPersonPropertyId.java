package plugins.personproperties.testsupport;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
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
import util.annotations.UnitTestMethod;
import util.random.RandomGeneratorProvider;
import util.wrappers.MutableInteger;

public class AT_TestPersonPropertyId implements PersonPropertyId {

	@Test
	@UnitTestMethod(target = TestPersonPropertyId.class, name = "getRandomPersonPropertyId", args = {
			RandomGenerator.class })
	public void testGetRandomPersonPropertyId() {
		Map<TestPersonPropertyId, MutableInteger> countMap = new LinkedHashMap<>();
		for (TestPersonPropertyId testPersonPropertyId : TestPersonPropertyId.values()) {
			countMap.put(testPersonPropertyId, new MutableInteger());
		}
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(5471359242434395756L);
		int sampleCount = 1000;
		for (int i = 0; i < sampleCount; i++) {
			TestPersonPropertyId randomPersonPropertyId = TestPersonPropertyId
					.getRandomPersonPropertyId(randomGenerator);
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
	@UnitTestMethod(target = TestPersonPropertyId.class, name = "getRandomPropertyValue", args = {
			RandomGenerator.class })
	public void testGetRandomPropertyValue() {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(8507625070108242089L);

		/*
		 * Show that randomly generated values are compatible with the associated
		 * property definition. Show that the values are reasonably unique
		 */
		for (TestPersonPropertyId testPersonPropertyId : TestPersonPropertyId.values()) {
			PropertyDefinition propertyDefinition = testPersonPropertyId.getPropertyDefinition();
			Set<Object> values = new LinkedHashSet<>();
			for (int i = 0; i < 100; i++) {
				Object propertyValue = testPersonPropertyId.getRandomPropertyValue(randomGenerator);
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
	@UnitTestMethod(target = TestPersonPropertyId.class, name = "getPropertyDefinition", args = {})
	public void testGetPropertyDefinition() {
		for (TestPersonPropertyId testPersonPropertyId : TestPersonPropertyId.values()) {
			PropertyDefinition propertyDefinition = testPersonPropertyId.getPropertyDefinition();
			assertNotNull(propertyDefinition);
		}
	}

	@Test
	@UnitTestMethod(target = TestPersonPropertyId.class, name = "getUnknownPersonPropertyId", args = {})
	public void testGetUnknownPersonPropertyId() {
		/*
		 * Shows that a generated unknown person property id is unique, not null and not
		 * a member of the enum
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
	@UnitTestMethod(target = TestPersonPropertyId.class, name = "getPropertiesWithDefaultValues", args = {})
	public void testGetPropertiesWithDefaultValues() {
		List<TestPersonPropertyId> expectedValues = new ArrayList<>();

		for (TestPersonPropertyId id : TestPersonPropertyId.values()) {
			if (id.getPropertyDefinition().getDefaultValue().isPresent()) {
				expectedValues.add(id);
			}
		}

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
	@UnitTestMethod(target = TestPersonPropertyId.class, name = "getPropertiesWithoutDefaultValues", args = {})
	public void testGetPropertiesWithoutDefaultValues() {
		List<TestPersonPropertyId> expectedValues = new ArrayList<>();

		for (TestPersonPropertyId id : TestPersonPropertyId.values()) {
			if (id.getPropertyDefinition().getDefaultValue().isEmpty()) {
				expectedValues.add(id);
			}
		}

		List<TestPersonPropertyId> actualValues = TestPersonPropertyId.getPropertiesWithoutDefaultValues();

		assertNotNull(actualValues);
		assertEquals(expectedValues.size(), actualValues.size());
		Set<TestPersonPropertyId> setOfExpectedValues = new LinkedHashSet<>(expectedValues);
		Set<TestPersonPropertyId> setOfActualValues = new LinkedHashSet<>(actualValues);
		assertEquals(setOfExpectedValues, setOfActualValues);
		assertEquals(expectedValues.size(), setOfExpectedValues.size());
		assertEquals(actualValues.size(), setOfActualValues.size());
	}

	@Test
	@UnitTestMethod(target = TestPersonPropertyId.class, name = "next", args = {})
	public void testNext() {
		for (TestPersonPropertyId testPersonPropertyId : TestPersonPropertyId.values()) {
			int index = testPersonPropertyId.ordinal();
			index += 1;
			index %= TestPersonPropertyId.values().length;
			TestPersonPropertyId expectedNextTestPersonPropertyId = TestPersonPropertyId.values()[index];
			assertEquals(expectedNextTestPersonPropertyId, testPersonPropertyId.next());
		}
	}

	@Test
	@UnitTestMethod(target = TestPersonPropertyId.class, name = "getPersonPropertyIds", args = {})
	public void testGetPersonPropertyIds() {
		assertEquals(Arrays.asList(TestPersonPropertyId.values()), TestPersonPropertyId.getPersonPropertyIds());
	}

	@Test
	@UnitTestMethod(target = TestPersonPropertyId.class, name = "isTimeTracked", args = {})
	public void testIsTimeTracked() {
		assertEquals(9, TestPersonPropertyId.values().length);
		assertFalse(TestPersonPropertyId.PERSON_PROPERTY_1_BOOLEAN_MUTABLE_NO_TRACK.isTimeTracked());
		assertFalse(TestPersonPropertyId.PERSON_PROPERTY_2_INTEGER_MUTABLE_NO_TRACK.isTimeTracked());
		assertFalse(TestPersonPropertyId.PERSON_PROPERTY_3_DOUBLE_MUTABLE_NO_TRACK.isTimeTracked());
		assertTrue(TestPersonPropertyId.PERSON_PROPERTY_4_BOOLEAN_MUTABLE_TRACK.isTimeTracked());
		assertTrue(TestPersonPropertyId.PERSON_PROPERTY_5_INTEGER_MUTABLE_TRACK.isTimeTracked());
		assertTrue(TestPersonPropertyId.PERSON_PROPERTY_6_DOUBLE_MUTABLE_TRACK.isTimeTracked());
		assertFalse(TestPersonPropertyId.PERSON_PROPERTY_7_BOOLEAN_IMMUTABLE_NO_TRACK.isTimeTracked());
		assertFalse(TestPersonPropertyId.PERSON_PROPERTY_8_INTEGER_IMMUTABLE_NO_TRACK.isTimeTracked());
		assertFalse(TestPersonPropertyId.PERSON_PROPERTY_9_DOUBLE_IMMUTABLE_NO_TRACK.isTimeTracked());
	}

	@Test
	@UnitTestMethod(target = TestPersonPropertyId.class, name = "getShuffledPersonPropertyIds", args = {
			RandomGenerator.class })
	public void testGetShuffledPersonPropertyIds() {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(687867202081341049L);

		Set<TestPersonPropertyId> baseSet = new LinkedHashSet<>();
		for (TestPersonPropertyId testPersonPropertyId : TestPersonPropertyId.values()) {
			baseSet.add(testPersonPropertyId);
		}
		//There are 9!=362880 possible lists.  The probability of collision is very low.
		Set<List<TestPersonPropertyId>> lists = new LinkedHashSet<>();
		for (int i = 0; i < 1000; i++) {
			List<TestPersonPropertyId> shuffledPersonPropertyIds = TestPersonPropertyId
					.getShuffledPersonPropertyIds(randomGenerator);
			
			lists.add(shuffledPersonPropertyIds);
			
			Set<TestPersonPropertyId> set = new LinkedHashSet<>(shuffledPersonPropertyIds);
			assertEquals(baseSet, set);
		}
		
		assertEquals(1000,lists.size());
	}

}