package gov.hhs.aspr.ms.gcm.simulation.plugins.partitions;

import gov.hhs.aspr.ms.gcm.simulation.nucleus.PluginId;

/**
 * Static plugin id implementation for the GlobalsPlugin
 */
public final class PartitionsPluginId implements PluginId {
	public final static PluginId PLUGIN_ID = new PartitionsPluginId();

	private PartitionsPluginId() {
	};
}
