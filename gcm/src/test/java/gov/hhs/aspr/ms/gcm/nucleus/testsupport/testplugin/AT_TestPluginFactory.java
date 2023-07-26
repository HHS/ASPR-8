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
import nucleus.testsupport.testplugin.TestPluginFactory.Factory;
import util.annotations.UnitTestMethod;
import util.errors.ContractException;
import util.wrappers.MutableBoolean;

public class AT_TestPluginFactory {

	@Test
	@UnitTestMethod(target = TestPluginFactory.class, name = "factory", args = { Consumer.class })
	public void testFactory_Consumer() {
		MutableBoolean executed = new MutableBoolean();
		Factory factory = TestPluginFactory.factory(c -> executed.setValue(true));
		TestSimulation.builder().addPlugins(factory.getPlugins()).build().execute();
		assertTrue(executed.getValue());

		// precondition: consumer is null
		Consumer<ActorContext> nullConsumer = null;
		ContractException contractException = assertThrows(ContractException.class,
				() -> TestPluginFactory.factory(nullConsumer));
		assertEquals(NucleusError.NULL_ACTOR_CONTEXT_CONSUMER, contractException.getErrorType());
	}

	@Test
	@UnitTestMethod(target = TestPluginFactory.class, name = "factory", args = { TestPluginData.class })
	public void testFactory_TestPluginData() {
		MutableBoolean executed = new MutableBoolean();
		TestPluginData.Builder pluginBuilder = TestPluginData.builder();
		pluginBuilder.addTestActorPlan("actor", new TestActorPlan(0, c -> executed.setValue(true)));
		TestPluginData testPluginData = pluginBuilder.build();
		Factory factory = TestPluginFactory.factory(testPluginData);		
		TestSimulation.builder().addPlugins(factory.getPlugins()).build().execute();
		assertTrue(executed.getValue());

		// precondition: testPluginData is null
		TestPluginData nullTestPluginData = null;
		ContractException contractException = assertThrows(ContractException.class,
				() -> TestPluginFactory.factory(nullTestPluginData));
		assertEquals(NucleusError.NULL_PLUGIN_DATA, contractException.getErrorType());

	}

	@Test
	@UnitTestMethod(target = TestPluginFactory.Factory.class, name = "getPlugins", args = {})
	public void testGetPlugins() {

		List<Plugin> plugins = TestPluginFactory.factory((c) -> {
		}).getPlugins();
		assertEquals(1, plugins.size());

		Plugin testPlugin = null;

		for (Plugin plugin : plugins) {
			if (plugin.getPluginId().equals(TestPluginId.PLUGIN_ID)) {
				assertNull(testPlugin);
				testPlugin = plugin;
			}
		}

		assertNotNull(testPlugin);
	}
}
