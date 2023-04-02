package plugins.stochastics;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.commons.math3.random.RandomGenerator;
import org.junit.jupiter.api.Test;

import nucleus.PluginDataBuilder;
import plugins.stochastics.support.RandomNumberGeneratorId;
import plugins.stochastics.support.StochasticsError;
import plugins.stochastics.support.WellState;
import plugins.stochastics.testsupport.TestRandomGeneratorId;
import util.annotations.UnitTestMethod;
import util.errors.ContractException;
import util.random.RandomGeneratorProvider;

public class AT_StochasticsPluginData {

	@Test
	@UnitTestMethod(target = StochasticsPluginData.class, name = "getCloneBuilder", args = {})
	public void testGetCloneBuilder() {

		WellState wellState = WellState.builder().setSeed(4970625656919510170L).build();

		StochasticsPluginData stochasticsPluginData = StochasticsPluginData	.builder()//
																			.setMainRNG(wellState)//
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
			StochasticsPluginData stochasticsPluginData = StochasticsPluginData.builder().setMainRNG(wellState).build();
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
		builder.setMainRNG(wellState);
		StochasticsPluginData stochasticsPluginData = builder.build();

		Map<RandomNumberGeneratorId, WellState> actualRandomNumberGeneratorIds = new LinkedHashMap<>();
		for (RandomNumberGeneratorId randomNumberGeneratorId : stochasticsPluginData.getRandomNumberGeneratorIds()) {
			wellState = stochasticsPluginData.getWellState(randomNumberGeneratorId);
			actualRandomNumberGeneratorIds.put(randomNumberGeneratorId, wellState);
		}
		assertEquals(expectedRandomNumberGeneratorIds, actualRandomNumberGeneratorIds);
	}
	
	@Test
	@UnitTestMethod(target = StochasticsPluginData.class, name = "getRandomNumberGeneratorIds", args = {RandomNumberGeneratorId.class})
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
		builder.setMainRNG(wellState);
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
		ContractException contractException = assertThrows(ContractException.class, () -> StochasticsPluginData.builder().build());
		assertEquals(StochasticsError.NULL_SEED, contractException.getErrorType());
	}

	@Test
	@UnitTestMethod(target = StochasticsPluginData.Builder.class, name = "setMainRNG", args = { WellState.class })
	public void testSetMainRNG() {
		long seed = 235234623445234756L;
		WellState wellState = WellState.builder().setSeed(seed).build();
		StochasticsPluginData stochasticsPluginData = StochasticsPluginData	.builder()//
																			.setMainRNG(wellState)//
																			.build();
		assertEquals(seed, stochasticsPluginData.getWellState().getSeed());
	}

	@Test
	@UnitTestMethod(target = StochasticsPluginData.Builder.class, name = "addRNG", args = { RandomNumberGeneratorId.class })
	public void testAddRNG() {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(4300202782621809065L);

		Map<RandomNumberGeneratorId, WellState> expectedGenerators = new LinkedHashMap<>();

		StochasticsPluginData.Builder builder = StochasticsPluginData.builder();//
		builder.setMainRNG(WellState.builder().setSeed(randomGenerator.nextLong()).build());
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

}
