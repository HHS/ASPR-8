package plugins.materials;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.LinkedHashSet;
import java.util.Set;

import org.junit.jupiter.api.Test;

import nucleus.Plugin;
import nucleus.PluginId;
import plugins.regions.RegionPluginId;
import plugins.resources.ResourcesPluginId;
import tools.annotations.UnitTest;
import tools.annotations.UnitTestMethod;

@UnitTest(target = MaterialsPlugin.class)
public class AT_MaterialsPlugin {

	@Test
	@UnitTestMethod(name = "getMaterialsPlugin", args = { MaterialsPluginData.class })
	public void testGetMaterialsPlugin() {
		MaterialsPluginData materialsPluginData = MaterialsPluginData.builder().build();
		Plugin materialsPlugin = MaterialsPlugin.getMaterialsPlugin(materialsPluginData);

		assertEquals(1, materialsPlugin.getPluginDatas().size());
		assertTrue(materialsPlugin.getPluginDatas().contains(materialsPluginData));

		assertEquals(MaterialsPluginId.PLUGIN_ID, materialsPlugin.getPluginId());

		Set<PluginId> expectedDependencies = new LinkedHashSet<>();

		expectedDependencies.add(RegionPluginId.PLUGIN_ID);
		expectedDependencies.add(ResourcesPluginId.PLUGIN_ID);
		assertEquals(expectedDependencies, materialsPlugin.getPluginDependencies());

	}

}
