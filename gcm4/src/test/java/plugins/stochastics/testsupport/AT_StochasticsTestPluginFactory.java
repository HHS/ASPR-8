package plugins.stochastics.testsupport;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.function.Consumer;

import org.apache.commons.math3.random.RandomGenerator;
import org.junit.jupiter.api.Test;

import nucleus.ActorContext;
import nucleus.NucleusError;
import nucleus.Plugin;
import nucleus.PluginData;
import nucleus.PluginId;
import nucleus.testsupport.testplugin.TestActorPlan;
import nucleus.testsupport.testplugin.TestPluginData;
import nucleus.testsupport.testplugin.TestPluginId;
import nucleus.testsupport.testplugin.TestSimulation;
import plugins.stochastics.StochasticsPluginId;
import plugins.stochastics.datamanagers.StochasticsPluginData;
import plugins.stochastics.support.StochasticsError;
import plugins.stochastics.support.WellState;
import plugins.stochastics.testsupport.StochasticsTestPluginFactory.Factory;
import util.annotations.UnitTestMethod;
import util.errors.ContractException;
import util.random.RandomGeneratorProvider;
import util.wrappers.MutableBoolean;

public class AT_StochasticsTestPluginFactory {

    @Test
    @UnitTestMethod(target = StochasticsTestPluginFactory.class, name = "factory", args = { long.class,
            Consumer.class })
    public void testFactory_Consumer() {
        MutableBoolean executed = new MutableBoolean();

        Factory factory = StochasticsTestPluginFactory.factory(576570479777898470L, c -> executed.setValue(true));
        TestSimulation.builder().addPlugins(factory.getPlugins()).build().execute();
        assertTrue(executed.getValue());

        // precondition: consumer is null
        Consumer<ActorContext> nullConsumer = null;
        ContractException contractException = assertThrows(ContractException.class,
                () -> StochasticsTestPluginFactory.factory(0, nullConsumer));
        assertEquals(NucleusError.NULL_ACTOR_CONTEXT_CONSUMER, contractException.getErrorType());
    }

    @Test
    @UnitTestMethod(target = StochasticsTestPluginFactory.class, name = "factory", args = { long.class,
            TestPluginData.class })
    public void testFactory_TestPluginData() {
        MutableBoolean executed = new MutableBoolean();

        TestPluginData.Builder builder = TestPluginData.builder();
        builder.addTestActorPlan("actor", new TestActorPlan(0, c -> executed.setValue(true)));
        TestPluginData testPluginData = builder.build();

        Factory factory = StochasticsTestPluginFactory.factory(45235233432345378L, testPluginData);
        TestSimulation.builder().addPlugins(factory.getPlugins()).build().execute();
        assertTrue(executed.getValue());

        // precondition: testPluginData is null
        TestPluginData nullTestPluginData = null;
        ContractException contractException = assertThrows(ContractException.class,
                () -> StochasticsTestPluginFactory.factory(0, nullTestPluginData));
        assertEquals(NucleusError.NULL_PLUGIN_DATA, contractException.getErrorType());
    }

    /*
     * Given a list of plugins, will show that the plugin with the given pluginId
     * exists, and exists EXACTLY once.
     */
    private Plugin checkPluginExists(List<Plugin> plugins, PluginId pluginId) {
        Plugin actualPlugin = null;
        for (Plugin plugin : plugins) {
            if (plugin.getPluginId().equals(pluginId)) {
                assertNull(actualPlugin);
                actualPlugin = plugin;
            }
        }

        assertNotNull(actualPlugin);

        return actualPlugin;
    }

    /**
     * Given a list of plugins, will show that the explicit plugindata for the given
     * pluginid exists, and exists EXACTLY once.
     */
    private <T extends PluginData> void checkPluginDataExists(List<Plugin> plugins, T expectedPluginData,
            PluginId pluginId) {
        Plugin actualPlugin =TestFactoryUtil.checkPluginExists(plugins, pluginId);
        List<PluginData> actualPluginDatas = actualPlugin.getPluginDatas();
        assertNotNull(actualPluginDatas);
        assertEquals(1, actualPluginDatas.size());
        PluginData actualPluginData = actualPluginDatas.get(0);
        assertTrue(expectedPluginData == actualPluginData);
    }

    @Test
    @UnitTestMethod(target = StochasticsTestPluginFactory.Factory.class, name = "getPlugins", args = {})
    public void testGetPlugins() {

        List<Plugin> plugins = StochasticsTestPluginFactory.factory(3626443405517810332L, t -> {
        }).getPlugins();
        assertEquals(2, plugins.size());

       TestFactoryUtil.checkPluginExists(plugins, StochasticsPluginId.PLUGIN_ID);
       TestFactoryUtil.checkPluginExists(plugins, TestPluginId.PLUGIN_ID);
    }

    @Test
    @UnitTestMethod(target = StochasticsTestPluginFactory.Factory.class, name = "setStochasticsPluginData", args = {
            StochasticsPluginData.class })
    public void testSetStochasticsPluginData() {
        StochasticsPluginData.Builder builder = StochasticsPluginData.builder();

        WellState wellState = WellState.builder().setSeed(2990359774692004249L).build();
        builder.setMainRNGState(wellState);

        StochasticsPluginData stochasticsPluginData = builder.build();

        List<Plugin> plugins = StochasticsTestPluginFactory.factory(5433603767451466687L, t -> {
        }).setStochasticsPluginData(stochasticsPluginData).getPlugins();

       TestFactoryUtil.checkPluginDataExists(plugins, stochasticsPluginData, StochasticsPluginId.PLUGIN_ID);

        // precondition: stochasticsPluginData is not null
        ContractException contractException = assertThrows(ContractException.class,
                () -> StochasticsTestPluginFactory.factory(5433603767451466687L, t -> {
                }).setStochasticsPluginData(null));
        assertEquals(StochasticsError.NULL_STOCHASTICS_PLUGIN_DATA, contractException.getErrorType());
    }

    @Test
    @UnitTestMethod(target = StochasticsTestPluginFactory.class, name = "getStandardStochasticsPluginData", args = {
            long.class })
    public void testGetStandardStochasticsPluginData() {
        long seed = 6072871729256538807L;

        StochasticsPluginData.Builder builder = StochasticsPluginData.builder();
        RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(seed);
        WellState wellState = WellState.builder().setSeed(randomGenerator.nextLong()).build();
        builder.setMainRNGState(wellState);
        for (TestRandomGeneratorId testRandomGeneratorId : TestRandomGeneratorId.values()) {
            wellState = WellState.builder().setSeed(randomGenerator.nextLong()).build();
            builder.addRNG(testRandomGeneratorId, wellState);
        }

        StochasticsPluginData expectedPluginData = builder.build();
        StochasticsPluginData actualPluginData = StochasticsTestPluginFactory
                .getStandardStochasticsPluginData(seed);

        assertEquals(expectedPluginData, actualPluginData);
    }
}
