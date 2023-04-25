package plugins.util.properties;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.commons.math3.random.RandomGenerator;
import org.junit.jupiter.api.Test;

import nucleus.testsupport.testplugin.TestPluginFactory;
import nucleus.testsupport.testplugin.TestPluginFactory.Factory;
import nucleus.testsupport.testplugin.TestSimulation;
import util.annotations.UnitTestConstructor;
import util.annotations.UnitTestMethod;
import util.errors.ContractException;
import util.random.RandomGeneratorProvider;

/**
 * Common interface to all person property managers. A person property manager
 * manages all the property values for people for a particular person property
 * identifier.
 * 
 *
 */

public class AT_EnumPropertyManager {

	@Test
	@UnitTestMethod(target = EnumPropertyManager.class, name = "getPropertyValue", args = { int.class })
	public void testGetPropertyValue() {
		Factory factory = TestPluginFactory.factory((c) -> {
			RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(5102684240650614254L);

			Color defaultValue = Color.YELLOW;
			PropertyDefinition propertyDefinition = PropertyDefinition.builder().setType(Color.class).setDefaultValue(defaultValue).setTimeTrackingPolicy(TimeTrackingPolicy.TRACK_TIME).build();

			EnumPropertyManager enumPropertyManager = new EnumPropertyManager(propertyDefinition, 0);

			/*
			 * We will set the first 300 values multiple times at random
			 */
			Map<Integer, Color> expectedValues = new LinkedHashMap<>();

			for (int i = 0; i < 1000; i++) {
				int id = randomGenerator.nextInt(300);
				Color value = Color.values()[randomGenerator.nextInt(Color.values().length)];
				expectedValues.put(id, value);
				enumPropertyManager.setPropertyValue(id, value);
			}

			/*
			 * if the value was set above, then it should equal the last value
			 * place in the expected values, otherwise it will have the default
			 * value.
			 */
			for (int i = 0; i < 300; i++) {
				if (expectedValues.containsKey(i)) {
					assertEquals(expectedValues.get(i), enumPropertyManager.getPropertyValue(i));

				} else {
					assertEquals(defaultValue, (Color) enumPropertyManager.getPropertyValue(i));

				}
			}

			// precondition tests
			ContractException contractException = assertThrows(ContractException.class, () -> enumPropertyManager.getPropertyValue(-1));
			assertEquals(PropertyError.NEGATIVE_INDEX, contractException.getErrorType());
		});
		TestSimulation.builder().addPlugins(factory.getPlugins()).build().execute();
	}


	@Test
	@UnitTestMethod(target = EnumPropertyManager.class, name = "setPropertyValue", args = { int.class, Object.class })
	public void testSetPropertyValue() {
		Factory factory = TestPluginFactory.factory((c) -> {
			RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(6716984272666831621L);

			Color defaultValue = Color.YELLOW;
			PropertyDefinition propertyDefinition = PropertyDefinition.builder().setType(Color.class).setDefaultValue(defaultValue).setTimeTrackingPolicy(TimeTrackingPolicy.TRACK_TIME).build();

			EnumPropertyManager enumPropertyManager = new EnumPropertyManager(propertyDefinition, 0);

			/*
			 * We will set the first 300 values multiple times at random
			 */
			Map<Integer, Color> expectedValues = new LinkedHashMap<>();

			for (int i = 0; i < 1000; i++) {
				int id = randomGenerator.nextInt(300);
				Color value = Color.values()[randomGenerator.nextInt(Color.values().length)];
				expectedValues.put(id, value);
				enumPropertyManager.setPropertyValue(id, value);
			}

			/*
			 * if the value was set above, then it should equal the last value
			 * place in the expected values, otherwise it will have the default
			 * value.
			 */
			for (int i = 0; i < 300; i++) {
				if (expectedValues.containsKey(i)) {
					assertEquals(expectedValues.get(i), enumPropertyManager.getPropertyValue(i));

				} else {
					assertEquals(defaultValue, (Color) enumPropertyManager.getPropertyValue(i));

				}
			}

			// precondition tests
			ContractException contractException = assertThrows(ContractException.class, () -> enumPropertyManager.setPropertyValue(-1, Color.BLUE));
			assertEquals(PropertyError.NEGATIVE_INDEX, contractException.getErrorType());
		});
		TestSimulation.builder().addPlugins(factory.getPlugins()).build().execute();
	}

