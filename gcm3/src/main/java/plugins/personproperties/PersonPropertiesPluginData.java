package plugins.personproperties;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import net.jcip.annotations.Immutable;
import nucleus.PluginData;
import nucleus.PluginDataBuilder;
import nucleus.util.ContractException;
import plugins.people.support.PersonError;
import plugins.people.support.PersonId;
import plugins.personproperties.support.PersonPropertyError;
import plugins.personproperties.support.PersonPropertyId;
import plugins.util.properties.PropertyDefinition;
import plugins.util.properties.PropertyError;

/**
 * An immutable container of the initial state of person properties. Contains:
 * <BR>
 * <ul>
 * <li>person property ids</li>
 * <li>person property definitions</li>
 * </ul>
 * 
 * @author Shawn Hatch
 *
 */
@Immutable
public class PersonPropertiesPluginData implements PluginData {

	private static class Data {
		private Map<PersonPropertyId, PropertyDefinition> personPropertyDefinitions = new LinkedHashMap<>();

		private Map<PersonId, Map<PersonPropertyId, Object>> personPropertyValues = new LinkedHashMap<>();
		
		private Data() {
		}

		private Data(Data data) {
			personPropertyDefinitions.putAll(data.personPropertyDefinitions);
			for(PersonId personId : data.personPropertyValues.keySet()) {
				Map<PersonPropertyId, Object> map = data.personPropertyValues.get(personId);
				Map<PersonPropertyId, Object> newMap = new LinkedHashMap<>(map);
				personPropertyValues.put(personId, newMap);
			}
		}
	}

	/**
	 * Returns a new builder instance
	 */
	public static Builder builder() {
		return new Builder(new Data());
	}

	/**
	 * Builder class for PersonPropertyInitialData
	 * 
	 * @author Shawn Hatch
	 *
	 */
	public static class Builder implements PluginDataBuilder {
		private Data data;

		private Builder(Data data) {
			this.data = data;
		}

		/**
		 * Builds the {@linkplain PersonPropertiesPluginData} from the collected
		 * data.
		 * 
		 */
		public PersonPropertiesPluginData build() {
			try {
				validateData(data);
				return new PersonPropertiesPluginData(data);
			} finally {
				data = new Data();
			}
		}

		/**
		 * Defines a person property definition
		 * 
		 * @throws ContractException
		 *             <li>{@linkplain PersonPropertyError#NULL_PERSON_PROPERTY_ID}
		 *             if the person property id is null</li>
		 *             <li>{@linkplain PersonPropertyError#NULL_PERSON_PROPERTY_DEFINITION}
		 *             if the person property definition value is null</li>
		 *             <li>{@linkplain PersonPropertyError#DUPLICATE_PERSON_PROPERTY_DEFINITION}
		 *             if the person property definition is already added</li>
		 *             <li>{@linkplain PersonPropertyError#PROPERTY_DEFINITION_REQUIRES_DEFAULT}
		 *             if the person property definition does not have a default
		 *             value</li>
		 * 
		 * 
		 */
		public Builder definePersonProperty(final PersonPropertyId personPropertyId, final PropertyDefinition propertyDefinition) {
			validatePersonPropertyIdNotNull(personPropertyId);
			validatePersonPropertyDefinitionNotNull(propertyDefinition);
			validatePersonPropertyIsNotDefined(data, personPropertyId);
			validatePersonPropertyDefinitionHasDefault(propertyDefinition);
			data.personPropertyDefinitions.put(personPropertyId, propertyDefinition);
			return this;
		}
		/**
		 * Sets the person's property value
		 * 
		 * @throws ContractException
		 *             <li>{@linkplain PersonError#NULL_PERSON_ID} if the person
		 *             id is null</li>
		 *             <li>{@linkplain PersonPropertyError#NULL_PERSON_PROPERTY_ID}
		 *             if the person property id is null</li>
		 *             <li>{@linkplain PersonPropertyError#NULL_PERSON_PROPERTY_VALUE}
		 *             if the person property value is null</li>
		 *             <li>{@linkplain PersonPropertyError#DUPLICATE_PERSON_PROPERTY_VALUE_ASSIGNMENT}
		 *             if the person property value is already assigned</li>
		 */
		public Builder setPersonPropertyValue(final PersonId personId, final PersonPropertyId personPropertyId, final Object personPropertyValue) {
			validatePersonIdNotNull(personId);
			validatePersonPropertyIdNotNull(personPropertyId);
			validatePersonPropertyValueNotNull(personPropertyValue);
			validatePersonPropertyNotAssigned(data, personId, personPropertyId);
			Map<PersonPropertyId, Object> map = data.personPropertyValues.get(personId);
			if (map == null) {
				map = new LinkedHashMap<>();
				data.personPropertyValues.put(personId, map);
			}
			map.put(personPropertyId, personPropertyValue);
			return this;
		}
	}

	private static void validatePersonPropertyIsNotDefined(final Data data, final PersonPropertyId personPropertyId) {
		final PropertyDefinition propertyDefinition = data.personPropertyDefinitions.get(personPropertyId);
		if (propertyDefinition != null) {
			throw new ContractException(PersonPropertyError.DUPLICATE_PERSON_PROPERTY_DEFINITION, personPropertyId);
		}
	}

	private static void validatePersonPropertyDefinitionNotNull(PropertyDefinition propertyDefinition) {
		if (propertyDefinition == null) {
			throw new ContractException(PersonPropertyError.NULL_PERSON_PROPERTY_DEFINITION);
		}
	}

