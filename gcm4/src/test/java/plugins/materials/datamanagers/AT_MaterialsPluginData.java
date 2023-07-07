package plugins.materials.datamanagers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.math3.random.RandomGenerator;
import org.junit.jupiter.api.Test;

import nucleus.PluginData;
import nucleus.PluginDataBuilder;
import plugins.materials.datamangers.MaterialsPluginData;
import plugins.materials.support.BatchId;
import plugins.materials.support.BatchPropertyId;
import plugins.materials.support.MaterialId;
import plugins.materials.support.MaterialsError;
import plugins.materials.support.MaterialsProducerId;
import plugins.materials.support.MaterialsProducerPropertyId;
import plugins.materials.support.StageId;
import plugins.materials.testsupport.TestBatchPropertyId;
import plugins.materials.testsupport.TestMaterialId;
import plugins.materials.testsupport.TestMaterialsProducerId;
import plugins.materials.testsupport.TestMaterialsProducerPropertyId;
import plugins.resources.support.ResourceError;
import plugins.resources.support.ResourceId;
import plugins.resources.testsupport.TestResourceId;
import plugins.util.properties.PropertyDefinition;
import plugins.util.properties.PropertyError;
import util.annotations.UnitTestMethod;
import util.errors.ContractException;
import util.random.RandomGeneratorProvider;
import util.wrappers.MultiKey;

public class AT_MaterialsPluginData {

	@Test
	@UnitTestMethod(target = MaterialsPluginData.class, name = "builder", args = {})
	public void testBuilder() {
		assertNotNull(MaterialsPluginData.builder());
	}

	@Test
	@UnitTestMethod(target = MaterialsPluginData.Builder.class, name = "build", args = {})
	public void testBuild() {

		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(8543723876953755503L);

		MaterialsPluginData materialsInitialData = MaterialsPluginData.builder().build();

		assertNotNull(materialsInitialData);

		assertTrue(materialsInitialData.getBatchIds().isEmpty());
		assertTrue(materialsInitialData.getMaterialIds().isEmpty());
		assertTrue(materialsInitialData.getResourceIds().isEmpty());
		assertTrue(materialsInitialData.getStageIds().isEmpty());

		/*
		 * precondition test: if a batch property is associated with a material id that
		 * was not properly added
		 */
		ContractException contractException = assertThrows(ContractException.class, () -> {
			MaterialId materialId = TestMaterialId.MATERIAL_1;
			TestBatchPropertyId propertyId = TestBatchPropertyId.BATCH_PROPERTY_1_1_BOOLEAN_IMMUTABLE_NO_TRACK;
			PropertyDefinition propertyDefinition = propertyId.getPropertyDefinition();
			MaterialsPluginData.builder()//
					.defineBatchProperty(materialId, propertyId, propertyDefinition)//
					.build();//
		});
		assertEquals(MaterialsError.UNKNOWN_MATERIAL_ID, contractException.getErrorType());

		/*
		 * precondition test: if a batch is added without assigned property values for
		 * each property definition that lacks a default value
		 */
		contractException = assertThrows(ContractException.class, () -> {
			MaterialsProducerId materialsProducerId = TestMaterialsProducerId.MATERIALS_PRODUCER_1;
			MaterialId materialId = TestMaterialId.MATERIAL_1;
			TestBatchPropertyId propertyId = TestBatchPropertyId.BATCH_PROPERTY_1_1_BOOLEAN_IMMUTABLE_NO_TRACK;
			PropertyDefinition propertyDefinition = PropertyDefinition.builder()//
					.setType(Boolean.class)//
					.setPropertyValueMutability(false)//
					.build();//

			MaterialsPluginData.builder()//
					.addMaterial(materialId)//
					.addMaterialsProducerId(materialsProducerId)//
					.addBatch(new BatchId(12), materialId, 12.3)//
					.addBatchToMaterialsProducerInventory(new BatchId(12), materialsProducerId)//
					.defineBatchProperty(materialId, propertyId, propertyDefinition).build();//
		});
		assertEquals(PropertyError.INSUFFICIENT_PROPERTY_VALUE_ASSIGNMENT, contractException.getErrorType());

		/*
		 * precondition test: if a materials property value is associated with a
		 * materials producer id that was not properly added
		 */
		contractException = assertThrows(ContractException.class, () -> {
			MaterialsProducerId materialsProducerId = TestMaterialsProducerId.MATERIALS_PRODUCER_1;
			TestMaterialsProducerPropertyId propertyId = TestMaterialsProducerPropertyId.MATERIALS_PRODUCER_PROPERTY_1_BOOLEAN_MUTABLE_NO_TRACK;
			PropertyDefinition propertyDefinition = propertyId.getPropertyDefinition();//
			Object value = propertyId.getRandomPropertyValue(randomGenerator);

			MaterialsPluginData.builder()//
					.defineMaterialsProducerProperty(propertyId, propertyDefinition)//
					.setMaterialsProducerPropertyValue(materialsProducerId, propertyId, value)//
					.build();//
		});
		assertEquals(MaterialsError.UNKNOWN_MATERIALS_PRODUCER_ID, contractException.getErrorType());

		/*
		 * precondition test: if a materials property value is associated with a
		 * materials producer property id that was not properly defined
		 */
		contractException = assertThrows(ContractException.class, () -> {
			MaterialsProducerId materialsProducerId = TestMaterialsProducerId.MATERIALS_PRODUCER_1;
			TestMaterialsProducerPropertyId propertyId = TestMaterialsProducerPropertyId.MATERIALS_PRODUCER_PROPERTY_1_BOOLEAN_MUTABLE_NO_TRACK;
			Object value = propertyId.getRandomPropertyValue(randomGenerator);

			MaterialsPluginData.builder()//
					.addMaterialsProducerId(materialsProducerId)//
					.setMaterialsProducerPropertyValue(materialsProducerId, propertyId, value)//
					.build();//
		});
		assertEquals(PropertyError.UNKNOWN_PROPERTY_ID, contractException.getErrorType());

		/*
		 * precondition test: if a materials property value is associated with a value
		 * that is not compatible with the corresponding property definition
		 * 
		 */
		contractException = assertThrows(ContractException.class, () -> {
			MaterialsProducerId materialsProducerId = TestMaterialsProducerId.MATERIALS_PRODUCER_1;
			TestMaterialsProducerPropertyId propertyId = TestMaterialsProducerPropertyId.MATERIALS_PRODUCER_PROPERTY_1_BOOLEAN_MUTABLE_NO_TRACK;
			PropertyDefinition propertyDefinition = propertyId.getPropertyDefinition();//
			Object value = 12;

			MaterialsPluginData.builder()//
					.addMaterialsProducerId(materialsProducerId)//
					.defineMaterialsProducerProperty(propertyId, propertyDefinition)//
					.setMaterialsProducerPropertyValue(materialsProducerId, propertyId, value)//
					.build();//
		});
		assertEquals(PropertyError.INCOMPATIBLE_VALUE, contractException.getErrorType());

		/*
		 * precondition test: if a materials property is defined without a default value
		 * and there is not an assigned property value for each added materials producer
		 */
		contractException = assertThrows(ContractException.class, () -> {
			MaterialsProducerId materialsProducerId = TestMaterialsProducerId.MATERIALS_PRODUCER_1;
			TestMaterialsProducerPropertyId propertyId = TestMaterialsProducerPropertyId.MATERIALS_PRODUCER_PROPERTY_1_BOOLEAN_MUTABLE_NO_TRACK;

			PropertyDefinition propertyDefinition = PropertyDefinition.builder()//
					.setType(Boolean.class)//
					.setPropertyValueMutability(true)//
					.build();

			MaterialsPluginData.builder()//
					.addMaterialsProducerId(materialsProducerId)//
					.defineMaterialsProducerProperty(propertyId, propertyDefinition)//
					.build();//
		});
		assertEquals(PropertyError.INSUFFICIENT_PROPERTY_VALUE_ASSIGNMENT, contractException.getErrorType());

		/*
		 * precondition test: if a materials resource level is set for a material
		 * producer id that was not properly added
		 */

		contractException = assertThrows(ContractException.class, () -> {
			MaterialsProducerId materialsProducerId = TestMaterialsProducerId.MATERIALS_PRODUCER_1;
			TestResourceId testResourceId = TestResourceId.RESOURCE_2;
			Long resourceLevel = 10L;

			MaterialsPluginData.builder()//
					.setMaterialsProducerResourceLevel(materialsProducerId, testResourceId, resourceLevel) //
					.build();//
		});
		assertEquals(MaterialsError.UNKNOWN_MATERIALS_PRODUCER_ID, contractException.getErrorType());

		/*
		 * precondition test: if a batch is associated with a material that was not
		 * properly added
		 */
		contractException = assertThrows(ContractException.class, () -> {
			MaterialsProducerId materialsProducerId = TestMaterialsProducerId.MATERIALS_PRODUCER_1;
			TestMaterialId testMaterialId = TestMaterialId.MATERIAL_3;
			BatchId batchId = new BatchId(67);
			double amount = 345.543;

			MaterialsPluginData.builder()//
					.addBatch(batchId, testMaterialId, amount)//
					.addBatchToMaterialsProducerInventory(batchId, materialsProducerId)//
					.addMaterialsProducerId(materialsProducerId)//
					.build();//
		});
		assertEquals(MaterialsError.UNKNOWN_MATERIAL_ID, contractException.getErrorType());

		/*
		 * precondition test: if a batch is associated with at material producer that
		 * was not properly added
		 */
		contractException = assertThrows(ContractException.class, () -> {
			MaterialsProducerId materialsProducerId = TestMaterialsProducerId.MATERIALS_PRODUCER_1;
			TestMaterialId testMaterialId = TestMaterialId.MATERIAL_3;
			BatchId batchId = new BatchId(67);
			double amount = 345.543;

			MaterialsPluginData.builder()//
					.addBatch(batchId, testMaterialId, amount)//
					.addBatchToMaterialsProducerInventory(batchId, materialsProducerId)//
					.addMaterial(testMaterialId)//
					.build();//
		});
		assertEquals(MaterialsError.UNKNOWN_MATERIALS_PRODUCER_ID, contractException.getErrorType());

		/*
		 * precondition test: if a batch property is associated with batch id that was
		 * not properly added
		 */
		contractException = assertThrows(ContractException.class, () -> {

			TestBatchPropertyId testBatchPropertyId = TestBatchPropertyId.BATCH_PROPERTY_1_3_DOUBLE_MUTABLE_NO_TRACK;
			PropertyDefinition propertyDefinition = testBatchPropertyId.getPropertyDefinition();
			testBatchPropertyId.getRandomPropertyValue(randomGenerator);
			TestMaterialId testMaterialId = TestMaterialId.MATERIAL_1;

			BatchId batchId = new BatchId(67);

			MaterialsPluginData.builder()//
					.defineBatchProperty(testMaterialId, testBatchPropertyId, propertyDefinition)//
					.addMaterial(testMaterialId)//
					.setBatchPropertyValue(batchId, testBatchPropertyId, batchId)//
					.build();//
		});
		assertEquals(MaterialsError.UNKNOWN_BATCH_ID, contractException.getErrorType());

		/*
		 * precondition test: if a batch property is associated with batch property id
		 * that was not properly defined
		 */
		contractException = assertThrows(ContractException.class, () -> {
			TestMaterialsProducerId testMaterialsProducerId = TestMaterialsProducerId.MATERIALS_PRODUCER_2;
			TestBatchPropertyId testBatchPropertyId = TestBatchPropertyId.BATCH_PROPERTY_1_3_DOUBLE_MUTABLE_NO_TRACK;
			Object value = testBatchPropertyId.getRandomPropertyValue(randomGenerator);
			TestMaterialId testMaterialId = TestMaterialId.MATERIAL_1;

			BatchId batchId = new BatchId(67);
			double amount = 345.54;

			MaterialsPluginData.builder()//
					.addBatch(batchId, testMaterialId, amount)//
					.addBatchToMaterialsProducerInventory(batchId, testMaterialsProducerId)//
					.addMaterial(testMaterialId)//
					.setBatchPropertyValue(batchId, testBatchPropertyId, value)//
					.addMaterialsProducerId(testMaterialsProducerId)//
					.build();//
		});
		assertEquals(PropertyError.UNKNOWN_PROPERTY_ID, contractException.getErrorType());

		/*
		 * precondition test: if a batch property value is incompatible with the
		 * corresponding property definition
		 */

		contractException = assertThrows(ContractException.class, () -> {
			TestMaterialsProducerId testMaterialsProducerId = TestMaterialsProducerId.MATERIALS_PRODUCER_2;
			TestBatchPropertyId testBatchPropertyId = TestBatchPropertyId.BATCH_PROPERTY_1_3_DOUBLE_MUTABLE_NO_TRACK;
			PropertyDefinition propertyDefinition = testBatchPropertyId.getPropertyDefinition();
			Object incompatibleValue = "bad value";
			TestMaterialId testMaterialId = TestMaterialId.MATERIAL_1;

			BatchId batchId = new BatchId(67);
			double amount = 345.54;

			MaterialsPluginData.builder()//
					.addBatch(batchId, testMaterialId, amount)//
					.addBatchToMaterialsProducerInventory(batchId, testMaterialsProducerId)//
					.addMaterial(testMaterialId)//
					.setBatchPropertyValue(batchId, testBatchPropertyId, incompatibleValue)//
					.addMaterialsProducerId(testMaterialsProducerId)//
					.defineBatchProperty(testMaterialId, testBatchPropertyId, propertyDefinition)//
					.build();//
		});
		assertEquals(PropertyError.INCOMPATIBLE_VALUE, contractException.getErrorType());

		/*
		 * precondition test: if a stage is associated with a materials producer id that
		 * was not properly added
		 */
		contractException = assertThrows(ContractException.class, () -> {
			TestMaterialsProducerId testMaterialsProducerId = TestMaterialsProducerId.MATERIALS_PRODUCER_2;
			StageId stageId = new StageId(543);
			boolean offered = false;

			MaterialsPluginData.builder()//
					.addStage(stageId, offered)//
					.addStageToMaterialProducer(stageId, testMaterialsProducerId)//
					.build();//
		});
		assertEquals(MaterialsError.UNKNOWN_MATERIALS_PRODUCER_ID, contractException.getErrorType());

		/*
		 * precondition test: if a batch is associated with a stage id that was not
		 * properly added
		 */

		contractException = assertThrows(ContractException.class, () -> {
			TestMaterialsProducerId testMaterialsProducerId = TestMaterialsProducerId.MATERIALS_PRODUCER_2;
			StageId stageId = new StageId(543);
			BatchId batchId = new BatchId(55);
			double amount = 86.0;
			TestMaterialId testMaterialId = TestMaterialId.MATERIAL_3;

			MaterialsPluginData.builder()//
					.addBatch(batchId, testMaterialId, amount)//
					.addBatchToStage(stageId, batchId)//
					.addMaterial(testMaterialId)//
					.addMaterialsProducerId(testMaterialsProducerId)//
					.build();//
		});
		assertEquals(MaterialsError.UNKNOWN_STAGE_ID, contractException.getErrorType());

		/*
		 * precondition test: if a stage is associated with a batch id that was not
		 * properly added
		 */
		contractException = assertThrows(ContractException.class, () -> {
			TestMaterialsProducerId testMaterialsProducerId = TestMaterialsProducerId.MATERIALS_PRODUCER_2;
			StageId stageId = new StageId(543);
			BatchId batchId = new BatchId(55);
			TestMaterialId testMaterialId = TestMaterialId.MATERIAL_3;
			boolean offered = false;

			MaterialsPluginData.builder()//
					.addStage(stageId, offered)//
					.addStageToMaterialProducer(stageId, testMaterialsProducerId)//
					.addBatchToStage(stageId, batchId)//
					.addMaterial(testMaterialId)//
					.addMaterialsProducerId(testMaterialsProducerId)//
					.build();//
		});
		assertEquals(MaterialsError.UNKNOWN_BATCH_ID, contractException.getErrorType());

		/*
		 * precondition test: if a batch is associated with more than one stage
		 */
		contractException = assertThrows(ContractException.class, () -> {
			TestMaterialsProducerId testMaterialsProducerId = TestMaterialsProducerId.MATERIALS_PRODUCER_2;
			StageId stageId1 = new StageId(543);
			StageId stageId2 = new StageId(659);
			BatchId batchId = new BatchId(55);
			TestMaterialId testMaterialId = TestMaterialId.MATERIAL_3;
			boolean offered = false;
			double amount = 765.87;

			MaterialsPluginData.builder()//
					.addBatch(batchId, testMaterialId, amount)//
					.addStage(stageId1, offered)//
					.addStageToMaterialProducer(stageId1, testMaterialsProducerId)//
					.addStage(stageId2, offered)//
					.addStageToMaterialProducer(stageId2, testMaterialsProducerId)//
					.addBatchToStage(stageId1, batchId)//
					.addBatchToStage(stageId2, batchId)//
					.addMaterial(testMaterialId)//
					.addMaterialsProducerId(testMaterialsProducerId)//
					.build();//
		});
		assertEquals(MaterialsError.BATCH_ALREADY_STAGED, contractException.getErrorType());

	}

