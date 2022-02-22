package nucleus.testsupport.testplugin;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

import util.annotations.UnitTest;

@UnitTest(target = TestPluginId.class)
public class AT_TestPluginId {
	
	@Test
	public void test() {
		assertNotNull(TestPluginId.PLUGIN_ID);
	}

}
