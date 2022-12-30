package plugins.partitions;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

import tools.annotations.UnitTest;
import tools.annotations.UnitTestField;

@UnitTest(target = PartitionsPluginId.class)
public class AT_PartitionsPluginId {

	@Test
	@UnitTestField(name = "PLUGIN_ID")
	public void testPluginId() {
		assertNotNull(PartitionsPluginId.PLUGIN_ID);
	}
}
