package gov.hhs.aspr.ms.gcm.simulation.nucleus.testsupport.testplugin;

import gov.hhs.aspr.ms.gcm.simulation.nucleus.PluginId;
import gov.hhs.aspr.ms.gcm.simulation.nucleus.SimplePluginId;

/**
 * Static plugin id implementation for the TestPlugin
 */
public class TestPluginId {
	private TestPluginId() {
	}

	public final static PluginId PLUGIN_ID = new SimplePluginId(TestPluginId.class);
}
