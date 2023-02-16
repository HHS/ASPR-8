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
		TestSimulation.executeSimulation(TestPluginFactory.factory((c) -> {
			RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(5102684240650614254L);

			Color defaultValue = Color.YELLOW;
			PropertyDefinition propertyDefinition = PropertyDefinition.builder().setType(Color.class).setDefaultValue(defaultValue).setTimeTrackingPolicy(TimeTrackingPolicy.TRACK_TIME).build();

			EnumPropertyManager enumPropertyManager = new EnumPropertyManager(c, propertyDefinition, 0);

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
		}).getPlugins());
	}

	/*
	 * Local data manager used to properly initialize an EnumPropertyManager for
	 * use in time sensitive tests
	 */
	private static class LocalDM extends TestDataManager {
		public EnumPropertyManager enumPropertyManager;

		@Override
		protected void init(DataManagerContext dataManagerContext) {
			super.init(dataManagerContext);
			PropertyDefinition propertyDefinition = PropertyDefinition.builder().setType(Color.class).setDefaultValue(Color.YELLOW).setTimeTrackingPolicy(TimeTrackingPolicy.TRACK_TIME).build();
			enumPropertyManager = new EnumPropertyManager(dataManagerContext, propertyDefinition, 0);
		}
	}

	@Test
	@UnitTestMethod(target = EnumPropertyManager.class, name = "getPropertyTime", args = { int.class })
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

		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(2965406559079298427L);

		TestPluginData.Builder pluginDataBuilder = TestPluginData.builder();

		IntStream.range(0, 1000).forEach((i -> {
			pluginDataBuilder.addTestActorPlan("actor", new TestActorPlan(i, (c) -> {
				LocalDM localDM = c.getDataManager(LocalDM.class);
				int id = randomGenerator.nextInt(300);
				Color value = Color.values()[randomGenerator.nextInt(3)];
				EnumPropertyManager enumPropertyManager = localDM.enumPropertyManager;
				enumPropertyManager.setPropertyValue(id, value);
				// show that the property time for the id was properly set
				assertEquals(c.getTime(), enumPropertyManager.getPropertyTime(id), 0);
			}));
		}));

		// add the local data manager
		pluginDataBuilder.addTestDataManager("dm", () -> new LocalDM());

		// build and run the simulation
		TestPluginData testPluginData = pluginDataBuilder.build();
		TestSimulation.executeSimulation(TestPluginFactory.factory(testPluginData).getPlugins());

		// precondition tests:
		TestSimulation.executeSimulation(TestPluginFactory.factory((c) -> {
			PropertyDefinition propertyDefinition = PropertyDefinition.builder().setType(Color.class).setDefaultValue(Color.BLUE).build();
			EnumPropertyManager epm = new EnumPropertyManager(c, propertyDefinition, 0);
			ContractException contractException = assertThrows(ContractException.class, () -> epm.getPropertyTime(0));
			assertEquals(PropertyError.TIME_TRACKING_OFF, contractException.getErrorType());
		}).getPlugins());

		TestSimulation.executeSimulation(TestPluginFactory.factory((c) -> {
			PropertyDefinition propertyDefinition = PropertyDefinition.builder().setType(Color.class).setDefaultValue(Color.BLUE).setTimeTrackingPolicy(TimeTrackingPolicy.TRACK_TIME).build();
			EnumPropertyManager epm = new EnumPropertyManager(c, propertyDefinition, 0);
			ContractException contractException = assertThrows(ContractException.class, () -> epm.getPropertyTime(-1));
			assertEquals(PropertyError.NEGATIVE_INDEX, contractException.getErrorType());
		}).getPlugins());
	}

	@Test
	@UnitTestMethod(target = EnumPropertyManager.class, name = "setPropertyValue", args = { int.class, Object.class })
	public void testSetPropertyValue() {
		TestSimulation.executeSimulation(TestPluginFactory.factory((c) -> {
			RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(6716984272666831621L);

			Color defaultValue = Color.YELLOW;
			PropertyDefinition propertyDefinition = PropertyDefinition.builder().setType(Color.class).setDefaultValue(defaultValue).setTimeTrackingPolicy(TimeTrackingPolicy.TRACK_TIME).build();

			EnumPropertyManager enumPropertyManager = new EnumPropertyManager(c, propertyDefinition, 0);

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
		}).getPlugins());
	}

	@Test
	@UnitTestMethod(target = EnumPropertyManager.class, name = "removeId", args = { int.class })
	public void testRemoveId() {
		TestSimulation.executeSimulation(TestPluginFactory.factory((c) -> {
			/*
			 * Should have no effect on the value that is stored for the sake of
			 * efficiency.
			 */

			// we will first test the manager with an initial value of false
			Color defaultValue = Color.RED;
			PropertyDefinition propertyDefinition = PropertyDefinition.builder().setType(Color.class).setDefaultValue(defaultValue).setTimeTrackingPolicy(TimeTrackingPolicy.TRACK_TIME).build();

			EnumPropertyManager enumPropertyManager = new EnumPropertyManager(c, propertyDefinition, 0);

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

			enumPropertyManager = new EnumPropertyManager(c, propertyDefinition, 0);

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
			EnumPropertyManager epm = new EnumPropertyManager(c, def, 0);

			ContractException contractException = assertThrows(ContractException.class, () -> epm.removeId(-1));

			assertEquals(PropertyError.NEGATIVE_INDEX, contractException.getErrorType());
		}).getPlugins());
	}

	// Helper enum
	private static enum Color {
		RED, YELLOW, BLUE;
	}

	@Test
	@UnitTestConstructor(target = EnumPropertyManager.class, args = { SimulationContext.class, PropertyDefinition.class, int.class })
	public void testConstructor() {
		TestSimulation.executeSimulation(TestPluginFactory.factory((c) -> {

			PropertyDefinition goodPropertyDefinition = PropertyDefinition.builder().setType(Color.class).setDefaultValue(Color.BLUE).build();
			PropertyDefinition badPropertyDefinition = PropertyDefinition.builder().setType(Boolean.class).setDefaultValue(false).build();

			// if the property definition is null
			ContractException contractException = assertThrows(ContractException.class, () -> new EnumPropertyManager(c, null, 0));
			assertEquals(PropertyError.NULL_PROPERTY_DEFINITION, contractException.getErrorType());

			// if the property definition does not have a type of Enum.class
			contractException = assertThrows(ContractException.class, () -> new EnumPropertyManager(c, badPropertyDefinition, 0));
			assertEquals(PropertyError.PROPERTY_DEFINITION_IMPROPER_TYPE, contractException.getErrorType());

			// if the initial size is negative
			contractException = assertThrows(ContractException.class, () -> new EnumPropertyManager(c, goodPropertyDefinition, -1));
			assertEquals(PropertyError.NEGATIVE_INITIAL_SIZE, contractException.getErrorType());

			EnumPropertyManager enumPropertyManager = new EnumPropertyManager(c, goodPropertyDefinition, 0);
			assertNotNull(enumPropertyManager);
		}).getPlugins());
	}

	@Test
	@UnitTestMethod(target = EnumPropertyManager.class, name = "incrementCapacity", args = { int.class })
	public void testIncrementCapacity() {
		TestSimulation.executeSimulation(TestPluginFactory.factory((c) -> {

			PropertyDefinition propertyDefinition = PropertyDefinition.builder().setType(Color.class).setDefaultValue(Color.RED).setTimeTrackingPolicy(TimeTrackingPolicy.TRACK_TIME).build();

			EnumPropertyManager enumPropertyManager = new EnumPropertyManager(c, propertyDefinition, 0);

			// precondition tests
			ContractException contractException = assertThrows(ContractException.class, () -> enumPropertyManager.incrementCapacity(-1));
			assertEquals(PropertyError.NEGATIVE_CAPACITY_INCREMENT, contractException.getErrorType());
		}).getPlugins());
	}

}
