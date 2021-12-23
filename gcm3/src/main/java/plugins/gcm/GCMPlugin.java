package plugins.gcm;

import nucleus.PluginContext;
import nucleus.PluginId;
import nucleus.SimplePluginId;
import nucleus.SimpleResolverId;
import plugins.compartments.CompartmentPlugin;
import plugins.components.ComponentPlugin;
import plugins.gcm.resolvers.PermissionResolver;
import plugins.globals.GlobalPlugin;
import plugins.groups.GroupPlugin;
import plugins.materials.MaterialsPlugin;
import plugins.partitions.PartitionsPlugin;
import plugins.people.PeoplePlugin;
import plugins.personproperties.PersonPropertiesPlugin;
import plugins.properties.PropertiesPlugin;
import plugins.regions.RegionPlugin;
import plugins.reports.ReportPlugin;
import plugins.resources.ResourcesPlugin;
import plugins.stochastics.StochasticsPlugin;

public final class GCMPlugin  {
	public final static PluginId PLUGIN_ID = new SimplePluginId(GCMPlugin.class);
	
	public void init(PluginContext pluginContext) {	
		pluginContext.defineResolver(new SimpleResolverId(PermissionResolver.class),new PermissionResolver()::init);		

		pluginContext.addPluginDependency(ComponentPlugin.PLUGIN_ID);
		pluginContext.addPluginDependency(PartitionsPlugin.PLUGIN_ID);
		pluginContext.addPluginDependency(PeoplePlugin.PLUGIN_ID);
		pluginContext.addPluginDependency(PropertiesPlugin.PLUGIN_ID);
		pluginContext.addPluginDependency(ReportPlugin.PLUGIN_ID);		
		pluginContext.addPluginDependency(CompartmentPlugin.PLUGIN_ID);
		pluginContext.addPluginDependency(GlobalPlugin.PLUGIN_ID);
		pluginContext.addPluginDependency(GroupPlugin.PLUGIN_ID);
		pluginContext.addPluginDependency(MaterialsPlugin.PLUGIN_ID);
		pluginContext.addPluginDependency(PersonPropertiesPlugin.PLUGIN_ID);
		pluginContext.addPluginDependency(RegionPlugin.PLUGIN_ID);
		pluginContext.addPluginDependency(ResourcesPlugin.PLUGIN_ID);
		pluginContext.addPluginDependency(StochasticsPlugin.PLUGIN_ID);
	}

}
