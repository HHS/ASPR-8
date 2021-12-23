package plugins.stochastics.initialdata;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.LinkedHashSet;
import java.util.Set;

import org.junit.jupiter.api.Test;

import nucleus.DataView;
import plugins.stochastics.support.RandomNumberGeneratorId;
import plugins.stochastics.support.StochasticsError;
import plugins.stochastics.testsupport.TestRandomGeneratorId;
import util.ContractException;
import util.annotations.UnitTest;
import util.annotations.UnitTestMethod;

@UnitTest(target = StochasticsInitialData.class)
public class AT_StochasticsInitialData implements DataView {

	@Test
	@UnitTestMethod(name = "builder", args = {})
	public void testBuilder() {
		// show that the builder returns a non-null instance of
		// StochasticsInitialData.Builder
		assertNotNull(StochasticsInitialData.builder());
	}

	@Test
	@UnitTestMethod(target = StochasticsInitialData.Builder.class, name = "build", args = {})
	public void testBuild() {
		// test covered by remaining tests
	}

	@Test
	@UnitTestMethod(target = StochasticsInitialData.Builder.class, name = "addRandomGeneratorId", args = { RandomNumberGeneratorId.class })
	public void testAddRandomGeneratorId() {
		for (TestRandomGeneratorId testRandomGeneratorId : TestRandomGeneratorId.values()) {
			StochasticsInitialData stochasticsInitialData = StochasticsInitialData.builder().setSeed(4300202782621809065L).addRandomGeneratorId(testRandomGeneratorId).build();
			assertTrue(stochasticsInitialData.getRandomNumberGeneratorIds().contains(testRandomGeneratorId));
		}

		// precondition tests
		ContractException contractException = assertThrows(ContractException.class, () -> StochasticsInitialData.builder().setSeed(1130627593613916615L).addRandomGeneratorId(null));
		assertEquals(StochasticsError.NULL_RANDOM_NUMBER_GENERATOR_ID, contractException.getErrorType());

	}

	@Test
	@UnitTestMethod(target = StochasticsInitialData.Builder.class, name = "setSeed", args = { RandomNumberGeneratorId.class })
	public void testSetSeed() {
		
		long seed = 235234623445234756L;

		StochasticsInitialData stochasticsInitialData = StochasticsInitialData.builder().setSeed(seed).build();
		assertEquals(seed, stochasticsInitialData.getSeed());
	}

	@Test
	@UnitTestMethod(name = "getRandomNumberGeneratorIds", args = {})
	public void testGetRandomNumberGeneratorIds() {
		Set<RandomNumberGeneratorId> expectedRandomNumberGeneratorIds = new LinkedHashSet<>();
		StochasticsInitialData.Builder builder = StochasticsInitialData.builder();
		for (TestRandomGeneratorId testRandomGeneratorId : TestRandomGeneratorId.values()) {
			expectedRandomNumberGeneratorIds.add(testRandomGeneratorId);
			builder.addRandomGeneratorId(testRandomGeneratorId);
		}
		builder.setSeed(3244635455542808061L);
		StochasticsInitialData stochasticsInitialData = builder.build();
		Set<RandomNumberGeneratorId> actualRandomNumberGeneratorIds = stochasticsInitialData.getRandomNumberGeneratorIds();
		assertEquals(expectedRandomNumberGeneratorIds, actualRandomNumberGeneratorIds);
	}

}
