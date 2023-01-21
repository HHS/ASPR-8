package plugins.personproperties;

import nucleus.Plugin;
import plugins.people.PeoplePluginId;
import plugins.personproperties.datamanagers.PersonPropertiesDataManager;
import plugins.personproperties.dataviews.PersonPropertiesDataView;
import plugins.regions.RegionsPluginId;

/**
 * A plugin providing a person property management to the simulation.
 * 
 *
 */

public final class PersonPropertiesPlugin {

	private PersonPropertiesPlugin() {

	}

	public static Plugin getPersonPropertyPlugin(PersonPropertiesPluginData personPropertiesPluginData) {

		return Plugin	.builder()//
						.setPluginId(PersonPropertiesPluginId.PLUGIN_ID)//
						.addPluginData(personPropertiesPluginData)//
						.addPluginDependency(PeoplePluginId.PLUGIN_ID)//
						.addPluginDependency(RegionsPluginId.PLUGIN_ID)//
						.setInitializer((c) -> {
							PersonPropertiesPluginData pluginData = c.getPluginData(PersonPropertiesPluginData.class);
							PersonPropertiesDataManager personPropertiesDataManager = new PersonPropertiesDataManager(pluginData);
							c.addDataManager(personPropertiesDataManager);
							PersonPropertiesDataView personPropertiesDataView = new PersonPropertiesDataView(personPropertiesDataManager);
							c.addDataView(personPropertiesDataView);
						}).build();

	}

}
