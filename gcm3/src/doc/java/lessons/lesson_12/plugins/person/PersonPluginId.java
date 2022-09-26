package lessons.lesson_12.plugins.person;

import nucleus.PluginId;
import nucleus.SimplePluginId;
/**
 * Static plugin id implementation for the Model Plugin
 * 
 * @author Shawn Hatch
 *
 */

public final class PersonPluginId implements PluginId {
	private PersonPluginId() {};
	public final static PluginId PLUGIN_ID = new SimplePluginId("person plugin id");
	
}
