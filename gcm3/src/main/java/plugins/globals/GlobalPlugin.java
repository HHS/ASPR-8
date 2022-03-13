package plugins.globals;

import nucleus.Plugin;
import plugins.globals.actors.GlobalPropertyReport;
import plugins.reports.ReportsPluginId;

/**
 * A plugin providing a global property data manager to the simulation.
 * 
 * @author Shawn Hatch
 *
 */
public final class GlobalPlugin {

	private GlobalPlugin() {
	}

	/**
	 * Returns the global plugin.
	 *
	 * <P>
	 * Uses GlobalsPluginId.PLUGIN_ID as its id
	 * </P>
	 * 
	 * <P>
	 * Depends on plugins:
	 * <ul>
	 * <li>Report Plugin</li>
	 * </ul>
	 * </P>
	 * 
	 * <P>
	 * Provides data mangers:
	 * <ul>
	 * <li>{@linkplain GlobalDataManager}</li>
	 * </ul>
	 * </P>
	 * 
	 * <P>
	 * Provides actors:
	 * <ul>
	 * <li>{@linkplain GlobalPropertyReport}</li>
	 * </ul>
	 * </P>
	 * 
	 */
	public static Plugin getPlugin(GlobalPluginData globalPluginData) {
		return Plugin	.builder()//
						.addPluginData(globalPluginData)//
						.addPluginDependency(ReportsPluginId.PLUGIN_ID)//
						.setInitializer((c) -> {
							GlobalPluginData data = c.getPluginData(GlobalPluginData.class).get();
							c.addDataManager(new GlobalDataManager(data));
						})//
						.setPluginId(GlobalsPluginId.PLUGIN_ID)//
						.build();
	}

}