	@Test
	@UnitTestMethod(target = MaterialsPluginData.Builder.class, name = "addBatch", args = { BatchId.class,
			MaterialId.class, double.class })
	public void testAddBatch() {
		BatchId batchId = new BatchId(456);
		MaterialId materialId = TestMaterialId.MATERIAL_1;
		double amount = 16.7;
		MaterialsProducerId materialsProducerId = TestMaterialsProducerId.MATERIALS_PRODUCER_3;

		/*
		 * adding duplicate data to show that the value persists
		 */
		MaterialsPluginData materialsInitialData = MaterialsPluginData.builder()//
				.addBatch(batchId, materialId, amount)//
				.addBatchToMaterialsProducerInventory(batchId, materialsProducerId)//

				.addBatch(batchId, materialId, amount)//
				.addBatchToMaterialsProducerInventory(batchId, materialsProducerId)//
				.addMaterial(materialId)//
				.addMaterialsProducerId(materialsProducerId).build();//

		assertTrue(materialsInitialData.getBatchIds().contains(batchId));
		assertEquals(materialId, materialsInitialData.getBatchMaterial(batchId));
		assertEquals(amount, materialsInitialData.getBatchAmount(batchId));
		assertTrue(materialsInitialData.getMaterialsProducerInventoryBatches(materialsProducerId).contains(batchId));

		// idempotency tests

		MaterialId materialId2 = TestMaterialId.MATERIAL_2;
		double amount2 = 76.1;

		materialsInitialData = MaterialsPluginData.builder()//
				.addBatch(batchId, materialId, amount)//
				.addBatchToMaterialsProducerInventory(batchId, materialsProducerId)//
				// replacing data to show
				// that the value persists
				.addBatch(batchId, materialId2, amount2)//
				.addBatchToMaterialsProducerInventory(batchId, materialsProducerId)//
				.addMaterial(materialId2)//
				.addMaterialsProducerId(materialsProducerId).build();//

		assertTrue(materialsInitialData.getBatchIds().contains(batchId));
		assertEquals(materialId2, materialsInitialData.getBatchMaterial(batchId));
		assertEquals(amount2, materialsInitialData.getBatchAmount(batchId));
		assertTrue(materialsInitialData.getMaterialsProducerInventoryBatches(materialsProducerId).contains(batchId));

		// precondition tests

		// if the batch id is null
		ContractException contractException = assertThrows(ContractException.class,
				() -> MaterialsPluginData.builder().addBatch(null, materialId, amount));
		assertEquals(MaterialsError.NULL_BATCH_ID, contractException.getErrorType());

		// if the material id is null
		contractException = assertThrows(ContractException.class,
				() -> MaterialsPluginData.builder().addBatch(batchId, null, amount));
		assertEquals(MaterialsError.NULL_MATERIAL_ID, contractException.getErrorType());

		// if the material amount is infinite
		contractException = assertThrows(ContractException.class,
				() -> MaterialsPluginData.builder().addBatch(batchId, materialId, Double.POSITIVE_INFINITY));
		assertEquals(MaterialsError.NON_FINITE_MATERIAL_AMOUNT, contractException.getErrorType());

		// if the material amount is negative
		contractException = assertThrows(ContractException.class,
				() -> MaterialsPluginData.builder().addBatch(batchId, materialId, -1));
		assertEquals(MaterialsError.NEGATIVE_MATERIAL_AMOUNT, contractException.getErrorType());

	}

	@Test
	@UnitTestMethod(target = MaterialsPluginData.Builder.class, name = "addBatchToMaterialsProducerInventory", args = {
			BatchId.class, MaterialsProducerId.class })
	public void testAddBatchToMaterialsProducerInventory() {

		BatchId batchId = new BatchId(456);
		MaterialId materialId = TestMaterialId.MATERIAL_1;
		MaterialsProducerId materialsProducerId = TestMaterialsProducerId.MATERIALS_PRODUCER_3;
		double amount = 16.7;

		MaterialsPluginData materialsPluginData = MaterialsPluginData.builder()//
				.addBatch(batchId, materialId, amount)//
				.addMaterial(materialId)//
				.addMaterialsProducerId(materialsProducerId)//
				.addBatchToMaterialsProducerInventory(batchId, materialsProducerId).build();//

		assertTrue(materialsPluginData.getMaterialsProducerInventoryBatches(materialsProducerId).contains(batchId));

		// precondition test: if the batch id is null
		ContractException contractException = assertThrows(ContractException.class,
				() -> MaterialsPluginData.builder().addBatchToMaterialsProducerInventory(null, materialsProducerId));
		assertEquals(MaterialsError.NULL_BATCH_ID, contractException.getErrorType());

		// precondition test: if the materials producer id is null
		contractException = assertThrows(ContractException.class,
				() -> MaterialsPluginData.builder().addBatchToMaterialsProducerInventory(batchId, null));
		assertEquals(MaterialsError.NULL_MATERIALS_PRODUCER_ID, contractException.getErrorType());

	}

	@Test
	@UnitTestMethod(target = MaterialsPluginData.Builder.class, name = "addBatchToStage", args = { StageId.class,
			BatchId.class })
	public void testAddBatchToStage() {
		BatchId batchId = new BatchId(456);
		StageId stageId = new StageId(543);
		MaterialId materialId = TestMaterialId.MATERIAL_1;
		double amount = 16.7;
		MaterialsProducerId materialsProducerId = TestMaterialsProducerId.MATERIALS_PRODUCER_3;

		/*
		 * adding duplicate data to show that the value persists
		 */

		MaterialsPluginData materialsInitialData = MaterialsPluginData.builder()//
				.addBatch(batchId, materialId, amount)//
				.addBatchToStage(stageId, batchId)//
				.addBatchToStage(stageId, batchId)//
				.addStage(stageId, false)//
				.addStageToMaterialProducer(stageId, materialsProducerId)//
				.addMaterial(materialId)//
				.addMaterialsProducerId(materialsProducerId)//
				.build();//

		assertTrue(materialsInitialData.getStageBatches(stageId).contains(batchId));

		// precondition tests

		// if the stage id is null
		ContractException contractException = assertThrows(ContractException.class,
				() -> MaterialsPluginData.builder().addBatchToStage(null, batchId));
		assertEquals(MaterialsError.NULL_STAGE_ID, contractException.getErrorType());

		// if the batch id is null
		contractException = assertThrows(ContractException.class,
				() -> MaterialsPluginData.builder().addBatchToStage(stageId, null));
		assertEquals(MaterialsError.NULL_BATCH_ID, contractException.getErrorType());

	}

	@Test
	@UnitTestMethod(target = MaterialsPluginData.Builder.class, name = "addMaterial", args = { MaterialId.class })
	public void testAddMaterial() {

		MaterialId materialId = TestMaterialId.MATERIAL_1;

		MaterialsPluginData materialsInitialData = MaterialsPluginData.builder()//
				.addMaterial(materialId)
				// adding
				// duplicate
				// data
				// to
				// show
				// that
				// the
				// value
				// persists
				.addMaterial(materialId)//
				.build();//

		assertTrue(materialsInitialData.getMaterialIds().contains(materialId));

		// if the material id is null
		ContractException contractException = assertThrows(ContractException.class,
				() -> MaterialsPluginData.builder().addMaterial(null));
		assertEquals(MaterialsError.NULL_MATERIAL_ID, contractException.getErrorType());

		// show that duplicated values persist
		Set<MaterialId> expectedIds = new LinkedHashSet<>();
		MaterialsPluginData.Builder builder = MaterialsPluginData.builder();
		for (MaterialId matId : TestMaterialId.values()) {
			expectedIds.add(matId);
			builder.addMaterial(matId).addMaterial(matId);
		}
		materialsInitialData = builder.build();
		Set<MaterialId> actualIds = materialsInitialData.getMaterialIds();
		assertEquals(expectedIds, actualIds);
	}

	@Test
	@UnitTestMethod(target = MaterialsPluginData.Builder.class, name = "addMaterialsProducerId", args = {
			MaterialsProducerId.class })
	public void testAddMaterialsProducerId() {
		MaterialsProducerId materialsProducerId1 = TestMaterialsProducerId.MATERIALS_PRODUCER_1;
		MaterialsProducerId materialsProducerId2 = TestMaterialsProducerId.MATERIALS_PRODUCER_2;

		MaterialsPluginData materialsInitialData = MaterialsPluginData.builder()//
				.addMaterialsProducerId(materialsProducerId1)//
				// adding
				// duplicate
				// data
				// to
				// show
				// that
				// the
				// value
				// persists
				.addMaterialsProducerId(materialsProducerId1).addMaterialsProducerId(materialsProducerId2)//
				.build();//

		// show that the materials producer ids were added
		assertTrue(materialsInitialData.getMaterialsProducerIds().contains(materialsProducerId1));

		assertTrue(materialsInitialData.getMaterialsProducerIds().contains(materialsProducerId2));

		// precondition tests

		// if the material id is null
		ContractException contractException = assertThrows(ContractException.class,
				() -> MaterialsPluginData.builder().addMaterialsProducerId(null));
		assertEquals(MaterialsError.NULL_MATERIALS_PRODUCER_ID, contractException.getErrorType());

	}

	@Test
	@UnitTestMethod(target = MaterialsPluginData.Builder.class, name = "addStageToMaterialProducer", args = {
			StageId.class, MaterialsProducerId.class })
	public void testAddStageToMaterialProducer() {
		StageId stageId = new StageId(456);
		MaterialsProducerId materialsProducerId1 = TestMaterialsProducerId.MATERIALS_PRODUCER_1;
		MaterialsProducerId materialsProducerId2 = TestMaterialsProducerId.MATERIALS_PRODUCER_2;
		MaterialsPluginData materialsPluginData = MaterialsPluginData.builder()//
				.addStage(stageId, false)//
				.addStageToMaterialProducer(stageId, materialsProducerId1)//
				.addStageToMaterialProducer(stageId, materialsProducerId1)// duplicated command
				.addMaterialsProducerId(materialsProducerId1)//
				.addMaterialsProducerId(materialsProducerId2)//
				.build();
		assertTrue(materialsPluginData.getMaterialsProducerStages(materialsProducerId1).contains(stageId));
		assertFalse(materialsPluginData.getMaterialsProducerStages(materialsProducerId2).contains(stageId));

		// precondition test: if the stage id is null
		ContractException contractException = assertThrows(ContractException.class,
				() -> MaterialsPluginData.builder().addStageToMaterialProducer(null, materialsProducerId1));
		assertEquals(MaterialsError.NULL_STAGE_ID, contractException.getErrorType());

		// precondition test: if the materials producer id is null
		contractException = assertThrows(ContractException.class,
				() -> MaterialsPluginData.builder().addStageToMaterialProducer(stageId, null));
		assertEquals(MaterialsError.NULL_MATERIALS_PRODUCER_ID, contractException.getErrorType());
	}

	@Test
	@UnitTestMethod(target = MaterialsPluginData.Builder.class, name = "addStage", args = { StageId.class,
			boolean.class })
	public void testAddStage() {
		StageId stageId = new StageId(456);
		boolean offered = true;
		MaterialsProducerId materialsProducerId = TestMaterialsProducerId.MATERIALS_PRODUCER_3;

		MaterialsPluginData materialsInitialData = MaterialsPluginData.builder()//
				.addStage(stageId, offered)//
				.addStageToMaterialProducer(stageId, materialsProducerId)//
				.addStage(stageId, offered)//
				.addStageToMaterialProducer(stageId, materialsProducerId)//
				.addMaterialsProducerId(materialsProducerId)//
				.build();//
		assertTrue(materialsInitialData.getStageIds().contains(stageId));
		assertEquals(offered, materialsInitialData.isStageOffered(stageId));
		assertTrue(materialsInitialData.getMaterialsProducerStages(materialsProducerId).contains(stageId));

		offered = false;
		materialsInitialData = MaterialsPluginData.builder()//
				.addStage(stageId, offered)//
				.addStageToMaterialProducer(stageId, materialsProducerId)//
				.addStage(stageId, offered)//
				.addStageToMaterialProducer(stageId, materialsProducerId)//
				.addMaterialsProducerId(materialsProducerId)//
				.build();//

		assertTrue(materialsInitialData.getStageIds().contains(stageId));
		assertEquals(offered, materialsInitialData.isStageOffered(stageId));
		assertTrue(materialsInitialData.getMaterialsProducerStages(materialsProducerId).contains(stageId));

		// idempotency tests

		boolean offered2 = true;
		MaterialsProducerId materialsProducerId2 = TestMaterialsProducerId.MATERIALS_PRODUCER_2;

		/*
		 * replacing data to show that the value persists
		 * 
		 */
		materialsInitialData = MaterialsPluginData.builder()//
				.addStage(stageId, offered)//
				.addStageToMaterialProducer(stageId, materialsProducerId2)//
				.addStage(stageId, offered2)//
				.addStageToMaterialProducer(stageId, materialsProducerId2)//
				.addMaterialsProducerId(materialsProducerId2)//
				.build();//
		assertTrue(materialsInitialData.getStageIds().contains(stageId));
		assertEquals(offered2, materialsInitialData.isStageOffered(stageId));
		assertTrue(materialsInitialData.getMaterialsProducerStages(materialsProducerId2).contains(stageId));

		offered = true;
		offered2 = false;
		materialsInitialData = MaterialsPluginData.builder()//
				.addStage(stageId, offered) //
				.addStageToMaterialProducer(stageId, materialsProducerId2)//
				.addStage(stageId, offered2)//
				.addStageToMaterialProducer(stageId, materialsProducerId2)//
				.addMaterialsProducerId(materialsProducerId2)//
				.build();//

		assertTrue(materialsInitialData.getStageIds().contains(stageId));
		assertEquals(offered2, materialsInitialData.isStageOffered(stageId));
		assertTrue(materialsInitialData.getMaterialsProducerStages(materialsProducerId2).contains(stageId));

		// precondition test: if the stage id is null
		ContractException contractException = assertThrows(ContractException.class,
				() -> MaterialsPluginData.builder().addStage(null, true));
		assertEquals(MaterialsError.NULL_STAGE_ID, contractException.getErrorType());
	}

