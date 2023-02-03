package plugins.people.testsupport;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

import org.junit.jupiter.api.Test;

import nucleus.ActorContext;
import nucleus.Plugin;
import nucleus.PluginData;
import nucleus.PluginId;
import nucleus.testsupport.testplugin.TestActorPlan;
import nucleus.testsupport.testplugin.TestPluginData;
import nucleus.testsupport.testplugin.TestSimulation;
import plugins.people.PeoplePluginData;
import plugins.people.PeoplePluginId;
import plugins.people.datamanagers.PeopleDataManager;
import plugins.people.support.PersonId;
import plugins.stochastics.StochasticsPluginData;
import plugins.stochastics.StochasticsPluginId;
import plugins.stochastics.testsupport.TestRandomGeneratorId;
import tools.annotations.UnitTestMethod;
import util.random.RandomGeneratorProvider;
import util.wrappers.MutableBoolean;

public class AT_PeopleTestPluginFactory {

	/**
	 * Convience method to create a consumer to facilitate testing the factory
	 * methods
	 * {@link AT_PeopleTestPluginFactory#testFactory_Consumer()}
	 * and
	 * {@link AT_PeopleTestPluginFactory#testFactory_TestPluginData()}
	 * 
	 * <li>either for passing directly to
	 * <li>{@link PeopleTestPluginFactory#factory(long, Consumer)}
	 * <li>or indirectly via creating a TestPluginData and passing it to
	 * <li>{@link PeopleTestPluginFactory#factory(long, TestPluginData)}
	 * 
	 * @param executed boolean to set once the consumer completes
	 * @return the consumer
	 * 
	 */
	private Consumer<ActorContext> factoryConsumer(MutableBoolean executed) {
		return (c) -> {

			PeopleDataManager peopleDataManager = c.getDataManager(PeopleDataManager.class);
			assertEquals(0, peopleDataManager.getPeople().size());

			executed.setValue(true);
		};
	}

	@Test
	@UnitTestMethod(target = PeopleTestPluginFactory.class, name = "factory", args = { long.class, Consumer.class })
	public void testFactory_Consumer() {
		MutableBoolean executed = new MutableBoolean();
		TestSimulation.executeSimulation(PeopleTestPluginFactory
				.factory(6489240163414718858L, factoryConsumer(executed)).getPlugins());
		assertTrue(executed.getValue());
	}

	@Test
	@UnitTestMethod(target = PeopleTestPluginFactory.class, name = "factory", args = { long.class,
			TestPluginData.class })
	public void testFactory_TestPluginData() {
		MutableBoolean executed = new MutableBoolean();
		TestPluginData.Builder pluginBuilder = TestPluginData.builder();
		pluginBuilder.addTestActorPlan("actor", new TestActorPlan(0, factoryConsumer(executed)));
		TestPluginData testPluginData = pluginBuilder.build();

		TestSimulation.executeSimulation(
				PeopleTestPluginFactory.factory(3745668053390022091L, testPluginData).getPlugins());
		assertTrue(executed.getValue());

	}

	@Test
	@UnitTestMethod(target = PeopleTestPluginFactory.Factory.class, name = "getPlugins", args = {})
	public void testGetPlugins() {
		assertEquals(3, PeopleTestPluginFactory.factory(0, t -> {
		}).getPlugins().size());
	}

	private <T extends PluginData> void checkPlugins(List<Plugin> plugins, T expectedPluginData, PluginId pluginId) {
		Plugin actualPlugin = null;
		for(Plugin plugin : plugins) {
			if(plugin.getPluginId().equals(pluginId)) {
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
	@UnitTestMethod(target = PeopleTestPluginFactory.Factory.class, name = "setPeoplePluginData", args = {
			PeoplePluginData.class })
	public void testSetPeoplePluginData() {
		PeoplePluginData.Builder builder = PeoplePluginData.builder();

		for (int i = 0; i < 100; i++) {
			builder.addPersonId(new PersonId(i));
		}

		PeoplePluginData peoplePluginData = builder.build();

		List<Plugin> plugins = PeopleTestPluginFactory
				.factory(0, t -> {
				})
				.setPeoplePluginData(peoplePluginData)
				.getPlugins();

		checkPlugins(plugins, peoplePluginData, PeoplePluginId.PLUGIN_ID);
	}

	@Test
	@UnitTestMethod(target = PeopleTestPluginFactory.Factory.class, name = "setStochasticsPluginData", args = {
			StochasticsPluginData.class })
	public void testSetStochasticsPluginData() {
		StochasticsPluginData.Builder builder = StochasticsPluginData.builder();

		builder.setSeed(2758378374654665699L).addRandomGeneratorId(TestRandomGeneratorId.BLITZEN);

		StochasticsPluginData stochasticsPluginData = builder.build();

		List<Plugin> plugins = PeopleTestPluginFactory
				.factory(0, t -> {
				})
				.setStochasticsPluginData(stochasticsPluginData)
				.getPlugins();

		checkPlugins(plugins, stochasticsPluginData, StochasticsPluginId.PLUGIN_ID);
	}

	@Test
	@UnitTestMethod(target = PeopleTestPluginFactory.class, name = "getStandardPeoplePluginData", args = {})
	public void testGetStandardPeoplePluginData() {

		PeoplePluginData peoplePluginData = PeopleTestPluginFactory.getStandardPeoplePluginData();

		assertEquals(0, peoplePluginData.getPersonIds().size());
	}

	@Test
	@UnitTestMethod(target = PeopleTestPluginFactory.class, name = "getStandardStochasticsPluginData", args = {
			long.class })
	public void testGetStandardStochasticsPluginData() {
		long seed = 6072871729256538807L;
		StochasticsPluginData stochasticsPluginData = PeopleTestPluginFactory
				.getStandardStochasticsPluginData(seed);

		assertEquals(RandomGeneratorProvider.getRandomGenerator(seed).nextLong(), stochasticsPluginData.getSeed());
		assertEquals(0, stochasticsPluginData.getRandomNumberGeneratorIds().size());
	}

}