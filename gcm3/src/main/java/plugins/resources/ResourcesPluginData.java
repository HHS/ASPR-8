package plugins.resources;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import net.jcip.annotations.Immutable;
import nucleus.PluginData;
import nucleus.PluginDataBuilder;
import plugins.people.support.PersonError;
import plugins.people.support.PersonId;
import plugins.regions.support.RegionError;
import plugins.regions.support.RegionId;
import plugins.resources.support.ResourceError;
import plugins.resources.support.ResourceId;
import plugins.resources.support.ResourcePropertyId;
import plugins.util.properties.PropertyDefinition;
import plugins.util.properties.TimeTrackingPolicy;
import util.errors.ContractException;

/**
 * An immutable container of the initial state of resources. It contains: <BR>
 * <ul>
 * <li>resource ids</li>
 * <li>resource property definitions</li>
 * <li>region resource levels</li>
 * <li>person resource levels</li>
 * </ul>
 * 
 * @author Shawn Hatch
 *
 */
@Immutable
public final class ResourcesPluginData implements PluginData {

	private static class Data {

		private final Map<ResourceId, Map<ResourcePropertyId, PropertyDefinition>> resourcePropertyDefinitions;

		private final Map<ResourceId, Map<ResourcePropertyId, Object>> resourcePropertyValues;

		private final Map<PersonId, Map<ResourceId, Long>> personResourceLevels;

		private final Set<ResourceId> resourceIds;

		private final Map<RegionId, Map<ResourceId, Long>> regionResourceLevels;

		private final Map<ResourceId, TimeTrackingPolicy> resourceTimeTrackingPolicies;
		
		public Data() {
			resourcePropertyDefinitions = new LinkedHashMap<>();
			resourcePropertyValues = new LinkedHashMap<>();
			personResourceLevels = new LinkedHashMap<>();
			resourceIds = new LinkedHashSet<>();
			regionResourceLevels = new LinkedHashMap<>();
			resourceTimeTrackingPolicies = new LinkedHashMap<>();
		}
		
		public Data(Data data) {
			
            resourcePropertyDefinitions = new LinkedHashMap<>();
            for(ResourceId resourceId : data.resourcePropertyDefinitions.keySet()) {
            	Map<ResourcePropertyId, PropertyDefinition> map = data.resourcePropertyDefinitions.get(resourceId);
            	Map<ResourcePropertyId, PropertyDefinition> newMap = new LinkedHashMap<>(map);
            	resourcePropertyDefinitions.put(resourceId, newMap);
            }

			resourcePropertyValues = new LinkedHashMap<>();
			for(ResourceId resourceId : data.resourcePropertyValues.keySet()) {
				Map<ResourcePropertyId, Object> map = data.resourcePropertyValues.get(resourceId);
				Map<ResourcePropertyId, Object> newMap = new LinkedHashMap<>(map);
				resourcePropertyValues.put(resourceId, newMap);
			}

			personResourceLevels = new LinkedHashMap<>();
			for(PersonId personId : data.personResourceLevels.keySet()) {
				Map<ResourceId, Long> map = data.personResourceLevels.get(personId);
				Map<ResourceId, Long> newMap = new LinkedHashMap<>(map);
				personResourceLevels.put(personId, newMap);
			}

			resourceIds = new LinkedHashSet<>(data.resourceIds);
			
			
			regionResourceLevels = new LinkedHashMap<>();
			for(RegionId regionId : data.regionResourceLevels.keySet()) {
				Map<ResourceId, Long> map = data.regionResourceLevels.get(regionId);
				Map<ResourceId, Long> newMap = new LinkedHashMap<>(map);
				regionResourceLevels.put(regionId, newMap);
			}

			resourceTimeTrackingPolicies = new LinkedHashMap<>(data.resourceTimeTrackingPolicies);
			
		}
		
	}

	private final Data data;

	private ResourcesPluginData(Data data) {
		this.data = data;
	}

