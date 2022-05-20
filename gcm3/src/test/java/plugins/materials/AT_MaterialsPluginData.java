package plugins.materials;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
import java.util.Random;
import java.util.Set;
import java.util.function.Supplier;

import org.apache.commons.math3.random.RandomGenerator;
import org.junit.jupiter.api.Test;

import nucleus.PluginData;
import nucleus.PluginDataBuilder;
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
import plugins.util.properties.TimeTrackingPolicy;
import tools.annotations.UnitTest;
import tools.annotations.UnitTestMethod;
import util.errors.ContractException;
import util.random.RandomGeneratorProvider;
import util.wrappers.MultiKey;

@UnitTest(target = MaterialsPluginData.class)
public class AT_MaterialsPluginData {

	@Test
	@UnitTestMethod(name = "builder", args = {})
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

		// precondition tests

		/*
		 * if a batch property is associated with a material id that was not
		 * properly added
		 */
		ContractException contractException = assertThrows(ContractException.class, () -> {
			MaterialId materialId = TestMaterialId.MATERIAL_1;
			TestBatchPropertyId propertyId = TestBatchPropertyId.BATCH_PROPERTY_1_1_BOOLEAN_IMMUTABLE_NO_TRACK;
			PropertyDefinition propertyDefinition = propertyId.getPropertyDefinition();
			MaterialsPluginData	.builder()//
								.defineBatchProperty(materialId, propertyId, propertyDefinition)//
								.build();//
		});
		assertEquals(MaterialsError.UNKNOWN_MATERIAL_ID, contractException.getErrorType());

		/*
		 * if a batch property is defined without a default value
		 */
		contractException = assertThrows(ContractException.class, () -> {
			MaterialId materialId = TestMaterialId.MATERIAL_1;
			TestBatchPropertyId propertyId = TestBatchPropertyId.BATCH_PROPERTY_1_1_BOOLEAN_IMMUTABLE_NO_TRACK;
			PropertyDefinition propertyDefinition = PropertyDefinition	.builder()//
																		.setType(Boolean.class)//
																		.setPropertyValueMutability(false)//
																		.setTimeTrackingPolicy(TimeTrackingPolicy.DO_NOT_TRACK_TIME)//
																		.build();//

			MaterialsPluginData	.builder()//
								.addMaterial(materialId)//
								.defineBatchProperty(materialId, propertyId, propertyDefinition).build();//
		});
		assertEquals(PropertyError.PROPERTY_DEFINITION_MISSING_DEFAULT, contractException.getErrorType());

		/*
		 * if a materials property value is associated with a materials producer
		 * id that was not properly added
		 */
		contractException = assertThrows(ContractException.class, () -> {
			MaterialsProducerId materialsProducerId = TestMaterialsProducerId.MATERIALS_PRODUCER_1;
			TestMaterialsProducerPropertyId propertyId = TestMaterialsProducerPropertyId.MATERIALS_PRODUCER_PROPERTY_1_BOOLEAN_MUTABLE_NO_TRACK;
			PropertyDefinition propertyDefinition = propertyId.getPropertyDefinition();//
			Object value = propertyId.getRandomPropertyValue(randomGenerator);

			MaterialsPluginData	.builder()//
								.defineMaterialsProducerProperty(propertyId, propertyDefinition)//
								.setMaterialsProducerPropertyValue(materialsProducerId, propertyId, value)//
								.build();//
		});
		assertEquals(MaterialsError.UNKNOWN_MATERIALS_PRODUCER_ID, contractException.getErrorType());

		/*
		 * if a materials property value is associated with a materials producer
		 * property id that was not properly defined
		 */
		contractException = assertThrows(ContractException.class, () -> {
			MaterialsProducerId materialsProducerId = TestMaterialsProducerId.MATERIALS_PRODUCER_1;
			TestMaterialsProducerPropertyId propertyId = TestMaterialsProducerPropertyId.MATERIALS_PRODUCER_PROPERTY_1_BOOLEAN_MUTABLE_NO_TRACK;
			Object value = propertyId.getRandomPropertyValue(randomGenerator);

			MaterialsPluginData	.builder()//
								.addMaterialsProducerId(materialsProducerId)//
								.setMaterialsProducerPropertyValue(materialsProducerId, propertyId, value)//
								.build();//
		});
		assertEquals(MaterialsError.UNKNOWN_MATERIALS_PRODUCER_PROPERTY_ID, contractException.getErrorType());

		/*
		 * if a materials property value is associated with a value that is not
		 * compatible with the corresponding property definition
		 * 
		 */

		contractException = assertThrows(ContractException.class, () -> {
			MaterialsProducerId materialsProducerId = TestMaterialsProducerId.MATERIALS_PRODUCER_1;
			TestMaterialsProducerPropertyId propertyId = TestMaterialsProducerPropertyId.MATERIALS_PRODUCER_PROPERTY_1_BOOLEAN_MUTABLE_NO_TRACK;
			PropertyDefinition propertyDefinition = propertyId.getPropertyDefinition();//
			Object value = 12;

			MaterialsPluginData	.builder()//
								.addMaterialsProducerId(materialsProducerId)//
								.defineMaterialsProducerProperty(propertyId, propertyDefinition)//
								.setMaterialsProducerPropertyValue(materialsProducerId, propertyId, value)//
								.build();//
		});
		assertEquals(MaterialsError.INCOMPATIBLE_MATERIALS_PRODUCER_PROPERTY_VALUE, contractException.getErrorType());

		/*
		 * if a materials property is defined without a default value and there
		 * is not an assigned property value for each added materials producer
		 */
		contractException = assertThrows(ContractException.class, () -> {
			MaterialsProducerId materialsProducerId = TestMaterialsProducerId.MATERIALS_PRODUCER_1;
			TestMaterialsProducerPropertyId propertyId = TestMaterialsProducerPropertyId.MATERIALS_PRODUCER_PROPERTY_1_BOOLEAN_MUTABLE_NO_TRACK;

			PropertyDefinition propertyDefinition = PropertyDefinition	.builder()//
																		.setType(Boolean.class)//
																		.setPropertyValueMutability(true)//
																		.setTimeTrackingPolicy(TimeTrackingPolicy.DO_NOT_TRACK_TIME)//
																		.build();

			MaterialsPluginData	.builder()//
								.addMaterialsProducerId(materialsProducerId)//
								.defineMaterialsProducerProperty(propertyId, propertyDefinition)//
								.build();//
		});
		assertEquals(MaterialsError.INSUFFICIENT_MATERIALS_PRODUCER_PROPERTY_VALUE_ASSIGNMENT, contractException.getErrorType());

		/*
		 * if a materials resource level is set for a material producer id that
		 * was not properly added
		 */

		contractException = assertThrows(ContractException.class, () -> {
			MaterialsProducerId materialsProducerId = TestMaterialsProducerId.MATERIALS_PRODUCER_1;
			TestResourceId testResourceId = TestResourceId.RESOURCE_2;
			Long resourceLevel = 10L;

			MaterialsPluginData	.builder()//
								.setMaterialsProducerResourceLevel(materialsProducerId, testResourceId, resourceLevel) //
								.build();//
		});
		assertEquals(MaterialsError.UNKNOWN_MATERIALS_PRODUCER_ID, contractException.getErrorType());

		/*
		 * if a batch is associated with at material that was not properly added
		 */
		contractException = assertThrows(ContractException.class, () -> {
			MaterialsProducerId materialsProducerId = TestMaterialsProducerId.MATERIALS_PRODUCER_1;
			TestMaterialId testMaterialId = TestMaterialId.MATERIAL_3;
			BatchId batchId = new BatchId(67);
			double amount = 345.543;

			MaterialsPluginData	.builder()//
								.addBatch(batchId, testMaterialId, amount, materialsProducerId)//
								.addMaterialsProducerId(materialsProducerId)//
								.build();//
		});
		assertEquals(MaterialsError.UNKNOWN_MATERIAL_ID, contractException.getErrorType());

		/*
		 * if a batch is associated with at material producer that was not
		 * properly added
		 */
		// MaterialsError.
		contractException = assertThrows(ContractException.class, () -> {
			MaterialsProducerId materialsProducerId = TestMaterialsProducerId.MATERIALS_PRODUCER_1;
			TestMaterialId testMaterialId = TestMaterialId.MATERIAL_3;
			BatchId batchId = new BatchId(67);
			double amount = 345.543;

			MaterialsPluginData	.builder()//
								.addBatch(batchId, testMaterialId, amount, materialsProducerId)//
								.addMaterial(testMaterialId)//
								.build();//
		});
		assertEquals(MaterialsError.UNKNOWN_MATERIALS_PRODUCER_ID, contractException.getErrorType());

		/*
		 * if a batch property is associated with batch id that was not properly
		 * added
		 */
		contractException = assertThrows(ContractException.class, () -> {

			TestBatchPropertyId testBatchPropertyId = TestBatchPropertyId.BATCH_PROPERTY_1_3_DOUBLE_MUTABLE_NO_TRACK;
			PropertyDefinition propertyDefinition = testBatchPropertyId.getPropertyDefinition();
			testBatchPropertyId.getRandomPropertyValue(randomGenerator);
			TestMaterialId testMaterialId = TestMaterialId.MATERIAL_1;

			BatchId batchId = new BatchId(67);

			MaterialsPluginData	.builder()//
								.defineBatchProperty(testMaterialId, testBatchPropertyId, propertyDefinition)//
								.addMaterial(testMaterialId)//
								.setBatchPropertyValue(batchId, testBatchPropertyId, batchId)//
								.build();//
		});
		assertEquals(MaterialsError.UNKNOWN_BATCH_ID, contractException.getErrorType());

