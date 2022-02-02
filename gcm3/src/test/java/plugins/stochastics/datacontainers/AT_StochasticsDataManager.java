package plugins.stochastics.datacontainers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.math3.random.RandomGenerator;
import org.apache.commons.math3.random.Well44497b;
import org.junit.jupiter.api.Test;

import plugins.stochastics.support.RandomNumberGeneratorId;
import plugins.stochastics.testsupport.TestRandomGeneratorId;
import util.annotations.UnitTest;
import util.annotations.UnitTestConstructor;
import util.annotations.UnitTestMethod;

@UnitTest(target = StochasticsDataManager.class)
public final class AT_StochasticsDataManager {

	@Test
	@UnitTestConstructor(args = {})
	public void testConstructor() {
		// test of constructor is covered by the method tests
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

		// show that the manager returns the correct random generator ids
		Set<RandomNumberGeneratorId> actualRandomNumberGeneratorIds = stochasticsDataManager.getRandomNumberGeneratorIds();
		assertEquals(expectedRandomGeneratorIds, actualRandomNumberGeneratorIds);
	}

	@Test
	@UnitTestMethod(name = "getRandomGenerator", args = {})
	public void testGetRandomGenerator() {

		//show that distinct instances of Well44497b are not equal
		assertNotEquals(new Well44497b(), new Well44497b());

		// show that we are contributing random generator ids
		assertTrue(TestRandomGeneratorId.values().length > 0);

		// build the manager
		StochasticsDataManager stochasticsDataManager = new StochasticsDataManager();
		RandomGenerator expectedRandomGenerator = new Well44497b();
		stochasticsDataManager.setGeneralRandomGenerator(expectedRandomGenerator);

		// show that the manager returns the correct random generator
		RandomGenerator actualRandomGenerator = stochasticsDataManager.getRandomGenerator();

		// show that the random generator is the one added
		assertEquals(expectedRandomGenerator,actualRandomGenerator);
	}

	@Test
	@UnitTestMethod(name = "randomGeneratorFromIdExists", args = { RandomNumberGeneratorId.class })
	public void testRandomGeneratorFromIdExists() {
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

		// show that the manager indicates the random generator id exists for
		// each id in the initial data
		for (TestRandomGeneratorId testRandomGeneratorId : TestRandomGeneratorId.values()) {
			assertTrue(stochasticsDataManager.randomGeneratorFromIdExists(testRandomGeneratorId));
		}

		// show that other random generator ids do not exist
		assertFalse(stochasticsDataManager.randomGeneratorFromIdExists(TestRandomGeneratorId.getUnknownRandomNumberGeneratorId()));

	}

	@Test
	@UnitTestMethod(name = "getRandomGeneratorFromId", args = { RandomNumberGeneratorId.class })
	public void testGetRandomGeneratorFromId() {
		//show that distinct instances of Well44497b are not equal
		assertNotEquals(new Well44497b(), new Well44497b());

		// show that we are contributing random generator ids
		assertTrue(TestRandomGeneratorId.values().length > 0);

		// build the manager
		StochasticsDataManager stochasticsDataManager = new StochasticsDataManager();

		Map<TestRandomGeneratorId,RandomGenerator> expectedRandomGenerators = new LinkedHashMap<>();

		for (TestRandomGeneratorId testRandomGeneratorId : TestRandomGeneratorId.values()) {
			RandomGenerator randomGenerator = new Well44497b();
			expectedRandomGenerators.put(testRandomGeneratorId,randomGenerator);
			stochasticsDataManager.setRandomGeneratorById(testRandomGeneratorId, randomGenerator);
		}

		for (TestRandomGeneratorId testRandomGeneratorId : TestRandomGeneratorId.values()) {
			RandomGenerator expectedRandomGenerator = expectedRandomGenerators.get(testRandomGeneratorId);
			// show that the manager returns the correct random generator
			RandomGenerator actualRandomGenerator = stochasticsDataManager.getRandomGeneratorFromId(testRandomGeneratorId);
			// show that the random generator is the expected one
			assertEquals(expectedRandomGenerator,actualRandomGenerator);
		}

	}
}
