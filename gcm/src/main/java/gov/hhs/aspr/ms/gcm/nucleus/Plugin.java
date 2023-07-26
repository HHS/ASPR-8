package gov.hhs.aspr.ms.gcm.nucleus;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;

import net.jcip.annotations.ThreadSafe;
import util.errors.ContractException;

/**
 * A plugin is the main compositional element of an experiment. Plugins contain
 * the initial data state for simulations and add actors and data managers to
 * each simulation at the startup.
 * 
 * 
 *
 */
@ThreadSafe
public final class Plugin {

	private static class Data {
		private PluginId pluginId;
		private Set<PluginId> pluginDependencies = new LinkedHashSet<>();
		private List<PluginData> pluginDatas = new ArrayList<>();
		private Consumer<PluginContext> initializer;

		public Data() {
		}

		public Data(Data data) {
			pluginId = data.pluginId;
			pluginDependencies.addAll(data.pluginDependencies);
			pluginDatas.addAll(data.pluginDatas);
			initializer = data.initializer;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;			
			LinkedHashSet<PluginData> set = new LinkedHashSet<>(pluginDatas);
			result = prime * result +  set.hashCode();
			result = prime * result + pluginDependencies.hashCode();
			result = prime * result + pluginId.hashCode();
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			}
			if (!(obj instanceof Data)) {
				return false;
			}
			Data other = (Data) obj;
			LinkedHashSet<PluginData> set = new LinkedHashSet<>(pluginDatas);
			LinkedHashSet<PluginData> otherSet = new LinkedHashSet<>(other.pluginDatas);
			if (!set.equals(otherSet)) {
				return false;
			}
			if (!pluginDependencies.equals(other.pluginDependencies)) {
				return false;
			}
			if (!pluginId.equals(other.pluginId)) {
				return false;
			}
			return true;
		}

	}

	/**
	 * Returns an new instance of the Builder
	 */
	public static Builder builder() {
		return new Builder();
	}

	/**
	 * A builder class for Plugin
	 * 
	 *
	 */
	public static class Builder {
		private Builder() {
		}

		private void validate() {
			if (data.pluginId == null) {
				throw new ContractException(NucleusError.NULL_PLUGIN_ID);
			}
		}

		private Data data = new Data();

		/**
		 * Returns the plugin formed by the inputs collected by this builder.
		 * 
		 * @throws ContractException
		 * 
		 *             <li>{@linkplain NucleusError#NULL_PLUGIN_ID} if the
		 *             plugin id was not set or set to null</li>
		 */
		public Plugin build() {
			validate();
			return new Plugin(new Data(data));
		}

		public Builder setPluginId(PluginId pluginId) {
			if (pluginId == null) {
				throw new ContractException(NucleusError.NULL_PLUGIN_ID);
			}
			data.pluginId = pluginId;
			return this;
		}

		/**
		 * Establishes that the plugin using this plugin context depends upon
		 * the given plugin.
		 * 
		 * Plugin dependencies are gathered by nucleus and used to determine
		 * that the simulation is well formed. Nucleus requires that: 1) there
		 * are no duplicate plugins, 2)there are no null plugins, 3)there are no
		 * missing plugins, and 4) the plugin dependencies form an acyclic,
		 * directed graph.
		 * 
		 * Nucleus will initialize each plugin primarily in the order dictated
		 * by this graph and secondarily in the order each plugin was
		 * contributed to a simulation or experiment.
		 * 
		 * @throws ContractException
		 * 
		 *             <li>{@link NucleusError#NULL_PLUGIN_ID} if the plugin id
		 *             is null
		 */
		public Builder addPluginDependency(PluginId pluginId) {
			if (pluginId == null) {
				throw new ContractException(NucleusError.NULL_PLUGIN_ID);
			}
			data.pluginDependencies.add(pluginId);
			return this;
		}

		/**
		 * Adds a plugin data object. Plugin data object must be thread-safe. It
		 * is best practice for a plugin data to be properly immutable: 1) its
		 * state cannot be altered after construction, 2) all its member fields
		 * are declared final and 3) it does not pass any reference to itself
		 * during its construction.
		 * 
		 * @throws ContractException
		 *             <li>{@linkplain NucleusError#NULL_PLUGIN_DATA} if the
		 *             plugin data is null</li>
		 * 
		 */
		public Builder addPluginData(PluginData pluginData) {
			if (pluginData == null) {
				throw new ContractException(NucleusError.NULL_PLUGIN_DATA);
			}
			data.pluginDatas.add(pluginData);
			return this;
		}

		/**
		 * Sets the consumer of plugin context that interacts with the
		 * simulation by adding actors and data mangers to the simulation on the
		 * simulation's startup. The initializer must be thread-safe. It is best
		 * practice for the initializer to be stateless.
		 * 
		 * @throws ContractException
		 *             <li>{@linkplain NucleusError#NULL_PLUGIN_INITIALIZER} if
		 *             the initializer is null</li>
		 * 
		 */
		public Builder setInitializer(Consumer<PluginContext> initializer) {
			if (initializer == null) {
				throw new ContractException(NucleusError.NULL_PLUGIN_INITIALIZER);
			}
			data.initializer = initializer;
			return this;
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
	 * Returns the plugin id values of the other plugins that this plugin
	 * depends on to function correctly. These dependencies for a directed
	 * acyclic graph and determine the initialization order of plugins.
	 */
	public final Set<PluginId> getPluginDependencies() {
		return new LinkedHashSet<>(data.pluginDependencies);
	}

	/**
	 * Returns the set thread-safe plugin data objects collected by this
	 * plugin's builder.
	 */
	public final List<PluginData> getPluginDatas() {
		return new ArrayList<>(data.pluginDatas);
	}

	/**
	 * Returns a thread-safe consumer of plugin context. The initializer
	 * interacts with the simulation by adding actors and data mangers to the
	 * simulation on the simulation's startup.
	 */
	public final Optional<Consumer<PluginContext>> getInitializer() {
		return Optional.ofNullable(data.initializer);
	}

	/**
	 * Implementation consistent with equals()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((data == null) ? 0 : data.hashCode());
		return result;
	}

	/**
	 * Two Plugins are equal if and only if their plugin ids, plugin
	 * dependencies and plugin datas are equal. INITIALIZERS ARE NOT COMPARED.
	 * 
	 * Initialization behavior can only be confirmed by executing the plugin via
	 * a simulation instance.
	 * 
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof Plugin)) {
			return false;
		}
		Plugin other = (Plugin) obj;
		if (data == null) {
			if (other.data != null) {
				return false;
			}
		} else if (!data.equals(other.data)) {
			return false;
		}
		return true;
	}
}
