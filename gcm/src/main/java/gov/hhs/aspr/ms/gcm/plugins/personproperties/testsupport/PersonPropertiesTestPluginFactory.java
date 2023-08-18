package gov.hhs.aspr.ms.gcm.plugins.personproperties.testsupport;

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
import gov.hhs.aspr.ms.gcm.plugins.personproperties.PersonPropertiesPlugin;
import gov.hhs.aspr.ms.gcm.plugins.personproperties.datamanagers.PersonPropertiesPluginData;
import gov.hhs.aspr.ms.gcm.plugins.personproperties.reports.PersonPropertyInteractionReportPluginData;
import gov.hhs.aspr.ms.gcm.plugins.personproperties.reports.PersonPropertyReportPluginData;
import gov.hhs.aspr.ms.gcm.plugins.personproperties.support.PersonPropertyError;
import gov.hhs.aspr.ms.gcm.plugins.regions.RegionsPlugin;
import gov.hhs.aspr.ms.gcm.plugins.regions.datamanagers.RegionsPluginData;
import gov.hhs.aspr.ms.gcm.plugins.regions.support.RegionError;
import gov.hhs.aspr.ms.gcm.plugins.regions.testsupport.TestRegionId;
import gov.hhs.aspr.ms.gcm.plugins.stochastics.StochasticsPlugin;
import gov.hhs.aspr.ms.gcm.plugins.stochastics.datamanagers.StochasticsPluginData;
import gov.hhs.aspr.ms.gcm.plugins.stochastics.support.StochasticsError;
import gov.hhs.aspr.ms.gcm.plugins.stochastics.support.WellState;
import util.errors.ContractException;
import util.random.RandomGeneratorProvider;

/**
 * A static test support class for the {@linkplain PersonPropertiesPlugin}.
 * Provides convenience methods for obtaining standarized PluginData for the
 * listed Plugin.
 * <p>
 * Also contains factory methods to obtain a list of plugins that is the minimal
 * set needed to adequately test this Plugin that can be utilized with
 * </p>
 * {@link TestSimulation#execute}
 */
public class PersonPropertiesTestPluginFactory {

    private PersonPropertiesTestPluginFactory() {
    }

    private static class Data {
        private PersonPropertyInteractionReportPluginData personPropertyInteractionReportPluginData;
        private PersonPropertyReportPluginData personPropertyReportPluginData;
        private PersonPropertiesPluginData personPropertiesPluginData;
        private RegionsPluginData regionsPluginData;
        private PeoplePluginData peoplePluginData;
        private StochasticsPluginData stochasticsPluginData;
        private TestPluginData testPluginData;

