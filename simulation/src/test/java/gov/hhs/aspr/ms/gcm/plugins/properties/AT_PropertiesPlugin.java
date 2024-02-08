package gov.hhs.aspr.ms.gcm.plugins.properties;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import gov.hhs.aspr.ms.gcm.nucleus.Plugin;
import gov.hhs.aspr.ms.util.annotations.UnitTestMethod;

public class AT_PropertiesPlugin {

	@Test
	@UnitTestMethod(target = PropertiesPlugin.class, name = "getPropertiesPlugin", args = {})
	public void testGetPersonPropertyPlugin() {
		Plugin propertiesPlugin = PropertiesPlugin.getPropertiesPlugin();
		assertTrue(propertiesPlugin.getPluginDatas().isEmpty());
		assertEquals(PropertiesPluginId.PLUGIN_ID, propertiesPlugin.getPluginId());
		assertTrue(propertiesPlugin.getPluginDependencies().isEmpty());
	}

}
