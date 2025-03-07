package gov.hhs.aspr.ms.gcm.simulation.plugins.globalproperties.datamanagers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.Set;

import org.apache.commons.math3.random.RandomGenerator;
import org.junit.jupiter.api.Test;

import gov.hhs.aspr.ms.gcm.simulation.nucleus.StandardVersioning;
import gov.hhs.aspr.ms.gcm.simulation.plugins.globalproperties.support.GlobalPropertyId;
import gov.hhs.aspr.ms.gcm.simulation.plugins.globalproperties.support.SimpleGlobalPropertyId;
import gov.hhs.aspr.ms.gcm.simulation.plugins.globalproperties.testsupport.GlobalPropertiesTestPluginFactory;
import gov.hhs.aspr.ms.gcm.simulation.plugins.globalproperties.testsupport.TestGlobalPropertyId;
import gov.hhs.aspr.ms.gcm.simulation.plugins.properties.support.PropertyDefinition;
import gov.hhs.aspr.ms.gcm.simulation.plugins.properties.support.PropertyError;
import gov.hhs.aspr.ms.util.annotations.UnitTestMethod;
import gov.hhs.aspr.ms.util.errors.ContractException;
import gov.hhs.aspr.ms.util.random.RandomGeneratorProvider;

public class AT_GlobalPropertiesPluginData {

	@Test
	@UnitTestMethod(target = GlobalPropertiesPluginData.Builder.class, name = "build", args = {})
	public void testBuild() {

		GlobalPropertiesPluginData globalInitialData = GlobalPropertiesPluginData.builder().build();
		assertNotNull(globalInitialData);

		PropertyDefinition propertyDefinition = PropertyDefinition.builder().setType(Integer.class).setDefaultValue(34)
				.build();
		GlobalPropertyId globalPropertyId1 = new SimpleGlobalPropertyId("id 1");
		GlobalPropertyId globalPropertyId2 = new SimpleGlobalPropertyId("id 2");

		// show that the builder clears its contents on build
		GlobalPropertiesPluginData.Builder builder = GlobalPropertiesPluginData.builder();//
		builder.defineGlobalProperty(globalPropertyId1, propertyDefinition, 0)//
				.build();

		builder = GlobalPropertiesPluginData.builder();//
		GlobalPropertiesPluginData globalPropertiesPluginData = builder.build();
		assertTrue(globalPropertiesPluginData.getGlobalPropertyIds().isEmpty());

		/*
		 * precondition test: if a global property value was associated with a global
		 * property id that was not defined
		 */

		ContractException contractException = assertThrows(ContractException.class, () -> {
			GlobalPropertiesPluginData.builder()//
					.defineGlobalProperty(globalPropertyId1, propertyDefinition, 0)//
					.setGlobalPropertyValue(globalPropertyId2, 67, 0)//
					.build();
		});
		assertEquals(PropertyError.UNKNOWN_PROPERTY_ID, contractException.getErrorType());

		/*
		 * precondition test: if a global property value was associated with a global
		 * property id that is incompatible with the corresponding property definition.
		 */

		contractException = assertThrows(ContractException.class, () -> {
			GlobalPropertiesPluginData.builder()//
					.defineGlobalProperty(globalPropertyId1, propertyDefinition, 0)//
					.setGlobalPropertyValue(globalPropertyId1, "bad value", 0)//
					.build();
		});
		assertEquals(PropertyError.INCOMPATIBLE_VALUE, contractException.getErrorType());

		/*
		 * precondition test: if a global property time is less than the corresponding
		 * property definition creation time.
		 */

		contractException = assertThrows(ContractException.class, () -> {
			GlobalPropertiesPluginData.builder()//
					.defineGlobalProperty(globalPropertyId1, propertyDefinition, 3.6)//
					.setGlobalPropertyValue(globalPropertyId1, 12, 2.2)//
					.build();
		});
		assertEquals(PropertyError.INCOMPATIBLE_TIME, contractException.getErrorType());

		/*
		 * precondition test: if a global property definition has no default value and
		 * there is also no corresponding property value assignment.
		 */
		contractException = assertThrows(ContractException.class, () -> {
			GlobalPropertiesPluginData.builder()//
					.defineGlobalProperty(globalPropertyId1,
							PropertyDefinition.builder().setType(Integer.class).build(), 0)//
					.build();
		});
		assertEquals(PropertyError.INSUFFICIENT_PROPERTY_VALUE_ASSIGNMENT, contractException.getErrorType());

	}

