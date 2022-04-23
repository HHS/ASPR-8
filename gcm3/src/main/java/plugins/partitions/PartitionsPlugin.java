package plugins.partitions;

import nucleus.Plugin;
import plugins.partitions.datamanagers.PartitionsDataManager;
import plugins.people.PeoplePluginId;
import plugins.stochastics.StochasticsPluginId;

/**
 *
 * A nucleus plugin for the management of population partitions. A population
 * partition represents a filtered and partitioned subset of the people in the
 * simulation.
 */
public final class PartitionsPlugin {
	private PartitionsPlugin() {
	}

	/**
	 * Returns the partitions plugin.
	 *
	 * <P>
	 * Uses PartitionsPluginId.PLUGIN_ID as its id
	 * </P>
	 * 
	 * <P>
	 * Depends on plugins:
	 * <ul>
	 * <li>Stochastics Plugin</li>
	 * <li>People Plugin</li>
	 * </ul>
	 * </P>
	 * 
	 * <P>
	 * Provides data mangers:
	 * <ul>
	 * <li>{@linkplain PartitionsDataManager}</li>
	 * </ul>
	 * </P>
	 * 
	 * <P>
	 * Provides not actors:
	 * </P>
	 * 
	 */
	public static Plugin getPartitionsPlugin() {

		return Plugin	.builder()//
						.setPluginId(PartitionsPluginId.PLUGIN_ID).//
						setInitializer((c) -> {
							c.addDataManager(new PartitionsDataManager());
						})//
						.addPluginDependency(PeoplePluginId.PLUGIN_ID)//
						.addPluginDependency(StochasticsPluginId.PLUGIN_ID)//
						.build();
	}

}
