package gov.hhs.aspr.ms.gcm.nucleus.testsupport.testplugin;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.apache.commons.math3.random.RandomGenerator;
import org.junit.jupiter.api.Test;

import gov.hhs.aspr.ms.gcm.nucleus.Plugin;
import gov.hhs.aspr.ms.gcm.nucleus.SimulationState;
import gov.hhs.aspr.ms.gcm.nucleus.testsupport.runcontinuityplugin.RunContinuityPlugin;
import gov.hhs.aspr.ms.gcm.nucleus.testsupport.runcontinuityplugin.RunContinuityPluginData;
import gov.hhs.aspr.ms.util.annotations.UnitTestMethod;
import gov.hhs.aspr.ms.util.random.RandomGeneratorProvider;
import gov.hhs.aspr.ms.util.wrappers.MutableBoolean;

public class AT_TestSimulation {

	@Test
	@UnitTestMethod(target = TestSimulation.Builder.class, name = "build", args = {})
	public void testBuild() {
		assertNotNull(TestSimulation.builder().build());
	}

	@Test
	@UnitTestMethod(target = TestSimulation.Builder.class, name = "setProduceSimulationStateOnHalt", args = {
			boolean.class })
	public void testProduceSimulationStateOnHalt() {

		for (int i = 0; i < 10; i++) {
			TestPluginData testPluginData = TestPluginData.builder()
					.addTestActorPlan("testActor", new TestActorPlan(i, (c) -> {
					})).build();

			RunContinuityPluginData runContinuityPluginData = RunContinuityPluginData.builder()
					.addContextConsumer(i, (c) -> {
					}).build();

			Plugin testPlugin = TestPlugin.getTestPlugin(testPluginData);
			Plugin plugin = RunContinuityPlugin.builder().setRunContinuityPluginData(runContinuityPluginData).build();

			double haltTime = 10;
			TestSimulation testSimulation = TestSimulation.builder().setProduceSimulationStateOnHalt(true)
					.setSimulationHaltTime(haltTime).addPlugin(testPlugin).addPlugin(plugin).build();

			TestOutputConsumer testOutputConsumer = testSimulation.execute();

			Optional<SimulationState> simState = testOutputConsumer.getOutputItem(SimulationState.class);
			assertTrue(simState.isPresent());

			List<RunContinuityPluginData> pluginDatas = testOutputConsumer
					.getOutputItems(RunContinuityPluginData.class);

			assertFalse(pluginDatas.isEmpty());
			assertTrue(pluginDatas.size() == 1);

			RunContinuityPluginData pluginData = pluginDatas.get(0);
			assertTrue(pluginData.allPlansComplete());			
		}

	}

	@Test
	@UnitTestMethod(target = TestSimulation.Builder.class, name = "setSimulationHaltTime", args = { double.class })
	public void testSetSimulationHaltTime() {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(2020906151186894101L);

		for (int i = 0; i < 10; i++) {
			double haltTime = randomGenerator.nextInt(100) + 1;

			TestPluginData testPluginData = TestPluginData.builder()
					.addTestActorPlan("testActor", new TestActorPlan(haltTime - 1, (c) -> {
					})).build();

			Plugin testPlugin = TestPlugin.getTestPlugin(testPluginData);

			TestSimulation testSimulation = TestSimulation.builder().setProduceSimulationStateOnHalt(true)
					.setSimulationHaltTime(haltTime).addPlugin(testPlugin).build();

			TestOutputConsumer testOutputConsumer = testSimulation.execute();

			Optional<SimulationState> osimulationState = testOutputConsumer.getOutputItem(SimulationState.class);

			assertTrue(osimulationState.isPresent());

			SimulationState simState = osimulationState.get();

			assertEquals(haltTime, simState.getStartTime());
		}

	}

	@Test
	@UnitTestMethod(target = TestSimulation.Builder.class, name = "setSimulationState", args = {
			SimulationState.class })
	public void testSetSimulationState() {

		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(3070829833293509127L);

		for (int i = 0; i < 10; i++) {
			double startTime = randomGenerator.nextInt(10) + 1;
			double planTime = startTime + 2;
			MutableBoolean called = new MutableBoolean(false);

			RunContinuityPluginData runContinuityPluginData = RunContinuityPluginData.builder()
					.addContextConsumer(planTime, (c) -> called.setValue(true))//
					.build();

			Plugin actorPlugin = RunContinuityPlugin.builder().setRunContinuityPluginData(runContinuityPluginData)
					.build();

			TestPluginData testPluginData = TestPluginData.builder()
					.addTestActorPlan("test actor 2", new TestActorPlan(planTime++, (context) -> {

					})).build();

			SimulationState simulationState = SimulationState.builder()
					.setStartTime(startTime).build();

			TestSimulation testSimulation = TestSimulation.builder().addPlugin(actorPlugin)
					.addPlugin(TestPlugin.getTestPlugin(testPluginData)).setSimulationState(simulationState).build();

			TestOutputConsumer testOutputConsumer = testSimulation.execute();

			assertTrue(called.getValue());

			List<RunContinuityPluginData> pluginDatas = testOutputConsumer
					.getOutputItems(RunContinuityPluginData.class);

			assertTrue(pluginDatas.size() == 1);

			RunContinuityPluginData pluginData = pluginDatas.get(0);

			
			assertTrue(pluginData.allPlansComplete());
		}

	}

