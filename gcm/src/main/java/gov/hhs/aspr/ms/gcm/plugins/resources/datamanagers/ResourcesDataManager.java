package gov.hhs.aspr.ms.gcm.plugins.resources.datamanagers;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.apache.commons.math3.util.FastMath;

import gov.hhs.aspr.ms.gcm.nucleus.DataManager;
import gov.hhs.aspr.ms.gcm.nucleus.DataManagerContext;
import gov.hhs.aspr.ms.gcm.nucleus.Event;
import gov.hhs.aspr.ms.gcm.nucleus.EventFilter;
import gov.hhs.aspr.ms.gcm.nucleus.IdentifiableFunctionMap;
import gov.hhs.aspr.ms.gcm.nucleus.NucleusError;
import gov.hhs.aspr.ms.gcm.plugins.people.datamanagers.PeopleDataManager;
import gov.hhs.aspr.ms.gcm.plugins.people.events.PersonImminentAdditionEvent;
import gov.hhs.aspr.ms.gcm.plugins.people.events.PersonRemovalEvent;
import gov.hhs.aspr.ms.gcm.plugins.people.support.PersonConstructionData;
import gov.hhs.aspr.ms.gcm.plugins.people.support.PersonError;
import gov.hhs.aspr.ms.gcm.plugins.people.support.PersonId;
import gov.hhs.aspr.ms.gcm.plugins.regions.datamanagers.RegionsDataManager;
import gov.hhs.aspr.ms.gcm.plugins.regions.events.RegionAdditionEvent;
import gov.hhs.aspr.ms.gcm.plugins.regions.support.RegionError;
import gov.hhs.aspr.ms.gcm.plugins.regions.support.RegionId;
import gov.hhs.aspr.ms.gcm.plugins.resources.events.PersonResourceUpdateEvent;
import gov.hhs.aspr.ms.gcm.plugins.resources.events.RegionResourceUpdateEvent;
import gov.hhs.aspr.ms.gcm.plugins.resources.events.ResourceIdAdditionEvent;
import gov.hhs.aspr.ms.gcm.plugins.resources.events.ResourcePropertyDefinitionEvent;
import gov.hhs.aspr.ms.gcm.plugins.resources.events.ResourcePropertyUpdateEvent;
import gov.hhs.aspr.ms.gcm.plugins.resources.support.ResourceError;
import gov.hhs.aspr.ms.gcm.plugins.resources.support.ResourceId;
import gov.hhs.aspr.ms.gcm.plugins.resources.support.ResourceInitialization;
import gov.hhs.aspr.ms.gcm.plugins.resources.support.ResourcePropertyId;
import gov.hhs.aspr.ms.gcm.plugins.resources.support.ResourcePropertyInitialization;
import gov.hhs.aspr.ms.gcm.plugins.util.properties.PropertyDefinition;
import gov.hhs.aspr.ms.gcm.plugins.util.properties.PropertyError;
import gov.hhs.aspr.ms.gcm.plugins.util.properties.arraycontainers.DoubleValueContainer;
import gov.hhs.aspr.ms.gcm.plugins.util.properties.arraycontainers.IntValueContainer;
import util.errors.ContractException;
import util.wrappers.MutableLong;

/**
 * Data manager for resources. Resource property values are generally mutable
 * and specific to the type of resource.
 */
public final class ResourcesDataManager extends DataManager {

	private PeopleDataManager peopleDataManager;
	private RegionsDataManager regionsDataManager;

	// resources
	private Map<ResourceId, Map<ResourcePropertyId, Object>> resourcePropertyValues = new LinkedHashMap<>();

	/*
	 * Stores resource amounts per person keyed by the resourceId
	 */
	private final Map<ResourceId, IntValueContainer> personResourceLevels = new LinkedHashMap<>();

	private Map<ResourceId, Map<ResourcePropertyId, PropertyDefinition>> resourcePropertyDefinitions = new LinkedHashMap<>();

	/*
	 * Stores resource assignment times per person keyed by the resourceId. Key
	 * existence subject to time recording policies specified by the scenario.
	 */
	private Map<ResourceId, Double> resourceDefaultTimes = new LinkedHashMap<>();

	private Map<ResourceId, Boolean> resourceTimeTrackingPolicies = new LinkedHashMap<>();

	private final Map<ResourceId, DoubleValueContainer> personResourceTimes = new LinkedHashMap<>();

	private final Map<RegionId, Map<ResourceId, MutableLong>> regionResources = new LinkedHashMap<>();

	private final ResourcesPluginData resourcesPluginData;

	private DataManagerContext dataManagerContext;

	/**
	 * Constructs the PersonResourceManager from the context
	 *
	 * @throws ContractException {@linkplain ResourceError#NULL_RESOURCE_PLUGIN_DATA}
	 *                           if the plugin data is null
	 */
	public ResourcesDataManager(final ResourcesPluginData resourcesPluginData) {
		if (resourcesPluginData == null) {
			throw new ContractException(ResourceError.NULL_RESOURCE_PLUGIN_DATA);
		}
		this.resourcesPluginData = resourcesPluginData;
	}

	/**
	 * Reduces the resource for the particular person and resource by the amount.
	 *
	 * @throws RuntimeException
	 *                          <ul>
	 *                          <li>if the resource id is null</li>
	 *                          <li>if the resource id is unknown</li>
	 *                          <li>if the person id null</li>
	 *                          <li>if the amount causes an overflow</li>
	 *                          </ul>
	 */
	private void decrementPersonResourceLevel(final ResourceId resourceId, final PersonId personId,
			final long resourceAmount) {
		personResourceLevels.get(resourceId).decrementLongValue(personId.getValue(), resourceAmount);
		/*
		 * if the resource assignment times are being tracked, then record the resource
		 * time.
		 */
		Boolean trackTimes = resourceTimeTrackingPolicies.get(resourceId);
		if (trackTimes) {
			DoubleValueContainer doubleValueContainer = personResourceTimes.get(resourceId);
			if (doubleValueContainer == null) {
				double resourceDefinitionTime = resourceDefaultTimes.get(resourceId);
				doubleValueContainer = new DoubleValueContainer(resourceDefinitionTime,
						peopleDataManager::getPersonIndexIterator);
				personResourceTimes.put(resourceId, doubleValueContainer);
			}
			doubleValueContainer.setValue(personId.getValue(), dataManagerContext.getTime());
		}

	}

	/**
	 * Reduces the resource for the particular region and resource by the amount.
	 *
	 * @throws RuntimeException</li>
	 *                               <li>if the resource id is null</li>
	 *                               <li>if the resource id is unknown</li>
	 *                               <li>if the region id null</li>
	 *                               <li>if the region id is unknown</li>
	 * @throws ContractException
	 *                               <ul>
	 *                               <li>{@linkplain ResourceError#NEGATIVE_RESOURCE_AMOUNT}
	 *                               if the amount is negative</li>
	 *                               <li>{@linkplain ResourceError#INSUFFICIENT_RESOURCES_AVAILABLE}
	 *                               if the amount exceeds the current balance</li>
	 *                               </ul>
	 */
	private void decrementRegionResourceLevel(final RegionId regionId, final ResourceId resourceId, final long amount) {
		Map<ResourceId, MutableLong> map = regionResources.get(regionId);
		if (map == null) {
			map = new LinkedHashMap<>();
			regionResources.put(regionId, map);
		}
		MutableLong mutableLong = map.get(resourceId);
		if (mutableLong == null) {
			mutableLong = new MutableLong();
			map.put(resourceId, mutableLong);
		}
		mutableLong.decrement(amount);
	}

	/**
	 * Expands the capacity of data structures to hold people by the given count.
	 * Used to more efficiently prepare for multiple population additions.
	 * 
	 * @throws ContractException {@linkplain PersonError#NEGATIVE_GROWTH_PROJECTION}
	 *                           if the count is negative
	 */
	public void expandCapacity(final int count) {
		if (count < 0) {
			throw new ContractException(PersonError.NEGATIVE_GROWTH_PROJECTION);
		}
		if (count > 0) {

			for (final ResourceId resourceId : personResourceLevels.keySet()) {
				final IntValueContainer intValueContainer = personResourceLevels.get(resourceId);
				intValueContainer.setCapacity(intValueContainer.getCapacity() + count);
			}

			for (final ResourceId resourceId : personResourceTimes.keySet()) {
				final DoubleValueContainer doubleValueContainer = personResourceTimes.get(resourceId);
				doubleValueContainer.setCapacity(doubleValueContainer.getCapacity() + count);
			}
		}
	}

	/**
	 * Returns the set of people who do not have any of the given resource as a
	 * list.
	 *
	 * @throws ContractException
	 *                           <ul>
	 *                           <li>{@linkplain ResourceError#NULL_RESOURCE_ID} if
	 *                           the resource id is null</li>
	 *                           <li>{@linkplain ResourceError#UNKNOWN_RESOURCE_ID}
	 *                           if the resource id is unknown</li>
	 *                           </ul>
	 */
	public List<PersonId> getPeopleWithoutResource(final ResourceId resourceId) {
		validateResourceId(resourceId);
		/*
		 * First, we loop through all possible person id values and determine the exact
		 * size of the returned list.
		 */
		final IntValueContainer intValueContainer = personResourceLevels.get(resourceId);
		if (intValueContainer == null) {
			return new ArrayList<>();
		}

		int count = 0;
		final int n = peopleDataManager.getPersonIdLimit();
		for (int personIndex = 0; personIndex < n; personIndex++) {
			if (peopleDataManager.personIndexExists(personIndex)) {
				final long resourceLevel = intValueContainer.getValueAsLong(personIndex);
				if (resourceLevel == 0) {
					count++;
				}
			}
		}

		/*
		 * Now we create the list
		 */
		final List<PersonId> result = new ArrayList<>(count);

		/*
		 * We loop again and add the people to the list
		 */
		for (int personId = 0; personId < n; personId++) {
			if (peopleDataManager.personIndexExists(personId)) {
				final long resourceLevel = intValueContainer.getValueAsLong(personId);
				if (resourceLevel == 0) {
					result.add(peopleDataManager.getBoxedPersonId(personId).get());
				}
			}
		}
		return result;
	}

