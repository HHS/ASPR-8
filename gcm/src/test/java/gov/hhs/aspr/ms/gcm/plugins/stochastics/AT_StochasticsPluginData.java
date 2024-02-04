package gov.hhs.aspr.ms.gcm.plugins.stochastics;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
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

import gov.hhs.aspr.ms.gcm.nucleus.PluginDataBuilder;
import gov.hhs.aspr.ms.gcm.plugins.stochastics.datamanagers.StochasticsPluginData;
import gov.hhs.aspr.ms.gcm.plugins.stochastics.support.RandomNumberGeneratorId;
import gov.hhs.aspr.ms.gcm.plugins.stochastics.support.StochasticsError;
import gov.hhs.aspr.ms.gcm.plugins.stochastics.support.WellState;
import gov.hhs.aspr.ms.gcm.plugins.stochastics.testsupport.TestRandomGeneratorId;
import gov.hhs.aspr.ms.util.annotations.UnitTestMethod;
import gov.hhs.aspr.ms.util.errors.ContractException;
import gov.hhs.aspr.ms.util.random.RandomGeneratorProvider;

public class AT_StochasticsPluginData {

	@Test
	@UnitTestMethod(target = StochasticsPluginData.class, name = "getCloneBuilder", args = {})
	public void testGetCloneBuilder() {

		WellState wellState = WellState.builder().setSeed(4970625656919510170L).build();

		StochasticsPluginData stochasticsPluginData = StochasticsPluginData.builder()//
				.setMainRNGState(wellState)//
				.addRNG(TestRandomGeneratorId.BLITZEN, wellState)//
				.addRNG(TestRandomGeneratorId.COMET, wellState)//
				.build();//

		// show that the clone builder is not null
		PluginDataBuilder cloneBuilder = stochasticsPluginData.getCloneBuilder();
		assertNotNull(cloneBuilder);
		StochasticsPluginData cloneData = (StochasticsPluginData) cloneBuilder.build();

		// show that the clone builder is properly initialized
		assertEquals(cloneData, stochasticsPluginData);

	}

	@Test
	@UnitTestMethod(target = StochasticsPluginData.class, name = "getWellState", args = {})
	public void testGetWellState() {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(4970625656919510170L);
		for (int i = 0; i < 30; i++) {
			long seed = randomGenerator.nextLong();
			WellState wellState = WellState.builder().setSeed(seed).build();
			StochasticsPluginData stochasticsPluginData = StochasticsPluginData.builder().setMainRNGState(wellState)
					.build();
			assertEquals(wellState, stochasticsPluginData.getWellState());
		}
	}

	@Test
	@UnitTestMethod(target = StochasticsPluginData.class, name = "getRandomNumberGeneratorIds", args = {})
	public void testGetRandomNumberGeneratorIds() {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(1644320989680923741L);
		Map<RandomNumberGeneratorId, WellState> expectedRandomNumberGeneratorIds = new LinkedHashMap<>();
		StochasticsPluginData.Builder builder = StochasticsPluginData.builder();
		for (TestRandomGeneratorId testRandomGeneratorId : TestRandomGeneratorId.values()) {
			WellState wellState = WellState.builder().setSeed(randomGenerator.nextLong()).build();
			expectedRandomNumberGeneratorIds.put(testRandomGeneratorId, wellState);
			builder.addRNG(testRandomGeneratorId, wellState);
		}
		WellState wellState = WellState.builder().setSeed(randomGenerator.nextLong()).build();
		builder.setMainRNGState(wellState);
		StochasticsPluginData stochasticsPluginData = builder.build();

		Map<RandomNumberGeneratorId, WellState> actualRandomNumberGeneratorIds = new LinkedHashMap<>();
		for (RandomNumberGeneratorId randomNumberGeneratorId : stochasticsPluginData.getRandomNumberGeneratorIds()) {
			wellState = stochasticsPluginData.getWellState(randomNumberGeneratorId);
			actualRandomNumberGeneratorIds.put(randomNumberGeneratorId, wellState);
		}
		assertEquals(expectedRandomNumberGeneratorIds, actualRandomNumberGeneratorIds);
	}

