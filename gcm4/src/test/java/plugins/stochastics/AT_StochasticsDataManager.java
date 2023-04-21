package plugins.stochastics;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.*;

import nucleus.testsupport.testplugin.TestActorPlan;
import nucleus.testsupport.testplugin.TestOutputConsumer;
import nucleus.testsupport.testplugin.TestPluginData;
import org.apache.commons.math3.random.RandomGenerator;
import org.junit.jupiter.api.Test;

import nucleus.DataManagerContext;
import nucleus.testsupport.testplugin.TestSimulation;
import plugins.stochastics.support.RandomNumberGeneratorId;
import plugins.stochastics.support.StochasticsError;
import plugins.stochastics.support.Well;
import plugins.stochastics.support.WellState;
import plugins.stochastics.testsupport.StochasticsTestPluginFactory;
import plugins.stochastics.testsupport.StochasticsTestPluginFactory.Factory;
import plugins.stochastics.testsupport.TestRandomGeneratorId;
import util.annotations.UnitTestConstructor;
import util.annotations.UnitTestMethod;
import util.errors.ContractException;
import util.random.RandomGeneratorProvider;

public class AT_StochasticsDataManager {

	@Test
	@UnitTestMethod(target = StochasticsDataManager.class, name = "init", args = { DataManagerContext.class })
	public void testInit() {
		// nothing to test
	}

	@Test
	@UnitTestMethod(target = StochasticsDataManager.class, name = "init", args = {DataManagerContext.class})
	public void testInit_State() {
		WellState wellState = WellState.builder().build();
		StochasticsPluginData stochasticsPluginData = StochasticsPluginData.builder()
				.setMainRNGState(wellState)
				.addRNG(TestRandomGeneratorId.BLITZEN, wellState)
				.build();
		List<RandomNumberGeneratorId> expectedRandomGeneratorIds = new ArrayList<>();
		List<WellState> expectedWellStates = new ArrayList<>();
		TestPluginData.Builder pluginBuilder = TestPluginData.builder();

		pluginBuilder.addTestActorPlan("actor", new TestActorPlan(0, (c) -> {
			StochasticsDataManager stochasticsDataManager = c.getDataManager(StochasticsDataManager.class);
//			WellState wellState = WellState.builder().build();
//			stochasticsDataManager.addRandomNumberGenerator(TestRandomGeneratorId.BLITZEN, wellState);
//			expectedRandomGeneratorIds.add(TestRandomGeneratorId.BLITZEN);
//			expectedWellStates.add(wellState);
		}));

		TestPluginData testPluginData = pluginBuilder.build();
		Factory factory = StochasticsTestPluginFactory.factory(3078336459131759089L, testPluginData)
				.setStochasticsPluginData(stochasticsPluginData);
		TestOutputConsumer testOutputConsumer = TestSimulation.builder().addPlugins(factory.getPlugins())
				.setProduceSimulationStateOnHalt(true)
				.setSimulationHaltTime(2)
				.build()
				.execute();
		Map<StochasticsPluginData, Integer> outputItems = testOutputConsumer.getOutputItems(StochasticsPluginData.class);
		assertEquals(1, outputItems.size());
		StochasticsPluginData actualPluginData = outputItems.keySet().iterator().next();
		StochasticsPluginData expectedPluginData = StochasticsPluginData.builder()
				.setMainRNGState(wellState)
				.addRNG(TestRandomGeneratorId.BLITZEN, wellState)
				.build();
		assertEquals(expectedPluginData, actualPluginData);

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
