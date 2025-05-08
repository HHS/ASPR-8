package gov.hhs.aspr.ms.gcm.simulation.nucleus.testsupport;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import gov.hhs.aspr.ms.gcm.simulation.nucleus.NucleusError;
import gov.hhs.aspr.ms.gcm.simulation.nucleus.Plugin;
import gov.hhs.aspr.ms.gcm.simulation.nucleus.PluginData;
import gov.hhs.aspr.ms.gcm.simulation.nucleus.PluginId;
import gov.hhs.aspr.ms.gcm.simulation.nucleus.SimulationState;
import gov.hhs.aspr.ms.util.errors.ContractException;

public final class ScenarioContainer {

	private static class Data {

		private Map<PluginId, Plugin> plugins = new LinkedHashMap<>();
		private SimulationState simulationState = SimulationState.builder().build();

		private boolean locked;

		private Data() {
		}

		private Data(Data data) {
			plugins.putAll(data.plugins);
			simulationState = data.simulationState;
			locked = data.locked;
		}

		/**
		 * Standard implementation consistent with the {@link #equals(Object)} method
		 */
		@Override
		public int hashCode() {
			return Objects.hash(plugins, simulationState);
		}

		/**
		 * Two {@link Data} instances are equal if and only if their inputs are equal.
		 */
		@Override
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			}
			if (!(obj instanceof Data)) {
				return false;
			}
			Data other = (Data) obj;
			return Objects.equals(plugins, other.plugins) && Objects.equals(simulationState, other.simulationState);
		}

	}

	private final Data data;

	private ScenarioContainer(Data data) {
		this.data = data;
	}

	/**
	 * Returns a new builder instance
	 */
	public static Builder builder() {
		return new Builder(new Data());
	}

	/**
	 * Builder class for ScenarioContainer
	 */
	public static class Builder {
		private Data data;

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

		private Builder(Data data) {
			this.data = data;
		}

		/**
		 * Returns the ScenarioContainer built from the collected data.
		 */
		public ScenarioContainer build() {

			if (!data.locked) {
				validateData();
			}
			ensureImmutability();
			return new ScenarioContainer(data);
		}

		/**
		 * Adds a plugin to this container. Replaces any existing plugin with the same
		 * plugin id.
		 * 
		 * @throws ContractException
		 *                           <ul>
		 *                           <li>{@linkplain NucleusError#NULL_PLUGIN} if the
		 *                           plugin is null</li>
		 *                           </ul>
		 */
		public Builder addPlugin(final Plugin plugin) {
			ensureDataMutability();
			validatePluginNotNull(plugin);
			data.plugins.put(plugin.getPluginId(), plugin);
			return this;
		}

		/**
		 * Sets the simulation state. Defaults to the default SimulationState at the
		 * current execution time.
		 * 
		 * @throws ContractException
		 * 
		 *                           <ul>
		 *                           <li>{@linkplain NucleusError#NULL_SIMULATION_STATE}
		 *                           if the simulation state is null</li>
		 *                           </ul>
		 */
		public Builder setSimulationState(SimulationState simulationState) {
			validateSimulationStateNotNull(simulationState);
            ensureDataMutability();
			data.simulationState = simulationState;
			return this;
		}

		private void validateData() {
			// do nothing
		}

		private void validateSimulationStateNotNull(SimulationState simulationState) {
			if (simulationState == null) {
				throw new ContractException(NucleusError.NULL_SIMULATION_STATE);
			}
		}

		private void validatePluginNotNull(Plugin plugin) {
			if (plugin == null) {
				throw new ContractException(NucleusError.NULL_PLUGIN);
			}
		}

	}

	/**
	 * Returns the plugin data object compatible with the given plugin data class
	 * reference.
	 * 
	 * @throws ContractException
	 *                           <ul>
	 *                           <li>{@linkplain NucleusError#NULL_PLUGIN_DATA_CLASS}
	 *                           if the class reference is null</li>
	 *                           <li>{@linkplain NucleusError#AMBIGUOUS_PLUGIN_DATA_CLASS}
	 *                           if more than one plugin data object matches the
	 *                           class reference</li>
	 *                           </ul>
	 */
	@SuppressWarnings("unchecked")
	public <T extends PluginData> Optional<T> getPluginData(Class<T> pluginDataClass) {
		if (pluginDataClass == null) {
			throw new ContractException(NucleusError.NULL_PLUGIN_DATA_CLASS);
		}
		PluginData result = null;
		for (PluginId pluginId : data.plugins.keySet()) {
			Plugin plugin = data.plugins.get(pluginId);
			for (PluginData pluginData : plugin.getPluginDatas()) {
				if (pluginData.getClass().isAssignableFrom(pluginDataClass)) {
					if (result == null) {
						result = pluginData;
					} else {
						throw new ContractException(NucleusError.AMBIGUOUS_PLUGIN_DATA_CLASS);
					}
				}
			}
		}
		return Optional.ofNullable((T) result);
	}

	/**
	 * Returns the plugin data objects associated with the given class reference
	 * 
	 * @throws ContractException {@linkplain NucleusError#NULL_PLUGIN_DATA_CLASS} if
	 *                           the class reference is null
	 */
	@SuppressWarnings("unchecked")
	public <T extends PluginData> List<T> getPluginDatas(Class<T> pluginDataClass) {
        if (pluginDataClass == null) {
			throw new ContractException(NucleusError.NULL_PLUGIN_DATA_CLASS);
		}
		List<T> result = new ArrayList<>();
		for (PluginId pluginId : data.plugins.keySet()) {
			Plugin plugin = data.plugins.get(pluginId);
			for (PluginData pluginData : plugin.getPluginDatas()) {
				if (pluginData.getClass().isAssignableFrom(pluginDataClass)) {
					result.add((T) pluginData);
				}
			}
		}
		return result;
	}

	/**
	 * Returns the contained plugin for the given plugin id. Tolerate null.
	 */
	public Optional<Plugin> getPlugin(PluginId pluginId) {
		Plugin plugin = data.plugins.get(pluginId);
		return Optional.ofNullable(plugin);
	}

	/**
	 * Returns the list of plugins in this container.
	 */
	public List<Plugin> getPlugins() {
		return new ArrayList<>(data.plugins.values());
	}

	/**
	 * Returns the simulation state.
	 * 
	 */
	public SimulationState getSimulationState() {
		return data.simulationState;
	}

	public Builder toBuilder() {
		return new Builder(data);
	}

	/**
	 * Standard implementation consistent with the {@link #equals(Object)} method
	 */
	@Override
	public int hashCode() {
		return Objects.hash(data);
	}

	/**
	 * Two {@link ScenarioContainer} instances are equal if and only if their
	 * inputs are equal.
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
		ScenarioContainer other = (ScenarioContainer) obj;
		return Objects.equals(data, other.data);
	}
}