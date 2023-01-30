package plugins.partitions.testsupport;

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
import plugins.materials.MaterialsPluginId;
import plugins.partitions.PartitionsPlugin;
import plugins.partitions.testsupport.attributes.AttributesPluginData;
import plugins.partitions.testsupport.attributes.AttributesPluginId;
import plugins.partitions.testsupport.attributes.support.TestAttributeId;
import plugins.people.PeoplePluginData;
import plugins.people.support.PersonId;
import plugins.stochastics.StochasticsPluginData;
import plugins.stochastics.testsupport.TestRandomGeneratorId;
import tools.annotations.UnitTestMethod;
import util.random.RandomGeneratorProvider;
import util.wrappers.MutableBoolean;

public class AT_PartitionsTestPluginFactory {

	private Consumer<ActorContext> factoryConsumer(MutableBoolean executed) {
		return (c) -> {

			// TODO: add checks

			executed.setValue(true);
		};
	}

	@Test
	@UnitTestMethod(target = PartitionsTestPluginFactory.class, name = "factory", args = { int.class, long.class,
			Consumer.class })
	public void testFactory1() {
		MutableBoolean executed = new MutableBoolean();
		TestSimulation.executeSimulation(PartitionsTestPluginFactory
				.factory(0, 9029198675932589278L, factoryConsumer(executed)).getPlugins());
		assertTrue(executed.getValue());
	}

	@Test
	@UnitTestMethod(target = PartitionsTestPluginFactory.class, name = "factory", args = { int.class, long.class,
			TestPluginData.class })
	public void testFactory2() {
		MutableBoolean executed = new MutableBoolean();
		TestPluginData.Builder pluginBuilder = TestPluginData.builder();
		pluginBuilder.addTestActorPlan("actor", new TestActorPlan(0, factoryConsumer(executed)));
		TestPluginData testPluginData = pluginBuilder.build();

		TestSimulation.executeSimulation(
				PartitionsTestPluginFactory.factory(0, 2990359774692004249L, testPluginData).getPlugins());
		assertTrue(executed.getValue());

	}

	@Test
	@UnitTestMethod(target = PartitionsTestPluginFactory.Factory.class, name = "getPlugins", args = {})
	public void testGetPlugins() {
		assertEquals(5, PartitionsTestPluginFactory.factory(0, 0, t -> {
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
	@UnitTestMethod(target = PartitionsTestPluginFactory.Factory.class, name = "setAttributesPluginData", args = {
			AttributesPluginData.class })
	public void testSetAttributesPluginData() {
		AttributesPluginData.Builder attributesBuilder = AttributesPluginData.builder();
		for (TestAttributeId testAttributeId : TestAttributeId.values()) {
			attributesBuilder.defineAttribute(testAttributeId, testAttributeId.getAttributeDefinition());
		}

		AttributesPluginData attributesPluginData = attributesBuilder.build();

		List<Plugin> plugins = PartitionsTestPluginFactory.factory(0, 0, t -> {
		}).setAttributesPluginData(attributesPluginData).getPlugins();

		checkPlugins(plugins, attributesPluginData);

	}

	@Test
	@UnitTestMethod(target = PartitionsTestPluginFactory.Factory.class, name = "setPartitionsPlugin", args = {
			Plugin.class })
	public void testSetPartitionsPlugin() {
		Plugin partitionsPlugin = PartitionsPlugin.getPartitionsPlugin(AttributesPluginId.PLUGIN_ID,
				MaterialsPluginId.PLUGIN_ID);

		List<Plugin> plugins = PartitionsTestPluginFactory.factory(0, 0, t -> {
		}).setPartitionsPlugin(partitionsPlugin).getPlugins();

		assertTrue(plugins.contains(partitionsPlugin));

	}

	@Test
	@UnitTestMethod(target = PartitionsTestPluginFactory.Factory.class, name = "setPeoplePluginData", args = {
			PeoplePluginData.class })
	public void testSetPeoplePluginData() {
		PeoplePluginData.Builder builder = PeoplePluginData.builder();

		for (int i = 0; i < 100; i++) {
			builder.addPersonId(new PersonId(i));
		}

		PeoplePluginData peoplePluginData = builder.build();

		List<Plugin> plugins = PartitionsTestPluginFactory.factory(0, 0, t -> {
		}).setPeoplePluginData(peoplePluginData).getPlugins();

		checkPlugins(plugins, peoplePluginData);

	}

	@Test
	@UnitTestMethod(target = PartitionsTestPluginFactory.Factory.class, name = "setStochasticsPluginData", args = {
			StochasticsPluginData.class })
	public void testSetStochasticsPluginData() {
		StochasticsPluginData.Builder builder = StochasticsPluginData.builder();

		builder.setSeed(2990359774692004249L).addRandomGeneratorId(TestRandomGeneratorId.BLITZEN);

		StochasticsPluginData stochasticsPluginData = builder.build();

		List<Plugin> plugins = PartitionsTestPluginFactory.factory(0, 0, t -> {
		}).setStochasticsPluginData(stochasticsPluginData).getPlugins();

		checkPlugins(plugins, stochasticsPluginData);
	}

	@Test
	@UnitTestMethod(target = PartitionsTestPluginFactory.class, name = "getStandardAttributesPluginData", args = {})
	public void testGetStandardAttributesPluginData() {

		AttributesPluginData attributesPluginData = PartitionsTestPluginFactory.getStandardAttributesPluginData();
		assertNotNull(attributesPluginData);

		for (TestAttributeId testAttributeId : TestAttributeId.values()) {
			assertTrue(attributesPluginData.getAttributeIds().contains(testAttributeId));
			assertEquals(testAttributeId.getAttributeDefinition(),
					attributesPluginData.getAttributeDefinition(testAttributeId));
		}
	}

	@Test
	@UnitTestMethod(target = PartitionsTestPluginFactory.class, name = "getStandardPartitionsPlugin", args = {})
	public void testGetStandardPartitionsPlugin() {

		Plugin partitionsPlugin = PartitionsTestPluginFactory.getStandardPartitionsPlugin();
		assertNotNull(partitionsPlugin);
		assertTrue(partitionsPlugin.getPluginDependencies().contains(AttributesPluginId.PLUGIN_ID));
	}

	@Test
	@UnitTestMethod(target = PartitionsTestPluginFactory.class, name = "getStandardPeoplePluginData", args = {
			int.class })
	public void testGetStandardPeoplePluginData() {

		PeoplePluginData peoplePluginData = PartitionsTestPluginFactory.getStandardPeoplePluginData(100);
		assertNotNull(peoplePluginData);
		assertEquals(100, peoplePluginData.getPersonIds().size());
	}

	@Test
	@UnitTestMethod(target = PartitionsTestPluginFactory.class, name = "getStandardStochasticsPluginData", args = {
			long.class })
	public void testGetStandardStochasticsPluginData() {
		long seed = 7995349318419680542L;
		StochasticsPluginData stochasticsPluginData = PartitionsTestPluginFactory
				.getStandardStochasticsPluginData(seed);

		assertEquals(RandomGeneratorProvider.getRandomGenerator(seed).nextLong(), stochasticsPluginData.getSeed());
		assertEquals(0, stochasticsPluginData.getRandomNumberGeneratorIds().size());
	}
}