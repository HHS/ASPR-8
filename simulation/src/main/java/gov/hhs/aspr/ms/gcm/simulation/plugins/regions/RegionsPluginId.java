package gov.hhs.aspr.ms.gcm.simulation.plugins.regions;

import gov.hhs.aspr.ms.gcm.simulation.nucleus.PluginId;

/**
 * Static plugin id implementation for the GlobalsPlugin
 */
public final class RegionsPluginId implements PluginId {
	public final static PluginId PLUGIN_ID = new RegionsPluginId();

	private RegionsPluginId() {
	};
}
