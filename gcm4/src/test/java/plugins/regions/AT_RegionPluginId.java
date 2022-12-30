package plugins.regions;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

import tools.annotations.UnitTest;
import tools.annotations.UnitTestField;

@UnitTest(target = RegionsPluginId.class)
public class AT_RegionPluginId {

	@Test
	@UnitTestField(name = "PLUGIN_ID")
	public void testPluginId() {
		assertNotNull(RegionsPluginId.PLUGIN_ID);
	}
}
