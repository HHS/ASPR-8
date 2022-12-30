package nucleus.testsupport.testplugin;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

import tools.annotations.UnitTest;
import tools.annotations.UnitTestField;

@UnitTest(target = TestPluginId.class)
public class AT_TestPluginId {
	
	@Test
	@UnitTestField(name = "PLUGIN_ID")
	public void testPluginId() {
		assertNotNull(TestPluginId.PLUGIN_ID);
	}

}
