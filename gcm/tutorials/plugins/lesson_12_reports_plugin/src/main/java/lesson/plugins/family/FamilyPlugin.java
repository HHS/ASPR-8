package lesson.plugins.family;

import lesson.plugins.family.datamanagers.FamilyDataManager;
import lesson.plugins.person.PersonPluginId;
import nucleus.Plugin;

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