	@Test
	@UnitTestMethod(target = MaterialsPluginData.Builder.class, name = "defineBatchProperty", args = { MaterialId.class,
			BatchPropertyId.class, PropertyDefinition.class })
	public void testDefineBatchProperty() {

		MaterialsPluginData.Builder builder = MaterialsPluginData.builder();//
		for (TestMaterialId testMaterialId : TestMaterialId.values()) {
			builder.addMaterial(testMaterialId);
		}
		for (TestBatchPropertyId testBatchPropertyId : TestBatchPropertyId.values()) {
			// adding duplicate data to show that the value persists
			builder.defineBatchProperty(testBatchPropertyId.getTestMaterialId(), testBatchPropertyId,
					testBatchPropertyId.getPropertyDefinition())
					.defineBatchProperty(testBatchPropertyId.getTestMaterialId(), testBatchPropertyId,
							testBatchPropertyId.getPropertyDefinition());
		}
		MaterialsPluginData materialsInitialData = builder.build();//

		for (TestBatchPropertyId testBatchPropertyId : TestBatchPropertyId.values()) {
			TestMaterialId testMaterialId = testBatchPropertyId.getTestMaterialId();
			assertTrue(materialsInitialData.getBatchPropertyIds(testMaterialId).contains(testBatchPropertyId));
			PropertyDefinition actualPropertyDefinition = materialsInitialData
					.getBatchPropertyDefinition(testMaterialId, testBatchPropertyId);
			PropertyDefinition expectedPropertyDefinition = testBatchPropertyId.getPropertyDefinition();
			assertEquals(expectedPropertyDefinition, actualPropertyDefinition);
		}
		// idempotency tests
		TestBatchPropertyId testBatchPropertyId = TestBatchPropertyId.BATCH_PROPERTY_2_2_INTEGER_IMMUTABLE_TRACK;
		TestMaterialId testMaterialId = testBatchPropertyId.getTestMaterialId();
		PropertyDefinition propertyDefinition = testBatchPropertyId.getPropertyDefinition();

		// show that replaced values persist
		builder = MaterialsPluginData.builder();
		TestBatchPropertyId testBatchPropertyId2 = TestBatchPropertyId.BATCH_PROPERTY_1_2_INTEGER_MUTABLE_NO_TRACK;
		PropertyDefinition propertyDefinition2 = testBatchPropertyId2.getPropertyDefinition();
		builder.addMaterial(testMaterialId);
		builder.defineBatchProperty(testMaterialId, testBatchPropertyId, propertyDefinition)
				.defineBatchProperty(testMaterialId, testBatchPropertyId2, propertyDefinition2);
		materialsInitialData = builder.build();
		assertTrue(materialsInitialData.getBatchPropertyIds(testMaterialId).contains(testBatchPropertyId2));
		PropertyDefinition actualPropertyDefinition2 = materialsInitialData.getBatchPropertyDefinition(testMaterialId,
				testBatchPropertyId2);
		PropertyDefinition expectedPropertyDefinition2 = testBatchPropertyId2.getPropertyDefinition();
		assertEquals(expectedPropertyDefinition2, actualPropertyDefinition2);

		// precondition test: if the batch property id is null
		ContractException contractException = assertThrows(ContractException.class,
				() -> MaterialsPluginData.builder().defineBatchProperty(testMaterialId, null, propertyDefinition));
		assertEquals(PropertyError.NULL_PROPERTY_ID, contractException.getErrorType());

		// precondition test: if the material id is null
		contractException = assertThrows(ContractException.class,
				() -> MaterialsPluginData.builder().defineBatchProperty(null, testBatchPropertyId, propertyDefinition));
		assertEquals(MaterialsError.NULL_MATERIAL_ID, contractException.getErrorType());

		// precondition test: if the property definition is null
		contractException = assertThrows(ContractException.class,
				() -> MaterialsPluginData.builder().defineBatchProperty(testMaterialId, testBatchPropertyId, null));
		assertEquals(PropertyError.NULL_PROPERTY_DEFINITION, contractException.getErrorType());

	}

	@Test
	@UnitTestMethod(target = MaterialsPluginData.Builder.class, name = "defineMaterialsProducerProperty", args = {
			MaterialsProducerPropertyId.class, PropertyDefinition.class })
	public void testDefineMaterialsProducerProperty() {
		MaterialsPluginData.Builder builder = MaterialsPluginData.builder();//

		for (TestMaterialsProducerPropertyId testMaterialsProducerPropertyId : TestMaterialsProducerPropertyId
				.values()) {
			// adding duplicate data to show that the value persists
			builder.defineMaterialsProducerProperty(testMaterialsProducerPropertyId,
					testMaterialsProducerPropertyId.getPropertyDefinition()).defineMaterialsProducerProperty(
							testMaterialsProducerPropertyId, testMaterialsProducerPropertyId.getPropertyDefinition());
		}
		MaterialsPluginData materialsInitialData = builder.build();//

		for (TestMaterialsProducerPropertyId testMaterialsProducerPropertyId : TestMaterialsProducerPropertyId
				.values()) {
			assertTrue(
					materialsInitialData.getMaterialsProducerPropertyIds().contains(testMaterialsProducerPropertyId));
			PropertyDefinition actualPropertyDefinition = materialsInitialData
					.getMaterialsProducerPropertyDefinition(testMaterialsProducerPropertyId);
			PropertyDefinition expectedPropertyDefinition = testMaterialsProducerPropertyId.getPropertyDefinition();
			assertEquals(expectedPropertyDefinition, actualPropertyDefinition);
		}

		TestMaterialsProducerPropertyId testMaterialsProducerPropertyId = TestMaterialsProducerPropertyId.MATERIALS_PRODUCER_PROPERTY_3_DOUBLE_MUTABLE_NO_TRACK;

		PropertyDefinition propertyDefinition = testMaterialsProducerPropertyId.getPropertyDefinition();
		// idempotency tests

		// show that replaced values persist
		builder = MaterialsPluginData.builder();
		PropertyDefinition propertyDefinition2 = testMaterialsProducerPropertyId.getPropertyDefinition();
		builder.defineMaterialsProducerProperty(testMaterialsProducerPropertyId, propertyDefinition)
				.defineMaterialsProducerProperty(testMaterialsProducerPropertyId, propertyDefinition2);
		materialsInitialData = builder.build();
		assertTrue(materialsInitialData.getMaterialsProducerPropertyIds().contains(testMaterialsProducerPropertyId));
		PropertyDefinition actualPropertyDefinition2 = materialsInitialData
				.getMaterialsProducerPropertyDefinition(testMaterialsProducerPropertyId);
		assertEquals(propertyDefinition2, actualPropertyDefinition2);

		// precondition test: if the materials producer property id is null
		ContractException contractException = assertThrows(ContractException.class,
				() -> MaterialsPluginData.builder().defineMaterialsProducerProperty(null, propertyDefinition));
		assertEquals(PropertyError.NULL_PROPERTY_ID, contractException.getErrorType());

		// precondition test: if the property definition is null
		contractException = assertThrows(ContractException.class, () -> MaterialsPluginData.builder()
				.defineMaterialsProducerProperty(testMaterialsProducerPropertyId, null));
		assertEquals(PropertyError.NULL_PROPERTY_DEFINITION, contractException.getErrorType());

	}

	@Test
	@UnitTestMethod(target = MaterialsPluginData.Builder.class, name = "setBatchPropertyValue", args = { BatchId.class,
			BatchPropertyId.class, Object.class })
	public void testSetBatchPropertyValue() {

		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(8767111011954878165L);

		/*
		 * Add 30 batches with about half of the batch properties being set to
		 * randomized values and the other half set to the default for the property
		 * definition
		 */
		MaterialsPluginData.Builder builder = MaterialsPluginData.builder();//

		for (TestMaterialId testMaterialId : TestMaterialId.values()) {
			builder.addMaterial(testMaterialId);
		}
		for (TestBatchPropertyId testBatchPropertyId : TestBatchPropertyId.values()) {
			builder.defineBatchProperty(testBatchPropertyId.getTestMaterialId(), testBatchPropertyId,
					testBatchPropertyId.getPropertyDefinition());
		}
		for (TestMaterialsProducerId testMaterialsProducerId : TestMaterialsProducerId.values()) {
			builder.addMaterialsProducerId(testMaterialsProducerId);
		}

		// create a container to hold expected batch property values
		Map<MultiKey, Object> expectedBatchPropertyValues = new LinkedHashMap<>();

		// add the batches
		for (int i = 0; i < 30; i++) {
			BatchId batchId = new BatchId(i);
			TestMaterialId testMaterialId = TestMaterialId.getRandomMaterialId(randomGenerator);
			builder.addBatch(batchId, testMaterialId, randomGenerator.nextDouble());
			builder.addBatchToMaterialsProducerInventory(batchId,
					TestMaterialsProducerId.getRandomMaterialsProducerId(randomGenerator));

			for (TestBatchPropertyId testBatchPropertyId : TestBatchPropertyId
					.getTestBatchPropertyIds(testMaterialId)) {
				MultiKey multiKey = new MultiKey(batchId, testBatchPropertyId);
				boolean required = testBatchPropertyId.getPropertyDefinition().getDefaultValue().isEmpty();
				if (required || randomGenerator.nextBoolean()) {
					Object propertyValue = testBatchPropertyId.getRandomPropertyValue(randomGenerator);
					// adding duplicate data to show that the value persists
					builder.setBatchPropertyValue(batchId, testBatchPropertyId, propertyValue)//
							.setBatchPropertyValue(batchId, testBatchPropertyId, propertyValue);
					expectedBatchPropertyValues.put(multiKey, propertyValue);
				}

			}
		}

		// build the MaterialsInitialization
		MaterialsPluginData materialsInitialData = builder.build();//

		// show that the MaterialsInitialization returns the expected batch
		// property values
		for (MultiKey multiKey : expectedBatchPropertyValues.keySet()) {
			BatchId batchid = multiKey.getKey(0);
			BatchPropertyId batchPropertyId = multiKey.getKey(1);
			Object expectedValue = expectedBatchPropertyValues.get(multiKey);
			Map<BatchPropertyId, Object> batchPropertyValues = materialsInitialData.getBatchPropertyValues(batchid);
			Object actualValue = batchPropertyValues.get(batchPropertyId);
			assertEquals(expectedValue, actualValue);
		}

		// idempotency test (replacement)
		builder = MaterialsPluginData.builder();
		for (TestMaterialId testMaterialId : TestMaterialId.values()) {
			builder.addMaterial(testMaterialId);
		}
		for (TestBatchPropertyId testBatchPropertyId : TestBatchPropertyId.values()) {
			builder.defineBatchProperty(testBatchPropertyId.getTestMaterialId(), testBatchPropertyId,
					testBatchPropertyId.getPropertyDefinition());
		}
		for (TestMaterialsProducerId testMaterialsProducerId : TestMaterialsProducerId.values()) {
			builder.addMaterialsProducerId(testMaterialsProducerId);
		}

		// reset container to hold expected batch property values
		expectedBatchPropertyValues = new LinkedHashMap<>();

		// add the batches
		for (int i = 0; i < 30; i++) {
			BatchId batchId = new BatchId(i);
			TestMaterialId testMaterialId = TestMaterialId.getRandomMaterialId(randomGenerator);
			builder.addBatch(batchId, testMaterialId, randomGenerator.nextDouble());
			builder.addBatchToMaterialsProducerInventory(batchId,
					TestMaterialsProducerId.getRandomMaterialsProducerId(randomGenerator));

			for (TestBatchPropertyId testBatchPropertyId : TestBatchPropertyId
					.getTestBatchPropertyIds(testMaterialId)) {
				MultiKey multiKey = new MultiKey(batchId, testBatchPropertyId);
				boolean required = testBatchPropertyId.getPropertyDefinition().getDefaultValue().isEmpty();
				if (required || randomGenerator.nextBoolean()) {
					Object propertyValue = testBatchPropertyId.getRandomPropertyValue(randomGenerator);
					// replaced data to show that the value persists
					builder.setBatchPropertyValue(batchId, testBatchPropertyId, propertyValue);
					propertyValue = testBatchPropertyId.getRandomPropertyValue(randomGenerator);
					builder.setBatchPropertyValue(batchId, testBatchPropertyId, propertyValue);
					expectedBatchPropertyValues.put(multiKey, propertyValue);
				}

			}
		}

		// build the MaterialsInitialization
		materialsInitialData = builder.build();//

		// show that the MaterialsInitialization returns the expected batch
		// property values
		for (MultiKey multiKey : expectedBatchPropertyValues.keySet()) {
			BatchId batchid = multiKey.getKey(0);
			BatchPropertyId batchPropertyId = multiKey.getKey(1);
			Object expectedValue = expectedBatchPropertyValues.get(multiKey);
			Map<BatchPropertyId, Object> batchPropertyValues = materialsInitialData.getBatchPropertyValues(batchid);
			Object actualValue = batchPropertyValues.get(batchPropertyId);
			assertEquals(expectedValue, actualValue);
		}

		// precondition tests

		BatchId batchId = new BatchId(0);
		TestBatchPropertyId testBatchPropertyId = TestBatchPropertyId.BATCH_PROPERTY_2_2_INTEGER_IMMUTABLE_TRACK;
		Object propertyValue = 17;

		// if the batch id is null
		ContractException contractException = assertThrows(ContractException.class,
				() -> MaterialsPluginData.builder().setBatchPropertyValue(null, testBatchPropertyId, propertyValue));
		assertEquals(MaterialsError.NULL_BATCH_ID, contractException.getErrorType());

		// if the batch property id is null
		contractException = assertThrows(ContractException.class,
				() -> MaterialsPluginData.builder().setBatchPropertyValue(batchId, null, propertyValue));
		assertEquals(PropertyError.NULL_PROPERTY_ID, contractException.getErrorType());

		// if the batch property value is null
		contractException = assertThrows(ContractException.class,
				() -> MaterialsPluginData.builder().setBatchPropertyValue(batchId, testBatchPropertyId, null));
		assertEquals(PropertyError.NULL_PROPERTY_VALUE, contractException.getErrorType());
	}

	@Test
	@UnitTestMethod(target = MaterialsPluginData.Builder.class, name = "setMaterialsProducerPropertyValue", args = {
			MaterialsProducerId.class, MaterialsProducerPropertyId.class, Object.class })
	public void testSetMaterialsProducerPropertyValue() {

		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(5680332692938057510L);
		MaterialsPluginData.Builder builder = MaterialsPluginData.builder();//

		for (TestMaterialsProducerPropertyId testMaterialsProducerPropertyId : TestMaterialsProducerPropertyId
				.values()) {
			builder.defineMaterialsProducerProperty(testMaterialsProducerPropertyId,
					testMaterialsProducerPropertyId.getPropertyDefinition());
		}

		Map<MultiKey, Object> expectedPropertyValues = new LinkedHashMap<>();

		for (TestMaterialsProducerId testMaterialsProducerId : TestMaterialsProducerId.values()) {
			builder.addMaterialsProducerId(testMaterialsProducerId);
			for (TestMaterialsProducerPropertyId testMaterialsProducerPropertyId : TestMaterialsProducerPropertyId
					.values()) {
				boolean required = testMaterialsProducerPropertyId.getPropertyDefinition().getDefaultValue().isEmpty();
				MultiKey multiKey = new MultiKey(testMaterialsProducerId, testMaterialsProducerPropertyId);
				if (required || randomGenerator.nextBoolean()) {
					Object propertyValue = testMaterialsProducerPropertyId.getRandomPropertyValue(randomGenerator);
					// adding duplicate data to show that the value persists
					builder.setMaterialsProducerPropertyValue(testMaterialsProducerId, testMaterialsProducerPropertyId,
							propertyValue);
					builder.setMaterialsProducerPropertyValue(testMaterialsProducerId, testMaterialsProducerPropertyId,
							propertyValue);
					expectedPropertyValues.put(multiKey, propertyValue);
				}

			}
		}

		MaterialsPluginData materialsInitialData = builder.build();//

		for (MultiKey multiKey : expectedPropertyValues.keySet()) {
			Object expectedValue = expectedPropertyValues.get(multiKey);
			TestMaterialsProducerId testMaterialsProducerId = multiKey.getKey(0);
			TestMaterialsProducerPropertyId testMaterialsProducerPropertyId = multiKey.getKey(1);
			Map<MaterialsProducerPropertyId, Object> materialsProducerPropertyValues = materialsInitialData
					.getMaterialsProducerPropertyValues(testMaterialsProducerId);
			Object actualValue = materialsProducerPropertyValues.get(testMaterialsProducerPropertyId);
			assertEquals(expectedValue, actualValue);
		}

		// idempotency test(replacement)
		builder = MaterialsPluginData.builder();
		for (TestMaterialsProducerPropertyId testMaterialsProducerPropertyId : TestMaterialsProducerPropertyId
				.values()) {
			builder.defineMaterialsProducerProperty(testMaterialsProducerPropertyId,
					testMaterialsProducerPropertyId.getPropertyDefinition());
		}

		expectedPropertyValues = new LinkedHashMap<>();

		for (TestMaterialsProducerId testMaterialsProducerId : TestMaterialsProducerId.values()) {
			builder.addMaterialsProducerId(testMaterialsProducerId);
			for (TestMaterialsProducerPropertyId testMaterialsProducerPropertyId : TestMaterialsProducerPropertyId
					.values()) {
				boolean required = testMaterialsProducerPropertyId.getPropertyDefinition().getDefaultValue().isEmpty();
				MultiKey multiKey = new MultiKey(testMaterialsProducerId, testMaterialsProducerPropertyId);
				if (required || randomGenerator.nextBoolean()) {
					Object propertyValue = testMaterialsProducerPropertyId.getRandomPropertyValue(randomGenerator);
					// replacing data to show that the value persists
					builder.setMaterialsProducerPropertyValue(testMaterialsProducerId, testMaterialsProducerPropertyId,
							propertyValue);
					propertyValue = testMaterialsProducerPropertyId.getRandomPropertyValue(randomGenerator);
					builder.setMaterialsProducerPropertyValue(testMaterialsProducerId, testMaterialsProducerPropertyId,
							propertyValue);
					expectedPropertyValues.put(multiKey, propertyValue);
				}

			}
		}

		materialsInitialData = builder.build();//

		for (MultiKey multiKey : expectedPropertyValues.keySet()) {
			Object expectedValue = expectedPropertyValues.get(multiKey);
			TestMaterialsProducerId testMaterialsProducerId = multiKey.getKey(0);
			TestMaterialsProducerPropertyId testMaterialsProducerPropertyId = multiKey.getKey(1);
			Map<MaterialsProducerPropertyId, Object> materialsProducerPropertyValues = materialsInitialData
					.getMaterialsProducerPropertyValues(testMaterialsProducerId);
			Object actualValue = materialsProducerPropertyValues.get(testMaterialsProducerPropertyId);
			assertEquals(expectedValue, actualValue);
		}

		// precondition tests

		TestMaterialsProducerId testMaterialsProducerId = TestMaterialsProducerId.MATERIALS_PRODUCER_1;
		TestMaterialsProducerPropertyId testMaterialsProducerPropertyId = TestMaterialsProducerPropertyId.MATERIALS_PRODUCER_PROPERTY_3_DOUBLE_MUTABLE_NO_TRACK;
		Object propertyValue = 45.6;

		// if the materials producer id is null
		ContractException contractException = assertThrows(ContractException.class, () -> MaterialsPluginData.builder()
				.setMaterialsProducerPropertyValue(null, testMaterialsProducerPropertyId, propertyValue));
		assertEquals(MaterialsError.NULL_MATERIALS_PRODUCER_ID, contractException.getErrorType());

		// if the materials producer property id is null
		contractException = assertThrows(ContractException.class, () -> MaterialsPluginData.builder()
				.setMaterialsProducerPropertyValue(testMaterialsProducerId, null, propertyValue));
		assertEquals(PropertyError.NULL_PROPERTY_ID, contractException.getErrorType());

		// if the materials producer property value is null
		contractException = assertThrows(ContractException.class, () -> MaterialsPluginData.builder()
				.setMaterialsProducerPropertyValue(testMaterialsProducerId, testMaterialsProducerPropertyId, null));
		assertEquals(PropertyError.NULL_PROPERTY_VALUE, contractException.getErrorType());
	}

