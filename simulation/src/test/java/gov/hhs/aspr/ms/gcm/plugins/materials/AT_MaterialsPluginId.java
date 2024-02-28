package gov.hhs.aspr.ms.gcm.plugins.materials;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

import gov.hhs.aspr.ms.util.annotations.UnitTestField;

public class AT_MaterialsPluginId {

	@Test
	@UnitTestField(target = MaterialsPluginId.class, name = "PLUGIN_ID")
	public void testPluginId() {
		assertNotNull(MaterialsPluginId.PLUGIN_ID);
	}
}
