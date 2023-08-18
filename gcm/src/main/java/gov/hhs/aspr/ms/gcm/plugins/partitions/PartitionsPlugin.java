package gov.hhs.aspr.ms.gcm.plugins.partitions;

import java.util.LinkedHashSet;
import java.util.Set;

import gov.hhs.aspr.ms.gcm.nucleus.NucleusError;
import gov.hhs.aspr.ms.gcm.nucleus.Plugin;
import gov.hhs.aspr.ms.gcm.nucleus.PluginId;
import gov.hhs.aspr.ms.gcm.plugins.partitions.datamanagers.PartitionsDataManager;
import gov.hhs.aspr.ms.gcm.plugins.partitions.datamanagers.PartitionsPluginData;
import gov.hhs.aspr.ms.gcm.plugins.partitions.support.PartitionError;
import gov.hhs.aspr.ms.gcm.plugins.people.PeoplePluginId;
import gov.hhs.aspr.ms.gcm.plugins.stochastics.StochasticsPluginId;
import util.errors.ContractException;

/**
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
	 * <p>
	 * Uses PartitionsPluginId.PLUGIN_ID as its id
	 * </p>
	 * <p>
	 * Depends on plugins:
	 * <ul>
	 * <li>Stochastics Plugin</li>
	 * <li>People Plugin</li>
	 * </ul>
	 * </p>
	 * <p>
	 * Provides data mangers: {@linkplain PartitionsDataManager}
	 * </p>
	 * <p>
	 * Provides no actors:
	 * </p>
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
		 *                           <ul>
		 *                           <li>{@linkplain PartitionError#NULL_PARTITION_PLUGIN_DATA}
		 *                           if the partitionsPluginData is null</li>
		 *                           <li>{@linkplain NucleusError#NULL_PLUGIN_ID} if an
		 *                           included plugin dependency id null</li>
		 *                           </ul>
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
