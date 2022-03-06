package plugins.properties;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import nucleus.Plugin;
import nucleus.PluginContext;
import util.annotations.UnitTest;
import util.annotations.UnitTestMethod;

@UnitTest(target = PropertiesPlugin.class)
public class AT_PropertiesPlugin {

	@Test
	@UnitTestMethod(name = "getPlugin", args = { PluginContext.class })
	public void testInit() {
		Plugin globalsPlugin = PropertiesPlugin.getPlugin();
		// show that the plugin only contains an id
		assertFalse(globalsPlugin.getInitializer().isPresent());
		assertTrue(globalsPlugin.getPluginDatas().isEmpty());
		assertTrue(globalsPlugin.getPluginDependencies().isEmpty());
		assertEquals(PropertiesPluginId.PLUGIN_ID, globalsPlugin.getPluginId());
	}

}
