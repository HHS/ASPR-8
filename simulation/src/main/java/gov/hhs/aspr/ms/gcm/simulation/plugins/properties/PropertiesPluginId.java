package gov.hhs.aspr.ms.gcm.simulation.plugins.properties;

import gov.hhs.aspr.ms.gcm.simulation.nucleus.PluginId;

/**
 * Static plugin id implementation for the GlobalsPlugin
 */
public final class PropertiesPluginId implements PluginId {
	public final static PluginId PLUGIN_ID = new PropertiesPluginId();

	private PropertiesPluginId() {
	};
}
