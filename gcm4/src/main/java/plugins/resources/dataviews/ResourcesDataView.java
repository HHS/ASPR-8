package plugins.resources.dataviews;

import java.util.List;
import java.util.Set;

import nucleus.DataView;
import plugins.people.support.PersonError;
import plugins.people.support.PersonId;
import plugins.regions.support.RegionError;
import plugins.regions.support.RegionId;
import plugins.resources.datamanagers.ResourcesDataManager;
import plugins.resources.support.ResourceError;
import plugins.resources.support.ResourceId;
import plugins.resources.support.ResourcePropertyId;
import plugins.util.properties.PropertyDefinition;
import plugins.util.properties.PropertyError;
import plugins.util.properties.TimeTrackingPolicy;
import util.errors.ContractException;

/**
 * Data view of the ResourcesDataManager
 *
 */

public final class ResourcesDataView implements DataView {

	private final ResourcesDataManager resourcesDataManager;

	/**
	 * Constructs this view from the corresponding data manager
	 * 
	 */
	public ResourcesDataView(ResourcesDataManager resourcesDataManager) {
		this.resourcesDataManager = resourcesDataManager;
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
		return resourcesDataManager.getPeopleWithoutResource(resourceId);
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
		return resourcesDataManager.getPeopleWithResource(resourceId);
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
		return resourcesDataManager.getPersonResourceLevel(resourceId, personId);
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
		return resourcesDataManager.getPersonResourceTime(resourceId, personId);
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
		return resourcesDataManager.getPersonResourceTimeTrackingPolicy(resourceId);
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
		return resourcesDataManager.getRegionResourceLevel(regionId, resourceId);
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
		return resourcesDataManager.getRegionResourceTime(regionId, resourceId);
	}

	/**
	 * Returns the resource ids
	 */
	public <T extends ResourceId> Set<T> getResourceIds() {
		return resourcesDataManager.getResourceIds();
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
	 *             <li>{@linkplain PropertyError#NULL_PROPERTY_ID} if the
	 *             resource property id is null</li>
	 *             <li>{@linkplain PropertyError#UNKNOWN_PROPERTY_ID} if the
	 *             resource property id is unknown</li>
	 */
	public PropertyDefinition getResourcePropertyDefinition(final ResourceId resourceId, final ResourcePropertyId resourcePropertyId) {
		return resourcesDataManager.getResourcePropertyDefinition(resourceId, resourcePropertyId);
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
		return resourcesDataManager.getResourcePropertyIds(resourceId);
	}

	/**
	 * Returns the last assignment time for the resource property
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
	 */

	public double getResourcePropertyTime(final ResourceId resourceId, final ResourcePropertyId resourcePropertyId) {
		return resourcesDataManager.getResourcePropertyTime(resourceId, resourcePropertyId);
	}

	/**
	 * Returns the value of the resource property.
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
	 */
	public <T> T getResourcePropertyValue(final ResourceId resourceId, final ResourcePropertyId resourcePropertyId) {
		return resourcesDataManager.getResourcePropertyValue(resourceId, resourcePropertyId);
	}

	/**
	 * Returns true if and only if the given resource id is known
	 */
	public boolean resourceIdExists(final ResourceId resourceId) {
		return resourcesDataManager.resourceIdExists(resourceId);
	}

	/**
	 * Returns true if and only if there is a resource property defined for the
	 * given resource id and resource property id
	 */
	public boolean resourcePropertyIdExists(final ResourceId resourceId, final ResourcePropertyId resourcePropertyId) {
		return resourcesDataManager.resourcePropertyIdExists(resourceId, resourcePropertyId);
	}

}