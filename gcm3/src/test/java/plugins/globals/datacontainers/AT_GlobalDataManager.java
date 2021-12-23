package plugins.globals.datacontainers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.math3.random.RandomGenerator;
import org.junit.jupiter.api.Test;

import nucleus.ResolverContext;
import nucleus.testsupport.MockContext;
import plugins.globals.initialdata.GlobalInitialData;
import plugins.globals.support.GlobalComponentId;
import plugins.globals.support.GlobalError;
import plugins.globals.support.GlobalPropertyId;
import plugins.globals.support.SimpleGlobalComponentId;
import plugins.globals.support.SimpleGlobalPropertyId;
import plugins.properties.support.PropertyDefinition;
import util.ContractException;
import util.MutableDouble;
import util.MutableInteger;
import util.SeedProvider;
import util.annotations.UnitTest;
import util.annotations.UnitTestConstructor;
import util.annotations.UnitTestMethod;

@UnitTest(target = GlobalDataManager.class)

public final class AT_GlobalDataManager {

	@Test
	@UnitTestConstructor(args = { ResolverContext.class, GlobalInitialData.class })
	public void testConstructor() {
		// show that we can create a global data manager
		assertNotNull(new GlobalDataManager(MockContext.builder().build()));
	}

	@Test
	@UnitTestMethod(name = "getGlobalPropertyDefinition", args = { GlobalPropertyId.class })
	public void testGetGlobalPropertyDefinition() {
		GlobalDataManager globalDataManager = new GlobalDataManager(MockContext.builder().build());

		// create a container for the expected property definitions
		Map<GlobalPropertyId, PropertyDefinition> expectedPropertyDefintions = new LinkedHashMap<>();

		// Generate a few global property definitions
		for (int i = 0; i < 10; i++) {
			GlobalPropertyId globalPropertyId = new SimpleGlobalPropertyId("property id " + i);
			PropertyDefinition propertyDefinition = PropertyDefinition.builder().setType(Integer.class).setDefaultValue(i).build();
			globalDataManager.addGlobalPropertyDefinition(globalPropertyId, propertyDefinition);
			expectedPropertyDefintions.put(globalPropertyId, propertyDefinition);
		}

		// show that each expected property definition can be retrieved
		for (GlobalPropertyId globalPropertyId : expectedPropertyDefintions.keySet()) {
			PropertyDefinition expectedPropertyDefinition = expectedPropertyDefintions.get(globalPropertyId);
			PropertyDefinition actualPropertyDefinition = globalDataManager.getGlobalPropertyDefinition(globalPropertyId);
			assertEquals(expectedPropertyDefinition, actualPropertyDefinition);
		}

		// show that null or unknown property ids result in null property
		// definitions
		assertNull(globalDataManager.getGlobalPropertyDefinition(null));
		assertNull(globalDataManager.getGlobalPropertyDefinition(new SimpleGlobalPropertyId("unknown")));
	}

	@Test
	@UnitTestMethod(name = "getGlobalPropertyIds", args = {})
	public void testGetGlobalPropertyIds() {
		GlobalDataManager globalDataManager = new GlobalDataManager(MockContext.builder().build());

		// create a container for the expected property ids
		Set<GlobalPropertyId> expectedGlobalPropertyIds = new LinkedHashSet<>();

		// Generate a few global property definitions
		for (int i = 0; i < 10; i++) {
			GlobalPropertyId globalPropertyId = new SimpleGlobalPropertyId("property id " + i);
			PropertyDefinition propertyDefinition = PropertyDefinition.builder().setType(Integer.class).setDefaultValue(i).build();
			globalDataManager.addGlobalPropertyDefinition(globalPropertyId, propertyDefinition);
			expectedGlobalPropertyIds.add(globalPropertyId);
		}

		// show that each expected property id can be retrieved
		Set<GlobalPropertyId> actualGlobalPropertyIds = globalDataManager.getGlobalPropertyIds();
		assertEquals(expectedGlobalPropertyIds, actualGlobalPropertyIds);
	}

