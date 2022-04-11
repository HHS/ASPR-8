package plugins.materials;

import nucleus.PluginId;
/**
 * Static plugin id implementation for the materials plugin
 * 
 * @author Shawn Hatch
 *
 */

public final class MaterialsPluginId implements PluginId {
	public final static PluginId PLUGIN_ID = new MaterialsPluginId();
	private MaterialsPluginId() {};
}
