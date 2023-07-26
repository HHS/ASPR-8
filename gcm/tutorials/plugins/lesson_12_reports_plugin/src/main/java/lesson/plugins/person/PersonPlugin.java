package lesson.plugins.person;

import gov.hhs.aspr.ms.gcm.nucleus.Plugin;
import lesson.plugins.person.datamanagers.PersonDataManager;

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
