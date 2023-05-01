package plugins.personproperties;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
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

		private Map<PersonPropertyId, Double> propertyTrackingTimes = new LinkedHashMap<>();

		private Map<PersonPropertyId, List<Object>> personPropertyValues = new LinkedHashMap<>();

		private Map<PersonPropertyId, List<Double>> personPropertyTimes = new LinkedHashMap<>();

		private List<Object> emptyValueList = Collections.unmodifiableList(new ArrayList<>());

		private List<Double> emptyTimeList = Collections.unmodifiableList(new ArrayList<>());

		private boolean locked;

		private Data() {
		}

		private Data(Data data) {
			personPropertyDefinitions.putAll(data.personPropertyDefinitions);
			propertyTrackingTimes.putAll(data.propertyTrackingTimes);
			for (PersonPropertyId personPropertyId : data.personPropertyValues.keySet()) {
				List<Object> list = new ArrayList<>();
				personPropertyValues.put(personPropertyId, list);
				list.addAll(data.personPropertyValues.get(personPropertyId));
			}
			for (PersonPropertyId personPropertyId : data.personPropertyTimes.keySet()) {
				List<Double> list = new ArrayList<>();
				personPropertyTimes.put(personPropertyId, list);
				list.addAll(data.personPropertyTimes.get(personPropertyId));
			}
			locked = data.locked;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + personPropertyDefinitions.hashCode();
			result = prime * result + propertyTrackingTimes.hashCode();
			result = prime * result + getPersonPropertyValuesHashCode();
			result = prime * result + getPersonPropertyTimesHashCode();
			return result;
		}

		private int getPersonPropertyTimesHashCode() {
			int result = 0;
			for (PersonPropertyId personPropertyId : propertyTrackingTimes.keySet()) {
				result += getPersonPropertyTimesHashCode(personPropertyId);
			}
			return result;
		}

		private int getPersonPropertyTimesHashCode(PersonPropertyId personPropertyId) {
			int result = 0;
			Double defaultTime = propertyTrackingTimes.get(personPropertyId);

			List<Double> list = personPropertyTimes.get(personPropertyId);
			int prime = 31;
			result = 1;
			for (Double time : list) {
				if (time != null) {
					if (!time.equals(defaultTime)) {
						result = result * prime + time.hashCode();
					}
				}
			}

			return result;
		}

		private int getPersonPropertyValuesHashCode(PersonPropertyId personPropertyId) {
			int prime = 31;
			int result = 1;
			PropertyDefinition propertyDefinition = personPropertyDefinitions.get(personPropertyId);
			Optional<Object> optional = propertyDefinition.getDefaultValue();
			List<Object> list = personPropertyValues.get(personPropertyId);

			if (optional.isPresent()) {
				Object defaultValue = optional.get();

				for (Object value : list) {
					if (value != null && !value.equals(defaultValue)) {
						result = result * prime + value.hashCode();
					}
				}
			} else {
				for (Object value : list) {
					if (value != null) {
						result = result * prime + value.hashCode();
					}
				}
			}
			return result;
		}

		private int getPersonPropertyValuesHashCode() {
			int result = 0;

			for (PersonPropertyId personPropertyId : personPropertyValues.keySet()) {
				result += getPersonPropertyValuesHashCode(personPropertyId);

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

			if (!propertyTrackingTimes.equals(other.propertyTrackingTimes)) {
				return false;
			}

			if (!comparePersonPropertyValues(this, other)) {
				return false;
			}

			if (!comparePersonPropertyTimes(this, other)) {
				return false;
			}
			return true;
		}

		@Override
		public String toString() {
			StringBuilder builder = new StringBuilder();
			builder.append("Data [personPropertyDefinitions=");
			builder.append(personPropertyDefinitions);
			builder.append(", propertyTrackingTimes=");
			builder.append(propertyTrackingTimes);
			builder.append(", personPropertyValues=");
			builder.append(personPropertyValues);
			builder.append(", personPropertyTimes=");
			builder.append(personPropertyTimes);
			builder.append(", emptyValueList=");
			builder.append(emptyValueList);
			builder.append(", emptyTimeList=");
			builder.append(emptyTimeList);
			builder.append(", locked=");
			builder.append(locked);
			builder.append("]");
			return builder.toString();
		}
		

	}

	private static boolean comparePersonPropertyValues(Data a, Data b) {
		for (PersonPropertyId personPropertyId : a.personPropertyDefinitions.keySet()) {
			List<Object> alist = a.personPropertyValues.get(personPropertyId);
			List<Object> blist = b.personPropertyValues.get(personPropertyId);

			PropertyDefinition propertyDefinition = a.personPropertyDefinitions.get(personPropertyId);
			int n = FastMath.max(alist.size(), blist.size());
			Optional<Object> optional = propertyDefinition.getDefaultValue();
			if (optional.isPresent()) {
				Object defaultValue = optional.get();

				for (int i = 0; i < n; i++) {
					Object aValue = null;
					if (i < alist.size()) {
						aValue = alist.get(i);
					}
					if (aValue == null) {
						aValue = defaultValue;
					}
					Object bValue = null;
					if (i < blist.size()) {
						bValue = blist.get(i);
					}
					if (bValue == null) {
						bValue = defaultValue;
					}

					if (!aValue.equals(bValue)) {
						return false;
					}
				}
			} else {
				for (int i = 0; i < n; i++) {
					Object aValue = null;
					if (i < alist.size()) {
						aValue = alist.get(i);
					}

					Object bValue = null;
					if (i < blist.size()) {
						bValue = blist.get(i);
					}

					if (!Objects.equals(aValue, bValue)) {
						return false;
					}
				}
			}
		}
		return true;
	}

	private static boolean comparePersonPropertyTimes(Data a, Data b) {
		for (PersonPropertyId personPropertyId : a.propertyTrackingTimes.keySet()) {
			Double defaultTime = a.propertyTrackingTimes.get(personPropertyId);
			List<Double> alist = a.personPropertyTimes.get(personPropertyId);
			List<Double> blist = b.personPropertyTimes.get(personPropertyId);
			int n = FastMath.max(alist.size(), blist.size());
			for (int i = 0; i < n; i++) {
				Double aTime = null;
				if (i < alist.size()) {
					aTime = alist.get(i);
				}
				if (aTime == null) {
					aTime = defaultTime;
				}
				Double bTime = null;
				if (i < blist.size()) {
					bTime = blist.get(i);
				}
				if (bTime == null) {
					bTime = defaultTime;
				}

				if (!aTime.equals(bTime)) {
					return false;
				}
			}
		}
		return true;
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
		 *             person is assigned a property value or time for a
		 *             property that was not defined. If a person property was
		 *             assigned a default tracking time but has no corresponding
		 *             property definition</li>
		 * 
		 *             <li>{@linkplain PropertyError#INCOMPATIBLE_VALUE} if a
		 *             person is assigned a property value that is incompatible
		 *             with the associated property definition</li>
		 * 
		 *             <li>{@linkplain PropertyError#TIME_TRACKING_OFF} if a
		 *             person is assigned a property assignment time, but the
		 *             corresponding property is not marked for time
		 *             tracking</li>
		 * 
		 *             <li>{@linkplain PropertyError#PROPERTY_TIME_PRECEDES_DEFAULT}
		 *             if a person is assigned a property assignment time, but
		 *             that value precedes default tracking time for the
		 *             corresponding property id</li>
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
		 * Sets the Defines a person property definition. Duplicate inputs
		 * override previous inputs.
		 * 
		 * @throws ContractException
		 *             <li>{@linkplain PropertyError#NULL_PROPERTY_ID} if the
		 *             person property id is null</li>
		 *             <li>{@linkplain PersonPropertyError#NON_FINITE_TIME} if
		 *             the default property time is not finite</li>
		 * 
		 */
		public Builder setTimeTracking(final PersonPropertyId personPropertyId, final double defaultPropertyTime) {
			ensureDataMutability();
			validatePersonPropertyIdNotNull(personPropertyId);
			validateTime(defaultPropertyTime);
			data.propertyTrackingTimes.put(personPropertyId, defaultPropertyTime);
			return this;
		}

		/**
		 * Sets the person's property value. Duplicate inputs override previous
		 * inputs. Avoid setting the value to the default value of the
		 * corresponding property definition.
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

			List<Object> list = data.personPropertyValues.get(personPropertyId);
			if (list == null) {
				list = new ArrayList<>();
				data.personPropertyValues.put(personPropertyId, list);
			}

			int personIndex = personId.getValue();
			while (list.size() <= personIndex) {
				list.add(null);
			}
			list.set(personIndex, personPropertyValue);

			return this;
		}

		/**
		 * Sets the person's property time. Duplicate inputs override previous
		 * inputs. Avoid setting the time to the default tracking time.
		 * 
		 * @throws ContractException
		 *             <li>{@linkplain PersonError#NULL_PERSON_ID} if the person
		 *             id is null</li>
		 *             <li>{@linkplain PropertyError#NULL_PROPERTY_ID} if the
		 *             person property id is null</li>
		 *             <li>{@linkplain PersonPropertyError#NULL_TIME} if the
		 *             person property time is null</li>
		 *             <li>{@linkplain PersonPropertyError#NON_FINITE_TIME} if
		 *             the person property time is not finite</li>
		 */
		public Builder setPersonPropertyTime(final PersonId personId, final PersonPropertyId personPropertyId, final Double personPropertyTime) {
			ensureDataMutability();
			validatePersonId(personId);
			validatePersonPropertyIdNotNull(personPropertyId);
			validateTime(personPropertyTime);

			List<Double> list = data.personPropertyTimes.get(personPropertyId);
			if (list == null) {
				list = new ArrayList<>();
				data.personPropertyTimes.put(personPropertyId, list);
			}

			int personIndex = personId.getValue();
			while (list.size() <= personIndex) {
				list.add(null);
			}
			list.set(personIndex, personPropertyTime);

			return this;
		}

		private void validateData() {

			// show all property ids agree with the definitions
			for (PersonPropertyId personPropertyId : data.propertyTrackingTimes.keySet()) {
				if (!data.personPropertyDefinitions.keySet().contains(personPropertyId)) {
					throw new ContractException(PropertyError.UNKNOWN_PROPERTY_ID, personPropertyId);
				}
			}
			for (PersonPropertyId personPropertyId : data.personPropertyValues.keySet()) {
				if (!data.personPropertyDefinitions.keySet().contains(personPropertyId)) {
					throw new ContractException(PropertyError.UNKNOWN_PROPERTY_ID, personPropertyId);
				}
			}
			for (PersonPropertyId personPropertyId : data.personPropertyTimes.keySet()) {
				if (!data.personPropertyDefinitions.keySet().contains(personPropertyId)) {
					throw new ContractException(PropertyError.UNKNOWN_PROPERTY_ID, personPropertyId);
				}
			}

			// add lists where needed
			for (PersonPropertyId personPropertyId : data.personPropertyDefinitions.keySet()) {
				if (!data.personPropertyValues.keySet().contains(personPropertyId)) {
					data.personPropertyValues.put(personPropertyId, new ArrayList<>());
				}
			}

			for (PersonPropertyId personPropertyId : data.personPropertyDefinitions.keySet()) {
				if (!data.personPropertyTimes.keySet().contains(personPropertyId)) {
					data.personPropertyTimes.put(personPropertyId, new ArrayList<>());
				}
			}

			/*
			 * show that each value is compatible with the property definition
			 */
			for (PersonPropertyId personPropertyId : data.personPropertyDefinitions.keySet()) {
				PropertyDefinition propertyDefinition = data.personPropertyDefinitions.get(personPropertyId);

				List<Object> list = data.personPropertyValues.get(personPropertyId);
				for (int i = 0; i < list.size(); i++) {
					Object value = list.get(i);
					if (value != null) {
						if (!propertyDefinition.getType().isAssignableFrom(value.getClass())) {
							throw new ContractException(PropertyError.INCOMPATIBLE_VALUE, personPropertyId + " = " + value);
						}
					}
				}

			}

			// show that any property that is not time tracked has no time
			// values
			for (PersonPropertyId personPropertyId : data.personPropertyDefinitions.keySet()) {
				if (!data.propertyTrackingTimes.keySet().contains(personPropertyId)) {
					List<Double> list = data.personPropertyTimes.get(personPropertyId);
					if (!list.isEmpty()) {
						throw new ContractException(PropertyError.TIME_TRACKING_OFF, personPropertyId + " has tracking times collected, but is not itself marked for tracking");
					}
				}
			}

			// show that any property that is time tracked has no time
			// value less than the default time value
			for (PersonPropertyId personPropertyId : data.personPropertyDefinitions.keySet()) {
				if (data.propertyTrackingTimes.keySet().contains(personPropertyId)) {
					Double defaultTime = data.propertyTrackingTimes.get(personPropertyId);
					List<Double> list = data.personPropertyTimes.get(personPropertyId);
					for (Double time : list) {
						if (time != null) {
							if (time < defaultTime) {
								throw new ContractException(PersonPropertyError.PROPERTY_TIME_PRECEDES_DEFAULT);
							}
						}
					}
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
	 * Returns the default tracking time for the given person property id as an
	 * Optional<Double>. The property will be time tracked if the optional is
	 * present.
	 * 
	 * @throws ContractException
	 * 
	 *             <li>{@linkplain PropertyError#NULL_PROPERTY_ID} if the person
	 *             property id is null</li>
	 *             <li>{@linkplain PropertyError#UNKNOWN_PROPERTY_ID} if the
	 *             person property id is unknown</li>
	 * 
	 */
	public Optional<Double> getTrackingTime(final PersonPropertyId personPropertyId) {
		validatePersonPropertyId(personPropertyId);
		Double result = data.propertyTrackingTimes.get(personPropertyId);
		return Optional.ofNullable(result);
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

	private void validatePersonPropertyId(PersonPropertyId personPropertyId) {
		if (personPropertyId == null) {
			throw new ContractException(PropertyError.NULL_PROPERTY_ID);
		}
		if (!data.personPropertyDefinitions.containsKey(personPropertyId)) {
			throw new ContractException(PropertyError.UNKNOWN_PROPERTY_ID);
		}
	}

	private static void validatePersonPropertyValueNotNull(Object personPropertyValue) {
		if (personPropertyValue == null) {
			throw new ContractException(PropertyError.NULL_PROPERTY_VALUE);
		}
	}

	private static void validatePersonId(PersonId personId) {
		if (personId == null) {
			throw new ContractException(PersonError.NULL_PERSON_ID);
		}
	}

	private static void validateTime(double time) {
		if (!Double.isFinite(time)) {
			throw new ContractException(PersonPropertyError.NON_FINITE_TIME);
		}
	}

	private static void validateTime(Double time) {
		if (time == null) {
			throw new ContractException(PersonPropertyError.NULL_TIME);
		}
		validateTime(time.doubleValue());
	}

	/**
	 * Returns the property values for the given person property id as an
	 * unmodifiable list. Each object in the list corresponds to a PersonId in
	 * ascending order starting from zero.
	 *
	 * @throws ContractException
	 *             <li>{@linkplain PropertyError#NULL_PROPERTY_ID} if the person
	 *             property id is null</li>
	 *             <li>{@linkplain PropertyError#UNKNOWN_PROPERTY_ID} if the
	 *             person property id is unknown</li>
	 * 
	 */
	public List<Object> getPropertyValues(PersonPropertyId personPropertyId) {
		validatePersonPropertyId(personPropertyId);
		List<Object> list = data.personPropertyValues.get(personPropertyId);
		if (list == null) {
			return data.emptyValueList;
		}
		return Collections.unmodifiableList(list);
	}

	/**
	 * Returns the property values for the given person property id as an
	 * unmodifiable list. Each Double in the list corresponds to a PersonId in
	 * ascending order starting from zero.
	 *
	 */
	public List<Double> getPropertyTimes(PersonPropertyId personPropertyId) {
		validatePersonPropertyId(personPropertyId);
		List<Double> list = data.personPropertyTimes.get(personPropertyId);
		if (list == null) {
			return data.emptyTimeList;
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

	@Override
	public String toString() {
		StringBuilder builder2 = new StringBuilder();
		builder2.append("PersonPropertiesPluginData [data=");
		builder2.append(data);
		builder2.append("]");
		return builder2.toString();
	}
	
	

}
