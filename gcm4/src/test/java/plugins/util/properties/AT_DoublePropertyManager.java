package plugins.util.properties;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.IntStream;

import org.apache.commons.math3.random.RandomGenerator;
import org.junit.jupiter.api.Test;

import nucleus.DataManagerContext;
import nucleus.SimulationContext;
import nucleus.testsupport.testplugin.TestActorPlan;
import nucleus.testsupport.testplugin.TestDataManager;
import nucleus.testsupport.testplugin.TestPluginData;
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

public class AT_DoublePropertyManager {

	@Test
	@UnitTestMethod(target = DoublePropertyManager.class,name = "getPropertyValue", args = { int.class })
	public void testGetPropertyValue() {
		Factory factory = TestPluginFactory.factory((c) -> {
			RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(3799865640223574835L);

			double defaultValue = 423.645;
			PropertyDefinition propertyDefinition = PropertyDefinition.builder().setType(Double.class).setDefaultValue(defaultValue).setTimeTrackingPolicy(TimeTrackingPolicy.TRACK_TIME).build();

			DoublePropertyManager doublePropertyManager = new DoublePropertyManager(c, propertyDefinition, 0);

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

	/*
	 * Local data manager used to properly initialize an ObjectPropertyManager
	 * for use in time sensitive tests
	 */
	private static class LocalDM extends TestDataManager {
		protected DoublePropertyManager doublePropertyManager;

		@Override
		public void init(DataManagerContext dataManagerContext) {
			super.init(dataManagerContext);
			PropertyDefinition propertyDefinition = PropertyDefinition.builder().setType(Double.class).setDefaultValue(342.4234).setTimeTrackingPolicy(TimeTrackingPolicy.TRACK_TIME).build();
			doublePropertyManager = new DoublePropertyManager(dataManagerContext, propertyDefinition, 0);
		}
	}

	@Test
	@UnitTestMethod(target = DoublePropertyManager.class,name = "getPropertyTime", args = { int.class })
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
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(2349682401845769564L);

		TestPluginData.Builder pluginDataBuilder = TestPluginData.builder();

		IntStream.range(0, 1000).forEach((i -> {
			pluginDataBuilder.addTestActorPlan("actor", new TestActorPlan(i, (c) -> {
				LocalDM localDM = c.getDataManager(LocalDM.class);
				int id = randomGenerator.nextInt(300);
				double value = randomGenerator.nextDouble();
				DoublePropertyManager doublePropertyManager = localDM.doublePropertyManager;
				doublePropertyManager.setPropertyValue(id, value);
				// show that the property time for the id was properly set
				assertEquals(c.getTime(), doublePropertyManager.getPropertyTime(id), 0);
			}));
		}));

		// add the local data manager
		pluginDataBuilder.addTestDataManager("dm", ()->new LocalDM());

		// build and run the simulation
		TestPluginData testPluginData = pluginDataBuilder.build();
		Factory factory = TestPluginFactory.factory(testPluginData);
		TestSimulation.builder().addPlugins(factory.getPlugins()).build().execute();
		// precondition tests:
		ContractException contractException = assertThrows(ContractException.class, () ->{
			Factory factory2 = TestPluginFactory.factory((c) -> {
				PropertyDefinition propertyDefinition = PropertyDefinition.builder().setType(Double.class).setDefaultValue(4.5).build();
				DoublePropertyManager dpm = new DoublePropertyManager(c, propertyDefinition, 0);
				dpm.getPropertyTime(0);			
			});
			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();
		});
		assertEquals(PropertyError.TIME_TRACKING_OFF, contractException.getErrorType());
		
		contractException = assertThrows(ContractException.class, () ->{
			Factory factory2 = TestPluginFactory.factory((c) -> {
				PropertyDefinition propertyDefinition = PropertyDefinition.builder().setType(Double.class).setDefaultValue(4.5).setTimeTrackingPolicy(TimeTrackingPolicy.TRACK_TIME).build();
				DoublePropertyManager dpm = new DoublePropertyManager(c, propertyDefinition, 0);
				dpm.getPropertyTime(-1);			
			});
			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();
		});
		assertEquals(PropertyError.NEGATIVE_INDEX, contractException.getErrorType());
	}

	@Test
	@UnitTestMethod(target = DoublePropertyManager.class,name = "setPropertyValue", args = { int.class, Object.class })
	public void testSetPropertyValue() {
		Factory factory = TestPluginFactory.factory((c) -> {
			RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(1599837792379294459L);

			double defaultValue = 423.645;
			PropertyDefinition propertyDefinition = PropertyDefinition.builder().setType(Double.class).setDefaultValue(defaultValue).setTimeTrackingPolicy(TimeTrackingPolicy.TRACK_TIME).build();

			DoublePropertyManager doublePropertyManager = new DoublePropertyManager(c, propertyDefinition, 0);

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
			PropertyDefinition propertyDefinition = PropertyDefinition.builder().setType(Double.class).setDefaultValue(defaultValue).setTimeTrackingPolicy(TimeTrackingPolicy.TRACK_TIME).build();

			DoublePropertyManager doublePropertyManager = new DoublePropertyManager(c, propertyDefinition, 0);

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
			propertyDefinition = PropertyDefinition.builder().setType(Double.class).setDefaultValue(defaultValue).setTimeTrackingPolicy(TimeTrackingPolicy.TRACK_TIME).build();

			doublePropertyManager = new DoublePropertyManager(c, propertyDefinition, 0);

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
				PropertyDefinition def = PropertyDefinition.builder().setType(Double.class).setDefaultValue(4534.4).setTimeTrackingPolicy(TimeTrackingPolicy.TRACK_TIME).build();
				DoublePropertyManager dpm = new DoublePropertyManager(c, def, 0);
				dpm.removeId(-1);
			});
			assertEquals(PropertyError.NEGATIVE_INDEX, contractException.getErrorType());
		});
		TestSimulation.builder().addPlugins(factory.getPlugins()).build().execute();
	}

