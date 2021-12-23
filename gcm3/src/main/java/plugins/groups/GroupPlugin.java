package plugins.groups;

import nucleus.PluginContext;
import nucleus.PluginId;
import nucleus.SimplePluginId;
import nucleus.SimpleResolverId;
import plugins.groups.events.mutation.GroupConstructionEvent;
import plugins.groups.initialdata.GroupInitialData;
import plugins.groups.resolvers.GroupEventResolver;
import plugins.groups.support.GroupLabeler;
import plugins.partitions.PartitionsPlugin;
import plugins.people.PeoplePlugin;
import plugins.people.events.mutation.BulkPersonCreationEvent;
import plugins.properties.PropertiesPlugin;
import plugins.reports.ReportPlugin;
import plugins.stochastics.StochasticsPlugin;
import util.ContractError;

/**
 *
 * <p>
 * <b>Summary</b> A nucleus plugin for managing groups of people. Groups
 * represent colocated people in close contact such as family, coworkers and
 * classmates. Groups have a group type that remains constant throughout the
 * group's existence. Groups have property values dictated by the type of the
 * group.
 * </p>
 *
 * <p>
 * <b>Events </b> See each event class for details.
 * <ul>
 * <li><b>GroupCreationEvent</b>: Creates a group from a group type</li>
 * <li><b>GroupConstructionEvent</b>: Creates a group from a group type and
 * group property values</li>
 * <li><b>GroupMembershipAdditionEvent</b>: Adds a person to a group</li>
 * <li><b>GroupMembershipRemovalEvent</b>: Removes a person from a group</li>
 * <li><b>GroupPropertyValueAssignmentEvent</b>: Assigns a group property to a
 * group</li>
 * <li><b>GroupRemovalRequestEvent</b>: Removes a group from the simulation</li>
 * <li><b>GroupCreationObservationEvent</b>: Notifies subscribed oberservers of
 * a group creation</li>
 * <li><b>GroupImminentRemovalObservationEvent</b>: Notifies subscribed
 * oberservers of an imminent removal of a group</li>
 * <li><b>GroupMembershipAdditionObservationEvent</b>: Notifies subscribed
 * oberservers of the addition of a person to a group</li>
 * <li><b>GroupMembershipRemovalObservationEvent</b>: Notifies subscribed
 * oberservers of the removal of a person form a group</li>
 * <li><b>GroupPropertyChangeObservationEvent</b>: Notifies subscribed
 * oberservers of group property change</li>
 * </ul>
 * </p>
 *
 * <p>
 * <b>Resolvers</b>
 * <ul>
 * <li><b>GroupEventResolver</b>: Uses initializing data to create and publish
 * data views. Handles all plugin-defined events.
 * </ul>
 * </p>
 *
 * <p>
 * <b>Data Views</b> The compartment plugin supplies one data view
 * <ul>
 * <li><b>Person Group Data View</b>: Supplies group membership and property
 * information</li>
 * </ul>
 * </p>
 *
 * <p>
 * <b>Reports</b>
 * <ul>
 * <li><b>Group Population Report: </b>A periodic Report that displays the
 * number of groups having a particular number of people for a given group type.
 * 
 * <li><b>Group Property Report: </b>A periodic Report that displays the number
 * of groups having particular values for each group property for a given group
 * type.
 * 
 * </ul>
 * </p>
 *
 * <p>
 * <b>Agents: </b>This plugin does not provide any agent implementations.
 * </p>
 *
 * <p>
 * <b>Initializing data:</b>
 * 
 * <li><b>Group Initialization Data: </b>An immutable container of the initial
 * state of groups, memberships, types, property definitions and property
 * values.
 * </ul>
 *
 *
 * </p>
 *
 * <p>
 * <b>Support classes</b>
 * <ul>
 * <li><b>BulkGroupMembershipData</b></li>Data structure supporting the
 * collection of group related data for a {@link BulkPersonCreationEvent}
 * <li><b>GroupConstructionInfo</b></li>Data structure supporting the collection
 * of group related data for a {@link GroupConstructionEvent}
 * <li><b>GroupError</b></li>Enumeration implementing {@linkplain ContractError}
 * for this plugin.
 * <li><b>GroupId</b></li>Defines a type for group identifiers.
 * <li><b>GroupLabeler</b></li>Provides dimension labeling for groups in
 * partitions.
 * <li>
 * <li><b>GroupMemberFilter</b></li>Defines a filter used in partitions.
 * <li><b>GroupPropertyId</b></li>Defines a type for group property identifiers.
 * <li><b>GroupSampler</b></li>Defines a query against a group for the purpose
 * of randomly selecting a member of that group.
 * <li><b>GroupsForPersonAndGroupTypeFilter</b></li>Defines a filter used in
 * partitions.
 * <li><b>GroupsForPersonFilter</b></li>Defines a filter used in partitions.
 * <li><b>GroupTypeCountMap</b></li>A map of group type to the number of groups
 * of that type for a given person. Used to support the {@link GroupLabeler}
 * <li><b>GroupTypeId</b></li>Defines a type for group type identifiers.
 * <li><b>GroupTypesForPersonFilter</b></li>Defines a filter used in partitions.
 * <li><b>GroupWeightingFunction</b></li>A relative weighting function for a
 * person in a group. Used by the {@link GroupLabeler}
 * </ul>
 * </p>
 *
 * <p>
 * <b>Required Plugins</b>
 * <ul>
 * <li><b>PartitionsPlugin:</b> Used for defining filters and labelers.
 * <li><b>PeoplePlugin:</b> Used throughout the plugin since groups contain
 * people.
 * <li><b>PropertiesPlugin:</b> Used to manage property values for groups.
 * <li><b>ReportPlugin:</b> Used to support reports in this plugin.
 * <li><b>StochasticsPlugin:</b> Used to support group sampling.
 * </ul>
 * </p>
 *
 * @author Shawn Hatch
 *
 */

public final class GroupPlugin {
	public final static PluginId PLUGIN_ID = new SimplePluginId(GroupPlugin.class);

	private final GroupInitialData groupInitialData;

	public GroupPlugin(GroupInitialData groupInitialData) {
		this.groupInitialData = groupInitialData;
	}

	public void init(PluginContext pluginContext) {
		pluginContext.defineResolver(new SimpleResolverId(GroupEventResolver.class), new GroupEventResolver(groupInitialData)::init);

		pluginContext.addPluginDependency(PartitionsPlugin.PLUGIN_ID);
		pluginContext.addPluginDependency(PeoplePlugin.PLUGIN_ID);
		pluginContext.addPluginDependency(PropertiesPlugin.PLUGIN_ID);
		pluginContext.addPluginDependency(ReportPlugin.PLUGIN_ID);
		pluginContext.addPluginDependency(StochasticsPlugin.PLUGIN_ID);
	}

}
