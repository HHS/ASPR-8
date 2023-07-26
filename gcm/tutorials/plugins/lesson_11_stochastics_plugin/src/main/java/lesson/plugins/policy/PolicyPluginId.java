package lesson.plugins.policy;

import gov.hhs.aspr.ms.gcm.nucleus.PluginId;
import gov.hhs.aspr.ms.gcm.nucleus.SimplePluginId;

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