	/**
	 * Returns a new builder instance
	 */
	public static Builder builder() {
		return new Builder(new Data());
	}

	private static void validateResourceIdNotNull(ResourceId resourceId) {
		if (resourceId == null) {
			throw new ContractException(ResourceError.NULL_RESOURCE_ID);
		}
	}

	private static void validateTimeTrackingPolicyNotNull(TimeTrackingPolicy timeTrackingPolicy) {
		if (timeTrackingPolicy == null) {
			throw new ContractException(ResourceError.NULL_TIME_TRACKING_POLICY);
		}
	}

	private static void validateResourceTimeTrackingNotSet(final Data data, final ResourceId resourceId) {
		if (data.resourceTimeTrackingPolicies.get(resourceId) != null) {
			throw new ContractException(ResourceError.DUPLICATE_TIME_TRACKING_POLICY_ASSIGNMENT);
		}
	}

	private static void validateRegionResourceNotSet(final Data data, final RegionId regionId, final ResourceId resourceId) {
		final Map<ResourceId, Long> resourceLevelMap = data.regionResourceLevels.get(regionId);
		if (resourceLevelMap != null) {
			if (resourceLevelMap.containsKey(resourceId)) {
				throw new ContractException(ResourceError.DUPLICATE_REGION_RESOURCE_LEVEL_ASSIGNMENT, resourceId + ": " + regionId);
			}
		}
	}

	private static void validateRegionIdNotNull(RegionId regionId) {
		if (regionId == null) {
			throw new ContractException(RegionError.NULL_REGION_ID);
		}
	}

	private static void validatePersonIdNotNull(PersonId personId) {
		if (personId == null) {
			throw new ContractException(PersonError.NULL_PERSON_ID);
		}
	}

	private static void validateResourceAmount(final long amount) {
		if (amount < 0) {
			throw new ContractException(ResourceError.NEGATIVE_RESOURCE_AMOUNT, amount);
		}
	}

	private static void validatePersonResourceLevelNotSet(final Data data, final PersonId personId, final ResourceId resourceId) {
		final Map<ResourceId, Long> resourceLevelMap = data.personResourceLevels.get(personId);
		if (resourceLevelMap != null) {
			if (resourceLevelMap.containsKey(resourceId)) {
				throw new ContractException(ResourceError.DUPLICATE_PERSON_RESOURCE_LEVEL_ASSIGNMENT, resourceId + ": " + personId);
			}
		}
	}

	private static void validateResourcePropertyIdNotNull(ResourcePropertyId resourcePropertyId) {
		if (resourcePropertyId == null) {
			throw new ContractException(ResourceError.NULL_RESOURCE_PROPERTY_ID);
		}
	}

	private static void validateResourcePropertyValueNotNull(Object resourcePropertyValue) {
		if (resourcePropertyValue == null) {
			throw new ContractException(ResourceError.NULL_RESOURCE_PROPERTY_VALUE);
		}
	}

	private static void validateResourcePropertyValueNotSet(final Data data, final ResourceId resourceId, final ResourcePropertyId resourcePropertyId) {
		final Map<ResourcePropertyId, Object> propertyMap = data.resourcePropertyValues.get(resourceId);
		if (propertyMap != null) {
			if (propertyMap.containsKey(resourcePropertyId)) {
				throw new ContractException(ResourceError.DUPLICATE_RESOURCE_PROPERTY_VALUE_ASSIGNMENT, resourcePropertyId + ": " + resourceId);
			}
		}
	}

	private static void validateResourcePropertyDefintionNotNull(PropertyDefinition propertyDefinition) {
		if (propertyDefinition == null) {
			throw new ContractException(ResourceError.NULL_RESOURCE_PROPERTY_DEFINITION);
		}
	}

