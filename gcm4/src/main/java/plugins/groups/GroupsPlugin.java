package plugins.groups;

import java.util.Optional;

import nucleus.Plugin;
import plugins.groups.datamanagers.GroupsDataManager;
import plugins.groups.reports.GroupPopulationReport;
import plugins.groups.reports.GroupPopulationReportPluginData;
import plugins.groups.reports.GroupPropertyReport;
import plugins.groups.reports.GroupPropertyReportPluginData;
import plugins.groups.support.GroupError;
import plugins.people.PeoplePluginId;
import plugins.personproperties.support.PersonPropertyError;
import plugins.stochastics.StochasticsPluginId;
import util.errors.ContractException;

/**
 * A plugin providing a group data manager to the simulation.
 * 
 *
 */
public final class GroupsPlugin {

	private static class Data {
		private GroupsPluginData groupsPluginData;
		private GroupPopulationReportPluginData groupPopulationReportPluginData;
		private GroupPropertyReportPluginData groupPropertyReportPluginData;
	}

	private GroupsPlugin() {
	}

	public static Builder builder() {
		return new Builder();
	}

	public static class Builder {
		private Builder() {
		}

		private Data data = new Data();

		private void validate() {
			if (data.groupsPluginData == null) {
				throw new ContractException(GroupError.NULL_GROUP_PLUGIN_DATA);
			}
		}

		public Builder setGroupsPluginData(GroupsPluginData groupsPluginData) {
			data.groupsPluginData = groupsPluginData;
			return this;
		}

		public Builder setGroupPopulationReportPluginData(GroupPopulationReportPluginData groupPopulationReportPluginData) {
			data.groupPopulationReportPluginData = groupPopulationReportPluginData;
			return this;
		}

		public Builder setGroupPropertyReportPluginData(GroupPropertyReportPluginData groupPropertyReportPluginData) {
			data.groupPropertyReportPluginData = groupPropertyReportPluginData;
			return this;
		}

		/**
		 * Builds the PersonPropertiesPlugin from the collected inputs
		 * 
		 * @throws ContractException
		 *             <li>{@linkplain PersonPropertyError#NULL_GROUP_PLUGIN_DATA}
		 *             if the groupsPluginData is null</li>
		 */
		public Plugin getGroupsPlugin() {

			try {
				validate();
				Plugin.Builder builder = Plugin.builder();//
				builder.setPluginId(GroupsPluginId.PLUGIN_ID);//

				if (data.groupPopulationReportPluginData != null) {
					builder.addPluginData(data.groupPopulationReportPluginData);
				}

				if (data.groupPropertyReportPluginData != null) {
					builder.addPluginData(data.groupPropertyReportPluginData);
				}

				builder.addPluginData(data.groupsPluginData);//
				builder.addPluginDependency(PeoplePluginId.PLUGIN_ID);//
				builder	.addPluginDependency(StochasticsPluginId.PLUGIN_ID)//
						.setInitializer((c) -> {
							GroupsPluginData pluginData = c.getPluginData(GroupsPluginData.class).get();
							c.addDataManager(new GroupsDataManager(pluginData));

							Optional<GroupPopulationReportPluginData> optional1 = c.getPluginData(GroupPopulationReportPluginData.class);
							if (optional1.isPresent()) {
								GroupPopulationReportPluginData groupPopulationReportPluginData = optional1.get();
								c.addReport(new GroupPopulationReport(groupPopulationReportPluginData)::init);
							}

							Optional<GroupPropertyReportPluginData> optional2 = c.getPluginData(GroupPropertyReportPluginData.class);
							if (optional2.isPresent()) {
								GroupPropertyReportPluginData groupPropertyReportPluginData = optional2.get();
								c.addReport(new GroupPropertyReport(groupPropertyReportPluginData)::init);
							}

						});//
				return builder.build();
			} finally {
				data = new Data();
			}
		}
	}

}
