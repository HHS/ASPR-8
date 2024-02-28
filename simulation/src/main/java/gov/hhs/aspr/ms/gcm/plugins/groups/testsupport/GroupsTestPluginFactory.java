package gov.hhs.aspr.ms.gcm.plugins.groups.testsupport;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.function.Consumer;

import org.apache.commons.math3.random.RandomGenerator;
import org.apache.commons.math3.util.FastMath;

import gov.hhs.aspr.ms.gcm.nucleus.ActorContext;
import gov.hhs.aspr.ms.gcm.nucleus.NucleusError;
import gov.hhs.aspr.ms.gcm.nucleus.Plugin;
import gov.hhs.aspr.ms.gcm.nucleus.PluginData;
import gov.hhs.aspr.ms.gcm.nucleus.testsupport.testplugin.TestActorPlan;
import gov.hhs.aspr.ms.gcm.nucleus.testsupport.testplugin.TestPlugin;
import gov.hhs.aspr.ms.gcm.nucleus.testsupport.testplugin.TestPluginData;
import gov.hhs.aspr.ms.gcm.nucleus.testsupport.testplugin.TestSimulation;
import gov.hhs.aspr.ms.gcm.plugins.groups.GroupsPlugin;
import gov.hhs.aspr.ms.gcm.plugins.groups.datamanagers.GroupsPluginData;
import gov.hhs.aspr.ms.gcm.plugins.groups.reports.GroupPopulationReportPluginData;
import gov.hhs.aspr.ms.gcm.plugins.groups.reports.GroupPropertyReportPluginData;
import gov.hhs.aspr.ms.gcm.plugins.groups.support.GroupError;
import gov.hhs.aspr.ms.gcm.plugins.groups.support.GroupId;
import gov.hhs.aspr.ms.gcm.plugins.people.PeoplePlugin;
import gov.hhs.aspr.ms.gcm.plugins.people.datamanagers.PeoplePluginData;
import gov.hhs.aspr.ms.gcm.plugins.people.support.PersonError;
import gov.hhs.aspr.ms.gcm.plugins.people.support.PersonId;
import gov.hhs.aspr.ms.gcm.plugins.people.support.PersonRange;
import gov.hhs.aspr.ms.gcm.plugins.properties.support.PropertyDefinition;
import gov.hhs.aspr.ms.gcm.plugins.stochastics.StochasticsPlugin;
import gov.hhs.aspr.ms.gcm.plugins.stochastics.datamanagers.StochasticsPluginData;
import gov.hhs.aspr.ms.gcm.plugins.stochastics.support.StochasticsError;
import gov.hhs.aspr.ms.gcm.plugins.stochastics.support.WellState;
import gov.hhs.aspr.ms.util.errors.ContractException;
import gov.hhs.aspr.ms.util.random.RandomGeneratorProvider;
import gov.hhs.aspr.ms.util.wrappers.MultiKey;

/**
 * A static test support class for the {@linkplain GroupsPlugin}. Provides
 * convenience methods for obtaining standarized PluginData for the listed
 * Plugin.
 * <p>
 * Also contains factory methods to obtain a list of plugins that is the minimal
 * set needed to adequately test this Plugin that can be utilized with
 * </p>
 * {@link TestSimulation#execute}
 */
public final class GroupsTestPluginFactory {

    private GroupsTestPluginFactory() {
    }

    private static class Data {
        private GroupPopulationReportPluginData groupPopulationReportPluginData;
        private GroupPropertyReportPluginData groupPropertyReportPluginData;
        private GroupsPluginData groupsPluginData;
        private PeoplePluginData peoplePluginData;
        private StochasticsPluginData stochasticsPluginData;
        private TestPluginData testPluginData;

