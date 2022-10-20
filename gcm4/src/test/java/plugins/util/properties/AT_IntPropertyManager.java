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
import nucleus.testsupport.testplugin.TestActionSupport;
import nucleus.testsupport.testplugin.TestActorPlan;
import nucleus.testsupport.testplugin.TestDataManager;
import nucleus.testsupport.testplugin.TestPluginData;
import tools.annotations.UnitTest;
import tools.annotations.UnitTestConstructor;
import tools.annotations.UnitTestMethod;
import util.errors.ContractException;
import util.random.RandomGeneratorProvider;

/**
 * Common interface to all person property managers. A person property manager
 * manages all the property values for people for a particular person property
 * identifier.
 * 
 * @author Shawn Hatch
 *
 */
@UnitTest(target = IntPropertyManager.class)
public class AT_IntPropertyManager {

	@Test
	@UnitTestMethod(name = "getPropertyValue", args = { int.class })
	public void testGetPropertyValue() {
		TestActionSupport.testConsumer((c) -> {
			RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(5297426971018191882L);

			int defaultValue = 423;
			PropertyDefinition propertyDefinition = PropertyDefinition.builder().setType(Integer.class).setDefaultValue(defaultValue).setTimeTrackingPolicy(TimeTrackingPolicy.TRACK_TIME).build();

			IntPropertyManager intPropertyManager = new IntPropertyManager(c, propertyDefinition, 0);

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
			 * if the value was set above, then it should equal the last value
			 * place in the expected values, otherwise it will have the default
			 * value.
			 */
			for (int i = 0; i < 300; i++) {
				if (expectedValues.containsKey(i)) {
					assertEquals(expectedValues.get(i), intPropertyManager.getPropertyValue(i));

				} else {
					assertEquals(defaultValue, ((Integer) intPropertyManager.getPropertyValue(i)).intValue());
				}
			}

			// precondition tests
			ContractException contractException = assertThrows(ContractException.class, () -> intPropertyManager.getPropertyValue(-1));
			assertEquals(PropertyError.NEGATIVE_INDEX, contractException.getErrorType());
		});
	}

	/*
	 * Local data manager used to properly initialize an ObjectPropertyManager
	 * for use in time sensitive tests
	 */
	public static class LocalDM extends TestDataManager {
		public IntPropertyManager intPropertyManager;

		@Override
		public void init(DataManagerContext dataManagerContext) {
			super.init(dataManagerContext);
			PropertyDefinition propertyDefinition = PropertyDefinition.builder().setType(Integer.class).setDefaultValue(342).setTimeTrackingPolicy(TimeTrackingPolicy.TRACK_TIME).build();
			intPropertyManager = new IntPropertyManager(dataManagerContext, propertyDefinition, 0);
		}
	}

	@Test
	@UnitTestMethod(name = "getPropertyTime", args = { int.class })
	public void testGetPropertyTime() {

		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(7115872240650739867L);

		TestPluginData.Builder pluginDataBuilder = TestPluginData.builder();

		// Plan 1000 changes
		IntStream.range(0, 1000).forEach((i -> {
			pluginDataBuilder.addTestActorPlan("actor", new TestActorPlan(i, (c) -> {
				LocalDM localDM = c.getDataManager(LocalDM.class);
				int id = randomGenerator.nextInt(300);
				int value = randomGenerator.nextInt();

				IntPropertyManager intPropertyManager = localDM.intPropertyManager;

				intPropertyManager.setPropertyValue(id, value);
				// show that the property time for the id was properly set
				assertEquals(c.getTime(), intPropertyManager.getPropertyTime(id), 0);
			}));
		}));

		// precondition tests:
		TestActionSupport.testConsumer((c) -> {
			PropertyDefinition propertyDefinition = PropertyDefinition.builder().setType(Integer.class).setDefaultValue(2).build();
			IntPropertyManager ipm = new IntPropertyManager(c, propertyDefinition, 0);
			ContractException contractException = assertThrows(ContractException.class, () -> ipm.getPropertyTime(0));
			assertEquals(PropertyError.TIME_TRACKING_OFF, contractException.getErrorType());
		});

		TestActionSupport.testConsumer((c) -> {
			PropertyDefinition propertyDefinition = PropertyDefinition.builder().setType(Integer.class).setDefaultValue(2).setTimeTrackingPolicy(TimeTrackingPolicy.TRACK_TIME).build();
			IntPropertyManager ipm = new IntPropertyManager(c, propertyDefinition, 0);
			ContractException contractException = assertThrows(ContractException.class, () -> ipm.getPropertyTime(-1));
			assertEquals(PropertyError.NEGATIVE_INDEX, contractException.getErrorType());
		});
	}

