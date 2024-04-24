package gov.hhs.aspr.ms.gcm.lessons.plugins.person;

import gov.hhs.aspr.ms.gcm.lessons.plugins.person.datamanagers.PersonDataManager;
import gov.hhs.aspr.ms.gcm.simulation.nucleus.Plugin;

public class PersonPlugin {

	private PersonPlugin() {
	}

	public static Plugin getPersonPlugin() {

		return Plugin.builder()//
				.setPluginId(PersonPluginId.PLUGIN_ID)//
				.setInitializer((c) -> {
					c.addDataManager(new PersonDataManager());
				})//
				.build();
	}
}
