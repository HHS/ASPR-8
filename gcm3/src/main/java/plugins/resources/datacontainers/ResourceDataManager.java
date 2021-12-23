package plugins.resources.datacontainers;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import nucleus.Context;
import nucleus.NucleusError;
import plugins.people.datacontainers.PersonDataView;
import plugins.people.support.PersonId;
import plugins.properties.support.PropertyDefinition;
import plugins.properties.support.PropertyValueRecord;
import plugins.properties.support.TimeTrackingPolicy;
import plugins.regions.support.RegionError;
import plugins.regions.support.RegionId;
import plugins.resources.ResourcesPlugin;
import plugins.resources.support.ResourceError;
import plugins.resources.support.ResourceId;
import plugins.resources.support.ResourcePropertyId;
import util.ContractException;
import util.arraycontainers.DoubleValueContainer;
import util.arraycontainers.IntValueContainer;

/**
 * Mutable data manager that backs the {@linkplain ResourcesDataView}. This data
 * manager is for internal use by the {@link ResourcesPlugin} and should not be
 * published.
 * 
 * All resources are defined during construction and cannot be changed. Resource
 * property values are mutable and specific to the type of resource. Limited
 * validation of inputs are performed and mutation methods have invocation
 * ordering requirements.
 * 
 * @author Shawn Hatch
 *
 */

public final class ResourceDataManager {
	/*
	 * Static utility class for tracking region resources.
	 */
	private static class RegionResourceRecord {
		private final Context context;

		private long amount;

		private double assignmentTime;

		public RegionResourceRecord(final Context context) {
			this.context = context;
		}

		public void decrementAmount(final long amount) {
			if (amount < 0) {
				throw new ContractException(ResourceError.NEGATIVE_RESOURCE_AMOUNT);
			}

			if (this.amount < amount) {
				throw new ContractException(ResourceError.INSUFFICIENT_RESOURCES_AVAILABLE);
			}
			this.amount = Math.subtractExact(this.amount, amount);
			assignmentTime = context.getTime();
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
			assignmentTime = context.getTime();
		}

	}

	private PersonDataView personDataView;

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

