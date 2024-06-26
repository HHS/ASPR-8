package gov.hhs.aspr.ms.gcm.simulation.plugins.properties.support;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import org.apache.commons.math3.random.RandomGenerator;
import org.junit.jupiter.api.Test;

import gov.hhs.aspr.ms.gcm.simulation.nucleus.testsupport.testplugin.TestPluginFactory;
import gov.hhs.aspr.ms.gcm.simulation.nucleus.testsupport.testplugin.TestPluginFactory.Factory;
import gov.hhs.aspr.ms.gcm.simulation.nucleus.testsupport.testplugin.TestSimulation;
import gov.hhs.aspr.ms.util.annotations.UnitTestConstructor;
import gov.hhs.aspr.ms.util.annotations.UnitTestMethod;
import gov.hhs.aspr.ms.util.errors.ContractException;
import gov.hhs.aspr.ms.util.random.RandomGeneratorProvider;

/**
 * Common interface to all person property managers. A person property manager
 * manages all the property values for people for a particular person property
 * identifier.
 * 
 *
 */

public class AT_BooleanPropertyManager {

	private Iterator<Integer> getEmptyIndexIterator() {
		return Collections.emptyIterator();
	}

	@Test
	@UnitTestMethod(target = BooleanPropertyManager.class, name = "getPropertyValue", args = { int.class })
	public void testGetPropertyValue() {
		Factory factory = TestPluginFactory.factory((c) -> {
			RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(4879223247393954289L);

			PropertyDefinition propertyDefinition = PropertyDefinition.builder().setType(Boolean.class)
					.setDefaultValue(false).build();

			BooleanPropertyManager booleanPropertyManager = new BooleanPropertyManager(propertyDefinition,
					this::getEmptyIndexIterator);

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
			 * if the value was set above, then it should equal the last value place in the
			 * expected values, otherwise it will have the default value.
			 */
			for (int i = 0; i < 300; i++) {
				if (expectedValues.containsKey(i)) {
					assertEquals(expectedValues.get(i), booleanPropertyManager.getPropertyValue(i));

				} else {
					assertFalse((Boolean) booleanPropertyManager.getPropertyValue(i));

				}
			}

			// precondition tests
			ContractException contractException = assertThrows(ContractException.class,
					() -> booleanPropertyManager.getPropertyValue(-1));
			assertEquals(PropertyError.NEGATIVE_INDEX, contractException.getErrorType());
		});
		TestSimulation.builder().addPlugins(factory.getPlugins()).build().execute();

	}

	@Test
	@UnitTestMethod(target = BooleanPropertyManager.class, name = "setPropertyValue", args = { int.class,
			Object.class })
	public void testSetPropertyValue() {
		Factory factory = TestPluginFactory.factory((c) -> {
			RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(4827517950755837724L);

			PropertyDefinition propertyDefinition = PropertyDefinition.builder().setType(Boolean.class)
					.setDefaultValue(false).build();

			BooleanPropertyManager booleanPropertyManager = new BooleanPropertyManager(propertyDefinition,
					this::getEmptyIndexIterator);

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
			 * if the value was set above, then it should equal the last value place in the
			 * expected values, otherwise it will have the default value.
			 */
			for (int i = 0; i < 300; i++) {
				if (expectedValues.containsKey(i)) {
					assertEquals(expectedValues.get(i), booleanPropertyManager.getPropertyValue(i));

				} else {
					assertFalse((Boolean) booleanPropertyManager.getPropertyValue(i));

				}
			}

			// precondition tests
			ContractException contractException = assertThrows(ContractException.class,
					() -> booleanPropertyManager.setPropertyValue(-1, false));
			assertEquals(PropertyError.NEGATIVE_INDEX, contractException.getErrorType());
		});
		TestSimulation.builder().addPlugins(factory.getPlugins()).build().execute();
	}

