package plugins.regions;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import tools.annotations.UnitTest;

@UnitTest(target = RegionsPluginId.class)
public class AT_RegionPluginId {

	public void test() {
		assertNotNull(RegionsPluginId.PLUGIN_ID);
	}
}
