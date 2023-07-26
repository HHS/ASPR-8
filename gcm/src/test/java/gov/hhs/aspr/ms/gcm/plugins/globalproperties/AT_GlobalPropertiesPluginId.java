package gov.hhs.aspr.ms.gcm.plugins.globalproperties;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

import util.annotations.UnitTestField;


public class AT_GlobalPropertiesPluginId {

	@Test
	@UnitTestField(target = GlobalPropertiesPluginId.class,name = "PLUGIN_ID")
	public void testPluginId() {
		assertNotNull(GlobalPropertiesPluginId.PLUGIN_ID);
	}
}
