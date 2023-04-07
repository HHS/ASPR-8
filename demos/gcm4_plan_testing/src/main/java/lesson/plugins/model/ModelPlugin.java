package lesson.plugins.model;

import lesson.plugins.model.actors.antigenproducer.AntigenProducerPluginData;
import lesson.plugins.model.actors.contactmanager.ContactManager;
import lesson.plugins.model.actors.contactmanager.ContactManagerPluginData;
import lesson.plugins.model.actors.populationloader.PopulationLoader;
import lesson.plugins.model.actors.vaccinator.Vaccinator;
import lesson.plugins.model.actors.vaccinator.VaccinatorPluginData;
import lesson.plugins.model.actors.vaccineproducer.VaccineProducer;
import lesson.plugins.model.actors.vaccineproducer.VaccineProducerPluginData;
import nucleus.Plugin;
import nucleus.PluginContext;

public final class ModelPlugin {
	private static class Data {
		private AntigenProducerPluginData antigenProducerPluginData;
		private VaccineProducerPluginData vaccineProducerPluginData;
		private VaccinatorPluginData vaccinatorPluginData;
		private ContactManagerPluginData contactManagerPluginData;

	}

	public static Builder builder() {
		return new Builder();
	}

	public static class Builder {

		private Builder() {
		}

		private Data data = new Data();

		public Plugin getModelPlugin() {
			try {
				validate();
				Plugin.Builder builder = Plugin.builder();
//				builder.addPluginData(data.antigenProducerPluginData);
				builder.addPluginData(data.vaccineProducerPluginData);
				builder.addPluginData(data.vaccinatorPluginData);
				builder.addPluginData(data.contactManagerPluginData);
				builder.setPluginId(ModelPluginId.PLUGIN_ID);
				builder.setInitializer(ModelPlugin::init);
				return builder.build();
			} finally {
				data = new Data();
			}
		}

		private void validate() {
//			if (data.antigenProducerPluginData == null) {
//				throw new ContractException(ModelError.NULL_ANTIGEN_PRODUCER_PLUGIN_DATA);
//			}
//			if (data.vaccineProducerPluginData == null) {
//				throw new ContractException(ModelError.NULL_VACCINE_PRODUCER_PLUGIN_DATA);
//			}
//
//			if (data.vaccinatorPluginData == null) {
//				throw new ContractException(ModelError.NULL_VACCINATOR_PLUGIN_DATA);
//			}
//			if (data.contactManagerPluginData == null) {
//				throw new ContractException(ModelError.NULL_CONTACT_MANAGER_PLUGIN_DATA);
//			}
		}

		public Builder setAntigenProducerPluginData(AntigenProducerPluginData antigenProducerPluginData) {
			data.antigenProducerPluginData = antigenProducerPluginData;
			return this;
		}

		public Builder setVaccineProducerPluginData(VaccineProducerPluginData vaccineProducerPluginData) {
			data.vaccineProducerPluginData = vaccineProducerPluginData;
			return this;
		}

		public Builder setVaccinatorPluginData(VaccinatorPluginData vaccinatorPluginData) {
			data.vaccinatorPluginData = vaccinatorPluginData;
			return this;
		}

		public Builder setContactManagerPluginData(ContactManagerPluginData contactManagerPluginData) {
			data.contactManagerPluginData = contactManagerPluginData;
			return this;
		}

	}

	private static void init(PluginContext pluginContext) {
//		AntigenProducerPluginData antigenProducerPluginData = pluginContext.getPluginData(AntigenProducerPluginData.class).get();
		VaccineProducerPluginData vaccineProducerPluginData = pluginContext.getPluginData(VaccineProducerPluginData.class).get();
		VaccinatorPluginData vaccinatorPluginData = pluginContext.getPluginData(VaccinatorPluginData.class).get();
		ContactManagerPluginData contactManagerPluginData = pluginContext.getPluginData(ContactManagerPluginData.class).get();

		pluginContext.addActor(new PopulationLoader()::init);
		pluginContext.addActor(new ContactManager(contactManagerPluginData)::init);
		pluginContext.addActor(new Vaccinator(vaccinatorPluginData)::init);
		pluginContext.addActor(new VaccineProducer(vaccineProducerPluginData)::init);
//		pluginContext.addActor(new AntigenProducer(antigenProducerPluginData)::init);
//		pluginContext.addReport(new DiseaseStateReport(ModelReportLabel.DISEASE_STATE_REPORT, ReportPeriod.END_OF_SIMULATION)::init);//
//		pluginContext.addReport(new VaccineReport(ModelReportLabel.VACCINE_REPORT, ReportPeriod.DAILY)::init);//
//		pluginContext.addReport(new VaccineProductionReport(ModelReportLabel.VACCINE_PRODUCTION_REPORT, ReportPeriod.DAILY)::init);//
	}

	private ModelPlugin() {

	}
}
