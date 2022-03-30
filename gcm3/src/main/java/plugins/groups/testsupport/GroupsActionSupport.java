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
import nucleus.testsupport.testplugin.ScenarioPlanCompletionObserver;
import nucleus.testsupport.testplugin.TestActorPlan;
import nucleus.testsupport.testplugin.TestError;
import nucleus.testsupport.testplugin.TestPlugin;
import nucleus.testsupport.testplugin.TestPluginData;
import nucleus.util.ContractException;
import plugins.groups.GroupPlugin;
import plugins.groups.GroupPluginData;
import plugins.groups.support.GroupId;
import plugins.partitions.PartitionsPlugin;
import plugins.people.PeoplePlugin;
import plugins.people.PeoplePluginData;
import plugins.people.support.PersonId;
import plugins.reports.ReportsPlugin;
import plugins.reports.ReportsPluginData;
import plugins.stochastics.StochasticsPlugin;
import plugins.stochastics.StochasticsPluginData;
import util.MultiKey;
import util.RandomGeneratorProvider;

/**
 * A static test support class for the groups plugin. Provides convenience
 * methods for integrating an action plugin into a groups-based simulation test
 * harness.
 * 
 * 
 * @author Shawn Hatch
 *
 */
public class GroupsActionSupport {

	/**
	 * Creates an action plugin with an agent that will execute the given
	 * consumer at time 0. The action plugin and the remaining arguments are
	 * passed to an invocation of the testConsumers() method.
	 */
	public static void testConsumer(int initialPopulation, double expectedGroupsPerPerson, double expectedPeoplePerGroup, long seed, Consumer<ActorContext> consumer) {
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
	 *             <li>{@linkplain ActionError#ACTION_EXECUTION_FAILURE} if not
	 *             all action plans execute or if there are no action plans
	 *             contained in the action plugin</li>
	 */
	public static void testConsumers(int initialPopulation, double expectedGroupsPerPerson, double expectedPeoplePerGroup, long seed, Plugin testPlugin) {

		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(seed);

		// create a list of people
		List<PersonId> people = new ArrayList<>();
		for (int i = 0; i < initialPopulation; i++) {
			people.add(new PersonId(i));
		}

		int membershipCount = (int) FastMath.round(initialPopulation * expectedGroupsPerPerson);
		int groupCount = (int) FastMath.round(membershipCount / expectedPeoplePerGroup);

		Builder builder = Simulation.builder();

		// add the group plugin
		GroupPluginData.Builder groupBuilder = GroupPluginData.builder();
		// add group types
		for (TestGroupTypeId testGroupTypeId : TestGroupTypeId.values()) {
			groupBuilder.addGroupTypeId(testGroupTypeId);
		}
		// define group properties
		for (TestGroupPropertyId testGroupPropertyId : TestGroupPropertyId.values()) {
			groupBuilder.defineGroupProperty(testGroupPropertyId.getTestGroupTypeId(), testGroupPropertyId, testGroupPropertyId.getPropertyDefinition());
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
		GroupPluginData groupPluginData = groupBuilder.build();
		Plugin groupPlugin = GroupPlugin.getGroupPlugin(groupPluginData);
		builder.addPlugin(groupPlugin);

		// add the partitions plugin
		builder.addPlugin(PartitionsPlugin.getPartitionsPlugin());
		
		// add the people plugin
		PeoplePluginData.Builder peopleBuilder = PeoplePluginData.builder();
		for (PersonId personId : people) {
			peopleBuilder.addPersonId(personId);			
		}
		PeoplePluginData peoplePluginData = peopleBuilder.build();
		Plugin peoplePlugin = PeoplePlugin.getPeoplePlugin(peoplePluginData);
		builder.addPlugin(peoplePlugin);

		// add the report plugin
		Plugin reportPlugin = ReportsPlugin.getReportPlugin(ReportsPluginData.builder().build());
		builder.addPlugin(reportPlugin);


		// add the stochastics plugin
		StochasticsPluginData stochasticsPluginData = StochasticsPluginData.builder().setSeed(randomGenerator.nextLong()).build();
		Plugin stochasticPlugin = StochasticsPlugin.getStochasticsPlugin(stochasticsPluginData);
		builder.addPlugin(stochasticPlugin);

		// add the action plugin
		builder.addPlugin(testPlugin);

		// build and execute the engine
		ScenarioPlanCompletionObserver scenarioPlanCompletionObserver = new ScenarioPlanCompletionObserver();
		builder.setOutputConsumer(scenarioPlanCompletionObserver::handleOutput).build().execute();

		// show that all actions were executed
		if (!scenarioPlanCompletionObserver.allPlansExecuted()) {
			throw new ContractException(TestError.TEST_EXECUTION_FAILURE);
		}
	}

}
