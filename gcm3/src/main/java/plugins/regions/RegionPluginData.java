package plugins.regions;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import net.jcip.annotations.Immutable;
import nucleus.PluginData;
import nucleus.PluginDataBuilder;
import nucleus.util.ContractException;
import plugins.regions.support.RegionError;
import plugins.regions.support.RegionId;
import plugins.regions.support.RegionPropertyId;
import plugins.util.properties.PropertyDefinition;
import plugins.util.properties.PropertyError;
import plugins.util.properties.TimeTrackingPolicy;

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
 * @author Shawn Hatch
 *
 */

@Immutable
public class RegionPluginData implements PluginData {

	private static class Data {

		private final Map<RegionPropertyId, PropertyDefinition> regionPropertyDefinitions = new LinkedHashMap<>();

		private final Set<RegionId> regionIds = new LinkedHashSet<>();

		private TimeTrackingPolicy regionArrivalTimeTrackingPolicy;

		private final Map<RegionId, Map<RegionPropertyId, Object>> regionPropertyValues = new LinkedHashMap<>();

		public Data() {
		}

		public Data(Data data) {
			regionPropertyDefinitions.putAll(data.regionPropertyDefinitions);
			regionIds.addAll(data.regionIds);
			regionArrivalTimeTrackingPolicy = data.regionArrivalTimeTrackingPolicy;
			for (RegionId regionId : data.regionPropertyValues.keySet()) {
				Map<RegionPropertyId, Object> map = new LinkedHashMap<>(data.regionPropertyValues.get(regionId));
				regionPropertyValues.put(regionId, map);
			}
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

	private static void validateData(Data data) {


		for (RegionId regionId : data.regionPropertyValues.keySet()) {
			if (!data.regionIds.contains(regionId)) {
				throw new ContractException(RegionError.UNKNOWN_REGION_ID, regionId + " in region property values");
			}
			Map<RegionPropertyId, Object> map = data.regionPropertyValues.get(regionId);
			if (map != null) {
				for (RegionPropertyId regionPropertyId : map.keySet()) {
					PropertyDefinition propertyDefinition = data.regionPropertyDefinitions.get(regionPropertyId);
					if (propertyDefinition == null) {
						throw new ContractException(RegionError.UNKNOWN_REGION_PROPERTY_ID, regionPropertyId + " for region " + regionId);
					}
					Object propertyValue = map.get(regionPropertyId);
					if (!propertyDefinition.getType().isAssignableFrom(propertyValue.getClass())) {
						throw new ContractException(PropertyError.INCOMPATIBLE_VALUE, regionId + ":" + regionPropertyId + " = " + propertyValue);
					}
				}
			}
		}

		/*
		 * For every region property definition that has a null default value,
		 * ensure that there all corresponding region property values are not
		 * null and repair the definition.
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
						throw new ContractException(RegionError.INSUFFICIENT_REGION_PROPERTY_VALUE_ASSIGNMENT, regionPropertyId);
					}
				}
			}
		}

	}

	private static void validateTimeTrackingPolicyNotNull(TimeTrackingPolicy timeTrackingPolicy) {
		if (timeTrackingPolicy == null) {
			throw new ContractException(RegionError.NULL_TIME_TRACKING_POLICY);
		}
	}

	private static void validatePersonRegionArrivalTrackingNotSet(Data data) {
		if (data.regionArrivalTimeTrackingPolicy != null) {
			throw new ContractException(RegionError.DUPLICATE_TIME_TRACKING_POLICY);
		}
	}

	private static void validateRegionPropertyValueNotSet(final Data data, final RegionId regionId, final RegionPropertyId regionPropertyId) {
		final Map<RegionPropertyId, Object> propertyMap = data.regionPropertyValues.get(regionId);
		if (propertyMap != null) {
			if (propertyMap.containsKey(regionPropertyId)) {
				throw new ContractException(RegionError.DUPLICATE_REGION_PROPERTY_VALUE, regionPropertyId + " = " + regionId);
			}
		}
	}


	private static void validateRegionIdNotNull(RegionId regionId) {
		if (regionId == null) {
			throw new ContractException(RegionError.NULL_REGION_ID);
		}
	}

	private static void validateRegionPropertyIdNotNull(RegionPropertyId regionPropertyId) {
		if (regionPropertyId == null) {
			throw new ContractException(RegionError.NULL_REGION_PROPERTY_ID);
		}
	}

	private static void validateRegionPropertyDefinitionNotNull(PropertyDefinition propertyDefinition) {
		if (propertyDefinition == null) {
			throw new ContractException(RegionError.NULL_REGION_PROPERTY_DEFINITION);
		}

	}

	private static void validateRegionPropertyIsDefined(Data data, RegionPropertyId regionPropertyId) {

		if (!data.regionPropertyDefinitions.containsKey(regionPropertyId)) {
			throw new ContractException(RegionError.UNKNOWN_REGION_PROPERTY_ID);
		}
	}

	private static void validateRegionPropertyIsNotDefined(Data data, RegionPropertyId regionPropertyId) {
		if (data.regionPropertyDefinitions.containsKey(regionPropertyId)) {
			throw new ContractException(RegionError.DUPLICATE_REGION_PROPERTY_DEFINITION_ASSIGNMENT);
		}
	}

	public static class Builder implements PluginDataBuilder {
		private Data data;

		private Builder(Data data) {
			this.data = data;
		}

		/**
		 * Returns the {@link RegionInitialData} from the collected information
		 * supplied to this builder.
		 * 
		 * @throws ContractException
		 * 
		 *             <li>{@linkplain RegionError#UNKNOWN_REGION_ID}</li> if a
		 *             region property value was associated with a region id
		 *             that was not properly added with an initial agent
		 *             behavior.
		 * 
		 *             <li>{@linkplain RegionError#UNKNOWN_REGION_PROPERTY_ID}</li>
		 *             if a region property value was associated with a region
		 *             property id that was not defined
		 * 
		 *             <li>{@linkplain PropertyError#INCOMPATIBLE_VALUE}</li> if
		 *             a region property value was associated with a region and
		 *             region property id that is incompatible with the
		 *             corresponding property definition.
		 * 
		 *             <li>{@linkplain RegionError#INSUFFICIENT_REGION_PROPERTY_VALUE_ASSIGNMENT}</li>
		 *             if a region property definition does not have a default
		 *             value and there are no property values added to replace
		 *             that default.
		 * 
		 */
		public RegionPluginData build() {
			try {
				if (data.regionArrivalTimeTrackingPolicy == null) {
					data.regionArrivalTimeTrackingPolicy = TimeTrackingPolicy.DO_NOT_TRACK_TIME;
				}
				validateData(data);
				return new RegionPluginData(data);
			} finally {
				data = new Data();
			}
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
		 *             <li>{@linkplain RegionError#NULL_REGION_PROPERTY_ID}
		 *             </li>if the region property id is null
		 * 
		 *             <li>{@linkplain RegionError#DUPLICATE_REGION_PROPERTY_VALUE}
		 *             </li>if the region property value was previously defined
		 * 
		 */
		public Builder setRegionPropertyValue(final RegionId regionId, final RegionPropertyId regionPropertyId, final Object regionPropertyValue) {
			validateRegionIdNotNull(regionId);
			validateRegionPropertyIdNotNull(regionPropertyId);
			validateRegionPropertyValueNotSet(data, regionId, regionPropertyId);
			Map<RegionPropertyId, Object> propertyMap = data.regionPropertyValues.get(regionId);
			if (propertyMap == null) {
				propertyMap = new LinkedHashMap<>();
				data.regionPropertyValues.put(regionId, propertyMap);
			}
			propertyMap.put(regionPropertyId, regionPropertyValue);
			return this;
		}

		/**
		 * Sets the tracking policy for region arrival times
		 * 
		 * @throws ContractException
		 * 
		 *             <li>{@linkplain RegionError#NULL_TIME_TRACKING_POLICY}</li>if
		 *             the timeTrackingPolicy is null
		 * 
		 *             <li>{@linkplain RegionError#DUPLICATE_TIME_TRACKING_POLICY}
		 *             </li>if the timeTrackingPolicy was previously defined
		 * 
		 */
		public Builder setPersonRegionArrivalTracking(final TimeTrackingPolicy timeTrackingPolicy) {
			validateTimeTrackingPolicyNotNull(timeTrackingPolicy);
			validatePersonRegionArrivalTrackingNotSet(data);
			data.regionArrivalTimeTrackingPolicy = timeTrackingPolicy;
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
			validateRegionIdNotNull(regionId);
			data.regionIds.add(regionId);
			return this;
		}

		/**
		 * Defines a compartment property
		 * 
		 * @throws ContractException
		 * 
		 *             <li>{@linkplain RegionError#NULL_REGION_PROPERTY_ID}</li>
		 *             if the region property id is null
		 * 
		 *             <li>{@linkplain RegionError#NULL_REGION_PROPERTY_DEFINITION}
		 *             </li> if the property definition is null
		 *
		 *             <li>{@linkplain RegionError#DUPLICATE_REGION_PROPERTY_DEFINITION_ASSIGNMENT}
		 *             </li> if a property definition for the given property id
		 *             was previously defined.
		 * 
		 */
		public Builder defineRegionProperty(final RegionPropertyId regionPropertyId, final PropertyDefinition propertyDefinition) {
			validateRegionPropertyIdNotNull(regionPropertyId);
			validateRegionPropertyDefinitionNotNull(propertyDefinition);
			validateRegionPropertyIsNotDefined(data, regionPropertyId);
			data.regionPropertyDefinitions.put(regionPropertyId, propertyDefinition);
			return this;
		}

	}

	private final Data data;

	private RegionPluginData(Data data) {
		this.data = data;
	}

	/**
	 * Returns the {@link PropertyDefinition} for the given
	 * {@link RegionPropertyId}.
	 * 
	 * @throws ContractException
	 * 
	 *             <li>{@linkplain RegionError#NULL_REGION_PROPERTY_ID}</li> if
	 *             the region property id is null
	 *             <li>{@linkplain RegionError#UNKNOWN_REGION_PROPERTY_ID}</li>
	 *             if the region property id is known
	 */
	public PropertyDefinition getRegionPropertyDefinition(final RegionPropertyId regionPropertyId) {
		validateRegionPropertyIdNotNull(regionPropertyId);
		validateRegionPropertyIsDefined(data, regionPropertyId);
		return data.regionPropertyDefinitions.get(regionPropertyId);
	}

	/**
	 * Returns the set of {@link CompartmentPropertyId}
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
	 *             <li>{@linkplain RegionError#NULL_REGION_PROPERTY_ID}</li> if
	 *             the region property id is null
	 *             <li>{@linkplain RegionError#UNKNOWN_REGION_PROPERTY_ID}</li>
	 *             if the region property id is known
	 */
	@SuppressWarnings("unchecked")
	public <T> T getRegionPropertyValue(final RegionId regionId, final RegionPropertyId regionPropertyId) {
		validateRegionExists(data, regionId);
		validateRegionPropertyIdNotNull(regionPropertyId);
		validateRegionPropertyIsDefined(data, regionPropertyId);
		Object result = null;
		final Map<RegionPropertyId, Object> map = data.regionPropertyValues.get(regionId);
		if (map != null) {
			result = map.get(regionPropertyId);
		}
		if (result == null) {
			final PropertyDefinition propertyDefinition = data.regionPropertyDefinitions.get(regionPropertyId);
			/*
			 * we verified earlier that every region property either has a value
			 * or has a property definition that has a default value
			 */
			result = propertyDefinition.getDefaultValue().get();
		}
		return (T) result;
	}

	
	/**
	 * Returns the set of {@link RegionId} values contained in this initial
	 * data. Each region id will correspond to a region agent that is
	 * automatically added to the simulation during initialization.
	 */
	@SuppressWarnings("unchecked")
	public <T extends RegionId> Set<T> getRegionIds() {
		Set<T> result = new LinkedHashSet<>();
		for(RegionId regionId : data.regionIds) {
			result.add((T)regionId);
		}
		return result;
	}

	/**
	 * Returns the {@link TimeTrackingPolicy}. Defaulted to
	 * {@link TimeTrackingPolicy#DO_NOT_TRACK_TIME} if not set in the builder.
	 * 
	 */
	public TimeTrackingPolicy getPersonRegionArrivalTrackingPolicy() {
		return data.regionArrivalTimeTrackingPolicy;
	}

	@Override
	public PluginDataBuilder getCloneBuilder() {
		return new Builder(new Data(data));
	}
}
