package gov.hhs.aspr.ms.gcm.simulation.plugins.materials.support;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.math3.random.RandomGenerator;
import org.apache.commons.math3.util.Pair;
import org.junit.jupiter.api.Test;

import gov.hhs.aspr.ms.gcm.simulation.plugins.materials.testsupport.TestMaterialsProducerId;
import gov.hhs.aspr.ms.gcm.simulation.plugins.materials.testsupport.TestMaterialsProducerPropertyId;
import gov.hhs.aspr.ms.gcm.simulation.plugins.properties.support.PropertyDefinition;
import gov.hhs.aspr.ms.gcm.simulation.plugins.properties.support.PropertyError;
import gov.hhs.aspr.ms.util.annotations.UnitTestMethod;
import gov.hhs.aspr.ms.util.errors.ContractException;
import gov.hhs.aspr.ms.util.random.RandomGeneratorProvider;

public class AT_MaterialsProducerPropertyDefinitionInitialization {

	@Test
	@UnitTestMethod(target = MaterialsProducerPropertyDefinitionInitialization.class, name = "builder", args = {})
	public void testBuilder() {
		MaterialsProducerPropertyDefinitionInitialization.Builder builder = MaterialsProducerPropertyDefinitionInitialization.builder();
		assertNotNull(builder);
	}

	@Test
	@UnitTestMethod(target = MaterialsProducerPropertyDefinitionInitialization.class, name = "getPropertyDefinition", args = {})
	public void testGetPropertyDefinition() {
		MaterialsProducerPropertyDefinitionInitialization.Builder builder = MaterialsProducerPropertyDefinitionInitialization.builder();
		PropertyDefinition propertyDefinition = PropertyDefinition.builder().setDefaultValue(100).setPropertyValueMutability(false).setType(Integer.class).build();
		MaterialsProducerPropertyId producerPropertyId = TestMaterialsProducerPropertyId.MATERIALS_PRODUCER_PROPERTY_8_INTEGER_IMMUTABLE_NO_TRACK;

		builder.setPropertyDefinition(propertyDefinition).setMaterialsProducerPropertyId(producerPropertyId);

		MaterialsProducerPropertyDefinitionInitialization definitionInitialization = builder.build();

		assertNotNull(definitionInitialization);
		assertEquals(propertyDefinition, definitionInitialization.getPropertyDefinition());

	}

	@Test
	@UnitTestMethod(target = MaterialsProducerPropertyDefinitionInitialization.class, name = "getPropertyValues", args = {})
	public void testGetPropertyValues() {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(7180465772129297639L);
		MaterialsProducerPropertyDefinitionInitialization.Builder builder = MaterialsProducerPropertyDefinitionInitialization.builder();
		PropertyDefinition propertyDefinition = PropertyDefinition.builder().setDefaultValue(100).setPropertyValueMutability(false).setType(Integer.class).build();
		MaterialsProducerPropertyId producerPropertyId = TestMaterialsProducerPropertyId.MATERIALS_PRODUCER_PROPERTY_8_INTEGER_IMMUTABLE_NO_TRACK;

		builder.setPropertyDefinition(propertyDefinition).setMaterialsProducerPropertyId(producerPropertyId);

		List<Pair<MaterialsProducerId, Object>> expectedValues = new ArrayList<>();
		for (int i = 0; i < 15; i++) {
			int value = randomGenerator.nextInt(100);
			MaterialsProducerId producerId = TestMaterialsProducerId.getRandomMaterialsProducerId(randomGenerator);
			expectedValues.add(new Pair<>(producerId, value));
			builder.addPropertyValue(producerId, value);
		}

		MaterialsProducerPropertyDefinitionInitialization definitionInitialization = builder.build();
		List<Pair<MaterialsProducerId, Object>> actualValues = definitionInitialization.getPropertyValues();
		assertNotNull(definitionInitialization);
		assertEquals(expectedValues.size(), actualValues.size());
		assertEquals(expectedValues, actualValues);
	}

