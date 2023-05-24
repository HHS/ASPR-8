package plugins.partitions;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.LinkedHashSet;
import java.util.Set;

import org.junit.jupiter.api.Test;

import nucleus.Plugin;
import nucleus.PluginId;
import plugins.partitions.datamanagers.PartitionsPluginData;
import plugins.people.PeoplePluginId;
import plugins.stochastics.StochasticsPluginId;
import util.annotations.UnitTestMethod;

public final class AT_PartitionsPlugin {

	@Test
	@UnitTestMethod(target = PartitionsPlugin.class, name = "getPartitionsPlugin", args = { PluginId[].class })
	public void testGetPartitionsPlugin() {
		
		PartitionsPluginData partitionsPluginData = PartitionsPluginData.builder().build();
		
		Plugin partitionsPlugin = PartitionsPlugin.builder()//		
		.setPartitionsPluginData(partitionsPluginData)//
		.getPartitionsPlugin();

		assertEquals(1,partitionsPlugin.getPluginDatas().size());
		assertTrue(partitionsPlugin.getPluginDatas().contains(partitionsPluginData));

		assertEquals(PartitionsPluginId.PLUGIN_ID, partitionsPlugin.getPluginId());

		Set<PluginId> expectedDependencies = new LinkedHashSet<>();
		expectedDependencies.add(PeoplePluginId.PLUGIN_ID);
		expectedDependencies.add(StochasticsPluginId.PLUGIN_ID);
		assertEquals(expectedDependencies, partitionsPlugin.getPluginDependencies());

	}

}
