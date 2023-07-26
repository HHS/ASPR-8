package plugins.partitions;

import java.util.LinkedHashSet;
import java.util.Set;

import nucleus.NucleusError;
import nucleus.Plugin;
import nucleus.PluginId;
import plugins.partitions.datamanagers.PartitionsDataManager;
import plugins.partitions.datamanagers.PartitionsPluginData;
import plugins.partitions.support.PartitionError;
import plugins.people.PeoplePluginId;
import plugins.stochastics.StochasticsPluginId;
import util.errors.ContractException;

/**
 *
 * A nucleus plugin for the management of population partitions. A population
 * partition represents a filtered and partitioned subset of the people in the
 * simulation.
 */
public final class PartitionsPlugin {

	private static class Data {
		private Set<PluginId> pluginDependencies = new LinkedHashSet<>();
		private PartitionsPluginData partitionsPluginData;
	}

	private PartitionsPlugin() {
	}

	public static Builder builder() {
		return new Builder();
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
	 * Provides no actors:
	 * </P>
	 * 
	 */
	public static class Builder {
		private Builder() {
		}

		private Data data = new Data();

		private void validate() {
			if (data.partitionsPluginData == null) {
				throw new ContractException(PartitionError.NULL_PARTITION_PLUGIN_DATA);
			}
		}

		/**
		 * Builds the PartitionsPlugin from the collected inputs
		 * 
		 * @throws ContractException
		 *             <li>{@linkplain PartitionError#NULL_PARTITION_PLUGIN_DATA}
		 *             if the partitionsPluginData is null</li>
		 *             
		 *              <li>{@linkplain NucleusError#NULL_PLUGIN_ID}
		 *             if an included plugin dependency id null</li>
		 *            
		 */
		public Plugin getPartitionsPlugin() {

			validate();
			Plugin.Builder builder = Plugin.builder();//
			builder.setPluginId(PartitionsPluginId.PLUGIN_ID);//
			builder.addPluginData(data.partitionsPluginData);//
			builder.addPluginDependency(PeoplePluginId.PLUGIN_ID);//
			builder.addPluginDependency(StochasticsPluginId.PLUGIN_ID);//
			for (PluginId pluginId : data.pluginDependencies) {
				builder.addPluginDependency(pluginId);//
			}

			builder.setInitializer((c) -> {
				PartitionsPluginData partitionsPluginData = c.getPluginData(PartitionsPluginData.class).get();
				
				c.addDataManager(new PartitionsDataManager(partitionsPluginData));
			});//

			return builder.build();
		}

		public Builder setPartitionsPluginData(PartitionsPluginData partitionsPluginData) {
			data.partitionsPluginData = partitionsPluginData;
			return this;
		}

		public Builder addPluginDependency(PluginId pluginId) {
			data.pluginDependencies.add(pluginId);
			return this;
		}

	}

}