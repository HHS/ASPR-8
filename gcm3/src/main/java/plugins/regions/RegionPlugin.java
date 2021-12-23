package plugins.regions;

import nucleus.PluginContext;
import nucleus.PluginId;
import nucleus.SimplePluginId;
import nucleus.SimpleResolverId;
import plugins.components.ComponentPlugin;
import plugins.partitions.PartitionsPlugin;
import plugins.people.PeoplePlugin;
import plugins.properties.PropertiesPlugin;
import plugins.regions.initialdata.RegionInitialData;
import plugins.regions.resolvers.RegionEventResolver;
import plugins.reports.ReportPlugin;

public final class RegionPlugin {
	public final static PluginId PLUGIN_ID = new SimplePluginId(RegionPlugin.class);
	private final RegionInitialData regionInitialData;

	public RegionPlugin(RegionInitialData regionInitialData) {
		this.regionInitialData = regionInitialData;
	}

	public void init(PluginContext pluginContext) {

		pluginContext.defineResolver(new SimpleResolverId(RegionEventResolver.class), new RegionEventResolver(regionInitialData)::init);

		pluginContext.addPluginDependency(ComponentPlugin.PLUGIN_ID);
		pluginContext.addPluginDependency(PartitionsPlugin.PLUGIN_ID);
		pluginContext.addPluginDependency(PeoplePlugin.PLUGIN_ID);
		pluginContext.addPluginDependency(PropertiesPlugin.PLUGIN_ID);
		pluginContext.addPluginDependency(ReportPlugin.PLUGIN_ID);

	}

}
