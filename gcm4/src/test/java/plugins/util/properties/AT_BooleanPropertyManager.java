package plugins.util.properties;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

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

public class AT_BooleanPropertyManager {

	@Test
	@UnitTestMethod(target = BooleanPropertyManager.class, name = "getPropertyValue", args = { int.class })
	public void testGetPropertyValue() {
		TestActionSupport.testConsumer((c) -> {
			RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(4879223247393954289L);

			PropertyDefinition propertyDefinition = PropertyDefinition.builder().setType(Boolean.class).setDefaultValue(false).setTimeTrackingPolicy(TimeTrackingPolicy.TRACK_TIME).build();

			BooleanPropertyManager booleanPropertyManager = new BooleanPropertyManager(c, propertyDefinition, 0);

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
			 * if the value was set above, then it should equal the last value
			 * place in the expected values, otherwise it will have the default
			 * value.
			 */
			for (int i = 0; i < 300; i++) {
				if (expectedValues.containsKey(i)) {
					assertEquals(expectedValues.get(i), booleanPropertyManager.getPropertyValue(i));

				} else {
					assertFalse((Boolean) booleanPropertyManager.getPropertyValue(i));

				}
			}

			// precondition tests
			ContractException contractException = assertThrows(ContractException.class, () -> booleanPropertyManager.getPropertyValue(-1));
			assertEquals(PropertyError.NEGATIVE_INDEX, contractException.getErrorType());
		});

	}

	/*
	 * Local data manager used to properly initialize an BooleanPropertyManager
	 * for use in time sensitive tests
	 */
	private static class LocalDM extends TestDataManager {
		protected BooleanPropertyManager booleanPropertyManager;

		@Override
		protected void init(DataManagerContext dataManagerContext) {
			super.init(dataManagerContext);
			PropertyDefinition propertyDefinition = PropertyDefinition.builder().setType(Boolean.class).setDefaultValue(false).setTimeTrackingPolicy(TimeTrackingPolicy.TRACK_TIME).build();
			booleanPropertyManager = new BooleanPropertyManager(dataManagerContext, propertyDefinition, 0);
		}
	}

	@Test
	@UnitTestMethod(target = BooleanPropertyManager.class, name = "getPropertyTime", args = { int.class })
	public void testGetPropertyTime() {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(6779797760333524552L);

		TestPluginData.Builder pluginDataBuilder = TestPluginData.builder();

		IntStream.range(0, 1000).forEach((i -> {
			pluginDataBuilder.addTestActorPlan("actor", new TestActorPlan(i, (c) -> {
				LocalDM localDM = c.getDataManager(LocalDM.class);
				int id = randomGenerator.nextInt(300);
				boolean value = randomGenerator.nextBoolean();
				BooleanPropertyManager booleanPropertyManager = localDM.booleanPropertyManager;
				booleanPropertyManager.setPropertyValue(id, value);
				// show that the property time for the id was properly set
				assertEquals(c.getTime(), booleanPropertyManager.getPropertyTime(id), 0);
			}));
		}));

		// add the local data manager
		pluginDataBuilder.addTestDataManager("dm", () -> new LocalDM());

		// build and run the simulation
		TestPluginData testPluginData = pluginDataBuilder.build();
		Plugin plugin = TestPlugin.getTestPlugin(testPluginData);
		TestActionSupport.testConsumers(plugin);

		// precondition tests:
		TestActionSupport.testConsumer((c) -> {
			PropertyDefinition propertyDefinition = PropertyDefinition.builder().setType(Boolean.class).setDefaultValue(false).build();
			BooleanPropertyManager booleanPropertyManager = new BooleanPropertyManager(c, propertyDefinition, 0);
			ContractException contractException = assertThrows(ContractException.class, () -> booleanPropertyManager.getPropertyTime(0));
			assertEquals(PropertyError.TIME_TRACKING_OFF, contractException.getErrorType());
		});

		TestActionSupport.testConsumer((c) -> {
			PropertyDefinition propertyDefinition = PropertyDefinition.builder().setType(Boolean.class).setDefaultValue(false).setTimeTrackingPolicy(TimeTrackingPolicy.TRACK_TIME).build();
			BooleanPropertyManager booleanPropertyManager = new BooleanPropertyManager(c, propertyDefinition, 0);
			ContractException contractException = assertThrows(ContractException.class, () -> booleanPropertyManager.getPropertyTime(-1));
			assertEquals(PropertyError.NEGATIVE_INDEX, contractException.getErrorType());
		});
	}

