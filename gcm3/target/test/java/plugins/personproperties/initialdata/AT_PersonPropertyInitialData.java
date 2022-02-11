package plugins.personproperties.initialdata;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.math3.random.RandomGenerator;
import org.junit.jupiter.api.Test;

import plugins.people.support.PersonError;
import plugins.people.support.PersonId;
import plugins.personproperties.support.PersonPropertyError;
import plugins.personproperties.support.PersonPropertyId;
import plugins.personproperties.testsupport.TestPersonPropertyId;
import plugins.properties.support.PropertyDefinition;
import plugins.properties.support.PropertyError;
import plugins.properties.support.TimeTrackingPolicy;
import util.ContractException;
import util.SeedProvider;
import util.annotations.UnitTest;
import util.annotations.UnitTestMethod;

@UnitTest(target = PersonPropertyInitialData.class)
public class AT_PersonPropertyInitialData {

	@Test
	@UnitTestMethod(name = "builder", args = {})
	public void testBuilder() {
		assertNotNull(PersonPropertyInitialData.builder());
	}

	@Test
	@UnitTestMethod(target = PersonPropertyInitialData.Builder.class, name = "build", args = {})
	public void testBuild() {

		assertNotNull(PersonPropertyInitialData.builder().build());

		// precondition tests

		/*
		 * if a person has been assigned a value for a property id that does not
		 * correspond to a property definition
		 */
		ContractException contractException = assertThrows(ContractException.class, () -> {
			PersonPropertyInitialData.Builder builder = PersonPropertyInitialData.builder();
			builder.setPersonPropertyValue(new PersonId(0), TestPersonPropertyId.PERSON_PROPERTY_1_BOOLEAN_MUTABLE_NO_TRACK, false);
			builder.build();
		});
		assertEquals(PersonPropertyError.UNKNOWN_PERSON_PROPERTY_ID, contractException.getErrorType());

		/*
		 * if a person has been assigned a value for a property id that is not
		 * compatible with the corresponding property definition.
		 */
		contractException = assertThrows(ContractException.class, () -> {
			PersonPropertyInitialData.Builder personPropertyBuilder = PersonPropertyInitialData.builder();
			TestPersonPropertyId propertyId = TestPersonPropertyId.PERSON_PROPERTY_1_BOOLEAN_MUTABLE_NO_TRACK;
			personPropertyBuilder.definePersonProperty(propertyId, propertyId.getPropertyDefinition());
			personPropertyBuilder.setPersonPropertyValue(new PersonId(0), TestPersonPropertyId.PERSON_PROPERTY_1_BOOLEAN_MUTABLE_NO_TRACK, 1);
			personPropertyBuilder.build();

		});
		assertEquals(PropertyError.INCOMPATIBLE_VALUE, contractException.getErrorType());

	}

