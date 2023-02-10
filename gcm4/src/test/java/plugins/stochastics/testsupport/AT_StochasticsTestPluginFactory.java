package plugins.stochastics.testsupport;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

import org.junit.jupiter.api.Test;

import nucleus.ActorContext;
import nucleus.NucleusError;
import nucleus.Plugin;
import nucleus.PluginData;
import nucleus.PluginId;
import nucleus.testsupport.testplugin.TestActorPlan;
import nucleus.testsupport.testplugin.TestPluginData;
import nucleus.testsupport.testplugin.TestPluginId;
import nucleus.testsupport.testplugin.TestSimulation;
import plugins.stochastics.StochasticsPluginData;
import plugins.stochastics.StochasticsPluginId;
import plugins.stochastics.support.RandomNumberGeneratorId;
import plugins.stochastics.support.StochasticsError;
import tools.annotations.UnitTestMethod;
import util.errors.ContractException;
import util.wrappers.MutableBoolean;

public class AT_StochasticsTestPluginFactory {

	@Test
	@UnitTestMethod(target = StochasticsTestPluginFactory.class, name = "factory", args = { long.class,
			Consumer.class })
	public void testFactory_Consumer() {
		MutableBoolean executed = new MutableBoolean();
		TestSimulation.executeSimulation(
				StochasticsTestPluginFactory.factory(576570479777898470L, c -> executed.setValue(true)).getPlugins());
		assertTrue(executed.getValue());

		// precondition: consumer is null
		Consumer<ActorContext> nullConsumer = null;
		ContractException contractException = assertThrows(ContractException.class,
				() -> StochasticsTestPluginFactory.factory(0, nullConsumer));
		assertEquals(NucleusError.NULL_ACTOR_CONTEXT_CONSUMER, contractException.getErrorType());
	}

	@Test
	@UnitTestMethod(target = StochasticsTestPluginFactory.class, name = "testConsumers", args = { long.class,
			TestPluginData.class })
	public void testFactory_TestPluginData() {
		MutableBoolean executed = new MutableBoolean();

		TestPluginData.Builder builder = TestPluginData.builder();
		builder.addTestActorPlan("actor", new TestActorPlan(0, c -> executed.setValue(true)));
		TestPluginData testPluginData = builder.build();
		TestSimulation.executeSimulation(
				StochasticsTestPluginFactory.factory(45235233432345378L, testPluginData).getPlugins());
		assertTrue(executed.getValue());

		// precondition: testPluginData is null
		TestPluginData nullTestPluginData = null;
		ContractException contractException = assertThrows(ContractException.class,
				() -> StochasticsTestPluginFactory.factory(0, nullTestPluginData));
		assertEquals(NucleusError.NULL_PLUGIN_DATA, contractException.getErrorType());
	}

	/*
	 * Given a list of plugins, will show that the plugin with the given pluginId
	 * exists, and exists EXACTLY once.
	 */
	private Plugin checkPluginExists(List<Plugin> plugins, PluginId pluginId) {
		Plugin actualPlugin = null;
		for (Plugin plugin : plugins) {
			if (plugin.getPluginId().equals(pluginId)) {
				assertNull(actualPlugin);
				actualPlugin = plugin;
			}
		}

		assertNotNull(actualPlugin);

		return actualPlugin;
	}

	/**
	 * Given a list of plugins, will show that the explicit plugindata for the given
	 * pluginid exists, and exists EXACTLY once.
	 */
	private <T extends PluginData> void checkPluginDataExists(List<Plugin> plugins, T expectedPluginData,
			PluginId pluginId) {
		Plugin actualPlugin = checkPluginExists(plugins, pluginId);
		Set<PluginData> actualPluginDatas = actualPlugin.getPluginDatas();
		assertNotNull(actualPluginDatas);
		assertEquals(1, actualPluginDatas.size());
		PluginData actualPluginData = actualPluginDatas.stream().toList().get(0);
		assertTrue(expectedPluginData == actualPluginData);
	}

	@Test
	@UnitTestMethod(target = StochasticsTestPluginFactory.Factory.class, name = "getPlugins", args = {})
	public void testGetPlugins() {

		List<Plugin> plugins = StochasticsTestPluginFactory.factory(3626443405517810332L, t -> {
		}).getPlugins();
		assertEquals(2, plugins.size());

		checkPluginExists(plugins, StochasticsPluginId.PLUGIN_ID);
		checkPluginExists(plugins, TestPluginId.PLUGIN_ID);
	}

	@Test
	@UnitTestMethod(target = StochasticsTestPluginFactory.Factory.class, name = "setStochasticsPluginData", args = {
			StochasticsPluginData.class })
	public void testSetStochasticsPluginData() {
		StochasticsPluginData.Builder builder = StochasticsPluginData.builder();

		builder.setSeed(2990359774692004249L).addRandomGeneratorId(TestRandomGeneratorId.BLITZEN);

		StochasticsPluginData stochasticsPluginData = builder.build();

		List<Plugin> plugins = StochasticsTestPluginFactory.factory(5433603767451466687L, t -> {
		}).setStochasticsPluginData(stochasticsPluginData).getPlugins();

		checkPluginDataExists(plugins, stochasticsPluginData, StochasticsPluginId.PLUGIN_ID);

		// precondition: stochasticsPluginData is not null
		ContractException contractException = assertThrows(ContractException.class,
				() -> StochasticsTestPluginFactory.factory(5433603767451466687L, t -> {
				}).setStochasticsPluginData(null));
		assertEquals(StochasticsError.NULL_STOCHASTICS_PLUGIN_DATA, contractException.getErrorType());
	}

	@Test
	@UnitTestMethod(target = StochasticsTestPluginFactory.class, name = "getStandardStochasticsPluginData", args = {
			long.class })
	public void testGetStandardStochasticsPluginData() {
		long seed = 6072871729256538807L;
		StochasticsPluginData stochasticsPluginData = StochasticsTestPluginFactory
				.getStandardStochasticsPluginData(seed);

		assertEquals(seed, stochasticsPluginData.getSeed());

		Set<TestRandomGeneratorId> expectedRandomGeneratorIds = EnumSet.allOf(TestRandomGeneratorId.class);
		assertFalse(expectedRandomGeneratorIds.isEmpty());

		Set<RandomNumberGeneratorId> actualsGeneratorIds = stochasticsPluginData.getRandomNumberGeneratorIds();

		assertEquals(expectedRandomGeneratorIds, actualsGeneratorIds);
	}
}
