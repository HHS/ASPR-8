package plugins.globals.initialdata;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Supplier;

import org.apache.commons.math3.random.RandomGenerator;
import org.junit.jupiter.api.Test;

import nucleus.AgentContext;
import plugins.globals.support.GlobalComponentId;
import plugins.globals.support.GlobalError;
import plugins.globals.support.GlobalPropertyId;
import plugins.globals.support.SimpleGlobalPropertyId;
import plugins.globals.testsupport.TestGlobalComponentId;
import plugins.globals.testsupport.TestGlobalPropertyId;
import plugins.properties.support.PropertyDefinition;
import util.ContractException;
import util.SeedProvider;
import util.annotations.UnitTest;
import util.annotations.UnitTestMethod;

@UnitTest(target = GlobalInitialData.class)

public class AT_GlobalInitialData {

	@Test
	@UnitTestMethod(target = GlobalInitialData.Builder.class, name = "build", args = {})
	public void testBuild() {

		GlobalInitialData globalInitialData = GlobalInitialData.builder().build();
		assertNotNull(globalInitialData);

		// precondition tests
		GlobalInitialData.Builder builder = GlobalInitialData.builder();
		PropertyDefinition propertyDefinition = PropertyDefinition.builder().setType(Integer.class).setDefaultValue(34).build();
		GlobalPropertyId globalPropertyId1 = new SimpleGlobalPropertyId("id 1");
		GlobalPropertyId globalPropertyId2 = new SimpleGlobalPropertyId("id 2");

		/*
		 * If a global property value was associated with a global property id
		 * that was not defined
		 */
		builder.defineGlobalProperty(globalPropertyId1, propertyDefinition);
		builder.setGlobalPropertyValue(globalPropertyId2, 67);
		ContractException contractException = assertThrows(ContractException.class, () -> builder.build());
		assertEquals(GlobalError.UNKNOWN_GLOBAL_PROPERTY_ID, contractException.getErrorType());

		/*
		 * If a global property value was associated with a global property id
		 * that is incompatible with the corresponding property definition.
		 */
		builder.defineGlobalProperty(globalPropertyId1, propertyDefinition);
		builder.setGlobalPropertyValue(globalPropertyId1, "bad value");
		contractException = assertThrows(ContractException.class, () -> builder.build());
		assertEquals(GlobalError.INCOMPATIBLE_VALUE, contractException.getErrorType());

		/*
		 * If a global property definition does not have a default value and
		 * there are no property values added to replace that default.
		 */
		propertyDefinition = PropertyDefinition.builder().setType(Integer.class).build();
		builder.defineGlobalProperty(globalPropertyId1, propertyDefinition);
		contractException = assertThrows(ContractException.class, () -> builder.build());
		assertEquals(GlobalError.INSUFFICIENT_GLOBAL_PROPERTY_VALUE_ASSIGNMENT, contractException.getErrorType());

	}

	@Test
	@UnitTestMethod(target = GlobalInitialData.Builder.class, name = "defineGlobalProperty", args = { GlobalPropertyId.class, PropertyDefinition.class })
	public void testDefineGlobalProperty() {
		GlobalInitialData.Builder builder = GlobalInitialData.builder();

		// create a container to hold the expected property definitions
		Map<GlobalPropertyId, PropertyDefinition> expectedPropertyDefinitions = new LinkedHashMap<>();

		// define a few global properties
		PropertyDefinition propertyDefinition = PropertyDefinition.builder().setType(Integer.class).setDefaultValue(34).build();
		GlobalPropertyId globalPropertyId = new SimpleGlobalPropertyId("id 1");
		expectedPropertyDefinitions.put(globalPropertyId, propertyDefinition);
		builder.defineGlobalProperty(globalPropertyId, propertyDefinition);

		propertyDefinition = PropertyDefinition.builder().setType(Double.class).setDefaultValue(234.34).build();
		globalPropertyId = new SimpleGlobalPropertyId("id 2");
		expectedPropertyDefinitions.put(globalPropertyId, propertyDefinition);
		builder.defineGlobalProperty(globalPropertyId, propertyDefinition);

		propertyDefinition = PropertyDefinition.builder().setType(String.class).setDefaultValue("default value").build();
		globalPropertyId = new SimpleGlobalPropertyId("id 3");
		expectedPropertyDefinitions.put(globalPropertyId, propertyDefinition);
		builder.defineGlobalProperty(globalPropertyId, propertyDefinition);

		// build the initial data
		GlobalInitialData globalInitialData = builder.build();

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
		assertEquals(GlobalError.NULL_GLOBAL_PROPERTY_ID, contractException.getErrorType());

		// if the property definition is null
		contractException = assertThrows(ContractException.class, () -> builder.defineGlobalProperty(new SimpleGlobalPropertyId("id"), null));
		assertEquals(GlobalError.NULL_GLOBAL_PROPERTY_DEFINITION, contractException.getErrorType());

		// if a property definition for the given global property id was
		// previously defined.
		builder.defineGlobalProperty(new SimpleGlobalPropertyId("id"), propDef);
		contractException = assertThrows(ContractException.class, () -> builder.defineGlobalProperty(new SimpleGlobalPropertyId("id"), propDef));
		assertEquals(GlobalError.DUPLICATE_GLOBAL_PROPERTY_DEFINITION, contractException.getErrorType());

	}

