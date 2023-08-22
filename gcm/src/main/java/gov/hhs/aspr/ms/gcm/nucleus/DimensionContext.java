package gov.hhs.aspr.ms.gcm.nucleus;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import util.errors.ContractException;

/**
 * A context containing PluginData and PluginDataBuilder instances that are used
 * to build a particular scenario within an experiment.
 */
public final class DimensionContext implements PluginDataBuilderContainer {

	private Map<Class<?>, PluginDataBuilder> pluginDataBuilderBaseMap = new LinkedHashMap<>();
	private Map<Class<?>, PluginDataBuilder> pluginDataBuilderWorkingMap = new LinkedHashMap<>();
	private Map<Class<?>, PluginData> pluginDataBaseMap = new LinkedHashMap<>();
	private Map<Class<?>, PluginData> pluginDataWorkingMap = new LinkedHashMap<>();

	private DimensionContext() {
	}

	/**
	 * A builder class for DimensionContext
	 */
	public static class Builder {
		private Builder() {
		}

		private Map<Class<?>, PluginDataBuilder> pluginDataBuilderMap = new LinkedHashMap<>();
		private Map<Class<?>, PluginData> pluginDataMap = new LinkedHashMap<>();

		/**
		 * Returns the DimensionContext instance composed from the inputs to this
		 * builder.
		 */
		public DimensionContext build() {
			DimensionContext result = new DimensionContext();
			result.pluginDataBuilderBaseMap.putAll(pluginDataBuilderMap);
			result.pluginDataBaseMap.putAll(pluginDataMap);
			return result;
		}

		/**
		 * Given a plugin Data, will add it and its clone builder to the internal map in
		 * this class
		 * 
		 * @throws ContractException {@linkplain NucleusError#NULL_PLUGIN_DATA} if the
		 *                           plugin data builder is null
		 */
		public <T extends PluginData> PluginDataBuilder add(T t) {
			if (t == null) {
				throw new ContractException(NucleusError.NULL_PLUGIN_DATA);
			}
			pluginDataMap.put(t.getClass(), t);
			PluginDataBuilder builder = t.getCloneBuilder();

			pluginDataBuilderMap.put(builder.getClass(), builder);
			return builder;
		}
	}

	/**
	 * Returns a typed Builder instance for DimensionContext
	 */
	public static Builder builder() {
		return new Builder();
	}

	@Override
	public <T extends PluginDataBuilder> T getPluginDataBuilder(Class<T> classRef) {

		PluginDataBuilder pluginDataBuilder = pluginDataBuilderWorkingMap.get(classRef);
		if (pluginDataBuilder == null) {
			List<Class<?>> candidates = new ArrayList<>();
			for (Class<?> c : pluginDataBuilderBaseMap.keySet()) {
				if (classRef.isAssignableFrom(c)) {
					candidates.add(c);
				}
			}
			if (candidates.isEmpty()) {
				throw new ContractException(NucleusError.UNKNOWN_PLUGIN_DATA_BUILDER_CLASS);
			}
			if (candidates.size() > 1) {
				throw new ContractException(NucleusError.AMBIGUOUS_PLUGIN_DATA_BUILDER_CLASS);
			}

			pluginDataBuilder = pluginDataBuilderBaseMap.get(candidates.get(0));
			pluginDataBuilderWorkingMap.put(classRef, pluginDataBuilder);
		}

		return classRef.cast(pluginDataBuilder);
	}

	/**
	 * Returns the stored item matching the given class reference.
	 * 
	 * @throws ContractException
	 *                           <ul>
	 *                           <li>{@linkplain NucleusError#AMBIGUOUS_PLUGIN_DATA_CLASS}
	 *                           if more than one plugin data matches the given
	 *                           class reference</li>
	 *                           <li>{@linkplain NucleusError#UNKNOWN_PLUGIN_DATA_CLASS}
	 *                           if no plugin data matches the given class
	 *                           reference</li>
	 *                           </ul>
	 */
	public <T extends PluginData> T getPluginData(Class<T> classRef) {

		PluginData pluginData = pluginDataWorkingMap.get(classRef);
		if (pluginData == null) {
			List<Class<?>> candidates = new ArrayList<>();
			for (Class<?> c : pluginDataBaseMap.keySet()) {
				if (classRef.isAssignableFrom(c)) {
					candidates.add(c);
				}
			}
			if (candidates.isEmpty()) {
				throw new ContractException(NucleusError.UNKNOWN_PLUGIN_DATA_CLASS);
			}
			if (candidates.size() > 1) {
				throw new ContractException(NucleusError.AMBIGUOUS_PLUGIN_DATA_CLASS);
			}

			pluginData = pluginDataBaseMap.get(candidates.get(0));
			pluginDataWorkingMap.put(classRef, pluginData);
		}

		return classRef.cast(pluginData);
	}
}
