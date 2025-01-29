package gov.hhs.aspr.ms.gcm.simulation.plugins.resources;

import java.util.Optional;

import gov.hhs.aspr.ms.gcm.simulation.nucleus.Plugin;
import gov.hhs.aspr.ms.gcm.simulation.plugins.people.PeoplePluginId;
import gov.hhs.aspr.ms.gcm.simulation.plugins.regions.RegionsPluginId;
import gov.hhs.aspr.ms.gcm.simulation.plugins.resources.datamanagers.ResourcesDataManager;
import gov.hhs.aspr.ms.gcm.simulation.plugins.resources.datamanagers.ResourcesPluginData;
import gov.hhs.aspr.ms.gcm.simulation.plugins.resources.reports.PersonResourceReport;
import gov.hhs.aspr.ms.gcm.simulation.plugins.resources.reports.PersonResourceReportPluginData;
import gov.hhs.aspr.ms.gcm.simulation.plugins.resources.reports.ResourcePropertyReport;
import gov.hhs.aspr.ms.gcm.simulation.plugins.resources.reports.ResourcePropertyReportPluginData;
import gov.hhs.aspr.ms.gcm.simulation.plugins.resources.reports.ResourceReport;
import gov.hhs.aspr.ms.gcm.simulation.plugins.resources.reports.ResourceReportPluginData;
import gov.hhs.aspr.ms.gcm.simulation.plugins.resources.support.ResourceError;
import gov.hhs.aspr.ms.util.errors.ContractException;

public final class ResourcesPlugin {

	private static class Data {
		private ResourcesPluginData resourcesPluginData;
		private PersonResourceReportPluginData personResourceReportPluginData;
		private ResourcePropertyReportPluginData resourcePropertyReportPluginData;
		private ResourceReportPluginData resourceReportPluginData;
		private boolean locked;

		private Data() {
		}

		private Data(Data data) {
			resourcesPluginData = data.resourcesPluginData;
			personResourceReportPluginData = data.personResourceReportPluginData;
			resourcePropertyReportPluginData = data.resourcePropertyReportPluginData;
			resourceReportPluginData = data.resourceReportPluginData;
			locked = data.locked;
		}
	}

	private final Data data;

	private ResourcesPlugin(final Data data) {
		this.data = data;
	}

	public static Builder builder() {
		return new Builder(new Data());
	}

	public static class Builder {
		private Builder(Data data) {
			this.data = data;
		}

		private Data data;

		private void validate() {
			if (data.resourcesPluginData == null) {
				throw new ContractException(ResourceError.NULL_RESOURCE_PLUGIN_DATA);
			}
		}

		/**
		 * Builds the PersonPropertiesPlugin from the collected inputs
		 * 
		 * @throws ContractException {@linkplain ResourceError#NULL_RESOURCE_PLUGIN_DATA}
		 *                           if the personPropertiesPluginData is null
		 */
		public Plugin getResourcesPlugin() {
			if (!data.locked) {
				validate();
			}
			ensureImmutability();

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

				Optional<PersonResourceReportPluginData> optional1 = c
						.getPluginData(PersonResourceReportPluginData.class);
				if (optional1.isPresent()) {
					PersonResourceReportPluginData personResourceReportPluginData = optional1.get();
					c.addReport(new PersonResourceReport(personResourceReportPluginData)::init);
				}

				Optional<ResourcePropertyReportPluginData> optional2 = c
						.getPluginData(ResourcePropertyReportPluginData.class);
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
			ensureDataMutability();
			data.resourcesPluginData = resourcesPluginData;
			return this;
		}

		public Builder setPersonResourceReportPluginData(
				PersonResourceReportPluginData personResourceReportPluginData) {
			ensureDataMutability();
			data.personResourceReportPluginData = personResourceReportPluginData;
			return this;
		}

		public Builder setResourcePropertyReportPluginData(
				ResourcePropertyReportPluginData resourcePropertyReportPluginData) {
			ensureDataMutability();
			data.resourcePropertyReportPluginData = resourcePropertyReportPluginData;
			return this;
		}

		public Builder setResourceReportPluginData(ResourceReportPluginData resourceReportPluginData) {
			ensureDataMutability();
			data.resourceReportPluginData = resourceReportPluginData;
			return this;
		}

		private void ensureDataMutability() {
			if (data.locked) {
				data = new Data(data);
				data.locked = false;
			}
		}

		private void ensureImmutability() {
			if (!data.locked) {
				data.locked = true;
			}
		}
	}

	public Builder toBuilder() {
		return new Builder(data);
	}

}