	@Test
	@UnitTestMethod(target = GlobalInitialData.Builder.class, name = "setGlobalComponentInitialBehaviorSupplier", args = { GlobalComponentId.class, Supplier.class })
	public void testSetGlobalComponentInitialBehaviorSupplier() {
		GlobalInitialData.Builder builder = GlobalInitialData.builder();
		/*
		 * Add consumers associated with global component ids
		 */
		Map<GlobalComponentId, Consumer<AgentContext>> expectedConsumers = new LinkedHashMap<>();
		for (TestGlobalComponentId testGlobalComponentId : TestGlobalComponentId.values()) {
			Consumer<AgentContext> consumer = (c) -> {
			};
			expectedConsumers.put(testGlobalComponentId, consumer);
			Supplier<Consumer<AgentContext>> supplier = () -> consumer;
			builder.setGlobalComponentInitialBehaviorSupplier(testGlobalComponentId, supplier);
		}
		GlobalInitialData globalInitialData = builder.build();

		/*
		 * Show that the consumers retrieved by global component id were the
		 * ones that had been added to the builder
		 */

		for (TestGlobalComponentId testGlobalComponentId : TestGlobalComponentId.values()) {
			Consumer<AgentContext> actualConsumer = globalInitialData.getGlobalComponentInitialBehavior(testGlobalComponentId);
			Consumer<AgentContext> expectedConsumer = expectedConsumers.get(testGlobalComponentId);
			assertEquals(expectedConsumer, actualConsumer);
		}

		// precondition tests

		// null global component id
		ContractException contractException = assertThrows(ContractException.class, () -> globalInitialData.getGlobalComponentInitialBehavior(null));
		assertEquals(GlobalError.NULL_GLOBAL_COMPONENT_ID, contractException.getErrorType());

		// unknown global component id
		contractException = assertThrows(ContractException.class, () -> globalInitialData.getGlobalComponentInitialBehavior(TestGlobalComponentId.getUnknownGlobalComponentId()));
		assertEquals(GlobalError.UNKNOWN_GLOBAL_COMPONENT_ID, contractException.getErrorType());

	}

	@Test
	@UnitTestMethod(target = GlobalInitialData.Builder.class, name = "setGlobalPropertyValue", args = { GlobalPropertyId.class, Object.class })
	public void testSetGlobalPropertyValue() {
		RandomGenerator randomGenerator = SeedProvider.getRandomGenerator(170390875787254562L);

		GlobalInitialData.Builder builder = GlobalInitialData.builder();

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
		GlobalInitialData globalInitialData = builder.build();
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
		assertEquals(GlobalError.NULL_GLOBAL_PROPERTY_ID, contractException.getErrorType());

		// if the global property value is null
		contractException = assertThrows(ContractException.class, () -> builder.setGlobalPropertyValue(TestGlobalPropertyId.Global_Property_1, null));
		assertEquals(GlobalError.NULL_GLOBAL_PROPERTY_VALUE, contractException.getErrorType());

		// if the global property value was previously defined for the given
		// global property id
		builder.setGlobalPropertyValue(TestGlobalPropertyId.Global_Property_1, 4);
		contractException = assertThrows(ContractException.class, () -> builder.setGlobalPropertyValue(TestGlobalPropertyId.Global_Property_1, 5));
		assertEquals(GlobalError.DUPLICATE_GLOBAL_PROPERTY_VALUE_ASSIGNMENT, contractException.getErrorType());

	}

	@Test
	@UnitTestMethod(name = "builder", args = {})
	public void testBuilder() {
		// show that the builder can be created
		assertNotNull(GlobalInitialData.builder());
	}

