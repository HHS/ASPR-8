package lessons.lesson_11.plugins.model;

import nucleus.PluginId;
import nucleus.SimplePluginId;
/**
 * Static plugin id implementation for the GlobalsPlugin
 * 
 * @author Shawn Hatch
 *
 */

public final class ModelPluginId implements PluginId {
	private ModelPluginId() {};
	public final static PluginId PLUGIN_ID = new SimplePluginId("model plugin id");
	
}