	private static void validateResourcePropertyIsNotDefined(final Data data, final ResourceId resourceId, final ResourcePropertyId resourcePropertyId) {
		final Map<ResourcePropertyId, PropertyDefinition> defMap = data.resourcePropertyDefinitions.get(resourceId);
		if (defMap != null) {
			final PropertyDefinition propertyDefinition = defMap.get(resourcePropertyId);
			if (propertyDefinition != null) {
				throw new ContractException(ResourceError.DUPLICATE_RESOURCE_PROPERTY_DEFINITION, resourcePropertyId);
			}
		}
	}

	private static void validateResourceDoesNotExist(final Data data, final ResourceId resourceId) {
		if (data.resourceIds.contains(resourceId)) {
			throw new ContractException(ResourceError.DUPLICATE_RESOURCE_ID, resourceId);
		}
	}

	/**
	 * Builder class for ResourceInitialData
	 * 
	 * @author Shawn Hatch
	 *
	 */
	public static class Builder implements PluginDataBuilder{
		private Data data;

		private Builder(Data data) {
			this.data = data;
		}

		/**
		 * Returns the ResourceInitialData built from the collected data.
		 * 
		 * @throws ContractException
		 *
		 * 
		 *             1
		 *             <li>{@linkplain ResourceError#UNKNOWN_RESOURCE_ID} if a
		 *             resource tracking policy was collected for a resource
		 *             that was not added</li>
		 * 
		 *             2
		 *             <li>{@linkplain ResourceError#UNKNOWN_RESOURCE_ID} if a
		 *             resource property definition was collected for a resource
		 *             that was not added</li>
		 * 
		 *             3
		 *             <li>{@linkplain ResourceError#UNKNOWN_RESOURCE_PROPERTY_ID}
		 *             if a resource property value was collected for a resource
		 *             that was not added</li>
		 * 
		 *             4
		 *             <li>{@linkplain ResourceError#UNKNOWN_RESOURCE_PROPERTY_ID}
		 *             if a resource property value was collected for a resource
		 *             property that is not associated with the given resource
		 *             id</li>
		 * 
		 *             5
		 *             <li>{@linkplain ResourceError#INCOMPATIBLE_VALUE} if a
		 *             resource property value was collected for a resource
		 *             property that is not compatible with the associated
		 *             resource property definition</li>
		 * 
		 *             6
		 *             <li>{@linkplain ResourceError#INSUFFICIENT_RESOURCE_PROPERTY_VALUE_ASSIGNMENT}
		 *             if a resource property definition has a null default
		 *             value and there is no assigned resource property value
		 *             for that resource</li>
		 * 
		 * 
		 *             7
		 *             <li>{@linkplain ResourceError#UNKNOWN_RESOURCE_ID} if a
		 *             resource level was collected for a person that is an
		 *             unknown resource id</li>
		 * 
		 *             8
		 *             <li>{@linkplain ResourceError#UNKNOWN_RESOURCE_ID} if a
		 *             resource level was collected for a region that is an
		 *             unknown resource id</li>
		 * 
		 */
		public ResourcesPluginData build() {
			try {
				for (final ResourceId resourceId : data.resourceIds) {
					final TimeTrackingPolicy timeTrackingPolicy = data.resourceTimeTrackingPolicies.get(resourceId);
					if (timeTrackingPolicy == null) {
						data.resourceTimeTrackingPolicies.put(resourceId, TimeTrackingPolicy.DO_NOT_TRACK_TIME);
					}
				}

				validateData(data);
				return new ResourcesPluginData(data);
			} finally {
				data = new Data();
			}
		}

		/**
		 * Adds the given resouce id.
		 * 
		 * @throws ContractException
		 *             <li>{@linkplain ResourceError#NULL_RESOURCE_ID} if the
		 *             resource id is null</li>
		 *             <li>{@linkplain ResourceError#DUPLICATE_RESOURCE_ID} if
		 *             the resource id was previously added</li>
		 */
		public Builder addResource(final ResourceId resourceId) {
			validateResourceIdNotNull(resourceId);
			validateResourceDoesNotExist(data, resourceId);
			data.resourceIds.add(resourceId);
			return this;
		}

