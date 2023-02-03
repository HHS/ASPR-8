package plugins.materials.testsupport;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.function.Consumer;

import org.apache.commons.math3.random.RandomGenerator;

import nucleus.ActorContext;
import nucleus.Plugin;
import nucleus.PluginData;
import nucleus.testsupport.testplugin.TestActorPlan;
import nucleus.testsupport.testplugin.TestPlugin;
import nucleus.testsupport.testplugin.TestPluginData;
import plugins.materials.MaterialsPlugin;
import plugins.materials.MaterialsPluginData;
import plugins.materials.support.BatchId;
import plugins.materials.support.StageId;
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
import util.random.RandomGeneratorProvider;

/**
 * A static test support class for the materials plugin. Provides convenience
 * methods for obtaining standard Materials, Resources, Regions, People and
 * Stochastics PluginData.
 * 
 * Also contains factory methods to obtain a list of plugins that can be
 * utilized with
 * {@code TestSimulation.executeSimulation()}
 * 
 */
public class MaterialsTestPluginFactory {

	private MaterialsTestPluginFactory() {
	}

	private static class Data {
		private MaterialsPluginData materialsPluginData;
		private ResourcesPluginData resourcesPluginData;
		private RegionsPluginData regionsPluginData;
		private PeoplePluginData peoplePluginData;
		private StochasticsPluginData stochasticsPluginData;
		private TestPluginData testPluginData;

		private Data(int numBatches, int numStages,
				int numBatchesInStage, long seed, TestPluginData testPluginData) {
			this.materialsPluginData = getStandardMaterialsPluginData(numBatches, numStages,
					numBatchesInStage, seed);
			this.resourcesPluginData = getStandardResourcesPluginData(seed);
			this.regionsPluginData = getStandardRegionsPluginData();
			this.peoplePluginData = getStandardPeoplePluginData();
			this.stochasticsPluginData = getStandardStochasticsPluginData(seed);
			this.testPluginData = testPluginData;
		}
	}

	/**
	 * Factory class that facilitates the building of {@linkplain PluginData}
	 * with the various setter methods.
	 */
	public static class Factory {
		private Data data;

		private Factory(Data data) {
			this.data = data;
		}

		/**
		 * Method that will get the PluginData for the Materials, Resources, Regions,
		 * People, Stochastic and
		 * Test Plugins
		 * and use the respective PluginData to build Plugins
		 * 
		 * @return a List containing a MaterialsPlugin, ResourcesPlugin, RegionsPlugin,
		 *         PeoplePlugin, StochasticsPlugin and
		 *         a TestPlugin
		 * 
		 */
		public List<Plugin> getPlugins() {
			List<Plugin> pluginsToAdd = new ArrayList<>();

			Plugin materialsPlugin = MaterialsPlugin.getMaterialsPlugin(this.data.materialsPluginData);

			Plugin resourcesPlugin = ResourcesPlugin.getResourcesPlugin(this.data.resourcesPluginData);

			Plugin regionsPlugin = RegionsPlugin.getRegionsPlugin(this.data.regionsPluginData);

			// add the people plugin
			Plugin peoplePlugin = PeoplePlugin.getPeoplePlugin(this.data.peoplePluginData);

			// add the stochastics plugin
			Plugin stochasticPlugin = StochasticsPlugin.getStochasticsPlugin(this.data.stochasticsPluginData);

			Plugin testPlugin = TestPlugin.getTestPlugin(this.data.testPluginData);

			pluginsToAdd.add(materialsPlugin);
			pluginsToAdd.add(resourcesPlugin);
			pluginsToAdd.add(regionsPlugin);
			pluginsToAdd.add(peoplePlugin);
			pluginsToAdd.add(stochasticPlugin);
			pluginsToAdd.add(testPlugin);

			return pluginsToAdd;
		}

