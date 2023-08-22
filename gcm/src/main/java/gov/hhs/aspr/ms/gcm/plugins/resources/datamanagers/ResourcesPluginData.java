package gov.hhs.aspr.ms.gcm.plugins.resources.datamanagers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import gov.hhs.aspr.ms.gcm.nucleus.PluginData;
import gov.hhs.aspr.ms.gcm.nucleus.PluginDataBuilder;
import gov.hhs.aspr.ms.gcm.plugins.people.support.PersonError;
import gov.hhs.aspr.ms.gcm.plugins.people.support.PersonId;
import gov.hhs.aspr.ms.gcm.plugins.regions.support.RegionError;
import gov.hhs.aspr.ms.gcm.plugins.regions.support.RegionId;
import gov.hhs.aspr.ms.gcm.plugins.resources.support.ResourceError;
import gov.hhs.aspr.ms.gcm.plugins.resources.support.ResourceId;
import gov.hhs.aspr.ms.gcm.plugins.resources.support.ResourcePropertyId;
import gov.hhs.aspr.ms.gcm.plugins.util.properties.PropertyDefinition;
import gov.hhs.aspr.ms.gcm.plugins.util.properties.PropertyError;
import net.jcip.annotations.Immutable;
import util.errors.ContractException;

/**
 * An immutable container of the initial state of resources. It contains: <BR>
 * <ul>
 * <li>resource ids</li>
 * <li>resource property definitions</li>
 * <li>region resource levels</li>
 * <li>person resource levels</li>
 * </ul>
 */
@Immutable
public final class ResourcesPluginData implements PluginData {

	private static class Data {

		private final Map<ResourceId, Double> resourceDefaultTimes;
		private Map<ResourceId, Boolean> resourceTimeTrackingPolicies;
		private Map<ResourceId, Map<ResourcePropertyId, PropertyDefinition>> resourcePropertyDefinitions;
		private Map<ResourceId, Map<ResourcePropertyId, Object>> resourcePropertyValues;
		private Map<ResourceId, List<Long>> personResourceLevels;
		private Map<ResourceId, List<Double>> personResourceTimes;
		private Map<RegionId, Map<ResourceId, Long>> regionResourceLevels;

		private boolean locked;

		public Data() {
			resourceDefaultTimes = new LinkedHashMap<>();
			resourceTimeTrackingPolicies = new LinkedHashMap<>();
			resourcePropertyDefinitions = new LinkedHashMap<>();
			resourcePropertyValues = new LinkedHashMap<>();
			personResourceLevels = new LinkedHashMap<>();
			personResourceTimes = new LinkedHashMap<>();
			regionResourceLevels = new LinkedHashMap<>();
		}

		public Data(Data data) {
			resourceDefaultTimes = new LinkedHashMap<>(data.resourceDefaultTimes);
			resourceTimeTrackingPolicies = new LinkedHashMap<>(data.resourceTimeTrackingPolicies);
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

			personResourceLevels = new LinkedHashMap<>();
			for (ResourceId resourceId : data.personResourceLevels.keySet()) {
				List<Long> list = data.personResourceLevels.get(resourceId);
				List<Long> newlist = new ArrayList<>(list);
				personResourceLevels.put(resourceId, newlist);
			}

			personResourceTimes = new LinkedHashMap<>();
			for (ResourceId resourceId : data.personResourceTimes.keySet()) {
				List<Double> list = data.personResourceTimes.get(resourceId);
				List<Double> newlist = new ArrayList<>(list);
				personResourceTimes.put(resourceId, newlist);
			}

			regionResourceLevels = new LinkedHashMap<>();
			for (RegionId regionId : data.regionResourceLevels.keySet()) {
				Map<ResourceId, Long> sourceMap = data.regionResourceLevels.get(regionId);
				Map<ResourceId, Long> destinationMap = new LinkedHashMap<>(sourceMap);
				regionResourceLevels.put(regionId, destinationMap);
			}

			locked = data.locked;
		}

