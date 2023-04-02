package plugins.stochastics;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.LinkedHashSet;
import java.util.Set;

import org.apache.commons.math3.random.RandomGenerator;
import org.junit.jupiter.api.Test;

import nucleus.DataManagerContext;
import nucleus.testsupport.testplugin.TestSimulation;
import plugins.stochastics.support.RandomNumberGeneratorId;
import plugins.stochastics.support.StochasticsError;
import plugins.stochastics.testsupport.StochasticsTestPluginFactory;
import plugins.stochastics.testsupport.StochasticsTestPluginFactory.Factory;
import plugins.stochastics.testsupport.TestRandomGeneratorId;
import util.annotations.UnitTestConstructor;
import util.annotations.UnitTestMethod;
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

		Factory factory = StochasticsTestPluginFactory.factory(1244273915891145733L, (c) -> {

			StochasticsDataManager stochasticsDataManager = c.getDataManager(StochasticsDataManager.class);

			Set<RandomNumberGeneratorId> actualRandomNumberGeneratorIds = stochasticsDataManager.getRandomNumberGeneratorIds();

			Set<TestRandomGeneratorId> expectedRandomGeneratorIds = new LinkedHashSet<>();
			for (TestRandomGeneratorId testRandomGeneratorId : TestRandomGeneratorId.values()) {
				expectedRandomGeneratorIds.add(testRandomGeneratorId);
			}
			assertEquals(expectedRandomGeneratorIds, actualRandomNumberGeneratorIds);
		});
		TestSimulation.builder().addPlugins(factory.getPlugins()).build().execute();
	}

	@Test
	@UnitTestMethod(target = StochasticsDataManager.class, name = "getRandomGeneratorFromId", args = { RandomNumberGeneratorId.class })
	public void testGetRandomGeneratorFromId() {

		// show that random generators can be retrieved by ids.
		Factory factory = StochasticsTestPluginFactory.factory(5489824520767978373L, (c) -> {
			StochasticsDataManager stochasticsDataManager = c.getDataManager(StochasticsDataManager.class);
			for (TestRandomGeneratorId testRandomGeneratorId : TestRandomGeneratorId.values()) {
				RandomGenerator randomGeneratorFromId = stochasticsDataManager.getRandomGeneratorFromId(testRandomGeneratorId);
				assertNotNull(randomGeneratorFromId);
			}
		});

		TestSimulation.builder().addPlugins(factory.getPlugins()).build().execute();
		
		// show that an unknown random number generator id will retrieve a
		// random generator
		factory = StochasticsTestPluginFactory.factory(5985120270606833945L, (c) -> {
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

		});
		TestSimulation.builder().addPlugins(factory.getPlugins()).build().execute();

		// precondition test : if the random number generator is null
		ContractException contractException = assertThrows(ContractException.class, () ->{
			Factory factory2 = StochasticsTestPluginFactory.factory(1893848105389404535L, (c) -> {
				StochasticsDataManager stochasticsDataManager = c.getDataManager(StochasticsDataManager.class);
				stochasticsDataManager.getRandomGeneratorFromId(null);			
			});
			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();
		});
		assertEquals(StochasticsError.NULL_RANDOM_NUMBER_GENERATOR_ID, contractException.getErrorType());
		

	}

	@Test
	@UnitTestMethod(target = StochasticsDataManager.class, name = "getRandomGenerator", args = {})
	public void testGetRandomGenerator() {
		// show that random generators can be retrieved by ids
		Factory factory = StochasticsTestPluginFactory.factory(683597885444214892L, (c) -> {
			StochasticsDataManager stochasticsDataManager = c.getDataManager(StochasticsDataManager.class);
			RandomGenerator randomGeneratorFromId = stochasticsDataManager.getRandomGenerator();
			assertNotNull(randomGeneratorFromId);
		});
		TestSimulation.builder().addPlugins(factory.getPlugins()).build().execute();
	}

	@Test
	@UnitTestConstructor(target = StochasticsDataManager.class, args = { StochasticsPluginData.class })
	public void testConstructor() {
		// test of constructor is covered by the method tests
	}

	

}
