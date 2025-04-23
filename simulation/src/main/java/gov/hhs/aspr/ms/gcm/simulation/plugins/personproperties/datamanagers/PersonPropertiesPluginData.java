package gov.hhs.aspr.ms.gcm.simulation.plugins.personproperties.datamanagers;

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
import gov.hhs.aspr.ms.gcm.simulation.nucleus.StandardVersioning;
import gov.hhs.aspr.ms.gcm.simulation.plugins.people.support.PersonError;
import gov.hhs.aspr.ms.gcm.simulation.plugins.people.support.PersonId;
import gov.hhs.aspr.ms.gcm.simulation.plugins.personproperties.support.PersonPropertyError;
import gov.hhs.aspr.ms.gcm.simulation.plugins.personproperties.support.PersonPropertyId;
import gov.hhs.aspr.ms.gcm.simulation.plugins.properties.support.PropertyDefinition;
import gov.hhs.aspr.ms.gcm.simulation.plugins.properties.support.PropertyError;
import gov.hhs.aspr.ms.util.errors.ContractException;
import net.jcip.annotations.Immutable;

/**
 * An immutable container of the initial state of person properties. Contains:
 * <BR>
 * <ul>
 * <li>person property ids</li>
 * <li>person property definitions</li>
 * </ul>
 */
@Immutable
public class PersonPropertiesPluginData implements PluginData {

	private static class Data {

		private Map<PersonPropertyId, PropertyDefinition> propertyDefinitions = new LinkedHashMap<>();

		private Map<PersonPropertyId, Double> propertyDefinitionTimes = new LinkedHashMap<>();

		private Map<PersonPropertyId, Boolean> propertyTrackingPolicies = new LinkedHashMap<>();

		private Map<PersonPropertyId, List<Object>> propertyValues = new LinkedHashMap<>();

		private Map<PersonPropertyId, List<Double>> propertyTimes = new LinkedHashMap<>();

		private List<Object> emptyValueList = Collections.unmodifiableList(new ArrayList<>());

		private List<Double> emptyTimeList = Collections.unmodifiableList(new ArrayList<>());

		private boolean locked;

		private Data() {
		}

		private Data(Data data) {
			propertyDefinitions.putAll(data.propertyDefinitions);
			propertyDefinitionTimes.putAll(data.propertyDefinitionTimes);
			propertyTrackingPolicies.putAll(data.propertyTrackingPolicies);
			for (PersonPropertyId personPropertyId : data.propertyValues.keySet()) {
				List<Object> list = new ArrayList<>();
				propertyValues.put(personPropertyId, list);
				list.addAll(data.propertyValues.get(personPropertyId));
			}
			for (PersonPropertyId personPropertyId : data.propertyTimes.keySet()) {
				List<Double> list = new ArrayList<>();
				propertyTimes.put(personPropertyId, list);
				list.addAll(data.propertyTimes.get(personPropertyId));
			}
			locked = data.locked;
		}

		/**
    	 * Standard implementation consistent with the {@link #equals(Object)} method
    	 */
		@Override
		public int hashCode() {
			return Objects.hash(propertyDefinitions, propertyDefinitionTimes, propertyTrackingPolicies, propertyValues,
					propertyTimes);
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
			return Objects.equals(propertyDefinitions, other.propertyDefinitions)
					&& Objects.equals(propertyDefinitionTimes, other.propertyDefinitionTimes)
					&& Objects.equals(propertyTrackingPolicies, other.propertyTrackingPolicies)
					&& Objects.equals(propertyValues, other.propertyValues)
					&& Objects.equals(propertyTimes, other.propertyTimes);
		}

