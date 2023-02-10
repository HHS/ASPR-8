package plugins.resources.testsupport;

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

import plugins.resources.support.ResourcePropertyId;
import plugins.util.properties.PropertyDefinition;
import util.annotations.UnitTestMethod;
import util.random.RandomGeneratorProvider;
import util.wrappers.MutableInteger;

public class AT_TestResourcePropertyId {

	@Test
	@UnitTestMethod(target = TestResourcePropertyId.class, name = "getPropertyDefinition", args = {})
	public void testGetPropertyDefinition() {
		for (TestResourcePropertyId testResourcePropertyId : TestResourcePropertyId.values()) {
			assertNotNull(testResourcePropertyId.getPropertyDefinition());
		}
	}

	@Test
	@UnitTestMethod(target = TestResourcePropertyId.class, name = "getTestResourceId", args = {})
	public void testGetTestResourceId() {
		for (TestResourcePropertyId testResourcePropertyId : TestResourcePropertyId.values()) {
			assertNotNull(testResourcePropertyId.getTestResourceId());
		}
	}

	@Test
	@UnitTestMethod(target = TestResourcePropertyId.class, name = "getUnknownResourcePropertyId", args = {})
	public void testGetUnknownResourcePropertyId() {
		Set<TestResourcePropertyId> oldIds = EnumSet.allOf(TestResourcePropertyId.class);
		Set<ResourcePropertyId> unknownIds = new LinkedHashSet<>();

		// show that each unknown id is not null and unique
		for (int i = 1; i < 100; i++) {
			ResourcePropertyId unknownResourcePropertyId = TestResourcePropertyId.getUnknownResourcePropertyId();
			assertNotNull(unknownResourcePropertyId);
			boolean unique = unknownIds.add(unknownResourcePropertyId);
			assertTrue(unique);
			assertFalse(oldIds.contains(unknownResourcePropertyId));
		}
	}

	@Test
	@UnitTestMethod(target = TestResourcePropertyId.class, name = "getTestResourcePropertyIds", args = { TestResourceId.class })
	public void testGetTestResourcePropertyIds() {

		for (TestResourceId testResourceId : TestResourceId.values()) {
			Set<TestResourcePropertyId> expectedIds = new LinkedHashSet<>();
			for (TestResourcePropertyId testResourcePropertyId : TestResourcePropertyId.values()) {
				if (testResourceId.equals(testResourcePropertyId.getTestResourceId())) {
					expectedIds.add(testResourcePropertyId);
				}
			}
			Set<TestResourcePropertyId> actualIds = TestResourcePropertyId.getTestResourcePropertyIds(testResourceId);
			assertEquals(actualIds, expectedIds);
		}
	}

	@Test
	@UnitTestMethod(target = TestResourcePropertyId.class, name = "getRandomResourcePropertyId", args = { TestResourceId.class, RandomGenerator.class })
	public void testGetRandomResourcePropertyId() {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(7615402310345074403L);

		for (TestResourceId testResourceId : TestResourceId.values()) {

			// gather the expected test resource property id values for each
			// given test resource
			Set<TestResourcePropertyId> expectedTestResourcePropertyIds = new LinkedHashSet<>();
			for (TestResourcePropertyId testResourcePropertyId : TestResourcePropertyId.values()) {
				if (testResourcePropertyId.getTestResourceId().equals(testResourceId)) {
					expectedTestResourcePropertyIds.add(testResourcePropertyId);
				}
			}

			// initialize the property id counter to zeros
			Map<TestResourcePropertyId, MutableInteger> propertyIdCounter = new LinkedHashMap<>();
			for (TestResourcePropertyId testResourcePropertyId : expectedTestResourcePropertyIds) {
				propertyIdCounter.put(testResourcePropertyId, new MutableInteger());
			}

			// sample a reasonable number of invocations
			int sampleCount = 10 * expectedTestResourcePropertyIds.size();
			for (int i = 0; i < sampleCount; i++) {
				TestResourcePropertyId testResourcePropertyId = TestResourcePropertyId.getRandomResourcePropertyId(testResourceId, randomGenerator);
				assertTrue(expectedTestResourcePropertyIds.contains(testResourcePropertyId));
				propertyIdCounter.get(testResourcePropertyId).increment();
			}

			// show that we get a reasonable number of matches to each resource
			// property id
			for (TestResourcePropertyId testResourcePropertyId : propertyIdCounter.keySet()) {
				MutableInteger mutableInteger = propertyIdCounter.get(testResourcePropertyId);
				int value = mutableInteger.getValue();
				assertTrue(value >= 5 && value <= 20);
			}

		}
	}

	@Test
	@UnitTestMethod(target = TestResourcePropertyId.class, name = "getRandomPropertyValue", args = { RandomGenerator.class })
	public void testGetRandomPropertyValue() {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(7615402310345074403L);
		/*
		 * Show that randomly generated values are compatible with the
		 * associated property definition. Show that the values are reasonably
		 * unique
		 */
		for (TestResourcePropertyId testResourcePropertyId : TestResourcePropertyId.values()) {
			PropertyDefinition propertyDefinition = testResourcePropertyId.getPropertyDefinition();
			Set<Object> values = new LinkedHashSet<>();
			for (int i = 0; i < 100; i++) {
				Object propertyValue = testResourcePropertyId.getRandomPropertyValue(randomGenerator);
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

}