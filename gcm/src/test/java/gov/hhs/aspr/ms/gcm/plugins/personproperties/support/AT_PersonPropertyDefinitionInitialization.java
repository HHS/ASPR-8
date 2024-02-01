package gov.hhs.aspr.ms.gcm.plugins.personproperties.support;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.math3.random.RandomGenerator;
import org.apache.commons.math3.util.Pair;
import org.junit.jupiter.api.Test;

import gov.hhs.aspr.ms.gcm.plugins.people.support.PersonError;
import gov.hhs.aspr.ms.gcm.plugins.people.support.PersonId;
import gov.hhs.aspr.ms.gcm.plugins.personproperties.testsupport.TestPersonPropertyId;
import gov.hhs.aspr.ms.gcm.plugins.properties.support.PropertyDefinition;
import gov.hhs.aspr.ms.gcm.plugins.properties.support.PropertyError;
import util.annotations.UnitTestMethod;
import util.errors.ContractException;
import util.random.RandomGeneratorProvider;

public class AT_PersonPropertyDefinitionInitialization {

	@Test
	@UnitTestMethod(target = PersonPropertyDefinitionInitialization.class, name = "builder", args = {})
	public void testBuilder() {
		PersonPropertyDefinitionInitialization.Builder builder = PersonPropertyDefinitionInitialization.builder();

		assertNotNull(builder);
	}

	@Test
	@UnitTestMethod(target = PersonPropertyDefinitionInitialization.class, name = "getPropertyDefinition", args = {})
	public void testGetPropertyDefinition() {
		PersonPropertyDefinitionInitialization.Builder builder = PersonPropertyDefinitionInitialization.builder();
		PropertyDefinition propertyDefinition = PropertyDefinition.builder().setType(Integer.class).setDefaultValue(100)
				.setPropertyValueMutability(true).build();

		builder.setPropertyDefinition(propertyDefinition);
		builder.setPersonPropertyId(TestPersonPropertyId.PERSON_PROPERTY_2_INTEGER_MUTABLE_NO_TRACK);

		PersonPropertyDefinitionInitialization propertyDefinitionInitialization = builder.build();

		assertNotNull(propertyDefinitionInitialization);
		assertEquals(propertyDefinition, propertyDefinitionInitialization.getPropertyDefinition());
	}

	@Test
	@UnitTestMethod(target = PersonPropertyDefinitionInitialization.class, name = "getPropertyValues", args = {})
	public void testGetPropertyValues() {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(2754843240208076356L);

		PersonPropertyDefinitionInitialization.Builder builder = PersonPropertyDefinitionInitialization.builder();
		PropertyDefinition propertyDefinition = PropertyDefinition.builder().setType(Integer.class).setDefaultValue(100)
				.setPropertyValueMutability(true).build();

		builder.setPropertyDefinition(propertyDefinition);
		builder.setPersonPropertyId(TestPersonPropertyId.PERSON_PROPERTY_2_INTEGER_MUTABLE_NO_TRACK);

		List<Pair<PersonId, Object>> expectedValues = new ArrayList<>();

		for (int i = 0; i < 20; i++) {
			int value = randomGenerator.nextInt(100);
			PersonId personId = new PersonId(i * 2);
			builder.addPropertyValue(personId, value);
			expectedValues.add(new Pair<>(personId, value));
		}

		PersonPropertyDefinitionInitialization propertyDefinitionInitialization = builder.build();

		assertNotNull(propertyDefinitionInitialization);

		List<Pair<PersonId, Object>> actualValues = propertyDefinitionInitialization.getPropertyValues();
		assertNotNull(actualValues);
		assertFalse(actualValues.isEmpty());
		assertEquals(expectedValues, actualValues);
	}

	@Test
	@UnitTestMethod(target = PersonPropertyDefinitionInitialization.class, name = "getPersonPropertyId", args = {})
	public void testGetPersonPropertyId() {
		PersonPropertyDefinitionInitialization.Builder builder = PersonPropertyDefinitionInitialization.builder();
		PropertyDefinition propertyDefinition = PropertyDefinition.builder().setType(Integer.class).setDefaultValue(100)
				.setPropertyValueMutability(true).build();
		PersonPropertyId personPropertyId = TestPersonPropertyId.PERSON_PROPERTY_2_INTEGER_MUTABLE_NO_TRACK;
		builder.setPropertyDefinition(propertyDefinition);
		builder.setPersonPropertyId(personPropertyId);
		PersonPropertyDefinitionInitialization propertyDefinitionInitialization = builder.build();

		assertNotNull(propertyDefinitionInitialization);
		assertEquals(personPropertyId, propertyDefinitionInitialization.getPersonPropertyId());
	}