		@Override
		public String toString() {
			StringBuilder builder = new StringBuilder();
			builder.append("Data [personPropertyDefinitions=");
			builder.append(propertyDefinitions);
			builder.append(", propertyDefinitionTimes=");
			builder.append(propertyDefinitionTimes);
			builder.append(", propertyTrackingPolicies=");
			builder.append(propertyTrackingPolicies);
			builder.append(", personPropertyValues=");
			builder.append(propertyValues);
			builder.append(", personPropertyTimes=");
			builder.append(propertyTimes);
			builder.append("]");
			return builder.toString();
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
		 * Builds the {@linkplain PersonPropertiesPluginData} from the collected data.
		 * 
		 * @throws ContractException
		 *                           <ul>
		 *                           <li>{@linkplain PropertyError#UNKNOWN_PROPERTY_ID}
		 *                           if a person is assigned a property value or time
		 *                           for a property that was not defined.</li>
		 *                           <li>{@linkplain PropertyError#INCOMPATIBLE_VALUE}
		 *                           if a person is assigned a property value that is
		 *                           incompatible with the associated property
		 *                           definition</li>
		 *                           <li>{@linkplain PropertyError#TIME_TRACKING_OFF} if
		 *                           a person is assigned a property assignment time,
		 *                           but the corresponding property is not marked for
		 *                           time tracking</li>
		 *                           <li>{@linkplain PersonPropertyError#PROPERTY_TIME_PRECEDES_DEFAULT}
		 *                           if a person is assigned a property assignment time,
		 *                           but that value precedes default tracking time for
		 *                           the corresponding property id</li>
		 *                           </ul>
		 */
		public PersonPropertiesPluginData build() {

			if (!data.locked) {
				validateData();
			}
			ensureImmutability();
			return new PersonPropertiesPluginData(data);

		}

		/**
		 * Defines a person property definition. Duplicate inputs override previous
		 * inputs.
		 * 
		 * @throws ContractException
		 *                           <ul>
		 *                           <li>{@linkplain PropertyError#NULL_PROPERTY_ID} if
		 *                           the person property id is null</li>
		 *                           <li>{@linkplain PropertyError#NULL_PROPERTY_DEFINITION}
		 *                           if the person property definition value is
		 *                           null</li>
		 *                           <li>{@linkplain PersonPropertyError#NON_FINITE_TIME}
		 *                           if the default property time is not finite</li>
		 *                           </ul>
		 */
		public Builder definePersonProperty(final PersonPropertyId personPropertyId,
				final PropertyDefinition propertyDefinition, double time, boolean trackTimes) {
			ensureDataMutability();
			validatePersonPropertyIdNotNull(personPropertyId);
			validatePersonPropertyDefinitionNotNull(propertyDefinition);
			validateTime(time);
			data.propertyDefinitions.put(personPropertyId, propertyDefinition);
			data.propertyDefinitionTimes.put(personPropertyId, time);
			data.propertyTrackingPolicies.put(personPropertyId, trackTimes);
			return this;
		}

		/**
		 * Sets the person's property value. Duplicate inputs override previous inputs.
		 * Avoid setting the value to the default value of the corresponding property
		 * definition.
		 * 
		 * @throws ContractException
		 *                           <ul>
		 *                           <li>{@linkplain PersonError#NULL_PERSON_ID} if the
		 *                           person id is null</li>
		 *                           <li>{@linkplain PropertyError#NULL_PROPERTY_ID} if
		 *                           the person property id is null</li>
		 *                           <li>{@linkplain PropertyError#NULL_PROPERTY_VALUE}
		 *                           if the person property value is null</li>
		 *                           </ul>
		 */
		public Builder setPersonPropertyValue(final PersonId personId, final PersonPropertyId personPropertyId,
				final Object personPropertyValue) {
			ensureDataMutability();
			validatePersonId(personId);
			validatePersonPropertyIdNotNull(personPropertyId);
			validatePersonPropertyValueNotNull(personPropertyValue);

			List<Object> list = data.propertyValues.get(personPropertyId);
			if (list == null) {
				list = new ArrayList<>();
				data.propertyValues.put(personPropertyId, list);
			}

			int personIndex = personId.getValue();
			while (list.size() <= personIndex) {
				list.add(null);
			}
			list.set(personIndex, personPropertyValue);

			return this;
		}

		/**
		 * Sets the person's property time. Duplicate inputs override previous inputs.
		 * Avoid setting the time to the default tracking time.
		 * 
		 * @throws ContractException
		 *                           <ul>
		 *                           <li>{@linkplain PersonError#NULL_PERSON_ID} if the
		 *                           person id is null</li>
		 *                           <li>{@linkplain PropertyError#NULL_PROPERTY_ID} if
		 *                           the person property id is null</li>
		 *                           <li>{@linkplain PersonPropertyError#NULL_TIME} if
		 *                           the person property time is null</li>
		 *                           <li>{@linkplain PersonPropertyError#NON_FINITE_TIME}
		 *                           if the person property time is not finite</li>
		 *                           </ul>
		 */
		public Builder setPersonPropertyTime(final PersonId personId, final PersonPropertyId personPropertyId,
				final Double personPropertyTime) {
			ensureDataMutability();
			validatePersonId(personId);
			validatePersonPropertyIdNotNull(personPropertyId);
			validateTime(personPropertyTime);

			List<Double> list = data.propertyTimes.get(personPropertyId);
			if (list == null) {
				list = new ArrayList<>();
				data.propertyTimes.put(personPropertyId, list);
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

			for (PersonPropertyId personPropertyId : data.propertyValues.keySet()) {
				if (!data.propertyDefinitions.keySet().contains(personPropertyId)) {
					throw new ContractException(PropertyError.UNKNOWN_PROPERTY_ID, personPropertyId);
				}
			}
			for (PersonPropertyId personPropertyId : data.propertyTimes.keySet()) {
				if (!data.propertyDefinitions.keySet().contains(personPropertyId)) {
					throw new ContractException(PropertyError.UNKNOWN_PROPERTY_ID, personPropertyId);
				}
			}

			/*
			 * show that each value is compatible with the property definition
			 */
			for (PersonPropertyId personPropertyId : data.propertyDefinitions.keySet()) {
				PropertyDefinition propertyDefinition = data.propertyDefinitions.get(personPropertyId);

				List<Object> list = data.propertyValues.get(personPropertyId);
				if (list != null) {
					for (int i = 0; i < list.size(); i++) {
						Object value = list.get(i);
						if (value != null) {
							if (!propertyDefinition.getType().isAssignableFrom(value.getClass())) {
								throw new ContractException(PropertyError.INCOMPATIBLE_VALUE,
										personPropertyId + " = " + value);
							}
						}
					}
				}
			}

			// show that any property that is not time tracked has no time
			// values
			for (PersonPropertyId personPropertyId : data.propertyTrackingPolicies.keySet()) {
				Boolean tracked = data.propertyTrackingPolicies.get(personPropertyId);
				if (!tracked) {
					if (data.propertyTimes.containsKey(personPropertyId)) {
						throw new ContractException(PropertyError.TIME_TRACKING_OFF, personPropertyId
								+ " has tracking times collected, but is not itself marked for tracking");
					}
				}
			}

			// show that any property that is time tracked has no time
			// value less than the default time value
			for (PersonPropertyId personPropertyId : data.propertyTrackingPolicies.keySet()) {
				Boolean tracked = data.propertyTrackingPolicies.get(personPropertyId);
				if (tracked) {
					Double defaultTime = data.propertyDefinitionTimes.get(personPropertyId);
					List<Double> list = data.propertyTimes.get(personPropertyId);
					if (list != null) {
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
	 * Returns the {@link PropertyDefinition} for the given {@link PersonPropertyId}
	 * 
	 * @throws ContractException
	 *                           <ul>
	 *                           <li>{@linkplain PropertyError#NULL_PROPERTY_ID} if
	 *                           the person property id is null</li>
	 *                           <li>{@linkplain PropertyError#UNKNOWN_PROPERTY_ID}
	 *                           if the person property id is unknown</li>
	 *                           </ul>
	 */
	public PropertyDefinition getPersonPropertyDefinition(final PersonPropertyId personPropertyId) {
		validatePersonPropertyIdNotNull(personPropertyId);
		final PropertyDefinition propertyDefinition = data.propertyDefinitions.get(personPropertyId);
		if (propertyDefinition == null) {
			throw new ContractException(PropertyError.UNKNOWN_PROPERTY_ID, personPropertyId);
		}
		return propertyDefinition;
	}

	/**
	 * Returns the time when the person property id was added.
	 * 
	 * @throws ContractException
	 *                           <ul>
	 *                           <li>{@linkplain PropertyError#NULL_PROPERTY_ID} if
	 *                           the person property id is null</li>
	 *                           <li>{@linkplain PropertyError#UNKNOWN_PROPERTY_ID}
	 *                           if the person property id is unknown</li>
	 *                           </ul>
	 */
	public double getPropertyDefinitionTime(final PersonPropertyId personPropertyId) {
		validatePersonPropertyId(personPropertyId);
		return data.propertyDefinitionTimes.get(personPropertyId);
	}

	/**
	 * Returns true if the person property assignment times are tracked.
	 * 
	 * @throws ContractException
	 *                           <ul>
	 *                           <li>{@linkplain PropertyError#NULL_PROPERTY_ID} if
	 *                           the person property id is null</li>
	 *                           <li>{@linkplain PropertyError#UNKNOWN_PROPERTY_ID}
	 *                           if the person property id is unknown</li>
	 *                           </ul>
	 */
	public boolean propertyAssignmentTimesTracked(final PersonPropertyId personPropertyId) {
		validatePersonPropertyId(personPropertyId);
		return data.propertyTrackingPolicies.get(personPropertyId);
	}

	/**
	 * Returns the set of {@link PersonPropertyId} ids
	 */
	@SuppressWarnings("unchecked")
	public <T extends PersonPropertyId> Set<T> getPersonPropertyIds() {
		Set<T> result = new LinkedHashSet<>();
		for (PersonPropertyId personPropertyId : data.propertyDefinitions.keySet()) {
			result.add((T) personPropertyId);
		}
		return result;
	}

	@Override
	public Builder toBuilder() {
		return new Builder(data);
	}

	private void validatePersonPropertyId(PersonPropertyId personPropertyId) {
		if (personPropertyId == null) {
			throw new ContractException(PropertyError.NULL_PROPERTY_ID);
		}
		if (!data.propertyDefinitions.containsKey(personPropertyId)) {
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
	 *                           <ul>
	 *                           <li>{@linkplain PropertyError#NULL_PROPERTY_ID} if
	 *                           the person property id is null</li>
	 *                           <li>{@linkplain PropertyError#UNKNOWN_PROPERTY_ID}
	 *                           if the person property id is unknown</li>
	 *                           </ul>
	 */
	public List<Object> getPropertyValues(PersonPropertyId personPropertyId) {
		validatePersonPropertyId(personPropertyId);
		List<Object> list = data.propertyValues.get(personPropertyId);
		if (list == null) {
			return data.emptyValueList;
		}
		return Collections.unmodifiableList(list);
	}

	/**
	 * Returns the property values for the given person property id as an
	 * unmodifiable list. Each Double in the list corresponds to a PersonId in
	 * ascending order starting from zero.
	 */
	public List<Double> getPropertyTimes(PersonPropertyId personPropertyId) {
		validatePersonPropertyId(personPropertyId);
		List<Double> list = data.propertyTimes.get(personPropertyId);
		if (list == null) {
			return data.emptyTimeList;
		}
		return Collections.unmodifiableList(list);
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

	/**
     * Standard implementation consistent with the {@link #equals(Object)} method
     */
	@Override
	public int hashCode() {
		return Objects.hash(data);
	}

	/**
     * Two {@link PersonPropertiesPluginData} instances are equal if and only if
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
		PersonPropertiesPluginData other = (PersonPropertiesPluginData) obj;
		return Objects.equals(data, other.data);
	}

	@Override
	public String toString() {
		StringBuilder builder2 = new StringBuilder();
		builder2.append("PersonPropertiesPluginData [data=");
		builder2.append(data);
		builder2.append("]");
		return builder2.toString();
	}

	public Map<PersonPropertyId, Boolean> getPropertyTrackingPolicies() {
		return new LinkedHashMap<>(data.propertyTrackingPolicies);
	}

	public Map<PersonPropertyId, PropertyDefinition> getPropertyDefinitions() {
		return new LinkedHashMap<>(data.propertyDefinitions);
	}

	public Map<PersonPropertyId, Double> getPropertyDefinitionTimes() {
		return new LinkedHashMap<>(data.propertyDefinitionTimes);
	}

	public Map<PersonPropertyId, List<Object>> getPropertyValues() {
		Map<PersonPropertyId, List<Object>> result = new LinkedHashMap<>();
		for (PersonPropertyId personPropertyId : data.propertyValues.keySet()) {
			List<Object> list = data.propertyValues.get(personPropertyId);
			List<Object> newList = new ArrayList<>(list);
			result.put(personPropertyId, newList);
		}
		return result;
	}

	public Map<PersonPropertyId, List<Double>> getPropertyTimes() {
		Map<PersonPropertyId, List<Double>> result = new LinkedHashMap<>();
		for (PersonPropertyId personPropertyId : data.propertyTimes.keySet()) {
			List<Double> list = data.propertyTimes.get(personPropertyId);
			List<Double> newList = new ArrayList<>(list);
			result.put(personPropertyId, newList);
		}
		return result;
	}

}
