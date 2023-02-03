package nucleus.testsupport.testplugin;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.function.Consumer;

import org.junit.jupiter.api.Test;

import nucleus.ActorContext;
import nucleus.NucleusError;
import nucleus.Plugin;
import plugins.globalproperties.testsupport.GlobalPropertiesTestPluginFactory;
import tools.annotations.UnitTestMethod;
import util.errors.ContractException;
import util.wrappers.MutableBoolean;

public class AT_TestPluginFactory {

	/**
	 * Convience method to create a consumer to facilitate testing the factory
	 * methods
	 * {@link AT_TestPluginFactory#testFactory_Consumer()}
	 * and
	 * {@link AT_TestPluginFactory#testFactory_TestPluginData()}
	 * 
	 * <li>either for passing directly to
	 * <li>{@link TestPluginFactory#factory(long, Consumer)}
	 * <li>or indirectly via creating a TestPluginData and passing it to
	 * <li>{@link TestPluginFactory#factory(long, TestPluginData)}
	 * 
	 * @param executed boolean to set once the consumer completes
	 * @return the consumer
	 * 
	 */
	private Consumer<ActorContext> factoryConsumer(MutableBoolean executed) {
		return (c) -> {
			/*
			 * Show that the minimal plugins for the test are present by confirming that the
			 * corresponding datamanger is not null.
			 * Do not need to explicitly test the data associated with the datamanager, as
			 * that will be tested in the 'getStandardizedXPluginData' tests below.
			 */
			TestPlanDataManager testPlanDataManager = c.getDataManager(TestPlanDataManager.class);
			assertNotNull(testPlanDataManager);
			executed.setValue(true);
		};
	}

	@Test
	@UnitTestMethod(target = TestPluginFactory.class, name = "factory", args = { Consumer.class })
	public void testFactory_Consumer() {
		MutableBoolean executed = new MutableBoolean();
		TestSimulation
				.executeSimulation(TestPluginFactory.factory(factoryConsumer(executed)).getPlugins());
		assertTrue(executed.getValue());

		// precondition: consumer is null
		Consumer<ActorContext> nullConsumer = null;
		ContractException contractException = assertThrows(ContractException.class,
				() -> GlobalPropertiesTestPluginFactory.factory(nullConsumer));
		assertEquals(NucleusError.NULL_ACTOR_CONTEXT_CONSUMER, contractException.getErrorType());
	}

	@Test
	@UnitTestMethod(target = TestPluginFactory.class, name = "factory", args = { TestPluginData.class })
	public void testFactory_TestPluginData() {
		MutableBoolean executed = new MutableBoolean();
		TestPluginData.Builder pluginBuilder = TestPluginData.builder();
		pluginBuilder.addTestActorPlan("actor", new TestActorPlan(0, factoryConsumer(executed)));
		TestPluginData testPluginData = pluginBuilder.build();

		TestSimulation.executeSimulation(TestPluginFactory.factory(testPluginData).getPlugins());
		assertTrue(executed.getValue());

		// precondition: testPluginData is null
		TestPluginData nullTestPluginData = null;
		ContractException contractException = assertThrows(ContractException.class,
				() -> GlobalPropertiesTestPluginFactory.factory(nullTestPluginData));
		assertEquals(NucleusError.NULL_PLUGIN_DATA, contractException.getErrorType());

	}

	@Test
	@UnitTestMethod(target = TestPluginFactory.Factory.class, name = "getPlugins", args = {})
	public void testGetPlugins() {

		List<Plugin> plugins = TestPluginFactory.factory((c) -> {}).getPlugins();
		assertEquals(1, plugins.size());

		Plugin testPlugin = null;

		for(Plugin plugin : plugins) {
			if (plugin.getPluginId().equals(TestPluginId.PLUGIN_ID)) {
				assertNull(testPlugin);
				testPlugin = plugin;
			}
		}

		assertNotNull(testPlugin);
	}
}
