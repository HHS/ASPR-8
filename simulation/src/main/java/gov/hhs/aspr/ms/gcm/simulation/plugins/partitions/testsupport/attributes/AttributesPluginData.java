package gov.hhs.aspr.ms.gcm.simulation.plugins.partitions.testsupport.attributes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import gov.hhs.aspr.ms.gcm.simulation.nucleus.PluginData;
import gov.hhs.aspr.ms.gcm.simulation.nucleus.PluginDataBuilder;
import gov.hhs.aspr.ms.gcm.simulation.plugins.partitions.testsupport.attributes.support.AttributeDefinition;
import gov.hhs.aspr.ms.gcm.simulation.plugins.partitions.testsupport.attributes.support.AttributeError;
import gov.hhs.aspr.ms.gcm.simulation.plugins.partitions.testsupport.attributes.support.AttributeId;
import gov.hhs.aspr.ms.gcm.simulation.plugins.people.support.PersonError;
import gov.hhs.aspr.ms.gcm.simulation.plugins.people.support.PersonId;
import gov.hhs.aspr.ms.util.errors.ContractException;
import net.jcip.annotations.Immutable;

@Immutable
public class AttributesPluginData implements PluginData {

	private static class Data {
		private Map<AttributeId, AttributeDefinition> attributeDefinitions = new LinkedHashMap<>();
		private Map<AttributeId, List<Object>> personAttributeValues = new LinkedHashMap<>();
		private List<Object> emptyValueList = Collections.unmodifiableList(new ArrayList<>());
		private boolean locked;

		private Data() {
		}

		private Data(Data data) {
			locked = data.locked;
			attributeDefinitions.putAll(data.attributeDefinitions);
			for (AttributeId attributeId : data.personAttributeValues.keySet()) {
				List<Object> list = data.personAttributeValues.get(attributeId);
				List<Object> newList = new ArrayList<>(list);
				personAttributeValues.put(attributeId, newList);
			}
		}

		/**
    	 * Standard implementation consistent with the {@link #equals(Object)} method
    	 */
		@Override
		public int hashCode() {
			return Objects.hash(attributeDefinitions, personAttributeValues);
		}

