package plugins.personproperties;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.math3.random.RandomGenerator;
import org.junit.jupiter.api.Test;

import nucleus.PluginData;
import nucleus.PluginDataBuilder;
import plugins.people.support.PersonError;
import plugins.people.support.PersonId;
import plugins.personproperties.support.PersonPropertyId;
import plugins.personproperties.support.PersonPropertyInitialization;
import plugins.personproperties.testsupport.TestPersonPropertyId;
import plugins.util.properties.PropertyDefinition;
import plugins.util.properties.PropertyError;
import tools.annotations.UnitTestMethod;
import util.errors.ContractException;
import util.random.RandomGeneratorProvider;

public class AT_PersonPropertyPluginData {

	@Test
	@UnitTestMethod(target = PersonPropertiesPluginData.class, name = "builder", args = {})
	public void testBuilder() {
		assertNotNull(PersonPropertiesPluginData.builder());
	}

	@Test
	@UnitTestMethod(target = PersonPropertiesPluginData.Builder.class, name = "build", args = {})
	public void testBuild() {

		assertNotNull(PersonPropertiesPluginData.builder().build());

		/*
		 * precondition test: if a person is assigned a property value for a
		 * property that was not defined
		 */
		ContractException contractException = assertThrows(ContractException.class, //
				() -> PersonPropertiesPluginData.builder()//
												.setPersonPropertyValue(new PersonId(0), TestPersonPropertyId.PERSON_PROPERTY_1_BOOLEAN_MUTABLE_NO_TRACK, true)//
												.build());//
		assertEquals(PropertyError.UNKNOWN_PROPERTY_ID, contractException.getErrorType());

		/*
		 * precondition test: if a person is assigned a property value that is
		 * incompatible with the associated property definition
		 */
		contractException = assertThrows(ContractException.class, //
				() -> {//
					TestPersonPropertyId testPersonPropertyId = TestPersonPropertyId.PERSON_PROPERTY_1_BOOLEAN_MUTABLE_NO_TRACK;
					PropertyDefinition propertyDefinition = testPersonPropertyId.getPropertyDefinition();
					PersonPropertiesPluginData	.builder()//
												.definePersonProperty(testPersonPropertyId, propertyDefinition)//
												.setPersonPropertyValue(new PersonId(0), TestPersonPropertyId.PERSON_PROPERTY_1_BOOLEAN_MUTABLE_NO_TRACK, 45)//
												.build();//
				});//
		assertEquals(PropertyError.INCOMPATIBLE_VALUE, contractException.getErrorType());

		/*
		 * precondition test: if a person is not assigned a property value for a
		 * property id where the associated property definition does not contain
		 * a default value
		 */
		contractException = assertThrows(ContractException.class, //
				() -> {//

					TestPersonPropertyId prop1 = TestPersonPropertyId.PERSON_PROPERTY_1_BOOLEAN_MUTABLE_NO_TRACK;
					PropertyDefinition def1 = prop1.getPropertyDefinition();

					// this property has no associated default value
					TestPersonPropertyId prop2 = TestPersonPropertyId.PERSON_PROPERTY_9_DOUBLE_IMMUTABLE_NO_TRACK;
					PropertyDefinition def2 = prop2.getPropertyDefinition();

					PersonPropertiesPluginData	.builder()//
												.definePersonProperty(prop1, def1)//
												.definePersonProperty(prop2, def2)//
												.setPersonPropertyValue(new PersonId(0), prop1, false).build();//
				});//
		assertEquals(PropertyError.INSUFFICIENT_PROPERTY_VALUE_ASSIGNMENT, contractException.getErrorType());

	}

