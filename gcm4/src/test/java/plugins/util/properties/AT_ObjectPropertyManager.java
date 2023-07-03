package plugins.util.properties;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Collections;
import java.util.Iterator;
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

public class AT_ObjectPropertyManager {
	
	
	private Iterator<Integer> getEmptyIndexIterator() {
		return Collections.emptyIterator();
	}

	@Test
	@UnitTestMethod(target = ObjectPropertyManager.class, name = "getPropertyValue", args = { int.class })
	public void testGetPropertyValue() {
		Factory factory = TestPluginFactory.factory((c) -> {
			RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(3707927404057976793L);

			String defaultValue = "YELLOW";
			PropertyDefinition propertyDefinition = PropertyDefinition.builder().setType(String.class).setDefaultValue(defaultValue).build();

			ObjectPropertyManager objectPropertyManager = new ObjectPropertyManager(propertyDefinition, this::getEmptyIndexIterator);

			/*
			 * We will set the first 300 values multiple times at random
			 */
			Map<Integer, String> expectedValues = new LinkedHashMap<>();

			for (int i = 0; i < 1000; i++) {
				int id = randomGenerator.nextInt(300);
				String value = getRandomString(randomGenerator);
				expectedValues.put(id, value);
				objectPropertyManager.setPropertyValue(id, value);
			}

			/*
			 * if the value was set above, then it should equal the last value
			 * place in the expected values, otherwise it will have the default
			 * value.
			 */
			for (int i = 0; i < 300; i++) {
				if (expectedValues.containsKey(i)) {
					assertEquals(expectedValues.get(i), objectPropertyManager.getPropertyValue(i));

				} else {
					assertEquals(defaultValue, (String) objectPropertyManager.getPropertyValue(i));

				}
			}

			// precondition tests
			ContractException contractException = assertThrows(ContractException.class, () -> objectPropertyManager.getPropertyValue(-1));
			assertEquals(PropertyError.NEGATIVE_INDEX, contractException.getErrorType());
		});
		TestSimulation.builder().addPlugins(factory.getPlugins()).build().execute();
	}

	private static String getRandomString(RandomGenerator randomGenerator) {
		switch (randomGenerator.nextInt(3)) {
		case 0:
			return "RED";
		case 1:
			return "YELLOW";
		default:
			return "BLUE";
		}
	}

	@Test
	@UnitTestMethod(target = ObjectPropertyManager.class, name = "setPropertyValue", args = { int.class, Object.class })
	public void testSetPropertyValue() {
		Factory factory = TestPluginFactory.factory((c) -> {
			RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(6268125375257441705L);

			String defaultValue = "YELLOW";
			PropertyDefinition propertyDefinition = PropertyDefinition.builder().setType(String.class).setDefaultValue(defaultValue).build();

			ObjectPropertyManager objectPropertyManager = new ObjectPropertyManager( propertyDefinition, this::getEmptyIndexIterator);

			/*
			 * We will set the first 300 values multiple times at random
			 */
			Map<Integer, String> expectedValues = new LinkedHashMap<>();

			for (int i = 0; i < 1000; i++) {
				int id = randomGenerator.nextInt(300);
				String value = getRandomString(randomGenerator);
				expectedValues.put(id, value);
				objectPropertyManager.setPropertyValue(id, value);
			}

			/*
			 * if the value was set above, then it should equal the last value
			 * place in the expected values, otherwise it will have the default
			 * value.
			 */
			for (int i = 0; i < 300; i++) {
				if (expectedValues.containsKey(i)) {
					assertEquals(expectedValues.get(i), objectPropertyManager.getPropertyValue(i));

				} else {
					assertEquals(defaultValue, (String) objectPropertyManager.getPropertyValue(i));

				}
			}

			// precondition tests
			ContractException contractException = assertThrows(ContractException.class, () -> objectPropertyManager.setPropertyValue(-1, "value"));
			assertEquals(PropertyError.NEGATIVE_INDEX, contractException.getErrorType());
		});
		TestSimulation.builder().addPlugins(factory.getPlugins()).build().execute();
	}