	@Test
	@UnitTestMethod(target = EnumPropertyManager.class, name = "removeId", args = { int.class })
	public void testRemoveId() {
		Factory factory = TestPluginFactory.factory((c) -> {
			/*
			 * Should have no effect on the value that is stored for the sake of
			 * efficiency.
			 */

			// we will first test the manager with an initial value of false
			Color defaultValue = Color.RED;
			PropertyDefinition propertyDefinition = PropertyDefinition.builder().setType(Color.class).setDefaultValue(defaultValue).setTimeTrackingPolicy(TimeTrackingPolicy.TRACK_TIME).build();

			EnumPropertyManager enumPropertyManager = new EnumPropertyManager(propertyDefinition, 0);

			// initially, the value should be the default value for the manager
			assertEquals(defaultValue, (Color) enumPropertyManager.getPropertyValue(5));

			// after setting the value we should be able to retrieve a new value
			Color newValue = Color.BLUE;
			enumPropertyManager.setPropertyValue(5, newValue);
			assertEquals(newValue, (Color) enumPropertyManager.getPropertyValue(5));

			// removing the id from the manager should have no effect, since we
			// do
			// not waste time setting the value back to the default
			enumPropertyManager.removeId(5);

			assertEquals(newValue, (Color) enumPropertyManager.getPropertyValue(5));

			// we will next test the manager with an initial value of true
			propertyDefinition = PropertyDefinition.builder().setType(Color.class).setDefaultValue(defaultValue).setTimeTrackingPolicy(TimeTrackingPolicy.TRACK_TIME).build();

			enumPropertyManager = new EnumPropertyManager(propertyDefinition, 0);

			// initially, the value should be the default value for the manager
			assertEquals(defaultValue, (Color) enumPropertyManager.getPropertyValue(5));

			// after setting the value we should be able to retrieve the new
			// value
			enumPropertyManager.setPropertyValue(5, newValue);
			assertEquals(newValue, (Color) enumPropertyManager.getPropertyValue(5));

			// removing the id from the manager should have no effect, since we
			// do
			// not waste time setting the value back to the default
			enumPropertyManager.removeId(5);

			assertEquals(newValue, (Color) enumPropertyManager.getPropertyValue(5));

			// precondition tests
			// precondition tests
			PropertyDefinition def = PropertyDefinition.builder().setType(Color.class).setDefaultValue(Color.YELLOW).setTimeTrackingPolicy(TimeTrackingPolicy.TRACK_TIME).build();
			EnumPropertyManager epm = new EnumPropertyManager(def, 0);

			ContractException contractException = assertThrows(ContractException.class, () -> epm.removeId(-1));

			assertEquals(PropertyError.NEGATIVE_INDEX, contractException.getErrorType());
		});
		TestSimulation.builder().addPlugins(factory.getPlugins()).build().execute();
	}

	// Helper enum
	private static enum Color {
		RED, YELLOW, BLUE;
	}

	@Test
	@UnitTestConstructor(target = EnumPropertyManager.class, args = {PropertyDefinition.class, int.class })
	public void testConstructor() {
		Factory factory = TestPluginFactory.factory((c) -> {

			PropertyDefinition goodPropertyDefinition = PropertyDefinition.builder().setType(Color.class).setDefaultValue(Color.BLUE).build();
			PropertyDefinition badPropertyDefinition = PropertyDefinition.builder().setType(Boolean.class).setDefaultValue(false).build();

			// if the property definition is null
			ContractException contractException = assertThrows(ContractException.class, () -> new EnumPropertyManager(null, 0));
			assertEquals(PropertyError.NULL_PROPERTY_DEFINITION, contractException.getErrorType());

			// if the property definition does not have a type of Enum.class
			contractException = assertThrows(ContractException.class, () -> new EnumPropertyManager(badPropertyDefinition, 0));
			assertEquals(PropertyError.PROPERTY_DEFINITION_IMPROPER_TYPE, contractException.getErrorType());

			// if the initial size is negative
			contractException = assertThrows(ContractException.class, () -> new EnumPropertyManager(goodPropertyDefinition, -1));
			assertEquals(PropertyError.NEGATIVE_INITIAL_SIZE, contractException.getErrorType());

			EnumPropertyManager enumPropertyManager = new EnumPropertyManager(goodPropertyDefinition, 0);
			assertNotNull(enumPropertyManager);
		});
		TestSimulation.builder().addPlugins(factory.getPlugins()).build().execute();
	}

	@Test
	@UnitTestMethod(target = EnumPropertyManager.class, name = "incrementCapacity", args = { int.class })
	public void testIncrementCapacity() {
		Factory factory = TestPluginFactory.factory((c) -> {

			PropertyDefinition propertyDefinition = PropertyDefinition.builder().setType(Color.class).setDefaultValue(Color.RED).setTimeTrackingPolicy(TimeTrackingPolicy.TRACK_TIME).build();

			EnumPropertyManager enumPropertyManager = new EnumPropertyManager(propertyDefinition, 0);

			// precondition tests
			ContractException contractException = assertThrows(ContractException.class, () -> enumPropertyManager.incrementCapacity(-1));
			assertEquals(PropertyError.NEGATIVE_CAPACITY_INCREMENT, contractException.getErrorType());
		});
		TestSimulation.builder().addPlugins(factory.getPlugins()).build().execute();
	}

}
