package gov.hhs.aspr.ms.gcm.simulation.plugins.stochastics;

import gov.hhs.aspr.ms.gcm.simulation.nucleus.PluginId;

/**
 * Static plugin id implementation for the StochasticsPlugin
 */
public final class StochasticsPluginId implements PluginId {
	public final static PluginId PLUGIN_ID = new StochasticsPluginId();

	private StochasticsPluginId() {
	};
}
