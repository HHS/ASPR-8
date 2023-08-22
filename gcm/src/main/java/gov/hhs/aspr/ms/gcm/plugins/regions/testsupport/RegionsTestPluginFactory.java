package gov.hhs.aspr.ms.gcm.plugins.regions.testsupport;

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
import gov.hhs.aspr.ms.gcm.plugins.people.PeoplePlugin;
import gov.hhs.aspr.ms.gcm.plugins.people.datamanagers.PeoplePluginData;
import gov.hhs.aspr.ms.gcm.plugins.people.support.PersonError;
import gov.hhs.aspr.ms.gcm.plugins.people.support.PersonId;
import gov.hhs.aspr.ms.gcm.plugins.people.support.PersonRange;
import gov.hhs.aspr.ms.gcm.plugins.regions.RegionsPlugin;
import gov.hhs.aspr.ms.gcm.plugins.regions.datamanagers.RegionsPluginData;
import gov.hhs.aspr.ms.gcm.plugins.regions.reports.RegionPropertyReportPluginData;
import gov.hhs.aspr.ms.gcm.plugins.regions.reports.RegionTransferReportPluginData;
import gov.hhs.aspr.ms.gcm.plugins.regions.support.RegionError;
import gov.hhs.aspr.ms.gcm.plugins.stochastics.StochasticsPlugin;
import gov.hhs.aspr.ms.gcm.plugins.stochastics.datamanagers.StochasticsPluginData;
import gov.hhs.aspr.ms.gcm.plugins.stochastics.support.StochasticsError;
import gov.hhs.aspr.ms.gcm.plugins.stochastics.support.WellState;
import gov.hhs.aspr.ms.gcm.plugins.util.properties.PropertyDefinition;
import util.errors.ContractException;
import util.random.RandomGeneratorProvider;

/**
 * A static test support class for the {@linkplain RegionsPlugin}. Provides
 * convenience methods for obtaining standarized PluginData for the listed
 * Plugin.
 * <p>
 * Also contains factory methods to obtain a list of plugins that is the minimal
 * set needed to adequately test this Plugin that can be utilized with
 * </p>
 * {@link TestSimulation#execute}
 */
public final class RegionsTestPluginFactory {

    private RegionsTestPluginFactory() {
    }

    private static class Data {
        private RegionPropertyReportPluginData regionPropertyReportPluginData;
        private RegionTransferReportPluginData regionTransferReportPluginData;
        private RegionsPluginData regionsPluginData;
        private PeoplePluginData peoplePluginData;
        private StochasticsPluginData stochasticsPluginData;
        private TestPluginData testPluginData;

