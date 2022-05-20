package plugins.personproperties;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.EnumSet;
import java.util.Set;

import org.apache.commons.math3.random.RandomGenerator;
import org.junit.jupiter.api.Test;

import nucleus.PluginData;
import nucleus.PluginDataBuilder;
import plugins.people.support.PersonId;
import plugins.personproperties.support.PersonPropertyError;
import plugins.personproperties.support.PersonPropertyId;
import plugins.personproperties.testsupport.TestPersonPropertyId;
import plugins.util.properties.PropertyDefinition;
import plugins.util.properties.PropertyError;
import plugins.util.properties.TimeTrackingPolicy;
import tools.annotations.UnitTest;
import tools.annotations.UnitTestMethod;
import util.errors.ContractException;
import util.random.RandomGeneratorProvider;

@UnitTest(target = PersonPropertiesPluginData.class)
public class AT_PersonPropertyPluginData {

	@Test
	@UnitTestMethod(name = "builder", args = {})
	public void testBuilder() {
		assertNotNull(PersonPropertiesPluginData.builder());
	}

	@Test
	@UnitTestMethod(target = PersonPropertiesPluginData.Builder.class, name = "build", args = {})
	public void testBuild() {
		assertNotNull(PersonPropertiesPluginData.builder().build());
	}

	@Test
	@UnitTestMethod(target = PersonPropertiesPluginData.Builder.class, name = "definePersonProperty", args = { PersonPropertyId.class, PropertyDefinition.class })
	public void testDefinePersonProperty() {
		// create a builder
		PersonPropertiesPluginData.Builder personPropertyBuilder = PersonPropertiesPluginData.builder();

		// fill the builder with property definitions
		for (TestPersonPropertyId testPersonPropertyId : TestPersonPropertyId.values()) {
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
		assertEquals(PersonPropertyError.NULL_PERSON_PROPERTY_ID, contractException.getErrorType());

		// if the person property definition value is null
		contractException = assertThrows(ContractException.class, () -> {
			PersonPropertiesPluginData.Builder builder = PersonPropertiesPluginData.builder();
			TestPersonPropertyId testPersonPropertyId = TestPersonPropertyId.PERSON_PROPERTY_1_BOOLEAN_MUTABLE_NO_TRACK;
			builder.definePersonProperty(testPersonPropertyId, null);
		});
		assertEquals(PersonPropertyError.NULL_PERSON_PROPERTY_DEFINITION, contractException.getErrorType());

		// if the person property definition is already added
		contractException = assertThrows(ContractException.class, () -> {
			PersonPropertiesPluginData.Builder builder = PersonPropertiesPluginData.builder();
			TestPersonPropertyId testPersonPropertyId = TestPersonPropertyId.PERSON_PROPERTY_1_BOOLEAN_MUTABLE_NO_TRACK;
			builder.definePersonProperty(testPersonPropertyId, testPersonPropertyId.getPropertyDefinition());
			builder.definePersonProperty(testPersonPropertyId, testPersonPropertyId.getPropertyDefinition());
		});
		assertEquals(PersonPropertyError.DUPLICATE_PERSON_PROPERTY_DEFINITION, contractException.getErrorType());

		// if the person property definition does not have a default value
		contractException = assertThrows(ContractException.class, () -> {
			PersonPropertiesPluginData.Builder builder = PersonPropertiesPluginData.builder();
			TestPersonPropertyId testPersonPropertyId = TestPersonPropertyId.PERSON_PROPERTY_1_BOOLEAN_MUTABLE_NO_TRACK;
			PropertyDefinition propertyDefinition = PropertyDefinition	.builder()//
																		.setType(Boolean.class)//
																		// .setDefaultValue(false)//
																		.setPropertyValueMutability(true)//
																		.setTimeTrackingPolicy(TimeTrackingPolicy.DO_NOT_TRACK_TIME)//
																		.build();

			builder.definePersonProperty(testPersonPropertyId, propertyDefinition);

		});
		assertEquals(PropertyError.PROPERTY_DEFINITION_MISSING_DEFAULT, contractException.getErrorType());

	}

	@Test
	@UnitTestMethod(name = "getPersonPropertyDefinition", args = { PersonPropertyId.class })
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
		assertEquals(PersonPropertyError.NULL_PERSON_PROPERTY_ID, contractException.getErrorType());

		// if the person property id is unknown
		contractException = assertThrows(ContractException.class, () -> personPropertiesPluginData.getPersonPropertyDefinition(TestPersonPropertyId.getUnknownPersonPropertyId()));
		assertEquals(PersonPropertyError.UNKNOWN_PERSON_PROPERTY_ID, contractException.getErrorType());

	}

	@Test
	@UnitTestMethod(name = "getPersonPropertyIds", args = {})
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
	@UnitTestMethod(name = "getCloneBuilder", args = {})
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
				if (randomGenerator.nextBoolean()) {
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
		Set<PersonId> expectedPersonIds = personPropertiesPluginData.getPersonIds();
		Set<PersonId> actualPersonIds = clonePersonPropertiesPluginData.getPersonIds();
		assertEquals(expectedPersonIds, actualPersonIds);


		for(PersonId personId : personPropertiesPluginData.getPersonIds()) {
			for(PersonPropertyId  personPropertyId : personPropertiesPluginData.getPersonPropertyIds()) {
				Object expectedPropertyValue = personPropertiesPluginData.getPersonPropertyValue(personId, personPropertyId);
				Object actualPropertyValue = clonePersonPropertiesPluginData.getPersonPropertyValue(personId, personPropertyId);
				assertEquals(expectedPropertyValue,actualPropertyValue);
			}
		}
		
	}
}
