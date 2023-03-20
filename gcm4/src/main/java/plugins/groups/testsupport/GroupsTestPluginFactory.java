package plugins.groups.testsupport;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

import org.apache.commons.math3.random.RandomGenerator;
import org.apache.commons.math3.util.FastMath;

import nucleus.ActorContext;
import nucleus.NucleusError;
import nucleus.Plugin;
import nucleus.PluginData;
import nucleus.testsupport.testplugin.TestActorPlan;
import nucleus.testsupport.testplugin.TestPlugin;
import nucleus.testsupport.testplugin.TestPluginData;
import nucleus.testsupport.testplugin.TestSimulation;
import plugins.groups.GroupsPlugin;
import plugins.groups.GroupsPluginData;
import plugins.groups.reports.GroupPopulationReportPluginData;
import plugins.groups.reports.GroupPropertyReportPluginData;
import plugins.groups.support.GroupError;
import plugins.groups.support.GroupId;
import plugins.people.PeoplePlugin;
import plugins.people.PeoplePluginData;
import plugins.people.support.PersonError;
import plugins.people.support.PersonId;
import plugins.stochastics.StochasticsPlugin;
import plugins.stochastics.StochasticsPluginData;
import plugins.stochastics.support.StochasticsError;
import util.errors.ContractException;
import util.random.RandomGeneratorProvider;
import util.wrappers.MultiKey;

