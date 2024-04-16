package gov.hhs.aspr.ms.gcm.simulation.plugins.properties;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

import gov.hhs.aspr.ms.util.annotations.UnitTestField;

public class AT_PropertiesPluginId {

	@Test
	@UnitTestField(target = PropertiesPluginId.class, name = "PLUGIN_ID")
	public void testPluginId() {
		assertNotNull(PropertiesPluginId.PLUGIN_ID);
	}
}
