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
import nucleus.Plugin;
import nucleus.SimulationContext;
import nucleus.testsupport.testplugin.TestActionSupport;
import nucleus.testsupport.testplugin.TestActorPlan;
import nucleus.testsupport.testplugin.TestDataManager;
import nucleus.testsupport.testplugin.TestPlugin;
import nucleus.testsupport.testplugin.TestPluginData;
import tools.annotations.UnitTestConstructor;
import tools.annotations.UnitTestMethod;
import util.errors.ContractException;
import util.random.RandomGeneratorProvider;

/**
 * Common interface to all person property managers. A person property manager
 * manages all the property values for people for a particular person property
 * identifier.
 * 
 *
 */

public class AT_FloatPropertyManager {

	@Test
	@UnitTestMethod(target = FloatPropertyManager.class,name = "getPropertyValue", args = { int.class })
	public void testGetPropertyValue() {

		TestActionSupport.testConsumer((c) -> {
			RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(8486538414190886901L);

			float defaultValue = 423.645F;
			PropertyDefinition propertyDefinition = PropertyDefinition.builder().setType(Float.class).setDefaultValue(defaultValue).setTimeTrackingPolicy(TimeTrackingPolicy.TRACK_TIME).build();

			FloatPropertyManager floatPropertyManager = new FloatPropertyManager(c, propertyDefinition, 0);

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
			 * if the value was set above, then it should equal the last value
			 * place in the expected values, otherwise it will have the default
			 * value.
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
		});
	}

	/*
	 * Local data manager used to properly initialize an ObjectPropertyManager
	 * for use in time sensitive tests
	 */
	private static class LocalDM extends TestDataManager {
		public FloatPropertyManager floatPropertyManager;

		@Override
		protected void init(DataManagerContext dataManagerContext) {
			super.init(dataManagerContext);
			PropertyDefinition propertyDefinition = PropertyDefinition.builder().setType(Float.class).setDefaultValue(342.4234F).setTimeTrackingPolicy(TimeTrackingPolicy.TRACK_TIME).build();
			floatPropertyManager = new FloatPropertyManager(dataManagerContext, propertyDefinition, 0);
		}
	}

	@Test
	@UnitTestMethod(target = FloatPropertyManager.class,name = "getPropertyTime", args = { int.class })
	public void testGetPropertyTime() {

		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(6894984813418975068L);
		TestPluginData.Builder pluginDataBuilder = TestPluginData.builder();

		IntStream.range(0, 1000).forEach((i -> {
			pluginDataBuilder.addTestActorPlan("actor", new TestActorPlan(i, (c) -> {
				LocalDM localDM = c.getDataManager(LocalDM.class);
				int id = randomGenerator.nextInt(300);
				float value = randomGenerator.nextFloat();
				FloatPropertyManager floatPropertyManager = localDM.floatPropertyManager;
				floatPropertyManager.setPropertyValue(id, value);
				// show that the property time for the id was properly set
				assertEquals(c.getTime(), floatPropertyManager.getPropertyTime(id), 0);
			}));
		}));

		// add the local data manager
		pluginDataBuilder.addTestDataManager("dm", ()->new LocalDM());

		// build and run the simulation
		TestPluginData testPluginData = pluginDataBuilder.build();
		Plugin plugin = TestPlugin.getTestPlugin(testPluginData);
		TestActionSupport.testConsumers(plugin);

		// precondition test: if time tracking is not engaged
		TestActionSupport.testConsumer((c) -> {
			PropertyDefinition propertyDefinition = PropertyDefinition.builder().setType(Float.class).setDefaultValue(2.2F).build();
			FloatPropertyManager fpm = new FloatPropertyManager(c, propertyDefinition, 0);
			ContractException contractException = assertThrows(ContractException.class, () -> fpm.getPropertyTime(0));
			assertEquals(PropertyError.TIME_TRACKING_OFF, contractException.getErrorType());
		});

		// precondition test: if a property time is retrieved for a negative
		// index
		TestActionSupport.testConsumer((c) -> {
			PropertyDefinition propertyDefinition = PropertyDefinition.builder().setType(Float.class).setDefaultValue(2.2F).setTimeTrackingPolicy(TimeTrackingPolicy.TRACK_TIME).build();
			FloatPropertyManager fpm = new FloatPropertyManager(c, propertyDefinition, 0);
			ContractException contractException = assertThrows(ContractException.class, () -> fpm.getPropertyTime(-1));
			assertEquals(PropertyError.NEGATIVE_INDEX, contractException.getErrorType());
		});
	}

	@Test
	@UnitTestMethod(target = FloatPropertyManager.class,name = "setPropertyValue", args = { int.class, Object.class })
	public void testSetPropertyValue() {
		TestActionSupport.testConsumer((c) -> {
			RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(6087185710247012204L);

			float defaultValue = 423.645F;
			PropertyDefinition propertyDefinition = PropertyDefinition.builder().setType(Float.class).setDefaultValue(defaultValue).setTimeTrackingPolicy(TimeTrackingPolicy.TRACK_TIME).build();

			FloatPropertyManager floatPropertyManager = new FloatPropertyManager(c, propertyDefinition, 0);

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
			 * if the value was set above, then it should equal the last value
			 * place in the expected values, otherwise it will have the default
			 * value.
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
		});
	}

