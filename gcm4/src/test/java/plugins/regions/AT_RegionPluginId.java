package plugins.regions;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

import tools.annotations.UnitTest;

@UnitTest(target = RegionsPluginId.class)
public class AT_RegionPluginId {

	@Test
	public void test() {
		assertNotNull(RegionsPluginId.PLUGIN_ID);
	}
}
