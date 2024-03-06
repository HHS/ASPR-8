package gov.hhs.aspr.ms.gcm.plugins.globalproperties.testsupport;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.apache.commons.math3.random.RandomGenerator;
import org.junit.jupiter.api.Test;

import gov.hhs.aspr.ms.gcm.plugins.globalproperties.support.GlobalPropertyId;
import gov.hhs.aspr.ms.gcm.plugins.properties.support.PropertyDefinition;
import gov.hhs.aspr.ms.util.annotations.UnitTestMethod;
import gov.hhs.aspr.ms.util.random.RandomGeneratorProvider;
import gov.hhs.aspr.ms.util.wrappers.MutableInteger;

public class AT_TestGlobalPropertyId {

	@Test
	@UnitTestMethod(target = TestGlobalPropertyId.class,name = "getRandomGlobalPropertyId", args = {RandomGenerator.class})
	public void testGetRandomGlobalPropertyId() {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(242770195073333036L);
		Map<TestGlobalPropertyId, MutableInteger> idCounter = new LinkedHashMap<>();
		Set<GlobalPropertyId> setOfRandomIds = new LinkedHashSet<>();
		for (TestGlobalPropertyId testGlobalPropertyId : TestGlobalPropertyId.values()) {
			idCounter.put(testGlobalPropertyId, new MutableInteger());
		}

		// show that generated values are reasonably unique
		for (int i = 0; i < 600; i++) {
			TestGlobalPropertyId globalPropertyId = TestGlobalPropertyId.getRandomGlobalPropertyId(randomGenerator);
			setOfRandomIds.add(globalPropertyId);
			idCounter.get(globalPropertyId).increment();
		}

		for (TestGlobalPropertyId propertyId : idCounter.keySet()) {
			assertTrue(idCounter.get(propertyId).getValue() >= 30 && idCounter.get(propertyId).getValue() <= 150);
		}

		assertEquals(idCounter.values().stream().mapToInt(a -> a.getValue()).sum(), 600);
		assertEquals(setOfRandomIds.size(), 6);
	}

	@Test
	@UnitTestMethod(target = TestGlobalPropertyId.class,name = "getRandomMutableGlobalPropertyId", args = {RandomGenerator.class})
	public void testGetRandomMutableGlobalPropertyId() {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(6104930304058715301L);
		Map<TestGlobalPropertyId, MutableInteger> idCounter = new LinkedHashMap<>();
		Set<GlobalPropertyId> setOfRandomMutableIds = new LinkedHashSet<>();

		for (TestGlobalPropertyId testGlobalPropertyId : TestGlobalPropertyId.values()) {
			if (testGlobalPropertyId.getPropertyDefinition().propertyValuesAreMutable()) {
				idCounter.put(testGlobalPropertyId, new MutableInteger());
			}
		}

		// show that generated values are reasonably unique
		for (int i = 0; i < 300; i++){
			TestGlobalPropertyId mutableGlobalPropertyId = TestGlobalPropertyId.getRandomMutableGlobalPropertyId(randomGenerator);
			setOfRandomMutableIds.add(mutableGlobalPropertyId);
			assertTrue(mutableGlobalPropertyId.getPropertyDefinition().propertyValuesAreMutable());
			idCounter.get(mutableGlobalPropertyId).increment();
		}

		for (TestGlobalPropertyId propertyId : idCounter.keySet()) {
			assertTrue(idCounter.get(propertyId).getValue() >= 30 && idCounter.get(propertyId).getValue() <= 150);
		}

		assertEquals(idCounter.values().stream().mapToInt(a -> a.getValue()).sum(), 300);
		assertEquals(setOfRandomMutableIds.size(), 3);
	}

	@Test
	@UnitTestMethod(target = TestGlobalPropertyId.class,name = "getPropertyDefinition", args = {})
	public void testGetPropertyDefinition() {
		for (TestGlobalPropertyId testGlobalPropertyId : TestGlobalPropertyId.values()) {
			assertNotNull(testGlobalPropertyId.getPropertyDefinition());
		}
	}

	@Test
	@UnitTestMethod(target = TestGlobalPropertyId.class,name = "getRandomPropertyValue", args = { RandomGenerator.class })
	public void testGetRandomPropertyValue() {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(3456870569545355468L);

		/*
		 * Show that randomly generated values are compatible with the
		 * associated property definition. Show that the values are reasonably
		 * unique
		 */
		for (TestGlobalPropertyId testGlobalPropertyId : TestGlobalPropertyId.values()) {
			PropertyDefinition propertyDefinition = testGlobalPropertyId.getPropertyDefinition();
			Set<Object> values = new LinkedHashSet<>();
			for (int i = 0; i < 100; i++) {
				Object propertyValue = testGlobalPropertyId.getRandomPropertyValue(randomGenerator);
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
	@UnitTestMethod(target = TestGlobalPropertyId.class,name = "getUnknownGlobalPropertyId", args = {})
	public void testGetUnknownRegionId() {
		Set<GlobalPropertyId> unknownGlobalPropertyIds = new LinkedHashSet<>();
		for (int i = 0; i < 30; i++) {
			GlobalPropertyId unknownGlobalPropertyId = TestGlobalPropertyId.getUnknownGlobalPropertyId();
			assertNotNull(unknownGlobalPropertyId);
			boolean unique = unknownGlobalPropertyIds.add(unknownGlobalPropertyId);
			assertTrue(unique);
			for (TestGlobalPropertyId testGlobalPropertyId : TestGlobalPropertyId.values()) {
				assertNotEquals(testGlobalPropertyId, unknownGlobalPropertyId);
			}
		}
	}

    @Test
    @UnitTestMethod(target = TestGlobalPropertyId.class, name = "getGlobalPropertyIds", args={})
    public void testGetGlobalPropertyIds() {
        assertEquals(Arrays.asList(TestGlobalPropertyId.values()), TestGlobalPropertyId.getGlobalPropertyIds());
    }

    @Test
    @UnitTestMethod(target = TestGlobalPropertyId.class, name = "getShuffledGlobalPropertyIds", args={RandomGenerator.class})
    public void testGetShuffledGlobalPropertyIds() {
        RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(503706897966130759L);
        List<TestGlobalPropertyId> ids = TestGlobalPropertyId.getGlobalPropertyIds();
        Collections.shuffle(ids, new Random(randomGenerator.nextLong()));

        assertEquals(ids, TestGlobalPropertyId.getShuffledGlobalPropertyIds(RandomGeneratorProvider.getRandomGenerator(503706897966130759L)));
    }
	
}
