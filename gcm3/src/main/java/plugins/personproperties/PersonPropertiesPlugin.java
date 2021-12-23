package plugins.personproperties;

import nucleus.PluginContext;
import nucleus.PluginId;
import nucleus.SimplePluginId;
import nucleus.SimpleResolverId;
import plugins.compartments.CompartmentPlugin;
import plugins.partitions.PartitionsPlugin;
import plugins.people.PeoplePlugin;
import plugins.personproperties.initialdata.PersonPropertyInitialData;
import plugins.personproperties.resolvers.PersonPropertyEventResolver;
import plugins.personproperties.support.PersonPropertyError;
import plugins.properties.PropertiesPlugin;
import plugins.regions.RegionPlugin;
import util.ContractError;
import util.ContractException;

/**
 *
 * <p>
 * <b>Summary</b> A nucleus plugin for managing person properties.
 * </p>
 *
 * <p>
 * <b>Events </b> See each event class for details.
 * <ul>
 * <li><b>PersonPropertyValueAssignmentEvent</b>: Sets a property value on a
 * person</li>
 *
 * <li><b>PersonPropertyChangeObservationEvent</b>: Notifies subscribed
 * oberservers of a person property change</li>
 *
 *
 * </ul>
 * </p>
 *
 * <p>
 * <b>Resolvers</b>
 * <ul>
 * <li><b>PersonPropertyEventResolver</b>: Uses initializing data to create and
 * publish data view the person properties data view. Handles all plugin-defined
 * events.
 * </ul>
 * </p>
 *
 * <p>
 * <b>Data Views</b> The compartment plugin supplies a single data view
 * <ul>
 * <li><b>Person Property Data View</b>: Supplies person property id values,
 * property definitions and values</li> *
 * </ul>
 * </p>
 *
 * <p>
 * <b>Reports</b> The plugin defines no reports
 * </p>
 *
 * <p>
 * <b>Agents: </b>This plugin does not provide any agent implementations.
 * </p>
 *
 * <p>
 * <b>Initializing data:</b> An immutable container of the initial state of
 * person properties.
 * </ul>
 *
 *
 * </p>
 *
 * <p>
 * <b>Support classes</b>
 * <ul>
 * <li><b>PersonPropertyError: </b></li>Enumeration implementing
 * {@linkplain ContractError} for this plugin.
 * <li><b>PropertyFilter: </b></li>Defines a filter used in partitions.
 * <li><b>PersonPropertyId: </b></li>Defines a marker interface for person
 * property identifiers.
 * <li><b>PersonPropertyLabeler: </b></li>Provides dimension labeling for
 * compartments in partitions.
 * <li><b>PersonPropertyInitialization: </b></li> Used to specify person
 * property initial values for newly added people
 * </ul>
 * </p>
 *
 * <p>
 * <b>Required Plugins</b>
 * <ul>
 * <li><b>CompartmentPlugin:</b> Used for labels and labelers
 * <li><b>RegionPlugin:</b> Used for labels and labelers
 * <li><b>PartitionsPlugin:</b> Used for defining filters and labelers.
 * <li><b>PeoplePlugin:</b> Used throughout the plugin since compartments contain people.
 * <li><b>PropertiesPlugin:</b> Used to support contract exceptions
 * 
 * 
 * </ul>
 * </p>
 *
 * @author Shawn Hatch
 *
 */
public final class PersonPropertiesPlugin {
	public final static PluginId PLUGIN_ID = new SimplePluginId(PersonPropertiesPlugin.class);

	private final PersonPropertyInitialData personPropertyInitialData;

	/**
	 * Constructs the plugin
	 * 
	 * @throws ContractException
	 *             <li>{@linkplain PersonPropertyError#NULL_PERSON_PROPERTY_INITIAL_DATA}
	 *             if the initial data is null</li>
	 */
	public PersonPropertiesPlugin(PersonPropertyInitialData personPropertyInitialData) {
		if (personPropertyInitialData == null) {
			throw new ContractException(PersonPropertyError.NULL_PERSON_PROPERTY_INITIAL_DATA);
		}
		this.personPropertyInitialData = personPropertyInitialData;
	}

	public void init(PluginContext pluginContext) {

		pluginContext.defineResolver(new SimpleResolverId(PersonPropertyEventResolver.class), new PersonPropertyEventResolver(personPropertyInitialData)::init);

		pluginContext.addPluginDependency(PartitionsPlugin.PLUGIN_ID);
		pluginContext.addPluginDependency(PeoplePlugin.PLUGIN_ID);
		pluginContext.addPluginDependency(PropertiesPlugin.PLUGIN_ID);
		pluginContext.addPluginDependency(CompartmentPlugin.PLUGIN_ID);
		pluginContext.addPluginDependency(RegionPlugin.PLUGIN_ID);
	}

}
