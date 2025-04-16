package gov.hhs.aspr.ms.gcm.simulation.plugins.materials.support;

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

public class AT_BatchId {

	@Test
	@UnitTestConstructor(target = BatchId.class,args = { int.class })
	public void testConstructor() {
		for (int i = 0; i < 10; i++) {
			BatchId BatchId = new BatchId(i);
			assertEquals(i, BatchId.getValue());
		}

	}

	@Test
	@UnitTestMethod(target = BatchId.class,name = "compareTo", args = { BatchId.class })
	public void testCompareTo() {
		for (int i = 0; i < 10; i++) {
			BatchId batchA = new BatchId(i);
			for (int j = 0; j < 10; j++) {
				BatchId batchB = new BatchId(j);
				int comparisonValue = batchA.compareTo(batchB);
				if (i < j) {
					assertTrue(comparisonValue < 0);
				} else if (i > j) {
					assertTrue(comparisonValue > 0);
				} else {
					assertTrue(comparisonValue == 0);
				}
			}
		}
	}

	@Test
	@UnitTestMethod(target = BatchId.class,name = "equals", args = { Object.class })
	public void testEquals() {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(8980821418377306870L);

		// never equal to another type
		for (int i = 0; i < 30; i++) {
			BatchId batchId = getRandomBatchId(randomGenerator.nextLong());
			assertFalse(batchId.equals(new Object()));
		}

		// never equal to null
		for (int i = 0; i < 30; i++) {
			BatchId batchId = getRandomBatchId(randomGenerator.nextLong());
			assertFalse(batchId.equals(null));
		}

		// reflexive
		for (int i = 0; i < 30; i++) {
			BatchId batchId = getRandomBatchId(randomGenerator.nextLong());
			assertTrue(batchId.equals(batchId));
		}

		// symmetric, transitive, consistent
		for (int i = 0; i < 30; i++) {
			long seed = randomGenerator.nextLong();
			BatchId batchId1 = getRandomBatchId(seed);
			BatchId batchId2 = getRandomBatchId(seed);
			assertFalse(batchId1 == batchId2);
			for (int j = 0; j < 10; j++) {
				assertTrue(batchId1.equals(batchId2));
				assertTrue(batchId2.equals(batchId1));
			}
		}

		// different inputs yield unequal batchIds
		Set<BatchId> set = new LinkedHashSet<>();
		for (int i = 0; i < 100; i++) {
			BatchId batchId = getRandomBatchId(randomGenerator.nextLong());
			set.add(batchId);
		}
		assertEquals(100, set.size());
	}

	@Test
	@UnitTestMethod(target = BatchId.class,name = "getValue", args = {})
	public void testGetValue() {
		for (int i = 0; i < 10; i++) {
			BatchId batch = new BatchId(i);
			assertEquals(i, batch.getValue());
		}
	}

	@Test
	@UnitTestMethod(target = BatchId.class,name = "hashCode", args = {})
	public void testHashCode() {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(2657771508465183354L);

		// equal objects have equal hash codes
		for (int i = 0; i < 30; i++) {
			long seed = randomGenerator.nextLong();
			BatchId batchId1 = getRandomBatchId(seed);
			BatchId batchId2 = getRandomBatchId(seed);

			assertEquals(batchId1, batchId2);
			assertEquals(batchId1.hashCode(), batchId2.hashCode());
		}

		// hash codes are reasonably distributed
		Set<Integer> hashCodes = new LinkedHashSet<>();
		for (int i = 0; i < 100; i++) {
			BatchId batchId = getRandomBatchId(randomGenerator.nextLong());
			hashCodes.add(batchId.hashCode());
		}

		assertEquals(100, hashCodes.size());
	}

	@Test
	@UnitTestMethod(target = BatchId.class,name = "toString", args = {})
	public void testToString() {
		for (int i = 0; i < 10; i++) {
			BatchId batch = new BatchId(i);
			assertEquals(Integer.toString(i), batch.toString());
		}
	}

	private BatchId getRandomBatchId(long seed) {
        RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(seed);
        return new BatchId(randomGenerator.nextInt());
    }
}
