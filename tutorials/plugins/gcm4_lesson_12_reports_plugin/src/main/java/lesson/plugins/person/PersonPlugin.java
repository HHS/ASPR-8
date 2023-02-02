package lesson.plugins.person;

import lesson.plugins.person.datamanagers.PersonDataManager;
import nucleus.Plugin;

public class PersonPlugin {

	private PersonPlugin() {
	}

	public static Plugin getPersonPlugin() {

		return Plugin	.builder()//
						.setPluginId(PersonPluginId.PLUGIN_ID)//
						.setInitializer((c) -> {
							c.addDataManager(new PersonDataManager());
						})//
						.build();
	}
}
