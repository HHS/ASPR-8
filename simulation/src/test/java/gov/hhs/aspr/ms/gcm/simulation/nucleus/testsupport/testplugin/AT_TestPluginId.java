package gov.hhs.aspr.ms.gcm.simulation.nucleus.testsupport.testplugin;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

import gov.hhs.aspr.ms.util.annotations.UnitTestField;

public class AT_TestPluginId {

	@Test
	@UnitTestField(target = TestPluginId.class, name = "PLUGIN_ID")
	public void testPluginId() {
		assertNotNull(TestPluginId.PLUGIN_ID);
	}

}
