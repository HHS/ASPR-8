package nucleus;

import java.util.LinkedHashSet;
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
		private Set<PluginData> pluginDatas = new LinkedHashSet<>();
		private Consumer<PluginContext> initializer;		
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
			try {
				validate();
				return new Plugin(data);
			} finally {
				data = new Data();
			}
		}

		public Builder setPluginId(PluginId pluginId) {
			if(pluginId == null) {
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
			if(pluginId == null) {
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
		 * <li>{@linkplain NucleusError#NULL_PLUGIN_INITIALIZER} if the initializer is null</li>
		 * 
		 */
		public Builder setInitializer(Consumer<PluginContext> initializer) {
			if(initializer == null) {
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
	public final Set<PluginData> getPluginDatas() {
		return new LinkedHashSet<>(data.pluginDatas);
	}

	/**
	 * Returns a thread-safe consumer of plugin context. The initializer
	 * interacts with the simulation by adding actors and data mangers to the
	 * simulation on the simulation's startup.
	 */
	public final Optional<Consumer<PluginContext>> getInitializer() {
		return Optional.ofNullable(data.initializer);
	}
}