	@Test
	@UnitTestMethod(target = MaterialsPluginData.Builder.class, name = "setMaterialsProducerResourceLevel", args = {
			MaterialsProducerId.class, ResourceId.class, long.class })
	public void testSetMaterialsProducerResourceLevel() {

		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(3277582868385203332L);

		Map<MultiKey, Long> expectedResourceLevels = new LinkedHashMap<>();

		MaterialsPluginData.Builder builder = MaterialsPluginData.builder();
		for (TestMaterialsProducerId testMaterialsProducerId : TestMaterialsProducerId.values()) {
			builder.addMaterialsProducerId(testMaterialsProducerId);
			for (TestResourceId testResourceId : TestResourceId.values()) {
				MultiKey multiKey = new MultiKey(testMaterialsProducerId, testResourceId);
				long level = 0;
				if (randomGenerator.nextBoolean()) {
					level = randomGenerator.nextInt(100);
					// adding duplicate data to show that the value persists
					builder//
							.setMaterialsProducerResourceLevel(testMaterialsProducerId, testResourceId, level)//
							.setMaterialsProducerResourceLevel(testMaterialsProducerId, testResourceId, level);
				}
				expectedResourceLevels.put(multiKey, level);
			}
		}

		MaterialsPluginData materialsInitialData = builder.build();

		for (MultiKey multiKey : expectedResourceLevels.keySet()) {
			long expectedValue = expectedResourceLevels.get(multiKey);
			TestMaterialsProducerId testMaterialsProducerId = multiKey.getKey(0);
			TestResourceId testResourceId = multiKey.getKey(1);
			Long actualValue = materialsInitialData.getMaterialsProducerResourceLevel(testMaterialsProducerId,
					testResourceId);
			assertEquals(expectedValue, actualValue);
		}

		// idempotency test (replacement)

		expectedResourceLevels = new LinkedHashMap<>();
		builder = MaterialsPluginData.builder();
		for (TestMaterialsProducerId testMaterialsProducerId : TestMaterialsProducerId.values()) {
			builder.addMaterialsProducerId(testMaterialsProducerId);
			for (TestResourceId testResourceId : TestResourceId.values()) {
				MultiKey multiKey = new MultiKey(testMaterialsProducerId, testResourceId);
				long level = 0;
				if (randomGenerator.nextBoolean()) {
					level = randomGenerator.nextInt(100);
					// replacing data to show that the value persists
					builder.setMaterialsProducerResourceLevel(testMaterialsProducerId, testResourceId, level);
					level = 1;
					builder.setMaterialsProducerResourceLevel(testMaterialsProducerId, testResourceId, level);
				}
				expectedResourceLevels.put(multiKey, level);
			}
		}

		materialsInitialData = builder.build();

		for (MultiKey multiKey : expectedResourceLevels.keySet()) {
			long expectedValue = expectedResourceLevels.get(multiKey);
			TestMaterialsProducerId testMaterialsProducerId = multiKey.getKey(0);
			TestResourceId testResourceId = multiKey.getKey(1);
			Long actualValue = materialsInitialData.getMaterialsProducerResourceLevel(testMaterialsProducerId,
					testResourceId);
			assertEquals(expectedValue, actualValue);
		}

		// precondition tests

		TestMaterialsProducerId testMaterialsProducerId = TestMaterialsProducerId.MATERIALS_PRODUCER_2;
		TestResourceId testResourceId = TestResourceId.RESOURCE_3;
		long level = 345;

		// if the materials producer id is null
		ContractException contractException = assertThrows(ContractException.class,
				() -> MaterialsPluginData.builder().setMaterialsProducerResourceLevel(null, testResourceId, level));
		assertEquals(MaterialsError.NULL_MATERIALS_PRODUCER_ID, contractException.getErrorType());

		// if the resource id is null
		contractException = assertThrows(ContractException.class, () -> MaterialsPluginData.builder()
				.setMaterialsProducerResourceLevel(testMaterialsProducerId, null, level));
		assertEquals(ResourceError.NULL_RESOURCE_ID, contractException.getErrorType());

		// if the resource amount is negative
		contractException = assertThrows(ContractException.class, () -> MaterialsPluginData.builder()
				.setMaterialsProducerResourceLevel(testMaterialsProducerId, testResourceId, -1));
		assertEquals(ResourceError.NEGATIVE_RESOURCE_AMOUNT, contractException.getErrorType());
	}

	@Test
	@UnitTestMethod(target = MaterialsPluginData.class, name = "getBatchAmounts", args = {})
	public void testGetBatchAmounts() {

		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(6746980823689022132L);

		Map<BatchId, Double> expectedBatchAmounts = new LinkedHashMap<>();

		MaterialsPluginData.Builder builder = MaterialsPluginData.builder();

		for (int i = 0; i < 20; i++) {
			BatchId batchId = new BatchId(i);
			MaterialId materialId = TestMaterialId.getRandomMaterialId(randomGenerator);
			double amount = randomGenerator.nextDouble();
			TestMaterialsProducerId testMaterialsProducerId = TestMaterialsProducerId
					.getRandomMaterialsProducerId(randomGenerator);
			builder.addBatch(batchId, materialId, amount);
			builder.addBatchToMaterialsProducerInventory(batchId, testMaterialsProducerId);
			expectedBatchAmounts.put(batchId, amount);
		}

		for (TestMaterialId testMaterialId : TestMaterialId.values()) {
			builder.addMaterial(testMaterialId);
		}

		for (TestMaterialsProducerId testMaterialsProducerId : TestMaterialsProducerId.values()) {
			builder.addMaterialsProducerId(testMaterialsProducerId);
		}

		MaterialsPluginData materialsPluginData = builder.build();

		Map<BatchId, Double> actualBatchAmounts = materialsPluginData.getBatchAmounts();

		assertEquals(expectedBatchAmounts, actualBatchAmounts);

	}

	@Test
	@UnitTestMethod(target = MaterialsPluginData.class, name = "getBatchAmount", args = { BatchId.class })
	public void testGetBatchAmount() {

		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(6746980823689022132L);

		Map<BatchId, Double> expectedBatchAmounts = new LinkedHashMap<>();

		MaterialsPluginData.Builder builder = MaterialsPluginData.builder();

		for (int i = 0; i < 20; i++) {
			BatchId batchId = new BatchId(i);
			MaterialId materialId = TestMaterialId.getRandomMaterialId(randomGenerator);
			double amount = randomGenerator.nextDouble();
			TestMaterialsProducerId testMaterialsProducerId = TestMaterialsProducerId
					.getRandomMaterialsProducerId(randomGenerator);
			builder.addBatch(batchId, materialId, amount);
			builder.addBatchToMaterialsProducerInventory(batchId, testMaterialsProducerId);
			expectedBatchAmounts.put(batchId, amount);
		}

		for (TestMaterialId testMaterialId : TestMaterialId.values()) {
			builder.addMaterial(testMaterialId);
		}

		for (TestMaterialsProducerId testMaterialsProducerId : TestMaterialsProducerId.values()) {
			builder.addMaterialsProducerId(testMaterialsProducerId);
		}

		MaterialsPluginData materialsInitialData = builder.build();

		for (BatchId batchId : expectedBatchAmounts.keySet()) {
			Double expectedValue = expectedBatchAmounts.get(batchId);
			Double actualAmount = materialsInitialData.getBatchAmount(batchId);
			assertEquals(expectedValue, actualAmount);
		}

		// precondition tests

		// if the batch id is null
		ContractException contractException = assertThrows(ContractException.class,
				() -> materialsInitialData.getBatchAmount(null));
		assertEquals(MaterialsError.NULL_BATCH_ID, contractException.getErrorType());

		// if the batch id is unknown
		contractException = assertThrows(ContractException.class,
				() -> materialsInitialData.getBatchAmount(new BatchId(10000000)));
		assertEquals(MaterialsError.UNKNOWN_BATCH_ID, contractException.getErrorType());

	}

	@Test
	@UnitTestMethod(target = MaterialsPluginData.class, name = "getBatchIds", args = {})
	public void testGetBatchIds() {

		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(1361793252807708004L);

		Set<BatchId> expectedBatchIds = new LinkedHashSet<>();

		MaterialsPluginData.Builder builder = MaterialsPluginData.builder();

		for (int i = 0; i < 20; i++) {
			BatchId batchId = new BatchId(i);
			MaterialId materialId = TestMaterialId.getRandomMaterialId(randomGenerator);
			double amount = randomGenerator.nextDouble();
			TestMaterialsProducerId testMaterialsProducerId = TestMaterialsProducerId
					.getRandomMaterialsProducerId(randomGenerator);
			builder.addBatch(batchId, materialId, amount);
			builder.addBatchToMaterialsProducerInventory(batchId, testMaterialsProducerId);
			expectedBatchIds.add(batchId);
		}

		for (TestMaterialId testMaterialId : TestMaterialId.values()) {
			builder.addMaterial(testMaterialId);
		}

		for (TestMaterialsProducerId testMaterialsProducerId : TestMaterialsProducerId.values()) {
			builder.addMaterialsProducerId(testMaterialsProducerId);
		}

		MaterialsPluginData materialsInitialData = builder.build();
		assertEquals(expectedBatchIds, materialsInitialData.getBatchIds());

	}

	@Test
	@UnitTestMethod(target = MaterialsPluginData.class, name = "getBatchMaterials", args = {})
	public void testGetBatchMaterials() {

		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(116943580559448312L);

		MaterialsPluginData.Builder builder = MaterialsPluginData.builder();

		Map<BatchId, MaterialId> expectedBatchMaterials = new LinkedHashMap<>();

		for (int i = 0; i < 20; i++) {
			BatchId batchId = new BatchId(i);
			MaterialId materialId = TestMaterialId.getRandomMaterialId(randomGenerator);
			double amount = randomGenerator.nextDouble();
			TestMaterialsProducerId testMaterialsProducerId = TestMaterialsProducerId
					.getRandomMaterialsProducerId(randomGenerator);
			builder.addBatch(batchId, materialId, amount);
			builder.addBatchToMaterialsProducerInventory(batchId, testMaterialsProducerId);
			expectedBatchMaterials.put(batchId, materialId);
		}

		for (TestMaterialId testMaterialId : TestMaterialId.values()) {
			builder.addMaterial(testMaterialId);
		}

		for (TestMaterialsProducerId testMaterialsProducerId : TestMaterialsProducerId.values()) {
			builder.addMaterialsProducerId(testMaterialsProducerId);
		}

		MaterialsPluginData materialsPluginData = builder.build();

		Map<BatchId, MaterialId> actualBatchMaterials = materialsPluginData.getBatchMaterials();
		assertEquals(expectedBatchMaterials, actualBatchMaterials);

	}

	@Test
	@UnitTestMethod(target = MaterialsPluginData.class, name = "getBatchMaterial", args = { BatchId.class })
	public void testGetBatchMaterial() {

		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(116943580559448312L);

		MaterialsPluginData.Builder builder = MaterialsPluginData.builder();

		Map<BatchId, MaterialId> expectedMaterialIds = new LinkedHashMap<>();

		for (int i = 0; i < 20; i++) {
			BatchId batchId = new BatchId(i);
			MaterialId materialId = TestMaterialId.getRandomMaterialId(randomGenerator);
			double amount = randomGenerator.nextDouble();
			TestMaterialsProducerId testMaterialsProducerId = TestMaterialsProducerId
					.getRandomMaterialsProducerId(randomGenerator);
			builder.addBatch(batchId, materialId, amount);
			builder.addBatchToMaterialsProducerInventory(batchId, testMaterialsProducerId);
			expectedMaterialIds.put(batchId, materialId);
		}

		for (TestMaterialId testMaterialId : TestMaterialId.values()) {
			builder.addMaterial(testMaterialId);
		}

		for (TestMaterialsProducerId testMaterialsProducerId : TestMaterialsProducerId.values()) {
			builder.addMaterialsProducerId(testMaterialsProducerId);
		}

		MaterialsPluginData materialsInitialData = builder.build();

		for (BatchId batchId : expectedMaterialIds.keySet()) {
			MaterialId expectedMaterialId = expectedMaterialIds.get(batchId);
			Object actualMaterialId = materialsInitialData.getBatchMaterial(batchId);
			assertEquals(expectedMaterialId, actualMaterialId);
		}

		// precondition tests

		// if the batch id is null
		ContractException contractException = assertThrows(ContractException.class,
				() -> materialsInitialData.getBatchMaterial(null));
		assertEquals(MaterialsError.NULL_BATCH_ID, contractException.getErrorType());

		// if the batch id is unknown
		contractException = assertThrows(ContractException.class,
				() -> materialsInitialData.getBatchMaterial(new BatchId(10000)));
		assertEquals(MaterialsError.UNKNOWN_BATCH_ID, contractException.getErrorType());

	}

	@Test
	@UnitTestMethod(target = MaterialsPluginData.class, name = "getMaterialsProducerInventoryBatches", args = {})
	public void testGetMaterialsProducerInventoryBatches() {

		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(4201153583410535220L);

		MaterialsPluginData.Builder builder = MaterialsPluginData.builder();

		Map<MaterialsProducerId, Set<BatchId>> expectedInventoryBatches = new LinkedHashMap<>();
		for (int i = 0; i < 20; i++) {
			BatchId batchId = new BatchId(i);
			MaterialId materialId = TestMaterialId.getRandomMaterialId(randomGenerator);
			double amount = randomGenerator.nextDouble();
			TestMaterialsProducerId testMaterialsProducerId = TestMaterialsProducerId
					.getRandomMaterialsProducerId(randomGenerator);
			builder.addBatch(batchId, materialId, amount);
			builder.addBatchToMaterialsProducerInventory(batchId, testMaterialsProducerId);

			Set<BatchId> set = expectedInventoryBatches.get(testMaterialsProducerId);
			if (set == null) {
				set = new LinkedHashSet<>();
				expectedInventoryBatches.put(testMaterialsProducerId, set);
			}
			set.add(batchId);
		}

		for (TestMaterialId testMaterialId : TestMaterialId.values()) {
			builder.addMaterial(testMaterialId);
		}

		for (TestMaterialsProducerId testMaterialsProducerId : TestMaterialsProducerId.values()) {
			builder.addMaterialsProducerId(testMaterialsProducerId);
		}

		MaterialsPluginData materialsInitialData = builder.build();

		Map<MaterialsProducerId, Set<BatchId>> actualInventoryBatches = materialsInitialData
				.getMaterialsProducerInventoryBatches();
		assertEquals(expectedInventoryBatches, actualInventoryBatches);

	}

