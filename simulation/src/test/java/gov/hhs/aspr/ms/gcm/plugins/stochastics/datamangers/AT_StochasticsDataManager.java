package gov.hhs.aspr.ms.gcm.plugins.stochastics.datamangers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.IntStream;

import org.apache.commons.math3.random.RandomGenerator;
import org.apache.commons.math3.util.FastMath;
import org.apache.commons.math3.util.Pair;
import org.junit.jupiter.api.Test;

import gov.hhs.aspr.ms.gcm.nucleus.ActorContext;
import gov.hhs.aspr.ms.gcm.nucleus.DataManagerContext;
import gov.hhs.aspr.ms.gcm.nucleus.Plugin;
import gov.hhs.aspr.ms.gcm.nucleus.Simulation;
import gov.hhs.aspr.ms.gcm.nucleus.SimulationState;
import gov.hhs.aspr.ms.gcm.nucleus.testsupport.runcontinuityplugin.RunContinuityPlugin;
import gov.hhs.aspr.ms.gcm.nucleus.testsupport.runcontinuityplugin.RunContinuityPluginData;
import gov.hhs.aspr.ms.gcm.nucleus.testsupport.testplugin.TestActorPlan;
import gov.hhs.aspr.ms.gcm.nucleus.testsupport.testplugin.TestOutputConsumer;
import gov.hhs.aspr.ms.gcm.nucleus.testsupport.testplugin.TestPluginData;
import gov.hhs.aspr.ms.gcm.nucleus.testsupport.testplugin.TestSimulation;
import gov.hhs.aspr.ms.gcm.plugins.stochastics.StochasticsPlugin;
import gov.hhs.aspr.ms.gcm.plugins.stochastics.datamanagers.StochasticsDataManager;
import gov.hhs.aspr.ms.gcm.plugins.stochastics.datamanagers.StochasticsPluginData;
import gov.hhs.aspr.ms.gcm.plugins.stochastics.support.RandomNumberGeneratorId;
import gov.hhs.aspr.ms.gcm.plugins.stochastics.support.StochasticsError;
import gov.hhs.aspr.ms.gcm.plugins.stochastics.support.Well;
import gov.hhs.aspr.ms.gcm.plugins.stochastics.support.WellState;
import gov.hhs.aspr.ms.gcm.plugins.stochastics.testsupport.StochasticsTestPluginFactory;
import gov.hhs.aspr.ms.gcm.plugins.stochastics.testsupport.StochasticsTestPluginFactory.Factory;
import gov.hhs.aspr.ms.util.annotations.UnitTestConstructor;
import gov.hhs.aspr.ms.util.annotations.UnitTestMethod;
import gov.hhs.aspr.ms.util.errors.ContractException;
import gov.hhs.aspr.ms.util.random.RandomGeneratorProvider;
import gov.hhs.aspr.ms.gcm.plugins.stochastics.testsupport.TestRandomGeneratorId;

public class AT_StochasticsDataManager {

	/**
	 * Demonstrates that the data manager exhibits run continuity. The state of the
	 * data manager is not effected by repeatedly starting and stopping the
	 * simulation.
	 */
	@Test
	@UnitTestMethod(target = StochasticsDataManager.class, name = "init", args = { DataManagerContext.class })
	public void testStateContinuity() {

		/*
		 * Note that we are not testing the content of the plugin datas -- that is
		 * covered by the other state tests. We show here only that the resulting plugin
		 * data state is the same without regard to how we break up the run.
		 */

		Set<String> pluginDatas = new LinkedHashSet<>();
		pluginDatas.add(testStateContinuity(1));
		pluginDatas.add(testStateContinuity(6));
		pluginDatas.add(testStateContinuity(15));

		assertEquals(1, pluginDatas.size());

	}

