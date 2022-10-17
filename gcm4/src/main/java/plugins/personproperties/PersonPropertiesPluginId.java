package plugins.personproperties;

import nucleus.PluginId;

/**
 * Static plugin id implementation for the person properties plugin
 * 
 * @author Shawn Hatch
 *
 */

public final class PersonPropertiesPluginId implements PluginId {
	public final static PluginId PLUGIN_ID = new PersonPropertiesPluginId();

	private PersonPropertiesPluginId() {
	};
}
