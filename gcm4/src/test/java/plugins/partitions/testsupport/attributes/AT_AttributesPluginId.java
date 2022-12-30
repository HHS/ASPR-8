package plugins.partitions.testsupport.attributes;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

import tools.annotations.UnitTest;
import tools.annotations.UnitTestField;

@UnitTest(target = AttributesPluginId.class)
public class AT_AttributesPluginId {

	@Test
	@UnitTestField(name = "PLUGIN_ID")
	public void testPluginId() {
		assertNotNull(AttributesPluginId.PLUGIN_ID);
	}
}
