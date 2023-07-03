package nucleus.testsupport.testplugin;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Optional;

import org.apache.commons.math3.random.RandomGenerator;
import org.junit.jupiter.api.Test;

import nucleus.PlanQueueData;
import nucleus.Planner;
import nucleus.Plugin;
import nucleus.SimulationState;
import nucleus.testsupport.runcontinuityplugin.RunContinuityPlanData;
import nucleus.testsupport.runcontinuityplugin.RunContinuityPlugin;
import nucleus.testsupport.runcontinuityplugin.RunContinuityPluginData;
import util.random.RandomGeneratorProvider;
import util.wrappers.MutableBoolean;

public class AT_TestSimulation {

    @Test
    public void testBuild() {
        assertNotNull(TestSimulation.builder().build());
    }

    @Test
    public void testProduceSimulationStateOnHalt() {

        for (int i = 0; i < 10; i++) {
            TestPluginData testPluginData = TestPluginData.builder()
                    .addTestActorPlan("testActor", new TestActorPlan(i, (c) -> {
                    }))
                    .build();

            RunContinuityPluginData runContinuityPluginData = RunContinuityPluginData.builder()
                    .addContextConsumer(i, (c) -> {
                    })
                    .build();

            Plugin testPlugin = TestPlugin.getTestPlugin(testPluginData);
            Plugin plugin = RunContinuityPlugin.builder()
                    .setRunContinuityPluginData(runContinuityPluginData)
                    .build();

            double haltTime = 10;
            TestSimulation testSimulation = TestSimulation.builder()
                    .setProduceSimulationStateOnHalt(true)
                    .setSimulationHaltTime(haltTime)
                    .addPlugin(testPlugin)
                    .addPlugin(plugin)
                    .build();

            TestOutputConsumer testOutputConsumer = testSimulation.execute();

            Optional<SimulationState> simState = testOutputConsumer.getOutputItem(SimulationState.class);
            assertTrue(simState.isPresent());

            List<RunContinuityPluginData> pluginDatas = testOutputConsumer
                    .getOutputItems(RunContinuityPluginData.class);

            assertFalse(pluginDatas.isEmpty());
            assertTrue(pluginDatas.size() == 1);

            RunContinuityPluginData pluginData = pluginDatas.get(0);
            assertTrue(pluginData.allPlansComplete());
            assertEquals(1, pluginData.getCompletionCount());
        }

    }

    @Test
    public void testSetSimulationHaltTime() {
        RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(2020906151186894101L);

        for (int i = 0; i < 10; i++) {
            double haltTime = randomGenerator.nextInt(100) + 1;

            TestPluginData testPluginData = TestPluginData.builder()
                    .addTestActorPlan("testActor", new TestActorPlan(haltTime - 1, (c) -> {
                    }))
                    .build();

            Plugin testPlugin = TestPlugin.getTestPlugin(testPluginData);

            TestSimulation testSimulation = TestSimulation.builder()
                    .setProduceSimulationStateOnHalt(true)
                    .setSimulationHaltTime(haltTime)
                    .addPlugin(testPlugin)
                    .build();

            TestOutputConsumer testOutputConsumer = testSimulation.execute();

            Optional<SimulationState> osimulationState = testOutputConsumer.getOutputItem(SimulationState.class);

            assertTrue(osimulationState.isPresent());

            SimulationState simState = osimulationState.get();

            assertEquals(haltTime, simState.getStartTime());
        }

    }

    @Test
    public void testSetSimulationState() {

        RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(3070829833293509127L);

        for (int i = 0; i < 10; i++) {
            double startTime = randomGenerator.nextInt(10) + 1;
            double planTime = startTime + 2;
            MutableBoolean called = new MutableBoolean(false);

            RunContinuityPlanData runContinuityPlanData = new RunContinuityPlanData(0);
            RunContinuityPluginData runContinuityPluginData = RunContinuityPluginData.builder()
                    .addContextConsumer(planTime, (c) -> called.setValue(true))
                    .setPlansAreScheduled(true)
                    .build();

            Plugin actorPlugin = RunContinuityPlugin.builder().setRunContinuityPluginData(runContinuityPluginData)
                    .build();

            TestPluginData testPluginData = TestPluginData.builder()
                    .addTestActorPlan("test actor 2", new TestActorPlan(planTime++, (context) -> {

                    }))
                    .build();

            PlanQueueData planQueueData = PlanQueueData.builder()
                    .setPlanData(runContinuityPlanData)
                    .setTime(planTime)
                    .setPlanner(Planner.ACTOR)
                    .build();

            SimulationState simulationState = SimulationState.builder()
                    .addPlanQueueData(planQueueData)
                    .setStartTime(startTime)
                    .setPlanningQueueArrivalId(2)
                    .build();

            TestSimulation testSimulation = TestSimulation.builder()
                    .addPlugin(actorPlugin)
                    .addPlugin(TestPlugin.getTestPlugin(testPluginData))
                    .setSimulationState(simulationState)
                    .build();

            TestOutputConsumer testOutputConsumer = testSimulation.execute();

            assertTrue(called.getValue());

            List<RunContinuityPluginData> pluginDatas = testOutputConsumer
                    .getOutputItems(RunContinuityPluginData.class);

            assertTrue(pluginDatas.size() == 1);

            RunContinuityPluginData pluginData = pluginDatas.get(0);

            assertEquals(1, pluginData.getCompletionCount());
            assertTrue(pluginData.allPlansComplete());
        }

    }

    @Test
    public void testAddPlugin() {

    }

    @Test
    public void testAddPlugins() {

    }

    @Test
    public void testBuilder() {

    }

    @Test
    public void testExecute() {

    }
}