	@Test
	@UnitTestMethod(name = "getGlobalPropertyValue", args = { GlobalPropertyId.class })
	public void testGetGlobalPropertyValue() {
		RandomGenerator randomGenerator = SeedProvider.getRandomGenerator(8148367750247612326L);

		GlobalDataManager globalDataManager = new GlobalDataManager(MockContext.builder().build());
		Map<GlobalPropertyId, MutableInteger> expectedGlobalPropertyValues = new LinkedHashMap<>();
		for (int i = 0; i < 10; i++) {
			GlobalPropertyId globalPropertyId = new SimpleGlobalPropertyId("property id " + i);
			PropertyDefinition propertyDefinition = PropertyDefinition.builder().setType(Integer.class).setDefaultValue(i).build();
			globalDataManager.addGlobalPropertyDefinition(globalPropertyId, propertyDefinition);
			expectedGlobalPropertyValues.put(globalPropertyId, new MutableInteger(i));
		}

		// Set the global property values a few times and record the values.
		for (int i = 0; i < 20; i++) {
			int index = i % 10;
			GlobalPropertyId globalPropertyId = new SimpleGlobalPropertyId("property id " + index);
			int value = randomGenerator.nextInt();
			globalDataManager.setGlobalPropertyValue(globalPropertyId, value);
			expectedGlobalPropertyValues.get(globalPropertyId).setValue(value);
		}

		for (int i = 0; i < 10; i++) {
			GlobalPropertyId globalPropertyId = new SimpleGlobalPropertyId("property id " + i);
			Integer actualValue = globalDataManager.getGlobalPropertyValue(globalPropertyId);
			MutableInteger expectedValue = expectedGlobalPropertyValues.get(globalPropertyId);
			assertEquals(expectedValue.getValue(), actualValue);
		}

		assertThrows(RuntimeException.class, () -> globalDataManager.getGlobalPropertyValue(null));
		assertThrows(RuntimeException.class, () -> globalDataManager.getGlobalPropertyValue(new SimpleGlobalPropertyId("bad prop")));

	}

	@Test
	@UnitTestMethod(name = "getGlobalPropertyTime", args = { GlobalPropertyId.class })
	public void testGetGlobalPropertyTime() {
		MutableDouble time = new MutableDouble();
		RandomGenerator randomGenerator = SeedProvider.getRandomGenerator(8148367750247612326L);
		MockContext mockContext = MockContext.builder().setTimeSupplier(() -> time.getValue()).build();
		GlobalDataManager globalDataManager = new GlobalDataManager(mockContext);
		Map<GlobalPropertyId, MutableDouble> expectedGlobalPropertyTimes = new LinkedHashMap<>();
		for (int i = 0; i < 10; i++) {
			GlobalPropertyId globalPropertyId = new SimpleGlobalPropertyId("property id " + i);
			PropertyDefinition propertyDefinition = PropertyDefinition.builder().setType(Integer.class).setDefaultValue(i).build();
			globalDataManager.addGlobalPropertyDefinition(globalPropertyId, propertyDefinition);
			expectedGlobalPropertyTimes.put(globalPropertyId, new MutableDouble(mockContext.getTime()));
		}

		// Set the global property values a few times and record the values.
		for (int i = 0; i < 20; i++) {
			time.increment(randomGenerator.nextDouble());
			int index = i % 10;
			GlobalPropertyId globalPropertyId = new SimpleGlobalPropertyId("property id " + index);
			int value = randomGenerator.nextInt();
			globalDataManager.setGlobalPropertyValue(globalPropertyId, value);
			expectedGlobalPropertyTimes.get(globalPropertyId).setValue(mockContext.getTime());
		}

		// show that the time associated with each value is correct
		for (int i = 0; i < 10; i++) {
			GlobalPropertyId globalPropertyId = new SimpleGlobalPropertyId("property id " + i);
			double actualTime = globalDataManager.getGlobalPropertyTime(globalPropertyId);
			MutableDouble expectedTime = expectedGlobalPropertyTimes.get(globalPropertyId);
			assertEquals(expectedTime.getValue(), actualTime);
		}

		// show that a null global property id will throw a RuntimeException
		assertThrows(RuntimeException.class, () -> globalDataManager.getGlobalPropertyTime(null));

		// show that an unknown global property id will throw a RuntimeException
		assertThrows(RuntimeException.class, () -> globalDataManager.getGlobalPropertyTime(new SimpleGlobalPropertyId("bad prop")));

	}