        private Data(int initialPopulation, double expectedGroupsPerPerson, double expectedPeoplePerGroup, long seed,
                TestPluginData testPluginData) {

            RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(seed);

            this.peoplePluginData = getStandardPeoplePluginData(initialPopulation);
            this.groupsPluginData = getStandardGroupsPluginData(expectedGroupsPerPerson, expectedPeoplePerGroup,
                    this.peoplePluginData.getPersonIds(), randomGenerator.nextLong());
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
         * Returns a list of plugins containing a Groups, People, Stocastics and a Test
         * Plugin built from the contributed PluginDatas.
         * <ul>
         * <li>GroupsPlugin is defaulted to one formed from
         * {@link GroupsTestPluginFactory#getStandardGroupsPluginData}</li>
         * <li>PeoplePlugin is defaulted to one formed from
         * {@link GroupsTestPluginFactory#getStandardPeoplePluginData}</li>
         * <li>StochasticsPlugin is defaulted to one formed from
         * {@link GroupsTestPluginFactory#getStandardStochasticsPluginData}</li>
         * <li>TestPlugin is formed from the TestPluginData passed into
         * {@link GroupsTestPluginFactory#factory}</li>
         * </ul>
         */
        public List<Plugin> getPlugins() {
            List<Plugin> pluginsToAdd = new ArrayList<>();
            GroupsPlugin.Builder groupsPluginBuilder = GroupsPlugin.builder();
            groupsPluginBuilder.setGroupsPluginData(this.data.groupsPluginData);
            if (data.groupPopulationReportPluginData != null) {
                groupsPluginBuilder.setGroupPopulationReportPluginData(data.groupPopulationReportPluginData);
            }
            if (data.groupPropertyReportPluginData != null) {
                groupsPluginBuilder.setGroupPropertyReportPluginData(data.groupPropertyReportPluginData);
            }

            Plugin groupPlugin = groupsPluginBuilder.getGroupsPlugin();

            Plugin peoplePlugin = PeoplePlugin.getPeoplePlugin(this.data.peoplePluginData);

            Plugin stochasticPlugin = StochasticsPlugin.getStochasticsPlugin(this.data.stochasticsPluginData);

            Plugin testPlugin = TestPlugin.getTestPlugin(this.data.testPluginData);

            pluginsToAdd.add(groupPlugin);
            pluginsToAdd.add(peoplePlugin);
            pluginsToAdd.add(stochasticPlugin);
            pluginsToAdd.add(testPlugin);

            return pluginsToAdd;
        }

        /**
         * Sets the {@link GroupsPluginData} in this Factory. This explicit instance of
         * pluginData will be used to create a GroupsPlugin
         * 
         * @throws ContractException {@linkplain GroupError#NULL_GROUP_PLUGIN_DATA} if
         *                           the passed in pluginData is null
         */
        public Factory setGroupsPluginData(GroupsPluginData groupsPluginData) {
            if (groupsPluginData == null) {
                throw new ContractException(GroupError.NULL_GROUP_PLUGIN_DATA);
            }
            this.data.groupsPluginData = groupsPluginData;
            return this;
        }

        /**
         * Sets the {@link GroupPopulationReportPluginData} in this Factory. This
         * explicit instance of pluginData will be used to create a GroupsPlugin
         * 
         * @throws ContractException {@linkplain GroupError#NULL_GROUP_POPULATION_REPORT_PLUGIN_DATA}
         *                           if the passed in pluginData is null
         */
        public Factory setGroupPopulationReportPluginData(
                GroupPopulationReportPluginData groupPopulationReportPluginData) {
            if (groupPopulationReportPluginData == null) {
                throw new ContractException(GroupError.NULL_GROUP_POPULATION_REPORT_PLUGIN_DATA);
            }
            this.data.groupPopulationReportPluginData = groupPopulationReportPluginData;
            return this;
        }

        /**
         * Sets the {@link GroupPopulationReportPluginData} in this Factory. This
         * explicit instance of pluginData will be used to create a GroupsPlugin
         * 
         * @throws ContractException {@linkplain GroupError#NULL_GROUP_PROPERTY_REPORT_PLUGIN_DATA}
         *                           if the passed in pluginData is null
         */
        public Factory setGroupPropertyReportPluginData(GroupPropertyReportPluginData groupPropertyReportPluginData) {
            if (groupPropertyReportPluginData == null) {
                throw new ContractException(GroupError.NULL_GROUP_PROPERTY_REPORT_PLUGIN_DATA);
            }
            this.data.groupPropertyReportPluginData = groupPropertyReportPluginData;
            return this;
        }

        /**
         * Sets the {@link PeoplePluginData} in this Factory. This explicit instance of
         * pluginData will be used to create a PeoplePlugin
         * 
         * @throws ContractException {@linkplain PersonError#NULL_PEOPLE_PLUGIN_DATA} if
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
         * Sets the {@link StochasticsPluginData} in this Factory. This explicit
         * instance of pluginData will be used to create a StochasticsPlugin
         * 
         * @throws ContractException {@linkplain StochasticsError#NULL_STOCHASTICS_PLUGIN_DATA}
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
     * Returns a Factory that facilitates the creation of a minimal set of plugins
     * needed to adequately test the {@link GroupsPlugin} by generating:
     * <ul>
     * <li>{@link GroupsPluginData}</li>
     * <li>{@link PeoplePluginData}</li>
     * <li>{@link StochasticsPluginData}</li>
     * </ul>
     * either directly (by default) via
     * <ul>
     * <li>{@link #getStandardGroupsPluginData},
     * <li>{@link #getStandardPeoplePluginData},
     * <li>{@link #getStandardStochasticsPluginData}</li>
     * </ul>
     * or explicitly set via
     * <ul>
     * <li>{@link Factory#setGroupsPluginData},
     * <li>{@link Factory#setPeoplePluginData},
     * <li>{@link Factory#setStochasticsPluginData}</li>
     * </ul>
     * via the {@link Factory#getPlugins()} method.
     *
     * @throws ContractException {@linkplain NucleusError#NULL_PLUGIN_DATA} if
     *                           testPluginData is null
     */
    public static Factory factory(int initialPopulation, double expectedGroupsPerPerson, double expectedPeoplePerGroup,
            long seed, TestPluginData testPluginData) {
        if (testPluginData == null) {
            throw new ContractException(NucleusError.NULL_PLUGIN_DATA);
        }
        return new Factory(
                new Data(initialPopulation, expectedGroupsPerPerson, expectedPeoplePerGroup, seed, testPluginData));
    }

    /**
     * Creates a Factory that facilitates the creation of a minimal set of plugins
     * needed to adequately test the {@link GroupsPlugin} by generating:
     * <ul>
     * <li>{@link GroupsPluginData}</li>
     * <li>{@link PeoplePluginData}</li>
     * <li>{@link StochasticsPluginData}</li>
     * </ul>
     * either directly (by default) via
     * <ul>
     * <li>{@link #getStandardGroupsPluginData},
     * <li>{@link #getStandardPeoplePluginData},
     * <li>{@link #getStandardStochasticsPluginData}</li>
     * </ul>
     * or explicitly set via
     * <ul>
     * <li>{@link Factory#setGroupsPluginData},
     * <li>{@link Factory#setPeoplePluginData},
     * <li>{@link Factory#setStochasticsPluginData}</li>
     * </ul>
     * via the {@link Factory#getPlugins()} method.
     *
     * @throws ContractException {@linkplain NucleusError#NULL_ACTOR_CONTEXT_CONSUMER}
     *                           if consumer is null
     */
    public static Factory factory(int initialPopulation, double expectedGroupsPerPerson, double expectedPeoplePerGroup,
            long seed, Consumer<ActorContext> consumer) {
        if (consumer == null) {
            throw new ContractException(NucleusError.NULL_ACTOR_CONTEXT_CONSUMER);
        }

        TestPluginData.Builder pluginBuilder = TestPluginData.builder();
        pluginBuilder.addTestActorPlan("actor", new TestActorPlan(0, consumer));
        TestPluginData testPluginData = pluginBuilder.build();
        return factory(initialPopulation, expectedGroupsPerPerson, expectedPeoplePerGroup, seed, testPluginData);
    }

    /**
     * Returns a standardized GroupsPluginData that is minimally adequate for
     * testing the GroupsPlugin The resulting GroupsPluginData will include:
     * <ul>
     * <li>Every GroupTypeId included in {@link TestGroupTypeId}</li>
     * <li>Every GroupPropertyId included in {@link TestGroupPropertyId}
     * <ul>
     * <li>along with the groupTypeId and propertyDefinition for each</li>
     * </ul>
     * <li>A number of groups equal to the passed in groupCount
     * <ul>
     * <li>each group will get a random groupTypeId based on a RandomGenerator
     * seeded by the passed in seed</li>
     * <li>every GroupPropertyId included in {@link TestGroupPropertyId} with a
     * randomPropertyValue obtained from each based on the same RandomGenerator</li>
     * </ul>
     * <li>an average group membership based on the passed in membershipCount and
     * passed in people.
     * <ul>
     * <li>This is determined based on the above RandomGenerator.
     * </ul>
     * </ul>
     */
    public static GroupsPluginData getStandardGroupsPluginData(double expectedGroupsPerPerson,
            double expectedPeoplePerGroup, List<PersonId> people, long seed) {

        int membershipCount = (int) FastMath.round(people.size() * expectedGroupsPerPerson);
        int groupCount = (int) FastMath.round(membershipCount / expectedPeoplePerGroup);
        membershipCount = FastMath.min(membershipCount, groupCount * people.size());

        RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(seed);
        GroupsPluginData.Builder groupBuilder = GroupsPluginData.builder();

        for (TestGroupTypeId testGroupTypeId : TestGroupTypeId.values()) {
            groupBuilder.addGroupTypeId(testGroupTypeId);
        }

        for (TestGroupPropertyId testGroupPropertyId : TestGroupPropertyId.values()) {
            groupBuilder.defineGroupProperty(testGroupPropertyId.getTestGroupTypeId(), testGroupPropertyId,
                    testGroupPropertyId.getPropertyDefinition());

        }

        List<GroupId> groups = new ArrayList<>();

        TestGroupTypeId testGroupTypeId = TestGroupTypeId.GROUP_TYPE_1;
        for (int i = 0; i < groupCount; i++) {
            GroupId groupId = new GroupId(i);
            groups.add(groupId);
            groupBuilder.addGroup(groupId, testGroupTypeId);

            for (TestGroupPropertyId testGroupPropertyId : TestGroupPropertyId
                    .getShuffledTestGroupPropertyIds(testGroupTypeId, randomGenerator)) {
                PropertyDefinition propertyDefinition = testGroupPropertyId.getPropertyDefinition();
                boolean hasDefaultValue = propertyDefinition.getDefaultValue().isPresent();
                boolean setValue = randomGenerator.nextBoolean();

                if (!hasDefaultValue || setValue) {
                    groupBuilder.setGroupPropertyValue(groupId, testGroupPropertyId,
                            testGroupPropertyId.getRandomPropertyValue(randomGenerator));
                }
            }
            testGroupTypeId = testGroupTypeId.next();
        }

        List<MultiKey> groupMemeberships = new ArrayList<>();
        for (PersonId personId : people) {
            for (GroupId groupId : groups) {
                groupMemeberships.add(new MultiKey(groupId, personId));
            }
        }
        Collections.shuffle(groupMemeberships, new Random(randomGenerator.nextLong()));

        for (int i = 0; i < membershipCount; i++) {
            MultiKey multiKey = groupMemeberships.get(i);
            GroupId groupId = multiKey.getKey(0);
            PersonId personId = multiKey.getKey(1);
            groupBuilder.associatePersonToGroup(groupId, personId);
        }
        return groupBuilder.build();
    }

    /**
     * Returns a standardized PeoplePluginData that is minimally adequate for
     * testing the GroupsPlugin The resulting PeoplePluginData will include:
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
     * testing the GroupsPlugin The resulting StochasticsPluginData will include:
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
