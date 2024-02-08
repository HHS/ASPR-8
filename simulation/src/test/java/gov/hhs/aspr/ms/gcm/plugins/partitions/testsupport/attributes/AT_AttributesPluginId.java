package gov.hhs.aspr.ms.gcm.plugins.partitions.testsupport.attributes;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

import gov.hhs.aspr.ms.util.annotations.UnitTestField;

public class AT_AttributesPluginId {

	@Test
	@UnitTestField(target = AttributesPluginId.class, name = "PLUGIN_ID")
	public void testPluginId() {
		assertNotNull(AttributesPluginId.PLUGIN_ID);
	}
}
