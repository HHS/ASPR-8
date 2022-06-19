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

@UnitTest(target = ObjectPropertyManager.class)
public class AT_ObjectPropertyManager {

	@Test
	@UnitTestMethod(name = "getPropertyValue", args = { int.class })
	public void testGetPropertyValue() {
		TestActionSupport.testConsumer((c) -> {
			RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(6268125375257441705L);

			String defaultValue = "YELLOW";
			PropertyDefinition propertyDefinition = PropertyDefinition.builder().setType(String.class).setDefaultValue(defaultValue).setTimeTrackingPolicy(TimeTrackingPolicy.TRACK_TIME).build();

			ObjectPropertyManager objectPropertyManager = new ObjectPropertyManager(c, propertyDefinition, 0);

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
	}

	/*
	 * Local data manager used to properly initialize an ObjectPropertyManager
	 * for use in time sensitive tests
	 */
	private static class LocalDM extends TestDataManager {
		public ObjectPropertyManager objectPropertyManager;

		@Override
		public void init(DataManagerContext dataManagerContext) {
			super.init(dataManagerContext);
			PropertyDefinition propertyDefinition = PropertyDefinition.builder().setType(String.class).setDefaultValue("YELLOW").setTimeTrackingPolicy(TimeTrackingPolicy.TRACK_TIME).build();
			objectPropertyManager = new ObjectPropertyManager(dataManagerContext, propertyDefinition, 0);
		}
	}

	@Test
	@UnitTestMethod(name = "getPropertyTime", args = { int.class })
	public void testGetPropertyTime() {

		/*
		 * Execute random changes to an object property for several people.
		 */
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(3180659211825142278L);

		TestPluginData.Builder pluginDataBuilder = TestPluginData.builder();

		// Plan 1000 changes
		IntStream.range(0, 1000).forEach((i -> {
			pluginDataBuilder.addTestActorPlan("actor", new TestActorPlan(i, (c) -> {
				LocalDM localDM = c.getDataManager(LocalDM.class);
				int id = randomGenerator.nextInt(300);
				String value = getRandomString(randomGenerator);
				ObjectPropertyManager objectPropertyManager = localDM.objectPropertyManager;
				objectPropertyManager.setPropertyValue(id, value);
				// show that the property time for the id was properly set
				assertEquals(c.getTime(), objectPropertyManager.getPropertyTime(id), 0);
			}));
		}));

		// add the local data manager
		pluginDataBuilder.addTestDataManager("dm",()-> new LocalDM());

		// build and run the simulation
		TestPluginData testPluginData = pluginDataBuilder.build();
		Plugin plugin = TestPlugin.getTestPlugin(testPluginData);
		TestActionSupport.testConsumers(plugin);

		// precondition test: if time tracking is no engaged
		TestActionSupport.testConsumer((c) -> {
			PropertyDefinition propertyDefinition = PropertyDefinition	.builder()//
																		.setType(Boolean.class)//
																		.setDefaultValue(false)//
																		.build();//
			ObjectPropertyManager opm = new ObjectPropertyManager(c, propertyDefinition, 0);
			ContractException contractException = assertThrows(ContractException.class, () -> opm.getPropertyTime(0));
			assertEquals(PropertyError.TIME_TRACKING_OFF, contractException.getErrorType());
		});

		// precondition test: if a property time is retrieved for a negative
		// index
		TestActionSupport.testConsumer((c) -> {
			PropertyDefinition propertyDefinition = PropertyDefinition	.builder()//
																		.setType(Boolean.class)//
																		.setDefaultValue(false)//
																		.setTimeTrackingPolicy(TimeTrackingPolicy.TRACK_TIME)//
																		.build();//
			ObjectPropertyManager opm = new ObjectPropertyManager(c, propertyDefinition, 0);
			ContractException contractException = assertThrows(ContractException.class, () -> opm.getPropertyTime(-1));
			assertEquals(PropertyError.NEGATIVE_INDEX, contractException.getErrorType());
		});

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
	@UnitTestMethod(name = "setPropertyValue", args = { int.class, Object.class })
	public void testSetPropertyValue() {
		TestActionSupport.testConsumer((c) -> {
			RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(6268125375257441705L);

			String defaultValue = "YELLOW";
			PropertyDefinition propertyDefinition = PropertyDefinition.builder().setType(String.class).setDefaultValue(defaultValue).setTimeTrackingPolicy(TimeTrackingPolicy.TRACK_TIME).build();

			ObjectPropertyManager objectPropertyManager = new ObjectPropertyManager(c, propertyDefinition, 0);

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
	}

	@Test
	@UnitTestMethod(name = "removeId", args = { int.class })
	public void testRemoveId() {

		TestActionSupport.testConsumer((c) -> {
			/*
			 * Should have no effect on the value that is stored for the sake of
			 * efficiency.
			 */

			// we will first test the manager with an initial value of false
			String defaultValue = "RED";
			PropertyDefinition propertyDefinition = PropertyDefinition.builder().setType(String.class).setDefaultValue(defaultValue).setTimeTrackingPolicy(TimeTrackingPolicy.TRACK_TIME).build();

			ObjectPropertyManager objectPropertyManager = new ObjectPropertyManager(c, propertyDefinition, 0);

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
			propertyDefinition = PropertyDefinition.builder().setType(String.class).setDefaultValue(defaultValue).setTimeTrackingPolicy(TimeTrackingPolicy.TRACK_TIME).build();

			objectPropertyManager = new ObjectPropertyManager(c, propertyDefinition, 0);

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
			PropertyDefinition def = PropertyDefinition.builder().setType(Boolean.class).setDefaultValue(true).setTimeTrackingPolicy(TimeTrackingPolicy.TRACK_TIME).build();
			ObjectPropertyManager opm = new ObjectPropertyManager(c, def, 0);

			ContractException contractException = assertThrows(ContractException.class, () -> opm.removeId(-1));
			assertEquals(PropertyError.NEGATIVE_INDEX, contractException.getErrorType());
		});
	}

	@Test
	@UnitTestConstructor(args = { SimulationContext.class, PropertyDefinition.class, int.class })
	public void testConstructor() {
		TestActionSupport.testConsumer((c) -> {

			PropertyDefinition goodPropertyDefinition = PropertyDefinition.builder().setType(Object.class).setDefaultValue("BLUE").build();

			// if the property definition is null
			ContractException contractException = assertThrows(ContractException.class, () -> new ObjectPropertyManager(c, null, 0));
			assertEquals(PropertyError.NULL_PROPERTY_DEFINITION, contractException.getErrorType());
			
			// if the initial size is negative
			contractException = assertThrows(ContractException.class, () -> new ObjectPropertyManager(c, goodPropertyDefinition, -1));
			assertEquals(PropertyError.NEGATIVE_INITIAL_SIZE, contractException.getErrorType());

			ObjectPropertyManager objectPropertyManager = new ObjectPropertyManager(c, goodPropertyDefinition, 0);
			assertNotNull(objectPropertyManager);
		});
	}

	@Test
	@UnitTestMethod(name = "incrementCapacity", args = { int.class })
	public void testIncrementCapacity() {
		TestActionSupport.testConsumer((c) -> {

			PropertyDefinition propertyDefinition = PropertyDefinition.builder().setType(Integer.class).setDefaultValue(234).setTimeTrackingPolicy(TimeTrackingPolicy.TRACK_TIME).build();

			ObjectPropertyManager objectPropertyManager = new ObjectPropertyManager(c, propertyDefinition, 0);

			// precondition tests
			ContractException contractException = assertThrows(ContractException.class, () -> objectPropertyManager.incrementCapacity(-1));
			assertEquals(PropertyError.NEGATIVE_CAPACITY_INCREMENT, contractException.getErrorType());
		});
	}

}
