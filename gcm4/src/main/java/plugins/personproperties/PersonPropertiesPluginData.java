package plugins.personproperties;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.apache.commons.math3.util.FastMath;

import net.jcip.annotations.Immutable;
import nucleus.PluginData;
import nucleus.PluginDataBuilder;
import plugins.people.support.PersonError;
import plugins.people.support.PersonId;
import plugins.personproperties.support.PersonPropertyError;
import plugins.personproperties.support.PersonPropertyId;
import plugins.personproperties.support.PersonPropertyValueInitialization;
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
 *
 */
@Immutable
public class PersonPropertiesPluginData implements PluginData {

	private static class Data {

		private Map<PersonPropertyId, PropertyDefinition> personPropertyDefinitions = new LinkedHashMap<>();
		
		private Map<PersonPropertyId, Double> personPropertyDefinitionTimes = new LinkedHashMap<>();
		
		private Map<PersonPropertyId,List<Object>> personPropertyValues = new LinkedHashMap<>();
		
		private Map<PersonPropertyId,List<Double>> personPropertyTimes = new LinkedHashMap<>();

		private boolean locked;

		private Data() {
		}

		private Data(Data data) {

			personPropertyDefinitions.putAll(data.personPropertyDefinitions);
			
			for (List<PersonPropertyValueInitialization> list : data.personPropertyValues) {
				List<PersonPropertyValueInitialization> newList = new ArrayList<>(list);
				personPropertyValues.add(newList);
			}
			locked = data.locked;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + personPropertyDefinitions.hashCode();
			result = prime * result + getPersonPropertyValuesHashCode();
			return result;
		}

		private int getPersonPropertyValuesHashCode() {
			int result = 0;
			int prime = 31;

			for (int i = 0; i < personPropertyValues.size(); i++) {
				List<PersonPropertyValueInitialization> list = personPropertyValues.get(i);
				if (list != null) {
					for (PersonPropertyValueInitialization personPropertyValueInitialization : list) {
						PersonPropertyId personPropertyId = personPropertyValueInitialization.getPersonPropertyId();
						PropertyDefinition propertyDefinition = personPropertyDefinitions.get(personPropertyId);
						boolean use = true;
						Object propertyValue = personPropertyValueInitialization.getValue();
						Optional<Object> optional = propertyDefinition.getDefaultValue();
						if (optional.isPresent()) {
							Object defaultValue = optional.get();
							if (defaultValue.equals(propertyValue)) {
								use = false;
							}
						}
						if (use) {
							int subResult = 1;
							subResult = subResult * prime + i;
							subResult = subResult * prime + personPropertyId.hashCode();
							subResult = subResult * prime + propertyValue.hashCode();
							result += subResult;
						}
					}
				}
			}

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
			
			if (!personPropertyDefinitions.equals(other.personPropertyDefinitions)) {
				return false;
			}
			if (!comparePersonPropertyValues(this, other)) {
				return false;
			}
			return true;
		}

	}

	private static boolean comparePersonPropertyValues(Data a, Data b) {
		int n = FastMath.max(a.personPropertyValues.size(), b.personPropertyValues.size());

		for (int i = 0; i < n; i++) {
			Set<PersonPropertyValueInitialization> aSet = getNonDefaultPersonPropertyInitializations(a, i);
			Set<PersonPropertyValueInitialization> bSet = getNonDefaultPersonPropertyInitializations(b, i);
			if (!aSet.equals(bSet)) {
				return false;
			}
		}
		return true;
	}

