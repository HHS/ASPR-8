package plugins.globalproperties;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.math3.random.RandomGenerator;
import org.junit.jupiter.api.Test;

import nucleus.PluginData;
import nucleus.PluginDataBuilder;
import plugins.globalproperties.support.GlobalPropertyId;
import plugins.globalproperties.support.SimpleGlobalPropertyId;
import plugins.globalproperties.testsupport.TestGlobalPropertyId;
import plugins.util.properties.PropertyDefinition;
import plugins.util.properties.PropertyError;
import tools.annotations.UnitTestMethod;
import util.errors.ContractException;
import util.random.RandomGeneratorProvider;

public class AT_GlobalPropertiesPluginData {

	@Test
	@UnitTestMethod(target = GlobalPropertiesPluginData.Builder.class, name = "build", args = {})
	public void testBuild() {

		GlobalPropertiesPluginData globalInitialData = GlobalPropertiesPluginData.builder().build();
		assertNotNull(globalInitialData);

		GlobalPropertiesPluginData.Builder builder = GlobalPropertiesPluginData.builder();
		PropertyDefinition propertyDefinition = PropertyDefinition.builder().setType(Integer.class).setDefaultValue(34).build();
		GlobalPropertyId globalPropertyId1 = new SimpleGlobalPropertyId("id 1");
		GlobalPropertyId globalPropertyId2 = new SimpleGlobalPropertyId("id 2");

		/*
		 * precondition test: if a global property value was associated with a
		 * global property id that was not defined
		 */
		builder.defineGlobalProperty(globalPropertyId1, propertyDefinition);
		builder.setGlobalPropertyValue(globalPropertyId2, 67);
		ContractException contractException = assertThrows(ContractException.class, () -> builder.build());
		assertEquals(PropertyError.UNKNOWN_PROPERTY_ID, contractException.getErrorType());

		/*
		 * precondition test: if a global property value was associated with a
		 * global property id that is incompatible with the corresponding
		 * property definition.
		 */
		builder.defineGlobalProperty(globalPropertyId1, propertyDefinition);
		builder.setGlobalPropertyValue(globalPropertyId1, "bad value");
		contractException = assertThrows(ContractException.class, () -> builder.build());
		assertEquals(PropertyError.INCOMPATIBLE_VALUE, contractException.getErrorType());

		/*
		 * precondition test: if a global property definition has no default
		 * value and there is also no corresponding property value assignment.
		 */
		propertyDefinition = PropertyDefinition.builder().setType(Integer.class).build();
		builder.defineGlobalProperty(globalPropertyId1, propertyDefinition);
		contractException = assertThrows(ContractException.class, () -> builder.build());
		assertEquals(PropertyError.INSUFFICIENT_PROPERTY_VALUE_ASSIGNMENT, contractException.getErrorType());

	}

