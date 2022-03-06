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

@UnitTest(target = DoublePropertyManager.class)
public class AT_DoublePropertyManager {

	@Test
	@UnitTestMethod(name = "getPropertyValue", args = { int.class })
	public void testGetPropertyValue() {
		RandomGenerator randomGenerator = SeedProvider.getRandomGenerator(1599837792379294459L);

		MockSimulationContext mockContext = MockSimulationContext.builder().build();

		double defaultValue = 423.645;
		PropertyDefinition propertyDefinition = PropertyDefinition.builder().setType(Double.class).setDefaultValue(defaultValue).setTimeTrackingPolicy(TimeTrackingPolicy.TRACK_TIME).build();

		DoublePropertyManager doublePropertyManager = new DoublePropertyManager(mockContext, propertyDefinition, 0);

		/*
		 * We will set the first 300 values multiple times at random
		 */
		Map<Integer, Double> expectedValues = new LinkedHashMap<>();

		for (int i = 0; i < 1000; i++) {
			int id = randomGenerator.nextInt(300);
			double value = randomGenerator.nextDouble();
			expectedValues.put(id, value);
			doublePropertyManager.setPropertyValue(id, value);
		}

		/*
		 * if the value was set above, then it should equal the last value place
		 * in the expected values, otherwise it will have the default value.
		 */
		for (int i = 0; i < 300; i++) {
			if (expectedValues.containsKey(i)) {
				assertEquals(expectedValues.get(i), doublePropertyManager.getPropertyValue(i));

			} else {
				assertEquals(defaultValue, (Double) doublePropertyManager.getPropertyValue(i));

			}
		}

		// precondition tests
		ContractException contractException = assertThrows(ContractException.class, () -> doublePropertyManager.getPropertyValue(-1));
		assertEquals(PropertyError.NEGATIVE_INDEX, contractException.getErrorType());

	}

	@Test
	@UnitTestMethod(name = "getPropertyTime", args = { int.class })
	public void testGetPropertyTime() {
		/**
		 * Returns the assignment time when the id's property was last set. Note
		 * that this does not imply that the id exists in the simulation.
		 * 
		 * @throws RuntimeException
		 *             if time tracking is not turned on for this property via
		 *             the policies established in the scenario.
		 * 
		 */
		// public double getPropertyTime(int id);
		RandomGenerator randomGenerator = SeedProvider.getRandomGenerator(2349682401845769564L);

		MutableDouble time = new MutableDouble(0);
		MockSimulationContext mockContext = MockSimulationContext.builder().setTimeSupplier(() -> time.getValue()).build();

		PropertyDefinition propertyDefinition = PropertyDefinition.builder().setType(Double.class).setDefaultValue(234.432).build();

		DoublePropertyManager doublePropertyManager = new DoublePropertyManager(mockContext, propertyDefinition, 0);
		assertThrows(RuntimeException.class, () -> doublePropertyManager.getPropertyTime(0));

		propertyDefinition = PropertyDefinition.builder().setType(Double.class).setDefaultValue(342.4234).setTimeTrackingPolicy(TimeTrackingPolicy.TRACK_TIME).build();

		DoublePropertyManager doublePropertyManager2 = new DoublePropertyManager(mockContext, propertyDefinition, 0);
		for (int i = 0; i < 1000; i++) {
			int id = randomGenerator.nextInt(300);
			time.setValue(randomGenerator.nextDouble() * 1000);
			double value = randomGenerator.nextDouble();
			doublePropertyManager2.setPropertyValue(id, value);
			assertEquals(time.getValue(), doublePropertyManager2.getPropertyTime(id), 0);
		}

		// precondition tests:
		propertyDefinition = PropertyDefinition.builder().setType(Double.class).setDefaultValue(4.5).build();
		DoublePropertyManager dpm = new DoublePropertyManager(mockContext, propertyDefinition, 0);

		ContractException contractException = assertThrows(ContractException.class, () -> dpm.getPropertyTime(0));
		assertEquals(PropertyError.TIME_TRACKING_OFF, contractException.getErrorType());

		contractException = assertThrows(ContractException.class, () -> dpm.getPropertyTime(-1));
		assertEquals(PropertyError.NEGATIVE_INDEX, contractException.getErrorType());

	}

