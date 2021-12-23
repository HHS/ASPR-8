package plugins.groups.testsupport;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

import org.apache.commons.math3.random.RandomGenerator;
import org.apache.commons.math3.util.FastMath;

import nucleus.AgentContext;
import nucleus.Engine;
import nucleus.Engine.EngineBuilder;
import nucleus.testsupport.actionplugin.ActionError;
import nucleus.testsupport.actionplugin.ActionPlugin;
import nucleus.testsupport.actionplugin.AgentActionPlan;
import plugins.components.ComponentPlugin;
import plugins.groups.GroupPlugin;
import plugins.groups.initialdata.GroupInitialData;
import plugins.groups.support.GroupId;
import plugins.partitions.PartitionsPlugin;
import plugins.people.PeoplePlugin;
import plugins.people.initialdata.PeopleInitialData;
import plugins.people.support.PersonId;
import plugins.properties.PropertiesPlugin;
import plugins.reports.ReportPlugin;
import plugins.reports.initialdata.ReportsInitialData;
import plugins.stochastics.StochasticsPlugin;
import plugins.stochastics.initialdata.StochasticsInitialData;
import util.ContractException;
import util.MultiKey;
import util.SeedProvider;

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
	public static void testConsumer(int initialPopulation, double expectedGroupsPerPerson, double expectedPeoplePerGroup, long seed, Consumer<AgentContext> consumer) {
		ActionPlugin.Builder pluginBuilder = ActionPlugin.builder();
		pluginBuilder.addAgent("agent");
		pluginBuilder.addAgentActionPlan("agent", new AgentActionPlan(0, consumer));
		testConsumers(initialPopulation, expectedGroupsPerPerson, expectedPeoplePerGroup, seed, pluginBuilder.build());
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
	public static void testConsumers(int initialPopulation, double expectedGroupsPerPerson, double expectedPeoplePerGroup, long seed, ActionPlugin actionPlugin) {

		RandomGenerator randomGenerator = SeedProvider.getRandomGenerator(seed);

		// create a list of people
		List<PersonId> people = new ArrayList<>();
		for (int i = 0; i < initialPopulation; i++) {
			people.add(new PersonId(i));
		}

		int membershipCount = (int) FastMath.round(initialPopulation * expectedGroupsPerPerson);
		int groupCount = (int) FastMath.round(membershipCount / expectedPeoplePerGroup);

		EngineBuilder engineBuilder = Engine.builder();

		// add the group plugin
		GroupInitialData.Builder groupBuilder = GroupInitialData.builder();
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

		engineBuilder.addPlugin(GroupPlugin.PLUGIN_ID, new GroupPlugin(groupBuilder.build())::init);

		// add the people plugin
		engineBuilder.addPlugin(PartitionsPlugin.PLUGIN_ID, new PartitionsPlugin()::init);
		PeopleInitialData.Builder peopleBuilder = PeopleInitialData.builder();

		for (PersonId personId : people) {
			peopleBuilder.addPersonId(personId);
		}

		engineBuilder.addPlugin(PeoplePlugin.PLUGIN_ID, new PeoplePlugin(peopleBuilder.build())::init);

		// add the properties plugin
		engineBuilder.addPlugin(PropertiesPlugin.PLUGIN_ID, new PropertiesPlugin()::init);

		// add the report plugin
		engineBuilder.addPlugin(ReportPlugin.PLUGIN_ID, new ReportPlugin(ReportsInitialData.builder().build())::init);

		// add the component plugin
		engineBuilder.addPlugin(ComponentPlugin.PLUGIN_ID, new ComponentPlugin()::init);

		// add the stochastics plugin
		engineBuilder.addPlugin(StochasticsPlugin.PLUGIN_ID, new StochasticsPlugin(StochasticsInitialData.builder().setSeed(randomGenerator.nextLong()).build())::init);

		// add the action plugin
		engineBuilder.addPlugin(ActionPlugin.PLUGIN_ID, actionPlugin::init);

		// build and execute the engine
		engineBuilder.build().execute();

		// show that all actions were executed
		if (!actionPlugin.allActionsExecuted()) {
			throw new ContractException(ActionError.ACTION_EXECUTION_FAILURE);
		}
	}

}