	/**
	 * Returns the list of people who have a non-zero level of the resource
	 *
	 * @throws ContractException
	 *                           <ul>
	 *                           <li>{@linkplain ResourceError#NULL_RESOURCE_ID} if
	 *                           the resource id is null</li>
	 *                           <li>{@linkplain ResourceError#UNKNOWN_RESOURCE_ID}
	 *                           if the resource id is unknown</li>
	 *                           </ul>
	 */
	public List<PersonId> getPeopleWithResource(final ResourceId resourceId) {
		validateResourceId(resourceId);
		/*
		 * First, we loop through all possible person id values and determine the exact
		 * size of the returned list.
		 */
		final IntValueContainer intValueContainer = personResourceLevels.get(resourceId);

		if (intValueContainer == null) {
			return new ArrayList<>();
		}

		int count = 0;
		final int n = peopleDataManager.getPersonIdLimit();
		for (int personId = 0; personId < n; personId++) {
			if (peopleDataManager.personIndexExists(personId)) {
				final long resourceLevel = intValueContainer.getValueAsLong(personId);
				if (resourceLevel > 0) {
					count++;
				}
			}
		}
		/*
		 * Now we create the list
		 */
		final List<PersonId> result = new ArrayList<>(count);
		/*
		 * We loop again and add the people to the list
		 */
		for (int personIndex = 0; personIndex < n; personIndex++) {
			if (peopleDataManager.personIndexExists(personIndex)) {
				final long resourceLevel = intValueContainer.getValueAsLong(personIndex);
				if (resourceLevel > 0) {
					result.add(peopleDataManager.getBoxedPersonId(personIndex).get());
				}
			}
		}
		return result;

	}

	/**
	 * Returns the region resource level.
	 *
	 * @throws ContractException
	 *                           <ul>
	 *                           <li>{@linkplain RegionError#NULL_REGION_ID} if the
	 *                           region id is null</li>
	 *                           <li>{@linkplain RegionError#UNKNOWN_REGION_ID} if
	 *                           the region id is unknown</li>
	 *                           <li>{@linkplain ResourceError#NULL_RESOURCE_ID} if
	 *                           the resource id is null</li>
	 *                           <li>{@linkplain ResourceError#UNKNOWN_RESOURCE_ID}
	 *                           if the resource id is unknown</li>
	 *                           </ul>
	 */
	public long getPersonResourceLevel(final ResourceId resourceId, final PersonId personId) {
		validatePersonExists(personId);
		validateResourceId(resourceId);
		long result = 0;
		IntValueContainer intValueContainer = personResourceLevels.get(resourceId);
		if (intValueContainer != null) {
			result = intValueContainer.getValueAsLong(personId.getValue());
		}
		return result;

	}

	private long _getPersonResourceLevel(final ResourceId resourceId, final PersonId personId) {

		long result = 0;
		IntValueContainer intValueContainer = personResourceLevels.get(resourceId);
		if (intValueContainer != null) {
			result = intValueContainer.getValueAsLong(personId.getValue());
		}
		return result;

	}

	/**
	 * Returns the time when the resource level was last assigned for the given
	 * person and resource
	 *
	 * @throws ContractException
	 *                           <ul>
	 *                           <li>{@linkplain PersonError#NULL_PERSON_ID} if the
	 *                           person id is null</li>
	 *                           <li>{@linkplain PersonError#UNKNOWN_PERSON_ID} if
	 *                           the person id is unknown</li>
	 *                           <li>{@linkplain ResourceError#NULL_RESOURCE_ID} if
	 *                           the resource id is null</li>
	 *                           <li>{@linkplain ResourceError#UNKNOWN_RESOURCE_ID}
	 *                           if the resource id is unknown</li>
	 *                           <li>{@linkplain ResourceError#RESOURCE_ASSIGNMENT_TIME_NOT_TRACKED}
	 *                           if assignment times are not tracked for the
	 *                           resource when applied to people</li>
	 *                           </ul>
	 */
	public double getPersonResourceTime(final ResourceId resourceId, final PersonId personId) {
		validatePersonExists(personId);
		validateResourceId(resourceId);
		validatePersonResourceTimesTracked(resourceId);
		final DoubleValueContainer doubleValueContainer = personResourceTimes.get(resourceId);
		if (doubleValueContainer == null) {
			return resourceDefaultTimes.get(resourceId);
		} else {
			return doubleValueContainer.getValue(personId.getValue());
		}
	}

	/**
	 * Returns the time tracking policy for the given resource
	 *
	 * @throws ContractException
	 *                           <ul>
	 *                           <li>{@linkplain ResourceError#NULL_RESOURCE_ID} if
	 *                           the resource id is null</li>
	 *                           <li>{@linkplain ResourceError#UNKNOWN_RESOURCE_ID}
	 *                           if the resource id is unknown</li>
	 *                           </ul>
	 */
	public boolean getPersonResourceTimeTrackingPolicy(final ResourceId resourceId) {
		validateResourceId(resourceId);
		return resourceTimeTrackingPolicies.get(resourceId);
	}

	/**
	 * Returns the current resource level for the given resource id and region id
	 *
	 * @throws ContractException
	 *                           <ul>
	 *                           <li>{@linkplain RegionError#NULL_REGION_ID} if the
	 *                           region id is null</li>
	 *                           <li>{@linkplain RegionError#UNKNOWN_REGION_ID} if
	 *                           the region id is unknown</li>
	 *                           <li>{@linkplain ResourceError#NULL_RESOURCE_ID} if
	 *                           the resource id is null</li>
	 *                           <li>{@linkplain ResourceError#UNKNOWN_RESOURCE_ID}
	 *                           if the resource id is unknown</li>
	 *                           </ul>
	 */
	public long getRegionResourceLevel(final RegionId regionId, final ResourceId resourceId) {
		validateRegionId(regionId);
		validateResourceId(resourceId);

		return _getRegionResourceLevel(regionId, resourceId);
	}

	private long _getRegionResourceLevel(final RegionId regionId, final ResourceId resourceId) {

		long result = 0;

		Map<ResourceId, MutableLong> map = regionResources.get(regionId);
		if (map != null) {
			MutableLong mutableLong = map.get(resourceId);
			if (mutableLong != null) {
				result = mutableLong.getValue();
			}
		}
		return result;
	}

	/**
	 * Returns the resource ids
	 */
	@SuppressWarnings("unchecked")
	public <T extends ResourceId> Set<T> getResourceIds() {
		final Set<T> result = new LinkedHashSet<>(resourceDefaultTimes.size());
		for (final ResourceId resourceId : resourceDefaultTimes.keySet()) {
			result.add((T) resourceId);
		}
		return result;
	}

	private void validateResourceTypeIsUnknown(final ResourceId resourceId) {
		if (resourceId == null) {
			throw new ContractException(ResourceError.NULL_RESOURCE_ID);
		}
		if (resourceDefaultTimes.containsKey(resourceId)) {
			throw new ContractException(ResourceError.DUPLICATE_RESOURCE_ID, resourceId);
		}
	}

	/*
	 * Precondition : the resource id must exist
	 */
	private void validateNewResourcePropertyId(final ResourceId resourceId,
			final ResourcePropertyId resourcePropertyId) {

		final Map<ResourcePropertyId, PropertyDefinition> map = resourcePropertyDefinitions.get(resourceId);

		if ((map != null) && map.containsKey(resourcePropertyId)) {
			throw new ContractException(PropertyError.DUPLICATE_PROPERTY_DEFINITION, resourcePropertyId);
		}
	}

	private static record ResourcePropertyDefinitionMutationEvent(
			ResourcePropertyInitialization resourcePropertyInitialization) implements Event {
	}

	/**
	 * Defines a new resource property. Generates the corresponding
	 * ResourcePropertyAdditionEvent.
	 * 
	 * @throws ContractException
	 *                           <ul>
	 *                           <li>{@linkplain ResourceError#UNKNOWN_RESOURCE_ID}
	 *                           if the resource id is unknown</li>
	 *                           <li>{@linkplain PropertyError#DUPLICATE_PROPERTY_DEFINITION}
	 *                           if the resource property is already defined</li>
	 *                           </ul>
	 */
	public void defineResourceProperty(ResourcePropertyInitialization resourcePropertyInitialization) {
		dataManagerContext
				.releaseMutationEvent(new ResourcePropertyDefinitionMutationEvent(resourcePropertyInitialization));
	}

	private void handleResourcePropertyDefinitionMutationEvent(DataManagerContext dataManagerContext,
			ResourcePropertyDefinitionMutationEvent resourcePropertyDefinitionMutationEvent) {
		ResourcePropertyInitialization resourcePropertyInitialization = resourcePropertyDefinitionMutationEvent
				.resourcePropertyInitialization();

		ResourceId resourceId = resourcePropertyInitialization.getResourceId();
		ResourcePropertyId resourcePropertyId = resourcePropertyInitialization.getResourcePropertyId();
		PropertyDefinition propertyDefinition = resourcePropertyInitialization.getPropertyDefinition();

		validateResourceId(resourceId);
		validateNewResourcePropertyId(resourceId, resourcePropertyId);

		Map<ResourcePropertyId, PropertyDefinition> defMap = resourcePropertyDefinitions.get(resourceId);
		if (defMap == null) {
			defMap = new LinkedHashMap<>();
			resourcePropertyDefinitions.put(resourceId, defMap);
		}
		defMap.put(resourcePropertyId, propertyDefinition);

		Object propertyValue;
		Optional<Object> optionalValue = resourcePropertyInitialization.getValue();
		if (optionalValue.isPresent()) {
			propertyValue = optionalValue.get();
			Map<ResourcePropertyId, Object> map = resourcePropertyValues.get(resourceId);
			if (map == null) {
				map = new LinkedHashMap<>();
				resourcePropertyValues.put(resourceId, map);
			}
			map.put(resourcePropertyId, propertyValue);
		} else {
			propertyValue = propertyDefinition.getDefaultValue().get();
		}

		if (dataManagerContext.subscribersExist(ResourcePropertyDefinitionEvent.class)) {
			dataManagerContext.releaseObservationEvent(
					new ResourcePropertyDefinitionEvent(resourceId, resourcePropertyId, propertyValue));
		}

	}

	private record ResourceIdAdditionMutationEvent(ResourceId resourceId, boolean timeTrackingPolicy) implements Event {
	}

