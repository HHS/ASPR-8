package plugins.materials.testsupport;

import java.util.Set;
import java.util.function.Consumer;

import org.apache.commons.math3.random.RandomGenerator;

import nucleus.AgentContext;
import nucleus.ReportContext;
import nucleus.SimpleReportId;
import nucleus.Simulation;
import nucleus.Simulation.Builder;
import nucleus.testsupport.actionplugin.ActionAgent;
import nucleus.testsupport.actionplugin.ActionError;
import nucleus.testsupport.actionplugin.ActionPluginInitializer;
import nucleus.testsupport.actionplugin.AgentActionPlan;
import plugins.compartments.CompartmentPlugin;
import plugins.compartments.initialdata.CompartmentInitialData;
import plugins.compartments.testsupport.TestCompartmentId;
import plugins.components.ComponentPlugin;
import plugins.materials.MaterialsPlugin;
import plugins.materials.initialdata.MaterialsInitialData;
import plugins.partitions.PartitionsPlugin;
import plugins.people.PeoplePlugin;
import plugins.people.initialdata.PeopleInitialData;
import plugins.properties.PropertiesPlugin;
import plugins.properties.support.PropertyDefinition;
import plugins.regions.RegionPlugin;
import plugins.regions.initialdata.RegionInitialData;
import plugins.regions.testsupport.TestRegionId;
import plugins.reports.ReportPlugin;
import plugins.reports.initialdata.ReportsInitialData;
import plugins.resources.ResourcesPlugin;
import plugins.resources.initialdata.ResourceInitialData;
import plugins.resources.testsupport.TestResourceId;
import plugins.resources.testsupport.TestResourcePropertyId;
import plugins.stochastics.StochasticsPlugin;
import util.ContractException;
import util.SeedProvider;

/**
 * A static test support class for the materials plugin. Provides convenience
 * methods for integrating an action plugin into a materials-based simulation
 * test harness.
 * 
 * 
 * @author Shawn Hatch
 *
 */
public class MaterialsActionSupport {

	/**
	 * Creates an action plugin with an agent that will execute the given
	 * consumer at time 0. The action plugin and the remaining arguments are
	 * passed to an invocation of the testConsumers() method.
	 */
	public static void testConsumer(long seed, Consumer<AgentContext> consumer) {
		ActionPluginInitializer.Builder pluginBuilder = ActionPluginInitializer.builder();
		pluginBuilder.addAgent("agent");
		pluginBuilder.addAgentActionPlan("agent", new AgentActionPlan(0, consumer));
		testConsumers(seed, pluginBuilder.build());
	}

	/**
	 * Executes a simulation instance that supports materials plugin testing.
	 * 
	 * The initial population is added in the initial data.
	 * 
	 * Materials, Materials Producers and their associated properties are added.
	 * No batches or stages are created. Materials producer resource levels are
	 * zero.
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
	public static void testConsumers(long seed, ActionPluginInitializer actionPluginInitializer, Consumer<Object> outputConsumer, Consumer<ReportContext> report) {
		_testConsumers(seed, actionPluginInitializer, outputConsumer, report);
	}

	public static void testConsumers(long seed, ActionPluginInitializer actionPluginInitializer) {
		_testConsumers(seed, actionPluginInitializer, null, null);
	}

	private static void _testConsumers(long seed, ActionPluginInitializer actionPluginInitializer, Consumer<Object> outputConsumer, Consumer<ReportContext> report) {

		RandomGenerator randomGenerator = SeedProvider.getRandomGenerator(seed);

		Builder builder = Simulation.builder();

		MaterialsInitialData.Builder materialsBuilder = MaterialsInitialData.builder();

		for (TestMaterialId testMaterialId : TestMaterialId.values()) {
			materialsBuilder.addMaterial(testMaterialId);
		}

		for (TestMaterialsProducerId testMaterialsProducerId : TestMaterialsProducerId.values()) {
			materialsBuilder.addMaterialsProducerId(testMaterialsProducerId, () -> new ActionAgent(testMaterialsProducerId)::init);
		}

		for (TestMaterialsProducerPropertyId testMaterialsProducerPropertyId : TestMaterialsProducerPropertyId.values()) {
			materialsBuilder.defineMaterialsProducerProperty(testMaterialsProducerPropertyId, testMaterialsProducerPropertyId.getPropertyDefinition());
		}

		for (TestMaterialId testMaterialId : TestMaterialId.values()) {
			Set<TestBatchPropertyId> testBatchPropertyIds = TestBatchPropertyId.getTestBatchPropertyIds(testMaterialId);
			for (TestBatchPropertyId testBatchPropertyId : testBatchPropertyIds) {
				materialsBuilder.defineBatchProperty(testMaterialId, testBatchPropertyId, testBatchPropertyId.getPropertyDefinition());
			}
		}

		builder.addPlugin(MaterialsPlugin.PLUGIN_ID, new MaterialsPlugin(materialsBuilder.build())::init);

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

		builder.addPlugin(ResourcesPlugin.PLUGIN_ID, new ResourcesPlugin(resourcesBuilder.build())::init);

		// add the partitions plugin
		builder.addPlugin(PartitionsPlugin.PLUGIN_ID, new PartitionsPlugin()::init);

		// add the people plugin

		PeopleInitialData.Builder peopleBuilder = PeopleInitialData.builder();
		builder.addPlugin(PeoplePlugin.PLUGIN_ID, new PeoplePlugin(peopleBuilder.build())::init);

		// add the properties plugin
		builder.addPlugin(PropertiesPlugin.PLUGIN_ID, new PropertiesPlugin()::init);

		// add the compartments plugin
		CompartmentInitialData.Builder compartmentsBuilder = CompartmentInitialData.builder();
		for (TestCompartmentId testCompartmentId : TestCompartmentId.values()) {
			compartmentsBuilder.setCompartmentInitialBehaviorSupplier(testCompartmentId, () -> new ActionAgent(testCompartmentId)::init);
		}

		builder.addPlugin(CompartmentPlugin.PLUGIN_ID, new CompartmentPlugin(compartmentsBuilder.build())::init);

		// add the regions plugin
		RegionInitialData.Builder regionsBuilder = RegionInitialData.builder();
		for (TestRegionId testRegionId : TestRegionId.values()) {
			regionsBuilder.setRegionComponentInitialBehaviorSupplier(testRegionId, () -> new ActionAgent(testRegionId)::init);
		}

		builder.addPlugin(RegionPlugin.PLUGIN_ID, new RegionPlugin(regionsBuilder.build())::init);

		// add the report plugin

		ReportsInitialData.Builder reportsBuilder = ReportsInitialData.builder();
		if (report != null) {
			reportsBuilder.addReport(new SimpleReportId("report"), () -> report);
		}

		builder.addPlugin(ReportPlugin.PLUGIN_ID, new ReportPlugin(reportsBuilder.build())::init);

		// add the component plugin
		builder.addPlugin(ComponentPlugin.PLUGIN_ID, new ComponentPlugin()::init);

		// add the stochastics plugin
		builder.addPlugin(StochasticsPlugin.PLUGIN_ID, StochasticsPlugin.builder().setSeed(randomGenerator.nextLong()).build()::init);

		// add the action plugin
		builder.addPlugin(ActionPluginInitializer.PLUGIN_ID, actionPluginInitializer::init);

		// set the output consumer
		builder.setOutputConsumer(outputConsumer);

		// build and execute the engine
		builder.build().execute();

		// show that all actions were executed
		if (!actionPluginInitializer.allActionsExecuted()) {
			throw new ContractException(ActionError.ACTION_EXECUTION_FAILURE);
		}
	}

}
