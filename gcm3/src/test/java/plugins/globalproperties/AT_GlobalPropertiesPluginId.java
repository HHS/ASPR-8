package plugins.globalproperties;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import tools.annotations.UnitTest;

@UnitTest(target = GlobalPropertiesPluginId.class)
public class AT_GlobalPropertiesPluginId {

	public void test() {
		assertNotNull(GlobalPropertiesPluginId.PLUGIN_ID);
	}
}
