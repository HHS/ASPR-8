package gov.hhs.aspr.ms.gcm.lessons;

import gov.hhs.aspr.ms.gcm.nucleus.Plugin;
import gov.hhs.aspr.ms.gcm.nucleus.PluginId;
import gov.hhs.aspr.ms.gcm.nucleus.SimplePluginId;
import gov.hhs.aspr.ms.gcm.nucleus.Simulation;

public final class Example_5 {

	private Example_5() {
	}

	public static void main(String[] args) {

		PluginId pluginId = new SimplePluginId("example plugin");

		Plugin plugin = Plugin.builder()//
				.setPluginId(pluginId)//
				.setInitializer(pluginContext -> {
					pluginContext.addActor(new Actor1()::init);
					pluginContext.addActor(new Actor2()::init);
					pluginContext.addActor(new Actor3()::init);
					pluginContext.addDataManager(new ExampleDataManager());
				})//
				.build();

		Simulation.builder()//
				.addPlugin(plugin)//
				.build()//
				.execute();
	}
}