	@Test
	@UnitTestMethod(target = MaterialsPluginData.class, name = "getMaterialsProducerInventoryBatches", args = {
			MaterialsProducerId.class })
	public void testGetMaterialsProducerInventoryBatches_ProducerId() {

		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(4201153583410535220L);

		MaterialsPluginData.Builder builder = MaterialsPluginData.builder();

		Map<MaterialsProducerId, Set<BatchId>> expectedBatchMap = new LinkedHashMap<>();
		for (TestMaterialsProducerId testMaterialsProducerId : TestMaterialsProducerId.values()) {
			expectedBatchMap.put(testMaterialsProducerId, new LinkedHashSet<>());
		}

		for (int i = 0; i < 20; i++) {
			BatchId batchId = new BatchId(i);
			MaterialId materialId = TestMaterialId.getRandomMaterialId(randomGenerator);
			double amount = randomGenerator.nextDouble();
			TestMaterialsProducerId testMaterialsProducerId = TestMaterialsProducerId
					.getRandomMaterialsProducerId(randomGenerator);
			builder.addBatch(batchId, materialId, amount);
			builder.addBatchToMaterialsProducerInventory(batchId, testMaterialsProducerId);
			expectedBatchMap.get(testMaterialsProducerId).add(batchId);
		}

		for (TestMaterialId testMaterialId : TestMaterialId.values()) {
			builder.addMaterial(testMaterialId);
		}

		for (TestMaterialsProducerId testMaterialsProducerId : TestMaterialsProducerId.values()) {
			builder.addMaterialsProducerId(testMaterialsProducerId);
		}

		MaterialsPluginData materialsInitialData = builder.build();

		for (TestMaterialsProducerId testMaterialsProducerId : TestMaterialsProducerId.values()) {
			Set<BatchId> expectedBatches = expectedBatchMap.get(testMaterialsProducerId);
			Set<BatchId> actualBatches = new LinkedHashSet<>(
					materialsInitialData.getMaterialsProducerInventoryBatches(testMaterialsProducerId));
			assertEquals(expectedBatches, actualBatches);
		}

		// precondition tests

		// if the materials producer id is null
		ContractException contractException = assertThrows(ContractException.class,
				() -> materialsInitialData.getMaterialsProducerInventoryBatches(null));
		assertEquals(MaterialsError.NULL_MATERIALS_PRODUCER_ID, contractException.getErrorType());

	}

	@Test
	@UnitTestMethod(target = MaterialsPluginData.class, name = "getBatchPropertyDefinitions", args = {})
	public void testGetBatchPropertyDefinitions() {

		Map<MaterialId, Map<BatchPropertyId, PropertyDefinition>> expectedBatchPropertyDefinitions = new LinkedHashMap<>();

		MaterialsPluginData.Builder builder = MaterialsPluginData.builder();//

		for (TestMaterialId testMaterialId : TestMaterialId.values()) {
			builder.addMaterial(testMaterialId);
		}

		for (TestBatchPropertyId testBatchPropertyId : TestBatchPropertyId.values()) {
			MaterialId materialId = testBatchPropertyId.getTestMaterialId();
			PropertyDefinition propertyDefinition = testBatchPropertyId.getPropertyDefinition();
			builder.defineBatchProperty(materialId, testBatchPropertyId, propertyDefinition);

			Map<BatchPropertyId, PropertyDefinition> map = expectedBatchPropertyDefinitions.get(materialId);
			if (map == null) {
				map = new LinkedHashMap<>();
				expectedBatchPropertyDefinitions.put(materialId, map);
			}
			map.put(testBatchPropertyId, propertyDefinition);

		}

		// build the MaterialsInitialization
		MaterialsPluginData materialsPluginData = builder.build();//

		Map<MaterialId, Map<BatchPropertyId, PropertyDefinition>> actualBatchPropertyDefinitions = materialsPluginData
				.getBatchPropertyDefinitions();
		assertEquals(expectedBatchPropertyDefinitions, actualBatchPropertyDefinitions);
	}

	@Test
	@UnitTestMethod(target = MaterialsPluginData.class, name = "getBatchPropertyDefinition", args = { MaterialId.class,
			BatchPropertyId.class })
	public void testGetBatchPropertyDefinition() {

		MaterialsPluginData.Builder builder = MaterialsPluginData.builder();//

		for (TestMaterialId testMaterialId : TestMaterialId.values()) {
			builder.addMaterial(testMaterialId);
		}

		for (TestBatchPropertyId testBatchPropertyId : TestBatchPropertyId.values()) {
			builder.defineBatchProperty(testBatchPropertyId.getTestMaterialId(), testBatchPropertyId,
					testBatchPropertyId.getPropertyDefinition());
		}

		// build the MaterialsInitialization
		MaterialsPluginData materialsInitialData = builder.build();//

		// show that the MaterialsInitialization returns the expected batch
		// property definitions
		for (TestBatchPropertyId testBatchPropertyId : TestBatchPropertyId.values()) {
			PropertyDefinition expectedPropertyDefinition = testBatchPropertyId.getPropertyDefinition();
			TestMaterialId testMaterialId = testBatchPropertyId.getTestMaterialId();
			PropertyDefinition actualPropertyDefinition = materialsInitialData
					.getBatchPropertyDefinition(testMaterialId, testBatchPropertyId);
			assertEquals(expectedPropertyDefinition, actualPropertyDefinition);
		}

		// precondition tests

		TestMaterialId testMaterialId = TestMaterialId.MATERIAL_2;
		TestBatchPropertyId testBatchPropertyId = TestBatchPropertyId.BATCH_PROPERTY_2_2_INTEGER_IMMUTABLE_TRACK;

		// if the material id is null
		ContractException contractException = assertThrows(ContractException.class,
				() -> materialsInitialData.getBatchPropertyDefinition(null, testBatchPropertyId));
		assertEquals(MaterialsError.NULL_MATERIAL_ID, contractException.getErrorType());

		// if the material id is unknown
		contractException = assertThrows(ContractException.class, () -> materialsInitialData
				.getBatchPropertyDefinition(TestMaterialId.getUnknownMaterialId(), testBatchPropertyId));
		assertEquals(MaterialsError.UNKNOWN_MATERIAL_ID, contractException.getErrorType());

		// if the batch property id is null
		contractException = assertThrows(ContractException.class,
				() -> materialsInitialData.getBatchPropertyDefinition(testMaterialId, null));
		assertEquals(PropertyError.NULL_PROPERTY_ID, contractException.getErrorType());

		// if the batch property id is unknown
		contractException = assertThrows(ContractException.class, () -> materialsInitialData
				.getBatchPropertyDefinition(testMaterialId, TestBatchPropertyId.getUnknownBatchPropertyId()));
		assertEquals(PropertyError.UNKNOWN_PROPERTY_ID, contractException.getErrorType());

	}

	@Test
	@UnitTestMethod(target = MaterialsPluginData.class, name = "getBatchPropertyIds", args = { MaterialId.class })
	public void testGetBatchPropertyIds() {

		MaterialsPluginData.Builder builder = MaterialsPluginData.builder();//

		for (TestMaterialId testMaterialId : TestMaterialId.values()) {
			builder.addMaterial(testMaterialId);
		}

		for (TestBatchPropertyId testBatchPropertyId : TestBatchPropertyId.values()) {
			builder.defineBatchProperty(testBatchPropertyId.getTestMaterialId(), testBatchPropertyId,
					testBatchPropertyId.getPropertyDefinition());
		}

		// build the MaterialsInitialization
		MaterialsPluginData materialsInitialData = builder.build();//

		// show that the MaterialsInitialization returns the expected batch
		// property ids

		for (TestMaterialId testMaterialId : TestMaterialId.values()) {
			Set<TestBatchPropertyId> expectedBatchPropertyIds = TestBatchPropertyId
					.getTestBatchPropertyIds(testMaterialId);
			Set<BatchPropertyId> actualBatchPropertyIds = materialsInitialData.getBatchPropertyIds(testMaterialId);
			assertEquals(expectedBatchPropertyIds, actualBatchPropertyIds);
		}

		// precondition tests

		// if the material id is null
		ContractException contractException = assertThrows(ContractException.class,
				() -> materialsInitialData.getBatchPropertyIds(null));
		assertEquals(MaterialsError.NULL_MATERIAL_ID, contractException.getErrorType());

		// if the material id is unknown
		contractException = assertThrows(ContractException.class,
				() -> materialsInitialData.getBatchPropertyIds(TestMaterialId.getUnknownMaterialId()));
		assertEquals(MaterialsError.UNKNOWN_MATERIAL_ID, contractException.getErrorType());

	}

	@Test
	@UnitTestMethod(target = MaterialsPluginData.class, name = "getBatchPropertyValues", args = { BatchId.class })
	public void testGetBatchPropertyValues_batchId() {

		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(4884114879424388887L);

		/*
		 * Add 30 batches with about half of the batch properties being set to
		 * randomized values and the other half set to the default for the property
		 * definition
		 */
		MaterialsPluginData.Builder builder = MaterialsPluginData.builder();//

		for (TestMaterialId testMaterialId : TestMaterialId.values()) {
			builder.addMaterial(testMaterialId);
		}
		for (TestBatchPropertyId testBatchPropertyId : TestBatchPropertyId.values()) {
			builder.defineBatchProperty(testBatchPropertyId.getTestMaterialId(), testBatchPropertyId,
					testBatchPropertyId.getPropertyDefinition());
		}
		for (TestMaterialsProducerId testMaterialsProducerId : TestMaterialsProducerId.values()) {
			builder.addMaterialsProducerId(testMaterialsProducerId);
		}

		// create a container to hold expected batch property values
		Map<MultiKey, Object> expectedBatchPropertyValues = new LinkedHashMap<>();

		// add the batches
		for (int i = 0; i < 30; i++) {
			BatchId batchId = new BatchId(i);
			TestMaterialId testMaterialId = TestMaterialId.getRandomMaterialId(randomGenerator);
			builder.addBatch(batchId, testMaterialId, randomGenerator.nextDouble());
			builder.addBatchToMaterialsProducerInventory(batchId,
					TestMaterialsProducerId.getRandomMaterialsProducerId(randomGenerator));

			for (TestBatchPropertyId testBatchPropertyId : TestBatchPropertyId
					.getTestBatchPropertyIds(testMaterialId)) {
				MultiKey multiKey = new MultiKey(batchId, testBatchPropertyId);

				boolean required = testBatchPropertyId.getPropertyDefinition().getDefaultValue().isEmpty();
				if (required || randomGenerator.nextBoolean()) {
					Object propertyValue = testBatchPropertyId.getRandomPropertyValue(randomGenerator);
					builder.setBatchPropertyValue(batchId, testBatchPropertyId, propertyValue);
					expectedBatchPropertyValues.put(multiKey, propertyValue);
				}
			}
		}

		// build the MaterialsInitialization
		MaterialsPluginData materialsInitialData = builder.build();//

		// show that the MaterialsInitialization returns the expected batch
		// property values
		for (MultiKey multiKey : expectedBatchPropertyValues.keySet()) {
			BatchId batchid = multiKey.getKey(0);
			BatchPropertyId batchPropertyId = multiKey.getKey(1);
			Object expectedValue = expectedBatchPropertyValues.get(multiKey);
			Object actualValue = materialsInitialData.getBatchPropertyValues(batchid).get(batchPropertyId);
			assertEquals(expectedValue, actualValue);
		}

		// precondition test: if the batch id is null
		ContractException contractException = assertThrows(ContractException.class,
				() -> materialsInitialData.getBatchPropertyValues(null));
		assertEquals(MaterialsError.NULL_BATCH_ID, contractException.getErrorType());

		// precondition test: if the batch id is unknown
		contractException = assertThrows(ContractException.class,
				() -> materialsInitialData.getBatchPropertyValues(new BatchId(10000)));
		assertEquals(MaterialsError.UNKNOWN_BATCH_ID, contractException.getErrorType());

	}

	@Test
	@UnitTestMethod(target = MaterialsPluginData.class, name = "getMaterialIds", args = {})
	public void testGetMaterialIds() {

		MaterialsPluginData.Builder builder = MaterialsPluginData.builder();

		for (TestMaterialId testMaterialId : TestMaterialId.values()) {
			builder.addMaterial(testMaterialId);
		}
		MaterialsPluginData materialsInitialData = builder.build();//
		assertEquals(EnumSet.allOf(TestMaterialId.class), materialsInitialData.getMaterialIds());

	}

	@Test
	@UnitTestMethod(target = MaterialsPluginData.class, name = "getMaterialsProducerIds", args = {})
	public void testGetMaterialsProducerIds() {

		MaterialsPluginData.Builder builder = MaterialsPluginData.builder();//
		for (TestMaterialsProducerId testMaterialsProducerId : TestMaterialsProducerId.values()) {
			builder.addMaterialsProducerId(testMaterialsProducerId);
		}
		MaterialsPluginData materialsInitialData = builder.build();//

		// show that the materials producer ids were added
		assertEquals(EnumSet.allOf(TestMaterialsProducerId.class), materialsInitialData.getMaterialsProducerIds());

	}

	@Test
	@UnitTestMethod(target = MaterialsPluginData.class, name = "getMaterialsProducerPropertyDefinitions", args = {})
	public void testGetMaterialsProducerPropertyDefinitions() {
		Map<MaterialsProducerPropertyId, PropertyDefinition> expectedPropertyDefinitions = new LinkedHashMap<>();
		MaterialsPluginData.Builder builder = MaterialsPluginData.builder();//

		for (TestMaterialsProducerPropertyId testMaterialsProducerPropertyId : TestMaterialsProducerPropertyId
				.values()) {
			PropertyDefinition propertyDefinition = testMaterialsProducerPropertyId.getPropertyDefinition();
			builder.defineMaterialsProducerProperty(testMaterialsProducerPropertyId, propertyDefinition);
			expectedPropertyDefinitions.put(testMaterialsProducerPropertyId, propertyDefinition);
		}
		MaterialsPluginData materialsPluginData = builder.build();//

		Map<MaterialsProducerPropertyId, PropertyDefinition> actualPropertyDefinitions = materialsPluginData
				.getMaterialsProducerPropertyDefinitions();
		assertEquals(expectedPropertyDefinitions, actualPropertyDefinitions);

	}

	@Test
	@UnitTestMethod(target = MaterialsPluginData.class, name = "getMaterialsProducerPropertyDefinition", args = {
			MaterialsProducerPropertyId.class })
	public void testGetMaterialsProducerPropertyDefinition() {

		MaterialsPluginData.Builder builder = MaterialsPluginData.builder();//

		for (TestMaterialsProducerPropertyId testMaterialsProducerPropertyId : TestMaterialsProducerPropertyId
				.values()) {
			builder.defineMaterialsProducerProperty(testMaterialsProducerPropertyId,
					testMaterialsProducerPropertyId.getPropertyDefinition());
		}
		MaterialsPluginData materialsInitialData = builder.build();//

		for (TestMaterialsProducerPropertyId testMaterialsProducerPropertyId : TestMaterialsProducerPropertyId
				.values()) {
			assertTrue(
					materialsInitialData.getMaterialsProducerPropertyIds().contains(testMaterialsProducerPropertyId));
			PropertyDefinition actualPropertyDefinition = materialsInitialData
					.getMaterialsProducerPropertyDefinition(testMaterialsProducerPropertyId);
			PropertyDefinition expectedPropertyDefinition = testMaterialsProducerPropertyId.getPropertyDefinition();
			assertEquals(expectedPropertyDefinition, actualPropertyDefinition);
		}

		// precondition tests

		// if the materials producer property id is null
		ContractException contractException = assertThrows(ContractException.class,
				() -> materialsInitialData.getMaterialsProducerPropertyDefinition(null));
		assertEquals(PropertyError.NULL_PROPERTY_ID, contractException.getErrorType());

		// if the materials producer property id is unknown
		contractException = assertThrows(ContractException.class,
				() -> materialsInitialData.getMaterialsProducerPropertyDefinition(
						TestMaterialsProducerPropertyId.getUnknownMaterialsProducerPropertyId()));
		assertEquals(PropertyError.UNKNOWN_PROPERTY_ID, contractException.getErrorType());

	}