	@Test
	@UnitTestMethod(name = "getGlobalComponentInitialBehavior", args = { GlobalComponentId.class })
	public void testGetGlobalComponentInitialBehavior() {
		GlobalInitialData.Builder builder = GlobalInitialData.builder();

		// create a container for the expected consumers
		Map<GlobalComponentId, Consumer<AgentContext>> expectedConsumers = new LinkedHashMap<>();

		for (TestGlobalComponentId testGlobalComponentId : TestGlobalComponentId.values()) {
			Consumer<AgentContext> consumer = (c) -> {
			};
			Supplier<Consumer<AgentContext>> supplier = () -> consumer;
			builder.setGlobalComponentInitialBehaviorSupplier(testGlobalComponentId, supplier);
			expectedConsumers.put(testGlobalComponentId, consumer);
		}
		GlobalInitialData globalInitialData = builder.build();

		for (TestGlobalComponentId testGlobalComponentId : TestGlobalComponentId.values()) {
			Consumer<AgentContext> expectedConsumer = expectedConsumers.get(testGlobalComponentId);
			Consumer<AgentContext> actualConsumer = globalInitialData.getGlobalComponentInitialBehavior(testGlobalComponentId);
			assertEquals(expectedConsumer, actualConsumer);
		}

		// precondition tests

		// if the global component id is null
		ContractException contractException = assertThrows(ContractException.class, () -> globalInitialData.getGlobalComponentInitialBehavior(null));
		assertEquals(GlobalError.NULL_GLOBAL_COMPONENT_ID, contractException.getErrorType());

		contractException = assertThrows(ContractException.class, () -> globalInitialData.getGlobalComponentInitialBehavior(TestGlobalComponentId.getUnknownGlobalComponentId()));
		assertEquals(GlobalError.UNKNOWN_GLOBAL_COMPONENT_ID, contractException.getErrorType());

	}

	@Test
	@UnitTestMethod(name = "getGlobalComponentIds", args = {})
	public void testGetGlobalComponentIds() {
		GlobalInitialData.Builder builder = GlobalInitialData.builder();

		// create a container for the expected component ids
		Set<GlobalComponentId> expectedGlobalComponentIds = new LinkedHashSet<>();

		for (TestGlobalComponentId testGlobalComponentId : TestGlobalComponentId.values()) {
			Consumer<AgentContext> consumer = (c) -> {
			};
			Supplier<Consumer<AgentContext>> supplier = () -> consumer;
			builder.setGlobalComponentInitialBehaviorSupplier(testGlobalComponentId, supplier);
			expectedGlobalComponentIds.add(testGlobalComponentId);
		}
		GlobalInitialData globalInitialData = builder.build();

		assertEquals(expectedGlobalComponentIds, globalInitialData.getGlobalComponentIds());

	}

	@Test
	@UnitTestMethod(name = "getGlobalPropertyDefinition", args = { GlobalPropertyId.class })
	public void testGetGlobalPropertyDefinition() {
		GlobalInitialData.Builder builder = GlobalInitialData.builder();

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
		GlobalInitialData globalInitialData = builder.build();

		for (TestGlobalPropertyId testGlobalPropertyId : TestGlobalPropertyId.values()) {
			PropertyDefinition expectedPropertyDefinition = expectedGlobalPropertyDefinitions.get(testGlobalPropertyId);
			PropertyDefinition actualPropertyDefinition = globalInitialData.getGlobalPropertyDefinition(testGlobalPropertyId);
			assertEquals(expectedPropertyDefinition, actualPropertyDefinition);
		}

		// precondition tests
		ContractException contractException = assertThrows(ContractException.class, () -> globalInitialData.getGlobalPropertyDefinition(null));
		assertEquals(GlobalError.NULL_GLOBAL_PROPERTY_ID, contractException.getErrorType());

		contractException = assertThrows(ContractException.class, () -> globalInitialData.getGlobalPropertyDefinition(TestGlobalPropertyId.getUnknownGlobalPropertyId()));
		assertEquals(GlobalError.UNKNOWN_GLOBAL_PROPERTY_ID, contractException.getErrorType());

	}

	@Test
	@UnitTestMethod(name = "getGlobalPropertyIds", args = {})
	public void testGetGlobalPropertyIds() {

		GlobalInitialData.Builder builder = GlobalInitialData.builder();

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
		GlobalInitialData globalInitialData = builder.build();
		assertEquals(expectedGlobalPropertyIds, globalInitialData.getGlobalPropertyIds());
	}

	@Test
	@UnitTestMethod(name = "getGlobalPropertyValue", args = { GlobalPropertyId.class })
	public void testGetGlobalPropertyValue() {
		RandomGenerator randomGenerator = SeedProvider.getRandomGenerator(4250048639082754761L);

		GlobalInitialData.Builder builder = GlobalInitialData.builder();

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
		GlobalInitialData globalInitialData = builder.build();
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
		assertEquals(GlobalError.NULL_GLOBAL_PROPERTY_ID, contractException.getErrorType());

		// if the global property value is null
		contractException = assertThrows(ContractException.class, () -> globalInitialData.getGlobalPropertyValue(TestGlobalPropertyId.getUnknownGlobalPropertyId()));
		assertEquals(GlobalError.UNKNOWN_GLOBAL_PROPERTY_ID, contractException.getErrorType());

	}

}