/**
 * A static test support class for the {@linkplain GroupsPlugin}. Provides
 * convenience
 * methods for obtaining standarized PluginData for the listed Plugin.
 * 
 * <p>
 * Also contains factory methods to obtain a list of plugins that is the minimal
 * set needed to adequately test this Plugin that can be
 * utilized with
 * </p>
 * 
 * <li>{@link TestSimulation#executeSimulation}
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

		private Data(int initialPopulation, double expectedGroupsPerPerson,
				double expectedPeoplePerGroup, long seed, TestPluginData testPluginData) {

			int membershipCount = (int) FastMath.round(initialPopulation * expectedGroupsPerPerson);
			int groupCount = expectedPeoplePerGroup == 0 ? 0
					: (int) FastMath.round(membershipCount / expectedPeoplePerGroup);

			this.peoplePluginData = getStandardPeoplePluginData(initialPopulation);
			this.groupsPluginData = getStandardGroupsPluginData(groupCount, membershipCount,
					this.peoplePluginData.getPersonIds(), seed);
			this.stochasticsPluginData = getStandardStochasticsPluginData(seed);
			this.testPluginData = testPluginData;
		}
	}

	/**
	 * Factory class that facilitates the building of {@linkplain PluginData}
	 * with the various setter methods.
	 */
	public static class Factory {
		private Data data;

		private Factory(Data data) {
			this.data = data;
		}

		/**
		 * Returns a list of plugins containing a Groups, People, Stocastics and a Test
		 * Plugin
		 * built from the contributed PluginDatas.
		 * 
		 * <li>GroupsPlugin is defaulted to one formed from
		 * {@link GroupsTestPluginFactory#getStandardGroupsPluginData}
		 * <li>PeoplePlugin is defaulted to one formed from
		 * {@link GroupsTestPluginFactory#getStandardPeoplePluginData}
		 * <li>StochasticsPlugin is defaulted to one formed from
		 * {@link GroupsTestPluginFactory#getStandardStochasticsPluginData}
		 * <li>TestPlugin is formed from the TestPluginData passed into
		 * {@link GroupsTestPluginFactory#factory}
		 */
		public List<Plugin> getPlugins() {
			List<Plugin> pluginsToAdd = new ArrayList<>();
			GroupsPlugin.Builder groupsPluginBuilder = GroupsPlugin.builder();
			groupsPluginBuilder.setGroupsPluginData(this.data.groupsPluginData);
			if(data.groupPopulationReportPluginData != null) {
				groupsPluginBuilder.setGroupPopulationReportPluginData(data.groupPopulationReportPluginData);	
			}
			if(data.groupPropertyReportPluginData != null) {
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
		 * Sets the {@link GroupsPluginData} in this Factory.
		 * This explicit instance of pluginData will be used to create a
		 * GroupsPlugin
		 * 
		 * @throws ContractExecption
		 *                           {@linkplain GroupError#NULL_GROUP_PLUGIN_DATA}
		 *                           if the passed in pluginData is null
		 */
		public Factory setGroupsPluginData(GroupsPluginData groupsPluginData) {
			if (groupsPluginData == null) {
				throw new ContractException(GroupError.NULL_GROUP_PLUGIN_DATA);
			}
			this.data.groupsPluginData = groupsPluginData;
			return this;
		}
		
		/**
		 * Sets the {@link GroupPopulationReportPluginData} in this Factory.
		 * This explicit instance of pluginData will be used to create a
		 * GroupsPlugin
		 * 
		 * @throws ContractExecption
		 *                           {@linkplain GroupError#NULL_GROUP_POPULATION_REPORT_PLUGIN_DATA}
		 *                           if the passed in pluginData is null
		 */
		public Factory setGroupPopulationReportPluginData(GroupPopulationReportPluginData groupPopulationReportPluginData) {
			if (groupPopulationReportPluginData == null) {
				throw new ContractException(GroupError.NULL_GROUP_POPULATION_REPORT_PLUGIN_DATA);
			}
			this.data.groupPopulationReportPluginData = groupPopulationReportPluginData;
			return this;
		}
		/**
		 * Sets the {@link GroupPopulationReportPluginData} in this Factory.
		 * This explicit instance of pluginData will be used to create a
		 * GroupsPlugin
		 * 
		 * @throws ContractExecption
		 *                           {@linkplain GroupError#NULL_GROUP_PROPERTY_REPORT_PLUGIN_DATA}
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
		 * Sets the {@link PeoplePluginData} in this Factory.
		 * This explicit instance of pluginData will be used to create a
		 * PeoplePlugin
		 * 
		 * @throws ContractExecption
		 *                           {@linkplain PersonError#NULL_PEOPLE_PLUGIN_DATA}
		 *                           if the passed in pluginData is null
		 */
		public Factory setPeoplePluginData(PeoplePluginData peoplePluginData) {
			if (peoplePluginData == null) {
				throw new ContractException(PersonError.NULL_PEOPLE_PLUGIN_DATA);
			}
			this.data.peoplePluginData = peoplePluginData;
			return this;
		}

		/**
		 * Sets the {@link StochasticsPluginData} in this Factory.
		 * This explicit instance of pluginData will be used to create a
		 * StochasticsPlugin
		 * 
		 * @throws ContractExecption
		 *                           {@linkplain StochasticsError#NULL_STOCHASTICS_PLUGIN_DATA}
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
	 * <li>{@link GroupsPluginData}
	 * <li>{@link PeoplePluginData}
	 * <li>{@link StochasticsPluginData}
	 * </ul>
	 * <li>either directly (by default) via
	 * <ul>
	 * <li>{@link #getStandardGroupsPluginData},
	 * <li>{@link #getStandardPeoplePluginData},
	 * <li>{@link #getStandardStochasticsPluginData}
	 * </ul>
	 * <li>or explicitly set via
	 * <ul>
	 * <li>{@link Factory#setGroupsPluginData},
	 * <li>{@link Factory#setPeoplePluginData},
	 * <li>{@link Factory#setStochasticsPluginData}
	 * </ul>
	 * 
	 * <li>via the
	 * {@link Factory#getPlugins()} method.
	 *
	 * 
	 * @throws ContractExecption
	 *                           {@linkplain NucleusError#NULL_PLUGIN_DATA}
	 *                           if testPluginData is null
	 */
	public static Factory factory(int initialPopulation, double expectedGroupsPerPerson,
			double expectedPeoplePerGroup, long seed, TestPluginData testPluginData) {
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
	 * <li>{@link GroupsPluginData}
	 * <li>{@link PeoplePluginData}
	 * <li>{@link StochasticsPluginData}
	 * </ul>
	 * <li>either directly (by default) via
	 * <ul>
	 * <li>{@link #getStandardGroupsPluginData},
	 * <li>{@link #getStandardPeoplePluginData},
	 * <li>{@link #getStandardStochasticsPluginData}
	 * </ul>
	 * <li>or explicitly set via
	 * <ul>
	 * <li>{@link Factory#setGroupsPluginData},
	 * <li>{@link Factory#setPeoplePluginData},
	 * <li>{@link Factory#setStochasticsPluginData}
	 * </ul>
	 * 
	 * <li>via the
	 * {@link Factory#getPlugins()} method.
	 *
	 * @throws ContractExecption
	 *                           {@linkplain NucleusError#NULL_ACTOR_CONTEXT_CONSUMER}
	 *                           if consumer is null
	 */
	public static Factory factory(int initialPopulation, double expectedGroupsPerPerson,
			double expectedPeoplePerGroup, long seed, Consumer<ActorContext> consumer) {
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
	 * testing the GroupsPlugin
	 * <li>The resulting GroupsPluginData will include:
	 * <ul>
	 * <li>Every GroupTypeId included in {@link TestGroupTypeId}
	 * <li>Every GroupPropertyId included in {@link TestGroupPropertyId}
	 * <ul>
	 * <li>along with the groupTypeId and propertyDefinition for each
	 * </ul>
	 * <li>A number of groups equal to the passed in groupCount
	 * <ul>
	 * <li>each group will get a random groupTypeId based on a RandomGenerator
	 * seeded by the passed
	 * in seed
	 * <li>every GroupPropertyId included in
	 * {@link TestGroupPropertyId} with a randomPropertyValue obtained from each
	 * based on the same RandomGenerator
	 * </ul>
	 * <li>an average group membership based on the passed in membershipCount and
	 * passed in people.
	 * <ul>
	 * <li>This is determined based on the above RandomGenerator.
	 * </ul>
	 * </ul>
	 */
	public static GroupsPluginData getStandardGroupsPluginData(int groupCount, int membershipCount,
			List<PersonId> people, long seed) {

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
			for (TestGroupPropertyId testGroupPropertyId : TestGroupPropertyId.getTestGroupPropertyIds(testGroupTypeId)) {
				groupBuilder.setGroupPropertyValue(groupId, testGroupPropertyId,
						testGroupPropertyId.getRandomPropertyValue(randomGenerator));
			}
			testGroupTypeId = testGroupTypeId.next();
		}

		Set<MultiKey> groupMemeberships = new LinkedHashSet<>();
		while (groupMemeberships.size() < membershipCount) {
			PersonId personId = people.get(randomGenerator.nextInt(people.size()));
			GroupId groupId = groups.get(randomGenerator.nextInt(groups.size()));
			groupMemeberships.add(new MultiKey(groupId, personId));
		}

		for (MultiKey multiKey : groupMemeberships) {
			GroupId groupId = multiKey.getKey(0);
			PersonId personId = multiKey.getKey(1);
			groupBuilder.addPersonToGroup(groupId, personId);
		}
		return groupBuilder.build();
	}

	/**
	 * Returns a standardized PeoplePluginData that is minimally adequate for
	 * testing the GroupsPlugin
	 * <li>The resulting PeoplePluginData will include:
	 * <ul>
	 * <li>a number of people equal to the passed in intialPopulation
	 * </ul>
	 */
	public static PeoplePluginData getStandardPeoplePluginData(int initialPopulation) {
		PeoplePluginData.Builder peopleBuilder = PeoplePluginData.builder();

		for (int i = 0; i < initialPopulation; i++) {
			peopleBuilder.addPersonId(new PersonId(i));
		}
		return peopleBuilder.build();
	}

	/**
	 * Returns a standardized StochasticsPluginData that is minimally adequate for
	 * testing the GroupsPlugin
	 * <li>The resulting StochasticsPluginData will include:
	 * <ul>
	 * <li>a seed based on the nextLong of a RandomGenerator seeded from the
	 * passed in seed
	 * </ul>
	 */
	public static StochasticsPluginData getStandardStochasticsPluginData(long seed) {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(seed);
		return StochasticsPluginData.builder()
				.setSeed(randomGenerator.nextLong()).build();
	}

}