	/**
	 * Adds a resource type. Generates a corresponding ResourceIdAdditionEvent.
	 * 
	 * @throws ContractException
	 *                           <ul>
	 *                           <li>{@linkplain ResourceError#NULL_RESOURCE_ID} if
	 *                           the resource id is null</li>
	 *                           <li>{@linkplain ResourceError#DUPLICATE_RESOURCE_ID}
	 *                           if the resource type is already present</li>
	 *                           </ul>
	 */
	public void addResourceId(ResourceId resourceId, boolean timeTrackingPolicy) {

		dataManagerContext.releaseMutationEvent(new ResourceIdAdditionMutationEvent(resourceId, timeTrackingPolicy));
	}

	private void handleResourceIdAdditionMutationEvent(DataManagerContext dataManagerContext,
			ResourceIdAdditionMutationEvent resourceIdAdditionMutationEvent) {
		ResourceId resourceId = resourceIdAdditionMutationEvent.resourceId();
		validateResourceTypeIsUnknown(resourceId);
		boolean trackTimes = resourceIdAdditionMutationEvent.timeTrackingPolicy();
		double resourceDefinitionTime = dataManagerContext.getTime();
		resourceDefaultTimes.put(resourceId, resourceDefinitionTime);

		resourceTimeTrackingPolicies.put(resourceId, trackTimes);

		// release notice that a new resource id has been added
		if (dataManagerContext.subscribersExist(ResourceIdAdditionEvent.class)) {
			dataManagerContext.releaseObservationEvent(new ResourceIdAdditionEvent(resourceId, trackTimes));
		}

	}

	/**
	 * Returns the property definition for the given resource id and resource
	 * property id
	 *
	 * @throws ContractException
	 *                           <ul>
	 *                           <li>{@linkplain ResourceError#NULL_RESOURCE_ID} if
	 *                           the resource id is null</li>
	 *                           <li>{@linkplain ResourceError#UNKNOWN_RESOURCE_ID}
	 *                           if the resource id is unknown</li>
	 *                           <li>{@linkplain PropertyError#NULL_PROPERTY_ID} if
	 *                           the resource property id is null</li>
	 *                           <li>{@linkplain PropertyError#UNKNOWN_PROPERTY_ID}
	 *                           if the resource property id is unknown</li>
	 *                           </ul>
	 */
	public PropertyDefinition getResourcePropertyDefinition(final ResourceId resourceId,
			final ResourcePropertyId resourcePropertyId) {
		validateResourceId(resourceId);
		validateResourcePropertyId(resourceId, resourcePropertyId);
		return resourcePropertyDefinitions.get(resourceId).get(resourcePropertyId);
	}

	/**
	 * Returns the resource property id values for the given resource id
	 *
	 * @throws ContractException
	 *                           <ul>
	 *                           <li>{@linkplain ResourceError#NULL_RESOURCE_ID} if
	 *                           the resource id is null</li>
	 *                           <li>{@linkplain ResourceError#UNKNOWN_RESOURCE_ID}
	 *                           if the resource id is unknown</li>
	 *                           </ul>
	 */
	@SuppressWarnings("unchecked")
	public <T extends ResourcePropertyId> Set<T> getResourcePropertyIds(final ResourceId resourceId) {
		validateResourceId(resourceId);
		Set<T> result;
		final Map<ResourcePropertyId, PropertyDefinition> defMap = resourcePropertyDefinitions.get(resourceId);
		if (defMap != null) {
			result = new LinkedHashSet<>(defMap.keySet().size());
			for (final ResourcePropertyId resourcePropertyId : defMap.keySet()) {
				result.add((T) resourcePropertyId);
			}
		} else {
			result = new LinkedHashSet<>();
		}
		return result;
	}

	/**
	 * Returns the value of the resource property.
	 *
	 * @throws ContractException
	 *                           <ul>
	 *                           <li>{@linkplain ResourceError#NULL_RESOURCE_ID} if
	 *                           the resource id is null</li>
	 *                           <li>{@linkplain ResourceError#UNKNOWN_RESOURCE_ID}
	 *                           if the resource id is unknown</li>
	 *                           <li>{@linkplain PropertyError#NULL_PROPERTY_ID} if
	 *                           the resource property id is null</li>
	 *                           <li>{@linkplain PropertyError#UNKNOWN_PROPERTY_ID}
	 *                           if the resource property id is unknown</li>
	 *                           </ul>
	 */
	@SuppressWarnings("unchecked")
	public <T> T getResourcePropertyValue(final ResourceId resourceId, final ResourcePropertyId resourcePropertyId) {
		validateResourceId(resourceId);
		validateResourcePropertyId(resourceId, resourcePropertyId);
		Object result = null;
		Map<ResourcePropertyId, Object> valueMap = resourcePropertyValues.get(resourceId);
		if (valueMap != null) {
			result = valueMap.get(resourcePropertyId);
		}
		if (result == null) {
			Map<ResourcePropertyId, PropertyDefinition> defMap = resourcePropertyDefinitions.get(resourceId);
			PropertyDefinition propertyDefinition = defMap.get(resourcePropertyId);
			result = propertyDefinition.getDefaultValue().get();
		}
		return (T) result;
	}

	/**
	 * Increase the resource for the particular person and resource by the amount.
	 *
	 * @throws RuntimeException
	 *                          <li>if the resource id is null</li>
	 *                          <li>if the resource id is unknown</li>
	 *                          <li>if the person id null</li>
	 *                          <li>if the amount causes an overflow</li>
	 *                          </ul>
	 */
	private void incrementPersonResourceLevel(final ResourceId resourceId, final PersonId personId,
			final long resourceAmount) {
		IntValueContainer intValueContainer = personResourceLevels.get(resourceId);
		if (intValueContainer == null) {
			intValueContainer = new IntValueContainer(0L, peopleDataManager::getPersonIndexIterator);
			personResourceLevels.put(resourceId, intValueContainer);
		}
		intValueContainer.incrementLongValue(personId.getValue(), resourceAmount);
		/*
		 * if the resource assignment times are being tracked, then record the resource
		 * time.
		 */
		Boolean trackTimes = resourceTimeTrackingPolicies.get(resourceId);
		if (trackTimes) {
			DoubleValueContainer doubleValueContainer = personResourceTimes.get(resourceId);
			if (doubleValueContainer == null) {
				double resourceDefinitionTime = resourceDefaultTimes.get(resourceId);
				doubleValueContainer = new DoubleValueContainer(resourceDefinitionTime,
						peopleDataManager::getPersonIndexIterator);
				personResourceTimes.put(resourceId, doubleValueContainer);
			}
			doubleValueContainer.setValue(personId.getValue(), dataManagerContext.getTime());
		}

	}

	/**
	 * Increases the resource for the particular region and resource by the amount.
	 *
	 * @throws RuntimeException
	 *                           <ul>
	 *                           <li>if the resource id is null</li>
	 *                           <li>if the resource id is unknown</li>
	 *                           <li>if the region id null</li>
	 *                           <li>if the region id is unknown</li>
	 *                           <li>if the amount is negative</li>
	 *                           <li>if the amount causes an overflow</li>
	 *                           </ul>
	 * @throws ContractException {@linkplain ResourceError#NEGATIVE_RESOURCE_AMOUNT}
	 *                           if the amount is negative
	 */
	private void incrementRegionResourceLevel(final RegionId regionId, final ResourceId resourceId, final long amount) {
		Map<ResourceId, MutableLong> map = regionResources.get(regionId);
		if (map == null) {
			map = new LinkedHashMap<>();
			regionResources.put(regionId, map);
		}
		MutableLong mutableLong = map.get(resourceId);
		if (mutableLong == null) {
			mutableLong = new MutableLong();
			map.put(resourceId, mutableLong);
		}
		mutableLong.increment(amount);
	}

	private void loadResourcePropertyDefinitions() {
		resourcePropertyDefinitions = resourcesPluginData.getResourcePropertyDefinitions();
	}

	private void loadResourcePropertyValues() {
		resourcePropertyValues = resourcesPluginData.getResourcePropertyValues();
	}

	private void loadResourceDefaultTimes() {
		resourceDefaultTimes = resourcesPluginData.getResourceDefaultTimes();
		for (ResourceId resourceId : resourceDefaultTimes.keySet()) {
			Double resourceDefaultTime = resourceDefaultTimes.get(resourceId);
			if (resourceDefaultTime > dataManagerContext.getTime()) {
				throw new ContractException(ResourceError.RESOURCE_CREATION_TIME_EXCEEDS_SIM_TIME);
			}
		}
	}

	private void loadResourceTimeTrackingPolicies() {
		resourceTimeTrackingPolicies = resourcesPluginData.getResourceTimeTrackingPolicies();
	}

	private void loadPersonResourceLevels() {

		Map<ResourceId, List<Long>> map = resourcesPluginData.getPersonResourceLevels();

		for (ResourceId resourceId : map.keySet()) {

			final IntValueContainer intValueContainer = new IntValueContainer(0L,
					peopleDataManager::getPersonIndexIterator);
			personResourceLevels.put(resourceId, intValueContainer);
			List<Long> personResourceLevels = map.get(resourceId);
			// load the person levels here
			int n = FastMath.max(personResourceLevels.size(), peopleDataManager.getPersonIdLimit());
			for (int i = 0; i < n; i++) {

				Long value = null;
				if (i < personResourceLevels.size()) {
					value = personResourceLevels.get(i);
				}

				if (peopleDataManager.personIndexExists(i)) {
					if (value != null) {
						if (value != 0) {
							intValueContainer.setLongValue(i, value);
						}
					}
				} else {
					if (value != null) {
						throw new ContractException(PersonError.UNKNOWN_PERSON_ID,
								"A non-null resource level for person " + i + " for resource " + resourceId
										+ " was found, but that person does not exist");
					}

				}
			}
		}
	}

