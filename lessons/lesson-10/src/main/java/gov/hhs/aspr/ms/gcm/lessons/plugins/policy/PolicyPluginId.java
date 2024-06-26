package gov.hhs.aspr.ms.gcm.lessons.plugins.policy;

import gov.hhs.aspr.ms.gcm.simulation.nucleus.PluginId;
import gov.hhs.aspr.ms.gcm.simulation.nucleus.SimplePluginId;

/**
 * Static plugin id implementation for the GlobalsPlugin
 * 
 *
 */

public final class PolicyPluginId implements PluginId {
	private PolicyPluginId() {
	};

	public final static PluginId PLUGIN_ID = new SimplePluginId("policy plugin id");

}
