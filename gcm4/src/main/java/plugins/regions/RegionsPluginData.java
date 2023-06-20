package plugins.regions;

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
import plugins.regions.support.RegionError;
import plugins.regions.support.RegionId;
import plugins.regions.support.RegionPropertyId;
import plugins.util.properties.PropertyDefinition;
import plugins.util.properties.PropertyError;
import util.errors.ContractException;

/**
 * An immutable container of the initial state of regions. It contains: <BR>
 * <ul>
 * <li>region ids</li>
 * <li>suppliers of consumers of {@linkplain AgentContext} for region
 * initialization</li>
 * <li>region property definitions: all regions share a set of property
 * definitions with default values, but have individual property values</li>
 * <li>region property values</li>
 * <li>person region assignments</li>
 * <li>person region arrival time tracking policy</li>
 * </ul>
 * 
 *
 */

@Immutable
public class RegionsPluginData implements PluginData {

	private static enum PersonAdditionMode {
		UNDETERMINED, REGION_ONLY, REGION_AND_TIME
	}

	private static class Data {

		private final Map<RegionPropertyId, PropertyDefinition> regionPropertyDefinitions = new LinkedHashMap<>();

		private final Set<RegionId> regionIds = new LinkedHashSet<>();

		private boolean trackRegionArrivalTimes;

		private final Map<RegionId, Map<RegionPropertyId, Object>> regionPropertyValues = new LinkedHashMap<>();

		private final Map<RegionPropertyId, Object> emptyRegionPropertyMap = Collections.unmodifiableMap(new LinkedHashMap<>());

		private final List<RegionId> personRegions = new ArrayList<>();

		private final List<Double> personArrivalTimes = new ArrayList<>();

		private PersonAdditionMode personAdditionMode = PersonAdditionMode.UNDETERMINED;

		private boolean locked;

		public Data() {
		}		

		public Data(Data data) {
			regionPropertyDefinitions.putAll(data.regionPropertyDefinitions);
			regionIds.addAll(data.regionIds);
			trackRegionArrivalTimes = data.trackRegionArrivalTimes;
			for (RegionId regionId : data.regionPropertyValues.keySet()) {
				Map<RegionPropertyId, Object> map = new LinkedHashMap<>(data.regionPropertyValues.get(regionId));
				regionPropertyValues.put(regionId, map);
			}
			personRegions.addAll(data.personRegions);
			personAdditionMode = data.personAdditionMode;

			locked = data.locked;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + (locked ? 1231 : 1237);
			result = prime * result + personRegions.hashCode();
			result = prime * result + personArrivalTimes.hashCode();

			result = prime * result + regionIds.hashCode();
			result = prime * result + regionPropertyDefinitions.hashCode();
			result = prime * result + regionPropertyValues.hashCode();
			
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

			/*
			 * We exclude the following fields:
			 * 
			 * locked -- two Datas are only compared when they are both locked
			 * -- there are no equality comparisons in this class.
			 * 
			 * personAdditionMode -- is a function of the existing data or is in
			 * the UNDETERMINED state
			 * 
			 * emptyRegionPropertyMap -- just an empty map
			 */
			if (trackRegionArrivalTimes != other.trackRegionArrivalTimes) {
				return false;
			}
			if (!personRegions.equals(other.personRegions)) {
				return false;
			}
			if (!personRegions.equals(other.personRegions)) {
				return false;
			}
			if (!personArrivalTimes.equals(other.personArrivalTimes)) {
				return false;
			}
			if (!regionIds.equals(other.regionIds)) {
				return false;
			}
			if (!regionPropertyDefinitions.equals(other.regionPropertyDefinitions)) {
				return false;
			}
			
			if(!regionPropertyValues.equals(other.regionPropertyValues)){
				return false;
			}
			

			return true;
		}

		@Override
		public String toString() {
			StringBuilder builder = new StringBuilder();
			builder.append("Data [regionPropertyDefinitions=");
			builder.append(regionPropertyDefinitions);
			builder.append(", regionIds=");
			builder.append(regionIds);
			builder.append(", trackRegionArrivalTimes=");
			builder.append(trackRegionArrivalTimes);
			builder.append(", regionPropertyValues=");
			builder.append(regionPropertyValues);
			builder.append(", personRegions=");
			builder.append(personRegions);
			builder.append(", personArrivalTimes=");
			builder.append(personArrivalTimes);
			builder.append(", locked=");
			builder.append(locked);
			builder.append("]");
			return builder.toString();
		}
		
		

	}