	@Test
	@UnitTestMethod(target = GlobalPropertiesPluginData.Builder.class, name = "defineGlobalProperty", args = { GlobalPropertyId.class, PropertyDefinition.class })
	public void testDefineGlobalProperty() {
		GlobalPropertiesPluginData.Builder builder = GlobalPropertiesPluginData.builder();

		// create a container to hold the expected property definitions
		Map<GlobalPropertyId, PropertyDefinition> expectedPropertyDefinitions = new LinkedHashMap<>();

		// define a few global properties
		PropertyDefinition propertyDefinition = PropertyDefinition.builder().setType(Integer.class).setDefaultValue(34).build();
		PropertyDefinition propertyDefinition2 = PropertyDefinition.builder().setType(Integer.class).setDefaultValue(57).build();
		GlobalPropertyId globalPropertyId = new SimpleGlobalPropertyId("id 1");
		builder.defineGlobalProperty(globalPropertyId, propertyDefinition2);
		// replacing data to show that the value persists
		builder.defineGlobalProperty(globalPropertyId, propertyDefinition);
		// adding duplicate data to show that the value persists
		builder.defineGlobalProperty(globalPropertyId, propertyDefinition);
		expectedPropertyDefinitions.put(globalPropertyId, propertyDefinition);

		propertyDefinition = PropertyDefinition.builder().setType(Double.class).setDefaultValue(234.34).build();
		propertyDefinition2 = PropertyDefinition.builder().setType(Double.class).setDefaultValue(795.88).build();
		globalPropertyId = new SimpleGlobalPropertyId("id 2");
		builder.defineGlobalProperty(globalPropertyId, propertyDefinition2);
		// replacing data to show that the value persists
		builder.defineGlobalProperty(globalPropertyId, propertyDefinition);
		// adding duplicate data to show that the value persists
		builder.defineGlobalProperty(globalPropertyId, propertyDefinition);
		expectedPropertyDefinitions.put(globalPropertyId, propertyDefinition);

		propertyDefinition = PropertyDefinition.builder().setType(String.class).setDefaultValue("default value").build();
		propertyDefinition2 = PropertyDefinition.builder().setType(String.class).setDefaultValue("second default").build();
		globalPropertyId = new SimpleGlobalPropertyId("id 3");
		builder.defineGlobalProperty(globalPropertyId, propertyDefinition2);
		// replacing data to show that the value persists
		builder.defineGlobalProperty(globalPropertyId, propertyDefinition);
		// adding duplicate data to show that the value persists
		builder.defineGlobalProperty(globalPropertyId, propertyDefinition);
		expectedPropertyDefinitions.put(globalPropertyId, propertyDefinition);

		// build the initial data
		GlobalPropertiesPluginData globalInitialData = builder.build();

		// show that the property definitions are retrieved by their ids
		for (GlobalPropertyId gpid : expectedPropertyDefinitions.keySet()) {
			PropertyDefinition expectedPropertyDefinition = expectedPropertyDefinitions.get(gpid);
			PropertyDefinition actualPropertyDefinition = globalInitialData.getGlobalPropertyDefinition(gpid);
			assertEquals(expectedPropertyDefinition, actualPropertyDefinition);
		}

		// precondition tests

		// if the global property id is null
		PropertyDefinition propDef = PropertyDefinition.builder().setType(Integer.class).setDefaultValue(17).build();
		ContractException contractException = assertThrows(ContractException.class, () -> builder.defineGlobalProperty(null, propDef));
		assertEquals(PropertyError.NULL_PROPERTY_ID, contractException.getErrorType());

		// if the property definition is null
		contractException = assertThrows(ContractException.class, () -> builder.defineGlobalProperty(new SimpleGlobalPropertyId("id"), null));
		assertEquals(PropertyError.NULL_PROPERTY_DEFINITION, contractException.getErrorType());
	}

	@Test
	@UnitTestMethod(target = GlobalPropertiesPluginData.Builder.class, name = "setGlobalPropertyValue", args = { GlobalPropertyId.class, Object.class })
	public void testSetGlobalPropertyValue() {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(170390875787254562L);

		GlobalPropertiesPluginData.Builder builder = GlobalPropertiesPluginData.builder();

		// show there are some properties in the support enum
		assertTrue(TestGlobalPropertyId.values().length > 0);

		// define some properties
		for (TestGlobalPropertyId testGlobalPropertyId : TestGlobalPropertyId.values()) {
			PropertyDefinition propertyDefinition = PropertyDefinition.builder().setType(Integer.class).setDefaultValue(0).build();
			builder.defineGlobalProperty(testGlobalPropertyId, propertyDefinition);
		}
		// create a container for the expected values of the properties
		Map<GlobalPropertyId, Integer> expectedValues = new LinkedHashMap<>();

		// set the values
		for (TestGlobalPropertyId testGlobalPropertyId : TestGlobalPropertyId.values()) {
			int value = randomGenerator.nextInt();
			builder.setGlobalPropertyValue(testGlobalPropertyId, value);
			expectedValues.put(testGlobalPropertyId, value);
		}

		// show that the expected values are present
		GlobalPropertiesPluginData globalInitialData = builder.build();
		for (TestGlobalPropertyId testGlobalPropertyId : TestGlobalPropertyId.values()) {
			Integer expectedGlobalPropertyValue = expectedValues.get(testGlobalPropertyId);
			Integer actualGlobalPropertyValue = globalInitialData.getGlobalPropertyValue(testGlobalPropertyId);
			assertEquals(expectedGlobalPropertyValue, actualGlobalPropertyValue);
		}

		/*
		 * precondition tests -- Note that invalid values are not covered here.
		 * The build() validates the values to see if they are compatible with
		 * the corresponding definitions.
		 */

		// if the global property id is null
		ContractException contractException = assertThrows(ContractException.class, () -> builder.setGlobalPropertyValue(null, 5));
		assertEquals(PropertyError.NULL_PROPERTY_ID, contractException.getErrorType());

		// if the global property value is null
		contractException = assertThrows(ContractException.class, () -> builder.setGlobalPropertyValue(TestGlobalPropertyId.GLOBAL_PROPERTY_1_BOOLEAN_MUTABLE, null));
		assertEquals(PropertyError.NULL_PROPERTY_VALUE, contractException.getErrorType());

		// // if the global property value was previously defined for the given
		// // global property id
		// builder.setGlobalPropertyValue(TestGlobalPropertyId.GLOBAL_PROPERTY_1_BOOLEAN_MUTABLE,
		// 4);
		// contractException = assertThrows(ContractException.class, () ->
		// builder.setGlobalPropertyValue(TestGlobalPropertyId.GLOBAL_PROPERTY_1_BOOLEAN_MUTABLE,
		// 5));
		// assertEquals(PropertyError.DUPLICATE_PROPERTY_VALUE_ASSIGNMENT,
		// contractException.getErrorType());

	}