	@Test
	@UnitTestMethod(target = GlobalPropertiesPluginData.Builder.class, name = "defineGlobalProperty", args = {
			GlobalPropertyId.class, PropertyDefinition.class, double.class })
	public void testDefineGlobalProperty() {
		GlobalPropertiesPluginData.Builder builder = GlobalPropertiesPluginData.builder();

		// create a container to hold the expected property definitions
		Map<GlobalPropertyId, PropertyDefinition> expectedPropertyDefinitions = new LinkedHashMap<>();
		Map<GlobalPropertyId, Double> expectedPropertyDefinitionCreationTimes = new LinkedHashMap<>();

		// define a few global properties
		PropertyDefinition propertyDefinition = PropertyDefinition.builder().setType(Integer.class).setDefaultValue(34)
				.build();
		PropertyDefinition propertyDefinition2 = PropertyDefinition.builder().setType(Integer.class).setDefaultValue(57)
				.build();
		GlobalPropertyId globalPropertyId = new SimpleGlobalPropertyId("id 1");
		builder.defineGlobalProperty(globalPropertyId, propertyDefinition2, 2.5);
		// replacing data to show that the value persists
		builder.defineGlobalProperty(globalPropertyId, propertyDefinition, 0);
		// adding duplicate data to show that the value persists
		builder.defineGlobalProperty(globalPropertyId, propertyDefinition, 3.4);
		expectedPropertyDefinitions.put(globalPropertyId, propertyDefinition);
		expectedPropertyDefinitionCreationTimes.put(globalPropertyId, 3.4);

		propertyDefinition = PropertyDefinition.builder().setType(Double.class).setDefaultValue(234.34).build();
		propertyDefinition2 = PropertyDefinition.builder().setType(Double.class).setDefaultValue(795.88).build();
		globalPropertyId = new SimpleGlobalPropertyId("id 2");
		builder.defineGlobalProperty(globalPropertyId, propertyDefinition2, 2.0);
		// replacing data to show that the value persists
		builder.defineGlobalProperty(globalPropertyId, propertyDefinition, 2.9);
		// adding duplicate data to show that the value persists
		builder.defineGlobalProperty(globalPropertyId, propertyDefinition, 3.1);
		expectedPropertyDefinitions.put(globalPropertyId, propertyDefinition);
		expectedPropertyDefinitionCreationTimes.put(globalPropertyId, 3.1);

		propertyDefinition = PropertyDefinition.builder().setType(String.class).setDefaultValue("default value")
				.build();
		propertyDefinition2 = PropertyDefinition.builder().setType(String.class).setDefaultValue("second default")
				.build();
		globalPropertyId = new SimpleGlobalPropertyId("id 3");
		builder.defineGlobalProperty(globalPropertyId, propertyDefinition2, 0.5);
		// replacing data to show that the value persists
		builder.defineGlobalProperty(globalPropertyId, propertyDefinition, 6.8);
		// adding duplicate data to show that the value persists
		builder.defineGlobalProperty(globalPropertyId, propertyDefinition, 2.1);
		expectedPropertyDefinitions.put(globalPropertyId, propertyDefinition);
		expectedPropertyDefinitionCreationTimes.put(globalPropertyId, 2.1);

		// build the initial data
		GlobalPropertiesPluginData globalInitialData = builder.build();

		// show that the expected property ids are there
		Set<GlobalPropertyId> actualGlobalPropertyIds = globalInitialData.getGlobalPropertyIds();
		Set<GlobalPropertyId> expectedGlobalPropertyIds = expectedPropertyDefinitions.keySet();
		assertEquals(expectedGlobalPropertyIds, actualGlobalPropertyIds);

		// show that the property definitions are retrieved by their ids
		for (GlobalPropertyId gpid : expectedPropertyDefinitions.keySet()) {
			PropertyDefinition expectedPropertyDefinition = expectedPropertyDefinitions.get(gpid);
			PropertyDefinition actualPropertyDefinition = globalInitialData.getGlobalPropertyDefinition(gpid);
			assertEquals(expectedPropertyDefinition, actualPropertyDefinition);
		}

		// show that the property creation times are correct
		for (GlobalPropertyId gpid : expectedPropertyDefinitionCreationTimes.keySet()) {
			Double expectedTime = expectedPropertyDefinitionCreationTimes.get(gpid);
			Double actualTime = globalInitialData.getGlobalPropertyDefinitionTime(gpid);
			assertEquals(expectedTime, actualTime);
		}

		// precondition tests

		// if the global property id is null
		PropertyDefinition propDef = PropertyDefinition.builder().setType(Integer.class).setDefaultValue(17).build();
		ContractException contractException = assertThrows(ContractException.class,
				() -> builder.defineGlobalProperty(null, propDef, 0));
		assertEquals(PropertyError.NULL_PROPERTY_ID, contractException.getErrorType());

		// if the property definition is null
		contractException = assertThrows(ContractException.class,
				() -> builder.defineGlobalProperty(new SimpleGlobalPropertyId("id"), null, 0));
		assertEquals(PropertyError.NULL_PROPERTY_DEFINITION, contractException.getErrorType());

	}

