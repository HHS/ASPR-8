package plugins.personproperties;

import nucleus.Plugin;
import plugins.partitions.PartitionsPluginId;
import plugins.people.PeoplePluginId;
import plugins.personproperties.datamanagers.PersonPropertiesDataManager;
import plugins.regions.RegionPluginId;

/**
 * A plugin providing a person property management to the simulation.
 * 
 * @author Shawn Hatch
 *
 */

public final class PersonPropertiesPlugin {

	private PersonPropertiesPlugin() {

	}

	public static Plugin getPersonPropertyPlugin(PersonPropertiesPluginData personPropertiesPluginData) {

		return Plugin	.builder()//
						.setPluginId(PersonPropertiesPluginId.PLUGIN_ID)//
						.addPluginData(personPropertiesPluginData)//
						.addPluginDependency(PartitionsPluginId.PLUGIN_ID)//
						.addPluginDependency(PeoplePluginId.PLUGIN_ID)//
						.addPluginDependency(RegionPluginId.PLUGIN_ID)//
						.setInitializer((c) -> {
							PersonPropertiesPluginData pluginData = c.getPluginData(PersonPropertiesPluginData.class);
							c.addDataManager(new PersonPropertiesDataManager(pluginData));
						}).build();

	}

}
