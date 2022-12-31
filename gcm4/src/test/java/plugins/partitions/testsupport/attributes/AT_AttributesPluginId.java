package plugins.partitions.testsupport.attributes;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

import tools.annotations.UnitTestField;

public class AT_AttributesPluginId {

	@Test
	@UnitTestField(target = AttributesPluginId.class, name = "PLUGIN_ID")
	public void testPluginId() {
		assertNotNull(AttributesPluginId.PLUGIN_ID);
	}
}