	@Test
	@UnitTestMethod(target = GlobalPropertiesPluginData.class, name = "builder", args = {})
	public void testBuilder() {
		// show that the builder can be created
		assertNotNull(GlobalPropertiesPluginData.builder());
	}

	@Test
	@UnitTestMethod(target = GlobalPropertiesPluginData.class, name = "getGlobalPropertyDefinition", args = { GlobalPropertyId.class })
	public void testGetGlobalPropertyDefinition() {
		GlobalPropertiesPluginData.Builder builder = GlobalPropertiesPluginData.builder();

		// show there are some properties in the support enum
		assertTrue(TestGlobalPropertyId.values().length > 0);

		// create a container for the expected values of the properties
		Map<GlobalPropertyId, PropertyDefinition> expectedGlobalPropertyDefinitions = new LinkedHashMap<>();

		// define some properties

		for (TestGlobalPropertyId testGlobalPropertyId : TestGlobalPropertyId.values()) {
			PropertyDefinition propertyDefinition = PropertyDefinition.builder().setType(Integer.class).setDefaultValue(0).build();
			builder.defineGlobalProperty(testGlobalPropertyId, propertyDefinition);
			expectedGlobalPropertyDefinitions.put(testGlobalPropertyId, propertyDefinition);
		}

		// show that the expected property definitions are present
		GlobalPropertiesPluginData globalInitialData = builder.build();

		for (TestGlobalPropertyId testGlobalPropertyId : TestGlobalPropertyId.values()) {
			PropertyDefinition expectedPropertyDefinition = expectedGlobalPropertyDefinitions.get(testGlobalPropertyId);
			PropertyDefinition actualPropertyDefinition = globalInitialData.getGlobalPropertyDefinition(testGlobalPropertyId);
			assertEquals(expectedPropertyDefinition, actualPropertyDefinition);
		}

		// precondition tests
		ContractException contractException = assertThrows(ContractException.class, () -> globalInitialData.getGlobalPropertyDefinition(null));
		assertEquals(PropertyError.NULL_PROPERTY_ID, contractException.getErrorType());

		contractException = assertThrows(ContractException.class, () -> globalInitialData.getGlobalPropertyDefinition(TestGlobalPropertyId.getUnknownGlobalPropertyId()));
		assertEquals(PropertyError.UNKNOWN_PROPERTY_ID, contractException.getErrorType());

	}

	@Test
	@UnitTestMethod(target = GlobalPropertiesPluginData.class, name = "getGlobalPropertyIds", args = {})
	public void testGetGlobalPropertyIds() {

		GlobalPropertiesPluginData.Builder builder = GlobalPropertiesPluginData.builder();

		// show there are some properties in the support enum
		assertTrue(TestGlobalPropertyId.values().length > 0);

		// create a container for the expected values of the properties
		Set<GlobalPropertyId> expectedGlobalPropertyIds = new LinkedHashSet<>();

		// define some properties

		for (TestGlobalPropertyId testGlobalPropertyId : TestGlobalPropertyId.values()) {
			PropertyDefinition propertyDefinition = PropertyDefinition.builder().setType(Integer.class).setDefaultValue(0).build();
			builder.defineGlobalProperty(testGlobalPropertyId, propertyDefinition);
			expectedGlobalPropertyIds.add(testGlobalPropertyId);
		}

		// show that the expected values are present
		GlobalPropertiesPluginData globalInitialData = builder.build();
		assertEquals(expectedGlobalPropertyIds, globalInitialData.getGlobalPropertyIds());
	}

