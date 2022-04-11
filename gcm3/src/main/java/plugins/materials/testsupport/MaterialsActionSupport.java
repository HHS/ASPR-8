package plugins.materials.testsupport;


import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

import org.apache.commons.math3.random.RandomGenerator;

import nucleus.ActorContext;
import nucleus.Experiment;
import nucleus.Plugin;
import nucleus.testsupport.testplugin.ExperimentPlanCompletionObserver;
import nucleus.testsupport.testplugin.TestActorPlan;
import nucleus.testsupport.testplugin.TestError;
import nucleus.testsupport.testplugin.TestPlugin;
import nucleus.testsupport.testplugin.TestPluginData;
import nucleus.util.ContractException;
import plugins.materials.MaterialsPlugin;
import plugins.materials.MaterialsPluginData;
import plugins.partitions.PartitionsPlugin;
import plugins.people.PeoplePlugin;
import plugins.people.PeoplePluginData;
import plugins.regions.RegionPlugin;
import plugins.regions.RegionPluginData;
import plugins.regions.testsupport.TestRegionId;
import plugins.reports.ReportsPlugin;
import plugins.reports.ReportsPluginData;
import plugins.reports.support.ReportItem;
import plugins.reports.testsupport.TestReportItemOutputConsumer;
import plugins.resources.ResourcesPlugin;
import plugins.resources.ResourcesPluginData;
import plugins.resources.testsupport.TestResourceId;
import plugins.resources.testsupport.TestResourcePropertyId;
import plugins.stochastics.StochasticsPlugin;
import plugins.stochastics.StochasticsPluginData;
import plugins.util.properties.PropertyDefinition;
import util.RandomGeneratorProvider;

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
	public static Set<ReportItem> testConsumer(long seed, Consumer<ActorContext> consumer) {
		TestPluginData.Builder pluginBuilder = TestPluginData.builder();
		pluginBuilder.addTestActorPlan("agent", new TestActorPlan(0, consumer));
		TestPluginData testPluginData = pluginBuilder.build();
		Plugin testPlugin = TestPlugin.getTestPlugin(testPluginData);
		return testConsumers(seed, testPlugin,  null);
	}

	

	public static Set<ReportItem> testConsumers(long seed, Plugin testPlugin) {
		return testConsumers(seed, testPlugin,  null);
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
	 * The test plugin is integrated into the simulation run and must contain
	 * at least one action plan. This helps to ensure that a test that does not
	 * run completely does not lead to a false positive test evaluation.
	 * 
	 * @throws ContractException
	 *             <li>{@linkplain TestError#TEST_EXECUTION_FAILURE} if not
	 *             all action plans execute or if there are no action plans
	 *             contained in the action plugin</li>
	 */
	public static Set<ReportItem> testConsumers(long seed, Plugin testPlugin,
			
			Consumer<ActorContext> report) {

		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(seed);

		Experiment.Builder builder = Experiment.builder();

		MaterialsPluginData.Builder materialsBuilder = MaterialsPluginData.builder();

		for (TestMaterialId testMaterialId : TestMaterialId.values()) {
			materialsBuilder.addMaterial(testMaterialId);
		}

		for (TestMaterialsProducerId testMaterialsProducerId : TestMaterialsProducerId.values()) {
			materialsBuilder.addMaterialsProducerId(testMaterialsProducerId);
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
		MaterialsPluginData materialsPluginData = materialsBuilder.build();
		Plugin materialsPlugin = MaterialsPlugin.getMaterialsPlugin(materialsPluginData);
		builder.addPlugin(materialsPlugin);

		// add the resources plugin
		ResourcesPluginData.Builder resourcesBuilder = ResourcesPluginData.builder();

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

		
		ResourcesPluginData resourcesPluginData = resourcesBuilder.build();
		Plugin resourcesPlugin = ResourcesPlugin.getResourcesPlugin(resourcesPluginData);
		builder.addPlugin(resourcesPlugin);

		// add the partitions plugin
		builder.addPlugin(PartitionsPlugin.getPartitionsPlugin());

		// add the people plugin

		PeoplePluginData.Builder peopleBuilder = PeoplePluginData.builder();
		PeoplePluginData peoplePluginData = peopleBuilder.build();
		Plugin peoplePlugin = PeoplePlugin.getPeoplePlugin(peoplePluginData);
		builder.addPlugin(peoplePlugin);
		
		// add the regions plugin
		RegionPluginData.Builder regionsBuilder = RegionPluginData.builder();
		for (TestRegionId testRegionId : TestRegionId.values()) {
			regionsBuilder.addRegion(testRegionId);
		}
		RegionPluginData regionPluginData = regionsBuilder.build();
		Plugin regionPlugin = RegionPlugin.getRegionPlugin(regionPluginData);
		builder.addPlugin(regionPlugin);

		// add the report plugin

		ReportsPluginData.Builder reportsBuilder = ReportsPluginData.builder();
		if (report != null) {
			reportsBuilder.addReport(() -> report);
		}
		ReportsPluginData reportsPluginData = reportsBuilder.build();
		Plugin reportPlugin = ReportsPlugin.getReportPlugin(reportsPluginData);
		builder.addPlugin(reportPlugin);

		StochasticsPluginData stochasticsPluginData = StochasticsPluginData.builder().setSeed(randomGenerator.nextLong()).build();
		Plugin stochasticsPlugin = StochasticsPlugin.getStochasticsPlugin(stochasticsPluginData);
		// add the stochastics plugin
		builder.addPlugin(stochasticsPlugin);

		// add the action plugin
		builder.addPlugin(testPlugin);

		// set the output consumer
		TestReportItemOutputConsumer testReportItemOutputConsumer = new TestReportItemOutputConsumer();
		builder.addOutputHandler(testReportItemOutputConsumer::init);
		ExperimentPlanCompletionObserver experimentPlanCompletionObserver = new ExperimentPlanCompletionObserver();
		builder.addOutputHandler(experimentPlanCompletionObserver::init);
		builder.setExperimentProgressConsole(false);

		// build and execute the engine
		
		builder.build().execute();
		Map<ReportItem, Integer> reportItems = testReportItemOutputConsumer.getReportItems().get(0);
		Set<ReportItem> result = new LinkedHashSet<>();
		if(reportItems != null) {
			result.addAll(reportItems.keySet());
		}

		// show that all actions were executed
		boolean complete = experimentPlanCompletionObserver.getActionCompletionReport(0).get().isComplete();
		if(!complete) {
			throw new ContractException(TestError.TEST_EXECUTION_FAILURE);
		}		
		return result;
	}

}
