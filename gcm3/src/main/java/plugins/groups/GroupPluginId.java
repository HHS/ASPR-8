package plugins.groups;

import nucleus.PluginId;
/**
 * Static plugin id implementation for the GroupPlugin
 * 
 * @author Shawn Hatch
 *
 */

public final class GroupPluginId implements PluginId {
	public final static PluginId PLUGIN_ID = new GroupPluginId();
	private GroupPluginId() {};
}
