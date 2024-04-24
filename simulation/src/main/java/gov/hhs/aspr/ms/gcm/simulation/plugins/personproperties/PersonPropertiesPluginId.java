package gov.hhs.aspr.ms.gcm.simulation.plugins.personproperties;

import gov.hhs.aspr.ms.gcm.simulation.nucleus.PluginId;

/**
 * Static plugin id implementation for the person properties plugin
 */
public final class PersonPropertiesPluginId implements PluginId {
	public final static PluginId PLUGIN_ID = new PersonPropertiesPluginId();

	private PersonPropertiesPluginId() {
	};
}
