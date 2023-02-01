package nucleus.testsupport.testplugin;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

import org.junit.jupiter.api.Test;

import nucleus.ActorContext;
import nucleus.Plugin;
import tools.annotations.UnitTestMethod;
import util.wrappers.MutableBoolean;

public class AT_TestSimulation {

    private Consumer<ActorContext> simulationConsumer(MutableBoolean executed) {
        return (c) -> {
            executed.setValue(true);
        };
    }

    @Test
    @UnitTestMethod(target = TestSimulation.class, name = "executeSimulation", args = { List.class })
    public void testExecuteSimulation1() {
        MutableBoolean executed = new MutableBoolean();
        TestPluginData testPluginData = TestPluginData.builder()
                .addTestActorPlan("actor", new TestActorPlan(0, simulationConsumer(executed))).build();
        List<Plugin> plugins = Arrays.asList(TestPlugin.getTestPlugin(testPluginData));
        assertDoesNotThrow(() -> TestSimulation.executeSimulation(plugins));
        assertTrue(executed.getValue());
    }

    @Test
    @UnitTestMethod(target = TestSimulation.class, name = "executeSimulation", args = { List.class, TestSimulationOutputConsumer.class })
    public void testExecuteSimulation2() {
        MutableBoolean executed = new MutableBoolean();
        TestSimulationOutputConsumer outputConsumer = new TestSimulationOutputConsumer();
        TestPluginData testPluginData = TestPluginData.builder()
                .addTestActorPlan("actor", new TestActorPlan(0, simulationConsumer(executed))).build();
        List<Plugin> plugins = Arrays.asList(TestPlugin.getTestPlugin(testPluginData));
        assertDoesNotThrow(() -> TestSimulation.executeSimulation(plugins, outputConsumer));
        assertTrue(executed.getValue());
        assertTrue(outputConsumer.isComplete());
    }
}
