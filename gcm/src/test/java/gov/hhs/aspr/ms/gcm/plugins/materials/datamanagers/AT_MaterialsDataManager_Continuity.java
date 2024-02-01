package gov.hhs.aspr.ms.gcm.plugins.materials.datamanagers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.function.Consumer;

import org.apache.commons.math3.random.RandomGenerator;
import org.apache.commons.math3.util.FastMath;
import org.apache.commons.math3.util.Pair;
import org.junit.jupiter.api.Test;

import gov.hhs.aspr.ms.gcm.nucleus.ActorContext;
import gov.hhs.aspr.ms.gcm.nucleus.DataManagerContext;
import gov.hhs.aspr.ms.gcm.nucleus.Plugin;
import gov.hhs.aspr.ms.gcm.nucleus.Simulation;
import gov.hhs.aspr.ms.gcm.nucleus.SimulationState;
import gov.hhs.aspr.ms.gcm.nucleus.testsupport.runcontinuityplugin.RunContinuityPlugin;
import gov.hhs.aspr.ms.gcm.nucleus.testsupport.runcontinuityplugin.RunContinuityPluginData;
import gov.hhs.aspr.ms.gcm.nucleus.testsupport.testplugin.TestOutputConsumer;
import gov.hhs.aspr.ms.gcm.plugins.materials.MaterialsPlugin;
import gov.hhs.aspr.ms.gcm.plugins.materials.datamangers.MaterialsDataManager;
import gov.hhs.aspr.ms.gcm.plugins.materials.datamangers.MaterialsPluginData;
import gov.hhs.aspr.ms.gcm.plugins.materials.support.BatchConstructionInfo;
import gov.hhs.aspr.ms.gcm.plugins.materials.support.BatchId;
import gov.hhs.aspr.ms.gcm.plugins.materials.support.BatchPropertyDefinitionInitialization;
import gov.hhs.aspr.ms.gcm.plugins.materials.support.MaterialId;
import gov.hhs.aspr.ms.gcm.plugins.materials.support.MaterialsProducerConstructionData;
import gov.hhs.aspr.ms.gcm.plugins.materials.support.MaterialsProducerId;
import gov.hhs.aspr.ms.gcm.plugins.materials.support.MaterialsProducerPropertyDefinitionInitialization;
import gov.hhs.aspr.ms.gcm.plugins.materials.support.StageConversionInfo;
import gov.hhs.aspr.ms.gcm.plugins.materials.support.StageId;
import gov.hhs.aspr.ms.gcm.plugins.materials.testsupport.TestBatchPropertyId;
import gov.hhs.aspr.ms.gcm.plugins.materials.testsupport.TestMaterialId;
import gov.hhs.aspr.ms.gcm.plugins.materials.testsupport.TestMaterialsProducerId;
import gov.hhs.aspr.ms.gcm.plugins.materials.testsupport.TestMaterialsProducerPropertyId;
import gov.hhs.aspr.ms.gcm.plugins.people.PeoplePlugin;
import gov.hhs.aspr.ms.gcm.plugins.people.datamanagers.PeopleDataManager;
import gov.hhs.aspr.ms.gcm.plugins.people.datamanagers.PeoplePluginData;
import gov.hhs.aspr.ms.gcm.plugins.people.support.PersonConstructionData;
import gov.hhs.aspr.ms.gcm.plugins.properties.support.PropertyDefinition;
import gov.hhs.aspr.ms.gcm.plugins.regions.RegionsPlugin;
import gov.hhs.aspr.ms.gcm.plugins.regions.datamanagers.RegionsPluginData;
import gov.hhs.aspr.ms.gcm.plugins.regions.testsupport.TestRegionId;
import gov.hhs.aspr.ms.gcm.plugins.resources.ResourcesPlugin;
import gov.hhs.aspr.ms.gcm.plugins.resources.datamanagers.ResourcesDataManager;
import gov.hhs.aspr.ms.gcm.plugins.resources.datamanagers.ResourcesPluginData;
import gov.hhs.aspr.ms.gcm.plugins.resources.support.ResourceId;
import gov.hhs.aspr.ms.gcm.plugins.resources.testsupport.TestResourceId;
import gov.hhs.aspr.ms.gcm.plugins.stochastics.StochasticsPlugin;
import gov.hhs.aspr.ms.gcm.plugins.stochastics.datamanagers.StochasticsDataManager;
import gov.hhs.aspr.ms.gcm.plugins.stochastics.datamanagers.StochasticsPluginData;
import gov.hhs.aspr.ms.gcm.plugins.stochastics.support.WellState;
import util.annotations.UnitTestMethod;
import util.random.RandomGeneratorProvider;

public class AT_MaterialsDataManager_Continuity {

	/**
	 * Demonstrates that the data manager exhibits run continuity. The state of the
	 * data manager is not effected by repeatedly starting and stopping the
	 * simulation.
	 */

