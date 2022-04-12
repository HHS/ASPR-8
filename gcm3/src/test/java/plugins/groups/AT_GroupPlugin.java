package plugins.groups;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.LinkedHashSet;
import java.util.Set;

import org.junit.jupiter.api.Test;

import nucleus.Plugin;
import nucleus.PluginId;
import plugins.groups.testsupport.GroupsActionSupport;
import plugins.partitions.PartitionsPluginId;
import plugins.people.PeoplePluginId;
import plugins.reports.ReportsPluginId;
import plugins.stochastics.StochasticsPluginId;
import tools.annotations.UnitTest;
import tools.annotations.UnitTestMethod;

@UnitTest(target = GroupPlugin.class)
public class AT_GroupPlugin {
	

	@Test
	@UnitTestMethod(name = "getGroupPlugin", args = { GroupPluginData.class })
	public void testGetGroupPlugin() {

		GroupPluginData groupPluginData = GroupPluginData.builder().build();
		Plugin groupPlugin = GroupPlugin.getGroupPlugin(groupPluginData);

		assertEquals(1,groupPlugin.getPluginDatas().size());
		assertTrue(groupPlugin.getPluginDatas().contains(groupPluginData));

		assertEquals(GroupPluginId.PLUGIN_ID, groupPlugin.getPluginId());

		Set<PluginId> expectedDependencies = new LinkedHashSet<>();
		expectedDependencies.add(PartitionsPluginId.PLUGIN_ID);
		expectedDependencies.add(PeoplePluginId.PLUGIN_ID);
		expectedDependencies.add(ReportsPluginId.PLUGIN_ID);
		expectedDependencies.add(StochasticsPluginId.PLUGIN_ID);
		
		assertEquals(expectedDependencies, groupPlugin.getPluginDependencies());

		GroupsActionSupport.testConsumer(0,3,10, 924462486121444909L, (c) -> {
			assertTrue(c.getDataManager(GroupDataManager.class).isPresent());
		});

	}

}