	@Test
	@UnitTestMethod(target = MaterialsPluginData.class, name = "getMaterialsProducerPropertyIds", args = {})
	public void testGetMaterialsProducerPropertyIds() {
		MaterialsPluginData.Builder builder = MaterialsPluginData.builder();//

		for (TestMaterialsProducerPropertyId testMaterialsProducerPropertyId : TestMaterialsProducerPropertyId
				.values()) {
			builder.defineMaterialsProducerProperty(testMaterialsProducerPropertyId,
					testMaterialsProducerPropertyId.getPropertyDefinition());
		}
		MaterialsPluginData materialsInitialData = builder.build();//
		assertEquals(EnumSet.allOf(TestMaterialsProducerPropertyId.class),
				materialsInitialData.getMaterialsProducerPropertyIds());

	}

	@Test
	@UnitTestMethod(target = MaterialsPluginData.class, name = "getMaterialsProducerPropertyValues", args = {})
	public void testGetMaterialsProducerPropertyValues() {

		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(175219330466509056L);
		MaterialsPluginData.Builder builder = MaterialsPluginData.builder();//

		for (TestMaterialsProducerPropertyId testMaterialsProducerPropertyId : TestMaterialsProducerPropertyId
				.values()) {
			builder.defineMaterialsProducerProperty(testMaterialsProducerPropertyId,
					testMaterialsProducerPropertyId.getPropertyDefinition());
		}
		Map<MaterialsProducerId, Map<MaterialsProducerPropertyId, Object>> expectedProducerPropertyValues = new LinkedHashMap<>();

		for (TestMaterialsProducerId testMaterialsProducerId : TestMaterialsProducerId.values()) {
			builder.addMaterialsProducerId(testMaterialsProducerId);
			for (TestMaterialsProducerPropertyId testMaterialsProducerPropertyId : TestMaterialsProducerPropertyId
					.values()) {
				Object propertyValue;

				boolean required = testMaterialsProducerPropertyId.getPropertyDefinition().getDefaultValue().isEmpty();

				if (required || randomGenerator.nextBoolean()) {
					propertyValue = testMaterialsProducerPropertyId.getRandomPropertyValue(randomGenerator);
					builder.setMaterialsProducerPropertyValue(testMaterialsProducerId, testMaterialsProducerPropertyId,
							propertyValue);

					Map<MaterialsProducerPropertyId, Object> map = expectedProducerPropertyValues
							.get(testMaterialsProducerId);
					if (map == null) {
						map = new LinkedHashMap<>();
						expectedProducerPropertyValues.put(testMaterialsProducerId, map);
					}
					map.put(testMaterialsProducerPropertyId, propertyValue);
				}
			}
		}

		MaterialsPluginData materialsPluginData = builder.build();//

		Map<MaterialsProducerId, Map<MaterialsProducerPropertyId, Object>> actualProducerPropertyValues = materialsPluginData
				.getMaterialsProducerPropertyValues();

		assertEquals(expectedProducerPropertyValues, actualProducerPropertyValues);
	}

	@Test
	@UnitTestMethod(target = MaterialsPluginData.class, name = "getMaterialsProducerPropertyValues", args = {
			MaterialsProducerId.class })
	public void testGetMaterialsProducerPropertyValues_ProducerId() {

		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(175219330466509056L);
		MaterialsPluginData.Builder builder = MaterialsPluginData.builder();//

		for (TestMaterialsProducerPropertyId testMaterialsProducerPropertyId : TestMaterialsProducerPropertyId
				.values()) {
			builder.defineMaterialsProducerProperty(testMaterialsProducerPropertyId,
					testMaterialsProducerPropertyId.getPropertyDefinition());
		}

		Map<MultiKey, Object> expectedPropertyValues = new LinkedHashMap<>();

		for (TestMaterialsProducerId testMaterialsProducerId : TestMaterialsProducerId.values()) {
			builder.addMaterialsProducerId(testMaterialsProducerId);
			for (TestMaterialsProducerPropertyId testMaterialsProducerPropertyId : TestMaterialsProducerPropertyId
					.values()) {
				MultiKey multiKey = new MultiKey(testMaterialsProducerId, testMaterialsProducerPropertyId);
				Object propertyValue;

				boolean required = testMaterialsProducerPropertyId.getPropertyDefinition().getDefaultValue().isEmpty();

				if (required || randomGenerator.nextBoolean()) {
					propertyValue = testMaterialsProducerPropertyId.getRandomPropertyValue(randomGenerator);
					builder.setMaterialsProducerPropertyValue(testMaterialsProducerId, testMaterialsProducerPropertyId,
							propertyValue);
					expectedPropertyValues.put(multiKey, propertyValue);
				}
			}
		}

		MaterialsPluginData materialsInitialData = builder.build();//

		for (MultiKey multiKey : expectedPropertyValues.keySet()) {
			Object expectedValue = expectedPropertyValues.get(multiKey);
			TestMaterialsProducerId testMaterialsProducerId = multiKey.getKey(0);
			TestMaterialsProducerPropertyId testMaterialsProducerPropertyId = multiKey.getKey(1);
			Map<MaterialsProducerPropertyId, Object> materialsProducerPropertyValues = materialsInitialData
					.getMaterialsProducerPropertyValues(testMaterialsProducerId);
			Object actualValue = materialsProducerPropertyValues.get(testMaterialsProducerPropertyId);
			assertEquals(expectedValue, actualValue);
		}

		// precondition test: if the materials producer id is null
		ContractException contractException = assertThrows(ContractException.class,
				() -> materialsInitialData.getMaterialsProducerPropertyValues(null));
		assertEquals(MaterialsError.NULL_MATERIALS_PRODUCER_ID, contractException.getErrorType());

		// precondition test: if the materials producer id is unknown
		contractException = assertThrows(ContractException.class, () -> materialsInitialData
				.getMaterialsProducerPropertyValues(TestMaterialsProducerId.getUnknownMaterialsProducerId()));
		assertEquals(MaterialsError.UNKNOWN_MATERIALS_PRODUCER_ID, contractException.getErrorType());

	}

	@Test
	@UnitTestMethod(target = MaterialsPluginData.class, name = "getMaterialsProducerResourceLevels", args = {})
	public void testGetMaterialsProducerResourceLevels() {

		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(4448010834982849838L);

		Map<MaterialsProducerId, Map<ResourceId, Long>> expectedProducerResourceLevels = new LinkedHashMap<>();

		MaterialsPluginData.Builder builder = MaterialsPluginData.builder();
		for (TestMaterialsProducerId testMaterialsProducerId : TestMaterialsProducerId.values()) {
			builder.addMaterialsProducerId(testMaterialsProducerId);
			for (TestResourceId testResourceId : TestResourceId.values()) {

				long level = 0;
				if (randomGenerator.nextBoolean()) {
					level = randomGenerator.nextInt(100);
					builder.setMaterialsProducerResourceLevel(testMaterialsProducerId, testResourceId, level);

					Map<ResourceId, Long> map = expectedProducerResourceLevels.get(testMaterialsProducerId);
					if (map == null) {
						map = new LinkedHashMap<>();
						expectedProducerResourceLevels.put(testMaterialsProducerId, map);
					}
					map.put(testResourceId, level);
				}
			}
		}

		MaterialsPluginData materialsPluginData = builder.build();

		Map<MaterialsProducerId, Map<ResourceId, Long>> actualProducerResourceLevels = materialsPluginData
				.getMaterialsProducerResourceLevels();
		assertEquals(expectedProducerResourceLevels, actualProducerResourceLevels);
	}

	@Test
	@UnitTestMethod(target = MaterialsPluginData.class, name = "getMaterialsProducerResourceLevel", args = {
			MaterialsProducerId.class, ResourceId.class })
	public void testGetMaterialsProducerResourceLevel() {

		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(4448010834982849838L);

		Map<MultiKey, Long> expectedResourceLevels = new LinkedHashMap<>();

		MaterialsPluginData.Builder builder = MaterialsPluginData.builder();
		for (TestMaterialsProducerId testMaterialsProducerId : TestMaterialsProducerId.values()) {
			builder.addMaterialsProducerId(testMaterialsProducerId);
			for (TestResourceId testResourceId : TestResourceId.values()) {
				MultiKey multiKey = new MultiKey(testMaterialsProducerId, testResourceId);
				long level = 0;
				if (randomGenerator.nextBoolean()) {
					level = randomGenerator.nextInt(100);
					builder.setMaterialsProducerResourceLevel(testMaterialsProducerId, testResourceId, level);
				}
				expectedResourceLevels.put(multiKey, level);
			}
		}

		MaterialsPluginData materialsInitialData = builder.build();

		for (MultiKey multiKey : expectedResourceLevels.keySet()) {
			long expectedValue = expectedResourceLevels.get(multiKey);
			TestMaterialsProducerId testMaterialsProducerId = multiKey.getKey(0);
			TestResourceId testResourceId = multiKey.getKey(1);
			Long actualValue = materialsInitialData.getMaterialsProducerResourceLevel(testMaterialsProducerId,
					testResourceId);
			assertEquals(expectedValue, actualValue);
		}

		// precondition tests

		TestMaterialsProducerId testMaterialsProducerId = TestMaterialsProducerId.MATERIALS_PRODUCER_2;
		TestResourceId testResourceId = TestResourceId.RESOURCE_3;

		// if the materials producer id is null
		ContractException contractException = assertThrows(ContractException.class,
				() -> materialsInitialData.getMaterialsProducerResourceLevel(null, testResourceId));
		assertEquals(MaterialsError.NULL_MATERIALS_PRODUCER_ID, contractException.getErrorType());

		// if the materials producer id is unknown
		contractException = assertThrows(ContractException.class,
				() -> materialsInitialData.getMaterialsProducerResourceLevel(
						TestMaterialsProducerId.getUnknownMaterialsProducerId(), testResourceId));
		assertEquals(MaterialsError.UNKNOWN_MATERIALS_PRODUCER_ID, contractException.getErrorType());

		// if the resource id is unknown
		contractException = assertThrows(ContractException.class,
				() -> materialsInitialData.getMaterialsProducerResourceLevel(testMaterialsProducerId, null));
		assertEquals(ResourceError.NULL_RESOURCE_ID, contractException.getErrorType());

	}

	@Test
	@UnitTestMethod(target = MaterialsPluginData.class, name = "getStageBatches", args = {})
	public void testGetStageBatches() {

		MaterialsPluginData.Builder builder = MaterialsPluginData.builder();
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(273625089589349694L);

		// construct a container to hold the expected stage/batch relationships

		Map<StageId, Set<BatchId>> expectedStageBatches = new LinkedHashMap<>();

		// for each materials producer, add 50 batches, 10 stages and assign 0
		// to 3 batches per stage
		int stageIndex = 0;
		int batchIndex = 0;

		for (TestMaterialId testMaterialId : TestMaterialId.values()) {
			builder.addMaterial(testMaterialId);
		}

		for (TestMaterialsProducerId testMaterialsProducerId : TestMaterialsProducerId.values()) {

			builder.addMaterialsProducerId(testMaterialsProducerId);

			List<BatchId> stagedBatchIds = new ArrayList<>();
			for (int i = 0; i < 30; i++) {
				BatchId batchId = new BatchId(batchIndex++);
				stagedBatchIds.add(batchId);
				TestMaterialId testMaterialId = TestMaterialId.getRandomMaterialId(randomGenerator);
				double amount = randomGenerator.nextDouble();
				builder.addBatch(batchId, testMaterialId, amount);
			}

			for (int i = 0; i < 20; i++) {
				BatchId batchId = new BatchId(batchIndex++);
				TestMaterialId testMaterialId = TestMaterialId.getRandomMaterialId(randomGenerator);
				double amount = randomGenerator.nextDouble();
				builder.addBatch(batchId, testMaterialId, amount);
				builder.addBatchToMaterialsProducerInventory(batchId, testMaterialsProducerId);
			}

			List<StageId> stageIds = new ArrayList<>();
			for (int i = 0; i < 10; i++) {
				StageId stageId = new StageId(stageIndex++);
				stageIds.add(stageId);

				boolean offered = randomGenerator.nextBoolean();
				builder.addStage(stageId, offered);
				builder.addStageToMaterialProducer(stageId, testMaterialsProducerId);
			}

			for (BatchId batchId : stagedBatchIds) {
				StageId stageId = stageIds.get(randomGenerator.nextInt(stageIds.size()));
				builder.addBatchToStage(stageId, batchId);
				Set<BatchId> set = expectedStageBatches.get(stageId);
				if (set == null) {
					set = new LinkedHashSet<>();
					expectedStageBatches.put(stageId, set);
				}
				set.add(batchId);

			}
		}

		MaterialsPluginData materialsInitialData = builder.build();

		for (StageId stageId : expectedStageBatches.keySet()) {
			Set<BatchId> expectedBatches = expectedStageBatches.get(stageId);
			Set<BatchId> actualBatches = materialsInitialData.getStageBatches(stageId);
			assertEquals(expectedBatches, actualBatches);
		}

		Map<StageId, Set<BatchId>> actualStageBatches = materialsInitialData.getStageBatches();
		assertEquals(expectedStageBatches, actualStageBatches);
	}

	@Test
	@UnitTestMethod(target = MaterialsPluginData.class, name = "getStageBatches", args = { StageId.class })
	public void testGetStageBatches_StageId() {

		MaterialsPluginData.Builder builder = MaterialsPluginData.builder();
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(273625089589349694L);

		// construct a container to hold the expected stage/batch relationships

		Map<StageId, Set<BatchId>> expectedRelationships = new LinkedHashMap<>();

		// for each materials producer, add 50 batches, 10 stages and assign 0
		// to 3 batches per stage
		int stageIndex = 0;
		int batchIndex = 0;

		for (TestMaterialId testMaterialId : TestMaterialId.values()) {
			builder.addMaterial(testMaterialId);
		}

		for (TestMaterialsProducerId testMaterialsProducerId : TestMaterialsProducerId.values()) {

			builder.addMaterialsProducerId(testMaterialsProducerId);

			List<BatchId> stagedBatchIds = new ArrayList<>();
			for (int i = 0; i < 30; i++) {
				BatchId batchId = new BatchId(batchIndex++);
				stagedBatchIds.add(batchId);
				TestMaterialId testMaterialId = TestMaterialId.getRandomMaterialId(randomGenerator);
				double amount = randomGenerator.nextDouble();
				builder.addBatch(batchId, testMaterialId, amount);
			}

			for (int i = 0; i < 20; i++) {
				BatchId batchId = new BatchId(batchIndex++);
				TestMaterialId testMaterialId = TestMaterialId.getRandomMaterialId(randomGenerator);
				double amount = randomGenerator.nextDouble();
				builder.addBatch(batchId, testMaterialId, amount);
				builder.addBatchToMaterialsProducerInventory(batchId, testMaterialsProducerId);
			}

			List<StageId> stageIds = new ArrayList<>();
			for (int i = 0; i < 10; i++) {
				StageId stageId = new StageId(stageIndex++);
				stageIds.add(stageId);
				expectedRelationships.put(stageId, new LinkedHashSet<>());
				boolean offered = randomGenerator.nextBoolean();
				builder.addStage(stageId, offered);
				builder.addStageToMaterialProducer(stageId, testMaterialsProducerId);
			}

			for (BatchId batchId : stagedBatchIds) {
				StageId stageId = stageIds.get(randomGenerator.nextInt(stageIds.size()));
				expectedRelationships.get(stageId).add(batchId);
				builder.addBatchToStage(stageId, batchId);
			}
		}

		MaterialsPluginData materialsInitialData = builder.build();

		for (StageId stageId : expectedRelationships.keySet()) {
			Set<BatchId> expectedBatches = expectedRelationships.get(stageId);
			Set<BatchId> actualBatches = materialsInitialData.getStageBatches(stageId);
			assertEquals(expectedBatches, actualBatches);
		}

		// precondition tests

		// if the stage id is null
		ContractException contractException = assertThrows(ContractException.class,
				() -> materialsInitialData.getStageBatches(null));
		assertEquals(MaterialsError.NULL_STAGE_ID, contractException.getErrorType());

		// if the batch id is null
		contractException = assertThrows(ContractException.class,
				() -> materialsInitialData.getStageBatches(new StageId(10000000)));
		assertEquals(MaterialsError.UNKNOWN_STAGE_ID, contractException.getErrorType());

	}