	@Test
	@UnitTestMethod(target = MaterialsDataManager.class, name = "init", args = { DataManagerContext.class })
	public void testStateContinuity() {

		/*
		 * The returned string is the ordered state of the materials data manager. We
		 * generate this state at the end of each batch of simulation runs.
		 */

		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(1347391862469572673L);
		long seed = randomGenerator.nextLong();
		Set<String> pluginDatas = new LinkedHashSet<>();
		pluginDatas.add(testStateContinuity(1, seed));
		pluginDatas.add(testStateContinuity(5, seed));
		pluginDatas.add(testStateContinuity(10, seed));
		assertEquals(1, pluginDatas.size());

	}

	/*
	 * Contains the current plugin data state of the simulation
	 *
	 */
	private static class StateData {

		private PeoplePluginData peoplePluginData;
		private MaterialsPluginData materialsPluginData;
		private RunContinuityPluginData runContinuityPluginData;
		private RegionsPluginData regionsPluginData;
		private ResourcesPluginData resourcesPluginData;
		private StochasticsPluginData stochasticsPluginData;
		private SimulationState simulationState;
		private double haltTime;
		private String output;
	}

	private static StateData getInitialState(long seed) {
		StateData result = new StateData();
		result.peoplePluginData = getPeoplePluginData();
		result.materialsPluginData = getMaterialsPluginData();
		result.runContinuityPluginData = getRunContinuityPluginData();
		result.regionsPluginData = getRegionsPluginData();
		result.resourcesPluginData = getResourcesPluginData();
		result.stochasticsPluginData = getStochasticsPluginData(seed);
		result.simulationState = getSimulationState();
		return result;
	}

	private static ResourcesPluginData getResourcesPluginData() {
		ResourcesPluginData.Builder resourcesBuilder = ResourcesPluginData.builder();
		for (TestResourceId testResourceId : TestResourceId.values()) {
			resourcesBuilder.addResource(testResourceId, 0.0, false);
		}
		return resourcesBuilder.build();
	}

	private static MaterialsPluginData getMaterialsPluginData() {
		return MaterialsPluginData.builder().build();
	}

	private static RegionsPluginData getRegionsPluginData() {
		RegionsPluginData.Builder regionsBuilder = RegionsPluginData.builder();
		for (TestRegionId testRegionId : TestRegionId.values()) {
			regionsBuilder.addRegion(testRegionId);
		}
		return regionsBuilder.build();
	}

	private static int BASE_BATCH_COUNT = 50;
	private static int BASE_STAGE_COUNT = BASE_BATCH_COUNT / 2;

	private static void addFirstPeople(RunContinuityPluginData.Builder continuityBuilder) {
		// add a few people
		continuityBuilder.addContextConsumer(0.5, (c) -> {
			RandomGenerator randomGenerator = c.getDataManager(StochasticsDataManager.class).getRandomGenerator();

			PeopleDataManager peopleDataManager = c.getDataManager(PeopleDataManager.class);
			for (int i = 0; i < 10; i++) {
				TestRegionId regionId = TestRegionId.getRandomRegionId(randomGenerator);
				peopleDataManager.addPerson(PersonConstructionData.builder().add(regionId).build());
			}
		});
	}

