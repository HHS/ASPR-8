package lessons.lesson_12.plugins.vaccine;

import nucleus.PluginId;
import nucleus.SimplePluginId;
/**
 * Static plugin id implementation for the Vaccine Plugin
 * 
 * @author Shawn Hatch
 *
 */

public final class VaccinePluginId implements PluginId {
	private VaccinePluginId() {};
	public final static PluginId PLUGIN_ID = new SimplePluginId("vaccine plugin id");
	
}
