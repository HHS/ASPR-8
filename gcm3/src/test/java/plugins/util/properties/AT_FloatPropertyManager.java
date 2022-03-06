package plugins.properties.support;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

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

@UnitTest(target = FloatPropertyManager.class)
public class AT_FloatPropertyManager {

	@Test
	@UnitTestMethod(name = "getPropertyValue", args = { int.class })
	public void testGetPropertyValue() {
		RandomGenerator randomGenerator = SeedProvider.getRandomGenerator(6087185710247012204L);

		MockSimulationContext mockContext = MockSimulationContext.builder().build();

		float defaultValue = 423.645F;
		PropertyDefinition propertyDefinition = PropertyDefinition.builder().setType(Float.class).setDefaultValue(defaultValue).setTimeTrackingPolicy(TimeTrackingPolicy.TRACK_TIME).build();

		FloatPropertyManager floatPropertyManager = new FloatPropertyManager(mockContext, propertyDefinition, 0);

		/*
		 * We will set the first 300 values multiple times at random
		 */
		Map<Integer, Float> expectedValues = new LinkedHashMap<>();

		for (int i = 0; i < 1000; i++) {
			int id = randomGenerator.nextInt(300);
			float value = randomGenerator.nextFloat();
			expectedValues.put(id, value);
			floatPropertyManager.setPropertyValue(id, value);
		}

		/*
		 * if the value was set above, then it should equal the last value place
		 * in the expected values, otherwise it will have the default value.
		 */
		for (int i = 0; i < 300; i++) {
			if (expectedValues.containsKey(i)) {
				assertEquals(expectedValues.get(i), floatPropertyManager.getPropertyValue(i));

			} else {
				assertEquals(defaultValue, (Float) floatPropertyManager.getPropertyValue(i));

			}
		}

		// precondition tests
		ContractException contractException = assertThrows(ContractException.class, () -> floatPropertyManager.getPropertyValue(-1));
		assertEquals(PropertyError.NEGATIVE_INDEX, contractException.getErrorType());

	}

	@Test
	@UnitTestMethod(name = "getPropertyTime", args = { int.class })
	public void testGetPropertyTime() {

		RandomGenerator randomGenerator = SeedProvider.getRandomGenerator(6894984813418975068L);

		MutableDouble time = new MutableDouble(0);
		MockSimulationContext mockContext = MockSimulationContext.builder().setTimeSupplier(() -> time.getValue()).build();

		PropertyDefinition propertyDefinition = PropertyDefinition.builder().setType(Float.class).setDefaultValue(234.432F).build();

		FloatPropertyManager floatPropertyManager = new FloatPropertyManager(mockContext, propertyDefinition, 0);
		assertThrows(RuntimeException.class, () -> floatPropertyManager.getPropertyTime(0));

		propertyDefinition = PropertyDefinition.builder().setType(Float.class).setDefaultValue(342.4234F).setTimeTrackingPolicy(TimeTrackingPolicy.TRACK_TIME).build();

		FloatPropertyManager doublePropertyManager2 = new FloatPropertyManager(mockContext, propertyDefinition, 0);
		for (int i = 0; i < 1000; i++) {
			int id = randomGenerator.nextInt(300);
			time.setValue(randomGenerator.nextDouble() * 1000);
			float value = randomGenerator.nextFloat();
			doublePropertyManager2.setPropertyValue(id, value);
			assertEquals(time.getValue(), doublePropertyManager2.getPropertyTime(id), 0);
		}

		// precondition tests:
		propertyDefinition = PropertyDefinition.builder().setType(Float.class).setDefaultValue(2.2F).build();
		FloatPropertyManager fpm = new FloatPropertyManager(mockContext, propertyDefinition, 0);
		ContractException contractException = assertThrows(ContractException.class, () -> fpm.getPropertyTime(0));
		assertEquals(PropertyError.TIME_TRACKING_OFF, contractException.getErrorType());

		contractException = assertThrows(ContractException.class, () -> fpm.getPropertyTime(-1));
		assertEquals(PropertyError.NEGATIVE_INDEX, contractException.getErrorType());

	}

	@Test
	@UnitTestMethod(name = "setPropertyValue", args = { int.class, Object.class })
	public void testSetPropertyValue() {

		RandomGenerator randomGenerator = SeedProvider.getRandomGenerator(6087185710247012204L);

		MockSimulationContext mockContext = MockSimulationContext.builder().build();

		float defaultValue = 423.645F;
		PropertyDefinition propertyDefinition = PropertyDefinition.builder().setType(Float.class).setDefaultValue(defaultValue).setTimeTrackingPolicy(TimeTrackingPolicy.TRACK_TIME).build();

		FloatPropertyManager floatPropertyManager = new FloatPropertyManager(mockContext, propertyDefinition, 0);

		/*
		 * We will set the first 300 values multiple times at random
		 */
		Map<Integer, Float> expectedValues = new LinkedHashMap<>();

		for (int i = 0; i < 1000; i++) {
			int id = randomGenerator.nextInt(300);
			float value = randomGenerator.nextFloat();
			expectedValues.put(id, value);
			floatPropertyManager.setPropertyValue(id, value);
		}

		/*
		 * if the value was set above, then it should equal the last value place
		 * in the expected values, otherwise it will have the default value.
		 */
		for (int i = 0; i < 300; i++) {
			if (expectedValues.containsKey(i)) {
				assertEquals(expectedValues.get(i), floatPropertyManager.getPropertyValue(i));

			} else {
				assertEquals(defaultValue, (Float) floatPropertyManager.getPropertyValue(i));

			}
		}

		// precondition tests
		ContractException contractException = assertThrows(ContractException.class, () -> floatPropertyManager.setPropertyValue(-1, 3.4F));
		assertEquals(PropertyError.NEGATIVE_INDEX, contractException.getErrorType());

	}