		@Override
		public String toString() {
			StringBuilder builder = new StringBuilder();
			builder.append("Data [resourceDefaultTimes=");
			builder.append(resourceDefaultTimes);
			builder.append(", resourceTimeTrackingPolicies=");
			builder.append(resourceTimeTrackingPolicies);
			builder.append(", resourcePropertyDefinitions=");
			builder.append(resourcePropertyDefinitions);
			builder.append(", resourcePropertyValues=");
			builder.append(resourcePropertyValues);
			builder.append(", personResourceLevels=");
			builder.append(personResourceLevels);
			builder.append(", personResourceTimes=");
			builder.append(personResourceTimes);
			builder.append(", regionResourceLevels=");
			builder.append(regionResourceLevels);
			builder.append("]");
			return builder.toString();
		}

		/**
		 * 
		 */
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + resourceDefaultTimes.hashCode();
			result = prime * result + resourceTimeTrackingPolicies.hashCode();
			result = prime * result + resourcePropertyDefinitions.hashCode();
			result = prime * result + resourcePropertyValues.hashCode();
			result = prime * result + personResourceLevels.hashCode();
			result = prime * result + personResourceTimes.hashCode();
			result = prime * result + regionResourceLevels.hashCode();

			return result;
		}

		/**
		 * 
		 */
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
			 * locked -- two Datas are only compared when they are both locked -- there are
			 * no equality comparisons in this class.
			 * 
			 * 
			 */
			// These are simply compared:
			if (!resourceDefaultTimes.equals(other.resourceDefaultTimes)) {
				return false;
			}

			if (!resourceTimeTrackingPolicies.equals(other.resourceTimeTrackingPolicies)) {
				return false;
			}

			if (!resourcePropertyDefinitions.equals(other.resourcePropertyDefinitions)) {
				return false;
			}

			if (!resourcePropertyValues.equals(other.resourcePropertyValues)) {
				return false;
			}

			if (!personResourceLevels.equals(other.personResourceLevels)) {
				return false;
			}

			if (!personResourceTimes.equals(other.personResourceTimes)) {
				return false;
			}

			if (!regionResourceLevels.equals(other.regionResourceLevels)) {
				return false;
			}

			return true;
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

