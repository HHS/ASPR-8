package plugins.globalproperties.testsupport;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;
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
import plugins.globalproperties.GlobalPropertiesPluginData;
import plugins.globalproperties.GlobalPropertiesPluginId;
import plugins.globalproperties.reports.GlobalPropertyReportPluginData;
import plugins.globalproperties.support.GlobalPropertiesError;
import plugins.globalproperties.support.GlobalPropertyId;
import plugins.globalproperties.support.SimpleGlobalPropertyId;
import plugins.globalproperties.testsupport.GlobalPropertiesTestPluginFactory.Factory;
import plugins.reports.support.SimpleReportLabel;
import plugins.util.properties.PropertyDefinition;
import util.annotations.UnitTestMethod;
import util.errors.ContractException;
import util.random.RandomGeneratorProvider;
import util.wrappers.MutableBoolean;

public class AT_GlobalPropertiesTestPluginFactory {

    @Test
    @UnitTestMethod(target = GlobalPropertiesTestPluginFactory.Factory.class, name = "factory", args = { long.class,
            Consumer.class })

    public void testFactory_Consumer() {
        MutableBoolean executed = new MutableBoolean();
        Factory factory = GlobalPropertiesTestPluginFactory.factory(2050026532065791481L, c -> executed.setValue(true));
        TestSimulation.builder().addPlugins(factory.getPlugins()).build().execute();

        assertTrue(executed.getValue());

        // precondition: consumer is null
        Consumer<ActorContext> nullConsumer = null;
        ContractException contractException = assertThrows(ContractException.class,
                () -> GlobalPropertiesTestPluginFactory.factory(0, nullConsumer));
        assertEquals(NucleusError.NULL_ACTOR_CONTEXT_CONSUMER, contractException.getErrorType());
    }

    @Test
    @UnitTestMethod(target = GlobalPropertiesTestPluginFactory.class, name = "factory", args = { long.class,
            TestPluginData.class })
    public void testFactory_TestPluginData() {
        MutableBoolean executed = new MutableBoolean();
        TestPluginData.Builder pluginBuilder = TestPluginData.builder();
        pluginBuilder.addTestActorPlan("actor", new TestActorPlan(0, c -> executed.setValue(true)));
        TestPluginData testPluginData = pluginBuilder.build();
        Factory factory = GlobalPropertiesTestPluginFactory.factory(2050026532065791481L, testPluginData);
        TestSimulation.builder().addPlugins(factory.getPlugins()).build().execute();
        assertTrue(executed.getValue());

        // precondition: testPluginData is null
        TestPluginData nullTestPluginData = null;
        ContractException contractException = assertThrows(ContractException.class,
                () -> GlobalPropertiesTestPluginFactory.factory(0, nullTestPluginData));
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
            PluginId pluginId, int numPluginDatas) {
        Plugin actualPlugin = checkPluginExists(plugins, pluginId);
        List<PluginData> actualPluginDatas = actualPlugin.getPluginDatas();
        assertNotNull(actualPluginDatas);
        assertEquals(numPluginDatas, actualPluginDatas.size());

        if (numPluginDatas > 1) {
            for (PluginData pluginData : actualPluginDatas) {
                if (expectedPluginData.getClass().isAssignableFrom(pluginData.getClass())) {
                    assertTrue(expectedPluginData == pluginData);
                    break;
                }
            }
        } else {
            PluginData actualPluginData = actualPluginDatas.get(0);
            assertTrue(expectedPluginData == actualPluginData);
        }

    }

    /**
     * Given a list of plugins, will show that the explicit plugindata for the given
     * pluginid exists, and exists EXACTLY once.
     */
    private <T extends PluginData> void checkPluginDataExists(List<Plugin> plugins, T expectedPluginData,
            PluginId pluginId) {
        checkPluginDataExists(plugins, expectedPluginData, pluginId, 1);
    }

    @Test
    @UnitTestMethod(target = GlobalPropertiesTestPluginFactory.Factory.class, name = "getPlugins", args = {})
    public void testGetPlugins() {

        List<Plugin> plugins = GlobalPropertiesTestPluginFactory.factory(2050026532065791481L, t -> {
        }).getPlugins();
        assertEquals(2, plugins.size());

        checkPluginExists(plugins, GlobalPropertiesPluginId.PLUGIN_ID);
        checkPluginExists(plugins, TestPluginId.PLUGIN_ID);
    }

    @Test
    @UnitTestMethod(target = GlobalPropertiesTestPluginFactory.Factory.class, name = "setGlobalPropertyReportPluginData", args = {
            GlobalPropertyReportPluginData.class })
    public void testSetGlobalPropertyReportPluginData() {
        GlobalPropertyReportPluginData.Builder builder = GlobalPropertyReportPluginData.builder();

        builder.setDefaultInclusion(false)
                .setReportLabel(new SimpleReportLabel("global property report"));

        RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(1086184935572375203L);
        for (GlobalPropertyId globalPropertyId : TestGlobalPropertyId.values()) {
            if (randomGenerator.nextBoolean()) {
                builder.includeGlobalProperty(globalPropertyId);
            } else {
                builder.excludeGlobalProperty(globalPropertyId);
            }
        }

        GlobalPropertyReportPluginData pluginData = builder.build();

        List<Plugin> plugins = GlobalPropertiesTestPluginFactory.factory(2050026532065791481L, t -> {
        }).setGlobalPropertyReportPluginData(pluginData).getPlugins();

        checkPluginDataExists(plugins, pluginData, GlobalPropertiesPluginId.PLUGIN_ID, 2);

        // precondition: globalPropReportPluginData is null
        ContractException contractException = assertThrows(ContractException.class,
                () -> GlobalPropertiesTestPluginFactory.factory(2050026532065791481L, t -> {
                }).setGlobalPropertyReportPluginData(null));
        assertEquals(GlobalPropertiesError.NULL_GLOBAL_PROPERTY_REPORT_PLUGIN_DATA, contractException.getErrorType());
    }

