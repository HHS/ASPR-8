package plugins.materials.testsupport;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

import org.junit.jupiter.api.Test;

import nucleus.ActorContext;
import nucleus.Plugin;
import nucleus.PluginData;
import nucleus.testsupport.testplugin.TestActorPlan;
import nucleus.testsupport.testplugin.TestPluginData;
import nucleus.testsupport.testplugin.TestSimulation;
import plugins.materials.MaterialsPluginData;
import plugins.people.PeoplePluginData;
import plugins.people.support.PersonId;
import plugins.regions.RegionsPluginData;
import plugins.resources.ResourcesPluginData;
import plugins.stochastics.StochasticsPluginData;
import plugins.stochastics.testsupport.TestRandomGeneratorId;
import tools.annotations.UnitTestMethod;
import util.random.RandomGeneratorProvider;
import util.wrappers.MutableBoolean;

public class AT_MaterialsTestPluginFactory {

	private Consumer<ActorContext> factoryConsumer(MutableBoolean executed) {
		return (c) -> {

			// TODO: add checks

			executed.setValue(true);
		};
	}

	@Test
	@UnitTestMethod(target = MaterialsTestPluginFactory.class, name = "factory", args = { int.class, int.class,
			int.class, long.class, Consumer.class })
	public void testFactory1() {
		MutableBoolean executed = new MutableBoolean();
		TestSimulation.executeSimulation(MaterialsTestPluginFactory
				.factory(0, 0, 0, 3328026739613106739L, factoryConsumer(executed)).getPlugins());
		assertTrue(executed.getValue());
	}

	@Test
	@UnitTestMethod(target = MaterialsTestPluginFactory.class, name = "factory", args = { int.class, int.class,
			int.class, long.class, TestPluginData.class })
	public void testFactory2() {
		MutableBoolean executed = new MutableBoolean();
		TestPluginData.Builder pluginBuilder = TestPluginData.builder();
		pluginBuilder.addTestActorPlan("actor", new TestActorPlan(0, factoryConsumer(executed)));
		TestPluginData testPluginData = pluginBuilder.build();

		TestSimulation.executeSimulation(
				MaterialsTestPluginFactory.factory(0, 0, 0, 7995349318419680542L, testPluginData).getPlugins());
		assertTrue(executed.getValue());

	}

	@Test
	@UnitTestMethod(target = MaterialsTestPluginFactory.Factory.class, name = "getPlugins", args = {})
	public void testGetPlugins() {
		assertEquals(6, MaterialsTestPluginFactory.factory(0, 0, 0, 0, t -> {
		}).getPlugins().size());
	}

	private <T extends PluginData> void checkPlugins(List<Plugin> plugins, T expectedPluginData) {
		Class<?> classRef = expectedPluginData.getClass();
		plugins.forEach((plugin) -> {
			Set<PluginData> pluginDatas = plugin.getPluginDatas();
			if(pluginDatas.size() > 0) {
				PluginData pluginData = pluginDatas.toArray(new PluginData[0])[0];
				if (classRef.isAssignableFrom(pluginData.getClass())) {
					assertEquals(expectedPluginData, classRef.cast(pluginData));
				} else {
					assertNotEquals(expectedPluginData, pluginData);
				}
			}
		});
	}

	@Test
	@UnitTestMethod(target = MaterialsTestPluginFactory.Factory.class, name = "setMaterialsPluginData", args = {
			MaterialsPluginData.class })
	public void testSetMaterialsPluginData() {
		MaterialsPluginData.Builder builder = MaterialsPluginData.builder();

		// TODO: add stuff to builder

		MaterialsPluginData materialsPluginData = builder.build();

		List<Plugin> plugins = MaterialsTestPluginFactory.factory(0, 0, 0, 0, t -> {
		}).setMaterialsPluginData(materialsPluginData).getPlugins();

		checkPlugins(plugins, materialsPluginData);

	}

	@Test
	@UnitTestMethod(target = MaterialsTestPluginFactory.Factory.class, name = "setResourcesPluginData", args = {
			ResourcesPluginData.class })
	public void testSetResourcesPluginData() {
		ResourcesPluginData.Builder builder = ResourcesPluginData.builder();

		// TODO: add stuff to builder

		ResourcesPluginData resourcesPluginData = builder.build();

		List<Plugin> plugins = MaterialsTestPluginFactory.factory(0, 0, 0, 0, t -> {
		}).setResourcesPluginData(resourcesPluginData).getPlugins();

		checkPlugins(plugins, resourcesPluginData);

	}

