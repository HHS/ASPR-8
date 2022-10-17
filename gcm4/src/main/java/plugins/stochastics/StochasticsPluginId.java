package plugins.stochastics;

import nucleus.PluginId;
/**
 * Static plugin id implementation for the StochasticsPlugin
 * 
 * @author Shawn Hatch
 *
 */
public final class StochasticsPluginId implements PluginId {
	public final static PluginId PLUGIN_ID = new StochasticsPluginId();
	private StochasticsPluginId() {};
}