		/**
		 * Defines a resource property
		 * 
		 * @throws ContractException
		 *             <li>{@linkplain ResourceError#NULL_RESOURCE_ID} if the
		 *             resource id is null</li>
		 * 
		 *             <li>{@linkplain ResourceError#NULL_RESOURCE_PROPERTY_ID}
		 *             </li> if the resource property id is null
		 * 
		 *             <li>{@linkplain ResourceError#NULL_RESOURCE_PROPERTY_DEFINITION}
		 *             </li> if the property definition is null
		 *
		 *             <li>{@linkplain ResourceError#DUPLICATE_RESOURCE_PROPERTY_DEFINITION}
		 *             </li> if a resource property definition for the given
		 *             resource id and property id was previously defined.
		 * 
		 */
		public Builder defineResourceProperty(final ResourceId resourceId, final ResourcePropertyId resourcePropertyId, final PropertyDefinition propertyDefinition) {
			validateResourceIdNotNull(resourceId);
			validateResourcePropertyIdNotNull(resourcePropertyId);
			validateResourcePropertyDefintionNotNull(propertyDefinition);
			validateResourcePropertyIsNotDefined(data, resourceId, resourcePropertyId);
			Map<ResourcePropertyId, PropertyDefinition> map = data.resourcePropertyDefinitions.get(resourceId);
			if (map == null) {
				map = new LinkedHashMap<>();
				data.resourcePropertyDefinitions.put(resourceId, map);
			}
			map.put(resourcePropertyId, propertyDefinition);
			return this;
		}

		/**
		 * Sets a person's initial resource level
		 * 
		 * @throws ContractException
		 *             <li>{@linkplain PersonError#NULL_PERSON_ID} if the person
		 *             id is null</li>
		 *             <li>{@linkplain ResourceError#NULL_RESOURCE_ID} if the
		 *             resource id is null</li>
		 *             <li>{@linkplain ResourceError#NEGATIVE_RESOURCE_AMOUNT}
		 *             if the resource amount is negative</li> *
		 *             <li>{@linkplain ResourceError#DUPLICATE_PERSON_RESOURCE_LEVEL_ASSIGNMENT}
		 *             if the person's resource level was previously
		 *             assigned</li>
		 */

		public Builder setPersonResourceLevel(final PersonId personId, final ResourceId resourceId, final long amount) {
			validatePersonIdNotNull(personId);
			validateResourceIdNotNull(resourceId);
			validateResourceAmount(amount);
			validatePersonResourceLevelNotSet(data, personId, resourceId);
			Map<ResourceId, Long> resourceLevelMap = data.personResourceLevels.get(personId);
			if (resourceLevelMap == null) {
				resourceLevelMap = new LinkedHashMap<>();
				data.personResourceLevels.put(personId, resourceLevelMap);
			}
			resourceLevelMap.put(resourceId, amount);
			return this;
		}

		/**
		 * Sets a region's initial resource level
		 * 
		 * @throws ContractException
		 *             <li>{@linkplain RegionError#NULL_REGION_ID} if the region
		 *             id is null</li>
		 *             <li>{@linkplain ResourceError#NULL_RESOURCE_ID} if the
		 *             resource id is null</li>
		 *             <li>{@linkplain ResourceError#NEGATIVE_RESOURCE_AMOUNT}
		 *             if the resource amount is negative</li> *
		 *             <li>{@linkplain ResourceError#DUPLICATE_REGION_RESOURCE_LEVEL_ASSIGNMENT}
		 *             if the region's resource level was previously
		 *             assigned</li>
		 */

