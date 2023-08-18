package gov.hhs.aspr.ms.gcm.plugins.regions;

import gov.hhs.aspr.ms.gcm.nucleus.PluginId;

/**
 * Static plugin id implementation for the GlobalsPlugin
 */

public final class RegionsPluginId implements PluginId {
	public final static PluginId PLUGIN_ID = new RegionsPluginId();

	private RegionsPluginId() {
	};
}
