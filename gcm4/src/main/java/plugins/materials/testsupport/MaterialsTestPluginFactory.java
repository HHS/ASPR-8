package plugins.materials.testsupport;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.function.Consumer;

import org.apache.commons.math3.random.RandomGenerator;

import nucleus.ActorContext;
import nucleus.NucleusError;
import nucleus.Plugin;
import nucleus.PluginData;
import nucleus.testsupport.testplugin.TestActorPlan;
import nucleus.testsupport.testplugin.TestPlugin;
import nucleus.testsupport.testplugin.TestPluginData;
import nucleus.testsupport.testplugin.TestSimulation;
import plugins.materials.MaterialsPlugin;
import plugins.materials.MaterialsPluginData;
import plugins.materials.reports.BatchStatusReportPluginData;
import plugins.materials.reports.MaterialsProducerPropertyReportPluginData;
import plugins.materials.reports.MaterialsProducerResourceReportPluginData;
import plugins.materials.reports.StageReportPluginData;
import plugins.materials.support.BatchId;
import plugins.materials.support.MaterialsError;
import plugins.materials.support.StageId;
import plugins.people.PeoplePlugin;
import plugins.people.PeoplePluginData;
import plugins.people.support.PersonError;
import plugins.regions.RegionsPlugin;
import plugins.regions.RegionsPluginData;
import plugins.regions.support.RegionError;
import plugins.regions.testsupport.TestRegionId;
import plugins.resources.ResourcesPlugin;
import plugins.resources.ResourcesPluginData;
import plugins.resources.support.ResourceError;
import plugins.resources.support.ResourceId;
import plugins.resources.testsupport.TestResourceId;
import plugins.resources.testsupport.TestResourcePropertyId;
import plugins.stochastics.StochasticsPlugin;
import plugins.stochastics.StochasticsPluginData;
import plugins.stochastics.support.StochasticsError;
import plugins.stochastics.support.WellState;
import plugins.util.properties.PropertyDefinition;
import util.errors.ContractException;
import util.random.RandomGeneratorProvider;

/**
 * A static test support class for the {@linkplain MaterialsPlugin}. Provides
 * convenience methods for obtaining standarized PluginData for the listed
 * Plugin.
 * 
 * <p>
 * Also contains factory methods to obtain a list of plugins that is the minimal
 * set needed to adequately test this Plugin that can be utilized with
 * </p>
 * 
 * <li>{@link TestSimulation#executeSimulation}
 */
public class MaterialsTestPluginFactory {

	private MaterialsTestPluginFactory() {
	}

	private static class Data {
		private MaterialsPluginData materialsPluginData;
		private BatchStatusReportPluginData batchStatusReportPluginData;
		private MaterialsProducerPropertyReportPluginData materialsProducerPropertyReportPluginData;
		private MaterialsProducerResourceReportPluginData materialsProducerResourceReportPluginData;
		private StageReportPluginData stageReportPluginData;
		private ResourcesPluginData resourcesPluginData;
		private RegionsPluginData regionsPluginData;
		private PeoplePluginData peoplePluginData;
		private StochasticsPluginData stochasticsPluginData;
		private TestPluginData testPluginData;

