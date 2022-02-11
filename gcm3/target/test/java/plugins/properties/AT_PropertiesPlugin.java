package plugins.properties;


import org.junit.jupiter.api.Test;

import nucleus.PluginContext;
import util.annotations.UnitTest;
import util.annotations.UnitTestConstructor;
import util.annotations.UnitTestMethod;

@UnitTest(target = PropertiesPlugin.class)
public class AT_PropertiesPlugin {

	@Test
	@UnitTestConstructor(args = {})
	public void testConstructor() {
		// nothing to test
	}

	@Test
	@UnitTestMethod(name = "init", args = { PluginContext.class })
	public void testInit() {
		// nothing to test
		
		
	}

}
