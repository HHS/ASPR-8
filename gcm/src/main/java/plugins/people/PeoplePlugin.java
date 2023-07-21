package plugins.people;

import nucleus.Plugin;
import plugins.people.datamanagers.PeopleDataManager;
import plugins.people.datamanagers.PeoplePluginData;

/**
 * A nucleus plugin for representing people, dealing only with their existence.
 * 
 *
 */

public final class PeoplePlugin {

	private PeoplePlugin() {

	}

	/**
	 * Returns the people plugin.
	 *
	 * <P>
	 * Uses PeoplePluginId.PLUGIN_ID as its id
	 * </P>
	 * 
	 * <P>
	 * Depends on plugins: none
	 * 
	 * <P>
	 * Provides data mangers:
	 * <ul>
	 * <li>{@linkplain PeopleDataManager}</li>
	 * </ul>
	 * </P>
	 * 
	 * 
	 * 
	 */
	public static Plugin getPeoplePlugin(PeoplePluginData peoplePluginData) {

		return Plugin	.builder()//
						.addPluginData(peoplePluginData)//
						.setPluginId(PeoplePluginId.PLUGIN_ID)//
						.setInitializer((c) -> {
							PeoplePluginData pluginData = c.getPluginData(PeoplePluginData.class).get();
							c.addDataManager(new PeopleDataManager(pluginData));							
						})//
						.build();
	}

}
