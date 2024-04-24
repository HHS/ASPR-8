package gov.hhs.aspr.ms.gcm.simulation.plugins.materials.support;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.math3.random.RandomGenerator;
import org.apache.commons.math3.util.Pair;
import org.junit.jupiter.api.Test;

import gov.hhs.aspr.ms.gcm.simulation.plugins.materials.testsupport.TestBatchPropertyId;
import gov.hhs.aspr.ms.gcm.simulation.plugins.materials.testsupport.TestMaterialId;
import gov.hhs.aspr.ms.gcm.simulation.plugins.properties.support.PropertyDefinition;
import gov.hhs.aspr.ms.gcm.simulation.plugins.properties.support.PropertyError;
import gov.hhs.aspr.ms.util.annotations.UnitTestMethod;
import gov.hhs.aspr.ms.util.errors.ContractException;
import gov.hhs.aspr.ms.util.random.RandomGeneratorProvider;

public class AT_BatchPropertyDefinitionInitialization {
	@Test
	@UnitTestMethod(target = BatchPropertyDefinitionInitialization.class, name = "builder", args = {})
	public void testBuilder() {
		BatchPropertyDefinitionInitialization.Builder builder = BatchPropertyDefinitionInitialization.builder();
		assertNotNull(builder);
	}

	@Test
	@UnitTestMethod(target = BatchPropertyDefinitionInitialization.class, name = "getPropertyDefinition", args = {})
	public void testGetPropertyDefinition() {
		PropertyDefinition propertyDefinition = PropertyDefinition	.builder().setDefaultValue(100).setPropertyValueMutability(false)
																	.setType(Integer.class).build();

		BatchPropertyId batchPropertyId = TestBatchPropertyId.BATCH_PROPERTY_2_2_INTEGER_IMMUTABLE_TRACK;
		MaterialId materialId = TestMaterialId.MATERIAL_1;

		BatchPropertyDefinitionInitialization.Builder builder = BatchPropertyDefinitionInitialization	.builder().setMaterialId(materialId).setPropertyId(batchPropertyId)
																										.setPropertyDefinition(propertyDefinition);

		BatchPropertyDefinitionInitialization definitionInitialization = builder.build();

		assertNotNull(definitionInitialization);
		assertEquals(propertyDefinition, definitionInitialization.getPropertyDefinition());
	}

	@Test
	@UnitTestMethod(target = BatchPropertyDefinitionInitialization.class, name = "getPropertyId", args = {})
	public void testGetPropertyId() {
		PropertyDefinition propertyDefinition = PropertyDefinition	.builder().setDefaultValue(100).setPropertyValueMutability(false)
																	.setType(Integer.class).build();

		BatchPropertyId batchPropertyId = TestBatchPropertyId.BATCH_PROPERTY_2_2_INTEGER_IMMUTABLE_TRACK;
		MaterialId materialId = TestMaterialId.MATERIAL_1;

		BatchPropertyDefinitionInitialization.Builder builder = BatchPropertyDefinitionInitialization	.builder().setMaterialId(materialId).setPropertyId(batchPropertyId)
																										.setPropertyDefinition(propertyDefinition);

		BatchPropertyDefinitionInitialization definitionInitialization = builder.build();

		assertNotNull(definitionInitialization);
		assertEquals(batchPropertyId, definitionInitialization.getPropertyId());
	}

	@Test
	@UnitTestMethod(target = BatchPropertyDefinitionInitialization.class, name = "getPropertyValues", args = {})
	public void testGetPropertyValues() {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(8487271708488687492L);
		PropertyDefinition propertyDefinition = PropertyDefinition	.builder().setDefaultValue("100").setPropertyValueMutability(false)
																	.setType(String.class).build();

		BatchPropertyId batchPropertyId = TestBatchPropertyId.BATCH_PROPERTY_2_2_INTEGER_IMMUTABLE_TRACK;
		MaterialId materialId = TestMaterialId.MATERIAL_1;

		BatchPropertyDefinitionInitialization.Builder builder = BatchPropertyDefinitionInitialization	.builder().setMaterialId(materialId).setPropertyId(batchPropertyId)
																										.setPropertyDefinition(propertyDefinition);

		List<Pair<BatchId, Object>> expectedValues = new ArrayList<>();
		for (int i = 0; i < 15; i++) {
			String value = Integer.toString(randomGenerator.nextInt(100));
			BatchId batchId = new BatchId(i);
			builder.addPropertyValue(batchId, value);
			expectedValues.add(new Pair<>(batchId, value));
		}

		BatchPropertyDefinitionInitialization definitionInitialization = builder.build();

		assertNotNull(definitionInitialization);
		assertEquals(expectedValues.size(), definitionInitialization.getPropertyValues().size());
		assertEquals(expectedValues, definitionInitialization.getPropertyValues());

	}

