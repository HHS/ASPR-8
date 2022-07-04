package plugins.materials.datamanagers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.Set;

import org.apache.commons.math3.random.RandomGenerator;
import org.junit.jupiter.api.Test;

import nucleus.DataManagerContext;
import nucleus.EventLabel;
import nucleus.EventLabeler;
import nucleus.NucleusError;
import nucleus.Plugin;
import nucleus.Simulation;
import nucleus.Simulation.Builder;
import nucleus.testsupport.testplugin.ScenarioPlanCompletionObserver;
import nucleus.testsupport.testplugin.TestActorPlan;
import nucleus.testsupport.testplugin.TestError;
import nucleus.testsupport.testplugin.TestPlugin;
import nucleus.testsupport.testplugin.TestPluginData;
import plugins.materials.MaterialsPlugin;
import plugins.materials.MaterialsPluginData;
import plugins.materials.datamangers.MaterialsDataManager;
import plugins.materials.events.BatchAdditionEvent;
import plugins.materials.events.BatchAmountUpdateEvent;
import plugins.materials.events.BatchImminentRemovalEvent;
import plugins.materials.events.BatchPropertyDefinitionEvent;
import plugins.materials.events.BatchPropertyUpdateEvent;
import plugins.materials.events.MaterialIdAdditionEvent;
import plugins.materials.events.MaterialsProducerAdditionEvent;
import plugins.materials.events.MaterialsProducerPropertyUpdateEvent;
import plugins.materials.events.MaterialsProducerResourceUpdateEvent;
import plugins.materials.events.StageAdditionEvent;
import plugins.materials.events.StageImminentRemovalEvent;
import plugins.materials.events.StageMaterialsProducerUpdateEvent;
import plugins.materials.events.StageMembershipAdditionEvent;
import plugins.materials.events.StageMembershipRemovalEvent;
import plugins.materials.events.StageOfferUpdateEvent;
import plugins.materials.support.BatchConstructionInfo;
import plugins.materials.support.BatchId;
import plugins.materials.support.BatchPropertyDefinitionInitialization;
import plugins.materials.support.BatchPropertyId;
import plugins.materials.support.MaterialId;
import plugins.materials.support.MaterialsError;
import plugins.materials.support.MaterialsProducerConstructionData;
import plugins.materials.support.MaterialsProducerId;
import plugins.materials.support.MaterialsProducerPropertyId;
import plugins.materials.support.StageId;
import plugins.materials.testsupport.MaterialsActionSupport;
import plugins.materials.testsupport.TestBatchConstructionInfo;
import plugins.materials.testsupport.TestBatchPropertyId;
import plugins.materials.testsupport.TestMaterialId;
import plugins.materials.testsupport.TestMaterialsProducerId;
import plugins.materials.testsupport.TestMaterialsProducerPropertyId;
import plugins.people.PeoplePlugin;
import plugins.people.PeoplePluginData;
import plugins.regions.RegionsPlugin;
import plugins.regions.RegionsPluginData;
import plugins.regions.support.RegionError;
import plugins.regions.support.RegionId;
import plugins.regions.testsupport.TestRegionId;
import plugins.resources.ResourcesPlugin;
import plugins.resources.ResourcesPluginData;
import plugins.resources.datamanagers.ResourcesDataManager;
import plugins.resources.events.RegionResourceUpdateEvent;
import plugins.resources.support.ResourceError;
import plugins.resources.support.ResourceId;
import plugins.resources.testsupport.TestResourceId;
import plugins.resources.testsupport.TestResourcePropertyId;
import plugins.stochastics.StochasticsDataManager;
import plugins.stochastics.StochasticsPlugin;
import plugins.stochastics.StochasticsPluginData;
import plugins.util.properties.PropertyDefinition;
import plugins.util.properties.PropertyError;
import plugins.util.properties.TimeTrackingPolicy;
import tools.annotations.UnitTest;
import tools.annotations.UnitTestConstructor;
import tools.annotations.UnitTestMethod;
import util.errors.ContractException;
import util.random.RandomGeneratorProvider;
import util.wrappers.MultiKey;
import util.wrappers.MutableDouble;
import util.wrappers.MutableLong;

@UnitTest(target = MaterialsDataManager.class)
public class AT_MaterialsDataManager {

	@Test
	@UnitTestConstructor(args = { MaterialsPluginData.class })
	public void testConstructor() {
		ContractException contractException = assertThrows(ContractException.class, () -> new MaterialsDataManager(null));
		assertEquals(MaterialsError.NULL_MATERIALS_PLUGIN_DATA, contractException.getErrorType());
	}

	@Test
	@UnitTestMethod(name = "addBatch", args = { BatchConstructionInfo.class })
	public void testAddBatch() {

		TestPluginData.Builder pluginBuilder = TestPluginData.builder();

		// create containers to hold observations
		Set<BatchId> expectedBatchObservations = new LinkedHashSet<>();
		Set<BatchId> actualBatchObservations = new LinkedHashSet<>();

		/* create an observer actor that will observe the batch creations */

		pluginBuilder.addTestActorPlan("observer", new TestActorPlan(0, (c) -> {
			c.subscribe(BatchAdditionEvent.class, (c2, e) -> {
				actualBatchObservations.add(e.getBatchId());
			});
		}));

		// create some batches and show that their various features are correct
		pluginBuilder.addTestActorPlan("actor", new TestActorPlan(1, (c) -> {
			MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);
			StochasticsDataManager stochasticsDataManager = c.getDataManager(StochasticsDataManager.class);
			RandomGenerator randomGenerator = stochasticsDataManager.getRandomGenerator();

			for (int i = 0; i < 20; i++) {
				BatchConstructionInfo.Builder builder = BatchConstructionInfo.builder();
				TestMaterialId testMaterialId = TestMaterialId.getRandomMaterialId(randomGenerator);
				builder.setMaterialsProducerId(TestMaterialsProducerId.MATERIALS_PRODUCER_1);
				builder.setMaterialId(testMaterialId);
				double amount = randomGenerator.nextDouble();
				builder.setAmount(amount);//
				for (TestBatchPropertyId testBatchPropertyId : TestBatchPropertyId.getTestBatchPropertyIds(testMaterialId)) {
					builder.setPropertyValue(testBatchPropertyId, testBatchPropertyId.getRandomPropertyValue(randomGenerator));
				}
				BatchConstructionInfo batchConstructionInfo = builder.build();//

				BatchId batchId = materialsDataManager.addBatch(batchConstructionInfo);
				assertEquals(testMaterialId, materialsDataManager.getBatchMaterial(batchId));
				assertEquals(amount, materialsDataManager.getBatchAmount(batchId));

				expectedBatchObservations.add(batchId);
			}

		}));

		/*
		 * have the observer show that the observations are properly generated
		 */

		pluginBuilder.addTestActorPlan("observer", new TestActorPlan(2, (c) -> {
			assertTrue(expectedBatchObservations.size() > 0);
			assertEquals(expectedBatchObservations, actualBatchObservations);

		}));

		TestPluginData testPluginData = pluginBuilder.build();
		Plugin testPlugin = TestPlugin.getTestPlugin(testPluginData);
		MaterialsActionSupport.testConsumers(1063265892920576062L, testPlugin);