	@Test
	@UnitTestMethod(target = PersonPropertyInitialData.Builder.class, name = "definePersonProperty", args = { PersonPropertyId.class, PropertyDefinition.class })
	public void testDefinePersonProperty() {
		// create a builder
		PersonPropertyInitialData.Builder personPropertyBuilder = PersonPropertyInitialData.builder();

		// fill the builder with property definitions
		for (TestPersonPropertyId testPersonPropertyId : TestPersonPropertyId.values()) {
			personPropertyBuilder.definePersonProperty(testPersonPropertyId, testPersonPropertyId.getPropertyDefinition());
		}

		// build the person property initial data
		PersonPropertyInitialData personPropertyInitialData = personPropertyBuilder.build();

		/*
		 * Show that the definitions returned by the initial data match the
		 * expected definitions.
		 */
		for (TestPersonPropertyId testPersonPropertyId : TestPersonPropertyId.values()) {
			PropertyDefinition expectedPropertyDefinition = testPersonPropertyId.getPropertyDefinition();
			PropertyDefinition actualPropertyDefinition = personPropertyInitialData.getPersonPropertyDefinition(testPersonPropertyId);
			assertEquals(expectedPropertyDefinition, actualPropertyDefinition);
		}

		// precondition tests

		// if the person property id is null
		ContractException contractException = assertThrows(ContractException.class, () -> {
			PersonPropertyInitialData.Builder builder = PersonPropertyInitialData.builder();
			TestPersonPropertyId testPersonPropertyId = TestPersonPropertyId.PERSON_PROPERTY_1_BOOLEAN_MUTABLE_NO_TRACK;
			builder.definePersonProperty(null, testPersonPropertyId.getPropertyDefinition());
		});
		assertEquals(PersonPropertyError.NULL_PERSON_PROPERTY_ID, contractException.getErrorType());

		// if the person property definition value is null
		contractException = assertThrows(ContractException.class, () -> {
			PersonPropertyInitialData.Builder builder = PersonPropertyInitialData.builder();
			TestPersonPropertyId testPersonPropertyId = TestPersonPropertyId.PERSON_PROPERTY_1_BOOLEAN_MUTABLE_NO_TRACK;
			builder.definePersonProperty(testPersonPropertyId, null);
		});
		assertEquals(PersonPropertyError.NULL_PERSON_PROPERTY_DEFINITION, contractException.getErrorType());

		// if the person property definition is already added
		contractException = assertThrows(ContractException.class, () -> {
			PersonPropertyInitialData.Builder builder = PersonPropertyInitialData.builder();
			TestPersonPropertyId testPersonPropertyId = TestPersonPropertyId.PERSON_PROPERTY_1_BOOLEAN_MUTABLE_NO_TRACK;
			builder.definePersonProperty(testPersonPropertyId, testPersonPropertyId.getPropertyDefinition());
			builder.definePersonProperty(testPersonPropertyId, testPersonPropertyId.getPropertyDefinition());
		});
		assertEquals(PersonPropertyError.DUPLICATE_PERSON_PROPERTY_DEFINITION, contractException.getErrorType());

		// if the person property definition does not have a default value
		contractException = assertThrows(ContractException.class, () -> {
			PersonPropertyInitialData.Builder builder = PersonPropertyInitialData.builder();
			TestPersonPropertyId testPersonPropertyId = TestPersonPropertyId.PERSON_PROPERTY_1_BOOLEAN_MUTABLE_NO_TRACK;
			PropertyDefinition propertyDefinition = PropertyDefinition	.builder()//
																		.setType(Boolean.class)//
																		// .setDefaultValue(false)//
																		.setPropertyValueMutability(true)//
																		.setTimeTrackingPolicy(TimeTrackingPolicy.DO_NOT_TRACK_TIME)//
																		.build();

			builder.definePersonProperty(testPersonPropertyId, propertyDefinition);

		});
		assertEquals(PersonPropertyError.PROPERTY_DEFINITION_REQUIRES_DEFAULT, contractException.getErrorType());

	}

