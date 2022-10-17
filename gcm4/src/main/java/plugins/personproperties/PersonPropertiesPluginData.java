package plugins.personproperties;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.jcip.annotations.Immutable;
import nucleus.PluginData;
import nucleus.PluginDataBuilder;
import plugins.people.support.PersonError;
import plugins.people.support.PersonId;
import plugins.personproperties.support.PersonPropertyError;
import plugins.personproperties.support.PersonPropertyId;
import plugins.personproperties.support.PersonPropertyInitialization;
import plugins.util.properties.PropertyDefinition;
import plugins.util.properties.PropertyError;
import util.errors.ContractException;

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

		private List<List<PersonPropertyInitialization>> personPropertyValues = new ArrayList<>();

		private List<PersonPropertyInitialization> emptyList = Collections.unmodifiableList(new ArrayList<>());

		private Data() {
		}

		private Data(Data data) {
			personPropertyDefinitions.putAll(data.personPropertyDefinitions);
			for (List<PersonPropertyInitialization> list : data.personPropertyValues) {
				List<PersonPropertyInitialization> newList = new ArrayList<>(list);
				personPropertyValues.add(newList);
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
		private boolean dataIsMutable;

		private void ensureDataMutability() {
			if (!dataIsMutable) {
				data = new Data(data);
				dataIsMutable = true;
			}
		}

		private Builder(Data data) {
			this.data = data;
		}

		/**
		 * Builds the {@linkplain PersonPropertiesPluginData} from the collected
		 * data.
		 * 
		 * @throws ContractException
		 *             <li>{@linkplain PropertyError#UNKNOWN_PROPERTY_ID} if a
		 *             person is assigned a property value for a property that
		 *             was not defined</li>
		 * 
		 *             <li>{@linkplain PropertyError#INCOMPATIBLE_VALUE} if a
		 *             person is assigned a property value that is incompatible
		 *             with the associated property definition</li>
		 * 
		 *             <li>{@linkplain PropertyError#INSUFFICIENT_PROPERTY_VALUE_ASSIGNMENT}
		 *             if a person is not assigned a property value for a
		 *             property id where the associated property definition does
		 *             not contain a default value</li>
		 * 
		 */
		public PersonPropertiesPluginData build() {
			try {
				validateData();
				return new PersonPropertiesPluginData(data);
			} finally {
				data = new Data();
			}
		}

		/**
		 * Defines a person property definition
		 * 
		 * @throws ContractException
		 *             <li>{@linkplain PropertyError#NULL_PROPERTY_ID} if the
		 *             person property id is null</li>
		 *             <li>{@linkplain PropertyError#NULL_PROPERTY_DEFINITION}
		 *             if the person property definition value is null</li>
		 *             <li>{@linkplain PropertyError#DUPLICATE_PROPERTY_DEFINITION}
		 *             if the person property definition is already added</li>
		 * 
		 * 
		 * 
		 */
		public Builder definePersonProperty(final PersonPropertyId personPropertyId, final PropertyDefinition propertyDefinition) {
			ensureDataMutability();
			validatePersonPropertyIdNotNull(personPropertyId);
			validatePersonPropertyDefinitionNotNull(propertyDefinition);
			validatePersonPropertyIsNotDefined(data, personPropertyId);
			data.personPropertyDefinitions.put(personPropertyId, propertyDefinition);
			return this;
		}

		/**
		 * Sets the person's property value
		 * 
		 * @throws ContractException
		 *             <li>{@linkplain PersonError#NULL_PERSON_ID} if the person
		 *             id is null</li>
		 *             <li>{@linkplain PropertyError#NULL_PROPERTY_ID} if the
		 *             person property id is null</li>
		 *             <li>{@linkplain PersonPropertyError#NULL_PERSON_PROPERTY_VALUE}
		 *             if the person property value is null</li>
		 */
		public Builder setPersonPropertyValue(final PersonId personId, final PersonPropertyId personPropertyId, final Object personPropertyValue) {
			ensureDataMutability();
			validatePersonId(personId);
			validatePersonPropertyIdNotNull(personPropertyId);
			validatePersonPropertyValueNotNull(personPropertyValue);

			int personIndex = personId.getValue();
			while (data.personPropertyValues.size() <= personIndex) {
				data.personPropertyValues.add(null);
			}
			List<PersonPropertyInitialization> list = data.personPropertyValues.get(personIndex);
			if (list == null) {
				list = new ArrayList<>();
				data.personPropertyValues.set(personIndex, list);
			}
			PersonPropertyInitialization personPropertyInitialization = new PersonPropertyInitialization(personPropertyId, personPropertyValue);
			list.add(personPropertyInitialization);

			return this;
		}

		private void validateData() {
			if(!dataIsMutable) {
				return;
			}

			for (List<PersonPropertyInitialization> list : data.personPropertyValues) {
				if (list != null) {
					for (PersonPropertyInitialization personPropertyInitialization : list) {
						PersonPropertyId personPropertyId = personPropertyInitialization.getPersonPropertyId();
						PropertyDefinition propertyDefinition = data.personPropertyDefinitions.get(personPropertyId);
						if (propertyDefinition == null) {
							throw new ContractException(PropertyError.UNKNOWN_PROPERTY_ID, personPropertyId);
						}
						Object propertyValue = personPropertyInitialization.getValue();
						if (!propertyDefinition.getType().isAssignableFrom(propertyValue.getClass())) {
							throw new ContractException(PropertyError.INCOMPATIBLE_VALUE, personPropertyId + " = " + propertyValue);
						}
					}
				}
			}

			Map<PersonPropertyId, Integer> nonDefaultBearingPropertyIds = new LinkedHashMap<>();

			for (PersonPropertyId personPropertyId : data.personPropertyDefinitions.keySet()) {
				PropertyDefinition propertyDefinition = data.personPropertyDefinitions.get(personPropertyId);
				if (propertyDefinition.getDefaultValue().isEmpty()) {
					nonDefaultBearingPropertyIds.put(personPropertyId, nonDefaultBearingPropertyIds.size());
				}
			}
			if (nonDefaultBearingPropertyIds.isEmpty()) {
				return;
			}

			boolean[] nonDefaultChecks = new boolean[nonDefaultBearingPropertyIds.size()];

			for (int i = 0; i < data.personPropertyValues.size(); i++) {
				List<PersonPropertyInitialization> list = data.personPropertyValues.get(i);
				for (int j = 0; j < nonDefaultChecks.length; j++) {
					nonDefaultChecks[j] = false;
				}

				if (list != null) {
					for (PersonPropertyInitialization personPropertyInitialization : list) {
						PersonPropertyId personPropertyId = personPropertyInitialization.getPersonPropertyId();
						Integer index = nonDefaultBearingPropertyIds.get(personPropertyId);
						if (index != null) {
							nonDefaultChecks[index] = true;
						}
					}
				}

				boolean missingPropertyAssignments = false;
				for (int j = 0; j < nonDefaultChecks.length; j++) {
					if (!nonDefaultChecks[j]) {
						missingPropertyAssignments = true;
						break;
					}
				}

				if (missingPropertyAssignments) {
					StringBuilder sb = new StringBuilder();
					int index = -1;
					boolean firstMember = true;
					for (PersonPropertyId personPropertyId : nonDefaultBearingPropertyIds.keySet()) {
						index++;
						if (!nonDefaultChecks[index]) {
							if (firstMember) {
								firstMember = false;
							} else {
								sb.append(", ");
							}
							sb.append(personPropertyId);
						}
					}
					throw new ContractException(PropertyError.INSUFFICIENT_PROPERTY_VALUE_ASSIGNMENT, "person " + i + " is missing values for " + sb.toString());
				}

			}
		}
	}

	private static void validatePersonPropertyIsNotDefined(final Data data, final PersonPropertyId personPropertyId) {
		final PropertyDefinition propertyDefinition = data.personPropertyDefinitions.get(personPropertyId);
		if (propertyDefinition != null) {
			throw new ContractException(PropertyError.DUPLICATE_PROPERTY_DEFINITION, personPropertyId);
		}
	}

	private static void validatePersonPropertyDefinitionNotNull(PropertyDefinition propertyDefinition) {
		if (propertyDefinition == null) {
			throw new ContractException(PropertyError.NULL_PROPERTY_DEFINITION);
		}
	}

	private static void validatePersonPropertyIdNotNull(PersonPropertyId personPropertyId) {
		if (personPropertyId == null) {
			throw new ContractException(PropertyError.NULL_PROPERTY_ID);
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
	 *             <li>{@linkplain PropertyError#NULL_PROPERTY_ID} if the person
	 *             property id is null</li>
	 *             <li>{@linkplain PropertyError#UNKNOWN_PROPERTY_ID} if the
	 *             person property id is unknown</li>
	 * 
	 */
	public PropertyDefinition getPersonPropertyDefinition(final PersonPropertyId personPropertyId) {
		validatePersonPropertyIdNotNull(personPropertyId);
		final PropertyDefinition propertyDefinition = data.personPropertyDefinitions.get(personPropertyId);
		if (propertyDefinition == null) {
			throw new ContractException(PropertyError.UNKNOWN_PROPERTY_ID, personPropertyId);
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

		return new Builder(data);
	}

	private static void validatePersonId(PersonId personId) {
		if (personId == null) {
			throw new ContractException(PersonError.NULL_PERSON_ID);
		}
	}

	private static void validatePersonPropertyValueNotNull(Object personPropertyValue) {
		if (personPropertyValue == null) {
			throw new ContractException(PropertyError.NULL_PROPERTY_VALUE);
		}
	}

	/**
	 * Returns the set of {@link PersonId} ids collected by the builder
	 */
	public int getPersonCount() {
		return data.personPropertyValues.size();
	}

	/**
	 * Returns the property values for the given {@link PersonId}
	 *
	 *
	 */
	public List<PersonPropertyInitialization> getPropertyValues(int personIndex) {
		if (personIndex < 0) {
			return data.emptyList;
		}
		if (personIndex >= data.personPropertyValues.size()) {
			return data.emptyList;
		}
		List<PersonPropertyInitialization> list = data.personPropertyValues.get(personIndex);
		if (list == null) {
			return data.emptyList;
		}
		return Collections.unmodifiableList(list);
	}

}