	@Test
	@UnitTestConstructor(target = DoublePropertyManager.class,args = { SimulationContext.class, PropertyDefinition.class, int.class })
	public void testConstructor() {
		Factory factory = TestPluginFactory.factory((c) -> {

			PropertyDefinition goodPropertyDefinition = PropertyDefinition.builder().setType(Double.class).setDefaultValue(2.3).build();
			PropertyDefinition badPropertyDefinition = PropertyDefinition.builder().setType(Boolean.class).setDefaultValue(false).build();

			// precondition tests

			// if the property definition is null
			ContractException contractException = assertThrows(ContractException.class, () -> new DoublePropertyManager(c, null, 0));
			assertEquals(PropertyError.NULL_PROPERTY_DEFINITION, contractException.getErrorType());

			// if the property definition does not have a type of Double.class
			contractException = assertThrows(ContractException.class, () -> new DoublePropertyManager(c, badPropertyDefinition, 0));
			assertEquals(PropertyError.PROPERTY_DEFINITION_IMPROPER_TYPE, contractException.getErrorType());

			// if the initial size is negative
			contractException = assertThrows(ContractException.class, () -> new DoublePropertyManager(c, goodPropertyDefinition, -1));
			assertEquals(PropertyError.NEGATIVE_INITIAL_SIZE, contractException.getErrorType());

			DoublePropertyManager doublePropertyManager = new DoublePropertyManager(c, goodPropertyDefinition, 0);
			assertNotNull(doublePropertyManager);
		});
		TestSimulation.builder().addPlugins(factory.getPlugins()).build().execute();
	}

	@Test
	@UnitTestMethod(target = DoublePropertyManager.class,name = "incrementCapacity", args = { int.class })
	public void testIncrementCapacity() {
		Factory factory = TestPluginFactory.factory((c) -> {

			PropertyDefinition propertyDefinition = PropertyDefinition.builder().setType(Double.class).setDefaultValue(2.42).setTimeTrackingPolicy(TimeTrackingPolicy.TRACK_TIME).build();

			DoublePropertyManager doublePropertyManager = new DoublePropertyManager(c, propertyDefinition, 0);

			// precondition tests
			ContractException contractException = assertThrows(ContractException.class, () -> doublePropertyManager.incrementCapacity(-1));
			assertEquals(PropertyError.NEGATIVE_CAPACITY_INCREMENT, contractException.getErrorType());
		});
		TestSimulation.builder().addPlugins(factory.getPlugins()).build().execute();
	}

}
