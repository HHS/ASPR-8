package plugins.stochastics.testsupport;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

import org.junit.jupiter.api.Test;

import nucleus.ActorContext;
import nucleus.Plugin;
import nucleus.PluginData;
import nucleus.PluginId;
import nucleus.testsupport.testplugin.TestActorPlan;
import nucleus.testsupport.testplugin.TestPlanDataManager;
import nucleus.testsupport.testplugin.TestPluginData;
import nucleus.testsupport.testplugin.TestPluginId;
import nucleus.testsupport.testplugin.TestSimulation;
import plugins.stochastics.StochasticsDataManager;
import plugins.stochastics.StochasticsPluginData;
import plugins.stochastics.StochasticsPluginId;
import plugins.stochastics.support.RandomNumberGeneratorId;
import tools.annotations.UnitTestMethod;
import util.wrappers.MutableBoolean;

public class AT_StochasticsTestPluginFactory {

	/**
	 * Convience method to create a consumer to facilitate testing the factory
	 * methods
	 * {@link AT_StochasticsTestPluginFactory#testFactory_Consumer()}
	 * and
	 * {@link AT_StochasticsTestPluginFactory#testFactory_TestPluginData()}
	 * 
	 * <li>either for passing directly to
	 * <li>{@link StochasticsTestPluginFactory#factory(long, Consumer)}
	 * <li>or indirectly via creating a TestPluginData and passing it to
	 * <li>{@link StochasticsTestPluginFactory#factory(long, TestPluginData)}
	 * </p>
	 * 
	 * @param executed boolean to set once the consumer completes
	 * @return the consumer
	 * 
	 */
	private Consumer<ActorContext> factoryConsumer(MutableBoolean executed) {
		return (c) -> {

			StochasticsDataManager stochasticsDataManager = c.getDataManager(StochasticsDataManager.class);
			assertNotNull(stochasticsDataManager);
			assertFalse(stochasticsDataManager.getRandomNumberGeneratorIds().isEmpty());

			TestPlanDataManager testDataManager = c.getDataManager(TestPlanDataManager.class);
			assertNotNull(testDataManager);

			executed.setValue(true);
		};
	}

	@Test
	@UnitTestMethod(target = StochasticsTestPluginFactory.class, name = "factory", args = { long.class,
			Consumer.class })
	public void testFactory_Consumer() {
		MutableBoolean actorExecuted = new MutableBoolean();
		TestSimulation.executeSimulation(
				StochasticsTestPluginFactory.factory(576570479777898470L, factoryConsumer(actorExecuted)).getPlugins());
		assertTrue(actorExecuted.getValue());
	}

	@Test
	@UnitTestMethod(target = StochasticsTestPluginFactory.class, name = "testConsumers", args = { long.class,
			TestPluginData.class })
	public void testFactory_TestPluginData() {
		MutableBoolean actorExecuted = new MutableBoolean();

		TestPluginData.Builder builder = TestPluginData.builder();
		builder.addTestActorPlan("actor", new TestActorPlan(0, factoryConsumer(actorExecuted)));
		TestPluginData testPluginData = builder.build();
		TestSimulation.executeSimulation(
				StochasticsTestPluginFactory.factory(45235233432345378L, testPluginData).getPlugins());
		assertTrue(actorExecuted.getValue());
	}

	@Test
	@UnitTestMethod(target = StochasticsTestPluginFactory.Factory.class, name = "getPlugins", args = {})
	public void testGetPlugins() {

		List<Plugin> plugins = StochasticsTestPluginFactory.factory(3626443405517810332L, t -> {
		}).getPlugins();
		assertEquals(2, plugins.size());

		Plugin stocasticsPlugin = null;
		Plugin testPlugin = null;

		for (Plugin plugin : plugins) {
			if (plugin.getPluginId().equals(StochasticsPluginId.PLUGIN_ID)) {
				assertNull(stocasticsPlugin);
				stocasticsPlugin = plugin;
			}
			if (plugin.getPluginId().equals(TestPluginId.PLUGIN_ID)) {
				assertNull(testPlugin);
				testPlugin = plugin;
			}
		}
		assertNotNull(stocasticsPlugin);
		assertNotNull(testPlugin);
	}

	private <T extends PluginData> void checkPlugins(List<Plugin> plugins, T expectedPluginData, PluginId pluginId) {
		Plugin actualPlugin = null;
		for (Plugin plugin : plugins) {
			if (plugin.getPluginId().equals(pluginId)) {
				assertNull(actualPlugin);
				actualPlugin = plugin;
			}
		}

		assertNotNull(actualPlugin);
		Set<PluginData> actualPluginDatas = actualPlugin.getPluginDatas();
		assertNotNull(actualPluginDatas);
		assertEquals(1, actualPluginDatas.size());
		PluginData actualPluginData = actualPluginDatas.stream().toList().get(0);
		assertTrue(expectedPluginData == actualPluginData);
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

		checkPlugins(plugins, stochasticsPluginData, StochasticsPluginId.PLUGIN_ID);
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
