package lesson.plugins.vaccine;

import lesson.plugins.family.FamilyPluginId;
import lesson.plugins.person.PersonPluginId;
import nucleus.Plugin;

public class VaccinePlugin {

	private VaccinePlugin() {
	}

	public static Plugin getVaccinePlugin() {

		return Plugin	.builder()//
						.setPluginId(VaccinePluginId.PLUGIN_ID)//
						.addPluginDependency(PersonPluginId.PLUGIN_ID)//
						.addPluginDependency(FamilyPluginId.PLUGIN_ID)//
						.setInitializer((c) -> {
							c.addDataManager(new VaccinationDataManager());
						})//
						.build();

	}
}
