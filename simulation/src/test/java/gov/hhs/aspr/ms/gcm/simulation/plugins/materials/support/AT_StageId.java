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

public class AT_StageId {

	@Test
	@UnitTestConstructor(target = StageId.class, args = { int.class })
	public void testConstructor() {
		for (int i = 0; i < 10; i++) {
			StageId StageId = new StageId(i);
			assertEquals(i, StageId.getValue());
		}

	}

	@Test
	@UnitTestMethod(target = StageId.class, name = "compareTo", args = { StageId.class })
	public void testCompareTo() {
		for (int i = 0; i < 10; i++) {
			StageId stageA = new StageId(i);
			for (int j = 0; j < 10; j++) {
				StageId stageB = new StageId(j);
				int comparisonValue = stageA.compareTo(stageB);
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
	@UnitTestMethod(target = StageId.class, name = "equals", args = { Object.class })
	public void testEquals() {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(8980828338377306870L);

		// never equal to another type
		for (int i = 0; i < 30; i++) {
			StageId stageId = getRandomStageId(randomGenerator.nextLong());
			assertFalse(stageId.equals(new Object()));
		}

		// never equal to null
		for (int i = 0; i < 30; i++) {
			StageId stageId = getRandomStageId(randomGenerator.nextLong());
			assertFalse(stageId.equals(null));
		}

		// reflexive
		for (int i = 0; i < 30; i++) {
			StageId stageId = getRandomStageId(randomGenerator.nextLong());
			assertTrue(stageId.equals(stageId));
		}

		// symmetric, transitive, consistent
		for (int i = 0; i < 30; i++) {
			long seed = randomGenerator.nextLong();
			StageId stageId1 = getRandomStageId(seed);
			StageId stageId2 = getRandomStageId(seed);
			assertFalse(stageId1 == stageId2);
			for (int j = 0; j < 10; j++) {
				assertTrue(stageId1.equals(stageId2));
				assertTrue(stageId2.equals(stageId1));
			}
		}

		// different inputs yield unequal stageIds
		Set<StageId> set = new LinkedHashSet<>();
		for (int i = 0; i < 100; i++) {
			StageId stageId = getRandomStageId(randomGenerator.nextLong());
			set.add(stageId);
		}
		assertEquals(100, set.size());
	}

	@Test
	@UnitTestMethod(target = StageId.class, name = "getValue", args = {})
	public void testGetValue() {
		for (int i = 0; i < 10; i++) {
			StageId stage = new StageId(i);
			assertEquals(i, stage.getValue());
		}
	}

	@Test
	@UnitTestMethod(target = StageId.class, name = "hashCode", args = {})
	public void testHashCode() {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(2653491555415183354L);

		// equal objects have equal hash codes
		for (int i = 0; i < 30; i++) {
			long seed = randomGenerator.nextLong();
			StageId stageId1 = getRandomStageId(seed);
			StageId stageId2 = getRandomStageId(seed);

			assertEquals(stageId1, stageId2);
			assertEquals(stageId1.hashCode(), stageId2.hashCode());
		}

		// hash codes are reasonably distributed
		Set<Integer> hashCodes = new LinkedHashSet<>();
		for (int i = 0; i < 100; i++) {
			StageId stageId = getRandomStageId(randomGenerator.nextLong());
			hashCodes.add(stageId.hashCode());
		}

		assertEquals(100, hashCodes.size());
	}

	@Test
	@UnitTestMethod(target = StageId.class, name = "toString", args = {})
	public void testToString() {
		for (int i = 0; i < 10; i++) {
			StageId stage = new StageId(i);
			assertEquals(Integer.toString(i), stage.toString());
		}
	}

	private StageId getRandomStageId(long seed) {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(seed);
		return new StageId(randomGenerator.nextInt());
	}
}
