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

import gov.hhs.aspr.ms.gcm.simulation.nucleus.DataManagerContext;
import gov.hhs.aspr.ms.gcm.simulation.nucleus.testsupport.testplugin.TestDataManager;
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
public class AT_IntPropertyManager {

	private Iterator<Integer> getEmptyIndexIterator() {
		return Collections.emptyIterator();
	}

	@Test
	@UnitTestMethod(target = IntPropertyManager.class, name = "getPropertyValue", args = { int.class })
	public void testGetPropertyValue() {
		Factory factory = TestPluginFactory.factory((c) -> {
			RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(7951361060252638380L);

			int defaultValue = 423;
			PropertyDefinition propertyDefinition = PropertyDefinition.builder().setType(Integer.class)
					.setDefaultValue(defaultValue).build();

			IntPropertyManager intPropertyManager = new IntPropertyManager(propertyDefinition,
					this::getEmptyIndexIterator);

			/*
			 * We will set the first 300 values multiple times at random
			 */
			Map<Integer, Integer> expectedValues = new LinkedHashMap<>();

			for (int i = 0; i < 1000; i++) {
				int id = randomGenerator.nextInt(300);
				int value = randomGenerator.nextInt();
				expectedValues.put(id, value);
				intPropertyManager.setPropertyValue(id, value);
			}

			/*
			 * if the value was set above, then it should equal the last value place in the
			 * expected values, otherwise it will have the default value.
			 */
			for (int i = 0; i < 300; i++) {
				if (expectedValues.containsKey(i)) {
					assertEquals(expectedValues.get(i), intPropertyManager.getPropertyValue(i));

				} else {
					assertEquals(defaultValue, ((Integer) intPropertyManager.getPropertyValue(i)).intValue());
				}
			}

			// precondition tests
			ContractException contractException = assertThrows(ContractException.class,
					() -> intPropertyManager.getPropertyValue(-1));
			assertEquals(PropertyError.NEGATIVE_INDEX, contractException.getErrorType());
		});
		TestSimulation.builder().addPlugins(factory.getPlugins()).build().execute();
	}

	/*
	 * Local data manager used to properly initialize an ObjectPropertyManager for
	 * use in time sensitive tests
	 */
	public static class LocalDM extends TestDataManager {
		public IntPropertyManager intPropertyManager;

		@Override
		public void init(DataManagerContext dataManagerContext) {
			super.init(dataManagerContext);
			PropertyDefinition propertyDefinition = PropertyDefinition.builder().setType(Integer.class)
					.setDefaultValue(342).build();
			
			Supplier<Iterator<Integer>> supplier = ()->Collections.emptyIterator(); 
			
			intPropertyManager = new IntPropertyManager(propertyDefinition, supplier);
		}
	}

	@Test
	@UnitTestMethod(target = IntPropertyManager.class, name = "setPropertyValue", args = { int.class, Object.class })
	public void testSetPropertyValue() {
		Factory factory = TestPluginFactory.factory((c) -> {
			RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(5297426971018191882L);

			int defaultValue = 423;
			PropertyDefinition propertyDefinition = PropertyDefinition.builder().setType(Integer.class)
					.setDefaultValue(defaultValue).build();

			IntPropertyManager intPropertyManager = new IntPropertyManager(propertyDefinition,
					this::getEmptyIndexIterator);

			/*
			 * We will set the first 300 values multiple times at random
			 */
			Map<Integer, Integer> expectedValues = new LinkedHashMap<>();

			for (int i = 0; i < 1000; i++) {
				int id = randomGenerator.nextInt(300);
				int value = randomGenerator.nextInt();
				expectedValues.put(id, value);
				intPropertyManager.setPropertyValue(id, value);
			}

			/*
			 * if the value was set above, then it should equal the last value place in the
			 * expected values, otherwise it will have the default value.
			 */
			for (int i = 0; i < 300; i++) {
				if (expectedValues.containsKey(i)) {
					assertEquals(expectedValues.get(i), intPropertyManager.getPropertyValue(i));

				} else {
					assertEquals(defaultValue, ((Integer) intPropertyManager.getPropertyValue(i)).intValue());
				}
			}

			// precondition tests
			ContractException contractException = assertThrows(ContractException.class,
					() -> intPropertyManager.setPropertyValue(-1, 23));
			assertEquals(PropertyError.NEGATIVE_INDEX, contractException.getErrorType());
		});
		TestSimulation.builder().addPlugins(factory.getPlugins()).build().execute();
	}

