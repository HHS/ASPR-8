package gov.hhs.aspr.ms.gcm.simulation.nucleus;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;

import gov.hhs.aspr.ms.util.errors.ContractException;
import net.jcip.annotations.ThreadSafe;

/**
 * A plugin is the main compositional element of an experiment. Plugins contain
 * the initial data state for simulations and add actors and data managers to
 * each simulation at the startup.
 */
@ThreadSafe
public final class Plugin {

	private static class Data {
		private PluginId pluginId;
		private Set<PluginId> pluginDependencies = new LinkedHashSet<>();
		private List<PluginData> pluginDatas = new ArrayList<>();
		private Consumer<PluginContext> initializer;
		private boolean locked;

		private Data() {
		}

		private Data(Data data) {
			pluginId = data.pluginId;
			pluginDependencies.addAll(data.pluginDependencies);
			pluginDatas.addAll(data.pluginDatas);
			initializer = data.initializer;
			locked = data.locked;
		}

		/**
    	 * Standard implementation consistent with the {@link #equals(Object)} method
    	 */
		@Override
		public int hashCode() {
			LinkedHashSet<PluginData> set = new LinkedHashSet<>(pluginDatas);
			return Objects.hash(pluginId, pluginDependencies, set);
		}

		/**
		 * Two {@link Data} instances are equal if and only if 
		 * their plugin IDs, plugin dependencies, and plugin datas 
		 * are equal. The order in which plugin datas are added 
		 * does not matter. INITIALIZERS ARE NOT COMPARED. 
		 * Initialization behavior can only be confirmed by 
		 * executing the plugin via a simulation instance.
		 */
		@Override
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			}
			if (obj == null) {
				return false;
			}
			if (getClass() != obj.getClass()) {
				return false;
			}
			Data other = (Data) obj;
			LinkedHashSet<PluginData> set = new LinkedHashSet<>(pluginDatas);
			LinkedHashSet<PluginData> otherSet = new LinkedHashSet<>(other.pluginDatas);

			return Objects.equals(pluginId, other.pluginId)
					&& Objects.equals(pluginDependencies, other.pluginDependencies)
					&& Objects.equals(set, otherSet);
		}
	}

	/**
	 * Returns an new instance of the Builder
	 */
	public static Builder builder() {
		return new Builder(new Data());
	}

	/**
	 * A builder class for Plugin
	 */
	public static class Builder {
		private Builder(Data data) {
			this.data = data;
		}

		private void validate() {
			if (data.pluginId == null) {
				throw new ContractException(NucleusError.NULL_PLUGIN_ID);
			}
		}

		private Data data;

		/**
		 * Returns the plugin formed by the inputs collected by this builder.
		 * 
		 * @throws ContractException {@linkplain NucleusError#NULL_PLUGIN_ID} if the
		 *                           plugin id was not set or set to null
		 */
		public Plugin build() {
			if (!data.locked) {
				validate();
			}
			ensureImmutability();
			return new Plugin(data);
		}

		public Builder setPluginId(PluginId pluginId) {
			ensureDataMutability();
			if (pluginId == null) {
				throw new ContractException(NucleusError.NULL_PLUGIN_ID);
			}
			data.pluginId = pluginId;
			return this;
		}

		/**
		 * Establishes that the plugin using this plugin context depends upon the given
		 * plugin. Plugin dependencies are gathered by nucleus and used to determine
		 * that the simulation is well formed. Nucleus requires that: 1) there are no
		 * duplicate plugins, 2)there are no null plugins, 3)there are no missing
		 * plugins, and 4) the plugin dependencies form an acyclic, directed graph.
		 * Nucleus will initialize each plugin primarily in the order dictated by this
		 * graph and secondarily in the order each plugin was contributed to a
		 * simulation or experiment.
		 * 
		 * @throws ContractException {@link NucleusError#NULL_PLUGIN_ID} if the plugin
		 *                           id is null
		 */
		public Builder addPluginDependency(PluginId pluginId) {
			ensureDataMutability();
			if (pluginId == null) {
				throw new ContractException(NucleusError.NULL_PLUGIN_ID);
			}
			data.pluginDependencies.add(pluginId);
			return this;
		}

		/**
		 * Adds a plugin data object. Plugin data object must be thread-safe. It is best
		 * practice for a plugin data to be properly immutable: 1) its state cannot be
		 * altered after construction, 2) all its member fields are declared final and
		 * 3) it does not pass any reference to itself during its construction.
		 * 
		 * @throws ContractException {@linkplain NucleusError#NULL_PLUGIN_DATA} if the
		 *                           plugin data is null
		 */
		public Builder addPluginData(PluginData pluginData) {
			ensureDataMutability();
			if (pluginData == null) {
				throw new ContractException(NucleusError.NULL_PLUGIN_DATA);
			}
			data.pluginDatas.add(pluginData);
			return this;
		}

		/**
		 * Sets the consumer of plugin context that interacts with the simulation by
		 * adding actors and data mangers to the simulation on the simulation's startup.
		 * The initializer must be thread-safe. It is best practice for the initializer
		 * to be stateless.
		 * 
		 * @throws ContractException {@linkplain NucleusError#NULL_PLUGIN_INITIALIZER}
		 *                           if the initializer is null
		 */
		public Builder setInitializer(Consumer<PluginContext> initializer) {
			ensureDataMutability();
			if (initializer == null) {
				throw new ContractException(NucleusError.NULL_PLUGIN_INITIALIZER);
			}
			data.initializer = initializer;
			return this;
		}

		private void ensureDataMutability() {
			if (data.locked) {
				data = new Data(data);
				data.locked = false;
			}
		}

		private void ensureImmutability() {
			if (!data.locked) {
				data.locked = true;
			}
		}
	}

	private final Data data;

	private Plugin(Data data) {
		this.data = data;
	}

	/**
	 * Returns the plugin id of this plugin.
	 */
	public final PluginId getPluginId() {
		return data.pluginId;
	}

	/**
	 * Returns the plugin id values of the other plugins that this plugin depends on
	 * to function correctly. These dependencies for a directed acyclic graph and
	 * determine the initialization order of plugins.
	 */
	public final Set<PluginId> getPluginDependencies() {
		return new LinkedHashSet<>(data.pluginDependencies);
	}

	/**
	 * Returns the set thread-safe plugin data objects collected by this plugin's
	 * builder.
	 */
	public final List<PluginData> getPluginDatas() {
		return new ArrayList<>(data.pluginDatas);
	}

	/**
	 * Returns a thread-safe consumer of plugin context. The initializer interacts
	 * with the simulation by adding actors and data mangers to the simulation on
	 * the simulation's startup.
	 */
	public final Optional<Consumer<PluginContext>> getInitializer() {
		return Optional.ofNullable(data.initializer);
	}

	/**
	 * Standard implementation consistent with the {@link #equals(Object)} method
	 */
	@Override
	public int hashCode() {
		return Objects.hash(data);
	}

	/**
	 * Two {@link Plugin} instances are equal if and only if
	 * their plugin IDs, plugin dependencies, and plugin datas
	 * are equal. The order in which plugin datas are added 
	 * does not matter. INITIALIZERS ARE NOT COMPARED. Initialization
	 * behavior can only be confirmed by executing the plugin via
	 * a simulation instance.
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		Plugin other = (Plugin) obj;
		return Objects.equals(data, other.data);
	}

	/**
	 * Returns a new builder instance that is pre-filled with the current state of
	 * this instance.
	 */
	public Builder toBuilder() {
		return new Builder(data);
	}
}
