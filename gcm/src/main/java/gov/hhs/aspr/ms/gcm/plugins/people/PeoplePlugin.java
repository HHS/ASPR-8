package gov.hhs.aspr.ms.gcm.plugins.people;

import gov.hhs.aspr.ms.gcm.nucleus.Plugin;
import gov.hhs.aspr.ms.gcm.plugins.people.datamanagers.PeopleDataManager;
import gov.hhs.aspr.ms.gcm.plugins.people.datamanagers.PeoplePluginData;

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
	 * <p>
	 * Uses PeoplePluginId.PLUGIN_ID as its id
	 * </p>
	 * 
	 * <p>
	 * Depends on plugins: none
	 * 
	 * <p>
	 * Provides data mangers:
	 * <ul>
	 * <li>{@linkplain PeopleDataManager}</li>
	 * </ul>
	 * </p>
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
