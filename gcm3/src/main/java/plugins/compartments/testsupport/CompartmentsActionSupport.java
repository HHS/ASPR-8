package plugins.compartments.testsupport;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import org.apache.commons.math3.random.RandomGenerator;

import nucleus.AgentContext;
import nucleus.Simulation;
import nucleus.testsupport.actionplugin.ActionAgent;
import nucleus.testsupport.actionplugin.ActionError;
import nucleus.testsupport.actionplugin.ActionPlugin;
import nucleus.testsupport.actionplugin.AgentActionPlan;
import plugins.compartments.CompartmentPlugin;
import plugins.compartments.initialdata.CompartmentInitialData;
import plugins.compartments.support.CompartmentId;
import plugins.components.ComponentPlugin;
import plugins.partitions.PartitionsPlugin;
import plugins.people.PeoplePlugin;
import plugins.people.initialdata.PeopleInitialData;
import plugins.people.support.PersonId;
import plugins.properties.PropertiesPlugin;
import plugins.properties.support.TimeTrackingPolicy;
import plugins.reports.ReportPlugin;
import plugins.reports.initialdata.ReportsInitialData;
import plugins.stochastics.StochasticsPlugin;
import util.ContractException;
import util.MutableDouble;
import util.SeedProvider;

public class CompartmentsActionSupport {
	/**
	 * Creates an action plugin with an agent that will execute the given
	 * consumer at time 0. The action plugin and the remaining arguments are
	 * passed to an invocation of the testConsumers() method.
	 */
	public static void testConsumer(int initialPopulation, long seed,TimeTrackingPolicy timeTrackingPolicy, Consumer<AgentContext> consumer) {
		ActionPlugin.Builder pluginBuilder = ActionPlugin.builder();
		pluginBuilder.addAgent("agent");
		pluginBuilder.addAgentActionPlan("agent", new AgentActionPlan(0, consumer));
		testConsumers(initialPopulation, seed,timeTrackingPolicy, pluginBuilder.build());
	}

	/**
	 * Executes a simulation instance that supports resources plugin testing.
	 * 
	 * The initial population is added in the initial data.
	 * 
	 * Resources and their property definitions and initial values are added. No
	 * resources are allocated to regions or people.
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
	public static void testConsumers(int initialPopulation, long seed,TimeTrackingPolicy timeTrackingPolicy, ActionPlugin actionPlugin) {
		RandomGenerator randomGenerator = SeedProvider.getRandomGenerator(seed);
		// create a list of people
		List<PersonId> people = new ArrayList<>();
		for (int i = 0; i < initialPopulation; i++) {
			people.add(new PersonId(i));
		}

		Simulation.Builder builder = Simulation.builder();

		// add the compartments plugin
		CompartmentInitialData.Builder compartmentsBuilder = CompartmentInitialData.builder();

		for (TestCompartmentId testCompartmentId : TestCompartmentId.values()) {
			compartmentsBuilder.setCompartmentInitialBehaviorSupplier(testCompartmentId, () -> new ActionAgent(testCompartmentId)::init);
		}

		for (PersonId personId : people) {
			compartmentsBuilder.setPersonCompartment(personId, TestCompartmentId.getRandomCompartmentId(randomGenerator));
		}

		for (TestCompartmentPropertyId testCompartmentPropertyId : TestCompartmentPropertyId.values()) {
			compartmentsBuilder.defineCompartmentProperty(testCompartmentPropertyId.getTestCompartmentId(), testCompartmentPropertyId, testCompartmentPropertyId.getPropertyDefinition());
		}
		compartmentsBuilder.setPersonCompartmentArrivalTracking(timeTrackingPolicy);

		builder.addPlugin(CompartmentPlugin.PLUGIN_ID, new CompartmentPlugin(compartmentsBuilder.build())::init);

		// add the people plugin
		PeopleInitialData.Builder peopleBuilder = PeopleInitialData.builder();
		for (PersonId personId : people) {
			peopleBuilder.addPersonId(personId);
		}

		builder.addPlugin(PeoplePlugin.PLUGIN_ID, new PeoplePlugin(peopleBuilder.build())::init);
		builder.addPlugin(StochasticsPlugin.PLUGIN_ID, StochasticsPlugin.builder().setSeed(randomGenerator.nextLong()).build()::init);
		builder.addPlugin(ReportPlugin.PLUGIN_ID, new ReportPlugin(ReportsInitialData.builder().build())::init);
		builder.addPlugin(PropertiesPlugin.PLUGIN_ID, new PropertiesPlugin()::init);
		builder.addPlugin(ComponentPlugin.PLUGIN_ID, new ComponentPlugin()::init);
		builder.addPlugin(PartitionsPlugin.PLUGIN_ID, new PartitionsPlugin()::init);

		Map<CompartmentId, MutableDouble> expectedAssignmentTimes = new LinkedHashMap<>();
		for (TestCompartmentId testCompartmentId : TestCompartmentId.values()) {
			expectedAssignmentTimes.put(testCompartmentId, new MutableDouble());
		}

		// build and add the action plugin
		builder.addPlugin(ActionPlugin.PLUGIN_ID, actionPlugin::init);

		// build and execute the engine
		builder.build().execute();

		// show that all actions were executed
		if (!actionPlugin.allActionsExecuted()) {
			throw new ContractException(ActionError.ACTION_EXECUTION_FAILURE);
		}

	}

}
