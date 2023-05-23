package plugins.stochastics.support;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

import org.apache.commons.math3.random.RandomGenerator;
import org.junit.jupiter.api.Test;

import util.annotations.UnitTag;
import util.annotations.UnitTestMethod;
import util.errors.ContractException;
import util.random.RandomGeneratorProvider;

public class AT_WellState {

	@Test
	@UnitTestMethod(target = WellState.Builder.class, name = "build", args = {}, tags = UnitTag.INCOMPLETE)
	public void testBuild() {
		WellState wellState = WellState.builder().build();
		assertNotNull(wellState);
	}

	@Test
	@UnitTestMethod(target = WellState.Builder.class, name = "setSeed", args = { long.class }, tags = UnitTag.INCOMPLETE)
	public void testSetSeed() {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(6503031989275285502L);

		for (int i = 0; i < 30; i++) {
			long expectedSeed = randomGenerator.nextLong();
			WellState wellState = WellState.builder().setSeed(expectedSeed).build();
			Long actualSeed = wellState.getSeed();
			assertEquals(expectedSeed, actualSeed);
		}

	}

	@Test
	@UnitTestMethod(target = WellState.class, name = "builder", args = {})
	public void testBuilder() {
		assertNotNull(WellState.builder());
	}

	@Test
	@UnitTestMethod(target = WellState.class, name = "getSeed", args = {})
	public void testGetSeed() {
		Long expectedSeed = 764258969836004163L;

		WellState wellState = WellState.builder().setSeed(expectedSeed).build();

		Long actualSeed = wellState.getSeed();
		assertEquals(expectedSeed, actualSeed);
	}

	@Test
	@UnitTestMethod(target = WellState.class, name = "getIndex", args = {})
	public void testGetIndex() {
		WellState.Builder builder = WellState.builder();
		Long seed = 6559152513645047938L;
		int expectedIndex = 13;
		int[] vArray = new int[1391];

		WellState wellState = builder.setInternals(expectedIndex, vArray).setSeed(seed).build();
		int actualIndex = wellState.getIndex();
		assertEquals(expectedIndex, actualIndex);
	}

	@Test
	@UnitTestMethod(target = WellState.class, name = "getVArray", args = {})
	public void testGetVArray() {
		WellState.Builder builder = WellState.builder();
		Long seed = 6559152513645047938L;
		int index = 13;
		int[] expectedVArray = new int[1391];

		WellState wellState = builder.setInternals(index, expectedVArray).setSeed(seed).build();
		int[] actualVArray = wellState.getVArray();

		assertTrue(Arrays.equals(expectedVArray, actualVArray));
	}

	@Test
	@UnitTestMethod(target = WellState.class, name = "hashCode", args = {})
	public void testHashCode() {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(3103545448276048549L);

		// show that equal well states have equal hash codes
		for (int i = 0; i < 30; i++) {
			long seed = randomGenerator.nextLong();
			WellState wellState1 = createWellState(seed);
			WellState wellState2 = createWellState(seed);
			assertEquals(wellState1, wellState2);
			assertEquals(wellState1.hashCode(), wellState2.hashCode());
		}

		// show that hash codes are reasonably distributed
		Set<Integer> hashCodes = new LinkedHashSet<>();
		for (int i = 0; i < 100; i++) {
			hashCodes.add(createWellState(randomGenerator.nextLong()).hashCode());
		}
		assertTrue(hashCodes.size() >= 90);
	}

	@Test
	@UnitTestMethod(target = WellState.Builder.class, name = "setInternals", args = { int.class, int[].class }, tags = UnitTag.INCOMPLETE)
	public void testSetInternals() {
		WellState.Builder builder = WellState.builder();
		Long seed = 6559152513645047938L;
		int index = 13;
		int[] vArray = new int[1391];

		WellState wellState = builder.setInternals(index, vArray).setSeed(seed).build();

		int actualIndex = wellState.getIndex();
		int[] actualVArray = wellState.getVArray();

		assertEquals(index, actualIndex);
		assertTrue(Arrays.equals(vArray, actualVArray));

		// precondition test: null vArray
		ContractException contractException = assertThrows(ContractException.class, () -> builder.setInternals(index, null));
		assertEquals(StochasticsError.ILLEGAL_SEED_ININITIAL_STATE, contractException.getErrorType());

		// precondition test: improper vArray size
		ContractException contractException2 = assertThrows(ContractException.class, () -> builder.setInternals(index, new int[15]));
		assertEquals(StochasticsError.ILLEGAL_SEED_ININITIAL_STATE, contractException2.getErrorType());

		// precondition test: index out of allowed range
		ContractException contractException3 = assertThrows(ContractException.class, () -> builder.setInternals(-1, vArray));
		assertEquals(StochasticsError.ILLEGAL_SEED_ININITIAL_STATE, contractException3.getErrorType());

		ContractException contractException4 = assertThrows(ContractException.class, () -> builder.setInternals(1391, vArray));
		assertEquals(StochasticsError.ILLEGAL_SEED_ININITIAL_STATE, contractException4.getErrorType());
	}

	@Test
	@UnitTestMethod(target = WellState.class, name = "equals", args = { Object.class }, tags = UnitTag.INCOMPLETE)
	public void testEquals() {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(7242512295369848202L);

		// no object equals null
		for (int i = 0; i < 30; i++) {
			long seed = randomGenerator.nextLong();
			WellState wellState = createWellState(seed);
			assertFalse(wellState.equals(null));
		}

		// stability
		for (int i = 0; i < 30; i++) {
			long seed = randomGenerator.nextLong();
			WellState wellState1 = createWellState(seed);
			WellState wellState2 = createWellState(seed);
			for (int j = 0; j < 10; j++) {
				assertTrue(wellState1.equals(wellState2));
				assertTrue(wellState2.equals(wellState1));
			}
		}

		// reflexive
		for (int i = 0; i < 30; i++) {
			long seed = randomGenerator.nextLong();
			WellState wellState = createWellState(seed);
			assertTrue(wellState.equals(wellState));
		}

		// symmetric
		for (int i = 0; i < 30; i++) {
			long seed = randomGenerator.nextLong();
			WellState wellState1 = createWellState(seed);
			WellState wellState2 = createWellState(seed);
			assertTrue(wellState1.equals(wellState2));
			assertTrue(wellState2.equals(wellState1));
		}

		// transitive
		for (int i = 0; i < 30; i++) {
			long seed = randomGenerator.nextLong();
			WellState wellState1 = createWellState(seed);
			WellState wellState2 = createWellState(seed);
			WellState wellState3 = createWellState(seed);
			assertTrue(wellState1.equals(wellState2));
			assertTrue(wellState2.equals(wellState3));
			assertTrue(wellState1.equals(wellState3));
		}

		// show that different inputs lead to non-equality -- assumed
		for (int i = 0; i < 30; i++) {
			long seed1 = randomGenerator.nextLong();
			long seed2 = randomGenerator.nextLong();
			if (seed1 != seed2) {
				WellState wellState1 = createWellState(seed1);
				WellState wellState2 = createWellState(seed2);
				assertNotEquals(wellState1, wellState2);
			}
		}
	}

	private WellState createWellState(Long seed) {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(seed);
		int stateIndex = randomGenerator.nextInt(1390);
		int[] vArray = new int[1391];

		for (int i = 0; i < 1391; i++) {
			vArray[i] = randomGenerator.nextInt();
		}
		WellState wellState = WellState.builder().setInternals(stateIndex, vArray).setSeed(seed).build();
		return wellState;
	}
}