	private static void validateRegionIdNotNull(RegionId regionId) {
		if (regionId == null) {
			throw new ContractException(RegionError.NULL_REGION_ID);
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

	private static void validateResourcePropertyDefintionNotNull(PropertyDefinition propertyDefinition) {
		if (propertyDefinition == null) {
			throw new ContractException(PropertyError.NULL_PROPERTY_DEFINITION);
		}
	}

	/**
	 * Builder class for ResourceInitialData
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
		 * Returns the ResourceInitialData built from the collected data.
		 * 
		 * @throws ContractException
		 *                                       <li>{@linkplain ResourceError#UNKNOWN_RESOURCE_ID}
		 *                                       if a resource tracking policy was
		 *                                       collected for a resource that was not
		 *                                       added</li>
		 *                                       <li>{@linkplain ResourceError#UNKNOWN_RESOURCE_ID}
		 *                                       if a resource property definition was
		 *                                       collected for a resource that was not
		 *                                       added</li>
		 *                                       <li>{@linkplain PropertyError#UNKNOWN_PROPERTY_ID}
		 *                                       if a resource property value was
		 *                                       collected for a resource that was not
		 *                                       added</li>
		 *                                       <li>{@linkplain PropertyError#UNKNOWN_PROPERTY_ID}
		 *                                       if a resource property value was
		 *                                       collected for a resource property that
		 *                                       is not associated with the given
		 *                                       resource id</li>
		 *                                       <li>{@linkplain PropertyError#INCOMPATIBLE_VALUE}
		 *                                       if a resource property value was
		 *                                       collected for a resource property that
		 *                                       is not compatible with the associated
		 *                                       resource property definition</li>
		 *                                       <li>{@linkplain PropertyError#INSUFFICIENT_PROPERTY_VALUE_ASSIGNMENT}
		 *                                       if a resource property definition has a
		 *                                       null default value and there is no
		 *                                       assigned resource property value for
		 *                                       that resource</li>
		 *                                       <li>{@linkplain ResourceError#UNKNOWN_RESOURCE_ID}
		 *                                       if a resource level was collected for a
		 *                                       person that is an unknown resource
		 *                                       id</li>
		 *                                       <li>{@linkplain ResourceError#UNKNOWN_RESOURCE_ID}
		 *                                       if a resource level was collected for a
		 *                                       region that is an unknown resource
		 *                                       id</li></ul>
		 */
		public ResourcesPluginData build() {

			if (!data.locked) {
				validateData();
			}
			ensureImmutability();
			return new ResourcesPluginData(data);
		}

		/**
		 * Adds the given resouce id with default time value. Sets the time tracking
		 * policy for a resource. Duplicate inputs override previous inputs.
		 * 
		 * @throws ContractException
		 *                                       <ul>
		 *                                       <li>{@linkplain ResourceError#NULL_RESOURCE_ID}
		 *                                       if the resource id is null</li>
		 *                                       <li>{@linkplain ResourceError#NULL_TIME}
		 *                                       if the time is null</li>
		 *                                       </ul>
		 */
		public Builder addResource(final ResourceId resourceId, Double time, final boolean trackValueAssignmentTimes) {
			ensureDataMutability();
			validateResourceIdNotNull(resourceId);
			validateTime(time);
			data.resourceDefaultTimes.put(resourceId, time);
			data.resourceTimeTrackingPolicies.put(resourceId, trackValueAssignmentTimes);
			return this;
		}

		/**
		 * Defines a resource property Duplicate inputs override previous inputs.
		 * 
		 * @throws ContractException
		 *                                       <ul>
		 *                                       <li>{@linkplain ResourceError#NULL_RESOURCE_ID}
		 *                                       if the resource id is null</li>
		 *                                       <li>{@linkplain PropertyError#NULL_PROPERTY_ID}
		 *                                       if the resource property id is
		 *                                       null</li>
		 *                                       <li>{@linkplain PropertyError#NULL_PROPERTY_DEFINITION}
		 *                                       if the property definition is null</li>
		 *                                       </ul>
		 */
		public Builder defineResourceProperty(final ResourceId resourceId, final ResourcePropertyId resourcePropertyId,
				final PropertyDefinition propertyDefinition) {
			ensureDataMutability();
			validateResourceIdNotNull(resourceId);
			validateResourcePropertyIdNotNull(resourcePropertyId);
			validateResourcePropertyDefintionNotNull(propertyDefinition);
			Map<ResourcePropertyId, PropertyDefinition> map = data.resourcePropertyDefinitions.get(resourceId);
			if (map == null) {
				map = new LinkedHashMap<>();
				data.resourcePropertyDefinitions.put(resourceId, map);
			}
			map.put(resourcePropertyId, propertyDefinition);
			return this;
		}

		/**
		 * Sets a resource property value. Duplicate inputs override previous inputs.
		 * 
		 * @throws ContractException
		 *                                       <ul>
		 *                                       <li>{@linkplain ResourceError#NULL_RESOURCE_ID}
		 *                                       if the resource id is null</li>
		 *                                       <li>{@linkplain PropertyError#NULL_PROPERTY_ID}
		 *                                       if the resource property id is
		 *                                       null</li>
		 *                                       <li>{@linkplain PropertyError#NULL_PROPERTY_VALUE}
		 *                                       if the resource property value is
		 *                                       null</li>
		 *                                       </ul>
		 */
		public Builder setResourcePropertyValue(final ResourceId resourceId,
				final ResourcePropertyId resourcePropertyId, final Object resourcePropertyValue) {
			ensureDataMutability();
			validateResourceIdNotNull(resourceId);
			validateResourcePropertyIdNotNull(resourcePropertyId);
			validateResourcePropertyValueNotNull(resourcePropertyValue);

			Map<ResourcePropertyId, Object> propertyMap = data.resourcePropertyValues.get(resourceId);
			if (propertyMap == null) {
				propertyMap = new LinkedHashMap<>();
				data.resourcePropertyValues.put(resourceId, propertyMap);
			}
			propertyMap.put(resourcePropertyId, resourcePropertyValue);
			return this;
		}

		/**
		 * Sets a person's initial resource level. Duplicate inputs override previous
		 * inputs.
		 * 
		 * @throws ContractException
		 *                                       <ul>
		 *                                       <li>{@linkplain PersonError#NULL_PERSON_ID}
		 *                                       if the person id is null</li>
		 *                                       <li>{@linkplain ResourceError#NULL_RESOURCE_ID}
		 *                                       if the resource id is null</li>
		 *                                       <li>{@linkplain ResourceError#NEGATIVE_RESOURCE_AMOUNT}
		 *                                       if the resource amount is negative</li>
		 *                                       </ul>
		 */
		public Builder setPersonResourceLevel(final PersonId personId, final ResourceId resourceId, final long amount) {
			ensureDataMutability();
			validatePersonId(personId);
			validateResourceIdNotNull(resourceId);
			validateResourceAmount(amount);

			List<Long> list = data.personResourceLevels.get(resourceId);
			if (list == null) {
				list = new ArrayList<>();
				data.personResourceLevels.put(resourceId, list);
			}

			int personIndex = personId.getValue();
			while (list.size() <= personIndex) {
				list.add(null);
			}
			list.set(personIndex, amount);

			return this;

		}

		/**
		 * Sets a person's initial resource time. Duplicate inputs override previous
		 * inputs.
		 * 
		 * @throws ContractException
		 *                                       <ul>
		 *                                       <li>{@linkplain PersonError#NULL_PERSON_ID}
		 *                                       if the person id is null</li>
		 *                                       <li>{@linkplain ResourceError#NULL_RESOURCE_ID}
		 *                                       if the resource id is null</li>
		 *                                       <li>{@linkplain ResourceError#NULL_TIME}
		 *                                       if the time is null</li>
		 *                                       </ul>
		 */
		public Builder setPersonResourceTime(final PersonId personId, final ResourceId resourceId, final Double time) {
			ensureDataMutability();
			validatePersonId(personId);
			validateResourceIdNotNull(resourceId);
			validateTime(time);

			List<Double> list = data.personResourceTimes.get(resourceId);
			if (list == null) {
				list = new ArrayList<>();
				data.personResourceTimes.put(resourceId, list);
			}

			int personIndex = personId.getValue();
			while (list.size() <= personIndex) {
				list.add(null);
			}
			list.set(personIndex, time);

			return this;

		}

		/**
		 * Sets a region's initial resource level. Duplicate inputs override previous
		 * inputs.
		 * 
		 * @throws ContractException
		 *                                       <ul>
		 *                                       <li>{@linkplain RegionError#NULL_REGION_ID}
		 *                                       if the region id is null</li>
		 *                                       <li>{@linkplain ResourceError#NULL_RESOURCE_ID}
		 *                                       if the resource id is null</li>
		 *                                       <li>{@linkplain ResourceError#NEGATIVE_RESOURCE_AMOUNT}
		 *                                       if the resource amount is negative</li>
		 *                                       *
		 *                                       </ul>
		 */
		public Builder setRegionResourceLevel(final RegionId regionId, final ResourceId resourceId, final long amount) {
			ensureDataMutability();
			validateRegionIdNotNull(regionId);
			validateResourceIdNotNull(resourceId);
			validateResourceAmount(amount);
			Map<ResourceId, Long> map = data.regionResourceLevels.get(regionId);
			if (map == null) {
				map = new LinkedHashMap<>();
				data.regionResourceLevels.put(regionId, map);
			}
			map.put(resourceId, amount);
			return this;
		}

		private void validateData() {

			/*
			 * validate resourcePropertyDefinitions
			 */
			for (ResourceId resourceId : data.resourcePropertyDefinitions.keySet()) {
				if (!data.resourceDefaultTimes.containsKey(resourceId)) {
					throw new ContractException(ResourceError.UNKNOWN_RESOURCE_ID,
							resourceId + " has a property definitions but is not a known resource id");
				}
			}

			/*
			 * validate resourcePropertyValues
			 * 
			 * show that the resource ids are in data.resourceids
			 * 
			 * show that the resource property ids are in the
			 * data.resourcePropertyDefinitions
			 * 
			 * show that the values are compatible with the property definitions
			 */
			for (ResourceId resourceId : data.resourcePropertyValues.keySet()) {
				if (!data.resourceDefaultTimes.containsKey(resourceId)) {
					throw new ContractException(ResourceError.UNKNOWN_RESOURCE_ID,
							resourceId + " for a collected resource property value");
				}
				Map<ResourcePropertyId, PropertyDefinition> propMap = data.resourcePropertyDefinitions.get(resourceId);
				Map<ResourcePropertyId, Object> map = data.resourcePropertyValues.get(resourceId);
				for (ResourcePropertyId resourcePropertyId : map.keySet()) {
					if (propMap == null || !propMap.containsKey(resourcePropertyId)) {
						throw new ContractException(PropertyError.UNKNOWN_PROPERTY_ID,
								resourcePropertyId + " has a resource property value under resource " + resourceId
										+ " but there is no corresponding property definition");
					}
					Object propertyValue = map.get(resourcePropertyId);
					PropertyDefinition propertyDefinition = propMap.get(resourcePropertyId);
					if (!propertyDefinition.getType().isAssignableFrom(propertyValue.getClass())) {
						throw new ContractException(PropertyError.INCOMPATIBLE_VALUE,
								resourcePropertyId + " has a resource property value of " + propertyValue
										+ " under resource " + resourceId
										+ " that is incompatible with the corresponding property definition");
					}
				}
			}

			// show that property definitions without default values have
			// complete value coverage
			for (ResourceId resourceId : data.resourcePropertyDefinitions.keySet()) {
				Map<ResourcePropertyId, PropertyDefinition> propertyDefinitionMap = data.resourcePropertyDefinitions
						.get(resourceId);
				for (ResourcePropertyId resourcePropertyId : propertyDefinitionMap.keySet()) {
					PropertyDefinition propertyDefinition = propertyDefinitionMap.get(resourcePropertyId);
					if (!propertyDefinition.getDefaultValue().isPresent()) {
						Object propertyValue = null;
						Map<ResourcePropertyId, Object> propertyValueMap = data.resourcePropertyValues.get(resourceId);
						if (propertyValueMap != null) {
							propertyValue = propertyValueMap.get(resourcePropertyId);
						}
						if (propertyValue == null) {
							throw new ContractException(PropertyError.INSUFFICIENT_PROPERTY_VALUE_ASSIGNMENT,
									resourceId + ": " + resourcePropertyId);
						}
					}
				}
			}

			/*
			 * validate regionResourceLevels
			 */
			for (RegionId regionId : data.regionResourceLevels.keySet()) {
				Map<ResourceId, Long> map = data.regionResourceLevels.get(regionId);
				for (ResourceId resourceId : map.keySet()) {
					if (!data.resourceDefaultTimes.containsKey(resourceId)) {
						throw new ContractException(ResourceError.UNKNOWN_RESOURCE_ID,
								resourceId + " for region " + regionId + " has a level, but is not a known resource");
					}
				}
			}

			/*
			 * validate personResourceLevels
			 */
			for (ResourceId resourceId : data.personResourceLevels.keySet()) {
				if (!data.resourceDefaultTimes.containsKey(resourceId)) {
					throw new ContractException(ResourceError.UNKNOWN_RESOURCE_ID,
							resourceId + " has person resource levels, but is not a known resource id");
				}
			}

			/*
			 * validate personResourceTimes
			 */
			// private final Map<ResourceId, List<Double>> personResourceTimes;
			for (ResourceId resourceId : data.personResourceTimes.keySet()) {
				if (!data.resourceDefaultTimes.containsKey(resourceId)) {
					throw new ContractException(ResourceError.UNKNOWN_RESOURCE_ID,
							resourceId + " has person resource times, but is not a known resource id");
				}
			}

			for (ResourceId resourceId : data.personResourceTimes.keySet()) {
				Boolean trackTimes = data.resourceTimeTrackingPolicies.get(resourceId);
				if (!trackTimes) {
					throw new ContractException(ResourceError.RESOURCE_ASSIGNMENT_TIME_NOT_TRACKED,
							resourceId + " has person resource times, but the time tracking policy is false");
				}
			}

			for (ResourceId resourceId : data.personResourceTimes.keySet()) {
				Double creationTime = data.resourceDefaultTimes.get(resourceId);
				List<Double> times = data.personResourceTimes.get(resourceId);
				for (Double time : times) {
					if (time != null) {
						if (time < creationTime) {
							throw new ContractException(
									ResourceError.RESOURCE_ASSIGNMENT_TIME_PRECEEDS_RESOURCE_CREATION_TIME);
						}
					}
				}
			}

		}

	}

	private static void validateTime(Double time) {
		if (time == null) {
			throw new ContractException(ResourceError.NULL_TIME);
		}
	}

	private static void validateResourcePropertyIsDefined(final Data data, final ResourceId resourceId,
			final ResourcePropertyId resourcePropertyId) {
		Map<ResourcePropertyId, PropertyDefinition> map = data.resourcePropertyDefinitions.get(resourceId);
		if (map == null) {
			throw new ContractException(PropertyError.UNKNOWN_PROPERTY_ID, resourceId);
		}

		if (!map.containsKey(resourcePropertyId)) {
			throw new ContractException(PropertyError.UNKNOWN_PROPERTY_ID, resourcePropertyId);
		}
	}

	/**
	 * Returns the property definition associated with the resource id and resource
	 * property id.
	 * 
	 * @throws ContractException
	 *                                       <ul>
	 *                                       <li>{@linkplain ResourceError#NULL_RESOURCE_ID}
	 *                                       if the resource id is null</li>
	 *                                       <li>{@linkplain ResourceError#UNKNOWN_RESOURCE_ID}
	 *                                       if the resource id is unknown</li>
	 *                                       <li>{@linkplain PropertyError#NULL_PROPERTY_ID}
	 *                                       if the resource property id is
	 *                                       null</li>
	 *                                       <li>{@linkplain PropertyError#UNKNOWN_PROPERTY_ID}
	 *                                       if the resource property id is
	 *                                       unknown</li>
	 *                                       </ul>
	 */
	public PropertyDefinition getResourcePropertyDefinition(final ResourceId resourceId,
			final ResourcePropertyId resourcePropertyId) {
		validateResourceExists(resourceId);
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
	 *                                       <ul>
	 *                                       <li>{@linkplain ResourceError#NULL_RESOURCE_ID}
	 *                                       if the resource id is null</li>
	 *                                       <li>{@linkplain ResourceError#UNKNOWN_RESOURCE_ID}
	 *                                       if the resource id is unknown</li>
	 *                                       </ul>
	 */
	@SuppressWarnings("unchecked")
	public <T extends ResourcePropertyId> Set<T> getResourcePropertyIds(final ResourceId resourceId) {
		validateResourceExists(resourceId);
		Set<T> result = new LinkedHashSet<>();
		Map<ResourcePropertyId, PropertyDefinition> defMap = data.resourcePropertyDefinitions.get(resourceId);
		if (defMap != null) {
			for (ResourcePropertyId resourcePropertyId : defMap.keySet()) {
				result.add((T) resourcePropertyId);
			}
		}
		return result;
	}

	private void validateResourceExists(final ResourceId resourceId) {
		if (resourceId == null) {
			throw new ContractException(ResourceError.NULL_RESOURCE_ID);
		}
		if (!data.resourceDefaultTimes.containsKey(resourceId)) {
			throw new ContractException(ResourceError.UNKNOWN_RESOURCE_ID, resourceId);
		}
	}

	/**
	 * Returns the resource property value associated with the resource id and
	 * resource property id. Returns the default value of the associated property
	 * definition if now value was assigned.
	 * 
	 * @throws ContractException
	 *                                       <ul>
	 *                                       <li>{@linkplain ResourceError#NULL_RESOURCE_ID}
	 *                                       if the resource id is null</li>
	 *                                       <li>{@linkplain ResourceError#UNKNOWN_RESOURCE_ID}
	 *                                       if the * resource id is unknown</li>
	 *                                       <li>{@linkplain PropertyError#NULL_PROPERTY_ID}
	 *                                       if the resource property id is
	 *                                       null</li>
	 *                                       <li>{@linkplain PropertyError#UNKNOWN_PROPERTY_ID}
	 *                                       if the resource property id is
	 *                                       unknown</li>
	 *                                       </ul>
	 */
	@SuppressWarnings("unchecked")
	public <T> Optional<T> getResourcePropertyValue(final ResourceId resourceId,
			final ResourcePropertyId resourcePropertyId) {
		validateResourceExists(resourceId);
		validateResourcePropertyIdNotNull(resourcePropertyId);
		validateResourcePropertyIsDefined(data, resourceId, resourcePropertyId);

		Object result = null;
		final Map<ResourcePropertyId, Object> map = data.resourcePropertyValues.get(resourceId);
		if (map != null) {
			result = map.get(resourcePropertyId);
		}
		return Optional.ofNullable((T) result);
	}

	/**
	 * Returns an unmodifiable list of the initial resource levels for the given
	 * resource id. May contain null, may be empty.
	 * 
	 * @throws ContractException
	 *                                       <ul>
	 *                                       <li>{@linkplain ResourceError#NULL_RESOURCE_ID}
	 *                                       if the resource id is null</li>
	 *                                       <li>{@linkplain ResourceError#UNKNOWN_RESOURCE_ID}
	 *                                       if the resource id is unknown</li>
	 *                                       </ul>
	 */
	public List<Long> getPersonResourceLevels(final ResourceId resourcId) {
		validateResourceExists(resourcId);
		List<Long> list = data.personResourceLevels.get(resourcId);
		if (list != null) {
			return Collections.unmodifiableList(list);
		}
		return new ArrayList<>();
	}

	/**
	 * Returns an unmodifiable list of the initial resource levels for the given
	 * resource id. May contain null, may be empty.
	 * 
	 * @throws ContractException
	 *                                       <ul>
	 *                                       <li>{@linkplain ResourceError#NULL_RESOURCE_ID}
	 *                                       if the resource id is null</li>
	 *                                       <li>{@linkplain ResourceError#UNKNOWN_RESOURCE_ID}
	 *                                       if the resource id is unknown</li>
	 *                                       </ul>
	 */
	public List<Double> getPersonResourceTimes(final ResourceId resourcId) {
		validateResourceExists(resourcId);
		List<Double> list = data.personResourceTimes.get(resourcId);
		if (list != null) {
			return Collections.unmodifiableList(list);
		}
		return new ArrayList<>();
	}

	/**
	 * Returns the resource ids
	 */
	@SuppressWarnings("unchecked")
	public <T extends ResourceId> Set<T> getResourceIds() {
		Set<T> result = new LinkedHashSet<>(data.resourceDefaultTimes.size());
		for (ResourceId resourceId : data.resourceDefaultTimes.keySet()) {
			result.add((T) resourceId);
		}
		return result;
	}

	/**
	 * Returns the resource ids
	 * 
	 * @throws ContractException
	 *                                       <ul>
	 *                                       <li>{@linkplain ResourceError#NULL_RESOURCE_ID}
	 *                                       if the resource id is null</li>
	 *                                       <li>{@linkplain ResourceError#UNKNOWN_RESOURCE_ID}
	 *                                       if the resource id is unknown</li>
	 *                                       </ul>
	 */
	public Double getResourceDefaultTime(ResourceId resourceId) {
		validateResourceExists(resourceId);
		return data.resourceDefaultTimes.get(resourceId);
	}

	/**
	 * Returns the region's initial resource level. Returns 0 if no value was
	 * assigned during the build process.
	 * 
	 * @throws ContractException
	 *                                       <ul>
	 *                                       <li>{@linkplain RegionError#NULL_REGION_ID}
	 *                                       if the region id is null</li></li>
	 *                                       <li>{@linkplain ResourceError#NULL_RESOURCE_ID}
	 *                                       if the resource id is null</li></li>
	 *                                       <li>{@linkplain ResourceError#UNKNOWN_RESOURCE_ID}
	 *                                       if the resource id is unknown</li>
	 *                                       </ul>
	 */
	public Optional<Long> getRegionResourceLevel(final RegionId regionId, final ResourceId resourceId) {
		validateRegionIdNotNull(regionId);
		validateResourceExists(resourceId);
		Long result = null;
		Map<ResourceId, Long> map = data.regionResourceLevels.get(regionId);
		if (map != null) {
			result = map.get(resourceId);
		}
		return Optional.ofNullable(result);
	}

	/**
	 * Returns the tracking policy associated with the resource.
	 * 
	 * @throws ContractException
	 *                                       <ul>
	 *                                       <li>{@linkplain ResourceError#NULL_RESOURCE_ID}
	 *                                       if the resource id is null</li>
	 *                                       <li>{@linkplain ResourceError#UNKNOWN_RESOURCE_ID}
	 *                                       if the resource id is unknown</li>
	 *                                       </ul>
	 */
	public boolean getResourceTimeTrackingPolicy(final ResourceId resourceId) {
		validateResourceExists(resourceId);
		return data.resourceTimeTrackingPolicies.get(resourceId);
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

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * data.hashCode();
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof ResourcesPluginData)) {
			return false;
		}
		ResourcesPluginData other = (ResourcesPluginData) obj;
		if (!data.equals(other.data)) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		StringBuilder builder2 = new StringBuilder();
		builder2.append("ResourcesPluginData [data=");
		builder2.append(data);
		builder2.append("]");
		return builder2.toString();
	}

	public Map<RegionId, Map<ResourceId, Long>> getRegionResourceLevels() {
		Map<RegionId, Map<ResourceId, Long>> result = new LinkedHashMap<>();
		for (RegionId regionId : data.regionResourceLevels.keySet()) {
			Map<ResourceId, Long> map = data.regionResourceLevels.get(regionId);
			Map<ResourceId, Long> newMap = new LinkedHashMap<>(map);
			result.put(regionId, newMap);
		}
		return result;
	}

	public Map<ResourceId, Double> getResourceDefaultTimes() {
		return new LinkedHashMap<>(data.resourceDefaultTimes);
	}

	public Map<ResourceId, Boolean> getResourceTimeTrackingPolicies() {
		return new LinkedHashMap<>(data.resourceTimeTrackingPolicies);
	}

	public Map<ResourceId, List<Double>> getPersonResourceTimes() {
		Map<ResourceId, List<Double>> result = new LinkedHashMap<>();
		for (ResourceId resourceId : data.personResourceTimes.keySet()) {
			List<Double> values = data.personResourceTimes.get(resourceId);
			List<Double> newValues = new ArrayList<>(values);
			result.put(resourceId, newValues);
		}
		return result;
	}

	public Map<ResourceId, List<Long>> getPersonResourceLevels() {
		Map<ResourceId, List<Long>> result = new LinkedHashMap<>();
		for (ResourceId resourceId : data.personResourceLevels.keySet()) {
			List<Long> values = data.personResourceLevels.get(resourceId);
			List<Long> newValues = new ArrayList<>(values);
			result.put(resourceId, newValues);
		}
		return result;
	}

	public Map<ResourceId, Map<ResourcePropertyId, PropertyDefinition>> getResourcePropertyDefinitions() {
		Map<ResourceId, Map<ResourcePropertyId, PropertyDefinition>> result = new LinkedHashMap<>();

		for (ResourceId resourceId : data.resourcePropertyDefinitions.keySet()) {
			Map<ResourcePropertyId, PropertyDefinition> map = data.resourcePropertyDefinitions.get(resourceId);
			Map<ResourcePropertyId, PropertyDefinition> newMap = new LinkedHashMap<>(map);
			result.put(resourceId, newMap);
		}
		return result;
	}

	public Map<ResourceId, Map<ResourcePropertyId, Object>> getResourcePropertyValues() {
		Map<ResourceId, Map<ResourcePropertyId, Object>> result = new LinkedHashMap<>();

		for (ResourceId resourceId : data.resourcePropertyValues.keySet()) {
			Map<ResourcePropertyId, Object> map = data.resourcePropertyValues.get(resourceId);
			Map<ResourcePropertyId, Object> newMap = new LinkedHashMap<>(map);
			result.put(resourceId, newMap);
		}
		return result;
	}

}