	@Test
	@UnitTestMethod(name = "setGlobalPropertyValue", args = { GlobalPropertyId.class, Object.class })
	public void testSetGlobalPropertyValue() {

		RandomGenerator randomGenerator = SeedProvider.getRandomGenerator(8148367750247612326L);
		MockContext mockContext = MockContext.builder().build();
		GlobalDataManager globalDataManager = new GlobalDataManager(mockContext);

		for (int i = 0; i < 10; i++) {
			GlobalPropertyId globalPropertyId = new SimpleGlobalPropertyId("property id " + i);
			PropertyDefinition propertyDefinition = PropertyDefinition.builder().setType(Integer.class).setDefaultValue(i).build();
			globalDataManager.addGlobalPropertyDefinition(globalPropertyId, propertyDefinition);
		}

		// Set the global property values a few times and record the values.
		for (int i = 0; i < 10; i++) {
			GlobalPropertyId globalPropertyId = new SimpleGlobalPropertyId("property id " + i);
			int expectedValue = randomGenerator.nextInt();
			globalDataManager.setGlobalPropertyValue(globalPropertyId, expectedValue);
			Object actualValue = globalDataManager.getGlobalPropertyValue(globalPropertyId);
			assertEquals(expectedValue, actualValue);
		}

		// show that a null global property id will throw a RuntimeException
		assertThrows(RuntimeException.class, () -> globalDataManager.setGlobalPropertyValue(null, null));

		// show that an unknown global property id will throw a RuntimeException
		assertThrows(RuntimeException.class, () -> globalDataManager.setGlobalPropertyValue(new SimpleGlobalPropertyId("bad prop"), null));

	}

	@Test
	@UnitTestMethod(name = "globalPropertyIdExists", args = { GlobalPropertyId.class })
	public void testGlobalPropertyIdExists() {
		MockContext mockContext = MockContext.builder().build();
		GlobalDataManager globalDataManager = new GlobalDataManager(mockContext);

		for (int i = 0; i < 10; i++) {
			GlobalPropertyId globalPropertyId = new SimpleGlobalPropertyId("property id " + i);
			PropertyDefinition propertyDefinition = PropertyDefinition.builder().setType(Integer.class).setDefaultValue(i).build();
			globalDataManager.addGlobalPropertyDefinition(globalPropertyId, propertyDefinition);
		}

		// Set the global property values a few times and record the values.
		for (int i = 0; i < 10; i++) {
			GlobalPropertyId globalPropertyId = new SimpleGlobalPropertyId("property id " + i);
			assertTrue(globalDataManager.globalPropertyIdExists(globalPropertyId));
		}

		// show that a null global property id will return false
		assertFalse(globalDataManager.globalPropertyIdExists(null));

		// show that an unknown global property id will return false
		assertFalse(globalDataManager.globalPropertyIdExists(new SimpleGlobalPropertyId("bad prop")));

	}

	@Test
	@UnitTestMethod(name = "getGlobalComponentIds", args = {})
	public void testGetGlobalComponentIds() {
		MockContext mockContext = MockContext.builder().build();
		GlobalDataManager globalDataManager = new GlobalDataManager(mockContext);

		Set<GlobalComponentId> expectedGlobalComponentIds = new LinkedHashSet<>();
		for (int i = 0; i < 10; i++) {
			GlobalComponentId globalComponentId = new SimpleGlobalComponentId(i);
			globalDataManager.addGlobalComponentId(globalComponentId);
			expectedGlobalComponentIds.add(globalComponentId);
		}

		assertEquals(expectedGlobalComponentIds, globalDataManager.getGlobalComponentIds());

	}

