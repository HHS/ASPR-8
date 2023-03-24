package plugins.regions;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.LinkedHashSet;
import java.util.Set;

import org.junit.jupiter.api.Test;

import nucleus.Plugin;
import nucleus.PluginId;
import plugins.people.PeoplePluginId;
import util.annotations.UnitTestMethod;

public class AT_RegionPlugin {

	

	@Test
	@UnitTestMethod(target = RegionsPlugin.class,name = "getRegionsPlugin", args = {RegionsPluginData.class})
	public void testGetRegionPlugin() {
		RegionsPluginData regionsPluginData = RegionsPluginData.builder().build();
		Plugin regionPlugin = RegionsPlugin.builder().setRegionsPluginData(regionsPluginData).getRegionsPlugin();

		assertEquals(1,regionPlugin.getPluginDatas().size());
		assertTrue(regionPlugin.getPluginDatas().contains(regionsPluginData));

		assertEquals(RegionsPluginId.PLUGIN_ID, regionPlugin.getPluginId());

		Set<PluginId> expectedDependencies = new LinkedHashSet<>();
		expectedDependencies.add(PeoplePluginId.PLUGIN_ID);
		
		assertEquals(expectedDependencies, regionPlugin.getPluginDependencies());

	}


}