		public Builder setRegionResourceLevel(final RegionId regionId, final ResourceId resourceId, final long amount) {
			validateRegionIdNotNull(regionId);
			validateResourceIdNotNull(resourceId);
			validateRegionResourceNotSet(data, regionId, resourceId);
			validateResourceAmount(amount);
			Map<ResourceId, Long> resourceLevelMap = data.regionResourceLevels.get(regionId);
			if (resourceLevelMap == null) {
				resourceLevelMap = new LinkedHashMap<>();
				data.regionResourceLevels.put(regionId, resourceLevelMap);
			}
			resourceLevelMap.put(resourceId, amount);
			return this;
		}

		/**
		 * Sets a resource property value
		 * 
		 * @throws ContractException
		 *             <li>{@linkplain ResourceError#NULL_RESOURCE_ID} if the
		 *             resource id is null</li>
		 *             <li>{@linkplain ResourceError#NULL_RESOURCE_PROPERTY_ID}
		 *             if the resource property id is null</li>
		 *             <li>{@linkplain ResourceError#NULL_RESOURCE_PROPERTY_VALUE}
		 *             if the resource property value is null</li>
		 *             <li>{@linkplain ResourceError#DUPLICATE_RESOURCE_PROPERTY_VALUE_ASSIGNMENT}
		 *             if the resource property value was previously
		 *             assigned</li>
		 */
		public Builder setResourcePropertyValue(final ResourceId resourceId, final ResourcePropertyId resourcePropertyId, final Object resourcePropertyValue) {
			validateResourceIdNotNull(resourceId);
			validateResourcePropertyIdNotNull(resourcePropertyId);
			validateResourcePropertyValueNotNull(resourcePropertyValue);
			validateResourcePropertyValueNotSet(data, resourceId, resourcePropertyId);
			

			Map<ResourcePropertyId, Object> propertyMap = data.resourcePropertyValues.get(resourceId);
			if (propertyMap == null) {
				propertyMap = new LinkedHashMap<>();
				data.resourcePropertyValues.put(resourceId, propertyMap);
			}
			propertyMap.put(resourcePropertyId, resourcePropertyValue);
			return this;
		}

		/**
		 * Sets the time tracking policy for a resource
		 * 
		 * @throws ContractException
		 *             <li>{@linkplain ResourceError#NULL_RESOURCE_ID} if the
		 *             resource id is null</li>
		 *             <li>{@linkplain ResourceError.NULL_TIME_TRACKING_POLICY}
		 *             if the tracking policy is null</li>
		 *             <li>{@linkplain ResourceError#DUPLICATE_TIME_TRACKING_POLICY_ASSIGNMENT}
		 *             if the resource tracking policy was previously
		 *             assigned</li>
		 */
		public Builder setResourceTimeTracking(final ResourceId resourceId, final TimeTrackingPolicy trackValueAssignmentTimes) {
			validateResourceIdNotNull(resourceId);
			validateTimeTrackingPolicyNotNull(trackValueAssignmentTimes);
			validateResourceTimeTrackingNotSet(data, resourceId);
			data.resourceTimeTrackingPolicies.put(resourceId, trackValueAssignmentTimes);
			return this;
		}

	}

