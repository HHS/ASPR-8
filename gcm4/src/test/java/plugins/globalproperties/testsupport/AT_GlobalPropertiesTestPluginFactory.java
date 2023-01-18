package plugins.globalproperties.testsupport;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
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
import plugins.globalproperties.GlobalPropertiesPluginData;
import plugins.globalproperties.datamanagers.GlobalPropertiesDataManager;
import plugins.globalproperties.support.GlobalPropertyId;
import plugins.globalproperties.support.SimpleGlobalPropertyId;
import plugins.util.properties.PropertyDefinition;
import tools.annotations.UnitTestMethod;
import util.wrappers.MutableBoolean;

public class AT_GlobalPropertiesTestPluginFactory {

	private Consumer<ActorContext> factoryConsumer(MutableBoolean executed) {
		return (c) -> {
			GlobalPropertiesDataManager globalPropertiesDataManager = c
					.getDataManager(GlobalPropertiesDataManager.class);

			Set<GlobalPropertyId> propertyIds = globalPropertiesDataManager.getGlobalPropertyIds();
			assertTrue(propertyIds.size() > 0);
			for (TestGlobalPropertyId testGlobalPropertyId : TestGlobalPropertyId.values()) {
				PropertyDefinition propertyDefinition = testGlobalPropertyId.getPropertyDefinition();
				assertTrue(propertyIds.contains(testGlobalPropertyId));
				assertEquals(propertyDefinition,
						globalPropertiesDataManager.getGlobalPropertyDefinition(testGlobalPropertyId));
			}

			executed.setValue(true);
		};
	}

	@Test
	@UnitTestMethod(target = GlobalPropertiesTestPluginFactory.class, name = "factory", args = { Consumer.class })
	public void testFactory1() {
		MutableBoolean executed = new MutableBoolean();
		TestSimulation
				.executeSimulation(GlobalPropertiesTestPluginFactory.factory(factoryConsumer(executed)).getPlugins());
		assertTrue(executed.getValue());
	}

	@Test
	@UnitTestMethod(target = GlobalPropertiesTestPluginFactory.class, name = "factory", args = { Plugin.class })
	public void testFactory2() {
		MutableBoolean executed = new MutableBoolean();
		TestPluginData.Builder pluginBuilder = TestPluginData.builder();
		pluginBuilder.addTestActorPlan("actor", new TestActorPlan(0, factoryConsumer(executed)));
		TestPluginData testPluginData = pluginBuilder.build();

		TestSimulation.executeSimulation(GlobalPropertiesTestPluginFactory.factory(testPluginData).getPlugins());
		assertTrue(executed.getValue());

	}

	@Test
	@UnitTestMethod(target = GlobalPropertiesTestPluginFactory.Factory.class, name = "getPlugins", args = {})
	public void testGetPlugins() {
		assertEquals(2, GlobalPropertiesTestPluginFactory.factory(t -> {
		}).getPlugins().size());
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

		plugins.forEach((plugin) -> {
			// can do this because it is known that each plugin only gets one data
			// associated with it in this instance
			PluginData pluginData = plugin.getPluginDatas().toArray(new PluginData[0])[0];
			if (pluginData instanceof GlobalPropertiesPluginData) {
				assertEquals(globalPropertiesPluginData, (GlobalPropertiesPluginData) pluginData);
			} else {
				assertNotEquals(globalPropertiesPluginData, pluginData);
			}
		});
	}

	@Test
	@UnitTestMethod(target = GlobalPropertiesTestPluginFactory.class, name = "getStandardGlobalPropertiesPluginData", args = {})
	public void testGetStandardGlobalPropertiesPluginData() {
		GlobalPropertiesPluginData globalPropertiesPluginData = GlobalPropertiesTestPluginFactory
				.getStandardGlobalPropertiesPluginData();

		Set<GlobalPropertyId> globalPropertyIds = globalPropertiesPluginData.getGlobalPropertyIds();

		assertTrue(globalPropertyIds.size() > 0);

		for (TestGlobalPropertyId testGlobalPropertyId : TestGlobalPropertyId.values()) {
			PropertyDefinition propertyDefinition = testGlobalPropertyId.getPropertyDefinition();
			assertTrue(globalPropertyIds.contains(testGlobalPropertyId));
			assertEquals(propertyDefinition,
					globalPropertiesPluginData.getGlobalPropertyDefinition(testGlobalPropertyId));
		}
	}

}