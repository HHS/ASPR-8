package gov.hhs.aspr.ms.gcm.nucleus.testsupport.testplugin;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import gov.hhs.aspr.ms.gcm.nucleus.Plugin;
import gov.hhs.aspr.ms.gcm.nucleus.PluginId;

/**
 * A static utility class for comparing the content of two lists of plugins.
 * 
 *
 *
 */

public final class PluginEquivalence {
	
	private PluginEquivalence() {}
	/**
	 * Asserts that two lists of plugins are equivalent. Two lists of plugins
	 * are considered equivalent they are not null, contain no null entries and
	 * contain the same set of plugins.
	 */
	public static void assertEquivalent(List<Plugin> expectedPlugins, List<Plugin> actualPlugins) {

		assertNotNull(expectedPlugins, "expected plugins list is null");
		assertNotNull(actualPlugins, "actual plugins list is null");

		Map<PluginId, Plugin> expectedMap = new LinkedHashMap<>();
		for (Plugin plugin : expectedPlugins) {
			assertNotNull(plugin, "expected plugins contains a null plugin");
			assertNull(expectedMap.put(plugin.getPluginId(), plugin), "expected plugins contains a duplicate plugin for " + plugin.getPluginId());
		}
		for (Plugin plugin : actualPlugins) {
			assertNotNull(plugin, "actual plugins contains a null plugin");
			assertNull(expectedMap.put(plugin.getPluginId(), plugin), "actual plugins contains a duplicate plugin for " + plugin.getPluginId());
		}

		Map<PluginId, Plugin> actualMap = new LinkedHashMap<>();
		for (Plugin plugin : actualPlugins) {
			assertNotNull(plugin, "actual plugins contains a null plugin");
			assertNull(expectedMap.put(plugin.getPluginId(), plugin), "actual plugins contains a duplicate plugin for " + plugin.getPluginId());
		}
		for (Plugin plugin : actualPlugins) {
			assertNotNull(plugin, "actual plugins contains a null plugin");
			assertNull(expectedMap.put(plugin.getPluginId(), plugin), "actual plugins contains a duplicate plugin for " + plugin.getPluginId());
		}

		assertEquals(expectedMap.keySet(), actualMap.keySet(), "expected and actual plugins do not contain the same plugin ids");

		for (PluginId pluginId : expectedMap.keySet()) {
			Plugin expectedPlugin = expectedMap.get(pluginId);
			Plugin actualPlugin = actualMap.get(pluginId);
			assertEquals(expectedPlugin, actualPlugin, "Plugin equality failure for " + pluginId);
		}
	}
}
