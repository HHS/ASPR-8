package plugins.globalproperties;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

import tools.annotations.UnitTest;

@UnitTest(target = GlobalPropertiesPluginId.class)
public class AT_GlobalPropertiesPluginId {

	@Test
	public void test() {
		assertNotNull(GlobalPropertiesPluginId.PLUGIN_ID);
	}
}