	private void loadPersonResourceTimes() {

		Map<ResourceId, List<Double>> map = resourcesPluginData.getPersonResourceTimes();
		for (ResourceId resourceId : map.keySet()) {
			double resourceDefinitionTime = resourceDefaultTimes.get(resourceId);
			final DoubleValueContainer doubleValueContainer = new DoubleValueContainer(resourceDefinitionTime,
					peopleDataManager::getPersonIndexIterator);
			personResourceTimes.put(resourceId, doubleValueContainer);

			List<Double> personResourceTimes = map.get(resourceId);
			int n = FastMath.max(personResourceTimes.size(), peopleDataManager.getPersonIdLimit());
			for (int i = 0; i < n; i++) {

				Double value = null;
				if (i < personResourceTimes.size()) {
					value = personResourceTimes.get(i);
				}

				if (value != null && resourceDefinitionTime > value) {
					throw new ContractException(ResourceError.RESOURCE_CREATION_TIME_EXCEEDS_SIM_TIME);
				}

				if (peopleDataManager.personIndexExists(i)) {
					if (value != null) {
						if (value != 0) {
							doubleValueContainer.setValue(i, value);
						}
					}
				} else {
					if (value != null) {
						throw new ContractException(PersonError.UNKNOWN_PERSON_ID,
								"A non-null resource assignment time for person " + i + " for resource " + resourceId
										+ " was found, but that person does not exist");
					}

				}
			}

		}
	}

	private void loadRegionResourceLevels() {
		Set<RegionId> totalRegionIds = regionsDataManager.getRegionIds();
		Set<RegionId> regionIds = resourcesPluginData.getRegionIds();

		for (final RegionId regionId : regionIds) {
			if (!totalRegionIds.contains(regionId)) {
				throw new ContractException(RegionError.UNKNOWN_REGION_ID,
						regionId + " is an unknown region with initial resources");
			}
		}

		Map<RegionId, Map<ResourceId, Long>> regionResourceLevels = resourcesPluginData.getRegionResourceLevels();

		for (final RegionId regionId : regionResourceLevels.keySet()) {
			Map<ResourceId, Long> externalMap = regionResourceLevels.get(regionId);
			Map<ResourceId, MutableLong> internalMap = new LinkedHashMap<>();
			regionResources.put(regionId, internalMap);
			for (ResourceId resourceId : externalMap.keySet()) {
				Long value = externalMap.get(resourceId);
				internalMap.put(resourceId, new MutableLong(value));
			}
		}
	}

	/**
	 * <ul>
	 * <li>Adds all event labelers defined by the following events
	 * <ul>
	 * <li>{@linkplain PersonResourceUpdateEvent}</li>
	 * <li>{@linkplain RegionResourceUpdateEvent}</li>
	 * <li>{@linkplain ResourcePropertyUpdateEvent}</li>
	 * </ul>
	 * </li>
	 * <li>Sets resource property values from the
	 * {@linkplain ResourcesPluginData}</li>
	 * <li>Sets region resource levels from the
	 * {@linkplain ResourcesPluginData}</li>
	 * <li>Sets person resource levels from the
	 * {@linkplain ResourcesPluginData}</li>
	 * </ul>
	 * Subscribes to the following events:
	 * <ul>
	 * <li>{@linkplain PersonImminentAdditionEvent}
	 * <ul>
	 * <li>Sets the person's initial resource levels in the
	 * {@linkplain ResourcesDataManager} from the ResourceInitialization references
	 * in the auxiliary data of the event.
	 * </ul>
	 * <li>{@linkplain PersonRemovalEvent}
	 * <ul>
	 * <li>Removes the resource assignment data for the person from the
	 * {@linkplain ResourcesDataManager}
	 * </ul>
	 * </ul>
	 * 
	 * @throws ContractException
	 *                           <ul>
	 *                           <li>{@linkplain PersonError#NULL_PERSON_ID} if the
	 *                           person id is null</li>
	 *                           <li>{@linkplain PersonError#UNKNOWN_PERSON_ID} if
	 *                           the person id is unknown</li>
	 *                           <li>{@linkplain ResourceError#NULL_RESOURCE_ID} if
	 *                           the auxiliary data contains a
	 *                           ResourceInitialization that has a null resource
	 *                           id</li>
	 *                           <li>{@linkplain ResourceError#UNKNOWN_RESOURCE_ID}
	 *                           if the auxiliary data contains a
	 *                           ResourceInitialization that has an unknown resource
	 *                           id</li>
	 *                           <li>{@linkplain ResourceError#NEGATIVE_RESOURCE_AMOUNT}
	 *                           if the auxiliary data contains a
	 *                           ResourceInitialization that has a negative resource
	 *                           level</li>
	 *                           </ul>
	 */
	@Override
	public void init(final DataManagerContext dataManagerContext) {
		super.init(dataManagerContext);
		if (dataManagerContext == null) {
			throw new ContractException(NucleusError.NULL_SIMULATION_CONTEXT);
		}
		this.dataManagerContext = dataManagerContext;
		peopleDataManager = dataManagerContext.getDataManager(PeopleDataManager.class);
		regionsDataManager = dataManagerContext.getDataManager(RegionsDataManager.class);

		loadResourceDefaultTimes();
		loadResourceTimeTrackingPolicies();
		loadRegionResourceLevels();
		loadPersonResourceTimes();
		loadPersonResourceLevels();
		loadResourcePropertyDefinitions();
		loadResourcePropertyValues();

		dataManagerContext.subscribe(RegionAdditionEvent.class, this::handleRegionAdditionEvent);
		dataManagerContext.subscribe(PersonImminentAdditionEvent.class, this::handlePersonAdditionEvent);
		dataManagerContext.subscribe(PersonRemovalEvent.class, this::handlePersonRemovalEvent);
		dataManagerContext.subscribe(ResourceIdAdditionMutationEvent.class,
				this::handleResourceIdAdditionMutationEvent);
		dataManagerContext.subscribe(RegionResourceUpdateMutationEvent.class,
				this::handleRegionResourceUpdateMutationEvent);
		dataManagerContext.subscribe(ResourcePropertyDefinitionMutationEvent.class,
				this::handleResourcePropertyDefinitionMutationEvent);
		dataManagerContext.subscribe(PersonResourceUpdateMutationEvent.class,
				this::handlePersonResourceUpdateMutationEvent);
		dataManagerContext.subscribe(RegionResourceRemovalMutationEvent.class,
				this::handleRegionResourceRemovalMutationEvent);
		dataManagerContext.subscribe(ResourcePropertyUpdateMutationEvent.class,
				this::handleResourcePropertyUpdateMutationEvent);
		dataManagerContext.subscribe(InterRegionalResourceTransferMutationEvent.class,
				this::handleInterRegionalResourceTransferMutationEvent);
		dataManagerContext.subscribe(PersonToRegionResourceTransferMutationEvent.class,
				this::handlePersonToRegionResourceTransferMutationEvent);
		dataManagerContext.subscribe(RegionToPersonResourceTransferMutationEvent.class,
				this::handleRegionToPersonResourceTransferMutationEvent);

		if (dataManagerContext.stateRecordingIsScheduled()) {
			dataManagerContext.subscribeToSimulationClose(this::recordSimulationState);
		}
	}

	private void recordSimulationState(DataManagerContext dataManagerContext) {
		ResourcesPluginData.Builder builder = ResourcesPluginData.builder();

		for (ResourceId resourceId : resourceDefaultTimes.keySet()) {
			Boolean trackTimes = resourceTimeTrackingPolicies.get(resourceId);
			Double defaultResourceTime = resourceDefaultTimes.get(resourceId);
			builder.addResource(resourceId, defaultResourceTime, trackTimes);
		}

		List<PersonId> people = peopleDataManager.getPeople();

		for (ResourceId resourceId : personResourceTimes.keySet()) {
			Double defaultResourceTime = resourceDefaultTimes.get(resourceId);
			DoubleValueContainer doubleValueContainer = personResourceTimes.get(resourceId);
			for (PersonId personId : people) {
				double personResourceTime = doubleValueContainer.getValue(personId.getValue());
				if (personResourceTime != defaultResourceTime) {
					builder.setPersonResourceTime(personId, resourceId, personResourceTime);
				}
			}
		}

		for (ResourceId resourceId : personResourceLevels.keySet()) {
			IntValueContainer intValueContainer = personResourceLevels.get(resourceId);
			for (PersonId personId : people) {
				long resourceLevel = intValueContainer.getValueAsLong(personId.getValue());
				if (resourceLevel != 0) {
					builder.setPersonResourceLevel(personId, resourceId, resourceLevel);
				}
			}
		}

		for (ResourceId resourceId : resourcePropertyDefinitions.keySet()) {
			Map<ResourcePropertyId, PropertyDefinition> map = resourcePropertyDefinitions.get(resourceId);
			for (ResourcePropertyId resourcePropertyId : map.keySet()) {
				PropertyDefinition propertyDefinition = map.get(resourcePropertyId);
				builder.defineResourceProperty(resourceId, resourcePropertyId, propertyDefinition);
			}
		}

		for (RegionId regionId : regionResources.keySet()) {
			Map<ResourceId, MutableLong> map = regionResources.get(regionId);
			for (ResourceId resourceId : map.keySet()) {
				MutableLong mutableLong = map.get(resourceId);
				builder.setRegionResourceLevel(regionId, resourceId, mutableLong.getValue());
			}
		}

		for (ResourceId resourceId : resourcePropertyValues.keySet()) {
			Map<ResourcePropertyId, Object> map = resourcePropertyValues.get(resourceId);
			for (ResourcePropertyId resourcePropertyId : map.keySet()) {
				Object propertyValue = map.get(resourcePropertyId);
				builder.setResourcePropertyValue(resourceId, resourcePropertyId, propertyValue);
			}
		}

		dataManagerContext.releaseOutput(builder.build());

	}

	private void handleRegionAdditionEvent(DataManagerContext dataManagerContext,
			RegionAdditionEvent regionAdditionEvent) {
		RegionId regionId = regionAdditionEvent.getRegionId();
		if (regionResources.containsKey(regionId)) {
			throw new ContractException(RegionError.DUPLICATE_REGION_ID);
		}
		List<ResourceInitialization> resourceInitializations = regionAdditionEvent
				.getValues(ResourceInitialization.class);
		for (ResourceInitialization resourceInitialization : resourceInitializations) {
			ResourceId resourceId = resourceInitialization.getResourceId();
			validateResourceId(resourceId);
			Long amount = resourceInitialization.getAmount();
			validateNonnegativeResourceAmount(amount);
			incrementRegionResourceLevel(regionId, resourceId, amount);
		}
	}

	/**
	 * Returns true if and only if the given resource id is known
	 */
	public boolean resourceIdExists(final ResourceId resourceId) {
		return resourceDefaultTimes.containsKey(resourceId);
	}

