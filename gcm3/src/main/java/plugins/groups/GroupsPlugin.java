package plugins.groups;

import nucleus.Plugin;
import plugins.groups.datamanagers.GroupsDataManager;
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
public final class GroupsPlugin {
	private GroupsPlugin() {
	}

	public static Plugin getGroupPlugin(GroupsPluginData groupsPluginData) {

		return Plugin	.builder()//
						.setPluginId(GroupsPluginId.PLUGIN_ID)//
						.addPluginData(groupsPluginData)//
						.addPluginDependency(PartitionsPluginId.PLUGIN_ID)//
						.addPluginDependency(PeoplePluginId.PLUGIN_ID)//
						.addPluginDependency(ReportsPluginId.PLUGIN_ID)//
						.addPluginDependency(StochasticsPluginId.PLUGIN_ID)//
						.setInitializer((c) -> {
							GroupsPluginData pluginData = c.getPluginData(GroupsPluginData.class);
							c.addDataManager(new GroupsDataManager(pluginData));
						})//
						.build();
	}

}
