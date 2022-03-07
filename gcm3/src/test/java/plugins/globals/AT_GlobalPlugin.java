package plugins.globals;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.LinkedHashSet;
import java.util.Set;

import org.junit.jupiter.api.Test;

import annotations.UnitTest;
import annotations.UnitTestMethod;
import nucleus.Plugin;
import nucleus.PluginId;
import plugins.reports.ReportsPluginId;

@UnitTest(target = GlobalPlugin.class)
public class AT_GlobalPlugin {

	@Test
	@UnitTestMethod(name = "getPlugin", args = { GlobalPluginData.class })
	public void testInit() {
		/*
		 *Show that the plugin contains the plugin data and has the property id and dependencies 
		 */

		GlobalPluginData globalPluginData = GlobalPluginData.builder().build();
		Plugin globalsPlugin = GlobalPlugin.getPlugin(globalPluginData);

		assertTrue(globalsPlugin.getPluginDatas().contains(globalPluginData));
		assertEquals(GlobalsPluginId.PLUGIN_ID, globalsPlugin.getPluginId());

		Set<PluginId> expectedDependencies = new LinkedHashSet<>();		
		expectedDependencies.add(ReportsPluginId.PLUGIN_ID);
		assertEquals(expectedDependencies, globalsPlugin.getPluginDependencies());

	}

}
