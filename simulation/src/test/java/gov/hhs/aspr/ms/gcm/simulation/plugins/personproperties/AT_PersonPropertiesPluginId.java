package gov.hhs.aspr.ms.gcm.simulation.plugins.personproperties;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

import gov.hhs.aspr.ms.util.annotations.UnitTestField;

public class AT_PersonPropertiesPluginId {

	@Test
	@UnitTestField(target = PersonPropertiesPluginId.class, name = "PLUGIN_ID")
	public void testPluginId() {
		assertNotNull(PersonPropertiesPluginId.PLUGIN_ID);
	}
}