	private static void validateData(Data data) {

		// 1
		for (ResourceId resourceId : data.resourceTimeTrackingPolicies.keySet()) {
			if (!data.resourceIds.contains(resourceId)) {
				throw new ContractException(ResourceError.UNKNOWN_RESOURCE_ID, resourceId + " has a resource tracking policy but is not a known resource id");
			}
		}

		// 2
		for (ResourceId resourceId : data.resourcePropertyDefinitions.keySet()) {
			if (!data.resourceIds.contains(resourceId)) {
				throw new ContractException(ResourceError.UNKNOWN_RESOURCE_ID, resourceId + " has a property definitions but is not a known resource id");
			}
		}

		for (ResourceId resourceId : data.resourcePropertyValues.keySet()) {
			if (!data.resourceIds.contains(resourceId)) {
				// 3
				throw new ContractException(ResourceError.UNKNOWN_RESOURCE_ID, resourceId + " has a property values but is not a known resource id");
			}

			Map<ResourcePropertyId, PropertyDefinition> propDefMap = data.resourcePropertyDefinitions.get(resourceId);

			Map<ResourcePropertyId, Object> map = data.resourcePropertyValues.get(resourceId);
			for (ResourcePropertyId resourcePropertyId : map.keySet()) {
				if (propDefMap == null || !propDefMap.containsKey(resourcePropertyId)) {
					// 4
					throw new ContractException(ResourceError.UNKNOWN_RESOURCE_PROPERTY_ID, resourceId + ": " + resourcePropertyId);
				}
				Object propertyValue = map.get(resourcePropertyId);
				PropertyDefinition propertyDefinition = propDefMap.get(resourcePropertyId);
				if (!propertyDefinition.getType().isAssignableFrom(propertyValue.getClass())) {
					// 5
					throw new ContractException(ResourceError.INCOMPATIBLE_VALUE, resourceId + ": " + resourcePropertyId + ": " + propertyValue);
				}
			}

		}

		/*
		 * For every resource property definition that has a null default value,
		 * ensure that there all corresponding resource property values are not
		 * null and repair the definition.
		 */
		for (ResourceId resourceId : data.resourceIds) {
			Map<ResourcePropertyId, PropertyDefinition> propertyDefinitionMap = data.resourcePropertyDefinitions.get(resourceId);
			if (propertyDefinitionMap != null) {
				for (ResourcePropertyId resourcePropertyId : propertyDefinitionMap.keySet()) {
					PropertyDefinition propertyDefinition = propertyDefinitionMap.get(resourcePropertyId);
					if (!propertyDefinition.getDefaultValue().isPresent()) {
						Object propertyValue = null;
						Map<ResourcePropertyId, Object> propertyValueMap = data.resourcePropertyValues.get(resourceId);
						if (propertyValueMap != null) {
							propertyValue = propertyValueMap.get(resourcePropertyId);
						}
						if (propertyValue == null) {
							// 6
							throw new ContractException(ResourceError.INSUFFICIENT_RESOURCE_PROPERTY_VALUE_ASSIGNMENT, resourceId + ": " + resourcePropertyId);
						}
					}
				}
			}
		}

		for (PersonId personId : data.personResourceLevels.keySet()) {
			Map<ResourceId, Long> map = data.personResourceLevels.get(personId);
			for (ResourceId resourceId : map.keySet()) {
				if (!data.resourceIds.contains(resourceId)) {
					// 7
					throw new ContractException(ResourceError.UNKNOWN_RESOURCE_ID, personId + ": " + resourceId);
				}
			}
		}

		for (RegionId regionId : data.regionResourceLevels.keySet()) {
			Map<ResourceId, Long> map = data.regionResourceLevels.get(regionId);
			for (ResourceId resourceId : map.keySet()) {
				if (!data.resourceIds.contains(resourceId)) {
					// 8
					throw new ContractException(ResourceError.UNKNOWN_RESOURCE_ID, regionId + ": " + resourceId);
				}
			}
		}

	}

	private static void validateResourcePropertyIsDefined(final Data data, final ResourceId resourceId, final ResourcePropertyId resourcePropertyId) {
		Map<ResourcePropertyId, PropertyDefinition> map = data.resourcePropertyDefinitions.get(resourceId);
		if (map == null) {
			throw new ContractException(ResourceError.UNKNOWN_RESOURCE_PROPERTY_ID, resourceId);
		}

		if (!map.containsKey(resourcePropertyId)) {
			throw new ContractException(ResourceError.UNKNOWN_RESOURCE_PROPERTY_ID, resourcePropertyId);
		}
	}

