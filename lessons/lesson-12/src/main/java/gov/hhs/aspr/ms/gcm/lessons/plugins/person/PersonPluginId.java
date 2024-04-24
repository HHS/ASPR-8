package gov.hhs.aspr.ms.gcm.lessons.plugins.person;

import gov.hhs.aspr.ms.gcm.simulation.nucleus.PluginId;
import gov.hhs.aspr.ms.gcm.simulation.nucleus.SimplePluginId;

/**
 * Static plugin id implementation for the Model Plugin
 * 
 *
 */

public final class PersonPluginId implements PluginId {
	private PersonPluginId() {
	};

	public final static PluginId PLUGIN_ID = new SimplePluginId("person plugin id");

}
