package gov.hhs.aspr.ms.gcm.plugins.groups;

import java.util.Optional;

import gov.hhs.aspr.ms.gcm.nucleus.Plugin;
import gov.hhs.aspr.ms.gcm.plugins.groups.datamanagers.GroupsDataManager;
import gov.hhs.aspr.ms.gcm.plugins.groups.datamanagers.GroupsPluginData;
import gov.hhs.aspr.ms.gcm.plugins.groups.reports.GroupPopulationReport;
import gov.hhs.aspr.ms.gcm.plugins.groups.reports.GroupPopulationReportPluginData;
import gov.hhs.aspr.ms.gcm.plugins.groups.reports.GroupPropertyReport;
import gov.hhs.aspr.ms.gcm.plugins.groups.reports.GroupPropertyReportPluginData;
import gov.hhs.aspr.ms.gcm.plugins.groups.support.GroupError;
import gov.hhs.aspr.ms.gcm.plugins.people.PeoplePluginId;
import gov.hhs.aspr.ms.gcm.plugins.stochastics.StochasticsPluginId;
import util.errors.ContractException;

/**
 * A plugin providing a group data manager to the simulation.
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

		public Builder setGroupPopulationReportPluginData(
				GroupPopulationReportPluginData groupPopulationReportPluginData) {
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
		 * @throws ContractException {@linkplain GroupError#NULL_GROUP_PLUGIN_DATA} if
		 *                           the groupsPluginData is null
		 */
		public Plugin getGroupsPlugin() {

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
			builder.addPluginDependency(StochasticsPluginId.PLUGIN_ID)//
					.setInitializer((c) -> {
						GroupsPluginData pluginData = c.getPluginData(GroupsPluginData.class).get();
						c.addDataManager(new GroupsDataManager(pluginData));

						Optional<GroupPopulationReportPluginData> optional1 = c
								.getPluginData(GroupPopulationReportPluginData.class);
						if (optional1.isPresent()) {
							GroupPopulationReportPluginData groupPopulationReportPluginData = optional1.get();
							c.addReport(new GroupPopulationReport(groupPopulationReportPluginData)::init);
						}

						Optional<GroupPropertyReportPluginData> optional2 = c
								.getPluginData(GroupPropertyReportPluginData.class);
						if (optional2.isPresent()) {
							GroupPropertyReportPluginData groupPropertyReportPluginData = optional2.get();
							c.addReport(new GroupPropertyReport(groupPropertyReportPluginData)::init);
						}

					});//
			return builder.build();

		}
	}

}