	/**
	 * Returns the property definition associated with the resource id and
	 * resource property id.
	 * 
	 * @throws ContractException
	 *             <li>{@linkplain ResourceError#NULL_RESOURCE_ID} if the
	 *             resource id is null</li>
	 *             <li>{@linkplain ResourceError#UNKNOWN_RESOURCE_ID} if the
	 *             resource id is unknown</li>
	 *             <li>{@linkplain ResourceError#NULL_RESOURCE_PROPERTY_ID} if
	 *             the resource property id is null</li>
	 *             <li>{@linkplain ResourceError#UNKNOWN_RESOURCE_PROPERTY_ID}
	 *             if the resource property id is unknown</li>
	 * 
	 */
	public PropertyDefinition getResourcePropertyDefinition(final ResourceId resourceId, final ResourcePropertyId resourcePropertyId) {
		validateResourceExists(data, resourceId);
		validateResourcePropertyIdNotNull(resourcePropertyId);
		validateResourcePropertyIsDefined(data, resourceId, resourcePropertyId);
		final Map<ResourcePropertyId, PropertyDefinition> defMap = data.resourcePropertyDefinitions.get(resourceId);
		final PropertyDefinition propertyDefinition = defMap.get(resourcePropertyId);
		return propertyDefinition;
	}

	/**
	 * Returns the resource property id associated with the resource.
	 * 
	 * @throws ContractException
	 *             <li>{@linkplain ResourceError#NULL_RESOURCE_ID} if the
	 *             resource id is null</li>
	 *             <li>{@linkplain ResourceError#UNKNOWN_RESOURCE_ID} if the
	 *             resource id is unknown</li>
	 */
	@SuppressWarnings("unchecked")
	public <T extends ResourcePropertyId> Set<T> getResourcePropertyIds(final ResourceId resourceId) {
		validateResourceExists(data, resourceId);
		Set<T> result = new LinkedHashSet<>();
		Map<ResourcePropertyId, PropertyDefinition> defMap = data.resourcePropertyDefinitions.get(resourceId);
		if (defMap != null) {
			for (ResourcePropertyId resourcePropertyId : defMap.keySet()) {
				result.add((T) resourcePropertyId);
			}
		}
		return result;
	}

	private static void validateResourceExists(final Data data, final ResourceId resourceId) {
		if (resourceId == null) {
			throw new ContractException(ResourceError.NULL_RESOURCE_ID);
		}
		if (!data.resourceIds.contains(resourceId)) {
			throw new ContractException(ResourceError.UNKNOWN_RESOURCE_ID, resourceId);
		}
	}

	/**
	 * Returns the resource property value associated with the resource id and
	 * resource property id. Returns the default value of the associated
	 * property definition if now value was assigned.
	 * 
	 * @throws ContractException
	 *             <li>{@linkplain ResourceError#NULL_RESOURCE_ID} if the
	 *             resource id is null</li>
	 *             <li>{@linkplain ResourceError#UNKNOWN_RESOURCE_ID} if the *
	 *             resource id is unknown</li>
	 *             <li>{@linkplain ResourceError#NULL_RESOURCE_PROPERTY_ID} if
	 *             the resource property id is null</li>
	 *             <li>{@linkplain ResourceError#UNKNOWN_RESOURCE_PROPERTY_ID}
	 *             if the resource property id is unknown</li>
	 */
	@SuppressWarnings("unchecked")
	public <T> T getResourcePropertyValue(final ResourceId resourceId, final ResourcePropertyId resourcePropertyId) {
		validateResourceExists(data, resourceId);
		validateResourcePropertyIdNotNull(resourcePropertyId);
		validateResourcePropertyIsDefined(data, resourceId, resourcePropertyId);

		Object result = null;
		final Map<ResourcePropertyId, Object> map = data.resourcePropertyValues.get(resourceId);
		if (map != null) {
			result = map.get(resourcePropertyId);
		}
		if (result == null) {
			final Map<ResourcePropertyId, PropertyDefinition> defMap = data.resourcePropertyDefinitions.get(resourceId);
			final PropertyDefinition propertyDefinition = defMap.get(resourcePropertyId);
			Optional<Object> optional = propertyDefinition.getDefaultValue();
			if (optional.isPresent()) {
				result = optional.get();
			}
		}
		return (T)result;
	}

