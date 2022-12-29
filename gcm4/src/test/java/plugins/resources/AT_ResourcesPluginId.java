package plugins.resources;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

import tools.annotations.UnitTest;

@UnitTest(target = ResourcesPluginId.class)
public class AT_ResourcesPluginId {

	@Test
	public void test() {
		assertNotNull(ResourcesPluginId.PLUGIN_ID);
	}
}