	@Test
	@UnitTestMethod(name = "setPropertyValue", args = { int.class, Object.class })
	public void testSetPropertyValue() {
		TestActionSupport.testConsumer((c) -> {
			RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(5297426971018191882L);

			int defaultValue = 423;
			PropertyDefinition propertyDefinition = PropertyDefinition.builder().setType(Integer.class).setDefaultValue(defaultValue).setTimeTrackingPolicy(TimeTrackingPolicy.TRACK_TIME).build();

			IntPropertyManager intPropertyManager = new IntPropertyManager(c, propertyDefinition, 0);

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
			 * if the value was set above, then it should equal the last value
			 * place in the expected values, otherwise it will have the default
			 * value.
			 */
			for (int i = 0; i < 300; i++) {
				if (expectedValues.containsKey(i)) {
					assertEquals(expectedValues.get(i), intPropertyManager.getPropertyValue(i));

				} else {
					assertEquals(defaultValue, ((Integer) intPropertyManager.getPropertyValue(i)).intValue());
				}
			}

			// precondition tests
			ContractException contractException = assertThrows(ContractException.class, () -> intPropertyManager.setPropertyValue(-1, 23));
			assertEquals(PropertyError.NEGATIVE_INDEX, contractException.getErrorType());
		});
	}

	@Test
	@UnitTestMethod(name = "removeId", args = { int.class })
	public void testRemoveId() {
		TestActionSupport.testConsumer((c) -> {
			/*
			 * Should have no effect on the value that is stored for the sake of
			 * efficiency.
			 */

			// we will first test the manager with an initial value of 6
			int defaultValue = 6;
			PropertyDefinition propertyDefinition = PropertyDefinition.builder().setType(Integer.class).setDefaultValue(defaultValue).setTimeTrackingPolicy(TimeTrackingPolicy.TRACK_TIME).build();

			IntPropertyManager intPropertyManager = new IntPropertyManager(c, propertyDefinition, 0);

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
			propertyDefinition = PropertyDefinition.builder().setType(Integer.class).setDefaultValue(defaultValue).setTimeTrackingPolicy(TimeTrackingPolicy.TRACK_TIME).build();

			intPropertyManager = new IntPropertyManager(c, propertyDefinition, 0);

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
			PropertyDefinition def = PropertyDefinition.builder().setType(Integer.class).setDefaultValue(3).setTimeTrackingPolicy(TimeTrackingPolicy.TRACK_TIME).build();
			IntPropertyManager ipm = new IntPropertyManager(c, def, 0);

			ContractException contractException = assertThrows(ContractException.class, () -> ipm.removeId(-1));
			assertEquals(PropertyError.NEGATIVE_INDEX, contractException.getErrorType());
		});
	}

	@Test
	@UnitTestConstructor(args = { SimulationContext.class, PropertyDefinition.class, int.class })
	public void testConstructor() {
		TestActionSupport.testConsumer((c) -> {

			PropertyDefinition goodPropertyDefinition = PropertyDefinition.builder().setType(Integer.class).setDefaultValue(2).build();
			PropertyDefinition badPropertyDefinition = PropertyDefinition.builder().setType(Boolean.class).setDefaultValue(false).build();

			// if the property definition is null
			ContractException contractException = assertThrows(ContractException.class, () -> new IntPropertyManager(c, null, 0));
			assertEquals(PropertyError.NULL_PROPERTY_DEFINITION, contractException.getErrorType());

			// if the property definition does not have a type of Double.class
			contractException = assertThrows(ContractException.class, () -> new IntPropertyManager(c, badPropertyDefinition, 0));
			assertEquals(PropertyError.PROPERTY_DEFINITION_IMPROPER_TYPE, contractException.getErrorType());

			// if the initial size is negative
			contractException = assertThrows(ContractException.class, () -> new IntPropertyManager(c, goodPropertyDefinition, -1));
			assertEquals(PropertyError.NEGATIVE_INITIAL_SIZE, contractException.getErrorType());

			IntPropertyManager doublePropertyManager = new IntPropertyManager(c, goodPropertyDefinition, 0);
			assertNotNull(doublePropertyManager);
		});
	}

	@Test
	@UnitTestMethod(name = "incrementCapacity", args = { int.class })
	public void testIncrementCapacity() {
		TestActionSupport.testConsumer((c) -> {

			PropertyDefinition propertyDefinition = PropertyDefinition.builder().setType(Integer.class).setDefaultValue(234).setTimeTrackingPolicy(TimeTrackingPolicy.TRACK_TIME).build();

			IntPropertyManager intPropertyManager = new IntPropertyManager(c, propertyDefinition, 0);

			// precondition tests
			ContractException contractException = assertThrows(ContractException.class, () -> intPropertyManager.incrementCapacity(-1));
			assertEquals(PropertyError.NEGATIVE_CAPACITY_INCREMENT, contractException.getErrorType());
		});
	}

}
