package lesson;

import nucleus.Plugin;
import nucleus.PluginId;
import nucleus.SimplePluginId;
import nucleus.Simulation;

public final class Example_2 {

	private Example_2() {
	}
	
	public static void main(String[] args) {

		PluginId pluginId = new SimplePluginId("example plugin");
	
		Plugin plugin = Plugin	.builder()//
								.setPluginId(pluginId)//
								.build();

		Simulation	.builder()//
					.addPlugin(plugin)//
					.build()//
					.execute();		
	}
	
}


