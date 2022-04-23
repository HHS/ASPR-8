package plugins.resources.datamanagers;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import nucleus.DataManager;
import nucleus.DataManagerContext;
import nucleus.NucleusError;
import nucleus.SimulationContext;
import nucleus.util.ContractException;
import plugins.people.datamanagers.PeopleDataManager;
import plugins.people.events.BulkPersonAdditionEvent;
import plugins.people.events.PersonAdditionEvent;
import plugins.people.events.PersonImminentRemovalEvent;
import plugins.people.support.BulkPersonConstructionData;
import plugins.people.support.PersonConstructionData;
import plugins.people.support.PersonError;
import plugins.people.support.PersonId;
import plugins.regions.datamanagers.RegionsDataManager;
import plugins.regions.support.RegionError;
import plugins.regions.support.RegionId;
import plugins.resources.ResourcesPlugin;
import plugins.resources.ResourcesPluginData;
import plugins.resources.events.PersonResourceUpdateEvent;
import plugins.resources.events.RegionResourceUpdateEvent;
import plugins.resources.events.ResourcePropertyUpdateEvent;
import plugins.resources.support.ResourceError;
import plugins.resources.support.ResourceId;
import plugins.resources.support.ResourceInitialization;
import plugins.resources.support.ResourcePropertyId;
import plugins.util.properties.PropertyDefinition;
import plugins.util.properties.PropertyError;
import plugins.util.properties.PropertyValueRecord;
import plugins.util.properties.TimeTrackingPolicy;
import plugins.util.properties.arraycontainers.DoubleValueContainer;
import plugins.util.properties.arraycontainers.IntValueContainer;

/**
 * Mutable data manager that backs the {@linkplain ResourcesDataManager}. This
 * data manager is for internal use by the {@link ResourcesPlugin} and should
 * not be published.
 *
 * All resources are defined during construction and cannot be changed. Resource
 * property values are mutable and specific to the type of resource. Limited
 * validation of inputs are performed and mutation methods have invocation
 * ordering requirements.
 *
 * @author Shawn Hatch
 *
 */

public final class ResourcesDataManager extends DataManager {
	/*
	 * Static utility class for tracking region resources.
	 */
	private static class RegionResourceRecord {
		private final SimulationContext simulationContext;

		private long amount;

		private double assignmentTime;

		public RegionResourceRecord(final SimulationContext simulationContext) {
			this.simulationContext = simulationContext;
		}

		public void decrementAmount(final long amount) {
			if (amount < 0) {
				throw new ContractException(ResourceError.NEGATIVE_RESOURCE_AMOUNT);
			}

			if (this.amount < amount) {
				throw new ContractException(ResourceError.INSUFFICIENT_RESOURCES_AVAILABLE);
			}
			this.amount = Math.subtractExact(this.amount, amount);
			assignmentTime = simulationContext.getTime();
		}

		public long getAmount() {
			return amount;
		}

		public double getAssignmentTime() {
			return assignmentTime;
		}

		public void incrementAmount(final long amount) {
			if (amount < 0) {
				throw new ContractException(ResourceError.NEGATIVE_RESOURCE_AMOUNT);
			}
			this.amount = Math.addExact(this.amount, amount);
			assignmentTime = simulationContext.getTime();
		}

	}

	private PeopleDataManager peopleDataManager;
	private RegionsDataManager regionsDataManager;

	// resources
	private final Map<ResourceId, Map<ResourcePropertyId, PropertyValueRecord>> resourcePropertyMap = new LinkedHashMap<>();

	/*
	 * Stores resource amounts per person keyed by the resourceId
	 */
	private final Map<ResourceId, IntValueContainer> personResourceValues = new LinkedHashMap<>();

	private final Map<ResourceId, Map<ResourcePropertyId, PropertyDefinition>> resourcePropertyDefinitions = new LinkedHashMap<>();

	/*
	 * Stores resource assignment times per person keyed by the resourceId. Key
	 * existence subject to time recording policies specified by the scenario.
	 */
	private final Map<ResourceId, DoubleValueContainer> personResourceTimes = new LinkedHashMap<>();

	private final Map<ResourceId, TimeTrackingPolicy> resourceTimeTrackingPolicies = new LinkedHashMap<>();

	private final Map<RegionId, Map<ResourceId, RegionResourceRecord>> regionResources = new LinkedHashMap<>();

	private final ResourcesPluginData resourcesPluginData;

	private DataManagerContext dataManagerContext;

	/**
	 * Constructs the PersonResourceManager from the context
	 *
	 * @throws ContractException
	 *             <li>{@linkplain NucleusError#NULL_RESOURCE_PLUGIN_DATA} if the plugin data is null</li>
	 */
	public ResourcesDataManager(final ResourcesPluginData resourcesPluginData) {
		if(resourcesPluginData == null) {
			throw new  ContractException(ResourceError.NULL_RESOURCE_PLUGIN_DATA);
		}
		this.resourcesPluginData = resourcesPluginData;
	}

	/**
	 * Reduces the resource for the particular person and resource by the
	 * amount.
	 *
	 * @throws RuntimeException
	 *             <li>if the resource id is null</li>
	 *             <li>if the resource id is unknown</li>
	 *             <li>if the person id null</li>
	 *             <li>if the person id has a negative value</li>
	 *             <li>if the amount causes an overflow</li>
	 */
	private void decrementPersonResourceLevel(final ResourceId resourceId, final PersonId personId, final long resourceAmount) {
		personResourceValues.get(resourceId).decrementLongValue(personId.getValue(), resourceAmount);
		/*
		 * if the resource assignment times are being tracked, then record the
		 * resource time.
		 */
		final DoubleValueContainer doubleValueContainer = personResourceTimes.get(resourceId);
		if (doubleValueContainer != null) {
			doubleValueContainer.setValue(personId.getValue(), dataManagerContext.getTime());
		}
	}

	/**
	 * Reduces the resource for the particular region and resource by the
	 * amount.
	 *
	 * @throws RuntimeException
	 *             <li>if the resource id is null</li>
	 *             <li>if the resource id is unknown</li>
	 *             <li>if the region id null</li>
	 *             <li>if the region id is unknown</li>
	 * 
	 *
	 * @throws ContractException
	 *             <li>{@linkplain ResourceError#NEGATIVE_RESOURCE_AMOUNT} if
	 *             the amount is negative</li>
	 *             <li>{@linkplain ResourceError#INSUFFICIENT_RESOURCES_AVAILABLE}
	 *             if the amount exceeds the current balance</li>
	 */