    @Test
    @UnitTestMethod(target = GlobalPropertiesTestPluginFactory.Factory.class, name = "setGlobalPropertiesPluginData", args = {
            GlobalPropertiesPluginData.class })
    public void testSetGlobalPropertiesPluginData() {
        GlobalPropertiesPluginData.Builder initialDatabuilder = GlobalPropertiesPluginData.builder();

        GlobalPropertyId globalPropertyId_1 = new SimpleGlobalPropertyId("id_1");
        PropertyDefinition propertyDefinition = PropertyDefinition.builder().setType(Integer.class).setDefaultValue(3)
                .build();
        initialDatabuilder.defineGlobalProperty(globalPropertyId_1, propertyDefinition, 0);

        GlobalPropertyId globalPropertyId_2 = new SimpleGlobalPropertyId("id_2");
        propertyDefinition = PropertyDefinition.builder().setType(Double.class).setDefaultValue(6.78).build();
        initialDatabuilder.defineGlobalProperty(globalPropertyId_2, propertyDefinition, 0);

        GlobalPropertyId globalPropertyId_3 = new SimpleGlobalPropertyId("id_3");
        propertyDefinition = PropertyDefinition.builder().setType(Boolean.class).setDefaultValue(true).build();
        initialDatabuilder.defineGlobalProperty(globalPropertyId_3, propertyDefinition, 0);

        GlobalPropertiesPluginData globalPropertiesPluginData = initialDatabuilder.build();

        List<Plugin> plugins = GlobalPropertiesTestPluginFactory.factory(2050026532065791481L, t -> {
        }).setGlobalPropertiesPluginData(globalPropertiesPluginData).getPlugins();

        checkPluginDataExists(plugins, globalPropertiesPluginData, GlobalPropertiesPluginId.PLUGIN_ID);

        // precondition: globalPropertiesPluginData is not null
        ContractException contractException = assertThrows(ContractException.class,
                () -> GlobalPropertiesTestPluginFactory.factory(2050026532065791481L, t -> {
                }).setGlobalPropertiesPluginData(null));
        assertEquals(GlobalPropertiesError.NULL_GLOBAL_PLUGIN_DATA, contractException.getErrorType());
    }

    @Test
    @UnitTestMethod(target = GlobalPropertiesTestPluginFactory.class, name = "getStandardGlobalPropertiesPluginData", args = {
            long.class })
    public void testGetStandardGlobalPropertiesPluginData() {
        long seed = 2376369840099946020L;

        RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(seed);

        GlobalPropertiesPluginData actualPluginData = GlobalPropertiesTestPluginFactory
                .getStandardGlobalPropertiesPluginData(seed);

        Set<TestGlobalPropertyId> expectedPropertyIds = EnumSet.allOf(TestGlobalPropertyId.class);
        assertFalse(expectedPropertyIds.isEmpty());

        Set<GlobalPropertyId> actualGlobalPropertyIds = actualPluginData.getGlobalPropertyIds();
        assertEquals(expectedPropertyIds, actualGlobalPropertyIds);

        GlobalPropertiesPluginData.Builder builder = GlobalPropertiesPluginData.builder();

        for (TestGlobalPropertyId testGlobalPropertyId : TestGlobalPropertyId.values()) {
            PropertyDefinition expectedPropertyDefinition = testGlobalPropertyId.getPropertyDefinition();
            builder.defineGlobalProperty(testGlobalPropertyId, expectedPropertyDefinition, 0);

            boolean hasDefaultValue = testGlobalPropertyId.getPropertyDefinition().getDefaultValue().isPresent();

            if (!hasDefaultValue) {
                builder.setGlobalPropertyValue(testGlobalPropertyId,
                        testGlobalPropertyId.getRandomPropertyValue(randomGenerator), 0);
            }

            // set a value to the default
            if (randomGenerator.nextBoolean() && hasDefaultValue) {
                builder.setGlobalPropertyValue(testGlobalPropertyId,
                        testGlobalPropertyId.getPropertyDefinition().getDefaultValue().get(), 0);
            }

            // set a value to not the default
            if (randomGenerator.nextBoolean() && hasDefaultValue) {
                builder.setGlobalPropertyValue(testGlobalPropertyId,
                        testGlobalPropertyId.getRandomPropertyValue(randomGenerator), 0);
            }

        }

        GlobalPropertiesPluginData expectedPluginData = builder.build();

        assertEquals(expectedPluginData, actualPluginData);

    }

}