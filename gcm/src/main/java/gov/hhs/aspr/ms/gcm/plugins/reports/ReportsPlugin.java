package gov.hhs.aspr.ms.gcm.plugins.reports;

import gov.hhs.aspr.ms.gcm.nucleus.Plugin;

/**
 * A plugin providing a report actors to the simulation.
 * 
 *
 */
public class ReportsPlugin {

	private ReportsPlugin() {
	}

	/**
	 * Returns the report plugin.
	 *
	 * <P>
	 * Uses ReportsPluginId.PLUGIN_ID as its id
	 * </P>
	 * 
	 * <P>
	 * Depends on no plugins
	 * </P>
	 * 
	 * <P>
	 * Provides no actors, reports or data mangers:
	 * </P>
	 * 
	 */

	public static Plugin getReportsPlugin() {
		return Plugin	.builder()//						
						.setPluginId(ReportsPluginId.PLUGIN_ID)//
						.build();//
	}

}
