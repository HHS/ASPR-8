package plugins.globalproperties;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

import tools.annotations.UnitTest;
import tools.annotations.UnitTestField;

@UnitTest(target = GlobalPropertiesPluginId.class)
public class AT_GlobalPropertiesPluginId {

	@Test
	@UnitTestField(name = "PLUGIN_ID")
	public void testPluginId() {
		assertNotNull(GlobalPropertiesPluginId.PLUGIN_ID);
	}
}
