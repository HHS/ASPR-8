package plugins.reports;

import java.util.function.Consumer;

import nucleus.Plugin;
import nucleus.ReportContext;

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
	 * Provides no data mangers:
	 * </P>
	 * 
	 * <P>
	 * Provides report actors based on content in the
	 * {@linkplain ReportsPluginData}
	 * </P>
	 * 
	 */

	public static Plugin getReportsPlugin(ReportsPluginData reportsPluginData) {
		return Plugin	.builder()//
						.addPluginData(reportsPluginData)//
						.setPluginId(ReportsPluginId.PLUGIN_ID)//
						.setInitializer((c) -> {
							ReportsPluginData pluginData = c.getPluginData(ReportsPluginData.class).get();
							for (Consumer<ReportContext> consumer : pluginData.getReports()) {
								c.addReport(consumer);
							}
						}).build();//
	}

}