	@Test
	@UnitTestMethod(target = MaterialsTestPluginFactory.Factory.class, name = "setRegionsPluginData", args = {
			RegionsPluginData.class })
	public void testSetRegionsPluginData() {
		RegionsPluginData.Builder builder = RegionsPluginData.builder();

		// TODO: add stuff to builder

		RegionsPluginData regionsPluginData = builder.build();

		List<Plugin> plugins = MaterialsTestPluginFactory.factory(0, 0, 0, 0, t -> {
		}).setRegionsPluginData(regionsPluginData).getPlugins();

		checkPlugins(plugins, regionsPluginData);

	}

	@Test
	@UnitTestMethod(target = MaterialsTestPluginFactory.Factory.class, name = "setPeoplePluginData", args = {
			PeoplePluginData.class })
	public void testSetPeoplePluginData() {
		PeoplePluginData.Builder builder = PeoplePluginData.builder();

		for (int i = 0; i < 100; i++) {
			builder.addPersonId(new PersonId(i));
		}

		PeoplePluginData peoplePluginData = builder.build();

		List<Plugin> plugins = MaterialsTestPluginFactory.factory(0, 0, 0, 0, t -> {
		}).setPeoplePluginData(peoplePluginData).getPlugins();

		checkPlugins(plugins, peoplePluginData);

	}

	@Test
	@UnitTestMethod(target = MaterialsTestPluginFactory.Factory.class, name = "setStochasticsPluginData", args = {
			StochasticsPluginData.class })
	public void testSetStochasticsPluginData() {
		StochasticsPluginData.Builder builder = StochasticsPluginData.builder();

		builder.setSeed(2990359774692004249L).addRandomGeneratorId(TestRandomGeneratorId.BLITZEN);

		StochasticsPluginData stochasticsPluginData = builder.build();

		List<Plugin> plugins = MaterialsTestPluginFactory.factory(0, 0, 0, 0, t -> {
		}).setStochasticsPluginData(stochasticsPluginData).getPlugins();

		checkPlugins(plugins, stochasticsPluginData);
	}

	@Test
	@UnitTestMethod(target = MaterialsTestPluginFactory.class, name = "getStandardMaterialsPluginData", args = {
			int.class, int.class, int.class, long.class })
	public void testGetStandardMaterialsPluginData() {

		MaterialsPluginData materialsPluginData = MaterialsTestPluginFactory.getStandardMaterialsPluginData(50, 10, 30,
				9029198675932589278L);
		assertNotNull(materialsPluginData);

		// TODO: add additional checks
	}

	@Test
	@UnitTestMethod(target = MaterialsTestPluginFactory.class, name = "getStandardResourcesPluginData", args = {
			long.class })
	public void testGetStandardResourcesPluginData() {

		ResourcesPluginData resourcesPluginData = MaterialsTestPluginFactory
				.getStandardResourcesPluginData(4800551796983227153L);
		assertNotNull(resourcesPluginData);

		// TODO: add additional checks
	}

	@Test
	@UnitTestMethod(target = MaterialsTestPluginFactory.class, name = "getStandardRegionsPluginData", args = {})
	public void testGetStandardRegionsPluginData() {

		RegionsPluginData regionsPluginData = MaterialsTestPluginFactory.getStandardRegionsPluginData();
		assertNotNull(regionsPluginData);

		// TODO: add additional checks
	}

	@Test
	@UnitTestMethod(target = MaterialsTestPluginFactory.class, name = "getStandardPeoplePluginData", args = {})
	public void testGetStandardPeoplePluginData() {

		PeoplePluginData peoplePluginData = MaterialsTestPluginFactory.getStandardPeoplePluginData();
		assertNotNull(peoplePluginData);
	}

	@Test
	@UnitTestMethod(target = MaterialsTestPluginFactory.class, name = "getStandardStochasticsPluginData", args = {
			long.class })
	public void testGetStandardStochasticsPluginData() {
		long seed = 6072871729256538807L;
		StochasticsPluginData stochasticsPluginData = MaterialsTestPluginFactory
				.getStandardStochasticsPluginData(seed);

		assertEquals(RandomGeneratorProvider.getRandomGenerator(seed).nextLong(), stochasticsPluginData.getSeed());
		assertEquals(0, stochasticsPluginData.getRandomNumberGeneratorIds().size());
	}
}
