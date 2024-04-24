package gov.hhs.aspr.ms.gcm.lessons.plugins.family;

import gov.hhs.aspr.ms.gcm.lessons.plugins.family.datamanagers.FamilyDataManager;
import gov.hhs.aspr.ms.gcm.lessons.plugins.person.PersonPluginId;
import gov.hhs.aspr.ms.gcm.simulation.nucleus.Plugin;

public class FamilyPlugin {

	private FamilyPlugin() {
	}

	public static Plugin getFamilyPlugin(FamilyPluginData familyPluginData) {

		return Plugin.builder()//
				.addPluginData(familyPluginData)//
				.setPluginId(FamilyPluginId.PLUGIN_ID)//
				.addPluginDependency(PersonPluginId.PLUGIN_ID)//
				.setInitializer((c) -> {
					FamilyPluginData pluginData = c.getPluginData(FamilyPluginData.class).get();
					c.addDataManager(new FamilyDataManager(pluginData));
				})//
				.build();
	}
}
