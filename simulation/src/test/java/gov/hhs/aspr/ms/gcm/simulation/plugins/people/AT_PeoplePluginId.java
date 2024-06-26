package gov.hhs.aspr.ms.gcm.simulation.plugins.people;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

import gov.hhs.aspr.ms.util.annotations.UnitTestField;

public class AT_PeoplePluginId {

	@Test
	@UnitTestField(target = PeoplePluginId.class, name = "PLUGIN_ID")
	public void testPluginId() {
		assertNotNull(PeoplePluginId.PLUGIN_ID);
	}
}