	@Test
	@UnitTestMethod(name = "removeId", args = { int.class })
	public void testRemoveId() {
		/*
		 * Should have no effect on the value that is stored for the sake of
		 * efficiency.
		 */

		MockSimulationContext mockContext = MockSimulationContext.builder().build();

		// we will first test the manager with an initial value of false
		float defaultValue = 6.2345345F;
		PropertyDefinition propertyDefinition = PropertyDefinition.builder().setType(Float.class).setDefaultValue(defaultValue).setTimeTrackingPolicy(TimeTrackingPolicy.TRACK_TIME).build();

		FloatPropertyManager floatPropertyManager = new FloatPropertyManager(mockContext, propertyDefinition, 0);

		// initially, the value should be the default value for the manager
		assertEquals(defaultValue, (Float) floatPropertyManager.getPropertyValue(5), 0);

		// after setting the value we should be able to retrieve a new value
		float newValue = 34534.4F;
		floatPropertyManager.setPropertyValue(5, newValue);
		assertEquals(newValue, (Float) floatPropertyManager.getPropertyValue(5), 0);

		// removing the id from the manager should have no effect, since we do
		// not waste time setting the value back to the default
		floatPropertyManager.removeId(5);

		assertEquals(newValue, (Float) floatPropertyManager.getPropertyValue(5), 0);

		// we will next test the manager with an initial value of true
		propertyDefinition = PropertyDefinition.builder().setType(Float.class).setDefaultValue(defaultValue).setTimeTrackingPolicy(TimeTrackingPolicy.TRACK_TIME).build();

		floatPropertyManager = new FloatPropertyManager(mockContext, propertyDefinition, 0);

		// initially, the value should be the default value for the manager
		assertEquals(defaultValue, (Float) floatPropertyManager.getPropertyValue(5), 0);

		// after setting the value we should be able to retrieve the new value
		floatPropertyManager.setPropertyValue(5, newValue);
		assertEquals(newValue, (Float) floatPropertyManager.getPropertyValue(5), 0);

		// removing the id from the manager should have no effect, since we do
		// not waste time setting the value back to the default
		floatPropertyManager.removeId(5);

		assertEquals(newValue, (Float) floatPropertyManager.getPropertyValue(5), 0);

		// precondition tests
		PropertyDefinition def = PropertyDefinition.builder().setType(Float.class).setDefaultValue(4.5F).setTimeTrackingPolicy(TimeTrackingPolicy.TRACK_TIME).build();
		FloatPropertyManager fpm = new FloatPropertyManager(mockContext, def, 0);

		ContractException contractException = assertThrows(ContractException.class, () -> fpm.removeId(-1));
		assertEquals(PropertyError.NEGATIVE_INDEX, contractException.getErrorType());

	}

	@Test
	@UnitTestConstructor(args = { Context.class, PropertyDefinition.class, int.class })
	public void testConstructor() {
		MockSimulationContext mockContext = MockSimulationContext.builder().build();

		PropertyDefinition goodPropertyDefinition = PropertyDefinition.builder().setType(Float.class).setDefaultValue(2.3F).build();
		PropertyDefinition badPropertyDefinition = PropertyDefinition.builder().setType(Boolean.class).setDefaultValue(false).build();
		PropertyDefinition badFloatPropertyDefinition = PropertyDefinition.builder().setType(Float.class).build();

		// if the property definition is null
		ContractException contractException = assertThrows(ContractException.class, () -> new FloatPropertyManager(mockContext, null, 0));
		assertEquals(PropertyError.NULL_PROPERTY_DEFINITION, contractException.getErrorType());

		// if the property definition does not have a type of Float.class
		contractException = assertThrows(ContractException.class, () -> new FloatPropertyManager(mockContext, badPropertyDefinition, 0));
		assertEquals(PropertyError.PROPERTY_DEFINITION_IMPROPER_TYPE, contractException.getErrorType());

		// if the property definition does not contain a default value
		contractException = assertThrows(ContractException.class, () -> new FloatPropertyManager(mockContext, badFloatPropertyDefinition, 0));
		assertEquals(PropertyError.PROPERTY_DEFINITION_MISSING_DEFAULT, contractException.getErrorType());

		// if the initial size is negative
		contractException = assertThrows(ContractException.class, () -> new FloatPropertyManager(mockContext, goodPropertyDefinition, -1));
		assertEquals(PropertyError.NEGATIVE_INITIAL_SIZE, contractException.getErrorType());

		FloatPropertyManager doublePropertyManager = new FloatPropertyManager(mockContext, goodPropertyDefinition, 0);
		assertNotNull(doublePropertyManager);

	}
	
	@Test
	@UnitTestMethod(name = "incrementCapacity", args = { int.class })
	public void testIncrementCapacity() {
		MutableDouble time = new MutableDouble(0);
		MockSimulationContext mockContext = MockSimulationContext.builder().setTimeSupplier(() -> time.getValue()).build();

		PropertyDefinition propertyDefinition = PropertyDefinition.builder().setType(Float.class).setDefaultValue(234.42F).setTimeTrackingPolicy(TimeTrackingPolicy.TRACK_TIME).build();

		FloatPropertyManager floatPropertyManager = new FloatPropertyManager(mockContext, propertyDefinition, 0);

		// precondition tests
		ContractException contractException = assertThrows(ContractException.class, () -> floatPropertyManager.incrementCapacity(-1));
		assertEquals(PropertyError.NEGATIVE_CAPACITY_INCREMENT, contractException.getErrorType());
	}

}
