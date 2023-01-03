package nucleus;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import util.errors.ContractException;

/**
 * 
 * A context containing PluginDataBuilder instances that are used to build a
 * particular scenario within an experiment.
 * 
 *
 * 
 */
public final class PluginDataBuilderContext {

	private PluginDataBuilderContext() {
	}

	private Map<Class<?>, PluginDataBuilder> baseMap = new LinkedHashMap<>();

	private Map<Class<?>, PluginDataBuilder> workingMap = new LinkedHashMap<>();

	/**
	 * Returns the stored item matching the given class reference.
	 * 
	 * @throws ContractException
	 *             <li>{@linkplain NucleusError#AMBIGUOUS_PLUGIN_DATA_BUILDER_CLASS}
	 *             if more than one plugin data builder matches the given class
	 *             reference</li>
	 * 
	 *             <li>{@linkplain NucleusError#UNKNOWN_PLUGIN_DATA_BUILDER_CLASS}
	 *             if no plugin data builder matches the given class
	 *             reference</li>
	 */
	@SuppressWarnings("unchecked")
	public <T extends PluginDataBuilder> T get(Class<T> classRef) {

		PluginDataBuilder pluginDataBuilder = workingMap.get(classRef);
		if (pluginDataBuilder == null) {
			List<Class<?>> candidates = new ArrayList<>();
			for (Class<?> c : baseMap.keySet()) {
				if (classRef.isAssignableFrom(c)) {
					candidates.add(c);
				}
			}
			if (candidates.size() > 1) {
				throw new ContractException(NucleusError.AMBIGUOUS_PLUGIN_DATA_BUILDER_CLASS);
			}
			if (candidates.size() == 1) {
				pluginDataBuilder = baseMap.get(candidates.get(0));
				workingMap.put(classRef, pluginDataBuilder);
			}
		}
		if (pluginDataBuilder == null) {
			throw new ContractException(NucleusError.UNKNOWN_PLUGIN_DATA_BUILDER_CLASS);
		}
		return (T)pluginDataBuilder;
	}

	/**
	 * Returns a typed Builder instance for PluginDataBuilderContext
	 */
	public static Builder builder() {
		return new Builder();
	}

	/**
	 * A builder class for PluginDataBuilderContext
	 * 
	 *
	 */
	public static class Builder {
		private Builder() {
		}

		private Map<Class<?>, PluginDataBuilder> map = new LinkedHashMap<>();

		/**
		 * Returns the PluginDataBuilderContext instance composed from the
		 * inputs to this builder.
		 */
		public PluginDataBuilderContext build() {
			try {
				PluginDataBuilderContext result = new PluginDataBuilderContext();
				result.baseMap = map;
				return result;
			} finally {
				map = new LinkedHashMap<>();
			}
		}

		/**
		 * @throws ContractException
		 *             <li>{@linkplain NucleusError#NULL_PLUGIN_DATA_BUILDER} if
		 *             the plugin data builder is null</li>
		 * 
		 */
		public <T extends PluginDataBuilder> Builder add(T t) {
			if (t == null) {
				throw new ContractException(NucleusError.NULL_PLUGIN_DATA_BUILDER);
			}
			map.put(t.getClass(), t);
			return this;
		}
	}

	/**
	 * Returns the set of items stored in this container.
	 */
	public Set<PluginDataBuilder> getContents() {
		Set<PluginDataBuilder> result = new LinkedHashSet<>();
		for (PluginDataBuilder pluginDataBuilder : baseMap.values()) {
			result.add(pluginDataBuilder);
		}
		return result;
	}

}