	@Test
	@UnitTestMethod(target = PersonPropertyDefinitionInitialization.Builder.class, name = "build", args = {})
	public void testBuild() {
		PersonPropertyDefinitionInitialization.Builder builder = PersonPropertyDefinitionInitialization.builder();
		PropertyDefinition propertyDefinition = PropertyDefinition.builder().setType(Integer.class).setDefaultValue(100)
				.setPropertyValueMutability(true).build();
		PersonPropertyId personPropertyId = TestPersonPropertyId.PERSON_PROPERTY_2_INTEGER_MUTABLE_NO_TRACK;
		builder.setPropertyDefinition(propertyDefinition);
		builder.setPersonPropertyId(personPropertyId);
		PersonPropertyDefinitionInitialization propertyDefinitionInitialization = builder.build();

		assertNotNull(propertyDefinitionInitialization);

		// precondition: null propertyDefinition
		ContractException contractException = assertThrows(ContractException.class, () -> {
			PersonPropertyDefinitionInitialization.builder().setPersonPropertyId(personPropertyId).build();
		});
		assertEquals(PropertyError.NULL_PROPERTY_DEFINITION, contractException.getErrorType());

		// precondition: person property id is null
		contractException = assertThrows(ContractException.class, () -> {
			PersonPropertyDefinitionInitialization.builder().setPropertyDefinition(propertyDefinition).build();
		});
		assertEquals(PropertyError.NULL_PROPERTY_ID, contractException.getErrorType());

		// precondition: incomaptible value
		contractException = assertThrows(ContractException.class, () -> {
			PersonPropertyDefinitionInitialization.builder().setPropertyDefinition(propertyDefinition)
					.setPersonPropertyId(personPropertyId).addPropertyValue(new PersonId(1000), "100").build();
		});
		assertEquals(PropertyError.INCOMPATIBLE_VALUE, contractException.getErrorType());
	}

	@Test
	@UnitTestMethod(target = PersonPropertyDefinitionInitialization.Builder.class, name = "setPropertyDefinition", args = {
			PropertyDefinition.class })
	public void testSetPropertyDefinition() {
		PersonPropertyDefinitionInitialization.Builder builder = PersonPropertyDefinitionInitialization.builder();
		PropertyDefinition propertyDefinition = PropertyDefinition.builder().setType(Double.class)
				.setDefaultValue(100.0).setPropertyValueMutability(true).build();
		PersonPropertyId personPropertyId = TestPersonPropertyId.PERSON_PROPERTY_3_DOUBLE_MUTABLE_NO_TRACK;
		builder.setPropertyDefinition(propertyDefinition);
		builder.setPersonPropertyId(personPropertyId);
		PersonPropertyDefinitionInitialization propertyDefinitionInitialization = builder.build();

		assertNotNull(propertyDefinitionInitialization);
		assertEquals(personPropertyId, propertyDefinitionInitialization.getPersonPropertyId());

		// precondition: property definition is null
		ContractException contractException = assertThrows(ContractException.class, () -> {
			PersonPropertyDefinitionInitialization.builder().setPropertyDefinition(null);
		});
		assertEquals(PropertyError.NULL_PROPERTY_DEFINITION, contractException.getErrorType());
	}

	@Test
	@UnitTestMethod(target = PersonPropertyDefinitionInitialization.Builder.class, name = "addPropertyValue", args = {
			PersonId.class, Object.class })
	public void testAddPropertyValue() {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(2816191091329528844L);

		PersonPropertyDefinitionInitialization.Builder builder = PersonPropertyDefinitionInitialization.builder();
		PropertyDefinition propertyDefinition = PropertyDefinition.builder().setType(Double.class)
				.setDefaultValue(100.0).setPropertyValueMutability(true).build();

		builder.setPropertyDefinition(propertyDefinition);
		builder.setPersonPropertyId(TestPersonPropertyId.PERSON_PROPERTY_3_DOUBLE_MUTABLE_NO_TRACK);

		List<Pair<PersonId, Object>> expectedValues = new ArrayList<>();
		for (int i = 0; i < 20; i++) {
			double value = randomGenerator.nextDouble() * 100;
			PersonId personId = new PersonId(i * 2);
			builder.addPropertyValue(personId, value);
			expectedValues.add(new Pair<>(personId, value));
		}

		PersonPropertyDefinitionInitialization propertyDefinitionInitialization = builder.build();
		List<Pair<PersonId, Object>> actualValues = propertyDefinitionInitialization.getPropertyValues();
		assertNotNull(actualValues);
		assertFalse(actualValues.isEmpty());
		assertEquals(expectedValues, actualValues);

		// precondition: null person id
		ContractException contractException = assertThrows(ContractException.class, () -> {
			PersonPropertyDefinitionInitialization.builder().addPropertyValue(null, 100.0);
		});
		assertEquals(PersonError.NULL_PERSON_ID, contractException.getErrorType());

		// precondition: value is null
		contractException = assertThrows(ContractException.class, () -> {
			PersonPropertyDefinitionInitialization.builder().addPropertyValue(new PersonId(1000), null);
		});
		assertEquals(PropertyError.NULL_PROPERTY_VALUE, contractException.getErrorType());
	}

