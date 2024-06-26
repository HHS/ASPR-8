package gov.hhs.aspr.ms.gcm.lessons.plugins.disease;

import gov.hhs.aspr.ms.gcm.simulation.nucleus.Plugin;

public final class DiseasePlugin {

	private DiseasePlugin() {

	}

	public static Plugin getDiseasePlugin(DiseasePluginData diseasePluginData) {

		return Plugin.builder()//
				.addPluginData(diseasePluginData)//
				.setPluginId(DiseasePluginId.PLUGIN_ID)//
				.setInitializer((c) -> {
					DiseasePluginData pluginData = c.getPluginData(DiseasePluginData.class).get();
					c.addDataManager(new DiseaseDataManager(pluginData));
				})//
				.build();
	}

}
