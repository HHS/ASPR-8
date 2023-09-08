package gov.hhs.aspr.ms.gcm.plugins.globalproperties.testsupport;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import org.apache.commons.math3.random.RandomGenerator;

import gov.hhs.aspr.ms.gcm.nucleus.ActorContext;
import gov.hhs.aspr.ms.gcm.nucleus.NucleusError;
import gov.hhs.aspr.ms.gcm.nucleus.Plugin;
import gov.hhs.aspr.ms.gcm.nucleus.PluginData;
import gov.hhs.aspr.ms.gcm.nucleus.testsupport.testplugin.TestActorPlan;
import gov.hhs.aspr.ms.gcm.nucleus.testsupport.testplugin.TestPlugin;
import gov.hhs.aspr.ms.gcm.nucleus.testsupport.testplugin.TestPluginData;
import gov.hhs.aspr.ms.gcm.nucleus.testsupport.testplugin.TestSimulation;
import gov.hhs.aspr.ms.gcm.plugins.globalproperties.GlobalPropertiesPlugin;
import gov.hhs.aspr.ms.gcm.plugins.globalproperties.datamanagers.GlobalPropertiesPluginData;
import gov.hhs.aspr.ms.gcm.plugins.globalproperties.reports.GlobalPropertyReportPluginData;
import gov.hhs.aspr.ms.gcm.plugins.globalproperties.support.GlobalPropertiesError;
import util.errors.ContractException;
import util.random.RandomGeneratorProvider;

/**
 * A static test support class for the {@linkplain GlobalPropertiesPlugin}.
 * Provides convenience methods for obtaining standarized PluginData for the
 * listed Plugin.
 * <p>
 * Also contains factory methods to obtain a list of plugins that is the minimal
 * set needed to adequately test this Plugin that can be utilized with
 * </p>
 * {@link TestSimulation#execute}
 */
public final class GlobalPropertiesTestPluginFactory {

    private GlobalPropertiesTestPluginFactory() {
    }

    private static class Data {
        private GlobalPropertiesPluginData globalPropertiesPluginData;
        private GlobalPropertyReportPluginData globalPropertyReportPluginData;
        private TestPluginData testPluginData;

        private Data(long seed, TestPluginData testPluginData) {
            this.globalPropertiesPluginData = getStandardGlobalPropertiesPluginData(seed);
            this.testPluginData = testPluginData;
        }
    }

    /**
     * Factory class that facilitates the building of {@linkplain PluginData} with
     * the various setter methods.
     */
    public static class Factory {
        private Data data;

        private Factory(Data data) {
            this.data = data;
        }

        /**
         * Returns a list of plugins containing a GlobalProperties and a Test Plugin
         * built from the contributed PluginDatas. <ul>
         * <li>GlobalPropertiesPlugin is defaulted to one formed from
         * {@link GlobalPropertiesTestPluginFactory#getStandardGlobalPropertiesPluginData}
         * </li>
         * <li>TestPlugin is formed from the TestPluginData passed into
         * {@link GlobalPropertiesTestPluginFactory#factory}</li>
         * </ul>
         */
        public List<Plugin> getPlugins() {
            List<Plugin> pluginsToAdd = new ArrayList<>();

            GlobalPropertiesPlugin.Builder builder = GlobalPropertiesPlugin.builder();

            builder.setGlobalPropertiesPluginData(this.data.globalPropertiesPluginData);
            if (this.data.globalPropertyReportPluginData != null) {
                builder.setGlobalPropertyReportPluginData(this.data.globalPropertyReportPluginData);
            }

            Plugin globalPropertiesPlugin = builder.getGlobalPropertiesPlugin();
            pluginsToAdd.add(globalPropertiesPlugin);

            Plugin testPlugin = TestPlugin.getTestPlugin(this.data.testPluginData);
            pluginsToAdd.add(testPlugin);

            return pluginsToAdd;
        }

        /**
         * Sets the {@link GlobalPropertiesPluginData} in this Factory. This explicit
         * instance of pluginData will be used to create a GlobalPropertiesPlugin
         * 
         * @throws ContractException {@linkplain GlobalPropertiesError#NULL_GLOBAL_PLUGIN_DATA}
         *                           if the passed in pluginData is null
         */
        public Factory setGlobalPropertiesPluginData(GlobalPropertiesPluginData globalPropertiesPluginData) {
            if (globalPropertiesPluginData == null) {
                throw new ContractException(GlobalPropertiesError.NULL_GLOBAL_PLUGIN_DATA);
            }
            this.data.globalPropertiesPluginData = globalPropertiesPluginData;
            return this;
        }

