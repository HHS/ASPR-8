package plugins.globals;

import nucleus.PluginContext;
import nucleus.PluginId;
import nucleus.SimplePluginId;
import nucleus.SimpleResolverId;
import plugins.components.ComponentPlugin;
import plugins.globals.initialdata.GlobalInitialData;
import plugins.globals.resolvers.GlobalPropertyResolver;
import plugins.properties.PropertiesPlugin;
import plugins.reports.ReportPlugin;

public final class GlobalPlugin {
	
	public final static PluginId PLUGIN_ID = new SimplePluginId(GlobalPlugin.class);

	private final GlobalInitialData globalInitialData;

	public GlobalPlugin(GlobalInitialData globalInitialData) {
		this.globalInitialData = globalInitialData;
	}
	
	
	public void init(PluginContext pluginContext) {
		pluginContext.defineResolver(new SimpleResolverId(GlobalPropertyResolver.class), new GlobalPropertyResolver(globalInitialData)::init);
		
		pluginContext.addPluginDependency(ComponentPlugin.PLUGIN_ID);
		pluginContext.addPluginDependency(PropertiesPlugin.PLUGIN_ID);
		pluginContext.addPluginDependency(ReportPlugin.PLUGIN_ID);		
	}


}
