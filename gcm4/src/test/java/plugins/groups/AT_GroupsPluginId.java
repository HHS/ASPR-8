package plugins.groups;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

import tools.annotations.UnitTest;

@UnitTest(target = GroupsPluginId.class)
public class AT_GroupsPluginId {

	@Test
	public void test() {
		assertNotNull(GroupsPluginId.PLUGIN_ID);
	}
}