	/*
	 * Returns the StochasticsPluginData resulting from several random draws over
	 * several days. Attempt to stop and start the simulation by the given number of
	 * increments.
	 */
	private String testStateContinuity(int incrementCount) {

		String result = null;

		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(4693559432563807708L);
		/*
		 * Build the RunContinuityPluginData with five context consumers that will add
		 * and remove people over several days
		 */
		RunContinuityPluginData.Builder continuityBuilder = RunContinuityPluginData.builder();

		int n = 15;

		IntStream.range(0, n).forEach((i) -> {
			double time = randomGenerator.nextDouble() * 10;
			continuityBuilder.addContextConsumer(time, (c) -> {
				StochasticsDataManager stochasticsDataManager = c.getDataManager(StochasticsDataManager.class);

				// attempt to add a new rng
				List<TestRandomGeneratorId> candidates = new ArrayList<>();
				for (TestRandomGeneratorId id : TestRandomGeneratorId.values()) {
					if (!stochasticsDataManager.randomNumberGeneratorIdExists(id)) {
						candidates.add(id);
					}
				}
				if (!candidates.isEmpty()) {
					TestRandomGeneratorId testRandomGeneratorId = candidates
							.get(randomGenerator.nextInt(candidates.size()));
					stochasticsDataManager.addRandomNumberGenerator(testRandomGeneratorId,
							getRandomWellState(randomGenerator));
				}

				// randomly stimulate the existing rngs
				RandomGenerator rng = stochasticsDataManager.getRandomGenerator();
				if (randomGenerator.nextBoolean()) {
					rng.nextDouble();
				}
				for (RandomNumberGeneratorId id : stochasticsDataManager.getRandomNumberGeneratorIds()) {
					if (randomGenerator.nextBoolean()) {
						rng = stochasticsDataManager.getRandomGeneratorFromId(id);
						rng.nextDouble();
					}
				}

				if (i == (n - 1)) {
					c.releaseOutput(stochasticsDataManager.toString());
				}

			});
		});

		RunContinuityPluginData runContinuityPluginData = continuityBuilder.build();

		StochasticsPluginData stochasticsPluginData = StochasticsPluginData.builder()//
				.setMainRNGState(getRandomWellState(randomGenerator))//
				.build();

		// build the initial simulation state data -- time starts at zero
		SimulationState simulationState = SimulationState.builder().build();

		/*
		 * Run the simulation in one day increments until all the plans in the run
		 * continuity plugin data have been executed
		 */
		double haltTime = 0;
		double maxTime = Double.NEGATIVE_INFINITY;
		for (Pair<Double, Consumer<ActorContext>> pair : runContinuityPluginData.getConsumers()) {
			Double time = pair.getFirst();
			maxTime = FastMath.max(maxTime, time);
		}
		double timeIncrement = maxTime / incrementCount;
		while (!runContinuityPluginData.allPlansComplete()) {
			haltTime += timeIncrement;

			// build the run continuity plugin
			Plugin runContinuityPlugin = RunContinuityPlugin.builder()//
					.setRunContinuityPluginData(runContinuityPluginData)//
					.build();

			Plugin stochasticsPlugin = StochasticsPlugin.getStochasticsPlugin(stochasticsPluginData);

			TestOutputConsumer outputConsumer = new TestOutputConsumer();

			// execute the simulation so that it produces a people plugin data
			Simulation simulation = Simulation.builder()//
					.addPlugin(runContinuityPlugin)//
					.addPlugin(stochasticsPlugin)//
					.setSimulationHaltTime(haltTime)//
					.setRecordState(true)//
					.setOutputConsumer(outputConsumer)//
					.setSimulationState(simulationState)//
					.build();//
			simulation.execute();

			// retrieve the people plugin data
			stochasticsPluginData = outputConsumer.getOutputItem(StochasticsPluginData.class).get();

			// retrieve the simulation state
			simulationState = outputConsumer.getOutputItem(SimulationState.class).get();

			// retrieve the run continuity plugin data
			runContinuityPluginData = outputConsumer.getOutputItem(RunContinuityPluginData.class).get();

			Optional<String> optional = outputConsumer.getOutputItem(String.class);
			if (optional.isPresent()) {
				result = optional.get();
			}
		}
		assertNotNull(result);

		return result;

	}

