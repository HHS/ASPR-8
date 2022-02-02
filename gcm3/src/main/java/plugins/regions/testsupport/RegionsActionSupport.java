package plugins.regions.testsupport;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import nucleus.AgentContext;
import nucleus.Simulation;
import nucleus.Simulation.Builder;
import nucleus.testsupport.actionplugin.ActionAgent;
import nucleus.testsupport.actionplugin.ActionError;
import nucleus.testsupport.actionplugin.ActionPlugin;
import nucleus.testsupport.actionplugin.AgentActionPlan;
import plugins.components.ComponentPlugin;
import plugins.partitions.PartitionsPlugin;
import plugins.people.PeoplePlugin;
import plugins.people.initialdata.PeopleInitialData;
import plugins.people.support.PersonId;
import plugins.properties.PropertiesPlugin;
import plugins.properties.support.TimeTrackingPolicy;
import plugins.regions.RegionPlugin;
import plugins.regions.initialdata.RegionInitialData;
import plugins.reports.ReportPlugin;
import plugins.reports.initialdata.ReportsInitialData;
import plugins.stochastics.StochasticsPlugin;
import util.ContractException;

public final class RegionsActionSupport {
	public static void testConsumer(int initialPopulation, long seed, TimeTrackingPolicy timeTrackingPolicy, Consumer<AgentContext> consumer) {
		ActionPlugin.Builder pluginBuilder = ActionPlugin.builder();
		pluginBuilder.addAgent("agent");
		pluginBuilder.addAgentActionPlan("agent", new AgentActionPlan(0, consumer));
		testConsumers(initialPopulation, seed, timeTrackingPolicy, pluginBuilder.build());
	}

	public static void testConsumers(int initialPopulation, long seed,TimeTrackingPolicy timeTrackingPolicy, ActionPlugin actionPlugin) {
		List<PersonId> people = new ArrayList<>();

		for (int i = 0; i < initialPopulation; i++) {
			people.add(new PersonId(i));
		}

		Builder builder = Simulation.builder();

		// add the region plugin
		RegionInitialData.Builder regionBuilder = RegionInitialData.builder();

		// assign people to regions
		TestRegionId testRegionId = TestRegionId.REGION_1;
		for (PersonId personId : people) {
			regionBuilder.setPersonRegion(personId, testRegionId.next());
		}

		for (TestRegionId regionId : TestRegionId.values()) {
			regionBuilder.setRegionComponentInitialBehaviorSupplier(regionId, () -> new ActionAgent(regionId)::init);
		}
		regionBuilder.setPersonRegionArrivalTracking(timeTrackingPolicy);
		builder.addPlugin(RegionPlugin.PLUGIN_ID, new RegionPlugin(regionBuilder.build())::init);
		
		

		// add the people plugin

		PeopleInitialData.Builder peopleBuilder = PeopleInitialData.builder();
		for (PersonId personId : people) {
			peopleBuilder.addPersonId(personId);
		}

		builder.addPlugin(PeoplePlugin.PLUGIN_ID, new PeoplePlugin(peopleBuilder.build())::init);

		// add the properties plugin
		builder.addPlugin(PropertiesPlugin.PLUGIN_ID, new PropertiesPlugin()::init);

		// add the component plugin
		builder.addPlugin(ComponentPlugin.PLUGIN_ID, new ComponentPlugin()::init);

		// add the report plugin
		builder.addPlugin(ReportPlugin.PLUGIN_ID, new ReportPlugin(ReportsInitialData.builder().build())::init);

		// add the stochastics plugin
		builder.addPlugin(StochasticsPlugin.PLUGIN_ID, StochasticsPlugin.builder().setSeed(seed).build()::init);

		// add the partitions plugin
		builder.addPlugin(PartitionsPlugin.PLUGIN_ID, new PartitionsPlugin()::init);

		// add the action plugin
		builder.addPlugin(ActionPlugin.PLUGIN_ID, actionPlugin::init);

		// build and execute the engine
		builder.build().execute();

		// show that all actions were executed
		if (!actionPlugin.allActionsExecuted()) {
			throw new ContractException(ActionError.ACTION_EXECUTION_FAILURE);
		}

	}

}