	@Test
	@UnitTestMethod(target = MaterialsPluginData.class, name = "getStageIds", args = {})
	public void testGetStageIds() {

		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(3725911532254654669L);

		MaterialsPluginData.Builder builder = MaterialsPluginData.builder();

		for (TestMaterialsProducerId testMaterialsProducerId : TestMaterialsProducerId.values()) {
			builder.addMaterialsProducerId(testMaterialsProducerId);
		}
		Set<StageId> expectedStageIds = new LinkedHashSet<>();
		for (int i = 0; i < 100; i++) {
			StageId stageId = new StageId(i);
			expectedStageIds.add(stageId);
			boolean offered = randomGenerator.nextBoolean();
			TestMaterialsProducerId testMaterialsProducerId = TestMaterialsProducerId
					.getRandomMaterialsProducerId(randomGenerator);
			builder.addStage(stageId, offered);
			builder.addStageToMaterialProducer(stageId, testMaterialsProducerId);
		}

		MaterialsPluginData materialsInitialData = builder.build();//

		assertEquals(expectedStageIds, materialsInitialData.getStageIds());

	}

	@Test
	@UnitTestMethod(target = MaterialsPluginData.class, name = "getMaterialsProducerStages", args = {
			MaterialsProducerId.class })
	public void testGetMaterialsProducerStages_materialsProducerId() {

		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(4722411464538864709L);

		MaterialsPluginData.Builder builder = MaterialsPluginData.builder();

		Map<MaterialsProducerId, List<StageId>> expectedMap = new LinkedHashMap<>();

		for (TestMaterialsProducerId testMaterialsProducerId : TestMaterialsProducerId.values()) {
			builder.addMaterialsProducerId(testMaterialsProducerId);
			expectedMap.put(testMaterialsProducerId, new ArrayList<>());
		}

		for (int i = 0; i < 100; i++) {
			StageId stageId = new StageId(i);
			boolean offered = randomGenerator.nextBoolean();
			TestMaterialsProducerId testMaterialsProducerId = TestMaterialsProducerId
					.getRandomMaterialsProducerId(randomGenerator);

			builder.addStage(stageId, offered);
			builder.addStageToMaterialProducer(stageId, testMaterialsProducerId);

			expectedMap.get(testMaterialsProducerId).add(stageId);
		}

		MaterialsPluginData materialsInitialData = builder.build();//

		for (MaterialsProducerId materialsProducerId : expectedMap.keySet()) {
			List<StageId> expectedStages = expectedMap.get(materialsProducerId);
			assertEquals(expectedStages, materialsInitialData.getMaterialsProducerStages(materialsProducerId));
		}

		// precondition tests

		// if the stage id is null
		ContractException contractException = assertThrows(ContractException.class,
				() -> materialsInitialData.getMaterialsProducerStages(null));
		assertEquals(MaterialsError.NULL_MATERIALS_PRODUCER_ID, contractException.getErrorType());

		// if the stage id is unknown
		contractException = assertThrows(ContractException.class, () -> materialsInitialData
				.getMaterialsProducerStages(TestMaterialsProducerId.getUnknownMaterialsProducerId()));
		assertEquals(MaterialsError.UNKNOWN_MATERIALS_PRODUCER_ID, contractException.getErrorType());

	}

	@Test
	@UnitTestMethod(target = MaterialsPluginData.class, name = "getMaterialsProducerStages", args = {})
	public void testGetMaterialsProducerStages() {

		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(4722411464538864709L);

		MaterialsPluginData.Builder builder = MaterialsPluginData.builder();

		Map<MaterialsProducerId, Set<StageId>> expectedMaterialsProducerStages = new LinkedHashMap<>();

		for (TestMaterialsProducerId testMaterialsProducerId : TestMaterialsProducerId.values()) {
			builder.addMaterialsProducerId(testMaterialsProducerId);
		}

		for (int i = 0; i < 100; i++) {
			StageId stageId = new StageId(i);
			boolean offered = randomGenerator.nextBoolean();
			TestMaterialsProducerId testMaterialsProducerId = TestMaterialsProducerId
					.getRandomMaterialsProducerId(randomGenerator);

			builder.addStage(stageId, offered);
			builder.addStageToMaterialProducer(stageId, testMaterialsProducerId);

			Set<StageId> set = expectedMaterialsProducerStages.get(testMaterialsProducerId);
			if (set == null) {
				set = new LinkedHashSet<>();
				expectedMaterialsProducerStages.put(testMaterialsProducerId, set);
			}
			set.add(stageId);

		}

		MaterialsPluginData materialsPluginData = builder.build();//

		Map<MaterialsProducerId, Set<StageId>> acutalMaterialsProducerStages = materialsPluginData
				.getMaterialsProducerStages();
		assertEquals(expectedMaterialsProducerStages, acutalMaterialsProducerStages);
	}

	@Test
	@UnitTestMethod(target = MaterialsPluginData.class, name = "getStageOffers", args = {})
	public void testGetStageOffers() {

		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(1042601351499648378L);

		MaterialsPluginData.Builder builder = MaterialsPluginData.builder();

		for (TestMaterialsProducerId testMaterialsProducerId : TestMaterialsProducerId.values()) {
			builder.addMaterialsProducerId(testMaterialsProducerId);
		}
		Map<StageId, Boolean> expectedStageOffers = new LinkedHashMap<>();

		for (int i = 0; i < 100; i++) {
			StageId stageId = new StageId(i);
			boolean offered = randomGenerator.nextBoolean();
			TestMaterialsProducerId testMaterialsProducerId = TestMaterialsProducerId
					.getRandomMaterialsProducerId(randomGenerator);
			expectedStageOffers.put(stageId, offered);
			builder.addStage(stageId, offered);
			builder.addStageToMaterialProducer(stageId, testMaterialsProducerId);
		}

		MaterialsPluginData materialsPluginData = builder.build();//

		Map<StageId, Boolean> actualStageOffers = materialsPluginData.getStageOffers();

		assertEquals(expectedStageOffers, actualStageOffers);

	}

	@Test
	@UnitTestMethod(target = MaterialsPluginData.class, name = "isStageOffered", args = { StageId.class })
	public void testIsStageOffered() {

		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(1042601351499648378L);

		MaterialsPluginData.Builder builder = MaterialsPluginData.builder();

		for (TestMaterialsProducerId testMaterialsProducerId : TestMaterialsProducerId.values()) {
			builder.addMaterialsProducerId(testMaterialsProducerId);
		}

		Map<StageId, Boolean> expectedStageOffers = new LinkedHashMap<>();
		for (int i = 0; i < 100; i++) {
			StageId stageId = new StageId(i);
			boolean offered = randomGenerator.nextBoolean();
			TestMaterialsProducerId testMaterialsProducerId = TestMaterialsProducerId
					.getRandomMaterialsProducerId(randomGenerator);
			expectedStageOffers.put(stageId, offered);
			builder.addStage(stageId, offered);
			builder.addStageToMaterialProducer(stageId, testMaterialsProducerId);
		}

		MaterialsPluginData materialsInitialData = builder.build();//

		for (StageId stageId : expectedStageOffers.keySet()) {
			Boolean expectedOfferedState = expectedStageOffers.get(stageId);
			Boolean actualOfferedState = materialsInitialData.isStageOffered(stageId);
			assertEquals(expectedOfferedState, actualOfferedState);
		}

		// precondition tests

		// if the stage id is null
		ContractException contractException = assertThrows(ContractException.class,
				() -> materialsInitialData.isStageOffered(null));
		assertEquals(MaterialsError.NULL_STAGE_ID, contractException.getErrorType());

		// if the stage id is unknown
		contractException = assertThrows(ContractException.class,
				() -> materialsInitialData.isStageOffered(new StageId(10000000)));
		assertEquals(MaterialsError.UNKNOWN_STAGE_ID, contractException.getErrorType());
	}

	@Test
	@UnitTestMethod(target = MaterialsPluginData.class, name = "getResourceIds", args = {})
	public void testGetResourceIds() {

		MaterialsPluginData materialsInitialData = MaterialsPluginData.builder().build();//

		assertTrue(materialsInitialData.getResourceIds().isEmpty());

		TestMaterialsProducerId testMaterialsProducerId = TestMaterialsProducerId.MATERIALS_PRODUCER_1;
		long amount = 45L;

		MaterialsPluginData.Builder builder = MaterialsPluginData.builder();
		for (TestResourceId testResourceId : TestResourceId.values()) {
			builder.setMaterialsProducerResourceLevel(testMaterialsProducerId, testResourceId, amount++);
			testMaterialsProducerId = testMaterialsProducerId.next();
		}

		for (TestMaterialsProducerId producerId : TestMaterialsProducerId.values()) {
			builder.addMaterialsProducerId(producerId);
		}

		materialsInitialData = builder.build();

		assertEquals(EnumSet.allOf(TestResourceId.class), materialsInitialData.getResourceIds());
	}

	@Test
	@UnitTestMethod(target = MaterialsPluginData.class, name = "getCloneBuilder", args = {})
	public void testGetCloneBuilder() {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(1064212917574117854L);

		MaterialsPluginData.Builder builder = MaterialsPluginData.builder();

		for (TestMaterialsProducerId testMaterialsProducerId : TestMaterialsProducerId.values()) {
			builder.addMaterialsProducerId(testMaterialsProducerId);
		}
		for (TestMaterialsProducerPropertyId testMaterialsProducerPropertyId : TestMaterialsProducerPropertyId
				.values()) {
			builder.defineMaterialsProducerProperty(testMaterialsProducerPropertyId,
					testMaterialsProducerPropertyId.getPropertyDefinition());
		}
		for (TestMaterialsProducerId testMaterialsProducerId : TestMaterialsProducerId.values()) {
			for (TestMaterialsProducerPropertyId testMaterialsProducerPropertyId : TestMaterialsProducerPropertyId
					.values()) {
				boolean requiredProperty = testMaterialsProducerPropertyId.getPropertyDefinition().getDefaultValue()
						.isEmpty();
				if (requiredProperty || randomGenerator.nextBoolean()) {
					builder.setMaterialsProducerPropertyValue(testMaterialsProducerId, testMaterialsProducerPropertyId,
							testMaterialsProducerPropertyId.getRandomPropertyValue(randomGenerator));
				}
			}
		}
		Map<MaterialsProducerId, List<StageId>> stageMap = new LinkedHashMap<>();
		for (int i = 0; i < 30; i++) {
			boolean offered = i % 2 == 0;
			StageId stageId = new StageId(i);
			TestMaterialsProducerId materialsProducerId = TestMaterialsProducerId
					.getRandomMaterialsProducerId(randomGenerator);
			builder.addStage(stageId, offered);
			builder.addStageToMaterialProducer(stageId, materialsProducerId);
			List<StageId> list = stageMap.get(materialsProducerId);
			if (list == null) {
				list = new ArrayList<>();
				stageMap.put(materialsProducerId, list);
			}
			list.add(stageId);
		}

		for (TestBatchPropertyId testBatchPropertyId : TestBatchPropertyId.values()) {
			builder.defineBatchProperty(testBatchPropertyId.getTestMaterialId(), testBatchPropertyId,
					testBatchPropertyId.getPropertyDefinition());
		}

		for (TestMaterialId testMaterialId : TestMaterialId.values()) {
			builder.addMaterial(testMaterialId);
		}

		for (int i = 0; i < 150; i++) {
			BatchId batchId = new BatchId(i);
			TestMaterialsProducerId materialsProducerId = TestMaterialsProducerId
					.getRandomMaterialsProducerId(randomGenerator);
			TestMaterialId randomMaterialId = TestMaterialId.getRandomMaterialId(randomGenerator);
			builder.addBatch(batchId, randomMaterialId, randomGenerator.nextDouble());

			boolean stageFound = false;
			if (randomGenerator.nextBoolean()) {
				List<StageId> stages = stageMap.get(materialsProducerId);
				if (!stages.isEmpty()) {
					StageId stageId = stages.get(randomGenerator.nextInt(stages.size()));
					builder.addBatchToStage(stageId, batchId);
					stageFound = true;
				}
			}
			if (!stageFound) {
				builder.addBatchToMaterialsProducerInventory(batchId, materialsProducerId);
			}

			for (TestBatchPropertyId testBatchPropertyId : TestBatchPropertyId
					.getTestBatchPropertyIds(randomMaterialId)) {
				boolean required = testBatchPropertyId.getPropertyDefinition().getDefaultValue().isEmpty();
				if (required || randomGenerator.nextBoolean()) {
					builder.setBatchPropertyValue(batchId, testBatchPropertyId,
							testBatchPropertyId.getRandomPropertyValue(randomGenerator));
				}
			}
		}

		for (TestMaterialsProducerId testMaterialsProducerId : TestMaterialsProducerId.values()) {
			for (TestResourceId testResourceId : TestResourceId.values()) {
				if (randomGenerator.nextBoolean()) {
					int amount = randomGenerator.nextInt(10);
					builder.setMaterialsProducerResourceLevel(testMaterialsProducerId, testResourceId, amount);
				}
			}
		}

		MaterialsPluginData materialsPluginData = builder.build();
		// show the clone builder is not null
		PluginDataBuilder cloneBuilder = materialsPluginData.getCloneBuilder();
		assertNotNull(cloneBuilder);
		// show that the clone plugin data is not null
		PluginData pluginData = cloneBuilder.build();
		assertNotNull(pluginData);

		// show that the clone plugin data has the correct type
		assertTrue(pluginData instanceof MaterialsPluginData);

		MaterialsPluginData clonePluginData = (MaterialsPluginData) pluginData;

		assertEquals(materialsPluginData, clonePluginData);

	}

	@Test
	@UnitTestMethod(target = MaterialsPluginData.Builder.class, name = "setNextBatchRecordId", args = { int.class })
	public void testSetNextBatchRecordId() {

		for (int i = 0; i < 30; i++) {
			MaterialsPluginData materialsPluginData = MaterialsPluginData.builder().setNextBatchRecordId(i).build();
			assertEquals(i, materialsPluginData.getNextBatchRecordId());
		}

		// precondition test: if the next batch record id is negative
		ContractException contractException = assertThrows(ContractException.class,
				() -> MaterialsPluginData.builder().setNextBatchRecordId(-1));
		assertEquals(MaterialsError.NEGATIVE_BATCH_ID, contractException.getErrorType());
	}

	@Test
	@UnitTestMethod(target = MaterialsPluginData.class, name = "getNextBatchRecordId", args = {})
	public void testGetNextBatchRecordId() {

		assertEquals(0, MaterialsPluginData.builder().build().getNextBatchRecordId());

		for (int i = 0; i < 30; i++) {
			MaterialsPluginData materialsPluginData = MaterialsPluginData.builder().setNextBatchRecordId(i).build();
			assertEquals(i, materialsPluginData.getNextBatchRecordId());
		}
	}

	@Test
	@UnitTestMethod(target = MaterialsPluginData.Builder.class, name = "setNextStageRecordId", args = { int.class })
	public void testSetNextStageRecordId() {

		for (int i = 0; i < 30; i++) {
			MaterialsPluginData materialsPluginData = MaterialsPluginData.builder().setNextStageRecordId(i).build();
			assertEquals(i, materialsPluginData.getNextStageRecordId());
		}

		// precondition test: if the next batch record id is negative
		ContractException contractException = assertThrows(ContractException.class,
				() -> MaterialsPluginData.builder().setNextStageRecordId(-1));
		assertEquals(MaterialsError.NEGATIVE_STAGE_ID, contractException.getErrorType());
	}

	@Test
	@UnitTestMethod(target = MaterialsPluginData.class, name = "getNextStageRecordId", args = {})
	public void testGetNextStageRecordId() {

		assertEquals(0, MaterialsPluginData.builder().build().getNextStageRecordId());

		for (int i = 0; i < 30; i++) {
			MaterialsPluginData materialsPluginData = MaterialsPluginData.builder().setNextStageRecordId(i).build();
			assertEquals(i, materialsPluginData.getNextStageRecordId());
		}
	}