	private void decrementRegionResourceLevel(final RegionId regionId, final ResourceId resourceId, final long amount) {
		final RegionResourceRecord regionResourceRecord = regionResources.get(regionId).get(resourceId);
		regionResourceRecord.decrementAmount(amount);
	}

	/**
	 * Expands the capacity of data structures to hold people by the given
	 * count. Used to more efficiently prepare for bulk population additions.
	 * 
	 * @throws ContractException
	 *             <li>{@linkplain PersonError#NEGATIVE_GROWTH_PROJECTION} if
	 *             the count is negative</li>
	 */
	public void expandCapacity(final int count) {
		if (count < 0) {
			throw new ContractException(PersonError.NEGATIVE_GROWTH_PROJECTION);
		}
		if (count > 0) {
			for (final ResourceId resourceId : personResourceValues.keySet()) {
				final IntValueContainer intValueContainer = personResourceValues.get(resourceId);
				intValueContainer.setCapacity(intValueContainer.getCapacity() + count);
				final TimeTrackingPolicy resourceTimeTrackingPolicy = resourceTimeTrackingPolicies.get(resourceId);
				if (resourceTimeTrackingPolicy == TimeTrackingPolicy.TRACK_TIME) {
					final DoubleValueContainer doubleValueContainer = personResourceTimes.get(resourceId);
					doubleValueContainer.setCapacity(doubleValueContainer.getCapacity() + count);
				}
			}
		}
	}

