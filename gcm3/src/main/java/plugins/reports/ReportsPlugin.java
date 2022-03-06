package plugins.reports;

import java.util.function.Consumer;

import nucleus.ActorContext;
import nucleus.Plugin;

public class ReportsPlugin {
	
	private ReportsPlugin() {}

	public static Plugin getReportPlugin(ReportsPluginData reportsPluginData) {
		return Plugin	.builder()//
				.addPluginData(reportsPluginData)//
				.setPluginId(ReportsPluginId.PLUGIN_ID)//
				.setInitializer((c) -> {
					ReportsPluginData pluginData = c.getPluginData(ReportsPluginData.class).get();
					for( Consumer<ActorContext> consumer : pluginData.getReports()) {
						c.addActor(consumer);
					}					
				}).build();//
	}
	
	
}