	@Test
	@UnitTestMethod(target = FloatPropertyManager.class,name = "removeId", args = { int.class })
	public void testRemoveId() {

		TestActionSupport.testConsumer((c) -> {
			/*
			 * Should have no effect on the value that is stored for the sake of
			 * efficiency.
			 */

			// we will first test the manager with an initial value of false
			float defaultValue = 6.2345345F;
			PropertyDefinition propertyDefinition = PropertyDefinition.builder().setType(Float.class).setDefaultValue(defaultValue).setTimeTrackingPolicy(TimeTrackingPolicy.TRACK_TIME).build();

			FloatPropertyManager floatPropertyManager = new FloatPropertyManager(c, propertyDefinition, 0);

			// initially, the value should be the default value for the manager
			assertEquals(defaultValue, (Float) floatPropertyManager.getPropertyValue(5), 0);

			// after setting the value we should be able to retrieve a new value
			float newValue = 34534.4F;
			floatPropertyManager.setPropertyValue(5, newValue);
			assertEquals(newValue, (Float) floatPropertyManager.getPropertyValue(5), 0);

			// removing the id from the manager should have no effect, since we
			// do
			// not waste time setting the value back to the default
			floatPropertyManager.removeId(5);

			assertEquals(newValue, (Float) floatPropertyManager.getPropertyValue(5), 0);

			// we will next test the manager with an initial value of true
			propertyDefinition = PropertyDefinition.builder().setType(Float.class).setDefaultValue(defaultValue).setTimeTrackingPolicy(TimeTrackingPolicy.TRACK_TIME).build();

			floatPropertyManager = new FloatPropertyManager(c, propertyDefinition, 0);

			// initially, the value should be the default value for the manager
			assertEquals(defaultValue, (Float) floatPropertyManager.getPropertyValue(5), 0);

			// after setting the value we should be able to retrieve the new
			// value
			floatPropertyManager.setPropertyValue(5, newValue);
			assertEquals(newValue, (Float) floatPropertyManager.getPropertyValue(5), 0);

			// removing the id from the manager should have no effect, since we
			// do
			// not waste time setting the value back to the default
			floatPropertyManager.removeId(5);

			assertEquals(newValue, (Float) floatPropertyManager.getPropertyValue(5), 0);

			// precondition tests
			PropertyDefinition def = PropertyDefinition.builder().setType(Float.class).setDefaultValue(4.5F).setTimeTrackingPolicy(TimeTrackingPolicy.TRACK_TIME).build();
			FloatPropertyManager fpm = new FloatPropertyManager(c, def, 0);

			ContractException contractException = assertThrows(ContractException.class, () -> fpm.removeId(-1));
			assertEquals(PropertyError.NEGATIVE_INDEX, contractException.getErrorType());
		});
	}

	@Test
	@UnitTestConstructor(target = FloatPropertyManager.class,args = { SimulationContext.class, PropertyDefinition.class, int.class })
	public void testConstructor() {
		TestActionSupport.testConsumer((c) -> {

			PropertyDefinition goodPropertyDefinition = PropertyDefinition.builder().setType(Float.class).setDefaultValue(2.3F).build();
			PropertyDefinition badPropertyDefinition = PropertyDefinition.builder().setType(Boolean.class).setDefaultValue(false).build();

			// if the property definition is null
			ContractException contractException = assertThrows(ContractException.class, () -> new FloatPropertyManager(c, null, 0));
			assertEquals(PropertyError.NULL_PROPERTY_DEFINITION, contractException.getErrorType());

			// if the property definition does not have a type of Float.class
			contractException = assertThrows(ContractException.class, () -> new FloatPropertyManager(c, badPropertyDefinition, 0));
			assertEquals(PropertyError.PROPERTY_DEFINITION_IMPROPER_TYPE, contractException.getErrorType());

			// if the initial size is negative
			contractException = assertThrows(ContractException.class, () -> new FloatPropertyManager(c, goodPropertyDefinition, -1));
			assertEquals(PropertyError.NEGATIVE_INITIAL_SIZE, contractException.getErrorType());

			FloatPropertyManager doublePropertyManager = new FloatPropertyManager(c, goodPropertyDefinition, 0);
			assertNotNull(doublePropertyManager);
		});
	}

	@Test
	@UnitTestMethod(target = FloatPropertyManager.class,name = "incrementCapacity", args = { int.class })
	public void testIncrementCapacity() {
		TestActionSupport.testConsumer((c) -> {

			PropertyDefinition propertyDefinition = PropertyDefinition.builder().setType(Float.class).setDefaultValue(234.42F).setTimeTrackingPolicy(TimeTrackingPolicy.TRACK_TIME).build();

			FloatPropertyManager floatPropertyManager = new FloatPropertyManager(c, propertyDefinition, 0);

			// precondition tests
			ContractException contractException = assertThrows(ContractException.class, () -> floatPropertyManager.incrementCapacity(-1));
			assertEquals(PropertyError.NEGATIVE_CAPACITY_INCREMENT, contractException.getErrorType());
		});
	}

}
