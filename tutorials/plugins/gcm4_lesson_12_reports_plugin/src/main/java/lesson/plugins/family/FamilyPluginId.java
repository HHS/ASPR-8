package lesson.plugins.family;

import nucleus.PluginId;
import nucleus.SimplePluginId;
/**
 * Static plugin id implementation for the Family Plugin
 * 
 *
 */

public final class FamilyPluginId implements PluginId {
	private FamilyPluginId() {};
	public final static PluginId PLUGIN_ID = new SimplePluginId("family plugin id");
	
}
