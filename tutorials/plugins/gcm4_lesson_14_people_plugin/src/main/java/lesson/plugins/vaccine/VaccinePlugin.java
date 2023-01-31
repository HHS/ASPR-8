package lesson.plugins.vaccine;

import lesson.plugins.vaccine.datamanagers.VaccinationDataManager;
import nucleus.Plugin;
import plugins.people.PeoplePluginId;

public class VaccinePlugin {

	private VaccinePlugin() {
	}

	public static Plugin getVaccinePlugin() {

		return Plugin	.builder()//
						.setPluginId(VaccinePluginId.PLUGIN_ID)//
						.addPluginDependency(PeoplePluginId.PLUGIN_ID)//						
						.setInitializer((c) -> {
							c.addDataManager(new VaccinationDataManager());
						})//
						.build();

	}
}
