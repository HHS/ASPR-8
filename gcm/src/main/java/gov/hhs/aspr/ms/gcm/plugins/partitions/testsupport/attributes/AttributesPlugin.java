package gov.hhs.aspr.ms.gcm.plugins.partitions.testsupport.attributes;

import gov.hhs.aspr.ms.gcm.nucleus.Plugin;
import gov.hhs.aspr.ms.gcm.plugins.partitions.testsupport.attributes.support.AttributeError;
import gov.hhs.aspr.ms.gcm.plugins.people.PeoplePluginId;
import gov.hhs.aspr.ms.util.errors.ContractError;
import gov.hhs.aspr.ms.util.errors.ContractException;

/**
 * <p>
 * <b>Summary</b> A nucleus test support plugin for testing the partitions
 * plugin. Introduces the concept of attributes that can be assigned to people.
 * </p>
 * <p>
 * <b>Events </b> See each event class for details.
 * <ul>
 * <li><b>AttributeValueAssignmentEvent</b>: Sets an attribute value for a
 * person</li>
 * <li><b>AttributeUpdateEvent</b>: Notifies subscribed oberservers of a person
 * attribute value change</li>
 * </ul>
 * <p>
 * <b>Resolvers</b>
 * <ul>
 * <li><b>AttributesEventResolver</b>: Uses initializing data to create and
 * publish data view. Handles all plugin-defined events.</li>
 * </ul>
 * <p>
 * <b>Data Views</b> The attributes plugin supplies one data view.
 * <ul>
 * <li><b>Attributes Data View</b>: Supplies attribute values, ids and
 * definitions.</li>
 * </ul>
 * <p>
 * <b>Reports</b> The plugin defines no reports.
 * </p>
 * <p>
 * <b>Agents: </b>This plugin defines no agent implementations.
 * </p>
 * <p>
 * <b>Initializing data:</b> An immutable container of the attribute
 * definitions.
 * </p>
 * <p>
 * <b>Support classes</b>
 * <ul>
 * <li><b>AttributeError:</b>Enumeration implementing {@linkplain ContractError}
 * for this plugin.</li>
 * <li><b>AttributeDefinition: </b>Class for defining the type and default value
 * of an attribute</li>
 * <li><b>AttributeFilter: </b>Defines a filter used in partitions.</li>
 * <li><b>AttributeId: </b>Marker interface that defines attribute id
 * values.</li>
 * <li><b>AttributeLabeler: </b>Provides dimension labeling for person
 * attributes in partitions.</li>
 * </ul>
 * <p>
 * <b>Required Plugins</b>
 * <ul>
 * <li><b>PartitionsPlugin:</b> Uses support classes in the partitions plugin to
 * define filtering and labeling behaviors.
 * <li><b>PeoplePlugin:</b> Used throughout the plugin since the plugin is
 * focused on person attributes</li>
 * </ul>
 */
public final class AttributesPlugin {

	private AttributesPlugin() {
	}

	/**
	 * Constructs this plugin
	 * 
	 * @throws ContractException {@linkplain AttributeError#NULL_ATTRIBUTE_INITIAL_DATA}
	 *                           if the initial data is null
	 */
	public static Plugin getAttributesPlugin(AttributesPluginData attributesPluginData) {
		if (attributesPluginData == null) {
			throw new ContractException(AttributeError.NULL_ATTRIBUTE_INITIAL_DATA);
		}

		return Plugin.builder()//
				.setPluginId(AttributesPluginId.PLUGIN_ID)//
				.addPluginDependency(PeoplePluginId.PLUGIN_ID)//
				.setInitializer((c) -> {
					AttributesPluginData pluginData = c.getPluginData(AttributesPluginData.class).get();
					c.addDataManager(new AttributesDataManager(pluginData));
				})//
				.addPluginData(attributesPluginData)//
				.build();

	}

}