	@Test
	@UnitTestMethod(target = IntPropertyManager.class, name = "removeId", args = { int.class })
	public void testRemoveId() {
		Factory factory = TestPluginFactory.factory((c) -> {
			/*
			 * Should have no effect on the value that is stored for the sake of efficiency.
			 */

			// we will first test the manager with an initial value of 6
			int defaultValue = 6;
			PropertyDefinition propertyDefinition = PropertyDefinition.builder().setType(Integer.class)
					.setDefaultValue(defaultValue).build();

			IntPropertyManager intPropertyManager = new IntPropertyManager(propertyDefinition,
					this::getEmptyIndexIterator);

			// initially, the value should be the default value for the manager
			assertEquals(defaultValue, ((Integer) intPropertyManager.getPropertyValue(5)).intValue());

			// after setting the value we should be able to retrieve a new value
			int newValue = 34534;
			intPropertyManager.setPropertyValue(5, newValue);
			assertEquals(newValue, ((Integer) intPropertyManager.getPropertyValue(5)).intValue());

			// removing the id from the manager should have no effect, since we
			// do
			// not waste time setting the value back to the default
			intPropertyManager.removeId(5);

			assertEquals(newValue, ((Integer) intPropertyManager.getPropertyValue(5)).intValue());

			// we will next test the manager with an initial value of true
			propertyDefinition = PropertyDefinition.builder().setType(Integer.class).setDefaultValue(defaultValue)
					.build();

			intPropertyManager = new IntPropertyManager(propertyDefinition, this::getEmptyIndexIterator);

			// initially, the value should be the default value for the manager
			assertEquals(defaultValue, ((Integer) intPropertyManager.getPropertyValue(5)).intValue());

			// after setting the value we should be able to retrieve the new
			// value
			intPropertyManager.setPropertyValue(5, newValue);
			assertEquals(newValue, ((Integer) intPropertyManager.getPropertyValue(5)).intValue(), 0);

			// removing the id from the manager should have no effect, since we
			// do
			// not waste time setting the value back to the default
			intPropertyManager.removeId(5);

			assertEquals(newValue, ((Integer) intPropertyManager.getPropertyValue(5)).intValue(), 0);

			// precondition tests
			PropertyDefinition def = PropertyDefinition.builder().setType(Integer.class).setDefaultValue(3).build();
			IntPropertyManager ipm = new IntPropertyManager(def, this::getEmptyIndexIterator);

			ContractException contractException = assertThrows(ContractException.class, () -> ipm.removeId(-1));
			assertEquals(PropertyError.NEGATIVE_INDEX, contractException.getErrorType());
		});
		TestSimulation.builder().addPlugins(factory.getPlugins()).build().execute();
	}

	@Test
	@UnitTestConstructor(target = IntPropertyManager.class, args = { PropertyDefinition.class, Supplier.class })
	public void testConstructor() {
		Factory factory = TestPluginFactory.factory((c) -> {

			PropertyDefinition goodPropertyDefinition = PropertyDefinition.builder().setType(Integer.class)
					.setDefaultValue(2).build();
			PropertyDefinition badPropertyDefinition = PropertyDefinition.builder().setType(Boolean.class)
					.setDefaultValue(false).build();

			// if the property definition is null
			ContractException contractException = assertThrows(ContractException.class,
					() -> new IntPropertyManager(null, this::getEmptyIndexIterator));
			assertEquals(PropertyError.NULL_PROPERTY_DEFINITION, contractException.getErrorType());

			// if the property definition does not have a type of Double.class
			contractException = assertThrows(ContractException.class,
					() -> new IntPropertyManager(badPropertyDefinition, this::getEmptyIndexIterator));
			assertEquals(PropertyError.PROPERTY_DEFINITION_IMPROPER_TYPE, contractException.getErrorType());

			IntPropertyManager doublePropertyManager = new IntPropertyManager(goodPropertyDefinition,
					this::getEmptyIndexIterator);
			assertNotNull(doublePropertyManager);
		});
		TestSimulation.builder().addPlugins(factory.getPlugins()).build().execute();
	}

	@Test
	@UnitTestMethod(target = IntPropertyManager.class, name = "incrementCapacity", args = { int.class })
	public void testIncrementCapacity() {
		Factory factory = TestPluginFactory.factory((c) -> {

			PropertyDefinition propertyDefinition = PropertyDefinition.builder().setType(Integer.class)
					.setDefaultValue(234).build();

			IntPropertyManager intPropertyManager = new IntPropertyManager(propertyDefinition,
					this::getEmptyIndexIterator);

			// precondition tests
			ContractException contractException = assertThrows(ContractException.class,
					() -> intPropertyManager.incrementCapacity(-1));
			assertEquals(PropertyError.NEGATIVE_CAPACITY_INCREMENT, contractException.getErrorType());
		});
		TestSimulation.builder().addPlugins(factory.getPlugins()).build().execute();
	}
	
	
	@Test
	@UnitTestMethod(target = IntPropertyManager.class, name = "toString", args = {})
	public void testToString() {
		Factory factory = TestPluginFactory.factory((c) -> {

			PropertyDefinition propertyDefinition = PropertyDefinition.builder().setType(Integer.class)
					.setDefaultValue(0).build();

			List<Integer> list = new ArrayList<>();
			list.add(1);
			list.add(2);
			list.add(5);
			list.add(6);
			list.add(7);

			IntPropertyManager intPropertyManager = new IntPropertyManager(propertyDefinition,
					() -> list.iterator());

			intPropertyManager.setPropertyValue(5, 2);
			intPropertyManager.setPropertyValue(7, 3);
			intPropertyManager.setPropertyValue(1, 0);
			intPropertyManager.setPropertyValue(8, 4);

			String actualValue = intPropertyManager.toString();
			String expectedValue = "IntPropertyManager [intValueContainer=IntValueContainer [subTypeArray=ByteArray [values=[1=0, 2=0, 5=2, 6=0, 7=3], defaultValue=0]], intValueType=INT]";

			assertEquals(expectedValue, actualValue);
		});
		TestSimulation.builder().addPlugins(factory.getPlugins()).build().execute();

	}

}
