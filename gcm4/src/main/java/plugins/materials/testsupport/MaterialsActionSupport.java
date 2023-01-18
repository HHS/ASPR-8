package plugins.materials.testsupport;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

import org.apache.commons.math3.random.RandomGenerator;

import nucleus.ActorContext;
import nucleus.Plugin;
import nucleus.Simulation;
import nucleus.testsupport.testplugin.TestActorPlan;
import nucleus.testsupport.testplugin.TestError;
import nucleus.testsupport.testplugin.TestPlugin;
import nucleus.testsupport.testplugin.TestPluginData;
import nucleus.testsupport.testplugin.TestSimulationOutputConsumer;
import plugins.materials.MaterialsPlugin;
import plugins.materials.MaterialsPluginData;
import plugins.people.PeoplePlugin;
import plugins.people.PeoplePluginData;
import plugins.regions.RegionsPlugin;
import plugins.regions.RegionsPluginData;
import plugins.regions.testsupport.TestRegionId;
import plugins.resources.ResourcesPlugin;
import plugins.resources.ResourcesPluginData;
import plugins.resources.testsupport.TestResourceId;
import plugins.resources.testsupport.TestResourcePropertyId;
import plugins.stochastics.StochasticsPlugin;
import plugins.stochastics.StochasticsPluginData;
import plugins.util.properties.PropertyDefinition;
import util.errors.ContractException;
import util.random.RandomGeneratorProvider;

/**
 * A static test support class for the materials plugin. Provides convenience
 * methods for integrating an action plugin into a materials-based simulation
 * test harness.
 * 
 * 
 *
 */
public class MaterialsActionSupport {

	private MaterialsActionSupport() {
	}

	/**
	 * Creates an action plugin with an agent that will execute the given
	 * consumer at time 0. The action plugin and the remaining arguments are
	 * passed to an invocation of the testConsumers() method.
	 */
	public static void testConsumer(long seed, Consumer<ActorContext> consumer) {
		TestPluginData.Builder pluginBuilder = TestPluginData.builder();
		pluginBuilder.addTestActorPlan("actor", new TestActorPlan(0, consumer));
		TestPluginData testPluginData = pluginBuilder.build();
		Plugin testPlugin = TestPlugin.getTestPlugin(testPluginData);
		testConsumers(seed, testPlugin);
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
	 * The test plugin is integrated into the simulation run and must contain at
	 * least one action plan. This helps to ensure that a test that does not run
	 * completely does not lead to a false positive test evaluation.
	 * 
	 * @throws ContractException
	 *                           <li>{@linkplain TestError#TEST_EXECUTION_FAILURE}
	 *                           if not all
	 *                           action plans execute or if there are no action
	 *                           plans
	 *                           contained in the action plugin</li>
	 */
	public static void testConsumers(long seed, Plugin testPlugin) {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(seed);

		Simulation.Builder builder = Simulation.builder();

		for (Plugin plugin : setUpPluginsForTest(seed)) {
			builder.addPlugin(plugin);
		}

		StochasticsPluginData stochasticsPluginData = StochasticsPluginData.builder()
				.setSeed(randomGenerator.nextLong()).build();
		Plugin stochasticsPlugin = StochasticsPlugin.getStochasticsPlugin(stochasticsPluginData);
		// add the stochastics plugin
		builder.addPlugin(stochasticsPlugin);

		// set the output consumer
		TestSimulationOutputConsumer outputConsumer = new TestSimulationOutputConsumer();

		// build and execute the engine
		builder.addPlugin(testPlugin)
				.setOutputConsumer(outputConsumer)
				.build()
				.execute();

		if (!outputConsumer.isComplete()) {
			throw new ContractException(TestError.TEST_EXECUTION_FAILURE);
		}
	}

	public static List<Plugin> setUpPluginsForTest(long seed) {

		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(seed);

		MaterialsPluginData.Builder materialsBuilder = MaterialsPluginData.builder();

		for (TestMaterialId testMaterialId : TestMaterialId.values()) {
			materialsBuilder.addMaterial(testMaterialId);
		}

		for (TestMaterialsProducerId testMaterialsProducerId : TestMaterialsProducerId.values()) {
			materialsBuilder.addMaterialsProducerId(testMaterialsProducerId);
		}

		for (TestMaterialsProducerPropertyId testMaterialsProducerPropertyId : TestMaterialsProducerPropertyId
				.values()) {
			materialsBuilder.defineMaterialsProducerProperty(testMaterialsProducerPropertyId,
					testMaterialsProducerPropertyId.getPropertyDefinition());
		}

		for (TestMaterialsProducerPropertyId testMaterialsProducerPropertyId : TestMaterialsProducerPropertyId
				.getPropertiesWithoutDefaultValues()) {
			for (TestMaterialsProducerId testMaterialsProducerId : TestMaterialsProducerId.values()) {
				Object randomPropertyValue = testMaterialsProducerPropertyId.getRandomPropertyValue(randomGenerator);
				materialsBuilder.setMaterialsProducerPropertyValue(testMaterialsProducerId,
						testMaterialsProducerPropertyId, randomPropertyValue);
			}
		}

		for (TestMaterialId testMaterialId : TestMaterialId.values()) {
			Set<TestBatchPropertyId> testBatchPropertyIds = TestBatchPropertyId.getTestBatchPropertyIds(testMaterialId);
			for (TestBatchPropertyId testBatchPropertyId : testBatchPropertyIds) {
				materialsBuilder.defineBatchProperty(testMaterialId, testBatchPropertyId,
						testBatchPropertyId.getPropertyDefinition());
			}
		}
		MaterialsPluginData materialsPluginData = materialsBuilder.build();
		Plugin materialsPlugin = MaterialsPlugin.getMaterialsPlugin(materialsPluginData);

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

		// add the people plugin

		PeoplePluginData.Builder peopleBuilder = PeoplePluginData.builder();
		PeoplePluginData peoplePluginData = peopleBuilder.build();
		Plugin peoplePlugin = PeoplePlugin.getPeoplePlugin(peoplePluginData);

		// add the regions plugin
		RegionsPluginData.Builder regionsBuilder = RegionsPluginData.builder();
		for (TestRegionId testRegionId : TestRegionId.values()) {
			regionsBuilder.addRegion(testRegionId);
		}
		RegionsPluginData regionsPluginData = regionsBuilder.build();
		Plugin regionPlugin = RegionsPlugin.getRegionsPlugin(regionsPluginData);

		StochasticsPluginData stochasticsPluginData = StochasticsPluginData.builder()
				.setSeed(randomGenerator.nextLong()).build();
		Plugin stochasticsPlugin = StochasticsPlugin.getStochasticsPlugin(stochasticsPluginData);

		List<Plugin> pluginsToAdd = new ArrayList<>();

		pluginsToAdd.add(materialsPlugin);
		pluginsToAdd.add(resourcesPlugin);
		pluginsToAdd.add(peoplePlugin);
		pluginsToAdd.add(regionPlugin);
		pluginsToAdd.add(stochasticsPlugin);

		return pluginsToAdd;
	}

}
