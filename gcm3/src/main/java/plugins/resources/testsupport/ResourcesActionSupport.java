package plugins.resources.testsupport;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import org.apache.commons.math3.random.RandomGenerator;

import nucleus.AgentContext;
import nucleus.Engine;
import nucleus.Engine.EngineBuilder;
import nucleus.testsupport.actionplugin.ActionAgent;
import nucleus.testsupport.actionplugin.ActionError;
import nucleus.testsupport.actionplugin.ActionPlugin;
import nucleus.testsupport.actionplugin.AgentActionPlan;
import plugins.compartments.CompartmentPlugin;
import plugins.compartments.initialdata.CompartmentInitialData;
import plugins.compartments.testsupport.TestCompartmentId;
import plugins.components.ComponentPlugin;
import plugins.partitions.PartitionsPlugin;
import plugins.people.PeoplePlugin;
import plugins.people.initialdata.PeopleInitialData;
import plugins.people.support.PersonId;
import plugins.properties.PropertiesPlugin;
import plugins.properties.support.PropertyDefinition;
import plugins.regions.RegionPlugin;
import plugins.regions.initialdata.RegionInitialData;
import plugins.regions.testsupport.TestRegionId;
import plugins.reports.ReportPlugin;
import plugins.reports.initialdata.ReportsInitialData;
import plugins.resources.ResourcesPlugin;
import plugins.resources.initialdata.ResourceInitialData;
import plugins.stochastics.StochasticsPlugin;
import plugins.stochastics.initialdata.StochasticsInitialData;
import util.ContractException;
import util.SeedProvider;

/**
 * A static test support class for the resources plugin. Provides convenience
 * methods for integrating an action plugin into a resource-based simulation
 * test harness.
 * 
 * 
 * @author Shawn Hatch
 *
 */
public class ResourcesActionSupport {

	/**
	 * Creates an action plugin with an agent that will execute the given
	 * consumer at time 0. The action plugin and the remaining arguments are
	 * passed to an invocation of the testConsumers() method.
	 */
	public static void testConsumer(int initialPopulation, long seed, Consumer<AgentContext> consumer) {
		ActionPlugin.Builder pluginBuilder = ActionPlugin.builder();
		pluginBuilder.addAgent("agent");
		pluginBuilder.addAgentActionPlan("agent", new AgentActionPlan(0, consumer));
		testConsumers(initialPopulation, seed, pluginBuilder.build());
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
	public static void testConsumers(int initialPopulation, long seed, ActionPlugin actionPlugin) {
		
		RandomGenerator randomGenerator = SeedProvider.getRandomGenerator(seed);

		// create a list of people
		List<PersonId> people = new ArrayList<>();
		for (int i = 0; i < initialPopulation; i++) {
			people.add(new PersonId(i));
		}

		EngineBuilder engineBuilder = Engine.builder();

		// add the resources plugin
		ResourceInitialData.Builder resourcesBuilder = ResourceInitialData.builder(); 
		
		
		for (TestResourceId testResourceId : TestResourceId.values()) {
			resourcesBuilder.addResource(testResourceId);
			resourcesBuilder.setResourceTimeTracking(testResourceId, testResourceId.getTimeTrackingPolicy());			
		}
		
		for (TestResourcePropertyId testResourcePropertyId : TestResourcePropertyId.values()) {
			TestResourceId testResourceId = testResourcePropertyId.getTestResourceId();
			PropertyDefinition propertyDefinition = testResourcePropertyId.getPropertyDefinition();
			Object propertyValue = testResourcePropertyId.getRandomPropertyValue(randomGenerator);
			resourcesBuilder.defineResourceProperty(testResourceId, testResourcePropertyId, propertyDefinition);
			resourcesBuilder.setResourcePropertyValue(testResourceId, testResourcePropertyId, propertyValue);
		}
		
		engineBuilder.addPlugin(ResourcesPlugin.PLUGIN_ID, new ResourcesPlugin(resourcesBuilder.build())::init);
		

		// add the partitions plugin
		engineBuilder.addPlugin(PartitionsPlugin.PLUGIN_ID, new PartitionsPlugin()::init);

		// add the people plugin

		PeopleInitialData.Builder peopleBuilder = PeopleInitialData.builder();

		for (PersonId personId : people) {
			peopleBuilder.addPersonId(personId);
		}

		engineBuilder.addPlugin(PeoplePlugin.PLUGIN_ID, new PeoplePlugin(peopleBuilder.build())::init);

		// add the properties plugin
		engineBuilder.addPlugin(PropertiesPlugin.PLUGIN_ID, new PropertiesPlugin()::init);

		// add the compartments plugin
		CompartmentInitialData.Builder compartmentsBuilder = CompartmentInitialData.builder();
		for (TestCompartmentId testCompartmentId : TestCompartmentId.values()) {
			compartmentsBuilder.setCompartmentInitialBehaviorSupplier(testCompartmentId, () -> new ActionAgent(testCompartmentId)::init);
		}
		
		for (PersonId personId : people) {
			compartmentsBuilder.setPersonCompartment(personId, TestCompartmentId.getRandomCompartmentId(randomGenerator));
		}
		
		engineBuilder.addPlugin(CompartmentPlugin.PLUGIN_ID, new CompartmentPlugin(compartmentsBuilder.build())::init);

		// add the regions plugin
		RegionInitialData.Builder regionsBuilder = RegionInitialData.builder();
		for (TestRegionId testRegionId : TestRegionId.values()) {
			regionsBuilder.setRegionComponentInitialBehaviorSupplier(testRegionId, () -> new ActionAgent(testRegionId)::init);
		}
		for (PersonId personId : people) {
			regionsBuilder.setPersonRegion(personId, TestRegionId.getRandomRegionId(randomGenerator));
		}

		engineBuilder.addPlugin(RegionPlugin.PLUGIN_ID, new RegionPlugin(regionsBuilder.build())::init);

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
