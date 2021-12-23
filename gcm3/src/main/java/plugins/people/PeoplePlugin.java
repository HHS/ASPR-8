package plugins.people;

import nucleus.PluginContext;
import nucleus.PluginId;
import nucleus.SimplePluginId;
import nucleus.SimpleResolverId;
import plugins.people.initialdata.PeopleInitialData;
import plugins.people.resolvers.PersonEventResolver;
import util.ContractError;

/**
 *
 * <p>
 * <b>Summary</b> A nucleus plugin for representing people. A person may be
 * created at any time.
 * </p>
 *
 * <p>
 * <b>Events </b> See each event class for details.
 * <ul>
 * <li><b>BulkPersonCreationEvent</b>: Creates multiple people</li>
 *
 * <li><b>PersonCreationEvent</b>: Creates a single person</li>
 *
 * <li><b>PersonRemovalRequestEvent</b>: Removes a single person</li>
 *
 * <li><b>PopulationGrowthProjectionEvent</b>: Helps the plugin prepare for
 * loading large numbers of people</li>
 * 
 * <li><b>BulkPersonCreationObservationEvent</b>: Allows agents, resolver and
 * reports observe that a bulk person creation has occured</li>
 * 
 * <li><b>PersonCreationObservationEvent</b>: Allows agents, resolver and
 * reports observe that a person creation has occured</li>
 * 
 * <li><b>PersonImminentRemovalObservationEvent</b>: Allows agents, resolver and
 * reports observe that a person will be removed</li>
 *
 * </ul>
 * </p>
 *
 * <p>
 * <b>Resolvers</b>
 * <ul>
 * <li><b>PersonEventResolver</b>: Uses initializing data to create and publish
 * data views. Handles all plugin-defined events.
 * </ul>
 * </p>
 *
 * <p>
 * <b>Data Views</b> The people plugin supplies a single data view
 * <ul>
 * <li><b>Compartment Data View</b>: Supplies person id values, person existence
 * and other person related information</li>
 * </ul>
 * </p>
 *
 * <p>
 * <b>Reports</b> This plugin defines no reports.
 * </p>
 *
 * <p>
 * <b>Agents: </b>This plugin defines no agents.
 * </p>
 *
 * <p>
 * <b>Initializing data:</b> An immutable container of the initial state of
 * people.
 * </ul>
 *
 *
 * </p>
 *
 * <p>
 * <b>Support classes</b>
 * <ul>
 * <li><b>PersonError: </b></li>Enumeration implementing
 * {@linkplain ContractError} for this plugin.
 * <li><b>PersonId: </b></li>Defines a unique identifier for people.
 * <li><b>PersonConstructionData: </b></li>Provides data containment for person
 * construction
 * <li><b>BulkPersonConstructionData: </b></li>Provides data containment for
 * bulk person construction
 * </ul>
 * </p>
 *
 * <p>
 * <b>Required Plugins</b> This plugin has no dependencies on other plugins *
 * </p>
 *
 * @author Shawn Hatch
 *
 */

public final class PeoplePlugin {
	/**
	 * Plugin id for this plugin
	 */
	public final static PluginId PLUGIN_ID = new SimplePluginId(PeoplePlugin.class);

	private final PeopleInitialData peopleInitialData;

	/**
	 * Creates this plugin
	 */
	public PeoplePlugin(PeopleInitialData peopleInitialData) {
		this.peopleInitialData = peopleInitialData;
	}

	/**
	 * Initial behavior of this plugin. <BR>
	 *
	 * <UL>
	 * <li>defines the single resolver {@linkplain PersonEventResolver}</li>
	 * </UL>
	 */
	public void init(PluginContext pluginContext) {
		pluginContext.defineResolver(new SimpleResolverId(PersonEventResolver.class), new PersonEventResolver(peopleInitialData)::init);
	}

}
