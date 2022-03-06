package nucleus;

import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;

import net.jcip.annotations.ThreadSafe;
import util.ContractException;

@ThreadSafe
public final class Plugin {
	
	private static class Data {
		private PluginId pluginId;
		private Set<PluginId> pluginDependencies = new LinkedHashSet<>();
		private Set<PluginData> pluginDatas = new LinkedHashSet<>();
		private Consumer<PluginContext> init;
	}

	public static Builder builder() {
		return new Builder();
	}
	
	public static class Builder {
		private Builder() {}
		
		private void validate() {
			if (data.pluginId == null) {
				throw new ContractException(NucleusError.NULL_PLUGIN_ID);
			}
		}

		private Data data = new Data();

		public Plugin build() {
			try {
				validate();
				return new Plugin(data);
			} finally {
				data = new Data();
			}
		}

		public Builder setPluginId(PluginId pluginId) {
			data.pluginId = pluginId;
			return this;
		}
		/**
		 * Establishes that the plugin using this plugin context depends upon the
		 * given plugin.
		 * 
		 * Plugin dependencies are gathered by nucleus and used to determine that
		 * the simulation is well formed. Nucleus requires that: 1) there are no
		 * duplicate plugins, 2)there are no null plugins, 3)there are no missing
		 * plugins, and 4) the plugin dependencies form an acyclic, directed graph.
		 * 
		 * Nucleus will initialize each plugin primarily in the order dictated by
		 * this graph and secondarily in the order each plugin was contributed to
		 * nucleus.
		 * 
		 * @throws ContractException
		 *             <li>{@link NucleusError#PLUGIN_INITIALIZATION_CLOSED} if
		 *             plugin initialization is over
		 *             <li>{@link NucleusError#NULL_PLUGIN_ID} if the plugin id is
		 *             null
		 */
		public Builder addPluginDependency(PluginId pluginId) {
			data.pluginDependencies.add(pluginId);
			return this;
		}

		public Builder addPluginData(PluginData pluginData) {
			data.pluginDatas.add(pluginData);
			return this;
		}

		public Builder setInitializer(Consumer<PluginContext> init) {
			data.init = init;
			return this;
		}

	}

	private final Data data;

	private Plugin(Data data) {
		this.data = data;
	}

	public final PluginId getPluginId() {
		return data.pluginId;
	}

	public final Set<PluginId> getPluginDependencies() {
		return new LinkedHashSet<>(data.pluginDependencies);
	}

	public final Set<PluginData> getPluginDatas() {
		return new LinkedHashSet<>(data.pluginDatas);
	}

	public final Optional<Consumer<PluginContext>> getInitializer() {
		return Optional.ofNullable(data.init);
	}
}