		/**
		 * Method to set the MaterialsPluginData in this Factory.
		 * 
		 * @param materialsPluginData the MaterialsPluginData you want to use, if
		 *                            different
		 *                            from the standard PluginData
		 * @return an instance of this Factory
		 * 
		 */
		public Factory setMaterialsPluginData(MaterialsPluginData materialsPluginData) {
			this.data.materialsPluginData = materialsPluginData;
			return this;
		}

		/**
		 * Method to set the ResourcesPluginData in this Factory.
		 * 
		 * @param resourcesPluginData the ResourcesPluginData you want to use, if
		 *                            different
		 *                            from the standard PluginData
		 * @return an instance of this Factory
		 * 
		 */
		public Factory setResourcesPluginData(ResourcesPluginData resourcesPluginData) {
			this.data.resourcesPluginData = resourcesPluginData;
			return this;
		}

		/**
		 * Method to set the RegionsPluginData in this Factory.
		 * 
		 * @param regionsPluginData the RegionsPluginData you want to use, if different
		 *                          from the standard PluginData
		 * @return an instance of this Factory
		 * 
		 */
		public Factory setRegionsPluginData(RegionsPluginData regionsPluginData) {
			this.data.regionsPluginData = regionsPluginData;
			return this;
		}

		/**
		 * Method to set the PeoplePluginData in this Factory.
		 * 
		 * @param peoplePluginData the PeoplePluginData you want to use, if different
		 *                         from the standard PluginData
		 * @return an instance of this Factory
		 * 
		 */
		public Factory setPeoplePluginData(PeoplePluginData peoplePluginData) {
			this.data.peoplePluginData = peoplePluginData;
			return this;
		}

		/**
		 * Method to set the StochasticsPluginData in this Factory.
		 * 
		 * @param stochasticsPluginData the StochasticsPluginData you want to use, if
		 *                              different
		 *                              from the standard PluginData
		 * @return an instance of this Factory
		 * 
		 */
		public Factory setStochasticsPluginData(StochasticsPluginData stochasticsPluginData) {
			this.data.stochasticsPluginData = stochasticsPluginData;
			return this;
		}

	}

	/**
	 * Method that will generate MaterialsPluginData, ResourcesPluginData,
	 * RegionsPluginData, PeoplePluginData and
	 * StocasticsPluginData based on some configuration parameters.
	 * 
	 * @param numBatches        number of batches to make
	 * @param numStages         number of stages to make
	 * @param numBatchesInStage number of batches that should be staged
	 * @param seed              used to seed a RandomGenerator
	 * @param testPluginData    PluginData that will be used to generate a
	 *                          TestPlugin
	 * @return a new instance of Factory
	 * 
	 */
	public static Factory factory(int numBatches, int numStages,
			int numBatchesInStage, long seed, TestPluginData testPluginData) {
		return new Factory(new Data(numBatches, numStages, numBatchesInStage, seed, testPluginData));
	}

	/**
	 * Method that will generate MaterialsPluginData, ResourcesPluginData,
	 * RegionsPluginData, PeoplePluginData,
	 * StocasticsPluginData and TestPluginData based on some configuration
	 * parameters.
	 * 
	 * @param numBatches        number of batches to make
	 * @param numStages         number of stages to make
	 * @param numBatchesInStage number of batches that should be staged
	 * @param seed              used to seed a RandomGenerator
	 * @param consumer          consumer to use to generate TestPluginData
	 * @return a new instance of Factory
	 * 
	 */
	public static Factory factory(int numBatches, int numStages,
			int numBatchesInStage, long seed, Consumer<ActorContext> consumer) {

		TestPluginData.Builder pluginBuilder = TestPluginData.builder();
		pluginBuilder.addTestActorPlan("actor", new TestActorPlan(0, consumer));
		TestPluginData testPluginData = pluginBuilder.build();
		return factory(numBatches, numStages, numBatchesInStage, seed, testPluginData);
	}

