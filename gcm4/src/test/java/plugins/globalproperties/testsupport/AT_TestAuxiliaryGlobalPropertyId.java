package plugins.globalproperties.testsupport;

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

import plugins.globalproperties.support.GlobalPropertyId;
import plugins.util.properties.PropertyDefinition;
import util.annotations.UnitTestMethod;
import util.random.RandomGeneratorProvider;
import util.wrappers.MutableInteger;

public class AT_TestAuxiliaryGlobalPropertyId {

	@Test
	@UnitTestMethod(target = TestAuxiliaryGlobalPropertyId.class, name = "getRandomGlobalPropertyId", args = { RandomGenerator.class })
	public void testGetRandomGlobalPropertyId() {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(5005107416828888981L);
		Map<TestAuxiliaryGlobalPropertyId, MutableInteger> idCounter = new LinkedHashMap<>();
		Set<TestAuxiliaryGlobalPropertyId> hashSetOfRandomIds = new LinkedHashSet<>();

		for (TestAuxiliaryGlobalPropertyId testAuxiliaryGlobalPropertyId : TestAuxiliaryGlobalPropertyId.values()) {
			idCounter.put(testAuxiliaryGlobalPropertyId, new MutableInteger());
		}

		// show that generated values are reasonably unique
		for (int i = 0; i < 600; i++) {
			TestAuxiliaryGlobalPropertyId testAuxiliaryGlobalPropertyId = TestAuxiliaryGlobalPropertyId.getRandomGlobalPropertyId(randomGenerator);
			hashSetOfRandomIds.add(testAuxiliaryGlobalPropertyId);
			idCounter.get(testAuxiliaryGlobalPropertyId).increment();
		}
		for (TestAuxiliaryGlobalPropertyId propertyId : idCounter.keySet()) {
			assertTrue(idCounter.get(propertyId).getValue() >= 30 && idCounter.get(propertyId).getValue() <= 150);
		}

		assertEquals(idCounter.values().stream().mapToInt(a -> a.getValue()).sum(), 600);
		assertEquals(hashSetOfRandomIds.size(), 6);

	}

	@Test
	@UnitTestMethod(target = TestAuxiliaryGlobalPropertyId.class, name = "getRandomPropertyValue", args = { RandomGenerator.class })
	public void testGetRandomPropertyValue() {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(6173923848365818813L);
		/*
		 * Show that randomly generated values are compatible with the
		 * associated property definition. Show that the values are reasonably
		 * unique
		 */
		for (TestAuxiliaryGlobalPropertyId testAuxiliaryGlobalPropertyId : TestAuxiliaryGlobalPropertyId.values()) {
			PropertyDefinition propertyDefinition = testAuxiliaryGlobalPropertyId.getPropertyDefinition();
			Set<Object> values = new LinkedHashSet<>();
			for (int i = 0; i < 100; i++) {
				Object propertyValue = testAuxiliaryGlobalPropertyId.getRandomPropertyValue(randomGenerator);
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
	@UnitTestMethod(target = TestAuxiliaryGlobalPropertyId.class, name = "getPropertyDefinition", args = {})
	public void testGetPropertyDefinition() {
		for (TestAuxiliaryGlobalPropertyId testAuxiliaryGlobalPropertyId : TestAuxiliaryGlobalPropertyId.values()) {
			assertNotNull(testAuxiliaryGlobalPropertyId.getPropertyDefinition());
		}
	}

	@Test
	@UnitTestMethod(target = TestAuxiliaryGlobalPropertyId.class, name = "getUnknownGlobalPropertyId", args = {})
	public void testGetUnknownGlobalPropertyId() {
		/*
		 * Shows that a generated unknown group property id is unique, not null
		 * and not a member of the enum
		 */
		Set<TestAuxiliaryGlobalPropertyId> testProperties = EnumSet.allOf(TestAuxiliaryGlobalPropertyId.class);
		Set<GlobalPropertyId> unknownGroupPropertyIds = new LinkedHashSet<>(); // ????
		for (int i = 0; i < 30; i++) {
			GlobalPropertyId unknownGlobalPropertyId = TestAuxiliaryGlobalPropertyId.getUnknownGlobalPropertyId();
			assertNotNull(unknownGlobalPropertyId);
			boolean unique = unknownGroupPropertyIds.add(unknownGlobalPropertyId);
			assertTrue(unique);
			assertFalse(testProperties.contains(unknownGlobalPropertyId));
		}
	}
}