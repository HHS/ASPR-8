package plugins.resources;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.LinkedHashSet;
import java.util.Set;

import org.junit.jupiter.api.Test;

import nucleus.Plugin;
import nucleus.PluginId;
import plugins.people.PeoplePluginId;
import plugins.regions.RegionsPluginId;
import util.annotations.UnitTestMethod;

public class AT_ResourcesPlugin {

	@Test
	@UnitTestMethod(target = ResourcesPlugin.class, name = "getResourcesPlugin", args = { ResourcesPluginData.class })
	public void testGetResourcesPlugin() {

		ResourcesPluginData resourcesPluginData = ResourcesPluginData.builder().build();
		Plugin resourcesPlugin = ResourcesPlugin.builder().setResourcesPluginData(resourcesPluginData).getResourcesPlugin();

		assertEquals(1, resourcesPlugin.getPluginDatas().size());
		assertTrue(resourcesPlugin.getPluginDatas().contains(resourcesPluginData));

		assertEquals(ResourcesPluginId.PLUGIN_ID, resourcesPlugin.getPluginId());

		Set<PluginId> expectedDependencies = new LinkedHashSet<>();
		expectedDependencies.add(PeoplePluginId.PLUGIN_ID);
		expectedDependencies.add(RegionsPluginId.PLUGIN_ID);

		assertEquals(expectedDependencies, resourcesPlugin.getPluginDependencies());

	}

}
