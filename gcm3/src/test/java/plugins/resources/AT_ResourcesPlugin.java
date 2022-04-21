package plugins.resources;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.LinkedHashSet;
import java.util.Set;

import org.junit.jupiter.api.Test;

import nucleus.Plugin;
import nucleus.PluginId;
import plugins.partitions.PartitionsPluginId;
import plugins.people.PeoplePluginId;
import plugins.regions.RegionPluginId;
import plugins.reports.ReportsPluginId;
import tools.annotations.UnitTest;
import tools.annotations.UnitTestMethod;

@UnitTest(target = ResourcesPlugin.class)
public class AT_ResourcesPlugin {

	

	@Test
	@UnitTestMethod(name = "getResourcesPlugin(ResourcesPluginData)", args = { ResourcesPluginData.class })
	public void testInit() {

		ResourcesPluginData resourcesPluginData = ResourcesPluginData.builder().build();
		Plugin resourcesPlugin = ResourcesPlugin.getResourcesPlugin(resourcesPluginData);

		assertEquals(1,resourcesPlugin.getPluginDatas().size());
		assertTrue(resourcesPlugin.getPluginDatas().contains(resourcesPluginData));

		assertEquals(ResourcesPluginId.PLUGIN_ID, resourcesPlugin.getPluginId());

		Set<PluginId> expectedDependencies = new LinkedHashSet<>();
		expectedDependencies.add(PartitionsPluginId.PLUGIN_ID);
		expectedDependencies.add(PeoplePluginId.PLUGIN_ID);
		expectedDependencies.add(ReportsPluginId.PLUGIN_ID);
		expectedDependencies.add(RegionPluginId.PLUGIN_ID);
		
		assertEquals(expectedDependencies, resourcesPlugin.getPluginDependencies());

		

	}

}
