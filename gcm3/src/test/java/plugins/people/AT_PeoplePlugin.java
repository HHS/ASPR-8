package plugins.people;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import annotations.UnitTest;
import annotations.UnitTestMethod;
import nucleus.Plugin;
import plugins.globals.GlobalPluginData;

@UnitTest(target = PeoplePlugin.class)
public class AT_PeoplePlugin {
	
	
	@Test
	@UnitTestMethod(name = "getPlugin", args = { GlobalPluginData.class })
	public void testGetPlugin() {
		

	    PeoplePluginData peoplePluginData = PeoplePluginData.builder().build();
		Plugin peoplePlugin = PeoplePlugin.getPeoplePlugin(peoplePluginData);

		assertTrue(peoplePlugin.getPluginDatas().contains(peoplePluginData));
		assertEquals(PeoplePluginId.PLUGIN_ID, peoplePlugin.getPluginId());

		assertTrue(peoplePlugin.getPluginDependencies().isEmpty());

	}

}