	@Test
	@UnitTestMethod(target = BatchPropertyDefinitionInitialization.class, name = "getMaterialId", args = {})
	public void testGetMaterialId() {
		PropertyDefinition propertyDefinition = PropertyDefinition	.builder().setDefaultValue(100).setPropertyValueMutability(false)
																	.setType(Integer.class).build();

		BatchPropertyId batchPropertyId = TestBatchPropertyId.BATCH_PROPERTY_2_2_INTEGER_IMMUTABLE_TRACK;
		MaterialId materialId = TestMaterialId.MATERIAL_1;

		BatchPropertyDefinitionInitialization.Builder builder = BatchPropertyDefinitionInitialization	.builder().setMaterialId(materialId).setPropertyId(batchPropertyId)
																										.setPropertyDefinition(propertyDefinition);

		BatchPropertyDefinitionInitialization definitionInitialization = builder.build();

		assertNotNull(definitionInitialization);
		assertEquals(materialId, definitionInitialization.getMaterialId());
	}

	@Test
	@UnitTestMethod(target = BatchPropertyDefinitionInitialization.Builder.class, name = "build", args = {})
	public void testBuild() {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(8487271708488687492L);
		PropertyDefinition propertyDefinition = PropertyDefinition	.builder().setDefaultValue("100").setPropertyValueMutability(false)
																	.setType(String.class).build();

		BatchPropertyId batchPropertyId = TestBatchPropertyId.BATCH_PROPERTY_2_2_INTEGER_IMMUTABLE_TRACK;
		MaterialId materialId = TestMaterialId.MATERIAL_1;

		BatchPropertyDefinitionInitialization.Builder builder = BatchPropertyDefinitionInitialization	.builder().setMaterialId(materialId).setPropertyId(batchPropertyId)
																										.setPropertyDefinition(propertyDefinition);

		List<Pair<BatchId, Object>> expectedValues = new ArrayList<>();
		for (int i = 0; i < 15; i++) {
			String value = Integer.toString(randomGenerator.nextInt(100));
			BatchId batchId = new BatchId(i);
			builder.addPropertyValue(batchId, value);
			expectedValues.add(new Pair<>(batchId, value));
		}

		BatchPropertyDefinitionInitialization definitionInitialization = builder.build();

		assertNotNull(definitionInitialization);

		// precondition: null property definition
		PropertyDefinition nPropertyDefinition = null;
		ContractException contractException = assertThrows(ContractException.class,
				() -> BatchPropertyDefinitionInitialization.builder().setMaterialId(materialId).setPropertyId(batchPropertyId).setPropertyDefinition(nPropertyDefinition).build());
		assertEquals(PropertyError.NULL_PROPERTY_DEFINITION, contractException.getErrorType());

		// precondition: null property id
		BatchPropertyId nBatchPropertyId = null;
		contractException = assertThrows(ContractException.class,
				() -> BatchPropertyDefinitionInitialization.builder().setMaterialId(materialId).setPropertyId(nBatchPropertyId).setPropertyDefinition(propertyDefinition).build());
		assertEquals(PropertyError.NULL_PROPERTY_ID, contractException.getErrorType());

		// precondition: null material id
		MaterialId nMaterialId = null;
		contractException = assertThrows(ContractException.class,
				() -> BatchPropertyDefinitionInitialization.builder().setMaterialId(nMaterialId).setPropertyId(batchPropertyId).setPropertyDefinition(propertyDefinition).build());
		assertEquals(MaterialsError.NULL_MATERIAL_ID, contractException.getErrorType());

		// precondition: incomaptible value
		contractException = assertThrows(ContractException.class,
				() -> BatchPropertyDefinitionInitialization	.builder().setMaterialId(materialId).setPropertyId(batchPropertyId).setPropertyDefinition(propertyDefinition)
															.addPropertyValue(new BatchId(0), 100).build());
		assertEquals(PropertyError.INCOMPATIBLE_VALUE, contractException.getErrorType());

	}

	@Test
	@UnitTestMethod(target = BatchPropertyDefinitionInitialization.Builder.class, name = "setPropertyDefinition", args = { PropertyDefinition.class })
	public void testSetPropertyDefinition() {
		PropertyDefinition propertyDefinition = PropertyDefinition	.builder().setDefaultValue(100).setPropertyValueMutability(false)
																	.setType(Integer.class).build();

		BatchPropertyId batchPropertyId = TestBatchPropertyId.BATCH_PROPERTY_2_2_INTEGER_IMMUTABLE_TRACK;
		MaterialId materialId = TestMaterialId.MATERIAL_1;

		BatchPropertyDefinitionInitialization definitionInitialization = BatchPropertyDefinitionInitialization	.builder().setMaterialId(materialId).setPropertyId(batchPropertyId)
																												.setPropertyDefinition(propertyDefinition).build();

		assertNotNull(definitionInitialization);
		assertEquals(propertyDefinition, definitionInitialization.getPropertyDefinition());

		// precondition: null property definition
		ContractException contractException = assertThrows(ContractException.class, () -> BatchPropertyDefinitionInitialization.builder().setPropertyDefinition(null));
		assertEquals(PropertyError.NULL_PROPERTY_DEFINITION, contractException.getErrorType());
	}