		/**
    	 * Two {@link Data} instances are equal if and only if
    	 * their inputs are equal.
    	 */
		@Override
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			}
			if (obj == null) {
				return false;
			}
			if (getClass() != obj.getClass()) {
				return false;
			}
			Data other = (Data) obj;
			return Objects.equals(attributeDefinitions, other.attributeDefinitions)
					&& Objects.equals(personAttributeValues, other.personAttributeValues);
		}

		@Override
		public String toString() {
			StringBuilder builder = new StringBuilder();
			builder.append("Data [attributeDefinitions=");
			builder.append(attributeDefinitions);
			builder.append(", personAttributeValues=");
			builder.append(personAttributeValues);
			builder.append("]");
			return builder.toString();
		}

	}

	public static Builder builder() {
		return new Builder(new Data());
	}

	public static class Builder implements PluginDataBuilder {
		private Data data;

		private Builder(Data data) {
			this.data = data;
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

		private void validateData() {

			// show all property ids agree with the definitions
			for (AttributeId attributeId : data.personAttributeValues.keySet()) {
				if (!data.attributeDefinitions.keySet().contains(attributeId)) {
					throw new ContractException(AttributeError.UNKNOWN_ATTRIBUTE_ID, attributeId);
				}
			}

			// add lists where needed
			for (AttributeId attributeId : data.attributeDefinitions.keySet()) {
				if (!data.personAttributeValues.keySet().contains(attributeId)) {
					data.personAttributeValues.put(attributeId, new ArrayList<>());
				}
			}

			/*
			 * show that each value is compatible with the property definition
			 */
			for (AttributeId attributeId : data.attributeDefinitions.keySet()) {
				AttributeDefinition attributeDefinition = data.attributeDefinitions.get(attributeId);

				List<Object> list = data.personAttributeValues.get(attributeId);
				for (int i = 0; i < list.size(); i++) {
					Object value = list.get(i);
					if (value != null) {
						if (!attributeDefinition.getType().isAssignableFrom(value.getClass())) {
							throw new ContractException(AttributeError.INCOMPATIBLE_VALUE, attributeId + " = " + value);
						}
					}
				}

			}

			// reorder property values map to match the definitions order
			Map<AttributeId, List<Object>> temp = new LinkedHashMap<>();

			for (AttributeId attributeId : data.attributeDefinitions.keySet()) {
				if (data.personAttributeValues.containsKey(attributeId)) {
					temp.put(attributeId, data.personAttributeValues.get(attributeId));
				}
			}
			data.personAttributeValues = temp;

		}

		/**
		 * Returns the {@linkplain AttributesPluginData} from the collected data
		 * 
		 * @throws ContractException
		 *                           <ul>
		 *                           <li>{@linkplain AttributeError#UNKNOWN_ATTRIBUTE_ID}
		 *                           if a person attribute value was recorded for an
		 *                           unknown attribute id</li>
		 *                           <li>{@linkplain AttributeError#INCOMPATIBLE_VALUE}
		 *                           if a person attribute value was recorded that is
		 *                           not compatible witht he corresponding attribute
		 *                           definition</li>
		 *                           </ul>
		 */
		public AttributesPluginData build() {
			if (!data.locked) {
				validateData();
			}
			ensureImmutability();
			return new AttributesPluginData(data);
		}

		/**
		 * Adds an attribute definition.
		 *
		 * @throws ContractException
		 *                           <ul>
		 *                           <li>{@linkplain AttributeError#NULL_ATTRIBUTE_ID}
		 *                           if the attribute id is null</li>
		 *                           <li>{@linkplain AttributeError#NULL_ATTRIBUTE_DEFINITION}
		 *                           if the attribute definition is null</li>
		 *                           <li>{@linkplain AttributeError#DUPLICATE_ATTRIBUTE_DEFINITION}
		 *                           if the attribute id was previously added</li>
		 *                           </ul>
		 */
		public Builder defineAttribute(final AttributeId attributeId, final AttributeDefinition attributeDefinition) {
			ensureDataMutability();
			validateAttributeIdNotNull(attributeId);
			validateAttributeDefinitionNotNull(attributeDefinition);
			validateAttributeIsNotDefined(data, attributeId);
			data.attributeDefinitions.put(attributeId, attributeDefinition);
			return this;
		}

		/**
		 * Sets the person's attribute value. Duplicate inputs override previous inputs.
		 * 
		 * @throws ContractException
		 *                           <ul>
		 *                           <li>{@linkplain PersonError#NULL_PERSON_ID} if the
		 *                           person id is null</li>
		 *                           <li>{@linkplain AttributeError#NULL_ATTRIBUTE_ID}
		 *                           if the attribute property id is null</li>
		 *                           <li>{@linkplain AttributeError#NULL_ATTRIBUTE_VALUE}
		 *                           if the attribute property value is null</li>
		 *                           </ul>
		 */
		public Builder setPersonAttributeValue(final PersonId personId, final AttributeId attributeId,
				final Object attributeValue) {
			ensureDataMutability();
			validatePersonId(personId);
			validateAttributeIdNotNull(attributeId);
			validateAttributeValueNotNull(attributeValue);

			List<Object> list = data.personAttributeValues.get(attributeId);
			if (list == null) {
				list = new ArrayList<>();
				data.personAttributeValues.put(attributeId, list);
			}

			int personIndex = personId.getValue();
			while (list.size() <= personIndex) {
				list.add(null);
			}
			list.set(personIndex, attributeValue);

			return this;
		}

	}

	private static void validateAttributeIsNotDefined(final Data data, final AttributeId attributeId) {
		AttributeDefinition attributeDefinition = data.attributeDefinitions.get(attributeId);
		if (attributeDefinition != null) {
			throw new ContractException(AttributeError.DUPLICATE_ATTRIBUTE_DEFINITION, attributeId);
		}
	}

	private static void validateAttributeDefinitionNotNull(AttributeDefinition attributeDefinition) {
		if (attributeDefinition == null) {
			throw new ContractException(AttributeError.NULL_ATTRIBUTE_DEFINITION);
		}
	}

	private static void validateAttributeIdNotNull(AttributeId attributeId) {
		if (attributeId == null) {
			throw new ContractException(AttributeError.NULL_ATTRIBUTE_ID);
		}
	}

	private final Data data;

	private AttributesPluginData(Data data) {
		this.data = data;
	}

	/**
	 * Returns the attribute definition for the given attribute id
	 * 
	 * @throws ContractException
	 *                           <ul>
	 *                           <li>{@linkplain AttributeError#NULL_ATTRIBUTE_ID}
	 *                           if the attribute id is null</li>
	 *                           <li>{@linkplain AttributeError#UNKNOWN_ATTRIBUTE_ID}
	 *                           if the attribute id is unknown</li>
	 *                           </ul>
	 */
	public AttributeDefinition getAttributeDefinition(final AttributeId attributeId) {
		validateAttributeIdNotNull(attributeId);
		AttributeDefinition attributeDefinition = data.attributeDefinitions.get(attributeId);
		if (attributeDefinition == null) {
			throw new ContractException(AttributeError.UNKNOWN_ATTRIBUTE_ID, attributeId);
		}
		return attributeDefinition;
	}

	/**
	 * Returns the attribute ids
	 */
	@SuppressWarnings("unchecked")
	public <T extends AttributeId> Set<T> getAttributeIds() {
		Set<T> result = new LinkedHashSet<>();
		for (AttributeId attributeId : data.attributeDefinitions.keySet()) {
			result.add((T) attributeId);
		}
		return result;
	}
	
	/**
	 * Returns a new builder instance that is pre-filled with the current state of
	 * this instance.
	 */
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
	 * Two {@link AttributesPluginData} instances are equal if and only if
	 * their inputs are equal.
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		AttributesPluginData other = (AttributesPluginData) obj;
		return Objects.equals(data, other.data);
	}

	@Override
	public String toString() {
		StringBuilder builder2 = new StringBuilder();
		builder2.append("AttributesPluginData [data=");
		builder2.append(data);
		builder2.append("]");
		return builder2.toString();
	}

	private static void validatePersonId(PersonId personId) {
		if (personId == null) {
			throw new ContractException(PersonError.NULL_PERSON_ID);
		}
	}

	private static void validateAttributeValueNotNull(Object attributeValue) {
		if (attributeValue == null) {
			throw new ContractException(AttributeError.NULL_ATTRIBUTE_VALUE);
		}
	}

	/**
	 * Returns the attribute values for the given attribute id as an unmodifiable
	 * list. Each object in the list corresponds to a PersonId in ascending order
	 * starting from zero.
	 *
	 * @throws ContractException
	 *                           <ul>
	 *                           <li>{@linkplain AttributeError#NULL_ATTRIBUTE_ID}
	 *                           if the attribute id is null</li>
	 *                           <li>{@linkplain AttributeError#UNKNOWN_ATTRIBUTE_ID}
	 *                           if the attribute id is unknown</li>
	 *                           </ul>
	 */
	public List<Object> getAttributeValues(AttributeId attributeId) {
		validateAttributeId(attributeId);
		List<Object> list = data.personAttributeValues.get(attributeId);
		if (list == null) {
			return data.emptyValueList;
		}
		return Collections.unmodifiableList(list);
	}

	private void validateAttributeId(AttributeId attributeId) {
		if (attributeId == null) {
			throw new ContractException(AttributeError.NULL_ATTRIBUTE_ID);
		}
		if (!data.attributeDefinitions.containsKey(attributeId)) {
			throw new ContractException(AttributeError.UNKNOWN_ATTRIBUTE_ID);
		}
	}

	public Map<AttributeId, AttributeDefinition> getAttributeDefinitions() {
		return new LinkedHashMap<>(data.attributeDefinitions);
	}

	public Map<AttributeId, List<Object>> getAttributeValues() {

		Map<AttributeId, List<Object>> result = new LinkedHashMap<>();

		for (AttributeId attributeId : data.personAttributeValues.keySet()) {
			List<Object> list = data.personAttributeValues.get(attributeId);
			List<Object> newList = new ArrayList<>(list);
			result.put(attributeId, newList);
		}

		return result;
	}
}
