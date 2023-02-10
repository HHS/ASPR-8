package plugins.personproperties;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.LinkedHashSet;
import java.util.Set;

import org.junit.jupiter.api.Test;

import nucleus.Plugin;
import nucleus.PluginId;
import plugins.people.PeoplePluginId;
import plugins.regions.RegionsPluginId;
import util.annotations.UnitTestMethod;

public class AT_PersonPropertiesPlugin {

	@Test
	@UnitTestMethod(target = PersonPropertiesPlugin.class, name = "getPersonPropertyPlugin", args = { PersonPropertiesPluginData.class })
	public void testGetPersonPropertyPlugin() {
		PersonPropertiesPluginData personPropertiesPluginData = PersonPropertiesPluginData.builder().build();
		Plugin personPropertiesPlugin = PersonPropertiesPlugin.getPersonPropertyPlugin(personPropertiesPluginData);

		assertEquals(1, personPropertiesPlugin.getPluginDatas().size());
		assertTrue(personPropertiesPlugin.getPluginDatas().contains(personPropertiesPluginData));

		assertEquals(PersonPropertiesPluginId.PLUGIN_ID, personPropertiesPlugin.getPluginId());

		Set<PluginId> expectedDependencies = new LinkedHashSet<>();
		expectedDependencies.add(PeoplePluginId.PLUGIN_ID);
		expectedDependencies.add(RegionsPluginId.PLUGIN_ID);

		assertEquals(expectedDependencies, personPropertiesPlugin.getPluginDependencies());

	}

}