		/*
		 * if a batch property is associated with batch property id that was not
		 * properly defined
		 */
		contractException = assertThrows(ContractException.class, () -> {
			TestMaterialsProducerId testMaterialsProducerId = TestMaterialsProducerId.MATERIALS_PRODUCER_2;
			TestBatchPropertyId testBatchPropertyId = TestBatchPropertyId.BATCH_PROPERTY_1_3_DOUBLE_MUTABLE_NO_TRACK;
			Object value = testBatchPropertyId.getRandomPropertyValue(randomGenerator);
			TestMaterialId testMaterialId = TestMaterialId.MATERIAL_1;

			BatchId batchId = new BatchId(67);
			double amount = 345.54;

			MaterialsPluginData	.builder()//
								.addBatch(batchId, testMaterialId, amount, testMaterialsProducerId)//
								.addMaterial(testMaterialId)//
								.setBatchPropertyValue(batchId, testBatchPropertyId, value)//
								.addMaterialsProducerId(testMaterialsProducerId)//
								.build();//
		});
		assertEquals(MaterialsError.UNKNOWN_BATCH_PROPERTY_ID, contractException.getErrorType());

		/*
		 * if a batch property value is incompatible with the corresponding
		 * property definition
		 */

		contractException = assertThrows(ContractException.class, () -> {
			TestMaterialsProducerId testMaterialsProducerId = TestMaterialsProducerId.MATERIALS_PRODUCER_2;
			TestBatchPropertyId testBatchPropertyId = TestBatchPropertyId.BATCH_PROPERTY_1_3_DOUBLE_MUTABLE_NO_TRACK;
			PropertyDefinition propertyDefinition = testBatchPropertyId.getPropertyDefinition();
			Object incompatibleValue = "bad value";
			TestMaterialId testMaterialId = TestMaterialId.MATERIAL_1;

			BatchId batchId = new BatchId(67);
			double amount = 345.54;

			MaterialsPluginData	.builder()//
								.addBatch(batchId, testMaterialId, amount, testMaterialsProducerId)//
								.addMaterial(testMaterialId)//
								.setBatchPropertyValue(batchId, testBatchPropertyId, incompatibleValue)//
								.addMaterialsProducerId(testMaterialsProducerId)//
								.defineBatchProperty(testMaterialId, testBatchPropertyId, propertyDefinition)//
								.build();//
		});
		assertEquals(MaterialsError.INCOMPATIBLE_BATCH_PROPERTY_VALUE, contractException.getErrorType());

		/*
		 * if a stage is associated with a materials producer id that was not
		 * properly added
		 */
		contractException = assertThrows(ContractException.class, () -> {
			TestMaterialsProducerId testMaterialsProducerId = TestMaterialsProducerId.MATERIALS_PRODUCER_2;
			StageId stageId = new StageId(543);
			boolean offered = false;

			MaterialsPluginData	.builder()//
								.addStage(stageId, offered, testMaterialsProducerId)//
								.build();//
		});
		assertEquals(MaterialsError.UNKNOWN_MATERIALS_PRODUCER_ID, contractException.getErrorType());

		/*
		 * if a batch is associated with a stage id that was not properly added
		 */

		contractException = assertThrows(ContractException.class, () -> {
			TestMaterialsProducerId testMaterialsProducerId = TestMaterialsProducerId.MATERIALS_PRODUCER_2;
			StageId stageId = new StageId(543);
			BatchId batchId = new BatchId(55);
			double amount = 86.0;
			TestMaterialId testMaterialId = TestMaterialId.MATERIAL_3;

			MaterialsPluginData	.builder()//
								.addBatch(batchId, testMaterialId, amount, testMaterialsProducerId)//
								.addBatchToStage(stageId, batchId)//
								.addMaterial(testMaterialId)//
								.addMaterialsProducerId(testMaterialsProducerId)//
								.build();//
		});
		assertEquals(MaterialsError.UNKNOWN_STAGE_ID, contractException.getErrorType());

		/*
		 * if a stage is associated with a batch id that was not properly added
		 */
		contractException = assertThrows(ContractException.class, () -> {
			TestMaterialsProducerId testMaterialsProducerId = TestMaterialsProducerId.MATERIALS_PRODUCER_2;
			StageId stageId = new StageId(543);
			BatchId batchId = new BatchId(55);
			TestMaterialId testMaterialId = TestMaterialId.MATERIAL_3;
			boolean offered = false;

			MaterialsPluginData	.builder()//
								.addStage(stageId, offered, testMaterialsProducerId)//
								.addBatchToStage(stageId, batchId)//
								.addMaterial(testMaterialId)//
								.addMaterialsProducerId(testMaterialsProducerId)//
								.build();//
		});
		assertEquals(MaterialsError.UNKNOWN_BATCH_ID, contractException.getErrorType());

		/*
		 * if a batch is associated with more than one stage
		 */
		contractException = assertThrows(ContractException.class, () -> {
			TestMaterialsProducerId testMaterialsProducerId = TestMaterialsProducerId.MATERIALS_PRODUCER_2;
			StageId stageId1 = new StageId(543);
			StageId stageId2 = new StageId(659);
			BatchId batchId = new BatchId(55);
			TestMaterialId testMaterialId = TestMaterialId.MATERIAL_3;
			boolean offered = false;
			double amount = 765.87;

			MaterialsPluginData	.builder()//
								.addBatch(batchId, testMaterialId, amount, testMaterialsProducerId)//
								.addStage(stageId1, offered, testMaterialsProducerId)//
								.addStage(stageId2, offered, testMaterialsProducerId)//
								.addBatchToStage(stageId1, batchId)//
								.addBatchToStage(stageId2, batchId)//
								.addMaterial(testMaterialId)//
								.addMaterialsProducerId(testMaterialsProducerId)//
								.build();//
		});
		assertEquals(MaterialsError.BATCH_ALREADY_STAGED, contractException.getErrorType());

