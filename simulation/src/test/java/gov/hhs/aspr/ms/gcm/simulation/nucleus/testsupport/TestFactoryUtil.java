package gov.hhs.aspr.ms.gcm.simulation.nucleus.testsupport;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import gov.hhs.aspr.ms.gcm.simulation.nucleus.Plugin;
import gov.hhs.aspr.ms.gcm.simulation.nucleus.PluginData;
import gov.hhs.aspr.ms.gcm.simulation.nucleus.PluginId;

public class TestFactoryUtil {
	/*
	 * Given a list of plugins, will show that the plugin with the given pluginId
	 * exists, and exists EXACTLY once.
	 */
	public static Plugin checkPluginExists(List<Plugin> plugins, PluginId pluginId) {
		Plugin actualPlugin = null;
		for (Plugin plugin : plugins) {
			if (plugin.getPluginId().equals(pluginId)) {
				assertNull(actualPlugin);
				actualPlugin = plugin;
			}
		}
		assertNotNull(actualPlugin);

		return actualPlugin;
	}

	/**
	 * Given a list of plugins, will show that the explicit plugindata for the given
	 * pluginid exists, and exists EXACTLY once.
	 */
	public static <T extends PluginData> void checkPluginDataExists(List<Plugin> plugins, T expectedPluginData,
			PluginId pluginId) {
		assertNotNull(expectedPluginData);
		Plugin plugin = checkPluginExists(plugins, pluginId);		
		List<PluginData> pluginDatas = plugin.getPluginDatas();
		assertNotNull(pluginDatas);
		assertTrue(pluginDatas.contains(expectedPluginData));
	}
}