	@Test
	@UnitTestMethod(target = PersonPropertyDefinitionInitialization.Builder.class, name = "setPersonPropertyId", args = {
			PersonPropertyId.class })
	public void testSetPersonPropertyId() {
		PersonPropertyDefinitionInitialization.Builder builder = PersonPropertyDefinitionInitialization.builder();
		PropertyDefinition propertyDefinition = PropertyDefinition.builder().setType(Double.class)
				.setDefaultValue(100.0).setPropertyValueMutability(true).build();
		PersonPropertyId personPropertyId = TestPersonPropertyId.PERSON_PROPERTY_3_DOUBLE_MUTABLE_NO_TRACK;
		builder.setPropertyDefinition(propertyDefinition);
		builder.setPersonPropertyId(personPropertyId);
		PersonPropertyDefinitionInitialization propertyDefinitionInitialization = builder.build();

		assertNotNull(propertyDefinitionInitialization);
		assertEquals(personPropertyId, propertyDefinitionInitialization.getPersonPropertyId());

		// precondition: property id is null
		ContractException contractException = assertThrows(ContractException.class, () -> {
			PersonPropertyDefinitionInitialization.builder().setPersonPropertyId(null);
		});
		assertEquals(PropertyError.NULL_PROPERTY_ID, contractException.getErrorType());
	}
	
	@Test
	@UnitTestMethod(target = PersonPropertyDefinitionInitialization.class, name = "trackTimes", args = {})
	public void testTrackTimes() {

		for (boolean trackTimes : new boolean[] { true, false }) {
			PropertyDefinition propertyDefinition = PropertyDefinition.builder()//
					.setType(Double.class)//
					.setDefaultValue(100.0)//
					.setPropertyValueMutability(true)//
					.build();//
			PersonPropertyId personPropertyId = TestPersonPropertyId.PERSON_PROPERTY_3_DOUBLE_MUTABLE_NO_TRACK;

			PersonPropertyDefinitionInitialization.Builder builder = PersonPropertyDefinitionInitialization.builder();
			builder.setPropertyDefinition(propertyDefinition);
			builder.setPersonPropertyId(personPropertyId);
			builder.setTrackTimes(trackTimes);
			PersonPropertyDefinitionInitialization propertyDefinitionInitialization = builder.build();

			assertNotNull(propertyDefinitionInitialization);
			assertEquals(trackTimes, propertyDefinitionInitialization.trackTimes());

		}
		
		
	}
	
 	@Test
	@UnitTestMethod(target = PersonPropertyDefinitionInitialization.Builder.class, name = "setTrackTimes", args = {
			boolean.class })
	public void testSetTrackTimes() {

		for (boolean trackTimes : new boolean[] { true, false }) {
			PropertyDefinition propertyDefinition = PropertyDefinition.builder()//
					.setType(Double.class)//
					.setDefaultValue(100.0)//
					.setPropertyValueMutability(true)//
					.build();//
			PersonPropertyId personPropertyId = TestPersonPropertyId.PERSON_PROPERTY_3_DOUBLE_MUTABLE_NO_TRACK;

			PersonPropertyDefinitionInitialization.Builder builder = PersonPropertyDefinitionInitialization.builder();
			builder.setPropertyDefinition(propertyDefinition);
			builder.setPersonPropertyId(personPropertyId);
			builder.setTrackTimes(trackTimes);
			PersonPropertyDefinitionInitialization propertyDefinitionInitialization = builder.build();

			assertNotNull(propertyDefinitionInitialization);
			assertEquals(trackTimes, propertyDefinitionInitialization.trackTimes());

		}
		
		
	}
}
