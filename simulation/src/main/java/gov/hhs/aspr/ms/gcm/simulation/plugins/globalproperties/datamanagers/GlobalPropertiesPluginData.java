package gov.hhs.aspr.ms.gcm.simulation.plugins.globalproperties.datamanagers;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import gov.hhs.aspr.ms.gcm.simulation.nucleus.PluginData;
import gov.hhs.aspr.ms.gcm.simulation.nucleus.PluginDataBuilder;
import gov.hhs.aspr.ms.gcm.simulation.nucleus.StandardVersioning;
import gov.hhs.aspr.ms.gcm.simulation.plugins.globalproperties.support.GlobalPropertyId;
import gov.hhs.aspr.ms.gcm.simulation.plugins.properties.support.PropertyDefinition;
import gov.hhs.aspr.ms.gcm.simulation.plugins.properties.support.PropertyError;
import gov.hhs.aspr.ms.util.errors.ContractException;
import net.jcip.annotations.Immutable;

/**
 * An immutable container of the initial state of global components and global
 * properties. It contains: <BR>
 * <ul>
 * <li>global property definitions</li>
 * <li>global property values</li>
 * </ul>
 */
@Immutable
public final class GlobalPropertiesPluginData implements PluginData {

	/**
	 * Builder class for GloblaInitialData
	 */
	public static class Builder implements PluginDataBuilder {
		private Data data;

		private Builder(Data data) {
			this.data = data;
		}

