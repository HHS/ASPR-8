package plugins.partitions.testsupport.attributes;

import nucleus.PluginContext;
import nucleus.PluginId;
import nucleus.SimplePluginId;
import nucleus.SimpleResolverId;
import plugins.partitions.PartitionsPlugin;
import plugins.partitions.testsupport.attributes.initialdata.AttributeInitialData;
import plugins.partitions.testsupport.attributes.resolvers.AttributesEventResolver;
import plugins.partitions.testsupport.attributes.support.AttributeError;
import plugins.people.PeoplePlugin;
import util.ContractError;
import util.ContractException;

/**
 *
 * <p>
 * <b>Summary</b> A nucleus test support plugin for testing the partitions
 * plugin. Introduces the concept of attributes that can be assigned to people.
 * </p>
 *
 * <p>
 * <b>Events </b> See each event class for details.
 * <ul>
 * <li><b>AttributeValueAssignmentEvent</b>: Sets an attribute value for a
 * person</li>
 *
 * <li><b>AttributeChangeObservationEvent</b>: Notifies subscribed oberservers
 * of a person attribute value change</li>
 *
 * </ul>
 * </p>
 *
 * <p>
 * <b>Resolvers</b>
 * <ul>
 * <li><b>AttributesEventResolver</b>: Uses initializing data to create and
 * publish data view. Handles all plugin-defined events.
 * </ul>
 * </p>
 *
 * <p>
 * <b>Data Views</b> The attributes plugin supplies one data view.
 * <ul>
 * <li><b>Attributes Data View</b>: Supplies attribute values, ids and definitions.</li>
 * </ul>
 * </p>
 *
 * <p>
 * <b>Reports</b> The plugin defines no reports.
 * </p>
 *
 * <p>
 * <b>Agents: </b>This plugin defines no agent implementations.
 * </p>
 *
 * <p>
 * <b>Initializing data:</b> An immutable container of the attribute
 * definitions.
 * </ul>
 *
 *
 * </p>
 *
 * <p>
 * <b>Support classes</b>
 * <ul>
 * <li><b>AttributeError: </b></li>Enumeration implementing
 * {@linkplain ContractError} for this plugin.
 * <li><b>AttributeDefinition: </b></li>Class for defining the type and default
 * value of an attribute
 * <li><b>AttributeFilter: </b></li>Defines a filter used in partitions.
 * <li><b>AttributeId: </b></li>Marker interface that defines attribute id
 * values.
 * <li><b>AttributeLabeler: </b></li>Provides dimension labeling for person
 * attributes in partitions.
 * </ul>
 * </p>
 *
 * <p>
 * <b>Required Plugins</b>
 * <ul>
 * <li><b>PartitionsPlugin:</b> Uses support classes in the partitions
 * plugin to define filtering and labeling behaviors.
 * <li><b>PeoplePlugin:</b> Used throughout the plugin since the plugin is
 * focused on person attributes
 * </ul>
 * </p>
 *
 * @author Shawn Hatch
 *
 */
public final class AttributesPlugin {
	public final static PluginId PLUGIN_ID = new SimplePluginId(AttributesPlugin.class);

	private final AttributeInitialData attributeInitialData;

	/**
	 * Constructs this plugin
	 * 
	 * @throws ContractException
	 * <li>{@linkplain AttributeError#NULL_ATTRIBUTE_INITIAL_DATA}  if the initial data is null</li>
	 * 
	 */
	public AttributesPlugin(AttributeInitialData attributeInitialData) {
		if(attributeInitialData == null) {
			throw new ContractException(AttributeError.NULL_ATTRIBUTE_INITIAL_DATA);
		}
		this.attributeInitialData = attributeInitialData;
	}

	public void init(PluginContext pluginContext) {

		pluginContext.defineResolver(new SimpleResolverId(AttributesEventResolver.class), new AttributesEventResolver(attributeInitialData)::init);

		pluginContext.addPluginDependency(PeoplePlugin.PLUGIN_ID);
		pluginContext.addPluginDependency(PartitionsPlugin.PLUGIN_ID);

	}

}