	/**
	 * Returns true if and only if there is a resource property defined for the
	 * given resource id and resource property id
	 */
	public boolean resourcePropertyIdExists(final ResourceId resourceId, final ResourcePropertyId resourcePropertyId) {
		if ((resourceId == null) || (resourcePropertyId == null)) {
			return false;
		}

		Map<ResourcePropertyId, PropertyDefinition> map = resourcePropertyDefinitions.get(resourceId);

		if (map == null) {
			return false;
		}

		return map.containsKey(resourcePropertyId);
	}

	private void validatePersonExists(final PersonId personId) {
		if (personId == null) {
			throw new ContractException(PersonError.NULL_PERSON_ID);
		}
		if (!peopleDataManager.personExists(personId)) {
			throw new ContractException(PersonError.UNKNOWN_PERSON_ID);
		}
	}

	/*
	 * Preconditions: the resource id must exist
	 */
	private void validatePersonResourceTimesTracked(final ResourceId resourceId) {
		Boolean tracked = resourceTimeTrackingPolicies.get(resourceId);
		if (!tracked) {
			throw new ContractException(ResourceError.RESOURCE_ASSIGNMENT_TIME_NOT_TRACKED);
		}
	}

	private void validateRegionId(final RegionId regionId) {

		if (regionId == null) {
			throw new ContractException(RegionError.NULL_REGION_ID);
		}

		if (!regionsDataManager.regionIdExists(regionId)) {
			throw new ContractException(RegionError.UNKNOWN_REGION_ID, regionId);
		}
	}

	private void validateResourceId(final ResourceId resourceId) {
		if (resourceId == null) {
			throw new ContractException(ResourceError.NULL_RESOURCE_ID);
		}
		if (!resourceDefaultTimes.containsKey(resourceId)) {
			throw new ContractException(ResourceError.UNKNOWN_RESOURCE_ID, resourceId);
		}
	}

	/*
	 * Precondition : the resource id must exist
	 */
	private void validateResourcePropertyId(final ResourceId resourceId, final ResourcePropertyId resourcePropertyId) {
		if (resourcePropertyId == null) {
			throw new ContractException(PropertyError.NULL_PROPERTY_ID);
		}

		Map<ResourcePropertyId, PropertyDefinition> map = resourcePropertyDefinitions.get(resourceId);

		if ((map == null) || !map.containsKey(resourcePropertyId)) {
			throw new ContractException(PropertyError.UNKNOWN_PROPERTY_ID, resourcePropertyId);
		}

	}

	private static record InterRegionalResourceTransferMutationEvent(ResourceId resourceId, RegionId sourceRegionId,
			RegionId destinationRegionId, long amount) implements Event {
	}

	/**
	 * Transfers resources from one region to another. Generates the corresponding
	 * {@linkplain RegionResourceUpdateEvent} events for each region.
	 * 
	 * @throws ContractException
	 *                           <ul>
	 *                           <li>{@linkplain RegionError#NULL_REGION_ID} if the
	 *                           source region is null</li>
	 *                           <li>{@linkplain RegionError#UNKNOWN_REGION_ID} if
	 *                           the source region is unknown</li>
	 *                           <li>{@linkplain RegionError#NULL_REGION_ID} if the
	 *                           destination region is null</li>
	 *                           <li>{@linkplain RegionError#UNKNOWN_REGION_ID} if
	 *                           the destination region is unknown</li>
	 *                           <li>{@linkplain ResourceError#NULL_RESOURCE_ID} if
	 *                           the resource id is null</li>
	 *                           <li>{@linkplain ResourceError#UNKNOWN_RESOURCE_ID}
	 *                           if the resource id is unknown</li>
	 *                           <li>{@linkplain ResourceError#NEGATIVE_RESOURCE_AMOUNT}
	 *                           if the resource amount is negative</li>
	 *                           <li>{@linkplain ResourceError#REFLEXIVE_RESOURCE_TRANSFER}
	 *                           if the source and destination region are equal</li>
	 *                           <li>{@linkplain ResourceError#INSUFFICIENT_RESOURCES_AVAILABLE}
	 *                           if the source region does not have sufficient
	 *                           resources to support the transfer</li>
	 *                           <li>{@linkplain ResourceError#RESOURCE_ARITHMETIC_EXCEPTION}
	 *                           if the transfer will cause a numeric overflow in
	 *                           the destination region</li>
	 *                           </ul>
	 */
	public void transferResourceBetweenRegions(ResourceId resourceId, RegionId sourceRegionId,
			RegionId destinationRegionId, long amount) {
		dataManagerContext.releaseMutationEvent(new InterRegionalResourceTransferMutationEvent(resourceId,
				sourceRegionId, destinationRegionId, amount));
	}

	private void handleInterRegionalResourceTransferMutationEvent(DataManagerContext dataManagerContext,
			InterRegionalResourceTransferMutationEvent interRegionalResourceTransferMutationEvent) {
		ResourceId resourceId = interRegionalResourceTransferMutationEvent.resourceId();
		RegionId sourceRegionId = interRegionalResourceTransferMutationEvent.sourceRegionId();
		RegionId destinationRegionId = interRegionalResourceTransferMutationEvent.destinationRegionId();
		long amount = interRegionalResourceTransferMutationEvent.amount();
		validateRegionId(sourceRegionId);
		validateRegionId(destinationRegionId);
		validateResourceId(resourceId);
		validateNonnegativeResourceAmount(amount);
		validateDifferentRegionsForResourceTransfer(sourceRegionId, destinationRegionId);
		validateRegionHasSufficientResources(resourceId, sourceRegionId, amount);

		final long previousDestinationRegionResourceLevel = _getRegionResourceLevel(destinationRegionId, resourceId);
		validateResourceAdditionValue(previousDestinationRegionResourceLevel, amount);

		if (dataManagerContext.subscribersExist(RegionResourceUpdateEvent.class)) {
			final long previousSourceRegionResourceLevel = _getRegionResourceLevel(sourceRegionId, resourceId);

			decrementRegionResourceLevel(sourceRegionId, resourceId, amount);
			incrementRegionResourceLevel(destinationRegionId, resourceId, amount);

			long currentSourceRegionResourceLevel = _getRegionResourceLevel(sourceRegionId, resourceId);
			long currentDestinationRegionResourceLevel = _getRegionResourceLevel(destinationRegionId, resourceId);

			dataManagerContext.releaseObservationEvent(new RegionResourceUpdateEvent(sourceRegionId, resourceId,
					previousSourceRegionResourceLevel, currentSourceRegionResourceLevel));
			dataManagerContext.releaseObservationEvent(new RegionResourceUpdateEvent(destinationRegionId, resourceId,
					previousDestinationRegionResourceLevel, currentDestinationRegionResourceLevel));
		} else {

			decrementRegionResourceLevel(sourceRegionId, resourceId, amount);
			incrementRegionResourceLevel(destinationRegionId, resourceId, amount);

		}

	}

	private void validateNonnegativeResourceAmount(final long amount) {
		if (amount < 0) {
			throw new ContractException(ResourceError.NEGATIVE_RESOURCE_AMOUNT);
		}
	}

	private void validateDifferentRegionsForResourceTransfer(final RegionId sourceRegionId,
			final RegionId destinationRegionId) {
		if (sourceRegionId.equals(destinationRegionId)) {
			throw new ContractException(ResourceError.REFLEXIVE_RESOURCE_TRANSFER);
		}
	}

	/*
	 * Preconditions : the region and resource must exist
	 */
	private void validateRegionHasSufficientResources(final ResourceId resourceId, final RegionId regionId,
			final long amount) {
		long currentAmount = 0;
		Map<ResourceId, MutableLong> map = regionResources.get(regionId);
		if (map != null) {
			MutableLong mutableLong = map.get(resourceId);
			if (mutableLong != null) {
				currentAmount = mutableLong.getValue();
			}
		}
		if (currentAmount < amount) {
			throw new ContractException(ResourceError.INSUFFICIENT_RESOURCES_AVAILABLE);
		}
	}

	private void validateResourceAdditionValue(final long currentResourceLevel, final long amount) {
		try {
			Math.addExact(currentResourceLevel, amount);
		} catch (final ArithmeticException e) {
			throw new ContractException(ResourceError.RESOURCE_ARITHMETIC_EXCEPTION);
		}
	}

	private static record PersonResourceUpdateMutationEvent(ResourceId resourceId, PersonId personId, long amount)
			implements Event {
	}

	/**
	 * Expends an amount of resource from a person. Generates the corresponding
	 * {@linkplain PersonResourceUpdateEvent} event
	 * 
	 * @throws ContractException
	 *                           <ul>
	 *                           <li>{@linkplain PersonError#NULL_PERSON_ID} if the
	 *                           person id is null</li>
	 *                           <li>{@linkplain PersonError#UNKNOWN_PERSON_ID} if
	 *                           the person does not exist</li>
	 *                           <li>{@linkplain ResourceError#NULL_RESOURCE_ID} if
	 *                           the resource id is null</li>
	 *                           <li>{@linkplain ResourceError#UNKNOWN_RESOURCE_ID}
	 *                           if the resource id is unknown</li>
	 *                           <li>{@linkplain ResourceError#NEGATIVE_RESOURCE_AMOUNT}
	 *                           if the amount is negative</li>
	 *                           <li>{@linkplain ResourceError#INSUFFICIENT_RESOURCES_AVAILABLE}
	 *                           if the person does not have the required amount of
	 *                           the resource</li>
	 *                           </ul>
	 */
	public void removeResourceFromPerson(ResourceId resourceId, PersonId personId, long amount) {
		dataManagerContext.releaseMutationEvent(new PersonResourceUpdateMutationEvent(resourceId, personId, amount));
	}