	private static Set<PersonPropertyValueInitialization> getNonDefaultPersonPropertyInitializations(Data data, int personIndex) {
		Set<PersonPropertyValueInitialization> result = new LinkedHashSet<>();
		if (personIndex < data.personPropertyValues.size()) {
			List<PersonPropertyValueInitialization> list = data.personPropertyValues.get(personIndex);
			if (list != null) {
				for (PersonPropertyValueInitialization personPropertyValueInitialization : list) {
					PersonPropertyId personPropertyId = personPropertyValueInitialization.getPersonPropertyId();
					PropertyDefinition propertyDefinition = data.personPropertyDefinitions.get(personPropertyId);
					boolean use = true;
					Object propertyValue = personPropertyValueInitialization.getValue();
					Optional<Object> optional = propertyDefinition.getDefaultValue();
					if (optional.isPresent()) {
						Object defaultValue = optional.get();
						if (defaultValue.equals(propertyValue)) {
							use = false;
						}
					}
					if (use) {
						result.add(personPropertyValueInitialization);
					}
				}
			}
		}
		return result;
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
	 *
	 */
	public static class Builder implements PluginDataBuilder {
		private Data data;

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
		 *             
		 * 
		 * 
		 * 
		 * 
		 */
		public PersonPropertiesPluginData build() {

			if (!data.locked) {
				validateData();
			}
			ensureImmutability();
			return new PersonPropertiesPluginData(data);

		}

		/**
		 * Defines a person property definition. Duplicate inputs override
		 * previous inputs.
		 * 
		 * @throws ContractException
		 *             <li>{@linkplain PropertyError#NULL_PROPERTY_ID} if the
		 *             person property id is null</li>
		 *             <li>{@linkplain PropertyError#NULL_PROPERTY_DEFINITION}
		 *             if the person property definition value is null</li>
		 * 
		 */
		public Builder definePersonProperty(final PersonPropertyId personPropertyId, final PropertyDefinition propertyDefinition) {
			ensureDataMutability();
			validatePersonPropertyIdNotNull(personPropertyId);
			validatePersonPropertyDefinitionNotNull(propertyDefinition);
			data.personPropertyDefinitions.put(personPropertyId, propertyDefinition);
			return this;
		}


		/**
		 * Sets the person's property value. Duplicate inputs override previous
		 * inputs.
		 * 
		 * @throws ContractException
		 *             <li>{@linkplain PersonError#NULL_PERSON_ID} if the person
		 *             id is null</li>
		 *             <li>{@linkplain PropertyError#NULL_PROPERTY_ID} if the
		 *             person property id is null</li>
		 *             <li>{@linkplain PersonPropertyError#NULL_PROPERTY_VALUE}
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

			List<PersonPropertyValueInitialization> list = data.personPropertyValues.get(personIndex);
			PersonPropertyValueInitialization personPropertyValueInitialization = new PersonPropertyValueInitialization(personPropertyId, personPropertyValue);

			if (list == null) {
				list = new ArrayList<>();
				data.personPropertyValues.set(personIndex, list);
			}

			int index = -1;

			for (int i = 0; i < list.size(); i++) {
				if (list.get(i).getPersonPropertyId().equals(personPropertyId)) {
					index = i;
					break;
				}
			}

			if (index == -1) {
				list.add(personPropertyValueInitialization);
			} else {
				list.set(index, personPropertyValueInitialization);
			}

			return this;
		}

		private void validateData() {

			for (List<PersonPropertyValueInitialization> list : data.personPropertyValues) {
				if (list != null) {
					for (PersonPropertyValueInitialization personPropertyValueInitialization : list) {
						PersonPropertyId personPropertyId = personPropertyValueInitialization.getPersonPropertyId();
						PropertyDefinition propertyDefinition = data.personPropertyDefinitions.get(personPropertyId);
						if (propertyDefinition == null) {
							throw new ContractException(PropertyError.UNKNOWN_PROPERTY_ID, personPropertyId);
						}
						Object propertyValue = personPropertyValueInitialization.getValue();
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
				List<PersonPropertyValueInitialization> list = data.personPropertyValues.get(i);

				for (int j = 0; j < nonDefaultChecks.length; j++) {
					nonDefaultChecks[j] = false;
				}

				if (list != null) {
					for (PersonPropertyValueInitialization personPropertyValueInitialization : list) {
						PersonPropertyId personPropertyId = personPropertyValueInitialization.getPersonPropertyId();
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
	 * Returns the number of people 
	 */
	public int getPersonCount() {
		return data.personPropertyValues.size();
	}

	/**
	 * Returns the property values for the given {@link PersonId} as an
	 * unmodifiable list.
	 *
	 */
	public List<PersonPropertyValueInitialization> getPropertyValues(int personIndex) {
		
		if (personIndex<0 || personIndex >= data.personPropertyValues.size()) {
			return data.emptyList;
		}
		List<PersonPropertyValueInitialization> list = data.personPropertyValues.get(personIndex);
		if (list == null) {
			return data.emptyList;
		}
		return Collections.unmodifiableList(list);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + data.hashCode();
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}

		if (!(obj instanceof PersonPropertiesPluginData)) {
			return false;
		}

		PersonPropertiesPluginData other = (PersonPropertiesPluginData) obj;

		if (!data.equals(other.data)) {
			return false;
		}
		return true;
	}

}
