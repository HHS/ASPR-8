package plugins.partitions;

import nucleus.PluginId;
/**
 * Static plugin id implementation for the GlobalsPlugin
 * 
 * @author Shawn Hatch
 *
 */

public final class PartitionsPluginId implements PluginId {
	public final static PluginId PLUGIN_ID = new PartitionsPluginId();
	private PartitionsPluginId() {};
}