        /**
         * Sets the {@link GlobalPropertyReportPluginData} in this Factory. This
         * explicit instance of pluginData will be used to create a
         * GlobalPropertiesPlugin
         * 
         * @throws ContractException {@linkplain GlobalPropertiesError#NULL_GLOBAL_PROPERTY_REPORT_PLUGIN_DATA}
         *                           if the passed in pluginData is null
         */
        public Factory setGlobalPropertyReportPluginData(
                GlobalPropertyReportPluginData globalPropertyReportPluginData) {
            if (globalPropertyReportPluginData == null) {
                throw new ContractException(GlobalPropertiesError.NULL_GLOBAL_PROPERTY_REPORT_PLUGIN_DATA);
            }
            this.data.globalPropertyReportPluginData = globalPropertyReportPluginData;
            return this;
        }

    }

    /**
     * Returns a Factory that facilitates the creation of a minimal set of plugins
     * needed to adequately test the {@link GlobalPropertiesPlugin} by generating:
     * <ul>
     * <li>{@link GlobalPropertiesPluginData}</li>
     * </ul>
     * either directly (by default) via
     * <ul>
     * <li>{@link #getStandardGlobalPropertiesPluginData}</li>
     * </ul>
     * or explicitly set via
     * <ul>
     * <li>{@link Factory#setGlobalPropertiesPluginData}</li>
     * </ul>
     * via the {@link Factory#getPlugins()} method.
     * 
     * @throws ContractException {@linkplain NucleusError#NULL_PLUGIN_DATA} if
     *                           testPluginData is null
     */
    public static Factory factory(long seed, TestPluginData testPluginData) {
        if (testPluginData == null) {
            throw new ContractException(NucleusError.NULL_PLUGIN_DATA);
        }
        return new Factory(new Data(seed, testPluginData));
    }

    /**
     * Returns a Factory that facilitates the creation of a minimal set of plugins
     * needed to adequately test the {@link GlobalPropertiesPlugin} by generating:
     * <ul>
     * <li>{@link GlobalPropertiesPluginData}</li>
     * </ul>
     * either directly (by default) via
     * <ul>
     * <li>{@link #getStandardGlobalPropertiesPluginData}</li>
     * </ul>
     * or explicitly set via
     * <ul>
     * <li>{@link Factory#setGlobalPropertiesPluginData}</li>
     * </ul>
     * via the {@link Factory#getPlugins()} method.
     * 
     * @throws ContractException {@linkplain NucleusError#NULL_ACTOR_CONTEXT_CONSUMER}
     *                           if consumer is null
     */
    public static Factory factory(long seed, Consumer<ActorContext> consumer) {
        if (consumer == null) {
            throw new ContractException(NucleusError.NULL_ACTOR_CONTEXT_CONSUMER);
        }
        TestPluginData.Builder pluginBuilder = TestPluginData.builder();
        pluginBuilder.addTestActorPlan("actor", new TestActorPlan(0, consumer));
        TestPluginData testPluginData = pluginBuilder.build();
        return factory(seed, testPluginData);
    }

    /**
     * Returns a Standardized GlobalPropertiesPluginData that is minimally adequate
     * for testing the GlobalPropertiesPlugin The resulting
     * GlobalPropertiesPluginData will include:
     * <ul>
     * <li>Every GlobalPropertyId included in {@link TestGlobalPropertyId}
     * <ul>
     * <li>with the defined propertyDefinition for each</li>
     * </ul>
     * </ul>
     */
    public static GlobalPropertiesPluginData getStandardGlobalPropertiesPluginData(long seed) {
        RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(seed);

        GlobalPropertiesPluginData.Builder globalsPluginBuilder = GlobalPropertiesPluginData.builder();

        for (TestGlobalPropertyId testGlobalPropertyId : TestGlobalPropertyId.getGlobalPropertyIds()) {
            globalsPluginBuilder.defineGlobalProperty(testGlobalPropertyId,
                    testGlobalPropertyId.getPropertyDefinition(), 0);
            boolean hasDefaultValue = testGlobalPropertyId.getPropertyDefinition().getDefaultValue().isPresent();
            if (!hasDefaultValue) {
                globalsPluginBuilder.setGlobalPropertyValue(testGlobalPropertyId,
                        testGlobalPropertyId.getRandomPropertyValue(randomGenerator), 0);
            }
        }

        for (TestGlobalPropertyId testGlobalPropertyId : TestGlobalPropertyId
                .getShuffledGlobalPropertyIds(randomGenerator)) {

            boolean hasDefaultValue = testGlobalPropertyId.getPropertyDefinition().getDefaultValue().isPresent();
            boolean setValue = randomGenerator.nextBoolean();
            if (hasDefaultValue && setValue) {
                // set a value to the default
                globalsPluginBuilder.setGlobalPropertyValue(testGlobalPropertyId,
                        testGlobalPropertyId.getPropertyDefinition().getDefaultValue().get(), 0);
            } else if (setValue) {
                // set a value to not the default
                globalsPluginBuilder.setGlobalPropertyValue(testGlobalPropertyId,
                        testGlobalPropertyId.getRandomPropertyValue(randomGenerator), 0);
            }
        }

        return globalsPluginBuilder.build();
    }

}
