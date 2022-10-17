package plugins.people;

import nucleus.PluginId;
/**
 * Static plugin id implementation for the GlobalsPlugin
 * 
 * @author Shawn Hatch
 *
 */

public final class PeoplePluginId implements PluginId {
	public final static PluginId PLUGIN_ID = new PeoplePluginId();
	private PeoplePluginId() {};
}
