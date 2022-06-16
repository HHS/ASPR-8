package plugins.globalproperties;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

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
 * @author Shawn Hatch
 *
 */
@Immutable
public final class GlobalPropertiesPluginData implements PluginData {

	private static void validateGlobalPropertyIsNotDefined(final Data data, final GlobalPropertyId globalPropertyId) {
		final PropertyDefinition propertyDefinition = data.globalPropertyDefinitions.get(globalPropertyId);
		if (propertyDefinition != null) {
			throw new ContractException(PropertyError.DUPLICATE_PROPERTY_DEFINITION, globalPropertyId);
		}
	}

	private static void validateGlobalPropertyValueNotAssigned(final Data data, final GlobalPropertyId globalPropertyId) {
		if (data.globalPropertyValues.containsKey(globalPropertyId)) {
			throw new ContractException(PropertyError.DUPLICATE_PROPERTY_VALUE_ASSIGNMENT, globalPropertyId);
		}
	}

	/**
	 * Builder class for GloblaInitialData
	 * 
	 * @author Shawn Hatch
	 *
	 */
	public static class Builder implements PluginDataBuilder {
		private Data data;
		private boolean dataIsMutable;

		private Builder(Data data) {
			this.data = data;
		}

		/**
		 * Returns the {@link GlobalInitialData} from the collected information
		 * supplied to this builder. Clears the builder's state.
		 * 
		 * @throws ContractException
		 *             <li>{@linkplain PropertyError.UNKNOWN_PROPERTY_ID}</li>
		 *             if a global property value was associated with a global
		 *             property id that was not defined
		 * 
		 *             <li>{@linkplain PropertyError#INCOMPATIBLE_VALUE}</li> if a
		 *             global property value was associated with a global
		 *             property id that is incompatible with the corresponding
		 *             property definition.
		 * 
		 *             <li>{@linkplain PropertyError#PROPERTY_DEFINITION_MISSING_DEFAULT}</li>
		 *             if a global property definition does not have a default
		 *             value and there are no property values added to replace
		 *             that default.
		 */
		public GlobalPropertiesPluginData build() {
			try {
				validateData();
				return new GlobalPropertiesPluginData(data);
			} finally {
				data = new Data();
			}
		}

		/**
		 * Defines a global property
		 * 
		 * @throws ContractException
		 * 
		 * 
		 *             <li>{@linkplain PropertyError#NULL_PROPERTY_ID}</li>
		 *             if the global property id is null
		 * 
		 *             <li>{@linkplain PropertyError#NULL_PROPERTY_DEFINITION}
		 *             </li> if the property definition is null
		 *
		 *             <li>{@linkplain PropertyError#DUPLICATE_PROPERTY_DEFINITION}
		 *             </li> if a property definition for the given global
		 *             property id was previously defined.
		 * 
		 */
		public Builder defineGlobalProperty(final GlobalPropertyId globalPropertyId, final PropertyDefinition propertyDefinition) {
			ensureDataMutability();
			validateGlobalPropertyIdNotNull(globalPropertyId);
			validateGlobalPropertyDefinitionNotNull(propertyDefinition);
			validateGlobalPropertyIsNotDefined(data, globalPropertyId);
			data.globalPropertyDefinitions.put(globalPropertyId, propertyDefinition);
			return this;
		}
		
		private void ensureDataMutability() {
			if(!dataIsMutable) {
				data = new Data(data);
				dataIsMutable = true;
			}
		}

		/**
		 * Sets the global property value that overrides the default value of
		 * the corresponding property definition
		 * 
		 * @throws ContractException
		 * 
		 *             <li>{@linkplain PropertyError#NULL_PROPERTY_ID}
		 *             </li>if the global property id is null
		 * 
		 *             <li>{@linkplain PropertyError#NULL_PROPERTY_VALUE}
		 *             </li>if the global property value is null
		 * 
		 *             <li>{@linkplain PropertyError#DUPLICATE_PROPERTY_VALUE_ASSIGNMENT}
		 *             </li>if the global property value was previously defined
		 *             for the given global property id
		 * 
		 */
		public Builder setGlobalPropertyValue(final GlobalPropertyId globalPropertyId, final Object propertyValue) {
			ensureDataMutability();
			validateGlobalPropertyIdNotNull(globalPropertyId);
			validateGlobalPropertyValueNotNull(propertyValue);
			validateGlobalPropertyValueNotAssigned(data, globalPropertyId);
			data.globalPropertyValues.put(globalPropertyId, propertyValue);
			return this;
		}

		private void validateData() {
			if(!dataIsMutable) {
				return;
			}
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
			 * For every global property definition that has a null default value,
			 * ensure that there is a corresponding global property value assignment
			 * and put that initial assignment on the property definition and repair
			 * the definition.
			 */
			for (GlobalPropertyId globalPropertyId : data.globalPropertyDefinitions.keySet()) {
				PropertyDefinition propertyDefinition = data.globalPropertyDefinitions.get(globalPropertyId);
				if (!propertyDefinition.getDefaultValue().isPresent()) {
					Object propertyValue = data.globalPropertyValues.get(globalPropertyId);
					if (propertyValue == null) {
						throw new ContractException(PropertyError.PROPERTY_DEFINITION_MISSING_DEFAULT, globalPropertyId);
					}
					propertyDefinition = //
							PropertyDefinition	.builder()//
												.setPropertyValueMutability(propertyDefinition.propertyValuesAreMutable())//
												.setDefaultValue(propertyValue)//
												.setTimeTrackingPolicy(propertyDefinition.getTimeTrackingPolicy())//
												.setType(propertyDefinition.getType())//
												.build();//
					data.globalPropertyDefinitions.put(globalPropertyId, propertyDefinition);
				}
			}
		}

	}

	private static class Data {

		private final Map<GlobalPropertyId, PropertyDefinition> globalPropertyDefinitions = new LinkedHashMap<>();

		private final Map<GlobalPropertyId, Object> globalPropertyValues = new LinkedHashMap<>();

		private Data() {
		}

		private Data(Data data) {
			globalPropertyDefinitions.putAll(data.globalPropertyDefinitions);
			globalPropertyValues.putAll(globalPropertyValues);
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
	 *             <li>{@linkplain PropertyError#NULL_PROPERTY_ID}</li> if
	 *             the global property id is null
	 *             <li>{@linkplain PropertyError#UNKNOWN_PROPERTY_ID}</li>
	 *             if the global property id is known
	 */
	public PropertyDefinition getGlobalPropertyDefinition(final GlobalPropertyId globalPropertyId) {
		validategGlobalPropertyIdExists(data, globalPropertyId);
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
	 *             <li>{@linkplain PropertyError#NULL_PROPERTY_ID}</li> if
	 *             the global property id is null
	 *             <li>{@linkplain PropertyError#UNKNOWN_PROPERTY_ID}</li>
	 *             if the global property id is known
	 */
	@SuppressWarnings("unchecked")
	public <T> T getGlobalPropertyValue(final GlobalPropertyId globalPropertyId) {
		validategGlobalPropertyIdExists(data, globalPropertyId);
		T result = (T) data.globalPropertyValues.get(globalPropertyId);
		if(result == null) {
			result = (T) data.globalPropertyDefinitions.get(globalPropertyId).getDefaultValue().get();			
		}
		return result;
	}

	private static void validategGlobalPropertyIdExists(final Data data, final GlobalPropertyId globalPropertyId) {
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

}
