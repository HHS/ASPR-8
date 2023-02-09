package plugins.globalproperties.testsupport;

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
import plugins.globalproperties.GlobalPropertiesPluginData;
import plugins.globalproperties.GlobalPropertiesPluginId;
import plugins.globalproperties.support.GlobalPropertiesError;
import plugins.globalproperties.support.GlobalPropertyId;
import plugins.globalproperties.support.SimpleGlobalPropertyId;
import plugins.util.properties.PropertyDefinition;
import tools.annotations.UnitTestMethod;
import util.errors.ContractException;
import util.wrappers.MutableBoolean;

public class AT_GlobalPropertiesTestPluginFactory {

	@Test
	@UnitTestMethod(target = GlobalPropertiesTestPluginFactory.class, name = "factory", args = { Consumer.class })
	public void testFactory_Consumer() {
		MutableBoolean executed = new MutableBoolean();
		TestSimulation
				.executeSimulation(
						GlobalPropertiesTestPluginFactory.factory(c -> executed.setValue(true)).getPlugins());
		assertTrue(executed.getValue());

		// precondition: consumer is null
		Consumer<ActorContext> nullConsumer = null;
		ContractException contractException = assertThrows(ContractException.class,
				() -> GlobalPropertiesTestPluginFactory.factory(nullConsumer));
		assertEquals(NucleusError.NULL_ACTOR_CONTEXT_CONSUMER, contractException.getErrorType());
	}

	@Test
	@UnitTestMethod(target = GlobalPropertiesTestPluginFactory.class, name = "factory", args = { TestPluginData.class })
	public void testFactory_TestPluginData() {
		MutableBoolean executed = new MutableBoolean();
		TestPluginData.Builder pluginBuilder = TestPluginData.builder();
		pluginBuilder.addTestActorPlan("actor", new TestActorPlan(0, c -> executed.setValue(true)));
		TestPluginData testPluginData = pluginBuilder.build();

		TestSimulation.executeSimulation(GlobalPropertiesTestPluginFactory.factory(testPluginData).getPlugins());
		assertTrue(executed.getValue());

		// precondition: testPluginData is null
		TestPluginData nullTestPluginData = null;
		ContractException contractException = assertThrows(ContractException.class,
				() -> GlobalPropertiesTestPluginFactory.factory(nullTestPluginData));
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
	@UnitTestMethod(target = GlobalPropertiesTestPluginFactory.Factory.class, name = "getPlugins", args = {})
	public void testGetPlugins() {

		List<Plugin> plugins = GlobalPropertiesTestPluginFactory.factory(t -> {
		}).getPlugins();
		assertEquals(2, plugins.size());

		checkPluginExists(plugins, GlobalPropertiesPluginId.PLUGIN_ID);
		checkPluginExists(plugins, TestPluginId.PLUGIN_ID);
	}

	@Test
	@UnitTestMethod(target = GlobalPropertiesTestPluginFactory.Factory.class, name = "setGlobalPropertiesPluginData", args = {
			GlobalPropertiesPluginData.class })
	public void testSetGlobalPropertiesPluginData() {
		GlobalPropertiesPluginData.Builder initialDatabuilder = GlobalPropertiesPluginData.builder();

		GlobalPropertyId globalPropertyId_1 = new SimpleGlobalPropertyId("id_1");
		PropertyDefinition propertyDefinition = PropertyDefinition.builder().setType(Integer.class).setDefaultValue(3)
				.build();
		initialDatabuilder.defineGlobalProperty(globalPropertyId_1, propertyDefinition);

		GlobalPropertyId globalPropertyId_2 = new SimpleGlobalPropertyId("id_2");
		propertyDefinition = PropertyDefinition.builder().setType(Double.class).setDefaultValue(6.78).build();
		initialDatabuilder.defineGlobalProperty(globalPropertyId_2, propertyDefinition);

		GlobalPropertyId globalPropertyId_3 = new SimpleGlobalPropertyId("id_3");
		propertyDefinition = PropertyDefinition.builder().setType(Boolean.class).setDefaultValue(true).build();
		initialDatabuilder.defineGlobalProperty(globalPropertyId_3, propertyDefinition);

		GlobalPropertiesPluginData globalPropertiesPluginData = initialDatabuilder.build();

		List<Plugin> plugins = GlobalPropertiesTestPluginFactory.factory(t -> {
		}).setGlobalPropertiesPluginData(globalPropertiesPluginData).getPlugins();

		checkPluginDataExists(plugins, globalPropertiesPluginData, GlobalPropertiesPluginId.PLUGIN_ID);

		// precondition: globalPropertiesPluginData is not null
		ContractException contractException = assertThrows(ContractException.class,
				() -> GlobalPropertiesTestPluginFactory.factory(t -> {
				}).setGlobalPropertiesPluginData(null));
		assertEquals(GlobalPropertiesError.NULL_GLOBAL_PLUGIN_DATA, contractException.getErrorType());
	}

	@Test
	@UnitTestMethod(target = GlobalPropertiesTestPluginFactory.class, name = "getStandardGlobalPropertiesPluginData", args = {})
	public void testGetStandardGlobalPropertiesPluginData() {
		GlobalPropertiesPluginData globalPropertiesPluginData = GlobalPropertiesTestPluginFactory
				.getStandardGlobalPropertiesPluginData();

		Set<TestGlobalPropertyId> expectedPropertyIds = EnumSet.allOf(TestGlobalPropertyId.class);
		assertFalse(expectedPropertyIds.isEmpty());

		Set<GlobalPropertyId> actualGlobalPropertyIds = globalPropertiesPluginData.getGlobalPropertyIds();
		assertEquals(expectedPropertyIds, actualGlobalPropertyIds);

		for (TestGlobalPropertyId testGlobalPropertyId : TestGlobalPropertyId.values()) {
			PropertyDefinition expectedPropertyDefinition = testGlobalPropertyId.getPropertyDefinition();
			PropertyDefinition actualPropertyDefinition = globalPropertiesPluginData
					.getGlobalPropertyDefinition(testGlobalPropertyId);
			assertNotNull(expectedPropertyDefinition);
			assertEquals(expectedPropertyDefinition, actualPropertyDefinition);
		}

	}

}