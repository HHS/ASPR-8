package plugins.materials.datacontainers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.Set;

import javax.naming.Context;

import org.apache.commons.math3.random.RandomGenerator;
import org.junit.jupiter.api.Test;

import nucleus.DataView;
import nucleus.testsupport.actionplugin.ActionPluginInitializer;
import nucleus.testsupport.actionplugin.AgentActionPlan;
import plugins.components.datacontainers.ComponentDataView;
import plugins.materials.events.mutation.BatchCreationEvent;
import plugins.materials.events.mutation.BatchPropertyValueAssignmentEvent;
import plugins.materials.events.mutation.BatchRemovalRequestEvent;
import plugins.materials.events.mutation.MaterialsProducerPropertyValueAssignmentEvent;
import plugins.materials.events.mutation.MoveBatchToInventoryEvent;
import plugins.materials.events.mutation.MoveBatchToStageEvent;
import plugins.materials.events.mutation.StageCreationEvent;
import plugins.materials.events.mutation.StageOfferEvent;
import plugins.materials.events.mutation.StageToResourceConversionEvent;
import plugins.materials.support.BatchId;
import plugins.materials.support.BatchPropertyId;
import plugins.materials.support.MaterialId;
import plugins.materials.support.MaterialsError;
import plugins.materials.support.MaterialsProducerId;
import plugins.materials.support.MaterialsProducerPropertyId;
import plugins.materials.support.StageId;
import plugins.materials.testsupport.MaterialsActionSupport;
import plugins.materials.testsupport.TestBatchPropertyId;
import plugins.materials.testsupport.TestMaterialId;
import plugins.materials.testsupport.TestMaterialsProducerId;
import plugins.materials.testsupport.TestMaterialsProducerPropertyId;
import plugins.properties.support.PropertyDefinition;
import plugins.resources.support.ResourceError;
import plugins.resources.support.ResourceId;
import plugins.resources.testsupport.TestResourceId;
import plugins.stochastics.StochasticsDataManager;
import util.ContractException;
import util.MultiKey;
import util.MutableDouble;
import util.MutableInteger;
import util.MutableLong;
import util.annotations.UnitTest;
import util.annotations.UnitTestConstructor;
import util.annotations.UnitTestMethod;

@UnitTest(target = MaterialsDataView.class)
public final class AT_MaterialsDataView implements DataView {

	@Test
	@UnitTestConstructor(args = { Context.class, MaterialsDataManager.class })
	public void testConstructor() {
		// nothing to test
	}

	@Test
	@UnitTestMethod(name = "batchExists", args = { BatchId.class })
	public void testBatchExists() {

		ActionPluginInitializer.Builder pluginBuilder = ActionPluginInitializer.builder();

		/*
		 * Create a data structure to hold batch ids that have been removed and
		 * require flow of control to leave the agent before removal can be
		 * confirmed
		 */
		Map<TestMaterialsProducerId, Set<BatchId>> removalConfirmationBatches = new LinkedHashMap<>();
		for (TestMaterialsProducerId testMaterialsProducerId : TestMaterialsProducerId.values()) {
			removalConfirmationBatches.put(testMaterialsProducerId, new LinkedHashSet<>());

			pluginBuilder.addAgentActionPlan(testMaterialsProducerId, new AgentActionPlan(0, (c) -> {
				MaterialsDataView materialsDataView = c.getDataView(MaterialsDataView.class).get();
				StochasticsDataManager stochasticsDataManager = c.getDataView(StochasticsDataManager.class).get();
				RandomGenerator randomGenerator = stochasticsDataManager.getRandomGenerator();
				Set<BatchId> confimationBatches = removalConfirmationBatches.get(testMaterialsProducerId);
				for (TestMaterialId testMaterialId : TestMaterialId.values()) {
					double value = randomGenerator.nextDouble() * 100;
					// add the batch an show it exists
					c.resolveEvent(new BatchCreationEvent(testMaterialId, value));
					BatchId batchId = materialsDataView.getLastIssuedBatchId().get();
					assertTrue(materialsDataView.batchExists(batchId));
					// remove the batch and show that it still exists
					c.resolveEvent(new BatchRemovalRequestEvent(batchId));
					assertTrue(materialsDataView.batchExists(batchId));
					confimationBatches.add(batchId);
				}
				// show that null and unknown batch ids return false
				assertFalse(materialsDataView.batchExists(null));
				assertFalse(materialsDataView.batchExists(new BatchId(10000000)));

			}));

			// show that the batches removed above are now gone
			pluginBuilder.addAgentActionPlan(testMaterialsProducerId, new AgentActionPlan(0, (c) -> {
				MaterialsDataView materialsDataView = c.getDataView(MaterialsDataView.class).get();
				Set<BatchId> confimationBatches = removalConfirmationBatches.get(testMaterialsProducerId);
				for (BatchId batchId : confimationBatches) {
					assertFalse(materialsDataView.batchExists(batchId));
				}
			}));
		}

		ActionPluginInitializer actionPluginInitializer = pluginBuilder.build();
		MaterialsActionSupport.testConsumers(3680467733415023569L, actionPluginInitializer);
	}

	@Test
	@UnitTestMethod(name = "getBatchTime", args = { BatchId.class })
	public void testGetBatchTime() {

		ActionPluginInitializer.Builder pluginBuilder = ActionPluginInitializer.builder();

		Map<BatchId, Double> expectedBatchTimes = new LinkedHashMap<>();

		pluginBuilder.addAgent("agent");

		TestMaterialsProducerId materialsProducerId = TestMaterialsProducerId.MATERIALS_PRODUCER_1;
		// build batches at several times
		for (int i = 1; i <= 10; i++) {
			materialsProducerId = materialsProducerId.next();
			double planTime = i;
			pluginBuilder.addAgentActionPlan(materialsProducerId, new AgentActionPlan(planTime, (c) -> {
				MaterialsDataView materialsDataView = c.getDataView(MaterialsDataView.class).get();

				StochasticsDataManager stochasticsDataManager = c.getDataView(StochasticsDataManager.class).get();
				RandomGenerator randomGenerator = stochasticsDataManager.getRandomGenerator();

				// build a few batches
				int numberOfBatches = randomGenerator.nextInt(5) + 1;
				for (int j = 0; j < numberOfBatches; j++) {

					TestMaterialId materialId = TestMaterialId.getRandomMaterialId(randomGenerator);
					double amount = randomGenerator.nextDouble();
					c.resolveEvent(new BatchCreationEvent(materialId, amount));
					BatchId batchId = materialsDataView.getLastIssuedBatchId().get();
					expectedBatchTimes.put(batchId, c.getTime());
				}

				// show that all batches have the expected batch times
				ComponentDataView componentDataView = c.getDataView(ComponentDataView.class).get();

				List<BatchId> inventoryBatches = materialsDataView.getInventoryBatches(componentDataView.getFocalComponentId());
				for (BatchId batchId : inventoryBatches) {
					double expectedBatchTime = expectedBatchTimes.get(batchId);
					double actualBatchTime = materialsDataView.getBatchTime(batchId);
					assertEquals(expectedBatchTime, actualBatchTime);
				}

			}));

			// precondition tests
			pluginBuilder.addAgentActionPlan("agent", new AgentActionPlan(11, (c) -> {
				MaterialsDataView materialsDataView = c.getDataView(MaterialsDataView.class).get();

				// if the batch id is null
				ContractException contractException = assertThrows(ContractException.class, () -> materialsDataView.getBatchTime(null));
				assertEquals(MaterialsError.NULL_BATCH_ID, contractException.getErrorType());
				// if the batch id is unknown
				contractException = assertThrows(ContractException.class, () -> materialsDataView.getBatchTime(new BatchId(1000000)));
				assertEquals(MaterialsError.UNKNOWN_BATCH_ID, contractException.getErrorType());
			}));
		}
		ActionPluginInitializer actionPluginInitializer = pluginBuilder.build();
		MaterialsActionSupport.testConsumers(8449887495666455982L, actionPluginInitializer);
	}

	@Test
	@UnitTestMethod(name = "getBatchAmount", args = { BatchId.class })
	public void testGetBatchAmount() {

		ActionPluginInitializer.Builder pluginBuilder = ActionPluginInitializer.builder();

		pluginBuilder.addAgentActionPlan(TestMaterialsProducerId.MATERIALS_PRODUCER_1, new AgentActionPlan(0, (c) -> {

			MaterialsDataView materialsDataView = c.getDataView(MaterialsDataView.class).get();
			StochasticsDataManager stochasticsDataManager = c.getDataView(StochasticsDataManager.class).get();
			RandomGenerator randomGenerator = stochasticsDataManager.getRandomGenerator();

			for (int i = 0; i < 100; i++) {
				TestMaterialId testMaterialId = TestMaterialId.getRandomMaterialId(randomGenerator);
				double value = randomGenerator.nextDouble() * 100;
				c.resolveEvent(new BatchCreationEvent(testMaterialId, value));
				BatchId batchId = materialsDataView.getLastIssuedBatchId().get();

				// show that the batch matches the inputs
				assertEquals(value, materialsDataView.getBatchAmount(batchId));
			}

			// precondition tests : none

			// if the batch id is null
			ContractException contractException = assertThrows(ContractException.class, () -> materialsDataView.getBatchAmount(null));
			assertEquals(MaterialsError.NULL_BATCH_ID, contractException.getErrorType());

			// if the batch id is unknown
			contractException = assertThrows(ContractException.class, () -> materialsDataView.getBatchAmount(new BatchId(10000000)));
			assertEquals(MaterialsError.UNKNOWN_BATCH_ID, contractException.getErrorType());

		}));

		ActionPluginInitializer actionPluginInitializer = pluginBuilder.build();
		MaterialsActionSupport.testConsumers(1333558356470864456L, actionPluginInitializer);
	}

