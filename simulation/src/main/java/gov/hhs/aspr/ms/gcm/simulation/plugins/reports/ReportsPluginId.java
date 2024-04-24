package gov.hhs.aspr.ms.gcm.simulation.plugins.reports;

import gov.hhs.aspr.ms.gcm.simulation.nucleus.PluginId;

/**
 * Static plugin id implementation for the ReportsPlugin
 */
public final class ReportsPluginId implements PluginId {
	public final static PluginId PLUGIN_ID = new ReportsPluginId();

	private ReportsPluginId() {
	};
}
