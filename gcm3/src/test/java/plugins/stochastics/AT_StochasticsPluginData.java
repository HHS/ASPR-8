package plugins.stochastics;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.LinkedHashSet;
import java.util.Set;

import org.apache.commons.math3.random.RandomGenerator;
import org.junit.jupiter.api.Test;

import nucleus.PluginDataBuilder;
import nucleus.util.ContractException;
import plugins.stochastics.support.RandomNumberGeneratorId;
import plugins.stochastics.support.StochasticsError;
import plugins.stochastics.testsupport.TestRandomGeneratorId;
import tools.annotations.UnitTest;
import tools.annotations.UnitTestMethod;
import util.random.RandomGeneratorProvider;

@UnitTest(target = StochasticsPluginData.class)
public class AT_StochasticsPluginData {

	@Test
	@UnitTestMethod(name = "getCloneBuilder", args = {})
	public void testGetCloneBuilder() {

		
		StochasticsPluginData stochasticsPluginData = StochasticsPluginData	.builder()//
																			.setSeed(4970625656919510170L)//
																			.addRandomGeneratorId(TestRandomGeneratorId.BLITZEN)//
																			.addRandomGeneratorId(TestRandomGeneratorId.COMET)//
																			.build();//

		//show that the clone builder is not null
		PluginDataBuilder cloneBuilder = stochasticsPluginData.getCloneBuilder();
		assertNotNull(cloneBuilder);
		StochasticsPluginData cloneData = (StochasticsPluginData) cloneBuilder.build();

		//show that the clone builder is properly initialized
		assertEquals(cloneData.getRandomNumberGeneratorIds(), stochasticsPluginData.getRandomNumberGeneratorIds());
		assertEquals(cloneData.getSeed(), stochasticsPluginData.getSeed());

	}

	@Test
	@UnitTestMethod(name = "getSeed", args = {})
	public void testGetSeed() {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(4970625656919510170L);
		for (int i = 0; i < 30; i++) {
			long seed = randomGenerator.nextLong();
			StochasticsPluginData stochasticsPluginData = StochasticsPluginData.builder().setSeed(seed).build();
			assertEquals(seed, stochasticsPluginData.getSeed());
		}
	}

	@Test
	@UnitTestMethod(name = "getRandomNumberGeneratorIds", args = {})
	public void testGetRandomNumberGeneratorIds() {
		Set<RandomNumberGeneratorId> expectedRandomNumberGeneratorIds = new LinkedHashSet<>();
		StochasticsPluginData.Builder builder = StochasticsPluginData.builder();
		for (TestRandomGeneratorId testRandomGeneratorId : TestRandomGeneratorId.values()) {
			expectedRandomNumberGeneratorIds.add(testRandomGeneratorId);
			builder.addRandomGeneratorId(testRandomGeneratorId);
		}
		builder.setSeed(3244635455542808061L);
		StochasticsPluginData stochasticsPluginData = builder.build();
		Set<RandomNumberGeneratorId> actualRandomNumberGeneratorIds = stochasticsPluginData.getRandomNumberGeneratorIds();
		assertEquals(expectedRandomNumberGeneratorIds, actualRandomNumberGeneratorIds);
	}

	@Test
	@UnitTestMethod(name = "builder", args = {})
	public void testBuilder() {
		// show that the builder returns a non-null instance of
		// StochasticsPluginData.Builder
		assertNotNull(StochasticsPluginData.builder());
	}

	@Test
	@UnitTestMethod(target = StochasticsPluginData.Builder.class, name = "build", args = {})
	public void testBuild() {
		// test covered by remaining tests
	}

	@Test
	@UnitTestMethod(target = StochasticsPluginData.Builder.class, name = "setSeed", args = { long.class })
	public void testSetSeed() {
		long seed = 235234623445234756L;
		StochasticsPluginData stochasticsPluginData = StochasticsPluginData.builder().setSeed(seed).build();
		assertEquals(seed, stochasticsPluginData.getSeed());
	}

	@Test
	@UnitTestMethod(target = StochasticsPluginData.Builder.class, name = "addRandomGeneratorId", args = { RandomNumberGeneratorId.class })
	public void testAddRandomGeneratorId() {

		for (TestRandomGeneratorId testRandomGeneratorId : TestRandomGeneratorId.values()) {
			StochasticsPluginData stochasticsPluginData = StochasticsPluginData.builder().setSeed(4300202782621809065L).addRandomGeneratorId(testRandomGeneratorId).build();
			assertTrue(stochasticsPluginData.getRandomNumberGeneratorIds().contains(testRandomGeneratorId));
		}

		// precondition tests
		ContractException contractException = assertThrows(ContractException.class, () -> StochasticsPluginData.builder().setSeed(1130627593613916615L).addRandomGeneratorId(null));
		assertEquals(StochasticsError.NULL_RANDOM_NUMBER_GENERATOR_ID, contractException.getErrorType());

	}

}