	@Test
	@UnitTestMethod(target = TestSimulation.Builder.class, name = "addPlugin", args = { Plugin.class })
	public void testAddPlugin() {

		/*
		 * We will create two plugins and show that they are exeucted by the simulation,
		 * and thus the plugin addition is working properly
		 */
		RunContinuityPluginData runContinuityPluginData = RunContinuityPluginData.builder()
				.addContextConsumer(0, (c) -> {
				})//
				.build();

		Plugin runContinuityPlugin = RunContinuityPlugin.builder()//
				.setRunContinuityPluginData(runContinuityPluginData).build();

		TestPluginData testPluginData = TestPluginData.builder()
				.addTestActorPlan("test actor 2", new TestActorPlan(1, (context) -> {
				})).build();

		Plugin testActorPlugin = TestPlugin.getTestPlugin(testPluginData);

		TestSimulation testSimulation = TestSimulation.builder()//
				.addPlugin(runContinuityPlugin)//
				.addPlugin(testActorPlugin)//
				.build();

		TestOutputConsumer testOutputConsumer = testSimulation.execute();

		/*
		 * we show that the TestPlugin was added properly and executed
		 */
		
		Optional<TestScenarioReport> optionalTestScenarioReport = testOutputConsumer
				.getOutputItem(TestScenarioReport.class);
		assertTrue(optionalTestScenarioReport.isPresent());
		TestScenarioReport testScenarioReport = optionalTestScenarioReport.get();
		assertTrue(testScenarioReport.isComplete());

		// we show that the RunContinuityPlugin was added properly and executed
		Optional<RunContinuityPluginData> optionalRunContinuityPluginData = testOutputConsumer
				.getOutputItem(RunContinuityPluginData.class);
		assertTrue(optionalRunContinuityPluginData.isPresent());
		runContinuityPluginData = optionalRunContinuityPluginData.get();		
		assertTrue(runContinuityPluginData.allPlansComplete());
	}

	@Test
	@UnitTestMethod(target = TestSimulation.Builder.class, name = "addPlugins", args = { Collection.class })
	public void testAddPlugins() {
		/*
		 * We will create two plugins and show that they are exeucted by the simulation,
		 * and thus the plugin addition is working properly
		 */
		RunContinuityPluginData runContinuityPluginData = RunContinuityPluginData.builder()
				.addContextConsumer(0, (c) -> {
				})//
				.build();

		Plugin runContinuityPlugin = RunContinuityPlugin.builder()//
				.setRunContinuityPluginData(runContinuityPluginData).build();

		TestPluginData testPluginData = TestPluginData.builder()
				.addTestActorPlan("test actor 2", new TestActorPlan(1, (context) -> {
				})).build();

		Plugin testActorPlugin = TestPlugin.getTestPlugin(testPluginData);

		List<Plugin> plugins = new ArrayList<>();

		plugins.add(runContinuityPlugin);
		plugins.add(testActorPlugin);

		TestSimulation testSimulation = TestSimulation.builder()//
				.addPlugins(plugins)//
				.build();

		TestOutputConsumer testOutputConsumer = testSimulation.execute();

		/*
		 * we show that the TestPlugin was added properly and executed
		 */
		
		Optional<TestScenarioReport> optionalTestScenarioReport = testOutputConsumer
				.getOutputItem(TestScenarioReport.class);
		assertTrue(optionalTestScenarioReport.isPresent());
		TestScenarioReport testScenarioReport = optionalTestScenarioReport.get();
		assertTrue(testScenarioReport.isComplete());

		// we show that the RunContinuityPlugin was added properly and executed
		Optional<RunContinuityPluginData> optionalRunContinuityPluginData = testOutputConsumer
				.getOutputItem(RunContinuityPluginData.class);
		assertTrue(optionalRunContinuityPluginData.isPresent());
		runContinuityPluginData = optionalRunContinuityPluginData.get();		
		assertTrue(runContinuityPluginData.allPlansComplete());
	}

	@Test
	@UnitTestMethod(target = TestSimulation.class, name = "builder", args = {})
	public void testBuilder() {
		assertNotNull(TestSimulation.builder());
	}

	@Test
	@UnitTestMethod(target = TestSimulation.class, name = "execute", args = {})
	public void testExecute() {
		/*
		 * We will create two plugins and show that they are exeucted by the simulation,
		 * and thus the plugin addition is working properly
		 */
		RunContinuityPluginData runContinuityPluginData = RunContinuityPluginData.builder()
				.addContextConsumer(0, (c) -> {
				})//
				.build();

		Plugin runContinuityPlugin = RunContinuityPlugin.builder()//
				.setRunContinuityPluginData(runContinuityPluginData).build();

		TestPluginData testPluginData = TestPluginData.builder()
				.addTestActorPlan("test actor 2", new TestActorPlan(1, (context) -> {
				})).build();

		Plugin testActorPlugin = TestPlugin.getTestPlugin(testPluginData);

		List<Plugin> plugins = new ArrayList<>();

		plugins.add(runContinuityPlugin);
		plugins.add(testActorPlugin);

		TestSimulation testSimulation = TestSimulation.builder()//
				.addPlugins(plugins)//
				.build();

		TestOutputConsumer testOutputConsumer = testSimulation.execute();

		/*
		 * we show that the TestPlugin was added properly and executed
		 */
		
		Optional<TestScenarioReport> optionalTestScenarioReport = testOutputConsumer
				.getOutputItem(TestScenarioReport.class);
		assertTrue(optionalTestScenarioReport.isPresent());
		TestScenarioReport testScenarioReport = optionalTestScenarioReport.get();
		assertTrue(testScenarioReport.isComplete());

		// we show that the RunContinuityPlugin was added properly and executed
		Optional<RunContinuityPluginData> optionalRunContinuityPluginData = testOutputConsumer
				.getOutputItem(RunContinuityPluginData.class);
		assertTrue(optionalRunContinuityPluginData.isPresent());
		runContinuityPluginData = optionalRunContinuityPluginData.get();		
		assertTrue(runContinuityPluginData.allPlansComplete());
	}
}
