package plugins.partitions;

import nucleus.PluginContext;
import nucleus.PluginId;
import nucleus.SimplePluginId;
import nucleus.SimpleResolverId;
import plugins.components.ComponentPlugin;
import plugins.partitions.resolvers.PartitionEventResolver;
import plugins.people.PeoplePlugin;
import plugins.stochastics.StochasticsPlugin;
import util.ContractError;

/**
 *
 * <p>
 * <b>Summary</b> A nucleus plugin for the managemnet of population partitions.
 * A population partition represents a filtered and partitioned subset of the
 * people in the simulation.
 * </p>
 *
 * <p>
 * <b>Events </b> See each event class for details.
 * <ul>
 * <li><b>PartitionAdditionEvent</b>: Adds a population partition with an associated unique key</li>
 *
 * <li><b>PartitionRemovalEvent</b>: Removes a population partition</li>
 *
 * </ul>
 * </p>
 *
 * <p>
 * <b>Resolvers</b>
 * <ul>
 * <li><b>PartitionEventResolver</b>: Initializes and
 * publishes data views. Handles all plugin-defined events.
 * </ul>
 * </p>
 *
 * <p>
 * <b>Data Views</b> The plugin supplies a single data view
 * <ul>
 * <li><b>Partition Data View</b>: Supplies partition information and sampling from partitioned populations</li>
 * 
 * </ul>
 * </p>
 *
 * <p>
 * <b>Reports</b> The plugin defined no reports.
 * 
 * </p>
 *
 * <p>
 * <b>Agents: </b>This plugin does not provide any agent implementations.
 * </p>
 *
 * <p>
 * <b>Initializing data:</b> The plugin defines no initial data structure.
 * </ul>
 *
 *
 * </p>
 *
 * <p>
 * <b>Support classes</b>
 * <ul>
 * <li><b>PartitionError: </b></li>Enumeration implementing {@linkplain ContractError} for this plugin.
 * <li><b>Filter: </b></li>Defines the base filter class used in partitions.
 * <li><b>DegeneratePopulationPartionImpl: </b></li>An implementor of Population partition that is optimized for population partitions having a single cell  
 * <li><b>Labeler: </b></li>Defines dimension labeling used to partition people into cells
 * <li><b>Equality: </b></li>An enumeration used for describing comparisons in filters
 * <li><b>EventPredicate: </b></li>An interface that defines efficient filter refresh operations
 * <li><b>FilterSensitivity: </b></li>An interface that defines a filter's sensitivity to various events
 * <li><b>LabelerSensitivity: </b></li>An interface that defines a labeler's sensitivity to various events
 * <li><b>LabelFunction: </b></li>An interface that defines the conversion of person attributes into label values
 * <li><b>LabelSet: </b></li>A set of label values used in sampling and querying population partitions
 * <li><b>LabelSetWeightingFunction: </b></li>An interface defining the conversion of a label set into a double valued weight. Used in sampling.
 * <li><b>Partition: </b></li> Defines the fixed features of a population partition.
 * <li><b>PartitionSampler: </b></li> Defines a query against a population partition for the purpose of selecting a person from that partition.
 * <li><b>PopulationPartitionImpl: </b></li> A general implementor of population partition
 * <li><b>Tuplator: </b></li> A utility for calculating tuples.  Used in managing the cells of a population partition
 * <li><b>PeopleContainer: </b></li> An interface that defines an efficient collection of the person ids in a single cell of population partition 
 * <li><b>BasePeopleContainer: </b></li> A PeopleContainer implementor that switches between two sub-implementors for efficiency as populations change   
 * <li><b>IntSetPeopleContainer: </b></li> A PeopleContainer implementor deals efficiently with small populations
 * <li><b>TreeBitSetPeopleContainer: </b></li> A PeopleContainer implementor deals efficiently with large populations
 * </ul>
 * </p>
 *
 * <p>
 * <b>Required Plugins</b>
 * <ul>
 * <li><b>ComponentPlugin:</b> Used in population partition construction validation 
 * <li><b>PeoplePlugin:</b> Used throughout the plugin.
 * <li><b>StochasticsPlugin:</b> Used in sampling
 * </ul>
 * </p>
 *
 * @author Shawn Hatch
 *
 */
public final class PartitionsPlugin {
	public final static PluginId PLUGIN_ID = new SimplePluginId(PartitionsPlugin.class);

	public void init(PluginContext pluginContext) {

		pluginContext.defineResolver(new SimpleResolverId(PartitionEventResolver.class), new PartitionEventResolver()::init);

		pluginContext.addPluginDependency(ComponentPlugin.PLUGIN_ID);
		pluginContext.addPluginDependency(PeoplePlugin.PLUGIN_ID);
		pluginContext.addPluginDependency(StochasticsPlugin.PLUGIN_ID);
	}

}
