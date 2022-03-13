package nucleus.testsupport.testplugin;

import nucleus.PluginId;
import nucleus.SimplePluginId;
/**
 * Static plugin id implementation for the TestPlugin
 * 
 * @author Shawn Hatch
 *
 */
public class TestPluginId {
	private TestPluginId() {}
	public final static PluginId PLUGIN_ID = new SimplePluginId(TestPluginId.class);
}
