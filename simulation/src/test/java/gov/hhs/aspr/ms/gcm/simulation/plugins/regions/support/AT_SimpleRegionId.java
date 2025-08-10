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

public class AT_SimpleRegionId {

	@Test
	@UnitTestConstructor(target = SimpleRegionId.class, args = { Object.class })
	public void testConstructor() {
		assertNotNull(new SimpleRegionId(5));

		assertThrows(NullPointerException.class, () -> new SimpleRegionId(null));
	}
	
	@Test
	@UnitTestMethod(target = SimpleRegionId.class, name = "getValue", args = {})
	public void testGetValue() {

		Object value = "some value";
		SimpleRegionId simpleRegionId = new SimpleRegionId(value);
		assertEquals(value, simpleRegionId.getValue());
		
		value = 678;
		simpleRegionId = new SimpleRegionId(value);
		assertEquals(value, simpleRegionId.getValue());
		
		
		value = false;
		simpleRegionId = new SimpleRegionId(value);
		assertEquals(value, simpleRegionId.getValue());
		
		value = 2.98;
		simpleRegionId = new SimpleRegionId(value);
		assertEquals(value, simpleRegionId.getValue());
		

	}
	@Test
	@UnitTestMethod(target = SimpleRegionId.class, name = "toString", args = {})
	public void testToString() {
		/*
		 * Show that the toString of the SimpleRegionId equals its input's
		 * toString
		 */

		assertEquals(Integer.toString(5), new SimpleRegionId(5).toString());
		assertEquals("table", new SimpleRegionId("table").toString());
		assertEquals(Double.toString(2345.5345), new SimpleRegionId(2345.5345).toString());

	}

	@Test
	@UnitTestMethod(target = SimpleRegionId.class, name = "equals", args = { Object.class })
	public void testEquals() {
        RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(8980223493557306496L);
        
        // never equal to another type
		for (int i = 0; i < 30; i++) {
            SimpleRegionId simpleRegionId = getRandomSimpleRegionId(randomGenerator.nextLong());
            assertFalse(simpleRegionId.equals(new Object()));
		}

		// never equal to null
		for (int i = 0; i < 30; i++) {
			SimpleRegionId simpleRegionId = getRandomSimpleRegionId(randomGenerator.nextLong());
			assertFalse(simpleRegionId.equals(null));
		}

		// reflexive
		for (int i = 0; i < 30; i++) {
			SimpleRegionId simpleRegionId = getRandomSimpleRegionId(randomGenerator.nextLong());
			assertTrue(simpleRegionId.equals(simpleRegionId));
		}

		// symmetric, transitive, consistent
		for (int i = 0; i < 30; i++) {
			long seed = randomGenerator.nextLong();
			SimpleRegionId simpleRegionId1 = getRandomSimpleRegionId(seed);
			SimpleRegionId simpleRegionId2 = getRandomSimpleRegionId(seed);
			assertFalse(simpleRegionId1 == simpleRegionId2);
			for (int j = 0; j < 10; j++) {				
				assertTrue(simpleRegionId1.equals(simpleRegionId2));
				assertTrue(simpleRegionId2.equals(simpleRegionId1));
			}
		}

		// different inputs yield unequal SimpleRegionIds
		Set<SimpleRegionId> set = new LinkedHashSet<>();
		for (int i = 0; i < 100; i++) {
			SimpleRegionId simpleRegionId = getRandomSimpleRegionId(randomGenerator.nextLong());
			set.add(simpleRegionId);
		}
		assertEquals(100, set.size());
	}

	@Test
	@UnitTestMethod(target = SimpleRegionId.class, name = "hashCode", args = {})
	public void testHashCode() {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(6496939919926275913L);

		// equal objects have equal hash codes
		for (int i = 0; i < 30; i++) {
			long seed = randomGenerator.nextLong();
			SimpleRegionId s1 = getRandomSimpleRegionId(seed);
			SimpleRegionId s2 = getRandomSimpleRegionId(seed);

			assertEquals(s1, s2);
			assertEquals(s1.hashCode(), s2.hashCode());
		}

		// hash codes are reasonably distributed
		Set<Integer> hashCodes = new LinkedHashSet<>();
		for (int i = 0; i < 100; i++) {
			SimpleRegionId simpleRegionId = getRandomSimpleRegionId(randomGenerator.nextLong());
			hashCodes.add(simpleRegionId.hashCode());
		}

		assertEquals(100, hashCodes.size());
	}

	private SimpleRegionId getRandomSimpleRegionId(long seed) {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(seed);
		return new SimpleRegionId(randomGenerator.nextInt());
	}
}
