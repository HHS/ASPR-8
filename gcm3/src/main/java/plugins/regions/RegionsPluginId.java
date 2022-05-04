package plugins.regions;

import nucleus.PluginId;
/**
 * Static plugin id implementation for the GlobalsPlugin
 * 
 * @author Shawn Hatch
 *
 */

public final class RegionPluginId implements PluginId {
	public final static PluginId PLUGIN_ID = new RegionPluginId();
	private RegionPluginId() {};
}
