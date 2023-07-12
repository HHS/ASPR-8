package plugins.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import nucleus.Plugin;
import nucleus.PluginData;
import nucleus.PluginId;

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
            PluginId pluginId, int numPluginDatas) {
        Plugin actualPlugin = checkPluginExists(plugins, pluginId);
        List<PluginData> actualPluginDatas = actualPlugin.getPluginDatas();
        assertNotNull(actualPluginDatas);
        assertEquals(numPluginDatas, actualPluginDatas.size());

        if (numPluginDatas > 1) {
            for (PluginData pluginData : actualPluginDatas) {
                if (expectedPluginData.getClass().isAssignableFrom(pluginData.getClass())) {
                    assertTrue(expectedPluginData == pluginData);
                    break;
                }
            }
        } else {
            PluginData actualPluginData = actualPluginDatas.get(0);
            assertTrue(expectedPluginData == actualPluginData);
        }

    }

    /**
     * Given a list of plugins, will show that the explicit plugindata for the given
     * pluginid exists, and exists EXACTLY once.
     */
    public static <T extends PluginData> void checkPluginDataExists(List<Plugin> plugins, T expectedPluginData,
            PluginId pluginId) {
        checkPluginDataExists(plugins, expectedPluginData, pluginId, 1);
    }
}
