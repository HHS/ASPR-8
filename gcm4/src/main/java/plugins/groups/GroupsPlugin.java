package plugins.groups;

import nucleus.Plugin;
import plugins.groups.dataViews.GroupsDataView;
import plugins.groups.datamanagers.GroupsDataManager;
import plugins.people.PeoplePluginId;
import plugins.stochastics.StochasticsPluginId;

/**
 * A plugin providing a group data manager to the simulation.
 * 
 *
 */
public final class GroupsPlugin {
	private GroupsPlugin() {
	}

	public static Plugin getGroupPlugin(GroupsPluginData groupsPluginData) {

		return Plugin	.builder()//
						.setPluginId(GroupsPluginId.PLUGIN_ID)//
						.addPluginData(groupsPluginData)//
						.addPluginDependency(PeoplePluginId.PLUGIN_ID)//
						.addPluginDependency(StochasticsPluginId.PLUGIN_ID)//
						.setInitializer((c) -> {
							GroupsPluginData pluginData = c.getPluginData(GroupsPluginData.class);
							GroupsDataManager groupsDataManager = new GroupsDataManager(pluginData);
							c.addDataManager(groupsDataManager);							
							GroupsDataView groupsDataView = new GroupsDataView(groupsDataManager);
							c.addDataView(groupsDataView);
						})//
						.build();
	}

}