	@Test
	@UnitTestMethod(name = "getBatchMaterial", args = { BatchId.class })
	public void testGetBatchMaterial() {

		ActionPluginInitializer.Builder pluginBuilder = ActionPluginInitializer.builder();

		pluginBuilder.addAgentActionPlan(TestMaterialsProducerId.MATERIALS_PRODUCER_2, new AgentActionPlan(0, (c) -> {

			MaterialsDataView materialsDataView = c.getDataView(MaterialsDataView.class).get();
			StochasticsDataManager stochasticsDataManager = c.getDataView(StochasticsDataManager.class).get();
			RandomGenerator randomGenerator = stochasticsDataManager.getRandomGenerator();

			for (int i = 0; i < 100; i++) {
				TestMaterialId testMaterialId = TestMaterialId.getRandomMaterialId(randomGenerator);
				double value = randomGenerator.nextDouble() * 100;
				c.resolveEvent(new BatchCreationEvent(testMaterialId, value));
				BatchId batchId = materialsDataView.getLastIssuedBatchId().get();

				// show that the batch matches the inputs
				assertEquals(testMaterialId, materialsDataView.getBatchMaterial(batchId));
			}

			// precondition tests : none

			// if the batch id is null
			ContractException contractException = assertThrows(ContractException.class, () -> materialsDataView.getBatchMaterial(null));
			assertEquals(MaterialsError.NULL_BATCH_ID, contractException.getErrorType());

			// if the batch id is unknown
			contractException = assertThrows(ContractException.class, () -> materialsDataView.getBatchMaterial(new BatchId(10000000)));
			assertEquals(MaterialsError.UNKNOWN_BATCH_ID, contractException.getErrorType());

		}));
		ActionPluginInitializer actionPluginInitializer = pluginBuilder.build();
		MaterialsActionSupport.testConsumers(2922188778885130752L, actionPluginInitializer);
	}

	@Test
	@UnitTestMethod(name = "getBatchStageId", args = { BatchId.class })
	public void testGetBatchStageId() {
		ActionPluginInitializer.Builder pluginBuilder = ActionPluginInitializer.builder();

		pluginBuilder.addAgentActionPlan(TestMaterialsProducerId.MATERIALS_PRODUCER_3, new AgentActionPlan(0, (c) -> {

			MaterialsDataView materialsDataView = c.getDataView(MaterialsDataView.class).get();
			// create a stage and a batch
			c.resolveEvent(new StageCreationEvent());
			StageId stageId = materialsDataView.getLastIssuedStageId().get();
			c.resolveEvent(new BatchCreationEvent(TestMaterialId.MATERIAL_2, 4.5));
			BatchId batchId = materialsDataView.getLastIssuedBatchId().get();

			// show that the batch does not have a stage
			Optional<StageId> optionalOwningStageId = materialsDataView.getBatchStageId(batchId);
			assertFalse(optionalOwningStageId.isPresent());

			// put the batch onto the stage
			c.resolveEvent(new MoveBatchToStageEvent(batchId, stageId));

			// show that the batch is on the stage
			optionalOwningStageId = materialsDataView.getBatchStageId(batchId);
			assertTrue(optionalOwningStageId.isPresent());
			assertEquals(stageId, optionalOwningStageId.get());

			// put the batch back into inventory
			c.resolveEvent(new MoveBatchToInventoryEvent(batchId));

			// show that the batch is not on any stage
			optionalOwningStageId = materialsDataView.getBatchStageId(batchId);
			assertFalse(optionalOwningStageId.isPresent());

			// precondition tests:

			// if the batch id is null
			ContractException contractException = assertThrows(ContractException.class, () -> materialsDataView.getBatchStageId(null));
			assertEquals(MaterialsError.NULL_BATCH_ID, contractException.getErrorType());

			// if the batch id is unknown
			contractException = assertThrows(ContractException.class, () -> materialsDataView.getBatchStageId(new BatchId(100000)));
			assertEquals(MaterialsError.UNKNOWN_BATCH_ID, contractException.getErrorType());

		}));

		ActionPluginInitializer actionPluginInitializer = pluginBuilder.build();
		MaterialsActionSupport.testConsumers(472707250737446845L, actionPluginInitializer);
	}

	@Test
	@UnitTestMethod(name = "stageExists", args = { StageId.class })
	public void testStageExists() {

		ActionPluginInitializer.Builder pluginBuilder = ActionPluginInitializer.builder();

		pluginBuilder.addAgentActionPlan(TestMaterialsProducerId.MATERIALS_PRODUCER_3, new AgentActionPlan(0, (c) -> {
			MaterialsDataView materialsDataView = c.getDataView(MaterialsDataView.class).get();

			for (int i = 0; i < 10; i++) {
				c.resolveEvent(new StageCreationEvent());
				StageId stageId = materialsDataView.getLastIssuedStageId().get();
				assertTrue(materialsDataView.stageExists(stageId));
			}

			assertFalse(materialsDataView.stageExists(null));

			assertFalse(materialsDataView.stageExists(new StageId(123)));
		}));

		ActionPluginInitializer actionPluginInitializer = pluginBuilder.build();
		MaterialsActionSupport.testConsumers(4646356228574091149L, actionPluginInitializer);
	}

	@Test
	@UnitTestMethod(name = "isStageOffered", args = { StageId.class })
	public void testIsStageOffered() {

		ActionPluginInitializer.Builder pluginBuilder = ActionPluginInitializer.builder();

		pluginBuilder.addAgentActionPlan(TestMaterialsProducerId.MATERIALS_PRODUCER_1, new AgentActionPlan(0, (c) -> {
			MaterialsDataView materialsDataView = c.getDataView(MaterialsDataView.class).get();

			for (int i = 0; i < 100; i++) {
				c.resolveEvent(new StageCreationEvent());
				StageId stageId = materialsDataView.getLastIssuedStageId().get();
				assertFalse(materialsDataView.isStageOffered(stageId));
				c.resolveEvent(new StageOfferEvent(stageId, true));
				assertTrue(materialsDataView.isStageOffered(stageId));
			}

			// precondition tests

			// if the stage id is null
			ContractException contractException = assertThrows(ContractException.class, () -> materialsDataView.isStageOffered(null));
			assertEquals(MaterialsError.NULL_STAGE_ID, contractException.getErrorType());

			// if the stage id is unknown
			contractException = assertThrows(ContractException.class, () -> materialsDataView.isStageOffered(new StageId(1000000)));
			assertEquals(MaterialsError.UNKNOWN_STAGE_ID, contractException.getErrorType());

		}));

		ActionPluginInitializer actionPluginInitializer = pluginBuilder.build();
		MaterialsActionSupport.testConsumers(475901778920012875L, actionPluginInitializer);
	}

	@Test
	@UnitTestMethod(name = "getStages", args = { MaterialsProducerId.class })
	public void testGetStages() {

		ActionPluginInitializer.Builder pluginBuilder = ActionPluginInitializer.builder();

		for (TestMaterialsProducerId testMaterialsProducerId : TestMaterialsProducerId.values()) {
			pluginBuilder.addAgentActionPlan(testMaterialsProducerId, new AgentActionPlan(0, (c) -> {

				MaterialsDataView materialsDataView = c.getDataView(MaterialsDataView.class).get();
				StochasticsDataManager stochasticsDataManager = c.getDataView(StochasticsDataManager.class).get();
				RandomGenerator randomGenerator = stochasticsDataManager.getRandomGenerator();

				Set<StageId> expectedStages = new LinkedHashSet<>();

				int count = randomGenerator.nextInt(10) + 1;
				for (int i = 0; i < count; i++) {
					c.resolveEvent(new StageCreationEvent());
					StageId stageId = materialsDataView.getLastIssuedStageId().get();
					expectedStages.add(stageId);
				}

				List<StageId> stages = materialsDataView.getStages(testMaterialsProducerId);

				Set<StageId> actualStageIds = new LinkedHashSet<>(stages);
				assertEquals(stages.size(), actualStageIds.size());
				assertEquals(expectedStages, actualStageIds);

				// if the materials producer id is null
				ContractException contractException = assertThrows(ContractException.class, () -> materialsDataView.getStages(null));
				assertEquals(MaterialsError.NULL_MATERIALS_PRODUCER_ID, contractException.getErrorType());

				// if the materials producer id is unknown
				contractException = assertThrows(ContractException.class, () -> materialsDataView.getStages(TestMaterialsProducerId.getUnknownMaterialsProducerId()));
				assertEquals(MaterialsError.UNKNOWN_MATERIALS_PRODUCER_ID, contractException.getErrorType());

			}));
		}

		ActionPluginInitializer actionPluginInitializer = pluginBuilder.build();
		MaterialsActionSupport.testConsumers(3431193355375533655L, actionPluginInitializer);

	}

	@Test
	@UnitTestMethod(name = "getOfferedStages", args = { MaterialsProducerId.class })
	public void testGetOfferedStages() {

		ActionPluginInitializer.Builder pluginBuilder = ActionPluginInitializer.builder();

		for (TestMaterialsProducerId testMaterialsProducerId : TestMaterialsProducerId.values()) {

			pluginBuilder.addAgentActionPlan(testMaterialsProducerId, new AgentActionPlan(0, (c) -> {
				MaterialsDataView materialsDataView = c.getDataView(MaterialsDataView.class).get();
				StochasticsDataManager stochasticsDataManager = c.getDataView(StochasticsDataManager.class).get();
				RandomGenerator randomGenerator = stochasticsDataManager.getRandomGenerator();

				// create several stages and place about half of them into the
				// offer
				// state
				Set<StageId> expectedStageOffers = new LinkedHashSet<>();

				for (int i = 0; i < 100; i++) {

					c.resolveEvent(new StageCreationEvent());
					StageId stageId = materialsDataView.getLastIssuedStageId().get();
					if (randomGenerator.nextBoolean()) {
						expectedStageOffers.add(stageId);
						c.resolveEvent(new StageOfferEvent(stageId, true));
					}
				}

				List<StageId> stages = materialsDataView.getOfferedStages(testMaterialsProducerId);
				Set<StageId> actualStages = new LinkedHashSet<>(stages);
				assertEquals(stages.size(), actualStages.size());
				assertEquals(expectedStageOffers, actualStages);

				// precondition tests

				// if the materials producerId id is null
				ContractException contractException = assertThrows(ContractException.class, () -> materialsDataView.getOfferedStages(null));
				assertEquals(MaterialsError.NULL_MATERIALS_PRODUCER_ID, contractException.getErrorType());

				// if the materials producerId id is unknown
				contractException = assertThrows(ContractException.class, () -> materialsDataView.getOfferedStages(TestMaterialsProducerId.getUnknownMaterialsProducerId()));
				assertEquals(MaterialsError.UNKNOWN_MATERIALS_PRODUCER_ID, contractException.getErrorType());
			}));
		}

		ActionPluginInitializer actionPluginInitializer = pluginBuilder.build();
		MaterialsActionSupport.testConsumers(7995017020582510238L, actionPluginInitializer);

	}

