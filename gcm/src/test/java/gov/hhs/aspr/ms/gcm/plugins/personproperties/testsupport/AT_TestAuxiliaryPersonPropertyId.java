package gov.hhs.aspr.ms.gcm.plugins.personproperties.testsupport;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.math3.random.RandomGenerator;
import org.junit.jupiter.api.Test;

import gov.hhs.aspr.ms.gcm.plugins.personproperties.support.PersonPropertyId;
import gov.hhs.aspr.ms.gcm.plugins.util.properties.PropertyDefinition;
import util.annotations.UnitTestMethod;
import util.random.RandomGeneratorProvider;
import util.wrappers.MutableInteger;

public class AT_TestAuxiliaryPersonPropertyId {
	@Test
	@UnitTestMethod(target = TestAuxiliaryPersonPropertyId.class, name = "getRandomPersonPropertyId", args = { RandomGenerator.class })
	public void testGetRandomPersonPropertyId() {
		Map<TestAuxiliaryPersonPropertyId, MutableInteger> countMap = new LinkedHashMap<>();
		for (TestAuxiliaryPersonPropertyId testPersonPropertyId : TestAuxiliaryPersonPropertyId.values()) {
			countMap.put(testPersonPropertyId, new MutableInteger());
		}
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(2864629454580072362L);
		int sampleCount = 1000;
		for (int i = 0; i < sampleCount; i++) {
			TestAuxiliaryPersonPropertyId randomPersonPropertyId = TestAuxiliaryPersonPropertyId.getRandomPersonPropertyId(randomGenerator);
			assertNotNull(randomPersonPropertyId);
			countMap.get(randomPersonPropertyId).increment();
		}

		int minCount = sampleCount / TestAuxiliaryPersonPropertyId.values().length;
		minCount *= 4;
		minCount /= 5;
		for (TestAuxiliaryPersonPropertyId testPersonPropertyId : TestAuxiliaryPersonPropertyId.values()) {
			assertTrue(countMap.get(testPersonPropertyId).getValue() > minCount);
		}
	}

	@Test
	@UnitTestMethod(target = TestAuxiliaryPersonPropertyId.class, name = "getRandomPropertyValue", args = { RandomGenerator.class })
	public void testGetRandomPropertyValue() {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(890505028463718572L);

		/*
		 * Show that randomly generated values are compatible with the
		 * associated property definition. Show that the values are reasonably
		 * unique
		 */
		for (TestAuxiliaryPersonPropertyId testPersonPropertyId : TestAuxiliaryPersonPropertyId.values()) {
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
	@UnitTestMethod(target = TestAuxiliaryPersonPropertyId.class, name = "getPropertyDefinition", args = {})
	public void testGetPropertyDefinition() {
		for (TestAuxiliaryPersonPropertyId testPersonPropertyId : TestAuxiliaryPersonPropertyId.values()) {
			PropertyDefinition propertyDefinition = testPersonPropertyId.getPropertyDefinition();
			assertNotNull(propertyDefinition);
		}
	}

	@Test
	@UnitTestMethod(target = TestAuxiliaryPersonPropertyId.class, name = "getUnknownPersonPropertyId", args = {})
	public void testGetUnknownPersonPropertyId() {
		/*
		 * Shows that a generated unknown person property id is unique, not null
		 * and not a member of the enum
		 */
		Set<TestAuxiliaryPersonPropertyId> testProperties = EnumSet.allOf(TestAuxiliaryPersonPropertyId.class);
		Set<PersonPropertyId> unknownPersonPropertyIds = new LinkedHashSet<>();
		for (int i = 0; i < 30; i++) {
			PersonPropertyId unknownPersonPropertyId = TestAuxiliaryPersonPropertyId.getUnknownPersonPropertyId();
			assertNotNull(unknownPersonPropertyId);
			boolean unique = unknownPersonPropertyIds.add(unknownPersonPropertyId);
			assertTrue(unique);
			assertFalse(testProperties.contains(unknownPersonPropertyId));
		}
	}
}
