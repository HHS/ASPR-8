package plugins.partitions.testsupport;

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
import plugins.partitions.PartitionsPlugin;
import plugins.partitions.PartitionsPluginId;
import plugins.partitions.support.PartitionError;
import plugins.partitions.testsupport.PartitionsTestPluginFactory.Factory;
import plugins.partitions.testsupport.attributes.AttributesPluginData;
import plugins.partitions.testsupport.attributes.AttributesPluginId;
import plugins.partitions.testsupport.attributes.support.AttributeError;
import plugins.partitions.testsupport.attributes.support.AttributeId;
import plugins.partitions.testsupport.attributes.support.TestAttributeId;
import plugins.people.PeoplePluginData;
import plugins.people.PeoplePluginId;
import plugins.people.support.PersonError;
import plugins.people.support.PersonId;
import plugins.stochastics.StochasticsPluginData;
import plugins.stochastics.StochasticsPluginId;
import plugins.stochastics.support.StochasticsError;
import plugins.stochastics.support.WellState;
import plugins.stochastics.testsupport.TestRandomGeneratorId;
import util.annotations.UnitTestMethod;
import util.errors.ContractException;
import util.wrappers.MutableBoolean;

public class AT_PartitionsTestPluginFactory {

	@Test
	@UnitTestMethod(target = PartitionsTestPluginFactory.class, name = "factory", args = { int.class, long.class,
			Consumer.class })
	public void testFtestFactory_Consumeractory1() {
		MutableBoolean executed = new MutableBoolean();
		Factory factory = PartitionsTestPluginFactory.factory(100, 9029198675932589278L, c -> executed.setValue(true));
		TestSimulation.builder().addPlugins(factory.getPlugins()).build().execute();
		assertTrue(executed.getValue());

		// precondition: consumer is null
		Consumer<ActorContext> nullConsumer = null;
		ContractException contractException = assertThrows(ContractException.class,
				() -> PartitionsTestPluginFactory.factory(0, 0, nullConsumer));
		assertEquals(NucleusError.NULL_ACTOR_CONTEXT_CONSUMER, contractException.getErrorType());
	}

