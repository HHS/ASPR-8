package lesson.plugins.family;

import gov.hhs.aspr.ms.gcm.nucleus.PluginId;
import gov.hhs.aspr.ms.gcm.nucleus.SimplePluginId;

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