		/* precondition test: if the materials producer is null */
		MaterialsActionSupport.testConsumer(5818867905165255006L, (c) -> {
			MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);
			ContractException contractException = assertThrows(ContractException.class, () -> {
				BatchConstructionInfo.Builder builder = BatchConstructionInfo.builder();
				builder.setMaterialId(TestMaterialId.MATERIAL_1);
				builder.setAmount(0.1234);
				BatchConstructionInfo batchConstructionInfo = builder.build();
				materialsDataManager.addBatch(batchConstructionInfo);
			});
			assertEquals(MaterialsError.NULL_MATERIALS_PRODUCER_ID, contractException.getErrorType());
		});

		/* precondition test: if the materials producer is null */
		MaterialsActionSupport.testConsumer(7126499343584962390L, (c) -> {
			MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);
			ContractException contractException = assertThrows(ContractException.class, () -> {
				BatchConstructionInfo.Builder builder = BatchConstructionInfo.builder();
				builder.setMaterialId(TestMaterialId.MATERIAL_1);
				builder.setAmount(0.1234);
				builder.setMaterialsProducerId(TestMaterialsProducerId.getUnknownMaterialsProducerId());
				BatchConstructionInfo batchConstructionInfo = builder.build();

				materialsDataManager.addBatch(batchConstructionInfo);
			});
			assertEquals(MaterialsError.UNKNOWN_MATERIALS_PRODUCER_ID, contractException.getErrorType());
		});

		/*
		 * precondition test: if the batch construction info in the event is
		 * null
		 */
		MaterialsActionSupport.testConsumer(6921778272119512748L, (c) -> {
			MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);
			ContractException contractException = assertThrows(ContractException.class, () -> {
				materialsDataManager.addBatch(null);
			});
			assertEquals(MaterialsError.NULL_BATCH_CONSTRUCTION_INFO, contractException.getErrorType());
		});

		/*
		 * precondition test: if the material id in the batch construction info
		 * is null
		 */
		MaterialsActionSupport.testConsumer(3677913497762052761L, (c) -> {
			MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);
			ContractException contractException = assertThrows(ContractException.class, () -> {
				BatchConstructionInfo.Builder builder = BatchConstructionInfo.builder();
				builder.setAmount(0.1234);
				builder.setMaterialsProducerId(TestMaterialsProducerId.MATERIALS_PRODUCER_1);
				BatchConstructionInfo batchConstructionInfo = builder.build();
				materialsDataManager.addBatch(batchConstructionInfo);

			});
			assertEquals(MaterialsError.NULL_MATERIAL_ID, contractException.getErrorType());
		});

		/*
		 * precondition test: if the material id in the batch construction info
		 * is unknown
		 */
		MaterialsActionSupport.testConsumer(6823349146270705865L, (c) -> {
			MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);
			ContractException contractException = assertThrows(ContractException.class, () -> {
				BatchConstructionInfo.Builder builder = BatchConstructionInfo.builder();
				builder.setMaterialId(TestMaterialId.getUnknownMaterialId());
				builder.setAmount(0.1234);
				builder.setMaterialsProducerId(TestMaterialsProducerId.MATERIALS_PRODUCER_3);
				BatchConstructionInfo batchConstructionInfo = builder.build();
				materialsDataManager.addBatch(batchConstructionInfo);

			});
			assertEquals(MaterialsError.UNKNOWN_MATERIAL_ID, contractException.getErrorType());
		});

		/*
		 * precondition test: if the amount in the batch construction info is
		 * not finite
		 */
		MaterialsActionSupport.testConsumer(1740687746013988916L, (c) -> {
			MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);
			ContractException contractException = assertThrows(ContractException.class, () -> {
				BatchConstructionInfo.Builder builder = BatchConstructionInfo.builder();
				builder.setMaterialId(TestMaterialId.MATERIAL_1);
				builder.setAmount(Double.POSITIVE_INFINITY);
				builder.setMaterialsProducerId(TestMaterialsProducerId.MATERIALS_PRODUCER_1);
				BatchConstructionInfo batchConstructionInfo = builder.build();
				materialsDataManager.addBatch(batchConstructionInfo);
			});
			assertEquals(MaterialsError.NON_FINITE_MATERIAL_AMOUNT, contractException.getErrorType());
		});

		/*
		 * precondition test: if the amount in the batch construction info is
		 * negative
		 */
		MaterialsActionSupport.testConsumer(3552750401177629416L, (c) -> {
			MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);
			ContractException contractException = assertThrows(ContractException.class, () -> {
				BatchConstructionInfo.Builder builder = BatchConstructionInfo.builder();
				builder.setMaterialId(TestMaterialId.MATERIAL_1);
				builder.setAmount(-1.0);
				builder.setMaterialsProducerId(TestMaterialsProducerId.MATERIALS_PRODUCER_2);
				BatchConstructionInfo batchConstructionInfo = builder.build();
				materialsDataManager.addBatch(batchConstructionInfo);
			});
			assertEquals(MaterialsError.NEGATIVE_MATERIAL_AMOUNT, contractException.getErrorType());
		});

		/*
		 * precondition test: if the batch construction info contains a null
		 * batch property id
		 */
		MaterialsActionSupport.testConsumer(2067301487300157385L, (c) -> {
			MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);
			ContractException contractException = assertThrows(ContractException.class, () -> {
				BatchConstructionInfo.Builder builder = BatchConstructionInfo.builder();
				builder.setMaterialId(TestMaterialId.MATERIAL_1);
				builder.setAmount(0.1234);
				builder.setPropertyValue(null, 15);
				builder.setMaterialsProducerId(TestMaterialsProducerId.MATERIALS_PRODUCER_1);
				BatchConstructionInfo batchConstructionInfo = builder.build();
				materialsDataManager.addBatch(batchConstructionInfo);
			});
			assertEquals(PropertyError.NULL_PROPERTY_ID, contractException.getErrorType());
		});

		/*
		 * precondition test: if the batch construction info contains an unknown
		 * batch property id
		 */
		MaterialsActionSupport.testConsumer(3866738227501386466L, (c) -> {
			MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);
			ContractException contractException = assertThrows(ContractException.class, () -> {
				BatchConstructionInfo.Builder builder = BatchConstructionInfo.builder();
				builder.setMaterialId(TestMaterialId.MATERIAL_1);
				builder.setAmount(0.1234);
				builder.setMaterialsProducerId(TestMaterialsProducerId.MATERIALS_PRODUCER_1);
				builder.setPropertyValue(TestBatchPropertyId.getUnknownBatchPropertyId(), 15);
				BatchConstructionInfo batchConstructionInfo = builder.build();
				materialsDataManager.addBatch(batchConstructionInfo);
			});
			assertEquals(PropertyError.UNKNOWN_PROPERTY_ID, contractException.getErrorType());
		});

		/*
		 * precondition test: if the batch construction info contains a batch
		 * property value that is incompatible with the corresponding property
		 * def
		 */
		MaterialsActionSupport.testConsumer(2067301487300157385L, (c) -> {
			MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);
			ContractException contractException = assertThrows(ContractException.class, () -> {
				BatchConstructionInfo.Builder builder = BatchConstructionInfo.builder();
				builder.setMaterialId(TestMaterialId.MATERIAL_1);
				builder.setAmount(0.1234);
				builder.setMaterialsProducerId(TestMaterialsProducerId.MATERIALS_PRODUCER_1);
				builder.setPropertyValue(TestBatchPropertyId.BATCH_PROPERTY_1_2_INTEGER_MUTABLE_NO_TRACK, 2.3);
				BatchConstructionInfo batchConstructionInfo = builder.build();
				materialsDataManager.addBatch(batchConstructionInfo);
			});
			assertEquals(PropertyError.INCOMPATIBLE_VALUE, contractException.getErrorType());
		});

		/*
		 * precondition test: if the batch construction info contains a batch
		 * property value that is incompatible with the corresponding property
		 * def
		 */
		MaterialsActionSupport.testConsumer(8126846490003696164L, (c) -> {
			MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);
			ContractException contractException = assertThrows(ContractException.class, () -> {
				BatchConstructionInfo.Builder builder = BatchConstructionInfo.builder();
				builder.setMaterialId(TestMaterialId.MATERIAL_1);
				builder.setAmount(0.1234);
				builder.setMaterialsProducerId(TestMaterialsProducerId.MATERIALS_PRODUCER_1);
				builder.setPropertyValue(TestBatchPropertyId.BATCH_PROPERTY_1_2_INTEGER_MUTABLE_NO_TRACK, 2.3);
				BatchConstructionInfo batchConstructionInfo = builder.build();
				materialsDataManager.addBatch(batchConstructionInfo);
			});
			assertEquals(PropertyError.INCOMPATIBLE_VALUE, contractException.getErrorType());
		});

	}

	@Test
	@UnitTestMethod(name = "addStage", args = { MaterialsProducerId.class })
	public void testAddStage() {

		TestPluginData.Builder pluginBuilder = TestPluginData.builder();
		double actionTime = 0;

		// create containers to hold observations
		Set<MultiKey> expectedObservations = new LinkedHashSet<>();
		Set<MultiKey> actualObservations = new LinkedHashSet<>();

		// have a actor observe stage creations

		pluginBuilder.addTestActorPlan("observer", new TestActorPlan(actionTime++, (c) -> {
			c.subscribe(StageAdditionEvent.class, (c2, e) -> {
				actualObservations.add(new MultiKey(c2.getTime(), e.getStageId()));
			});
		}));

		// produce stages at various times
		for (int i = 0; i < 10; i++) {
			for (TestMaterialsProducerId testMaterialsProducerId : TestMaterialsProducerId.values()) {
				pluginBuilder.addTestActorPlan("actor", new TestActorPlan(actionTime++, (c) -> {
					MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);
					StageId stageId = materialsDataManager.addStage(testMaterialsProducerId);

					// show that the stage exists and belongs to the producer
					assertTrue(materialsDataManager.stageExists(stageId));
					assertEquals(testMaterialsProducerId, materialsDataManager.getStageProducer(stageId));

					// generated expected observations
					expectedObservations.add(new MultiKey(c.getTime(), stageId));
				}));
			}
		}

		// have the observer show that the observations are correct
		pluginBuilder.addTestActorPlan("observer", new TestActorPlan(actionTime++, (c) -> {
			assertTrue(expectedObservations.size() > 0);
			assertEquals(expectedObservations, actualObservations);
		}));

		TestPluginData testPluginData = pluginBuilder.build();
		Plugin testPlugin = TestPlugin.getTestPlugin(testPluginData);
		MaterialsActionSupport.testConsumers(1344617610771747654L, testPlugin);

		/* precondition test if the materials producer is null */
		MaterialsActionSupport.testConsumer(2938510662832987631L, (c) -> {
			MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);
			ContractException contractException = assertThrows(ContractException.class, () -> materialsDataManager.addStage(null));
			assertEquals(MaterialsError.NULL_MATERIALS_PRODUCER_ID, contractException.getErrorType());
		});

		/* precondition test if the materials producer is unknown */
		MaterialsActionSupport.testConsumer(2938510662832987631L, (c) -> {
			MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);
			ContractException contractException = assertThrows(ContractException.class, () -> materialsDataManager.addStage(TestMaterialsProducerId.getUnknownMaterialsProducerId()));
			assertEquals(MaterialsError.UNKNOWN_MATERIALS_PRODUCER_ID, contractException.getErrorType());
		});

	}

	@Test
	@UnitTestMethod(name = "batchExists", args = { BatchId.class })
	public void testBatchExists() {

		TestPluginData.Builder pluginBuilder = TestPluginData.builder();

		/*
		 * Create a data structure to hold batch ids that have been removed and
		 * require flow of control to leave the actor before removal can be
		 * confirmed
		 */
		Map<TestMaterialsProducerId, Set<BatchId>> removalConfirmationBatches = new LinkedHashMap<>();
		for (TestMaterialsProducerId testMaterialsProducerId : TestMaterialsProducerId.values()) {
			removalConfirmationBatches.put(testMaterialsProducerId, new LinkedHashSet<>());

			pluginBuilder.addTestActorPlan("actor", new TestActorPlan(0, (c) -> {
				MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);
				StochasticsDataManager stochasticsDataManager = c.getDataManager(StochasticsDataManager.class);
				RandomGenerator randomGenerator = stochasticsDataManager.getRandomGenerator();
				Set<BatchId> confimationBatches = removalConfirmationBatches.get(testMaterialsProducerId);
				for (TestMaterialId testMaterialId : TestMaterialId.values()) {
					double value = randomGenerator.nextDouble() * 100;
					// add the batch an show it exists
					BatchConstructionInfo batchConstructionInfo = TestBatchConstructionInfo.getBatchConstructionInfo(testMaterialsProducerId, testMaterialId, value, randomGenerator);
					BatchId batchId = materialsDataManager.addBatch(batchConstructionInfo);
					assertTrue(materialsDataManager.batchExists(batchId));
					// remove the batch and show that it still exists
					materialsDataManager.removeBatch(batchId);
					assertTrue(materialsDataManager.batchExists(batchId));
					confimationBatches.add(batchId);
				}
				// show that null and unknown batch ids return false
				assertFalse(materialsDataManager.batchExists(null));
				assertFalse(materialsDataManager.batchExists(new BatchId(10000000)));

			}));

			// show that the batches removed above are now gone
			pluginBuilder.addTestActorPlan("actor", new TestActorPlan(0, (c) -> {
				MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);
				Set<BatchId> confimationBatches = removalConfirmationBatches.get(testMaterialsProducerId);
				for (BatchId batchId : confimationBatches) {
					assertFalse(materialsDataManager.batchExists(batchId));
				}
			}));
		}

		TestPluginData testPluginData = pluginBuilder.build();
		Plugin testPlugin = TestPlugin.getTestPlugin(testPluginData);
		MaterialsActionSupport.testConsumers(3680467733415023569L, testPlugin);
	}

	@Test
	@UnitTestMethod(name = "batchPropertyIdExists", args = { MaterialId.class, BatchPropertyId.class })
	public void testBatchPropertyIdExists() {

		MaterialsActionSupport.testConsumer(4250860228077588132L, (c) -> {
			MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);

			for (TestBatchPropertyId testBatchPropertyId : TestBatchPropertyId.values()) {
				MaterialId associatedMaterialId = testBatchPropertyId.getTestMaterialId();
				for (TestMaterialId testMaterialId : TestMaterialId.values()) {
					if (testMaterialId.equals(associatedMaterialId)) {
						assertTrue(materialsDataManager.batchPropertyIdExists(testMaterialId, testBatchPropertyId));
					} else {
						assertFalse(materialsDataManager.batchPropertyIdExists(testMaterialId, testBatchPropertyId));
					}
				}
			}

			assertFalse(materialsDataManager.batchPropertyIdExists(TestMaterialId.MATERIAL_1, null));
			assertFalse(materialsDataManager.batchPropertyIdExists(null, TestBatchPropertyId.BATCH_PROPERTY_1_1_BOOLEAN_IMMUTABLE_NO_TRACK));
			assertFalse(materialsDataManager.batchPropertyIdExists(null, null));

			// precondition tests : none

		});

	}

	@Test
	@UnitTestMethod(name = "convertStageToBatch", args = { StageId.class, MaterialId.class, double.class })
	public void testConvertStageToBatch() {
		
		TestPluginData.Builder pluginBuilder = TestPluginData.builder();
		double actionTime = 0;

		// create containers to hold observations
		Set<MultiKey> expectedObservations = new LinkedHashSet<>();
		Set<MultiKey> actualObservations = new LinkedHashSet<>();

		// have an actor observe
		pluginBuilder.addTestActorPlan("observer", new TestActorPlan(actionTime++, (c) -> {

			c.subscribe(BatchImminentRemovalEvent.class, (c2, e) -> {
				actualObservations.add(new MultiKey(c2.getTime(), e.getBatchId(), "removal"));
			});

			c.subscribe(BatchAdditionEvent.class, (c2, e) -> {
				actualObservations.add(new MultiKey(c2.getTime(), e.getBatchId(), "creation"));
			});

			c.subscribe(StageImminentRemovalEvent.class, (c2, e) -> {
				actualObservations.add(new MultiKey(c2.getTime(), e.getStageId()));
			});

		}));

		// have the producers generate batches via stage conversion
		for (TestMaterialsProducerId testMaterialsProducerId : TestMaterialsProducerId.values()) {

			List<StageId> stagesToConfirm = new ArrayList<>();
			List<BatchId> batchesToConfirm = new ArrayList<>();
			pluginBuilder.addTestActorPlan("actor", new TestActorPlan(actionTime++, (c) -> {
				MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);
				StochasticsDataManager stochasticsDataManager = c.getDataManager(StochasticsDataManager.class);
				RandomGenerator randomGenerator = stochasticsDataManager.getRandomGenerator();
				for (int i = 0; i < 50; i++) {
					StageId stageId = materialsDataManager.addStage(testMaterialsProducerId);

					MaterialId materialId;
					double amount;
					int batchCount = randomGenerator.nextInt(3);
					for (int j = 0; j < batchCount; j++) {
						materialId = TestMaterialId.getRandomMaterialId(randomGenerator);
						amount = randomGenerator.nextDouble() + 0.01;
						BatchConstructionInfo batchConstructionInfo = TestBatchConstructionInfo.getBatchConstructionInfo(testMaterialsProducerId, materialId, amount, randomGenerator);
						BatchId batchId = materialsDataManager.addBatch(batchConstructionInfo);
						materialsDataManager.moveBatchToStage(batchId, stageId);
					}
					materialId = TestMaterialId.getRandomMaterialId(randomGenerator);
					amount = randomGenerator.nextDouble() + 0.01;
					List<BatchId> stageBatches = materialsDataManager.getStageBatches(stageId);
					BatchId producedBatchId = materialsDataManager.convertStageToBatch(stageId, materialId, amount);

					// record the stages and batches that should be removed, but
					// only after the current actor activation
					stagesToConfirm.add(stageId);
					batchesToConfirm.addAll(stageBatches);

					// show that the stage was properly converted
					assertTrue(materialsDataManager.batchExists(producedBatchId));
					assertEquals(materialId, materialsDataManager.getBatchMaterial(producedBatchId));
					assertEquals(amount, materialsDataManager.getBatchAmount(producedBatchId));

					// generate the expected observations
					for (BatchId batchId : stageBatches) {
						expectedObservations.add(new MultiKey(c.getTime(), batchId, "creation"));
						expectedObservations.add(new MultiKey(c.getTime(), batchId, "removal"));
					}
					expectedObservations.add(new MultiKey(c.getTime(), producedBatchId, "creation"));
					expectedObservations.add(new MultiKey(c.getTime(), stageId));
				}
			}));

			// show that the stages and batches used to generate the new batches
			// were in fact removed
			pluginBuilder.addTestActorPlan("actor", new TestActorPlan(actionTime++, (c) -> {
				MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);
				for (StageId stageId : stagesToConfirm) {
					assertFalse(materialsDataManager.stageExists(stageId));
				}
				for (BatchId batchId : batchesToConfirm) {
					assertFalse(materialsDataManager.batchExists(batchId));
				}
			}));
		}

		// have the observer show that the correct observations were generated
		pluginBuilder.addTestActorPlan("observer", new TestActorPlan(actionTime++, (c) -> {
			assertTrue(expectedObservations.size() > 0);
			assertEquals(expectedObservations, actualObservations);
		}));

		/* precondition test: if the material id is null */
		MaterialsActionSupport.testConsumer(5594572733415411831L, (c) -> {
			MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);
			StageId stageId = materialsDataManager.addStage(TestMaterialsProducerId.MATERIALS_PRODUCER_1);
			double amount = 12.5;
			ContractException contractException = assertThrows(ContractException.class, () -> materialsDataManager.convertStageToBatch(stageId, null, amount));
			assertEquals(MaterialsError.NULL_MATERIAL_ID, contractException.getErrorType());
		});

		/* precondition test: if the material id is unknown */
		MaterialsActionSupport.testConsumer(5132874324434783837L, (c) -> {
			MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);
			StageId stageId = materialsDataManager.addStage(TestMaterialsProducerId.MATERIALS_PRODUCER_1);
			double amount = 12.5;
			ContractException contractException = assertThrows(ContractException.class, () -> materialsDataManager.convertStageToBatch(stageId, TestMaterialId.getUnknownMaterialId(), amount));
			assertEquals(MaterialsError.UNKNOWN_MATERIAL_ID, contractException.getErrorType());

		});

		/* precondition test: if the stage id is null */
		MaterialsActionSupport.testConsumer(195083586581127005L, (c) -> {
			MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);
			MaterialId materialId = TestMaterialId.MATERIAL_1;
			double amount = 12.5;
			ContractException contractException = assertThrows(ContractException.class, () -> materialsDataManager.convertStageToBatch(null, materialId, amount));
			assertEquals(MaterialsError.NULL_STAGE_ID, contractException.getErrorType());

		});

		/* precondition test: if stage id is unknown */
		MaterialsActionSupport.testConsumer(6716241372071908817L, (c) -> {
			MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);
			MaterialId materialId = TestMaterialId.MATERIAL_1;
			double amount = 12.5;
			ContractException contractException = assertThrows(ContractException.class, () -> materialsDataManager.convertStageToBatch(new StageId(10000000), materialId, amount));
			assertEquals(MaterialsError.UNKNOWN_STAGE_ID, contractException.getErrorType());
		});

		/* precondition test: if the stage is offered */
		MaterialsActionSupport.testConsumer(696988531477059866L, (c) -> {
			MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);
			StageId stageId = materialsDataManager.addStage(TestMaterialsProducerId.MATERIALS_PRODUCER_1);
			MaterialId materialId = TestMaterialId.MATERIAL_1;
			double amount = 12.5;
			materialsDataManager.setStageOfferState(stageId, true);
			ContractException contractException = assertThrows(ContractException.class, () -> materialsDataManager.convertStageToBatch(stageId, materialId, amount));
			assertEquals(MaterialsError.OFFERED_STAGE_UNALTERABLE, contractException.getErrorType());
		});

		/* precondition test: if the material amount is not finite */
		MaterialsActionSupport.testConsumer(1976879256674379671L, (c) -> {
			MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);
			StageId stageId = materialsDataManager.addStage(TestMaterialsProducerId.MATERIALS_PRODUCER_1);
			MaterialId materialId = TestMaterialId.MATERIAL_1;
			ContractException contractException = assertThrows(ContractException.class, () -> materialsDataManager.convertStageToBatch(stageId, materialId, Double.POSITIVE_INFINITY));
			assertEquals(MaterialsError.NON_FINITE_MATERIAL_AMOUNT, contractException.getErrorType());
		});

		/* precondition test: if the material amount is negative */
		MaterialsActionSupport.testConsumer(8026304657517692525L, (c) -> {
			MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);
			StageId stageId = materialsDataManager.addStage(TestMaterialsProducerId.MATERIALS_PRODUCER_1);
			MaterialId materialId = TestMaterialId.MATERIAL_1;
			ContractException contractException = assertThrows(ContractException.class, () -> materialsDataManager.convertStageToBatch(stageId, materialId, -1.0));
			assertEquals(MaterialsError.NEGATIVE_MATERIAL_AMOUNT, contractException.getErrorType());
		});

		TestPluginData testPluginData = pluginBuilder.build();
		Plugin testPlugin = TestPlugin.getTestPlugin(testPluginData);
		MaterialsActionSupport.testConsumers(855059044560726814L, testPlugin);

	}

	@Test
	@UnitTestMethod(name = "convertStageToBatch", args = { StageId.class, MaterialId.class, double.class })
	public void testStageToResourceConversionEvent() {
		TestPluginData.Builder pluginBuilder = TestPluginData.builder();
		double actionTime = 0;

		// create containers to hold observations
		Set<MultiKey> expectedObservations = new LinkedHashSet<>();
		Set<MultiKey> actualObservations = new LinkedHashSet<>();

		// have an actor observe
		pluginBuilder.addTestActorPlan("observer", new TestActorPlan(actionTime++, (c) -> {

			c.subscribe(BatchImminentRemovalEvent.class, (c2, e) -> {
				actualObservations.add(new MultiKey(c2.getTime(), e.getBatchId()));
			});

			for (TestResourceId testResourceId : TestResourceId.values()) {
				c.subscribe(MaterialsProducerResourceUpdateEvent.getEventLabelByResource(c, testResourceId), (c2, e) -> {
					actualObservations.add(new MultiKey(c2.getTime(), e.getMaterialsProducerId(), e.getResourceId(), e.getPreviousResourceLevel(), e.getCurrentResourceLevel()));
				});
			}

			c.subscribe(StageImminentRemovalEvent.class, (c2, e) -> {
				actualObservations.add(new MultiKey(c2.getTime(), e.getStageId()));
			});

		}));

		// have the producers generate resources via stage conversion
		for (TestMaterialsProducerId testMaterialsProducerId : TestMaterialsProducerId.values()) {

			List<StageId> stagesToConfirm = new ArrayList<>();
			List<BatchId> batchesToConfirm = new ArrayList<>();

			pluginBuilder.addTestActorPlan("actor", new TestActorPlan(actionTime++, (c) -> {
				MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);
				StochasticsDataManager stochasticsDataManager = c.getDataManager(StochasticsDataManager.class);
				RandomGenerator randomGenerator = stochasticsDataManager.getRandomGenerator();
				for (int i = 0; i < 50; i++) {
					StageId stageId = materialsDataManager.addStage(testMaterialsProducerId);

					MaterialId materialId;
					double amount;
					int batchCount = randomGenerator.nextInt(3);
					for (int j = 0; j < batchCount; j++) {
						materialId = TestMaterialId.getRandomMaterialId(randomGenerator);
						amount = randomGenerator.nextDouble() + 0.01;
						BatchConstructionInfo batchConstructionInfo = TestBatchConstructionInfo.getBatchConstructionInfo(testMaterialsProducerId, materialId, amount, randomGenerator);
						BatchId batchId = materialsDataManager.addBatch(batchConstructionInfo);
						materialsDataManager.moveBatchToStage(batchId, stageId);
					}
					ResourceId resourceId = TestResourceId.getRandomResourceId(randomGenerator);

					long resourceAmount = randomGenerator.nextInt(100) + 1;
					long previousResourceLevel = materialsDataManager.getMaterialsProducerResourceLevel(testMaterialsProducerId, resourceId);
					long expectedResourceLevel = previousResourceLevel + resourceAmount;
					List<BatchId> stageBatches = materialsDataManager.getStageBatches(stageId);
					materialsDataManager.convertStageToResource(stageId, resourceId, resourceAmount);

					// record the stages and batches that should be removed, but
					// only after the current actor activation
					stagesToConfirm.add(stageId);
					batchesToConfirm.addAll(stageBatches);

					// show that the stage was properly converted
					long currentResourceLevel = materialsDataManager.getMaterialsProducerResourceLevel(testMaterialsProducerId, resourceId);
					assertEquals(expectedResourceLevel, currentResourceLevel);

					// generate the expected observations
					for (BatchId batchId : stageBatches) {
						expectedObservations.add(new MultiKey(c.getTime(), batchId));
					}
					expectedObservations.add(new MultiKey(c.getTime(), testMaterialsProducerId, resourceId, previousResourceLevel, currentResourceLevel));
					expectedObservations.add(new MultiKey(c.getTime(), stageId));
				}
			}));

			// show that the stages and batches used to generate the new batches
			// were in fact removed
			pluginBuilder.addTestActorPlan("actor", new TestActorPlan(actionTime++, (c) -> {
				MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);
				for (StageId stageId : stagesToConfirm) {
					assertFalse(materialsDataManager.stageExists(stageId));
				}
				for (BatchId batchId : batchesToConfirm) {
					assertFalse(materialsDataManager.batchExists(batchId));
				}
			}));
		}

		// have the observer show that the correct observations were generated
		pluginBuilder.addTestActorPlan("observer", new TestActorPlan(actionTime++, (c) -> {
			assertTrue(expectedObservations.size() > 0);
			assertEquals(expectedObservations, actualObservations);
		}));

		TestPluginData testPluginData = pluginBuilder.build();
		Plugin testPlugin = TestPlugin.getTestPlugin(testPluginData);
		MaterialsActionSupport.testConsumers(7708324840412313909L, testPlugin);

		/* precondition test: if the resource id is null */
		MaterialsActionSupport.testConsumer(684895513326133078L, (c) -> {
			MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);
			StageId stageId = materialsDataManager.addStage(TestMaterialsProducerId.MATERIALS_PRODUCER_1);
			long amount = 15L;
			ContractException contractException = assertThrows(ContractException.class, () -> materialsDataManager.convertStageToResource(stageId, null, amount));
			assertEquals(ResourceError.NULL_RESOURCE_ID, contractException.getErrorType());
		});

		/* precondition test: if the resource id is unknown */
		MaterialsActionSupport.testConsumer(4496439455291273002L, (c) -> {
			MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);
			StageId stageId = materialsDataManager.addStage(TestMaterialsProducerId.MATERIALS_PRODUCER_1);
			long amount = 15L;
			ContractException contractException = assertThrows(ContractException.class, () -> materialsDataManager.convertStageToResource(stageId, TestResourceId.getUnknownResourceId(), amount));
			assertEquals(ResourceError.UNKNOWN_RESOURCE_ID, contractException.getErrorType());
		});

		/* precondition test: */
		MaterialsActionSupport.testConsumer(7915777615994239053L, (c) -> {
			MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);
			ResourceId resourceId = TestResourceId.RESOURCE_1;
			long amount = 15L;
			// if the stage id is null
			ContractException contractException = assertThrows(ContractException.class, () -> materialsDataManager.convertStageToResource(null, resourceId, amount));
			assertEquals(MaterialsError.NULL_STAGE_ID, contractException.getErrorType());
		});

		/* precondition test: if the stage id is unknown */
		MaterialsActionSupport.testConsumer(7939732368430243050L, (c) -> {
			MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);
			ResourceId resourceId = TestResourceId.RESOURCE_1;
			long amount = 15L;
			ContractException contractException = assertThrows(ContractException.class, () -> materialsDataManager.convertStageToResource(new StageId(10000000), resourceId, amount));
			assertEquals(MaterialsError.UNKNOWN_STAGE_ID, contractException.getErrorType());
		});

		/* precondition test: if the stage is offered */
		MaterialsActionSupport.testConsumer(5426191091405983240L, (c) -> {
			MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);
			StageId stageId = materialsDataManager.addStage(TestMaterialsProducerId.MATERIALS_PRODUCER_1);
			ResourceId resourceId = TestResourceId.RESOURCE_1;
			long amount = 15L;
			materialsDataManager.setStageOfferState(stageId, true);
			ContractException contractException = assertThrows(ContractException.class, () -> materialsDataManager.convertStageToResource(stageId, resourceId, amount));
			assertEquals(MaterialsError.OFFERED_STAGE_UNALTERABLE, contractException.getErrorType());
			materialsDataManager.setStageOfferState(stageId, false);
		});

		/* precondition test: if the the resource amount is negative */
		MaterialsActionSupport.testConsumer(677670960081138598L, (c) -> {
			MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);
			StageId stageId = materialsDataManager.addStage(TestMaterialsProducerId.MATERIALS_PRODUCER_1);
			ResourceId resourceId = TestResourceId.RESOURCE_1;
			ContractException contractException = assertThrows(ContractException.class, () -> materialsDataManager.convertStageToResource(stageId, resourceId, -1L));
			assertEquals(ResourceError.NEGATIVE_RESOURCE_AMOUNT, contractException.getErrorType());
		});

		/*
		 * precondition test: if the resource amount would cause an overflow of
		 * the materials producer's resource level
		 */
		MaterialsActionSupport.testConsumer(3420597827763142806L, (c) -> {
			MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);
			StageId stageId = materialsDataManager.addStage(TestMaterialsProducerId.MATERIALS_PRODUCER_1);
			ResourceId resourceId = TestResourceId.RESOURCE_1;
			// first ensure that there is some small amount of resource stored
			// on the producer
			StageId stageId2 = materialsDataManager.addStage(TestMaterialsProducerId.MATERIALS_PRODUCER_1);
			materialsDataManager.convertStageToResource(stageId2, resourceId, 10);
			ContractException contractException = assertThrows(ContractException.class, () -> materialsDataManager.convertStageToResource(stageId, resourceId, Long.MAX_VALUE));
			assertEquals(ResourceError.RESOURCE_ARITHMETIC_EXCEPTION, contractException.getErrorType());
		});

	}

	@Test
	@UnitTestMethod(name = "getBatchAmount", args = { BatchId.class })
	public void testGetBatchAmount() {
		
		
		TestPluginData.Builder pluginBuilder = TestPluginData.builder();

		pluginBuilder.addTestActorPlan("actor", new TestActorPlan(0, (c) -> {

			MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);
			StochasticsDataManager stochasticsDataManager = c.getDataManager(StochasticsDataManager.class);
			RandomGenerator randomGenerator = stochasticsDataManager.getRandomGenerator();

			for (int i = 0; i < 100; i++) {
				TestMaterialId testMaterialId = TestMaterialId.getRandomMaterialId(randomGenerator);
				double value = randomGenerator.nextDouble() * 100;
				BatchConstructionInfo batchConstructionInfo = TestBatchConstructionInfo.getBatchConstructionInfo(TestMaterialsProducerId.MATERIALS_PRODUCER_1, testMaterialId, value, randomGenerator);
				BatchId batchId = materialsDataManager.addBatch(batchConstructionInfo);

				// show that the batch matches the inputs
				assertEquals(value, materialsDataManager.getBatchAmount(batchId));
			}

			// precondition tests : none

			// if the batch id is null
			ContractException contractException = assertThrows(ContractException.class, () -> materialsDataManager.getBatchAmount(null));
			assertEquals(MaterialsError.NULL_BATCH_ID, contractException.getErrorType());

			// if the batch id is unknown
			contractException = assertThrows(ContractException.class, () -> materialsDataManager.getBatchAmount(new BatchId(10000000)));
			assertEquals(MaterialsError.UNKNOWN_BATCH_ID, contractException.getErrorType());

		}));

		TestPluginData testPluginData = pluginBuilder.build();
		Plugin testPlugin = TestPlugin.getTestPlugin(testPluginData);
		MaterialsActionSupport.testConsumers(1333558356470864456L, testPlugin);
	}

	@Test
	@UnitTestMethod(name = "getBatchMaterial", args = { BatchId.class })
	public void testGetBatchMaterial() {
		
		MaterialsActionSupport.testConsumer(2922188778885130752L, (c) -> {
			MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);
			StochasticsDataManager stochasticsDataManager = c.getDataManager(StochasticsDataManager.class);
			RandomGenerator randomGenerator = stochasticsDataManager.getRandomGenerator();

			for (int i = 0; i < 100; i++) {
				TestMaterialId testMaterialId = TestMaterialId.getRandomMaterialId(randomGenerator);
				double value = randomGenerator.nextDouble() * 100;
				BatchConstructionInfo batchConstructionInfo = TestBatchConstructionInfo.getBatchConstructionInfo(TestMaterialsProducerId.MATERIALS_PRODUCER_2, testMaterialId, value, randomGenerator);
				BatchId batchId = materialsDataManager.addBatch(batchConstructionInfo);

				// show that the batch matches the inputs
				assertEquals(testMaterialId, materialsDataManager.getBatchMaterial(batchId));
			}

		});

		/* precondition test: if the batch id is null */
		MaterialsActionSupport.testConsumer(8694113802920961598L, (c) -> {
			MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);
			ContractException contractException = assertThrows(ContractException.class, () -> materialsDataManager.getBatchMaterial(null));
			assertEquals(MaterialsError.NULL_BATCH_ID, contractException.getErrorType());
		});

		/* precondition test: if the batch id is unknown */
		MaterialsActionSupport.testConsumer(6524569565798029395L, (c) -> {
			MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);
			ContractException // if the batch id is unknown
			contractException = assertThrows(ContractException.class, () -> materialsDataManager.getBatchMaterial(new BatchId(10000000)));
			assertEquals(MaterialsError.UNKNOWN_BATCH_ID, contractException.getErrorType());
		});
	}

	@Test
	@UnitTestMethod(name = "getBatchProducer", args = { BatchId.class })
	public void testGetBatchProducer() {
		
		TestPluginData.Builder pluginBuilder = TestPluginData.builder();

		for (TestMaterialsProducerId testMaterialsProducerId : TestMaterialsProducerId.values()) {
			pluginBuilder.addTestActorPlan("actor", new TestActorPlan(0, (c) -> {

				MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);

				StochasticsDataManager stochasticsDataManager = c.getDataManager(StochasticsDataManager.class);
				RandomGenerator randomGenerator = stochasticsDataManager.getRandomGenerator();

				for (int i = 0; i < 10; i++) {

					TestMaterialId testMaterialId = TestMaterialId.getRandomMaterialId(randomGenerator);
					double value = randomGenerator.nextDouble() * 100;
					BatchConstructionInfo batchConstructionInfo = TestBatchConstructionInfo.getBatchConstructionInfo(testMaterialsProducerId, testMaterialId, value, randomGenerator);
					BatchId batchId = materialsDataManager.addBatch(batchConstructionInfo);

					// show that the batch matches the inputs
					assertEquals(testMaterialsProducerId, materialsDataManager.getBatchProducer(batchId));
				}

			}));
		}

		TestPluginData testPluginData = pluginBuilder.build();
		Plugin testPlugin = TestPlugin.getTestPlugin(testPluginData);
		MaterialsActionSupport.testConsumers(8873616248377004295L, testPlugin);

		/* precondition test : if the batch id is null */
		MaterialsActionSupport.testConsumer(1422948417739515067L, (c) -> {
			MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);

			ContractException contractException = assertThrows(ContractException.class, () -> materialsDataManager.getBatchProducer(null));
			assertEquals(MaterialsError.NULL_BATCH_ID, contractException.getErrorType());

		});

		/* precondition test : if the batch id is unknown */
		MaterialsActionSupport.testConsumer(6083037726892077495L, (c) -> {
			MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);

			ContractException contractException = assertThrows(ContractException.class, () -> materialsDataManager.getBatchProducer(new BatchId(10000000)));
			assertEquals(MaterialsError.UNKNOWN_BATCH_ID, contractException.getErrorType());
		});

	}

	@Test
	@UnitTestMethod(name = "getBatchPropertyDefinition", args = { MaterialId.class, BatchPropertyId.class })
	public void testGetBatchPropertyDefinition() {

		MaterialsActionSupport.testConsumer(4785939121817102392L, (c) -> {
			MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);

			for (TestMaterialId testMaterialId : TestMaterialId.values()) {

				Set<TestBatchPropertyId> testBatchPropertyIds = TestBatchPropertyId.getTestBatchPropertyIds(testMaterialId);
				for (TestBatchPropertyId testBatchPropertyId : testBatchPropertyIds) {
					PropertyDefinition expectedPropertyDefinition = testBatchPropertyId.getPropertyDefinition();
					PropertyDefinition actualPropertyDefinition = materialsDataManager.getBatchPropertyDefinition(testMaterialId, testBatchPropertyId);
					assertEquals(expectedPropertyDefinition, actualPropertyDefinition);
				}
			}

		});

		/* precondition tests if the material id is null */
		MaterialsActionSupport.testConsumer(5856664286545303775L, (c) -> {
			MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);
			TestBatchPropertyId testBatchPropertyId = TestBatchPropertyId.BATCH_PROPERTY_2_3_DOUBLE_MUTABLE_TRACK;
			ContractException contractException = assertThrows(ContractException.class, () -> materialsDataManager.getBatchPropertyDefinition(null, testBatchPropertyId));
			assertEquals(MaterialsError.NULL_MATERIAL_ID, contractException.getErrorType());
		});

		/* precondition tests if the material id is unknown */
		MaterialsActionSupport.testConsumer(3682262623372578238L, (c) -> {
			MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);
			TestBatchPropertyId testBatchPropertyId = TestBatchPropertyId.BATCH_PROPERTY_2_3_DOUBLE_MUTABLE_TRACK;
			ContractException contractException = assertThrows(ContractException.class,
					() -> materialsDataManager.getBatchPropertyDefinition(TestMaterialId.getUnknownMaterialId(), testBatchPropertyId));
			assertEquals(MaterialsError.UNKNOWN_MATERIAL_ID, contractException.getErrorType());
		});

		/* precondition tests if the batch property id is null */
		MaterialsActionSupport.testConsumer(2977320444281387466L, (c) -> {
			MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);
			ContractException contractException = assertThrows(ContractException.class, () -> materialsDataManager.getBatchPropertyDefinition(TestMaterialId.MATERIAL_1, null));
			assertEquals(PropertyError.NULL_PROPERTY_ID, contractException.getErrorType());
		});

		/* precondition tests if the batch property id is unknown */
		MaterialsActionSupport.testConsumer(712791219730643932L, (c) -> {
			MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);
			ContractException contractException = assertThrows(ContractException.class,
					() -> materialsDataManager.getBatchPropertyDefinition(TestMaterialId.MATERIAL_1, TestBatchPropertyId.getUnknownBatchPropertyId()));
			assertEquals(PropertyError.UNKNOWN_PROPERTY_ID, contractException.getErrorType());
		});

	}

	@Test
	@UnitTestMethod(name = "getBatchPropertyIds", args = { MaterialId.class })
	public void testGetBatchPropertyIds() {

		MaterialsActionSupport.testConsumer(8657082858154514151L, (c) -> {
			MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);

			for (TestMaterialId testMaterialId : TestMaterialId.values()) {
				Set<TestBatchPropertyId> expectedBatchPropertyIds = TestBatchPropertyId.getTestBatchPropertyIds(testMaterialId);
				Set<BatchPropertyId> actualBatchPropertyIds = materialsDataManager.getBatchPropertyIds(testMaterialId);
				assertEquals(expectedBatchPropertyIds, actualBatchPropertyIds);
			}

			// precondition tests

			// if the material id is null
			ContractException contractException = assertThrows(ContractException.class, () -> materialsDataManager.getBatchPropertyIds(null));
			assertEquals(MaterialsError.NULL_MATERIAL_ID, contractException.getErrorType());

			// if the material id is unknown
			contractException = assertThrows(ContractException.class, () -> materialsDataManager.getBatchPropertyIds(TestMaterialId.getUnknownMaterialId()));
			assertEquals(MaterialsError.UNKNOWN_MATERIAL_ID, contractException.getErrorType());

		});

		/* precondition test: if the material id is null */
		MaterialsActionSupport.testConsumer(6822125249787156609L, (c) -> {
			MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);
			ContractException contractException = assertThrows(ContractException.class, () -> materialsDataManager.getBatchPropertyIds(null));
			assertEquals(MaterialsError.NULL_MATERIAL_ID, contractException.getErrorType());
		});

		/* precondition test: if the material id is unknown */
		MaterialsActionSupport.testConsumer(7025275053813907413L, (c) -> {
			MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);
			ContractException contractException = assertThrows(ContractException.class, () -> materialsDataManager.getBatchPropertyIds(TestMaterialId.getUnknownMaterialId()));
			assertEquals(MaterialsError.UNKNOWN_MATERIAL_ID, contractException.getErrorType());
		});

	}

	@Test
	@UnitTestMethod(name = "getBatchPropertyTime", args = { BatchId.class, BatchPropertyId.class })
	public void testGetBatchPropertyTime() {
		TestPluginData.Builder pluginBuilder = TestPluginData.builder();

		// create a data structure to hold the assignments we expect to
		// retrieve.
		Map<MultiKey, MutableDouble> expectedAssignmentTimes = new LinkedHashMap<>();

		// create an actor

		/*
		 * Have the actor add 50 randomized batches and record the assignment
		 * times for all properties
		 */
		pluginBuilder.addTestActorPlan("actor", new TestActorPlan(0, (c) -> {
			MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);
			StochasticsDataManager stochasticsDataManager = c.getDataManager(StochasticsDataManager.class);
			RandomGenerator randomGenerator = stochasticsDataManager.getRandomGenerator();

			// create a few batches
			for (int i = 0; i < 50; i++) {
				MaterialId materialId = TestMaterialId.getRandomMaterialId(randomGenerator);
				double amount = randomGenerator.nextDouble();
				BatchConstructionInfo batchConstructionInfo = TestBatchConstructionInfo.getBatchConstructionInfo(TestMaterialsProducerId.MATERIALS_PRODUCER_3, materialId, amount, randomGenerator);
				BatchId batchId = materialsDataManager.addBatch(batchConstructionInfo);
				Set<TestBatchPropertyId> batchPropertyIds = materialsDataManager.getBatchPropertyIds(materialId);
				for (TestBatchPropertyId batchPropertyId : batchPropertyIds) {
					expectedAssignmentTimes.put(new MultiKey(batchId, batchPropertyId), new MutableDouble(c.getTime()));
				}
			}
		}));

		/*
		 * Have the actor alter about 1/3 of batch property values at 10
		 * distinct times, recording the new assignment times as we go.
		 */
		for (int i = 1; i < 10; i++) {
			double actionTime = i;
			pluginBuilder.addTestActorPlan("actor", new TestActorPlan(actionTime, (c) -> {
				MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);
				StochasticsDataManager stochasticsDataManager = c.getDataManager(StochasticsDataManager.class);
				RandomGenerator randomGenerator = stochasticsDataManager.getRandomGenerator();

				// plan several times to alter some of the batch properties

				// alter the batch properties

				List<BatchId> inventoryBatches = materialsDataManager.getInventoryBatches(TestMaterialsProducerId.MATERIALS_PRODUCER_3);
				for (BatchId batchId : inventoryBatches) {
					MaterialId materialId = materialsDataManager.getBatchMaterial(batchId);
					Set<TestBatchPropertyId> batchPropertyIds = materialsDataManager.getBatchPropertyIds(materialId);
					for (TestBatchPropertyId batchPropertyId : batchPropertyIds) {
						if (batchPropertyId.getPropertyDefinition().propertyValuesAreMutable()) {
							if (randomGenerator.nextDouble() < 0.5) {
								Object value = batchPropertyId.getRandomPropertyValue(randomGenerator);
								materialsDataManager.setBatchPropertyValue(batchId, batchPropertyId, value);
								MutableDouble mutableDouble = expectedAssignmentTimes.get(new MultiKey(batchId, batchPropertyId));
								mutableDouble.setValue(c.getTime());
							}
						}
					}
				}

			}));
		}

		/*
		 * Have the actor compare the assignment times at time = 10 to the
		 * expected values.
		 */
		pluginBuilder.addTestActorPlan("actor", new TestActorPlan(10, (c) -> {
			MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);
			StochasticsDataManager stochasticsDataManager = c.getDataManager(StochasticsDataManager.class);
			RandomGenerator randomGenerator = stochasticsDataManager.getRandomGenerator();

			for (MaterialsProducerId materialsProducerId : materialsDataManager.getMaterialsProducerIds()) {
				List<BatchId> inventoryBatches = materialsDataManager.getInventoryBatches(materialsProducerId);
				for (BatchId batchId : inventoryBatches) {
					MaterialId materialId = materialsDataManager.getBatchMaterial(batchId);
					Set<TestBatchPropertyId> batchPropertyIds = materialsDataManager.getBatchPropertyIds(materialId);
					for (TestBatchPropertyId batchPropertyId : batchPropertyIds) {
						if (randomGenerator.nextDouble() < 0.33) {
							MutableDouble mutableDouble = expectedAssignmentTimes.get(new MultiKey(batchId, batchPropertyId));
							double expectedAssignmentTime = mutableDouble.getValue();
							double actualAssignmentTime = materialsDataManager.getBatchPropertyTime(batchId, batchPropertyId);
							assertEquals(expectedAssignmentTime, actualAssignmentTime);
						}
					}
				}
			}

		}));

		TestPluginData testPluginData = pluginBuilder.build();
		Plugin testPlugin = TestPlugin.getTestPlugin(testPluginData);
		MaterialsActionSupport.testConsumers(1470041164645430466L, testPlugin);

		/* precondition test: if the batch id is null */
		MaterialsActionSupport.testConsumer(411385203720638722L, (c) -> {
			MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);
			BatchPropertyId batchPropertyId = TestBatchPropertyId.BATCH_PROPERTY_1_1_BOOLEAN_IMMUTABLE_NO_TRACK;
			ContractException contractException = assertThrows(ContractException.class, () -> materialsDataManager.getBatchPropertyTime(null, batchPropertyId));
			assertEquals(MaterialsError.NULL_BATCH_ID, contractException.getErrorType());

		});
		/* precondition test: if the batch id is unknown */
		MaterialsActionSupport.testConsumer(6352485251167807955L, (c) -> {
			MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);
			BatchPropertyId batchPropertyId = TestBatchPropertyId.BATCH_PROPERTY_1_1_BOOLEAN_IMMUTABLE_NO_TRACK;
			ContractException contractException = assertThrows(ContractException.class, () -> materialsDataManager.getBatchPropertyTime(new BatchId(100000), batchPropertyId));
			assertEquals(MaterialsError.UNKNOWN_BATCH_ID, contractException.getErrorType());
		});

		/* precondition test: if the batch property id is null */
		MaterialsActionSupport.testConsumer(3856953954489485161L, (c) -> {
			StochasticsDataManager stochasticsDataManager = c.getDataManager(StochasticsDataManager.class);
			RandomGenerator randomGenerator = stochasticsDataManager.getRandomGenerator();
			MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);
			BatchConstructionInfo batchConstructionInfo = TestBatchConstructionInfo.getBatchConstructionInfo(TestMaterialsProducerId.MATERIALS_PRODUCER_3, TestMaterialId.MATERIAL_2, 15L,randomGenerator);
			BatchId batchId = materialsDataManager.addBatch(batchConstructionInfo);
			ContractException contractException = assertThrows(ContractException.class, () -> materialsDataManager.getBatchPropertyTime(batchId, null));
			assertEquals(PropertyError.NULL_PROPERTY_ID, contractException.getErrorType());
		});

		/* precondition test: if the batch property id is unknown */
		MaterialsActionSupport.testConsumer(2978468228127714889L, (c) -> {
			StochasticsDataManager stochasticsDataManager = c.getDataManager(StochasticsDataManager.class);
			RandomGenerator randomGenerator = stochasticsDataManager.getRandomGenerator();
			MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);
			BatchConstructionInfo batchConstructionInfo = TestBatchConstructionInfo.getBatchConstructionInfo(TestMaterialsProducerId.MATERIALS_PRODUCER_1, TestMaterialId.MATERIAL_2, 65L,randomGenerator);
			BatchId batchId = materialsDataManager.addBatch(batchConstructionInfo);
			ContractException contractException = assertThrows(ContractException.class, () -> materialsDataManager.getBatchPropertyTime(batchId, TestBatchPropertyId.getUnknownBatchPropertyId()));
			assertEquals(PropertyError.UNKNOWN_PROPERTY_ID, contractException.getErrorType());
		});
	}

	@Test
	@UnitTestMethod(name = "getBatchPropertyValue", args = { BatchId.class, BatchPropertyId.class })
	public void testGetBatchPropertyValue() {
		

		TestPluginData.Builder pluginBuilder = TestPluginData.builder();
		/*
		 * create a data structure to hold the assignments we expect to
		 * retrieve.
		 */

		Map<MultiKey, Object> expectedValues = new LinkedHashMap<>();

		pluginBuilder.addTestActorPlan("actor", new TestActorPlan(0, (c) -> {

			/*
			 * Have the actor add 50 randomized batches and record the values
			 * for all properties
			 */

			MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);
			StochasticsDataManager stochasticsDataManager = c.getDataManager(StochasticsDataManager.class);
			RandomGenerator randomGenerator = stochasticsDataManager.getRandomGenerator();

			// create a few batches
			for (int i = 0; i < 50; i++) {

				MaterialId materialId = TestMaterialId.getRandomMaterialId(randomGenerator);
				double amount = randomGenerator.nextDouble();
				BatchConstructionInfo batchConstructionInfo = TestBatchConstructionInfo.getBatchConstructionInfo(TestMaterialsProducerId.MATERIALS_PRODUCER_2, materialId, amount,randomGenerator);
				BatchId batchId = materialsDataManager.addBatch(batchConstructionInfo);

				Set<TestBatchPropertyId> batchPropertyIds = materialsDataManager.getBatchPropertyIds(materialId);
				for (TestBatchPropertyId batchPropertyId : batchPropertyIds) {
					Object value = materialsDataManager.getBatchPropertyValue(batchId, batchPropertyId);
					expectedValues.put(new MultiKey(batchId, batchPropertyId), value);
				}
			}

			// alter randomly chosen batch properties

			for (int i = 0; i < 200; i++) {

				List<BatchId> inventoryBatches = materialsDataManager.getInventoryBatches(TestMaterialsProducerId.MATERIALS_PRODUCER_2);
				BatchId batchId = inventoryBatches.get(randomGenerator.nextInt(inventoryBatches.size()));
				TestMaterialId materialId = materialsDataManager.getBatchMaterial(batchId);
				TestBatchPropertyId batchPropertyId = TestBatchPropertyId.getRandomMutableBatchPropertyId(materialId, randomGenerator);
				Object value = batchPropertyId.getRandomPropertyValue(randomGenerator);
				materialsDataManager.setBatchPropertyValue(batchId, batchPropertyId, value);
				expectedValues.put(new MultiKey(batchId, batchPropertyId), value);

			}

		}));

		// have an actor show that the batches have the expected property values
		pluginBuilder.addTestActorPlan("actor", new TestActorPlan(1, (c) -> {
			MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);
			for (MaterialsProducerId materialsProducerId : materialsDataManager.getMaterialsProducerIds()) {
				List<BatchId> inventoryBatches = materialsDataManager.getInventoryBatches(materialsProducerId);
				for (BatchId batchId : inventoryBatches) {
					MaterialId materialId = materialsDataManager.getBatchMaterial(batchId);
					Set<TestBatchPropertyId> batchPropertyIds = materialsDataManager.getBatchPropertyIds(materialId);
					for (TestBatchPropertyId batchPropertyId : batchPropertyIds) {
						Object expectedValue = expectedValues.get(new MultiKey(batchId, batchPropertyId));
						Object actualValue = materialsDataManager.getBatchPropertyValue(batchId, batchPropertyId);
						assertEquals(expectedValue, actualValue);
					}
				}
			}
		}));

		TestPluginData testPluginData = pluginBuilder.build();
		Plugin testPlugin = TestPlugin.getTestPlugin(testPluginData);
		MaterialsActionSupport.testConsumers(1629075115765446254L, testPlugin);

		/* precondition test: if the batch id is null */
		MaterialsActionSupport.testConsumer(7782292483170344303L, (c) -> {
			MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);
			BatchPropertyId batchPropertyId = TestBatchPropertyId.BATCH_PROPERTY_1_1_BOOLEAN_IMMUTABLE_NO_TRACK;
			ContractException contractException = assertThrows(ContractException.class, () -> materialsDataManager.getBatchPropertyValue(null, batchPropertyId));
			assertEquals(MaterialsError.NULL_BATCH_ID, contractException.getErrorType());
		});

		/* precondition test: if the batch id is unknown */
		MaterialsActionSupport.testConsumer(2235610256211958684L, (c) -> {
			MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);
			BatchPropertyId batchPropertyId = TestBatchPropertyId.BATCH_PROPERTY_1_1_BOOLEAN_IMMUTABLE_NO_TRACK;
			ContractException contractException = assertThrows(ContractException.class, () -> materialsDataManager.getBatchPropertyValue(new BatchId(100000), batchPropertyId));
			assertEquals(MaterialsError.UNKNOWN_BATCH_ID, contractException.getErrorType());
		});

		/* precondition test: if the batch property id is null */
		MaterialsActionSupport.testConsumer(4276253162944402582L, (c) -> {
			StochasticsDataManager stochasticsDataManager = c.getDataManager(StochasticsDataManager.class);
			RandomGenerator randomGenerator = stochasticsDataManager.getRandomGenerator();
			MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);
			BatchConstructionInfo batchConstructionInfo = TestBatchConstructionInfo.getBatchConstructionInfo(TestMaterialsProducerId.MATERIALS_PRODUCER_1, TestMaterialId.MATERIAL_2, 45L,randomGenerator);
			BatchId batchId = materialsDataManager.addBatch(batchConstructionInfo);
			ContractException contractException = assertThrows(ContractException.class, () -> materialsDataManager.getBatchPropertyValue(batchId, null));
			assertEquals(PropertyError.NULL_PROPERTY_ID, contractException.getErrorType());
		});

		/* precondition test: if the batch property id is unknown */
		MaterialsActionSupport.testConsumer(211483511977100214L, (c) -> {
			StochasticsDataManager stochasticsDataManager = c.getDataManager(StochasticsDataManager.class);
			RandomGenerator randomGenerator = stochasticsDataManager.getRandomGenerator();
			MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);
			BatchConstructionInfo batchConstructionInfo = TestBatchConstructionInfo.getBatchConstructionInfo(TestMaterialsProducerId.MATERIALS_PRODUCER_1, TestMaterialId.MATERIAL_2, 45L,randomGenerator);
			BatchId batchId = materialsDataManager.addBatch(batchConstructionInfo);
			ContractException contractException = assertThrows(ContractException.class, () -> materialsDataManager.getBatchPropertyValue(batchId, TestBatchPropertyId.getUnknownBatchPropertyId()));
			assertEquals(PropertyError.UNKNOWN_PROPERTY_ID, contractException.getErrorType());
		});

	}

	@Test
	@UnitTestMethod(name = "getBatchStageId", args = { BatchId.class })
	public void testGetBatchStageId() {
		
		MaterialsActionSupport.testConsumer(472707250737446845L, (c) -> {
			StochasticsDataManager stochasticsDataManager = c.getDataManager(StochasticsDataManager.class);
			RandomGenerator randomGenerator = stochasticsDataManager.getRandomGenerator();

			MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);
			// create a stage and a batch
			StageId stageId = materialsDataManager.addStage(TestMaterialsProducerId.MATERIALS_PRODUCER_3);

			BatchConstructionInfo batchConstructionInfo = TestBatchConstructionInfo.getBatchConstructionInfo(TestMaterialsProducerId.MATERIALS_PRODUCER_3, TestMaterialId.MATERIAL_2, 4.5,randomGenerator);
			BatchId batchId = materialsDataManager.addBatch(batchConstructionInfo);

			// show that the batch does not have a stage
			Optional<StageId> optionalOwningStageId = materialsDataManager.getBatchStageId(batchId);
			assertFalse(optionalOwningStageId.isPresent());

			// put the batch onto the stage
			materialsDataManager.moveBatchToStage(batchId, stageId);

			// show that the batch is on the stage
			optionalOwningStageId = materialsDataManager.getBatchStageId(batchId);
			assertTrue(optionalOwningStageId.isPresent());
			assertEquals(stageId, optionalOwningStageId.get());

			// put the batch back into inventory
			materialsDataManager.moveBatchToInventory(batchId);

			// show that the batch is not on any stage
			optionalOwningStageId = materialsDataManager.getBatchStageId(batchId);
			assertFalse(optionalOwningStageId.isPresent());

		});
		/* precondition test: if the batch id is null */
		MaterialsActionSupport.testConsumer(8182230906627557939L, (c) -> {
			MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);
			ContractException contractException = assertThrows(ContractException.class, () -> materialsDataManager.getBatchStageId(null));
			assertEquals(MaterialsError.NULL_BATCH_ID, contractException.getErrorType());
		});

		/* precondition test: if the batch id is unknown */
		MaterialsActionSupport.testConsumer(3682958492574276233L, (c) -> {
			MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);
			ContractException contractException = assertThrows(ContractException.class, () -> materialsDataManager.getBatchStageId(new BatchId(100000)));
			assertEquals(MaterialsError.UNKNOWN_BATCH_ID, contractException.getErrorType());
		});
	}

	@Test
	@UnitTestMethod(name = "getBatchTime", args = { BatchId.class })
	public void testGetBatchTime() {
		
		TestPluginData.Builder pluginBuilder = TestPluginData.builder();
		Map<BatchId, Double> expectedBatchTimes = new LinkedHashMap<>();

		Arrays.asList(TestMaterialsProducerId.values()).stream().forEach((mpid) -> {

		});
		;

		// build batches at several times
		for (int i = 1; i <= 10; i++) {

			double planTime = i;
			pluginBuilder.addTestActorPlan("actor", new TestActorPlan(planTime, (c) -> {
				StochasticsDataManager stochasticsDataManager = c.getDataManager(StochasticsDataManager.class);
				RandomGenerator randomGenerator = stochasticsDataManager.getRandomGenerator();
				TestMaterialsProducerId materialsProducerId = TestMaterialsProducerId.getRandomMaterialsProducerId(randomGenerator);
				MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);

				// build a few batches
				int numberOfBatches = randomGenerator.nextInt(5) + 1;
				for (int j = 0; j < numberOfBatches; j++) {

					TestMaterialId materialId = TestMaterialId.getRandomMaterialId(randomGenerator);
					double amount = randomGenerator.nextDouble();
					BatchConstructionInfo batchConstructionInfo = TestBatchConstructionInfo.getBatchConstructionInfo(materialsProducerId, materialId, amount, randomGenerator);
					BatchId batchId = materialsDataManager.addBatch(batchConstructionInfo);
					expectedBatchTimes.put(batchId, c.getTime());
				}

				// show that all batches have the expected batch times

				List<BatchId> inventoryBatches = materialsDataManager.getInventoryBatches(materialsProducerId);

				for (BatchId batchId : inventoryBatches) {
					double expectedBatchTime = expectedBatchTimes.get(batchId);
					double actualBatchTime = materialsDataManager.getBatchTime(batchId);
					assertEquals(expectedBatchTime, actualBatchTime);
				}

			}));

		}
		TestPluginData testPluginData = pluginBuilder.build();
		Plugin testPlugin = TestPlugin.getTestPlugin(testPluginData);
		MaterialsActionSupport.testConsumers(8449887495666455982L, testPlugin);

		/* precondition test: if the batch id is null */
		MaterialsActionSupport.testConsumer(2942652850143901549L, (c) -> {
			MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);
			ContractException contractException = assertThrows(ContractException.class, () -> materialsDataManager.getBatchTime(null));
			assertEquals(MaterialsError.NULL_BATCH_ID, contractException.getErrorType());
		});

		/* precondition test: if the batch id is unknown */
		MaterialsActionSupport.testConsumer(8578067293001466760L, (c) -> {
			MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);
			ContractException contractException = assertThrows(ContractException.class, () -> materialsDataManager.getBatchTime(new BatchId(1000000)));
			assertEquals(MaterialsError.UNKNOWN_BATCH_ID, contractException.getErrorType());
		});
	}

	@Test
	@UnitTestMethod(name = "getInventoryBatches", args = { MaterialsProducerId.class })
	public void testGetInventoryBatches() {
		
		TestPluginData.Builder pluginBuilder = TestPluginData.builder();

		// create a data structure to hold expectd inventories
		Map<TestMaterialsProducerId, Set<BatchId>> expectedInventoryBatchesByProducer = new LinkedHashMap<>();
		for (TestMaterialsProducerId testMaterialsProducerId : TestMaterialsProducerId.values()) {
			expectedInventoryBatchesByProducer.put(testMaterialsProducerId, new LinkedHashSet<>());
		}

		for (TestMaterialsProducerId testMaterialsProducerId : TestMaterialsProducerId.values()) {

			pluginBuilder.addTestActorPlan("actor", new TestActorPlan(0, (c) -> {
				StochasticsDataManager stochasticsDataManager = c.getDataManager(StochasticsDataManager.class);
				RandomGenerator randomGenerator = stochasticsDataManager.getRandomGenerator();
				MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);
				Set<BatchId> expectedInventoryBatches = expectedInventoryBatchesByProducer.get(testMaterialsProducerId);

				// create some batches
				for (int i = 0; i < 100; i++) {
					BatchConstructionInfo batchConstructionInfo = TestBatchConstructionInfo.getBatchConstructionInfo(testMaterialsProducerId, TestMaterialId.getRandomMaterialId(randomGenerator), randomGenerator.nextInt(100),randomGenerator);
					BatchId batchId = materialsDataManager.addBatch(batchConstructionInfo);
					expectedInventoryBatches.add(batchId);
				}

				// show that the inventory batches are correct
				List<BatchId> inventory = materialsDataManager.getInventoryBatches(testMaterialsProducerId);
				Set<BatchId> actualInventoryBatches = new LinkedHashSet<>(inventory);
				assertEquals(inventory.size(), actualInventoryBatches.size());
				assertEquals(expectedInventoryBatches, actualInventoryBatches);

				// create some stages and put some of the batches onto stages
				List<BatchId> batches = new ArrayList<>(expectedInventoryBatches);
				Collections.shuffle(batches, new Random(randomGenerator.nextLong()));
				int batchIndex = 0;
				for (int i = 0; i < 5; i++) {
					StageId stageId = materialsDataManager.addStage(testMaterialsProducerId);
					for (int j = 0; j < 5; j++) {
						BatchId batchId = batches.get(batchIndex++);
						materialsDataManager.moveBatchToStage(batchId, stageId);
						expectedInventoryBatches.remove(batchId);
					}
				}

				// destroy a few inventory batches
				for (int i = 0; i < 10; i++) {
					BatchId batchId = batches.get(batchIndex++);
					materialsDataManager.removeBatch(batchId);
					expectedInventoryBatches.remove(batchId);
				}

			}));

		}

		pluginBuilder.addTestActorPlan("actor", new TestActorPlan(1, (c) -> {
			MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);

			for (TestMaterialsProducerId testMaterialsProducerId : TestMaterialsProducerId.values()) {
				Set<BatchId> expectedInventoryBatches = expectedInventoryBatchesByProducer.get(testMaterialsProducerId);

				// show that the inventory batches are correct
				List<BatchId> batchList = materialsDataManager.getInventoryBatches(testMaterialsProducerId);
				Set<BatchId> actualInventoryBatches = new LinkedHashSet<>(batchList);
				assertEquals(batchList.size(), actualInventoryBatches.size());
				assertEquals(expectedInventoryBatches, actualInventoryBatches);
			}
		}));

		TestPluginData testPluginData = pluginBuilder.build();
		Plugin testPlugin = TestPlugin.getTestPlugin(testPluginData);
		MaterialsActionSupport.testConsumers(6343917844917632364L, testPlugin);

		/* precondition tests if the materials producerId id is null */
		MaterialsActionSupport.testConsumer(6759896268818524420L, (c) -> {
			MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);
			ContractException contractException = assertThrows(ContractException.class, () -> materialsDataManager.getInventoryBatches(null));
			assertEquals(MaterialsError.NULL_MATERIALS_PRODUCER_ID, contractException.getErrorType());
		});

		/* precondition tests if the materials producerId id is unknown */
		MaterialsActionSupport.testConsumer(944257921550728616L, (c) -> {
			MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);
			ContractException contractException = assertThrows(ContractException.class, () -> materialsDataManager.getInventoryBatches(TestMaterialsProducerId.getUnknownMaterialsProducerId()));
			assertEquals(MaterialsError.UNKNOWN_MATERIALS_PRODUCER_ID, contractException.getErrorType());
		});

	}

	@Test
	@UnitTestMethod(name = "getInventoryBatchesByMaterialId", args = { MaterialsProducerId.class, MaterialId.class })
	public void testGetInventoryBatchesByMaterialId() {
		
		TestPluginData.Builder pluginBuilder = TestPluginData.builder();

		// create a data structure to hold the expected inventories
		Map<MaterialsProducerId, Map<MaterialId, Set<BatchId>>> expectedInventoryBatchesMap = new LinkedHashMap<>();
		for (TestMaterialsProducerId testMaterialsProducerId : TestMaterialsProducerId.values()) {
			Map<MaterialId, Set<BatchId>> materialToBatchesMap = new LinkedHashMap<>();
			expectedInventoryBatchesMap.put(testMaterialsProducerId, materialToBatchesMap);
			for (TestMaterialId testMaterialId : TestMaterialId.values()) {
				materialToBatchesMap.put(testMaterialId, new LinkedHashSet<>());
			}
		}
		/*
		 * Have the various material producers create batches and place them in
		 * inventory. Add some batches to stages. Destroy some of the batches.
		 */
		for (int k = 0; k < 10; k++) {

			pluginBuilder.addTestActorPlan("actor", new TestActorPlan(0, (c) -> {

				StochasticsDataManager stochasticsDataManager = c.getDataManager(StochasticsDataManager.class);
				RandomGenerator randomGenerator = stochasticsDataManager.getRandomGenerator();
				MaterialsProducerId materialsProducerId = TestMaterialsProducerId.getRandomMaterialsProducerId(randomGenerator);
				MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);
				Map<MaterialId, Set<BatchId>> materialToBatchesMap = expectedInventoryBatchesMap.get(materialsProducerId);

				// create some (100) batches
				for (int i = 0; i < 100; i++) {
					MaterialId materialId = TestMaterialId.getRandomMaterialId(randomGenerator);
					BatchConstructionInfo batchConstructionInfo = TestBatchConstructionInfo.getBatchConstructionInfo(materialsProducerId, materialId, randomGenerator.nextInt(100), randomGenerator);
					BatchId batchId = materialsDataManager.addBatch(batchConstructionInfo);
					materialToBatchesMap.get(materialId).add(batchId);
				}

				// show that the inventory batches are correct
				for (TestMaterialId testMaterialId : TestMaterialId.values()) {
					List<BatchId> batchList = materialsDataManager.getInventoryBatchesByMaterialId(materialsProducerId, testMaterialId);
					Set<BatchId> actualInventoryBatches = new LinkedHashSet<>(batchList);
					assertEquals(batchList.size(), actualInventoryBatches.size());
					Set<BatchId> expectedInventoryBatches = materialToBatchesMap.get(testMaterialId);
					assertEquals(expectedInventoryBatches, actualInventoryBatches);
				}

				/*
				 * Create some stages and put some (25) of the batches onto
				 * stages
				 * 
				 */
				List<BatchId> batches = new ArrayList<>();
				for (Set<BatchId> expectedInventoryBatches : materialToBatchesMap.values()) {
					batches.addAll(expectedInventoryBatches);
				}

				Collections.shuffle(batches, new Random(randomGenerator.nextLong()));
				int batchIndex = 0;
				for (int i = 0; i < 5; i++) {
					StageId stageId = materialsDataManager.addStage(materialsProducerId);
					for (int j = 0; j < 5; j++) {
						BatchId batchId = batches.get(batchIndex++);
						materialsDataManager.moveBatchToStage(batchId, stageId);
						MaterialId batchMaterial = materialsDataManager.getBatchMaterial(batchId);
						materialToBatchesMap.get(batchMaterial).remove(batchId);

					}
				}

				// destroy a few inventory batches
				for (int i = 0; i < 10; i++) {
					BatchId batchId = batches.get(batchIndex++);
					MaterialId batchMaterial = materialsDataManager.getBatchMaterial(batchId);
					materialsDataManager.removeBatch(batchId);
					materialToBatchesMap.get(batchMaterial).remove(batchId);
				}

			}));
		}

		pluginBuilder.addTestActorPlan("actor", new TestActorPlan(1, (c) -> {
			MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);

			// show that the inventory batches are correct
			for (TestMaterialsProducerId testMaterialsProducerId : TestMaterialsProducerId.values()) {
				Map<MaterialId, Set<BatchId>> materialToBatchesMap = expectedInventoryBatchesMap.get(testMaterialsProducerId);
				for (TestMaterialId testMaterialId : TestMaterialId.values()) {
					List<BatchId> invBatches = materialsDataManager.getInventoryBatchesByMaterialId(testMaterialsProducerId, testMaterialId);
					Set<BatchId> actualInventoryBatches = new LinkedHashSet<>(invBatches);
					assertEquals(invBatches.size(), actualInventoryBatches.size());
					Set<BatchId> expectedInventoryBatches = materialToBatchesMap.get(testMaterialId);
					assertEquals(expectedInventoryBatches, actualInventoryBatches);
				}
			}
		}));

		TestPluginData testPluginData = pluginBuilder.build();
		Plugin testPlugin = TestPlugin.getTestPlugin(testPluginData);
		MaterialsActionSupport.testConsumers(8436700054410844417L, testPlugin);

		/* precondition test: if the materials producerId id is null */
		MaterialsActionSupport.testConsumer(8066333940937253765L, (c) -> {
			MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);
			ContractException contractException = assertThrows(ContractException.class, () -> materialsDataManager.getInventoryBatchesByMaterialId(null, TestMaterialId.MATERIAL_1));
			assertEquals(MaterialsError.NULL_MATERIALS_PRODUCER_ID, contractException.getErrorType());
		});

		/* precondition test: if the materials producerId id is unknown */
		MaterialsActionSupport.testConsumer(3143917391309849287L, (c) -> {
			MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);
			ContractException contractException = assertThrows(ContractException.class,
					() -> materialsDataManager.getInventoryBatchesByMaterialId(TestMaterialsProducerId.getUnknownMaterialsProducerId(), TestMaterialId.MATERIAL_2));
			assertEquals(MaterialsError.UNKNOWN_MATERIALS_PRODUCER_ID, contractException.getErrorType());
		});

		/* precondition test: if the material id is null */
		MaterialsActionSupport.testConsumer(874196115936784556L, (c) -> {
			MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);
			ContractException contractException = assertThrows(ContractException.class, () -> materialsDataManager.getInventoryBatchesByMaterialId(TestMaterialsProducerId.MATERIALS_PRODUCER_1, null));
			assertEquals(MaterialsError.NULL_MATERIAL_ID, contractException.getErrorType());
		});

		/* precondition test: if the material id is null */
		MaterialsActionSupport.testConsumer(9112311292467047420L, (c) -> {
			MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);
			ContractException contractException = assertThrows(ContractException.class,
					() -> materialsDataManager.getInventoryBatchesByMaterialId(TestMaterialsProducerId.MATERIALS_PRODUCER_1, TestMaterialId.getUnknownMaterialId()));
			assertEquals(MaterialsError.UNKNOWN_MATERIAL_ID, contractException.getErrorType());
		});

	}

	@Test
	@UnitTestMethod(name = "getMaterialIds", args = {})
	public void testGetMaterialIds() {
		MaterialsActionSupport.testConsumer(6611654668838622496L, (c) -> {
			MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);
			assertEquals(EnumSet.allOf(TestMaterialId.class), materialsDataManager.getMaterialIds());
		});
	}

	@Test
	@UnitTestMethod(name = "getMaterialsProducerIds", args = {})
	public void testGetMaterialsProducerIds() {

		MaterialsActionSupport.testConsumer(3824970086302200338L, (c) -> {
			MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);
			assertEquals(EnumSet.allOf(TestMaterialsProducerId.class), materialsDataManager.getMaterialsProducerIds());
		});
	}

	@Test
	@UnitTestMethod(name = "materialIdExists", args = { MaterialId.class })
	public void testMaterialIdExists() {

		MaterialsActionSupport.testConsumer(6918669723394457093L, (c) -> {
			MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);

			for (TestMaterialId testMaterialId : TestMaterialId.values()) {
				assertTrue(materialsDataManager.materialIdExists(testMaterialId));
			}
			assertFalse(materialsDataManager.materialIdExists(TestMaterialId.getUnknownMaterialId()));
			assertFalse(materialsDataManager.materialIdExists(null));
		});
	}

	@Test
	@UnitTestMethod(name = "getMaterialsProducerPropertyDefinition", args = { MaterialsProducerPropertyId.class })
	public void testGetMaterialsProducerPropertyDefinition() {

		MaterialsActionSupport.testConsumer(7151961147034751776L, (c) -> {
			MaterialsDataManager materialsDaView = c.getDataManager(MaterialsDataManager.class);

			for (TestMaterialsProducerPropertyId testMaterialsProducerPropertyId : TestMaterialsProducerPropertyId.values()) {
				PropertyDefinition expectedPropertyDefinition = testMaterialsProducerPropertyId.getPropertyDefinition();
				PropertyDefinition actualPropertyDefinition = materialsDaView.getMaterialsProducerPropertyDefinition(testMaterialsProducerPropertyId);
				assertEquals(expectedPropertyDefinition, actualPropertyDefinition);
			}

		});

		/* precondition test: if the materials producer property id is null */
		MaterialsActionSupport.testConsumer(4030472148503907839L, (c) -> {
			MaterialsDataManager materialsDaView = c.getDataManager(MaterialsDataManager.class);
			ContractException contractException = assertThrows(ContractException.class, () -> materialsDaView.getMaterialsProducerPropertyDefinition(null));
			assertEquals(PropertyError.NULL_PROPERTY_ID, contractException.getErrorType());
		});

		/*
		 * precondition test: if the materials producer property id is unknown
		 */
		MaterialsActionSupport.testConsumer(863172317284141879L, (c) -> {
			MaterialsDataManager materialsDaView = c.getDataManager(MaterialsDataManager.class);
			ContractException contractException = assertThrows(ContractException.class,
					() -> materialsDaView.getMaterialsProducerPropertyDefinition(TestMaterialsProducerPropertyId.getUnknownMaterialsProducerPropertyId()));
			assertEquals(PropertyError.UNKNOWN_PROPERTY_ID, contractException.getErrorType());
		});

	}

	@Test
	@UnitTestMethod(name = "getMaterialsProducerPropertyIds", args = {})
	public void testGetMaterialsProducerPropertyIds() {
		MaterialsActionSupport.testConsumer(8718225529106870071L, (c) -> {
			MaterialsDataManager materialsDaView = c.getDataManager(MaterialsDataManager.class);
			assertEquals(EnumSet.allOf(TestMaterialsProducerPropertyId.class), materialsDaView.getMaterialsProducerPropertyIds());
		});
	}

	@Test
	@UnitTestMethod(name = "getMaterialsProducerPropertyTime", args = { MaterialsProducerId.class, MaterialsProducerPropertyId.class })
	public void testGetMaterialsProducerPropertyTime() {

		TestPluginData.Builder pluginBuilder = TestPluginData.builder();

		// create a structure to hold expected assignment times for materials
		// producer property values
		Map<MultiKey, MutableDouble> expectedTimesMap = new LinkedHashMap<>();
		for (TestMaterialsProducerId testMaterialsProducerId : TestMaterialsProducerId.values()) {
			for (TestMaterialsProducerPropertyId testMaterialsProducerPropertyId : TestMaterialsProducerPropertyId.values()) {
				expectedTimesMap.put(new MultiKey(testMaterialsProducerId, testMaterialsProducerPropertyId), new MutableDouble());
			}
		}

		// determine a reasonable number of changes per time
		int propertyChangeCount = TestMaterialsProducerId.size() * TestMaterialsProducerPropertyId.size() / 10;

		// update several random property values at various times
		for (int i = 0; i < 10; i++) {
			pluginBuilder.addTestActorPlan("actor", new TestActorPlan(0, (c) -> {
				StochasticsDataManager stochasticsDataManager = c.getDataManager(StochasticsDataManager.class);
				RandomGenerator randomGenerator = stochasticsDataManager.getRandomGenerator();
				MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);

				for (int j = 0; j < propertyChangeCount; j++) {
					TestMaterialsProducerId materialsProducerId = TestMaterialsProducerId.getRandomMaterialsProducerId(randomGenerator);
					TestMaterialsProducerPropertyId materialsProducerPropertyId = TestMaterialsProducerPropertyId.getRandomMutableMaterialsProducerPropertyId(randomGenerator);
					Object propertyValue = materialsProducerPropertyId.getRandomPropertyValue(randomGenerator);
					materialsDataManager.setMaterialsProducerPropertyValue(materialsProducerId, materialsProducerPropertyId, propertyValue);
					MultiKey multiKey = new MultiKey(materialsProducerId, materialsProducerPropertyId);
					expectedTimesMap.get(multiKey).setValue(c.getTime());
				}
			}));
		}

		pluginBuilder.addTestActorPlan("actor", new TestActorPlan(10, (c) -> {
			MaterialsDataManager materialsDaView = c.getDataManager(MaterialsDataManager.class);
			for (TestMaterialsProducerId testMaterialsProducerId : TestMaterialsProducerId.values()) {
				for (TestMaterialsProducerPropertyId testMaterialsProducerPropertyId : TestMaterialsProducerPropertyId.values()) {
					MultiKey multiKey = new MultiKey(testMaterialsProducerId, testMaterialsProducerPropertyId);
					double expectedTime = expectedTimesMap.get(multiKey).getValue();
					double actualTime = materialsDaView.getMaterialsProducerPropertyTime(testMaterialsProducerId, testMaterialsProducerPropertyId);
					assertEquals(expectedTime, actualTime);
				}
			}
		}));

		TestPluginData testPluginData = pluginBuilder.build();
		Plugin testPlugin = TestPlugin.getTestPlugin(testPluginData);
		MaterialsActionSupport.testConsumers(4362229716953652532L, testPlugin);

		/* precondition test: if the materials producerId id is null */
		MaterialsActionSupport.testConsumer(8047663013308359028L, (c) -> {
			MaterialsDataManager materialsDaView = c.getDataManager(MaterialsDataManager.class);
			ContractException contractException = assertThrows(ContractException.class,
					() -> materialsDaView.getMaterialsProducerPropertyTime(null, TestMaterialsProducerPropertyId.MATERIALS_PRODUCER_PROPERTY_4_BOOLEAN_MUTABLE_TRACK));
			assertEquals(MaterialsError.NULL_MATERIALS_PRODUCER_ID, contractException.getErrorType());
		});

		/* precondition test: if the materials producerId id is unknown */
		MaterialsActionSupport.testConsumer(7076209560671384217L, (c) -> {
			MaterialsDataManager materialsDaView = c.getDataManager(MaterialsDataManager.class);
			ContractException contractException = assertThrows(ContractException.class, () -> materialsDaView.getMaterialsProducerPropertyTime(TestMaterialsProducerId.getUnknownMaterialsProducerId(),
					TestMaterialsProducerPropertyId.MATERIALS_PRODUCER_PROPERTY_4_BOOLEAN_MUTABLE_TRACK));
			assertEquals(MaterialsError.UNKNOWN_MATERIALS_PRODUCER_ID, contractException.getErrorType());
		});

		/* precondition test: if the materials producerId property id is null */
		MaterialsActionSupport.testConsumer(8444324674368897195L, (c) -> {
			MaterialsDataManager materialsDaView = c.getDataManager(MaterialsDataManager.class);
			ContractException contractException = assertThrows(ContractException.class, () -> materialsDaView.getMaterialsProducerPropertyTime(TestMaterialsProducerId.MATERIALS_PRODUCER_1, null));
			assertEquals(PropertyError.NULL_PROPERTY_ID, contractException.getErrorType());
		});

		/*
		 * precondition test: if the materials producerId property id is unknown
		 */
		MaterialsActionSupport.testConsumer(3195486517854831744L, (c) -> {
			MaterialsDataManager materialsDaView = c.getDataManager(MaterialsDataManager.class);
			ContractException contractException = assertThrows(ContractException.class,
					() -> materialsDaView.getMaterialsProducerPropertyTime(TestMaterialsProducerId.MATERIALS_PRODUCER_1, TestMaterialsProducerPropertyId.getUnknownMaterialsProducerPropertyId()));
			assertEquals(PropertyError.UNKNOWN_PROPERTY_ID, contractException.getErrorType());
		});

	}

	@Test
	@UnitTestMethod(name = "getMaterialsProducerPropertyValue", args = { MaterialsProducerId.class, MaterialsProducerPropertyId.class })
	public void testGetMaterialsProducerPropertyValue() {

		TestPluginData.Builder pluginBuilder = TestPluginData.builder();

		// create a structure to hold expected assignment times for materials
		// producer property values
		Map<MultiKey, Object> expectedValuesMap = new LinkedHashMap<>();

		// initialize the materials data manager
		pluginBuilder.addTestActorPlan("actor", new TestActorPlan(0, (c) -> {
			MaterialsDataManager materialsDaView = c.getDataManager(MaterialsDataManager.class);

			for (TestMaterialsProducerId testMaterialsProducerId : TestMaterialsProducerId.values()) {
				for (TestMaterialsProducerPropertyId testMaterialsProducerPropertyId : TestMaterialsProducerPropertyId.values()) {
					MultiKey multiKey = new MultiKey(testMaterialsProducerId, testMaterialsProducerPropertyId);
					Object value = materialsDaView.getMaterialsProducerPropertyValue(testMaterialsProducerId, testMaterialsProducerPropertyId);
					expectedValuesMap.put(multiKey, value);
				}
			}

		}));

		// determine a reasonable number of changes per time
		int propertyChangeCount = TestMaterialsProducerId.size() * TestMaterialsProducerPropertyId.size() / 10;

		// update several random property values at various times
		for (int i = 0; i < 10; i++) {
			pluginBuilder.addTestActorPlan("actor", new TestActorPlan(0, (c) -> {
				MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);
				StochasticsDataManager stochasticsDataManager = c.getDataManager(StochasticsDataManager.class);
				RandomGenerator randomGenerator = stochasticsDataManager.getRandomGenerator();
				for (int j = 0; j < propertyChangeCount; j++) {
					TestMaterialsProducerId materialsProducerId = TestMaterialsProducerId.getRandomMaterialsProducerId(randomGenerator);
					TestMaterialsProducerPropertyId materialsProducerPropertyId = TestMaterialsProducerPropertyId.getRandomMutableMaterialsProducerPropertyId(randomGenerator);
					Object propertyValue = materialsProducerPropertyId.getRandomPropertyValue(randomGenerator);
					materialsDataManager.setMaterialsProducerPropertyValue(materialsProducerId, materialsProducerPropertyId, propertyValue);
					MultiKey multiKey = new MultiKey(materialsProducerId, materialsProducerPropertyId);
					expectedValuesMap.put(multiKey, propertyValue);
				}
			}));
		}

		pluginBuilder.addTestActorPlan("actor", new TestActorPlan(10, (c) -> {
			MaterialsDataManager materialsDaView = c.getDataManager(MaterialsDataManager.class);
			for (TestMaterialsProducerId testMaterialsProducerId : TestMaterialsProducerId.values()) {
				for (TestMaterialsProducerPropertyId testMaterialsProducerPropertyId : TestMaterialsProducerPropertyId.values()) {
					MultiKey multiKey = new MultiKey(testMaterialsProducerId, testMaterialsProducerPropertyId);
					Object expectedValue = expectedValuesMap.get(multiKey);
					Object actualValue = materialsDaView.getMaterialsProducerPropertyValue(testMaterialsProducerId, testMaterialsProducerPropertyId);
					assertEquals(expectedValue, actualValue);
				}
			}
		}));

		TestPluginData testPluginData = pluginBuilder.build();
		Plugin testPlugin = TestPlugin.getTestPlugin(testPluginData);
		MaterialsActionSupport.testConsumers(3587272435527239583L, testPlugin);

		/* precondition test: if the materials producerId id is null */
		MaterialsActionSupport.testConsumer(4247143641356704364L, (c) -> {
			MaterialsDataManager materialsDaView = c.getDataManager(MaterialsDataManager.class);
			ContractException contractException = assertThrows(ContractException.class,
					() -> materialsDaView.getMaterialsProducerPropertyValue(null, TestMaterialsProducerPropertyId.MATERIALS_PRODUCER_PROPERTY_4_BOOLEAN_MUTABLE_TRACK));
			assertEquals(MaterialsError.NULL_MATERIALS_PRODUCER_ID, contractException.getErrorType());
		});

		/* precondition test: if the materials producerId id is unknown */
		MaterialsActionSupport.testConsumer(7731689857034028615L, (c) -> {
			MaterialsDataManager materialsDaView = c.getDataManager(MaterialsDataManager.class);
			ContractException contractException = assertThrows(ContractException.class, () -> materialsDaView.getMaterialsProducerPropertyValue(TestMaterialsProducerId.getUnknownMaterialsProducerId(),
					TestMaterialsProducerPropertyId.MATERIALS_PRODUCER_PROPERTY_4_BOOLEAN_MUTABLE_TRACK));
			assertEquals(MaterialsError.UNKNOWN_MATERIALS_PRODUCER_ID, contractException.getErrorType());
		});

		/* precondition test: if the materials producerId property id is null */
		MaterialsActionSupport.testConsumer(1004792420489047936L, (c) -> {
			MaterialsDataManager materialsDaView = c.getDataManager(MaterialsDataManager.class);
			ContractException contractException = assertThrows(ContractException.class, () -> materialsDaView.getMaterialsProducerPropertyValue(TestMaterialsProducerId.MATERIALS_PRODUCER_1, null));
			assertEquals(PropertyError.NULL_PROPERTY_ID, contractException.getErrorType());
		});

		/*
		 * precondition test: if the materials producerId property id is unknown
		 */
		MaterialsActionSupport.testConsumer(133851619411326116L, (c) -> {
			MaterialsDataManager materialsDaView = c.getDataManager(MaterialsDataManager.class);
			ContractException contractException = assertThrows(ContractException.class,
					() -> materialsDaView.getMaterialsProducerPropertyValue(TestMaterialsProducerId.MATERIALS_PRODUCER_1, TestMaterialsProducerPropertyId.getUnknownMaterialsProducerPropertyId()));
			assertEquals(PropertyError.UNKNOWN_PROPERTY_ID, contractException.getErrorType());
		});
	}

	@Test
	@UnitTestMethod(name = "getMaterialsProducerResourceLevel", args = { MaterialsProducerId.class, ResourceId.class })
	public void testGetMaterialsProducerResourceLevel() {

		TestPluginData.Builder pluginBuilder = TestPluginData.builder();

		// create a structure to hold expected resource levels for producers
		Map<MultiKey, MutableLong> expectedLevelsMap = new LinkedHashMap<>();
		for (TestMaterialsProducerId testMaterialsProducerId : TestMaterialsProducerId.values()) {
			for (TestResourceId testResourceId : TestResourceId.values()) {
				expectedLevelsMap.put(new MultiKey(testMaterialsProducerId, testResourceId), new MutableLong());
			}
		}

		// determine a reasonable number of changes per time
		int resourceLevelChangeCount = TestMaterialsProducerId.size() * TestResourceId.size() / 10;

		// update several random resource levels values at various times
		for (TestMaterialsProducerId testMaterialsProducerId : TestMaterialsProducerId.values()) {
			pluginBuilder.addTestActorPlan("actor", new TestActorPlan(0, (c) -> {
				StochasticsDataManager stochasticsDataManager = c.getDataManager(StochasticsDataManager.class);
				RandomGenerator randomGenerator = stochasticsDataManager.getRandomGenerator();
				MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);

				for (int j = 0; j < resourceLevelChangeCount; j++) {

					StageId stageId = materialsDataManager.addStage(testMaterialsProducerId);
					TestResourceId testResourceId = TestResourceId.getRandomResourceId(randomGenerator);
					Long resourceLevel = (long) (randomGenerator.nextInt(100) + 1);
					materialsDataManager.convertStageToResource(stageId, testResourceId, resourceLevel);
					MultiKey multiKey = new MultiKey(testMaterialsProducerId, testResourceId);
					MutableLong mutableLong = expectedLevelsMap.get(multiKey);
					mutableLong.increment(resourceLevel);
				}
			}));
		}

		pluginBuilder.addTestActorPlan("actor", new TestActorPlan(10, (c) -> {
			MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);
			for (TestMaterialsProducerId testMaterialsProducerId : TestMaterialsProducerId.values()) {
				for (TestResourceId testResourceId : TestResourceId.values()) {
					MultiKey multiKey = new MultiKey(testMaterialsProducerId, testResourceId);
					long expectedLevel = expectedLevelsMap.get(multiKey).getValue();
					long actualLevel = materialsDataManager.getMaterialsProducerResourceLevel(testMaterialsProducerId, testResourceId);
					assertEquals(expectedLevel, actualLevel);
				}
			}
		}));

		TestPluginData testPluginData = pluginBuilder.build();
		Plugin testPlugin = TestPlugin.getTestPlugin(testPluginData);
		MaterialsActionSupport.testConsumers(1633676078121550637L, testPlugin);

		/* precondition test: if the materials producerId id is null */
		MaterialsActionSupport.testConsumer(11082575022266925L, (c) -> {
			MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);
			ContractException contractException = assertThrows(ContractException.class, () -> materialsDataManager.getMaterialsProducerResourceLevel(null, TestResourceId.RESOURCE_1));
			assertEquals(MaterialsError.NULL_MATERIALS_PRODUCER_ID, contractException.getErrorType());
		});

		/* precondition test: if the materials producerId id is unknown */
		MaterialsActionSupport.testConsumer(6362058221207452078L, (c) -> {
			MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);
			ContractException contractException = assertThrows(ContractException.class,
					() -> materialsDataManager.getMaterialsProducerResourceLevel(TestMaterialsProducerId.getUnknownMaterialsProducerId(), TestResourceId.RESOURCE_1));
			assertEquals(MaterialsError.UNKNOWN_MATERIALS_PRODUCER_ID, contractException.getErrorType());
		});

		/* precondition test: if the resource id is null */
		MaterialsActionSupport.testConsumer(7531288497048301736L, (c) -> {
			MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);
			ContractException contractException = assertThrows(ContractException.class,
					() -> materialsDataManager.getMaterialsProducerResourceLevel(TestMaterialsProducerId.MATERIALS_PRODUCER_1, null));
			assertEquals(ResourceError.NULL_RESOURCE_ID, contractException.getErrorType());
		});

		/* precondition test: if the resource id is unknown */
		MaterialsActionSupport.testConsumer(2745862264533327311L, (c) -> {
			MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);
			ContractException contractException = assertThrows(ContractException.class,
					() -> materialsDataManager.getMaterialsProducerResourceLevel(TestMaterialsProducerId.MATERIALS_PRODUCER_1, TestResourceId.getUnknownResourceId()));
			assertEquals(ResourceError.UNKNOWN_RESOURCE_ID, contractException.getErrorType());
		});
	}

	@Test
	@UnitTestMethod(name = "getMaterialsProducerResourceTime", args = { MaterialsProducerId.class, ResourceId.class })
	public void testGetMaterialsProducerResourceTime() {

		TestPluginData.Builder pluginBuilder = TestPluginData.builder();

		// create a structure to hold expected assignment times for producer
		// resource levels
		Map<MultiKey, MutableDouble> expectedTimesMap = new LinkedHashMap<>();
		for (TestMaterialsProducerId testMaterialsProducerId : TestMaterialsProducerId.values()) {
			for (TestResourceId testResourceId : TestResourceId.values()) {
				expectedTimesMap.put(new MultiKey(testMaterialsProducerId, testResourceId), new MutableDouble());
			}
		}

		// update several random resource levels at various times
		double plantime = 0;
		for (TestMaterialsProducerId testMaterialsProducerId : TestMaterialsProducerId.values()) {
			pluginBuilder.addTestActorPlan("actor", new TestActorPlan(plantime++, (c) -> {
				StochasticsDataManager stochasticsDataManager = c.getDataManager(StochasticsDataManager.class);
				RandomGenerator randomGenerator = stochasticsDataManager.getRandomGenerator();
				MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);
				for (int j = 0; j < 5; j++) {
					TestResourceId testResourceId = TestResourceId.getRandomResourceId(randomGenerator);
					Integer resourceLevel = randomGenerator.nextInt(100) + 1;
					StageId stageId = materialsDataManager.addStage(testMaterialsProducerId);
					materialsDataManager.convertStageToResource(stageId, testResourceId, resourceLevel);
					MultiKey multiKey = new MultiKey(testMaterialsProducerId, testResourceId);
					expectedTimesMap.get(multiKey).setValue(c.getTime());
				}
			}));
		}

		pluginBuilder.addTestActorPlan("actor", new TestActorPlan(10, (c) -> {
			MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);
			for (TestMaterialsProducerId testMaterialsProducerId : TestMaterialsProducerId.values()) {
				for (TestResourceId testResourceId : TestResourceId.values()) {
					MultiKey multiKey = new MultiKey(testMaterialsProducerId, testResourceId);
					double expectedTime = expectedTimesMap.get(multiKey).getValue();
					double actualTime = materialsDataManager.getMaterialsProducerResourceTime(testMaterialsProducerId, testResourceId);
					assertEquals(expectedTime, actualTime);
				}
			}
		}));

		TestPluginData testPluginData = pluginBuilder.build();
		Plugin testPlugin = TestPlugin.getTestPlugin(testPluginData);
		MaterialsActionSupport.testConsumers(102509608008549692L, testPlugin);

		/* precondition test: if the materials producerId id is null */
		MaterialsActionSupport.testConsumer(5802266741184871831L, (c) -> {
			MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);
			ContractException contractException = assertThrows(ContractException.class, () -> materialsDataManager.getMaterialsProducerResourceTime(null, TestResourceId.RESOURCE_1));
			assertEquals(MaterialsError.NULL_MATERIALS_PRODUCER_ID, contractException.getErrorType());
		});

		/* precondition test: if the materials producerId id is unknown */
		MaterialsActionSupport.testConsumer(4254859094661916110L, (c) -> {
			MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);
			ContractException contractException = assertThrows(ContractException.class,
					() -> materialsDataManager.getMaterialsProducerResourceTime(TestMaterialsProducerId.getUnknownMaterialsProducerId(), TestResourceId.RESOURCE_1));
			assertEquals(MaterialsError.UNKNOWN_MATERIALS_PRODUCER_ID, contractException.getErrorType());
		});

		/* precondition test: if the resource id is null */
		MaterialsActionSupport.testConsumer(3911919336328300644L, (c) -> {
			MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);
			ContractException contractException = assertThrows(ContractException.class,
					() -> materialsDataManager.getMaterialsProducerResourceTime(TestMaterialsProducerId.MATERIALS_PRODUCER_1, null));
			assertEquals(ResourceError.NULL_RESOURCE_ID, contractException.getErrorType());
		});

		/* precondition test: if the resource id is unknown */
		MaterialsActionSupport.testConsumer(2654216033515042203L, (c) -> {
			MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);
			ContractException contractException = assertThrows(ContractException.class,
					() -> materialsDataManager.getMaterialsProducerResourceTime(TestMaterialsProducerId.MATERIALS_PRODUCER_1, TestResourceId.getUnknownResourceId()));
			assertEquals(ResourceError.UNKNOWN_RESOURCE_ID, contractException.getErrorType());
		});
	}

	@Test
	@UnitTestMethod(name = "getOfferedStages", args = { MaterialsProducerId.class })
	public void testGetOfferedStages() {

		TestPluginData.Builder pluginBuilder = TestPluginData.builder();

		for (TestMaterialsProducerId testMaterialsProducerId : TestMaterialsProducerId.values()) {

			pluginBuilder.addTestActorPlan("actor", new TestActorPlan(0, (c) -> {
				MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);
				StochasticsDataManager stochasticsDataManager = c.getDataManager(StochasticsDataManager.class);
				RandomGenerator randomGenerator = stochasticsDataManager.getRandomGenerator();

				/*
				 * create several stages and place about half of them into the
				 * offer state
				 */
				Set<StageId> expectedStageOffers = new LinkedHashSet<>();

				for (int i = 0; i < 100; i++) {
					StageId stageId = materialsDataManager.addStage(testMaterialsProducerId);
					if (randomGenerator.nextBoolean()) {
						expectedStageOffers.add(stageId);
						materialsDataManager.setStageOfferState(stageId, true);
					}
				}

				List<StageId> stages = materialsDataManager.getOfferedStages(testMaterialsProducerId);
				Set<StageId> actualStages = new LinkedHashSet<>(stages);
				assertEquals(stages.size(), actualStages.size());
				assertEquals(expectedStageOffers, actualStages);

			}));
		}

		TestPluginData testPluginData = pluginBuilder.build();
		Plugin testPlugin = TestPlugin.getTestPlugin(testPluginData);
		MaterialsActionSupport.testConsumers(7995017020582510238L, testPlugin);

		/* precondition tests if the materials producerId id is null */
		MaterialsActionSupport.testConsumer(1728234953072549300L, (c) -> {
			MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);
			ContractException contractException = assertThrows(ContractException.class, () -> materialsDataManager.getOfferedStages(null));
			assertEquals(MaterialsError.NULL_MATERIALS_PRODUCER_ID, contractException.getErrorType());
		});

		/* precondition tests if the materials producerId id is unknown */
		MaterialsActionSupport.testConsumer(809512240800144004L, (c) -> {
			MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);
			ContractException contractException = assertThrows(ContractException.class, () -> materialsDataManager.getOfferedStages(TestMaterialsProducerId.getUnknownMaterialsProducerId()));
			assertEquals(MaterialsError.UNKNOWN_MATERIALS_PRODUCER_ID, contractException.getErrorType());
		});

	}

	@Test
	@UnitTestMethod(name = "getStageBatches", args = { StageId.class })
	public void testGetStageBatches() {
		TestPluginData.Builder pluginBuilder = TestPluginData.builder();

		// create a structure to hold expectations
		Map<StageId, Set<BatchId>> expectedStageBatches = new LinkedHashMap<>();

		// have each of the materials producers create and stage some batches
		for (TestMaterialsProducerId testMaterialsProducerId : TestMaterialsProducerId.values()) {

			pluginBuilder.addTestActorPlan("actor", new TestActorPlan(0, (c) -> {
				MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);
				StochasticsDataManager stochasticsDataManager = c.getDataManager(StochasticsDataManager.class);
				RandomGenerator randomGenerator = stochasticsDataManager.getRandomGenerator();

				// create some stages and batches
				for (int i = 0; i < 10; i++) {
					StageId stageId = materialsDataManager.addStage(testMaterialsProducerId);
					int batchCount = randomGenerator.nextInt(3) + 1;
					Set<BatchId> batches = new LinkedHashSet<>();
					for (int j = 0; j < batchCount; j++) {
						BatchConstructionInfo batchConstructionInfo = TestBatchConstructionInfo.getBatchConstructionInfo(testMaterialsProducerId, TestMaterialId.getRandomMaterialId(randomGenerator), randomGenerator.nextInt(100),randomGenerator);
						BatchId batchId = materialsDataManager.addBatch(batchConstructionInfo);
						batches.add(batchId);
						materialsDataManager.moveBatchToStage(batchId, stageId);
					}
					expectedStageBatches.put(stageId, batches);
				}
			}));
		}
		// have an actor show that the batches are staged as expected

		pluginBuilder.addTestActorPlan("actor", new TestActorPlan(1, (c) -> {
			MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);
			Map<StageId, Set<BatchId>> actualStageBatches = new LinkedHashMap<>();
			for (MaterialsProducerId materialsProducerId : materialsDataManager.getMaterialsProducerIds()) {
				List<StageId> stageIds = materialsDataManager.getStages(materialsProducerId);
				for (StageId stageId : stageIds) {
					actualStageBatches.put(stageId, new LinkedHashSet<>(materialsDataManager.getStageBatches(stageId)));
				}
			}

			assertEquals(expectedStageBatches, actualStageBatches);
		}));

		TestPluginData testPluginData = pluginBuilder.build();
		Plugin testPlugin = TestPlugin.getTestPlugin(testPluginData);
		MaterialsActionSupport.testConsumers(3682458920522952415L, testPlugin);

		/* precondition tests if the stage id is null */
		MaterialsActionSupport.testConsumer(7434749084817685354L, (c) -> {
			MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);
			ContractException contractException = assertThrows(ContractException.class, () -> materialsDataManager.getStageBatches(null));
			assertEquals(MaterialsError.NULL_STAGE_ID, contractException.getErrorType());
		});

		/* precondition tests if the stage id is unknown */
		MaterialsActionSupport.testConsumer(8988415576850624232L, (c) -> {
			MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);
			ContractException contractException = assertThrows(ContractException.class, () -> materialsDataManager.getStageBatches(new StageId(10000000)));
			assertEquals(MaterialsError.UNKNOWN_STAGE_ID, contractException.getErrorType());
		});

	}

	@Test
	@UnitTestMethod(name = "getStageBatchesByMaterialId", args = { StageId.class, MaterialId.class })
	public void testGetStageBatchesByMaterialId() {
	
		TestPluginData.Builder pluginBuilder = TestPluginData.builder();

		// build a structure to hold the batches expected per stage and
		// material id
		Map<StageId, Map<MaterialId, Set<BatchId>>> expectedStageBatches = new LinkedHashMap<>();

		// have each of the materials producers create and stage some batches
		for (TestMaterialsProducerId testMaterialsProducerId : TestMaterialsProducerId.values()) {

			pluginBuilder.addTestActorPlan("actor", new TestActorPlan(0, (c) -> {
				MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);

				StochasticsDataManager stochasticsDataManager = c.getDataManager(StochasticsDataManager.class);
				RandomGenerator randomGenerator = stochasticsDataManager.getRandomGenerator();

				// create some stages and batches
				for (int i = 0; i < 10; i++) {
					StageId stageId = materialsDataManager.addStage(testMaterialsProducerId);
					int batchCount = randomGenerator.nextInt(10) + 1;
					Map<MaterialId, Set<BatchId>> map = new LinkedHashMap<>();
					for (int j = 0; j < batchCount; j++) {
						TestMaterialId materialId = TestMaterialId.getRandomMaterialId(randomGenerator);
						Set<BatchId> batches = map.get(materialId);
						if (batches == null) {
							batches = new LinkedHashSet<>();
							map.put(materialId, batches);
						}
						BatchConstructionInfo batchConstructionInfo = TestBatchConstructionInfo.getBatchConstructionInfo(testMaterialsProducerId, materialId, randomGenerator.nextInt(100), randomGenerator);
						BatchId batchId = materialsDataManager.addBatch(batchConstructionInfo);
						batches.add(batchId);
						materialsDataManager.moveBatchToStage(batchId, stageId);
					}
					expectedStageBatches.put(stageId, map);
				}

			}));
		}

		// have an actor show that the batches are staged as expected
		pluginBuilder.addTestActorPlan("actor", new TestActorPlan(1, (c) -> {

			MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);

			/*
			 * gather the actual stage batches in a structure identical to the
			 * expected values
			 */
			Map<StageId, Map<MaterialId, Set<BatchId>>> actualStageBatches = new LinkedHashMap<>();
			for (MaterialsProducerId materialsProducerId : materialsDataManager.getMaterialsProducerIds()) {
				List<StageId> stageIds = materialsDataManager.getStages(materialsProducerId);

				for (StageId stageId : stageIds) {
					Map<MaterialId, Set<BatchId>> map = new LinkedHashMap<>();
					actualStageBatches.put(stageId, map);
					List<BatchId> stageBatches = materialsDataManager.getStageBatches(stageId);
					for (BatchId batchId : stageBatches) {
						MaterialId materialId = materialsDataManager.getBatchMaterial(batchId);
						Set<BatchId> set = map.get(materialId);
						if (set == null) {
							set = new LinkedHashSet<>();
							map.put(materialId, set);
						}
						set.add(batchId);
					}
				}
			}

			// show that the actual batches are retrievable by stage and
			// material id correctly
			assertEquals(expectedStageBatches, actualStageBatches);

		}));

		// precondition tests
		pluginBuilder.addTestActorPlan("actor", new TestActorPlan(2, (c) -> {
			MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);

			// if the stage id is null
			ContractException contractException = assertThrows(ContractException.class, () -> materialsDataManager.getStageBatchesByMaterialId(null, TestMaterialId.MATERIAL_1));
			assertEquals(MaterialsError.NULL_STAGE_ID, contractException.getErrorType());

			// if the stage id is unknown
			contractException = assertThrows(ContractException.class, () -> materialsDataManager.getStageBatchesByMaterialId(new StageId(10000000), TestMaterialId.MATERIAL_1));
			assertEquals(MaterialsError.UNKNOWN_STAGE_ID, contractException.getErrorType());

			// if the material id is null
			contractException = assertThrows(ContractException.class, () -> materialsDataManager.getStageBatchesByMaterialId(new StageId(0), null));
			assertEquals(MaterialsError.NULL_MATERIAL_ID, contractException.getErrorType());

			// if the material id is unknown
			contractException = assertThrows(ContractException.class, () -> materialsDataManager.getStageBatchesByMaterialId(new StageId(0), TestMaterialId.getUnknownMaterialId()));
			assertEquals(MaterialsError.UNKNOWN_MATERIAL_ID, contractException.getErrorType());

		}));

		TestPluginData testPluginData = pluginBuilder.build();
		Plugin testPlugin = TestPlugin.getTestPlugin(testPluginData);
		MaterialsActionSupport.testConsumers(2013967899243685546L, testPlugin);
	}

	@Test
	@UnitTestMethod(name = "getStageProducer", args = { StageId.class })
	public void testGetStageProducer() {

		TestPluginData.Builder pluginBuilder = TestPluginData.builder();

		for (TestMaterialsProducerId testMaterialsProducerId : TestMaterialsProducerId.values()) {
			pluginBuilder.addTestActorPlan("actor", new TestActorPlan(0, (c) -> {
				MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);
				StageId stageId = materialsDataManager.addStage(testMaterialsProducerId);
				assertEquals(testMaterialsProducerId, materialsDataManager.getStageProducer(stageId));
			}));
		}

		TestPluginData testPluginData = pluginBuilder.build();
		Plugin testPlugin = TestPlugin.getTestPlugin(testPluginData);
		MaterialsActionSupport.testConsumers(4322374809851867527L, testPlugin);

		/* precondition test: if the stage id is null */
		MaterialsActionSupport.testConsumer(3429442631390139742L, (c) -> {
			MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);
			ContractException contractException = assertThrows(ContractException.class, () -> materialsDataManager.getStageProducer(null));
			assertEquals(MaterialsError.NULL_STAGE_ID, contractException.getErrorType());
		});

		/* precondition test: if the stage id is unknown */
		MaterialsActionSupport.testConsumer(8553021441594074433L, (c) -> {
			MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);
			ContractException contractException = assertThrows(ContractException.class, () -> materialsDataManager.getStageProducer(new StageId(1000000000)));
			assertEquals(MaterialsError.UNKNOWN_STAGE_ID, contractException.getErrorType());
		});

	}

	@Test
	@UnitTestMethod(name = "getStages", args = { MaterialsProducerId.class })
	public void testGetStages() {

		TestPluginData.Builder pluginBuilder = TestPluginData.builder();

		for (TestMaterialsProducerId testMaterialsProducerId : TestMaterialsProducerId.values()) {
			pluginBuilder.addTestActorPlan("actor", new TestActorPlan(0, (c) -> {

				MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);
				StochasticsDataManager stochasticsDataManager = c.getDataManager(StochasticsDataManager.class);
				RandomGenerator randomGenerator = stochasticsDataManager.getRandomGenerator();

				Set<StageId> expectedStages = new LinkedHashSet<>();

				int count = randomGenerator.nextInt(10) + 1;
				for (int i = 0; i < count; i++) {
					StageId stageId = materialsDataManager.addStage(testMaterialsProducerId);
					expectedStages.add(stageId);
				}

				List<StageId> stages = materialsDataManager.getStages(testMaterialsProducerId);

				Set<StageId> actualStageIds = new LinkedHashSet<>(stages);
				assertEquals(stages.size(), actualStageIds.size());
				assertEquals(expectedStages, actualStageIds);
			}));
		}

		TestPluginData testPluginData = pluginBuilder.build();
		Plugin testPlugin = TestPlugin.getTestPlugin(testPluginData);
		MaterialsActionSupport.testConsumers(3431193355375533655L, testPlugin);

		/* precondition test: if the materials producer id is null */
		MaterialsActionSupport.testConsumer(8012112350970626114L, (c) -> {
			MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);
			ContractException contractException = assertThrows(ContractException.class, () -> materialsDataManager.getStages(null));
			assertEquals(MaterialsError.NULL_MATERIALS_PRODUCER_ID, contractException.getErrorType());
		});

		/* precondition test: if the materials producer id is unknown */
		MaterialsActionSupport.testConsumer(4013634140214782310L, (c) -> {
			MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);
			ContractException contractException = assertThrows(ContractException.class, () -> materialsDataManager.getStages(TestMaterialsProducerId.getUnknownMaterialsProducerId()));
			assertEquals(MaterialsError.UNKNOWN_MATERIALS_PRODUCER_ID, contractException.getErrorType());
		});
	}

	@Test
	@UnitTestMethod(name = "isStageOffered", args = { StageId.class })
	public void testIsStageOffered() {

		TestPluginData.Builder pluginBuilder = TestPluginData.builder();

		pluginBuilder.addTestActorPlan("actor", new TestActorPlan(0, (c) -> {
			MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);

			for (int i = 0; i < 100; i++) {
				StageId stageId = materialsDataManager.addStage(TestMaterialsProducerId.MATERIALS_PRODUCER_1);
				assertFalse(materialsDataManager.isStageOffered(stageId));
				materialsDataManager.setStageOfferState(stageId, true);
				assertTrue(materialsDataManager.isStageOffered(stageId));
			}

			// precondition tests

			// if the stage id is null
			ContractException contractException = assertThrows(ContractException.class, () -> materialsDataManager.isStageOffered(null));
			assertEquals(MaterialsError.NULL_STAGE_ID, contractException.getErrorType());

			// if the stage id is unknown
			contractException = assertThrows(ContractException.class, () -> materialsDataManager.isStageOffered(new StageId(1000000)));
			assertEquals(MaterialsError.UNKNOWN_STAGE_ID, contractException.getErrorType());

		}));

		TestPluginData testPluginData = pluginBuilder.build();
		Plugin testPlugin = TestPlugin.getTestPlugin(testPluginData);
		MaterialsActionSupport.testConsumers(475901778920012875L, testPlugin);
	}

	@Test
	@UnitTestMethod(name = "materialsProducerIdExists", args = { MaterialsProducerId.class })
	public void testMaterialsProducerIdExists() {

		MaterialsActionSupport.testConsumer(4005535514531641716L, (c) -> {
			MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);

			for (TestMaterialsProducerId testMaterialsProducerId : TestMaterialsProducerId.values()) {
				assertTrue(materialsDataManager.materialsProducerIdExists(testMaterialsProducerId));
			}
			assertFalse(materialsDataManager.materialsProducerIdExists(TestMaterialsProducerId.getUnknownMaterialsProducerId()));
			assertFalse(materialsDataManager.materialsProducerIdExists(null));
		});

	}

	@Test
	@UnitTestMethod(name = "materialsProducerPropertyIdExists", args = { MaterialsProducerPropertyId.class })
	public void testMaterialsProducerPropertyIdExists() {

		MaterialsActionSupport.testConsumer(8256309156804000329L, (c) -> {
			MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);

			for (TestMaterialsProducerPropertyId testMaterialsProducerPropertyId : TestMaterialsProducerPropertyId.values()) {
				assertTrue(materialsDataManager.materialsProducerPropertyIdExists(testMaterialsProducerPropertyId));
			}
			assertFalse(materialsDataManager.materialsProducerPropertyIdExists(TestMaterialsProducerPropertyId.getUnknownMaterialsProducerPropertyId()));
			assertFalse(materialsDataManager.materialsProducerPropertyIdExists(null));
		});
	}

	@Test
	@UnitTestMethod(name = "stageExists", args = { StageId.class })
	public void testStageExists() {
		MaterialsActionSupport.testConsumer(4646356228574091149L, (c) -> {
			MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);
			for (int i = 0; i < 10; i++) {
				StageId stageId = materialsDataManager.addStage(TestMaterialsProducerId.MATERIALS_PRODUCER_1);
				assertTrue(materialsDataManager.stageExists(stageId));
			}
			assertFalse(materialsDataManager.stageExists(null));
			assertFalse(materialsDataManager.stageExists(new StageId(123)));
		});
	}

	@Test
	@UnitTestMethod(name = "moveBatchToInventory", args = { BatchId.class })
	public void testMoveBatchToInventoryEvent() {
		
		TestPluginData.Builder pluginBuilder = TestPluginData.builder();
		double actionTime = 0;

		// create containers to hold observations
		Set<MultiKey> expectedObservations = new LinkedHashSet<>();
		Set<MultiKey> actualObservations = new LinkedHashSet<>();

		// have an observer record batches being removed from stages
		pluginBuilder.addTestActorPlan("observer", new TestActorPlan(actionTime++, (c) -> {
			c.subscribe(StageMembershipRemovalEvent.class, (c2, e) -> {
				MultiKey multiKey = new MultiKey(c2.getTime(), e.getBatchId(), e.getStageId());
				actualObservations.add(multiKey);
			});
		}));

		/*
		 * Have the materials producers create batches and place about half of
		 * them on stages
		 */
		for (TestMaterialsProducerId testMaterialsProducerId : TestMaterialsProducerId.values()) {
			pluginBuilder.addTestActorPlan("actor", new TestActorPlan(actionTime++, (c) -> {
				StochasticsDataManager stochasticsDataManager = c.getDataManager(StochasticsDataManager.class);
				RandomGenerator randomGenerator = stochasticsDataManager.getRandomGenerator();
				MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);

				for (int i = 0; i < 40; i++) {
					BatchConstructionInfo batchConstructionInfo = TestBatchConstructionInfo.getBatchConstructionInfo(testMaterialsProducerId, TestMaterialId.getRandomMaterialId(randomGenerator), randomGenerator.nextDouble() + 0.01, randomGenerator);
					materialsDataManager.addBatch(batchConstructionInfo);
				}

				for (int i = 0; i < 5; i++) {
					materialsDataManager.addStage(testMaterialsProducerId);
				}

				List<StageId> stages = materialsDataManager.getStages(testMaterialsProducerId);
				List<BatchId> batches = materialsDataManager.getInventoryBatches(testMaterialsProducerId);
				for (BatchId batchId : batches) {
					if (randomGenerator.nextBoolean()) {
						StageId stageId = stages.get(randomGenerator.nextInt(stages.size()));
						materialsDataManager.moveBatchToStage(batchId, stageId);
					}
				}

			}));
		}

		/*
		 * Have the materials producers return some about half of the staged
		 * batches to inventory and show that the batches are now in inventory
		 */
		for (TestMaterialsProducerId testMaterialsProducerId : TestMaterialsProducerId.values()) {
			pluginBuilder.addTestActorPlan("actor", new TestActorPlan(actionTime++, (c) -> {
				StochasticsDataManager stochasticsDataManager = c.getDataManager(StochasticsDataManager.class);
				RandomGenerator randomGenerator = stochasticsDataManager.getRandomGenerator();
				MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);

				List<StageId> stages = materialsDataManager.getStages(testMaterialsProducerId);
				for (StageId stageId : stages) {
					List<BatchId> batches = materialsDataManager.getStageBatches(stageId);
					for (BatchId batchId : batches) {
						if (randomGenerator.nextBoolean()) {
							materialsDataManager.moveBatchToInventory(batchId);
							// show that the batch was returned to inventory
							assertFalse(materialsDataManager.getBatchStageId(batchId).isPresent());
							assertTrue(materialsDataManager.getInventoryBatches(testMaterialsProducerId).contains(batchId));
							// create the observation
							MultiKey multiKey = new MultiKey(c.getTime(), batchId, stageId);
							expectedObservations.add(multiKey);
						}
					}
				}
			}));
		}

		// have the observer show that the correct observations were made
		pluginBuilder.addTestActorPlan("observer", new TestActorPlan(actionTime++, (c) -> {
			assertTrue(expectedObservations.size() > 0);
			assertEquals(expectedObservations, actualObservations);
		}));

		TestPluginData testPluginData = pluginBuilder.build();
		Plugin testPlugin = TestPlugin.getTestPlugin(testPluginData);
		MaterialsActionSupport.testConsumers(5136057466059323708L, testPlugin);

		/* precondition test: if the batch id is null */
		MaterialsActionSupport.testConsumer(9033912130601526542L, (c) -> {
			MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);
			ContractException contractException = assertThrows(ContractException.class, () -> materialsDataManager.moveBatchToInventory(null));
			assertEquals(MaterialsError.NULL_BATCH_ID, contractException.getErrorType());
		});

		/* precondition test: if the batch id is unknown */
		MaterialsActionSupport.testConsumer(4357313141781993780L, (c) -> {
			MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);
			ContractException contractException = assertThrows(ContractException.class, () -> materialsDataManager.moveBatchToInventory(new BatchId(10000000)));
			assertEquals(MaterialsError.UNKNOWN_BATCH_ID, contractException.getErrorType());
		});

		/* precondition test: if the batch is not staged */
		MaterialsActionSupport.testConsumer(5899034328012868517L, (c) -> {
			MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);
			StochasticsDataManager stochasticsDataManager = c.getDataManager(StochasticsDataManager.class);
			RandomGenerator randomGenerator = stochasticsDataManager.getRandomGenerator();
			BatchConstructionInfo batchConstructionInfo = TestBatchConstructionInfo.getBatchConstructionInfo(TestMaterialsProducerId.MATERIALS_PRODUCER_1, TestMaterialId.MATERIAL_1, 5.0, randomGenerator);
			BatchId batchId = materialsDataManager.addBatch(batchConstructionInfo);
			ContractException contractException = assertThrows(ContractException.class, () -> materialsDataManager.moveBatchToInventory(batchId));
			assertEquals(MaterialsError.BATCH_NOT_STAGED, contractException.getErrorType());
		});

		/* precondition test: if the stage containing the batch is offered */
		MaterialsActionSupport.testConsumer(6151702850690578711L, (c) -> {
			MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);
			StochasticsDataManager stochasticsDataManager = c.getDataManager(StochasticsDataManager.class);
			RandomGenerator randomGenerator = stochasticsDataManager.getRandomGenerator();
			BatchConstructionInfo batchConstructionInfo = TestBatchConstructionInfo.getBatchConstructionInfo(TestMaterialsProducerId.MATERIALS_PRODUCER_1, TestMaterialId.MATERIAL_1, 5.0, randomGenerator);
			BatchId batchId = materialsDataManager.addBatch(batchConstructionInfo);
			StageId stageId = materialsDataManager.addStage(TestMaterialsProducerId.MATERIALS_PRODUCER_1);
			materialsDataManager.moveBatchToStage(batchId, stageId);
			materialsDataManager.setStageOfferState(stageId, true);
			ContractException contractException = assertThrows(ContractException.class, () -> materialsDataManager.moveBatchToInventory(batchId));
			assertEquals(MaterialsError.OFFERED_STAGE_UNALTERABLE, contractException.getErrorType());
		});

	}

	@Test
	@UnitTestMethod(name = "moveBatchToStage", args = { BatchId.class, StageId.class })
	public void testMoveBatchToStageEvent() {
		TestPluginData.Builder pluginBuilder = TestPluginData.builder();
		double actionTime = 0;

		// create containers to hold observations
		Set<MultiKey> expectedObservations = new LinkedHashSet<>();
		Set<MultiKey> actualObservations = new LinkedHashSet<>();

		// have an observer record observations of batches being assigned to
		// stages
		pluginBuilder.addTestActorPlan("observer", new TestActorPlan(actionTime++, (c) -> {
			c.subscribe(StageMembershipAdditionEvent.class, (c2, e) -> {
				MultiKey multiKey = new MultiKey(c2.getTime(), e.getBatchId(), e.getStageId());
				actualObservations.add(multiKey);
			});
		}));

		// have the producers create several batches and stages
		for (TestMaterialsProducerId testMaterialsProducerId : TestMaterialsProducerId.values()) {

			pluginBuilder.addTestActorPlan("actor", new TestActorPlan(actionTime++, (c) -> {
				MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);
				StochasticsDataManager stochasticsDataManager = c.getDataManager(StochasticsDataManager.class);
				RandomGenerator randomGenerator = stochasticsDataManager.getRandomGenerator();
				for (int i = 0; i < 40; i++) {
					BatchConstructionInfo batchConstructionInfo = TestBatchConstructionInfo.getBatchConstructionInfo(testMaterialsProducerId, TestMaterialId.getRandomMaterialId(randomGenerator), randomGenerator.nextDouble() + 0.01, randomGenerator);
					materialsDataManager.addBatch(batchConstructionInfo);
				}
				for (int i = 0; i < 5; i++) {
					materialsDataManager.addStage(testMaterialsProducerId);
				}
			}));
		}

		// have the producers put about half of their batches onto stages
		for (TestMaterialsProducerId testMaterialsProducerId : TestMaterialsProducerId.values()) {

			pluginBuilder.addTestActorPlan("actor", new TestActorPlan(actionTime++, (c) -> {
				MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);
				StochasticsDataManager stochasticsDataManager = c.getDataManager(StochasticsDataManager.class);
				RandomGenerator randomGenerator = stochasticsDataManager.getRandomGenerator();

				List<StageId> stages = materialsDataManager.getStages(testMaterialsProducerId);
				List<BatchId> batches = materialsDataManager.getInventoryBatches(testMaterialsProducerId);
				for (BatchId batchId : batches) {
					if (randomGenerator.nextBoolean()) {
						StageId stageId = stages.get(randomGenerator.nextInt(stages.size()));
						materialsDataManager.moveBatchToStage(batchId, stageId);

						// show that the batch is now on the stage
						Optional<StageId> optionalStageId = materialsDataManager.getBatchStageId(batchId);
						assertTrue(optionalStageId.isPresent());
						StageId actualStageId = optionalStageId.get();
						assertEquals(stageId, actualStageId);

						// add the expected observation
						MultiKey multiKey = new MultiKey(c.getTime(), batchId, stageId);
						expectedObservations.add(multiKey);
					}
				}

			}));
		}
		// have the observer show that the correct observations were generated
		pluginBuilder.addTestActorPlan("observer", new TestActorPlan(actionTime++, (c) -> {
			assertTrue(expectedObservations.size() > 0);
			assertEquals(expectedObservations, actualObservations);
		}));

		TestPluginData testPluginData = pluginBuilder.build();
		Plugin testPlugin = TestPlugin.getTestPlugin(testPluginData);
		MaterialsActionSupport.testConsumers(6845954292451913670L, testPlugin);

		/* precondition test: if the batch id is null */
		MaterialsActionSupport.testConsumer(0, (c) -> {
			MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);
			StageId stageId = materialsDataManager.addStage(TestMaterialsProducerId.MATERIALS_PRODUCER_1);
			ContractException contractException = assertThrows(ContractException.class, () -> materialsDataManager.moveBatchToStage(null, stageId));
			assertEquals(MaterialsError.NULL_BATCH_ID, contractException.getErrorType());
		});

		/* precondition test: if the batch id is unknown */
		MaterialsActionSupport.testConsumer(0, (c) -> {
			MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);
			StageId stageId = materialsDataManager.addStage(TestMaterialsProducerId.MATERIALS_PRODUCER_1);
			ContractException contractException = assertThrows(ContractException.class, () -> materialsDataManager.moveBatchToStage(new BatchId(100000000), stageId));
			assertEquals(MaterialsError.UNKNOWN_BATCH_ID, contractException.getErrorType());
		});

		/* precondition test: if the batch is already staged */
		MaterialsActionSupport.testConsumer(0, (c) -> {
			MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);
			StochasticsDataManager stochasticsDataManager = c.getDataManager(StochasticsDataManager.class);
			RandomGenerator randomGenerator = stochasticsDataManager.getRandomGenerator();
			BatchConstructionInfo batchConstructionInfo = TestBatchConstructionInfo.getBatchConstructionInfo(TestMaterialsProducerId.MATERIALS_PRODUCER_1, TestMaterialId.MATERIAL_2, 5.6, randomGenerator);
			BatchId batchId = materialsDataManager.addBatch(batchConstructionInfo);
			StageId stageId = materialsDataManager.addStage(TestMaterialsProducerId.MATERIALS_PRODUCER_1);
			materialsDataManager.moveBatchToStage(batchId, stageId);
			ContractException contractException = assertThrows(ContractException.class, () -> materialsDataManager.moveBatchToStage(batchId, stageId));
			assertEquals(MaterialsError.BATCH_ALREADY_STAGED, contractException.getErrorType());
		});

		/* precondition test: if the stage id is null */
		MaterialsActionSupport.testConsumer(0, (c) -> {
			MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);
			StochasticsDataManager stochasticsDataManager = c.getDataManager(StochasticsDataManager.class);
			RandomGenerator randomGenerator = stochasticsDataManager.getRandomGenerator();
			BatchConstructionInfo batchConstructionInfo = TestBatchConstructionInfo.getBatchConstructionInfo(TestMaterialsProducerId.MATERIALS_PRODUCER_1, TestMaterialId.MATERIAL_2, 5.6, randomGenerator);
			BatchId batchId = materialsDataManager.addBatch(batchConstructionInfo);
			ContractException contractException = assertThrows(ContractException.class, () -> materialsDataManager.moveBatchToStage(batchId, null));
			assertEquals(MaterialsError.NULL_STAGE_ID, contractException.getErrorType());
		});

		/* precondition test: if the stage id is unknown */
		MaterialsActionSupport.testConsumer(0, (c) -> {
			MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);
			StochasticsDataManager stochasticsDataManager = c.getDataManager(StochasticsDataManager.class);
			RandomGenerator randomGenerator = stochasticsDataManager.getRandomGenerator();
			BatchConstructionInfo batchConstructionInfo = TestBatchConstructionInfo.getBatchConstructionInfo(TestMaterialsProducerId.MATERIALS_PRODUCER_1, TestMaterialId.MATERIAL_2, 5.6, randomGenerator);
			BatchId batchId = materialsDataManager.addBatch(batchConstructionInfo);
			ContractException contractException = assertThrows(ContractException.class, () -> materialsDataManager.moveBatchToStage(batchId, new StageId(10000000)));
			assertEquals(MaterialsError.UNKNOWN_STAGE_ID, contractException.getErrorType());
		});

		/* precondition test: if the stage is offered */
		MaterialsActionSupport.testConsumer(0, (c) -> {
			MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);
			StochasticsDataManager stochasticsDataManager = c.getDataManager(StochasticsDataManager.class);
			RandomGenerator randomGenerator = stochasticsDataManager.getRandomGenerator();
			BatchConstructionInfo batchConstructionInfo = TestBatchConstructionInfo.getBatchConstructionInfo(TestMaterialsProducerId.MATERIALS_PRODUCER_1, TestMaterialId.MATERIAL_2, 5.6, randomGenerator);
			BatchId batchId = materialsDataManager.addBatch(batchConstructionInfo);
			StageId stageId = materialsDataManager.addStage(TestMaterialsProducerId.MATERIALS_PRODUCER_1);
			materialsDataManager.setStageOfferState(stageId, true);
			ContractException contractException = assertThrows(ContractException.class, () -> materialsDataManager.moveBatchToStage(batchId, stageId));
			assertEquals(MaterialsError.OFFERED_STAGE_UNALTERABLE, contractException.getErrorType());
		});

		/*
		 * precondition test: if batch and stage do not have the same owning
		 * materials producer
		 */
		MaterialsActionSupport.testConsumer(0, (c) -> {
			MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);
			StochasticsDataManager stochasticsDataManager = c.getDataManager(StochasticsDataManager.class);
			RandomGenerator randomGenerator = stochasticsDataManager.getRandomGenerator();
			BatchConstructionInfo batchConstructionInfo = TestBatchConstructionInfo.getBatchConstructionInfo(TestMaterialsProducerId.MATERIALS_PRODUCER_2, TestMaterialId.MATERIAL_2, 12, randomGenerator);
			BatchId batchId = materialsDataManager.addBatch(batchConstructionInfo);
			StageId stageId = materialsDataManager.addStage(TestMaterialsProducerId.MATERIALS_PRODUCER_1);
			ContractException contractException = assertThrows(ContractException.class, () -> materialsDataManager.moveBatchToStage(batchId, stageId));
			assertEquals(MaterialsError.BATCH_STAGED_TO_DIFFERENT_OWNER, contractException.getErrorType());
		});
	}

	@Test
	@UnitTestMethod(name = "removeBatch", args = { BatchId.class })
	public void testRemoveBatch() {

		TestPluginData.Builder pluginBuilder = TestPluginData.builder();
		double actionTime = 0;

		// construct data structures to hold observations
		Set<BatchId> expectedObservations = new LinkedHashSet<>();
		Set<BatchId> actualObservations = new LinkedHashSet<>();

		pluginBuilder.addTestActorPlan("observer", new TestActorPlan(actionTime++, (c) -> {
			c.subscribe(BatchImminentRemovalEvent.class, (c2, e) -> {
				actualObservations.add(e.getBatchId());
			});
		}));

		/*
		 * Add batches -- although we will concentrate on producer 1, we will
		 * have the other producers generate batches for use in precondition
		 * tests
		 */
		for (TestMaterialsProducerId testMaterialsProducerId : TestMaterialsProducerId.values()) {
			pluginBuilder.addTestActorPlan("actor", new TestActorPlan(actionTime++, (c) -> {
				MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);				
				StochasticsDataManager stochasticsDataManager = c.getDataManager(StochasticsDataManager.class);
				RandomGenerator randomGenerator = stochasticsDataManager.getRandomGenerator();
				for (int i = 0; i < 50; i++) {
					BatchConstructionInfo batchConstructionInfo = TestBatchConstructionInfo.getBatchConstructionInfo(testMaterialsProducerId, TestMaterialId.getRandomMaterialId(randomGenerator), randomGenerator.nextDouble(), randomGenerator);
					materialsDataManager.addBatch(batchConstructionInfo);
				}
			}));
		}

		// remove some batches
		pluginBuilder.addTestActorPlan("actor", new TestActorPlan(actionTime++, (c) -> {
			StochasticsDataManager stochasticsDataManager = c.getDataManager(StochasticsDataManager.class);
			RandomGenerator randomGenerator = stochasticsDataManager.getRandomGenerator();
			MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);
			List<BatchId> inventoryBatches = materialsDataManager.getInventoryBatches(TestMaterialsProducerId.MATERIALS_PRODUCER_1);
			for (BatchId batchId : inventoryBatches) {
				if (randomGenerator.nextBoolean()) {
					materialsDataManager.removeBatch(batchId);
					expectedObservations.add(batchId);
				}
			}
		}));

		// show that the batches were indeed removed
		pluginBuilder.addTestActorPlan("actor", new TestActorPlan(actionTime++, (c) -> {
			MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);
			for (BatchId batchId : expectedObservations) {
				assertFalse(materialsDataManager.batchExists(batchId));
			}
		}));

		// show that the observations were properly generated
		pluginBuilder.addTestActorPlan("observer", new TestActorPlan(actionTime++, (c) -> {
			assertTrue(expectedObservations.size() > 0);
			assertEquals(expectedObservations, actualObservations);
		}));

		TestPluginData testPluginData = pluginBuilder.build();
		Plugin testPlugin = TestPlugin.getTestPlugin(testPluginData);
		MaterialsActionSupport.testConsumers(2869388661813620663L, testPlugin);

		/* precondition test: if the batch id is null */
		MaterialsActionSupport.testConsumer(6338675888020916426L, (c) -> {
			MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);
			ContractException contractException = assertThrows(ContractException.class, () -> materialsDataManager.removeBatch(null));
			assertEquals(MaterialsError.NULL_BATCH_ID, contractException.getErrorType());
		});

		/* precondition test: if the batch id is unknown */
		MaterialsActionSupport.testConsumer(3451796010171778050L, (c) -> {
			MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);
			ContractException contractException = assertThrows(ContractException.class, () -> materialsDataManager.removeBatch(new BatchId(100000)));
			assertEquals(MaterialsError.UNKNOWN_BATCH_ID, contractException.getErrorType());
		});

		/* precondition test: if the batch is on an offered stage */
		MaterialsActionSupport.testConsumer(2022222016456137374L, (c) -> {
			MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);
			StochasticsDataManager stochasticsDataManager = c.getDataManager(StochasticsDataManager.class);
			RandomGenerator randomGenerator = stochasticsDataManager.getRandomGenerator();
			BatchConstructionInfo batchConstructionInfo = TestBatchConstructionInfo.getBatchConstructionInfo(TestMaterialsProducerId.MATERIALS_PRODUCER_1, TestMaterialId.MATERIAL_1, 1.0, randomGenerator);
			BatchId batchId = materialsDataManager.addBatch(batchConstructionInfo);
			StageId stageId = materialsDataManager.addStage(TestMaterialsProducerId.MATERIALS_PRODUCER_1);
			materialsDataManager.moveBatchToStage(batchId, stageId);
			materialsDataManager.setStageOfferState(stageId, true);
			ContractException contractException = assertThrows(ContractException.class, () -> materialsDataManager.removeBatch(batchId));
			assertEquals(MaterialsError.OFFERED_STAGE_UNALTERABLE, contractException.getErrorType());
		});

	}

	@Test
	@UnitTestMethod(name = "removeStage", args = { StageId.class, boolean.class })
	public void testRemoveStage() {
		TestPluginData.Builder pluginBuilder = TestPluginData.builder();
		double actionTime = 0;

		// create containers to hold observations
		Set<MultiKey> expectedObservations = new LinkedHashSet<>();
		Set<MultiKey> actualObservations = new LinkedHashSet<>();

		// have a agent observe stage creations

		pluginBuilder.addTestActorPlan("observer", new TestActorPlan(actionTime++, (c) -> {

			c.subscribe(StageImminentRemovalEvent.class, (c2, e) -> {
				actualObservations.add(new MultiKey(c2.getTime(), e.getStageId()));
			});

			c.subscribe(StageMembershipRemovalEvent.class, (c2, e) -> {
				actualObservations.add(new MultiKey(c2.getTime(), e.getBatchId(), e.getStageId()));
			});

			c.subscribe(BatchImminentRemovalEvent.class, (c2, e) -> {
				actualObservations.add(new MultiKey(c2.getTime(), e.getBatchId()));
			});
		}));

		// have the producers create some stages with batches
		for (TestMaterialsProducerId testMaterialsProducerId : TestMaterialsProducerId.values()) {
			pluginBuilder.addTestActorPlan("actor", new TestActorPlan(actionTime++, (c) -> {
				MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);
				StochasticsDataManager stochasticsDataManager = c.getDataManager(StochasticsDataManager.class);
				RandomGenerator randomGenerator = stochasticsDataManager.getRandomGenerator();
				for (int i = 0; i < 50; i++) {
					StageId stageId = materialsDataManager.addStage(testMaterialsProducerId);
					int batchCount = randomGenerator.nextInt(3);
					for (int j = 0; j < batchCount; j++) {
						BatchConstructionInfo batchConstructionInfo = TestBatchConstructionInfo.getBatchConstructionInfo(testMaterialsProducerId, TestMaterialId.getRandomMaterialId(randomGenerator), randomGenerator.nextDouble() + 0.01, randomGenerator);
						BatchId batchId = materialsDataManager.addBatch(batchConstructionInfo);
						materialsDataManager.moveBatchToStage(batchId, stageId);
					}
				}
			}));
		}

		// have the producers destroy all their stages, returning about half of
		// the batches to inventory
		for (TestMaterialsProducerId testMaterialsProducerId : TestMaterialsProducerId.values()) {

			List<StageId> stagesToConfirm = new ArrayList<>();
			List<BatchId> batchesToConfirm = new ArrayList<>();
			Set<BatchId> expectedInventoryBatches = new LinkedHashSet<>();

			pluginBuilder.addTestActorPlan("actor", new TestActorPlan(actionTime++, (c) -> {
				MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);
				StochasticsDataManager stochasticsDataManager = c.getDataManager(StochasticsDataManager.class);
				RandomGenerator randomGenerator = stochasticsDataManager.getRandomGenerator();

				List<StageId> stages = materialsDataManager.getStages(testMaterialsProducerId);
				for (StageId stageId : stages) {
					boolean destroyBatches = randomGenerator.nextBoolean();
					List<BatchId> stageBatches = materialsDataManager.getStageBatches(stageId);
					materialsDataManager.removeStage(stageId, destroyBatches);

					/*
					 * record the batch and stage ids that will be removed after
					 * the current agent activation so that they can be
					 * confirmed later
					 */
					if (destroyBatches) {
						batchesToConfirm.addAll(stageBatches);
					} else {
						expectedInventoryBatches.addAll(stageBatches);
					}
					stagesToConfirm.add(stageId);

					// generate the expected observations
					if (destroyBatches) {
						for (BatchId batchId : stageBatches) {
							expectedObservations.add(new MultiKey(c.getTime(), batchId));
						}
					}

					expectedObservations.add(new MultiKey(c.getTime(), stageId));

				}
			}));

			// show that the expected batches and stages were removed
			pluginBuilder.addTestActorPlan("actor", new TestActorPlan(actionTime++, (c) -> {
				MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);
				for (StageId stageId : stagesToConfirm) {
					assertFalse(materialsDataManager.stageExists(stageId));
				}
				for (BatchId batchId : batchesToConfirm) {
					assertFalse(materialsDataManager.batchExists(batchId));
				}
				List<BatchId> inventoryBatches = materialsDataManager.getInventoryBatches(testMaterialsProducerId);
				Set<BatchId> actualInventoryBatches = new LinkedHashSet<>(inventoryBatches);
				assertEquals(expectedInventoryBatches, actualInventoryBatches);
			}));

		}

		// have the observer show that the correct observations were generated
		pluginBuilder.addTestActorPlan("observer", new TestActorPlan(actionTime++, (c) -> {
			assertTrue(expectedObservations.size() > 0);
			assertEquals(expectedObservations, actualObservations);
		}));

		TestPluginData testPluginData = pluginBuilder.build();
		Plugin testPlugin = TestPlugin.getTestPlugin(testPluginData);
		MaterialsActionSupport.testConsumers(886447125697525680L, testPlugin);

		/* precondition test: if the stage id is null */
		MaterialsActionSupport.testConsumer(0, (c) -> {
			MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);
			ContractException contractException = assertThrows(ContractException.class, () -> materialsDataManager.removeStage(null, false));
			assertEquals(MaterialsError.NULL_STAGE_ID, contractException.getErrorType());
		});

		/* precondition test: if the stage id is unknown */
		MaterialsActionSupport.testConsumer(0, (c) -> {
			MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);
			ContractException contractException = assertThrows(ContractException.class, () -> materialsDataManager.removeStage(new StageId(1000000), false));
			assertEquals(MaterialsError.UNKNOWN_STAGE_ID, contractException.getErrorType());
		});

		/* precondition test: if stage is offered */
		MaterialsActionSupport.testConsumer(0, (c) -> {
			MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);
			StageId stageId = materialsDataManager.addStage(TestMaterialsProducerId.MATERIALS_PRODUCER_1);
			materialsDataManager.setStageOfferState(stageId, true);
			ContractException contractException = assertThrows(ContractException.class, () -> materialsDataManager.removeStage(stageId, false));
			assertEquals(MaterialsError.OFFERED_STAGE_UNALTERABLE, contractException.getErrorType());
		});

	}

	@Test
	@UnitTestMethod(name = "setBatchPropertyValue", args = { BatchId.class, BatchPropertyId.class, Object.class })
	public void testBatchPropertyValueAssignmentEvent() {
		TestPluginData.Builder pluginBuilder = TestPluginData.builder();
		double actionTime = 0;

		// create containers for observations
		Set<MultiKey> expectedObservations = new LinkedHashSet<>();
		Set<MultiKey> actualObservations = new LinkedHashSet<>();

		pluginBuilder.addTestActorPlan("observer", new TestActorPlan(actionTime++, (c) -> {
			c.subscribe(BatchPropertyUpdateEvent.class, (c2, e) -> {
				MultiKey multiKey = new MultiKey(c2.getTime(), e.getBatchId(), e.getBatchPropertyId(), e.getPreviousPropertyValue(), e.getCurrentPropertyValue());
				actualObservations.add(multiKey);
			});
		}));

		// create some batches
		for (TestMaterialsProducerId testMaterialsProducerId : TestMaterialsProducerId.values()) {
			pluginBuilder.addTestActorPlan("actor", new TestActorPlan(actionTime++, (c) -> {
				MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);
				StochasticsDataManager stochasticsDataManager = c.getDataManager(StochasticsDataManager.class);
				RandomGenerator randomGenerator = stochasticsDataManager.getRandomGenerator();
				for (int i = 0; i < 10; i++) {
					BatchConstructionInfo batchConstructionInfo = TestBatchConstructionInfo.getBatchConstructionInfo(testMaterialsProducerId, TestMaterialId.getRandomMaterialId(randomGenerator), randomGenerator.nextDouble() + 0.01, randomGenerator);
					materialsDataManager.addBatch(batchConstructionInfo);
				}
			}));
		}

		// Alter about half of the mutable properties of batches over 5
		// different times
		for (int i = 0; i < 5; i++) {
			for (TestMaterialsProducerId testMaterialsProducerId : TestMaterialsProducerId.values()) {
				pluginBuilder.addTestActorPlan("actor", new TestActorPlan(actionTime++, (c) -> {
					StochasticsDataManager stochasticsDataManager = c.getDataManager(StochasticsDataManager.class);
					RandomGenerator randomGenerator = stochasticsDataManager.getRandomGenerator();
					MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);
					List<BatchId> inventoryBatches = materialsDataManager.getInventoryBatches(testMaterialsProducerId);
					for (BatchId batchId : inventoryBatches) {
						TestMaterialId batchMaterial = materialsDataManager.getBatchMaterial(batchId);
						for (TestBatchPropertyId testBatchPropertyId : TestBatchPropertyId.getTestBatchPropertyIds(batchMaterial)) {
							if (testBatchPropertyId.getPropertyDefinition().propertyValuesAreMutable()) {
								if (randomGenerator.nextBoolean()) {
									Object newPropertyValue = testBatchPropertyId.getRandomPropertyValue(randomGenerator);
									Object oldPropertyValue = materialsDataManager.getBatchPropertyValue(batchId, testBatchPropertyId);
									expectedObservations.add(new MultiKey(c.getTime(), batchId, testBatchPropertyId, oldPropertyValue, newPropertyValue));
									materialsDataManager.setBatchPropertyValue(batchId, testBatchPropertyId, newPropertyValue);

									// show that the new property value is
									// present
									assertEquals(newPropertyValue, materialsDataManager.getBatchPropertyValue(batchId, testBatchPropertyId));
								}
							}
						}
					}
				}));
			}
		}

		pluginBuilder.addTestActorPlan("observer", new TestActorPlan(actionTime++, (c) -> {
			assertTrue(expectedObservations.size() > 0);
			assertEquals(expectedObservations, actualObservations);
		}));

		TestPluginData testPluginData = pluginBuilder.build();
		Plugin testPlugin = TestPlugin.getTestPlugin(testPluginData);
		MaterialsActionSupport.testConsumers(6834204103777199004L, testPlugin);

		/* precondition test: if the batch id is null */
		MaterialsActionSupport.testConsumer(0, (c) -> {
			MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);
			BatchPropertyId batchPropertyId = TestBatchPropertyId.BATCH_PROPERTY_1_2_INTEGER_MUTABLE_NO_TRACK;
			Object propertyValue = 56;
			ContractException contractException = assertThrows(ContractException.class, () -> materialsDataManager.setBatchPropertyValue(null, batchPropertyId, propertyValue));
			assertEquals(MaterialsError.NULL_BATCH_ID, contractException.getErrorType());
		});

		/* precondition test: if the batch id is unknown */
		MaterialsActionSupport.testConsumer(0, (c) -> {
			MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);
			BatchPropertyId batchPropertyId = TestBatchPropertyId.BATCH_PROPERTY_1_2_INTEGER_MUTABLE_NO_TRACK;
			Object propertyValue = 56;
			ContractException contractException = assertThrows(ContractException.class, () -> materialsDataManager.setBatchPropertyValue(new BatchId(100000), batchPropertyId, propertyValue));
			assertEquals(MaterialsError.UNKNOWN_BATCH_ID, contractException.getErrorType());
		});

		/* precondition test: if the batch property id is null */
		MaterialsActionSupport.testConsumer(0, (c) -> {
			StochasticsDataManager stochasticsDataManager = c.getDataManager(StochasticsDataManager.class);
			RandomGenerator randomGenerator = stochasticsDataManager.getRandomGenerator();
			MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);
			MaterialId materialId = TestMaterialId.MATERIAL_1;
			BatchConstructionInfo batchConstructionInfo = TestBatchConstructionInfo.getBatchConstructionInfo(TestMaterialsProducerId.MATERIALS_PRODUCER_1, materialId, 1.0, randomGenerator);
			BatchId batchId = materialsDataManager.addBatch(batchConstructionInfo);
			Object propertyValue = 56;
			ContractException contractException = assertThrows(ContractException.class, () -> materialsDataManager.setBatchPropertyValue(batchId, null, propertyValue));
			assertEquals(PropertyError.NULL_PROPERTY_ID, contractException.getErrorType());
		});

		/* precondition test: if the batch property id is unknown */
		MaterialsActionSupport.testConsumer(0, (c) -> {
			StochasticsDataManager stochasticsDataManager = c.getDataManager(StochasticsDataManager.class);
			RandomGenerator randomGenerator = stochasticsDataManager.getRandomGenerator();
			MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);
			MaterialId materialId = TestMaterialId.MATERIAL_1;
			BatchConstructionInfo batchConstructionInfo = TestBatchConstructionInfo.getBatchConstructionInfo(TestMaterialsProducerId.MATERIALS_PRODUCER_1, materialId, 1.0, randomGenerator);
			BatchId batchId = materialsDataManager.addBatch(batchConstructionInfo);
			Object propertyValue = 56;
			ContractException contractException = assertThrows(ContractException.class,
					() -> materialsDataManager.setBatchPropertyValue(batchId, TestBatchPropertyId.getUnknownBatchPropertyId(), propertyValue));
			assertEquals(PropertyError.UNKNOWN_PROPERTY_ID, contractException.getErrorType());
		});

		/* precondition test: if batch property is not mutable */
		MaterialsActionSupport.testConsumer(0, (c) -> {
			StochasticsDataManager stochasticsDataManager = c.getDataManager(StochasticsDataManager.class);
			RandomGenerator randomGenerator = stochasticsDataManager.getRandomGenerator();
			MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);
			MaterialId materialId = TestMaterialId.MATERIAL_1;
			BatchConstructionInfo batchConstructionInfo = TestBatchConstructionInfo.getBatchConstructionInfo(TestMaterialsProducerId.MATERIALS_PRODUCER_1, materialId, 1.0, randomGenerator);
			BatchId batchId = materialsDataManager.addBatch(batchConstructionInfo);
			ContractException contractException = assertThrows(ContractException.class,
					() -> materialsDataManager.setBatchPropertyValue(batchId, TestBatchPropertyId.BATCH_PROPERTY_1_1_BOOLEAN_IMMUTABLE_NO_TRACK, false));
			assertEquals(PropertyError.IMMUTABLE_VALUE, contractException.getErrorType());
		});

		/* precondition test: if the batch property value is null */
		MaterialsActionSupport.testConsumer(0, (c) -> {
			StochasticsDataManager stochasticsDataManager = c.getDataManager(StochasticsDataManager.class);
			RandomGenerator randomGenerator = stochasticsDataManager.getRandomGenerator();
			MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);
			MaterialId materialId = TestMaterialId.MATERIAL_1;
			BatchConstructionInfo batchConstructionInfo = TestBatchConstructionInfo.getBatchConstructionInfo(TestMaterialsProducerId.MATERIALS_PRODUCER_1, materialId, 1.0, randomGenerator);
			BatchId batchId = materialsDataManager.addBatch(batchConstructionInfo);
			BatchPropertyId batchPropertyId = TestBatchPropertyId.BATCH_PROPERTY_1_2_INTEGER_MUTABLE_NO_TRACK;
			ContractException contractException = assertThrows(ContractException.class, () -> materialsDataManager.setBatchPropertyValue(batchId, batchPropertyId, null));
			assertEquals(PropertyError.NULL_PROPERTY_VALUE, contractException.getErrorType());
		});

		/*
		 * precondition test: if the batch property value is not compatible with
		 * the corresponding property definition
		 */
		MaterialsActionSupport.testConsumer(0, (c) -> {
			MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);
			MaterialId materialId = TestMaterialId.MATERIAL_1;
			RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(5751385676704973926L);
			BatchConstructionInfo batchConstructionInfo = TestBatchConstructionInfo.getBatchConstructionInfo(TestMaterialsProducerId.MATERIALS_PRODUCER_1, materialId, 1.0, randomGenerator);
			BatchId batchId = materialsDataManager.addBatch(batchConstructionInfo);
			BatchPropertyId batchPropertyId = TestBatchPropertyId.BATCH_PROPERTY_1_2_INTEGER_MUTABLE_NO_TRACK;
			ContractException contractException = assertThrows(ContractException.class, () -> materialsDataManager.setBatchPropertyValue(batchId, batchPropertyId, 12.4));
			assertEquals(PropertyError.INCOMPATIBLE_VALUE, contractException.getErrorType());
		});

		/* precondition test: if the batch in on an offered stage */
		MaterialsActionSupport.testConsumer(0, (c) -> {
			MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);
			MaterialId materialId = TestMaterialId.MATERIAL_1;						
			StochasticsDataManager stochasticsDataManager = c.getDataManager(StochasticsDataManager.class);
			RandomGenerator randomGenerator = stochasticsDataManager.getRandomGenerator();
			BatchConstructionInfo batchConstructionInfo = TestBatchConstructionInfo.getBatchConstructionInfo(TestMaterialsProducerId.MATERIALS_PRODUCER_1, materialId, 1.0, randomGenerator);
			BatchId batchId = materialsDataManager.addBatch(batchConstructionInfo);
			BatchPropertyId batchPropertyId = TestBatchPropertyId.BATCH_PROPERTY_1_2_INTEGER_MUTABLE_NO_TRACK;
			Object propertyValue = 56;
			StageId stageId = materialsDataManager.addStage(TestMaterialsProducerId.MATERIALS_PRODUCER_1);
			materialsDataManager.moveBatchToStage(batchId, stageId);
			materialsDataManager.setStageOfferState(stageId, true);
			ContractException contractException = assertThrows(ContractException.class, () -> materialsDataManager.setBatchPropertyValue(batchId, batchPropertyId, propertyValue));
			assertEquals(MaterialsError.OFFERED_STAGE_UNALTERABLE, contractException.getErrorType());
		});

	}

	@Test
	@UnitTestMethod(name = "testSetMaterialsProducerPropertyValue", args = {})
	public void testMaterialsProducerPropertyValueAssignmentEvent() {

		TestPluginData.Builder pluginBuilder = TestPluginData.builder();
		double actionTime = 0;

		Set<MultiKey> expectedObservations = new LinkedHashSet<>();
		Set<MultiKey> actualObservations = new LinkedHashSet<>();

		// have an agent observe all changes to all producer property values
		pluginBuilder.addTestActorPlan("observer", new TestActorPlan(actionTime++, (c) -> {
			for (TestMaterialsProducerId testMaterialsProducerId : TestMaterialsProducerId.values()) {
				for (TestMaterialsProducerPropertyId testMaterialsProducerPropertyId : TestMaterialsProducerPropertyId.values()) {
					EventLabel<MaterialsProducerPropertyUpdateEvent> eventLabel = MaterialsProducerPropertyUpdateEvent.getEventLabelByMaterialsProducerAndProperty(c, testMaterialsProducerId,
							testMaterialsProducerPropertyId);
					c.subscribe(eventLabel, (c2, e) -> {
						MultiKey multiKey = new MultiKey(c2.getTime(), e.getMaterialsProducerId(), e.getMaterialsProducerPropertyId(), e.getPreviousPropertyValue(), e.getCurrentPropertyValue());
						actualObservations.add(multiKey);
					});
				}
			}
		}));

		for (int i = 0; i < 200; i++) {
			pluginBuilder.addTestActorPlan("actor", new TestActorPlan(actionTime++, (c) -> {
				// pick a random materials producer property and update it to a
				// random value
				StochasticsDataManager stochasticsDataManager = c.getDataManager(StochasticsDataManager.class);
				MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);
				RandomGenerator randomGenerator = stochasticsDataManager.getRandomGenerator();
				MaterialsProducerId materialsProducerId = TestMaterialsProducerId.getRandomMaterialsProducerId(randomGenerator);
				TestMaterialsProducerPropertyId testMaterialsProducerPropertyId = TestMaterialsProducerPropertyId.getRandomMutableMaterialsProducerPropertyId(randomGenerator);
				Object oldValue = materialsDataManager.getMaterialsProducerPropertyValue(materialsProducerId, testMaterialsProducerPropertyId);
				Object newValue = testMaterialsProducerPropertyId.getRandomPropertyValue(randomGenerator);
				materialsDataManager.setMaterialsProducerPropertyValue(materialsProducerId, testMaterialsProducerPropertyId, newValue);

				// show that the new value is present
				assertEquals(newValue, materialsDataManager.getMaterialsProducerPropertyValue(materialsProducerId, testMaterialsProducerPropertyId));

				// record the expected observation
				MultiKey multiKey = new MultiKey(c.getTime(), materialsProducerId, testMaterialsProducerPropertyId, oldValue, newValue);
				expectedObservations.add(multiKey);
			}));
		}

		// have the observer show that the proper observations were generated
		pluginBuilder.addTestActorPlan("observer", new TestActorPlan(actionTime++, (c) -> {
			assertTrue(expectedObservations.size() > 0);
			assertEquals(expectedObservations, actualObservations);
		}));

		TestPluginData testPluginData = pluginBuilder.build();
		Plugin testPlugin = TestPlugin.getTestPlugin(testPluginData);
		MaterialsActionSupport.testConsumers(3888305479377600267L, testPlugin);

		/* precondition test: if the materials producer id is null */
		MaterialsActionSupport.testConsumer(6542684073527908815L, (c) -> {
			MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);
			MaterialsProducerPropertyId materialsProducerPropertyId = TestMaterialsProducerPropertyId.MATERIALS_PRODUCER_PROPERTY_2_INTEGER_MUTABLE_NO_TRACK;
			Object propertyValue = 5;
			ContractException contractException = assertThrows(ContractException.class, () -> materialsDataManager.setMaterialsProducerPropertyValue(null, materialsProducerPropertyId, propertyValue));
			assertEquals(MaterialsError.NULL_MATERIALS_PRODUCER_ID, contractException.getErrorType());
		});

		/* precondition test: if the materials producer id is unknown */
		MaterialsActionSupport.testConsumer(7681762910631637513L, (c) -> {
			MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);
			MaterialsProducerPropertyId materialsProducerPropertyId = TestMaterialsProducerPropertyId.MATERIALS_PRODUCER_PROPERTY_2_INTEGER_MUTABLE_NO_TRACK;
			Object propertyValue = 5;
			ContractException contractException = assertThrows(ContractException.class,
					() -> materialsDataManager.setMaterialsProducerPropertyValue(TestMaterialsProducerId.getUnknownMaterialsProducerId(), materialsProducerPropertyId, propertyValue));
			assertEquals(MaterialsError.UNKNOWN_MATERIALS_PRODUCER_ID, contractException.getErrorType());
		});

		/* precondition test: if the materials producer property id is null */
		MaterialsActionSupport.testConsumer(5021355716878157868L, (c) -> {
			MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);
			MaterialsProducerId materialsProducerId = TestMaterialsProducerId.MATERIALS_PRODUCER_1;
			Object propertyValue = 5;
			ContractException contractException = assertThrows(ContractException.class, () -> materialsDataManager.setMaterialsProducerPropertyValue(materialsProducerId, null, propertyValue));
			assertEquals(PropertyError.NULL_PROPERTY_ID, contractException.getErrorType());
		});

		/*
		 * precondition test: if the materials producer property id is unknown
		 */
		MaterialsActionSupport.testConsumer(6739449188760503613L, (c) -> {
			MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);
			MaterialsProducerId materialsProducerId = TestMaterialsProducerId.MATERIALS_PRODUCER_1;
			Object propertyValue = 5;
			ContractException contractException = assertThrows(ContractException.class,
					() -> materialsDataManager.setMaterialsProducerPropertyValue(materialsProducerId, TestMaterialsProducerPropertyId.getUnknownMaterialsProducerPropertyId(), propertyValue));
			assertEquals(PropertyError.UNKNOWN_PROPERTY_ID, contractException.getErrorType());
		});

		/* precondition test: if the materials producer property is immutable */
		MaterialsActionSupport.testConsumer(3015359065220869477L, (c) -> {
			MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);
			MaterialsProducerId materialsProducerId = TestMaterialsProducerId.MATERIALS_PRODUCER_1;
			Object propertyValue = 5;
			ContractException contractException = assertThrows(ContractException.class, () -> materialsDataManager.setMaterialsProducerPropertyValue(materialsProducerId,
					TestMaterialsProducerPropertyId.MATERIALS_PRODUCER_PROPERTY_8_INTEGER_IMMUTABLE_NO_TRACK, propertyValue));
			assertEquals(PropertyError.IMMUTABLE_VALUE, contractException.getErrorType());
		});

		/* precondition test: if the property value is null */
		MaterialsActionSupport.testConsumer(2614476067199172944L, (c) -> {
			MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);
			MaterialsProducerId materialsProducerId = TestMaterialsProducerId.MATERIALS_PRODUCER_1;
			MaterialsProducerPropertyId materialsProducerPropertyId = TestMaterialsProducerPropertyId.MATERIALS_PRODUCER_PROPERTY_2_INTEGER_MUTABLE_NO_TRACK;
			ContractException contractException = assertThrows(ContractException.class,
					() -> materialsDataManager.setMaterialsProducerPropertyValue(materialsProducerId, materialsProducerPropertyId, null));
			assertEquals(PropertyError.NULL_PROPERTY_VALUE, contractException.getErrorType());
		});

		/*
		 * precondition test: if the property value is incompatible with the
		 * corresponding property definition
		 */
		MaterialsActionSupport.testConsumer(5704066697861534404L, (c) -> {
			MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);
			MaterialsProducerId materialsProducerId = TestMaterialsProducerId.MATERIALS_PRODUCER_1;
			MaterialsProducerPropertyId materialsProducerPropertyId = TestMaterialsProducerPropertyId.MATERIALS_PRODUCER_PROPERTY_2_INTEGER_MUTABLE_NO_TRACK;
			ContractException contractException = assertThrows(ContractException.class,
					() -> materialsDataManager.setMaterialsProducerPropertyValue(materialsProducerId, materialsProducerPropertyId, 12.5));
			assertEquals(PropertyError.INCOMPATIBLE_VALUE, contractException.getErrorType());
		});

	}

	@Test
	@UnitTestMethod(name = "setStageOfferState", args = { StageId.class, boolean.class })
	public void testStageOfferEvent() {
		TestPluginData.Builder pluginBuilder = TestPluginData.builder();
		double actionTime = 0;

		// create containers to hold observations
		Set<MultiKey> expectedObservations = new LinkedHashSet<>();
		Set<MultiKey> actualObservations = new LinkedHashSet<>();

		// have a agent observe stage creations
		pluginBuilder.addTestActorPlan("observer", new TestActorPlan(actionTime++, (c) -> {
			c.subscribe(StageOfferUpdateEvent.class, (c2, e) -> {
				actualObservations.add(new MultiKey(c2.getTime(), e.getStageId(), e.isPreviousOfferState(), e.isCurrentOfferState()));
			});
		}));

		// have the producers create a few stages
		for (TestMaterialsProducerId testMaterialsProducerId : TestMaterialsProducerId.values()) {
			pluginBuilder.addTestActorPlan("actor", new TestActorPlan(actionTime++, (c) -> {
				MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);
				for (int i = 0; i < 5; i++) {
					materialsDataManager.addStage(testMaterialsProducerId);
				}
			}));
		}

		// have the producers make a few random offer state changes
		for (int i = 0; i < 10; i++) {
			for (TestMaterialsProducerId testMaterialsProducerId : TestMaterialsProducerId.values()) {
				pluginBuilder.addTestActorPlan("actor", new TestActorPlan(actionTime++, (c) -> {
					StochasticsDataManager stochasticsDataManager = c.getDataManager(StochasticsDataManager.class);
					RandomGenerator randomGenerator = stochasticsDataManager.getRandomGenerator();
					MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);
					List<StageId> stages = materialsDataManager.getStages(testMaterialsProducerId);
					StageId stageId = stages.get(randomGenerator.nextInt(stages.size()));
					boolean isOffered = materialsDataManager.isStageOffered(stageId);
					materialsDataManager.setStageOfferState(stageId, !isOffered);

					// show that the offer state changed
					boolean newOfferState = materialsDataManager.isStageOffered(stageId);
					assertNotEquals(isOffered, newOfferState);

					// generate the expected observation
					expectedObservations.add(new MultiKey(c.getTime(), stageId, isOffered, !isOffered));
				}));
			}
		}

		// have the observer show that the correct observations were generated
		pluginBuilder.addTestActorPlan("observer", new TestActorPlan(actionTime++, (c) -> {
			assertTrue(expectedObservations.size() > 0);
			assertEquals(expectedObservations, actualObservations);
		}));

		TestPluginData testPluginData = pluginBuilder.build();
		Plugin testPlugin = TestPlugin.getTestPlugin(testPluginData);
		MaterialsActionSupport.testConsumers(916177724112971509L, testPlugin);

		/* precondition test: if the stage id is null */
		MaterialsActionSupport.testConsumer(5384463547664747393L, (c) -> {
			MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);
			ContractException contractException = assertThrows(ContractException.class, () -> materialsDataManager.setStageOfferState(null, true));
			assertEquals(MaterialsError.NULL_STAGE_ID, contractException.getErrorType());
		});

		/* precondition test: if the stage id is unknown */
		MaterialsActionSupport.testConsumer(319106508275202491L, (c) -> {
			MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);
			ContractException contractException = assertThrows(ContractException.class, () -> materialsDataManager.setStageOfferState(new StageId(10000000), true));
			assertEquals(MaterialsError.UNKNOWN_STAGE_ID, contractException.getErrorType());
		});
	}

	@Test
	@UnitTestMethod(name = "transferMaterialBetweenBatches", args = { BatchId.class, BatchId.class, double.class })
	public void testBatchContentShiftEvent() {

		TestPluginData.Builder pluginBuilder = TestPluginData.builder();

		double actionTime = 0;

		// create data structures to

		Set<MultiKey> expectedObservations = new LinkedHashSet<>();
		Set<MultiKey> actualObservations = new LinkedHashSet<>();

		// have an agent observe the movement of materials between batches
		pluginBuilder.addTestActorPlan("observer", new TestActorPlan(actionTime++, (c) -> {
			c.subscribe(BatchAmountUpdateEvent.class, (c2, e) -> {
				MultiKey multiKey = new MultiKey(e.getBatchId(), e.getPreviousAmount(), e.getCurrentAmount());
				actualObservations.add(multiKey);
			});
		}));

		// Have the materials producers create a few batches, ensuring the
		// amount in each batch is positive
		for (TestMaterialsProducerId testMaterialsProducerId : TestMaterialsProducerId.values()) {
			pluginBuilder.addTestActorPlan("actor", new TestActorPlan(actionTime++, (c) -> {
				MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);
				StochasticsDataManager stochasticsDataManager = c.getDataManager(StochasticsDataManager.class);
				RandomGenerator randomGenerator = stochasticsDataManager.getRandomGenerator();
				for (TestMaterialId testMaterialId : TestMaterialId.values()) {
					for (int i = 0; i < 10; i++) {
						BatchConstructionInfo batchConstructionInfo = TestBatchConstructionInfo.getBatchConstructionInfo(testMaterialsProducerId, testMaterialId, randomGenerator.nextDouble() + 0.1,randomGenerator);
						materialsDataManager.addBatch(batchConstructionInfo);
					}
				}
			}));
		}

		/*
		 * We will concentrate on just producer 2 for most of the test but will
		 * utilize batches in other producers in some tests
		 */
		MaterialsProducerId materialsProducerId = TestMaterialsProducerId.MATERIALS_PRODUCER_2;

		// have the materials producer swap material amount around
		pluginBuilder.addTestActorPlan("actor", new TestActorPlan(actionTime++, (c) -> {
			MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);
			StochasticsDataManager stochasticsDataManager = c.getDataManager(StochasticsDataManager.class);
			RandomGenerator randomGenerator = stochasticsDataManager.getRandomGenerator();
			for (TestMaterialId testMaterialId : TestMaterialId.values()) {
				List<BatchId> batches = materialsDataManager.getInventoryBatchesByMaterialId(materialsProducerId, testMaterialId);
				int index1 = randomGenerator.nextInt(batches.size());
				int index2 = randomGenerator.nextInt(batches.size());
				if (index2 == index1) {
					index2++;
					index2 = index2 % batches.size();
				}
				BatchId batchId1 = batches.get(index1);
				BatchId batchId2 = batches.get(index2);
				double batchAmount1 = materialsDataManager.getBatchAmount(batchId1);
				double batchAmount2 = materialsDataManager.getBatchAmount(batchId2);
				// ensure that we transfer a positive amount, but not the whole
				// amount
				double portion = randomGenerator.nextDouble() * 0.8 + 0.1;
				double transferAmount = batchAmount1 * portion;
				materialsDataManager.transferMaterialBetweenBatches(batchId1, batchId2, transferAmount);

				double batchAmount3 = materialsDataManager.getBatchAmount(batchId1);
				double batchAmount4 = materialsDataManager.getBatchAmount(batchId2);

				assertEquals(batchAmount1 - transferAmount, batchAmount3, 0.00000000001);
				assertEquals(batchAmount2 + transferAmount, batchAmount4, 0.00000000001);

				MultiKey multiKey = new MultiKey(batchId1, batchAmount1, batchAmount3);
				expectedObservations.add(multiKey);

				multiKey = new MultiKey(batchId2, batchAmount2, batchAmount4);
				expectedObservations.add(multiKey);
			}
		}));

		// Have the observer show that the observations were generated correctly
		pluginBuilder.addTestActorPlan("observer", new TestActorPlan(actionTime++, (c) -> {
			assertEquals(expectedObservations, actualObservations);
		}));

		TestPluginData testPluginData = pluginBuilder.build();
		Plugin testPlugin = TestPlugin.getTestPlugin(testPluginData);
		MaterialsActionSupport.testConsumers(6185579658134885353L, testPlugin);

		/* precondition test: if the source batch id is null */
		MaterialsActionSupport.testConsumer(4531031694400929670L, (c) -> {
			ContractException contractException = assertThrows(ContractException.class, () -> {
				MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);
				StochasticsDataManager stochasticsDataManager = c.getDataManager(StochasticsDataManager.class);
				RandomGenerator randomGenerator = stochasticsDataManager.getRandomGenerator();
				BatchConstructionInfo batchConstructionInfo = TestBatchConstructionInfo.getBatchConstructionInfo(TestMaterialsProducerId.MATERIALS_PRODUCER_1, TestMaterialId.MATERIAL_1, 1.0, randomGenerator);
				BatchId destinationBatchId = materialsDataManager.addBatch(batchConstructionInfo);
				materialsDataManager.transferMaterialBetweenBatches(null, destinationBatchId, 0.1);
			});
			assertEquals(MaterialsError.NULL_BATCH_ID, contractException.getErrorType());
		});

		/* precondition test: if the source batch id is unknown */
		MaterialsActionSupport.testConsumer(6565712732641056695L, (c) -> {
			ContractException contractException = assertThrows(ContractException.class, () -> {
				StochasticsDataManager stochasticsDataManager = c.getDataManager(StochasticsDataManager.class);
				RandomGenerator randomGenerator = stochasticsDataManager.getRandomGenerator();
				MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);
				BatchId sourceBatchId = new BatchId(100000);
				BatchConstructionInfo batchConstructionInfo = TestBatchConstructionInfo.getBatchConstructionInfo(TestMaterialsProducerId.MATERIALS_PRODUCER_1, TestMaterialId.MATERIAL_1, 1.0, randomGenerator);
				BatchId destinationBatchId = materialsDataManager.addBatch(batchConstructionInfo);
				materialsDataManager.transferMaterialBetweenBatches(sourceBatchId, destinationBatchId, 0.1);
			});
			assertEquals(MaterialsError.UNKNOWN_BATCH_ID, contractException.getErrorType());
		});

		/* precondition test: if the destination batch id is null */
		MaterialsActionSupport.testConsumer(2331763843587032989L, (c) -> {
			ContractException contractException = assertThrows(ContractException.class, () -> {
				StochasticsDataManager stochasticsDataManager = c.getDataManager(StochasticsDataManager.class);
				RandomGenerator randomGenerator = stochasticsDataManager.getRandomGenerator();
				MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);
				BatchConstructionInfo batchConstructionInfo = TestBatchConstructionInfo.getBatchConstructionInfo(TestMaterialsProducerId.MATERIALS_PRODUCER_1, TestMaterialId.MATERIAL_1, 1.0, randomGenerator);
				BatchId sourceBatchId = materialsDataManager.addBatch(batchConstructionInfo);
				BatchId destinationBatchId = null;
				materialsDataManager.transferMaterialBetweenBatches(sourceBatchId, destinationBatchId, 0.1);
			});
			assertEquals(MaterialsError.NULL_BATCH_ID, contractException.getErrorType());
		});

		/* precondition test: if the destination batch id is unknown */
		MaterialsActionSupport.testConsumer(6191035775701595700L, (c) -> {
			ContractException contractException = assertThrows(ContractException.class, () -> {
				StochasticsDataManager stochasticsDataManager = c.getDataManager(StochasticsDataManager.class);
				RandomGenerator randomGenerator = stochasticsDataManager.getRandomGenerator();
				MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);
				BatchConstructionInfo batchConstructionInfo = TestBatchConstructionInfo.getBatchConstructionInfo(TestMaterialsProducerId.MATERIALS_PRODUCER_1, TestMaterialId.MATERIAL_1, 1.0, randomGenerator);
				BatchId sourceBatchId = materialsDataManager.addBatch(batchConstructionInfo);
				BatchId destinationBatchId = new BatchId(10000000);
				materialsDataManager.transferMaterialBetweenBatches(sourceBatchId, destinationBatchId, 0.1);
			});
			assertEquals(MaterialsError.UNKNOWN_BATCH_ID, contractException.getErrorType());
		});

		/*
		 * precondition test: if the source and destination batches are the same
		 */
		MaterialsActionSupport.testConsumer(3554772523918373156L, (c) -> {
			ContractException contractException = assertThrows(ContractException.class, () -> {
				StochasticsDataManager stochasticsDataManager = c.getDataManager(StochasticsDataManager.class);
				RandomGenerator randomGenerator = stochasticsDataManager.getRandomGenerator();
				MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);
				BatchConstructionInfo batchConstructionInfo = TestBatchConstructionInfo.getBatchConstructionInfo(TestMaterialsProducerId.MATERIALS_PRODUCER_1, TestMaterialId.MATERIAL_1, 1.0, randomGenerator);
				BatchId sourceBatchId = materialsDataManager.addBatch(batchConstructionInfo);
				BatchId destinationBatchId = sourceBatchId;
				materialsDataManager.transferMaterialBetweenBatches(sourceBatchId, destinationBatchId, 0.1);
			});
			assertEquals(MaterialsError.REFLEXIVE_BATCH_SHIFT, contractException.getErrorType());
		});

		/*
		 * precondition test: if the batches do not have the same material type
		 */
		MaterialsActionSupport.testConsumer(7162782209932339508L, (c) -> {
			ContractException contractException = assertThrows(ContractException.class, () -> {
				StochasticsDataManager stochasticsDataManager = c.getDataManager(StochasticsDataManager.class);
				RandomGenerator randomGenerator = stochasticsDataManager.getRandomGenerator();
				MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);
				
				BatchConstructionInfo batchConstructionInfo = TestBatchConstructionInfo.getBatchConstructionInfo(TestMaterialsProducerId.MATERIALS_PRODUCER_1, TestMaterialId.MATERIAL_1, 1.0, randomGenerator);
				BatchId sourceBatchId = materialsDataManager.addBatch(batchConstructionInfo);
				
				batchConstructionInfo = TestBatchConstructionInfo.getBatchConstructionInfo(TestMaterialsProducerId.MATERIALS_PRODUCER_1, TestMaterialId.MATERIAL_2, 1.0, randomGenerator);
				BatchId destinationBatchId = materialsDataManager.addBatch(batchConstructionInfo);
				materialsDataManager.transferMaterialBetweenBatches(sourceBatchId, destinationBatchId, 0.1);
			});
			assertEquals(MaterialsError.MATERIAL_TYPE_MISMATCH, contractException.getErrorType());
		});

		/*
		 * precondition test: if the batches have different owning materials
		 * producers
		 */
		MaterialsActionSupport.testConsumer(5391338933894482101L, (c) -> {
			ContractException contractException = assertThrows(ContractException.class, () -> {
				StochasticsDataManager stochasticsDataManager = c.getDataManager(StochasticsDataManager.class);
				RandomGenerator randomGenerator = stochasticsDataManager.getRandomGenerator();
				MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);
				BatchConstructionInfo batchConstructionInfo = TestBatchConstructionInfo.getBatchConstructionInfo(TestMaterialsProducerId.MATERIALS_PRODUCER_1, TestMaterialId.MATERIAL_1, 1.0, randomGenerator);
				BatchId sourceBatchId = materialsDataManager.addBatch(batchConstructionInfo);
				batchConstructionInfo = TestBatchConstructionInfo.getBatchConstructionInfo(TestMaterialsProducerId.MATERIALS_PRODUCER_2, TestMaterialId.MATERIAL_1, 1.0, randomGenerator);
				BatchId destinationBatchId = materialsDataManager.addBatch(batchConstructionInfo);
				materialsDataManager.transferMaterialBetweenBatches(sourceBatchId, destinationBatchId, 0.1);
			});
			assertEquals(MaterialsError.BATCH_SHIFT_WITH_MULTIPLE_OWNERS, contractException.getErrorType());
		});

		/* precondition test: if the source batch is on a stage is offered */
		MaterialsActionSupport.testConsumer(3272165493215915111L, (c) -> {
			ContractException contractException = assertThrows(ContractException.class, () -> {
				StochasticsDataManager stochasticsDataManager = c.getDataManager(StochasticsDataManager.class);
				RandomGenerator randomGenerator = stochasticsDataManager.getRandomGenerator();
				MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);
				BatchConstructionInfo batchConstructionInfo = TestBatchConstructionInfo.getBatchConstructionInfo(TestMaterialsProducerId.MATERIALS_PRODUCER_1, TestMaterialId.MATERIAL_1, 1.0, randomGenerator);
				BatchId sourceBatchId = materialsDataManager.addBatch(batchConstructionInfo);
				StageId stageId = materialsDataManager.addStage(TestMaterialsProducerId.MATERIALS_PRODUCER_1);
				materialsDataManager.moveBatchToStage(sourceBatchId, stageId);
				materialsDataManager.setStageOfferState(stageId, true);
				batchConstructionInfo = TestBatchConstructionInfo.getBatchConstructionInfo(TestMaterialsProducerId.MATERIALS_PRODUCER_1, TestMaterialId.MATERIAL_1, 1.0, randomGenerator);
				BatchId destinationBatchId = materialsDataManager.addBatch(batchConstructionInfo);
				materialsDataManager.transferMaterialBetweenBatches(sourceBatchId, destinationBatchId, 0.1);
			});
			assertEquals(MaterialsError.OFFERED_STAGE_UNALTERABLE, contractException.getErrorType());
		});

		/*
		 * precondition test: if the destination batch is on a stage is offered
		 */
		MaterialsActionSupport.testConsumer(472094558069917703L, (c) -> {
			ContractException contractException = assertThrows(ContractException.class, () -> {
				MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);
				StochasticsDataManager stochasticsDataManager = c.getDataManager(StochasticsDataManager.class);
				RandomGenerator randomGenerator = stochasticsDataManager.getRandomGenerator();
				BatchConstructionInfo batchConstructionInfo = TestBatchConstructionInfo.getBatchConstructionInfo(TestMaterialsProducerId.MATERIALS_PRODUCER_1, TestMaterialId.MATERIAL_1, 1.0, randomGenerator);
				BatchId sourceBatchId = materialsDataManager.addBatch(batchConstructionInfo);
				batchConstructionInfo = TestBatchConstructionInfo.getBatchConstructionInfo(TestMaterialsProducerId.MATERIALS_PRODUCER_1, TestMaterialId.MATERIAL_1, 1.0, randomGenerator);
				BatchId destinationBatchId = materialsDataManager.addBatch(batchConstructionInfo);
				StageId stageId = materialsDataManager.addStage(TestMaterialsProducerId.MATERIALS_PRODUCER_1);
				materialsDataManager.moveBatchToStage(destinationBatchId, stageId);
				materialsDataManager.setStageOfferState(stageId, true);
				materialsDataManager.transferMaterialBetweenBatches(sourceBatchId, destinationBatchId, 0.1);
			});
			assertEquals(MaterialsError.OFFERED_STAGE_UNALTERABLE, contractException.getErrorType());
		});

		/* precondition test: if the shift amount is not a finite number */
		MaterialsActionSupport.testConsumer(2032850391793850929L, (c) -> {
			ContractException contractException = assertThrows(ContractException.class, () -> {
				StochasticsDataManager stochasticsDataManager = c.getDataManager(StochasticsDataManager.class);
				RandomGenerator randomGenerator = stochasticsDataManager.getRandomGenerator();
				MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);
				BatchConstructionInfo batchConstructionInfo = TestBatchConstructionInfo.getBatchConstructionInfo(TestMaterialsProducerId.MATERIALS_PRODUCER_1, TestMaterialId.MATERIAL_1, 1.0, randomGenerator);
				BatchId sourceBatchId = materialsDataManager.addBatch(batchConstructionInfo);
				batchConstructionInfo = TestBatchConstructionInfo.getBatchConstructionInfo(TestMaterialsProducerId.MATERIALS_PRODUCER_1, TestMaterialId.MATERIAL_1, 1.0, randomGenerator);
				BatchId destinationBatchId = materialsDataManager.addBatch(batchConstructionInfo);
				materialsDataManager.transferMaterialBetweenBatches(sourceBatchId, destinationBatchId, Double.POSITIVE_INFINITY);
			});
			assertEquals(MaterialsError.NON_FINITE_MATERIAL_AMOUNT, contractException.getErrorType());
		});

		/* precondition test: if the shift amount is negative */
		MaterialsActionSupport.testConsumer(5245408361837825062L, (c) -> {
			ContractException contractException = assertThrows(ContractException.class, () -> {
				StochasticsDataManager stochasticsDataManager = c.getDataManager(StochasticsDataManager.class);
				RandomGenerator randomGenerator = stochasticsDataManager.getRandomGenerator();
				MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);
				BatchConstructionInfo batchConstructionInfo = TestBatchConstructionInfo.getBatchConstructionInfo(TestMaterialsProducerId.MATERIALS_PRODUCER_1, TestMaterialId.MATERIAL_1, 1.0, randomGenerator);
				BatchId sourceBatchId = materialsDataManager.addBatch(batchConstructionInfo);
				batchConstructionInfo = TestBatchConstructionInfo.getBatchConstructionInfo(TestMaterialsProducerId.MATERIALS_PRODUCER_1, TestMaterialId.MATERIAL_1, 1.0, randomGenerator);
				BatchId destinationBatchId = materialsDataManager.addBatch(batchConstructionInfo);
				materialsDataManager.transferMaterialBetweenBatches(sourceBatchId, destinationBatchId, -0.5);
			});
			assertEquals(MaterialsError.NEGATIVE_MATERIAL_AMOUNT, contractException.getErrorType());
		});

		/*
		 * precondition test: if the shift amount exceeds the available material
		 * on the source batch
		 */
		MaterialsActionSupport.testConsumer(8724998204446972180L, (c) -> {
			ContractException contractException = assertThrows(ContractException.class, () -> {
				StochasticsDataManager stochasticsDataManager = c.getDataManager(StochasticsDataManager.class);
				RandomGenerator randomGenerator = stochasticsDataManager.getRandomGenerator();
				MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);
				BatchConstructionInfo batchConstructionInfo = TestBatchConstructionInfo.getBatchConstructionInfo(TestMaterialsProducerId.MATERIALS_PRODUCER_1, TestMaterialId.MATERIAL_1, 1.0, randomGenerator);
				BatchId sourceBatchId = materialsDataManager.addBatch(batchConstructionInfo);
				batchConstructionInfo = TestBatchConstructionInfo.getBatchConstructionInfo(TestMaterialsProducerId.MATERIALS_PRODUCER_1, TestMaterialId.MATERIAL_1, 1.0, randomGenerator);
				BatchId destinationBatchId = materialsDataManager.addBatch(batchConstructionInfo);
				materialsDataManager.transferMaterialBetweenBatches(sourceBatchId, destinationBatchId, 2.0);
			});
			assertEquals(MaterialsError.INSUFFICIENT_MATERIAL_AVAILABLE, contractException.getErrorType());
		});

		/*
		 * precondition test: if the shift amount would cause an overflow on the
		 * destination batch
		 */
		MaterialsActionSupport.testConsumer(3007064903100070620L, (c) -> {
			ContractException contractException = assertThrows(ContractException.class, () -> {
				StochasticsDataManager stochasticsDataManager = c.getDataManager(StochasticsDataManager.class);
				RandomGenerator randomGenerator = stochasticsDataManager.getRandomGenerator();
				MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);
				BatchConstructionInfo batchConstructionInfo = TestBatchConstructionInfo.getBatchConstructionInfo(TestMaterialsProducerId.MATERIALS_PRODUCER_1, TestMaterialId.MATERIAL_1, Double.MAX_VALUE, randomGenerator);
				BatchId sourceBatchId = materialsDataManager.addBatch(batchConstructionInfo);
				batchConstructionInfo = TestBatchConstructionInfo.getBatchConstructionInfo(TestMaterialsProducerId.MATERIALS_PRODUCER_1, TestMaterialId.MATERIAL_1, Double.MAX_VALUE, randomGenerator);
				BatchId destinationBatchId = materialsDataManager.addBatch(batchConstructionInfo);
				double amount = Double.MAX_VALUE / 2;
				materialsDataManager.transferMaterialBetweenBatches(sourceBatchId, destinationBatchId, amount);
			});
			assertEquals(MaterialsError.MATERIAL_ARITHMETIC_EXCEPTION, contractException.getErrorType());
		});

	}

	@Test
	@UnitTestMethod(name = "transferOfferedStage", args = { StageId.class, MaterialsProducerId.class })
	public void testTransferOfferedStage() {
		TestPluginData.Builder pluginBuilder = TestPluginData.builder();
		double actionTime = 0;

		// create containers to hold observations
		Set<MultiKey> expectedObservations = new LinkedHashSet<>();
		Set<MultiKey> actualObservations = new LinkedHashSet<>();

		pluginBuilder.addTestActorPlan("observer", new TestActorPlan(actionTime++, (c) -> {
			c.subscribe(StageMaterialsProducerUpdateEvent.class, (c2, e) -> {
				MultiKey multiKey = new MultiKey(c.getTime(), e.getStageId(), e.getPreviousMaterialsProducerId(), e.getCurrentMaterialsProducerId());
				actualObservations.add(multiKey);
			});

			c.subscribe(StageOfferUpdateEvent.class, (c2, e) -> {
				MultiKey multiKey = new MultiKey(c.getTime(), e.getStageId());
				actualObservations.add(multiKey);

			});
		}));

		int stagesPerProducer = 5;
		int transferCount = stagesPerProducer * TestMaterialsProducerId.values().length;

		// have the materials producers create a few offered stages
		for (TestMaterialsProducerId testMaterialsProducerId : TestMaterialsProducerId.values()) {
			pluginBuilder.addTestActorPlan("actor", new TestActorPlan(actionTime++, (c) -> {
				StochasticsDataManager stochasticsDataManager = c.getDataManager(StochasticsDataManager.class);
				RandomGenerator randomGenerator = stochasticsDataManager.getRandomGenerator();
				MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);
				for (int i = 0; i < stagesPerProducer; i++) {
					StageId stageId = materialsDataManager.addStage(testMaterialsProducerId);
					for (int j = 0; j < 3; j++) {
						BatchConstructionInfo batchConstructionInfo = TestBatchConstructionInfo.getBatchConstructionInfo(testMaterialsProducerId, TestMaterialId.getRandomMaterialId(randomGenerator), randomGenerator.nextDouble() + 0.01, randomGenerator);
						BatchId batchId = materialsDataManager.addBatch(batchConstructionInfo);
						materialsDataManager.moveBatchToStage(batchId, stageId);
					}
					materialsDataManager.setStageOfferState(stageId, true);
					MultiKey multiKey = new MultiKey(c.getTime(), stageId);
					expectedObservations.add(multiKey);
				}
			}));
		}

		// have an agent transfer offered stages
		for (int i = 0; i < transferCount; i++) {
			pluginBuilder.addTestActorPlan("actor", new TestActorPlan(actionTime++, (c) -> {
				MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);
				StochasticsDataManager stochasticsDataManager = c.getDataManager(StochasticsDataManager.class);
				RandomGenerator randomGenerator = stochasticsDataManager.getRandomGenerator();

				// determine the stages to transfer

				List<StageId> stagesToTransfer = new ArrayList<>();
				for (TestMaterialsProducerId testMaterialsProducerId : TestMaterialsProducerId.values()) {
					List<StageId> offeredStages = materialsDataManager.getOfferedStages(testMaterialsProducerId);
					stagesToTransfer.addAll(offeredStages);
				}
				StageId stageId = stagesToTransfer.get(randomGenerator.nextInt(stagesToTransfer.size()));

				// select a producer at random to receive the transfered
				// stage

				MaterialsProducerId stageProducer = materialsDataManager.getStageProducer(stageId);
				List<MaterialsProducerId> candidateProducers = new ArrayList<>(materialsDataManager.getMaterialsProducerIds());
				candidateProducers.remove(stageProducer);
				MaterialsProducerId altProducer = candidateProducers.get(randomGenerator.nextInt(candidateProducers.size()));

				// transfer the stage
				materialsDataManager.transferOfferedStage(stageId, altProducer);

				// show that the stage was properly transferred
				assertEquals(altProducer, materialsDataManager.getStageProducer(stageId));

				// record expected observations

				MultiKey multiKey = new MultiKey(c.getTime(), stageId, stageProducer, altProducer);
				expectedObservations.add(multiKey);

				multiKey = new MultiKey(c.getTime(), stageId);
				expectedObservations.add(multiKey);

			}));
		}

		pluginBuilder.addTestActorPlan("observer", new TestActorPlan(actionTime++, (c) -> {
			assertTrue(expectedObservations.size() > 0);
			assertEquals(expectedObservations, actualObservations);
		}));

		TestPluginData testPluginData = pluginBuilder.build();
		Plugin testPlugin = TestPlugin.getTestPlugin(testPluginData);
		MaterialsActionSupport.testConsumers(3739485201643969207L, testPlugin);

		/* precondition test: if the stage id is null */
		MaterialsActionSupport.testConsumer(1130010427224075392L, (c) -> {
			MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);
			StageId stageId = materialsDataManager.addStage(TestMaterialsProducerId.MATERIALS_PRODUCER_1);
			MaterialsProducerId altProducer = TestMaterialsProducerId.MATERIALS_PRODUCER_2;
			materialsDataManager.setStageOfferState(stageId, true);
			ContractException contractException = assertThrows(ContractException.class, () -> materialsDataManager.transferOfferedStage(null, altProducer));
			assertEquals(MaterialsError.NULL_STAGE_ID, contractException.getErrorType());
		});

		/* precondition test: if the stage id is unknown */
		MaterialsActionSupport.testConsumer(3682112595474000731L, (c) -> {
			MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);
			StageId stageId = materialsDataManager.addStage(TestMaterialsProducerId.MATERIALS_PRODUCER_1);
			MaterialsProducerId altProducer = TestMaterialsProducerId.MATERIALS_PRODUCER_2;
			materialsDataManager.setStageOfferState(stageId, true);
			ContractException contractException = assertThrows(ContractException.class, () -> materialsDataManager.transferOfferedStage(new StageId(10000000), altProducer));
			assertEquals(MaterialsError.UNKNOWN_STAGE_ID, contractException.getErrorType());
		});

		/* precondition test: if the materials producer id is null */
		MaterialsActionSupport.testConsumer(2900230239814256887L, (c) -> {
			MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);
			StageId stageId = materialsDataManager.addStage(TestMaterialsProducerId.MATERIALS_PRODUCER_1);
			materialsDataManager.setStageOfferState(stageId, true);
			ContractException contractException = assertThrows(ContractException.class, () -> materialsDataManager.transferOfferedStage(stageId, null));
			assertEquals(MaterialsError.NULL_MATERIALS_PRODUCER_ID, contractException.getErrorType());
		});

		/* precondition test: if the materials producer id is unknown */
		MaterialsActionSupport.testConsumer(452781590728467653L, (c) -> {
			MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);
			StageId stageId = materialsDataManager.addStage(TestMaterialsProducerId.MATERIALS_PRODUCER_1);
			materialsDataManager.setStageOfferState(stageId, true);
			ContractException contractException = assertThrows(ContractException.class,
					() -> materialsDataManager.transferOfferedStage(stageId, TestMaterialsProducerId.getUnknownMaterialsProducerId()));
			assertEquals(MaterialsError.UNKNOWN_MATERIALS_PRODUCER_ID, contractException.getErrorType());
		});

		/* precondition test: if the stage is not offered */
		MaterialsActionSupport.testConsumer(6778669475043282422L, (c) -> {
			MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);
			StageId stageId = materialsDataManager.addStage(TestMaterialsProducerId.MATERIALS_PRODUCER_1);
			MaterialsProducerId altProducer = TestMaterialsProducerId.MATERIALS_PRODUCER_2;
			materialsDataManager.setStageOfferState(stageId, true);
			materialsDataManager.setStageOfferState(stageId, false);
			ContractException contractException = assertThrows(ContractException.class, () -> materialsDataManager.transferOfferedStage(stageId, altProducer));
			assertEquals(MaterialsError.UNOFFERED_STAGE_NOT_TRANSFERABLE, contractException.getErrorType());
			materialsDataManager.setStageOfferState(stageId, true);
		});

		/*
		 * precondition test: if the source and destination materials producers
		 * are the same
		 */
		MaterialsActionSupport.testConsumer(4646205108657064829L, (c) -> {

			MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);
			StageId stageId = materialsDataManager.addStage(TestMaterialsProducerId.MATERIALS_PRODUCER_1);
			materialsDataManager.setStageOfferState(stageId, true);
			ContractException contractException = assertThrows(ContractException.class, () -> materialsDataManager.transferOfferedStage(stageId, TestMaterialsProducerId.MATERIALS_PRODUCER_1));
			assertEquals(MaterialsError.REFLEXIVE_STAGE_TRANSFER, contractException.getErrorType());

		});
	}

	@Test
	@UnitTestMethod(name = "transferResourceToRegion", args = { MaterialsProducerId.class, ResourceId.class, RegionId.class, long.class })
	public void testTransferResourceToRegion() {
		TestPluginData.Builder pluginBuilder = TestPluginData.builder();
		double actionTime = 0;

		// create containers to hold observations
		Set<MultiKey> expectedObservations = new LinkedHashSet<>();
		Set<MultiKey> actualObservations = new LinkedHashSet<>();

		// have a agent observe resource transfers from producers to regions.
		pluginBuilder.addTestActorPlan("observer", new TestActorPlan(actionTime++, (c) -> {
			/*
			 * When transferring resources from a producer to a region, a
			 * RegionResourceAdditionEvent is generated. This is not an
			 * observation, but is the contracted reaction event that is then
			 * processed by the resources package. To assess that this event is
			 * indeed generated we could add a custom resolver, but choose
			 * instead to have the observer agent subscribe to the resulting
			 * RegionResourceUpdateEvent
			 */

			for (TestRegionId testRegionId : TestRegionId.values()) {
				for (TestResourceId testResourceId : TestResourceId.values()) {
					c.subscribe(RegionResourceUpdateEvent.getEventLabelByRegionAndResource(c, testRegionId, testResourceId), (c2, e) -> {
						MultiKey multiKey = new MultiKey(c.getTime(), e.getResourceId(), e.getRegionId(), e.getPreviousResourceLevel(), e.getCurrentResourceLevel());
						actualObservations.add(multiKey);
					});
				}
			}

			for (TestResourceId testResourceId : TestResourceId.values()) {
				c.subscribe(MaterialsProducerResourceUpdateEvent.getEventLabelByResource(c, testResourceId), (c2, e) -> {
					MultiKey multiKey = new MultiKey(c.getTime(), e.getResourceId(), e.getMaterialsProducerId(), e.getPreviousResourceLevel(), e.getCurrentResourceLevel());
					actualObservations.add(multiKey);
				});
			}

		}));

		// have the producers generate some resources
		for (TestMaterialsProducerId testMaterialsProducerId : TestMaterialsProducerId.values()) {
			pluginBuilder.addTestActorPlan("actor", new TestActorPlan(actionTime++, (c) -> {
				MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);
				StochasticsDataManager stochasticsDataManager = c.getDataManager(StochasticsDataManager.class);
				RandomGenerator randomGenerator = stochasticsDataManager.getRandomGenerator();

				for (TestResourceId testResourceId : TestResourceId.values()) {
					if (randomGenerator.nextBoolean()) {
						StageId stageId = materialsDataManager.addStage(testMaterialsProducerId);
						long amount = (randomGenerator.nextInt(1000) + 100);
						materialsDataManager.convertStageToResource(stageId, testResourceId, amount);
						MultiKey multiKey = new MultiKey(c.getTime(), testResourceId, testMaterialsProducerId, 0L, amount);
						expectedObservations.add(multiKey);
					}
				}

			}));
		}

		// have an agent distribute the resources over time
		for (TestMaterialsProducerId testMaterialsProducerId : TestMaterialsProducerId.values()) {
			for (TestResourceId testResourceId : TestResourceId.values()) {
				pluginBuilder.addTestActorPlan("actor", new TestActorPlan(actionTime++, (c) -> {
					MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);
					ResourcesDataManager resourcesDataManager = c.getDataManager(ResourcesDataManager.class);
					StochasticsDataManager stochasticsDataManager = c.getDataManager(StochasticsDataManager.class);
					RandomGenerator randomGenerator = stochasticsDataManager.getRandomGenerator();
					long materialsProducerResourceLevel = materialsDataManager.getMaterialsProducerResourceLevel(testMaterialsProducerId, testResourceId);
					if (materialsProducerResourceLevel > 0) {
						long amountToTransfer = randomGenerator.nextInt((int) materialsProducerResourceLevel) + 1;
						TestRegionId regionId = TestRegionId.getRandomRegionId(randomGenerator);
						long regionResourceLevel = resourcesDataManager.getRegionResourceLevel(regionId, testResourceId);
						materialsDataManager.transferResourceToRegion(testMaterialsProducerId, testResourceId, regionId, amountToTransfer);

						// show that the resource was transfered
						long currentProducerResourceLevel = materialsDataManager.getMaterialsProducerResourceLevel(testMaterialsProducerId, testResourceId);
						long currentRegionResourceLevel = resourcesDataManager.getRegionResourceLevel(regionId, testResourceId);

						assertEquals(materialsProducerResourceLevel - amountToTransfer, currentProducerResourceLevel);
						assertEquals(regionResourceLevel + amountToTransfer, currentRegionResourceLevel);

						// record the expected observations

						MultiKey multiKey = new MultiKey(c.getTime(), testResourceId, regionId, regionResourceLevel, currentRegionResourceLevel);
						expectedObservations.add(multiKey);
						multiKey = new MultiKey(c.getTime(), testResourceId, testMaterialsProducerId, materialsProducerResourceLevel, currentProducerResourceLevel);
						expectedObservations.add(multiKey);

					}
				}));
			}
		}

		// have the observer show that the observations are correct
		pluginBuilder.addTestActorPlan("observer", new TestActorPlan(actionTime++, (c) -> {
			assertTrue(expectedObservations.size() > 0);
			assertEquals(expectedObservations, actualObservations);
		}));

		TestPluginData testPluginData = pluginBuilder.build();
		Plugin testPlugin = TestPlugin.getTestPlugin(testPluginData);
		MaterialsActionSupport.testConsumers(2289322490697828226L, testPlugin);

		/* precondition test: if the resource id is null */
		MaterialsActionSupport.testConsumer(1367796071113751106L, (c) -> {
			MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);
			MaterialsProducerId materialsProducerId = TestMaterialsProducerId.MATERIALS_PRODUCER_2;
			ResourceId resourceId = TestResourceId.RESOURCE_3;
			RegionId regionId = TestRegionId.REGION_4;
			long amountToTransfer = 45L;
			StageId stageId = materialsDataManager.addStage(materialsProducerId);
			materialsDataManager.convertStageToResource(stageId, resourceId, amountToTransfer);
			ContractException contractException = assertThrows(ContractException.class, () -> materialsDataManager.transferResourceToRegion(materialsProducerId, null, regionId, amountToTransfer));
			assertEquals(ResourceError.NULL_RESOURCE_ID, contractException.getErrorType());
		});

		/* precondition test: if the resource id is unknown */
		MaterialsActionSupport.testConsumer(8014590590926533288L, (c) -> {
			MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);
			MaterialsProducerId materialsProducerId = TestMaterialsProducerId.MATERIALS_PRODUCER_2;
			ResourceId resourceId = TestResourceId.RESOURCE_3;
			RegionId regionId = TestRegionId.REGION_4;
			long amountToTransfer = 45L;
			StageId stageId = materialsDataManager.addStage(materialsProducerId);
			materialsDataManager.convertStageToResource(stageId, resourceId, amountToTransfer);
			ContractException contractException = assertThrows(ContractException.class,
					() -> materialsDataManager.transferResourceToRegion(materialsProducerId, TestResourceId.getUnknownResourceId(), regionId, amountToTransfer));
			assertEquals(ResourceError.UNKNOWN_RESOURCE_ID, contractException.getErrorType());
		});

		/* precondition test: if the region id is null */
		MaterialsActionSupport.testConsumer(4865873025074936636L, (c) -> {
			MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);
			MaterialsProducerId materialsProducerId = TestMaterialsProducerId.MATERIALS_PRODUCER_2;
			ResourceId resourceId = TestResourceId.RESOURCE_3;
			long amountToTransfer = 45L;
			StageId stageId = materialsDataManager.addStage(materialsProducerId);
			materialsDataManager.convertStageToResource(stageId, resourceId, amountToTransfer);
			ContractException contractException = assertThrows(ContractException.class, () -> materialsDataManager.transferResourceToRegion(materialsProducerId, resourceId, null, amountToTransfer));
			assertEquals(RegionError.NULL_REGION_ID, contractException.getErrorType());
		});

		/* precondition test: if the region id is unknown */
		MaterialsActionSupport.testConsumer(4472671173642659805L, (c) -> {
			MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);
			MaterialsProducerId materialsProducerId = TestMaterialsProducerId.MATERIALS_PRODUCER_2;
			ResourceId resourceId = TestResourceId.RESOURCE_3;
			long amountToTransfer = 45L;
			StageId stageId = materialsDataManager.addStage(materialsProducerId);
			materialsDataManager.convertStageToResource(stageId, resourceId, amountToTransfer);
			ContractException contractException = assertThrows(ContractException.class,
					() -> materialsDataManager.transferResourceToRegion(materialsProducerId, resourceId, TestRegionId.getUnknownRegionId(), amountToTransfer));
			assertEquals(RegionError.UNKNOWN_REGION_ID, contractException.getErrorType());
		});

		/* precondition test: if the materials producer id is null */
		MaterialsActionSupport.testConsumer(6956131170154399460L, (c) -> {
			MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);
			MaterialsProducerId materialsProducerId = TestMaterialsProducerId.MATERIALS_PRODUCER_2;
			ResourceId resourceId = TestResourceId.RESOURCE_3;
			RegionId regionId = TestRegionId.REGION_4;
			long amountToTransfer = 45L;
			StageId stageId = materialsDataManager.addStage(materialsProducerId);
			materialsDataManager.convertStageToResource(stageId, resourceId, amountToTransfer);
			ContractException contractException = assertThrows(ContractException.class, () -> materialsDataManager.transferResourceToRegion(null, resourceId, regionId, amountToTransfer));
			assertEquals(MaterialsError.NULL_MATERIALS_PRODUCER_ID, contractException.getErrorType());
		});

		/* precondition test: if the materials producer id is unknown */
		MaterialsActionSupport.testConsumer(1760306489660703762L, (c) -> {
			MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);
			MaterialsProducerId materialsProducerId = TestMaterialsProducerId.MATERIALS_PRODUCER_2;
			ResourceId resourceId = TestResourceId.RESOURCE_3;
			RegionId regionId = TestRegionId.REGION_4;
			long amountToTransfer = 45L;
			StageId stageId = materialsDataManager.addStage(materialsProducerId);
			materialsDataManager.convertStageToResource(stageId, resourceId, amountToTransfer);
			ContractException contractException = assertThrows(ContractException.class,
					() -> materialsDataManager.transferResourceToRegion(TestMaterialsProducerId.getUnknownMaterialsProducerId(), resourceId, regionId, amountToTransfer));
			assertEquals(MaterialsError.UNKNOWN_MATERIALS_PRODUCER_ID, contractException.getErrorType());
		});

		/* precondition test: if the materials amount is negative */
		MaterialsActionSupport.testConsumer(2214714534579989103L, (c) -> {
			MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);
			MaterialsProducerId materialsProducerId = TestMaterialsProducerId.MATERIALS_PRODUCER_2;
			ResourceId resourceId = TestResourceId.RESOURCE_3;
			RegionId regionId = TestRegionId.REGION_4;
			long amountToTransfer = 45L;
			StageId stageId = materialsDataManager.addStage(materialsProducerId);
			materialsDataManager.convertStageToResource(stageId, resourceId, amountToTransfer);
			ContractException contractException = assertThrows(ContractException.class, () -> materialsDataManager.transferResourceToRegion(materialsProducerId, resourceId, regionId, -1L));
			assertEquals(ResourceError.NEGATIVE_RESOURCE_AMOUNT, contractException.getErrorType());
		});

		/*
		 * precondition test: if the materials amount exceeds the resource level
		 * of the materials producer
		 */
		MaterialsActionSupport.testConsumer(8260344965557977221L, (c) -> {
			MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);
			MaterialsProducerId materialsProducerId = TestMaterialsProducerId.MATERIALS_PRODUCER_2;
			ResourceId resourceId = TestResourceId.RESOURCE_3;
			RegionId regionId = TestRegionId.REGION_4;
			long amountToTransfer = 45L;
			StageId stageId = materialsDataManager.addStage(materialsProducerId);
			materialsDataManager.convertStageToResource(stageId, resourceId, amountToTransfer);
			ContractException contractException = assertThrows(ContractException.class,
					() -> materialsDataManager.transferResourceToRegion(materialsProducerId, resourceId, regionId, amountToTransfer * 2));
			assertEquals(ResourceError.INSUFFICIENT_RESOURCES_AVAILABLE, contractException.getErrorType());
		});

		/*
		 * precondition test: if the materials amount would cause an overflow of
		 * the regions resource level
		 */
		MaterialsActionSupport.testConsumer(4416313459810970424L, (c) -> {
			MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);
			MaterialsProducerId materialsProducerId = TestMaterialsProducerId.MATERIALS_PRODUCER_2;
			ResourceId resourceId = TestResourceId.RESOURCE_3;
			RegionId regionId = TestRegionId.REGION_4;
			long amountToTransfer = 45L;
			StageId stageId = materialsDataManager.addStage(materialsProducerId);
			materialsDataManager.convertStageToResource(stageId, resourceId, amountToTransfer);
			materialsDataManager.transferResourceToRegion(materialsProducerId, resourceId, regionId, amountToTransfer);

			/*
			 * There is currently some of the resource present, so we will add
			 * half of the max value of long two times in a row. That will cause
			 * the region to overflow while keeping the producer from doing so
			 * 
			 */
			long hugeAmount = Long.MAX_VALUE / 2;
			stageId = materialsDataManager.addStage(materialsProducerId);
			materialsDataManager.convertStageToResource(stageId, resourceId, hugeAmount);
			materialsDataManager.transferResourceToRegion(materialsProducerId, resourceId, regionId, hugeAmount);

			stageId = materialsDataManager.addStage(materialsProducerId);
			materialsDataManager.convertStageToResource(stageId, resourceId, hugeAmount);
			ContractException contractException = assertThrows(ContractException.class, () -> materialsDataManager.transferResourceToRegion(materialsProducerId, resourceId, regionId, hugeAmount));
			assertEquals(ResourceError.RESOURCE_ARITHMETIC_EXCEPTION, contractException.getErrorType());

		});

	}

	@Test
	@UnitTestMethod(name = "init", args = { DataManagerContext.class })
	public void testStageOfferUpdateEventLabelers() {
		MaterialsActionSupport.testConsumer(645534075810555962L, (c) -> {
			EventLabeler<StageOfferUpdateEvent> eventLabeler1 = StageOfferUpdateEvent.getEventLabelerForStage();
			assertNotNull(eventLabeler1);
			ContractException contractException = assertThrows(ContractException.class, () -> c.addEventLabeler(eventLabeler1));
			assertEquals(NucleusError.DUPLICATE_LABELER_ID_IN_EVENT_LABELER, contractException.getErrorType());
		});

	}

	@Test
	@UnitTestMethod(name = "init", args = { DataManagerContext.class })
	public void testMaterialsProducerPropertyUpdateEventLabelers() {
		/*
		 * Have the agent attempt to add the event labeler and show that a
		 * contract exception is thrown, indicating that the labeler was
		 * previously added by the resolver.
		 */
		MaterialsActionSupport.testConsumer(301724267100798742L, (c) -> {
			EventLabeler<MaterialsProducerPropertyUpdateEvent> eventLabeler = MaterialsProducerPropertyUpdateEvent.getEventLabelerForMaterialsProducerAndProperty();
			assertNotNull(eventLabeler);
			ContractException contractException = assertThrows(ContractException.class, () -> c.addEventLabeler(eventLabeler));
			assertEquals(NucleusError.DUPLICATE_LABELER_ID_IN_EVENT_LABELER, contractException.getErrorType());

		});
	}

	@Test
	@UnitTestMethod(name = "init", args = { DataManagerContext.class })
	public void testMaterialsProducerResourceUpdateEventLabelers() {

		/*
		 * Have the agent attempt to add the event labeler and show that a
		 * contract exception is thrown, indicating that the labeler was
		 * previously added by the resolver.
		 */
		MaterialsActionSupport.testConsumer(13119761810425715L, (c) -> {
			EventLabeler<MaterialsProducerResourceUpdateEvent> eventLabeler1 = MaterialsProducerResourceUpdateEvent.getEventLabelerForMaterialsProducerAndResource();
			assertNotNull(eventLabeler1);
			ContractException contractException = assertThrows(ContractException.class, () -> c.addEventLabeler(eventLabeler1));
			assertEquals(NucleusError.DUPLICATE_LABELER_ID_IN_EVENT_LABELER, contractException.getErrorType());

			EventLabeler<MaterialsProducerResourceUpdateEvent> eventLabeler2 = MaterialsProducerResourceUpdateEvent.getEventLabelerForResource();
			assertNotNull(eventLabeler2);
			contractException = assertThrows(ContractException.class, () -> c.addEventLabeler(eventLabeler2));
			assertEquals(NucleusError.DUPLICATE_LABELER_ID_IN_EVENT_LABELER, contractException.getErrorType());
		});
	}

	@Test
	@UnitTestMethod(name = "init", args = { DataManagerContext.class })
	public void testStageMaterialsProducerUpdateEventLabelers() {

		MaterialsActionSupport.testConsumer(6871307284439549691L, (c) -> {

			EventLabeler<StageMaterialsProducerUpdateEvent> eventLabeler1 = StageMaterialsProducerUpdateEvent.getEventLabelerForDestination();
			assertNotNull(eventLabeler1);
			ContractException contractException = assertThrows(ContractException.class, () -> c.addEventLabeler(eventLabeler1));
			assertEquals(NucleusError.DUPLICATE_LABELER_ID_IN_EVENT_LABELER, contractException.getErrorType());

			EventLabeler<StageMaterialsProducerUpdateEvent> eventLabeler2 = StageMaterialsProducerUpdateEvent.getEventLabelerForSource();
			assertNotNull(eventLabeler2);
			contractException = assertThrows(ContractException.class, () -> c.addEventLabeler(eventLabeler2));
			assertEquals(NucleusError.DUPLICATE_LABELER_ID_IN_EVENT_LABELER, contractException.getErrorType());

			EventLabeler<StageMaterialsProducerUpdateEvent> eventLabeler3 = StageMaterialsProducerUpdateEvent.getEventLabelerForStage();
			assertNotNull(eventLabeler3);
			contractException = assertThrows(ContractException.class, () -> c.addEventLabeler(eventLabeler3));
			assertEquals(NucleusError.DUPLICATE_LABELER_ID_IN_EVENT_LABELER, contractException.getErrorType());

		});
	}

	@Test
	@UnitTestMethod(name = "init", args = { DataManagerContext.class })
	public void testResourceIdAddition() {
		/*
		 * Have the actor add a resource id and show that the materials data
		 * manager will support the addition
		 */
		MaterialsActionSupport.testConsumer(7336173642619419311L, (c) -> {
			ResourceId newResourceId = TestResourceId.getUnknownResourceId();
			ResourcesDataManager resourcesDataManager = c.getDataManager(ResourcesDataManager.class);
			resourcesDataManager.addResourceId(newResourceId, TimeTrackingPolicy.TRACK_TIME);

			MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);
			Set<MaterialsProducerId> materialsProducerIds = materialsDataManager.getMaterialsProducerIds();
			assertTrue(materialsProducerIds.size() > 0);
			for (MaterialsProducerId materialsProducerId : materialsProducerIds) {
				long materialsProducerResourceLevel = materialsDataManager.getMaterialsProducerResourceLevel(materialsProducerId, newResourceId);
				assertEquals(0, materialsProducerResourceLevel);
			}
		});
	}

	@Test
	@UnitTestMethod(name = "init", args = { DataManagerContext.class })
	public void testMaterialsDataManagerInitialState() {

		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(5272599676969321594L);

		Builder builder = Simulation.builder();

		/*
		 * Add the materials plugin, utilizing all materials initial data
		 * methods to provide sufficient variety to test the proper loading of
		 * initial data. Note that stage and batch ids do not start with zero.
		 * This will force the renumbering of all batches and stages and
		 * complicate the testing a bit, but will show that the resolver is
		 * correctly renumbering the ids.
		 */
		MaterialsPluginData.Builder materialsBuilder = MaterialsPluginData.builder();

		int bId = 0;
		int sId = 0;
		for (TestMaterialsProducerId testMaterialsProducerId : TestMaterialsProducerId.values()) {
			List<BatchId> batches = new ArrayList<>();

			for (int i = 0; i < 50; i++) {

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

			for (int i = 0; i < 10; i++) {
				StageId stageId = new StageId(sId++);
				stages.add(stageId);
				boolean offered = i % 2 == 0;
				materialsBuilder.addStage(stageId, offered, testMaterialsProducerId);
			}

			Collections.shuffle(batches, new Random(randomGenerator.nextLong()));
			for (int i = 0; i < 30; i++) {
				BatchId batchId = batches.get(i);
				StageId stageId = stages.get(randomGenerator.nextInt(stages.size()));
				materialsBuilder.addBatchToStage(stageId, batchId);
			}
		}

		for (TestMaterialId testMaterialId : TestMaterialId.values()) {
			materialsBuilder.addMaterial(testMaterialId);
		}

		for (TestMaterialsProducerId testMaterialsProducerId : TestMaterialsProducerId.values()) {
			materialsBuilder.addMaterialsProducerId(testMaterialsProducerId);
			for (TestMaterialsProducerPropertyId testMaterialsProducerPropertyId : TestMaterialsProducerPropertyId.values()) {
				boolean required = testMaterialsProducerPropertyId.getPropertyDefinition().getDefaultValue().isEmpty();
				if (required || randomGenerator.nextBoolean()) {
					materialsBuilder.setMaterialsProducerPropertyValue(testMaterialsProducerId, testMaterialsProducerPropertyId,
							testMaterialsProducerPropertyId.getRandomPropertyValue(randomGenerator));
				}
			}
			for (TestResourceId testResourceId : TestResourceId.values()) {
				if (randomGenerator.nextBoolean()) {
					long value = randomGenerator.nextInt(15) + 1;
					materialsBuilder.setMaterialsProducerResourceLevel(testMaterialsProducerId, testResourceId, value);
				}
			}
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

		// add the people plugin

		PeoplePluginData.Builder peopleBuilder = PeoplePluginData.builder();
		PeoplePluginData peoplePluginData = peopleBuilder.build();
		Plugin peoplePlugin = PeoplePlugin.getPeoplePlugin(peoplePluginData);
		builder.addPlugin(peoplePlugin);

		// add the regions plugin
		RegionsPluginData.Builder regionsBuilder = RegionsPluginData.builder();
		for (TestRegionId testRegionId : TestRegionId.values()) {
			regionsBuilder.addRegion(testRegionId);
		}
		RegionsPluginData regionsPluginData = regionsBuilder.build();
		Plugin regionPlugin = RegionsPlugin.getRegionsPlugin(regionsPluginData);
		builder.addPlugin(regionPlugin);

		// add the stochastics plugin
		StochasticsPluginData.Builder stochasticsBuilder = StochasticsPluginData.builder();
		stochasticsBuilder.setSeed(randomGenerator.nextLong());
		StochasticsPluginData stochasticsPluginData = stochasticsBuilder.build();
		Plugin stochasticsPlugin = StochasticsPlugin.getStochasticsPlugin(stochasticsPluginData);
		builder.addPlugin(stochasticsPlugin);

		TestPluginData.Builder pluginBuilder = TestPluginData.builder();

		// create an agent to show that materials initial data was properly
		// loaded as reflected in the data view
		pluginBuilder.addTestActorPlan("actor", new TestActorPlan(0, (c) -> {
			MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);
			ResourcesDataManager resourcesDataManager = c.getDataManager(ResourcesDataManager.class);

			// show that the correct materials producer ids are present
			assertEquals(materialsPluginData.getMaterialsProducerIds(), materialsDataManager.getMaterialsProducerIds());

			// show that the correct material ids are present
			assertEquals(materialsPluginData.getMaterialIds(), materialsDataManager.getMaterialIds());

			// show that the resource ids used for initial resource levels are
			// all contained in the resource plugin
			assertTrue(resourcesDataManager.getResourceIds().containsAll(materialsPluginData.getResourceIds()));

			// show that the material property ids are correct
			assertEquals(materialsPluginData.getMaterialsProducerPropertyIds(), materialsDataManager.getMaterialsProducerPropertyIds());

			// show that the material producer property definitions are correct
			for (MaterialsProducerPropertyId materialsProducerPropertyId : materialsPluginData.getMaterialsProducerPropertyIds()) {
				assertEquals(materialsPluginData.getMaterialsProducerPropertyDefinition(materialsProducerPropertyId),
						materialsDataManager.getMaterialsProducerPropertyDefinition(materialsProducerPropertyId));
			}

			/*
			 * Show that the material producer property values are correct. Show
			 * that the initial resource levels are correct.
			 */
			for (MaterialsProducerId materialsProducerId : materialsPluginData.getMaterialsProducerIds()) {
				for (MaterialsProducerPropertyId materialsProducerPropertyId : materialsPluginData.getMaterialsProducerPropertyIds()) {
					Map<MaterialsProducerPropertyId, Object> materialsProducerPropertyValues = materialsPluginData.getMaterialsProducerPropertyValues(materialsProducerId);
					Object expectedValue = materialsProducerPropertyValues.get(materialsProducerPropertyId);
					if (expectedValue == null) {
						PropertyDefinition propertyDefinition = materialsPluginData.getMaterialsProducerPropertyDefinition(materialsProducerPropertyId);
						expectedValue = propertyDefinition.getDefaultValue().get();
					}
					Object actualValue = materialsDataManager.getMaterialsProducerPropertyValue(materialsProducerId, materialsProducerPropertyId);
					assertEquals(expectedValue, actualValue);
				}
				for (ResourceId resourceId : resourcesDataManager.getResourceIds()) {
					Long expectedResourceLevel = materialsPluginData.getMaterialsProducerResourceLevel(materialsProducerId, resourceId);
					Long actualResourceLevel = materialsDataManager.getMaterialsProducerResourceLevel(materialsProducerId, resourceId);
					assertEquals(expectedResourceLevel, actualResourceLevel);
				}
			}
			/*
			 * Show that each material is associated with the correct batch
			 * property ids. Show that each batch property id has the correct
			 * definition.
			 */
			for (MaterialId materialId : materialsPluginData.getMaterialIds()) {
				assertEquals(materialsPluginData.getBatchPropertyIds(materialId), materialsDataManager.getBatchPropertyIds(materialId));
				for (BatchPropertyId batchPropertyId : materialsPluginData.getBatchPropertyIds(materialId)) {
					PropertyDefinition expectedPropertyDefinition = materialsPluginData.getBatchPropertyDefinition(materialId, batchPropertyId);
					PropertyDefinition actualPropertyDefinition = materialsDataManager.getBatchPropertyDefinition(materialId, batchPropertyId);
					assertEquals(expectedPropertyDefinition, actualPropertyDefinition);
				}
			}

			// Show that the correct initial batches are present.
			Set<BatchId> expectedBatchIds = new LinkedHashSet<>();
			for (BatchId batchId : materialsPluginData.getBatchIds()) {
				expectedBatchIds.add(batchId);
			}

			Set<BatchId> actualBatchIds = new LinkedHashSet<>();
			for (MaterialsProducerId materialsProducerId : materialsDataManager.getMaterialsProducerIds()) {
				actualBatchIds.addAll(materialsDataManager.getInventoryBatches(materialsProducerId));
				List<StageId> stages = materialsDataManager.getStages(materialsProducerId);
				for (StageId stageId : stages) {
					actualBatchIds.addAll(materialsDataManager.getStageBatches(stageId));
				}
			}
			assertEquals(expectedBatchIds, actualBatchIds);

			// Show that the batches have the correct material id, materials
			// producer and amounts

			for (BatchId batchId : materialsPluginData.getBatchIds()) {

				MaterialId expectedMaterialId = materialsPluginData.getBatchMaterial(batchId);
				MaterialId actualMaterialId = materialsDataManager.getBatchMaterial(batchId);
				assertEquals(expectedMaterialId, actualMaterialId);

				MaterialsProducerId expectedMaterialsProducerId = materialsPluginData.getBatchMaterialsProducer(batchId);
				MaterialsProducerId actualMaterialsProducerId = materialsDataManager.getBatchProducer(batchId);
				assertEquals(expectedMaterialsProducerId, actualMaterialsProducerId);

				double expectedAmount = materialsPluginData.getBatchAmount(batchId);
				double actualAmount = materialsDataManager.getBatchAmount(batchId);
				assertEquals(expectedAmount, actualAmount);

				for (BatchPropertyId batchPropertyId : materialsPluginData.getBatchPropertyIds(expectedMaterialId)) {
					Object expectedValue = materialsPluginData.getBatchPropertyValues(batchId).get(batchPropertyId);
					if (expectedValue == null) {
						PropertyDefinition propertyDefinition = materialsPluginData.getBatchPropertyDefinition(expectedMaterialId, batchPropertyId);
						expectedValue = propertyDefinition.getDefaultValue().get();
					}
					Object actualValue = materialsDataManager.getBatchPropertyValue(batchId, batchPropertyId);
					assertEquals(expectedValue, actualValue);
				}

			}

			// Show that the correct initial stages are present with their
			// normalized id values.
			Set<StageId> expectedStageIds = new LinkedHashSet<>();
			for (StageId stageId : materialsPluginData.getStageIds()) {
				expectedStageIds.add(stageId);
			}

			Set<StageId> actualStageIds = new LinkedHashSet<>();
			for (MaterialsProducerId materialsProducerId : materialsDataManager.getMaterialsProducerIds()) {
				actualStageIds.addAll(materialsDataManager.getStages(materialsProducerId));

			}
			assertEquals(expectedStageIds, actualStageIds);

			for (StageId stageId : materialsPluginData.getStageIds()) {

				MaterialsProducerId expectedMaterialsProducerId = materialsPluginData.getStageMaterialsProducer(stageId);
				MaterialsProducerId actualMaterialsProducerId = materialsDataManager.getStageProducer(stageId);
				assertEquals(expectedMaterialsProducerId, actualMaterialsProducerId);

				boolean expectedOfferState = materialsPluginData.isStageOffered(stageId);
				boolean actualOfferStage = materialsDataManager.isStageOffered(stageId);
				assertEquals(expectedOfferState, actualOfferStage);

				Set<BatchId> expectedBatches = new LinkedHashSet<>();
				for (BatchId batchId : materialsPluginData.getStageBatches(stageId)) {
					expectedBatches.add(batchId);
				}
				Set<BatchId> actualBatches = new LinkedHashSet<>(materialsDataManager.getStageBatches(stageId));
				assertEquals(expectedBatches, actualBatches);
			}

		}));

		// add the test plugin
		TestPluginData testPluginData = pluginBuilder.build();
		Plugin testPlugin = TestPlugin.getTestPlugin(testPluginData);
		builder.addPlugin(testPlugin);

		// build and execute the engine
		ScenarioPlanCompletionObserver scenarioPlanCompletionObserver = new ScenarioPlanCompletionObserver();
		builder.setOutputConsumer(scenarioPlanCompletionObserver::handleOutput).build().execute();

		// show that all actions were executed
		if (!scenarioPlanCompletionObserver.allPlansExecuted()) {
			throw new ContractException(TestError.TEST_EXECUTION_FAILURE);
		}

	}

	@Test
	@UnitTestMethod(name = "addMaterialsProducer", args = { MaterialsProducerConstructionData.class })
	public void testAddMaterialsProducer() {

		Set<MultiKey> expectedObservations = new LinkedHashSet<>();
		Set<MultiKey> actualObservations = new LinkedHashSet<>();

		TestPluginData.Builder pluginBuilder = TestPluginData.builder();
		pluginBuilder.addTestActorPlan("observer", new TestActorPlan(20, (c) -> {
			c.subscribe(MaterialsProducerAdditionEvent.class, (c2, e) -> {
				MultiKey multiKey = new MultiKey(c2.getTime(), e.getMaterialsProducerId());
				actualObservations.add(multiKey);
			});
		}));

		// show that a new materials producer can be added
		pluginBuilder.addTestActorPlan("actor", new TestActorPlan(20, (c) -> {
			StochasticsDataManager stochasticsDataManager = c.getDataManager(StochasticsDataManager.class);
			RandomGenerator randomGenerator = stochasticsDataManager.getRandomGenerator();
			MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);
			MaterialsProducerId newMaterialsProducerId = TestMaterialsProducerId.getUnknownMaterialsProducerId();
			assertFalse(materialsDataManager.materialsProducerIdExists(newMaterialsProducerId));

			MaterialsProducerConstructionData.Builder builder //
			= MaterialsProducerConstructionData	.builder()//
												.setMaterialsProducerId(newMaterialsProducerId);//

			Map<TestMaterialsProducerPropertyId, Object> expectedPropertyValues = new LinkedHashMap<>();
			for (TestMaterialsProducerPropertyId testMaterialsProducerPropertyId : TestMaterialsProducerPropertyId.values()) {
				Optional<Object> optional = testMaterialsProducerPropertyId.getPropertyDefinition().getDefaultValue();
				if (optional.isPresent()) {
					expectedPropertyValues.put(testMaterialsProducerPropertyId, optional.get());
				} else {
					Object randomPropertyValue = testMaterialsProducerPropertyId.getRandomPropertyValue(randomGenerator);
					builder.setMaterialsProducerPropertyValue(testMaterialsProducerPropertyId, randomPropertyValue);
					expectedPropertyValues.put(testMaterialsProducerPropertyId, randomPropertyValue);
				}
			}

			MaterialsProducerConstructionData materialsProducerConstructionData = builder.build();

			materialsDataManager.addMaterialsProducer(materialsProducerConstructionData);
			assertTrue(materialsDataManager.materialsProducerIdExists(newMaterialsProducerId));

			double expectedTime = c.getTime();
			int propertyTimeIsCurrentTimeCount = 0;
			for (TestMaterialsProducerPropertyId testMaterialsProducerPropertyId : TestMaterialsProducerPropertyId.values()) {
				Object expectedValue = expectedPropertyValues.get(testMaterialsProducerPropertyId);
				Object actualValue = materialsDataManager.getMaterialsProducerPropertyValue(newMaterialsProducerId, testMaterialsProducerPropertyId);
				assertEquals(expectedValue, actualValue);

				double actualTime = materialsDataManager.getMaterialsProducerPropertyTime(newMaterialsProducerId, testMaterialsProducerPropertyId);

				boolean defaultValueExists = testMaterialsProducerPropertyId.getPropertyDefinition().getDefaultValue().isPresent();

				if (defaultValueExists) {
					assertEquals(0.0, actualTime);
				} else {
					propertyTimeIsCurrentTimeCount++;
					assertEquals(expectedTime, actualTime);
				}
			}

			assertTrue(propertyTimeIsCurrentTimeCount > 0);
			long expectedLevel = 0;
			for (TestResourceId testResourceId : TestResourceId.values()) {
				long actualLevel = materialsDataManager.getMaterialsProducerResourceLevel(newMaterialsProducerId, testResourceId);
				assertEquals(expectedLevel, actualLevel);
				double actualTime = materialsDataManager.getMaterialsProducerResourceTime(newMaterialsProducerId, testResourceId);
				assertEquals(expectedTime, actualTime);
			}

			MultiKey multiKey = new MultiKey(c.getTime(), newMaterialsProducerId);
			expectedObservations.add(multiKey);
		}));

		pluginBuilder.addTestActorPlan("observer", new TestActorPlan(21, (c) -> {
			assertFalse(expectedObservations.isEmpty());
			assertEquals(expectedObservations, actualObservations);
		}));

		TestPluginData testPluginData = pluginBuilder.build();
		Plugin testPlugin = TestPlugin.getTestPlugin(testPluginData);
		MaterialsActionSupport.testConsumers(7934044435210594542L, testPlugin);

		/*
		 * precondition test: if the materials producer id is already present
		 */
		MaterialsActionSupport.testConsumer(6261955547781316622L, (c) -> {
			MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);
			StochasticsDataManager stochasticsDataManager = c.getDataManager(StochasticsDataManager.class);
			RandomGenerator randomGenerator = stochasticsDataManager.getRandomGenerator();
			MaterialsProducerConstructionData.Builder builder = //
					MaterialsProducerConstructionData.builder();
			builder.setMaterialsProducerId(TestMaterialsProducerId.MATERIALS_PRODUCER_1);
			for (TestMaterialsProducerPropertyId testMaterialsProducerPropertyId : TestMaterialsProducerPropertyId.getPropertiesWithoutDefaultValues()) {
				Object randomPropertyValue = testMaterialsProducerPropertyId.getRandomPropertyValue(randomGenerator);
				builder.setMaterialsProducerPropertyValue(testMaterialsProducerPropertyId, randomPropertyValue);
			}
			MaterialsProducerConstructionData materialsProducerConstructionData = builder.build();
			ContractException contractException = assertThrows(ContractException.class, () -> materialsDataManager.addMaterialsProducer(materialsProducerConstructionData));
			assertEquals(MaterialsError.DUPLICATE_MATERIALS_PRODUCER_ID, contractException.getErrorType());
		});

		/*
		 * precondition test: if the materialsProducerConstructionData does not
		 * contain a property value for any corresponding materials producer
		 * property definition that lacks a default value
		 */
		MaterialsActionSupport.testConsumer(1777796798041842032L, (c) -> {
			MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);
			MaterialsProducerId newMaterialsProducerId = TestMaterialsProducerId.getUnknownMaterialsProducerId();
			MaterialsProducerConstructionData materialsProducerConstructionData = MaterialsProducerConstructionData.builder().setMaterialsProducerId(newMaterialsProducerId).build();
			ContractException contractException = assertThrows(ContractException.class, () -> materialsDataManager.addMaterialsProducer(materialsProducerConstructionData));
			assertEquals(PropertyError.INSUFFICIENT_PROPERTY_VALUE_ASSIGNMENT, contractException.getErrorType());
		});

		/*
		 * precondition test: if the materialsProducerConstructionData contains
		 * a property value assignment for an unknown materials producer
		 * property id.
		 */
		MaterialsActionSupport.testConsumer(1777796798041842032L, (c) -> {
			MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);

			StochasticsDataManager stochasticsDataManager = c.getDataManager(StochasticsDataManager.class);
			RandomGenerator randomGenerator = stochasticsDataManager.getRandomGenerator();
			MaterialsProducerConstructionData.Builder builder = //
					MaterialsProducerConstructionData.builder();
			builder.setMaterialsProducerId(TestMaterialsProducerId.getUnknownMaterialsProducerId());
			for (TestMaterialsProducerPropertyId testMaterialsProducerPropertyId : TestMaterialsProducerPropertyId.getPropertiesWithoutDefaultValues()) {
				Object randomPropertyValue = testMaterialsProducerPropertyId.getRandomPropertyValue(randomGenerator);
				builder.setMaterialsProducerPropertyValue(testMaterialsProducerPropertyId, randomPropertyValue);
			}

			builder.setMaterialsProducerPropertyValue(TestMaterialsProducerPropertyId.getUnknownMaterialsProducerPropertyId(), 10);

			MaterialsProducerConstructionData materialsProducerConstructionData = builder.build();

			ContractException contractException = assertThrows(ContractException.class, () -> materialsDataManager.addMaterialsProducer(materialsProducerConstructionData));
			assertEquals(PropertyError.UNKNOWN_PROPERTY_ID, contractException.getErrorType());
		});

	}

	@Test
	@UnitTestMethod(name = "defineBatchProperty", args = { BatchPropertyDefinitionInitialization.class })
	public void testDefineBatchProperty() {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(7985084158958183488L);
		
		Set<MultiKey> expectedObservations = new LinkedHashSet<>();
		Set<MultiKey> actualObservations = new LinkedHashSet<>();

		TestPluginData.Builder pluginBuilder = TestPluginData.builder();

		pluginBuilder.addTestActorPlan("observer", new TestActorPlan(0, (c) -> {
			c.subscribe(BatchPropertyDefinitionEvent.class, (c2, e) -> {
				MultiKey multiKey = new MultiKey(c2.getTime(), e.getMaterialId(), e.getBatchPropertyId());
				actualObservations.add(multiKey);
			});
		}));

		pluginBuilder.addTestActorPlan("actor", new TestActorPlan(1, (c) -> {
			MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);
			PropertyDefinition propertyDefinition = PropertyDefinition.builder().setType(Integer.class).setDefaultValue(12).build();
			BatchPropertyId batchPropertyId = TestBatchPropertyId.getUnknownBatchPropertyId();
			MaterialId materialId = TestMaterialId.MATERIAL_1;
			BatchPropertyDefinitionInitialization batchPropertyDefinitionInitialization = BatchPropertyDefinitionInitialization	.builder().setMaterialId(materialId).setPropertyId(batchPropertyId)
																																.setPropertyDefinition(propertyDefinition).build();
			materialsDataManager.defineBatchProperty(batchPropertyDefinitionInitialization);
			MultiKey multiKey = new MultiKey(c.getTime(), materialId, batchPropertyId);
			expectedObservations.add(multiKey);
			assertTrue(materialsDataManager.getBatchPropertyIds(materialId).contains(batchPropertyId));
			PropertyDefinition actualPropertyDefinition = materialsDataManager.getBatchPropertyDefinition(materialId, batchPropertyId);
			assertEquals(propertyDefinition, actualPropertyDefinition);
		}));

		pluginBuilder.addTestActorPlan("actor", new TestActorPlan(2, (c) -> {
			MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);
			PropertyDefinition propertyDefinition = PropertyDefinition.builder().setType(String.class).setDefaultValue("default").build();
			BatchPropertyId batchPropertyId = TestBatchPropertyId.getUnknownBatchPropertyId();
			MaterialId materialId = TestMaterialId.MATERIAL_2;
			BatchPropertyDefinitionInitialization batchPropertyDefinitionInitialization = BatchPropertyDefinitionInitialization	.builder().setMaterialId(materialId).setPropertyId(batchPropertyId)
																																.setPropertyDefinition(propertyDefinition).build();
			materialsDataManager.defineBatchProperty(batchPropertyDefinitionInitialization);
			MultiKey multiKey = new MultiKey(c.getTime(), materialId, batchPropertyId);
			expectedObservations.add(multiKey);
			assertTrue(materialsDataManager.getBatchPropertyIds(materialId).contains(batchPropertyId));
			PropertyDefinition actualPropertyDefinition = materialsDataManager.getBatchPropertyDefinition(materialId, batchPropertyId);
			assertEquals(propertyDefinition, actualPropertyDefinition);
		}));

		pluginBuilder.addTestActorPlan("observer", new TestActorPlan(3, (c) -> {
			assertFalse(expectedObservations.isEmpty());
			assertEquals(expectedObservations, actualObservations);
		}));

		TestPluginData testPluginData = pluginBuilder.build();
		Plugin testPlugin = TestPlugin.getTestPlugin(testPluginData);
		MaterialsActionSupport.testConsumers(2434116219643564071L, testPlugin);

		/*
		 * precondition test: if the material id is unknown
		 */
		MaterialsActionSupport.testConsumer(3376758409444036216L, (c) -> {
			MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);
			PropertyDefinition propertyDefinition = PropertyDefinition.builder().setType(Integer.class).setDefaultValue(12).build();
			BatchPropertyId batchPropertyId = TestBatchPropertyId.getUnknownBatchPropertyId();
			MaterialId materialId = TestMaterialId.getUnknownMaterialId();

			BatchPropertyDefinitionInitialization batchPropertyDefinitionInitialization = //
					BatchPropertyDefinitionInitialization	.builder()//
															.setMaterialId(materialId)//
															.setPropertyId(batchPropertyId)//
															.setPropertyDefinition(propertyDefinition)//
															.build();
			ContractException contractException = assertThrows(ContractException.class, () -> materialsDataManager.defineBatchProperty(batchPropertyDefinitionInitialization));
			assertEquals(MaterialsError.UNKNOWN_MATERIAL_ID, contractException.getErrorType());
		});

		/*
		 * precondition test: if the batch property id is already present
		 */
		MaterialsActionSupport.testConsumer(7152319084879177681L, (c) -> {
			MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);
			PropertyDefinition propertyDefinition = PropertyDefinition.builder().setType(Integer.class).setDefaultValue(12).build();
			BatchPropertyId batchPropertyId = TestBatchPropertyId.BATCH_PROPERTY_1_3_DOUBLE_MUTABLE_NO_TRACK;
			MaterialId materialId = TestMaterialId.MATERIAL_1;

			BatchPropertyDefinitionInitialization batchPropertyDefinitionInitialization = //
					BatchPropertyDefinitionInitialization	.builder()//
															.setMaterialId(materialId)//
															.setPropertyId(batchPropertyId)//
															.setPropertyDefinition(propertyDefinition)//
															.build();

			ContractException contractException = assertThrows(ContractException.class, () -> materialsDataManager.defineBatchProperty(batchPropertyDefinitionInitialization));
			assertEquals(PropertyError.DUPLICATE_PROPERTY_DEFINITION, contractException.getErrorType());
		});

		// precondition test: if a batch property value assignment has an
		// unknown batch id</li>
		MaterialsActionSupport.testConsumer(8540977102873288312L, (c) -> {
			MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);
			PropertyDefinition propertyDefinition = PropertyDefinition.builder().setType(Integer.class).build();
			BatchPropertyId batchPropertyId = TestBatchPropertyId.getUnknownBatchPropertyId();
			MaterialId materialId = TestMaterialId.MATERIAL_1;

			BatchPropertyDefinitionInitialization batchPropertyDefinitionInitialization = //
					BatchPropertyDefinitionInitialization	.builder()//
															.setMaterialId(materialId)//
															.setPropertyId(batchPropertyId)//
															.setPropertyDefinition(propertyDefinition)//
															.addPropertyValue(new BatchId(765), 88)//
															.build();

			ContractException contractException = assertThrows(ContractException.class, () -> materialsDataManager.defineBatchProperty(batchPropertyDefinitionInitialization));
			assertEquals(MaterialsError.UNKNOWN_BATCH_ID, contractException.getErrorType());
		});

		/*
		 * precondition test: if a batch property value assignment has a batch
		 * id associated with a different material id type
		 */
		MaterialsActionSupport.testConsumer(8540977102873288312L, (c) -> {
			MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);
			PropertyDefinition propertyDefinition = PropertyDefinition.builder().setType(Integer.class).build();
			BatchPropertyId batchPropertyId = TestBatchPropertyId.getUnknownBatchPropertyId();
			MaterialId materialId = TestMaterialId.MATERIAL_1;
			MaterialId altMaterialId = TestMaterialId.MATERIAL_2;
			BatchConstructionInfo batchConstructionInfo = TestBatchConstructionInfo.getBatchConstructionInfo(TestMaterialsProducerId.MATERIALS_PRODUCER_1, altMaterialId, 345.0, randomGenerator);
			BatchId batchId = materialsDataManager.addBatch(batchConstructionInfo);

			BatchPropertyDefinitionInitialization batchPropertyDefinitionInitialization = //
					BatchPropertyDefinitionInitialization	.builder()//
															.setMaterialId(materialId)//
															.setPropertyId(batchPropertyId)//
															.setPropertyDefinition(propertyDefinition)//
															.addPropertyValue(batchId, 45)//
															.build();

			ContractException contractException = assertThrows(ContractException.class, () -> materialsDataManager.defineBatchProperty(batchPropertyDefinitionInitialization));
			assertEquals(MaterialsError.MATERIAL_TYPE_MISMATCH, contractException.getErrorType());
		});

	}

	@Test
	@UnitTestMethod(name = "addMaterialId", args = { MaterialId.class })
	public void testAddMaterialId() {

		Set<MultiKey> expectedObservations = new LinkedHashSet<>();
		Set<MultiKey> actualObservations = new LinkedHashSet<>();

		TestPluginData.Builder pluginBuilder = TestPluginData.builder();

		pluginBuilder.addTestActorPlan("observer", new TestActorPlan(0, (c) -> {
			c.subscribe(MaterialIdAdditionEvent.class, (c2, e) -> {
				MultiKey multiKey = new MultiKey(c2.getTime(), e.getMaterialId());
				actualObservations.add(multiKey);
			});
		}));

		pluginBuilder.addTestActorPlan("actor", new TestActorPlan(1, (c) -> {
			MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);
			MaterialId newMaterialId = TestMaterialId.getUnknownMaterialId();
			materialsDataManager.addMaterialId(newMaterialId);
			MultiKey multiKey = new MultiKey(c.getTime(), newMaterialId);
			expectedObservations.add(multiKey);
		}));

		pluginBuilder.addTestActorPlan("actor", new TestActorPlan(2, (c) -> {
			MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);
			MaterialId newMaterialId = TestMaterialId.getUnknownMaterialId();
			materialsDataManager.addMaterialId(newMaterialId);
			MultiKey multiKey = new MultiKey(c.getTime(), newMaterialId);
			expectedObservations.add(multiKey);
		}));

		pluginBuilder.addTestActorPlan("observer", new TestActorPlan(3, (c) -> {
			assertFalse(expectedObservations.isEmpty());
			assertEquals(expectedObservations, actualObservations);
		}));

		TestPluginData testPluginData = pluginBuilder.build();
		Plugin testPlugin = TestPlugin.getTestPlugin(testPluginData);
		MaterialsActionSupport.testConsumers(2713286843450316570L, testPlugin);

		/*
		 * precondition test: if the material id is null
		 */
		MaterialsActionSupport.testConsumer(801838096204060748L, (c) -> {
			MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);
			ContractException contractException = assertThrows(ContractException.class, () -> materialsDataManager.addMaterialId(null));
			assertEquals(MaterialsError.NULL_MATERIAL_ID, contractException.getErrorType());
		});

		/*
		 * precondition test: if the material id is already present
		 */
		MaterialsActionSupport.testConsumer(4318358212946306160L, (c) -> {
			MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);
			ContractException contractException = assertThrows(ContractException.class, () -> materialsDataManager.addMaterialId(TestMaterialId.MATERIAL_1));
			assertEquals(MaterialsError.DUPLICATE_MATERIAL, contractException.getErrorType());
		});

	}
}