	@Test
	@UnitTestMethod(target = PersonPropertyInitialData.Builder.class, name = "setPersonPropertyValue", args = { PersonId.class, PersonPropertyId.class, Object.class })
	public void testSetPersonPropertyValue() {
		// create a builder
		PersonPropertyInitialData.Builder personPropertyBuilder = PersonPropertyInitialData.builder();

		// add property 2
		TestPersonPropertyId testPersonPropertyId = TestPersonPropertyId.PERSON_PROPERTY_2_INTEGER_MUTABLE_NO_TRACK;
		personPropertyBuilder.definePersonProperty(testPersonPropertyId, testPersonPropertyId.getPropertyDefinition());

		RandomGenerator randomGenerator = SeedProvider.getRandomGenerator(2018450241720666345L);

		// build a container for expectations
		Map<PersonId, Integer> expectedValues = new LinkedHashMap<>();
		for (int i = 0; i < 20; i++) {
			expectedValues.put(new PersonId(i), randomGenerator.nextInt());
		}

		// set some person property values
		for (PersonId personId : expectedValues.keySet()) {
			Integer value = expectedValues.get(personId);
			personPropertyBuilder.setPersonPropertyValue(personId, testPersonPropertyId, value);
		}

		// build the person property initial data
		PersonPropertyInitialData personPropertyInitialData = personPropertyBuilder.build();

		/*
		 * Show that the values are present in the initial data.
		 */
		for (PersonId personId : expectedValues.keySet()) {
			Integer expectedValue = expectedValues.get(personId);
			Object actualValue = personPropertyInitialData.getPersonPropertyValue(personId, testPersonPropertyId);
			assertEquals(expectedValue, actualValue);
		}

		// precondition tests

		// if the person id is null
		ContractException contractException = assertThrows(ContractException.class, () -> {
			PersonPropertyInitialData.Builder builder = PersonPropertyInitialData.builder();
			builder.setPersonPropertyValue(null, testPersonPropertyId, 1);
		});
		assertEquals(PersonError.NULL_PERSON_ID, contractException.getErrorType());

		// if the person property id is null
		contractException = assertThrows(ContractException.class, () -> {
			PersonPropertyInitialData.Builder builder = PersonPropertyInitialData.builder();
			builder.setPersonPropertyValue(new PersonId(0), null, 1);
		});
		assertEquals(PersonPropertyError.NULL_PERSON_PROPERTY_ID, contractException.getErrorType());

		// if the person property value is null
		contractException = assertThrows(ContractException.class, () -> {
			PersonPropertyInitialData.Builder builder = PersonPropertyInitialData.builder();
			builder.setPersonPropertyValue(new PersonId(0), testPersonPropertyId, null);
		});
		assertEquals(PersonPropertyError.NULL_PERSON_PROPERTY_VALUE, contractException.getErrorType());

		// if the person property value is already assigned
		contractException = assertThrows(ContractException.class, () -> {
			PersonPropertyInitialData.Builder builder = PersonPropertyInitialData.builder();
			builder.setPersonPropertyValue(new PersonId(0), testPersonPropertyId, 14);
			builder.setPersonPropertyValue(new PersonId(0), testPersonPropertyId, 14);
		});
		assertEquals(PersonPropertyError.DUPLICATE_PERSON_PROPERTY_VALUE_ASSIGNMENT, contractException.getErrorType());
	}

	@Test
	@UnitTestMethod(name = "getPersonPropertyDefinition", args = { PersonPropertyId.class })
	public void testGetPersonPropertyDefinition() {
		// create a builder
		PersonPropertyInitialData.Builder personPropertyBuilder = PersonPropertyInitialData.builder();

		// fill the builder with property definitions
		for (TestPersonPropertyId testPersonPropertyId : TestPersonPropertyId.values()) {
			personPropertyBuilder.definePersonProperty(testPersonPropertyId, testPersonPropertyId.getPropertyDefinition());
		}

		// build the person property initial data
		PersonPropertyInitialData personPropertyInitialData = personPropertyBuilder.build();

		/*
		 * Show that the definitions returned by the initial data match the
		 * expected definitions.
		 */
		for (TestPersonPropertyId testPersonPropertyId : TestPersonPropertyId.values()) {
			PropertyDefinition expectedPropertyDefinition = testPersonPropertyId.getPropertyDefinition();
			PropertyDefinition actualPropertyDefinition = personPropertyInitialData.getPersonPropertyDefinition(testPersonPropertyId);
			assertEquals(expectedPropertyDefinition, actualPropertyDefinition);
		}

		// precondition tests

		// if the person property id is null
		ContractException contractException = assertThrows(ContractException.class, () -> personPropertyInitialData.getPersonPropertyDefinition(null));
		assertEquals(PersonPropertyError.NULL_PERSON_PROPERTY_ID, contractException.getErrorType());

		// if the person property id is unknown
		contractException = assertThrows(ContractException.class, () -> personPropertyInitialData.getPersonPropertyDefinition(TestPersonPropertyId.getUnknownPersonPropertyId()));
		assertEquals(PersonPropertyError.UNKNOWN_PERSON_PROPERTY_ID, contractException.getErrorType());

	}

	@Test
	@UnitTestMethod(name = "getPersonPropertyIds", args = {})
	public void testGetPersonPropertyIds() {

		// create a builder
		PersonPropertyInitialData.Builder personPropertyBuilder = PersonPropertyInitialData.builder();

		// fill the builder with property definitions
		for (TestPersonPropertyId testPersonPropertyId : TestPersonPropertyId.values()) {
			personPropertyBuilder.definePersonProperty(testPersonPropertyId, testPersonPropertyId.getPropertyDefinition());
		}

		// build the person property initial data
		PersonPropertyInitialData personPropertyInitialData = personPropertyBuilder.build();

		/*
		 * Show that the person proproperty ids match expectations
		 */
		assertEquals(EnumSet.allOf(TestPersonPropertyId.class), personPropertyInitialData.getPersonPropertyIds());

	}

