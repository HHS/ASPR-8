package plugins.regions;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import annotations.UnitTest;

@UnitTest(target = RegionPluginId.class)
public class AT_RegionPluginId {

	public void test() {
		assertNotNull(RegionPluginId.PLUGIN_ID);
	}
}