	private void handlePersonResourceUpdateMutationEvent(DataManagerContext dataManagerContext,
			PersonResourceUpdateMutationEvent personResourceUpdateMutationEvent) {
		ResourceId resourceId = personResourceUpdateMutationEvent.resourceId();
		PersonId personId = personResourceUpdateMutationEvent.personId();
		long amount = personResourceUpdateMutationEvent.amount();
		validatePersonExists(personId);
		validateResourceId(resourceId);
		validateNonnegativeResourceAmount(amount);
		validatePersonHasSufficientResources(resourceId, personId, amount);

		if (dataManagerContext.subscribersExist(PersonResourceUpdateEvent.class)) {
			IntValueContainer intValueContainer = personResourceLevels.get(resourceId);
			if (intValueContainer != null) {
				final long oldLevel = personResourceLevels.get(resourceId).getValueAsLong(personId.getValue());
				decrementPersonResourceLevel(resourceId, personId, amount);
				final long newLevel = personResourceLevels.get(resourceId).getValueAsLong(personId.getValue());
				dataManagerContext.releaseObservationEvent(
						new PersonResourceUpdateEvent(personId, resourceId, oldLevel, newLevel));
			} else {
				// validation above guarantees that the values are all zero
				dataManagerContext.releaseObservationEvent(new PersonResourceUpdateEvent(personId, resourceId, 0, 0));
			}
		} else {
			decrementPersonResourceLevel(resourceId, personId, amount);
		}
	}

	/*
	 * Preconditions : the resource and person must exist
	 */
	private void validatePersonHasSufficientResources(final ResourceId resourceId, final PersonId personId,
			final long amount) {

		long value = 0;
		IntValueContainer intValueContainer = personResourceLevels.get(resourceId);
		if (intValueContainer != null) {
			value = intValueContainer.getValueAsLong(personId.getValue());
		}
		if (value < amount) {
			throw new ContractException(ResourceError.INSUFFICIENT_RESOURCES_AVAILABLE);
		}
	}

	private static record RegionResourceUpdateMutationEvent(ResourceId resourceId, RegionId regionId, long amount)
			implements Event {
	}

	/**
	 * Adds an amount of resource to a region. Generates the corresponding
	 * {@linkplain RegionResourceUpdateEvent} event.
	 * 
	 * @throws ContractException
	 *                           <ul>
	 *                           <li>{@linkplain RegionError#NULL_REGION_ID} if the
	 *                           region id is null</li>
	 *                           <li>{@linkplain RegionError#UNKNOWN_REGION_ID} if
	 *                           the region id is unknown</li>
	 *                           <li>{@linkplain ResourceError#NULL_RESOURCE_ID} if
	 *                           the resource id is null</li>
	 *                           <li>{@linkplain ResourceError#UNKNOWN_RESOURCE_ID}
	 *                           if the resource id is unknown</li>
	 *                           <li>{@linkplain ResourceError#NEGATIVE_RESOURCE_AMOUNT}
	 *                           if the amount is negative</li>
	 *                           <li>{@linkplain ResourceError#RESOURCE_ARITHMETIC_EXCEPTION}
	 *                           if the addition results in an overflow</li>
	 *                           </ul>
	 */
	public void addResourceToRegion(ResourceId resourceId, RegionId regionId, long amount) {
		dataManagerContext.releaseMutationEvent(new RegionResourceUpdateMutationEvent(resourceId, regionId, amount));
	}

	private void handleRegionResourceUpdateMutationEvent(DataManagerContext dataManagerContext,
			RegionResourceUpdateMutationEvent regionResourceUpdateMutationEvent) {
		RegionId regionId = regionResourceUpdateMutationEvent.regionId();
		ResourceId resourceId = regionResourceUpdateMutationEvent.resourceId();
		long amount = regionResourceUpdateMutationEvent.amount();

		validateRegionId(regionId);
		validateResourceId(resourceId);
		validateNonnegativeResourceAmount(amount);

		if (dataManagerContext.subscribersExist(RegionResourceUpdateEvent.class)) {
			final long previousResourceLevel = _getRegionResourceLevel(regionId, resourceId);
			validateResourceAdditionValue(previousResourceLevel, amount);
			incrementRegionResourceLevel(regionId, resourceId, amount);
			long currentResourceLevel = _getRegionResourceLevel(regionId, resourceId);
			dataManagerContext.releaseObservationEvent(
					new RegionResourceUpdateEvent(regionId, resourceId, previousResourceLevel, currentResourceLevel));
		} else {
			final long previousResourceLevel = _getRegionResourceLevel(regionId, resourceId);
			validateResourceAdditionValue(previousResourceLevel, amount);
			incrementRegionResourceLevel(regionId, resourceId, amount);
		}

	}

	private static record RegionResourceRemovalMutationEvent(ResourceId resourceId, RegionId regionId, long amount)
			implements Event {
	}

	/**
	 * Removes an amount of resource from a region.Generates the corresponding
	 * {@linkplain RegionResourceUpdateEvent} event
	 * 
	 * @throws ContractException
	 *                           <ul>
	 *                           <li>{@linkplain RegionError#NULL_REGION_ID} if the
	 *                           region id is null</li>
	 *                           <li>{@linkplain RegionError#UNKNOWN_REGION_ID} if
	 *                           the region id is unknown</li>
	 *                           <li>{@linkplain ResourceError#NULL_RESOURCE_ID} if
	 *                           the resource id is null</li>
	 *                           <li>{@linkplain ResourceError#UNKNOWN_RESOURCE_ID}
	 *                           if the resource id is unknown</li>
	 *                           <li>{@linkplain ResourceError#NEGATIVE_RESOURCE_AMOUNT}
	 *                           if the amount is negative</li>
	 *                           <li>{@linkplain ResourceError#INSUFFICIENT_RESOURCES_AVAILABLE}
	 *                           if the region does not have the required amount of
	 *                           the resource</li>
	 *                           </ul>
	 */
	public void removeResourceFromRegion(ResourceId resourceId, RegionId regionId, long amount) {
		dataManagerContext.releaseMutationEvent(new RegionResourceRemovalMutationEvent(resourceId, regionId, amount));
	}

	private void handleRegionResourceRemovalMutationEvent(DataManagerContext dataManagerContext,
			RegionResourceRemovalMutationEvent regionResourceRemovalMutationEvent) {
		ResourceId resourceId = regionResourceRemovalMutationEvent.resourceId();
		RegionId regionId = regionResourceRemovalMutationEvent.regionId();
		long amount = regionResourceRemovalMutationEvent.amount();
		validateRegionId(regionId);
		validateResourceId(resourceId);
		validateNonnegativeResourceAmount(amount);
		validateRegionHasSufficientResources(resourceId, regionId, amount);
		if (dataManagerContext.subscribersExist(RegionResourceUpdateEvent.class)) {
			final long previousResourceLevel = _getRegionResourceLevel(regionId, resourceId);
			decrementRegionResourceLevel(regionId, resourceId, amount);
			long currentResourceLevel = _getRegionResourceLevel(regionId, resourceId);
			dataManagerContext.releaseObservationEvent(
					new RegionResourceUpdateEvent(regionId, resourceId, previousResourceLevel, currentResourceLevel));
		} else {
			decrementRegionResourceLevel(regionId, resourceId, amount);
		}

	}

	private static record ResourcePropertyUpdateMutationEvent(ResourceId resourceId,
			ResourcePropertyId resourcePropertyId, Object resourcePropertyValue) implements Event {
	}

	/**
	 * Assigns a value to a resource property. Generates the corresponding
	 * {@linkplain ResourcePropertyUpdateEvent} event
	 * 
	 * @throws ContractException
	 *                           <ul>
	 *                           <li>{@linkplain ResourceError#NULL_RESOURCE_ID} if
	 *                           the resource id is null</li>
	 *                           <li>{@linkplain ResourceError#UNKNOWN_RESOURCE_ID}
	 *                           if the resource id is unknown</li>
	 *                           <li>{@linkplain PropertyError#NULL_PROPERTY_ID} if
	 *                           the resource property id is null</li>
	 *                           <li>{@linkplain PropertyError#UNKNOWN_PROPERTY_ID}
	 *                           if the resource property id is unknown</li>
	 *                           <li>{@linkplain PropertyError#NULL_PROPERTY_VALUE}
	 *                           if the resource property value is null</li>
	 *                           <li>{@linkplain PropertyError#INCOMPATIBLE_VALUE}
	 *                           if the resource property value is incompatible with
	 *                           the corresponding property definition</li>
	 *                           <li>{@linkplain PropertyError#IMMUTABLE_VALUE} if
	 *                           the property has been defined as immutable</li>
	 *                           </ul>
	 */
	public void setResourcePropertyValue(ResourceId resourceId, ResourcePropertyId resourcePropertyId,
			Object resourcePropertyValue) {
		dataManagerContext.releaseMutationEvent(
				new ResourcePropertyUpdateMutationEvent(resourceId, resourcePropertyId, resourcePropertyValue));
	}

	private void handleResourcePropertyUpdateMutationEvent(DataManagerContext dataManagerContext,
			ResourcePropertyUpdateMutationEvent resourcePropertyUpdateMutationEvent) {
		ResourceId resourceId = resourcePropertyUpdateMutationEvent.resourceId();
		ResourcePropertyId resourcePropertyId = resourcePropertyUpdateMutationEvent.resourcePropertyId();
		Object resourcePropertyValue = resourcePropertyUpdateMutationEvent.resourcePropertyValue();
		validateResourceId(resourceId);
		validateResourcePropertyId(resourceId, resourcePropertyId);
		validateResourcePropertyValueNotNull(resourcePropertyValue);
		final PropertyDefinition propertyDefinition = resourcePropertyDefinitions.get(resourceId)
				.get(resourcePropertyId);
		validateValueCompatibility(resourcePropertyId, propertyDefinition, resourcePropertyValue);
		validatePropertyMutability(propertyDefinition);

		Map<ResourcePropertyId, Object> map = resourcePropertyValues.get(resourceId);
		if (map == null) {
			map = new LinkedHashMap<>();
			resourcePropertyValues.put(resourceId, map);
		}

		Object oldPropertyValue = map.get(resourcePropertyId);

		if (oldPropertyValue == null) {
			oldPropertyValue = propertyDefinition.getDefaultValue().get();
		}

		map.put(resourcePropertyId, resourcePropertyValue);
		if (dataManagerContext.subscribersExist(ResourcePropertyUpdateEvent.class)) {
			dataManagerContext.releaseObservationEvent(new ResourcePropertyUpdateEvent(resourceId, resourcePropertyId,
					oldPropertyValue, resourcePropertyValue));
		}
	}

	private void validateResourcePropertyValueNotNull(final Object propertyValue) {
		if (propertyValue == null) {
			throw new ContractException(PropertyError.NULL_PROPERTY_VALUE);
		}
	}

