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
		 *             <li>{@linkplain PropertyError#INCOMPATIBLE_TIME}</li> if
		 *             a global property assignment time was less than the
		 *             associated property definition creation time.
		 * 
		 *             <li>{@linkplain PropertyError#INSUFFICIENT_PROPERTY_VALUE_ASSIGNMENT}</li>
		 *             if a global property definition has no default value and
		 *             there is also no corresponding property value assignment.
		 * 
		 * 
		 * 
		 */
		public GlobalPropertiesPluginData build() {
			try {
				if (!data.locked) {
					validateData();
				}
				ensureImmutability();
				return new GlobalPropertiesPluginData(data);
			} finally {
				data = new Data();
			}
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
		public Builder defineGlobalProperty(final GlobalPropertyId globalPropertyId, final PropertyDefinition propertyDefinition, final double time) {
			ensureDataMutability();
			validateTime(time);
			validateGlobalPropertyIdNotNull(globalPropertyId);
			validateGlobalPropertyDefinitionNotNull(propertyDefinition);
			data.globalPropertyDefinitions.put(globalPropertyId, propertyDefinition);
			data.globalPropertyDefinitionTimes.put(globalPropertyId, time);
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
		 *             <li>{@linkplain PropertyError#NEGATIVE_TIME}</li>if the
		 *             assignment time is negative
		 * 
		 *
		 * 
		 */
		public Builder setGlobalPropertyValue(final GlobalPropertyId globalPropertyId, final Object propertyValue, final double assignmentTime) {
			ensureDataMutability();
			validateTime(assignmentTime);
			validateGlobalPropertyIdNotNull(globalPropertyId);
			validateGlobalPropertyValueNotNull(propertyValue);
			data.globalPropertyValues.put(globalPropertyId, propertyValue);
			data.globalPropertyTimes.put(globalPropertyId, assignmentTime);
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

			for (final GlobalPropertyId globalPropertyId : data.globalPropertyValues.keySet()) {
				final Double propertyTime = data.globalPropertyTimes.get(globalPropertyId);
				final Double definitionTime = data.globalPropertyDefinitionTimes.get(globalPropertyId);
				if (propertyTime < definitionTime) {
					throw new ContractException(PropertyError.INCOMPATIBLE_TIME, globalPropertyId + " at " + propertyTime);
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

		private final Map<GlobalPropertyId, Double> globalPropertyDefinitionTimes = new LinkedHashMap<>();

		private final Map<GlobalPropertyId, Object> globalPropertyValues = new LinkedHashMap<>();

		private final Map<GlobalPropertyId, Double> globalPropertyTimes = new LinkedHashMap<>();

		private boolean locked;

		private Data() {
		}

		private Data(Data data) {
			globalPropertyDefinitions.putAll(data.globalPropertyDefinitions);
			globalPropertyDefinitionTimes.putAll(data.globalPropertyDefinitionTimes);
			globalPropertyValues.putAll(data.globalPropertyValues);
			globalPropertyTimes.putAll(data.globalPropertyTimes);
			locked = data.locked;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((globalPropertyDefinitionTimes == null) ? 0 : globalPropertyDefinitionTimes.hashCode());
			result = prime * result + ((globalPropertyDefinitions == null) ? 0 : globalPropertyDefinitions.hashCode());
			result = prime * result + ((globalPropertyTimes == null) ? 0 : globalPropertyTimes.hashCode());
			result = prime * result + ((globalPropertyValues == null) ? 0 : globalPropertyValues.hashCode());
			result = prime * result + (locked ? 1231 : 1237);
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
			if (globalPropertyDefinitionTimes == null) {
				if (other.globalPropertyDefinitionTimes != null) {
					return false;
				}
			} else if (!globalPropertyDefinitionTimes.equals(other.globalPropertyDefinitionTimes)) {
				return false;
			}
			if (globalPropertyDefinitions == null) {
				if (other.globalPropertyDefinitions != null) {
					return false;
				}
			} else if (!globalPropertyDefinitions.equals(other.globalPropertyDefinitions)) {
				return false;
			}

			for (GlobalPropertyId globalPropertyId : globalPropertyDefinitions.keySet()) {
				Object propertyValue = globalPropertyValues.get(globalPropertyId);
				if (propertyValue == null) {
					propertyValue = globalPropertyDefinitions.get(globalPropertyId).getDefaultValue().get();
				}
				Object otherPropertyValue = other.globalPropertyValues.get(globalPropertyId);
				if (otherPropertyValue == null) {
					otherPropertyValue = other.globalPropertyDefinitions.get(globalPropertyId).getDefaultValue().get();
				}
				if (!propertyValue.equals(otherPropertyValue)) {
					return false;
				}
			}

			for (GlobalPropertyId globalPropertyId : globalPropertyDefinitions.keySet()) {
				Double propertyTime = globalPropertyTimes.get(globalPropertyId);
				if (propertyTime == null) {
					propertyTime = globalPropertyDefinitionTimes.get(globalPropertyId);
				}
				Double otherPropertyTime = other.globalPropertyTimes.get(globalPropertyId);
				if (otherPropertyTime == null) {
					otherPropertyTime = other.globalPropertyDefinitionTimes.get(globalPropertyId);
				}
				if (!propertyTime.equals(otherPropertyTime)) {
					return false;
				}
			}

			if (locked != other.locked) {
				return false;
			}
			return true;
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

	private static void validateTime(double time) {
		if (time < 0) {
			throw new ContractException(PropertyError.NEGATIVE_TIME);
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
	 * Returns the creation time for the given {@link GlobalPropertyId}.
	 * 
	 * @throws ContractException
	 * 
	 *             <li>{@linkplain PropertyError#NULL_PROPERTY_ID}</li> if the
	 *             global property id is null
	 *             <li>{@linkplain PropertyError#UNKNOWN_PROPERTY_ID}</li> if
	 *             the global property id is known
	 */
	public Double getGlobalPropertyDefinitionTime(final GlobalPropertyId globalPropertyId) {
		validateGlobalPropertyIdExists(data, globalPropertyId);
		return data.globalPropertyDefinitionTimes.get(globalPropertyId);
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

	/**
	 * Returns the property assignment time for the given
	 * {@link GlobalPropertyId}.
	 * 
	 * @throws ContractException
	 * 
	 * 
	 *             <li>{@linkplain PropertyError#NULL_PROPERTY_ID}</li> if the
	 *             global property id is null
	 *             <li>{@linkplain PropertyError#UNKNOWN_PROPERTY_ID}</li> if
	 *             the global property id is known
	 */
	public Double getGlobalPropertyTime(final GlobalPropertyId globalPropertyId) {
		validateGlobalPropertyIdExists(data, globalPropertyId);
		Double result = data.globalPropertyTimes.get(globalPropertyId);
		if (result == null) {
			result = data.globalPropertyDefinitionTimes.get(globalPropertyId);
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
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((data == null) ? 0 : data.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof GlobalPropertiesPluginData)) {
			return false;
		}
		GlobalPropertiesPluginData other = (GlobalPropertiesPluginData) obj;
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