	@Test
	@UnitTestMethod(target = GlobalPropertiesPluginData.class, name = "getGlobalPropertyValue", args = { GlobalPropertyId.class })
	public void testGetGlobalPropertyValue() {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(4250048639082754761L);

		GlobalPropertiesPluginData.Builder builder = GlobalPropertiesPluginData.builder();

		// show there are some properties in the support enum
		assertTrue(TestGlobalPropertyId.values().length > 0);

		// define some properties -- note that we do not set default values to
		// test that the values provided explicitly will properly replace the
		// default values.
		for (TestGlobalPropertyId testGlobalPropertyId : TestGlobalPropertyId.values()) {
			PropertyDefinition propertyDefinition = PropertyDefinition.builder().setType(Integer.class).build();
			builder.defineGlobalProperty(testGlobalPropertyId, propertyDefinition);
		}
		// create a container for the expected values of the properties
		Map<GlobalPropertyId, Integer> expectedValues = new LinkedHashMap<>();

		// set the values
		for (TestGlobalPropertyId testGlobalPropertyId : TestGlobalPropertyId.values()) {
			int value = randomGenerator.nextInt();
			builder.setGlobalPropertyValue(testGlobalPropertyId, value);
			expectedValues.put(testGlobalPropertyId, value);
		}

		// show that the expected values are present
		GlobalPropertiesPluginData globalInitialData = builder.build();
		for (TestGlobalPropertyId testGlobalPropertyId : TestGlobalPropertyId.values()) {
			Integer expectedGlobalPropertyValue = expectedValues.get(testGlobalPropertyId);
			Integer actualGlobalPropertyValue = globalInitialData.getGlobalPropertyValue(testGlobalPropertyId);
			assertEquals(expectedGlobalPropertyValue, actualGlobalPropertyValue);
		}

		/*
		 * precondition tests -- Note that invalid values are not covered here.
		 * The build() validates the values to see if they are compatible with
		 * the corresponding definitions.
		 */

		// if the global property id is null
		ContractException contractException = assertThrows(ContractException.class, () -> globalInitialData.getGlobalPropertyValue(null));
		assertEquals(PropertyError.NULL_PROPERTY_ID, contractException.getErrorType());

		// if the global property value is null
		contractException = assertThrows(ContractException.class, () -> globalInitialData.getGlobalPropertyValue(TestGlobalPropertyId.getUnknownGlobalPropertyId()));
		assertEquals(PropertyError.UNKNOWN_PROPERTY_ID, contractException.getErrorType());

	}

	@Test
	@UnitTestMethod(target = GlobalPropertiesPluginData.class, name = "getCloneBuilder", args = {})
	public void testGetCloneBuilder() {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(9113503089361379130L);
		GlobalPropertiesPluginData.Builder builder = GlobalPropertiesPluginData.builder();

		for (TestGlobalPropertyId testGlobalPropertyId : TestGlobalPropertyId.values()) {
			builder.defineGlobalProperty(testGlobalPropertyId, testGlobalPropertyId.getPropertyDefinition());
			builder.setGlobalPropertyValue(testGlobalPropertyId, testGlobalPropertyId.getRandomPropertyValue(randomGenerator));
		}

		GlobalPropertiesPluginData globalPropertiesPluginData = builder.build();
		PluginDataBuilder cloneBuilder = globalPropertiesPluginData.getCloneBuilder();
		assertNotNull(cloneBuilder);
		PluginData pluginData = cloneBuilder.build();
		assertTrue(pluginData instanceof GlobalPropertiesPluginData);
		GlobalPropertiesPluginData cloneGlobalPropertiesPluginData = (GlobalPropertiesPluginData) pluginData;

		// show that the two plugin datas have the same property ids
		Set<GlobalPropertyId> expectedGlobalPropertyIds = globalPropertiesPluginData.getGlobalPropertyIds();
		Set<GlobalPropertyId> actualGlobalPropertyIds = cloneGlobalPropertiesPluginData.getGlobalPropertyIds();
		assertEquals(expectedGlobalPropertyIds, actualGlobalPropertyIds);

		// show that the two plugin datas have the same property definitions
		for (GlobalPropertyId globalPropertyId : globalPropertiesPluginData.getGlobalPropertyIds()) {
			PropertyDefinition expectedPropertyDefinition = globalPropertiesPluginData.getGlobalPropertyDefinition(globalPropertyId);
			PropertyDefinition actualPropertyDefinition = cloneGlobalPropertiesPluginData.getGlobalPropertyDefinition(globalPropertyId);
			assertEquals(expectedPropertyDefinition, actualPropertyDefinition);
		}

		// show that the two plugin datas have the same values
		for (GlobalPropertyId globalPropertyId : globalPropertiesPluginData.getGlobalPropertyIds()) {
			Object expectedValues = globalPropertiesPluginData.getGlobalPropertyValue(globalPropertyId);
			Object actualValues = cloneGlobalPropertiesPluginData.getGlobalPropertyValue(globalPropertyId);
			assertEquals(expectedValues, actualValues);
		}

	}

}
