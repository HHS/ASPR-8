package plugins.resources;

import java.util.Optional;

import nucleus.Plugin;
import plugins.people.PeoplePluginId;
import plugins.regions.RegionsPluginId;
import plugins.resources.datamanagers.ResourcesDataManager;
import plugins.resources.reports.PersonResourceReport;
import plugins.resources.reports.PersonResourceReportPluginData;
import plugins.resources.reports.ResourcePropertyReport;
import plugins.resources.reports.ResourcePropertyReportPluginData;
import plugins.resources.reports.ResourceReport;
import plugins.resources.reports.ResourceReportPluginData;
import plugins.resources.support.ResourceError;
import util.errors.ContractException;

public final class ResourcesPlugin {

	private static class Data {
		private ResourcesPluginData resourcesPluginData;
		private PersonResourceReportPluginData personResourceReportPluginData;
		private ResourcePropertyReportPluginData resourcePropertyReportPluginData;
		private ResourceReportPluginData resourceReportPluginData;
	}

	private ResourcesPlugin() {
	}

	public static Builder builder() {
		return new Builder();
	}

	public static class Builder {
		private Builder() {
		}

		private Data data = new Data();

		private void validate() {
			if (data.resourcesPluginData == null) {
				throw new ContractException(ResourceError.NULL_RESOURCE_PLUGIN_DATA);
			}
		}

		/**
		 * Builds the PersonPropertiesPlugin from the collected inputs
		 * 
		 * @throws ContractException
		 *             <li>{@linkplain ResourceError#NULL_RESOURCE_PLUGIN_DATA}
		 *             if the personPropertiesPluginData is null</li>
		 */
		public Plugin getResourcesPlugin() {

			validate();
			Plugin.Builder builder = Plugin.builder();//
			builder.setPluginId(ResourcesPluginId.PLUGIN_ID);//

			builder.addPluginData(data.resourcesPluginData);//

			if (data.personResourceReportPluginData != null) {
				builder.addPluginData(data.personResourceReportPluginData);//
			}
			if (data.resourcePropertyReportPluginData != null) {
				builder.addPluginData(data.resourcePropertyReportPluginData);//
			}
			if (data.resourceReportPluginData != null) {
				builder.addPluginData(data.resourceReportPluginData);//
			}

			builder.addPluginDependency(PeoplePluginId.PLUGIN_ID);//
			builder.addPluginDependency(RegionsPluginId.PLUGIN_ID);//

			builder.setInitializer((c) -> {

				ResourcesPluginData pluginData = c.getPluginData(ResourcesPluginData.class).get();
				c.addDataManager(new ResourcesDataManager(pluginData));

				Optional<PersonResourceReportPluginData> optional1 = c.getPluginData(PersonResourceReportPluginData.class);
				if (optional1.isPresent()) {
					PersonResourceReportPluginData personResourceReportPluginData = optional1.get();
					c.addReport(new PersonResourceReport(personResourceReportPluginData)::init);
				}

				Optional<ResourcePropertyReportPluginData> optional2 = c.getPluginData(ResourcePropertyReportPluginData.class);
				if (optional2.isPresent()) {
					ResourcePropertyReportPluginData resourcePropertyReportPluginData = optional2.get();
					c.addReport(new ResourcePropertyReport(resourcePropertyReportPluginData)::init);
				}

				Optional<ResourceReportPluginData> optional3 = c.getPluginData(ResourceReportPluginData.class);
				if (optional3.isPresent()) {
					ResourceReportPluginData resourceReportPluginData = optional3.get();
					c.addReport(new ResourceReport(resourceReportPluginData)::init);
				}

			});
			return builder.build();

		}

		public Builder setResourcesPluginData(ResourcesPluginData resourcesPluginData) {
			data.resourcesPluginData = resourcesPluginData;
			return this;
		}

		public Builder setPersonResourceReportPluginData(PersonResourceReportPluginData personResourceReportPluginData) {
			data.personResourceReportPluginData = personResourceReportPluginData;
			return this;
		}

		public Builder setResourcePropertyReportPluginData(ResourcePropertyReportPluginData resourcePropertyReportPluginData) {
			data.resourcePropertyReportPluginData = resourcePropertyReportPluginData;
			return this;
		}

		public Builder setResourceReportPluginData(ResourceReportPluginData resourceReportPluginData) {
			data.resourceReportPluginData = resourceReportPluginData;
			return this;
		}
	}

}