	private static void validateRegionExists(final Data data, final RegionId regionId) {
		if (regionId == null) {
			throw new ContractException(RegionError.NULL_REGION_ID);
		}
		if (!data.regionIds.contains(regionId)) {
			throw new ContractException(RegionError.UNKNOWN_REGION_ID, regionId);
		}
	}

	public static Builder builder() {
		return new Builder(new Data());
	}

	private static void validateRegionIdNotNull(RegionId regionId) {
		if (regionId == null) {
			throw new ContractException(RegionError.NULL_REGION_ID);
		}
	}

	private static void validateRegionPropertyIdNotNull(RegionPropertyId regionPropertyId) {
		if (regionPropertyId == null) {
			throw new ContractException(PropertyError.NULL_PROPERTY_ID);
		}
	}

	private static void validateRegionPropertyDefinitionNotNull(PropertyDefinition propertyDefinition) {
		if (propertyDefinition == null) {
			throw new ContractException(PropertyError.NULL_PROPERTY_DEFINITION);
		}

	}

	private static void validateRegionPropertyIsDefined(Data data, RegionPropertyId regionPropertyId) {

		if (!data.regionPropertyDefinitions.containsKey(regionPropertyId)) {
			throw new ContractException(PropertyError.UNKNOWN_PROPERTY_ID);
		}
	}

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
		 * Returns the {@link RegionInitialData} from the collected information
		 * supplied to this builder.
		 * 
		 * @throws ContractException
		 * 
		 *             <li>{@linkplain RegionError#UNKNOWN_REGION_ID} if a
		 *             region property value was associated with a region id
		 *             that was not properly added with an initial agent
		 *             behavior.</li>
		 * 
		 *             <li>{@linkplain PropertyError#UNKNOWN_PROPERTY_ID} if a
		 *             region property value was associated with a region
		 *             property id that was not defined</li>
		 * 
		 *             <li>{@linkplain PropertyError#INCOMPATIBLE_VALUE} if a
		 *             region property value was associated with a region and
		 *             region property id that is incompatible with the
		 *             corresponding property definition.</li>
		 * 
		 *             <li>{@linkplain PropertyError#INSUFFICIENT_PROPERTY_VALUE_ASSIGNMENT}
		 *             if a region property definition does not have a default
		 *             value and there are no property values added to replace
		 *             that default.</li>
		 * 
		 *             <li>{@linkplain RegionError#PERSON_ARRIVAL_DATA_PRESENT}
		 *             if a person region arrival data was collected, but the
		 *             region arrival tracking policy is true</li>
		 * 
		 *             <li>{@linkplain RegionError#MISSING_PERSON_ARRIVAL_DATA}
		 *             if person region arrival data was collected, but the
		 *             region arrival time tracking policy is false</li>
		 * 
		 * 
		 * 
		 * 
		 */
		public RegionsPluginData build() {
			if (!data.locked) {
				validateData();
			}
			ensureImmutability();
			return new RegionsPluginData(data);
		}

		/**
		 * Sets the region property value that overrides the default value of
		 * the corresponding property definition
		 * 
		 * @throws ContractException
		 * 
		 *             <li>{@linkplain RegionError#NULL_REGION_ID}</li>if the
		 *             region id is null
		 * 
		 *             <li>{@linkplain PropertyError#NULL_PROPERTY_ID}</li>if
		 *             the region property id is null
		 * 
		 */
		public Builder setRegionPropertyValue(final RegionId regionId, final RegionPropertyId regionPropertyId, final Object regionPropertyValue) {
			ensureDataMutability();
			validateRegionIdNotNull(regionId);
			validateRegionPropertyIdNotNull(regionPropertyId);
			Map<RegionPropertyId, Object> propertyMap = data.regionPropertyValues.get(regionId);
			if (propertyMap == null) {
				propertyMap = new LinkedHashMap<>();
				data.regionPropertyValues.put(regionId, propertyMap);
			}
			propertyMap.put(regionPropertyId, regionPropertyValue);
			return this;
		}

		private void setPersonAdditionMode(PersonAdditionMode personAdditionMode) {
			if (personAdditionMode == PersonAdditionMode.UNDETERMINED) {
				throw new RuntimeException("person addition mode cannot be set to " + PersonAdditionMode.UNDETERMINED);
			}
			switch (data.personAdditionMode) {
			case REGION_AND_TIME:
			case REGION_ONLY:
				if (personAdditionMode != data.personAdditionMode) {
					throw new ContractException(RegionError.REGION_ARRIVAL_TIMES_MISMATCHED);
				}
				break;
			case UNDETERMINED:
				data.personAdditionMode = personAdditionMode;
				break;
			default:
				throw new RuntimeException("unhandled case " + data.personAdditionMode);
			}
		}

		/**
		 * Sets the person's region. Should be used exclusively when time
		 * tracking will be set to false.
		 * 
		 * @throws ContractException
		 * 
		 *             <li>{@linkplain PersonError#NULL_PERSON_ID} if the person
		 *             id is null</li>
		 * 
		 *             <li>{@linkplain RegionError#NULL_REGION_ID} if the region
		 *             id is null</li>
		 * 
		 *             <li>{@linkplain RegionError#REGION_ARRIVAL_TIMES_MISMATCHED}
		 *             if other people have been added using region arrival
		 *             times</li>
		 * 
		 * 
		 */
		public Builder addPerson(final PersonId personId, final RegionId regionId) {
			ensureDataMutability();
			validatePersonId(personId);
			validateRegionIdNotNull(regionId);
			setPersonAdditionMode(PersonAdditionMode.REGION_ONLY);
			int personIndex = personId.getValue();
			while (personIndex >= data.personRegions.size()) {
				data.personRegions.add(null);
			}
			data.personRegions.set(personIndex, regionId);
			return this;
		}

		/**
		 * Sets the person's region and region arrival time. Should be used
		 * exclusively when time tracking will be set to true.
		 * 
		 * @throws ContractException
		 * 
		 *             <li>{@linkplain PersonError#NULL_PERSON_ID}if the person
		 *             id is null</li>
		 * 
		 *             <li>{@linkplain RegionError#NON_FINITE_TIME}if the
		 *             arrival time is not finite</li>
		 *             
		 *              <li>{@linkplain RegionError#NULL_TIME}if the
		 *             arrival time is null</li>
		 * 
		 *             <li>{@linkplain RegionError#REGION_ARRIVAL_TIMES_MISMATCHED}
		 *             if other people have been added without using region
		 *             arrival times</li>
		 */
		public Builder addPerson(final PersonId personId, final RegionId regionId, final Double arrivalTime) {
			ensureDataMutability();
			validatePersonId(personId);
			validateRegionIdNotNull(regionId);
			validateTime(arrivalTime);
			setPersonAdditionMode(PersonAdditionMode.REGION_AND_TIME);
			int personIndex = personId.getValue();
			while (personIndex >= data.personRegions.size()) {
				data.personRegions.add(null);
				data.personArrivalTimes.add(null);
			}
			data.personRegions.set(personIndex, regionId);
			data.personArrivalTimes.set(personIndex, arrivalTime);
			return this;
		}

		/**
		 * Sets the tracking policy for region arrival times. Defaults to false.
		 * Must be set to true if people are added with arrival times.
		 * 
		 * @throws ContractException
		 *
		 */
		public Builder setPersonRegionArrivalTracking(boolean trackRegionArrivalTimes) {
			ensureDataMutability();
			data.trackRegionArrivalTimes = trackRegionArrivalTimes;
			return this;
		}

		/**
		 * Adds the region id and its associated agent initial behavior.
		 * 
		 * @throws ContractException
		 * 
		 *             <li>{@linkplain RegionError#NULL_REGION_ID}</li>if the
		 *             region id is null
		 */
		public Builder addRegion(final RegionId regionId) {
			ensureDataMutability();
			validateRegionIdNotNull(regionId);
			data.regionIds.add(regionId);
			return this;
		}

		/**
		 * Defines a region property
		 * 
		 * @throws ContractException
		 * 
		 *             <li>{@linkplain PropertyError#NULL_PROPERTY_ID}</li> if
		 *             the region property id is null
		 * 
		 *             <li>{@linkplain PropertyError#NULL_PROPERTY_DEFINITION}
		 *             </li> if the property definition is null
		 * 
		 */
		public Builder defineRegionProperty(final RegionPropertyId regionPropertyId, final PropertyDefinition propertyDefinition) {
			ensureDataMutability();
			validateRegionPropertyIdNotNull(regionPropertyId);
			validateRegionPropertyDefinitionNotNull(propertyDefinition);
			data.regionPropertyDefinitions.put(regionPropertyId, propertyDefinition);
			return this;
		}

		private void validateData() {

			// if the time tracking policy is off, then there should be no times
			// recorded

			switch (data.personAdditionMode) {
			case REGION_AND_TIME:
				if (!data.trackRegionArrivalTimes) {
					throw new ContractException(RegionError.PERSON_ARRIVAL_DATA_PRESENT);
				}
				break;
			case REGION_ONLY:
				if (data.trackRegionArrivalTimes) {
					throw new ContractException(RegionError.MISSING_PERSON_ARRIVAL_DATA);
				}
				break;
			case UNDETERMINED:
				// nothing to check
				break;
			default:
				throw new RuntimeException("unhandled case " + data.personAdditionMode);
			}

			for (RegionId regionId : data.personRegions) {
				if (regionId != null) {
					if (!data.regionIds.contains(regionId)) {
						throw new ContractException(RegionError.UNKNOWN_REGION_ID, regionId + " in person region assignments");
					}
				}
			}

			for (RegionId regionId : data.regionPropertyValues.keySet()) {
				if (!data.regionIds.contains(regionId)) {
					throw new ContractException(RegionError.UNKNOWN_REGION_ID, regionId + " in region property values");
				}
				Map<RegionPropertyId, Object> map = data.regionPropertyValues.get(regionId);
				if (map != null) {
					for (RegionPropertyId regionPropertyId : map.keySet()) {
						PropertyDefinition propertyDefinition = data.regionPropertyDefinitions.get(regionPropertyId);
						if (propertyDefinition == null) {
							throw new ContractException(PropertyError.UNKNOWN_PROPERTY_ID, regionPropertyId + " for region " + regionId);
						}
						Object propertyValue = map.get(regionPropertyId);
						if (!propertyDefinition.getType().isAssignableFrom(propertyValue.getClass())) {
							throw new ContractException(PropertyError.INCOMPATIBLE_VALUE, regionId + ":" + regionPropertyId + " = " + propertyValue);
						}
					}
				}
			}

			/*
			 * For every region property definition that has a null default
			 * value, ensure that there all corresponding region property values
			 * are not null.
			 */
			for (RegionPropertyId regionPropertyId : data.regionPropertyDefinitions.keySet()) {
				PropertyDefinition propertyDefinition = data.regionPropertyDefinitions.get(regionPropertyId);
				if (!propertyDefinition.getDefaultValue().isPresent()) {
					for (RegionId regionId : data.regionIds) {
						Object propertyValue = null;
						Map<RegionPropertyId, Object> propertyValueMap = data.regionPropertyValues.get(regionId);
						if (propertyValueMap != null) {
							propertyValue = propertyValueMap.get(regionPropertyId);
						}
						if (propertyValue == null) {
							throw new ContractException(PropertyError.INSUFFICIENT_PROPERTY_VALUE_ASSIGNMENT, regionPropertyId);
						}
					}
				}
			}

		}

	}

	private final Data data;

	private RegionsPluginData(Data data) {
		this.data = data;
	}

	/**
	 * Returns the {@link PropertyDefinition} for the given
	 * {@link RegionPropertyId}.
	 * 
	 * @throws ContractException
	 * 
	 *             <li>{@linkplain PropertyError#NULL_PROPERTY_ID}</li> if the
	 *             region property id is null
	 *             <li>{@linkplain PropertyError#UNKNOWN_PROPERTY_ID}</li> if
	 *             the region property id is known
	 */
	public PropertyDefinition getRegionPropertyDefinition(final RegionPropertyId regionPropertyId) {
		validateRegionPropertyIdNotNull(regionPropertyId);
		validateRegionPropertyIsDefined(data, regionPropertyId);
		return data.regionPropertyDefinitions.get(regionPropertyId);
	}

	/**
	 * Returns the set of {@link RegionPropertyId}
	 * 
	 */
	@SuppressWarnings("unchecked")
	public <T extends RegionPropertyId> Set<T> getRegionPropertyIds() {
		Set<T> result = new LinkedHashSet<>();
		for (RegionPropertyId regionPropertyId : data.regionPropertyDefinitions.keySet()) {
			result.add((T) regionPropertyId);
		}
		return result;
	}

	/**
	 * Returns the property value for the given {@link RegionId} and
	 * {@link RegionPropertyId}.
	 * 
	 * @throws ContractException
	 * 
	 *             <li>{@linkplain RegionError#NULL_REGION_ID}</li> if the
	 *             region id is null
	 *             <li>{@linkplain RegionError#UNKNOWN_REGION_ID}</li> if the
	 *             region id is unknown
	 */
	public Map<RegionPropertyId, Object> getRegionPropertyValues(final RegionId regionId) {
		validateRegionExists(data, regionId);
		final Map<RegionPropertyId, Object> map = data.regionPropertyValues.get(regionId);
		if (map == null) {
			return data.emptyRegionPropertyMap;
		}
		return Collections.unmodifiableMap(map);
	}

	/**
	 * Returns the set of {@link RegionId} values contained in this initial
	 * data. Each region id will correspond to a region agent that is
	 * automatically added to the simulation during initialization.
	 */
	public Set<RegionId> getRegionIds() {
		return Collections.unmodifiableSet(data.regionIds);
	}

	/**
	 * Returns the time tracking Policy}. Defaulted to
	 * false if not set in the builder.
	 * 
	 */
	public boolean getPersonRegionArrivalTrackingPolicy() {
		return data.trackRegionArrivalTimes;
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

	private static void validateTime(Double time) {
		if(time == null) {
			throw new ContractException(RegionError.NULL_TIME);
		}
		if (!Double.isFinite(time)) {
			throw new ContractException(RegionError.NON_FINITE_TIME);
		}
	}

	/**
	 * Returns the largest id value of any person assigned a region.
	 */
	public int getPersonCount() {
		return FastMath.max(data.personRegions.size(), data.personArrivalTimes.size());
	}

	/**
	 * Returns the {@link RegionId} for the given {@link PersonId}.
	 * 
	 * @throws ContractException
	 * 
	 *             <li>{@linkplain PersonError#NULL_PERSON_ID}</li> if the
	 *             person id is null
	 * 
	 */
	@SuppressWarnings("unchecked")
	public <T extends RegionId> Optional<T> getPersonRegion(final PersonId personId) {
		validatePersonId(personId);

		int personIndex = personId.getValue();
		if (personIndex < data.personRegions.size()) {
			return Optional.ofNullable((T) data.personRegions.get(personIndex));
		}
		return Optional.empty();
	}

	/**
	 * Returns the region arrival time for the given {@link PersonId}.
	 * 
	 * @throws ContractException
	 * 
	 *             <li>{@linkplain PersonError#NULL_PERSON_ID}</li> if the
	 *             person id is null
	 * 
	 */
	public Optional<Double> getPersonRegionArrivalTime(final PersonId personId) {
		validatePersonId(personId);
		int personIndex = personId.getValue();
		if (personIndex < data.personArrivalTimes.size()) {
			return Optional.ofNullable(data.personArrivalTimes.get(personIndex));
		}
		return Optional.empty();
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
		if (!(obj instanceof RegionsPluginData)) {
			return false;
		}
		RegionsPluginData other = (RegionsPluginData) obj;
		if (!data.equals(other.data)) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		StringBuilder builder2 = new StringBuilder();
		builder2.append("RegionsPluginData [data=");
		builder2.append(data);
		builder2.append("]");
		return builder2.toString();
	}
	
	

}
