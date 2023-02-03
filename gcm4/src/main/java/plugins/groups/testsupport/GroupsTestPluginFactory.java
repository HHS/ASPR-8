package plugins.groups.testsupport;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

import org.apache.commons.math3.random.RandomGenerator;
import org.apache.commons.math3.util.FastMath;

import nucleus.ActorContext;
import nucleus.Plugin;
import nucleus.testsupport.testplugin.TestActorPlan;
import nucleus.testsupport.testplugin.TestPlugin;
import nucleus.testsupport.testplugin.TestPluginData;
import plugins.groups.GroupsPlugin;
import plugins.groups.GroupsPluginData;
import plugins.groups.support.GroupId;
import plugins.people.PeoplePlugin;
import plugins.people.PeoplePluginData;
import plugins.people.support.PersonId;
import plugins.stochastics.StochasticsPlugin;
import plugins.stochastics.StochasticsPluginData;
import util.random.RandomGeneratorProvider;
import util.wrappers.MultiKey;

/**
 * A static test support class for the groups plugin. Provides convenience
 * methods for obtaining standard Groups, People and Stochastics PluginData.
 * 
 * Also contains factory methods to obtain a list of plugins that can be
 * utilized with
 * {@code TestSimulation.executeSimulation()}
 * 
 */
public final class GroupsTestPluginFactory {

	private GroupsTestPluginFactory() {
	}

	private static class Data {
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
		 * Method that will get the PluginData for the Groups, People, Stochastic and
		 * Test Plugins
		 * and use the respective PluginData to build Plugins
		 * 
		 * @return a List containing a GroupsPlugin, PeoplePlugin, StochasticsPlugin and
		 *         a TestPlugin
		 * 
		 */
		public List<Plugin> getPlugins() {
			List<Plugin> pluginsToAdd = new ArrayList<>();
			Plugin groupPlugin = GroupsPlugin.getGroupPlugin(this.data.groupsPluginData);

			// add the people plugin
			Plugin peoplePlugin = PeoplePlugin.getPeoplePlugin(this.data.peoplePluginData);

			// add the stochastics plugin
			Plugin stochasticPlugin = StochasticsPlugin.getStochasticsPlugin(this.data.stochasticsPluginData);

			Plugin testPlugin = TestPlugin.getTestPlugin(this.data.testPluginData);

			pluginsToAdd.add(groupPlugin);
			pluginsToAdd.add(peoplePlugin);
			pluginsToAdd.add(stochasticPlugin);
			pluginsToAdd.add(testPlugin);

			return pluginsToAdd;
		}

		/**
		 * Method to set the GroupsPluginData in this Factory.
		 * 
		 * @param groupsPluginData the GroupsPluginData you want to use, if different
		 *                         from the standard PluginData
		 * @return an instance of this Factory
		 * 
		 */
		public Factory setGroupsPluginData(GroupsPluginData groupsPluginData) {
			this.data.groupsPluginData = groupsPluginData;
			return this;
		}

		/**
		 * Method to set the PeoplePluginData in this Factory.
		 * 
		 * @param peoplePluginData the PeoplePluginData you want to use, if different
		 *                         from the standard PluginData
		 * @return an instance of this Factory
		 * 
		 */
		public Factory setPeoplePluginData(PeoplePluginData peoplePluginData) {
			this.data.peoplePluginData = peoplePluginData;
			return this;
		}

		/**
		 * Method to set the StochasticsPluginData in this Factory.
		 * 
		 * @param stochasticsPluginData the StochasticsPluginData you want to use, if
		 *                              different
		 *                              from the standard PluginData
		 * @return an instance of this Factory
		 * 
		 */
		public Factory setStochasticsPluginData(StochasticsPluginData stochasticsPluginData) {
			this.data.stochasticsPluginData = stochasticsPluginData;
			return this;
		}

	}

	/**
	 * Method that will generate GroupsPluginData, PeoplePluginData and
	 * StocasticsPluginData based on some configuration parameters.
	 * 
	 * @param initialPopulation       how many people are in the simulation at the
	 *                                start
	 * @param expectedGroupsPerPerson the average number of groups each person
	 *                                should be in
	 * @param expectedPeoplePerGroup  the average number of people that should be in
	 *                                each group
	 * @param seed                    used to seed a RandomGenerator
	 * @param testPluginData          PluginData that will be used to generate a
	 *                                TestPlugin
	 * @return a new instance of Factory
	 * 
	 */
	public static Factory factory(int initialPopulation, double expectedGroupsPerPerson,
			double expectedPeoplePerGroup, long seed, TestPluginData testPluginData) {
		return new Factory(
				new Data(initialPopulation, expectedGroupsPerPerson, expectedPeoplePerGroup, seed, testPluginData));
	}