	/**
	 * Returns the set of people who do not have any of the given resource as a
	 * list.
	 *
	 * @throws ContractException
	 *             <li>{@linkplain ResourceError#NULL_RESOURCE_ID} if the
	 *             resource id is null</li>
	 *             <li>{@linkplain ResourceError#UNKNOWN_RESOURCE_ID} if the
	 *             resource id is unknown</li>
	 */
	public List<PersonId> getPeopleWithoutResource(final ResourceId resourceId) {
		validateResourceId(resourceId);
		/*
		 * First, we loop through all possible person id values and determine
		 * the exact size of the returned list.
		 */
		int count = 0;
		final IntValueContainer intValueContainer = personResourceValues.get(resourceId);
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
	 *             <li>{@linkplain ResourceError#NULL_RESOURCE_ID} if the
	 *             resource id is null</li>
	 *             <li>{@linkplain ResourceError#UNKNOWN_RESOURCE_ID} if the
	 *             resource id is unknown</li>
	 *
	 */
	public List<PersonId> getPeopleWithResource(final ResourceId resourceId) {
		validateResourceId(resourceId);
		/*
		 * First, we loop through all possible person id values and determine
		 * the exact size of the returned list.
		 */
		int count = 0;
		final IntValueContainer intValueContainer = personResourceValues.get(resourceId);
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
	 *             <li>{@linkplain RegionError#NULL_REGION_ID} if the region id
	 *             is null</li>
	 *             <li>{@linkplain RegionError#UNKNOWN_REGION_ID} if the region
	 *             id is unknown</li>
	 *             <li>{@linkplain ResourceError#NULL_RESOURCE_ID} if the
	 *             resource id is null</li>
	 *             <li>{@linkplain ResourceError#UNKNOWN_RESOURCE_ID} if the
	 *             resource id is unknown</li>
	 */
	public long getPersonResourceLevel(final ResourceId resourceId, final PersonId personId) {
		validatePersonExists(personId);
		validateResourceId(resourceId);
		return personResourceValues.get(resourceId).getValueAsLong(personId.getValue());
	}

	/**
	 * Returns the time when the resource level was last assigned for the given
	 * person and resource
	 *
	 * @throws ContractException
	 *             <li>{@linkplain PersonError#NULL_PERSON_ID} if the person id
	 *             is null</li>
	 *             <li>{@linkplain PersonError#UNKNOWN_PERSON_ID} if the person
	 *             id is unknown</li>
	 *             <li>{@linkplain ResourceError#NULL_RESOURCE_ID} if the
	 *             resource id is null</li>
	 *             <li>{@linkplain ResourceError#UNKNOWN_RESOURCE_ID} if the
	 *             resource id is unknown</li>
	 *             <li>{@linkplain ResourceError#RESOURCE_ASSIGNMENT_TIME_NOT_TRACKED}
	 *             if assignment times are not tracked for the resource when
	 *             applied to people</li>
	 */
	public double getPersonResourceTime(final ResourceId resourceId, final PersonId personId) {
		validatePersonExists(personId);
		validateResourceId(resourceId);
		validatePersonResourceTimesTracked(resourceId);
		final DoubleValueContainer doubleValueContainer = personResourceTimes.get(resourceId);
		return doubleValueContainer.getValue(personId.getValue());

	}

	/**
	 * Returns the time tracking policy for the given resource
	 *
	 * @throws ContractException
	 *             <li>{@linkplain ResourceError#NULL_RESOURCE_ID} if the
	 *             resource id is null</li>
	 *             <li>{@linkplain ResourceError#UNKNOWN_RESOURCE_ID} if the
	 *             resource id is unknown</li>
	 */
	public TimeTrackingPolicy getPersonResourceTimeTrackingPolicy(final ResourceId resourceId) {
		validateResourceId(resourceId);
		return resourceTimeTrackingPolicies.get(resourceId);
	}

	/**
	 * Returns the current resource level for the given resource id and region
	 * id
	 *
	 * @throws ContractException
	 *             <li>{@linkplain RegionError#NULL_REGION_ID} if the region id
	 *             is null</li>
	 *             <li>{@linkplain RegionError#UNKNOWN_REGION_ID} if the region
	 *             id is unknown</li>
	 *             <li>{@linkplain ResourceError#NULL_RESOURCE_ID} if the
	 *             resource id is null</li>
	 *             <li>{@linkplain ResourceError#UNKNOWN_RESOURCE_ID} if the
	 *             resource id is unknown</li>
	 */
	public long getRegionResourceLevel(final RegionId regionId, final ResourceId resourceId) {
		validateRegionId(regionId);
		validateResourceId(resourceId);
		final RegionResourceRecord regionResourceRecord = regionResources.get(regionId).get(resourceId);
		return regionResourceRecord.getAmount();
	}

	/**
	 * Returns the last assignment time for the region resource level
	 *
	 * @throws ContractException
	 *             <li>{@linkplain RegionError#NULL_REGION_ID} if the region id
	 *             is null</li>
	 *             <li>{@linkplain RegionError#UNKNOWN_REGION_ID} if the region
	 *             id is unknown</li>
	 *             <li>{@linkplain ResourceError#NULL_RESOURCE_ID} if the
	 *             resource id is null</li>
	 *             <li>{@linkplain ResourceError#UNKNOWN_RESOURCE_ID} if the
	 *             resource id is unknown</li>
	 */
	public double getRegionResourceTime(final RegionId regionId, final ResourceId resourceId) {
		validateRegionId(regionId);
		validateResourceId(resourceId);

		final RegionResourceRecord regionResourceRecord = regionResources.get(regionId).get(resourceId);
		return regionResourceRecord.getAssignmentTime();

	}

	/**
	 * Returns the resource ids
	 */
	@SuppressWarnings("unchecked")
	public <T extends ResourceId> Set<T> getResourceIds() {
		final Set<T> result = new LinkedHashSet<>(personResourceValues.size());
		for (final ResourceId resourceId : personResourceValues.keySet()) {
			result.add((T) resourceId);
		}
		personResourceValues.keySet();
		return result;
	}

	/**
	 * Returns the property definition for the given resource id and resource
	 * property id
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
	 */
	public PropertyDefinition getResourcePropertyDefinition(final ResourceId resourceId, final ResourcePropertyId resourcePropertyId) {
		validateResourceId(resourceId);
		validateResourcePropertyId(resourceId, resourcePropertyId);
		return resourcePropertyDefinitions.get(resourceId).get(resourcePropertyId);
	}

	/**
	 * Returns the resource property id values for the given resource id
	 *
	 * @throws ContractException
	 *             <li>{@linkplain ResourceError#NULL_RESOURCE_ID} if the
	 *             resource id is null</li>
	 *             <li>{@linkplain ResourceError#UNKNOWN_RESOURCE_ID} if the
	 *             resource id is unknown</li>
	 */
	@SuppressWarnings("unchecked")
	public <T extends ResourcePropertyId> Set<T> getResourcePropertyIds(final ResourceId resourceId) {
		validateResourceId(resourceId);
		final Map<ResourcePropertyId, PropertyDefinition> defMap = resourcePropertyDefinitions.get(resourceId);
		final Set<T> result = new LinkedHashSet<>(defMap.keySet().size());
		for (final ResourcePropertyId resourcePropertyId : defMap.keySet()) {
			result.add((T) resourcePropertyId);
		}
		return result;
	}

	/**
	 * Returns the last assignment time for the resource property
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
	 */

	public double getResourcePropertyTime(final ResourceId resourceId, final ResourcePropertyId resourcePropertyId) {
		validateResourceId(resourceId);
		validateResourcePropertyId(resourceId, resourcePropertyId);
		return resourcePropertyMap.get(resourceId).get(resourcePropertyId).getAssignmentTime();
	}

	/**
	 * Returns the value of the resource property.
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
	 */
	@SuppressWarnings("unchecked")
	public <T> T getResourcePropertyValue(final ResourceId resourceId, final ResourcePropertyId resourcePropertyId) {
		validateResourceId(resourceId);
		validateResourcePropertyId(resourceId, resourcePropertyId);
		return (T) resourcePropertyMap.get(resourceId).get(resourcePropertyId).getValue();
	}

	/**
	 * Increase the resource for the particular person and resource by the
	 * amount.
	 *
	 * @throws RuntimeException
	 *             <li>if the resource id is null</li>
	 *             <li>if the resource id is unknown</li>
	 *             <li>if the person id null</li>
	 *             <li>if the person id has a negative value</li>
	 *             <li>if the amount causes an overflow</li>
	 */
	private void incrementPersonResourceLevel(final ResourceId resourceId, final PersonId personId, final long resourceAmount) {
		personResourceValues.get(resourceId).incrementLongValue(personId.getValue(), resourceAmount);
		/*
		 * if the resource assignment times are being tracked, then record the
		 * resource time.
		 */
		final DoubleValueContainer doubleValueContainer = personResourceTimes.get(resourceId);
		if (doubleValueContainer != null) {
			doubleValueContainer.setValue(personId.getValue(), dataManagerContext.getTime());
		}
	}

	/**
	 * Increases the resource for the particular region and resource by the
	 * amount.
	 *
	 * @throws RuntimeException
	 *             <li>if the resource id is null</li>
	 *             <li>if the resource id is unknown</li>
	 *             <li>if the region id null</li>
	 *             <li>if the region id is unknown</li>
	 *             <li>if the amount is negative</li>
	 *             <li>if the amount causes an overflow</li>
	 *
	 * @throws ContractException
	 *
	 *             <li>{@linkplain ResourceError#NEGATIVE_RESOURCE_AMOUNT} if
	 *             the amount is negative</li>
	 */
	private void incrementRegionResourceLevel(final RegionId regionId, final ResourceId resourceId, final long amount) {
		final RegionResourceRecord regionResourceRecord = regionResources.get(regionId).get(resourceId);
		regionResourceRecord.incrementAmount(amount);
	}

	/**
	 * 
	 * <ul>
	 * <li>Adds all event labelers defined by the following events <blockquote>
	 * <ul>
	 * <li>{@linkplain PersonResourceUpdateEvent}</li>
	 * <li>{@linkplain RegionResourceUpdateEvent}</li>
	 * <li>{@linkplain ResourcePropertyUpdateEvent}</li>
	 * </ul>
	 * </blockquote></li>
	 * 
	 * <li>Sets resource property values from the
	 * {@linkplain ResourcesPluginData}</li>
	 * 
	 * <li>Sets region resource levels from the
	 * {@linkplain ResourcesPluginData}</li>
	 * 
	 * <li>Sets person resource levels from the
	 * {@linkplain ResourcesPluginData}</li>
	 * 
	 * <P>
	 * Subscribes to the following events:
	 * 
	 * <ul>
	 *
	 *
	 * <li>{@linkplain PersonAdditionEvent}<blockquote> Sets the
	 * person's initial resource levels in the {@linkplain ResourcesDataManager}
	 * from the ResourceInitialization references in the auxiliary data of the
	 * event.
	 * 
	 * <BR>
	 * <BR>
	 * Throws {@link ContractException}
	 * <ul>
	 * <li>{@linkplain PersonError#NULL_PERSON_ID} if the person id is null</li>
	 * <li>{@linkplain PersonError#UNKNOWN_PERSON_ID} if the person id is
	 * unknown</li>
	 * <li>{@linkplain ResourceError#NULL_RESOURCE_ID} if the auxiliary data
	 * contains a ResourceInitialization that has a null resource id</li>
	 * <li>{@linkplain ResourceError#UNKNOWN_RESOURCE_ID} if the auxiliary data
	 * contains a ResourceInitialization that has an unknown resource id</li>
	 * <li>{@linkplain ResourceError#NEGATIVE_RESOURCE_AMOUNT} if the auxiliary
	 * data contains a ResourceInitialization that has a negative resource
	 * level</li>
	 * </ul>
	 * 
	 * 
	 * </blockquote></li>
	 * -------------------------------------------------------------------------------
	 * <li>{@linkplain BulkPersonAdditionEvent}<blockquote> Sets each
	 * person's initial resource levels in the {@linkplain ResourcesDataManager}
	 * from the ResourceInitialization references in the auxiliary data of the
	 * event.
	 * 
	 * <BR>
	 * <BR>
	 * Throws {@link ContractException}
	 * <ul>
	 * <li>{@linkplain PersonError#NULL_PERSON_ID} if the person id is null</li>
	 * <li>{@linkplain PersonError#UNKNOWN_PERSON_ID} if the person id is
	 * unknown</li>
	 * <li>{@linkplain ResourceError#NULL_RESOURCE_ID} if the auxiliary data
	 * contains a ResourceInitialization that has a null resource id</li>
	 * <li>{@linkplain ResourceError#UNKNOWN_RESOURCE_ID} if the auxiliary data
	 * contains a ResourceInitialization that has an unknown resource id</li>
	 * <li>{@linkplain ResourceError#NEGATIVE_RESOURCE_AMOUNT} if the auxiliary
	 * data contains a ResourceInitialization that has a negative resource
	 * level</li>
	 * </ul>
	 * 
	 * </blockquote></li>
	 * -------------------------------------------------------------------------------
	 * <li>{@linkplain PersonImminentRemovalEvent}<blockquote>
	 * Removes the resource assignment data for the person from the
	 * {@linkplain ResourcesDataManager} by scheduling the removal for the
	 * current time. This allows the person and their resource levels to remain
	 * long enough for resolvers, agents and reports to have final reference to
	 * the person while still associated with any relevant resources. <BR>
	 * <BR>
	 * Throws {@linkplain ContractException}
	 * <ul>
	 * <li>{@linkplain PersonError#NULL_PERSON_ID} if the person id is null</li>
	 * <li>{@linkplain PersonError#UNKNOWN_PERSON_ID} if the person id is
	 * unknown</li>
	 * </ul>
	 * 
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

		dataManagerContext.addEventLabeler(PersonResourceUpdateEvent.getEventLabelerForRegionAndResource(regionsDataManager));
		dataManagerContext.addEventLabeler(PersonResourceUpdateEvent.getEventLabelerForPersonAndResource());
		dataManagerContext.addEventLabeler(PersonResourceUpdateEvent.getEventLabelerForResource());
		dataManagerContext.addEventLabeler(ResourcePropertyUpdateEvent.getEventLabeler());
		dataManagerContext.addEventLabeler(RegionResourceUpdateEvent.getEventLabelerForRegionAndResource());

		regionsDataManager = dataManagerContext.getDataManager(RegionsDataManager.class);

		peopleDataManager = dataManagerContext.getDataManager(PeopleDataManager.class);

		// load resource property definitions, property values and time tracking
		// policies
		for (ResourceId resourceId : resourcesPluginData.getResourceIds()) {
			// private final Map<ResourceId, TimeTrackingPolicy>
			// resourceTimeTrackingPolicies = new LinkedHashMap<>();
			TimeTrackingPolicy timeTrackingPolicy = resourcesPluginData.getPersonResourceTimeTrackingPolicy(resourceId);
			resourceTimeTrackingPolicies.put(resourceId, timeTrackingPolicy);

			if (timeTrackingPolicy == TimeTrackingPolicy.TRACK_TIME) {
				final DoubleValueContainer doubleValueContainer = new DoubleValueContainer(0D);
				personResourceTimes.put(resourceId, doubleValueContainer);
			}

			// private final Map<ResourceId, Map<ResourcePropertyId,
			// PropertyValueRecord>> resourcePropertyMap = new
			// LinkedHashMap<>();
			// private final Map<ResourceId, Map<ResourcePropertyId,
			// PropertyDefinition>> resourcePropertyDefinitions = new
			// LinkedHashMap<>();

			final IntValueContainer intValueContainer = new IntValueContainer(0L);
			personResourceValues.put(resourceId, intValueContainer);

			Set<ResourcePropertyId> resourcePropertyIds = resourcesPluginData.getResourcePropertyIds(resourceId);
			for (ResourcePropertyId resourcePropertyId : resourcePropertyIds) {
				PropertyDefinition propertyDefinition = resourcesPluginData.getResourcePropertyDefinition(resourceId, resourcePropertyId);
				Object resourcePropertyValue = resourcesPluginData.getResourcePropertyValue(resourceId, resourcePropertyId);

				Map<ResourcePropertyId, PropertyDefinition> defMap = resourcePropertyDefinitions.get(resourceId);
				if (defMap != null) {
					if (defMap.containsKey(resourcePropertyId)) {
						throw new ContractException(ResourceError.DUPLICATE_RESOURCE_PROPERTY_DEFINITION, resourcePropertyId);
					}
				}

				if (defMap == null) {
					defMap = new LinkedHashMap<>();
					resourcePropertyDefinitions.put(resourceId, defMap);
				}

				defMap.put(resourcePropertyId, propertyDefinition);

				Map<ResourcePropertyId, PropertyValueRecord> map = resourcePropertyMap.get(resourceId);
				if (map == null) {
					map = new LinkedHashMap<>();
					resourcePropertyMap.put(resourceId, map);
				}

				final PropertyValueRecord propertyValueRecord = new PropertyValueRecord(dataManagerContext);
				propertyValueRecord.setPropertyValue(resourcePropertyValue);
				map.put(resourcePropertyId, propertyValueRecord);

			}
		}

		// load the region resources
		// private final Map<RegionId, Map<ResourceId, RegionResourceRecord>>
		// regionResources = new LinkedHashMap<>();
		Set<RegionId> regionIds = regionsDataManager.getRegionIds();

		for (RegionId regionId : regionIds) {
			final Map<ResourceId, RegionResourceRecord> map = new LinkedHashMap<>();
			regionResources.put(regionId, map);
		}
		for (ResourceId resourceId : resourcesPluginData.getResourceIds()) {
			for (final RegionId regionId : regionResources.keySet()) {
				final Map<ResourceId, RegionResourceRecord> map = regionResources.get(regionId);
				map.put(resourceId, new RegionResourceRecord(dataManagerContext));
			}
		}

		for (final RegionId regionId : resourcesPluginData.getRegionIds()) {
			if (!regionIds.contains(regionId)) {
				throw new ContractException(RegionError.UNKNOWN_REGION_ID, regionId + " is an unknown region with initial resources");
			}
			for (final ResourceId resourceId : resourcesPluginData.getResourceIds()) {
				final Long amount = resourcesPluginData.getRegionResourceLevel(regionId, resourceId);
				if (amount != null) {
					final RegionResourceRecord regionResourceRecord = regionResources.get(regionId).get(resourceId);
					regionResourceRecord.incrementAmount(amount);
				}
			}
		}

		// load the person resources
		// private final Map<ResourceId, IntValueContainer> personResourceValues
		// = new LinkedHashMap<>();
		// private final Map<ResourceId, DoubleValueContainer>
		// personResourceTimes = new LinkedHashMap<>();
		for (final PersonId personId : resourcesPluginData.getPersonIds()) {
			if (!peopleDataManager.personExists(personId)) {
				throw new ContractException(PersonError.UNKNOWN_PERSON_ID, personId);
			}
			for (final ResourceId resourceId : personResourceValues.keySet()) {
				final Long resourceAmount = resourcesPluginData.getPersonResourceLevel(personId, resourceId);
				if (resourceAmount > 0) {
					personResourceValues.get(resourceId).incrementLongValue(personId.getValue(), resourceAmount);
					final DoubleValueContainer doubleValueContainer = personResourceTimes.get(resourceId);
					if (doubleValueContainer != null) {
						doubleValueContainer.setValue(personId.getValue(), dataManagerContext.getTime());
					}
				}
			}
		}

		dataManagerContext.subscribe(PersonAdditionEvent.class, this::handlePersonAdditionEvent);
		dataManagerContext.subscribe(BulkPersonAdditionEvent.class, this::handleBulkPersonAdditionEvent);
		dataManagerContext.subscribe(PersonImminentRemovalEvent.class, this::handlePersonImminentRemovalEvent);
	}

	/**
	 * Returns true if and only if the given resource id is known
	 */
	public boolean resourceIdExists(final ResourceId resourceId) {
		return personResourceValues.containsKey(resourceId);
	}

	/**
	 * Returns true if and only if there is a resource property defined for the
	 * given resource id and resource property id
	 */
	public boolean resourcePropertyIdExists(final ResourceId resourceId, final ResourcePropertyId resourcePropertyId) {
		if ((resourceId == null) || (resourcePropertyId == null)) {
			return false;
		}

		final Map<ResourcePropertyId, PropertyValueRecord> map = resourcePropertyMap.get(resourceId);

		if (map == null) {
			return false;
		}

		if (!map.containsKey(resourcePropertyId)) {
			return false;
		}

		return true;

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
		if (resourceTimeTrackingPolicies.get(resourceId) != TimeTrackingPolicy.TRACK_TIME) {
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
		if (!personResourceValues.containsKey(resourceId)) {
			throw new ContractException(ResourceError.UNKNOWN_RESOURCE_ID, resourceId);
		}
	}

	/*
	 * Precondition : the resource id must exist
	 */
	private void validateResourcePropertyId(final ResourceId resourceId, final ResourcePropertyId resourcePropertyId) {
		if (resourcePropertyId == null) {
			throw new ContractException(ResourceError.NULL_RESOURCE_PROPERTY_ID);
		}

		final Map<ResourcePropertyId, PropertyValueRecord> map = resourcePropertyMap.get(resourceId);

		if ((map == null) || !map.containsKey(resourcePropertyId)) {
			throw new ContractException(ResourceError.UNKNOWN_RESOURCE_PROPERTY_ID, resourcePropertyId);
		}

	}

	/**
	 * Transfers resources from one region to another. Generates the
	 * corresponding {@linkplain RegionResourceUpdateEvent} events
	 * for each region.
	 * 
	 * @throws ContractException
	 *             <li>{@linkplain RegionError#NULL_REGION_ID} if the source
	 *             region is null</li>
	 *             <li>{@linkplain RegionError#UNKNOWN_REGION_ID} if the source
	 *             region is unknown</li>
	 *             <li>{@linkplain RegionError#NULL_REGION_ID} if the
	 *             destination region is null</li>
	 *             <li>{@linkplain RegionError#UNKNOWN_REGION_ID} if the
	 *             destination region is unknown</li>
	 *             <li>{@linkplain ResourceError#NULL_RESOURCE_ID} if the
	 *             resource id is null</li>
	 *             <li>{@linkplain ResourceError#UNKNOWN_RESOURCE_ID} if the
	 *             resource id is unknown</li>
	 *             <li>{@linkplain ResourceError#NEGATIVE_RESOURCE_AMOUNT} if
	 *             the resource amount is negative</li>
	 *             <li>{@linkplain ResourceError#REFLEXIVE_RESOURCE_TRANSFER} if
	 *             the source and destination region are equal</li>
	 *             <li>{@linkplain ResourceError#INSUFFICIENT_RESOURCES_AVAILABLE}
	 *             if the source region does not have sufficient resources to
	 *             support the transfer</li>
	 *             <li>{@linkplain ResourceError#RESOURCE_ARITHMETIC_EXCEPTION}
	 *             if the transfer will cause a numeric overflow in the
	 *             destination region</li>
	 */
	public void transferResourceBetweenRegions(ResourceId resourceId, RegionId sourceRegionId, RegionId destinationRegionId, long amount) {

		validateRegionId(sourceRegionId);
		validateRegionId(destinationRegionId);
		validateResourceId(resourceId);
		validateNonnegativeResourceAmount(amount);
		validateDifferentRegionsForResourceTransfer(sourceRegionId, destinationRegionId);
		validateRegionHasSufficientResources(resourceId, sourceRegionId, amount);

		RegionResourceRecord sourceRecord = regionResources.get(sourceRegionId).get(resourceId);
		RegionResourceRecord destinationRecord = regionResources.get(destinationRegionId).get(resourceId);

		final long regionResourceLevel = regionResources.get(destinationRegionId).get(resourceId).getAmount();

		validateResourceAdditionValue(regionResourceLevel, amount);

		final long previousSourceRegionResourceLevel = sourceRecord.getAmount();
		final long previousDestinationRegionResourceLevel = destinationRecord.getAmount();

		decrementRegionResourceLevel(sourceRegionId, resourceId, amount);
		incrementRegionResourceLevel(destinationRegionId, resourceId, amount);

		long currentSourceRegionResourceLevel = sourceRecord.getAmount();
		long currentDestinationRegionResourceLevel = destinationRecord.getAmount();

		dataManagerContext.releaseEvent(new RegionResourceUpdateEvent(sourceRegionId, resourceId, previousSourceRegionResourceLevel, currentSourceRegionResourceLevel));
		dataManagerContext.releaseEvent(new RegionResourceUpdateEvent(destinationRegionId, resourceId, previousDestinationRegionResourceLevel, currentDestinationRegionResourceLevel));

	}

	private void validateNonnegativeResourceAmount(final long amount) {
		if (amount < 0) {
			throw new ContractException(ResourceError.NEGATIVE_RESOURCE_AMOUNT);
		}
	}

	private void validateDifferentRegionsForResourceTransfer(final RegionId sourceRegionId, final RegionId destinationRegionId) {
		if (sourceRegionId.equals(destinationRegionId)) {
			throw new ContractException(ResourceError.REFLEXIVE_RESOURCE_TRANSFER);
		}
	}

	/*
	 * Preconditions : the region and resource must exist
	 */
	private void validateRegionHasSufficientResources(final ResourceId resourceId, final RegionId regionId, final long amount) {
		RegionResourceRecord regionResourceRecord = regionResources.get(regionId).get(resourceId);
		final long currentAmount = regionResourceRecord.getAmount();
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

	/**
	 * Expends an amount of resource from a person. Generates the corresponding
	 * {@linkplain PersonResourceUpdateEvent} event
	 * 
	 * @throws ContractException
	 * 
	 *             <li>{@linkplain PersonError#NULL_PERSON_ID} if the person id
	 *             is null</li>
	 *             <li>{@linkplain PersonError#UNKNOWN_PERSON_ID} if the person
	 *             does not exist</li>
	 *             <li>{@linkplain ResourceError#NULL_RESOURCE_ID} if the
	 *             resource id is null</li>
	 *             <li>{@linkplain ResourceError#UNKNOWN_RESOURCE_ID} if the
	 *             resource id is unknown</li>
	 *             <li>{@linkplain ResourceError#NEGATIVE_RESOURCE_AMOUNT} if
	 *             the amount is negative</li>
	 *             <li>{@linkplain ResourceError#INSUFFICIENT_RESOURCES_AVAILABLE}
	 *             if the person does not have the required amount of the
	 *             resource</li>
	 * 
	 */
	public void removeResourceFromPerson(ResourceId resourceId, PersonId personId, long amount) {

		validatePersonExists(personId);
		validateResourceId(resourceId);
		validateNonnegativeResourceAmount(amount);
		validatePersonHasSufficientResources(resourceId, personId, amount);

		final long oldLevel = personResourceValues.get(resourceId).getValueAsLong(personId.getValue());
		decrementPersonResourceLevel(resourceId, personId, amount);
		final long newLevel = personResourceValues.get(resourceId).getValueAsLong(personId.getValue());
		dataManagerContext.releaseEvent(new PersonResourceUpdateEvent(personId, resourceId, oldLevel, newLevel));
	}

	/*
	 * Preconditions : the resource and person must exist
	 */
	private void validatePersonHasSufficientResources(final ResourceId resourceId, final PersonId personId, final long amount) {
		final long oldValue = personResourceValues.get(resourceId).getValueAsLong(personId.getValue());
		if (oldValue < amount) {
			throw new ContractException(ResourceError.INSUFFICIENT_RESOURCES_AVAILABLE);
		}
	}

	/**
	 * Adds an amount of resource to a region. Generates the corresponding
	 * {@linkplain RegionResourceUpdateEvent} event.
	 * 
	 * @throws ContractException
	 * 
	 *             <li>{@linkplain RegionError#NULL_REGION_ID} if the region id
	 *             is null</li>
	 *             <li>{@linkplain RegionError#UNKNOWN_REGION_ID} if the region
	 *             id is unknown</li>
	 *             <li>{@linkplain ResourceError#NULL_RESOURCE_ID} if the
	 *             resource id is null</li>
	 *             <li>{@linkplain ResourceError#UNKNOWN_RESOURCE_ID} if the
	 *             resource id is unknown</li>
	 *             <li>{@linkplain ResourceError#NEGATIVE_RESOURCE_AMOUNT} if
	 *             the amount is negative</li>
	 *             <li>{@linkplain ResourceError#RESOURCE_ARITHMETIC_EXCEPTION}
	 *             if the addition results in an overflow</li>
	 * 
	 * 
	 * 
	 */
	public void addResourceToRegion(ResourceId resourceId, RegionId regionId, long amount) {
		validateRegionId(regionId);
		validateResourceId(resourceId);
		validateNonnegativeResourceAmount(amount);
		final long previousResourceLevel = regionResources.get(regionId).get(resourceId).getAmount();
		validateResourceAdditionValue(previousResourceLevel, amount);
		incrementRegionResourceLevel(regionId, resourceId, amount);
		long currentResourceLevel = regionResources.get(regionId).get(resourceId).getAmount();
		dataManagerContext.releaseEvent(new RegionResourceUpdateEvent(regionId, resourceId, previousResourceLevel, currentResourceLevel));
	}

	/**
	 * Removes an amount of resource from a region.Generates the corresponding
	 * {@linkplain RegionResourceUpdateEvent} event<
	 * 
	 * @throws ContractException
	 * 
	 *             <li>{@linkplain RegionError#NULL_REGION_ID} if the region id
	 *             is null</li>
	 *             <li>{@linkplain RegionError#UNKNOWN_REGION_ID} if the region
	 *             id is unknown</li>
	 *             <li>{@linkplain ResourceError#NULL_RESOURCE_ID} if the
	 *             resource id is null</li>
	 *             <li>{@linkplain ResourceError#UNKNOWN_RESOURCE_ID} if the
	 *             resource id is unknown</li>
	 *             <li>{@linkplain ResourceError#NEGATIVE_RESOURCE_AMOUNT} if
	 *             the amount is negative</li>
	 *             <li>{@linkplain ResourceError#INSUFFICIENT_RESOURCES_AVAILABLE}
	 *             if the region does not have the required amount of the
	 *             resource</li>
	 *
	 * 
	 */
	public void removeResourceFromRegion(ResourceId resourceId, RegionId regionId, long amount) {
		validateRegionId(regionId);
		validateResourceId(resourceId);
		validateNonnegativeResourceAmount(amount);
		validateRegionHasSufficientResources(resourceId, regionId, amount);
		final long previousResourceLevel = regionResources.get(regionId).get(resourceId).getAmount();
		decrementRegionResourceLevel(regionId, resourceId, amount);
		long currentResourceLevel = regionResources.get(regionId).get(resourceId).getAmount();
		dataManagerContext.releaseEvent(new RegionResourceUpdateEvent(regionId, resourceId, previousResourceLevel, currentResourceLevel));
	}

	/**
	 * Assigns a value to a resource property. Generates the corresponding
	 * {@linkplain ResourcePropertyUpdateEvent} event
	 * 
	 * @throws ContractException
	 * 
	 *             <li>{@linkplain ResourceError#NULL_RESOURCE_ID} if the
	 *             resource id is null</li>
	 *             <li>{@linkplain ResourceError#UNKNOWN_RESOURCE_ID} if the
	 *             resource id is unknown</li>
	 *             <li>{@linkplain ResourceError#NULL_RESOURCE_PROPERTY_ID} if
	 *             the resource property id is null</li>
	 *             <li>{@linkplain ResourceError#UNKNOWN_RESOURCE_PROPERTY_ID}
	 *             if the resource property id is unknown</li>
	 *             <li>{@linkplain ResourceError#NULL_RESOURCE_PROPERTY_VALUE}
	 *             if the resource property value is null</li>
	 *             <li>{@linkplain PropertyError#INCOMPATIBLE_VALUE} if the
	 *             resource property value is incompatible with the
	 *             corresponding property definition</li>
	 *             <li>{@linkplain PropertyError#IMMUTABLE_VALUE} if the
	 *             property has been defined as immutable</li>
	 * 
	 * 
	 * 
	 */
	public void setResourcePropertyValue(ResourceId resourceId, ResourcePropertyId resourcePropertyId, Object resourcePropertyValue) {
		validateResourceId(resourceId);
		validateResourcePropertyId(resourceId, resourcePropertyId);
		validateResourcePropertyValueNotNull(resourcePropertyValue);
		final PropertyDefinition propertyDefinition = resourcePropertyDefinitions.get(resourceId).get(resourcePropertyId);
		validateValueCompatibility(resourcePropertyId, propertyDefinition, resourcePropertyValue);
		validatePropertyMutability(propertyDefinition);
		final Object oldPropertyValue = resourcePropertyMap.get(resourceId).get(resourcePropertyId).getValue();
		resourcePropertyMap.get(resourceId).get(resourcePropertyId).setPropertyValue(resourcePropertyValue);
		dataManagerContext.releaseEvent(new ResourcePropertyUpdateEvent(resourceId, resourcePropertyId, oldPropertyValue, resourcePropertyValue));
	}

	private void validateResourcePropertyValueNotNull(final Object propertyValue) {
		if (propertyValue == null) {
			throw new ContractException(ResourceError.NULL_RESOURCE_PROPERTY_VALUE);
		}
	}

	private void validateValueCompatibility(final Object propertyId, final PropertyDefinition propertyDefinition, final Object propertyValue) {
		if (!propertyDefinition.getType().isAssignableFrom(propertyValue.getClass())) {
			throw new ContractException(PropertyError.INCOMPATIBLE_VALUE,
					"Property value " + propertyValue + " is not of type " + propertyDefinition.getType().getName() + " and does not match definition of " + propertyId);
		}
	}

	private void validatePropertyMutability(final PropertyDefinition propertyDefinition) {
		if (!propertyDefinition.propertyValuesAreMutable()) {
			throw new ContractException(PropertyError.IMMUTABLE_VALUE);
		}
	}

	/**
	 * Transfers an amount of resource from a person to the person's current
	 * region. Generates the corresponding
	 * {@linkplain RegionResourceUpdateEvent} and
	 * {@linkplain PersonResourceUpdateEvent} events
	 * 
	 * @throws ContractException
	 * 
	 *             <li>{@linkplain PersonError#NULL_PERSON_ID} if the person id
	 *             is null</li>
	 *             <li>{@linkplain PersonError#UNKNOWN_PERSON_ID} if the person
	 *             does not exist</li>
	 *             <li>{@linkplain ResourceError#NULL_RESOURCE_ID} if the
	 *             resource id is null</li>
	 *             <li>{@linkplain ResourceError#UNKNOWN_RESOURCE_ID} if the
	 *             resource id is unknown</li>
	 *             <li>{@linkplain ResourceError#NEGATIVE_RESOURCE_AMOUNT} if
	 *             the amount is negative</li>
	 *             <li>{@linkplain ResourceError#INSUFFICIENT_RESOURCES_AVAILABLE}
	 *             if the person does not have the required amount of the
	 *             resource</li>
	 *             <li>{@linkplain ResourceError#RESOURCE_ARITHMETIC_EXCEPTION}
	 *             if the transfer results in an overflow of the region's
	 *             resource level</li>
	 */
	public void transferResourceFromPersonToRegion(ResourceId resourceId, PersonId personId, long amount) {
		validatePersonExists(personId);
		validateResourceId(resourceId);
		validateNonnegativeResourceAmount(amount);
		validatePersonHasSufficientResources(resourceId, personId, amount);
		final RegionId regionId = regionsDataManager.getPersonRegion(personId);
		final long previousRegionResourceLevel = regionResources.get(regionId).get(resourceId).getAmount();
		validateResourceAdditionValue(previousRegionResourceLevel, amount);
		final long oldLevel = personResourceValues.get(resourceId).getValueAsLong(personId.getValue());
		decrementPersonResourceLevel(resourceId, personId, amount);
		final long newLevel = personResourceValues.get(resourceId).getValueAsLong(personId.getValue());
		incrementRegionResourceLevel(regionId, resourceId, amount);
		long currentRegionResourceLevel = regionResources.get(regionId).get(resourceId).getAmount();
		dataManagerContext.releaseEvent(new PersonResourceUpdateEvent(personId, resourceId, oldLevel, newLevel));
		dataManagerContext.releaseEvent(new RegionResourceUpdateEvent(regionId, resourceId, previousRegionResourceLevel, currentRegionResourceLevel));
	}

	/**
	 * Transfers an amount of resource to a person from the person's current
	 * region. Generates the corresponding
	 * {@linkplain RegionResourceUpdateEvent} and
	 * {@linkplain PersonResourceUpdateEvent} events
	 * 
	 * 
	 * 
	 * @throws ContractException
	 * 
	 *             <li>{@linkplain PersonError#NULL_PERSON_ID} if the person id
	 *             is null</li>
	 *             <li>{@linkplain PersonError#UNKNOWN_PERSON_ID} if the person
	 *             does not exist</li>
	 *             <li>{@linkplain ResourceError#NULL_RESOURCE_ID} if the
	 *             resource id is null</li>
	 *             <li>{@linkplain ResourceError#UNKNOWN_RESOURCE_ID} if the
	 *             resource id is unknown</li>
	 *             <li>{@linkplain ResourceError#NEGATIVE_RESOURCE_AMOUNT} if
	 *             the amount is negative</li>
	 *             <li>{@linkplain ResourceError#INSUFFICIENT_RESOURCES_AVAILABLE}
	 *             if the region does not have the required amount of the
	 *             resource</li>
	 *             <li>{@linkplain ResourceError#RESOURCE_ARITHMETIC_EXCEPTION}
	 *             if the transfer results in an overflow of the person's
	 *             resource level</li>
	 *
	 */
	public void transferResourceToPersonFromRegion(ResourceId resourceId, PersonId personId, long amount) {

		validatePersonExists(personId);
		validateResourceId(resourceId);
		validateNonnegativeResourceAmount(amount);
		final RegionId regionId = regionsDataManager.getPersonRegion(personId);
		validateRegionHasSufficientResources(resourceId, regionId, amount);
		final long personResourceLevel = personResourceValues.get(resourceId).getValueAsLong(personId.getValue());
		validateResourceAdditionValue(personResourceLevel, amount);

		final long previousRegionResourceLevel = regionResources.get(regionId).get(resourceId).getAmount();
		decrementRegionResourceLevel(regionId, resourceId, amount);
		incrementPersonResourceLevel(resourceId, personId, amount);
		final long newLevel = personResourceValues.get(resourceId).getValueAsLong(personId.getValue());
		long currentRegionResourceLevel = regionResources.get(regionId).get(resourceId).getAmount();

		dataManagerContext.releaseEvent(new RegionResourceUpdateEvent(regionId, resourceId, previousRegionResourceLevel, currentRegionResourceLevel));

		dataManagerContext.releaseEvent(new PersonResourceUpdateEvent(personId, resourceId, personResourceLevel, newLevel));

	}

	private void handlePersonAdditionEvent(final DataManagerContext dataManagerContext, final PersonAdditionEvent personAdditionEvent) {
		PersonId personId = personAdditionEvent.getPersonId();
		PersonConstructionData personConstructionData = personAdditionEvent.getPersonConstructionData();
		validatePersonExists(personId);
		List<ResourceInitialization> resourceAssignments = personConstructionData.getValues(ResourceInitialization.class);
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

	private void handleBulkPersonAdditionEvent(final DataManagerContext dataManagerContext, final BulkPersonAdditionEvent bulkPersonAdditionEvent) {
		PersonId personId = bulkPersonAdditionEvent.getPersonId();
		BulkPersonConstructionData bulkPersonConstructionData = bulkPersonAdditionEvent.getBulkPersonConstructionData();
		List<PersonConstructionData> personConstructionDatas = bulkPersonConstructionData.getPersonConstructionDatas();
		for (PersonConstructionData personConstructionData : personConstructionDatas) {
			List<ResourceInitialization> resourceAssignments = personConstructionData.getValues(ResourceInitialization.class);
			for (final ResourceInitialization resourceAssignment : resourceAssignments) {
				ResourceId resourceId = resourceAssignment.getResourceId();
				Long amount = resourceAssignment.getAmount();
				validateResourceId(resourceId);
				validateNonnegativeResourceAmount(amount);
			}
		}

		int pId = personId.getValue();

		for (PersonConstructionData personConstructionData : personConstructionDatas) {
			List<ResourceInitialization> resourceAssignments = personConstructionData.getValues(ResourceInitialization.class);
			PersonId boxedPersonId = peopleDataManager.getBoxedPersonId(pId).get();
			for (final ResourceInitialization resourceAssignment : resourceAssignments) {
				incrementPersonResourceLevel(resourceAssignment.getResourceId(), boxedPersonId, resourceAssignment.getAmount());
			}
			pId++;
		}

	}

	private void handlePersonImminentRemovalEvent(final DataManagerContext dataManagerContext, final PersonImminentRemovalEvent personImminentRemovalEvent) {
		validatePersonExists(personImminentRemovalEvent.getPersonId());

		dataManagerContext.addPlan((context) -> {
			PersonId personId = personImminentRemovalEvent.getPersonId();
			for (final IntValueContainer intValueContainer : personResourceValues.values()) {
				intValueContainer.setLongValue(personId.getValue(), 0);
			}
		}, dataManagerContext.getTime());
	}
}
