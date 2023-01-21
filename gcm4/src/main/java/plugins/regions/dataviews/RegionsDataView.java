package plugins.regions.dataviews;

import java.util.List;
import java.util.Set;

import nucleus.DataView;
import plugins.people.support.PersonError;
import plugins.people.support.PersonId;
import plugins.regions.RegionsPluginData;
import plugins.regions.datamanagers.RegionsDataManager;
import plugins.regions.support.RegionError;
import plugins.regions.support.RegionId;
import plugins.regions.support.RegionPropertyId;
import plugins.util.properties.PropertyDefinition;
import plugins.util.properties.PropertyError;
import plugins.util.properties.TimeTrackingPolicy;
import util.errors.ContractException;

/**
 * Data view of the RegionsDataManager
 *
 */
public final class RegionsDataView implements DataView {

	private final RegionsDataManager regionsDataManager;

	/**
	 * Constructs this view from the corresponding data manager
	 * 
	 */
	public RegionsDataView(RegionsDataManager regionsDataManager) {
		this.regionsDataManager = regionsDataManager;
	}

	/**
	 * Returns as a List the person identifiers of the people in the given
	 * region. List elements are unique.
	 *
	 * @throws ContractException
	 *             <li>{@linkplain RegionError#NULL_REGION_ID} if the c id is
	 *             null
	 *             <li>{@linkplain RegionError#UNKNOWN_REGION_ID} if the region
	 *             id is not known
	 */
	public List<PersonId> getPeopleInRegion(final RegionId regionId) {
		return regionsDataManager.getPeopleInRegion(regionId);
	}

	/**
	 * Returns the region associated with the given person id.
	 *
	 * @throwsContractException
	 *                          <li>{@linkplain PersonError#NULL_PERSON_ID} if
	 *                          the person id is null
	 *                          <li>{@linkplain PersonError#UNKNOWN_PERSON_ID}
	 *                          if the person id is unknown
	 */
	public <T extends RegionId> T getPersonRegion(final PersonId personId) {
		return regionsDataManager.getPersonRegion(personId);
	}

	/**
	 * Returns the time when then person arrived at their current region.
	 *
	 * @throwsContractException
	 *                          <li>{@linkplain PersonError#NULL_PERSON_ID} if
	 *                          the person id is null
	 *                          <li>{@linkplain PersonError#UNKNOWN_PERSON_ID}
	 *                          if the person id is unknown
	 *                          <li>{@linkplain RegionError#REGION_ARRIVAL_TIMES_NOT_TRACKED}
	 *                          if the region arrival times are not being
	 *                          tracked</li>
	 *
	 */
	public double getPersonRegionArrivalTime(final PersonId personId) {
		return regionsDataManager.getPersonRegionArrivalTime(personId);
	}

	/**
	 * Returns the policy for tracking the last region arrival time for each
	 * person
	 */
	public TimeTrackingPolicy getPersonRegionArrivalTrackingPolicy() {
		return regionsDataManager.getPersonRegionArrivalTrackingPolicy();
	}

	/**
	 * Returns the set of {@link RegionId} values that are defined by the
	 * {@link RegionsPluginData}.
	 */
	public <T extends RegionId> Set<T> getRegionIds() {
		return regionsDataManager.getRegionIds();
	}

	/**
	 * Returns the number of people currently in the given region.
	 *
	 * @throws ContractException
	 *             <li>{@linkplain RegionError#NULL_REGION_ID} if the region id
	 *             is null
	 *             <li>{@linkplain RegionError#UNKNOWN_REGION_ID} if the region
	 *             id is not known
	 */
	public int getRegionPopulationCount(final RegionId regionId) {
		return regionsDataManager.getRegionPopulationCount(regionId);
	}

	/**
	 * Returns the time when the current population of the given region was
	 * established.
	 *
	 * @throwsContractException
	 *                          <li>{@linkplain RegionError#NULL_REGION_ID} if
	 *                          the region id is null
	 *                          <li>{@linkplain RegionError#UNKNOWN_REGION_ID}
	 *                          if the region id is not known
	 */
	public double getRegionPopulationTime(final RegionId regionId) {
		return regionsDataManager.getRegionPopulationTime(regionId);
	}

	/**
	 * Returns the property definition for the given {@link RegionPropertyId}
	 *
	 * @throws ContractException
	 *             <li>{@linkplain PropertyError#NULL_PROPERTY_ID} if the region
	 *             property id is null</li>
	 *             <li>{@linkplain PropertyError#UNKNOWN_PROPERTY_ID} if the
	 *             region property id is unknown
	 *
	 */
	public PropertyDefinition getRegionPropertyDefinition(final RegionPropertyId regionPropertyId) {
		return regionsDataManager.getRegionPropertyDefinition(regionPropertyId);
	}

	/**
	 * Returns the time when the of the region property was last assigned.
	 *
	 * @throws ContractException
	 *             <li>{@linkplain RegionError#NULL_REGION_ID} if the region id
	 *             is null</li>
	 *             <li>{@linkplain RegionError#UNKNOWN_REGION_ID} if the region
	 *             id is not known</li>
	 *             <li>{@linkplain PropertyError#NULL_PROPERTY_ID} if the region
	 *             property id is null</li>
	 *             <li>{@linkplain PropertyError#UNKNOWN_PROPERTY_ID} if the
	 *             region property id is unknown</li>
	 */
	public double getRegionPropertyTime(final RegionId regionId, final RegionPropertyId regionPropertyId) {
		return regionsDataManager.getRegionPropertyTime(regionId, regionPropertyId);
	}

	/**
	 * Returns the value of the region property.
	 *
	 * @throws ContractException
	 *             <li>{@linkplain RegionError#NULL_REGION_ID} if the region id
	 *             is null</li>
	 *             <li>{@linkplain RegionError#UNKNOWN_REGION_ID} if the region
	 *             id is not known</li>
	 *             <li>{@linkplain PropertyError#NULL_PROPERTY_ID} if the region
	 *             property id is null</li>
	 *             <li>{@linkplain PropertyError#UNKNOWN_PROPERTY_ID} if the
	 *             region property id is unknown</li>
	 */
	public <T> T getRegionPropertyValue(final RegionId regionId, final RegionPropertyId regionPropertyId) {
		return regionsDataManager.getRegionPropertyValue(regionId, regionPropertyId);
	}

	/**
	 * Return true if and only if the given {@link RegionId} exits. Null
	 * tolerant.
	 */
	public boolean regionIdExists(final RegionId regionId) {
		return regionsDataManager.regionIdExists(regionId);
	}

	/**
	 * Returns true if and only if the given {@link RegionPropertyId} exists.
	 * Tolerates nulls.
	 */
	public boolean regionPropertyIdExists(final RegionPropertyId regionPropertyId) {
		return regionsDataManager.regionPropertyIdExists(regionPropertyId);
	}

}
