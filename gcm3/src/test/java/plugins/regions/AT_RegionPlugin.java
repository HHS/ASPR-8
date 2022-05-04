package plugins.regions;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.LinkedHashSet;
import java.util.Set;

import org.junit.jupiter.api.Test;

import nucleus.Plugin;
import nucleus.PluginId;
import plugins.people.PeoplePluginId;
import tools.annotations.UnitTest;
import tools.annotations.UnitTestMethod;

@UnitTest(target = RegionPlugin.class)
public class AT_RegionPlugin {

	

	@Test
	@UnitTestMethod(name = "getRegionPlugin", args = {RegionPluginData.class})
	public void testGetRegionPlugin() {
		RegionPluginData regionPluginData = RegionPluginData.builder().build();
		Plugin regionPlugin = RegionPlugin.getRegionPlugin(regionPluginData);

		assertEquals(1,regionPlugin.getPluginDatas().size());
		assertTrue(regionPlugin.getPluginDatas().contains(regionPluginData));

		assertEquals(RegionPluginId.PLUGIN_ID, regionPlugin.getPluginId());

		Set<PluginId> expectedDependencies = new LinkedHashSet<>();
		expectedDependencies.add(PeoplePluginId.PLUGIN_ID);
		
		assertEquals(expectedDependencies, regionPlugin.getPluginDependencies());

	}


}