	/**
	 * Demonstrates that the data manager's initial state reflects its plugin data
	 */
	@Test
	@UnitTestMethod(target = StochasticsDataManager.class, name = "init", args = { DataManagerContext.class })
	public void testStateInitialization() {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(2051947783068799322L);

		// Generate a few random well states
		WellState wellState_MAIN = getRandomWellState(randomGenerator);
		WellState wellState_BLITZEN = getRandomWellState(randomGenerator);
		WellState wellState_CUPID = getRandomWellState(randomGenerator);
		WellState wellState_DANCER = getRandomWellState(randomGenerator);

		// create the initial plugin data with only BLITZEN
		StochasticsPluginData stochasticsPluginData = StochasticsPluginData.builder()//
				.setMainRNGState(wellState_MAIN)//
				.addRNG(TestRandomGeneratorId.BLITZEN, wellState_BLITZEN)//
				.addRNG(TestRandomGeneratorId.CUPID, wellState_CUPID)//
				.addRNG(TestRandomGeneratorId.DANCER, wellState_DANCER)//
				.build();

		TestPluginData.Builder pluginBuilder = TestPluginData.builder();

		// Have an actor add CUPID and use the various random generators
		pluginBuilder.addTestActorPlan("actor", new TestActorPlan(0, (c) -> {
			StochasticsDataManager stochasticsDataManager = c.getDataManager(StochasticsDataManager.class);

			// show that the rng ids are as expected
			Set<RandomNumberGeneratorId> expectedIds = new LinkedHashSet<>();
			expectedIds.add(TestRandomGeneratorId.BLITZEN);
			expectedIds.add(TestRandomGeneratorId.CUPID);
			expectedIds.add(TestRandomGeneratorId.DANCER);
			Set<RandomNumberGeneratorId> actualIds = stochasticsDataManager.getRandomNumberGeneratorIds();
			assertEquals(expectedIds, actualIds);

			// show that the state of the rngs are correct
			Well well = (Well) stochasticsDataManager.getRandomGenerator();
			assertEquals(wellState_MAIN, well.getWellState());

			well = (Well) stochasticsDataManager.getRandomGeneratorFromId(TestRandomGeneratorId.BLITZEN);
			assertEquals(wellState_BLITZEN, well.getWellState());

			well = (Well) stochasticsDataManager.getRandomGeneratorFromId(TestRandomGeneratorId.CUPID);
			assertEquals(wellState_CUPID, well.getWellState());

			well = (Well) stochasticsDataManager.getRandomGeneratorFromId(TestRandomGeneratorId.DANCER);
			assertEquals(wellState_DANCER, well.getWellState());

		}));

		// run the simulation
		TestPluginData testPluginData = pluginBuilder.build();
		Factory factory = StochasticsTestPluginFactory.factory(2244108072445615171L, testPluginData)//
				.setStochasticsPluginData(stochasticsPluginData);
		TestSimulation.builder().addPlugins(factory.getPlugins())//
				.setProduceSimulationStateOnHalt(true)//
				.setSimulationHaltTime(2)//
				.build()//
				.execute();

	}