	private Context context;

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
	public void decrementPersonResourceLevel(final ResourceId resourceId, final PersonId personId, final long resourceAmount) {
		personResourceValues.get(resourceId).decrementLongValue(personId.getValue(), resourceAmount);
		/*
		 * if the resource assignment times are being tracked, then record the
		 * resource time.
		 */
		final DoubleValueContainer doubleValueContainer = personResourceTimes.get(resourceId);
		if (doubleValueContainer != null) {
			doubleValueContainer.setValue(personId.getValue(), context.getTime());
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

	public void decrementRegionResourceLevel(final RegionId regionId, final ResourceId resourceId, final long amount) {
		final RegionResourceRecord regionResourceRecord = regionResources.get(regionId).get(resourceId);
		regionResourceRecord.decrementAmount(amount);
	}

	/**
	 * Sets all resource values for the person to zero
	 * 
	 * @throws RuntimeException
	 *             <li>if the personId id is null</li>
	 *             <li>if the personId id has a negative value</li>
	 */
	public void dropPerson(final PersonId personId) {
		for (final IntValueContainer intValueContainer : personResourceValues.values()) {
			intValueContainer.setLongValue(personId.getValue(), 0);
		}
	}

	/**
	 * Returns the set of people who do not have any of the given resource as a
	 * list.
	 * 
	 * @throws RuntimeException
	 *             <li>if the resource id is null</li>
	 *             <li>if the resource id is unknown</li>
	 */
	public List<PersonId> getPeopleWithoutResource(final ResourceId resourceId) {
		/*
		 * First, we loop through all possible person id values and determine
		 * the exact size of the returned list.
		 */
		int count = 0;
		final IntValueContainer intValueContainer = personResourceValues.get(resourceId);
		final int n = personDataView.getPersonIdLimit();
		for (int personIndex = 0; personIndex < n; personIndex++) {
			if (personDataView.personIndexExists(personIndex)) {
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
			if (personDataView.personIndexExists(personId)) {
				final long resourceLevel = intValueContainer.getValueAsLong(personId);
				if (resourceLevel == 0) {
					result.add(personDataView.getBoxedPersonId(personId));
				}
			}
		}
		return result;
	}

	/**
	 * Returns the set of people who have at least one unit of the given
	 * resource as a list.
	 * 
	 * @throws RuntimeException
	 *             <li>if the resource id is null</li>
	 *             <li>if the resource id is unknown</li>
	 */
	public List<PersonId> getPeopleWithResource(final ResourceId resourceId) {
		/*
		 * First, we loop through all possible person id values and determine
		 * the exact size of the returned list.
		 */
		int count = 0;
		final IntValueContainer intValueContainer = personResourceValues.get(resourceId);
		final int n = personDataView.getPersonIdLimit();
		for (int personId = 0; personId < n; personId++) {
			if (personDataView.personIndexExists(personId)) {
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
			if (personDataView.personIndexExists(personIndex)) {
				final long resourceLevel = intValueContainer.getValueAsLong(personIndex);
				if (resourceLevel > 0) {
					result.add(personDataView.getBoxedPersonId(personIndex));
				}
			}
		}
		return result;

	}

	/**
	 * Returns the current resource level for the given resource id and person
	 * id
	 * 
	 * @throws RuntimeException
	 *             <li>if the resource id is null</li>
	 *             <li>if the resource id is unknown</li>
	 *             <li>if the person id null</li>
	 *             <li>if the person id has a negative value</li>
	 */
	public long getPersonResourceLevel(final ResourceId resourceId, final PersonId personId) {
		return personResourceValues.get(resourceId).getValueAsLong(personId.getValue());
	}

	/**
	 * Returns the time when the current resource level for the given resource
	 * id and person id was assigned.
	 * 
	 * @throws RuntimeException
	 *             <li>if the assignment times for the resource are not
	 *             tracked</li>
	 *             <li>if the resource id is null</li>
	 *             <li>if the resource id is unknown</li>
	 *             <li>if the person id null</li>
	 *             <li>if the person id has a negative value</li>
	 */
	public double getPersonResourceTime(final ResourceId resourceId, final PersonId personId) {
		final DoubleValueContainer doubleValueContainer = personResourceTimes.get(resourceId);
		return doubleValueContainer.getValue(personId.getValue());
	}

	/**
	 * Returns the TimeTrackingPolicy for the given resource id. Returns null if
	 * the resource id is null or unknown.
	 * 
	 */
	public TimeTrackingPolicy getPersonResourceTimeTrackingPolicy(final ResourceId resourceId) {
		return resourceTimeTrackingPolicies.get(resourceId);
	}

	/**
	 * Returns the current resource level for the given resource id and region
	 * id
	 * 
	 * @throws RuntimeException
	 *             <li>if the resource id is null</li>
	 *             <li>if the resource id is unknown</li>
	 *             <li>if the region id null</li>
	 *             <li>if the region id is unknown</li>
	 */
	public long getRegionResourceLevel(final RegionId regionId, final ResourceId resourceId) {
		final RegionResourceRecord regionResourceRecord = regionResources.get(regionId).get(resourceId);
		return regionResourceRecord.getAmount();
	}

	/**
	 * Returns the time when the current resource level for the given resource
	 * id and region id was assigned.
	 * 
	 * @throws RuntimeException
	 *             <li>if the resource id is null</li>
	 *             <li>if the resource id is unknown</li>
	 *             <li>if the region id null</li>
	 *             <li>if the region id is unknown</li>
	 */
	public double getRegionResourceTime(final RegionId regionId, final ResourceId resourceId) {
		final RegionResourceRecord regionResourceRecord = regionResources.get(regionId).get(resourceId);
		return regionResourceRecord.getAssignmentTime();
	}

	/**
	 * Returns the set of resource ids
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
	 * Returns the property definition associated with the given resource id and
	 * resource property id
	 * 
	 * @throws RuntimeException
	 *             <li>if the resource id is null</li>
	 *             <li>if the resource id unknown</li>
	 *             <li>if the resource property id is null</li>
	 *             <li>if the resource property id unknown(not associated with
	 *             the resource id)</li>
	 */
	public PropertyDefinition getResourcePropertyDefinition(final ResourceId resourceId, final ResourcePropertyId resourcePropertyId) {
		return resourcePropertyDefinitions.get(resourceId).get(resourcePropertyId);
	}

	/**
	 * Returns the set of resource property ids for the given resource id
	 *
	 * @throws RuntimeException
	 *             <li>if the resource id is null</li>
	 *             <li>if the resource id unknown</li>
	 */
	@SuppressWarnings("unchecked")
	public <T extends ResourcePropertyId> Set<T> getResourcePropertyIds(final ResourceId resourceId) {
		final Map<ResourcePropertyId, PropertyDefinition> defMap = resourcePropertyDefinitions.get(resourceId);
		final Set<T> result = new LinkedHashSet<>(defMap.keySet().size());
		for (final ResourcePropertyId resourcePropertyId : defMap.keySet()) {
			result.add((T) resourcePropertyId);
		}
		return result;
	}

	/**
	 * 
	 * Returns the time when the property value for the given resource id and
	 * resource property id was last assigned
	 * 
	 * @throws RuntimeException
	 *             <li>if the resource id is null</li>
	 *             <li>if the resource id unknown</li>
	 *             <li>if the resource property id is null</li>
	 *             <li>if the resource property id unknown(not associated with
	 *             the resource id)</li>
	 */
	public double getResourcePropertyTime(final ResourceId resourceId, final ResourcePropertyId resourcePropertyId) {
		return resourcePropertyMap.get(resourceId).get(resourcePropertyId).getAssignmentTime();

	}

	/**
	 * 
	 * Returns the property value for the given resource id and resource
	 * property id
	 * 
	 * @throws RuntimeException
	 *             <li>if the resource id is null</li>
	 *             <li>if the resource id unknown</li>
	 *             <li>if the resource property id is null</li>
	 *             <li>if the resource property id unknown(not associated with
	 *             the resource id)</li>
	 */
	@SuppressWarnings("unchecked")
	public <T> T getResourcePropertyValue(final ResourceId resourceId, final ResourcePropertyId resourcePropertyId) {
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
	public void incrementPersonResourceLevel(final ResourceId resourceId, final PersonId personId, final long resourceAmount) {
		personResourceValues.get(resourceId).incrementLongValue(personId.getValue(), resourceAmount);
		/*
		 * if the resource assignment times are being tracked, then record the
		 * resource time.
		 */
		final DoubleValueContainer doubleValueContainer = personResourceTimes.get(resourceId);
		if (doubleValueContainer != null) {
			doubleValueContainer.setValue(personId.getValue(), context.getTime());
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
	public void incrementRegionResourceLevel(final RegionId regionId, final ResourceId resourceId, final long amount) {
		final RegionResourceRecord regionResourceRecord = regionResources.get(regionId).get(resourceId);
		regionResourceRecord.incrementAmount(amount);
	}

	/**
	 * Adds the resource and establishes the time tracking policy for resource
	 * values associated with people
	 * 
	 * @throws ContractException
	 *             <li>{@linkplain ResourceError#NULL_RESOURCE_ID} if the
	 *             resource id is null</li>
	 *             <li>{@linkplain ResourceError#DUPLICATE_RESOURCE_ID}if the
	 *             resource id was previously added</li>
	 *             <li>{@linkplain ResourceError#NULL_TIME_TRACKING_POLICY}if
	 *             the time tracking policy is null</li>
	 */
	public void addResource(ResourceId resourceId, TimeTrackingPolicy timeTrackingPolicy) {

		if (resourceId == null) {
			throw new ContractException(ResourceError.NULL_RESOURCE_ID);
		}
		if (timeTrackingPolicy == null) {
			throw new ContractException(ResourceError.NULL_TIME_TRACKING_POLICY);
		}

		if (personResourceValues.containsKey(resourceId)) {
			throw new ContractException(ResourceError.DUPLICATE_RESOURCE_ID, resourceId);
		}

		for (RegionId regionId : regionResources.keySet()) {
			Map<ResourceId, RegionResourceRecord> map = regionResources.get(regionId);
			map.put(resourceId, new RegionResourceRecord(context));
		}

		final IntValueContainer intValueContainer = new IntValueContainer(0L);
		personResourceValues.put(resourceId, intValueContainer);
		if (timeTrackingPolicy == TimeTrackingPolicy.TRACK_TIME) {
			final DoubleValueContainer doubleValueContainer = new DoubleValueContainer(0D);
			personResourceTimes.put(resourceId, doubleValueContainer);
		}
		resourceTimeTrackingPolicies.put(resourceId, timeTrackingPolicy);

	}

	/**
	 * Defines the resource property associated with the given resource id and
	 * resource property id
	 * 
	 * @throws ContractException
	 *             <li>{@linkplain ResourceError#NULL_RESOURCE_ID} if the
	 *             resource id is null</li>
	 *             <li>{@linkplain ResourceError#UNKNOWN_RESOURCE_ID} if the
	 *             resource id is unknown</li>
	 *             <li>{@linkplain ResourceError#NULL_RESOURCE_PROPERTY_ID} if
	 *             the resource property id is null</li>
	 *             <li>{@linkplain ResourceError#NULL_RESOURCE_PROPERTY_DEFINITION}
	 *             if the resource property definition is null</li>
	 *             <li>{@linkplain ResourceError#NULL_RESOURCE_PROPERTY_VALUE}
	 *             if the resource property value is null</li>
	 *             <li>{@linkplain ResourceError#INCOMPATIBLE_VALUE} if the
	 *             resource property value is incompatible with the property
	 *             definition</li>
	 *             <li>{@linkplain ResourceError#DUPLICATE_RESOURCE_PROPERTY_DEFINITION}
	 *             if the resource property was previously defined</li>
	 * 
	 */
	public void defineResourceProperty(ResourceId resourceId, ResourcePropertyId resourcePropertyId, PropertyDefinition propertyDefinition, Object resourcePropertyValue) {

		// resource must already exist
		if (resourceId == null) {
			throw new ContractException(ResourceError.NULL_RESOURCE_ID);
		}
		if (!personResourceValues.containsKey(resourceId)) {
			throw new ContractException(ResourceError.UNKNOWN_RESOURCE_ID, resourceId);
		}
		if (resourcePropertyId == null) {
			throw new ContractException(ResourceError.NULL_RESOURCE_PROPERTY_ID);
		}
		if (propertyDefinition == null) {
			throw new ContractException(ResourceError.NULL_RESOURCE_PROPERTY_DEFINITION);
		}
		if (resourcePropertyValue == null) {
			throw new ContractException(ResourceError.NULL_RESOURCE_PROPERTY_VALUE);
		}
		if (!propertyDefinition.getType().isAssignableFrom(resourcePropertyValue.getClass())) {
			throw new ContractException(ResourceError.INCOMPATIBLE_VALUE);
		}

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
		final PropertyValueRecord propertyValueRecord = new PropertyValueRecord(context);
		propertyValueRecord.setPropertyValue(resourcePropertyValue);
		map.put(resourcePropertyId, propertyValueRecord);

	}

	/**
	 * Adds a region
	 * 
	 * @throws ContractException
	 *             <li>{@linkplain RegionError#NULL_REGION_ID} if the region id
	 *             is null</li>
	 *             <li>{@linkplain RegionError#DUPLICATE_REGION_ID} if the
	 *             region id was previously added</li>
	 */
	public void addRegion(RegionId regionId) {
		if (regionId == null) {
			throw new ContractException(RegionError.NULL_REGION_ID);
		}

		if (regionResources.containsKey(regionId)) {
			throw new ContractException(RegionError.DUPLICATE_REGION_ID);
		}

		final Map<ResourceId, RegionResourceRecord> map = new LinkedHashMap<>();
		regionResources.put(regionId, map);
		for (final ResourceId resourceId : personResourceValues.keySet()) {
			map.put(resourceId, new RegionResourceRecord(context));
		}

	}

	/**
	 * Constructs the PersonResourceManager from the context
	 * 
	 * @throws ContractException
	 *             <li>{@linkplain NucleusError#NULL_CONTEXT} if the context is
	 *             null</li>
	 */
	public ResourceDataManager(final Context context) {
		if (context == null) {
			throw new ContractException(NucleusError.NULL_CONTEXT);
		}
		this.context = context;
		personDataView = context.getDataView(PersonDataView.class).get();
	}

	/**
	 * Returns true if and only if the given resource id is known
	 */
	public boolean resourceIdExists(final ResourceId resourceId) {
		return personResourceValues.containsKey(resourceId);
	}

	/**
	 * Returns true if and only if the given resource property id is associated
	 * with the given resource id.
	 */
	public boolean resourcePropertyIdExists(final ResourceId resourceId, final ResourcePropertyId resourcePropertyId) {
		if (resourceId == null) {
			return false;
		}
		if (!resourcePropertyMap.containsKey(resourceId)) {
			return false;
		}
		if (resourcePropertyId == null) {
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

	/**
	 * Sets the value of the resource property. Values are not validated.
	 * 
	 * @throws RuntimeException
	 *             <li>if the resource id is null</li>
	 *             <li>if the resource id is unknown</li>
	 *             <li>if the resource property id is null</li>
	 *             <li>if the resource property id is unknown</li>
	 */
	public void setResourcePropertyValue(final ResourceId resourceId, final ResourcePropertyId resourcePropertyId, final Object resourcPropertyValue) {
		resourcePropertyMap.get(resourceId).get(resourcePropertyId).setPropertyValue(resourcPropertyValue);
	}

	/**
	 * Expands the capacity of data structures to hold people by the given
	 * count. Used to more efficiently prepare for bulk population additions.
	 */
	public void expandCapacity(int count) {
		for (final ResourceId resourceId : personResourceValues.keySet()) {
			IntValueContainer intValueContainer = personResourceValues.get(resourceId);
			intValueContainer.setCapacity(intValueContainer.getCapacity() + count);
			final TimeTrackingPolicy resourceTimeTrackingPolicy = resourceTimeTrackingPolicies.get(resourceId);
			if (resourceTimeTrackingPolicy == TimeTrackingPolicy.TRACK_TIME) {
				final DoubleValueContainer doubleValueContainer = personResourceTimes.get(resourceId);
				doubleValueContainer.setCapacity(doubleValueContainer.getCapacity() + count);
			}
		}
	}

}
