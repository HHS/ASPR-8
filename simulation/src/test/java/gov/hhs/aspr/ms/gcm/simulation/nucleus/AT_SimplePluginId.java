package gov.hhs.aspr.ms.gcm.simulation.nucleus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.LinkedHashSet;
import java.util.Set;

import org.apache.commons.math3.random.RandomGenerator;
import org.junit.jupiter.api.Test;

import gov.hhs.aspr.ms.util.annotations.UnitTestConstructor;
import gov.hhs.aspr.ms.util.annotations.UnitTestMethod;
import gov.hhs.aspr.ms.util.random.RandomGeneratorProvider;

public class AT_SimplePluginId {
	@Test
	@UnitTestConstructor(target = SimplePluginId.class, args = { Object.class })
	public void testConstructor() {
		assertThrows(RuntimeException.class, () -> new SimplePluginId(null));
	}

	@Test
	@UnitTestMethod(target = SimplePluginId.class, name = "equals", args = { Object.class })
	public void testEquals() {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(8980821418377306870L);

		// never equal to another type
		for (int i = 0; i < 30; i++) {
			SimplePluginId simplePluginId = getRandomSimplePluginId(randomGenerator.nextLong());
			assertFalse(simplePluginId.equals(new Object()));
		}

		// never equal to null
		for (int i = 0; i < 30; i++) {
			SimplePluginId simplePluginId = getRandomSimplePluginId(randomGenerator.nextLong());
			assertFalse(simplePluginId.equals(null));
		}

		// reflexive
		for (int i = 0; i < 30; i++) {
			SimplePluginId simplePluginId = getRandomSimplePluginId(randomGenerator.nextLong());
			assertTrue(simplePluginId.equals(simplePluginId));
		}

		// symmetric, transitive, consistent
		for (int i = 0; i < 30; i++) {
			long seed = randomGenerator.nextLong();
			SimplePluginId simplePluginId1 = getRandomSimplePluginId(seed);
			SimplePluginId simplePluginId2 = getRandomSimplePluginId(seed);
			assertFalse(simplePluginId1 == simplePluginId2);
			for (int j = 0; j < 10; j++) {
				assertTrue(simplePluginId1.equals(simplePluginId2));
				assertTrue(simplePluginId2.equals(simplePluginId1));
			}
		}

		// different inputs yield unequal simplePluginIds
		Set<SimplePluginId> set = new LinkedHashSet<>();
		for (int i = 0; i < 100; i++) {
			SimplePluginId simplePluginId = getRandomSimplePluginId(randomGenerator.nextLong());
			set.add(simplePluginId);
		}
		assertEquals(100, set.size());
	}

	@Test
	@UnitTestMethod(target = SimplePluginId.class, name = "hashCode", args = {})
	public void testHashCode() {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(2653491509465183354L);

		// equal objects have equal hash codes
		for (int i = 0; i < 30; i++) {
			long seed = randomGenerator.nextLong();
			SimplePluginId simplePluginId1 = getRandomSimplePluginId(seed);
			SimplePluginId simplePluginId2 = getRandomSimplePluginId(seed);

			assertEquals(simplePluginId1, simplePluginId2);
			assertEquals(simplePluginId1.hashCode(), simplePluginId2.hashCode());
		}

		// hash codes are reasonably distributed
		Set<Integer> hashCodes = new LinkedHashSet<>();
		for (int i = 0; i < 100; i++) {
			SimplePluginId simplePluginId = getRandomSimplePluginId(randomGenerator.nextLong());
			hashCodes.add(simplePluginId.hashCode());
		}

		assertEquals(100, hashCodes.size());
	}

	@Test
	@UnitTestMethod(target = SimplePluginId.class, name = "toString", args = {})
	public void testToString() {
		assertEquals("A", new SimplePluginId("A").toString());
		assertEquals("ASDF", new SimplePluginId("ASDF").toString());
		assertEquals(Integer.toString(12), new SimplePluginId(12).toString());
	}

	private SimplePluginId getRandomSimplePluginId(long seed) {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(seed);
		return new SimplePluginId(randomGenerator.nextInt(Integer.MAX_VALUE));
	}
}
