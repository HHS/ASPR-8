package gov.hhs.aspr.ms.gcm.lessons.plugins.vaccine;

import gov.hhs.aspr.ms.gcm.simulation.nucleus.PluginId;
import gov.hhs.aspr.ms.gcm.simulation.nucleus.SimplePluginId;

/**
 * Static plugin id implementation for the Vaccine Plugin
 * 
 *
 */

public final class VaccinePluginId implements PluginId {
	private VaccinePluginId() {
	};

	public final static PluginId PLUGIN_ID = new SimplePluginId("vaccine plugin id");

}
