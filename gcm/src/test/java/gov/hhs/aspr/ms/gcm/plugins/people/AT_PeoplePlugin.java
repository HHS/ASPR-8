package gov.hhs.aspr.ms.gcm.plugins.people;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import gov.hhs.aspr.ms.gcm.nucleus.Plugin;
import gov.hhs.aspr.ms.gcm.plugins.people.datamanagers.PeoplePluginData;
import util.annotations.UnitTestMethod;

public class AT_PeoplePlugin {
	
	
	@Test
	@UnitTestMethod(target = PeoplePlugin.class,name = "getPeoplePlugin", args = { PeoplePluginData.class })
	public void testGetPlugin() {
		

	    PeoplePluginData peoplePluginData = PeoplePluginData.builder().build();
		Plugin peoplePlugin = PeoplePlugin.getPeoplePlugin(peoplePluginData);

		assertTrue(peoplePlugin.getPluginDatas().contains(peoplePluginData));
		assertEquals(PeoplePluginId.PLUGIN_ID, peoplePlugin.getPluginId());

		assertTrue(peoplePlugin.getPluginDependencies().isEmpty());

	}

}