	private static void validatePersonPropertyDefinitionHasDefault(PropertyDefinition propertyDefinition) {
		if (!propertyDefinition.getDefaultValue().isPresent()) {
			throw new ContractException(PersonPropertyError.PROPERTY_DEFINITION_REQUIRES_DEFAULT);
		}
	}

	private static void validatePersonPropertyIdNotNull(PersonPropertyId personPropertyId) {
		if (personPropertyId == null) {
			throw new ContractException(PersonPropertyError.NULL_PERSON_PROPERTY_ID);
		}
	}

	private final Data data;

	private PersonPropertiesPluginData(Data data) {
		this.data = data;
	}

	/**
	 * Returns the {@link PropertyDefinition} for the given
	 * {@link PersonPropertyId}
	 * 
	 * @throws ContractException
	 * 
	 *             <li>{@linkplain PersonPropertyError#NULL_PERSON_PROPERTY_ID}
	 *             if the person property id is null</li>
	 *             <li>{@linkplain PersonPropertyError#UNKNOWN_PERSON_PROPERTY_ID}
	 *             if the person property id is unknown</li>
	 * 
	 */
	public PropertyDefinition getPersonPropertyDefinition(final PersonPropertyId personPropertyId) {
		validatePersonPropertyIdNotNull(personPropertyId);
		final PropertyDefinition propertyDefinition = data.personPropertyDefinitions.get(personPropertyId);
		if (propertyDefinition == null) {
			throw new ContractException(PersonPropertyError.UNKNOWN_PERSON_PROPERTY_ID, personPropertyId);
		}
		return propertyDefinition;
	}

	/**
	 * Returns the set of {@link PersonPropertyId} ids
	 * 
	 */
	@SuppressWarnings("unchecked")
	public <T extends PersonPropertyId> Set<T> getPersonPropertyIds() {
		Set<T> result = new LinkedHashSet<>();
		for (PersonPropertyId personPropertyId : data.personPropertyDefinitions.keySet()) {
			result.add((T) personPropertyId);
		}
		return result;
	}

	@Override
	public PluginDataBuilder getCloneBuilder() {

		return new Builder(new Data(data));
	}
	
	private static void validatePersonIdNotNull(PersonId personId) {
		if (personId == null) {
			throw new ContractException(PersonError.NULL_PERSON_ID);
		}
	}
	
	private static void validatePersonPropertyValueNotNull(Object personPropertyValue) {
		if (personPropertyValue == null) {
			throw new ContractException(PersonPropertyError.NULL_PERSON_PROPERTY_VALUE);
		}
	}
	
	private static void validatePersonPropertyNotAssigned(final Data data, final PersonId personId, final PersonPropertyId personPropertyId) {
		final Map<PersonPropertyId, Object> propertyMap = data.personPropertyValues.get(personId);
		if (propertyMap != null) {
			if (propertyMap.containsKey(personPropertyId)) {
				throw new ContractException(PersonPropertyError.DUPLICATE_PERSON_PROPERTY_VALUE_ASSIGNMENT, personPropertyId + " = " + personId);
			}
		}
	}
	
	/**
	 * Returns the set of {@link PersonId} ids collected by the builder
	 */
	public Set<PersonId> getPersonIds() {
		return new LinkedHashSet<>(data.personPropertyValues.keySet());
	}
	
	/**
	 * Returns the property value for the given {@link PersonId} and
	 * {@link PersonPropertyId}.
	 * 
	 * @throws ContractException
	 * 
	 *             <li>{@linkplain PersonError#NULL_PERSON_ID}</li> if the
	 *             person id is null             
	 *             <li>{@linkplain PersonPropertyError#NULL_PERSON_PROPERTY_ID}
	 *             </li> if the person property id is null
	 *             <li>{@linkplain PersonPropertyError#UNKNOWN_PERSON_PROPERTY_ID}
	 *             </li> if the person property id is known
	 */
	@SuppressWarnings("unchecked")
	public <T> T getPersonPropertyValue(final PersonId personId, final PersonPropertyId personPropertyId) {
		validatePersonIdNotNull(personId);
		validatePersonPropertyIdNotNull(personPropertyId);
		validatePersonPropertyDefinitionIsDefined(data,personPropertyId);		
		Object result = null;
		final Map<PersonPropertyId, Object> map = data.personPropertyValues.get(personId);
		if (map != null) {
			result = map.get(personPropertyId);
		}
		if (result == null) {
			final PropertyDefinition propertyDefinition = data.personPropertyDefinitions.get(personPropertyId);
			result = propertyDefinition.getDefaultValue().get();
		}
		return (T) result;
	}
	
	private static void validatePersonPropertyDefinitionIsDefined(Data data, PersonPropertyId personPropertyId) {
		if (!data.personPropertyDefinitions.containsKey(personPropertyId)) {
			throw new ContractException(PersonPropertyError.UNKNOWN_PERSON_PROPERTY_ID, personPropertyId);
		}
	}
	
	private static void validateData(Data data) {

		for (PersonId personId : data.personPropertyValues.keySet()) {
			Map<PersonPropertyId, Object> map = data.personPropertyValues.get(personId);
			for (PersonPropertyId personPropertyId : map.keySet()) {
				PropertyDefinition propertyDefinition = data.personPropertyDefinitions.get(personPropertyId);
				if (propertyDefinition == null) {
					throw new ContractException(PersonPropertyError.UNKNOWN_PERSON_PROPERTY_ID, personPropertyId);
				}
				Object propertyValue = map.get(personPropertyId);
				if (!propertyDefinition.getType().isAssignableFrom(propertyValue.getClass())) {
					throw new ContractException(PropertyError.INCOMPATIBLE_VALUE, personPropertyId + " = " + propertyValue);
				}
			}
		}

	}
	
}
