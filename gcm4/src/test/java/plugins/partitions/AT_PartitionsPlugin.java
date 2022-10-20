package plugins.partitions;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.LinkedHashSet;
import java.util.Set;

import org.junit.jupiter.api.Test;

import nucleus.Plugin;
import nucleus.PluginId;
import plugins.people.PeoplePluginId;
import plugins.stochastics.StochasticsPluginId;
import tools.annotations.UnitTest;
import tools.annotations.UnitTestMethod;

@UnitTest(target = PartitionsPlugin.class)
public final class AT_PartitionsPlugin {

	@Test
	@UnitTestMethod(name = "getPartitionsPlugin", args = {PluginId[].class})
	public void testGetPartitionsPlugin() {
		Plugin partitionsPlugin = PartitionsPlugin.getPartitionsPlugin();

		assertTrue(partitionsPlugin.getPluginDatas().isEmpty());

		assertEquals(PartitionsPluginId.PLUGIN_ID, partitionsPlugin.getPluginId());

		Set<PluginId> expectedDependencies = new LinkedHashSet<>();
		expectedDependencies.add(PeoplePluginId.PLUGIN_ID);
		expectedDependencies.add(StochasticsPluginId.PLUGIN_ID);
		assertEquals(expectedDependencies, partitionsPlugin.getPluginDependencies());

	}

}
