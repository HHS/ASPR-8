package plugins.stochastics.datacontainers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.math3.random.RandomGenerator;
import org.apache.commons.math3.random.Well44497b;
import org.junit.jupiter.api.Test;

import nucleus.Context;
import nucleus.testsupport.MockContext;
import plugins.stochastics.support.RandomNumberGeneratorId;
import plugins.stochastics.testsupport.TestRandomGeneratorId;
import util.annotations.UnitTest;
import util.annotations.UnitTestConstructor;
import util.annotations.UnitTestMethod;

@UnitTest(target = StochasticsDataView.class)
public class AT_StochasticsDataView {

	@Test
	@UnitTestConstructor(args = { Context.class, StochasticsDataManager.class })
	public void testConstructor() {
		// test of constructor is covered by the method tests
	}

	@Test
	@UnitTestMethod(name = "getRandomGenerator", args = {})
	public void testGetRandomGenerator() {
		// show that distinct instances of Well44497b are not equal
		assertNotEquals(new Well44497b(), new Well44497b());
		// build the manager
		StochasticsDataManager stochasticsDataManager = new StochasticsDataManager();

		RandomGenerator expectedRandomGenerator = new Well44497b();
		stochasticsDataManager.setGeneralRandomGenerator(expectedRandomGenerator);

		StochasticsDataView stochasticsDataView = new StochasticsDataView(MockContext.builder().build(), stochasticsDataManager);

		// show that the data view returns the correct random generator
		RandomGenerator actualRandomGenerator = stochasticsDataView.getRandomGenerator();

		// show that the random generator is the expected one
		assertEquals(expectedRandomGenerator, actualRandomGenerator);
	}

	@Test
	@UnitTestMethod(name = "getRandomGeneratorFromId", args = { RandomNumberGeneratorId.class })
	public void testGetRandomGeneratorFromId() {
		// show that distinct instances of Well44497b are not equal
		assertNotEquals(new Well44497b(), new Well44497b());

		// build the manager
		StochasticsDataManager stochasticsDataManager = new StochasticsDataManager();

		// show that we are contributing random generator ids
		assertTrue(TestRandomGeneratorId.values().length > 0);

		// build the initial data
		Map<TestRandomGeneratorId, RandomGenerator> expectedRandomGenerators = new LinkedHashMap<>();

		for (TestRandomGeneratorId testRandomGeneratorId : TestRandomGeneratorId.values()) {
			RandomGenerator randomGenerator = new Well44497b();
			expectedRandomGenerators.put(testRandomGeneratorId, randomGenerator);
			stochasticsDataManager.setRandomGeneratorById(testRandomGeneratorId, randomGenerator);
		}

		StochasticsDataView stochasticsDataView = new StochasticsDataView(MockContext.builder().build(), stochasticsDataManager);

		for (TestRandomGeneratorId testRandomGeneratorId : TestRandomGeneratorId.values()) {
			// show that the data view returns the correct random generator
			RandomGenerator expectedRandomGenerator = expectedRandomGenerators.get(testRandomGeneratorId);

			RandomGenerator actualRandomGenerator = stochasticsDataView.getRandomGeneratorFromId(testRandomGeneratorId);

			assertEquals(expectedRandomGenerator, actualRandomGenerator);

		}
	}

	@Test
	@UnitTestMethod(name = "getRandomNumberGeneratorIds", args = {})
	public void testGetRandomNumberGeneratorIds() {
		// build the manager
		StochasticsDataManager stochasticsDataManager = new StochasticsDataManager();

		// show that we are contributing random generator ids
		assertTrue(TestRandomGeneratorId.values().length > 0);

		// build the initial data
		Set<TestRandomGeneratorId> expectedRandomGeneratorIds = new LinkedHashSet<>();

		for (TestRandomGeneratorId testRandomGeneratorId : TestRandomGeneratorId.values()) {
			expectedRandomGeneratorIds.add(testRandomGeneratorId);
			stochasticsDataManager.setRandomGeneratorById(testRandomGeneratorId, new Well44497b());
		}
		

		StochasticsDataView stochasticsDataView = new StochasticsDataView(MockContext.builder().build(), stochasticsDataManager);

		// show that the data view returns the correct random generator ids
		Set<RandomNumberGeneratorId> actualRandomNumberGeneratorIds = stochasticsDataView.getRandomNumberGeneratorIds();
		assertEquals(expectedRandomGeneratorIds, actualRandomNumberGeneratorIds);
	}

}
