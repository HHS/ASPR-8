package gov.hhs.aspr.ms.gcm.simulation.nucleus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.LinkedHashSet;
import java.util.Set;

import org.apache.commons.math3.random.RandomGenerator;
import org.junit.jupiter.api.Test;

import gov.hhs.aspr.ms.util.annotations.UnitTestConstructor;
import gov.hhs.aspr.ms.util.annotations.UnitTestMethod;
import gov.hhs.aspr.ms.util.random.RandomGeneratorProvider;

public final class AT_DataManagerId {

	@UnitTestMethod(target = DataManagerId.class, name = "getValue", args = {})
	@Test
	public void testGetValue() {
		for (int i = 0; i < 100; i++) {
			assertEquals(i, new DataManagerId(i).getValue());
		}
	}

	@UnitTestMethod(target = DataManagerId.class, name = "toString", args = {})
	@Test
	public void testToString() {
		for (int i = 0; i < 100; i++) {
			assertEquals("DataManagerId [id=" + i + "]", new DataManagerId(i).toString());
		}
	}

	@UnitTestMethod(target = DataManagerId.class, name = "hashCode", args = {})
	@Test
	public void testHashCode() {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(2653491508465183354L);

		// equal objects have equal hash codes
		for (int i = 0; i < 30; i++) {
			long seed = randomGenerator.nextLong();
			DataManagerId dataManagerId1 = getRandomDataManagerId(seed);
			DataManagerId dataManagerId2 = getRandomDataManagerId(seed);

			assertEquals(dataManagerId1, dataManagerId2);
			assertEquals(dataManagerId1.hashCode(), dataManagerId2.hashCode());
		}

		// hash codes are reasonably distributed
		Set<Integer> hashCodes = new LinkedHashSet<>();
		for (int i = 0; i < 100; i++) {
			DataManagerId dataManagerId = getRandomDataManagerId(randomGenerator.nextLong());
			hashCodes.add(dataManagerId.hashCode());
		}

		assertEquals(100, hashCodes.size());
	}

	@UnitTestMethod(target = DataManagerId.class, name = "equals", args = { Object.class })
	@Test
	public void testEquals() {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(8980825558377306870L);

		// never equal to another type
		for (int i = 0; i < 30; i++) {
			DataManagerId dataManagerId = getRandomDataManagerId(randomGenerator.nextLong());
			assertFalse(dataManagerId.equals(new Object()));
		}

		// never equal to null
		for (int i = 0; i < 30; i++) {
			DataManagerId dataManagerId = getRandomDataManagerId(randomGenerator.nextLong());
			assertFalse(dataManagerId.equals(null));
		}

		// reflexive
		for (int i = 0; i < 30; i++) {
			DataManagerId dataManagerId = getRandomDataManagerId(randomGenerator.nextLong());
			assertTrue(dataManagerId.equals(dataManagerId));
		}

		// symmetric, transitive, consistent
		for (int i = 0; i < 30; i++) {
			long seed = randomGenerator.nextLong();
			DataManagerId dataManagerId1 = getRandomDataManagerId(seed);
			DataManagerId dataManagerId2 = getRandomDataManagerId(seed);
			assertFalse(dataManagerId1 == dataManagerId2);
			for (int j = 0; j < 10; j++) {
				assertTrue(dataManagerId1.equals(dataManagerId2));
				assertTrue(dataManagerId2.equals(dataManagerId1));
			}
		}

		// different inputs yield unequal dataManagerIds
		Set<DataManagerId> set = new LinkedHashSet<>();
		for (int i = 0; i < 100; i++) {
			DataManagerId dataManagerId = getRandomDataManagerId(randomGenerator.nextLong());
			set.add(dataManagerId);
		}
		assertEquals(100, set.size());
	}

	@Test
	@UnitTestMethod(target = DataManagerId.class, name = "compareTo", args = { DataManagerId.class })
	public void testCompareTo() {

		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(8041307094727012939L);
		for (int i = 0; i < 100; i++) {
			int a = randomGenerator.nextInt();
			int b = randomGenerator.nextInt();

			int expectedComparison = Integer.compare(a, b);
			int actualComparison = new DataManagerId(a).compareTo(new DataManagerId(b));
			assertEquals(expectedComparison, actualComparison);
		}

	}

	@Test
	@UnitTestConstructor(target = DataManagerId.class, args = { int.class })
	public void testConstructor() {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(7051188045588654915L);
		for (int i = 0; i < 100; i++) {
			int expectedIdValue = randomGenerator.nextInt();
			DataManagerId dataManagerId = new DataManagerId(expectedIdValue);
			int actualIdValue = dataManagerId.getValue();
			assertEquals(expectedIdValue, actualIdValue);
		}
	}

	private DataManagerId getRandomDataManagerId(long seed) {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(seed);
		return new DataManagerId(randomGenerator.nextInt(Integer.MAX_VALUE));
	}
}
