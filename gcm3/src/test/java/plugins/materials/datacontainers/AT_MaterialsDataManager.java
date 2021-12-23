package plugins.materials.datacontainers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
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

import org.apache.commons.math3.random.RandomGenerator;
import org.junit.jupiter.api.Test;

import nucleus.Context;
import nucleus.DataView;
import nucleus.NucleusError;
import nucleus.ResolverContext;
import nucleus.testsupport.actionplugin.ActionPlugin;
import nucleus.testsupport.actionplugin.AgentActionPlan;
import plugins.materials.initialdata.MaterialsInitialData;
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
import plugins.properties.support.PropertyError;
import plugins.resources.datacontainers.ResourceDataView;
import plugins.resources.support.ResourceError;
import plugins.resources.support.ResourceId;
import plugins.resources.testsupport.TestResourceId;
import plugins.stochastics.datacontainers.StochasticsDataView;
import util.ContractException;
import util.MultiKey;
import util.MutableDouble;
import util.MutableLong;
import util.annotations.UnitTest;
import util.annotations.UnitTestConstructor;
import util.annotations.UnitTestMethod;

@UnitTest(target = MaterialsDataManager.class)

public final class AT_MaterialsDataManager {

	/*
	 * Creates a copy of the current MaterialsDataManager via data extracted
	 * from the MaterialsDataView. This will not copy the materials producer
	 * property values or any batches or stages.
	 */
	private MaterialsDataManager getMaterialsDataManager(Context context) {
		MaterialsDataManager result = new MaterialsDataManager(context);

		MaterialsDataView materialsDataView = context.getDataView(MaterialsDataView.class).get();

		ResourceDataView resourceDataView = context.getDataView(ResourceDataView.class).get();

		for (ResourceId resourceId : resourceDataView.getResourceIds()) {
			result.addResource(resourceId);
		}

		for (MaterialId materialId : materialsDataView.getMaterialIds()) {
			result.addMaterialId(materialId);
		}

		for (MaterialId materialId : materialsDataView.getMaterialIds()) {
			Set<BatchPropertyId> batchPropertyIds = materialsDataView.getBatchPropertyIds(materialId);
			for (BatchPropertyId batchPropertyId : batchPropertyIds) {
				PropertyDefinition propertyDefinition = materialsDataView.getBatchPropertyDefinition(materialId, batchPropertyId);
				result.defineBatchProperty(materialId, batchPropertyId, propertyDefinition);
			}
		}

		for (MaterialsProducerId materialsProducerId : materialsDataView.getMaterialsProducerIds()) {
			result.addMaterialsProducerId(materialsProducerId);
		}

		Set<MaterialsProducerPropertyId> materialsProducerPropertyIds = materialsDataView.getMaterialsProducerPropertyIds();
		for (MaterialsProducerPropertyId materialsProducerPropertyId : materialsProducerPropertyIds) {
			PropertyDefinition propertyDefinition = materialsDataView.getMaterialsProducerPropertyDefinition(materialsProducerPropertyId);
			result.defineMaterialsProducerProperty(materialsProducerPropertyId, propertyDefinition);
		}

		return result;
	}

	@Test
	@UnitTestMethod(name = "batchExists", args = { BatchId.class })
	public void testBatchExists() {

		MaterialsActionSupport.testConsumer(4304396078647347650L, (c) -> {
			MaterialsDataManager materialsDataManager = getMaterialsDataManager(c);
			StochasticsDataView stochasticsDataView = c.getDataView(StochasticsDataView.class).get();
			RandomGenerator randomGenerator = stochasticsDataView.getRandomGenerator();

			for (TestMaterialsProducerId testMaterialsProducerId : TestMaterialsProducerId.values()) {
				for (TestMaterialId testMaterialId : TestMaterialId.values()) {
					double value = randomGenerator.nextDouble() * 100;
					BatchId batchId = materialsDataManager.createBatch(testMaterialsProducerId, testMaterialId, value);
					assertTrue(materialsDataManager.batchExists(batchId));
					materialsDataManager.destroyBatch(batchId);
					assertFalse(materialsDataManager.batchExists(batchId));
				}
			}

			// show that null and unknown batch ids return false
			assertFalse(materialsDataManager.batchExists(null));
			assertFalse(materialsDataManager.batchExists(new BatchId(10000000)));
		});

	}

	@Test
	@UnitTestMethod(name = "", args = { MaterialsProducerId.class, MaterialId.class, double.class })
	public void testCreateBatch() {
		MaterialsActionSupport.testConsumer(4304396078647347650L, (c) -> {
			MaterialsDataManager materialsDataManager = getMaterialsDataManager(c);
			StochasticsDataView stochasticsDataView = c.getDataView(StochasticsDataView.class).get();
			RandomGenerator randomGenerator = stochasticsDataView.getRandomGenerator();

			Set<BatchId> batchIds = new LinkedHashSet<>();
			for (int i = 0; i < 100; i++) {
				TestMaterialsProducerId testMaterialsProducerId = TestMaterialsProducerId.getRandomMaterialsProducerId(randomGenerator);
				TestMaterialId testMaterialId = TestMaterialId.getRandomMaterialId(randomGenerator);
				double value = randomGenerator.nextDouble() * 100;
				BatchId batchId = materialsDataManager.createBatch(testMaterialsProducerId, testMaterialId, value);

				// show that the returned batch id exists
				assertNotNull(batchId);
				assertTrue(materialsDataManager.batchExists(batchId));

				// show that the batch matches the inputs
				assertEquals(testMaterialsProducerId, materialsDataManager.getBatchProducer(batchId));
				assertEquals(testMaterialId, materialsDataManager.getBatchMaterial(batchId));
				assertEquals(value, materialsDataManager.getBatchAmount(batchId));

				// show that the batch id is unique
				assertTrue(batchIds.add(batchId));
			}

			// precondition tests
			assertThrows(RuntimeException.class, () -> materialsDataManager.createBatch(null, TestMaterialId.MATERIAL_1, 123.3));
			assertThrows(RuntimeException.class, () -> materialsDataManager.createBatch(TestMaterialsProducerId.getUnknownMaterialsProducerId(), TestMaterialId.MATERIAL_1, 123.3));
			assertThrows(RuntimeException.class, () -> materialsDataManager.createBatch(TestMaterialsProducerId.MATERIALS_PRODUCER_1, null, 44.3));
			assertThrows(RuntimeException.class, () -> materialsDataManager.createBatch(TestMaterialsProducerId.MATERIALS_PRODUCER_1, TestMaterialId.getUnknownMaterialId(), 44.3));

		});

	}

	@Test
	@UnitTestMethod(name = "createStage", args = { MaterialsProducerId.class })
	public void testCreateStage() {

		MaterialsActionSupport.testConsumer(2954527776362069867L, (c) -> {
			MaterialsDataManager materialsDataManager = getMaterialsDataManager(c);
			StochasticsDataView stochasticsDataView = c.getDataView(StochasticsDataView.class).get();
			RandomGenerator randomGenerator = stochasticsDataView.getRandomGenerator();

			Set<StageId> stageIds = new LinkedHashSet<>();
			for (int i = 0; i < 100; i++) {
				TestMaterialsProducerId testMaterialsProducerId = TestMaterialsProducerId.getRandomMaterialsProducerId(randomGenerator);
				StageId stageId = materialsDataManager.createStage(testMaterialsProducerId);

				// show that the returned stage id exists
				assertNotNull(stageId);
				assertTrue(materialsDataManager.stageExists(stageId));

				// show that the stage matches the inputs
				assertEquals(testMaterialsProducerId, materialsDataManager.getStageProducer(stageId));
				assertTrue(materialsDataManager.getStageBatches(stageId).isEmpty());
				assertFalse(materialsDataManager.isStageOffered(stageId));

				// show that the batch id is unique
				assertTrue(stageIds.add(stageId));
			}

			// precondition tests
			assertThrows(RuntimeException.class, () -> materialsDataManager.createStage(null));
			assertThrows(RuntimeException.class, () -> materialsDataManager.createStage(TestMaterialsProducerId.getUnknownMaterialsProducerId()));

		});
	}

	@Test
	@UnitTestMethod(name = "decrementMaterialsProducerResourceLevel", args = { MaterialsProducerId.class, ResourceId.class, long.class })
	public void testDecrementMaterialsProducerResourceLevel() {

		MaterialsActionSupport.testConsumer(872123029658730134L, (c) -> {
			MaterialsDataManager materialsDataManager = getMaterialsDataManager(c);
			StochasticsDataView stochasticsDataView = c.getDataView(StochasticsDataView.class).get();
			RandomGenerator randomGenerator = stochasticsDataView.getRandomGenerator();

			for (int i = 0; i < 1000; i++) {
				TestMaterialsProducerId testMaterialsProducerId = TestMaterialsProducerId.getRandomMaterialsProducerId(randomGenerator);
				TestResourceId resourceId = TestResourceId.getRandomResourceId(randomGenerator);
				long amount = randomGenerator.nextInt(10000);
				materialsDataManager.incrementMaterialsProducerResourceLevel(testMaterialsProducerId, resourceId, amount);
			}
			for (int i = 0; i < 1000; i++) {
				TestMaterialsProducerId testMaterialsProducerId = TestMaterialsProducerId.getRandomMaterialsProducerId(randomGenerator);
				TestResourceId resourceId = TestResourceId.getRandomResourceId(randomGenerator);
				long level = materialsDataManager.getMaterialsProducerResourceLevel(testMaterialsProducerId, resourceId);
				long amount = randomGenerator.nextInt((int) level);
				materialsDataManager.decrementMaterialsProducerResourceLevel(testMaterialsProducerId, resourceId, amount);
				long expectedLevel = level - amount;
				long actualLevel = materialsDataManager.getMaterialsProducerResourceLevel(testMaterialsProducerId, resourceId);
				assertEquals(expectedLevel, actualLevel);
			}

			// precondition tests
			MaterialsProducerId materialsProducerId = TestMaterialsProducerId.MATERIALS_PRODUCER_1;
			ResourceId resourceId = TestResourceId.RESOURCE_3;

			// if the materials producer id is null
			assertThrows(RuntimeException.class, () -> materialsDataManager.decrementMaterialsProducerResourceLevel(null, resourceId, 1L));

			// if the materials producer id is unknown
			assertThrows(RuntimeException.class, () -> materialsDataManager.decrementMaterialsProducerResourceLevel(TestMaterialsProducerId.getUnknownMaterialsProducerId(), resourceId, 1L));

			// if the resource id is null
			assertThrows(RuntimeException.class, () -> materialsDataManager.decrementMaterialsProducerResourceLevel(materialsProducerId, null, 1L));

			// if the resource id is unknown
			assertThrows(RuntimeException.class, () -> materialsDataManager.decrementMaterialsProducerResourceLevel(materialsProducerId, TestResourceId.getUnknownResourceId(), 1L));

		});
	}

	@Test
	@UnitTestMethod(name = "destroyBatch", args = { BatchId.class })
	public void testDestroyBatch() {

		MaterialsActionSupport.testConsumer(449693794508704901L, (c) -> {
			MaterialsDataManager materialsDataManager = getMaterialsDataManager(c);
			StochasticsDataView stochasticsDataView = c.getDataView(StochasticsDataView.class).get();
			RandomGenerator randomGenerator = stochasticsDataView.getRandomGenerator();

			List<BatchId> batchIds = new ArrayList<>();
			for (int i = 0; i < 100; i++) {
				TestMaterialsProducerId testMaterialsProducerId = TestMaterialsProducerId.getRandomMaterialsProducerId(randomGenerator);
				TestMaterialId testMaterialId = TestMaterialId.getRandomMaterialId(randomGenerator);
				double amount = randomGenerator.nextDouble() * 100;
				BatchId batchId = materialsDataManager.createBatch(testMaterialsProducerId, testMaterialId, amount);
				batchIds.add(batchId);
			}
			for (BatchId batchId : batchIds) {
				assertTrue(materialsDataManager.batchExists(batchId));
				materialsDataManager.destroyBatch(batchId);
				assertFalse(materialsDataManager.batchExists(batchId));
			}

			// precondition tests

			// if the batch id is null
			assertThrows(RuntimeException.class, () -> materialsDataManager.destroyBatch(null));

			// if the batch id is unknown
			assertThrows(RuntimeException.class, () -> materialsDataManager.destroyBatch(new BatchId(1000000)));

		});

	}

	@Test
	@UnitTestMethod(name = "destroyStage", args = { StageId.class })
	public void testDestroyStage() {

		MaterialsActionSupport.testConsumer(6323403688959267719L, (c) -> {
			MaterialsDataManager materialsDataManager = getMaterialsDataManager(c);
			StochasticsDataView stochasticsDataView = c.getDataView(StochasticsDataView.class).get();
			RandomGenerator randomGenerator = stochasticsDataView.getRandomGenerator();

			List<StageId> stageIds = new ArrayList<>();
			for (int i = 0; i < 100; i++) {
				TestMaterialsProducerId testMaterialsProducerId = TestMaterialsProducerId.getRandomMaterialsProducerId(randomGenerator);
				StageId stageId = materialsDataManager.createStage(testMaterialsProducerId);
				stageIds.add(stageId);
			}
			for (StageId stageId : stageIds) {
				assertTrue(materialsDataManager.stageExists(stageId));
				materialsDataManager.destroyStage(stageId);
				assertFalse(materialsDataManager.stageExists(stageId));
			}

			// precondition tests

			// if the batch id is null
			assertThrows(RuntimeException.class, () -> materialsDataManager.destroyStage(null));

			// if the batch id is unknown
			assertThrows(RuntimeException.class, () -> materialsDataManager.destroyStage(new StageId(1000000)));

		});
	}

