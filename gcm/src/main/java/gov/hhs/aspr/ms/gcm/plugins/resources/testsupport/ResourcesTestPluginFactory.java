package gov.hhs.aspr.ms.gcm.plugins.resources.testsupport;

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
import gov.hhs.aspr.ms.gcm.plugins.regions.support.RegionError;
import gov.hhs.aspr.ms.gcm.plugins.regions.support.RegionId;
import gov.hhs.aspr.ms.gcm.plugins.regions.testsupport.TestRegionId;
import gov.hhs.aspr.ms.gcm.plugins.resources.ResourcesPlugin;
import gov.hhs.aspr.ms.gcm.plugins.resources.datamanagers.ResourcesPluginData;
import gov.hhs.aspr.ms.gcm.plugins.resources.reports.PersonResourceReportPluginData;
import gov.hhs.aspr.ms.gcm.plugins.resources.reports.ResourcePropertyReportPluginData;
import gov.hhs.aspr.ms.gcm.plugins.resources.reports.ResourceReportPluginData;
import gov.hhs.aspr.ms.gcm.plugins.resources.support.ResourceError;
import gov.hhs.aspr.ms.gcm.plugins.stochastics.StochasticsPlugin;
import gov.hhs.aspr.ms.gcm.plugins.stochastics.datamanagers.StochasticsPluginData;
import gov.hhs.aspr.ms.gcm.plugins.stochastics.support.StochasticsError;
import gov.hhs.aspr.ms.gcm.plugins.stochastics.support.WellState;
import gov.hhs.aspr.ms.gcm.plugins.util.properties.PropertyDefinition;
import util.errors.ContractException;
import util.random.RandomGeneratorProvider;

/**
 * A static test support class for the {@linkplain ResourcesPlugin}. Provides
 * convenience methods for obtaining standarized PluginData for the listed
 * Plugin.
 * <p>
 * Also contains factory methods to obtain a list of plugins that is the minimal
 * set needed to adequately test this Plugin that can be utilized with
 * </p>
 * {@link TestSimulation#execute}
 */
public class ResourcesTestPluginFactory {

    private ResourcesTestPluginFactory() {
    }

    private static class Data {
        private ResourcesPluginData resourcesPluginData;
        private PersonResourceReportPluginData personResourceReportPluginData;
        private ResourcePropertyReportPluginData resourcePropertyReportPluginData;
        private ResourceReportPluginData resourceReportPluginData;

        private RegionsPluginData regionsPluginData;
        private PeoplePluginData peoplePluginData;
        private StochasticsPluginData stochasticsPluginData;
        private TestPluginData testPluginData;