	@Test
	@UnitTestMethod(target = GlobalPropertiesPluginData.Builder.class, name = "setGlobalPropertyValue", args = {
			GlobalPropertyId.class, Object.class, double.class })
	public void testSetGlobalPropertyValue() {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(170390875787254562L);

		GlobalPropertiesPluginData.Builder builder = GlobalPropertiesPluginData.builder();

		// show there are some properties in the support enum
		assertTrue(TestGlobalPropertyId.values().length > 0);

		// define some properties
		Set<GlobalPropertyId> expectedGlobalPropertyIds = new LinkedHashSet<>();
		for (TestGlobalPropertyId testGlobalPropertyId : TestGlobalPropertyId.values()) {
			PropertyDefinition propertyDefinition = PropertyDefinition.builder().setType(Integer.class)
					.setDefaultValue(0).build();
			builder.defineGlobalProperty(testGlobalPropertyId, propertyDefinition, 0);
			expectedGlobalPropertyIds.add(testGlobalPropertyId);
		}
		// create a container for the expected values of the properties
		Map<GlobalPropertyId, Integer> expectedValues = new LinkedHashMap<>();
		Map<GlobalPropertyId, Double> expectedTimes = new LinkedHashMap<>();

		// set the values
		boolean useProperty = true;
		for (TestGlobalPropertyId testGlobalPropertyId : TestGlobalPropertyId.values()) {
			if (useProperty) {
				int value = randomGenerator.nextInt();
				int value2 = randomGenerator.nextInt();
				double time = randomGenerator.nextDouble();
				builder.setGlobalPropertyValue(testGlobalPropertyId, value2, time);
				// replacing data to show that the value persists
				time = randomGenerator.nextDouble();
				builder.setGlobalPropertyValue(testGlobalPropertyId, value, time);
				// duplicating data to show that the value persists
				time = randomGenerator.nextDouble();
				builder.setGlobalPropertyValue(testGlobalPropertyId, value, time);
				expectedValues.put(testGlobalPropertyId, value);
				expectedTimes.put(testGlobalPropertyId, time);
			}
			useProperty = !useProperty;
		}

		// build the initial data
		GlobalPropertiesPluginData globalInitialData = builder.build();

		// show that the expected property ids are there
		Set<GlobalPropertyId> actualGlobalPropertyIds = globalInitialData.getGlobalPropertyIds();

		assertEquals(expectedGlobalPropertyIds, actualGlobalPropertyIds);

		// show that the expected values are present
		for (TestGlobalPropertyId testGlobalPropertyId : TestGlobalPropertyId.values()) {
			Object expectedValue = expectedValues.get(testGlobalPropertyId);
			Optional<Object> optionalValue = globalInitialData.getGlobalPropertyValue(testGlobalPropertyId);

			if (expectedValue == null) {
				assertFalse(optionalValue.isPresent());
			} else {
				assertTrue(optionalValue.isPresent());
				Object actualValue = optionalValue.get();
				assertEquals(expectedValue, actualValue);
			}
		}

		// show that the expected times are present
		for (TestGlobalPropertyId testGlobalPropertyId : TestGlobalPropertyId.values()) {
			Double expectedTime = expectedTimes.get(testGlobalPropertyId);
			Optional<Double> optionalTime = globalInitialData.getGlobalPropertyTime(testGlobalPropertyId);

			if (expectedTime == null) {
				assertFalse(optionalTime.isPresent());
			} else {
				assertTrue(optionalTime.isPresent());
				Double actualTime = optionalTime.get();
				assertEquals(expectedTime, actualTime);
			}
		}

		/*
		 * precondition tests -- Note that invalid values are not covered here. The
		 * build() validates the values to see if they are compatible with the
		 * corresponding definitions.
		 */

		// if the global property id is null
		ContractException contractException = assertThrows(ContractException.class,
				() -> builder.setGlobalPropertyValue(null, 5, 0));
		assertEquals(PropertyError.NULL_PROPERTY_ID, contractException.getErrorType());

		// if the global property value is null
		contractException = assertThrows(ContractException.class,
				() -> builder.setGlobalPropertyValue(TestGlobalPropertyId.GLOBAL_PROPERTY_1_BOOLEAN_MUTABLE, null, 0));
		assertEquals(PropertyError.NULL_PROPERTY_VALUE, contractException.getErrorType());

	}

	@Test
	@UnitTestMethod(target = GlobalPropertiesPluginData.class, name = "builder", args = {})
	public void testBuilder() {
		// show that the builder can be created
		assertNotNull(GlobalPropertiesPluginData.builder());
	}

	@Test
	@UnitTestMethod(target = GlobalPropertiesPluginData.class, name = "getGlobalPropertyDefinition", args = {
			GlobalPropertyId.class })
	public void testGetGlobalPropertyDefinition() {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(5427251266191264753L);

		GlobalPropertiesPluginData.Builder builder = GlobalPropertiesPluginData.builder();

		// show there are some properties in the support enum
		assertTrue(TestGlobalPropertyId.values().length > 0);

		// create a container for the expected values of the properties
		Map<GlobalPropertyId, PropertyDefinition> expectedGlobalPropertyDefinitions = new LinkedHashMap<>();
		Map<GlobalPropertyId, Double> expectedGlobalPropertyDefinitionTimes = new LinkedHashMap<>();

		// define some properties

		for (TestGlobalPropertyId testGlobalPropertyId : TestGlobalPropertyId.values()) {
			PropertyDefinition propertyDefinition = PropertyDefinition.builder().setType(Integer.class)
					.setDefaultValue(0).build();
			double time = randomGenerator.nextDouble();
			builder.defineGlobalProperty(testGlobalPropertyId, propertyDefinition, time);
			expectedGlobalPropertyDefinitions.put(testGlobalPropertyId, propertyDefinition);
			expectedGlobalPropertyDefinitionTimes.put(testGlobalPropertyId, time);
		}

		// show that the expected property definitions are present
		GlobalPropertiesPluginData globalInitialData = builder.build();

		for (TestGlobalPropertyId testGlobalPropertyId : TestGlobalPropertyId.values()) {
			PropertyDefinition expectedPropertyDefinition = expectedGlobalPropertyDefinitions.get(testGlobalPropertyId);
			PropertyDefinition actualPropertyDefinition = globalInitialData
					.getGlobalPropertyDefinition(testGlobalPropertyId);
			assertEquals(expectedPropertyDefinition, actualPropertyDefinition);

			Double expetedTime = expectedGlobalPropertyDefinitionTimes.get(testGlobalPropertyId);
			Double actualTime = globalInitialData.getGlobalPropertyDefinitionTime(testGlobalPropertyId);
			assertEquals(expetedTime, actualTime);

		}

		// precondition tests
		ContractException contractException = assertThrows(ContractException.class,
				() -> globalInitialData.getGlobalPropertyDefinition(null));
		assertEquals(PropertyError.NULL_PROPERTY_ID, contractException.getErrorType());

		contractException = assertThrows(ContractException.class,
				() -> globalInitialData.getGlobalPropertyDefinition(TestGlobalPropertyId.getUnknownGlobalPropertyId()));
		assertEquals(PropertyError.UNKNOWN_PROPERTY_ID, contractException.getErrorType());

	}

