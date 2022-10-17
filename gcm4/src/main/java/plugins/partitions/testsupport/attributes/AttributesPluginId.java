package plugins.partitions.testsupport.attributes;

import nucleus.PluginId;
/**
 * Static plugin id implementation for the Attributes Plugin
 * 
 * @author Shawn Hatch
 *
 */

public final class AttributesPluginId implements PluginId {
	public final static PluginId PLUGIN_ID = new AttributesPluginId();
	private AttributesPluginId() {};
}
