package plugins.stochastics;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.math3.random.RandomGenerator;
import org.junit.jupiter.api.Test;

import nucleus.DataManagerContext;
import nucleus.testsupport.testplugin.TestSimulation;
import plugins.stochastics.support.RandomNumberGeneratorId;
import plugins.stochastics.support.StochasticsError;
import plugins.stochastics.testsupport.StochasticsTestPluginFactory;
import plugins.stochastics.testsupport.TestRandomGeneratorId;
import tools.annotations.UnitTestConstructor;
import tools.annotations.UnitTestMethod;
import util.errors.ContractException;

public class AT_StochasticsDataManager {

	@Test
	@UnitTestMethod(target = StochasticsDataManager.class, name = "init", args = { DataManagerContext.class })
	public void testInit() {
		// nothing to test
	}

	@Test
	@UnitTestMethod(target = StochasticsDataManager.class, name = "getRandomNumberGeneratorIds", args = {})
	public void testGetRandomNumberGeneratorIds() {

		TestSimulation.executeSimulation(StochasticsTestPluginFactory.factory(1244273915891145733L, (c) -> {

			StochasticsDataManager stochasticsDataManager = c.getDataManager(StochasticsDataManager.class);

			Set<RandomNumberGeneratorId> actualRandomNumberGeneratorIds = stochasticsDataManager.getRandomNumberGeneratorIds();

			Set<TestRandomGeneratorId> expectedRandomGeneratorIds = new LinkedHashSet<>();
			for (TestRandomGeneratorId testRandomGeneratorId : TestRandomGeneratorId.values()) {
				expectedRandomGeneratorIds.add(testRandomGeneratorId);
			}
			assertEquals(expectedRandomGeneratorIds, actualRandomNumberGeneratorIds);
		}).getPlugins());
	}

	@Test
	@UnitTestMethod(target = StochasticsDataManager.class, name = "getRandomGeneratorFromId", args = { RandomNumberGeneratorId.class })
	public void testGetRandomGeneratorFromId() {

		// show that random generators can be retrieved by ids.
		TestSimulation.executeSimulation(StochasticsTestPluginFactory.factory(5489824520767978373L, (c) -> {
			StochasticsDataManager stochasticsDataManager = c.getDataManager(StochasticsDataManager.class);
			for (TestRandomGeneratorId testRandomGeneratorId : TestRandomGeneratorId.values()) {
				RandomGenerator randomGeneratorFromId = stochasticsDataManager.getRandomGeneratorFromId(testRandomGeneratorId);
				assertNotNull(randomGeneratorFromId);
			}
		}).getPlugins());

		// show that an unknown random number generator id will retrieve a
		// random generator
		TestSimulation.executeSimulation(StochasticsTestPluginFactory.factory(5985120270606833945L, (c) -> {
			StochasticsDataManager stochasticsDataManager = c.getDataManager(StochasticsDataManager.class);

			RandomNumberGeneratorId randomNumberGeneratorIdA = new RandomNumberGeneratorId() {
				@Override
				public String toString() {
					return "some string";
				}
			};

			RandomNumberGeneratorId randomNumberGeneratorIdB = new RandomNumberGeneratorId() {
				@Override
				public String toString() {
					return "some string";
				}
			};

			// show that random number generators can be retrieved for new id
			// values
			RandomGenerator randomGeneratorFromIdA = stochasticsDataManager.getRandomGeneratorFromId(randomNumberGeneratorIdA);
			assertNotNull(randomGeneratorFromIdA);

			RandomGenerator randomGeneratorFromIdB = stochasticsDataManager.getRandomGeneratorFromId(randomNumberGeneratorIdB);
			assertNotNull(randomGeneratorFromIdB);

			// show that the random generators are identical since their ids
			// evaluate to same string and were generated under the same base
			// seed value(no reseed invocations between generators)

			for (int i = 0; i < 10; i++) {
				long valueA = randomGeneratorFromIdA.nextLong();
				long valueB = randomGeneratorFromIdB.nextLong();
				assertEquals(valueA, valueB);
			}

		}).getPlugins());

		// precondition test : if the random number generator is null
		TestSimulation.executeSimulation(StochasticsTestPluginFactory.factory(1893848105389404535L, (c) -> {
			StochasticsDataManager stochasticsDataManager = c.getDataManager(StochasticsDataManager.class);
			ContractException contractException = assertThrows(ContractException.class, () -> stochasticsDataManager.getRandomGeneratorFromId(null));
			assertEquals(StochasticsError.NULL_RANDOM_NUMBER_GENERATOR_ID, contractException.getErrorType());
		}).getPlugins());

	}

