package gov.hhs.aspr.ms.gcm.simulation.plugins.reports;

import gov.hhs.aspr.ms.gcm.simulation.nucleus.Plugin;

/**
 * A plugin providing a report actors to the simulation.
 */
public class ReportsPlugin {

	private ReportsPlugin() {
	}

	/**
	 * Returns the report plugin.
	 * <p>
	 * Uses ReportsPluginId.PLUGIN_ID as its id
	 * </p>
	 * <p>
	 * Depends on no plugins
	 * </p>
	 * <p>
	 * Provides no actors, reports or data mangers:
	 * </p>
	 */
	public static Plugin getReportsPlugin() {
		return Plugin.builder()//
				.setPluginId(ReportsPluginId.PLUGIN_ID)//
				.build();//
	}

}
