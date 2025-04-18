package gov.hhs.aspr.ms.gcm.simulation.plugins.partitions.testsupport.attributes.support;

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

public class AT_SimpleAttributeId {

	@Test
	@UnitTestConstructor(target = SimpleAttributeId.class, args = { Object.class })
	public void testConstructor() {
		assertNotNull(new SimpleAttributeId(5));

		assertThrows(RuntimeException.class, () -> new SimpleAttributeId(null));
	}

	@Test
	@UnitTestMethod(target = SimpleAttributeId.class, name = "toString", args = {})
	public void testToString() {
		/*
		 * Show that the toString of the SimpleAttributeId equals its input's
		 * toString
		 */

		assertEquals(Integer.toString(5), new SimpleAttributeId(5).toString());
		assertEquals("table", new SimpleAttributeId("table").toString());
		assertEquals(Double.toString(2345.5345), new SimpleAttributeId(2345.5345).toString());

	}

	@Test
	@UnitTestMethod(target = SimpleAttributeId.class, name = "equals", args = { Object.class })
	public void testEquals() {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(8985521418377306870L);

		// never equal to another type
		for (int i = 0; i < 30; i++) {
			SimpleAttributeId simpleAttributeId = getRandomSimpleAttributeId(randomGenerator.nextLong());
			assertFalse(simpleAttributeId.equals(new Object()));
		}

		// never equal to null
		for (int i = 0; i < 30; i++) {
			SimpleAttributeId simpleAttributeId = getRandomSimpleAttributeId(randomGenerator.nextLong());
			assertFalse(simpleAttributeId.equals(null));
		}

		// reflexive
		for (int i = 0; i < 30; i++) {
			SimpleAttributeId simpleAttributeId = getRandomSimpleAttributeId(randomGenerator.nextLong());
			assertTrue(simpleAttributeId.equals(simpleAttributeId));
		}

		// symmetric, transitive, consistent
		for (int i = 0; i < 30; i++) {
			long seed = randomGenerator.nextLong();
			SimpleAttributeId simpleAttributeId1 = getRandomSimpleAttributeId(seed);
			SimpleAttributeId simpleAttributeId2 = getRandomSimpleAttributeId(seed);
			assertFalse(simpleAttributeId1 == simpleAttributeId2);
			for (int j = 0; j < 10; j++) {
				assertTrue(simpleAttributeId1.equals(simpleAttributeId2));
				assertTrue(simpleAttributeId2.equals(simpleAttributeId1));
			}
		}

		// different inputs yield unequal simpleAttributeIds
		Set<SimpleAttributeId> set = new LinkedHashSet<>();
		for (int i = 0; i < 100; i++) {
			SimpleAttributeId simpleAttributeId = getRandomSimpleAttributeId(randomGenerator.nextLong());
			set.add(simpleAttributeId);
		}
		assertEquals(100, set.size());
	}

	@Test
	@UnitTestMethod(target = SimpleAttributeId.class, name = "hashCode", args = {})
	public void testHashCode() {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(2653491502365183354L);

		// equal objects have equal hash codes
		for (int i = 0; i < 30; i++) {
			long seed = randomGenerator.nextLong();
			SimpleAttributeId simpleAttributeId1 = getRandomSimpleAttributeId(seed);
			SimpleAttributeId simpleAttributeId2 = getRandomSimpleAttributeId(seed);

			assertEquals(simpleAttributeId1, simpleAttributeId2);
			assertEquals(simpleAttributeId1.hashCode(), simpleAttributeId2.hashCode());
		}

		// hash codes are reasonably distributed
		Set<Integer> hashCodes = new LinkedHashSet<>();
		for (int i = 0; i < 100; i++) {
			SimpleAttributeId simpleAttributeId = getRandomSimpleAttributeId(randomGenerator.nextLong());
			hashCodes.add(simpleAttributeId.hashCode());
		}

		assertEquals(100, hashCodes.size());
	}

	private SimpleAttributeId getRandomSimpleAttributeId(long seed) {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(seed);
		return new SimpleAttributeId(randomGenerator.nextInt());
	}
}
