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
import nucleus.testsupport.testplugin.TestSimulation;
import plugins.groups.GroupsPlugin;
import plugins.groups.GroupsPluginData;
import plugins.groups.support.GroupId;
import plugins.people.PeoplePlugin;
import plugins.people.PeoplePluginData;
import plugins.people.support.PersonId;
import plugins.stochastics.StochasticsPlugin;
import plugins.stochastics.StochasticsPluginData;
import util.errors.ContractException;
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

			List<PersonId> people = new ArrayList<>();
			for (int i = 0; i < initialPopulation; i++) {
				people.add(new PersonId(i));
			}

			this.groupsPluginData = GroupsTestPluginFactory.getStandardGroupsPluginData(groupCount, membershipCount, people, randomGenerator);
			this.peoplePluginData = GroupsTestPluginFactory.getStandardPeoplePluginData(initialPopulation, people);
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

	/**
	 * Creates an action plugin with an agent that will execute the given
	 * consumer at time 0. The action plugin and the remaining arguments are
	 * passed to an invocation of the testConsumers() method.
	 */
	public static void testConsumer(int initialPopulation, double expectedGroupsPerPerson,
			double expectedPeoplePerGroup, long seed, Consumer<ActorContext> consumer) {
		TestPluginData.Builder pluginBuilder = TestPluginData.builder();
		pluginBuilder.addTestActorPlan("actor", new TestActorPlan(0, consumer));
		TestPluginData testPluginData = pluginBuilder.build();
		Plugin testPlugin = TestPlugin.getTestPlugin(testPluginData);
		testConsumers(initialPopulation, expectedGroupsPerPerson, expectedPeoplePerGroup, seed, testPlugin);
	}

	/**
	 * Executes a simulation instance that supports group plugin testing.
	 * 
	 * The initial population is added in the initial data. The expected groups
	 * per person and expected people per group are used to determine the number
	 * of groups from the number of initial people. People are randomly
	 * allocated to these groups in a way that approximates the expected rates.
	 * Groups are allocated uniformly to the TestGroupId enumeration members.
	 * 
	 * The seed is used to produce randomized initial group types and group
	 * memberships.
	 * 
	 * The action plugin is integrated into the simulation run and must contain
	 * at least one action plan. This helps to ensure that a test that does not
	 * run completely does not lead to a false positive test evaluation.
	 * 
	 * @throws ContractException
	 *                           <li>{@linkplain ActionError#ACTION_EXECUTION_FAILURE}
	 *                           if not
	 *                           all action plans execute or if there are no action
	 *                           plans
	 *                           contained in the action plugin</li>
	 */
	public static void testConsumers(int initialPopulation, double expectedGroupsPerPerson,
			double expectedPeoplePerGroup, long seed, Plugin testPlugin) {
		List<Plugin> pluginsToAdd = getStandardPlugins(initialPopulation, expectedGroupsPerPerson,
				expectedPeoplePerGroup,
				seed, testPlugin);
		pluginsToAdd.add(testPlugin);

		TestSimulation.executeSimulation(pluginsToAdd);
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

	public static PeoplePluginData getStandardPeoplePluginData(int initialPopulation, List<PersonId> people) {
		PeoplePluginData.Builder peopleBuilder = PeoplePluginData.builder();
		for (PersonId personId : people) {
			peopleBuilder.addPersonId(personId);
		}
		return peopleBuilder.build();
	}

	public static StochasticsPluginData getStandardStochasticsPluginData(RandomGenerator randomGenerator) {
		return StochasticsPluginData.builder()
				.setSeed(randomGenerator.nextLong()).build();
	}

	private static List<Plugin> _getStandardPlugins(int initialPopulation, double expectedGroupsPerPerson,
			double expectedPeoplePerGroup, long seed) {

		List<Plugin> pluginsToAdd = new ArrayList<>();
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(seed);

		// create a list of people
		List<PersonId> people = new ArrayList<>();
		for (int i = 0; i < initialPopulation; i++) {
			people.add(new PersonId(i));
		}

		int membershipCount = (int) FastMath.round(initialPopulation * expectedGroupsPerPerson);
		int groupCount = (int) FastMath.round(membershipCount / expectedPeoplePerGroup);

		Plugin groupPlugin = GroupsPlugin
				.getGroupPlugin(getStandardGroupsPluginData(groupCount, membershipCount, people, randomGenerator));

		// add the people plugin
		Plugin peoplePlugin = PeoplePlugin.getPeoplePlugin(getStandardPeoplePluginData(initialPopulation, people));

		// add the stochastics plugin
		Plugin stochasticPlugin = StochasticsPlugin
				.getStochasticsPlugin(getStandardStochasticsPluginData(randomGenerator));

		pluginsToAdd.add(groupPlugin);
		pluginsToAdd.add(peoplePlugin);
		pluginsToAdd.add(stochasticPlugin);

		return pluginsToAdd;
	}

	public static List<Plugin> getStandardPlugins(int initialPopulation, double expectedGroupsPerPerson,
			double expectedPeoplePerGroup, long seed, Plugin testPlugin) {
		List<Plugin> plugins = _getStandardPlugins(initialPopulation, expectedGroupsPerPerson, expectedPeoplePerGroup,
				seed);
		plugins.add(testPlugin);

		return plugins;
	}

	public static List<Plugin> getStandardPlugins(int initialPopulation, double expectedGroupsPerPerson,
			double expectedPeoplePerGroup, long seed, Consumer<ActorContext> consumer) {
		List<Plugin> plugins = _getStandardPlugins(initialPopulation, expectedGroupsPerPerson, expectedPeoplePerGroup,
				seed);

		TestPluginData.Builder pluginBuilder = TestPluginData.builder();
		pluginBuilder.addTestActorPlan("actor", new TestActorPlan(0, consumer));
		TestPluginData testPluginData = pluginBuilder.build();
		Plugin testPlugin = TestPlugin.getTestPlugin(testPluginData);

		plugins.add(testPlugin);

		return plugins;
	}
}