	@Test
	@UnitTestMethod(target = BooleanPropertyManager.class, name = "setPropertyValue", args = { int.class, Object.class })
	public void testSetPropertyValue() {
		TestActionSupport.testConsumer((c) -> {
			RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(4827517950755837724L);

			PropertyDefinition propertyDefinition = PropertyDefinition.builder().setType(Boolean.class).setDefaultValue(false).setTimeTrackingPolicy(TimeTrackingPolicy.TRACK_TIME).build();

			BooleanPropertyManager booleanPropertyManager = new BooleanPropertyManager(c, propertyDefinition, 0);

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
			 * if the value was set above, then it should equal the last value
			 * place in the expected values, otherwise it will have the default
			 * value.
			 */
			for (int i = 0; i < 300; i++) {
				if (expectedValues.containsKey(i)) {
					assertEquals(expectedValues.get(i), booleanPropertyManager.getPropertyValue(i));

				} else {
					assertFalse((Boolean) booleanPropertyManager.getPropertyValue(i));

				}
			}

			// precondition tests
			ContractException contractException = assertThrows(ContractException.class, () -> booleanPropertyManager.setPropertyValue(-1, false));
			assertEquals(PropertyError.NEGATIVE_INDEX, contractException.getErrorType());
		});
	}

	@Test
	@UnitTestMethod(target = BooleanPropertyManager.class, name = "removeId", args = { int.class })
	public void testRemoveId() {
		TestActionSupport.testConsumer((c) -> {

			// we will first test the manager with an initial value of false
			PropertyDefinition propertyDefinition = PropertyDefinition.builder().setType(Boolean.class).setDefaultValue(false).setTimeTrackingPolicy(TimeTrackingPolicy.TRACK_TIME).build();

			BooleanPropertyManager booleanPropertyManager = new BooleanPropertyManager(c, propertyDefinition, 0);

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
			propertyDefinition = PropertyDefinition.builder().setType(Boolean.class).setDefaultValue(true).setTimeTrackingPolicy(TimeTrackingPolicy.TRACK_TIME).build();

			booleanPropertyManager = new BooleanPropertyManager(c, propertyDefinition, 0);

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
			PropertyDefinition def = PropertyDefinition.builder().setType(Boolean.class).setDefaultValue(true).setTimeTrackingPolicy(TimeTrackingPolicy.TRACK_TIME).build();
			BooleanPropertyManager bpm = new BooleanPropertyManager(c, def, 0);

			ContractException contractException = assertThrows(ContractException.class, () -> bpm.removeId(-1));
			assertEquals(PropertyError.NEGATIVE_INDEX, contractException.getErrorType());
		});
	}

	@Test
	@UnitTestConstructor(target = BooleanPropertyManager.class, args = { SimulationContext.class, PropertyDefinition.class, int.class })
	public void testConstructor() {
		TestActionSupport.testConsumer((c) -> {

			PropertyDefinition goodPropertyDefinition = PropertyDefinition.builder().setType(Boolean.class).setDefaultValue(false).build();
			PropertyDefinition badPropertyDefinition = PropertyDefinition.builder().setType(Double.class).setDefaultValue(2.3).build();

			// precondition tests

			// if the property definition is null
			ContractException contractException = assertThrows(ContractException.class, () -> new BooleanPropertyManager(c, null, 0));
			assertEquals(PropertyError.NULL_PROPERTY_DEFINITION, contractException.getErrorType());

			// if the property definition does not have a type of Boolean.class
			contractException = assertThrows(ContractException.class, () -> new BooleanPropertyManager(c, badPropertyDefinition, 0));
			assertEquals(PropertyError.PROPERTY_DEFINITION_IMPROPER_TYPE, contractException.getErrorType());

			// if the initial size is negative
			contractException = assertThrows(ContractException.class, () -> new BooleanPropertyManager(c, goodPropertyDefinition, -1));
			assertEquals(PropertyError.NEGATIVE_INITIAL_SIZE, contractException.getErrorType());

			BooleanPropertyManager booleanPropertyManager = new BooleanPropertyManager(c, goodPropertyDefinition, 0);
			assertNotNull(booleanPropertyManager);
		});
	}

	@Test
	@UnitTestMethod(target = BooleanPropertyManager.class, name = "incrementCapacity", args = { int.class })
	public void testIncrementCapacity() {
		TestActionSupport.testConsumer((c) -> {

			PropertyDefinition propertyDefinition = PropertyDefinition.builder().setType(Boolean.class).setDefaultValue(false).setTimeTrackingPolicy(TimeTrackingPolicy.TRACK_TIME).build();

			BooleanPropertyManager booleanPropertyManager = new BooleanPropertyManager(c, propertyDefinition, 0);

			// precondition tests
			ContractException contractException = assertThrows(ContractException.class, () -> booleanPropertyManager.incrementCapacity(-1));
			assertEquals(PropertyError.NEGATIVE_CAPACITY_INCREMENT, contractException.getErrorType());
		});
	}

}
