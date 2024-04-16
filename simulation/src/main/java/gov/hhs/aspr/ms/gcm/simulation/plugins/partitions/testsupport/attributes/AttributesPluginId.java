package gov.hhs.aspr.ms.gcm.simulation.plugins.partitions.testsupport.attributes;

import gov.hhs.aspr.ms.gcm.simulation.nucleus.PluginId;

/**
 * Static plugin id implementation for the Attributes Plugin
 */
public final class AttributesPluginId implements PluginId {
	public final static PluginId PLUGIN_ID = new AttributesPluginId();

	private AttributesPluginId() {
	};
}