	@Test
	@UnitTestMethod(target = ObjectPropertyManager.class, name = "removeId", args = { int.class })
	public void testRemoveId() {

		Factory factory = TestPluginFactory.factory((c) -> {
			/*
			 * Should have no effect on the value that is stored for the sake of
			 * efficiency.
			 */

			// we will first test the manager with an initial value of false
			String defaultValue = "RED";
			PropertyDefinition propertyDefinition = PropertyDefinition.builder().setType(String.class).setDefaultValue(defaultValue).build();

			ObjectPropertyManager objectPropertyManager = new ObjectPropertyManager(propertyDefinition, this::getEmptyIndexIterator);

			// initially, the value should be the default value for the manager
			assertEquals(defaultValue, (String) objectPropertyManager.getPropertyValue(5));

			// after setting the value we should be able to retrieve a new value
			String newValue = "BLUE";
			objectPropertyManager.setPropertyValue(5, newValue);
			assertEquals(newValue, (String) objectPropertyManager.getPropertyValue(5));

			// removing the id from the manager should return the value to the
			// deafault
			objectPropertyManager.removeId(5);

			assertEquals(defaultValue, (String) objectPropertyManager.getPropertyValue(5));

			// we will next test the manager with an initial value of true
			propertyDefinition = PropertyDefinition.builder().setType(String.class).setDefaultValue(defaultValue).build();

			objectPropertyManager = new ObjectPropertyManager(propertyDefinition, this::getEmptyIndexIterator);

			// initially, the value should be the default value for the manager
			assertEquals(defaultValue, (String) objectPropertyManager.getPropertyValue(5));

			// after setting the value we should be able to retrieve the new
			// value
			objectPropertyManager.setPropertyValue(5, newValue);
			assertEquals(newValue, (String) objectPropertyManager.getPropertyValue(5));

			// removing the id from the manager should return the value to the
			// default
			objectPropertyManager.removeId(5);

			assertEquals(defaultValue, (String) objectPropertyManager.getPropertyValue(5));

			// precondition tests
			PropertyDefinition def = PropertyDefinition.builder().setType(Boolean.class).setDefaultValue(true).build();
			ObjectPropertyManager opm = new ObjectPropertyManager( def, this::getEmptyIndexIterator);

			ContractException contractException = assertThrows(ContractException.class, () -> opm.removeId(-1));
			assertEquals(PropertyError.NEGATIVE_INDEX, contractException.getErrorType());
		});
		TestSimulation.builder().addPlugins(factory.getPlugins()).build().execute();
	}

	@Test
	@UnitTestConstructor(target = ObjectPropertyManager.class, args = { PropertyDefinition.class, int.class })
	public void testConstructor() {
		Factory factory = TestPluginFactory.factory((c) -> {

			PropertyDefinition goodPropertyDefinition = PropertyDefinition.builder().setType(Object.class).setDefaultValue("BLUE").build();

			// if the property definition is null
			ContractException contractException = assertThrows(ContractException.class, () -> new ObjectPropertyManager(null, this::getEmptyIndexIterator));
			assertEquals(PropertyError.NULL_PROPERTY_DEFINITION, contractException.getErrorType());


			ObjectPropertyManager objectPropertyManager = new ObjectPropertyManager(goodPropertyDefinition, this::getEmptyIndexIterator);
			assertNotNull(objectPropertyManager);
		});
		TestSimulation.builder().addPlugins(factory.getPlugins()).build().execute();
	}

	@Test
	@UnitTestMethod(target = ObjectPropertyManager.class, name = "incrementCapacity", args = { int.class })
	public void testIncrementCapacity() {
		Factory factory = TestPluginFactory.factory((c) -> {

			PropertyDefinition propertyDefinition = PropertyDefinition.builder().setType(Integer.class).setDefaultValue(234).build();

			ObjectPropertyManager objectPropertyManager = new ObjectPropertyManager(propertyDefinition, this::getEmptyIndexIterator);

			// precondition tests
			ContractException contractException = assertThrows(ContractException.class, () -> objectPropertyManager.incrementCapacity(-1));
			assertEquals(PropertyError.NEGATIVE_CAPACITY_INCREMENT, contractException.getErrorType());
		});
		TestSimulation.builder().addPlugins(factory.getPlugins()).build().execute();
	}

}