	@Test
	@UnitTestMethod(name = "setPropertyValue", args = { int.class, Object.class })
	public void testSetPropertyValue() {

		RandomGenerator randomGenerator = SeedProvider.getRandomGenerator(1599837792379294459L);

		MockSimulationContext mockContext = MockSimulationContext.builder().build();

		double defaultValue = 423.645;
		PropertyDefinition propertyDefinition = PropertyDefinition.builder().setType(Double.class).setDefaultValue(defaultValue).setTimeTrackingPolicy(TimeTrackingPolicy.TRACK_TIME).build();

		DoublePropertyManager doublePropertyManager = new DoublePropertyManager(mockContext, propertyDefinition, 0);

		/*
		 * We will set the first 300 values multiple times at random
		 */
		Map<Integer, Double> expectedValues = new LinkedHashMap<>();

		for (int i = 0; i < 1000; i++) {
			int id = randomGenerator.nextInt(300);
			double value = randomGenerator.nextDouble();
			expectedValues.put(id, value);
			doublePropertyManager.setPropertyValue(id, value);
		}

		/*
		 * if the value was set above, then it should equal the last value place
		 * in the expected values, otherwise it will have the default value.
		 */
		for (int i = 0; i < 300; i++) {
			if (expectedValues.containsKey(i)) {
				assertEquals(expectedValues.get(i), doublePropertyManager.getPropertyValue(i));

			} else {
				assertEquals(defaultValue, (Double) doublePropertyManager.getPropertyValue(i));

			}
		}

		// precondition tests

		ContractException contractException = assertThrows(ContractException.class, () -> doublePropertyManager.setPropertyValue(-1, 23.4));
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
		double defaultValue = 6.2345345;
		PropertyDefinition propertyDefinition = PropertyDefinition.builder().setType(Double.class).setDefaultValue(defaultValue).setTimeTrackingPolicy(TimeTrackingPolicy.TRACK_TIME).build();

		DoublePropertyManager doublePropertyManager = new DoublePropertyManager(mockContext, propertyDefinition, 0);

		// initially, the value should be the default value for the manager
		assertEquals(defaultValue, (Double) doublePropertyManager.getPropertyValue(5), 0);

		// after setting the value we should be able to retrieve a new value
		double newValue = 34534.4;
		doublePropertyManager.setPropertyValue(5, newValue);
		assertEquals(newValue, (Double) doublePropertyManager.getPropertyValue(5), 0);

		// removing the id from the manager should have no effect, since we do
		// not waste time setting the value back to the default
		doublePropertyManager.removeId(5);

		assertEquals(newValue, (Double) doublePropertyManager.getPropertyValue(5), 0);

		// we will next test the manager with an initial value of true
		propertyDefinition = PropertyDefinition.builder().setType(Double.class).setDefaultValue(defaultValue).setTimeTrackingPolicy(TimeTrackingPolicy.TRACK_TIME).build();

		doublePropertyManager = new DoublePropertyManager(mockContext, propertyDefinition, 0);

		// initially, the value should be the default value for the manager
		assertEquals(defaultValue, (Double) doublePropertyManager.getPropertyValue(5), 0);

		// after setting the value we should be able to retrieve the new value
		doublePropertyManager.setPropertyValue(5, newValue);
		assertEquals(newValue, (Double) doublePropertyManager.getPropertyValue(5), 0);

		// removing the id from the manager should have no effect, since we do
		// not waste time setting the value back to the default
		doublePropertyManager.removeId(5);

		assertEquals(newValue, (Double) doublePropertyManager.getPropertyValue(5), 0);

		// precondition tests
		ContractException contractException = assertThrows(ContractException.class, () -> {
			PropertyDefinition def = PropertyDefinition.builder().setType(Double.class).setDefaultValue(4534.4).setTimeTrackingPolicy(TimeTrackingPolicy.TRACK_TIME).build();
			DoublePropertyManager dpm = new DoublePropertyManager(mockContext, def, 0);
			dpm.removeId(-1);
		});
		assertEquals(PropertyError.NEGATIVE_INDEX, contractException.getErrorType());

	}

	@Test
	@UnitTestConstructor(args = { Context.class, PropertyDefinition.class, int.class })
	public void testConstructor() {
		MockSimulationContext mockContext = MockSimulationContext.builder().build();

		PropertyDefinition goodPropertyDefinition = PropertyDefinition.builder().setType(Double.class).setDefaultValue(2.3).build();
		PropertyDefinition badPropertyDefinition = PropertyDefinition.builder().setType(Boolean.class).setDefaultValue(false).build();
		PropertyDefinition badDoublePropertyDefinition = PropertyDefinition.builder().setType(Double.class).build();

		// precondition tests

		// if the property definition is null
		ContractException contractException = assertThrows(ContractException.class, () -> new DoublePropertyManager(mockContext, null, 0));
		assertEquals(PropertyError.NULL_PROPERTY_DEFINITION, contractException.getErrorType());

		// if the property definition does not have a type of Double.class
		contractException = assertThrows(ContractException.class, () -> new DoublePropertyManager(mockContext, badPropertyDefinition, 0));
		assertEquals(PropertyError.PROPERTY_DEFINITION_IMPROPER_TYPE, contractException.getErrorType());

		// if the property definition does not contain a default value
		contractException = assertThrows(ContractException.class, () -> new DoublePropertyManager(mockContext, badDoublePropertyDefinition, 0));
		assertEquals(PropertyError.PROPERTY_DEFINITION_MISSING_DEFAULT, contractException.getErrorType());

		// if the initial size is negative
		contractException = assertThrows(ContractException.class, () -> new DoublePropertyManager(mockContext, goodPropertyDefinition, -1));
		assertEquals(PropertyError.NEGATIVE_INITIAL_SIZE, contractException.getErrorType());

		DoublePropertyManager doublePropertyManager = new DoublePropertyManager(mockContext, goodPropertyDefinition, 0);
		assertNotNull(doublePropertyManager);

	}
	
	@Test
	@UnitTestMethod(name = "incrementCapacity", args = { int.class })
	public void testIncrementCapacity() {
		MutableDouble time = new MutableDouble(0);
		MockSimulationContext mockContext = MockSimulationContext.builder().setTimeSupplier(() -> time.getValue()).build();

		PropertyDefinition propertyDefinition = PropertyDefinition.builder().setType(Double.class).setDefaultValue(2.42).setTimeTrackingPolicy(TimeTrackingPolicy.TRACK_TIME).build();

		DoublePropertyManager doublePropertyManager = new DoublePropertyManager(mockContext, propertyDefinition, 0);

		// precondition tests
		ContractException contractException = assertThrows(ContractException.class, () -> doublePropertyManager.incrementCapacity(-1));
		assertEquals(PropertyError.NEGATIVE_CAPACITY_INCREMENT, contractException.getErrorType());
	}


}
