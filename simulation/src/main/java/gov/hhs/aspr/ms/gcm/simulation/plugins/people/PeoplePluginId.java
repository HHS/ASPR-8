package gov.hhs.aspr.ms.gcm.simulation.plugins.people;

import gov.hhs.aspr.ms.gcm.simulation.nucleus.PluginId;

/**
 * Static plugin id implementation for the GlobalsPlugin
 */
public final class PeoplePluginId implements PluginId {
	public final static PluginId PLUGIN_ID = new PeoplePluginId();

	private PeoplePluginId() {
	};
}