	private void validateValueCompatibility(final Object propertyId, final PropertyDefinition propertyDefinition,
			final Object propertyValue) {
		if (!propertyDefinition.getType().isAssignableFrom(propertyValue.getClass())) {
			throw new ContractException(PropertyError.INCOMPATIBLE_VALUE,
					"Property value " + propertyValue + " is not of type " + propertyDefinition.getType().getName()
							+ " and does not match definition of " + propertyId);
		}
	}

	private void validatePropertyMutability(final PropertyDefinition propertyDefinition) {
		if (!propertyDefinition.propertyValuesAreMutable()) {
			throw new ContractException(PropertyError.IMMUTABLE_VALUE);
		}
	}

	private static record PersonToRegionResourceTransferMutationEvent(ResourceId resourceId, PersonId personId,
			long amount) implements Event {
	}

	/**
	 * Transfers an amount of resource from a person to the person's current region.
	 * Generates the corresponding {@linkplain RegionResourceUpdateEvent} and
	 * {@linkplain PersonResourceUpdateEvent} events
	 * 
	 * @throws ContractException
	 *                           <ul>
	 *                           <li>{@linkplain PersonError#NULL_PERSON_ID} if the
	 *                           person id is null</li>
	 *                           <li>{@linkplain PersonError#UNKNOWN_PERSON_ID} if
	 *                           the person does not exist</li>
	 *                           <li>{@linkplain ResourceError#NULL_RESOURCE_ID} if
	 *                           the resource id is null</li>
	 *                           <li>{@linkplain ResourceError#UNKNOWN_RESOURCE_ID}
	 *                           if the resource id is unknown</li>
	 *                           <li>{@linkplain ResourceError#NEGATIVE_RESOURCE_AMOUNT}
	 *                           if the amount is negative</li>
	 *                           <li>{@linkplain ResourceError#INSUFFICIENT_RESOURCES_AVAILABLE}
	 *                           if the person does not have the required amount of
	 *                           the resource</li>
	 *                           <li>{@linkplain ResourceError#RESOURCE_ARITHMETIC_EXCEPTION}
	 *                           if the transfer results in an overflow of the
	 *                           region's resource level</li>
	 *                           </ul>
	 */
	public void transferResourceFromPersonToRegion(ResourceId resourceId, PersonId personId, long amount) {
		dataManagerContext
				.releaseMutationEvent(new PersonToRegionResourceTransferMutationEvent(resourceId, personId, amount));
	}

	private void handlePersonToRegionResourceTransferMutationEvent(DataManagerContext dataManagerContext,
			PersonToRegionResourceTransferMutationEvent personToRegionResourceTransferMutationEvent) {
		ResourceId resourceId = personToRegionResourceTransferMutationEvent.resourceId();
		PersonId personId = personToRegionResourceTransferMutationEvent.personId();
		long amount = personToRegionResourceTransferMutationEvent.amount();
		validatePersonExists(personId);
		validateResourceId(resourceId);
		validateNonnegativeResourceAmount(amount);
		validatePersonHasSufficientResources(resourceId, personId, amount);
		final RegionId regionId = regionsDataManager.getPersonRegion(personId);
		final long previousRegionResourceLevel = _getRegionResourceLevel(regionId, resourceId);
		validateResourceAdditionValue(previousRegionResourceLevel, amount);
		final long oldLevel = _getPersonResourceLevel(resourceId, personId);
		decrementPersonResourceLevel(resourceId, personId, amount);
		final long newLevel = _getPersonResourceLevel(resourceId, personId);
		incrementRegionResourceLevel(regionId, resourceId, amount);
		long currentRegionResourceLevel = _getRegionResourceLevel(regionId, resourceId);
		if (dataManagerContext.subscribersExist(PersonResourceUpdateEvent.class)) {
			dataManagerContext
					.releaseObservationEvent(new PersonResourceUpdateEvent(personId, resourceId, oldLevel, newLevel));
		}
		if (dataManagerContext.subscribersExist(RegionResourceUpdateEvent.class)) {
			dataManagerContext.releaseObservationEvent(new RegionResourceUpdateEvent(regionId, resourceId,
					previousRegionResourceLevel, currentRegionResourceLevel));
		}

	}

	private static record RegionToPersonResourceTransferMutationEvent(ResourceId resourceId, PersonId personId,
			long amount) implements Event {
	}

	/**
	 * Transfers an amount of resource to a person from the person's current region.
	 * Generates the corresponding {@linkplain RegionResourceUpdateEvent} and
	 * {@linkplain PersonResourceUpdateEvent} events
	 * 
	 * @throws ContractException
	 *                           <ul>
	 *                           <li>{@linkplain PersonError#NULL_PERSON_ID} if the
	 *                           person id is null</li>
	 *                           <li>{@linkplain PersonError#UNKNOWN_PERSON_ID} if
	 *                           the person does not exist</li>
	 *                           <li>{@linkplain ResourceError#NULL_RESOURCE_ID} if
	 *                           the resource id is null</li>
	 *                           <li>{@linkplain ResourceError#UNKNOWN_RESOURCE_ID}
	 *                           if the resource id is unknown</li>
	 *                           <li>{@linkplain ResourceError#NEGATIVE_RESOURCE_AMOUNT}
	 *                           if the amount is negative</li>
	 *                           <li>{@linkplain ResourceError#INSUFFICIENT_RESOURCES_AVAILABLE}
	 *                           if the region does not have the required amount of
	 *                           the resource</li>
	 *                           <li>{@linkplain ResourceError#RESOURCE_ARITHMETIC_EXCEPTION}
	 *                           if the transfer results in an overflow of the
	 *                           person's resource level</li>
	 *                           </ul>
	 */
	public void transferResourceToPersonFromRegion(ResourceId resourceId, PersonId personId, long amount) {
		dataManagerContext
				.releaseMutationEvent(new RegionToPersonResourceTransferMutationEvent(resourceId, personId, amount));
	}

	private void handleRegionToPersonResourceTransferMutationEvent(DataManagerContext dataManagerContext,
			RegionToPersonResourceTransferMutationEvent regionToPersonResourceTransferMutationEvent) {
		ResourceId resourceId = regionToPersonResourceTransferMutationEvent.resourceId();
		PersonId personId = regionToPersonResourceTransferMutationEvent.personId();
		long amount = regionToPersonResourceTransferMutationEvent.amount();
		validatePersonExists(personId);
		validateResourceId(resourceId);
		validateNonnegativeResourceAmount(amount);
		final RegionId regionId = regionsDataManager.getPersonRegion(personId);
		validateRegionHasSufficientResources(resourceId, regionId, amount);
		final long personResourceLevel = _getPersonResourceLevel(resourceId, personId);
		validateResourceAdditionValue(personResourceLevel, amount);

		final long previousRegionResourceLevel = _getRegionResourceLevel(regionId, resourceId);

		decrementRegionResourceLevel(regionId, resourceId, amount);
		incrementPersonResourceLevel(resourceId, personId, amount);
		final long newLevel = _getPersonResourceLevel(resourceId, personId);
		long currentRegionResourceLevel = _getRegionResourceLevel(regionId, resourceId);

		if (dataManagerContext.subscribersExist(RegionResourceUpdateEvent.class)) {
			dataManagerContext.releaseObservationEvent(new RegionResourceUpdateEvent(regionId, resourceId,
					previousRegionResourceLevel, currentRegionResourceLevel));
		}
		if (dataManagerContext.subscribersExist(PersonResourceUpdateEvent.class)) {
			dataManagerContext.releaseObservationEvent(
					new PersonResourceUpdateEvent(personId, resourceId, personResourceLevel, newLevel));
		}
	}

	private void handlePersonAdditionEvent(final DataManagerContext dataManagerContext,
			final PersonImminentAdditionEvent personImminentAdditionEvent) {
		PersonId personId = personImminentAdditionEvent.personId();
		PersonConstructionData personConstructionData = personImminentAdditionEvent.personConstructionData();
		validatePersonExists(personId);
		List<ResourceInitialization> resourceAssignments = personConstructionData
				.getValues(ResourceInitialization.class);
		for (final ResourceInitialization resourceAssignment : resourceAssignments) {
			ResourceId resourceId = resourceAssignment.getResourceId();
			Long amount = resourceAssignment.getAmount();
			validateResourceId(resourceId);
			validateNonnegativeResourceAmount(amount);
		}

		for (final ResourceInitialization resourceAssignment : resourceAssignments) {
			incrementPersonResourceLevel(resourceAssignment.getResourceId(), personId, resourceAssignment.getAmount());
		}
	}

	private void handlePersonRemovalEvent(final DataManagerContext dataManagerContext,
			final PersonRemovalEvent personRemovalEvent) {

		PersonId personId = personRemovalEvent.personId();
		for (final IntValueContainer intValueContainer : personResourceLevels.values()) {
			intValueContainer.setLongValue(personId.getValue(), 0);
		}

	}

	private static enum PersonResourceUpdateEventFunctionId {
		RESOURCE, REGION, PERSON
	}

	private IdentifiableFunctionMap<PersonResourceUpdateEvent> personResourceUpdateFunctionMap = //
			IdentifiableFunctionMap.builder(PersonResourceUpdateEvent.class)//
					.put(PersonResourceUpdateEventFunctionId.RESOURCE, e -> e.resourceId())//
					.put(PersonResourceUpdateEventFunctionId.REGION,
							e -> regionsDataManager.getPersonRegion(e.personId()))//
					.put(PersonResourceUpdateEventFunctionId.PERSON, e -> e.personId())//
					.build();//

	/**
	 * Returns an event filter used to subscribe to
	 * {@link PersonResourceUpdateEvent} events. Matches on the resource id.
	 *
	 * @throws ContractException
	 *                           <ul>
	 *                           <li>{@linkplain ResourceError#NULL_RESOURCE_ID} if
	 *                           the resource id is null</li>
	 *                           <li>{@linkplain ResourceError#UNKNOWN_RESOURCE_ID}
	 *                           if the resource id is not known</li>
	 *                           </ul>
	 */
	public EventFilter<PersonResourceUpdateEvent> getEventFilterForPersonResourceUpdateEvent(ResourceId resourceId) {
		validateResourceId(resourceId);
		return EventFilter.builder(PersonResourceUpdateEvent.class)//
				.addFunctionValuePair(personResourceUpdateFunctionMap.get(PersonResourceUpdateEventFunctionId.RESOURCE),
						resourceId)//
				.build();
	}

