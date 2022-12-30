package plugins.groups;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

import tools.annotations.UnitTest;
import tools.annotations.UnitTestField;

@UnitTest(target = GroupsPluginId.class)
public class AT_GroupsPluginId {

	@Test
	@UnitTestField(name = "PLUGIN_ID")
	public void testPluginId() {
		assertNotNull(GroupsPluginId.PLUGIN_ID);
	}
}
