package plugins.resources;

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
import plugins.resources.support.ResourceError;
import plugins.resources.support.ResourceId;
import plugins.resources.support.ResourceInitialization;
import plugins.resources.support.ResourcePropertyId;
import plugins.util.properties.PropertyDefinition;
import plugins.util.properties.PropertyError;
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
 *
 */
@Immutable
public final class ResourcesPluginData implements PluginData {

	private static class Data {

		private final Map<ResourceId, Map<ResourcePropertyId, PropertyDefinition>> resourcePropertyDefinitions;

		private final Map<ResourceId, Map<ResourcePropertyId, Object>> resourcePropertyValues;

		private final List<List<ResourceInitialization>> personResourceLevels;

		private final List<ResourceInitialization> emptyResourceInitializationList = Collections.unmodifiableList(new ArrayList<>());

		private int personCount;

		private final Set<ResourceId> resourceIds;

		private final Map<RegionId, List<ResourceInitialization>> regionResourceLevels;

		private final Map<ResourceId, TimeTrackingPolicy> resourceTimeTrackingPolicies;

		public Data() {
			resourcePropertyDefinitions = new LinkedHashMap<>();
			resourcePropertyValues = new LinkedHashMap<>();
			personResourceLevels = new ArrayList<>();
			resourceIds = new LinkedHashSet<>();
			regionResourceLevels = new LinkedHashMap<>();
			resourceTimeTrackingPolicies = new LinkedHashMap<>();
		}