	/**
	 * Demonstrates that the data manager produces plugin data that reflects its
	 * final state
	 */
	@Test
	@UnitTestMethod(target = StochasticsDataManager.class, name = "init", args = { DataManagerContext.class })
	public void testStateFinalization() {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(2051947783068799322L);

		// Generate a few random well states
		WellState wellState_MAIN = getRandomWellState(randomGenerator);
		WellState wellState_BLITZEN = getRandomWellState(randomGenerator);
		WellState wellState_CUPID = getRandomWellState(randomGenerator);
		WellState wellState_DANCER = getRandomWellState(randomGenerator);

		// create the initial plugin data with only BLITZEN
		StochasticsPluginData stochasticsPluginData = StochasticsPluginData.builder()//
				.setMainRNGState(wellState_MAIN)//
				.addRNG(TestRandomGeneratorId.BLITZEN, wellState_BLITZEN)//
				.build();

		TestPluginData.Builder pluginBuilder = TestPluginData.builder();

		// Have an actor add CUPID and use the various random generators
		pluginBuilder.addTestActorPlan("actor", new TestActorPlan(0, (c) -> {
			StochasticsDataManager stochasticsDataManager = c.getDataManager(StochasticsDataManager.class);
			stochasticsDataManager.addRandomNumberGenerator(TestRandomGeneratorId.CUPID, wellState_CUPID);
			RandomGenerator rng = stochasticsDataManager.getRandomGenerator();
			rng.nextBoolean();
			rng.nextInt();
			rng = stochasticsDataManager.getRandomGeneratorFromId(TestRandomGeneratorId.CUPID);
			rng.nextDouble();
			rng.nextDouble();
			rng.nextDouble();
		}));

		// Have an actor add DANCER and use the various random generators
		pluginBuilder.addTestActorPlan("actor", new TestActorPlan(1, (c) -> {
			StochasticsDataManager stochasticsDataManager = c.getDataManager(StochasticsDataManager.class);
			stochasticsDataManager.addRandomNumberGenerator(TestRandomGeneratorId.DANCER, wellState_DANCER);
			RandomGenerator rng = stochasticsDataManager.getRandomGenerator();
			rng.nextInt();
			rng.nextLong();
			rng = stochasticsDataManager.getRandomGeneratorFromId(TestRandomGeneratorId.DANCER);
			rng.nextBoolean();
			rng.nextBoolean();
			rng.nextFloat();
			rng = stochasticsDataManager.getRandomGeneratorFromId(TestRandomGeneratorId.CUPID);
			rng.nextLong();
			rng.nextDouble();
			rng.nextDouble();

		}));

		// run the simulation
		TestPluginData testPluginData = pluginBuilder.build();
		Factory factory = StochasticsTestPluginFactory.factory(3078336459131759089L, testPluginData)//
				.setStochasticsPluginData(stochasticsPluginData);
		TestOutputConsumer testOutputConsumer = TestSimulation.builder().addPlugins(factory.getPlugins())//
				.setProduceSimulationStateOnHalt(true)//
				.setSimulationHaltTime(2)//
				.build()//
				.execute();
		// Get the resulting StochasticsPluginData
		Map<StochasticsPluginData, Integer> outputItems = testOutputConsumer
				.getOutputItemMap(StochasticsPluginData.class);
		assertEquals(1, outputItems.size());
		StochasticsPluginData actualPluginData = outputItems.keySet().iterator().next();

		/*
		 * Build the expected StochasticsPluginData by repeating the actions of the
		 * actors for each well
		 */
		StochasticsPluginData.Builder builder = StochasticsPluginData.builder();//
		Well well = new Well(wellState_MAIN);
		well.nextBoolean();
		well.nextInt();
		well.nextInt();
		well.nextLong();
		builder.setMainRNGState(well.getWellState());

		well = new Well(wellState_BLITZEN);
		builder.addRNG(TestRandomGeneratorId.BLITZEN, well.getWellState());

		well = new Well(wellState_CUPID);
		well.nextDouble();
		well.nextDouble();
		well.nextDouble();
		well.nextLong();
		well.nextDouble();
		well.nextDouble();
		builder.addRNG(TestRandomGeneratorId.CUPID, well.getWellState());

		well = new Well(wellState_DANCER);
		well.nextBoolean();
		well.nextBoolean();
		well.nextFloat();
		builder.addRNG(TestRandomGeneratorId.DANCER, well.getWellState());

		StochasticsPluginData expectedPluginData = builder.build();

		// show the resulting plugin data are equal
		assertEquals(expectedPluginData, actualPluginData);
	}