	/**
	 * Method that will generate GroupsPluginData, PeoplePluginData,
	 * StocasticsPluginData and TestPluginData based on some configuration
	 * parameters.
	 * 
	 * @param initialPopulation       how many people are in the simulation at the
	 *                                start
	 * @param expectedGroupsPerPerson the average number of groups each person
	 *                                should be in
	 * @param expectedPeoplePerGroup  the average number of people that should be in
	 *                                each group
	 * @param seed                    used to seed a RandomGenerator
	 * @param consumer                consumer to use to generate TestPluginData
	 * @return a new instance of Factory
	 * 
	 */
	public static Factory factory(int initialPopulation, double expectedGroupsPerPerson,
			double expectedPeoplePerGroup, long seed, Consumer<ActorContext> consumer) {

		TestPluginData.Builder pluginBuilder = TestPluginData.builder();
		pluginBuilder.addTestActorPlan("actor", new TestActorPlan(0, consumer));
		TestPluginData testPluginData = pluginBuilder.build();
		return factory(initialPopulation, expectedGroupsPerPerson, expectedPeoplePerGroup, seed, testPluginData);
	}

	/**
	 * Method that will return a Standard GroupsPluginData based on some
	 * configuration parameters.
	 * 
	 * @param groupCount      how many groups there should be
	 * @param membershipCount initial population * expected people per group
	 * @param people          a List containing PersonIds. These should be the same
	 *                        PersonIds used in the PeoplePluginData
	 * @param seed            a seed to seed a RandomGenerator
	 * @return the resulting GroupsPluginData
	 * 
	 */
	public static GroupsPluginData getStandardGroupsPluginData(int groupCount, int membershipCount,
			List<PersonId> people, long seed) {

		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(seed);
		// add the group plugin
		GroupsPluginData.Builder groupBuilder = GroupsPluginData.builder();
		// add group types
		for (TestGroupTypeId testGroupTypeId : TestGroupTypeId.values()) {
			groupBuilder.addGroupTypeId(testGroupTypeId);
		}
		// define group properties
		for (TestGroupPropertyId testGroupPropertyId : TestGroupPropertyId.values()) {
			groupBuilder.defineGroupProperty(testGroupPropertyId.getTestGroupTypeId(), testGroupPropertyId,
					testGroupPropertyId.getPropertyDefinition());
		}

		// add the groups
		List<GroupId> groups = new ArrayList<>();
		for (int i = 0; i < groupCount; i++) {
			GroupId groupId = new GroupId(i);
			groups.add(groupId);
			TestGroupTypeId groupTypeId = TestGroupTypeId.getRandomGroupTypeId(randomGenerator);
			groupBuilder.addGroup(groupId, groupTypeId);
			for (TestGroupPropertyId testGroupPropertyId : TestGroupPropertyId.getTestGroupPropertyIds(groupTypeId)) {
				groupBuilder.setGroupPropertyValue(groupId, testGroupPropertyId,
						testGroupPropertyId.getRandomPropertyValue(randomGenerator));
			}
		}

		// add the group memberships
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
	 * Method that will return a Standard PeoplePluginData based on some
	 * configuration parameters.
	 * 
	 * @param initialPopulation how many people should be in the simulation at the
	 *                          start
	 * @return the resulting PeoplePluginData
	 * 
	 */
	public static PeoplePluginData getStandardPeoplePluginData(int initialPopulation) {
		PeoplePluginData.Builder peopleBuilder = PeoplePluginData.builder();

		for (int i = 0; i < initialPopulation; i++) {
			peopleBuilder.addPersonId(new PersonId(i));
		}
		return peopleBuilder.build();
	}

	/**
	 * Method that will return a Standard StochasticsPluginData based on some
	 * configuration parameters.
	 * 
	 * @param seed a seed to seed a RandomGenerator
	 * @return the resulting StocasticsPluginData
	 * 
	 */
	public static StochasticsPluginData getStandardStochasticsPluginData(long seed) {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(seed);
		return StochasticsPluginData.builder()
				.setSeed(randomGenerator.nextLong()).build();
	}

}