	@Test
	@UnitTestMethod(target = MaterialsProducerPropertyDefinitionInitialization.class, name = "getMaterialsProducerPropertyId", args = {})
	public void testGetMaterialsProducerPropertyId() {
		MaterialsProducerPropertyDefinitionInitialization.Builder builder = MaterialsProducerPropertyDefinitionInitialization.builder();
		PropertyDefinition propertyDefinition = PropertyDefinition	.builder().setDefaultValue(100)//
																	.setPropertyValueMutability(false)//
																	.setType(Integer.class)//
																	.build();
		MaterialsProducerPropertyId producerPropertyId = TestMaterialsProducerPropertyId.MATERIALS_PRODUCER_PROPERTY_8_INTEGER_IMMUTABLE_NO_TRACK;

		builder.setPropertyDefinition(propertyDefinition).setMaterialsProducerPropertyId(producerPropertyId);

		MaterialsProducerPropertyDefinitionInitialization definitionInitialization = builder.build();

		assertNotNull(definitionInitialization);
		assertEquals(producerPropertyId, definitionInitialization.getMaterialsProducerPropertyId());
	}

	@Test
	@UnitTestMethod(target = MaterialsProducerPropertyDefinitionInitialization.Builder.class, name = "build", args = {})
	public void testBuild() {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(6087174477323266390L);
		MaterialsProducerPropertyDefinitionInitialization.Builder builder = MaterialsProducerPropertyDefinitionInitialization.builder();
		PropertyDefinition propertyDefinition = PropertyDefinition	.builder()//
																	.setDefaultValue(100)//
																	.setPropertyValueMutability(false)//
																	.setType(Integer.class).build();
		MaterialsProducerPropertyId producerPropertyId = TestMaterialsProducerPropertyId.MATERIALS_PRODUCER_PROPERTY_8_INTEGER_IMMUTABLE_NO_TRACK;

		builder.setPropertyDefinition(propertyDefinition).setMaterialsProducerPropertyId(producerPropertyId);

		List<Pair<MaterialsProducerId, Object>> expectedValues = new ArrayList<>();
		for (int i = 0; i < 15; i++) {
			int value = randomGenerator.nextInt(100);
			MaterialsProducerId producerId = TestMaterialsProducerId.getRandomMaterialsProducerId(randomGenerator);
			expectedValues.add(new Pair<>(producerId, value));
			builder.addPropertyValue(producerId, value);
		}

		MaterialsProducerPropertyDefinitionInitialization definitionInitialization = builder.build();

		assertNotNull(definitionInitialization);

		// precondition: propertyDefinition is null
		ContractException contractException = assertThrows(ContractException.class,
				() -> MaterialsProducerPropertyDefinitionInitialization.builder().setMaterialsProducerPropertyId(producerPropertyId).build());
		assertEquals(PropertyError.NULL_PROPERTY_DEFINITION, contractException.getErrorType());

		// precondition: producer property id is null
		contractException = assertThrows(ContractException.class, () -> MaterialsProducerPropertyDefinitionInitialization.builder().setPropertyDefinition(propertyDefinition).build());
		assertEquals(PropertyError.NULL_PROPERTY_ID, contractException.getErrorType());

		// precondition: incompatible value
		contractException = assertThrows(ContractException.class,
				() -> MaterialsProducerPropertyDefinitionInitialization	.builder().setMaterialsProducerPropertyId(producerPropertyId).setPropertyDefinition(propertyDefinition)
																		.addPropertyValue(TestMaterialsProducerId.getRandomMaterialsProducerId(randomGenerator), "100").build());
		assertEquals(PropertyError.INCOMPATIBLE_VALUE, contractException.getErrorType());
	}

	@Test
	@UnitTestMethod(target = MaterialsProducerPropertyDefinitionInitialization.Builder.class, name = "setPropertyDefinition", args = { PropertyDefinition.class })
	public void testSetPropertyDefinition() {
		MaterialsProducerPropertyDefinitionInitialization.Builder builder = MaterialsProducerPropertyDefinitionInitialization.builder();
		PropertyDefinition propertyDefinition = PropertyDefinition	.builder()//
																	.setDefaultValue(100)//
																	.setPropertyValueMutability(false)//
																	.setType(Integer.class)//
																	.build();
		MaterialsProducerPropertyId producerPropertyId = TestMaterialsProducerPropertyId.MATERIALS_PRODUCER_PROPERTY_8_INTEGER_IMMUTABLE_NO_TRACK;

		builder.setPropertyDefinition(propertyDefinition).setMaterialsProducerPropertyId(producerPropertyId);

		MaterialsProducerPropertyDefinitionInitialization definitionInitialization = builder.build();

		assertNotNull(definitionInitialization);
		assertEquals(propertyDefinition, definitionInitialization.getPropertyDefinition());

		// precondition: propertyDefinition is null
		ContractException contractException = assertThrows(ContractException.class, () -> MaterialsProducerPropertyDefinitionInitialization.builder().setPropertyDefinition(null));
		assertEquals(PropertyError.NULL_PROPERTY_DEFINITION, contractException.getErrorType());
	}

