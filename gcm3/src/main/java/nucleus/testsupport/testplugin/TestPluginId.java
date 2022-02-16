package nucleus.testsupport.testplugin;

import nucleus.PluginId;
import nucleus.SimplePluginId;

public class TestPluginId {
	private TestPluginId() {}
	public final static PluginId PLUGIN_ID = new SimplePluginId(TestPluginId.class);
}
