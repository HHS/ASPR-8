package plugins.groups;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.LinkedHashSet;
import java.util.Set;

import org.junit.jupiter.api.Test;

import nucleus.Plugin;
import nucleus.PluginId;
import plugins.people.PeoplePluginId;
import plugins.stochastics.StochasticsPluginId;
import util.annotations.UnitTestMethod;

public class AT_GroupsPlugin {

	@Test
	@UnitTestMethod(target = GroupsPlugin.Builder.class, name = "getGroupsPlugin", args = {})
	public void testGetGroupsPlugin() {

		GroupsPluginData groupsPluginData = GroupsPluginData.builder().build();
		Plugin groupPlugin = GroupsPlugin.builder().setGroupsPluginData(groupsPluginData).getGroupsPlugin();

		assertEquals(1, groupPlugin.getPluginDatas().size());
		assertTrue(groupPlugin.getPluginDatas().contains(groupsPluginData));

		assertEquals(GroupsPluginId.PLUGIN_ID, groupPlugin.getPluginId());

		Set<PluginId> expectedDependencies = new LinkedHashSet<>();
		expectedDependencies.add(PeoplePluginId.PLUGIN_ID);
		expectedDependencies.add(StochasticsPluginId.PLUGIN_ID);

		assertEquals(expectedDependencies, groupPlugin.getPluginDependencies());

	}

}