        private Data(int initialPopulation, boolean trackTimes, long seed, TestPluginData testPluginData) {
            RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(seed);
            this.peoplePluginData = getStandardPeoplePluginData(initialPopulation);
            this.regionsPluginData = getStandardRegionsPluginData(this.peoplePluginData.getPersonIds(), trackTimes,
                    randomGenerator.nextLong());
            this.stochasticsPluginData = getStandardStochasticsPluginData(randomGenerator.nextLong());
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
         * Returns a list of plugins containing a People, Regions, Stochastics and Test
         * Plugin built from the contributed PluginDatas.
         * {@link RegionsTestPluginFactory#getStandardRegionsPluginData}
         * <li>PeoplePlugin is defaulted to one formed from
         * {@link RegionsTestPluginFactory#getStandardPeoplePluginData}</li>
         * <li>StochasticsPlugin is defaulted to one formed from
         * {@link RegionsTestPluginFactory#getStandardStochasticsPluginData}</li>
         * <li>TestPlugin is formed from the TestPluginData passed into
         * {@link RegionsTestPluginFactory#factory}</li>
         * </ul>
         */
        public List<Plugin> getPlugins() {
            List<Plugin> pluginsToAdd = new ArrayList<>();

            Plugin peoplePlugin = PeoplePlugin.getPeoplePlugin(this.data.peoplePluginData);

            Plugin regionsPlugin = RegionsPlugin.builder()//
                    .setRegionsPluginData(this.data.regionsPluginData)//
                    .setRegionPropertyReportPluginData(this.data.regionPropertyReportPluginData)//
                    .setRegionTransferReportPluginData(this.data.regionTransferReportPluginData)//
                    .getRegionsPlugin();

            Plugin stochasticPlugin = StochasticsPlugin.getStochasticsPlugin(this.data.stochasticsPluginData);

            Plugin testPlugin = TestPlugin.getTestPlugin(this.data.testPluginData);

            pluginsToAdd.add(peoplePlugin);
            pluginsToAdd.add(regionsPlugin);
            pluginsToAdd.add(stochasticPlugin);
            pluginsToAdd.add(testPlugin);

            return pluginsToAdd;
        }

        /**
         * Sets the {@link PeoplePluginData} in this Factory. This explicit instance of
         * pluginData will be used to create a PeoplePlugin
         * 
         * @throws ContractExecption {@linkplain PersonError#NULL_PEOPLE_PLUGIN_DATA} if
         *                           the passed in pluginData is null
         */
        public Factory setPeoplePluginData(PeoplePluginData peoplePluginData) {
            if (peoplePluginData == null) {
                throw new ContractException(PersonError.NULL_PEOPLE_PLUGIN_DATA);
            }
            this.data.peoplePluginData = peoplePluginData;
            return this;
        }

        /**
         * Sets the {@link RegionPropertyReportPluginData} in this Factory. This
         * explicit instance of pluginData will be used to create a PeoplePlugin
         * 
         * @throws ContractExecption {@linkplain RegionError#NULL_REGION_PROPERTY_REPORT_PLUGIN_DATA}
         *                           if the passed in pluginData is null
         */
        public Factory setRegionPropertyReportPluginData(
                RegionPropertyReportPluginData regionPropertyReportPluginData) {
            if (regionPropertyReportPluginData == null) {
                throw new ContractException(RegionError.NULL_REGION_PROPERTY_REPORT_PLUGIN_DATA);
            }
            this.data.regionPropertyReportPluginData = regionPropertyReportPluginData;
            return this;
        }

        /**
         * Sets the {@link RegionTransferReportPluginData} in this Factory. This
         * explicit instance of pluginData will be used to create a PeoplePlugin
         * 
         * @throws ContractExecption {@linkplain RegionError#NULL_REGION_TRANSFER_REPORT_PLUGIN_DATA}
         *                           if the passed in pluginData is null
         */
        public Factory setRegionTransferReportPluginData(
                RegionTransferReportPluginData regionTransferReportPluginData) {
            if (regionTransferReportPluginData == null) {
                throw new ContractException(RegionError.NULL_REGION_TRANSFER_REPORT_PLUGIN_DATA);
            }
            this.data.regionTransferReportPluginData = regionTransferReportPluginData;
            return this;
        }

        /**
         * Sets the {@link RegionsPluginData} in this Factory. This explicit instance of
         * pluginData will be used to create a RegionsPlugin
         * 
         * @throws ContractExecption {@linkplain RegionError#NULL_REGION_PLUGIN_DATA} if
         *                           the passed in pluginData is null
         */
        public Factory setRegionsPluginData(RegionsPluginData regionsPluginData) {
            if (regionsPluginData == null) {
                throw new ContractException(RegionError.NULL_REGION_PLUGIN_DATA);
            }
            this.data.regionsPluginData = regionsPluginData;
            return this;
        }

        /**
         * Sets the {@link StochasticsPluginData} in this Factory. This explicit
         * instance of pluginData will be used to create a StochasticsPlugin
         * 
         * @throws ContractExecption {@linkplain StochasticsError#NULL_STOCHASTICS_PLUGIN_DATA}
         *                           if the passed in pluginData is null
         */
        public Factory setStochasticsPluginData(StochasticsPluginData stochasticsPluginData) {
            if (stochasticsPluginData == null) {
                throw new ContractException(StochasticsError.NULL_STOCHASTICS_PLUGIN_DATA);
            }
            this.data.stochasticsPluginData = stochasticsPluginData;
            return this;
        }

    }

    /**
     * Creates a Factory that facilitates the creation of a minimal set of plugins
     * needed to adequately test the {@link RegionsPlugin} by generating:
     * <ul>
     * <li>{@link RegionsPluginData}</li>
     * <li>{@link PeoplePluginData}</li>
     * <li>{@link StochasticsPluginData}</li>
     * </ul>
     * either directly (by default) via
     * <ul>
     * <li>{@link #getStandardPeoplePluginData},
     * <li>{@link #getStandardRegionsPluginData},
     * <li>{@link #getStandardStochasticsPluginData}</li>
     * </ul>
     * or explicitly set via
     * <ul>
     * <li>{@link Factory#setPeoplePluginData},
     * <li>{@link Factory#setRegionsPluginData},
     * <li>{@link Factory#setStochasticsPluginData}</li>
     * </ul>
     * via the {@link Factory#getPlugins()} method.
     * 
     * @throws ContractExecption {@linkplain NucleusError#NULL_PLUGIN_DATA} if
     *                           testPluginData is null
     */
    public static Factory factory(int initialPopulation, long seed, boolean trackTimes, TestPluginData testPluginData) {
        if (testPluginData == null) {
            throw new ContractException(NucleusError.NULL_PLUGIN_DATA);
        }

        return new Factory(new Data(initialPopulation, trackTimes, seed, testPluginData));

    }

    /**
     * Creates a Factory that facilitates the creation of a minimal set of plugins
     * needed to adequately test the {@link RegionsPlugin} by generating:
     * <ul>
     * <li>{@link RegionsPluginData}</li>
     * <li>{@link PeoplePluginData}</li>
     * <li>{@link StochasticsPluginData}</li>
     * </ul>
     * either directly (by default) via
     * <ul>
     * <li>{@link #getStandardPeoplePluginData},
     * <li>{@link #getStandardRegionsPluginData},
     * <li>{@link #getStandardStochasticsPluginData}</li>
     * </ul>
     * or explicitly set via
     * <ul>
     * <li>{@link Factory#setPeoplePluginData},
     * <li>{@link Factory#setRegionsPluginData},
     * <li>{@link Factory#setStochasticsPluginData}</li>
     * </ul>
     * via the {@link Factory#getPlugins()} method.
     * 
     * @throws ContractExecption {@linkplain NucleusError#NULL_ACTOR_CONTEXT_CONSUMER}
     *                           if consumer is null
     */
    public static Factory factory(int initialPopulation, long seed, boolean trackTimes,
            Consumer<ActorContext> consumer) {
        if (consumer == null) {
            throw new ContractException(NucleusError.NULL_ACTOR_CONTEXT_CONSUMER);
        }
        TestPluginData.Builder pluginBuilder = TestPluginData.builder();

        pluginBuilder.addTestActorPlan("actor", new TestActorPlan(0, consumer));
        TestPluginData testPluginData = pluginBuilder.build();
        return factory(initialPopulation, seed, trackTimes, testPluginData);
    }

    /**
     * Returns a standardized RegionsPluginData that is minimally adequate for
     * testing the RegionsPlugin The resulting RegionsPluginData will include:
     * <ul>
     * <li>Every RegionId in {@link TestRegionId}</li>
     * <li>Every RegionPropertyId in {@link TestRegionPropertyId}</li>
     * <ul>
     * <li>along with the propertyDefinition for each.
     * <li>If the propertyDefinition has a default value, that value is used.
     * Otherwise a randomPropertyValue is set using a RandomGenerator seeded by the
     * passed in seed via {@link TestRegionPropertyId#getRandomPropertyValue}
     * </ul>
     * <li>the passed in timeTrackingPolicy</li>
     * <li>Every person in the passed in list will be added to a RegionId</li>
     * <ul>
     * <li>starting with RegionId_1 and looping through all possible RegionIds in
     * {@link TestRegionId}
     * </ul>
     * </ul>
     */
    public static RegionsPluginData getStandardRegionsPluginData(List<PersonId> people, boolean trackTimes, long seed) {
        RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(seed);

        RegionsPluginData.Builder regionPluginBuilder = RegionsPluginData.builder();
        for (TestRegionId regionId : TestRegionId.values()) {
            regionPluginBuilder.addRegion(regionId);
        }

        for (TestRegionPropertyId testRegionPropertyId : TestRegionPropertyId.getTestRegionPropertyIds()) {
            PropertyDefinition propertyDefinition = testRegionPropertyId.getPropertyDefinition();
            regionPluginBuilder.defineRegionProperty(testRegionPropertyId, propertyDefinition);
            boolean hasDeaultValue = propertyDefinition.getDefaultValue().isPresent();

            if (!hasDeaultValue) {
                for (TestRegionId regionId : TestRegionId.values()) {
                    regionPluginBuilder.setRegionPropertyValue(regionId, testRegionPropertyId,
                            testRegionPropertyId.getRandomPropertyValue(randomGenerator));
                }
            }
        }

        for (TestRegionPropertyId testRegionPropertyId : TestRegionPropertyId
                .getTestShuffledRegionPropertyIds(randomGenerator)) {
            PropertyDefinition propertyDefinition = testRegionPropertyId.getPropertyDefinition();
            boolean hasDeaultValue = propertyDefinition.getDefaultValue().isPresent();
            boolean setValue = randomGenerator.nextBoolean();

            if (hasDeaultValue && setValue) {
                for (TestRegionId regionId : TestRegionId.values()) {
                    regionPluginBuilder.setRegionPropertyValue(regionId, testRegionPropertyId,
                            propertyDefinition.getDefaultValue().get());
                }
            } else if (setValue) {
                for (TestRegionId regionId : TestRegionId.values()) {
                    regionPluginBuilder.setRegionPropertyValue(regionId, testRegionPropertyId,
                            testRegionPropertyId.getRandomPropertyValue(randomGenerator));
                }
            }
        }

        regionPluginBuilder.setPersonRegionArrivalTracking(trackTimes);
        TestRegionId testRegionId = TestRegionId.REGION_1;
        if (trackTimes) {
            for (PersonId personId : people) {
                regionPluginBuilder.addPerson(personId, testRegionId, 0.0);
                testRegionId = testRegionId.next();
            }
        } else {
            for (PersonId personId : people) {
                regionPluginBuilder.addPerson(personId, testRegionId);
                testRegionId = testRegionId.next();
            }
        }

        return regionPluginBuilder.build();
    }

    /**
     * Returns a standardized PeoplePluginData that is minimally adequate for
     * testing the RegionsPlugin The resulting PeoplePluginData will include:
     * <ul>
     * <li>a number of people equal to the passed in intialPopulation</li>
     * </ul>
     */
    public static PeoplePluginData getStandardPeoplePluginData(int initialPopulation) {
        PeoplePluginData.Builder peopleBuilder = PeoplePluginData.builder();
        if (initialPopulation > 0) {
            peopleBuilder.addPersonRange(new PersonRange(0, initialPopulation - 1));
        }
        return peopleBuilder.build();
    }

    /**
     * Returns a standardized StochasticsPluginData that is minimally adequate for
     * testing the RegionsPlugin The resulting StochasticsPluginData will include:
     * <ul>
     * <li>a seed based on the nextLong of a RandomGenerator seeded from the passed
     * in seed</li>
     * </ul>
     */
    public static StochasticsPluginData getStandardStochasticsPluginData(long seed) {
        WellState wellState = WellState.builder().setSeed(seed).build();
        return StochasticsPluginData.builder().setMainRNGState(wellState).build();
    }
}
