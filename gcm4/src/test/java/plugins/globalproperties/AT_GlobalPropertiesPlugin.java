package plugins.globalproperties;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.LinkedHashSet;
import java.util.Set;

import org.junit.jupiter.api.Test;

import nucleus.Plugin;
import nucleus.PluginId;
import tools.annotations.UnitTestMethod;

public class AT_GlobalPropertiesPlugin {

	@Test
	@UnitTestMethod(target = GlobalPropertiesPlugin.class, name = "getGlobalPropertiesPlugin", args = { GlobalPropertiesPluginData.class })
	public void testGetPlugin() {
		/*
		 * Show that the plugin contains the plugin data and has the property id
		 * and dependencies
		 */

		GlobalPropertiesPluginData globalPropertiesPluginData = GlobalPropertiesPluginData.builder().build();
		Plugin globalsPlugin = GlobalPropertiesPlugin.getGlobalPropertiesPlugin(globalPropertiesPluginData);

		assertTrue(globalsPlugin.getPluginDatas().contains(globalPropertiesPluginData));
		assertEquals(GlobalPropertiesPluginId.PLUGIN_ID, globalsPlugin.getPluginId());

		Set<PluginId> expectedDependencies = new LinkedHashSet<>();
		assertEquals(expectedDependencies, globalsPlugin.getPluginDependencies());

	}

}