	@Test
	@UnitTestMethod(target = StochasticsDataManager.class, name = "getRandomNumberGeneratorIds", args = {})
	public void testGetRandomNumberGeneratorIds() {

		Factory factory = StochasticsTestPluginFactory.factory(1244273915891145733L, (c) -> {

			StochasticsDataManager stochasticsDataManager = c.getDataManager(StochasticsDataManager.class);

			Set<RandomNumberGeneratorId> actualRandomNumberGeneratorIds = stochasticsDataManager
					.getRandomNumberGeneratorIds();

			Set<TestRandomGeneratorId> expectedRandomGeneratorIds = new LinkedHashSet<>();
			for (TestRandomGeneratorId testRandomGeneratorId : TestRandomGeneratorId.values()) {
				expectedRandomGeneratorIds.add(testRandomGeneratorId);
			}
			assertEquals(expectedRandomGeneratorIds, actualRandomNumberGeneratorIds);
		});
		TestSimulation.builder().addPlugins(factory.getPlugins()).build().execute();
	}

	@Test
	@UnitTestMethod(target = StochasticsDataManager.class, name = "randomNumberGeneratorIdExists", args = {
			RandomNumberGeneratorId.class })
	public void testRandomNumberGeneratorIdExists() {
		Factory factory = StochasticsTestPluginFactory.factory(1244273915891145733L, (c) -> {
			StochasticsDataManager stochasticsDataManager = c.getDataManager(StochasticsDataManager.class);
			RandomNumberGeneratorId unknownRandomGeneratorId = TestRandomGeneratorId
					.getUnknownRandomNumberGeneratorId();
			Set<RandomNumberGeneratorId> randomNumberGeneratorIds = stochasticsDataManager
					.getRandomNumberGeneratorIds();
			for (TestRandomGeneratorId testRandomGeneratorId : TestRandomGeneratorId.values()) {
				assertTrue(randomNumberGeneratorIds.contains(testRandomGeneratorId));
			}
			assertFalse(randomNumberGeneratorIds.contains(unknownRandomGeneratorId));
		});
		TestSimulation.builder().addPlugins(factory.getPlugins()).build().execute();
	}

