package plugins.globalproperties;

import nucleus.Plugin;
import plugins.globalproperties.actors.GlobalPropertyReport;
import plugins.globalproperties.datamanagers.GlobalPropertiesDataManager;
import plugins.reports.ReportsPluginId;

/**
 * A plugin providing a global property data manager to the simulation.
 * 
 * @author Shawn Hatch
 *
 */
public final class GlobalPropertiesPlugin {

	private GlobalPropertiesPlugin() {
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
	 * <li>{@linkplain GlobalPropertiesDataManager}</li>
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
	public static Plugin getPlugin(GlobalPropertiesPluginData globalPropertiesPluginData) {
		return Plugin	.builder()//
						.addPluginData(globalPropertiesPluginData)//
						.addPluginDependency(ReportsPluginId.PLUGIN_ID)//
						.setInitializer((c) -> {
							GlobalPropertiesPluginData data = c.getPluginData(GlobalPropertiesPluginData.class);
							c.addDataManager(new GlobalPropertiesDataManager(data));
						})//
						.setPluginId(GlobalPropertiesPluginId.PLUGIN_ID)//
						.build();
	}

}
