package gov.hhs.aspr.ms.gcm.plugins.regions;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

import util.annotations.UnitTestField;

public class AT_RegionPluginId {

	@Test
	@UnitTestField(target = RegionsPluginId.class, name = "PLUGIN_ID")
	public void testPluginId() {
		assertNotNull(RegionsPluginId.PLUGIN_ID);
	}
}
