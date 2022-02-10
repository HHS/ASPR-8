package plugins.globals.datacontainers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.math3.random.RandomGenerator;
import org.junit.jupiter.api.Test;

import nucleus.DataView;
import nucleus.NucleusError;
import nucleus.DataManagerContext;
import nucleus.testsupport.MockSimulationContext;
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

@UnitTest(target = GlobalDataView.class)
public final class AT_GlobalDataView implements DataView {

	@Test
	@UnitTestConstructor(args = { DataManagerContext.class, GlobalDataManager.class })
	public void testConstructor() {
		MockSimulationContext mockSimulationContext = MockSimulationContext.builder().build();
		GlobalDataManager globalDataManager = new GlobalDataManager(mockSimulationContext);
		assertNotNull(new GlobalDataView(mockSimulationContext, globalDataManager));

		ContractException contractException = assertThrows(ContractException.class, () -> new GlobalDataView(null, globalDataManager));
		assertEquals(NucleusError.NULL_CONTEXT, contractException.getErrorType());

		contractException = assertThrows(ContractException.class, () -> new GlobalDataView(mockSimulationContext, null));
		assertEquals(GlobalError.NULL_GLOBAL_DATA_MANGER, contractException.getErrorType());

	}

	@Test
	@UnitTestMethod(name = "getGlobalPropertyDefinition", args = { GlobalPropertyId.class })
	public void testGetGlobalPropertyDefinition() {
		MockSimulationContext mockSimulationContext = MockSimulationContext.builder().build();
		GlobalDataManager globalDataManager = new GlobalDataManager(mockSimulationContext);
		GlobalDataView globalDataView = new GlobalDataView(mockSimulationContext, globalDataManager);

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
			PropertyDefinition actualPropertyDefinition = globalDataView.getGlobalPropertyDefinition(globalPropertyId);
			assertEquals(expectedPropertyDefinition, actualPropertyDefinition);
		}

		// show that null or unknown property ids result in null property
		// definitions

		ContractException contractException = assertThrows(ContractException.class, () -> globalDataView.getGlobalPropertyDefinition(null));
		assertEquals(GlobalError.NULL_GLOBAL_PROPERTY_ID, contractException.getErrorType());