	@Test
	@UnitTestMethod(target = BatchPropertyDefinitionInitialization.Builder.class, name = "setPropertyId", args = { BatchPropertyId.class })
	public void testSetPropertyId() {
		PropertyDefinition propertyDefinition = PropertyDefinition	.builder().setDefaultValue(100).setPropertyValueMutability(false)
																	.setType(Integer.class).build();

		BatchPropertyId batchPropertyId = TestBatchPropertyId.BATCH_PROPERTY_2_2_INTEGER_IMMUTABLE_TRACK;
		MaterialId materialId = TestMaterialId.MATERIAL_1;

		BatchPropertyDefinitionInitialization.Builder builder = BatchPropertyDefinitionInitialization	.builder().setMaterialId(materialId).setPropertyId(batchPropertyId)
																										.setPropertyDefinition(propertyDefinition);

		BatchPropertyDefinitionInitialization definitionInitialization = builder.build();

		assertNotNull(definitionInitialization);
		assertEquals(batchPropertyId, definitionInitialization.getPropertyId());

		// precondition: null property id
		ContractException contractException = assertThrows(ContractException.class, () -> BatchPropertyDefinitionInitialization.builder().setPropertyId(null));
		assertEquals(PropertyError.NULL_PROPERTY_ID, contractException.getErrorType());
	}

	@Test
	@UnitTestMethod(target = BatchPropertyDefinitionInitialization.Builder.class, name = "addPropertyValue", args = { BatchId.class, Object.class })
	public void testAddPropertyValue() {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(8487271708488687492L);
		PropertyDefinition propertyDefinition = PropertyDefinition	.builder().setDefaultValue("100").setPropertyValueMutability(false)
																	.setType(String.class).build();

		BatchPropertyId batchPropertyId = TestBatchPropertyId.BATCH_PROPERTY_2_2_INTEGER_IMMUTABLE_TRACK;
		MaterialId materialId = TestMaterialId.MATERIAL_1;

		BatchPropertyDefinitionInitialization.Builder builder = BatchPropertyDefinitionInitialization	.builder().setMaterialId(materialId).setPropertyId(batchPropertyId)
																										.setPropertyDefinition(propertyDefinition);

		List<Pair<BatchId, Object>> expectedValues = new ArrayList<>();
		for (int i = 0; i < 15; i++) {
			String value = Integer.toString(randomGenerator.nextInt(100));
			BatchId batchId = new BatchId(i);
			builder.addPropertyValue(batchId, value);
			expectedValues.add(new Pair<>(batchId, value));
		}

		BatchPropertyDefinitionInitialization definitionInitialization = builder.build();

		assertNotNull(definitionInitialization);
		assertEquals(expectedValues.size(), definitionInitialization.getPropertyValues().size());
		assertEquals(expectedValues, definitionInitialization.getPropertyValues());

		// precondition: null batch id
		ContractException contractException = assertThrows(ContractException.class, () -> BatchPropertyDefinitionInitialization.builder().addPropertyValue(null, "100"));
		assertEquals(MaterialsError.NULL_BATCH_ID, contractException.getErrorType());

		// precondition: null value
		contractException = assertThrows(ContractException.class, () -> BatchPropertyDefinitionInitialization.builder().addPropertyValue(new BatchId(0), null));
		assertEquals(PropertyError.NULL_PROPERTY_VALUE, contractException.getErrorType());
	}

	@Test
	@UnitTestMethod(target = BatchPropertyDefinitionInitialization.Builder.class, name = "setMaterialId", args = { MaterialId.class })
	public void testSetMaterialId() {
		PropertyDefinition propertyDefinition = PropertyDefinition	.builder().setDefaultValue(100).setPropertyValueMutability(false)
																	.setType(Integer.class).build();

		BatchPropertyId batchPropertyId = TestBatchPropertyId.BATCH_PROPERTY_2_2_INTEGER_IMMUTABLE_TRACK;
		MaterialId materialId = TestMaterialId.MATERIAL_1;

		BatchPropertyDefinitionInitialization.Builder builder = BatchPropertyDefinitionInitialization	.builder().setMaterialId(materialId).setPropertyId(batchPropertyId)
																										.setPropertyDefinition(propertyDefinition);

		BatchPropertyDefinitionInitialization definitionInitialization = builder.build();

		assertNotNull(definitionInitialization);
		assertEquals(materialId, definitionInitialization.getMaterialId());

		// precondition: null material id
		ContractException contractException = assertThrows(ContractException.class, () -> BatchPropertyDefinitionInitialization.builder().setMaterialId(null));
		assertEquals(MaterialsError.NULL_MATERIAL_ID, contractException.getErrorType());
	}
}
