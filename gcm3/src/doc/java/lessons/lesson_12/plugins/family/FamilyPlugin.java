package lessons.lesson_12.plugins.family;

import lessons.lesson_12.plugins.person.PersonPluginId;
import nucleus.Plugin;

public class FamilyPlugin {
	
	private FamilyPlugin() {}
	
	public static Plugin getFamilyPlugin(FamilyPluginData familyPluginData) {

		return Plugin	.builder()//
						.addPluginData(familyPluginData)//
						.setPluginId(FamilyPluginId.PLUGIN_ID)//
						.addPluginDependency(PersonPluginId.PLUGIN_ID)//
						.setInitializer((c) -> {
							FamilyPluginData pluginData = c.getPluginData(FamilyPluginData.class);
							c.addDataManager(new FamilyDataManager(pluginData));
						})//
						.build();
	}
}
