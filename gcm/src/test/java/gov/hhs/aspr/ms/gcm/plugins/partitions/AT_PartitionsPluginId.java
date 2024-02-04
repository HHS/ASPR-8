package gov.hhs.aspr.ms.gcm.plugins.partitions;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

import gov.hhs.aspr.ms.util.annotations.UnitTestField;

public class AT_PartitionsPluginId {

	@Test
	@UnitTestField(target = PartitionsPluginId.class, name = "PLUGIN_ID")
	public void testPluginId() {
		assertNotNull(PartitionsPluginId.PLUGIN_ID);
	}
}