		/**
		 * Returns the {@link GlobalPropertiesPluginData} from the collected information
		 * supplied to this builder. Clears the builder's state.
		 * 
		 * @throws ContractException
		 *                           <ul>
		 *                           <li>{@linkplain PropertyError#UNKNOWN_PROPERTY_ID}
		 *                           if a global property value was associated with a
		 *                           global property id that was not defined</li>
		 *                           <li>{@linkplain PropertyError#INCOMPATIBLE_VALUE}
		 *                           if a global property value was associated with a
		 *                           global property id that is incompatible with the
		 *                           corresponding property definition.</li>
		 *                           <li>{@linkplain PropertyError#UNKNOWN_PROPERTY_ID}
		 *                           if a global property time was associated with a
		 *                           global property id that was not defined</li>
		 *                           <li>{@linkplain PropertyError#INCOMPATIBLE_TIME} if
		 *                           a global property assignment time was less than the
		 *                           associated property definition creation time.</li>
		 *                           <li>{@linkplain PropertyError#INSUFFICIENT_PROPERTY_VALUE_ASSIGNMENT}
		 *                           if a global property definition has no default
		 *                           value and there is also no corresponding property
		 *                           value assignment.</li>
		 *                           </ul>
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
		 *                           <ul>
		 *                           <li>{@linkplain PropertyError#NULL_PROPERTY_ID} if
		 *                           the global property id is null</li>
		 *                           <li>{@linkplain PropertyError#NULL_PROPERTY_DEFINITION}
		 *                           if the property definition is null</li>
		 *                           </ul>
		 */
		public Builder defineGlobalProperty(final GlobalPropertyId globalPropertyId,
				final PropertyDefinition propertyDefinition, final double time) {
			ensureDataMutability();
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
		 * Sets the global property value that overrides the default value of the
		 * corresponding property definition. Duplicate inputs override previous inputs.
		 * 
		 * @throws ContractException
		 *                           <ul>
		 *                           <li>{@linkplain PropertyError#NULL_PROPERTY_ID}if
		 *                           the global property id is null</li>
		 *                           <li>{@linkplain PropertyError#NULL_PROPERTY_VALUE}if
		 *                           the global property value is null</li>
		 *                           </ul>
		 */
		public Builder setGlobalPropertyValue(final GlobalPropertyId globalPropertyId, final Object propertyValue,
				final double assignmentTime) {
			ensureDataMutability();
			validateGlobalPropertyIdNotNull(globalPropertyId);
			validateGlobalPropertyValueNotNull(propertyValue);
			data.globalPropertyValues.put(globalPropertyId, propertyValue);
			data.globalPropertyTimes.put(globalPropertyId, assignmentTime);
			return this;
		}

		private void validateData() {

			/*
			 * show that each recorded property value corresponds to a known property
			 * definition
			 */
			for (final GlobalPropertyId globalPropertyId : data.globalPropertyValues.keySet()) {
				if (!data.globalPropertyDefinitions.containsKey(globalPropertyId)) {
					throw new ContractException(PropertyError.UNKNOWN_PROPERTY_ID, globalPropertyId);
				}
			}

			/*
			 * show that each property value is compatible with the corresponding property
			 * definition
			 */
			for (final GlobalPropertyId globalPropertyId : data.globalPropertyValues.keySet()) {
				final Object propertyValue = data.globalPropertyValues.get(globalPropertyId);
				final PropertyDefinition propertyDefinition = data.globalPropertyDefinitions.get(globalPropertyId);
				if (!propertyDefinition.getType().isAssignableFrom(propertyValue.getClass())) {
					throw new ContractException(PropertyError.INCOMPATIBLE_VALUE,
							globalPropertyId + " = " + propertyValue);
				}
			}

			/*
			 * Show that each recorded property time corresponds to a known property
			 * definition
			 */
			for (final GlobalPropertyId globalPropertyId : data.globalPropertyTimes.keySet()) {
				if (!data.globalPropertyDefinitions.containsKey(globalPropertyId)) {
					throw new ContractException(PropertyError.UNKNOWN_PROPERTY_ID, globalPropertyId);
				}
			}

			/*
			 * Show that each recorded property time is compatible with the property
			 * definition's time
			 */
			for (final GlobalPropertyId globalPropertyId : data.globalPropertyTimes.keySet()) {
				final Double propertyTime = data.globalPropertyTimes.get(globalPropertyId);
				final Double definitionTime = data.globalPropertyDefinitionTimes.get(globalPropertyId);
				if (propertyTime < definitionTime) {
					throw new ContractException(PropertyError.INCOMPATIBLE_TIME,
							globalPropertyId + " at " + propertyTime);
				}
			}

			/*
			 * For every global property definition that has no default value, ensure that
			 * there is a corresponding global property value assignment
			 */
			for (GlobalPropertyId globalPropertyId : data.globalPropertyDefinitions.keySet()) {
				PropertyDefinition propertyDefinition = data.globalPropertyDefinitions.get(globalPropertyId);
				if (!propertyDefinition.getDefaultValue().isPresent()) {
					Object propertyValue = data.globalPropertyValues.get(globalPropertyId);
					if (propertyValue == null) {
						throw new ContractException(PropertyError.INSUFFICIENT_PROPERTY_VALUE_ASSIGNMENT,
								globalPropertyId);
					}
				}
			}
		}

	}

	private static class Data {

		private final Map<GlobalPropertyId, PropertyDefinition> globalPropertyDefinitions = new LinkedHashMap<>();

		private Map<GlobalPropertyId, Double> globalPropertyDefinitionTimes = new LinkedHashMap<>();

		private Map<GlobalPropertyId, Object> globalPropertyValues = new LinkedHashMap<>();

		private Map<GlobalPropertyId, Double> globalPropertyTimes = new LinkedHashMap<>();

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
		public String toString() {
			StringBuilder builder = new StringBuilder();
			builder.append("Data [globalPropertyDefinitions=");
			builder.append(globalPropertyDefinitions);
			builder.append(", globalPropertyDefinitionTimes=");
			builder.append(globalPropertyDefinitionTimes);
			builder.append(", globalPropertyValues=");
			builder.append(globalPropertyValues);
			builder.append(", globalPropertyTimes=");
			builder.append(globalPropertyTimes);
			builder.append(", locked=");
			builder.append(locked);
			builder.append("]");
			return builder.toString();
		}

		/**
    	 * Standard implementation consistent with the {@link #equals(Object)} method
    	 */
		@Override
		public int hashCode() {
			return Objects.hash(globalPropertyDefinitions, globalPropertyDefinitionTimes, globalPropertyValues,
					globalPropertyTimes);
		}

		/**
    	 * Two {@link Data} instances are equal if and only if
    	 * their inputs are equal.
    	 */
		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			Data other = (Data) obj;
			return Objects.equals(globalPropertyDefinitions, other.globalPropertyDefinitions)
					&& Objects.equals(globalPropertyDefinitionTimes, other.globalPropertyDefinitionTimes)
					&& Objects.equals(globalPropertyValues, other.globalPropertyValues)
					&& Objects.equals(globalPropertyTimes, other.globalPropertyTimes);
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
	 *                           <ul>
	 *                           <li>{@linkplain PropertyError#NULL_PROPERTY_ID} if
	 *                           the global property id is null</li>
	 *                           <li>{@linkplain PropertyError#UNKNOWN_PROPERTY_ID}
	 *                           if the global property id is known</li>
	 *                           </ul>
	 */
	public PropertyDefinition getGlobalPropertyDefinition(final GlobalPropertyId globalPropertyId) {
		validateGlobalPropertyIdExists(data, globalPropertyId);
		return data.globalPropertyDefinitions.get(globalPropertyId);
	}

	/**
	 * Returns the creation time for the given {@link GlobalPropertyId}.
	 * 
	 * @throws ContractException
	 *                           <ul>
	 *                           <li>{@linkplain PropertyError#NULL_PROPERTY_ID} if
	 *                           the global property id is null</li>
	 *                           <li>{@linkplain PropertyError#UNKNOWN_PROPERTY_ID}
	 *                           if the global property id is known</li>
	 *                           </ul>
	 */
	public Double getGlobalPropertyDefinitionTime(final GlobalPropertyId globalPropertyId) {
		validateGlobalPropertyIdExists(data, globalPropertyId);
		return data.globalPropertyDefinitionTimes.get(globalPropertyId);
	}

	/**
	 * Returns the set of {@link GlobalPropertyId}
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
	 * Returns the optional property value for the given property id
	 * 
	 * @throws ContractException
	 *                           <ul>
	 *                           <li>{@linkplain PropertyError#NULL_PROPERTY_ID} if
	 *                           the global property id is null</li>
	 *                           <li>{@linkplain PropertyError#UNKNOWN_PROPERTY_ID}
	 *                           if the global property id is known</li>
	 *                           </ul>
	 */
	@SuppressWarnings("unchecked")
	public <T> Optional<T> getGlobalPropertyValue(final GlobalPropertyId globalPropertyId) {
		validateGlobalPropertyIdExists(data, globalPropertyId);
		T result = (T) data.globalPropertyValues.get(globalPropertyId);
		return Optional.ofNullable(result);
	}

	/**
	 * Returns the optional property assignment time for the given property id
	 * 
	 * @throws ContractException
	 *                           <ul>
	 *                           <li>{@linkplain PropertyError#NULL_PROPERTY_ID} if
	 *                           the global property id is null</li>
	 *                           <li>{@linkplain PropertyError#UNKNOWN_PROPERTY_ID}
	 *                           if the global property id is known</li>
	 *                           </ul>
	 */
	public Optional<Double> getGlobalPropertyTime(final GlobalPropertyId globalPropertyId) {
		validateGlobalPropertyIdExists(data, globalPropertyId);
		Double result = data.globalPropertyTimes.get(globalPropertyId);
		return Optional.ofNullable(result);
	}

	private static void validateGlobalPropertyIdExists(final Data data, final GlobalPropertyId globalPropertyId) {
		if (globalPropertyId == null) {
			throw new ContractException(PropertyError.NULL_PROPERTY_ID);
		}
		if (!data.globalPropertyDefinitions.containsKey(globalPropertyId)) {
			throw new ContractException(PropertyError.UNKNOWN_PROPERTY_ID, globalPropertyId);
		}
	}

	/**
	 * Returns the current version of this Simulation Plugin, which is equal to the
	 * version of the GCM Simulation
	 */
	public String getVersion() {
		return StandardVersioning.VERSION;
	}

	/**
	 * Given a version string, returns whether the version is a supported version or
	 * not.
	 */
	public static boolean checkVersionSupported(String version) {
		return StandardVersioning.checkVersionSupported(version);
	}

	@Override
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
     * Two {@link GlobalPropertiesPluginData} instances are equal if and only if
     * their inputs are equal.
     */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		GlobalPropertiesPluginData other = (GlobalPropertiesPluginData) obj;
		return Objects.equals(data, other.data);
	}

	public Map<GlobalPropertyId, PropertyDefinition> getGlobalPropertyDefinitions() {
		return new LinkedHashMap<>(data.globalPropertyDefinitions);
	}

	public Map<GlobalPropertyId, Double> getGlobalPropertyDefinitionTimes() {
		return new LinkedHashMap<>(data.globalPropertyDefinitionTimes);
	}

	public Map<GlobalPropertyId, Object> getGlobalPropertyValues() {
		return new LinkedHashMap<>(data.globalPropertyValues);
	}

	public Map<GlobalPropertyId, Double> getGlobalPropertyTimes() {
		return new LinkedHashMap<>(data.globalPropertyTimes);
	}

	@Override
	public String toString() {
		StringBuilder builder2 = new StringBuilder();
		builder2.append("GlobalPropertiesPluginData [data=");
		builder2.append(data);
		builder2.append("]");
		return builder2.toString();
	}

}
