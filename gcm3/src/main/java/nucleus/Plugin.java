package nucleus;

import java.util.LinkedHashSet;
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

		public Builder addPluginDependency(PluginId pluginId) {
			data.pluginDependencies.add(pluginId);
			return this;
		}

		public Builder addPluginData(PluginData pluginData) {
			data.pluginDatas.add(pluginData);
			return this;
		}

		public Builder setSimInit(Consumer<PluginContext> init) {
			data.init = init;
			return this;
		}

	}

	private final Data data;

	private Plugin(Data data) {
		this.data = data;
	}

	public PluginId getPluginId() {
		return data.pluginId;
	}

	public Set<PluginId> getPluginDependencies() {
		return new LinkedHashSet<>(data.pluginDependencies);
	}

	public Set<PluginData> getPluginDatas() {
		return new LinkedHashSet<>(data.pluginDatas);
	}

	public void init(PluginContext pluginContext) {
		if (data.init != null) {
			data.init.accept(pluginContext);
		}
	}
}
