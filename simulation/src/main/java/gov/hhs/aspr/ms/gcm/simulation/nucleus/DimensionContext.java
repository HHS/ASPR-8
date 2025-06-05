package gov.hhs.aspr.ms.gcm.simulation.nucleus;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import gov.hhs.aspr.ms.util.errors.ContractException;

/**
 * A context containing PluginData and PluginDataBuilder instances that are used
 * to build a particular scenario within an experiment.
 */
public final class DimensionContext implements PluginDataBuilderContainer {

	private SimulationState simulationState;
	private Map<Class<?>, List<PluginDataBuilder>> pluginDataBuilderBaseMap = new LinkedHashMap<>();
	private Map<Class<?>, List<PluginDataBuilder>> pluginDataBuilderWorkingMap = new LinkedHashMap<>();
	private Map<Class<?>, List<PluginData>> pluginDataBaseMap = new LinkedHashMap<>();
	private Map<Class<?>, List<PluginData>> pluginDataWorkingMap = new LinkedHashMap<>();

	private DimensionContext() {
	}

	/**
	 * A builder class for DimensionContext
	 */
	public static class Builder {
		private Builder() {
		}

		private SimulationState simulationState;
		private Map<Class<?>, List<PluginDataBuilder>> pluginDataBuilderMap = new LinkedHashMap<>();
		private Map<Class<?>, List<PluginData>> pluginDataMap = new LinkedHashMap<>();

		private void validate() {
			if(simulationState == null) {
				throw new ContractException(NucleusError.NULL_SIMULATION_STATE);
			}
		}

		/**
		 * Returns the DimensionContext instance composed from the inputs to this
		 * builder.
		 * 
		 * @throws ContractException
		 *                           <ul>
		 *                           <li>{@linkplain NucleusError#NULL_SIMULATION_STATE}
		 *                           if the simulation state is not set</li>
		 *                           </ul>
		 */
		public DimensionContext build() {
			validate();
			DimensionContext result = new DimensionContext();
			result.pluginDataBuilderBaseMap.putAll(pluginDataBuilderMap);
			result.pluginDataBaseMap.putAll(pluginDataMap);
			result.simulationState = simulationState;
			return result;
		}

		/**
		 * Adds the plugin data to the context and returns the plugin data builder
		 * generated from the plugin data.
		 * 
		 * @throws ContractException {@linkplain NucleusError#NULL_PLUGIN_DATA} if the
		 *                           plugin data builder is null
		 */
		public <T extends PluginData> PluginDataBuilder add(T t) {
			if (t == null) {
				throw new ContractException(NucleusError.NULL_PLUGIN_DATA);
			}
			List<PluginData> pluginDatas = pluginDataMap.get(t.getClass());
			if (pluginDatas == null) {
				pluginDatas = new ArrayList<>();
				pluginDataMap.put(t.getClass(), pluginDatas);
			}
			pluginDatas.add(t);

			PluginDataBuilder builder = t.toBuilder();
			List<PluginDataBuilder> pluginDataBuilders = pluginDataBuilderMap.get(builder.getClass());
			if (pluginDataBuilders == null) {
				pluginDataBuilders = new ArrayList<>();
				pluginDataBuilderMap.put(builder.getClass(), pluginDataBuilders);
			}
			pluginDataBuilders.add(builder);
			return builder;
		}
		
		public void setSimulationState(SimulationState simulationState) {
			this.simulationState = simulationState;
		}
	}

	/**
	 * Returns a typed Builder instance for DimensionContext
	 */
	public static Builder builder() {
		return new Builder();
	}

	/**
	 * Returns the stored plugin data builder matching the given class reference.
	 * 
	 * @throws ContractException
	 *                           <ul>
	 *                           <li>{@linkplain NucleusError#NULL_PLUGIN_DATA_BUILDER_CLASS}
	 *                           if the class reference is null</li>
	 *                           <li>{@linkplain NucleusError#AMBIGUOUS_PLUGIN_DATA_BUILDER_CLASS}
	 *                           if more than one plugin data builder matches the
	 *                           given class reference</li>
	 *                           <li>{@linkplain NucleusError#UNKNOWN_PLUGIN_DATA_BUILDER_CLASS}
	 *                           if no plugin data builder matches the given class
	 *                           reference</li>
	 *                           </ul>
	 */
	@Override
	public <T extends PluginDataBuilder> T getPluginDataBuilder(Class<T> classRef) {
		if (classRef == null) {
			throw new ContractException(NucleusError.NULL_PLUGIN_DATA_BUILDER_CLASS);
		}
		List<PluginDataBuilder> pluginDataBuilders = pluginDataBuilderWorkingMap.get(classRef);

		if (pluginDataBuilders == null) {
			pluginDataBuilders = new ArrayList<>();
			for (Class<?> c : pluginDataBuilderBaseMap.keySet()) {
				if (classRef.isAssignableFrom(c)) {
					List<PluginDataBuilder> list = pluginDataBuilderBaseMap.get(c);
					pluginDataBuilders.addAll(list);
				}
			}
			pluginDataBuilderWorkingMap.put(classRef, pluginDataBuilders);
		}
		if (pluginDataBuilders.isEmpty()) {
			throw new ContractException(NucleusError.UNKNOWN_PLUGIN_DATA_BUILDER_CLASS);
		}
		if (pluginDataBuilders.size() > 1) {
			throw new ContractException(NucleusError.AMBIGUOUS_PLUGIN_DATA_BUILDER_CLASS);
		}
		PluginDataBuilder pluginDataBuilder = pluginDataBuilders.get(0);

		return classRef.cast(pluginDataBuilder);
	}

