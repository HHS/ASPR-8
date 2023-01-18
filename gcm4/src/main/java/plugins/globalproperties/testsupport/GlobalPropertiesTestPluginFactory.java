package plugins.globalproperties.testsupport;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import nucleus.ActorContext;
import nucleus.Plugin;
import nucleus.testsupport.testplugin.TestActorPlan;
import nucleus.testsupport.testplugin.TestPlugin;
import nucleus.testsupport.testplugin.TestPluginData;
import plugins.globalproperties.GlobalPropertiesPlugin;
import plugins.globalproperties.GlobalPropertiesPluginData;

/**
 * A static test support class for the globals plugin. Provides convenience
 * methods for integrating a test plugin into a global-properties simulation
 * test harness.
 * 
 * 
 *
 */

public final class GlobalPropertiesTestPluginFactory {

	private GlobalPropertiesTestPluginFactory() {
	}

	private static class Data {
		private GlobalPropertiesPluginData globalPropertiesPluginData;
		private Plugin testPlugin;

		private Data(Plugin testPlugin) {
			this.globalPropertiesPluginData = GlobalPropertiesTestPluginFactory.getStandardGlobalPropertiesPluginData();
			this.testPlugin = testPlugin;
		}
	}

	public static class Factory {
		private Data data;

		private Factory(Data data) {
			this.data = data;
		}

		public List<Plugin> getPlugins() {
			List<Plugin> pluginsToAdd = new ArrayList<>();
			Plugin groupPlugin = GlobalPropertiesPlugin.getGlobalPropertiesPlugin(this.data.globalPropertiesPluginData);

			pluginsToAdd.add(groupPlugin);
			pluginsToAdd.add(this.data.testPlugin);

			return pluginsToAdd;
		}

		public Factory setGlobalPropertiesPluginData(GlobalPropertiesPluginData globalPropertiesPluginData) {
			this.data.globalPropertiesPluginData = globalPropertiesPluginData;
			return this;
		}
	}

	public static Factory factory(Plugin testPlugin) {
		return new Factory(new Data(testPlugin));
	}

	public static Factory factory(Consumer<ActorContext> consumer) {
		TestPluginData.Builder pluginBuilder = TestPluginData.builder();
		pluginBuilder.addTestActorPlan("actor", new TestActorPlan(0, consumer));
		TestPluginData testPluginData = pluginBuilder.build();
		Plugin testPlugin = TestPlugin.getTestPlugin(testPluginData);
		return new Factory(new Data(testPlugin));
	}

	public static GlobalPropertiesPluginData getStandardGlobalPropertiesPluginData() {
		GlobalPropertiesPluginData.Builder globalsPluginBuilder = GlobalPropertiesPluginData.builder();
		for (TestGlobalPropertyId testGlobalPropertyId : TestGlobalPropertyId.values()) {
			globalsPluginBuilder.defineGlobalProperty(testGlobalPropertyId,
					testGlobalPropertyId.getPropertyDefinition());
		}

		return globalsPluginBuilder.build();
	}

}