	@Test
	@UnitTestMethod(target = GlobalPropertiesPluginData.class, name = "getGlobalPropertyDefinitions", args = {})
	public void testGetGlobalPropertyDefinitions() {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(5428251266091264753L);

		GlobalPropertiesPluginData.Builder builder = GlobalPropertiesPluginData.builder();

		// show there are some properties in the support enum
		assertTrue(TestGlobalPropertyId.values().length > 0);

		// create a container for the expected values of the properties
		Map<GlobalPropertyId, PropertyDefinition> expectedGlobalPropertyDefinitions = new LinkedHashMap<>();

		// define some properties

		for (TestGlobalPropertyId testGlobalPropertyId : TestGlobalPropertyId.values()) {
			PropertyDefinition propertyDefinition = PropertyDefinition.builder().setType(Integer.class)
					.setDefaultValue(0).build();
			double time = randomGenerator.nextDouble();
			builder.defineGlobalProperty(testGlobalPropertyId, propertyDefinition, time);
			expectedGlobalPropertyDefinitions.put(testGlobalPropertyId, propertyDefinition);
		}

		// show that the expected property definitions are present
		GlobalPropertiesPluginData globalInitialData = builder.build();

		assertEquals(expectedGlobalPropertyDefinitions, globalInitialData.getGlobalPropertyDefinitions());
	}

	@Test
	@UnitTestMethod(target = GlobalPropertiesPluginData.class, name = "getGlobalPropertyDefinitionTimes", args = {})
	public void testGetGlobalPropertyDefinitionTimes() {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(5427251266091264753L);

		GlobalPropertiesPluginData.Builder builder = GlobalPropertiesPluginData.builder();

		// show there are some properties in the support enum
		assertTrue(TestGlobalPropertyId.values().length > 0);

		// create a container for the expected values of the properties
		Map<GlobalPropertyId, Double> expectedGlobalPropertyDefinitionTimes = new LinkedHashMap<>();

		// define some properties

		for (TestGlobalPropertyId testGlobalPropertyId : TestGlobalPropertyId.values()) {
			PropertyDefinition propertyDefinition = PropertyDefinition.builder().setType(Integer.class)
					.setDefaultValue(0).build();
			double time = randomGenerator.nextDouble();
			builder.defineGlobalProperty(testGlobalPropertyId, propertyDefinition, time);
			expectedGlobalPropertyDefinitionTimes.put(testGlobalPropertyId, time);
		}

		// show that the expected property definitions are present
		GlobalPropertiesPluginData globalInitialData = builder.build();

		assertEquals(expectedGlobalPropertyDefinitionTimes, globalInitialData.getGlobalPropertyDefinitionTimes());
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
			PropertyDefinition propertyDefinition = PropertyDefinition.builder()//
					.setType(Integer.class)//
					.setDefaultValue(0)//
					.build();
			builder.defineGlobalProperty(testGlobalPropertyId, propertyDefinition, 0);
			expectedGlobalPropertyIds.add(testGlobalPropertyId);
		}

