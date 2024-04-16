package gov.hhs.aspr.ms.gcm.lessons.plugins.family;

import gov.hhs.aspr.ms.gcm.simulation.nucleus.PluginId;
import gov.hhs.aspr.ms.gcm.simulation.nucleus.SimplePluginId;

/**
 * Static plugin id implementation for the Family Plugin
 * 
 *
 */

public final class FamilyPluginId implements PluginId {
	private FamilyPluginId() {
	};

	public final static PluginId PLUGIN_ID = new SimplePluginId("family plugin id");

}
