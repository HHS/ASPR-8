package gov.hhs.aspr.ms.gcm.lessons;

import gov.hhs.aspr.ms.gcm.simulation.nucleus.Plugin;
import gov.hhs.aspr.ms.gcm.simulation.nucleus.PluginId;
import gov.hhs.aspr.ms.gcm.simulation.nucleus.SimplePluginId;
import gov.hhs.aspr.ms.gcm.simulation.nucleus.Simulation;

/*start code_ref=plugins_intro_to_plugins|code_cap=A simple plugin added to the simulation. Plugins act as modules for all components contributed to the simulation.*/
public final class Example_2 {

	private Example_2() {
	}

	public static void main(String[] args) {

		PluginId pluginId = new SimplePluginId("example plugin");

		Plugin plugin = Plugin.builder()//
				.setPluginId(pluginId)//
				.build();

		Simulation.builder()//
				.addPlugin(plugin)//
				.build()//
				.execute();
	}

}
/* end */
