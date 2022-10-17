package plugins.resources;

import nucleus.PluginId;
/**
 * Static plugin id implementation for the GlobalsPlugin
 * 
 * @author Shawn Hatch
 *
 */

public final class ResourcesPluginId implements PluginId {
	public final static PluginId PLUGIN_ID = new ResourcesPluginId();
	private ResourcesPluginId() {};
}