        private Data(int initialPopulation, long seed, TestPluginData testPluginData) {
            RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(seed);
            this.peoplePluginData = getStandardPeoplePluginData(initialPopulation);
            this.resourcesPluginData = getStandardResourcesPluginData(this.peoplePluginData.getPersonIds(),
                    randomGenerator.nextLong());
            this.regionsPluginData = getStandardRegionsPluginData(this.peoplePluginData.getPersonIds(),
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
         * Returns a list of plugins containing a Resources, People, Regions,
         * Stochastics and Test Plugin built from the contributed PluginDatas.
         * <li>ResourcesPlugin is defaulted to one formed from
         * {@link ResourcesTestPluginFactory#getStandardResourcesPluginData}</li>
         * <li>RegionsPlugin is defaulted to one formed from
         * {@link ResourcesTestPluginFactory#getStandardRegionsPluginData}</li>
         * <li>PeoplePlugin is defaulted to one formed from
         * {@link ResourcesTestPluginFactory#getStandardPeoplePluginData}</li>
         * <li>StochasticsPlugin is defaulted to one formed from
         * {@link ResourcesTestPluginFactory#getStandardStochasticsPluginData}</li>
         * <li>TestPlugin is formed from the TestPluginData passed into
         * {@link ResourcesTestPluginFactory#factory}</li>
         */
        public List<Plugin> getPlugins() {
            List<Plugin> pluginsToAdd = new ArrayList<>();
            Plugin resourcesPlugin = //
                    ResourcesPlugin.builder()//
                            .setResourcesPluginData(this.data.resourcesPluginData)//
                            .setPersonResourceReportPluginData(data.personResourceReportPluginData)//
                            .setResourcePropertyReportPluginData(data.resourcePropertyReportPluginData)//
                            .setResourceReportPluginData(data.resourceReportPluginData)//
                            .getResourcesPlugin();//

            Plugin peoplePlugin = PeoplePlugin.getPeoplePlugin(this.data.peoplePluginData);

            Plugin regionsPlugin = RegionsPlugin.builder().setRegionsPluginData(this.data.regionsPluginData)
                    .getRegionsPlugin();

            Plugin stochasticPlugin = StochasticsPlugin.getStochasticsPlugin(this.data.stochasticsPluginData);

            Plugin testPlugin = TestPlugin.getTestPlugin(this.data.testPluginData);

            pluginsToAdd.add(resourcesPlugin);
            pluginsToAdd.add(peoplePlugin);
            pluginsToAdd.add(regionsPlugin);
            pluginsToAdd.add(stochasticPlugin);
            pluginsToAdd.add(testPlugin);

            return pluginsToAdd;
        }

        /**
         * Sets the {@link ResourcesPluginData} in this Factory. This explicit instance
         * of pluginData will be used to create a ResourcesPlugin
         * 
         * @throws ContractExecption {@linkplain ResourceError#NULL_RESOURCE_PLUGIN_DATA}
         *                           if the passed in pluginData is null
         */
        public Factory setResourcesPluginData(ResourcesPluginData resourcesPluginData) {
            if (resourcesPluginData == null) {
                throw new ContractException(ResourceError.NULL_RESOURCE_PLUGIN_DATA);
            }
            this.data.resourcesPluginData = resourcesPluginData;
            return this;
        }

        /**
         * Sets the {@link PersonResourceReportPluginData} in this Factory. This
         * explicit instance of pluginData will be used to create a ResourcesPlugin
         * 
         * @throws ContractExecption {@linkplain ResourceError#NULL_RESOURCE_PLUGIN_DATA}
         *                           if the passed in pluginData is null
         */
        public Factory setPersonResourceReportPluginData(
                PersonResourceReportPluginData personResourceReportPluginData) {
            if (personResourceReportPluginData == null) {
                throw new ContractException(ResourceError.NULL_PERSON_RESOURCE_REPORT_PLUGIN_DATA);
            }
            this.data.personResourceReportPluginData = personResourceReportPluginData;
            return this;
        }

        /**
         * Sets the {@link ResourcePropertyReportPluginData} in this Factory. This
         * explicit instance of pluginData will be used to create a ResourcesPlugin
         * 
         * @throws ContractExecption {@linkplain ResourceError#NULL_RESOURCE_PLUGIN_DATA}
         *                           if the passed in pluginData is null
         */
        public Factory setResourcePropertyReportPluginData(
                ResourcePropertyReportPluginData resourcePropertyReportPluginData) {
            if (resourcePropertyReportPluginData == null) {
                throw new ContractException(ResourceError.NULL_RESOURCE_PROPERTY_REPORT_PLUGIN_DATA);
            }
            this.data.resourcePropertyReportPluginData = resourcePropertyReportPluginData;
            return this;
        }

        /**
         * Sets the {@link ResourceReportPluginData} in this Factory. This explicit
         * instance of pluginData will be used to create a ResourcesPlugin
         * 
         * @throws ContractExecption {@linkplain ResourceError#NULL_RESOURCE_PLUGIN_DATA}
         *                           if the passed in pluginData is null
         */
        public Factory setResourceReportPluginData(ResourceReportPluginData resourceReportPluginData) {
            if (resourceReportPluginData == null) {
                throw new ContractException(ResourceError.NULL_RESOURCE_REPORT_PLUGIN_DATA);
            }
            this.data.resourceReportPluginData = resourceReportPluginData;
            return this;
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
     * needed to adequately test the {@link ResourcesPlugin} by generating:
     * <ul>
     * <li>{@link ResourcesPluginData}</li>
     * <li>{@link RegionsPluginData}</li>
     * <li>{@link PeoplePluginData}</li>
     * <li>{@link StochasticsPluginData}</li>
     * </ul>
     * either directly (by default) via
     * <ul>
     * <li>{@link #getStandardResourcesPluginData}</li>
     * <li>{@link #getStandardPeoplePluginData},
     * <li>{@link #getStandardRegionsPluginData},
     * <li>{@link #getStandardStochasticsPluginData}</li>
     * </ul>
     * or explicitly set via
     * <ul>
     * <li>{@link Factory#setResourcesPluginData},
     * <li>{@link Factory#setPeoplePluginData},
     * <li>{@link Factory#setRegionsPluginData},
     * <li>{@link Factory#setStochasticsPluginData}</li>
     * </ul>
     * via the {@link Factory#getPlugins()} method.
     * 
     * @throws ContractExecption {@linkplain NucleusError#NULL_PLUGIN_DATA} if
     *                           testPluginData is null
     */
    public static Factory factory(int initialPopulation, long seed, TestPluginData testPluginData) {
        if (testPluginData == null) {
            throw new ContractException(NucleusError.NULL_PLUGIN_DATA);
        }
        return new Factory(new Data(initialPopulation, seed, testPluginData));
    }

    /**
     * Creates a Factory that facilitates the creation of a minimal set of plugins
     * needed to adequately test the {@link ResourcesPlugin} by generating:
     * <ul>
     * <li>{@link ResourcesPluginData}</li>
     * <li>{@link RegionsPluginData}</li>
     * <li>{@link PeoplePluginData}</li>
     * <li>{@link StochasticsPluginData}</li>
     * </ul>
     * either directly (by default) via
     * <ul>
     * <li>{@link #getStandardResourcesPluginData}</li>
     * <li>{@link #getStandardPeoplePluginData},
     * <li>{@link #getStandardRegionsPluginData},
     * <li>{@link #getStandardStochasticsPluginData}</li>
     * </ul>
     * or explicitly set via
     * <ul>
     * <li>{@link Factory#setResourcesPluginData},
     * <li>{@link Factory#setPeoplePluginData},
     * <li>{@link Factory#setRegionsPluginData},
     * <li>{@link Factory#setStochasticsPluginData}</li>
     * </ul>
     * via the {@link Factory#getPlugins()} method.
     * 
     * @throws ContractExecption {@linkplain NucleusError#NULL_ACTOR_CONTEXT_CONSUMER}
     *                           if consumer is null
     */
    public static Factory factory(int initialPopulation, long seed, Consumer<ActorContext> consumer) {
        if (consumer == null) {
            throw new ContractException(NucleusError.NULL_ACTOR_CONTEXT_CONSUMER);
        }
        TestPluginData.Builder pluginBuilder = TestPluginData.builder();
        pluginBuilder.addTestActorPlan("actor", new TestActorPlan(0, consumer));
        TestPluginData testPluginData = pluginBuilder.build();
        return factory(initialPopulation, seed, testPluginData);
    }

    /**
     * Returns a standardized PeoplePluginData that is minimally adequate for
     * testing the ResourcesPlugin
     * <li>The resutling PeoplePluginData will include:
     * <ul>
     * <li>A number of people equal to the value of initialPopulation</li>
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
     * Returns a standardized RegionsPluginData that is minimally adequate for
     * testing the ResourcesPlugin
     * <li>The resulting RegionsPluginData will include:
     * <ul>
     * <li>Every RegionId included in {@link TestRegionId}</li>
     * <li>Every person passed in via people.
     * <ul>
     * <li>Each person will be assigned a random region based on the passed in seed
     * </li>
     * </ul>
     * </ul>
     */
    public static RegionsPluginData getStandardRegionsPluginData(List<PersonId> people, long seed) {
        RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(seed);

        RegionsPluginData.Builder regionBuilder = RegionsPluginData.builder();
        // add the regions
        for (TestRegionId testRegionId : TestRegionId.values()) {
            regionBuilder.addRegion(testRegionId);
        }
        for (PersonId personId : people) {
            TestRegionId randomRegionId = TestRegionId.getRandomRegionId(randomGenerator);
            regionBuilder.addPerson(personId, randomRegionId);
        }
        return regionBuilder.build();

    }

    /**
     * Returns a standardized ResourcesPluginData that is minimally adequate for
     * testing the ResourcesPlugin
     * <li>The resulting ResourcesPluginData will include:
     * <ul>
     * <li>Every ResourceId included in {@link TestResourceId}</li>
     * <ul>
     * <li>along with the defined timeTrackingPolicy for each</li>
     * </ul>
     * <li>Every ResourcePropertyId included in {@link TestResourcePropertyId} along
     * </li> with the defined propertyDefinition for each.
     * <ul>
     * <li>Each Resource will have a random property value assigned based on a
     * RandomGenerator that is created with the passed in seed</li></li>
     * </ul>
     * </ul>
     */
    public static ResourcesPluginData getStandardResourcesPluginData(List<PersonId> people, long seed) {
        RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(seed);

        ResourcesPluginData.Builder resourcesBuilder = ResourcesPluginData.builder();

        for (TestResourceId testResourceId : TestResourceId.values()) {
            resourcesBuilder.addResource(testResourceId, 0.0, testResourceId.getTimeTrackingPolicy());

            for (PersonId personId : people) {
                if (randomGenerator.nextBoolean()) {
                    resourcesBuilder.setPersonResourceLevel(personId, testResourceId, randomGenerator.nextInt(10));
                }
                boolean trackTimes = testResourceId.getTimeTrackingPolicy();
                if (trackTimes && randomGenerator.nextBoolean()) {
                    resourcesBuilder.setPersonResourceTime(personId, testResourceId, 0.0);
                }
            }

            for (RegionId regionId : TestRegionId.values()) {
                if (randomGenerator.nextBoolean()) {
                    resourcesBuilder.setRegionResourceLevel(regionId, testResourceId, randomGenerator.nextInt(10));
                } else {
                    resourcesBuilder.setRegionResourceLevel(regionId, testResourceId, 0);
                }
            }
        }

        for (TestResourcePropertyId testResourcePropertyId : TestResourcePropertyId.getTestResourcePropertyIds()) {
            TestResourceId testResourceId = testResourcePropertyId.getTestResourceId();
            PropertyDefinition propertyDefinition = testResourcePropertyId.getPropertyDefinition();
            boolean hasDeaultValue = propertyDefinition.getDefaultValue().isPresent();

            resourcesBuilder.defineResourceProperty(testResourceId, testResourcePropertyId, propertyDefinition);

            if (!hasDeaultValue) {
                Object propertyValue = testResourcePropertyId.getRandomPropertyValue(randomGenerator);
                resourcesBuilder.setResourcePropertyValue(testResourceId, testResourcePropertyId, propertyValue);
            }
        }

        for (TestResourcePropertyId testResourcePropertyId : TestResourcePropertyId
                .getShuffledTestResourcePropertyIds(randomGenerator)) {
            TestResourceId testResourceId = testResourcePropertyId.getTestResourceId();
            PropertyDefinition propertyDefinition = testResourcePropertyId.getPropertyDefinition();
            boolean hasDefault = propertyDefinition.getDefaultValue().isPresent();
            boolean setValue = randomGenerator.nextBoolean();
            if (hasDefault && setValue) {
                resourcesBuilder.setResourcePropertyValue(testResourceId, testResourcePropertyId,
                        propertyDefinition.getDefaultValue().get());
            } else if (setValue) {
                Object propertyValue = testResourcePropertyId.getRandomPropertyValue(randomGenerator);
                resourcesBuilder.setResourcePropertyValue(testResourceId, testResourcePropertyId, propertyValue);
            }
        }

        return resourcesBuilder.build();
    }

    /**
     * Returns a standardized StochasticsPluginData that is minimally adequate for
     * testing the ResourcesPlugin
     * <li>The resulting StochasticsPluginData will include:
     * <ul>
     * <li>a seed based on the nextLong of a RandomGenerator seeded from the passed
     * in seed</li></li>
     * </ul>
     */
    public static StochasticsPluginData getStandardStochasticsPluginData(long seed) {
        WellState wellState = WellState.builder().setSeed(seed).build();
        return StochasticsPluginData.builder().setMainRNGState(wellState).build();
    }

}
