package plugins.personproperties;

import java.util.Optional;

import net.jcip.annotations.ThreadSafe;
import nucleus.Plugin;
import plugins.people.PeoplePluginId;
import plugins.personproperties.datamanagers.PersonPropertiesDataManager;
import plugins.personproperties.reports.PersonPropertyInteractionReport;
import plugins.personproperties.reports.PersonPropertyInteractionReportPluginData;
import plugins.personproperties.reports.PersonPropertyReport;
import plugins.personproperties.reports.PersonPropertyReportPluginData;
import plugins.personproperties.support.PersonPropertyError;
import plugins.regions.RegionsPluginId;
import util.errors.ContractException;

/**
 * A plugin providing a person property management to the simulation.
 * 
 *
 */
@ThreadSafe
public final class PersonPropertiesPlugin {

	private static class Data {
		private PersonPropertiesPluginData personPropertiesPluginData;
		private PersonPropertyReportPluginData personPropertyReportPluginData;
		private PersonPropertyInteractionReportPluginData personPropertyInteractionReportPluginData;
	}

	private PersonPropertiesPlugin() {

	}

	public static Builder builder() {
		return new Builder();
	}

	public static class Builder {
		private Builder() {
		}

		private Data data = new Data();

		private void validate() {
			if (data.personPropertiesPluginData == null) {
				throw new ContractException(PersonPropertyError.NULL_PERSON_PROPERTY_PLUGN_DATA);
			}
		}

		/**
		 * Builds the PersonPropertiesPlugin from the collected inputs
		 * 
		 * @throws ContractException
		 *             <li>{@linkplain PersonPropertyError#NULL_PERSON_PROPERTY_PLUGN_DATA}
		 *             if the personPropertiesPluginData is null</li>
		 */
		public Plugin getPersonPropertyPlugin() {

			validate();
			Plugin.Builder builder = Plugin.builder();//
			builder.setPluginId(PersonPropertiesPluginId.PLUGIN_ID);//
			builder.addPluginData(data.personPropertiesPluginData);//
			if (data.personPropertyInteractionReportPluginData != null) {
				builder.addPluginData(data.personPropertyInteractionReportPluginData);//
			}
			if (data.personPropertyReportPluginData != null) {
				builder.addPluginData(data.personPropertyReportPluginData);//
			}
			builder.addPluginDependency(PeoplePluginId.PLUGIN_ID);//
			builder.addPluginDependency(RegionsPluginId.PLUGIN_ID);//
			builder.setInitializer((c) -> {
				PersonPropertiesPluginData pluginData = c.getPluginData(PersonPropertiesPluginData.class).get();
				c.addDataManager(new PersonPropertiesDataManager(pluginData));

				Optional<PersonPropertyReportPluginData> optional1 = c.getPluginData(PersonPropertyReportPluginData.class);
				if (optional1.isPresent()) {
					PersonPropertyReportPluginData personPropertyReportPluginData = optional1.get();
					c.addReport(new PersonPropertyReport(personPropertyReportPluginData)::init);
				}

				Optional<PersonPropertyInteractionReportPluginData> optional2 = c.getPluginData(PersonPropertyInteractionReportPluginData.class);
				if (optional2.isPresent()) {
					PersonPropertyInteractionReportPluginData personPropertyInteractionReportPluginData = optional2.get();
					c.addReport(new PersonPropertyInteractionReport(personPropertyInteractionReportPluginData)::init);
				}

			});
			return builder.build();

		}

		public Builder setPersonPropertiesPluginData(PersonPropertiesPluginData personPropertiesPluginData) {
			data.personPropertiesPluginData = personPropertiesPluginData;
			return this;
		}

		public Builder setPersonPropertyReportPluginData(PersonPropertyReportPluginData personPropertyReportPluginData) {
			data.personPropertyReportPluginData = personPropertyReportPluginData;
			return this;
		}

		public Builder setPersonPropertyInteractionReportPluginData(PersonPropertyInteractionReportPluginData personPropertyInteractionReportPluginData) {
			data.personPropertyInteractionReportPluginData = personPropertyInteractionReportPluginData;
			return this;
		}
	}

}