		contractException = assertThrows(ContractException.class, () -> globalDataView.getGlobalPropertyDefinition(new SimpleGlobalPropertyId("unknown")));
		assertEquals(GlobalError.UNKNOWN_GLOBAL_PROPERTY_ID, contractException.getErrorType());
	}

	@Test
	@UnitTestMethod(name = "getGlobalPropertyIds", args = {})
	public void testGetGlobalPropertyIds() {
		MockSimulationContext mockSimulationContext = MockSimulationContext.builder().build();
		GlobalDataManager globalDataManager = new GlobalDataManager(mockSimulationContext);
		GlobalDataView globalDataView = new GlobalDataView(mockSimulationContext, globalDataManager);
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
		Set<GlobalPropertyId> actualGlobalPropertyIds = globalDataView.getGlobalPropertyIds();
		assertEquals(expectedGlobalPropertyIds, actualGlobalPropertyIds);
	}

	@Test
	@UnitTestMethod(name = "getGlobalComponentIds", args = {})
	public void testGetGlobalComponentIds() {
		MockSimulationContext mockSimulationContext = MockSimulationContext.builder().build();
		GlobalDataManager globalDataManager = new GlobalDataManager(mockSimulationContext);
		GlobalDataView globalDataView = new GlobalDataView(mockSimulationContext, globalDataManager);

		Set<GlobalComponentId> expectedGlobalComponentIds = new LinkedHashSet<>();
		for (int i = 0; i < 10; i++) {
			GlobalComponentId globalComponentId = new SimpleGlobalComponentId(i);
			globalDataManager.addGlobalComponentId(globalComponentId);
			expectedGlobalComponentIds.add(globalComponentId);
		}
		assertEquals(expectedGlobalComponentIds, globalDataView.getGlobalComponentIds());
	}

	@Test
	@UnitTestMethod(name = "getGlobalPropertyValue", args = { GlobalPropertyId.class })
	public void testGetGlobalPropertyValue() {
		RandomGenerator randomGenerator = SeedProvider.getRandomGenerator(1059537118783693383L);

		MockSimulationContext mockSimulationContext = MockSimulationContext.builder().build();
		GlobalDataManager globalDataManager = new GlobalDataManager(mockSimulationContext);
		GlobalDataView globalDataView = new GlobalDataView(mockSimulationContext, globalDataManager);

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
			Integer actualValue = globalDataView.getGlobalPropertyValue(globalPropertyId);
			MutableInteger expectedValue = expectedGlobalPropertyValues.get(globalPropertyId);
			assertEquals(expectedValue.getValue(), actualValue);
		}

		ContractException contractException = assertThrows(ContractException.class, () -> globalDataView.getGlobalPropertyValue(null));
		assertEquals(GlobalError.NULL_GLOBAL_PROPERTY_ID, contractException.getErrorType());

		contractException = assertThrows(ContractException.class, () -> globalDataView.getGlobalPropertyValue(new SimpleGlobalPropertyId("bad prop")));
		assertEquals(GlobalError.UNKNOWN_GLOBAL_PROPERTY_ID, contractException.getErrorType());
	}

	@Test
	@UnitTestMethod(name = "getGlobalPropertyTime", args = { GlobalPropertyId.class })
	public void testGetGlobalPropertyTime() {
		MutableDouble time = new MutableDouble();
		RandomGenerator randomGenerator = SeedProvider.getRandomGenerator(5323616867741088481L);
		MockSimulationContext mockSimulationContext = MockSimulationContext.builder().setTimeSupplier(() -> time.getValue()).build();	
		GlobalDataManager globalDataManager = new GlobalDataManager(mockSimulationContext);
		GlobalDataView globalDataView = new GlobalDataView(mockSimulationContext, globalDataManager);
		Map<GlobalPropertyId, MutableDouble> expectedGlobalPropertyTimes = new LinkedHashMap<>();
		for (int i = 0; i < 10; i++) {
			GlobalPropertyId globalPropertyId = new SimpleGlobalPropertyId("property id " + i);
			PropertyDefinition propertyDefinition = PropertyDefinition.builder().setType(Integer.class).setDefaultValue(i).build();
			globalDataManager.addGlobalPropertyDefinition(globalPropertyId, propertyDefinition);
			expectedGlobalPropertyTimes.put(globalPropertyId, new MutableDouble(mockSimulationContext.getTime()));
		}

		// Set the global property values a few times and record the values.
		for (int i = 0; i < 20; i++) {
			time.increment(randomGenerator.nextDouble());
			int index = i % 10;
			GlobalPropertyId globalPropertyId = new SimpleGlobalPropertyId("property id " + index);
			int value = randomGenerator.nextInt();
			globalDataManager.setGlobalPropertyValue(globalPropertyId, value);
			expectedGlobalPropertyTimes.get(globalPropertyId).setValue(mockSimulationContext.getTime());
		}

		// show that the time associated with each value is correct
		for (int i = 0; i < 10; i++) {
			GlobalPropertyId globalPropertyId = new SimpleGlobalPropertyId("property id " + i);
			double actualTime = globalDataView.getGlobalPropertyTime(globalPropertyId);
			MutableDouble expectedTime = expectedGlobalPropertyTimes.get(globalPropertyId);
			assertEquals(expectedTime.getValue(), actualTime);
		}

		 
		ContractException contractException = assertThrows(ContractException.class, () -> globalDataView.getGlobalPropertyTime(null));
		assertEquals(GlobalError.NULL_GLOBAL_PROPERTY_ID, contractException.getErrorType());

		
		contractException = assertThrows(ContractException.class, () -> globalDataView.getGlobalPropertyTime(new SimpleGlobalPropertyId("bad prop")));
		assertEquals(GlobalError.UNKNOWN_GLOBAL_PROPERTY_ID, contractException.getErrorType());
		
	}

}