	@Test
	@UnitTestMethod(target = PersonPropertiesPluginData.Builder.class, name = "definePersonProperty", args = { PersonPropertyId.class, PropertyDefinition.class })
	public void testDefinePersonProperty() {

		// create a builder
		PersonPropertiesPluginData.Builder personPropertyBuilder = PersonPropertiesPluginData.builder();

		// fill the builder with property definitions
		for (TestPersonPropertyId testPersonPropertyId : TestPersonPropertyId.values()) {
			TestPersonPropertyId testPersonPropertyId2 = testPersonPropertyId.next();
			personPropertyBuilder.definePersonProperty(testPersonPropertyId, testPersonPropertyId2.getPropertyDefinition());
			// replacing data to show that the value persists
			personPropertyBuilder.definePersonProperty(testPersonPropertyId, testPersonPropertyId.getPropertyDefinition());
			// adding duplicate data to show that the value persists
			personPropertyBuilder.definePersonProperty(testPersonPropertyId, testPersonPropertyId.getPropertyDefinition());
		}

		// build the person property initial data
		PersonPropertiesPluginData personPropertyInitialData = personPropertyBuilder.build();

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
			PersonPropertiesPluginData.Builder builder = PersonPropertiesPluginData.builder();
			TestPersonPropertyId testPersonPropertyId = TestPersonPropertyId.PERSON_PROPERTY_1_BOOLEAN_MUTABLE_NO_TRACK;
			builder.definePersonProperty(null, testPersonPropertyId.getPropertyDefinition());
		});
		assertEquals(PropertyError.NULL_PROPERTY_ID, contractException.getErrorType());

		// if the person property definition value is null
		contractException = assertThrows(ContractException.class, () -> {
			PersonPropertiesPluginData.Builder builder = PersonPropertiesPluginData.builder();
			TestPersonPropertyId testPersonPropertyId = TestPersonPropertyId.PERSON_PROPERTY_1_BOOLEAN_MUTABLE_NO_TRACK;
			builder.definePersonProperty(testPersonPropertyId, null);
		});
		assertEquals(PropertyError.NULL_PROPERTY_DEFINITION, contractException.getErrorType());
	}

	@Test
	@UnitTestMethod(target = PersonPropertiesPluginData.class, name = "getPersonPropertyDefinition", args = { PersonPropertyId.class })
	public void testGetPersonPropertyDefinition() {
		// create a builder
		PersonPropertiesPluginData.Builder personPropertyBuilder = PersonPropertiesPluginData.builder();

		// fill the builder with property definitions
		for (TestPersonPropertyId testPersonPropertyId : TestPersonPropertyId.values()) {
			personPropertyBuilder.definePersonProperty(testPersonPropertyId, testPersonPropertyId.getPropertyDefinition());
		}

		// build the person property initial data
		PersonPropertiesPluginData personPropertiesPluginData = personPropertyBuilder.build();

		/*
		 * Show that the definitions returned by the initial data match the
		 * expected definitions.
		 */
		for (TestPersonPropertyId testPersonPropertyId : TestPersonPropertyId.values()) {
			PropertyDefinition expectedPropertyDefinition = testPersonPropertyId.getPropertyDefinition();
			PropertyDefinition actualPropertyDefinition = personPropertiesPluginData.getPersonPropertyDefinition(testPersonPropertyId);
			assertEquals(expectedPropertyDefinition, actualPropertyDefinition);
		}

		// precondition tests

		// if the person property id is null
		ContractException contractException = assertThrows(ContractException.class, () -> personPropertiesPluginData.getPersonPropertyDefinition(null));
		assertEquals(PropertyError.NULL_PROPERTY_ID, contractException.getErrorType());

		// if the person property id is unknown
		contractException = assertThrows(ContractException.class, () -> personPropertiesPluginData.getPersonPropertyDefinition(TestPersonPropertyId.getUnknownPersonPropertyId()));
		assertEquals(PropertyError.UNKNOWN_PROPERTY_ID, contractException.getErrorType());

	}

	@Test
	@UnitTestMethod(target = PersonPropertiesPluginData.class, name = "getPersonPropertyIds", args = {})
	public void testGetPersonPropertyIds() {

		// create a builder
		PersonPropertiesPluginData.Builder personPropertyBuilder = PersonPropertiesPluginData.builder();

		// fill the builder with property definitions
		for (TestPersonPropertyId testPersonPropertyId : TestPersonPropertyId.values()) {
			personPropertyBuilder.definePersonProperty(testPersonPropertyId, testPersonPropertyId.getPropertyDefinition());
		}

		// build the person property initial data
		PersonPropertiesPluginData personPropertyInitialData = personPropertyBuilder.build();

		/*
		 * Show that the person proproperty ids match expectations
		 */
		assertEquals(EnumSet.allOf(TestPersonPropertyId.class), personPropertyInitialData.getPersonPropertyIds());

	}

	@Test
	@UnitTestMethod(target = PersonPropertiesPluginData.class, name = "getCloneBuilder", args = {})
	public void testGetCloneBuilder() {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(3666595741799189966L);
		PersonPropertiesPluginData.Builder pluginBuilder = PersonPropertiesPluginData.builder();

		for (TestPersonPropertyId testPersonPropertyId : TestPersonPropertyId.values()) {
			pluginBuilder.definePersonProperty(testPersonPropertyId, testPersonPropertyId.getPropertyDefinition());
		}
		int personCount = 100;
		for (int i = 0; i < personCount; i++) {
			PersonId personId = new PersonId(i);
			for (TestPersonPropertyId testPersonPropertyId : TestPersonPropertyId.values()) {
				boolean hasDefault = testPersonPropertyId.getPropertyDefinition().getDefaultValue().isPresent();
				if (!hasDefault || randomGenerator.nextBoolean()) {
					Object randomPropertyValue = testPersonPropertyId.getRandomPropertyValue(randomGenerator);
					pluginBuilder.setPersonPropertyValue(personId, testPersonPropertyId, randomPropertyValue);
				}
			}
		}

		PersonPropertiesPluginData personPropertiesPluginData = pluginBuilder.build();

		PluginDataBuilder cloneBuilder = personPropertiesPluginData.getCloneBuilder();
		assertNotNull(cloneBuilder);
		PluginData pluginData = cloneBuilder.build();
		assertTrue(pluginData instanceof PersonPropertiesPluginData);
		PersonPropertiesPluginData clonePersonPropertiesPluginData = (PersonPropertiesPluginData) pluginData;

		// show that the two plugin datas have the same property ids
		Set<PersonPropertyId> expectedPersonPropertyIds = personPropertiesPluginData.getPersonPropertyIds();
		Set<PersonPropertyId> actualPersonPropertyIds = clonePersonPropertiesPluginData.getPersonPropertyIds();
		assertEquals(expectedPersonPropertyIds, actualPersonPropertyIds);

		// show that the two plugin datas have the same property definitions
		for (PersonPropertyId personPropertyId : personPropertiesPluginData.getPersonPropertyIds()) {
			PropertyDefinition expectedPropertyDefinition = personPropertiesPluginData.getPersonPropertyDefinition(personPropertyId);
			PropertyDefinition actualPropertyDefinition = clonePersonPropertiesPluginData.getPersonPropertyDefinition(personPropertyId);
			assertEquals(expectedPropertyDefinition, actualPropertyDefinition);
		}

		// show that the two plugin datas have the same people
		int expectedPersonCount = personPropertiesPluginData.getPersonCount();
		int actualPersonCount = clonePersonPropertiesPluginData.getPersonCount();
		assertEquals(expectedPersonCount, actualPersonCount);

		for (int personIndex = 0; personIndex < personPropertiesPluginData.getPersonCount(); personIndex++) {
			List<PersonPropertyInitialization> expectedPropertyValues = personPropertiesPluginData.getPropertyValues(personIndex);
			List<PersonPropertyInitialization> actualPropertyValues = clonePersonPropertiesPluginData.getPropertyValues(personIndex);
			assertEquals(expectedPropertyValues, actualPropertyValues);
		}

	}

	@Test
	@UnitTestMethod(target = PersonPropertiesPluginData.Builder.class, name = "setPersonPropertyValue", args = { PersonId.class, PersonPropertyId.class, Object.class })
	public void testSetPersonPropertyValue() {

		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(6340277988168121078L);

		// create a builder
		PersonPropertiesPluginData.Builder personPropertyBuilder = PersonPropertiesPluginData.builder();

		// fill the builder with property definitions
		List<TestPersonPropertyId> propertiesWithoutDefaultValues = TestPersonPropertyId.getPropertiesWithoutDefaultValues();
		for (TestPersonPropertyId testPersonPropertyId : TestPersonPropertyId.values()) {
			PropertyDefinition propertyDefinition = testPersonPropertyId.getPropertyDefinition();
			personPropertyBuilder.definePersonProperty(testPersonPropertyId, propertyDefinition);
		}

		List<List<PersonPropertyInitialization>> expectedPropertyValues = new ArrayList<>();

		int personCount = 50;
		for (int i = 0; i < personCount; i++) {
			List<PersonPropertyInitialization> list = new ArrayList<>();
			expectedPropertyValues.add(list);
			PersonId personId = new PersonId(i);
			int propertyCount = randomGenerator.nextInt(3);
			for (int j = 0; j < propertyCount; j++) {
				TestPersonPropertyId testPersonPropertyId = TestPersonPropertyId.getRandomPersonPropertyId(randomGenerator);
				Object value = testPersonPropertyId.getRandomPropertyValue(randomGenerator);
				personPropertyBuilder.setPersonPropertyValue(personId, testPersonPropertyId, value);
				list.add(new PersonPropertyInitialization(testPersonPropertyId, value));
			}
			for (TestPersonPropertyId testPersonPropertyId : propertiesWithoutDefaultValues) {
				Object value = testPersonPropertyId.getRandomPropertyValue(randomGenerator);
				personPropertyBuilder.setPersonPropertyValue(personId, testPersonPropertyId, value);
				list.add(new PersonPropertyInitialization(testPersonPropertyId, value));
			}

		}

		// build the person property initial data
		PersonPropertiesPluginData personPropertyInitialData = personPropertyBuilder.build();

		/*
		 * Show that property values match expectations
		 */

		for (int i = 0; i < personCount; i++) {
			List<PersonPropertyInitialization> expectedList = expectedPropertyValues.get(i);
			List<PersonPropertyInitialization> actualList = personPropertyInitialData.getPropertyValues(i);
			assertEquals(expectedList, actualList);
		}

		// precondition test: if the person id is null
		ContractException contractException = assertThrows(ContractException.class, () -> {
			PersonPropertiesPluginData.Builder builder = PersonPropertiesPluginData.builder();
			TestPersonPropertyId testPersonPropertyId = TestPersonPropertyId.PERSON_PROPERTY_1_BOOLEAN_MUTABLE_NO_TRACK;
			builder.setPersonPropertyValue(null, testPersonPropertyId, true);
		});
		assertEquals(PersonError.NULL_PERSON_ID, contractException.getErrorType());

		// precondition test: if the person property value is null
		contractException = assertThrows(ContractException.class, () -> {
			PersonPropertiesPluginData.Builder builder = PersonPropertiesPluginData.builder();
			TestPersonPropertyId testPersonPropertyId = TestPersonPropertyId.PERSON_PROPERTY_1_BOOLEAN_MUTABLE_NO_TRACK;
			builder.setPersonPropertyValue(new PersonId(0), testPersonPropertyId, null);
		});
		assertEquals(PropertyError.NULL_PROPERTY_VALUE, contractException.getErrorType());

		// precondition test: if the person property id is null
		contractException = assertThrows(ContractException.class, () -> {
			PersonPropertiesPluginData.Builder builder = PersonPropertiesPluginData.builder();
			builder.setPersonPropertyValue(new PersonId(0), null, true);
		});
		assertEquals(PropertyError.NULL_PROPERTY_ID, contractException.getErrorType());

	}

	@Test
	@UnitTestMethod(target = PersonPropertiesPluginData.class, name = "getPersonCount", args = {})
	public void testGetPersonCount() {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(3422619272027560361L);

		// create a builder
		PersonPropertiesPluginData.Builder personPropertyBuilder = PersonPropertiesPluginData.builder();

		// fill the builder with property definitions

		List<TestPersonPropertyId> propertiesWithDefaultValues = TestPersonPropertyId.getPropertiesWithDefaultValues();
		for (TestPersonPropertyId testPersonPropertyId : TestPersonPropertyId.getPropertiesWithDefaultValues()) {
			personPropertyBuilder.definePersonProperty(testPersonPropertyId, testPersonPropertyId.getPropertyDefinition());
		}

		int personCount = 50;
		int expectedPersonCount = 0;
		for (int i = 0; i < personCount; i++) {

			if (randomGenerator.nextBoolean()) {
				PersonId personId = new PersonId(i);
				int propertyCount = randomGenerator.nextInt(5);
				if (propertyCount > 0) {
					expectedPersonCount = i + 1;
				}
				for (int j = 0; j < propertyCount; j++) {
					TestPersonPropertyId testPersonPropertyId = propertiesWithDefaultValues.get(randomGenerator.nextInt(propertiesWithDefaultValues.size()));
					Object value = testPersonPropertyId.getRandomPropertyValue(randomGenerator);
					personPropertyBuilder.setPersonPropertyValue(personId, testPersonPropertyId, value);
				}
			}
		}

		// build the person property initial data
		PersonPropertiesPluginData personPropertyInitialData = personPropertyBuilder.build();

		/*
		 * Show that the personCount matches expectations
		 */
		int actualPersonCount = personPropertyInitialData.getPersonCount();

		assertEquals(expectedPersonCount, actualPersonCount);

	}

	@Test
	@UnitTestMethod(target = PersonPropertiesPluginData.class, name = "getPropertyValues", args = { int.class })
	public void testGetPropertyValues() {
		// covered by testSetPersonPropertyValues()
	}

}