	@Test
	@UnitTestMethod(name = "addGlobalComponentId", args = { GlobalComponentId.class })
	public void testAddGlobalComponentId() {
		MockContext mockContext = MockContext.builder().build();
		GlobalDataManager globalDataManager = new GlobalDataManager(mockContext);

		Set<GlobalComponentId> expectedGlobalComponentIds = new LinkedHashSet<>();
		for (int i = 0; i < 10; i++) {
			GlobalComponentId globalComponentId = new SimpleGlobalComponentId(i);
			globalDataManager.addGlobalComponentId(globalComponentId);
			expectedGlobalComponentIds.add(globalComponentId);
		}

		assertEquals(expectedGlobalComponentIds, globalDataManager.getGlobalComponentIds());

		ContractException contractException = assertThrows(ContractException.class, () -> globalDataManager.addGlobalComponentId(null));
		assertEquals(GlobalError.NULL_GLOBAL_COMPONENT_ID, contractException.getErrorType());

		globalDataManager.addGlobalComponentId(new SimpleGlobalComponentId("repeated"));
		contractException = assertThrows(ContractException.class, () -> globalDataManager.addGlobalComponentId(new SimpleGlobalComponentId("repeated")));
		assertEquals(GlobalError.DUPLICATE_GLOBAL_COMPONENT_ID, contractException.getErrorType());

	}

	@Test
	@UnitTestMethod(name = "addGlobalPropertyDefinition", args = { GlobalPropertyId.class, PropertyDefinition.class })
	public void testAddGlobalPropertyDefinition() {
		GlobalDataManager globalDataManager = new GlobalDataManager(MockContext.builder().build());

		// create a container for the expected property definitions
		Map<GlobalPropertyId, PropertyDefinition> expectedPropertyDefintions = new LinkedHashMap<>();

		// Generate a few global property definitions
		for (int i = 0; i < 10; i++) {
			GlobalPropertyId globalPropertyId = new SimpleGlobalPropertyId("property id " + i);
			PropertyDefinition propertyDefinition = PropertyDefinition.builder().setType(Integer.class).setDefaultValue(i).build();
			globalDataManager.addGlobalPropertyDefinition(globalPropertyId, propertyDefinition);
			expectedPropertyDefintions.put(globalPropertyId, propertyDefinition);
		}

		// show that each expected property definition can be retrieved
		for (GlobalPropertyId globalPropertyId : expectedPropertyDefintions.keySet()) {
			PropertyDefinition expectedPropertyDefinition = expectedPropertyDefintions.get(globalPropertyId);
			PropertyDefinition actualPropertyDefinition = globalDataManager.getGlobalPropertyDefinition(globalPropertyId);
			assertEquals(expectedPropertyDefinition, actualPropertyDefinition);
		}

		// precondition tests
		PropertyDefinition propertyDefinition = PropertyDefinition.builder().setType(Integer.class).setDefaultValue(0).build();
		ContractException contractException = assertThrows(ContractException.class, () -> globalDataManager.addGlobalPropertyDefinition(null, propertyDefinition));
		assertEquals(GlobalError.NULL_GLOBAL_PROPERTY_ID, contractException.getErrorType());

		contractException = assertThrows(ContractException.class, () -> globalDataManager.addGlobalPropertyDefinition(new SimpleGlobalPropertyId("id"), null));
		assertEquals(GlobalError.NULL_GLOBAL_PROPERTY_DEFINITION, contractException.getErrorType());

		globalDataManager.addGlobalPropertyDefinition(new SimpleGlobalPropertyId("id2"), propertyDefinition);
		contractException = assertThrows(ContractException.class, () -> globalDataManager.addGlobalPropertyDefinition(new SimpleGlobalPropertyId("id2"), propertyDefinition));
		assertEquals(GlobalError.DUPLICATE_GLOBAL_PROPERTY_DEFINITION, contractException.getErrorType());

	}

}
