package lessons.lesson_12.plugins.family;

import nucleus.PluginId;
import nucleus.SimplePluginId;
/**
 * Static plugin id implementation for the Family Plugin
 * 
 * @author Shawn Hatch
 *
 */

public final class FamilyPluginId implements PluginId {
	private FamilyPluginId() {};
	public final static PluginId PLUGIN_ID = new SimplePluginId("family plugin id");
	
}
