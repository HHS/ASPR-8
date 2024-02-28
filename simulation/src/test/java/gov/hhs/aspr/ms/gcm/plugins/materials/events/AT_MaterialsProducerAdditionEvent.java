package gov.hhs.aspr.ms.gcm.plugins.materials.events;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.math3.random.RandomGenerator;
import org.junit.jupiter.api.Test;

import gov.hhs.aspr.ms.gcm.plugins.materials.support.MaterialsError;
import gov.hhs.aspr.ms.gcm.plugins.materials.support.MaterialsProducerId;
import gov.hhs.aspr.ms.gcm.plugins.materials.testsupport.TestMaterialsProducerId;
import gov.hhs.aspr.ms.util.annotations.UnitTestMethod;
import gov.hhs.aspr.ms.util.errors.ContractException;
import gov.hhs.aspr.ms.util.random.RandomGeneratorProvider;

public class AT_MaterialsProducerAdditionEvent {

	@Test
	@UnitTestMethod(target = MaterialsProducerAdditionEvent.class, name = "builder", args = {})
	public void testBuilder() {
		MaterialsProducerAdditionEvent.Builder builder = MaterialsProducerAdditionEvent.builder();
		assertNotNull(builder);
	}

	@Test
	@UnitTestMethod(target = MaterialsProducerAdditionEvent.class, name = "getMaterialsProducerId", args = {})
	public void testGetMaterialsProdcuerId() {
		MaterialsProducerAdditionEvent.Builder builder = MaterialsProducerAdditionEvent.builder();
		MaterialsProducerId producerId = TestMaterialsProducerId.MATERIALS_PRODUCER_1;

		builder.setMaterialsProducerId(producerId);

		MaterialsProducerAdditionEvent event = builder.build();
		assertNotNull(event);

		assertEquals(producerId, event.getMaterialsProducerId());
	}

	@Test
	@UnitTestMethod(target = MaterialsProducerAdditionEvent.class, name = "getValues", args = { Class.class })
	public void testGetValues() {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(2177130500877054468L);
		MaterialsProducerAdditionEvent.Builder builder = MaterialsProducerAdditionEvent.builder();
		MaterialsProducerId producerId = TestMaterialsProducerId.MATERIALS_PRODUCER_1;

		builder.setMaterialsProducerId(producerId);

		List<Integer> expectedIntegerValues = new ArrayList<>();
		List<String> expectedStringValues = new ArrayList<>();

		for (int i = 0; i < 15; i++) {
			int integerVal = randomGenerator.nextInt(100);
			String stringVal = Integer.toString(integerVal);
			builder.addValue(integerVal);
			expectedIntegerValues.add(integerVal);
			builder.addValue(stringVal);
			expectedStringValues.add(stringVal);
		}

		MaterialsProducerAdditionEvent event = builder.build();
		assertNotNull(event);

		List<Integer> actualIntegerValues = event.getValues(Integer.class);
		List<String> actualStringValues = event.getValues(String.class);

		assertEquals(expectedIntegerValues.size(), actualIntegerValues.size());
		assertEquals(expectedIntegerValues, actualIntegerValues);

		assertEquals(expectedStringValues.size(), actualStringValues.size());
		assertEquals(expectedStringValues, actualStringValues);

		assertEquals(0, event.getValues(Double.class).size());
	}

	@Test
	@UnitTestMethod(target = MaterialsProducerAdditionEvent.Builder.class, name = "build", args = {})
	public void testBuild() {
		MaterialsProducerAdditionEvent.Builder builder = MaterialsProducerAdditionEvent.builder();
		MaterialsProducerId producerId = TestMaterialsProducerId.MATERIALS_PRODUCER_1;

		builder.setMaterialsProducerId(producerId);

		MaterialsProducerAdditionEvent event = builder.build();
		assertNotNull(event);

		// precondition: null materials producer id
		MaterialsProducerId nProducerId = null;
		ContractException contractException = assertThrows(ContractException.class, () -> MaterialsProducerAdditionEvent.builder().setMaterialsProducerId(nProducerId).build());
		assertEquals(MaterialsError.NULL_MATERIALS_PRODUCER_ID, contractException.getErrorType());
	}

	@Test
	@UnitTestMethod(target = MaterialsProducerAdditionEvent.Builder.class, name = "addValue", args = { Object.class })
	public void testAddValue() {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(5464369416326792932L);
		MaterialsProducerAdditionEvent.Builder builder = MaterialsProducerAdditionEvent.builder();
		MaterialsProducerId producerId = TestMaterialsProducerId.MATERIALS_PRODUCER_1;

		builder.setMaterialsProducerId(producerId);

		List<Integer> expectedIntegerValues = new ArrayList<>();
		List<String> expectedStringValues = new ArrayList<>();

		for (int i = 0; i < 15; i++) {
			int integerVal = randomGenerator.nextInt(100);
			String stringVal = Integer.toString(integerVal);
			builder.addValue(integerVal);
			expectedIntegerValues.add(integerVal);
			builder.addValue(stringVal);
			expectedStringValues.add(stringVal);
		}

		MaterialsProducerAdditionEvent event = builder.build();
		assertNotNull(event);

		List<Integer> actualIntegerValues = event.getValues(Integer.class);
		List<String> actualStringValues = event.getValues(String.class);

		assertEquals(expectedIntegerValues.size(), actualIntegerValues.size());
		assertEquals(expectedIntegerValues, actualIntegerValues);

		assertEquals(expectedStringValues.size(), actualStringValues.size());
		assertEquals(expectedStringValues, actualStringValues);

		assertEquals(0, event.getValues(Double.class).size());

		// precondition: value is null
		ContractException contractException = assertThrows(ContractException.class, () -> MaterialsProducerAdditionEvent.builder().addValue(null));
		assertEquals(MaterialsError.NULL_AUXILIARY_DATA, contractException.getErrorType());
	}

	@Test
	@UnitTestMethod(target = MaterialsProducerAdditionEvent.Builder.class, name = "setMaterialsProducerId", args = { MaterialsProducerId.class })
	public void testSetMaterialsProducerId() {
		MaterialsProducerAdditionEvent.Builder builder = MaterialsProducerAdditionEvent.builder();
		MaterialsProducerId producerId = TestMaterialsProducerId.MATERIALS_PRODUCER_1;

		builder.setMaterialsProducerId(producerId);

		MaterialsProducerAdditionEvent event = builder.build();
		assertNotNull(event);

		assertEquals(producerId, event.getMaterialsProducerId());

		// precondition: null materials producer id
		ContractException contractException = assertThrows(ContractException.class, () -> MaterialsProducerAdditionEvent.builder().setMaterialsProducerId(null));
		assertEquals(MaterialsError.NULL_MATERIALS_PRODUCER_ID, contractException.getErrorType());
	}
	
	
}
