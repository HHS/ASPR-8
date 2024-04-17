package gov.hhs.aspr.ms.gcm.simulation.plugins.resources.testsupport;

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

import gov.hhs.aspr.ms.gcm.simulation.plugins.properties.support.PropertyDefinition;
import gov.hhs.aspr.ms.gcm.simulation.plugins.resources.support.ResourcePropertyId;
import gov.hhs.aspr.ms.util.annotations.UnitTestMethod;
import gov.hhs.aspr.ms.util.random.RandomGeneratorProvider;
import gov.hhs.aspr.ms.util.wrappers.MutableInteger;

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
	@UnitTestMethod(target = TestResourcePropertyId.class, name = "getTestResourcePropertyIds", args = {})
	public void testGetTestResourcePropertyIds() {
		assertEquals(Arrays.asList(TestResourcePropertyId.values()),
				TestResourcePropertyId.getTestResourcePropertyIds());

	}

	@Test
	@UnitTestMethod(target = TestResourcePropertyId.class, name = "getTestResourcePropertyIds", args = {
			TestResourceId.class })
	public void testGetTestResourcePropertyIds_ResourceId() {

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
	@UnitTestMethod(target = TestResourcePropertyId.class, name = "getRandomResourcePropertyId", args = {
			TestResourceId.class, RandomGenerator.class })
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
				TestResourcePropertyId testResourcePropertyId = TestResourcePropertyId
						.getRandomResourcePropertyId(testResourceId, randomGenerator);
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
	@UnitTestMethod(target = TestResourcePropertyId.class, name = "getRandomPropertyValue", args = {
			RandomGenerator.class })
	public void testGetRandomPropertyValue() {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(7615402310345074403L);
		/*
		 * Show that randomly generated values are compatible with the associated
		 * property definition. Show that the values are reasonably unique
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

	@Test
	@UnitTestMethod(target = TestResourcePropertyId.class, name = "next", args = {})
	public void testNext() {
		for (TestResourcePropertyId testResourcePropertyId : TestResourcePropertyId.values()) {
			int index = testResourcePropertyId.ordinal();
			index += 1;
			index %= TestResourcePropertyId.values().length;
			TestResourcePropertyId expectedNextTestResourcePropertyId = TestResourcePropertyId.values()[index];
			assertEquals(expectedNextTestResourcePropertyId, testResourcePropertyId.next());
		}
	}

//	TestResourcePropertyId	public static java.util.List plugins.resources.testsupport.TestResourcePropertyId.getShuffledTestResourcePropertyIds(org.apache.commons.math3.random.RandomGenerator)
	
	@Test
	@UnitTestMethod(target = TestResourcePropertyId.class, name = "getShuffledTestResourcePropertyIds", args = {RandomGenerator.class})
	public void testGetTestShuffledRegionPropertyIds() {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(8246696863539332004L);

		Set<TestResourcePropertyId> baseSet = new LinkedHashSet<>();
		for (TestResourcePropertyId testResourcePropertyId : TestResourcePropertyId.values()) {
			baseSet.add(testResourcePropertyId);
		}

		Set<List<TestResourcePropertyId>> lists = new LinkedHashSet<>();

		/*
		 * Generate a few thousand random lists and show that each list contains all the
		 * expected region property ids
		 * 
		 */
		for (int i = 0; i < 3000; i++) {
			List<TestResourcePropertyId> list = TestResourcePropertyId.getShuffledTestResourcePropertyIds(randomGenerator);
			lists.add(list);
			assertEquals(baseSet, new LinkedHashSet<>(list));

		}
		
		

		//There are 10! possible lists, so we don't expect many collsions
		assertTrue(lists.size() > 2900);
		

	}


}