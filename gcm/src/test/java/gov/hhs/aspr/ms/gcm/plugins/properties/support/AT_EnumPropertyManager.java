package gov.hhs.aspr.ms.gcm.plugins.properties.support;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import org.apache.commons.math3.random.RandomGenerator;
import org.junit.jupiter.api.Test;

import gov.hhs.aspr.ms.gcm.nucleus.testsupport.testplugin.TestPluginFactory;
import gov.hhs.aspr.ms.gcm.nucleus.testsupport.testplugin.TestPluginFactory.Factory;
import gov.hhs.aspr.ms.gcm.nucleus.testsupport.testplugin.TestSimulation;
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

	private Iterator<Integer> getEmptyIndexIterator() {
		return Collections.emptyIterator();				
	}
	
	@Test
	@UnitTestMethod(target = EnumPropertyManager.class, name = "getPropertyValue", args = { int.class })
	public void testGetPropertyValue() {
		Factory factory = TestPluginFactory.factory((c) -> {
			RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(5102684240650614254L);

			Color defaultValue = Color.YELLOW;
			PropertyDefinition propertyDefinition = PropertyDefinition.builder().setType(Color.class).setDefaultValue(defaultValue).build();

			EnumPropertyManager enumPropertyManager = new EnumPropertyManager(propertyDefinition, this::getEmptyIndexIterator);

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
			PropertyDefinition propertyDefinition = PropertyDefinition.builder().setType(Color.class).setDefaultValue(defaultValue).build();

			EnumPropertyManager enumPropertyManager = new EnumPropertyManager(propertyDefinition, this::getEmptyIndexIterator);

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
			PropertyDefinition propertyDefinition = PropertyDefinition.builder().setType(Color.class).setDefaultValue(defaultValue).build();

			EnumPropertyManager enumPropertyManager = new EnumPropertyManager(propertyDefinition, this::getEmptyIndexIterator);

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
			propertyDefinition = PropertyDefinition.builder().setType(Color.class).setDefaultValue(defaultValue).build();

			enumPropertyManager = new EnumPropertyManager(propertyDefinition, this::getEmptyIndexIterator);

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
			PropertyDefinition def = PropertyDefinition.builder().setType(Color.class).setDefaultValue(Color.YELLOW).build();
			EnumPropertyManager epm = new EnumPropertyManager(def, this::getEmptyIndexIterator);

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
	@UnitTestConstructor(target = EnumPropertyManager.class, args = {PropertyDefinition.class, Supplier.class })
	public void testConstructor() {
		Factory factory = TestPluginFactory.factory((c) -> {
			PropertyDefinition goodPropertyDefinition = PropertyDefinition.builder().setType(Color.class).setDefaultValue(Color.BLUE).build();
			PropertyDefinition badPropertyDefinition = PropertyDefinition.builder().setType(Boolean.class).setDefaultValue(false).build();
			
			EnumPropertyManager enumPropertyManager = new EnumPropertyManager(goodPropertyDefinition, this::getEmptyIndexIterator);
			assertNotNull(enumPropertyManager);
			
			// precondition test: if the property definition is null
			ContractException contractException = assertThrows(ContractException.class, () -> new EnumPropertyManager(null, this::getEmptyIndexIterator));
			assertEquals(PropertyError.NULL_PROPERTY_DEFINITION, contractException.getErrorType());

			// precondition test: if the property definition does not have a type of Enum.class
			contractException = assertThrows(ContractException.class, () -> new EnumPropertyManager(badPropertyDefinition, this::getEmptyIndexIterator));
			assertEquals(PropertyError.PROPERTY_DEFINITION_IMPROPER_TYPE, contractException.getErrorType());


		});
		TestSimulation.builder().addPlugins(factory.getPlugins()).build().execute();
	}

	@Test
	@UnitTestMethod(target = EnumPropertyManager.class, name = "incrementCapacity", args = { int.class })
	public void testIncrementCapacity() {
		Factory factory = TestPluginFactory.factory((c) -> {

			PropertyDefinition propertyDefinition = PropertyDefinition.builder().setType(Color.class).setDefaultValue(Color.RED).build();

			EnumPropertyManager enumPropertyManager = new EnumPropertyManager(propertyDefinition, this::getEmptyIndexIterator);

			// precondition tests
			ContractException contractException = assertThrows(ContractException.class, () -> enumPropertyManager.incrementCapacity(-1));
			assertEquals(PropertyError.NEGATIVE_CAPACITY_INCREMENT, contractException.getErrorType());
		});
		TestSimulation.builder().addPlugins(factory.getPlugins()).build().execute();
	}
	
	@Test
	@UnitTestMethod(target = EnumPropertyManager.class, name = "toString", args = {})
	public void testToString() {
		Factory factory = TestPluginFactory.factory((c) -> {

			PropertyDefinition propertyDefinition = PropertyDefinition.builder().setType(Color.class)
					.setDefaultValue(Color.BLUE).build();

			List<Integer> list = new ArrayList<>();
			list.add(1);
			list.add(2);
			list.add(5);
			list.add(6);
			list.add(7);

			EnumPropertyManager enumPropertyManager = new EnumPropertyManager(propertyDefinition,
					() -> list.iterator());

			enumPropertyManager.setPropertyValue(5, Color.RED);
			enumPropertyManager.setPropertyValue(7, Color.YELLOW);
			enumPropertyManager.setPropertyValue(1, Color.RED);
			enumPropertyManager.setPropertyValue(8, Color.BLUE);

			String actualValue = enumPropertyManager.toString();
			
			String expectedValue = "EnumPropertyManager [enumContainer=EnumContainer [values=[1=RED, 2=BLUE, 5=RED, 6=BLUE, 7=YELLOW], enumClass=class gov.hhs.aspr.ms.gcm.plugins.properties.support.AT_EnumPropertyManager$Color]]";

			assertEquals(expectedValue, actualValue);
		});
		TestSimulation.builder().addPlugins(factory.getPlugins()).build().execute();

	}

}
