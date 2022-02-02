package plugins.stochastics;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.math3.random.RandomGenerator;
import org.apache.commons.math3.random.Well44497b;
import org.junit.jupiter.api.Test;

import nucleus.Context;
import nucleus.DataView;
import nucleus.ResolverContext;
import nucleus.Simulation;
import nucleus.Simulation.Builder;
import nucleus.testsupport.MockContext;
import nucleus.testsupport.MockResolverContext;
import nucleus.testsupport.actionplugin.ActionPlugin;
import nucleus.testsupport.actionplugin.AgentActionPlan;
import plugins.stochastics.StochasticsDataView;
import plugins.stochastics.StochasticsPlugin;
import plugins.stochastics.support.RandomNumberGeneratorId;
import plugins.stochastics.testsupport.StochasticsActionSupport;
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
		long seed = 5427457445645345L;
		StochasticsPlugin stochasticsPlugin = StochasticsPlugin.builder().setSeed(seed).build();

		RandomGenerator expectedRandomGenerator = new Well44497b(seed);

		StochasticsDataView stochasticsDataView = new StochasticsDataView(stochasticsPlugin);

		// show that the data view returns the correct random generator
		RandomGenerator actualRandomGenerator = stochasticsDataView.getRandomGenerator();

		// show that the random generator is the expected one
		assertEquals(expectedRandomGenerator.getClass(), actualRandomGenerator.getClass());

		for (int i = 0; i < 5; i++) {
			assertEquals(expectedRandomGenerator.nextLong(),actualRandomGenerator.nextLong());
		}

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

	@Test
	@UnitTestMethod(name = "init", args = { ResolverContext.class })
	public void testStochasticsReseedEvent() {
		long masterSeed = 508143430508125725L;
		StochasticsActionSupport.testConsumer(masterSeed,(c)->{
			StochasticsDataView stochasticsDataView = c.getDataView(StochasticsDataView.class).get();
			RandomGenerator randomGenerator = stochasticsDataView.getRandomGenerator();

			/*
			 * Establish the first long generated by each random number
			 * generator
			 */
			long expectedValue = randomGenerator.nextLong();

			Map<TestRandomGeneratorId, Long> expectedValues = new LinkedHashMap<>();
			for (TestRandomGeneratorId testRandomGeneratorId : TestRandomGeneratorId.values()) {
				long value = stochasticsDataView.getRandomGeneratorFromId(testRandomGeneratorId).nextLong();
				expectedValues.put(testRandomGeneratorId, value);
			}

			/*
			 * re-seed to a new seed and show that the initial longs returned
			 * changed for all generators
			 */
			stochasticsDataView.resetSeeds(3885859435446444843L);
			
			long actualValue = randomGenerator.nextLong();
			assertNotEquals(expectedValue, actualValue);
			for (TestRandomGeneratorId testRandomGeneratorId : TestRandomGeneratorId.values()) {
				actualValue = stochasticsDataView.getRandomGeneratorFromId(testRandomGeneratorId).nextLong();
				assertNotEquals(expectedValues.get(testRandomGeneratorId), actualValue);
			}

			/*
			 * re-seed back to the original seed and show that the initial longs
			 * returned for all generators are correct
			 */
			stochasticsDataView.resetSeeds(masterSeed);
			
			actualValue = randomGenerator.nextLong();
			assertEquals(expectedValue, actualValue);
			for (TestRandomGeneratorId testRandomGeneratorId : TestRandomGeneratorId.values()) {
				actualValue = stochasticsDataView.getRandomGeneratorFromId(testRandomGeneratorId).nextLong();
				assertEquals(expectedValues.get(testRandomGeneratorId), actualValue);
			}

		});

	}

	////////// Really tests the construction from plugin
	@Test
	@UnitTestMethod(name = "init", args = { ResolverContext.class })
	public void testStochasticsDataViewInitialzation() {
		long seed = 745645785689L;

		// show that the stochastics data view is published and has the correct
		// state

		// show that we are contributing random generator ids
		assertTrue(TestRandomGeneratorId.values().length > 0);

		// build the initial data
		Set<TestRandomGeneratorId> expectedRandomGeneratorIds = new LinkedHashSet<>();
		StochasticsPlugin.Builder builder = StochasticsPlugin.builder();
		for (TestRandomGeneratorId testRandomGeneratorId : TestRandomGeneratorId.values()) {
			expectedRandomGeneratorIds.add(testRandomGeneratorId);
			builder.addRandomGeneratorId(testRandomGeneratorId);
		}
		builder.setSeed(seed);
		StochasticsPlugin stochasticsPlugin = builder.build();

		List<DataView> publishedDataViews = new ArrayList<>();

		// build the manager
		MockResolverContext mockResolverContext = MockResolverContext.builder().setPublishDataViewConsumer((d) -> publishedDataViews.add(d)).build();
		StochasticsResolver stochasticsResolver = new StochasticsResolver(stochasticsPlugin);
		stochasticsResolver.init(mockResolverContext);

		// show that only one data view was published
		assertEquals(1, publishedDataViews.size());

		// show that the published data view is not null
		DataView dataView = publishedDataViews.get(0);
		assertNotNull(dataView);

		// show that the published data view is a StochasticsDataView
		assertEquals(StochasticsDataView.class, dataView.getClass());

		StochasticsDataView stochasticsDataView = (StochasticsDataView) dataView;

		// show that the data view returns the correct random generator
		RandomGenerator randomGenerator = stochasticsDataView.getRandomGenerator();
		// show that the random generator is not null
		assertNotNull(randomGenerator);
		// show that the random generator is the expected implementor
		assertEquals(Well44497b.class, randomGenerator.getClass());

		// show that the random generator is likely to have been seeded
		// correctly
		Well44497b well44497b = new Well44497b(seed);
		for (int i = 0; i < 100; i++) {
			assertEquals(well44497b.nextLong(), randomGenerator.nextLong());
		}

		// show that the data view returns the correct random generator ids
		Set<RandomNumberGeneratorId> actualRandomNumberGeneratorIds = stochasticsDataView.getRandomNumberGeneratorIds();
		assertEquals(expectedRandomGeneratorIds, actualRandomNumberGeneratorIds);

		// show that the random generators associated with id values are correct
		for (TestRandomGeneratorId testRandomGeneratorId : TestRandomGeneratorId.values()) {
			// show that the data view returns the correct random generator
			randomGenerator = stochasticsDataView.getRandomGeneratorFromId(testRandomGeneratorId);
			// show that the random generator is not null
			assertNotNull(randomGenerator);
			// show that the random generator is the expected implementor
			assertEquals(Well44497b.class, randomGenerator.getClass());

			// show that the random generator is likely to have been seeded
			// correctly
			well44497b = new Well44497b(seed + testRandomGeneratorId.toString().hashCode());
			for (int i = 0; i < 100; i++) {
				assertEquals(well44497b.nextLong(), randomGenerator.nextLong());
			}
		}

	}
}