	@Test
	@UnitTestMethod(target = MaterialsPluginData.class, name = "getBatchPropertyValues", args = {})
	public void testGetBatchPropertyValues() {

		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(4884114879424388887L);

		/*
		 * Add 30 batches with about half of the batch properties being set to
		 * randomized values and the other half set to the default for the property
		 * definition
		 */
		MaterialsPluginData.Builder builder = MaterialsPluginData.builder();//

		for (TestMaterialId testMaterialId : TestMaterialId.values()) {
			builder.addMaterial(testMaterialId);
		}
		for (TestBatchPropertyId testBatchPropertyId : TestBatchPropertyId.values()) {
			builder.defineBatchProperty(testBatchPropertyId.getTestMaterialId(), testBatchPropertyId,
					testBatchPropertyId.getPropertyDefinition());
		}
		for (TestMaterialsProducerId testMaterialsProducerId : TestMaterialsProducerId.values()) {
			builder.addMaterialsProducerId(testMaterialsProducerId);
		}

		// create a container to hold expected batch property values
		Map<BatchId, Map<BatchPropertyId, Object>> expectedBatchPropertyValues = new LinkedHashMap<>();

		// add the batches
		for (int i = 0; i < 30; i++) {
			BatchId batchId = new BatchId(i);
			Map<BatchPropertyId, Object> propMap = new LinkedHashMap<>();

			TestMaterialId testMaterialId = TestMaterialId.getRandomMaterialId(randomGenerator);
			builder.addBatch(batchId, testMaterialId, randomGenerator.nextDouble());
			builder.addBatchToMaterialsProducerInventory(batchId,
					TestMaterialsProducerId.getRandomMaterialsProducerId(randomGenerator));

			for (TestBatchPropertyId testBatchPropertyId : TestBatchPropertyId
					.getTestBatchPropertyIds(testMaterialId)) {
				boolean required = testBatchPropertyId.getPropertyDefinition().getDefaultValue().isEmpty();
				if (required || randomGenerator.nextBoolean()) {
					Object propertyValue = testBatchPropertyId.getRandomPropertyValue(randomGenerator);
					builder.setBatchPropertyValue(batchId, testBatchPropertyId, propertyValue);
					propMap.put(testBatchPropertyId, propertyValue);
				}
			}
			if (!propMap.isEmpty()) {
				expectedBatchPropertyValues.put(batchId, propMap);
			}
		}

		// build the MaterialsInitialization
		MaterialsPluginData materialsPluginData = builder.build();//

		Map<BatchId, Map<BatchPropertyId, Object>> actualBatchPropertyValues = materialsPluginData
				.getBatchPropertyValues();

		// show that the MaterialsInitialization returns the expected batch
		// property values
		assertEquals(expectedBatchPropertyValues, actualBatchPropertyValues);
	}

	private MaterialsPluginData getRandomMaterialsPluginData(long seed) {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(seed);

		MaterialsPluginData.Builder builder = MaterialsPluginData.builder();//

		List<MaterialId> materialIds = new ArrayList<>();
		for (TestMaterialId testMaterialId : TestMaterialId.values()) {
			builder.addMaterial(testMaterialId);
			materialIds.add(testMaterialId);
			if (randomGenerator.nextBoolean()) {
				break;
			}
		}

		List<TestMaterialsProducerId> selectedProducerIds = new ArrayList<>();
		for (TestMaterialsProducerId testMaterialsProducerId : TestMaterialsProducerId.values()) {
			builder.addMaterialsProducerId(testMaterialsProducerId);
			selectedProducerIds.add(testMaterialsProducerId);
			if (randomGenerator.nextBoolean()) {
				break;
			}
		}
		List<StageId> stageIds = new ArrayList<>();
		int stageCount = randomGenerator.nextInt(5) + 3;
		for (int i = 0; i < stageCount; i++) {
			StageId stageId = new StageId(i);
			stageIds.add(stageId);
			builder.addStage(stageId, randomGenerator.nextBoolean());
			TestMaterialsProducerId testMaterialsProducerId = selectedProducerIds
					.get(randomGenerator.nextInt(selectedProducerIds.size()));
			builder.addStageToMaterialProducer(stageId, testMaterialsProducerId);
		}

		Map<BatchId, MaterialId> batchIds = new LinkedHashMap<>();
		int batchCount = randomGenerator.nextInt(15) + 5;
		for (int i = 0; i < batchCount; i++) {
			BatchId batchId = new BatchId(i);
			MaterialId materialId = materialIds.get(randomGenerator.nextInt(materialIds.size()));
			double amount = randomGenerator.nextDouble();
			builder.addBatch(batchId, materialId, amount);
			batchIds.put(batchId, materialId);
			if (randomGenerator.nextBoolean()) {
				TestMaterialsProducerId testMaterialsProducerId = selectedProducerIds
						.get(randomGenerator.nextInt(selectedProducerIds.size()));
				builder.addBatchToMaterialsProducerInventory(batchId, testMaterialsProducerId);
			} else {
				StageId stageId = stageIds.get(randomGenerator.nextInt(stageIds.size()));
				builder.addBatchToStage(stageId, batchId);
			}
		}

		builder.setNextBatchRecordId(batchCount + randomGenerator.nextInt(10));
		builder.setNextStageRecordId(stageCount + randomGenerator.nextInt(10));

		List<ResourceId> resourceIds = new ArrayList<>();
		for (TestResourceId testResourceId : TestResourceId.values()) {
			resourceIds.add(testResourceId);
			if (randomGenerator.nextBoolean()) {
				break;
			}
		}

		for (TestMaterialsProducerId testMaterialsProducerId : selectedProducerIds) {
			for (ResourceId resourceId : resourceIds) {
				long amount = randomGenerator.nextInt(1000);
				builder.setMaterialsProducerResourceLevel(testMaterialsProducerId, resourceId, amount);
			}
		}
		List<MaterialsProducerPropertyId> materialsProducerPropertyIds = new ArrayList<>();
		for (TestMaterialsProducerPropertyId testMaterialsProducerPropertyId : TestMaterialsProducerPropertyId
				.values()) {
			PropertyDefinition propertyDefinition = testMaterialsProducerPropertyId.getPropertyDefinition();
			materialsProducerPropertyIds.add(testMaterialsProducerPropertyId);
			builder.defineMaterialsProducerProperty(testMaterialsProducerPropertyId, propertyDefinition);
			boolean valueRequired = propertyDefinition.getDefaultValue().isEmpty();
			for (TestMaterialsProducerId testMaterialsProducerId : selectedProducerIds) {
				if (valueRequired || randomGenerator.nextBoolean()) {
					Object propertyValue = testMaterialsProducerPropertyId.getRandomPropertyValue(randomGenerator);
					builder.setMaterialsProducerPropertyValue(testMaterialsProducerId, testMaterialsProducerPropertyId,
							propertyValue);
				}
			}

			if (randomGenerator.nextBoolean()) {
				break;
			}
		}

		for (TestBatchPropertyId testBatchPropertyId : TestBatchPropertyId.values()) {
			MaterialId materialId = testBatchPropertyId.getTestMaterialId();
			if (!materialIds.contains(materialId)) {
				continue;
			}
			PropertyDefinition propertyDefinition = testBatchPropertyId.getPropertyDefinition();
			builder.defineBatchProperty(materialId, testBatchPropertyId, propertyDefinition);
			boolean valueRequired = propertyDefinition.getDefaultValue().isEmpty();

			for (BatchId batchId : batchIds.keySet()) {
				MaterialId mat = batchIds.get(batchId);
				if (mat.equals(materialId)) {
					if (valueRequired || randomGenerator.nextBoolean()) {
						Object propertyValue = testBatchPropertyId.getRandomBatchPropertyValue(randomGenerator);
						builder.setBatchPropertyValue(batchId, testBatchPropertyId, propertyValue);
					}
				}
			}

			if (randomGenerator.nextDouble() < 0.2) {
				break;
			}
		}

		return builder.build();

	}

	@Test
	@UnitTestMethod(target = MaterialsPluginData.class, name = "equals", args = { Object.class })
	public void testEquals() {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(215284709954874816L);

		// is never equal to null
		for (int i = 0; i < 10; i++) {
			MaterialsPluginData randomMaterialsPluginData = getRandomMaterialsPluginData(randomGenerator.nextLong());
			assertFalse(randomMaterialsPluginData.equals(null));
		}

		// reflexive
		for (int i = 0; i < 10; i++) {
			MaterialsPluginData randomMaterialsPluginData = getRandomMaterialsPluginData(randomGenerator.nextLong());
			assertTrue(randomMaterialsPluginData.equals(randomMaterialsPluginData));
		}

		// symmetric, transitive , consistent
		for (int i = 0; i < 10; i++) {
			long seed = randomGenerator.nextLong();
			MaterialsPluginData randomMaterialsPluginData1 = getRandomMaterialsPluginData(seed);
			MaterialsPluginData randomMaterialsPluginData2 = getRandomMaterialsPluginData(seed);
			for (int j = 0; j < 3; j++) {
				assertTrue(randomMaterialsPluginData1.equals(randomMaterialsPluginData2));
				assertTrue(randomMaterialsPluginData2.equals(randomMaterialsPluginData1));
			}
		}

		// changes to inputs yield unequal objects -- high probability that they are not
		// equal
		for (int i = 0; i < 10; i++) {
			MaterialsPluginData randomMaterialsPluginData1 = getRandomMaterialsPluginData(randomGenerator.nextLong());
			MaterialsPluginData randomMaterialsPluginData2 = getRandomMaterialsPluginData(randomGenerator.nextLong());
			assertNotEquals(randomMaterialsPluginData1, randomMaterialsPluginData2);
		}

	}

	@Test
	@UnitTestMethod(target = MaterialsPluginData.class, name = "hashCode", args = {})
	public void testHashCode() {
		// equal objects have equal hash codes
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(7949697829968462056L);
		for (int i = 0; i < 10; i++) {
			long seed = randomGenerator.nextLong();
			MaterialsPluginData randomMaterialsPluginData1 = getRandomMaterialsPluginData(seed);
			MaterialsPluginData randomMaterialsPluginData2 = getRandomMaterialsPluginData(seed);
			assertEquals(randomMaterialsPluginData1, randomMaterialsPluginData2);
			assertEquals(randomMaterialsPluginData1.hashCode(), randomMaterialsPluginData2.hashCode());
		}

		// show that hash codes are reasonably distributed
		Set<Integer> hashCodes = new LinkedHashSet<>();
		for (int i = 0; i < 100; i++) {
			MaterialsPluginData randomMaterialsPluginData = getRandomMaterialsPluginData(randomGenerator.nextLong());
			hashCodes.add(randomMaterialsPluginData.hashCode());
		}
		assertTrue(hashCodes.size() > 95);
	}

 
	@Test
	@UnitTestMethod(target = MaterialsPluginData.class, name = "toString", args = {})
	public void testToString() {
		MaterialsPluginData randomMaterialsPluginData = getRandomMaterialsPluginData(8064459530862960720L);

		//The expected value was manually verified
		String expectedValue = "MaterialsPluginData [data=Data [materialsProducerIds=[MATERIALS_PRODUCER_1], materialIds=[MATERIAL_1, MATERIAL_2], batchPropertyDefinitions={MATERIAL_1={BATCH_PROPERTY_1_1_BOOLEAN_IMMUTABLE_NO_TRACK=PropertyDefinition [type=class java.lang.Boolean, propertyValuesAreMutable=false, defaultValue=false], BATCH_PROPERTY_1_2_INTEGER_MUTABLE_NO_TRACK=PropertyDefinition [type=class java.lang.Integer, propertyValuesAreMutable=true, defaultValue=null], BATCH_PROPERTY_1_3_DOUBLE_MUTABLE_NO_TRACK=PropertyDefinition [type=class java.lang.Double, propertyValuesAreMutable=true, defaultValue=null]}, MATERIAL_2={BATCH_PROPERTY_2_1_BOOLEAN_MUTABLE_TRACK=PropertyDefinition [type=class java.lang.Boolean, propertyValuesAreMutable=true, defaultValue=false], BATCH_PROPERTY_2_2_INTEGER_IMMUTABLE_TRACK=PropertyDefinition [type=class java.lang.Integer, propertyValuesAreMutable=false, defaultValue=0], BATCH_PROPERTY_2_3_DOUBLE_MUTABLE_TRACK=PropertyDefinition [type=class java.lang.Double, propertyValuesAreMutable=true, defaultValue=0.0]}}, materialsProducerPropertyDefinitions={MATERIALS_PRODUCER_PROPERTY_1_BOOLEAN_MUTABLE_NO_TRACK=PropertyDefinition [type=class java.lang.Boolean, propertyValuesAreMutable=true, defaultValue=false], MATERIALS_PRODUCER_PROPERTY_2_INTEGER_MUTABLE_NO_TRACK=PropertyDefinition [type=class java.lang.Integer, propertyValuesAreMutable=true, defaultValue=null]}, materialsProducerPropertyValues={MATERIALS_PRODUCER_1={MATERIALS_PRODUCER_PROPERTY_1_BOOLEAN_MUTABLE_NO_TRACK=true, MATERIALS_PRODUCER_PROPERTY_2_INTEGER_MUTABLE_NO_TRACK=-1137930538}}, materialsProducerResourceLevels={MATERIALS_PRODUCER_1={RESOURCE_1=354}}, batchIds=[0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10], batchMaterials={0=MATERIAL_2, 1=MATERIAL_1, 2=MATERIAL_1, 3=MATERIAL_1, 4=MATERIAL_2, 5=MATERIAL_2, 6=MATERIAL_1, 7=MATERIAL_1, 8=MATERIAL_2, 9=MATERIAL_1, 10=MATERIAL_2}, batchAmounts={0=0.37700109658565584, 1=0.6121304249912323, 2=0.2327292009900117, 3=0.5848445304496632, 4=0.3626526310416065, 5=0.8141764231338207, 6=0.7282094630729463, 7=0.2911578167148421, 8=0.3813260302130712, 9=0.038100504160549775, 10=0.294860719409465}, materialsProducerInventoryBatches={MATERIALS_PRODUCER_1=[0, 1, 3, 6, 7, 9, 10]}, batchPropertyValues={1={BATCH_PROPERTY_1_1_BOOLEAN_IMMUTABLE_NO_TRACK=true, BATCH_PROPERTY_1_2_INTEGER_MUTABLE_NO_TRACK=-2092162941, BATCH_PROPERTY_1_3_DOUBLE_MUTABLE_NO_TRACK=0.580252301720191}, 3={BATCH_PROPERTY_1_1_BOOLEAN_IMMUTABLE_NO_TRACK=true, BATCH_PROPERTY_1_2_INTEGER_MUTABLE_NO_TRACK=1273654431, BATCH_PROPERTY_1_3_DOUBLE_MUTABLE_NO_TRACK=0.8140217670183427}, 6={BATCH_PROPERTY_1_1_BOOLEAN_IMMUTABLE_NO_TRACK=true, BATCH_PROPERTY_1_2_INTEGER_MUTABLE_NO_TRACK=1631575277, BATCH_PROPERTY_1_3_DOUBLE_MUTABLE_NO_TRACK=0.8263248505964975}, 9={BATCH_PROPERTY_1_1_BOOLEAN_IMMUTABLE_NO_TRACK=false, BATCH_PROPERTY_1_2_INTEGER_MUTABLE_NO_TRACK=1716954844, BATCH_PROPERTY_1_3_DOUBLE_MUTABLE_NO_TRACK=0.9459982578081394}, 2={BATCH_PROPERTY_1_2_INTEGER_MUTABLE_NO_TRACK=-1465030731, BATCH_PROPERTY_1_3_DOUBLE_MUTABLE_NO_TRACK=0.828428083842786}, 7={BATCH_PROPERTY_1_2_INTEGER_MUTABLE_NO_TRACK=-894472152, BATCH_PROPERTY_1_3_DOUBLE_MUTABLE_NO_TRACK=0.7150821622079484}, 4={BATCH_PROPERTY_2_1_BOOLEAN_MUTABLE_TRACK=true, BATCH_PROPERTY_2_2_INTEGER_IMMUTABLE_TRACK=771315814}, 8={BATCH_PROPERTY_2_1_BOOLEAN_MUTABLE_TRACK=true, BATCH_PROPERTY_2_2_INTEGER_IMMUTABLE_TRACK=-2000257358, BATCH_PROPERTY_2_3_DOUBLE_MUTABLE_TRACK=0.5783721359816127}, 10={BATCH_PROPERTY_2_1_BOOLEAN_MUTABLE_TRACK=true}}, stageIds=[0, 1, 2, 3, 4, 5], stageOffers={0=false, 1=true, 2=true, 3=false, 4=true, 5=true}, materialsProducerStages={MATERIALS_PRODUCER_1=[0, 1, 2, 3, 4, 5]}, stageBatches={5=[2], 4=[4], 3=[5], 2=[8]}, nextBatchRecordId=20, nextStageRecordId=14]]";
		
		String actualValue = randomMaterialsPluginData.toString();

		assertEquals(expectedValue, actualValue);

	}

}
