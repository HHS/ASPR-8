package gov.hhs.aspr.ms.gcm.nucleus;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import util.errors.ContractException;

/**
 * An implementor of PluginDataBuilderContainer.
 */
public final class PluginDataBuilderContext implements PluginDataBuilderContainer{

	private Map<Class<?>, PluginDataBuilder> pluginDataBuilderBaseMap = new LinkedHashMap<>();
	private Map<Class<?>, PluginDataBuilder> pluginDataBuilderWorkingMap = new LinkedHashMap<>();

	private PluginDataBuilderContext() {
	}

	/**
	 * A builder class for DimensionContext
	 * 
	 *
	 */
	public static class Builder {
		private Builder() {
		}

		private Map<Class<?>, PluginDataBuilder> pluginDataBuilderMap = new LinkedHashMap<>();
		

		/**
		 * Returns the DimensionContext instance composed from the inputs to
		 * this builder.
		 */
		public PluginDataBuilderContext build() {
			PluginDataBuilderContext result = new PluginDataBuilderContext();
			result.pluginDataBuilderBaseMap.putAll(pluginDataBuilderMap);			
			return result;
		}

		/**
		 * Given a plugin Data, will add it and its clone builder to the internal map in
		 * this class
		 * 
		 * @throws ContractException
		 *                           <ul>
		 *                           <li>{@linkplain NucleusError#NULL_PLUGIN_DATA_BUILDER} if
		 *                           the plugin data builder is null</li>
		 * 
		 */
		public <T extends PluginDataBuilder> Builder add(T t) {
			if (t == null) {
				throw new ContractException(NucleusError.NULL_PLUGIN_DATA_BUILDER);
			}
			pluginDataBuilderMap.put(t.getClass(), t);
			return this;
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

	
}