		public Data(Data data) {
			personCount = data.personCount;

			resourcePropertyDefinitions = new LinkedHashMap<>();
			for (ResourceId resourceId : data.resourcePropertyDefinitions.keySet()) {
				Map<ResourcePropertyId, PropertyDefinition> map = data.resourcePropertyDefinitions.get(resourceId);
				Map<ResourcePropertyId, PropertyDefinition> newMap = new LinkedHashMap<>(map);
				resourcePropertyDefinitions.put(resourceId, newMap);
			}

			resourcePropertyValues = new LinkedHashMap<>();
			for (ResourceId resourceId : data.resourcePropertyValues.keySet()) {
				Map<ResourcePropertyId, Object> map = data.resourcePropertyValues.get(resourceId);
				Map<ResourcePropertyId, Object> newMap = new LinkedHashMap<>(map);
				resourcePropertyValues.put(resourceId, newMap);
			}

			personResourceLevels = new ArrayList<>();
			int n = data.personResourceLevels.size();
			for (int i = 0; i < n; i++) {

				List<ResourceInitialization> list = data.personResourceLevels.get(i);
				List<ResourceInitialization> newList = null;
				if (list != null) {
					newList = new ArrayList<>(list);
				}
				personResourceLevels.add(newList);
			}

			resourceIds = new LinkedHashSet<>(data.resourceIds);

			regionResourceLevels = new LinkedHashMap<>();
			for (RegionId regionId : data.regionResourceLevels.keySet()) {
				List<ResourceInitialization> list = data.regionResourceLevels.get(regionId);
				List<ResourceInitialization> newList = new ArrayList<>(list);				
				regionResourceLevels.put(regionId, newList);
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
		List<ResourceInitialization> resourceInitializations = data.regionResourceLevels.get(regionId);
		if (resourceInitializations != null) {
			for (ResourceInitialization resourceInitialization : resourceInitializations) {
				if (resourceInitialization.getResourceId().equals(resourceId)) {
					throw new ContractException(ResourceError.DUPLICATE_REGION_RESOURCE_LEVEL_ASSIGNMENT, resourceId + ": " + regionId);
				}
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

	private static void validatePersonId(PersonId personId) {
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
		int personIndex = personId.getValue();
		if (personIndex >= data.personResourceLevels.size()) {
			return;
		}
		List<ResourceInitialization> list = data.personResourceLevels.get(personIndex);
		if (list != null) {
			for (ResourceInitialization resourceInitialization : list) {
				if (resourceInitialization.getResourceId().equals(resourceId)) {
					throw new ContractException(ResourceError.DUPLICATE_PERSON_RESOURCE_LEVEL_ASSIGNMENT, resourceId + ": " + personId);
				}
			}
		}
	}

	private static void validateResourcePropertyIdNotNull(ResourcePropertyId resourcePropertyId) {
		if (resourcePropertyId == null) {
			throw new ContractException(PropertyError.NULL_PROPERTY_ID);
		}
	}

	private static void validateResourcePropertyValueNotNull(Object resourcePropertyValue) {
		if (resourcePropertyValue == null) {
			throw new ContractException(PropertyError.NULL_PROPERTY_VALUE);
		}
	}

	private static void validateResourcePropertyValueNotSet(final Data data, final ResourceId resourceId, final ResourcePropertyId resourcePropertyId) {
		final Map<ResourcePropertyId, Object> propertyMap = data.resourcePropertyValues.get(resourceId);
		if (propertyMap != null) {
			if (propertyMap.containsKey(resourcePropertyId)) {
				throw new ContractException(PropertyError.DUPLICATE_PROPERTY_VALUE_ASSIGNMENT, resourcePropertyId + ": " + resourceId);
			}
		}
	}

	private static void validateResourcePropertyDefintionNotNull(PropertyDefinition propertyDefinition) {
		if (propertyDefinition == null) {
			throw new ContractException(PropertyError.NULL_PROPERTY_DEFINITION);
		}
	}

	private static void validateResourcePropertyIsNotDefined(final Data data, final ResourceId resourceId, final ResourcePropertyId resourcePropertyId) {
		final Map<ResourcePropertyId, PropertyDefinition> defMap = data.resourcePropertyDefinitions.get(resourceId);
		if (defMap != null) {
			final PropertyDefinition propertyDefinition = defMap.get(resourcePropertyId);
			if (propertyDefinition != null) {
				throw new ContractException(PropertyError.DUPLICATE_PROPERTY_DEFINITION, resourcePropertyId);
			}
		}
	}

	/**
	 * Builder class for ResourceInitialData
	 * 
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
		 * Returns the ResourceInitialData built from the collected data.
		 * 
		 * @throws ContractException
		 *
		 * 
		 *             <li>{@linkplain ResourceError#UNKNOWN_RESOURCE_ID} if a
		 *             resource tracking policy was collected for a resource
		 *             that was not added</li>
		 * 
		 *             <li>{@linkplain ResourceError#UNKNOWN_RESOURCE_ID} if a
		 *             resource property definition was collected for a resource
		 *             that was not added</li>
		 * 
		 *             <li>{@linkplain PropertyError#UNKNOWN_PROPERTY_ID}
		 *             if a resource property value was collected for a resource
		 *             that was not added</li>
		 * 
		 *             <li>{@linkplain PropertyError#UNKNOWN_PROPERTY_ID}
		 *             if a resource property value was collected for a resource
		 *             property that is not associated with the given resource
		 *             id</li>
		 * 
		 *             <li>{@linkplain PropertyError#INCOMPATIBLE_VALUE} if a
		 *             resource property value was collected for a resource
		 *             property that is not compatible with the associated
		 *             resource property definition</li>
		 * 
		 *             <li>{@linkplain PropertyError#INSUFFICIENT_PROPERTY_VALUE_ASSIGNMENT}
		 *             if a resource property definition has a null default
		 *             value and there is no assigned resource property value
		 *             for that resource</li>
		 * 
		 * 
		 *             <li>{@linkplain ResourceError#UNKNOWN_RESOURCE_ID} if a
		 *             resource level was collected for a person that is an
		 *             unknown resource id</li>
		 * 
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
						dataIsMutable = true;
						data.resourceTimeTrackingPolicies.put(resourceId, TimeTrackingPolicy.DO_NOT_TRACK_TIME);
					}
				}
				validateData();
				return new ResourcesPluginData(data);
			} finally {
				data = new Data();
			}
		}

		/**
		 * Adds the given resouce id.
		 * Duplicate inputs override previous inputs.
		 * 
		 * @throws ContractException
		 *             <li>{@linkplain ResourceError#NULL_RESOURCE_ID} if the
		 *             resource id is null</li>
		 *
		 */
		public Builder addResource(final ResourceId resourceId) {
			ensureDataMutability();
			validateResourceIdNotNull(resourceId);
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
		 *             <li>{@linkplain PropertyError#NULL_PROPERTY_ID}
		 *             </li> if the resource property id is null
		 * 
		 *             <li>{@linkplain PropertyError#NULL_PROPERTY_DEFINITION}
		 *             </li> if the property definition is null
		 *
		 *             <li>{@linkplain PropertyError#DUPLICATE_PROPERTY_DEFINITION}
		 *             </li> if a resource property definition for the given
		 *             resource id and property id was previously defined.
		 * 
		 */
		public Builder defineResourceProperty(final ResourceId resourceId, final ResourcePropertyId resourcePropertyId, final PropertyDefinition propertyDefinition) {
			ensureDataMutability();
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
			ensureDataMutability();
			validatePersonId(personId);
			validateResourceIdNotNull(resourceId);
			validateResourceAmount(amount);
			validatePersonResourceLevelNotSet(data, personId, resourceId);

			int personIndex = personId.getValue();
			data.personCount = FastMath.max(data.personCount, personIndex + 1);

			while (personIndex >= data.personResourceLevels.size()) {
				data.personResourceLevels.add(null);
			}

			List<ResourceInitialization> list = data.personResourceLevels.get(personIndex);

			if (list == null) {
				list = new ArrayList<>();
				data.personResourceLevels.set(personIndex, list);
			}
			ResourceInitialization resourceInitialization = new ResourceInitialization(resourceId, amount);
			list.add(resourceInitialization);
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
			ensureDataMutability();
			validateRegionIdNotNull(regionId);
			validateResourceIdNotNull(resourceId);
			validateRegionResourceNotSet(data, regionId, resourceId);
			validateResourceAmount(amount);
			List<ResourceInitialization> resourceInitializations = data.regionResourceLevels.get(regionId);
			if (resourceInitializations == null) {
				resourceInitializations = new ArrayList<>();
				data.regionResourceLevels.put(regionId, resourceInitializations);
			}
			resourceInitializations.add(new ResourceInitialization(resourceId, amount));			
			return this;
		}

		/**
		 * Sets a resource property value
		 * 
		 * @throws ContractException
		 *             <li>{@linkplain ResourceError#NULL_RESOURCE_ID} if the
		 *             resource id is null</li>
		 *             <li>{@linkplain PropertyError#NULL_PROPERTY_ID}
		 *             if the resource property id is null</li>
		 *             <li>{@linkplain PropertyError#NULL_PROPERTY_VALUE}
		 *             if the resource property value is null</li>
		 *             <li>{@linkplain PropertyError#DUPLICATE_PROPERTY_VALUE_ASSIGNMENT}
		 *             if the resource property value was previously
		 *             assigned</li>
		 */
		public Builder setResourcePropertyValue(final ResourceId resourceId, final ResourcePropertyId resourcePropertyId, final Object resourcePropertyValue) {
			ensureDataMutability();
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
			ensureDataMutability();
			validateResourceIdNotNull(resourceId);
			validateTimeTrackingPolicyNotNull(trackValueAssignmentTimes);
			validateResourceTimeTrackingNotSet(data, resourceId);
			data.resourceTimeTrackingPolicies.put(resourceId, trackValueAssignmentTimes);
			return this;
		}

		private void validateData() {

			if (!dataIsMutable) {
				return;
			}

			for (ResourceId resourceId : data.resourceTimeTrackingPolicies.keySet()) {
				if (!data.resourceIds.contains(resourceId)) {
					throw new ContractException(ResourceError.UNKNOWN_RESOURCE_ID, resourceId + " has a resource tracking policy but is not a known resource id");
				}
			}

			for (ResourceId resourceId : data.resourcePropertyDefinitions.keySet()) {
				if (!data.resourceIds.contains(resourceId)) {
					throw new ContractException(ResourceError.UNKNOWN_RESOURCE_ID, resourceId + " has a property definitions but is not a known resource id");
				}
			}

			for (ResourceId resourceId : data.resourcePropertyValues.keySet()) {
				if (!data.resourceIds.contains(resourceId)) {
					throw new ContractException(ResourceError.UNKNOWN_RESOURCE_ID, resourceId + " has a property values but is not a known resource id");
				}

				Map<ResourcePropertyId, PropertyDefinition> propDefMap = data.resourcePropertyDefinitions.get(resourceId);

				Map<ResourcePropertyId, Object> map = data.resourcePropertyValues.get(resourceId);
				for (ResourcePropertyId resourcePropertyId : map.keySet()) {
					if (propDefMap == null || !propDefMap.containsKey(resourcePropertyId)) {
						throw new ContractException(PropertyError.UNKNOWN_PROPERTY_ID, resourceId + ": " + resourcePropertyId);
					}
					Object propertyValue = map.get(resourcePropertyId);
					PropertyDefinition propertyDefinition = propDefMap.get(resourcePropertyId);
					if (!propertyDefinition.getType().isAssignableFrom(propertyValue.getClass())) {
						throw new ContractException(PropertyError.INCOMPATIBLE_VALUE, resourceId + ": " + resourcePropertyId + ": " + propertyValue);
					}
				}

			}

			/*
			 * For every resource property definition that has a null default
			 * value, ensure that there all corresponding resource property
			 * values are not null.
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
								throw new ContractException(PropertyError.INSUFFICIENT_PROPERTY_VALUE_ASSIGNMENT, resourceId + ": " + resourcePropertyId);
							}
						}
					}
				}
			}

			int n = data.personResourceLevels.size();
			for (int i = 0; i < n; i++) {
				List<ResourceInitialization> list = data.personResourceLevels.get(i);
				if (list != null) {
					for (ResourceInitialization resourceInitialization : list) {
						if (!data.resourceIds.contains(resourceInitialization.getResourceId())) {
							throw new ContractException(ResourceError.UNKNOWN_RESOURCE_ID, new PersonId(i) + ": " + resourceInitialization.getResourceId());
						}
					}
				}
			}

			for (RegionId regionId : data.regionResourceLevels.keySet()) {
				List<ResourceInitialization> resourceInitializations = data.regionResourceLevels.get(regionId);
				if (resourceInitializations != null) {
					for (ResourceInitialization resourceInitialization : resourceInitializations) {
						if (!data.resourceIds.contains(resourceInitialization.getResourceId())) {
							throw new ContractException(ResourceError.UNKNOWN_RESOURCE_ID, regionId + ": " + resourceInitialization.getResourceId());
						}
					}
				}
			}

		}

	}

	private static void validateResourcePropertyIsDefined(final Data data, final ResourceId resourceId, final ResourcePropertyId resourcePropertyId) {
		Map<ResourcePropertyId, PropertyDefinition> map = data.resourcePropertyDefinitions.get(resourceId);
		if (map == null) {
			throw new ContractException(PropertyError.UNKNOWN_PROPERTY_ID, resourceId);
		}

		if (!map.containsKey(resourcePropertyId)) {
			throw new ContractException(PropertyError.UNKNOWN_PROPERTY_ID, resourcePropertyId);
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
	 *             <li>{@linkplain PropertyError#NULL_PROPERTY_ID} if
	 *             the resource property id is null</li>
	 *             <li>{@linkplain PropertyError#UNKNOWN_PROPERTY_ID}
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
	 *             <li>{@linkplain PropertyError#NULL_PROPERTY_ID} if
	 *             the resource property id is null</li>
	 *             <li>{@linkplain PropertyError#UNKNOWN_PROPERTY_ID}
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
		return (T) result;
	}

	/**
	 * Returns the person's initial resource levels.
	 * 
	 * @throws ContractException
	 *             <li>{@linkplain PersonError#NULL_PERSON_ID} if the person id
	 *             is null</li>
	 *             <li>{@linkplain ResourceError#NULL_RESOURCE_ID} if the
	 *             resource id is null</li>
	 *             <li>{@linkplain ResourceError#UNKNOWN_RESOURCE_ID} if the
	 *             resource id is unknown</li>
	 */
	public List<ResourceInitialization> getPersonResourceLevels(final PersonId personId) {

		validatePersonIdNotNull(personId);
		int personIndex = personId.getValue();
		if (personIndex >= data.personResourceLevels.size()) {
			return data.emptyResourceInitializationList;
		}

		List<ResourceInitialization> list = data.personResourceLevels.get(personIndex);
		if (list == null) {
			return data.emptyResourceInitializationList;
		}

		return Collections.unmodifiableList(list);
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
	 */
	public List<ResourceInitialization> getRegionResourceLevels(final RegionId regionId) {
		validateRegionIdNotNull(regionId);
		List<ResourceInitialization> list = data.regionResourceLevels.get(regionId);
		if(list==null) {
			return data.emptyResourceInitializationList;
		}
		return Collections.unmodifiableList(list);
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
	 * Returns the person ids associated with assigned resources
	 */
	public Set<RegionId> getRegionIds() {
		return new LinkedHashSet<>(data.regionResourceLevels.keySet());
	}

	@Override
	public PluginDataBuilder getCloneBuilder() {
		return new Builder(data);
	}

	/**
	 * Returns the int value that exceeds by one the highest person id value
	 * encountered while associating people with resources.
	 */
	public int getPersonCount() {
		return data.personCount;
	}

}
