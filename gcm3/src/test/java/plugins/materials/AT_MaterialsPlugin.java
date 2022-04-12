package plugins.materials;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.LinkedHashSet;
import java.util.Set;

import org.junit.jupiter.api.Test;

import annotations.UnitTest;
import annotations.UnitTestMethod;
import nucleus.Plugin;
import nucleus.PluginId;
import plugins.materials.datamangers.MaterialsDataManager;
import plugins.materials.testsupport.MaterialsActionSupport;
import plugins.regions.RegionPluginId;
import plugins.reports.ReportsPluginId;
import plugins.resources.ResourcesPluginId;

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
		expectedDependencies.add(ReportsPluginId.PLUGIN_ID);
		expectedDependencies.add(ResourcesPluginId.PLUGIN_ID);
		assertEquals(expectedDependencies, materialsPlugin.getPluginDependencies());

		MaterialsActionSupport.testConsumer(924462486121444909L, (c) -> {
			assertTrue(c.getDataManager(MaterialsDataManager.class).isPresent());
		});

	}

}
