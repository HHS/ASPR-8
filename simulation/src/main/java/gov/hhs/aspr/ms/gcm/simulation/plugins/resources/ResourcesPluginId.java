package gov.hhs.aspr.ms.gcm.simulation.plugins.resources;

import gov.hhs.aspr.ms.gcm.simulation.nucleus.PluginId;

/**
 * Static plugin id implementation for the GlobalsPlugin
 */
public final class ResourcesPluginId implements PluginId {
	public final static PluginId PLUGIN_ID = new ResourcesPluginId();

	private ResourcesPluginId() {
	};
}