	@Test
	@UnitTestMethod(target = PartitionsTestPluginFactory.class, name = "factory", args = { int.class, long.class,
			TestPluginData.class })
	public void testFactory_TestPluginData() {
		MutableBoolean executed = new MutableBoolean();
		TestPluginData.Builder pluginBuilder = TestPluginData.builder();
		pluginBuilder.addTestActorPlan("actor", new TestActorPlan(0, c -> executed.setValue(true)));
		TestPluginData testPluginData = pluginBuilder.build();

		
		Factory factory = PartitionsTestPluginFactory.factory(100, 2990359774692004249L, testPluginData);
		TestSimulation.builder().addPlugins(factory.getPlugins()).build().execute();
		
		assertTrue(executed.getValue());

		// precondition: testPluginData is null
		TestPluginData nullTestPluginData = null;
		ContractException contractException = assertThrows(ContractException.class,
				() -> PartitionsTestPluginFactory.factory(0, 0, nullTestPluginData));
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
	@UnitTestMethod(target = PartitionsTestPluginFactory.Factory.class, name = "getPlugins", args = {})
	public void testGetPlugins() {
		List<Plugin> plugins = PartitionsTestPluginFactory.factory(0, 0, t -> {
		}).getPlugins();
		assertEquals(5, plugins.size());

		checkPluginExists(plugins, AttributesPluginId.PLUGIN_ID);
		checkPluginExists(plugins, PartitionsPluginId.PLUGIN_ID);
		checkPluginExists(plugins, PeoplePluginId.PLUGIN_ID);
		checkPluginExists(plugins, StochasticsPluginId.PLUGIN_ID);
		checkPluginExists(plugins, TestPluginId.PLUGIN_ID);
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

		checkPluginDataExists(plugins, attributesPluginData, AttributesPluginId.PLUGIN_ID);

		// precondition: attributesPluginData is not null
		ContractException contractException = assertThrows(ContractException.class,
				() -> PartitionsTestPluginFactory.factory(0, 0, t -> {
				}).setAttributesPluginData(null));
		assertEquals(AttributeError.NULL_ATTRIBUTES_PLUGIN_DATA, contractException.getErrorType());
	}

	@Test
	@UnitTestMethod(target = PartitionsTestPluginFactory.Factory.class, name = "setPartitionsPlugin", args = {
			Plugin.class })
	public void testSetPartitionsPlugin() {
		Plugin partitionsPlugin = PartitionsPlugin.getPartitionsPlugin(AttributesPluginId.PLUGIN_ID);

		List<Plugin> plugins = PartitionsTestPluginFactory.factory(0, 0, t -> {
		}).setPartitionsPlugin(partitionsPlugin).getPlugins();

		Plugin actualPlugin = checkPluginExists(plugins, PartitionsPluginId.PLUGIN_ID);
		assertTrue(partitionsPlugin == actualPlugin);

		// precondition: partitionsPlugin is not null
		ContractException contractException = assertThrows(ContractException.class,
				() -> PartitionsTestPluginFactory.factory(0, 0, t -> {
				}).setPartitionsPlugin(null));
		assertEquals(PartitionError.NULL_PARTITION_PLUGIN, contractException.getErrorType());
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

		checkPluginDataExists(plugins, peoplePluginData, PeoplePluginId.PLUGIN_ID);

		// precondition: peoplePluginData is not null
		ContractException contractException = assertThrows(ContractException.class,
				() -> PartitionsTestPluginFactory.factory(0, 0, t -> {
				}).setPeoplePluginData(null));
		assertEquals(PersonError.NULL_PEOPLE_PLUGIN_DATA, contractException.getErrorType());
	}

	@Test
	@UnitTestMethod(target = PartitionsTestPluginFactory.Factory.class, name = "setStochasticsPluginData", args = {
			StochasticsPluginData.class })
	public void testSetStochasticsPluginData() {
		StochasticsPluginData.Builder builder = StochasticsPluginData.builder();

		WellState wellState = WellState.builder().setSeed(2990359774692004249L).build();
		builder.setMainRNG(wellState);
		
		wellState = WellState.builder().setSeed(450787180090162111L).build();
		builder.addRNG(TestRandomGeneratorId.BLITZEN,wellState);

		StochasticsPluginData stochasticsPluginData = builder.build();

		List<Plugin> plugins = PartitionsTestPluginFactory.factory(0, 0, t -> {
		}).setStochasticsPluginData(stochasticsPluginData).getPlugins();

		checkPluginDataExists(plugins, stochasticsPluginData, StochasticsPluginId.PLUGIN_ID);

		// precondition: stochasticsPluginData is not null
		ContractException contractException = assertThrows(ContractException.class,
				() -> PartitionsTestPluginFactory.factory(0, 0, t -> {
				}).setStochasticsPluginData(null));
		assertEquals(StochasticsError.NULL_STOCHASTICS_PLUGIN_DATA, contractException.getErrorType());
	}

	@Test
	@UnitTestMethod(target = PartitionsTestPluginFactory.class, name = "getStandardAttributesPluginData", args = {})
	public void testGetStandardAttributesPluginData() {

		AttributesPluginData attributesPluginData = PartitionsTestPluginFactory.getStandardAttributesPluginData();

		Set<TestAttributeId> expectedAttributeIds = EnumSet.allOf(TestAttributeId.class);
		assertFalse(expectedAttributeIds.isEmpty());

		Set<AttributeId> actualAttributeIds = attributesPluginData.getAttributeIds();
		assertEquals(expectedAttributeIds, actualAttributeIds);

		for (TestAttributeId testAttributeId : expectedAttributeIds) {
			assertEquals(testAttributeId.getAttributeDefinition(),
					attributesPluginData.getAttributeDefinition(testAttributeId));
		}
	}

	@Test
	@UnitTestMethod(target = PartitionsTestPluginFactory.class, name = "getStandardPartitionsPlugin", args = {})
	public void testGetStandardPartitionsPlugin() {

		Plugin partitionsPlugin = PartitionsTestPluginFactory.getStandardPartitionsPlugin();
		assertTrue(partitionsPlugin.getPluginDependencies().contains(AttributesPluginId.PLUGIN_ID));
	}

	@Test
	@UnitTestMethod(target = PartitionsTestPluginFactory.class, name = "getStandardPeoplePluginData", args = {
			int.class })
	public void testGetStandardPeoplePluginData() {

		PeoplePluginData peoplePluginData = PartitionsTestPluginFactory.getStandardPeoplePluginData(100);
		assertEquals(100, peoplePluginData.getPersonIds().size());
	}

	@Test
	@UnitTestMethod(target = PartitionsTestPluginFactory.class, name = "getStandardStochasticsPluginData", args = {
			long.class })
	public void testGetStandardStochasticsPluginData() {
		long seed = 7995349318419680542L;
		StochasticsPluginData stochasticsPluginData = PartitionsTestPluginFactory
				.getStandardStochasticsPluginData(seed);

		assertEquals(seed, stochasticsPluginData.getWellState().getSeed());
		assertEquals(0, stochasticsPluginData.getRandomNumberGeneratorIds().size());
	}
}
