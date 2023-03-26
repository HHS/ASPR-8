package plugins.util.properties;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

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


public class AT_AbstractIndexedPropertyManager {

	/*
	 * 
	 * A simple concrete extension of AbstractIndexedPropertyManager used to
	 * test AbstractIndexedPropertyManager.
	 *
	 */
	private static class SimplePropertyManager extends AbstractIndexedPropertyManager {

		public SimplePropertyManager(SimulationContext context, PropertyDefinition propertyDefinition, int initialSize) {
			super(context, propertyDefinition, initialSize);
		}

		@Override
		public <T> T getPropertyValue(int id) {
			return null;
		}

	}

	@Test
	@UnitTestConstructor(target = AbstractIndexedPropertyManager.class,args = { SimulationContext.class, PropertyDefinition.class, int.class })
	public void testConstructor() {
		
		
		Factory factory = TestPluginFactory.factory((c) -> {

			PropertyDefinition goodPropertyDefinition = PropertyDefinition.builder().setType(Boolean.class).setDefaultValue(false).build();

			// if the property definition is null
			ContractException contractException = assertThrows(ContractException.class, () -> new BooleanPropertyManager(c, null, 0));
			assertEquals(PropertyError.NULL_PROPERTY_DEFINITION, contractException.getErrorType());

			// if the initial size is negative
			contractException = assertThrows(ContractException.class, () -> new BooleanPropertyManager(c, goodPropertyDefinition, -1));
			assertEquals(PropertyError.NEGATIVE_INITIAL_SIZE, contractException.getErrorType());

			SimplePropertyManager simplePropertyManager = new SimplePropertyManager(c, goodPropertyDefinition, 0);
			assertNotNull(simplePropertyManager);
		});
		
		TestSimulation.builder().addPlugins(factory.getPlugins()).build().execute();
	}

	@Test
	@UnitTestMethod(target = AbstractIndexedPropertyManager.class,name = "setPropertyValue", args = { int.class, Object.class })
	public void testSetPropertyValue() {
		TestSimulation.executeSimulation(TestPluginFactory.factory((c) -> {

			PropertyDefinition propertyDefinition = PropertyDefinition.builder().setType(Boolean.class).setDefaultValue(false).setTimeTrackingPolicy(TimeTrackingPolicy.TRACK_TIME).build();

			// precondition tests
			SimplePropertyManager simplePropertyManager = new SimplePropertyManager(c, propertyDefinition, 0);
			ContractException contractException = assertThrows(ContractException.class, () -> simplePropertyManager.setPropertyValue(-1, false));
			assertEquals(PropertyError.NEGATIVE_INDEX, contractException.getErrorType());
		}).getPlugins());
	}

	/*
	 * Local data manager used to properly initialize an ObjectPropertyManager
	 * for use in time sensitive tests
	 */
	private static class LocalDM extends TestDataManager {
		public SimplePropertyManager simplePropertyManager;

		@Override
		public void init(DataManagerContext dataManagerContext) {
			super.init(dataManagerContext);
			PropertyDefinition propertyDefinition = PropertyDefinition.builder().setType(Boolean.class).setDefaultValue(false).setTimeTrackingPolicy(TimeTrackingPolicy.TRACK_TIME).build();
			simplePropertyManager = new SimplePropertyManager(dataManagerContext, propertyDefinition, 0);
		}
	}

	@Test
	@UnitTestMethod(target = AbstractIndexedPropertyManager.class,name = "getPropertyTime", args = { int.class })
	public void testGetPropertyTime() {

		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(1003433950467196390L);

		TestPluginData.Builder pluginDataBuilder = TestPluginData.builder();

		IntStream.range(0, 1000).forEach((i -> {
			pluginDataBuilder.addTestActorPlan("actor", new TestActorPlan(i, (c) -> {
				LocalDM localDM = c.getDataManager(LocalDM.class);
				int id = randomGenerator.nextInt(300);
				boolean value = randomGenerator.nextBoolean();
				SimplePropertyManager simplePropertyManager = localDM.simplePropertyManager;
				simplePropertyManager.setPropertyValue(id, value);
				// show that the property time for the id was properly set
				assertEquals(c.getTime(), simplePropertyManager.getPropertyTime(id), 0);
			}));
		}));

		// add the local data manager
		pluginDataBuilder.addTestDataManager("dm", ()->new LocalDM());

		// build and run the simulation
		TestPluginData testPluginData = pluginDataBuilder.build();
		TestSimulation.executeSimulation(TestPluginFactory.factory(testPluginData).getPlugins());

		// precondition test: if time tracking is no engaged
		TestSimulation.executeSimulation(TestPluginFactory.factory((c) -> {
			PropertyDefinition propertyDefinition = PropertyDefinition.builder().setType(Boolean.class).setDefaultValue(false).build();
			SimplePropertyManager spm = new SimplePropertyManager(c, propertyDefinition, 0);
			ContractException contractException = assertThrows(ContractException.class, () -> spm.getPropertyTime(0));
			assertEquals(PropertyError.TIME_TRACKING_OFF, contractException.getErrorType());
		}).getPlugins());

		// precondition test: if a property time is retrieved for a negative
		// index
		TestSimulation.executeSimulation(TestPluginFactory.factory((c) -> {
			PropertyDefinition propertyDefinition = PropertyDefinition.builder().setType(Boolean.class).setDefaultValue(false).setTimeTrackingPolicy(TimeTrackingPolicy.TRACK_TIME).build();
			SimplePropertyManager spm = new SimplePropertyManager(c, propertyDefinition, 0);
			ContractException contractException = assertThrows(ContractException.class, () -> spm.getPropertyTime(-1));
			assertEquals(PropertyError.NEGATIVE_INDEX, contractException.getErrorType());
		}).getPlugins());
	}

	@Test
	@UnitTestMethod(target = AbstractIndexedPropertyManager.class,name = "removeId", args = { int.class })
	public void testRemoveId() {
		TestSimulation.executeSimulation(TestPluginFactory.factory((c) -> {
			// precondition tests
			PropertyDefinition def = PropertyDefinition.builder().setType(Boolean.class).setDefaultValue(true).setTimeTrackingPolicy(TimeTrackingPolicy.TRACK_TIME).build();
			SimplePropertyManager spm = new SimplePropertyManager(c, def, 0);

			ContractException contractException = assertThrows(ContractException.class, () -> spm.removeId(-1));
			assertEquals(PropertyError.NEGATIVE_INDEX, contractException.getErrorType());
		}).getPlugins());
	}

	@Test
	@UnitTestMethod(target = AbstractIndexedPropertyManager.class,name = "incrementCapacity", args = { int.class })
	public void testIncrementCapacity() {
		TestSimulation.executeSimulation(TestPluginFactory.factory((c) -> {

			PropertyDefinition propertyDefinition = PropertyDefinition.builder().setType(Boolean.class).setDefaultValue(false).setTimeTrackingPolicy(TimeTrackingPolicy.TRACK_TIME).build();

			SimplePropertyManager simplePropertyManager = new SimplePropertyManager(c, propertyDefinition, 0);

			// precondition tests
			ContractException contractException = assertThrows(ContractException.class, () -> simplePropertyManager.incrementCapacity(-1));
			assertEquals(PropertyError.NEGATIVE_CAPACITY_INCREMENT, contractException.getErrorType());
		}).getPlugins());
	}
}
