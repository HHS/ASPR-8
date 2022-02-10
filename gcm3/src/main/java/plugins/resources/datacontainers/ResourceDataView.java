package plugins.resources.datacontainers;

import java.util.List;
import java.util.Set;

import nucleus.SimulationContext;
import nucleus.DataView;
import nucleus.NucleusError;
import plugins.people.datacontainers.PersonDataView;
import plugins.people.support.PersonError;
import plugins.people.support.PersonId;
import plugins.properties.support.PropertyDefinition;
import plugins.properties.support.TimeTrackingPolicy;
import plugins.regions.datacontainers.RegionDataView;
import plugins.regions.support.RegionError;
import plugins.regions.support.RegionId;
import plugins.resources.support.ResourceError;
import plugins.resources.support.ResourceId;
import plugins.resources.support.ResourcePropertyId;
import util.ContractException;

/**
 * Published data view that provides information for person resources, region
 * resources and resource properties.
 * 
 * @author Shawn Hatch
 *
 */
public final class ResourceDataView implements DataView {

	private final ResourceDataManager resourceDataManager;

	private final SimulationContext simulationContext;

	private PersonDataView personDataView;

	private RegionDataView regionDataView;

	public ResourceDataView(SimulationContext simulationContext, ResourceDataManager resourceDataManager) {
		if (simulationContext == null) {
			throw new ContractException(NucleusError.NULL_CONTEXT);
		}
		if (resourceDataManager == null) {
			throw new ContractException(ResourceError.NULL_RESOURCE_DATA_MANAGER);
		}
		this.simulationContext = simulationContext;
		this.resourceDataManager = resourceDataManager;
		personDataView = simulationContext.getDataView(PersonDataView.class).get();
		regionDataView = simulationContext.getDataView(RegionDataView.class).get();
	}

	/**
	 * Returns the list of people who have a zero level of the resource
	 * 
	 * @throws ContractException
	 *             <li>{@linkplain ResourceError#NULL_RESOURCE_ID} if the
	 *             resource id is null</li>
	 *             <li>{@linkplain ResourceError#UNKNOWN_RESOURCE_ID} if the
	 *             resource id is unknown</li>
	 */
	public List<PersonId> getPeopleWithoutResource(final ResourceId resourceId) {
		validateResourceId(resourceId);
		return resourceDataManager.getPeopleWithoutResource(resourceId);
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
		return resourceDataManager.getPeopleWithResource(resourceId);
	}

	/**
	 * Returns the resource level for the given person and resource
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
	 */
	public long getPersonResourceLevel(final ResourceId resourceId, final PersonId personId) {
		validatePersonExists(personId);
		validateResourceId(resourceId);
		return resourceDataManager.getPersonResourceLevel(resourceId, personId);
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
		return resourceDataManager.getPersonResourceTime(resourceId, personId);
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
	public long getRegionResourceLevel(RegionId regionId, ResourceId resourceId) {
		validateRegionId(regionId);
		validateResourceId(resourceId);
		return resourceDataManager.getRegionResourceLevel(regionId, resourceId);
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
	public double getRegionResourceTime(RegionId regionId, ResourceId resourceId) {
		validateRegionId(regionId);
		validateResourceId(resourceId);
		return resourceDataManager.getRegionResourceTime(regionId, resourceId);
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
	public TimeTrackingPolicy getPersonResourceTimeTrackingPolicy(ResourceId resourceId) {
		validateResourceId(resourceId);
		return resourceDataManager.getPersonResourceTimeTrackingPolicy(resourceId);
	}

	/**
	 * Returns the resource ids
	 */
	public <T extends ResourceId> Set<T> getResourceIds() {
		return resourceDataManager.getResourceIds();
	}

	/**
	 * Returns true if and only if there is a resource property defined for the
	 * given resource id and resource property id
	 */
	public boolean resourcePropertyIdExists(ResourceId resourceId, ResourcePropertyId resourcePropertyId) {
		return resourceDataManager.resourcePropertyIdExists(resourceId, resourcePropertyId);
	}

	/**
	 * Returns true if and only if the given resource id exists
	 */
	public boolean resourceIdExists(ResourceId resourceId) {
		return resourceDataManager.resourceIdExists(resourceId);
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
		return resourceDataManager.getResourcePropertyDefinition(resourceId, resourcePropertyId);
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
	public <T extends ResourcePropertyId> Set<T> getResourcePropertyIds(final ResourceId resourceId) {
		validateResourceId(resourceId);
		return resourceDataManager.getResourcePropertyIds(resourceId);
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
	public <T> T getResourcePropertyValue(ResourceId resourceId, ResourcePropertyId resourcePropertyId) {
		validateResourceId(resourceId);
		validateResourcePropertyId(resourceId, resourcePropertyId);
		return resourceDataManager.getResourcePropertyValue(resourceId, resourcePropertyId);
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

	public double getResourcePropertyTime(ResourceId resourceId, ResourcePropertyId resourcePropertyId) {
		validateResourceId(resourceId);
		validateResourcePropertyId(resourceId, resourcePropertyId);
		return resourceDataManager.getResourcePropertyTime(resourceId, resourcePropertyId);
	}

	private void validatePersonExists(final PersonId personId) {
		if (personId == null) {
			simulationContext.throwContractException(PersonError.NULL_PERSON_ID);
		}
		if (!personDataView.personExists(personId)) {
			simulationContext.throwContractException(PersonError.UNKNOWN_PERSON_ID);
		}
	}

	/*
	 * Preconditions: the resource id must exist
	 */
	private void validatePersonResourceTimesTracked(final ResourceId resourceId) {
		if (resourceDataManager.getPersonResourceTimeTrackingPolicy(resourceId) != TimeTrackingPolicy.TRACK_TIME) {
			simulationContext.throwContractException(ResourceError.RESOURCE_ASSIGNMENT_TIME_NOT_TRACKED);
		}
	}

	private void validateRegionId(final RegionId regionId) {

		if (regionId == null) {
			simulationContext.throwContractException(RegionError.NULL_REGION_ID);
		}

		if (!regionDataView.regionIdExists(regionId)) {
			simulationContext.throwContractException(RegionError.UNKNOWN_REGION_ID, regionId);
		}
	}

	private void validateResourceId(final ResourceId resourceId) {
		if (resourceId == null) {
			simulationContext.throwContractException(ResourceError.NULL_RESOURCE_ID);
		}
		if (!resourceDataManager.resourceIdExists(resourceId)) {
			simulationContext.throwContractException(ResourceError.UNKNOWN_RESOURCE_ID, resourceId);
		}
	}

	private void validateResourcePropertyId(final ResourceId resourceId, final ResourcePropertyId resourcePropertyId) {
		if (resourcePropertyId == null) {
			simulationContext.throwContractException(ResourceError.NULL_RESOURCE_PROPERTY_ID);
		}

		if (!resourceDataManager.resourcePropertyIdExists(resourceId, resourcePropertyId)) {
			simulationContext.throwContractException(ResourceError.UNKNOWN_RESOURCE_PROPERTY_ID, resourcePropertyId);
		}
	}
}
