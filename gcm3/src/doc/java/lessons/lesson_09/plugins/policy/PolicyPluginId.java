package lessons.lesson_09.plugins.policy;

import nucleus.PluginId;
import nucleus.SimplePluginId;
/**
 * Static plugin id implementation for the GlobalsPlugin
 * 
 * @author Shawn Hatch
 *
 */

public final class PolicyPluginId implements PluginId {
	private PolicyPluginId() {};
	public final static PluginId PLUGIN_ID = new SimplePluginId("policy plugin id");
	
}
