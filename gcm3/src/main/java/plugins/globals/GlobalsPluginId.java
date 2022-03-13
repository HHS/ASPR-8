package plugins.globals;

import nucleus.PluginId;
/**
 * Static plugin id implementation for the GlobalsPlugin
 * 
 * @author Shawn Hatch
 *
 */

public final class GlobalsPluginId implements PluginId {
	public final static PluginId PLUGIN_ID = new GlobalsPluginId();
	private GlobalsPluginId() {};
}
