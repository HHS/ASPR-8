package plugins.globalproperties;

import nucleus.PluginId;
/**
 * Static plugin id implementation for the GlobalsPlugin
 * 
 * @author Shawn Hatch
 *
 */

public final class GlobalPropertiesPluginId implements PluginId {
	public final static PluginId PLUGIN_ID = new GlobalPropertiesPluginId();
	private GlobalPropertiesPluginId() {};
}
