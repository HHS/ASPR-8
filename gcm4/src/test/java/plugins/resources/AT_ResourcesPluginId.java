package plugins.resources;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

import tools.annotations.UnitTestField;

public class AT_ResourcesPluginId {

	@Test
	@UnitTestField(target = ResourcesPluginId.class, name = "PLUGIN_ID")
	public void testPluginId() {
		assertNotNull(ResourcesPluginId.PLUGIN_ID);
	}
}