	/**
	 * Returns an event filter used to subscribe to
	 * {@link PersonResourceUpdateEvent} events. Matches on the resource id and
	 * person id.
	 *
	 * @throws ContractException
	 *                           <ul>
	 *                           <li>{@linkplain ResourceError#NULL_RESOURCE_ID} if
	 *                           the resource id is null</li>
	 *                           <li>{@linkplain ResourceError#UNKNOWN_RESOURCE_ID}
	 *                           if the resource id is not known</li>
	 *                           <li>{@linkplain PersonError#NULL_PERSON_ID} if the
	 *                           person id is null</li>
	 *                           <li>{@linkplain PersonError#UNKNOWN_PERSON_ID} if
	 *                           the person id is not known</li>
	 *                           </ul>
	 */
	public EventFilter<PersonResourceUpdateEvent> getEventFilterForPersonResourceUpdateEvent(ResourceId resourceId,
			PersonId personId) {
		validateResourceId(resourceId);
		validatePersonExists(personId);
		return EventFilter.builder(PersonResourceUpdateEvent.class)//
				.addFunctionValuePair(personResourceUpdateFunctionMap.get(PersonResourceUpdateEventFunctionId.RESOURCE),
						resourceId)//
				.addFunctionValuePair(personResourceUpdateFunctionMap.get(PersonResourceUpdateEventFunctionId.PERSON),
						personId)//
				.build();
	}

	/**
	 * Returns an event filter used to subscribe to
	 * {@link PersonResourceUpdateEvent} events. Matches on the resource id and
	 * person id.
	 *
	 * @throws ContractException
	 *                           <ul>
	 *                           <li>{@linkplain ResourceError#NULL_RESOURCE_ID} if
	 *                           the resource id is null</li>
	 *                           <li>{@linkplain ResourceError#UNKNOWN_RESOURCE_ID}
	 *                           if the resource id is not known</li>
	 *                           <li>{@linkplain RegionError#NULL_REGION_ID} if the
	 *                           region id is null</li>
	 *                           <li>{@linkplain RegionError#UNKNOWN_REGION_ID} if
	 *                           the region id is not known</li>
	 *                           </ul>
	 */
	public EventFilter<PersonResourceUpdateEvent> getEventFilterForPersonResourceUpdateEvent(ResourceId resourceId,
			RegionId regionId) {
		validateResourceId(resourceId);
		validateRegionId(regionId);
		return EventFilter.builder(PersonResourceUpdateEvent.class)//
				.addFunctionValuePair(personResourceUpdateFunctionMap.get(PersonResourceUpdateEventFunctionId.RESOURCE),
						resourceId)//
				.addFunctionValuePair(personResourceUpdateFunctionMap.get(PersonResourceUpdateEventFunctionId.REGION),
						regionId)//
				.build();
	}

	/**
	 * Returns an event filter used to subscribe to
	 * {@link PersonResourceUpdateEvent} events. Matches on all such events.
	 */
	public EventFilter<PersonResourceUpdateEvent> getEventFilterForPersonResourceUpdateEvent() {
		return EventFilter.builder(PersonResourceUpdateEvent.class)//
				.build();
	}

	private static enum RegionResourceUpdateEventFunctionId {
		RESOURCE, REGION
	}

	private IdentifiableFunctionMap<RegionResourceUpdateEvent> regionResourceUpdateMap = //
			IdentifiableFunctionMap.builder(RegionResourceUpdateEvent.class)//
					.put(RegionResourceUpdateEventFunctionId.RESOURCE, e -> e.resourceId())//
					.put(RegionResourceUpdateEventFunctionId.REGION, e -> e.regionId())//
					.build();//

	/**
	 * Returns an event filter used to subscribe to
	 * {@link RegionResourceUpdateEvent} events. Matches on the resource id.
	 *
	 * @throws ContractException
	 *                           <ul>
	 *                           <li>{@linkplain ResourceError#NULL_RESOURCE_ID} if
	 *                           the resource id is null</li>
	 *                           <li>{@linkplain ResourceError#UNKNOWN_RESOURCE_ID}
	 *                           if the resource id is not known</li>
	 *                           </ul>
	 */
	public EventFilter<RegionResourceUpdateEvent> getEventFilterForRegionResourceUpdateEvent(ResourceId resourceId) {
		validateResourceId(resourceId);
		return EventFilter.builder(RegionResourceUpdateEvent.class)//
				.addFunctionValuePair(regionResourceUpdateMap.get(RegionResourceUpdateEventFunctionId.RESOURCE),
						resourceId)//
				.build();
	}

	/**
	 * Returns an event filter used to subscribe to
	 * {@link RegionResourceUpdateEvent} events. Matches on the resource id and
	 * region id.
	 *
	 * @throws ContractException
	 *                           <ul>
	 *                           <li>{@linkplain ResourceError#NULL_RESOURCE_ID} if
	 *                           the resource id is null</li>
	 *                           <li>{@linkplain ResourceError#UNKNOWN_RESOURCE_ID}
	 *                           if the resource id is not known</li>
	 *                           <li>{@linkplain RegionError#NULL_REGION_ID} if the
	 *                           region id is null</li>
	 *                           <li>{@linkplain RegionError#UNKNOWN_REGION_ID} if
	 *                           the region id is not known</li>
	 *                           </ul>
	 */
	public EventFilter<RegionResourceUpdateEvent> getEventFilterForRegionResourceUpdateEvent(ResourceId resourceId,
			RegionId regionId) {
		validateResourceId(resourceId);
		validateRegionId(regionId);
		return EventFilter.builder(RegionResourceUpdateEvent.class)//
				.addFunctionValuePair(regionResourceUpdateMap.get(RegionResourceUpdateEventFunctionId.RESOURCE),
						resourceId)//
				.addFunctionValuePair(regionResourceUpdateMap.get(RegionResourceUpdateEventFunctionId.REGION), regionId)//
				.build();
	}

	/**
	 * Returns an event filter used to subscribe to
	 * {@link RegionResourceUpdateEvent} events. Matches on all such events.
	 */
	public EventFilter<RegionResourceUpdateEvent> getEventFilterForRegionResourceUpdateEvent() {
		return EventFilter.builder(RegionResourceUpdateEvent.class)//
				.build();
	}

	private static enum ResourcePropertyUpdateEventFunctionId {
		RESOURCE, PROPERTY
	}

	private IdentifiableFunctionMap<ResourcePropertyUpdateEvent> resourcePropertyUpdateMap = //
			IdentifiableFunctionMap.builder(ResourcePropertyUpdateEvent.class)//
					.put(ResourcePropertyUpdateEventFunctionId.RESOURCE, e -> e.resourceId())//
					.put(ResourcePropertyUpdateEventFunctionId.PROPERTY, e -> e.resourcePropertyId())//
					.build();//

	/**
	 * Returns an event filter used to subscribe to
	 * {@link ResourcePropertyUpdateEvent} events. Matches on the resource id and
	 * resource property id.
	 *
	 * @throws ContractException
	 *                           <ul>
	 *                           <li>{@linkplain ResourceError#NULL_RESOURCE_ID} if
	 *                           the resource id is null</li>
	 *                           <li>{@linkplain ResourceError#UNKNOWN_RESOURCE_ID}
	 *                           if the resource id is not known</li>
	 *                           <li>{@linkplain PropertyError#NULL_PROPERTY_ID} if
	 *                           the resource property id is null</li>
	 *                           <li>{@linkplain PropertyError#UNKNOWN_PROPERTY_ID}
	 *                           if the resource property id is not known</li>
	 *                           </ul>
	 */
	public EventFilter<ResourcePropertyUpdateEvent> getEventFilterForResourcePropertyUpdateEvent(ResourceId resourceId,
			ResourcePropertyId resourcePropertyId) {
		validateResourceId(resourceId);
		validateResourcePropertyId(resourceId, resourcePropertyId);
		return EventFilter.builder(ResourcePropertyUpdateEvent.class)//
				.addFunctionValuePair(resourcePropertyUpdateMap.get(ResourcePropertyUpdateEventFunctionId.RESOURCE),
						resourceId)//
				.addFunctionValuePair(resourcePropertyUpdateMap.get(ResourcePropertyUpdateEventFunctionId.PROPERTY),
						resourcePropertyId)//
				.build();
	}

	/**
	 * Returns an event filter used to subscribe to
	 * {@link ResourcePropertyUpdateEvent} events. Matches all such events.
	 */
	public EventFilter<ResourcePropertyUpdateEvent> getEventFilterForResourcePropertyUpdateEvent() {
		return EventFilter.builder(ResourcePropertyUpdateEvent.class)//
				.build();
	}

	/**
	 * Returns an event filter used to subscribe to {@link ResourceIdAdditionEvent}
	 * events. Matches all such events.
	 */
	public EventFilter<ResourceIdAdditionEvent> getEventFilterForResourceIdAdditionEvent() {
		return EventFilter.builder(ResourceIdAdditionEvent.class)//
				.build();
	}

	/**
	 * Returns an event filter used to subscribe to
	 * {@link ResourcePropertyDefinitionEvent} events. Matches all such events.
	 */
	public EventFilter<ResourcePropertyDefinitionEvent> getEventFilterForResourcePropertyDefinitionEvent() {
		return EventFilter.builder(ResourcePropertyDefinitionEvent.class)//
				.build();
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("ResourcesDataManager [");

		builder.append("resourceDefaultTimes=");
		builder.append(resourceDefaultTimes);

		builder.append(", resourcePropertyValues=");
		builder.append(resourcePropertyValues);

		builder.append(", personResourceLevels=");
		builder.append(personResourceLevels);

		builder.append(", resourcePropertyDefinitions=");
		builder.append(resourcePropertyDefinitions);

		builder.append(", resourceTimeTrackingPolicies=");
		builder.append(resourceTimeTrackingPolicies);

		builder.append(", personResourceTimes=");
		builder.append(personResourceTimes);

		builder.append(", regionResources=");
		builder.append(regionResources);

		builder.append("]");
		return builder.toString();
	}

}