		/*
		 * if a batch is associated with a stage that is not owned by the same
		 * materials producer as the batch
		 */
		// MaterialsError.
		contractException = assertThrows(ContractException.class, () -> {
			TestMaterialsProducerId testMaterialsProducerId1 = TestMaterialsProducerId.MATERIALS_PRODUCER_1;
			TestMaterialsProducerId testMaterialsProducerId2 = TestMaterialsProducerId.MATERIALS_PRODUCER_2;
			StageId stageId = new StageId(543);
			BatchId batchId = new BatchId(55);
			TestMaterialId testMaterialId = TestMaterialId.MATERIAL_3;
			boolean offered = false;
			double amount = 765.87;

			MaterialsPluginData	.builder()//
								.addBatch(batchId, testMaterialId, amount, testMaterialsProducerId1)//
								.addStage(stageId, offered, testMaterialsProducerId2)//
								.addBatchToStage(stageId, batchId)//
								.addMaterial(testMaterialId)//
								.addMaterialsProducerId(testMaterialsProducerId1)//
								.addMaterialsProducerId(testMaterialsProducerId2)//
								.build();//
		});
		assertEquals(MaterialsError.BATCH_STAGED_TO_DIFFERENT_OWNER, contractException.getErrorType());
	}

	@Test
	@UnitTestMethod(target = MaterialsPluginData.Builder.class, name = "addBatch", args = { BatchId.class, MaterialId.class, double.class, MaterialsProducerId.class })
	public void testAddBatch() {
		BatchId batchId = new BatchId(456);
		MaterialId materialId = TestMaterialId.MATERIAL_1;
		double amount = 16.7;
		MaterialsProducerId materialsProducerId = TestMaterialsProducerId.MATERIALS_PRODUCER_3;

		MaterialsPluginData materialsInitialData = MaterialsPluginData	.builder()//
																		.addBatch(batchId, materialId, amount, materialsProducerId)//
																		.addMaterial(materialId)//
																		.addMaterialsProducerId(materialsProducerId).build();//

		assertTrue(materialsInitialData.getBatchIds().contains(batchId));
		assertEquals(materialId, materialsInitialData.getBatchMaterial(batchId));
		assertEquals(amount, materialsInitialData.getBatchAmount(batchId));
		assertEquals(materialsProducerId, materialsInitialData.getBatchMaterialsProducer(batchId));

		// precondition tests

		// if the batch id is null
		ContractException contractException = assertThrows(ContractException.class, () -> MaterialsPluginData.builder().addBatch(null, materialId, amount, materialsProducerId));
		assertEquals(MaterialsError.NULL_BATCH_ID, contractException.getErrorType());

		// if the material id is null
		contractException = assertThrows(ContractException.class, () -> MaterialsPluginData.builder().addBatch(batchId, null, amount, materialsProducerId));
		assertEquals(MaterialsError.NULL_MATERIAL_ID, contractException.getErrorType());

		// if the material amount is infinite
		contractException = assertThrows(ContractException.class, () -> MaterialsPluginData.builder().addBatch(batchId, materialId, Double.POSITIVE_INFINITY, materialsProducerId));
		assertEquals(MaterialsError.NON_FINITE_MATERIAL_AMOUNT, contractException.getErrorType());

		// if the material amount is negative
		contractException = assertThrows(ContractException.class, () -> MaterialsPluginData.builder().addBatch(batchId, materialId, -1, materialsProducerId));
		assertEquals(MaterialsError.NEGATIVE_MATERIAL_AMOUNT, contractException.getErrorType());

		// if the materials producer id is null
		contractException = assertThrows(ContractException.class, () -> MaterialsPluginData.builder().addBatch(batchId, materialId, amount, null));
		assertEquals(MaterialsError.NULL_MATERIALS_PRODUCER_ID, contractException.getErrorType());

	}

	@Test
	@UnitTestMethod(target = MaterialsPluginData.Builder.class, name = "addBatchToStage", args = { StageId.class, BatchId.class })
	public void testAddBatchToStage() {
		BatchId batchId = new BatchId(456);
		StageId stageId = new StageId(543);
		MaterialId materialId = TestMaterialId.MATERIAL_1;
		double amount = 16.7;
		MaterialsProducerId materialsProducerId = TestMaterialsProducerId.MATERIALS_PRODUCER_3;

		MaterialsPluginData materialsInitialData = MaterialsPluginData	.builder()//
																		.addBatch(batchId, materialId, amount, materialsProducerId)//
																		.addBatchToStage(stageId, batchId)//
																		.addStage(stageId, false, materialsProducerId)//
																		.addMaterial(materialId)//
																		.addMaterialsProducerId(materialsProducerId)//
																		.build();//

		assertTrue(materialsInitialData.getStageBatches(stageId).contains(batchId));

		// precondition tests

		// if the stage id is null
		ContractException contractException = assertThrows(ContractException.class, () -> MaterialsPluginData.builder().addBatchToStage(null, batchId));
		assertEquals(MaterialsError.NULL_STAGE_ID, contractException.getErrorType());

		// if the batch id is null
		contractException = assertThrows(ContractException.class, () -> MaterialsPluginData.builder().addBatchToStage(stageId, null));
		assertEquals(MaterialsError.NULL_BATCH_ID, contractException.getErrorType());

	}

	@Test
	@UnitTestMethod(target = MaterialsPluginData.Builder.class, name = "addMaterial", args = { MaterialId.class })
	public void testAddMaterial() {

		MaterialId materialId = TestMaterialId.MATERIAL_1;

		MaterialsPluginData materialsInitialData = MaterialsPluginData	.builder()//
																		.addMaterial(materialId)//
																		.build();//

		assertTrue(materialsInitialData.getMaterialIds().contains(materialId));

		// precondition tests

		// if the material id is null
		ContractException contractException = assertThrows(ContractException.class, () -> MaterialsPluginData.builder().addMaterial(null));
		assertEquals(MaterialsError.NULL_MATERIAL_ID, contractException.getErrorType());

		// if the material was previously added
		contractException = assertThrows(ContractException.class, () -> {
			MaterialsPluginData.builder().addMaterial(materialId).addMaterial(materialId);
		});
		assertEquals(MaterialsError.DUPLICATE_MATERIAL, contractException.getErrorType());

	}

	@Test
	@UnitTestMethod(target = MaterialsPluginData.Builder.class, name = "addMaterialsProducerId", args = { MaterialsProducerId.class, Supplier.class })
	public void testAddMaterialsProducerId() {
		MaterialsProducerId materialsProducerId1 = TestMaterialsProducerId.MATERIALS_PRODUCER_1;
		MaterialsProducerId materialsProducerId2 = TestMaterialsProducerId.MATERIALS_PRODUCER_2;

		MaterialsPluginData materialsInitialData = MaterialsPluginData	.builder()//
																		.addMaterialsProducerId(materialsProducerId1)//
																		.addMaterialsProducerId(materialsProducerId2)//
																		.build();//

		// show that the materials producer ids were added
		assertTrue(materialsInitialData.getMaterialsProducerIds().contains(materialsProducerId1));

		assertTrue(materialsInitialData.getMaterialsProducerIds().contains(materialsProducerId2));

		// precondition tests

		// if the material id is null
		ContractException contractException = assertThrows(ContractException.class, () -> MaterialsPluginData.builder().addMaterialsProducerId(null));
		assertEquals(MaterialsError.NULL_MATERIALS_PRODUCER_ID, contractException.getErrorType());

	}

	@Test
	@UnitTestMethod(target = MaterialsPluginData.Builder.class, name = "addStage", args = { StageId.class, boolean.class, MaterialsProducerId.class })
	public void testAddStage() {
		StageId stageId = new StageId(456);
		boolean offered = true;
		MaterialsProducerId materialsProducerId = TestMaterialsProducerId.MATERIALS_PRODUCER_3;

		MaterialsPluginData materialsInitialData = MaterialsPluginData	.builder()//
																		.addStage(stageId, offered, materialsProducerId)//
																		.addMaterialsProducerId(materialsProducerId)//
																		.build();//
		assertTrue(materialsInitialData.getStageIds().contains(stageId));
		assertEquals(offered, materialsInitialData.isStageOffered(stageId));
		assertEquals(materialsProducerId, materialsInitialData.getStageMaterialsProducer(stageId));

		offered = false;
		materialsInitialData = MaterialsPluginData	.builder()//
													.addStage(stageId, offered, materialsProducerId)//
													.addMaterialsProducerId(materialsProducerId)//
													.build();//

		assertTrue(materialsInitialData.getStageIds().contains(stageId));
		assertEquals(offered, materialsInitialData.isStageOffered(stageId));
		assertEquals(materialsProducerId, materialsInitialData.getStageMaterialsProducer(stageId));

		// precondition tests

		// if the stage id is null
		ContractException contractException = assertThrows(ContractException.class, () -> MaterialsPluginData.builder().addStage(null, true, materialsProducerId));
		assertEquals(MaterialsError.NULL_STAGE_ID, contractException.getErrorType());

		// if the materials producer id is null
		contractException = assertThrows(ContractException.class, () -> MaterialsPluginData.builder().addStage(stageId, true, null));
		assertEquals(MaterialsError.NULL_MATERIALS_PRODUCER_ID, contractException.getErrorType());

	}

	@Test
	@UnitTestMethod(target = MaterialsPluginData.Builder.class, name = "defineBatchProperty", args = { MaterialId.class, BatchPropertyId.class, PropertyDefinition.class })
	public void testDefineBatchProperty() {

		MaterialsPluginData.Builder builder = MaterialsPluginData.builder();//
		for (TestMaterialId testMaterialId : TestMaterialId.values()) {
			builder.addMaterial(testMaterialId);
		}
		for (TestBatchPropertyId testBatchPropertyId : TestBatchPropertyId.values()) {
			builder.defineBatchProperty(testBatchPropertyId.getTestMaterialId(), testBatchPropertyId, testBatchPropertyId.getPropertyDefinition());
		}
		MaterialsPluginData materialsInitialData = builder.build();//

		for (TestBatchPropertyId testBatchPropertyId : TestBatchPropertyId.values()) {
			TestMaterialId testMaterialId = testBatchPropertyId.getTestMaterialId();
			assertTrue(materialsInitialData.getBatchPropertyIds(testMaterialId).contains(testBatchPropertyId));
			PropertyDefinition actualPropertyDefinition = materialsInitialData.getBatchPropertyDefinition(testMaterialId, testBatchPropertyId);
			PropertyDefinition expectedPropertyDefinition = testBatchPropertyId.getPropertyDefinition();
			assertEquals(expectedPropertyDefinition, actualPropertyDefinition);
		}

		// precondition tests

		TestBatchPropertyId testBatchPropertyId = TestBatchPropertyId.BATCH_PROPERTY_2_2_INTEGER_IMMUTABLE_TRACK;
		TestMaterialId testMaterialId = testBatchPropertyId.getTestMaterialId();
		PropertyDefinition propertyDefinition = testBatchPropertyId.getPropertyDefinition();

		// if the batch property id is null
		ContractException contractException = assertThrows(ContractException.class, () -> MaterialsPluginData.builder().defineBatchProperty(testMaterialId, null, propertyDefinition));
		assertEquals(MaterialsError.NULL_BATCH_PROPERTY_ID, contractException.getErrorType());

		// if the material id is null
		contractException = assertThrows(ContractException.class, () -> MaterialsPluginData.builder().defineBatchProperty(null, testBatchPropertyId, propertyDefinition));
		assertEquals(MaterialsError.NULL_MATERIAL_ID, contractException.getErrorType());

		// if the property definition is null
		contractException = assertThrows(ContractException.class, () -> MaterialsPluginData.builder().defineBatchProperty(testMaterialId, testBatchPropertyId, null));
		assertEquals(PropertyError.NULL_PROPERTY_DEFINITION, contractException.getErrorType());

		// if the property definition was previously defined
		contractException = assertThrows(ContractException.class, () -> {
			MaterialsPluginData	.builder()//
								.defineBatchProperty(testMaterialId, testBatchPropertyId, propertyDefinition)//
								.defineBatchProperty(testMaterialId, testBatchPropertyId, propertyDefinition);//
		});
		assertEquals(MaterialsError.DUPLICATE_BATCH_PROPERTY_DEFINITION, contractException.getErrorType());

	}

	@Test
	@UnitTestMethod(target = MaterialsPluginData.Builder.class, name = "defineMaterialsProducerProperty", args = { MaterialsProducerPropertyId.class, PropertyDefinition.class })
	public void testDefineMaterialsProducerProperty() {
		MaterialsPluginData.Builder builder = MaterialsPluginData.builder();//

		for (TestMaterialsProducerPropertyId testMaterialsProducerPropertyId : TestMaterialsProducerPropertyId.values()) {
			builder.defineMaterialsProducerProperty(testMaterialsProducerPropertyId, testMaterialsProducerPropertyId.getPropertyDefinition());
		}
		MaterialsPluginData materialsInitialData = builder.build();//

		for (TestMaterialsProducerPropertyId testMaterialsProducerPropertyId : TestMaterialsProducerPropertyId.values()) {
			assertTrue(materialsInitialData.getMaterialsProducerPropertyIds().contains(testMaterialsProducerPropertyId));
			PropertyDefinition actualPropertyDefinition = materialsInitialData.getMaterialsProducerPropertyDefinition(testMaterialsProducerPropertyId);
			PropertyDefinition expectedPropertyDefinition = testMaterialsProducerPropertyId.getPropertyDefinition();
			assertEquals(expectedPropertyDefinition, actualPropertyDefinition);
		}

		// precondition tests

		TestMaterialsProducerPropertyId testMaterialsProducerPropertyId = TestMaterialsProducerPropertyId.MATERIALS_PRODUCER_PROPERTY_3_DOUBLE_MUTABLE_NO_TRACK;

		PropertyDefinition propertyDefinition = testMaterialsProducerPropertyId.getPropertyDefinition();

		// if the materials producer property id is null
		ContractException contractException = assertThrows(ContractException.class, () -> MaterialsPluginData.builder().defineMaterialsProducerProperty(null, propertyDefinition));
		assertEquals(MaterialsError.NULL_MATERIALS_PRODUCER_PROPERTY_ID, contractException.getErrorType());

		// if the property definition is null
		contractException = assertThrows(ContractException.class, () -> MaterialsPluginData.builder().defineMaterialsProducerProperty(testMaterialsProducerPropertyId, null));
		assertEquals(PropertyError.NULL_PROPERTY_DEFINITION, contractException.getErrorType());

		// if the materials producer property was previously defined
		contractException = assertThrows(ContractException.class, () -> {
			MaterialsPluginData	.builder()//
								.defineMaterialsProducerProperty(testMaterialsProducerPropertyId, propertyDefinition)//
								.defineMaterialsProducerProperty(testMaterialsProducerPropertyId, propertyDefinition);//
		});
		assertEquals(MaterialsError.DUPLICATE_MATERIALS_PRODUCER_PROPERTY_DEFINITION, contractException.getErrorType());

	}

	@Test
	@UnitTestMethod(target = MaterialsPluginData.Builder.class, name = "setBatchPropertyValue", args = { BatchId.class, BatchPropertyId.class, Object.class })
	public void testSetBatchPropertyValue() {

		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(4884114879424388887L);

		/*
		 * Add 30 batches with about half of the batch properties being set to
		 * randomized values and the other half set to the default for the
		 * property definition
		 */
		MaterialsPluginData.Builder builder = MaterialsPluginData.builder();//

		for (TestMaterialId testMaterialId : TestMaterialId.values()) {
			builder.addMaterial(testMaterialId);
		}
		for (TestBatchPropertyId testBatchPropertyId : TestBatchPropertyId.values()) {
			builder.defineBatchProperty(testBatchPropertyId.getTestMaterialId(), testBatchPropertyId, testBatchPropertyId.getPropertyDefinition());
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
			builder.addBatch(batchId, testMaterialId, randomGenerator.nextDouble(), TestMaterialsProducerId.getRandomMaterialsProducerId(randomGenerator));
			for (TestBatchPropertyId testBatchPropertyId : TestBatchPropertyId.getTestBatchPropertyIds(testMaterialId)) {
				MultiKey multiKey = new MultiKey(batchId, testBatchPropertyId);
				Object propertyValue;
				if (randomGenerator.nextBoolean()) {
					propertyValue = testBatchPropertyId.getRandomPropertyValue(randomGenerator);
					builder.setBatchPropertyValue(batchId, testBatchPropertyId, propertyValue);

				} else {
					propertyValue = testBatchPropertyId.getPropertyDefinition().getDefaultValue().get();
				}
				expectedBatchPropertyValues.put(multiKey, propertyValue);
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
			Object actualValue = materialsInitialData.getBatchPropertyValue(batchid, batchPropertyId);
			assertEquals(expectedValue, actualValue);
		}

		// precondition tests

		BatchId batchId = new BatchId(0);
		TestBatchPropertyId testBatchPropertyId = TestBatchPropertyId.BATCH_PROPERTY_2_2_INTEGER_IMMUTABLE_TRACK;
		Object propertyValue = 17;

		// if the batch id is null
		ContractException contractException = assertThrows(ContractException.class, () -> MaterialsPluginData.builder().setBatchPropertyValue(null, testBatchPropertyId, propertyValue));
		assertEquals(MaterialsError.NULL_BATCH_ID, contractException.getErrorType());

		// if the batch property id is null
		contractException = assertThrows(ContractException.class, () -> MaterialsPluginData.builder().setBatchPropertyValue(batchId, null, propertyValue));
		assertEquals(MaterialsError.NULL_BATCH_PROPERTY_ID, contractException.getErrorType());

		// if the batch property value is null
		contractException = assertThrows(ContractException.class, () -> MaterialsPluginData.builder().setBatchPropertyValue(batchId, testBatchPropertyId, null));
		assertEquals(MaterialsError.NULL_BATCH_PROPERTY_VALUE, contractException.getErrorType());

		// if the batch property value was previously set
		contractException = assertThrows(ContractException.class, () -> {
			MaterialsPluginData	.builder()//
								.setBatchPropertyValue(batchId, testBatchPropertyId, propertyValue)//
								.setBatchPropertyValue(batchId, testBatchPropertyId, propertyValue);//
		});
		assertEquals(MaterialsError.DUPLICATE_BATCH_PROPERTY_VALUE_ASSIGNMENT, contractException.getErrorType());

	}

	@Test
	@UnitTestMethod(target = MaterialsPluginData.Builder.class, name = "setMaterialsProducerPropertyValue", args = { MaterialsProducerId.class, MaterialsProducerPropertyId.class, Object.class })
	public void testSetMaterialsProducerPropertyValue() {

		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(5680332692938057510L);
		MaterialsPluginData.Builder builder = MaterialsPluginData.builder();//

		for (TestMaterialsProducerPropertyId testMaterialsProducerPropertyId : TestMaterialsProducerPropertyId.values()) {
			builder.defineMaterialsProducerProperty(testMaterialsProducerPropertyId, testMaterialsProducerPropertyId.getPropertyDefinition());
		}

		Map<MultiKey, Object> expectedPropertyValues = new LinkedHashMap<>();

		for (TestMaterialsProducerId testMaterialsProducerId : TestMaterialsProducerId.values()) {
			builder.addMaterialsProducerId(testMaterialsProducerId);
			for (TestMaterialsProducerPropertyId testMaterialsProducerPropertyId : TestMaterialsProducerPropertyId.values()) {
				MultiKey multiKey = new MultiKey(testMaterialsProducerId, testMaterialsProducerPropertyId);
				Object propertyValue;
				if (randomGenerator.nextBoolean()) {
					propertyValue = testMaterialsProducerPropertyId.getRandomPropertyValue(randomGenerator);
					builder.setMaterialsProducerPropertyValue(testMaterialsProducerId, testMaterialsProducerPropertyId, propertyValue);
				} else {
					propertyValue = testMaterialsProducerPropertyId.getPropertyDefinition().getDefaultValue().get();
				}
				expectedPropertyValues.put(multiKey, propertyValue);
			}
		}

		MaterialsPluginData materialsInitialData = builder.build();//

		for (MultiKey multiKey : expectedPropertyValues.keySet()) {
			Object expectedValue = expectedPropertyValues.get(multiKey);
			TestMaterialsProducerId testMaterialsProducerId = multiKey.getKey(0);
			TestMaterialsProducerPropertyId testMaterialsProducerPropertyId = multiKey.getKey(1);
			Object actualValue = materialsInitialData.getMaterialsProducerPropertyValue(testMaterialsProducerId, testMaterialsProducerPropertyId);
			assertEquals(expectedValue, actualValue);
		}

		// precondition tests

		TestMaterialsProducerId testMaterialsProducerId = TestMaterialsProducerId.MATERIALS_PRODUCER_1;
		TestMaterialsProducerPropertyId testMaterialsProducerPropertyId = TestMaterialsProducerPropertyId.MATERIALS_PRODUCER_PROPERTY_3_DOUBLE_MUTABLE_NO_TRACK;
		Object propertyValue = 45.6;

		// if the materials producer id is null
		ContractException contractException = assertThrows(ContractException.class,
				() -> MaterialsPluginData.builder().setMaterialsProducerPropertyValue(null, testMaterialsProducerPropertyId, propertyValue));
		assertEquals(MaterialsError.NULL_MATERIALS_PRODUCER_ID, contractException.getErrorType());

		// if the materials producer property id is null
		contractException = assertThrows(ContractException.class, () -> MaterialsPluginData.builder().setMaterialsProducerPropertyValue(testMaterialsProducerId, null, propertyValue));
		assertEquals(MaterialsError.NULL_MATERIALS_PRODUCER_PROPERTY_ID, contractException.getErrorType());

		// if the materials producer property value is null
		contractException = assertThrows(ContractException.class,
				() -> MaterialsPluginData.builder().setMaterialsProducerPropertyValue(testMaterialsProducerId, testMaterialsProducerPropertyId, null));
		assertEquals(MaterialsError.NULL_MATERIALS_PRODUCER_PROPERTY_VALUE, contractException.getErrorType());

		// if the materials producer property value was previously set
		contractException = assertThrows(ContractException.class, () -> {
			MaterialsPluginData	.builder()//
								.setMaterialsProducerPropertyValue(testMaterialsProducerId, testMaterialsProducerPropertyId, propertyValue)//
								.setMaterialsProducerPropertyValue(testMaterialsProducerId, testMaterialsProducerPropertyId, propertyValue);
		});
		assertEquals(MaterialsError.DUPLICATE_MATERIALS_PRODUCER_PROPERTY_VALUE_ASSIGNMENT, contractException.getErrorType());

	}

	@Test
	@UnitTestMethod(target = MaterialsPluginData.Builder.class, name = "setMaterialsProducerResourceLevel", args = { MaterialsProducerId.class, ResourceId.class, long.class })
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
			Long actualValue = materialsInitialData.getMaterialsProducerResourceLevel(testMaterialsProducerId, testResourceId);
			assertEquals(expectedValue, actualValue);
		}

		// precondition tests

		TestMaterialsProducerId testMaterialsProducerId = TestMaterialsProducerId.MATERIALS_PRODUCER_2;
		TestResourceId testResourceId = TestResourceId.RESOURCE_3;
		long level = 345;

		// if the materials producer id is null
		ContractException contractException = assertThrows(ContractException.class, () -> MaterialsPluginData.builder().setMaterialsProducerResourceLevel(null, testResourceId, level));
		assertEquals(MaterialsError.NULL_MATERIALS_PRODUCER_ID, contractException.getErrorType());

		// if the resource id is null
		contractException = assertThrows(ContractException.class, () -> MaterialsPluginData.builder().setMaterialsProducerResourceLevel(testMaterialsProducerId, null, level));
		assertEquals(ResourceError.NULL_RESOURCE_ID, contractException.getErrorType());

		// if the resource amount is negative
		contractException = assertThrows(ContractException.class, () -> MaterialsPluginData.builder().setMaterialsProducerResourceLevel(testMaterialsProducerId, testResourceId, -1));
		assertEquals(ResourceError.NEGATIVE_RESOURCE_AMOUNT, contractException.getErrorType());

		// if the materials producer resource level was previously set
		contractException = assertThrows(ContractException.class, () -> {
			MaterialsPluginData	.builder()//
								.setMaterialsProducerResourceLevel(testMaterialsProducerId, testResourceId, level)//
								.setMaterialsProducerResourceLevel(testMaterialsProducerId, testResourceId, level);//
		});
		assertEquals(MaterialsError.DUPLICATE_MATERIALS_PRODUCER_RESOURCE_ASSIGNMENT, contractException.getErrorType());

	}

	@Test
	@UnitTestMethod(name = "getBatchAmount", args = { BatchId.class })
	public void testGetBatchAmount() {

		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(6746980823689022132L);

		Map<BatchId, Double> expectedBatchAmounts = new LinkedHashMap<>();

		MaterialsPluginData.Builder builder = MaterialsPluginData.builder();

		for (int i = 0; i < 20; i++) {
			BatchId batchId = new BatchId(i);
			MaterialId materialId = TestMaterialId.getRandomMaterialId(randomGenerator);
			double amount = randomGenerator.nextDouble();
			TestMaterialsProducerId testMaterialsProducerId = TestMaterialsProducerId.getRandomMaterialsProducerId(randomGenerator);
			builder.addBatch(batchId, materialId, amount, testMaterialsProducerId);
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
		ContractException contractException = assertThrows(ContractException.class, () -> materialsInitialData.getBatchAmount(null));
		assertEquals(MaterialsError.NULL_BATCH_ID, contractException.getErrorType());

		// if the batch id is unknown
		contractException = assertThrows(ContractException.class, () -> materialsInitialData.getBatchAmount(new BatchId(10000000)));
		assertEquals(MaterialsError.UNKNOWN_BATCH_ID, contractException.getErrorType());

	}

	@Test
	@UnitTestMethod(name = "getBatchIds", args = {})
	public void testGetBatchIds() {

		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(1361793252807708004L);

		Set<BatchId> expectedBatchIds = new LinkedHashSet<>();

		MaterialsPluginData.Builder builder = MaterialsPluginData.builder();

		for (int i = 0; i < 20; i++) {
			BatchId batchId = new BatchId(i);
			MaterialId materialId = TestMaterialId.getRandomMaterialId(randomGenerator);
			double amount = randomGenerator.nextDouble();
			TestMaterialsProducerId testMaterialsProducerId = TestMaterialsProducerId.getRandomMaterialsProducerId(randomGenerator);
			builder.addBatch(batchId, materialId, amount, testMaterialsProducerId);
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
	@UnitTestMethod(name = "getBatchMaterial", args = { BatchId.class })
	public void testGetBatchMaterial() {

		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(116943580559448312L);

		MaterialsPluginData.Builder builder = MaterialsPluginData.builder();

		Map<BatchId, MaterialId> expectedMaterialIds = new LinkedHashMap<>();

		for (int i = 0; i < 20; i++) {
			BatchId batchId = new BatchId(i);
			MaterialId materialId = TestMaterialId.getRandomMaterialId(randomGenerator);
			double amount = randomGenerator.nextDouble();
			TestMaterialsProducerId testMaterialsProducerId = TestMaterialsProducerId.getRandomMaterialsProducerId(randomGenerator);
			builder.addBatch(batchId, materialId, amount, testMaterialsProducerId);
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
		ContractException contractException = assertThrows(ContractException.class, () -> materialsInitialData.getBatchMaterial(null));
		assertEquals(MaterialsError.NULL_BATCH_ID, contractException.getErrorType());

		// if the batch id is unknown
		contractException = assertThrows(ContractException.class, () -> materialsInitialData.getBatchMaterial(new BatchId(10000)));
		assertEquals(MaterialsError.UNKNOWN_BATCH_ID, contractException.getErrorType());

	}

	@Test
	@UnitTestMethod(name = "getBatchMaterialsProducer", args = { BatchId.class })
	public void testGetBatchMaterialsProducer() {

		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(4201153583410535220L);

		MaterialsPluginData.Builder builder = MaterialsPluginData.builder();

		Map<BatchId, MaterialsProducerId> expectedMaterialProducerIds = new LinkedHashMap<>();

		for (int i = 0; i < 20; i++) {
			BatchId batchId = new BatchId(i);
			MaterialId materialId = TestMaterialId.getRandomMaterialId(randomGenerator);
			double amount = randomGenerator.nextDouble();
			TestMaterialsProducerId testMaterialsProducerId = TestMaterialsProducerId.getRandomMaterialsProducerId(randomGenerator);
			builder.addBatch(batchId, materialId, amount, testMaterialsProducerId);
			expectedMaterialProducerIds.put(batchId, testMaterialsProducerId);
		}

		for (TestMaterialId testMaterialId : TestMaterialId.values()) {
			builder.addMaterial(testMaterialId);
		}

		for (TestMaterialsProducerId testMaterialsProducerId : TestMaterialsProducerId.values()) {
			builder.addMaterialsProducerId(testMaterialsProducerId);
		}

		MaterialsPluginData materialsInitialData = builder.build();

		for (BatchId batchId : expectedMaterialProducerIds.keySet()) {
			MaterialsProducerId expectedMaterialsProducerId = expectedMaterialProducerIds.get(batchId);
			Object actualMaterialsProducerId = materialsInitialData.getBatchMaterialsProducer(batchId);
			assertEquals(expectedMaterialsProducerId, actualMaterialsProducerId);
		}

		// precondition tests

		// if the batch id is null
		ContractException contractException = assertThrows(ContractException.class, () -> materialsInitialData.getBatchMaterial(null));
		assertEquals(MaterialsError.NULL_BATCH_ID, contractException.getErrorType());

		// if the batch id is unknown
		contractException = assertThrows(ContractException.class, () -> materialsInitialData.getBatchMaterial(new BatchId(10000)));
		assertEquals(MaterialsError.UNKNOWN_BATCH_ID, contractException.getErrorType());
	}

	@Test
	@UnitTestMethod(name = "getBatchPropertyDefinition", args = { MaterialId.class, BatchPropertyId.class })
	public void testGetBatchPropertyDefinition() {

		MaterialsPluginData.Builder builder = MaterialsPluginData.builder();//

		for (TestMaterialId testMaterialId : TestMaterialId.values()) {
			builder.addMaterial(testMaterialId);
		}

		for (TestBatchPropertyId testBatchPropertyId : TestBatchPropertyId.values()) {
			builder.defineBatchProperty(testBatchPropertyId.getTestMaterialId(), testBatchPropertyId, testBatchPropertyId.getPropertyDefinition());
		}

		// build the MaterialsInitialization
		MaterialsPluginData materialsInitialData = builder.build();//

		// show that the MaterialsInitialization returns the expected batch
		// property definitions
		for (TestBatchPropertyId testBatchPropertyId : TestBatchPropertyId.values()) {
			PropertyDefinition expectedPropertyDefinition = testBatchPropertyId.getPropertyDefinition();
			TestMaterialId testMaterialId = testBatchPropertyId.getTestMaterialId();
			PropertyDefinition actualPropertyDefinition = materialsInitialData.getBatchPropertyDefinition(testMaterialId, testBatchPropertyId);
			assertEquals(expectedPropertyDefinition, actualPropertyDefinition);
		}

		// precondition tests

		TestMaterialId testMaterialId = TestMaterialId.MATERIAL_2;
		TestBatchPropertyId testBatchPropertyId = TestBatchPropertyId.BATCH_PROPERTY_2_2_INTEGER_IMMUTABLE_TRACK;

		// if the material id is null
		ContractException contractException = assertThrows(ContractException.class, () -> materialsInitialData.getBatchPropertyDefinition(null, testBatchPropertyId));
		assertEquals(MaterialsError.NULL_MATERIAL_ID, contractException.getErrorType());

		// if the material id is unknown
		contractException = assertThrows(ContractException.class, () -> materialsInitialData.getBatchPropertyDefinition(TestMaterialId.getUnknownMaterialId(), testBatchPropertyId));
		assertEquals(MaterialsError.UNKNOWN_MATERIAL_ID, contractException.getErrorType());

		// if the batch property id is null
		contractException = assertThrows(ContractException.class, () -> materialsInitialData.getBatchPropertyDefinition(testMaterialId, null));
		assertEquals(MaterialsError.NULL_BATCH_PROPERTY_ID, contractException.getErrorType());

		// if the batch property id is unknown
		contractException = assertThrows(ContractException.class, () -> materialsInitialData.getBatchPropertyDefinition(testMaterialId, TestBatchPropertyId.getUnknownBatchPropertyId()));
		assertEquals(MaterialsError.UNKNOWN_BATCH_PROPERTY_ID, contractException.getErrorType());

	}

	@Test
	@UnitTestMethod(name = "getBatchPropertyIds", args = { MaterialId.class })
	public void testGetBatchPropertyIds() {

		MaterialsPluginData.Builder builder = MaterialsPluginData.builder();//

		for (TestMaterialId testMaterialId : TestMaterialId.values()) {
			builder.addMaterial(testMaterialId);
		}

		for (TestBatchPropertyId testBatchPropertyId : TestBatchPropertyId.values()) {
			builder.defineBatchProperty(testBatchPropertyId.getTestMaterialId(), testBatchPropertyId, testBatchPropertyId.getPropertyDefinition());
		}

		// build the MaterialsInitialization
		MaterialsPluginData materialsInitialData = builder.build();//

		// show that the MaterialsInitialization returns the expected batch
		// property ids

		for (TestMaterialId testMaterialId : TestMaterialId.values()) {
			Set<TestBatchPropertyId> expectedBatchPropertyIds = TestBatchPropertyId.getTestBatchPropertyIds(testMaterialId);
			Set<BatchPropertyId> actualBatchPropertyIds = materialsInitialData.getBatchPropertyIds(testMaterialId);
			assertEquals(expectedBatchPropertyIds, actualBatchPropertyIds);
		}

		// precondition tests

		// if the material id is null
		ContractException contractException = assertThrows(ContractException.class, () -> materialsInitialData.getBatchPropertyIds(null));
		assertEquals(MaterialsError.NULL_MATERIAL_ID, contractException.getErrorType());

		// if the material id is unknown
		contractException = assertThrows(ContractException.class, () -> materialsInitialData.getBatchPropertyIds(TestMaterialId.getUnknownMaterialId()));
		assertEquals(MaterialsError.UNKNOWN_MATERIAL_ID, contractException.getErrorType());

	}

	@Test
	@UnitTestMethod(name = "getBatchPropertyValue", args = { BatchId.class, BatchPropertyId.class })
	public void testGetBatchPropertyValue() {

		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(4884114879424388887L);

		/*
		 * Add 30 batches with about half of the batch properties being set to
		 * randomized values and the other half set to the default for the
		 * property definition
		 */
		MaterialsPluginData.Builder builder = MaterialsPluginData.builder();//

		for (TestMaterialId testMaterialId : TestMaterialId.values()) {
			builder.addMaterial(testMaterialId);
		}
		for (TestBatchPropertyId testBatchPropertyId : TestBatchPropertyId.values()) {
			builder.defineBatchProperty(testBatchPropertyId.getTestMaterialId(), testBatchPropertyId, testBatchPropertyId.getPropertyDefinition());
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
			builder.addBatch(batchId, testMaterialId, randomGenerator.nextDouble(), TestMaterialsProducerId.getRandomMaterialsProducerId(randomGenerator));
			for (TestBatchPropertyId testBatchPropertyId : TestBatchPropertyId.getTestBatchPropertyIds(testMaterialId)) {
				MultiKey multiKey = new MultiKey(batchId, testBatchPropertyId);
				Object propertyValue;
				if (randomGenerator.nextBoolean()) {
					propertyValue = testBatchPropertyId.getRandomPropertyValue(randomGenerator);
					builder.setBatchPropertyValue(batchId, testBatchPropertyId, propertyValue);

				} else {
					propertyValue = testBatchPropertyId.getPropertyDefinition().getDefaultValue().get();
				}
				expectedBatchPropertyValues.put(multiKey, propertyValue);
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
			Object actualValue = materialsInitialData.getBatchPropertyValue(batchid, batchPropertyId);
			assertEquals(expectedValue, actualValue);
		}

		// precondition tests

		BatchId batchId = new BatchId(0);
		TestBatchPropertyId testBatchPropertyId = TestBatchPropertyId.BATCH_PROPERTY_2_2_INTEGER_IMMUTABLE_TRACK;

		// if the batch id is null
		ContractException contractException = assertThrows(ContractException.class, () -> materialsInitialData.getBatchPropertyValue(null, testBatchPropertyId));
		assertEquals(MaterialsError.NULL_BATCH_ID, contractException.getErrorType());

		// if the batch id is unknown
		contractException = assertThrows(ContractException.class, () -> materialsInitialData.getBatchPropertyValue(new BatchId(10000), testBatchPropertyId));
		assertEquals(MaterialsError.UNKNOWN_BATCH_ID, contractException.getErrorType());

		// if the batch property id is null
		contractException = assertThrows(ContractException.class, () -> materialsInitialData.getBatchPropertyValue(batchId, null));
		assertEquals(MaterialsError.NULL_BATCH_PROPERTY_ID, contractException.getErrorType());

		// if the batch property id is unknown</li>
		contractException = assertThrows(ContractException.class, () -> materialsInitialData.getBatchPropertyValue(batchId, TestBatchPropertyId.getUnknownBatchPropertyId()));
		assertEquals(MaterialsError.UNKNOWN_BATCH_PROPERTY_ID, contractException.getErrorType());

	}

	@Test
	@UnitTestMethod(name = "getMaterialIds", args = {})
	public void testGetMaterialIds() {

		MaterialsPluginData.Builder builder = MaterialsPluginData.builder();

		for (TestMaterialId testMaterialId : TestMaterialId.values()) {
			builder.addMaterial(testMaterialId);
		}
		MaterialsPluginData materialsInitialData = builder.build();//
		assertEquals(EnumSet.allOf(TestMaterialId.class), materialsInitialData.getMaterialIds());

	}

	@Test
	@UnitTestMethod(name = "getMaterialsProducerIds", args = {})
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
	@UnitTestMethod(name = "getMaterialsProducerPropertyDefinition", args = { MaterialsProducerPropertyId.class })
	public void testGetMaterialsProducerPropertyDefinition() {

		MaterialsPluginData.Builder builder = MaterialsPluginData.builder();//

		for (TestMaterialsProducerPropertyId testMaterialsProducerPropertyId : TestMaterialsProducerPropertyId.values()) {
			builder.defineMaterialsProducerProperty(testMaterialsProducerPropertyId, testMaterialsProducerPropertyId.getPropertyDefinition());
		}
		MaterialsPluginData materialsInitialData = builder.build();//

		for (TestMaterialsProducerPropertyId testMaterialsProducerPropertyId : TestMaterialsProducerPropertyId.values()) {
			assertTrue(materialsInitialData.getMaterialsProducerPropertyIds().contains(testMaterialsProducerPropertyId));
			PropertyDefinition actualPropertyDefinition = materialsInitialData.getMaterialsProducerPropertyDefinition(testMaterialsProducerPropertyId);
			PropertyDefinition expectedPropertyDefinition = testMaterialsProducerPropertyId.getPropertyDefinition();
			assertEquals(expectedPropertyDefinition, actualPropertyDefinition);
		}

		// precondition tests

		// if the materials producer property id is null
		ContractException contractException = assertThrows(ContractException.class, () -> materialsInitialData.getMaterialsProducerPropertyDefinition(null));
		assertEquals(MaterialsError.NULL_MATERIALS_PRODUCER_PROPERTY_ID, contractException.getErrorType());

		// if the materials producer property id is unknown
		contractException = assertThrows(ContractException.class,
				() -> materialsInitialData.getMaterialsProducerPropertyDefinition(TestMaterialsProducerPropertyId.getUnknownMaterialsProducerPropertyId()));
		assertEquals(MaterialsError.UNKNOWN_MATERIALS_PRODUCER_PROPERTY_ID, contractException.getErrorType());

	}

	@Test
	@UnitTestMethod(name = "getMaterialsProducerPropertyIds", args = {})
	public void testGetMaterialsProducerPropertyIds() {
		MaterialsPluginData.Builder builder = MaterialsPluginData.builder();//

		for (TestMaterialsProducerPropertyId testMaterialsProducerPropertyId : TestMaterialsProducerPropertyId.values()) {
			builder.defineMaterialsProducerProperty(testMaterialsProducerPropertyId, testMaterialsProducerPropertyId.getPropertyDefinition());
		}
		MaterialsPluginData materialsInitialData = builder.build();//
		assertEquals(EnumSet.allOf(TestMaterialsProducerPropertyId.class), materialsInitialData.getMaterialsProducerPropertyIds());

	}

	@Test
	@UnitTestMethod(name = "getMaterialsProducerPropertyValue", args = { MaterialsProducerId.class, MaterialsProducerPropertyId.class })
	public void testGetMaterialsProducerPropertyValue() {

		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(175219330466509056L);
		MaterialsPluginData.Builder builder = MaterialsPluginData.builder();//

		for (TestMaterialsProducerPropertyId testMaterialsProducerPropertyId : TestMaterialsProducerPropertyId.values()) {
			builder.defineMaterialsProducerProperty(testMaterialsProducerPropertyId, testMaterialsProducerPropertyId.getPropertyDefinition());
		}

		Map<MultiKey, Object> expectedPropertyValues = new LinkedHashMap<>();

		for (TestMaterialsProducerId testMaterialsProducerId : TestMaterialsProducerId.values()) {
			builder.addMaterialsProducerId(testMaterialsProducerId);
			for (TestMaterialsProducerPropertyId testMaterialsProducerPropertyId : TestMaterialsProducerPropertyId.values()) {
				MultiKey multiKey = new MultiKey(testMaterialsProducerId, testMaterialsProducerPropertyId);
				Object propertyValue;
				if (randomGenerator.nextBoolean()) {
					propertyValue = testMaterialsProducerPropertyId.getRandomPropertyValue(randomGenerator);
					builder.setMaterialsProducerPropertyValue(testMaterialsProducerId, testMaterialsProducerPropertyId, propertyValue);
				} else {
					propertyValue = testMaterialsProducerPropertyId.getPropertyDefinition().getDefaultValue().get();
				}
				expectedPropertyValues.put(multiKey, propertyValue);
			}
		}

		MaterialsPluginData materialsInitialData = builder.build();//

		for (MultiKey multiKey : expectedPropertyValues.keySet()) {
			Object expectedValue = expectedPropertyValues.get(multiKey);
			TestMaterialsProducerId testMaterialsProducerId = multiKey.getKey(0);
			TestMaterialsProducerPropertyId testMaterialsProducerPropertyId = multiKey.getKey(1);
			Object actualValue = materialsInitialData.getMaterialsProducerPropertyValue(testMaterialsProducerId, testMaterialsProducerPropertyId);
			assertEquals(expectedValue, actualValue);
		}

		// precondition tests

		TestMaterialsProducerId testMaterialsProducerId = TestMaterialsProducerId.MATERIALS_PRODUCER_1;
		TestMaterialsProducerPropertyId testMaterialsProducerPropertyId = TestMaterialsProducerPropertyId.MATERIALS_PRODUCER_PROPERTY_3_DOUBLE_MUTABLE_NO_TRACK;

		// if the materials producer id is null
		ContractException contractException = assertThrows(ContractException.class, () -> materialsInitialData.getMaterialsProducerPropertyValue(null, testMaterialsProducerPropertyId));
		assertEquals(MaterialsError.NULL_MATERIALS_PRODUCER_ID, contractException.getErrorType());

		// if the materials producer id is unknown
		contractException = assertThrows(ContractException.class,
				() -> materialsInitialData.getMaterialsProducerPropertyValue(TestMaterialsProducerId.getUnknownMaterialsProducerId(), testMaterialsProducerPropertyId));
		assertEquals(MaterialsError.UNKNOWN_MATERIALS_PRODUCER_ID, contractException.getErrorType());

		// if the materials producer property id is null
		contractException = assertThrows(ContractException.class, () -> materialsInitialData.getMaterialsProducerPropertyValue(testMaterialsProducerId, null));
		assertEquals(MaterialsError.NULL_MATERIALS_PRODUCER_PROPERTY_ID, contractException.getErrorType());

		// if the materials producer property id is unknown
		contractException = assertThrows(ContractException.class,
				() -> materialsInitialData.getMaterialsProducerPropertyValue(testMaterialsProducerId, TestMaterialsProducerPropertyId.getUnknownMaterialsProducerPropertyId()));
		assertEquals(MaterialsError.UNKNOWN_MATERIALS_PRODUCER_PROPERTY_ID, contractException.getErrorType());

	}

	@Test
	@UnitTestMethod(name = "getMaterialsProducerResourceLevel", args = { MaterialsProducerId.class, ResourceId.class })
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
			Long actualValue = materialsInitialData.getMaterialsProducerResourceLevel(testMaterialsProducerId, testResourceId);
			assertEquals(expectedValue, actualValue);
		}

		// precondition tests

		TestMaterialsProducerId testMaterialsProducerId = TestMaterialsProducerId.MATERIALS_PRODUCER_2;
		TestResourceId testResourceId = TestResourceId.RESOURCE_3;

		// if the materials producer id is null
		ContractException contractException = assertThrows(ContractException.class, () -> materialsInitialData.getMaterialsProducerResourceLevel(null, testResourceId));
		assertEquals(MaterialsError.NULL_MATERIALS_PRODUCER_ID, contractException.getErrorType());

		// if the materials producer id is unknown
		contractException = assertThrows(ContractException.class,
				() -> materialsInitialData.getMaterialsProducerResourceLevel(TestMaterialsProducerId.getUnknownMaterialsProducerId(), testResourceId));
		assertEquals(MaterialsError.UNKNOWN_MATERIALS_PRODUCER_ID, contractException.getErrorType());

		// if the resource id is unknown
		contractException = assertThrows(ContractException.class, () -> materialsInitialData.getMaterialsProducerResourceLevel(testMaterialsProducerId, null));
		assertEquals(ResourceError.NULL_RESOURCE_ID, contractException.getErrorType());

	}

	@Test
	@UnitTestMethod(name = "getStageBatches", args = { StageId.class })
	public void testGetStageBatches() {

		MaterialsPluginData.Builder builder = MaterialsPluginData.builder();
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(273625089589349694L);
		Random random = new Random(randomGenerator.nextLong());

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

			List<BatchId> batchIds = new ArrayList<>();
			for (int i = 0; i < 50; i++) {
				BatchId batchId = new BatchId(batchIndex++);
				batchIds.add(batchId);
				TestMaterialId testMaterialId = TestMaterialId.getRandomMaterialId(randomGenerator);
				double amount = randomGenerator.nextDouble();
				builder.addBatch(batchId, testMaterialId, amount, testMaterialsProducerId);
			}

			List<StageId> stageIds = new ArrayList<>();
			for (int i = 0; i < 10; i++) {
				StageId stageId = new StageId(stageIndex++);
				stageIds.add(stageId);
				expectedRelationships.put(stageId, new LinkedHashSet<>());
				boolean offered = randomGenerator.nextBoolean();
				builder.addStage(stageId, offered, testMaterialsProducerId);
			}

			Collections.shuffle(batchIds, random);
			for (int i = 0; i < 30; i++) {
				BatchId batchId = batchIds.get(i);
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
		ContractException contractException = assertThrows(ContractException.class, () -> materialsInitialData.getStageBatches(null));
		assertEquals(MaterialsError.NULL_STAGE_ID, contractException.getErrorType());

		// if the batch id is null
		contractException = assertThrows(ContractException.class, () -> materialsInitialData.getStageBatches(new StageId(10000000)));
		assertEquals(MaterialsError.UNKNOWN_STAGE_ID, contractException.getErrorType());

	}

	@Test
	@UnitTestMethod(name = "getStageIds", args = {})
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
			TestMaterialsProducerId testMaterialsProducerId = TestMaterialsProducerId.getRandomMaterialsProducerId(randomGenerator);
			builder.addStage(stageId, offered, testMaterialsProducerId);
		}

		MaterialsPluginData materialsInitialData = builder.build();//

		assertEquals(expectedStageIds, materialsInitialData.getStageIds());

	}

	@Test
	@UnitTestMethod(name = "getStageMaterialsProducer", args = { StageId.class })
	public void testGetStageMaterialsProducer() {

		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(4722411464538864709L);

		MaterialsPluginData.Builder builder = MaterialsPluginData.builder();

		for (TestMaterialsProducerId testMaterialsProducerId : TestMaterialsProducerId.values()) {
			builder.addMaterialsProducerId(testMaterialsProducerId);
		}

		Map<StageId, MaterialsProducerId> expectedMaterialsProducerIds = new LinkedHashMap<>();
		for (int i = 0; i < 100; i++) {
			StageId stageId = new StageId(i);
			boolean offered = randomGenerator.nextBoolean();
			TestMaterialsProducerId testMaterialsProducerId = TestMaterialsProducerId.getRandomMaterialsProducerId(randomGenerator);
			expectedMaterialsProducerIds.put(stageId, testMaterialsProducerId);
			builder.addStage(stageId, offered, testMaterialsProducerId);
		}

		MaterialsPluginData materialsInitialData = builder.build();//

		for (StageId stageId : expectedMaterialsProducerIds.keySet()) {
			MaterialsProducerId expectedMaterialsProducerId = expectedMaterialsProducerIds.get(stageId);
			MaterialsProducerId actualMaterialsProducerId = materialsInitialData.getStageMaterialsProducer(stageId);
			assertEquals(expectedMaterialsProducerId, actualMaterialsProducerId);
		}

		// precondition tests

		// if the stage id is null
		ContractException contractException = assertThrows(ContractException.class, () -> materialsInitialData.getStageMaterialsProducer(null));
		assertEquals(MaterialsError.NULL_STAGE_ID, contractException.getErrorType());

		// if the stage id is unknown
		contractException = assertThrows(ContractException.class, () -> materialsInitialData.getStageMaterialsProducer(new StageId(10000000)));
		assertEquals(MaterialsError.UNKNOWN_STAGE_ID, contractException.getErrorType());

	}

	@Test
	@UnitTestMethod(name = "isStageOffered", args = { StageId.class })
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
			TestMaterialsProducerId testMaterialsProducerId = TestMaterialsProducerId.getRandomMaterialsProducerId(randomGenerator);
			expectedStageOffers.put(stageId, offered);
			builder.addStage(stageId, offered, testMaterialsProducerId);
		}

		MaterialsPluginData materialsInitialData = builder.build();//

		for (StageId stageId : expectedStageOffers.keySet()) {
			Boolean expectedOfferedState = expectedStageOffers.get(stageId);
			Boolean actualOfferedState = materialsInitialData.isStageOffered(stageId);
			assertEquals(expectedOfferedState, actualOfferedState);
		}

		// precondition tests

		// if the stage id is null
		ContractException contractException = assertThrows(ContractException.class, () -> materialsInitialData.isStageOffered(null));
		assertEquals(MaterialsError.NULL_STAGE_ID, contractException.getErrorType());

		// if the stage id is unknown
		contractException = assertThrows(ContractException.class, () -> materialsInitialData.isStageOffered(new StageId(10000000)));
		assertEquals(MaterialsError.UNKNOWN_STAGE_ID, contractException.getErrorType());
	}

	@Test
	@UnitTestMethod(name = "getResourceIds", args = {})
	public void testGetResourceIds() {

		MaterialsPluginData.Builder builder = MaterialsPluginData.builder();

		MaterialsPluginData materialsInitialData = builder.build();//

		assertTrue(materialsInitialData.getResourceIds().isEmpty());

		TestMaterialsProducerId testMaterialsProducerId = TestMaterialsProducerId.MATERIALS_PRODUCER_1;
		long amount = 45L;
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
	@UnitTestMethod(name = "getCloneBuilder", args = {})
	public void testGetCloneBuilder() {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(1064212917574117854L);

		MaterialsPluginData.Builder builder = MaterialsPluginData.builder();

		for (TestMaterialsProducerId testMaterialsProducerId : TestMaterialsProducerId.values()) {
			builder.addMaterialsProducerId(testMaterialsProducerId);
		}
		for (TestMaterialsProducerPropertyId testMaterialsProducerPropertyId : TestMaterialsProducerPropertyId.values()) {
			builder.defineMaterialsProducerProperty(testMaterialsProducerPropertyId, testMaterialsProducerPropertyId.getPropertyDefinition());
		}
		for (TestMaterialsProducerId testMaterialsProducerId : TestMaterialsProducerId.values()) {
			for (TestMaterialsProducerPropertyId testMaterialsProducerPropertyId : TestMaterialsProducerPropertyId.values()) {
				if (randomGenerator.nextBoolean()) {
					builder.setMaterialsProducerPropertyValue(testMaterialsProducerId, testMaterialsProducerPropertyId, testMaterialsProducerPropertyId.getRandomPropertyValue(randomGenerator));
				}
			}
		}
		Map<MaterialsProducerId, List<StageId>> stageMap = new LinkedHashMap<>();
		for (int i = 0; i < 30; i++) {
			boolean offered = i % 2 == 0;
			StageId stageId = new StageId(i);
			TestMaterialsProducerId materialsProducerId = TestMaterialsProducerId.getRandomMaterialsProducerId(randomGenerator);
			builder.addStage(stageId, offered, materialsProducerId);
			List<StageId> list = stageMap.get(materialsProducerId);
			if (list == null) {
				list = new ArrayList<>();
				stageMap.put(materialsProducerId, list);
			}
			list.add(stageId);
		}

		for (TestBatchPropertyId testBatchPropertyId : TestBatchPropertyId.values()) {
			builder.defineBatchProperty(testBatchPropertyId.getTestMaterialId(), testBatchPropertyId, testBatchPropertyId.getPropertyDefinition());
		}

		for (TestMaterialId testMaterialId : TestMaterialId.values()) {
			builder.addMaterial(testMaterialId);
		}

		for (int i = 0; i < 150; i++) {
			BatchId batchId = new BatchId(i);
			TestMaterialsProducerId materialsProducerId = TestMaterialsProducerId.getRandomMaterialsProducerId(randomGenerator);
			TestMaterialId randomMaterialId = TestMaterialId.getRandomMaterialId(randomGenerator);
			builder.addBatch(batchId, randomMaterialId, randomGenerator.nextDouble(), materialsProducerId);
			if (randomGenerator.nextBoolean()) {
				List<StageId> stages = stageMap.get(materialsProducerId);
				if (!stages.isEmpty()) {
					StageId stageId = stages.get(randomGenerator.nextInt(stages.size()));
					builder.addBatchToStage(stageId, batchId);
				}
			}
			
			
			for (TestBatchPropertyId testBatchPropertyId : TestBatchPropertyId.getTestBatchPropertyIds(randomMaterialId)) {
				if (randomGenerator.nextBoolean()) {
					builder.setBatchPropertyValue(batchId, testBatchPropertyId, testBatchPropertyId.getRandomPropertyValue(randomGenerator));
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
		assertNotNull(false);

		// show that the clone plugin data has the correct type
		assertTrue(pluginData instanceof MaterialsPluginData);

		MaterialsPluginData clonePluginData = (MaterialsPluginData) pluginData;

		// show that the two plugin datas have the same material producer ids
		assertEquals(materialsPluginData.getMaterialsProducerIds(), clonePluginData.getMaterialsProducerIds());

		// show that the two plugin datas have the same material producer
		// property ids
		assertEquals(materialsPluginData.getMaterialsProducerPropertyIds(), clonePluginData.getMaterialsProducerPropertyIds());

		// show that the two plugin datas have the same material producer
		// property definitions
		for (MaterialsProducerPropertyId materialsProducerPropertyId : materialsPluginData.getMaterialsProducerPropertyIds()) {
			PropertyDefinition expectedPropertyDefinition = materialsPluginData.getMaterialsProducerPropertyDefinition(materialsProducerPropertyId);
			PropertyDefinition actualPropertyDefinition = clonePluginData.getMaterialsProducerPropertyDefinition(materialsProducerPropertyId);
			assertEquals(expectedPropertyDefinition, actualPropertyDefinition);
		}

		// show that the two plugin datas have the same material producer
		// property values
		for (MaterialsProducerId materialsProducerId : materialsPluginData.getMaterialsProducerIds()) {
			for (MaterialsProducerPropertyId materialsProducerPropertyId : materialsPluginData.getMaterialsProducerPropertyIds()) {
				Object expectedValue = materialsPluginData.getMaterialsProducerPropertyValue(materialsProducerId, materialsProducerPropertyId);
				Object actualValue = clonePluginData.getMaterialsProducerPropertyValue(materialsProducerId, materialsProducerPropertyId);
				assertEquals(expectedValue, actualValue);
			}
		}

		// show that the two plugin datas have the same resource ids
		assertEquals(materialsPluginData.getResourceIds(), clonePluginData.getResourceIds());

		// show that the two plugin datas have the same stage ids
		assertEquals(materialsPluginData.getStageIds(), clonePluginData.getStageIds());

		// show that the two plugin datas have the same stage offer states
		for (StageId stageId : materialsPluginData.getStageIds()) {
			assertEquals(materialsPluginData.isStageOffered(stageId), clonePluginData.isStageOffered(stageId));
		}

		// show that the two plugin datas have the same stage material producers
		for (StageId stageId : materialsPluginData.getStageIds()) {
			MaterialsProducerId expectedMaterialsProducerId = materialsPluginData.getStageMaterialsProducer(stageId);
			MaterialsProducerId actualMaterialsProducerId = clonePluginData.getStageMaterialsProducer(stageId);
			assertEquals(expectedMaterialsProducerId, actualMaterialsProducerId);
		}

		// show that the two plugin datas have the same stage batches
		for (StageId stageId : materialsPluginData.getStageIds()) {
			Set<BatchId> expectedStageBatches = materialsPluginData.getStageBatches(stageId);
			Set<BatchId> actualStageBatches = clonePluginData.getStageBatches(stageId);
			assertEquals(expectedStageBatches, actualStageBatches);
		}

		// show that the two plugin datas have the same material ids
		assertEquals(materialsPluginData.getMaterialIds(), clonePluginData.getMaterialIds());

		// show that the two plugin datas have the same material producer
		// resource levels
		for (TestResourceId testResourceId : TestResourceId.values()) {
			for (MaterialsProducerId materialsProducerId : materialsPluginData.getMaterialsProducerIds()) {
				Long expectedLevel = materialsPluginData.getMaterialsProducerResourceLevel(materialsProducerId, testResourceId);
				Long actualLevel = clonePluginData.getMaterialsProducerResourceLevel(materialsProducerId, testResourceId);
				assertEquals(expectedLevel, actualLevel);
			}
		}

		// show that the two plugin datas have the same batch property ids
		for (TestMaterialId testMaterialId : TestMaterialId.values()) {
			assertEquals(materialsPluginData.getBatchPropertyIds(testMaterialId), clonePluginData.getBatchPropertyIds(testMaterialId));
		}

		// show that the two plugin datas have the same batch property definitions
		for (TestMaterialId testMaterialId : TestMaterialId.values()) {
			for(BatchPropertyId  batchPropertyId : materialsPluginData.getBatchPropertyIds(testMaterialId)) {
				PropertyDefinition expectedPropertyDefinition = materialsPluginData.getBatchPropertyDefinition(testMaterialId, batchPropertyId);
				PropertyDefinition actualPropertyDefinition = clonePluginData.getBatchPropertyDefinition(testMaterialId, batchPropertyId);
				assertEquals(expectedPropertyDefinition, actualPropertyDefinition);
			}
		}
		
		// show that the two plugin datas have the same batch ids
		assertEquals(materialsPluginData.getBatchIds(), clonePluginData.getBatchIds());
		for(BatchId batchId : materialsPluginData.getBatchIds()) {
			//show that the amounts are equal
			Double expectedAmount = materialsPluginData.getBatchAmount(batchId);
			Double actualAmount = clonePluginData.getBatchAmount(batchId);
			assertEquals(expectedAmount, actualAmount);
			
			//show that the materials are equal
			MaterialId expectedMaterialId = materialsPluginData.getBatchMaterial(batchId);
			MaterialId actualMaterialId = clonePluginData.getBatchMaterial(batchId);
			assertEquals(expectedMaterialId, actualMaterialId);
			
			//show that the materials producers
			MaterialsProducerId expectedMaterialsProducerId = materialsPluginData.getBatchMaterialsProducer(batchId);
			MaterialsProducerId actualMaterialsProducerId = clonePluginData.getBatchMaterialsProducer(batchId);
			assertEquals(expectedMaterialsProducerId, actualMaterialsProducerId);
			
			for(BatchPropertyId batchPropertyId : materialsPluginData.getBatchPropertyIds(expectedMaterialId)){
				Object expectedValue = materialsPluginData.getBatchPropertyValue(batchId, batchPropertyId);
				Object actualValue = clonePluginData.getBatchPropertyValue(batchId, batchPropertyId);
				assertEquals(expectedValue, actualValue);
			}
		}		

	}

}
