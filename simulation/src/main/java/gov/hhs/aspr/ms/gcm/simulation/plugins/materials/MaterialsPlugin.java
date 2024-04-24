package gov.hhs.aspr.ms.gcm.simulation.plugins.materials;

import java.util.Optional;

import gov.hhs.aspr.ms.gcm.simulation.nucleus.Plugin;
import gov.hhs.aspr.ms.gcm.simulation.plugins.materials.datamangers.MaterialsDataManager;
import gov.hhs.aspr.ms.gcm.simulation.plugins.materials.datamangers.MaterialsPluginData;
import gov.hhs.aspr.ms.gcm.simulation.plugins.materials.reports.BatchStatusReport;
import gov.hhs.aspr.ms.gcm.simulation.plugins.materials.reports.BatchStatusReportPluginData;
import gov.hhs.aspr.ms.gcm.simulation.plugins.materials.reports.MaterialsProducerPropertyReport;
import gov.hhs.aspr.ms.gcm.simulation.plugins.materials.reports.MaterialsProducerPropertyReportPluginData;
import gov.hhs.aspr.ms.gcm.simulation.plugins.materials.reports.MaterialsProducerResourceReport;
import gov.hhs.aspr.ms.gcm.simulation.plugins.materials.reports.MaterialsProducerResourceReportPluginData;
import gov.hhs.aspr.ms.gcm.simulation.plugins.materials.reports.StageReport;
import gov.hhs.aspr.ms.gcm.simulation.plugins.materials.reports.StageReportPluginData;
import gov.hhs.aspr.ms.gcm.simulation.plugins.materials.support.MaterialsError;
import gov.hhs.aspr.ms.gcm.simulation.plugins.regions.RegionsPluginId;
import gov.hhs.aspr.ms.gcm.simulation.plugins.resources.ResourcesPluginId;
import gov.hhs.aspr.ms.util.errors.ContractException;

/**
 * A plugin providing a materials data manager to the simulation.
 */
public final class MaterialsPlugin {

	private static class Data {
		private BatchStatusReportPluginData batchStatusReportPluginData;
		private MaterialsProducerPropertyReportPluginData materialsProducerPropertyReportPluginData;
		private MaterialsProducerResourceReportPluginData materialsProducerResourceReportPluginData;
		private StageReportPluginData stageReportPluginData;
		private MaterialsPluginData materialsPluginData;
	}

	private MaterialsPlugin() {
	}

	public static Builder builder() {
		return new Builder();
	}

	public static class Builder {
		private Builder() {
		}

		private Data data = new Data();

		private void validate() {
			if (data.materialsPluginData == null) {
				throw new ContractException(MaterialsError.NULL_MATERIALS_PLUGIN_DATA);
			}
		}

		/**
		 * Builds the PersonPropertiesPlugin from the collected inputs
		 * 
		 * @throws ContractException {@linkplain MaterialsError#NULL_MATERIALS_PLUGIN_DATA}
		 *                           if the materials plugin data is null
		 */
		public Plugin getMaterialsPlugin() {

			validate();
			Plugin.Builder builder = Plugin.builder();//
			builder.setPluginId(MaterialsPluginId.PLUGIN_ID);//

			builder.addPluginData(data.materialsPluginData);//

			if (data.batchStatusReportPluginData != null) {
				builder.addPluginData(data.batchStatusReportPluginData);//
			}
			if (data.materialsProducerPropertyReportPluginData != null) {
				builder.addPluginData(data.materialsProducerPropertyReportPluginData);//
			}
			if (data.materialsProducerResourceReportPluginData != null) {
				builder.addPluginData(data.materialsProducerResourceReportPluginData);//
			}
			if (data.stageReportPluginData != null) {
				builder.addPluginData(data.stageReportPluginData);//
			}

			builder.addPluginDependency(RegionsPluginId.PLUGIN_ID);//
			builder.addPluginDependency(ResourcesPluginId.PLUGIN_ID);//

			builder.setInitializer((c) -> {

				MaterialsPluginData pluginData = c.getPluginData(MaterialsPluginData.class).get();
				c.addDataManager(new MaterialsDataManager(pluginData));

				Optional<BatchStatusReportPluginData> optional1 = c.getPluginData(BatchStatusReportPluginData.class);
				if (optional1.isPresent()) {
					BatchStatusReportPluginData batchStatusReportPluginData = optional1.get();
					c.addReport(new BatchStatusReport(batchStatusReportPluginData)::init);
				}

				Optional<MaterialsProducerPropertyReportPluginData> optional2 = c
						.getPluginData(MaterialsProducerPropertyReportPluginData.class);
				if (optional2.isPresent()) {
					MaterialsProducerPropertyReportPluginData materialsProducerPropertyReportPluginData = optional2
							.get();
					c.addReport(new MaterialsProducerPropertyReport(materialsProducerPropertyReportPluginData)::init);
				}

				Optional<MaterialsProducerResourceReportPluginData> optional3 = c
						.getPluginData(MaterialsProducerResourceReportPluginData.class);
				if (optional3.isPresent()) {
					MaterialsProducerResourceReportPluginData materialsProducerResourceReportPluginData = optional3
							.get();
					c.addReport(new MaterialsProducerResourceReport(materialsProducerResourceReportPluginData)::init);
				}

				Optional<StageReportPluginData> optional4 = c.getPluginData(StageReportPluginData.class);
				if (optional4.isPresent()) {
					StageReportPluginData stageReportPluginData = optional4.get();
					c.addReport(new StageReport(stageReportPluginData)::init);
				}

			});
			return builder.build();

		}

		public Builder setBatchStatusReportPluginData(BatchStatusReportPluginData batchStatusReportPluginData) {
			data.batchStatusReportPluginData = batchStatusReportPluginData;
			return this;
		}

		public Builder setMaterialsProducerPropertyReportPluginData(
				MaterialsProducerPropertyReportPluginData materialsProducerPropertyReportPluginData) {
			data.materialsProducerPropertyReportPluginData = materialsProducerPropertyReportPluginData;
			return this;
		}

		public Builder setMaterialsProducerResourceReportPluginData(
				MaterialsProducerResourceReportPluginData materialsProducerResourceReportPluginData) {
			data.materialsProducerResourceReportPluginData = materialsProducerResourceReportPluginData;
			return this;
		}

		public Builder setStageReportPluginData(StageReportPluginData stageReportPluginData) {
			data.stageReportPluginData = stageReportPluginData;
			return this;
		}

		public Builder setMaterialsPluginData(MaterialsPluginData materialsPluginData) {
			data.materialsPluginData = materialsPluginData;
			return this;
		}
	}

	// public static Plugin getMaterialsPlugin(MaterialsPluginData
	// materialsPluginData) {
	//
	// return Plugin .builder()//
	// .setPluginId(MaterialsPluginId.PLUGIN_ID)//
	// .addPluginData(materialsPluginData)//
	// .addPluginDependency(RegionsPluginId.PLUGIN_ID)//
	// .addPluginDependency(ResourcesPluginId.PLUGIN_ID)//
	// .setInitializer((c) -> {
	// MaterialsPluginData pluginData =
	// c.getPluginData(MaterialsPluginData.class).get();
	// c.addDataManager(new MaterialsDataManager(pluginData));
	// }).build();
	//
	// }

}