	/**
	 * Returns the stored plugin data builders matching the given class reference.
	 * 
	 * @throws ContractException
	 *                           <ul>
	 *                           <li>{@linkplain NucleusError#NULL_PLUGIN_DATA_BUILDER_CLASS}
	 *                           if the class reference is null</li> *
	 *                           </ul>
	 */
	@SuppressWarnings("unchecked")
	public <T extends PluginDataBuilder> List<T> getPluginDataBuilders(Class<T> classRef) {
		if (classRef == null) {
			throw new ContractException(NucleusError.NULL_PLUGIN_DATA_BUILDER_CLASS);
		}

		List<T> result = new ArrayList<>();

		List<PluginDataBuilder> pluginDataBuilders = pluginDataBuilderWorkingMap.get(classRef);

		if (pluginDataBuilders == null) {
			pluginDataBuilders = new ArrayList<>();
			for (Class<?> c : pluginDataBuilderBaseMap.keySet()) {
				if (classRef.isAssignableFrom(c)) {
					List<PluginDataBuilder> list = pluginDataBuilderBaseMap.get(c);
					pluginDataBuilders.addAll(list);
				}
			}
			pluginDataBuilderWorkingMap.put(classRef, pluginDataBuilders);
		}

		for (PluginDataBuilder pluginDataBuilder : pluginDataBuilders) {
			result.add((T) pluginDataBuilder);
		}

		return result;
	}

	/**
	 * Returns the stored plugin data matching the given class reference.
	 * 
	 * @throws ContractException
	 *                           <ul>
	 *                           <li>{@linkplain NucleusError#NULL_PLUGIN_DATA_CLASS}
	 *                           if the class reference is null</li>
	 *                           <li>{@linkplain NucleusError#AMBIGUOUS_PLUGIN_DATA_CLASS}
	 *                           if more than one plugin data matches the given
	 *                           class reference</li>
	 *                           <li>{@linkplain NucleusError#UNKNOWN_PLUGIN_DATA_CLASS}
	 *                           if no plugin data matches the given class
	 *                           reference</li>
	 *                           </ul>
	 */
	public <T extends PluginData> T getPluginData(Class<T> classRef) {

		if (classRef == null) {
			throw new ContractException(NucleusError.NULL_PLUGIN_DATA_CLASS);
		}

		List<PluginData> pluginDatas = pluginDataWorkingMap.get(classRef);
		if (pluginDatas == null) {
			pluginDatas = new ArrayList<>();
			for (Class<?> c : pluginDataBaseMap.keySet()) {
				if (classRef.isAssignableFrom(c)) {
					List<PluginData> list = pluginDataBaseMap.get(c);
					pluginDatas.addAll(list);
				}
			}
			pluginDataWorkingMap.put(classRef, pluginDatas);
		}

		if (pluginDatas.isEmpty()) {
			throw new ContractException(NucleusError.UNKNOWN_PLUGIN_DATA_CLASS);
		}
		if (pluginDatas.size() > 1) {
			throw new ContractException(NucleusError.AMBIGUOUS_PLUGIN_DATA_CLASS);
		}

		PluginData pluginData = pluginDatas.get(0);

		return classRef.cast(pluginData);
	}

	/**
	 * Returns the stored plugin datas matching the given class reference.
	 * 
	 * @throws ContractException
	 *                           <ul>
	 *                           <li>{@linkplain NucleusError#NULL_PLUGIN_DATA_CLASS}
	 *                           if the class reference is null</li>
	 *                           </ul>
	 */
	@SuppressWarnings("unchecked")
	public <T extends PluginData> List<T> getPluginDatas(Class<T> classRef) {

		if (classRef == null) {
			throw new ContractException(NucleusError.NULL_PLUGIN_DATA_CLASS);
		}

		List<T> result = new ArrayList<>();

		List<PluginData> pluginDatas = pluginDataWorkingMap.get(classRef);
		if (pluginDatas == null) {
			pluginDatas = new ArrayList<>();
			for (Class<?> c : pluginDataBaseMap.keySet()) {
				if (classRef.isAssignableFrom(c)) {
					List<PluginData> list = pluginDataBaseMap.get(c);
					pluginDatas.addAll(list);
				}
			}
			pluginDataWorkingMap.put(classRef, pluginDatas);
		}

		for (PluginData pluginData : pluginDatas) {
			result.add((T) pluginData);
		}

		return result;
	}

	/**
	 * Returns the simulation state.
	 */
	public SimulationState getSimulationState() {
		return simulationState;
	}
}