        private Data(int initialPopulation, long seed, TestPluginData testPluginData) {
            RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(seed);
            this.peoplePluginData = getStandardPeoplePluginData(initialPopulation);
            this.personPropertiesPluginData = PersonPropertiesTestPluginFactory.getStandardPersonPropertiesPluginData(
                    this.peoplePluginData.getPersonIds(), randomGenerator.nextLong());
            this.regionsPluginData = PersonPropertiesTestPluginFactory
                    .getStandardRegionsPluginData(this.peoplePluginData.getPersonIds(), randomGenerator.nextLong());
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
         * Returns a list of plugins containing a PersonProperties, Regions, People,
         * Stochastics and Test Plugin built from the contributed PluginDatas.
         * <li>PersonPropertiesPlugin is defaulted to one formed from
         * {@link PersonPropertiesTestPluginFactory#getStandardPersonPropertiesPluginData}
         * </li>
         * <li>RegionsPlugin is defaulted to one formed from
         * {@link PersonPropertiesTestPluginFactory#getStandardRegionsPluginData}</li>
         * <li>PeoplePlugin is defaulted to one formed from
         * {@link PersonPropertiesTestPluginFactory#getStandardPeoplePluginData}</li>
         * <li>StochasticsPlugin is defaulted to one formed from
         * {@link PersonPropertiesTestPluginFactory#getStandardStochasticsPluginData}
         * </li>
         * <li>TestPlugin is formed from the TestPluginData passed into
         * {@link PersonPropertiesTestPluginFactory#factory}</li>
         */
        public List<Plugin> getPlugins() {
            List<Plugin> pluginsToAdd = new ArrayList<>();

            PersonPropertiesPlugin.Builder personPropertiesPluginBuilder = PersonPropertiesPlugin.builder()
                    .setPersonPropertiesPluginData(data.personPropertiesPluginData);
            if (data.personPropertyInteractionReportPluginData != null) {
                personPropertiesPluginBuilder
                        .setPersonPropertyInteractionReportPluginData(data.personPropertyInteractionReportPluginData);
            }
            if (data.personPropertyReportPluginData != null) {
                personPropertiesPluginBuilder.setPersonPropertyReportPluginData(data.personPropertyReportPluginData);
            }
            Plugin personPropertiesPlugin = personPropertiesPluginBuilder.getPersonPropertyPlugin();

            Plugin peoplePlugin = PeoplePlugin.getPeoplePlugin(this.data.peoplePluginData);

            Plugin regionsPlugin = RegionsPlugin.builder().setRegionsPluginData(this.data.regionsPluginData)
                    .getRegionsPlugin();

            Plugin stochasticPlugin = StochasticsPlugin.getStochasticsPlugin(this.data.stochasticsPluginData);

            Plugin testPlugin = TestPlugin.getTestPlugin(this.data.testPluginData);

            pluginsToAdd.add(personPropertiesPlugin);
            pluginsToAdd.add(peoplePlugin);
            pluginsToAdd.add(regionsPlugin);
            pluginsToAdd.add(stochasticPlugin);
            pluginsToAdd.add(testPlugin);

            return pluginsToAdd;
        }

        /**
         * Sets the {@link PersonPropertiesPluginData} in this Factory. This explicit
         * instance of pluginData will be used to create a PersonPropertiesPlugin
         * 
         * @throws ContractExecption {@linkplain PersonPropertyError#NULL_PERSON_PROPERTY_PLUGN_DATA}
         *                           if the passed in pluginData is null
         */
        public Factory setPersonPropertiesPluginData(PersonPropertiesPluginData personPropertiesPluginData) {
            if (personPropertiesPluginData == null) {
                throw new ContractException(PersonPropertyError.NULL_PERSON_PROPERTY_PLUGN_DATA);
            }
            this.data.personPropertiesPluginData = personPropertiesPluginData;
            return this;
        }

        /**
         * Sets the {@link PersonPropertyInteractionReportPluginData} in this Factory.
         * This explicit instance of pluginData will be used to create a
         * PersonPropertiesPlugin
         * 
         * @throws ContractExecption {@linkplain PersonPropertyError#NULL_PERSON_PROPERTY_INTERACTION_REPORT_PLUGIN_DATA}
         *                           if the passed in pluginData is null
         */
        public Factory setPersonPropertyInteractionReportPluginData(
                PersonPropertyInteractionReportPluginData personPropertyInteractionReportPluginData) {
            if (personPropertyInteractionReportPluginData == null) {
                throw new ContractException(PersonPropertyError.NULL_PERSON_PROPERTY_INTERACTION_REPORT_PLUGIN_DATA);
            }
            this.data.personPropertyInteractionReportPluginData = personPropertyInteractionReportPluginData;
            return this;
        }

        /**
         * Sets the {@link PersonPropertyInteractionReportPluginData} in this Factory.
         * This explicit instance of pluginData will be used to create a
         * PersonPropertiesPlugin
         * 
         * @throws ContractExecption {@linkplain PersonPropertyError#NULL_PERSON_PROPERTY_REPORT_PLUGIN_DATA}
         *                           if the passed in pluginData is null
         */
        public Factory setPersonPropertyReportPluginData(
                PersonPropertyReportPluginData personPropertyReportPluginData) {
            if (personPropertyReportPluginData == null) {
                throw new ContractException(PersonPropertyError.NULL_PERSON_PROPERTY_REPORT_PLUGIN_DATA);
            }
            this.data.personPropertyReportPluginData = personPropertyReportPluginData;
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
     * needed to adequately test the {@link PersonPropertiesPlugin} by generating:
     * <ul>
     * <li>{@link PersonPropertiesPluginData}</li>
     * <li>{@link RegionsPluginData}</li>
     * <li>{@link PeoplePluginData}</li>
     * <li>{@link StochasticsPluginData}</li>
     * </ul>
     * <li>either directly (by default) via
     * <ul>
     * <li>{@link #getStandardPersonPropertiesPluginData}</li>
     * <li>{@link #getStandardPeoplePluginData},
     * <li>{@link #getStandardRegionsPluginData},
     * <li>{@link #getStandardStochasticsPluginData}</li>
     * </ul>
     * <li>or explicitly set via</li>
     * <ul>
     * <li>{@link Factory#setPersonPropertiesPluginData}</li>
     * <li>{@link Factory#setPeoplePluginData},
     * <li>{@link Factory#setRegionsPluginData},
     * <li>{@link Factory#setStochasticsPluginData}</li>
     * </ul>
     * <li>via the {@link Factory#getPlugins()} method.
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
     * needed to adequately test the {@link PersonPropertiesPlugin} by generating:
     * <ul>
     * <li>{@link PersonPropertiesPluginData}</li>
     * <li>{@link RegionsPluginData}</li>
     * <li>{@link PeoplePluginData}</li>
     * <li>{@link StochasticsPluginData}</li>
     * </ul>
     * <li>either directly (by default) via
     * <ul>
     * <li>{@link #getStandardPersonPropertiesPluginData}</li>
     * <li>{@link #getStandardPeoplePluginData},
     * <li>{@link #getStandardRegionsPluginData},
     * <li>{@link #getStandardStochasticsPluginData}</li>
     * </ul>
     * <li>or explicitly set via</li>
     * <ul>
     * <li>{@link Factory#setPersonPropertiesPluginData}</li>
     * <li>{@link Factory#setPeoplePluginData},
     * <li>{@link Factory#setRegionsPluginData},
     * <li>{@link Factory#setStochasticsPluginData}</li>
     * </ul>
     * <li>via the {@link Factory#getPlugins()} method.
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
     * Returns a standardized PersonPropertiesPluginData that is minimally adequate
     * for testing the PersonPropertiesPlugin.
     * <li>The resulting PersonPropertiesPluginData will include:
     * <ul>
     * <li>Every PersonPropertyId included in {@link TestPersonPropertyId}</li>
     * <ul>
     * <li>along with the propertyDefinition for each</li>
     * </ul>
     * <li>Every person in the list of passed in people.
     * <ul>
     * <li>Each person will have a property value if the propertyDefinition does not
     * </li> have a defaultValue, OR via a RandomGenerator seeded by the passed in
     * seed via nextBoolean.
     * <li>The property value is gotten from
     * {@link TestPersonPropertyId#getRandomPropertyValue}</li>
     * </ul>
     * </ul>
     */
    public static PersonPropertiesPluginData getStandardPersonPropertiesPluginData(List<PersonId> people, long seed,
            double... propertyTime) {
        RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(seed);

        double actualPropertyTime = 0;
        if (propertyTime.length > 0) {
            actualPropertyTime = propertyTime[0];
        }

        PersonPropertiesPluginData.Builder personPropertyBuilder = PersonPropertiesPluginData.builder();

        for (TestPersonPropertyId testPersonPropertyId : TestPersonPropertyId.getPersonPropertyIds()) {
            personPropertyBuilder.definePersonProperty(testPersonPropertyId,
                    testPersonPropertyId.getPropertyDefinition(), 0.0, testPersonPropertyId.isTimeTracked());
        }

        for (PersonId personId : people) {

            for (TestPersonPropertyId testPersonPropertyId : TestPersonPropertyId
                    .getShuffledPersonPropertyIds(randomGenerator)) {

                boolean hasDefault = testPersonPropertyId.getPropertyDefinition().getDefaultValue().isPresent();
                boolean setValue = randomGenerator.nextBoolean();

                if (!hasDefault || setValue) {
                    Object randomPropertyValue = testPersonPropertyId.getRandomPropertyValue(randomGenerator);
                    personPropertyBuilder.setPersonPropertyValue(personId, testPersonPropertyId, randomPropertyValue);
                } else if (hasDefault && personId.getValue() % 5 == 0) {
                    personPropertyBuilder.setPersonPropertyValue(personId, testPersonPropertyId,
                            testPersonPropertyId.getPropertyDefinition().getDefaultValue().get());
                }

                if (testPersonPropertyId.isTimeTracked() && personId.getValue() % 5 == 0) {
                    personPropertyBuilder.setPersonPropertyTime(personId, testPersonPropertyId, actualPropertyTime);
                }
            }
        }

        return personPropertyBuilder.build();
    }

    /**
     * Returns a standardized PeoplePluginData that is minimally adequate for
     * testing the PersonPropertiesPlugin
     * <li>The resulting PeoplePluginData will include:
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
     * Returns a standardized RegionsPluginData that is minimally adequate for
     * testing the PersonPropertiesPlugin
     * <li>The resulting RegionsPluginData will include:
     * <ul>
     * <li>Every RegionId included in {@link TestRegionId}</li>
     * <li>Every person in the list of passed in people.
     * <ul>
     * <li>Each person will be assigned to a random region based on a
     * RandomGenerator seeded by the passed in seed and selected via the
     * {@link TestRegionId#getRandomRegionId}</li>
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
     * Returns a standardized StochasticsPluginData that is minimally adequate for
     * testing the PersonPropertiesPlugin
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
