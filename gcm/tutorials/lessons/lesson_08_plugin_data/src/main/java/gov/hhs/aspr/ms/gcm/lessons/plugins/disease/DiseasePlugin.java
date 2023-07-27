package gov.hhs.aspr.ms.gcm.lessons.plugins.disease;

import gov.hhs.aspr.ms.gcm.nucleus.Plugin;

/* start code_ref=plugin_data_plugin */
public final class DiseasePlugin {

	private DiseasePlugin() {

	}

	public static Plugin getDiseasePlugin(DiseasePluginData diseasePluginData) {

		return Plugin.builder()//
				.addPluginData(diseasePluginData)//
				.setPluginId(DiseasePluginId.PLUGIN_ID)//
				.setInitializer((pluginContext) -> {
					DiseasePluginData pluginData = pluginContext.getPluginData(DiseasePluginData.class).get();
					pluginContext.addDataManager(new DiseaseDataManager(pluginData));
				})//
				.build();
	}

}
/* end */