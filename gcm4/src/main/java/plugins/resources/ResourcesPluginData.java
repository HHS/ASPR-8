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
import util.errors.ContractException;
import util.wrappers.MultiKey;

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

		private final Map<ResourceId, Double> resourceIds;
		private final Map<ResourceId, Boolean> resourceTimeTrackingPolicies;
		private final Map<ResourceId, Map<ResourcePropertyId, PropertyDefinition>> resourcePropertyDefinitions;
		private final Map<ResourceId, Map<ResourcePropertyId, Object>> resourcePropertyValues;
		private final Map<ResourceId, List<Long>> personResourceLevels;
		private final Map<ResourceId, List<Double>> personResourceTimes;
		private final Map<RegionId, List<ResourceInitialization>> regionResourceLevels;

		private boolean locked;
		private final List<ResourceInitialization> emptyResourceInitializationList = Collections.unmodifiableList(new ArrayList<>());

		public Data() {
			resourceIds = new LinkedHashMap<>();
			resourceTimeTrackingPolicies = new LinkedHashMap<>();
			resourcePropertyDefinitions = new LinkedHashMap<>();
			resourcePropertyValues = new LinkedHashMap<>();
			personResourceLevels = new LinkedHashMap<>();
			personResourceTimes = new LinkedHashMap<>();
			regionResourceLevels = new LinkedHashMap<>();
		}

		public Data(Data data) {
			resourceIds = new LinkedHashMap<>(data.resourceIds);
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
				List<ResourceInitialization> list = data.regionResourceLevels.get(regionId);
				List<ResourceInitialization> newList = new ArrayList<>(list);
				regionResourceLevels.put(regionId, newList);
			}

			locked = data.locked;
		}

		/**
		 * 
		 */
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + resourceIds.hashCode();
			result = prime * result + resourceTimeTrackingPolicies.hashCode();
			result = prime * result + resourcePropertyDefinitions.hashCode();
			result = prime * result + getResourcePropertyValuesHashCode();
			result = prime * result + getRegionResourceLevelsHashCode();
			result = prime * result + getPersonResourceLevelsHashCode();
			result = prime * result + getPersonResourceTimesHashCode();
			return result;
		}

		private int getResourcePropertyValuesHashCode() {
			final int prime = 31;
			int result = 0;
			for (ResourceId resourceId : resourcePropertyValues.keySet()) {
				// the defMap might be null
				Map<ResourcePropertyId, PropertyDefinition> defMap = resourcePropertyDefinitions.get(resourceId);
				Map<ResourcePropertyId, Object> map = resourcePropertyValues.get(resourceId);
				if (map != null) {
					for (ResourcePropertyId resourcePropertyId : map.keySet()) {
						boolean addValue = true;
						Object value = map.get(resourcePropertyId);
						// the existence of the property id in a validated Data
						// implies the defMap is not null
						PropertyDefinition propertyDefinition = defMap.get(resourcePropertyId);
						Optional<Object> optional = propertyDefinition.getDefaultValue();
						if (optional.isPresent()) {
							Object defaultValue = optional.get();
							if (value.equals(defaultValue)) {
								addValue = false;
							}
						}
						if (addValue) {
							int subResult = 1;
							subResult = prime * subResult + resourceId.hashCode();
							subResult = prime * subResult + resourcePropertyId.hashCode();
							subResult = prime * subResult + value.hashCode();
							result += subResult;
						}
					}
				}
			}
			return result;
		}

		private int getRegionResourceLevelsHashCode() {
			final int prime = 31;
			int result = 0;
			for (RegionId regionId : regionResourceLevels.keySet()) {
				List<ResourceInitialization> list = regionResourceLevels.get(regionId);
				if (list != null) {
					for (ResourceInitialization resourceInitialization : list) {
						Long amount = resourceInitialization.getAmount();
						if (amount != 0L) {
							ResourceId resourceId = resourceInitialization.getResourceId();
							int subResult = 1;
							subResult = prime * subResult + regionId.hashCode();
							subResult = prime * subResult + resourceId.hashCode();
							subResult = prime * subResult + amount.hashCode();
							result += subResult;
						}
					}
				}
			}
			return result;
		}

		private int getPersonResourceLevelsHashCode() {
			final int prime = 31;
			int result = 0;
			for (int personIndex = 0; personIndex < personResourceLevels.size(); personIndex++) {
				List<ResourceInitialization> list = personResourceLevels.get(personIndex);
				if (list != null) {
					for (ResourceInitialization resourceInitialization : list) {
						Long amount = resourceInitialization.getAmount();
						if (amount != 0L) {
							ResourceId resourceId = resourceInitialization.getResourceId();
							int subResult = 1;
							subResult = prime * subResult + resourceId.hashCode();
							subResult = prime * subResult + amount.hashCode();
							result += subResult;
						}
					}
				}
			}
			return result;
		}

		private int getPersonResourceTimesHashCode() {
			return 0;
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
			 * locked -- two Datas are only compared when they are both locked
			 * -- there are no equality comparisons in this class.
			 * 
			 * emptyResourceInitializationList -- are just empty lists
			 */

			// These are simply compared:
			if (!resourceIds.equals(other.resourceIds)) {
				return false;
			}

			if (!resourceTimeTrackingPolicies.equals(other.resourceTimeTrackingPolicies)) {
				return false;
			}

			if (!resourcePropertyDefinitions.equals(other.resourcePropertyDefinitions)) {
				return false;
			}

			/*
			 * The remaining fields must be compared by disregarding assignments
			 * of default property values and zero resource levels
			 */
			if (!compareResourcePropertyValues(this, other)) {
				return false;
			}

			if (!compareRegionResourceLevels(this, other)) {
				return false;
			}

			if (!comparePersonResourceLevels(this, other)) {
				return false;
			}
			if (!comparePersonResourceTimes(this, other)) {
				return false;
			}
			return true;
		}

		/*
		 * Both Data instances have been fully formed, validated and have equal
		 * resource ids and resource property definitions
		 */
		private static boolean comparePersonResourceLevels(Data a, Data b) {

			int personCount = FastMath.max(a.personCount, b.personCount);
			for (int i = 0; i < personCount; i++) {
				Set<MultiKey> aLevels = getPersonResourceLevels(a, i);
				Set<MultiKey> bLevels = getPersonResourceLevels(b, i);
				if (!aLevels.equals(bLevels)) {
					return false;
				}
			}
			return true;
		}

		/*
		 * Both Data instances have been fully formed, validated and have equal
		 * resource ids and resource property definitions
		 */
		private static boolean comparePersonResourceTimes(Data a, Data b) {

			// int personCount = FastMath.max(a.personCount, b.personCount);
			// for (int i = 0; i < personCount; i++) {
			// Set<MultiKey> aLevels = getPersonResourceLevels(a, i);
			// Set<MultiKey> bLevels = getPersonResourceLevels(b, i);
			// if (!aLevels.equals(bLevels)) {
			// return false;
			// }
			// }
			return true;

		}

		/*
		 * Assembles a set of multi-keys from the region resource levels
		 * contained in the data that are not zero.
		 */
		private static Set<MultiKey> getPersonResourceLevels(Data data, int personIndex) {
			Set<MultiKey> result = new LinkedHashSet<>();
			if (personIndex < data.personResourceLevels.size()) {
				List<ResourceInitialization> list = data.personResourceLevels.get(personIndex);
				if (list != null) {
					for (ResourceInitialization resourceInitialization : list) {
						Long amount = resourceInitialization.getAmount();
						if (amount != 0L) {
							ResourceId resourceId = resourceInitialization.getResourceId();
							result.add(new MultiKey(resourceId, amount));
						}
					}
				}
			}
			return result;
		}

		/*
		 * Both Data instances have been fully formed, validated and have equal
		 * resource ids and resource property definitions
		 */
		private static boolean compareRegionResourceLevels(Data a, Data b) {
			Set<MultiKey> aLevels = getRegionResourceLevels(a);
			Set<MultiKey> bLevels = getRegionResourceLevels(b);
			return aLevels.equals(bLevels);
		}

		/*
		 * Assembles a set of multi-keys from the region resource levels
		 * contained in the data that are not zero.
		 */
		private static Set<MultiKey> getRegionResourceLevels(Data data) {
			Set<MultiKey> result = new LinkedHashSet<>();
			for (RegionId regionId : data.regionResourceLevels.keySet()) {
				List<ResourceInitialization> list = data.regionResourceLevels.get(regionId);
				if (list != null) {
					for (ResourceInitialization resourceInitialization : list) {
						Long amount = resourceInitialization.getAmount();
						if (amount != 0L) {
							ResourceId resourceId = resourceInitialization.getResourceId();
							result.add(new MultiKey(regionId, resourceId, amount));
						}
					}
				}
			}
			return result;
		}

		/*
		 * Both Data instances have been fully formed, validated and have equal
		 * resource ids and resource property definitions
		 */
		private static boolean compareResourcePropertyValues(Data a, Data b) {
			Set<MultiKey> aValues = getResourcePropertyValues(a);
			Set<MultiKey> bValues = getResourcePropertyValues(b);
			return aValues.equals(bValues);
		}

		/*
		 * Assembles a set of multi-keys from the resource property value
		 * contained in the data that are not default values.
		 */
		private static Set<MultiKey> getResourcePropertyValues(Data data) {
			Set<MultiKey> result = new LinkedHashSet<>();
			for (ResourceId resourceId : data.resourcePropertyValues.keySet()) {
				// This defMap might be null
				Map<ResourcePropertyId, PropertyDefinition> defMap = data.resourcePropertyDefinitions.get(resourceId);
				Map<ResourcePropertyId, Object> map = data.resourcePropertyValues.get(resourceId);
				if (map != null) {
					for (ResourcePropertyId resourcePropertyId : map.keySet()) {
						boolean addValue = true;
						Object value = map.get(resourcePropertyId);
						/*
						 * The Data was validated and since we have a property
						 * id, we know that the defMap cannot be null
						 */
						PropertyDefinition propertyDefinition = defMap.get(resourcePropertyId);
						Optional<Object> optional = propertyDefinition.getDefaultValue();
						if (optional.isPresent()) {
							Object defaultValue = optional.get();
							if (value.equals(defaultValue)) {
								addValue = false;
							}
						}
						if (addValue) {
							result.add(new MultiKey(resourceId, resourcePropertyId, value));
						}
					}
				}
			}
			return result;
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

	private static void validateResourceAmount(final Long amount) {
		if (amount == null) {
			throw new ContractException(ResourceError.NULL_AMOUNT, amount);
		}
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
		 *             <li>{@linkplain PropertyError#UNKNOWN_PROPERTY_ID} if a
		 *             resource property value was collected for a resource that
		 *             was not added</li>
		 * 
		 *             <li>{@linkplain PropertyError#UNKNOWN_PROPERTY_ID} if a
		 *             resource property value was collected for a resource
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

			if (!data.locked) {
				validateData();
			}
			ensureImmutability();
			return new ResourcesPluginData(data);
		}

		/**
		 * Adds the given resouce id with default time value. Duplicate inputs
		 * override previous inputs.
		 * 
		 * @throws ContractException
		 *             <li>{@linkplain ResourceError#NULL_RESOURCE_ID} if the
		 *             resource id is null</li>
		 *             <li>{@linkplain ResourceError#NULL_TIME} if the time is
		 *             null</li>
		 *
		 */
		public Builder addResource(final ResourceId resourceId, Double time) {
			ensureDataMutability();
			validateResourceIdNotNull(resourceId);
			validateTime(time);
			data.resourceIds.put(resourceId, time);
			return this;
		}

		/**
		 * Defines a resource property Duplicate inputs override previous
		 * inputs.
		 * 
		 * @throws ContractException
		 *             <li>{@linkplain ResourceError#NULL_RESOURCE_ID} if the
		 *             resource id is null</li>
		 * 
		 *             <li>{@linkplain PropertyError#NULL_PROPERTY_ID}</li> if
		 *             the resource property id is null
		 * 
		 *             <li>{@linkplain PropertyError#NULL_PROPERTY_DEFINITION}
		 *             </li> if the property definition is null
		 *
		 * 
		 */
		public Builder defineResourceProperty(final ResourceId resourceId, final ResourcePropertyId resourcePropertyId, final PropertyDefinition propertyDefinition) {
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
		 * Sets a person's initial resource level. Duplicate inputs override
		 * previous inputs.
		 * 
		 * @throws ContractException
		 *             <li>{@linkplain PersonError#NULL_PERSON_ID} if the person
		 *             id is null</li>
		 *             <li>{@linkplain ResourceError#NULL_RESOURCE_ID} if the
		 *             resource id is null</li>
		 *             <li>{@linkplain ResourceError#NEGATIVE_RESOURCE_AMOUNT}
		 *             if the resource amount is negative</li>
		 *             <li>{@linkplain ResourceError#NULL_AMOUNT} if the amount
		 *             is null</li>
		 * 
		 */

		public Builder setPersonResourceLevel(final PersonId personId, final ResourceId resourceId, final Long amount) {
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
		 * Sets a person's initial resource time. Duplicate inputs override
		 * previous inputs.
		 * 
		 * @throws ContractException
		 *             <li>{@linkplain PersonError#NULL_PERSON_ID} if the person
		 *             id is null</li>
		 *             <li>{@linkplain ResourceError#NULL_RESOURCE_ID} if the
		 *             resource id is null</li>
		 *             <li>{@linkplain ResourceError#NEGATIVE_RESOURCE_AMOUNT}
		 *             if the resource amount is negative</li> *
		 *             <li>{@linkplain ResourceError#NULL_TIME} if the time is
		 *             null</li>
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
		 * Sets a region's initial resource level. Duplicate inputs override
		 * previous inputs.
		 * 
		 * @throws ContractException
		 *             <li>{@linkplain RegionError#NULL_REGION_ID} if the region
		 *             id is null</li>
		 *             <li>{@linkplain ResourceError#NULL_RESOURCE_ID} if the
		 *             resource id is null</li>
		 *             <li>{@linkplain ResourceError#NEGATIVE_RESOURCE_AMOUNT}
		 *             if the resource amount is negative</li> *
		 *
		 */

		public Builder setRegionResourceLevel(final RegionId regionId, final ResourceId resourceId, final long amount) {
			ensureDataMutability();
			validateRegionIdNotNull(regionId);
			validateResourceIdNotNull(resourceId);
			validateResourceAmount(amount);
			List<ResourceInitialization> resourceInitializations = data.regionResourceLevels.get(regionId);

			if (resourceInitializations == null) {
				resourceInitializations = new ArrayList<>();
				data.regionResourceLevels.put(regionId, resourceInitializations);
			}

			int index = -1;

			for (int i = 0; i < resourceInitializations.size(); i++) {
				if (resourceInitializations.get(i).getResourceId().equals(resourceId)) {
					index = i;
					break;
				}
			}

			if (index == -1) {
				resourceInitializations.add(new ResourceInitialization(resourceId, amount));
			} else {
				resourceInitializations.set(index, new ResourceInitialization(resourceId, amount));
			}

			return this;
		}

		/**
		 * Sets a resource property value. Duplicate inputs override previous
		 * inputs.
		 * 
		 * @throws ContractException
		 *             <li>{@linkplain ResourceError#NULL_RESOURCE_ID} if the
		 *             resource id is null</li>
		 *             <li>{@linkplain PropertyError#NULL_PROPERTY_ID} if the
		 *             resource property id is null</li>
		 *             <li>{@linkplain PropertyError#NULL_PROPERTY_VALUE} if the
		 *             resource property value is null</li>
		 *
		 */
		public Builder setResourcePropertyValue(final ResourceId resourceId, final ResourcePropertyId resourcePropertyId, final Object resourcePropertyValue) {
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
		 * Sets the time tracking policy for a resource. Duplicate inputs
		 * override previous inputs.
		 * 
		 * @throws ContractException
		 *             <li>{@linkplain ResourceError#NULL_RESOURCE_ID} if the
		 *             resource id is null</li>
		 *             <li>{@linkplain ResourceError#NULL_TIME_TRACKING_POLICY}
		 *             if the tracking policy is null</li>
		 *
		 */
		public Builder setResourceTimeTracking(final ResourceId resourceId, final boolean trackValueAssignmentTimes) {
			ensureDataMutability();
			validateResourceIdNotNull(resourceId);
			data.resourceTimeTrackingPolicies.put(resourceId, trackValueAssignmentTimes);
			return this;
		}

		private void validateData() {

			/*
			 *  validate and fill in resourceTimeTrackingPolicies
			 */
			for (ResourceId resourceId : data.resourceTimeTrackingPolicies.keySet()) {
				if (!data.resourceIds.containsKey(resourceId)) {
					throw new ContractException(ResourceError.UNKNOWN_RESOURCE_ID, resourceId + " has a resource tracking policy but is not a known resource id");
				}
			}

			//filling in with false
			for (ResourceId resourceId : data.resourceIds.keySet()) {
				if (!data.resourceTimeTrackingPolicies.containsKey(resourceId)) {
					data.resourceTimeTrackingPolicies.put(resourceId, false);
				}
			}

			/*
			 *  validate and fill in resourcePropertyDefinitions
			 */
			for (ResourceId resourceId : data.resourcePropertyDefinitions.keySet()) {
				if (!data.resourceIds.containsKey(resourceId)) {
					throw new ContractException(ResourceError.UNKNOWN_RESOURCE_ID, resourceId + " has a property definitions but is not a known resource id");
				}
			}

			//filling in with empty maps
			for (ResourceId resourceId : data.resourceIds.keySet()) {
				if (!data.resourcePropertyDefinitions.containsKey(resourceId)) {
					data.resourcePropertyDefinitions.put(resourceId, new LinkedHashMap<>());
				}
			}

			/*
			 *  validate and fill in resourcePropertyValues
			 *  
			 *  show that the resource ids are in data.resourceids
			 *  
			 *  show that the resource property ids are in the data.resourcePropertyDefinitions
			 *  
			 *  show that the values are compatible with the property definitions
			 */
			for (ResourceId resourceId : data.resourcePropertyValues.keySet()) {
				if (!data.resourceIds.containsKey(resourceId)) {
					throw new ContractException(ResourceError.UNKNOWN_RESOURCE_ID, resourceId + " for a collected resource property value");
				}
				Map<ResourcePropertyId, PropertyDefinition> propMap = data.resourcePropertyDefinitions.get(resourceId);
				Map<ResourcePropertyId, Object> map = data.resourcePropertyValues.get(resourceId);
				for (ResourcePropertyId resourcePropertyId : map.keySet()) {
					if (!propMap.containsKey(resourcePropertyId)) {
						throw new ContractException(PropertyError.UNKNOWN_PROPERTY_ID,
								resourcePropertyId + " has a resource property value under resource " + resourceId + " but there is no corresponding property definition");
					}
					Object propertyValue = map.get(resourcePropertyId);
					PropertyDefinition propertyDefinition = propMap.get(resourcePropertyId);
					if (!propertyDefinition.getType().isAssignableFrom(propertyValue.getClass())) {
						throw new ContractException(PropertyError.INCOMPATIBLE_VALUE, resourcePropertyId + " has a resource property value of " + propertyValue + " under resource " + resourceId
								+ " that is incompatible with the corresponding property definition");
					}
				}
			}

			//fill in with empty maps
			for (ResourceId resourceId : data.resourceIds.keySet()) {
				if (!data.resourcePropertyValues.containsKey(resourceId)) {
					data.resourcePropertyValues.put(resourceId, new LinkedHashMap<>());
				}
			}

			//show that property definitions without default values have complete value coverage
			for (ResourceId resourceId : data.resourcePropertyDefinitions.keySet()) {
				Map<ResourcePropertyId, PropertyDefinition> propertyDefinitionMap = data.resourcePropertyDefinitions.get(resourceId);
				for (ResourcePropertyId resourcePropertyId : propertyDefinitionMap.keySet()) {
					PropertyDefinition propertyDefinition = propertyDefinitionMap.get(resourcePropertyId);
					if (!propertyDefinition.getDefaultValue().isPresent()) {
						Object propertyValue = null;
						Map<ResourcePropertyId, Object> propertyValueMap = data.resourcePropertyValues.get(resourceId);						
						propertyValue = propertyValueMap.get(resourcePropertyId);						
						if (propertyValue == null) {
							throw new ContractException(PropertyError.INSUFFICIENT_PROPERTY_VALUE_ASSIGNMENT, resourceId + ": " + resourcePropertyId);
						}
					}
				}
			}

			/*
			 *  validate regionResourceLevels
			 */
			for (RegionId regionId : data.regionResourceLevels.keySet()) {
				List<ResourceInitialization> list = data.regionResourceLevels.get(regionId);
				for (ResourceInitialization resourceInitialization : list) {
					ResourceId resourceId = resourceInitialization.getResourceId();
					if (!data.resourceIds.containsKey(resourceId)) {
						throw new ContractException(ResourceError.UNKNOWN_RESOURCE_ID, resourceId + " for region " + regionId + " has a level, but is not a known resource");
					}
				}
			}

			/*
			 * validate and fill in personResourceLevels
			 * private final Map<ResourceId, List<Long>> personResourceLevels;
			 */
			srtghkaeroptakweptoawket
			
			for(ResourceId resourceId : data.personResourceLevels.keySet()) {
				if (!data.resourceIds.containsKey(resourceId)) {
					throw new ContractException(ResourceError.UNKNOWN_RESOURCE_ID, resourceId + " has person resource levels, but is not a known resource id");
				}
			}
			
			//filling in with empty lists
			for(ResourceId resourceId : data.resourceIds.keySet()) {
				if (!data.personResourceLevels.containsKey(resourceId)) {
					data.personResourceLevels.put(resourceId, new ArrayList<>());
				}
			}

			// private final Map<ResourceId, List<Double>> personResourceTimes;

			

		}

	}

	private static void validateTime(Double time) {
		if (time == null) {
			throw new ContractException(ResourceError.NULL_TIME);
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
	 *             <li>{@linkplain PropertyError#NULL_PROPERTY_ID} if the
	 *             resource property id is null</li>
	 *             <li>{@linkplain PropertyError#UNKNOWN_PROPERTY_ID} if the
	 *             resource property id is unknown</li>
	 * 
	 */
	public PropertyDefinition getResourcePropertyDefinition(final ResourceId resourceId, final ResourcePropertyId resourcePropertyId) {
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
	 *             <li>{@linkplain ResourceError#NULL_RESOURCE_ID} if the
	 *             resource id is null</li>
	 *             <li>{@linkplain ResourceError#UNKNOWN_RESOURCE_ID} if the
	 *             resource id is unknown</li>
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
		if (!data.resourceIds.containsKey(resourceId)) {
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
	 *             <li>{@linkplain PropertyError#NULL_PROPERTY_ID} if the
	 *             resource property id is null</li>
	 *             <li>{@linkplain PropertyError#UNKNOWN_PROPERTY_ID} if the
	 *             resource property id is unknown</li>
	 */
	@SuppressWarnings("unchecked")
	public <T> T getResourcePropertyValue(final ResourceId resourceId, final ResourcePropertyId resourcePropertyId) {
		validateResourceExists(resourceId);
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
	 * Returns an unmodifiable list of the initial resource levels for the given
	 * resource id. May contain null, may be empty.
	 * 
	 * @throws ContractException
	 * 
	 *             <li>{@linkplain ResourceError#NULL_RESOURCE_ID} if the
	 *             resource id is null</li>
	 *             <li>{@linkplain ResourceError#UNKNOWN_RESOURCE_ID} if the
	 *             resource id is unknown</li>
	 * 
	 */
	public List<Long> getPersonResourceLevels(final ResourceId resourcId) {
		validateResourceExists(resourcId);
		List<Long> list = data.personResourceLevels.get(resourcId);
		return Collections.unmodifiableList(list);
	}

	/**
	 * Returns an unmodifiable list of the initial resource levels for the given
	 * resource id. May contain null, may be empty.
	 * 
	 * @throws ContractException
	 * 
	 *             <li>{@linkplain ResourceError#NULL_RESOURCE_ID} if the
	 *             resource id is null</li>
	 *             <li>{@linkplain ResourceError#UNKNOWN_RESOURCE_ID} if the
	 *             resource id is unknown</li>
	 * 
	 */
	public List<Double> getPersonResourceTimes(final ResourceId resourcId) {
		validateResourceExists(resourcId);
		List<Double> list = data.personResourceTimes.get(resourcId);
		return Collections.unmodifiableList(list);
	}

	/**
	 * Returns the resource ids
	 */
	@SuppressWarnings("unchecked")
	public <T extends ResourceId> Set<T> getResourceIds() {
		Set<T> result = new LinkedHashSet<>(data.resourceIds.size());
		for (ResourceId resourceId : data.resourceIds.keySet()) {
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
		if (list == null) {
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
	public boolean getPersonResourceTimeTrackingPolicy(final ResourceId resourceId) {
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

}