	/**
	 * Returns the person's initial resource level. Returns 0 if no value was
	 * assigned during the build process.
	 * 
	 * @throws ContractException
	 *             <li>{@linkplain PersonError#NULL_PERSON_ID} if the person id
	 *             is null</li>
	 *             <li>{@linkplain ResourceError#NULL_RESOURCE_ID} if the
	 *             resource id is null</li>
	 *             <li>{@linkplain ResourceError#UNKNOWN_RESOURCE_ID} if the
	 *             resource id is unknown</li>
	 */
	public Long getPersonResourceLevel(final PersonId personId, final ResourceId resourceId) {
		validatePersonIdNotNull(personId);
		validateResourceExists(data, resourceId);
		Long result = null;
		final Map<ResourceId, Long> map = data.personResourceLevels.get(personId);
		if (map != null) {
			result = map.get(resourceId);
		}
		if (result == null) {
			result = 0L;
		}
		return result;
	}

	/**
	 * Returns the resource ids
	 */
	@SuppressWarnings("unchecked")
	public <T extends ResourceId> Set<T> getResourceIds() {
		Set<T> result = new LinkedHashSet<>(data.resourceIds.size());
		for (ResourceId resourceId : data.resourceIds) {
			result.add((T) resourceId);
		}
		return result;
	}

	/**
	 * Returns the region's initial resource level. Returns 0 if no value was
	 * assigned during the build process.
	 * 
	 * @throws ContractException
	 *             <li>{@linkplain RegionError#NULL_REGION_ID} if the region id
	 *             is null</li>
	 *             <li>{@linkplain ResourceError#NULL_RESOURCE_ID} if the
	 *             resource id is null</li>
	 *             <li>{@linkplain ResourceError#UNKNOWN_RESOURCE_ID} if the
	 *             resource id is unknown</li>
	 */
	public Long getRegionResourceLevel(final RegionId regionId, final ResourceId resourceId) {
		validateRegionIdNotNull(regionId);
		validateResourceExists(data, resourceId);

		Long result = null;
		final Map<ResourceId, Long> map = data.regionResourceLevels.get(regionId);

		if (map != null) {
			result = map.get(resourceId);
		}

		if (result == null) {
			result = 0L;
		}

		return result;
	}

	/**
	 * Returns the tracking policy associated with the resource. Returns
	 * TimeTrackingPolicy.DO_NOT_TRACK_TIME if no policy was set.
	 * 
	 * @throws ContractException
	 *             <li>{@linkplain ResourceError#NULL_RESOURCE_ID} if the
	 *             resource id is null</li>
	 *             <li>{@linkplain ResourceError#UNKNOWN_RESOURCE_ID} if the
	 *             resource id is unknown</li>
	 */
	public TimeTrackingPolicy getPersonResourceTimeTrackingPolicy(final ResourceId resourceId) {
		validateResourceExists(data, resourceId);
		TimeTrackingPolicy result = data.resourceTimeTrackingPolicies.get(resourceId);
		if (result == null) {
			result = TimeTrackingPolicy.DO_NOT_TRACK_TIME;
		}
		return result;
	}

	/**
	 * Returns the region ids associated with assigned resources
	 */
	public Set<PersonId> getPersonIds() {
		return new LinkedHashSet<>(data.personResourceLevels.keySet());
	}

	/**
	 * Returns the person ids associated with assigned resources
	 */
	public Set<RegionId> getRegionIds() {
		return new LinkedHashSet<>(data.regionResourceLevels.keySet());
	}

	@Override
	public PluginDataBuilder getCloneBuilder() {
		return new Builder(new Data(data));
	}

}
