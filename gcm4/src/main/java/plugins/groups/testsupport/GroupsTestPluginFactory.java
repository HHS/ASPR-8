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
 * methods for integrating an action plugin into a groups-based simulation test
 * harness.
 * 
 * 
 *
 */
public final class GroupsTestPluginFactory {

	private static class Data {
		private GroupsPluginData groupsPluginData;
		private PeoplePluginData peoplePluginData;
		private StochasticsPluginData stochasticsPluginData;
		private Plugin testPlugin;

		private Data(int initialPopulation, double expectedGroupsPerPerson,
				double expectedPeoplePerGroup, long seed, Plugin testPlugin) {
			RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(seed);

			int membershipCount = (int) FastMath.round(initialPopulation * expectedGroupsPerPerson);
			int groupCount = (int) FastMath.round(membershipCount / expectedPeoplePerGroup);

			this.peoplePluginData = GroupsTestPluginFactory.getStandardPeoplePluginData(initialPopulation);
			this.groupsPluginData = GroupsTestPluginFactory.getStandardGroupsPluginData(groupCount, membershipCount,
					this.peoplePluginData.getPersonIds(), randomGenerator);
			this.stochasticsPluginData = GroupsTestPluginFactory.getStandardStochasticsPluginData(randomGenerator);
			this.testPlugin = testPlugin;
		}
	}

	public static Factory factory(int initialPopulation, double expectedGroupsPerPerson,
			double expectedPeoplePerGroup, long seed, Plugin testPlugin) {
		return new Factory(
				new Data(initialPopulation, expectedGroupsPerPerson, expectedPeoplePerGroup, seed, testPlugin));
	}

	public static Factory factory(int initialPopulation, double expectedGroupsPerPerson,
			double expectedPeoplePerGroup, long seed, Consumer<ActorContext> consumer) {

		TestPluginData.Builder pluginBuilder = TestPluginData.builder();
		pluginBuilder.addTestActorPlan("actor", new TestActorPlan(0, consumer));
		TestPluginData testPluginData = pluginBuilder.build();
		Plugin testPlugin = TestPlugin.getTestPlugin(testPluginData);
		return new Factory(
				new Data(initialPopulation, expectedGroupsPerPerson, expectedPeoplePerGroup, seed, testPlugin));
	}

	public static class Factory {
		private Data data;

		private Factory(Data data) {
			this.data = data;
		}

		public List<Plugin> getPlugins() {
			List<Plugin> pluginsToAdd = new ArrayList<>();
			Plugin groupPlugin = GroupsPlugin.getGroupPlugin(this.data.groupsPluginData);

			// add the people plugin
			Plugin peoplePlugin = PeoplePlugin.getPeoplePlugin(this.data.peoplePluginData);

			// add the stochastics plugin
			Plugin stochasticPlugin = StochasticsPlugin.getStochasticsPlugin(this.data.stochasticsPluginData);

			pluginsToAdd.add(groupPlugin);
			pluginsToAdd.add(peoplePlugin);
			pluginsToAdd.add(stochasticPlugin);
			pluginsToAdd.add(this.data.testPlugin);

			return pluginsToAdd;
		}

		public Factory setGroupsPluginData(GroupsPluginData groupsPluginData) {
			this.data.groupsPluginData = groupsPluginData;
			return this;
		}

		public Factory setPeoplePluginData(PeoplePluginData peoplePluginData) {
			this.data.peoplePluginData = peoplePluginData;
			return this;
		}

		public Factory setStochasticsPluginData(StochasticsPluginData stochasticsPluginData) {
			this.data.stochasticsPluginData = stochasticsPluginData;
			return this;
		}

	}

	private GroupsTestPluginFactory() {
	}

	public static GroupsPluginData getStandardGroupsPluginData(int groupCount, int membershipCount,
			List<PersonId> people, RandomGenerator randomGenerator) {
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

	public static PeoplePluginData getStandardPeoplePluginData(int initialPopulation) {
		PeoplePluginData.Builder peopleBuilder = PeoplePluginData.builder();

		for (int i = 0; i < initialPopulation; i++) {
			peopleBuilder.addPersonId(new PersonId(i));
		}
		return peopleBuilder.build();
	}

	public static StochasticsPluginData getStandardStochasticsPluginData(RandomGenerator randomGenerator) {
		return StochasticsPluginData.builder()
				.setSeed(randomGenerator.nextLong()).build();
	}

}