	@Test
	@UnitTestMethod(name = "getBatchAmount", args = { BatchId.class })
	public void testGetBatchAmount() {

		MaterialsActionSupport.testConsumer(587243311667892614L, (c) -> {
			MaterialsDataManager materialsDataManager = getMaterialsDataManager(c);
			StochasticsDataView stochasticsDataView = c.getDataView(StochasticsDataView.class).get();
			RandomGenerator randomGenerator = stochasticsDataView.getRandomGenerator();

			for (int i = 0; i < 100; i++) {
				TestMaterialsProducerId testMaterialsProducerId = TestMaterialsProducerId.getRandomMaterialsProducerId(randomGenerator);
				TestMaterialId testMaterialId = TestMaterialId.getRandomMaterialId(randomGenerator);
				double value = randomGenerator.nextDouble() * 100;
				BatchId batchId = materialsDataManager.createBatch(testMaterialsProducerId, testMaterialId, value);

				// show that the batch matches the inputs
				assertEquals(value, materialsDataManager.getBatchAmount(batchId));
			}

			// precondition tests : none

		});
	}

	@Test
	@UnitTestMethod(name = "getBatchMaterial", args = { BatchId.class })
	public void testGetBatchMaterial() {

		MaterialsActionSupport.testConsumer(7237305498429140287L, (c) -> {
			MaterialsDataManager materialsDataManager = getMaterialsDataManager(c);
			StochasticsDataView stochasticsDataView = c.getDataView(StochasticsDataView.class).get();
			RandomGenerator randomGenerator = stochasticsDataView.getRandomGenerator();

			for (int i = 0; i < 100; i++) {
				TestMaterialsProducerId testMaterialsProducerId = TestMaterialsProducerId.getRandomMaterialsProducerId(randomGenerator);
				TestMaterialId testMaterialId = TestMaterialId.getRandomMaterialId(randomGenerator);
				double value = randomGenerator.nextDouble() * 100;
				BatchId batchId = materialsDataManager.createBatch(testMaterialsProducerId, testMaterialId, value);

				// show that the batch matches the inputs
				assertEquals(testMaterialId, materialsDataManager.getBatchMaterial(batchId));
			}

			// precondition tests : none

		});
	}

	@Test
	@UnitTestMethod(name = "getBatchProducer", args = { BatchId.class })
	public void testGetBatchProducer() {

		MaterialsActionSupport.testConsumer(8167047880252723180L, (c) -> {
			MaterialsDataManager materialsDataManager = getMaterialsDataManager(c);
			StochasticsDataView stochasticsDataView = c.getDataView(StochasticsDataView.class).get();
			RandomGenerator randomGenerator = stochasticsDataView.getRandomGenerator();

			for (int i = 0; i < 100; i++) {
				TestMaterialsProducerId testMaterialsProducerId = TestMaterialsProducerId.getRandomMaterialsProducerId(randomGenerator);
				TestMaterialId testMaterialId = TestMaterialId.getRandomMaterialId(randomGenerator);
				double value = randomGenerator.nextDouble() * 100;
				BatchId batchId = materialsDataManager.createBatch(testMaterialsProducerId, testMaterialId, value);

				// show that the batch matches the inputs
				assertEquals(testMaterialsProducerId, materialsDataManager.getBatchProducer(batchId));
			}

			// precondition tests : none

		});
	}

	@Test
	@UnitTestMethod(name = "getBatchPropertyDefinition", args = { MaterialId.class, BatchPropertyId.class })
	public void testGetBatchPropertyDefinition() {

		MaterialsActionSupport.testConsumer(867889486512350294L, (c) -> {
			MaterialsDataManager materialsDataManager = getMaterialsDataManager(c);

			for (TestMaterialId testMaterialId : TestMaterialId.values()) {

				Set<TestBatchPropertyId> testBatchPropertyIds = TestBatchPropertyId.getTestBatchPropertyIds(testMaterialId);
				for (TestBatchPropertyId testBatchPropertyId : testBatchPropertyIds) {
					PropertyDefinition expectedPropertyDefinition = testBatchPropertyId.getPropertyDefinition();
					PropertyDefinition actualPropertyDefinition = materialsDataManager.getBatchPropertyDefinition(testMaterialId, testBatchPropertyId);
					assertEquals(expectedPropertyDefinition, actualPropertyDefinition);
				}
			}

			// precondition tests
			TestBatchPropertyId testBatchPropertyId = TestBatchPropertyId.BATCH_PROPERTY_2_3_DOUBLE_MUTABLE_TRACK;

			// if the material id is null
			assertThrows(RuntimeException.class, () -> materialsDataManager.getBatchPropertyDefinition(null, testBatchPropertyId));

			// if the material id is unknown
			assertThrows(RuntimeException.class, () -> materialsDataManager.getBatchPropertyDefinition(TestMaterialId.getUnknownMaterialId(), testBatchPropertyId));
		});
	}

	@Test
	@UnitTestMethod(name = "getBatchPropertyIds", args = { MaterialId.class })
	public void testGetBatchPropertyIds() {

		MaterialsActionSupport.testConsumer(7236125121689510412L, (c) -> {
			MaterialsDataManager materialsDataManager = getMaterialsDataManager(c);

			for (TestMaterialId testMaterialId : TestMaterialId.values()) {
				Set<TestBatchPropertyId> expectedBatchPropertyIds = TestBatchPropertyId.getTestBatchPropertyIds(testMaterialId);
				Set<BatchPropertyId> actualBatchPropertyIds = materialsDataManager.getBatchPropertyIds(testMaterialId);
				assertEquals(expectedBatchPropertyIds, actualBatchPropertyIds);
			}

			// precondition tests

			// if the material id is null
			assertThrows(RuntimeException.class, () -> materialsDataManager.getBatchPropertyIds(null));

			// if the material id is unknown
			assertThrows(RuntimeException.class, () -> materialsDataManager.getBatchPropertyIds(TestMaterialId.getUnknownMaterialId()));
		});
	}

