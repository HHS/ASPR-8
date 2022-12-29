package plugins.partitions.testsupport.attributes;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

import tools.annotations.UnitTest;

@UnitTest(target = AttributesPluginId.class)
public class AT_AttributesPluginId {

	@Test
	public void test() {
		assertNotNull(AttributesPluginId.PLUGIN_ID);
	}
}