	@Test
	@UnitTestMethod(target = StochasticsDataManager.class, name = "getRandomGenerator", args = {})
	public void testGetRandomGenerator() {
		// show that random generators can be retrieved by ids
		TestSimulation.executeSimulation(StochasticsTestPluginFactory.factory(683597885444214892L, (c) -> {
			StochasticsDataManager stochasticsDataManager = c.getDataManager(StochasticsDataManager.class);
			RandomGenerator randomGeneratorFromId = stochasticsDataManager.getRandomGenerator();
			assertNotNull(randomGeneratorFromId);
		}).getPlugins());
	}

	@Test
	@UnitTestConstructor(target = StochasticsDataManager.class, args = { StochasticsPluginData.class })
	public void testConstructor() {
		// test of constructor is covered by the method tests
	}

	@Test
	@UnitTestMethod(target = StochasticsDataManager.class, name = "resetSeeds", args = { long.class })
	public void testResetSeeds() {

		TestSimulation.executeSimulation(StochasticsTestPluginFactory.factory(7392476210385850542L, (c) -> {

			StochasticsDataManager stochasticsDataManager = c.getDataManager(StochasticsDataManager.class);

			RandomGenerator randomGenerator = stochasticsDataManager.getRandomGenerator();

			long seed1 = randomGenerator.nextLong();

			long seed2 = randomGenerator.nextLong();

			/*
			 * Establish the first long generated by each random number
			 * generator after resetting the seed to seed1
			 */
			stochasticsDataManager.resetSeeds(seed1);

			randomGenerator = stochasticsDataManager.getRandomGenerator();
			long expectedGeneratorValue = randomGenerator.nextLong();

			Map<TestRandomGeneratorId, Long> expectedGeneratorValues = new LinkedHashMap<>();
			for (TestRandomGeneratorId testRandomGeneratorId : TestRandomGeneratorId.values()) {
				long value = stochasticsDataManager.getRandomGeneratorFromId(testRandomGeneratorId).nextLong();
				expectedGeneratorValues.put(testRandomGeneratorId, value);
			}

			/*
			 * re-seed to a new seed and show that the initial longs returned
			 * changed for all generators
			 */
			stochasticsDataManager.resetSeeds(seed2);
			randomGenerator = stochasticsDataManager.getRandomGenerator();

			assertNotEquals(expectedGeneratorValue, randomGenerator.nextLong());

			for (TestRandomGeneratorId testRandomGeneratorId : TestRandomGeneratorId.values()) {
				long value = stochasticsDataManager.getRandomGeneratorFromId(testRandomGeneratorId).nextLong();
				assertNotEquals(expectedGeneratorValues.get(testRandomGeneratorId), value);
			}

			/*
			 * re-seed to the original seed and show that the values are as they
			 * were
			 */
			stochasticsDataManager.resetSeeds(seed1);
			randomGenerator = stochasticsDataManager.getRandomGenerator();

			assertEquals(expectedGeneratorValue, randomGenerator.nextLong());

			for (TestRandomGeneratorId testRandomGeneratorId : TestRandomGeneratorId.values()) {
				long value = stochasticsDataManager.getRandomGeneratorFromId(testRandomGeneratorId).nextLong();
				assertEquals(expectedGeneratorValues.get(testRandomGeneratorId), value);
			}

		}).getPlugins());

	}

}