	@Test
	@UnitTestMethod(target = MaterialsProducerPropertyDefinitionInitialization.Builder.class, name = "addPropertyValue", args = { MaterialsProducerId.class, Object.class })
	public void testAddPropertyValue() {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(3968585809330067411L);
		MaterialsProducerPropertyDefinitionInitialization.Builder builder = MaterialsProducerPropertyDefinitionInitialization.builder();
		PropertyDefinition propertyDefinition = PropertyDefinition	.builder()//
																	.setDefaultValue(100)//
																	.setPropertyValueMutability(false)//
																	.setType(Integer.class).build();
		MaterialsProducerPropertyId producerPropertyId = TestMaterialsProducerPropertyId.MATERIALS_PRODUCER_PROPERTY_8_INTEGER_IMMUTABLE_NO_TRACK;

		builder.setPropertyDefinition(propertyDefinition).setMaterialsProducerPropertyId(producerPropertyId);

		List<Pair<MaterialsProducerId, Object>> expectedValues = new ArrayList<>();
		for (int i = 0; i < 15; i++) {
			int value = randomGenerator.nextInt(100);
			MaterialsProducerId producerId = TestMaterialsProducerId.getRandomMaterialsProducerId(randomGenerator);
			expectedValues.add(new Pair<>(producerId, value));
			builder.addPropertyValue(producerId, value);
		}

		MaterialsProducerPropertyDefinitionInitialization definitionInitialization = builder.build();
		List<Pair<MaterialsProducerId, Object>> actualValues = definitionInitialization.getPropertyValues();
		assertNotNull(definitionInitialization);
		assertEquals(expectedValues.size(), actualValues.size());
		assertEquals(expectedValues, actualValues);

		// precondition: producer id is null
		ContractException contractException = assertThrows(ContractException.class, () -> MaterialsProducerPropertyDefinitionInitialization.builder().addPropertyValue(null, 100));
		assertEquals(MaterialsError.NULL_MATERIALS_PRODUCER_ID, contractException.getErrorType());

		// precondition: value is null
		contractException = assertThrows(ContractException.class,
				() -> MaterialsProducerPropertyDefinitionInitialization.builder().addPropertyValue(TestMaterialsProducerId.MATERIALS_PRODUCER_1, null));
		assertEquals(PropertyError.NULL_PROPERTY_VALUE, contractException.getErrorType());
	}

	@Test
	@UnitTestMethod(target = MaterialsProducerPropertyDefinitionInitialization.Builder.class, name = "setMaterialsProducerPropertyId", args = { MaterialsProducerPropertyId.class })
	public void testSetMaterialsProducerPropertyId() {
		MaterialsProducerPropertyDefinitionInitialization.Builder builder = MaterialsProducerPropertyDefinitionInitialization.builder();
		PropertyDefinition propertyDefinition = PropertyDefinition	.builder()//
																	.setDefaultValue(100)//
																	.setPropertyValueMutability(false)//
																	.setType(Integer.class).build();
		MaterialsProducerPropertyId producerPropertyId = TestMaterialsProducerPropertyId.MATERIALS_PRODUCER_PROPERTY_8_INTEGER_IMMUTABLE_NO_TRACK;

		builder.setPropertyDefinition(propertyDefinition).setMaterialsProducerPropertyId(producerPropertyId);

		MaterialsProducerPropertyDefinitionInitialization definitionInitialization = builder.build();

		assertNotNull(definitionInitialization);
		assertEquals(producerPropertyId, definitionInitialization.getMaterialsProducerPropertyId());

		// precondition: producer property id is null
		ContractException contractException = assertThrows(ContractException.class, () -> MaterialsProducerPropertyDefinitionInitialization.builder().setMaterialsProducerPropertyId(null));
		assertEquals(PropertyError.NULL_PROPERTY_ID, contractException.getErrorType());
	}
}
