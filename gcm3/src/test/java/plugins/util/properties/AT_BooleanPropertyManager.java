package plugins.util.properties;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.LinkedHashMap;
import java.util.Map;

import javax.naming.Context;

import org.apache.commons.math3.random.RandomGenerator;
import org.junit.jupiter.api.Test;

import nucleus.testsupport.MockSimulationContext;
import util.ContractException;
import util.MutableDouble;
import util.SeedProvider;
import util.annotations.UnitTest;
import util.annotations.UnitTestConstructor;
import util.annotations.UnitTestMethod;

/**
 * Common interface to all person property managers. A person property manager
 * manages all the property values for people for a particular person property
 * identifier.
 * 
 * @author Shawn Hatch
 *
 */

@UnitTest(target = BooleanPropertyManager.class)
public class AT_BooleanPropertyManager {


	@Test
	@UnitTestMethod(name = "getPropertyValue", args = { int.class })
	public void testGetPropertyValue() {
		RandomGenerator randomGenerator = SeedProvider.getRandomGenerator(4879223247393954289L);

		MockSimulationContext mockContext = MockSimulationContext.builder().build();

		PropertyDefinition propertyDefinition = PropertyDefinition.builder().setType(Boolean.class).setDefaultValue(false).setTimeTrackingPolicy(TimeTrackingPolicy.TRACK_TIME).build();

		BooleanPropertyManager booleanPropertyManager = new BooleanPropertyManager(mockContext, propertyDefinition, 0);

		/*
		 * We will set the first 300 values multiple times at random
		 */
		Map<Integer, Boolean> expectedValues = new LinkedHashMap<>();

		for (int i = 0; i < 1000; i++) {
			int id = randomGenerator.nextInt(300);
			boolean value = randomGenerator.nextBoolean();
			expectedValues.put(id, value);
			booleanPropertyManager.setPropertyValue(id, value);
		}

		/*
		 * if the value was set above, then it should equal the last value place
		 * in the expected values, otherwise it will have the default value.
		 */
		for (int i = 0; i < 300; i++) {
			if (expectedValues.containsKey(i)) {
				assertEquals(expectedValues.get(i), booleanPropertyManager.getPropertyValue(i));

			} else {
				assertFalse((Boolean) booleanPropertyManager.getPropertyValue(i));

			}
		}

		// precondition tests
		ContractException contractException = assertThrows(ContractException.class, () -> booleanPropertyManager.getPropertyValue(-1));
		assertEquals(PropertyError.NEGATIVE_INDEX, contractException.getErrorType());

	}

	@Test
	@UnitTestMethod(name = "getPropertyTime", args = { int.class })
	public void testGetPropertyTime() {
		RandomGenerator randomGenerator = SeedProvider.getRandomGenerator(6779797760333524552L);

		MutableDouble time = new MutableDouble(0);
		MockSimulationContext mockContext = MockSimulationContext.builder().setTimeSupplier(() -> time.getValue()).build();

		PropertyDefinition propertyDefinition = PropertyDefinition.builder().setType(Boolean.class).setDefaultValue(false).setTimeTrackingPolicy(TimeTrackingPolicy.TRACK_TIME).build();

		BooleanPropertyManager booleanPropertyManager2 = new BooleanPropertyManager(mockContext, propertyDefinition, 0);
		for (int i = 0; i < 1000; i++) {
			int id = randomGenerator.nextInt(300);
			time.setValue(randomGenerator.nextDouble() * 1000);

			boolean value = randomGenerator.nextBoolean();
			booleanPropertyManager2.setPropertyValue(id, value);
			assertEquals(time.getValue(), booleanPropertyManager2.getPropertyTime(id), 0);
		}

		// precondition tests:
		propertyDefinition = PropertyDefinition.builder().setType(Boolean.class).setDefaultValue(false).build();
		BooleanPropertyManager booleanPropertyManager = new BooleanPropertyManager(mockContext, propertyDefinition, 0);
		ContractException contractException = assertThrows(ContractException.class, () -> booleanPropertyManager.getPropertyTime(0));
		assertEquals(PropertyError.TIME_TRACKING_OFF, contractException.getErrorType());

		contractException = assertThrows(ContractException.class, () -> booleanPropertyManager.getPropertyTime(-1));
		assertEquals(PropertyError.NEGATIVE_INDEX, contractException.getErrorType());

	}

	@Test
	@UnitTestMethod(name = "setPropertyValue", args = { int.class, Object.class })
	public void testSetPropertyValue() {
		RandomGenerator randomGenerator = SeedProvider.getRandomGenerator(4827517950755837724L);

		MockSimulationContext mockContext = MockSimulationContext.builder().build();

		PropertyDefinition propertyDefinition = PropertyDefinition.builder().setType(Boolean.class).setDefaultValue(false).setTimeTrackingPolicy(TimeTrackingPolicy.TRACK_TIME).build();

		BooleanPropertyManager booleanPropertyManager = new BooleanPropertyManager(mockContext, propertyDefinition, 0);

		/*
		 * We will set the first 300 values multiple times at random
		 */
		Map<Integer, Boolean> expectedValues = new LinkedHashMap<>();

		for (int i = 0; i < 1000; i++) {
			int id = randomGenerator.nextInt(300);
			boolean value = randomGenerator.nextBoolean();
			expectedValues.put(id, value);
			booleanPropertyManager.setPropertyValue(id, value);
		}

		/*
		 * if the value was set above, then it should equal the last value place
		 * in the expected values, otherwise it will have the default value.
		 */
		for (int i = 0; i < 300; i++) {
			if (expectedValues.containsKey(i)) {
				assertEquals(expectedValues.get(i), booleanPropertyManager.getPropertyValue(i));

			} else {
				assertFalse((Boolean) booleanPropertyManager.getPropertyValue(i));

			}
		}

		// precondition tests
		ContractException contractException = assertThrows(ContractException.class, () -> booleanPropertyManager.setPropertyValue(-1, false));
		assertEquals(PropertyError.NEGATIVE_INDEX, contractException.getErrorType());
	}

