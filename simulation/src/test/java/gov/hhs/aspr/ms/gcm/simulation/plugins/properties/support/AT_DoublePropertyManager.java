package gov.hhs.aspr.ms.gcm.simulation.plugins.properties.support;

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

public class AT_DoublePropertyManager {
	
	private Iterator<Integer> getEmptyIndexIterator() {
		return Collections.emptyIterator();				
	}
	
	
	@Test
	@UnitTestMethod(target = DoublePropertyManager.class,name = "getPropertyValue", args = { int.class })
	public void testGetPropertyValue() {
		Factory factory = TestPluginFactory.factory((c) -> {
			RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(3799865640223574835L);

			double defaultValue = 423.645;
			PropertyDefinition propertyDefinition = PropertyDefinition.builder().setType(Double.class).setDefaultValue(defaultValue).build();

			DoublePropertyManager doublePropertyManager = new DoublePropertyManager(propertyDefinition,this::getEmptyIndexIterator);

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
			 * if the value was set above, then it should equal the last value
			 * place in the expected values, otherwise it will have the default
			 * value.
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
		});
		TestSimulation.builder().addPlugins(factory.getPlugins()).build().execute();
	}


	@Test
	@UnitTestMethod(target = DoublePropertyManager.class,name = "setPropertyValue", args = { int.class, Object.class })
	public void testSetPropertyValue() {
		Factory factory = TestPluginFactory.factory((c) -> {
			RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(1599837792379294459L);

			double defaultValue = 423.645;
			PropertyDefinition propertyDefinition = PropertyDefinition.builder().setType(Double.class).setDefaultValue(defaultValue).build();

			DoublePropertyManager doublePropertyManager = new DoublePropertyManager(propertyDefinition,this::getEmptyIndexIterator);

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
			 * if the value was set above, then it should equal the last value
			 * place in the expected values, otherwise it will have the default
			 * value.
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
		});
		TestSimulation.builder().addPlugins(factory.getPlugins()).build().execute();
	}

	@Test
	@UnitTestMethod(target = DoublePropertyManager.class,name = "removeId", args = { int.class })
	public void testRemoveId() {
		Factory factory = TestPluginFactory.factory((c) -> {
			/*
			 * Should have no effect on the value that is stored for the sake of
			 * efficiency.
			 */

			// we will first test the manager with an initial value of false
			double defaultValue = 6.2345345;
			PropertyDefinition propertyDefinition = PropertyDefinition.builder().setType(Double.class).setDefaultValue(defaultValue).build();

			DoublePropertyManager doublePropertyManager = new DoublePropertyManager(propertyDefinition,this::getEmptyIndexIterator);

			// initially, the value should be the default value for the manager
			assertEquals(defaultValue, (Double) doublePropertyManager.getPropertyValue(5), 0);

			// after setting the value we should be able to retrieve a new value
			double newValue = 34534.4;
			doublePropertyManager.setPropertyValue(5, newValue);
			assertEquals(newValue, (Double) doublePropertyManager.getPropertyValue(5), 0);

			// removing the id from the manager should have no effect, since we
			// do
			// not waste time setting the value back to the default
			doublePropertyManager.removeId(5);

			assertEquals(newValue, (Double) doublePropertyManager.getPropertyValue(5), 0);

			// we will next test the manager with an initial value of true
			propertyDefinition = PropertyDefinition.builder().setType(Double.class).setDefaultValue(defaultValue).build();

			doublePropertyManager = new DoublePropertyManager(propertyDefinition,this::getEmptyIndexIterator);

			// initially, the value should be the default value for the manager
			assertEquals(defaultValue, (Double) doublePropertyManager.getPropertyValue(5), 0);

			// after setting the value we should be able to retrieve the new
			// value
			doublePropertyManager.setPropertyValue(5, newValue);
			assertEquals(newValue, (Double) doublePropertyManager.getPropertyValue(5), 0);

			// removing the id from the manager should have no effect, since we
			// do
			// not waste time setting the value back to the default
			doublePropertyManager.removeId(5);

			assertEquals(newValue, (Double) doublePropertyManager.getPropertyValue(5), 0);

			// precondition tests
			ContractException contractException = assertThrows(ContractException.class, () -> {
				PropertyDefinition def = PropertyDefinition.builder().setType(Double.class).setDefaultValue(4534.4).build();
				DoublePropertyManager dpm = new DoublePropertyManager(def,this::getEmptyIndexIterator);
				dpm.removeId(-1);
			});
			assertEquals(PropertyError.NEGATIVE_INDEX, contractException.getErrorType());
		});
		TestSimulation.builder().addPlugins(factory.getPlugins()).build().execute();
	}