		// show that the expected values are present
		GlobalPropertiesPluginData globalInitialData = builder.build();
		assertEquals(expectedGlobalPropertyIds, globalInitialData.getGlobalPropertyIds());
	}

	@Test
	@UnitTestMethod(target = GlobalPropertiesPluginData.class, name = "getGlobalPropertyValue", args = {
			GlobalPropertyId.class })
	public void testGetGlobalPropertyValue() {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(4250048639082794761L);

		GlobalPropertiesPluginData.Builder builder = GlobalPropertiesPluginData.builder();

		// show there are some properties in the support enum
		assertTrue(TestGlobalPropertyId.values().length > 0);

		// define some properties
		for (TestGlobalPropertyId testGlobalPropertyId : TestGlobalPropertyId.values()) {
			PropertyDefinition propertyDefinition = PropertyDefinition.builder().setType(Integer.class)
					.setDefaultValue(0).build();
			builder.defineGlobalProperty(testGlobalPropertyId, propertyDefinition, 0);
		}
		// create a container for the expected values of the properties
		Map<GlobalPropertyId, Integer> expectedValues = new LinkedHashMap<>();

		// set about half of the values
		boolean shouldSetValue = true;
		for (TestGlobalPropertyId testGlobalPropertyId : TestGlobalPropertyId.values()) {
			if (shouldSetValue) {
				int value = randomGenerator.nextInt();
				builder.setGlobalPropertyValue(testGlobalPropertyId, value, 0);
				expectedValues.put(testGlobalPropertyId, value);
			}
			shouldSetValue = !shouldSetValue;
		}

		// show that the expected values are present
		GlobalPropertiesPluginData globalPropertiesPluginData = builder.build();
		for (TestGlobalPropertyId testGlobalPropertyId : TestGlobalPropertyId.values()) {
			Object expectedValue = expectedValues.get(testGlobalPropertyId);
			Optional<Object> optionalValue = globalPropertiesPluginData.getGlobalPropertyValue(testGlobalPropertyId);
			if (expectedValue == null) {
				assertFalse(optionalValue.isPresent());
			} else {
				assertTrue(optionalValue.isPresent());
				Object actualValue = optionalValue.get();
				assertEquals(expectedValue, actualValue);
			}
		}

		/*
		 * precondition tests -- Note that invalid values are not covered here. The
		 * build() validates the values to see if they are compatible with the
		 * corresponding definitions.
		 */

		// if the global property id is null
		ContractException contractException = assertThrows(ContractException.class,
				() -> globalPropertiesPluginData.getGlobalPropertyValue(null));
		assertEquals(PropertyError.NULL_PROPERTY_ID, contractException.getErrorType());

		// if the global property value is null
		contractException = assertThrows(ContractException.class, () -> globalPropertiesPluginData
				.getGlobalPropertyValue(TestGlobalPropertyId.getUnknownGlobalPropertyId()));
		assertEquals(PropertyError.UNKNOWN_PROPERTY_ID, contractException.getErrorType());

	}

	@Test
	@UnitTestMethod(target = GlobalPropertiesPluginData.class, name = "getGlobalPropertyValues", args = {})
	public void testGetGlobalPropertyValues() {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(4260048639082754761L);

		GlobalPropertiesPluginData.Builder builder = GlobalPropertiesPluginData.builder();

		// show there are some properties in the support enum
		assertTrue(TestGlobalPropertyId.values().length > 0);

		// define some properties
		for (TestGlobalPropertyId testGlobalPropertyId : TestGlobalPropertyId.values()) {
			PropertyDefinition propertyDefinition = PropertyDefinition.builder().setType(Integer.class)
					.setDefaultValue(0).build();
			builder.defineGlobalProperty(testGlobalPropertyId, propertyDefinition, 0);
		}
		// create a container for the expected values of the properties
		Map<GlobalPropertyId, Integer> expectedValues = new LinkedHashMap<>();

		// set about half of the values
		boolean shouldSetValue = true;
		for (TestGlobalPropertyId testGlobalPropertyId : TestGlobalPropertyId.values()) {
			if (shouldSetValue) {
				int value = randomGenerator.nextInt();
				builder.setGlobalPropertyValue(testGlobalPropertyId, value, 0);
				expectedValues.put(testGlobalPropertyId, value);
			}
			shouldSetValue = !shouldSetValue;
		}

		GlobalPropertiesPluginData globalPropertiesPluginData = builder.build();

		assertEquals(expectedValues, globalPropertiesPluginData.getGlobalPropertyValues());
	}

	@Test
	@UnitTestMethod(target = GlobalPropertiesPluginData.class, name = "toBuilder", args = {})
	public void testToBuilder() {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(9113503089361379130L);
		GlobalPropertiesPluginData.Builder builder = GlobalPropertiesPluginData.builder();

		for (TestGlobalPropertyId testGlobalPropertyId : TestGlobalPropertyId.values()) {
			double time = randomGenerator.nextDouble();
			builder.defineGlobalProperty(testGlobalPropertyId, testGlobalPropertyId.getPropertyDefinition(), time);
			time += randomGenerator.nextDouble();
			builder.setGlobalPropertyValue(testGlobalPropertyId,
					testGlobalPropertyId.getRandomPropertyValue(randomGenerator), time);
		}

		GlobalPropertiesPluginData globalPropertiesPluginData = builder.build();

		// show that the returned clone builder will build an identical instance if no
		// mutations are made
		GlobalPropertiesPluginData.Builder cloneBuilder = globalPropertiesPluginData.toBuilder();
		assertNotNull(cloneBuilder);
		assertEquals(globalPropertiesPluginData, cloneBuilder.build());

		// show that the clone builder builds a distinct instance if any mutation is
		// made

		// defineGlobalProperty
		cloneBuilder = globalPropertiesPluginData.toBuilder();
		cloneBuilder.defineGlobalProperty(TestGlobalPropertyId.GLOBAL_PROPERTY_3_DOUBLE_MUTABLE,
				TestGlobalPropertyId.GLOBAL_PROPERTY_3_DOUBLE_MUTABLE.getPropertyDefinition(), 0.0);
		assertNotEquals(globalPropertiesPluginData, cloneBuilder.build());

		// setGlobalPropertyValue
		cloneBuilder = globalPropertiesPluginData.toBuilder();
		cloneBuilder.setGlobalPropertyValue(TestGlobalPropertyId.GLOBAL_PROPERTY_3_DOUBLE_MUTABLE, 2.3, 1.0);
		assertNotEquals(globalPropertiesPluginData, cloneBuilder.build());
	}

	@Test
	@UnitTestMethod(target = GlobalPropertiesPluginData.class, name = "getGlobalPropertyDefinitionTime", args = {
			GlobalPropertyId.class })
	public void testGetGlobalPropertyDefinitionTime() {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(5487507072126661304L);

		GlobalPropertiesPluginData.Builder builder = GlobalPropertiesPluginData.builder();

		// show there are some properties in the support enum
		assertTrue(TestGlobalPropertyId.values().length > 0);

		// create a container for the expected values of the properties
		Map<GlobalPropertyId, PropertyDefinition> expectedGlobalPropertyDefinitions = new LinkedHashMap<>();
		Map<GlobalPropertyId, Double> expectedGlobalPropertyDefinitionTimes = new LinkedHashMap<>();

		// define some properties

		for (TestGlobalPropertyId testGlobalPropertyId : TestGlobalPropertyId.values()) {
			PropertyDefinition propertyDefinition = PropertyDefinition.builder().setType(Integer.class)
					.setDefaultValue(0).build();
			double time = randomGenerator.nextDouble();
			builder.defineGlobalProperty(testGlobalPropertyId, propertyDefinition, time);
			expectedGlobalPropertyDefinitions.put(testGlobalPropertyId, propertyDefinition);
			expectedGlobalPropertyDefinitionTimes.put(testGlobalPropertyId, time);
		}

		// show that the expected property definitions are present
		GlobalPropertiesPluginData globalInitialData = builder.build();

		for (TestGlobalPropertyId testGlobalPropertyId : TestGlobalPropertyId.values()) {
			PropertyDefinition expectedPropertyDefinition = expectedGlobalPropertyDefinitions.get(testGlobalPropertyId);
			PropertyDefinition actualPropertyDefinition = globalInitialData
					.getGlobalPropertyDefinition(testGlobalPropertyId);
			assertEquals(expectedPropertyDefinition, actualPropertyDefinition);

			Double expetedTime = expectedGlobalPropertyDefinitionTimes.get(testGlobalPropertyId);
			Double actualTime = globalInitialData.getGlobalPropertyDefinitionTime(testGlobalPropertyId);
			assertEquals(expetedTime, actualTime);

		}

		// precondition tests
		ContractException contractException = assertThrows(ContractException.class,
				() -> globalInitialData.getGlobalPropertyDefinition(null));
		assertEquals(PropertyError.NULL_PROPERTY_ID, contractException.getErrorType());

		contractException = assertThrows(ContractException.class,
				() -> globalInitialData.getGlobalPropertyDefinition(TestGlobalPropertyId.getUnknownGlobalPropertyId()));
		assertEquals(PropertyError.UNKNOWN_PROPERTY_ID, contractException.getErrorType());

	}

	@Test
	@UnitTestMethod(target = GlobalPropertiesPluginData.class, name = "getGlobalPropertyTime", args = {
			GlobalPropertyId.class })
	public void testGetGlobalPropertyTime() {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(4250058639082754761L);

		GlobalPropertiesPluginData.Builder builder = GlobalPropertiesPluginData.builder();

		// show there are some properties in the support enum
		assertTrue(TestGlobalPropertyId.values().length > 0);

		// define some properties -- note that we do not set default values to
		// test that the values provided explicitly will properly replace the
		// default values.
		for (TestGlobalPropertyId testGlobalPropertyId : TestGlobalPropertyId.values()) {
			PropertyDefinition propertyDefinition = PropertyDefinition.builder()//
					.setType(Integer.class)//
					.setDefaultValue(0)//
					.build();
			builder.defineGlobalProperty(testGlobalPropertyId, propertyDefinition, 0.0);
		}
		// create a container for the expected values of the properties
		Map<GlobalPropertyId, Double> expectedTimes = new LinkedHashMap<>();

		// set the times for half of the properties
		int counter = 0;
		for (TestGlobalPropertyId testGlobalPropertyId : TestGlobalPropertyId.values()) {
			if (counter % 2 == 0) {
				int value = randomGenerator.nextInt();
				double time = randomGenerator.nextDouble() + 0.1;
				builder.setGlobalPropertyValue(testGlobalPropertyId, value, time);
				expectedTimes.put(testGlobalPropertyId, time);
			}
			counter++;
		}

		// show that the expected times are present
		GlobalPropertiesPluginData globalPropertiesPluginData = builder.build();
		for (TestGlobalPropertyId testGlobalPropertyId : TestGlobalPropertyId.values()) {
			Double expectedTime = expectedTimes.get(testGlobalPropertyId);
			Optional<Double> optionalTime = globalPropertiesPluginData.getGlobalPropertyTime(testGlobalPropertyId);
			if (expectedTime == null) {
				assertFalse(optionalTime.isPresent());
			} else {
				assertTrue(optionalTime.isPresent());
				Double actualTime = optionalTime.get();
				assertEquals(expectedTime, actualTime);
			}
		}

		/*
		 * precondition tests -- Note that invalid values are not covered here. The
		 * build() validates the values to see if they are compatible with the
		 * corresponding definitions.
		 */

		// if the global property id is null
		ContractException contractException = assertThrows(ContractException.class,
				() -> globalPropertiesPluginData.getGlobalPropertyValue(null));
		assertEquals(PropertyError.NULL_PROPERTY_ID, contractException.getErrorType());

		// if the global property value is null
		contractException = assertThrows(ContractException.class, () -> globalPropertiesPluginData
				.getGlobalPropertyValue(TestGlobalPropertyId.getUnknownGlobalPropertyId()));
		assertEquals(PropertyError.UNKNOWN_PROPERTY_ID, contractException.getErrorType());

	}

	@Test
	@UnitTestMethod(target = GlobalPropertiesPluginData.class, name = "getGlobalPropertyTimes", args = {})
	public void testGetGlobalPropertyTimes() {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(4250048639082754761L);

		GlobalPropertiesPluginData.Builder builder = GlobalPropertiesPluginData.builder();

		// show there are some properties in the support enum
		assertTrue(TestGlobalPropertyId.values().length > 0);

		// define some properties -- note that we do not set default values to
		// test that the values provided explicitly will properly replace the
		// default values.
		for (TestGlobalPropertyId testGlobalPropertyId : TestGlobalPropertyId.values()) {
			PropertyDefinition propertyDefinition = PropertyDefinition.builder()//
					.setType(Integer.class)//
					.setDefaultValue(0)//
					.build();
			builder.defineGlobalProperty(testGlobalPropertyId, propertyDefinition, 0.0);
		}
		// create a container for the expected values of the properties
		Map<GlobalPropertyId, Double> expectedTimes = new LinkedHashMap<>();

		// set the times for half of the properties
		int counter = 0;
		for (TestGlobalPropertyId testGlobalPropertyId : TestGlobalPropertyId.values()) {
			if (counter % 2 == 0) {
				int value = randomGenerator.nextInt();
				double time = randomGenerator.nextDouble() + 0.1;
				builder.setGlobalPropertyValue(testGlobalPropertyId, value, time);
				expectedTimes.put(testGlobalPropertyId, time);
			}
			counter++;
		}

		// show that the expected times are present
		GlobalPropertiesPluginData globalPropertiesPluginData = builder.build();

		assertEquals(expectedTimes, globalPropertiesPluginData.getGlobalPropertyTimes());
	}

	@Test
	@UnitTestMethod(target = GlobalPropertiesPluginData.class, name = "getVersion", args = {})
	public void testGetVersion() {
		assertEquals(StandardVersioning.VERSION,
				GlobalPropertiesTestPluginFactory.getStandardGlobalPropertiesPluginData(0).getVersion());
	}

	@Test
	@UnitTestMethod(target = GlobalPropertiesPluginData.class, name = "checkVersionSupported", args = { String.class })
	public void testCheckVersionSupported() {
		List<String> versions = Arrays.asList(StandardVersioning.VERSION);

		for (String version : versions) {
			assertTrue(GlobalPropertiesPluginData.checkVersionSupported(version));
			assertFalse(GlobalPropertiesPluginData.checkVersionSupported(version + "badVersion"));
			assertFalse(GlobalPropertiesPluginData.checkVersionSupported("badVersion"));
			assertFalse(GlobalPropertiesPluginData.checkVersionSupported(version + "0"));
			assertFalse(GlobalPropertiesPluginData.checkVersionSupported(version + ".0.0"));
		}
	}

	@Test
	@UnitTestMethod(target = GlobalPropertiesPluginData.class, name = "equals", args = { Object.class })
	public void testEquals() {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(8980821418377306870L);

		// never equal to another type
		for (int i = 0; i < 30; i++) {
			GlobalPropertiesPluginData pluginData = getRandomGlobalPropertiesPluginData(randomGenerator.nextLong());
			assertFalse(pluginData.equals(new Object()));
		}

		// never equal to null
		for (int i = 0; i < 30; i++) {
			GlobalPropertiesPluginData pluginData = getRandomGlobalPropertiesPluginData(randomGenerator.nextLong());
			assertFalse(pluginData.equals(null));
		}

		// reflexive
		for (int i = 0; i < 30; i++) {
			GlobalPropertiesPluginData pluginData = getRandomGlobalPropertiesPluginData(randomGenerator.nextLong());
			assertTrue(pluginData.equals(pluginData));
		}

		// symmetric, transitive, consistent
		for (int i = 0; i < 30; i++) {
			long seed = randomGenerator.nextLong();
			GlobalPropertiesPluginData pluginData1 = getRandomGlobalPropertiesPluginData(seed);
			GlobalPropertiesPluginData pluginData2 = getRandomGlobalPropertiesPluginData(seed);
			assertFalse(pluginData1 == pluginData2);
			for (int j = 0; j < 10; j++) {
				assertTrue(pluginData1.equals(pluginData2));
				assertTrue(pluginData2.equals(pluginData1));
			}
		}

		// different inputs yield unequal plugin datas
		Set<GlobalPropertiesPluginData> set = new LinkedHashSet<>();
		for (int i = 0; i < 100; i++) {
			GlobalPropertiesPluginData pluginData = getRandomGlobalPropertiesPluginData(randomGenerator.nextLong());
			set.add(pluginData);
		}
		assertEquals(100, set.size());
	}

	@Test
	@UnitTestMethod(target = GlobalPropertiesPluginData.class, name = "hashCode", args = {})
	public void testHashCode() {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(2653491508465183354L);

		// equal objects have equal hash codes
		for (int i = 0; i < 30; i++) {
			long seed = randomGenerator.nextLong();
			GlobalPropertiesPluginData pluginData1 = getRandomGlobalPropertiesPluginData(seed);
			GlobalPropertiesPluginData pluginData2 = getRandomGlobalPropertiesPluginData(seed);

			assertEquals(pluginData1, pluginData2);
			assertEquals(pluginData1.hashCode(), pluginData2.hashCode());
		}

		// hash codes are reasonably distributed
		Set<Integer> hashCodes = new LinkedHashSet<>();
		for (int i = 0; i < 100; i++) {
			GlobalPropertiesPluginData pluginData = getRandomGlobalPropertiesPluginData(randomGenerator.nextLong());
			hashCodes.add(pluginData.hashCode());
		}

		assertEquals(100, hashCodes.size());
	}

	@Test
	@UnitTestMethod(target = GlobalPropertiesPluginData.class, name = "toString", args = {})
	public void testToString() {

		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(8337912786649642023L);

		/*
		 * Demonstrate a typical example with a full string. We will add all of the
		 * standard test definitions in the usual order, but will only add a few of the
		 * property values in reverse order. Note that we will cover the #3 member which
		 * does not have a corresponding default value.
		 */
		GlobalPropertiesPluginData.Builder builder = GlobalPropertiesPluginData.builder();

		Map<TestGlobalPropertyId, Double> definitionTimes = new LinkedHashMap<>();

		for (TestGlobalPropertyId testGlobalPropertyId : TestGlobalPropertyId.values()) {
			double time = randomGenerator.nextInt(1000);
			definitionTimes.put(testGlobalPropertyId, time);
			builder.defineGlobalProperty(testGlobalPropertyId, testGlobalPropertyId.getPropertyDefinition(), time);
		}

		List<TestGlobalPropertyId> propertiesForValueSetting = new ArrayList<>();
		propertiesForValueSetting.add(TestGlobalPropertyId.GLOBAL_PROPERTY_6_DOUBLE_IMMUTABLE);
		propertiesForValueSetting.add(TestGlobalPropertyId.GLOBAL_PROPERTY_3_DOUBLE_MUTABLE);
		propertiesForValueSetting.add(TestGlobalPropertyId.GLOBAL_PROPERTY_1_BOOLEAN_MUTABLE);

		for (TestGlobalPropertyId testGlobalPropertyId : propertiesForValueSetting) {
			double time = definitionTimes.get(testGlobalPropertyId) + randomGenerator.nextInt(100);
			builder.setGlobalPropertyValue(testGlobalPropertyId,
					testGlobalPropertyId.getRandomPropertyValue(randomGenerator), time);
		}

		GlobalPropertiesPluginData globalPropertiesPluginData = builder.build();

		String expectedValue = "GlobalPropertiesPluginData [data=Data [globalPropertyDefinitions"
				+ "={GLOBAL_PROPERTY_1_BOOLEAN_MUTABLE=PropertyDefinition [type=class java.lang.Boolean"
				+ ", propertyValuesAreMutable=true, defaultValue=false], GLOBAL_PROPERTY_2_INTEGER_MUTABLE"
				+ "=PropertyDefinition [type=class java.lang.Integer, propertyValuesAreMutable=true, "
				+ "defaultValue=0], GLOBAL_PROPERTY_3_DOUBLE_MUTABLE=PropertyDefinition [type=class "
				+ "java.lang.Double, propertyValuesAreMutable=true, defaultValue=null], "
				+ "GLOBAL_PROPERTY_4_BOOLEAN_IMMUTABLE=PropertyDefinition [type=class java.lang.Boolean, "
				+ "propertyValuesAreMutable=false, defaultValue=false], GLOBAL_PROPERTY_5_INTEGER_IMMUTABLE="
				+ "PropertyDefinition [type=class java.lang.Integer, propertyValuesAreMutable=false"
				+ ", defaultValue=0], GLOBAL_PROPERTY_6_DOUBLE_IMMUTABLE=PropertyDefinition [type=class "
				+ "java.lang.Double, propertyValuesAreMutable=false, defaultValue=0.0]}, "
				+ "globalPropertyDefinitionTimes={GLOBAL_PROPERTY_1_BOOLEAN_MUTABLE=852.0, "
				+ "GLOBAL_PROPERTY_2_INTEGER_MUTABLE=835.0, GLOBAL_PROPERTY_3_DOUBLE_MUTABLE=156.0, "
				+ "GLOBAL_PROPERTY_4_BOOLEAN_IMMUTABLE=505.0, GLOBAL_PROPERTY_5_INTEGER_IMMUTABLE=956.0, "
				+ "GLOBAL_PROPERTY_6_DOUBLE_IMMUTABLE=191.0}, globalPropertyValues={"
				+ "GLOBAL_PROPERTY_6_DOUBLE_IMMUTABLE=0.09917206486092223, GLOBAL_PROPERTY_3_DOUBLE_MUTABLE="
				+ "0.07709107250291058, GLOBAL_PROPERTY_1_BOOLEAN_MUTABLE=false}, globalPropertyTimes={"
				+ "GLOBAL_PROPERTY_6_DOUBLE_IMMUTABLE=255.0, GLOBAL_PROPERTY_3_DOUBLE_MUTABLE=168.0, "
				+ "GLOBAL_PROPERTY_1_BOOLEAN_MUTABLE=877.0}, locked=true]]";

		String actualValue = globalPropertiesPluginData.toString();

		assertEquals(expectedValue, actualValue);
	}

	private GlobalPropertiesPluginData getRandomGlobalPropertiesPluginData(Long seed) {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(seed);
		GlobalPropertiesPluginData.Builder builder = GlobalPropertiesPluginData.builder();

		List<TestGlobalPropertyId> testGlobalPropertyIds = Arrays.asList(TestGlobalPropertyId.values());
		Random rnd = new Random(seed);
		Collections.shuffle(testGlobalPropertyIds, rnd);

		int n = randomGenerator.nextInt(6) + 1;
		for (int i = 0; i < n; i++) {
			TestGlobalPropertyId testGlobalPropertyId = testGlobalPropertyIds.get(i);
			Double assignmentTime = randomGenerator.nextDouble();
			builder.defineGlobalProperty(testGlobalPropertyId, testGlobalPropertyId.getPropertyDefinition(),
					assignmentTime);

			if (randomGenerator.nextBoolean()
					|| testGlobalPropertyId.getPropertyDefinition().getDefaultValue().isEmpty()) {
				Object randomPropertyValue = testGlobalPropertyId.getRandomPropertyValue(randomGenerator);
				assignmentTime += randomGenerator.nextDouble() + 1.0;
				builder.setGlobalPropertyValue(testGlobalPropertyId, randomPropertyValue, assignmentTime);
			}
		}

		return builder.build();
	}
}