	private static void startMaterialsProduction(RunContinuityPluginData.Builder continuityBuilder) {
		/*
		 * define some of the material ids
		 * 
		 * add some materials producers
		 * 
		 * define batch Properties
		 * 
		 * define materials producer properties
		 */
		continuityBuilder.addContextConsumer(0.67, (c) -> {
			RandomGenerator randomGenerator = c.getDataManager(StochasticsDataManager.class).getRandomGenerator();
			MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);

			// create all but one of the test materials
			List<TestMaterialId> testMaterialIds = new ArrayList<>();
			for (TestMaterialId testMaterialId : TestMaterialId.values()) {
				testMaterialIds.add(testMaterialId);
			}
			Collections.shuffle(testMaterialIds, new Random(randomGenerator.nextInt()));
			testMaterialIds.remove(testMaterialIds.size() - 1);
			for (TestMaterialId testMaterialId : testMaterialIds) {
				materialsDataManager.addMaterialId(testMaterialId);
			}

			for (TestMaterialsProducerId testMaterialsProducerId : TestMaterialsProducerId.values()) {

				TestResourceId resourceId = TestResourceId.getRandomResourceId(randomGenerator);
				long resourceAmount = randomGenerator.nextInt(1000) + 1;

				MaterialsProducerConstructionData materialsProducerConstructionData = MaterialsProducerConstructionData
						.builder()//
						.setMaterialsProducerId(testMaterialsProducerId)//
						.setResourceLevel(resourceId, resourceAmount)//
						.build();//
				materialsDataManager.addMaterialsProducer(materialsProducerConstructionData);
			}

			for (TestMaterialsProducerPropertyId testMaterialsProducerPropertyId : TestMaterialsProducerPropertyId
					.values()) {

				MaterialsProducerPropertyDefinitionInitialization.Builder defbuilder = MaterialsProducerPropertyDefinitionInitialization
						.builder();

				defbuilder.setMaterialsProducerPropertyId(testMaterialsProducerPropertyId);
				PropertyDefinition propertyDefinition = testMaterialsProducerPropertyId.getPropertyDefinition();
				defbuilder.setPropertyDefinition(propertyDefinition);
				if (propertyDefinition.getDefaultValue().isEmpty()) {
					for (MaterialsProducerId materialsProducerId : materialsDataManager.getMaterialsProducerIds()) {
						defbuilder.addPropertyValue(materialsProducerId,
								testMaterialsProducerPropertyId.getRandomPropertyValue(randomGenerator));
					}
				}
				MaterialsProducerPropertyDefinitionInitialization materialsProducerPropertyDefinitionInitialization = defbuilder
						.build();

				materialsDataManager.defineMaterialsProducerProperty(materialsProducerPropertyDefinitionInitialization);
			}

			for (TestBatchPropertyId testBatchPropertyId : TestBatchPropertyId.getBatchPropertyIds()) {
				// we need to restrict ourselves to just those properties that were included
				// above
				if (testMaterialIds.contains(testBatchPropertyId.getTestMaterialId())) {
					BatchPropertyDefinitionInitialization batchPropertyDefinitionInitialization = //
							BatchPropertyDefinitionInitialization.builder()//
									.setPropertyId(testBatchPropertyId)//
									.setPropertyDefinition(testBatchPropertyId.getPropertyDefinition())//
									.setMaterialId(testBatchPropertyId.getTestMaterialId())//
									.build();//
					materialsDataManager.defineBatchProperty(batchPropertyDefinitionInitialization);
				}
			}

		});
	}

	private static void createFirstBatches(RunContinuityPluginData.Builder continuityBuilder) {
		// create some batches
		continuityBuilder.addContextConsumer(0.95, (c) -> {
			RandomGenerator randomGenerator = c.getDataManager(StochasticsDataManager.class).getRandomGenerator();
			MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);

			List<TestMaterialId> knownMaterialIds = new ArrayList<>(materialsDataManager.getMaterialIds());

			for (int i = 0; i < BASE_BATCH_COUNT; i++) {
				double amount = randomGenerator.nextDouble() * 100;
				int index = randomGenerator.nextInt(knownMaterialIds.size());
				TestMaterialId testMaterialId = knownMaterialIds.get(index);

				TestMaterialsProducerId materialsProducerId = TestMaterialsProducerId
						.getRandomMaterialsProducerId(randomGenerator);
				BatchConstructionInfo.Builder batchBuilder = BatchConstructionInfo.builder();//
				batchBuilder.setAmount(amount);//
				batchBuilder.setMaterialId(testMaterialId);//
				batchBuilder.setMaterialsProducerId(materialsProducerId);//

				for (TestBatchPropertyId testBatchPropertyId : TestBatchPropertyId
						.getTestBatchPropertyIds(testMaterialId)) {
					if (testBatchPropertyId.getPropertyDefinition().getDefaultValue().isEmpty()) {
						Object propertyValue = testBatchPropertyId.getRandomPropertyValue(randomGenerator);
						batchBuilder.setPropertyValue(testBatchPropertyId, propertyValue);
					}
				}

				BatchConstructionInfo batchConstructionInfo = batchBuilder.build();

				materialsDataManager.addBatch(batchConstructionInfo);
			}

		});
	}

	private static void addMoreMaterialsDetails(RunContinuityPluginData.Builder continuityBuilder) {
		// define the remaining material ids and batch Properties, adding a few
		// more batches
		continuityBuilder.addContextConsumer(1.34, (c) -> {
			RandomGenerator randomGenerator = c.getDataManager(StochasticsDataManager.class).getRandomGenerator();
			MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);

			TestMaterialId newTestMaterialId = null;
			for (TestMaterialId testMaterialId : TestMaterialId.values()) {
				if (!materialsDataManager.materialIdExists(testMaterialId)) {
					newTestMaterialId = testMaterialId;
					break;
				}
			}
			assertNotNull(newTestMaterialId);

			materialsDataManager.addMaterialId(newTestMaterialId);

			List<TestBatchPropertyId> testBatchPropertyIds = new ArrayList<>(
					TestBatchPropertyId.getTestBatchPropertyIds(newTestMaterialId));
			Collections.shuffle(testBatchPropertyIds, new Random(randomGenerator.nextLong()));

			for (TestBatchPropertyId testBatchPropertyId : testBatchPropertyIds) {
				BatchPropertyDefinitionInitialization batchPropertyDefinitionInitialization = BatchPropertyDefinitionInitialization//
						.builder()//
						.setPropertyId(testBatchPropertyId)//
						.setPropertyDefinition(testBatchPropertyId.getPropertyDefinition())//
						.setMaterialId(testBatchPropertyId.getTestMaterialId())//
						.build();//

				materialsDataManager.defineBatchProperty(batchPropertyDefinitionInitialization);

			}

			for (int i = 0; i < BASE_BATCH_COUNT / 2; i++) {
				double amount = randomGenerator.nextDouble() * 100;

				TestMaterialsProducerId materialsProducerId = TestMaterialsProducerId
						.getRandomMaterialsProducerId(randomGenerator);
				BatchConstructionInfo.Builder batchBuilder = BatchConstructionInfo.builder();//
				batchBuilder.setAmount(amount);//
				batchBuilder.setMaterialId(newTestMaterialId);//
				batchBuilder.setMaterialsProducerId(materialsProducerId);//

				List<TestBatchPropertyId> propertyIds = new ArrayList<>(
						TestBatchPropertyId.getTestBatchPropertyIds(newTestMaterialId));

				Collections.shuffle(propertyIds, new Random(randomGenerator.nextLong()));
				for (TestBatchPropertyId testBatchPropertyId : propertyIds) {
					if (testBatchPropertyId.getPropertyDefinition().getDefaultValue().isEmpty()) {
						Object propertyValue = testBatchPropertyId.getRandomPropertyValue(randomGenerator);
						batchBuilder.setPropertyValue(testBatchPropertyId, propertyValue);
					}
				}

				BatchConstructionInfo batchConstructionInfo = batchBuilder.build();

				materialsDataManager.addBatch(batchConstructionInfo);
			}

		});
	}

	private static void createSomeStages(RunContinuityPluginData.Builder continuityBuilder) {
		// create some stages
		continuityBuilder.addContextConsumer(1.8, (c) -> {
			RandomGenerator randomGenerator = c.getDataManager(StochasticsDataManager.class).getRandomGenerator();
			MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);
			for (int i = 0; i < BASE_STAGE_COUNT; i++) {
				TestMaterialsProducerId materialsProducerId = TestMaterialsProducerId
						.getRandomMaterialsProducerId(randomGenerator);
				materialsDataManager.addStage(materialsProducerId);
			}
		});
	}

	private static void moveSomeBatchesToStages(RunContinuityPluginData.Builder continuityBuilder) {
		// move some batches to stages
		continuityBuilder.addContextConsumer(2.1, (c) -> {
			RandomGenerator randomGenerator = c.getDataManager(StochasticsDataManager.class).getRandomGenerator();
			MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);

			List<MaterialsProducerId> materialsProducerIds = new ArrayList<>(
					materialsDataManager.getMaterialsProducerIds());
			Collections.shuffle(materialsProducerIds, new Random(randomGenerator.nextLong()));

			for (MaterialsProducerId materialsProducerId : materialsProducerIds) {
				List<BatchId> inventoryBatches = materialsDataManager.getInventoryBatches(materialsProducerId);
				List<StageId> stages = materialsDataManager.getStages(materialsProducerId);

				if (!stages.isEmpty()) {
					// move about two thirds of the batches out of inventory and
					// onto stages
					Collections.shuffle(inventoryBatches, new Random(randomGenerator.nextLong()));
					int n = 2 * inventoryBatches.size() / 3;
					for (int i = 0; i < n; i++) {
						BatchId batchId = inventoryBatches.get(i);
						StageId stageId = stages.get(randomGenerator.nextInt(stages.size()));
						materialsDataManager.moveBatchToStage(batchId, stageId);
					}
				}
			}
		});
	}

	private static void removeSomeStagesAndBatches(RunContinuityPluginData.Builder continuityBuilder) {
		// remove some stages and batches
		continuityBuilder.addContextConsumer(2.4, (c) -> {
			RandomGenerator randomGenerator = c.getDataManager(StochasticsDataManager.class).getRandomGenerator();
			MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);

			List<MaterialsProducerId> materialsProducerIds = new ArrayList<>(
					materialsDataManager.getMaterialsProducerIds());
			Collections.shuffle(materialsProducerIds, new Random(randomGenerator.nextLong()));

			for (MaterialsProducerId materialsProducerId : materialsProducerIds) {
				List<BatchId> inventoryBatches = materialsDataManager.getInventoryBatches(materialsProducerId);
				List<StageId> stages = materialsDataManager.getStages(materialsProducerId);

				Collections.shuffle(inventoryBatches, new Random(randomGenerator.nextLong()));
				int n = inventoryBatches.size() / 10;
				for (int i = 0; i < n; i++) {
					materialsDataManager.removeBatch(inventoryBatches.get(i));
				}

				Collections.shuffle(stages, new Random(randomGenerator.nextLong()));
				n = stages.size() / 10;
				for (int i = 0; i < n; i++) {
					materialsDataManager.removeStage(stages.get(i), randomGenerator.nextBoolean());
				}
			}
		});
	}

	private static void convertSomeStages(RunContinuityPluginData.Builder continuityBuilder) {
		// convert some stages to batches or resources
		continuityBuilder.addContextConsumer(2.7, (c) -> {
			RandomGenerator randomGenerator = c.getDataManager(StochasticsDataManager.class).getRandomGenerator();
			MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);

			List<MaterialsProducerId> materialsProducerIds = new ArrayList<>(
					materialsDataManager.getMaterialsProducerIds());
			Collections.shuffle(materialsProducerIds, new Random(randomGenerator.nextLong()));

			for (MaterialsProducerId materialsProducerId : materialsProducerIds) {

				List<StageId> stages = materialsDataManager.getStages(materialsProducerId);

				Collections.shuffle(stages, new Random(randomGenerator.nextLong()));
				int n = stages.size() / 10;
				for (int i = 0; i < n; i++) {
					if (randomGenerator.nextBoolean()) {
						double amount = randomGenerator.nextDouble() * 100;
						TestMaterialId materialId = TestMaterialId.getRandomMaterialId(randomGenerator);
						StageConversionInfo.Builder builder = StageConversionInfo.builder();//
						builder.setAmount(amount);//
						builder.setMaterialId(materialId);//
						builder.setStageId(stages.get(i));//

						Set<TestBatchPropertyId> batchPropertyIds = materialsDataManager
								.getBatchPropertyIds(materialId);

						for (TestBatchPropertyId batchPropertyId : batchPropertyIds) {
							PropertyDefinition propertyDefinition = materialsDataManager
									.getBatchPropertyDefinition(materialId, batchPropertyId);
							if (propertyDefinition.getDefaultValue().isEmpty()) {
								Object propertyValue = batchPropertyId.getRandomPropertyValue(randomGenerator);
								builder.setPropertyValue(batchPropertyId, propertyValue);
							}
						}

						StageConversionInfo stageConversionInfo = builder.build();
						materialsDataManager.convertStageToBatch(stageConversionInfo);
					} else {
						TestResourceId resourceId = TestResourceId.getRandomResourceId(randomGenerator);
						long amount = randomGenerator.nextInt(100) + 1;
						materialsDataManager.convertStageToResource(stages.get(i), resourceId, amount);
					}
				}
			}

		});
	}

	private static void offerStages(RunContinuityPluginData.Builder continuityBuilder) {
		// offer a few stages
		continuityBuilder.addContextConsumer(2.9, (c) -> {
			RandomGenerator randomGenerator = c.getDataManager(StochasticsDataManager.class).getRandomGenerator();
			MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);

			List<MaterialsProducerId> materialsProducerIds = new ArrayList<>(
					materialsDataManager.getMaterialsProducerIds());
			Collections.shuffle(materialsProducerIds, new Random(randomGenerator.nextLong()));

			for (MaterialsProducerId materialsProducerId : materialsProducerIds) {
				List<StageId> stages = materialsDataManager.getStages(materialsProducerId);
				Collections.shuffle(stages, new Random(randomGenerator.nextLong()));
				int n = stages.size() / 6;
				for (int i = 0; i < n; i++) {
					StageId stageId = stages.get(i);
					materialsDataManager.setStageOfferState(stageId, true);
				}
			}
		});
	}

	private static void moveBatchesToInventory(RunContinuityPluginData.Builder continuityBuilder) {
		// move some batches to inventory
		continuityBuilder.addContextConsumer(3.1, (c) -> {
			RandomGenerator randomGenerator = c.getDataManager(StochasticsDataManager.class).getRandomGenerator();
			MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);

			List<MaterialsProducerId> materialsProducerIds = new ArrayList<>(
					materialsDataManager.getMaterialsProducerIds());
			Collections.shuffle(materialsProducerIds, new Random(randomGenerator.nextLong()));

			for (MaterialsProducerId materialsProducerId : materialsProducerIds) {
				List<StageId> stages = materialsDataManager.getStages(materialsProducerId);
				Collections.shuffle(stages, new Random(randomGenerator.nextLong()));

				for (StageId stageId : stages) {
					if (!materialsDataManager.isStageOffered(stageId)) {
						List<BatchId> batches = materialsDataManager.getStageBatches(stageId);
						Collections.shuffle(batches, new Random(randomGenerator.nextLong()));

						for (BatchId batchId : batches) {
							if (randomGenerator.nextDouble() < 0.3) {
								materialsDataManager.moveBatchToInventory(batchId);
							}
						}
					}
				}
			}

		});
	}

	private static void setSomeBatchProperties(RunContinuityPluginData.Builder continuityBuilder) {
		// set some batch properties
		continuityBuilder.addContextConsumer(3.5, (c) -> {

			RandomGenerator randomGenerator = c.getDataManager(StochasticsDataManager.class).getRandomGenerator();
			MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);

			List<MaterialsProducerId> materialsProducerIds = new ArrayList<>(
					materialsDataManager.getMaterialsProducerIds());
			Collections.shuffle(materialsProducerIds, new Random(randomGenerator.nextLong()));

			for (MaterialsProducerId materialsProducerId : materialsProducerIds) {
				List<StageId> stages = materialsDataManager.getStages(materialsProducerId);

				Collections.shuffle(stages, new Random(randomGenerator.nextLong()));

				for (StageId stageId : stages) {
					if (!materialsDataManager.isStageOffered(stageId)) {
						List<BatchId> batches = materialsDataManager.getStageBatches(stageId);

						Collections.shuffle(batches, new Random(randomGenerator.nextLong()));

						for (BatchId batchId : batches) {
							MaterialId materialId = materialsDataManager.getBatchMaterial(batchId);
							List<TestBatchPropertyId> batchPropertyIds = new ArrayList<>(
									materialsDataManager.getBatchPropertyIds(materialId));

							Collections.shuffle(batchPropertyIds, new Random(randomGenerator.nextLong()));

							for (TestBatchPropertyId batchPropertyId : batchPropertyIds) {
								PropertyDefinition propertyDefinition = materialsDataManager
										.getBatchPropertyDefinition(materialId, batchPropertyId);
								if (propertyDefinition.propertyValuesAreMutable()) {
									if (randomGenerator.nextDouble() < 0.25) {
										Object propertyValue = batchPropertyId.getRandomPropertyValue(randomGenerator);
										materialsDataManager.setBatchPropertyValue(batchId, batchPropertyId,
												propertyValue);
									}
								}
							}
						}
					}
				}

				List<BatchId> inventoryBatches = materialsDataManager.getInventoryBatches(materialsProducerId);
				Collections.shuffle(inventoryBatches, new Random(randomGenerator.nextLong()));

				for (BatchId batchId : inventoryBatches) {
					MaterialId materialId = materialsDataManager.getBatchMaterial(batchId);
					List<TestBatchPropertyId> batchPropertyIds = new ArrayList<>(
							materialsDataManager.getBatchPropertyIds(materialId));
					Collections.shuffle(batchPropertyIds, new Random(randomGenerator.nextLong()));

					for (TestBatchPropertyId batchPropertyId : batchPropertyIds) {
						PropertyDefinition propertyDefinition = materialsDataManager
								.getBatchPropertyDefinition(materialId, batchPropertyId);
						if (propertyDefinition.propertyValuesAreMutable()) {
							if (randomGenerator.nextDouble() < 0.25) {
								Object propertyValue = batchPropertyId.getRandomPropertyValue(randomGenerator);
								materialsDataManager.setBatchPropertyValue(batchId, batchPropertyId, propertyValue);
							}
						}
					}
				}
			}
		});
	}

	private static void setMoreProducerProperties(RunContinuityPluginData.Builder continuityBuilder) {
		// set some materials producer properties, add a new resource
		continuityBuilder.addContextConsumer(5.5, (c) -> {
			RandomGenerator randomGenerator = c.getDataManager(StochasticsDataManager.class).getRandomGenerator();
			MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);

			List<MaterialsProducerId> materialsProducerIds = new ArrayList<>(
					materialsDataManager.getMaterialsProducerIds());
			Collections.shuffle(materialsProducerIds, new Random(randomGenerator.nextLong()));

			for (MaterialsProducerId materialsProducerId : materialsProducerIds) {
				List<TestMaterialsProducerPropertyId> materialsProducerPropertyIds = new ArrayList<>(
						materialsDataManager.getMaterialsProducerPropertyIds());

				Collections.shuffle(materialsProducerPropertyIds, new Random(randomGenerator.nextLong()));

				for (TestMaterialsProducerPropertyId materialsProducerPropertyId : materialsProducerPropertyIds) {
					PropertyDefinition propertyDefinition = materialsDataManager
							.getMaterialsProducerPropertyDefinition(materialsProducerPropertyId);
					if (propertyDefinition.propertyValuesAreMutable()) {
						Object propertyValue = materialsProducerPropertyId.getRandomPropertyValue(randomGenerator);
						materialsDataManager.setMaterialsProducerPropertyValue(materialsProducerId,
								materialsProducerPropertyId, propertyValue);
					}
				}
			}
			ResourceId newResourceId = new ResourceId() {
				@Override
				public String toString() {
					return "RESOURCE_6";
				}
			};

			ResourcesDataManager resourcesDataManager = c.getDataManager(ResourcesDataManager.class);
			resourcesDataManager.addResourceId(newResourceId, false);
		});
	}

	private static void transferMaterialsBetweenBatches(RunContinuityPluginData.Builder continuityBuilder) {
		// transfer materials between batches
		continuityBuilder.addContextConsumer(5.6, (c) -> {
			RandomGenerator randomGenerator = c.getDataManager(StochasticsDataManager.class).getRandomGenerator();
			MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);

			List<MaterialsProducerId> materialsProducerIds = new ArrayList<>(
					materialsDataManager.getMaterialsProducerIds());
			Collections.shuffle(materialsProducerIds, new Random(randomGenerator.nextLong()));

			for (MaterialsProducerId materialsProducerId : materialsProducerIds) {
				List<MaterialId> materialIds = new ArrayList<>(materialsDataManager.getMaterialIds());
				Collections.shuffle(materialIds, new Random(randomGenerator.nextLong()));

				for (MaterialId materialId : materialIds) {
					List<BatchId> batches = materialsDataManager.getInventoryBatchesByMaterialId(materialsProducerId,
							materialId);
					if (batches.size() > 1) {
						Collections.shuffle(batches, new Random(randomGenerator.nextLong()));

						int n = batches.size();

						for (int i = 0; i < n; i++) {
							BatchId sourceBatchId = batches.get(randomGenerator.nextInt(batches.size()));
							BatchId destinationBatchId = sourceBatchId;
							while (destinationBatchId.equals(sourceBatchId)) {
								destinationBatchId = batches.get(randomGenerator.nextInt(batches.size()));
							}
							double amount = materialsDataManager.getBatchAmount(sourceBatchId);
							amount /= 2;
							materialsDataManager.transferMaterialBetweenBatches(sourceBatchId, destinationBatchId,
									amount);
						}
					}
				}
			}
		});

	}

	private static void transferAnOfferedStage(RunContinuityPluginData.Builder continuityBuilder) {
		// transfer an offered stage
		continuityBuilder.addContextConsumer(5.9, (c) -> {
			RandomGenerator randomGenerator = c.getDataManager(StochasticsDataManager.class).getRandomGenerator();
			MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);

			List<TestMaterialsProducerId> materialsProducerIds = new ArrayList<>(
					materialsDataManager.getMaterialsProducerIds());
			Collections.shuffle(materialsProducerIds, new Random(randomGenerator.nextLong()));

			for (TestMaterialsProducerId materialsProducerId : materialsProducerIds) {
				List<StageId> offeredStages = materialsDataManager.getOfferedStages(materialsProducerId);

				Collections.shuffle(offeredStages, new Random(randomGenerator.nextLong()));

				for (StageId stageId : offeredStages) {
					if (randomGenerator.nextBoolean()) {
						TestMaterialsProducerId nextMaterialsProducerId = materialsProducerId.next();
						materialsDataManager.transferOfferedStage(stageId, nextMaterialsProducerId);
					}
				}
			}

		});
	}

	private static void transferResourcesToRegions(RunContinuityPluginData.Builder continuityBuilder) {
		// transfer resources to regions
		continuityBuilder.addContextConsumer(6.34, (c) -> {
			RandomGenerator randomGenerator = c.getDataManager(StochasticsDataManager.class).getRandomGenerator();
			MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);
			ResourcesDataManager resourcesDataManager = c.getDataManager(ResourcesDataManager.class);
			List<ResourceId> resourceIds = new ArrayList<>(resourcesDataManager.getResourceIds());
			Collections.shuffle(resourceIds, new Random(randomGenerator.nextLong()));

			for (ResourceId resourceId : resourceIds) {

				List<TestMaterialsProducerId> materialsProducerIds = new ArrayList<>(
						materialsDataManager.getMaterialsProducerIds());
				Collections.shuffle(materialsProducerIds, new Random(randomGenerator.nextLong()));

				for (MaterialsProducerId materialsProducerId : materialsProducerIds) {
					TestRegionId regionId = TestRegionId.getRandomRegionId(randomGenerator);
					long amount = materialsDataManager.getMaterialsProducerResourceLevel(materialsProducerId,
							resourceId);
					amount /= 2;
					materialsDataManager.transferResourceToRegion(materialsProducerId, resourceId, regionId, amount);
				}
			}
		});
	}

	private static void reportMaterialsManagerState(RunContinuityPluginData.Builder continuityBuilder) {
		// transfer resources to regions
		continuityBuilder.addContextConsumer(7.0, (c) -> {
			MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);
			c.releaseOutput(materialsDataManager.toString());
		});
	}

	private static RunContinuityPluginData getRunContinuityPluginData() {
		RunContinuityPluginData.Builder continuityBuilder = RunContinuityPluginData.builder();

		addFirstPeople(continuityBuilder);
		startMaterialsProduction(continuityBuilder);
		createFirstBatches(continuityBuilder);
		addMoreMaterialsDetails(continuityBuilder);
		createSomeStages(continuityBuilder);
		moveSomeBatchesToStages(continuityBuilder);
		removeSomeStagesAndBatches(continuityBuilder);
		convertSomeStages(continuityBuilder);
		offerStages(continuityBuilder);
		moveBatchesToInventory(continuityBuilder);
		setSomeBatchProperties(continuityBuilder);
		setMoreProducerProperties(continuityBuilder);
		transferMaterialsBetweenBatches(continuityBuilder);
		transferAnOfferedStage(continuityBuilder);
		transferResourcesToRegions(continuityBuilder);
		reportMaterialsManagerState(continuityBuilder);

		return continuityBuilder.build();

	}

	/*
	 * Returns the default Simulation state -- time starts at zero synchronized to
	 * the beginning of the epoch.
	 */
	private static SimulationState getSimulationState() {
		return SimulationState.builder().build();
	}

	/*
	 * Returns an empty people plugin data
	 */
	private static PeoplePluginData getPeoplePluginData() {
		return PeoplePluginData.builder().build();
	}

	/*
	 * Returns the stochastics plugin data with only the main random generator.
	 */
	private static StochasticsPluginData getStochasticsPluginData(long seed) {

		WellState wellState = WellState.builder()//
				.setSeed(seed)//
				.build();

		return StochasticsPluginData.builder().setMainRNGState(wellState).build();
	}

	/*
	 * Returns the duration for a single incremented run of the simulation. This is
	 * determined by finding the last scheduled task in the run continuity plugin
	 * data and dividing that by the number of increments.
	 */
	private static double getSimulationTimeIncrement(StateData stateData, int incrementCount) {
		double maxTime = Double.NEGATIVE_INFINITY;
		for (Pair<Double, Consumer<ActorContext>> pair : stateData.runContinuityPluginData.getConsumers()) {
			Double time = pair.getFirst();
			maxTime = FastMath.max(maxTime, time);
		}

		return maxTime / incrementCount;
	}

	/*
	 * Returns the ordered state of the Materials Data Manager as a string
	 */
	private String testStateContinuity(int incrementCount, long seed) {

		/*
		 * We initialize the various plugin datas needed for the simulation
		 */
		StateData stateData = getInitialState(seed);

		// We will break up the simulation run into several runs, each lasting a
		// fixed duration
		double timeIncrement = getSimulationTimeIncrement(stateData, incrementCount);

		while (!stateData.runContinuityPluginData.allPlansComplete()) {
			stateData.haltTime += timeIncrement;
			runSimulation(stateData);
		}

		/*
		 * When the simulation has finished -- the plans contained in the run continuity
		 * plugin data have been completed, the string state of the attributes data
		 * manager is returned
		 */

		// show that the groups data manager toString() is returning something
		// reasonable
		assertNotNull(stateData.output);
		assertTrue(stateData.output.length() > 100);

		return stateData.output;
	}

	private static void runSimulation(StateData stateData) {

		// build the people plugin
		Plugin peoplePlugin = PeoplePlugin.getPeoplePlugin(stateData.peoplePluginData);

		// build the materials plugin
		Plugin materialsPlugin = MaterialsPlugin.builder().setMaterialsPluginData(stateData.materialsPluginData)
				.getMaterialsPlugin();

		// build the run continuity plugin
		Plugin runContinuityPlugin = RunContinuityPlugin.builder()//
				.setRunContinuityPluginData(stateData.runContinuityPluginData)//
				.build();

		// build the regions plugin
		Plugin regionsPlugin = RegionsPlugin.builder().setRegionsPluginData(stateData.regionsPluginData)
				.getRegionsPlugin();

		// build the resources plugin
		Plugin resourcesPlugin = ResourcesPlugin.builder().setResourcesPluginData(stateData.resourcesPluginData)
				.getResourcesPlugin();

		// build the stochastics plugin
		Plugin stochasticsPlugin = StochasticsPlugin.getStochasticsPlugin(stateData.stochasticsPluginData);

		TestOutputConsumer outputConsumer = new TestOutputConsumer();

		// execute the simulation so that it produces a people plugin data
		Simulation simulation = Simulation.builder()//
				.addPlugin(peoplePlugin)//
				.addPlugin(materialsPlugin)//
				.addPlugin(runContinuityPlugin)//
				.addPlugin(regionsPlugin)//
				.addPlugin(resourcesPlugin)//
				.addPlugin(stochasticsPlugin)//
				.setSimulationHaltTime(stateData.haltTime)//
				.setRecordState(true)//
				.setOutputConsumer(outputConsumer)//
				.setSimulationState(stateData.simulationState)//
				.build();//
		simulation.execute();

		// retrieve the people plugin data
		stateData.peoplePluginData = outputConsumer.getOutputItem(PeoplePluginData.class).get();

		// retrieve the materials plugin data
		stateData.materialsPluginData = outputConsumer.getOutputItem(MaterialsPluginData.class).get();

		// retrieve the run continuity plugin data
		stateData.runContinuityPluginData = outputConsumer.getOutputItem(RunContinuityPluginData.class).get();

		// retrieve the regions plugin data
		stateData.regionsPluginData = outputConsumer.getOutputItem(RegionsPluginData.class).get();

		// retrieve the resources plugin data
		stateData.resourcesPluginData = outputConsumer.getOutputItem(ResourcesPluginData.class).get();

		// retrieve the stochastics plugin data
		stateData.stochasticsPluginData = outputConsumer.getOutputItem(StochasticsPluginData.class).get();

		// retrieve the simulation state
		stateData.simulationState = outputConsumer.getOutputItem(SimulationState.class).get();

		Optional<String> optional = outputConsumer.getOutputItem(String.class);
		if (optional.isPresent()) {
			stateData.output = optional.get();
			// stateData.output = stateData.materialsPluginData.toString();
		}

	}
}
