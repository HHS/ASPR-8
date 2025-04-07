package gov.hhs.aspr.ms.gcm.simulation.plugins.regions.support;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.LinkedHashSet;
import java.util.Set;

import org.apache.commons.math3.random.RandomGenerator;
import org.junit.jupiter.api.Test;

import gov.hhs.aspr.ms.util.annotations.UnitTestConstructor;
import gov.hhs.aspr.ms.util.annotations.UnitTestMethod;
import gov.hhs.aspr.ms.util.random.RandomGeneratorProvider;

public class AT_SimpleRegionPropertyId {
	@Test
	@UnitTestMethod(target = SimpleRegionPropertyId.class, name = "getValue", args = {})
	public void testGetValue() {

		Object value = "some value";
		SimpleRegionPropertyId simpleRegionPropertyId = new SimpleRegionPropertyId(value);
		assertEquals(value, simpleRegionPropertyId.getValue());
		
		value = 678;
		simpleRegionPropertyId = new SimpleRegionPropertyId(value);
		assertEquals(value, simpleRegionPropertyId.getValue());
		
		
		value = false;
		simpleRegionPropertyId = new SimpleRegionPropertyId(value);
		assertEquals(value, simpleRegionPropertyId.getValue());
		
		value = 2.98;
		simpleRegionPropertyId = new SimpleRegionPropertyId(value);
		assertEquals(value, simpleRegionPropertyId.getValue());
		

	}
	@Test
	@UnitTestConstructor(target = SimpleRegionPropertyId.class, args = { Object.class })
	public void testConstructor() {
		assertNotNull(new SimpleRegionPropertyId(5));

		assertThrows(NullPointerException.class, () -> new SimpleRegionPropertyId(null));
	}

	@Test
	@UnitTestMethod(target = SimpleRegionPropertyId.class, name = "toString", args = {})
	public void testToString() {
		/*
		 * Show that the toString of the SimpleRegionPropertyId equals its
		 * input's toString
		 */

		assertEquals(Integer.toString(5), new SimpleRegionPropertyId(5).toString());
		assertEquals("table", new SimpleRegionPropertyId("table").toString());
		assertEquals(Double.toString(2345.5345), new SimpleRegionPropertyId(2345.5345).toString());

	}

	@Test
	@UnitTestMethod(target = SimpleRegionPropertyId.class, name = "equals", args = { Object.class })
	public void testEquals() {
        RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(8980993493557306496L);
        
        // never equal to another type
		for (int i = 0; i < 30; i++) {
            SimpleRegionPropertyId simpleRegionPropertyId = getRandomSimpleRegionPropertyId(randomGenerator.nextLong());
            assertFalse(simpleRegionPropertyId.equals(new Object()));
		}

		// never equal to null
		for (int i = 0; i < 30; i++) {
			SimpleRegionPropertyId simpleRegionPropertyId = getRandomSimpleRegionPropertyId(randomGenerator.nextLong());
			assertFalse(simpleRegionPropertyId.equals(null));
		}

		// reflexive
		for (int i = 0; i < 30; i++) {
			SimpleRegionPropertyId simpleRegionPropertyId = getRandomSimpleRegionPropertyId(randomGenerator.nextLong());
			assertTrue(simpleRegionPropertyId.equals(simpleRegionPropertyId));
		}

		// symmetric, transitive, consistent
		for (int i = 0; i < 30; i++) {
			long seed = randomGenerator.nextLong();
			SimpleRegionPropertyId simpleRegionPropertyId1 = getRandomSimpleRegionPropertyId(seed);
			SimpleRegionPropertyId simpleRegionPropertyId2 = getRandomSimpleRegionPropertyId(seed);
			assertFalse(simpleRegionPropertyId1 == simpleRegionPropertyId2);
			for (int j = 0; j < 10; j++) {				
				assertTrue(simpleRegionPropertyId1.equals(simpleRegionPropertyId2));
				assertTrue(simpleRegionPropertyId2.equals(simpleRegionPropertyId1));
			}
		}

		// different inputs yield unequal SimpleRegionPropertyIds
		Set<SimpleRegionPropertyId> set = new LinkedHashSet<>();
		for (int i = 0; i < 100; i++) {
			SimpleRegionPropertyId simpleRegionPropertyId = getRandomSimpleRegionPropertyId(randomGenerator.nextLong());
			set.add(simpleRegionPropertyId);
		}
		assertEquals(100, set.size());
	}

	@Test
	@UnitTestMethod(target = SimpleRegionPropertyId.class, name = "hashCode", args = {})
	public void testHashCode() {
        RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(2274930019926275913L);

        // equal objects have equal hash codes
        for (int i = 0; i < 30; i++) {
            long seed = randomGenerator.nextLong();
			SimpleRegionPropertyId simpleRegionPropertyId1 = getRandomSimpleRegionPropertyId(seed);
			SimpleRegionPropertyId simpleRegionPropertyId2 = getRandomSimpleRegionPropertyId(seed);

            assertEquals(simpleRegionPropertyId1, simpleRegionPropertyId2);
            assertEquals(simpleRegionPropertyId1.hashCode(), simpleRegionPropertyId2.hashCode());
        }

        // hash codes are reasonably distributed
        Set<Integer> hashCodes = new LinkedHashSet<>();
        for (int i = 0; i < 100; i++) {
            SimpleRegionPropertyId simpleGlobalPropertyId = getRandomSimpleRegionPropertyId(randomGenerator.nextLong());
            hashCodes.add(simpleGlobalPropertyId.hashCode());
        }

        assertEquals(100, hashCodes.size());
	}

	private SimpleRegionPropertyId getRandomSimpleRegionPropertyId(long seed) {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(seed);
		return new SimpleRegionPropertyId(randomGenerator.nextInt());
	}
}