	@Test
	@UnitTestMethod(name = "getStageBatchesByMaterialId", args = { StageId.class, MaterialId.class })
	public void testGetStageBatchesByMaterialId() {
		ActionPluginInitializer.Builder pluginBuilder = ActionPluginInitializer.builder();

		// build a structure to hold the batches expected per stage and
		// material id
		Map<StageId, Map<MaterialId, Set<BatchId>>> expectedStageBatches = new LinkedHashMap<>();

		// have each of the materials producers create and stage some batches
		for (TestMaterialsProducerId testMaterialsProducerId : TestMaterialsProducerId.values()) {

			pluginBuilder.addAgentActionPlan(testMaterialsProducerId, new AgentActionPlan(0, (c) -> {
				MaterialsDataView materialsDataView = c.getDataView(MaterialsDataView.class).get();

				StochasticsDataManager stochasticsDataManager = c.getDataView(StochasticsDataManager.class).get();
				RandomGenerator randomGenerator = stochasticsDataManager.getRandomGenerator();

				// create some stages and batches
				for (int i = 0; i < 10; i++) {
					c.resolveEvent(new StageCreationEvent());
					StageId stageId = materialsDataView.getLastIssuedStageId().get();
					int batchCount = randomGenerator.nextInt(10) + 1;
					Map<MaterialId, Set<BatchId>> map = new LinkedHashMap<>();
					for (int j = 0; j < batchCount; j++) {
						TestMaterialId materialId = TestMaterialId.getRandomMaterialId(randomGenerator);
						Set<BatchId> batches = map.get(materialId);
						if (batches == null) {
							batches = new LinkedHashSet<>();
							map.put(materialId, batches);
						}
						c.resolveEvent(new BatchCreationEvent(materialId, randomGenerator.nextInt(100)));
						BatchId batchId = materialsDataView.getLastIssuedBatchId().get();
						batches.add(batchId);
						c.resolveEvent(new MoveBatchToStageEvent(batchId, stageId));
					}
					expectedStageBatches.put(stageId, map);
				}

			}));
		}

		// have an agent show that the batches are staged as expected
		pluginBuilder.addAgent("agent");
		pluginBuilder.addAgentActionPlan("agent", new AgentActionPlan(1, (c) -> {

			MaterialsDataView materialsDataView = c.getDataView(MaterialsDataView.class).get();

			/*
			 * gather the actual stage batches in a structure identical to the
			 * expected values
			 */
			Map<StageId, Map<MaterialId, Set<BatchId>>> actualStageBatches = new LinkedHashMap<>();
			for (MaterialsProducerId materialsProducerId : materialsDataView.getMaterialsProducerIds()) {
				List<StageId> stageIds = materialsDataView.getStages(materialsProducerId);

				for (StageId stageId : stageIds) {
					Map<MaterialId, Set<BatchId>> map = new LinkedHashMap<>();
					actualStageBatches.put(stageId, map);
					List<BatchId> stageBatches = materialsDataView.getStageBatches(stageId);
					for (BatchId batchId : stageBatches) {
						MaterialId materialId = materialsDataView.getBatchMaterial(batchId);
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
		pluginBuilder.addAgentActionPlan("agent", new AgentActionPlan(2, (c) -> {
			MaterialsDataView materialsDataView = c.getDataView(MaterialsDataView.class).get();

			// if the stage id is null
			ContractException contractException = assertThrows(ContractException.class, () -> materialsDataView.getStageBatchesByMaterialId(null, TestMaterialId.MATERIAL_1));
			assertEquals(MaterialsError.NULL_STAGE_ID, contractException.getErrorType());

			// if the stage id is unknown
			contractException = assertThrows(ContractException.class, () -> materialsDataView.getStageBatchesByMaterialId(new StageId(10000000), TestMaterialId.MATERIAL_1));
			assertEquals(MaterialsError.UNKNOWN_STAGE_ID, contractException.getErrorType());

			// if the material id is null
			contractException = assertThrows(ContractException.class, () -> materialsDataView.getStageBatchesByMaterialId(new StageId(0), null));
			assertEquals(MaterialsError.NULL_MATERIAL_ID, contractException.getErrorType());

			// if the material id is unknown
			contractException = assertThrows(ContractException.class, () -> materialsDataView.getStageBatchesByMaterialId(new StageId(0), TestMaterialId.getUnknownMaterialId()));
			assertEquals(MaterialsError.UNKNOWN_MATERIAL_ID, contractException.getErrorType());

		}));

		ActionPluginInitializer actionPluginInitializer = pluginBuilder.build();
		MaterialsActionSupport.testConsumers(2013967899243685546L, actionPluginInitializer);
	}

	@Test
	@UnitTestMethod(name = "getStageBatches", args = { StageId.class })
	public void testGetStageBatches() {
		ActionPluginInitializer.Builder pluginBuilder = ActionPluginInitializer.builder();

		// create a structure to hold expectations
		Map<StageId, Set<BatchId>> expectedStageBatches = new LinkedHashMap<>();

		// have each of the materials producers create and stage some batches
		for (TestMaterialsProducerId testMaterialsProducerId : TestMaterialsProducerId.values()) {

			pluginBuilder.addAgentActionPlan(testMaterialsProducerId, new AgentActionPlan(0, (c) -> {
				MaterialsDataView materialsDataView = c.getDataView(MaterialsDataView.class).get();

				StochasticsDataManager stochasticsDataManager = c.getDataView(StochasticsDataManager.class).get();
				RandomGenerator randomGenerator = stochasticsDataManager.getRandomGenerator();

				// create some stages and batches
				for (int i = 0; i < 10; i++) {
					c.resolveEvent(new StageCreationEvent());
					StageId stageId = materialsDataView.getLastIssuedStageId().get();
					int batchCount = randomGenerator.nextInt(3) + 1;
					Set<BatchId> batches = new LinkedHashSet<>();
					for (int j = 0; j < batchCount; j++) {
						c.resolveEvent(new BatchCreationEvent(TestMaterialId.getRandomMaterialId(randomGenerator), randomGenerator.nextInt(100)));
						BatchId batchId = materialsDataView.getLastIssuedBatchId().get();
						batches.add(batchId);
						c.resolveEvent(new MoveBatchToStageEvent(batchId, stageId));
					}
					expectedStageBatches.put(stageId, batches);
				}
			}));
		}
		// have an agent show that the batches are staged as expected
		pluginBuilder.addAgent("agent");
		pluginBuilder.addAgentActionPlan("agent", new AgentActionPlan(1, (c) -> {
			MaterialsDataView materialsDataView = c.getDataView(MaterialsDataView.class).get();
			Map<StageId, Set<BatchId>> actualStageBatches = new LinkedHashMap<>();
			for (MaterialsProducerId materialsProducerId : materialsDataView.getMaterialsProducerIds()) {
				List<StageId> stageIds = materialsDataView.getStages(materialsProducerId);
				for (StageId stageId : stageIds) {
					actualStageBatches.put(stageId, new LinkedHashSet<>(materialsDataView.getStageBatches(stageId)));
				}
			}

			assertEquals(expectedStageBatches, actualStageBatches);
		}));

		// precondition tests
		pluginBuilder.addAgentActionPlan("agent", new AgentActionPlan(2, (c) -> {
			MaterialsDataView materialsDataView = c.getDataView(MaterialsDataView.class).get();

			// if the stage id is null
			ContractException contractException = assertThrows(ContractException.class, () -> materialsDataView.getStageBatches(null));
			assertEquals(MaterialsError.NULL_STAGE_ID, contractException.getErrorType());

			// if the stage id is unknown
			contractException = assertThrows(ContractException.class, () -> materialsDataView.getStageBatches(new StageId(10000000)));
			assertEquals(MaterialsError.UNKNOWN_STAGE_ID, contractException.getErrorType());

		}));

		ActionPluginInitializer actionPluginInitializer = pluginBuilder.build();
		MaterialsActionSupport.testConsumers(3682458920522952415L, actionPluginInitializer);

	}

	@Test
	@UnitTestMethod(name = "getInventoryBatchesByMaterialId", args = { MaterialsProducerId.class, MaterialId.class })
	public void testGetInventoryBatchesByMaterialId() {

		ActionPluginInitializer.Builder pluginBuilder = ActionPluginInitializer.builder();

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
		for (TestMaterialsProducerId testMaterialsProducerId : TestMaterialsProducerId.values()) {

			pluginBuilder.addAgentActionPlan(testMaterialsProducerId, new AgentActionPlan(0, (c) -> {

				StochasticsDataManager stochasticsDataManager = c.getDataView(StochasticsDataManager.class).get();
				RandomGenerator randomGenerator = stochasticsDataManager.getRandomGenerator();
				MaterialsDataView materialsDataView = c.getDataView(MaterialsDataView.class).get();
				Map<MaterialId, Set<BatchId>> materialToBatchesMap = expectedInventoryBatchesMap.get(testMaterialsProducerId);

				// create some (100) batches
				for (int i = 0; i < 100; i++) {
					MaterialId materialId = TestMaterialId.getRandomMaterialId(randomGenerator);
					c.resolveEvent(new BatchCreationEvent(materialId, randomGenerator.nextInt(100)));
					BatchId batchId = materialsDataView.getLastIssuedBatchId().get();
					materialToBatchesMap.get(materialId).add(batchId);
				}

				// show that the inventory batches are correct
				for (TestMaterialId testMaterialId : TestMaterialId.values()) {
					List<BatchId> batchList = materialsDataView.getInventoryBatchesByMaterialId(testMaterialsProducerId, testMaterialId);
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
					c.resolveEvent(new StageCreationEvent());
					StageId stageId = materialsDataView.getLastIssuedStageId().get();
					for (int j = 0; j < 5; j++) {
						BatchId batchId = batches.get(batchIndex++);
						c.resolveEvent(new MoveBatchToStageEvent(batchId, stageId));
						MaterialId batchMaterial = materialsDataView.getBatchMaterial(batchId);
						materialToBatchesMap.get(batchMaterial).remove(batchId);

					}
				}

				// destroy a few inventory batches
				for (int i = 0; i < 10; i++) {
					BatchId batchId = batches.get(batchIndex++);
					MaterialId batchMaterial = materialsDataView.getBatchMaterial(batchId);
					c.resolveEvent(new BatchRemovalRequestEvent(batchId));
					materialToBatchesMap.get(batchMaterial).remove(batchId);
				}

			}));
		}

		pluginBuilder.addAgent("agent");
		pluginBuilder.addAgentActionPlan("agent", new AgentActionPlan(1, (c) -> {
			MaterialsDataView materialsDataView = c.getDataView(MaterialsDataView.class).get();

			// show that the inventory batches are correct
			for (TestMaterialsProducerId testMaterialsProducerId : TestMaterialsProducerId.values()) {
				Map<MaterialId, Set<BatchId>> materialToBatchesMap = expectedInventoryBatchesMap.get(testMaterialsProducerId);
				for (TestMaterialId testMaterialId : TestMaterialId.values()) {
					List<BatchId> invBatches = materialsDataView.getInventoryBatchesByMaterialId(testMaterialsProducerId, testMaterialId);
					Set<BatchId> actualInventoryBatches = new LinkedHashSet<>(invBatches);
					assertEquals(invBatches.size(), actualInventoryBatches.size());
					Set<BatchId> expectedInventoryBatches = materialToBatchesMap.get(testMaterialId);
					assertEquals(expectedInventoryBatches, actualInventoryBatches);
				}
			}
		}));

		// precondition tests
		pluginBuilder.addAgent("agent");
		pluginBuilder.addAgentActionPlan("agent", new AgentActionPlan(2, (c) -> {
			MaterialsDataView materialsDataView = c.getDataView(MaterialsDataView.class).get();

			// if the materials producerId id is null
			ContractException contractException = assertThrows(ContractException.class, () -> materialsDataView.getInventoryBatchesByMaterialId(null, TestMaterialId.MATERIAL_1));
			assertEquals(MaterialsError.NULL_MATERIALS_PRODUCER_ID, contractException.getErrorType());

			// if the materials producerId id is unknown
			contractException = assertThrows(ContractException.class,
					() -> materialsDataView.getInventoryBatchesByMaterialId(TestMaterialsProducerId.getUnknownMaterialsProducerId(), TestMaterialId.MATERIAL_2));
			assertEquals(MaterialsError.UNKNOWN_MATERIALS_PRODUCER_ID, contractException.getErrorType());

			// if the material id is null
			contractException = assertThrows(ContractException.class, () -> materialsDataView.getInventoryBatchesByMaterialId(TestMaterialsProducerId.MATERIALS_PRODUCER_1, null));
			assertEquals(MaterialsError.NULL_MATERIAL_ID, contractException.getErrorType());

			// if the material id is null
			contractException = assertThrows(ContractException.class,
					() -> materialsDataView.getInventoryBatchesByMaterialId(TestMaterialsProducerId.MATERIALS_PRODUCER_1, TestMaterialId.getUnknownMaterialId()));
			assertEquals(MaterialsError.UNKNOWN_MATERIAL_ID, contractException.getErrorType());
		}));

		ActionPluginInitializer actionPluginInitializer = pluginBuilder.build();
		MaterialsActionSupport.testConsumers(8436700054410844417L, actionPluginInitializer);

	}

	@Test
	@UnitTestMethod(name = "getInventoryBatches", args = { MaterialsProducerId.class })
	public void testGetInventoryBatches() {

		ActionPluginInitializer.Builder pluginBuilder = ActionPluginInitializer.builder();

		// create a data structure to hold expectd inventories
		Map<TestMaterialsProducerId, Set<BatchId>> expectedInventoryBatchesByProducer = new LinkedHashMap<>();
		for (TestMaterialsProducerId testMaterialsProducerId : TestMaterialsProducerId.values()) {
			expectedInventoryBatchesByProducer.put(testMaterialsProducerId, new LinkedHashSet<>());
		}

		for (TestMaterialsProducerId testMaterialsProducerId : TestMaterialsProducerId.values()) {

			pluginBuilder.addAgentActionPlan(testMaterialsProducerId, new AgentActionPlan(0, (c) -> {

				StochasticsDataManager stochasticsDataManager = c.getDataView(StochasticsDataManager.class).get();
				RandomGenerator randomGenerator = stochasticsDataManager.getRandomGenerator();

				MaterialsDataView materialsDataView = c.getDataView(MaterialsDataView.class).get();

				Set<BatchId> expectedInventoryBatches = expectedInventoryBatchesByProducer.get(testMaterialsProducerId);

				// create some batches
				for (int i = 0; i < 100; i++) {
					c.resolveEvent(new BatchCreationEvent(TestMaterialId.getRandomMaterialId(randomGenerator), randomGenerator.nextInt(100)));
					BatchId batchId = materialsDataView.getLastIssuedBatchId().get();
					expectedInventoryBatches.add(batchId);
				}

				// show that the inventory batches are correct
				List<BatchId> inventory = materialsDataView.getInventoryBatches(testMaterialsProducerId);
				Set<BatchId> actualInventoryBatches = new LinkedHashSet<>(inventory);
				assertEquals(inventory.size(), actualInventoryBatches.size());
				assertEquals(expectedInventoryBatches, actualInventoryBatches);

				// create some stages and put some of the batches onto stages
				List<BatchId> batches = new ArrayList<>(expectedInventoryBatches);
				Collections.shuffle(batches, new Random(randomGenerator.nextLong()));
				int batchIndex = 0;
				for (int i = 0; i < 5; i++) {
					c.resolveEvent(new StageCreationEvent());
					StageId stageId = materialsDataView.getLastIssuedStageId().get();
					for (int j = 0; j < 5; j++) {
						BatchId batchId = batches.get(batchIndex++);
						c.resolveEvent(new MoveBatchToStageEvent(batchId, stageId));
						expectedInventoryBatches.remove(batchId);

					}
				}

				// destroy a few inventory batches
				for (int i = 0; i < 10; i++) {
					BatchId batchId = batches.get(batchIndex++);
					c.resolveEvent(new BatchRemovalRequestEvent(batchId));
					expectedInventoryBatches.remove(batchId);
				}

			}));

		}
		pluginBuilder.addAgent("agent");
		pluginBuilder.addAgentActionPlan("agent", new AgentActionPlan(1, (c) -> {
			MaterialsDataView materialsDataView = c.getDataView(MaterialsDataView.class).get();

			for (TestMaterialsProducerId testMaterialsProducerId : TestMaterialsProducerId.values()) {
				Set<BatchId> expectedInventoryBatches = expectedInventoryBatchesByProducer.get(testMaterialsProducerId);

				// show that the inventory batches are correct
				List<BatchId> batchList = materialsDataView.getInventoryBatches(testMaterialsProducerId);
				Set<BatchId> actualInventoryBatches = new LinkedHashSet<>(batchList);
				assertEquals(batchList.size(), actualInventoryBatches.size());
				assertEquals(expectedInventoryBatches, actualInventoryBatches);
			}
		}));

		// precondition tests
		pluginBuilder.addAgentActionPlan("agent", new AgentActionPlan(2, (c) -> {
			MaterialsDataView materialsDataView = c.getDataView(MaterialsDataView.class).get();
			// if the materials producerId id is null
			ContractException contractException = assertThrows(ContractException.class, () -> materialsDataView.getInventoryBatches(null));
			assertEquals(MaterialsError.NULL_MATERIALS_PRODUCER_ID, contractException.getErrorType());

			// if the materials producerId id is unknown
			contractException = assertThrows(ContractException.class, () -> materialsDataView.getInventoryBatches(TestMaterialsProducerId.getUnknownMaterialsProducerId()));
			assertEquals(MaterialsError.UNKNOWN_MATERIALS_PRODUCER_ID, contractException.getErrorType());

		}));
		ActionPluginInitializer actionPluginInitializer = pluginBuilder.build();
		MaterialsActionSupport.testConsumers(6343917844917632364L, actionPluginInitializer);
	}

	@Test
	@UnitTestMethod(name = "getStageProducer", args = { StageId.class })
	public void testGetStageProducer() {

		ActionPluginInitializer.Builder pluginBuilder = ActionPluginInitializer.builder();

		for (TestMaterialsProducerId testMaterialsProducerId : TestMaterialsProducerId.values()) {
			pluginBuilder.addAgentActionPlan(testMaterialsProducerId, new AgentActionPlan(0, (c) -> {

				MaterialsDataView materialsDataView = c.getDataView(MaterialsDataView.class).get();
				c.resolveEvent(new StageCreationEvent());
				StageId stageId = materialsDataView.getLastIssuedStageId().get();
				assertEquals(testMaterialsProducerId, materialsDataView.getStageProducer(stageId));

			}));
		}

		pluginBuilder.addAgent("agent");
		pluginBuilder.addAgentActionPlan("agent", new AgentActionPlan(0, (c) -> {

			MaterialsDataView materialsDataView = c.getDataView(MaterialsDataView.class).get();

			// if the stage id is null
			ContractException contractException = assertThrows(ContractException.class, () -> materialsDataView.getStageProducer(null));
			assertEquals(MaterialsError.NULL_STAGE_ID, contractException.getErrorType());

			// if the stage id is unknown
			contractException = assertThrows(ContractException.class, () -> materialsDataView.getStageProducer(new StageId(1000000000)));
			assertEquals(MaterialsError.UNKNOWN_STAGE_ID, contractException.getErrorType());

		}));
		ActionPluginInitializer actionPluginInitializer = pluginBuilder.build();
		MaterialsActionSupport.testConsumers(4322374809851867527L, actionPluginInitializer);

	}

	@Test
	@UnitTestMethod(name = "getBatchProducer", args = { BatchId.class })
	public void testGetBatchProducer() {

		ActionPluginInitializer.Builder pluginBuilder = ActionPluginInitializer.builder();

		for (TestMaterialsProducerId testMaterialsProducerId : TestMaterialsProducerId.values()) {
			pluginBuilder.addAgentActionPlan(testMaterialsProducerId, new AgentActionPlan(0, (c) -> {

				MaterialsDataView materialsDataView = c.getDataView(MaterialsDataView.class).get();

				StochasticsDataManager stochasticsDataManager = c.getDataView(StochasticsDataManager.class).get();
				RandomGenerator randomGenerator = stochasticsDataManager.getRandomGenerator();

				for (int i = 0; i < 10; i++) {

					TestMaterialId testMaterialId = TestMaterialId.getRandomMaterialId(randomGenerator);
					double value = randomGenerator.nextDouble() * 100;
					c.resolveEvent(new BatchCreationEvent(testMaterialId, value));
					BatchId batchId = materialsDataView.getLastIssuedBatchId().get();

					// show that the batch matches the inputs
					assertEquals(testMaterialsProducerId, materialsDataView.getBatchProducer(batchId));
				}

				// precondition tests : none

			}));
		}

		// precondition tests
		pluginBuilder.addAgent("agent");
		pluginBuilder.addAgentActionPlan("agent", new AgentActionPlan(0, (c) -> {

			MaterialsDataView materialsDataView = c.getDataView(MaterialsDataView.class).get();

			ContractException contractException = assertThrows(ContractException.class, () -> materialsDataView.getBatchProducer(null));
			assertEquals(MaterialsError.NULL_BATCH_ID, contractException.getErrorType());

			contractException = assertThrows(ContractException.class, () -> materialsDataView.getBatchProducer(new BatchId(10000000)));
			assertEquals(MaterialsError.UNKNOWN_BATCH_ID, contractException.getErrorType());

		}));

		ActionPluginInitializer actionPluginInitializer = pluginBuilder.build();
		MaterialsActionSupport.testConsumers(8873616248377004295L, actionPluginInitializer);

	}

	@Test
	@UnitTestMethod(name = "getMaterialIds", args = {})
	public void testGetMaterialIds() {
		MaterialsActionSupport.testConsumer(6611654668838622496L, (c) -> {
			MaterialsDataView materialsDataView = c.getDataView(MaterialsDataView.class).get();
			assertEquals(EnumSet.allOf(TestMaterialId.class), materialsDataView.getMaterialIds());
		});
	}

	@Test
	@UnitTestMethod(name = "getLastIssuedBatchId", args = {})
	public void testGetLastIssuedBatchId() {
		ActionPluginInitializer.Builder pluginBuilder = ActionPluginInitializer.builder();

		// show that no batches are issued at the start of the simulation
		pluginBuilder.addAgent("agent");
		pluginBuilder.addAgentActionPlan("agent", new AgentActionPlan(0, (c) -> {
			MaterialsDataView materialsDataView = c.getDataView(MaterialsDataView.class).get();
			assertFalse(materialsDataView.getLastIssuedBatchId().isPresent());
		}));

		// show that the last batch issued agrees with the expected serial
		// issuance of batch ids
		MutableInteger expectedBatchId = new MutableInteger();

		for (TestMaterialsProducerId testMaterialsProducerId : TestMaterialsProducerId.values()) {
			pluginBuilder.addAgentActionPlan(testMaterialsProducerId, new AgentActionPlan(1, (c) -> {

				StochasticsDataManager stochasticsDataManager = c.getDataView(StochasticsDataManager.class).get();
				RandomGenerator randomGenerator = stochasticsDataManager.getRandomGenerator();

				MaterialsDataView materialsDataView = c.getDataView(MaterialsDataView.class).get();

				for (int i = 0; i < 20; i++) {
					c.resolveEvent(new BatchCreationEvent(TestMaterialId.getRandomMaterialId(randomGenerator), randomGenerator.nextInt(100)));
					assertTrue(materialsDataView.getLastIssuedBatchId().isPresent());
					BatchId batchId = materialsDataView.getLastIssuedBatchId().get();
					assertEquals(new BatchId(expectedBatchId.getValue()), batchId);
					expectedBatchId.increment();
				}

			}));
		}

		ActionPluginInitializer actionPluginInitializer = pluginBuilder.build();
		MaterialsActionSupport.testConsumers(6566570142860363660L, actionPluginInitializer);
	}

	@Test
	@UnitTestMethod(name = "getLastIssuedStageId", args = {})
	public void testGetLastIssuedStageId() {

		ActionPluginInitializer.Builder pluginBuilder = ActionPluginInitializer.builder();

		// show that no batches are issued at the start of the simulation
		pluginBuilder.addAgent("agent");
		pluginBuilder.addAgentActionPlan("agent", new AgentActionPlan(0, (c) -> {
			MaterialsDataView materialsDataView = c.getDataView(MaterialsDataView.class).get();
			assertFalse(materialsDataView.getLastIssuedStageId().isPresent());
		}));

		// show that the last stage issued agrees with the expected serial
		// issuance of stage ids
		MutableInteger expectedStageId = new MutableInteger();

		for (TestMaterialsProducerId testMaterialsProducerId : TestMaterialsProducerId.values()) {
			pluginBuilder.addAgentActionPlan(testMaterialsProducerId, new AgentActionPlan(1, (c) -> {

				MaterialsDataView materialsDataView = c.getDataView(MaterialsDataView.class).get();

				for (int i = 0; i < 20; i++) {
					c.resolveEvent(new StageCreationEvent());
					assertTrue(materialsDataView.getLastIssuedStageId().isPresent());
					StageId stageId = materialsDataView.getLastIssuedStageId().get();
					assertEquals(new StageId(expectedStageId.getValue()), stageId);
					expectedStageId.increment();
				}

			}));
		}

		ActionPluginInitializer actionPluginInitializer = pluginBuilder.build();
		MaterialsActionSupport.testConsumers(5232199374875912323L, actionPluginInitializer);
	}

	@Test
	@UnitTestMethod(name = "getMaterialsProducerResourceLevel", args = { MaterialsProducerId.class, ResourceId.class })
	public void testGetMaterialsProducerResourceLevel() {

		ActionPluginInitializer.Builder pluginBuilder = ActionPluginInitializer.builder();
		pluginBuilder.addAgent("agent");

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
			pluginBuilder.addAgentActionPlan(testMaterialsProducerId, new AgentActionPlan(0, (c) -> {
				StochasticsDataManager stochasticsDataManager = c.getDataView(StochasticsDataManager.class).get();
				RandomGenerator randomGenerator = stochasticsDataManager.getRandomGenerator();
				MaterialsDataView materialsDataView = c.getDataView(MaterialsDataView.class).get();

				for (int j = 0; j < resourceLevelChangeCount; j++) {
					c.resolveEvent(new StageCreationEvent());
					StageId stageId = materialsDataView.getLastIssuedStageId().get();
					TestResourceId testResourceId = TestResourceId.getRandomResourceId(randomGenerator);
					Long resourceLevel = (long) (randomGenerator.nextInt(100) + 1);
					c.resolveEvent(new StageToResourceConversionEvent(stageId, testResourceId, resourceLevel));
					MultiKey multiKey = new MultiKey(testMaterialsProducerId, testResourceId);
					MutableLong mutableLong = expectedLevelsMap.get(multiKey);
					mutableLong.increment(resourceLevel);
				}
			}));
		}

		pluginBuilder.addAgentActionPlan("agent", new AgentActionPlan(10, (c) -> {
			MaterialsDataView materialsDataView = c.getDataView(MaterialsDataView.class).get();
			for (TestMaterialsProducerId testMaterialsProducerId : TestMaterialsProducerId.values()) {
				for (TestResourceId testResourceId : TestResourceId.values()) {
					MultiKey multiKey = new MultiKey(testMaterialsProducerId, testResourceId);
					long expectedLevel = expectedLevelsMap.get(multiKey).getValue();
					long actualLevel = materialsDataView.getMaterialsProducerResourceLevel(testMaterialsProducerId, testResourceId);
					assertEquals(expectedLevel, actualLevel);
				}
			}
		}));

		// precondition tests
		pluginBuilder.addAgentActionPlan("agent", new AgentActionPlan(11, (c) -> {
			MaterialsDataView materialsDataView = c.getDataView(MaterialsDataView.class).get();

			// if the materials producerId id is null
			ContractException contractException = assertThrows(ContractException.class, () -> materialsDataView.getMaterialsProducerResourceLevel(null, TestResourceId.RESOURCE_1));
			assertEquals(MaterialsError.NULL_MATERIALS_PRODUCER_ID, contractException.getErrorType());

			// if the materials producerId id is unknown
			contractException = assertThrows(ContractException.class,
					() -> materialsDataView.getMaterialsProducerResourceLevel(TestMaterialsProducerId.getUnknownMaterialsProducerId(), TestResourceId.RESOURCE_1));
			assertEquals(MaterialsError.UNKNOWN_MATERIALS_PRODUCER_ID, contractException.getErrorType());

			// if the resource id is null
			contractException = assertThrows(ContractException.class, () -> materialsDataView.getMaterialsProducerResourceLevel(TestMaterialsProducerId.MATERIALS_PRODUCER_1, null));
			assertEquals(ResourceError.NULL_RESOURCE_ID, contractException.getErrorType());

			// if the resource id is unknown
			contractException = assertThrows(ContractException.class,
					() -> materialsDataView.getMaterialsProducerResourceLevel(TestMaterialsProducerId.MATERIALS_PRODUCER_1, TestResourceId.getUnknownResourceId()));
			assertEquals(ResourceError.UNKNOWN_RESOURCE_ID, contractException.getErrorType());

		}));

		ActionPluginInitializer actionPluginInitializer = pluginBuilder.build();
		MaterialsActionSupport.testConsumers(1633676078121550637L, actionPluginInitializer);
	}

	@Test
	@UnitTestMethod(name = "getMaterialsProducerResourceTime", args = { MaterialsProducerId.class, ResourceId.class })
	public void testGetMaterialsProducerResourceTime() {

		ActionPluginInitializer.Builder pluginBuilder = ActionPluginInitializer.builder();
		pluginBuilder.addAgent("agent");

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
			pluginBuilder.addAgentActionPlan(testMaterialsProducerId, new AgentActionPlan(plantime++, (c) -> {
				StochasticsDataManager stochasticsDataManager = c.getDataView(StochasticsDataManager.class).get();
				RandomGenerator randomGenerator = stochasticsDataManager.getRandomGenerator();
				MaterialsDataView materialsDataView = c.getDataView(MaterialsDataView.class).get();
				for (int j = 0; j < 5; j++) {
					TestResourceId testResourceId = TestResourceId.getRandomResourceId(randomGenerator);
					Integer resourceLevel = randomGenerator.nextInt(100) + 1;
					c.resolveEvent(new StageCreationEvent());
					StageId stageId = materialsDataView.getLastIssuedStageId().get();
					c.resolveEvent(new StageToResourceConversionEvent(stageId, testResourceId, resourceLevel));
					MultiKey multiKey = new MultiKey(testMaterialsProducerId, testResourceId);
					expectedTimesMap.get(multiKey).setValue(c.getTime());
				}
			}));
		}

		pluginBuilder.addAgentActionPlan("agent", new AgentActionPlan(10, (c) -> {
			MaterialsDataView materialsDataView = c.getDataView(MaterialsDataView.class).get();
			for (TestMaterialsProducerId testMaterialsProducerId : TestMaterialsProducerId.values()) {
				for (TestResourceId testResourceId : TestResourceId.values()) {
					MultiKey multiKey = new MultiKey(testMaterialsProducerId, testResourceId);
					double expectedTime = expectedTimesMap.get(multiKey).getValue();
					double actualTime = materialsDataView.getMaterialsProducerResourceTime(testMaterialsProducerId, testResourceId);
					assertEquals(expectedTime, actualTime);
				}
			}
		}));

		// precondition tests
		pluginBuilder.addAgentActionPlan("agent", new AgentActionPlan(11, (c) -> {
			MaterialsDataView materialsDataView = c.getDataView(MaterialsDataView.class).get();

			// if the materials producerId id is null
			ContractException contractException = assertThrows(ContractException.class, () -> materialsDataView.getMaterialsProducerResourceTime(null, TestResourceId.RESOURCE_1));
			assertEquals(MaterialsError.NULL_MATERIALS_PRODUCER_ID, contractException.getErrorType());

			// if the materials producerId id is unknown
			contractException = assertThrows(ContractException.class,
					() -> materialsDataView.getMaterialsProducerResourceTime(TestMaterialsProducerId.getUnknownMaterialsProducerId(), TestResourceId.RESOURCE_1));
			assertEquals(MaterialsError.UNKNOWN_MATERIALS_PRODUCER_ID, contractException.getErrorType());

			// if the resource id is null
			contractException = assertThrows(ContractException.class, () -> materialsDataView.getMaterialsProducerResourceTime(TestMaterialsProducerId.MATERIALS_PRODUCER_1, null));
			assertEquals(ResourceError.NULL_RESOURCE_ID, contractException.getErrorType());

			// if the resource id is unknown
			contractException = assertThrows(ContractException.class,
					() -> materialsDataView.getMaterialsProducerResourceTime(TestMaterialsProducerId.MATERIALS_PRODUCER_1, TestResourceId.getUnknownResourceId()));
			assertEquals(ResourceError.UNKNOWN_RESOURCE_ID, contractException.getErrorType());

		}));

		ActionPluginInitializer actionPluginInitializer = pluginBuilder.build();
		MaterialsActionSupport.testConsumers(102509608008549692L, actionPluginInitializer);
	}

	@Test
	@UnitTestMethod(name = "getMaterialsProducerPropertyDefinition", args = { MaterialsProducerPropertyId.class })
	public void testGetMaterialsProducerPropertyDefinition() {

		MaterialsActionSupport.testConsumer(7151961147034751776L, (c) -> {
			MaterialsDataView materialsDaView = c.getDataView(MaterialsDataView.class).get();

			for (TestMaterialsProducerPropertyId testMaterialsProducerPropertyId : TestMaterialsProducerPropertyId.values()) {
				PropertyDefinition expectedPropertyDefinition = testMaterialsProducerPropertyId.getPropertyDefinition();
				PropertyDefinition actualPropertyDefinition = materialsDaView.getMaterialsProducerPropertyDefinition(testMaterialsProducerPropertyId);
				assertEquals(expectedPropertyDefinition, actualPropertyDefinition);
			}

			// if the materials producer property id is null
			ContractException contractException = assertThrows(ContractException.class, () -> materialsDaView.getMaterialsProducerPropertyDefinition(null));
			assertEquals(MaterialsError.NULL_MATERIALS_PRODUCER_PROPERTY_ID, contractException.getErrorType());

			// if the materials producer property id is unknown
			contractException = assertThrows(ContractException.class,
					() -> materialsDaView.getMaterialsProducerPropertyDefinition(TestMaterialsProducerPropertyId.getUnknownMaterialsProducerPropertyId()));
			assertEquals(MaterialsError.UNKNOWN_MATERIALS_PRODUCER_PROPERTY_ID, contractException.getErrorType());
		});
	}

	@Test
	@UnitTestMethod(name = "getMaterialsProducerPropertyIds", args = {})
	public void testGetMaterialsProducerPropertyIds() {
		MaterialsActionSupport.testConsumer(8718225529106870071L, (c) -> {
			MaterialsDataView materialsDaView = c.getDataView(MaterialsDataView.class).get();
			assertEquals(EnumSet.allOf(TestMaterialsProducerPropertyId.class), materialsDaView.getMaterialsProducerPropertyIds());
		});
	}

	@Test
	@UnitTestMethod(name = "getBatchPropertyDefinition", args = { MaterialId.class, BatchPropertyId.class })
	public void testGetBatchPropertyDefinition() {

		MaterialsActionSupport.testConsumer(4785939121817102392L, (c) -> {
			MaterialsDataView materialsDaView = c.getDataView(MaterialsDataView.class).get();

			for (TestMaterialId testMaterialId : TestMaterialId.values()) {

				Set<TestBatchPropertyId> testBatchPropertyIds = TestBatchPropertyId.getTestBatchPropertyIds(testMaterialId);
				for (TestBatchPropertyId testBatchPropertyId : testBatchPropertyIds) {
					PropertyDefinition expectedPropertyDefinition = testBatchPropertyId.getPropertyDefinition();
					PropertyDefinition actualPropertyDefinition = materialsDaView.getBatchPropertyDefinition(testMaterialId, testBatchPropertyId);
					assertEquals(expectedPropertyDefinition, actualPropertyDefinition);
				}
			}

			// precondition tests
			TestBatchPropertyId testBatchPropertyId = TestBatchPropertyId.BATCH_PROPERTY_2_3_DOUBLE_MUTABLE_TRACK;

			// if the material id is null
			ContractException contractException = assertThrows(ContractException.class, () -> materialsDaView.getBatchPropertyDefinition(null, testBatchPropertyId));
			assertEquals(MaterialsError.NULL_MATERIAL_ID, contractException.getErrorType());

			// if the material id is unknown
			contractException = assertThrows(ContractException.class, () -> materialsDaView.getBatchPropertyDefinition(TestMaterialId.getUnknownMaterialId(), testBatchPropertyId));
			assertEquals(MaterialsError.UNKNOWN_MATERIAL_ID, contractException.getErrorType());

			// if the batch property id is null
			contractException = assertThrows(ContractException.class, () -> materialsDaView.getBatchPropertyDefinition(TestMaterialId.MATERIAL_1, null));
			assertEquals(MaterialsError.NULL_BATCH_PROPERTY_ID, contractException.getErrorType());

			// if the batch property id is unknown
			contractException = assertThrows(ContractException.class, () -> materialsDaView.getBatchPropertyDefinition(TestMaterialId.MATERIAL_1, TestBatchPropertyId.getUnknownBatchPropertyId()));
			assertEquals(MaterialsError.UNKNOWN_BATCH_PROPERTY_ID, contractException.getErrorType());

		});
	}

	@Test
	@UnitTestMethod(name = "getBatchPropertyIds", args = { MaterialId.class })
	public void testGetBatchPropertyIds() {

		MaterialsActionSupport.testConsumer(8657082858154514151L, (c) -> {
			MaterialsDataView materialsDaView = c.getDataView(MaterialsDataView.class).get();

			for (TestMaterialId testMaterialId : TestMaterialId.values()) {
				Set<TestBatchPropertyId> expectedBatchPropertyIds = TestBatchPropertyId.getTestBatchPropertyIds(testMaterialId);
				Set<BatchPropertyId> actualBatchPropertyIds = materialsDaView.getBatchPropertyIds(testMaterialId);
				assertEquals(expectedBatchPropertyIds, actualBatchPropertyIds);
			}

			// precondition tests

			// if the material id is null
			ContractException contractException = assertThrows(ContractException.class, () -> materialsDaView.getBatchPropertyIds(null));
			assertEquals(MaterialsError.NULL_MATERIAL_ID, contractException.getErrorType());

			// if the material id is unknown
			contractException = assertThrows(ContractException.class, () -> materialsDaView.getBatchPropertyIds(TestMaterialId.getUnknownMaterialId()));
			assertEquals(MaterialsError.UNKNOWN_MATERIAL_ID, contractException.getErrorType());

		});

	}

	@Test
	@UnitTestMethod(name = "getMaterialsProducerPropertyValue", args = { MaterialsProducerId.class, MaterialsProducerPropertyId.class })
	public void testGetMaterialsProducerPropertyValue() {

		ActionPluginInitializer.Builder pluginBuilder = ActionPluginInitializer.builder();
		pluginBuilder.addAgent("agent");

		// create a structure to hold expected assignment times for materials
		// producer property values
		Map<MultiKey, Object> expectedValuesMap = new LinkedHashMap<>();

		// initialize the materials data manager
		pluginBuilder.addAgentActionPlan("agent", new AgentActionPlan(0, (c) -> {
			MaterialsDataView materialsDaView = c.getDataView(MaterialsDataView.class).get();

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
			pluginBuilder.addAgentActionPlan("agent", new AgentActionPlan(0, (c) -> {
				StochasticsDataManager stochasticsDataManager = c.getDataView(StochasticsDataManager.class).get();
				RandomGenerator randomGenerator = stochasticsDataManager.getRandomGenerator();
				for (int j = 0; j < propertyChangeCount; j++) {
					TestMaterialsProducerId materialsProducerId = TestMaterialsProducerId.getRandomMaterialsProducerId(randomGenerator);
					TestMaterialsProducerPropertyId materialsProducerPropertyId = TestMaterialsProducerPropertyId.getRandomMutableMaterialsProducerPropertyId(randomGenerator);
					Object propertyValue = materialsProducerPropertyId.getRandomPropertyValue(randomGenerator);
					c.resolveEvent(new MaterialsProducerPropertyValueAssignmentEvent(materialsProducerId, materialsProducerPropertyId, propertyValue));
					MultiKey multiKey = new MultiKey(materialsProducerId, materialsProducerPropertyId);
					expectedValuesMap.put(multiKey, propertyValue);
				}
			}));
		}

		pluginBuilder.addAgentActionPlan("agent", new AgentActionPlan(10, (c) -> {
			MaterialsDataView materialsDaView = c.getDataView(MaterialsDataView.class).get();
			for (TestMaterialsProducerId testMaterialsProducerId : TestMaterialsProducerId.values()) {
				for (TestMaterialsProducerPropertyId testMaterialsProducerPropertyId : TestMaterialsProducerPropertyId.values()) {
					MultiKey multiKey = new MultiKey(testMaterialsProducerId, testMaterialsProducerPropertyId);
					Object expectedValue = expectedValuesMap.get(multiKey);
					Object actualValue = materialsDaView.getMaterialsProducerPropertyValue(testMaterialsProducerId, testMaterialsProducerPropertyId);
					assertEquals(expectedValue, actualValue);
				}
			}
		}));

		// precondition tests
		pluginBuilder.addAgentActionPlan("agent", new AgentActionPlan(11, (c) -> {
			MaterialsDataView materialsDaView = c.getDataView(MaterialsDataView.class).get();

			// if the materials producerId id is null
			ContractException contractException = assertThrows(ContractException.class,
					() -> materialsDaView.getMaterialsProducerPropertyValue(null, TestMaterialsProducerPropertyId.MATERIALS_PRODUCER_PROPERTY_4_BOOLEAN_MUTABLE_TRACK));
			assertEquals(MaterialsError.NULL_MATERIALS_PRODUCER_ID, contractException.getErrorType());

			// if the materials producerId id is unknown
			contractException = assertThrows(ContractException.class, () -> materialsDaView.getMaterialsProducerPropertyValue(TestMaterialsProducerId.getUnknownMaterialsProducerId(),
					TestMaterialsProducerPropertyId.MATERIALS_PRODUCER_PROPERTY_4_BOOLEAN_MUTABLE_TRACK));
			assertEquals(MaterialsError.UNKNOWN_MATERIALS_PRODUCER_ID, contractException.getErrorType());

			// if the materials producerId property id is null
			contractException = assertThrows(ContractException.class, () -> materialsDaView.getMaterialsProducerPropertyValue(TestMaterialsProducerId.MATERIALS_PRODUCER_1, null));
			assertEquals(MaterialsError.NULL_MATERIALS_PRODUCER_PROPERTY_ID, contractException.getErrorType());

			// if the materials producerId property id is unknown
			contractException = assertThrows(ContractException.class,
					() -> materialsDaView.getMaterialsProducerPropertyValue(TestMaterialsProducerId.MATERIALS_PRODUCER_1, TestMaterialsProducerPropertyId.getUnknownMaterialsProducerPropertyId()));
			assertEquals(MaterialsError.UNKNOWN_MATERIALS_PRODUCER_PROPERTY_ID, contractException.getErrorType());

		}));

		ActionPluginInitializer actionPluginInitializer = pluginBuilder.build();
		MaterialsActionSupport.testConsumers(3587272435527239583L, actionPluginInitializer);
	}

	@Test
	@UnitTestMethod(name = "getMaterialsProducerPropertyTime", args = { MaterialsProducerId.class, MaterialsProducerPropertyId.class })
	public void testGetMaterialsProducerPropertyTime() {

		ActionPluginInitializer.Builder pluginBuilder = ActionPluginInitializer.builder();
		pluginBuilder.addAgent("agent");

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
			pluginBuilder.addAgentActionPlan("agent", new AgentActionPlan(0, (c) -> {
				StochasticsDataManager stochasticsDataManager = c.getDataView(StochasticsDataManager.class).get();
				RandomGenerator randomGenerator = stochasticsDataManager.getRandomGenerator();

				for (int j = 0; j < propertyChangeCount; j++) {
					TestMaterialsProducerId materialsProducerId = TestMaterialsProducerId.getRandomMaterialsProducerId(randomGenerator);
					TestMaterialsProducerPropertyId materialsProducerPropertyId = TestMaterialsProducerPropertyId.getRandomMutableMaterialsProducerPropertyId(randomGenerator);
					Object propertyValue = materialsProducerPropertyId.getRandomPropertyValue(randomGenerator);
					c.resolveEvent(new MaterialsProducerPropertyValueAssignmentEvent(materialsProducerId, materialsProducerPropertyId, propertyValue));
					MultiKey multiKey = new MultiKey(materialsProducerId, materialsProducerPropertyId);
					expectedTimesMap.get(multiKey).setValue(c.getTime());
				}
			}));
		}

		pluginBuilder.addAgentActionPlan("agent", new AgentActionPlan(10, (c) -> {
			MaterialsDataView materialsDaView = c.getDataView(MaterialsDataView.class).get();
			for (TestMaterialsProducerId testMaterialsProducerId : TestMaterialsProducerId.values()) {
				for (TestMaterialsProducerPropertyId testMaterialsProducerPropertyId : TestMaterialsProducerPropertyId.values()) {
					MultiKey multiKey = new MultiKey(testMaterialsProducerId, testMaterialsProducerPropertyId);
					double expectedTime = expectedTimesMap.get(multiKey).getValue();
					double actualTime = materialsDaView.getMaterialsProducerPropertyTime(testMaterialsProducerId, testMaterialsProducerPropertyId);
					assertEquals(expectedTime, actualTime);
				}
			}
		}));

		// precondition tests
		pluginBuilder.addAgentActionPlan("agent", new AgentActionPlan(11, (c) -> {
			MaterialsDataView materialsDaView = c.getDataView(MaterialsDataView.class).get();

			// if the materials producerId id is null
			ContractException contractException = assertThrows(ContractException.class,
					() -> materialsDaView.getMaterialsProducerPropertyTime(null, TestMaterialsProducerPropertyId.MATERIALS_PRODUCER_PROPERTY_4_BOOLEAN_MUTABLE_TRACK));
			assertEquals(MaterialsError.NULL_MATERIALS_PRODUCER_ID, contractException.getErrorType());

			// if the materials producerId id is unknown
			contractException = assertThrows(ContractException.class, () -> materialsDaView.getMaterialsProducerPropertyTime(TestMaterialsProducerId.getUnknownMaterialsProducerId(),
					TestMaterialsProducerPropertyId.MATERIALS_PRODUCER_PROPERTY_4_BOOLEAN_MUTABLE_TRACK));
			assertEquals(MaterialsError.UNKNOWN_MATERIALS_PRODUCER_ID, contractException.getErrorType());

			// if the materials producerId property id is null
			contractException = assertThrows(ContractException.class, () -> materialsDaView.getMaterialsProducerPropertyTime(TestMaterialsProducerId.MATERIALS_PRODUCER_1, null));
			assertEquals(MaterialsError.NULL_MATERIALS_PRODUCER_PROPERTY_ID, contractException.getErrorType());

			// if the materials producerId property id is unknown
			contractException = assertThrows(ContractException.class,
					() -> materialsDaView.getMaterialsProducerPropertyTime(TestMaterialsProducerId.MATERIALS_PRODUCER_1, TestMaterialsProducerPropertyId.getUnknownMaterialsProducerPropertyId()));
			assertEquals(MaterialsError.UNKNOWN_MATERIALS_PRODUCER_PROPERTY_ID, contractException.getErrorType());

		}));

		ActionPluginInitializer actionPluginInitializer = pluginBuilder.build();
		MaterialsActionSupport.testConsumers(4362229716953652532L, actionPluginInitializer);
	}

	@Test
	@UnitTestMethod(name = "getBatchPropertyTime", args = { BatchId.class, BatchPropertyId.class })
	public void testGetBatchPropertyTime() {

		ActionPluginInitializer.Builder pluginBuilder = ActionPluginInitializer.builder();

		// create a data structure to hold the assignments we expect to
		// retrieve.
		Map<MultiKey, MutableDouble> expectedAssignmentTimes = new LinkedHashMap<>();

		// create an agent
		pluginBuilder.addAgent("agent");

		/*
		 * Have the agent add 50 randomized batches and record the assignment
		 * times for all properties
		 */
		pluginBuilder.addAgentActionPlan(TestMaterialsProducerId.MATERIALS_PRODUCER_3, new AgentActionPlan(0, (c) -> {
			MaterialsDataView materialsDaView = c.getDataView(MaterialsDataView.class).get();
			StochasticsDataManager stochasticsDataManager = c.getDataView(StochasticsDataManager.class).get();
			RandomGenerator randomGenerator = stochasticsDataManager.getRandomGenerator();

			// create a few batches
			for (int i = 0; i < 50; i++) {
				MaterialId materialId = TestMaterialId.getRandomMaterialId(randomGenerator);
				double amount = randomGenerator.nextDouble();
				c.resolveEvent(new BatchCreationEvent(materialId, amount));
				BatchId batchId = materialsDaView.getLastIssuedBatchId().get();

				Set<TestBatchPropertyId> batchPropertyIds = materialsDaView.getBatchPropertyIds(materialId);
				for (TestBatchPropertyId batchPropertyId : batchPropertyIds) {
					expectedAssignmentTimes.put(new MultiKey(batchId, batchPropertyId), new MutableDouble(c.getTime()));
				}
			}

		}));

		/*
		 * Have the agent alter about 1/3 of batch property values at 10
		 * distinct times, recording the new assignment times as we go.
		 */
		for (int i = 1; i < 10; i++) {
			double actionTime = i;
			pluginBuilder.addAgentActionPlan(TestMaterialsProducerId.MATERIALS_PRODUCER_3, new AgentActionPlan(actionTime, (c) -> {
				MaterialsDataView materialsDaView = c.getDataView(MaterialsDataView.class).get();
				StochasticsDataManager stochasticsDataManager = c.getDataView(StochasticsDataManager.class).get();
				RandomGenerator randomGenerator = stochasticsDataManager.getRandomGenerator();

				// plan several times to alter some of the batch properties

				// alter the batch properties

				List<BatchId> inventoryBatches = materialsDaView.getInventoryBatches(TestMaterialsProducerId.MATERIALS_PRODUCER_3);
				for (BatchId batchId : inventoryBatches) {
					MaterialId materialId = materialsDaView.getBatchMaterial(batchId);
					Set<TestBatchPropertyId> batchPropertyIds = materialsDaView.getBatchPropertyIds(materialId);
					for (TestBatchPropertyId batchPropertyId : batchPropertyIds) {
						if (batchPropertyId.getPropertyDefinition().propertyValuesAreMutable()) {
							if (randomGenerator.nextDouble() < 0.5) {
								Object value = batchPropertyId.getRandomPropertyValue(randomGenerator);
								c.resolveEvent(new BatchPropertyValueAssignmentEvent(batchId, batchPropertyId, value));
								MutableDouble mutableDouble = expectedAssignmentTimes.get(new MultiKey(batchId, batchPropertyId));
								mutableDouble.setValue(c.getTime());
							}
						}
					}
				}

			}));
		}

		/*
		 * Have the agent compare the assignment times at time = 10 to the
		 * expected values.
		 */
		pluginBuilder.addAgentActionPlan("agent", new AgentActionPlan(10, (c) -> {
			MaterialsDataView materialsDaView = c.getDataView(MaterialsDataView.class).get();
			StochasticsDataManager stochasticsDataManager = c.getDataView(StochasticsDataManager.class).get();
			RandomGenerator randomGenerator = stochasticsDataManager.getRandomGenerator();

			for (MaterialsProducerId materialsProducerId : materialsDaView.getMaterialsProducerIds()) {
				List<BatchId> inventoryBatches = materialsDaView.getInventoryBatches(materialsProducerId);
				for (BatchId batchId : inventoryBatches) {
					MaterialId materialId = materialsDaView.getBatchMaterial(batchId);
					Set<TestBatchPropertyId> batchPropertyIds = materialsDaView.getBatchPropertyIds(materialId);
					for (TestBatchPropertyId batchPropertyId : batchPropertyIds) {
						if (randomGenerator.nextDouble() < 0.33) {
							MutableDouble mutableDouble = expectedAssignmentTimes.get(new MultiKey(batchId, batchPropertyId));
							double expectedAssignmentTime = mutableDouble.getValue();
							double actualAssignmentTime = materialsDaView.getBatchPropertyTime(batchId, batchPropertyId);
							assertEquals(expectedAssignmentTime, actualAssignmentTime);
						}
					}
				}
			}

		}));

		/*
		 * precondition tests
		 */
		pluginBuilder.addAgentActionPlan("agent", new AgentActionPlan(11, (c) -> {
			MaterialsDataView materialsDaView = c.getDataView(MaterialsDataView.class).get();

			BatchId batchId = new BatchId(0);
			BatchPropertyId batchPropertyId = TestBatchPropertyId.BATCH_PROPERTY_1_1_BOOLEAN_IMMUTABLE_NO_TRACK;

			// if the batch id is null
			ContractException contractException = assertThrows(ContractException.class, () -> materialsDaView.getBatchPropertyTime(null, batchPropertyId));
			assertEquals(MaterialsError.NULL_BATCH_ID, contractException.getErrorType());

			// if the batch id is unknown
			contractException = assertThrows(ContractException.class, () -> materialsDaView.getBatchPropertyTime(new BatchId(100000), batchPropertyId));
			assertEquals(MaterialsError.UNKNOWN_BATCH_ID, contractException.getErrorType());

			// if the batch property id is null
			contractException = assertThrows(ContractException.class, () -> materialsDaView.getBatchPropertyTime(batchId, null));
			assertEquals(MaterialsError.NULL_BATCH_PROPERTY_ID, contractException.getErrorType());

			// if the batch property id is unknown
			contractException = assertThrows(ContractException.class, () -> materialsDaView.getBatchPropertyTime(batchId, TestBatchPropertyId.getUnknownBatchPropertyId()));
			assertEquals(MaterialsError.UNKNOWN_BATCH_PROPERTY_ID, contractException.getErrorType());

		}));

		ActionPluginInitializer actionPluginInitializer = pluginBuilder.build();
		MaterialsActionSupport.testConsumers(1470041164645430466L, actionPluginInitializer);
	}

	@Test
	@UnitTestMethod(name = "getBatchPropertyValue", args = { BatchId.class, BatchPropertyId.class })
	public void testGetBatchPropertyValue() {

		ActionPluginInitializer.Builder pluginBuilder = ActionPluginInitializer.builder();
		pluginBuilder.addAgent("agent");
		/*
		 * create a data structure to hold the assignments we expect to
		 * retrieve.
		 */

		Map<MultiKey, Object> expectedValues = new LinkedHashMap<>();

		pluginBuilder.addAgentActionPlan(TestMaterialsProducerId.MATERIALS_PRODUCER_2, new AgentActionPlan(0, (c) -> {

			/*
			 * Have the agent add 50 randomized batches and record the values
			 * for all properties
			 */

			MaterialsDataView materialsDataView = c.getDataView(MaterialsDataView.class).get();
			StochasticsDataManager stochasticsDataManager = c.getDataView(StochasticsDataManager.class).get();
			RandomGenerator randomGenerator = stochasticsDataManager.getRandomGenerator();

			// create a few batches
			for (int i = 0; i < 50; i++) {

				MaterialId materialId = TestMaterialId.getRandomMaterialId(randomGenerator);
				double amount = randomGenerator.nextDouble();
				c.resolveEvent(new BatchCreationEvent(materialId, amount));
				BatchId batchId = materialsDataView.getLastIssuedBatchId().get();

				Set<TestBatchPropertyId> batchPropertyIds = materialsDataView.getBatchPropertyIds(materialId);
				for (TestBatchPropertyId batchPropertyId : batchPropertyIds) {
					Object value = materialsDataView.getBatchPropertyValue(batchId, batchPropertyId);
					expectedValues.put(new MultiKey(batchId, batchPropertyId), value);
				}
			}

			// alter randomly chosen batch properties

			for (int i = 0; i < 200; i++) {

				List<BatchId> inventoryBatches = materialsDataView.getInventoryBatches(TestMaterialsProducerId.MATERIALS_PRODUCER_2);
				BatchId batchId = inventoryBatches.get(randomGenerator.nextInt(inventoryBatches.size()));
				TestMaterialId materialId = materialsDataView.getBatchMaterial(batchId);
				TestBatchPropertyId batchPropertyId = TestBatchPropertyId.getRandomMutableBatchPropertyId(materialId,randomGenerator);
				Object value = batchPropertyId.getRandomPropertyValue(randomGenerator);
				c.resolveEvent(new BatchPropertyValueAssignmentEvent(batchId, batchPropertyId, value));
				expectedValues.put(new MultiKey(batchId, batchPropertyId), value);

			}

		}));

		// have an agent show that the batches have the expected property values
		pluginBuilder.addAgentActionPlan("agent", new AgentActionPlan(1, (c) -> {
			MaterialsDataView materialsDataView = c.getDataView(MaterialsDataView.class).get();
			for (MaterialsProducerId materialsProducerId : materialsDataView.getMaterialsProducerIds()) {
				List<BatchId> inventoryBatches = materialsDataView.getInventoryBatches(materialsProducerId);
				for (BatchId batchId : inventoryBatches) {
					MaterialId materialId = materialsDataView.getBatchMaterial(batchId);
					Set<TestBatchPropertyId> batchPropertyIds = materialsDataView.getBatchPropertyIds(materialId);
					for (TestBatchPropertyId batchPropertyId : batchPropertyIds) {
						Object expectedValue = expectedValues.get(new MultiKey(batchId, batchPropertyId));
						Object actualValue = materialsDataView.getBatchPropertyValue(batchId, batchPropertyId);
						assertEquals(expectedValue, actualValue);
					}
				}
			}
		}));

		// precondition tests
		pluginBuilder.addAgentActionPlan("agent", new AgentActionPlan(2, (c) -> {
			MaterialsDataView materialsDataView = c.getDataView(MaterialsDataView.class).get();

			BatchId batchId = new BatchId(0);
			BatchPropertyId batchPropertyId = TestBatchPropertyId.BATCH_PROPERTY_1_1_BOOLEAN_IMMUTABLE_NO_TRACK;

			// if the batch id is null
			ContractException contractException = assertThrows(ContractException.class, () -> materialsDataView.getBatchPropertyValue(null, batchPropertyId));
			assertEquals(MaterialsError.NULL_BATCH_ID, contractException.getErrorType());

			// if the batch id is unknown
			contractException = assertThrows(ContractException.class, () -> materialsDataView.getBatchPropertyValue(new BatchId(100000), batchPropertyId));
			assertEquals(MaterialsError.UNKNOWN_BATCH_ID, contractException.getErrorType());

			// if the batch property id is null
			contractException = assertThrows(ContractException.class, () -> materialsDataView.getBatchPropertyValue(batchId, null));
			assertEquals(MaterialsError.NULL_BATCH_PROPERTY_ID, contractException.getErrorType());

			// if the batch property id is unknown
			contractException = assertThrows(ContractException.class, () -> materialsDataView.getBatchPropertyValue(batchId, TestBatchPropertyId.getUnknownBatchPropertyId()));
			assertEquals(MaterialsError.UNKNOWN_BATCH_PROPERTY_ID, contractException.getErrorType());

		}));

		ActionPluginInitializer actionPluginInitializer = pluginBuilder.build();
		MaterialsActionSupport.testConsumers(1629075115765446254L, actionPluginInitializer);

	}

	@Test
	@UnitTestMethod(name = "materialsProducerIdExists", args = { MaterialsProducerId.class })
	public void testMaterialsProducerIdExists() {
		
		MaterialsActionSupport.testConsumer(4005535514531641716L, (c) -> {
			MaterialsDataView materialsDataView = c.getDataView(MaterialsDataView.class).get();

			for (TestMaterialsProducerId testMaterialsProducerId : TestMaterialsProducerId.values()) {
				assertTrue(materialsDataView.materialsProducerIdExists(testMaterialsProducerId));
			}
			assertFalse(materialsDataView.materialsProducerIdExists(TestMaterialsProducerId.getUnknownMaterialsProducerId()));
			assertFalse(materialsDataView.materialsProducerIdExists(null));
		});
		
	}

	@Test
	@UnitTestMethod(name = "materialsProducerPropertyIdExists", args = { MaterialsProducerPropertyId.class })
	public void testMaterialsProducerPropertyIdExists() {
		 
		MaterialsActionSupport.testConsumer(8256309156804000329L, (c) -> {
			MaterialsDataView materialsDataView = c.getDataView(MaterialsDataView.class).get();

			for (TestMaterialsProducerPropertyId testMaterialsProducerPropertyId : TestMaterialsProducerPropertyId.values()) {
				assertTrue(materialsDataView.materialsProducerPropertyIdExists(testMaterialsProducerPropertyId));
			}
			assertFalse(materialsDataView.materialsProducerPropertyIdExists(TestMaterialsProducerPropertyId.getUnknownMaterialsProducerPropertyId()));
			assertFalse(materialsDataView.materialsProducerPropertyIdExists(null));
		});
	}

	@Test
	@UnitTestMethod(name = "getMaterialsProducerIds", args = {})
	public void testGetMaterialsProducerIds() {
		
		MaterialsActionSupport.testConsumer(3824970086302200338L, (c) -> {
			MaterialsDataView materialsDataView = c.getDataView(MaterialsDataView.class).get();
			assertEquals(EnumSet.allOf(TestMaterialsProducerId.class), materialsDataView.getMaterialsProducerIds());
		});
	}

}