	@Test
	@UnitTestConstructor(target = DoublePropertyManager.class,args = {PropertyDefinition.class, Supplier.class })
	public void testConstructor() {
		Factory factory = TestPluginFactory.factory((c) -> {

			PropertyDefinition goodPropertyDefinition = PropertyDefinition.builder().setType(Double.class).setDefaultValue(2.3).build();
			PropertyDefinition badPropertyDefinition = PropertyDefinition.builder().setType(Boolean.class).setDefaultValue(false).build();

			// precondition tests

			// if the property definition is null
			ContractException contractException = assertThrows(ContractException.class, () -> new DoublePropertyManager(null,this::getEmptyIndexIterator));
			assertEquals(PropertyError.NULL_PROPERTY_DEFINITION, contractException.getErrorType());

			// if the property definition does not have a type of Double.class
			contractException = assertThrows(ContractException.class, () -> new DoublePropertyManager(badPropertyDefinition,this::getEmptyIndexIterator));
			assertEquals(PropertyError.PROPERTY_DEFINITION_IMPROPER_TYPE, contractException.getErrorType());


			DoublePropertyManager doublePropertyManager = new DoublePropertyManager(goodPropertyDefinition,this::getEmptyIndexIterator);
			assertNotNull(doublePropertyManager);
		});
		TestSimulation.builder().addPlugins(factory.getPlugins()).build().execute();
	}

	@Test
	@UnitTestMethod(target = DoublePropertyManager.class,name = "incrementCapacity", args = { int.class })
	public void testIncrementCapacity() {
		Factory factory = TestPluginFactory.factory((c) -> {

			PropertyDefinition propertyDefinition = PropertyDefinition.builder().setType(Double.class).setDefaultValue(2.42).build();

			DoublePropertyManager doublePropertyManager = new DoublePropertyManager(propertyDefinition,this::getEmptyIndexIterator);

			// precondition tests
			ContractException contractException = assertThrows(ContractException.class, () -> doublePropertyManager.incrementCapacity(-1));
			assertEquals(PropertyError.NEGATIVE_CAPACITY_INCREMENT, contractException.getErrorType());
		});
		TestSimulation.builder().addPlugins(factory.getPlugins()).build().execute();
	}
	
	
	@Test
	@UnitTestMethod(target = DoublePropertyManager.class, name = "toString", args = {})
	public void testToString() {
		Factory factory = TestPluginFactory.factory((c) -> {

			PropertyDefinition propertyDefinition = PropertyDefinition.builder().setType(Double.class)
					.setDefaultValue(0.0).build();

			List<Integer> list = new ArrayList<>();
			list.add(1);
			list.add(2);
			list.add(5);
			list.add(6);
			list.add(7);

			DoublePropertyManager doublePropertyManager = new DoublePropertyManager(propertyDefinition,
					() -> list.iterator());

			doublePropertyManager.setPropertyValue(5, 2.5);
			doublePropertyManager.setPropertyValue(7, 3.5);
			doublePropertyManager.setPropertyValue(1, 0.5);
			doublePropertyManager.setPropertyValue(8, 4.0);

			String actualValue = doublePropertyManager.toString();

			String expectedValue = "DoublePropertyManager [doubleValueContainer=DoubleValueContainer [values=[1=0.5, 2=0.0, 5=2.5, 6=0.0, 7=3.5], defaultValue=0.0]]";
			assertEquals(expectedValue, actualValue);
		});
		TestSimulation.builder().addPlugins(factory.getPlugins()).build().execute();

	}

}