	@Test
	@UnitTestMethod(target = StochasticsPluginData.class, name = "getWellState", args = {
			RandomNumberGeneratorId.class })
	public void testGetWellState_randomNumberGeneratorId() {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(1644320989680923741L);
		Map<RandomNumberGeneratorId, WellState> expectedRandomNumberGeneratorIds = new LinkedHashMap<>();
		StochasticsPluginData.Builder builder = StochasticsPluginData.builder();
		for (TestRandomGeneratorId testRandomGeneratorId : TestRandomGeneratorId.values()) {
			WellState wellState = WellState.builder().setSeed(randomGenerator.nextLong()).build();
			expectedRandomNumberGeneratorIds.put(testRandomGeneratorId, wellState);
			builder.addRNG(testRandomGeneratorId, wellState);
		}
		WellState wellState = WellState.builder().setSeed(randomGenerator.nextLong()).build();
		builder.setMainRNGState(wellState);
		StochasticsPluginData stochasticsPluginData = builder.build();

		Map<RandomNumberGeneratorId, WellState> actualRandomNumberGeneratorIds = new LinkedHashMap<>();
		for (RandomNumberGeneratorId randomNumberGeneratorId : stochasticsPluginData.getRandomNumberGeneratorIds()) {
			wellState = stochasticsPluginData.getWellState(randomNumberGeneratorId);
			actualRandomNumberGeneratorIds.put(randomNumberGeneratorId, wellState);
		}
		assertEquals(expectedRandomNumberGeneratorIds, actualRandomNumberGeneratorIds);
	}

	@Test
	@UnitTestMethod(target = StochasticsPluginData.class, name = "builder", args = {})
	public void testBuilder() {
		// show that the builder returns a non-null instance of
		// StochasticsPluginData.Builder
		assertNotNull(StochasticsPluginData.builder());
	}

	@Test
	@UnitTestMethod(target = StochasticsPluginData.Builder.class, name = "build", args = {})
	public void testBuild() {
		ContractException contractException = assertThrows(ContractException.class,
				() -> StochasticsPluginData.builder().build());
		assertEquals(StochasticsError.NULL_SEED, contractException.getErrorType());
	}

	@Test
	@UnitTestMethod(target = StochasticsPluginData.Builder.class, name = "setMainRNGState", args = { WellState.class })
	public void testSetMainRNGState() {
		long seed = 235234623445234756L;
		WellState wellState = WellState.builder().setSeed(seed).build();
		StochasticsPluginData stochasticsPluginData = StochasticsPluginData.builder()//
				.setMainRNGState(wellState)//
				.build();
		assertEquals(seed, stochasticsPluginData.getWellState().getSeed());
	}

	@Test
	@UnitTestMethod(target = StochasticsPluginData.Builder.class, name = "addRNG", args = {
			RandomNumberGeneratorId.class, WellState.class })
	public void testAddRNG() {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(4300202782621809065L);

		Map<RandomNumberGeneratorId, WellState> expectedGenerators = new LinkedHashMap<>();

		StochasticsPluginData.Builder builder = StochasticsPluginData.builder();//
		builder.setMainRNGState(WellState.builder().setSeed(randomGenerator.nextLong()).build());
		for (TestRandomGeneratorId testRandomGeneratorId : TestRandomGeneratorId.values()) {
			WellState wellState = WellState.builder().setSeed(randomGenerator.nextLong()).build();
			builder.addRNG(testRandomGeneratorId, wellState);
			expectedGenerators.put(testRandomGeneratorId, wellState);
		}
		StochasticsPluginData stochasticsPluginData = builder.build();
		Map<RandomNumberGeneratorId, WellState> actualGenerators = new LinkedHashMap<>();
		for (RandomNumberGeneratorId randomNumberGeneratorId : stochasticsPluginData.getRandomNumberGeneratorIds()) {
			WellState wellState = stochasticsPluginData.getWellState(randomNumberGeneratorId);
			actualGenerators.put(randomNumberGeneratorId, wellState);
		}
		assertEquals(expectedGenerators, actualGenerators);

		// precondition test: if the random number generator id is null
		ContractException contractException = assertThrows(ContractException.class, () -> {
			WellState wellState = WellState.builder().setSeed(1130627593613916615L).build();

			StochasticsPluginData.builder()//
					.addRNG(null, wellState);
		});
		assertEquals(StochasticsError.NULL_RANDOM_NUMBER_GENERATOR_ID, contractException.getErrorType());

		// precondition test: if the well state is null
		contractException = assertThrows(ContractException.class, () -> {
			StochasticsPluginData.builder()//
					.addRNG(TestRandomGeneratorId.DANCER, null);
		});
		assertEquals(StochasticsError.NULL_WELL_STATE, contractException.getErrorType());

	}

