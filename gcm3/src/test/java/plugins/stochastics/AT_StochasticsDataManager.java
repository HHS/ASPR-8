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
import nucleus.util.ContractException;
import plugins.stochastics.support.RandomNumberGeneratorId;
import plugins.stochastics.support.StochasticsError;
import plugins.stochastics.testsupport.StochasticsActionSupport;
import plugins.stochastics.testsupport.TestRandomGeneratorId;
import tools.annotations.UnitTest;
import tools.annotations.UnitTestConstructor;
import tools.annotations.UnitTestMethod;

@UnitTest(target = StochasticsDataManager.class)
public class AT_StochasticsDataManager {

	@Test
	@UnitTestMethod(name = "init", args = { DataManagerContext.class })
	public void testInit() {
		// nothing to test
	}

	@Test
	@UnitTestMethod(name = "getRandomNumberGeneratorIds", args = {})
	public void testGetRandomNumberGeneratorIds() {

		StochasticsActionSupport.testConsumer(2276874395058795370L, (c) -> {

			StochasticsDataManager stochasticsDataManager = c.getDataManager(StochasticsDataManager.class).get();

			Set<RandomNumberGeneratorId> actualRandomNumberGeneratorIds = stochasticsDataManager.getRandomNumberGeneratorIds();

			Set<TestRandomGeneratorId> expectedRandomGeneratorIds = new LinkedHashSet<>();
			for (TestRandomGeneratorId testRandomGeneratorId : TestRandomGeneratorId.values()) {
				expectedRandomGeneratorIds.add(testRandomGeneratorId);
			}
			assertEquals(expectedRandomGeneratorIds, actualRandomNumberGeneratorIds);
		});
	}

	@Test
	@UnitTestMethod(name = "getRandomGeneratorFromId", args = { RandomNumberGeneratorId.class })
	public void testGetRandomGeneratorFromId() {

		// show that random generators can be retrieved by ids.
		StochasticsActionSupport.testConsumer(5489824520767978373L, (c) -> {
			StochasticsDataManager stochasticsDataManager = c.getDataManager(StochasticsDataManager.class).get();
			for (TestRandomGeneratorId testRandomGeneratorId : TestRandomGeneratorId.values()) {
				RandomGenerator randomGeneratorFromId = stochasticsDataManager.getRandomGeneratorFromId(testRandomGeneratorId);
				assertNotNull(randomGeneratorFromId);
			}
		});
		
		// show that an unknown random number generator id will retrieve a random generator
		StochasticsActionSupport.testConsumer(2276874395058795370L, (c) -> {
			StochasticsDataManager stochasticsDataManager = c.getDataManager(StochasticsDataManager.class).get();
			
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


			//show that random number generators can be retrieved for new id values
			RandomGenerator randomGeneratorFromIdA = stochasticsDataManager.getRandomGeneratorFromId(randomNumberGeneratorIdA);
			assertNotNull(randomGeneratorFromIdA);
			
			RandomGenerator randomGeneratorFromIdB = stochasticsDataManager.getRandomGeneratorFromId(randomNumberGeneratorIdB);
			assertNotNull(randomGeneratorFromIdB);
			
			//show that the random generators are identical since their ids evaluate to same string and were generated under the same base seed value(no reseed invocations between generators)
			
			for(int i = 0;i<10;i++) {
				long valueA = randomGeneratorFromIdA.nextLong();
				long valueB = randomGeneratorFromIdB.nextLong();
				assertEquals(valueA, valueB);
			}
			
		});


		// precondition test : if the random number generator is null
		StochasticsActionSupport.testConsumer(5489824520767978373L, (c) -> {
			StochasticsDataManager stochasticsDataManager = c.getDataManager(StochasticsDataManager.class).get();
			ContractException contractException = assertThrows(ContractException.class, () -> stochasticsDataManager.getRandomGeneratorFromId(null));
			assertEquals(StochasticsError.NULL_RANDOM_NUMBER_GENERATOR_ID, contractException.getErrorType());
		});


	}

	@Test
	@UnitTestMethod(name = "getRandomGenerator", args = {})
	public void testGetRandomGenerator() {
		// show that random generators can be retrieved by ids
		StochasticsActionSupport.testConsumer(683597885444214892L, (c) -> {
			StochasticsDataManager stochasticsDataManager = c.getDataManager(StochasticsDataManager.class).get();
			RandomGenerator randomGeneratorFromId = stochasticsDataManager.getRandomGenerator();
			assertNotNull(randomGeneratorFromId);
		});
	}

	@Test
	@UnitTestConstructor(args = { StochasticsPluginData.class })
	public void testConstructor() {
		// test of constructor is covered by the method tests
	}

	@Test
	@UnitTestMethod(name = "resetSeeds", args = { long.class })
	public void testResetSeeds() {

		StochasticsActionSupport.testConsumer(7392476210385850542L, (c) -> {

			StochasticsDataManager stochasticsDataManager = c.getDataManager(StochasticsDataManager.class).get();

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

		});

	}

}