	@Test
	@UnitTestMethod(name = "getPersonPropertyValue", args = { PersonId.class, PersonPropertyId.class })
	public void testGetPersonPropertyValue() {

		// create a builder
		PersonPropertyInitialData.Builder personPropertyBuilder = PersonPropertyInitialData.builder();

		// add property 2
		TestPersonPropertyId testPersonPropertyId = TestPersonPropertyId.PERSON_PROPERTY_2_INTEGER_MUTABLE_NO_TRACK;
		personPropertyBuilder.definePersonProperty(testPersonPropertyId, testPersonPropertyId.getPropertyDefinition());

		RandomGenerator randomGenerator = SeedProvider.getRandomGenerator(7199268339748368663L);

		// build a container for expectations
		Map<PersonId, Integer> expectedValues = new LinkedHashMap<>();
		for (int i = 0; i < 20; i++) {
			expectedValues.put(new PersonId(i), randomGenerator.nextInt());
		}

		// set some person property values
		for (PersonId personId : expectedValues.keySet()) {
			Integer value = expectedValues.get(personId);
			personPropertyBuilder.setPersonPropertyValue(personId, testPersonPropertyId, value);
		}

		// build the person property initial data
		PersonPropertyInitialData personPropertyInitialData = personPropertyBuilder.build();

		/*
		 * Show that the values are present in the initial data.
		 */
		for (PersonId personId : expectedValues.keySet()) {
			Integer expectedValue = expectedValues.get(personId);
			Object actualValue = personPropertyInitialData.getPersonPropertyValue(personId, testPersonPropertyId);
			assertEquals(expectedValue, actualValue);
		}

		// precondition tests

		PersonId personId = new PersonId(0);
		PersonPropertyId unknownPersonPropertyId = TestPersonPropertyId.getUnknownPersonPropertyId();

		// if the person id is null
		ContractException contractException = assertThrows(ContractException.class, () -> personPropertyInitialData.getPersonPropertyValue(null, testPersonPropertyId));
		assertEquals(PersonError.NULL_PERSON_ID, contractException.getErrorType());

		// if the person id property is null
		contractException = assertThrows(ContractException.class, () -> personPropertyInitialData.getPersonPropertyValue(personId, null));
		assertEquals(PersonPropertyError.NULL_PERSON_PROPERTY_ID, contractException.getErrorType());

		// if the person id property is unknown
		contractException = assertThrows(ContractException.class, () -> personPropertyInitialData.getPersonPropertyValue(personId, unknownPersonPropertyId));
		assertEquals(PersonPropertyError.UNKNOWN_PERSON_PROPERTY_ID, contractException.getErrorType());

	}

	@Test
	@UnitTestMethod(name = "getPersonIds", args = {})
	public void testGetPersonIds() {

		// create a builder
		PersonPropertyInitialData.Builder personPropertyBuilder = PersonPropertyInitialData.builder();

		// add property 2
		TestPersonPropertyId testPersonPropertyId = TestPersonPropertyId.PERSON_PROPERTY_2_INTEGER_MUTABLE_NO_TRACK;
		personPropertyBuilder.definePersonProperty(testPersonPropertyId, testPersonPropertyId.getPropertyDefinition());

		RandomGenerator randomGenerator = SeedProvider.getRandomGenerator(9089639625207393742L);

		// build a container for expectations
		Set<PersonId> expectedPeople = new LinkedHashSet<>();
		for (int i = 0; i < 20; i++) {
			expectedPeople.add(new PersonId(i));
		}

		// set some person property values
		for (PersonId personId : expectedPeople) {
			Integer value = randomGenerator.nextInt();
			personPropertyBuilder.setPersonPropertyValue(personId, testPersonPropertyId, value);
		}

		// build the person property initial data
		PersonPropertyInitialData personPropertyInitialData = personPropertyBuilder.build();

		/*
		 * Show that the people returned by the initial data match expectations
		 */
		assertEquals(expectedPeople, personPropertyInitialData.getPersonIds());

	}
}
