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
import nucleus.Simulation;
import nucleus.Simulation.Builder;
import nucleus.testsupport.testplugin.TestActorPlan;
import nucleus.testsupport.testplugin.TestError;
import nucleus.testsupport.testplugin.TestPlugin;
import nucleus.testsupport.testplugin.TestPluginData;
import nucleus.testsupport.testplugin.TestSimulationOutputConsumer;
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
public class GroupsActionSupport {

	private GroupsActionSupport() {
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

		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(seed);

		Builder builder = Simulation.builder();

		for (Plugin plugin : setUpPluginsForTest(initialPopulation, expectedGroupsPerPerson, expectedPeoplePerGroup,
				seed)) {
			builder.addPlugin(plugin);
		}

		// add the stochastics plugin
		StochasticsPluginData stochasticsPluginData = StochasticsPluginData.builder()
				.setSeed(randomGenerator.nextLong()).build();
		Plugin stochasticPlugin = StochasticsPlugin.getStochasticsPlugin(stochasticsPluginData);
		builder.addPlugin(stochasticPlugin);

		// build and execute the engine
		TestSimulationOutputConsumer outputConsumer = new TestSimulationOutputConsumer();
		builder.setOutputConsumer(outputConsumer)
				.addPlugin(testPlugin)
				.build()
				.execute();

		// show that all actions were executed
		if (!outputConsumer.isComplete()) {
			throw new ContractException(TestError.TEST_EXECUTION_FAILURE);
		}
	}

	public static List<Plugin> setUpPluginsForTest(int initialPopulation, double expectedGroupsPerPerson,
			double expectedPeoplePerGroup, long seed) {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(seed);

		// create a list of people
		List<PersonId> people = new ArrayList<>();
		for (int i = 0; i < initialPopulation; i++) {
			people.add(new PersonId(i));
		}

		int membershipCount = (int) FastMath.round(initialPopulation * expectedGroupsPerPerson);
		int groupCount = (int) FastMath.round(membershipCount / expectedPeoplePerGroup);

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
			groupBuilder.addGroup(groupId, TestGroupTypeId.getRandomGroupTypeId(randomGenerator));
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
		GroupsPluginData groupsPluginData = groupBuilder.build();
		Plugin groupPlugin = GroupsPlugin.getGroupPlugin(groupsPluginData);

		// add the people plugin
		PeoplePluginData.Builder peopleBuilder = PeoplePluginData.builder();
		for (PersonId personId : people) {
			peopleBuilder.addPersonId(personId);
		}
		PeoplePluginData peoplePluginData = peopleBuilder.build();
		Plugin peoplePlugin = PeoplePlugin.getPeoplePlugin(peoplePluginData);

		return setUpPluginsForTest(groupPlugin, peoplePlugin);
	}

	public static List<Plugin> setUpPluginsForTest(Plugin groupsPlugin, Plugin peoplePlugin) {
		List<Plugin> pluginsToAdd = new ArrayList<>();

		pluginsToAdd.add(groupsPlugin);
		pluginsToAdd.add(peoplePlugin);

		return pluginsToAdd;
	}

}