	@Test
	@UnitTestMethod(name = "removeId", args = { int.class })
	public void testRemoveId() {

		MockSimulationContext mockContext = MockSimulationContext.builder().build();

		// we will first test the manager with an initial value of false
		PropertyDefinition propertyDefinition = PropertyDefinition.builder().setType(Boolean.class).setDefaultValue(false).setTimeTrackingPolicy(TimeTrackingPolicy.TRACK_TIME).build();

		BooleanPropertyManager booleanPropertyManager = new BooleanPropertyManager(mockContext, propertyDefinition, 0);

		// initially, the value should be the default value for the manager
		assertFalse((Boolean) booleanPropertyManager.getPropertyValue(5));

		// after setting the value we should be able to retrieve a true value
		booleanPropertyManager.setPropertyValue(5, true);
		assertTrue((Boolean) booleanPropertyManager.getPropertyValue(5));

		// removing the id from the manager should have no effect, since we do
		// not waste time setting the value back to the default
		booleanPropertyManager.removeId(5);

		assertTrue((Boolean) booleanPropertyManager.getPropertyValue(5));

		// we will next test the manager with an initial value of true
		propertyDefinition = PropertyDefinition.builder().setType(Boolean.class).setDefaultValue(true).setTimeTrackingPolicy(TimeTrackingPolicy.TRACK_TIME).build();

		booleanPropertyManager = new BooleanPropertyManager(mockContext, propertyDefinition, 0);

		// initially, the value should be the default value for the manager
		assertTrue((Boolean) booleanPropertyManager.getPropertyValue(5));

		// after setting the value we should be able to retrieve a true value
		booleanPropertyManager.setPropertyValue(5, false);
		assertFalse((Boolean) booleanPropertyManager.getPropertyValue(5));

		// removing the id from the manager should have no effect, since we do
		// not waste time setting the value back to the default
		booleanPropertyManager.removeId(5);

		assertFalse((Boolean) booleanPropertyManager.getPropertyValue(5));

		// precondition tests
		PropertyDefinition def = PropertyDefinition.builder().setType(Boolean.class).setDefaultValue(true).setTimeTrackingPolicy(TimeTrackingPolicy.TRACK_TIME).build();
		BooleanPropertyManager bpm = new BooleanPropertyManager(mockContext, def, 0);
		
		ContractException contractException = assertThrows(ContractException.class, () ->bpm.removeId(-1));
		assertEquals(PropertyError.NEGATIVE_INDEX, contractException.getErrorType());

	}

	@Test
	@UnitTestConstructor(args = { Context.class, PropertyDefinition.class, int.class })
	public void testConstructor() {
		MockSimulationContext mockContext = MockSimulationContext.builder().build();

		PropertyDefinition goodPropertyDefinition = PropertyDefinition.builder().setType(Boolean.class).setDefaultValue(false).build();
		PropertyDefinition badPropertyDefinition = PropertyDefinition.builder().setType(Double.class).setDefaultValue(2.3).build();
		PropertyDefinition badBooleanPropertyDefinition = PropertyDefinition.builder().setType(Boolean.class).build();

		//precondition tests
		
		// if the property definition is null
		ContractException contractException = assertThrows(ContractException.class, () -> new BooleanPropertyManager(mockContext, null, 0));
		assertEquals(PropertyError.NULL_PROPERTY_DEFINITION, contractException.getErrorType());

		// if the property definition does not have a type of Boolean.class
		contractException = assertThrows(ContractException.class, () -> new BooleanPropertyManager(mockContext, badPropertyDefinition, 0));
		assertEquals(PropertyError.PROPERTY_DEFINITION_IMPROPER_TYPE, contractException.getErrorType());

		// if the property definition does not contain a default value
		contractException = assertThrows(ContractException.class, () -> new BooleanPropertyManager(mockContext, badBooleanPropertyDefinition, 0));
		assertEquals(PropertyError.PROPERTY_DEFINITION_MISSING_DEFAULT, contractException.getErrorType());

		// if the initial size is negative
		contractException = assertThrows(ContractException.class, () -> new BooleanPropertyManager(mockContext, goodPropertyDefinition, -1));
		assertEquals(PropertyError.NEGATIVE_INITIAL_SIZE, contractException.getErrorType());

		BooleanPropertyManager booleanPropertyManager = new BooleanPropertyManager(mockContext, goodPropertyDefinition, 0);
		assertNotNull(booleanPropertyManager);

	}
	
	@Test
	@UnitTestMethod(name = "incrementCapacity", args = { int.class })
	public void testIncrementCapacity() {
		MutableDouble time = new MutableDouble(0);
		MockSimulationContext mockContext = MockSimulationContext.builder().setTimeSupplier(() -> time.getValue()).build();

		PropertyDefinition propertyDefinition = PropertyDefinition.builder().setType(Boolean.class).setDefaultValue(false).setTimeTrackingPolicy(TimeTrackingPolicy.TRACK_TIME).build();

		BooleanPropertyManager booleanPropertyManager = new BooleanPropertyManager(mockContext, propertyDefinition, 0);

		// precondition tests
		ContractException contractException = assertThrows(ContractException.class, () -> booleanPropertyManager.incrementCapacity(-1));
		assertEquals(PropertyError.NEGATIVE_CAPACITY_INCREMENT, contractException.getErrorType());
	}

}