	private StochasticsPluginData getRandomStochasticsPluginData(long seed) {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(seed);
		Random random = new Random(randomGenerator.nextLong());
		List<TestRandomGeneratorId> randomGeneratorIds = Arrays.asList(TestRandomGeneratorId.values());
		Collections.shuffle(randomGeneratorIds, random);
		StochasticsPluginData.Builder builder = StochasticsPluginData.builder();
		builder.setMainRNGState(WellState.builder().setSeed(randomGenerator.nextLong()).build());
		for (TestRandomGeneratorId testRandomGeneratorId : randomGeneratorIds) {
			builder.addRNG(testRandomGeneratorId, WellState.builder().setSeed(randomGenerator.nextLong()).build());
			if (randomGenerator.nextDouble() < 0.25) {
				break;
			}
		}
		return builder.build();
	}

	@Test
	@UnitTestMethod(target = StochasticsPluginData.class, name = "equals", args = { Object.class })
	public void testEquals() {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(4427478130102505257L);
		// never equal to null
		for (int i = 0; i < 30; i++) {
			StochasticsPluginData stochasticsPluginData = getRandomStochasticsPluginData(randomGenerator.nextLong());
			assertFalse(stochasticsPluginData.equals(null));
		}

		// reflexive
		for (int i = 0; i < 30; i++) {
			StochasticsPluginData stochasticsPluginData = getRandomStochasticsPluginData(randomGenerator.nextLong());
			assertTrue(stochasticsPluginData.equals(stochasticsPluginData));
		}

		// symmetric, transitive, consistent
		for (int i = 0; i < 30; i++) {
			long seed = randomGenerator.nextLong();
			StochasticsPluginData stochasticsPluginData1 = getRandomStochasticsPluginData(seed);
			StochasticsPluginData stochasticsPluginData2 = getRandomStochasticsPluginData(seed);
			for (int j = 0; j < 5; j++) {
				assertTrue(stochasticsPluginData1.equals(stochasticsPluginData2));
				assertTrue(stochasticsPluginData2.equals(stochasticsPluginData1));
			}
		}

		// different inputs yield non-equal objects
		Set<StochasticsPluginData> stochasticsPluginDatas = new LinkedHashSet<>();
		for (int i = 0; i < 100; i++) {
			StochasticsPluginData stochasticsPluginData = getRandomStochasticsPluginData(randomGenerator.nextLong());
			stochasticsPluginDatas.add(stochasticsPluginData);
		}
		assertEquals(100, stochasticsPluginDatas.size());

	}

	@Test
	@UnitTestMethod(target = StochasticsPluginData.class, name = "hashCode", args = {})
	public void testHashCode() {

		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(8867946191732013544L);

		// equal objects have equal hash codes
		for (int i = 0; i < 30; i++) {
			long seed = randomGenerator.nextLong();
			StochasticsPluginData stochasticsPluginData1 = getRandomStochasticsPluginData(seed);
			StochasticsPluginData stochasticsPluginData2 = getRandomStochasticsPluginData(seed);
			assertEquals(stochasticsPluginData1, stochasticsPluginData2);
			assertEquals(stochasticsPluginData1.hashCode(), stochasticsPluginData2.hashCode());
		}

		// hash codes are reasonably distributed
		Set<Integer> hashCodes = new LinkedHashSet<>();
		for (int i = 0; i < 100; i++) {
			StochasticsPluginData stochasticsPluginData = getRandomStochasticsPluginData(randomGenerator.nextLong());
			hashCodes.add(stochasticsPluginData.hashCode());
		}
		assertEquals(100, hashCodes.size());
	}
 
	@Test
	@UnitTestMethod(target = StochasticsPluginData.class, name = "toString", args = {})
	public void testToString() {
		StochasticsPluginData stochasticsPluginData = getRandomStochasticsPluginData(3688475113239640194L);
		String actualValue = stochasticsPluginData.toString();

		/*
		 * Expected value manually verified. It is impractical to use the full string for verification, so we will assert that certain critical substrings are contained as expected.
		 */
		assertTrue(actualValue.contains("StochasticsPluginData [data=Data [wellState=WellState [data=Data [seed=-3890456017103968429"));
		assertTrue(actualValue.contains("VIXEN=WellState [data=Data [seed=-9202547125755605402")); 
		assertTrue(actualValue.contains("DONNER=WellState [data=Data [seed=-4994162167240462248"));
		assertTrue(actualValue.contains("PRANCER=WellState [data=Data [seed=2580414198424993374"));
		assertTrue(actualValue.contains("BLITZEN=WellState [data=Data [seed=-3565866373405448731")); 
		assertTrue(actualValue.contains("DANCER=WellState [data=Data [seed=3656710085871564729"));		
	}

}