		private Data(int numBatches, int numStages, int numBatchesInStage, long seed, TestPluginData testPluginData) {

			RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(seed);

			this.materialsPluginData = getStandardMaterialsPluginData(numBatches, numStages, numBatchesInStage, seed);
			this.resourcesPluginData = getStandardResourcesPluginData(randomGenerator.nextLong());
			this.regionsPluginData = getStandardRegionsPluginData();
			this.peoplePluginData = getStandardPeoplePluginData();
			this.stochasticsPluginData = getStandardStochasticsPluginData(randomGenerator.nextLong());
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
		 * Returns a list of plugins containing a Materials, Resources, Regions,
		 * People, Stochastic and Test Plugin built from the contributed
		 * PluginDatas
		 * 
		 * <li>MaterialsPlugin is defaulted to one formed from
		 * {@link MaterialsTestPluginFactory#getStandardMaterialsPluginData}
		 * <li>ResourcesPlugin is defaulted to one formed from
		 * {@link MaterialsTestPluginFactory#getStandardResourcesPluginData}
		 * <li>RegionsPlugin is defaulted to one formed from
		 * {@link MaterialsTestPluginFactory#getStandardRegionsPluginData}
		 * <li>PeoplePlugin is defaulted to one formed from
		 * {@link MaterialsTestPluginFactory#getStandardPeoplePluginData}
		 * <li>StochasticsPlugin is defaulted to one formed from
		 * {@link MaterialsTestPluginFactory#getStandardStochasticsPluginData}
		 * <li>TestPlugin is formed from the TestPluginData passed into
		 * {@link MaterialsTestPluginFactory#factory}
		 */
		public List<Plugin> getPlugins() {
			List<Plugin> pluginsToAdd = new ArrayList<>();

			MaterialsPlugin.Builder materialsPluginBuilder = MaterialsPlugin.builder();

			materialsPluginBuilder.setMaterialsPluginData(this.data.materialsPluginData);

			if (data.batchStatusReportPluginData != null) {
				materialsPluginBuilder.setBatchStatusReportPluginData(this.data.batchStatusReportPluginData);
			}
			if (data.materialsProducerPropertyReportPluginData != null) {
				materialsPluginBuilder.setMaterialsProducerPropertyReportPluginData(this.data.materialsProducerPropertyReportPluginData);
			}
			if (data.materialsProducerResourceReportPluginData != null) {
				materialsPluginBuilder.setMaterialsProducerResourceReportPluginData(this.data.materialsProducerResourceReportPluginData);
			}
			if (data.stageReportPluginData != null) {
				materialsPluginBuilder.setStageReportPluginData(this.data.stageReportPluginData);
			}

			Plugin materialsPlugin = materialsPluginBuilder.getMaterialsPlugin();

			Plugin resourcesPlugin = ResourcesPlugin.builder().setResourcesPluginData(this.data.resourcesPluginData).getResourcesPlugin();

			Plugin regionsPlugin = RegionsPlugin.builder().setRegionsPluginData(this.data.regionsPluginData).getRegionsPlugin();

			Plugin peoplePlugin = PeoplePlugin.getPeoplePlugin(this.data.peoplePluginData);

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
		 * Sets the {@link MaterialsPluginData} in this Factory. This explicit
		 * instance of pluginData will be used to create a MaterialsPlugin
		 * 
		 * @throws ContractExecption
		 *             {@linkplain MaterialsError#NULL_MATERIALS_PLUGIN_DATA} if
		 *             the passed in pluginData is null
		 */
		public Factory setMaterialsPluginData(MaterialsPluginData materialsPluginData) {
			if (materialsPluginData == null) {
				throw new ContractException(MaterialsError.NULL_MATERIALS_PLUGIN_DATA);
			}
			this.data.materialsPluginData = materialsPluginData;
			return this;
		}

		/**
		 * Sets the {@link BatchStatusReportPluginData} in this Factory. This
		 * explicit instance of pluginData will be used to create a
		 * MaterialsPlugin
		 * 
		 * @throws ContractExecption
		 *             {@linkplain MaterialsError#NULL_MATERIALS_PLUGIN_DATA} if
		 *             the passed in pluginData is null
		 */
		public Factory setBatchStatusReportPluginData(BatchStatusReportPluginData batchStatusReportPluginData) {
			if (batchStatusReportPluginData == null) {
				throw new ContractException(MaterialsError.NULL_BATCH_STATUS_REPORT_PLUGIN_DATA);
			}
			this.data.batchStatusReportPluginData = batchStatusReportPluginData;
			return this;
		}

		/**
		 * Sets the {@link MaterialsProducerPropertyReportPluginData} in this
		 * Factory. This explicit instance of pluginData will be used to create
		 * a MaterialsPlugin
		 * 
		 * @throws ContractExecption
		 *             {@linkplain MaterialsError#NULL_MATERIALS_PLUGIN_DATA} if
		 *             the passed in pluginData is null
		 */
		public Factory setMaterialsProducerPropertyReportPluginData(MaterialsProducerPropertyReportPluginData materialsProducerPropertyReportPluginData) {
			if (materialsProducerPropertyReportPluginData == null) {
				throw new ContractException(MaterialsError.NULL_MATERIALS_PRODUCER_PROPERTY_REPORT_PLUGIN_DATA);
			}
			this.data.materialsProducerPropertyReportPluginData = materialsProducerPropertyReportPluginData;
			return this;
		}

		/**
		 * Sets the {@link MaterialsProducerResourceReportPluginData} in this
		 * Factory. This explicit instance of pluginData will be used to create
		 * a MaterialsPlugin
		 * 
		 * @throws ContractExecption
		 *             {@linkplain MaterialsError#NULL_MATERIALS_PLUGIN_DATA} if
		 *             the passed in pluginData is null
		 */
		public Factory setMaterialsProducerResourceReportPluginData(MaterialsProducerResourceReportPluginData materialsProducerResourceReportPluginData) {
			if (materialsProducerResourceReportPluginData == null) {
				throw new ContractException(MaterialsError.NULL_MATERIALS_PRODUCER_RESOURCE_REPORT_PLUGIN_DATA);
			}
			this.data.materialsProducerResourceReportPluginData = materialsProducerResourceReportPluginData;
			return this;
		}

		/**
		 * Sets the {@link MaterialsProducerResourceReportPluginData} in this
		 * Factory. This explicit instance of pluginData will be used to create
		 * a MaterialsPlugin
		 * 
		 * @throws ContractExecption
		 *             {@linkplain MaterialsError#NULL_MATERIALS_PLUGIN_DATA} if
		 *             the passed in pluginData is null
		 */
		public Factory setStageReportPluginData(StageReportPluginData stageReportPluginData) {
			if (stageReportPluginData == null) {
				throw new ContractException(MaterialsError.NULL_STAGE_REPORT_PLUGIN_DATA);
			}
			this.data.stageReportPluginData = stageReportPluginData;
			return this;
		}

		/**
		 * Sets the {@link ResourcesPluginData} in this Factory. This explicit
		 * instance of pluginData will be used to create a ResourcesPlugin
		 * 
		 * @throws ContractExecption
		 *             {@linkplain ResourceError#NULL_RESOURCE_PLUGIN_DATA} if
		 *             the passed in pluginData is null
		 */
		public Factory setResourcesPluginData(ResourcesPluginData resourcesPluginData) {
			if (resourcesPluginData == null) {
				throw new ContractException(ResourceError.NULL_RESOURCE_PLUGIN_DATA);
			}
			this.data.resourcesPluginData = resourcesPluginData;
			return this;
		}

		/**
		 * Sets the {@link RegionsPluginData} in this Factory. This explicit
		 * instance of pluginData will be used to create a RegionsPlugin
		 * 
		 * @throws ContractExecption
		 *             {@linkplain RegionError#NULL_REGION_PLUGIN_DATA} if the
		 *             passed in pluginData is null
		 */
		public Factory setRegionsPluginData(RegionsPluginData regionsPluginData) {
			if (regionsPluginData == null) {
				throw new ContractException(RegionError.NULL_REGION_PLUGIN_DATA);
			}
			this.data.regionsPluginData = regionsPluginData;
			return this;
		}

		/**
		 * Sets the {@link PeoplePluginData} in this Factory. This explicit
		 * instance of pluginData will be used to create a PeoplePlugin
		 * 
		 * @throws ContractExecption
		 *             {@linkplain PersonError#NULL_PEOPLE_PLUGIN_DATA} if the
		 *             passed in pluginData is null
		 */
		public Factory setPeoplePluginData(PeoplePluginData peoplePluginData) {
			if (peoplePluginData == null) {
				throw new ContractException(PersonError.NULL_PEOPLE_PLUGIN_DATA);
			}
			this.data.peoplePluginData = peoplePluginData;
			return this;
		}

		/**
		 * Sets the {@link StochasticsPluginData} in this Factory. This explicit
		 * instance of pluginData will be used to create a StochasticsPlugin
		 * 
		 * @throws ContractExecption
		 *             {@linkplain StochasticsError#NULL_STOCHASTICS_PLUGIN_DATA}
		 *             if the passed in pluginData is null
		 */
		public Factory setStochasticsPluginData(StochasticsPluginData stochasticsPluginData) {
			if (stochasticsPluginData == null) {
				throw new ContractException(StochasticsError.NULL_STOCHASTICS_PLUGIN_DATA);
			}
			this.data.stochasticsPluginData = stochasticsPluginData;
			return this;
		}

	}

	/**
	 * Creates a Factory that facilitates the creation of a minimal set of
	 * plugins needed to adequately test the {@link MaterialsPlugin} by
	 * generating:
	 * <ul>
	 * <li>{@link MaterialsPluginData}
	 * <li>{@link ResourcesPluginData}
	 * <li>{@link RegionsPluginData}
	 * <li>{@link PeoplePluginData}
	 * <li>{@link StochasticsPluginData}
	 * </ul>
	 * <li>either directly (by default) via
	 * <ul>
	 * <li>{@link #getStandardMaterialsPluginData},
	 * <li>{@link #getStandardResourcesPluginData},
	 * <li>{@link #getStandardPeoplePluginData},
	 * <li>{@link #getStandardRegionsPluginData},
	 * <li>{@link #getStandardStochasticsPluginData}
	 * </ul>
	 * <li>or explicitly set via
	 * <ul>
	 * <li>{@link Factory#setMaterialsPluginData}
	 * <li>{@link Factory#setResourcesPluginData},
	 * <li>{@link Factory#setPeoplePluginData},
	 * <li>{@link Factory#setRegionsPluginData},
	 * <li>{@link Factory#setStochasticsPluginData}
	 * </ul>
	 * 
	 * <li>via the {@link Factory#getPlugins()} method.
	 * 
	 * @throws ContractExecption
	 *             {@linkplain NucleusError#NULL_PLUGIN_DATA} if testPluginData
	 *             is null
	 */
	public static Factory factory(int numBatches, int numStages, int numBatchesInStage, long seed, TestPluginData testPluginData) {
		if (testPluginData == null) {
			throw new ContractException(NucleusError.NULL_PLUGIN_DATA);
		}
		return new Factory(new Data(numBatches, numStages, numBatchesInStage, seed, testPluginData));
	}

	/**
	 * Creates a Factory that facilitates the creation of a minimal set of
	 * plugins needed to adequately test the {@link MaterialsPlugin} by
	 * generating:
	 * <ul>
	 * <li>{@link MaterialsPluginData}
	 * <li>{@link ResourcesPluginData}
	 * <li>{@link RegionsPluginData}
	 * <li>{@link PeoplePluginData}
	 * <li>{@link StochasticsPluginData}
	 * </ul>
	 * <li>either directly (by default) via
	 * <ul>
	 * <li>{@link #getStandardMaterialsPluginData},
	 * <li>{@link #getStandardResourcesPluginData},
	 * <li>{@link #getStandardPeoplePluginData},
	 * <li>{@link #getStandardRegionsPluginData},
	 * <li>{@link #getStandardStochasticsPluginData}
	 * </ul>
	 * <li>or explicitly set via
	 * <ul>
	 * <li>{@link Factory#setMaterialsPluginData}
	 * <li>{@link Factory#setResourcesPluginData},
	 * <li>{@link Factory#setPeoplePluginData},
	 * <li>{@link Factory#setRegionsPluginData},
	 * <li>{@link Factory#setStochasticsPluginData}
	 * </ul>
	 * 
	 * <li>via the {@link Factory#getPlugins()} method.
	 * 
	 * @throws ContractExecption
	 *             {@linkplain NucleusError#NULL_ACTOR_CONTEXT_CONSUMER} if
	 *             consumer is null
	 */
	public static Factory factory(int numBatches, int numStages, int numBatchesInStage, long seed, Consumer<ActorContext> consumer) {
		if (consumer == null) {
			throw new ContractException(NucleusError.NULL_ACTOR_CONTEXT_CONSUMER);
		}

		TestPluginData.Builder pluginBuilder = TestPluginData.builder();
		pluginBuilder.addTestActorPlan("actor", new TestActorPlan(0, consumer));
		TestPluginData testPluginData = pluginBuilder.build();
		return factory(numBatches, numStages, numBatchesInStage, seed, testPluginData);
	}

	/**
	 * Returns a standardized MaterialsPluginData that is minimally adequate for
	 * testing the MaterialsPlugin
	 * <li>The resulting MaterialsPluginData will include:
	 * <ul>
	 * <li>Every MaterialId included in {@link TestMaterialId}
	 * <ul>
	 * <li>Each MaterialId will be used to define a BatchProperty via
	 * {@link TestBatchPropertyId#getTestBatchPropertyIds} along with the
	 * propertyDefinition for each
	 * </ul>
	 * <li>Every MaterialProducerId included in {@link TestMaterialsProducerId}
	 * With each one containing:
	 * <ul>
	 * <li>The specified number of batches. Each batch will have the following:
	 * <ul>
	 * <li>a random materialId gotten from
	 * {@link TestMaterialId#getRandomMaterialId} based on a RandomGenerator
	 * seeded by the passed in seed
	 * <li>a random amount based on the same RandomGenerator.nextDouble
	 * <li>Every BatchPropertyId included in {@link TestBatchPropertyId} where
	 * the batchPropertyValue will be set if the batchPropertyDefinition does
	 * not have a default value OR if randomGenerator.nextBoolean is true.
	 * Either way, the value will be set to the result from
	 * {@link TestBatchPropertyId#getRandomPropertyValue}
	 * </ul>
	 * <li>the specified number of stages. Exactly half will be offered.
	 * <li>the specified number of batches in a stage dervied from a shuffled
	 * list of batches via nextLong and a random stageId via nextInt
	 * </ul>
	 * <li>Every MaterialProducerPropertyId included in
	 * {@link TestMaterialsProducerPropertyId} along with the defined
	 * propertyDefinition for each.
	 * <ul>
	 * <li>The MaterialProducerPropertyIds that do not have default values will
	 * have a property value set derived from
	 * {@link TestMaterialsProducerPropertyId#getRandomPropertyValue}
	 * </ul>
	 */
	public static MaterialsPluginData getStandardMaterialsPluginData(int numBatches, int numStages, int numBatchesInStage, long seed) {
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
				for (TestBatchPropertyId testBatchPropertyId : TestBatchPropertyId.getTestBatchPropertyIds(testMaterialId)) {
					boolean required = testBatchPropertyId.getPropertyDefinition().getDefaultValue().isEmpty();
					if (required || randomGenerator.nextBoolean()) {
						materialsBuilder.setBatchPropertyValue(batchId, testBatchPropertyId, testBatchPropertyId.getRandomPropertyValue(randomGenerator));
					}
				}

			}

			List<StageId> stages = new ArrayList<>();

			for (int i = 0; i < numStages; i++) {
				StageId stageId = new StageId(sId++);
				stages.add(stageId);
				boolean offered = i % 2 == 0;
				materialsBuilder.addStage(stageId, offered);
				materialsBuilder.addStageToMaterialProducer(stageId, testMaterialsProducerId);				
			}

			Collections.shuffle(batches, new Random(randomGenerator.nextLong()));
			for (int i = 0; i < numBatchesInStage; i++) {
				BatchId batchId = batches.get(i);
				StageId stageId = stages.get(randomGenerator.nextInt(stages.size()));
				materialsBuilder.addBatchToStage(stageId, batchId);
			}
			materialsBuilder.addMaterialsProducerId(testMaterialsProducerId);

			for (ResourceId resourceId : TestResourceId.values()) {
                if (randomGenerator.nextBoolean()) {
                    materialsBuilder.setMaterialsProducerResourceLevel(testMaterialsProducerId, resourceId,
                            randomGenerator.nextInt(10));
                }
            }
		}

		for (TestMaterialsProducerPropertyId testMaterialsProducerPropertyId : TestMaterialsProducerPropertyId.values()) {
			materialsBuilder.defineMaterialsProducerProperty(testMaterialsProducerPropertyId, testMaterialsProducerPropertyId.getPropertyDefinition());
		}

		for (TestMaterialsProducerPropertyId testMaterialsProducerPropertyId : TestMaterialsProducerPropertyId.getPropertiesWithoutDefaultValues()) {
			for (TestMaterialsProducerId testMaterialsProducerId : TestMaterialsProducerId.values()) {
				Object randomPropertyValue = testMaterialsProducerPropertyId.getRandomPropertyValue(randomGenerator);
				materialsBuilder.setMaterialsProducerPropertyValue(testMaterialsProducerId, testMaterialsProducerPropertyId, randomPropertyValue);
			}
		}

		for (TestMaterialId testMaterialId : TestMaterialId.values()) {
			Set<TestBatchPropertyId> testBatchPropertyIds = TestBatchPropertyId.getTestBatchPropertyIds(testMaterialId);
			for (TestBatchPropertyId testBatchPropertyId : testBatchPropertyIds) {
				materialsBuilder.defineBatchProperty(testMaterialId, testBatchPropertyId, testBatchPropertyId.getPropertyDefinition());
			}
		}
		return materialsBuilder.build();
	}

	/**
	 * Returns a standardized ResourcesPluginData that is minimally adequate for
	 * testing the MaterialsPlugin
	 * <li>The resulting ResourcesPluginData will include:
	 * <ul>
	 * <li>Every ResourceId included in {@link TestResourceId} along with the
	 * defined timeTrackingPolicy for each
	 * <li>Every ResourcePropertyId included in {@link TestResourcePropertyId}
	 * along with the defined propertyDefinition for each.
	 * <ul>
	 * <li>Each Resource will have a random property value assigned based on a
	 * RandomGenerator that is created with the passed in seed
	 * </ul>
	 * </ul>
	 */
	public static ResourcesPluginData getStandardResourcesPluginData(long seed) {

		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(seed);
		ResourcesPluginData.Builder resourcesBuilder = ResourcesPluginData.builder();

		for (TestResourceId testResourceId : TestResourceId.values()) {
			resourcesBuilder.addResource(testResourceId,0.0,testResourceId.getTimeTrackingPolicy());			
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
	 * Returns a standardized PeoplePluginData that is minimally adequate for
	 * testing the MaterialsPlugin
	 * <li>The resulting PeoplePluginData will be empty
	 * <ul>
	 * <li>the equivalent of PeoplePluginData.builder().build()
	 * </ul>
	 */
	public static PeoplePluginData getStandardPeoplePluginData() {
		PeoplePluginData.Builder peopleBuilder = PeoplePluginData.builder();
		return peopleBuilder.build();
	}

	/**
	 * Returns a standardized RegionsPluginData that is minimally adequate for
	 * testing the MaterialsPlugin
	 * <li>The resulting RegionsPluginData will include:
	 * <ul>
	 * <li>Every RegionId included in {@link TestRegionId}
	 * </ul>
	 */
	public static RegionsPluginData getStandardRegionsPluginData() {
		RegionsPluginData.Builder regionsBuilder = RegionsPluginData.builder();
		for (TestRegionId testRegionId : TestRegionId.values()) {
			regionsBuilder.addRegion(testRegionId);
		}
		return regionsBuilder.build();
	}

	/**
	 * Returns a standardized StochasticsPluginData that is minimally adequate
	 * for testing the MaterialsPlugin
	 * <li>The resulting StochasticsPluginData will include:
	 * <ul>
	 * <li>a seed based on the nextLong of a RandomGenerator seeded from the
	 * passed in seed
	 * </ul>
	 */
	public static StochasticsPluginData getStandardStochasticsPluginData(long seed) {
		return StochasticsPluginData.builder().setMainRNGState(WellState.builder().setSeed(seed).build()).build();
	}

}