	@Test
	@UnitTestMethod(target = BooleanPropertyManager.class, name = "removeId", args = { int.class })
	public void testRemoveId() {
		Factory factory = TestPluginFactory.factory((c) -> {

			// we will first test the manager with an initial value of false
			PropertyDefinition propertyDefinition = PropertyDefinition.builder().setType(Boolean.class)
					.setDefaultValue(false).build();

			BooleanPropertyManager booleanPropertyManager = new BooleanPropertyManager(propertyDefinition,
					this::getEmptyIndexIterator);

			// initially, the value should be the default value for the manager
			assertFalse((Boolean) booleanPropertyManager.getPropertyValue(5));

			// after setting the value we should be able to retrieve a true
			// value
			booleanPropertyManager.setPropertyValue(5, true);
			assertTrue((Boolean) booleanPropertyManager.getPropertyValue(5));

			// removing the id from the manager should have no effect, since we
			// do
			// not waste time setting the value back to the default
			booleanPropertyManager.removeId(5);

			assertTrue((Boolean) booleanPropertyManager.getPropertyValue(5));

			// we will next test the manager with an initial value of true
			propertyDefinition = PropertyDefinition.builder().setType(Boolean.class).setDefaultValue(true).build();

			booleanPropertyManager = new BooleanPropertyManager(propertyDefinition, this::getEmptyIndexIterator);

			// initially, the value should be the default value for the manager
			assertTrue((Boolean) booleanPropertyManager.getPropertyValue(5));

			// after setting the value we should be able to retrieve a true
			// value
			booleanPropertyManager.setPropertyValue(5, false);
			assertFalse((Boolean) booleanPropertyManager.getPropertyValue(5));

			// removing the id from the manager should have no effect, since we
			// do
			// not waste time setting the value back to the default
			booleanPropertyManager.removeId(5);

			assertFalse((Boolean) booleanPropertyManager.getPropertyValue(5));

			// precondition tests
			PropertyDefinition def = PropertyDefinition.builder().setType(Boolean.class).setDefaultValue(true).build();
			BooleanPropertyManager bpm = new BooleanPropertyManager(def, this::getEmptyIndexIterator);

			ContractException contractException = assertThrows(ContractException.class, () -> bpm.removeId(-1));
			assertEquals(PropertyError.NEGATIVE_INDEX, contractException.getErrorType());
		});
		TestSimulation.builder().addPlugins(factory.getPlugins()).build().execute();
	}

	@Test
	@UnitTestConstructor(target = BooleanPropertyManager.class, args = { PropertyDefinition.class, Supplier.class })
	public void testConstructor() {
		Factory factory = TestPluginFactory.factory((c) -> {

			PropertyDefinition goodPropertyDefinition = PropertyDefinition.builder().setType(Boolean.class)
					.setDefaultValue(false).build();
			PropertyDefinition badPropertyDefinition = PropertyDefinition.builder().setType(Double.class)
					.setDefaultValue(2.3).build();

			// precondition tests

			// if the property definition is null
			ContractException contractException = assertThrows(ContractException.class,
					() -> new BooleanPropertyManager(null, this::getEmptyIndexIterator));
			assertEquals(PropertyError.NULL_PROPERTY_DEFINITION, contractException.getErrorType());

			// if the property definition does not have a type of Boolean.class
			contractException = assertThrows(ContractException.class,
					() -> new BooleanPropertyManager(badPropertyDefinition, this::getEmptyIndexIterator));
			assertEquals(PropertyError.PROPERTY_DEFINITION_IMPROPER_TYPE, contractException.getErrorType());

			BooleanPropertyManager booleanPropertyManager = new BooleanPropertyManager(goodPropertyDefinition,
					this::getEmptyIndexIterator);
			assertNotNull(booleanPropertyManager);
		});
		TestSimulation.builder().addPlugins(factory.getPlugins()).build().execute();
	}

	@Test
	@UnitTestMethod(target = BooleanPropertyManager.class, name = "incrementCapacity", args = { int.class })
	public void testIncrementCapacity() {
		Factory factory = TestPluginFactory.factory((c) -> {

			PropertyDefinition propertyDefinition = PropertyDefinition.builder().setType(Boolean.class)
					.setDefaultValue(false).build();

			BooleanPropertyManager booleanPropertyManager = new BooleanPropertyManager(propertyDefinition,
					this::getEmptyIndexIterator);

			// precondition tests
			ContractException contractException = assertThrows(ContractException.class,
					() -> booleanPropertyManager.incrementCapacity(-1));
			assertEquals(PropertyError.NEGATIVE_CAPACITY_INCREMENT, contractException.getErrorType());
		});
		TestSimulation.builder().addPlugins(factory.getPlugins()).build().execute();
	}

	@Test
	@UnitTestMethod(target = BooleanPropertyManager.class, name = "toString", args = {})
	public void testToString() {
		Factory factory = TestPluginFactory.factory((c) -> {

			PropertyDefinition propertyDefinition = PropertyDefinition.builder().setType(Boolean.class)
					.setDefaultValue(false).build();

			List<Integer> list = new ArrayList<>();
			list.add(1);
			list.add(2);
			list.add(5);
			list.add(6);
			list.add(7);

			BooleanPropertyManager booleanPropertyManager = new BooleanPropertyManager(propertyDefinition,
					() -> list.iterator());

			booleanPropertyManager.setPropertyValue(5, true);
			booleanPropertyManager.setPropertyValue(7, true);
			booleanPropertyManager.setPropertyValue(1, true);
			booleanPropertyManager.setPropertyValue(8, true);

			String actualValue = booleanPropertyManager.toString();

			String expectedValue = "BooleanPropertyManager [boolContainer=BooleanContainer [defaultValue=false, bitSet=[1=true, 2=false, 5=true, 6=false, 7=true]]]";
			assertEquals(expectedValue, actualValue);
		});
		TestSimulation.builder().addPlugins(factory.getPlugins()).build().execute();

	}

}
