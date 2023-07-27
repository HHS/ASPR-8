package lesson.plugins.family;

import gov.hhs.aspr.ms.gcm.nucleus.Plugin;
import lesson.plugins.family.datamanagers.FamilyDataManager;
import lesson.plugins.person.PersonPluginId;

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
