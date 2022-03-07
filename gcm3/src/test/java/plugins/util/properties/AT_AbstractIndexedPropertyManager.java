package plugins.util.properties;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.apache.commons.math3.random.RandomGenerator;
import org.junit.jupiter.api.Test;

import annotations.UnitTest;
import annotations.UnitTestConstructor;
import annotations.UnitTestMethod;
import nucleus.SimulationContext;
import nucleus.testsupport.MockSimulationContext;
import nucleus.util.ContractException;
import util.MutableDouble;
import util.SeedProvider;

@UnitTest(target = AbstractIndexedPropertyManager.class)

public class AT_AbstractIndexedPropertyManager {

	/*
	 * 
	 * A simple concrete extension of AbstractIndexedPropertyManager used to
	 * test AbstractIndexedPropertyManager.
	 *
	 */
	private static class SimplePropertyManager extends AbstractIndexedPropertyManager {

		public SimplePropertyManager(SimulationContext context, PropertyDefinition propertyDefinition, int initialSize) {
			super(context, propertyDefinition, initialSize);
		}

		@Override
		public <T> T getPropertyValue(int id) {
			return null;
		}

	}

	@Test
	@UnitTestConstructor(args = { SimulationContext.class, PropertyDefinition.class, int.class })
	public void testConstructor() {
		MockSimulationContext mockContext = MockSimulationContext.builder().build();

		PropertyDefinition goodPropertyDefinition = PropertyDefinition.builder().setType(Boolean.class).setDefaultValue(false).build();

		// if the property definition is null
		ContractException contractException = assertThrows(ContractException.class, () -> new BooleanPropertyManager(mockContext, null, 0));
		assertEquals(PropertyError.NULL_PROPERTY_DEFINITION, contractException.getErrorType());

		// if the initial size is negative
		contractException = assertThrows(ContractException.class, () -> new BooleanPropertyManager(mockContext, goodPropertyDefinition, -1));
		assertEquals(PropertyError.NEGATIVE_INITIAL_SIZE, contractException.getErrorType());

		SimplePropertyManager simplePropertyManager = new SimplePropertyManager(mockContext, goodPropertyDefinition, 0);
		assertNotNull(simplePropertyManager);
	}

	@Test
	@UnitTestMethod(name = "setPropertyValue", args = { int.class, Object.class })
	public void testSetPropertyValue() {
		MockSimulationContext mockContext = MockSimulationContext.builder().build();
		PropertyDefinition propertyDefinition = PropertyDefinition.builder().setType(Boolean.class).setDefaultValue(false).setTimeTrackingPolicy(TimeTrackingPolicy.TRACK_TIME).build();
		
		// precondition tests
		SimplePropertyManager simplePropertyManager = new SimplePropertyManager(mockContext, propertyDefinition, 0);
		ContractException contractException = assertThrows(ContractException.class, () -> simplePropertyManager.setPropertyValue(-1, false));
		assertEquals(PropertyError.NEGATIVE_INDEX, contractException.getErrorType());

	}

	@Test
	@UnitTestMethod(name = "getPropertyTime", args = { int.class })
	public void testGetPropertyTime() {
		RandomGenerator randomGenerator = SeedProvider.getRandomGenerator(1003433950467196390L);

		MutableDouble time = new MutableDouble(0);
		MockSimulationContext mockContext = MockSimulationContext.builder().setTimeSupplier(() -> time.getValue()).build();

		PropertyDefinition propertyDefinition = PropertyDefinition.builder().setType(Boolean.class).setDefaultValue(false).setTimeTrackingPolicy(TimeTrackingPolicy.TRACK_TIME).build();

		SimplePropertyManager simplePropertyManager = new SimplePropertyManager(mockContext, propertyDefinition, 0);
		for (int i = 0; i < 1000; i++) {
			int id = randomGenerator.nextInt(300);
			time.setValue(randomGenerator.nextDouble() * 1000);

			boolean value = randomGenerator.nextBoolean();
			simplePropertyManager.setPropertyValue(id, value);
			assertEquals(time.getValue(), simplePropertyManager.getPropertyTime(id), 0);
		}

		// precondition tests:
		propertyDefinition = PropertyDefinition.builder().setType(Boolean.class).setDefaultValue(false).build();
		SimplePropertyManager spm = new SimplePropertyManager(mockContext, propertyDefinition, 0);
		ContractException contractException = assertThrows(ContractException.class, () -> spm.getPropertyTime(0));
		assertEquals(PropertyError.TIME_TRACKING_OFF, contractException.getErrorType());

		contractException = assertThrows(ContractException.class, () -> spm.getPropertyTime(-1));
		assertEquals(PropertyError.NEGATIVE_INDEX, contractException.getErrorType());
	}

	@Test
	@UnitTestMethod(name = "removeId", args = { int.class })
	public void testRemoveId() {		

		MockSimulationContext mockContext = MockSimulationContext.builder().build();

		// precondition tests
		PropertyDefinition def = PropertyDefinition.builder().setType(Boolean.class).setDefaultValue(true).setTimeTrackingPolicy(TimeTrackingPolicy.TRACK_TIME).build();
		SimplePropertyManager spm = new SimplePropertyManager(mockContext, def, 0);
		
		ContractException contractException = assertThrows(ContractException.class, () ->spm.removeId(-1));
		assertEquals(PropertyError.NEGATIVE_INDEX, contractException.getErrorType());
	}

	@Test
	@UnitTestMethod(name = "incrementCapacity", args = { int.class })
	public void testIncrementCapacity() {
		MutableDouble time = new MutableDouble(0);
		MockSimulationContext mockContext = MockSimulationContext.builder().setTimeSupplier(() -> time.getValue()).build();

		PropertyDefinition propertyDefinition = PropertyDefinition.builder().setType(Boolean.class).setDefaultValue(false).setTimeTrackingPolicy(TimeTrackingPolicy.TRACK_TIME).build();

		SimplePropertyManager simplePropertyManager = new SimplePropertyManager(mockContext, propertyDefinition, 0);

		// precondition tests
		ContractException contractException = assertThrows(ContractException.class, () -> simplePropertyManager.incrementCapacity(-1));
		assertEquals(PropertyError.NEGATIVE_CAPACITY_INCREMENT, contractException.getErrorType());
	}
}
