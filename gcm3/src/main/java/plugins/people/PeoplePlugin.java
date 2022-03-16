package plugins.people;

import nucleus.Plugin;
import plugins.people.actors.PeopleLoader;

/**
 * A nucleus plugin for representing people, dealing only with their existence.
 * 
 * @author Shawn Hatch
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
	 * <li>{@linkplain PersonDataManager}</li>
	 * </ul>
	 * </P>
	 * 
	 * <P>
	 * Provides actors:
	 * <ul>
	 * <li>{@linkplain PeopleLoader} for loading people from the
	 * PeoplePluginData</li>
	 * </ul>
	 * </P>
	 * 
	 */
	public static Plugin getPeoplePlugin(PeoplePluginData peoplePluginData) {

		return Plugin	.builder()//
						.addPluginData(peoplePluginData)//
						.setPluginId(PeoplePluginId.PLUGIN_ID)//
						.setInitializer((c) -> {
							PeoplePluginData pluginData = c.getPluginData(PeoplePluginData.class).get();
							c.addDataManager(new PersonDataManager());
							c.addActor(new PeopleLoader(pluginData)::init);
						})//
						.build();
	}

}