	@Test
	@UnitTestMethod(name = "batchPropertyIdExists", args = { MaterialId.class, BatchPropertyId.class })
	public void testBatchPropertyIdExists() {

		MaterialsActionSupport.testConsumer(4250860228077588132L, (c) -> {
			MaterialsDataManager materialsDataManager = getMaterialsDataManager(c);

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

	private static class LocalDataView implements DataView {
		MaterialsDataManager materialsDataManager;
	}

	@Test
	@UnitTestMethod(name = "getBatchPropertyTime", args = { BatchId.class, BatchPropertyId.class })
	public void testGetBatchPropertyTime() {

		ActionPlugin.Builder pluginBuilder = ActionPlugin.builder();
		pluginBuilder.addDataView(new LocalDataView());

		// create a data structure to hold the assignments we expect to
		// retrieve.
		Map<MultiKey, MutableDouble> expectedAssignmentTimes = new LinkedHashMap<>();

		// create an agent
		pluginBuilder.addAgent("agent");

		/*
		 * Have the agent add 50 randomized batches and record the assignment
		 * times for all properties
		 */
		pluginBuilder.addAgentActionPlan("agent", new AgentActionPlan(0, (c) -> {
			LocalDataView localDataView = c.getDataView(LocalDataView.class).get();
			localDataView.materialsDataManager = getMaterialsDataManager(c);
			StochasticsDataView stochasticsDataView = c.getDataView(StochasticsDataView.class).get();
			RandomGenerator randomGenerator = stochasticsDataView.getRandomGenerator();

			// create a few batches
			for (int i = 0; i < 50; i++) {
				MaterialsProducerId materialsProducerId = TestMaterialsProducerId.getRandomMaterialsProducerId(randomGenerator);
				MaterialId materialId = TestMaterialId.getRandomMaterialId(randomGenerator);
				double amount = randomGenerator.nextDouble();
				BatchId batchId = localDataView.materialsDataManager.createBatch(materialsProducerId, materialId, amount);

				Set<TestBatchPropertyId> batchPropertyIds = localDataView.materialsDataManager.getBatchPropertyIds(materialId);
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
			pluginBuilder.addAgentActionPlan("agent", new AgentActionPlan(actionTime, (c) -> {
				LocalDataView localDataView = c.getDataView(LocalDataView.class).get();
				MaterialsDataManager materialsDataManager = localDataView.materialsDataManager;
				StochasticsDataView stochasticsDataView = c.getDataView(StochasticsDataView.class).get();
				RandomGenerator randomGenerator = stochasticsDataView.getRandomGenerator();

				// plan several times to alter some of the batch properties

				// alter the batch properties
				for (MaterialsProducerId materialsProducerId : materialsDataManager.getMaterialsProducerIds()) {
					List<BatchId> inventoryBatches = materialsDataManager.getInventoryBatches(materialsProducerId);
					for (BatchId batchId : inventoryBatches) {
						MaterialId materialId = materialsDataManager.getBatchMaterial(batchId);
						Set<TestBatchPropertyId> batchPropertyIds = materialsDataManager.getBatchPropertyIds(materialId);
						for (TestBatchPropertyId batchPropertyId : batchPropertyIds) {
							if (randomGenerator.nextDouble() < 0.33) {
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
		 * Have the agent compare the assignment times at time = 10 to the
		 * expected values.
		 */
		pluginBuilder.addAgentActionPlan("agent", new AgentActionPlan(10, (c) -> {
			LocalDataView localDataView = c.getDataView(LocalDataView.class).get();
			MaterialsDataManager materialsDataManager = localDataView.materialsDataManager;
			StochasticsDataView stochasticsDataView = c.getDataView(StochasticsDataView.class).get();
			RandomGenerator randomGenerator = stochasticsDataView.getRandomGenerator();

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

		/*
		 * precondition tests
		 */
		pluginBuilder.addAgentActionPlan("agent", new AgentActionPlan(11, (c) -> {
			LocalDataView localDataView = c.getDataView(LocalDataView.class).get();
			MaterialsDataManager materialsDataManager = localDataView.materialsDataManager;

			BatchId batchId = new BatchId(0);
			BatchPropertyId batchPropertyId = TestBatchPropertyId.BATCH_PROPERTY_1_1_BOOLEAN_IMMUTABLE_NO_TRACK;

			// if the batch id is null
			assertThrows(RuntimeException.class, () -> materialsDataManager.getBatchPropertyTime(null, batchPropertyId));

			// if the batch id is unknown
			assertThrows(RuntimeException.class, () -> materialsDataManager.getBatchPropertyTime(new BatchId(100000), batchPropertyId));

			// if the batch property id is null
			assertThrows(RuntimeException.class, () -> materialsDataManager.getBatchPropertyTime(batchId, null));

			// if the batch property id is unknown
			assertThrows(RuntimeException.class, () -> materialsDataManager.getBatchPropertyTime(batchId, TestBatchPropertyId.getUnknownBatchPropertyId()));

		}));

		ActionPlugin actionPlugin = pluginBuilder.build();
		MaterialsActionSupport.testConsumers(757267012486628481L, actionPlugin);
	}

	@Test
	@UnitTestMethod(name = "getBatchPropertyValue", args = { BatchId.class, BatchPropertyId.class })
	public void testGetBatchPropertyValue() {

		MaterialsActionSupport.testConsumer(2615775072940414771L, (c) -> {
			// create a data structure to hold the assignments we expect to
			// retrieve.
			Map<MultiKey, Object> expectedValues = new LinkedHashMap<>();

			/*
			 * Have the agent add 50 randomized batches and record the values
			 * for all properties
			 */

			MaterialsDataManager materialsDataManager = getMaterialsDataManager(c);
			StochasticsDataView stochasticsDataView = c.getDataView(StochasticsDataView.class).get();
			RandomGenerator randomGenerator = stochasticsDataView.getRandomGenerator();

			// create a few batches
			for (int i = 0; i < 50; i++) {
				MaterialsProducerId materialsProducerId = TestMaterialsProducerId.getRandomMaterialsProducerId(randomGenerator);
				MaterialId materialId = TestMaterialId.getRandomMaterialId(randomGenerator);
				double amount = randomGenerator.nextDouble();
				BatchId batchId = materialsDataManager.createBatch(materialsProducerId, materialId, amount);

				Set<TestBatchPropertyId> batchPropertyIds = materialsDataManager.getBatchPropertyIds(materialId);
				for (TestBatchPropertyId batchPropertyId : batchPropertyIds) {
					Object value = materialsDataManager.getBatchPropertyValue(batchId, batchPropertyId);
					expectedValues.put(new MultiKey(batchId, batchPropertyId), value);
				}
			}

			// alter randomly chosen batch properties

			for (int i = 0; i < 200; i++) {
				MaterialsProducerId materialsProducerId = TestMaterialsProducerId.getRandomMaterialsProducerId(randomGenerator);
				List<BatchId> inventoryBatches = materialsDataManager.getInventoryBatches(materialsProducerId);
				BatchId batchId = inventoryBatches.get(randomGenerator.nextInt(inventoryBatches.size()));
				MaterialId materialId = materialsDataManager.getBatchMaterial(batchId);
				List<TestBatchPropertyId> batchPropertyIds = new ArrayList<>(materialsDataManager.getBatchPropertyIds(materialId));
				TestBatchPropertyId batchPropertyId = batchPropertyIds.get(randomGenerator.nextInt(batchPropertyIds.size()));
				Object value = batchPropertyId.getRandomPropertyValue(randomGenerator);
				materialsDataManager.setBatchPropertyValue(batchId, batchPropertyId, value);
				expectedValues.put(new MultiKey(batchId, batchPropertyId), value);
			}

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

			BatchId batchId = new BatchId(0);
			BatchPropertyId batchPropertyId = TestBatchPropertyId.BATCH_PROPERTY_1_1_BOOLEAN_IMMUTABLE_NO_TRACK;

			// if the batch id is null
			assertThrows(RuntimeException.class, () -> materialsDataManager.getBatchPropertyValue(null, batchPropertyId));

			// if the batch id is unknown
			assertThrows(RuntimeException.class, () -> materialsDataManager.getBatchPropertyValue(new BatchId(100000), batchPropertyId));

			// if the batch property id is null
			assertThrows(RuntimeException.class, () -> materialsDataManager.getBatchPropertyValue(batchId, null));

			// if the batch property id is unknown
			assertThrows(RuntimeException.class, () -> materialsDataManager.getBatchPropertyValue(batchId, TestBatchPropertyId.getUnknownBatchPropertyId()));

		});

	}

	@Test
	@UnitTestMethod(name = "getBatchStageId", args = { BatchId.class })
	public void testGetBatchStageId() {

		MaterialsActionSupport.testConsumer(4365461858544911964L, (c) -> {
			MaterialsDataManager materialsDataManager = getMaterialsDataManager(c);
			// create a stage and a batch
			StageId stageId = materialsDataManager.createStage(TestMaterialsProducerId.MATERIALS_PRODUCER_1);
			BatchId batchId = materialsDataManager.createBatch(TestMaterialsProducerId.MATERIALS_PRODUCER_1, TestMaterialId.MATERIAL_2, 4.5);

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

			// precondition tests:

			// if the batch id is null
			assertThrows(RuntimeException.class, () -> materialsDataManager.getBatchStageId(null));

			// if the batch id is unknown
			assertThrows(RuntimeException.class, () -> materialsDataManager.getBatchStageId(new BatchId(100000)));

		});
	}

	@Test
	@UnitTestMethod(name = "getBatchTime", args = { BatchId.class })
	public void testGetBatchTime() {
		ActionPlugin.Builder pluginBuilder = ActionPlugin.builder();

		Map<BatchId, Double> expectedBatchTimes = new LinkedHashMap<>();

		pluginBuilder.addDataView(new LocalDataView());

		pluginBuilder.addAgent("agent");

		pluginBuilder.addAgentActionPlan("agent", new AgentActionPlan(0, (c) -> {
			LocalDataView localDataView = c.getDataView(LocalDataView.class).get();
			MaterialsDataManager materialsDataManager = getMaterialsDataManager(c);
			localDataView.materialsDataManager = materialsDataManager;
		}));

		// build batches at several times
		for (int i = 1; i <= 10; i++) {
			double planTime = i;
			pluginBuilder.addAgentActionPlan("agent", new AgentActionPlan(planTime, (c) -> {
				LocalDataView localDataView = c.getDataView(LocalDataView.class).get();
				MaterialsDataManager materialsDataManager = localDataView.materialsDataManager;

				StochasticsDataView stochasticsDataView = c.getDataView(StochasticsDataView.class).get();
				RandomGenerator randomGenerator = stochasticsDataView.getRandomGenerator();

				// build a few batches
				int numberOfBatches = randomGenerator.nextInt(5) + 1;
				for (int j = 0; j < numberOfBatches; j++) {
					TestMaterialsProducerId materialsProducerId = TestMaterialsProducerId.getRandomMaterialsProducerId(randomGenerator);
					TestMaterialId materialId = TestMaterialId.getRandomMaterialId(randomGenerator);
					double amount = randomGenerator.nextDouble();
					BatchId batchId = materialsDataManager.createBatch(materialsProducerId, materialId, amount);
					expectedBatchTimes.put(batchId, c.getTime());
				}

				// show that all batches have the expected batch times
				for (TestMaterialsProducerId materialsProducerId : TestMaterialsProducerId.values()) {
					List<BatchId> inventoryBatches = materialsDataManager.getInventoryBatches(materialsProducerId);
					for (BatchId batchId : inventoryBatches) {
						double expectedBatchTime = expectedBatchTimes.get(batchId);
						double actualBatchTime = materialsDataManager.getBatchTime(batchId);
						assertEquals(expectedBatchTime, actualBatchTime);
					}
				}

			}));

			// precondition tests
			pluginBuilder.addAgentActionPlan("agent", new AgentActionPlan(11, (c) -> {
				LocalDataView localDataView = c.getDataView(LocalDataView.class).get();
				MaterialsDataManager materialsDataManager = localDataView.materialsDataManager;

				// if the batch id is null
				assertThrows(RuntimeException.class, () -> materialsDataManager.getBatchTime(null));
				// if the batch id is unknown
				assertThrows(RuntimeException.class, () -> materialsDataManager.getBatchTime(new BatchId(1000000)));
			}));
		}
		ActionPlugin actionPlugin = pluginBuilder.build();
		MaterialsActionSupport.testConsumers(2148567900898183351L, actionPlugin);
	}

	@Test
	@UnitTestMethod(name = "getInventoryBatches", args = { MaterialsProducerId.class })
	public void testGetInventoryBatches() {

		MaterialsActionSupport.testConsumer(5461135233101939296L, (c) -> {

			StochasticsDataView stochasticsDataView = c.getDataView(StochasticsDataView.class).get();
			RandomGenerator randomGenerator = stochasticsDataView.getRandomGenerator();

			MaterialsDataManager materialsDataManager = getMaterialsDataManager(c);
			MaterialsProducerId materialsProducerId = TestMaterialsProducerId.MATERIALS_PRODUCER_1;

			Set<BatchId> expectedInventoryBatches = new LinkedHashSet<>();
			// create some batches
			for (int i = 0; i < 100; i++) {
				BatchId batchId = materialsDataManager.createBatch(materialsProducerId, TestMaterialId.getRandomMaterialId(randomGenerator), randomGenerator.nextInt(100));
				expectedInventoryBatches.add(batchId);
			}

			// show that the inventory batches are correct
			List<BatchId> inventory = materialsDataManager.getInventoryBatches(materialsProducerId);
			Set<BatchId> actualInventoryBatches = new LinkedHashSet<>(inventory);
			assertEquals(inventory.size(), actualInventoryBatches.size());
			assertEquals(expectedInventoryBatches, actualInventoryBatches);

			// create some stages and put some of the batches onto stages
			List<BatchId> batches = new ArrayList<>(expectedInventoryBatches);
			Collections.shuffle(batches, new Random(randomGenerator.nextLong()));
			int batchIndex = 0;
			for (int i = 0; i < 5; i++) {
				StageId stageId = materialsDataManager.createStage(materialsProducerId);
				for (int j = 0; j < 5; j++) {
					BatchId batchId = batches.get(batchIndex++);
					materialsDataManager.moveBatchToStage(batchId, stageId);
					expectedInventoryBatches.remove(batchId);

				}
			}

			// destroy a few inventory batches
			for (int i = 0; i < 10; i++) {
				BatchId batchId = batches.get(batchIndex++);
				materialsDataManager.destroyBatch(batchId);
				expectedInventoryBatches.remove(batchId);
			}

			// show that the inventory batches are correct
			List<BatchId> batchList = materialsDataManager.getInventoryBatches(materialsProducerId);
			actualInventoryBatches = new LinkedHashSet<>(batchList);
			assertEquals(batchList.size(), actualInventoryBatches.size());
			assertEquals(expectedInventoryBatches, actualInventoryBatches);

			// precondition tests

			// if the materials producerId id is null
			assertThrows(RuntimeException.class, () -> materialsDataManager.getInventoryBatches(null));

			// if the materials producerId id is unknown
			assertThrows(RuntimeException.class, () -> materialsDataManager.getInventoryBatches(TestMaterialsProducerId.getUnknownMaterialsProducerId()));

		});
	}

	@Test
	@UnitTestMethod(name = "getInventoryBatchesByMaterialId", args = { MaterialsProducerId.class, MaterialId.class })
	public void testGetInventoryBatchesByMaterialId() {

		MaterialsActionSupport.testConsumer(3492079207976577634L, (c) -> {

			StochasticsDataView stochasticsDataView = c.getDataView(StochasticsDataView.class).get();
			RandomGenerator randomGenerator = stochasticsDataView.getRandomGenerator();

			MaterialsDataManager materialsDataManager = getMaterialsDataManager(c);
			MaterialsProducerId materialsProducerId = TestMaterialsProducerId.MATERIALS_PRODUCER_1;

			Map<MaterialId, Set<BatchId>> expectedInventoryBatchesMap = new LinkedHashMap<>();
			for (TestMaterialId testMaterialId : TestMaterialId.values()) {
				expectedInventoryBatchesMap.put(testMaterialId, new LinkedHashSet<>());
			}
			// create some batches
			for (int i = 0; i < 100; i++) {
				MaterialId materialId = TestMaterialId.getRandomMaterialId(randomGenerator);
				BatchId batchId = materialsDataManager.createBatch(materialsProducerId, materialId, randomGenerator.nextInt(100));
				expectedInventoryBatchesMap.get(materialId).add(batchId);
			}

			// show that the inventory batches are correct
			for (TestMaterialId testMaterialId : TestMaterialId.values()) {
				List<BatchId> batchList = materialsDataManager.getInventoryBatchesByMaterialId(materialsProducerId, testMaterialId);
				Set<BatchId> actualInventoryBatches = new LinkedHashSet<>(batchList);
				assertEquals(batchList.size(), actualInventoryBatches.size());
				Set<BatchId> expectedInventoryBatches = expectedInventoryBatchesMap.get(testMaterialId);
				assertEquals(expectedInventoryBatches, actualInventoryBatches);
			}

			// create some stages and put some of the batches onto stages
			List<BatchId> batches = new ArrayList<>();
			for (Set<BatchId> expectedInventoryBatches : expectedInventoryBatchesMap.values()) {
				batches.addAll(expectedInventoryBatches);
			}
			Collections.shuffle(batches, new Random(randomGenerator.nextLong()));
			int batchIndex = 0;
			for (int i = 0; i < 5; i++) {
				StageId stageId = materialsDataManager.createStage(materialsProducerId);
				for (int j = 0; j < 5; j++) {
					BatchId batchId = batches.get(batchIndex++);
					materialsDataManager.moveBatchToStage(batchId, stageId);
					MaterialId batchMaterial = materialsDataManager.getBatchMaterial(batchId);
					expectedInventoryBatchesMap.get(batchMaterial).remove(batchId);

				}
			}

			// destroy a few inventory batches
			for (int i = 0; i < 10; i++) {
				BatchId batchId = batches.get(batchIndex++);
				MaterialId batchMaterial = materialsDataManager.getBatchMaterial(batchId);
				materialsDataManager.destroyBatch(batchId);
				expectedInventoryBatchesMap.get(batchMaterial).remove(batchId);
			}

			// show that the inventory batches are correct
			for (TestMaterialId testMaterialId : TestMaterialId.values()) {
				List<BatchId> invBatches = materialsDataManager.getInventoryBatchesByMaterialId(materialsProducerId, testMaterialId);
				Set<BatchId> actualInventoryBatches = new LinkedHashSet<>(invBatches);
				assertEquals(invBatches.size(), actualInventoryBatches.size());
				Set<BatchId> expectedInventoryBatches = expectedInventoryBatchesMap.get(testMaterialId);
				assertEquals(expectedInventoryBatches, actualInventoryBatches);
			}

			// precondition tests

			// if the materials producerId id is null
			assertThrows(RuntimeException.class, () -> materialsDataManager.getInventoryBatchesByMaterialId(null, TestMaterialId.MATERIAL_1));

			// if the materials producerId id is unknown
			assertThrows(RuntimeException.class, () -> materialsDataManager.getInventoryBatchesByMaterialId(TestMaterialsProducerId.getUnknownMaterialsProducerId(), TestMaterialId.MATERIAL_2));

		});
	}

	@Test
	@UnitTestMethod(name = "getLastIssuedBatchId", args = {})
	public void testGetLastIssuedBatchId() {

		MaterialsActionSupport.testConsumer(8362619045299929852L, (c) -> {

			StochasticsDataView stochasticsDataView = c.getDataView(StochasticsDataView.class).get();
			RandomGenerator randomGenerator = stochasticsDataView.getRandomGenerator();

			MaterialsDataManager materialsDataManager = getMaterialsDataManager(c);
			assertFalse(materialsDataManager.getLastIssuedBatchId().isPresent());
			for (int i = 0; i < 20; i++) {
				BatchId batchId = materialsDataManager.createBatch(TestMaterialsProducerId.getRandomMaterialsProducerId(randomGenerator), TestMaterialId.getRandomMaterialId(randomGenerator),
						randomGenerator.nextInt(100));
				assertTrue(materialsDataManager.getLastIssuedBatchId().isPresent());
				assertEquals(batchId, materialsDataManager.getLastIssuedBatchId().get());
			}

		});
	}

	@Test
	@UnitTestMethod(name = "getLastIssuedStageId", args = {})
	public void testGetLastIssuedStageId() {

		MaterialsActionSupport.testConsumer(6693768795920272844L, (c) -> {

			StochasticsDataView stochasticsDataView = c.getDataView(StochasticsDataView.class).get();
			RandomGenerator randomGenerator = stochasticsDataView.getRandomGenerator();

			MaterialsDataManager materialsDataManager = getMaterialsDataManager(c);
			assertFalse(materialsDataManager.getLastIssuedStageId().isPresent());
			for (int i = 0; i < 20; i++) {
				StageId stageId = materialsDataManager.createStage(TestMaterialsProducerId.getRandomMaterialsProducerId(randomGenerator));
				assertTrue(materialsDataManager.getLastIssuedStageId().isPresent());
				assertEquals(stageId, materialsDataManager.getLastIssuedStageId().get());
			}

		});
	}

	@Test
	@UnitTestMethod(name = "getMaterialIds", args = {})
	public void testGetMaterialIds() {

		MaterialsActionSupport.testConsumer(3103504195281274292L, (c) -> {
			MaterialsDataManager materialsDataManager = getMaterialsDataManager(c);
			assertEquals(EnumSet.allOf(TestMaterialId.class), materialsDataManager.getMaterialIds());
		});
	}

	@Test
	@UnitTestMethod(name = "materialIdExists", args = { MaterialId.class })
	public void testMaterialIdExists() {

		MaterialsActionSupport.testConsumer(6918669723394457093L, (c) -> {
			MaterialsDataManager materialsDataManager = getMaterialsDataManager(c);

			for (TestMaterialId testMaterialId : TestMaterialId.values()) {
				assertTrue(materialsDataManager.materialIdExists(testMaterialId));
			}
			assertFalse(materialsDataManager.materialIdExists(TestMaterialId.getUnknownMaterialId()));
			assertFalse(materialsDataManager.materialIdExists(null));
		});
	}

	@Test
	@UnitTestMethod(name = "getMaterialsProducerIds", args = {})
	public void testGetMaterialsProducerIds() {

		MaterialsActionSupport.testConsumer(1677938075583498628L, (c) -> {
			MaterialsDataManager materialsDataManager = getMaterialsDataManager(c);
			assertEquals(EnumSet.allOf(TestMaterialsProducerId.class), materialsDataManager.getMaterialsProducerIds());
		});
	}

	@Test
	@UnitTestMethod(name = "getMaterialsProducerPropertyDefinition", args = { MaterialsProducerPropertyId.class })
	public void testGetMaterialsProducerPropertyDefinition() {

		MaterialsActionSupport.testConsumer(1515049910717371593L, (c) -> {
			MaterialsDataManager materialsDataManager = getMaterialsDataManager(c);

			for (TestMaterialsProducerPropertyId testMaterialsProducerPropertyId : TestMaterialsProducerPropertyId.values()) {
				PropertyDefinition expectedPropertyDefinition = testMaterialsProducerPropertyId.getPropertyDefinition();
				PropertyDefinition actualPropertyDefinition = materialsDataManager.getMaterialsProducerPropertyDefinition(testMaterialsProducerPropertyId);
				assertEquals(expectedPropertyDefinition, actualPropertyDefinition);
			}
		});
	}

	@Test
	@UnitTestMethod(name = "getMaterialsProducerPropertyIds", args = {})
	public void testGetMaterialsProducerPropertyIds() {

		MaterialsActionSupport.testConsumer(6586180630383700824L, (c) -> {
			MaterialsDataManager materialsDataManager = getMaterialsDataManager(c);
			assertEquals(EnumSet.allOf(TestMaterialsProducerPropertyId.class), materialsDataManager.getMaterialsProducerPropertyIds());
		});

	}

	@Test
	@UnitTestMethod(name = "getMaterialsProducerPropertyTime", args = { MaterialsProducerId.class, MaterialsProducerPropertyId.class })
	public void testGetMaterialsProducerPropertyTime() {

		ActionPlugin.Builder pluginBuilder = ActionPlugin.builder();
		pluginBuilder.addAgent("agent");

		pluginBuilder.addDataView(new LocalDataView());

		// create a structure to hold expected assignment times for materials
		// producer property values
		Map<MultiKey, MutableDouble> expectedTimesMap = new LinkedHashMap<>();
		for (TestMaterialsProducerId testMaterialsProducerId : TestMaterialsProducerId.values()) {
			for (TestMaterialsProducerPropertyId testMaterialsProducerPropertyId : TestMaterialsProducerPropertyId.values()) {
				expectedTimesMap.put(new MultiKey(testMaterialsProducerId, testMaterialsProducerPropertyId), new MutableDouble());
			}
		}

		// initialize the materials data manager
		pluginBuilder.addAgentActionPlan("agent", new AgentActionPlan(0, (c) -> {
			LocalDataView localDataView = c.getDataView(LocalDataView.class).get();
			localDataView.materialsDataManager = getMaterialsDataManager(c);
		}));

		// determine a reasonable number of changes per time
		int propertyChangeCount = TestMaterialsProducerId.size() * TestMaterialsProducerPropertyId.size() / 10;

		// update several random property values at various times
		for (int i = 0; i < 10; i++) {
			pluginBuilder.addAgentActionPlan("agent", new AgentActionPlan(0, (c) -> {
				StochasticsDataView stochasticsDataView = c.getDataView(StochasticsDataView.class).get();
				RandomGenerator randomGenerator = stochasticsDataView.getRandomGenerator();

				LocalDataView localDataView = c.getDataView(LocalDataView.class).get();
				MaterialsDataManager materialsDataManager = localDataView.materialsDataManager;

				for (int j = 0; j < propertyChangeCount; j++) {
					TestMaterialsProducerId materialsProducerId = TestMaterialsProducerId.getRandomMaterialsProducerId(randomGenerator);
					TestMaterialsProducerPropertyId materialsProducerPropertyId = TestMaterialsProducerPropertyId.getRandomMaterialsProducerPropertyId(randomGenerator);
					Object propertyValue = materialsProducerPropertyId.getRandomPropertyValue(randomGenerator);
					materialsDataManager.setMaterialsProducerPropertyValue(materialsProducerId, materialsProducerPropertyId, propertyValue);
					MultiKey multiKey = new MultiKey(materialsProducerId, materialsProducerPropertyId);
					expectedTimesMap.get(multiKey).setValue(c.getTime());
				}
			}));
		}

		pluginBuilder.addAgentActionPlan("agent", new AgentActionPlan(10, (c) -> {
			LocalDataView localDataView = c.getDataView(LocalDataView.class).get();
			MaterialsDataManager materialsDataManager = localDataView.materialsDataManager;
			for (TestMaterialsProducerId testMaterialsProducerId : TestMaterialsProducerId.values()) {
				for (TestMaterialsProducerPropertyId testMaterialsProducerPropertyId : TestMaterialsProducerPropertyId.values()) {
					MultiKey multiKey = new MultiKey(testMaterialsProducerId, testMaterialsProducerPropertyId);
					double expectedTime = expectedTimesMap.get(multiKey).getValue();
					double actualTime = materialsDataManager.getMaterialsProducerPropertyTime(testMaterialsProducerId, testMaterialsProducerPropertyId);
					assertEquals(expectedTime, actualTime);
				}
			}
		}));

		// precondition tests
		pluginBuilder.addAgentActionPlan("agent", new AgentActionPlan(11, (c) -> {
			LocalDataView localDataView = c.getDataView(LocalDataView.class).get();
			MaterialsDataManager materialsDataManager = localDataView.materialsDataManager;

			// if the materials producerId id is null
			assertThrows(RuntimeException.class,
					() -> materialsDataManager.getMaterialsProducerPropertyTime(null, TestMaterialsProducerPropertyId.MATERIALS_PRODUCER_PROPERTY_4_BOOLEAN_MUTABLE_TRACK));

			// if the materials producerId id is unknown
			assertThrows(RuntimeException.class, () -> materialsDataManager.getMaterialsProducerPropertyTime(TestMaterialsProducerId.getUnknownMaterialsProducerId(),
					TestMaterialsProducerPropertyId.MATERIALS_PRODUCER_PROPERTY_4_BOOLEAN_MUTABLE_TRACK));

			// if the materials producerId property id is null
			assertThrows(RuntimeException.class, () -> materialsDataManager.getMaterialsProducerPropertyTime(TestMaterialsProducerId.MATERIALS_PRODUCER_1, null));

			// if the materials producerId property id is unknown
			assertThrows(RuntimeException.class,
					() -> materialsDataManager.getMaterialsProducerPropertyTime(TestMaterialsProducerId.MATERIALS_PRODUCER_1, TestMaterialsProducerPropertyId.getUnknownMaterialsProducerPropertyId()));

		}));

		ActionPlugin actionPlugin = pluginBuilder.build();
		MaterialsActionSupport.testConsumers(3007587740871717747L, actionPlugin);

	}

	@Test
	@UnitTestMethod(name = "getMaterialsProducerPropertyValue", args = { MaterialsProducerId.class, MaterialsProducerPropertyId.class })
	public void testGetMaterialsProducerPropertyValue() {

		ActionPlugin.Builder pluginBuilder = ActionPlugin.builder();
		pluginBuilder.addAgent("agent");

		pluginBuilder.addDataView(new LocalDataView());

		// create a structure to hold expected assignment times for materials
		// producer property values
		Map<MultiKey, Object> expectedValuesMap = new LinkedHashMap<>();

		// initialize the materials data manager
		pluginBuilder.addAgentActionPlan("agent", new AgentActionPlan(0, (c) -> {
			LocalDataView localDataView = c.getDataView(LocalDataView.class).get();
			localDataView.materialsDataManager = getMaterialsDataManager(c);

			for (TestMaterialsProducerId testMaterialsProducerId : TestMaterialsProducerId.values()) {
				for (TestMaterialsProducerPropertyId testMaterialsProducerPropertyId : TestMaterialsProducerPropertyId.values()) {
					MultiKey multiKey = new MultiKey(testMaterialsProducerId, testMaterialsProducerPropertyId);
					Object value = localDataView.materialsDataManager.getMaterialsProducerPropertyValue(testMaterialsProducerId, testMaterialsProducerPropertyId);
					expectedValuesMap.put(multiKey, value);
				}
			}

		}));

		// determine a reasonable number of changes per time
		int propertyChangeCount = TestMaterialsProducerId.size() * TestMaterialsProducerPropertyId.size() / 10;

		// update several random property values at various times
		for (int i = 0; i < 10; i++) {
			pluginBuilder.addAgentActionPlan("agent", new AgentActionPlan(0, (c) -> {
				StochasticsDataView stochasticsDataView = c.getDataView(StochasticsDataView.class).get();
				RandomGenerator randomGenerator = stochasticsDataView.getRandomGenerator();

				LocalDataView localDataView = c.getDataView(LocalDataView.class).get();
				MaterialsDataManager materialsDataManager = localDataView.materialsDataManager;

				for (int j = 0; j < propertyChangeCount; j++) {
					TestMaterialsProducerId materialsProducerId = TestMaterialsProducerId.getRandomMaterialsProducerId(randomGenerator);
					TestMaterialsProducerPropertyId materialsProducerPropertyId = TestMaterialsProducerPropertyId.getRandomMaterialsProducerPropertyId(randomGenerator);
					Object propertyValue = materialsProducerPropertyId.getRandomPropertyValue(randomGenerator);
					materialsDataManager.setMaterialsProducerPropertyValue(materialsProducerId, materialsProducerPropertyId, propertyValue);
					MultiKey multiKey = new MultiKey(materialsProducerId, materialsProducerPropertyId);
					expectedValuesMap.put(multiKey, propertyValue);
				}
			}));
		}

		pluginBuilder.addAgentActionPlan("agent", new AgentActionPlan(10, (c) -> {
			LocalDataView localDataView = c.getDataView(LocalDataView.class).get();
			MaterialsDataManager materialsDataManager = localDataView.materialsDataManager;
			for (TestMaterialsProducerId testMaterialsProducerId : TestMaterialsProducerId.values()) {
				for (TestMaterialsProducerPropertyId testMaterialsProducerPropertyId : TestMaterialsProducerPropertyId.values()) {
					MultiKey multiKey = new MultiKey(testMaterialsProducerId, testMaterialsProducerPropertyId);
					Object expectedValue = expectedValuesMap.get(multiKey);
					Object actualValue = materialsDataManager.getMaterialsProducerPropertyValue(testMaterialsProducerId, testMaterialsProducerPropertyId);
					assertEquals(expectedValue, actualValue);
				}
			}
		}));

		// precondition tests
		pluginBuilder.addAgentActionPlan("agent", new AgentActionPlan(11, (c) -> {
			LocalDataView localDataView = c.getDataView(LocalDataView.class).get();
			MaterialsDataManager materialsDataManager = localDataView.materialsDataManager;

			// if the materials producerId id is null
			assertThrows(RuntimeException.class,
					() -> materialsDataManager.getMaterialsProducerPropertyValue(null, TestMaterialsProducerPropertyId.MATERIALS_PRODUCER_PROPERTY_4_BOOLEAN_MUTABLE_TRACK));

			// if the materials producerId id is unknown
			assertThrows(RuntimeException.class, () -> materialsDataManager.getMaterialsProducerPropertyValue(TestMaterialsProducerId.getUnknownMaterialsProducerId(),
					TestMaterialsProducerPropertyId.MATERIALS_PRODUCER_PROPERTY_4_BOOLEAN_MUTABLE_TRACK));

			// if the materials producerId property id is null
			assertThrows(RuntimeException.class, () -> materialsDataManager.getMaterialsProducerPropertyValue(TestMaterialsProducerId.MATERIALS_PRODUCER_1, null));

			// if the materials producerId property id is unknown
			assertThrows(RuntimeException.class, () -> materialsDataManager.getMaterialsProducerPropertyValue(TestMaterialsProducerId.MATERIALS_PRODUCER_1,
					TestMaterialsProducerPropertyId.getUnknownMaterialsProducerPropertyId()));

		}));

		ActionPlugin actionPlugin = pluginBuilder.build();
		MaterialsActionSupport.testConsumers(6607259282289246382L, actionPlugin);
	}

	@Test
	@UnitTestMethod(name = "getMaterialsProducerResourceTime", args = { MaterialsProducerId.class, ResourceId.class })
	public void testGetMaterialsProducerResourceTime() {

		ActionPlugin.Builder pluginBuilder = ActionPlugin.builder();
		pluginBuilder.addAgent("agent");

		pluginBuilder.addDataView(new LocalDataView());

		// create a structure to hold expected assignment times for producer
		// resource levels
		Map<MultiKey, MutableDouble> expectedTimesMap = new LinkedHashMap<>();
		for (TestMaterialsProducerId testMaterialsProducerId : TestMaterialsProducerId.values()) {
			for (TestResourceId testResourceId : TestResourceId.values()) {
				expectedTimesMap.put(new MultiKey(testMaterialsProducerId, testResourceId), new MutableDouble());
			}
		}

		// initialize the materials data manager
		pluginBuilder.addAgentActionPlan("agent", new AgentActionPlan(0, (c) -> {
			LocalDataView localDataView = c.getDataView(LocalDataView.class).get();
			localDataView.materialsDataManager = getMaterialsDataManager(c);
		}));

		// determine a reasonable number of changes per time
		int resourceLevelChangeCount = TestMaterialsProducerId.size() * TestResourceId.size() / 10;

		// update several random resource levels values at various times
		for (int i = 0; i < 10; i++) {
			pluginBuilder.addAgentActionPlan("agent", new AgentActionPlan(0, (c) -> {
				StochasticsDataView stochasticsDataView = c.getDataView(StochasticsDataView.class).get();
				RandomGenerator randomGenerator = stochasticsDataView.getRandomGenerator();

				LocalDataView localDataView = c.getDataView(LocalDataView.class).get();
				MaterialsDataManager materialsDataManager = localDataView.materialsDataManager;

				for (int j = 0; j < resourceLevelChangeCount; j++) {
					TestMaterialsProducerId materialsProducerId = TestMaterialsProducerId.getRandomMaterialsProducerId(randomGenerator);
					TestResourceId testResourceId = TestResourceId.getRandomResourceId(randomGenerator);
					Integer resourceLevel = randomGenerator.nextInt(100) + 1;
					materialsDataManager.incrementMaterialsProducerResourceLevel(materialsProducerId, testResourceId, resourceLevel);
					MultiKey multiKey = new MultiKey(materialsProducerId, testResourceId);
					expectedTimesMap.get(multiKey).setValue(c.getTime());
				}
			}));
		}

		pluginBuilder.addAgentActionPlan("agent", new AgentActionPlan(10, (c) -> {
			LocalDataView localDataView = c.getDataView(LocalDataView.class).get();
			MaterialsDataManager materialsDataManager = localDataView.materialsDataManager;
			for (TestMaterialsProducerId testMaterialsProducerId : TestMaterialsProducerId.values()) {
				for (TestResourceId testResourceId : TestResourceId.values()) {
					MultiKey multiKey = new MultiKey(testMaterialsProducerId, testResourceId);
					double expectedTime = expectedTimesMap.get(multiKey).getValue();
					double actualTime = materialsDataManager.getMaterialsProducerResourceTime(testMaterialsProducerId, testResourceId);
					assertEquals(expectedTime, actualTime);
				}
			}
		}));

		// precondition tests
		pluginBuilder.addAgentActionPlan("agent", new AgentActionPlan(11, (c) -> {
			LocalDataView localDataView = c.getDataView(LocalDataView.class).get();
			MaterialsDataManager materialsDataManager = localDataView.materialsDataManager;

			// if the materials producerId id is null
			assertThrows(RuntimeException.class, () -> materialsDataManager.getMaterialsProducerResourceTime(null, TestResourceId.RESOURCE_1));

			// if the materials producerId id is unknown
			assertThrows(RuntimeException.class, () -> materialsDataManager.getMaterialsProducerResourceTime(TestMaterialsProducerId.getUnknownMaterialsProducerId(), TestResourceId.RESOURCE_1));

			// if the resource id is null
			assertThrows(RuntimeException.class, () -> materialsDataManager.getMaterialsProducerResourceTime(TestMaterialsProducerId.MATERIALS_PRODUCER_1, null));

			// if the resource id is unknown
			assertThrows(RuntimeException.class, () -> materialsDataManager.getMaterialsProducerResourceTime(TestMaterialsProducerId.MATERIALS_PRODUCER_1, TestResourceId.getUnknownResourceId()));

		}));

		ActionPlugin actionPlugin = pluginBuilder.build();
		MaterialsActionSupport.testConsumers(8812558494817366843L, actionPlugin);
	}

	@Test
	@UnitTestMethod(name = "getMaterialsProducerResourceLevel", args = { MaterialsProducerId.class, ResourceId.class })
	public void testGetMaterialsProducerResourceLevel() {

		ActionPlugin.Builder pluginBuilder = ActionPlugin.builder();
		pluginBuilder.addAgent("agent");

		pluginBuilder.addDataView(new LocalDataView());

		// create a structure to hold expected resource levels for producers
		Map<MultiKey, MutableLong> expectedLevelsMap = new LinkedHashMap<>();
		for (TestMaterialsProducerId testMaterialsProducerId : TestMaterialsProducerId.values()) {
			for (TestResourceId testResourceId : TestResourceId.values()) {
				expectedLevelsMap.put(new MultiKey(testMaterialsProducerId, testResourceId), new MutableLong());
			}
		}

		// initialize the materials data manager
		pluginBuilder.addAgentActionPlan("agent", new AgentActionPlan(0, (c) -> {
			LocalDataView localDataView = c.getDataView(LocalDataView.class).get();
			localDataView.materialsDataManager = getMaterialsDataManager(c);
		}));

		// determine a reasonable number of changes per time
		int resourceLevelChangeCount = TestMaterialsProducerId.size() * TestResourceId.size() / 10;

		// update several random resource levels values at various times
		for (int i = 0; i < 10; i++) {
			pluginBuilder.addAgentActionPlan("agent", new AgentActionPlan(0, (c) -> {
				StochasticsDataView stochasticsDataView = c.getDataView(StochasticsDataView.class).get();
				RandomGenerator randomGenerator = stochasticsDataView.getRandomGenerator();

				LocalDataView localDataView = c.getDataView(LocalDataView.class).get();
				MaterialsDataManager materialsDataManager = localDataView.materialsDataManager;

				for (int j = 0; j < resourceLevelChangeCount; j++) {
					TestMaterialsProducerId materialsProducerId = TestMaterialsProducerId.getRandomMaterialsProducerId(randomGenerator);
					TestResourceId testResourceId = TestResourceId.getRandomResourceId(randomGenerator);
					Long resourceLevel = (long) (randomGenerator.nextInt(100) + 1);
					materialsDataManager.incrementMaterialsProducerResourceLevel(materialsProducerId, testResourceId, resourceLevel);
					MultiKey multiKey = new MultiKey(materialsProducerId, testResourceId);
					MutableLong mutableLong = expectedLevelsMap.get(multiKey);
					mutableLong.increment(resourceLevel);
				}
			}));
		}

		pluginBuilder.addAgentActionPlan("agent", new AgentActionPlan(10, (c) -> {
			LocalDataView localDataView = c.getDataView(LocalDataView.class).get();
			MaterialsDataManager materialsDataManager = localDataView.materialsDataManager;
			for (TestMaterialsProducerId testMaterialsProducerId : TestMaterialsProducerId.values()) {
				for (TestResourceId testResourceId : TestResourceId.values()) {
					MultiKey multiKey = new MultiKey(testMaterialsProducerId, testResourceId);
					long expectedLevel = expectedLevelsMap.get(multiKey).getValue();
					long actualLevel = materialsDataManager.getMaterialsProducerResourceLevel(testMaterialsProducerId, testResourceId);
					assertEquals(expectedLevel, actualLevel);
				}
			}
		}));

		// precondition tests
		pluginBuilder.addAgentActionPlan("agent", new AgentActionPlan(11, (c) -> {
			LocalDataView localDataView = c.getDataView(LocalDataView.class).get();
			MaterialsDataManager materialsDataManager = localDataView.materialsDataManager;

			// if the materials producerId id is null
			assertThrows(RuntimeException.class, () -> materialsDataManager.getMaterialsProducerResourceLevel(null, TestResourceId.RESOURCE_1));

			// if the materials producerId id is unknown
			assertThrows(RuntimeException.class, () -> materialsDataManager.getMaterialsProducerResourceLevel(TestMaterialsProducerId.getUnknownMaterialsProducerId(), TestResourceId.RESOURCE_1));

			// if the resource id is null
			assertThrows(RuntimeException.class, () -> materialsDataManager.getMaterialsProducerResourceLevel(TestMaterialsProducerId.MATERIALS_PRODUCER_1, null));

			// if the resource id is unknown
			assertThrows(RuntimeException.class, () -> materialsDataManager.getMaterialsProducerResourceLevel(TestMaterialsProducerId.MATERIALS_PRODUCER_1, TestResourceId.getUnknownResourceId()));

		}));

		ActionPlugin actionPlugin = pluginBuilder.build();
		MaterialsActionSupport.testConsumers(2508411066079102944L, actionPlugin);
	}

	@Test
	@UnitTestMethod(name = "getOfferedStages", args = { MaterialsProducerId.class })
	public void testGetOfferedStages() {

		MaterialsActionSupport.testConsumer(6961352999804722865L, (c) -> {
			StochasticsDataView stochasticsDataView = c.getDataView(StochasticsDataView.class).get();
			RandomGenerator randomGenerator = stochasticsDataView.getRandomGenerator();
			MaterialsDataManager materialsDataManager = getMaterialsDataManager(c);

			// create several stages and place about half of them into the offer
			// state
			Map<MaterialsProducerId, Set<StageId>> expectedStageOffers = new LinkedHashMap<>();
			for (TestMaterialsProducerId testMaterialsProducerId : TestMaterialsProducerId.values()) {
				expectedStageOffers.put(testMaterialsProducerId, new LinkedHashSet<>());
			}
			for (int i = 0; i < 100; i++) {
				TestMaterialsProducerId materialsProducerId = TestMaterialsProducerId.getRandomMaterialsProducerId(randomGenerator);
				StageId stageId = materialsDataManager.createStage(materialsProducerId);
				if (randomGenerator.nextBoolean()) {
					expectedStageOffers.get(materialsProducerId).add(stageId);
					materialsDataManager.setStageOffer(stageId, true);
				}
			}

			for (TestMaterialsProducerId testMaterialsProducerId : TestMaterialsProducerId.values()) {
				Set<StageId> expectedStages = expectedStageOffers.get(testMaterialsProducerId);
				List<StageId> stages = materialsDataManager.getOfferedStages(testMaterialsProducerId);
				Set<StageId> actualStages = new LinkedHashSet<>(stages);
				assertEquals(stages.size(), actualStages.size());
				assertEquals(expectedStages, actualStages);
			}

			// precondition tests

			// if the materials producerId id is null
			assertThrows(RuntimeException.class, () -> materialsDataManager.getOfferedStages(null));

			// if the materials producerId id is unknown
			assertThrows(RuntimeException.class, () -> materialsDataManager.getOfferedStages(TestMaterialsProducerId.getUnknownMaterialsProducerId()));

		});

	}

	@Test
	@UnitTestMethod(name = "getStageBatches", args = { StageId.class })
	public void testGetStageBatches() {

		MaterialsActionSupport.testConsumer(605353680135796702L, (c) -> {
			MaterialsDataManager materialsDataManager = getMaterialsDataManager(c);

			StochasticsDataView stochasticsDataView = c.getDataView(StochasticsDataView.class).get();
			RandomGenerator randomGenerator = stochasticsDataView.getRandomGenerator();

			// create some stages and batches

			Map<StageId, Set<BatchId>> expectedStageBatches = new LinkedHashMap<>();

			for (int i = 0; i < 30; i++) {
				TestMaterialsProducerId materialsProducerId = TestMaterialsProducerId.getRandomMaterialsProducerId(randomGenerator);
				StageId stageId = materialsDataManager.createStage(materialsProducerId);
				int batchCount = randomGenerator.nextInt(3) + 1;
				Set<BatchId> batches = new LinkedHashSet<>();
				for (int j = 0; j < batchCount; j++) {
					BatchId batchId = materialsDataManager.createBatch(materialsProducerId, TestMaterialId.getRandomMaterialId(randomGenerator), randomGenerator.nextInt(100));
					batches.add(batchId);
					materialsDataManager.moveBatchToStage(batchId, stageId);
				}
				expectedStageBatches.put(stageId, batches);
			}
			Map<StageId, Set<BatchId>> actualStageBatches = new LinkedHashMap<>();
			for (MaterialsProducerId materialsProducerId : materialsDataManager.getMaterialsProducerIds()) {
				List<StageId> stageIds = materialsDataManager.getStages(materialsProducerId);
				for (StageId stageId : stageIds) {
					actualStageBatches.put(stageId, new LinkedHashSet<>(materialsDataManager.getStageBatches(stageId)));
				}
			}

			assertEquals(expectedStageBatches, actualStageBatches);

			// precondition tests

			// if the stage id is null
			assertThrows(RuntimeException.class, () -> materialsDataManager.getStageBatches(null));

			// if the stage id is unknown
			assertThrows(RuntimeException.class, () -> materialsDataManager.getStageBatches(new StageId(10000000)));
		});

	}

	@Test
	@UnitTestMethod(name = "getStageBatchesByMaterialId", args = { StageId.class, MaterialId.class })
	public void testGetStageBatchesByMaterialId() {

		MaterialsActionSupport.testConsumer(566215504762667367L, (c) -> {
			MaterialsDataManager materialsDataManager = getMaterialsDataManager(c);

			StochasticsDataView stochasticsDataView = c.getDataView(StochasticsDataView.class).get();
			RandomGenerator randomGenerator = stochasticsDataView.getRandomGenerator();

			// build a structure to hold the batches expected per stage and
			// material id
			Map<StageId, Map<MaterialId, Set<BatchId>>> expectedStageBatches = new LinkedHashMap<>();

			// create some stages and batches
			for (int i = 0; i < 30; i++) {
				TestMaterialsProducerId materialsProducerId = TestMaterialsProducerId.getRandomMaterialsProducerId(randomGenerator);
				StageId stageId = materialsDataManager.createStage(materialsProducerId);
				int batchCount = randomGenerator.nextInt(10) + 1;
				Map<MaterialId, Set<BatchId>> map = new LinkedHashMap<>();
				for (int j = 0; j < batchCount; j++) {
					TestMaterialId materialId = TestMaterialId.getRandomMaterialId(randomGenerator);
					Set<BatchId> batches = map.get(materialId);
					if (batches == null) {
						batches = new LinkedHashSet<>();
						map.put(materialId, batches);
					}
					BatchId batchId = materialsDataManager.createBatch(materialsProducerId, materialId, randomGenerator.nextInt(100));
					batches.add(batchId);
					materialsDataManager.moveBatchToStage(batchId, stageId);
				}
				expectedStageBatches.put(stageId, map);
			}

			// gather the actual stage batches in a structure identical to the
			// expected values
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

			// precondition tests

			// if the stage id is null
			assertThrows(RuntimeException.class, () -> materialsDataManager.getStageBatchesByMaterialId(null, TestMaterialId.MATERIAL_1));

			// if the stage id is unknown
			assertThrows(RuntimeException.class, () -> materialsDataManager.getStageBatchesByMaterialId(new StageId(10000000), TestMaterialId.MATERIAL_1));
		});
	}

	@Test
	@UnitTestMethod(name = "getStageProducer", args = { StageId.class })
	public void testGetStageProducer() {

		MaterialsActionSupport.testConsumer(3353371804539765061L, (c) -> {
			MaterialsDataManager materialsDataManager = getMaterialsDataManager(c);
			for (TestMaterialsProducerId testMaterialsProducerId : TestMaterialsProducerId.values()) {
				StageId stageId = materialsDataManager.createStage(testMaterialsProducerId);
				assertEquals(testMaterialsProducerId, materialsDataManager.getStageProducer(stageId));
			}

			// if the stage id is null
			assertThrows(RuntimeException.class, () -> materialsDataManager.getStageProducer(null));

			// if the stage id is unknown
			assertThrows(RuntimeException.class, () -> materialsDataManager.getStageProducer(new StageId(1000000000)));

		});
	}

	@Test
	@UnitTestMethod(name = "getStages", args = { MaterialsProducerId.class })
	public void testGetStages() {

		MaterialsActionSupport.testConsumer(6063607931844819363L, (c) -> {

			StochasticsDataView stochasticsDataView = c.getDataView(StochasticsDataView.class).get();
			RandomGenerator randomGenerator = stochasticsDataView.getRandomGenerator();

			Map<TestMaterialsProducerId, Set<StageId>> expectedStagesMap = new LinkedHashMap<>();
			MaterialsDataManager materialsDataManager = getMaterialsDataManager(c);
			for (TestMaterialsProducerId testMaterialsProducerId : TestMaterialsProducerId.values()) {
				Set<StageId> stageIds = new LinkedHashSet<>();
				expectedStagesMap.put(testMaterialsProducerId, stageIds);
				int count = randomGenerator.nextInt(10) + 1;
				for (int i = 0; i < count; i++) {
					StageId stageId = materialsDataManager.createStage(testMaterialsProducerId);
					stageIds.add(stageId);
				}
			}
			for (TestMaterialsProducerId testMaterialsProducerId : TestMaterialsProducerId.values()) {
				Set<StageId> expectedStageIds = expectedStagesMap.get(testMaterialsProducerId);

				List<StageId> stages = materialsDataManager.getStages(testMaterialsProducerId);
				Set<StageId> actualStageIds = new LinkedHashSet<>(stages);
				assertEquals(stages.size(), actualStageIds.size());
				assertEquals(expectedStageIds, actualStageIds);
			}

			// if the stage id is null
			assertThrows(RuntimeException.class, () -> materialsDataManager.getStages(null));

			// if the stage id is unknown
			assertThrows(RuntimeException.class, () -> materialsDataManager.getStages(TestMaterialsProducerId.getUnknownMaterialsProducerId()));

		});
	}

	@Test
	@UnitTestMethod(name = "incrementMaterialsProducerResourceLevel", args = { MaterialsProducerId.class, ResourceId.class, long.class })
	public void testIncrementMaterialsProducerResourceLevel() {

		ActionPlugin.Builder pluginBuilder = ActionPlugin.builder();
		pluginBuilder.addAgent("agent");

		pluginBuilder.addDataView(new LocalDataView());

		// create a structure to hold expected resource levels for producers
		Map<MultiKey, MutableLong> expectedLevelsMap = new LinkedHashMap<>();
		for (TestMaterialsProducerId testMaterialsProducerId : TestMaterialsProducerId.values()) {
			for (TestResourceId testResourceId : TestResourceId.values()) {
				expectedLevelsMap.put(new MultiKey(testMaterialsProducerId, testResourceId), new MutableLong());
			}
		}

		// initialize the materials data manager
		pluginBuilder.addAgentActionPlan("agent", new AgentActionPlan(0, (c) -> {
			LocalDataView localDataView = c.getDataView(LocalDataView.class).get();
			localDataView.materialsDataManager = getMaterialsDataManager(c);
		}));

		// determine a reasonable number of changes per time
		int resourceLevelChangeCount = TestMaterialsProducerId.size() * TestResourceId.size() / 10;

		// update several random resource levels values at various times
		for (int i = 0; i < 10; i++) {
			pluginBuilder.addAgentActionPlan("agent", new AgentActionPlan(0, (c) -> {
				StochasticsDataView stochasticsDataView = c.getDataView(StochasticsDataView.class).get();
				RandomGenerator randomGenerator = stochasticsDataView.getRandomGenerator();

				LocalDataView localDataView = c.getDataView(LocalDataView.class).get();
				MaterialsDataManager materialsDataManager = localDataView.materialsDataManager;

				for (int j = 0; j < resourceLevelChangeCount; j++) {
					TestMaterialsProducerId materialsProducerId = TestMaterialsProducerId.getRandomMaterialsProducerId(randomGenerator);
					TestResourceId testResourceId = TestResourceId.getRandomResourceId(randomGenerator);
					Long resourceLevel = (long) (randomGenerator.nextInt(100) + 1);
					materialsDataManager.incrementMaterialsProducerResourceLevel(materialsProducerId, testResourceId, resourceLevel);
					MultiKey multiKey = new MultiKey(materialsProducerId, testResourceId);
					MutableLong mutableLong = expectedLevelsMap.get(multiKey);
					mutableLong.increment(resourceLevel);
				}
			}));
		}

		pluginBuilder.addAgentActionPlan("agent", new AgentActionPlan(10, (c) -> {
			LocalDataView localDataView = c.getDataView(LocalDataView.class).get();
			MaterialsDataManager materialsDataManager = localDataView.materialsDataManager;
			for (TestMaterialsProducerId testMaterialsProducerId : TestMaterialsProducerId.values()) {
				for (TestResourceId testResourceId : TestResourceId.values()) {
					MultiKey multiKey = new MultiKey(testMaterialsProducerId, testResourceId);
					long expectedLevel = expectedLevelsMap.get(multiKey).getValue();
					long actualLevel = materialsDataManager.getMaterialsProducerResourceLevel(testMaterialsProducerId, testResourceId);
					assertEquals(expectedLevel, actualLevel);
				}
			}
		}));

		// precondition tests
		pluginBuilder.addAgentActionPlan("agent", new AgentActionPlan(11, (c) -> {
			LocalDataView localDataView = c.getDataView(LocalDataView.class).get();
			MaterialsDataManager materialsDataManager = localDataView.materialsDataManager;

			// if the materials producerId id is null
			assertThrows(RuntimeException.class, () -> materialsDataManager.incrementMaterialsProducerResourceLevel(null, TestResourceId.RESOURCE_1, 56L));

			// if the materials producerId id is unknown
			assertThrows(RuntimeException.class,
					() -> materialsDataManager.incrementMaterialsProducerResourceLevel(TestMaterialsProducerId.getUnknownMaterialsProducerId(), TestResourceId.RESOURCE_1, 56L));

			// if the resource id is null
			assertThrows(RuntimeException.class, () -> materialsDataManager.incrementMaterialsProducerResourceLevel(TestMaterialsProducerId.MATERIALS_PRODUCER_1, null, 56L));

			// if the resource id is unknown
			assertThrows(RuntimeException.class,
					() -> materialsDataManager.incrementMaterialsProducerResourceLevel(TestMaterialsProducerId.MATERIALS_PRODUCER_1, TestResourceId.getUnknownResourceId(), 56L));

			// if the amount will cause an overflow
			materialsDataManager.incrementMaterialsProducerResourceLevel(TestMaterialsProducerId.MATERIALS_PRODUCER_1, TestResourceId.RESOURCE_1, 1L);
			assertThrows(RuntimeException.class,
					() -> materialsDataManager.incrementMaterialsProducerResourceLevel(TestMaterialsProducerId.MATERIALS_PRODUCER_1, TestResourceId.RESOURCE_1, Long.MAX_VALUE));

		}));

		ActionPlugin actionPlugin = pluginBuilder.build();
		MaterialsActionSupport.testConsumers(1600752904847403602L, actionPlugin);
	}

	@Test
	@UnitTestConstructor(args = { ResolverContext.class, MaterialsInitialData.class })
	public void testConstructor() {

		ContractException contractException = assertThrows(ContractException.class, () -> new MaterialsDataManager(null));
		assertEquals(NucleusError.NULL_CONTEXT, contractException.getErrorType());
	}

	@Test
	@UnitTestMethod(name = "isStageOffered", args = { StageId.class })
	public void testIsStageOffered() {

		MaterialsActionSupport.testConsumer(5828712166763133131L, (c) -> {
			StochasticsDataView stochasticsDataView = c.getDataView(StochasticsDataView.class).get();
			RandomGenerator randomGenerator = stochasticsDataView.getRandomGenerator();
			MaterialsDataManager materialsDataManager = getMaterialsDataManager(c);

			for (int i = 0; i < 100; i++) {
				TestMaterialsProducerId materialsProducerId = TestMaterialsProducerId.getRandomMaterialsProducerId(randomGenerator);
				StageId stageId = materialsDataManager.createStage(materialsProducerId);
				assertFalse(materialsDataManager.isStageOffered(stageId));
				materialsDataManager.setStageOffer(stageId, true);
				assertTrue(materialsDataManager.isStageOffered(stageId));
			}

			// precondition tests

			// if the stage id is null
			assertThrows(RuntimeException.class, () -> materialsDataManager.isStageOffered(null));

			// if the stage id is unknown
			assertThrows(RuntimeException.class, () -> materialsDataManager.isStageOffered(new StageId(1000000)));

		});
	}

	@Test
	@UnitTestMethod(name = "materialsProducerIdExists", args = { MaterialsProducerId.class })
	public void testMaterialsProducerIdExists() {

		MaterialsActionSupport.testConsumer(6899430562368763998L, (c) -> {
			MaterialsDataManager materialsDataManager = getMaterialsDataManager(c);

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

		MaterialsActionSupport.testConsumer(3928420962968756679L, (c) -> {
			MaterialsDataManager materialsDataManager = getMaterialsDataManager(c);

			for (TestMaterialsProducerPropertyId testMaterialsProducerPropertyId : TestMaterialsProducerPropertyId.values()) {
				assertTrue(materialsDataManager.materialsProducerPropertyIdExists(testMaterialsProducerPropertyId));
			}
			assertFalse(materialsDataManager.materialsProducerPropertyIdExists(TestMaterialsProducerPropertyId.getUnknownMaterialsProducerPropertyId()));
			assertFalse(materialsDataManager.materialsProducerPropertyIdExists(null));
		});
	}

	@Test
	@UnitTestMethod(name = "moveBatchToInventory", args = { BatchId.class })
	public void testMoveBatchToInventory() {

		MaterialsActionSupport.testConsumer(5365422390939417105L, (c) -> {
			MaterialsDataManager materialsDataManager = getMaterialsDataManager(c);

			StochasticsDataView stochasticsDataView = c.getDataView(StochasticsDataView.class).get();
			RandomGenerator randomGenerator = stochasticsDataView.getRandomGenerator();

			// create a stage and put batches on and off of it

			for (TestMaterialsProducerId testMaterialsProducerId : TestMaterialsProducerId.values()) {
				StageId stageId = materialsDataManager.createStage(testMaterialsProducerId);
				for (int j = 0; j < 3; j++) {
					BatchId batchId = materialsDataManager.createBatch(testMaterialsProducerId, TestMaterialId.getRandomMaterialId(randomGenerator), randomGenerator.nextInt(100));
					assertTrue(materialsDataManager.getInventoryBatches(testMaterialsProducerId).contains(batchId));
					materialsDataManager.moveBatchToStage(batchId, stageId);
					assertFalse(materialsDataManager.getInventoryBatches(testMaterialsProducerId).contains(batchId));
					materialsDataManager.moveBatchToInventory(batchId);
					assertTrue(materialsDataManager.getInventoryBatches(testMaterialsProducerId).contains(batchId));
				}

			}

			// precondition tests

			// if the batch id is null
			assertThrows(RuntimeException.class, () -> materialsDataManager.moveBatchToInventory(null));

			// if the batch id is unknown
			assertThrows(RuntimeException.class, () -> materialsDataManager.moveBatchToInventory(new BatchId(10000000)));

			// if the batch is not associated with a stage
			BatchId batchId = materialsDataManager.createBatch(TestMaterialsProducerId.MATERIALS_PRODUCER_1, TestMaterialId.MATERIAL_2, 300.0);
			assertThrows(RuntimeException.class, () -> materialsDataManager.moveBatchToInventory(batchId));

			// if the associated stage is being offered
			StageId stageId = materialsDataManager.createStage(TestMaterialsProducerId.MATERIALS_PRODUCER_1);
			materialsDataManager.moveBatchToStage(batchId, stageId);
			materialsDataManager.setStageOffer(stageId, true);
			assertThrows(RuntimeException.class, () -> materialsDataManager.moveBatchToInventory(batchId));

		});
	}

	@Test
	@UnitTestMethod(name = "moveBatchToStage", args = { BatchId.class, StageId.class })
	public void testMoveBatchToStage() {

		MaterialsActionSupport.testConsumer(7520513202615429081L, (c) -> {
			MaterialsDataManager materialsDataManager = getMaterialsDataManager(c);

			StochasticsDataView stochasticsDataView = c.getDataView(StochasticsDataView.class).get();
			RandomGenerator randomGenerator = stochasticsDataView.getRandomGenerator();

			// create a stage and put batches on and off of it

			for (TestMaterialsProducerId testMaterialsProducerId : TestMaterialsProducerId.values()) {
				StageId stageId = materialsDataManager.createStage(testMaterialsProducerId);
				for (int j = 0; j < 3; j++) {
					BatchId batchId = materialsDataManager.createBatch(testMaterialsProducerId, TestMaterialId.getRandomMaterialId(randomGenerator), randomGenerator.nextInt(100));
					assertTrue(materialsDataManager.getInventoryBatches(testMaterialsProducerId).contains(batchId));
					materialsDataManager.moveBatchToStage(batchId, stageId);
					assertEquals(stageId, materialsDataManager.getBatchStageId(batchId).get());
				}

			}

			// precondition tests
			StageId stageId = materialsDataManager.createStage(TestMaterialsProducerId.MATERIALS_PRODUCER_3);

			// if the batch id is null
			assertThrows(RuntimeException.class, () -> materialsDataManager.moveBatchToStage(null, stageId));

			// if the batch id is unknown
			assertThrows(RuntimeException.class, () -> materialsDataManager.moveBatchToStage(new BatchId(10000000), stageId));

			// if the stage id is null
			BatchId batchId = materialsDataManager.createBatch(TestMaterialsProducerId.MATERIALS_PRODUCER_1, TestMaterialId.MATERIAL_2, 300.0);
			assertThrows(RuntimeException.class, () -> materialsDataManager.moveBatchToStage(batchId, null));

			// if the stage id is unknown
			assertThrows(RuntimeException.class, () -> materialsDataManager.moveBatchToStage(batchId, new StageId(3423423)));

			// if the batch id is already associated with a stage
			materialsDataManager.moveBatchToStage(batchId, stageId);
			assertThrows(RuntimeException.class, () -> materialsDataManager.moveBatchToStage(batchId, stageId));

		});
	}

	@Test
	@UnitTestMethod(name = "setBatchPropertyValue", args = { BatchId.class, BatchPropertyId.class, Object.class })
	public void testSetBatchPropertyValue() {

		MaterialsActionSupport.testConsumer(7428318539552852631L, (c) -> {
			/*
			 * Have the agent add 50 randomized batches and record the values
			 * for all properties
			 */

			MaterialsDataManager materialsDataManager = getMaterialsDataManager(c);
			StochasticsDataView stochasticsDataView = c.getDataView(StochasticsDataView.class).get();
			RandomGenerator randomGenerator = stochasticsDataView.getRandomGenerator();

			// create a few batches
			for (int i = 0; i < 50; i++) {
				MaterialsProducerId materialsProducerId = TestMaterialsProducerId.getRandomMaterialsProducerId(randomGenerator);
				MaterialId materialId = TestMaterialId.getRandomMaterialId(randomGenerator);
				double amount = randomGenerator.nextDouble();
				materialsDataManager.createBatch(materialsProducerId, materialId, amount);
			}

			// alter randomly chosen batch properties

			for (int i = 0; i < 200; i++) {
				MaterialsProducerId materialsProducerId = TestMaterialsProducerId.getRandomMaterialsProducerId(randomGenerator);
				List<BatchId> inventoryBatches = materialsDataManager.getInventoryBatches(materialsProducerId);
				BatchId batchId = inventoryBatches.get(randomGenerator.nextInt(inventoryBatches.size()));
				MaterialId materialId = materialsDataManager.getBatchMaterial(batchId);
				List<TestBatchPropertyId> batchPropertyIds = new ArrayList<>(materialsDataManager.getBatchPropertyIds(materialId));
				TestBatchPropertyId batchPropertyId = batchPropertyIds.get(randomGenerator.nextInt(batchPropertyIds.size()));
				Object expectedValue = batchPropertyId.getRandomPropertyValue(randomGenerator);
				materialsDataManager.setBatchPropertyValue(batchId, batchPropertyId, expectedValue);
				Object actualValue = materialsDataManager.getBatchPropertyValue(batchId, batchPropertyId);
				assertEquals(expectedValue, actualValue);
			}

			BatchPropertyId batchPropertyId = TestBatchPropertyId.BATCH_PROPERTY_1_1_BOOLEAN_IMMUTABLE_NO_TRACK;

			// if the batch id is null
			assertThrows(RuntimeException.class, () -> materialsDataManager.setBatchPropertyValue(null, batchPropertyId, false));

			// if the batch id is unknown
			assertThrows(RuntimeException.class, () -> materialsDataManager.setBatchPropertyValue(new BatchId(100000), batchPropertyId, false));
		});
	}

	@Test
	@UnitTestMethod(name = "setMaterialsProducerPropertyValue", args = { MaterialsProducerId.class, MaterialsProducerPropertyId.class, Object.class })
	public void testSetMaterialsProducerPropertyValue() {

		ActionPlugin.Builder pluginBuilder = ActionPlugin.builder();
		pluginBuilder.addAgent("agent");

		pluginBuilder.addDataView(new LocalDataView());

		// create a structure to hold expected assignment times for materials
		// producer property values
		Map<MultiKey, Object> expectedValuesMap = new LinkedHashMap<>();

		// initialize the materials data manager
		pluginBuilder.addAgentActionPlan("agent", new AgentActionPlan(0, (c) -> {
			LocalDataView localDataView = c.getDataView(LocalDataView.class).get();
			localDataView.materialsDataManager = getMaterialsDataManager(c);

			for (TestMaterialsProducerId testMaterialsProducerId : TestMaterialsProducerId.values()) {
				for (TestMaterialsProducerPropertyId testMaterialsProducerPropertyId : TestMaterialsProducerPropertyId.values()) {
					MultiKey multiKey = new MultiKey(testMaterialsProducerId, testMaterialsProducerPropertyId);
					Object value = localDataView.materialsDataManager.getMaterialsProducerPropertyValue(testMaterialsProducerId, testMaterialsProducerPropertyId);
					expectedValuesMap.put(multiKey, value);
				}
			}

		}));

		// determine a reasonable number of changes per time
		int propertyChangeCount = TestMaterialsProducerId.size() * TestMaterialsProducerPropertyId.size() / 10;

		// update several random property values at various times
		for (int i = 0; i < 10; i++) {
			pluginBuilder.addAgentActionPlan("agent", new AgentActionPlan(0, (c) -> {
				StochasticsDataView stochasticsDataView = c.getDataView(StochasticsDataView.class).get();
				RandomGenerator randomGenerator = stochasticsDataView.getRandomGenerator();

				LocalDataView localDataView = c.getDataView(LocalDataView.class).get();
				MaterialsDataManager materialsDataManager = localDataView.materialsDataManager;

				for (int j = 0; j < propertyChangeCount; j++) {
					TestMaterialsProducerId materialsProducerId = TestMaterialsProducerId.getRandomMaterialsProducerId(randomGenerator);
					TestMaterialsProducerPropertyId materialsProducerPropertyId = TestMaterialsProducerPropertyId.getRandomMaterialsProducerPropertyId(randomGenerator);
					Object propertyValue = materialsProducerPropertyId.getRandomPropertyValue(randomGenerator);
					materialsDataManager.setMaterialsProducerPropertyValue(materialsProducerId, materialsProducerPropertyId, propertyValue);
					MultiKey multiKey = new MultiKey(materialsProducerId, materialsProducerPropertyId);
					expectedValuesMap.put(multiKey, propertyValue);
				}
			}));
		}

		pluginBuilder.addAgentActionPlan("agent", new AgentActionPlan(10, (c) -> {
			LocalDataView localDataView = c.getDataView(LocalDataView.class).get();
			MaterialsDataManager materialsDataManager = localDataView.materialsDataManager;
			for (TestMaterialsProducerId testMaterialsProducerId : TestMaterialsProducerId.values()) {
				for (TestMaterialsProducerPropertyId testMaterialsProducerPropertyId : TestMaterialsProducerPropertyId.values()) {
					MultiKey multiKey = new MultiKey(testMaterialsProducerId, testMaterialsProducerPropertyId);
					Object expectedValue = expectedValuesMap.get(multiKey);
					Object actualValue = materialsDataManager.getMaterialsProducerPropertyValue(testMaterialsProducerId, testMaterialsProducerPropertyId);
					assertEquals(expectedValue, actualValue);
				}
			}
		}));

		// precondition tests
		pluginBuilder.addAgentActionPlan("agent", new AgentActionPlan(11, (c) -> {
			LocalDataView localDataView = c.getDataView(LocalDataView.class).get();
			MaterialsDataManager materialsDataManager = localDataView.materialsDataManager;

			// if the materials producerId id is null
			assertThrows(RuntimeException.class,
					() -> materialsDataManager.setMaterialsProducerPropertyValue(null, TestMaterialsProducerPropertyId.MATERIALS_PRODUCER_PROPERTY_4_BOOLEAN_MUTABLE_TRACK, true));

			// if the materials producerId id is unknown
			assertThrows(RuntimeException.class, () -> materialsDataManager.setMaterialsProducerPropertyValue(TestMaterialsProducerId.getUnknownMaterialsProducerId(),
					TestMaterialsProducerPropertyId.MATERIALS_PRODUCER_PROPERTY_4_BOOLEAN_MUTABLE_TRACK, true));

			// if the materials producerId property id is null
			assertThrows(RuntimeException.class, () -> materialsDataManager.setMaterialsProducerPropertyValue(TestMaterialsProducerId.MATERIALS_PRODUCER_1, null, false));

			// if the materials producerId property id is unknown
			assertThrows(RuntimeException.class, () -> materialsDataManager.setMaterialsProducerPropertyValue(TestMaterialsProducerId.MATERIALS_PRODUCER_1,
					TestMaterialsProducerPropertyId.getUnknownMaterialsProducerPropertyId(), false));

		}));

		ActionPlugin actionPlugin = pluginBuilder.build();
		MaterialsActionSupport.testConsumers(153532399992465971L, actionPlugin);
	}

	@Test
	@UnitTestMethod(name = "setStageOffer", args = { StageId.class, boolean.class })
	public void testSetStageOffer() {

		MaterialsActionSupport.testConsumer(5098593357153704158L, (c) -> {
			StochasticsDataView stochasticsDataView = c.getDataView(StochasticsDataView.class).get();
			RandomGenerator randomGenerator = stochasticsDataView.getRandomGenerator();
			MaterialsDataManager materialsDataManager = getMaterialsDataManager(c);

			for (int i = 0; i < 100; i++) {
				TestMaterialsProducerId materialsProducerId = TestMaterialsProducerId.getRandomMaterialsProducerId(randomGenerator);
				StageId stageId = materialsDataManager.createStage(materialsProducerId);
				assertFalse(materialsDataManager.isStageOffered(stageId));
				materialsDataManager.setStageOffer(stageId, true);
				assertTrue(materialsDataManager.isStageOffered(stageId));
				materialsDataManager.setStageOffer(stageId, false);
				assertFalse(materialsDataManager.isStageOffered(stageId));
			}

			// precondition tests

			// if the stage id is null
			assertThrows(RuntimeException.class, () -> materialsDataManager.setStageOffer(null, true));

			// if the stage id is unknown
			assertThrows(RuntimeException.class, () -> materialsDataManager.setStageOffer(new StageId(1000000), true));

		});
	}

	@Test
	@UnitTestMethod(name = "shiftBatchContent", args = { BatchId.class, BatchId.class, double.class })
	public void testShiftBatchContent() {

		MaterialsActionSupport.testConsumer(5471599806591291876L, (c) -> {
			StochasticsDataView stochasticsDataView = c.getDataView(StochasticsDataView.class).get();
			RandomGenerator randomGenerator = stochasticsDataView.getRandomGenerator();
			MaterialsDataManager materialsDataManager = getMaterialsDataManager(c);

			for (int i = 0; i < 100; i++) {
				TestMaterialsProducerId materialsProducerId = TestMaterialsProducerId.getRandomMaterialsProducerId(randomGenerator);
				TestMaterialId testMaterialId = TestMaterialId.getRandomMaterialId(randomGenerator);
				double amount = randomGenerator.nextInt(10) + 1;
				BatchId batchId1 = materialsDataManager.createBatch(materialsProducerId, testMaterialId, amount);
				BatchId batchId2 = materialsDataManager.createBatch(materialsProducerId, testMaterialId, amount);
				double transferAmount = randomGenerator.nextDouble() * amount;
				materialsDataManager.shiftBatchContent(batchId1, batchId2, transferAmount);
				double batchAmount = materialsDataManager.getBatchAmount(batchId1);
				assertEquals(batchAmount, amount - transferAmount);
				batchAmount = materialsDataManager.getBatchAmount(batchId2);
				assertEquals(batchAmount, amount + transferAmount);
			}

			// precondition tests
			BatchId batchId1 = materialsDataManager.createBatch(TestMaterialsProducerId.MATERIALS_PRODUCER_1, TestMaterialId.MATERIAL_2, 100.0);
			BatchId batchId2 = materialsDataManager.createBatch(TestMaterialsProducerId.MATERIALS_PRODUCER_1, TestMaterialId.MATERIAL_2, 200.0);

			// if the source batch id is null
			assertThrows(RuntimeException.class, () -> materialsDataManager.shiftBatchContent(null, batchId2, 50));

			// if the source batch id is unknown
			assertThrows(RuntimeException.class, () -> materialsDataManager.shiftBatchContent(new BatchId(10000), batchId2, 50));

			// if the destination batch id is null
			assertThrows(RuntimeException.class, () -> materialsDataManager.shiftBatchContent(batchId1, null, 50));

			// if the destination batch id is unknown
			assertThrows(RuntimeException.class, () -> materialsDataManager.shiftBatchContent(batchId1, new BatchId(10000), 50));

		});
	}

	@Test
	@UnitTestMethod(name = "stageExists", args = { StageId.class })
	public void testStageExists() {

		MaterialsActionSupport.testConsumer(35431003820781165L, (c) -> {
			StochasticsDataView stochasticsDataView = c.getDataView(StochasticsDataView.class).get();
			RandomGenerator randomGenerator = stochasticsDataView.getRandomGenerator();
			MaterialsDataManager materialsDataManager = getMaterialsDataManager(c);

			for (int i = 0; i < 10; i++) {
				TestMaterialsProducerId materialsProducerId = TestMaterialsProducerId.getRandomMaterialsProducerId(randomGenerator);
				StageId stageId = materialsDataManager.createStage(materialsProducerId);
				assertTrue(materialsDataManager.stageExists(stageId));
			}

			assertFalse(materialsDataManager.stageExists(null));

			assertFalse(materialsDataManager.stageExists(new StageId(123)));
		});
	}

	@Test
	@UnitTestMethod(name = "transferOfferedStageToMaterialsProducer", args = { MaterialsProducerId.class, StageId.class })
	public void testTransferOfferedStageToMaterialsProducer() {

		MaterialsActionSupport.testConsumer(7401116571668245131L, (c) -> {
			StochasticsDataView stochasticsDataView = c.getDataView(StochasticsDataView.class).get();
			RandomGenerator randomGenerator = stochasticsDataView.getRandomGenerator();
			MaterialsDataManager materialsDataManager = getMaterialsDataManager(c);

			for (int i = 0; i < 10; i++) {

				TestMaterialsProducerId materialsProducerId1 = TestMaterialsProducerId.getRandomMaterialsProducerId(randomGenerator);
				StageId stageId = materialsDataManager.createStage(materialsProducerId1);
				materialsDataManager.setStageOffer(stageId, true);
				assertEquals(materialsProducerId1, materialsDataManager.getStageProducer(stageId));

				TestMaterialsProducerId materialsProducerId2 = TestMaterialsProducerId.getRandomMaterialsProducerId(randomGenerator);
				materialsDataManager.transferOfferedStageToMaterialsProducer(materialsProducerId2, stageId);
				assertEquals(materialsProducerId2, materialsDataManager.getStageProducer(stageId));

			}

			TestMaterialsProducerId materialsProducerId1 = TestMaterialsProducerId.MATERIALS_PRODUCER_1;
			StageId stageId = materialsDataManager.createStage(materialsProducerId1);
			materialsDataManager.setStageOffer(stageId, true);
			TestMaterialsProducerId materialsProducerId2 = TestMaterialsProducerId.MATERIALS_PRODUCER_2;

			// if the materials producer id is null
			assertThrows(RuntimeException.class, () -> materialsDataManager.transferOfferedStageToMaterialsProducer(null, stageId));

			// if the materials producer id is unknown
			assertThrows(RuntimeException.class, () -> materialsDataManager.transferOfferedStageToMaterialsProducer(TestMaterialsProducerId.getUnknownMaterialsProducerId(), stageId));

			// if the stage id is null
			assertThrows(RuntimeException.class, () -> materialsDataManager.transferOfferedStageToMaterialsProducer(materialsProducerId2, null));

			// if the stage id is unknown
			assertThrows(RuntimeException.class, () -> materialsDataManager.transferOfferedStageToMaterialsProducer(materialsProducerId2, new StageId(100000)));

		});
	}

	@Test
	@UnitTestMethod(name = "addMaterialId", args = { MaterialId.class })
	public void testAddMaterialId() {

		MaterialsActionSupport.testConsumer(7355262130645485205L, (c) -> {
			MaterialsDataManager materialsDataManager = getMaterialsDataManager(c);

			MaterialId materialId_A = new MaterialId() {
			};
			assertFalse(materialsDataManager.materialIdExists(materialId_A));

			materialsDataManager.addMaterialId(materialId_A);
			assertTrue(materialsDataManager.materialIdExists(materialId_A));

			// preconditions

			// if the material id is null
			assertThrows(RuntimeException.class, () -> materialsDataManager.addMaterialId(materialId_A));

			// if the material id was previously added
			assertThrows(RuntimeException.class, () -> materialsDataManager.addMaterialId(materialId_A));

		});
	}

	@Test
	@UnitTestMethod(name = "defineBatchProperty", args = { MaterialId.class, BatchPropertyId.class, PropertyDefinition.class })
	public void testDefineBatchProperty() {

		MaterialsActionSupport.testConsumer(8363926013482798433L, (c) -> {
			MaterialsDataManager materialsDataManager = getMaterialsDataManager(c);

			MaterialId materialId = TestMaterialId.MATERIAL_1;
			BatchPropertyId batchPropertyId = new BatchPropertyId() {
			};
			PropertyDefinition propertyDefinition = PropertyDefinition.builder().setDefaultValue(12).setType(Integer.class).build();

			materialsDataManager.defineBatchProperty(materialId, batchPropertyId, propertyDefinition);
			PropertyDefinition actualPropertyDefinition = materialsDataManager.getBatchPropertyDefinition(materialId, batchPropertyId);
			assertEquals(propertyDefinition, actualPropertyDefinition);

			// preconditions

			BatchPropertyId batchPropertyId2 = new BatchPropertyId() {
			};

			// if the material id is null
			ContractException contractException = assertThrows(ContractException.class, () -> materialsDataManager.defineBatchProperty(null, batchPropertyId2, propertyDefinition));
			assertEquals(MaterialsError.NULL_MATERIAL_ID, contractException.getErrorType());

			// if the material id is unknown
			contractException = assertThrows(ContractException.class, () -> materialsDataManager.defineBatchProperty(TestMaterialId.getUnknownMaterialId(), batchPropertyId2, propertyDefinition));
			assertEquals(MaterialsError.UNKNOWN_MATERIAL_ID, contractException.getErrorType());

			// if the batch property id is null
			contractException = assertThrows(ContractException.class, () -> materialsDataManager.defineBatchProperty(materialId, null, propertyDefinition));
			assertEquals(MaterialsError.NULL_BATCH_PROPERTY_ID, contractException.getErrorType());

			// if the batch property was previously defined
			contractException = assertThrows(ContractException.class, () -> materialsDataManager.defineBatchProperty(materialId, batchPropertyId, propertyDefinition));
			assertEquals(MaterialsError.DUPLICATE_BATCH_PROPERTY_DEFINITION, contractException.getErrorType());

			// if the property definition is null
			contractException = assertThrows(ContractException.class, () -> materialsDataManager.defineBatchProperty(materialId, batchPropertyId2, null));
			assertEquals(PropertyError.NULL_PROPERTY_DEFINITION, contractException.getErrorType());

			// if the property definition does not contain a default value
			contractException = assertThrows(ContractException.class,
					() -> materialsDataManager.defineBatchProperty(materialId, batchPropertyId2, PropertyDefinition.builder().setType(Integer.class).build()));
			assertEquals(PropertyError.PROPERTY_DEFINITION_MISSING_DEFAULT, contractException.getErrorType());

		});
	}

	@Test
	@UnitTestMethod(name = "addResource", args = { ResourceId.class })
	public void testAddResource() {

		MaterialsActionSupport.testConsumer(8894968453002205375L, (c) -> {

			MaterialsDataManager materialsDataManager = new MaterialsDataManager(c);
			ResourceId resourceId = new ResourceId() {
			};
			assertFalse(materialsDataManager.getResourceIds().contains(resourceId));
			materialsDataManager.addResource(resourceId);
			assertTrue(materialsDataManager.getResourceIds().contains(resourceId));

			// preconditions

			// if the resource id is null
			ContractException contractException = assertThrows(ContractException.class, () -> materialsDataManager.addResource(null));
			assertEquals(ResourceError.NULL_RESOURCE_ID, contractException.getErrorType());

			// if the resource was previously added
			contractException = assertThrows(ContractException.class, () -> materialsDataManager.addResource(resourceId));
			assertEquals(ResourceError.DUPLICATE_RESOURCE_ID, contractException.getErrorType());

			materialsDataManager.addMaterialsProducerId(TestMaterialsProducerId.MATERIALS_PRODUCER_1);
			ResourceId resourceId2 = new ResourceId() {
			};
			contractException = assertThrows(ContractException.class, () -> materialsDataManager.addResource(resourceId2));
			assertEquals(MaterialsError.RESOURCE_LOADING_ORDER, contractException.getErrorType());

		});
	}

	@Test
	@UnitTestMethod(name = "addMaterialsProducerId", args = { MaterialsProducerId.class })
	public void testAddMaterialsProducerId() {

		MaterialsActionSupport.testConsumer(6421950733132031037L, (c) -> {

			MaterialsDataManager materialsDataManager = new MaterialsDataManager(c);
			assertFalse(materialsDataManager.getMaterialsProducerIds().contains(TestMaterialsProducerId.MATERIALS_PRODUCER_1));
			materialsDataManager.addMaterialsProducerId(TestMaterialsProducerId.MATERIALS_PRODUCER_1);
			assertTrue(materialsDataManager.getMaterialsProducerIds().contains(TestMaterialsProducerId.MATERIALS_PRODUCER_1));

			// preconditions

			// if the materials producer id is null
			ContractException contractException = assertThrows(ContractException.class, () -> materialsDataManager.addMaterialsProducerId(null));
			assertEquals(MaterialsError.NULL_MATERIALS_PRODUCER_ID, contractException.getErrorType());

			// if the materials producer id was previously added
			contractException = assertThrows(ContractException.class, () -> materialsDataManager.addMaterialsProducerId(TestMaterialsProducerId.MATERIALS_PRODUCER_1));
			assertEquals(MaterialsError.DUPLICATE_MATERIALS_PRODUCER_ID, contractException.getErrorType());

			// if materials producer properties have already been added
			TestMaterialsProducerPropertyId materialsProducerPropertyId = TestMaterialsProducerPropertyId.MATERIALS_PRODUCER_PROPERTY_1_BOOLEAN_MUTABLE_NO_TRACK;
			PropertyDefinition propertyDefinition = materialsProducerPropertyId.getPropertyDefinition();
			materialsDataManager.defineMaterialsProducerProperty(materialsProducerPropertyId, propertyDefinition);

			contractException = assertThrows(ContractException.class, () -> materialsDataManager.addMaterialsProducerId(TestMaterialsProducerId.MATERIALS_PRODUCER_2));
			assertEquals(MaterialsError.MATERIALS_PRODUCER_PROPERTY_LOADING_ORDER, contractException.getErrorType());

		});
	}

	@Test
	@UnitTestMethod(name = "defineMaterialsProducerProperty", args = { MaterialsProducerPropertyId.class, PropertyDefinition.class })
	public void testDefineMaterialsProducerProperty() {

		MaterialsActionSupport.testConsumer(4064181446527683678L, (c) -> {

			MaterialsDataManager materialsDataManager = new MaterialsDataManager(c);

			TestMaterialsProducerPropertyId materialsProducerPropertyId = TestMaterialsProducerPropertyId.MATERIALS_PRODUCER_PROPERTY_1_BOOLEAN_MUTABLE_NO_TRACK;
			PropertyDefinition propertyDefinition = materialsProducerPropertyId.getPropertyDefinition();
			materialsDataManager.defineMaterialsProducerProperty(materialsProducerPropertyId, propertyDefinition);

			PropertyDefinition actualPropertyDefinition = materialsDataManager.getMaterialsProducerPropertyDefinition(materialsProducerPropertyId);
			assertEquals(propertyDefinition, actualPropertyDefinition);

			// preconditions

			// if the materials producer property id is null
			ContractException contractException = assertThrows(ContractException.class, () -> materialsDataManager.defineMaterialsProducerProperty(null, propertyDefinition));
			assertEquals(MaterialsError.NULL_MATERIALS_PRODUCER_PROPERTY_ID, contractException.getErrorType());

			// if the materials producer property was previously defined
			contractException = assertThrows(ContractException.class, () -> materialsDataManager.defineMaterialsProducerProperty(materialsProducerPropertyId, propertyDefinition));
			assertEquals(MaterialsError.DUPLICATE_MATERIALS_PRODUCER_PROPERTY_DEFINITION, contractException.getErrorType());

			// if the property definition is null
			contractException = assertThrows(ContractException.class,
					() -> materialsDataManager.defineMaterialsProducerProperty(TestMaterialsProducerPropertyId.MATERIALS_PRODUCER_PROPERTY_4_BOOLEAN_MUTABLE_TRACK, null));
			assertEquals(PropertyError.NULL_PROPERTY_DEFINITION, contractException.getErrorType());

		});
	}

	@Test
	@UnitTestMethod(name = "getResourceIds", args = {})
	public void testGetResourceIds() {
		// 8031618291497673910L
		MaterialsActionSupport.testConsumer(8031618291497673910L, (c) -> {

			MaterialsDataManager materialsDataManager = getMaterialsDataManager(c);
			assertEquals(EnumSet.allOf(TestResourceId.class), materialsDataManager.getResourceIds());

		});
	}

}
