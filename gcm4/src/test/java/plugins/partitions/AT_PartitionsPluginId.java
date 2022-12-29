package plugins.partitions;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

import tools.annotations.UnitTest;

@UnitTest(target = PartitionsPluginId.class)
public class AT_PartitionsPluginId {

	@Test
	public void test() {
		assertNotNull(PartitionsPluginId.PLUGIN_ID);
	}
}
