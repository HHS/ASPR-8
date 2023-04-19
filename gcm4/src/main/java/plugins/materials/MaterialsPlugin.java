package plugins.materials;

import java.util.Optional;

import nucleus.Plugin;
import plugins.materials.datamangers.MaterialsDataManager;
import plugins.materials.reports.BatchStatusReport;
import plugins.materials.reports.BatchStatusReportPluginData;
import plugins.materials.reports.MaterialsProducerPropertyReport;
import plugins.materials.reports.MaterialsProducerPropertyReportPluginData;
import plugins.materials.reports.MaterialsProducerResourceReport;
import plugins.materials.reports.MaterialsProducerResourceReportPluginData;
import plugins.materials.reports.StageReport;
import plugins.materials.reports.StageReportPluginData;
import plugins.materials.support.MaterialsError;
import plugins.regions.RegionsPluginId;
import plugins.resources.ResourcesPluginId;
import plugins.resources.support.ResourceError;
import util.errors.ContractException;

/**
 * A plugin providing a materials data manager to the simulation.
 * 
 *
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
		 * @throws ContractException
		 *             <li>{@linkplain ResourceError#NULL_RESOURCE_PLUGIN_DATA}
		 *             if the personPropertiesPluginData is null</li>
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

				Optional<MaterialsProducerPropertyReportPluginData> optional2 = c.getPluginData(MaterialsProducerPropertyReportPluginData.class);
				if (optional2.isPresent()) {
					MaterialsProducerPropertyReportPluginData materialsProducerPropertyReportPluginData = optional2.get();
					c.addReport(new MaterialsProducerPropertyReport(materialsProducerPropertyReportPluginData)::init);
				}

				Optional<MaterialsProducerResourceReportPluginData> optional3 = c.getPluginData(MaterialsProducerResourceReportPluginData.class);
				if (optional3.isPresent()) {
					MaterialsProducerResourceReportPluginData materialsProducerResourceReportPluginData = optional3.get();
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

		public Builder setMaterialsProducerPropertyReportPluginData(MaterialsProducerPropertyReportPluginData materialsProducerPropertyReportPluginData) {
			data.materialsProducerPropertyReportPluginData = materialsProducerPropertyReportPluginData;
			return this;
		}

		public Builder setMaterialsProducerResourceReportPluginData(MaterialsProducerResourceReportPluginData materialsProducerResourceReportPluginData) {
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
