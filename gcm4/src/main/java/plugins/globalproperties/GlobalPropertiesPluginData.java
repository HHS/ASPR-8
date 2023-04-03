package plugins.globalproperties;

import java.util.*;

import net.jcip.annotations.Immutable;
import nucleus.PluginData;
import nucleus.PluginDataBuilder;
import plugins.globalproperties.support.GlobalPropertyId;
import plugins.util.properties.PropertyDefinition;
import plugins.util.properties.PropertyError;
import util.errors.ContractException;

/**
 * An immutable container of the initial state of global components and global
 * properties. It contains: <BR>
 * <ul>
 * <li>global property definitions</li>
 * <li>global property values</li>
 * </ul>
 * 
 *
 */
@Immutable
public final class GlobalPropertiesPluginData implements PluginData {

	/**
	 * Builder class for GloblaInitialData
	 * 
	 *
	 */
	public static class Builder implements PluginDataBuilder {
		private Data data;

		private Builder(Data data) {
			this.data = data;
		}

		/**
		 * Returns the {@link GlobalPropertiesPluginData} from the collected
		 * information supplied to this builder. Clears the builder's state.
		 * 
		 * @throws ContractException
		 *             <li>{@linkplain PropertyError#UNKNOWN_PROPERTY_ID}</li>
		 *             if a global property value was associated with a global
		 *             property id that was not defined
		 * 
		 *             <li>{@linkplain PropertyError#INCOMPATIBLE_VALUE}</li> if
		 *             a global property value was associated with a global
		 *             property id that is incompatible with the corresponding
		 *             property definition.
		 * 
		 *             <li>{@linkplain PropertyError#INSUFFICIENT_PROPERTY_VALUE_ASSIGNMENT}</li>
		 *             if a global property definition has no default value and
		 *             there is also no corresponding property value assignment.
		 * 
		 */
		public GlobalPropertiesPluginData build() {
			if (!data.locked) {
				validateData();	
			}			
			ensureImmutability();
			return new GlobalPropertiesPluginData(data);
		}

		/**
		 * Defines a global property Duplicate inputs override previous inputs.
		 * 
		 * @throws ContractException
		 * 
		 * 
		 *             <li>{@linkplain PropertyError#NULL_PROPERTY_ID}</li> if
		 *             the global property id is null
		 * 
		 *             <li>{@linkplain PropertyError#NULL_PROPERTY_DEFINITION}
		 *             </li> if the property definition is null
		 *
		 * 
		 */
		public Builder defineGlobalProperty(final GlobalPropertyId globalPropertyId, final PropertyDefinition propertyDefinition) {
			ensureDataMutability();
			validateGlobalPropertyIdNotNull(globalPropertyId);
			validateGlobalPropertyDefinitionNotNull(propertyDefinition);
			data.globalPropertyDefinitions.put(globalPropertyId, propertyDefinition);
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

		/**
		 * Sets the global property value that overrides the default value of
		 * the corresponding property definition. Duplicate inputs override
		 * previous inputs.
		 * 
		 * @throws ContractException
		 * 
		 *             <li>{@linkplain PropertyError#NULL_PROPERTY_ID}</li>if
		 *             the global property id is null
		 * 
		 *             <li>{@linkplain PropertyError#NULL_PROPERTY_VALUE}</li>if
		 *             the global property value is null
		 *
		 * 
		 */
		public Builder setGlobalPropertyValue(final GlobalPropertyId globalPropertyId, final Object propertyValue) {
			ensureDataMutability();
			validateGlobalPropertyIdNotNull(globalPropertyId);
			validateGlobalPropertyValueNotNull(propertyValue);
			data.globalPropertyValues.put(globalPropertyId, propertyValue);
			return this;
		}

		private void validateData() {
			
			for (final GlobalPropertyId globalPropertyId : data.globalPropertyValues.keySet()) {
				if (!data.globalPropertyDefinitions.containsKey(globalPropertyId)) {
					throw new ContractException(PropertyError.UNKNOWN_PROPERTY_ID, globalPropertyId);
				}
			}
			for (final GlobalPropertyId globalPropertyId : data.globalPropertyValues.keySet()) {
				final Object propertyValue = data.globalPropertyValues.get(globalPropertyId);
				final PropertyDefinition propertyDefinition = data.globalPropertyDefinitions.get(globalPropertyId);
				if (!propertyDefinition.getType().isAssignableFrom(propertyValue.getClass())) {
					throw new ContractException(PropertyError.INCOMPATIBLE_VALUE, globalPropertyId + " = " + propertyValue);
				}
			}

			/*
			 * For every global property definition that has a null default
			 * value, ensure that there is a corresponding global property value
			 * assignment
			 * 
			 */
			for (GlobalPropertyId globalPropertyId : data.globalPropertyDefinitions.keySet()) {
				PropertyDefinition propertyDefinition = data.globalPropertyDefinitions.get(globalPropertyId);
				if (!propertyDefinition.getDefaultValue().isPresent()) {
					Object propertyValue = data.globalPropertyValues.get(globalPropertyId);
					if (propertyValue == null) {
						throw new ContractException(PropertyError.INSUFFICIENT_PROPERTY_VALUE_ASSIGNMENT, globalPropertyId);
					}
				}
			}
		}

	}