	@Test
	@UnitTestMethod(target = StochasticsDataManager.class, name = "getRandomGeneratorFromId", args = {
			RandomNumberGeneratorId.class })
	public void testGetRandomGeneratorFromId() {

		// show that random generators can be retrieved by ids.
		Factory factory = StochasticsTestPluginFactory.factory(5489824520767978373L, (c) -> {
			StochasticsDataManager stochasticsDataManager = c.getDataManager(StochasticsDataManager.class);
			for (TestRandomGeneratorId testRandomGeneratorId : TestRandomGeneratorId.values()) {
				RandomGenerator randomGeneratorFromId = stochasticsDataManager
						.getRandomGeneratorFromId(testRandomGeneratorId);
				assertNotNull(randomGeneratorFromId);
			}
		});

		TestSimulation.builder().addPlugins(factory.getPlugins()).build().execute();

		// precondition test : if the random number generator is null
		ContractException contractException = assertThrows(ContractException.class, () -> {
			Factory factory2 = StochasticsTestPluginFactory.factory(1893848105389404535L, (c) -> {
				StochasticsDataManager stochasticsDataManager = c.getDataManager(StochasticsDataManager.class);
				stochasticsDataManager.getRandomGeneratorFromId(null);
			});
			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();
		});
		assertEquals(StochasticsError.NULL_RANDOM_NUMBER_GENERATOR_ID, contractException.getErrorType());

		// precondition test : if the random number generator is unknown
		ContractException contractException2 = assertThrows(ContractException.class, () -> {
			Factory factory3 = StochasticsTestPluginFactory.factory(6057300273321098424L, (c) -> {
				StochasticsDataManager stochasticsDataManager = c.getDataManager(StochasticsDataManager.class);
				stochasticsDataManager
						.getRandomGeneratorFromId(TestRandomGeneratorId.getUnknownRandomNumberGeneratorId());
			});
			TestSimulation.builder().addPlugins(factory3.getPlugins()).build().execute();
		});
		assertEquals(StochasticsError.UNKNOWN_RANDOM_NUMBER_GENERATOR_ID, contractException2.getErrorType());
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
	@UnitTestMethod(target = StochasticsDataManager.class, name = "addRandomNumberGenerator", args = {
			RandomNumberGeneratorId.class, WellState.class })
	public void testAddRandomNumberGenerator() {
		Factory factory = StochasticsTestPluginFactory.factory(1244273915891145733L, (c) -> {

			StochasticsDataManager stochasticsDataManager = c.getDataManager(StochasticsDataManager.class);
			RandomNumberGeneratorId numberGeneratorIdToAdd = TestRandomGeneratorId.getUnknownRandomNumberGeneratorId();
			WellState wellState = WellState.builder().build();
			stochasticsDataManager.addRandomNumberGenerator(numberGeneratorIdToAdd, wellState);

			Set<RandomNumberGeneratorId> actualRandomNumberGeneratorIds = stochasticsDataManager
					.getRandomNumberGeneratorIds();

			Set<RandomNumberGeneratorId> expectedRandomGeneratorIds = new LinkedHashSet<>();
			for (RandomNumberGeneratorId testRandomGeneratorId : TestRandomGeneratorId.values()) {
				expectedRandomGeneratorIds.add(testRandomGeneratorId);
			}
			expectedRandomGeneratorIds.add(numberGeneratorIdToAdd);
			assertEquals(expectedRandomGeneratorIds, actualRandomNumberGeneratorIds);
		});
		TestSimulation.builder().addPlugins(factory.getPlugins()).build().execute();
	}

	@Test
	@UnitTestConstructor(target = StochasticsDataManager.class, args = { StochasticsPluginData.class })
	public void testConstructor() {
		// test of constructor is covered by the method tests
	}

	private WellState getRandomWellState(RandomGenerator randomGenerator) {
		int stateIndex = randomGenerator.nextInt(1390);
		int[] vArray = new int[1391];

		for (int i = 0; i < 1391; i++) {
			vArray[i] = randomGenerator.nextInt();
		}
		WellState wellState = WellState.builder()//
				.setInternals(stateIndex, vArray)//
				.setSeed(randomGenerator.nextLong())//
				.build();
		return wellState;
	}

	@Test
	@UnitTestMethod(target = StochasticsDataManager.class, name = "toString", args = {})
	public void testToString() {
		Factory factory = StochasticsTestPluginFactory.factory(4394401734465184701L, (c) -> {
			StochasticsDataManager stochasticsDataManager = c.getDataManager(StochasticsDataManager.class);
			String actualValue = stochasticsDataManager.toString();

			RandomGenerator mainRandomGenerator = stochasticsDataManager.getRandomGenerator();
			Map<RandomNumberGeneratorId, Well> randomGeneratorMap = new LinkedHashMap<>();
			for (RandomNumberGeneratorId randomNumberGeneratorId : stochasticsDataManager
					.getRandomNumberGeneratorIds()) {
				Well randomGeneratorFromId = stochasticsDataManager.getRandomGeneratorFromId(randomNumberGeneratorId);
				randomGeneratorMap.put(randomNumberGeneratorId, randomGeneratorFromId);
			}

			StringBuilder builder = new StringBuilder();
			builder.append("StochasticsDataManager [randomGeneratorMap=");
			builder.append(randomGeneratorMap);
			builder.append(", randomGenerator=");
			builder.append(mainRandomGenerator);
			builder.append("]");
			String expectedValue = builder.toString();

			assertEquals(expectedValue, actualValue);

		});
		TestSimulation.builder().addPlugins(factory.getPlugins()).build().execute();
	}

}
