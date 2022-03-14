package plugins.reports;

import nucleus.PluginId;

/**
 * Static plugin id implementation for the ReportsPlugin
 * 
 * @author Shawn Hatch
 *
 */

public final class ReportsPluginId implements PluginId {
	public final static PluginId PLUGIN_ID = new ReportsPluginId();
	private ReportsPluginId() {};
}
