package gov.hhs.aspr.ms.gcm.simulation.plugins.globalproperties;

import gov.hhs.aspr.ms.gcm.simulation.nucleus.PluginId;

/**
 * Static plugin id implementation for the GlobalsPlugin
 */
public final class GlobalPropertiesPluginId implements PluginId {
	public final static PluginId PLUGIN_ID = new GlobalPropertiesPluginId();

	private GlobalPropertiesPluginId() {
	};
}
