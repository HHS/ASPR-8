package plugins.resources;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import annotations.UnitTest;

@UnitTest(target = ResourcesPluginId.class)
public class AT_ResourcesPluginId {

	public void test() {
		assertNotNull(ResourcesPluginId.PLUGIN_ID);
	}
}
