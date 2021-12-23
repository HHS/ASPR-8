package plugins.resources;

import nucleus.PluginContext;
import nucleus.PluginId;
import nucleus.SimplePluginId;
import nucleus.SimpleResolverId;
import plugins.compartments.CompartmentPlugin;
import plugins.partitions.PartitionsPlugin;
import plugins.people.PeoplePlugin;
import plugins.properties.PropertiesPlugin;
import plugins.regions.RegionPlugin;
import plugins.reports.ReportPlugin;
import plugins.resources.initialdata.ResourceInitialData;
import plugins.resources.resolvers.ResourceEventResolver;

public final class ResourcesPlugin {
	public final static PluginId PLUGIN_ID = new SimplePluginId(ResourcesPlugin.class);
	private final ResourceInitialData resourceInitialData;

	public ResourcesPlugin(ResourceInitialData resourceInitialData) {
		this.resourceInitialData = resourceInitialData;
	}

	
	public void init(PluginContext pluginContext) {
	
		pluginContext.defineResolver(new SimpleResolverId(ResourceEventResolver.class), new ResourceEventResolver(resourceInitialData)::init);										
	
		pluginContext.addPluginDependency(PartitionsPlugin.PLUGIN_ID);
		pluginContext.addPluginDependency(PeoplePlugin.PLUGIN_ID);
		pluginContext.addPluginDependency(PropertiesPlugin.PLUGIN_ID);
		pluginContext.addPluginDependency(ReportPlugin.PLUGIN_ID);
		pluginContext.addPluginDependency(CompartmentPlugin.PLUGIN_ID);
		pluginContext.addPluginDependency(RegionPlugin.PLUGIN_ID);
	}

}