	/**
	 * 
	 * @param numBatches        number of batches to make
	 * @param numStages         number of stages to make
	 * @param numBatchesInStage number of batches that should be staged
	 * @param seed              used to seed a RandomGenerator
	 * @return the resulting MaterialsPluginData
	 * 
	 */
	public static MaterialsPluginData getStandardMaterialsPluginData(int numBatches, int numStages,
			int numBatchesInStage, long seed) {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(seed);
		MaterialsPluginData.Builder materialsBuilder = MaterialsPluginData.builder();

		int bId = 0;
		int sId = 0;
		for (TestMaterialId testMaterialId : TestMaterialId.values()) {
			materialsBuilder.addMaterial(testMaterialId);
		}

		for (TestMaterialsProducerId testMaterialsProducerId : TestMaterialsProducerId.values()) {

			List<BatchId> batches = new ArrayList<>();

			for (int i = 0; i < numBatches; i++) {

				TestMaterialId testMaterialId = TestMaterialId.getRandomMaterialId(randomGenerator);
				double amount = randomGenerator.nextDouble();
				BatchId batchId = new BatchId(bId++);
				materialsBuilder.addBatch(batchId, testMaterialId, amount, testMaterialsProducerId);
				batches.add(batchId);
				for (TestBatchPropertyId testBatchPropertyId : TestBatchPropertyId
						.getTestBatchPropertyIds(testMaterialId)) {
					boolean required = testBatchPropertyId.getPropertyDefinition().getDefaultValue().isEmpty();
					if (required || randomGenerator.nextBoolean()) {
						materialsBuilder.setBatchPropertyValue(batchId, testBatchPropertyId,
								testBatchPropertyId.getRandomPropertyValue(randomGenerator));
					}
				}

			}

			List<StageId> stages = new ArrayList<>();

			for (int i = 0; i < numStages; i++) {
				StageId stageId = new StageId(sId++);
				stages.add(stageId);
				boolean offered = i % 2 == 0;
				materialsBuilder.addStage(stageId, offered, testMaterialsProducerId);
			}

			Collections.shuffle(batches, new Random(randomGenerator.nextLong()));
			for (int i = 0; i < numBatchesInStage; i++) {
				BatchId batchId = batches.get(i);
				StageId stageId = stages.get(randomGenerator.nextInt(stages.size()));
				materialsBuilder.addBatchToStage(stageId, batchId);
			}
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
		return materialsBuilder.build();
	}

	/**
	 * Method that will return a Standard ResourcesPluginData based on some
	 * configuration parameters.
	 * 
	 * @param seed a seed to seed a RandomGenerator
	 * @return the resulting ResourcesPluginData
	 * 
	 */
	public static ResourcesPluginData getStandardResourcesPluginData(long seed) {

		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(seed);
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

		return resourcesBuilder.build();
	}

	/**
	 * Method that will return a Standard PeoplePluginData based on some
	 * configuration parameters.
	 * 
	 * @return the resulting PeoplePluginData
	 * 
	 */
	public static PeoplePluginData getStandardPeoplePluginData() {
		PeoplePluginData.Builder peopleBuilder = PeoplePluginData.builder();
		return peopleBuilder.build();
	}

	/**
	 * Method that will return a Standard RegionsPluginData based on some
	 * configuration parameters.
	 * 
	 * @return the resulting RegionsPluginData
	 * 
	 */
	public static RegionsPluginData getStandardRegionsPluginData() {
		RegionsPluginData.Builder regionsBuilder = RegionsPluginData.builder();
		for (TestRegionId testRegionId : TestRegionId.values()) {
			regionsBuilder.addRegion(testRegionId);
		}
		return regionsBuilder.build();
	}

	/**
	 * Method that will return a Standard StochasticsPluginData based on some
	 * configuration parameters.
	 * 
	 * @param seed a seed to seed a RandomGenerator
	 * @return the resulting StocasticsPluginData
	 * 
	 */
	public static StochasticsPluginData getStandardStochasticsPluginData(long seed) {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(seed);
		return StochasticsPluginData.builder()
				.setSeed(randomGenerator.nextLong()).build();
	}

}
