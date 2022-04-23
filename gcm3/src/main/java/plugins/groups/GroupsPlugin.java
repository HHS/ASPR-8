package plugins.groups;

import nucleus.Plugin;
import plugins.partitions.PartitionsPluginId;
import plugins.people.PeoplePluginId;
import plugins.reports.ReportsPluginId;
import plugins.stochastics.StochasticsPluginId;

/**
 * A plugin providing a group data manager to the simulation.
 * 
 * @author Shawn Hatch
 *
 */
public final class GroupPlugin {
	private GroupPlugin() {
	}

	public static Plugin getGroupPlugin(GroupPluginData groupPluginData) {

		return Plugin	.builder()//
						.setPluginId(GroupPluginId.PLUGIN_ID)//
						.addPluginData(groupPluginData)//
						.addPluginDependency(PartitionsPluginId.PLUGIN_ID)//
						.addPluginDependency(PeoplePluginId.PLUGIN_ID)//
						.addPluginDependency(ReportsPluginId.PLUGIN_ID)//
						.addPluginDependency(StochasticsPluginId.PLUGIN_ID)//
						.setInitializer((c) -> {
							GroupPluginData pluginData = c.getPluginData(GroupPluginData.class);
							c.addDataManager(new GroupDataManager(pluginData));
						})//
						.build();
	}

}