	private static class Data {

		private final Map<GlobalPropertyId, PropertyDefinition> globalPropertyDefinitions = new LinkedHashMap<>();

		private final Map<GlobalPropertyId, Object> globalPropertyValues = new LinkedHashMap<>();

		private boolean locked;

		private Data() {
		}

		private Data(Data data) {
			globalPropertyDefinitions.putAll(data.globalPropertyDefinitions);
			globalPropertyValues.putAll(data.globalPropertyValues);
			locked = data.locked;
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (!(o instanceof Data)) return false;
			Data data = (Data) o;
			return locked == data.locked && globalPropertyDefinitions.equals(data.globalPropertyDefinitions) && globalPropertyValues.equals(data.globalPropertyValues);
		}

		@Override
		public int hashCode() {
			return Objects.hash(globalPropertyDefinitions, globalPropertyValues, locked);
		}
	}

	/**
	 * Returns a Builder instance
	 */
	public static Builder builder() {
		return new Builder(new Data());
	}

	private static void validateGlobalPropertyDefinitionNotNull(final PropertyDefinition propertyDefinition) {
		if (propertyDefinition == null) {
			throw new ContractException(PropertyError.NULL_PROPERTY_DEFINITION);
		}
	}

	private static void validateGlobalPropertyIdNotNull(final GlobalPropertyId globalPropertyId) {
		if (globalPropertyId == null) {
			throw new ContractException(PropertyError.NULL_PROPERTY_ID);
		}
	}

	private static void validateGlobalPropertyValueNotNull(final Object propertyValue) {
		if (propertyValue == null) {
			throw new ContractException(PropertyError.NULL_PROPERTY_VALUE);
		}
	}

	private final Data data;

	private GlobalPropertiesPluginData(final Data data) {
		this.data = data;
	}

	/**
	 * Returns the {@link PropertyDefinition} for the given
	 * {@link GlobalPropertyId}.
	 * 
	 * @throws ContractException
	 * 
	 *             <li>{@linkplain PropertyError#NULL_PROPERTY_ID}</li> if the
	 *             global property id is null
	 *             <li>{@linkplain PropertyError#UNKNOWN_PROPERTY_ID}</li> if
	 *             the global property id is known
	 */
	public PropertyDefinition getGlobalPropertyDefinition(final GlobalPropertyId globalPropertyId) {
		validateGlobalPropertyIdExists(data, globalPropertyId);
		return data.globalPropertyDefinitions.get(globalPropertyId);
	}

	/**
	 * Returns the set of {@link GlobalPropertyId}
	 * 
	 */
	@SuppressWarnings("unchecked")
	public <T extends GlobalPropertyId> Set<T> getGlobalPropertyIds() {
		final Set<T> result = new LinkedHashSet<>();
		for (final GlobalPropertyId globalPropertyId : data.globalPropertyDefinitions.keySet()) {
			result.add((T) globalPropertyId);
		}
		return result;
	}

	/**
	 * Returns the property value for the given {@link GlobalPropertyId}.
	 * 
	 * @throws ContractException
	 * 
	 * 
	 *             <li>{@linkplain PropertyError#NULL_PROPERTY_ID}</li> if the
	 *             global property id is null
	 *             <li>{@linkplain PropertyError#UNKNOWN_PROPERTY_ID}</li> if
	 *             the global property id is known
	 */
	@SuppressWarnings("unchecked")
	public <T> T getGlobalPropertyValue(final GlobalPropertyId globalPropertyId) {
		validateGlobalPropertyIdExists(data, globalPropertyId);
		T result = (T) data.globalPropertyValues.get(globalPropertyId);
		if (result == null) {
			result = (T) data.globalPropertyDefinitions.get(globalPropertyId).getDefaultValue().get();
		}
		return result;
	}

	private static void validateGlobalPropertyIdExists(final Data data, final GlobalPropertyId globalPropertyId) {
		if (globalPropertyId == null) {
			throw new ContractException(PropertyError.NULL_PROPERTY_ID);
		}
		if (!data.globalPropertyDefinitions.containsKey(globalPropertyId)) {
			throw new ContractException(PropertyError.UNKNOWN_PROPERTY_ID, globalPropertyId);
		}
	}

	@Override
	public PluginDataBuilder getCloneBuilder() {
		return new Builder(data);
	}

	@Override
	public PluginDataBuilder getEmptyBuilder() {
		return builder();
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof GlobalPropertiesPluginData)) return false;
		GlobalPropertiesPluginData that = (GlobalPropertiesPluginData) o;
		return data.equals(that.data);
	}

	@Override
	public int hashCode() {
		return Objects.hash(data);
	}
}
