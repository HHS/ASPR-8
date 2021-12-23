package plugins.compartments;

import nucleus.PluginContext;
import nucleus.PluginId;
import nucleus.SimplePluginId;
import nucleus.SimpleResolverId;
import plugins.compartments.initialdata.CompartmentInitialData;
import plugins.compartments.resolvers.CompartmentEventResolver;
import plugins.compartments.support.CompartmentId;
import plugins.components.ComponentPlugin;
import plugins.components.support.ComponentId;
import plugins.partitions.PartitionsPlugin;
import plugins.people.PeoplePlugin;
import plugins.properties.PropertiesPlugin;
import plugins.reports.ReportPlugin;
import util.ContractError;

/**
 *
 * <p>
 * <b>Summary</b> A nucleus plugin for the compartmental modeling paradigm. A
 * compartment generally represents the disease progress state of people such as
 * susceptible, exposed and infectious, but is not limited to this
 * interpretation. Compartments control all people who have the associated
 * discrete state and every person is assigned to a compartment.
 * </p>
 *
 * <p>
 * <b>Events </b> See each event class for details.
 * <ul>
 * <li><b>CompartmentPropertyValueAssignmentEvent</b>: Sets a property on a
 * compartment</li>
 *
 * <li><b>PersonCompartmentAssignmentEvent</b>: Assigns a person to a
 * compartment</li>
 *
 * <li><b>CompartmentPropertyChangeObservationEvent</b>: Notifies subscribed
 * oberservers of a compartment property change</li>
 *
 * <li><b>PersonCompartmentChangeObservationEvent</b>: Notifies subscribed
 * oberservers of a person compartment assignment</li>
 *
 * </ul>
 * </p>
 *
 * <p>
 * <b>Resolvers</b>
 * <ul>
 * <li><b>CompartmentEventResolver</b>: Uses initializing data to create and
 * publish data views. Handles all plugin-defined events.
 * </ul>
 * </p>
 *
 * <p>
 * <b>Data Views</b> The compartment plugin supplies two data views
 * <ul>
 * <li><b>Compartment Data View</b>: Supplies compartment id values and
 * compartment property information</li>
 * <li><b>Compartment Location Data View:</b> Supplies the compartment
 * assignment information for people</li>
 * </ul>
 * </p>
 *
 * <p>
 * <b>Reports</b>
 * <ul>
 * <li><b>Compartment Property Report: </b>A trace report of compartment
 * property values over time.
 * </ul>
 * </p>
 *
 * <p>
 * <b>Agents: </b>This plugin does not provide any agent implementations. Each
 * {@linkplain CompartmentId} defined by the {@linkplain CompartmentInitialData}
 * becomes an agent.
 *
 * </p>
 *
 * <p>
 * <b>Initializing data:</b> An immutable container of the initial state of
 * compartments.
 * </ul>
 *
 *
 * </p>
 *
 * <p>
 * <b>Support classes</b>
 * <ul>
 * <li><b>CompartmentError: </b></li>Enumeration implementing {@linkplain ContractError} for this plugin.
 * <li><b>CompartmentFilter: </b></li>Defines a filter used in partitions.
 * <li><b>CompartmentId: </b></li>Defines a type {@linkplain ComponentId} for
 * compartments.
 * <li><b>CompartmentLabeler: </b></li>Provides dimension labeling for
 * compartments in partitions.
 * <li><b>CompartmentPropertyId: </b></li>Defines a typed property id for
 * compartment properties.
 * </ul>
 * </p>
 *
 * <p>
 * <b>Required Plugins</b>
 * <ul>
 * <li><b>ComponentPlugin:</b> Used for the construction of compartments.
 * <li><b>PartitionsPlugin:</b> Used for defining filters and labelers.
 * <li><b>PeoplePlugin:</b> Used throughout the plugin since compartments
 * contain people.
 * <li><b>PropertiesPlugin:</b> Used to manage property values for compartments.
 * <li><b>ReportPlugin:</b> Used to support reports in this plugin.
 * </ul>
 * </p>
 *
 * @author Shawn Hatch
 *
 */
public final class CompartmentPlugin {
	/**
	 * Plugin id for this plugin
	 */
	public final static PluginId PLUGIN_ID = new SimplePluginId(CompartmentPlugin.class);

	private final CompartmentInitialData compartmentInitialData;

	/**
	 * Creates this plugin
	 */
	public CompartmentPlugin(final CompartmentInitialData compartmentInitialData) {
		this.compartmentInitialData = compartmentInitialData;
	}

	/**
	 * Initial behavior of this plugin. <BR>
	 *
	 * <UL>
	 * <li>defines the single resolver {@linkplain CompartmentEventResolver}</li>
	 * <li>establishes dependencies on other plugins</li>
	 * </UL>
	 */

	public void init(final PluginContext pluginContext) {

		pluginContext.defineResolver(new SimpleResolverId(CompartmentEventResolver.class), new CompartmentEventResolver(compartmentInitialData)::init);

		pluginContext.addPluginDependency(ComponentPlugin.PLUGIN_ID);
		pluginContext.addPluginDependency(PartitionsPlugin.PLUGIN_ID);
		pluginContext.addPluginDependency(PeoplePlugin.PLUGIN_ID);
		pluginContext.addPluginDependency(PropertiesPlugin.PLUGIN_ID);
		pluginContext.addPluginDependency(ReportPlugin.PLUGIN_ID);

	}

}
