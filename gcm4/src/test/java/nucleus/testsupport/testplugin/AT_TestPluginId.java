package nucleus.testsupport.testplugin;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

import tools.annotations.UnitTestField;

public class AT_TestPluginId {

	@Test
	@UnitTestField(target = TestPluginId.class, name = "PLUGIN_ID")
	public void testPluginId() {
		assertNotNull(TestPluginId.PLUGIN_ID);
	}

}
